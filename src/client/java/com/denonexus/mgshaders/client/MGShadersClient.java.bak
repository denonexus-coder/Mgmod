package com.denonexus.mgshaders.client;

import com.denonexus.mgshaders.ram.RegionCacheManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.network.ClientPlayerEntity;

public class MGShadersClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                ClientPlayerEntity player = client.player;
                double px = player.getX();
                double pz = player.getZ();
                double vx = player.getVelocity().x;
                double vz = player.getVelocity().z;
                RegionCacheManager.onTick(px, pz, vx, vz);
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            RegionCacheManager.onWorldUnload();
        });
    }
}
