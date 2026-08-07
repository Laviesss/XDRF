package com.laviesss.xaerodisabledradarfixer.config;

import com.laviesss.xaerodisabledradarfixer.service.ReplayService;
import dev.gxlg.versiont.api.R;
import dev.gxlg.versiont.gen.dev.isxander.yacl3.api.ButtonOption;
import dev.gxlg.versiont.gen.dev.isxander.yacl3.api.ConfigCategory;
import dev.gxlg.versiont.gen.dev.isxander.yacl3.api.Option;
import dev.gxlg.versiont.gen.dev.isxander.yacl3.api.OptionDescription;
import dev.gxlg.versiont.gen.dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.gxlg.versiont.gen.dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.gxlg.versiont.gen.dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.gxlg.versiont.gen.dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.gxlg.versiont.gen.net.minecraft.client.gui.screens.Screen;
import dev.gxlg.versiont.gen.net.minecraft.network.chat.Component;

import java.util.function.Function;

public class ConfigScreen {
    public static Screen createConfigScreen(Screen parent) {
        Config config = Config.get();
        Config defaultConfig = new Config();

        Option enabled = Option.createBuilder().name(Component.nullToEmpty("Enable Radar Fixer")).description(description("Toggle blocking of server radar-disable messages."))
                               .binding(defaultConfig.isEnabled(), config::isEnabled, v -> config.setEnabled((boolean) v)).controller(booleanControllerFactory()).build();

        Option scope = Option.createBuilder().name(Component.nullToEmpty("Blocking Scope")).description(description("What type of radar-disabling attempts to block."))
                             .binding(defaultConfig.getBlockingScope(), config::getBlockingScope, v -> config.setBlockingScope((Config.BlockingScope) v))
                             .controller(enumControllerFactory(Config.BlockingScope.class)).build();

        Option chat = Option.createBuilder().name(Component.nullToEmpty("Show Chat Message")).description(description("Notify via chat when a radar message or packet is blocked."))
                            .binding(defaultConfig.isShowChatMessage(), config::isShowChatMessage, v -> config.setShowChatMessage((boolean) v)).controller(booleanControllerFactory()).build();

        Option toast = Option.createBuilder().name(Component.nullToEmpty("Show Toast Notifications")).description(description("Notify via toast when a radar message or packet is blocked."))
                             .binding(defaultConfig.isShowToast(), config::isShowToast, v -> config.setShowToast((boolean) v)).controller(booleanControllerFactory()).build();

        Option sound = Option.createBuilder().name(Component.nullToEmpty("Play Sound Notification")).description(description("Play a sound notification when a radar message or packet is blocked."))
                             .binding(defaultConfig.isPlaySound(), config::isPlaySound, v -> config.setPlaySound((boolean) v)).controller(booleanControllerFactory()).build();

        Option verbose = Option.createBuilder().name(Component.nullToEmpty("Verbose Logging"))
                               .description(description("Log detailed information about blocked chat messages and packets (for debugging)."))
                               .binding(defaultConfig.isVerboseLogging(), config::isVerboseLogging, v -> config.setVerboseLogging((boolean) v)).controller(booleanControllerFactory()).build();

        ButtonOption enforce = ButtonOption.createButtonBuilder().name(Component.nullToEmpty("Enforce Blocking"))
                                           .description(description("Replay the last blocked radar-disabling code or rules packet - based on what was cached this session."))
                                           .action((s, b) -> ReplayService.enforce()).build();

        ButtonOption revoke = ButtonOption.createButtonBuilder().name(Component.nullToEmpty("Revoke Blocking"))
                                          .description(description("Send the reset code or modified rules packet to undo the server's blocking attempt - based on what was cached this session."))
                                          .action((s, b) -> ReplayService.revoke()).build();

        ConfigCategory category = ConfigCategory.createBuilder().name(Component.nullToEmpty("General")).option(enabled).option(scope).option(chat).option(toast).option(sound).option(verbose)
                                                .option(enforce).option(revoke).build();

        return YetAnotherConfigLib.createBuilder().title(Component.nullToEmpty("Xaero Disabled Radar Fixer")).category(category).build().generateScreen(parent);
    }

    private static Function<Option, ControllerBuilder> booleanControllerFactory() {
        return opt -> BooleanControllerBuilder.create(opt).coloured(true);
    }

    @SuppressWarnings("SameParameterValue")
    private static Function<Option, ControllerBuilder> enumControllerFactory(Class<? extends Enum<?>> enumClass) {
        return opt -> EnumControllerBuilder.create(opt).enumClass(R.clz(enumClass));
    }

    private static OptionDescription description(String desc) {
        return OptionDescription.of(new Component[]{ Component.nullToEmpty(desc) });
    }
}
