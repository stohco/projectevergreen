// src/lib/sim/soul.ts
// The Unified 13-Dimensional Soul Matrix + Mega Archetype Registry + Voxel Factorization.
// Every living entity (human, beast, demon, ancient) inherits the same 13-D vector.
// Archetypes are classified from slider values — not chosen.

import type { SoulMatrix, ArchetypeId, ArchetypeDef, Technique, RealmId } from './types';
import type { RNG } from './generators';
import { pick, randInt } from './generators';
import { absoluteTier } from './realms';

// ─── Soul Matrix generation ────────────────────────────────────────
export function genSoul(rng: RNG, bias?: Partial<SoulMatrix>): SoulMatrix {
  const r = () => Math.round(rng() * 100) / 100;
  const s: SoulMatrix = {
    altruism: r(), ambition: r(), paranoia: r(), curiosity: r(), honor: r(),
    faceObsession: r(), heavenlyDefiance: r(), mortalTether: r(), vengefulness: r(),
    mortalityPanic: r(), chaosConcoction: r(), shamelessness: r(), loyaltyTether: r(),
  };
  return { ...s, ...(bias ?? {}) };
}

// ─── Mega Archetype Registry ───────────────────────────────────────
export const ARCHETYPES: ArchetypeDef[] = [
  { id: 'philanthropic_hermit', name: 'The Philanthropic Hermit', blurb: 'Settles near tribulation zones; builds defensive arrays to shield struggling cultivators without asking for loot.', loopBehavior: 'shields weaker cultivators near dangerous zones' },
  { id: 'righteous_hypocrite', name: 'The Righteous Hypocrite', blurb: 'Farms public reputation in towns; backstabs in wilderness chunks with rare drops.', loopBehavior: 'benevolent in public, murderous when isolated with loot' },
  { id: 'obsessive_scholar', name: 'The Obsessive Scholar', blurb: 'Ignores faction politics; tracks the player to cast capture arrays to analyze your custom techniques.', loopBehavior: 'pursues the player to study their techniques' },
  { id: 'arrogant_young_master', name: 'The Arrogant Young Master', blurb: 'Spawns with bodyguard entities; claims resource veins; escalates faction hostility if you refuse to yield.', loopBehavior: 'claims veins, demands submission, escalates on refusal' },
  { id: 'uncompromising_seeker', name: 'The Uncompromising Seeker', blurb: 'Views the player as a Dao peer; trades fairly but promises a life-or-death duel at the next tier.', loopBehavior: 'fair trade now, deadly duel at the next realm' },
  { id: 'lovable_cowardly_prodigy', name: 'The Lovable Cowardly Prodigy', blurb: 'Refuses risky dungeons unless shielded; accidentally blows up crafting stations; fights like a demon if your HP drops below 15%.', loopBehavior: 'cowardly until an ally is critical, then berserks' },
  { id: 'kleptomaniac_spirit_bird', name: 'The Kleptomaniac Spirit Bird', blurb: 'A talking creature that steals shiny items from your storage; can be bribed with ore to reveal inheritance coordinates.', loopBehavior: 'steals valuables, trades coordinates for ore' },
  { id: 'nagging_meat_jelly', name: 'The Nagging Meat Jelly', blurb: 'An un-damageable entity that attaches if your karma drops; lectures on morality but throws its hitbox in front of lethal strikes.', loopBehavior: 'karma-attached, lectures, but protects to the death' },
  { id: 'loyal_culinary_glutton', name: 'The Loyal Culinary Glutton', blurb: 'Manages your kitchen; cooks food buffs that permanently expand max Qi; defends cooking pots to the death.', loopBehavior: 'cooks Qi-expanding food, defends the kitchen' },
  { id: 'reincarnated_old_monster', name: 'The Reincarnated Old Monster', blurb: 'Appears as a weak child or low-tier beast; secretly harbors vast arrays; tracks blood-grudges across dimensional layers.', loopBehavior: 'weak facade, vast hidden power, cross-dimensional grudges' },
  { id: 'feral_beast', name: 'Feral Beast', blurb: 'A wild creature driven by hunger, fear, and territory. No higher drives.', loopBehavior: 'hunger · fear · territory · mating' },
  { id: 'generic_cultivator', name: 'Wandering Cultivator', blurb: 'A balanced cultivator with no extreme drives.', loopBehavior: 'cultivates, trades, seeks breakthroughs' },
];

export const ARCHETYPE_MAP: Record<ArchetypeId, ArchetypeDef> = Object.fromEntries(ARCHETYPES.map((a) => [a.id, a])) as Record<ArchetypeId, ArchetypeDef>;

