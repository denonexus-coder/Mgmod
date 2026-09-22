package com.denonexus.mgshaders.client.mixin;

import com.denonexus.mgshaders.client.profile.ServerTickProfiler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class ServerTickMixin {

    private static final ThreadLocal<Long> T_START = new ThreadLocal<>();

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void mgshaders_onTickHead(CallbackInfo ci) {
        T_START.set(System.nanoTime());
    }

    @Inject(method = "tickServer", at = @At("RETURN"))
    private void mgshaders_onTickReturn(CallbackInfo ci) {
        Long start = T_START.get();
        if (start == null) return;
        T_START.remove();
        ServerTickProfiler.record(System.nanoTime() - start);
    }
}
