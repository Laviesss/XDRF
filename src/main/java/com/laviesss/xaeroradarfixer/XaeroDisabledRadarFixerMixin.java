package com.laviesss.xaerodisabledradarfixer.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class XaeroDisabledRadarFixerMixin {

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    public void onGameMessage(GameMessageS2CPacket packet, CallbackInfo info) {
        Text message = packet.getMessage(); // Ensure this method is valid for the version you're working with

        // Check the message content and prevent it from being processed if it matches specific patterns
        if (message.getString().contains("§ƒ§ə§i§r§×§a§e§Ã§o") || 
            message.getString().contains("§x§a§e§Ã§o§w§m§§§§§§r§i§§ƒ§ä§i§r") || 
            message.getString().contains("§ƒ§ə§i§r§×§a§e§Ã§o §x§a§e§Ã§o§w§m§§§§§§r§i§§ƒ§ä§i§r")) {
            // Cancel the message if it matches
            info.cancel();
        }
    }
}
