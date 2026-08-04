package com.laviesss.xaerodisabledradarfixer.util.chaining.player;

import dev.gxlg.versiont.gen.net.minecraft.client.player.LocalPlayer;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;

public class Player_26_1_0 extends Player_1_21_0 {
    @Override
    public void sendMessage(Component text) {
        LocalPlayer player = getPlayer();
        if (player == null) {
            return;
        }
        player.sendSystemMessage(text);
    }
}
