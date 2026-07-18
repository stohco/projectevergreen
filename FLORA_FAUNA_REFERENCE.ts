// src/lib/sim/flora-fauna.ts
// Deep flora + fauna catalog: spirit herbs and the full 5-layer bestiary with food-webs,
// behavior scripts, modular loot tables, and special flags (destinedCompanion, gastroRealm, dualForm).
// Faithful to Er Gen's world across all cosmology tiers.

import type { SpiritHerb, BeastSpecies, BeastDrop, HerbGrade, HerbEnvironment, HerbGrowthStage, HerbHiddenProperties, RealmId, BeastTier } from './types';
import type { RNG } from './generators';
import { pick, pickN, uid, randInt } from './generators';
import { DEDUPED_CATALOG, TOTAL_SPECIES_COUNT } from './bestiary-catalog';

// ─── Growth stage durations by grade (in sim years) ────────────
// Higher-grade herbs take dramatically longer to mature — a dao herb
// may take centuries. This makes finding a mature heaven-grade herb rare.
const STAGE_DURATIONS: Record<HerbGrade, Record<HerbGrowthStage, [number, number]>> = {
  mortal:   { seed: [1,2], sprout: [1,2], young: [1,3], mature: [3,6], flowering: [1,2], fruiting: [1,2], dormant: [2,4], dead: [0,0] },
  spirit:   { seed: [2,4], sprout: [2,3], young: [3,5], mature: [5,10], flowering: [2,4], fruiting: [2,3], dormant: [4,8], dead: [0,0] },
  earth:    { seed: [5,10], sprout: [5,8], young: [8,15], mature: [15,30], flowering: [5,10], fruiting: [5,8], dormant: [10,20], dead: [0,0] },
  heaven:   { seed: [20,40], sprout: [20,30], young: [30,60], mature: [60,120], flowering: [20,40], fruiting: [20,30], dormant: [40,80], dead: [0,0] },
  dao:      { seed: [50,100], sprout: [50,80], young: [80,150], mature: [150,300], flowering: [50,100], fruiting: [50,80], dormant: [100,200], dead: [0,0] },
};

// What stage a herb starts at when the world is seeded
const SEED_START_STAGES: HerbGrowthStage[] = ['sprout', 'young', 'young', 'mature', 'mature', 'flowering', 'dormant'];

// Possible mutations (rare environmental influences)
const MUTATIONS: (string | null)[] = [
  null, null, null, null, null, null, null, null, // 80% no mutation
  'fire-aspected', 'water-aspected', 'wind-aspected', 'earth-aspected', 'lightning-aspected',
  'blood-soaked', 'karmic-stained', 'void-touched', 'yin-saturated', 'yang-saturated',
];

function rollMutation(rng: RNG, grade: HerbGrade): string | null {
  // Higher-grade herbs have higher mutation chance
  const chance = grade === 'mortal' ? 0.02 : grade === 'spirit' ? 0.05 : grade === 'earth' ? 0.12 : grade === 'heaven' ? 0.2 : 0.3;
  if (rng() > chance) return null;
  return pick(rng, MUTATIONS);
}

function initHerbProperties(rng: RNG, grade: HerbGrade): HerbHiddenProperties {
  return {
    age: 0,
    qiSaturation: 0.2 + rng() * 0.5,  // 0.2–0.7
    purity: 0.5 + rng() * 0.4,         // 0.5–0.9
    mutation: rollMutation(rng, grade),
    medicinalPotency: 0.3 + rng() * 0.4, // 0.3–0.7
    karmicResidue: rng() * 0.15,        // 0–0.15
  };
}

