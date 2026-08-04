package com.laviesss.xaerodisabledradarfixer.mixin.impl;

import com.laviesss.xaerodisabledradarfixer.XaeroDisabledRadarFixer;
import com.laviesss.xaerodisabledradarfixer.config.Config;
import com.laviesss.xaerodisabledradarfixer.service.ReplayService;
import com.laviesss.xaerodisabledradarfixer.util.NotificationHelper;
import dev.gxlg.versiont.gen.xaero.hud.packet.basic.ClientboundRulesPacket;
import dev.gxlg.versiont.gen.xaero.hud.packet.basic.ClientboundRulesPacket$ClientHandler;

public class XaeroMixinImpl {
    private static void logPacketDetails(Object packet) {
        try {
            Class<?> clazz = packet.getClass();
            XaeroDisabledRadarFixer.LOGGER.info("[XDRF] === Packet Details ===");
            XaeroDisabledRadarFixer.LOGGER.info("[XDRF] Packet class: {}", clazz.getName());

            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(packet);
                    XaeroDisabledRadarFixer.LOGGER.info("[XDRF]   Field: {} = {}", field.getName(), value);
                } catch (Exception e) {
                    XaeroDisabledRadarFixer.LOGGER.warn("[XDRF]   Could not read field: {}", field.getName());
                }
            }

            for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 && method.getName().startsWith("get") && !method.getName().equals("getClass")) {
                    method.setAccessible(true);
                    try {
                        Object value = method.invoke(packet);
                        XaeroDisabledRadarFixer.LOGGER.info("[XDRF]   Method: {}() = {}", method.getName(), value);
                    } catch (Exception e) {
                        XaeroDisabledRadarFixer.LOGGER.warn("[XDRF]   Could not invoke method: {}", method.getName());
                    }
                }
            }
            XaeroDisabledRadarFixer.LOGGER.info("[XDRF] === End Packet Details ===");
        } catch (Exception e) {
            XaeroDisabledRadarFixer.LOGGER.error("[XDRF] Failed to log packet details", e);
        }
    }

    public static boolean accept(ClientboundRulesPacket packet, ClientboundRulesPacket$ClientHandler handler) {
        Config config = Config.get();
        if (!config.isEnabled()) {
            return false;
        }
        if (ReplayService.isBlockingSuppressed() || !config.shouldBlockPackets()) {
            return false;
        }
        config.incrementPacketBlockedCount();
        XaeroDisabledRadarFixer.LOGGER.info("[XDRF] Blocked a server-enforced minimap rules packet (radar/cave-mode). Total: {}", config.getPacketBlockedCount());

        ReplayService.recordBlockedXaeroPacket(packet, handler);
        if (config.isVerboseLogging()) {
            logPacketDetails(packet);
        }
        if (config.isShowChatMessage()) {
            NotificationHelper.sendPacketMessage();
        }
        if (config.isShowToast()) {
            NotificationHelper.showPacketToast();
        }
        if (config.isPlaySound()) {
            NotificationHelper.playSound();
        }
        return true;
    }
}