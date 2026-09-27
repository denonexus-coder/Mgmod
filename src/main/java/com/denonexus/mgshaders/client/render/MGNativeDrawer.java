package com.denonexus.mgshaders.client.render;

public final class MGNativeDrawer {
    private static boolean initialized;

    private MGNativeDrawer() {}

    public static native boolean init();
    public static native long createBatch();
    public static native void uploadBatch(long batchHandle, int[] counts, long[] offsets, int[] baseVertices, int drawCount);
    public static native void drawBatch(long batchHandle, int vao, int vertexBuffer, int indexBuffer, int indexType);
    public static native void destroyBatch(long batchHandle);

    public static void initialize() {
        if (initialized) return;
        if (!init()) {
            throw new IllegalStateException("MobileGlues native multi-basevertex drawer unavailable");
        }
        initialized = true;
    }
}
