package com.laviesss.xaerodisabledradarfixer.mixin.impl;

import com.laviesss.xaerodisabledradarfixer.XaeroDisabledRadarFixer;
import com.laviesss.xaerodisabledradarfixer.config.Config;
import com.laviesss.xaerodisabledradarfixer.service.ReplayService;
import com.laviesss.xaerodisabledradarfixer.util.NotificationHelper;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;
import dev.gxlg.versiont.gen.net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

import java.util.Set;

public class ClientPacketListenerMixinImpl {
    private static final Set<String> BLOCKED_MESSAGES = Set.of("§f§a§i§r§x§a§e§r§o", "§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r", "§n§o§m§i§n§i§m§a§p");

    public static boolean handleSystemChat(ClientboundSystemChatPacket packet) {
        Config config = Config.get();
        if (!config.isEnabled()) {
            return false;
        }
        if (!config.shouldBlockMessages()) {
            return false;
        }
        if (ReplayService.isBlockingSuppressed()) {
            return false;
        }
        Component content = packet.content();
        String contentString = content.getString();
        boolean blocked = false;
        for (String block : BLOCKED_MESSAGES) {
            if (contentString.contains(block)) {
                blocked = true;
                break;
            }
        }
        if (!blocked) {
            return false;
        }
        ReplayService.recordBlockedChatPacket(packet);

        if (config.isVerboseLogging()) {
            XaeroDisabledRadarFixer.LOGGER.info("[XDRF] Blocked chat message content: {}", content.unwrap());
        }
        if (config.isShowChatMessage()) {
            NotificationHelper.sendMessageMessage();
        }
        if (config.isShowToast()) {
            NotificationHelper.showMessageToast();
        }
        if (config.isPlaySound()) {
            NotificationHelper.playSound();
        }
        return true;
    }
}
