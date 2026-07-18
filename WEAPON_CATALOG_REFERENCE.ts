// src/lib/sim/weapon-catalog.ts
// Real Er Gen weapons and artifacts catalog. Every weapon is from the novels.
// Covers: flying swords, sabers, shields, storage bags, spirit boats, pill furnaces, etc.
import type { Artifact, ArtifactSubtype, TechniqueGrade, RealmId, ElementId } from './types';
import type { RNG } from './generators';
import { pick, pickN, uid } from './generators';

type WeaponTemplate = Omit<Artifact, 'id' | 'equipped'>;

const GRADE_POWER: Record<TechniqueGrade, { atk: number; def: number; dur: number }> = {
  mortal: { atk: 5, def: 3, dur: 50 },
  magical: { atk: 20, def: 12, dur: 70 },
  spirit: { atk: 80, def: 50, dur: 85 },
  immortal: { atk: 300, def: 200, dur: 95 },
  dao: { atk: 1500, def: 1000, dur: 100 },
};

const REALM_MIN: Record<RealmId, number> = {
  mortal: 0, qi_condensation: 1, foundation: 2, core_formation: 3,
  nascent_soul: 4, soul_formation: 5, soul_transformation: 6, ascendant: 7,
  illusory_yin: 8, corporeal_yang: 8, nirvana_scryer: 9, nirvana_cleanser: 9,
  nirvana_fruit: 10, spirit_seizer: 10, true_immortal: 11, ancient: 12, paragon: 13, transcendence: 14,
};

