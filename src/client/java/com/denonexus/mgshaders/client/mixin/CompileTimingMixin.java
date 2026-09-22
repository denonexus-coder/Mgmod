package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.PipelineProfiler;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionCompiler.class)
public class CompileTimingMixin {

    @Inject(method = "compile", at = @At("HEAD"))
    private void mgshaders_onCompileHead(
            SectionPos pos,
            RenderSectionRegion region,
            VertexSorting sorting,
            SectionBufferBuilderPack buffers,
            CallbackInfoReturnable<SectionCompiler.Results> cir
    ) {
        long key = pos.asLong();
        PipelineProfiler.ChunkPhases p = PipelineProfiler.get(key);
        long now = System.nanoTime();
        p.compileStartNs = now;
        if (p.lightReadyNs == 0) p.lightReadyNs = now;
    }

    @Inject(method = "compile", at = @At("RETURN"))
    private void mgshaders_onCompileReturn(
            SectionPos pos,
            RenderSectionRegion region,
            VertexSorting sorting,
            SectionBufferBuilderPack buffers,
            CallbackInfoReturnable<SectionCompiler.Results> cir
    ) {
        PipelineProfiler.get(pos.asLong()).compileEndNs = System.nanoTime();
    }
}
