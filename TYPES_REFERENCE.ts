// src/lib/sim/types.ts
// The type system of the Living Universe Simulation core (runtime-agnostic).

// ─── Cosmic hierarchy ───────────────────────────────────────────────
export type CosmicKind =
  | 'multiverse' | 'universe' | 'star_region' | 'star_system'
  | 'world' | 'continent' | 'region' | 'settlement' | 'locality';

export interface CosmicNode {
  id: string;
  kind: CosmicKind;
  name: string;
  parentId: string | null;
  childIds: string[];
  realityRef?: string;
  generated: boolean;
  perceived: boolean;
  note?: string;
}

// ─── Reality Profile (System 2) ─────────────────────────────────────
export type Density = 'none' | 'low' | 'medium' | 'high' | 'extreme' | 'origin';
export type LawStrength = 'fragile' | 'low' | 'medium' | 'high' | 'absolute';
export type HeavenlyWill = 'hostile' | 'neutral' | 'benevolent' | 'absent';
export type WorldType =
  | 'planet' | 'sealed-dimension' | 'pocket-universe'
  | 'star-fragment' | 'ancient-corpse' | 'transcendent-dream';

export interface RealityProfile {
  worldType: WorldType;
  qiDensity: Density;
  spiritVeinDensity: 'scarce' | 'moderate' | 'rich' | 'saturated';
  worldLawStrength: LawStrength;
  maxRealm: RealmId;
  heavenlyWill: HeavenlyWill;
  karmaDensity: 'low' | 'medium' | 'high' | 'crushing';
  timeFlow: string;
  spaceStability: LawStrength;
  tribulationSeverity: 'none' | 'low' | 'moderate' | 'high' | 'world-ending';
  ancientRuins: 'few' | 'some' | 'many' | 'saturated';
  beastPopulation: 'low' | 'medium' | 'high' | 'teeming';
  voxelRealityStrength: RealmId;
  daoAffinity: Partial<Record<DaoId, number>>;
}

// ─── Realms (System 4) ──────────────────────────────────────────────
export type RealmId =
  | 'mortal' | 'qi_condensation' | 'foundation' | 'core_formation'
  | 'nascent_soul' | 'soul_formation' | 'soul_transformation' | 'ascendant'
  | 'illusory_yin' | 'corporeal_yang'
  | 'nirvana_scryer' | 'nirvana_cleanser' | 'nirvana_fruit' | 'spirit_seizer'
  | 'true_immortal' | 'ancient' | 'paragon' | 'transcendence';

export type SubStage = 'early' | 'middle' | 'late' | 'peak';

export interface RealmDef {
  id: RealmId;
  name: string;
  nameCn: string;
  step: 0 | 1 | 2 | 3 | 4;
  order: number;
  absoluteTier: number; // P_player — the Absolute Cultivation Tier used in S_eff = max(0, L_world - P_player)
  lifespan: number;
  blurb: string;
  unlocks: string[];
  realityInfluence: string;
  qiGate: number;
  daoGate: number;
}

// ─── Dao (System 5) ─────────────────────────────────────────────────
export type DaoId =
  | 'sword' | 'saber' | 'body' | 'movement' | 'divine_sense'
  | 'formation' | 'alchemy' | 'talisman' | 'beast' | 'slaughter'
  | 'seal' | 'karma' | 'fire' | 'water' | 'wind' | 'lightning'
  | 'earth' | 'wood' | 'metal' | 'ice' | 'time' | 'space'
  | 'life' | 'death'
  // Hundred Daos (Er Gen's signature unexpected paths)
  | 'divination' | 'poison' | 'music' | 'painting' | 'calligraphy' | 'cooking'
  | 'artifact_refining' | 'dream' | 'fate' | 'reincarnation';

export type DaoObstacle =
  | 'heart_demon' | 'karma' | 'talent' | 'resource' | 'comprehension' | 'none';

export type ComprehensionStyle =
  | 'enlightenment' | 'slumber' | 'slaughter' | 'contemplation' | 'epiphany';

export interface DaoProfile {
  primary: DaoId;
  secondary?: DaoId;
  understanding: Partial<Record<DaoId, number>>;
  potential: 'low' | 'medium' | 'high' | 'defy_heaven';
  obstacle: DaoObstacle;
  obstacleDetail?: string;
  conflict: number; // 0..1 friction primary↔secondary
  baseConflict: number; // the original conflict before harmony items reduced it
  comprehensionStyle: ComprehensionStyle;
  revelations: { year: number; text: string; dao?: DaoId; gain?: number }[];
  forged?: boolean;
}

// ─── Cultivator (System 4) ──────────────────────────────────────────
export interface SoulState {
  independent: boolean;
  fragments: number;
  divineSenseRange: number;
}

// ─── Soul Power (hybrid derived-additive) ───────────────────────────
// S_sense = S_realm × (1 + ΣM_manual) + S_tempering
//   S_realm: scales automatically with breakthrough tier (Qi Cond ≈10, Nascent Soul ≈10,000, Ancient Immortal ≈1,000,000).
//   M_manual: multipliers from soul-cultivation manuals/arts (e.g. soul-eating).
//   S_tempering: flat additive from dark-arts tempering (soul-eating, forbidden refinement).
//                soft-capped at S_realm × (realm_order+1) so a low-realm cultivator can't infinitely out-soul immortals.
export interface SoulPower {
  manuals: { name: string; multiplier: number }[]; // M_manual list
  tempering: number;                                // S_tempering flat additive
  manualBonus: number;                              // cached ΣM_manual for display
}

// ─── Xianxia Status Matrix ──────────────────────────────────────────
// Custom status effects stacked on the cultivator's capability layer (not vanilla potions).
// Each has a severity 0..1 and a remaining duration (years; -1 = permanent until cured).
export type XianxiaStatusId =
  | 'soul_fracture'    // mental counter-strike; blinds divine sense, drops flying artifacts
  | 'sealed_meridians' // locks a % of max Qi; high-tier spells un-castable
  | 'qi_deviation'     // reckless cultivation / unpurified pills; casting drains health
  | 'heart_demon'      // karmic/tribulation backlash; raises breakthrough failure
  | 'karmic_stain'     // visible reputation marker; righteous sects blacklist
  | 'meat_jelly';      // invincible nagging companion; draws aggro, hijacks dialogue

export interface XianxiaStatus {
  id: XianxiaStatusId;
  severity: number;     // 0..1
  remaining: number;    // years (−1 = permanent until cured by a specific elixir/action)
  source: string;       // what caused it
}

// ─── Observation Chains (emergent discovery, no quest markers) ────────
// The player notices environmental clues that chain into discoveries.
// Example: birds migrating away → valley goes silent → spirit pressure detected
// → discovers ancient cultivator cave. No quest markers — just paying attention.
// Clues are "tagged" onto ambient events. When enough clues accumulate for a chain,
// the ConcealedObject is auto-perceived (the player "figured it out" through observation).
export type ClueCategory =
  | 'fauna_behavior'    // birds fleeing, beasts avoiding an area, unusual silence
  | 'qi_anomaly'        // localized Qi fluctuation, pressure differential, dead zone
  | 'environmental'     // withered plants, unnaturally cold ground, missing water
  | 'historical_echo'   // NPC mentions old tale, ruined marker, faded inscription
  | 'sensory'           // strange smell, feeling of being watched, hair-raising
  | 'temporal'          // time distortion, seasons out of sync, accelerated decay;

export interface ObservationClue {
  id: string;
  chainId: string;           // which chain this clue belongs to
  text: string;              // the narrative text (appears in world-pulse, no marker)
  category: ClueCategory;
  perceivedByRealm: RealmId; // minimum realm to notice this clue
  discovered: boolean;       // has the player encountered this clue yet?
  year: number;              // when this clue appeared
  hintStrength: number;      // 0..1 — how directly it points (0.1 = vague, 0.9 = obvious)
  // For "tagging" onto ambient events — if a generated ambient event matches
  // this clue's region and category, the clue text replaces the ambient text
  regionHint?: string;       // e.g., "northeast", "near the Verdant Valley"
  chainIndex: number;        // position in the chain (0 = first clue, N = final before discovery)
}

export interface ObservationChain {
  id: string;
  targetId: string;          // id of the ConcealedObject this chain reveals
  name: string;              // human-readable name for the discovery
  clues: ObservationClue[];  // ordered clues (index 0 → N)
  requiredClues: number;     // how many clues needed to auto-discover (e.g., 3 of 5)
  discovered: boolean;       // has the player pieced it together?
  discoveredYear?: number;   // when discovered
  // Narrative reward: the "eureka" moment text
  discoveryText: string;     // e.g., "The pieces click. The silence, the dead birds, the Qi drain — there is something beneath the Verdant Valley."
  // Difficulty: controls clue generation quality
  difficulty: 'easy' | 'medium' | 'hard' | 'legendary';
}

