package com.laviesss.xaerodisabledradarfixer;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.laviesss.xaeroradarfixer.XaeroDisabledRadarFixerMixin;


public class XaeroDisabledRadarFixerClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // This is where you register the mixin
    }
}
