// native_chunk.cpp — leitor de chunk .mca via FFM
// Formato .mca:
//   Header: 8 KB (1024 * 4-byte offsets + 1024 * 4-byte timestamps)
//   Chunk: 4-byte length (big-endian) + 1-byte compression + payload
//   Compression: 1=gzip, 2=zlib, 3=uncompressed, 4=LZ4
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <zlib.h>

#if defined(_WIN32)
  #define EXPORT __declspec(dllexport)
#else
  #define EXPORT __attribute__((visibility("default")))
#endif

extern "C" {

// Lê um chunk completo de um .mca.
// Retorna bytes escritos em out, ou negativo em caso de erro.
//   -1 = não conseguiu abrir arquivo
//   -2 = chunk não existe no arquivo (offset = 0)
//   -3 = falha de leitura
//   -4 = compressão não suportada (LZ4, etc.)
//   -5 = falha de descompressão zlib
EXPORT int64_t mg_read_chunk(const char* path,
                             int32_t chunk_x, int32_t chunk_z,
                             uint8_t* out, int64_t out_cap) {
    if (!path || !out || out_cap <= 0) return -1;

    FILE* f = fopen(path, "rb");
    if (!f) return -1;

    // Offset entry = (chunk_x & 31) + (chunk_z & 31) * 32
    int lx = chunk_x & 31;
    int lz = chunk_z & 31;
    int entry_index = lx + lz * 32;

    // Lê a entry de offset (4 bytes, big-endian)
    long header_pos = (long)entry_index * 4;
    if (fseek(f, header_pos, SEEK_SET) != 0) { fclose(f); return -3; }

    uint8_t off_b[4];
    if (fread(off_b, 1, 4, f) != 4) { fclose(f); return -3; }

    // 3 bytes = offset em setores; 1 byte = número de setores
    uint32_t sector_off = ((uint32_t)off_b[0] << 16) |
                          ((uint32_t)off_b[1] << 8)  |
                           (uint32_t)off_b[2];
    uint8_t sector_count = off_b[3];

    if (sector_off == 0 || sector_count == 0) { fclose(f); return -2; }

    // Vai para o início do chunk
    if (fseek(f, (long)sector_off * 4096L, SEEK_SET) != 0) { fclose(f); return -3; }

    // Lê 5 bytes de cabeçalho: 4-byte length + 1-byte compression
    uint8_t header[5];
    if (fread(header, 1, 5, f) != 5) { fclose(f); return -3; }

    uint32_t length = ((uint32_t)header[0] << 24) |
                      ((uint32_t)header[1] << 16) |
                      ((uint32_t)header[2] << 8)  |
                       (uint32_t)header[3];
    uint8_t compression = header[4];

    if (length <= 1) { fclose(f); return -2; }
    if (length > (uint32_t)sector_count * 4096u) { fclose(f); return -3; }

    uint32_t payload_len = length - 1; // desconta o byte de compression
    if (payload_len == 0 || payload_len > out_cap) { fclose(f); return -5; }

    // Lê payload direto para o buffer de saída (se for raw, é o resultado)
    if (compression == 3) {
        size_t rd = fread(out, 1, payload_len, f);
        fclose(f);
        return (rd == payload_len) ? (int64_t)rd : -3;
    }

    // Para zlib/gzip, precisa de buffer temporário
    uint8_t* tmp = (uint8_t*)malloc(payload_len);
    if (!tmp) { fclose(f); return -5; }
    size_t rd = fread(tmp, 1, payload_len, f);
    fclose(f);
    if (rd != payload_len) { free(tmp); return -3; }

    if (compression == 2 || compression == 1) {
        // zlib (2) ou gzip (1) — usar zlib com wbits corretos
        z_stream zs;
        memset(&zs, 0, sizeof(zs));
        int wbits = (compression == 1) ? 16 + MAX_WBITS : MAX_WBITS;
        if (inflateInit2(&zs, wbits) != Z_OK) { free(tmp); return -5; }

        zs.next_in  = tmp;
        zs.avail_in = (uInt)payload_len;
        zs.next_out = out;
        zs.avail_out = (uInt)out_cap;

        int ret = inflate(&zs, Z_FINISH);
        int64_t written = (int64_t)((uint8_t*)zs.next_out - out);
        inflateEnd(&zs);
        free(tmp);

        if (ret != Z_STREAM_END) return -5;
        return written;
    }

    free(tmp);
    return -4; // LZ4 ou outro — não suportado
}

// Versão de teste: só conta quantos chunks válidos existem no arquivo.
EXPORT int32_t mg_count_chunks(const char* path) {
    FILE* f = fopen(path, "rb");
    if (!f) return -1;
    uint8_t header[4096];
    size_t rd = fread(header, 1, 4096, f);
    fclose(f);
    if (rd != 4096) return -1;
    int count = 0;
    for (int i = 0; i < 1024; i++) {
        uint32_t off = ((uint32_t)header[i*4] << 16) |
                       ((uint32_t)header[i*4+1] << 8) |
                        (uint32_t)header[i*4+2];
        if (off != 0) count++;
    }
    return count;
}

} // extern "C"
