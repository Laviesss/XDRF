package com.laviesss.xaerodisabledradarfixer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.client.font.TextRenderer;

import java.util.concurrent.TimeUnit;

public class XaeroDisabledRadarFixerClientMod implements ClientModInitializer {

    private final MinecraftClient client = MinecraftClient.getInstance();

    @Override
    public void onInitializeClient() {
        // Register the listener for the GameMessageS2CPacket
        ClientPlayNetworking.registerGlobalReceiver(GameMessageS2CPacket.class, (client, handler, packet, responseSender) -> {
            // Get the message content
            Text message = packet.getMessage();

            // Check if the message matches any of the patterns to block it
            if (message.getString().contains("§ƒ§ə§i§r§×§a§e§Ã§o") || message.getString().contains("§x§a§e§Ã§o§w§m§§§§§§r§i§§ƒ§ä§i§r") || message.getString().contains("§ƒ§ə§i§r§×§a§e§Ã§o §x§a§e§Ã§o§w§m§§§§§§r§i§§ƒ§ä§i§r")) {
                // Block the message from being processed or shown
                return;
            }

            // Otherwise, allow the message to be processed normally
            client.execute(() -> {
                handler.handleGameMessage(packet);
            });

            // Wait for 1 second and then check if the message is still visible in the chat
            client.execute(() -> {
                client.player.sendMessage(new LiteralText("Waiting to verify..."));

                // Introduce a delay (1 second) before checking if the message appeared in chat
                client.executeLater(() -> {
                    // After 1 second, check the chat for the specific message pattern
                    boolean messageFound = false;
                    for (String line : client.inGameHud.getChatHud().getMessages()) {
                        if (line.contains("§ƒ§ə§i§r§×§a§e§Ã§o") || line.contains("§x§a§e§Ã§o§w§m§§§§§§r§i§§ƒ§ä§i§r") || line.contains("§ƒ§ə§i§r§×§a§e§Ã§o §x§a§e§Ã§o§w§m§§§§§§r§i§§ƒ§ä§i§r")) {
                            messageFound = true;
                            break;
                        }
                    }

                    // If the message was found in the chat, notify "Mod Failed"
                    if (messageFound) {
                        sendModResultMessage("Mod Failed");
                    } else {
                        // If the message was not found in the chat, notify "Mod Worked"
                        sendModResultMessage("Mod Worked");
                    }
                }, 1, TimeUnit.SECONDS);
            });
        });
    }

    private void sendModResultMessage(String result) {
        // Send a confirmation message in purple text
        Text resultMessage = new LiteralText(result).styled(style -> style.withColor(TextColor.fromRgb(0x800080))); // Purple color
        client.player.sendMessage(resultMessage, false);
    }
}
