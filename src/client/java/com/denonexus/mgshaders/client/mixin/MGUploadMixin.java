package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.render.MGChunkBatch;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Captura o resultado de RenderSection.upload() para registrar a seção
 * no MGChunkBatch imediatamente após o upload dos GpuBuffers.
 *
 * Mojang 1.21.11 mapping:
 *   CompletableFuture<?> upload(Map<?,?>, CompiledSectionMesh)
 *   → hts$a (RenderSection) method upload
 *
 * Por que este é o ponto correto:
 *   - upload() é chamado na main thread após doTask() completar
 *   - neste ponto os GpuBuffers em CompiledSectionMesh já estão prontos
 *   - o mesh já está associado ao RenderSection via setSectionMesh()
 *   - não há race condition: upload roda serialmente na render thread
 *
 * O Inject em RETURN garante que os GpuBuffers foram escritos antes do
 * registro no MGChunkBatch, eliminando o risco de usar buffers vazios.
 */
@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class MGUploadMixin {

    @Inject(
            method = "upload",
            at = @At("RETURN")
    )
    private void mg$onUpload(
            Map<?, ?> map,
            CompiledSectionMesh mesh,
            CallbackInfoReturnable<CompletableFuture<?>> cir
    ) {
        if (mesh == null) return;

        SectionRenderDispatcher.RenderSection self =
                (SectionRenderDispatcher.RenderSection) (Object) this;

        MGChunkBatch.onSectionUploaded(self, mesh);
    }
}
