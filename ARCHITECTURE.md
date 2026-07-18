# Ergenverse Architecture — Canon-First Data-Driven Design

> **Pivot Date:** 2026-07-12
> **Trigger:** User directive that the project implements "an entire literary multiverse spanning millions of years" — not Minecraft content with cultivation flavor, but Er Gen's universe rendered through Minecraft.

## The Core Insight

The previous approach was **Minecraft-first**:
```
Minecraft → Forge → Custom Content (biomes, mobs, items)
```

This produced 116 worldgen JSONs for a novel that spans 80+ named locations, 158 characters, 178 artifacts, 214 techniques, and millions of years of fictional history. That's woefully inadequate.

The correct approach is **Canon-first**:
```
Er Gen Canon (novels + wiki)
        │
Canon Database (ri_canon_database.json — 630+ entries, single source of truth)
        │
Simulation Engine (Java — WorldGraph, ecology, civilization, timeline engines)
        │
Canon Worldgen Adapter (Python — reads canon DB, generates worldgen JSONs at scale)
        │
Minecraft / Forge (renders the generated worldgen — execution layer only)
```

**Forge is the rendering and execution layer.** The real "game" is the canon database plus the simulation engine. This architecture makes it possible to add every location, beast, treasure, technique, sect, historical event, and ecological relationship from the novels while keeping the Java code relatively stable as the content grows.

## Layer Responsibilities

### Layer 1: Canon Database
- **File:** `ri_canon_database.json` (630 entries: 158 characters, 80 locations, 178 artifacts, 214 techniques)
- **Purpose:** Objective, immutable record of what Er Gen wrote. Every entry has `canonConfidence` (1-5), `source`, `knownFacts[]`.
- **Rule:** If it's not in the database (or canon docs), it doesn't exist in the mod. Inferred content is marked as Type B/C derivation, never presented as canon.

### Layer 2: Simulation Engine
- **Files:** `src/main/java/dev/ergenverse/simulation/` (149+ Java files)
- **Purpose:** Runtime logic — WorldGraph traversal, ecology ticking, civilization evolution, opportunity resolution, artifact usage profiling, cultivation state machines.
- **Rule:** The simulation engine READS the canon database and worldgen data. It never hard-codes canon facts.

### Layer 3: Canon Worldgen Adapter (NEW)
- **Files:** `scripts/canon_worldgen_adapter.py` + generated JSONs in `data/ergenverse/worldgen/`
- **Purpose:** Reads the canon database and procedurally generates worldgen JSONs at scale. One country → 50+ sub-region biomes. One sect → 20+ sub-structure components. One herb → 8+ ecological variants.
- **Rule:** The adapter is RE-RUNNABLE. As the canon database grows, re-running the adapter generates more JSONs. No hand-writing individual biome JSONs unless they need manual tuning.

### Layer 4: Minecraft / Forge
- **Files:** `src/main/java/dev/ergenverse/` (mod entry point, capability registration, event handlers)
- **Purpose:** Render the worldgen data, handle player interaction, run the simulation engine ticks.
- **Rule:** Forge code is THIN. It registers capabilities, hooks events, and delegates to the simulation engine. All content lives in data JSONs.

## Scale Targets

| Category | Current (pre-pivot) | Target | Ratio |
|----------|-------------------|--------|-------|
| Biomes | 29 | 150-400 | 5-15x |
| Structures | 13 | 500-1500 | 40-115x |
| Configured Features | 16 | 200-500 | 12-30x |
| Placed Features | 16 | 200-500 | 12-30x |
| Dimensions | 8 | 15-25 | 2-3x |
| Total worldgen JSONs | 116 | 2,000-5,000 | 17-43x |
| Total data assets (incl. items, lang, etc.) | ~450 | 20,000+ | 45x |

## Generation Principles

