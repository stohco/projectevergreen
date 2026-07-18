# Canon Audit Findings — Cognition Pipeline vs RI Canon

**Date:** 2026-07-15
**Auditor:** main (self-critique)
**Verdict:** The architecture is sound, but many specific implementations are GENERIC xianxia, not RI-specific. They would fit any cultivation game, but they don't capture what makes Renegade Immortal unique.

## Summary of Issues

### 1. Process Types (7 issues)
- **CORRUPT** is BTT (Heterogeneity), not RI primary. RI has HEART_DEMON (心魔) and QI_DEVIATION (走火入魔).
- **ASSIMILATE** is too generic. RI has SOUL_DEVOUR (Ji Realm) and FLESH_DEVOUR (Mosquito Beast).
- **AWAKEN_SPIRIT** is wrong. RI artifact spirits are BORN when a cultivator imprints their soul, not "awakened."
- **MUTATE** needs causes. RI mutations come from Heterogeneity, Karmic residue, Tribulation lightning, Bloodline awakening.
- **HIBERNATE** is for beasts. RI cultivators do CLOSED-DOOR CULTIVATION (闭关). Need SECLUDE for cultivators.
- **DIGEST** is too specific. Should be CONSUME with subtypes.
- **SUCCEDE** is too simple. RI has INHERIT_DAO, INHERIT_SECT, INHERIT_BLOODLINE.

### 2. DaoIdentity Types (11 issues)
- **Missing DEFIANCE_DAO** — Wang Lin's CORE nature (逆). THE defining Dao of RI.
- **Missing SLAUGHTER_DAO** — Wang Lin's primary Dao (杀戮). Arguably most important.
- **Missing KARMA_DAO** — Wang Lin's 4th essence.
- **Missing LIFE_DEATH_DAO** — Wang Lin's 3rd essence. Central to Li Muwan's resurrection.
- **Missing TRUE_FALSE_DAO** — Wang Lin's 5th essence.
- **Missing RESTRICTION_DAO** — Wang Lin's obsession (7 years at Restriction Mountain).
- **Missing ROGUE_CULTIVATOR** — Wang Lin IS this for most of the novel.
- **Missing SECT_LEADER** — Wang Lin becomes Vermilion Bird Divine Emperor.
- **HUNTER** is too generic. RI doesn't have "hunters" as a Dao identity.
- **DESTROYER** is an action, not an identity. RI destruction is motivated by AVENGER or SURVIVAL.
- **SWORD_CULTIVATOR** is ISSTH/AWE, not RI. Wang Lin's Dao is DEFIANCE and SLAUGHTER.

### 3. Need Types (10 issues)
- **FACE vs PRIDE** are DUPLICATES. In xianxia, "face" (面子) IS pride/honor. Merge.
- **SEEKING_INSIGHT vs SEEKING_DAO** are DUPLICATES. Merge.
- **BOREDOM** is not a RI need. Wang Lin is never bored. Remove.
- **Missing HEART_DEMON** (心魔) — internal psychological trials during breakthroughs. MAJOR.
- **Missing KARMIC_DEBT** — Wang Lin must clear all karmic debts to leave Suzaku.
- **Missing DAO_HEART** (道心) — cultivation foundation stability. Critical.
- **Missing TRIBULATION_DEBT** — debt ALREADY owed from defying heaven.
- **Missing FILIAL_PIETY** — Wang Lin's devotion to parents is CENTRAL.
- **Missing SWORN_BROTHERHOOD** — Wang Lin's bonds with Qing Shui, Zhou Yi.
- **HUNGER/THIRST/SLEEP** are mortal-only. Need mortal-only flag or they waste computation.

### 4. Planner Trait Alignments (6 issues)
- **"steal" alignment is context-wrong.** In RI, stealing from enemies is standard, not dishonorable. Needs context.
- **"kill_owner" alignment is context-wrong.** Killing enemy cultivators is pragmatic, not "ruthless." Needs context.
- **"fight" alignment ignores Wang Lin's EXTREME_CAUTION.** He fights when sure of victory, not from courage.
- **"meditate" shouldn't be a scored option.** It's the DEFAULT state, not an alternative.
- **"wait" is STRATEGIC in RI, not passive.** Should align with CUNNING and STRATEGY.
- **Missing RI-specific actions:** SOUL_SEARCH, DEVOUR, SEAL, POISON, DISGUISE, FLEE_VIA_FORMATION, SUMMON_SWARM, BLOOD_BIND, INHERITANCE_TRIAL.

