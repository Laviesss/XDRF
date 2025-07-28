package com.laviesss.xaerodisabledradarfixer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class XaeroDisabledRadarFixerConfig {
    public static boolean enabled = true;
    public static boolean showChatMessage = true;
    public static boolean showToast = true;

    private static final File CONFIG_FILE = new File("config/xaero_disabled_radar_fixer.json");

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(new XaeroDisabledRadarFixerConfig(), writer);
        } catch (IOException e) {
            System.err.println("Failed to save Xaero Disabled Radar Fixer config:");
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save(); // Create config with default values
            return;
        }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            XaeroDisabledRadarFixerConfig loaded = new Gson().fromJson(reader, XaeroDisabledRadarFixerConfig.class);
            enabled = loaded.enabled;
            showChatMessage = loaded.showChatMessage;
            showToast = loaded.showToast;
        } catch (IOException e) {
            System.err.println("Failed to load Xaero Disabled Radar Fixer config:");
            e.printStackTrace();
        }
    }

    public static File getConfigFile() {
        return CONFIG_FILE;
    }

    // Optional utility methods for setting + auto-saving
    public static void setEnabled(boolean val) {
        enabled = val;
        save();
    }

    public static void setShowChatMessage(boolean val) {
        showChatMessage = val;
        save();
    }

    public static void setShowToast(boolean val) {
        showToast = val;
        save();
    }
}
