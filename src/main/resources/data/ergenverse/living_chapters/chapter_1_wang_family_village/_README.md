# Living Chapter 1 — Wang Family Village

**Constitution articles governing this chapter:** I–XXXI, Prime Amendment,
Final Questions, Living Chapters, Gold-Standard Location Template (XXXI.4),
Memory Metric (XXXI.5).

**The stronger metric (Article XXXI.3):**
> Could somebody who never read Renegade Immortal accidentally
> recreate Wang Lin's early life?

Not "can the player spend two hours here?" but "do the events that
shaped Wang Lin simply happen around the player — wandering cultivator
arrival, rumors, disappearances, elder's worry, strange jade, silent
birds, restless wolves — with no quest marker, tutorial, or narrator?"

This directory holds the DATA SCHEMAS that make that possible. They are
not Java classes (Article XXVI forbids gratuitous architecture). The
existing IntentEngine, DecisionEngine, WorldEventBus, KnowledgeEngine,
PerceptionEngine, BeliefRegistry, and WorldHistory will READ these
schemas and act on them once JDK is restored.

## Current assets (audit — as of AUTO-CANON-051)

### Furnished (scaffolding layer — exists, but not yet "alive")
- **Structures:** 13 (village center + 12 districts: warehouse, governor
  mansion, market, ancestral hall, memorial shrine, temple, residential,
  port docks, city gate, tavern, smuggler tunnels, cultivator quarter,
  mortal quarter, Wang Lin childhood home).
- **Template pools:** 15 (matching).
- **Loot tables:** 12 (main, market, tavern, governor mansion, warehouse,
  memorial shrine, temple district, city gate, port docks, childhood
  home, ancestral hall, mortal quarter, cultivator quarter, residential,
  smuggler tunnels).
- **NPCs pinned to location:** 9 (npc_wang_ping, npc_wang_tianshui,
  npc_zhou_tingsu, npc_da_niu, npc_zhou_rui, npc_wang_qingyue,
  npc_wang_tianshan, npc_heng_yue_recruiter, npc_zhang_tian).
- **Ecology file:** wang_family_village_ecology.json (4 beasts, 7 NPCs).
- **Civilization file:** civilizations/wang_family_village.json
  (population 520, economy, politics, lifecycle, perception tiers).
- **Species:** wang_family_hunting_dog.json (the dog from the Memory
  Metric canonical test — exists as data, not yet as a remembered being).

### Missing (the desire-driven layer — what this directory defines)
1. **Desire-state** on NPCs — what each NPC wants/offers/teaches each
   cycle. (Article XXXI.1) → `desire_state_schema.json` + per-NPC files.
2. **Affordances** on entities — verbs each NPC/item/beast/structure
   exposes. (Article XXVIII) → `affordance_schema.json` + populated file.
3. **Social engine templates** — the 10 engines as data schemas.
   (Article XXXI.1) → `social_engines_schema.json`.
4. **Memory ledger** — persistent remembered events, witnesses,
   retelling, decay/misremembering. (Article XXXI.5) →
   `memory_ledger_schema.json` + `memory_ledger_seed.json`.
5. **Capability scoring** — per-NPC assessment across 10+ dimensions,
   replacing binary complete/incomplete. → `capability_score_schema.json`.
6. **Living events** — seasonal, random, triggered events beyond
   daily_schedule. → `living_events_wang_family_village.json`.
7. **Wang Lin bidirectional protocol** — how Wang Lin approaches the
   player unbidden, the Request Capability mechanic. (Article XXXI.2) →
   `wang_lin_bidirectional_protocol.json`.

## Gold-Standard Location Template audit (Article XXXI.4)

| # | Dimension     | Status | Evidence |
|---|---------------|--------|----------|
| 1 | Ecology       | PARTIAL | 4 beasts listed, no food chain graph, no seasons, no spirit vein wiring |
| 2 | Society       | PARTIAL | 9 NPCs with schedules, no relationship graph, no disputes/celebrations |
| 3 | Economy       | PARTIAL | civilization.json declares economy fields, no production/trade/shortage simulation |
| 4 | History       | FAIL    | no memory ledger, no persistent consequences, no remembered events |
| 5 | Opportunities | FAIL    | no living event definitions, no dynamic emergence |
| 6 | Affordances   | FAIL    | no affordance schema on any entity |
| 7 | Mentorship    | FAIL    | NPCs have sect_tasks (transactional) but no teaching/challenge/recruit/mentor initiation |
| 8 | Conflict      | PARTIAL | civilization.json lists bandits as enemies, no faction simulation, no hidden agendas |
| 9 | Exploration   | FAIL    | no observation-based discovery hooks, secrets are loot tables not discoveries |
| 10| Evolution     | FAIL    | no change-over-time model, village resets on chunk unload |

