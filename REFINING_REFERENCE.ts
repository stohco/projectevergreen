// src/lib/sim/refining.ts
// Property-based artifact refining system. The second of Er Gen's Four Great Arts.
// Materials have hidden properties. Artifacts are resolved from property combos.
// A master refiner can forge artifacts nobody has ever seen before.
// "The hammer falls. Metal screams. The Dao takes shape."

import type { RealmId, ElementId, Artifact, ArtifactType, ArtifactSubtype, TechniqueGrade } from './types';
import type { RNG } from './generators';
import { pick, uid } from './generators';

// ─── Refining Materials & Properties ──────────────────────────────────
// Every material possesses a set of these. Artifacts are resolved from property combos.

export type RefiningProperty =
  // Material traits
  | 'hard' | 'flexible' | 'light' | 'heavy' | 'conductive' | 'insulating' | 'brittle' | 'resilient'
  // Elemental aspects
  | 'fire_aspect' | 'water_aspect' | 'wind_aspect' | 'earth_aspect' | 'metal_aspect'
  | 'lightning_aspect' | 'ice_aspect' | 'dark_aspect' | 'light_aspect' | 'wood_aspect'
  // Spiritual traits
  | 'qi_conductive' | 'soul_preserving' | 'dao_resonant' | 'karmic_absorb'
  // Special
  | 'blood_drinking' | 'space_warping' | 'time_resonant' | 'self_repairing'
  | 'cursed' | 'blessed' | 'sentient_seed';

export type MaterialType = 'ore' | 'beast_part' | 'spirit_item' | 'element_core' | 'essence';

export interface RefiningMaterial {
  name: string;
  nameCn?: string;
  materialType: MaterialType;
  properties: RefiningProperty[];
  potency: number;       // 0..1 — material quality
  stability: number;     // 0..1 — how stable under refining heat
  grade: TechniqueGrade;
  novel?: string;
  blurb?: string;
}

