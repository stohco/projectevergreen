// src/lib/sim/content.ts
// Static content pools + label maps for the simulation.
import type { DaoId, ElementId, TechniqueIntent, TechniqueOrigin, TechniqueGrade, RealmId } from './types';

export const DAO_LIST: { id: DaoId; name: string; cn: string; desc: string }[] = [
  { id: 'sword', name: 'Sword', cn: '剑', desc: 'The path of the flying sword and sword-intent.' },
  { id: 'saber', name: 'Saber', cn: '刀', desc: 'Heavy, domineering blade-arts.' },
  { id: 'body', name: 'Body', cn: '体', desc: 'Body cultivation; the flesh as a weapon.' },
  { id: 'movement', name: 'Movement', cn: '身法', desc: 'Footwork, evasion, traversal.' },
  { id: 'divine_sense', name: 'Divine Sense', cn: '神识', desc: 'Soul-perception and mental attacks.' },
  { id: 'formation', name: 'Formation', cn: '阵', desc: 'Arrays and formations.' },
  { id: 'alchemy', name: 'Alchemy', cn: '丹', desc: 'Pill-refining and herb-lore.' },
  { id: 'talisman', name: 'Talisman', cn: '符', desc: 'Talisman inscription.' },
  { id: 'beast', name: 'Beast', cn: '兽', desc: 'Beast-taming and beast-arts.' },
  { id: 'slaughter', name: 'Slaughter', cn: '杀', desc: 'Wang Lin\u2019s dao; killing as truth.' },
  { id: 'seal', name: 'Seal', cn: '封', desc: 'Meng Hao\u2019s dao; sealing the heavens.' },
  { id: 'karma', name: 'Karma', cn: '因果', desc: 'Cause and effect; the deepest law.' },
  { id: 'fire', name: 'Fire', cn: '火', desc: 'Flame element.' },
  { id: 'water', name: 'Water', cn: '水', desc: 'Water element.' },
  { id: 'wind', name: 'Wind', cn: '风', desc: 'Wind element.' },
  { id: 'lightning', name: 'Lightning', cn: '雷', desc: 'Lightning element.' },
  { id: 'earth', name: 'Earth', cn: '土', desc: 'Earth element.' },
  { id: 'wood', name: 'Wood', cn: '木', desc: 'Wood element; life and growth.' },
  { id: 'metal', name: 'Metal', cn: '金', desc: 'Metal element; sharpness.' },
  { id: 'ice', name: 'Ice', cn: '冰', desc: 'Ice element.' },
  { id: 'time', name: 'Time', cn: '光阴', desc: 'Xu Qing\u2019s dao; outside of time.' },
  { id: 'space', name: 'Space', cn: '空', desc: 'Spatial arts.' },
  { id: 'life', name: 'Life', cn: '生', desc: 'Life and healing.' },
  { id: 'death', name: 'Death', cn: '死', desc: 'Death and necromancy.' },
  // Hundred Daos — Er Gen's signature unexpected paths
  { id: 'divination', name: 'Divination', cn: '卜', desc: 'Fate-reading and heavenly calculation. The Heavenly Fate Sect walks this path.' },
  { id: 'poison', name: 'Poison', cn: '毒', desc: 'Venom and toxin cultivation. A demonic path feared even by sects.' },
  { id: 'music', name: 'Music', cn: '音', desc: 'Sound as a weapon and a Dao. The copper bell rings across battlefields.' },
  { id: 'painting', name: 'Painting', cn: '画', desc: 'Ink-dragon breath, scroll-worlds. Art becomes reality.' },
  { id: 'calligraphy', name: 'Calligraphy', cn: '书', desc: 'Character inscription — the stroke IS the technique.' },
  { id: 'cooking', name: 'Cooking', cn: '食', desc: 'Culinary cultivation. The path Bai Xiaochun stumbled into by accident.' },
  { id: 'artifact_refining', name: 'Artifact Refining', cn: '炼器', desc: 'The forge-Dao. Shaping treasures from raw materials.' },
  { id: 'dream', name: 'Dream', cn: '梦', desc: 'Dreams within dreams; the sleeping Dao.' },
  { id: 'fate', name: 'Fate', cn: '命', desc: 'Destiny and its manipulation. The loom of fate.' },
  { id: 'reincarnation', name: 'Reincarnation', cn: '轮回', desc: 'The cycle of rebirth. Past lives as power.' },
];

export const DAO_MAP: Record<DaoId, { name: string; cn: string; desc: string }> = Object.fromEntries(
  DAO_LIST.map((d) => [d.id, { name: d.name, cn: d.cn, desc: d.desc }]),
) as Record<DaoId, { name: string; cn: string; desc: string }>;

export const ELEMENTS: { id: ElementId; name: string; cn: string }[] = [
  { id: 'fire', name: 'Fire', cn: '火' },
  { id: 'water', name: 'Water', cn: '水' },
  { id: 'wind', name: 'Wind', cn: '风' },
  { id: 'lightning', name: 'Lightning', cn: '雷' },
  { id: 'earth', name: 'Earth', cn: '土' },
  { id: 'wood', name: 'Wood', cn: '木' },
  { id: 'metal', name: 'Metal', cn: '金' },
  { id: 'ice', name: 'Ice', cn: '冰' },
  { id: 'light', name: 'Light', cn: '光' },
  { id: 'dark', name: 'Dark', cn: '暗' },
  { id: 'time', name: 'Time', cn: '光阴' },
  { id: 'space', name: 'Space', cn: '空' },
];

