# World Blueprint DSL v2 — Specification

> **Article of authority:** Prime Directive + Articles V, XXVI, XXIX, XXXIV, XXXIX.
> **Supersedes:** `worldgen/blueprint/planet_suzaku.json` (v1, object-oriented).
> **Status:** SCAFFOLDING for Chapter 1. DATA, not Java (Article XXVI). The
> existing IntentEngine / DecisionEngine / WorldEventBus / KnowledgeEngine /
> PerceptionEngine / BeliefRegistry / WorldHistory will read the COMPILED
> STATE produced from this DSL — they will NOT query this DSL directly.

---

## 0. Why v2 exists

The v1 blueprint (`planet_suzaku.json`) is **object-oriented**: it lists
`countries: [...]`, `settlements: [...]`, `roads: [...]`, `mountains: [...]`.
Each object is a self-contained record. This is sufficient to PLACE things
in the world but insufficient to SIMULATE the world, for two reasons the
Constitution now names explicitly:

1. **Article XXXIV (Relationships Are Graphs).** A village is not just a
   point with properties. It is `located_in Zhao`, `fed_by spirit_vein_a`,
   `protected_by zhao_patrol`, `supplied_by farmland_3`, `threatened_by
   wolf_pack_north`. When the wolves disappear, the simulation must
   immediately understand the consequences for the village. An object list
   cannot express this. A relationship graph can.

2. **Article XXXIX (Reality Has Momentum).** The world is not a frozen
   tableau placed at chunk-load. Every system has a trajectory — a current
   state, a rate of change, drivers, thresholds, consequences. The v1
   blueprint has no way to declare that the village's spirit vein is
   "thinning at 0.4 qi-density/year, has been for a decade, will cross the
   depletion threshold in 8 years unless a formation restores it." v2 makes
   momentum a first-class declaration on every entity.

The user's architectural directive is precise:

```
             ER GEN CANON
                   │
         Immutable Truths
                   │
          Historical Timeline
                   │
          World Blueprint DSL   ← this document
                   │
           World Compiler        ← produces Compiled World State
                   │
      ┌────────────┼────────────┐
      │            │            │
 Spatial DB   Relationship DB   Travel Graph   (+ Spirit, Hydrology,
      │            │            │    Settlement, Influence, Country,
      └────────────┼────────────┘    Biome, LOD)
                   │
        Living World State
         (already in motion)        ← momentum advanced every tick
                   │
      NPC AI • Ecology • Economy
      History • Opportunities
      Politics • Cultivation
                   │
         Minecraft Adapter          ← LAST concern (Art XXXIX.7)
                   │
      Blocks • Entities • UI
```

DSL v2 is the **authoring language**. The **compiler** turns it into the
**Compiled World State** (a set of graphs and indices the simulation
queries). Terrain is rendered LAST from the compiled state's semantic
fields (`traversable`, `steep`, `river`, `forest`, `spirit_rich`) — never
queried as raw block noise.

---

## 1. Core principle: store facts, not terrain

> "You're no longer storing terrain. You're storing geological facts.
>  Minecraft derives terrain from those facts." — user directive

A v1 `mountain` entry said: `{"id":"x","center":[x,z],"radius":200,"height":120}`.
That is terrain data. Minecraft must obey it exactly.

A v2 `MountainRange` entity says:

```json
{
  "id": "zhao_northern_range",
  "type": "MountainRange",
  "facts": {
    "origin":            {"x": 3600, "z": -1800},
    "elevation_profile": "ridge_along_northeast_axis",
    "rock_type":         "granite_with_spirit_quartz_veins",
    "spirit_density":    {"value": 0.62, "trend": "rising", "rate_per_year": 0.015},
    "weather_influence": "orographic_rain_on_southern_face",
    "travel_cost":       {"traversable": "partial", "passes": ["zhao_northern_pass"]},
    "erosion_rate":      {"value": 0.02, "unit": "blocks/year", "drivers": ["freeze_thaw", "river_cutting"]}
  },
  "momentum": { "...see §4..." }
}
```

None of these fields are block coordinates. They are **geological and
ecological facts**. The compiler derives block-level terrain FROM these
facts (and may do so differently per LOD region — a distant mountain is a
low-poly silhouette; the mountain the player is climbing is full block
resolution). The simulation does not care whether a hill is 96 or 101
blocks tall. It cares that it is `steep`, `traversable: partial`, `spirit-
rich`, and `eroding at 0.02 blocks/year`.

---

## 2. Entity types (nodes)

