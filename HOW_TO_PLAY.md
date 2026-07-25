# How to Play the Ergenverse Mod

## Quick Start (For Players)

### Option A: Install in your existing Minecraft 1.20.1

1. **Install Forge 1.20.1** (version 47.4.0 or later)
   - Download the Forge installer from https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html
   - Run the installer, select "Install client"

2. **Get the mod JAR**
   - The mod JAR is at: `build/libs/ergenverse-0.1.5-alpha.jar`
   - Copy this file to your Minecraft `mods` folder:
     - Windows: `%APPDATA%\.minecraft\mods\`
     - Mac: `~/Library/Application Support/minecraft/mods/`
     - Linux: `~/.minecraft/mods/`

3. **Launch Minecraft** with the Forge 1.20.1 profile

4. **Create a new world** — the mod generates a custom cultivation world

### Option B: Run from source (Developer mode)

```bash
# Set Java 17
export JAVA_HOME=/path/to/jdk-17

# Launch the Minecraft client with the mod
./gradlew runClient

# OR launch a dedicated server
./gradlew runServer
```

---

## What to Expect

### The Living World (Phase 0)

When you spawn, you enter a world that **already exists** — it has been running for ten thousand years before you arrived. You are NOT the center of the world.

**You will see:**
- **Wang Family Village (王家族村)** — a hand-built village with NPCs that have their own lives, memories, and motivations
- **Spirit beasts** — wolves, deer, rabbits, cranes, hawks, bats, qilin, sea serpents, and more, each with correct anatomy and AI
- **Spirit herbs** growing in the wild — frost herb, intent herb, sky spirit herb, divine fire herb
- **Canonical structures** — Heng Yue Sect, Teng Family City, and other settlements from the Renegade Immortal novel

### Wang Lin and the NPCs

The village is populated by **canon-accurate NPCs**:
- **Wang Lin (王林)** — the protagonist. He is a PATIENT_PLANNER with 6 goals and 24 memories. He prioritizes CONCEAL_STRENGTH — he will NOT reveal his cultivation power unless absolutely necessary.
- **Wang Tianshui, Wang Zhou, Da Niu** — villagers who GUARD when threats appear
- **Wang Qingyue, Wang Wei, Wang Ping, Wang Yiyi, Wang Tianshan, Zhou Tingsu** — villagers who FLEE to safety

**Observed behavior (Living Observation #1, CRON-76):**
When a wolf pack stalks near the village:
1. Wang Lin stops meditating and moves to a vantage point to OBSERVE
2. Wang Tianshui, Wang Zhou, and Da Niu take up GUARDING positions
3. The other villagers FLEE home
4. Wang Lin does NOT intervene — he conceals his strength
5. The event is recorded in village memory — the village remembers

This behavior **emerged from the simulation** — nobody wrote "if wolf → observe." Wang Lin's motivation scoring system chose OBSERVE because FIGHT would harm his CONCEAL_STRENGTH goal.

### Cultivation

- Find a **spirit vein** landmark
- **Meditate** to begin gathering qi
- Break through to **Qi Condensation** realm
- As your cultivation advances, your **perception** changes — you begin to see spirit beasts where you once saw ordinary animals

### Items

The mod includes **309+ canonical items** from Wang Lin's arsenal:
- **Flying Swords** — right-click to launch a flying sword projectile (each sword has unique effects: teleport, lifesteal, poison, restriction)
- **Soul Bead** — right-click to channel soul power (reusable, 10 charges max)
- **Talismans** — single-use spiritual tools (fireball, barrier, lightning, teleport, etc.)
- **Spirit Pills** — cultivation-boosting consumables
- **Spirit Stones** — the currency of cultivation (low/mid/high tiers + immortal stone)

### Commands

- `/ergenverse status` — check world simulation status
- `/ergenverse village` — village information
- `/ergenverse book` — tutorial book
- `/ergenverse gear` — starter gear
- `/ergen worldsim` — world simulation commands
- `/ergen history` — world history
- `/ergen chronicle` — world chronicle
- `/wanglin arsenal grant <item>` — grant a Wang Lin arsenal item
- `/wanglin arsenal list` — list all 309 arsenal items
- `/wanglin worldstate status` — Wang Lin's current state

---

## Troubleshooting

### Server crashes with "One or more entry values did not copy to the correct id"
**FIXED in CRON-76.** This was caused by duplicate item registrations between ErgenverseItems and WangLinItems for `ji_realm` and `vermilion_bird_feather`. Both are now deduplicated via the `ERGENVERSE_ITEMS_OWNED_NAMES` set.

### Server takes 60 seconds to start
This is normal. The world generation includes:
- 4 flora species
- 10+ NPC memory systems
- 6+ territory seeds
- 7 ecosystem seeds
- 8 location layers
- Full Wang Lin cognitive stack (goals, memories, beliefs)

The server prints `Done (60s)!` when ready.

### Spawn area generation is slow
The first world generation takes ~60 seconds because of the complex worldgen (custom biomes, structures, spirit herb patches). Subsequent loads are faster.

---

## Build Verification

- **Compile:** `JAVA_HOME=/path/to/jdk-17 ./gradlew compileJava` → BUILD SUCCESSFUL (0 errors)
- **Full build:** `JAVA_HOME=/path/to/jdk-17 ./gradlew build` → BUILD SUCCESSFUL (produces JAR)
- **Server test:** `JAVA_HOME=/path/to/jdk-17 ./gradlew runServer` → Done (60s), simulation live
- **Living Observation Count:** 1 (CRON-76, 2026-07-25) — Wang Lin wolf-observation event verified

## Mod Info

- **Mod ID:** `ergenverse`
- **Version:** 0.1.5-alpha
- **Minecraft:** 1.20.1
- **Forge:** 47.4.0+
- **Java:** 17
- **License:** MIT
