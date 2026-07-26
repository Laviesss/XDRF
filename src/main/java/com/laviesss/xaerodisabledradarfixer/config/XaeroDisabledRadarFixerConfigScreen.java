package com.laviesss.xaerodisabledradarfixer.config;

import dev.gxlg.versiont.api.V;

/**
 * Version't chaining wrapper for config screen creation.
 * Uses V.lower() to select the appropriate version at runtime.
 * <p>
 * All method signatures use {@code Object} instead of {@code Screen}
 * to avoid bytecode references to intermediary class names that fail
 * on MC 26.2+ where Fabric Loader has no mappings.
 */
public class XaeroDisabledRadarFixerConfigScreen {
    static final Base implementation;

    static {
        // Check versions newest-first; first match wins.
        // V.lower("26") returns true if MC version < 26.0,
        // so all 26.x versions use the 26 subclass.
        if (!V.lower("26")) {
            implementation = new XaeroDisabledRadarFixerConfigScreen_26();
        } else {
            implementation = new XaeroDisabledRadarFixerConfigScreen_1_21();
        }
    }

    /**
     * Create the config screen. Returns vanilla Screen at runtime.
     * Signature uses Object to avoid Screen bytecode reference.
     *
     * @param parent the parent screen (vanilla Screen at runtime)
     * @return vanilla Screen at runtime
     */
    public static Object createScreen(Object parent) {
        return implementation.createScreenImpl(parent);
    }

    public abstract static class Base {
        protected abstract Object createScreenImpl(Object parent);
    }
}
