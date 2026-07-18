// src/lib/sim/divine.ts
// Soul Power (hybrid derived-additive), Divine Sense pipeline, Xianxia Status Matrix,
// Spiritual Camouflage registry, Karmic Backlash engine.

import type {
  SoulPower, XianxiaStatus, XianxiaStatusId, ConcealedObject, RealmId, World, NPC, DaoId,
} from './types';
import type { RNG } from './generators';
import { pick, randInt, uid } from './generators';
import { REALM_MAP, realmOrder, absoluteTier } from './realms';
import { ARCHETYPE_MAP } from './soul';

// ─── Soul Power: S_realm baseline per tier ──────────────────────────
// Qi Condensation ≈10, Foundation ≈100, Core ≈500, Nascent Soul ≈10,000,
// Soul Formation ≈30,000, Ascendant ≈80,000, Immortal ≈1,000,000 (logarithmic per the spec).
const S_REALM: Record<RealmId, number> = {
  mortal: 1, qi_condensation: 10, foundation: 100, core_formation: 500,
  nascent_soul: 10000, soul_formation: 30000, soul_transformation: 60000, ascendant: 80000,
  illusory_yin: 150000, corporeal_yang: 250000,
  nirvana_scryer: 400000, nirvana_cleanser: 600000, nirvana_fruit: 800000, spirit_seizer: 900000,
  true_immortal: 1000000, ancient: 5000000, paragon: 20000000, transcendence: 100000000,
};

export function sRealm(realm: RealmId): number {
  return S_REALM[realm] ?? 1;
}

// S_sense = S_realm × (1 + ΣM_manual) + S_tempering
// S_tempering is soft-capped at S_realm × (realm_order+1) so dark-arts tempering can exceed
// your realm but not infinitely (a Qi-Condensation cultivator can't out-soul an immortal forever).
export function soulPowerTotal(realm: RealmId, sp: SoulPower): number {
  const sR = sRealm(realm);
  const manualBonus = sp.manuals.reduce((s, m) => s + m.multiplier, 0);
  const temperingCap = sR * (realmOrder(realm) + 1);
  const tempering = Math.min(sp.tempering, temperingCap);
  return Math.round(sR * (1 + manualBonus) + tempering);
}

export function emptySoulPower(): SoulPower {
  return { manuals: [], tempering: 0, manualBonus: 0 };
}

// ─── Divine Sense radius (the user's equation) ──────────────────────
// R_sense = R_base × ln(S_sense + 1) × (1 − L_world/12) × Π(1 − σ_seal)
//   R_base = 16 blocks (default; raised by movement/divine-sense arts)
//   σ_seal = local sealing-formation suppression (0..1 each, multiplied)
export function divineSenseRadius(opts: { sSense: number; worldLawTier: number; rBase?: number; seals?: number[] }): number {
  const rBase = opts.rBase ?? 16;
  const lawFactor = Math.max(0.05, 1 - opts.worldLawTier / 12);
  const sealProduct = (opts.seals ?? []).reduce((p, s) => p * (1 - s), 1);
  const r = rBase * Math.log(opts.sSense + 1) * lawFactor * sealProduct;
  return Math.max(4, Math.round(r));
}

// ─── Spiritual Camouflage registry generation ───────────────────────
const CONCEALED_KINDS: ConcealedObject['kind'][] = ['inheritance_vault', 'spirit_vein', 'trap_array', 'hidden_cave', 'sealed_pavilion', 'ancient_formation'];
const CONCEALED_NAMES: Record<ConcealedObject['kind'], string[]> = {
  inheritance_vault: ['Jade-Void Vault', "Patriarch's Sealed Cave", 'Lotus-Pond Reliquary', 'Bone-Sealed Pavilion'],
  spirit_vein: ['Hidden Jade Vein', 'Concealed Frost Vein', 'Buried Fire-Vein', 'Vein of the Forgotten'],
  trap_array: ['The Nine-Knives Array', 'Silent-Bell Trap', 'Blood-Seal Ambush Array'],
  hidden_cave: ["Hermit's Cave", 'Cave of the Sleeping Fox', 'Secluded Meditation Grotto'],
  sealed_pavilion: ['Sealed Scripture Pavilion', 'Locked Pill-Hall', 'Forbidden Library'],
  ancient_formation: ['Dormant Heaven-Array', 'Forgotten Transport Array', 'Slumbering Defensive Formation'],
};
const CAMOUFLAGE_BY_KIND: Record<ConcealedObject['kind'], number> = {
  inheritance_vault: 25000, spirit_vein: 500, trap_array: 8000, hidden_cave: 200,
  sealed_pavilion: 15000, ancient_formation: 20000,
};

