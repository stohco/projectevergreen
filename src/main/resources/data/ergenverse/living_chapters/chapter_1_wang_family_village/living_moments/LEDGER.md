# THE CANON EXPERIENCES LEDGER

> **Article XLI §1 — Canon Experiences, Not Named Moments** (renamed from Living Moments Ledger per XLI)
> **Article XL §4 — The Ledger sets the bar for whether a cycle has advanced the simulation.**

The unit of progress is **Canon Experiences** — single observable scenes a player would remember years later. Each experience is defined by **what happens**, not who does it. (Article XLI §1.)

Per Article XLI §1, before a Canon Experience can be marked IMPL, the validator checks: **does a suitable actor exist in the world?** If not, the experience is IMPOSSIBLE, not pending. This validates reality instead of patching reality.

---

## How a Canon Experience earns status

| Status | Earned when |
|---|---|
| **SPEC** | The moment is described generically: scene, validation criteria, systems exercised, canon reference. Not yet implemented. |
| **IMPOSSIBLE** | No suitable actor exists in the world to fulfill the experience. Architecture gap or data gap. |
| **IMPL** | The Java/data wiring exists that should produce the moment. Not yet observed. |
| **OBSERVED** | A real observer (developer or agent-browser) has witnessed the moment occur in a running world. |
| **SURVIVED PLAYTEST** | The moment occurs in a fresh world without developer intervention, across at least two playtests. |

A moment at SPEC is a hypothesis. IMPOSSIBLE means the world lacks the foundation. IMPL is an untested hypothesis. OBSERVED is a feature. SURVIVED PLAYTEST is canon.

---

## The Five Pre-Wiring Questions (Article XLI §5)

Before any Canon Experience is wired in Java, these five questions must be answered in order:

1. **How do canonical people become simulated actors?**
2. **How do actors receive activities?**
3. **How are activities interrupted?**
4. **How do they resume afterward?**
5. **How does a canonical experience emerge from those systems?**

### Current answers (AUTO-CANON-069):