function randomStartStage(rng: RNG, grade: HerbGrade): { stage: HerbGrowthStage; stageYears: number; totalAge: number } {
  const stage = pick(rng, SEED_START_STAGES);
  const range = STAGE_DURATIONS[grade][stage];
  const stageYears = randInt(rng, range[0], range[1]);
  // Calculate total age based on stages before this one (approximate)
  const stageOrder: HerbGrowthStage[] = ['seed', 'sprout', 'young', 'mature', 'flowering', 'fruiting', 'dormant', 'dead'];
  const stageIndex = stageOrder.indexOf(stage);
  let totalAge = stageYears;
  for (let i = 0; i < stageIndex; i++) {
    const prevRange = STAGE_DURATIONS[grade][stageOrder[i]];
    totalAge += randInt(rng, prevRange[0], prevRange[1]);
  }
  return { stage, stageYears, totalAge };
}

// ─── Spirit Herb catalog ────────────────────────────────────────────
// Note: templates omit the new C-depth fields (growthStage, stageYears, totalAge, properties)
// — they are filled in by genHerbs().
const HERB_TEMPLATES: Omit<SpiritHerb, 'id' | 'regionId' | 'perceived' | 'growthStage' | 'stageYears' | 'totalAge' | 'properties'>[] = [
  { name: 'Qi-Grass', nameCn: '气草', grade: 'mortal', environment: 'forest', rarity: 0.1, effect: 'gently restores Qi', pillUse: 'Qi-Recovery Pill', growthSeason: 'year-round' },
  { name: 'Spirit-Wheat', nameCn: '灵麦', grade: 'mortal', environment: 'forest', rarity: 0.15, effect: 'sustains mortal cultivators', pillUse: 'Mortal Foundation Powder', growthSeason: 'summer' },
  { name: 'Clear-Mind Moss', nameCn: '清心苔', grade: 'mortal', environment: 'wetland', rarity: 0.2, effect: 'sharpens focus for meditation', pillUse: 'Clarity Pill', growthSeason: 'year-round' },
  { name: 'Iron-Root Herb', nameCn: '铁根草', grade: 'mortal', environment: 'mountain', rarity: 0.2, effect: 'strengthens the body', pillUse: 'Body-Tempering Pill', growthSeason: 'autumn' },
  { name: 'Frost-Lotus', nameCn: '寒莲', grade: 'spirit', environment: 'wetland', rarity: 0.4, effect: 'cures qi deviation; cools the meridians', pillUse: 'Purification Pill (cures qi_deviation)', growthSeason: 'winter' },
  { name: 'Fire-Grass', nameCn: '火草', grade: 'spirit', environment: 'desert', rarity: 0.4, effect: 'boosts fire-element arts', pillUse: 'Flame Foundation Pill', growthSeason: 'summer' },
  { name: 'Spirit-Veins Fern', nameCn: '灵脉蕨', grade: 'spirit', environment: 'spirit_vein', rarity: 0.5, effect: 'aligns the cultivator with the local vein', pillUse: 'Vein-Attunement Pill', growthSeason: 'year-round' },
  { name: 'Heart-Calm Lily', nameCn: '心安百合', grade: 'spirit', environment: 'wetland', rarity: 0.5, effect: 'soothes heart-demons', pillUse: 'Heart-Calming Incense (cures heart_demon)', growthSeason: 'spring' },
  { name: 'Meridian-Thread Vine', nameCn: '经脉藤', grade: 'spirit', environment: 'forest', rarity: 0.55, effect: 'reopens sealed meridians', pillUse: 'Meridian-Clearing Powder (cures sealed_meridians)', growthSeason: 'autumn' },
  { name: 'Soul-Mend Orchid', nameCn: '补魂兰', grade: 'earth', environment: 'underground', rarity: 0.75, effect: 'mends soul fractures', pillUse: 'Soul-Mending Elixir (cures soul_fracture)', growthSeason: 'year-round' },
  { name: 'Nine-Turn Lotus', nameCn: '九转莲', grade: 'earth', environment: 'spirit_vein', rarity: 0.8, effect: 'catalyzes Core Formation breakthroughs', pillUse: 'Nine-Turn Core Pill', growthSeason: 'summer' },
  { name: 'Karmic-Cleansing Reed', nameCn: '洗业芦', grade: 'earth', environment: 'wetland', rarity: 0.8, effect: 'washes away karmic stains', pillUse: 'Karma-Cleansing Pill (cures karmic_stain)', growthSeason: 'autumn' },
  { name: 'Heaven-Defying Ginseng', nameCn: '逆天参', grade: 'heaven', environment: 'mountain', rarity: 0.93, effect: 'grants a massive lifespan + Qi surge', pillUse: 'Heaven-Defying Pill (restores lifespanBurned)', growthSeason: 'winter' },
  { name: 'Star-Fall Flower', nameCn: '陨星花', grade: 'heaven', environment: 'void', rarity: 0.95, effect: 'aligns the cultivator with starlight dao', pillUse: 'Star-Alignment Pill', growthSeason: 'year-round' },
  { name: 'Dao-Fruit', nameCn: '道果', grade: 'dao', environment: 'void', rarity: 0.99, effect: 'grants a fragment of true Dao understanding', pillUse: 'Dao Comprehension Pill (+understanding)', growthSeason: 'year-round' },
  { name: 'Origin-Lotus', nameCn: '本源莲', grade: 'dao', environment: 'spirit_vein', rarity: 0.98, effect: 'a sip of origin energy', pillUse: 'Origin Pill (premium-tier resource)', growthSeason: 'year-round' },
  // Additional real Er Gen herbs from wiki research
  { name: 'Whitebone Lily', nameCn: '白骨莲', grade: 'spirit', environment: 'underground', rarity: 0.6, effect: 'bone-corrupting pollen; used in bone cultivation', pillUse: 'Bone-Tempering Pill', growthSeason: 'year-round' },
  { name: 'Thousand-Year Ginseng', nameCn: '千年参', grade: 'earth', environment: 'mountain', rarity: 0.85, effect: 'massive Qi + lifespan boost; the older the ginseng, the stronger', pillUse: 'Longevity Pill (+lifespan)', growthSeason: 'autumn' },
  { name: 'Resurrection Lily Herb', nameCn: '复活莲草', grade: 'heaven', environment: 'wetland', rarity: 0.92, effect: 'can resurrect the recently deceased; parasitic when alive', pillUse: 'Resurrection Pill (one-time revive)', growthSeason: 'spring' },
  { name: 'Five-Element Flower', nameCn: '五行花', grade: 'earth', environment: 'spirit_vein', rarity: 0.82, effect: 'balances all five elements in the body; cures elemental imbalance', pillUse: 'Five-Element Balancing Pill', growthSeason: 'summer' },
  { name: 'Blood-Refinement Grass', nameCn: '炼血草', grade: 'spirit', environment: 'desert', rarity: 0.55, effect: 'refines blood; used in bloodline-refinement pills', pillUse: 'Blood-Refinement Pill (bloodline upgrade)', growthSeason: 'summer' },
  { name: 'Soul-Thread Moss', nameCn: '魂丝苔', grade: 'earth', environment: 'underground', rarity: 0.78, effect: 'strengthens the soul; used in soul-cultivation pills', pillUse: 'Soul-Strengthening Pill (+soul power)', growthSeason: 'year-round' },
  { name: 'Star-Dust Grass', nameCn: '星尘草', grade: 'heaven', environment: 'void', rarity: 0.94, effect: 'absorbs starlight; boosts cosmic dao understanding', pillUse: 'Star-Comprehension Pill', growthSeason: 'year-round' },
  { name: 'Dream-Lotus', nameCn: '梦莲', grade: 'spirit', environment: 'wetland', rarity: 0.5, effect: 'induces prophetic dreams; used in heart-demon confrontation', pillUse: 'Dream-Pilgrimage Pill (heart-demon test)', growthSeason: 'spring' },
  { name: 'Karma-Thread Reed', nameCn: '业丝芦', grade: 'earth', environment: 'wetland', rarity: 0.8, effect: 'strengthens karmic resistance; used in karma-cleansing', pillUse: 'Karmic-Resist Pill', growthSeason: 'autumn' },
  { name: 'Thunder-Root Herb', nameCn: '雷根草', grade: 'spirit', environment: 'mountain', rarity: 0.55, effect: 'absorbs ambient lightning; boosts lightning dao', pillUse: 'Thunder-Foundation Pill', growthSeason: 'summer' },
  { name: 'Spirit-Gathering Orchid', nameCn: '聚灵兰', grade: 'spirit', environment: 'spirit_vein', rarity: 0.5, effect: 'gathers ambient Qi to itself; boosts cultivation when consumed', pillUse: 'Spirit-Gathering Pill (+Qi regen)', growthSeason: 'year-round' },
  { name: 'Underworld Mushroom', nameCn: '冥菇', grade: 'earth', environment: 'underground', rarity: 0.75, effect: 'grown in underworld-Qi; boosts death dao + yin cultivation', pillUse: 'Underworld Pill (+death dao understanding)', growthSeason: 'year-round' },
  { name: 'Cloud-Silk Grass', nameCn: '云丝草', grade: 'mortal', environment: 'mountain', rarity: 0.3, effect: 'used in flying-sword hilt crafting; minor wind affinity', pillUse: 'Wind-Affinity Pill (minor)', growthSeason: 'spring' },
  { name: 'Incense-Root Vine', nameCn: '香根藤', grade: 'spirit', environment: 'forest', rarity: 0.45, effect: 'the raw material for Bai Xiaochun-style incense; faith-power catalyst', pillUse: 'Incense-Base Material (for all incense types)', growthSeason: 'year-round' },
  { name: 'Ancient-God Marrow', nameCn: '古神髓', grade: 'dao', environment: 'void', rarity: 0.99, effect: 'marrow from a dead Ancient God; grants body-cultivation breakthrough', pillUse: 'Ancient-God Body Pill (kindles a star)', growthSeason: 'year-round' },
  { name: 'Heaven-Devouring Flower', nameCn: '吞天花', grade: 'heaven', environment: 'void', rarity: 0.96, effect: 'devours heavenly law fragments; used in law-breaking pills', pillUse: 'Law-Breaking Pill (weakens local world-law temporarily)', growthSeason: 'year-round' },
];