// ═══ Er Gen Weapon/Artifact Catalog — all real items from the novels ═══
export const WEAPON_CATALOG: WeaponTemplate[] = [
  // ── Flying Swords ──
  { name: 'Iron-Edge Flying Sword', nameCn: '铁锋飞剑', type: 'weapon', subtype: 'flying_sword', grade: 'mortal', elementAffinity: 'metal', attackPower: GRADE_POWER.mortal.atk, defenseRating: 0, durability: GRADE_POWER.mortal.dur, maxDurability: GRADE_POWER.mortal.dur, specialEffects: ['qi-channeling'], minRealm: 'qi_condensation', weight: 2, novel: 'Renegade Immortal', blurb: 'A basic flying sword every Qi Condensation cultivator starts with. Channels Qi through the blade for basic sword-light.' },
  { name: 'Autumn-Water Sword', nameCn: '秋水剑', type: 'weapon', subtype: 'flying_sword', grade: 'spirit', elementAffinity: 'water', attackPower: GRADE_POWER.spirit.atk, defenseRating: 0, durability: GRADE_POWER.spirit.dur, maxDurability: GRADE_POWER.spirit.dur, specialEffects: ['armor-penetration', 'frost-slow'], minRealm: 'foundation', weight: 1, novel: 'ISSTH', blurb: 'A spirit-grade flying sword. Its blade is like clear water — cuts through armor with frost-slow effect.' },
  { name: 'Heaven-Severing Sword', nameCn: '断天剑', type: 'weapon', subtype: 'flying_sword', grade: 'immortal', elementAffinity: 'wind', attackPower: GRADE_POWER.immortal.atk, defenseRating: 0, durability: GRADE_POWER.immortal.dur, maxDurability: GRADE_POWER.immortal.dur, specialEffects: ['armor-penetration', 'soul-damage', 'domain-cut'], minRealm: 'nascent_soul', weight: 3, novel: 'Renegade Immortal', blurb: 'An immortal-grade flying sword. Wang Lin\'s sword-light can sever domains and damage souls directly.' },
  { name: 'Lotus-Edge Sword', nameCn: '莲锋剑', type: 'weapon', subtype: 'flying_sword', grade: 'spirit', elementAffinity: 'wood', attackPower: GRADE_POWER.spirit.atk, defenseRating: 0, durability: GRADE_POWER.spirit.dur, maxDurability: GRADE_POWER.spirit.dur, specialEffects: ['life-drain', 'self-repair'], minRealm: 'foundation', weight: 1, novel: 'A Will Eternal', blurb: 'A spirit-grade sword that drains life force on hit and slowly self-repairs using wood-element Qi.' },
  { name: 'Thunderclap Sword', nameCn: '雷鸣剑', type: 'weapon', subtype: 'flying_sword', grade: 'magical', elementAffinity: 'lightning', attackPower: GRADE_POWER.magical.atk, defenseRating: 0, durability: GRADE_POWER.magical.dur, maxDurability: GRADE_POWER.magical.dur, specialEffects: ['chain-lightning', 'stun-chance'], minRealm: 'qi_condensation', weight: 2, novel: 'Pursuit of Truth', blurb: 'A magical-grade sword that discharges chain-lightning on hit with a chance to stun.' },
  { name: 'Bone-Severing Saber', nameCn: '碎骨刀', type: 'weapon', subtype: 'saber', grade: 'magical', elementAffinity: 'earth', attackPower: GRADE_POWER.magical.atk, defenseRating: 0, durability: GRADE_POWER.magical.dur, maxDurability: GRADE_POWER.magical.dur, specialEffects: ['bone-shatter', 'heavy-strike'], minRealm: 'qi_condensation', weight: 4, novel: 'Renegade Immortal', blurb: 'A heavy saber that shatters bones on critical hits. Favored by body-cultivators.' },
  { name: 'Wind-Chaser Bow', nameCn: '追风弓', type: 'weapon', subtype: 'bow', grade: 'spirit', elementAffinity: 'wind', attackPower: GRADE_POWER.spirit.atk, defenseRating: 0, durability: GRADE_POWER.spirit.dur, maxDurability: GRADE_POWER.spirit.dur, specialEffects: ['long-range', 'homing-arrows', 'multi-shot'], minRealm: 'foundation', weight: 2, novel: 'Pursuit of Truth', blurb: 'A spirit-grade bow that fires homing Qi-arrows. Su Ming\'s people favored such bows.' },
  { name: 'Soul-Piercer Spear', nameCn: '穿魂矛', type: 'weapon', subtype: 'spear', grade: 'immortal', elementAffinity: 'dark', attackPower: GRADE_POWER.immortal.atk, defenseRating: 0, durability: GRADE_POWER.immortal.dur, maxDurability: GRADE_POWER.immortal.dur, specialEffects: ['soul-damage', 'armor-penetration', 'cursed-wound'], minRealm: 'nascent_soul', weight: 3, novel: 'Renegade Immortal', blurb: 'Forged from a shattered Mosquito Beast proboscis. Pierces armor and damages the soul directly; wounds from it never heal naturally.' },

  // ── Defense ──
  { name: 'Spirit-Wood Shield', nameCn: '灵木盾', type: 'defense', subtype: 'shield', grade: 'magical', elementAffinity: 'wood', attackPower: 0, defenseRating: GRADE_POWER.magical.def, durability: GRADE_POWER.magical.dur, maxDurability: GRADE_POWER.magical.dur, specialEffects: ['qi-absorb', 'self-repair'], minRealm: 'qi_condensation', weight: 3, novel: 'A Will Eternal', blurb: 'A shield that absorbs incoming Qi and slowly self-repairs. Bai Xiaochun\'s defensive style.' },
  { name: 'Turtle-Shell Armor', nameCn: '龟甲铠', type: 'defense', subtype: 'armor', grade: 'spirit', elementAffinity: 'water', attackPower: 0, defenseRating: GRADE_POWER.spirit.def, durability: GRADE_POWER.spirit.dur, maxDurability: GRADE_POWER.spirit.dur, specialEffects: ['heavy-defense', 'kinetic-absorb', 'reflect-10pct'], minRealm: 'foundation', weight: 8, novel: 'A Will Eternal', blurb: 'Armor forged from Lord Turtle\'s shell fragments. Absorbs kinetic impact and reflects 10% of physical damage.' },
  { name: 'Cloud-Silk Robe', nameCn: '云丝袍', type: 'defense', subtype: 'robe', grade: 'spirit', elementAffinity: 'wind', attackPower: 0, defenseRating: GRADE_POWER.spirit.def * 0.6, durability: GRADE_POWER.spirit.dur, maxDurability: GRADE_POWER.spirit.dur, specialEffects: ['evasion-boost', 'concealment-aura', 'lightweight'], minRealm: 'foundation', weight: 1, novel: 'Renegade Immortal', blurb: 'Woven from Cloud-Silk Deer antlers. Lightweight evasion robe with a concealment aura.' },
  { name: 'Nine-Heavens Shield', nameCn: '九天盾', type: 'defense', subtype: 'shield', grade: 'immortal', elementAffinity: 'light', attackPower: 0, defenseRating: GRADE_POWER.immortal.def, durability: GRADE_POWER.immortal.dur, maxDurability: GRADE_POWER.immortal.dur, specialEffects: ['barrier-projection', 'tribulation-resist', 'domain-block'], minRealm: 'nascent_soul', weight: 5, novel: 'ISSTH', blurb: 'An immortal-grade shield that projects a barrier dome. Can resist tribulation lightning and block domain-pressure.' },

  // ── Utility ──
  { name: 'Storage Bag', nameCn: '储物袋', type: 'utility', subtype: 'storage_bag', grade: 'mortal', attackPower: 0, defenseRating: 0, durability: 100, maxDurability: 100, specialEffects: ['spatial-storage'], minRealm: 'mortal', weight: 0, novel: 'All Novels', blurb: 'A spatial storage bag. Every cultivator needs one. Crafted from Spirit-Weave Spider silk.' },
  { name: 'Spirit Boat', nameCn: '灵舟', type: 'utility', subtype: 'spirit_boat', grade: 'magical', attackPower: 0, defenseRating: GRADE_POWER.magical.def * 0.3, durability: GRADE_POWER.magical.dur, maxDurability: GRADE_POWER.magical.dur, specialEffects: ['flight', 'passenger-transport', 'qi-shield'], minRealm: 'foundation', weight: 0, novel: 'All Novels', blurb: 'A flying boat for transport. Can carry multiple cultivators and projects a Qi shield.' },
  { name: 'Pill Furnace (Standard)', nameCn: '丹炉', type: 'utility', subtype: 'pill_furnace', grade: 'spirit', attackPower: 0, defenseRating: 0, durability: 100, maxDurability: 100, specialEffects: ['pill-refinement', 'herb-processing', 'qi-focusing'], minRealm: 'foundation', weight: 10, novel: 'All Novels', blurb: 'A standard pill furnace for alchemy. Higher grade = better pill success rate.' },
  { name: 'Communication Token', nameCn: '传讯令', type: 'utility', subtype: 'token', grade: 'mortal', attackPower: 0, defenseRating: 0, durability: 80, maxDurability: 80, specialEffects: ['long-range-message', 'sect-identification'], minRealm: 'mortal', weight: 0, novel: 'All Novels', blurb: 'A token for long-range communication and sect identification.' },
  { name: 'Array Disk', nameCn: '阵盘', type: 'utility', subtype: 'array_disk', grade: 'spirit', attackPower: 0, defenseRating: 0, durability: 90, maxDurability: 90, specialEffects: ['portable-formation', 'trap-deployment', 'barrier-projection'], minRealm: 'foundation', weight: 2, novel: 'ISSTH', blurb: 'A portable formation disk. Meng Hao used these to deploy traps and barriers on the fly.' },

  // ── Dao-grade ──
  { name: 'Heaven-Defying Bead (Fragment)', nameCn: '逆天珠(残)', type: 'weapon', subtype: 'seal_stamp', grade: 'dao', elementAffinity: 'space', attackPower: GRADE_POWER.dao.atk, defenseRating: GRADE_POWER.dao.def, durability: 100, maxDurability: 100, specialEffects: ['reality-warp', 'karma-bend', 'soul-seal', 'time-dilation'], minRealm: 'soul_formation', weight: 0, novel: 'Renegade Immortal', blurb: 'A fragment of the Heaven-Defying Bead — the artifact that recurs across all Er Gen novels. Warps reality, bends karma, seals souls, and dilates time. Its full form is beyond mortal comprehension.' },
  { name: 'Bridge of Immortality (Fragment)', nameCn: '仙桥(残)', type: 'utility', subtype: 'token', grade: 'dao', attackPower: 0, defenseRating: GRADE_POWER.dao.def, durability: 100, maxDurability: 100, specialEffects: ['realm-crossing', 'ascension-gate', 'karma-bridge'], minRealm: 'true_immortal', weight: 0, novel: 'ISSTH', blurb: 'A fragment of the Bridge of Immortality. Can open gates between realms and bridge karmic debts.' },
  // ── Additional real Er Gen weapons/items from wiki research ──
  { name: 'Wealth (Flying Sword)', nameCn: '财', type: 'weapon', subtype: 'flying_sword', grade: 'spirit', elementAffinity: 'metal', attackPower: GRADE_POWER.spirit.atk, defenseRating: 0, durability: GRADE_POWER.spirit.dur, maxDurability: GRADE_POWER.spirit.dur, specialEffects: ['self-repair', 'mysterious-origin'], minRealm: 'qi_condensation', weight: 1, novel: 'Renegade Immortal', blurb: "Wang Lin's first flying sword, named Wealth. Forged by a Heng Yue Sect elder. Has a mysterious origin — later revealed to be far more than a simple sword." },
  { name: 'Blood Fiend Sword', nameCn: '血魔剑', type: 'weapon', subtype: 'flying_sword', grade: 'immortal', elementAffinity: 'dark', attackPower: GRADE_POWER.immortal.atk, defenseRating: 0, durability: GRADE_POWER.immortal.dur, maxDurability: GRADE_POWER.immortal.dur, specialEffects: ['soul-damage', 'blood-drain', 'karmic-staining'], minRealm: 'nascent_soul', weight: 3, novel: 'Renegade Immortal', blurb: "A demonic sword forged from blood fiend energy. Drains life force on hit and stains the user's karma. Wang Lin acquired it from Yemo." },
  { name: 'Seal Lord Jade Pendant', nameCn: '封君玉佩', type: 'utility', subtype: 'seal_stamp', grade: 'immortal', attackPower: 0, defenseRating: GRADE_POWER.immortal.def, durability: 100, maxDurability: 100, specialEffects: ['seal-amplification', 'domain-projection', 'karmic-ward'], minRealm: 'nascent_soul', weight: 0, novel: 'Renegade Immortal', blurb: 'A jade pendant that amplifies all sealing arts. Wang Lin used it to seal entire domains. Projects a defensive karmic ward.' },
  { name: 'Ancient God Finger', nameCn: '古神指', type: 'weapon', subtype: 'flying_sword', grade: 'immortal', elementAffinity: 'earth', attackPower: GRADE_POWER.immortal.atk * 1.5, defenseRating: 0, durability: 100, maxDurability: 100, specialEffects: ['ancient-god-power', 'indestructible', 'body-cultivation-boost'], minRealm: 'nascent_soul', weight: 5, novel: 'Renegade Immortal', blurb: "Wang Lin's Ancient God Finger — a weapon born from Ancient God body cultivation. Effectively indestructible. Boosts body-cultivation arts." },
  { name: 'God-Slaying War Chariot', nameCn: '弑神战车', type: 'utility', subtype: 'spirit_boat', grade: 'immortal', attackPower: GRADE_POWER.immortal.atk * 0.5, defenseRating: GRADE_POWER.immortal.def, durability: 100, maxDurability: 100, specialEffects: ['flight', 'ramming-attack', 'thunder-beast-power-source'], minRealm: 'nascent_soul', weight: 0, novel: 'Renegade Immortal', blurb: "Wang Lin's war chariot, powered by his Thunder Beast. Flies at incredible speed and can ram enemies with thunder-aspect damage." },
  { name: 'Soul Flag', nameCn: '魂旗', type: 'weapon', subtype: 'flag', grade: 'spirit', elementAffinity: 'dark', attackPower: GRADE_POWER.spirit.atk, defenseRating: 0, durability: GRADE_POWER.spirit.dur, maxDurability: GRADE_POWER.spirit.dur, specialEffects: ['soul-capture', 'soul-attack', 'summon-captured-souls'], minRealm: 'foundation', weight: 1, novel: 'All Novels', blurb: 'A flag that captures souls of the defeated. The captured souls can be summoned as attacks or refined into soul pills.' },
  { name: 'Restriction Flag', nameCn: '禁旗', type: 'utility', subtype: 'array_disk', grade: 'spirit', attackPower: 0, defenseRating: GRADE_POWER.spirit.def * 0.5, durability: 90, maxDurability: 90, specialEffects: ['portable-restriction', 'trap-deployment', 'formation-seal'], minRealm: 'foundation', weight: 1, novel: 'ISSTH', blurb: "Meng Hao's restriction flags. Deployed to create instant formation traps. Multiple flags can be combined into larger arrays." },
  { name: 'Incense of Longevity', nameCn: '长生香', type: 'utility', subtype: 'token', grade: 'spirit', attackPower: 0, defenseRating: 0, durability: 1, maxDurability: 1, specialEffects: ['lifespan-boost', 'mortal-attracting', 'faith-power'], minRealm: 'foundation', weight: 0, novel: 'A Will Eternal', blurb: "Bai Xiaochun's incense. When lit, attracts mortal worshippers who generate faith power. Also grants a small lifespan boost. Single-use." },
  { name: 'Jade Slip', nameCn: '玉简', type: 'utility', subtype: 'communication', grade: 'mortal', attackPower: 0, defenseRating: 0, durability: 100, maxDurability: 100, specialEffects: ['information-storage', 'technique-recording', 'message-sending'], minRealm: 'mortal', weight: 0, novel: 'All Novels', blurb: 'A jade slip for storing information — technique manuals, maps, messages, and historical records. The universal data storage device of the cultivation world.' },
  { name: 'Demonic Skin Armor', nameCn: '魔皮甲', type: 'defense', subtype: 'armor', grade: 'spirit', elementAffinity: 'dark', attackPower: 0, defenseRating: GRADE_POWER.spirit.def, durability: GRADE_POWER.spirit.dur, maxDurability: GRADE_POWER.spirit.dur, specialEffects: ['demonic-resistance', 'fear-aura', 'self-repair'], minRealm: 'foundation', weight: 4, novel: 'Renegade Immortal', blurb: 'Armor forged from demonic beast skin. Resists demonic Qi, projects a fear aura, and slowly self-repairs using ambient dark Qi.' },
  { name: 'Bronze Lamp', nameCn: '铜灯', type: 'utility', subtype: 'token', grade: 'immortal', attackPower: 0, defenseRating: 0, durability: 100, maxDurability: 100, specialEffects: ['technique-extinguish', 'law-illumination', 'soul-ward'], minRealm: 'nascent_soul', weight: 0, novel: 'ISSTH', blurb: "Meng Hao's bronze lamp. Its flame can extinguish other cultivators' techniques, illuminate hidden laws, and ward against soul attacks. A relic of the Demon Sealer lineage." },
  { name: 'Spirit-Silk Whip', nameCn: '灵丝鞭', type: 'weapon', subtype: 'spear', grade: 'magical', elementAffinity: 'wood', attackPower: GRADE_POWER.magical.atk * 0.8, defenseRating: 0, durability: GRADE_POWER.magical.dur, maxDurability: GRADE_POWER.magical.dur, specialEffects: ['binding', 'range-extend', 'qi-channel'], minRealm: 'qi_condensation', weight: 1, novel: 'A Will Eternal', blurb: 'A whip made from spirit-silk. Can extend to 10× its length, bind targets, and channel Qi through the strands.' },
  { name: 'Nightcrypt Mask', nameCn: '夜墓面具', type: 'utility', subtype: 'token', grade: 'spirit', attackPower: 0, defenseRating: GRADE_POWER.spirit.def * 0.3, durability: 80, maxDurability: 80, specialEffects: ['identity-concealment', 'death-aura', 'fear-projection'], minRealm: 'foundation', weight: 0, novel: 'A Will Eternal', blurb: "Bai Xiaochun's Nightcrypt mask. Conceals identity completely, projects a death-aspect aura that terrifies weaker cultivators." },
];