// Classify an entity's archetype from its 13-D soul matrix.
export function classifyArchetype(soul: SoulMatrix, isBeast = false): ArchetypeId {
  if (isBeast && soul.curiosity < 0.3 && soul.honor < 0.3 && soul.loyaltyTether < 0.3) return 'feral_beast';
  // score each archetype by how well the soul matches its bias profile
  const scores: [ArchetypeId, number][] = [
    ['philanthropic_hermit', soul.altruism * 0.5 + soul.honor * 0.3 - soul.ambition * 0.3],
    ['righteous_hypocrite', soul.ambition * 0.4 + soul.faceObsession * 0.4 - soul.honor * 0.3 - soul.altruism * 0.2],
    ['obsessive_scholar', soul.curiosity * 0.6 - soul.faceObsession * 0.2 - soul.vengefulness * 0.2],
    ['arrogant_young_master', soul.faceObsession * 0.5 + soul.paranoia * 0.3 - soul.mortalityPanic * 0.3],
    ['uncompromising_seeker', soul.ambition * 0.4 + soul.heavenlyDefiance * 0.4 - soul.shamelessness * 0.4],
    ['lovable_cowardly_prodigy', soul.mortalityPanic * 0.4 + soul.chaosConcoction * 0.3 + soul.loyaltyTether * 0.3],
    ['kleptomaniac_spirit_bird', soul.shamelessness * 0.5 + soul.chaosConcoction * 0.3 - soul.honor * 0.3],
    ['nagging_meat_jelly', soul.honor * 0.4 - soul.mortalityPanic * 0.3 + soul.shamelessness * 0.3],
    ['loyal_culinary_glutton', soul.loyaltyTether * 0.4 + soul.mortalTether * 0.3 - soul.ambition * 0.3],
    ['reincarnated_old_monster', soul.paranoia * 0.3 + soul.vengefulness * 0.4 + soul.ambition * 0.3],
  ];
  scores.sort((a, b) => b[1] - a[1]);
  // require a meaningful top score, else generic
  return scores[0][1] > 0.35 ? scores[0][0] : 'generic_cultivator';
}

// ─── Voxel Factorization Engine (the user's equations) ─────────────
// F_destruct = (B_base × P_player × C_tech × Q_artifact) / (S_eff + 1)²
// R_voxel    = μ_mat × (1 + (σ_world × L_world) / 10)
// If F_destruct >= R_voxel → blocks vaporize/terraform/freeze per the operator.

const GRADE_BASE: Record<string, number> = { mortal: 5, magical: 15, spirit: 50, immortal: 200, dao: 1000 };
const MATERIAL_HARDNESS: Record<string, number> = {
  air: 0, dirt: 2, wood: 3, stone: 8, iron_ore: 14, obsidian: 25, bedrock: 100, spirit_stone_block: 18, jade: 30,
};
const WORLD_SIGMA: Record<string, number> = { fragile: 0.3, low: 0.6, medium: 1.0, high: 1.6, absolute: 2.5 };

export interface VoxelFactorInputs {
  technique: Technique;
  playerRealm: RealmId;
  artifactQuality: number; // Q_artifact, 1..3 (none=1)
  worldLawTier: number;    // L_world
  worldLawStrength: string;// for σ_world
  suppressBypass: number;  // from the technique's dao-suppression
  blockMaterial?: string;  // μ_mat
}

export interface VoxelFactorResult {
  F_destruct: number;
  R_voxel: number;
  vaporized: boolean;
  blocksAffected: number;  // scale × geometry footprint
  operator: string;
  geometry: string;
}

export function voxelFactorize(inp: VoxelFactorInputs): VoxelFactorResult {
  const B_base = GRADE_BASE[inp.technique.grade] ?? 10;
  const P_player = absoluteTier(inp.playerRealm) + 1;
  const C_tech = (1 - inp.technique.comprehensionDifficulty) * 1.5 + 0.5;
  const Q_artifact = inp.artifactQuality;
  const S_eff = Math.max(0, inp.worldLawTier - absoluteTier(inp.playerRealm)) - inp.suppressBypass;
  const S_clamped = Math.max(0, S_eff);
  const F_destruct = Math.round((B_base * P_player * C_tech * Q_artifact) / Math.pow(S_clamped + 1, 2));
  const mu_mat = MATERIAL_HARDNESS[inp.blockMaterial ?? 'stone'] ?? 8;
  const sigma_world = WORLD_SIGMA[inp.worldLawStrength] ?? 0.6;
  const R_voxel = Math.round(mu_mat * (1 + (sigma_world * inp.worldLawTier) / 10));
  const vaporized = F_destruct >= R_voxel;
  const blocksAffected = inp.technique.scale * (vaporized ? 64 : 4);
  return {
    F_destruct, R_voxel, vaporized,
    blocksAffected,
    operator: inp.technique.blockOperator,
    geometry: inp.technique.voxelGeometry,
  };
}

// ─── Physics → Psychology cascade ──────────────────────────────────
// When a cultivator casts a technique, the voxel destruction alters the emotional
// state of every loaded NPC in the vicinity — per their 13-D vector.
export interface PsychologicalReaction {
  npcId: string;
  npcName: string;
  reaction: 'flee' | 'investigate' | 'rage' | 'shield_player' | 'envy' | 'indifferent';
  reason: string;
  soulDelta?: Partial<SoulMatrix>;
}

