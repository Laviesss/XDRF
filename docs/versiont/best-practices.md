# Version't Best Practices

Verified do's and don'ts from XDRF migration and Version't source analysis.

---

## DO

### Mapping
- [ ] **Use forward slashes in imports** — `net/minecraft/client/MinecraftClient` not dots. Determines generated package.
- [ ] **Order import chain**: Intermediary → Yarn 1.21 → Yarn 26.x → future versions.
- [ ] **Include Yarn name as 2nd entry** — required for `runClient` dev environment.
- [ ] **Declare only what you use** — every method/field in mapping = generated wrapper code.
- [ ] **Use `@Nullable` on fields** — generates nullable accessors; avoids NPE at runtime.
- [ ] **Static fields → `static final Type NAME`** — emits `NAME()` getter + setter.
- [ ] **Constructors use `<init>`** with param types only (no return type).
- [ ] **Omit inner classes with known bugs** (`KeyMapping$Category`, `SystemToast$Type`, `InputConstants$Type`) — generator emits broken field types.
- [ ] **Use `adapter` for generics** — `List<Component>`, `Optional<SoundEvents>` unsupported; write custom adapters.

### Source Code
- [ ] **Import generated wrappers** from `dev.gxlg.versiont.gen.*`
- [ ] **Keep vanilla `Screen`/`Text`** for YACL/ModMenu — they expect real MC types.
- [ ] **Use `V.higher()` for version dispatch** — `V.higher("1.21.10")` for 1.21.11+.
- [ ] **Wrap all `R.*` calls in try/catch** — they throw `Throwable`.
- [ ] **Use `R.wrapperInst(Wrapper.class, raw)`** to wrap constructor results.
- [ ] **Field access via `R.clz(W.class).inst(w.unwrap()).fld("name", Type.class).get()`**.
- [ ] **Inline Fabric API** via `R.clz("net.fabricmc.fabric.api...").mthd(...).invk(...)`.
- [ ] **Keep Mixin targets as vanilla imports** — Fabric Loader remaps them.
- [ ] **Use 1.21 Yarn names in Mixin `@Inject` params** — Mixin matches against compile-time 1.21 bytecode; Fabric Loader remaps at runtime for 1.21.11+.

### Build
- [ ] **Use `include` not `implementation`** for `versiont-library` — bundles into mod JAR.
- [ ] **Node.js on PATH** for `versiontLayer` task.
- [ ] **JDK 21 runtime, JDK 25 Gradle** (or configure toolchain).
- [ ] **`sourcesJar.dependsOn(versiontLayer)`** — include generated sources in sources JAR.

### Testing
- [ ] **Test on compile-base (1.21) first** — `./gradlew runClient`.
- [ ] **Test on 1.21.11** — first version break (KeyBinding enum).
- [ ] **Test on 26.x** — major rename validation.

---

## DON'T

### Mapping
- [ ] **Don't use dots in imports** — `net.minecraft.client.MinecraftClient` breaks package generation.
- [ ] **Don't omit Yarn name** — `runClient` will `ClassNotFoundException` on intermediary.
- [ ] **Don't map unused APIs** — bloats generated code, increases compile time.
- [ ] **Don't declare inner classes** (`KeyMapping$Category`, `SystemToast$Type`, `InputConstants$Type`) — generator bugs.
- [ ] **Don't use generics in mapping** — `List<Component>` unsupported; use `adapter`.
- [ ] **Don't map mixin targets** — keep vanilla imports; Loader remaps.

### Source Code
- [ ] **Don't use `Component`/`Screen` from wrappers for YACL/ModMenu** — type mismatch.
- [ ] **Don't call `R.*` without try/catch** — throws checked `Throwable`.
- [ ] **Don't pass wrapper directly to `R`** — must `.unwrap()` first.
- [ ] **Don't forget `()` on static getters** — they're methods, not fields.
- [ ] **Don't use `implementation` for versiont-library** — classes won't be in mod JAR.
- [ ] **Don't use `R.fld` on class directly** — must bind to instance via `.inst(w.unwrap())`.
- [ ] **Don't assume `static final` is a field** — it's a getter method `NAME()`.

### Build
- [ ] **Don't skip `versiontLayer`** — `compileJava` depends on it; run explicitly for debugging.
- [ ] **Don't forget Node.js in CI** — build will fail with "node: command not found".

### Testing
- [ ] **Don't only test on 1.21** — test on at least one break version (1.21.11) and 26.x.
- [ ] **Don't assume `runClient` works if compile passes** — runtime reflection can fail.

---

## ARCHITECTURAL DECISIONS

| Decision | Rationale |
|----------|-----------|
| Flat generated packages (`dev.gxlg.versiont.gen.*`) | Avoids deep nesting; import paths stay short |
| `V.higher()` strict greater-than | Matches semantic versioning; explicit intent |
| `static final Type NAME` → `NAME()` getter | Reflection requires method; field on wrapper ≠ MC field |
| `R` throws `Throwable` | Covers `ClassNotFoundException`, `NoSuchMethodException`, etc. |
| `include` for versiont-library | Loom remaps + bundles; `implementation` doesn't |
| No inner class wrappers | Generator bugs; `R.clz("Outer$Inner")` works reliably |
| Keep YACL/ModMenu vanilla | Their APIs expect real MC types; wrapper breaks `ConfigScreenFactory<Screen>` |
| Mixin params use 1.21 Yarn names | Mixin matches compile-time bytecode; Fabric Loader remaps at runtime |

---

## PERFORMANCE NOTES

- **First-call latency**: `R.clz(...)` caches after first successful `Class.forName`. Consider `R.preload(...)` at mod init.
- **Wrapper overhead**: Each wrapper call → reflection lookup → invoke. Negligible for config/keybinds; measurable in hot paths (tick loops).
- **Generated code size**: ~1 KB per wrapper class. XDRF (~12 wrappers) ≈ 12 KB.

---

## DEBUGGING CHECKLIST

| Symptom | First Check |
|---------|-------------|
| `node: command not found` | Node.js installed? On PATH? |
| `versiont.mapping: No such file` | `versiont { mapping = file('versiont.mapping') }` correct path? |
| Wrapper compile error: `cannot find symbol` | Missing import in mapping? Run `./gradlew versiontLayer` first? |
| Runtime `ClassNotFoundException` | Import chain missing Yarn name? Intermediary correct? |
| Runtime `NoSuchMethodException` | Arg types in mapping match real signature? |
| `V.higher("1.21.10")` false on 1.21.11 | It's strict `>`; use `V.higher("1.21.9")` for ≥1.21.10 |
| Mod JAR missing `dev/gxlg/versiont` | Used `include` not `implementation`? |
| Mixin injection mismatch | Use 1.21 Yarn names in @Inject params; Fabric Loader remaps at runtime |
| Enum constant not found | Use `R.clz("Outer$Inner").self()` + `Class.forName()` + `getEnumConstants()` |

---

*All practices verified in XDRF migration (MC 1.21 → 26.3, Version't 1.3.4/1.2.3).*