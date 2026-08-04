package com.laviesss.xaerodisabledradarfixer.service;

import com.laviesss.xaerodisabledradarfixer.XaeroDisabledRadarFixer;
import dev.gxlg.versiont.gen.net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents$DisconnectI;
import dev.gxlg.versiont.gen.net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents$JoinI;
import dev.gxlg.versiont.gen.net.minecraft.client.multiplayer.ClientPacketListener;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;
import dev.gxlg.versiont.gen.net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import dev.gxlg.versiont.gen.xaero.hud.packet.basic.ClientboundRulesPacket;
import dev.gxlg.versiont.gen.xaero.hud.packet.basic.ClientboundRulesPacket$ClientHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class ReplayService {

    private static ClientboundSystemChatPacket blockedChatPacket = null;

    private static XaeroPacket blockedXaeroPacket = null;

    private static ClientPacketListener packetListener = null;

    private static boolean suppressBlocking = false;

    public static void init() {
        ClientPlayConnectionEvents$JoinI join = (listener, s, m) -> {
            packetListener = listener;
            blockedChatPacket = null;
            blockedXaeroPacket = null;
            XaeroDisabledRadarFixer.LOGGER.info("[XDRF] Session started - cache cleared.");
        };
        ClientPlayConnectionEvents.JOIN.register(join.unwrap(ClientPlayConnectionEvents.Join.class));

        ClientPlayConnectionEvents$DisconnectI disconnect = (l, m) -> {
            packetListener = null;
            blockedChatPacket = null;
            blockedXaeroPacket = null;
            XaeroDisabledRadarFixer.LOGGER.info("[XDRF] Session ended - cache cleared.");
        };
        ClientPlayConnectionEvents.DISCONNECT.register(disconnect.unwrap(ClientPlayConnectionEvents.Disconnect.class));
    }


    public static boolean isBlockingSuppressed() {
        return suppressBlocking;
    }

    public static void enforce() {
        if (packetListener == null) {
            return;
        }
        suppressBlocking = true;
        if (blockedChatPacket != null) {
            packetListener.handleSystemChat(blockedChatPacket);
            XaeroDisabledRadarFixer.LOGGER.info("[XDRF] Sent original block code.");
        }
        if (blockedXaeroPacket != null) {
            blockedXaeroPacket.handler().accept(blockedXaeroPacket.packet());
            XaeroDisabledRadarFixer.LOGGER.info("[XDRF] Sent original rules packet.");
        }
        suppressBlocking = false;
    }

    public static void revoke() {
        if (packetListener == null) {
            return;
        }
        suppressBlocking = true;
        if (blockedChatPacket != null) {
            ClientboundSystemChatPacket packet = new ClientboundSystemChatPacket(Component.nullToEmpty("§r§e§s§e§t§x§a§e§r§o"), false);
            packetListener.handleSystemChat(packet);
            XaeroDisabledRadarFixer.LOGGER.info("[XDRF] Sent reset code.");
        }
        if (blockedXaeroPacket != null) {
            ClientboundRulesPacket packet = new ClientboundRulesPacket(true, true, true);
            blockedXaeroPacket.handler().accept(packet);
            XaeroDisabledRadarFixer.LOGGER.info("[XDRF] Sent modified rules packet.");
        }
        suppressBlocking = false;
    }

    public static void recordBlockedChatPacket(ClientboundSystemChatPacket packet) {
        blockedChatPacket = packet;
        XaeroDisabledRadarFixer.LOGGER.info("[XDRF] Recorded blocked chat packet.");
    }

    public static void recordBlockedXaeroPacket(ClientboundRulesPacket packet, ClientboundRulesPacket$ClientHandler handler) {
        blockedXaeroPacket = new XaeroPacket(packet, handler);
        XaeroDisabledRadarFixer.LOGGER.info("[XDRF] Recorded blocked rules packet.");
    }

    private record XaeroPacket(ClientboundRulesPacket packet, ClientboundRulesPacket$ClientHandler handler) {

    }
}