// ─── Material Catalog (all from Er Gen novels) ─────────────────────────
export const REFINING_MATERIALS: Record<string, RefiningMaterial> = {
  // ── Ores ──
  'Iron Sand': { name: 'Iron Sand', nameCn: '铁砂', materialType: 'ore', properties: ['metal_aspect', 'hard', 'heavy', 'conductive'], potency: 0.15, stability: 0.95, grade: 'mortal', blurb: 'Common iron sand from riverbeds. The humblest refining material.' },
  'Cold-Iron Ore': { name: 'Cold-Iron Ore', nameCn: '寒铁矿石', materialType: 'ore', properties: ['metal_aspect', 'hard', 'ice_aspect', 'conductive', 'insulating'], potency: 0.35, stability: 0.8, grade: 'mortal', blurb: 'Ore that radiates cold. Used for frost-aspect weapons.' },
  'Spirit-Iron': { name: 'Spirit-Iron', nameCn: '灵铁', materialType: 'ore', properties: ['metal_aspect', 'hard', 'qi_conductive', 'conductive'], potency: 0.5, stability: 0.75, grade: 'magical', novel: 'Renegade Immortal', blurb: 'Iron saturated with ambient Qi. The standard material for magical treasures.' },
  'Star-Iron Ore': { name: 'Star-Iron Ore', nameCn: '星铁矿石', materialType: 'ore', properties: ['metal_aspect', 'hard', 'light_aspect', 'space_warping', 'resilient'], potency: 0.7, stability: 0.6, grade: 'spirit', novel: 'ISSTH', blurb: 'Iron that fell from the stars. Absorbs light and bends space slightly.' },
  'Thunder-Iron Essence': { name: 'Thunder-Iron Essence', nameCn: '雷铁精华', materialType: 'ore', properties: ['metal_aspect', 'lightning_aspect', 'hard', 'conductive', 'qi_conductive'], potency: 0.8, stability: 0.5, grade: 'spirit', novel: 'Renegade Immortal', blurb: 'Iron essence from thunder-struck mountains. Vibrates with residual lightning Dao.' },
  'Nether-Iron': { name: 'Nether-Iron', nameCn: '冥铁', materialType: 'ore', properties: ['metal_aspect', 'dark_aspect', 'hard', 'soul_preserving', 'cursed'], potency: 0.85, stability: 0.45, grade: 'immortal', novel: 'Renegade Immortal', blurb: 'Iron from the nether-realm. Absorbs souls. Used in demonic artifacts.' },
  'Heaven-Iron Fragment': { name: 'Heaven-Iron Fragment', nameCn: '天铁碎片', materialType: 'ore', properties: ['metal_aspect', 'hard', 'light_aspect', 'dao_resonant', 'space_warping', 'resilient'], potency: 0.95, stability: 0.3, grade: 'immortal', novel: 'ISSTH', blurb: 'A fragment of iron that once formed part of the Heavenly Court. Dao-resonant and space-bending.' },

  // ── Beast Parts ──
  'Beast Bone': { name: 'Beast Bone', nameCn: '兽骨', materialType: 'beast_part', properties: ['hard', 'heavy', 'brittle', 'earth_aspect'], potency: 0.2, stability: 0.8, grade: 'mortal', blurb: 'Raw beast bone. Brittle but hard — good for bone-based artifacts.' },
  'Spirit Beast Scale': { name: 'Spirit Beast Scale', nameCn: '灵兽鳞片', materialType: 'beast_part', properties: ['hard', 'resilient', 'flexible', 'qi_conductive'], potency: 0.5, stability: 0.75, grade: 'spirit', novel: 'All Novels', blurb: 'Scales from a spirit beast. Naturally hard yet flexible — perfect for armor.' },
  'Dragon Blood Essence': { name: 'Dragon Blood Essence', nameCn: '龙血精华', materialType: 'beast_part', properties: ['fire_aspect', 'blood_drinking', 'soul_preserving', 'dao_resonant', 'blessed'], potency: 0.95, stability: 0.3, grade: 'heaven', novel: 'All Novels', blurb: 'The condensed blood essence of a true dragon. Virtually priceless. Can bless any artifact with draconic power.' },
  'Phoenix Feather': { name: 'Phoenix Feather', nameCn: '凤凰羽毛', materialType: 'beast_part', properties: ['fire_aspect', 'light_aspect', 'resilient', 'self_repairing', 'blessed'], potency: 0.95, stability: 0.3, grade: 'heaven', novel: 'All Novels', blurb: 'A feather from a reborn phoenix. Self-repairing and blessed by the fire-aspect Dao.' },
  'Qilin Scale': { name: 'Qilin Scale', nameCn: '麒麟鳞', materialType: 'beast_part', properties: ['fire_aspect', 'earth_aspect', 'metal_aspect', 'resilient', 'blessed', 'dao_resonant'], potency: 0.95, stability: 0.35, grade: 'heaven', novel: 'ISSTH', blurb: 'A scale from the mythical qilin. Combines fire, earth, and metal aspects.' },
  'Thunder Toad Hide': { name: 'Thunder Toad Hide', nameCn: '雷蟒皮', materialType: 'beast_part', properties: ['lightning_aspect', 'resilient', 'flexible', 'insulating', 'conductive'], potency: 0.55, stability: 0.7, grade: 'spirit', novel: 'Renegade Immortal', blurb: 'Hide of a thunder toad. Naturally insulates against lightning — or channels it.' },
  'Mosquito Beast Proboscis': { name: 'Mosquito Beast Proboscis', nameCn: '蚊兽口器', materialType: 'beast_part', properties: ['dark_aspect', 'blood_drinking', 'hard', 'cursed', 'space_warping'], potency: 0.8, stability: 0.4, grade: 'immortal', novel: 'Renegade Immortal', blurb: "The proboscis of a Mosquito Beast — the same material Wang Lin forged into the Soul-Piercer Spear." },
  'Cloud-Silk Deer Antler': { name: 'Cloud-Silk Deer Antler', nameCn: '云丝鹿角', materialType: 'beast_part', properties: ['wind_aspect', 'flexible', 'light', 'qi_conductive'], potency: 0.5, stability: 0.8, grade: 'spirit', novel: 'Renegade Immortal', blurb: 'Antlers that shimmer like cloud-silk. Used for lightweight evasion robes.' },

  // ── Spirit Items ──
  'Spirit Stone (Raw)': { name: 'Spirit Stone (Raw)', nameCn: '灵石（原矿）', materialType: 'spirit_item', properties: ['qi_conductive', 'light', 'resilient'], potency: 0.3, stability: 0.9, grade: 'mortal', blurb: 'Unrefined spirit stone. A common power source for artifact activation.' },
  'Jade Essence': { name: 'Jade Essence', nameCn: '玉髓', materialType: 'spirit_item', properties: ['soul_preserving', 'qi_conductive', 'resilient', 'blessed'], potency: 0.5, stability: 0.8, grade: 'spirit', novel: 'All Novels', blurb: 'Purified jade essence. Used for soul-preserving artifacts and communication tokens.' },
  'Soul Crystal': { name: 'Soul Crystal', nameCn: '魂晶', materialType: 'spirit_item', properties: ['soul_preserving', 'dark_aspect', 'dao_resonant', 'karmic_absorb'], potency: 0.7, stability: 0.55, grade: 'spirit', novel: 'Renegade Immortal', blurb: 'Crystallized soul energy. Used in soul flags and soul-sealing artifacts.' },
  'Spatial Crystal': { name: 'Spatial Crystal', nameCn: '空间水晶', materialType: 'spirit_item', properties: ['space_warping', 'light', 'resilient', 'dao_resonant'], potency: 0.8, stability: 0.45, grade: 'immortal', novel: 'All Novels', blurb: 'A crystal that contains a pocket of folded space. Essential for storage treasures and spatial artifacts.' },
  'Karmic Thread Spool': { name: 'Karmic Thread Spool', nameCn: '因果线轴', materialType: 'spirit_item', properties: ['karmic_absorb', 'time_resonant', 'soul_preserving', 'dao_resonant'], potency: 0.85, stability: 0.4, grade: 'immortal', novel: 'ISSTH', blurb: 'Threads spun from karmic connections. Meng Hao used these for his restriction arts.' },

  // ── Element Cores ──
  'Fire Crystal Core': { name: 'Fire Crystal Core', nameCn: '火晶核心', materialType: 'element_core', properties: ['fire_aspect', 'qi_conductive', 'brittle', 'light'], potency: 0.55, stability: 0.5, grade: 'spirit', blurb: 'A crystalized fire-aspect core. Embed in weapons for flame damage.' },
  'Water Pearl': { name: 'Water Pearl', nameCn: '水灵珠', materialType: 'element_core', properties: ['water_aspect', 'ice_aspect', 'flexible', 'qi_conductive'], potency: 0.55, stability: 0.6, grade: 'spirit', blurb: 'A pearl formed from water-aspect Qi. Used in flow-based artifacts.' },
  'Wind Essence Stone': { name: 'Wind Essence Stone', nameCn: '风灵石', materialType: 'element_core', properties: ['wind_aspect', 'light', 'flexible', 'qi_conductive'], potency: 0.55, stability: 0.55, grade: 'spirit', blurb: 'Condensed wind essence. Makes weapons lighter and faster.' },
  'Earth Core Fragment': { name: 'Earth Core Fragment', nameCn: '地核碎片', materialType: 'element_core', properties: ['earth_aspect', 'heavy', 'hard', 'resilient'], potency: 0.6, stability: 0.65, grade: 'spirit', blurb: 'A fragment from deep underground. Adds weight and durability.' },
  'Thunder Essence Orb': { name: 'Thunder Essence Orb', nameCn: '雷灵珠', materialType: 'element_core', properties: ['lightning_aspect', 'conductive', 'qi_conductive', 'brittle'], potency: 0.65, stability: 0.45, grade: 'spirit', novel: 'Renegade Immortal', blurb: 'An orb of pure lightning essence. Dangerous to handle — can discharge unpredictably.' },

  // ── Essences (highest tier) ──
  'Origin Essence': { name: 'Origin Essence', nameCn: '本源精', materialType: 'essence', properties: ['dao_resonant', 'soul_preserving', 'qi_conductive', 'blessed', 'sentient_seed', 'self_repairing'], potency: 1.0, stability: 0.2, grade: 'dao', blurb: 'The raw essence of creation. Can birth a sentient artifact.' },
  'Heaven-Devouring Essence': { name: 'Heaven-Devouring Essence', nameCn: '吞天精', materialType: 'essence', properties: ['dark_aspect', 'space_warping', 'karmic_absorb', 'cursed', 'dao_resonant', 'blood_drinking'], potency: 1.0, stability: 0.15, grade: 'dao', novel: 'Renegade Immortal', blurb: 'Essence extracted from a Heaven-Devouring Beast. Corrupts everything it touches — but holds incomparable power.' },
};

