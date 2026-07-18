# Ergenverse — Complete Systems Inventory

This document catalogues EVERY system we have built or designed, across both the Next.js reference implementation and the Java Forge mod port. It exists so no system is forgotten during the port.

**Rule:** Before writing any new Java system, check this document. If a system already exists in the Next.js version, port it faithfully — do not reinvent it.

---

## 1. Core Simulation (engine.ts — 3888 lines)

### Player actions
- `cultivate(days)` — Qi gain over time
- `attemptBreakthrough()` — realm advancement with tribulation
- `forgeDao(name, element, baseDao)` — create your own Dao (Foundation+)
- `generateTechnique(opts)` — procedural technique generation
- `seekInsight()` — random insight events
- `castTechnique(id)` — technique casting with NPC reactions
- `hunt(speciesId?)` — beast hunting
- `trade(action, resource, qty)` — resource trading with price movement
- `travelToWorld(worldId)` — cosmological travel
- `engageOpportunityAction(oppId)` / `declineOpportunityAction(oppId)` — random opportunities
- `brewPillAction(ingredients)` — alchemy
- `consumeElixir(target)` — status curing
- `toggleQiSenseAction()` — Qi sense toggle

### Divine Sense & Perception (THE CORRECTED SYSTEM)
- `divineSensePulseAction()` — **SNAPSHOT PULSE, button-triggered**. Returns `SensePulseResult` (radius, reactions, perceived objects, soul-fracture risk). NOT continuous.
- `soulSearch(npcId)` — forbidden art, steals technique, high karma cost, heart-demon risk, soul-fracture on failure
- `activateKarmaVisionAction()` — **SNAPSHOT PULSE, 20-year cooldown**, brief active window, auto-deactivates
- `traceKarmicThreadAction(threadId)` — trace karmic lineages
- `markBloodlineMemoryExtracted(npcId)` — bloodline memory extraction
- `getPerceptionOfLocation(description, realm, hiddenTruth)` — layered location perception

### Karmic & Social
- `recordKarmicMemory(...)` — persistent NPC memories
- `getMemoriesAboutPlayer(npcId)` — memory retrieval
- `classifyCombatIntent(...)` — intent recognition (accident vs betrayal vs duel)
- `protectCommandAction(allyId)` — ally protection
- `bondingCeremonyAction(allyId)` — dao companion bonding
- `karmicSeveranceAction(threadId)` — sever karmic threads

### Artifact & Beast
- `attemptBondArtifact(artifactId)` — sentient artifact bonding
- `mountArtifactAction(artifactId)` — artifact mounting with spiritual weight
- `bloodRefinementBindingAction(artifactId)` — blood refinement
- `spiritStoneOverclockAction(artifactId, stoneCount)` — artifact overclocking
- `crackSoulBrandAction(artifactId, method)` — soul brand cracking (multi-layer)
- `attemptTame(speciesId?, useArtifact)` — beast taming
- `offerPillToBeast(beastId)` — beast pill offering

### World & Faction
- `expandRegion(hint?)` — on-demand region expansion
- `tick(years)` — the world tick
- `attemptClaimInheritance(inheritanceId)` — inheritance vaults
- `influenceWar(warId, side)` — faction wars
- `recruitAlly(npcId)` — NPC recruitment
- `checkAlliesLoyalty()` — ally stability check
- `checkWorldExitEligibility()` / `attemptWorldExitAction(exitId)` — world exit framework
- `tickDimensionalManaPool(years)` — finite world energy drain

### Er Gen Depth (ergen-depth.ts — 447 lines)
- `kindleStarAction(clan)` — Ancient God/Devil/Demon parallel cultivation
- `splitSoulAction()` — soul-splitting for avatars (Nascent Soul+)
- `developCaveWorldAction(investment)` — personal pocket dimension
- `sealAction(...)` — sealing arts (beast/npc/technique/array/space/time)
- `switchArtAction(artId)` — cultivation art switching
- `testDaoHeartAction()` — Dao Heart trials
- `emotionalBreakthroughAction(emotion)` — emotional breakthroughs (grief/love/rage/enlightenment/despair)
- `toggleKarmicThreads()` — karma visibility

