package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.render.MGChunkBatch;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mantém o MGChunkBatch sincronizado com o lifecycle de chunks/mundo.
 *
 * Mojang 1.21.11 mapping (ClientLevel):
 *   unload(LevelChunk)      → remove seções do chunk descarregado do batch
 *   disconnect(Component)   → limpa todo o batch ao sair do mundo
 *
 * Por que capturar unload(LevelChunk) e não onChunkLoaded:
 *   - onChunkLoaded só sinaliza disponibilidade de dados; o mesh ainda não existe
 *   - unload() é o ponto definitivo onde o chunk sai do cliente
 *   - após unload, qualquer GpuBuffer referenciado pelo batch para esse chunk
 *     pode ser liberado pela GPU, causando crash se usado em multi-draw
 *
 * Por isso removemos do UPLOADED_MAP imediatamente, antes que o vanilla
 * libere os GpuBuffers internamente.
 */
@Mixin(ClientLevel.class)
public abstract class MGClientLevelMixin {

    @Inject(
            method = "unload",
            at = @At("HEAD")
    )
    private void mg$onChunkUnload(LevelChunk chunk, CallbackInfo ci) {
        if (chunk == null) return;
        MGChunkBatch.onChunkUnload(chunk.getPos());
    }

    @Inject(
            method = "disconnect",
            at = @At("HEAD")
    )
    private void mg$onDisconnect(Component reason, CallbackInfo ci) {
        MGChunkBatch.invalidateAll();
    }
}