// ─── Ecology Chains (food-web cascades, emergent world events) ───────
// The ecosystem is not static. When one species shifts, cascades follow.
// Blood-Refinement Grass blooms → Blood Mosquitoes swarm → Blood Wolves follow
// → cultivators hunt wolves → conflict over territory. The player observes
// these cascades as world-pulse events and can choose to engage or avoid.
// NO quest markers — just the world's ecology playing out.
export type EcologyCascadeStage =
  | 'herb_bloom'          // a resource herb has a growth surge
  | 'prey_surge'          // a prey species population spikes (feeding on the herb)
  | 'predator_follow'     // a predator follows the prey surge
  | 'cultivator_response' // cultivators react (hunt, avoid, exploit)
  | 'faction_conflict'    // sects/npcs clash over the ecological shift
  | 'ecosystem_collapse'  // overshoot → crash → recovery
  | 'opportunity_spawn';  // the cascade creates a harvestable opportunity

export interface EcologyCascadeEvent {
  id: string;
  chainId: string;
  stage: EcologyCascadeStage;
  year: number;
  text: string;                    // narrative text (blends into world-pulse)
  perceivedByRealm: RealmId;
  severity: WorldEvent['severity'];
  // Population effects applied when this stage fires:
  populationEffects: { speciesName: string; delta: number }[];
  // Economic effects:
  priceEffects?: { resource: string; change: number }[];
}

export interface EcologyChain {
  id: string;
  name: string;                    // e.g., "Blood Spirit Cascade"
  description: string;             // the full cascade narrative
  regionId: string;
  // The species chain (in order): resource → prey → predator → apex
  speciesChain: string[];          // names matching BeastPopulation.species or herb names
  // Current cascade state
  active: boolean;                 // is this chain currently cascading?
  currentStage: number;            // index into the stages array
  stages: EcologyCascadeEvent[];   // pre-generated cascade events
  lastTriggerYear: number;         // when this chain last fired (cooldown)
  cooldownYears: number;           // minimum years between cascades of this type
  // Conditions to trigger the cascade:
  triggerChance: number;           // base per-tick probability (modified by world state)
}

// ─── Rumor Propagation (whispers that distort across hops) ──────────
// Rumors originate from world events and propagate through NPCs.
// Each hop distorts the content: numbers inflate, names get confused,
// wild conclusions are drawn. The user's example: 1 hop: "strange lights"
// → 4 hops: "Wang Lin has returned."
// Rumors appear as 'rumor' WorldEvents — the player must judge truth.
export type RumorSource =
  | 'world_event'       // spawned from a tick event (battle, discovery, ecology)
  | 'npc_invention'     // an NPC fabricated or exaggerated
  | 'player_action'     // the player did something noteworthy
  | 'ancient_echo';     // a very old rumor from world history

export interface Rumor {
  id: string;
  originText: string;           // what actually happened (truth)
  currentText: string;          // what people are saying now (distorted)
  source: RumorSource;
  sourceEventId?: string;       // id of the originating WorldEvent
  sourceEventText?: string;     // original event text
  hops: number;                 // how many times retold (0 = original)
  maxHops: number;               // rumor dies after this many hops
  originYear: number;
  lastSpreadYear: number;        // when it was last propagated
  regionId: string;              // where it originated
  currentRegionId: string;       // where it is now (moves with spread)
  distortion: number;            // 0..1 — how distorted the current text is
  believedBy: string[];          // NPC ids who believe this rumor
  credibility: number;           // 0..1 — how believable it seems (degrades with distortion)
  isDead: boolean;               // rumor has died (too distorted or outdated)
}

// ─── Spiritual Camouflage registry (special-blocks-only C_voxel) ────
// Standard terrain is C=0 (instantly skipped by the scan). Only "special" blocks carry
// a camouflage value, looked up against this lean registry (not per-block metadata).
export interface ConcealedObject {
  id: string;
  kind: 'inheritance_vault' | 'spirit_vein' | 'trap_array' | 'hidden_cave' | 'sealed_pavilion' | 'ancient_formation';
  name: string;
  regionId: string;
  camouflage: number;   // C_voxel — the S_sense required to perceive it
  reward?: string;      // what perceiving/revealing it yields
  perceived: boolean;   // has the player's divine sense pierced it yet
  coords?: string;      // symbolic location description
}

export interface Bottleneck {
  realm: RealmId;
  obstacle: DaoObstacle;
  progress: number;
  detail?: string;
}

export interface Cultivator {
  id: string;
  name: string;
  titles?: string[];
  realm: RealmId;
  subStage: SubStage;
  qi: number;
  qiMax: number;
  lifespanUsed: number;
  lifespanMax: number;
  lifespanBurned: number; // permanently burned by lifespan-cost techniques; restored only on major breakthrough
  karmicDebt: number; // 0..1; raised by karma/slaughter techniques → bad luck, ambushes, heart-demon risk
  dao: DaoProfile;
  techniques: string[];
  soul: SoulState;
  soulPower: SoulPower;     // hybrid derived S_sense = S_realm × (1+ΣM) + S_tempering
  statuses: XianxiaStatus[]; // active Xianxia status effects (soul_fracture, sealed_meridians, qi_deviation, heart_demon, karmic_stain, meat_jelly)
  bottlenecks: Bottleneck[];
  tribulationDebt: number;
  bodyConstitution: string;
}

// ─── NPC (System 6) ─────────────────────────────────────────────────
export type NpcState =
  | 'cultivating' | 'traveling' | 'trading' | 'hunting' | 'seclusion'
  | 'dueling' | 'researching' | 'fleeing' | 'recruiting' | 'teaching' | 'idle' | 'dead';

// ─── The Unified 13-Dimensional Soul Matrix ─────────────────────────
// EVERY living entity (human, spirit beast, demon, ancient) inherits this same
// 13-dimensional core vector. The difference between a feral wolf, a comedic
// spirit bird, a greedy young master, and a loyal ally is purely where their
// sliders fall. This unifies the behavioral engine across all entity classes.
export interface SoulMatrix {
  // Ethical & Core Drives
  altruism: number;        // 0..1 — protect the weak, sacrifice for companions
  ambition: number;        // crave rare resources, high-tier keys, breakthroughs
  paranoia: number;        // baseline suspicion; pre-emptive defensive strikes
  curiosity: number;       // analyze forbidden spells, player techniques, anomalies
  honor: number;           // adhere to sect oaths, contracts, karmic debts
  // Social & World Reputations
  faceObsession: number;   // vulnerability to insults/outbidding/trespass → retaliation
  heavenlyDefiance: number;// prefer plundering forbidden demonic matrices over orthodox
  mortalTether: number;    // attachment to mortal settlements, lineage, peaceful bases
  vengefulness: number;    // grudge persistence; controls anger decay rate
  // Comedic & Survival Nuances
  mortalityPanic: number;  // terror of death; rapid barriers + tactical retreat
  chaosConcoction: number; // probability of volatile mutations in alchemy/crafting
  shamelessness: number;   // beg for mercy, flatter, scam to bypass combat
  loyaltyTether: number;   // protective bonding; overrides fear/greed for companions
}

export type ArchetypeId =
  | 'philanthropic_hermit' | 'righteous_hypocrite' | 'obsessive_scholar'
  | 'arrogant_young_master' | 'uncompromising_seeker' | 'lovable_cowardly_prodigy'
  | 'kleptomaniac_spirit_bird' | 'nagging_meat_jelly' | 'loyal_culinary_glutton'
  | 'reincarnated_old_monster' | 'feral_beast' | 'generic_cultivator';

export interface ArchetypeDef {
  id: ArchetypeId;
  name: string;
  blurb: string;
  loopBehavior: string;
}

export interface Memory {
  who: string;
  what: string;
  where?: string;
  when: number;
  emotion: 'gratitude' | 'grudge' | 'fear' | 'respect' | 'love' | 'rivalry' | 'neutral';
  weight: number;
  // Cognition extension: tracks recall recency so decay can work
  lastRecalled?: number;
  recallCount?: number;
}

// ─── NPC Cognitive Simulation (B.5) ─────────────────────────────
// NPCs have an internal mental model: prioritized goals, decaying memory,
// expectation-of-world-state, and an internal monologue that occasionally
// surfaces as world-pulse dialogue. The SoulMatrix (13-D) drives WHAT they
// think about; the cognition system drives HOW they reason.
//
// Design principles:
// - Priority queues: NPCs always pursue their highest-urgency goal, not a
//   random state. Ambition > paranoia > curiosity drive goal urgency.
// - Memory decay: memories lose weight over time unless recalled. Recent,
//   emotionally charged, and weighty memories persist longer.
// - Expectation model: NPCs form expectations about the world (resource
//   availability, danger level, NPC trustworthiness). Violations create
//   surprise events and drive plan changes.
// - Internal monologue: each tick, NPCs generate a thought. High-urgency
//   thoughts occasionally leak into the world-pulse as dialogue. The
//   monologue is driven by SoulMatrix values — a paranoid NPC fixates
//   on threats, an ambitious NPC fixates on advancement.
// - Dialogue from thoughts: the monologue is NOT random flavor text. It is
//   causally grounded in the NPC's current state, recent memories, and
//   active goals. When it surfaces, the player can learn about the world
//   by eavesdropping on NPC cognition.

