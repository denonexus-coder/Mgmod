package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.render.MGChunkBatch;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Delimita o ciclo de frame do MGChunkBatch dentro de GameRenderer.render().
 *
 * Mojang 1.21.11 mapping (hob):
 *   void render(DeltaTracker deltaTracker, boolean renderLevel)
 *
 * beginFrame() em HEAD:
 *   - Incrementa o frame counter
 *   - Limpa a lista de seções visíveis do frame anterior
 *   - Inicializa o pendingSections para o frame corrente
 *
 * finishFrame() em TAIL:
 *   - Ponto de extensão para métricas por frame (timing, draw count)
 *   - Não interfere com o pipeline de render
 *
 * Este Mixin é diferente de GameRendererTBDRLightMixin (que gerencia
 * o post-effect de iluminação TBDR). Ambos podem coexistir porque
 * fazem injeções não conflitantes no mesmo método.
 */
@Mixin(GameRenderer.class)
public abstract class MGGameRendererMixin {

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void mg$beginFrame(
            DeltaTracker deltaTracker,
            boolean renderLevel,
            CallbackInfo ci
    ) {
        MGChunkBatch.beginFrame();
    }

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void mg$finishFrame(
            DeltaTracker deltaTracker,
            boolean renderLevel,
            CallbackInfo ci
    ) {
        MGChunkBatch.finishFrame();
    }
}
