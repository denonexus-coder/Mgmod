package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.ServerBottleneckProfiler;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class ServerBottleneckBlockEntityMixin {

    @Inject(
            method = "tickBlockEntities",
            at = @At("HEAD")
    )
    private void mgshaders_block_entities_start(
            CallbackInfo ci
    ) {
        if ((Object) this instanceof net.minecraft.server.level.ServerLevel) {
            ServerBottleneckProfiler.beginBlockEntities();
        }
    }

    @Inject(
            method = "tickBlockEntities",
            at = @At("RETURN")
    )
    private void mgshaders_block_entities_end(
            CallbackInfo ci
    ) {
        if ((Object) this instanceof net.minecraft.server.level.ServerLevel) {
            ServerBottleneckProfiler.endBlockEntities();
        }
    }
}
