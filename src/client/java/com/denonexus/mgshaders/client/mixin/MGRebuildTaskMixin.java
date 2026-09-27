package com.denonexus.mgshaders.client.mixin;

import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

/**
 * Expõe RebuildTask.doTask() e isRecompile() como Invokers.
 *
 * Mojang 1.21.11 mapping (hts$a$b):
 *   CompletableFuture<?> doTask(SectionBufferBuilderPack)  → lines 406:434
 *   boolean isRecompile()                                   → inherited / field access
 *
 * doTask() executa o pipeline completo de meshing de uma seção:
 *   SectionCompiler.compile() → SectionMesh → upload de GpuBuffers
 *
 * Invoker permite que o pipeline MG acione tarefas de rebuild síncronas
 * fora do scheduler padrão quando necessário (ex: upload prioritário de
 * seções visíveis antes do primeiro frame de um chunk recém-carregado).
 */
@Mixin(targets = "net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection$RebuildTask")
public interface MGRebuildTaskMixin {

    @Invoker("doTask")
    CompletableFuture<?> mg$doTask(SectionBufferBuilderPack buffers);
}
