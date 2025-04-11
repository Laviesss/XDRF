package com.laviesss.xaeroradarfixer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;

public class XaeroRadarFixer implements ClientModInitializer {

    private static final String[] BLOCKED_PATTERNS = {
        "\u00a7f\u00a7a\u00a7i\u00a7r\u00a7x\u00a7a\u00a7e\u00a7r\u00a7o", // §f§a§i§r§x§a§e§r§o
        "\u00a7x\u00a7a\u00a7e\u00a7r\u00a7o\u00a7w\u00a7m\u00a7n\u00a7e\u00a7t\u00a7h\u00a7e\u00a7r\u00a7i\u00a7s\u00a7f\u00a7a\u00a7i\u00a7r",
        "\u00a7n\u00a7o\u00a7m\u00a7i\u00a7n\u00a7i\u00a7m\u00a7a\u00a7p",
        "\u00a7r\u00a7e\u00a7s\u00a7e\u00a7t\u00a7x\u00a7a\u00a7e\u00a7r\u00a7o"
    };

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ChatMessageS2CPacket.class, (client, handler, packet, sender) -> {
            String rawMessage = packet.getMessage().getString();
            for (String blockedPattern : BLOCKED_PATTERNS) {
                if (rawMessage.contains(blockedPattern)) {
                    // Block the message
                    return;
                }
            }
            // Otherwise, display the message (allow it)
            MinecraftClient.getInstance().inGameHud.addChatMessage(packet.getType(), packet.getMessage());
        });
    }
}