// src/lib/sim/talismans.ts
// Property-based talisman inscription system. The fourth of Er Gen's Four Great Arts (符箓).
// Talisman paper + ink have properties. Talismans emerge from property combos.
// A master talisman scribe can inscribe talismans no pattern describes.
// "Brush strokes flow like water. The Dao takes form on paper."

import type { RealmId, ElementId } from './types';

// ─── Talisman Properties ──────────────────────────────────────────
// Every material (paper/ink) has these. The talisman's effect is resolved from the combo.

export type TalismanProperty =
  // Effect traits
  | 'offensive' | 'defensive' | 'healing' | 'concealment' | 'speed'
  | 'binding' | 'purification' | 'detection' | 'summoning' | 'storage'
  | 'communication' | 'tracking' | 'barrier' | 'dispel' | 'seal'
  // Elemental aspects
  | 'fire_aspect' | 'water_aspect' | 'wind_aspect' | 'earth_aspect' | 'metal_aspect'
  | 'lightning_aspect' | 'ice_aspect' | 'dark_aspect' | 'light_aspect' | 'wood_aspect'
  // Spiritual traits
  | 'qi_channeling' | 'soul_touching' | 'dao_resonant' | 'karmic_purify'
  | 'mind_shielding' | 'spirit_binding'
  // Quality traits
  | 'single_use' | 'multi_use' | 'self_repairing' | 'sentient_seed'
  | 'cursed' | 'blessed' | 'explosive' | 'sustained' | 'instant';

export type TalismanMaterialType = 'paper' | 'ink' | 'brush' | 'seal_stamp';

export interface TalismanMaterial {
  name: string;
  nameCn?: string;
  materialType: TalismanMaterialType;
  properties: TalismanProperty[];
  potency: number;       // 0..1 — material quality
  stability: number;     // 0..1 — how stable during inscription
  grade: string;         // mortal → dao
  novel?: string;
  blurb?: string;
}

// ─── Talisman Material Catalog (all from Er Gen novels) ─────────────────

