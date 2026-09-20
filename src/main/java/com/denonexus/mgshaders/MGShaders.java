package com.denonexus.mgshaders;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MGShaders implements ModInitializer {
    public static final String MOD_ID = "mgshaders";
    public static final String MOD_NAME = "MGShaders";
    public static final String MOD_VERSION = "0.1.0-alpha";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("[{}] onInitialize (common) — v{}", MOD_NAME, MOD_VERSION);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
