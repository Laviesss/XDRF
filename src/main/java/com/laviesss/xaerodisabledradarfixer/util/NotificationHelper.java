package com.laviesss.xaerodisabledradarfixer.util;

import com.laviesss.xaerodisabledradarfixer.util.chaining.gui.Gui;
import com.laviesss.xaerodisabledradarfixer.util.chaining.player.Player;
import dev.gxlg.versiont.gen.net.minecraft.ChatFormatting;
import dev.gxlg.versiont.gen.net.minecraft.client.Minecraft;
import dev.gxlg.versiont.gen.net.minecraft.client.multiplayer.ClientLevel;
import dev.gxlg.versiont.gen.net.minecraft.client.player.LocalPlayer;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;
import dev.gxlg.versiont.gen.net.minecraft.sounds.SoundEvents;
import dev.gxlg.versiont.gen.net.minecraft.sounds.SoundSource;

public class NotificationHelper {
    public static void playSound() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.getPlayerField();
        ClientLevel level = minecraft.getLevelField();
        if (player == null || level == null) {
            return;
        }
        level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.VILLAGER_CELEBRATE(), SoundSource.NEUTRAL(), 1.0F, 1.0F, false);
    }

    public static void showPacketToast() {
        Gui.showToast(
            Component.nullToEmpty("🗺️ Xaero Disabled Radar Fixer").plainCopy().withStyle(ChatFormatting.DARK_PURPLE()),
            Component.nullToEmpty("Blocked a server-enforced minimap rules packet.").plainCopy().withStyle(ChatFormatting.DARK_PURPLE())
        );
    }

    public static void sendPacketMessage() {
        Component chatLine = Component.nullToEmpty("[XDRF] Blocked a server-enforced minimap rules packet.").plainCopy().withStyle(ChatFormatting.DARK_PURPLE());
        Player.sendMessage(chatLine);
    }

    public static void showMessageToast() {
        Gui.showToast(
            Component.nullToEmpty("🗺️ Xaero Disabled Radar Fixer").plainCopy().withStyle(ChatFormatting.DARK_PURPLE()),
            Component.nullToEmpty("Blocked a radar-disabling message.").plainCopy().withStyle(ChatFormatting.DARK_PURPLE())
        );
    }

    public static void sendMessageMessage() {
        Component chatLine = Component.nullToEmpty("[XDRF] Blocked a radar-disabling message.").plainCopy().withStyle(ChatFormatting.DARK_PURPLE());
        Player.sendMessage(chatLine);
    }
}
