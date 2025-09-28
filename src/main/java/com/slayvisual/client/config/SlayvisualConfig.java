package com.slayvisual.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.slayvisual.Slayvisual;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SlayvisualConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(Slayvisual.MOD_ID + ".json");

    private static ConfigData data = new ConfigData();

    private SlayvisualConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
            if (loaded != null) {
                loaded.applyDefaults();
                data = loaded;
            }
        } catch (IOException | JsonSyntaxException exception) {
            Slayvisual.LOGGER.error("Failed to load SlayVisual config. Using defaults.", exception);
            data = new ConfigData();
        }
    }

    public static void save() {
        data.applyDefaults();

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
        } catch (IOException exception) {
            Slayvisual.LOGGER.error("Failed to create config directory", exception);
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(data, writer);
        } catch (IOException exception) {
            Slayvisual.LOGGER.error("Failed to save SlayVisual config", exception);
        }
    }

    public static boolean isTriggerBotEnabled() {
        return Boolean.TRUE.equals(data.triggerBotEnabled);
    }

    public static void setTriggerBotEnabled(boolean enabled) {
        data.triggerBotEnabled = enabled;
        save();
    }

    public static int getTriggerBotDelayTicks() {
        int value = data.triggerBotDelayTicks != null ? data.triggerBotDelayTicks : 0;
        return Math.max(0, value);
    }

    public static void setTriggerBotDelayTicks(int delay) {
        data.triggerBotDelayTicks = Math.max(0, delay);
        save();
    }

    public static boolean isHudEnabled() {
        return Boolean.TRUE.equals(data.showHud);
    }

    public static void setHudEnabled(boolean showHud) {
        data.showHud = showHud;
        save();
    }

    private static class ConfigData {
        private Boolean triggerBotEnabled = Boolean.TRUE;
        private Integer triggerBotDelayTicks = 5;
        private Boolean showHud = Boolean.TRUE;

        private void applyDefaults() {
            if (triggerBotEnabled == null) {
                triggerBotEnabled = Boolean.TRUE;
            }

            if (triggerBotDelayTicks == null) {
                triggerBotDelayTicks = 5;
            }

            if (showHud == null) {
                showHud = Boolean.TRUE;
            }
        }
    }
}