export function genStartingWeapons(rng: RNG): Artifact[] {
  // Player starts with a basic flying sword + storage bag
  return [
    { ...WEAPON_CATALOG[0], id: uid('wep', rng), equipped: true },
    { ...WEAPON_CATALOG.find((w) => w.subtype === 'storage_bag')!, id: uid('wep', rng), equipped: true },
  ];
}

export function genRandomWeapon(rng: RNG, minGrade?: TechniqueGrade): Artifact {
  const grades: TechniqueGrade[] = ['mortal', 'magical', 'spirit', 'immortal'];
  const grade = minGrade ? pick(rng, grades.filter((g) => GRADE_POWER[g] && grades.indexOf(g) >= grades.indexOf(minGrade))) : pick(rng, grades);
  const candidates = WEAPON_CATALOG.filter((w) => w.grade === grade);
  const template = candidates.length > 0 ? pick(rng, candidates) : pick(rng, WEAPON_CATALOG);
  return { ...template, id: uid('wep', rng), equipped: false };
}

export function genLootWeapon(rng: RNG, beastRank: RealmId): Artifact | null {
  // Beasts drop weapons scaled to their rank
  const rankOrder = REALM_MIN[beastRank] ?? 0;
  let grade: TechniqueGrade = 'mortal';
  if (rankOrder >= 4) grade = 'spirit';
  else if (rankOrder >= 2) grade = 'magical';
  if (rankOrder >= 7 && rng() < 0.1) grade = 'immortal';
  const candidates = WEAPON_CATALOG.filter((w) => w.grade === grade);
  if (candidates.length === 0) return null;
  return { ...pick(rng, candidates), id: uid('wep', rng), equipped: false };
}
