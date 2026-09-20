package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.MGShaders;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hook em LevelRenderer.renderLevel.
 *
 * Assinatura extraída do mappings oficial 1.21.11:
 *   void renderLevel(GraphicsResourceAllocator, DeltaTracker, boolean, Camera,
 *                    Matrix4f, Matrix4f, Matrix4f, GpuBufferSlice, Vector4f, boolean)
 *   → obfuscado como "a" (mas officialMojangMappings expõe o nome real).
 *
 * Fase 1: log único para provar que o pipeline de mixin funciona.
 * Fases futuras: preparar uniforms (sun, camera, time) antes do render.
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void mgshaders_onRenderLevelHead(
            GraphicsResourceAllocator allocator,
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            Matrix4f positionMatrix,
            GpuBufferSlice fogBuffer,
            Vector4f fogColor,
            boolean shouldRenderSky,
            CallbackInfo ci
    ) {
        MGShaders.LOGGER.info("[{}] LevelRenderer.renderLevel hook active",
                              MGShaders.MOD_NAME);
    }
}
