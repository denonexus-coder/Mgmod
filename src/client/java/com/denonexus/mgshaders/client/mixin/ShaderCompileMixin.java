package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.RenderProfiler;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlShaderModule;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Alvo: GlDevice.getOrCompileShader(Identifier, ShaderType, ShaderDefines, ShaderSource)
 *        mappings oficial 1.21.11 linha 373:
 *        373:374:GlShaderModule getOrCompileShader(Identifier,ShaderType,ShaderDefines,ShaderSource) -> a
 *
 * Por que não hook em compileShader (linha 384):
 *   esse método recebe GlDevice$ShaderCompilationKey como parâmetro, que é
 *   private — impossível de referenciar como tipo em Java. getOrCompileShader
 *   é público em todos os tipos, chamado a cada lookup (cache hit ou compile).
 *   Filtramos os cache hits exigindo elapsed >= 1 ms.
 */
@Mixin(GlDevice.class)
public class ShaderCompileMixin {

    private static final ThreadLocal<Long> T0 = new ThreadLocal<>();
    private static final long COMPILE_THRESHOLD_NS = 1_000_000L;  // 1 ms

    @Inject(method = "getOrCompileShader", at = @At("HEAD"))
    private void mg$head(Identifier id, ShaderType type, ShaderDefines defines, ShaderSource source,
                         CallbackInfoReturnable<GlShaderModule> cir) {
        T0.set(System.nanoTime());
    }

    @Inject(method = "getOrCompileShader", at = @At("RETURN"))
    private void mg$tail(Identifier id, ShaderType type, ShaderDefines defines, ShaderSource source,
                         CallbackInfoReturnable<GlShaderModule> cir) {
        Long t = T0.get();
        if (t == null) return;
        T0.remove();
        long elapsed = System.nanoTime() - t;
        if (elapsed >= COMPILE_THRESHOLD_NS) {
            RenderProfiler.shaderCompile(elapsed);
        }
    }
}