export const TALISMAN_MATERIALS: Record<string, TalismanMaterial> = {
  // ── Papers (纸) — the base for all talismans ──
  'Spirit Paper': {
    name: 'Spirit Paper', nameCn: '灵纸', materialType: 'paper',
    properties: ['qi_channeling', 'single_use', 'defensive'],
    potency: 0.15, stability: 0.9, grade: 'mortal',
    blurb: 'Paper infused with spiritual Qi. The most basic talisman medium. Holds one-use patterns.',
  },
  'Beast-Skin Parchment': {
    name: 'Beast-Skin Parchment', nameCn: '兽皮纸', materialType: 'paper',
    properties: ['qi_channeling', 'sustained', 'defensive', 'offensive'],
    potency: 0.3, stability: 0.8, grade: 'magical',
    novel: 'Renegade Immortal',
    blurb: 'Tanned spirit beast skin. More durable than paper — can hold multi-use patterns.',
  },
  'Jade Sliver Paper': {
    name: 'Jade Sliver Paper', nameCn: '玉片纸', materialType: 'paper',
    properties: ['qi_channeling', 'dao_resonant', 'multi_use', 'defensive'],
    potency: 0.5, stability: 0.75, grade: 'spirit',
    novel: 'ISSTH',
    blurb: 'Paper woven from jade fibers. Dao-resonant. Meng Hao used jade talisman papers for higher-tier inscriptions.',
  },
  'Soul-Thread Paper': {
    name: 'Soul-Thread Paper', nameCn: '魂丝纸', materialType: 'paper',
    properties: ['soul_touching', 'sustained', 'spirit_binding', 'seal'],
    potency: 0.6, stability: 0.65, grade: 'spirit',
    novel: 'Renegade Immortal',
    blurb: 'Paper interwoven with soul threads. Can bind souls and seal spiritual entities.',
  },
  'Heaven-Defying Talisman Paper': {
    name: 'Heaven-Defying Talisman Paper', nameCn: '逆天符纸', materialType: 'paper',
    properties: ['dao_resonant', 'qi_channeling', 'self_repairing', 'multi_use', 'blessed', 'sentient_seed'],
    potency: 0.95, stability: 0.4, grade: 'heaven',
    novel: 'Renegade Immortal',
    blurb: 'Paper made from the essence of the Heaven-Defying Bead. The ultimate talisman medium. Can hold any pattern, self-repairs, and may develop sentience.',
  },
  'Blood Talisman Paper': {
    name: 'Blood Talisman Paper', nameCn: '血符纸', materialType: 'paper',
    properties: ['offensive', 'cursed', 'explosive', 'single_use', 'binding'],
    potency: 0.55, stability: 0.5, grade: 'spirit',
    novel: 'Renegade Immortal',
    blurb: 'Paper soaked in blood. Used for offensive and forbidden talismans. Unstable but powerful.',
  },

  // ── Inks (墨) — carry the Dao pattern ──
  'Basic Cinnabar Ink': {
    name: 'Basic Cinnabar Ink', nameCn: '朱砂墨', materialType: 'ink',
    properties: ['qi_channeling', 'defensive', 'purification'],
    potency: 0.15, stability: 0.9, grade: 'mortal',
    blurb: 'Ink mixed with cinnabar. The standard talisman ink for basic inscriptions.',
  },
  'Spirit Beast Blood Ink': {
    name: 'Spirit Beast Blood Ink', nameCn: '灵兽血墨', materialType: 'ink',
    properties: ['offensive', 'explosive', 'single_use', 'binding'],
    potency: 0.35, stability: 0.7, grade: 'magical',
    novel: 'All Novels',
    blurb: 'Ink made from spirit beast blood. Adds offensive properties to talismans.',
  },
  'Dragon Blood Ink': {
    name: 'Dragon Blood Ink', nameCn: '龙血墨', materialType: 'ink',
    properties: ['offensive', 'dao_resonant', 'blessed', 'explosive'],
    potency: 0.8, stability: 0.45, grade: 'heaven',
    novel: 'All Novels',
    blurb: 'Ink refined from true dragon blood. Imbues talismans with draconic power. Extremely rare.',
  },
  'Soul Ink': {
    name: 'Soul Ink', nameCn: '魂墨', materialType: 'ink',
    properties: ['soul_touching', 'seal', 'spirit_binding', 'sustained'],
    potency: 0.6, stability: 0.6, grade: 'spirit',
    novel: 'Renegade Immortal',
    blurb: 'Ink containing refined soul essence. Used for soul-sealing and spirit-binding talismans.',
  },
  'Karmic Ink': {
    name: 'Karmic Ink', nameCn: '因果墨', materialType: 'ink',
    properties: ['karmic_purify', 'dao_resonant', 'seal', 'purification'],
    potency: 0.7, stability: 0.5, grade: 'immortal',
    novel: 'Renegade Immortal',
    blurb: 'Ink distilled from karmic threads. Can purify karma and seal karmic entities.',
  },
  'Lightning Ink': {
    name: 'Lightning Ink', nameCn: '雷墨', materialType: 'ink',
    properties: ['lightning_aspect', 'offensive', 'explosive', 'instant'],
    potency: 0.5, stability: 0.55, grade: 'spirit',
    novel: 'Renegade Immortal',
    blurb: 'Ink charged with lightning Qi. Creates instant-activation lightning talismans.',
  },
  'Mind-Clear Ink': {
    name: 'Mind-Clear Ink', nameCn: '清心墨', materialType: 'ink',
    properties: ['mind_shielding', 'healing', 'purification', 'qi_channeling'],
    potency: 0.4, stability: 0.8, grade: 'magical',
    novel: 'All Novels',
    blurb: 'Ink brewed from calming herbs. Creates talismans that shield against mental attacks and soothe the mind.',
  },

  // ── Brushes (笔) — the inscribing instrument ──
  'Wolf-Hair Brush': {
    name: 'Wolf-Hair Brush', nameCn: '狼毫笔', materialType: 'brush',
    properties: ['qi_channeling'],
    potency: 0.1, stability: 0.95, grade: 'mortal',
    blurb: 'The standard talisman brush. Functional but unremarkable.',
  },
  'Spirit-Vein Brush': {
    name: 'Spirit-Vein Brush', nameCn: '灵脉笔', materialType: 'brush',
    properties: ['qi_channeling', 'dao_resonant', 'multi_use'],
    potency: 0.4, stability: 0.8, grade: 'spirit',
    novel: 'ISSTH',
    blurb: 'A brush carved from a spirit vein. Naturally channels Qi, improving pattern clarity.',
  },
  'Soul-Thread Brush': {
    name: 'Soul-Thread Brush', nameCn: '魂丝笔', materialType: 'brush',
    properties: ['soul_touching', 'spirit_binding', 'seal'],
    potency: 0.5, stability: 0.7, grade: 'spirit',
    novel: 'Renegade Immortal',
    blurb: 'A brush whose bristles are soul threads. Inscribes soul-affecting patterns with precision.',
  },
  'Heaven-Defying Brush': {
    name: 'Heaven-Defying Brush', nameCn: '逆天笔', materialType: 'brush',
    properties: ['dao_resonant', 'sentient_seed', 'blessed', 'self_repairing'],
    potency: 0.95, stability: 0.5, grade: 'dao',
    novel: 'Renegade Immortal',
    blurb: 'A brush that existed before the heavens. It has its own will and chooses its scribe. The ultimate talisman brush.',
  },

  // ── Seal Stamps (印) — press the pattern into reality ──
  'Sect Seal Stamp': {
    name: 'Sect Seal Stamp', nameCn: '宗门印', materialType: 'seal_stamp',
    properties: ['seal', 'binding', 'qi_channeling', 'defensive'],
    potency: 0.2, stability: 0.9, grade: 'mortal',
    novel: 'All Novels',
    blurb: 'A sect\'s official seal stamp. Adds authority to sealing talismans.',
  },
  'Restriction Seal': {
    name: 'Restriction Seal', nameCn: '禁制印', materialType: 'seal_stamp',
    properties: ['seal', 'binding', 'suppression', 'barrier'],
    potency: 0.5, stability: 0.7, grade: 'spirit',
    novel: 'ISSTH',
    blurb: 'Meng Hao\'s restriction seal. Adds sealing and binding power to talismans.',
  },
  'Soul-Seal Stamp': {
    name: 'Soul-Seal Stamp', nameCn: '封魂印', materialType: 'seal_stamp',
    properties: ['seal', 'soul_touching', 'spirit_binding', 'cursed'],
    potency: 0.65, stability: 0.55, grade: 'spirit',
    novel: 'Renegade Immortal',
    blurb: 'A stamp that seals souls. Used for soul-prison and soul-binding talismans.',
  },
  'Dao Seal of Heaven': {
    name: 'Dao Seal of Heaven', nameCn: '天道印', materialType: 'seal_stamp',
    properties: ['dao_resonant', 'seal', 'blessed', 'dispel', 'barrier'],
    potency: 0.9, stability: 0.5, grade: 'immortal',
    novel: 'Renegade Immortal',
    blurb: 'A seal resonating with the Heavenly Dao. Its inscriptions carry heavenly authority.',
  },
};

