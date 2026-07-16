# Introduction

## What Version't is

Version't is a two‑part library for Fabric Minecraft mods:

1. A **runtime reflection layer** (`dev.gxlg.versiont.api.R`, `dev.gxlg.versiont.api.V`, and a small set of helper types under `dev.gxlg.versiont.api.types`). At runtime it locates Minecraft classes, methods, fields, and constructors via `Class.forName` and `java.lang.invoke` so that code written against one set of Minecraft APIs can run against other Minecraft versions without recompilation.
2. A **Gradle toolchain plugin** (`dev.gxlg.versiont-toolchain`) that, at build time, parses a hand‑written mapping file (`versiont.mapping`) and emits strongly‑typed Java wrapper classes under `dev.gxlg.versiont.gen.*`. These wrappers call into the runtime reflection layer at the call sites they wrap.

The two parts are designed together: the wrapper classes are the intended ergonomic surface, but every wrapper method bodies `R.mthd(...)` / `R.fld(...)` rather than referencing the Minecraft class directly.

✅ Verified — present in:

- `dev/gxlg/versiont/api/R.java` (reflective resolution)
- `dev/gxlg/versiont/api/V.java` (Version comparison)
- `dev/gxlg/versiont/api/types/Wrapper.java` (wrapper base class)
- `scripts/generate-layer.js` in the toolchain (wrapper emission)

## Goals

- ✅ Verified — allow a single mod source tree to support multiple Minecraft releases.
- ✅ Verified — keep a hand‑written mapping file declarative; the toolchain does all code generation.
- ✅ Verified — let the runtime reflection layer be used *directly* as well as through generated wrappers.

## Architecture

Three components:

1. **`versiont-library`** (Maven coords `dev.gxlg:versiont-library`) — the runtime reflection layer:
   - Packaged as a Fabric mod (`fabric.mod.json`) — depended on as an `include` in the user's mod, not used as a side library directly.
   - Pulls in `net.bytebuddy:byte-buddy:1.18.4` transitively for runtime class extension (`R.extendWrapper`).

2. **`versiont-toolchain`** (Gradle plugin `dev.gxlg.versiont-toolchain`) — the wrapper generator:
   - Reads a user‑authored `versiont.mapping`.
   - Calls a bundled Node.js script `generate-layer.js` via the `versiontLayer` `Exec` task.
   - Adds the generated sources directory to the project's `main` source set and makes `compileJava` depend on `versiontLayer`.

3. **User mapping file** (`versiont.mapping`) — declarative description of each Minecraft API the user's mod touches.

## Design philosophy

- ✅ Verified — wrappers are *generated* artefacts, not maintained by hand. Every wrapper class lives in `build/generated/sources/versiont/java/dev/gxlg/versiont/gen/...`.
- ✅ Verified — the same wrapper file is the same Java class regardless of which Minecraft major runs the mod. The runtime dispatch happens via bytecode‑level `Class.forName` on the slash‑separated chain registered in the wrapper's static class.
- ✅ Verified — generated wrappers extend the abstract class `dev.gxlg.versiont.api.types.Wrapper`, which provides the delayed‑construction plumbing. Wrapper instances are bound to their wrapped Minecraft instance lazily through `Wrapper.DelayedConstructor`.

## What Version't solves

- ✅ Verified — multi‑version class renames. Example: `MinecraftClient` (1.21) → `Minecraft` (26.x). The wrapper's static class resolves through a slash‑separated chain of `Class.forName` calls so either name finds a real class at runtime.
- ✅ Verified — method/field identifier drift across versions. `mthd("a/b", …)` and `fld("a/b", …)` try each name.
- ✅ Verified — byte‑code‑stable intermediary‑name access on Mojang‑obfuscated MC JARs. The first name in the chain is conventionally the Fabric intermediary (`net.minecraft.class_xxx`).

## What Version't does NOT solve

- ⚠️ **Unverified to confirm against the spec, but plausible and worth documenting**: full bytecode remapping of class names *inside* your compiled mod. Version't wraps every reflective call with `R.clz(...)` / `R.mthd(...)`, but direct calls to Yarn‑named classes that you reference as Java type literals in your own source CAN miss on a Minecraft version whose Mojang names have moved. The Fabric Loader `RuntimeModRemapper` does the conventional bytecode remap on loaded mod bytecode; Version't does not attempt to replace it.
- ❌ **Incorrect assumption to flag**: the slashes in a mapping's lookup chain do not invent additional Java identifiers; they are runtime fallback strings consumed by `Class.forName`. Two slashes `class_X/Y/Z` only resolves at runtime, *not* at the level of Java type literals in your mod's source.

## Intended workflow

✅ Verified:

1. Author `versiont.mapping` describing every Minecraft class, method, and field the mod uses.
2. Add the Gradle plugin and Node.js prerequisite to your `build.gradle`.
3. Write mod source using only the generated wrapper classes.
4. Build. The `versiontLayer` task runs first; `compileJava` depends on it.
5. At runtime, regardless of Minecraft major, the wrappers resolve their targets via `R.clz("…intermediary…/…name1…/…name2…")` and `R.mthd("…intermediary…/…name1…/…name2…")`.

## Verified outcomes observed end‑to‑end

After authoring `versiont.mapping` with slash‑separated fallback names and running `./gradlew versiontLayer`:

```
> Task :versiontLayer
Generated dev.gxlg.versiont.gen.java.lang.Object
Generated dev.gxlg.versiont.gen.MinecraftClient
Generated dev.gxlg.versiont.gen.ClientPlayNetworkHandler
Generated dev.gxlg.versiont.gen.ClientPlayerEntity
Generated dev.gxlg.versiont.gen.GameMessageS2CPacket
Generated dev.gxlg.versiont.gen.net.minecraft.client.toast.SystemToast
Generated dev.gxlg.versiont.gen.com.mojang.blaze3d.platform.SystemToast$Type
Generated dev.gxlg.versiont.gen.net.minecraft.client.toast.ToastManager
Generated dev.gxlg.versiont.gen.Text
Generated dev.gxlg.versiont.gen.Formatting
Generated dev.gxlg.versiont.gen.net.minecraft.sound.SoundEvents
Generated dev.gxlg.versiont.gen.KeyBinding
Generated dev.gxlg.versiont.gen.net.minecraft.client.gui.screen.Screen
Version't layer generated!
BUILD SUCCESSFUL
```

The directory layout above is the canonical package arrangement produced by the toolchain.

## See also

- See `wrapper-generation.md` for the rules that turn those `Generated …` lines into actual Java files.
- See `runtime-api.md` for what every wrapper class calls at runtime.
- See `changelog.md` for triage history and disproofs of prior assumptions.
