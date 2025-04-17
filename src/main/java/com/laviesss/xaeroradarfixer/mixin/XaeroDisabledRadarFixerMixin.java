package com.laviesss.xaeroradarfixer.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class XaeroDisabledRadarFixerMixin {

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        Text message = packet.content();
        String content = message.getString();

        // Check for specific patterns in the message content
        if (content.contains("§ƒ§ə§i§r§×§a§e§Ã§o") ||
                content.contains("§x§a§e§Ã§o§w§m§§§§§§r§i§§ƒ§ä§i§r") ||
                content.contains("§ƒ§ə§i§r§×§a§e§Ã§o §x§a§e§Ã§o§w§m§§§§§§r§i§§ƒ§ä§i§r")) {
            ci.cancel(); // Block the message if it matches any of the patterns
        }
    }
}
