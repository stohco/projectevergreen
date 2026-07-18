# Unfinished Canon Research — Complete Gap List

> The z-ai API (web_search, web_reader, chat, vision) has been rate-limited (HTTP 429) for the entire session. All research below was attempted but could not be completed. This document tracks every known gap so it can be closed when the API recovers.

## Status: API RECOVERED (2026-07-12, RI-FORGE-CANON-RESEARCH run)
- web_search now returns valid results. 25 mandated + 4 supplementary searches = 29 queries run with **ZERO 429 errors**.
- web_reader / chat / vision endpoints NOT re-tested this run (focus was on web_search verification of existing canon).
- See `CANON_RESEARCH_REPORT.md` for the full canon-completeness report.
- NOTE: User has directed that the 8 cross-novel wiki searches are no longer relevant to the RI canon reconstruction. They are retained here for archival purposes but will NOT be re-attempted. Focus is now on RI-internal canon reconstruction only.

## MAJOR PIVOT (2026-07-12): Next.js retired, full focus on Forge mod
- The Next.js web app has been RETIRED from active development. The old web-focused cron job (264618) was deleted; a new Forge-mod-focused cron (265875) replaces it.
- All canon data was already extracted to forge-mod/ in prior sessions. Confirmed: no remaining canon data in Next.js src/ that isn't already portable (src/lib/sim/ is TypeScript design reference, not canon data; the canon docs + JSONs in forge-mod/ are the source of truth).
- User directive: "full focus on delivering mod not only to a playable state, but having all the content of the renegade immortal starting point from start until the immortal astral continent."
- User directive: "make sure there's access to ALL the arsenal of wanglin to be able to copy, all the hundreds of items, pets, techniques/etc dont miss a single item he's encountered/had, they all need to be in the game."
- User directive: "don't lock upgrades, there must always be a way to get it to the most upgraded state."

### Gap CLOSED this run (RI-FORGE-arsenal-registration):
- **Wang Lin arsenal manifest extracted**: 309 entries (every item/technique/pet/clone/companion) pulled from Next.js protagonist-arsenals.ts → forge-mod/wanglin_arsenal_manifest.json + mod resource.
- **Forge item registration built**: WangLinItems.java (DeferredRegister<Item>) registers ALL 309 arsenal items as actual Minecraft items. Previously ZERO items were registered — the mod was a data library, not a playable mod.
- **Always-upgradeable evolution system built**: ItemEvolutionChain.java + ItemEvolutionRegistry.java. ~60 explicit chains + ~250 default chains = all 309 items fully walkable from base to peak. validateNoDeadEnds() enforces the no-locked-upgrades directive at construction time.
- **Creative tab + command access**: WangLinCreativeTab + /wanglin arsenal grant/list/grantall/max/stage. Full access to all arsenal items in creative mode.
- **Files**: WangLinItem.java, ItemEvolutionChain.java, ItemEvolutionRegistry.java, WangLinItems.java, WangLinArsenalCommand.java, lang/en_us.json, wanglin_arsenal_manifest.json (resource).

### Remaining Forge mod gaps (next priorities):
- [x] ~~Item models + textures~~ — FULLY RESOLVED: 321 model JSONs, 310 unique texture PNGs (437-631 bytes each, no longer _placeholder.png). All models reference proper texture names. Gap closed in prior sessions.
- [~] Wang Lin dimension world-gen (Planet Suzaku → pocket dimensions → Cave World → Immortal Astral Continent) — MOSTLY RESOLVED. Planet Suzaku surface: 14 biomes + 7 spirit herb configured_features + 8 placed_features + 7 structures + 4 template pools + 7 structure_sets (RI-FORGE-PLANET-SUZAKU-WORLDGEN + RI-FORGE-SPIRIT-HERBS-STRUCTURES). Pocket dimensions COMPLETE (RI-FORGE-POCKET-DIMENSIONS): Land of the Ancient God (3 biomes via multi_noise, nether noise), Suzaku Tomb (1 biome, end noise), Immortal Graveyard (1 biome, nether noise) — 5 biomes + 3 dimension_types + 3 dimensions. Total worldgen: 67 data JSONs. Build compiles. REMAINING: (a) custom NBT structure templates to replace vanilla placeholder pools; (b) custom blocks + textures for spirit herbs; (c) Foreign Battleground dimension (accessed via Jue Ming Valley tokens); (d) Cave World + Immortal Astral Continent dimensions (later phases); (e) teleportation/rift access code for pocket dimensions (Java).
- [x] ~~Compile verification~~ — RESOLVED: `./gradlew build` → BUILD SUCCESSFUL. Target is MC 1.20.1 / Forge 47.4.0 / Java 17 (pivoted from MC 26.2/Forge 65 for API stability + ecosystem maturity). Only pre-existing deprecation warning on ModLoadingContext.get().
- [x] ~~Cultivation realm system as Forge capabilities~~ — RESOLVED: CultivationState (728 lines), CultivationCapability (165 lines), CultivationEvents (787 lines, +visual effects), BreakthroughResult (91 lines). Event-based breakthrough with tribulation + realm-tiered particle effects (tribulation: ELECTRIC_SPARK/END_ROD/CLOUD; breakthrough: FLAME/LAVA for low realms, ENCHANT/DRAGON_BREATH for mid, REVERSE_PORTAL/END_ROD/NAUTILUS for high). Wired into Ergenverse.java.
- [x] ~~Perception system~~ — FULLY RESOLVED (v4): v3 had PerceptionEngine (735 lines) + PerceptionTier (7 tiers) + DivineSense (527 lines) + ObjectiveNature + ObserverContext + PerceptionResult + ConcealmentFormation + PerceptionBridge + CultivationClientEvents + EntityCultivatorRenderer + V-key divine sense pulse + /ergen perceive command + server-side PerceptionEvents sync + PerceptionSyncS2CPacket + AmbientPerception (blocks) + PerceptionBlockEvents + item tooltip perception. v4 ADDITIONS (RI-FORGE-PERCEPTION-CANON-ENRICHMENT): (a) PerceptionEvents.buildEnrichedNatureFor() now loads canon NPC JSON data from data/ergenverse/npcs/&lt;id&gt;.json — extracts faction→sect, cultivation→RealmId (with half-step parsing), personality→origin, relationship_to_wanglin→karmicHistory, dao_heart→daoAffinities (threshold ≥50), type→titles; (b) Canon perception_tiers override: if the NPC JSON has a "perception_tiers" object, the system uses the canon-accurate description for the observer's tier instead of the generic PerceptionEngine text; (c) DivineSensePulseC2SPacket now reuses buildEnrichedNatureFor() for divine sense pulses (same rich data as periodic sync); (d) Cultivation string parser handles non-standard realm names ("Half-Step Deity Transformation", "Soul Transformation", "Golden Core", etc.). REMAINING v5 TODO: actual loot table replacement (Foundation+ gets spirit herb item instead of vanilla drop), shader-based aura rendering (v2 scaffold ready).
- [x] ~~Manifestation Gift system NPC~~ — FULLY RESOLVED: ManifestationGiftSystem.java (1011 lines, 5 protagonist profiles, 18 gift records, four-question evaluation engine). ManifestationGiftHandler.java — server-side right-click interaction with manifestation NPC, evaluates gifts via PlayerStateSnapshot, grants items via ForgeRegistries.ITEMS lookup, 5-min cooldown. ManifestationGiftCommand.java — /ergen gift [list|request|evaluate|lore] command. NPC JSON: npc_wang_lin_manifestation.json (manifestation_companion, perception tiers, dao_heart profile). Build: GREEN. REMAINING: persistent affinity tracking (PlayerStateSnapshot v1 uses hardcoded values).
- [x] ~~Expand explicit ItemEvolutionChains for remaining ~250 items~~ — RESOLVED by three-layer rewrite: items without documented canon transformations now correctly have NO chain (single canonical state). No invented chains.
- [x] ~~Samsara Dao, Joss Flame Economy, Cave World Ownership, Realm-Sealing Grand Array as gameplay mechanics~~ — RESOLVED (RI-FORGE-ADVANCED-MECHANICS): Backend data models already existed (SamsaraDao 253 lines, JossFlameEconomy 223 lines, CaveWorldOwnership 225 lines, RealmSealingGrandArray 178 lines). v1 gameplay integration added: (a) AdvancedMechanicsEvents.java (386 lines) — Joss Flame daily harvest loop (Loop E in Ergenverse.onServerTick), cultivation ceiling enforcement on breakthrough (CaveWorldOwnership + RealmSealingGrandArray + Heaven-Defying Bead check), perception-based Joss Flame reveal (Soul Formation+) and ownership loop reveal (Nirvana Scryer+), Cave World status messages (Nascent Soul+); (b) AdvancedMechanicsCommand.java (223 lines) — /ergen advanced [joss|cave|seal|essence|samsara|comprehend|dissolve|transfer] commands with debug ops; (c) CultivationState extended with 14-Essence tracking (boolean[] + NBT persistence) + hasHeavenDefyingBead() inventory check; (d) CultivationEvents.attemptBreakthrough() now checks ceiling BEFORE tribulation; (e) completeTribulationSuccess() now triggers Joss Flame revelation on realm-up. REMAINING v2 items: Joss Flame source auto-registration during structure worldgen — RESOLVED (14 biomes); Realm-Sealing anchor block items — RESOLVED (registered + textures + loot tables); Samsara Incarnation gameplay — RESOLVED (/ergen advanced samsara avatar summon|merge|status); ownership transfer via NPC combat — RESOLVED (EntityCultivator.die hook). REMAINING v3: Joss Flame item/block drops, NPC dialogue-driven history, ecology shift recording, custom NBT structure templates, shader-based aura rendering. RESOLVED THIS RUN: Cave World + IAC dimension structures (4 IAC + 3 Foreign Battleground structures added), Cave World ownership persistence (WorldRuntimeState save/load), dimension detection for Cave World + Foreign Battleground.
- [x] ~~Layer 3 (Emergent History) implementation~~ — RESOLVED (RI-FORGE-EMERGENT-HISTORY): All 4 data classes (PlayerHistory, WorldHistory, NpcMemory, RelationshipHistory) had full NBT serialization and were already wired for 2 event types (breakthrough, gift received). v1 completion added: (a) WorldHistory singleton invalidation fix + tickWorldHistory() daily checkpoint + seed-on-first-load persistence; (b) NPC interaction hook via Forge PlayerInteractEvent (Mob.interact() is final in MC 1.20.1, so HistoryEvents.java listens for EntityInteract and delegates to EntityCultivator.recordPlayerInteraction()); (c) NPC death hook in EntityCultivator.die() — records combat in NpcMemory + PlayerHistory + WorldHistory; (d) Perception tier shift discovery hook in CultivationEvents; (e) HistoryCommand.java — /ergen history [player|world|npc|relationship|stats] with color-coded output, type filtering, region filtering, and system statistics; (f) HistoryEvents.java — Forge event listener for entity interactions; (g) package-info.java updated from "STUBBED" to "v1 COMPLETE". 5 active wiring points: breakthrough, perception shift, gift received, NPC interaction, NPC death. REMAINING v2: NPC dialogue-driven history, ecology shift recording, faction succession tracking, RITimelineEngine integration for canon-consequence advancement.
- [x] ~~Lore rule for one-time consumed inheritances~~ — RESOLVED: Opportunity Classification system (Layer 2) built. Six categories (Transferable, Replicable, Successor, Parallel, Relationship Exclusive, Absolute Unique) + Opportunity Acquisition Policy ("never rewrite canonical ownership") + Protagonist Sharing Philosophies (5 canon protagonists). The "protagonist fantasy" and "canon faithfulness" now coexist.

