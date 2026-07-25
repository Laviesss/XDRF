package com.laviesss.xaerodisabledradarfixer.config;

import com.laviesss.xaerodisabledradarfixer.service.XaeroDisabledRadarFixerService;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * 1.21 implementation of config screen builder.
 * Uses vanilla MC types directly — YACL is compiled against these types
 * at build time. Version't handles intermediary remapping at runtime.
 * Override methods in newer version subclasses if YACL API changes.
 */
public class XaeroDisabledRadarFixerConfigScreen_1_21 extends XaeroDisabledRadarFixerConfigScreen.Base {
    @Override
    protected Screen createScreenImpl(Screen parent) {
        XaeroDisabledRadarFixerConfig config = XaeroDisabledRadarFixerConfig.get();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Xaero Disabled Radar Fixer"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("General"))

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Enable Radar Fixer"))
                                .description(OptionDescription.of(Text.literal("Toggle blocking of server radar-disable messages.")))
                                .binding(config.isEnabled(), config::isEnabled, config::setEnabled)
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .option(Option.<XaeroDisabledRadarFixerConfig.BlockingScope>createBuilder()
                                .name(Text.literal("Blocking Scope"))
                                .description(OptionDescription.of(Text.literal("What type of radar-disabling attempts to block.")))
                                .binding(config.getBlockingScope(), config::getBlockingScope, config::setBlockingScope)
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(XaeroDisabledRadarFixerConfig.BlockingScope.class))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Show Chat Message"))
                                .description(OptionDescription.of(Text.literal("Notify via chat when a radar message or packet is blocked.")))
                                .binding(config.isShowChatMessage(), config::isShowChatMessage, config::setShowChatMessage)
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Show Toast Notifications"))
                                .description(OptionDescription.of(Text.literal("Notify via toast when a radar message or packet is blocked.")))
                                .binding(config.isShowToast(), config::isShowToast, config::setShowToast)
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Verbose Logging"))
                                .description(OptionDescription.of(Text.literal("Log detailed information about blocked chat messages and packets (for debugging).")))
                                .binding(config.isVerboseLogging(), config::isVerboseLogging, config::setVerboseLogging)
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .option(ButtonOption.createBuilder()
                                .name(Text.literal("Enforce Blocking"))
                                .description(OptionDescription.of(Text.literal("Replay the last blocked radar-disabling code or rules packet — based on what was cached this session.")))
                                .action((screen, button) -> XaeroDisabledRadarFixerService.enforceBlocking())
                                .build())

                        .option(ButtonOption.createBuilder()
                                .name(Text.literal("Revoke Blocking"))
                                .description(OptionDescription.of(Text.literal("Send the reset code or modified rules packet to undo the server's blocking attempt — based on what was cached this session.")))
                                .action((screen, button) -> XaeroDisabledRadarFixerService.revokeBlocking())
                                .build())

                        .build())
                .build()
                .generateScreen(parent);
    }
}
