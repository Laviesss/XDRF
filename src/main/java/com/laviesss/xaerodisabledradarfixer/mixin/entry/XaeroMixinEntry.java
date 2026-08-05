package com.laviesss.xaerodisabledradarfixer.mixin.entry;

import com.laviesss.xaerodisabledradarfixer.mixin.impl.XaeroMixinImpl;
import dev.gxlg.versiont.api.R;
import dev.gxlg.versiont.gen.xaero.hud.packet.basic.ClientboundRulesPacket;
import dev.gxlg.versiont.gen.xaero.hud.packet.basic.ClientboundRulesPacket$ClientHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "xaero.hud.packet.basic.ClientboundRulesPacket$ClientHandler", remap = false)
public class XaeroMixinEntry {
    @SuppressWarnings("DataFlowIssue")
    @Inject(
        method = "accept(Lxaero/hud/packet/basic/ClientboundRulesPacket;)V", at = @At("HEAD"), cancellable = true, remap = false
    )
    private void accept(@Coerce Object message, CallbackInfo ci) {
        ClientboundRulesPacket packet = R.wrapperInst(ClientboundRulesPacket.class, message);
        ClientboundRulesPacket$ClientHandler handler = R.wrapperInst(ClientboundRulesPacket$ClientHandler.class, this);
        if (XaeroMixinImpl.accept(packet, handler)) {
            ci.cancel();
        }
    }
}
