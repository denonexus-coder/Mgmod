package com.denonexus.mgshaders.client;

import com.denonexus.mgshaders.MGShaders;
import net.fabricmc.api.ClientModInitializer;

public class MGShadersClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MGShaders.LOGGER.info("[{}] onInitializeClient", MGShaders.MOD_NAME);
        MGShaders.LOGGER.info("[{}] Target: MC 1.21.11 / official Mojang mappings",
                              MGShaders.MOD_NAME);
        MGShaders.LOGGER.info("[{}] Shader dir: /sdcard/MG/shaders/mgshaders/",
                              MGShaders.MOD_NAME);
        MGShaders.LOGGER.info("[{}] Ready (phase 1 — skeleton)",
                              MGShaders.MOD_NAME);
    }
}
