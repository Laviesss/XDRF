package com.laviesss.xaerodisabledradarfixer.util;

import dev.gxlg.versiont.api.R;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;

import java.lang.reflect.Array;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Version't-based helpers for YACL config screen creation.
 * <p>
 * All YACL class resolution and method invocation goes through Version't
 * ({@code R.clz(...).mthd(...).invk(...)}) — no manual {@code Class.forName()}
 * or {@code Method.invoke()} calls. This follows the same pattern as
 * {@link XaeroDisabledRadarFixerTextHelper}.
 * <p>
 * YACL types are resolved via Version't at runtime — no direct import
 * references to {@code dev.isxander.yacl3.*} appear in our bytecode.
 * All public methods return {@code Object} so callers never see YACL types.
 */
public class XaeroDisabledRadarFixerYaclHelper {

    // ── YACL class name constants (single-name format for non-MC) ──────

    private static final String YACL        = "dev.isxander.yacl3.api.YetAnotherConfigLib";
    private static final String OPTION_DESC = "dev.isxander.yacl3.api.OptionDescription";
    private static final String BOOL_CTRL   = "dev.isxander.yacl3.api.controller.BooleanControllerBuilder";
    private static final String ENUM_CTRL   = "dev.isxander.yacl3.api.controller.EnumControllerBuilder";

    // ── Text creation ───────────────────────────────────────────────────

    /**
     * Creates a vanilla {@code Text} object via {@link XaeroDisabledRadarFixerTextHelper}.
     */
    public static Object text(String content) {
        return XaeroDisabledRadarFixerTextHelper.literal(content);
    }

    // ── Helper: unwrap Version't wrappers ───────────────────────────────

    /**
     * If the object is a Version't wrapper (from {@code dev.gxlg.versiont.gen.*}),
     * calls {@code unwrap()} to return the raw object. Otherwise returns as-is.
     * <p>
     * YACL methods at runtime expect raw objects — never Version't wrappers —
     * so every object that crosses the YaclHelper ↔ YACL boundary must be unwrapped.
     */
    static Object unwrapIfWrapper(Object obj) {
        if (obj == null) return null;
        String className = obj.getClass().getName();
        if (className.startsWith("dev.gxlg.versiont.gen.")) {
            try {
                return obj.getClass().getMethod("unwrap").invoke(obj);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to unwrap Version't wrapper: " + className, e);
            }
        }
        return obj;
    }

    // ── OptionDescription ───────────────────────────────────────────────

    /**
     * Invokes {@code OptionDescription.of(Component...)} via Version't.
     * <p>
     * At bytecode level varargs is a single array parameter, so we build the
     * {@code Component[]} ourselves and pass it directly.
     */
    public static Object optionDesc(Object... texts) {
        try {
            // Build Component[] array (raw MC type — YACL expects raw objects)
            Object array = Array.newInstance(Component.clazz.self(), texts.length);
            for (int i = 0; i < texts.length; i++) {
                Array.set(array, i, unwrapIfWrapper(texts[i]));
            }
            // OptionDescription.of(Component[])  via Version't
            R.RClass descClass = R.clz(OPTION_DESC);
            return descClass
                    .mthd("of/of", descClass.self(), Component.clazz.self().arrayType())
                    .invk(array);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke OptionDescription.of()", e);
        }
    }

    // ── Generic YACL text-method invocation ─────────────────────────────

    /**
     * Invokes a YACL builder method that accepts a single vanilla {@code Text}
     * parameter — {@code title}, {@code name}, etc.
     * <p>
     * Unwraps the text object before passing it to YACL (YACL always expects
     * raw MC objects, never Version't wrappers).
     */
    @SuppressWarnings("unchecked")
    private static <T> T callYaclText(T builder, String methodName, Object textObj) {
        try {
            Object rawText = unwrapIfWrapper(textObj);
            R.RClass builderClass = R.clz(builder.getClass().getName());
            return (T) builderClass
                    .inst(builder)
                    .mthd(methodName + "/" + methodName,
                            builderClass.self(),
                            Component.clazz.self())
                    .invk(rawText);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke " + methodName + "()", e);
        }
    }

    // ── Static factory methods ──────────────────────────────────────────

    /**
     * Creates a YACL builder via Version't reflection.
     * <p>
     * Works for {@code YetAnotherConfigLib}, {@code ConfigCategory},
     * {@code Option}, and {@code ButtonOption} — the caller passes the
     * fully-qualified class name and gets back a raw builder object.
     */
    public static Object createBuilder(String fullyQualifiedClassName) {
        try {
            R.RClass cls = R.clz(fullyQualifiedClassName);
            return cls
                    .mthd("createBuilder/createBuilder", cls.self())
                    .invk();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create builder: " + fullyQualifiedClassName, e);
        }
    }

    // ── Convenience: title / name ───────────────────────────────────────

    /**
     * Sets the title on a {@code YetAnotherConfigLib.Builder}.
     */
    public static <T> T title(T builder, String content) {
        return callYaclText(builder, "title", text(content));
    }

    /**
     * Sets the name on any YACL builder (category, option, button).
     */
    public static Object name(Object builder, String content) {
        return callYaclText(builder, "name", text(content));
    }

    // ── Convenience: description ────────────────────────────────────────

    /**
     * Sets the description on any YACL builder
     * via {@code builder.description(OptionDescription.of(text))}.
     */
    public static Object desc(Object builder, String content) {
        try {
            Object descObj = optionDesc(text(content));
            R.RClass builderClass = R.clz(builder.getClass().getName());
            return builderClass
                    .inst(builder)
                    .mthd("description/description",
                            builderClass.self(),
                            R.clz(OPTION_DESC).self())
                    .invk(descObj);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke description()", e);
        }
    }

