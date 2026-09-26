package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.ServerBottleneckProfiler;
import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkCache.class)
public class ServerBottleneckChunkCacheMixin {

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void mgshaders_chunk_cache_start(
            java.util.function.BooleanSupplier haveTime,
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.beginChunkCache();
    }

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void mgshaders_chunk_cache_end(
            java.util.function.BooleanSupplier haveTime,
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.endChunkCache();
    }
}
