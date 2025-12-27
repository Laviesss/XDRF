package com.laviesss.xaerodisabledradarfixer.service;

import com.laviesss.xaerodisabledradarfixer.config.XaeroDisabledRadarFixerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class XaeroDisabledRadarFixerService {

    private static final Logger LOGGER = LoggerFactory.getLogger("XDRF-Service");
    private static String lastSentCode = "";
    private static boolean suppressBlocking = false;

    public static void processMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        XaeroDisabledRadarFixerConfig cfg = XaeroDisabledRadarFixerConfig.get();
        if (!cfg.isEnabled() || isBlockingSuppressed()) {
            return;
        }

        String content = packet.content().getString();

        for (String pattern : cfg.getBlockingPatterns()) {
            if (content.contains(pattern)) {
                ci.cancel();

                setLastSentCode(content);
                LOGGER.info("[XDRF] Intercepted and blocked radar-disable message: {}", content);

                MinecraftClient client = MinecraftClient.getInstance();
                ClientPlayerEntity player = client.player;

                if (cfg.isShowChatMessage() && player != null) {
                    String message = "A radar blocking message was prevented.";
                    if (cfg.isShowBlockedMessage()) {
                        message += " Message: " + content;
                    }
                    player.sendMessage(Text.literal(message).formatted(Formatting.DARK_PURPLE), false);
                }

                if (cfg.isShowToast()) {
                    SystemToast.add(
                            client.getToastManager(),
                            SystemToast.Type.WORLD_BACKUP,
                            Text.literal("Xaero Disabled Radar Fixer"),
                            Text.literal("Blocked a radar-disabling message.").formatted(Formatting.DARK_PURPLE)
                    );
                }
                return;
            }
        }
    }

    public static void setLastSentCode(String code) {
        lastSentCode = code;
    }

    public static String getLastSentCode() {
        return lastSentCode;
    }

    public static boolean isBlockingSuppressed() {
        return suppressBlocking;
    }

    public static void resendLastBlockedCode() {
        if (!lastSentCode.isEmpty()) {
            suppressBlocking = true;
            sendSystemMessage(lastSentCode);
            suppressBlocking = false;
        }
    }

    public static void sendResetCode() {
        String resetCode = "§r§e§s§e§t§x§a§e§r§o";
        suppressBlocking = true;
        sendSystemMessage(resetCode);
        suppressBlocking = false;
    }

    private static void sendSystemMessage(String content) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            lastSentCode = content;
            GameMessageS2CPacket packet = new GameMessageS2CPacket(Text.literal(content), false);
            LOGGER.info("[XDRF] Sending radar code to self: {}", content);
            client.getNetworkHandler().onGameMessage(packet);
        }
    }
}
