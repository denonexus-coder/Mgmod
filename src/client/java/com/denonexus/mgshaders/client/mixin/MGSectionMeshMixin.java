package com.denonexus.mgshaders.client.mixin;

import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionBuffers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Expõe os métodos de consulta de SectionMesh.
 *
 * Mojang 1.21.11 mapping (htr):
 *   SectionBuffers getBuffers(ChunkSectionLayer)  → htr.a(ChunkSectionLayer)
 *   boolean hasRenderableLayers()                 → htr.b()
 *   boolean hasTranslucentGeometry()              → htr.c()
 *   boolean isEmpty(ChunkSectionLayer)            → htr.d(ChunkSectionLayer)
 *
 * SectionMesh é o objeto produzido por SectionCompiler.compile() e
 * armazenado em RenderSection.sectionMesh via AtomicReference.
 * Contém o GpuBuffer de vértices e índices para cada layer de render.
 */
@Mixin(SectionMesh.class)
public interface MGSectionMeshMixin {

    @Invoker("getBuffers")
    SectionBuffers mg$getBuffers(ChunkSectionLayer layer);

    @Invoker("hasRenderableLayers")
    boolean mg$hasRenderableLayers();

    @Invoker("hasTranslucentGeometry")
    boolean mg$hasTranslucentGeometry();

    @Invoker("isEmpty")
    boolean mg$isEmpty(ChunkSectionLayer layer);
}
