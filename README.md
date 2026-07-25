# Xaero Disabled Radar Fixer

[![Build](https://github.com/Laviesss/XDRF/actions/workflows/build.yml/badge.svg)](https://github.com/Laviesss/XDRF/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/Laviesss/XDRF)](https://github.com/Laviesss/XDRF/releases/latest)
[![License](https://img.shields.io/badge/license-CC_BY--NC--ND_4.0-blue)](LICENSE)

Blocks server-side attempts to silently disable the radar in Xaero's Minimap and World Map.

Some Minecraft servers send hidden/obfuscated chat messages or enforce plugin-channel rules to silently disable radar on **Xaero's Minimap** and **Xaero's World Map**. This lightweight **client-side mod** intercepts both, giving you full control over your radar regardless of server intent.

---

## Features

- **Blocks radar-disabling chat codes** — intercepts hidden/obfuscated chat messages before they reach Xaero's mods
- **Blocks radar-disabling rules packets** — intercepts plugin-channel rules packets that disable radar or cave mode
- **Blocking Scope** — choose to block chat messages only, packets only, or both (default: both)
- **Toast notifications** — optional pop-up alert when a radar-disabling attempt is blocked
- **Chat notifications** — optional in-game chat message when something is blocked
- **Enforce Blocking** — replay the last blocked code or packet (useful if moderators ask for minimap proof)
- **Revoke Blocking** — send a reset code to undo the server's disable attempt
- **Verbose Logging** — detailed console logging for debugging blocked messages and packets
- **Config screen** — accessible via ModMenu + YACL (Yet Another Config Lib)

---

## Compatibility

| | |
|---|---|
| **Minecraft** | 1.21 through 26.2+ |
| **Mod Loader** | Fabric (requires Fabric Loader >= 0.15.0) |
| **Platform** | Client-side only — not needed on servers |
| **Xaero's Mods** | Minimap, World Map, and XaeroPlus |

Uses [Version't](https://modrinth.com/mod/versiont) for cross-version support, allowing a single jar to work across all supported Minecraft versions without recompilation.

---

## Dependencies

Install these **before** using XDRF:

| Dependency | Version | Bundled? |
|---|---|---|
| [Fabric Loader](https://fabricmc.net/) | >= 0.15.0 | No |
| [Fabric API](https://modrinth.com/mod/fabric-api) | Compatible with your MC version | No |
| [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap) or [World Map](https://modrinth.com/mod/xaeros-world-map) | Any version for your MC version | No |
| [Mod Menu](https://modrinth.com/mod/modmenu) | Any version for your MC version | No |
| [YACL (Yet Another Config Lib)](https://modrinth.com/mod/yacl) | >= 3.6.2 | No |
| [Version't](https://modrinth.com/mod/versiont) | >= 1.2.3 | **No** — must be installed separately |

---

## Download

Grab the latest release from the [Releases page](https://github.com/Laviesss/XDRF/releases). Jars include a SHA-256 checksum for verification.

---

## Configuration

Open ModMenu, find **Xaero Disabled Radar Fixer** in the mod list, and click the config icon (cog). The config screen provides:

| Option | Default | Description |
|---|---|---|
| Enable Radar Fixer | On | Master toggle for all blocking |
| Blocking Scope | Both | What to block: Chat Message, Packet, or Both |
| Show Chat Message | On | Notify via chat when something is blocked |
| Show Toast Notifications | On | Notify via toast when something is blocked |
| Verbose Logging | Off | Log detailed info about blocked items (debugging) |
| Enforce Blocking | — | Button: replay last blocked code or packet |
| Revoke Blocking | — | Button: send reset code to undo server's disable |

Config is saved to `config/xaero_disabled_radar_fixer.json`.

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
- Single jar works across MC 1.21 through 26.2+ thanks to Version't cross-version support

---

## Disclaimer

This mod was developed primarily using AI assistance. While it functions as intended, I am not a professional developer. Expect occasional bugs or limitations — I may not be able to fix all issues quickly or at all.

By using this mod, you acknowledge that you are solely responsible for any consequences, including but not limited to warnings, mutes, kicks, or bans from multiplayer servers. Use at your risk, especially on servers with rules against minimap or radar modifications.

---

## License

Licensed under **CC BY-NC-ND 4.0**.
- **Modpacks** are allowed
- **Modifications, forks, reuploads, or commercial use** are not
- **Proper credit is required**