// ─── Artifact Pattern Resolution ──────────────────────────────────────
// Like alchemy: property combinations resolve into artifact forms.

export interface RefinePattern {
  name: string;
  requiredProperties: RefiningProperty[];
  forbiddenProperties?: RefiningProperty[];
  minPotency: number;
  artifactType: ArtifactType;
  artifactSubtype: ArtifactSubtype;
  baseEffects: string[];
  minGradeForEffects: TechniqueGrade;
}

const EFFECT_MAP: Record<RefiningProperty, string> = {
  hard: 'armor-penetration', flexible: 'self-repair', light: 'evasion-boost',
  heavy: 'heavy-strike', conductive: 'qi-channeling', insulating: 'tribulation-resist',
  brittle: 'fragile', resilient: 'self-repair',
  fire_aspect: 'fire-damage', water_aspect: 'frost-slow', wind_aspect: 'speed-boost',
  earth_aspect: 'kinetic-absorb', metal_aspect: 'armor-penetration',
  lightning_aspect: 'chain-lightning', ice_aspect: 'frost-slow',
  dark_aspect: 'soul-damage', light_aspect: 'domain-cut',
  wood_aspect: 'life-drain', qi_conductive: 'qi-channeling',
  soul_preserving: 'soul-ward', dao_resonant: ' dao-attunement',
  karmic_absorb: 'karmic-ward', blood_drinking: 'blood-drain',
  space_warping: 'spatial-fold', time_resonant: 'time-dilation',
  self_repairing: 'self-repair', cursed: 'karmic-staining',
  blessed: 'karmic-ward', sentient_seed: 'awakening-potential',
};