Every entity in the authored world is a NODE with:
- `id` — stable canonical id (never changes; referenced by relationships)
- `type` — one of the entity types below
- `canon_ref` — Er Gen source citation or `INFERRED (Art XV)`
- `facts` — semantic properties of this entity (NOT block data)
- `momentum` — declared trajectory (§4); REQUIRED for any entity the
  simulation treats as "living"; OPTIONAL (but discouraged) for pure
  decoration
- `affordances` — verbs this entity exposes (Article XXVIII / XXXVI);
  resolved into the universal Interaction catalog at compile time

### 2.1 Geographic entities
- `Continent` — landmass. facts: landmass_id, area_class, climate_band.
- `Country` — political region. facts: polygon (GIS), capital, political_tier,
  cultivation_power, biome_rule, ruling_faction.
- `MountainRange` — facts: origin, elevation_profile, rock_type,
  spirit_density, weather_influence, travel_cost, erosion_rate.
- `River` — facts: source, mouth, course (semantic: "winding southeast"),
  flow_class, flood_risk, spirit_mineral_deposition, navigation_class.
- `Forest` — facts: canopy_density, dominant_species, spirit_density,
  beast_density, foraging_yield, fire_risk.
- `Plain` / `Valley` / `Plateau` / `Wetland` / `Desert` — semantic terrain
  with traversability, fertility, spirit_density, exposure.
- `Coast` / `Sea` — maritime equivalents.

### 2.2 Civilizational entities
- `Settlement` — village/town/city. facts: population_class, founding_era,
  primary_economy, governing_body, spirit_vein_dependency, defense_class,
  decline_or_ascent_trend.
- `Sect` — cultivation sect. facts: tier, path_affinity, headcount_class,
  resource_pressure, doctrine, recruitment_status, internal_factions.
- `Stronghold` / `Outpost` / `Watchtower` — military nodes.
- `Market` / `Caravanserai` / `Harbor` — trade nodes.
- `Shrine` / `AncestralHall` / `CaveDwelling` — spiritual nodes.
- `Farmland` / `SpiritHerbGarden` / `Mine` / `Quarry` — resource nodes.

### 2.3 Infrastructural entities
- `Road` — a NAVIGATION GRAPH EDGE, not a spline. facts: surface_class,
  maintenance_state, traffic_class, toll_class, danger_class, seasonality.
  A road is composed of `RoadSegment` nodes connected by the travel graph.
- `RoadSegment` — atomic edge between two waypoints. facts: length_class,
  terrain_crossed, bridge_required, pass_required, spirit_density_along.
- `Bridge` / `MountainPass` / `Ferry` — chokepoint nodes on the travel graph.
- `SpiritVein` — THE GRID. facts: qi_density, depth_class, element_affinity,
  fluctuation_period, connected_settlements (via spirit graph).
- `Formation` — a placed array that modifies local reality (qi density,
  weather, restriction). facts: type, integrity, owner, effect.

### 2.4 Ecological entities
- `BeastPopulation` — facts: species_id, count_class, territory_polygon,
  prey_density, aggression_class, migration_pattern.
- `FloraPatch` — facts: species_id, extent, ripeness, potency, maturation_eta.
- `SpiritHerbCluster` — specialized FloraPatch with spirit-density dependency.

### 2.5 Social / political entities
- `Faction` — facts: ideology, strength_class, grievance_ledger_id,
  ambition, territory_claim.
- `Family` / `Clan` — facts: lineage, head, members (refs to NPCs),
  standing_in_settlement.
- `Grudge` — a first-class entity. facts: parties, origin_event, severity,
  festering_rate, flashpoint_proximity. (Article XXXIX.2.)

### 2.6 Trajectory entities (mobile)
- `Caravan` — facts: route_id, position_along_route, supplies, morale,
  threat_exposure, cargo, origin, destination, ETA.
- `Patrol` — military equivalent.
- `WanderingCultivator` — an NPC-class trajectory entity (the Heng Yue
  recruiter en route to Wang Village is one of these).
- `Rumor` — facts: origin, subject, belief_intensity, spread_vector,
  distortion_rate, reach, age.

### 2.7 Individual entities
- `NPC` — referenced by id; full definition lives in `npcs/` and
  `living_chapters/chapter_1_wang_family_village/motivation_state_*.json`.
  The blueprint references NPCs as endpoints of relationships and as
  occupants of settlements. The NPC's own momentum is its motivation_state
  + capability_score + relationship_graph, advanced by the character
  reasoning pipeline (Article XXXV).
