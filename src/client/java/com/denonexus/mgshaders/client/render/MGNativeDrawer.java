package com.denonexus.mgshaders.client.render;

/**
 * JNI Bridge para comunicação direta com o MobileGlues.
 * Fornece acesso às funções nativas de batching e submissão
 * via glMultiDrawElementsBaseVertexEXT no PowerVR.
 */
public final class MGNativeDrawer {

    private static boolean initialized = false;

    private MGNativeDrawer() {
    }

    /**
     * Inicializa a bridge nativa. Verifica se o backend suporta a extensão requerida.
     * @return true se MobileGlues e MultiDraw estão disponíveis.
     */
    public static native boolean init();

    /**
     * Cria um novo batch persistente na GPU.
     * @return Handle do batch.
     */
    public static native long createBatch();

    /**
     * Faz o upload dos arrays de batch para a memória nativa.
     * @param batchHandle Handle retornado por createBatch.
     * @param counts Array de index counts.
     * @param offsets Array de offsets (em bytes).
     * @param baseVertices Array de base vertices.
     * @param drawCount Número de draws no batch.
     */
    public static native void uploadBatch(
            long batchHandle,
            int[] counts,
            long[] offsets,
            int[] baseVertices,
            int drawCount
    );

    /**
     * Submete o batch para a GPU.
     * @param batchHandle Handle retornado por createBatch.
     * @param vao Vertex Array Object atual (se aplicável).
     * @param vertexBuffer ID do GpuBuffer de vértices atual.
     * @param indexBuffer ID do GpuBuffer de índices atual.
     * @param indexType GL_UNSIGNED_SHORT ou GL_UNSIGNED_INT.
     */
    public static native void drawBatch(
            long batchHandle,
            int vao,
            int vertexBuffer,
            int indexBuffer,
            int indexType
    );

    /**
     * Destrói o batch e libera a memória nativa.
     */
    public static native void destroyBatch(long batchHandle);

    /**
     * Garante a inicialização da biblioteca nativa.
     */
    public static void initialize() {
        if (initialized) return;
        if (!init()) {
            throw new IllegalStateException("MobileGlues native multi-basevertex drawer unavailable");
        }
        initialized = true;
    }
}