export const KNOWN_REFINE_PATTERNS: RefinePattern[] = [
  // ── Weapons ──
  { name: 'Flying Sword', requiredProperties: ['metal_aspect', 'hard', 'qi_conductive'], minPotency: 0.2, artifactType: 'weapon', artifactSubtype: 'flying_sword', baseEffects: ['qi-channeling', 'armor-penetration'], minGradeForEffects: 'mortal' },
  { name: 'Flame Sword', requiredProperties: ['metal_aspect', 'hard', 'fire_aspect'], minPotency: 0.4, artifactType: 'weapon', artifactSubtype: 'flying_sword', baseEffects: ['fire-damage', 'armor-penetration'], minGradeForEffects: 'spirit' },
  { name: 'Thunder Blade', requiredProperties: ['metal_aspect', 'hard', 'lightning_aspect'], minPotency: 0.5, artifactType: 'weapon', artifactSubtype: 'flying_sword', baseEffects: ['chain-lightning', 'qi-channeling'], minGradeForEffects: 'spirit' },
  { name: 'Soul-Severing Sword', requiredProperties: ['metal_aspect', 'dark_aspect', 'soul_preserving'], minPotency: 0.6, artifactType: 'weapon', artifactSubtype: 'flying_sword', baseEffects: ['soul-damage', 'armor-penetration'], minGradeForEffects: 'immortal' },
  { name: 'Space-Edge Sword', requiredProperties: ['metal_aspect', 'space_warping', 'dao_resonant'], minPotency: 0.8, artifactType: 'weapon', artifactSubtype: 'flying_sword', baseEffects: ['spatial-fold', 'domain-cut'], minGradeForEffects: 'immortal' },
  { name: 'Heavy Saber', requiredProperties: ['metal_aspect', 'heavy', 'hard'], minPotency: 0.3, artifactType: 'weapon', artifactSubtype: 'saber', baseEffects: ['heavy-strike', 'armor-penetration'], minGradeForEffects: 'mortal' },
  { name: 'Spirit Bow', requiredProperties: ['flexible', 'light', 'wind_aspect'], minPotency: 0.4, artifactType: 'weapon', artifactSubtype: 'bow', baseEffects: ['speed-boost', 'long-range'], minGradeForEffects: 'spirit' },
  { name: 'Soul-Piercer', requiredProperties: ['dark_aspect', 'space_warping', 'hard'], minPotency: 0.7, artifactType: 'weapon', artifactSubtype: 'spear', baseEffects: ['soul-damage', 'armor-penetration', 'cursed-wound'], minGradeForEffects: 'immortal' },
  { name: 'Spirit-Silk Whip', requiredProperties: ['flexible', 'wood_aspect', 'qi_conductive'], minPotency: 0.35, artifactType: 'weapon', artifactSubtype: 'spear', baseEffects: ['binding', 'qi-channeling'], minGradeForEffects: 'magical' },

  // ── Defense ──
  { name: 'Iron Shield', requiredProperties: ['metal_aspect', 'heavy', 'hard'], minPotency: 0.2, artifactType: 'defense', artifactSubtype: 'shield', baseEffects: ['kinetic-absorb'], minGradeForEffects: 'mortal' },
  { name: 'Spirit-Weave Armor', requiredProperties: ['flexible', 'resilient', 'qi_conductive'], minPotency: 0.4, artifactType: 'defense', artifactSubtype: 'armor', baseEffects: ['qi-absorb', 'self-repair'], minGradeForEffects: 'spirit' },
  { name: 'Cloud-Silk Robe', requiredProperties: ['wind_aspect', 'light', 'flexible'], minPotency: 0.4, artifactType: 'defense', artifactSubtype: 'robe', baseEffects: ['evasion-boost', 'concealment-aura'], minGradeForEffects: 'spirit' },
  { name: 'Netherworld Armor', requiredProperties: ['dark_aspect', 'soul_preserving', 'resilient'], minPotency: 0.7, artifactType: 'defense', artifactSubtype: 'armor', baseEffects: ['demonic-resistance', 'fear-aura', 'self-repair'], minGradeForEffects: 'immortal' },
  { name: 'Nine-Heavens Barrier', requiredProperties: ['light_aspect', 'dao_resonant', 'resilient'], minPotency: 0.85, artifactType: 'defense', artifactSubtype: 'shield', baseEffects: ['barrier-projection', 'tribulation-resist', 'domain-block'], minGradeForEffects: 'immortal' },
  { name: 'Demonic Hide Armor', requiredProperties: ['dark_aspect', 'resilient', 'blood_drinking'], forbiddenProperties: ['blessed'], minPotency: 0.5, artifactType: 'defense', artifactSubtype: 'armor', baseEffects: ['demonic-resistance', 'fear-aura'], minGradeForEffects: 'spirit' },

  // ── Utility ──
  { name: 'Storage Bag', requiredProperties: ['space_warping', 'light'], minPotency: 0.3, artifactType: 'utility', artifactSubtype: 'storage_bag', baseEffects: ['spatial-storage'], minGradeForEffects: 'mortal' },
  { name: 'Storage Treasure', requiredProperties: ['space_warping', 'dao_resonant', 'resilient'], minPotency: 0.7, artifactType: 'utility', artifactSubtype: 'storage_bag', baseEffects: ['spatial-storage', 'self-repair', 'time-dilation'], minGradeForEffects: 'immortal' },
  { name: 'Pill Furnace', requiredProperties: ['fire_aspect', 'hard', 'qi_conductive'], minPotency: 0.4, artifactType: 'utility', artifactSubtype: 'pill_furnace', baseEffects: ['pill-refinement', 'herb-processing', 'qi-focusing'], minGradeForEffects: 'spirit' },
  { name: 'Array Disk', requiredProperties: ['qi_conductive', 'resilient', 'flexible'], minPotency: 0.45, artifactType: 'utility', artifactSubtype: 'array_disk', baseEffects: ['portable-formation', 'trap-deployment', 'barrier-projection'], minGradeForEffects: 'spirit' },
  { name: 'Communication Token', requiredProperties: ['soul_preserving', 'light', 'qi_conductive'], minPotency: 0.25, artifactType: 'utility', artifactSubtype: 'token', baseEffects: ['long-range-message', 'sect-identification'], minGradeForEffects: 'mortal' },
  { name: 'Soul Flag', requiredProperties: ['dark_aspect', 'soul_preserving', 'karmic_absorb'], minPotency: 0.5, artifactType: 'weapon', artifactSubtype: 'flag', baseEffects: ['soul-capture', 'soul-attack', 'summon-captured-souls'], minGradeForEffects: 'spirit' },
  { name: 'Restriction Flag', requiredProperties: ['karmic_absorb', 'resilient', 'qi_conductive'], minPotency: 0.5, artifactType: 'utility', artifactSubtype: 'array_disk', baseEffects: ['portable-restriction', 'trap-deployment', 'formation-seal'], minGradeForEffects: 'spirit' },
  { name: 'Spirit Boat', requiredProperties: ['wind_aspect', 'resilient', 'qi_conductive'], minPotency: 0.45, artifactType: 'utility', artifactSubtype: 'spirit_boat', baseEffects: ['flight', 'passenger-transport', 'qi-shield'], minGradeForEffects: 'magical' },
  { name: 'Jade Slip', requiredProperties: ['soul_preserving', 'light', 'resilient'], minPotency: 0.2, artifactType: 'utility', artifactSubtype: 'communication', baseEffects: ['information-storage', 'technique-recording'], minGradeForEffects: 'mortal' },
  { name: 'Seal Stamp', requiredProperties: ['earth_aspect', 'hard', 'dao_resonant'], minPotency: 0.6, artifactType: 'utility', artifactSubtype: 'seal_stamp', baseEffects: ['seal-amplification', 'domain-projection'], minGradeForEffects: 'immortal' },
  { name: 'Blood-Moon Seal', requiredProperties: ['blood_drinking', 'dark_aspect', 'dao_resonant'], forbiddenProperties: ['blessed'], minPotency: 0.7, artifactType: 'weapon', artifactSubtype: 'seal_stamp', baseEffects: ['blood-drain', 'soul-damage', 'karmic-staining'], minGradeForEffects: 'immortal' },
];

