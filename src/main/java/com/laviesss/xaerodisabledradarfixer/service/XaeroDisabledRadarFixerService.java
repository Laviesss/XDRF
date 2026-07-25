package com.laviesss.xaerodisabledradarfixer.service;

import com.laviesss.xaerodisabledradarfixer.XaeroDisabledRadarFixerClientMod;
import com.laviesss.xaerodisabledradarfixer.config.XaeroDisabledRadarFixerConfig;
import com.laviesss.xaerodisabledradarfixer.util.XaeroDisabledRadarFixerPacketHelper;
import com.laviesss.xaerodisabledradarfixer.util.XaeroDisabledRadarFixerTextHelper;
import dev.gxlg.versiont.api.R;

import dev.gxlg.versiont.gen.net.minecraft.client.Minecraft;
import dev.gxlg.versiont.gen.net.minecraft.client.multiplayer.ClientPacketListener;
import dev.gxlg.versiont.gen.net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;

import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.lang.reflect.Field;

public final class XaeroDisabledRadarFixerService {

    public enum LastBlockedType {
        CHAT_CODE,
        RULES_PACKET,
        BOTH
    }

    // ── Session cache ──────────────────────────────────────────
    private static LastBlockedType lastBlockedType = null;
    private static String lastBlockedChatCode = null;
    private static Object lastBlockedRulesPacket = null;
    private static boolean sessionActive = false;

    // ── Existing fields ───────────────────────────────────────
    private static String lastSentCode = "";
    private static boolean suppressBlocking = false;

    private XaeroDisabledRadarFixerService() {}

    // ── Session management ────────────────────────────────────
    public static void startSession() {
        sessionActive = true;
        clearCache();
        XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Session started – cache cleared.");
    }

    public static void endSession() {
        sessionActive = false;
        clearCache();
        XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Session ended – cache cleared.");
    }

    public static void clearCache() {
        lastBlockedType = null;
        lastBlockedChatCode = null;
        lastBlockedRulesPacket = null;
    }

    // ── Getters ────────────────────────────────────────────────
    public static LastBlockedType getLastBlockedType() { return lastBlockedType; }
    public static String getLastBlockedChatCode() { return lastBlockedChatCode; }
    public static Object getLastBlockedRulesPacket() { return lastBlockedRulesPacket; }
    public static boolean isSessionActive() { return sessionActive; }

    // ── Record blocked items ──────────────────────────────────
    public static void recordBlockedChatCode(String code) {
        if (!sessionActive) return;
        lastBlockedChatCode = code;
        if (lastBlockedType == null || lastBlockedType == LastBlockedType.CHAT_CODE) {
            lastBlockedType = LastBlockedType.CHAT_CODE;
        } else if (lastBlockedType == LastBlockedType.RULES_PACKET) {
            lastBlockedType = LastBlockedType.BOTH;
        }
        XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Recorded blocked chat code: {}", code);
    }

    public static void recordBlockedRulesPacket(Object packet) {
        if (!sessionActive) return;
        lastBlockedRulesPacket = packet;
        if (lastBlockedType == null || lastBlockedType == LastBlockedType.RULES_PACKET) {
            lastBlockedType = LastBlockedType.RULES_PACKET;
        } else if (lastBlockedType == LastBlockedType.CHAT_CODE) {
            lastBlockedType = LastBlockedType.BOTH;
        }
        XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Recorded blocked rules packet.");
    }

    // ── Reset actions ─────────────────────────────────────────
    public static void sendResetCode(String code) {
        suppressBlocking = true;
        sendSystemMessage(code);
        suppressBlocking = false;
    }

    /**
     * Replays the last blocked rules packet after setting its radar/cave-mode
     * fields to the given value.
     *
     * @param packet the stored packet to mutate and replay
     * @param allow  true = re-enable radar/cave mode (revoke), false = re-apply
     *               the server's original block (enforce)
     */
    public static void sendModifiedRulesPacket(Object packet, boolean allow) {
        try {
            Class<?> clazz = packet.getClass();
            Field radarField = clazz.getDeclaredField("allowRadarOnServer");
            radarField.setAccessible(true);
            radarField.set(packet, allow);

            Field caveField = clazz.getDeclaredField("allowCaveModeOnServer");
            caveField.setAccessible(true);
            caveField.set(packet, allow);

            Field netherCaveField = clazz.getDeclaredField("allowNetherCaveModeOnServer");
            netherCaveField.setAccessible(true);
            netherCaveField.set(packet, allow);

            // Replay the modified packet
            suppressBlocking = true;
            XaeroDisabledRadarFixerPacketHelper.replayLastPacket();
            suppressBlocking = false;

            XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Sent modified rules packet (all {}).", allow);
        } catch (Exception e) {
            XaeroDisabledRadarFixerClientMod.LOGGER.error("[XDRF] Failed to send modified rules packet", e);
        }
    }

