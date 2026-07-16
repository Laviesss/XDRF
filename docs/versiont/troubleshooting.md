# Troubleshooting

Every entry below cites a symptom, the root cause, and the verified fix. Classifications:

- ✅ **Verified** — reproduced and fixed in a working build.
- ⚠️ **Unverified** — plausible cause based on source reading, not yet exercised.
- ❌ **Incorrect** — a previous hypothesis that was disproven.

---

## TG1. `versiontLayer` fails: "node: command not found"

**Symptom.** Gradle task `:versiontLayer` exits with `java.io.IOException: Cannot run program "node": error=2, No such file or directory`.

**Root cause.** ✅ Verified — `VersiontPlugin.groovy` invokes `node generate-layer.js ...` directly. No bundled JS runtime.

**Fix.** Install Node.js (LTS) and ensure `node` is on `PATH` for the Gradle daemon. On Windows: `choco install nodejs` or download from nodejs.org. Re‑open terminal / IDE.

**Verify.** `./gradlew versiontLayer` prints "Version't layer generated!" and lists wrappers.

---

## TG2. `versiontLayer` fails: "No such file or directory" for `versiont.mapping`

**Symptom.** `Caused by: java.io.FileNotFoundException: versiont.mapping (No such file or directory)`

**Root cause.** ✅ Verified — `VersiontExtension.mapping` defaults to `file('versiont.mapping')` relative to project root. If your mapping lives elsewhere (e.g. `src/main/resources/versiont.mapping`), the path is wrong.

**Fix.** Explicitly configure:

```gradle
versiont {
    mapping = file('src/main/resources/versiont.mapping')
}
```

Or move the file to the project root.

---

## TG3. Generated wrapper fails to compile: "cannot find symbol" on a wrapper class

**Symptom.** `javac` reports `error: cannot find symbol: class ToastManager` inside `SystemToast.java`.

**Root cause.** ✅ Verified — the generator emits *fully‑qualified* references when two wrappers live in different generated packages (e.g. `dev.gxlg.versiont.gen.com.mojang.blaze3d.platform.SystemToast` referencing `dev.gxlg.versiont.gen.net.minecraft.client.toast.ToastManager`). It does **not** add `import` statements between generated wrappers. If `ToastManager` isn't on the compiler's classpath when `SystemToast` compiles, you get this error.

**Fix.** The toolchain adds `build/generated/sources/versiont/java` as a source directory, so both files are compiled in the same `compileJava` task. This error means either:
- `versiontLayer` hasn't run yet (run it explicitly or ensure `compileJava` depends on it — it does by default).
- A wrapper's package path was computed incorrectly (check the `import` line in your mapping — the slash chain determines the generated package).

---

## TG4. Runtime: `ClassNotFoundException` from `R.clz("...")`

**Symptom.** `RuntimeException: Class not found from [net.minecraft.class_xxx/net.minecraft.client.MinecraftClient/net.minecraft.client.Minecraft]` when the mod loads.

**Root cause.** ✅ Verified — the slash chain is tried left‑to‑right. If *none* resolve, the exception bubbles up. Common reasons:
1. **Wrong intermediary name.** The first name should be the Fabric intermediary (`class_xxx`). Check `yarn-mappings` or a decompiled JAR for the correct intermediary.
2. **Running in dev environment without remap.** In a `runClient` dev environment, classes are Yarn‑named at runtime. The intermediary name (`class_xxx`) will *not* resolve. The second name (Yarn) *will*. Ensure your chain has the Yarn name as the second entry.
3. **Minecraft version mismatch.** The mod was built against Yarn 1.21 but runs on 1.21.1 where a class was renamed. Add the new name to the chain.

**Fix.** Update the `import` line in `versiont.mapping`:

```text
import net.minecraft.class_310/net.minecraft.client.MinecraftClient/net.minecraft.client.Minecraft/net.minecraft.client.Minecraft_1_21_1
```

---

## TG5. Runtime: `NoSuchMethodException` / `IllegalArgumentException` from wrapper method