// ─── Refining Result ──────────────────────────────────────────────────

export interface RefineResult {
  artifact: Artifact | null;
  success: boolean;
  explosion: boolean;
  grade: TechniqueGrade | 'failed';
  message: string;
  discoveryText?: string; // set when a new artifact type is discovered
  refiningSkillGain: number;
  materialsUsed: string[];
}

// ─── Grade Power Tables ───────────────────────────────────────────────

const GRADE_POWER: Record<TechniqueGrade, { atk: number; def: number; dur: number }> = {
  mortal: { atk: 5, def: 3, dur: 50 },
  magical: { atk: 20, def: 12, dur: 70 },
  spirit: { atk: 80, def: 50, dur: 85 },
  immortal: { atk: 300, def: 200, dur: 95 },
  dao: { atk: 1500, def: 1000, dur: 100 },
};

const REALM_ORDER: Record<RealmId, number> = {
  mortal: 0, qi_condensation: 1, foundation: 2, core_formation: 3,
  nascent_soul: 4, soul_formation: 5, soul_transformation: 6, ascendant: 7,
  illusory_yin: 8, corporeal_yang: 8, nirvana_scryer: 9, nirvana_cleanser: 9,
  nirvana_fruit: 10, spirit_seizer: 10, true_immortal: 11, ancient: 12, paragon: 13, transcendence: 14,
};