export function genConcealedObject(rng: RNG, regionId: string, kind?: ConcealedObject['kind']): ConcealedObject {
  const k = kind ?? pick(rng, CONCEALED_KINDS);
  return {
    id: uid('conc', rng),
    kind: k,
    name: pick(rng, CONCEALED_NAMES[k]),
    regionId,
    camouflage: CAMOUFLAGE_BY_KIND[k],
    reward: pick(rng, ['a hidden technique scroll', 'a sealed pill', 'raw spirit ore', 'an ancient inheritance fragment', 'a formation blueprint', 'nothing — it was a decoy']),
    perceived: false,
    coords: pick(rng, ['beneath the eastern ridge', 'behind the waterfall', 'under the old shrine', 'inside the collapsed mine', 'beyond the thornwood', 'below the spirit-vein']),
  };
}

// ─── Divine Sense pulse: the NPC confrontation (ΔS = S_player − S_npc) ─
export type SenseReaction = {
  npcId: string;
  npcName: string;
  archetype?: string;
  outcome: 'intercepted' | 'suppressed' | 'unmasked' | 'unnoticed' | 'ally_alerted';
  detail: string;
  statusInflicted?: XianxiaStatusId; // e.g. soul_fracture from a counter-strike
  soulUnmasked?: boolean; // for the Righteous Hypocrite reveal
};

export function divineSensePulse(opts: {
  playerSSense: number;
  playerRealm: RealmId;
  nearbyNpcs: NPC[];
  rng: RNG;
}): { reactions: SenseReaction[]; soulFractureRisk: number } {
  const { playerSSense, nearbyNpcs, rng } = opts;
  const reactions: SenseReaction[] = [];
  let soulFractureRisk = 0;
  for (const npc of nearbyNpcs) {
    const npcSSense = soulPowerTotal(npc.realm, npc.soulPower ?? emptySoulPower());
    const delta = playerSSense - npcSSense;
    const soul = npc.soulMatrix;
    // Scenario A: Arrogant Interception (ΔS < 0, NPC stronger)
    if (delta < 0) {
      if (soul.faceObsession >= 0.7) {
        const counterStrike = rng() < 0.6;
        reactions.push({
          npcId: npc.id, npcName: npc.name, archetype: npc.archetype,
          outcome: 'intercepted',
          detail: `ΔS=${Math.round(delta)} — ${npc.name} detected your scan and, face-obsessed, read it as disrespect${counterStrike ? ' and fired a mental counter-strike (Soul Fracture risk!)' : '. They glower at you.'}.`,
          statusInflicted: counterStrike ? 'soul_fracture' : undefined,
        });
        if (counterStrike) soulFractureRisk = Math.max(soulFractureRisk, 0.5);
      } else {
        reactions.push({ npcId: npc.id, npcName: npc.name, archetype: npc.archetype, outcome: 'intercepted', detail: `ΔS=${Math.round(delta)} — ${npc.name} detected your scan but dismissed it.` });
      }
      continue;
    }
    // Scenario B: Absolute Spiritual Suppression (ΔS ≥ 5000, player far stronger)
    if (delta >= 5000) {
      if (soul.mortalityPanic >= 0.6 || soul.shamelessness >= 0.6) {
        reactions.push({
          npcId: npc.id, npcName: npc.name, archetype: npc.archetype,
          outcome: 'suppressed',
          detail: `ΔS=${Math.round(delta)} — absolute mental dominance. ${npc.name} drops to their knees, begging. Hidden trade tabs unlocked; taming requirements bypassed for beasts.`,
        });
        continue;
      }
    }
    // Scenario C: Unmasking the Hypocrite (Righteous Hypocrite archetype revealed)
    if (npc.archetype === 'righteous_hypocrite' && delta >= 2000) {
      reactions.push({
        npcId: npc.id, npcName: npc.name, archetype: npc.archetype,
        outcome: 'unmasked',
        detail: `ΔS=${Math.round(delta)} — your sense penetrates ${npc.name}'s concealment script! Their false aura of altruism/honor shatters; the true matrix is revealed: max ambition, near-zero honor.`,
        soulUnmasked: true,
      });
      continue;
    }
    // Ally alerted
    if (delta < 5000 && rng() < 0.2) {
      reactions.push({ npcId: npc.id, npcName: npc.name, archetype: npc.archetype, outcome: 'ally_alerted', detail: `${npc.name} felt your sense brush them but recognized you.` });
      continue;
    }
    reactions.push({ npcId: npc.id, npcName: npc.name, archetype: npc.archetype, outcome: 'unnoticed', detail: `${npc.name} did not notice your scan.` });
  }
  return { reactions, soulFractureRisk };
}

