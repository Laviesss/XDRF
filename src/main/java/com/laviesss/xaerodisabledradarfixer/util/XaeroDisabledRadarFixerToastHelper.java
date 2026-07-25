package com.laviesss.xaerodisabledradarfixer.util;

import dev.gxlg.versiont.api.R;
import dev.gxlg.versiont.api.V;
import dev.gxlg.versiont.gen.net.minecraft.client.Minecraft;
import dev.gxlg.versiont.gen.net.minecraft.client.gui.components.toasts.ToastManager;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;
import com.laviesss.xaerodisabledradarfixer.XaeroDisabledRadarFixerClientMod;

/**
 * Handles toast notifications across different Minecraft versions using Version't reflection.
 * <p>
 * All toast types are resolved at runtime via {@link R#clz(String)} to avoid the broken
 * code-generation of {@code SystemToast$Type} wrappers (inner-class wrappers are placed
 * at the wrong package by the Version't code generator).
 * <p>
 * Both paths use the same {@code R.clz()} calls — the only difference is how we obtain
 * the {@link ToastManager}:
 * <ul>
 *   <li><b>1.21:</b> {@code Minecraft.getToastManager()} (direct method)</li>
 *   <li><b>26.x:</b> {@code Minecraft.gui.toastManager()} (field access + method call)</li>
 * </ul>
 * {@code SystemToast$Type} resolves to {@code SystemToast$SystemToastId} on 26.x via the
 * slash-separated fallback chain in the class lookup.
 */
public class XaeroDisabledRadarFixerToastHelper {

    /** Intermediary/mojmap fallback chain for SystemToast class */
    private static final String TOAST_CLASS =
            "net.minecraft.class_370/net.minecraft.client.gui.components.toasts.SystemToast";

    /** Intermediary/mojmap fallback chain for SystemToast$Type / SystemToast$SystemToastId */
    private static final String TOAST_TYPE_CLASS =
            "net.minecraft.class_370$class_9037"
            + "/net.minecraft.client.gui.components.toasts.SystemToast$Type"
            + "/net.minecraft.client.gui.components.toasts.SystemToast$SystemToastId";

    /** Intermediary/mojmap fallback chain for ToastManager */
    private static final String TOAST_MANAGER_CLASS =
            "net.minecraft.class_374/net.minecraft.client.gui.components.toasts.ToastManager";

    /** Intermediary/mojmap fallback chain for Minecraft */
    private static final String MC_CLASS =
            "net.minecraft.class_310/net.minecraft.client.Minecraft";

    /** Intermediary/mojmap fallback chain for Gui */
    private static final String GUI_CLASS =
            "net.minecraft.class_329/net.minecraft.client.gui.Gui";

    /** Intermediary/mojmap fallback chain for Component */
    private static final String COMPONENT_CLASS =
            "net.minecraft.class_2561/net.minecraft.network.chat.Component";

    /**
     * Shows a system toast notification.
     *
     * @param title   The toast title (Component)
     * @param message The toast message (Component)
     */
    public static void showToast(Component title, Component message) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                XaeroDisabledRadarFixerClientMod.LOGGER.warn("[XDRF] Minecraft instance is null");
                return;
            }

            // ── Obtain ToastManager ──────────────────────────────
            Object tm;
            if (V.isObfuscated()) {
                // 1.21 path: direct getToastManager()
                tm = mc.getToastManager().unwrap();
            } else {
                // 26.x path: resolve Gui via R.clz() field access, then Gui.toastManager()
                R.RClass mcClass = R.clz(MC_CLASS);
                R.RClass guiClass = R.clz(GUI_CLASS);
                Object guiRaw = mcClass.inst(mc.unwrap())
                        .fld("gui", guiClass.self())
                        .get();
                if (guiRaw == null) {
                    XaeroDisabledRadarFixerClientMod.LOGGER.warn("[XDRF] Gui instance is null");
                    return;
                }
                // Gui.toastManager() via R.clz()
                Object tmRaw = guiClass.inst(guiRaw)
                        .mthd("toastManager/toastManager", R.clz(TOAST_MANAGER_CLASS).self())
                        .invk();
                if (tmRaw == null) {
                    XaeroDisabledRadarFixerClientMod.LOGGER.warn("[XDRF] ToastManager is null (26.x path)");
                    return;
                }
                tm = tmRaw;
            }

            if (tm == null) {
                XaeroDisabledRadarFixerClientMod.LOGGER.warn("[XDRF] ToastManager is null");
                return;
            }

            // ── Resolve SystemToast$Type.WORLD_BACKUP ────────────
            // R.clz() with slash-separated fallback chain handles the
            // 1.21 → 26.x class rename automatically.
            R.RClass typeClass = R.clz(TOAST_TYPE_CLASS);
            Object worldBackup = typeClass
                    .fld("field_47584/WORLD_BACKUP", typeClass.self())
                    .get();

            // ── Call SystemToast.add(ToastManager, Type, Component, Component) ──
            R.RClass toastClass = R.clz(TOAST_CLASS);
            R.RClass compClass = R.clz(COMPONENT_CLASS);

            toastClass.mthd(
                    "method_27024/add",
                    void.class,
                    R.clz(TOAST_MANAGER_CLASS).self(),
                    typeClass.self(),
                    compClass.self(),
                    compClass.self()
            ).invk(tm, worldBackup, title.unwrap(), message.unwrap());

        } catch (Throwable e) {
            XaeroDisabledRadarFixerClientMod.LOGGER.warn("[XDRF] Failed to show toast", e);
        }
    }
}