### Flight & Tribulation Parasitism
- `computeFlightProfile()` — flight snapshot (ambient absorption vs drain, cruise/sprint)
- `cruiseAction(distance)` — sustained flight
- `sprintBurstAction()` — sprint burst
- `enterStormAction(tribulationId, mode)` — tribulation parasitism (absorb/devour)
- `surviveStormRoundAction()` — storm round survival
- `leaveStormAction()` — exit storm
- `respectTribulationWishAction(tribulationId)` — respect tribulation wish (bonding)

### Protagonist Systems (3-Tier LOD)
- `createProtagonistManifestation(...)` — protagonist clones
- `recordBondEvent(...)` — protagonist bond events (5-7 events raise recognition)
- `mirrorCanonicalItemAction(protagonistId, itemId)` — inherit protagonist items
- `getProtagonistSimulationTier(protagonistId)` — Tier 0/1/2 LOD
- `tickMythicSimulation(protagonistId, years, rng)` — Tier 0 background sim
- `manifestProtagonistAction(protagonistId)` — manifest a protagonist
- `sovereignReconstitutionAction(...)` — protagonist reconstitution
- `personalityDrivenSupplyAction(protagonistId)` — protagonist supply
- `getCanonicalArsenal(protagonistId)` — protagonist arsenals (811 items)
- `getProtagonistDialogueContext(protagonistId)` — protagonist dialogue
- `checkProtagonistAwakening()` — true body awakening (frozen → awakened)
- `absorbCloneAction(protagonistId)` / `keepCloneIndependentAction(protagonistId)` — clone management

---

## 2. Divine Sense System (divine.ts — THE SYSTEM I FORGOT)

### Soul Power (hybrid model)
```
S_sense = S_realm × (1 + ΣM_manual) + S_tempering
```
- S_realm baseline: mortal=1, Qi=10, Foundation=100, Core=500, Nascent Soul=10000, Soul Formation=30000, Ascendant=80000, Immortal=1000000, Transcendence=100000000
- ΣM_manual = sum of soul-cultivation manual multipliers
- S_tempering = soul tempering (dark-arts), soft-capped at S_realm × (realm_order+1)
- **This is the core anti-lag mechanism**: S_sense grows logarithmically in effect, not linearly

### Divine Sense Radius (logarithmic — prevents lag)
```
R_sense = R_base × ln(S_sense + 1) × (1 − L_world/12) × Π(1 − σ_seal)
```
- R_base = 16 blocks (default; raised by movement/divine-sense arts)
- L_world = world law tier (0-12); higher law = smaller radius
- σ_seal = local sealing-formation suppression (0..1 each, multiplied)
- **Result**: radius stays in 50-150 block range across ALL tiers. No lag.

### Divine Sense Pulse (SNAPSHOT — button-triggered)
- `divineSensePulseAction(world)` returns `SensePulseResult`:
  - `radius` — computed from formula above
  - `sSense` — the player's current soul power
  - `reactions` — NPC confrontation results (see below)
  - `perceivedObjects` — concealed objects whose camouflage ≤ sSense
  - `soulFractureInflicted` — whether a counter-strike fractured the player's soul
- **NOT continuous. One snapshot, then done.**

### NPC Confrontation (ΔS = S_player − S_npc)
Three scenarios:
1. **Arrogant Interception** (ΔS < 0, NPC stronger):
   - If NPC `faceObsession >= 0.7`: 60% chance of mental counter-strike → Soul Fracture risk (0.5)
   - Otherwise: NPC detects scan, glowers, dismisses
