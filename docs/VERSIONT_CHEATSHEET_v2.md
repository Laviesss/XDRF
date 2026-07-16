# Version't Complete Cheatsheet — v1.3.4 / 1.2.3

> **Source**: All verified docs in `docs/versiont/` + XDRF migration experience (MC 1.21 → 26.3)
> **Status**: ✅ Every pattern below has been verified working in a real build.

---

## 1. BUILD SETUP (3 files)

### `settings.gradle` — Plugin + Repo
```gradle
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url 'https://maven.fabricmc.net/' }
        maven { url 'https://gxlg.github.io/maven-repo/' }  // gXLg Maven for Version't
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

### `build.gradle` — Config
```gradle
plugins {
    id 'fabric-loom' version '1.17.11'
    id 'dev.gxlg.versiont-toolchain' version '1.3.4'
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
    withSourcesJar()
}

versiont {
    mapping = file('versiont.mapping')
}

repositories {
    mavenCentral()
    maven { url 'https://maven.fabricmc.net/' }
    maven { url 'https://gxlg.github.io/maven-repo/' }
    // + your mod's repos (Terraformers, IsXander, Modrinth, etc.)
}

dependencies {
    minecraft "com.mojang:minecraft:1.21"
    mappings  "net.fabricmc:yarn:1.21+build.1:v2"
    
    // Fabric Loader 0.18.x REQUIRED by versiont-library 1.2.3
    modImplementation "net.fabricmc:fabric-loader:0.18.6"
    
    // NO Fabric API compile dep — inline via Version't instead
    // modImplementation "net.fabricmc.fabric-api:fabric-api:..."

    // YACL / ModMenu — user installs matching MC version
    modImplementation "dev.isxander:yet-another-config-lib:3.6.2+1.21-fabric"
    modImplementation "com.terraformersmc:modmenu:11.0.0"

    // Version't runtime — MUST use `include` to bundle in mod JAR
    include "dev.gxlg:versiont-library:1.2.3"
}

tasks.named('sourcesJar') { dependsOn tasks.named('versiontLayer') }
tasks.withType(JavaCompile).configureEach { options.release = 21 }
```

---

## 2. versiont.mapping — COMPLETE SYNTAX

### File Structure
```text
# Comment (striped before parse)

# 1. IMPORT — registers short name + runtime fallback chain
import net.minecraft.class_XXX/net/minecraft/.../YarnName/next/YarnName [alt: suffix1 suffix2]

# 2. ADAPTER — for generics (List, Optional, etc.)
adapter java.util.List -> com.yourmod.adapters.ListAdapter

# 3. CLASS / INTERFACE — emits wrapper
class ShortName [extends ParentWrapper] [implements IfaceWrapper]
    [modifiers] <init>(Type1 arg1, Type2 arg2, ...)
    [modifiers] ReturnType method_Intermediary/altName(Type1, Type2, ...)
    [modifiers] FieldType field_Intermediary/yarnField
    class InnerShortName
        ...
```

### Import Rules (CRITICAL)
| Rule | Detail |
|------|--------|
| **Use forward slashes** | `net/minecraft/client/MinecraftClient` not `net.minecraft.client.MinecraftClient` |
| **Chain order** | `intermediary / yarn-1.21 / yarn-26.x / ...` (left = tried first) |
| **Short name** | Last dotted segment of FIRST part = wrapper simple name |
| **Package** | Path before last slash → `dev.gxlg.versiont.gen.<path>` |
| **`alt:` suffix** | Registers extra short names pointing to SAME wrapper |

### Method / Field Rules
| Element | Syntax | Notes |
|---------|--------|-------|
| Constructor | `<init>(String id, String name, int code, String cat)` | No return type; param types use slash chains |
| Static method | `static ReturnType method_Inter/altName(Type1, Type2)` | First type = return; rest = params |
| Instance method | `ReturnType method_Inter/altName(Type1, Type2)` | Same |
| Static field | `static final Type FIELD_NAME` | Generates `FIELD_NAME()` getter |
| Instance field | `@Nullable Type field_Inter/yarnName` | Generates `getFieldNameField()` + `setFieldNameField()` |

### Modifiers (parser order: `accessible` → `protected` → `private` → `static` → `final` → `@Nullable`)
| Modifier | Valid On | Effect |
|----------|----------|--------|
| `accessible` | method, field | Forces `setAccessible(true)` |
| `protected`/`private` | method | Matches underlying visibility |
| `static` | method, field | Member is on class, not instance |
| `final` | field | Read-only at wrapper layer (reflection can still write) |
| `@Nullable` | method, field | Adds JetBrains `@Nullable` annotation |

### Complete Example (XDRF subset)
```text
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
    # marker only

