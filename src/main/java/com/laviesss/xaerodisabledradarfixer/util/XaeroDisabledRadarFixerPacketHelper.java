package com.laviesss.xaerodisabledradarfixer.util;

import com.laviesss.xaerodisabledradarfixer.XaeroDisabledRadarFixerClientMod;

import java.lang.reflect.Method;

public class XaeroDisabledRadarFixerPacketHelper {
    private static Object storedPacket = null;
    private static Object storedHandler = null;

    public static void setStored(Object packet, Object handler) {
        storedPacket = packet;
        storedHandler = handler;
    }

    public static void clearStored() {
        storedPacket = null;
        storedHandler = null;
    }

    public static Object getStoredPacket() {
        return storedPacket;
    }

    public static Object getStoredHandler() {
        return storedHandler;
    }

    public static void replayLastPacket() {
        if (storedPacket == null || storedHandler == null) {
            XaeroDisabledRadarFixerClientMod.LOGGER.warn("[XDRF] No stored packet to replay.");
            return;
        }
        try {
            Method accept = storedHandler.getClass().getMethod("accept", storedPacket.getClass());
            XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Replaying last blocked rules packet via {}", accept);
            accept.invoke(storedHandler, storedPacket);
        } catch (Exception e) {
            XaeroDisabledRadarFixerClientMod.LOGGER.error("[XDRF] Failed to replay rules packet", e);
        }
    }
}