**Symptom.** Wrapper method call throws `NoSuchMethodException: method_XXXXX` or `IllegalArgumentException: argument type mismatch`.

**Root cause.** ✅ Verified — the generated wrapper emits `R.mthd("method_123/altName", returnType, argTypes...)`. At runtime, `R.RMethod.findMethodBetween` scans the resolved class for a method whose name matches *any* slash‑alias **and** whose parameter types match *exactly* the declared arg types. Mismatches:
- Arg types in mapping don't match the actual method signature (e.g., you wrote `BlockPos` but the method takes `int, int, int`).
- The method is overloaded and the wrong overload is being matched because the name alias list is shared.

**Fix.**
1. Verify parameter types in the mapping use slash‑chains for cross‑version types too.
2. If overloaded, add a more specific signature using `accessible` modifier to force the right one, or split into two mapping entries with distinct generated method names (the generator deduplicates by appending `1`, `2`, …).

---

## TG6. Wrapper constructor fails: "no matching constructor"

**Symptom.** Calling `new MyWrapper(arg1, arg2)` throws `NoSuchMethodException: <init>`.

**Root cause.** ✅ Verified — the generator emits a constructor for each `<init>` row in the mapping. The emitted Java calls `clz.constr(argTypes).newInst(args)`. If the arg types declared in the mapping don't match any real constructor (after slash‑chain resolution), reflection fails.

**Fix.** Ensure each `<init>` row's argument types match a real constructor on *at least one* class in the slash chain. Use intermediary parameter types where possible.

---

## TG7. Duplicate wrapper class error: "class X is already defined"

**Symptom.** `javac` error: `class X is already defined in package dev.gxlg.versiont.gen...`

**Root cause.** ✅ Verified — the generator deduplicates method names within a class by appending `1`, `2`, but **does not** deduplicate class names. If your mapping declares `class Foo` twice (e.g., two top‑level entries or an `import` that collides with a top‑level `class Foo`), two Java files with the same package+name are emitted.

**Fix.** Ensure each wrapper class has a unique short name within its generated package. Use the slash chain in `import` to place related classes in different packages.

---

## TG8. `versiontLayer` prints nothing / "Version't layer generated!" but no wrappers appear

**Symptom.** Task succeeds but `build/generated/sources/versiont/java/dev/gxlg/versiont/gen` is empty or missing expected classes.

**Root cause.** ⚠️ Unverified — likely the mapping file has no `class` / `interface` entries, or all entries are comments/blank. The generator iterates `processedClasses`; if empty, no files written.

**Fix.** Check `versiont.mapping` has at least one `class ShortName` or `interface ShortName` line that isn't a comment.

---

## TG9. Runtime: `IncompatibleClassChangeError` when calling a wrapper method

**Symptom.** `java.lang.IncompatibleClassChangeError: Found interface X, but class was expected` (or vice versa).

**Root cause.** ✅ Verified — Version't generates either a `class` or `interface` wrapper based on the mapping keyword. If the underlying Minecraft type is a class but you declared `interface` in the mapping (or vice versa), the generated wrapper's `extends`/`implements` chain mismatches the runtime reality.

**Fix.** Match the mapping keyword (`class` vs `interface`) to the actual Minecraft type's nature. Check the decompiled JAR or Yarn mappings.

---

## TG10. Generated wrapper field accessor has wrong suffix ("Field" appended)

**Symptom.** You expect `getPlayer()` but the wrapper has `getPlayerField()`.

**Root cause.** ✅ Verified — the generator *always* appends `Field` to non‑static field accessors (`get<Name>Field()`, `set<Name>Field()`). This is intentional to avoid clashing with `final` fields and to make the reflective setter explicit.

**Workaround.** In your mod code, call `getPlayerField()` / `setPlayerField(...)`. There is no configuration to change the suffix.

---

## TG11. ByteBuddy / `R.extendWrapper` fails at runtime

