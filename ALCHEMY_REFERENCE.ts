// src/lib/sim/alchemy.ts
// Property-based alchemy system. Each ingredient has hidden properties.
// Pills are combinations of properties, not fixed recipes.
// A master alchemist can invent pills nobody has ever made before.

import type { RealmId, XianxiaStatusId } from './types';
import type { RNG } from './generators';
import { pick, pickN, uid } from './generators';

// ─── Alchemical Properties ──────────────────────────────────────────
// Every ingredient possesses a set of these. Pills are resolved from property combos.
export type AlchemyProperty =
  | 'cold' | 'heat' | 'yin' | 'yang' | 'purity' | 'corruption'
  | 'life' | 'death' | 'wood' | 'fire' | 'water' | 'metal' | 'earth'
  | 'lightning' | 'wind' | 'ice' | 'light' | 'dark' | 'time' | 'space'
  | 'blood' | 'spirit' | 'vitality' | 'explosion' | 'calm' | 'fury'
  | 'soul' | 'body' | 'meridian' | 'karma';

export interface IngredientProperties {
  name: string;
  properties: AlchemyProperty[];
  potency: number;     // 0..1 — how strong the ingredient is
  stability: number;   // 0..1 — how stable (high = less likely to explode)
  grade: 'mortal' | 'spirit' | 'earth' | 'heaven' | 'dao';
}

