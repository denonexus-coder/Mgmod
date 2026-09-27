package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.render.MGChunkBatch;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Captura o resultado de prepareChunkRenders para o MGChunkBatch.
 *
 * prepareChunkRenders() é o ponto central onde o vanilla:
 *   1. percorre visibleSections
 *   2. agrupa por layer/pipeline
 *   3. produz ChunkSectionsToRender com drawsPerLayer
 *
 * Capturamos esse objeto APÓS o vanilla fazer todo o agrupamento,
 * sem duplicar ou substituir o processo de culling.
 *
 * Mojang 1.21.11 mapping:
 *   ChunkSectionsToRender prepareChunkRenders(Matrix4fc, double, double, double)
 *   lines 1075:1129 in LevelRenderer (hoh)
 */
@Mixin(LevelRenderer.class)
public abstract class MGPrepareChunkRendersMixin {

    @Inject(
            method = "prepareChunkRenders",
            at = @At("RETURN")
    )
    private void mg$prepareChunkRenders(
            Matrix4fc matrix,
            double cameraX,
            double cameraY,
            double cameraZ,
            CallbackInfoReturnable<ChunkSectionsToRender> cir
    ) {
        ChunkSectionsToRender result = cir.getReturnValue();
        if (result != null) {
            MGChunkBatch.capture(result, cameraX, cameraY, cameraZ);
        }
    }
}
