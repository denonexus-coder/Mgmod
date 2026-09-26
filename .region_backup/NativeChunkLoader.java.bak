package com.denonexus.mgshaders.nativebridge;

import java.nio.ByteBuffer;

public class NativeChunkLoader {
    static {
        try {
            System.loadLibrary("compress-decompress");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("[MGShaders] Falha ao carregar libcompress-decompress.so nativa: " + e.getMessage());
        }
    }

    public static native int decompressLZ4Direct(ByteBuffer srcBuf, int srcOff, int srcLen, ByteBuffer dstBuf, int dstOff, int maxDstLen);
    public static native int compressLZ4Direct(ByteBuffer srcBuf, int srcOff, int srcLen, ByteBuffer dstBuf, int dstOff, int maxDstLen);
}