// ─── Herb → Property mapping (the hidden chemistry) ─────────────────
export const HERB_PROPERTIES: Record<string, IngredientProperties> = {
  'Qi-Grass': { name: 'Qi-Grass', properties: ['spirit', 'life', 'vitality'], potency: 0.2, stability: 0.9, grade: 'mortal' },
  'Spirit-Wheat': { name: 'Spirit-Wheat', properties: ['life', 'vitality', 'earth'], potency: 0.15, stability: 0.95, grade: 'mortal' },
  'Clear-Mind Moss': { name: 'Clear-Mind Moss', properties: ['calm', 'spirit', 'purity'], potency: 0.25, stability: 0.85, grade: 'mortal' },
  'Iron-Root Herb': { name: 'Iron-Root Herb', properties: ['body', 'metal', 'earth'], potency: 0.3, stability: 0.8, grade: 'mortal' },
  'Frost-Lotus': { name: 'Frost-Lotus', properties: ['cold', 'purity', 'water', 'yin', 'ice'], potency: 0.6, stability: 0.7, grade: 'spirit' },
  'Fire-Grass': { name: 'Fire-Grass', properties: ['heat', 'fire', 'yang', 'explosion'], potency: 0.55, stability: 0.5, grade: 'spirit' },
  'Spirit-Veins Fern': { name: 'Spirit-Veins Fern', properties: ['spirit', 'earth', 'meridian'], potency: 0.5, stability: 0.75, grade: 'spirit' },
  'Heart-Calm Lily': { name: 'Heart-Calm Lily', properties: ['calm', 'yin', 'soul', 'purity'], potency: 0.6, stability: 0.8, grade: 'spirit' },
  'Meridian-Thread Vine': { name: 'Meridian-Thread Vine', properties: ['meridian', 'wood', 'body', 'life'], potency: 0.65, stability: 0.7, grade: 'spirit' },
  'Soul-Mend Orchid': { name: 'Soul-Mend Orchid', properties: ['soul', 'purity', 'spirit', 'calm'], potency: 0.8, stability: 0.6, grade: 'earth' },
  'Nine-Turn Lotus': { name: 'Nine-Turn Lotus', properties: ['spirit', 'purity', 'life', 'yang', 'soul'], potency: 0.85, stability: 0.5, grade: 'earth' },
  'Karmic-Cleansing Reed': { name: 'Karmic-Cleansing Reed', properties: ['karma', 'purity', 'water', 'calm'], potency: 0.8, stability: 0.65, grade: 'earth' },
  'Heaven-Defying Ginseng': { name: 'Heaven-Defying Ginseng', properties: ['life', 'vitality', 'spirit', 'yang', 'soul', 'time'], potency: 0.95, stability: 0.4, grade: 'heaven' },
  'Star-Fall Flower': { name: 'Star-Fall Flower', properties: ['light', 'space', 'spirit', 'purity'], potency: 0.9, stability: 0.5, grade: 'heaven' },
  'Dao-Fruit': { name: 'Dao-Fruit', properties: ['spirit', 'soul', 'time', 'space', 'karma', 'purity'], potency: 1.0, stability: 0.3, grade: 'dao' },
  'Origin-Lotus': { name: 'Origin-Lotus', properties: ['life', 'death', 'yin', 'yang', 'spirit', 'soul'], potency: 1.0, stability: 0.35, grade: 'dao' },
  // Beast-derived ingredients
  'Rabbit Blood Essence': { name: 'Rabbit Blood Essence', properties: ['blood', 'wood', 'life', 'spirit'], potency: 0.2, stability: 0.85, grade: 'mortal' },
  'Fox Core': { name: 'Fox Core', properties: ['spirit', 'soul', 'dark'], potency: 0.4, stability: 0.6, grade: 'spirit' },
  'Wolf Core': { name: 'Wolf Core', properties: ['blood', 'fury', 'metal'], potency: 0.45, stability: 0.55, grade: 'spirit' },
  'Python Gallbladder': { name: 'Python Gallbladder', properties: ['fire', 'heat', 'purity', 'body'], potency: 0.6, stability: 0.65, grade: 'spirit' },
  'Thunder Horn': { name: 'Thunder Horn', properties: ['lightning', 'yang', 'explosion', 'metal'], potency: 0.85, stability: 0.4, grade: 'earth' },
  'Dragon Blood Essence': { name: 'Dragon Blood Essence', properties: ['blood', 'fire', 'yang', 'life', 'soul', 'vitality'], potency: 0.95, stability: 0.3, grade: 'heaven' },
  // New herb properties
  'Whitebone Lily': { name: 'Whitebone Lily', properties: ['death', 'bone', 'yin', 'corruption'], potency: 0.6, stability: 0.55, grade: 'spirit' },
  'Thousand-Year Ginseng': { name: 'Thousand-Year Ginseng', properties: ['life', 'vitality', 'spirit', 'yang', 'time'], potency: 0.9, stability: 0.4, grade: 'earth' },
  'Resurrection Lily Herb': { name: 'Resurrection Lily Herb', properties: ['life', 'death', 'soul', 'time', 'karma'], potency: 0.95, stability: 0.2, grade: 'heaven' },
  'Five-Element Flower': { name: 'Five-Element Flower', properties: ['fire', 'water', 'wood', 'metal', 'earth'], potency: 0.85, stability: 0.6, grade: 'earth' },
  'Blood-Refinement Grass': { name: 'Blood-Refinement Grass', properties: ['blood', 'fire', 'life'], potency: 0.55, stability: 0.6, grade: 'spirit' },
  'Soul-Thread Moss': { name: 'Soul-Thread Moss', properties: ['soul', 'spirit', 'purity'], potency: 0.7, stability: 0.75, grade: 'earth' },
  'Star-Dust Grass': { name: 'Star-Dust Grass', properties: ['light', 'space', 'spirit', 'purity'], potency: 0.9, stability: 0.5, grade: 'heaven' },
  'Dream-Lotus': { name: 'Dream-Lotus', properties: ['soul', 'calm', 'yin', 'spirit'], potency: 0.5, stability: 0.8, grade: 'spirit' },
  'Karma-Thread Reed': { name: 'Karma-Thread Reed', properties: ['karma', 'purity', 'water'], potency: 0.75, stability: 0.7, grade: 'earth' },
  'Thunder-Root Herb': { name: 'Thunder-Root Herb', properties: ['lightning', 'yang', 'earth'], potency: 0.55, stability: 0.6, grade: 'spirit' },
  'Spirit-Gathering Orchid': { name: 'Spirit-Gathering Orchid', properties: ['spirit', 'purity', 'life'], potency: 0.5, stability: 0.85, grade: 'spirit' },
  'Underworld Mushroom': { name: 'Underworld Mushroom', properties: ['death', 'yin', 'soul', 'earth'], potency: 0.75, stability: 0.55, grade: 'earth' },
  'Cloud-Silk Grass': { name: 'Cloud-Silk Grass', properties: ['wind', 'wood', 'spirit'], potency: 0.25, stability: 0.9, grade: 'mortal' },
  'Incense-Root Vine': { name: 'Incense-Root Vine', properties: ['spirit', 'calm', 'purity', 'life'], potency: 0.4, stability: 0.85, grade: 'spirit' },
  'Ancient-God Marrow': { name: 'Ancient-God Marrow', properties: ['body', 'soul', 'fire', 'earth', 'yang', 'life', 'vitality'], potency: 1.0, stability: 0.2, grade: 'dao' },
  'Heaven-Devouring Flower': { name: 'Heaven-Devouring Flower', properties: ['dark', 'space', 'death', 'karma'], potency: 0.95, stability: 0.25, grade: 'heaven' },
  // Additional beast-derived ingredients
  'Thunder Toad Core': { name: 'Thunder Toad Core', properties: ['lightning', 'water', 'spirit'], potency: 0.7, stability: 0.5, grade: 'spirit' },
  'Qilin Scale': { name: 'Qilin Scale', properties: ['fire', 'earth', 'metal', 'yang', 'body'], potency: 0.9, stability: 0.4, grade: 'heaven' },
  'Phoenix Feather': { name: 'Phoenix Feather', properties: ['fire', 'light', 'life', 'yang', 'soul'], potency: 0.95, stability: 0.3, grade: 'heaven' },
  'Ghost King Core': { name: 'Ghost King Core', properties: ['death', 'yin', 'soul', 'dark'], potency: 0.8, stability: 0.35, grade: 'earth' },
  'Dream Core': { name: 'Dream Core', properties: ['soul', 'dark', 'calm'], potency: 0.75, stability: 0.4, grade: 'earth' },
  'Heaven-Devouring Core': { name: 'Heaven-Devouring Core', properties: ['dark', 'space', 'karma', 'death'], potency: 1.0, stability: 0.15, grade: 'dao' },
};

