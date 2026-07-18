// src/lib/sim/formations.ts
// Property-based formation system. The third of Er Gen's Four Great Arts (阵法).
// Formation flags/stones have properties. Formations emerge from property combos.
// A master formation master can create arrays that no pattern describes.
// "Flags planted. Qi flows. Heaven and Earth shift."

import type { RealmId } from './types';

// ─── Formation Properties ──────────────────────────────────────────
// Every formation component has these. The formation's effect is resolved from the combo.

export type FormationProperty =
  // Spatial traits
  | 'barrier' | 'trap' | 'illusion' | 'concealment' | 'teleport'
  | 'binding' | 'suppression' | 'amplification' | 'isolation'
  // Elemental aspects
  | 'fire_aspect' | 'water_aspect' | 'wind_aspect' | 'earth_aspect' | 'metal_aspect'
  | 'lightning_aspect' | 'ice_aspect' | 'dark_aspect' | 'light_aspect' | 'wood_aspect'
  // Spiritual traits
  | 'qi_gathering' | 'spirit_weaving' | 'soul_locking' | 'dao_resonant'
  | 'karmic_anchor' | 'mind_pacifying'
  // Special
  | 'self_sustaining' | 'time_dilation' | 'space_folding' | 'blood_drinking'
  | 'cursed' | 'blessed' | 'sentient_seed' | 'leeching'
  | 'defensive' | 'offensive' | 'utility';

export type FormationComponentType = 'flag' | 'stone' | 'pillar' | 'disk' | 'jade_slip';

export interface FormationComponent {
  name: string;
  nameCn?: string;
  componentType: FormationComponentType;
  properties: FormationProperty[];
  potency: number;       // 0..1 — component power
  stability: number;     // 0..1 — formation stability under stress
  grade: string;
  novel?: string;
  blurb?: string;
}

// ─── Formation Component Catalog (all from Er Gen novels) ─────────────────

