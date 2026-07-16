# Version't

Multi-version Minecraft Fabric modding toolchain. A reflection-based abstraction that lets one source tree target many Minecraft releases at once.

## Status of this documentation

Every statement is classified by source as one of:

- ✅ **Verified** — confirmed through source code, generated wrapper output, build output, compiler behavior, or an official example project.
- ⚠️ **Unverified** — reasonable hypothesis not yet confirmed.
- ❌ **Incorrect** — a previous assumption that was disproven.

Where a hypothesis is later disproven or verified, this directory is updated in place — no superseded notes are retained.

---

## Table of contents

| File | Topic |
|------|-------|
| [introduction.md](introduction.md) | What Version't is, what it solves, what it does not solve. |
| [project-setup.md](project-setup.md) | Gradle plugins, repositories, dependencies, Java/Loom/Loader versions, `versiont {}` block. |
| [mapping-language.md](mapping-language.md) | Complete grammar of `versiont.mapping`. |
| [wrapper-generation.md](wrapper-generation.md) | How the toolchain turns mapping entries into Java wrappers. |
| [runtime-api.md](runtime-api.md) | `R`, `V`, `Wrapper`, `WrapperInterface`, `wrapperInst`, etc. |
| [compatibility-patterns.md](compatibility-patterns.md) | Recurring patterns for cross-version compatibility. |
| [troubleshooting.md](troubleshooting.md) | Common compile, generation, mapping, runtime failures. |
| [best-practices.md](best-practices.md) | Verified do's and do-not's. |
| [limitations.md](limitations.md) | Verified limitations, potential limitations, unknowns. |
| [migration-checklist.md](migration-checklist.md) | Step-by-step plan for adopting Version't in an existing Fabric mod. |
| [cheatsheet.md](cheatsheet.md) | Concise reference card. |
| [changelog.md](changelog.md) | Chronological log of what was learned and verified. |

---

## Verified at a glance

- **Class names in the runtime reflection layer (`R.clz`) take slash‑separated fallback chains** ✅ — confirmed in `dev/gxlg/versiont/api/R.java` (`className.split("/")`, tried left‑to‑right via `Class.forName`).
- **Method and field identifiers also take slash chains** (`mthd("a/b", …)`, `fld("a/b", …)`) ✅ — same `R.java`.
- **Generated wrappers live under `dev.gxlg.versiont.gen.<package>` where the package path is reconstructed from the lookup chain's slash‑separated segments** ✅ — confirmed in `build/generated/sources/.../dev/gxlg/versiont/gen/...` for every wrapper produced by the toolchain.
- **Inner‑class wrappers are valid Java classes whose simple name contains `$`** (e.g. `SystemToast$Type`) ✅ — confirmed in `generate-layer.js` import line handling and the generated `SystemToast$Type.java`.
- **Cross‑version class rename of the outer class should be expressed through the slash‑separated import chain using the Fabric intermediary as the first name** ✅ — canonical pattern in `gXLg/libr-getter/src/main/resources/versiont.mapping`.
- **The `alt:` suffix on an `import` line registers additional look‑up short names for the same Java class** ✅ — confirmed in `gXLg/libr-getter/src/main/resources/versiont.mapping` (`alt: _1_20_3`) and the `generate-layer.js` parser.
- **`V.higher("1.21.10")` is documented as the supported runtime version‑check primitive** ✅ — present in `dev/gxlg/versiont/api/V.java`.
