// src/lib/sim/realms.ts
import type { RealmDef, RealmId, SubStage } from './types';

export const REALMS: RealmDef[] = [
  {
    id: 'mortal', name: 'Mortal', nameCn: '凡人', step: 0, order: 0, absoluteTier: 0, lifespan: 80,
    blurb: 'An ordinary human, untouched by the Dao. Life is brief and the world is vast and merciless.',
    unlocks: ['observe the world', 'manual labor', 'hear travelers\u2019 tales of immortals'],
    realityInfluence: 'None. You are entirely subject to the world \u2014 weather, beasts, time.',
    qiGate: 0, daoGate: 0,
  },
  {
    id: 'qi_condensation', name: 'Qi Condensation', nameCn: '凝气', step: 1, order: 1, absoluteTier: 1, lifespan: 150,
    blurb: 'The first breath of spiritual energy. You begin to sense the Qi that saturates heaven and earth.',
    unlocks: ['sense Qi', 'weak techniques', 'strengthen body', 'use low-grade talismans'],
    realityInfluence: 'None on terrain. You can crack a stone with effort, no more.',
    qiGate: 100, daoGate: 5,
  },
  {
    id: 'foundation', name: 'Foundation Establishment', nameCn: '筑基', step: 1, order: 2, absoluteTier: 2, lifespan: 300,
    blurb: 'A spiritual foundation is laid within. Lifespan extends; the body is remade.',
    unlocks: ['ride flying treasures', 'stronger techniques', 'basic formations', 'spiritual control'],
    realityInfluence: 'Minor. You can fell ancient trees, crack boulders, leave marks on stone.',
    qiGate: 600, daoGate: 15,
  },
  {
    id: 'core_formation', name: 'Core Formation', nameCn: '结丹', step: 1, order: 3, absoluteTier: 3, lifespan: 500,
    blurb: 'Qi condenses into a Golden Core. A personal Dao begins to form; techniques become your own.',
    unlocks: ['personal Dao forms', 'personalized techniques', 'artifact bonding', 'core-tier alchemy'],
    realityInfluence: 'Small terrain edits where the world\u2019s law permits.',
    qiGate: 2400, daoGate: 30,
  },
  {
    id: 'nascent_soul', name: 'Nascent Soul', nameCn: '元婴', step: 1, order: 4, absoluteTier: 4, lifespan: 1000,
    blurb: 'The soul becomes an independent infant. A being fundamentally different from mortals.',
    unlocks: ['independent soul', 'avatar projection', 'divine sense', 'soul attacks', 'remote perception', 'possession', 'advanced formations'],
    realityInfluence: 'Reshape landscape up to the world\u2019s voxel-reality cap.',
    qiGate: 8000, daoGate: 45,
  },
  {
    id: 'soul_formation', name: 'Soul Formation', nameCn: '化神', step: 1, order: 5, absoluteTier: 5, lifespan: 2000,
    blurb: 'A Domain forms \u2014 a field of personal law. Reality begins to bend to your will within it.',
    unlocks: ['personal Domain', 'local law override', 'suppress lower realms by aura'],
    realityInfluence: 'Local reality override inside the Domain.',
    qiGate: 24000, daoGate: 60,
  },
  {
    id: 'soul_transformation', name: 'Soul Transformation', nameCn: '炼虚', step: 1, order: 6, absoluteTier: 6, lifespan: 3000,
    blurb: 'The Domain matures; soul and law merge. Your presence alone alters a region.',
    unlocks: ['mature Domain', 'aura suppression', 'soul-law fusion'],
    realityInfluence: 'Strong local override; regional aura pressure.',
    qiGate: 60000, daoGate: 70,
  },
  {
    id: 'ascendant', name: 'Ascendant', nameCn: '问鼎', step: 1, order: 7, absoluteTier: 7, lifespan: 5000,
    blurb: 'Asking the heavens. The peak of the mortal path, preparing to step onto the Dao.',
    unlocks: ['peak mortal power', 'prepare for Dao step', 'continental influence'],
    realityInfluence: 'Continental-scale influence.',
    qiGate: 140000, daoGate: 80,
  },
  {
    id: 'illusory_yin', name: 'Illusory Yin', nameCn: '空涅', step: 2, order: 8, absoluteTier: 8, lifespan: 8000,
    blurb: 'Stepping out of the mortal coil. Half-ethereal, no longer truly mortal.',
    unlocks: ['half-ethereal body', 'glimpse karmic threads'],
    realityInfluence: 'Karma becomes visible; body part-ethereal.',
    qiGate: 300000, daoGate: 85,
  },
  {
    id: 'corporeal_yang', name: 'Corporeal Yang', nameCn: '阳神', step: 2, order: 9, absoluteTier: 8, lifespan: 12000,
    blurb: 'The yang body solidifies. You stand beyond mortality.',
    unlocks: ['solid yang body', 'resist mortal-world decay'],
    realityInfluence: 'No longer bound by mortal-world decay.',
    qiGate: 600000, daoGate: 88,
  },
  {
    id: 'nirvana_scryer', name: 'Nirvana Scryer', nameCn: '窥霓', step: 3, order: 10, absoluteTier: 9, lifespan: 20000,
    blurb: 'Glimpse the nirvana of the Dao. Karma is now something you can read and weigh.',
    unlocks: ['glimpse Dao-nirvana', 'visible karma', 'escalating tribulations'],
    realityInfluence: 'World-scale influence begins.',
    qiGate: 1200000, daoGate: 90,
  },
  {
    id: 'nirvana_cleanser', name: 'Nirvana Cleanser', nameCn: '净涅槃', step: 3, order: 11, absoluteTier: 9, lifespan: 30000,
    blurb: 'Cleanse karma within nirvana. Tribulations grow terrifying.',
    unlocks: ['cleanse karma', 'survive escalated tribulations'],
    realityInfluence: 'World-scale; karma-cleansing reshapes fate.',
    qiGate: 2500000, daoGate: 92,
  },
  {
    id: 'nirvana_fruit', name: 'Nirvana Fruit', nameCn: '涅槃果', step: 3, order: 12, absoluteTier: 10, lifespan: 50000,
    blurb: 'Bear the fruit of nirvana. Fate itself bends toward your Dao.',
    unlocks: ['bear nirvana fruit', 'fate-bending'],
    realityInfluence: 'Fate bends; region-wide karmic shifts.',
    qiGate: 5000000, daoGate: 94,
  },
  {
    id: 'spirit_seizer', name: 'Spirit Seizer', nameCn: '夺神', step: 3, order: 13, absoluteTier: 10, lifespan: 100000,
    blurb: 'Seize the Dao. Define your law upon the world.',
    unlocks: ['seize the Dao', 'define personal law'],
    realityInfluence: 'World-scale law definition.',
    qiGate: 10000000, daoGate: 96,
  },
  {
    id: 'true_immortal', name: 'True Immortal', nameCn: '真仙', step: 4, order: 14, absoluteTier: 11, lifespan: -1,
    blurb: 'True immortality. A mortal thinks "destroying a mountain is impossible"; you think "that mountain was inconvenient."',
    unlocks: ['eternal life', 'worlds as resources', 'law manipulation'],
    realityInfluence: 'Mortal worlds cannot contain you.',
    qiGate: 30000000, daoGate: 98,
  },
  {
    id: 'ancient', name: 'Ancient Realm', nameCn: '古境', step: 4, order: 15, absoluteTier: 12, lifespan: -1,
    blurb: 'Touch the power of the Ancient Clans \u2014 God, Devil, Demon. Body-stars kindle within.',
    unlocks: ['Ancient-Clan power', 'body-stars'],
    realityInfluence: 'Star-system scale.',
    qiGate: 80000000, daoGate: 99,
  },
  {
    id: 'paragon', name: 'Paragon / Sovereign', nameCn: '大天尊', step: 4, order: 16, absoluteTier: 13, lifespan: -1,
    blurb: 'Rule a starry sky. Your presence alone reshapes a world.',
    unlocks: ['rule a starry sky', 'reshape worlds by presence'],
    realityInfluence: 'Star-system scale; reshape worlds by presence.',
    qiGate: 200000000, daoGate: 100,
  },
  {
    id: 'transcendence', name: 'Heaven-Trampling', nameCn: '踏天', step: 4, order: 17, absoluteTier: 14, lifespan: -1,
    blurb: 'Beyond all heavens. Outside the local Allheaven\u2019s law. The path Wang Lin walked.',
    unlocks: ['transcend Allheaven', 'multiverse-scale influence'],
    realityInfluence: 'Multiverse scale.',
    qiGate: Number.MAX_SAFE_INTEGER, daoGate: 100,
  },
];

