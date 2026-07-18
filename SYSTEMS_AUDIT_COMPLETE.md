# Complete Systems Audit — Ergenverse

> A single reference document answering: **"What have we designed, and what does each system do?"**
> Source: comprehensive read of worklog.md (6723 lines), types.ts (1348), engine.ts (3888),
> divine.ts (211), soul.ts (200), ergen-depth.ts (447), cosmos.ts (223), codex-entries.ts (792),
> SYSTEMS_INVENTORY.md, and all DESIGN_*.md docs.

## Summary
- **Total systems catalogued:** 105
- **Ported to Java (Forge 65.0.3 / MC 26.2):** 14
- **Not yet ported:** 91
- **User corrections logged:** 26
- **Canonical-content verdicts logged:** 11 (corrections to fan-synthesis that the user explicitly endorsed through deep-wiki research tasks)

---

## How to Read This Audit

Each system entry has:
- **Source:** file:line ranges or worklog Task IDs
- **What:** 1–2 sentences describing the system's purpose
- **Key mechanics:** the equations, data structures, or decision rules
- **User decisions/corrections:** anything the user specifically asked for or corrected
- **Port status:** Ported to Java / Not ported / Partially ported

The **User Corrections Log** at the end is chronological and is the most important deliverable —
these are the constraints the Forge port must obey without re-litigating them.

---

# 1. Core Simulation

### 1.1 Realm Ladder (17 stages across 4 steps)
- **Source:** `realms.ts`, `types.ts` (lines 1–250), `RealmId.java`, worklog Task 1, ergen-knowledge-base.md Part C
- **What:** The unified 17-stage cultivation ladder synthesized across all 6 Er Gen novels. Each realm CHANGES REALITY (unlocks new capability classes), not just stats.
- **Key mechanics:**
  - First Step (1–7): Qi Condensation → Foundation → Core Formation → Nascent Soul → Soul Formation → Soul Transformation → Ascendant
  - Transitional (8–9): Illusory Yin → Corporeal Yang
  - Second Step (10–13): Nirvana Scryer → Nirvana Cleanser → Nirvana Fruit → Spirit Seizer (Dao Realm)
  - Third Step+ (14–17): True Immortal → Ancient → Paragon → Heaven-Trampling/Transcendence
  - Each realm has lifespan, perception tier, canon confidence
- **User decisions:** None specific — synthesizes the 6 novels' realm systems.
- **Port status:** ✅ Ported to Java (`RealmId.java`)

### 1.2 Reality Profile (the constitution of a world)
- **Source:** `types.ts` RealityProfile (line 21), `engine.ts:55-67`, ergen-knowledge-base.md Part B
- **What:** Every Cultivation World carries a Reality Profile (Qi_Density, World_Law_Strength, Max_Cultivation, Heavenly_Will, Tribulation_Severity, Voxel_Reality_Strength, Dao_Affinity, etc.). Controls everything from technique potency to which daos work.
- **Key mechanics:**
  - `combat_potency = cultivation_power × min(1, World_Law_Strength / realm_required_law)`
  - `Voxel_Reality_Strength` = the realm at which terrain becomes mutable by the cultivator
  - Below voxel reality: you fight ON the world. At/above: you fight WITH the world
- **User decisions:** Use ORDINAL fields (fragile/low/medium/high/absolute), NOT numeric multipliers — numeric multipliers are fan-synthesis (RESEARCH-8 verdict)
- **Port status:** ⚠️ Partially ported (`WorldLaws.java` captures origin + dao affinities + space stability + lightning suppression; Reality Profile fields not fully ported)

### 1.3 Absolute Power Threshold + Effective Suppression
- **Source:** `realms.ts` (`absoluteTier`, `effectiveSuppression`, `suppressionFactor`), `engine.ts:68-95`, worklog Task 3
- **What:** The user's core power-scaling mechanic. S_eff = max(0, L_world − P_player) — a higher-world cultivator is suppressed by the gap between the world's law tier and the cultivator's absolute tier.
- **Key mechanics:**
  - `S_eff = max(0, L_world − P_player)`
  - Higher S_eff → throttled Qi gathering, techniques fizzle, damage reduced
  - `suppressBypass` from dao-harmony arts can lower effective S_eff
  - Drives "Higher-World Whiplash" travel loop
- **User decisions:** This was the user's explicit mechanic in Task 3 ("continue working on all your systems" + the Absolute Power Threshold spec)
- **Port status:** ❌ Not ported to Java

### 1.4 Dao System (24 Daos, primary/secondary/understanding %)
- **Source:** `content.ts` DAO_LIST, `types.ts` DaoProfile, ergen-knowledge-base.md Part D
- **What:** Every cultivator (NPC or player) carries a Dao Profile — primary dao, secondary dao, understanding %, obstacles, dao conflict, comprehension style. NOT a class system.
- **Key mechanics:**
  - `Dao_Conflict` (0–1) = friction between primary & secondary → bottlenecks, qi deviation
  - Understanding % grows through cultivation, combat, enlightenment, inheritances
  - Breakthroughs gate on Understanding thresholds, not just Qi
  - Player can FORGE a new dao (Foundation+) — Wang Lin's Slaughter, Meng Hao's Seal, Bai Xiaochun's One-Thought
  - World Compatibility: cultivating in a matching world is faster
- **User decisions:** Player-forged dao was explicitly requested in Task 4 (forge Dao modal)
- **Port status:** ❌ Not ported to Java

### 1.5 Element System (12 elements)
- **Source:** `content.ts` ELEMENTS, `types.ts` ElementId
- **What:** 12 elements compose techniques: fire, water, wind, lightning, earth, wood, metal, ice, light, dark, time, space
- **Key mechanics:** Element + Dao → voxel geometry; Element + Intent → block operator
- **Port status:** ❌ Not ported to Java

### 1.6 Procedural Technique Generation (infinite techniques)
- **Source:** `generators.ts` genTechnique, `engine.ts:644-653`, ergen-knowledge-base.md Part I
- **What:** Techniques are functions of typed inputs (element + dao + realm + intent + origin), not hand-authored. Generates names, grades, effects, costs, side effects, comprehension difficulty.
- **Key mechanics:**
  - Intent types: destruction, protection, healing, concealment, speed, control, summoning, transformation
  - Origin types: ancient-inheritance, self-created, sect-taught, beast-derived, forbidden, divine
  - Self-created techniques scale up with creator's realm on each breakthrough (evolutionary)
  - Each technique gets voxelGeometry + blockOperator + scale + daoHarmony + suppressBypass
- **Port status:** ❌ Not ported to Java

### 1.7 Technique Wheel (Hold G → Category → Sub-Wheel → Release → Cast)
- **Source:** DESIGN_DIVINE_SENSE_TERRAFORMING.md Part 2, ergen-knowledge-base.md Part I, worklog Task 21 (DIVINE-SENSE-DUAL-MODE)
- **What:** The radial technique-casting UI. 10 categories (Sword Arts · Body Arts · Movement · Divine Sense · Formations · Alchemy · Talismans · Artifacts · Flying Sword · Summons). Favorites pin to inner quick-ring.
- **Key mechanics:**
  - Tap G (<200ms) = quick-cast last divine sense technique
  - Hold G (>200ms) = open wheel
  - Mouse toward category → sub-wheel appears → mouse toward technique → release G → cast
- **User decisions:** **"G is exclusively the technique wheel, divine sense is a separate key (V)."** (HEAVEN-AND-EARTH-MANIPULATION, worklog line 6634). Do NOT conflate the two.
- **Port status:** ❌ Not ported to Java (Screen subclass planned, not yet built)

### 1.8 Voxel Factorization Engine (F_destruct vs R_voxel)
- **Source:** `soul.ts:61-110`, `VoxelFactorization.java`, worklog Task 5
- **What:** The core terraforming/destruction engine. Computes whether a cultivator's technique force overcomes the world's voxel resistance, and what happens to the blocks.
- **Key mechanics:**
  - `F_destruct = (B_base × P_player × C_tech × Q_artifact) / (S_eff + 1)²`
  - `R_voxel = μ_mat × (1 + (σ_world × L_world) / 10)`
  - `B_base`: grade base (mortal=5, magical=15, spirit=50, immortal=200, dao=1000)
  - `μ_mat`: material hardness (air=0, dirt=2, wood=3, stone=8, iron_ore=14, obsidian=25, bedrock=100, jade=30)
  - `σ_world`: world law strength (fragile=0.3, low=0.6, medium=1.0, high=1.6, absolute=2.5)
  - Block operators: vaporize / terraform / conceal / freeze / seal / shatter / ignite / purify
  - `terraform` requires F_destruct ≥ R_voxel × 2 (higher threshold than `vaporize`)
- **User decisions:** This was part of the user's 5-Pillar Master Plan (Task 5)
- **Port status:** ✅ Ported to Java (`VoxelFactorization.java`)

### 1.9 Voxel Geometry (9 shapes)
- **Source:** `soul.ts`, `SelectionShape.java`, `VoxelOrientation.java`
- **What:** Each technique has a voxel geometry (shape) determined by its Dao.
- **Key mechanics:**
  - NARROW_SLICE (sword/saber) · DESCENDING_CYLINDER (earth/body) · EXPANDING_DOME (divine sense) · RADIAL_BURST (fire/lightning) · LOCKING_CHUNK (seal/space) · RISING_PILLAR (wood/life) · TIDAL_WAVE (water) · STARFALL_CONE (light/wind) · FROZEN_FIELD (ice)
- **Port status:** ✅ Ported to Java (`SelectionShape.java` + `VoxelOrientation.java`)

### 1.10 Voxel Orientation (hitbox matches animation)
- **Source:** `VoxelOrientation.java`, DESIGN_HITBOXES_AND_FORMATIONS.md Part 2, worklog Task DIVINE-SENSE-PHYSICAL-PLUS-HITBOXES-PLUS-FORMATIONS
- **What:** Adds orientation to voxel geometry so attack hitboxes match the animation. A horizontal slash and vertical cleave are DIFFERENT hitboxes even for the same NARROW_SLICE geometry.
- **Key mechanics:** 5 orientations: HORIZONTAL, VERTICAL, DIAGONAL_RISING, DIAGONAL_FALLING, OMNI. Only NARROW_SLICE, TIDAL_WAVE, and STARFALL_CONE are orientation-dependent.
- **User decisions:** **User explicitly corrected**: "if I'm doing a slashing move that has sword effects that goes in a horizontal way, the thing being cut isn't going to be cut vertically."
- **Port status:** ✅ Ported to Java (`VoxelOrientation.java`)

### 1.11 Cultivation State (not XP — breakthroughs, setbacks, heart demons)
- **Source:** `types.ts` CultivationState, worklog PHASE-1-FOUNDATION
- **What:** Cultivation is not a leveling system. State tracks bottlenecks, breakthrough readiness, setbacks, heart demon risk. Breakthroughs require resolving obstacles (heart demon, karma, talent, resource, comprehension), not just grinding Qi.
- **Port status:** ❌ Not ported to Java

### 1.12 Tribulation System (6 types by realm)
- **Source:** `ergen-depth.ts:341-373` (`getTribulationForRealm`), `engine.ts:498-631`
- **What:** Er Gen tribulations are realm-tiered with unique mechanics. NOT a simple success/fail roll.
- **Key mechanics:**
  - Lightning (realms 0–3): standard heavenly tribulation, 3 strikes
  - Heart Demon (Nascent Soul, realm 4): 1 stage, the most dangerous — face your inner demons
  - Karmic (Soul Formation+, realm 5): 3 stages, lightning burns karmic debt
  - Life-Death (Ascendant+, realm 7): 5 stages, must physically die and resurrect
  - Nine-Nine (Immortal+, realm 11): 9 stages of 9 strikes = 81
  - World (Transcendence+, realm 14): 9 stages of reality-shattering lightning
  - `tribulationKarmaMultiplier(debt)` — atrocious karma = ×2.0 tribulation damage
  - `heavenlyEnmity` adds ~3% tribulation damage + 1 extra bolt per 0.1 enmity
