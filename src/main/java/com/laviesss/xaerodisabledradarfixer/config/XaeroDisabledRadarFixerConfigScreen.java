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
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Xaero Disabled Radar Fixer"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("General"))

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Enable Radar Fixer"))
                                .description(OptionDescription.of(Text.literal("Toggle blocking of server radar‑disable messages.")))
                                .binding(
                                        true,
                                        () -> XaeroDisabledRadarFixerConfig.enabled,
                                        val -> {
                                            XaeroDisabledRadarFixerConfig.setEnabled(val);
                                        }
                                )
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Show Chat Message"))
                                .description(OptionDescription.of(Text.literal("Notify via chat when a radar message is blocked.")))
                                .binding(
                                        true,
                                        () -> XaeroDisabledRadarFixerConfig.showChatMessage,
                                        val -> {
                                            XaeroDisabledRadarFixerConfig.setShowChatMessage(val);
                                        }
                                )
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Show Toast Notifications"))
                                .description(OptionDescription.of(Text.literal("Notify via toast when a radar message is blocked.")))
                                .binding(
                                        true,
                                        () -> XaeroDisabledRadarFixerConfig.showToast,
                                        val -> {
                                            XaeroDisabledRadarFixerConfig.setShowToast(val);
                                        }
                                )
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .option(ButtonOption.createBuilder()
                                .name(Text.literal("Resend Last Blocked Code"))
                                .description(OptionDescription.of(Text.literal("Re-send the last blocked radar-disabling code.")))
                                .action((screen, button) -> XaeroDisabledRadarFixerService.resendLastBlockedCode())
                                .build())

                        .option(ButtonOption.createBuilder()
                                .name(Text.literal("Send Reset Code"))
                                .description(OptionDescription.of(Text.literal("Send the original minimap reset message.")))
                                .action((screen, button) -> XaeroDisabledRadarFixerService.sendResetCode())
                                .build())

                        .build())
                // No need to call .save() here since we save on every toggle
                .build()
                .generateScreen(parent);
    }
}
