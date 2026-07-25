package com.laviesss.xaerodisabledradarfixer.mixin;

import com.laviesss.xaerodisabledradarfixer.XaeroDisabledRadarFixerClientMod;
import com.laviesss.xaerodisabledradarfixer.config.XaeroDisabledRadarFixerConfig;
import com.laviesss.xaerodisabledradarfixer.service.XaeroDisabledRadarFixerService;
import com.laviesss.xaerodisabledradarfixer.util.XaeroDisabledRadarFixerTextHelper;
import com.laviesss.xaerodisabledradarfixer.util.XaeroDisabledRadarFixerToastHelper;
import dev.gxlg.versiont.api.R;
import dev.gxlg.versiont.api.V;
import dev.gxlg.versiont.gen.net.minecraft.client.Minecraft;
import dev.gxlg.versiont.gen.net.minecraft.client.player.LocalPlayer;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;
import dev.gxlg.versiont.gen.net.minecraft.ChatFormatting;
import dev.gxlg.versiont.gen.net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import dev.gxlg.versiont.mixins.Compare;
import dev.gxlg.versiont.mixins.Comparison;
import dev.gxlg.versiont.mixins.VersiontMixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@VersiontMixin(
        compare = { @Compare(version = "26.1", comparison = Comparison.NOT_LOWER) },
        obfuscated = false
)
@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(targets = "net.minecraft.client.multiplayer.ClientPacketListener", remap = false)
public class XaeroDisabledRadarFixerMixin_Deobf {

    private static final java.util.Set<String> BLOCKED_CODES = java.util.Set.of(
            "§f§a§i§r§x§a§e§r§o",
            "§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r",
            "§n§o§m§i§n§i§m§a§p"
    );

    @Inject(method = "handleSystemChat", at = @At("HEAD"), cancellable = true)   // ✅ correct method name
    private void onGameMessage(@Coerce Object packet, CallbackInfo ci) {
        ClientboundSystemChatPacket wrappedPacket = R.wrapperInst(ClientboundSystemChatPacket.class, packet);
        if (!XaeroDisabledRadarFixerService.shouldBlockChatMessage(wrappedPacket)) {
            return;
        }

        XaeroDisabledRadarFixerService.recordBlockedChatMessage(wrappedPacket);

        if (XaeroDisabledRadarFixerConfig.get().isVerboseLogging()) {
            try {
                Component content = wrappedPacket.content();
                XaeroDisabledRadarFixerClientMod.LOGGER.info("[XDRF] Blocked chat message content: {}", content.unwrap());
            } catch (Exception e) {
                XaeroDisabledRadarFixerClientMod.LOGGER.warn("[XDRF] Could not log chat message content", e);
            }
        }

        if (XaeroDisabledRadarFixerService.shouldShowChatMessage()) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.getPlayerField();
            if (player != null) {
                Component chatLine = XaeroDisabledRadarFixerTextHelper.formatted(
                        XaeroDisabledRadarFixerTextHelper.literal("[XDRF] A radar blocking message was prevented."),
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
        }

        // Toast support via XaeroDisabledRadarFixerToastHelper — handles 1.21 and 26.x
        if (XaeroDisabledRadarFixerService.shouldShowToast()) {
            XaeroDisabledRadarFixerToastHelper.showToast(
                    XaeroDisabledRadarFixerTextHelper.formatted(
                            XaeroDisabledRadarFixerTextHelper.literal("🗺️ Radar Blocker"),
                            ChatFormatting.DARK_PURPLE()
                    ),
                    XaeroDisabledRadarFixerTextHelper.formatted(
                            XaeroDisabledRadarFixerTextHelper.literal("Blocked a radar-disabling message."),
                            ChatFormatting.DARK_PURPLE()
                    )
            );
        }

        ci.cancel();
    }
}