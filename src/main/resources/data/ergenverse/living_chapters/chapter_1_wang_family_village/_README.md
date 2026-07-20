# Living Chapter 1 — Wang Family Village

**Constitution articles governing this chapter:** I–XXXVIII, Prime Amendment,
Final Questions, Living Chapters, Gold-Standard Location Template (XXXI.4),
Memory Metric (XXXI.5), Character-First Pipeline (XXXV), Universal
Interaction (XXXVI), Three-Layer Learning Conversation UI (XXXVII),
Capability Compatibility (XXXVIII).

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

## Current assets (audit — as of AUTO-CANON-056)

### Furnished (scaffolding layer — exists since 051)
- **Structures:** 13 (village center + 12 districts: warehouse, governor
  mansion, market, ancestral hall, memorial shrine, temple, residential,
  port docks, city gate, tavern, smuggler tunnels, cultivator quarter,
  mortal quarter, Wang Lin childhood home).
- **Template pools:** 15 (matching).
- **Loot tables:** 12.
- **NPCs pinned to location:** 9 (npc_wang_ping, npc_wang_tianshui,
  npc_zhou_tingsu, npc_da_niu, npc_zhou_rui, npc_wang_qingyue,
  npc_wang_tianshan, npc_heng_yue_recruiter, npc_zhang_tian).
- **Ecology file:** wang_family_village_ecology.json (4 beasts, 7 NPCs).
- **Civilization file:** civilizations/wang_family_village.json
  (population 520, economy, politics, lifecycle, perception tiers).
- **Species:** wang_family_hunting_dog.json (the dog from the Memory
  Metric canonical test).

### Desire-driven / motivation-driven layer (051 → 056)
- **Motivation state (Art XXXIII, v2):** 9/9 NPCs POPULATED with
  universal `relevance_to_targets`. Wang Lin re-authored under v2.
  `motivation_state_schema.json` + 9 per-NPC files.
- **Relationship graph (Art XXXIV):** 31 key seeds POPULATED (10
  dimensions each — trust, affection, fear, respect, gratitude,
  grievance, obligation, familiarity). `relationship_graph_schema.json`
  + `relationship_graph_seeds.json` + `relationship_graphs_seed_chapter_1.json`.
- **Favor / debt ledger (Art XXXIV.2):** 9/9 NPC ledgers POPULATED.
  Wang Lin (2 staged favors), Wang Tianshui (1), Zhou Rui (2) carry
  conditional templates; others start empty at chapter start.
  `favor_debt_ledger_schema.json` + 9 `favor_ledger_npc_*.json`.
- **Conversation system (Art XXXII):** 3 modes (environment 90% /
  focused / major), "holding still is courtesy not freeze" (§XXXII.1a),
  verb affordances. `conversation_system_schema.json` +
  `conversation_affordances_per_npc.json` (6 NPCs).
- **Conversation UI (Art XXXVII):** 3-layer learning UI schema.
  `conversation_ui_schema.json`.
- **NPC initiation consent pipeline:** 8-stage gate before any NPC
  initiates. `npc_initiation_consent_pipeline.json`.
- **Approach deconfliction:** 7-step algorithm so NPCs don't mob the
  player. `approach_deconfliction_schema.json`.
- **Character reasoning pipeline (Art XXXV):** `character_reasoning_pipeline_schema.json`.
- **Capability compatibility (Art XXXVIII):** `capability_compatibility_schema.json`
  + `capability_score_schema.json`.
- **Universal interaction (Art XXXVI):** `universal_interaction_schema.json`
  + `interaction_sets_non_npc_entities.json` (6 entities, 27 interactions).
- **NPC interaction sets:** `interaction_sets_chapter_1.json` (9/9 NPCs).
- **Social engines (Art XXXI.1):** `social_engines_schema.json` (10 engines).
- **Memory ledger (Art XXXI.5):** `memory_ledger_schema.json` +
  `memory_ledger_seed.json` (12 seeds incl. canonical dog/wolf test).
