# THE LIVING MOMENTS LEDGER

> **Article XL §4 — The Living Moments Ledger sets the bar for whether a cycle has advanced the simulation.**

The unit of progress on this project is **not** files added, schemas authored, compilers built, or validators passing. The unit of progress is **Living Moments Created** — single observable scenes a player would remember years later.

A Living Moment is a scene in which the world demonstrates that it has life independent of the player. The canonical reference (Article XL §3):

> The player sees Wang Lin abandon his cultivation session because a distant spiritual disturbance exceeded his personal risk threshold — he stands, walks to a ridge, watches wolves stalking a spirit deer, turns, leaves. Nothing was scripted. Nothing involved the player. Yet the player learns: Wang Lin has priorities. Animals hunt. NPCs observe. The world has activity. Interesting things happen without quests.

---

## How a Living Moment earns status

| Status | Earned when |
|---|---|
| **SPEC** | The moment is described concretely: scene, systems exercised, canon reference, perceivable cues. Not yet implemented. |
| **IMPL** | The Java/data wiring exists that should produce the moment. Not yet observed. |
| **OBSERVED** | A real observer (developer or agent-browser) has witnessed the moment occur in a running world. |
| **SURVIVED PLAYTEST** | The moment occurs in a fresh world without developer intervention, across at least two playtests. |

A moment at SPEC is a hypothesis. A moment at IMPL is an untested hypothesis. A moment at OBSERVED is a feature. A moment at SURVIVED PLAYTEST is canon.

---

## The Ledger

