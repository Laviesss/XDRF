package com.laviesss.xaerodisabledradarfixer.mixin;

import com.laviesss.xaerodisabledradarfixer.service.XaeroDisabledRadarFixerService;
import dev.gxlg.versiont.api.R;
import dev.gxlg.versiont.gen.Minecraft;
import dev.gxlg.versiont.gen.LocalPlayer;
import dev.gxlg.versiont.gen.Component;
import dev.gxlg.versiont.gen.ChatFormatting;
import dev.gxlg.versiont.gen.SoundEvents;
import dev.gxlg.versiont.gen.SystemToast;
import dev.gxlg.versiont.gen.ToastManager;
import dev.gxlg.versiont.gen.ClientboundSystemChatPacket;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class XaeroDisabledRadarFixerMixin {

    private static final java.util.Set<String> BLOCKED_CODES = java.util.Set.of(
        "§f§a§i§r§x§a§e§r§o",
        "§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r",
        "§n§o§m§i§n§i§m§a§p"
    );

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (!XaeroDisabledRadarFixerService.shouldBlockChatMessage(packet)) {
            return;
        }

        XaeroDisabledRadarFixerService.recordBlockedChatMessage(packet);

        if (XaeroDisabledRadarFixerService.shouldShowChatMessage()) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.getPlayerField();
            if (player != null) {
                player.sendMessage(
                    Component.literal("[XDRF] A radar blocking message was prevented.")
                        .formatted(ChatFormatting.DARK_PURPLE()),
                    false
                );
            }
        }

        if (XaeroDisabledRadarFixerService.shouldShowToast()) {
            Minecraft mc = Minecraft.getInstance();
            ToastManager tm = (ToastManager) R.clz(Minecraft.class).inst(mc.unwrap())
                .fld("toastManager", ToastManager.class).get();
            if (tm != null) {
                SystemToast.add(tm, null,
                    Component.literal("Radar Blocker").formatted(ChatFormatting.DARK_PURPLE()),
                    Component.literal("Blocked a radar-disabling message.").formatted(ChatFormatting.DARK_PURPLE())
                );
            }
        }

        if (XaeroDisabledRadarFixerService.shouldPlaySound()) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.getPlayerField();
            if (player != null) {
                player.playSound(SoundEvents.ENTITY_VILLAGER_CELEBRATE(), 1.0F, 1.0F);
            }
        }

        ci.cancel();
    }
}