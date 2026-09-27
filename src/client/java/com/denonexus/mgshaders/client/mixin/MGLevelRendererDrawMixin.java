package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.render.MGChunkBatch;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepta os pontos de invalidação de LevelRenderer para manter
 * o MGChunkBatch sincronizado com o estado vanilla do renderizador.
 *
 * Targets (Mojang 1.21.11):
 *   endFrame()              → hoh.b()
 *   clearVisibleSections()  → hoh.c()
 *   allChanged()            → hoh.o()
 */
@Mixin(LevelRenderer.class)
public abstract class MGLevelRendererDrawMixin {

    /**
     * Ao final de cada frame, limpa a lista de seções visíveis do batch.
     * O endFrame vanilla encerra a preparação do frame de render.
     */
    @Inject(method = "endFrame", at = @At("HEAD"))
    private void mg$endFrame(CallbackInfo ci) {
        MGChunkBatch.endFrame();
    }

    /**
     * Ao limpar as seções visíveis (ex: no início do próximo frame de culling),
     * invalida o batch para evitar draws com seções obsoletas.
     */
    @Inject(method = "clearVisibleSections", at = @At("HEAD"))
    private void mg$clearVisibleSections(CallbackInfo ci) {
        MGChunkBatch.invalidateAll();
    }

    /**
     * Ao forçar re-renderização completa (ex: mudança de configuração gráfica,
     * reload de recursos), descarta todo o estado acumulado do batch.
     */
    @Inject(method = "allChanged", at = @At("HEAD"))
    private void mg$allChanged(CallbackInfo ci) {
        MGChunkBatch.invalidateAll();
    }
}
