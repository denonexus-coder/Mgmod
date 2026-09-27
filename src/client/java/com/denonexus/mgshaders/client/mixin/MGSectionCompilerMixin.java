package com.denonexus.mgshaders.client.mixin;

import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.SectionPos;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Expõe SectionCompiler.compile() como Invoker.
 *
 * Mojang 1.21.11 mapping (htp):
 *   SectionCompiler.Results compile(
 *       SectionPos, RenderSectionRegion, VertexSorting, SectionBufferBuilderPack
 *   )
 *
 * Permite ao pipeline MG acionar recompilações síncronas de seções
 * específicas sem passar pelo scheduler de prioridade de RebuildTask.
 * Útil para pré-aquecimento de regiões críticas (próximas ao jogador).
 */
@Mixin(SectionCompiler.class)
public interface MGSectionCompilerMixin {

    @Invoker("compile")
    SectionCompiler.Results mg$compile(
            SectionPos sectionPos,
            RenderSectionRegion region,
            VertexSorting vertexSorting,
            SectionBufferBuilderPack buffers
    );
}