    public static void enforceBlocking() {
        if (!sessionActive || lastBlockedType == null) {
            XaeroDisabledRadarFixerClientMod.LOGGER.warn("[XDRF] Nothing to enforce – no blocked items this session.");
            return;
        }

        XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Enforcing blocking – last blocked type: {}", lastBlockedType);

        switch (lastBlockedType) {
            case CHAT_CODE -> {
                if (lastBlockedChatCode != null && !lastBlockedChatCode.isEmpty()) {
                    sendResetCode(lastBlockedChatCode);
                }
            }
            case RULES_PACKET -> {
                if (lastBlockedRulesPacket != null) {
                    // Re-apply the server's original block (fields -> false)
                    sendModifiedRulesPacket(lastBlockedRulesPacket, false);
                }
            }
            case BOTH -> {
                if (lastBlockedChatCode != null && !lastBlockedChatCode.isEmpty()) {
                    sendResetCode(lastBlockedChatCode);
                }
                if (lastBlockedRulesPacket != null) {
                    sendModifiedRulesPacket(lastBlockedRulesPacket, false);
                }
            }
        }
    }

    public static void revokeBlocking() {
        if (!sessionActive || lastBlockedType == null) {
            XaeroDisabledRadarFixerClientMod.LOGGER.warn("[XDRF] Nothing to revoke – no blocked items this session.");
            return;
        }

        XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Revoking blocking – last blocked type: {}", lastBlockedType);

        switch (lastBlockedType) {
            case CHAT_CODE -> {
                sendResetCode("§r§e§s§e§t§x§a§e§r§o");
            }
            case RULES_PACKET -> {
                if (lastBlockedRulesPacket != null) {
                    // Re-enable radar/cave mode (fields -> true)
                    sendModifiedRulesPacket(lastBlockedRulesPacket, true);
                }
            }
            case BOTH -> {
                sendResetCode("§r§e§s§e§t§x§a§e§r§o");
                if (lastBlockedRulesPacket != null) {
                    sendModifiedRulesPacket(lastBlockedRulesPacket, true);
                }
            }
        }
    }

    // ── Existing methods ──────────────────────────────────────
    public static void setLastSentCode(String code) { lastSentCode = code; }
    public static String getLastSentCode() { return lastSentCode; }
    public static boolean isBlockingSuppressed() { return suppressBlocking; }

    public static boolean shouldBlockChatMessage(ClientboundSystemChatPacket packet) {
        XaeroDisabledRadarFixerConfig config = XaeroDisabledRadarFixerConfig.get();
        if (!config.isEnabled()) return false;
        if (suppressBlocking) return false;
        if (config.getBlockingScope() == XaeroDisabledRadarFixerConfig.BlockingScope.PACKET) return false;
        return false;
    }

    public static void recordBlockedChatMessage(ClientboundSystemChatPacket packet) {
        try {
            Component content = packet.content();
            String text = content.unwrap().toString();
            recordBlockedChatCode(text);
        } catch (Exception e) {
            XaeroDisabledRadarFixerClientMod.LOGGER.warn("[XDRF] Could not record blocked chat message", e);
        }
    }

    public static boolean shouldBlockChatMessage(GameMessageS2CPacket packet) {
        return shouldBlockChatMessage(R.wrapperInst(ClientboundSystemChatPacket.class, packet));
    }

    public static void recordBlockedChatMessage(GameMessageS2CPacket packet) {
        recordBlockedChatMessage(R.wrapperInst(ClientboundSystemChatPacket.class, packet));
    }

    public static boolean shouldBlockPacketRules(Object packet) {
        XaeroDisabledRadarFixerConfig config = XaeroDisabledRadarFixerConfig.get();
        return config.isEnabled() && config.isBlockPacketRules() && !suppressBlocking
                && config.getBlockingScope() != XaeroDisabledRadarFixerConfig.BlockingScope.CHAT_MESSAGE;
    }

    public static void recordBlockedPacket(Object packet) {
        recordBlockedRulesPacket(packet);
    }

    public static boolean shouldShowChatMessage() {
        return XaeroDisabledRadarFixerConfig.get().isShowChatMessage();
    }

    public static boolean shouldShowToast() {
        return XaeroDisabledRadarFixerConfig.get().isShowToast();
    }

    public static boolean shouldPlaySound() {
        return XaeroDisabledRadarFixerConfig.get().isPlaySound();
    }

    public static void sendResetCode() {
        String resetCode = "§r§e§s§e§t§x§a§e§r§o";
        sendResetCode(resetCode);
    }

    private static void sendSystemMessage(String content) {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener conn = mc == null ? null : mc.getNetworkHandler();
        if (conn == null) return;
        lastSentCode = content;
        ClientboundSystemChatPacket packet = new ClientboundSystemChatPacket(XaeroDisabledRadarFixerTextHelper.literal(content), false);
        XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Sending radar code to self: {}", content);
        conn.handleSystemChat(packet);
    }
}