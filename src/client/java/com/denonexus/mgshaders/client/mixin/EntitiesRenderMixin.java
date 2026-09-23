package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.RenderProfiler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class EntitiesRenderMixin {

    @Inject(method = "submitEntities", at = @At("HEAD"))
    private void mg$head(PoseStack pose, LevelRenderState state,
                         SubmitNodeCollector collector, CallbackInfo ci) {
        RenderProfiler.begin();
    }

    @Inject(method = "submitEntities", at = @At("RETURN"))
    private void mg$tail(PoseStack pose, LevelRenderState state,
                         SubmitNodeCollector collector, CallbackInfo ci) {
        RenderProfiler.end(RenderProfiler.ENTITIES);
    }
}