| Question | Status | Answer |
|---|---|---|
| Q1: How do canonical people become simulated actors? | ANSWERED (gap fixed this cycle) | Canon data (npcs/*.json) -> NpcSpawnRegistry (location mapping) -> ReificationScan (proximity trigger) -> EntityCultivator (Minecraft entity) -> ActorEntityLink (simulation bridge) -> Actor (simulation state). Wang Family Village was missing from NpcSpawnRegistry; 6 Wang family NPCs now registered. Data gap: npc_wang_tiangui has no JSON file. |
| Q2: How do actors receive activities? | SCHEMA PROVIDED | simulation_state_hierarchy.schema.json defines: motivations -> objective -> activity assignment. The DecisionEngine produces an objective; a resolver assigns a concrete activity (with lifecycle). See activity_lifecycle.schema.json. |
| Q3: How are activities interrupted? | SCHEMA PROVIDED | activity_lifecycle.schema.json: each activity declares interruption_conditions with trigger, evaluation (personality-trait-based), and response. When a WorldEventBus event matches, the activity transitions to INTERRUPTED. |
| Q4: How do they resume afterward? | SCHEMA PROVIDED | The resume_plan: when the reaction completes, the actor returns to the activity at the progress recorded in resumption_state. If resume_after is false, the activity is ABANDONED. |
| Q5: How does a canonical experience emerge? | SCHEMA PROVIDED | Actor exists (Q1), has activity (Q2), gets interrupted by a world event (Q3), reacts (Q4), and the player witnesses the reaction. The player's memory of that reaction IS the Canon Experience. |

---

## The Ledger

| # | Experience (generic) | Status | Validation Criteria | Systems | Canon Ref |
|---|---|---|---|---|---|
| 1 | A cautious cultivator abandons cultivation after noticing predator behavior | SPEC | Actor with caution > 0.7 exists, is in a stationary activity, within perception range of predator event | wolf_pack_momentum + activity_lifecycle (interruption) + simulation_state_hierarchy + perception | RI Ch 1-5 (Wang Lin observes before acting) |
| 2 | A recruiter arrives at a remote village; villagers react; the rumor was already distorted | SPEC | NPC with faction=heng_yue_sect + motivation=recruit exists; village location is registered | caravan_momentum + rumor_momentum (distortion) + NPC schedule | RI Ch 1-3 (recruitment circuit) |
| 3 | A cultivator approaches the player unbidden, demonstrates a technique silently, walks away | SPEC | Actor with personality.caution > 0.7 + personality.curiosity < 0.3 + capability in a demonstrable technique exists | activity_lifecycle (self-interruption) + bidirectional protocol + perception | RI Ch 6-10 (Wang Lin's restriction study) |
| 4 | Two cultivators arrive at the same herb before the player | SPEC | At least 2 NPCs with motivation containing gather_knowledge or alchemy exist; herb location registered in travel_graph | herb_momentum + rumor_momentum + travel_graph | RI Ch 4-7 (herb competition) |
| 5 | Market day: stalls empty; merchant refuses a bridge; village head watches | SPEC | NPC with motivation=economy exists; river_erosion_momentum below bridge_safe threshold | river_erosion_momentum + economy + NPC schedule | RI Ch 2-3 (village economic decline) |
| 6 | Burnt wagon on a road; innkeeper warns; smoke in the hills | SPEC | NPC with motivation=survive exists; bandit_momentum above raid threshold | bandit_momentum + travel_graph threat + NPC ambient | RI Ch 3-5 (bandit presence) |
| 7 | A merchant's caravan passes a turnoff without stopping; elder spits; child is hushed | SPEC | NPCs with competing motivations (trade vs. pride) exist; faction_grudge_momentum active | faction_grudge_momentum + caravan pathing + NPC ambient | RI (Wang-Teng feud) |
| 8 | A patriarch's seclusion: disciple turns visitors away; rare herbs bought in bulk; faint pressure at night | SPEC | NPC with motivation=breakthrough exists; elder_breakthrough_momentum active; spirit_graph qi > threshold | elder_breakthrough_momentum + spirit_graph + market | RI Ch 1-2 (patriarch presence) |
| 9 | Villagers disagree at the well about when a recruiter arrives and which sect | SPEC | Multiple NPCs with information about the same event; rumor_momentum active | rumor_momentum (distortion) + NPC ambient | RI Ch 1 (village information ecology) |
| 10 | A recruiter presses too hard; a wandering cultivator mentions trouble in a sect | SPEC | NPC with motivation=recruit + personality.aggression > threshold; wandering NPC with information | sect_momentum + motivation_state + NPC ambient | RI Ch 3-5 (sect decline) |
| 11 | A villager's dog dies in a predator raid; weeks later they say "used to have one" | SPEC | NPC with a pet/familiar relationship exists; predator_momentum crosses raid threshold; memory_ledger records the event | wolf_pack_momentum + memory_ledger + relationship_graph (familiarity decay) + rumor_momentum | RI Ch 1-2 (Memory Metric canonical test) |
| 12 | A spirit-touched creature at the treeline; predators stalking it; a cultivator watching from above | SPEC | Predator_momentum + spirit_graph both active; NPC with caution > 0.5 + activity observation | wolf_pack_momentum + spirit_graph + activity_lifecycle (observation interruption) | RI Ch 2-4 (observation habit) |
| 13 | A child retells an old story months later with the details wrong | SPEC | NPC child + original event recorded in memory_ledger with witnesses; time > 2 MC months | rumor_momentum (long-term distortion) + memory_ledger (decay) | RI Ch 1-2 (Memory Metric long-tail) |
| 14 | A cultivator emerges from seclusion irritable, asks about a resource, gets nothing | SPEC | NPC with motivation=breakthrough completed + resource scarcity in economy | elder_breakthrough_momentum + motivation_state + herbalist NPC | RI Ch 6-8 (resource scarcity) |
| 15 | A village head organizes a hunt after repeated losses; invites or excludes the player based on relationship | SPEC | NPC with motivation=protect_village exists; predator losses exceed threshold; relationship_graph has player dimensions | wolf_pack_momentum + motivation_state + relationship_graph (trust/familiarity) | RI Ch 2-4 (collective action) |
| 16 | A wandering cultivator passes through, mentions trouble, leaves, never returns | SPEC | NPC with motivation=wander exists; travel_graph allows transit; information to share | sect_momentum + travel_graph + rumor_momentum (seed) | RI Ch 3-5 (foreshadow) |
| 17 | A bridge collapses under a caravan; cargo lost; village head ashen | SPEC | river_erosion_momentum crosses bridge_collapse threshold; NPC with motivation=trade is present | river_erosion_momentum + economy + WorldEventBus cascade | RI Ch 2-3 (momentum consequences) |
| 18 | A merchant returns, demands double price; village head refuses; grain stays on the wagon | SPEC | NPCs with competing motivations; faction_grudge_momentum active; negotiation system | faction_grudge_momentum + economy + relationship_dimensions | RI (feud escalation) |
| 19 | A family member quietly asks the player (only if familiarity > threshold) to check on someone | SPEC | NPC with motivation=protect_family + relationship.familiarity > 0.5 toward player exists | relationship_dimensions (familiarity gate) + bidirectional protocol | RI Ch 6-8 (family concern) |
| 20 | First snow: predators raid; village doubles the watch; a recruiter postpones | SPEC | Season system triggers winter; bandit_momentum + predator_momentum both active | bandit_momentum + season + caravan_momentum + village schedule | RI Ch 3-5 (winter pressure) |

---

## Current count

- **Canon Experiences declared: 20**
- **Canon Experiences at OBSERVED: 0**
- **Canon Experiences at SURVIVED PLAYTEST: 0**
- **Five Pre-Wiring Questions: 5/5 SCHEMA PROVIDED, 1/5 PIPELINE VERIFIED (Q1)**

Per Article XLI §5, the next milestone is answering all five pre-wiring questions with running code, then observing Canon Experience #1.

---

## The honesty bar

A cycle that produces 20 Canon Experience SPECs and 0 OBSERVED has not advanced the simulation. It has produced 20 hypotheses. The cycle succeeds ONLY when one of those SPECs becomes OBSERVED in a running world.

Until the five pre-wiring questions are answered with running code and Canon Experience #1 is OBSERVED, every new schema, compiler, DSL, or validator is architecture before experience (Article XL §7) and must justify itself as the smallest believable path to that experience.

---

## The cascade

Once Canon Experience #1 is OBSERVED, the architecture it exercised (activity_lifecycle interruption + simulation_state_hierarchy + predator momentum + perception) is **proven**. That architecture generalizes to every quiet, character-driven scene: observation, herb competition, resource scarcity, collective action.

Once Canon Experience #11 is OBSERVED, the Memory Metric is proven. That architecture generalizes to every long-tail retelling and familiarity-driven interaction.

The cascade is: prove one, generalize to many. Not: build many, hope one works.