/** A single prioritized goal with urgency scoring */
export interface CognitiveGoal {
  id: string;
  description: string;            // e.g., "reach Foundation Establishment"
  category: 'cultivation' | 'resource' | 'social' | 'exploration' | 'survival' | 'revenge' | 'research';
  priority: number;               // 0..1 — base priority from SoulMatrix
  urgency: number;                // 0..1 — current urgency (time-pressure adjusted)
  progress: number;               // 0..1 — how close to completion
  blocked: boolean;               // something is preventing progress
  blockReason?: string;           // e.g., "need a Foundation Pill"
  deadline?: number;              // year by which this must be done (optional)
  subGoals?: string[];            // micro-steps the NPC is pursuing
}

/** An expectation the NPC holds about some aspect of the world */
export interface WorldExpectation {
  id: string;
  subject: string;                // e.g., "spirit_herb_availability" or "npc_trust:chen_xiao"
  expectedValue: number;          // 0..1 — what the NPC expects
  confidence: number;             // 0..1 — how certain
  lastUpdated: number;            // year of last update
  violationCount: number;         // how many times reality contradicted this
}

/** A single thought in the NPC's internal monologue */
export interface Thought {
  id: string;
  year: number;
  content: string;                // the thought text
  emotion: 'determination' | 'anxiety' | 'curiosity' | 'satisfaction' | 'frustration' | 'envy' | 'caution' | 'wonder' | 'boredom' | 'fear' | 'pride' | 'resignation';
  intensity: number;              // 0..1 — how strongly felt
  triggeredByGoal?: string;       // which goal triggered this thought (if any)
  triggeredByMemory?: string;     // which memory triggered this thought (if any)
  surfacedAsDialogue: boolean;    // did this thought leak into the world-pulse?
}

/** The full cognitive state of an NPC — attached to each NPC */
export interface NpcCognition {
  goals: CognitiveGoal[];                  // priority queue (sorted by urgency desc)
  expectations: WorldExpectation[];        // world model
  monologue: Thought[];                    // recent thoughts (capped at 20)
  dominantEmotion: string;                 // current emotional state (updated per tick)
  attentionSpan: number;                   // how many years before goals re-prioritize (3–12)
  attentionTimer: number;                  // years since last re-prioritization
  lastThoughtYear: number;                 // year of last generated thought
}

export interface Relationship {
  affinity: number;
  trust: number;
  debt: number;
  grudge: number;
  fear: number;           // added: for the loyalty Stability = Affinity + Fear×0.5 − ΔDaoHeart
  loyaltyStability: number; // running stability; <0 triggers desertion or betrayal
  daoHeartDelta: number;  // accumulated misalignment with the player's actions
}

export interface NPC extends Cultivator {
  layer: 1 | 2 | 3;
  soulMatrix: SoulMatrix; // the 13-D unified personality vector
  archetype?: ArchetypeId;
  personality: {
    temperament: string;
    values: string[];
    ambition: number;
    courage: number;
    loyalty: number;
    greed: number;
  };
  memory: Memory[];
  relationships: Record<string, Relationship>;
  goals: string[];
  fears: string[];
  enemies: string[];
  resources: string[];
  currentPlan: string;
  state: NpcState;
  schedule: string;
  factionId?: string;
  locationId: string;
  reputation: number;
  age: number;
  origin: string;
  destinyFlags?: string[];
  isProtagonist?: boolean;
  cognition?: NpcCognition;        // B.5 cognitive simulation (optional for backward compat)
}

// ─── Region aggregate (Layer 2) ─────────────────────────────────────
export type FactionType =
  | 'sect' | 'clan' | 'royal_court' | 'merchant_guild' | 'rogue_band' | 'demon_sect';
export type Alignment = 'righteous' | 'neutral' | 'demonic';

export interface RegionAggregate {
  id: string;
  name: string;
  kind: FactionType;
  alignment: Alignment;
  specialization: string;
  population: number;
  averageRealm: RealmId;
  resourceLevel: 'low' | 'medium' | 'high' | 'vast';
  internalConflict: 'low' | 'medium' | 'high';
  internalPolitics: string;
  currentGoal: string;
  enemies: string[];
  mood: string;
  futurePrediction: string;
  notableFigures: string[];
  peakRealm: RealmId;
  // L2 tick extensions (optional for migration safety)
  goalProgress?: number;           // 0..1 — progress toward currentGoal
  trend?: 'growing' | 'stable' | 'declining';
  warfareActive?: boolean;
  yearsSinceMajorEvent?: number;
  goalDeadline?: number;           // world year when goal must be achieved or abandoned
}

// ─── World macro (Layer 3) ──────────────────────────────────────────
export interface PendingMajorEvent {
  name: string;
  probability: number;
  eta: number;
  consequences: string[];
}

export interface WorldMacro {
  era: string;
  politicalState: string;
  majorConflict: string;
  beastActivity: 'calm' | 'normal' | 'increasing' | 'tide_imminent';
  cosmicInfluence: string;
  pendingMajorEvent?: PendingMajorEvent;
  // L3 tick extensions (optional for migration safety)
  yearsSinceBeastTide?: number;
  activeWarCount?: number;
  politicalStability?: number;       // 0..1 — derived from aggregate conflicts
}

// ─── Faction (System 7) ─────────────────────────────────────────────
export interface FactionGoal {
  goal: string;
  priority: 'low' | 'medium' | 'high';
  progress: number;
  deadline?: number;
}

export interface Faction {
  id: string;
  name: string;
  type: FactionType;
  alignment: Alignment;
  specialization: string;
  population: number;
  averageRealm: RealmId;
  peakRealm: RealmId;
  resources: { spiritStones: number; veins: number; herbs: number; artifacts: number; techniques: number };
  treasury: number;
  internalPolitics: string;
  goals: FactionGoal[];
  treaties: Record<string, { type: string; tension: number; expires?: number }>;
  reputation: number;
  history: { year: number; event: string }[];
  regionId: string;
}

// ─── Spirit Beast Ecology (System 8) ────────────────────────────────
export type BeastTier = 'normal' | 'spirit' | 'demon' | 'ancient';

export interface BeastPopulation {
  id: string;
  species: string;
  tier: BeastTier;
  rank: RealmId;
  population: number;
  birthRate: number;
  predationRate: number;
  huntingPressure: number;
  regionId: string;
  bloodline?: string;
  notes?: string;
}

export interface AncientBeast {
  id: string;
  name: string;
  species: string;
  state: 'sleeping' | 'stirring' | 'awake';
  influence: { weather?: string; qi?: string; migration?: string; geography?: string };
  regionId: string;
  stirringIn?: number;
}

// ─── Tamed Beast lifecycle (System 8 expansion: taming + sapience/shapeshift) ─
export type TamingTier = 'feral' | 'spiritual' | 'mythical';
export type TamingMethod = 'physical_dominance' | 'mental_coercion' | 'dao_recognition';

export interface TamedBeast {
  id: string;
  name: string;
  species: string;
  bloodline?: string;
  rank: RealmId;
  soul: SoulMatrix;          // beasts inherit the SAME 13-D soul matrix as humans
  archetype?: ArchetypeId;
  tamingTier: TamingTier;    // feral → spiritual → mythical (sapience/shapeshift)
  tamingMethod?: TamingMethod;
  qiStorage: number;         // gathers Qi through combat/meditation; gates ascension
  qiStorageCap: number;      // 10000 → spiritual; 100000 → mythical
  humanoidForm: boolean;     // mythical-tier beasts reshape into human form
  bonded: boolean;
  loyalty: number;           // 0..1; if it drops the beast may break the bond
  combatPower: number;       // contributes to player's effective power when present
  blurb: string;
}

// ─── History (System 9) ─────────────────────────────────────────────
export interface HistoryEvent {
  year: number;
  era?: string;
  event: string;
  consequences?: string[];
}

// ─── Technique (System 10) ──────────────────────────────────────────
export type ElementId =
  | 'fire' | 'water' | 'wind' | 'lightning' | 'earth' | 'wood' | 'metal'
  | 'ice' | 'light' | 'dark' | 'time' | 'space';
export type TechniqueGrade = 'mortal' | 'magical' | 'spirit' | 'immortal' | 'dao';
export type TechniqueIntent =
  | 'destruction' | 'protection' | 'healing' | 'concealment' | 'speed'
  | 'control' | 'summoning' | 'transformation' | 'execution';
export type TechniqueOrigin =
  | 'ancient-inheritance' | 'self-created' | 'sect-taught' | 'beast-derived' | 'forbidden' | 'divine';

export type VoxelGeometry =
  | 'narrow_slice' | 'descending_cylinder' | 'expanding_dome' | 'radial_burst'
  | 'locking_chunk' | 'rising_pillar' | 'tidal_wave' | 'starfall_cone' | 'frozen_field';

export type BlockOperator =
  | 'vaporize' | 'terraform' | 'conceal' | 'freeze' | 'seal' | 'shatter' | 'ignite' | 'purify';