| # | Title | Status | Systems Exercised | Canon Ref |
|---|---|---|---|---|
| 1 | Wang Lin abandons cultivation to watch wolves stalk a spirit deer | SPEC | wolf_pack_momentum + Wang Lin AI + perception | RI Ch 1-5 (Wang Lin observes before acting) |
| 2 | Heng Yue recruiter arrives; village reacts; rumor was already distorted | SPEC | caravan_momentum + rumor_momentum + NPC schedule | RI Ch 1-3 (recruitment circuit) |
| 3 | Wang Lin approaches unbidden: "Watch." Demonstrates one restriction. Walks away. | SPEC | elder_breakthrough_momentum (Wang Lin) + player-action perception + bidirectional protocol | RI Ch 6-10 (Wang Lin's restriction study) |
| 4 | Two cultivators arrive at the same herb before the player | SPEC | herb_momentum + rumor_momentum (spread to neighbor village) + travel_graph | RI Ch 4-7 (herb competition) |
| 5 | Market day: half the stalls empty; caravan master refuses the bridge; village head watches | SPEC | river_erosion_momentum + economy + NPC schedule | RI Ch 2-3 (village economic decline) |
| 6 | Burnt wagon on the south road; innkeeper's warning; smoke in the hills | SPEC | bandit_momentum + travel_graph threat + NPC ambient | RI Ch 3-5 (bandit presence) |
| 7 | Teng merchant's caravan passes the turnoff without stopping; elder spits; child is hushed | SPEC | faction_grudge_momentum + caravan pathing + NPC ambient | RI (Wang-Teng feud) |
| 8 | Patriarch's seclusion: disciple turns visitors away; rare herbs bought in bulk; faint pressure at night | SPEC | elder_breakthrough_momentum (patriarch) + spirit_graph coupling + market | RI Ch 1-2 (patriarch presence) |
| 9 | Villagers disagree at the well about when the recruiter arrives and which sect | SPEC | rumor_momentum (distortion + disagreement) + NPC ambient | RI Ch 1 (village information ecology) |
| 10 | Recruiter presses Wang Lin too hard; wandering cultivator mentions trouble in Heng Yue | SPEC | sect_momentum + recruiter motivation_state + NPC ambient | RI Ch 3-5 (sect decline) |
| 11 | Old Chen's dog dies in a wolf raid; weeks later Old Chen says "used to have one" | SPEC | wolf_pack_momentum threshold + memory_ledger + relationship_graph + rumor_momentum (distorted retelling) | RI Ch 1-2 (Memory Metric canonical test) |
| 12 | Spirit deer at the treeline — wolves stalking it — Wang Lin watching from the ridge | SPEC | wolf_pack_momentum + spirit_graph (deer as spirit-touched fauna) + Wang Lin AI | RI Ch 2-4 (Wang Lin's observation habit) |
| 13 | Child retells Old Chen's dog story months later — "seven wolves, not five; red eyes" | SPEC | rumor_momentum (long-term distortion) + memory_ledger (decay) + child NPC ambient | RI Ch 1-2 (Memory Metric long-tail) |
| 14 | Wang Lin emerges from the cave irritable, asks the herbalist about crimson root, gets nothing | SPEC | elder_breakthrough_momentum (supplement exhaustion) + Wang Lin motivation_state + herbalist NPC | RI Ch 6-8 (Wang Lin's resource scarcity) |
| 15 | Village head organizes a wolf hunt after the third livestock loss — invites (or excludes) the player based on relationship | SPEC | wolf_pack_momentum (livestock_raid threshold) + village_head motivation_state + relationship_graph (player standing) | RI Ch 2-4 (village collective action) |
| 16 | A wandering cultivator passes through, mentions "trouble in Heng Yue," leaves — never to be seen again | SPEC | sect_momentum + travel_graph (wandering NPC) + rumor_momentum (seed) | RI Ch 3-5 (sect decline foreshadow) |
| 17 | The bridge finally collapses under a caravan — cargo lost, driver furious, village head ashen | SPEC | river_erosion_momentum (bridge_collapse threshold) + economy (trade route abandonment) + WorldEventBus cascade | RI Ch 2-3 (canonical history-vs-momentum example) |
| 18 | The Teng merchant returns, demands double price for grain; village head refuses; grain stays on the wagon | SPEC | faction_grudge_momentum (trade_refusal threshold) + economy + negotiation | RI (Wang-Teng feud escalation) |
| 19 | Wang Lin's parents quietly ask the player (only if relationship earned) to check on him in the cave | SPEC | elder_breakthrough_momentum + relationship_graph (player-to-family) + bidirectional protocol | RI Ch 6-8 (family concern) |
| 20 | First snow: the bandits raid the south road; the village doubles the watch; the recruiter postpones | SPEC | bandit_momentum (raid_blitz) + season system + caravan_momentum (reroute) + village schedule | RI Ch 3-5 (winter pressure) |

---

## Current count

- **Living Moments declared: 20**
- **Living Moments at OBSERVED: 0**
- **Living Moments at SURVIVED PLAYTEST: 0**

Per Article XL §3, the project's next milestone is **Living Moment #1 at OBSERVED**. Everything else is downstream of that.

---

## The honesty bar

A cycle that produces 20 Living Moment SPECs and 0 OBSERVED has not advanced the simulation. It has produced 20 hypotheses. The cycle succeeds ONLY when one of those SPECs becomes OBSERVED in a running world.

Until Living Moment #1 is OBSERVED, every new schema, compiler, DSL, or validator is architecture before experience (Article XL §7) and must justify itself as the smallest believable path to that moment.

---

## The cascade

Once Living Moment #1 is OBSERVED, the architecture it exercised (wolf_pack_momentum tick + Wang Lin AI threshold response + perception + ridge pathing) is **proven**. That architecture can then be generalized to produce Living Moments #2, #11, #12, #15, #20 — all of which share the same threshold-cascade backbone.

Once Living Moment #3 is OBSERVED, the bidirectional Wang Lin protocol is proven. That architecture generalizes to Living Moments #14, #19.

Once Living Moment #11 is OBSERVED, the Memory Metric is proven. That architecture generalizes to Living Moments #13 (long-tail retelling), and to every future Chapter.

The cascade is: prove one, generalize to many. Not: build many, hope one works.

---

## What does NOT count as a Living Moment

- A scripted scene that fires once (Article IV violation)
- A quest with a marker (Article IV violation)
- A notification, toast, or "quest complete" (Article IV + XXXI violation)
- A system that produces no perceivable output (Article XXX violation — "referenced, not experienced")
- A moment that requires the player to exist (Article V violation — the world must move without the player)
- A moment that is identical across worlds (Article XXXIX §4 violation — momentum is compiled, not scripted)
- A moment that no NPC remembers later (Article XXXI.5 violation — Memory Metric)

A scene that fails any of these is not a Living Moment. It is content wearing a Living Moment's clothes.
