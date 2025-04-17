package com.laviesss.xaeroradarfixer.mixin;

import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameMessageS2CPacket.class)
public interface GameMessageS2CPacketAccessor {
    @Accessor("content")
    Text getContent(); // This must match the actual field name in Yarn!
}
