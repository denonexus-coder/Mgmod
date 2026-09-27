#include <fcntl.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <unistd.h>
#include <zlib.h>

#include <cstdint>
#include <cstdlib>
#include <cstring>

struct McaLoader {
    int fd;
    uint8_t* data;
    size_t size;
};

static int remap_if_needed(McaLoader* loader) {
    struct stat st{};

    if (fstat(loader->fd, &st) != 0) {
        return -1;
    }

    if (static_cast<size_t>(st.st_size) == loader->size) {
        return 0;
    }

    if (loader->data && loader->size) {
        munmap(loader->data, loader->size);
    }

    if (st.st_size < 8192) {
        loader->data = nullptr;
        loader->size = 0;
        return -2;
    }

    void* mapped = mmap(
        nullptr,
        static_cast<size_t>(st.st_size),
        PROT_READ,
        MAP_SHARED,
        loader->fd,
        0
    );

    if (mapped == MAP_FAILED) {
        loader->data = nullptr;
        loader->size = 0;
        return -3;
    }

    loader->data = static_cast<uint8_t*>(mapped);
    loader->size = static_cast<size_t>(st.st_size);

    madvise(
        mapped,
        loader->size,
        MADV_RANDOM
    );

    return 0;
}

extern "C"
__attribute__((visibility("default")))
int64_t mca_open(const char* path) {

    if (!path) {
        return 0;
    }

    int fd = open(
        path,
        O_RDONLY | O_CLOEXEC
    );

    if (fd < 0) {
        return 0;
    }

    struct stat st{};

    if (
        fstat(fd, &st) != 0 ||
        st.st_size < 8192
    ) {
        close(fd);
        return 0;
    }

    void* mapped = mmap(
        nullptr,
        static_cast<size_t>(st.st_size),
        PROT_READ,
        MAP_SHARED,
        fd,
        0
    );

    if (mapped == MAP_FAILED) {
        close(fd);
        return 0;
    }

    madvise(
        mapped,
        static_cast<size_t>(st.st_size),
        MADV_RANDOM
    );

    McaLoader* loader =
        static_cast<McaLoader*>(
            malloc(sizeof(McaLoader))
        );

    if (!loader) {
        munmap(
            mapped,
            static_cast<size_t>(st.st_size)
        );
        close(fd);
        return 0;
    }

    loader->fd = fd;
    loader->data = static_cast<uint8_t*>(mapped);
    loader->size = static_cast<size_t>(st.st_size);

    return reinterpret_cast<int64_t>(loader);
}

extern "C"
__attribute__((visibility("default")))
int32_t mca_read(
    int64_t handle,
    int32_t chunkX,
    int32_t chunkZ,
    uint8_t* output,
    uint32_t outputCapacity
) {

    McaLoader* loader =
        reinterpret_cast<McaLoader*>(handle);

    if (
        !loader ||
        !loader->data ||
        !output ||
        outputCapacity == 0
    ) {
        return -1;
    }

    uint32_t index =
        static_cast<uint32_t>(
            (chunkX & 31) +
            (chunkZ & 31) * 32
        );

    size_t headerOffset =
        static_cast<size_t>(index) * 4;

    if (headerOffset + 4 > loader->size) {
        return -2;
    }

    uint32_t packed =
        (static_cast<uint32_t>(loader->data[headerOffset]) << 24) |
        (static_cast<uint32_t>(loader->data[headerOffset + 1]) << 16) |
        (static_cast<uint32_t>(loader->data[headerOffset + 2]) << 8) |
        static_cast<uint32_t>(loader->data[headerOffset + 3]);

    uint32_t sectorOffset =
        packed >> 8;

    uint32_t sectorCount =
        packed & 0xFFu;

    if (
        sectorOffset == 0 ||
        sectorCount == 0
    ) {
        return 0;
    }

    size_t byteOffset =
        static_cast<size_t>(sectorOffset) * 4096u;

    if (byteOffset + 5 > loader->size) {
        if (remap_if_needed(loader) != 0) {
            return -3;
        }

        if (byteOffset + 5 > loader->size) {
            return -3;
        }
    }

    uint32_t length =
        (static_cast<uint32_t>(loader->data[byteOffset]) << 24) |
        (static_cast<uint32_t>(loader->data[byteOffset + 1]) << 16) |
        (static_cast<uint32_t>(loader->data[byteOffset + 2]) << 8) |
        static_cast<uint32_t>(loader->data[byteOffset + 3]);

    uint8_t compression =
        loader->data[byteOffset + 4];

    if (compression & 0x80u) {
        return -8;
    }

    if (length < 1) {
        return -4;
    }

    uint32_t compressedSize =
        length - 1;

    uint64_t sectorCapacity =
        static_cast<uint64_t>(sectorCount) * 4096ull;

    if (
        static_cast<uint64_t>(length) + 4ull >
        sectorCapacity
    ) {
        return -4;
    }

    const uint8_t* input =
        loader->data +
        byteOffset +
        5;

    size_t inputOffset =
        static_cast<size_t>(
            input - loader->data
        );

    if (
        inputOffset + compressedSize >
        loader->size
    ) {
        return -3;
    }

    if (compression == 3) {
        if (compressedSize > outputCapacity) {
            return -5;
        }

        memcpy(
            output,
            input,
            compressedSize
        );

        return static_cast<int32_t>(
            compressedSize
        );
    }

    int windowBits = 0;

    if (compression == 1) {
        windowBits = 31;
    } else if (compression == 2) {
        windowBits = 15;
    } else {
        return -6;
    }

    z_stream stream{};
    stream.next_in =
        const_cast<Bytef*>(
            reinterpret_cast<const Bytef*>(input)
        );
    stream.avail_in = compressedSize;
    stream.next_out = reinterpret_cast<Bytef*>(output);
    stream.avail_out = outputCapacity;

    if (inflateInit2(&stream, windowBits) != Z_OK) {
        return -7;
    }

    int result = inflate(&stream, Z_FINISH);
    uint32_t produced = outputCapacity - stream.avail_out;
    inflateEnd(&stream);

    if (result == Z_STREAM_END) {
        return static_cast<int32_t>(produced);
    }

    if (result == Z_BUF_ERROR && produced > 0) {
        return -9;
    }

    return -7;
}

extern "C"
__attribute__((visibility("default")))
void mca_close(
    int64_t handle
) {
    McaLoader* loader =
        reinterpret_cast<McaLoader*>(handle);
    if (!loader) return;

    if (loader->data && loader->size) {
        munmap(loader->data, loader->size);
    }
    close(loader->fd);
    free(loader);
}