- **Affordances (Art XXVIII):** `affordance_schema.json` +
  `affordances_wang_family_village.json`.
- **Living events:** `living_events_wang_family_village.json` (6 events).
- **Wang Lin bidirectional protocol (Art XXXI.2):** `wang_lin_bidirectional_protocol.json`.
- **Economy (Gold-Standard dim 3):** `economy_schema.json` — 7 resources,
  1 trade route, taxation, hidden economy, seasonal cycle, 5 exchange rates.
- **Conflict (Gold-Standard dim 8):** `conflict_schema.json` — 4
  interlocking conflicts (Teng oppression, wolf threat, cultivation gap,
  Zhang Tian's shame), 4 escalation levels each, resolution paths,
  grievance propagation rules, inter-conflict dynamics.
- **Legacy (SUPERSEDED):** `desire_state_schema.json` +
  `desire_state_wang_ping.json` — pre-v2, kept for reference only.

## Gold-Standard Location Template audit (Article XXXI.4)

| # | Dimension     | Status        | Evidence |
|---|---------------|---------------|----------|
| 1 | Ecology       | SCHEMA-READY  | ecology data in location JSON + bestiary |
| 2 | Society       | SCHEMA-READY  | 31 relationship graph seeds + 9 interaction sets + 9 favor ledgers |
| 3 | Economy       | SCHEMA-READY  | economy_schema.json — 7 resources, trade route, taxation, seasonal cycle |
| 4 | History       | SCHEMA-READY  | memory_ledger_seed.json (12 seeds) + memory_ledger_schema.json |
| 5 | Opportunities | SCHEMA-READY  | living_events_wang_family_village.json (6 events, multiple triggers) |
| 6 | Affordances   | SCHEMA-READY  | affordance_schema.json + affordances_wang_family_village.json + interaction_sets (NPC + non-NPC) |
| 7 | Mentorship    | SCHEMA-READY  | motivation_state_wang_lin (m_call_in_favor_ji_realm), zhou_rui (m_share_hunt_knowledge), heng_yue_recruiter (m_test_village_youth) |
| 8 | Conflict      | SCHEMA-READY  | conflict_schema.json — 4 conflicts, escalation, propagation, inter-conflict dynamics |
| 9 | Exploration   | SCHEMA-READY  | living_events (observation trigger for strange_jade), affordances (ancestral_hall, south_bend, childhood_home) |
| 10| Evolution     | SCHEMA-READY  | reasoning pipeline + memory ledger + seasonal cycles + conflict escalation |

**Verdict:** Wang Family Village is **SCHEMA-COMPLETE** — 10/10 Gold-Standard
dimensions have formal schema backing. Dimensions pass at runtime only when
Java wires them and a player experiences them (Article XXX). The data layer
for Chapter 1 is complete; the sole remaining gap is Java wiring (BLOCKED —
no JDK 17 in sandbox).

## Article XXVII five-question audit

1. Does something happen here without the player? **SCHEMA-READY** —
   economy runs on NPC production/consumption; conflicts escalate on
   grievance thresholds; Teng family collects tax; wolves hunt — all
   independent of the player.
2. Can the player discover instead of being told? **SCHEMA-READY** —
   observation verbs on every non-NPC entity; family mark, jade residue,
   wolf tracks, irrigation damage all discoverable without instruction.
3. Can NPCs affect each other? **SCHEMA-READY** — 31 relationship graph
   seeds, grievance propagation, shared-enemy bonding, favor ledgers.
4. Can the location change over time? **SCHEMA-READY** — seasonal cycles,
   conflict escalation, memory decay/misremembering, favor expiration.
5. Would returning weeks later reveal different circumstances? **SCHEMA-READY** —
   conflict escalation levels, economy stockpile decay, memory retelling
   distortion, relationship drift.

**Verdict:** 0/5 pass at runtime. 5/5 SCHEMA-READY.

## Memory Metric audit (Article XXXI.5)

The canonical test (Old Chen's dog → wolf attack → "used to have one"
child retells months later) **cannot occur today at runtime**, but is
fully STAGED: the dog species exists, the wolf exists, the memory ledger
schema + seed (including the dog/wolf test event) exist, the hunting dog's
interaction set includes `protect` (the verb that triggers the test), and
the strange jade's `observe` interaction is wired to the memory system.
When Java wires WorldHistory to the memory ledger, the test fires.

## AUTO-CANON-056 (this cycle) deliverables

This cycle **recovered and closed out** an interrupted 056 that left
uncommitted, partially-corrupted work:
- Fixed `favor_ledger_npc_zhou_rui.json` (was missing ALL commas — full rewrite).
- Fixed `interaction_sets_non_npc_entities.json` (single missing comma at line 325).
- Restored `affordance_schema.json` + `affordances_wang_family_village.json`
  from git HEAD (had been truncated to 0 bytes by the interrupted edit).
- Removed the broken `chapter_1_wang_family_village_fixed/` duplicate
  directory (47 comma-corrupted backup files).
- Validated all 48 JSON files parse cleanly.
- Updated this README to reflect 056 status.

Net new content committed this cycle: the 9 `favor_ledger_npc_*.json`
files + `interaction_sets_non_npc_entities.json` (the legitimate 056
deliverables) plus the two fixes above.

## How Java will wire this (when JDK is restored — NOT this cycle)

The existing engines read these schemas as follows:
- **IntentEngine** reads `motivation_state_*.json` each NPC tick, scores
  candidate motivations via `relevance_to_targets`, emits a
  `SOCIAL_INITIATION` IntentNature when conditions are met and priority
  exceeds threshold.
- **DecisionEngine** runs the 8-stage `npc_initiation_consent_pipeline`
  before resolving the intent, then resolves to a concrete action
  (approach, gesture, speak, gift, follow) gated by
  `approach_deconfliction_schema` so NPCs don't mob the player.
- **WorldEventBus** publishes the initiated interaction; tracks economy
  stockpiles per tick, applies seasonal modifiers, schedules trade
  routes, collects tax (modifying `economy_schema` resources).
- **BeliefRegistry** loads `relationship_graph_seeds`, updates on
  interaction/event, applies grievance propagation + decay; checks
  `conflict_schema` escalation thresholds.
- **ConversationStateController** drives the 3-layer UI from
  `conversation_ui_schema` + `interaction_sets_*` (Layer 1 verb wheel,
  Layer 2 context expansion, Layer 3 NPC reasoning gates).
- **WorldHistory** writes to the memory ledger on significant events
  (witnesses, retelling hooks, decay, distortion); tracks structured
  favors in `favor_ledger_npc_*` (callable debts, expiration).
- **KnowledgeEngine** lets witnessing NPCs retell events with distortion
  based on `memory_decay` and `distortion_over_time`.

No new Engine/Subscriber/Bus class is required (Article XXVI). The
social/motivation/conversation/economy/conflict systems are DATA; the
existing engines gain one new IntentNature (`SOCIAL_INITIATION`) and
read these JSONs. That is the extent of the Java change.

## Status

- **Schema definitions:** COMPLETE (data-only, JDK-blocked from runtime).
- **Populated examples:** COMPLETE — 9/9 NPC motivation states, 9/9 NPC
  favor ledgers, 31 relationship seeds, 9 NPC interaction sets, 6
  non-NPC entity interaction sets, economy + conflict instances.
- **Java wiring:** BLOCKED (no JDK 17 in sandbox). Tracked as the next
  phase — this is the ONLY remaining work for Chapter 1.
- **Living Chapter 1 completion:** 0/10 Gold-Standard dimensions pass at
  runtime; 10/10 SCHEMA-READY. Flips from SCHEMA-COMPLETE to LIVING when
  Java wiring lands.
- **File count:** 48 JSON + 1 README = 49 files.
- **Living Chapter 2+:** blocked by Article XXIX.
