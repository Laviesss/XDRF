package com.laviesss.xaerodisabledradarfixer.config;

import com.laviesss.xaerodisabledradarfixer.service.XaeroDisabledRadarFixerService;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.laviesss.xaerodisabledradarfixer.util.XaeroDisabledRadarFixerYaclHelper.*;

/**
 * 1.21 implementation of config screen builder.
 * <p>
 * All YACL methods that accept vanilla {@code Text} are invoked via
 * {@link XaeroDisabledRadarFixerYaclHelper} so no MC type references
 * appear in our bytecode. This prevents crashes on MC 26.2+ where
 * Fabric Loader has no mappings and intermediary class names don't exist.
 * <p>
 * YACL's {@code generateScreen(Screen)} is called via Version't reflection
 * ({@code R.clz(...).inst(...).mthd(...).invk(...)}) so no MC or YACL type
 * references appear in our bytecode.
 * <p>
 * Override methods in newer version subclasses if YACL API changes.
 */
public class XaeroDisabledRadarFixerConfigScreen_1_21 extends XaeroDisabledRadarFixerConfigScreen.Base {

    @Override
    protected Object createScreenImpl(Object parent) {
        XaeroDisabledRadarFixerConfig config = XaeroDisabledRadarFixerConfig.get();

        // ── Title on YetAnotherConfigLib.Builder ──────────────────
        Object yaclBuilder = createBuilder("YetAnotherConfigLib");
        title(yaclBuilder, "Xaero Disabled Radar Fixer");

        // ── Category: General ─────────────────────────────────────
        Object catBuilder = createBuilder("ConfigCategory");
        name(catBuilder, "General");

        // ── Option: Enable Radar Fixer ────────────────────────────
        Object enableOpt = createBuilder("Option");
        name(enableOpt, "Enable Radar Fixer");
        desc(enableOpt, "Toggle blocking of server radar-disable messages.");
        Supplier<Boolean> getEnabled   = config::isEnabled;
        Consumer<Boolean> setEnabled   = config::setEnabled;
        binding(enableOpt, config.isEnabled(), getEnabled, setEnabled);
        controller(enableOpt, booleanControllerFactory());
        option(catBuilder, build(enableOpt));

        // ── Option: Blocking Scope ────────────────────────────────
        Object scopeOpt = createBuilder("Option");
        name(scopeOpt, "Blocking Scope");
        desc(scopeOpt, "What type of radar-disabling attempts to block.");
        Supplier<XaeroDisabledRadarFixerConfig.BlockingScope> getScope = config::getBlockingScope;
        Consumer<XaeroDisabledRadarFixerConfig.BlockingScope> setScope = config::setBlockingScope;
        binding(scopeOpt, config.getBlockingScope(), getScope, setScope);
        controller(scopeOpt, enumControllerFactory(XaeroDisabledRadarFixerConfig.BlockingScope.class));
        option(catBuilder, build(scopeOpt));

        // ── Option: Show Chat Message ─────────────────────────────
        Object chatOpt = createBuilder("Option");
        name(chatOpt, "Show Chat Message");
        desc(chatOpt, "Notify via chat when a radar message or packet is blocked.");
        Supplier<Boolean> getChat   = config::isShowChatMessage;
        Consumer<Boolean> setChat   = config::setShowChatMessage;
        binding(chatOpt, config.isShowChatMessage(), getChat, setChat);
        controller(chatOpt, booleanControllerFactory());
        option(catBuilder, build(chatOpt));

        // ── Option: Show Toast Notifications ──────────────────────
        Object toastOpt = createBuilder("Option");
        name(toastOpt, "Show Toast Notifications");
        desc(toastOpt, "Notify via toast when a radar message or packet is blocked.");
        Supplier<Boolean> getToast   = config::isShowToast;
        Consumer<Boolean> setToast   = config::setShowToast;
        binding(toastOpt, config.isShowToast(), getToast, setToast);
        controller(toastOpt, booleanControllerFactory());
        option(catBuilder, build(toastOpt));

        // ── Option: Verbose Logging ───────────────────────────────
        Object verboseOpt = createBuilder("Option");
        name(verboseOpt, "Verbose Logging");
        desc(verboseOpt, "Log detailed information about blocked chat messages and packets (for debugging).");
        Supplier<Boolean> getVerbose   = config::isVerboseLogging;
        Consumer<Boolean> setVerbose   = config::setVerboseLogging;
        binding(verboseOpt, config.isVerboseLogging(), getVerbose, setVerbose);
        controller(verboseOpt, booleanControllerFactory());
        option(catBuilder, build(verboseOpt));

        // ── Button: Enforce Blocking ──────────────────────────────
        Object enforceBtn = createBuilder("ButtonOption");
        name(enforceBtn, "Enforce Blocking");
        desc(enforceBtn, "Replay the last blocked radar-disabling code or rules packet \u2014 based on what was cached this session.");
        action(enforceBtn, (screen, button) -> XaeroDisabledRadarFixerService.enforceBlocking());
        option(catBuilder, build(enforceBtn));

        // ── Button: Revoke Blocking ───────────────────────────────
        Object revokeBtn = createBuilder("ButtonOption");
        name(revokeBtn, "Revoke Blocking");
        desc(revokeBtn, "Send the reset code or modified rules packet to undo the server\u2019s blocking attempt \u2014 based on what was cached this session.");
        action(revokeBtn, (screen, button) -> XaeroDisabledRadarFixerService.revokeBlocking());
        option(catBuilder, build(revokeBtn));

        // ── Build ─────────────────────────────────────────────────
        category(yaclBuilder, build(catBuilder));
        Object yaclScreen = build(yaclBuilder);

        // generateScreen(Screen) called via Version't reflection — no Screen in our bytecode.
        // parent is a vanilla Screen at runtime; YACL expects vanilla Screen.
        return generateScreen(yaclScreen, parent);
    }
}