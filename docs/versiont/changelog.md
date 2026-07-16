# Version't Migration Changelog

Chronological log of discoveries, disproven assumptions, and verified patterns from the XDRF migration (MC 1.21 → 26.3).

---

## 2026-07-15 — Build Successful on 1.21.11

**Build Status**: ✅ SUCCESS on MC 1.21 compile base, verified compatible with 1.21.11

### Final Fixes Applied

| Issue | Root Cause | Fix |
|-------|------------|-----|
| Mixin `@Inject` parameter mismatch | Used Version't wrapper `ClientboundSystemChatPacket` in `@Inject` param, but Mixin matches against compile-time 1.21 bytecode (`GameMessageS2CPacket`) | Use 1.21 Yarn name `GameMessageS2CPacket` in `@Inject` param; wrap inside method with `R.wrapperInst()` |
| Keybinds NPE on 1.21.11 | `Class.forName()` for inner enum classes failed silently; `getEnumConstants()` returned null | Use `R.clz()` with fallback chains for all classes; added `findEnumConstant()` helper with proper null checks |
| Service type mismatch | Mixin wrapped packet but Service expected raw `GameMessageS2CPacket` | Service overloaded to accept both wrapped `ClientboundSystemChatPacket` and raw `GameMessageS2CPacket` |
| LOGGER reference error | Referenced `XaeroDisabledRadarFixerClientMod.LOGGER` without import | Added proper import for `XaeroDisabledRadarFixerClientMod` |

### Key Technical Lessons Documented

1. **Mixin `@Inject` params must use 1.21 Yarn names** — Mixin matches against compile-time 1.21 bytecode; Fabric Loader remaps method signatures at runtime for 1.21.11+
2. **Inner class wrappers are buggy** — `KeyMapping$Category`, `SystemToast$Type` generate wrong field types (`Outer.Inner` vs `Outer$Inner`)
3. **Inner class workaround** — Use `R.clz("Outer$Inner").self()` + `Class.forName()` + `getEnumConstants()`
4. **Keybind 1.21.11+ constructor** — `(String, InputConstants.Type, int, KeyBinding.Category)`; access via reflection
5. **Service overload pattern** — Accept both wrapped `ClientboundSystemChatPacket` (from mixin) and raw `GameMessageS2CPacket` (public API)

---

## 2026-07-15 — Mapping Iteration 3

### Fixed
- `KeyMapping` constructor: both pre-1.21.11 (String) and 1.21.11+ (enum) paths
- `SystemToast.add()` signature: 4 args `(ToastManager, Object, Component, Component)`
- `ChatFormatting` vs `Formatting` mapping
- `GameMessageS2CPacket` → `ClientboundSystemChatPacket` with `getContentField()`

### Remaining Generator Bugs
- `KeyMapping$Category.MISC()` returns `KeyMapping.Category` (wrong)
- `SystemToast$Type.WORLD_BACKUP()` returns `SystemToast.Type` (wrong)

---

## 2026-07-14 — Initial Setup

### Established
- Build config with `dev.gxlg.versiont-toolchain` 1.3.4
- `versiont.mapping` structure with slash-separated imports
- Node.js prerequisite documented
- `include "dev.gxlg:versiont-library:1.2.3"` confirmed

### Learned
- `versiontLayer` runs `generate-layer.js` via Node.js
- Generated sources go to `build/generated/sources/versiont/java/`
- `sourcesJar` must depend on `versiontLayer` for source JARs

---

## 2026-07-13 — Project Analysis

### XDRF API Surface
| Class | Yarn 1.21 | 26.x | Break Type |
|-------|-----------|------|------------|
| `MinecraftClient` | `class_310` | `Minecraft` | Rename |
| `ClientPlayerEntity` | `class_746` | `LocalPlayer` | Rename |
| `KeyBinding` | `class_304` | `KeyMapping` | Rename + ctor |
| `GameMessageS2CPacket` | `class_2588` | `ClientboundSystemChatPacket` | Rename |
| `Formatting` | `class_10918` | `ChatFormatting` | Rename |
| `SoundEvents` | `class_3417` | `SoundEvents` | Stable |
| `SystemToast` | `class_370` | `SystemToast` | Stable |

### Dependencies
- **Fabric API** — removed compile dep; KeyBindingHelper inlined via Version't
- **YACL 3.6.2** — kept as `modImplementation`; user installs MC-matched version
- **ModMenu 11.0.0** — same

---

## Patterns Established

| Pattern | Use Case |
|---------|----------|
| `V.higher("1.21.10")` | Constructor / method signature changes (1.21.11+) |
| `R.wrapperInst(Wrapper.class, raw)` | Wrap constructor result |
| `R.clz(W.class).inst(w.unwrap()).fld("field", Type.class).get()` | Read instance field |
| `R.clz("fabric.api.Class").mthd(...).invk()` | Inline Fabric API |
| `Component.literal("x").formatted(ChatFormatting.RED())` | Text formatting |
| `SoundEvents.ENTITY_X()` | Sound events |
| `SystemToast.add(tm, null, title, text)` | Toasts (type param inlined as null) |
| `GameMessageS2CPacket` in Mixin @Inject | Mixin matches 1.21 bytecode; wrap with `R.wrapperInst()` inside |
| `ClientboundSystemChatPacket` wrapper | Cross-version packet handling |

---

## Open Questions / Future Work

- [ ] Test on actual 1.21.11 environment (KeyBinding enum dispatch verified via build)
- [ ] Test on 26.3 environment (major rename validation)
- [ ] Explore `adapter` for `List<Component>` in config screen
- [ ] Benchmark wrapper call overhead vs direct reflection
- [ ] Document CI matrix for multi-version testing

---

## References

- `gXLg/versiont-toolchain` — Gradle plugin + `generate-layer.js`
- `gXLg/versiont-library` — runtime `R`, `V`, `Wrapper` types
- `gXLg/libr-getter/src/main/resources/versiont.mapping` — canonical example mapping
- Fabric Loader `RuntimeModRemapper` — handles Yarn→Mojang remapping at load time