export function genHerbs(rng: RNG, regionId: string, count = 8): SpiritHerb[] {
  return pickN(rng, HERB_TEMPLATES, Math.min(count, HERB_TEMPLATES.length)).map((t) => {
    const { stage, stageYears, totalAge } = randomStartStage(rng, t.grade);
    const props = initHerbProperties(rng, t.grade);
    props.age = totalAge;
    // Medicinal potency scales with age and purity (old + pure = potent)
    const ageBonus = Math.min(1, totalAge / (t.grade === 'mortal' ? 10 : t.grade === 'spirit' ? 30 : t.grade === 'earth' ? 100 : t.grade === 'heaven' ? 300 : 1000));
    props.medicinalPotency = Math.min(1, (props.purity * 0.6 + ageBonus * 0.4) * (props.mutation ? 1.2 : 1));
    // Qi saturation drifts with age
    props.qiSaturation = Math.min(1, props.qiSaturation + ageBonus * 0.2);
    return {
      ...t, id: uid('herb', rng), regionId, perceived: t.grade === 'mortal',
      growthStage: stage, stageYears, totalAge, properties: props,
    };
  });
}

// ─── The Full 5-Layer Bestiary Master Catalog ───────────────────────
// Each creature has: layer, behaviorScript, voxelScale, drops (modular loot table),
// aggroType, and special flags (destinedCompanion, gastroRealm, dualForm).
// Food-webs are per-layer. Layer 1: mortal ecology. Layer 4: void horrors. Layer 5: conceptual entities.

