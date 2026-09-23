package com.denonexus.mgshaders.nativebridge;

import com.denonexus.mgshaders.MGShaders;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Bridge FFM para leitura nativa de chunk .mca.
 * Requer --enable-preview em compile e runtime (Java 21).
 */
public final class NativeChunkLoader {

    private static boolean initialized = false;
    private static boolean available = false;
    private static String reason = "not initialized";

    private static MethodHandle hReadChunk;
    private static MethodHandle hCountChunks;

    private NativeChunkLoader() {}

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        // Tenta extrair o .so empacotado no jar
        try {
            Path nativeDir = extractNativeIfNeeded();
            System.load(nativeDir.toAbsolutePath().toString());
            SymbolLookup lookup = SymbolLookup.loaderLookup();
            Linker linker = Linker.nativeLinker();

            hReadChunk = linker.downcallHandle(
                lookup.find("mg_read_chunk").orElseThrow(),
                FunctionDescriptor.of(
                    ValueLayout.JAVA_LONG,
                    ValueLayout.ADDRESS,   // path
                    ValueLayout.JAVA_INT,  // chunk_x
                    ValueLayout.JAVA_INT,  // chunk_z
                    ValueLayout.ADDRESS,   // out
                    ValueLayout.JAVA_LONG  // out_cap
                )
            );

            hCountChunks = linker.downcallHandle(
                lookup.find("mg_count_chunks").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
            );

            available = true;
            reason = null;
            MGShaders.LOGGER.info("[MGShaders] NativeChunkLoader conectada");
        } catch (Throwable t) {
            reason = t.getClass().getSimpleName() + ": " + t.getMessage();
            MGShaders.LOGGER.warn("[MGShaders] NativeChunkLoader indisponível — {}", reason);
        }
    }

    private static Path extractNativeIfNeeded() throws Exception {
        // Caminho do .so empacotado no jar
        var url = NativeChunkLoader.class.getResource("/natives/linux-aarch64/libnativechunk.so");
        if (url == null) throw new IllegalStateException("libnativechunk.so não empacotada no jar");

        Path tmp = Files.createTempDirectory("mgshaders_native");
        Path out = tmp.resolve("libnativechunk.so");
        try (var in = url.openStream()) {
            Files.copy(in, out);
        }
        out.toFile().deleteOnExit();
        return out;
    }

    public static boolean isAvailable() { return available; }
    public static String getReason() { return reason; }

    /**
     * Lê um chunk nativo. Retorna os bytes descomprimidos ou null se não existir/erro.
     *
     * @param mcaPath   caminho do arquivo .mca
     * @param chunkX    coordenada X global do chunk
     * @param chunkZ    coordenada Z global do chunk
     * @param maxBytes  tamanho máximo esperado (2 MB é seguro)
     */
    public static byte[] readChunk(Path mcaPath, int chunkX, int chunkZ, int maxBytes) {
        if (!available || hReadChunk == null) return null;
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cPath = arena.allocateUtf8String(mcaPath.toAbsolutePath().toString());
            MemorySegment buf = arena.allocate(maxBytes);

            long written = (long) hReadChunk.invokeExact(
                cPath,
                chunkX, chunkZ,
                buf,
                (long) maxBytes
            );

            if (written < 0) return null;
            return buf.asSlice(0, written).toArray(ValueLayout.JAVA_BYTE);
        } catch (Throwable t) {
            return null;
        }
    }

    public static int countChunks(Path mcaPath) {
        if (!available || hCountChunks == null) return -1;
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cPath = arena.allocateUtf8String(mcaPath.toAbsolutePath().toString());
            return (int) hCountChunks.invokeExact(cPath);
        } catch (Throwable t) {
            return -1;
        }
    }
}
