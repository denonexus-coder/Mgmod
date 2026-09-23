package com.denonexus.mgshaders.client;

import com.denonexus.mgshaders.MGShaders;
import com.denonexus.mgshaders.client.bench.GpuBench;
import com.denonexus.mgshaders.client.hud.MgHudOverlay;
import com.denonexus.mgshaders.client.profile.ChunkProfiler;
import com.denonexus.mgshaders.client.profile.PipelineProfiler;
import com.denonexus.mgshaders.client.profile.ServerTickProfiler;
import com.denonexus.mgshaders.nativebridge.NativeChunkLoader;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class MGShadersClient implements ClientModInitializer {

    private static KeyMapping toggleHudKey;

    @Override
    public void onInitializeClient() {
        MGShaders.LOGGER.info("[{}] onInitializeClient", MGShaders.MOD_NAME);

        NativeChunkLoader.init();
        MGShaders.LOGGER.info("[{}] NativeChunkLoader available={}",
                MGShaders.MOD_NAME, NativeChunkLoader.isAvailable());

        MgHudOverlay.register();
        GpuBench.register();

        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.mgshaders.toggle_hud",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                "category.mgshaders"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleHudKey.consumeClick()) {
                MgHudOverlay.toggle();
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            MGShaders.LOGGER.info("[{}] flushing all profiles", MGShaders.MOD_NAME);
            ChunkProfiler.flush();
            ServerTickProfiler.flush();
            PipelineProfiler.flush();
        });

        MGShaders.LOGGER.info("[{}] Ready (phase 0.5 + HUD)", MGShaders.MOD_NAME);
    }
}
