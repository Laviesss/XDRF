# Xaero Disabled Radar Fixer

[![Build](https://github.com/Laviesss/XDRF/actions/workflows/build.yml/badge.svg)](https://github.com/Laviesss/XDRF/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/Laviesss/XDRF)](https://github.com/Laviesss/XDRF/releases/latest)
[![License](https://img.shields.io/badge/license-CC_BY--NC--ND_4.0-blue)](LICENSE)

Blocks server-side attempts to silently disable the radar in Xaero's Minimap & World Map.

Some Minecraft servers send hidden/obfuscated chat messages or enforce plugin-channel rules to silently disable radar on **Xaero's Minimap** and **Xaero's World Map**. This lightweight **client-side mod** intercepts both, giving you full control over your radar regardless of server intent.

---

## Features

- Blocks radar-disabling chat codes before they reach Xaero's mods
- Blocks server-enforced minimap rules packets (radar/cave-mode disable via plugin channel)
- Configurable keybinding (default N) to toggle blocking on/off
- Optional in-game chat notifications when something is blocked
- Optional toast pop-ups as visual alerts
- Optional sound effect on block (villager celebrate)
- Resend last blocked code — reapply previously blocked radar state if moderators ask for minimap proof
- Send reset code — manually restore radar to its unblocked state
- Fully configurable via Mod Menu + YACL (Yet Another Config Lib)

---

## Compatibility

- Xaero's Minimap, Xaero's World Map, and XaeroPlus
- Minecraft 1.21 – 1.21.x
- Requires Fabric Loader + Fabric API
- Client-side only — not needed on servers

---

## Download

Grab the latest release from the [Releases page](https://github.com/Laviesss/XDRF/releases). Jars include a SHA-256 checksum for verification.

---

## Building from Source

**Prerequisites:** JDK 21+ (Gradle wrapper handles the rest).

```bash
# Clone
git clone https://github.com/Laviesss/XDRF.git
cd XDRF

# Build
./gradlew build
```

The compiled jar lands in `build/libs/`.

### CI

| Workflow | Triggers | What it does |
|---|---|---|
| [build.yml](.github/workflows/build.yml) | Push / PR to `main` | Compiles, caches Gradle + Loom, uploads dev jar as artifact |
| [release.yml](.github/workflows/release.yml) | Tag `v*` or `workflow_dispatch` | Builds, signs, publishes GitHub Release with jar + SHA-256 checksums |

**Versioning:** Releases are tagged `vX.Y.Z`. Push a tag to trigger the release workflow:

```bash
git tag v1.0.1
git push origin v1.0.1
```

---

## Notes

- Client-side only — does not interfere with server operations
- Will violate server rules that prohibit radar use — use with discretion
- Lightweight — no gameplay or performance impact
- Config is saved persistently to `config/xaero_disabled_radar_fixer.json`

---

## Disclaimer

This mod was developed primarily using AI assistance. While it functions as intended, I am not a professional developer. Expect occasional bugs or limitations — I may not be able to fix all issues quickly or at all.

By using this mod, you acknowledge that you are solely responsible for any consequences, including but not limited to warnings, mutes, kicks, or bans from multiplayer servers. Use at your own risk, especially on servers with rules against minimap or radar modifications.

---

## License

Licensed under **CC BY-NC-ND 4.0**.
- ✅ **Modpacks** are allowed
- ❌ **Modifications, forks, reuploads, or commercial use** are not
- 📌 **Proper credit is required**