    // ── Convenience: binding ────────────────────────────────────────────

    /**
     * Calls {@code builder.binding(defaultValue, getter, setter)} via Version't.
     * <p>
     * Uses raw {@code Object} params — callers cast method references to the
     * correct functional interface ({@code Supplier<T>}, {@code Consumer<T>}).
     */
    public static Object binding(Object builder, Object defaultValue,
                                  Object getter, Object setter) {
        try {
            R.RClass builderClass = R.clz(builder.getClass().getName());
            return builderClass
                    .inst(builder)
                    .mthd("binding/binding",
                            builderClass.self(),
                            Object.class,
                            Supplier.class,
                            Consumer.class)
                    .invk(defaultValue, getter, setter);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke binding()", e);
        }
    }

    // ── Convenience: controller ─────────────────────────────────────────

    /**
     * Calls {@code builder.controller(function)} via Version't.
     */
    public static Object controller(Object builder, Object function) {
        try {
            R.RClass builderClass = R.clz(builder.getClass().getName());
            return builderClass
                    .inst(builder)
                    .mthd("controller/controller",
                            builderClass.self(),
                            Function.class)
                    .invk(function);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke controller()", e);
        }
    }

    // ── Controller factories ────────────────────────────────────────────

    /**
     * Returns a {@code Function<Option, BooleanControllerBuilder>} that creates
     * a coloured boolean controller — without importing {@code BooleanControllerBuilder}.
     */
    @SuppressWarnings("unchecked")
    public static Object booleanControllerFactory() {
        return (Function<Object, Object>) opt -> {
            try {
                R.RClass ctrlClass = R.clz(BOOL_CTRL);
                Object rawOpt = unwrapIfWrapper(opt);
                Object cbc = ctrlClass
                        .mthd("create/create", ctrlClass.self())
                        .invk(rawOpt);
                R.RClass cbcClass = R.clz(cbc.getClass().getName());
                return cbcClass
                        .inst(cbc)
                        .mthd("coloured/coloured", cbcClass.self(), boolean.class)
                        .invk(true);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to create boolean controller", e);
            }
        };
    }

    /**
     * Returns a {@code Function<Option, EnumControllerBuilder>} that creates
     * an enum controller for the given enum class — without importing
     * {@code EnumControllerBuilder}.
     */
    @SuppressWarnings("unchecked")
    public static Object enumControllerFactory(Class<?> enumClass) {
        return (Function<Object, Object>) opt -> {
            try {
                R.RClass ctrlClass = R.clz(ENUM_CTRL);
                Object rawOpt = unwrapIfWrapper(opt);
                Object ec = ctrlClass
                        .mthd("create/create", ctrlClass.self())
                        .invk(rawOpt);
                R.RClass ecClass = R.clz(ec.getClass().getName());
                ecClass
                        .inst(ec)
                        .mthd("enumClass/enumClass", ecClass.self(), Class.class)
                        .invk(enumClass);
                return ec;
            } catch (Throwable e) {
                throw new RuntimeException("Failed to create enum controller", e);
            }
        };
    }

    // ── Convenience: build / option / category / action ─────────────────

    /**
     * Calls {@code builder.build()} via Version't.
     */
    public static Object build(Object builder) {
        try {
            R.RClass builderClass = R.clz(builder.getClass().getName());
            return builderClass
                    .inst(builder)
                    .mthd("build/build", builderClass.self())
                    .invk();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke build()", e);
        }
    }

    /**
     * Calls {@code catBuilder.option(option)} via Version't.
     * Unwraps the option before passing (YACL expects raw objects).
     */
    public static Object option(Object catBuilder, Object opt) {
        try {
            R.RClass builderClass = R.clz(catBuilder.getClass().getName());
            return builderClass
                    .inst(catBuilder)
                    .mthd("option/option", builderClass.self())
                    .invk(unwrapIfWrapper(opt));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke option()", e);
        }
    }

    /**
     * Calls {@code yaclBuilder.category(category)} via Version't.
     * Unwraps the category before passing (YACL expects raw objects).
     */
    public static Object category(Object yaclBuilder, Object cat) {
        try {
            R.RClass builderClass = R.clz(yaclBuilder.getClass().getName());
            return builderClass
                    .inst(yaclBuilder)
                    .mthd("category/category", builderClass.self())
                    .invk(unwrapIfWrapper(cat));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke category()", e);
        }
    }

    /**
     * Calls {@code btnBuilder.action(biConsumer)} via Version't.
     * {@code BiConsumer} type ensures the compiler can infer lambda types.
     */
    public static Object action(Object btnBuilder, BiConsumer<Object, Object> biConsumer) {
        try {
            R.RClass builderClass = R.clz(btnBuilder.getClass().getName());
            return builderClass
                    .inst(btnBuilder)
                    .mthd("action/action",
                            builderClass.self(),
                            BiConsumer.class)
                    .invk(biConsumer);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke action()", e);
        }
    }

    // ── generateScreen ──────────────────────────────────────────────────

    /**
     * Calls {@code YetAnotherConfigLib.generateScreen(Screen)} via Version't
     * reflection ({@code R.clz(...).inst(...).mthd(...).invk(...)}).
     * <p>
     * Returns a raw {@code Screen} (what ModMenu expects).
     */
    public static Object generateScreen(Object yaclScreen, Object parentScreen) {
        try {
            R.RClass screenClass = R.clz(
                    "net.minecraft.class_437/net.minecraft.client.gui.screens.Screen");
            return R.clz(YACL)
                    .inst(yaclScreen)
                    .mthd("generateScreen/generateScreen", screenClass.self())
                    .invk(parentScreen);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to generate config screen via YACL", e);
        }
    }
}
