package com.denonexus.mgshaders.nativebridge;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class McaNativeLoader {

    private static final int BUFFER_SIZE =
            4 * 1024 * 1024;

    private static volatile boolean loaded;

    static {
        loaded = loadLibraries();
    }

    private McaNativeLoader() {
    }

    private static boolean loadLibraries() {

        if (!isArm64()) {
            return false;
        }

        try {
            Path dir =
                    Path.of(
                            System.getProperty(
                                    "java.io.tmpdir"
                            ),
                            "mgshaders-mca-native"
                    );

            Files.createDirectories(dir);

            Path fast =
                    extract(
                            "/natives/linux-aarch64/libmca_fast.so",
                            dir.resolve(
                                    "libmca_fast.so"
                            )
                    );

            Path jni =
                    extract(
                            "/natives/linux-aarch64/libmca_jni.so",
                            dir.resolve(
                                    "libmca_jni.so"
                            )
                    );

            System.load(
                    fast.toAbsolutePath()
                            .toString()
            );

            System.load(
                    jni.toAbsolutePath()
                            .toString()
            );

            return true;

        } catch (Throwable error) {

            System.err.println(
                    "[MGShaders] MCA native disabled: "
                            + error
            );

            return false;
        }
    }

    private static Path extract(
            String resource,
            Path destination
    ) throws Exception {

        try (
                InputStream input =
                        McaNativeLoader.class
                                .getResourceAsStream(
                                        resource
                                )
        ) {

            if (input == null) {
                throw new IllegalStateException(
                        "Missing native resource: "
                                + resource
                );
            }

            Files.copy(
                    input,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        destination.toFile()
                .setExecutable(
                        true,
                        true
                );

        return destination;
    }

    private static boolean isArm64() {

        String arch =
                System.getProperty(
                        "os.arch",
                        ""
                ).toLowerCase();

        return arch.contains("aarch64")
                || arch.contains("arm64");
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static ByteBuffer newBuffer() {
        return ByteBuffer.allocateDirect(
                BUFFER_SIZE
        );
    }

    public static native long open(
            String path
    );

    public static native int read(
            long handle,
            int chunkX,
            int chunkZ,
            ByteBuffer output,
            int capacity
    );

    public static native void close(
            long handle
    );
}
