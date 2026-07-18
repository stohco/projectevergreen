# Ergenverse Forge Mod — Master Project Document

> **Single source of truth for project state, mechanics, and direction.**
> Last updated: 2026-07-14. Maintained alongside `worklog.md` (chronological log) and `UNFINISHED_RESEARCH.md` (gap tracker).

---

## 1. Project Identity

**Ergenverse Forge** — a Minecraft mod reconstructing the world of Er Gen's *Renegade Immortal* (仙逆) as a playable, completionist xianxia sandbox.

- **Engine**: Minecraft 1.20.1 / Forge 47.4.0 / Java 17 (NON-NEGOTIABLE — never 1.21/Forge 65)
- **Location**: `/home/z/my-project/forge-mod/`
- **Next.js app**: RETIRED. Never touch `src/app/` or `src/components/`.
- **Canon source of truth**: `ri_canon_database.json` (632 entries) + 23 canon/design `.md` files (26,487 lines)

---

## 2. Hard Numbers (verified 2026-07-14)

| Metric | Count |
|---|---|
| Java files | 353 |
| Arsenal items (Wang Lin) | 309 |
| Total item lang keys | 372 |
| Block lang keys | 28 |
| Total lang keys | 1,561 |
| Textures (PNG) | 496 (zero placeholders) |
| Data JSONs | 3,033 |
| Canon DB entries | 632 (160 chars + 80 locs + 178 artifacts + 214 techniques) |
| Workstations (Block+BE+Menu+Screen) | 9 |
| Commands | 9+ |
| Network packets | 28 (IDs 0-27) |
| Dimensions | 4 (Planet Suzaku + 3 pocket dims) |
| Biomes | 19 (14 Suzaku + 5 pocket) |
| Species definitions | 94 (39 full + 55 stubs) |
| NPC JSONs | 152 |
| Structure JSONs | 262 |
| Structure sets | 146 |
| Template pools | ~30 |
| Build status | ✅ GREEN (`compileJava` in 26s) |

---

## 3. Completed Phases (P1-P13 + Cross-Cutting)

| Phase | System | Packet IDs | Status |
|---|---|---|---|
| P1 | Active Flight (Attractive Force Technique → mastery → flight) | 8 | ✅ |
| P2 | Alchemy (Pill Furnace, 11 recipes, 6-tier fire) | 9, 10 | ✅ |
| P3 | Artifact Forge (7 recipes, 6 modes) | shared 10 | ✅ |
| P4 | Formations (Formation Platform) | — | ✅ |
| P5 | Talisman + Jade Slip | — | ✅ |
| P6 | Restrictions (99,999 flag, 9-layer×11,111, realm-scaled) | — | ✅ |
| P7 | Storage (space ring) | — | ✅ |
| P8 | Cave (Cave World ownership) | — | ✅ |
| P9 | Beasts (Beast Pact Altar) | — | ✅ |
| P10 | Soul Refining (Soul Refining Cauldron, 6 soul types) | 20, 21 | ✅ |
| P11 | Body Refining (Refining Pool, HP drain, bloodRefinement stat) | 22, 23 | ✅ |
| P12 | Puppet Refining (Puppet Platform, 4 tiers, karma) | 24, 25 | ✅ |
| P13 | Dao Forging (14 Essences, 5 Domains, 7 Spells, 6 Swords) | — | ✅ |
| Cross | Unified Discovery UI (`/ergen discover`) | — | ✅ |

**Supporting systems (all complete):**
- Cultivation system (CultivationState 728 lines, Capability, Events 787 lines, breakthrough + tribulation + 14-Essence tracking)
- Perception system v4 (7 tiers, canon NPC data loading, V-key divine sense pulse)
- Manifestation Gift system (5 protagonist profiles, 18 gift records)
- Emergent History (PlayerHistory, WorldHistory, NpcMemory, RelationshipHistory — 5 wiring points)
- Advanced Mechanics (Joss Flame economy, Cave World ownership, Realm-Sealing Grand Array, Samsara Incarnation)
- Canon DB runtime (RICanonicalDatabase.java — 8,088 lines, 30+ query methods)
- Edge-of-Canon state (RIEdgeOfCanonState.java — 1,202 lines, 9 threat categories, 13 manifestation comments)
- Three-layer architecture (Canon / Simulation / Emergent History) with Provenance on every fact

---

## 4. Wang Lin Manifestation Mechanics (CORRECTED 2026-07-14)

**Critical corrections from user:**
- ❌ Wang Lin is NOT waste-grade aptitude. He starts as a **mortal** (manifestation/clone) but retains **all his knowledge and techniques**.
- ❌ His true body is NOT absent. It is **frozen in time** — waiting for the player to reach it. The true body advances no further until the player arrives.
- ✅ The manifestation grows in parallel with the player, doing his own thing.

### 4.1 Player Protagonist Power — Replica Arsenal

The player's protagonist power: **with sufficient affinity/bond, the player can use EVERY item/technique Wang Lin has ever used** (appropriate to power level), whether the manifestation currently holds it or not.

**Mechanic**: Wang Lin's true body (frozen) can **send exact replicas** of any item he's ever owned to the player — for free. This is the player's protagonist power, not a loan.

- The **shared arsenal grows over time** (early game: nothing; mid game: low-tier; late game: God-Slaying Sword, Karma Whip, etc.)
- Player can **request** any item Wang Lin has possessed in canon. If bond is high enough AND power level is appropriate, the true body sends a replica.
- This means the **entire 309-entry arsenal manifest is eventually accessible** to the player, gated only by bond + power level.

### 4.2 Technique Transmission

