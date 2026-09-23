package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.RenderProfiler;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlShaderModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlDevice.class)
public class ShaderCompileMixin {

    private static final ThreadLocal<Long> T0 = new ThreadLocal<>();

    @Inject(method = "compileShader", at = @At("HEAD"))
    private void mg$head(Object key, Object source, CallbackInfoReturnable<GlShaderModule> cir) {
        T0.set(System.nanoTime());
    }

    @Inject(method = "compileShader", at = @At("RETURN"))
    private void mg$tail(Object key, Object source, CallbackInfoReturnable<GlShaderModule> cir) {
        Long t = T0.get();
        if (t == null) return;
        T0.remove();
        RenderProfiler.shaderCompile(System.nanoTime() - t);
    }
}
