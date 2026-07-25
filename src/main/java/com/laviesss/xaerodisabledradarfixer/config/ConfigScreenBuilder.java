package com.laviesss.xaerodisabledradarfixer.config;

import dev.gxlg.versiont.api.V;
import net.minecraft.client.gui.screen.Screen;

/**
 * Version't chaining wrapper for config screen creation.
 * Uses V.lower() to select the appropriate version at runtime.
 */
public class ConfigScreenBuilder {
    static final Base implementation;

    static {
        // Check versions newest-first; first match wins
        if (!V.lower("26.2")) {
            implementation = new ConfigScreenBuilder_26_2();
        } else {
            implementation = new ConfigScreenBuilder_1_21();
        }
    }

    public static Screen createScreen(Screen parent) {
        return implementation.createScreenImpl(parent);
    }

    public abstract static class Base {
        protected abstract Screen createScreenImpl(Screen parent);
    }
}