export const FORMATION_COMPONENTS: Record<string, FormationComponent> = {
  // ── Flags (旗) — the most common formation component ──
  'Spirit-Gathering Flag': {
    name: 'Spirit-Gathering Flag', nameCn: '聚灵旗', componentType: 'flag',
    properties: ['qi_gathering', 'amplification', 'spirit_weaving', 'utility'],
    potency: 0.25, stability: 0.85, grade: 'mortal',
    blurb: 'A flag embroidered with spirit-gathering sigils. When planted, it draws ambient Qi to a single point. The most basic formation component.',
  },
  'Barrier Flag': {
    name: 'Barrier Flag', nameCn: '屏障旗', componentType: 'flag',
    properties: ['barrier', 'defensive', 'earth_aspect', 'utility'],
    potency: 0.3, stability: 0.8, grade: 'mortal',
    novel: 'Renegade Immortal',
    blurb: 'A flag that projects a defensive barrier when planted. Wang Lin used barrier flags to protect his cave dwelling.',
  },
  'Illusion Flag': {
    name: 'Illusion Flag', nameCn: '幻阵旗', componentType: 'flag',
    properties: ['illusion', 'concealment', 'mind_pacifying', 'utility'],
    potency: 0.35, stability: 0.7, grade: 'magical',
    novel: 'ISSTH',
    blurb: "Meng Hao's restriction flags could project illusions that deceived even Foundation Establishment cultivators.",
  },
  'Trap Flag': {
    name: 'Trap Flag', nameCn: '困阵旗', componentType: 'flag',
    properties: ['trap', 'binding', 'suppression', 'offensive'],
    potency: 0.4, stability: 0.65, grade: 'magical',
    novel: 'All Novels',
    blurb: 'A flag that creates a trapping array when planted. Common in sect territories and treasure vaults.',
  },
  'Restriction Flag': {
    name: 'Restriction Flag', nameCn: '禁旗', componentType: 'flag',
    properties: ['binding', 'suppression', 'spirit_weaving', 'isolation', 'offensive'],
    potency: 0.6, stability: 0.6, grade: 'spirit',
    novel: 'ISSTH',
    blurb: "Meng Hao's signature flags. Deployed to create instant formation traps. Multiple flags can combine into larger restriction arrays.",
  },
  'Soul-Locking Flag': {
    name: 'Soul-Locking Flag', nameCn: '锁魂旗', componentType: 'flag',
    properties: ['soul_locking', 'binding', 'dark_aspect', 'cursed', 'offensive'],
    potency: 0.7, stability: 0.5, grade: 'spirit',
    novel: 'Renegade Immortal',
    blurb: 'A flag that locks the souls of those caught inside. Wang Lin encountered these in ancient tombs.',
  },
  'Blood-Drinking Banner': {
    name: 'Blood-Drinking Banner', nameCn: '嗜血幡', componentType: 'flag',
    properties: ['blood_drinking', 'offensive', 'leeching', 'cursed', 'dark_aspect'],
    potency: 0.75, stability: 0.45, grade: 'spirit',
    novel: 'Renegade Immortal',
    blurb: 'A banner that absorbs the blood of those within the formation. Each death strengthens the array. Favored by demonic cultivators.',
  },
  'Nine-Heavens Flag': {
    name: 'Nine-Heavens Flag', nameCn: '九天旗', componentType: 'flag',
    properties: ['barrier', 'amplification', 'dao_resonant', 'light_aspect', 'blessed', 'defensive'],
    potency: 0.9, stability: 0.55, grade: 'immortal',
    novel: 'ISSTH',
    blurb: 'A flag resonating with the Nine Heavens. Projects a barrier that amplifies the cultivation of those within.',
  },
  'Karmic Anchor Flag': {
    name: 'Karmic Anchor Flag', nameCn: '因果锚旗', componentType: 'flag',
    properties: ['karmic_anchor', 'suppression', 'dao_resonant', 'isolation', 'binding'],
    potency: 0.95, stability: 0.4, grade: 'heaven',
    novel: 'Renegade Immortal',
    blurb: 'A flag that anchors karmic threads. Those with heavy karma cannot leave its range. Used by ancient cultivators to enforce karmic justice.',
  },

  // ── Stones (石) — foundation components for formations ──
  'Spirit Stone (Formation)': {
    name: 'Spirit Stone (Formation)', nameCn: '阵法灵石', componentType: 'stone',
    properties: ['qi_gathering', 'self_sustaining', 'utility'],
    potency: 0.15, stability: 0.95, grade: 'mortal',
    blurb: 'A spirit stone carved with formation sigils. The universal power source for mortal arrays.',
  },
  'Earth-Heart Stone': {
    name: 'Earth-Heart Stone', nameCn: '地心石', componentType: 'stone',
    properties: ['earth_aspect', 'barrier', 'defensive', 'self_sustaining'],
    potency: 0.5, stability: 0.85, grade: 'magical',
    novel: 'All Novels',
    blurb: 'A stone from deep within the earth. Naturally stabilizes formations and strengthens earth-aspect barriers.',
  },
  'Fire-Aspect Crystal': {
    name: 'Fire-Aspect Crystal', nameCn: '火属性晶石', componentType: 'stone',
    properties: ['fire_aspect', 'offensive', 'amplification'],
    potency: 0.5, stability: 0.7, grade: 'magical',
    blurb: 'A crystal imbued with fire-aspect Qi. Adds fire damage to offensive formations.',
  },
  'Water-Aspect Pearl': {
    name: 'Water-Aspect Pearl', nameCn: '水属性灵珠', componentType: 'stone',
    properties: ['water_aspect', 'barrier', 'defensive', 'qi_gathering'],
    potency: 0.5, stability: 0.8, grade: 'magical',
    blurb: 'A pearl imbued with water-aspect Qi. Strengthens defensive barriers and Qi-gathering arrays.',
  },
  'Wind-Whisper Stone': {
    name: 'Wind-Whisper Stone', nameCn: '风语石', componentType: 'stone',
    properties: ['wind_aspect', 'illusion', 'concealment', 'utility'],
    potency: 0.45, stability: 0.75, grade: 'magical',
    blurb: 'A stone that whispers with the wind. Used in concealment and speed formations.',
  },
  'Thunder Core Stone': {
    name: 'Thunder Core Stone', nameCn: '雷核石', componentType: 'stone',
    properties: ['lightning_aspect', 'offensive', 'suppression', 'binding'],
    potency: 0.65, stability: 0.55, grade: 'spirit',
    novel: 'Renegade Immortal',
    blurb: 'A stone containing the core of a lightning strike. Powers lightning-aspect offensive arrays.',
  },
  'Soul-Preserving Jade': {
    name: 'Soul-Preserving Jade', nameCn: '护魂玉', componentType: 'stone',
    properties: ['soul_locking', 'defensive', 'mind_pacifying', 'blessed'],
    potency: 0.6, stability: 0.75, grade: 'spirit',
    novel: 'All Novels',
    blurb: 'Jade that protects the souls of those within its formation range. Counters soul-attack arrays.',
  },
  'Star-Iron Formation Stone': {
    name: 'Star-Iron Formation Stone', nameCn: '星铁阵石', componentType: 'stone',
    properties: ['metal_aspect', 'barrier', 'suppression', 'space_folding', 'defensive'],
    potency: 0.8, stability: 0.6, grade: 'spirit',
    novel: 'ISSTH',
    blurb: 'Star-iron carved into formation nodes. Used in space-folding defensive arrays.',
  },
  'Void-Stone': {
    name: 'Void-Stone', nameCn: '虚空石', componentType: 'stone',
    properties: ['space_folding', 'teleport', 'isolation', 'dao_resonant'],
    potency: 0.9, stability: 0.4, grade: 'immortal',
    novel: 'ISSTH',
    blurb: 'A stone from the void between worlds. Enables teleportation and spatial isolation arrays.',
  },
  'Origin Formation Core': {
    name: 'Origin Formation Core', nameCn: '本源阵核', componentType: 'stone',
    properties: ['dao_resonant', 'amplification', 'qi_gathering', 'self_sustaining', 'blessed'],
    potency: 0.95, stability: 0.5, grade: 'heaven',
    novel: 'Renegade Immortal',
    blurb: 'A formation core from the origin of the world. Virtually never depletes. Powers heaven-grade formations.',
  },

  // ── Pillars (柱) — large formation anchors ──
  'Jade Pillar': {
    name: 'Jade Pillar', nameCn: '玉柱', componentType: 'pillar',
    properties: ['barrier', 'defensive', 'qi_gathering', 'amplification', 'utility'],
    potency: 0.5, stability: 0.9, grade: 'magical',
    blurb: 'A jade pillar used as a formation anchor. Commonly seen in sect grand arrays.',
  },
  'Dragon-Bone Pillar': {
    name: 'Dragon-Bone Pillar', nameCn: '龙骨柱', componentType: 'pillar',
    properties: ['suppression', 'binding', 'amplification', 'dao_resonant', 'offensive'],
    potency: 0.85, stability: 0.65, grade: 'immortal',
    novel: 'Renegade Immortal',
    blurb: 'A pillar carved from a true dragon\'s bone. Suppresses all cultivation below the deployer\'s realm within range.',
  },
  'Soul-Sealing Pillar': {
    name: 'Soul-Sealing Pillar', nameCn: '封魂柱', componentType: 'pillar',
    properties: ['soul_locking', 'binding', 'suppression', 'isolation', 'cursed', 'offensive'],
    potency: 0.9, stability: 0.5, grade: 'immortal',
    novel: 'Renegade Immortal',
    blurb: 'A pillar that seals souls. Used in ancient tombs to trap tomb robbers. Those who die within have their souls imprisoned forever.',
  },

  // ── Disks (盘) — portable formation controllers ──
  'Array Disk (Basic)': {
    name: 'Array Disk (Basic)', nameCn: '阵盘(基础)', componentType: 'disk',
    properties: ['utility', 'qi_gathering', 'self_sustaining'],
    potency: 0.2, stability: 0.8, grade: 'mortal',
    novel: 'ISSTH',
    blurb: 'A portable formation disk. The most basic controller for mortal formations.',
  },
  'Restriction Disk': {
    name: 'Restriction Disk', nameCn: '禁制阵盘', componentType: 'disk',
    properties: ['trap', 'binding', 'suppression', 'spirit_weaving', 'offensive'],
    potency: 0.65, stability: 0.6, grade: 'spirit',
    novel: 'ISSTH',
    blurb: "Meng Hao's restriction disks. Portable formation controllers that can deploy traps instantly.",
  },
  'Teleportation Disk': {
    name: 'Teleportation Disk', nameCn: '传送阵盘', componentType: 'disk',
    properties: ['teleport', 'space_folding', 'utility'],
    potency: 0.7, stability: 0.5, grade: 'spirit',
    novel: 'All Novels',
    blurb: 'A disk that controls a teleportation array. Requires paired disks at destination.',
  },
  'Time-Dilation Disk': {
    name: 'Time-Dilation Disk', nameCn: '时空阵盘', componentType: 'disk',
    properties: ['time_dilation', 'isolation', 'dao_resonant', 'self_sustaining'],
    potency: 0.9, stability: 0.35, grade: 'heaven',
    novel: 'Renegade Immortal',
    blurb: 'A disk that controls time within the formation. Time flows faster inside — 1 real day = 10 days inside. Incredibly rare.',
  },

  // ── Jade Slips (简) — formation knowledge carriers ──
  'Formation Manual (Mortal)': {
    name: 'Formation Manual (Mortal)', nameCn: '阵法入门', componentType: 'jade_slip',
    properties: ['utility', 'qi_gathering', 'barrier'],
    potency: 0.2, stability: 0.9, grade: 'mortal',
    blurb: 'A basic jade slip containing mortal formation diagrams. Essential learning material.',
  },
  'Grand Formation Scripture': {
    name: 'Grand Formation Scripture', nameCn: '大阵经', componentType: 'jade_slip',
    properties: ['dao_resonant', 'amplification', 'spirit_weaving', 'self_sustaining'],
    potency: 0.7, stability: 0.6, grade: 'spirit',
    novel: 'Renegade Immortal',
    blurb: "A scripture detailing grand formation principles. Wang Lin's deep formation knowledge came from texts like this.",
  },
  'Heaven-Defying Formation Jade': {
    name: 'Heaven-Defying Formation Jade', nameCn: '逆天阵玉', componentType: 'jade_slip',
    properties: ['dao_resonant', 'space_folding', 'time_dilation', 'karmic_anchor', 'sentient_seed'],
    potency: 1.0, stability: 0.3, grade: 'dao',
    novel: 'Renegade Immortal',
    blurb: 'A jade slip containing formation knowledge that defies heaven. The formation equivalent of the Heaven-Defying Bead.',
  },
};

