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

/**
 * Mede o tempo de meshing puro de um chunk.
 *
 * Assinatura extraida do mappings 1.21.11:
 *   SectionCompiler.Results compile(SectionPos, RenderSectionRegion,
 *                                    VertexSorting, SectionBufferBuilderPack)
 *   → obf: a
 *
 * Roda em worker thread. Nao modifica nada — so mede.
 */
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

        // queueWait=-1 (desconhecido aqui), compile=elapsed, total=-1
        ChunkProfiler.record(-1L, elapsed, -1L, success);
    }
}
