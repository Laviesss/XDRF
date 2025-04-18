package com.laviesss.xaeroradarfixer;

import com.laviesss.xaeroradarfixer.config.XaeroRadarFixerConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

public class XaeroRadarFixerModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> XaeroRadarFixerConfigScreen.create(parent);
    }
}