// The full 136-species bestiary is now in bestiary-catalog.ts (4 ecological categories
// across 5 cosmology layers + 6 novels). Import the deduped catalog for generation.
export { DEDUPED_CATALOG as BESTIARY_CATALOG, TOTAL_SPECIES_COUNT } from "./bestiary-catalog";

const SPECIES_ID_BY_NAME: Record<string, string> = Object.fromEntries(
  DEDUPED_CATALOG.map((s) => [s.name, s.name.toLowerCase().replace(/[^a-z0-9]+/g, "_").replace(/^_|_$/g, "")]),
);

export function genBeastSpecies(rng: RNG, count = 12): BeastSpecies[] {
  const chosen = pickN(rng, DEDUPED_CATALOG, Math.min(count, DEDUPED_CATALOG.length));
  return chosen.map((s) => ({
    ...s,
    id: SPECIES_ID_BY_NAME[s.name] ?? uid("spec", rng),
    populationStatus: s.tier === "ancient" ? "endangered" : s.tier === "demon" ? "declining" : pick(rng, ["thriving", "stable", "stable", "declining"] as const),
  }));
}

export function beastsByLayer(all: BeastSpecies[], layer: number): BeastSpecies[] {
  return all.filter((b) => b.layer === layer);
}
// ─── Helper: the cure-herb mapping (which herb cures which status) ──
export const STATUS_HERB_CURE: Record<string, { herb: string; pill: string }> = {
  soul_fracture: { herb: 'Soul-Mend Orchid', pill: 'Soul-Mending Elixir' },
  sealed_meridians: { herb: 'Meridian-Thread Vine', pill: 'Meridian-Clearing Powder' },
  qi_deviation: { herb: 'Frost-Lotus', pill: 'Purification Pill' },
  heart_demon: { herb: 'Heart-Calm Lily', pill: 'Heart-Calming Incense' },
  karmic_stain: { herb: 'Karmic-Cleansing Reed', pill: 'Karma-Cleansing Pill' },
};

