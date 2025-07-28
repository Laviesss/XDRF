package com.laviesss.xaerodisabledradarfixer.service;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XaeroDisabledRadarFixerService {
    private static final Logger LOGGER = LoggerFactory.getLogger("XDRF-Service");
    private static String lastSentCode = "";

    public static void setLastSentCode(String code) {
        lastSentCode = code;
    }

    public static String getLastSentCode() {
        return lastSentCode;
    }

    public static void resendLastBlockedCode() {
        if (!lastSentCode.isEmpty()) {
            sendSystemMessage(lastSentCode);
        }
    }

    public static void sendResetCode() {
        String resetCode = "§r§e§s§e§t§x§a§e§r§o";
        sendSystemMessage(resetCode);
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
