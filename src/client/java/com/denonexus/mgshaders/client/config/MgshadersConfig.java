package com.denonexus.mgshaders.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class MgshadersConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("MGShaders-Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("mgshaders_config.json");

    public boolean astc_enabled = true;
    public boolean shaders_enabled = true;
    public boolean logging_enabled = true;

    private static MgshadersConfig instance = new MgshadersConfig();

    public static MgshadersConfig getInstance() {
        return instance;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                instance = GSON.fromJson(reader, MgshadersConfig.class);
                if (instance == null) {
                    instance = new MgshadersConfig();
                }
                if (instance.logging_enabled) {
                    LOGGER.info("Configuracoes carregadas do mgshaders_config.json");
                }
            } catch (Exception e) {
                LOGGER.error("Falha ao carregar configuracoes, usando padroes", e);
                instance = new MgshadersConfig();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(instance, writer);
                if (instance.logging_enabled) {
                    LOGGER.info("Configuracoes salvas em mgshaders_config.json");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Falha ao salvar mgshaders_config.json", e);
        }
    }

    public static void log(String message) {
        if (instance.logging_enabled) {
            LOGGER.info(message);
        }
    }
}
