package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.PipelineProfiler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.LevelChunk;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Instrumenta 3 fases do pipeline de chunk:
 *  1. Chunk carregado (ClientLevel.onChunkLoaded)
 *  2. Light engine pronto (aproximacao: quando compile comeca)
 *  3. Compile executado (SectionCompiler.compile)
 *  4. Upload para GPU (uploadAllPendingUploads)
 */
public class PipelineMixin {

    /** Fase 1 — chunk carregado do disco/rede. */
    @Mixin(ClientLevel.class)
    public static class Loaded {
        @Inject(method = "onChunkLoaded", at = @At("RETURN"))
        private void mgshaders_onChunkLoaded(net.minecraft.world.level.ChunkPos pos,
                                              CallbackInfo ci) {
            long key = pos.toLong();
            PipelineProfiler.get(key).loadedNs = System.nanoTime();
        }
    }

    /** Fase 3+4 — compile e upload. */
    @Mixin(SectionCompiler.class)
    public static class Compile {
        @Inject(method = "compile", at = @At("HEAD"))
        private void onHead(SectionPos pos, RenderSectionRegion region,
                            VertexSorting sorting, SectionBufferBuilderPack buffers,
                            CallbackInfoReturnable<SectionCompiler.Results> cir) {
            long key = pos.asLong();
            PipelineProfiler.ChunkPhases p = PipelineProfiler.get(key);
            long now = System.nanoTime();
            p.compileStartNs = now;
            if (p.lightReadyNs == 0) p.lightReadyNs = now; // aprox
        }

        @Inject(method = "compile", at = @At("RETURN"))
        private void onReturn(SectionPos pos, RenderSectionRegion region,
                              VertexSorting sorting, SectionBufferBuilderPack buffers,
                              CallbackInfoReturnable<SectionCompiler.Results> cir) {
            long key = pos.asLong();
            PipelineProfiler.get(key).compileEndNs = System.nanoTime();
        }
    }

    @Mixin(SectionRenderDispatcher.class)
    public static class Upload {
        @Inject(method = "uploadAllPendingUploads", at = @At("RETURN"))
        private void onUploadReturn(CallbackInfo ci) {
            // Nao temos SectionPos aqui — apenas marca o tempo de "algo foi uploaded"
            // Isso eh um placeholder — o verdadeiro upload por chunk precisa de hook diferente
        }
    }
}