**Verdict:** Wang Family Village is FURNISHED, not LIVING. 0/10 dimensions
pass. This directory exists to close that gap, one schema at a time.

## Article XXVII five-question audit

1. Does something happen here without the player? **NO** — schedules
   loop but nothing emerges.
2. Can the player discover instead of being told? **PARTIAL** — loot
   tables exist but no observation-driven discovery.
3. Can NPCs affect each other? **NO** — NPCs have no cross-NPC desires.
4. Can the location change over time? **NO** — no evolution model.
5. Would returning weeks later reveal different circumstances? **NO** —
   village state is static.

**Verdict:** 0/5 pass. Furnished, not complete.

## Memory Metric audit (Article XXXI.5)

The canonical test (Old Chen's dog → wolf attack → "used to have one"
→ child retells months later) **cannot occur** today. The dog exists as
a species file, the wolf exists as a beast, but there is no memory
ledger to record the death, no retelling system, no decay. The world
does not remember.

## What this cycle (AUTO-CANON-051) delivers

- `desire_state_schema.json` — the data schema for per-cycle NPC desire.
- `desire_state_wang_ping.json` — a fully populated example for Wang
  Ping (Wang Lin's brother), showing how desire-state makes him
  initiate rather than wait.
- `social_engines_schema.json` — all 10 social engines as data templates.
- `memory_ledger_schema.json` — the schema for persistent memory.
- `memory_ledger_seed.json` — seed memory events including the
  canonical dog/wolf test, pre-staged for when the simulation runs.
- `capability_score_schema.json` — multi-dimensional NPC scoring.
- `affordance_schema.json` — the verb-ontology schema for entities.
- `affordances_wang_family_village.json` — populated affordances for
  key village entities (Wang Ping, the hunting dog, the childhood home
  jade, the irrigation ditch, the village elder).
- `living_events_wang_family_village.json` — seasonal, triggered, and
  random events including the wandering cultivator's arrival.
- `wang_lin_bidirectional_protocol.json` — how Wang Lin approaches the
  player unbidden (Article XXXI.2).

## How Java will wire this (when JDK is restored — NOT this cycle)

The existing engines read these schemas as follows:
- **IntentEngine** reads `desire_state_*.json` each NPC tick, scores
  candidate desires, and emits a `SocialIntent` (new IntentNature) when
  a desire's conditions are met and priority exceeds threshold.
- **DecisionEngine** resolves the `SocialIntent` to a concrete action
  (approach, gesture, speak, gift, follow) using the engine template
  from `social_engines_schema.json`.
- **WorldEventBus** publishes the initiated interaction so other NPCs
  can perceive it (PerceptionEngine) and remember it (WorldHistory +
  the new memory ledger).
- **WorldHistory** writes a memory entry to `memory_ledger_seed.json`
  (or a runtime copy) when a significant event fires, including
  witnesses and retelling hooks.
- **KnowledgeEngine** lets NPCs who witnessed an event retell it later,
  with distortion based on the event's `memory_decay` and
  `distortion_over_time` fields.

No new Engine/Subscriber/Bus class is required (Article XXVI). The
social engines are DATA; the existing engines gain one new IntentNature
(`SOCIAL_INITIATION`) and read these JSONs. That is the extent of the
Java change.

## Status

- **Schema definitions:** COMPLETE this cycle (data-only, JDK-blocked
  from runtime).
- **Populated examples:** COMPLETE for Wang Ping + key village entities.
- **Java wiring:** BLOCKED (no JDK). Tracked as the next phase.
- **Living Chapter 1 completion:** 0/10 Gold-Standard dimensions pass.
  This cycle moves dimensions 4 (History), 6 (Affordances), 7
  (Mentorship) from FAIL to SCHEMA-READY. They pass only when Java
  wires them and a player experiences them (Article XXX).
