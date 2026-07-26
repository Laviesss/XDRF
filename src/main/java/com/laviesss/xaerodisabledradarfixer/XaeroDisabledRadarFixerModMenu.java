package com.laviesss.xaerodisabledradarfixer;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class XaeroDisabledRadarFixerModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        try {
            // Resolve the create method reflectively — no Screen import in bytecode
            Method createMethod = Class.forName(
                    "com.laviesss.xaerodisabledradarfixer.config.XaeroDisabledRadarFixerConfigScreen"
            ).getMethod("createScreen", Object.class);

            // Return a Proxy implementing ConfigScreenFactory — no Screen in our bytecode.
            // ModMenu calls factory.create(Screen parent) → our handler calls createScreen(Object parent)
            return (ConfigScreenFactory<?>) Proxy.newProxyInstance(
                    XaeroDisabledRadarFixerModMenu.class.getClassLoader(),
                    new Class<?>[]{ConfigScreenFactory.class},
                    (proxy, method, args) -> {
                        if ("create".equals(method.getName()) && args != null && args.length == 1) {
                            return createMethod.invoke(null, args[0]);
                        }
                        return method.invoke(this, args);
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ModMenu config screen factory", e);
        }
    }
}
