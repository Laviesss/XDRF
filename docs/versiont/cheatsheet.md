# Version't Cheatsheet

> One-page reference for migration & daily use. Verified against XDRF migration (MC 1.21 → 26.3).

---

## Build Setup (2 files)

**`settings.gradle`**
```gradle
pluginManagement {
    repositories { gradlePluginPortal(); maven { url 'https://maven.fabricmc.net/' }; maven { url 'https://gxlg.github.io/maven-repo/' } }
    resolutionStrategy { eachPlugin { if (requested.id.id == 'dev.gxlg.versiont-toolchain') useModule("dev.gxlg:versiont-toolchain:1.3.4") } }
}
```

**`build.gradle`**
```gradle
plugins { id 'fabric-loom' version '1.17.11'; id 'dev.gxlg.versiont-toolchain' version '1.3.4' }
java { toolchain { languageVersion = JavaLanguageVersion.of(21) }; withSourcesJar() }
versiont { mapping = file('versiont.mapping') }

dependencies {
    minecraft "com.mojang:minecraft:1.21"
    mappings  "net.fabricmc:yarn:1.21+build.1:v2"
    modImplementation "net.fabricmc:fabric-loader:0.18.6"
    // NO Fabric API compile dep — inline via R
    include "dev.gxlg:versiont-library:1.2.3"
}
tasks.named('sourcesJar') { dependsOn tasks.named('versiontLayer') }
```

---

## versiont.mapping Template

```text
# Import: forward slashes = flat packages; last segment = wrapper simple name
import net.minecraft.class_310/net/minecraft/client/MinecraftClient/net/minecraft/client/Minecraft
import net.minecraft.class_746/net/minecraft/client/network/ClientPlayerEntity/net/minecraft/client/player/LocalPlayer
import net.minecraft.class_304/net/minecraft/client/option/KeyBinding/net/minecraft/client/KeyMapping
import net.minecraft.class_2561/net/minecraft/text/Text/net/minecraft/network/chat/Component
import net.minecraft.class_10918/net/minecraft/util/Formatting/net/minecraft/network/chat/ChatFormatting
import net.minecraft.class_3417/net/minecraft/sound/SoundEvents
import net.minecraft.class_370/net/minecraft/client/toast/SystemToast
import net.minecraft.class_374/net/minecraft/client/toast/ToastManager
import net.minecraft.class_350/net/minecraft/client/gui/screen/Screen
import net.minecraft.class_2588/net/minecraft/network/packet/s2c/play/GameMessageS2CPacket
import net.minecraft.class_2594/net/minecraft/network/protocol/game/ClientboundSystemChatPacket
import net.minecraft.class_915/net/minecraft/client/network/ClientPlayNetworkHandler/net/minecraft/client/multiplayer/ClientPacketListener

class Minecraft
    static Minecraft getInstance()
    @Nullable LocalPlayer player
    @Nullable ToastManager toastManager
    @Nullable ClientPacketListener networkHandler

class LocalPlayer
    void sendMessage(Component text, boolean overlay)
    void playSound(SoundEvents event, float volume, float pitch)

class KeyMapping
    <init>(String id, String translationKey, int code, String categoryId)
    boolean getWasPressedField()

class Component
    static Component literal(String text)
    Component formatted(ChatFormatting formatting)

class ChatFormatting
    static final ChatFormatting DARK_PURPLE

class SoundEvents
    static final SoundEvents ENTITY_VILLAGER_CELEBRATE

class SystemToast
    static void add(ToastManager manager, Object type, Component title, Component text)

class ToastManager
    # marker

class Screen
    # marker for YACL

class GameMessageS2CPacket
    Component getContentField()

class ClientboundSystemChatPacket
    <init>(Component content, boolean overlay)

class ClientPacketListener
    void onGameMessage(ClientboundSystemChatPacket packet)
```

---

## Source Migration Patterns

| Before | After |
|--------|-------|
| `MinecraftClient.getInstance()` | `Minecraft.getInstance()` |
| `mc.player` | `mc.getPlayerField()` |
| `mc.getToastManager()` | `mc.getToastManagerField()` |
| `mc.getNetworkHandler()` | `mc.getNetworkHandlerField()` |
| `Text.literal("x")` | `Component.literal("x")` |
| `text.formatted(Formatting.RED)` | `text.formatted(ChatFormatting.RED())` |
| `Formatting.RED` | `ChatFormatting.RED()` |
| `SoundEvents.ENTITY_X` | `SoundEvents.ENTITY_X()` |
| `SystemToast.add(tm, type, t1, t2)` | `SystemToast.add(tm, null, t1, t2)` |
| `packet.content()` | `packet.getContentField()` |
| `new KeyBinding(id, type, code, cat)` | `V.higher("1.21.10") ? enumCtor() : stringCtor()` |
| `KeyBindingHelper.registerKeyBinding(kb)` | `R.clz("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper").mthd("registerKeyBinding", void.class, KeyMapping.class).invk(null, kb)` |

---

## R API Quick Reference

