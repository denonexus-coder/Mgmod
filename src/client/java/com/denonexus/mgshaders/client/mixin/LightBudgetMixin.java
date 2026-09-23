package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.budget.FrameBudget;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Alvo: ClientLevel.pollLightUpdates() — mappings 1.21.11 linha 263
 * Limita quantas vezes por segundo a fila de luz é drenada, conforme FPS.
 * Se cancelado, a fila continua acumulando e é drenada no próximo slot.
 */
@Mixin(ClientLevel.class)
public class LightBudgetMixin {

    @Inject(method = "pollLightUpdates", at = @At("HEAD"), cancellable = true)
    private void mg$budgetLight(CallbackInfo ci) {
        if (!FrameBudget.canDrainLight()) {
            ci.cancel();
        }
    }
}