export const REALM_MAP: Record<RealmId, RealmDef> = Object.fromEntries(
  REALMS.map((r) => [r.id, r]),
) as Record<RealmId, RealmDef>;

export function realmOrder(id: RealmId): number {
  return REALM_MAP[id]?.order ?? 0;
}

export function nextRealm(id: RealmId): RealmId | null {
  const o = realmOrder(id);
  const nxt = REALMS.find((r) => r.order === o + 1);
  return nxt ? nxt.id : null;
}

export function realmAtLeast(a: RealmId, b: RealmId): boolean {
  return realmOrder(a) >= realmOrder(b);
}

// Absolute Power Threshold (the user's core mechanic):
//   S_eff = max(0, L_world - P_player)
// When P_player < L_world  → Power Deficit: suppressed (flight/casting/voxel-destruction throttled).
// When P_player >= L_world → Sovereign Break-Even: suppression = 0, you dominate that world.
export function absoluteTier(id: RealmId): number {
  return REALM_MAP[id]?.absoluteTier ?? 0;
}

export function effectiveSuppression(worldLawTier: number, playerRealm: RealmId): number {
  return Math.max(0, worldLawTier - absoluteTier(playerRealm));
}

// Suppression as a 0..1 debuff multiplier on flight/casting/voxel-destruction.
export function suppressionFactor(worldLawTier: number, playerRealm: RealmId): number {
  const s = effectiveSuppression(worldLawTier, playerRealm);
  // each tier of deficit ≈ 18% debuff, capped near 0.95
  return Math.min(0.95, s * 0.18);
}