// ─── Known Talisman Patterns ──────────────────────────────────────

export type TalismanIntent = 'attack' | 'defense' | 'heal' | 'conceal' | 'speed' | 'seal' | 'detect' | 'summon' | 'communicate' | 'purify' | 'track';

export interface TalismanPattern {
  name: string;
  nameCn: string;
  intent: TalismanIntent;
  requiredProperties: { prop: TalismanProperty; minCount: number }[];
  minMaterials: number;
  description: string;
  novel?: string;
  powerBase: number;
  uses: number;            // how many times the talisman can be used (1 = single-use)
}

export const TALISMAN_PATTERNS: TalismanPattern[] = [
  // ── Attack Talismans ──
  { name: 'Fire Ball Talisman', nameCn: '火球符', intent: 'attack',
    requiredProperties: [{ prop: 'fire_aspect', minCount: 1 }, { prop: 'offensive', minCount: 1 }],
    minMaterials: 2, powerBase: 0.3, uses: 1,
    description: 'Launches a fireball at the target. The most basic offensive talisman.',
    novel: 'All Novels' },
  { name: 'Lightning Strike Talisman', nameCn: '雷击符', intent: 'attack',
    requiredProperties: [{ prop: 'lightning_aspect', minCount: 1 }, { prop: 'offensive', minCount: 1 }, { prop: 'instant', minCount: 1 }],
    minMaterials: 3, powerBase: 0.6, uses: 1,
    description: 'Instant lightning strike. No activation delay — the bolt fires the moment Qi is channeled.',
    novel: 'Renegade Immortal' },
  { name: 'Soul-Piercing Talisman', nameCn: '穿魂符', intent: 'attack',
    requiredProperties: [{ prop: 'soul_touching', minCount: 1 }, { prop: 'offensive', minCount: 1 }],
    minMaterials: 2, powerBase: 0.5, uses: 1,
    description: 'Attacks the target\'s soul directly. Bypasses physical defenses.',
    novel: 'Renegade Immortal' },
  { name: 'Blood Explosion Talisman', nameCn: '血爆符', intent: 'attack',
    requiredProperties: [{ prop: 'explosive', minCount: 1 }, { prop: 'offensive', minCount: 1 }, { prop: 'cursed', minCount: 1 }],
    minMaterials: 3, powerBase: 0.7, uses: 1,
    description: 'Detonates in a shower of blood-Qi shrapnel. Forbidden. Damages all within range, including the user.',
    novel: 'Renegade Immortal' },
  { name: 'Five-Element Talisman', nameCn: '五行符', intent: 'attack',
    requiredProperties: [{ prop: 'fire_aspect', minCount: 1 }, { prop: 'water_aspect', minCount: 1 }, { prop: 'metal_aspect', minCount: 1 }],
    minMaterials: 4, powerBase: 0.65, uses: 1,
    description: 'Channels all five elements in a single devastating attack. Requires mastery of at least three elements.',
    novel: 'All Novels' },

  // ── Defense Talismans ──
  { name: 'Barrier Talisman', nameCn: '护身符', intent: 'defense',
    requiredProperties: [{ prop: 'defensive', minCount: 1 }, { prop: 'barrier', minCount: 1 }],
    minMaterials: 2, powerBase: 0.3, uses: 1,
    description: 'Projects a brief defensive barrier. Blocks one attack below the talisman\'s power.',
    novel: 'All Novels' },
  { name: 'Earth Shield Talisman', nameCn: '土盾符', intent: 'defense',
    requiredProperties: [{ prop: 'earth_aspect', minCount: 1 }, { prop: 'defensive', minCount: 1 }],
    minMaterials: 2, powerBase: 0.4, uses: 1,
    description: 'Raises an earthen shield. Absorbs earth-aspect attacks entirely.',
    novel: 'All Novels' },
  { name: 'Soul-Ward Talisman', nameCn: '护魂符', intent: 'defense',
    requiredProperties: [{ prop: 'soul_touching', minCount: 1 }, { prop: 'defensive', minCount: 1 }],
    minMaterials: 2, powerBase: 0.45, uses: 1,
    description: 'Wards the soul against spiritual attacks for a brief duration.',
    novel: 'Renegade Immortal' },
  { name: 'Karmic Shield Talisman', nameCn: '因果盾符', intent: 'defense',
    requiredProperties: [{ prop: 'karmic_purify', minCount: 1 }, { prop: 'defensive', minCount: 1 }, { prop: 'blessed', minCount: 1 }],
    minMaterials: 4, powerBase: 0.7, uses: 3,
    description: 'A rare multi-use talisman that deflects karmic retribution. Each use absorbs one karmic backlash. 3 uses.',
    novel: 'Renegade Immortal' },

  // ── Healing Talismans ──
  { name: 'Qi-Recovery Talisman', nameCn: '回灵符', intent: 'heal',
    requiredProperties: [{ prop: 'qi_channeling', minCount: 1 }, { prop: 'healing', minCount: 1 }],
    minMaterials: 2, powerBase: 0.25, uses: 1,
    description: 'Restores a burst of Qi when activated. The cultivator\'s emergency reserve.',
    novel: 'All Novels' },
  { name: 'Wound-Mending Talisman', nameCn: '疗伤符', intent: 'heal',
    requiredProperties: [{ prop: 'healing', minCount: 2 }],
    minMaterials: 2, powerBase: 0.35, uses: 1,
    description: 'Accelerates wound healing. Seal onto the injury — dissolves as flesh knits.',
    novel: 'All Novels' },
  { name: 'Mind-Calming Talisman', nameCn: '清心符', intent: 'heal',
    requiredProperties: [{ prop: 'mind_shielding', minCount: 1 }, { prop: 'healing', minCount: 1 }, { prop: 'qi_channeling', minCount: 1 }],
    minMaterials: 3, powerBase: 0.5, uses: 2,
    description: 'Calms the mind and suppresses heart demons temporarily. 2 uses. Essential for tribulation preparation.',
    novel: 'All Novels' },

  // ── Concealment Talismans ──
  { name: 'Breath-Hiding Talisman', nameCn: '敛息符', intent: 'conceal',
    requiredProperties: [{ prop: 'concealment', minCount: 1 }, { prop: 'qi_channeling', minCount: 1 }],
    minMaterials: 2, powerBase: 0.35, uses: 1,
    description: 'Hides all traces of cultivation for a brief period. Even divine sense cannot detect the user.',
    novel: 'All Novels' },
  { name: 'Shadow-Meld Talisman', nameCn: '影融符', intent: 'conceal',
    requiredProperties: [{ prop: 'concealment', minCount: 1 }, { prop: 'dark_aspect', minCount: 1 }],
    minMaterials: 2, powerBase: 0.5, uses: 1,
    description: 'Melds the user into shadows. Invisible to the naked eye. Movement leaves no trace.',
    novel: 'Renegade Immortal' },

  // ── Speed Talismans ──
  { name: 'Wind-Ride Talisman', nameCn: '御风符', intent: 'speed',
    requiredProperties: [{ prop: 'wind_aspect', minCount: 1 }, { prop: 'speed', minCount: 1 }],
    minMaterials: 2, powerBase: 0.3, uses: 1,
    description: 'Grants temporary wind-aspect flight speed. Single use — burns out after several li.',
    novel: 'All Novels' },
  { name: 'Thunder-Step Talisman', nameCn: '雷步符', intent: 'speed',
    requiredProperties: [{ prop: 'lightning_aspect', minCount: 1 }, { prop: 'speed', minCount: 1 }],
    minMaterials: 2, powerBase: 0.4, uses: 1,
    description: 'Teleports the user a short distance in a flash of lightning. The distance scales with the talisman\'s power.',
    novel: 'Renegade Immortal' },

  // ── Seal Talismans ──
  { name: 'Sealing Talisman', nameCn: '封印符', intent: 'seal',
    requiredProperties: [{ prop: 'seal', minCount: 1 }, { prop: 'binding', minCount: 1 }],
    minMaterials: 2, powerBase: 0.35, uses: 1,
    description: 'Seals a target\'s cultivation temporarily. The seal\'s strength depends on the scribe\'s skill vs the target\'s realm.',
    novel: 'All Novels' },
  { name: 'Soul-Seal Talisman', nameCn: '封魂符', intent: 'seal',
    requiredProperties: [{ prop: 'soul_touching', minCount: 1 }, { prop: 'seal', minCount: 1 }, { prop: 'spirit_binding', minCount: 1 }],
    minMaterials: 3, powerBase: 0.55, uses: 1,
    description: 'Seals a soul inside a vessel. Used for capturing beast souls or imprisoning enemies.',
    novel: 'Renegade Immortal' },
  { name: 'Restriction Talisman', nameCn: '禁制符', intent: 'seal',
    requiredProperties: [{ prop: 'seal', minCount: 2 }, { prop: 'suppression', minCount: 1 }],
    minMaterials: 3, powerBase: 0.5, uses: 1,
    description: 'Places a restriction on the target. Meng Hao\'s signature talisman — restricts movement and Qi gathering.',
    novel: 'ISSTH' },

  // ── Detection Talismans ──
  { name: 'Divine-Sense Talisman', nameCn: '神识符', intent: 'detect',
    requiredProperties: [{ prop: 'detection', minCount: 1 }, { prop: 'qi_channeling', minCount: 1 }],
    minMaterials: 2, powerBase: 0.25, uses: 1,
    description: 'Temporarily extends divine sense range. Invaluable for scouting dangerous areas.',
    novel: 'All Novels' },
  { name: 'Qi-Tracing Talisman', nameCn: '追踪符', intent: 'track',
    requiredProperties: [{ prop: 'tracking', minCount: 1 }, { prop: 'qi_channeling', minCount: 1 }],
    minMaterials: 2, powerBase: 0.3, uses: 1,
    description: 'Locks onto a Qi signature and points toward it. Loses the trail after several li.',
    novel: 'All Novels' },

  // ── Communication Talismans ──
  { name: 'Message Talisman', nameCn: '传音符', intent: 'communicate',
    requiredProperties: [{ prop: 'communication', minCount: 1 }, { prop: 'qi_channeling', minCount: 1 }],
    minMaterials: 2, powerBase: 0.2, uses: 1,
    description: 'Stores a brief spoken message. When torn, the message plays for anyone within range.',
    novel: 'All Novels' },

  // ── Purification Talismans ──
  { name: 'Purification Talisman', nameCn: '净化符', intent: 'purify',
    requiredProperties: [{ prop: 'purification', minCount: 1 }, { prop: 'qi_channeling', minCount: 1 }],
    minMaterials: 2, powerBase: 0.3, uses: 1,
    description: 'Purifies corrupted Qi or poisons in a small area. Essential for alchemists handling toxic ingredients.',
    novel: 'All Novels' },
  { name: 'Karmic-Cleansing Talisman', nameCn: '洗业符', intent: 'purify',
    requiredProperties: [{ prop: 'karmic_purify', minCount: 1 }, { prop: 'purification', minCount: 1 }],
    minMaterials: 3, powerBase: 0.6, uses: 1,
    description: 'Cleanses a portion of the user\'s karmic debt. Extremely rare — only the most skilled scribes can produce it.',
    novel: 'Renegade Immortal' },
];