export interface Technique {
  id: string;
  name: string;
  grade: TechniqueGrade;
  element: ElementId;
  dao: DaoId;
  realm: RealmId;
  intent: TechniqueIntent;
  origin: TechniqueOrigin;
  effects: string[];
  cost: { qi?: number; divineSense?: number; karma?: number; lifespan?: number };
  requirements: { realm: RealmId; daoUnderstanding: number; elementAffinity?: number };
  sideEffects: string[];
  comprehensionDifficulty: number;
  signature: string;
  creator?: string;
  category: string;
  voxelGeometry: VoxelGeometry; // element+dao → the shape of the spell's voxel footprint
  blockOperator: BlockOperator; // intent → what happens to voxels caught in the geometry
  scale: number; // origin+realm → magnitude (chunk radius / height / etc.)
  selfCreated?: boolean; // if origin=self-created: grade is evolutionary — scales with creator's realm, no need to hunt replacement manuals
  daoHarmony?: number; // 0..1; techniques/artifacts with daoHarmony reduce the cultivator's dao conflict
  suppressBypass?: number; // 0..1; dao-suppression effect: temporarily lowers local S_eff for the cast
  boundRealm?: RealmId; // for self-created: the realm at which it was last bound (scales up on breakthrough)
}

// ─── World events / pulse (System 12) ───────────────────────────────
export type WorldEventType =
  | 'sky_traffic' | 'tribulation' | 'battle' | 'beast_tide' | 'auction'
  | 'sect_recruitment' | 'marriage' | 'funeral' | 'pilgrimage' | 'ruin_opening'
  | 'cosmic' | 'faction_war' | 'price_shift' | 'revelation' | 'arrival' | 'rumor'
  | 'spirit_storm' | 'mana_depletion' | 'protagonist_breakthrough'
  | 'dialogue';

export interface WorldEvent {
  id: string;
  year: number;
  type: WorldEventType;
  severity: 'mundane' | 'notable' | 'significant' | 'major' | 'cosmic';
  text: string;
  perceivedByRealm: RealmId;
  locationId?: string;
  distance?: string;
}

// ─── Economy (System 13) ────────────────────────────────────────────
// Tiered resources: mortal tier (First Step, up to Ascendant) and premium tiers
// (Soul Formation+/Immortal+). Standard stones/herbs become irrelevant at the high tiers;
// the economy shifts to Celestial Jade, Immortal Pills, Dao Fruits — forcing exploration of
// higher-tier zones/planes.
export type ResourceTier = 'mortal' | 'celestial' | 'immortal' | 'dao';
export type ResourceId =
  | 'spirit_stone' | 'spirit_herb' | 'beast_core' | 'spirit_ore'
  | 'pill' | 'artifact' | 'talisman' | 'technique_scroll'
  // premium tiers:
  | 'celestial_jade' | 'immortal_pill' | 'dao_fruit' | 'origin_essence';

export interface EconomyEntry {
  price: number;
  supply: number;
  trend: 'up' | 'down' | 'stable';
  tier: ResourceTier;
}

export interface Economy {
  prices: Record<ResourceId, number>;
  supplies: Record<ResourceId, number>;
  trends: Record<ResourceId, 'up' | 'down' | 'stable'>;
  tiers: Record<ResourceId, ResourceTier>;
  entries: Partial<Record<ResourceId, EconomyEntry>>;
}

// ─── Region / Settlement (System 3) ─────────────────────────────────
export type RegionArchetype =
  | 'governed' | 'frontier' | 'wilderness' | 'forbidden' | 'contested' | 'sacred';

export interface Region {
  id: string;
  name: string;
  parentId: string;
  archetype: RegionArchetype;
  governingFactionId?: string;
  resources: string[];
  threats: string[];
  tradeRoutes: string[];
  commonTechniques: string[];
  professions: string[];
  rumors: string[];
  secretPowerSeekers: string[];
  foundingReason: string;
  historyEventIds: string[];
  hiddenInheritance?: { name: string; realm: RealmId; status: 'sealed' | 'opening' | 'claimed'; detail: string };
  qiArchitecture: string;
  mortalSurvival: string;
  beastThreat: string;
  sectAgreements: string;
  perceived: boolean;
  generated: boolean;
  vibe: string;
}

export type SettlementType =
  | 'village' | 'city' | 'sect_mountain' | 'sacred_peak' | 'ruin' | 'wilderness_camp' | 'port';

export interface Settlement {
  id: string;
  name: string;
  regionId: string;
  type: SettlementType;
  population: number;
  mortalRatio: number;
  governingFactionId?: string;
  spiritVein?: string;
  defenses: string;
  notableFigures: string[];
  historySnippet: string;
  perceived: boolean;
}

// ─── The World (root) ───────────────────────────────────────────────
export interface PlayerState extends Cultivator {
  knownRegionIds: string[];
  knownSettlementIds: string[];
  currentLocationId: string;
  mortalOrigin: string;
  inventory: Partial<Record<ResourceId, number>>;
  spiritStones: number; // liquid currency (mortal tier)
  celestialJade: number; // liquid currency (premium tier, post-Soul-Formation)
  currentWorldId: string; // the cosmic node the player currently inhabits
  knownWorldIds: string[]; // worlds the player has traveled to
  tamedBeasts: TamedBeast[]; // bonded spirit beasts (lifecycle: feral→spiritual→mythical)
  allies: string[]; // NPC ids in the player's party (loyalty-tracked)
  // ── Sustainable Flight (System: Movement) ──
  // Itemless flight is sustainable (ambient absorption vs drain). Sprint = pure Qi cost.
  mountedEntityId?: string;      // beast id or artifact id the player is currently mounted/piloting
  qiSenseActive: boolean;        // Ambient Qi Sense sub-mode (free toggle, range = divine sense radius)
  // ── Tribulation Parasitism ──
  // Stealing others' heavenly tribulations marks you. Your own future tribulations get harder.
  heavenlyEnmity: number;        // 0..1 — raised by parasitism; amplifies own tribulation damage + bolt count
  bodyTemperingGrade: number;    // 0..1 — permanent skeleton refinement from absorbed lightning (scales with bolts)
  // ── Karma Vision (snapshot pulse model) ──
  karmaVisionActive: boolean;    // currently in snapshot pulse mode (brief window after activation)
  karmaVisionCooldownYears: number; // remaining cooldown
  // ── Artifact Law Pressure: backlash cascade state ──
  bloodEssenceDamage: number;    // 0..1 — temporary max-HP reduction from meridian rupture cascade
  dantianShattered: boolean;     // catastrophic crippled debuff — locks Qi usage until meridian reconstruction
  meridianExpansion: Record<string, number>; // per-artifactId: 0..0.5 — permanent piloting efficiency from surviving backlash (ISSTH paragon perfection)
  // ── Dao Companion bonds ──
  daoCompanions: string[];       // NPC ids with whom the player has a dao companion bond
  // ── Tribulation Parasitism: duration-based ──
  // When the player enters a storm, they stay for a duration. Longer = more tempering + more damage.
  activeParasiteStormId?: string; // if set, the player is currently inside this storm
  stormRoundsSurvived: number;   // how many rounds the player has survived in the current storm
}

// A travelable higher world (Higher-World Whiplash). Traveling resets the power dynamic:
// player keeps realm but S_eff = max(0, L_world - P_player) can spike, slamming them down.
export interface ReachableWorld {
  id: string;
  name: string;
  lawTier: number; // L_world
  worldType: WorldType;
  maxRealm: RealmId;
  travelMethod: string; // 'Ancient Transfer Array' | 'Flying Treasure' | 'Ascension' | ...
  minRealmToReach: RealmId; // gate on perception
  blurb: string;
}

// Living skies + terrifying oceans (the world is never empty)
export interface SkyTraffic {
  id: string;
  hour: number;
  kind: 'flying_sword' | 'spirit_boat' | 'cultivator' | 'messenger_crane' | 'sect_patrol' | 'cloud_beast' | 'merchant_convoy' | 'immortal_battle' | 'tribulation' | 'spirit_storm' | 'enormous_unknown';
  text: string;
  perceivedByRealm: RealmId;
}

export interface OceanFeature {
  id: string;
  kind: 'leviathan' | 'sunken_sect' | 'moving_island' | 'living_whirlpool' | 'ancient_formation' | 'sea_civilization' | 'floating_mountain' | 'ghost_ship' | 'abyssal_trench';
  name: string;
  danger: RealmId; // approx realm needed to survive it
  blurb: string;
  perceived: boolean;
}

// ─── Contested Inheritances (System 11 expansion) ──────────────────
export interface InheritanceSeeker {
  npcId?: string;
  name: string;
  realm: RealmId;
  factionId?: string;
  progress: number; // 0..1 toward claiming
  disposition: 'rival' | 'ally' | 'neutral' | 'unknown';
}

export interface Inheritance {
  id: string;
  name: string;
  regionId: string;
  originFigure: string; // e.g. "the Jade-Void patriarch"
  peakRealm: RealmId;   // the inheritance's power ceiling
  status: 'sealed' | 'opening' | 'contested' | 'claimed';
  trials: string[];
  reward: { techniques: number; artifacts: number; resources: string[] };
  guardian: { name: string; realm: RealmId; weakness?: string };
  seekers: InheritanceSeeker[];
  openIn?: number; // years until it opens (if sealed)
  perceived: boolean;
  blurb: string;
}

// ─── Faction Wars (System 7 expansion) ──────────────────────────────
export interface FactionWar {
  id: string;
  attackerId: string;
  defenderId: string;
  cause: string;
  yearStarted: number;
  intensity: 'skirmish' | 'border_war' | 'total_war';
  status: 'ongoing' | 'ceasefire' | 'resolved';
  battles: { year: number; outcome: string; victor?: string }[];
  stakes: string; // e.g. "the Jade-Moon Spirit Vein"
}

