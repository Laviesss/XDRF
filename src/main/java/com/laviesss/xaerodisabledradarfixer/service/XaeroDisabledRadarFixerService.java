package com.laviesss.xaerodisabledradarfixer.service;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class XaeroDisabledRadarFixerService {
    private static String lastBlockedCode = "";

    public static void setLastBlockedCode(String code) {
        lastBlockedCode = code;
    }

    public static void resendLastBlockedCode() {
        if (!lastBlockedCode.isEmpty()) {
            sendSystemMessage(lastBlockedCode);
        }
    }

    public static void sendResetCode() {
        String resetCode = "§n§o§m§i§n§i§m§a§p";
        sendSystemMessage(resetCode);
    }

    private static void sendSystemMessage(String content) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            GameMessageS2CPacket packet = new GameMessageS2CPacket(
                    Text.literal(content),
                    false
            );
            client.getNetworkHandler().onGameMessage(packet);
        }
    }
}
