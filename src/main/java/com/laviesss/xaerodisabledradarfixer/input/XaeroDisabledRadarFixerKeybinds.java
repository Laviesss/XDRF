package com.laviesss.xaerodisabledradarfixer.input;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class XaeroDisabledRadarFixerKeybinds {
    public static final KeyBinding TOGGLE_ENABLED = new KeyBinding(
            "key.xdrf.toggle_radar",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            "category.xdrf.keybinds"
    );

    public static void register() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_ENABLED);
    }

    public static boolean isTogglePressed() {
        return TOGGLE_ENABLED.wasPressed();
    }
}