2. **Absolute Spiritual Suppression** (ΔS ≥ 5000):
   - If NPC `mortalityPanic >= 0.6` or `shamelessness >= 0.6`: NPC drops to knees, begs
   - Hidden trade tabs unlock; taming requirements bypassed for beasts
3. **Unmasking the Hypocrite** (righteous_hypocrite archetype, ΔS ≥ 2000):
   - False aura of altruism/honor shatters
   - True soul matrix revealed: max ambition, near-zero honor
- Plus: ally_alerted (20% chance if ΔS < 5000), unnoticed (default)

### Spiritual Camouflage Registry
Concealed objects with camouflage values (C). Player's S_sense must exceed C to perceive:
- inheritance_vault: C=25000
- ancient_formation: C=20000
- sealed_pavilion: C=15000
- trap_array: C=8000
- spirit_vein: C=500
- hidden_cave: C=200

### Xianxia Status Matrix
6 statuses with cure elixirs:
- `soul_fracture` — blinds divine sense, grounds flight. Cure: Soul-Mending Elixir
- `sealed_meridians` — locks % of max Qi. Cure: Meridian-Clearing Powder
- `qi_deviation` — casting drains health. Cure: Purification Pill
- `heart_demon` — raises breakthrough failure. Cure: Heart-Calming Incense
- `karmic_stain` — reputation marker, blacklisted. Cure: Nirvana cleansing
- `meat_jelly` (Lord Third) — invincible nagging companion. Cure: reduce karmicDebt below atrocious

### Karma System
- `karmicTier(debt)` → pure / light / tainted / heavy / atrocious
- `tribulationKarmaMultiplier(debt)` — karma amplifies tribulations
- `isBlacklisted(world)` — righteous sects blacklist you at atrocious karma

---

## 3. Soul System (soul.ts — 200 lines)

- **SoulMatrix** — 13-dimensional personality model (ethical/social/comedic groups)
- **ArchetypeId** — personality archetypes (righteous_hypocrite, face_obsessed, shameless, etc.)
- **voxelFactorize** — voxel geometry for techniques (block operators, scale)
- **psychologicalCascade** — psychological reaction cascade
- **computeAllyStability** — ally loyalty computation
- **resolveAllyBetrayalCheck** → loyal / desert / betray

---

## 4. Cosmos System (cosmos.ts — 223 lines)

- **CosmologyLayer** — 5-layer nested hierarchy (singular → true → cosmos → vast expanse → universe)
- **VastExpanseVortex** — 5 vortex kinds (spatial_tear, dead_star, floating_ruin, gravity_well, river_of_time_eddy)
- **RiverOfTimeEcho** — 4 echo kinds (item_blueprint, technique_fragment, phantom_npc, historical_record) — pullable by karma + divine sense
- **DimensionFoldState** — 3D/4D/5D spatial dimensions (fold/unfold)
- **Opportunity** — 19 kinds of random opportunities
- **CosmicRipple** — universe-scale crossover events

---

## 5. Canon Engine (canon-engine.ts — ported to Java ✓)

- CanonConfidence (0-5): 5=novel statement, 4=wiki-backed, 3=implication, 2=community, 1=speculation, 0=filler
- RealityLevel: reality / tradition / rumor / legend / unknown / forbidden
- canonFilter(confidence, regionStatus) — Level 0 FORBIDDEN in known canon regions
- Layered perception registry: 7 locations × 7 tiers = 49 perception entries

---

## 6. Cosmological Tree (cosmological-tree.ts — ported to Java ✓)

- 19 nodes across 7 branches (immortal_astral, vast_expanse, eternal_lands, arid_triad, starry_cosmos, wanggu_star_rings, root_dao)
- Travel rules: within-branch (realm-gated), cross-branch (4th Step + karmic anchor)
- 9 travel methods (walk → void_ship)
- RegionStatus: known_canon / partially_known / unknown / frontier
- DiscoveryTier: minimum perception tier to learn a node exists