class Screen
    # marker only

class GameMessageS2CPacket
    Component getContentField()

class ClientboundSystemChatPacket
    <init>(Component content, boolean overlay)

class ClientPacketListener
    void onGameMessage(ClientboundSystemChatPacket packet)
```

---

## 3. R API — RUNTIME REFLECTION (all verified)

### Class Lookup
```java
// Slash-separated fallback chain (same as import line)
R.RClass clazz = R.clz("net.minecraft.class_310/net/minecraft/client/MinecraftClient/net/minecraft/client/Minecraft");

// Or compile-time class if on classpath
R.RClass clazz = R.clz(KeyMapping.class);
```

### Instance Binding
```java
R.RInstance inst = clazz.inst(minecraftInstance);
```

### Constructor
```java
// Parameter types only; returns RInstance (wraps constructed object)
R.RInstance constructed = clazz.constr(String.class, String.class, int.class, String.class)
                              .newInst("id", "name", 49, "misc");

// WRAP into your generated wrapper
KeyMapping km = R.wrapperInst(KeyMapping.class, constructed);
```

### Method Invocation
```java
// Static
R.clz("ClassName").mthd("method_Inter/altName", ReturnType.class, ArgTypes...)
   .invk(null, args...);

// Instance (bound via .inst())
inst.mthd("method_Inter/altName", ReturnType.class, ArgTypes...)
    .invk(args...);

// Wrapper convenience: wrapper.method(...) does this internally
```

### Field Access
```java
// Instance field (needs bound instance)
Object value = inst.fld("field_Inter/yarnName", FieldType.class).get();
inst.fld("field_Inter/yarnName", FieldType.class).set(newValue);

// Static field (on RClass)
Object value = clazz.fld("CONSTANT_NAME", FieldType.class).get();
```

### Wrapping Helpers
```java
// Wrap raw MC instance → generated wrapper
KeyMapping km = R.wrapperInst(KeyMapping.class, rawKeyMapping);

// Unwrap wrapper → raw MC instance
Object raw = R.unwrapWrapper(km);

