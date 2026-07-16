# Version't Quick Reference Cheatsheet

## One-Page Summary for Cross-Version Migration

---

## Build Setup (2 files)

**settings.gradle:**
```gradle
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url 'https://maven.fabricmc.net/' }
        maven { url 'https://maven.gxlg.dev/releases' }
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

**build.gradle:**
```gradle
plugins {
    id 'fabric-loom' version '1.17.11'
    id 'dev.gxlg.versiont-toolchain' version '1.3.4'
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
versiont { mapping = file('versiont.mapping') }

dependencies {
    minecraft "com.mojang:minecraft:1.21"
    mappings  "net.fabricmc:yarn:1.21+build.1:v2"
    modImplementation "net.fabricmc:fabric-loader:0.18.6"
    // NO Fabric API compile dep
    modImplementation "dev.isxander:yet-another-config-lib:3.6.2+1.21-fabric"
    modImplementation "com.terraformersmc:modmenu:11.0.0"
    include "dev.gxlg:versiont-library:1.2.3"
}
tasks.named('sourcesJar') { dependsOn tasks.named('versiontLayer') }
```

---

## versiont.mapping Template

```text
# Import: use FORWARD SLASHES for flat generated packages
import net.minecraft.class_XXX/net/minecraft/.../ClassName/next/version/ClassName

# Class definition
class SimpleName
    static ReturnType methodName(ParamTypes...)
    @Nullable FieldType fieldName
    <init>(ParamTypes...)
    static final FieldType CONSTANT_NAME
```

**Key Rules:**
- Last slash segment = generated class simple name
- Static fields → `static final Type NAME` (generates `NAME()` getter)
- Constructors → `<init>(params...)` no return type
- Field accessors → explicit method names

---

## Source Migration Patterns

| MC Code | Version't Wrapper |
|---------|-------------------|
| `MinecraftClient.getInstance()` | `Minecraft.getInstance()` |
| `mc.player` | `mc.getPlayerField()` |
| `mc.getToastManager()` | `mc.getToastManagerField()` |
| `mc.getNetworkHandler()` | `mc.getNetworkHandlerField()` |
| `Text.literal("x")` | `Component.literal("x")` |
| `text.formatted(Formatting.RED)` | `text.formatted(ChatFormatting.RED())` |
| `SoundEvents.ENTITY_X` | `SoundEvents.ENTITY_X()` |
| `SystemToast.add(tm, type, t1, t2)` | `SystemToast.add(tm, null, t1, t2)` |
| `packet.content()` | `packet.getContentField()` |

---

## R API Patterns

```java
// Class lookup (fallback chain)
R.clz("intermediary/mojang/yarn")

// Instance method call
R.clz("Class").mthd("method", Ret.class, Param...).invk(wrapper.unwrap(), args...)

// Static method
R.clz("Class").mthd("staticMethod", Ret.class, Param...).invk(null, args...)

// Field get (MUST use .inst() on wrapper)
R.clz("Class").inst(wrapper.unwrap()).fld("field", Type.class).get()

// Field set
R.clz("Class").inst(wrapper.unwrap()).fld("field", Type.class).set(value)

// Static field
R.clz("Class").fld("CONSTANT", Type.class).get()

// Constructor + wrap
R.wrapperInst(Wrapper.class, R.clz(Wrapper.class).constr(Param...).newInst(args...))

// Version check
V.higher("1.21.10")
```

---

## Version-Specific Dispatch

```java
if (V.higher("1.21.10")) {
    // 1.21.11+ code path
} else {
    // Pre-1.21.11 code path
}
```

---

## Common Fixes

**RField.get() needs instance:**
```java
// WRONG
R.clz(Minecraft.class).fld("toastManager", ToastManager.class).get()

// CORRECT  
R.clz(Minecraft.class).inst(mc.unwrap()).fld("toastManager", ToastManager.class).get()
```

**Constructor returns RInstance, wrap it:**
```java
R.wrapperInst(KeyMapping.class, R.clz(KeyMapping.class).constr(...).newInst(...))
```

**Catch Throwable:**
```java
try { R.clz("...").mthd("...").invk(...); } catch (Throwable e) { throw new RuntimeException(e); }
```

---

## Generator Bugs (1.3.4)

| Class | Issue | Workaround |
|-------|-------|------------|
| `KeyMapping$Category` | `MISC()` returns wrong type | Use `R.clz("KeyMapping$Category").fld("MISC", ...)` |
| `SystemToast$Type` | `WORLD_BACKUP()` returns wrong type | Use `R.clz("SystemToast$Type").fld("WORLD_BACKUP", ...)` |

**Fix: Remove inner classes from mapping, use R API directly.**

---

## YACL/ModMenu: Keep Vanilla

```java
import net.minecraft.client.gui.screen.Screen;  // NOT Version't wrapper
import net.minecraft.text.Text;                 // NOT Version't wrapper

public static Screen create(Screen parent) { ... }
public ConfigScreenFactory<Screen> getModConfigScreenFactory() { ... }
```

---

## Testing Other Versions

```gradle
// build.gradle - change these:
minecraft "com.mojang:minecraft:1.21.11"
mappings  "net.fabricmc:yarn:1.21.11+build.1:v2"

// or 26.3:
minecraft "com.mojang:minecraft:26.3"
mappings  "net.fabricmc:yarn:26.3+build.1:v2"
```

Then: `./gradlew build runClient`

---

## Debug Commands

```bash
# Clean & regenerate
./gradlew clean versiontLayer compileJava --no-daemon

# Full build
./gradlew build --no-daemon

# Run client
./gradlew runClient --no-daemon

# View generated wrappers
ls build/generated/sources/versiont/java/dev/gxlg/versiont/gen/
```

---

## Checklist Per Project

- [ ] Add gXLg repo + Version't plugin
- [ ] Java 21 toolchain, Fabric Loader 0.18.6
- [ ] Create versiont.mapping with ALL MC APIs used
- [ ] Forward slashes in imports
- [ ] Static fields with `()` getter syntax
- [ ] No inner classes with bugs
- [ ] Replace MC imports → generated wrappers
- [ ] Keep vanilla Screen/Text for YACL/ModMenu
- [ ] Use R API for all field/method/constructor access
- [ ] V.higher() for version dispatch
- [ ] Try-catch all R calls
- [ ] Build passes on compile-base (1.21)
- [ ] Test on 1+ newer version