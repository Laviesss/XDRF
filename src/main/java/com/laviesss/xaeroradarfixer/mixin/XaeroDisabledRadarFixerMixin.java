package com.laviesss.xaeroradarfixer.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class XaeroDisabledRadarFixerMixin {

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        // only block when in a world
        if (net.minecraft.client.MinecraftClient.getInstance().world == null) return;

        Text message = packet.content();
        String content = message.getString();

        if ( content.contains("§ƒ§ə§i§r§×§a§e§Ã§o") ||
                content.contains("§x§a§e§Ã§o§w§m§§§§§§r§i§§ƒ§ä§i§r") ||
                content.contains("§ƒ§ə§i§r§×§a§e§Ã§o §x§a§e§Ã§o§w§m§§§§§§r§i§§ƒ§ä§i§r") ||
                content.contains("§f§a§i§r§x§a§e§r§o §x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r")
        ) {
            ci.cancel();
        }
    }
}
