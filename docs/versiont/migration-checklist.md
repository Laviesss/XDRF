# Version't Migration Checklist

Complete step-by-step adoption plan for existing Fabric mods.

---

## Pre-Migration

- [ ] **Commit current state** — `git commit -am "pre-versiont baseline"`
- [ ] **Verify requirements**:
  - [ ] Node.js LTS installed (`node --version`)
  - [ ] JDK 21+ for runtime, JDK 25 for Gradle (or configure toolchain)
  - [ ] Fabric Loader 0.18.x in `build.gradle`
  - [ ] Git clean (no uncommitted changes you can't afford to lose)
- [ ] **Read core docs**: `introduction.md`, `project-setup.md`, `mapping-language.md`

---

## Phase 1: Add Build Infrastructure

- [ ] **`settings.gradle`** — Add gXLg Maven repo + plugin resolution:
  ```gradle
  pluginManagement {
      repositories { gradlePluginPortal(); maven { url 'https://gxlg.github.io/maven-repo/' } }
      resolutionStrategy {
          eachPlugin { if (requested.id.id == 'dev.gxlg.versiont-toolchain') useModule("dev.gxlg:versiont-toolchain:1.3.4") }
      }
  }
  ```
- [ ] **`build.gradle`** — Apply plugin + config:
  ```gradle
  plugins { id 'dev.gxlg.versiont-toolchain' version '1.3.4' }
  versiont { mapping = file('versiont.mapping') }
  ```
- [ ] **`build.gradle`** — Update repositories + deps:
  ```gradle
  repositories { maven { url 'https://gxlg.github.io/maven-repo/' } }
  dependencies {
      minecraft "com.mojang:minecraft:1.21"
      mappings  "net.fabricmc:yarn:1.21+build.1:v2"
      modImplementation "net.fabricmc:fabric-loader:0.18.6"
      // REMOVE Fabric API compile dep — inline via Version't
      modImplementation "dev.isxander:yet-another-config-lib:3.6.2+1.21-fabric"
      modImplementation "com.terraformersmc:modmenu:11.0.0"
      include "dev.gxlg:versiont-library:1.2.3"
  }
  ```
- [ ] **`build.gradle`** — Java toolchain + sourcesJar:
  ```gradle
  java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
  tasks.named('sourcesJar') { dependsOn tasks.named('versiontLayer') }
  ```
- [ ] **Test**: `./gradlew versiontLayer` — should print "Version't layer generated!"

---

## Phase 2: Create Initial Mapping

- [ ] **Create `versiont.mapping`** at project root
- [ ] **Start minimal** — only classes you know are used:
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
- [ ] **Test**: `./gradlew clean versiontLayer compileJava` — must pass

---

## Phase 3: Audit & Expand Mapping

- [ ] **Find all MC imports** in source:
  ```bash
  grep -r "import net.minecraft" src/main/java --include="*.java" | \
    sed 's/.*import \(net\.minecraft\.[^;]*\).*/\1/' | sort -u
  ```
- [ ] **For each import**, add to `versiont.mapping`:
  - [ ] `import` line with intermediary/yarn/yarn-next chain
  - [ ] `class` declaration with methods/fields actually used
- [ ] **Special cases**:
  - [ ] Mixin targets (e.g. `ClientPlayNetworkHandler`) — **keep vanilla import**, don't wrap
  - [ ] Fabric API classes (e.g. `KeyBindingHelper`) — inline via `R.clz("fabric.api...")`
  - [ ] YACL/ModMenu types (`Screen`, `Text`) — **keep vanilla imports**
  - [ ] Inner classes with buggy generators — omit; use `R.clz("Outer$Inner")`
- [ ] **Test**: `./gradlew clean versiontLayer compileJava` after each batch

---

## Phase 4: Migrate Source Code

### 4.1 Update Imports
| Old | New |
|-----|-----|
| `net.minecraft.client.MinecraftClient` | `dev.gxlg.versiont.gen.Minecraft` |
| `net.minecraft.client.network.ClientPlayerEntity` | `dev.gxlg.versiont.gen.LocalPlayer` |
| `net.minecraft.client.option.KeyBinding` | `dev.gxlg.versiont.gen.KeyMapping` |
| `net.minecraft.text.Text` | `dev.gxlg.versiont.gen.Component` |
| `net.minecraft.util.Formatting` | `dev.gxlg.versiont.gen.ChatFormatting` |
| `net.minecraft.sound.SoundEvents` | `dev.gxlg.versiont.gen.SoundEvents` |
| `net.minecraft.client.toast.SystemToast` | `dev.gxlg.versiont.gen.SystemToast` |
| `net.minecraft.client.toast.ToastManager` | `dev.gxlg.versiont.gen.ToastManager` |
| `net.minecraft.network.packet.s2c.play.GameMessageS2CPacket` | `dev.gxlg.versiont.gen.ClientboundSystemChatPacket` |
| **Keep**: `net.minecraft.client.gui.screen.Screen`, `net.minecraft.text.Text` (YACL/ModMenu) |

### 4.2 Fix Method Calls
| Old | New |
|-----|-----|
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

### 4.3 Version-Specific Dispatch
```java
// KeyBinding constructor changed at 1.21.11
if (V.higher("1.21.10")) {
    return R.wrapperInst(KeyMapping.class,
        R.clz(KeyMapping.class).constr(String.class, String.class, int.class, String.class)
            .newInst(id, name, code, cat));
}
return R.wrapperInst(KeyMapping.class,
    R.clz(KeyMapping.class).constr(String.class, String.class, int.class, String.class)
        .newInst(id, name, code, cat));
```

### 4.4 Inline Fabric API
```java
// Instead of Fabric API dep
R.clz("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper")
    .mthd("registerKeyBinding", void.class, KeyMapping.class)
    .invk(null, keyMapping);
```

### 4.5 Field Access via R
```java
ToastManager tm = (ToastManager) R.clz(Minecraft.class)
    .inst(mc.unwrap())
    .fld("toastManager", ToastManager.class).get();

boolean pressed = (boolean) R.clz(KeyMapping.class)
    .inst(keyMapping.unwrap())
    .fld("wasPressed", boolean.class).get();
```

### 4.6 Keep Vanilla for YACL/ModMenu
```java
// Config screen factory — needs real Screen
public static net.minecraft.client.gui.screen.Screen create(
    net.minecraft.client.gui.screen.Screen parent) { ... }

@Nullable
@Override
public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return parent -> ConfigScreen.create(parent);
}
```

### 4.7 Wrap All R Calls in Try/Catch
```java
try {
    R.clz("...").mthd(...).invk(...);
} catch (Throwable e) {
    throw new RuntimeException(e);
}
```

---

## Phase 5: Build & Test

- [ ] `./gradlew clean build` — full build passes
- [ ] `./gradlew runClient` — test on MC 1.21
- [ ] **Verify features work**:
  - [ ] Keybind registration
  - [ ] Chat message blocking
  - [ ] Toast notifications
  - [ ] Sound playback
  - [ ] Config screen opens
  - [ ] ModMenu integration

---

## Phase 6: Cross-Version Validation

- [ ] **MC 1.21.11** (first constructor break):
  ```gradle
  minecraft "com.mojang:minecraft:1.21.11"
  mappings  "net.fabricmc:yarn:1.21.11+build.1:v2"
  ```
  - [ ] `./gradlew clean build runClient` — verify `V.higher("1.21.10")` path works

- [ ] **MC 26.3** (major rename):
  ```gradle
  minecraft "com.mojang:minecraft:26.3"
  mappings  "net.fabricmc:yarn:26.3+build.1:v2"
  ```
  - [ ] `./gradlew clean build runClient` — verify all wrapper fallbacks work

---

## Phase 7: Documentation & Cleanup

- [ ] **Update project README** with Version't requirement (Node.js, JDK 21+)
- [ ] **Document any custom adapters** created
- [ ] **Add mapping notes** for future maintainers
- [ ] **CI configuration** — ensure Node.js + JDK 25 in build matrix
- [ ] **Tag release**: `git tag v2.1.0-universal`

---

## Rollback Plan

If critical issue found:
```bash
git checkout pre-versiont-baseline -- .
./gradlew clean build  # verify original builds
```

---

## Quick Reference: Files Modified

| File | Purpose |
|------|---------|
| `settings.gradle` | gXLg repo + plugin resolution |
| `build.gradle` | Version't plugin, deps, toolchain |
| `versiont.mapping` | **Core** — all MC API declarations |
| `src/main/java/.../*.java` | All MC imports → Version't wrappers |
| `.gitignore` | Add `build/generated/sources/versiont/` |

---

## Success Criteria

- [ ] Single source tree builds on 1.21, 1.21.11, 26.3
- [ ] No version-specific source sets
- [ ] No duplicate feature implementations
- [ ] All original features functional
- [ ] CI passes on all target versions

---

**Estimated Effort**: 4-8 hours for small mod (XDRF ~6 hrs) | Scales with MC API surface area