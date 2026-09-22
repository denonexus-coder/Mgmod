package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.PipelineProfiler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ChunkLoadMixin {

    @Inject(method = "onChunkLoaded", at = @At("RETURN"))
    private void mgshaders_onChunkLoaded(ChunkPos pos, CallbackInfo ci) {
        PipelineProfiler.get(pos.toLong()).loadedNs = System.nanoTime();
    }
}
