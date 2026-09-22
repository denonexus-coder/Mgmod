package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.ChunkProfiler;
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
public class SectionCompilerMixin {

    private static final ThreadLocal<Long> T_START = new ThreadLocal<>();

    @Inject(method = "compile", at = @At("HEAD"))
    private void mgshaders_onCompileHead(
            SectionPos pos,
            RenderSectionRegion region,
            VertexSorting sorting,
            SectionBufferBuilderPack buffers,
            CallbackInfoReturnable<SectionCompiler.Results> cir
    ) {
        ChunkProfiler.recordThread();
        T_START.set(System.nanoTime());
    }

    @Inject(method = "compile", at = @At("RETURN"))
    private void mgshaders_onCompileReturn(
            SectionPos pos,
            RenderSectionRegion region,
            VertexSorting sorting,
            SectionBufferBuilderPack buffers,
            CallbackInfoReturnable<SectionCompiler.Results> cir
    ) {
        Long start = T_START.get();
        if (start == null) return;
        T_START.remove();

        long elapsed = System.nanoTime() - start;
        boolean success = cir.getReturnValue() != null;

        ChunkProfiler.record(-1L, elapsed, -1L, success);

        // Loga posição + tempo se for outlier (>500ms)
        String posStr = String.format("chunk(%d,%d,%d)",
            pos.x(), pos.y(), pos.z());
        ChunkProfiler.recordOutlier(posStr, elapsed);
    }
}
