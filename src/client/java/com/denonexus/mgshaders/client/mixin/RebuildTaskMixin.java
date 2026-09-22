package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.ChunkProfiler;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

/**
 * Mede o tempo total de uma tarefa de rebuild de chunk.
 *
 * Alvo: SectionRenderDispatcher.RenderSection.RebuildTask
 *   (classe aninhada, obf: hts$a$b)
 *
 * Metodo: doTask(SectionBufferBuilderPack) → CompletableFuture
 *   obf: a
 *
 * Roda em worker thread. Nao modifica nada — so mede.
 *
 * Nota: doTask retorna um CompletableFuture, mas o trabalho real
 * acontece ANTES de retornar (a future ja vem completa ou quase).
 * Medimos o tempo sincrono de doTask, que cobre meshing + staging.
 */
@Mixin(targets = "net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection$RebuildTask")
public class RebuildTaskMixin {

    private static final ThreadLocal<Long> T_START = new ThreadLocal<>();

    @Inject(method = "doTask", at = @At("HEAD"))
    private void mgshaders_onDoTaskHead(
            SectionBufferBuilderPack pack,
            CallbackInfoReturnable<CompletableFuture<?>> cir
    ) {
        T_START.set(System.nanoTime());
    }

    @Inject(method = "doTask", at = @At("RETURN"))
    private void mgshaders_onDoTaskReturn(
            SectionBufferBuilderPack pack,
            CallbackInfoReturnable<CompletableFuture<?>> cir
    ) {
        Long start = T_START.get();
        if (start == null) return;
        T_START.remove();

        long elapsed = System.nanoTime() - start;
        boolean success = cir.getReturnValue() != null;

        // total=elapsed, compile/queueWait desconhecidos aqui (-1)
        ChunkProfiler.record(-1L, -1L, elapsed, success);
    }
}
