package com.laviesss.xaerodisabledradarfixer.mixin;

import com.laviesss.xaerodisabledradarfixer.config.XaeroDisabledRadarFixerConfig;
import com.laviesss.xaerodisabledradarfixer.service.XaeroDisabledRadarFixerService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class XaeroDisabledRadarFixerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("XDRF-Mixin");

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        XaeroDisabledRadarFixerConfig cfg = XaeroDisabledRadarFixerConfig.get();
        if (!cfg.isEnabled()) return;
        if (XaeroDisabledRadarFixerService.isBlockingSuppressed()) return;

        String content = packet.content().getString();

        // These are the ONLY patterns that should be blocked
        boolean shouldBlock = content.equals("§f§a§i§r§x§a§e§r§o")
                || content.equals("§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r")
                || content.equals("§n§o§m§i§n§i§m§a§p");

        if (shouldBlock) {
            XaeroDisabledRadarFixerService.setLastSentCode(content);

            LOGGER.info("[XDRF] Intercepted and blocked radar-disable message: {}", content);

            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;

            if (cfg.isShowChatMessage() && player != null) {
                player.sendMessage(Text.literal("A radar blocking message was prevented.").formatted(Formatting.DARK_PURPLE), false);
            }

            if (cfg.isShowToast()) {
                SystemToast.add(
                        client.getToastManager(),
                        SystemToast.Type.WORLD_BACKUP,
                        Text.literal("Xaero Disabled Radar Fixer"),
                        Text.literal("Blocked a radar-disabling message.").formatted(Formatting.DARK_PURPLE)
                );
            }

            ci.cancel();
        }
    }
}