// ─── Inscribed Talisman (the created item) ──────────────────────────

export interface InscribedTalisman {
  id: string;
  name: string;
  nameCn: string;
  intent: TalismanIntent;
  grade: string;
  power: number;              // 0..1
  stability: number;         // 0..1
  uses: number;               // remaining uses (1 = single-use)
  maxUses: number;
  effects: string[];
  realm: RealmId;            // scribe's realm when created
  novel?: string;
  discoveryText?: string;
  inscriptionQuality: string; // 'crude' | 'standard' | 'refined' | 'masterwork' | 'dao-perfect'
}

// ─── Grade helpers ──────────────────────────────────────────────────

const GRADE_ORDER: Record<string, number> = {
  mortal: 0, magical: 1, spirit: 2, earth: 3, heaven: 4, immortal: 5, dao: 6,
};

const REALM_MIN: Record<RealmId, number> = {
  mortal: 0, qi_condensation: 1, foundation: 2, core_formation: 3,
  nascent_soul: 4, soul_formation: 5, soul_transformation: 6, ascendant: 7,
  illusory_yin: 8, corporeal_yang: 8, nirvana_scryer: 9, nirvana_cleanser: 9,
  nirvana_fruit: 10, spirit_seizer: 10, true_immortal: 11, ancient: 12, paragon: 13, transcendence: 14,
};

