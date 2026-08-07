package com.laviesss.xaerodisabledradarfixer.mixin.entry;

import com.laviesss.xaerodisabledradarfixer.mixin.impl.ClientPacketListenerMixinImpl;
import dev.gxlg.versiont.api.R;
import dev.gxlg.versiont.gen.net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import dev.gxlg.versiont.mixins.Compare;
import dev.gxlg.versiont.mixins.Comparison;
import dev.gxlg.versiont.mixins.VersiontMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@VersiontMixin(
    compare = { @Compare(version = "26.1", comparison = Comparison.NOT_LOWER) }
)
@Mixin(targets = "net.minecraft.client.multiplayer.ClientPacketListener", remap = false)
public class ClientPacketListenerMixinEntry_Deobf {
    @Inject(method = "handleSystemChat", at = @At("HEAD"), cancellable = true)
    private void handleSystemChat(@Coerce Object packet, CallbackInfo ci) {
        ClientboundSystemChatPacket wPacket = R.wrapperInst(ClientboundSystemChatPacket.class, packet);
        if (ClientPacketListenerMixinImpl.handleSystemChat(wPacket)) {
            ci.cancel();
        }
    }
}