// ─── Herb Growth Tick (C: Biological Growth Stages) ──────────────
// Called from engine.ts tick() — advances each herb's growth stage.
const STAGE_ORDER: HerbGrowthStage[] = ['seed', 'sprout', 'young', 'mature', 'flowering', 'fruiting', 'dormant', 'dead'];

export function tickHerbGrowth(rng: RNG, herbs: SpiritHerb[], years: number, qiDensity: number): void {
  // qiDensity 0..1 — higher Qi = faster growth
  const qiMult = 0.5 + qiDensity * 0.5; // 0.5x–1.0x

  for (const herb of herbs) {
    if (herb.growthStage === 'dead') {
      // Dead herbs have a small chance to self-seed (restart from seed)
      if (rng() < 0.05 * years) {
        herb.growthStage = 'seed';
        herb.stageYears = 0;
        herb.totalAge = 0;
        herb.properties = initHerbProperties(rng, herb.grade);
      }
      continue;
    }

    herb.stageYears += years * qiMult;
    herb.totalAge += years;
    herb.properties.age = herb.totalAge;

    // Advance stage if stageYears exceeds duration
    const maxStageYears = () => {
      const range = STAGE_DURATIONS[herb.grade][herb.growthStage];
      return range[0] + (range[1] - range[0]) * 0.6; // use 60th percentile as threshold
    };

    while (herb.stageYears >= maxStageYears() && herb.growthStage !== 'dead') {
      herb.stageYears -= maxStageYears();
      const currentIdx = STAGE_ORDER.indexOf(herb.growthStage);

      if (herb.growthStage === 'dormant') {
        // Dormant → sprout (new cycle), or dead if very old
        const maxLifespan = herb.grade === 'mortal' ? 20 : herb.grade === 'spirit' ? 60 : herb.grade === 'earth' ? 200 : herb.grade === 'heaven' ? 800 : 3000;
        if (herb.totalAge > maxLifespan && rng() < 0.3) {
          herb.growthStage = 'dead';
        } else {
          herb.growthStage = 'sprout';
        }
      } else if (herb.growthStage === 'fruiting') {
        // Fruiting → dormant (natural cycle)
        herb.growthStage = 'dormant';
      } else {
        // Normal progression to next stage
        const nextIdx = Math.min(currentIdx + 1, STAGE_ORDER.length - 1);
        herb.growthStage = STAGE_ORDER[nextIdx];
      }

      if (herb.growthStage === 'dead') break;
    }

    // Update properties based on growth
    herb.properties.qiSaturation = Math.min(1, herb.properties.qiSaturation + 0.01 * years * qiMult);
    // Purity slowly degrades if in a corrupt environment (simulated by karmic residue)
    if (herb.properties.karmicResidue > 0.3) {
      herb.properties.purity = Math.max(0.1, herb.properties.purity - 0.005 * years);
    }
    // Medicinal potency depends on stage: fruiting > flowering > mature > young > sprout
    const stagePotency: Record<HerbGrowthStage, number> = {
      seed: 0.05, sprout: 0.2, young: 0.5, mature: 0.7, flowering: 0.85, fruiting: 1.0, dormant: 0.3, dead: 0,
    };
    const basePotency = stagePotency[herb.growthStage] ?? 0.5;
    const ageBonus = Math.min(1, herb.totalAge / (herb.grade === 'mortal' ? 10 : herb.grade === 'spirit' ? 30 : herb.grade === 'earth' ? 100 : herb.grade === 'heaven' ? 300 : 1000));
    herb.properties.medicinalPotency = Math.min(1, basePotency * (herb.properties.purity * 0.6 + ageBonus * 0.4) * (herb.properties.mutation ? 1.2 : 1));
  }
}