// ─── Talisman inscription logic ──────────────────────────────────────

export interface TalismanResult {
  ok: boolean;
  message: string;
  talisman?: InscribedTalisman;
  explosion?: boolean;
  talismanSkillGain?: number;
}

function canUseMaterial(material: TalismanMaterial, playerRealm: RealmId): { ok: boolean; reason?: string } {
  const compGrade = GRADE_ORDER[material.grade] ?? 0;
  const playerGrade = REALM_MIN[playerRealm];
  if (compGrade > playerGrade + 2) {
    return { ok: false, reason: `Requires ${material.grade} realm` };
  }
  return { ok: true };
}

function aggregateProperties(materials: TalismanMaterial[]): Map<TalismanProperty, number> {
  const props = new Map<TalismanProperty, number>();
  for (const m of materials) {
    const seen = new Set<TalismanProperty>();
    for (const p of m.properties) {
      if (!seen.has(p)) {
        props.set(p, (props.get(p) ?? 0) + 1);
        seen.add(p);
      }
    }
  }
  return props;
}

function matchPattern(props: Map<TalismanProperty, number>, materialCount: number): TalismanPattern | null {
  let bestMatch: TalismanPattern | null = null;
  let bestSpecificity = -1;

  for (const pattern of TALISMAN_PATTERNS) {
    if (materialCount < pattern.minMaterials) continue;
    let matched = true;
    let specificity = 0;
    for (const req of pattern.requiredProperties) {
      const count = props.get(req.prop) ?? 0;
      if (count < req.minCount) { matched = false; break; }
      specificity += Math.min(count / req.minCount, 2);
    }
    if (matched && specificity > bestSpecificity) {
      bestSpecificity = specificity;
      bestMatch = pattern;
    }
  }
  return bestMatch;
}

