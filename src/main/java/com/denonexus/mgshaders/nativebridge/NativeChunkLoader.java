package com.denonexus.mgshaders.nativebridge;

import com.denonexus.mgshaders.region.storage.RegionLz4Cache;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.*;

public final class NativeChunkLoader {

    private static volatile boolean loaded;

    static {
        loaded = loadNative();
        RegionLz4Cache.markNativeReady(loaded);
    }

    private static boolean loadNative() {

        try {
            System.loadLibrary(
                    "compress-decompress"
            );
            return true;
        } catch (UnsatisfiedLinkError ignored) {
        }

        String arch =
                System.getProperty(
                        "os.arch",
                        ""
                );

        String base =
                (
                        arch.contains("aarch64")
                                ||
                        arch.contains("arm64")
                )
                        ?
                        "/natives/linux-aarch64/"
                        :
                        null;

        if (base == null) {
            return false;
        }

        try {

            Path dir =
                    Paths.get(
                            System.getProperty(
                                    "java.io.tmpdir"
                            ),
                            "mgshaders-native"
                    );

            Files.createDirectories(dir);

            /*
             * The native LZ4 library may depend on libz.
             * Extract both together for Android arm64.
             */
            Path zlib =
                    dir.resolve(
                            "libz.so.1"
                    );

            try (
                    InputStream zin =
                            NativeChunkLoader.class
                                    .getResourceAsStream(
                                            base +
                                                    "libz.so.1"
                                    )
            ) {

                if (zin != null) {
                    Files.copy(
                            zin,
                            zlib,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }

            if (Files.isRegularFile(zlib)) {
                try {
                    System.load(
                            zlib.toAbsolutePath()
                                    .toString()
                    );
                } catch (Throwable ignored) {
                }
            }

            Path lib =
                    dir.resolve(
                            "libcompress-decompress.so"
                    );

            try (
                    InputStream input =
                            NativeChunkLoader.class
                                    .getResourceAsStream(
                                            base +
                                                    "libcompress-decompress.so"
                                    )
            ) {

                if (input == null) {
                    return false;
                }

                Files.copy(
                        input,
                        lib,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            lib.toFile()
                    .setExecutable(
                            true,
                            true
                    );

            System.load(
                    lib.toAbsolutePath()
                            .toString()
            );

            return true;

        } catch (Throwable t) {

            System.err.println(
                    "[MGShaders] LZ4 native unavailable: "
                            +
                            t.getMessage()
            );

            return false;
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static native int
    decompressLZ4Direct(
            ByteBuffer srcBuf,
            int srcOff,
            int srcLen,
            ByteBuffer dstBuf,
            int dstOff,
            int maxDstLen
    );

    public static native int
    compressLZ4Direct(
            ByteBuffer srcBuf,
            int srcOff,
            int srcLen,
            ByteBuffer dstBuf,
            int dstOff,
            int maxDstLen
    );
}
