package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.budget.FrameBudget;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Alvo: EntityRenderDispatcher.shouldRender(Entity, Frustum, double, double, double)
 *        mappings 1.21.11 linha 122
 * Descarta entidades além da distância adaptativa do FrameBudget.
 * Player local nunca é descartado.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityCullMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void mg$distanceCull(Entity entity, Frustum frustum,
                                 double camX, double camY, double camZ,
                                 CallbackInfoReturnable<Boolean> cir) {
        if (entity == null) return;
        if (entity instanceof LocalPlayer) return;

        double dx = entity.getX() - camX;
        double dy = entity.getY() - camY;
        double dz = entity.getZ() - camZ;
        double d2 = dx*dx + dy*dy + dz*dz;

        int cd = FrameBudget.entityCullDistance;
        if (d2 > (double)cd * cd) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