function resolveTalismanGrade(
  materials: TalismanMaterial[],
  skill: number,
  playerRealm: RealmId,
): { grade: string; success: boolean; gradePenalty: number } {
  const maxMaterialGrade = Math.max(...materials.map(c => GRADE_ORDER[c.grade] ?? 0));
  const skillMaxGrade = skill < 0.1 ? 0 : skill < 0.3 ? 1 : skill < 0.5 ? 2 : skill < 0.7 ? 3 : skill < 0.9 ? 4 : 5;
  const playerMaxGrade = REALM_MIN[playerRealm] + 2;
  const effectiveMax = Math.min(maxMaterialGrade, skillMaxGrade, playerMaxGrade, 6);

  const gradePenalty = Math.max(0, maxMaterialGrade - effectiveMax) * 0.15;
  const baseSuccess = 0.6 + skill * 0.25 - gradePenalty;
  const success = Math.random() < Math.max(0.1, Math.min(0.95, baseSuccess));

  const gradeNames = ['mortal', 'magical', 'spirit', 'earth', 'heaven', 'immortal', 'dao'];
  return { grade: gradeNames[effectiveMax], success, gradePenalty };
}

function resolveEffects(
  pattern: TalismanPattern,
  props: Map<TalismanProperty, number>,
  grade: string,
  power: number,
): string[] {
  const effects: string[] = [];
  const gradeMul = 1 + (GRADE_ORDER[grade] ?? 0) * 0.3;

  switch (pattern.intent) {
    case 'attack':
      effects.push(`Damage: ${(power * gradeMul * 100).toFixed(0)}% of base attack`);
      if (props.has('fire_aspect')) effects.push('Fire-aspected damage');
      if (props.has('lightning_aspect')) effects.push('Lightning-aspected damage');
      if (props.has('soul_touching')) effects.push('Soul-piercing (ignores physical defense)');
      if (props.has('explosive')) effects.push('Area-of-effect explosion');
      if (props.has('cursed')) effects.push('Deals backlash damage to user');
      break;
    case 'defense':
      effects.push(`Blocks ${(power * gradeMul * 100).toFixed(0)}% of incoming damage`);
      if (props.has('barrier')) effects.push('Projects a physical barrier');
      if (props.has('soul_touching')) effects.push('Also wards soul attacks');
      if (props.has('blessed')) effects.push('Blessed — stronger vs cursed attacks');
      break;
    case 'heal':
      effects.push(`Restores ${(power * gradeMul * 30).toFixed(0)}% Qi`);
      if (props.has('mind_shielding')) effects.push('Suppresses heart demons for 1 hour');
      if (props.has('soul_touching')) effects.push('Also mends soul damage');
      break;
    case 'conceal':
      effects.push(`Concealment duration: ${Math.round(power * gradeMul * 10)} minutes`);
      if (props.has('dark_aspect')) effects.push('Shadow-melding (near-invisible)');
      break;
    case 'speed':
      effects.push(`Speed boost: ${(power * gradeMul * 200).toFixed(0)}% for ${Math.round(power * 3)} minutes`);
      if (props.has('lightning_aspect')) effects.push('Lightning teleport (instant, short range)');
      if (props.has('wind_aspect')) effects.push('Wind-propelled flight');
      break;
    case 'seal':
      effects.push(`Seal strength: ${(power * gradeMul * 100).toFixed(0)}%`);
      if (props.has('soul_touching')) effects.push('Can seal souls');
      if (props.has('suppression')) effects.push('Also suppresses cultivation within');
      break;
    case 'detect':
      effects.push(`Divine sense range: +${Math.round(power * gradeMul * 50)}%`);
      break;
    case 'track':
      effects.push(`Tracking range: ${Math.round(power * gradeMul * 10)} li`);
      effects.push('Locks onto Qi signature');
      break;
    case 'communicate':
      effects.push(`Message length: ${Math.round(power * 50)} words`);
      effects.push('Plays audibly when torn');
      break;
    case 'purify':
      effects.push(`Purification radius: ${Math.round(power * gradeMul * 5)} zhang`);
      if (props.has('karmic_purify')) effects.push('Cleanses karmic debt');
      break;
  }
  return effects;
}

