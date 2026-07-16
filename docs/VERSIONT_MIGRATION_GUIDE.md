# Migrating a Fabric Mod to Version't for Cross-Version Compatibility

## Complete Guide: From Single-Version to Multi-Version (1.21 → 26.3)

This document describes the full process of migrating the **XDRF (Xaero Disabled Radar Fixer)** mod from a single-version Fabric mod to a cross-version mod using **Version't**, a reflection-based abstraction layer that lets one source tree target multiple Minecraft releases.

---

## Table of Contents

1. [Prerequisites & Understanding Version't](#prerequisites--understanding-versiont)
2. [Project Setup](#project-setup)
3. [Audit MC API Surface](#audit-mc-api-surface)
4. [Write versiont.mapping](#write-versiontmapping)
5. [Migrate Source Code](#migrate-source-code)
6. [Fix Generated Wrapper Issues](#fix-generated-wrapper-issues)
7. [Build & Test](#build--test)
8. [Common Pitfalls & Solutions](#common-pitfalls--solutions)
9. [Checklist for Other Projects](#checklist-for-other-projects)

---

## Prerequisites & Understanding Version't

### What Version't Is
- **Runtime reflection layer** for Minecraft Fabric mods
- Generates **type-safe wrapper classes** at build time from a `versiont.mapping` file
- At runtime, uses `Class.forName()` with fallback chains (intermediary → mojang → yarn names)
- Single compile base (oldest supported version) + runtime dispatch for API differences

### What Version't Is NOT
- Not a source-set / multi-module per-version solution
- Not a replacement for Fabric's RuntimeModRemapper (works alongside it)
- Not magic — you must declare every MC class/method/field you touch

### Version't Architecture
```
Your Source → versiont.mapping → versiontLayer (Node.js) → 
Generated Wrappers (dev.gxlg.versiont.gen.*) → 
Your Code imports Wrappers → 
Runtime: R.clz("name1/name2/name3") → Class.forName() chain
```

### Key Version't Classes
| Class | Purpose |
|-------|---------|
| `R.clz()` | Runtime class lookup (slash-separated fallback chain) |
| `R.mthd()` | Method lookup + invocation |
| `R.fld()` | Field lookup + get/set |
| `R.constr()` | Constructor lookup + instantiation |
| `R.wrapperInst()` | Wrap raw object in generated wrapper |
| `V.higher("1.21.10")` | Runtime version check |
| `Wrapper` | Base class for all generated wrappers |

---

## Project Setup

### 1. Add Version't Repository & Plugin

**settings.gradle:**
```gradle
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url 'https://maven.fabricmc.net/' }
        maven { url 'https://maven.gxlg.dev/releases' }  // gXLg Maven for Version't
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == 'dev.gxlg.versiont-toolchain') {
                useModule("dev.gxlg:versiont-toolchain:1.3.4")
            }
        }
    }
}
```

### 2. Apply Plugin & Configure

**build.gradle:**
```gradle
plugins {
    id 'fabric-loom' version '1.17.11'
    id 'maven-publish'
    id 'dev.gxlg.versiont-toolchain' version '1.3.4'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)  // Match your JDK
    }
    withSourcesJar()
}

versiont {
    mapping = file('versiont.mapping')
}

repositories {
    mavenCentral()
    maven { url 'https://maven.fabricmc.net/' }
    maven { url 'https://maven.terraformersmc.com/' }
    maven { url 'https://maven.isxander.dev/releases' }
    maven { url 'https://maven.gxlg.dev/releases' }
    maven { url 'https://api.modrinth.com/maven' }
}

dependencies {
    // COMPILE BASE: Oldest supported MC version
    minecraft "com.mojang:minecraft:1.21"
    mappings  "net.fabricmc:yarn:1.21+build.1:v2"

    // Fabric Loader — current version (0.18.x required by Version't 1.2.3)
    modImplementation "net.fabricmc:fabric-loader:0.18.6"

    // DO NOT add Fabric API as compile dep — inline via Version't instead
    // modImplementation "net.fabricmc.fabric-api:fabric-api:..."

    // YACL / ModMenu — user installs matching version
    modImplementation "dev.isxander:yet-another-config-lib:3.6.2+1.21-fabric"
    modImplementation "com.terraformersmc:modmenu:11.0.0"

    // Version't runtime library
    include "dev.gxlg:versiont-library:1.2.3"
}

// SourcesJar needs generated wrappers
tasks.named('sourcesJar') {
    dependsOn tasks.named('versiontLayer')
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}
```

---

## Audit MC API Surface

### 1. List Every MC Class/Method/Field Your Mod Touches

For XDRF, the inventory was:

| MC Class (Yarn 1.21) | Yarn 26.x Name | Methods/Fields Used |
|---------------------|----------------|---------------------|
| `MinecraftClient` (`class_310`) | `Minecraft` | `getInstance()`, `player`, `toastManager`, `networkHandler` |
| `ClientPlayerEntity` (`class_746`) | `LocalPlayer` | `sendMessage(Text, boolean)`, `playSound(SoundEvent, float, float)` |
| `KeyBinding` (`class_304`) | `KeyMapping` | Constructor (4-arg), `wasPressed()` |
| `KeyBinding.Category` | `KeyMapping.Category` | `MISC` constant |
| `Text` (`class_2561`) | `Component` | `literal(String)`, `formatted(Formatting)` |
| `Formatting` (`class_10918`) | `ChatFormatting` | `DARK_PURPLE` |
| `SoundEvents` (`class_3417`) | `SoundEvents` | `ENTITY_VILLAGER_CELEBRATE` |
| `SystemToast` (`class_370`) | `SystemToast` | `add(ToastManager, Object, Text, Text)` |
| `ToastManager` (`class_374`) | `ToastManager` | (marker only) |
| `Screen` (`class_350`) | `Screen` | (marker for YACL) |
| `GameMessageS2CPacket` (`class_2588`) | `ClientboundSystemChatPacket` | `content()`, constructor |
| `ClientPlayNetworkHandler` (`class_915`) | `ClientPacketListener` | `onGameMessage()` |

**Also note mixin targets:**
- `ClientPlayNetworkHandler` (remapped — works via Fabric Loader)
- Xaero mod class: `xaero.hud.packet.basic.ClientboundRulesPacket$ClientHandler` (`remap=false`)

### 2. Identify Version Breaks

| Break | Versions Affected | Solution |
|-------|-------------------|----------|
| `KeyBinding` constructor: String category → `KeyBinding.Category` enum | 1.21.11+ | `V.higher("1.21.10")` dispatch |
| `ClientPlayNetworkHandler` → `ClientPacketListener` | 26.x | Version't wrapper + Fabric remapper |
| `MinecraftClient` → `Minecraft` | 26.x | Version't wrapper |
| `GameMessageS2CPacket` → `ClientboundSystemChatPacket` | 26.x | Version't wrapper |

---

## Write versiont.mapping

### Mapping File Syntax

```text
# Import format: import <fallback1>/<fallback2>/<fallback3>
# Class format: class <SimpleName>
# Method: <returnType> <intermediary>/<mojang>(<paramTypes...>)
# Field: <Type> <fieldName>
# Constructor: <init>(<paramTypes...>)
# Static field: static final <Type> <NAME>
```

### XDRF's Complete versiont.mapping

```text
# Version't mapping for XDRF (Xaero Disabled Radar Fixer)
# Single mapping drives MC 1.21 through 26.3 from one source tree
# Generated wrappers use last slash segment as simple name, 
# slash path as package under dev.gxlg.versiont.gen

# Core client classes - use forward slashes consistently
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

# Wrapper class definitions

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
    # Marker wrapper - no methods needed

class Screen
    # Marker wrapper for YACL config screen parent type

class GameMessageS2CPacket
    Component getContentField()

class ClientboundSystemChatPacket
    <init>(Component content, boolean overlay)

class ClientPacketListener
    void onGameMessage(ClientboundSystemChatPacket packet)
```

### Critical Mapping Rules Learned

1. **Use forward slashes in imports** — `net/minecraft/client/MinecraftClient` not `net.minecraft.client.MinecraftClient`
   - This makes generated wrappers land in flat `dev.gxlg.versiont.gen.*` packages
   - Avoids `dev.gxlg.versiont.gen.net.minecraft.client...` deep nesting

2. **Static fields need `()` in mapping** — `static final ChatFormatting DARK_PURPLE`
   - Generator creates `DARK_PURPLE()` method, not field access

3. **Constructor uses `<init>` with param types only** — NO return type

4. **Field accessors need explicit names** — `boolean getWasPressedField()` not `boolean wasPressed`

5. **Inner classes often buggy** — `KeyMapping$Category`, `SystemToast$Type` generators have bugs
   - Workaround: access via `R.clz("Outer$Inner").fld("CONSTANT", Outer$Inner.class).get()`

6. **Nullable fields** — Add `@Nullable` annotation in mapping for fields that can be null

7. **Package structure** — Generated class simple name = last slash segment of import
   - `import net.minecraft.class_310/.../Minecraft` → `class Minecraft` → `dev.gxlg.versiont.gen.Minecraft`

---

## Migrate Source Code

### Pattern  ### 1. Replace Direct MC Imports with Generated Wrappers

| Before | After |
|--------|-------|
| `import net.minecraft.client.MinecraftClient;` | `import dev.gxlg.versiont.gen.Minecraft;` |
| `import net.minecraft.client.network.ClientPlayerEntity;` | `import dev.gxlg.versiont.gen.LocalPlayer;` |
| `import net.minecraft.client.option.KeyBinding;` | `import dev.gxlg.versiont.gen.KeyMapping;` |
| `import net.minecraft.text.Text;` | `import dev.gxlg.versiont.gen.Component;` (for MC 26.x) |
| `import net.minecraft.util.Formatting;` | `import dev.gxlg.versiont.gen.ChatFormatting;` |
| `import net.minecraft.sound.SoundEvents;` | `import dev.gxlg.versiont.gen.SoundEvents;` |
| `import net.minecraft.client.toast.SystemToast;` | `import dev.gxlg.versiont.gen.SystemToast;` |
| `import net.minecraft.client.toast.ToastManager;` | `import dev.gxlg.versiont.gen.ToastManager;` |
| `import net.minecraft.client.gui.screen.Screen;` | `import net.minecraft.client.gui.screen.Screen;` (keep vanilla for YACL) |
| `import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;` | `import dev.gxlg.versiont.gen.ClientboundSystemChatPacket;` |
| `import net.minecraft.client.network.ClientPlayNetworkHandler;` | Keep for `@Mixin` target (Fabric remaps) |

### 2. Update Method Calls

| Before | After |
|--------|-------|
| `MinecraftClient.getInstance()` | `Minecraft.getInstance()` |
| `mc.player` | `mc.getPlayerField()` |
| `mc.getToastManager()` | `mc.getToastManagerField()` |
| `mc.getNetworkHandler()` | `mc.getNetworkHandlerField()` |
| `player.sendMessage(text, false)` | `player.sendMessage(text, false)` (same) |
| `player.playSound(event, 1.0F, 1.0F)` | `player.playSound(event, 1.0F, 1.0F)` (same) |
| `Text.literal("msg")` | `Component.literal("msg")` |
| `text.formatted(Formatting.DARK_PURPLE)` | `text.formatted(ChatFormatting.DARK_PURPLE())` |
| `Formatting.DARK_PURPLE` | `ChatFormatting.DARK_PURPLE()` |
| `SoundEvents.ENTITY_VILLAGER_CELEBRATE` | `SoundEvents.ENTITY_VILLAGER_CELEBRATE()` |
| `SystemToast.add(tm, type, title, text)` | `SystemToast.add(tm, null, title, text)` |
| `packet.content()` | `packet.getContentField()` |

### 3. Version-Specific Dispatch (KeyBinding Example)

```java
public final class XaeroDisabledRadarFixerKeybinds {
    
    public static final KeyMapping TOGGLE_ENABLED = create(...);

    private static KeyMapping create(String id, String translationKey, int code, String categoryId) {
        if (V.higher("1.21.10")) {
            // 1.21.11+ uses KeyBinding.Category enum
            return R.wrapperInst(KeyMapping.class,
                R.clz(KeyMapping.class).constr(String.class, String.class, int.class, String.class)
                    .newInst(id, translationKey, code, categoryId)
            );
        }
        // Pre-1.21.11 uses String category
        return R.wrapperInst(KeyMapping.class,
            R.clz(KeyMapping.class).constr(String.class, String.class, int.class, String.class)
                .newInst(id, translationKey, code, categoryId)
        );
    }

    public static void register() {
        try {
            R.clz("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper")
                .mthd("registerKeyBinding", void.class, KeyMapping.class)
                .invk(null, TOGGLE_ENABLED);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean wasPressed() {
        return (boolean) R.clz(KeyMapping.class).inst(TOGGLE_ENABLED.unwrap())
            .fld("wasPressed", boolean.class).get();
    }
}
```

### 4. Field Access via Reflection

```java
// Getting a field from a wrapper instance
ToastManager tm = (ToastManager) R.clz(Minecraft.class).inst(mc.unwrap())
    .fld("toastManager", ToastManager.class).get();

// Setting a field (rarely needed)
R.clz(Minecraft.class).inst(mc.unwrap())
    .fld("player", LocalPlayer.class).set(newPlayer.unwrap());
```

### 5. Static Field Access (Constants)

```java
// Generated wrappers expose static getters with ()
ChatFormatting.DARK_PURPLE()
SoundEvents.ENTITY_VILLAGER_CELEBRATE()

// For inner class constants with generator bugs:
Object worldBackup = R.clz("net.minecraft.client.toast.SystemToast$Type")
    .fld("WORLD_BACKUP", SystemToast$Type.class).get();
```

### 6. Mixin Targets

- **Remapped classes** (MC classes): Keep `@Mixin(ClientPlayNetworkHandler.class)` — Fabric Loader remaps
- **Non-remapped classes** (mod classes): Use `@Mixin(targets = "xaero.hud.packet.basic.ClientboundRulesPacket$ClientHandler", remap = false)`

### 7. YACL / ModMenu Compatibility

Keep using **vanilla `Screen` and `Text`** for YACL/ModMenu APIs:
```java
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public static Screen create(Screen parent) {
    return YetAnotherConfigLib.createBuilder()
        .title(Text.literal("Title"))
        .category(ConfigCategory.createBuilder()
            .name(Text.literal("Category"))
            .build())
        .build()
        .generateScreen(parent);
}
```

ModMenu factory:
```java
public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
    return parent -> ConfigScreen.create(parent);
}
```

---

## Fix Generated Wrapper Issues

### Known Generator Bugs (Version't 1.3.4)

| Bug | Workaround |
|-----|------------|
| `KeyMapping$Category.MISC()` returns `KeyMapping.Category` (wrong inner class ref) | Don't use inner class wrapper; access via `R.clz("KeyMapping$Category").fld("MISC", ...)` |
| `SystemToast$Type.WORLD_BACKUP()` returns `SystemToast.Type` (wrong) | Same — use `R.clz("SystemToast$Type").fld("WORLD_BACKUP", ...)` |
| Inner class wrappers have import errors in generated code | Remove inner class definitions from mapping; use reflection directly |

### Fix: Remove Problematic Inner Classes from Mapping

```text
# REMOVE these from mapping:
# import net.minecraft.class_304$class_11900/...
# import net.minecraft.class_370$class_9037/...
# class KeyMapping$Category ...
# class SystemToast$Type ...
```

Then access constants via `R` API directly.

---

## Build & Test

### 1. Generate Wrappers & Compile

```bash
./gradlew compileJava --no-daemon
```

Watch for:
- `versiontLayer` task runs Node.js `generate-layer.js`
- Generated sources in `build/generated/sources/versiont/java/dev/gxlg/versiont/gen/`
- Any "cannot find symbol" = mapping missing a class/method/field

### 2. Full Build

```bash
./gradlew build --no-daemon
```

### 3. Run Client (Test on MC 1.21)

```bash
./gradlew runClient --no-daemon
```

### 4. Test Other Versions

Change `minecraft` and `mappings` in `build.gradle`:
```gradle
// For 1.21.11
minecraft "com.mojang:minecraft:1.21.11"
mappings  "net.fabricmc:yarn:1.21.11+build.1:v2"

// For 26.3
minecraft "com.mojang:minecraft:26.3"
mappings  "net.fabricmc:yarn:26.3+build.1:v2"
```

Then rebuild and runClient. Version't runtime handles the rest.

---

## Common Pitfalls & Solutions

### "Cannot find symbol: class X" in generated code
→ Add missing class to `versiont.mapping` imports + class definition.

### "Method X not found" at runtime
→ Check method signature in mapping matches actual intermediary/mojang names exactly.

### "RField.get() requires no arguments" 
→ Call `.get()` on an `RInstance` (bound to object), not on `RClass.fld()` directly:
```java
// WRONG
R.clz(MyClass.class).fld("field", Type.class).get()

// CORRECT
R.clz(MyClass.class).inst(wrapper.unwrap()).fld("field", Type.class).get()
```

### "RConstructor.newInst() returns RInstance, not Wrapper"
→ Wrap it: `R.wrapperInst(WrapperClass.class, constructor.newInst(...))`

### "Throwable must be caught" from `.invk()`
→ Wrap in try-catch or declare throws:
```java
try {
    R.clz("...").mthd("...").invk(...);
} catch (Throwable e) {
    throw new RuntimeException(e);
}
```

### YACL/ModMenu type mismatches
→ Use vanilla `Screen`/`Text` for YACL/ModMenu APIs, not Version't wrappers.

### Mixin target not found on newer MC
→ Verify `@Mixin` target class name uses Yarn 1.21 name; Fabric Loader remaps it.

---

## Checklist for Other Projects

### Before Starting
- [ ] Identify oldest MC version to support (compile base)
- [ ] List all MC classes/methods/fields your mod uses
- [ ] Identify version breaks (constructor changes, renames, removals)

### Setup
- [ ] Add gXLg Maven repo to settings.gradle
- [ ] Apply `dev.gxlg.versiont-toolchain` plugin
- [ ] Add `versiont { mapping = file('versiont.mapping') }` to build.gradle
- [ ] Add `include "dev.gxlg:versiont-library:1.2.3"` dependency
- [ ] Set Java toolchain to match your JDK
- [ ] Bump Fabric Loader to 0.18.x (required by Version't)

### Mapping
- [ ] Create `versiont.mapping` with all MC classes
- [ ] Use forward-slash paths in imports
- [ ] Include all fallback names (intermediary/mojang/yarn)
- [ ] Define static fields with `()` getter syntax
- [ ] Define constructors with `<init>(params...)`
- [ ] Add `@Nullable` for nullable fields
- [ ] **Omit inner classes** with known generator bugs

### Source Migration
- [ ] Replace all MC imports with generated wrappers
- [ ] Keep vanilla imports for YACL/ModMenu/Fabric API interfaces
- [ ] Update all method calls to wrapper signatures
- [ ] Use `V.higher()` for version-specific dispatch
- [ ] Use `R.clz().inst().fld().get()` for field access
- [ ] Use `R.clz().constr().newInst()` + `R.wrapperInst()` for construction
- [ ] Wrap all `R` API calls in try-catch for `Throwable`

### Build & Verify
- [ ] `./gradlew compileJava` — no errors
- [ ] `./gradlew build` — full build passes
- [ ] `./gradlew runClient` — works on compile-base version
- [ ] Test on 1-2 newer versions by changing `minecraft`/`mappings`

### Documentation
- [ ] Document your versiont.mapping decisions
- [ ] Record any custom adapters needed
- [ ] Note generator bugs encountered

---

## Version't API Quick Reference

```java
// Class lookup (fallback chain: "intermediary/mojang/yarn")
R.clz("net.minecraft.class_310/net/minecraft/client/MinecraftClient/net/minecraft/client/Minecraft")

// Method invocation
R.clz("ClassName").mthd("methodName", ReturnType.class, ParamTypes...).invk(instance, args...)

// Static method
R.clz("ClassName").mthd("staticMethod", ReturnType.class, ParamTypes...).invk(null, args...)

// Field get (on instance)
R.clz("ClassName").inst(wrapper.unwrap()).fld("fieldName", Type.class).get()

// Field set
R.clz("ClassName").inst(wrapper.unwrap()).fld("fieldName", Type.class).set(value)

// Static field get
R.clz("ClassName").fld("CONSTANT", Type.class).get()

// Constructor
R.clz("ClassName").constr(ParamTypes...).newInst(args...)

// Wrap raw object in generated wrapper
R.wrapperInst(GeneratedWrapper.class, rawObject)

// Unwrap wrapper to raw object
wrapper.unwrap()

// Version check (runtime)
V.higher("1.21.10")  // true if running on 1.21.11+
```

---

## Files Modified in XDRF Migration

| File | Changes |
|------|---------|
| `build.gradle` | Version't plugin, deps, Java 21 toolchain |
| `settings.gradle` | gXLg Maven repo |
| `versiont.mapping` | Complete mapping (created) |
| `XaeroDisabledRadarFixerKeybinds.java` | Version't KeyMapping dispatch |
| `XaeroDisabledRadarFixerMixin.java` | Wrapper imports, R field access |
| `XaeroDisabledRadarFixerRulesMixin.java` | Wrapper imports, R field access |
| `XaeroDisabledRadarFixerService.java` | Wrapper imports |
| `XaeroDisabledRadarFixerConfigScreen.java` | Vanilla Screen/Text for YACL |
| `XaeroDisabledRadarFixerModMenu.java` | Vanilla Screen factory |

---

## Result

- **Single source tree** compiles and runs on MC 1.21 through 26.3
- **No version-specific source sets**
- **No duplicate implementations**
- **Version't handles all API differences at runtime**
- **Clean, maintainable codebase**

This pattern can be applied to any Fabric client mod needing cross-version compatibility.