Wang Lin teaches techniques via three methods:
1. **Jade slips** — player receives a jade slip item, meditates on it to learn
2. **Direct knowledge transfer** — Wang Lin transmits directly to player's mind (instant, requires high bond)
3. **Demonstration** — Wang Lin uses the technique in player's presence; player learns by observing (requires perception tier + repetition)

### 4.3 Wang Lin as Ally

- Wang Lin **does his own thing** by default — pursuing his own goals, cultivating, exploring
- He **always knows where the player is** (canon: his divine sense tracks allies)
- If the player asks to adventure together, Wang Lin joins as an **ally**
- His combat AI must be **extremely good** — feels like fighting alongside the real Wang Lin
- His decision-making in general must **feel like Wang Lin** — patient planner, ruthless when needed, protective of allies, cunning

### 4.4 Gifting & Request System (Q8 FINAL — reasoning-based, NOT tier-based)

**Core rule: The player can ALWAYS ask Wang Lin for anything. He will NEVER get mad.** Asking is always safe.

> **⚠ WARNING — Wang Lin is NOT a content dispenser.**
> He is a **mentor, observer, and judge** who occasionally helps. If players sometimes leave
> disappointed but understanding why, you've captured his character correctly. In the novels,
> Wang Lin is often generous, but he is never careless. The temptation to make him an
> endgame reward vendor MUST be resisted. He gives because he sees the player's path —
> not because the player asked.

**Hoarding correction (user-final):** Wang Lin does NOT withhold items as a punishment for hoarding.
If the player hoards resources, Wang Lin STILL looks for what the player **needs most** right now
and gives that. Hoarding behavior changes Wang Lin's *prediction* of the player's path (which
informs *what* is most needed) — but it NEVER causes punitive withholding. A hoarding player
may need a breakthrough catalyst more than a combat treasure; Wang Lin gives the catalyst.
He responds to genuine need, not to moral judgment.

Wang Lin's response is governed by a **reasoning engine**, not fixed rarity tiers:

| Factor | Question Wang Lin asks himself |
|---|---|
| **Necessity** | Does the player actually need this right now? |
| **Safety** | Will giving this item get the player killed? (too much power too fast) |
| **Usefulness** | Can the player actually use this at their current level? |
| **Uniqueness** | Is this item irreplaceable? Does the true body only have one? |
| **Current need** | Does Wang Lin himself need this item right now? |
| **Judgment** | Based on his Expectation Model of the player (see §6.13), is this the right time? |

**Three possible responses to a request:**
1. **Yes** — gives the replica immediately (true body sends it). Happens when necessity + usefulness are high and safety is acceptable.
2. **No (for now)** — "Your affinity with me isn't enough yet. Come back when you've grown." Never angry, just honest. The player learns what they need to improve.
3. **Challenge** — instead of yes/no, Wang Lin offers a crazy challenge: "I'll give you the Karma Whip — but first, survive three days in Mosquito Valley without any cultivation." The challenge is always thematically tied to the item requested. This is the most interesting response — it turns a rejection into gameplay.

**Why this beats tier-based gifting:** A rare item might be given freely if it's necessary and safe. A common item might be withheld if Wang Lin judges the player is becoming reliant. The reasoning is opaque to the player — they have to learn Wang Lin's personality, exactly like dealing with a real person.

### 4.5 Wang Lin AI Requirements

This connects directly to the **advanced NPC AI system** (see §6). Wang Lin is the highest-tier NPC and must use the full cognitive simulation:
- Full goal/need/resource/fear/knowledge/relationship model
- Long-term planning (patient_planner decision style)
- Predictive behavior (anticipates player and enemy moves)
- Realm-appropriate cognition (his mortal manifestation thinks like a mortal; as he cultivates up, his thinking expands)
- Internal monologue driving every decision

---

## 5. Mortal Origin System (IN DESIGN — pending Q8-Q10)

### 5.1 Setting
- **Fixed spawn**: Wang Family Village, Zhao Country, Planet Suzaku
- **Scale**: Canon-faithful small (~30 buildings, ~100 family NPCs all surnamed Wang)
- **Aesthetic**: Refined rural Zhao country architecture (mud-brick walls, tile roofs, well in square, ancestral hall, small market, family homes, fields outside walls)
- **Family fate (edge of canon)**: The Wang family massacre (by Teng Huayuan) happened in the past. The village is rebuilt/haunted — empty homes, memorial shrine, elders who speak of "the lost generation." Wang Lin's manifestation knows the truth and shares it once bond is high enough.

### 5.2 Player & Wang Lin Parallel Start
- Both spawn as **mortal youths** in Wang Family Village on Day 1
- Player is a random mortal (waste-grade aptitude — the underdog arc)
- Wang Lin's manifestation is a mortal too, but with all his knowledge/techniques sealed until he cultivates back up
- Wang Lin finds the Heaven-Defying Bead on Day ~5-7 (canon event, scripted for him)
- After the bead, Wang Lin's cultivation velocity explodes; player's stays slow unless they find their own path

### 5.3 Aptitude System (NOT Spiritual Roots)
**Lore correction**: Er Gen's RI uses **Aptitude/Talent (资质 — zīzhì)**, NOT Spiritual Roots (灵根 — that's *A Record of a Mortal's Journey*). Wang Lin's defining trait is **waste-grade aptitude**. The Heaven-Defying Bead's true power: **it can absorb others' aptitude fragments to improve your own** — this is the engine of his arc.