export const INTENTS: { id: TechniqueIntent; name: string }[] = [
  { id: 'destruction', name: 'Destruction' },
  { id: 'protection', name: 'Protection' },
  { id: 'healing', name: 'Healing' },
  { id: 'concealment', name: 'Concealment' },
  { id: 'speed', name: 'Speed' },
  { id: 'control', name: 'Control' },
  { id: 'summoning', name: 'Summoning' },
  { id: 'transformation', name: 'Transformation' },
  { id: 'execution', name: 'Execution' },
];

export const ORIGINS: { id: TechniqueOrigin; name: string }[] = [
  { id: 'ancient-inheritance', name: 'Ancient Inheritance' },
  { id: 'self-created', name: 'Self-Created' },
  { id: 'sect-taught', name: 'Sect-Taught' },
  { id: 'beast-derived', name: 'Beast-Derived' },
  { id: 'forbidden', name: 'Forbidden' },
  { id: 'divine', name: 'Divine' },
];

export const GRADES: TechniqueGrade[] = ['mortal', 'magical', 'spirit', 'immortal', 'dao'];

export const GRADE_BY_REALM: Record<number, TechniqueGrade> = {
  0: 'mortal', 1: 'mortal', 2: 'magical', 3: 'magical', 4: 'spirit',
  5: 'spirit', 6: 'spirit', 7: 'spirit', 8: 'spirit', 9: 'immortal',
  10: 'immortal', 11: 'immortal', 12: 'immortal', 13: 'dao', 14: 'dao', 15: 'dao', 16: 'dao', 17: 'dao',
};

// Name pools (xianxia-flavored; romanized)
export const SURNAMES = [
  'Wang', 'Li', 'Zhang', 'Liu', 'Chen', 'Su', 'Meng', 'Bai', 'Xu', 'Zhao',
  'Lin', 'Han', 'Mu', 'Qin', 'Ye', 'Shen', 'Gu', 'Yun', 'Feng', 'Xiao',
  'Lu', 'Zhou', 'Tang', 'Song', 'Yan', 'Cao', 'Mo', 'Luo', 'Duan', 'Xue',
];
export const GIVEN_M = [
  'Lin', 'Hao', 'Chen', 'Ming', 'Feng', 'Yun', 'Tian', 'Xiao', 'Jian', 'Yuan',
  'Qing', 'Xuan', 'Zhi', 'Heng', 'Bo', 'Ruo', 'Wu', 'Chong', 'Lie', 'An',
];
export const GIVEN_F = [
  'Wan\u2019er', 'Yue', 'Xin', 'Ling', 'Yao', 'Qing', 'Ruo', 'Xue', 'Meng', 'Yan',
  'Hui', 'Lan', 'Yun', 'Ning', 'Shuang', 'Yu', 'Ying', 'Jia', 'Mei', 'Ci',
];

export const SETTLEMENT_PREFIX = [
  'Mistveil', 'Verdant', 'Azure', 'Jade', 'Cloud', 'Spirit', 'Iron', 'Jade-Moon',
  'White-Bone', 'Canglan', 'Northern', 'Southern', 'Nine', 'Heavenly', 'Forgotten',
];
export const SETTLEMENT_SUFFIX = [
  'Village', 'Town', 'City', 'Fort', 'Market', 'Hamlet', 'Hold', 'Crossing',
];

export const SECT_PREFIX = [
  'Verdant Cloud', 'Black Flame', 'Heavenly Sword', 'Profound Yang', 'Jade-Void',
  'Spirit Stream', 'Blood Stream', 'Heaven Luo', 'Nine Heavens', 'Myriad',
];
export const SECT_SUFFIX = ['Sect', 'Clan', 'Pavilion', 'Hall', 'Academy', 'Palace'];

export const BEAST_SPECIES = [
  { species: 'Spirit Rabbit', tier: 'normal' as const, rank: 'mortal' as RealmId },
  { species: 'Spirit Fox', tier: 'spirit' as const, rank: 'qi_condensation' as RealmId },
  { species: 'Spirit Wolf', tier: 'spirit' as const, rank: 'foundation' as RealmId },
  { species: 'Iron-Beak Crane', tier: 'spirit' as const, rank: 'foundation' as RealmId },
  { species: 'Crimson Python', tier: 'spirit' as const, rank: 'core_formation' as RealmId },
  { species: 'Thunder Hawk', tier: 'spirit' as const, rank: 'core_formation' as RealmId },
  { species: 'Armored Bear', tier: 'spirit' as const, rank: 'core_formation' as RealmId },
  { species: 'Azure Dragon-Whelp', tier: 'demon' as const, rank: 'nascent_soul' as RealmId, bloodline: 'dragon' },
  { species: 'Nine-Tail Fox Matriarch', tier: 'demon' as const, rank: 'soul_formation' as RealmId, bloodline: 'fox-ancient' },
  { species: 'Kunpeng', tier: 'ancient' as const, rank: 'paragon' as RealmId, bloodline: 'kunpeng' },
];

export const PROFESSIONS = [
  'Herb Gatherer', 'Beast Hunter', 'Alchemist', 'Artifact Refiner', 'Formation Master',
  'Talisman Scribe', 'Spirit Farmer', 'Merchant', 'Innkeeper', 'Blacksmith',
  'Apothecary', 'Caravan Guard', 'Sect Disciple', 'Rogue Cultivator', 'Mortician',
];

export const RESOURCES = [
  'spirit stones', 'spirit herbs', 'beast cores', 'spirit ore', 'jade',
  'ironwood', 'frost-lotus', 'fire-grass', 'cloud-silk', 'spirit wine',
];

// Dao affinity labels for display
export const DAO_AFFINITY_LABEL = (v: number) =>
  v >= 0.8 ? 'Sovereign' : v >= 0.6 ? 'Resonant' : v >= 0.4 ? 'Tolerant' : v >= 0.2 ? 'Resistant' : 'Forbidden';