// ─── Known Formation Patterns ──────────────────────────────────────
// These are the "known recipes" — property combinations that produce specific formations.
// Unlike alchemy/refining, formations have a DURATION and POWER DECAY over time.

export type FormationIntent = 'defense' | 'trap' | 'cultivation_boost' | 'concealment' | 'teleportation' | 'suppression' | 'illusion' | 'siege' | 'soul_prison';

export interface FormationPattern {
  name: string;
  nameCn: string;
  intent: FormationIntent;
  requiredProperties: { prop: FormationProperty; minCount: number }[];
  minComponents: number;
  description: string;
  novel?: string;
  powerBase: number; // 0..1 — base power before scaling
}

export const FORMATION_PATTERNS: FormationPattern[] = [
  // ── Defensive Formations ──
  { name: 'Spirit-Gathering Array', nameCn: '聚灵阵', intent: 'cultivation_boost',
    requiredProperties: [{ prop: 'qi_gathering', minCount: 1 }],
    minComponents: 2, powerBase: 0.3,
    description: 'Draws ambient Qi to a single point. Cultivation speed inside is 1.5× normal.',
    novel: 'All Novels' },
  { name: 'Earth-Barrier Array', nameCn: '土墙阵', intent: 'defense',
    requiredProperties: [{ prop: 'barrier', minCount: 1 }, { prop: 'earth_aspect', minCount: 1 }],
    minComponents: 2, powerBase: 0.4,
    description: 'Raises an earthen barrier. Blocks physical attacks below the formation\'s power.',
    novel: 'All Novels' },
  { name: 'Four-Symbols Protection', nameCn: '四象护阵', intent: 'defense',
    requiredProperties: [{ prop: 'barrier', minCount: 2 }, { prop: 'defensive', minCount: 1 }],
    minComponents: 4, powerBase: 0.6,
    description: 'The Four Symbols (Azure Dragon, White Tiger, Vermilion Bird, Black Tortoise) form a protective domain. Blocks all attacks below Core Formation.',
    novel: 'All Novels' },
  { name: 'Nine-Heavens Barrier', nameCn: '九天屏障', intent: 'defense',
    requiredProperties: [{ prop: 'barrier', minCount: 2 }, { prop: 'dao_resonant', minCount: 1 }, { prop: 'blessed', minCount: 1 }],
    minComponents: 5, powerBase: 0.85,
    description: 'A barrier resonating with the Nine Heavens. Blocks attacks from cultivators up to one realm above the deployer.',
    novel: 'ISSTH' },
  { name: 'Mountain-Sea Shield', nameCn: '山海盾阵', intent: 'defense',
    requiredProperties: [{ prop: 'barrier', minCount: 1 }, { prop: 'defensive', minCount: 1 }, { prop: 'self_sustaining', minCount: 1 }],
    minComponents: 4, powerBase: 0.7,
    description: 'A self-sustaining barrier that draws power from the earth. Lasts for years without maintenance.',
    novel: 'ISSTH' },

  // ── Trap Formations ──
  { name: 'Binding Chains Array', nameCn: '锁链阵', intent: 'trap',
    requiredProperties: [{ prop: 'binding', minCount: 1 }, { prop: 'suppression', minCount: 1 }],
    minComponents: 2, powerBase: 0.4,
    description: 'Chains of Qi bind those who enter. Movement speed reduced by 50%.',
    novel: 'All Novels' },
  { name: 'Illusion Maze', nameCn: '幻阵迷宫', intent: 'illusion',
    requiredProperties: [{ prop: 'illusion', minCount: 2 }, { prop: 'concealment', minCount: 1 }],
    minComponents: 3, powerBase: 0.5,
    description: 'Those who enter lose their sense of direction. Exit shifts constantly. Only divine sense can pierce it.',
    novel: 'Renegade Immortal' },
  { name: 'Soul-Prison Array', nameCn: '锁魂阵', intent: 'soul_prison',
    requiredProperties: [{ prop: 'soul_locking', minCount: 1 }, { prop: 'isolation', minCount: 1 }, { prop: 'binding', minCount: 1 }],
    minComponents: 4, powerBase: 0.7,
    description: 'Imprisons the souls of those who die within. The trapped souls power the array indefinitely.',
    novel: 'Renegade Immortal' },
  { name: 'Five-Element Trap', nameCn: '五行困阵', intent: 'trap',
    requiredProperties: [{ prop: 'fire_aspect', minCount: 1 }, { prop: 'water_aspect', minCount: 1 }, { prop: 'earth_aspect', minCount: 1 }, { prop: 'metal_aspect', minCount: 1 }, { prop: 'wood_aspect', minCount: 1 }],
    minComponents: 5, powerBase: 0.8,
    description: 'The five elements cycle endlessly. Those caught inside are attacked by all five elements simultaneously.',
    novel: 'All Novels' },
  { name: 'Lightning-Punishment Array', nameCn: '天雷惩罚阵', intent: 'trap',
    requiredProperties: [{ prop: 'lightning_aspect', minCount: 2 }, { prop: 'offensive', minCount: 1 }],
    minComponents: 3, powerBase: 0.6,
    description: 'Calls down lightning from the heavens on those within. Each strike grows stronger as the array absorbs ambient lightning Qi.',
    novel: 'Renegade Immortal' },

  // ── Cultivation Boost Formations ──
  { name: 'Spirit-Nourishing Array', nameCn: '养灵阵', intent: 'cultivation_boost',
    requiredProperties: [{ prop: 'qi_gathering', minCount: 2 }, { prop: 'amplification', minCount: 1 }],
    minComponents: 3, powerBase: 0.5,
    description: 'A refined Qi-gathering array. Cultivation speed inside is 2× normal. Common in sect meditation chambers.',
    novel: 'All Novels' },
  { name: 'Dao-Comprehension Array', nameCn: '悟道阵', intent: 'cultivation_boost',
    requiredProperties: [{ prop: 'dao_resonant', minCount: 1 }, { prop: 'qi_gathering', minCount: 1 }, { prop: 'mind_pacifying', minCount: 1 }],
    minComponents: 4, powerBase: 0.7,
    description: 'Calms the mind and resonates with the Dao. Dao comprehension speed inside is 3× normal. Incredibly rare.',
    novel: 'Renegade Immortal' },
  { name: 'Time-Dilation Chamber', nameCn: '时空修炼室', intent: 'cultivation_boost',
    requiredProperties: [{ prop: 'time_dilation', minCount: 1 }, { prop: 'isolation', minCount: 1 }],
    minComponents: 3, powerBase: 0.9,
    description: 'Time flows faster inside. 1 real day = 10 days inside. The ultimate cultivation aid.',
    novel: 'Renegade Immortal' },

  // ── Concealment Formations ──
  { name: 'Mist Veil Array', nameCn: '雾隐阵', intent: 'concealment',
    requiredProperties: [{ prop: 'concealment', minCount: 1 }, { prop: 'illusion', minCount: 1 }],
    minComponents: 2, powerBase: 0.35,
    description: 'Shrouds an area in thick spirit mist. Those below Core Formation cannot see through it.',
    novel: 'All Novels' },
  { name: 'Breath-Hiding Array', nameCn: '敛息阵', intent: 'concealment',
    requiredProperties: [{ prop: 'concealment', minCount: 2 }, { prop: 'isolation', minCount: 1 }],
    minComponents: 3, powerBase: 0.5,
    description: 'Hides all traces of cultivation. Even Nascent Soul divine sense cannot detect those within — if their realm is lower than the array\'s power.',
    novel: 'All Novels' },

  // ── Teleportation Formations ──
  { name: 'Short-Range Transit Array', nameCn: '短程传送阵', intent: 'teleportation',
    requiredProperties: [{ prop: 'teleport', minCount: 1 }, { prop: 'space_folding', minCount: 1 }],
    minComponents: 2, powerBase: 0.4,
    description: 'Teleports those within to a paired array within 100 li. Requires spirit stones to power.',
    novel: 'All Novels' },
  { name: 'Cross-Realm Transfer Array', nameCn: '跨界传送阵', intent: 'teleportation',
    requiredProperties: [{ prop: 'teleport', minCount: 1 }, { prop: 'space_folding', minCount: 2 }, { prop: 'dao_resonant', minCount: 1 }],
    minComponents: 5, powerBase: 0.9,
    description: 'An array that crosses realms. Can teleport between continents — or even worlds. Requires immense power.',
    novel: 'Renegade Immortal' },

  // ── Suppression Formations ──
  { name: 'Realm-Suppression Array', nameCn: '境界压制阵', intent: 'suppression',
    requiredProperties: [{ prop: 'suppression', minCount: 2 }, { prop: 'binding', minCount: 1 }],
    minComponents: 3, powerBase: 0.6,
    description: 'Suppresses cultivation within. All cultivators below the deployer\'s realm fight at one sub-stage lower.',
    novel: 'Renegade Immortal' },
  { name: 'Flight-Forbidden Domain', nameCn: '禁空领域', intent: 'suppression',
    requiredProperties: [{ prop: 'suppression', minCount: 1 }, { prop: 'binding', minCount: 1 }, { prop: 'barrier', minCount: 1 }],
    minComponents: 3, powerBase: 0.55,
    description: 'Forbids flight within range. Even Core Formation cultivators must walk on foot.',
    novel: 'Renegade Immortal' },

  // ── Siege Formations ──
  { name: 'Siege-Breaker Array', nameCn: '破城阵', intent: 'siege',
    requiredProperties: [{ prop: 'offensive', minCount: 2 }, { prop: 'amplification', minCount: 1 }, { prop: 'suppression', minCount: 1 }],
    minComponents: 4, powerBase: 0.7,
    description: 'An offensive formation designed to break through sect barriers. Each component amplifies the others\' attack power.',
    novel: 'ISSTH' },
  { name: 'Blood-Sacrifice Array', nameCn: '血祭阵', intent: 'siege',
    requiredProperties: [{ prop: 'blood_drinking', minCount: 1 }, { prop: 'offensive', minCount: 1 }, { prop: 'leeching', minCount: 1 }],
    minComponents: 3, powerBase: 0.75,
    description: 'Drains the life force of those within to power the array. The more enemies, the stronger it becomes. Forbidden.',
    novel: 'Renegade Immortal' },
];

