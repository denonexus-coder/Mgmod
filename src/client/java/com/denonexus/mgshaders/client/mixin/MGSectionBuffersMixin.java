package com.denonexus.mgshaders.client.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import net.minecraft.client.renderer.chunk.SectionBuffers;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Expõe os campos de SectionBuffers para leitura direta.
 *
 * SectionBuffers é o objeto que contém os GpuBuffers reais de uma seção
 * para um ChunkSectionLayer específico.
 *
 * Mojang 1.21.11 mapping (hto):
 *   GpuBuffer vertexBuffer  → hto.a
 *   GpuBuffer indexBuffer   → hto.b
 *   int indexCount          → hto.c
 *   VertexFormat.IndexType indexType → hto.d
 *
 * Esses quatro campos são tudo o que o native bridge precisa para montar
 * um glMultiDrawElementsBaseVertexEXT:
 *   - vertexBuffer: fonte dos vértices para este draw
 *   - indexBuffer:  fonte dos índices para este draw
 *   - indexCount:   count[] do multi-draw
 *   - indexType:    GL_UNSIGNED_SHORT ou GL_UNSIGNED_INT
 */
@Mixin(SectionBuffers.class)
public interface MGSectionBuffersMixin {

    @Accessor("vertexBuffer")
    GpuBuffer mg$getVertexBuffer();

    @Accessor("indexBuffer")
    GpuBuffer mg$getIndexBuffer();

    @Accessor("indexCount")
    int mg$getIndexCount();

    @Accessor("indexType")
    VertexFormat.IndexType mg$getIndexType();
}
