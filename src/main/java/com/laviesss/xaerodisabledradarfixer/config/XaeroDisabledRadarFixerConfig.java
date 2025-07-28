package com.laviesss.xaerodisabledradarfixer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class XaeroDisabledRadarFixerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/xaero_disabled_radar_fixer.json");
    private static XaeroDisabledRadarFixerConfig INSTANCE;

    private boolean enabled = true;
    private boolean showChatMessage = true;
    private boolean showToast = true;

    public static XaeroDisabledRadarFixerConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, XaeroDisabledRadarFixerConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
                INSTANCE = new XaeroDisabledRadarFixerConfig();
            }
        } else {
            INSTANCE = new XaeroDisabledRadarFixerConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public boolean isShowChatMessage() {
        return showChatMessage;
    }

    public void setShowChatMessage(boolean showChatMessage) {
        this.showChatMessage = showChatMessage;
        save();
    }

    public boolean isShowToast() {
        return showToast;
    }

    public void setShowToast(boolean showToast) {
        this.showToast = showToast;
        save();
    }
}
