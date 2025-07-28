package com.laviesss.xaerodisabledradarfixer.mixin;

import com.laviesss.xaerodisabledradarfixer.config.XaeroDisabledRadarFixerConfig;
import com.laviesss.xaerodisabledradarfixer.service.XaeroDisabledRadarFixerService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.SystemToast.Type;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class XaeroDisabledRadarFixerMixin {

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (!XaeroDisabledRadarFixerConfig.enabled) return;

        String content = packet.content().getString();
        if (content.contains("§ƒ§ə§i§r§×§a§e§Ã§o") ||
                content.contains("§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§") ||
                content.contains("§n§o§m§i§n§i§m§a§p")) {

            XaeroDisabledRadarFixerService.setLastBlockedCode(content);
            MinecraftClient client = MinecraftClient.getInstance();

            new Thread(() -> {
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                client.execute(() -> {
                    ClientPlayerEntity player = client.player;
                    if (XaeroDisabledRadarFixerConfig.showChatMessage && player != null) {
                        player.sendMessage(Text.literal("A radar blocking message was prevented.").formatted(Formatting.DARK_PURPLE), false);
                    }
                    if (XaeroDisabledRadarFixerConfig.showToast) {
                        SystemToast.add(
                                client.getToastManager(),
                                Type.WORLD_BACKUP,
                                Text.literal("Xaero Disabled Radar Fixer"),
                                Text.literal("A radar blocking message was blocked.").formatted(Formatting.DARK_PURPLE)
                        );
                    }
                });
            }).start();

            ci.cancel();
        }
    }
}
