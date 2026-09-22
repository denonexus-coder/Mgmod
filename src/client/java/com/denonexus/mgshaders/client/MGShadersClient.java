package com.denonexus.mgshaders.client;

import com.denonexus.mgshaders.MGShaders;
import com.denonexus.mgshaders.client.profile.ChunkProfiler;
import com.denonexus.mgshaders.client.profile.PipelineProfiler;
import com.denonexus.mgshaders.client.profile.ServerTickProfiler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class MGShadersClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MGShaders.LOGGER.info("[{}] onInitializeClient", MGShaders.MOD_NAME);

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            MGShaders.LOGGER.info("[{}] flushing all profiles", MGShaders.MOD_NAME);
            ChunkProfiler.flush();
            ServerTickProfiler.flush();
            PipelineProfiler.flush();
        });

        MGShaders.LOGGER.info("[{}] Ready (phase 0.5)", MGShaders.MOD_NAME);
    }
}
