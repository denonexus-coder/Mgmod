package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.render.MGChunkBatch;
import net.minecraft.client.renderer.GpuSampler;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepta os pontos de entrada e saída de ChunkSectionsToRender.renderGroup().
 *
 * Mojang 1.21.11 mapping (htj):
 *   void renderGroup(ChunkSectionLayerGroup group, GpuSampler sampler)
 *
 * renderGroup() é chamado para cada grupo de layers (OPAQUE, CUTOUT, TRANSLUCENT).
 * É dentro deste método que o vanilla chama RenderPass.drawMultipleIndexed().
 *
 * beforeRenderGroup: ponto para submeter o batch nativo ANTES do vanilla
 *   emitir seus próprios draws, se a arquitetura exigir substituição.
 *
 * afterRenderGroup: ponto para cleanup ou métricas após o draw vanilla.
 *
 * Nesta fase, os hooks notificam o MGChunkBatch sem substituir o draw vanilla,
 * permitindo observação do pipeline sem quebrar a renderização.
 */
@Mixin(ChunkSectionsToRender.class)
public abstract class MGChunkSectionsToRenderMixin {

    @Inject(
            method = "renderGroup",
            at = @At("HEAD")
    )
    private void mg$beforeRenderGroup(
            ChunkSectionLayerGroup group,
            GpuSampler sampler,
            CallbackInfo ci
    ) {
        MGChunkBatch.beforeRenderGroup(group);
    }

    @Inject(
            method = "renderGroup",
            at = @At("TAIL")
    )
    private void mg$afterRenderGroup(
            ChunkSectionLayerGroup group,
            GpuSampler sampler,
            CallbackInfo ci
    ) {
        MGChunkBatch.afterRenderGroup(group);
    }
}
