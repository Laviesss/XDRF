package com.laviesss.xaerodisabledradarfixer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;
import net.minecraft.network.message.MessageType;

public class XaeroDisabledRadarFixer implements ClientModInitializer {

    private static final String[] BLOCKED_PATTERNS = {
        "\\u00a7f\\u00a7a\\u00a7i\\u00a7r\\u00a7x\\u00a7a\\u00a7e\\u00a7r\\u00a7o", // §f§a§i§r§x§a§e§r§o
        "\\u00a7x\\u00a7a\\u00a7e\\u00a7r\\u00a7o\\u00a7w\\u00a7m\\u00a7n\\u00a7e\\u00a7t\\u00a7h\\u00a7e\\u00a7r\\u00a7i\\u00a7s\\u00a7f\\u00a7a\\u00a7i\\u00a7r",
        "\\u00a7n\\u00a7o\\u00a7m\\u00a7i\\u00a7n\\u00a7i\\u00a7m\\u00a7a\\u00a7p",
        "\\u00a7r\\u00a7e\\u00a7s\\u00a7e\\u00a7t\\u00a7x\\u00a7a\\u00a7e\\u00a7r\\u00a7o"
    };

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.ALLOW.register((message, signedMessage, params, sender, receptionTimestamp) -> {
            String raw = message.getString();
            for (String blocked : BLOCKED_PATTERNS) {
                if (raw.contains(blocked)) {
                    return false; // Block the message
                }
            }
            return true; // Allow all other messages
        });
    }
}