// ─── Realm-Scaled Herb Perception (C) ────────────────────────────
// Mortal sees "a weed". Qi Condensation sees the name. Foundation sees grade + effect.
// Core Formation+ sees hidden properties. Nascent Soul+ sees karmic residue + mutations.

const MORTAL_DESCRIPTIONS = [
  'a common weed', 'some wild grass', 'an unremarkable plant', 'a patch of greenery',
  'a tuft of vegetation', 'a wild herb', 'overgrown weeds', 'a small plant',
];

const HERB_GRADE_PERCEPTION_REALM: Record<HerbGrade, number> = {
  mortal: 0,     // anyone can see
  spirit: 1,     // Qi Condensation+
  earth: 2,      // Foundation+
  heaven: 3,     // Core Formation+
  dao: 4,        // Nascent Soul+
};

export function perceiveHerb(herb: SpiritHerb, playerRealmOrder: number): HerbPerception {
  const gradeThreshold = HERB_GRADE_PERCEPTION_REALM[herb.grade];

  // Mortal: sees a weed (unless mortal-grade herb)
  if (playerRealmOrder === 0) {
    if (herb.grade === 'mortal' && herb.growthStage !== 'seed' && herb.growthStage !== 'dead') {
      return {
        displayName: herb.name,
        gradeRevealed: false,
        effectRevealed: false,
        stageRevealed: false,
        properties: {},
      };
    }
    return {
      displayName: MORTAL_DESCRIPTIONS[Math.abs(herb.id.charCodeAt(herb.id.length - 1)) % MORTAL_DESCRIPTIONS.length],
      gradeRevealed: false,
      effectRevealed: false,
      stageRevealed: false,
      properties: {},
    };
  }

  // Qi Condensation: sees name + grade for mortal/spirit herbs
  if (playerRealmOrder === 1) {
    const canSee = playerRealmOrder >= gradeThreshold;
    return {
      displayName: canSee ? herb.name : 'a faintly glowing plant',
      gradeRevealed: canSee,
      effectRevealed: canSee,
      stageRevealed: false,
      properties: {},
    };
  }

  // Foundation+: sees name, grade, effect, and growth stage for accessible herbs
  if (playerRealmOrder === 2) {
    const canSee = playerRealmOrder >= gradeThreshold;
    return {
      displayName: canSee ? herb.name : 'a spirit-veined plant',
      gradeRevealed: canSee,
      effectRevealed: canSee,
      stageRevealed: canSee,
      properties: canSee ? { age: herb.properties.age, qiSaturation: Math.round(herb.properties.qiSaturation * 100) / 100 } : {},
    };
  }

  // Core Formation+: sees all properties except karmic residue
  if (playerRealmOrder === 3) {
    const canSee = playerRealmOrder >= gradeThreshold;
    return {
      displayName: canSee ? herb.name : 'a radiant spirit herb',
      gradeRevealed: canSee,
      effectRevealed: canSee,
      stageRevealed: canSee,
      properties: canSee ? {
        age: herb.properties.age,
        qiSaturation: Math.round(herb.properties.qiSaturation * 100) / 100,
        purity: Math.round(herb.properties.purity * 100) / 100,
        mutation: herb.properties.mutation,
        medicinalPotency: Math.round(herb.properties.medicinalPotency * 100) / 100,
      } : {},
    };
  }

  // Nascent Soul+: sees everything including karmic residue
  const canSee = playerRealmOrder >= gradeThreshold;
  return {
    displayName: canSee ? herb.name : 'an unfathomable celestial herb',
    gradeRevealed: canSee,
    effectRevealed: canSee,
    stageRevealed: canSee,
    properties: canSee ? {
      age: herb.properties.age,
      qiSaturation: Math.round(herb.properties.qiSaturation * 100) / 100,
      purity: Math.round(herb.properties.purity * 100) / 100,
      mutation: herb.properties.mutation,
      medicinalPotency: Math.round(herb.properties.medicinalPotency * 100) / 100,
      karmicResidue: Math.round(herb.properties.karmicResidue * 100) / 100,
    } : {},
  };
}

