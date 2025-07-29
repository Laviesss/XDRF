package com.laviesss.xaerodisabledradarfixer.config;

import com.laviesss.xaerodisabledradarfixer.service.XaeroDisabledRadarFixerService;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class XaeroDisabledRadarFixerConfigScreen {
    public static Screen create(Screen parent) {
        XaeroDisabledRadarFixerConfig config = XaeroDisabledRadarFixerConfig.get();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Xaero Disabled Radar Fixer"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("General"))

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Enable Radar Fixer"))
                                .description(OptionDescription.of(Text.literal("Toggle blocking of server radar‑disable messages.")))
                                .binding(
                                        config.isEnabled(),
                                        config::isEnabled,
                                        config::setEnabled
                                )
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Show Chat Message"))
                                .description(OptionDescription.of(Text.literal("Notify via chat when a radar message is blocked.")))
                                .binding(
                                        config.isShowChatMessage(),
                                        config::isShowChatMessage,
                                        config::setShowChatMessage
                                )
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Show Toast Notifications"))
                                .description(OptionDescription.of(Text.literal("Notify via toast when a radar message is blocked.")))
                                .binding(
                                        config.isShowToast(),
                                        config::isShowToast,
                                        config::setShowToast
                                )
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .option(ButtonOption.createBuilder()
                                .name(Text.literal("Resend Last Blocked Code"))
                                .description(OptionDescription.of(Text.literal("Re-send the last blocked radar-disabling code so that if mods ask to see minimap if they suspect cheating or other stuff (e.g. screenshots or screen sharing).")))
                                .action((screen, button) -> XaeroDisabledRadarFixerService.resendLastBlockedCode())
                                .build())

                        .option(ButtonOption.createBuilder()
                                .name(Text.literal("Send Reset Code"))
                                .description(OptionDescription.of(Text.literal("Sends the reset message code built into Xaero's Minimap/World Map mods to allow radar functionality like it was never blocked.")))
                                .action((screen, button) -> XaeroDisabledRadarFixerService.sendResetCode())
                                .build())

                        .build())
                .build()
                .generateScreen(parent);
    }
}