// ─── Pill Recipe Resolution (property → effect) ─────────────────────
// The engine resolves a combination of ingredients into a pill by analyzing
// the aggregate property profile. This is NOT a fixed recipe — it's chemistry.
export interface PillResult {
  name: string;
  grade: 'mortal' | 'spirit' | 'earth' | 'heaven' | 'dao' | 'failed';
  effects: string[];
  curesStatus?: XianxiaStatusId;
  qiBoost?: number;
  understandingBoost?: number;
  lifespanRestore?: number;
  soulPowerBoost?: number;
  karmicDebtReduction?: number;
  sideEffects: string[];
  success: boolean;
  explosion: boolean;
  discoveryText?: string; // set when a new pill is discovered
  properties: AlchemyProperty[]; // the aggregate properties
}

// Known pill patterns — these are the "discovered" combinations.
// A master alchemist can discover NEW patterns by experimenting.
export interface PillPattern {
  name: string;
  requiredProperties: AlchemyProperty[]; // ALL must be present
  forbiddenProperties?: AlchemyProperty[]; // NONE may be present
  minPotency: number;
  effects: string[];
  curesStatus?: XianxiaStatusId;
  qiBoost?: number;
  understandingBoost?: number;
  lifespanRestore?: number;
  soulPowerBoost?: number;
  karmicDebtReduction?: number;
  sideEffects: string[];
}