- `Player` — a special NPC whose arrival time is the Simulation Seed's
  responsibility. The player is NOT in the blueprint (Article V). The
  player ARRIVES into the compiled state.

---

## 3. Relationship types (edges)

Relationships are FIRST-CLASS. The compiler builds the Relationship Graph
and several specialized sub-graphs (Travel, Spirit, Hydrology, Settlement,
Influence) from them. Every relationship is **directed** and **typed**.

### 3.1 Spatial / political
- `located_in`        — A located_in B (Settlement located_in Country)
- `contained_by`      — physical containment (CaveDwelling contained_by MountainRange)
- `borders`           — A borders B (Country borders Country); symmetric
- `claims`            — Faction claims Territory (disputed; see Influence Map)

### 3.2 Supply / dependency (the "electrical grid")
- `fed_by`            — Settlement fed_by SpiritVein (qi supply)
- `supplied_by`       — Settlement supplied_by Farmland / Mine / Caravan route
- `drains`            — Mine drains SpiritVein (resource extraction)
- `recharges`         — Formation recharges SpiritVein (restoration)
- `depends_on`        — generic dependency (Sect depends_on SpiritVein for cultivation)

### 3.3 Protection / threat
- `protected_by`      — Settlement protected_by Patrol / Sect / Formation
- `threatened_by`     — Settlement threatened_by BeastPopulation / BanditGroup / Faction
- `raids`             — BanditGroup raids Road / Settlement
- `defends`           — Patrol defends Road / Settlement
- `menaces`           — WanderingCultivator menaces Settlement (transient)

### 3.4 Connectivity (the travel graph)
- `connected_to`      — Waypoint connected_to Waypoint via RoadSegment
- `traverses`         — Road traverses MountainPass / Bridge / Ferry
- `route_of`          — RoadSegment route_of Road
- `leads_to`          — Road leads_to Settlement (terminal edge)

### 3.5 Hydrology
- `drains_into`       — River drains_into River / Sea
- `fed_by_spring`     — River fed_by_spring SpiritVein (spirit-mineral deposition)
- `floods`            — River floods Plain / Settlement (seasonal)

### 3.6 Ecological
- `preys_on`          — BeastPopulation preys_on FloraPatch / BeastPopulation
- `grazes_in`         — BeastPopulation grazes_in Plain / Forest
- `migrates_between`  — BeastPopulation migrates_between A and B
- `pollinated_by`     — FloraPatch pollinated_by BeastPopulation

### 3.7 Social / political
- `governs`           — Faction governs Settlement / Country
- `allied_with`       — Faction allied_with Faction
- `at_war_with`       — Faction at_war_with Faction
- `owes`              — NPC/Faction owes NPC/Faction (callable debt; Art XXXIV.2)
- `grudge_against`    — Party grudge_against Party (Grudge entity reference)
- `recruits_from`     — Sect recruits_from Settlement / Region
- `sworn_to`          — NPC sworn_to NPC / Faction

### 3.8 Informational
- `origin_of`         — Settlement origin_of Rumor
- `spread_to`         — Rumor spread_to Settlement (time-stamped edge)
- `witnessed_by`      — Event witnessed_by NPC (memory ledger coupling)
- `known_by`          — Fact known_by NPC (knowledge registry coupling)

Every relationship edge in the DSL carries:
- `from` — entity id
- `to`   — entity id
- `type` — one of the above
- `since` — when the relationship began (canon tick or historical event id)
- `strength` — 0.0–1.0 for soft relationships (allied_with, spread_to);
  omit for hard structural relationships (located_in, drains_into)
- `canon_ref` — Er Gen source or INFERRED
- `momentum` — OPTIONAL: if the relationship itself has a trajectory (e.g.
  an `allied_with` that is decaying into neutrality, a `grudge_against`
  that is festering toward flashpoint)

---

## 4. Momentum declarations (Article XXXIX.2)

Every entity the simulation treats as "living" MUST declare a `momentum`
block. A system WITHOUT a declared momentum is scenery (Article XXXIX.2).

### 4.1 The universal momentum shape

