package com.laviesss.xaerodisabledradarfixer.service;

import com.laviesss.xaerodisabledradarfixer.XaeroDisabledRadarFixerClientMod;
import com.laviesss.xaerodisabledradarfixer.config.XaeroDisabledRadarFixerConfig;
import com.laviesss.xaerodisabledradarfixer.mixin.XaeroDisabledRadarFixerRulesMixin;
import dev.gxlg.versiont.api.R;
import dev.gxlg.versiont.gen.Minecraft;
import dev.gxlg.versiont.gen.ClientPacketListener;
import dev.gxlg.versiont.gen.ClientboundSystemChatPacket;
import dev.gxlg.versiont.gen.Component;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public final class XaeroDisabledRadarFixerService {

    public enum LastBlockedType { CHAT_CODE, RULES_PACKET }

    private static String lastSentCode = "";
    private static boolean suppressBlocking = false;
    private static LastBlockedType lastBlockedType = LastBlockedType.CHAT_CODE;

    private XaeroDisabledRadarFixerService() {}

    public static void setLastSentCode(String code) { lastSentCode = code; }
    public static String getLastSentCode() { return lastSentCode; }
    public static boolean isBlockingSuppressed() { return suppressBlocking; }
    public static LastBlockedType getLastBlockedType() { return lastBlockedType; }
    public static void setLastBlockedType(LastBlockedType type) { lastBlockedType = type; }

    // Called from Mixin - receives Version't wrapped packet
    public static boolean shouldBlockChatMessage(dev.gxlg.versiont.gen.ClientboundSystemChatPacket packet) {
        XaeroDisabledRadarFixerConfig config = XaeroDisabledRadarFixerConfig.get();
        if (!config.isEnabled()) return false;
        if (suppressBlocking) return false;
        // Check if packet content matches blocked codes
        return false;
    }

    public static void recordBlockedChatPacket(dev.gxlg.versiont.gen.ClientboundSystemChatPacket packet) {
        // Record the packet for later replay
    }

    // Accepts raw GameMessageS2CPacket (1.21 Yarn name) from public API
    public static boolean shouldBlockChatMessage(GameMessageS2CPacket packet) {
        return shouldBlockChatMessage(R.wrapperInst(dev.gxlg.versiont.gen.ClientboundSystemChatPacket.class, packet));
    }

    public static void recordBlockedChatPacket(GameMessageS2CPacket packet) {
        recordBlockedChatPacket(R.wrapperInst(dev.gxlg.versiont.gen.ClientboundSystemChatPacket.class, packet));
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

    public static void enforceBlocking() {
        switch (lastBlockedType) {
            case CHAT_CODE -> {
                if (!lastSentCode.isEmpty()) {
                    suppressBlocking = true;
                    sendSystemMessage(lastSentCode);
                    suppressBlocking = false;
                }
            }
            case RULES_PACKET -> {
                suppressBlocking = true;
                XaeroDisabledRadarFixerRulesMixin.replayLastPacket();
                suppressBlocking = false;
            }
        }
    }

    public static void sendResetCode() {
        String resetCode = "§r§e§s§e§t§x§a§e§r§o";
        suppressBlocking = true;
        sendSystemMessage(resetCode);
        suppressBlocking = false;
    }

    private static void sendSystemMessage(String content) {
        Minecraft mc = Minecraft.getInstance();
        dev.gxlg.versiont.gen.ClientPacketListener conn = mc == null ? null : mc.getNetworkHandlerField();
        if (conn == null) return;
        lastSentCode = content;
        dev.gxlg.versiont.gen.ClientboundSystemChatPacket packet = new dev.gxlg.versiont.gen.ClientboundSystemChatPacket(dev.gxlg.versiont.gen.Component.literal(content), false);
        XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Sending radar code to self: {}", content);
        conn.onGameMessage(packet);
    }
}