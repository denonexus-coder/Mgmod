package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.ChunkProfiler;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mede o tempo gasto por frame fazendo upload de chunks para a GPU.
 *
 * Alvo: SectionRenderDispatcher.uploadAllPendingUploads()
 *   obf: a
 *
 * Roda na main thread. Mede apenas se rodou — se a fila estava vazia,
 * o tempo vai ser ~0 e nao vale registrar.
 *
 * Nao modifica nada — so mede.
 */
@Mixin(SectionRenderDispatcher.class)
public class UploadMixin {

    private static final long MIN_MEASURABLE_NS = 100_000L; // 0.1ms

    @Inject(method = "uploadAllPendingUploads", at = @At("HEAD"))
    private void mgshaders_onUploadHead(CallbackInfo ci) {
        // Guarda o timestamp no ThreadLocal global do ChunkProfiler
        ChunkProfiler.markUploadStart();
    }

    @Inject(method = "uploadAllPendingUploads", at = @At("RETURN"))
    private void mgshaders_onUploadReturn(CallbackInfo ci) {
        long elapsed = ChunkProfiler.markUploadEnd();
        if (elapsed >= MIN_MEASURABLE_NS) {
            // Registra upload como um "chunk extra" para nao poluir as stats
            // de meshing. Vai aparecer em total_ms se quisermos.
            // Por ora, apenas loga no proximo dump via contador interno.
            ChunkProfiler.recordUpload(elapsed);
        }
    }
}
