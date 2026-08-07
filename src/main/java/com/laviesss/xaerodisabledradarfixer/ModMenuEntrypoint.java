package com.laviesss.xaerodisabledradarfixer;

import com.laviesss.xaerodisabledradarfixer.config.ConfigScreen;
import dev.gxlg.versiont.api.R;
import dev.gxlg.versiont.api.types.Wrapper;
import dev.gxlg.versiont.gen.net.minecraft.client.gui.screens.Screen;
import dev.gxlg.versiont.plugins.modmenu.VersiontModMenuApi;

public class ModMenuEntrypoint implements VersiontModMenuApi {
    @Override
    public Screen getModConfigScreen(Wrapper<?> parentScreen) {
        return ConfigScreen.createConfigScreen((Screen) parentScreen);
    }

    @Override
    public Screen wrapScreen(Object screen) {
        return R.wrapperInst(Screen.class, screen);
    }
}