```json
"momentum": {
  "system": "spirit_vein | sect | caravan | elder_breakthrough | herb | bandit | wolf_pack | rumor | river_erosion | faction_grudge | economy | ...",
  "schema_ref": "ergenverse:worldgen/blueprint/v2/momentum/<system>_momentum.schema.json",
  "current_state": { "...system-specific fields..." },
  "rate_of_change": { "...per-tick deltas and their drivers..." },
  "drivers": [ "string — what is pushing this trajectory forward or backward" ],
  "thresholds": [
    {
      "name": "depletion | flashpoint | maturation | harvest_window | scarcity | ...",
      "condition": "expression — when this is true, the threshold is crossed",
      "consequence": "string — what happens to the world when crossed",
      "irreversible": "bool — can the trajectory be restored after crossing?"
    }
  ],
  "interruptible_by": [ "string — interventions that redirect this trajectory (Art XXXIX.6)" ],
  "perceivable_without_notification": "string — how a player could perceive this momentum WITHOUT being told (Art XXXIX.5). If empty, the momentum is invisible to the player and fails the Momentum Test.",
  "compiled_into": "string — which compiled-state graph this momentum advances (spirit_graph | settlement_graph | travel_graph | ...)"
}
```

### 4.2 The Momentum Test gate (Article XXXIX.5)

Every momentum declaration's `perceivable_without_notification` field is
GATED. If it is empty or vague ("the player will notice"), the entity
FAILS the Momentum Test and the chapter containing it cannot be declared
living. The field must describe a concrete, ambient, unannounced cue:
fresh incense at an abandoned shrine, a half-harvested garden, wagons in
the distance, two villagers mid-argument about a days-old rumor.

### 4.3 Momentum is compiled, not scripted (Article XXXIX.4)

The momentum block declares the trajectory's SHAPE and DRIVERS. The
compiler seeds each momentum to its canonical initial state (derivable
from the Historical Timeline + Simulation Seed + player arrival time).
The simulation advances it every tick using the declared drivers. The
same blueprint + a different arrival time yields a different present
moment. This is the replayability mechanism.

---

## 5. The compile target: Compiled World State

The DSL is NOT queried at runtime. The World Compiler reads the DSL and
produces the Compiled World State — a set of graphs and indices the
simulation queries in O(1) or O(log n). The schemas for each compiled
artifact live in `compiled_world_state/`:

| Artifact | Schema file | Purpose |
|---|---|---|
| Spatial Index | `spatial_index.schema.json` | Point/region lookup: "what entities are at/near (x,z)?" |
| Relationship Graph | `relationship_graph.schema.json` | All edges, queryable by (from, type) or (to, type) |
| Travel Graph | `travel_graph.schema.json` | Navigation graph for caravans/patrols/refugees/NPCs |
| Spirit Graph | `spirit_graph.schema.json` | Spirit vein → settlements/sects/formations; qi flow |
| Hydrology Graph | `hydrology_graph.schema.json` | Rivers, springs, flood plains, spirit-mineral deposition |
| Settlement Graph | `settlement_graph.schema.json` | Settlement ↔ suppliers/defenders/threats/veins |
| Influence Map | `influence_map.schema.json` | Faction influence per region; disputed zones |
| Country Lookup | `country_lookup.schema.json` | Point-in-polygon: "which country is (x,z) in?" |
| Biome Lookup | `biome_lookup.schema.json` | Point → biome_rule (drives chunk rendering, LAST) |
| LOD Regions | `lod_regions.schema.json` | Distant/medium/near render fidelity per region |
| Momentum Registry | `momentum_registry.schema.json` | Master index of all live trajectories, tick-advanced |

### 5.1 Chunks are stateless renderers (Article XXXIX.7)

A chunk, on load, does NOT ask "should I place Wang Village here?" The
simulation already knows Wang Village exists (it is in the Compiled World
State). The chunk asks the Spatial Index: "what entities intersect my
bounding box?" and renders them. The chunk is a **renderer**, not an
authority. This is the inversion the user demanded.

### 5.2 Terrain is derived LAST (Article XXXIX.7)

The compiler produces semantic fields per region: `traversable`, `steep`,
`river`, `forest`, `spirit_rich`, `elevation_band`. The Minecraft adapter
converts these into blocks at render time. A hill's exact height (96 vs
101 blocks) is the adapter's business, governed by the LOD region's
fidelity. The simulation never queries block height. It queries `steep`.

---

## 6. Canon confidence tagging

Every entity and relationship carries `canon_ref`:
- `EXPLICIT` — directly stated in Er Gen RI canon
- `INFERRED (Art XV)` — derived from canonical relationships/travel times
- `AUTHORED` — invented for simulation coherence, no Er Gen source
  (must be flagged; subject to canon review)

This is inherited from v1. The v2 DSL preserves it because Article I
(Canon Is Reality) and Article XV (Inference Discipline) are immutable.

---

## 7. Relationship to existing Chapter 1 scaffolding