// ─── Sentient Artifacts (System 11 expansion) ───────────────────────
export interface SentientArtifact {
  id: string;
  name: string;
  grade: TechniqueGrade;
  kind: 'sword' | 'flag' | 'seal' | 'pagoda' | 'mirror' | 'bell' | 'cauldron' | 'fan';
  spiritName: string;
  temperament: 'aloof' | 'bloodthirsty' | 'righteous' | 'mischievous' | 'ancient' | 'sorrowful';
  realm: RealmId;       // minimum realm to even approach
  ownerLog: { name: string; fate: string; year: number }[];
  currentOwner?: string;
  bonded?: boolean;     // bonded to the player
  compatibility: number; // 0..1 with the player's dao
  will: string;         // what it wants
  blurb: string;
}

// ─── Weapon/Artifact System (broader than SentientArtifact) ─────────
// In Er Gen's universe, weapons ARE a subset of artifacts. The hierarchy:
// Mortal → Magical Treasure → Spirit Treasure → Immortal Treasure (sentient) → Dao Treasure.
// This type covers weapon/defense/utility artifacts. SentientArtifact covers the high end.
export type ArtifactType = 'weapon' | 'defense' | 'utility' | 'sentient';
export type ArtifactSubtype =
  | 'flying_sword' | 'saber' | 'spear' | 'bow' | 'shield' | 'armor' | 'robe'
  | 'storage_bag' | 'spirit_boat' | 'token' | 'talisman_set' | 'pill_furnace'
  | 'communication' | 'array_disk' | 'seal_stamp';

export interface Artifact {
  id: string;
  name: string;
  nameCn?: string;
  type: ArtifactType;
  subtype: ArtifactSubtype;
  grade: TechniqueGrade;        // mortal → magical → spirit → immortal → dao
  elementAffinity?: ElementId;  // fire sword, ice shield, etc.
  attackPower: number;          // for weapons; 0 for non-weapon
  defenseRating: number;        // for shields/armor; 0 for non-defense
  durability: number;           // 0..100; Blood-Flame Salamander melts it!
  maxDurability: number;
  specialEffects: string[];     // armor penetration, soul damage, qi-channeling, etc.
  minRealm: RealmId;            // minimum realm to wield effectively (also defines spiritualWeight tier)
  weight: number;               // affects movement speed if equipped
  craftingMaterials?: string[]; // what drops are needed to forge this
  novel?: string;               // which Er Gen novel features this artifact type
  blurb: string;
  equipped: boolean;            // is the player currently wielding/wearing this
  // ── Soul Brand system (looted items carry the previous owner's spiritual signature) ──
  soulBrand?: SoulBrand;        // present on looted items; absent on crafted/found items
  // ── Artifact Law Pressure (meridian tearing when tier > player realm) ──
  bloodRefinementUntilYear?: number;  // if set, blood-refinement binding reduces spiritualWeight until this year
  spiritLineFeed?: number;       // spirit stones loaded into the overclocking buffer
}

// ─── Soul Brand (灵魂印记) ────────────────────────────────────────────
// Looted spatial items / high-tier weapons carry the previous owner's soul brand.
// While active, the brand acts as a signal booster for NPC karma tracking.
// The player must crack the brand (channel Qi + divine sense over time) to safely
// claim the item. High-tier brands require a soul-refining art, not just brute Qi.
// Multiple cracking methods exist (Ji Realm lightning, blood devouring, brute divine
// sense, karma severance, slaughter blood, seal arts, time reversal, undying blood).
// Multi-layer brands: an item passed through N owners has N layers; each layer is
// cracked with the same technique (just another round). Some techniques (Ji Realm)
// destroy all layers at once.
export type BrandCrackMethod =
  | 'ji_realm_lightning'    // Wang Lin's chaos lightning — destroys brand, damages item, stains karma
  | 'blood_devouring'       // Meng Hao's Blood Demon Grand Magic — consumes essence, gain Qi signature + Dao sliver
  | 'brute_divine_sense'    // low-tier only — pure soul-power throughput. Fails on Soul Formation+ (soul fracture)
  | 'karma_severance'       // Meng Hao's 7th Hex / Wang Lin's karma domain — severs karmic cord, owner feels it
  | 'slaughter_blood'       // Wang Lin's slaughter crystal — overwhelms with slaughter law, stains slaughter karma
  | 'seal_art'              // Meng Hao's 8th Hex — seals brand (cleanest, slowest, Paragon-tier)
  | 'time_reversal'         // Bai Xiaochun / Meng Hao time arts — rewinds brand out of existence (attracts time paragons)
  | 'undying_blood'         // Bai Xiaochun's Undying Codex — immortal blood overwhelms mortal-tier brands instantly
  | 'brand_fusion';         // accept the brand, become the new owner — inherit debts, gain Qi signature + Dao fragment

export interface SoulBrand {
  originalOwner: string;         // who imprinted this brand
  originalOwnerRealm: RealmId;   // the brand's tier (determines cracking difficulty)
  strength: number;              // 0..1 — how intact the CURRENT layer is (1 = untouched, 0 = fully cracked)
  crackingProgress: number;      // 0..1 — accumulated progress toward cracking current layer
  requiresSoulArt: boolean;      // true for Soul Formation+ brands — can't brute-force with Qi alone
  layers: number;                // how many layers (one per previous owner); each must be cracked separately
  currentLayer: number;          // which layer we're on (1-indexed; currentLayer > layers = fully cracked)
  fusedWith?: boolean;           // if true, the player fused with the brand instead of cracking it
}

// ─── Nested Cosmology: True World / Expanse Cosmos / Vast Expanse / Universe ──
// The universe is a nested hierarchy. Lower tiers (Singular Worlds) are physically
// and temporally contained inside higher tiers. Each tier has its own temporal dilation.
export type CosmicTier = 'singular_world' | 'true_world' | 'expanse_cosmos' | 'vast_expanse' | 'universe';

export interface CosmologyLayer {
  id: string;
  tier: CosmicTier;
  name: string;
  parentId: string | null;
  childIds: string[];
  worldLawIntensity: number;    // L_world for this tier
  temporalDilation: number;     // T_dilation: 1.0 mortal, 30.0 cave-world, etc. Internal ticks = base × T_dilation.
  // Vast-Expanse-specific:
  isVoid?: boolean;             // the Vast Expanse is a cold gray void
  voidFriction?: number;        // soul-depletion rate if you lack Ascendant+ capability
  vortexes?: VastExpanseVortex[];
  // Cosmos-boundary:
  barrierRealm?: RealmId;       // min realm to cross the boundary without a shield-script backlash
}

export interface VastExpanseVortex {
  id: string;
  name: string;
  kind: 'spatial_tear' | 'dead_star' | 'floating_ruin' | 'gravity_well' | 'river_of_time_eddy';
  pull: number;                 // gravitational pull strength
  reward?: string;              // what's inside (ruin blueprints, ancient items)
  danger: RealmId;              // realm needed to survive approaching
  perceived: boolean;
}

// ─── River of Time ──────────────────────────────────────────────────
// A physical stream through the Vast Expanse containing historical imprints of dead worlds.
// Cultivators with deep karma/divine-sense can pull ancient blueprints or phantom NPCs from it.
export interface RiverOfTimeEcho {
  id: string;
  originWorld: string;          // e.g. "the Jade-Void civilization", "a dead star"
  yearImprint: number;          // how ancient (negative)
  kind: 'item_blueprint' | 'technique_fragment' | 'phantom_npc' | 'historical_record';
  name: string;
  description: string;
  karmicCost: number;           // karmic debt added by pulling it
  sSenseRequired: number;       // divine sense needed to perceive the echo
  pulled: boolean;              // has the player pulled it
}

// ─── Spatial Dimension Folding ──────────────────────────────────────
// The Vast Expanse transitions between spatial dimensions. A player can "fold" into the
// 4th/5th dimension to bypass 3D barriers, becoming invisible to standard NPC raycasts.
export type SpatialDimension = '3d' | '4d' | '5d';

export interface DimensionFoldState {
  current: SpatialDimension;
  turnsRemaining: number;       // how many ticks/turns the player can stay folded
  sSensePerTickCost: number;    // divine-sense drain per turn while folded
}

// ─── Flora catalog (spirit herbs) ───────────────────────────────────
export type HerbGrade = 'mortal' | 'spirit' | 'earth' | 'heaven' | 'dao';
export type HerbEnvironment = 'forest' | 'wetland' | 'mountain' | 'desert' | 'tundra' | 'underground' | 'spirit_vein' | 'void' | 'ocean';
export type HerbGrowthStage = 'seed' | 'sprout' | 'young' | 'mature' | 'flowering' | 'fruiting' | 'dormant' | 'dead';

// Hidden herb properties — revealed based on player realm
export interface HerbHiddenProperties {
  age: number;              // years since sprouting (0 at seed/sprout)
  qiSaturation: number;     // 0..1 — how saturated with ambient Qi
  purity: number;           // 0..1 — how pure/untainted by environmental corruption
  mutation: string | null;  // e.g., "fire-aspected", "blood-soaked", "karmic-stained", "void-touched"
  medicinalPotency: number; // 0..1 — actual medicinal effectiveness (affected by purity + age + stage)
  karmicResidue: number;    // 0..1 — karmic weight absorbed from environment
}

