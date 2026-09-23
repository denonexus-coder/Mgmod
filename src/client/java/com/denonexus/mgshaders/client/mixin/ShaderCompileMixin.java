package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.RenderProfiler;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlShaderModule;
import com.mojang.blaze3d.shaders.ShaderSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Alvo: GlDevice.compileShader(GlDevice$ShaderCompilationKey, ShaderSource)
 *        mappings oficial 1.21.11 linha 384:
 *        384:402:GlShaderModule compileShader(GlDevice$ShaderCompilationKey,ShaderSource) -> a
 *
 * Mixin exige tipos EXATOS — Object não casa com o descriptor.
 */
@Mixin(GlDevice.class)
public class ShaderCompileMixin {

    private static final ThreadLocal<Long> T0 = new ThreadLocal<>();

    @Inject(method = "compileShader", at = @At("HEAD"))
    private void mg$head(GlDevice.ShaderCompilationKey key, ShaderSource source,
                         CallbackInfoReturnable<GlShaderModule> cir) {
        T0.set(System.nanoTime());
    }

    @Inject(method = "compileShader", at = @At("RETURN"))
    private void mg$tail(GlDevice.ShaderCompilationKey key, ShaderSource source,
                         CallbackInfoReturnable<GlShaderModule> cir) {
        Long t = T0.get();
        if (t == null) return;
        T0.remove();
        RenderProfiler.shaderCompile(System.nanoTime() - t);
    }
}
