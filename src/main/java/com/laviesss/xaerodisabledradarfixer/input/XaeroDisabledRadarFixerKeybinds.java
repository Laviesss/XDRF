package com.laviesss.xaerodisabledradarfixer.input;

import com.laviesss.xaerodisabledradarfixer.XaeroDisabledRadarFixerClientMod;
import dev.gxlg.versiont.api.R;
import dev.gxlg.versiont.api.V;
import dev.gxlg.versiont.gen.KeyMapping;

import java.lang.reflect.Constructor;

public final class XaeroDisabledRadarFixerKeybinds {

    public static final KeyMapping TOGGLE_ENABLED = create(
        "key.xaerodisabledradarfixer.toggle",
        "key.categories.xaerodisabledradarfixer",
        49, // N key
        "misc"
    );

    private static KeyMapping create(String id, String translationKey, int code, String categoryId) {
        if (V.higher("1.21.10")) {
            // 1.21.11+ uses KeyBinding.Category enum and InputConstants.Type
            try {
                // Get KeyBinding class via Version't (handles fallback chains)
                Class<?> keyBindingClass = R.clz("net.minecraft.class_304/net/minecraft/client/option/KeyBinding/net/minecraft/client/KeyMapping").self();
                
                // Get KeyBinding.Category enum class via Version't fallback chain
                Class<?> categoryClass = R.clz("net.minecraft.class_304$class_11900/net/minecraft/client/option/KeyBinding$Category/net/minecraft/client/KeyMapping$Category").self();
                
                // Find MISC enum constant
                Object misc = findEnumConstant(categoryClass, "MISC");
                if (misc == null) throw new IllegalStateException("MISC not found in KeyBinding.Category");

                // Get InputConstants.Type.KEYSYM
                Class<?> typeClass = Class.forName("net.minecraft.class_3675$class_3676");
                Object keySym = findEnumConstant(typeClass, "KEYSYM");
                if (keySym == null) throw new IllegalStateException("KEYSYM not found in InputConstants.Type");

                // Create KeyMapping with (String, InputConstants.Type, int, KeyBinding.Category)
                Constructor<?> constructor = keyBindingClass.getDeclaredConstructor(
                    String.class,
                    keySym.getClass(), // InputConstants.Type
                    int.class,
                    categoryClass
                );
                constructor.setAccessible(true);

                Object rawKeyMapping = constructor.newInstance(id, keySym, code, misc);
                return R.wrapperInst(KeyMapping.class, rawKeyMapping);
            } catch (Throwable e) {
                XaeroDisabledRadarFixerClientMod.LOGGER.error("Failed to create 1.21.11+ KeyMapping, falling back to pre-1.21.11 constructor", e);
                // Fall through to pre-1.21.11 constructor
            }
        }
        // Pre-1.21.11 uses String category
        return R.wrapperInst(KeyMapping.class,
            R.clz(KeyMapping.class).constr(String.class, String.class, int.class, String.class)
                .newInst(id, translationKey, code, categoryId));
    }

    private static Object findEnumConstant(Class<?> enumClass, String name) {
        if (enumClass == null || !enumClass.isEnum()) {
            return null;
        }
        for (Object c : enumClass.getEnumConstants()) {
            if (name.equals(((Enum<?>) c).name())) {
                return c;
            }
        }
        return null;
    }

    public static void register() {
        try {
            R.clz("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper")
                .mthd("registerKeyBinding", void.class, KeyMapping.class)
                .invk(null, TOGGLE_ENABLED);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean wasPressed() {
        return (boolean) R.clz("net.minecraft.class_304/net/minecraft/client/option/KeyBinding/net/minecraft/client/KeyMapping")
            .inst(TOGGLE_ENABLED.unwrap())
            .fld("wasPressed", boolean.class).get();
    }
}