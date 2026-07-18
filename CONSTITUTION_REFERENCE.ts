// src/lib/sim/constitution.ts
// Constitution Evolution System. Constitutions are NOT permanent character classes.
// They can be awakened, refined, repaired, transformed, corrupted, merged, replaced, evolved.
// 5 layers: Innate Constitution → Bloodline → Body Tempering State → Soul Traits → Dao Physique.

import type { RealmId, DaoId } from './types';
import type { RNG } from './generators';
import { pick, uid } from './generators';

// ─── Constitution Layers ────────────────────────────────────────────
export type ConstitutionLayer = 'innate' | 'bloodline' | 'tempering' | 'soul' | 'dao_physique';

export interface ConstitutionState {
  innate: string;           // what you're born with
  bloodline?: string;       // inherited or acquired lineage
  tempering?: string;       // how the body has been refined
  soulTrait?: string;       // changes to the spirit/divine soul
  daoPhysique?: string;     // evolved state after profound Dao understanding
  history: { year: number; layer: ConstitutionLayer; from: string; to: string; cause: string }[];
}

// ─── Constitution Catalog ───────────────────────────────────────────
export const INNATE_CONSTITUTIONS = [
  { name: 'Mortal Body', desc: 'An ordinary mortal physique. No spiritual affinity.', bonuses: {}, drawbacks: {} },
  { name: 'Wood-Spirit Body', desc: 'Naturally attuned to wood-element Qi. +20% wood dao cultivation speed.', bonuses: { woodDao: 0.2 }, drawbacks: {} },
  { name: 'Fire-Spirit Body', desc: 'Naturally attuned to fire-element Qi. +20% fire dao cultivation speed.', bonuses: { fireDao: 0.2 }, drawbacks: { waterDao: -0.1 } },
  { name: 'Ice-Veined Body', desc: 'Ice-element meridians. +20% ice/water dao speed.', bonuses: { iceDao: 0.2, waterDao: 0.1 }, drawbacks: { fireDao: -0.15 } },
  { name: 'Ancient-God Trace', desc: 'A trace of Ancient God bloodline. +10% body cultivation. Rare.', bonuses: { bodyDao: 0.1, soulPower: 0.1 }, drawbacks: { instability: 0.05 } },
  { name: 'Spiritual Body', desc: 'Naturally pure meridians. +15% all cultivation speed.', bonuses: { allDao: 0.15 }, drawbacks: {} },
  { name: 'Heaven-Defying Body', desc: 'An extremely rare constitution that defies heaven. +30% all cultivation, but +50% tribulation intensity.', bonuses: { allDao: 0.3 }, drawbacks: { tribulation: 0.5 } },
];

export const BLOODLINES = [
  { name: 'Dragon Bloodline (Trace)', desc: 'A trace of dragon blood. +fire/water affinity, +body strength.', source: 'Azure Dragon-Whelp bloodline essence' },
  { name: 'Fox Bloodline (Ancient)', desc: 'Ancient fox lineage. +illusion arts, +cunning.', source: 'Nine-Tail Fox Matriarch bloodline essence' },
  { name: 'Kunpeng Bloodline (Fragment)', desc: 'A fragment of the Kunpeng bloodline. Grants aquatic↔spatial form-switching.', source: 'Prime Kunpeng bloodline source' },
  { name: 'Thunder Beast Bloodline', desc: 'Lightning-aspected bloodline. +lightning dao, absorbs lightning damage.', source: 'Silver-Horned Thunder Beast bloodline' },
  { name: 'Berserker Bloodline', desc: 'From Su Ming\'s world. Activates bloodline surge at low HP.', source: 'Berserker Beast bloodline fragment' },
];

export const TEMPERING_STATES = [
  { name: 'Unrefined', desc: 'Body has not been tempered.', bonuses: {} },
  { name: 'Iron-Bone Tempering', desc: 'Bones refined with iron-element Qi. +20% physical defense.', bonuses: { physDef: 0.2 } },
  { name: 'Jade-Marrow Tempering', desc: 'Marrow refined with spirit-grade herbs. +10% Qi regeneration.', bonuses: { qiRegen: 0.1 } },
  { name: 'Heaven-Tempered Body', desc: 'Refined by surviving tribulation lightning. +30% tribulation survival.', bonuses: { tribSurvival: 0.3 } },
  { name: 'Ancient-God Body (Partial)', desc: 'Body partially reshaped into Ancient God form. +50% body cultivation, +soul power.', bonuses: { bodyDao: 0.5, soulPower: 0.3 } },
];