// ─── Deployed Formation ──────────────────────────────────────────

export interface DeployedFormation {
  id: string;
  name: string;
  nameCn: string;
  intent: FormationIntent;
  grade: string;
  power: number;              // 0..1 — current power (decays over time)
  maxPower: number;           // 0..1 — initial power when deployed
  stability: number;          // 0..1 — how resistant to disruption
  duration: number;           // years remaining before dissolution
  maxDuration: number;        // years total when first deployed
  components: string[];       // names of components used
  effects: string[];          // effect descriptions
  realm: RealmId;             // the deployer's realm when created
  novel?: string;
  discoveryText?: string;     // if this was a procedural discovery
}

// ─── Formation creation logic ──────────────────────────────────────

const GRADE_ORDER: Record<string, number> = {
  mortal: 0, magical: 1, spirit: 2, earth: 3, heaven: 4, immortal: 5, dao: 6,
};

const REALM_MIN: Record<RealmId, number> = {
  mortal: 0, qi_condensation: 1, foundation: 2, core_formation: 3,
  nascent_soul: 4, soul_formation: 5, soul_transformation: 6, ascendant: 7,
  illusory_yin: 8, corporeal_yang: 8, nirvana_scryer: 9, nirvana_cleanser: 9,
  nirvana_fruit: 10, spirit_seizer: 10, true_immortal: 11, ancient: 12, paragon: 13, transcendence: 14,
};

