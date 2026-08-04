package com.laviesss.xaerodisabledradarfixer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    private boolean enabled = true;

    private boolean showChatMessage = true;

    private boolean showToast = true;

    private boolean verboseLogging = false;

    private BlockingScope blockingScope = BlockingScope.BOTH;

    private boolean playSound = true;

    private int packetBlockedCount = 0;

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

    public boolean isVerboseLogging() {
        return verboseLogging;
    }

    public void setVerboseLogging(boolean verboseLogging) {
        this.verboseLogging = verboseLogging;
        save();
    }

    public BlockingScope getBlockingScope() {
        return blockingScope;
    }

    public void setBlockingScope(BlockingScope blockingScope) {
        this.blockingScope = blockingScope;
        save();
    }

    public boolean shouldBlockPackets() {
        return blockingScope == BlockingScope.PACKET || blockingScope == BlockingScope.BOTH;
    }

    public boolean shouldBlockMessages() {
        return blockingScope == BlockingScope.CHAT_MESSAGE || blockingScope == BlockingScope.BOTH;
    }

    public int getPacketBlockedCount() {
        return packetBlockedCount;
    }

    public void incrementPacketBlockedCount() {
        this.packetBlockedCount++;
        save();
    }

    public boolean isPlaySound() {
        return playSound;
    }

    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
        save();
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final File CONFIG_FILE = new File("config/xaero_disabled_radar_fixer.json");

    private static Config INSTANCE;

    public static Config get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, Config.class);
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                INSTANCE = new Config();
            }
        } else {
            INSTANCE = new Config();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public enum BlockingScope {
        CHAT_MESSAGE,
        PACKET,
        BOTH
    }
}