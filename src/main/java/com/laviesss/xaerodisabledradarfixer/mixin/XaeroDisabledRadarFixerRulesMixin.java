package com.laviesss.xaerodisabledradarfixer.mixin;

import com.laviesss.xaerodisabledradarfixer.XaeroDisabledRadarFixerClientMod;
import com.laviesss.xaerodisabledradarfixer.config.XaeroDisabledRadarFixerConfig;
import com.laviesss.xaerodisabledradarfixer.service.XaeroDisabledRadarFixerService;
import com.laviesss.xaerodisabledradarfixer.util.XaeroDisabledRadarFixerPacketHelper;
import com.laviesss.xaerodisabledradarfixer.util.XaeroDisabledRadarFixerTextHelper;
import com.laviesss.xaerodisabledradarfixer.util.XaeroDisabledRadarFixerToastHelper;
import dev.gxlg.versiont.api.R;
import dev.gxlg.versiont.api.V;

import dev.gxlg.versiont.gen.net.minecraft.client.Minecraft;
import dev.gxlg.versiont.gen.net.minecraft.client.player.LocalPlayer;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;
import dev.gxlg.versiont.gen.net.minecraft.ChatFormatting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "xaero.hud.packet.basic.ClientboundRulesPacket$ClientHandler", remap = false)
public class XaeroDisabledRadarFixerRulesMixin {

    private void logPacketDetails(Object packet) {
        if (!XaeroDisabledRadarFixerConfig.get().isVerboseLogging()) return;
        try {
            Class<?> clazz = packet.getClass();
            XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] === Packet Details ===");
            XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Packet class: {}", clazz.getName());

            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(packet);
                    XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF]   Field: {} = {}", field.getName(), value);
                } catch (Exception e) {
                    XaeroDisabledRadarFixerClientMod.LOGGER.warn("[XDRF]   Could not read field: {}", field.getName());
                }
            }

            for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 &&
                        method.getName().startsWith("get") &&
                        !method.getName().equals("getClass")) {
                    method.setAccessible(true);
                    try {
                        Object value = method.invoke(packet);
                        XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF]   Method: {}() = {}", method.getName(), value);
                    } catch (Exception e) {
                        XaeroDisabledRadarFixerClientMod.LOGGER.warn("[XDRF]   Could not invoke method: {}", method.getName());
                    }
                }
            }
            XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] === End Packet Details ===");
        } catch (Exception e) {
            XaeroDisabledRadarFixerClientMod.LOGGER.error("[XDRF] Failed to log packet details", e);
        }
    }

    @Inject(
            method = "accept",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void onAcceptRules(@Coerce Object packet, CallbackInfo ci) {
        XaeroDisabledRadarFixerConfig cfg = XaeroDisabledRadarFixerConfig.get();
        if (!cfg.isEnabled() || !cfg.isBlockPacketRules()) return;
        if (XaeroDisabledRadarFixerService.isBlockingSuppressed()) return;

        // ── Store for session cache ──────────────────────────────
        XaeroDisabledRadarFixerPacketHelper.setStored(packet, this);
        XaeroDisabledRadarFixerService.recordBlockedPacket(packet);

        // ── Verbose logging ──────────────────────────────────────
        logPacketDetails(packet);

        cfg.incrementPacketBlockedCount();
        XaeroDisabledRadarFixerService.setLastSentCode("");

        XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Blocked a server-enforced minimap rules packet (radar/cave-mode). Total: {}", cfg.getPacketBlockedCount());

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.getPlayerField();

        if (cfg.isShowChatMessage() && player != null) {
            Component chatLine = XaeroDisabledRadarFixerTextHelper.formatted(
                    XaeroDisabledRadarFixerTextHelper.literal("A server-enforced minimap rules packet was prevented."),
                    ChatFormatting.DARK_PURPLE()
            );
            if (V.isObfuscated()) {
                // 1.21 – 1.21.x: displayClientMessage(Component, boolean) — overlay=false sends to chat
                player.displayClientMessage(chatLine, false);
            } else {
                // 26.x: sendSystemMessage sends to chat, sendOverlayMessage to the action-bar/overlay
                player.sendSystemMessage(chatLine);
            }
        }

        // Toast support via XaeroDisabledRadarFixerToastHelper — handles 1.21 and 26.x
        if (cfg.isShowToast()) {
            XaeroDisabledRadarFixerToastHelper.showToast(
                    XaeroDisabledRadarFixerTextHelper.formatted(
                            XaeroDisabledRadarFixerTextHelper.literal("🗺️ Xaero Disabled Radar Fixer"),
                            ChatFormatting.DARK_PURPLE()
                    ),
                    XaeroDisabledRadarFixerTextHelper.formatted(
                            XaeroDisabledRadarFixerTextHelper.literal("Blocked a server-enforced minimap rules packet."),
                            ChatFormatting.DARK_PURPLE()
                    )
            );
        }

        // Sound disabled – no valid SoundEvents mapping in Yarn 1.21
        /*
        if (cfg.isPlaySound() && player != null) {
            player.playSound(SoundEvents.ENTITY_VILLAGER_CELEBRATE(), 1.0F, 1.0F);
        }
        */

        ci.cancel();
    }
}