function getInscriptionQuality(grade: string, power: number): string {
  const q = GRADE_ORDER[grade] ?? 0;
  if (q >= 6) return 'dao-perfect';
  if (q >= 4 || power > 0.85) return 'masterwork';
  if (q >= 3 || power > 0.7) return 'refined';
  if (q >= 2) return 'standard';
  return 'crude';
}

/**
 * Main function: inscribe a talisman from selected materials
 */
export function inscribeTalisman(
  skill: number,
  playerRealm: RealmId,
  materialNames: string[],
): TalismanResult {
  const materials = materialNames.map(n => TALISMAN_MATERIALS[n]).filter(Boolean);
  if (materials.length < 2) {
    return { ok: false, message: 'At least 2 materials are needed to inscribe a talisman.' };
  }

  // Must have at least one paper and one ink
  const hasPaper = materials.some(m => m.materialType === 'paper');
  const hasInk = materials.some(m => m.materialType === 'ink');
  if (!hasPaper || !hasInk) {
    return { ok: false, message: 'A talisman requires at least one paper and one ink.', talismanSkillGain: 0.003 };
  }

  const props = aggregateProperties(materials);

  // Explosion check
  const avgStability = materials.reduce((s, c) => s + c.stability, 0) / materials.length;
  const avgPotency = materials.reduce((s, c) => s + c.potency, 0) / materials.length;
  const explosionChance = Math.max(0, (1 - avgStability) * avgPotency * 0.5 - skill * 0.3);

  if (Math.random() < explosionChance) {
    const skillGain = 0.005 + Math.random() * 0.01;
    return {
      ok: false,
      message: `The talisman paper ignites! ${materials.map(c => c.name).join(', ')} react violently — your eyebrows singe. You learned from the failure.`,
      explosion: true,
      talismanSkillGain: skillGain,
    };
  }

  const { grade, success, gradePenalty } = resolveTalismanGrade(materials, skill, playerRealm);
  if (!success) {
    const skillGain = 0.003 + Math.random() * 0.008;
    return {
      ok: false,
      message: `The inscription fails — the Dao pattern dissipates. ${gradePenalty > 0 ? 'The materials are too high-grade for your current skill.' : 'The brush strokes lack clarity.'} You carefully store the ruined paper for recycling.`,
      talismanSkillGain: skillGain,
    };
  }

  // Match pattern
  const pattern = matchPattern(props, materials.length);

  // Procedural discovery
  let isDiscovery = false;
  let discoveryText: string | undefined;
  if (!pattern && skill > 0.4 && Math.random() < (skill - 0.4) * 0.4) {
    isDiscovery = true;
    const dominantProp = [...props.entries()].sort((a, b) => b[1] - a[1])[0]?.[0] ?? 'unknown';
    const intent = guessIntent(props);
    discoveryText = `DISCOVERY! You inscribe a pattern no jade slip describes — a ${formatPropertyName(dominantProp)}-aspect ${intent} talisman born from your unique understanding.`;
  }

  const basePower = pattern ? pattern.powerBase : 0.3 + Math.random() * 0.3;
  const power = Math.min(1, basePower * (1 + (GRADE_ORDER[grade] ?? 0) * 0.1));

  // Determine uses: single_use unless multi_use/sustained/self_repairing
  let uses = 1;
  if (props.has('multi_use') && props.has('sustained')) uses = 5;
  else if (props.has('multi_use')) uses = 3;
  else if (props.has('sustained')) uses = 2;
  if (pattern) uses = pattern.uses;
  if (props.has('self_repairing') && uses > 1) uses += 1;

  const effects = pattern
    ? resolveEffects(pattern, props, grade, power)
    : [`Generic ${grade} talisman with power ${(power * 100).toFixed(0)}%`];

  const name = isDiscovery
    ? `${formatPropertyName([...props.entries()].sort((a, b) => b[1] - a[1])[0]?.[0] ?? 'Unknown')}-Aspect Discovery Talisman`
    : pattern
      ? pattern.name
      : 'Unresolved Talisman';

  const nameCn = isDiscovery
    ? `${[...props.entries()].sort((a, b) => b[1] - a[1])[0]?.[0]?.split('_')[0] ?? '未知'}发现符`
    : pattern
      ? pattern.nameCn
      : '未名符';

  const intent = pattern?.intent ?? guessIntent(props);
  const quality = getInscriptionQuality(grade, power);
  const skillGain = 0.008 + Math.random() * 0.012 + (isDiscovery ? 0.02 : 0);

  const talisman: InscribedTalisman = {
    id: `talisman_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    name, nameCn, intent, grade, power,
    stability: Math.min(1, avgStability * (1 + skill * 0.2)),
    uses, maxUses: uses,
    effects, realm: playerRealm,
    novel: pattern?.novel,
    discoveryText: isDiscovery ? discoveryText : undefined,
    inscriptionQuality: quality,
  };

  return {
    ok: true,
    message: isDiscovery
      ? `The brush moves on its own — ${discoveryText} Power: ${(power * 100).toFixed(0)}%. Quality: ${quality}. Uses: ${uses}.`
      : `The ${pattern?.name} is complete! ${pattern?.description} Power: ${(power * 100).toFixed(0)}%. Quality: ${quality}. Uses: ${uses}.`,
    talisman,
    talismanSkillGain: skillGain,
  };
}

export function getAvailableTalismanMaterials(
  inventory: Record<string, number>,
  playerRealm: RealmId,
): { name: string; material: TalismanMaterial; available: boolean; reason?: string }[] {
  return Object.entries(TALISMAN_MATERIALS).map(([name, material]) => {
    const inInventory = (inventory[name] ?? 0) > 0;
    if (!inInventory) return { name, material, available: false, reason: 'Not in inventory' };
    const realmCheck = canUseMaterial(material, playerRealm);
    if (!realmCheck.ok) return { name, material, available: false, reason: realmCheck.reason };
    return { name, material, available: true };
  });
}

function guessIntent(props: Map<TalismanProperty, number>): TalismanIntent {
  const scores: Record<TalismanIntent, number> = {
    attack: 0, defense: 0, heal: 0, conceal: 0, speed: 0,
    seal: 0, detect: 0, summon: 0, communicate: 0, purify: 0, track: 0,
  };
  for (const [prop, count] of props) {
    if (prop === 'offensive' || prop === 'explosive') scores.attack += count;
    if (prop === 'defensive' || prop === 'barrier') scores.defense += count;
    if (prop === 'healing' || prop === 'mind_shielding') scores.heal += count;
    if (prop === 'concealment' || prop === 'dark_aspect') scores.conceal += count;
    if (prop === 'speed') scores.speed += count;
    if (prop === 'seal' || prop === 'binding' || prop === 'spirit_binding') scores.seal += count;
    if (prop === 'detection') scores.detect += count;
    if (prop === 'tracking') scores.track += count;
    if (prop === 'communication') scores.communicate += count;
    if (prop === 'purification' || prop === 'karmic_purify') scores.purify += count;
    if (prop === 'summoning') scores.summon += count;
  }
  return Object.entries(scores).sort((a, b) => b[1] - a[1])[0][0] as TalismanIntent;
}

function formatPropertyName(prop: string): string {
  return prop.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
}