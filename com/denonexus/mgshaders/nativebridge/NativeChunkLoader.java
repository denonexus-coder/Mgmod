package com.denonexus.mgshaders.nativebridge;
import java.nio.ByteBuffer;

public class NativeChunkLoader {
    static { System.loadLibrary("compress-decompress"); }

    public static native int decompressLZ4Direct(
        ByteBuffer srcBuffer, int srcOffset, int srcLen,
        ByteBuffer dstBuffer, int dstOffset, int maxDstLen);

    public static native int decompressLZ4Array(
        byte[] srcArray, int srcLen, byte[] dstArray, int maxDstLen);
}
