package com.laviesss.xaerodisabledradarfixer.mixin;

import com.laviesss.xaerodisabledradarfixer.XaeroDisabledRadarFixerClientMod;
import com.laviesss.xaerodisabledradarfixer.config.XaeroDisabledRadarFixerConfig;
import com.laviesss.xaerodisabledradarfixer.service.XaeroDisabledRadarFixerService;
import dev.gxlg.versiont.api.R;
import dev.gxlg.versiont.gen.Minecraft;
import dev.gxlg.versiont.gen.LocalPlayer;
import dev.gxlg.versiont.gen.Component;
import dev.gxlg.versiont.gen.ChatFormatting;
import dev.gxlg.versiont.gen.SoundEvents;
import dev.gxlg.versiont.gen.SystemToast;
import dev.gxlg.versiont.gen.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

@Mixin(targets = "xaero.hud.packet.basic.ClientboundRulesPacket$ClientHandler", remap = false)
public class XaeroDisabledRadarFixerRulesMixin {

    private static Object storedPacket = null;
    private static Object storedHandler = null;

    @Inject(
        method = "accept",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void onAcceptRules(Object packet, CallbackInfo ci) {
        XaeroDisabledRadarFixerConfig cfg = XaeroDisabledRadarFixerConfig.get();
        if (!cfg.isEnabled() || !cfg.isBlockPacketRules()) return;
        if (XaeroDisabledRadarFixerService.isBlockingSuppressed()) return;

        storedPacket = packet;
        storedHandler = this;
        cfg.incrementPacketBlockedCount();
        XaeroDisabledRadarFixerService.setLastBlockedType(XaeroDisabledRadarFixerService.LastBlockedType.RULES_PACKET);
        XaeroDisabledRadarFixerService.setLastSentCode("");

        XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Blocked a server-enforced minimap rules packet (radar/cave-mode). Total: {}", cfg.getPacketBlockedCount());

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.getPlayerField();

        if (cfg.isShowChatMessage() && player != null) {
            player.sendMessage(
                Component.literal("A server-enforced minimap rules packet was prevented.")
                    .formatted(ChatFormatting.DARK_PURPLE()),
                false
            );
        }

        if (cfg.isShowToast()) {
            ToastManager tm = (ToastManager) R.clz(Minecraft.class).inst(mc.unwrap())
                .fld("toastManager", ToastManager.class).get();
            if (tm != null) {
                SystemToast.add(tm, null,
                    Component.literal("Xaero Disabled Radar Fixer").formatted(ChatFormatting.DARK_PURPLE()),
                    Component.literal("Blocked a server-enforced minimap rules packet.").formatted(ChatFormatting.DARK_PURPLE())
                );
            }
        }

        if (cfg.isPlaySound() && player != null) {
            player.playSound(SoundEvents.ENTITY_VILLAGER_CELEBRATE(), 1.0F, 1.0F);
        }

        ci.cancel();
    }

    public static void clearStoredPacket() {
        storedPacket = null;
        storedHandler = null;
    }

    public static void replayLastPacket() {
        if (storedPacket == null || storedHandler == null) {
            XaeroDisabledRadarFixerClientMod.LOGGER.warn("[XDRF] No stored packet to replay.");
            return;
        }
        try {
            Method accept = storedHandler.getClass().getMethod("accept", storedPacket.getClass());
            XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Replaying last blocked rules packet via {}", accept);
            accept.invoke(storedHandler, storedPacket);
        } catch (Exception e) {
            XaeroDisabledRadarFixerClientMod.LOGGER.error("[XDRF] Failed to replay rules packet", e);
        }
    }
}