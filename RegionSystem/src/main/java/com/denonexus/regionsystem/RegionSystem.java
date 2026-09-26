package com.denonexus.regionsystem;

import com.denonexus.regionsystem.server.RegionServerManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RegionSystem implements ModInitializer {

    public static final String MOD_ID = "regionsystem";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    private static RegionServerManager manager;

    @Override
    public void onInitialize() {

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            manager = new RegionServerManager(server);
            manager.start();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (manager != null) {
                manager.stop();
                manager = null;
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (manager != null) {
                manager.tick();
            }
        });

        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> {
                    if (manager != null) {
                        manager.onPlayerJoin(handler.getPlayer());
                    }
                }
        );

        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> {
                    if (manager != null) {
                        manager.onPlayerLeave(handler.getPlayer());
                    }
                }
        );

        LOGGER.info(
                "Region System {} initialized",
                "0.1.0"
        );
    }

    public static RegionServerManager manager() {
        return manager;
    }
}