// Realm-scaled herb perception: what the player sees depends on their realm.
// Mortals see weeds. Qi Condensation sees names. Foundation sees grade + effect.
// Core Formation+ sees hidden properties. Nascent Soul+ sees karmic residue + mutations.
export interface HerbPerception {
  displayName: string;       // what the player sees (mortal: "a weed", QC: "Qi-Grass")
  gradeRevealed: boolean;    // can the player see the herb grade?
  effectRevealed: boolean;   // can the player see the effect description?
  stageRevealed: boolean;    // can the player see the growth stage?
  properties: Partial<HerbHiddenProperties>; // revealed properties (empty for mortals)
}

export interface SpiritHerb {
  id: string;
  name: string;
  nameCn?: string;
  grade: HerbGrade;
  environment: HerbEnvironment;
  rarity: number;               // 0..1
  effect: string;               // e.g. "restores Qi", "cures qi deviation", "boosts wood dao"
  pillUse: string;              // what pill it brews into
  growthSeason: string;         // 'spring' | 'summer' | 'autumn' | 'winter' | 'year-round'
  regionId?: string;            // where it's found
  perceived: boolean;           // legacy field — kept for backward compat
  // Cultivation Depth (C): growth stages + hidden properties
  growthStage: HerbGrowthStage;
  stageYears: number;           // years spent in current growth stage
  totalAge: number;             // total years since seeding
  properties: HerbHiddenProperties;
}

// ─── Fauna food-web (deeper than BeastPopulation) ────────────────────
export interface BeastDrop {
  item: string;
  weight: number;               // 0..1 drop chance
  count?: string;               // "1", "2-4", etc.
  craftingUse?: string;         // what it brews/crafts into
}

export interface BeastSpecies {
  id: string;
  name: string;
  nameCn?: string;
  tier: BeastTier;
  baseRank: RealmId;
  bloodline?: string;
  habitat: HerbEnvironment;
  diet: 'herbivore' | 'carnivore' | 'omnivore' | 'spiritivore' | 'carrion' | 'cosmic' | 'conceptual';
  preysOn: string[];            // species ids this beast eats
  preyedUponBy: string[];       // species ids that eat this
  temperament: 'docile' | 'territorial' | 'aggressive' | 'cunning' | 'ancient_dormant' | 'passive_cataclysmic';
  innateTechnique?: string;     // a technique the beast can use
  value: string;                // what harvesting it yields (cores, hide, bloodline essence)
  populationStatus: 'thriving' | 'stable' | 'declining' | 'endangered' | 'extinct';
  // Expanded fields for the 5-layer bestiary:
  layer: number;                // which cosmology layer (1-5) this creature belongs to
  behaviorScript: string;       // named AI loop (Flee State, Pack Packets, Core Siphon Piercing, Reality Fracture Flap, etc.)
  voxelScale?: string;          // e.g. "0.5×0.4×0.6 blocks" or "world-layer asset"
  drops: BeastDrop[];           // modular loot table with weights + crafting-use
  aggroType: string;            // Passive_Flee, Defensive_AoE, Stealth_Ambush, Soul_Drain_Swarm, etc.
  destinedCompanion?: boolean;  // Wang Lin's mosquito — special taming path
  gastroRealm?: string;         // Moongazer-type: has an instanced sub-dimension inside its body
  dualForm?: { kun: string; peng: string }; // Kunpeng-type: transforms between aquatic/spatial forms
  category: BeastCategory;      // one of 4 ecological categories
  novel?: string;               // which Er Gen novel the creature originates from
}

// ─── The 4 Ecological Categories (from the 136-species master record) ──
export type BeastCategory =
  | 'spatial_cosmic_leviathan'  // void/cosmic entities; no standard pathfinding; map hazards or high-tier transport
  | 'bloodline_mutant'          // standard ground/sky fauna; drop Inner Dans + bloodline essence
  | 'alchemical_chimera'        // artificial synthesis strains; soul-weaving / mutation arrays
  | 'conceptual_law_bound';     // non-biological; born from shattered heavenly laws; need Divine Sense / law counters

// ─── Opportunity engine (the bustling world) ────────────────────────
// Random opportunities, encounters, and emergent events that make the world feel alive
// and full of chances — not scripted quests, but things that *can* happen.
export type OpportunityKind =
  | 'wandering_merchant' | 'distressed_cultivator' | 'rare_herb_bloom' | 'beast_migration'
  | 'auction_notice' | 'sect_recruiter' | 'inheritance_rumor' | 'karmic_encounter'
  | 'spirit_vein_surge' | 'tribulation_witness' | 'bandit_ambush' | 'ancient_array_awakening'
  | 'dao_companion_meeting' | 'fallen_cultivator_loot' | 'mortal_village_in_peril'
  | 'dimensional_rift' | 'phantom_echo' | 'mysterious_pill_recipe' | 'bloodline_awakening'
  // 8 new cosmic/metaphysical events (27 total):
  | 'heaven_trampling' | 'god_corpse' | 'dao_debate' | 'devil_awakening'
  | 'memory_flash' | 'tribulation_theft' | 'debt_collection' | 'qi_deviation_zone';

export interface Opportunity {
  id: string;
  kind: OpportunityKind;
  title: string;
  description: string;
  regionId: string;
  minRealm: RealmId;            // realm needed to safely engage
  reward: string;               // what engaging yields
  risk: string;                 // what engaging risks
  expiresIn: number;            // years until it fades
  taken: boolean;
  declined: boolean;
  perceived: boolean;
  // Layer-gating (the OpportunityRegistry pattern):
  minWorldLaw: number;          // only spawns in layers with L_world >= this
  maxWorldLaw: number;          // only spawns in layers with L_world <= this
  baseWeight: number;           // probability weight (modified by player luck/karma)
  karmicResonance?: 'pure' | 'tainted' | 'heavy' | 'atrocious' | 'any'; // only triggers if player's karma tier matches
  chainsInto?: OpportunityKind; // event chain: engaging this can spawn a follow-up event later
}

// ─── The Universe (the infinite all) ────────────────────────────────
// At the Universe scale, coordinate data types transition to BigInteger tracking. This layer
// facilitates end-game progression when the player transcends world boundaries and can
// manipulate whole cosmoses as single data entries. The River of Space and Time flows here.
export interface CosmicRipple {
  id: string;
  source: string;               // what caused the ripple (e.g. "a cataclysm in the Mountain & Sea Realm")
  effect: string;               // how it alters generation of new world sectors
  year: number;
}

// ─── Sustainable Flight Profile (derived display snapshot) ─────────
// Not persisted; computed on demand from player realm + dao + region + mounts.
// Net Qi drain = flightCost − ambientAbsorption. If ambient ≥ cost, flight is free.
export interface FlightProfile {
  canFly: boolean;                  // requires Foundation+ (Qi Condensation can't levitate sustainably)
  ambientQiDensity: number;         // 0..1 — from current region's spiritVeinDensity (0 in dead zones)
  ambientAbsorption: number;        // Qi/year absorbed while flying (realm × density)
  flightCost: number;               // Qi/year baseline flight drain
  netDrain: number;                 // flightCost − ambientAbsorption (negative = sustainable, refills Qi)
  cruiseSpeed: number;              // blocks/year baseline (realm-driven)
  purityMultiplier: number;         // 0.85..1.4 — Perfect Gold Core = 1.3, flawed breakthrough = 0.9
  daoResonance: number;             // 0.6..1.8 — boost if primary dao matches region's dominant element
  constitutionBonus: number;        // 1.0..2.5 — special physiques (Astral Lightning Body, Void Body, etc.)
  sprintReady: boolean;             // cooldown elapsed AND core stress below crack threshold
  sprintSpeed: number;              // cruiseSpeed × 8 (one-shot velocity burst)
  sprintCoreStress: number;         // stress added per sprint (0.18 baseline)
  mountMultiplier: number;          // 1.0 default; Kunpeng ~ 50, Void Sun-Chaser Sword ~ 22
  mountName?: string;
  effectiveTopSpeed: number;        // cruiseSpeed × purity × resonance × constitution × mount
  deadZone: boolean;                // ambient = 0 → flight drains reserves
}

// ─── Tribulation Parasitism events ────────────────────────────────
// When a powerful NPC is about to break through, the server spawns a hijackable storm.
// The player may stand in the strike zone to absorb bolts (body tempering + heavenly enmity)
// or devour them with a soul-refining art (Dao insight). The NPC's breakthrough fails.
export interface ParasiteTribulation {
  id: string;
  targetNpcId: string;
  targetName: string;
  targetRealm: RealmId;              // the realm the NPC is breaking into (caps body-tempering reward)
  regionId: string;
  intensity: 'minor' | 'major' | 'world-shaking';  // bolt count + damage
  boltsTotal: number;
  boltsAbsorbed: number;             // by the player (derived from rounds survived)
  bodyTemperingReward: number;       // 0..1 — skeleton refinement PER ROUND; scales with intensity
  daoInsightReward: number;          // 0..1 — Dao understanding gain per round if devouring
  expiresInYears: number;            // window to intervene before the NPC completes the breakthrough
  resolved: boolean;
  resolution?: 'absorbed' | 'devoured' | 'npc_survived' | 'expired' | 'protected' | 'respected' | 'player_fled';
  // ── Duration-based mechanics ──
  maxRounds: number;                 // total rounds the storm lasts (3-8 based on intensity)
  currentRound: number;              // how many rounds have elapsed
  damagePerRound: number;            // Qi damage per round survived (scales with intensity + body tempering reduction)
  // ── NPC wishes (dao companion bond path) ──
  wish?: 'help' | 'stay_back';       // the NPC's expressed wish when the storm spawns
  wishRespected?: boolean;           // did the player listen (help if asked, stay back if told)
}