The v2 DSL does NOT replace the Chapter 1 social scaffolding
(`living_chapters/chapter_1_wang_family_village/`). It COMPLEMENTS it:

- `motivation_state_*.json` (Art XXXIII/XXXV) → NPC trajectory momentum.
  The NPC's momentum IS its motivation_state + capability_score +
  relationship_graph, advanced by the character reasoning pipeline.
- `relationship_graph_*.json` (Art XXXIV) → the SOCIAL sub-graph of the
  compiled Relationship Graph.
- `memory_ledger_*.json` (Art XXXI.5) → written by momentum threshold
  crossings and by NPC interactions.
- `living_events_*.json` → DEPRECATED in favor of momentum thresholds
  (Art XXXIX.4: "Momentum is not a scripted event sequence"). Existing
  living events are re-expressed as momentum threshold crossings where
  possible. Pure scheduled events (seasonal festivals) remain.
- `affordance_*.json` (Art XXVIII/XXXVI) → compiled into each entity's
  affordance set at compile time.

The v2 DSL is the GEOGRAPHIC + ECOLOGICAL + CIVILIZATIONAL layer. The
Chapter 1 social scaffolding is the CHARACTER layer. They interlock at
the NPC entity boundary: an NPC is a node in the v2 DSL (occupant of a
settlement, endpoint of relationships) AND the subject of a
motivation_state (character reasoning pipeline).

---

## 8. File layout

```
worldgen/blueprint/v2/
├── dsl_specification.md              ← this file
├── planet_suzaku.blueprint.json      ← the v2 planet (future)
├── wang_family_village.blueprint.json ← the canonical Chapter 1 reference (this cycle)
├── compiled_world_state/             ← schemas for compiler output
│   ├── spatial_index.schema.json
│   ├── relationship_graph.schema.json
│   ├── travel_graph.schema.json
│   ├── spirit_graph.schema.json
│   ├── hydrology_graph.schema.json
│   ├── settlement_graph.schema.json
│   ├── influence_map.schema.json
│   ├── country_lookup.schema.json
│   ├── biome_lookup.schema.json
│   ├── lod_regions.schema.json
│   └── momentum_registry.schema.json
└── momentum/                         ← per-system momentum schemas (Art XXXIX.2)
    ├── spirit_vein_momentum.schema.json
    ├── sect_momentum.schema.json
    ├── caravan_momentum.schema.json
    ├── elder_breakthrough_momentum.schema.json
    ├── herb_momentum.schema.json
    ├── bandit_momentum.schema.json
    ├── wolf_pack_momentum.schema.json
    ├── rumor_momentum.schema.json
    ├── river_erosion_momentum.schema.json
    └── faction_grudge_momentum.schema.json
```

---

## 9. What this DSL does NOT do (Article XXVI / XXIX guardrails)

- Does NOT define new Java engines. The 10 social engines (Art XXXI.1)
  are DATA SCHEMAS read by existing engines. Momentum schemas are the
  same: DATA the existing WorldEventBus / DecisionEngine will advance.
- Does NOT expand geographically beyond Chapter 1's scope (Art XXIX).
  Wang Family Village + its immediate relationships (the spirit vein it
  depends on, the road to Heng Yue, the wolf pack threatening it, the
  recruiter en route) is the authored scope this cycle.
- Does NOT render terrain. Terrain is the adapter's job, done LAST
  (Art XXXIX.7). This DSL stores facts; the adapter derives blocks.
- Does NOT special-case the player (Art V, XXXV.2). The player is not
  in the blueprint. The player arrives into the compiled state.

---

## 10. Success criterion for this DSL

The DSL succeeds when, given only `wang_family_village.blueprint.json`,
a compiler could produce a Compiled World State in which:

1. At least THREE systems have visible momentum at player-arrival time
   (Art XXXIX.5 — the Momentum Test).
2. The simulation can answer "what threatens Wang Village right now?"
   by querying the Settlement Graph — not by reading a scripted event.
3. The simulation can answer "if the wolf pack is eliminated, what
   changes?" by traversing the Relationship Graph — not by hardcoded
   if/else.
4. The simulation can answer "where is the Heng Yue recruiter right
   now?" by querying the Travel Graph + the recruiter's momentum —
   not by a scheduled quest.
5. A chunk renderer can ask "what entities intersect this box?" and
   get an answer from the Spatial Index — without the chunk owning
   any geography.

If all five hold, the world is authored, compiled, and in motion.
The player arrives mid-sentence. That is the bar.