const GRADE_ORDER: Record<TechniqueGrade, number> = { mortal: 0, magical: 1, spirit: 2, immortal: 3, dao: 4 };

// ─── Grade from Materials + Skill ─────────────────────────────────────

function resolveGrade(maxMaterialGrade: TechniqueGrade, avgPotency: number, skill: number, rng: RNG): { grade: TechniqueGrade | 'failed'; skillCapped: boolean } {
  const maxOrd = GRADE_ORDER[maxMaterialGrade] ?? 0;
  // Skill determines how much of the material's potential the refiner can unlock
  const skillTier = skill < 0.1 ? 0 : skill < 0.3 ? 1 : skill < 0.5 ? 2 : skill < 0.7 ? 3 : skill < 0.9 ? 4 : 5;
  const reachableOrd = Math.min(maxOrd, skillTier);
  // Some randomness: can exceed by 1 with luck, or fall short by 1
  const finalOrd = Math.max(0, Math.min(4, reachableOrd + (rng() < 0.15 ? 1 : 0) - (rng() < 0.1 ? 1 : 0)));
  const gradeNames: TechniqueGrade[] = ['mortal', 'magical', 'spirit', 'immortal', 'dao'];
  return { grade: gradeNames[finalOrd], skillCapped: finalOrd < maxOrd };
}

// ─── Determine Element Affinity ──────────────────────────────────────

const ELEMENT_MAP: Partial<Record<RefiningProperty, ElementId>> = {
  fire_aspect: 'fire', water_aspect: 'water', wind_aspect: 'wind',
  earth_aspect: 'earth', metal_aspect: 'metal', lightning_aspect: 'lightning',
  ice_aspect: 'water', dark_aspect: 'dark', light_aspect: 'light', wood_aspect: 'wood',
};

function dominantElement(properties: RefiningProperty[]): ElementId | undefined {
  const counts = new Map<ElementId, number>();
  for (const p of properties) {
    const el = ELEMENT_MAP[p];
    if (el) counts.set(el, (counts.get(el) ?? 0) + 1);
  }
  let best: ElementId | undefined;
  let bestCount = 0;
  for (const [el, count] of counts) {
    if (count > bestCount) { best = el; bestCount = count; }
  }
  return best;
}

// ─── Resolve Min Realm from Grade ────────────────────────────────────

function gradeToMinRealm(grade: TechniqueGrade): RealmId {
  const map: Record<TechniqueGrade, RealmId> = {
    mortal: 'mortal', magical: 'qi_condensation', spirit: 'foundation',
    immortal: 'nascent_soul', dao: 'soul_formation',
  };
  return map[grade];
}

// ─── Build Special Effects ───────────────────────────────────────────

function buildSpecialEffects(properties: RefiningProperty[], pattern: RefinePattern | null, grade: TechniqueGrade, avgPotency: number): string[] {
  const effects = new Set<string>();
  // Start with pattern base effects
  if (pattern) {
    for (const e of pattern.baseEffects) effects.add(e);
  }
  // Add effects from individual properties (capped at 4 bonus effects)
  let bonusCount = 0;
  for (const p of properties) {
    const mapped = EFFECT_MAP[p];
    if (mapped && !effects.has(mapped) && bonusCount < 4) {
      effects.add(mapped);
      bonusCount++;
    }
  }
  // High potency adds a bonus
  if (avgPotency > 0.85) effects.add('overclocking-compatible');
  // Dao-grade gets domain interaction
  if (grade === 'dao') effects.add('domain-resonance');
  return [...effects];
}

// ─── Refine an Artifact ──────────────────────────────────────────────

