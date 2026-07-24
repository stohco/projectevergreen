# Ergenverse — Distribution README

This `dist/` folder produces two distributable artifacts for the Ergenverse mod (Renegade Immortal / 仙逆), Minecraft 1.20.1 + Forge 47.4.0.

## Artifacts

### 1. `ergenverse-modpack-0.1.3-alpha.zip` — CurseForge-importable modpack

Standard CurseForge modpack format. Contains:
- `manifest.json` — declares Minecraft 1.20.1 + Forge 47.4.0
- `modlist.html` — human-readable mod list
- `overrides/mods/ergenverse-0.1.3-alpha.jar` — the mod

**Usage:** CurseForge → + Create Custom Profile → Import → select ZIP.

### 2. `ergenverse-instance-0.1.3-alpha.zip` — Raw CurseForge instance folder

A ready-to-place instance folder. Contains:
- `Ergenverse/profile.json`
- `Ergenverse/minecraftinstance.json`
- `Ergenverse/minecraft/mods/ergenverse-0.1.3-alpha.jar`
- `Ergenverse/README.md`

**Usage:** Extract and drop `Ergenverse/` into `curseforge/minecraft/Instances/`.

Both artifacts are uploaded to GitHub Release `v0.1.3-alpha` as additional assets alongside the bare JAR.

## Build

The packaging is reproducible:

```bash
cd forge-mod
# 1. Build the mod JAR
export JAVA_HOME=/tmp/my-project/.jdks/jdk-17.0.13+11
./gradlew build
# 2. Copy fresh JAR into dist dirs
cp build/libs/ergenverse-0.1.3-alpha.jar dist/modpack/overrides/mods/
cp build/libs/ergenverse-0.1.3-alpha.jar dist/instance/Ergenverse/minecraft/mods/
# 3. Package both distributions
cd dist && zip -r ../releases/ergenverse-modpack-0.1.3-alpha.zip modpack
cd ../dist && zip -r ../releases/ergenverse-instance-0.1.3-alpha.zip instance
```

## Forge version

- Minecraft: 1.20.1
- Forge: 47.4.0
- Java: 17

These are pinned in `gradle.properties` and mirrored in both `manifest.json` (modpack) and `profile.json` / `minecraftinstance.json` (instance).