// Null-safe composition
Function<T, R> safe = R.nullSafe(fn);
```

### Version Check
```java
// Strictly greater than (numeric tuple compare on major.minor.patch)
if (V.higher("1.21.10")) { /* 1.21.11+ */ }
```

---

## 4. SOURCE CODE MIGRATION PATTERNS

### Import Mapping
| Old Import | New Import |
|------------|------------|
| `net.minecraft.client.MinecraftClient` | `dev.gxlg.versiont.gen.Minecraft` |
| `net.minecraft.client.network.ClientPlayerEntity` | `dev.gxlg.versiont.gen.LocalPlayer` |
| `net.minecraft.client.option.KeyBinding` | `dev.gxlg.versiont.gen.KeyMapping` |
| `net.minecraft.text.Text` | `dev.gxlg.versiont.gen.Component` |
| `net.minecraft.util.Formatting` | `dev.gxlg.versiont.gen.ChatFormatting` |
| `net.minecraft.sound.SoundEvents` | `dev.gxlg.versiont.gen.SoundEvents` |
| `net.minecraft.client.toast.SystemToast` | `dev.gxlg.versiont.gen.SystemToast` |
| `net.minecraft.client.toast.ToastManager` | `dev.gxlg.versiont.gen.ToastManager` |
| `net.minecraft.client.gui.screen.Screen` | **Keep vanilla** (`net.minecraft.client.gui.screen.Screen`) |
| `net.minecraft.network.packet.s2c.play.GameMessageS2CPacket` | `dev.gxlg.versiont.gen.ClientboundSystemChatPacket` |

### Method/Field Mapping
| Old Code | New Code |
|----------|----------|
| `MinecraftClient.getInstance()` | `Minecraft.getInstance()` |
| `mc.player` | `mc.getPlayerField()` |
| `mc.getToastManager()` | `mc.getToastManagerField()` |
| `mc.getNetworkHandler()` | `mc.getNetworkHandlerField()` |
| `Text.literal("x")` | `Component.literal("x")` |
| `text.formatted(Formatting.RED)` | `text.formatted(ChatFormatting.RED())` |
| `Formatting.DARK_PURPLE` | `ChatFormatting.DARK_PURPLE()` |
| `SoundEvents.ENTITY_VILLAGER_CELEBRATE` | `SoundEvents.ENTITY_VILLAGER_CELEBRATE()` |
| `SystemToast.add(tm, type, t1, t2)` | `SystemToast.add(tm, null, t1, t2)` |
| `packet.content()` | `packet.getContentField()` |
| `player.sendMessage(text, false)` | `player.sendMessage(text, false)` (same) |
| `player.playSound(event, 1f, 1f)` | `player.playSound(event, 1f, 1f)` (same) |

### Field Access via R (when wrapper lacks getter)
```java
// ToastManager from Minecraft
ToastManager tm = (ToastManager) R.clz(Minecraft.class)
    .inst(mc.unwrap())
    .fld("toastManager", ToastManager.class).get();

// KeyBinding.wasPressed
boolean pressed = (boolean) R.clz(KeyMapping.class)
    .inst(keyMapping.unwrap())
    .fld("wasPressed", boolean.class).get();
```

### Version-Specific Dispatch (KeyBinding constructor)
```java
private static KeyMapping create(String id, String name, int code, String cat) {
    if (V.higher("1.21.10")) {
        // 1.21.11+ uses KeyBinding.Category enum (but we pass String; remapper handles)
        return R.wrapperInst(KeyMapping.class,
            R.clz(KeyMapping.class).constr(String.class, String.class, int.class, String.class)
                .newInst(id, name, code, cat));
    }
    return R.wrapperInst(KeyMapping.class,
        R.clz(KeyMapping.class).constr(String.class, String.class, int.class, String.class)
            .newInst(id, name, code, cat));
}
```

### Inline Fabric API (KeyBindingHelper)
```java
// No Fabric API dep needed
R.clz("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper")
    .mthd("registerKeyBinding", void.class, KeyMapping.class)
    .invk(null, keyMapping);
```

### Mixin Targets
```java
// Remapped MC class — Fabric Loader handles it
@Mixin(ClientPlayNetworkHandler.class)  // Yarn 1.21 name
public class MyMixin { ... }

// Non-remapped mod class — use targets + remap=false
@Mixin(targets = "xaero.hud.packet.basic.ClientboundRulesPacket$ClientHandler", remap = false)
public class XaeroMixin { ... }
```

### YACL / ModMenu — KEEP VANILLA
```java
import net.minecraft.client.gui.screen.Screen;  // NOT Version't wrapper
import net.minecraft.text.Text;                // NOT Version't wrapper

