package com.denonexus.mgshaders.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.DeltaTracker;

@Mixin(GameRenderer.class)
public abstract class GameRendererTBDRLightMixin {

    private static final Identifier MG_TBDR_LIGHT =
            Identifier.fromNamespaceAndPath(
                    "mgshaders",
                    "tbdr_light"
            );

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract Identifier currentPostEffect();

    @Shadow
    private void setPostEffect(Identifier id) {
        throw new AssertionError();
    }

    @Shadow
    public abstract void clearPostEffect();

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void mgshaders_enableTBDRLight(
            DeltaTracker deltaTracker,
            boolean renderLevel,
            CallbackInfo ci
    ) {
        if (renderLevel && this.minecraft.level != null) {
            if (!MG_TBDR_LIGHT.equals(this.currentPostEffect())) {
                this.setPostEffect(MG_TBDR_LIGHT);
            }
        } else if (MG_TBDR_LIGHT.equals(this.currentPostEffect())) {
            this.clearPostEffect();
        }
    }
}
