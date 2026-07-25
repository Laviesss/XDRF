package com.laviesss.xaerodisabledradarfixer.util;

import dev.gxlg.versiont.api.R;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.MutableComponent;
import dev.gxlg.versiont.gen.net.minecraft.ChatFormatting;

public class XaeroDisabledRadarFixerTextHelper {

    private static final String TEXT_CLASS = "net.minecraft.class_2561/net.minecraft.network.chat.Component";
    private static final String MUTABLE_TEXT_CLASS = "net.minecraft.class_5250/net.minecraft.network.chat.MutableComponent";

    /**
     * Creates a plain Component from a String.
     * <p>
     * Implementation note: we use {@code nullToEmpty(String)} rather than
     * {@code literal(String)} because:
     * <ul>
     *   <li>{@code nullToEmpty} returns the interface type {@code Component}
     *       (verified intermediary id {@code method_30163}), matching what our
     *       reflection signature declaration expects.</li>
     *   <li>{@code literal} is a static-in-interface method whose intermediary
     *       ID we could not pin down via the available MCP tooling &mdash; the
     *       cleanest workaround is calling the verified sibling method that
     *       internally delegates to {@code literal}.</li>
     * </ul>
     */
    public static Component literal(String text) {
        try {
            R.RClass textClass = R.clz(TEXT_CLASS);
            Object raw = textClass
                    .mthd("method_30163/nullToEmpty", Component.clazz.self(), String.class)
                    .invk(text);
            return R.wrapperInst(Component.class, raw);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create literal text", e);
        }
    }

    public static Component formatted(Component component, ChatFormatting formatting) {
        try {
            R.RClass mutableTextClass = R.clz(MUTABLE_TEXT_CLASS);
            Object raw = component.unwrap();
            Object result = mutableTextClass
                    .inst(raw)
                    .mthd("method_27692/withStyle", MutableComponent.clazz.self(), ChatFormatting.clazz.self())
                    .invk(formatting.unwrap());
            return R.wrapperInst(Component.class, result);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to format text", e);
        }
    }
}