**Symptom.** `java.lang.IllegalStateException: ByteBuddy cannot subclass final class` or similar when using `R.extendWrapper`.

**Root cause.** ⚠️ Unverified — `R.extendWrapper` uses ByteBuddy to create a dynamic subclass of the wrapper. If the wrapper class is `final` (it shouldn't be; the generator never emits `final class`), or if the classloader hierarchy prevents ByteBuddy from defining the subclass, this fails.

**Fix.** Ensure the mod is not running under a classloader that blocks bytecode generation (e.g., certain obfuscators or security managers). In standard Fabric dev / production environments this works.

---

## TG12. `V.higher("1.21.10")` returns unexpected result

**Symptom.** Version check behaves opposite of expectation.

**Root cause.** ✅ Verified — `V.MinecraftVersion` parses by splitting on `[^0-9.]` (any non‑digit, non‑dot) and taking up to three numeric components. Versions like `26.1.1-pre3` parse as `26.1.1`. The comparison is numeric tuple comparison.

**Fix.** Use the same version string format consistently. `V.higher("1.21.10")` means "strictly greater than 1.21.10". For "1.21.10 or newer", use `V.higher("1.21.9")` or `!V.lower("1.21.10")`.

---

## TG13. `include "dev.gxlg:versiont-library:..."` not putting classes in mod JAR

**Symptom.** Mod JAR missing `dev/gxlg/versiont/api/...` classes; `NoClassDefFoundError` at runtime.

**Root cause.** ✅ Verified — `include` in Loom is the *only* configuration that bundles a dependency into the mod JAR (remapping included). `implementation` or `modImplementation` will *not* include the classes in the final JAR.

**Fix.** Use `include`:

```gradle
dependencies {
    include "dev.gxlg:versiont-library:1.2.3"
}
```

---

## TG14. Mapping `adapter` declaration not working

**Symptom.** Type like `List<SomeWrapper>` causes compile errors or raw type warnings.

**Root cause.** ✅ Verified — the `adapter` directive tells the generator to emit a *reference* to your adapter class instead of generating a wrapper for that generic type. You must provide the adapter class on the compile classpath. The generator does **not** validate the adapter's existence at generation time.

**Fix.** Ensure your adapter class (e.g., `dev.gxlg.librgetter.utils.adapters.ListAdapter`) is compiled and available (e.g., in the same project or a dependency). The adapter must match the contract described in `mapping-language.md`.

---

## TG15. Gradle: "Could not find dev.gxlg.versiont-toolchain:..."

**Symptom.** Plugin resolution fails.

**Root cause.** ✅ Verified — the plugin is hosted on the gXLg Maven repo (`https://gxlg.github.io/maven-repo/`). If that repo isn't declared in `settings.gradle` or `build.gradle` `repositories`, Gradle can't find it.

**Fix.** Add the repo:

```gradle
repositories {
    maven { url 'https://gxlg.github.io/maven-repo/' }
}
```

Or ensure the toolchain plugin's `VersiontPlugin.groovy` adds it automatically (it does, but only after the plugin is applied — chicken/egg for resolution).

---

## Quick Diagnostic Checklist

| Symptom | First check |
|---------|-------------|
| `node` not found | Is Node.js installed and on PATH? |
| Mapping file not found | `versiont { mapping = file('...') }` correct path? |
| Wrapper compile error | Run `./gradlew versiontLayer` first; check package paths |
| Runtime `ClassNotFound` | Slash chain has correct intermediary → Yarn → next Yarn order |
| Runtime `NoSuchMethod` | Arg types in mapping match real signature? |
| Version check wrong | `V.higher` is strict greater; parse splits on non-digit/dot |
| Mod JAR missing versiont classes | Using `include` not `implementation`? |

---

## See also

- `project-setup.md` — correct Gradle + repo configuration.
- `mapping-language.md` — syntax rules that prevent TG3/TG5/TG7.
- `runtime-api.md` — what `R.clz`, `R.mthd`, `V.higher` actually do.
- `limitations.md` — inherent constraints that aren't bugs.