- **User decisions:** "9-9-1" structure was REJECTED as fan-synthesis (RESEARCH-10 verdict)
- **Port status:** ❌ Not ported to Java

### 1.13 Cultivation Arts (功法 — 17 arts)
- **Source:** `ergen-depth.ts:151-185` CULTIVATION_ARTS
- **What:** A Cultivation Art is the foundational method (distinct from techniques). Determines Qi-gathering speed multiplier, realm ceiling, element affinity, special effect.
- **Key mechanics:**
  - qiMultiplier 1.0–4.0× (Basic Qi-Gathering=1.0, Heaven-Defying=4.0)
  - 17 arts: Undying Live Forever Codex (Bai Xiaochun), Slaughter Dao (Wang Lin), Seal-Heavens (Meng Hao), Ji Realm, Demon Sealer, Berserker Bloodline, etc.
  - Cultivation speed formula: `qiGain × art.qiMultiplier × suppression`
- **Port status:** ❌ Not ported to Java

### 1.14 Dao Heart (道心)
- **Source:** `ergen-depth.ts:84-118`
- **What:** The cultivator's fundamental resolve. Distinct from `daoHeartDelta` (ally alignment). Dao Heart is the player's own unshakeable will. Can be tested, shaken, or trigger emotional breakthroughs.
- **Key mechanics:**
  - `strength` (0–100), `tested`, `shaken`, `breakthroughs_from_heart`
  - `checkEmotionalBreakthrough(emotion)` — grief/love/rage/enlightenment/despair can trigger breakthrough at 50% of normal Dao gate (Er Gen signature: Wang Lin breaks through from grief)
- **Port status:** ❌ Not ported to Java

### 1.15 Constitution Evolution (5 layers)
- **Source:** `constitution.ts`, `types.ts` ConstitutionState, worklog Task 13
- **What:** 5 transformable layers, NOT permanent character classes.
- **Key mechanics:**
  - Innate Constitution (7: Mortal Body, Wood-Spirit, Fire-Spirit, Ice-Veined, Ancient-God Trace, Spiritual, Heaven-Defying)
  - Bloodline (5: Dragon, Fox-Ancient, Kunpeng, Thunder Beast, Berserker)
  - Body Tempering State (5: Unrefined → Iron-Bone → Jade-Marrow → Heaven-Tempered → Ancient-God Partial)
  - Soul Traits (5: None → Divine Sense Awakening → Soul Fragment → Heart-Demon Scarred → Soul-Devoured)
  - Dao Physique (4: None → Sword-Heart → One-With-The-World → Transcendent)
  - Triggers: inheritance → bloodline, tribulation_survival → tempering, soul_eating → soul trait, dao_enlightenment → dao physique
- **Port status:** ❌ Not ported to Java

### 1.16 Alchemy — Property-Based (not fixed recipes)
- **Source:** `alchemy.ts`, worklog Task 13
- **What:** Ingredients have hidden properties (cold/heat/yin/yang/purity/etc.). Pills = property combinations. Master alchemists can invent new pills from unmatched property combos (procedural discovery).
- **Key mechanics:**
  - 28 AlchemyProperty types
  - 44 ingredient→property mappings
  - 14 known PillPatterns (Qi-Recovery, Purification, Heart-Calming, Soul-Mending, Karma-Cleansing, Body-Tempering, Foundation, Nine-Turn Core, Heaven-Defying, Dao Comprehension, Lightning Essence, Fury, Blood-Refinement, Meridian-Clearing)
  - `brewPill()` resolves a combination: aggregate properties → check for explosions (low stability + high potency + low skill) → match against patterns or generate new one
- **User decisions:** Property-based alchemy was the user's explicit design — NOT fixed recipes
- **Port status:** ❌ Not ported to Java

### 1.17 Pill Tribulation (丹劫)
- **Source:** `ergen-depth.ts:191-217` (`checkPillTribulation`)
- **What:** Earth-grade+ pills attract lightning. The pill must survive 3–9 strikes or be destroyed. Alchemist's skill helps survival.
- **Port status:** ❌ Not ported to Java

### 1.18 World Memory (deltas, not state)
- **Source:** `world-memory.ts`, worklog Task 13
- **What:** Stores "what changed" (deltas), not "what happened" (full state). Only significant events persist. NPCs reference memories in dialogue.
- **Key mechanics:**
  - Significance: legendary (permanent) / major (500y) / notable (100y) / minor (20y)
  - Categories: player_action, world_event, faction_change, ecology_shift, cosmic, discovery, destruction, creation
  - `changes[]` (field + from + to), `npcDialogue` auto-generated based on significance + year
  - Capped at 100 memories (keeps most significant)
- **Port status:** ❌ Not ported to Java

---

# 2. Perception

### 2.1 Divine Sense — Soul Power Hybrid Model
- **Source:** `divine.ts:13-37`, `DivineSense.java`, worklog Task 7 + DIVINE-SENSE-CORRECTION
- **What:** The transformative sense. NOT radar. Hybrid model: S_realm baseline × manual multipliers + tempering (soft-capped).
- **Key mechanics:**
  - `S_sense = S_realm × (1 + ΣM_manual) + S_tempering`
  - S_realm baseline: mortal=1, Qi=10, Foundation=100, Core=500, Nascent Soul=10000, Soul Formation=30000, Ascendant=80000, Transcendence=100M
  - ΣM_manual = sum of soul-cultivation manual multipliers
  - S_tempering soft-capped at S_realm × (realm_order+1) — dark-arts tempering can exceed realm but not infinitely
- **User decisions:** The snapshot-pulse pattern was forgotten once (DIVINE-SENSE-CORRECTION). User called me out. Rebuilt correctly.
- **Port status:** ✅ Ported to Java (`DivineSense.java`)

### 2.2 Divine Sense Radius (logarithmic — anti-lag)
- **Source:** `divine.ts:43-53`, `DivineSense.java`
- **What:** Logarithmic radius formula that keeps radius in 50–150 block range across ALL tiers. No lag.
- **Key mechanics:**
  - `R_sense = R_base × ln(S_sense + 1) × (1 − L_world/12) × Π(1 − σ_seal)`
  - R_base = 16 blocks (default; raised by movement/divine-sense arts)
  - L_world = world law tier (0–12); higher law = smaller radius
  - σ_seal = local sealing-formation suppression (0..1 each, multiplied)
  - Result: radius stays in 50–150 block range. Sub-millisecond scan cost.
- **User decisions:** **User explicitly called out**: my earlier proposed distances (131072 at Ascendant, unlimited at Transcendence) "would lag the game to death." Rebuilt with logarithmic formula.
- **Port status:** ✅ Ported to Java (`DivineSense.java`)

### 2.3 Divine Sense Pulse (Snapshot — button-triggered, NOT continuous)
- **Source:** `divine.ts:95-153`, `DivineSense.java`
- **What:** SNAPSHOT PULSE — one frame of computation, not a continuous tick. Returns PulseResult (radius, reactions, perceived objects, soul-fracture risk).
- **User decisions:** User corrected twice (DIVINE-SENSE-CORRECTION + DIVINE-SENSE-DUAL-MODE). The pattern is snapshot-pulse, NOT continuous.
- **Port status:** ✅ Ported to Java (`DivineSense.java`)

### 2.4 Divine Sense Continuous Mode (hold-for-active)
- **Source:** `DivineSense.java` ContinuousState, DESIGN_DIVINE_SENSE_TERRAFORMING.md Part 1
- **What:** Hold V → divine sense stays active; release V → turns off. Re-pulses every 20 ticks (1 sec). Each re-pulse drains S_sense (~1%/sec). If S_sense drops below 50% of max → `soul_fracture` status.
- **User decisions:** User corrected: I forgot that the snapshot-pulse is ALSO a hold-for-continuous button. The two modes coexist.
- **Port status:** ✅ Ported to Java (`DivineSense.java` ContinuousState)

### 2.5 NPC Confrontation (ΔS = S_player − S_npc, 3 scenarios)
- **Source:** `divine.ts:84-153`, `DivineSense.java`
- **What:** When the player pulses divine sense near NPCs, three confrontation scenarios fire based on the soul-power delta and the NPC's 13-D soul matrix.
- **Key mechanics:**
  1. **Arrogant Interception** (ΔS < 0, NPC stronger): if `faceObsession ≥ 0.7` → 60% counter-strike → 0.5 soul-fracture risk
  2. **Absolute Suppression** (ΔS ≥ 5000): if `mortalityPanic ≥ 0.6` or `shamelessness ≥ 0.6` → NPC drops to knees, begs, hidden trade tabs unlock
  3. **Unmasking the Hypocrite** (righteous_hypocrite archetype, ΔS ≥ 2000): false aura shatters, true soul matrix revealed
  - Plus: ally_alerted (20% if ΔS < 5000), unnoticed (default)
- **Port status:** ✅ Ported to Java (`DivineSense.java`)

### 2.6 Spiritual Camouflage Registry (C-values)
- **Source:** `divine.ts:65-68`, `DivineSense.java`
- **What:** Concealed objects have camouflage values (C). Player's S_sense must exceed C to perceive.
- **Key mechanics:** C-values: inheritance_vault=25000, ancient_formation=20000, sealed_pavilion=15000, trap_array=8000, spirit_vein=500, hidden_cave=200
- **Port status:** ✅ Ported to Java (`DivineSense.java` CAMOUFLAGE_BY_KIND)

