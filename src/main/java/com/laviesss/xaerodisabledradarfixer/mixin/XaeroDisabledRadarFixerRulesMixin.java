package com.laviesss.xaerodisabledradarfixer.mixin;

import com.laviesss.xaerodisabledradarfixer.config.XaeroDisabledRadarFixerConfig;
import com.laviesss.xaerodisabledradarfixer.service.XaeroDisabledRadarFixerService;
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

import java.lang.reflect.Method;

/** Cancels server-enforced minimap rules via Xaero's plugin channel packet. */
@Mixin(targets = "xaero.hud.packet.basic.ClientboundRulesPacket$ClientHandler", remap = false)
public class XaeroDisabledRadarFixerRulesMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("XDRF-RulesMixin");

    private static Object storedPacket = null;
    private static Object storedHandler = null;

    @Inject(method = "accept(Lxaero/hud/packet/basic/ClientboundRulesPacket;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onAcceptRules(Object packet, CallbackInfo ci) {
        XaeroDisabledRadarFixerConfig cfg = XaeroDisabledRadarFixerConfig.get();
        if (!cfg.isEnabled() || !cfg.isBlockPacketRules()) return;

        // If we're replaying a previously blocked packet, let it through
        if (XaeroDisabledRadarFixerService.isBlockingSuppressed()) return;

        storedPacket = packet;
        storedHandler = this;

        cfg.incrementPacketBlockedCount();
        XaeroDisabledRadarFixerService.setLastBlockedType(XaeroDisabledRadarFixerService.LastBlockedType.RULES_PACKET);
        XaeroDisabledRadarFixerService.setLastSentCode("");

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

    /** Clears the stored packet so stale data is never replayed after a chat-code block. */
    public static void clearStoredPacket() {
        storedPacket = null;
        storedHandler = null;
    }

    /** Re-dispatches the last stored rules packet via reflection so it reaches Xaero's handler. */
    public static void replayLastPacket() {
        if (storedPacket == null || storedHandler == null) {
            LOGGER.warn("[XDRF] No stored packet to replay.");
            return;
        }
        try {
            Method accept = storedHandler.getClass().getMethod("accept", storedPacket.getClass());
            LOGGER.info("[XDRF] Replaying last blocked rules packet via {}", accept);
            accept.invoke(storedHandler, storedPacket);
        } catch (Exception e) {
            LOGGER.error("[XDRF] Failed to replay rules packet", e);
        }
    }
}
