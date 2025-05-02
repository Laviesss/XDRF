package com.laviesss.xaerodisabledradarfixer;

import com.laviesss.xaerodisabledradarfixer.config.XaeroRadarFixerConfig;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.fabricmc.api.ClientModInitializer;

public class XaeroDisabledRadarFixerClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Save the config when the mod is initialized
        XaeroRadarFixerConfig.load();  // Load the configuration if necessary
        // You can add other client-side setup here if needed
    }
}