### 1. Sub-Region Expansion (Biomes)
Every canon country/region expands into 30-50+ sub-region biomes:
- **Geographic variants:** farmland, river basin, hills, valley, forest, marsh, coast, plateau, canyon, mesa
- **Cultivation variants:** spirit herb hills, broken restriction valley, cultivator hunting grounds, hidden cave networks, collapsed spirit mine, minor spirit vein region
- **Historical variants:** ancient battlefield, abandoned [faction] battlefield, old caravan road, ruined outpost
- **Ecological variants:** mortals' hunting forest, fog marsh, beast territory, corrupted zone

Each sub-region has slightly different temperature, downfall, features, and spawners — derived from the parent country's `worldLawTier` and `spiritVeins` description.

### 2. Structure Decomposition (Sects/Cities)
Every canon sect/city expands into 20-60+ individual structure components:
- **Sect components:** outer gate, disciple dormitories, spirit herb garden, sword peak, core formation hall, library, secret pavilion, spirit beast pens, array hall, main plaza, inner sect, mountain cave, ancestor hall, hidden treasury, spirit spring, sword tomb, underground passage, trial grounds, puppet workshop, alchemy courtyard
- **City components:** city gate, market district, residential district, cultivator quarter, mortal quarter, port/docks, governor's mansion, tavern district, warehouse district, temple district, underground smuggler tunnels

Each component is a separate structure JSON + structure_set + template_pool, placed within the parent region's biome.

### 3. Spirit Vein Systems (Procedural)
Spirit veins are not single ores — they are procedural vein systems:
- **Tier variants:** minor, major, branch, dead, contaminated
- **Elemental variants:** wood, fire, earth, metal, water, heavenly, ancient, broken, concealed, beast, dragon
- **Properties:** flow, purity, pressure, density, Qi output, law affinity, element, age, fractures, guardian beasts

Each variant is a configured_feature (ore with custom block) + placed_feature (with rarity/height/biome filters).

### 4. Herb Ecological Variants
Every base herb expands into 8+ ecological variants:
- **Growth stages:** seedling, young, mature, ancient
- **Spiritual variants:** spirit, tribulation
- **State variants:** corrupted, dormant

Each variant has different rarity, placement constraints, and block stand-in.

### 5. Ocean Layers
Oceans are layered systems, not flat biomes:
- Surface → Shallow → Open Ocean → Sunlight Zone → Twilight Zone → Dark Zone → Abyss → Trench → Spirit Abyss → Ancient Abyss

Each layer has different pressure, visibility, species, resources, and laws.

### 6. Provenance Metadata
Every generated asset carries provenance:
- `canon_source`: which canon entry (L23, N25, I42, etc.) it derives from
- `derivation_type`: A (direct canon), B (canon-consistent inference), C (ecological/systemic inference)
- `parent_location`: the canon location it belongs to
- `generation_method`: which adapter pass produced it

## Adapter Implementation

The adapter (`scripts/canon_worldgen_adapter.py`) is a Python script that:
1. Reads `ri_canon_database.json`
2. For each location, applies generation templates based on location type
3. Writes generated JSONs to `data/ergenverse/worldgen/`
4. Updates `lang/en_us.json` with new entries
5. Outputs a generation report (counts, new files, provenance)

The adapter is designed to be **idempotent** — re-running it produces the same output (unless the canon DB changed). This means the cron can re-run it safely.

## Prime Directive Compliance

The adapter respects the Prime Directive: "Reality is objective; cultivation changes understanding, not existence."

- All generated biomes/features/structures **exist objectively** in the world.
- The **perception system** (Layer 2 simulation) determines what the player can see/interact with based on cultivation tier.
- A mortal walks through Zhao Farmland and sees wheat fields. A Qi Condensation cultivator sees the same fields PLUS the minor spirit vein beneath. A Soul Formation cultivator sees the full spirit vein network, the ancient battlefield imprint, and the collapsed mine.
- The worldgen data is the same for everyone. The simulation engine filters perception.
