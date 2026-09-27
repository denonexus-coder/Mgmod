package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.ServerBottleneckProfiler;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DistanceManager.class)
public class ServerBottleneckDistanceMixin {

    @Inject(
            method = "runAllUpdates",
            at = @At("HEAD")
    )
    private void mgshaders_distance_start(
            ChunkMap chunkMap,
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.beginDistanceManager();
    }

    @Inject(
            method = "runAllUpdates",
            at = @At("RETURN")
    )
    private void mgshaders_distance_end(
            ChunkMap chunkMap,
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.endDistanceManager();
    }
}
