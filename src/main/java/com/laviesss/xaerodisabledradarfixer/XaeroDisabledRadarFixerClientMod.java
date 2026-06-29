package com.laviesss.xaerodisabledradarfixer;

import com.laviesss.xaerodisabledradarfixer.config.XaeroDisabledRadarFixerConfig;
import com.laviesss.xaerodisabledradarfixer.input.XaeroDisabledRadarFixerKeybinds;
import net.fabricmc.api.ClientModInitializer;

public class XaeroDisabledRadarFixerClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Load config on mod start
        XaeroDisabledRadarFixerConfig.load();
        // Register keybindings
        XaeroDisabledRadarFixerKeybinds.register();
    }
}