export const KNOWN_PILL_PATTERNS: PillPattern[] = [
  { name: 'Qi-Recovery Pill', requiredProperties: ['spirit', 'life'], minPotency: 0.15, effects: ['restores Qi'], qiBoost: 50, sideEffects: [] },
  { name: 'Purification Pill', requiredProperties: ['cold', 'purity', 'water'], forbiddenProperties: ['heat', 'fire'], minPotency: 0.4, effects: ['cures qi deviation', 'cools meridians'], curesStatus: 'qi_deviation', sideEffects: ['temporary cold-slow'] },
  { name: 'Heart-Calming Incense', requiredProperties: ['calm', 'soul', 'purity'], minPotency: 0.4, effects: ['soothes heart-demons'], curesStatus: 'heart_demon', sideEffects: ['drowsiness'] },
  { name: 'Meridian-Clearing Powder', requiredProperties: ['meridian', 'wood', 'body'], minPotency: 0.4, effects: ['reopens sealed meridians'], curesStatus: 'sealed_meridians', sideEffects: ['meridian pain'] },
  { name: 'Soul-Mending Elixir', requiredProperties: ['soul', 'purity', 'spirit', 'calm'], minPotency: 0.6, effects: ['mends soul fractures'], curesStatus: 'soul_fracture', sideEffects: ['spiritual exhaustion'] },
  { name: 'Karma-Cleansing Pill', requiredProperties: ['karma', 'purity', 'water'], minPotency: 0.6, effects: ['washes away karmic stains'], curesStatus: 'karmic_stain', sideEffects: ['karmic backlash (minor)'] },
  { name: 'Body-Tempering Pill', requiredProperties: ['body', 'metal', 'earth'], minPotency: 0.3, effects: ['strengthens the physical body', '+5 max Qi'], qiBoost: 5, sideEffects: ['muscle soreness'] },
  { name: 'Foundation Pill', requiredProperties: ['spirit', 'life', 'earth', 'purity'], minPotency: 0.4, effects: ['stabilizes foundation', '+100 max Qi'], qiBoost: 100, sideEffects: [] },
  { name: 'Nine-Turn Core Pill', requiredProperties: ['spirit', 'purity', 'life', 'yang', 'soul'], minPotency: 0.7, effects: ['catalyzes Core Formation breakthrough', '+500 max Qi'], qiBoost: 500, sideEffects: ['internal heat'] },
  { name: 'Heaven-Defying Pill', requiredProperties: ['life', 'vitality', 'spirit', 'yang', 'soul', 'time'], minPotency: 0.9, effects: ['massive lifespan restoration', '+1000 max Qi'], qiBoost: 1000, lifespanRestore: 100, sideEffects: ['tribulation attraction'] },
  { name: 'Dao Comprehension Pill', requiredProperties: ['spirit', 'soul', 'time', 'space', 'karma', 'purity'], minPotency: 0.95, effects: ['grants a fragment of true Dao understanding'], understandingBoost: 10, soulPowerBoost: 50000, sideEffects: ['heart-demon risk'] },
  { name: 'Lightning Essence Pill', requiredProperties: ['lightning', 'yang', 'explosion'], minPotency: 0.6, effects: ['converts to Lightning Essence blocks', 'boosts lightning dao'], understandingBoost: 3, sideEffects: ['Qi deviation risk'] },
  { name: 'Fury Pill', requiredProperties: ['fury', 'blood', 'fire'], minPotency: 0.4, effects: ['+200% attack for 1 battle', 'removes fear'], sideEffects: ['exhaustion after', 'Qi deviation risk'] },
  { name: 'Blood-Refinement Pill', requiredProperties: ['blood', 'life', 'soul'], minPotency: 0.5, effects: ['refines bloodline', '+body constitution potential'], sideEffects: ['painful transformation'] },
];

