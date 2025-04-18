package com.laviesss.xaeroradarfixer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class XaeroRadarFixerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "xaero_radar_fixer.json");

    public static boolean enabled = true;
    public static boolean showChatMessage = true;
    public static boolean showToast = true;

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                XaeroRadarFixerConfig loaded = GSON.fromJson(reader, XaeroRadarFixerConfig.class);
                enabled = loaded.enabled;
                showChatMessage = loaded.showChatMessage;
                showToast = loaded.showToast;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(new XaeroRadarFixerConfig(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
