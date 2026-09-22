package com.denonexus.mgshaders.client;

import com.denonexus.mgshaders.MGShaders;
import com.denonexus.mgshaders.client.profile.ChunkProfiler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class MGShadersClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MGShaders.LOGGER.info("[{}] onInitializeClient", MGShaders.MOD_NAME);
        MGShaders.LOGGER.info("[{}] Target: MC 1.21.11 / official Mojang mappings",
                              MGShaders.MOD_NAME);
        MGShaders.LOGGER.info("[{}] Profile output: /sdcard/MG/mgshaders_chunk_profile.json",
                              MGShaders.MOD_NAME);

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            MGShaders.LOGGER.info("[{}] flushing chunk profile on shutdown",
                                  MGShaders.MOD_NAME);
            ChunkProfiler.flush();
        });

        MGShaders.LOGGER.info("[{}] Ready (phase 0 — chunk profiler)",
                              MGShaders.MOD_NAME);
    }
}
