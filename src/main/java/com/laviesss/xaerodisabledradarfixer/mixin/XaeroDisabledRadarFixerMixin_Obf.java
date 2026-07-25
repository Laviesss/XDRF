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
        compare = { @Compare(version = "1.21", comparison = Comparison.NOT_LOWER) },
        obfuscated = true
)
@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(targets = "net.minecraft.class_634", remap = false)
public class XaeroDisabledRadarFixerMixin_Obf {

    private static final java.util.Set<String> BLOCKED_CODES = java.util.Set.of(
            "§f§a§i§r§x§a§e§r§o",
            "§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r",
            "§n§o§m§i§n§i§m§a§p"
    );

    @Inject(method = "method_43596", at = @At("HEAD"), cancellable = true)
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
                player.displayClientMessage(
                        XaeroDisabledRadarFixerTextHelper.formatted(
                                XaeroDisabledRadarFixerTextHelper.literal("[XDRF] A radar blocking message was prevented."),
                                ChatFormatting.DARK_PURPLE()
                        ),
                        false
                );
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