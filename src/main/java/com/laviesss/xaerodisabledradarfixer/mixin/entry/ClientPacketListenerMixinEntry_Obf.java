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
    compare = { @Compare(version = "1.21", comparison = Comparison.NOT_LOWER) }, obfuscated = true
)
@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "net.minecraft.class_634", remap = false)
public class ClientPacketListenerMixinEntry_Obf {
    @Inject(method = "method_43596", at = @At("HEAD"), cancellable = true)
    private void handleSystemChat(@Coerce Object packet, CallbackInfo ci) {
        ClientboundSystemChatPacket wPacket = R.wrapperInst(ClientboundSystemChatPacket.class, packet);
        if (ClientPacketListenerMixinImpl.handleSystemChat(wPacket)) {
            ci.cancel();
        }
    }
}