- All players **always qualify** (minimum waste-grade) — no game-over failure path
- Aptitude can be improved via: Heaven-Defying Bead (player's path), rare spirit herbs, canon-attested methods
- Higher aptitude = faster cultivation velocity

### 5.4 Mortal Phase & The Opportunity Engine (Q9 FINAL — exploration-driven, NOT timer-driven)

**THE KEY INSIGHT (user's core correction):** Er Gen's novels are about **Opportunity**, NOT **Player Progression**. These are not the same thing.

- **Progression-thinking** (wrong): "Player needs X hours of grinding, so we give them a reward every Y minutes."
- **Opportunity-thinking** (right): "The world constantly generates opportunities. The player finds them by being curious, observant, and bold. Timers are background simulation constraints, never primary triggers."

**The Opportunity Engine** constantly asks:
1. What opportunities currently exist in the world?
2. Who knows about them?
3. Who is competing for them?
4. How long before they disappear?
5. What happens if they're ignored?

**Worked example — a spirit fruit ripens:**
The world does NOT spawn a "quest." It simulates consequences:
- Nearby beasts smell it → begin migrating toward it
- Cultivators detect spiritual fluctuations → sect scouts dispatched
- Birds begin gathering in the canopy above
- Insects infest the fruit if unchecked
- The nearby spirit vein becomes unstable from the fruit's energy draw
- If no one claims it in X hours, it falls and rots, OR a beast eats it and advances, OR a sect harvests it

The player discovers this by **exploring curiously** — following the unusual bird migration, noticing the spiritual fluctuation, tracking the beast tracks. A curious player might find it in 10 minutes. Another player might wander for an hour and never notice.

**Exploration-driven shortcut triggers (NOT timers):**
1. **Following an unusual bird** → leads to a spirit nest or hidden glade
2. **Climbing an unnamed peak** → reveals vista, maybe a hermit's cave
3. **Exploring a hidden canyon** → ancient inscription or forgotten corpse with manual
4. **Camping overnight** → witness a spirit beast migration or cultivator flying overhead
5. **Investigating strange spiritual fluctuations** → spirit fruit ripening, formation activating, treasure birthing
6. **Saving a wandering cultivator** → they teach a basic technique as thanks
7. **Watching two beasts fight** → inspiration (small aptitude boost), and the winner is weakened enough to harvest
8. **Reading forgotten inscriptions** → fragment of a lost technique
9. **Befriending Wang Lin early** → he shares discoveries, teaches
10. **Finding hidden manuals** → in abandoned houses, caves, ancestral hall secret compartment

**The base path still exists** (~1 hour: village life → Heng Yue recruiter Day 3 → aptitude test → outer disciple → Qi Condensation 1 → trial → Heaven-Defying Bead). But the player can deviate at any point by being curious. The world doesn't care about the clock — it cares about attention.

### 5.4.1 Opportunity Lifecycle (Finite-State Machine)

**Every opportunity is a finite-state machine.** Nothing should instantly appear. Everything should *become*.

| State | Meaning | Transition triggers |
|---|---|---|
| **Dormant** | The opportunity exists in potential but has not yet become perceptible. A spirit fruit is growing but unripe; a vein is stable but weakening. | Time + world simulation (fruit ripens, vein destabilizes) → Forming |
| **Forming** | The opportunity is now perceptible to the right observer. Spiritual fluctuations begin. Beasts nearby grow restless. Mortals hear strange sounds. | First competitor arrives (beast, cultivator, scout) → Contested |
| **Contested** | Multiple actors are aware and competing. Beasts fighting cultivators; two sects dispatching scouts; a wandering cultivator arrives. | One actor claims it / it is destroyed / time expires → Resolved |
| **Resolved** | The opportunity has concluded. Someone got it. The fruit was eaten. The vein was harvested. A beast advanced. | Recorded in history → Historical |
| **Historical** | The opportunity is part of the world's memory. NPCs reference it: "Three years ago, a spirit fruit ripened in Mosquito Valley — many died for it." Informing rumor propagation and Expectation Model. | (terminal state — persists in world memory) |

**Key design rule:** Players who arrive late find *consequences*, not the opportunity itself. They find a beast's corpse (winner ate the fruit), a battle scar (two sects fought), a wandering cultivator's journal. The world records what happened — even if the player never saw it.

### 5.4.2 Worked Example — Spirit Fruit Timeline (canon-faithful emergent story)

```
Spirit Fruit ripens           t = 0h
+2h  — Spirit insects gather
+4h  — Local deer herd changes route (avoiding the glade)
+6h  — Wolf spirit beast notices prey concentration
+8h  — Wandering Qi Condensation cultivator senses fluctuation
+12h — Two factions dispatch scouts
+16h — Conflict becomes possible
```

At NO point did the engine create a quest. It simply advanced world state. A curious player who notices the deer route change at +4h might find the glade at +6h (early, peaceful observation). A late player arriving at +18h finds corpses, scent of blood, and a powerful advanced wolf beast — completely different story, both emergent.

### 5.5 Journal + Divine Sense Atlas (Q10 FINAL — living knowledge map)

**Journal item** (in inventory from start) — updates with observations, lore notes, character notes. "Wang Lin's Travels" style. Records what the player has actually experienced.

**Divine Sense Atlas** (M-key) — NOT a map that reveals everything. It contains ONLY knowledge the player has genuinely acquired. The atlas **evolves with cultivation** and is structured as **4 layered knowledge planes**. The same coordinate can contain all four layers simultaneously; the player's realm determines which layers are rendered:

**Layer 1 — Physical (Mortal+):**
- Terrain, roads, rivers, villages, mountains
- NPC settlements, named locations
- Everything a mortal can perceive with ordinary senses

**Layer 2 — Qi (Foundation+):**
- Spirit veins, qi density gradients, herb concentrations
- Cultivation hotspots, qi flow patterns
- What a Foundation cultivator senses as ambient energy

**Layer 3 — Restriction (Nascent Soul+):**
- Formations, seals, hidden arrays, spatial anchors
- Restriction boundaries, forbidden zones, ancient barriers
- What Wang Lin perceives when he scans an area with divine sense

**Layer 4 — Law (Ascendant+):**
- Karmic nodes, space cracks, law distortions, ancient battle scars
- Void paths, dimensional weak points, civilization-scale patterns
- The deepest reality — visible only to those who comprehend Dao

| Realm | Layers visible |
|---|---|
| **Mortal** | Physical only |
| **Qi Condensation** | Physical + faint Qi hints |
| **Foundation** | Physical + full Qi |
| **Core Formation** | Physical + Qi + faint Restriction glimmers |
| **Nascent Soul** | Physical + Qi + Restriction |
| **Soul Formation** | Physical + Qi + Restriction + faint Law echoes |
| **Ascendant** | All 4 layers |
| **Third Step** | All 4 layers + karmic threads, civilization patterns |

The map is a **perception record**, not a satellite view. A Nascent Soul cultivator's atlas looks fundamentally different from a mortal's — not because the mortal's map is "fog of war" but because the Nascent Soul cultivator *perceives things the mortal literally cannot*.

**Both UIs coexist**: physical map item (right-click, immersive, can be shared/traded) + M-key Divine Sense Atlas (always accessible, scales with cultivation).

### 5.6 Design Questions — ALL RESOLVED
- **Q8** ✅ Reasoning-based gifting (necessity/safety/usefulness/uniqueness/current need/judgment). Player can always ask, Wang Lin never mad, can say no or offer a challenge.
- **Q9** ✅ Exploration-driven opportunities, NOT timer-driven. Opportunity Engine simulates consequences, not quests.
- **Q10** ✅ Divine Sense Atlas — living knowledge map that evolves with cultivation realm.

---

## 6. NPC AI — Cognitive Simulation Architecture

### 6.1 Design Philosophy
**The user's vision (confirmed match with existing design):** NPCs are NOT dialogue trees. They are living beings with minds. The pipeline is:

```
World State
      ↓
Perception (limited by cultivation and senses)
      ↓
Knowledge & Memory (what this NPC actually knows)
      ↓
Goals & Needs (what it is trying to achieve)
      ↓
Prediction (what it expects will happen)
      ↓
Decision (choose an action)
      ↓
Action (move, cultivate, attack, trade, flee...)
      ↓
Dialogue (if speaking is part of the chosen action) — LAST, not first
```

**The player is NOT the center of the world.** NPCs already had something they were trying to accomplish. The player interrupts them.

### 6.2 NPC Mind Model (every NPC has)

| Component | Description |
|---|---|
| Identity | Who they are (name, faction, role, history) |
| Goals | What they're trying to accomplish (priority queue) |
| Knowledge | What they actually know (INCOMPLETE — no omniscience) |
| Beliefs | What they think is true (may be wrong) |
| Needs | Physical/spiritual requirements (food, qi, rest, resources) |
| Relationships | Multi-dimensional (trust, respect, fear, gratitude, suspicion, debt, love, hatred, karmic connection) — NOT a single number |
| Resources | What they possess and can deploy |
| Cultivation | Realm + techniques + treasures |
| Fears | What they avoid |
| Personality | Tendencies (risk tolerance, patience, mercy, pride, curiosity, ambition, paranoia, compassion, emotional control) — NOT adjectives |
| Schedule | Daily/seasonal routine |
| Current Task | What they're doing RIGHT NOW |
| Mood | Computed from world simulation (mother died, sect losing war, hungry, recently broke through) — NOT a state enum |
| Recent Events | Short-term memory (decays) |
| Long-term Memory | Permanent (killed my son, saved my life, stole my treasure, joined sect, married disciple) |

### 6.3 Priority Queue (intelligence from priorities)

Every NPC has a priority queue like:
1. Repair Soul Banner
2. Defend Sect
3. Train
4. Eat
5. Sleep
6. Talk
7. Wander

The player only **changes priorities** — doesn't freeze the NPC.

### 6.4 Knowledge Spreading

Information must **physically spread**. No omniscient NPCs.
- Merchant knows: nearby towns, customers, prices, bandits
- Merchant does NOT know: Ancient God, Seven-Colored Daoist, player's inventory, future
- Sect elder knows: formation damage, disciples, treasury, sect enemies
- Sect elder does NOT know: what happened 500 km away yesterday

### 6.5 Memory Decay

| Tier | Content | Duration |
|---|---|---|
| Long-term | Killed my son, saved my life, stole my treasure, joined sect, married disciple | Forever |
| Medium | Talked yesterday, borrowed sword, lost duel | Weeks |
| Short | Passed by, asked directions, bought pills | Hours-days |
| Forgotten | — | Decayed |

### 6.6 Realm-Scaled Cognition (xianxia-unique)

Cultivators think differently by realm. No xianxia game has captured this.

| Realm | Planning Horizon | Concern |
|---|---|---|
| Mortal | Today | "I need food." |
| Qi Condensation | Months | "I need pills." |
| Foundation | Years | "I need territory." |
| Core Formation | Decades | "I need resources." |
| Nascent Soul | Centuries | "I need allies." |
| Soul Formation | Millennia | "I need karmic opportunities." |
| Ascendant | Tens of thousands of years | "I need to preserve this lineage." |
| Third Step | Hundreds of thousands of years | "I must preserve this civilization because in 80,000 years it becomes important." |

AI fundamentally changes as NPCs advance.

### 6.7 Dialogue Generation

Don't store `"Hello traveler."` Store the **thought**:
```
Thought:
  Player appears harmless.
  Need to maintain reputation.
  Busy.
  Neutral.
```
Generator produces `"Speak quickly."` Another time: `"I don't have much time."` Same thought, different wording.

### 6.8 Anticipatory Behavior (the killer feature)

NPCs don't **react** to the player. They **predict** the player.
- Veteran elder: "This young man has asked about the Ancient God inheritance three times. He's pretending to browse the library, but he's searching for forbidden records. I'll quietly assign two disciples to observe him."
- Spirit beast: retreats from a valley before the player arrives because it sensed a dangerous aura hours earlier.

### 6.9 Internal Monologue Simulation

Every important NPC has an internal monologue updating every few seconds (not shown to player):
```
Current Objective: Repair formation.
Problem: Need spirit stones.
Concern: Outer disciple watching me.
Opportunity: Merchant arriving tomorrow.
Danger: Formation instability rising.
```
Every decision comes from this.

### 6.10 World Runs Without Player

If the player goes AFK, NPCs continue: cultivating, trading, arguing, hunting, migrating, building, stealing, researching formations, warring. The player returns to a changed world.

### 6.11 Rumor System (social network propagation — the missing glue)

NPCs don't have perfect information. They hear **rumors**, and rumors **propagate like a real social network**:

| Actor class | What they hear | How fast |
|---|---|---|
| Nearby mortals | Strange sounds, unusual weather, beast migrations | Hours (overheard, witnessed) |
| Merchants | Increased beast activity, closed roads, missing caravans | Days (travel-based) |
| Sect scouts | Spiritual fluctuations, treasure births, foreign cultivators | Hours-days (patrol routes) |
| Sect elders | Incomplete scout reports, faction intelligence, karmic ripples | Days-weeks (filtered, distorted) |
| Wandering cultivators | Hearsay at inns, fragmentary divinations | Days (chance-based) |
| Players | Distorted versions of all of the above — never the full picture | Variable |

**Distortion model:** Each rumor hop adds noise. A simple fact ("A spirit fruit ripened in Mosquito Valley") becomes:
- 1 hop: "Strange lights in Mosquito Valley."
- 2 hops: "A treasure was born in the eastern canyons."
- 3 hops: "An immortal inheritance has appeared somewhere near Zhao."
- 4 hops: "Wang Lin has returned." (totally fabricated but believed)

NPCs act on the rumor *as they heard it*, not as it actually is. A sect elder who hears the 3-hop version dispatches scouts to the wrong canyon. A wandering cultivator who hears the 4-hop version spreads panic.

**This is the missing glue** between the Opportunity Engine (world simulation) and the Expectation Model (NPC prediction). Opportunities generate signals → rumors carry those signals (with distortion) → NPCs form predictions based on the distorted signals → NPCs act → consequences feed back into the world.

Players hear distorted versions and must investigate to learn the truth. This makes the world feel enormous and alive.

### 6.12 Performance Tiering

- **Ordinary NPCs**: deterministic, lightweight (villagers, generic disciples)
- **Major NPCs**: full cognitive simulation (sect leaders, Wang Lin, legendary beasts, protagonist clones)

Reserve expensive planning for high-impact entities. Gives a living world without requiring every villager to run an expensive planner every tick.

### 6.13 Expectation Model (the user's critical addition)

Every important NPC doesn't just react — they **predict** the player and act on predictions.

**Wang Lin's Expectation Model examples:**

*Scenario 1:* Player has been asking about restrictions + spends hours studying formations + avoids unnecessary killing.
- **Prediction**: Likely pursuing Restriction Dao.
- **Recommendation**: Leave a restriction jade slip where they might find it. Don't hand it to them — let them discover it.

*Scenario 2:* Player keeps challenging stronger opponents.
- **Prediction**: Will die soon.
- **Intervention**: Give a subtle warning. Do NOT interfere directly (would rob them of growth).

*Scenario 3 (REVISED per user-final hoarding correction):* Player hoards resources, never helps villagers.
- **Prediction**: Self-reliant path, may face resource bottlenecks at breakthrough thresholds.
- **Intervention**: STILL identifies what the player needs most right now and gives that. Hoarding is not punished — Wang Lin responds to genuine need regardless of moral judgment. A hoarding player may need a breakthrough catalyst more than a combat treasure; Wang Lin gives the catalyst. The Expectation Model *informs what is needed*, never *withholds as punishment*.

This is **much more interesting than scripted affinity rewards**. Wang Lin acts as a mentor who reads the player's behavior and responds with judgment, not a vending machine that dispenses items at affinity thresholds.

### 6.13.1 Prediction Confidence (predictions are uncertain)

Predictions are NOT certainties. Every prediction carries a **confidence percentage**. Wang Lin's internal monologue might read:

```
Pursuing Restriction Dao                     82%
Will challenge a stronger cultivator soon    61%
Can survive Mosquito Valley                   34%
Likely to break through within 30 days       47%
```

**Why this matters:** NPCs can now make **mistakes**. That makes them feel alive.
- Wang Lin might predict (82%) the player is pursuing Restriction Dao and leave a jade slip — but the player was actually researching formations to find a specific seal. The jade slip is a wasted gesture; Wang Lin updates his model.
- Wang Lin might predict (34%) the player can survive Mosquito Valley and offer it as a challenge — but the player dies. Wang Lin's prediction was wrong. He grieves (in his own way) and recalibrates.
- A sect elder might predict (90%) the player is a spy and assign watchers — but the player was just curious. The watchers waste time; the elder updates.

**Confidence decays with time** and updates with new observations. Old predictions fade; new evidence shifts the percentages. This makes the Expectation Model feel like a living, learning mind rather than a static rules table.

### 6.14 Existing Implementation

- `CANON_RI_CHARACTER_DECISIONS.md` (1,952 lines) — 32 NPC profiles with full goal-based planner data
- `RICanonicalDatabase.java` (8,088 lines) — 632 canon entries queryable
- `RIEdgeOfCanonState.java` (1,202 lines) — 9 threat categories, 13 manifestation comments
- `EntityCultivator.java` — polymorphic NPC shell driven by canon JSON data
- `HistoryEvents.java` — Forge event listener for entity interactions (5 wiring points)
- `WangLinAntagonists.java` — 6 antagonists with 3-layer threat model

**Status**: Data layer complete. Cognitive simulation engine (priority queue, memory decay, prediction, internal monologue) is the **next major build** after mortal origin system.

---

## 7. JSON Ecosystem (what each JSON is for)

| JSON set | Count | Purpose |
|---|---|---|
| `ri_canon_database.json` | 632 | Source of truth — 160 chars, 80 locs, 178 artifacts, 214 techniques |
| `wanglin_arsenal_manifest.json` | 309 | Every Wang Lin item/technique/pet/clone/companion → registered as MC items. Player can acquire replicas via protagonist power |
| `npcs/*.json` | 151+ | Each NPC's canon data (faction, realm, perception_tiers, personality, dao_heart, relationship_to_wanglin). Read at spawn by `EntityCultivator` |
| `worldgen/structure/*.json` | 262 | Structure definitions (Heng Yue Sect 23 sub-structures, Wang Family Village, Nan Dou City, etc.) |
| `worldgen/structure_set/*.json` | 146 | Placement rules for structures (biomes, frequency) |
| `worldgen/template_pool/*.json` | ~30 | Building block pools for jigsaw generation |
| `ecosystems/*.json` | ~10 | Ambient creature spawns per biome |
| `species/*.json` | 94 | Species definitions (39 full + 55 stubs — 10 creature stubs need Java entity impl, 45 flora stubs need placed_feature impl) |
| `faction_relationships/*.json` | ~15 | Faction diplomacy (enemies, allies) — drives NPC interactions |

---

## 8. Build Order (next phases)

### Phase A — Mortal Origin System (DESIGN COMPLETE — BUILDING NOW)
1. Wang Family Village worldgen (refine existing 13 template pools, ~30 buildings, canon aesthetics)
2. Mortal spawn logic (force spawn here, lock cultivation capability until unlocked)
3. **Opportunity Engine v1** (see §5.4, §5.4.1, §5.4.2) — world generates opportunities with consequences, not quests. Lifecycle FSM (Dormant→Forming→Contested→Resolved→Historical). Spirit fruit ripening timeline canon-faithful.
4. Wang Lin manifestation NPC with parallel progression + reasoning-based gifting (§4.4) + Expectation Model (§6.13) + Prediction Confidence (§6.13.1) + Rumor System (§6.11)
5. Journal item + Divine Sense Atlas (M-key, evolves with cultivation realm, §5.5)
6. ✅ Aptitude test ceremony event (Day 3, Heng Yue recruiter — AptitudeTestEvent.java)
7. ✅ Exploration-driven shortcut opportunities (10 triggers — ExplorationShortcutSystem.java)
8. Heng Yue Sect travel (existing structures)
9. Heaven-Defying Bead acquisition path (parallel to Wang Lin's)
10. Cultivation unlock + integration with existing P1-P13 systems

### Phase B — NPC Cognitive Simulation Engine
1. **Priority queue system** ✅ (per-NPC goal stack per §6.3 — npc/goals/ package: NpcGoalQueue.java, NpcGoalTickHandler.java. 2 files, ~1,070 lines. 17 GoalTypes, 6 GoalStates, 9 GoalSources, 8 DecisionStyles. Memory-driven re-prioritization every 30s. Emotional override (fear→FLEE). SavedData persistence, static convenience API, rumor/defend goal push helpers.)
2. **Memory system** ✅ (long/medium/short with decay per §6.5 — npc/memory/ package: NpcCognitiveMemory.java, NpcMemoryTickHandler.java. 2 files, ~570 lines. 3-tier decay: long=forever, medium=14 days→short, short=3 days→forgotten. 8 memory categories, emotional state computation, SavedData persistence, static convenience API.)
3. **Knowledge spreading** ✅ (rumor propagation per §6.11 — npc/rumor/ package: Rumor.java, RumorDistortion.java, RumorNetwork.java, RumorEngineEvents.java. 4 files, ~1,100 lines. Distortion model, 6 actor classes, 8 origin types, NPC knowledge tracking, SavedData persistence.)
4. **Realm-scaled cognition** ✅ (planning horizon by realm per §6.6 — npc/cognition/ package: NpcRealmCognition.java. 1 file, ~700 lines. 18 CognitionProfiles (one per RealmId). Planning horizon: Mortal=1 day → Transcendence=effectively infinite. Per-realm: relevant/amplified/suppressed goal types, max goals (4→15), deadline multiplier (0.3x→∞), re-prio weight, rumor believability, karma/faction awareness, concern examples. Static query API: getGoalPriorityModifier, getEffectiveDeadline, getRandomConcern, getWorldviewScale.)
5. **Internal monologue** ✅ (hidden, drives decisions per §6.9 — npc/monologue/ package: NpcInternalMonologue.java, NpcMonologueTickHandler.java. 2 files, ~484 lines. 5-field MonologueSnapshot: Current Objective (from B.1 goals), Problem (B.2 cultivation/world_event memories), Concern (B.2 social/player_action memories), Opportunity (B.3 rumors or positive world events), Danger (B.2 combat/negative emotional memories) + Mood label. Synthesizes from all cognitive subsystems every 200 ticks (10s). NOT persisted — regenerated on load. Static query API: getMonologue, formatMonologue, trackNpc.)
6. **Expectation Model** ✅ (NPCs predict player per §6.13 — npc/expectation/ package: NpcExpectationNetwork.java, NpcExpectationTickHandler.java. 2 files, ~620 lines. SavedData persistence, per-NPC-per-target prediction stores, 8 canonical prediction IDs. Memory-driven inference: OBSERVATION/PLAYER_ACTION/COMBAT/SOCIAL memories → prediction confidence adjustments every 30s. Realm-scaled belief via NpcRealmCognition.getRumorBelievability(). Confidence decay 1%/day, prune at 2%. Max 200 NPC-target pairs, 12 predictions per pair. Reuses Prediction value object from wanglin/ai/reasoning/. Player right-click EntityCultivator → seed baseline predictions. Static query API.)
7. **Dialogue generation from thoughts** ✅ (not stored lines per §6.7 — npc/dialogue/ package: NpcDialogueGenerator.java, NpcDialogueTickHandler.java. 2 files, ~440 lines. Stateless generator: takes MonologueSnapshot (B.5) + trust/hostile/danger confidence (B.6) + active goal urgency (B.1) + decision style (B.1) + interaction count (B.2) → produces DialogueLine. 8 DialogueTone values: TERSE/COLD/WARM/FORMAL/HOSTILE/URGENT/MYSTERIOUS/CONDESCENDING. Tone cascade: hostile≥0.6→HOSTILE, trust≥0.6+mood positive→WARM, trust<0.2+stranger→COLD, urgent goal→URGENT, patient_planner→MYSTERIOUS. Each tone has 6 template variants (same thought, different wording per §6.7). Player right-click EntityCultivator → assembles context from B.1-B.6 → generates line → sends as action bar message. NOT persisted.)
8. **World-sim-tick** ✅ (NPCs continue while player AFK per §6.10 — npc/worldsim/ package: NpcWorldSimulation.java. 1 file, ~390 lines. Cognitive catch-up on player login: tracks lastPlayerOnlineTick via inner WorldSimState SavedData. On login, if >1 min offline: runs batch catch-up across all tracked NPCs (max 200, capped at 30 in-game days). Simulates: goal time-expiry + auto-completion of passive goals (CULTIVATE→new CULTIVATE, REST, TEACH), accelerated memory decay (capped 600 cycles), expectation decay. No physical simulation (no chunk loading) — only cognitive state progression. Player returns to NPCs with shifted goals, faded memories, decayed expectations.)
9. Wang Lin AI ✅ (highest-tier — full simulation, patient_planner, predictive, reasoning-based gifting, NEVER a content dispenser per §4.4 — wanglin/ai/ package: WangLinCognitiveBridge.java, WangLinAiTickHandler.java. 2 files, ~530 lines. Bridges 14 traits + 11 habits + 23 canon defining memories + 7 speech patterns to generic NPC cognitive systems. On level load: pushes 6 persistent goals from habits (RESTRICTION_PRACTICE→CULTIVATE 85p, BEAD_TIME_DILATION→CULTIVATE 80p, RESOURCE_HOARDING→GATHER 60p, PUPPET_MAINTENANCE→CRAFT 55p, MORTAL_LIFE→CUSTOM 70p, INTENTION_TESTING→INVESTIGATE 50p), seeds PATIENT_PLANNER decision style, injects 23 long-term memories from canon events, registers for monologue tracking. Periodic sync every 2 min: habit-driven short-term memories (dawn restriction study, day divine sense scouting, dusk puppet maintenance, night bead cultivation, monthly memory echoes). On first player interaction: seeds 4 cautious expectations (trust=0.05, hostile=0.10, danger=0.20, combat=0.15 — much more cautious than generic 0.15), records PLAYER_ACTION memory. Speech pattern selection: 7 patterns (TERSE_TO_PEERS/COLD_TO_STRANGERS/RESPECTFUL_TO_MENTORS/GENTLE_WITH_FAMILY/WANG_SELF_REFERENCE_WHEN_COLD/RARE_BUT_FOLLOWED_THROUGH_THREATS/IMAGE_RICH_INTERNAL_MONOLOGUE) selected by trust+hostility thresholds. Dao focus from active goal type. Static query API for external systems. Single write boundary: only WangLinCognitiveBridge writes to npc/ systems.)

### Phase C — World Completion
1. Ecosystem entity implementation ✅ (10 creature stubs: fire_phoenix, thunder_bird, sword_spirit_beast, vein_guardian_beast, ancient_god_guardian_beast, ancient_nether_emperor, immortal_ghost, karma_demon, pill_demon, reincarnation_spirit — entity/EntitySpiritBeast.java + entity/EREntityTypes.java SPIRIT_BEAST registration + client/render/SpiritBeastRenderer.java. 2 new Java files (~430 lines). Polymorphic Monster-based entity reading from species/ JSONs. 6 combat AI profiles: territorial_aggressive, passive_fleeing, pack_hunter, guardian, elemental, spirit. Data-driven attributes from species JSON (max_hp, attack_damage, speed, follow_range). 10 species JSONs enriched with canon-specific data: nameCn, appearance, size, behavior, habitat, cultivation, intelligence, diet, techniques, treasures, bloodline_tier, combat_ai, art_direction. Bloodline tier HP scaling: ordinary=30, spirit=50, demon=80, variant=100, ancient=200.)
2. Flora placed_feature implementation (45 flora stubs) — **FloraSystem foundation BUILT** ✅ (8 new files in flora/: BiologicalStage, FloraRenderTier, FloraHiddenProperties, FloraSpecies, RealmHarvestBehavior, FloraBlockEntity, SmallHerbBlock, FloraTickHandler. 4-tier render system: CROSS_SPRITE 70% / SHRUB 20% / TREE 8% / LEGENDARY 2%. 8 biological stages: SEED→SPROUT→YOUNG→MATURE→FLOWERING→FRUITING→DORMANT→DEAD. 11 hidden properties: age, qiSaturation, purity, element, mutation, medicinalPotency, karmicResidue, ownerMarks, disease, moonExposure, tribulationExposure. Realm-scaled harvest: MORTAL uproot/30% quality → QI_CONDENSATION pluck/60% → FOUNDATION roots/85% → CORE_FORMATION 90% → NASCENT_SOUL+ divine sense/100%/unharmed. 4 canon herbs fully implemented: frost_herb 冰寒草, intent_herb 意草, sky_spirit_herb 天灵草, divine_fire_herb 神火草. 4 harvest items registered. 24 flora stubs remain for future population.)
3. Foreign Battleground dimension
4. Cave World dimension
5. Immortal Astral Continent dimension (the finish line per user directive)
6. Custom NBT structure templates (replace vanilla placeholders)
7. Canon audit (file-by-file cross-reference of 632 entries vs implemented)

---

## 8.5 Phase B.10 — ObservationEngine (Emergent Discovery) ✅

**Architectural shift from "trigger-reward" to "world simulation" per user directive.**

The `ExplorationShortcutSystem` (v1, trigger-reward: "do X → get Y") is now supplemented by the `ObservationEngine` (v2, world simulation: "the world behaves consistently, observant players discover opportunities").

**6 new files in `perception/observation/`:**
- `ObservationPhenomenon.java` — 12 Kind enum (BIRD_MIGRATION, SPIRIT_FLUCTUATION, UNUSUAL_SILENCE, SPIRIT_PRESSURE, ANCIENT_INSCRIPTION, BEAST_TERRITORY, KARMIC_TRACE, QI_DENSITY_ANOMALY, CONCEALMENT_TRACE, TRIBULATION_AFTERMATH, FACTION_SCOUT, HERB_CLUSTER). Phenomena generated FROM WORLD STATE, not scripted.
- `KnowledgeTag.java` — 13 perception-expanding tags. Knowledge unlocks perception (mortal who learned herb lore sees spirit herbs where another mortal sees weeds).
- `ObservationChain.java` — 7 predefined chains. Canonical: BIRD_MIGRATION + UNUSUAL_SILENCE + SPIRIT_PRESSURE → "Something powerful slumbers beneath" → grants RECOGNIZES_QI_DENSITY. Chains NEVER announce "Quest Started."
- `PlayerObservationState.java` — per-player noticed phenomena + acquired tags + completed chains. Persisted in NBT.
- `ObservationEngine.java` — core engine. scanForPhenomena() generates phenomena from world state (birds, silence, elevation, herbs, NPCs, Wang Lin pressure). perceive() checks PerceptionTier. evaluateChains() grants knowledge + whispers understanding.
- `ObservationTickHandler.java` — Forge events. Every 400 ticks scans world around each player.

**Design rule:** "No quest. No marker. No reward. The world was being itself; the player paid attention; understanding emerged."

---

## 9. API Rules (MC 1.20.1 / Forge 47.4.0 ONLY)

- `new ResourceLocation(ns, path)` [NOT fromNamespaceAndPath]
- `ForgeRegistries.ITEMS.getKey(item)` [NOT item.getRegistryName()]
- `ForgeRegistries.ITEMS.getValue` returns Item DIRECTLY (not Optional) — NO `.orElse()`
- `IForgeMenuType.create()` [NOT MenuType.create()]
- `serverPlayer.serverLevel()` for ServerLevel
- `ERNetwork.getChannel().send(PacketDistributor.PLAYER.with(() -> player), pkt)`
- `CompoundTag.getXxx(String)` — 1-arg, NOT 2-arg with default
- `RandomSource` (not java.util.Random) from `level.getRandom()`
- `String.replace(char, char)` or `String.replace(CharSequence, CharSequence)` — never char+String mix
- BlockEntity pattern: `extends BlockEntity implements MenuProvider, Container` with ItemStackHandler (NOT BaseContainerBlockEntity for new BEs)

---

## 10. Ground Rules

- NEVER touch `src/app/page.tsx` or `src/components/`. Next.js is RETIRED.
- NEVER rebuild existing systems from scratch — only extend/fix.
- Canon docs (`ri_canon_database.json`, `CANON_*.md`, `DESIGN_*.md`) are the single source of truth.
- Completionist: every named character/location/technique/artifact/event must be accessible in-game.
- No AFK gates, no real-time waiting walls.
- If a cron task description is stale, delete + recreate it with a fresh description (STEP 0 self-improvement).

---

## 11. Companion Documents

- `worklog.md` — chronological work log (11,787+ lines, append-only)
- `UNFINISHED_RESEARCH.md` — gap tracker (468 lines)
- `ARCHITECTURE.md` — technical architecture
- `SYSTEMS_INVENTORY.md` / `SYSTEMS_AUDIT_COMPLETE.md` — system inventory
- `CANON_RI_CHARACTER_DECISIONS.md` — 32 NPC decision profiles (1,952 lines)
- `CANON_RI_EDGE_OF_CANON.md` / `CANON_RI_EDGE_OF_CANON_STATE.md` — edge-of-canon framing
- `DESIGN_UNIFIED_CRAFTING.md` — P1-P13 crafting design (1,315 lines)
- `DESIGN_MANIFESTATION_GIFTS.md` — manifestation gift system
- All `CANON_RI_*.md` files — canon reconstruction (items, techniques, world, civilization, ecology, timeline, wiki findings)