---

## 7. Ecology Engine (ecology-engine.ts — 1382 lines, needs Java port)

- 5 zones: sky / ground / ocean / underground / forbidden_zones
- Each zone has multiple depth layers
- 6 branch-specific ecology profiles (inherits from cosmology)
- Dao modifiers, ceiling filters, history modifiers
- Ecology pressure system (survivability check)

---

## 8. History Engine (history-engine.ts — 1274 lines, needs Java port)

- 7-phase lifecycle: birth → rise → golden_age → decline → twilight → current → possible_future
- 6 branch-specific history templates
- Every faction and location has a temporal lifecycle
- Possible futures with probability
- advanceHistory() — phase shifts over time

---

## 9. World Laws (ported to Java ✓)

- Origin reasons (ANCIENT_BATTLE, SEALED_BEAST, FALLEN_IMMORTAL, etc.)
- Dao affinities (dominant/suppressed)
- Space stability, lightning suppression
- 6 canon locations registered

---

## 10. Three-Layer World Model (ported to Java ✓)

- PHYSICAL layer — vanilla Minecraft
- SPIRITUAL layer — spirit veins, formations, spirit herbs (mortals can't touch)
- DAO layer — karmic traces, Dao imprints, heavenly laws
- ChunkLayerData — per-chunk storage

---

## 11. Corrected Perception Engine (ported to Java ✓)

- Objective interface — things exist independently of observers
- ObjectiveNature — observer-independent truth
- ObserverContext — realm, daoComprehension, divineSenseActive, specializations
- PerceptionResult — what the observer UNDERSTANDS (not what changed)
- ConcealmentFormation — anchored in spiritual layer, mortals can't break
- **The rabbit example**: same rabbit, different understanding at different tiers

---

## 12. Data Catalogs (need Java port)

- **protagonist-arsenals.ts** — 811 canonical items across 6 protagonists
- **bestiary-catalog.ts** — 131 beasts
- **alchemy.ts** — 67 ingredients
- **weapon-catalog.ts** — 32 weapons
- **pill-catalog.ts** — 87 pills
- **npc-catalog.ts** — 104 NPCs
- **sect-catalog.ts** — 47 sects
- **location-catalog.ts** — 75 locations
- **formation-talisman-catalog.ts** — 100 entries

---

## 13. Fortuitous Rebound (designed, not yet built)

Per user priority 6: "Very important, but after the world systems."
- Not yet implemented in either version.

---

## 14. Spiritual Restraint (designed, not yet built)

Per user priority 5: "Necessary once companions matter."
- Not yet implemented in either version.

---

## PORT PRIORITY ORDER

When porting to Java, follow this order to preserve system dependencies:

1. ✅ Canon Engine (foundation)
2. ✅ Cosmological Tree (foundation)
3. ✅ World Laws (foundation)
4. ✅ Three-Layer World Model (foundation)
5. ✅ Corrected Perception Engine (foundation)
6. **NEXT: Divine Sense System** (Soul Power, logarithmic radius, pulse, NPC confrontation, camouflage registry, status matrix)
7. Soul System (13-D matrix, archetypes)
8. Er Gen Depth (karmic threads, ancient clans, Dao heart, cultivation arts, tribulations, avatars, cave world, past lives, sealing, restricted domains)
9. Cosmos System (cosmology layers, vast expanse vortexes, river of time, dimension fold, opportunities, cosmic ripples)
10. Ecology Engine (5 zones, 6 branch profiles)
11. History Engine (7-phase lifecycle)
12. Data Catalogs (811+ items, 131 beasts, 87 pills, etc.)
13. Protagonist Systems (3-tier LOD, bond events, true bodies, arsenals)
14. Flight & Tribulation Parasitism
15. Alchemy, artifact bonding, beast taming
16. Spiritual Restraint (new)
17. Fortuitous Rebound (new)