export function refineArtifact(
  rng: RNG,
  materialNames: string[],
  refiningSkill: number,
  playerRealm: RealmId,
  playerName: string,
): RefineResult {
  // Gather materials
  const materials = materialNames.map((n) => REFINING_MATERIALS[n]).filter(Boolean);
  if (materials.length < 2) {
    return { artifact: null, success: false, explosion: false, grade: 'failed', message: 'Need at least 2 materials to refine.', refiningSkillGain: 0, materialsUsed: materialNames };
  }

  // Aggregate properties
  const propertySet = new Set<RefiningProperty>();
  for (const m of materials) {
    for (const p of m.properties) propertySet.add(p);
  }
  const properties = [...propertySet];

  // Aggregate potency + stability
  const avgPotency = materials.reduce((s, m) => s + m.potency, 0) / materials.length;
  const avgStability = materials.reduce((s, m) => s + m.stability, 0) / materials.length;

  // Find highest material grade
  let maxGradeOrd = 0;
  for (const m of materials) {
    const ord = GRADE_ORDER[m.grade] ?? 0;
    if (ord > maxGradeOrd) maxGradeOrd = ord;
  }
  const maxMaterialGrade: TechniqueGrade = (['mortal', 'magical', 'spirit', 'immortal', 'dao'] as TechniqueGrade[])[maxGradeOrd];

  // Explosion check: low stability + high potency + high grade + low skill = boom
  const explosionChance = Math.max(0,
    (1 - avgStability) * 0.4 +
    (avgPotency - 0.7) * 0.25 +
    (maxGradeOrd - 2) * 0.05 -
    refiningSkill * 0.15
  );
  if (rng() < explosionChance) {
    const skillGain = 0.005;
    return {
      artifact: null, success: false, explosion: true, grade: 'failed',
      message: 'The furnace explodes! Scorching Qi sprays everywhere. Your materials are destroyed.',
      refiningSkillGain: skillGain, materialsUsed: materialNames,
    };
  }

  // Success check: base 60% + skill bonus - grade penalty
  const baseSuccess = 0.6 + refiningSkill * 0.25 - maxGradeOrd * 0.05;
  if (rng() > baseSuccess) {
    const skillGain = 0.01;
    return {
      artifact: null, success: false, explosion: false, grade: 'failed',
      message: 'The materials fail to fuse. A twisted lump of slag cools in the furnace.',
      refiningSkillGain: skillGain, materialsUsed: materialNames,
    };
  }

  // Resolve grade
  const { grade, skillCapped } = resolveGrade(maxMaterialGrade, avgPotency, refiningSkill, rng);

  // Match against known patterns
  const matched = KNOWN_REFINE_PATTERNS.filter((p) => {
    if (p.forbiddenProperties && p.forbiddenProperties.some((fp) => properties.includes(fp))) return false;
    if (p.requiredProperties.some((rp) => !properties.includes(rp))) return false;
    if (avgPotency < p.minPotency) return false;
    return true;
  });

  let pattern: RefinePattern | null = null;
  let artifactName = '';

  if (matched.length > 0) {
    // Best match = most required properties (most specific)
    pattern = matched.sort((a, b) => b.requiredProperties.length - a.requiredProperties.length)[0];
    artifactName = pattern.name;
    // Add element prefix if applicable
    const el = dominantElement(properties);
    if (el && el !== 'metal' && el !== 'earth') {
      const elPrefix: Record<string, string> = { fire: 'Flame', water: 'Frost', wind: 'Wind', lightning: 'Thunder', dark: 'Shadow', light: 'Radiant', wood: 'Jade' };
      const prefix = elPrefix[el];
      if (prefix && !artifactName.toLowerCase().includes(prefix.toLowerCase())) {
        artifactName = `${prefix} ${artifactName}`;
      }
    }
  } else if (refiningSkill > 0.4 && rng() < refiningSkill * 0.25) {
    // Procedural discovery!
    const el = dominantElement(properties);
    const elName = el ? el.charAt(0).toUpperCase() + el.slice(1) : 'Void';
    const baseType = properties.includes('hard') && properties.includes('metal_aspect')
      ? 'Sword'
      : properties.includes('flexible') && properties.includes('light')
        ? 'Robe'
        : properties.includes('heavy') && properties.includes('resilient')
          ? 'Shield'
          : properties.includes('space_warping')
            ? 'Treasure'
            : 'Artifact';

    artifactName = `${playerName}'s ${elName} ${baseType}`;
    const discoveryText = `You forged something never seen before: ${artifactName}! The properties ${properties.slice(0, 5).join(', ')} combined in a way no known pattern describes. A true Grandmaster Refiner's creation!`;

    // Still determine type/subtype heuristically
    const type = properties.includes('hard') || properties.includes('heavy') ? 'weapon' as ArtifactType : properties.includes('resilient') ? 'defense' as ArtifactType : 'utility' as ArtifactType;
    const subtype = properties.includes('metal_aspect') ? 'flying_sword' as ArtifactSubtype : properties.includes('space_warping') ? 'storage_bag' as ArtifactSubtype : properties.includes('fire_aspect') ? 'pill_furnace' as ArtifactSubtype : 'token' as ArtifactSubtype;

    const gp = GRADE_POWER[grade];
    const element = dominantElement(properties);
    const effects = buildSpecialEffects(properties, null, grade, avgPotency);
    const skillGain = 0.02 + 0.05; // discovery bonus
    const artifact: Artifact = {
      id: uid('ref', rng),
      name: artifactName,
      type,
      subtype,
      grade,
      elementAffinity: element,
      attackPower: type === 'weapon' ? Math.round(gp.atk * avgPotency) : 0,
      defenseRating: type === 'defense' ? Math.round(gp.def * avgPotency) : 0,
      durability: gp.dur,
      maxDurability: gp.dur,
      specialEffects: effects,
      minRealm: gradeToMinRealm(grade),
      weight: properties.includes('light') ? 1 : properties.includes('heavy') ? 5 : 2,
      blurb: discoveryText,
      equipped: false,
    };

    return { artifact, success: true, explosion: false, grade, message: `Forged: ${artifactName} (${grade})! ${discoveryText}`, discoveryText, refiningSkillGain: skillGain, materialsUsed: materialNames };
  } else {
    // No pattern matched, not skilled enough to discover → generic result
    const skillGain = 0.01;
    return {
      artifact: null, success: false, explosion: false, grade: 'failed',
      message: 'The materials fused into an inert shape. The property combination is unknown to you — perhaps with greater skill you could discover something.',
      refiningSkillGain: skillGain, materialsUsed: materialNames,
    };
  }

  // ── Build the artifact from matched pattern ──
  const gp = GRADE_POWER[grade];
  const element = dominantElement(properties);
  const effects = buildSpecialEffects(properties, pattern, grade, avgPotency);
  const isWeapon = pattern.artifactType === 'weapon';
  const isDefense = pattern.artifactType === 'defense';

  // Realm scaling: if player realm is below artifact's natural min, reduce stats
  const artifactRealmOrd = REALM_ORDER[gradeToMinRealm(grade)] ?? 0;
  const playerRealmOrd = REALM_ORDER[playerRealm] ?? 0;
  const realmFactor = playerRealmOrd >= artifactRealmOrd ? 1.0 : 0.5 + 0.5 * (playerRealmOrd / Math.max(1, artifactRealmOrd));

  const artifact: Artifact = {
    id: uid('ref', rng),
    name: artifactName,
    type: pattern.artifactType,
    subtype: pattern.artifactSubtype,
    grade,
    elementAffinity: element,
    attackPower: isWeapon ? Math.round(gp.atk * avgPotency * realmFactor) : 0,
    defenseRating: isDefense ? Math.round(gp.def * avgPotency * realmFactor) : 0,
    durability: gp.dur,
    maxDurability: gp.dur,
    specialEffects: effects,
    minRealm: gradeToMinRealm(grade),
    weight: properties.includes('light') ? 1 : properties.includes('heavy') ? 5 : 2,
    blurb: `Refined by ${playerName} from ${materials.map(m => m.name).join(', ')}.`,
    equipped: false,
  };

  const skillGain = 0.01 + (pattern.requiredProperties.length > 4 ? 0.015 : 0) + (skillCapped ? 0.005 : 0);
  const cappedNote = skillCapped ? ' (skill-capped grade)' : '';
  return {
    artifact, success: true, explosion: false, grade,
    message: `Forged: ${artifactName} (${grade})${cappedNote}. ${effects.length} special effects.`,
    refiningSkillGain: skillGain, materialsUsed: materialNames,
  };
}