export interface FormationResult {
  ok: boolean;
  message: string;
  formation?: DeployedFormation;
  explosion?: boolean;
  formationSkillGain?: number;
}

/**
 * Check if the player can use a component (realm-gating: can't use components 2+ grades above realm)
 */
function canUseComponent(component: FormationComponent, playerRealm: RealmId): { ok: boolean; reason?: string } {
  const compGrade = GRADE_ORDER[component.grade] ?? 0;
  const playerGrade = REALM_MIN[playerRealm];
  if (compGrade > playerGrade + 2) {
    return { ok: false, reason: `Requires ${component.grade} realm (too high-grade)` };
  }
  return { ok: true };
}

/**
 * Aggregate properties from selected components
 */
function aggregateProperties(components: FormationComponent[]): Map<FormationProperty, number> {
  const props = new Map<FormationProperty, number>();
  for (const c of components) {
    const seen = new Set<FormationProperty>();
    for (const p of c.properties) {
      if (!seen.has(p)) {
        props.set(p, (props.get(p) ?? 0) + 1);
        seen.add(p);
      }
    }
  }
  return props;
}

/**
 * Match aggregated properties against known patterns. Returns the best match or null.
 */
function matchPattern(props: Map<FormationProperty, number>, componentCount: number): FormationPattern | null {
  let bestMatch: FormationPattern | null = null;
  let bestSpecificity = -1;

  for (const pattern of FORMATION_PATTERNS) {
    if (componentCount < pattern.minComponents) continue;
    let matched = true;
    let specificity = 0;
    for (const req of pattern.requiredProperties) {
      const count = props.get(req.prop) ?? 0;
      if (count < req.minCount * 0.5) { matched = false; break; }
      specificity += Math.min(count / req.minCount, 2);
    }
    if (matched && specificity > bestSpecificity) {
      bestSpecificity = specificity;
      bestMatch = pattern;
    }
  }

  return bestMatch;
}

