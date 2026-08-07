package com.laviesss.xaerodisabledradarfixer.util.chaining.player;

import dev.gxlg.versiont.api.V;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;

public class Player {
    private static final Base implementation;

    static {
        if (!V.lower("26.1")) {
            implementation = new Player_26_1_0();
        } else {
            implementation = new Player_1_21_0();
        }
    }

    public static void sendMessage(Component text) {
        implementation.sendMessage(text);
    }

    public abstract static class Base {
        public abstract void sendMessage(Component text);
    }
}
