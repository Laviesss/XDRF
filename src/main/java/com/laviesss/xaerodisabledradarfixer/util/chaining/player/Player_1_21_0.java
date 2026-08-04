package com.laviesss.xaerodisabledradarfixer.util.chaining.player;

import dev.gxlg.versiont.gen.net.minecraft.client.Minecraft;
import dev.gxlg.versiont.gen.net.minecraft.client.player.LocalPlayer;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;

public class Player_1_21_0 extends Player.Base {
    @Override
    public void sendMessage(Component text) {
        LocalPlayer player = getPlayer();
        if (player == null) {
            return;
        }
        player.displayClientMessage(text, false);
    }

    protected LocalPlayer getPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.getPlayerField();
    }
}