// ─── Harvest Yields (C: Realm-scaled harvesting) ──────────────────
// Higher realm = better yield, can only harvest fruiting/flowering/mature herbs.
// Returns the number of herb units yielded (0 if unharvestable).

const HARVESTABLE_STAGES: HerbGrowthStage[] = ['mature', 'flowering', 'fruiting'];

const STAGE_YIELD_MULT: Record<HerbGrowthStage, number> = {
  seed: 0, sprout: 0, young: 0, mature: 0.5, flowering: 0.8, fruiting: 1.0, dormant: 0.1, dead: 0,
};

export function harvestHerb(herb: SpiritHerb, playerRealmOrder: number): { yield: number; potency: number; text: string } {
  if (!HARVESTABLE_STAGES.includes(herb.growthStage) && herb.growthStage !== 'dormant') {
    return { yield: 0, potency: 0, text: `${herb.name} is not yet mature enough to harvest.` };
  }

  // Base yield: 1-3 for mortal, more for higher grades
  const gradeBase = herb.grade === 'mortal' ? 1 : herb.grade === 'spirit' ? 1 : herb.grade === 'earth' ? 2 : herb.grade === 'heaven' ? 3 : 5;
  const stageMult = STAGE_YIELD_MULT[herb.growthStage];
  const realmBonus = 1 + playerRealmOrder * 0.15; // +15% per realm tier
  const potencyMult = 0.5 + herb.properties.medicinalPotency * 0.5;

  const yield_ = Math.max(1, Math.round(gradeBase * stageMult * realmBonus * potencyMult));

  // Harvesting resets the herb to dormant (or seed if young)
  if (herb.growthStage === 'dormant') {
    herb.growthStage = 'seed';
    herb.stageYears = 0;
  } else {
    herb.growthStage = 'dormant';
    herb.stageYears = 0;
  }

  const potencyDesc = herb.properties.medicinalPotency > 0.8 ? 'exceptionally potent' : herb.properties.medicinalPotency > 0.5 ? 'decent quality' : 'mediocre quality';
  const ageDesc = herb.properties.age > 100 ? `${herb.properties.age}-year-old` : herb.properties.age > 10 ? `${herb.properties.age}-year` : '';
  const mutDesc = herb.properties.mutation ? `, ${herb.properties.mutation}` : '';

  return {
    yield: yield_,
    potency: herb.properties.medicinalPotency,
    text: `Harvested ${yield_}x ${herb.name} (${potencyDesc}${ageDesc ? `, ${ageDesc}` : ''}${mutDesc}). The herb enters dormancy.`,
  };
}