export const SUBSTAGE_MULT: Record<SubStage, number> = {
  early: 0.85, middle: 1, late: 1.15, peak: 1.3,
};

export function realmLabel(id: RealmId, sub?: SubStage): string {
  const r = REALM_MAP[id];
  if (!r) return id;
  return sub && sub !== 'middle' ? `${r.name} (${sub})` : r.name;
}

// Reality-impact summary for the matrix the user asked for (System 4 + Layer 3).
export const REALM_REALITY_IMPACT: Record<RealmId, { interaction: string; becomes: string }> = {
  mortal: { becomes: 'qi-unaware mortal', interaction: 'sense nothing of the Dao; subject fully to the world' },
  qi_condensation: { becomes: 'qi-aware mortal', interaction: 'sense spiritual energy · strengthen body · basic techniques · low-grade talismans' },
  foundation: { becomes: 'spiritually founded being', interaction: 'spiritual foundation · longer lifespan · flying treasures usable · basic formations' },
  core_formation: { becomes: 'personal-energy-core being', interaction: 'personal Dao forms · techniques personalize · artifact bonding · advanced techniques' },
  nascent_soul: { becomes: 'independent-soul being', interaction: 'independent soul · soul travel · divine-sense attacks · possession · remote perception' },
  soul_formation: { becomes: 'law-emanating being', interaction: 'personal laws · Domain · reality influence begins' },
  soul_transformation: { becomes: 'soul-law fused being', interaction: 'mature Domain · aura suppression · regional reality override' },
  ascendant: { becomes: 'peak mortal being', interaction: 'asking the heavens · continental influence · prepare for Dao step' },
  illusory_yin: { becomes: 'half-ethereal being', interaction: 'stepping out of the mortal coil · glimpse karmic threads' },
  corporeal_yang: { becomes: 'solid yang being', interaction: 'solid yang body · beyond mortal decay' },
  nirvana_scryer: { becomes: 'Dao-glimpsing being', interaction: 'glimpse Dao-nirvana · karma visible · escalating tribulations' },
  nirvana_cleanser: { becomes: 'karma-cleansing being', interaction: 'cleanse karma in nirvana · tribulations escalate' },
  nirvana_fruit: { becomes: 'fate-bearing being', interaction: 'bear nirvana fruit · fate itself bends' },
  spirit_seizer: { becomes: 'Dao-seizing being', interaction: 'seize the Dao · define your law upon the world' },
  true_immortal: { becomes: 'beyond-the-world being', interaction: 'mountains become insignificant · worlds become resources · laws manipulable' },
  ancient: { becomes: 'Ancient-Clan-touched being', interaction: 'touch God/Devil/Demon power · kindle body-stars' },
  paragon: { becomes: 'starry-sky ruler', interaction: 'rule a starry sky · reshape worlds by presence' },
  transcendence: { becomes: 'Heaven-Trampling being', interaction: 'beyond all heavens · outside the local Allheaven\u2019s law' },
};