// ─── Xianxia Status Matrix helpers ──────────────────────────────────
export const STATUS_META: Record<XianxiaStatusId, { name: string; color: string; cure: string; desc: string }> = {
  soul_fracture: { name: 'Soul Fracture', color: 'vermilion-text', cure: 'Soul-Mending Elixir', desc: 'Divine Sense radar blinded; flying artifacts grounded; camera shake/blur.' },
  sealed_meridians: { name: 'Sealed Meridians', color: 'amber-text', cure: 'Meridian-Clearing Powder', desc: 'Locks a % of max Qi; high-tier spells un-castable.' },
  qi_deviation: { name: 'Qi Deviation', color: 'vermilion-text', cure: 'Purification Pill', desc: 'Casting drains health; internal energy reversed.' },
  heart_demon: { name: 'Heart Demon', color: 'vermilion-text', cure: 'Heart-Calming Incense / resolve bottleneck', desc: 'Raises breakthrough failure; karmic/tribulation backlash.' },
  karmic_stain: { name: 'Karmic Stain', color: 'vermilion-text', cure: 'Nirvana cleansing / karma-resolving arts', desc: 'Visible reputation marker; righteous sects blacklist you.' },
  meat_jelly: { name: 'Lord Third (Meat Jelly)', color: 'amber-text', cure: 'Reduce karmicDebt below atrocious', desc: 'Invincible nagging companion; hijacks dialogue; draws aggro.' },
};

export function applyStatus(statuses: XianxiaStatus[], id: XianxiaStatusId, severity: number, remaining: number, source: string): void {
  const existing = statuses.find((s) => s.id === id);
  if (existing) {
    existing.severity = Math.max(existing.severity, severity);
    if (remaining === -1) existing.remaining = -1;
    else existing.remaining = Math.max(existing.remaining, remaining);
    existing.source = source;
  } else {
    statuses.push({ id, severity, remaining, source });
  }
}

export function cureStatus(statuses: XianxiaStatus[], id: XianxiaStatusId): boolean {
  const i = statuses.findIndex((s) => s.id === id);
  if (i >= 0) { statuses.splice(i, 1); return true; }
  return false;
}

export function hasStatus(statuses: XianxiaStatus[], id: XianxiaStatusId): boolean {
  return statuses.some((s) => s.id === id);
}

// ─── Karmic Backlash engine ─────────────────────────────────────────
export type KarmicTier = 'pure' | 'light' | 'tainted' | 'heavy' | 'atrocious';

export function karmicTier(karmicDebt: number): KarmicTier {
  if (karmicDebt < 0.1) return 'pure';
  if (karmicDebt < 0.3) return 'light';
  if (karmicDebt < 0.6) return 'tainted';
  if (karmicDebt < 0.85) return 'heavy';
  return 'atrocious';
}

export const KARMIC_TIER_LABEL: Record<KarmicTier, string> = {
  pure: 'Pure', light: 'Light Karma', tainted: 'Tainted', heavy: 'Heavy Karma', atrocious: 'Atrocious',
};

// Tribulation amplification from karmic debt (doubles at atrocious per the spec).
export function tribulationKarmaMultiplier(karmicDebt: number): number {
  const t = karmicTier(karmicDebt);
  return t === 'atrocious' ? 2.0 : t === 'heavy' ? 1.6 : t === 'tainted' ? 1.25 : 1.0;
}

// Check if righteous sects blacklist the player (karmic_stain status OR atrocious debt).
export function isBlacklisted(w: World): boolean {
  return hasStatus(w.player.statuses, 'karmic_stain') || karmicTier(w.player.karmicDebt) === 'atrocious';
}
