package com.laviesss.xaerodisabledradarfixer;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XaeroDisabledRadarFixerClientMod implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("XDRF");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[XDRF] Loaded. Radar blocking active.");
    }
}