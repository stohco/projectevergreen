# Item/Skill/Technique/Artifact Graph Layers — Extended Architecture

## The Rule

> If the player can own it, learn it, equip it, consume it, craft it,
> drop it, trade it, or unlock it, it must have a graph projection and
> a runtime path.

## Revised Layer Stack (expanded from 30 to 40 layers)

```
Canon Kernel
  → Semantic World Graph
  → Spatial Graph
  → Settlement Graph
  → Actor Graph
  → Relationship Graph
  → Memory Graph
  → Event Graph
  → Opportunity Graph
  → Ecology Graph
  → Economy Graph
  → Cultivation Graph
  → Item Graph          ← NEW
  → Inventory Graph     ← NEW
  → Equipment Graph     ← NEW
  → Skill Graph         ← NEW
  → Technique Graph     ← NEW
  → Artifact Graph      ← NEW
  → Crafting Graph      ← NEW
  → Recipe Graph        ← NEW
  → Formation Graph
  → Damage / Deformation Graph
  → Physics / Collision Graph
  → Template Library
  → Compiler
  → Runtime Deltas
  → Three.js Materialization
```

## Layer Specifications

### Item Graph
Every item is a semantic node with: name, type, rarity, provenance,
owner, binding, material, current state, hidden capabilities.

Answers: What is this item? Who owns it? Can it be used? Consumed?
Refined? Traded? Is it canon-related?

Data source: ri_canon_artifacts_enriched.json (178 artifacts),
ri_canon_database.json (178 artifacts), CANON_RI_COMPLETE_ITEMS.md.

### Inventory Graph
Containment and access: bag, storage ring, chest, pocket dimension,
sect vault, ground loot, corpse inventory.

Answers: What is inside what? What is accessible? Equipped? Hidden?
Stolen? Sealed?

### Equipment Graph
Wearables and held items: weapon slots, robe slots, accessory slots,
storage slots, talisman slots, spirit tool slots.

Answers: What is equipped? Active? Modifies performance? Visually shown?

### Skill Graph
Learning and use: prerequisites, realm gates, affinity, mastery, synergy,
incompatibility, Dao dependence, passive vs active.

Answers: Can this actor learn it? What unlocks it? Costs? Body/qi changes?
Items/environments that make it stronger?

### Technique Graph
Techniques with: type (cultivation method, spell, secret art, body
refinement, divine ability), practitioner, effects, chapter learned.

Data source: ri_canon_techniques_enriched.json (184 techniques),
CANON_RI_COMPLETE_TECHNIQUES.md.

### Artifact Graph
Capability objects: flying swords, storage rings, soul gourds, talismans,
formation cores, spirit lamps, divine tools.

Answers: What capabilities does it expose? Cultivation interaction?
Realm usage? Containment/emission? Visual effects?

### Crafting Graph
Production: pills, artifacts, talismans, formations, tools, consumables,
upgrades, refinement chains.

Answers: Materials required? Station needed? Realm needed? Failure states?
Quality tiers?

### Recipe Graph
Recipe nodes with: inputs, outputs, station, realm requirement, success
rate, quality tiers, mastery unlocks.

Data source: ALCHEMY_REFERENCE.ts, FORMATIONS_REFERENCE.ts,
TALISMANS_REFERENCE.ts, REFINING_REFERENCE.ts.

## 10-Cycle Implementation Priority (Revised)

| Cycle | Goal | Why |
|-------|------|-----|
| 1 | Split WorldCanvas into Render/Input/Sim | Removes largest architectural violation |
| 2 | Boot GraphLayerSystem.buildAll() | Makes graph architecture real |
| 3 | Canon bootstrap from JSON for ALL data | Stops hardcoded lore |
| 4 | Item + Inventory + Equipment graphs | Prevents item system from being afterthought |
| 5 | Skill + Technique + Artifact graphs | Makes learning and equipment systemic |
| 6 | Wire Memory Graph into cognition | NPCs remember what matters |
| 7 | Event + Opportunity graphs | World becomes causally alive |
| 8 | Crafting + Recipe + Refinement graphs | Pills, talismans, alchemy become real |
| 9 | Economy + Ecology + Beast loot graphs | Resources, drops, habitats connect |
| 10 | Formation + Deformation + QA + Space | Cultivation physics + future expansion |

## Data-Driven From Day One

All of these must come from JSON/MD/canonical registry data:
- items, skills, techniques, artifacts, recipes
- beast drops, equipment slots, technique prerequisites
- body arts, divine sense techniques, cultivation requirements
- talent/affinity restrictions, crafting stations, formation nodes
- loot tables, reward tables

## PRD Audit Precision

Domain-level tracking, not coarse "Phase 2 60%":

| Domain | Status | Notes |
|--------|--------|-------|
| Items | 20% | Some artifacts in canon DB, no item graph |
| Skills | 10% | NPCCognition has motivation, no skill system |
| Techniques | 15% | 184 techniques in JSON, no graph projection |
| Artifacts | 30% | Canon DB has 178, no capability graph |
| Crafting | 5% | Design docs only, no runtime |
| Inventory | 40% | WorldDeltaStore exists, no item containment |
| Equipment | 0% | Nothing |
| Recipes | 5% | Reference files exist, no runtime |
| Beast drops | 0% | Beasts spawn but don't drop |
| Formations | 5% | Design doc + block, no runtime |
