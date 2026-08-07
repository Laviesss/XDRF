package com.laviesss.xaerodisabledradarfixer;

import com.laviesss.xaerodisabledradarfixer.service.ReplayService;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XaeroDisabledRadarFixer implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("XDRF");

    @Override
    public void onInitializeClient() {
        ReplayService.init();
        LOGGER.info("[XDRF] Loaded. Radar blocking active.");
    }
}