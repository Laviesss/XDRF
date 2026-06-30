package com.laviesss.xaerodisabledradarfixer.service;

import com.laviesss.xaerodisabledradarfixer.config.XaeroDisabledRadarFixerConfig;
import com.laviesss.xaerodisabledradarfixer.mixin.XaeroDisabledRadarFixerRulesMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XaeroDisabledRadarFixerService {
    private static final Logger LOGGER = LoggerFactory.getLogger("XDRF-Service");

    public enum LastBlockedType { CHAT_CODE, RULES_PACKET }

    private static String lastSentCode = "";
    private static boolean suppressBlocking = false;
    private static LastBlockedType lastBlockedType = LastBlockedType.CHAT_CODE;

    public static void setLastSentCode(String code) {
        lastSentCode = code;
    }

    public static String getLastSentCode() {
        return lastSentCode;
    }

    public static boolean isBlockingSuppressed() {
        return suppressBlocking;
    }

    public static LastBlockedType getLastBlockedType() {
        return lastBlockedType;
    }

    public static void setLastBlockedType(LastBlockedType type) {
        lastBlockedType = type;
    }

    public static void enforceBlocking() {
        switch (lastBlockedType) {
            case CHAT_CODE:
                if (!lastSentCode.isEmpty()) {
                    suppressBlocking = true;
                    sendSystemMessage(lastSentCode);
                    suppressBlocking = false;
                }
                break;
            case RULES_PACKET:
                suppressBlocking = true;
                XaeroDisabledRadarFixerRulesMixin.replayLastPacket();
                suppressBlocking = false;
                break;
        }
    }

    public static void sendResetCode() {
        String resetCode = "§r§e§s§e§t§x§a§e§r§o";
        suppressBlocking = true;
        sendSystemMessage(resetCode);
        suppressBlocking = false;
    }

    private static void sendSystemMessage(String content) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            lastSentCode = content;
            GameMessageS2CPacket packet = new GameMessageS2CPacket(Text.literal(content), false);
            LOGGER.info("[XDRF] Sending radar code to self: {}", content);
            client.getNetworkHandler().onGameMessage(packet);
        }
    }
}