/**
 * Resolve the formation grade from components and skill.
 */
function resolveFormationGrade(
  components: FormationComponent[],
  skill: number,
  playerRealm: RealmId,
): { grade: string; success: boolean; gradePenalty: number } {
  const maxComponentGrade = Math.max(...components.map(c => GRADE_ORDER[c.grade]));
  // Skill determines max grade: 0-0.1=mortal, 0.1-0.3=magical, 0.3-0.5=spirit, 0.5-0.7=earth, 0.7-0.9=heaven, 0.9+=immortal
  const skillMaxGrade = skill < 0.1 ? 0 : skill < 0.3 ? 1 : skill < 0.5 ? 2 : skill < 0.7 ? 3 : skill < 0.9 ? 4 : 5;
  const playerMaxGrade = REALM_MIN[playerRealm] + 2; // can overreach by 2 grades
  const effectiveMax = Math.min(maxComponentGrade, skillMaxGrade, playerMaxGrade, 6);

  // Success check
  const gradePenalty = Math.max(0, maxComponentGrade - effectiveMax) * 0.15;
  const baseSuccess = 0.65 + skill * 0.2 - gradePenalty;
  const success = Math.random() < Math.max(0.1, Math.min(0.95, baseSuccess));

  const gradeNames = ['mortal', 'magical', 'spirit', 'earth', 'heaven', 'immortal', 'dao'];
  return { grade: gradeNames[effectiveMax] as string, success, gradePenalty };
}

/**
 * Calculate formation effects from the matched pattern + properties
 */
function resolveEffects(
  pattern: FormationPattern,
  props: Map<FormationProperty, number>,
  grade: string,
  power: number,
): string[] {
  const effects: string[] = [];
  const gradeMultiplier = 1 + (GRADE_ORDER[grade] ?? 0) * 0.3;

  switch (pattern.intent) {
    case 'defense':
      effects.push(`Blocks attacks up to ${(power * gradeMultiplier * 100).toFixed(0)}% effectiveness`);
      if (props.has('self_sustaining')) effects.push('Self-sustaining (no Qi drain)');
      if (props.has('qi_gathering')) effects.push('Qi-gathering for those within');
      break;
    case 'trap':
      effects.push(`Traps targets for ${(power * gradeMultiplier * 10).toFixed(0)} years`);
      if (props.has('suppression')) effects.push('Cultivation suppressed within');
      if (props.has('offensive')) effects.push(`Deals ${(power * gradeMultiplier * 50).toFixed(0)}% damage to trapped targets per year`);
      if (props.has('blood_drinking')) effects.push('Drains life force from trapped targets');
      if (props.has('leeching')) effects.push('Strengthens from ambient Qi');
      break;
    case 'cultivation_boost':
      const speedMultiplier = 1 + power * gradeMultiplier * 0.5;
      effects.push(`Cultivation speed ${speedMultiplier.toFixed(1)}× normal`);
      if (props.has('mind_pacifying')) effects.push('Calms the mind (reduces heart demon risk)');
      if (props.has('time_dilation')) effects.push(`Time flows ${Math.round(5 + power * 10)}× faster inside`);
      break;
    case 'concealment':
      effects.push(`Hides from divine sense up to realm ${(Math.round(power * gradeMultiplier * 14))}`);
      if (props.has('illusion')) effects.push('Illusion overlay — area appears empty/unchanged');
      break;
    case 'teleportation':
      const range = props.has('space_folding') && props.has('dao_resonant') ? 'cross-realm' : props.has('space_folding') ? '1000 li' : '100 li';
      effects.push(`Range: ${range}`);
      if (!props.has('self_sustaining')) effects.push('Requires spirit stones to power each use');
      break;
    case 'suppression':
      effects.push(`Suppressed cultivators fight at ${(1 - power * 0.5).toFixed(1)}× effectiveness`);
      if (props.has('binding')) effects.push('Movement restricted');
      if (props.has('barrier')) effects.push('Flight forbidden within range');
      break;
    case 'illusion':
      effects.push(`Illusion strength: ${(power * 100).toFixed(0)}%`);
      effects.push('Divine sense required to pierce');
      if (props.has('mind_pacifying')) effects.push('Entranced targets may fall into cultivation trance');
      break;
    case 'siege':
      effects.push(`Attack power: ${(power * gradeMultiplier * 100).toFixed(0)}%`);
      if (props.has('amplification')) effects.push('Components amplify each other');
      if (props.has('blood_drinking')) effects.push('Grows stronger with each enemy death');
      break;
    case 'soul_prison':
      effects.push('Traps souls of the deceased');
      effects.push('Trapped souls power the array indefinitely');
      if (props.has('cursed')) effects.push('Cursed — cannot be dismantled without karmic resolution');
      break;
  }

  return effects;
}

