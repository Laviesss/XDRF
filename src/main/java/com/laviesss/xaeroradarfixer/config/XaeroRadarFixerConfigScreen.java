package com.laviesss.xaeroradarfixer.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription; // <- ✅ FIXED
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class XaeroRadarFixerConfigScreen {
    public static Screen create(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Xaero Radar Fixer Config"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Settings"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Enable Xaero Radar Fixer"))
                                .description(OptionDescription.of(Text.literal("Toggle the radar message blocker")))
                                .binding(
                                        true,
                                        () -> XaeroRadarFixerConfig.enabled,
                                        val -> XaeroRadarFixerConfig.enabled = val
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Show Chat Message"))
                                .description(OptionDescription.of(Text.literal("Toggle whether the chat message is shown")))
                                .binding(
                                        true,
                                        () -> XaeroRadarFixerConfig.showChatMessage,
                                        val -> XaeroRadarFixerConfig.showChatMessage = val
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Show Toast"))
                                .description(OptionDescription.of(Text.literal("Toggle whether the toast notification is shown")))
                                .binding(
                                        true,
                                        () -> XaeroRadarFixerConfig.showToast,
                                        val -> XaeroRadarFixerConfig.showToast = val
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .save(XaeroRadarFixerConfig::save)
                .build()
                .generateScreen(parent);
    }
}