### 5. Belief Sources (6 missing)
- **JADE_SLIP** — primary information source in RI (written records).
- **DIVINE_SENSE_SCAN** — active spiritual probing, distinct from passive observation.
- **KARMIC_VISION** — Wang Lin's karma vision reveals karmic threads.
- **HEART_DEMON_TRIAL** — heart demons reveal truths during tribulation.
- **INHERITANCE_MEMORY** — deliberate knowledge transfer (Tu Si's memories).
- **PROPHECY** — Heaven-Defying Bead visions of the future.

### 6. Memory Types (5 missing)
- **BLOODLINE** — ancestral memories carried in blood (Ancient God bloodline).
- **KARMIC_IMPRINT** — intense karmic events leave imprints.
- **TRIBULATION_MEMORY** — tribulation experiences burned into soul.
- **SOUL_FRAGMENT** — absorbed soul fragments carry memory fragments.
- **REINCARNATION_ECHO** — Samsara Dao users retain past-life echoes.

### 7. SimulationLevel Tick Frequencies (5 issues)
- **HISTORICAL daily tick** is too fast for cultivator cognition. Cultivation progresses daily, but cognition updates on emergence from seclusion.
- **TERRITORY seasonal tick** is wrong for sect politics. Politics change daily, ecology seasonal, military hourly.
- **FULL_COGNITION at 1/sec** is too fast for Ancient beings. Cognition frequency should scale with planning horizon.
- **STORY_IMPORTANCE every tick** is wasteful. Should split: reaction=every tick, planning=every 20, reflection=every 100.
- **Missing SECLUSION state.** RI cultivators enter seclusion for years — should be at STATIC_DATA level during seclusion.

### 8. WorldPressureEngine Types (8 issues)
- **FOOD_PRESSURE** is wrong for cultivator world. Cultivators absorb Qi, not farm. Only mortal villages have food pressure.
- **Missing TRIBULATION_PRESSURE** — felt across region, beasts flee, formations activate.
- **Missing INHERITANCE_PRESSURE** — draws cultivators from across region when inheritance opens.
- **Missing BLOODLINE_PRESSURE** — bloodline awakening resonates with related beings.
- **Missing SEAL_WEAKENING_PRESSURE** — Cave World seal weakening is major plot driver.
- **Missing SOUL_FLUCTUATION_PRESSURE** — detectable by divine sense users from far away.
- **LAW_PRESSURE too vague** — should be DAO_CLASH, REALITY_FISSURE, WORLD_LAW_ENFORCEMENT.
- **TIME_PRESSURE too vague** — should be FORMATION_DECAY, VEIN_DEPLETION, HERB_MATURATION, INHERITANCE_TIMING.

### 9. Territory Populations (4 issues)
- **Mosquito Valley** is not barren. Has spirit herbs, blood-attracted beasts, corpse piles.
- **Sea of Devils** is too sparse. Missing pirates, underwater cities, ancient battlefields.
- **Country of Zhao** is just wilderness. Missing cities, sect disciples, villages, merchants.
- **Heng Yue Sect** has beasts but no sect members. Missing outer/inner/core disciples, elders, patriarch.

## The Core Problem

**The architecture is correct, but the CONTENT is generic.** I built a "cultivation game cognition system" that would work for any xianxia novel. But the user wants RENEGADE IMMORTAL specifically. The difference is in the specific Daos, needs, beliefs, memories, pressures, and actions that RI characters actually experience.

**The fix is not architectural — it's content-level.** I need to:
1. Replace generic xianxia terms with RI-specific terms
2. Add the missing RI-specific Dao types (Defiance, Slaughter, Karma, Life-Death, True-False, Restriction)
3. Add the missing RI-specific needs (Heart Demon, Karmic Debt, Dao Heart, Filial Piety)
4. Add the missing RI-specific belief sources (Jade Slip, Divine Sense, Karma Vision, Prophecy)
5. Add the missing RI-specific memory types (Bloodline, Karmic Imprint, Tribulation, Soul Fragment)
6. Add the missing RI-specific pressure types (Tribulation, Inheritance, Bloodline, Seal Weakening)
7. Add the missing RI-specific actions (Soul Search, Devour, Seal, Disguise, Blood Bind)
8. Fix the territory populations to reflect actual RI geography
9. Make trait alignments context-dependent (steal from enemy vs innocent)
10. Add the SECLUSION state to the LoS engine