export const SOUL_TRAITS = [
  { name: 'None', desc: 'No soul traits.', bonuses: {} },
  { name: 'Divine Sense Awakening', desc: 'Divine sense has awakened. +20% divine sense range.', bonuses: { divineSenseRange: 0.2 } },
  { name: 'Soul Fragment', desc: 'Soul has split into a fragment. Enables avatar projection.', bonuses: { avatar: true } },
  { name: 'Heart-Demon Scarred', desc: 'Survived a heart-demon. -10% heart-demon resistance, but +15% dao understanding from pain.', bonuses: { daoUnderstanding: 0.15 }, drawbacks: { heartDemonResist: -0.1 } },
  { name: 'Soul-Devoured (Enhanced)', desc: 'Soul enhanced via soul-eating arts. +100% soul power, but karmic debt accumulates faster.', bonuses: { soulPower: 1.0 }, drawbacks: { karmicAccel: 0.5 } },
];

export const DAO_PHYSIQUES = [
  { name: 'None', desc: 'No Dao Physique achieved.', bonuses: {} },
  { name: 'Sword-Heart Physique', desc: 'The body IS a sword. +50% sword dao, sword-intent passive.', bonuses: { swordDao: 0.5 }, requirement: 'Sword dao understanding ≥ 80%' },
  { name: 'One-With-The-World', desc: 'Dao physique of unity. +30% all dao, natural Qi gathering.', bonuses: { allDao: 0.3, qiRegen: 0.5 }, requirement: 'Any dao understanding ≥ 90%' },
  { name: 'Transcendent Physique', desc: 'Beyond the world\'s laws. Immune to suppression.', bonuses: { suppressionImmune: true }, requirement: 'Transcendence realm' },
];

// ─── Constitution evolution ─────────────────────────────────────────
export function genStartingConstitution(rng: RNG): ConstitutionState {
  const innate = pick(rng, INNATE_CONSTITUTIONS);
  return {
    innate: innate.name,
    history: [],
  };
}

export function evolveConstitution(
  state: ConstitutionState,
  layer: ConstitutionLayer,
  newValue: string,
  cause: string,
  year: number,
): ConstitutionState {
  const old = (state as any)[layer] ?? 'None';
  (state as any)[layer] = newValue;
  state.history.push({ year, layer, from: old, to: newValue, cause });
  return state;
}

// Check if a constitution can evolve (opportunities, inheritances, tribulations)
export function canEvolveConstitution(state: ConstitutionState, trigger: string): { layer: ConstitutionLayer; value: string; cause: string } | null {
  // Inheritance → may awaken bloodline
  if (trigger === 'inheritance' && !state.bloodline) {
    return { layer: 'bloodline', value: 'Dragon Bloodline (Trace)', cause: 'absorbed bloodline essence from an ancient inheritance' };
  }
  // Tribulation survival → tempering
  if (trigger === 'tribulation_survival' && (!state.tempering || state.tempering === 'Unrefined')) {
    return { layer: 'tempering', value: 'Heaven-Tempered Body', cause: 'survived tribulation lightning; body refined by heaven' };
  }
  // Soul-eating → soul trait
  if (trigger === 'soul_eating' && (!state.soulTrait || state.soulTrait === 'None')) {
    return { layer: 'soul', value: 'Soul-Devoured (Enhanced)', cause: 'consumed souls via soul-eating arts' };
  }
  // High dao understanding → dao physique
  if (trigger === 'dao_enlightenment' && (!state.daoPhysique || state.daoPhysique === 'None')) {
    return { layer: 'dao_physique', value: 'One-With-The-World', cause: 'profound Dao understanding transformed the physique' };
  }
  // Beast bloodline absorption
  if (trigger === 'beast_bloodline' && !state.bloodline) {
    return { layer: 'bloodline', value: 'Thunder Beast Bloodline', cause: 'absorbed a spirit beast bloodline essence' };
  }
  return null;
}