// ─── Lightning Hunters (heavenly debt pursuers) ─────────────────────
// At high heavenly debt, the karmic ledger points to the player. Lightning-element
// NPC cultivators and heavenly emissaries are drawn to them. This is why Wang Lin
// is constantly hunted — the heavens' rebalancing mechanism spawns pursuers.
export interface LightningHunter {
  id: string;
  name: string;
  realm: RealmId;                    // scales with player's realm
  element: 'lightning' | 'heavenly'; // lightning cultivators vs heavenly emissaries
  regionId: string;
  expiresInYears: number;            // how long until they track you down
  resolved: boolean;
  resolution?: 'ambushed' | 'evaded' | 'slain' | 'expired';
}

// ─── AFK Catch-Up (B.6: world-sim-tick) ─────────────────────────────
// When the player is away, the world continues. On return, the world has
// advanced N simulation years. Events are categorized into an immersive
// narrative summary so the player feels they "opened their eyes from meditation."
export type AfkHighlightCategory = 'breakthrough' | 'death' | 'ecology' | 'faction' | 'rumor' | 'cosmic' | 'opportunity' | 'arrival' | 'other';

export interface AfkHighlight {
  category: AfkHighlightCategory;
  text: string;
  year: number;
  severity: WorldEvent['severity'];
}

export interface AfkSummary {
  yearsPassed: number;             // simulation years that elapsed while AFK
  realMinutesAway: number;         // real-world minutes the player was away
  narrative: string;               // immersive opening narrative
  highlights: AfkHighlight[];      // categorized event highlights (capped at 15)
  npcChanges: {                   // notable NPC state changes
    breakthroughs: string[];       // "Name → Realm"
    deaths: string[];              // "Name, title, age"
    arrivals: string[];            // "Name — origin"
  };
  ecologyShift: string | null;     // one-line ecology summary if changes occurred
}

export interface AfkConfig {
  yearsPerHour: number;          // sim years per real hour (default: 2)
  maxCatchUpYears: number;       // hard cap per catch-up (default: 50)
  minAfkMinutes: number;         // minimum absence to trigger catch-up (default: 5)
}

export interface World {
  id: string;
  seed: number;
  currentYear: number;
  reality: RealityProfile;
  lawTier: number; // L_world — the Absolute World Law Tier (numeric) for S_eff = max(0, L_world - P_player)
  cosmicTree: CosmicNode[];
  reachableWorlds: ReachableWorld[]; // higher worlds the player may travel to (whiplash)
  player: PlayerState;
  regions: Region[];
  settlements: Settlement[];
  npcs: NPC[];
  aggregates: RegionAggregate[];
  worldMacro: WorldMacro;
  factions: Faction[];
  ecology: { populations: BeastPopulation[]; ancients: AncientBeast[] };
  ecologyChains: EcologyChain[];    // food-web cascades (ecosystem shifts → emergent events)
  rumors: Rumor[];               // propagated whispers that distort across NPC hops
  history: HistoryEvent[];
  events: WorldEvent[];
  economy: Economy;
  techniques: Technique[];
  sky: SkyTraffic[]; // living skies feed
  ocean: OceanFeature[]; // terrifying oceans
  protagonists: NPC[]; // destiny-flagged legendary NPCs advancing on the world-clock
  inheritances: Inheritance[]; // contested inheritance sites
  wars: FactionWar[]; // ongoing faction wars
  artifacts: SentientArtifact[]; // sentient treasures that choose owners
  weapons: Artifact[];           // broader weapon/defense/utility artifacts (equippable)
  // Alchemy + Constitution + World Memory
  alchemySkill: number;          // 0..1 — the player's alchemy proficiency (grows with practice)
  refiningSkill: number;         // 0..1 — the player's artifact-refining proficiency (grows with practice)
  formationSkill: number;        // 0..1 — the player's formation mastery (grows with practice)
  deployedFormations: import('./formations').DeployedFormation[]; // active formations the player has placed
  talismanSkill: number;        // 0..1 — the player's talisman inscription mastery (grows with practice)
  talismans: import('./talismans').InscribedTalisman[];      // inscribed talismans in the player's inventory
  constitution: import('./constitution').ConstitutionState; // the player's evolving constitution (5 layers)
  worldMemory: import('./world-memory').WorldMemory[];      // persistent significant-event memory
  // Er Gen depth systems
  karmicThreads: import('./ergen-depth').KarmicThread[];    // karmic bonds visible at Nascent Soul+
  ancientClan: import('./ergen-depth').AncientClanState;    // Ancient God/Devil/Demon body-cultivation parallel path
  daoHeart: import('./ergen-depth').DaoHeartState;          // the cultivator's fundamental resolve
  cultivationArt: import('./ergen-depth').CultivationArt;   // the foundational cultivation method (功法)
  caveWorld: import('./ergen-depth').CaveWorld;             // personal pocket dimension inside storage treasure
  pastLifeMemories: import('./ergen-depth').PastLifeMemory[]; // recovered past-life memory fragments
  avatars: import('./ergen-depth').AvatarState[];           // soul-split avatars (Nascent Soul+)
  concealedObjects: ConcealedObject[]; // Spiritual Camouflage registry (special-blocks C_voxel)
  observationChains: ObservationChain[]; // emergent discovery chains (clue accumulation → hidden object revealed)
  // Nested cosmology + Vast Expanse + River of Time + spatial folding
  cosmologyLayers: CosmologyLayer[];     // the nested hierarchy (singular → true → cosmos → vast expanse → universe)
  riverOfTime: RiverOfTimeEcho[];        // ancient imprints pullable by karma/divine-sense
  dimensionFold: DimensionFoldState;     // the player's current spatial-dimension state
  // Bustling-world content
  herbs: SpiritHerb[];                   // flora catalog (spirit herbs by grade/environment)
  beastSpecies: BeastSpecies[];          // fauna food-web (deeper than BeastPopulation)
  opportunities: Opportunity[];          // random emergent chances
  cosmicRipples: CosmicRipple[];         // universe-scale cross-over events (River of Space and Time)
  parasiteTribulations: ParasiteTribulation[]; // hijackable NPC breakthrough storms (Tribulation Parasitism)
  lightningHunters: LightningHunter[];     // heavenly-debt pursuers (spawn at high heavenlyDebt)
  localAntagonist?: LocalAntagonist;        // per-world Heaven-Will antagonist (NOT universal Allheaven)
  worldExit?: WorldExitFramework;           // authorized/illegal exit methods for this sealed world
  karmicMemories: KarmicMemory[];           // persistent interaction memories (for protagonist AI recall)
  dimensionalManaPool: DimensionalManaPool; // finite, drainable world energy
  protagonistManifestations: ProtagonistManifestation[]; // the 6 protagonist clones (one per world, Tier 2 when player is near)
  mythicSimulations: MythicSimulationState[]; // Tier 0 background sims for far-away protagonists
  regionalSimulations: RegionalSimulationState[]; // Tier 1 sims for nearby-region protagonists
  combatIntentLog: CombatIntentEvent[];     // recent combat intent events (for accident vs betrayal detection)
  lastTick: number;
  lastActiveTime: number;            // real-time ms when player was last active (AFK detection)
  afkConfig: AfkConfig;              // AFK catch-up time-scale settings
  log: { year: number; text: string }[];
}

// ─── Local Antagonist (per-world Heaven-Will) ──────────────────────
// RESEARCH-14 confirmed: Allheaven is ISSTH-specific, NOT universal. Each novel's
// world has its own Heaven-Will antagonist. Kill it to unlock Transcendence.
export interface LocalAntagonist {
  id: string;
  name: string;                    // e.g., "Seven-Colored Daoist" (Cave World), "Allheaven" (Mountain & Sea), "Gravekeeper" (Heavenspan)
  title: string;                   // e.g., "The Heaven-Will of the Cave World"
  realm: RealmId;                  // the antagonist's cultivation realm (very high)
  worldId: string;                 // which world this antagonist rules
  isAllheaven: boolean;            // true ONLY for ISSTH's Mountain & Sea Realm
  defeatedBy?: string;             // who defeated them (if anyone) — "Wang Lin" etc.
  defeatUnlocks: string;           // what defeating them unlocks (e.g., "Transcendence Gate")
  blurb: string;
}

