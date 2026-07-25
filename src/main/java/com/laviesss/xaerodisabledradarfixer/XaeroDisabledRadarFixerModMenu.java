package com.laviesss.xaerodisabledradarfixer;

import com.laviesss.xaerodisabledradarfixer.config.ConfigScreenBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

public class XaeroDisabledRadarFixerModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        return ConfigScreenBuilder::createScreen;
    }
}