/**
 * Main function: deploy a formation from selected components
 */
export function deployFormation(
  skill: number,
  playerRealm: RealmId,
  componentNames: string[],
): FormationResult {
  // Validate components
  const components = componentNames.map(n => FORMATION_COMPONENTS[n]).filter(Boolean);
  if (components.length < 2) {
    return { ok: false, message: 'At least 2 components are needed to form an array.' };
  }

  // Aggregate properties
  const props = aggregateProperties(components);

  // Stability check (explosion risk)
  const avgStability = components.reduce((s, c) => s + c.stability, 0) / components.length;
  const avgPotency = components.reduce((s, c) => s + c.potency, 0) / components.length;
  const explosionChance = Math.max(0, (1 - avgStability) * avgPotency * 0.5 - skill * 0.3);

  if (Math.random() < explosionChance) {
    const skillGain = 0.005 + Math.random() * 0.01;
    return {
      ok: false,
      message: `The formation destabilizes! ${components.map(c => c.name).join(', ')} clash — Qi erupts in all directions. Your robes are singed, but you learned from the failure.`,
      explosion: true,
      formationSkillGain: skillGain,
    };
  }

  // Resolve grade + success
  const { grade, success, gradePenalty } = resolveFormationGrade(components, skill, playerRealm);
  if (!success) {
    const skillGain = 0.003 + Math.random() * 0.008;
    return {
      ok: false,
      message: `The Qi flows fail to align. The components are too ${gradePenalty > 0 ? 'high-grade for your current skill' : 'unstable in combination'}. You carefully dismantle the array before it collapses.`,
      formationSkillGain: skillGain,
    };
  }

  // Match pattern
  const pattern = matchPattern(props, components.length);

  // Procedural discovery check
  let isDiscovery = false;
  let discoveryText: string | undefined;
  if (!pattern && skill > 0.4 && Math.random() < (skill - 0.4) * 0.5) {
    isDiscovery = true;
    // Generate a discovery name based on dominant properties
    const dominantProp = [...props.entries()].sort((a, b) => b[1] - a[1])[0]?.[0] ?? 'unknown';
    const intent = guessIntentFromProperties(props);
    discoveryText = `DISCOVERY! You have created a formation no known pattern describes — a ${formatPropertyName(dominantProp)}-aspect ${intent} array that bears your name.`;
  }

  // Calculate power
  const basePower = pattern ? pattern.powerBase : 0.3 + Math.random() * 0.3;
  const power = Math.min(1, basePower * (1 + GRADE_ORDER[grade] * 0.1));

  // Calculate duration (formations decay over time)
  const baseDuration = 10 + power * 90; // 10-100 years base
  const selfSustaining = props.has('self_sustaining') ? 3 : 1;
  const stability = Math.min(1, avgStability * (1 + skill * 0.2));
  const duration = Math.round(baseDuration * selfSustaining * (1 + GRADE_ORDER[grade] * 0.5));

  // Resolve effects
  const effects = isDiscovery
    ? resolveEffectsFromProperties(props, grade, power)
    : pattern
      ? resolveEffects(pattern, props, grade, power)
      : [`Generic ${grade} formation with power ${(power * 100).toFixed(0)}%`];

  const name = isDiscovery
    ? `${formatPropertyName([...props.entries()].sort((a, b) => b[1] - a[1])[0]?.[0] ?? 'Unknown')}-Aspect Discovery Array`
    : pattern
      ? pattern.name
      : 'Unresolved Array';

  const nameCn = isDiscovery
    ? `${[...props.entries()].sort((a, b) => b[1] - a[1])[0]?.[0]?.split('_')[0] ?? '未知'}发现阵`
    : pattern
      ? pattern.nameCn
      : '未名阵';

  const intent = pattern?.intent ?? guessIntentFromProperties(props);

  // Skill gain
  const skillGain = 0.008 + Math.random() * 0.012 + (isDiscovery ? 0.02 : 0);

  const formation: DeployedFormation = {
    id: `formation_${Date.now()}_${Math.random().toString(36).slice(2,8)}`,
    name,
    nameCn,
    intent,
    grade,
    power,
    maxPower: power,
    stability,
    duration,
    maxDuration: duration,
    components: componentNames,
    effects,
    realm: playerRealm,
    novel: pattern?.novel,
    discoveryText: isDiscovery ? discoveryText : undefined,
  };

  return {
    ok: true,
    message: isDiscovery
      ? `The Qi flows in patterns you've never seen before. ${discoveryText} Power: ${(power * 100).toFixed(0)}%. Duration: ${duration} years.`
      : `The ${pattern?.name} takes shape! ${pattern?.description} Power: ${(power * 100).toFixed(0)}%. Duration: ${duration} years.`,
    formation,
    formationSkillGain: skillGain,
  };
}