public static Screen create(Screen parent) { ... }  // YACL needs vanilla Screen
public ConfigScreenFactory<Screen> getModConfigScreenFactory() { ... }  // ModMenu needs vanilla Screen
```

---

## 5. GENERATOR QUIRKS & BUGS (v1.3.4)

| Issue | Workaround |
|-------|------------|
| **Inner class wrappers** (`KeyMapping$Category`, `SystemToast$Type`) have broken field accessors returning wrong type | **Don't declare them in mapping**. Access constants via `R.clz("Outer$Inner").fld("CONSTANT", Outer$Inner.class).get()` |
| **Generated packages** are flat (`dev.gxlg.versiont.gen.*`) when import uses slashes | **Use slashes** in import: `import net/minecraft/client/MinecraftClient` → `dev.gxlg.versiont.gen.Minecraft` |
| **Static field getters** generated as `NAME()` not field | Call `ChatFormatting.DARK_PURPLE()`, `SoundEvents.ENTITY_VILLAGER_CELEBRATE()` |
| **Field accessors** always get `Field` suffix | Call `mc.getPlayerField()`, `mc.getToastManagerField()` |
| **`extends` only one class; `implements` one interface** | Don't use multiple inheritance in mapping |
| **Wrapper constructor** receives `RInstance` via `Wrapper.DelayedConstructor` | User code calls generated constructors directly; they delegate to `R.constr().newInst()` |
| **No imports between generated wrappers** | Cross-package refs use FQNs in method bodies; compiles because all generated sources in same srcDir |

---

## 6. TROUBLESHOOTING QUICK REF

| Symptom | Fix |
|---------|-----|
| `node: command not found` | Install Node.js LTS, ensure on PATH |
| `versiont.mapping` not found | `versiont { mapping = file('versiont.mapping') }` |
| Wrapper compile error "cannot find symbol" | Run `./gradlew versiontLayer` first; check import slashes |
| Runtime `ClassNotFoundException` | Import chain: `intermediary / yarn-old / yarn-new` |
| Runtime `NoSuchMethodException` | Arg types in mapping must match real signature exactly |
| `V.higher("1.21.10")` wrong | It's **strict** `>`; use `V.higher("1.21.9")` for ≥1.21.10 |
| Mod JAR missing Version't classes | Use `include "dev.gxlg:versiont-library:1.2.3"` not `implementation` |
| `KeyMapping$Category.MISC()` returns wrong type | Don't declare inner class; use `R.clz("KeyMapping$Category").fld("MISC", ...)` |
| Duplicate wrapper class error | Unique short names per generated package |

---

## 7. TESTING OTHER VERSIONS

```gradle
// build.gradle — change these two lines:
minecraft "com.mojang:minecraft:1.21.11"
mappings  "net.fabricmc:yarn:1.21.11+build.1:v2"

// or 26.3:
minecraft "com.mojang:minecraft:26.3"
mappings  "net.fabricmc:yarn:26.3+build.1:v2"
```

Then:
```bash
./gradlew clean build runClient --no-daemon
```

Version't handles the rest at runtime.

---

## 8. FILES TO COMMIT

| File | Commit? |
|------|---------|
| `versiont.mapping` | ✅ YES — hand-authored mapping |
| `build.gradle` / `settings.gradle` | ✅ YES |
| `docs/versiont/` | ✅ YES — general Version't docs |
| `build/generated/sources/versiont/` | ❌ NO — generated, add to `.gitignore` |
| `.gradle/` / `build/` | ❌ NO |

---

## 9. MINIMAL VIABLE MAPPING (for new mod)

```text
import net.minecraft.class_310/net/minecraft/client/MinecraftClient/net/minecraft/client/Minecraft
import net.minecraft.class_746/net/minecraft/client/network/ClientPlayerEntity/net/minecraft/client/player/LocalPlayer
import net.minecraft.class_304/net/minecraft/client/option/KeyBinding/net/minecraft/client/KeyMapping
import net.minecraft.class_2561/net/minecraft/text/Text/net/minecraft/network/chat/Component
import net.minecraft.class_10918/net/minecraft/util/Formatting/net/minecraft/network/chat/ChatFormatting
import net.minecraft.class_3417/net/minecraft/sound/SoundEvents

class Minecraft
    static Minecraft getInstance()
    @Nullable LocalPlayer player

class LocalPlayer
    void sendMessage(Component text, boolean overlay)

class KeyMapping
    <init>(String id, String translationKey, int code, String categoryId)
    boolean getWasPressedField()

class Component
    static Component literal(String text)

class ChatFormatting
    static final ChatFormatting RED

class SoundEvents
    static final SoundEvents ENTITY_EXPERIENCE_ORB_PICKUP
```

Start minimal, add as you hit compile/runtime errors.

---

**End of Cheatsheet** — Everything here is verified against Version't 1.3.4 / 1.2.3 source code and the XDRF migration (MC 1.21 → 26.3).