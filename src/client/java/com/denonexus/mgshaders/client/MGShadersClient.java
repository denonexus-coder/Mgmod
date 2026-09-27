package com.denonexus.mgshaders.client;

import com.denonexus.mgshaders.client.astc.AstcAtlasManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MGShadersClient
        implements ClientModInitializer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    "MGShaders-Client"
            );

    @Override
    public void onInitializeClient() {

        /*
         * The ASTC resources are bundled inside the mod JAR.
         *
         * Runtime never depends on:
         *
         * /storage/emulated/0/minecraft_astc/...
         *
         * The resources are materialized into the game's private
         * mg_astc_cache directory so FileChannel.map() can be used.
         */
        AstcAtlasManager.prepareBundledAssets();

        ClientTickEvents.END_CLIENT_TICK
                .register(client -> {
                });

        LOGGER.info(
                "MGShaders: direct bundled ASTC atlas backend enabled"
        );
    }
}
