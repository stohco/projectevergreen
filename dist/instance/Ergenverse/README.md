# Ergenverse — CurseForge Instance Folder

This is a **ready-to-place CurseForge instance folder** for the Ergenverse mod (Renegade Immortal / 仙逆), Minecraft 1.20.1 + Forge 47.4.0.

## What's inside

```
Ergenverse/
├── profile.json                 ← CurseForge profile metadata (MC 1.20.1, Forge 47.4.0, Java 17)
├── minecraftinstance.json       ← CurseForge instance metadata (forge addon, targets)
└── minecraft/
    ├── mods/
    │   └── ergenverse-0.1.0-alpha.jar   ← THE MOD (13 MB, 814 compiled classes)
    └── config/
        └── (empty — mod generates configs on first run)
```

## How to use it — two options

### Option A: Drop into your existing CurseForge Instances folder (fastest)

1. Close the CurseForge app.
2. Find your CurseForge instances folder:
   - **Windows:** `C:\Users\<you>\curseforge\minecraft\Instances\`
   - **macOS:** `~/Library/Application Support/curseforge/minecraft/Instances/`
   - **Linux:** `~/.curseforge/minecraft/Instances/` (or wherever CurseForge is installed)
3. Copy this entire `Ergenverse/` folder into that `Instances/` directory.
4. Reopen CurseForge. The **Ergenverse** profile should appear in your profile list.
5. CurseForge will detect Forge 47.4.0 is required and install it on first launch if missing.
6. Click **Play**.

### Option B: Import as a modpack ZIP (cleaner, recommended)

Use `ergenverse-modpack-0.1.0-alpha.zip` (the companion file in the GitHub release) instead:

1. Open CurseForge → **+ Create Custom Profile** → **Import**.
2. Select `ergenverse-modpack-0.1.0-alpha.zip`.
3. CurseForge creates a fresh profile named **Ergenverse**, installs Forge 47.4.0, and drops the mod in `mods/`.
4. Click **Play**.

## Requirements

- **Minecraft 1.20.1** (CurseForge downloads automatically)
- **Forge 47.4.0** (CurseForge installs automatically)
- **Java 17** (Forge 1.20.1 requirement — CurseForge bundles its own, but having a system Java 17 helps)

## What happens on first launch

When you create a new world and join for the first time:

1. **Wang Family Village** is built around you at spawn — a 23×23 settlement made entirely of custom blocks:
   - Central **spirit stone plaza** with a **Spirit Vein Stone** centerpiece and 4 **Formation Core Stones** forming a small array
   - 4 corner houses (spirit wood planks + log pillars + leaves roofs):
     - **NW — Alchemy Pavilion** (contains an Alchemy Furnace)
     - **NE — Formation Hall** (contains a Formation Flag Base)
     - **SW — Storage** (chest with starter gear: 8 spirit stones, jade slip, 4 qi gathering pills, meditation mat, 6 herb seeds, 2 formation flag blanks, 4 talisman papers)
     - **SE — Your Dwelling** (empty, for you)
   - **Herb garden** south of the plaza (8 custom herb species)
   - 4 decorative **spirit wood trees** at the corners
   - Torches lighting the plaza
2. You receive a **Beginner's Guide** written book (6 pages: welcome, keybinds, first steps, village guide, danger, path forward)
3. You're teleported to the plaza, facing the spirit vein

### Keybinds
- **B** — Breakthrough attempt (when Qi is full)
- **V** — Divine Sense pulse (scan nearby entities/concealed objects)

## What the mod contains

817 compiled Java classes covering:
- **Cultivation** — Qi Condensation → Foundation → Core Formation → Nascent Soul → Soul Formation → Void Tribulation → Ascendant
- **Body Refining** — physical cultivation track
- **Alchemy** — pill recipes and refinement
- **Beasts & Flora** — ecological simulation
- **Formations & Talismans** — placement, activation, hitboxes
- **Canon & Divine Sense** — 175-check canon validator (passes)
- **NPC AI** — IntentEngine, DecisionEngine, WorldEventBus, KnowledgeEngine, PerceptionEngine, BeliefRegistry, WorldHistory

Plus, bundled inside the JAR as resources: 49 Living Chapter 1 JSON files (Wang Family Village) — 9 NPC motivation states, 31 relationship graph seeds, 9 favor/debt ledgers, universal interaction catalog (37 verbs), conversation system, NPC initiation consent pipeline, 10 social engines, Wang Lin bidirectional protocol.

## Status

This is an **alpha**. The mod compiles and loads. The data layer (NPC desires, relationships, favors, conversations) is schema-complete inside the JAR. Java runtime wiring of those schemas into the NPC AI engines is the next phase. The world compiles — next it must *desire*.

## Links

- **Release:** https://github.com/stohco/projectevergreen/releases/tag/v0.1.0-alpha
- **Repo:** https://github.com/stohco/projectevergreen
- **Mod JAR (direct):** https://github.com/stohco/projectevergreen/releases/download/v0.1.0-alpha/ergenverse-0.1.0-alpha.jar
