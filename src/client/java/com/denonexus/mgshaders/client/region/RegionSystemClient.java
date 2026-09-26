package com.denonexus.mgshaders.client.region;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class RegionSystemClient
        implements ClientModInitializer {

    private long ticks;

    @Override
    public void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {

                    ticks++;

                    /*
                     * Do not replace vanilla rendering.
                     *
                     * Chunks arriving from the server enter
                     * ClientLevel and continue through Minecraft's
                     * normal section extraction/rebuild/upload path.
                     *
                     * This is the mesh stage of the Region pipeline.
                     */
                }
        );
    }
}
