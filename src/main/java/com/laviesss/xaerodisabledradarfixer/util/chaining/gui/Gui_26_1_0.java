package com.laviesss.xaerodisabledradarfixer.util.chaining.gui;

import dev.gxlg.versiont.gen.net.minecraft.client.Minecraft;
import dev.gxlg.versiont.gen.net.minecraft.client.gui.components.toasts.ToastManager;

public class Gui_26_1_0 extends Gui_1_21_0 {
    @Override
    protected ToastManager getToastManager() {
        return Minecraft.getInstance().getGuiField().toastManager();
    }
}
