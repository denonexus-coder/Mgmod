package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.ServerBottleneckProfiler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class ServerBottleneckServerMixin {

    @Inject(
            method = "tickServer",
            at = @At("HEAD")
    )
    private void mgshaders_profiler_start(
            java.util.function.BooleanSupplier haveTime,
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.beginServerTick();
    }

    @Inject(
            method = "tickServer",
            at = @At("RETURN")
    )
    private void mgshaders_profiler_end(
            java.util.function.BooleanSupplier haveTime,
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.endServerTick();
    }

    @Inject(
            method = "tickChildren",
            at = @At("HEAD")
    )
    private void mgshaders_children_start(
            java.util.function.BooleanSupplier haveTime,
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.beginChildren();
    }

    @Inject(
            method = "tickChildren",
            at = @At("RETURN")
    )
    private void mgshaders_children_end(
            java.util.function.BooleanSupplier haveTime,
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.endChildren();
    }

    @Inject(
            method = "tickConnection",
            at = @At("HEAD")
    )
    private void mgshaders_connection_start(
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.beginConnection();
    }

    @Inject(
            method = "tickConnection",
            at = @At("RETURN")
    )
    private void mgshaders_connection_end(
            CallbackInfo ci
    ) {
        ServerBottleneckProfiler.endConnection();
    }
}