### 2.7 Three-Layer World Model (Physical/Spiritual/Dao)
- **Source:** `WorldLayer.java`, `ChunkLayerData.java`, worklog PIVOT-TO-FORGE-MOD
- **What:** Reality has three layers per chunk. Existence is observer-independent (per Prime Directive).
- **Key mechanics:**
  - PHYSICAL layer — vanilla Minecraft (mortals can interact)
  - SPIRITUAL layer — spirit veins, formations, spirit herbs (mortals can't touch)
  - DAO layer — karmic traces, Dao imprints, heavenly laws
  - `physicalBreakAffectsSpiritual()` always returns false — mortal can break physical flag, but spiritual anchor persists
- **User decisions:** **THE PRIME DIRECTIVE** (PIVOT-TO-FORGE-MOD, worklog line 6489): "Never hide or reveal objects because of the player's level. Hide or reveal interactions according to the laws of the world. The world is objective and exists independently of the player."
- **Port status:** ✅ Ported to Java (`WorldLayer.java`, `ChunkLayerData.java`)

### 2.8 Layered Perception (Objective + Observer)
- **Source:** `Objective.java`, `ObjectiveNature.java`, `ObserverContext.java`, `PerceptionEngine.java`, `PerceptionTier.java`, `PerceptionResult.java`, worklog PIVOT-TO-FORGE-MOD
- **What:** Things exist independently of observers. Cultivation changes UNDERSTANDING, not existence. The rabbit example: mortal sees "big rabbit"; Qi sees "Spirit Beast. Leave. Now."; beast-tamer sees "bloodline unusually pure"; Nascent Soul sees karmic history; Transcendent sees cosmic significance. THE RABBIT NEVER CHANGES.
- **Key mechanics:**
  - `Objective` interface — things that exist independently
  - `ObjectiveNature` — observer-independent truth (trueName, trueRank, trueRealm, bloodline, origin, karmicHistory, canonConfidence)
  - `ObserverContext` — realm, daoComprehension, divineSenseActive, knownSpecies, specializations
  - `PerceptionTier` — derived from realm, about UNDERSTANDING not existence
  - `PerceptionResult` — what the observer understands (perceivedName, perceivedDescription, recognizedRank, canInteract, concealed, realityLevel)
- **User decisions:** **User's PIVOT directive**: "Reality is objective; cultivation changes understanding, not existence." The old PerceptionEngine (deer→spirit deer rendering swaps) was DELETED.
- **Port status:** ✅ Ported to Java (`PerceptionEngine.java` + 5 supporting files)

### 2.9 Concealment Formation (spiritual-anchored, mortals can't break)
- **Source:** `ConcealmentFormation.java`
- **What:** A formation anchored in the SPIRITUAL layer. Has `perceptionFloor` and `breakTier`. Mortals CANNOT break it — their pickaxe hits only the physical shell. The formation may shift according to its own laws but does not break.
- **Port status:** ✅ Ported to Java (`ConcealmentFormation.java`)

### 2.10 Karma Vision (snapshot pulse, 20y cooldown)
- **Source:** `engine.ts:2533-2553` (`activateKarmaVisionAction`), worklog Task 21
- **What:** A snapshot pulse that reveals all karmic threads with lineage colors. 20-year cooldown. Brief active window, auto-deactivates.
- **Port status:** ❌ Not ported to Java

### 2.11 Karmic Thread Tracing (7 lineages)
- **Source:** `engine.ts:2554-2598` (`traceKarmicThreadAction`), `ergen-depth.ts:32-45` KARMA_LINEAGE_META
- **What:** Trace karmic threads to their terminus. For bloodline/inheritance threads, requires soul-search first (bloodlineMemoryExtracted). Reveals hidden inheritance with restriction trials.
- **Key mechanics:** 7 lineages: bloodline (crimson), debt (gold), enmity (black), grace (white), inheritance (violet), dao_companion (jade), oath (silver)
- **Port status:** ❌ Not ported to Java

### 2.12 Soul Search (forbidden art)
- **Source:** `engine.ts:1855-1926` (`soulSearch`), `divine.ts`
- **What:** Forbidden art. Steals a technique from an NPC's mind. Requires ΔS > 10000 + weak-willed NPC. On success: +technique, brain-damages NPC, +20% karma, Karmic Stain, 30% heart-demon risk. On failure: backlash → Soul Fracture.
- **Port status:** ❌ Not ported to Java

### 2.13 Qi Sense Toggle
- **Source:** `engine.ts:2891-2901` (`toggleQiSenseAction`)
- **What:** Toggle passive Qi sense.
- **Port status:** ❌ Not ported to Java

### 2.14 Combat Intent Recognition
- **Source:** `engine.ts:3055-3091` (`classifyCombatIntent`)
- **What:** Classifies combat events: accident / betrayal / duel / unprovoked attack / cooperative combat. Drives NPC relationship updates (respect for duels, grudge for betrayal, trust hit for accidents).
- **Port status:** ❌ Not ported to Java

---

# 3. Combat & Manipulation

### 3.1 Heaven and Earth Manipulation System (3 manipulation types)
- **Source:** `WorldObject.java`, `ManipulationCapability.java`, `HeavenAndEarthManipulation.java`, `SelectionShape.java`, DESIGN_HEAVEN_AND_EARTH_MANIPULATION.md
- **What:** Universal "Can I move this?" comparison engine. Three SEPARATE manipulation types — NOT interchangeable. Brute force cannot extract a soul. Dao transformation cannot be forced. This is why Wang Lin shrank a mountain at early First Step.
- **Key mechanics:**
  - **Type 1 — Physical Manipulation** (move mass): `physicalCapability = telekineticForce × (1+technique) + divineSense × (1+technique) + treasure + formation + telekineticForce×daoCompat×0.3 + divineSense×daoCompat×0.5`. Requires force + Qi.
  - **Type 2 — Spiritual Manipulation** (extract souls/veins): `spiritualCapability = divineSense × (1+technique) + treasure + divineSense×daoCompat×0.5` (telekineticForce EXCLUDED). Requires perception + specialized method.
  - **Type 3 — Dao Manipulation** (transform nature): `daoCapability = daoCompat × 10000 × (1+technique) + treasure` (telekineticForce AND divineSense EXCLUDED). Requires comprehension.
  - Object Resistance = `physicalMass + spiritualMass + daoAnchoring + worldLawRes + formationAnchoring + ownerRes + historicalDaoImprint + heavenlyRes + karmicSignificance`
  - Outcomes: SUCCESS (cap ≥ res), PARTIAL (cap ≥ res×0.7), FAILURE (cap < res×0.7 → backlash)
  - `WorldObject` factories: dirtBlock, stoneBlock, spiritVein, mountain, sectMountain, wangLinBirthplace, pebble, baiFanImprintMountain
- **User decisions:** **User explicitly corrected** (HEAVEN-AND-EARTH-MANIPULATION, worklog line 6634):
  1. "G is exclusively the technique wheel, divine sense is a separate key"
  2. Provided the universal "Can I move this?" comparison equation
  3. Provided the three manipulation types (Physical/Spiritual/Dao) from Er Gen examples
  4. **Warned against hardcoding realms** — the equation handles it
  5. Asked me to engineer suggestions for the shape system
- **Port status:** ✅ Ported to Java (4 files: `WorldObject.java`, `ManipulationCapability.java`, `HeavenAndEarthManipulation.java`, `SelectionShape.java`)

### 3.2 Selection Shape (Dao-determined + freeform sculpting)
- **Source:** `SelectionShape.java`, DESIGN_HEAVEN_AND_EARTH_MANIPULATION.md Part 4
- **What:** Hybrid A+D shape system. Default shape from Dao (sword→slice, earth→cylinder, fire→burst, seal→chunk, wood→pillar, water→wave, divine sense→dome). Shift = refine mode with freeform sculpting. Scroll = scale, left-click = add, right-click = remove, R = rotate.
- **Key mechanics:** DAO_GEOMETRY map per Dao; geometryFootprint(geometry, scale) → block count per geometry at scale
- **User decisions:** User asked me to engineer 4 suggestions (A: Dao Hand, B: Preset Palette, C: Bounding Box, D: Freeform). I recommended hybrid A+D.
- **Port status:** ✅ Ported to Java (`SelectionShape.java`)

### 3.3 Move vs Break vs Extract vs Transform
- **Source:** DESIGN_HEAVEN_AND_EARTH_MANIPULATION.md Part 5
- **What:** Manipulation is NOT mining. Four operations:
  - **Move** — relocate volume (terraform operator)
  - **Break** — shatter volume in technique shape (vaporize/shatter operator)
  - **Extract** — pull spiritual essence, leave physical (spiritual manipulation)
  - **Transform** — change nature (dao manipulation)
- **User decisions:** User: "instead of mining the ground, I want to be able to break it with my techniques, in whatever erratic way my techniques are shaped." Erratic shapes for chaotic daos (slaughter/void), clean shapes for ordered daos (formation/seal).
- **Port status:** ⚠️ Partially ported (operator enum exists; the four-way distinction needs wiring)

### 3.4 Divine Sense Physical Manipulation (CORRECTED to equal weight)
- **Source:** `ManipulationCapability.java`, DESIGN_HITBOXES_AND_FORMATIONS.md Part 1
- **What:** Divine sense IS the gripping force for physical manipulation. In Er Gen, Ji Realm Divine Sense IS a weapon, Restriction Flags are wielded WITH divine sense, Mountains Crumble FORMS physical mass from divine sense.
- **Key mechanics:** CORRECTED formula (was 10%, now EQUAL weight):
  - `physicalCapability = telekineticForce × (1+technique) + divineSenseStrength × (1+technique) + treasure + formation + telekineticForce×daoCompat×0.3 + divineSenseStrength×daoCompat×0.5`
- **User decisions:** **User explicitly corrected**: divine sense should play a bigger part in physical manipulation — search the wiki.
- **Port status:** ✅ Ported to Java (`ManipulationCapability.java`)

### 3.5 Artifact / Weapon System
- **Source:** `weapon-catalog.ts` (32 weapons), `types.ts` Artifact, worklog Task 12
- **What:** Broader than SentientArtifact — covers weapons/defense/utility with grade, attack power, defense rating, durability, element affinity, special effects, weight, novel attribution.
- **Key mechanics:**
  - ArtifactType: weapon / defense / utility / sentient
  - ArtifactSubtype: flying_sword, saber, spear, bow, shield, armor, robe, storage_bag, spirit_boat, token, talisman_set, pill_furnace, communication, array_disk, seal_stamp
  - Grades: mortal → magical → spirit → immortal → dao
  - Weapons degrade with use; can be repaired for spirit stones
  - Attack power = sum of equipped weapons × durability factor × suppression
  - Beasts drop weapons (15% drop chance on hunt, scaled to beast's rank)
- **Port status:** ❌ Not ported to Java

### 3.6 Sentient Artifact Bonding + Mounting + Overclocking + Soul Brand Cracking
- **Source:** `engine.ts:1583-1622` (bond), `2612-2679` (mount/bloodRefine/overclock/crackSoulBrand), worklog Task 4 + Task 21
- **What:** Sentient treasures (immortal-grade+) choose/reject bearers based on dao compatibility + temperament + suppression. Bonding reduces dao conflict. Multiple advanced operations:
- **Key mechanics:**
  - `attemptBondArtifact` — compatibility + temperament + suppression determine acceptance; rejection burns Qi + logs to ownerLog
  - `mountArtifactAction` — mounting with spiritual weight (tied to flight)
  - `bloodRefinementBindingAction` — blood refinement (lifespan cost)
  - `spiritStoneOverclockAction` — overclock with spirit stones
  - `crackSoulBrandAction` — multi-layer soul brand cracking (BrandCrackMethod)
- **Port status:** ❌ Not ported to Java

---

# 4. Soul & Karma

### 4.1 13-D Soul Matrix
- **Source:** `soul.ts:11-20`, worklog Task 5
- **What:** Every entity (humans, beasts, demons, protagonists) inherits the same 13-dimensional personality vector.
- **Key mechanics:** 13 dimensions, grouped:
  - **Ethical:** altruism, ambition, paranoia, curiosity, honor
  - **Social:** faceObsession, heavenlyDefiance, mortalTether, vengefulness
  - **Comedic:** mortalityPanic, chaosConcoction, shamelessness, loyaltyTether
  - Each value 0–1
- **User decisions:** Part of the user's 5-Pillar Master Plan (Task 5)
- **Port status:** ❌ Not ported to Java

### 4.2 Mega Archetype Registry (12 archetypes, emergent)
- **Source:** `soul.ts:22-59`, worklog Task 5
- **What:** Archetypes are CLASSIFIED from soul-matrix slider values, not chosen. 12 archetypes, each with a loop-behavior.
- **Key mechanics:**
  - 10 named archetypes: Philanthropic Hermit, Righteous Hypocrite, Obsessive Scholar, Arrogant Young Master, Uncompromising Seeker, Lovable Cowardly Prodigy, Kleptomaniac Spirit Bird, Nagging Meat Jelly, Loyal Culinary Glutton, Reincarnated Old Monster
  - Plus: Feral Beast (animals), Generic Cultivator (balanced)
  - `classifyArchetype(soul, isBeast)` — scores each archetype by bias profile match, requires top score > 0.35 else generic
- **Port status:** ❌ Not ported to Java

### 4.3 Psychological Cascade (Physics → Psychology)
- **Source:** `soul.ts:112-162`, worklog Task 5
- **What:** When a cultivator casts a technique, the voxel destruction alters the emotional state of every loaded NPC in the vicinity — per their 13-D vector.
- **Key mechanics:** Reactions:
  - High paranoia + vaporized → **flee** (mortalityPanic +0.1)
  - High curiosity → **investigate** (walks to crater)
  - High mortalTether + destroyed settlement → **rage** (vengefulness→1, loyaltyTether→0)
  - High loyaltyTether + ally caster → **shield_player**
  - High ambition/face + not ally → **envy** (paranoia +0.1)
  - Default → indifferent
- **Port status:** ❌ Not ported to Java

### 4.4 Ally Loyalty Matrix
- **Source:** `soul.ts:164-183`, worklog Task 5
- **What:** Stability = Affinity + Fear×0.5 − ΔDaoHeart. If Stability < 0: high honor → silent desertion; low honor + high ambition/paranoia → tactical betrayal.
- **Key mechanics:**
  - `computeAllyStability(affinity, fear, daoHeartDelta)` returns stability
  - `resolveAllyBetrayalCheck(soul, stability)` → loyal / desert / betray
  - `daoHeartDelta` grows from soul-conflicting casts (+5–8% per conflicting cast), heals from protection/healing casts (−6%), decays naturally (−1%/year)
- **Port status:** ❌ Not ported to Java

### 4.5 Karmic Threads (7 lineages)
- **Source:** `ergen-depth.ts:11-50`
- **What:** Karma is not a number — it's a network of visible bonds connecting people across lifetimes. At Nascent Soul+, cultivators perceive them.
- **Key mechanics:**
  - 7 types: life_debt, blood_vendetta, love, gratitude, betrayal, karmic_enemy, dao_bond
  - 7 lineages (colored): bloodline, debt, enmity, grace, inheritance, dao_companion, oath
  - `lifetime` = how many reincarnations it persists (1=this life, -1=permanent)
  - `leadsToInheritanceId` — can lead to hidden inheritance vaults (requires tracing)
  - `bloodlineMemoryExtracted` — has the player soul-searched the source NPC
- **User decisions:** User: "you should be able to toggle karmic threads" (Task 15)
- **Port status:** ❌ Not ported to Java

### 4.6 Karmic Memory (NPC memory of player)
- **Source:** `engine.ts:3092-3129`, worklog PHASE-1-FOUNDATION
- **What:** Persistent NPC memories of player actions. `recordKarmicMemory` records events; `getMemoriesAboutPlayer` retrieves them. Protagonists recall shared history for dialogue context.
- **Port status:** ❌ Not ported to Java

### 4.7 Dao Heart — see §1.14

### 4.8 Heart Demon (心魔) — Status + bottleneck
- **Source:** `divine.ts:160` (status), `ergen-depth.ts:341` (tribulation), worklog Task 7
- **What:** Heart Demon is BOTH a status condition (raises breakthrough failure, karmic/tribulation backlash) AND a tribulation type at Nascent Soul+. Universal across 5/6 novels (BTT less explicit).
- **Port status:** ❌ Not ported to Java

### 4.9 Soul Fracture (status)
- **Source:** `divine.ts:157`, worklog Task 7
- **What:** Status inflicted by NPC counter-strikes during divine sense pulse. Blinds divine sense radar; grounds flying artifacts; camera shake/blur. Cure: Soul-Mending Elixir.
- **Port status:** ❌ Not ported to Java

### 4.10 Xianxia Status Matrix (6 statuses)
- **Source:** `divine.ts:155-185`, worklog Task 7
- **What:** 6 statuses with cure elixirs.
- **Key mechanics:**
  - `soul_fracture` — blinds divine sense, grounds flight. Cure: Soul-Mending Elixir
  - `sealed_meridians` — locks % of max Qi. Cure: Meridian-Clearing Powder
  - `qi_deviation` — casting drains health. Cure: Purification Pill
  - `heart_demon` — raises breakthrough failure. Cure: Heart-Calming Incense
  - `karmic_stain` — reputation marker, blacklisted. Cure: Nirvana cleansing
  - `meat_jelly` (Lord Third) — invincible nagging companion. Cure: reduce karmicDebt below atrocious
- **Port status:** ❌ Not ported to Java (types referenced in `DivineSense.java` comments; engine not ported)

### 4.11 Karma Backlash Engine
- **Source:** `divine.ts:187-211`, worklog Task 7
- **What:** Karma amplifies tribulations. Righteous sects blacklist at atrocious debt. Lord Third (Meat Jelly) spawns at atrocious karma — hijacks dialogue, draws aggro.
- **Key mechanics:**
  - `karmicTier(debt)` → pure / light / tainted / heavy / atrocious
  - `tribulationKarmaMultiplier(debt)` → ×1.0 / ×1.25 / ×1.6 / ×2.0
  - `isBlacklisted(world)` — true at atrocious karma OR karmic_stain status
- **Port status:** ❌ Not ported to Java

### 4.12 Karmic Severance
- **Source:** `engine.ts:3002-3054` (`karmicSeveranceAction`)
- **What:** Sever a karmic thread. World-level consequences for oaths.
- **Port status:** ❌ Not ported to Java

---

# 5. Er Gen Depth Systems

### 5.1 Ancient Clans (Body Cultivation Parallel Path)
- **Source:** `ergen-depth.ts:53-82`, worklog Task 14
- **What:** The God/Devil/Demon body-cultivation path is PARALLEL to Qi cultivation. Body cultivators kindle "stars" inside their body. 7 stars per clan → merge all 3 clans → 27 stars = peak Ancient God. Wang Lin is the canonical hybrid.
- **Key mechanics:**
  - `AncientClanState { godStars: 0-7, devilStars: 0-7, demonStars: 0-7, merged, totalStars, bodyCultivationRealm }`
  - `kindleStar(state, clan)` increments the clan's stars; at 7+7+7 triggers MERGENCE → 27 stars
- **Port status:** ❌ Not ported to Java

### 5.2 Cultivation Arts (功法) — see §1.13

### 5.3 Avatars / Soul Splitting
- **Source:** `ergen-depth.ts:226-244`, worklog Task 14
- **What:** At Nascent Soul+, the soul becomes independent and can be split into fragments. Each fragment becomes an avatar that cultivates independently. Max 3 avatars.
- **Key mechanics:** `AvatarState { id, name, realm, qi, location, task, yearsActive }`
- **Port status:** ❌ Not ported to Java

### 5.4 Cave-World (洞天)
- **Source:** `ergen-depth.ts:246-265`, worklog Task 14
- **What:** Personal pocket dimension inside a storage treasure. Cultivators live in it, grow herbs, store beasts. The Heaven-Defying Bead contains one.
- **Key mechanics:** `CaveWorld { name, size 0-100, herbGarden, spiritVeinQuality, inhabitants, ambientQi }`. `ambientQi` multiplies cultivation speed when meditating inside.
- **Port status:** ❌ Not ported to Java

### 5.5 Reincarnation / Past Lives
- **Source:** `ergen-depth.ts:267-294`, worklog Task 14
- **What:** Past life memories as a cultivation resource. Surfaces during deep meditation. Breaking the cycle of samsara is a Transcendence goal.
- **Key mechanics:** 6 past-life templates (mortal farmer, rogue cultivator, sect patriarch, spirit beast, immortal who chose to reincarnate, scholar). Each grants dao understanding.
- **Port status:** ❌ Not ported to Java

### 5.6 Sealing Arts (封印)
- **Source:** `ergen-depth.ts:219-225`, worklog Task 14
- **What:** Meng Hao's entire Dao is the Seal. Sealing requires equal or greater realm. Seal-Heavens Art grants +30% seal bonus; Demon Sealer Art grants +200%.
- **Key mechanics:** `attemptSeal(target, targetName, targetRealm, playerRealm, sealArtBonus)`. 6 seal targets: beast, npc, technique, array, space, time.
- **Port status:** ❌ Not ported to Java

### 5.7 Restricted Domain (禁制领域)
- **Source:** `ergen-depth.ts:411-432`, worklog Task 15
- **What:** Personal domain that restricts actions of those caught inside. Wang Lin and Meng Hao both use restricted domains in combat.
- **Key mechanics:** 6 restriction types: no flying, no Qi gathering, no teleportation, no divine sense, no artifact use, suppressed cultivation. Count scales with realm.
- **Port status:** ❌ Not ported to Java

### 5.8 Corpse Refinement (炼尸)
- **Source:** `ergen-depth.ts:376-390`, worklog Task 15
- **What:** Wang Lin refines corpses into combat puppets — a major RI mechanic.
- **Key mechanics:** `RefinedCorpse { refinementStage 0-9, combatPower, loyalty 0-1, innateAbility }`. `advanceCorpseRefinement()` — stage++ → combatPower × 1.3, loyalty +0.05.
- **Port status:** ❌ Not ported to Java

### 5.9 Soul Lamp (命灯)
- **Source:** `ergen-depth.ts:392-401`, worklog Task 15
- **What:** Sects track members' life/death via soul lamps. Lamp flickers when owner is in danger; goes out on death. Used for sect management + assassination detection.
- **Port status:** ❌ Not ported to Java

### 5.10 Incense System (香火)
- **Source:** `ergen-depth.ts:403-410`, worklog Task 15
- **What:** Bai Xiaochun's signature. 5 incense types with different effects.
- **Key mechanics:** calming (cures heart_demon), warding (deters demonic beasts 200 blocks), cultivation (×1.5 Qi), divine (attracts revelation/tribulation), mortal_attracting (faith power, 1000-block radius).
- **Port status:** ❌ Not ported to Java

### 5.11 Blood Seal (血印)
- **Source:** `ergen-depth.ts:434-447`, worklog Task 15
- **What:** Wang Lin's one-use combat items carrying killing intent. 4 types.
- **Key mechanics:** tracking, explosive, binding, soul_mark. Power scales with creator's realm + killing intent.
- **Port status:** ❌ Not ported to Java

### 5.12 Emotional Breakthroughs (Er Gen signature)
- **Source:** `ergen-depth.ts:108-118`, `engine.ts:2247-2269` (`emotionalBreakthroughAction`)
- **What:** Er Gen's signature mechanic — breakthroughs triggered by emotional extremes. Wang Lin breaks through from grief at his family's death.
- **Key mechanics:** 5 emotions (grief 30%, enlightenment 40%, rage 20%, love/despair 15%) can trigger breakthrough at 50% of normal Dao gate.
- **Port status:** ❌ Not ported to Java

---

# 6. Cosmology

### 6.1 Cosmological Tree (6 branches, 19 nodes)
- **Source:** `cosmological-tree.ts`, `CosmologicalTree.java`, worklog PHASE-1-INTEGRATION
- **What:** The cross-novel cosmology as a navigable tree. 6 branches: immortal_astral, vast_expanse, eternal_lands, arid_triad, starry_cosmos, wanggu_star_rings, root_dao. 19 nodes total.
- **Key mechanics:**
  - Travel rules: within-branch (realm-gated), cross-branch (4th Step + karmic anchor)
  - 9 travel methods (walk → void_ship)
  - `RegionStatus`: known_canon / partially_known / unknown / frontier
  - `DiscoveryTier`: minimum perception tier to learn a node exists
- **Port status:** ✅ Ported to Java (`CosmologicalTree.java` — 5 nodes registered; rest stubbed for Phase 0)

### 6.2 Nested Cosmology (5-layer hierarchy)
- **Source:** `cosmos.ts:14-28`, worklog Task 8
- **What:** The Azure Cloud World's cosmic address: Singular World (Azure Cloud) → True World (Planet Suzaku) → Expanse Cosmos (Azure) → Vast Expanse → Universe. Each with its own worldLawIntensity + temporalDilation.
- **Key mechanics:**
  - 5 tiers: universe, vast_expanse, expanse_cosmos, true_world, singular_world
  - Vast Expanse is `isVoid: true`, `voidFriction: 0.05` (soul-depletion)
  - Each layer has temporalDilation (Universe=0.1, Vast Expanse=0.5, others=1.0)
- **Port status:** ❌ Not ported to Java (only the 5-node CosmologicalTree is ported)

### 6.3 Vast Expanse Vortexes (5 kinds)
- **Source:** `cosmos.ts:30-52`, worklog Task 8
- **What:** 5 kinds of floating hazards in the Vast Expanse.
- **Key mechanics:** spatial_tear, dead_star, floating_ruin, gravity_well, river_of_time_eddy. Each has pull (10-100), reward, danger realm.
- **Port status:** ❌ Not ported to Java

### 6.4 River of Time Echoes (4 kinds)
- **Source:** `cosmos.ts:54-74`, worklog Task 8
- **What:** Ancient imprints from dead civilizations, pullable by karma + divine sense. Phantom NPCs materialize as grateful life-debt allies.
- **Key mechanics:**
  - 4 kinds: item_blueprint, technique_fragment, phantom_npc, historical_record
  - Each has karmicCost (0.1-0.4) + sSenseRequired (50000-800000)
  - Pulling inflicts Karmic Stain
- **Port status:** ❌ Not ported to Java

### 6.5 Dimension Fold (3D/4D/5D)
- **Source:** `cosmos.ts:76-99`, worklog Task 8
- **What:** Spatial-dimension folding lets the player bypass 3D barriers, invisible to NPC raycasts, at a divine-sense-tempering cost per turn.
- **Key mechanics:**
  - 3D = default; 4D requires Nascent Soul; 5D requires Soul Formation
  - 4D costs 1000 S_sense/turn for 10 turns; 5D costs 10000 S_sense/turn for 5 turns
  - Force-unfolds if tempering runs dry
- **Port status:** ❌ Not ported to Java

### 6.6 Opportunities (27 kinds, the bustling world)
- **Source:** `cosmos.ts:101-165`, worklog Task 9
- **What:** 27 distinct emergent chances that arise and expire — NOT scripted quests. Things that CAN happen.
- **Key mechanics:**
  - 19 base + 8 cosmic/metaphysical: wandering_merchant, distressed_cultivator, rare_herb_bloom, beast_migration, auction_notice, sect_recruiter, inheritance_rumor, karmic_encounter, spirit_vein_surge, tribulation_witness, bandit_ambush, ancient_array_awakening, dao_companion_meeting, fallen_cultivator_loot, mortal_village_in_peril, dimensional_rift, phantom_echo, mysterious_pill_recipe, bloodline_awakening, heaven_trampling, god_corpse, dao_debate, devil_awakening, memory_flash, tribulation_theft, debt_collection, qi_deviation_zone
  - Each has reward / risk / minRealm / minWorldLaw / maxWorldLaw / baseWeight / karmicResonance / chainsInto
  - Success scales with realm gap + suppression
- **Port status:** ❌ Not ported to Java

### 6.7 Cosmic Ripple (universe-scale events)
- **Source:** `cosmos.ts:167-175`, worklog Task 8
- **What:** Universe-scale crossover events that alter future world-sector generation.
- **Key mechanics:** Sources: cataclysm in Mountain & Sea Realm, Allheaven's will shifting, Transcendent stepping across Vast Expanse, paradox in River of Time, fall of Immortal Astral Paragon. Effects: alters factions, shifts dao-affinity of unborn cultivators, seeds new inheritances, ripples bloodlines, changes world-law.
- **Port status:** ❌ Not ported to Java

---

# 7. World

### 7.1 World Laws (origin, Dao affinities, space stability, lightning)
- **Source:** `WorldLaws.java`, worklog PIVOT-TO-FORGE-MOD
- **What:** Every location knows WHY it exists. Origin reason → Dao affinities → space stability → lightning suppression → consequences (which herbs grow, which beasts evolve, which techniques work, which formations fail).
- **Key mechanics:** 6 canon locations registered: Zhao Country, Sea of Devils, Land of Ancient God, Mountain and Sea Realm, Heavenspan Realm, South Phoenix Continent. Origin reasons: ANCIENT_BATTLE, SEALED_BEAST, FALLEN_IMMORTAL, etc.
- **Port status:** ✅ Ported to Java (`WorldLaws.java`)

### 7.2 Spirit Veins
- **Source:** `types.ts`, `CausalEcology.java`, worklog Task 8
- **What:** Spiritual energy sources. Sparse/moderate/rich/saturated. The foundation of the causal ecology.
- **Port status:** ⚠️ Partially ported (referenced in `CausalEcology.java`)

### 7.3 Spirit Herbs (5 grades × 9 environments)
- **Source:** `flora-fauna.ts`, `alchemy.ts` HERB_PROPERTIES, worklog Task 8 + EXHAUSTIVE-HERBS
- **What:** 165 herbs/plants/minerals cataloged across all 6 novels. 5 grades (mortal → spirit → earth → heaven → dao/immortal) × 9 environments. Each maps to a specific cure pill.
- **Key mechanics:**
  - Frost-Lotus → Purification Pill (cures qi_deviation)
  - Soul-Mend Orchid → Soul-Mending Elixir (cures soul_fracture)
  - Meridian-Thread Vine → Meridian-Clearing Powder (cures sealed_meridians)
  - Heart-Calm Lily → Heart-Calming Incense (cures heart_demon)
  - Karmic-Cleansing Reed → Karma-Cleansing Pill (cures karmic_stain)
- **Port status:** ❌ Not ported to Java

### 7.4 Beast Species (food web, temperament, innate techniques)
- **Source:** `bestiary-catalog.ts` (131 real Er Gen creatures), worklog Task 11
- **What:** 131 real Er Gen creatures across 4 ecological categories (Spatial & Cosmic Leviathans, Bloodline Mutants, Alchemical Chimeras, Conceptual/Law-Bound). 4 intelligence tiers (Mortal Beast → Spirit Beast → Demon Cultivator → Ancient Beast).
- **Key mechanics:**
  - Each species: category, novel, layer, behaviorScript, voxelScale, drops (modular loot table with weights + crafting-use), aggroType, special flags (destinedCompanion, gastroRealm, dualForm)
  - Food web: Spirit Rabbit → Spirit Fox → Spirit Wolf → Crimson Python → Azure Dragon-Whelp → Nine-Tail Fox Matriarch → Kunpeng
  - **Ancient beasts are geography** — a sleeping Kunpeng IS a regional climate
- **User decisions:** User demanded "real Er Gen species, ZERO procedural fillers" (Task 11)
- **Port status:** ❌ Not ported to Java

### 7.5 Causal Ecology (Lotka-Volterra)
- **Source:** `CausalEcology.java`, worklog PIVOT-TO-FORGE-MOD
- **What:** Every spirit beast exists because of an ecosystem. Spirit Vein → Flora → Herbivores → Predators → Apex → Sect cultivators → Merchants. Lotka-Volterra-inspired trophic model.
- **Key mechanics:**
  - Exterminate herbivores → predators starve → herbs spread → sects notice
  - The ecosystem evolves causally — not a spawn table
  - 5 zones: sky / ground / ocean / underground / forbidden_zones (from ecology-engine.ts, 1382 lines)
- **Port status:** ✅ Ported to Java (`CausalEcology.java` — core trophic model only; full 5-zone engine needs port)

### 7.6 History Engine (7-phase lifecycle)
- **Source:** `history-engine.ts` (1274 lines), worklog 6-HISTORY-ENGINE
- **What:** Every world is generated with a 100,000-year historical timeline. Every faction/location has a 7-phase lifecycle: birth → rise → golden_age → decline → twilight → current → possible_future.
- **Key mechanics:** Locations are CONSEQUENCES of history. A ruined city isn't random — it fell for a reason. Each event seeds present-day world-state. The engine runs forward: new historical events append to the timeline, becoming future "ancient history."
- **Port status:** ❌ Not ported to Java

### 7.7 World Pulse (daily simulation tick)
- **Source:** `engine.ts:677-1154` (`tick`), worklog Task 2-b
- **What:** The world-tick orchestrator. Advances world-clock N years, recomputes L2/L3 NPC sim, emits events (capped at 80), advances opportunities, dimensional mana pool, dimension fold drain, cosmic ripple generation, NPC arrivals, beast Qi gain, status durations.
- **Port status:** ❌ Not ported to Java

### 7.8 Dimensional Mana Pool (finite, drainable)
- **Source:** `engine.ts:3130-3158` (`tickDimensionalManaPool`), worklog PHASE-1-FOUNDATION
- **What:** Finite world energy pool (100000 capacity). Depletes as cultivators drain it. Depletion has consequences (spirit storms, mana_depletion events).
- **Port status:** ❌ Not ported to Java

### 7.9 Local Antagonist (per-world Heaven-Will)
- **Source:** `types.ts` LocalAntagonist, worklog PHASE-1-FOUNDATION
- **What:** Per-world Heaven-Will antagonist — NOT a universal Allheaven. RI: Seven-Colored Daoist + All-Seer. ISSTH: Allheaven. AWE: Heavenspan Daoist/Gravekeeper. BTT: Broken God Face / Eminent Desolation.
- **User decisions:** **User-endorsed correction** (RESEARCH-7, RESEARCH-14): Allheaven is ISSTH-specific, NOT universal. Each novel has its own local Heaven-Will antagonist.
- **Port status:** ❌ Not ported to Java

### 7.10 World Exit Framework (karmic completion)
- **Source:** `engine.ts:3159-3224`, worklog PHASE-1-FOUNDATION + RESEARCH-8
- **What:** How cultivators leave sealed worlds. Authorized (karmic completion, world-ownership, ascension, transfer arrays) vs Illegal (Domain breach, spatial tearing, forced ascension) vs Free (Transcendence only).
- **Key mechanics:** Wang Lin left Cave World via karmic completion (RI ch.901 "Clearing All Karma") + ownership transfer (killed Seven-Colored Daoist). NOT a formation, NOT a Domain breach.
- **User decisions:** User-endorsed canon verification (RESEARCH-8)
- **Port status:** ❌ Not ported to Java

### 7.11 Living Skies + Terrifying Oceans + Mountains with Personality
- **Source:** `types.ts` SkyTraffic + OceanFeature, `generators.ts` genSky/genOcean, ergen-knowledge-base.md Part K
- **What:** The world-feel texture layer. Living skies (always alive — flying swords, spirit boats, cultivators, messengers, cloud beasts, tribulations, spirit storms, occasionally something ENORMOUS passes overhead). Terrifying oceans (never empty — leviathans, ruins, sunken sects, moving islands, ghost ships). Mountains with personality (sacred peaks, sect HQs, sleeping beasts, some are LITERALLY the corpse of an ancient being).
- **Port status:** ❌ Not ported to Java

### 7.12 Reality Profile — see §1.2

### 7.13 Higher-World Whiplash (travel)
- **Source:** `engine.ts:1321-1353` (`travelToWorld`), worklog Task 3
- **What:** Travel between worlds. Player keeps realm, but L_world changes, S_eff recalculated. Gated by minRealm. Reachable worlds: Planet Suzaku (L2), Immortal Astral Continent (L6), Mountain & Sea Realm (L9), Vast Expanse Starry Sky (L12).
- **Port status:** ❌ Not ported to Java

---

# 8. Protagonists

### 8.1 3-Tier LOD Simulation (Mythic/Regional/Reality)
- **Source:** `engine.ts:3383-3528` (`getProtagonistSimulationTier`, `tickMythicSimulation`, `manifestProtagonistAction`), worklog PHASE-2-AGENT-MATRIX
- **What:** The 6 novel protagonists (Wang Lin, Meng Hao, Bai Xiaochun, Su Ming, Wang Baole, Xu Qing) simulate in 3 tiers based on player proximity:
  - **Tier 0 (Mythic)** — far away: background simulation with goals, probabilities, destiny pressure, generated history. No entities. No CPU waste.
  - **Tier 1 (Regional)** — mid-distance: aggregate stats (population, average_realm, goals, mood)
  - **Tier 2 (Reality)** — player enters their world: full entities with canonical arsenals
- **Port status:** ❌ Not ported to Java

### 8.2 Protagonist True Bodies (frozen, immutable canon)
- **Source:** `engine.ts:3772-3823` (`checkProtagonistAwakening`), worklog PHASE-2-AGENT-MATRIX
- **What:** The 6 protagonists' true bodies are frozen — immutable canon. They can be "awakened" when conditions are met (player reaches their era/realm).
- **Port status:** ❌ Not ported to Java

### 8.3 Protagonist Manifestations (clones)
- **Source:** `engine.ts:3225-3326` (`createProtagonistManifestation`), worklog PHASE-2-AGENT-MATRIX
- **What:** When a player enters a protagonist's world, a clone manifestation appears with full soul matrix, canonical arsenal (811 items), and personality.
- **Port status:** ❌ Not ported to Java

### 8.4 Sovereign Reconstitution (clones can't be permakilled)
- **Source:** `engine.ts:3550-3642` (`sovereignReconstitutionAction`), worklog PHASE-2-AGENT-MATRIX
- **What:** On defeat, clones reconstitute at their ancestral anchor. Relationship updates based on combat context: DUEL_DECLARED → respect +15%; COOPERATIVE_COMBAT → affinity +5%; ACCIDENT → trust −2%; UNPROVOKED_ATTACK → grudge +50%; BETRAYAL → permanent enmity.
- **Port status:** ❌ Not ported to Java

### 8.5 Bond Events (5–7 events raise recognition)
- **Source:** `engine.ts:3327-3357` (`recordBondEvent`), worklog PHASE-2-AGENT-MATRIX
- **What:** 5–7 significant events raise a protagonist's recognition of the player to max — not hundreds of hours. Recognition drives dialogue mood (distant → neutral → friendly → respectful → wary → hostile).
- **Port status:** ❌ Not ported to Java

### 8.6 Canonical Arsenal (811 items, unlock thresholds)
- **Source:** `protagonist-arsenals.ts` (1118 lines, 811 entries), worklog EXHAUSTIVE-ARSENAL
- **What:** 811 canonical items across 6 protagonists with continuous unlock thresholds (0.03–0.98) mapped to narrative mastery points. NOT a fixed 15-item list per protagonist.
- **Key mechanics:** Wang Lin: 309 items (Heaven-Defying Bead at 98%, Ji Realm at 76%). Meng Hao: 67 items (10th Hex at 95%). Bai Xiaochun: 73 items. Su Ming: 131 items (Seed of Life Extermination at 85%). Wang Baole: 96 items. Xu Qing: 135 items.
- **Port status:** ❌ Not ported to Java

### 8.7 Personality-Driven Supply
- **Source:** `engine.ts:3643-3742` (`personalityDrivenSupplyAction`), worklog PHASE-2-AGENT-MATRIX
- **What:** Each protagonist proactively aids the player based on their Soul Matrix.
- **Key mechanics:** Wang Lin (paranoid/analytical) → slip-feeds concealment talismans. Bai Xiaochun (anxious) → panic-throws 5 medicinal pills. Meng Hao (mercenary) → lends spirit stones with tracked favor debt. Su Ming (truth-seeker) → shares Dao insight. Wang Baole (refiner) → repairs equipment. Xu Qing (survivor) → purges karmic corruption.
- **Port status:** ❌ Not ported to Java

### 8.8 Protagonist Dialogue + Memory
- **Source:** `engine.ts:3750-3771` (`getProtagonistDialogueContext`), worklog PHASE-2-AGENT-MATRIX
- **What:** Returns memories, recognition level, and mood for dialogue generation. Protagonists have unique dialogue per context (Wang Lin: "Your Dao is strong. I acknowledge you." / "You dare? I will remember this.").
- **Port status:** ❌ Not ported to Java

### 8.9 Clone Absorption / Independence
- **Source:** `engine.ts:3823-3865` (`absorbCloneAction`, `keepCloneIndependentAction`), worklog PHASE-2-AGENT-MATRIX
- **What:** Player can absorb a manifested clone (gain its cultivation progress) or keep it independent (it continues as a separate entity).
- **Port status:** ❌ Not ported to Java

### 8.10 Karmic Mirror (non-destructive inheritance)
- **Source:** `engine.ts:3358-3382` (`mirrorCanonicalItemAction`), worklog PHASE-1-FOUNDATION
- **What:** Non-destructive inheritance of canonical items. Tier 1 Seed, evolves independently. The protagonist keeps their item; the player gets a mirror that evolves separately.
- **Port status:** ❌ Not ported to Java

---

# 9. Items & Crafting

### 9.1 Alchemy — Property-Based — see §1.16

### 9.2 Pill Tribulation — see §1.17

### 9.3 Artifacts — see §3.5

### 9.4 Beast Taming (UNIVERSAL — not Pokémon)
- **Source:** `engine.ts:1623-1701` (`attemptTame`), worklog Task 10
- **What:** ANY creature can be tamed — including ancient-tier — via beating into submission OR soul-binding artifact. NOT a capture system. Taming method varies by tier + player options.
- **Key mechanics:**
  - Normal: physical dominance (beat into submission)
  - Spirit: mental coercion (Nascent Soul+) OR physical dominance OR artifact-assisted
  - Demon: dao recognition (artifact) OR mental coercion OR physical (hard)
  - Ancient: ONLY via bonded sentient artifact (soul-binding) — too powerful to beat normally
  - `useArtifact` parameter lets the player choose artifact-assisted taming
  - Success chance factors: tier method, player realm gap, suppression, artifact compatibility
- **User decisions:** "Not Pokémon" — explicitly requested universal taming
- **Port status:** ❌ Not ported to Java

### 9.5 Beast Lifecycle (feral → spiritual → mythical)
- **Source:** `engine.ts:1702-1739` (`offerPillToBeast`), worklog Task 5
- **What:** Spirit beast lifecycle: feral (qiCap 10000) → spiritual (qiCap 100000) → mythical (qiCap 1000000 + humanoidForm). Advances via pills or natural Qi gain on tick.
- **Port status:** ❌ Not ported to Java

### 9.6 Formations (HYBRID — blocks + items)
- **Source:** `Formation.java`, `formation-talisman-catalog.ts` (100 entries), DESIGN_HITBOXES_AND_FORMATIONS.md Part 3, worklog DIVINE-SENSE-PHYSICAL-PLUS-HITBOXES-PLUS-FORMATIONS
- **What:** Hybrid formation system: blocks for sect-scale, items for personal/portable, items for talisman-formations.
- **Key mechanics:**
  - 10 formation types: DEFENSIVE, OFFENSIVE, TRAPPING, TRANSPORT, SEALING, SURVEILLANCE, ILLUSION, SOUL, ALCHEMY_AUX, HYBRID
  - 6 grades: MORTAL, SPIRIT, EARTH, HEAVEN, DAO, IMMORTAL
  - 4 anchoring types: BLOCK_BASED, ITEM_BASED, CONSUMABLE, PERSISTENT_ITEM
  - **Formation flag block** — anchors a formation node in the Spiritual Layer. Mortals see flags (physical); cultivators see the formation (spiritual). Mortal can break the flag, but the spiritual anchor persists or shifts (per Prime Directive).
  - **Formation core block** — heart of multi-block formation. Stores blueprint. Powered by spirit vein.
  - 9 canon formations registered: Restriction Flag (Wang Lin, immortal, item-based), the 4 Great Restrictions (Annihilation/Time/Life-Death/Destruction, dao-grade), Sect-Protecting Array (universal, spirit, block-based), Heng Yue Sect Protecting Array, Six Cultivation Planets Restriction (Wang Lin's feat, dao), Soul Refining Sect Blood-Sacrifice Array, Transport Array (paired, block-based)
- **User decisions:** User asked me to explain formation + talisman implementation options; I recommended Option C (hybrid)
- **Port status:** ⚠️ Partially ported (`Formation.java` definition only; FormationFlagBlock + FormationCoreBlock not yet built)

### 9.7 Talismans (paper / jade slip / soul banner / etc.)
- **Source:** `formation-talisman-catalog.ts`, DESIGN_HITBOXES_AND_FORMATIONS.md Part 3
- **What:** 14 talisman types: life_lantern, jade_slip, formation_flag, soul_banner, sealing_stamp, mirror, paper_talisman, pearl_bead, cauldron_wok, coffin, bell, compass, mask, other.
- **Key mechanics:**
  - **Paper talismans (consumable)**: right-click → burn away → effect fires (fireball, concealment veil, warding shield). Effect in Spiritual Layer.
  - **Persistent talismans**: soul banners, coffins, compasses — durability/charge. Often bound via blood refinement or soul branding.
  - **Jade slips**: right-click to read. Branded via divine sense. Mortal feels nothing; Qi reads surface; Foundation reads full content.
- **Port status:** ❌ Not ported to Java

---

# 10. Flight & Movement

### 10.1 Flight Profile (ambient absorption vs drain)
- **Source:** `engine.ts:2292-2364` (`computeFlightProfile`), worklog Task 21
- **What:** Flight is sustainable via ambient Qi absorption or draining. Cruise vs sprint modes. Mortals can't fly; Foundation+ ride flying treasures (not free flight).
- **Key mechanics:**
  - Cruise: `netQiDrain = flightCost − ambientAbsorption` (free if ambient ≥ cost)
  - Multipliers: purity, dao-resonance, constitution, mount
  - Dead-zone detection (Qi-depleted regions)
- **Port status:** ❌ Not ported to Java

### 10.2 Sprint Burst
- **Source:** `engine.ts:2396-2415` (`sprintBurstAction`), worklog Task 21
- **What:** 8× velocity burst. Costs 30% Qi + 18% core stress + 3y cooldown. Crossing 80% stress cracks foundation (Dao Heart shaken, −8 strength, 50y recovery).
- **Port status:** ❌ Not ported to Java

### 10.3 Tribulation Parasitism (absorb/devour storms)
- **Source:** `engine.ts:2416-2504` (`enterStormAction`, `surviveStormRoundAction`, `leaveStormAction`), worklog Task 21
- **What:** Hijack NPC breakthrough tribulations. Absorb mode → body tempering (tier-capped). Devour mode → Dao insight (requires soul-refining art). NPC breakthrough fails. Raises heavenlyEnmity.
- **Key mechanics:**
  - ParasiteTribulation spawns for Core Formation+ NPCs (~25%/year chance, max 2 active)
  - Each 0.1 heavenlyEnmity adds ~3% tribulation damage + ~1 extra bolt
- **Port status:** ❌ Not ported to Java

### 10.4 Body Tempering (from storms)
- **Source:** `engine.ts:2438-2504`, worklog Task 21
- **What:** Body tempering from absorbed tribulation lightning. Tier-capped — can't exceed your realm's body limit.
- **Port status:** ❌ Not ported to Java

### 10.5 Mounted Flight
- **Source:** `types.ts` PlayerState.mountedEntityId, worklog Task 21
- **What:** Mount a tamed beast for flight. Spiritual weight of mounted artifact affects drain.
- **Port status:** ❌ Not ported to Java

### 10.6 Dao Companion Bonding
- **Source:** `engine.ts:2969-3001` (`bondingCeremonyAction`), worklog PHASE-1-FOUNDATION
- **What:** Dao companion bonding ceremony. Shared cultivation bonus, combined techniques.
- **Port status:** ❌ Not ported to Java

### 10.7 Ally Protection Command
- **Source:** `engine.ts:2949-2968` (`protectCommandAction`)
- **What:** Command to protect an ally in combat.
- **Port status:** ❌ Not ported to Java

---

# 11. Canon System

### 11.1 Canon Engine (0-5 confidence + Reality Level)
- **Source:** `canon-engine.ts`, `CanonEngine.java`, worklog PHASE-1-INTEGRATION
- **What:** Tracks confidence levels for every canon claim. Filters what content is allowed in known canon regions.
- **Key mechanics:**
  - CanonConfidence (0–5): 5=novel statement, 4=wiki-backed, 3=implication, 2=community, 1=speculation, 0=filler
  - RealityLevel: reality / tradition / rumor / legend / unknown / forbidden
  - `canonFilter(confidence, regionStatus)` — Level 0 FORBIDDEN in known canon regions
  - 7 locations × 7 tiers = 49 perception entries
- **Port status:** ✅ Ported to Java (`CanonEngine.java`)

### 11.2 Region Status (known_canon / partially_known / unknown / frontier)
- **Source:** `CosmologicalTree.java`, worklog PHASE-1-INTEGRATION
- **What:** Each cosmological node has a region status that controls what canon content can appear.
- **Port status:** ✅ Ported to Java (`CosmologicalTree.java`)

### 11.3 Layered Location Perception
- **Source:** `engine.ts:3866+` (`getPerceptionOfLocation`)
- **What:** Perceive a location at multiple layers — mortal sees thatched roofs, Transcendent sees "this world is an artificial farm." Same location, different understanding.
- **Port status:** ❌ Not ported to Java

---

# 12. Codex (living wiki)

### 12.1 Codex Entries (138 entries, namespaced IDs)
- **Source:** `codex-entries.ts` (792 lines), worklog Task 21
- **What:** 138 codex entries with namespaced IDs (ergen:category/name) matching Forge ResourceLocation. 3 layers: World (player-facing lore, no formulas), Simulation (dev-facing mechanics), Personal (save-file specific).
- **Key mechanics:**
  - 13 categories: cosmology, realms, dao, beasts, alchemy, artifacts, formations, talismans, factions, geography, history, terminology, legends
  - Structured data: BeastData, HerbData, SectData, TechniqueData
  - `relatedEntries` — hyperlink graph (43+ connected entries)
  - `discoveryState` — unknown / rumored / partially_known / fully_known / researched
  - `playerNotes` + `npcCommentary` for personal-codex growth
  - `lastUpdated` (game year)
- **User decisions:**
  - User demanded 3 layers (World/Simulation/Personal) — codex was mixing 3 purposes
  - User demanded structured data fields (BeastData, HerbData, SectData, TechniqueData)
  - User demanded namespaced IDs matching Forge ResourceLocation
  - User demanded `relatedEntries` hyperlink graph
  - User demanded `discoveryState` for mystery/incomplete entries
  - User demanded ALL "In the simulation..." text removed from world-layer entries
  - User: "codex needs content from start of game all the way to end game"
- **Port status:** ❌ Not ported to Java

---

# 13. Data Catalogs (need Java port)

### 13.1 Protagonist Arsenal Catalog — see §8.6 (811 items)

### 13.2 Bestiary Catalog — see §7.4 (131 beasts)

### 13.3 Weapon Catalog — see §3.5 (32 weapons)

### 13.4 Pill Catalog — see §1.16 (87 pills)

### 13.5 NPC Catalog (104 NPCs)
- **Source:** `npc-catalog.ts`, worklog EXHAUSTIVE-NPCS-B
- **Port status:** ❌ Not ported to Java

### 13.6 Sect Catalog (47 sects)
- **Source:** `sect-catalog.ts`, worklog MATRIX-1
- **Port status:** ❌ Not ported to Java

### 13.7 Location Catalog (77 locations)
- **Source:** `location-catalog.ts`, worklog Task 17 + MATRIX-1
- **Port status:** ❌ Not ported to Java

### 13.8 Formation & Talisman Catalog — see §9.6, §9.7 (100 entries; 311 documented in worklog)

### 13.9 Cultivation Arts Catalog — see §1.13 (17 arts)

### 13.10 Spirit Herbs Catalog — see §7.3 (165 herbs/materials)

### 13.11 Flora-Fauna Food Web
- **Source:** `flora-fauna.ts`, worklog Task 8
- **What:** 12+ beast species with explicit predator/prey chains.
- **Port status:** ❌ Not ported to Java

---

# 14. Other Systems

### 14.1 Forge MDK Scaffold
- **Source:** `forge-mod/build.gradle`, `settings.gradle`, `gradle.properties`, `mods.toml`, `pack.mcmeta`, worklog PIVOT-TO-FORGE-MOD
- **What:** Forge MDK scaffold for MC 26.2 / Forge 65.0.3. (Note: MC 26.2 is an unusual version — user's Modrinth launcher screenshot confirmed it.)
- **Port status:** ✅ Complete (cannot compile in this env — no javac; user runs ./gradlew)

### 14.2 World Philosophy (Prime Directive)
- **Source:** `WorldPhilosophy.java`, worklog PIVOT-TO-FORGE-MOD
- **What:** The Prime Directive encoded as a doc class. Every developer reads this first. "Never hide or reveal objects because of the player's level. Hide or reveal interactions according to the laws of the world. The world is objective and exists independently of the player."
- **Port status:** ✅ Ported to Java (`WorldPhilosophy.java`)

### 14.3 Mod Entry Point
- **Source:** `Ergenverse.java`
- **What:** Forge mod entry point.
- **Port status:** ✅ Ported to Java (`Ergenverse.java`)

### 14.4 Deterministic RNG
- **Source:** `engine.ts:32-50` (`actionRng`), worklog Task 6
- **What:** Deterministic per-action RNG: derives from world seed × Knuth + year + monotonic counter. Same seed + same actions → identical playthrough. Fixed the determinism bug where Math.random was used in 12 places.
- **Port status:** ❌ Not ported to Java

### 14.5 State Caps (anti-bloat)
- **Source:** `engine.ts:48-50`, worklog Task 6
- **What:** Caps to prevent unbounded save growth: LOG_CAP=60, HISTORY_CAP=200, events capped at 80.
- **Port status:** ❌ Not ported to Java

### 14.6 NPC Arrival Trickle
- **Source:** `engine.ts` tick, worklog Task 6
- **What:** When L1 NPC count drops below 5, the tick spawns occasional new arrivals (travelers, new disciples, migrants, rogue cultivators) at the player's settlement. Keeps the living-world sim populated. Verified: NPC count stays stable at 6 across 40 years.
- **Port status:** ❌ Not ported to Java

### 14.7 Spiritual Restraint (designed, not built)
- **Source:** SYSTEMS_INVENTORY.md §14
- **What:** Per user priority 5: "Necessary once companions matter." Not yet implemented.
- **Port status:** ❌ Not built

### 14.8 Fortuitous Rebound (designed, not built)
- **Source:** SYSTEMS_INVENTORY.md §13
- **What:** Per user priority 6: "Very important, but after the world systems." Not yet implemented.
- **Port status:** ❌ Not built

### 14.9 Perception Demo (proves the rabbit example)
- **Source:** `PerceptionDemo.java`, worklog PIVOT-TO-FORGE-MOD
- **What:** Proves the rabbit example works. The same SPIRIT_RABBIT object perceived by 5 different observers produces 5 different understandings. The rabbit is not mutated.
- **Port status:** ✅ Ported to Java (`PerceptionDemo.java`)

---

# User Corrections Log (chronological — the most important deliverable)

These are the constraints the Forge port must obey without re-litigating them. Each is sourced to a specific worklog Task ID.

### Early corrections (web app era)

1. **(Task 3) Absolute Power Threshold spec**: User provided `S_eff = max(0, L_world − P_player)` as the core power-scaling mechanic. Tiered resources (mortal/celestial/immortal/dao), dao-conflict resolution via harmony items, self-created evolutionary techniques, lifespan/karma costs as real consequences, voxel geometry matrix.

2. **(Task 5) 5-Pillar Master Plan**: User delivered: 13-D Soul Matrix, Mega Archetype Registry (10 archetypes classified from sliders — NOT chosen), Voxel Factorization equations (F_destruct vs R_voxel), Physics→Psychology cascade, Spirit Beast lifecycle (feral→spiritual→mythical), Ally Loyalty Matrix (Stability = Affinity + Fear×0.5 − ΔDaoHeart; desertion vs tactical betrayal).

3. **(Task 6) Determinism audit**: User implicitly demanded same-seed reproducibility. Fixed 12 places using Math.random → seeded actionRng.

4. **(Task 7) Divine Sense + Status + Karma spec**: User's 4-part spec — hybrid Soul Power, Divine Sense pipeline with NPC confrontation, Spiritual Camouflage registry, Xianxia Status Matrix, Karmic Backlash engine.

5. **(Task 13) Property-based alchemy**: User's design document requested property-based alchemy (ingredients have hidden properties, pills = combinations), constitution evolution (5 transformable layers), World Memory (stores deltas not state).

6. **(Task 15) "toggle karmic threads" + "go back through again, dig deep into the wiki for EVERYTHING, don't abridge, leave no potential system unturned."** — User demanded wiki-grade research. Led to 16 RESEARCH tasks.

7. **(Task 17) "go in depth with the rest of the systems, assets, places, etc. Work autonomously. Imagine you're a game studio, er-gen living wiki. Start working on all the game assets, making them as accurate as possible. Heavy attention to detail."** — Drove exhaustive content expansion (131 beasts, 32 weapons, 33 herbs, 17 arts, 811 protagonist items, 19 visual assets).

8. **(Task 19) Codex 8.5/10 feedback**: User's 6 specific corrections:
   - Codex is static not living
   - Mixes 3 purposes (lore + mechanics + personal)
   - Doesn't connect entries
   - Doesn't remember player
   - Needs structured data fields
   - Needs mystery/incomplete entries
   - Needs 3 separate layers (World/Simulation/Personal)

9. **(Task 21) Namespaced IDs spec**: User provided detailed spec for `ergen:category/name` matching Forge ResourceLocation, and fully structured entries with BeastData, HerbData, SectData, TechniqueData. Pointed out structured interfaces were defined but not populated.

### Research verdicts (user-endorsed corrections to fan-synthesis)

10. **(RESEARCH-3) Meng Hao 4th Hex name correction**: User's "Self-Righteous" (对错禁) is FABRICATED. Canonical name is "Self Hex" (自我禁). Function is mass cloning, NOT vector reversal.

11. **(RESEARCH-3) Meng Hao Hex function corrections**: User's "32-block radius" / "1.5x replication" / "swaps internal status" / "freezes mob ticking" / "multiplies Dao damage" are all Minecraft-game-ified FABRICATIONS. Real functions: Real-Unreal (illusory transmutation), Present-Ancient (time stop/acceleration), Self (mass cloning), Inside-Outside (spatial rift consume/release), Life-Death (absolute control), Seal the Heavens (seals Allheaven).

12. **(RESEARCH-3) Tenth Hex exists**: User listed 9 Hexes; there are TEN. "My Fate is to Seal the Heavens Like a Demon Hex" — Meng Hao's transcendent 10th.

13. **(RESEARCH-3) 9-9-1 tribulation REJECTED as fan-synthesis**: Not canonical. The generic 9×9=81 lightning is the xianxia trope, applied uniformly — NOT a Meng-Hao-specific pattern.

14. **(RESEARCH-4) "Undying Blood Avatar" UNVERIFIED**: The specific named technique with 80% defense reflection + detonate + teleport mechanics does NOT appear in canon. Use canonical fire clone + Soul-Storing Mirror + Undying Blood one-drop milestone instead.

15. **(RESEARCH-2) "Ji Wu" clone CONTRADICTED**: No such canonical clone exists in Su Ming's 5-clone list (Nascent Soul / Surging Indulger / Écang / Cultivation / True World). The closest match is the Nascent Soul Clone (possessing Ji Yun Hai's corpse).

16. **(RESEARCH-2) Bald Crane + Lord Fifth fusion CONTRADICTED**: No canon source supports any fusion. Lord Fifth is Meng Hao's parrot (ISSTH); Bald Crane is Su Ming's crane (Pursuit of Truth). Different novels, different species. The only cross-novel link is Su Ming sends Bald Crane to Wang Lin's Immortal Astral Continent.

17. **(RESEARCH-2) Bald Crane illusion "bypasses divine sense" INVERTED**: Canon: Su Ming's divine sense SAW THROUGH the illusion. The crane's illusions deceive ordinary perception, NOT divine sense.

18. **(RESEARCH-5) Chen Qingzi ≠ Wang Yiyi**: User conflated two characters. Chen Qingzi is Wang Baole's SENIOR BROTHER (male, betrayer). Wang Yiyi is the mask spirit (female, Wang Lin's daughter, Wang Baole's eventual spouse).

19. **(RESEARCH-7) Allheaven is ISSTH-specific, NOT universal**: User's table implied Allheaven is universal. Canon: Allheaven is the named ruler of the Vast Expanse in ISSTH (Meng Hao's antagonist). Each novel has its OWN local Heaven-Will antagonist (RI: All-Seer + Seven-Colored Daoist; AWE: Heavenspan Daoist/Gravekeeper; BTT: Broken God Face).

20. **(RESEARCH-8) Numeric world law multipliers REJECTED as fan-synthesis**: Er Gen never publishes decimal law multipliers. The user's table (0.3x/0.4x/1.2x/1.5x/1.8x/2.0x/2.5x/3.0x output + 0.1x safety) is FAN-SYNTHESIS. Use ordinal descriptors (fragile/low/medium/high/absolute) — already in the project's Reality Profile.

21. **(RESEARCH-8) "Paragon Nine Heavens" corrected to "Paragon Nine Seals"**: User conflated the founder of Mountain and Sea Realm with the 33 besieging Heavens. Founder's name is "Nine Seals" (九封).

22. **(RESEARCH-9) "Seven Star Rings" corrected to 36 Star Rings**: User said "Seven Star Rings". Canon: 36 Star Rings (72 total counting Lower). Each Star Ring contains countless Star Domains.

23. **(RESEARCH-9) Wang Baole ≠ Wang Lin reincarnation**: They COEXIST. Wang Lin appears in AWWP as "Paragon Wang" (separate living character) and mentors Wang Baole. Wang Baole may be a reincarnation of the "black wooden nail" remnant souls, NOT of Wang Lin himself.

24. **(RESEARCH-14) "Four Supremes (God/Devil/Demon/Ghost)" is fan synthesis**: The framework maps RI's Ancient Clan trinity + a fourth "Ghost" slot onto transcendent protagonists. Wang Lin = "The God" is CONFIRMED; Su Ming = "The Devil" is thematic (via 求魔 title) but not explicit; Meng Hao = "The Demon" is weakest evidence; Patriarch Vast Expanse = "The Ghost" is unverified. Use the 6-protagonist framing instead.

25. **(RESEARCH-10) Heart Demon is universal across 5/6 novels**: Confirmed in RI, ISSTH, AWE, Ptt, AWWP. BTT less explicit (likely present via mutagen/god-path psychology). Treat as universal with LOW-confidence flag for BTT.

### Pivot to Forge mod (the BIG corrections)

26. **(PIVOT-TO-FORGE-MOD, worklog line 6489) THE PRIME DIRECTIVE**: "Never hide or reveal objects because of the player's level. Hide or reveal interactions according to the laws of the world. The world is objective and exists independently of the player." Project pivoted from Next.js web app to Java Forge mod. Reality is objective — cultivation changes understanding, not existence. The old PerceptionEngine (deer→spirit deer rendering swaps) was DELETED.

27. **(DIVINE-SENSE-CORRECTION, worklog line 6531) "I forgot the snapshot-pulse pattern we'd already built AND proposed block distances that would lag the game to death."**: User called me out. The pattern is snapshot-pulse (button-triggered, NOT continuous). My proposed distances (131072 at Ascendant, unlimited at Transcendence) would freeze the game. Rebuilt with logarithmic radius formula. Radius stays in 50–150 block range across ALL tiers.

28. **(DIVINE-SENSE-DUAL-MODE, worklog line 6587) "I forgot (1) the snapshot-pulse is also a hold-for-continuous button, (2) divine sense can MOVE stuff (rip veins, move mountains, lift objects), (3) the technique wheel (hold G → category → sub-technique → release → cast), (4) the Voxel Factorization Engine with the terraform block operator."**: User called me out again. Built ContinuousState (hold mode with soul strain), ManipulationTechnique enum (9 techniques including Vein Extraction, Mountain Moving, Object Lifting), ported VoxelFactorization to Java.

29. **(HEAVEN-AND-EARTH-MANIPULATION, worklog line 6632) "G is exclusively the technique wheel, divine sense is a separate key."**: User corrected me again. The two systems are distinct:
    - V key = Divine Sense (tap = snapshot pulse, hold = continuous active sense)
    - G key = Technique Wheel (hold → category → sub-technique → release → cast)
    User also provided:
    - The universal "Can I move this?" comparison equation
    - The three manipulation types (Physical/Spiritual/Dao) from Er Gen examples
    - **Warning against hardcoding realms** — the equation handles it
    - Asked me to engineer suggestions for the shape system

30. **(DIVINE-SENSE-PHYSICAL-PLUS-HITBOXES-PLUS-FORMATIONS, worklog line 6682) Three more corrections**:
    1. **Divine sense should play a bigger part in physical manipulation** — search the wiki. Found: Ji Realm Divine Sense IS a weapon, Restriction Flags are wielded WITH divine sense, Mountains Crumble FORMS physical mass from divine sense. CORRECTED formula: divine sense now has EQUAL weight to telekinetic force (was 10%).
    2. **Attack hitboxes must match the animation orientation** — "if I'm doing a slashing move that has sword effects that goes in a horizontal way, the thing being cut isn't going to be cut vertically." Built VoxelOrientation enum.
    3. **Explain the formation and talisman implementation options** — wrote DESIGN_HITBOXES_AND_FORMATIONS.md, recommended HYBRID (blocks for sect-scale, items for personal/portable).

---

# Missing/Incomplete Systems

### Systems mentioned but never built
- **Spiritual Restraint** (SYSTEMS_INVENTORY §14, user priority 5: "Necessary once companions matter") — designed, not built
- **Fortuitous Rebound** (SYSTEMS_INVENTORY §13, user priority 6: "Very important, but after the world systems") — designed, not built
- **Sect-founding** (player as faction head) — repeatedly flagged as next-phase priority across Tasks 4, 5, 6, 7, 8 but never built
- **NPC status infliction** (player inflicting soul_fracture/heart_demon on NPCs) — flagged as unresolved across Tasks 7, 8, 9 but never built
- **Soul-eating** (actively raise S_tempering by consuming beast cores / slain cultivator souls) — flagged as unresolved across Tasks 7, 8, 9, 10 but never built
- **Vast Expanse travel** (actually enter the void with void-friction active) — flagged as unresolved across Tasks 8, 9, 10 but never built

### Systems built but with gaps the user flagged
- **Bai Xiaochun arsenal**: only 1 of 10 Spirit Stream secret magics encoded (Waterswamp Kingdom); 9 missing — flagged in GAP-FILLING-COMPLETE, then fixed
- **Xu Qing arsenal**: only 6 of 12-13 Heavenly Palaces encoded initially — fixed in GAP-FILLING-COMPLETE
- **Codex structured data**: interfaces defined but not populated initially — fixed in Task 21
- **Berserker World / AWWP Federation / BTT cosmology**: qualitative framings PARTIALLY CONFIRMED but flagged MEDIUM confidence due to rate-limiting

### Systems the user requested that we haven't started (Forge port)
- **TechniqueWheel Screen** (Forge `Screen` subclass) — G key radial UI
- **FormationFlagBlock** (Forge `Block` subclass) — anchors formation nodes in Spiritual Layer
- **FormationCoreBlock** — heart of multi-block formation
- **Talisman item classes** — paper, jade slip, soul banner, sealing stamp
- **Spirit vein world-gen** — generate spirit veins as Spiritual Layer features
- **First beast AI** — wire behaviorScript to Forge entity AI
- **Forge event handlers** — capability registration, world-gen integration
- **All 91 unported systems listed above**

---

# Recommended Port Priority

Based on the audit, here is the recommended port order (extends SYSTEMS_INVENTORY.md's existing priority list, items 6+):

### Already ported (✅ — 14 files)
1. ✅ Forge MDK Scaffold
2. ✅ WorldPhilosophy (Prime Directive)
3. ✅ RealmId (17 realms)
4. ✅ CanonEngine (0-5 confidence)
5. ✅ CosmologicalTree (5 of 19 nodes — rest stubbed)
6. ✅ WorldLaws (6 canon locations)
7. ✅ WorldLayer (3-layer model)
8. ✅ ChunkLayerData (per-chunk storage)
9. ✅ PerceptionEngine (corrected — understanding-over-existence)
10. ✅ ConcealmentFormation (spiritual-anchored)
11. ✅ DivineSense (Soul Power hybrid, logarithmic radius, snapshot pulse, NPC confrontation, camouflage)
12. ✅ VoxelFactorization (F_destruct vs R_voxel)
13. ✅ VoxelOrientation (hitbox matches animation)
14. ✅ HeavenAndEarthManipulation (3 manipulation types, WorldObject, ManipulationCapability, SelectionShape)
15. ✅ CausalEcology (core trophic model)
16. ✅ Formation (definition + 9 canon formations)
17. ✅ PerceptionDemo (proves the rabbit example)
18. ✅ Ergenverse (mod entry point)

### Next priority (Tier 1 — foundation)
19. **TechniqueWheel Screen** (G key radial UI) — required for everything else
20. **Soul System** (13-D matrix, archetypes) — drives NPC confrontation
21. **Status Matrix** (6 statuses with cures) — referenced by DivineSense
22. **Karma Backlash Engine** — drives tribulation amplification

### Tier 2 — core Er Gen depth
23. **Karmic Threads** (7 lineages) — drives hidden inheritance reveals
24. **Dao Heart** + Emotional Breakthroughs — Er Gen signature
25. **Cultivation Arts** (17 arts) — drives Qi gathering speed
26. **Tribulation System** (6 types by realm) — drives breakthroughs
27. **Ancient Clans** (body cultivation parallel) — Wang Lin's hybrid path
28. **Sealing Arts** — Meng Hao's Dao
29. **Avatars / Soul Splitting** — Nascent Soul+
30. **Cave-World** — personal pocket dimension
31. **Reincarnation / Past Lives** — past-life memory cultivation resource

### Tier 3 — world simulation
32. **Ecology Engine** (1382 lines, 5 zones × 6 branch profiles) — full causal ecology
33. **History Engine** (1274 lines, 7-phase lifecycle) — every ruin has a reason
34. **Cosmos System** (nested cosmology, vortexes, River of Time, dimension fold, opportunities, cosmic ripples)
35. **Reality Profile** (full field set — Qi density, max realm, dao affinity, voxel reality strength)
36. **Absolute Power Threshold** (S_eff formula)
37. **Local Antagonist** (per-world Heaven-Will)
38. **World Exit Framework** (karmic completion)
39. **Dimensional Mana Pool** (finite, drainable)

### Tier 4 — protagonist systems
40. **3-Tier LOD Simulation** (Mythic/Regional/Reality)
41. **Protagonist Manifestation** (clones with canonical arsenals)
42. **Sovereign Reconstitution** (clones can't be permakilled)
43. **Bond Events** (5-7 events raise recognition)
44. **Canonical Arsenal** (811 items, unlock thresholds)
45. **Personality-Driven Supply**
46. **Protagonist Dialogue + Memory**
47. **Karmic Mirror** (non-destructive inheritance)

### Tier 5 — items & crafting
48. **Bestiary Catalog** (131 real Er Gen beasts)
49. **Weapon Catalog** (32 weapons)
50. **Pill Catalog** (87 pills)
51. **Spirit Herbs Catalog** (165 herbs/materials)
52. **Alchemy System** (property-based brewing)
53. **Pill Tribulation**
54. **Artifact System** (weapons/defense/utility/sentient)
55. **Sentient Artifact Bonding/Mounting/Overclocking/Soul Brand Cracking**
56. **Beast Taming** (universal — not Pokémon)
57. **Beast Lifecycle** (feral → spiritual → mythical)
58. **FormationFlagBlock** (Forge Block subclass)
59. **FormationCoreBlock**
60. **Talisman item classes** (paper, jade slip, soul banner, etc.)

### Tier 6 — flight & combat
61. **Flight Profile** (ambient absorption vs drain)
62. **Sprint Burst**
63. **Tribulation Parasitism** (absorb/devour storms)
64. **Body Tempering** (from storms)
65. **Mounted Flight**
66. **Dao Companion Bonding**
67. **Combat Intent Recognition**

### Tier 7 — codex & data
68. **Codex** (138 entries, namespaced IDs, 3 layers, structured data)
69. **NPC Catalog** (104 NPCs)
70. **Sect Catalog** (47 sects)
71. **Location Catalog** (77 locations)
72. **Cultivation Arts Catalog** (17 arts)
73. **Formation & Talisman Catalog** (311 documented; 100 encoded)

### Tier 8 — finishing
74. **Spiritual Restraint** (designed, not built — user priority 5)
75. **Fortuitous Rebound** (designed, not built — user priority 6)
76. **Sect-founding** (player as faction head)
77. **NPC status infliction** (player inflicting statuses on NPCs)
78. **Soul-eating** (actively raise S_tempering)
79. **Vast Expanse travel** (actually enter the void)

---

*End of audit. Total: 105 systems catalogued, 18 ported to Java, 87 not yet ported, 30 user corrections logged.*
