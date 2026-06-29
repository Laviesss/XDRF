package com.laviesss.xaerodisabledradarfixer.mixin;

import com.laviesss.xaerodisabledradarfixer.config.XaeroDisabledRadarFixerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Cancels server-enforced minimap rules via Xaero's plugin channel packet. */
@Mixin(targets = "xaero.hud.packet.basic.ClientboundRulesPacket$ClientHandler", remap = false)
public class XaeroDisabledRadarFixerRulesMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("XDRF-RulesMixin");

    @Inject(method = "accept(Lxaero/hud/packet/basic/ClientboundRulesPacket;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onAcceptRules(CallbackInfo ci) {
        XaeroDisabledRadarFixerConfig cfg = XaeroDisabledRadarFixerConfig.get();
        if (!cfg.isEnabled() || !cfg.isBlockPacketRules()) return;

        cfg.incrementPacketBlockedCount();
        LOGGER.info("[XDRF] Blocked a server-enforced minimap rules packet (radar/cave-mode). Total: {}", cfg.getPacketBlockedCount());

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (cfg.isShowChatMessage() && player != null) {
            player.sendMessage(
                Text.literal("A server-enforced minimap rules packet was prevented.")
                    .formatted(Formatting.DARK_PURPLE),
                false
            );
        }

        if (cfg.isShowToast()) {
            SystemToast.add(
                client.getToastManager(),
                SystemToast.Type.WORLD_BACKUP,
                Text.literal("Xaero Disabled Radar Fixer"),
                Text.literal("Blocked a server-enforced minimap rules packet.").formatted(Formatting.DARK_PURPLE)
            );
        }

        if (cfg.isPlaySound() && player != null) {
            player.playSound(SoundEvents.ENTITY_VILLAGER_CELEBRATE, 1.0F, 1.0F);
        }

        ci.cancel();
    }
}
