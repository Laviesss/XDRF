package com.laviesss.xaerodisabledradarfixer.util.chaining.gui;

import dev.gxlg.versiont.api.V;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;

public class Gui {
    private static final Base implementation;

    static {
        if (!V.lower("26.1")) {
            implementation = new Gui_26_1_0();
        } else {
            implementation = new Gui_1_21_0();
        }
    }

    public static void showToast(Component title, Component message) {
        implementation.showToast(title, message);
    }

    public static abstract class Base {
        public abstract void showToast(Component title, Component message);
    }
}
