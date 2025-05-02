package com.laviesss.xaerodisabledradarfixer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class XaeroRadarFixerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "xaero_radar_fixer.txt"      // ← now a .txt in the config folder
    );

    // these are your "live" flags that the rest of your mod reads
    public static boolean enabled         = true;
    public static boolean showChatMessage = true;
    public static boolean showToast       = true;

    // instance copies for GSON
    private boolean enabledField         = enabled;
    private boolean showChatMessageField = showChatMessage;
    private boolean showToastField       = showToast;

    /** Call this in your ClientModInitializer. */
    public static void load() {
        try {
            // If the file is missing or empty, write defaults and return
            if (!CONFIG_FILE.exists() || CONFIG_FILE.length() == 0) {
                save();
                return;
            }
            // Otherwise read the user’s file
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                XaeroRadarFixerConfig loaded = GSON.fromJson(reader, XaeroRadarFixerConfig.class);
                if (loaded != null) {
                    enabled         = loaded.enabledField;
                    showChatMessage = loaded.showChatMessageField;
                    showToast       = loaded.showToastField;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Call this in your YACL .save() callback. */
    public static void save() {
        try {
            // ensure config folder exists
            File parent = CONFIG_FILE.getParentFile();
            if (!parent.exists()) parent.mkdirs();

            // build an instance with current static values, then write it
            XaeroRadarFixerConfig toWrite = new XaeroRadarFixerConfig();
            toWrite.enabledField         = enabled;
            toWrite.showChatMessageField = showChatMessage;
            toWrite.showToastField       = showToast;

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(toWrite, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