### Structural gaps closed since last session:
- **BridgingPolicy.java expanded**: 43 → 113 policy entries. All major factions (15), techniques (15), items (12), ecology systems (7), civilization systems (8), world mechanics (8), and forbidden guards (5 new) now have explicit policy entries. The default-fallback-to-SPECULATION problem for these systems is resolved.
- **Timeline UI integrated**: The Cosmology panel now renders the full 108-event TimelineDetailPanel (with titleCn, participants, remainingTraces, canonConfidence) instead of the previous 39-event simplified inline version. All 7 priority items (a-g) are now complete.
- **Canon Database full load**: CanonDatabaseBrowser now loads all 630 entries from ri_canon_database.json (was 55 inline). Every named entity is searchable in the UI.
- **Civilization card completionist**: All 45 canon-attested factions now rendered in the UI (was 18 detailed-only). 27 brief factions added from CANON_RI_CIVILIZATION.md Part 3 with proper confidence tags (A/B/C), type icons, and alignment colors.
- **RICanonicalDatabase.java**: NEW — 8,088 lines. Provides typed Java access to all 630 canon entries (158 chars, 80 locs, 178 artifacts, 214 techniques) with 30+ query methods. Previously ZERO of these were queryable from Java.
- **WangLinAntagonists.java expanded**: 2 → 6 antagonists (153 → 257 lines). Added LOCAL_THREAT layer + 4 mid-tier antagonists (Teng Huayuan, Yao Xixue, Daoist Water, Yao Xinghai/Blood Ancestor). 3-layer threat model.
- **WangLinCosmologyRegistry.java updated**: 109 → 118 lines. Registers RICanonicalDatabase (section 12), updated antagonist logging, fixed stale 39→108 event counts.
- **CANON_RI_CHARACTER_DECISIONS.md**: NEW — 1,952 lines. 32 decision-making NPC profiles with full goal-based planner data (Goals/Needs/Resources/Fears/Knowledge/Relationships/Trigger Conditions/Known Actions/Decision Style). Organized into 7 parts: cosmic-tier, mortal-scheme, local-threat, mentors, allies, faction leaders, reincarnation-linked. Includes edge-of-canon world state (the RI dimension's state when the player enters — all canonical antagonists dead, seal dissolved, Wang Lin is Cave World owner). Feeds the future NPC decision engine (build-phase 4).
- **CANON_RI_EDGE_OF_CANON.md**: NEW — 1,206 lines. The "State of the World at the Edge of Canon" document for Renegade Immortal (doc 1 of 6 in the planned series). EMBODIES THE USER'S CRITICAL REFRAME: "post-Wang-Lin world" → "Wang Lin's branch at the edge of recorded canon." Wang Lin EXISTS (true body on IAC, manifestation travels with player). Threats are NOT replacement NPCs — they are the universe itself (9 threat categories). Contains: 30 Historical Consequence chains (Event → Immediate → 100yr → 1000yr → Current → Player implication), 14 Inheritance Records (12-question classification + protagonist access path for EVERY inheritance per the "protagonist finds a way" directive), 6 Decision Horizons (MINUTES → MILLENNIA), the Manifestation Companion mechanic (13 location-triggered comments), faction states (alive/gone/transformed), figure states (9 alive, 14 dead — do NOT resurrect), legends-become-myths, scars, and opportunities. This is the foundational framing document for the RI branch.
- **RIEdgeOfCanonState.java**: NEW — 1,202 lines. The Java runtime encoding of CANON_RI_EDGE_OF_CANON.md. Makes the edge-of-canon state queryable from the mod: ThreatCategory enum (9), ManifestationCompanion inner class (Emotion enum, ManifestationComment record, 13 registered comments, getCommentFor(locationId, playerTier) tier-gated lookup, isQuestGiver/isBodyguard/isPermanent design-rule assertions), ConsequenceChain record + 30 static instances, InheritanceRecord record (12 fields + protagonistAccessPath) + 14 static instances + getAccessibleInheritances(), DecisionHorizon enum (6 horizons) + playerDecisionHorizon(tier), FactionState/FigureState records + 21 faction states + 9 alive + 14 dead figures + isFigureAlive(id). Registered as section 13 in WangLinCosmologyRegistry.
- **"Post-Wang-Lin" framing corrected**: The incorrect "post-Wang-Lin world" language in CANON_RI_CHARACTER_DECISIONS.md (line 1940 and line 1938) has been rewritten to reflect the correct "edge of recorded canon" framing and the "protagonist finds a way" inheritance directive.

### Gap CLOSED this run (RI-FORGE-three-layer-rewrite):
- **THREE-LAYER ARCHITECTURE established**: The codebase now has a clear conceptual separation:
  - Layer 1 — Canon (`dev.ergenverse.canon`): immutable canon reconstruction. Every fact carries Provenance.
  - Layer 2 — Simulation (`dev.ergenverse.simulation`): how the universe functions. Includes `simulation/affinity` (ManifestationGiftSystem + BridgingPolicy, MOVED here from the wanglin package).
  - Layer 3 — Emergent History (`dev.ergenverse.history`): everything that happens after the player enters. Stubs created: PlayerHistory, WorldHistory, NpcMemory, RelationshipHistory.
- **Prime Directive REWRITTEN**: "The Er Gen novels are the specification. The game is an implementation of that specification. If canon is silent, do not invent mechanics during the reconstruction phase. Record the silence as a gap. Gaps are filled only after the canon layer is complete, and every gap-filled addition must be clearly marked as inferred rather than canonical."
- **Provenance.java created**: Every canon fact now carries a Provenance record (sourceNovel, chapters, EXPLICIT/INFERRED attestation, confidence 1-5, ambiguities). This guarantees you can ALWAYS distinguish "this is what Er Gen wrote" from "this is what the simulation created after the player entered the world."
- **ALL invented evolution chains DELETED**: The previous ~60 explicit chains used invented stage names ("Peak", "Awakened", "Ascended"), invented "Manifestation Gift" bridging alternatives, and invented realm thresholds. ALL removed. The registry now contains ONLY 10 canon-attested multi-state histories, each with chapter-cited Provenance: Core-Treasure Sword→Dark Green Flying Sword, Soul Lasher→Karma Whip (Ch.731), Fragment Stamp→18-Hell Stamp (Ch.915), Star Compass→Silver Dragon Star Compass (Ch.477), Celestial Sword→Rain Celestial Sword (Ch.717), 1st Restriction Flag Incomplete→Complete, Mosquito Beast destroyed→restored (Ch.1276→1626), God-Slaying Sword destroyed→restored (Ch.1273), Seven-Colored Nail destroyed→restored (Ch.1082→1626), Soul Flag→Billion Soul Flag.
- **"No locked upgrades" directive REWRITTEN**: Replaced with the honest formulation: "Every canonical state must be obtainable." If an item has 3 canon states, the player can reach all 3. If it has 1 canon state, that is the complete item — nothing is missing. No invented "Peak" stages.
- **ItemEvolutionChain rewritten as Historical State Machine**: Removed `bridgingAlternatives` field, removed `validateNoDeadEnds()` enforcement, renamed `EvolutionStage`→`CanonState`, added `Provenance` to every state, reframed states as event-driven (each state tied to a specific canon event). The builder methods are now `acquisitionState()` and `canonState()` (replacing `baseStage()` and `stage()`).
- **WangLinItem tooltip rewritten**: Now shows "Canon State: X / Y documented" (not "Evolution: X / Max"), displays the canon event that produced the current state, shows the Provenance citation (e.g. "RI Ch. 731 [EXPLICIT 5/5]"), and for single-state items shows "single documented form — no invented upgrades."
- **WangLinItems.registerDefaultChain() DELETED**: Items without explicit canon histories no longer get invented 2-stage chains. They simply have no chain — the item exists in one canonical form, period.
- **ManifestationGiftSystem + BridgingPolicy MOVED**: From `dev.ergenverse.wanglin` (canon-adjacent) to `dev.ergenverse.simulation.affinity` (Layer 2). The Canon layer (ItemEvolutionChain, ItemEvolutionRegistry, WangLinItem, WangLinItems) no longer references them at all. WangLinCosmologyRegistry imports them from the new location for bootstrap-time logging only.
- **Cross-layer reference audit PASSED**: Canon → does not reference simulation/history in code. Simulation → does not reference wanglin package (no circular deps). History → stubs only, no cross-refs yet.

---

## 1. Meng Hao (ISSTH) — Unfinished Research

### Gaps (from worklog line 883)
- [ ] Full list of Meng Hao's incantations from the Techniques subpage
- [ ] Full Meng Hao/Equipments list from the Fandom wiki
- [ ] Exact tribulation waveform per realm
- [ ] Full enumeration of Meng Hao's titles
- [ ] 9-9-1 tribulation structure — remains UNVERIFIED (likely fan-synthesis)

### Medium confidence (from worklog line 882)
- [ ] Exact function nuances of 7th (Karmic) and 8th (Body-Mind) Hexes — confirmed from snippets but not deeply explored

### Wiki pages to read when API recovers
- https://meng-hao-i-shall-seal-the-heavens.fandom.com/wiki/Meng_Hao/Techniques
- https://meng-hao-i-shall-seal-the-heavens.fandom.com/wiki/Meng_Hao/Equipments
- https://meng-hao-i-shall-seal-the-heavens.fandom.com/wiki/Meng_Hao (full page)

---

## 2. Su Ming (Pursuit of the Truth) — Unfinished Research

### Gaps (from worklog lines 1343-1346)
- [ ] Full Su Ming/Cultivation wiki page — could not retrieve (rate-limited); relied on snippets
- [ ] Full realm ladder for Su Ming — not exhaustively enumerated
- [ ] /wiki/Su_Ming/Relationships page — could not retrieve
- [ ] /wiki/Su_Ming/Fights page — could not retrieve

### Wiki pages to read when API recovers
- https://pursuit-of-the-truth-novel.fandom.com/wiki/Su_Ming/Cultivation
- https://pursuit-of-the-truth-novel.fandom.com/wiki/Su_Ming/Relationships
- https://pursuit-of-the-truth-novel.fandom.com/wiki/Su_Ming/Fights

---

## 3. Wang Baole (AWWP) — Unfinished Research

### Gaps (from worklog lines 2713-2716)
- [ ] Hades Coffin (冥棺) — coffin-interior-world mechanics
- [ ] Wang Yiyi (王一一) — mask spirit details
- [ ] Broken Arm (残臂) — unverified
- [ ] Eight Extreme Dao (八极道) — unverified
- [ ] Dark Art (黑暗术) — full technique list
- [ ] Wang Baole's mass-produced Dharma Artifacts (法器批量制造)
- [ ] Twelve Emperors Puppets (十二帝王傀儡)
- [ ] Dark Sect (黑暗星河) interstellar travel mechanics

### Low confidence (from worklog line 1555)
- [ ] Divine Donkey "Son of a bitch!" catchphrase — UNVERIFIED
- [ ] Divine Donkey "raw material points" framing — UNVERIFIED (likely game-mechanic paraphrase)

---

## 4. Xu Qing (BTT) — Unfinished Research

### Gaps (from worklog line 1667)
- [ ] BTT cosmology deep-dive (Xu Qing's era, world structure, relationship to Vast Expanse / Outside of Time)
- [ ] BTT-specific cosmology — gap-flagged due to rate-limiting

### Unverified claims (from worklog lines 959-979, 1120-1123)
- [ ] Mysterious Shadow "copies passive environmental resistances" — UNVERIFIED. Canon: evolution-per-Great-Realm via Heterogeneity devouring
- [ ] You Lingzi "manages micro-targeting coordinates of flying iron rods" — UNVERIFIED. Canon: directs one Black Iron Sign weapon
- [ ] "God-Advent, God-Form" terminology — UNVERIFIED. Wiki uses "1st-Level God State" through "4th-Level God State"
- [ ] "God State 7+ levels" — UNVERIFIED. Wiki lists only 4 God State levels (4th = Violet Lord)
- [ ] Vajra Ancestor "constantly flatters him" — UNVERIFIED, plausible but needs chapter reading

---

## 5. Bai Xiaochun (AWE) — Unfinished Research

### Gaps (from worklog line 1621)
- [ ] "Undying Blood Avatar" (不死血身) — UNVERIFIED. Not supported by primary sources. If a "blood clone" mechanic is desired, use: fire clone + Soul-Storing Mirror illusory clones + Undying Blood one-drop milestone

---

## 6. Cross-Novel / Cosmology — Unfinished Research

### Gaps (from worklog lines 1774-1778, 2387, 2327)
- [ ] Eternal Mother / Eternal Lands ↔ Vast Expanse relationship
- [ ] The "5th step" cultivation realm — is it the same as the KB's "Heaven-Trampling / Transcendence" (stage 17), or a new tier beyond it?
- [ ] Verify whether Wang Lin / Su Ming canonically assault Allheaven or merely parallel Meng Hao's path
- [ ] "Four Supremes (God/Devil/Demon/Ghost)" canonical term — UNVERIFIED
- [ ] Meng Hao = "The Demon" Supreme title — UNVERIFIED
- [ ] Patriarch Vast Expanse = "The Ghost" — NOT CONFIRMED
- [ ] Patriarch Vast Expanse "first to Transcend" — UNVERIFIED
- [ ] "Vast Expanse School" founding — UNVERIFIED
- [ ] Patriarch Vast Expanse "created the Copper Mirror" — UNVERIFIED (files say Bronze Lamp, not Copper Mirror)
- [ ] Heaven-Defying Bead being the SAME bead across novels — UNVERIFIED
- [ ] Su Ming's direct appearance in ISSTH/later novels — UNVERIFIED
- [ ] De Luozi in AWWP — UNVERIFIED
- [ ] Per-novel Heaven-Will antagonists for AWE (Heavenspan Daoist/Gravekeeper), Ptt, AWWP, BTT
- [ ] Wang Baole's Transcendence — UNVERIFIED
- [ ] Xu Qing's Living God ↔ Transcendence equivalence — UNVERIFIED

---

## 7. Wang Lin (RI) — Unfinished Research

### Gaps (from worklog line 917-920)
- [ ] Exact chapter numbers for many long-tail items/techniques — unverified beyond the Fandom table
- [ ] "Lu Mo = Slaughter-Silence Clone" identity — discrepancy between Baidu ("Killing+Silent Extinction" fusion) and the wiki ("Slaughter+Restriction+Absolute Beginning+Absolute End+Thunder essences" fusion). Flagged ⚠️

### Wiki pages READ when API recovered (2026-07-12) — TWO rounds:

**Round 1 (earlier session — alt-wiki / renegade-immortal-xian-ni):**
- [x] https://renegade-immortal-xian-ni.fandom.com/wiki/Wang_Lin/Items — 19 items in narrative form. See CANON_RI_WIKI_FINDINGS.md §1.
- [x] https://renegade-immortal-xian-ni.fandom.com/wiki/Wang_Lin/Techniques — specializations, divine abilities, Bai Fan's 6 spells, Qing Lin's spell, 29+ techniques. See CANON_RI_WIKI_FINDINGS.md §2.
- [x] https://renegade-immortal-xian-ni.fandom.com/wiki/Wang_Lin/Cultivation_Explanation — full realm progression with chapter numbers. See CANON_RI_WIKI_FINDINGS.md §3.

**Round 2 (2026-07-12, RI-BIBLE-wiki-research — primary xian-ni wiki + supplementary pages):**
- [x] https://xian-ni.fandom.com/wiki/Wang_Lin/Items — FULL items table with chapter citations, 12 sub-categories. ~95 distinct entries. See CANON_RI_WIKI_RESEARCH_FINDINGS.md §1.
- [x] https://xian-ni.fandom.com/wiki/Wang_Lin/Techniques — FULL techniques table, 6 main sections. ~105 distinct entries. See CANON_RI_WIKI_RESEARCH_FINDINGS.md §2.
- [x] https://xian-ni.fandom.com/wiki/Wang_Lin/Relationships — Li Muwan (wife), Liu Mei/Mu Bingmei & Li Qian Mei (loved by), 5 named FRIENDS (Dun Tian, Situ Nan, Zhou Yi, Qing Shui, Lu Yun). See §3.
- [x] https://xian-ni.fandom.com/wiki/Wang_Lin/Cultivation — 2,077 lines; confirms 14 Essences and 9 Heaven-Trampling Bridges progression. See §9C.
- [x] https://xian-ni.fandom.com/wiki/Essences_Evolutions — full evolution history of each of 14 Essences with chapter citations. See §9D.
- [x] https://xian-ni.fandom.com/wiki/Restrictions — 6-grade restriction hierarchy (Yellow/Earth/Mystery/Heaven/Void/Abstract) + 4 Great Restrictions. See §4B.
- [x] https://xian-ni.fandom.com/wiki/Shi_Realm — sister realm to Ji Realm (Creativity and Life); confirms Ji/Shi/Dao all have spiritual-energy vs divine-sense versions. See §9B.
- [x] https://xian-ni.fandom.com/wiki/Ji_Realm — full article; Ji Realm mechanics, acquisition narrative (Jue Ming Valley → foreign battleground), 2-type distinction (qi-integrated vs True Ji Realm), Death Spell imitation. See §8.
- [x] https://xian-ni.fandom.com/wiki/Category:Formations — exists but only lists 3 formations (sparse category). See §4A.
- [x] https://xian-ni.fandom.com/wiki/Restriction_Flag — **PAGE DOES NOT EXIST** (Fandom placeholder). Synthesized from other wiki pages. See §5.
- [x] https://xian-ni.fandom.com/wiki/Prohibition_Banner — **PAGE DOES NOT EXIST** (Fandom placeholder). Concept overlaps with Restriction Flag / Soul Devil Ship / Isolation Restriction Compass. See §6.
- [x] https://xian-ni.fandom.com/wiki/Divine_Sense — **PAGE DOES NOT EXIST** (Fandom placeholder). Divine Sense mechanics synthesized from Ji_Realm, Shi_Realm, items, techniques pages. See §7.
- [x] https://renegade-immortal-xian-ni.fandom.com/wiki/Wang_Lin — alt-wiki main page (cross-ref). See §9E.
- [x] https://renegade-immortal-xian-ni.fandom.com/wiki/Wang_Lin/Techniques — alt-wiki techniques; revealed 4 missing techniques (Furnace Becomes the World, Soul Vortex, Ghost Summoning Technique, Ji Realm Spiritual Energy Refining) + 1 missing spell (Unknown Palm Spell of Master Lufu). See §9F.
- [x] https://renegade-immortal-xian-ni.fandom.com/wiki/Wang_Lin/Items — alt-wiki items; added context for Sword Sheaths (Heng Yue Sect friend with axe origin), Devil Armor (Ancient Demon Bei Lou helper), Celestial Emperor Crown (kill 9,999 mortal emperors mechanic). See §9G.

### Wang Lin RI gaps status after Round 2 (2026-07-12):
- [x] **CLOSED:** Full items list (read both xian-ni wiki table AND alt-wiki narrative)
- [x] **CLOSED:** Full techniques list (read both xian-ni wiki table AND alt-wiki)
- [x] **CLOSED:** Wang Lin/Relationships
- [x] **CLOSED:** Category:Formations (page exists, only 3 formations listed — sparse wiki)
- [x] **CLOSED:** Restriction_Flag dedicated page — does NOT exist on xian-ni wiki; data synthesized from items/techniques/alt-wiki pages
- [x] **CLOSED:** Prohibition_Banner dedicated page — does NOT exist on xian-ni wiki; concept overlaps with Restriction Flag family
- [x] **CLOSED:** Divine_Sense dedicated page — does NOT exist on xian-ni wiki; mechanics documented across Ji_Realm + Shi_Realm + items + techniques pages
- [x] **CLOSED:** Ji_Realm dedicated page — full article content
- [x] **CLOSED:** Wang Lin/Cultivation page — 14 Essences + 9 Bridges progression fully verified
- [x] **CLOSED:** Essences Evolutions page — all 14 Essences with chapter citations
- [ ] "Lu Mo = Slaughter-Silence Clone" identity discrepancy — STILL UNRESOLVED (the wiki Cultivation page does not explicitly clarify Lu Mo's exact essence composition; alt-wiki says Slaughter True Body was "given independence and sent back in time by Wang Lin to find a method to resurrect Li Muwan")

### Wang Lin RI gaps status after Round 3 (RI-FORGE-CANON-RESEARCH, 2026-07-12):
- [x] **CLOSED:** Restriction Flag — Tu Si (Ancient God) ink foundation confirmed (Reddit r/Donghua 1g83gfw); 3-flag set + 99,999 restrictions confirmed (xian-ni wiki Wang_Lin/Items).
- [x] **CLOSED:** Heaven Defying Bead — dual nature + dew/water→spiritual liquid mechanic + Ji Realm precondition confirmed (xian-ni wiki Heaven_Defying_Bead + Baidu Wang Lin entry).
- [x] **CLOSED:** Karma Whip — creator Wang Lin + Karma Domain binding + Soul Lasher fusion confirmed (xian-ni wiki Karma_Whip).
- [x] **CLOSED:** 9 Heaven Trampling Bridges — 9-bridge structure + per-bridge trials (Dao Heart, Inner Demon, ...) confirmed (xian-ni wiki Heaven_Trampling + Half_Heaven_Trampling).
- [x] **CLOSED:** Situ Nan inheritance — Green Soul identity + Heaven Defying Pearl bloodline link confirmed (Baidu Si_Tu_Nan entry).
- [x] **CLOSED:** Qing Lin spell — Body Fixation Art transmission confirmed; Qing Lin = Immortal Lord Qing who gifted Origin Treasure (Baidu Wang Lin entry).
- [x] **CLOSED:** Tu Si / Ancient Devil — knowledge/power inheritance split (Wang Lin ↔ Tuo Sen) confirmed (xian-ni wiki Tu_Si + Reddit 1lvadil).
- [x] **CLOSED:** Pets list — Mosquito Beast (Lil Mosqi), Thunder Toad, Lei Ji, etc. confirmed; 10,000+ mosquito swarm confirmed.
- [x] **CLOSED:** Flying swords list — Core-Treasure→Dark Green evolution, Dragon Formation, Jufu sword-spirit, etc. confirmed.
- [x] **CLOSED:** Puppets / Immortal Guards — transformation mechanic + 3 named guards (Du Jian, Thunder Daoist, Ta Shan) confirmed.
- [x] **CLOSED:** Storage treasures — Collection Pavilion, Storage Space, Space Stone, etc. confirmed.
- [x] **CLOSED:** 14 Essences — explicitly enumerated by xian-ni wiki + Baidu Baike ("fourteen Origins: Metal, Wood, Water, Fire, Earth, Thunder, Life and Death, True and False, Cause and Effect, Reincarnation, ...").
- [x] **CLOSED:** Avatars/clones — "no primary/secondary body" principle confirmed (alt-wiki Wang_Lin/Avatar_and_Clones_Explanation).
- [x] **CLOSED:** Devil Armor + Ancient Demon Bei Lou helper role confirmed (Baidu Ancient_Demon_Beiluo entry).
- [x] **CLOSED:** Sword Sheaths ×5 — count and "insert flying sword to enhance" mechanic confirmed (xian-ni wiki + Baidu).
- [x] **CLOSED:** Formation flags — Tu Si ink foundation + 6-grade restriction hierarchy confirmed.
- [x] **CLOSED:** Talismans — Blue-skinned man with 9 talismans encounter confirmed.
- [x] **CLOSED:** Divine abilities — Bai Fan's 6 spells + Situ Nan's 3 celestial finger spells confirmed.
- [x] **CLOSED:** Movement/escape arts — Ascendant self-detonation escape confirmed.
- [x] **CLOSED:** Cultivation realm ladder — 4-step structure (1st/2nd/3rd/4th Step) confirmed across multiple sources.
- [x] **CLOSED:** Third/Fourth Step mechanics — 9-bridge requirement + Grand Empyrean bridge-crosser gap confirmed.
- [x] **CLOSED:** Joss Flame economy — poisonous nature + Wang Lin's no-Joss-Flame path confirmed (xian-ni wiki Essence + Reddit 1qkem3y).
- [x] **CLOSED:** Samsara / Reincarnation Dao — Reincarnation Essence sent Slaughter back in time (NOT Dream Dao) confirmed (Reddit 1ao3ltp).
- [x] **CLOSED:** Three-branch Ancient Clan (God/Devil/Demon, descended from Ye Mo) confirmed (xian-ni wiki Ancient_Clan + Reddit 1q8jqe3).

### NEW gaps OPENED by Round 3 research (next-fill list):
- [x] **M1. CLOSED (RI-FORGE-GAPS-CLOSE).** Lei Daozi (雷道子) registered as `enemy_lei_daozi` in `CanonicalEnemies.java` — envoy of Lei Xian Hall, Yin Xu-tier master, coveted Lei Ji, threatened Wang Ping, refined into Wang Lin's First Immortal Guard puppet. Provenance cites Ch. ~1080-1100 (Lei Ji era; donghua Ep. 143), confidence 4. Note: original gap text said "Ch. ~1500s" but Lei Ji was unsealed Ch. ~1080 (per CanonicalPets), so Lei Daozi's defeat is ~Ch. 1080-1100 (Episode 143 of the donghua).
- [x] **M2. CLOSED (RI-FORGE-GAPS-CLOSE).** Lei Xian Hall / Thunder Immortal Hall (雷仙殿) registered as `realm_lei_xian_hall` in `CanonicalRealms.java`. Canon disambiguation vs Thunder Celestial Temple (L54) explicitly flagged as pending in the entry's canonSummary, demonstrated behaviors ("Possible overlap with Thunder Celestial Temple (L54) — disambiguation pending" + "Name UNKNOWN at edge of canon for distinct faction identity — not invented"), and interactionTags (`needs_disambiguation`). No faction list edit made — the WangLinMasterRegistry IS the canonical faction/location store.
- [x] **M3. CLOSED (prior session).** "Lil Mosqi" personal name already present in `CanonicalPets.java` (displayName="Mosquito Beast (Lil Mosqi)", displayNameCn="蚊兽 / 小蚊") and `CanonicalPetRegistry.java` (5 explicit references: javadoc, displayName, bloodline field, lifecycle milestones, canonSummary, provenance citation citing xian-ni.fandom.com/wiki/Lil_Mosqi). No duplicate created.
- [x] **M4. CLOSED (prior session).** Qing Lin entry in `CanonicalKnowledge.java` (`knowledge_qing_lin_spell`) already has both mandated facts in its canonDemonstratedBehaviors (lines 226-227): "Strongest Celestial Emperor inside Sealed Realm during Celestial Domain reign" + "Used a life-threatening spell to seal the Inner Realm for 30 years during Wang Lin's 70-year absence". Provenance ambiguity note (line 216) cites the xian-ni wiki source. Existing behaviors retained. (Note: this is the canonical-registry entry — the separate `CANON_RI_COMPLETE_WORLD.md` N26 narrative doc was not edited; the registry is the source of truth for the mod.)
- [x] **M5. CLOSED (prior session + verified RI-FORGE-GAPS-CLOSE).** 9 Heaven Trampling Bridges all have explicit tribulation names in `CanonicalDao.java` (each bridge entry has "Tribulation name: X" as first item of canonDemonstratedBehaviors) AND in `HeavenTramplingBridgesSpec.java` (9 StateDescription entries with the tribulation name in the title). Per xian-ni.fandom.com/wiki/Half_Heaven_Trampling: 1st=Universal Law & Origin Soul Fusion; 2nd=Dao Heart Tribulation; 3rd=Inner Demon Tribulation; 4th=Unknown; 5th=Transcending Reincarnation; 6th-9th=Unknown. **4 of 9 bridges canon-named; 5 of 9 explicitly marked UNKNOWN** ("Name UNKNOWN at edge of canon — not invented"). Web_search (RI-FORGE-GAPS-CLOSE run) re-verified the wiki only names 2nd, 3rd, 4th (=Unknown) explicitly in its snippet — the prior session's full-wiki-read extraction of 1st (Universal Law & Origin Soul Fusion) and 5th (Transcending Reincarnation) is retained as canon-grounded.
- [ ] **Optional. Cross-check YouTube "10 Spirit Beasts Wang Lin Subdued" list** against our 8-pet list — verify if 2 additional beasts are canon-attested.
- [ ] **Optional. Enumerate the "8 Epic Fusions" of Wang Lin's Ancient God Body** (TikTok) as a narrative layer in the Ancient Order Cultivation Track of `CANON_RI_COMPLETE_TECHNIQUES.md`.

### Resolved contradictions (Round 3):
- [x] **C1. "17 Domains" claim** (Hindi fan YouTube) vs our 14 Essences + 5 Domains. **RESOLVED**: 14 Essences explicitly confirmed by both xian-ni wiki AND Baidu Baike. "17 Domains" is a fan-channel miscount. Our canon is correct. No action.
- [x] **C2. "Four Great Ancient Races" claim** (Baidu snippet on Ancient Demon Beiluo) vs "3 branches of Ancient Clan" (xian-ni wiki). **RESOLVED**: xian-ni wiki consistently says 3 branches (God/Devil/Demon descended from Ye Mo); Reddit directly confirms "YE MO was the progenitor of Ancient gods, devil's, and demons" (3, not 4). Baidu's "Four Great Ancient Races" is likely a Baidu categorization counting the Ancient Clan as a whole + its 3 branches. Our canon is correct (3 branches). No action.

### New gaps OPENED by Round 2 research (next-fill list):
- [ ] Add 3 Tattoo Tribe Treasures to CANON_RI_COMPLETE_ITEMS.md: Wither Dao pair (Ch. 774), Tattoo Talisman (Speed Boost), Beast Skin Tattoo
- [ ] Add Furnace Becomes the World, Soul Vortex, Ghost Summoning Technique, Ji Realm Spiritual Energy Refining, Unknown Palm Spell of Master Lufu to CANON_RI_COMPLETE_TECHNIQUES.md (all from alt-wiki)
- [ ] Add Ji Realm 2-type distinction (qi-integrated vs True Ji Realm at spiritual level) to CANON_RI_COMPLETE_TECHNIQUES.md
- [ ] Add Death Spell technique (imitates Ji Realm) to CANON_RI_COMPLETE_TECHNIQUES.md
- [ ] Verify and add NPCs to CANON_RI_COMPLETE_WORLD.md: Liu Wen (first Shi divine sense), Master Lufu (3rd-Step Cultivator), Ancient Demon Bei Lou (Devil Armor acquisition helper)
- [ ] Add Celestial Emperor Crown mechanic: requires killing 9,999 mortal emperors
- [ ] Add Sword Sheath #1 origin detail (from Heng Yue Sect friend with axe → new evil master)
- [ ] Disambiguate Blood Nascent Soul vs Blood-Red Nascent Soul (both Ch. 1194)

---

## 8. Formation/Talisman Research — Unfinished

### Searches completed when API recovered (2026-07-12)
- [x] Wang Lin Restriction Flag — FOUND. Reddit discussion confirms it uses ink from Tu Si (Ancient God) as foundation. See CANON_RI_WIKI_FINDINGS.md §4.
- [x] Wang Lin items complete list — FOUND. 19 items on dedicated wiki page. See CANON_RI_WIKI_FINDINGS.md §1.
- [x] Wang Lin techniques complete list — FOUND. See CANON_RI_WIKI_FINDINGS.md §2.
- [x] Unnamed Wheel Formation — FOUND. It's an upgraded Life-Death domain formation. See CANON_RI_WIKI_FINDINGS.md §5.
- [x] **CLOSED (2026-07-12, RI-BIBLE-wiki-research):** Wang Lin Ji Realm origin — fully resolved via dedicated Ji_Realm wiki page. Jue Ming Valley → foreign battleground narrative confirmed. Ji Realm acquired unknowingly at Foundation Establishment. See CANON_RI_WIKI_RESEARCH_FINDINGS.md §8.
- [ ] Heavenly Fate Sect 7 color divisions — searched but no RI-specific results found. Still open.

### Wiki pages READ (2026-07-12, RI-BIBLE-wiki-research):
- [x] https://xian-ni.fandom.com/wiki/Category:Formations — page exists; only 3 formations listed (Life-Death/Karma/True-False Wheel Formation; Realm-Sealing Formation; Unnamed Wheel Formation). Wiki category is sparse. See CANON_RI_WIKI_RESEARCH_FINDINGS.md §4.
- [x] https://xian-ni.fandom.com/wiki/Restriction_Flag — **PAGE DOES NOT EXIST** (Fandom placeholder). Restriction Flag data synthesized from Wang Lin/Items + Wang Lin/Techniques + alt-wiki pages. See CANON_RI_WIKI_RESEARCH_FINDINGS.md §5. Three flags confirmed (1st incomplete / summon divine tribulation; 2nd mixed restrictions; 3rd pure attack). 99,999 restrictions to complete. Made from 3× Ink Stones.
- [x] https://xian-ni.fandom.com/wiki/Prohibition_Banner — **PAGE DOES NOT EXIST** (Fandom placeholder). Concept overlaps with Restriction Flag family (禁 = both "prohibition" and "restriction"). See CANON_RI_WIKI_RESEARCH_FINDINGS.md §6.
- [x] https://xian-ni.fandom.com/wiki/Restrictions (supplementary) — confirmed 6-grade restriction hierarchy (Yellow/Earth/Mystery/Heaven/Void/Abstract) and 4 Great Restrictions (Annihilation, Life/Death, Ancient Soul, Time). Abstract is the peak; no one comprehended it before Wang Lin. Quotes from Li Yuan (Ch. 749) and Wang Lin (Ch. 1188).

---

## 9. Divine Sense Research — Unfinished

### Wiki pages READ (2026-07-12, RI-BIBLE-wiki-research):
- [x] https://xian-ni.fandom.com/wiki/Divine_Sense — **PAGE DOES NOT EXIST** (Fandom placeholder). Divine Sense mechanics synthesized from Ji_Realm + Shi_Realm + items + techniques pages. See CANON_RI_WIKI_RESEARCH_FINDINGS.md §7. Key findings: Soul Piercing Eyes (Ch. 179, requires restriction mastery); Heart Restriction (Ch. 858); Eyes Suppressing the World (Ch. 1896, from Ji Si's sword fragment); Straw Hat (blocks divine senses); Dark Heaven Stone (stores divine sense for avatars); Heaven Trampling divine sense glimpse from Second Bridge (covered entire Celestial Clan).
- [x] https://xian-ni.fandom.com/wiki/Ji_Realm — FULL article. One of 3 Great Realms (with Shi and Dao). Represents Extreme Force and Death. Lightning-element. Acquisition: Jue Ming Valley sliver → foreign battleground completion. 2 types: qi-integrated (weaker, Wang Lin's Core Formation state) vs True Ji Realm (spiritual-level, like Shi/Dao). Bypass via avatar cultivation OR refining into treasure (Wang Lin used it for Thunder Essence). Death Spell technique partially imitates Ji Realm. See CANON_RI_WIKI_RESEARCH_FINDINGS.md §8.
- [x] https://xian-ni.fandom.com/wiki/Shi_Realm (supplementary) — sister to Ji Realm. Represents Creativity and Life. Allows cultivator to form own spells. **CANON MECHANIC:** Ji, Shi, and Dao all have spiritual-energy versions (weaker, fleeting) AND divine-sense versions (much more powerful, hard to reach, last long). Liu Wen = first cultivator with recorded Shi divine sense. See CANON_RI_WIKI_RESEARCH_FINDINGS.md §9B.
- [ ] https://meng-hao-i-shall-seal-the-heavens.fandom.com/wiki/Divine_Sense — NOT YET READ (out of scope for RI-BIBLE-wiki-research; ISSTH-specific).
- [ ] Dao God Scripture Divine Sense Volume (ISSTH) — NOT YET READ (out of scope for RI-BIBLE-wiki-research; ISSTH-specific).

### RI Divine Sense gaps CLOSED (2026-07-12):
- [x] Ji_Realm dedicated page — fully read
- [x] Shi_Realm dedicated page — fully read (sister to Ji Realm)
- [x] Ji/Shi/Dao = spiritual energy vs divine sense versions mechanic — confirmed canon
- [x] Liu Wen = first Shi divine sense cultivator — confirmed
- [x] Ji Realm 2-type distinction — confirmed
- [x] Death Spell (imitates Ji Realm) — confirmed
- [x] Heaven Trampling divine sense glimpse from Second Bridge — confirmed
- [x] Ji Realm acquisition narrative (Jue Ming Valley → foreign battleground) — confirmed

### RI Divine Sense gaps still OPEN (next-fill):
- [ ] Add the above 8 closed items explicitly to CANON_DIVINE_SENSE_USES.md as a new "Part 16: Ji/Shi/Dao Realms Canon" section (will be added by this research run).
- [ ] Verify and add Liu Wen NPC to CANON_RI_COMPLETE_WORLD.md
- [ ] Verify and add Master Lufu (3rd-Step Cultivator) NPC to CANON_RI_COMPLETE_WORLD.md

---

## 10. Research Verdicts Requiring Fresh Wiki Access

From worklog line 2387, these items are LOW/UNVERIFIED and need fresh wiki access:
- [ ] "Four Supremes (God/Devil/Demon/Ghost)" canonical term
- [ ] Meng Hao = "The Demon" Supreme title
- [ ] Patriarch Vast Expanse = "The Ghost"
- [ ] Patriarch Vast Expanse "first to Transcend"
- [ ] "Vast Expanse School" founding
- [ ] Patriarch Vast Expanse "created the Copper Mirror" (vs Bronze Lamp)
- [ ] Heaven-Defying Bead being the SAME bead across novels
- [ ] Su Ming's direct appearance in ISSTH/later novels
- [ ] De Luozi in AWWP
- [ ] Per-novel Heaven-Will antagonists (AWE, Ptt, AWWP, BTT)
- [ ] Wang Baole's Transcendence
- [ ] Xu Qing's Living God ↔ Transcendence equivalence

---

## Action Plan When API Recovers

1. **Re-run the 8 mandated searches** (from worklog line 2388):
   - Patriarch Vast Expanse wiki page
   - "Four Supremes" term
   - Vast Expanse School
   - Copper-mirror-vs-bronze-lamp disambiguation
   - Su Ming ISSTH cameo
   - De Luozi AWWP
   - Per-novel Heaven-Will antagonists
   - BTT cosmology deep-dive

2. **Read the wiki pages listed above** using web_reader

3. **Update CANON_DIVINE_SENSE_USES.md**, CANON_FORMATIONS_TALISMANS_USES.md, and SYSTEMS_AUDIT_COMPLETE.md with verified findings

4. **Close the UNVERIFIED flags** in the worklog and codex

5. **Set up a cron job** to retry the API periodically until it recovers

### Gap CLOSED this run (RI-FORGE-REGISTRY-FRAMEWORK):
- **WangLinMasterRegistry framework wired + verified**: The dev/ergenverse/wanglin/registry/ package (27 .java files, built by a prior session) was assessed, found to have ONE compile-blocking bug (CanonicalCategory missing BODIES constant — CanonicalAvatars.java referenced it 5 times), and was NOT yet wired into the main mod class.
- **FIXED CanonicalCategory.java**: Added `BODIES(Group.DAO_ESSENCE, "Bodies", "True Bodies condensed from Origins (Five Elements True Body, Taichu/Miemie/Restriction/Thunder Origin True Bodies)")` — unblocked 5 references in CanonicalAvatars.java.
- **WIRED into Ergenverse.java**: Added `import dev.ergenverse.wanglin.registry.WangLinMasterRegistry;` + `WangLinMasterRegistry.bootstrap();` call after WangLinPersonality.bootstrap() and before WorldLaws.bootstrap() (correct order: after cosmology + personality, before WangLinItems).
- **VERIFIED COMPILATION**: Used portable JDK 21 (/tmp/jdk-21.0.4+7/bin/javac) + slf4j + Ergenverse LOGGER stubs + copied all 8 wanglin/ai dependency files. Compiled all 27 registry files + dependencies → 0 errors, 36 .class files generated. The registry package compiles cleanly.
- **REGISTRY IS DENSELY POPULATED**: 252 canonical entries across 18 sub-registries (far beyond the "2-3 example entries per registry" framework-only minimum):
  - CanonicalInventory: 34 | CanonicalTechniques: 40 | CanonicalKnowledge: 10 | CanonicalPets: 9
  - CanonicalCompanions: 13 | CanonicalAvatars: 14 | CanonicalBodies: 9 | CanonicalRestrictions: 12
  - CanonicalFormations: 12 | CanonicalEssences: 14 | CanonicalDao: 7 | CanonicalTitles: 9
  - CanonicalSkills: 12 | CanonicalExperiences: 12 | CanonicalEnemies: 10 | CanonicalAllies: 11
  - CanonicalRealms: 10 | CanonicalHistoricalEvents: 14
- **TeachingResolver pipeline live**: Walks Player request → lookup → ownership gate → transferability gate → exact-copy gate → WangLinTeachingPolicy delegation → TeachingResult. 9 outcomes including GRANTS_EXACT_COPY (honoring the user's "exact copy" directive).
- **web_search API recovered**: z-ai web_search now returns valid results (no longer 429). Available for future canon-verification runs.

### Remaining Forge mod gaps (next priorities):
- [x] ~~POPULATE the registry to FULL completion (currently 252/630+ entries vs RICanonicalDatabase; user wants every single Wang Lin–attested entity).~~ — RESOLVED. 484 → 555 entries. 303/303 bridge coverage (100%). All 13 BehavioralSpecs linked. Sub-registries: CanonicalInventory 151, CanonicalTechniques 169, CanonicalCompanions 24 (+7 puppets), all others unchanged.
- [x] ~~LINK CanonicalEntry → BehavioralSpec~~ — RESOLVED. 32 → 35 withSpec() calls. All 13 specs linked. 3 orphaned specs (situ_nan_inheritance, tu_si_remnants, qing_lin_inheritance) now connected.
- [x] ~~WIRE WangLinItem tooltips to pull from the registry~~ — RESOLVED (was already done). WangLinItem.appendHoverText() line 225 calls WangLinMasterRegistry.lookup(canonId). With 100% bridge coverage, all 303 items now get canon enrichment in tooltips.
- [ ] IN-GAME teaching NPC interaction screen (calls TeachingResolver.teachableNow() + resolve(request), renders TeachingResult as dialogue)
- [ ] NORMALIZE entry IDs across sub-registries (some use I09_ prefix mirroring RICanonicalDatabase, some don't) for bidirectional lookups
- [ ] JDK 25 environment for full ForgeGradle build (sandbox only has JDK 21; Forge 65.0.3 requires JDK 25 minimum). Pure-Java registry files compile cleanly under JDK 21 — only Forge API mismatches remain unverifiable.

## Status Update — RI-FORGE-CONTENT-EXPANSION-WAVE-1 (2026-07-12)

### Closed gaps this cycle
- **CRITICAL: placed_feature reference naming bug** — All 14 surface biome JSONs referenced non-existent `*_placed` IDs (e.g. `ergenverse:spirit_vein_ore_placed`). FIXED: corrected to actual file names (`ergenverse:spirit_vein_quartz_ore`, `ergenverse:qi_gathering_grass`, etc. — no `_placed` suffix). 31 references fixed across 14 files.
- **Missing structure lang entries** — All 13 structures (7 existing + 6 new) now have lang entries in en_us.json.
- **Pocket dimension latent bugs** — (1) `preset`+`biomes` redundancy in ancient_god_land.json + ancient_thunder_realm.json caused Codec.either resolution failure at world load. FIXED: removed preset field. (2) Flat-list `features` in 5 pocket biomes violated the 11-element list-of-lists biome codec. FIXED: distributed features into correct generation steps. (3) mood_sound used biome names instead of sound event names. FIXED.

### New content added (50 new JSON files)
- **3 new dimensions**: Foreign Battleground (外域战场), Immortal Astral Continent (仙罡大陆), Ancient Thunder Realm (古雷界)
- **10 new biomes** across the 3 dimensions
- **6 new structures**: Xuan Dao Sect, Heavenly Fate Sect main, Vermilion Bird Dynasty capital, Snow Country capital, Ancient God cultivation cave (underground), Soul Refining ancestral ground (underground)
- **8 new spirit herbs**: nine_leaf_clover, soul_nourishing_lotus, five_color_ginseng, blood_forgetting_grass, dao_trace_vine, heart_devil_flower, reincarnation_lily, void_nether_grass

### Still open (priority for next cycles)
- Item textures: 303 items still use `_placeholder.png`. Top 20 iconic items need real textures via image-generation skill.
- Cultivation system expansion: tribulation events, qi-gathering block interactions, breakthrough VFX.
- Perception system: ObjectiveNature + PerceptionEngine + DivineSense (entity renders differently by cultivation tier).
- Manifestation Gift NPC: Wang Lin's companion as in-game NPC.
- Layer 3 emergent history: PlayerHistory, WorldHistory, NpcMemory, RelationshipHistory persistence.
- Advanced mechanics: Samsara Dao, Joss Flame Economy, Cave World Ownership, Realm-Sealing Grand Array.
- Custom NBT structure templates (currently using vanilla village/fortress/mansion stand-ins).
- Teleportation/rift access code for pocket dimensions (dimensions exist but no in-game entry mechanism yet).
- Entity/mob registration: Wang Lin's clones, thunder beasts, soul beasts, fiend beasts.

## Architecture Pivot (2026-07-12 — RI-FORGE-ARCHITECTURE-PIVOT)

### The Pivot
The project architecture has been fundamentally redesigned from Minecraft-first to Canon-first:
```
Er Gen Canon (novels + wiki)
    → Canon Database (ri_canon_database.json — 630 entries)
    → Simulation Engine (Java — WorldGraph, ecology, civilization)
    → Canon Worldgen Adapter (Python — scripts/canon_worldgen_adapter.py)
    → Minecraft / Forge (rendering layer only)
```

### What Changed
- **Before:** 116 hand-written worldgen JSONs (29 biomes, 13 structures, 16 features)
- **After:** 1,373 generated worldgen JSONs (303 biomes, 250 structures, 152 features) — generated by the adapter from canon data
- **Growth:** 11.8x in a single adapter pass

### The Adapter
`scripts/canon_worldgen_adapter.py` is a 6-pass generator:
1. Sub-Region Biomes (country → 24 sub-regions)
2. Structure Decomposition (sect → 20 components, city → 11 components)
3. Spirit Vein Systems (5 tiers + 11 elements = 16 variants)
4. Herb Ecological Variants (15 herbs × 8 growth states = 120 variants)
5. Ocean Layers (10 depth layers)
6. Lang File Update (auto-generated display names)

The adapter is IDEMPOTENT and RE-RUNNABLE. Adding more canon data and re-running produces more JSONs.

### Next Expansion Targets
- Add more sects/cities to SECT_BIOME_MAP and CITY_BIOME_MAP in the adapter
- Add sky-layer biomes (cloud beasts, floating mountains, sword cultivators)
- Add underground civilization biomes (spirit mines, crystal forests, sealed demons)
- Add species definitions for spirit beasts (taxonomy, ecology, behavior)
- Add loot provenance metadata (every item with origin, age, owner history)
- Add formation/pill/technique JSON definitions
- Add NPC archetype definitions with dialogue trees
- Add faction relationship graphs
- Target: 5,000-20,000 JSON assets total

## Expansion Cycle (2026-07-12 — RI-FORGE-EXPANSION-SKY-UNDERGROUND-SPECIES-PROVENANCE)

### New Systems Added
1. **Sky Realm** — 12 sky-layer biomes + sky_realm dimension using end noise settings for floating islands. Covers: cloud sea, floating islands (low/high), sword cultivator airspace, cloud whale territory, thunder bird nesting, ancient sky battlefield, meteor stream zone, spatial crack region, celestial observation peak, spirit crane migration, flying sect airspace.

2. **Underground Civilizations** — 12 underground biomes covering: spirit mine shafts, crystal forest caverns, ancient ruin chambers, forgotten formation halls, dead beast crypts, sealed demon prisons, underground spirit rivers, elemental fire/ice caverns, ancient god bone chambers, ore kingdom deep, nether mining colonies.

3. **Spirit Beast Species** — 8 species definition JSONs in `data/ergenverse/species/` with full taxonomy, ecology, and behavior data. Includes the Mosquito Valley ecosystem definition with 5 trophic layers and population dynamics (10M larvae → 1 ancient queen).

4. **Loot Provenance** — 178 provenance JSONs in `data/ergenverse/provenance/`, one per artifact in the canon database. Each has origin, current owner, canon confidence, known facts, and provenance metadata (derivation type, blood-bound status, repair count, known owners, historical events).

### Architecture Notes
- The simulation engine (Java, Layer 2) can now read from `data/ergenverse/species/` and `data/ergenverse/provenance/` — these are data-driven systems.
- The adapter scripts are re-runnable. Adding more canon data and re-running produces more JSONs automatically.
- Total data JSONs: 1,585 (up from 1,373).

### Still Open
- Sky-layer structures (floating sect headquarters, cloud whale observation posts, ancient sky fortress ruins)
- Underground structures (mine shaft networks, crystal forest chambers, sealed demon gates)
- Faction definitions with relationship graphs
- NPC archetype definitions with dialogue trees
- Formation/pill/technique data files
- Seasonal cycle and economy data
- More spirit beast species (the canon has many more named beasts)
- More sects/cities in the structure decomposition maps

## Phase 2 Priority Items — RESOLVED (RI-FORGE-P2-CONTINUE, 2026-07-13)

### Closed gaps this cycle
- **Item Textures 100% Complete**: All 303 items now have real procedural 16x16 pixel-art textures (was 204/303 = 67%, now 303/303 = 100%). Batch 5 added 99 textures (ji_realm, thunder_toad, nine_drops_poison_miao_yin, soul_lasher, soul_devil_ship, devilish_flames_fire_dragon, etc.) plus a final 4-texture sub-batch (demon_spell_wind_fire_mountain, nine_tribulation_karma_fires, ice_imitation_dongling_pool). Zero placeholders remain.
- **Anchor Block Items (Task 3) COMPLETED**: BlockItems for realm_sealing_flag, heaven_splitting_axe_pedestal, dao_binding_stone were already registered via ErgenverseBlocks.registerAnchor(). Fixed missing `CREATIVE_TABS.register(modEventBus)` call (creative tab DeferredRegister was wired but never registered with the mod event bus, so the tab would never have appeared in-game). Added 21 lang entries (3 anchor blocks + 14 herb blocks + creative tab name + divine sense keybind). Build verified GREEN.
- **Joss Flame Source Worldgen (Task 4) COMPLETED**: Created `configured_feature/joss_flame_source.json` (soul_campfire representing a mortal temple/shrine) and `placed_feature/joss_flame_source.json` (rarity 1/32 — very rare). Wired `ergenverse:joss_flame_source` into all 14 canonical biomes' VEGETAL_DECORATION step. Per canon: "Joss Flames are cultivation energy produced by mortal faith" — the soul campfire is the objective-world anchor; mortals see only a campfire, Soul Formation+ cultivators perceive the flame column.
- **Samsara Avatar Gameplay (Task 5) COMPLETED**: Extended `CultivationState` with `samsaraIncarnations` list (NBT-serialized) + `summonIncarnation()` (requires Heaven-Defying Bead, costs 10% Qi) + `mergeIncarnation(id)` (grants next uncomprehended Essence in Wang Lin's completion order). Added `/ergen advanced samsara avatar <summon|merge|status>` subcommands to AdvancedMechanicsCommand with tab-completion for active incarnation IDs. Each incarnation lives a full life (50-1000 yrs) in a randomly-chosen canon context (Mortal Town, Suzaku Tomb, Foreign Battleground, etc.). Reabsorbing all 14 → Heaven Trampling achievement.
- **Cave World Ownership Combat Transfer (Task 6) COMPLETED**: Refactored `CaveWorldOwnership` to support runtime overrides — canon defaults remain immutable, but a `RUNTIME_OWNERSHIP` map records post-divergence state. Added `getOwnedWorldForNpc(characterId)` mapping `seven_colored_daoist` → `cave_world`. Hooked into `EntityCultivator.die()`: when a player kills an owner NPC, `transferOwnership()` fires automatically — cascades to sealed_realm + outer_realm (same owner), dissolves the Realm-Sealing Grand Array, lifts the cultivation ceiling, and sends the player a confirmation message. Recorded as a `cave_world_ownership_transfer` discovery in HistoryManager.
- **Tribulation Multi-Block Event (Task 7) COMPLETED**: Extended `CultivationEvents.spawnTribulationBolt()` with multi-block lightning-pillar tribulation for Nascent Soul+ breakthroughs. Spawns a circle of 4-32 lightning pillars (scaling with realm) around the cultivator. Added `isRealmSealingArrayNearby()` — scans a 16-block AABB for the 3 anchor blocks (realm_sealing_flag, heaven_splitting_axe_pedestal, dao_binding_stone). If found, pillar count is halved and damage multiplier is reduced to 0.5×. Canon: the array was designed to control cultivator breakthroughs; it can also shelter them via the Heaven-Splitting Axe's spirit.
- **Qi-Gathering Block Jade Slip Interactions (Task 8) COMPLETED**: Created `dev.ergenverse.item` package with `ErgenverseItems` (registers the `jade_slip` item — 64-durability, UNCOMMON rarity, "Ergenverse Items" creative tab) and `JadeSlipQiGatheringEvent` (Forge @SubscribeEvent on PlayerInteractEvent.RightClickBlock). When a Qi Condensation+ cultivator right-clicks a spirit-vein or spirit-herb block while holding a jade slip, the slip channels Qi into the cultivator's dantian. Yields: 50 Qi (custom spirit vein) / 20 Qi (custom spirit herb) / 10 Qi (vanilla perception-block stand-in like sea_pickle). Per-block cooldown of 1 MC day prevents exploits; the block is NOT consumed (Qi regenerates canonically). Slip takes 1 durability damage per use. Generated procedural jade_slip.png texture (pale jade-green slip with carved runes + soft glow). Wired `ErgenverseItems.register()` into Ergenverse.java constructor.
- **Build Status**: GREEN throughout (only pre-existing deprecation warnings about `ResourceLocation(String, String)` constructor, which is the correct API for MC 1.20.1 per the task's CRITICAL API CONSTRAINTS).
- **Total Java files**: 203 (was 200). Total textures: 311 (was 204 + jade_slip = 304 → final 311 after batch 5 + jade slip).

### Still Open (v3 priorities)
- [x] ~~World-save persistence for runtime ownership overrides~~ — RESOLVED this session. CaveWorldOwnership.persistToWorldSave() + restoreFromWorldSave() were already built in WorldRuntimeState. Fixed the null ServerLevel bug in transferOwnership() so it actually persists now.
- [x] ~~More owner NPCs beyond `seven_colored_daoist`~~ — RESOLVED this session. Added 3 new owners: Tu Si's remnant (Land of the Ancient God), Vermilion Bird ancestor spirit (Suzaku Tomb), Blood Ancestor (Immortal Graveyard). Total: 4 world-layer owners.
- [x] ~~Loot table replacement for jade slip mining~~ — RESOLVED this session. Foundation+ cultivators now have a 15-60% chance (scaling with realm) to extract the actual herb item when channeling Qi with a jade slip.
- [x] ~~Registry full population~~ — RESOLVED this session. 484 → 555 entries. Bridge coverage: 233/303 → 303/303 (100%). All 13 BehavioralSpecs now linked (32 → 35 withSpec calls). 7 new puppet companion entries added.
- Samsara incarnation visualization — currently the incarnations exist as data; v2 could spawn ghost/wraith entities representing active incarnations.
- Custom NBT structure templates (still using vanilla village/fortress/mansion stand-ins).
