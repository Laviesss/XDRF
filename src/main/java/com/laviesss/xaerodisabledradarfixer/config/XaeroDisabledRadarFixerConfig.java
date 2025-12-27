package com.laviesss.xaerodisabledradarfixer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XaeroDisabledRadarFixerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/xaero_disabled_radar_fixer.json");
    private static XaeroDisabledRadarFixerConfig INSTANCE;

    private boolean enabled = true;
    private boolean showChatMessage = true;
    private boolean showToast = true;
    private boolean showBlockedMessage = false;
    private List<String> blockingPatterns = new ArrayList<>();

    public XaeroDisabledRadarFixerConfig() {
        blockingPatterns.add("§f§a§i§r§x§a§e§r§o");
        blockingPatterns.add("§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r");
        blockingPatterns.add("§n§o§m§i§n§i§m§a§p");
    }

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

    public List<String> getBlockingPatterns() {
        return blockingPatterns;
    }

    public void setBlockingPatterns(List<String> blockingPatterns) {
        this.blockingPatterns = blockingPatterns;
        save();
    }

    public boolean isShowBlockedMessage() {
        return showBlockedMessage;
    }

    public void setShowBlockedMessage(boolean showBlockedMessage) {
        this.showBlockedMessage = showBlockedMessage;
        save();
    }
}