// ─── Brew a pill from ingredients ───────────────────────────────────
export function brewPill(rng: RNG, ingredientNames: string[], alchemySkill: number): PillResult {
  // Gather ingredients
  const ingredients = ingredientNames.map((n) => HERB_PROPERTIES[n]).filter(Boolean);
  if (ingredients.length < 2) {
    return { name: 'Failed Concoction', grade: 'failed', effects: [], sideEffects: ['wasted ingredients'], success: false, explosion: false, properties: [] };
  }

  // Aggregate properties
  const propertySet = new Set<AlchemyProperty>();
  for (const ing of ingredients) {
    for (const p of ing.properties) propertySet.add(p);
  }
  const properties = [...propertySet];

  // Aggregate potency + stability
  const avgPotency = ingredients.reduce((s, i) => s + i.potency, 0) / ingredients.length;
  const avgStability = ingredients.reduce((s, i) => s + i.stability, 0) / ingredients.length;

  // Explosion check: low stability + high potency + low alchemy skill = boom
  const explosionChance = Math.max(0, (1 - avgStability) * 0.5 + (avgPotency - 0.7) * 0.3 - alchemySkill * 0.1);
  if (rng() < explosionChance) {
    return {
      name: 'Alchemical Explosion!',
      grade: 'failed',
      effects: [],
      sideEffects: ['Qi deviation', 'furnace damage', 'ingredient loss'],
      success: false,
      explosion: true,
      properties,
    };
  }

  // Match against known patterns
  const matched = KNOWN_PILL_PATTERNS.filter((p) => {
    if (p.forbiddenProperties && p.forbiddenProperties.some((fp) => properties.includes(fp))) return false;
    if (p.requiredProperties.some((rp) => !properties.includes(rp))) return false;
    if (avgPotency < p.minPotency) return false;
    return true;
  });

  if (matched.length > 0) {
    // Use the highest-tier match
    const best = matched.sort((a, b) => (b.minPotency - a.minPotency))[0];
    const grade = best.minPotency >= 0.9 ? 'dao' : best.minPotency >= 0.8 ? 'heaven' : best.minPotency >= 0.6 ? 'earth' : best.minPotency >= 0.4 ? 'spirit' : 'mortal';
    return {
      name: best.name,
      grade: grade as PillResult['grade'],
      effects: best.effects,
      curesStatus: best.curesStatus,
      qiBoost: best.qiBoost,
      understandingBoost: best.understandingBoost,
      lifespanRestore: best.lifespanRestore,
      soulPowerBoost: best.soulPowerBoost,
      karmicDebtReduction: best.karmicDebtReduction,
      sideEffects: best.sideEffects,
      success: true,
      explosion: false,
      properties,
    };
  }

  // No known pattern matched — but a master alchemist might discover something new
  if (alchemySkill > 0.5 && rng() < alchemySkill * 0.3) {
    // Procedural discovery: invent a new pill from the property combination
    const primaryProperty = properties[0];
    const newName = `${primaryProperty.charAt(0).toUpperCase() + primaryProperty.slice(1)}-Essence Pill`;
    const discoveryText = `You discovered a new pill: ${newName}! The properties ${properties.join(', ')} combined in a way no known recipe describes. A Grandmaster Alchemist's intuition!`;
    return {
      name: newName,
      grade: avgPotency >= 0.8 ? 'heaven' : avgPotency >= 0.5 ? 'earth' : 'spirit',
      effects: [`+${Math.round(avgPotency * 100)} Qi`, `+${Math.round(avgPotency * 20)} understanding (weak)`],
      qiBoost: Math.round(avgPotency * 100),
      understandingBoost: Math.round(avgPotency * 20),
      sideEffects: ['unknown long-term effects'],
      success: true,
      explosion: false,
      discoveryText,
      properties,
    };
  }

  // Failed brew — properties didn't cohere
  return {
    name: 'Murky Sludge',
    grade: 'failed',
    effects: [],
    sideEffects: ['wasted ingredients', 'mild nausea'],
    success: false,
    explosion: false,
    properties,
  };
}

// ─── Available ingredients for the player (from inventory herbs + beast cores) ──
export function availableIngredients(playerInventory: Record<string, number>, herbs: { name: string }[]): string[] {
  const available: string[] = [];
  // Add herbs the player has
  for (const herb of herbs) {
    if (HERB_PROPERTIES[herb.name] && (playerInventory.spirit_herb ?? 0) > 0) {
      available.push(herb.name);
    }
  }
  // Add beast-core-derived ingredients if player has beast cores
  if ((playerInventory.beast_core ?? 0) > 0) {
    available.push('Rabbit Blood Essence', 'Fox Core', 'Wolf Core', 'Python Gallbladder');
  }
  // Always allow Qi-Grass as a basic ingredient (stands in for common herbs)
  available.push('Qi-Grass', 'Clear-Mind Moss', 'Iron-Root Herb');
  // Remove duplicates
  return [...new Set(available)];
}
