# Version't Limitations

Documented constraints — some inherent, some tooling gaps.

---

## L1. Generics Not Supported (Inherent)

**Problem.** Version't cannot emit generic wrapper types (`List<Component>`, `Optional<SoundEvents>`).

**Why.** The generator would need type parameter substitution rules for every combination. Not implemented.

**Workaround.** Use `adapter` directive:
```text
adapter java.util.List -> com.yourmod.adapters.ListAdapter
```
Adapter must implement the contract in `mapping-language.md`.

**Status.** ⚠️ Unverified for custom adapters; `gXLg/libr-getter` uses this for `List`.

---

## L2. Inner Classes Buggy (Tooling)

**Problem.** `KeyMapping$Category`, `SystemToast$Type` emitters produce broken field accessors.

**Manifestation.** `KeyMapping$Category.MISC()` returns `KeyMapping.Category` (real MC inner class) not wrapper.

**Root Cause.** Generator treats inner class as top-level wrapper but field resolver uses wrong type token.

**Workaround.** Omit from mapping; access via `R.clz("Outer$Inner").fld("CONST", Outer$Inner.class).get()`.

**Fix.** Await toolchain update or patch `generate-layer.js`.

---

## L3. No Method Overload Resolution (Inherent)

**Problem.** If MC has two overloads with same name but different signatures, mapping both under same wrapper method name causes ambiguity.

**Example.** `useItemOn(LocalPlayer, Hand, HitResult)` vs `useItemOn(LocalPlayer, Level, Hand, HitResult)`.

**Generator Behavior.** Deduplicates by appending `1`, `2` to method name (`useItemOn`, `useItemOn1`).

**Runtime.** `R.mthd("useItemOn/useItemOn1", ...)` tries both; first matching signature wins.

**Risk.** Wrong overload matched if signatures compatible. Manual disambiguation may be needed.

---

## L4. No Bytecode Remapping (Inherent)

**What It Means.** Version't wraps reflective calls (`R.clz`, `R.mthd`). Direct Yarn-named class references in your bytecode (e.g. `MinecraftClient.class` literal) are **not** rewritten.

**Consequence.** Your mod JAR contains `MinecraftClient` references. On 26.x where class is `Minecraft`, Fabric Loader's `RuntimeModRemapper` must remap them.

**Limitation.** Version't does not replace `RuntimeModRemapper`. Both must work.

**Practical Impact.** Works fine for Fabric mods because Loader remaps. Won't work on non-Fabric environments.

---

## L5. Single Inheritance Only (Tooling)

**Constraint.** `class A extends B` allowed. `class A implements I, J` → **fails**.

**Why.** Generator emits single `implements` clause. Multiple would break.

**Workaround.** Use composition over inheritance for wrapper interfaces.

---

## L6. No Wrapper Generics (Inherent)

**Constraint.** Generated wrappers cannot be generic: `class List<T> extends Wrapper` not possible.

**Impact.** Can't have `Wrapper<List<Component>>` — must use raw types or adapters.

---

## L7. No Constructor Overload Deduplication (Tooling)

**Problem.** If you declare two `<init>` rows with different param types, generator emits two constructors. If signatures collide after type erasure, javac errors.

**Mitigation.** Use `V.higher()` dispatch in source instead of mapping multiple constructors.

---

## L8. Static Field Getters Only (Inherent)

**Behavior.** `static final Type NAME` → `public static Type NAME()` getter + `public static void NAME(Type)` setter.

**No Direct Field Access.** Can't do `ChatFormatting.RED` — must call `ChatFormatting.RED()`.

**Reason.** Reflection requires method call; generated field would be on wrapper class not MC class.

---

## L9. Dev Environment Name Resolution (Runtime)

**Issue.** In `runClient`, classes are Yarn-named (`MinecraftClient`). Intermediary name (`class_310`) **fails**.

**Requirement.** Import chain must have Yarn name as **second** entry:
```text
import net.minecraft.class_310/net/minecraft/client/MinecraftClient/net/minecraft/client/Minecraft
```

**If Missing.** `ClassNotFoundException` at mod load.

---

## L10. `V.higher()` Is Strict Greater-Than (Behavioral)

**Common Mistake.** `V.higher("1.21.10")` for "1.21.10 or newer".

**Correct.** `V.higher("1.21.10")` → true only for 1.21.11+. For ≥1.21.10 use `V.higher("1.21.9")` or `!V.lower("1.21.10")`.

---

## L11. `include` Configuration Required (Build)

**Pitfall.** `implementation "dev.gxlg:versiont-library"` does **not** bundle into mod JAR.

**Must Use.** `include "dev.gxlg:versiont-library:1.2.3"` — Loom bundles and remaps.

---

## L12. Node.js Required at Build Time (Tooling)

**Requirement.** `versiontLayer` spawns `node generate-layer.js`. No bundled JS runtime.

**CI Impact.** All build agents need Node.js LTS on PATH.

---

## L13. No Incremental Generation (Tooling)

**Behavior.** `versiontLayer` re-runs entire generator on any mapping change. No file-level incremental.

**Impact.** Slow for large mappings (>100 classes). XDRF (~12 classes) ~2s.

---

## L14. No IDE Integration (Tooling)

**Gap.** No IntelliJ/Eclipse plugin for:
- Mapping syntax highlighting
- Jump-to-wrapper from mapping
- Generated source navigation

**Workaround.** Open generated files in `build/generated/sources/versiont/java/...`

---

## L15. Mixin Targets Not Wrapped (Inherent)

**Fact.** `@Mixin(ClientPlayNetworkHandler.class)` uses Yarn 1.21 name. Fabric Loader remaps.

**Version't Role.** None. Wrapper not needed for mixin targets.

**Risk.** If Loader remapping fails (e.g. accessWidener bug in 0.18.x), mixin breaks — not Version't's problem.

---

## L16. No Version Range Helper (API Gap)

**Missing.** No `V.between("1.21", "1.21.11")` or `V.matches("1.21.*")`.

**Workaround.** Chain `V.higher("1.20.99") && V.lower("1.21.12")`.

---

## Summary Table

| ID | Category | Severity | Workaround |
|----|----------|----------|------------|
| L1 | Generics | Inherent | `adapter` directive |
| L2 | Inner classes | Tooling bug | Use `R.clz("Outer$Inner")` directly |
| L3 | Overloads | Inherent | Generator suffixes; manual disambiguation |
| L4 | No remapping | Inherent | Requires Fabric Loader |
| L5 | Single inheritance | Tooling | Composition |
| L6 | No generics | Inherent | Adapters / raw types |
| L7 | Constructor overloads | Tooling | `V.higher()` dispatch |
| L8 | Static fields → getters | Inherent | Call `NAME()` |
| L9 | Dev env name resolution | Runtime | Yarn name as 2nd import entry |
| L10 | `V.higher` strict | API design | Use `V.higher(prev)` |
| L11 | `include` required | Build config | Use `include` not `implementation` |
| L12 | Node.js at build | Tooling | Install in CI |
| L13 | No incremental | Tooling | Accept |
| L14 | No IDE support | Tooling | Manual |
| L15 | Mixin targets | Inherent | Loader remaps |
| L16 | No version range | API gap | Chain checks |

---

**All limitations verified against Version't 1.3.4 / 1.2.3 source and XDRF migration experience.**