// ─── World Exit Framework ──────────────────────────────────────────
// RESEARCH-8 confirmed: Wang Lin left the Cave World via karmic completion (ch.901)
// after killing the Seven-Colored Daoist (becoming the new world-owner).
// Authorized exits: karmic completion, world-ownership transfer, heaven-sanctioned
// ascension, ancient transfer arrays. Illegal exits: domain breach, spatial tearing,
// forced ascension. Free exit: only at Transcendence.
export interface WorldExitFramework {
  worldId: string;
  isSealed: boolean;               // true if the world is sealed (Cave World, Heavenspan, Stone Stele World)
  sealReason?: string;             // e.g., "Seven-Colored Daoist's artificial farm", "Luo Tian's palm"
  authorizedExits: ExitMethod[];   // heaven-harmonious exits
  illegalExits: ExitMethod[];      // heaven-defying, world-damaging exits
  worldWillResistance: number;     // 0..1 — how hard the world fights illegal exit
  karmicCompletionRequired: boolean; // must resolve all karmic threads before authorized exit
}

export interface ExitMethod {
  id: string;
  name: string;                    // e.g., "Karmic Completion Ascension", "Ancient Transfer Array", "Domain Breach"
  type: 'authorized' | 'illegal';
  description: string;
  minRealm: RealmId;               // minimum realm to use this exit
  cost?: string;                   // e.g., "All karmic threads resolved", "World damage", "Tribulation"
}

// ─── Cultivation State (not XP) ────────────────────────────────────
// Per user spec: cultivators don't level via XP. They have a Cultivation State
// that changes through events (breakthroughs, setbacks, insights, heart demons).
export interface CultivationState {
  realm: RealmId;
  subStage: SubStage;
  daoComprehension: Partial<Record<DaoId, number>>; // 0..100% per dao
  foundationStability: number;     // 0..1 — how solid the foundation is (Perfect = 1.0)
  currentBottleneck?: string;      // e.g., "Soul Transformation Barrier", "Heart Demon"
  tribulationRisk: 'none' | 'low' | 'medium' | 'high' | 'extreme';
  heartDemonIntensity: number;     // 0..1 — current heart demon pressure (Ptt path = highest)
  lastBreakthroughYear?: number;
  failedBreakthroughs: number;     // setbacks are real
}

// ─── Karmic Memory ─────────────────────────────────────────────────
// Every major interaction creates a memory. Protagonists recall these in dialogue.
// "You once stood between me and death in the Ancient Void."
export interface KarmicMemory {
  id: string;
  entityId: string;                // who has this memory (NPC id)
  targetId: string;                // who the memory is about (player id or NPC id)
  event: string;                   // what happened
  importance: 'minor' | 'notable' | 'major' | 'legendary';
  emotion: 'gratitude' | 'grudge' | 'fear' | 'respect' | 'love' | 'rivalry' | 'neutral';
  daoEffect?: string;              // e.g., "Trust increased", "Respect gained"
  year: number;
  locationId?: string;
  futureReference: boolean;        // will this be referenced in future dialogue?
}

// ─── Intent Recognition (combat context) ───────────────────────────
// The engine tracks not just "what happened" but "why." Distinguishes accident
// from betrayal. AoE that clips Wang Lin during a Void Beast fight = accident.
// Repeated targeted attacks with no enemy nearby = betrayal.
export type CombatIntent = 'duel_declared' | 'cooperative_combat' | 'unprovoked_attack' | 'accident' | 'betrayal';

export interface CombatIntentEvent {
  id: string;
  attackerId: string;
  targetId: string;
  intent: CombatIntent;
  damageDealt: number;
  contextEnemyId?: string;         // was there a common enemy? (cooperative combat check)
  spiritualRestraintActive: boolean; // was restraint on? (reduces intent severity)
  repeatedAttacks: number;         // how many times this attacker has hit this target recently
  year: number;
}

// ─── Dimensional Mana Pool (finite, drainable) ─────────────────────
// RESEARCH-8 (user spec): each dimension has finite spiritual energy. High-tier
// cultivators drain it. Depletion → regen stops, breakthrough chance drops.
export interface DimensionalManaPool {
  worldId: string;
  maxMana: number;                 // total spiritual capacity
  currentMana: number;             // current level
  drainRate: number;               // per-tick drain from all cultivators
  replenishRate: number;           // natural replenish per tick
  depletedThreshold: number;       // below this = depleted state (e.g., 0.2 * max)
  isDepleted: boolean;             // computed: currentMana < depletedThreshold
}

// ─── 3-Tier LOD Simulation (Mythic / Regional / Reality) ───────────
// Per user spec: protagonists simulate at different detail levels based on
// player proximity. Tier 0 = goal/probability only. Tier 1 = objective tracking.
// Tier 2 = full entity. This prevents 10000 NPC AIs while keeping history alive.
export type SimulationTier = 0 | 1 | 2;

// Tier 0: Mythic Simulation (very far away — no entity, no chunks)
// Simulates goals, probabilities, destiny pressure, major events. Produces history.
export interface MythicSimulationState {
  protagonistId: string;           // which protagonist (e.g., 'wang_lin')
  currentGoal: string;             // e.g., "Search Ancient Void"
  timeElapsed: number;             // years since last Tier 2 encounter
  possibleEvents: MythicEvent[];
  generatedHistory: GeneratedHistoryEvent[];  // accumulated background events
  cultivationState: CultivationState;         // their current cultivation (not XP)
}

export interface MythicEvent {
  description: string;
  probability: number;             // 0..1
  outcome: string;
  growthType: 'inheritance' | 'treasure' | 'technique' | 'setback' | 'relationship';
}

export interface GeneratedHistoryEvent {
  year: number;
  event: string;
  cultivationChange?: string;      // e.g., "Late Soul Transformation", "Failed breakthrough"
  relationshipChange?: string;     // e.g., "Friend → Rival"
}

// Tier 1: Regional Simulation (same dimension, nearby region)
// Tracks approximate location, objective, companions, enemies, resources.
export interface RegionalSimulationState {
  protagonistId: string;
  approximateLocationId: string;   // region/settlement id
  currentObjective: string;
  activityDistribution: { activity: string; weight: number }[]; // e.g., Traveling 60%, Searching 25%, Combat 15%
  companions: string[];            // NPC ids traveling with them
  enemies: string[];               // NPC ids pursuing them
  resourcesGathered: number;       // spirit stones equivalent
}

// Tier 2: Reality Simulation (player nearby — full entity)
// The protagonist exists as a full NPC with AI, combat, dialogue, inventory.
// This is the existing NPC type, extended with protagonist-specific fields.
export interface ProtagonistManifestation {
  protagonistId: string;           // canonical id (e.g., 'wang_lin', 'meng_hao')
  canonicalName: string;           // "Wang Lin"
  cloneId: string;                 // unique to this manifestation
  worldId: string;                 // which world they manifest in
  trueSelfRealm: RealmId;          // the real protagonist's realm (frozen/transcendent in IAC)
  localRealm: RealmId;             // the clone's current local realm (starts from scratch)
  independence: number;            // 0..1 — how autonomous (high for protagonists)
  currentGoal: string;
  cultivationState: CultivationState;
  soulMatrix: SoulMatrix;          // fixed per novel characterization
  combatStrategyProfile?: string;  // AI behavior tree id
  ancestralAnchorId: string;       // home location for reconstitution
  isReconstituting: boolean;       // mid-reconstitution flag
  relationshipWithPlayer: ProtagonistRelationship;
  spatialBag: ProtagonistInventoryEntry[];  // their local inventory (canonical + sandbox)
  dailyRoutine?: DailyRoutineSchedule;
}

export interface ProtagonistRelationship {
  recognition: number;             // 0..1 — the core recognition track (bond events + time)
  trust: number;                   // 0..1
  respect: number;                 // 0..1
  fear: number;                    // 0..1
  affinity: number;                // 0..1
  grudge: number;                  // 0..1
  karmicConnection: number;        // 0..1
  sharedHistory: string[];         // summary of shared events
  favorsOwed: number;              // net favors (player owes protagonist)
  grievances: string[];            // specific grievances
  philosophicalAlignment: number;  // 0..1 — Dao compatibility
  bondedEvents: BondEvent[];       // the 5-7 events that raised recognition
  mirroredItems: string[];         // canonical items already mirrored to the player
}

export interface BondEvent {
  id: string;
  type: 'saved_in_combat' | 'shared_breakthrough' | 'brought_resource' | 'respected_wish' | 'defended_tribulation' | 'dao_debate' | 'formal_duel' | 'cooperation';
  year: number;
  recognitionGained: number;       // how much this event raised recognition
  description: string;
}

export interface ProtagonistInventoryEntry {
  itemId: string;                  // ResourceLocation-style id
  isCanonical: boolean;            // true = wiki-sourced unique; false = sandbox loot
  count: number;
  unlockThreshold: number;         // 0..1 recognition required to mirror (canonical only)
  alreadyMirrored: boolean;        // has the player already mirrored this?
}

export interface DailyRoutineSchedule {
  meditationHours: number;         // cross-legged Qi gathering
  marketVisits: number;            // per week
  lectureAttendance: number;       // per month
  teaHouseRest: number;            // hours per week
  combatTraining: number;          // hours per week
  personalityModifiers: Partial<Record<keyof SoulMatrix, number>>; // how soul matrix affects routine
}