// ─── Available Materials (based on inventory) ─────────────────────────

export function availableRefiningMaterials(
  inventory: { spirit_stone?: number; beast_core?: number; spirit_herb?: number; celestial_jade?: number },
  playerRealmOrd: number,
): { name: string; material: RefiningMaterial; available: boolean; reason?: string }[] {
  const results: { name: string; material: RefiningMaterial; available: boolean; reason?: string }[] = [];

  for (const [name, mat] of Object.entries(REFINING_MATERIALS)) {
    const gradeOrd = GRADE_ORDER[mat.grade] ?? 0;
    let available = false;
    let reason: string | undefined;

    // Mortal materials: always available (common ore/herbs)
    if (gradeOrd <= 0) {
      available = true;
    }
    // Magical materials: need spirit stones
    else if (gradeOrd === 1) {
      available = (inventory.spirit_stone ?? 0) >= 5;
      if (!available) reason = 'Need 5 spirit stones';
    }
    // Spirit materials: need beast cores or spirit stones
    else if (gradeOrd === 2) {
      available = (inventory.beast_core ?? 0) >= 1 || (inventory.spirit_stone ?? 0) >= 20;
      if (!available) reason = 'Need 1 beast core or 20 spirit stones';
    }
    // Immortal materials: need celestial jade or many beast cores
    else if (gradeOrd === 3) {
      available = (inventory.celestial_jade ?? 0) >= 1 || (inventory.beast_core ?? 0) >= 5;
      if (!available) reason = 'Need 1 celestial jade or 5 beast cores';
    }
    // Dao materials: need celestial jade
    else if (gradeOrd >= 4) {
      available = (inventory.celestial_jade ?? 0) >= 3;
      if (!available) reason = 'Need 3 celestial jade';
    }

    // Realm gate: can't use materials 2+ grades above your realm
    if (gradeOrd > playerRealmOrd + 2) {
      available = false;
      reason = 'Realm too low to handle this material';
    }

    results.push({ name, material: mat, available, reason });
  }

  return results;
}