package com.laviesss.xaerodisabledradarfixer.mixin;

import com.laviesss.xaerodisabledradarfixer.config.XaeroRadarFixerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class XaeroDisabledRadarFixerMixin {

    private static int blockCount = 0;

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (!XaeroRadarFixerConfig.enabled) {
            return;
        }

        String content = packet.content().getString();

        // Detection logic — excluding §r§e§s§e§t§x§a§e§r§o
        if (content.contains("§ƒ§ə§i§r§×§a§e§Ã§o") ||
                content.contains("§x§a§e§Ã§o§w§m§§§§§§r§i§§ƒ§ä§i§r") ||
                content.contains("§ƒ§ə§i§r§×§a§e§Ã§o §x§a§e§Ã§o§w§m§§§§§§r§i§§ƒ§ä§i§r") ||
                content.contains("§f§a§i§r§x§a§e§r§o §x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r") ||
                content.contains("§f§a§i§r§x§a§e§r§o") ||
                content.contains("§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r") ||
                content.contains("§n§o§m§i§n§i§m§a§p")) {

            blockCount++;

            MinecraftClient client = MinecraftClient.getInstance();

            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {}

                client.execute(() -> {
                    if (XaeroRadarFixerConfig.showChatMessage && client.player != null) {
                        client.player.sendMessage(
                                Text.literal("Radar Disabling Message Blocked! Blocked: " + blockCount + " times")
                                        .formatted(Formatting.DARK_PURPLE),
                                false
                        );
                    }

                    if (XaeroRadarFixerConfig.showToast) {
                        SystemToast.add(
                                client.getToastManager(),
                                SystemToast.Type.WORLD_BACKUP,
                                Text.literal("Xaero Radar Fixer"),
                                Text.literal("Blocked " + blockCount + " radar message(s)!")
                                        .formatted(Formatting.DARK_PURPLE)
                        );
                    }
                });
            }).start();

            ci.cancel();
        }
    }
}