```java
// Class lookup (slash-separated fallback chain)
R.clz("net.minecraft.class_310/net/minecraft/client/MinecraftClient/net/minecraft/client/Minecraft")

// Instance method
R.clz(W.class).inst(w.unwrap()).mthd("methodName", Ret.class, Arg1.class, Arg2.class).invk(arg1, arg2)

// Static method
R.clz("pkg.Class").mthd("staticMethod", Ret.class, Arg.class).invk(null, arg)

// Field read
R.clz(W.class).inst(w.unwrap()).fld("fieldName", Type.class).get()

// Field write
R.clz(W.class).inst(w.unwrap()).fld("fieldName", Type.class).set(value)

// Constructor + wrap
R.wrapperInst(Wrapper.class, R.clz(Wrapper.class).constr(String.class, int.class).newInst("id", 42))

// Version check
V.higher("1.21.10")   // strictly > 1.21.10
V.higher("1.21.9")    // ≥ 1.21.10
```

---

## Version Dispatch Example

```java
private static KeyMapping createKey(String id, String name, int code, String cat) {
    if (V.higher("1.21.10")) {  // 1.21.11+
        try {
            Class<?> categoryClass = Class.forName("net.minecraft.class_304$class_11900");
            Object misc = Arrays.stream(categoryClass.getEnumConstants())
                .filter(c -> "MISC".equals(((Enum<?>)c).name()))
                .findFirst().orElseThrow();
            Class<?> typeClass = Class.forName("net.minecraft.class_3675$class_3676");
            Object keySym = Arrays.stream(typeClass.getEnumConstants())
                .filter(t -> "KEYSYM".equals(((Enum<?>)t).name()))
                .findFirst().orElseThrow();
            return R.wrapperInst(KeyMapping.class,
                R.clz(KeyMapping.class).constr(String.class, keySym.getClass(), int.class, categoryClass)
                    .newInst(id, name, code, misc));
        } catch (Throwable e) { throw new RuntimeException(e); }
    }
    return R.wrapperInst(KeyMapping.class,
        R.clz(KeyMapping.class).constr(String.class, String.class, int.class, String.class)
            .newInst(id, name, code, cat));
}
```

---

## Mixin @Inject Parameter Types

| Scenario | @Inject Parameter Type |
|----------|------------------------|
| Target method exists in 1.21 Yarn | Use 1.21 Yarn name (e.g., `GameMessageS2CPacket`) |
| Target method renamed in 1.21.11+ | Use 1.21 Yarn name; Fabric Loader remaps at runtime |
| Need Version't wrapper inside | Wrap with `R.wrapperInst(Wrapper.class, rawPacket)` inside method |

```java
// Correct: Use 1.21 Yarn name in @Inject
@Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
private void onGameMessage(net.minecraft.network.packet.s2c.play.GameMessageS2CPacket packet, CallbackInfo ci) {
    // Wrap for Version't cross-version handling
    ClientboundSystemChatPacket wrapped = R.wrapperInst(ClientboundSystemChatPacket.class, packet);
    ...
}
```

---

## YACL / ModMenu — Keep Vanilla

```java
import net.minecraft.client.gui.screen.Screen;   // NOT wrapper
import net.minecraft.text.Text;                  // NOT wrapper

public static Screen create(Screen parent) {
    return YetAnotherConfigLib.createBuilder()
        .title(Text.literal("Title"))  // vanilla Text
        .category(ConfigCategory.createBuilder()
            .name(Text.literal("Cat"))
            .build())
        .build()
        .generateScreen(parent);  // parent is vanilla Screen
}
```

---

## Common Fixes

| Error | Fix |
|-------|-----|
| `node: command not found` | Install Node.js LTS, ensure on PATH |
| `versiont.mapping not found` | `versiont { mapping = file('versiont.mapping') }` in build.gradle |
| Wrapper compile error: `cannot find symbol` | Missing import in mapping? Run `./gradlew versiontLayer` first? |
| Runtime `ClassNotFoundException` | Import chain missing Yarn name? Check mapping `import` order |
| Runtime `NoSuchMethodException` | Arg types in mapping match real signature? |
| `V.higher("1.21.10")` false on 1.21.11 | It's strict `>`; use `V.higher("1.21.9")` for ≥ 1.21.10 |
| Mod JAR missing `dev/gxlg/versiont` | Used `include "dev.gxlg:versiont-library"` not `implementation` |
| Inner class getter returns wrong type | Don't declare inner classes in mapping; use `R.clz("Outer$Inner").fld(...)` directly |

---

## Test Matrix

| MC Version | mappings | Purpose |
|------------|----------|---------|
| 1.21 | 1.21+build.1:v2 | Compile base; runClient |
| 1.21.11 | 1.21.11+build.1:v2 | First break (KeyBinding enum) |
| 26.3 | 26.3+build.1:v2 | Major rename validation |

---

## Files Checklist

- [ ] `settings.gradle` — gXLg repo + plugin resolution
- [ ] `build.gradle` — plugin, deps, `include versiont-library`, `sourcesJar` depends on `versiontLayer`
- [ ] `versiont.mapping` — all MC APIs your mod touches
- [ ] Source files — MC imports → `dev.gxlg.versiont.gen.*` wrappers
- [ ] YACL/ModMenu — keep `net.minecraft.client.gui.screen.Screen`, `net.minecraft.text.Text`
- [ ] CI — Node.js + JDK 25 (Gradle) + JDK 21 (runtime)

---

*All patterns verified in XDRF migration (MC 1.21 → 26.3, Version't 1.3.4/1.2.3).*