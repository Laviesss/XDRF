package com.laviesss.xaerodisabledradarfixer.util.chaining.gui;

import dev.gxlg.versiont.gen.net.minecraft.client.Minecraft;
import dev.gxlg.versiont.gen.net.minecraft.client.gui.components.toasts.SystemToast;
import dev.gxlg.versiont.gen.net.minecraft.client.gui.components.toasts.SystemToast$SystemToastId;
import dev.gxlg.versiont.gen.net.minecraft.client.gui.components.toasts.ToastManager;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;

public class Gui_1_21_0 extends Gui.Base {
    @Override
    public void showToast(Component title, Component message) {
        ToastManager toastManager = getToastManager();
        SystemToast.add(toastManager, SystemToast$SystemToastId.WORLD_BACKUP(), title, message);
    }

    protected ToastManager getToastManager() {
        return Minecraft.getInstance().getToastManager();
    }
}