export function psychologicalCascade(opts: {
  casterName: string;
  casterIsPlayer: boolean;
  vaporized: boolean;
  blocksAffected: number;
  nearbyNpcs: { id: string; name: string; soul: SoulMatrix; isAlly?: boolean }[];
  destroyedMortalSettlement?: boolean;
}): PsychologicalReaction[] {
  const out: PsychologicalReaction[] = [];
  for (const npc of opts.nearbyNpcs) {
    const s = npc.soul;
    // High paranoia → flee
    if (s.paranoia > 0.6 && opts.vaporized) {
      out.push({ npcId: npc.id, npcName: npc.name, reaction: 'flee', reason: `paranoia ${Math.round(s.paranoia*100)}% — fled the blast vector`, soulDelta: { mortalityPanic: Math.min(1, s.mortalityPanic + 0.1) } });
      continue;
    }
    // High curiosity → investigate
    if (s.curiosity > 0.6) {
      out.push({ npcId: npc.id, npcName: npc.name, reaction: 'investigate', reason: `curiosity ${Math.round(s.curiosity*100)}% — walked to the crater to sample lingering elements`, soulDelta: {} });
      continue;
    }
    // High mortalTether + destroyed settlement → rage
    if (s.mortalTether > 0.6 && opts.destroyedMortalSettlement) {
      out.push({ npcId: npc.id, npcName: npc.name, reaction: 'rage', reason: `mortalTether ${Math.round(s.mortalTether*100)}% — ancestral home destroyed; vengefulness maxed`, soulDelta: { vengefulness: 1, loyaltyTether: 0 } });
      continue;
    }
    // High loyaltyTether + ally → shield player
    if (s.loyaltyTether > 0.6 && opts.casterIsPlayer && npc.isAlly) {
      out.push({ npcId: npc.id, npcName: npc.name, reaction: 'shield_player', reason: `loyaltyTether ${Math.round(s.loyaltyTether*100)}% — moved to shield you`, soulDelta: {} });
      continue;
    }
    // High ambition/faceObsession + not ally → envy
    if ((s.ambition > 0.6 || s.faceObsession > 0.6) && !npc.isAlly && opts.vaporized) {
      out.push({ npcId: npc.id, npcName: npc.name, reaction: 'envy', reason: `ambition/face — covets your power`, soulDelta: { paranoia: Math.min(1, s.paranoia + 0.1) } });
      continue;
    }
    out.push({ npcId: npc.id, npcName: npc.name, reaction: 'indifferent', reason: 'no strong drive triggered', soulDelta: {} });
  }
  return out;
}

// ─── Ally Loyalty Matrix ───────────────────────────────────────────
// Stability = Affinity + (Fear × 0.5) − ΔDaoHeart
// If Stability < 0:
//   - high honor → silent desertion (leaves a scroll)
//   - low honor + high ambition/paranoia → tactical betrayal (stays, collects weaknesses, strikes when player is weak)
export function computeAllyStability(opts: {
  affinity: number;   // -1..1
  fear: number;       // 0..1
  daoHeartDelta: number; // accumulated misalignment, 0..1
}): number {
  return opts.affinity + opts.fear * 0.5 - opts.daoHeartDelta;
}

export function resolveAllyBetrayalCheck(opts: { soul: SoulMatrix; stability: number }): 'loyal' | 'desert' | 'betray' {
  if (opts.stability >= 0) return 'loyal';
  const s = opts.soul;
  if (s.honor > 0.5) return 'desert';
  if (s.ambition > 0.5 || s.paranoia > 0.5) return 'betray';
  return 'desert';
}

// ─── Soul display helpers ──────────────────────────────────────────
export const SOUL_DIMENSIONS: { key: keyof SoulMatrix; label: string; group: string }[] = [
  { key: 'altruism', label: 'Altruism', group: 'Ethical' },
  { key: 'ambition', label: 'Ambition', group: 'Ethical' },
  { key: 'paranoia', label: 'Paranoia', group: 'Ethical' },
  { key: 'curiosity', label: 'Curiosity', group: 'Ethical' },
  { key: 'honor', label: 'Honor', group: 'Ethical' },
  { key: 'faceObsession', label: 'Face-Obsession', group: 'Social' },
  { key: 'heavenlyDefiance', label: 'Heavenly Defiance', group: 'Social' },
  { key: 'mortalTether', label: 'Mortal Tether', group: 'Social' },
  { key: 'vengefulness', label: 'Vengefulness', group: 'Social' },
  { key: 'mortalityPanic', label: 'Mortality Panic', group: 'Comedic' },
  { key: 'chaosConcoction', label: 'Chaos Concoction', group: 'Comedic' },
  { key: 'shamelessness', label: 'Shamelessness', group: 'Comedic' },
  { key: 'loyaltyTether', label: 'Loyalty Tether', group: 'Comedic' },
];