/**
 * Tick a deployed formation — decay power and duration
 */
export function tickFormation(f: DeployedFormation, years: number): DeployedFormation | null {
  const decayRate = f.stability * 0.02; // higher stability = slower decay
  const powerLoss = years * decayRate * 0.1;
  const durationLoss = years;

  const newPower = Math.max(0, f.power - powerLoss);
  const newDuration = Math.max(0, f.duration - durationLoss);

  if (newDuration <= 0 || newPower <= 0.01) return null; // formation dissolved

  return {
    ...f,
    power: newPower,
    duration: newDuration,
  };
}

/**
 * Get available components based on player inventory and realm
 */
export function getAvailableFormationComponents(
  inventory: Record<string, number>,
  playerRealm: RealmId,
): { name: string; component: FormationComponent; available: boolean; reason?: string }[] {
  return Object.entries(FORMATION_COMPONENTS).map(([name, component]) => {
    const inInventory = (inventory[name] ?? 0) > 0;
    if (!inInventory) return { name, component, available: false, reason: 'Not in inventory' };
    const realmCheck = canUseComponent(component, playerRealm);
    if (!realmCheck.ok) return { name, component, available: false, reason: realmCheck.reason };
    return { name, component, available: true };
  });
}

// ─── Helpers ──────────────────────────────────────────────────────

function guessIntentFromProperties(props: Map<FormationProperty, number>): FormationIntent {
  const scores: Record<FormationIntent, number> = {
    defense: 0, trap: 0, cultivation_boost: 0, concealment: 0, teleportation: 0,
    suppression: 0, illusion: 0, siege: 0, soul_prison: 0,
  };
  for (const [prop, count] of props) {
    if (prop === 'barrier' || prop === 'defensive') scores.defense += count;
    if (prop === 'trap' || prop === 'binding') scores.trap += count;
    if (prop === 'qi_gathering' || prop === 'amplification' || prop === 'mind_pacifying' || prop === 'time_dilation') scores.cultivation_boost += count;
    if (prop === 'concealment') scores.concealment += count;
    if (prop === 'teleport' || prop === 'space_folding') scores.teleportation += count;
    if (prop === 'suppression') scores.suppression += count;
    if (prop === 'illusion') scores.illusion += count;
    if (prop === 'offensive' || prop === 'siege' || prop === 'blood_drinking') scores.siege += count;
    if (prop === 'soul_locking' || prop === 'isolation') scores.soul_prison += count;
  }
  return Object.entries(scores).sort((a, b) => b[1] - a[1])[0][0] as FormationIntent;
}

function resolveEffectsFromProperties(
  props: Map<FormationProperty, number>,
  grade: string,
  power: number,
): string[] {
  const effects: string[] = [];
  const gradeMultiplier = 1 + (GRADE_ORDER[grade] ?? 0) * 0.3;
  const intent = guessIntentFromProperties(props);

  // Generic effects based on dominant properties
  if (props.has('barrier')) effects.push(`Barrier strength: ${(power * gradeMultiplier * 100).toFixed(0)}%`);
  if (props.has('qi_gathering')) effects.push(`Cultivation boost: ${(1 + power * 0.5).toFixed(1)}×`);
  if (props.has('offensive')) effects.push(`Offensive power: ${(power * gradeMultiplier * 50).toFixed(0)}%`);
  if (props.has('trap')) effects.push(`Trapping strength: ${(power * 100).toFixed(0)}%`);
  if (props.has('concealment')) effects.push('Conceals from divine sense');
  if (props.has('teleport')) effects.push('Spatial folding active');
  if (props.has('suppression')) effects.push('Realm suppression active');
  if (props.has('illusion')) effects.push('Illusion field generated');
  if (props.has('self_sustaining')) effects.push('Self-sustaining (no Qi drain)');
  if (props.has('time_dilation')) effects.push(`Time distortion: ${Math.round(5 + power * 10)}×`);
  if (props.has('blood_drinking')) effects.push('Life-draining effect');
  if (props.has('soul_locking')) effects.push('Soul-locking field');
  if (props.has('karmic_anchor')) effects.push('Karmic anchoring');

  if (effects.length === 0) effects.push(`Unknown ${intent} formation (${(power * 100).toFixed(0)}% power)`);
  return effects;
}

function formatPropertyName(prop: string): string {
  return prop.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
}