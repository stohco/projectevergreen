# Ergenverse — Installation Guide

Minecraft 1.20.1 + Forge 47.4.0 + Java 17

## RECOMMENDED: Use the Modpack ZIP (Option B)

The **Modpack ZIP** is the most reliable install method. CurseForge reads the manifest, downloads Forge 47.4.0 automatically, and sets everything up.

1. Download `ergenverse-modpack-0.1.0-alpha.zip`
2. Open CurseForge → click **+ Create Custom Profile** → **Import**
3. Select `ergenverse-modpack-0.1.0-alpha.zip`
4. CurseForge creates a profile named **Ergenverse**, installs Forge 47.4.0, drops the mod in `mods/`
5. Click **Play**

## ALTERNATIVE: Instance ZIP (Option A)

If the Modpack ZIP doesn't work, use the Instance ZIP:

1. Download `ergenverse-instance-0.1.0-alpha.zip`
2. Extract it — you get an `Ergenverse/` folder
3. Find your CurseForge instances folder:
   - **Windows:** `C:\Users\<you>\curseforge\minecraft\Instances\`
   - **macOS:** `~/Library/Application Support/curseforge/minecraft/Instances/`
4. Copy the `Ergenverse/` folder into that `Instances/` directory
5. Restart CurseForge — the Ergenverse profile should appear
6. **Important:** In CurseForge settings, make sure Java 17 is selected (not Java 21)
7. Click **Play**

## Requirements

- **Minecraft 1.20.1** (CurseForge downloads automatically)
- **Forge 47.4.0** (CurseForge installs automatically)
- **Java 17** — Forge 1.20.1 REQUIRES Java 17. If you only have Java 21, Forge will crash.
  - Download Java 17 from Adoptium (Eclipse Temurin 17) if needed
  - In CurseForge: Settings → Minecraft → set Java version to 17

## If it still fails

If you get "FAILED TO LAUNCH MODPACK, UNEXPECTED ERROR OCCURRED":

1. Open the instance folder: `CurseForge/Minecraft/Instances/Ergenverse/minecraft/logs/`
2. Find `latest.log` (or the most recent file in `crash-reports/`)
3. Copy the last 50 lines (or anything saying "Exception", "Error", "Caused by")
4. Send it to me — I'll fix the root cause immediately

## What's inside

```
Ergenverse/
├── minecraftinstance.json       ← CurseForge instance metadata
├── profile.json                 ← Profile metadata
└── minecraft/
    ├── mods/
    │   └── ergenverse-0.1.0-alpha.jar   ← THE MOD (13 MB, 800+ classes)
    ├── config/                   ← Mod configs (generated on first run)
    ├── versions/                 ← Forge/MC versions (created by CurseForge)
    ├── logs/                     ← Game logs (created on first launch)
    └── saves/                    ← World saves (created on first launch)
```

## Links

- **Release:** https://github.com/stohco/projectevergreen/releases/tag/v0.1.0-alpha
- **Modpack ZIP:** https://github.com/stohco/projectevergreen/releases/download/v0.1.0-alpha/ergenverse-modpack-0.1.0-alpha.zip
- **Instance ZIP:** https://github.com/stohco/projectevergreen/releases/download/v0.1.0-alpha/ergenverse-instance-0.1.0-alpha.zip
- **Mod JAR only:** https://github.com/stohco/projectevergreen/releases/download/v0.1.0-alpha/ergenverse-0.1.0-alpha.jar
