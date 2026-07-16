package com.laviesss.xaerodisabledradarfixer;

import com.laviesss.xaerodisabledradarfixer.config.XaeroDisabledRadarFixerConfig;
import com.laviesss.xaerodisabledradarfixer.input.XaeroDisabledRadarFixerKeybinds;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XaeroDisabledRadarFixerClientMod implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("XDRF");

    @Override
    public void onInitializeClient() {
        XaeroDisabledRadarFixerConfig.load();
        XaeroDisabledRadarFixerKeybinds.register();
        LOGGER.info("[XDRF] Loaded. Press the toggle keybind to disable / re-enable radar-block.");
    }
}