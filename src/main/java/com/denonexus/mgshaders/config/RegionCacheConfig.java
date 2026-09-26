package com.denonexus.mgshaders.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;

public class RegionCacheConfig {
    private static final Path CONFIG_PATH = Paths.get("config", "mgshaders_regioncache.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean regionCacheEnabled = true;
    public int lookaheadTicks = 40;
    public int cooldownTicks = 100;
    public boolean logComparison = true;
    public int maxRamMb = 256;

    private static RegionCacheConfig INSTANCE;

    public static RegionCacheConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                    INSTANCE = GSON.fromJson(reader, RegionCacheConfig.class);
                }
            } else {
                INSTANCE = new RegionCacheConfig();
                save();
            }
        } catch (Exception e) {
            INSTANCE = new RegionCacheConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception ignored) {}
    }
}
