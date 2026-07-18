// src/lib/sim/ergen-depth.ts
// The missing depth systems from the Er Gen multiverse. These are the mechanics
// that separate a surface-level xianxia game from a faithful Er Gen simulation.
// Built from deep novel knowledge — not stopping short.

import type { RealmId, DaoId } from './types';
import type { RNG } from './generators';
import { pick, randInt, uid } from './generators';
import { realmOrder } from './realms';

// ═══ 1. KARMIC THREADS (因果线) ═════════════════════════════════════
// At Nascent Soul+, cultivators can perceive karmic threads — the invisible bonds
// of cause-and-effect that connect people across lifetimes. Wang Lin's entire
// journey is shaped by karma: his family's death, his revenge, his love for Li Muwan.
export interface KarmicThread {
  id: string;
  targetName: string;         // who the thread connects to
  targetId?: string;          // NPC id if applicable
  type: 'life_debt' | 'blood_vendetta' | 'love' | 'gratitude' | 'betrayal' | 'karmic_enemy' | 'dao_bond';
  strength: number;           // 0..1 — how deeply bound
  origin: string;             // what caused the thread
  year: number;               // when it formed
  lifetime: number;            // how many reincarnations it persists (1 = this life, -1 = permanent)
  visible: boolean;           // can the player perceive it (needs Nascent Soul+)
  // ── Karma Vision / Inheritance tracing (Tribulation Parasitism + Karma Vision systems) ──
  lineage?: 'bloodline' | 'debt' | 'enmity' | 'grace' | 'inheritance' | 'dao_companion' | 'oath'; // colored thread category
  leadsToInheritanceId?: string; // a hidden inheritance cave this thread leads to (requires tracing)
  bloodlineMemoryExtracted?: boolean; // has the player soul-searched the source NPC for the bloodline memory
  traced?: boolean;              // has the player followed this thread to its terminus
}

// The seven colored thread categories visible in Karma Vision. Each reveals different content.
export const KARMA_LINEAGE_META: Record<NonNullable<KarmicThread['lineage']>, { color: string; label: string; hint: string }> = {
  bloodline:      { color: 'crimson',   label: 'Bloodline',      hint: 'A genetic thread to an ancestor. Following it may reveal a hidden inheritance vault.' },
  debt:           { color: 'gold',      label: 'Karmic Debt',    hint: 'An unpaid obligation. Tracing leads to the creditor — or their surviving descendants.' },
  enmity:         { color: 'black',     label: 'Enmity',         hint: 'A grudge-cord. Following it exposes your enemy\'s hidden weakness or stash.' },
  grace:          { color: 'white',     label: 'Grace',          hint: 'A gift un-repaid. Tracing leads to someone who owes you — or you owe.' },
  inheritance:    { color: 'violet',    label: 'Inheritance',    hint: 'A direct cord to a sealed legacy. The strongest thread-type for treasure-hunting.' },
  dao_companion:  { color: 'jade',      label: 'Dao Companion',  hint: 'A cultivation-bond. Tracing reveals your companion\'s true realm or hidden crisis.' },
  oath:           { color: 'silver',    label: 'Oath',           hint: 'A sworn vow binding two souls. Severing it has world-level consequences.' },
};

export function canPerceiveKarma(realm: RealmId): boolean {
  return realmOrder(realm) >= 4; // Nascent Soul+
}

export function formKarmicThread(rng: RNG, targetName: string, type: KarmicThread['type'], strength: number, origin: string, year: number): KarmicThread {
  return {
    id: uid('karma', rng), targetName, type, strength: Math.max(0, Math.min(1, strength)),
    origin, year, lifetime: type === 'blood_vendetta' || type === 'love' ? -1 : 3, visible: false,
  };
}

// ═══ 2. ANCIENT CLANS / BODY CULTIVATION (古族) ═════════════════════
// The Ancient God/Devil/Demon path is a PARALLEL cultivation route to Qi cultivation.
// Wang Lin becomes a hybrid. Body cultivators kindle "stars" inside their body.
// 7 stars per clan → merge all 3 clans → 27 stars = peak Ancient God.
export type AncientClanType = 'god' | 'devil' | 'demon';

export interface AncientClanState {
  godStars: number;           // 0..7 — Ancient God stars kindled
  devilStars: number;         // 0..7 — Ancient Devil stars
  demonStars: number;         // 0..7 — Ancient Demon stars
  merged: boolean;            // have all 3 been merged into 27?
  totalStars: number;         // god + devil + demon (max 21 pre-merge, 27 post-merge)
  bodyCultivationRealm: RealmId; // parallel to Qi cultivation — body can be stronger/weaker
}

export function emptyAncientClanState(): AncientClanState {
  return { godStars: 0, devilStars: 0, demonStars: 0, merged: false, totalStars: 0, bodyCultivationRealm: 'mortal' };
}

export function kindleStar(state: AncientClanState, clan: AncientClanType): { ok: boolean; message: string } {
  const max = 7;
  if (clan === 'god' && state.godStars >= max) return { ok: false, message: 'Already at 7 Ancient God stars.' };
  if (clan === 'devil' && state.devilStars >= max) return { ok: false, message: 'Already at 7 Ancient Devil stars.' };
  if (clan === 'demon' && state.demonStars >= max) return { ok: false, message: 'Already at 7 Ancient Demon stars.' };
  if (clan === 'god') state.godStars++;
  if (clan === 'devil') state.devilStars++;
  if (clan === 'demon') state.demonStars++;
  state.totalStars = state.godStars + state.devilStars + state.demonStars;
  // Check for 27-star merge (all three clans at 7 each → merge into 27)
  if (state.godStars === 7 && state.devilStars === 7 && state.demonStars === 7 && !state.merged) {
    state.merged = true;
    state.totalStars = 27;
    return { ok: true, message: 'ANCIENT GOD MERGENCE! All three clans fused into 27 stars. You are now a peak Ancient God hybrid — like Wang Lin.' };
  }
  return { ok: true, message: `Kindled a ${clan} star. Total: ${state.totalStars} stars.` };
}

// ═══ 3. DAO HEART (道心) ═══════════════════════════════════════════
// The cultivator's fundamental resolve. Different from daoHeartDelta (which is
// about ally alignment). Dao Heart is the player's own unshakeable will.
// "If your Dao Heart is unshakeable, the heavens cannot stop you."
export interface DaoHeartState {
  strength: number;           // 0..100 — the power of the Dao Heart
  tested: number;             // how many times it's been tested
  shaken: boolean;            // is it currently shaken (from a failed tribulation / loss)?
    shakeRecoveryYear?: number; // when the shake will heal
  breakthroughs_from_heart: number; // breakthroughs triggered by emotional extremes
}

export function genDaoHeart(): DaoHeartState {
  return { strength: 30 + Math.random() * 20, tested: 0, shaken: false, breakthroughs_from_heart: 0 };
}

export function testDaoHeart(state: DaoHeartState, difficulty: number, rng: RNG): { passed: boolean; message: string } {
  state.tested++;
  if (state.shaken) {
    return { passed: false, message: 'Your Dao Heart is shaken — you cannot face another test until it heals.' };
  }
  const passed = state.strength / 100 > difficulty * (0.5 + rng() * 0.5);
  if (passed) {
    state.strength = Math.min(100, state.strength + 5);
    return { passed: true, message: 'Your Dao Heart held firm. (+5 Dao Heart strength)' };
  }
  state.shaken = true;
  state.strength = Math.max(10, state.strength - 10);
  return { passed: false, message: 'Your Dao Heart was shaken! -10 strength. You must recover before the next test.' };
}

export function checkEmotionalBreakthrough(state: DaoHeartState, emotion: 'grief' | 'love' | 'rage' | 'enlightenment' | 'despair', rng: RNG): { triggered: boolean; message: string } {
  // Er Gen's signature: breakthroughs triggered by emotional extremes
  // Wang Lin broke through from grief at his family's death.
  const chance = emotion === 'grief' ? 0.3 : emotion === 'enlightenment' ? 0.4 : emotion === 'rage' ? 0.2 : 0.15;
  if (rng() < chance) {
    state.breakthroughs_from_heart++;
    state.strength = Math.min(100, state.strength + 15);
    return { triggered: true, message: `EMOTIONAL BREAKTHROUGH! Your ${emotion} shook the heavens. The Dao Heart resonates with your ${emotion}. (+15 Dao Heart)` };
  }
  return { triggered: false, message: '' };
}

// ═══ 4. CULTIVATION ARTS (功法) ═══════════════════════════════════
// A Cultivation Art is the foundational method — distinct from techniques (术法).
// Arts determine cultivation speed, Qi capacity multiplier, and the ceiling.
// e.g., the Undying Live Forever Codex (Bai Xiaochun), the Slaughter Dao (Wang Lin).
export interface CultivationArt {
  id: string;
  name: string;
  nameCn?: string;
  grade: 'mortal' | 'spirit' | 'earth' | 'heaven' | 'dao';
  qiMultiplier: number;       // multiplier on Qi gathering speed
  realmCeiling: RealmId;      // max realm this art can take you
  elementAffinity?: DaoId;    // what dao it aligns with
  specialEffect: string;      // e.g., "undying body", "soul-eating", "seal amplification"
  novel?: string;
  blurb: string;
}

export const CULTIVATION_ARTS: CultivationArt[] = [
  { id: 'art_qi_gathering', name: 'Basic Qi-Gathering Art', nameCn: '聚气诀', grade: 'mortal', qiMultiplier: 1.0, realmCeiling: 'foundation', specialEffect: 'none', novel: 'All Novels', blurb: 'The most basic cultivation art. Every cultivator starts here. Ceiling: Foundation Establishment.' },
  { id: 'art_undying', name: 'Undying Live Forever Codex', nameCn: '不死长生诀', grade: 'heaven', qiMultiplier: 2.5, realmCeiling: 'transcendence', elementAffinity: 'life', specialEffect: 'undying body — extreme vitality, near-impossible to kill; fear of death drives the cultivator', novel: 'A Will Eternal', blurb: "Bai Xiaochun's signature art. Pursues eternal life above all. Grants an undying body but the cultivator becomes obsessed with not dying." },
  { id: 'art_slaughter', name: 'Slaughter Dao Art', nameCn: '杀道', grade: 'dao', qiMultiplier: 3.0, realmCeiling: 'transcendence', elementAffinity: 'slaughter', specialEffect: 'killing intent — each kill strengthens the art; soul-eating capability', novel: 'Renegade Immortal', blurb: "Wang Lin's self-created Dao. Killing as a path to truth. Each kill grants understanding. The more you kill, the stronger you become." },
  { id: 'art_seal', name: 'Seal-Heavens Art', nameCn: '封天诀', grade: 'dao', qiMultiplier: 3.0, realmCeiling: 'transcendence', elementAffinity: 'seal', specialEffect: 'seal amplification — all sealing techniques are 200% more powerful; can seal anything', novel: 'ISSTH', blurb: "Meng Hao's Dao. The Seal —封. Can seal the heavens themselves. All sealing/restriction arts are amplified." },
  { id: 'art_one_thought', name: 'One-Thought Eternal Art', nameCn: '一念永恒诀', grade: 'heaven', qiMultiplier: 2.0, realmCeiling: 'ancient', elementAffinity: 'life', specialEffect: 'one-thought manifestation — will can briefly alter reality', novel: 'A Will Eternal', blurb: 'A variant of the Undying Codex focused on will-manifestation. One thought can change reality — briefly.' },
  { id: 'art_ancient_god', name: 'Ancient God Body Refinement', nameCn: '古神体炼', grade: 'heaven', qiMultiplier: 1.5, realmCeiling: 'ancient', specialEffect: 'ancient god stars — kindles body-cultivation stars; parallel to Qi cultivation', novel: 'Renegade Immortal', blurb: "The Ancient God body-cultivation path. Kindles stars inside the body. A parallel path — body and Qi can both grow independently." },
  { id: 'art_berserker', name: 'Berserker Bloodline Art', nameCn: '蛮血诀', grade: 'spirit', qiMultiplier: 1.8, realmCeiling: 'soul_formation', elementAffinity: 'body', specialEffect: 'bloodline surge — at low HP, +100% power but -50% control', novel: 'Pursuit of Truth', blurb: "Su Ming's people's art. Activates berserker bloodline under stress. Powerful but reckless." },
  { id: 'art_spirit_stream', name: 'Spirit Stream Sect Foundation Art', nameCn: '灵溪宗基础功法', grade: 'spirit', qiMultiplier: 1.5, realmCeiling: 'core_formation', elementAffinity: 'wood', specialEffect: 'wood-spirit affinity — +20% wood dao speed', novel: 'A Will Eternal', blurb: 'The foundational art of the Spirit Stream Sect. Balanced and reliable.' },
  { id: 'art_heaven_defying', name: 'Heaven-Defying Art', nameCn: '逆天诀', grade: 'dao', qiMultiplier: 4.0, realmCeiling: 'transcendence', specialEffect: 'heaven-defiance — tribulations are 50% weaker; can defy world-law at a cost', novel: 'Renegade Immortal', blurb: "The art of the Heaven-Defying Bead's bearer. Reduces tribulation intensity by 50% and allows temporary world-law defiance. The ultimate art for those who walk against heaven." },
  // Additional real Er Gen cultivation arts
  { id: 'art_immortal_guard', name: 'Immortal Guard Art', nameCn: '仙卫诀', grade: 'spirit', qiMultiplier: 1.6, realmCeiling: 'soul_formation', elementAffinity: 'body', specialEffect: 'immortal guard — forms a Qi-armor that absorbs damage proportional to Qi spent', novel: 'Renegade Immortal', blurb: "Wang Lin's defensive art. Forms a Qi-armor (Immortal Guard) that absorbs damage. The more Qi invested, the stronger the defense." },
  { id: 'art_origin_soul', name: 'Origin Soul Art', nameCn: '本源魂诀', grade: 'heaven', qiMultiplier: 2.2, realmCeiling: 'ancient', elementAffinity: 'divine_sense', specialEffect: 'origin soul — strengthens the origin soul; allows origin-soul attacks at Nascent Soul+', novel: 'Renegade Immortal', blurb: "Wang Lin's soul cultivation art. Strengthens the origin soul beyond normal Nascent Soul cultivators. Enables origin-soul attacks that bypass physical defense." },
  { id: 'art_god_slaying_chariot', name: 'God-Slaying Chariot Art', nameCn: '弑神车诀', grade: 'heaven', qiMultiplier: 1.8, realmCeiling: 'ancient', specialEffect: 'chariot summoning — can summon the God-Slaying War Chariot (requires Thunder Beast power source)', novel: 'Renegade Immortal', blurb: "The art of the God-Slaying War Chariot. Requires a Thunder Beast as a power source. The chariot can ram through formations and enemy lines." },
  { id: 'art_ji_realm', name: 'Ji Realm Art', nameCn: '寂灭境', grade: 'dao', qiMultiplier: 3.5, realmCeiling: 'transcendence', elementAffinity: 'slaughter', specialEffect: 'ji realm — creates a zone of absolute silence where no Qi can flow except the user\'s', novel: 'Renegade Immortal', blurb: "Wang Lin's Ji Realm — a domain of absolute silence. Inside it, no one can circulate Qi except Wang Lin himself. The ultimate suppression domain." },
  { id: 'art_demon_sealer', name: 'Demon Sealer Art', nameCn: '封魔诀', grade: 'dao', qiMultiplier: 2.8, realmCeiling: 'transcendence', elementAffinity: 'seal', specialEffect: 'demon sealing — all sealing techniques are 300% more powerful; can seal anything including heavenly tribulation', novel: 'ISSTH', blurb: "The art of the League of Demon Sealers, passed down through Meng Hao. Amplifies all sealing techniques to an extreme degree. Can seal even heavenly tribulation lightning." },
  { id: 'art_blood_stream', name: 'Blood Stream Secret Art', nameCn: '血溪秘法', grade: 'spirit', qiMultiplier: 1.7, realmCeiling: 'core_formation', elementAffinity: 'body', specialEffect: 'blood manipulation — can control own blood as a weapon; blood-aspect attacks', novel: 'A Will Eternal', blurb: "The secret art of the Blood Stream Sect. Allows blood manipulation — the cultivator's own blood becomes a weapon. Can form blood-swords, blood-shields, and blood-pills." },
  { id: 'art_starry_sky', name: 'Starry Sky Dao Polarity Art', nameCn: '星空道极诀', grade: 'heaven', qiMultiplier: 2.3, realmCeiling: 'ancient', elementAffinity: 'space', specialEffect: 'starry sky — can absorb starlight for Qi; +50% power at night under starlight', novel: 'A Will Eternal', blurb: "The art of the Starry Sky Dao Polarity Sect. Absorbs starlight as Qi. The cultivator grows stronger under the night sky. A top-tier cosmic art." },
  { id: 'art_undying_live_forever', name: 'Undying Live Forever Codex (Complete)', nameCn: '不死长生诀(全)', grade: 'dao', qiMultiplier: 3.5, realmCeiling: 'transcendence', elementAffinity: 'life', specialEffect: 'undying — the cultivator cannot be permanently killed; will always reform from a drop of blood given enough time', novel: 'A Will Eternal', blurb: "The COMPLETE Undying Live Forever Codex — both the Undying and the Live Forever halves combined. The cultivator is effectively unkillable. Bai Xiaochun's ultimate art." },
];

export function genStartingArt(rng: RNG): CultivationArt {
  return CULTIVATION_ARTS[0]; // Basic Qi-Gathering Art
}

// ═══ 5. PILL TRIBULATION (丹劫) ═══════════════════════════════════
// High-grade pills attract heavenly tribulation. The pill must survive
// lightning strikes to stabilize. The alchemist must protect their furnace.
export interface PillTribulation {
  lightningStrikes: number;   // how many strikes the pill must survive
  pillGrade: string;
  survived: boolean;
  text: string;
}

export function checkPillTribulation(rng: RNG, pillGrade: string, alchemySkill: number): PillTribulation | null {
  if (pillGrade === 'mortal' || pillGrade === 'failed') return null;
  const strikes = pillGrade === 'dao' ? 9 : pillGrade === 'heaven' ? 7 : pillGrade === 'earth' ? 5 : 3;
  // The alchemist's skill helps the pill survive
  let survived = true;
  for (let i = 0; i < strikes; i++) {
    if (rng() > 0.5 + alchemySkill * 0.3) { survived = false; break; }
  }
  return {
    lightningStrikes: strikes,
    pillGrade,
    survived,
    text: survived
      ? `PILL TRIBULATION! ${strikes} lightning strikes descended. The ${pillGrade}-grade pill survived and stabilized!`
      : `PILL TRIBULATION FAILED! The ${pillGrade}-grade pill was destroyed by lightning on strike ${strikes}. Your furnace is damaged.`,
  };
}

// ═══ 6. SEALING SYSTEM (封印/禁制) ════════════════════════════════
// Meng Hao's entire Dao is the Seal. Sealing is a major combat/utility system.
export type SealTarget = 'beast' | 'npc' | 'technique' | 'array' | 'space' | 'time';

export interface SealResult {
  ok: boolean;
  message: string;
  sealedName?: string;
  sealedType?: SealTarget;
  duration?: number; // years the seal holds
}

export function attemptSeal(rng: RNG, target: SealTarget, targetName: string, targetRealm: RealmId, playerRealm: RealmId, sealArtBonus: number): SealResult {
  // Sealing requires the player to be at least equal realm to the target
  if (realmOrder(playerRealm) < realmOrder(targetRealm)) {
    return { ok: false, message: `Cannot seal ${targetName} — their realm exceeds yours. Sealing requires equal or greater cultivation.` };
  }
  const baseChance = 0.3 + (realmOrder(playerRealm) - realmOrder(targetRealm)) * 0.15 + sealArtBonus;
  const chance = Math.max(0.1, Math.min(0.85, baseChance));
  if (rng() > chance) {
    return { ok: false, message: `The seal failed — ${targetName} broke free. Sealing arts require more practice.` };
  }
  const duration = randInt(rng, 10, 100) + sealArtBonus * 50;
  return { ok: true, message: `Sealed ${targetName}! The seal will hold for ~${duration} years.`, sealedName: targetName, sealedType: target, duration };
}

// ═══ 7. SOUL SPLITTING / AVATARS (分身) ═══════════════════════════
// At Nascent Soul+, the soul becomes independent and can be split into fragments.
// Each fragment can become an avatar that cultivates independently.
export interface AvatarState {
  id: string;
  name: string;
  realm: RealmId;              // the avatar's own cultivation realm (independent)
  qi: number;
  location: string;
  task: string;                // what the avatar is doing
  yearsActive: number;
}

export function canSplitSoul(realm: RealmId, fragments: number): boolean {
  return realmOrder(realm) >= 4 && fragments < 3; // Nascent Soul+; max 3 avatars
}

export function createAvatar(rng: RNG, parentName: string, parentRealm: RealmId): AvatarState {
  return {
    id: uid('avatar', rng),
    name: `${parentName}'s Avatar ${randInt(rng, 1, 99)}`,
    realm: parentRealm,
    qi: 0,
    location: 'unknown',
    task: 'cultivating independently',
    yearsActive: 0,
  };
}

// ═══ 8. CAVE-WORLD (洞天) ════════════════════════════════════════
// A personal pocket dimension inside a storage treasure. Cultivators can
// live inside it, grow herbs, store beasts. The Heaven-Defying Bead contains one.
export interface CaveWorld {
  name: string;
  size: number;                // 0..100 — how developed the cave-world is
  herbGarden: number;          // 0..100 — herb cultivation capacity
  spiritVeinQuality: number;   // 0..1 — quality of the internal vein
  inhabitants: string[];       // NPCs/beasts living inside
  ambientQi: number;           // Qi density inside (affects cultivation speed when meditating inside)
}

export function genEmptyCaveWorld(): CaveWorld {
  return { name: 'Undeveloped Cave-World', size: 1, herbGarden: 0, spiritVeinQuality: 0.1, inhabitants: [], ambientQi: 1.2 };
}

export function developCaveWorld(cw: CaveWorld, investment: number): { message: string } {
  cw.size = Math.min(100, cw.size + investment * 0.5);
  cw.ambientQi = 1 + cw.size * 0.02;
  cw.herbGarden = Math.min(100, cw.herbGarden + investment * 0.3);
  return { message: `Cave-World developed. Size: ${Math.round(cw.size)}/100. Ambient Qi: ${cw.ambientQi.toFixed(1)}×. Herb garden: ${Math.round(cw.herbGarden)}/100.` };
}

// ═══ 9. REINCARNATION / PAST LIVES (轮回/前世) ════════════════════
// Er Gen deals heavily with reincarnation. Past life memories can be a
// cultivation resource. Breaking the cycle of samsara is a Transcendence goal.
export interface PastLifeMemory {
  id: string;
  life: string;                // who you were
  era: string;                 // when
  fragment: string;            // what memory fragment you recovered
  understandingGain: number;   // dao understanding bonus
  karmicThread?: string;       // a karmic thread from that life
  year: number;                // when the memory surfaced
}

export function genPastLifeMemory(rng: RNG, year: number): PastLifeMemory {
  const lives = [
    { life: 'a nameless mortal farmer', era: 'before the Jade-Void civilization', frag: 'the feel of soil and the patience of seasons', gain: 2 },
    { life: 'a rogue cultivator who died in a tribulation', era: 'the Age of Ash', frag: 'the taste of failed ambition and the weight of lightning', gain: 5 },
    { life: 'a sect patriarch who fell to a demonic plot', era: 'the early Verdant Alliance', frag: 'the bitterness of betrayal and the clarity of death', gain: 8 },
    { life: 'a spirit beast who achieved sapience', era: 'before human cultivation', frag: 'the instinct of the wild and the memory of running free', gain: 3 },
    { life: 'an immortal who chose to reincarnate', era: 'before the Vast Expanse formed', frag: 'a fragment of true Dao understanding', gain: 15 },
    { life: 'a scholar who pursued truth unto death', era: 'the Age of Jade-Void', frag: 'the joy of discovery and the sorrow of incomplete knowledge', gain: 6 },
  ];
  const chosen = pick(rng, lives);
  return {
    id: uid('pastlife', rng), life: chosen.life, era: chosen.era, fragment: chosen.frag,
    understandingGain: chosen.gain, year,
  };
}

// ═══ 10. TRIBULATION DEPTH (天劫深度) ═════════════════════════════
// Er Gen tribulations are not just "lightning strikes." Each major realm has
// its own tribulation type with unique mechanics.
export type TribulationType = 'heart_demon' | 'karmic' | 'lightning' | 'life_death' | 'nine_nine' | 'world';

export interface TribulationEvent {
  type: TribulationType;
  forRealm: RealmId;
  stages: number;              // how many stages the tribulation has
  description: string;
  canShare: boolean;           // can allies help you through it?
  canSteal: boolean;           // can someone steal your tribulation lightning? (Thunder Beast)
}

export function getTribulationForRealm(realm: RealmId): TribulationEvent {
  const o = realmOrder(realm);
  if (o >= 14) return { type: 'world', forRealm: realm, stages: 9, description: 'WORLD TRIBULATION — the world itself rejects your transcendence. Nine stages of reality-shattering lightning.', canShare: false, canSteal: false };
  if (o >= 11) return { type: 'nine_nine', forRealm: realm, stages: 9, description: 'NINE-NINE TRIBULATION — 9 stages of 9 strikes each. The ultimate heavenly test.', canShare: true, canSteal: true };
  if (o >= 7) return { type: 'life_death', forRealm: realm, stages: 5, description: 'LIFE-AND-DEATH TRIBULATION — five stages testing your very existence.', canShare: true, canSteal: true };
  if (o >= 5) return { type: 'karmic', forRealm: realm, stages: 3, description: 'KARMIC TRIBULATION — lightning that burns away your karmic debts. Three stages.', canShare: false, canSteal: true };
  if (o >= 4) return { type: 'heart_demon', forRealm: realm, stages: 1, description: 'HEART DEMON TRIBULATION — face your inner demons. One stage, but the most dangerous.', canShare: false, canSteal: false };
  return { type: 'lightning', forRealm: realm, stages: 3, description: 'HEAVENLY LIGHTNING TRIBULATION — standard tribulation. Three lightning strikes.', canShare: false, canSteal: true };
}

// ═══ 11. CORPSE REFINEMENT (炼尸) ═════════════════════════════════
// Wang Lin refines corpses into puppets — a major RI mechanic.
// Corpses become combat puppets that fight alongside the cultivator.
export interface RefinedCorpse {
  id: string;
  name: string;               // who they were in life
  originalRealm: RealmId;     // their cultivation when alive
  refinementStage: number;    // 0..9 — how refined the corpse is
  combatPower: number;        // scales with refinement + original realm
  innateAbility?: string;     // a technique preserved from life
  loyalty: number;            // 0..1 — how obedient (may rebel if low)
}

export function refineCorpse(rng: RNG, name: string, originalRealm: RealmId): RefinedCorpse {
  return {
    id: uid('corpse', rng), name, originalRealm,
    refinementStage: 1, combatPower: (realmOrder(originalRealm) + 1) * 20,
    loyalty: 0.5,
  };
}

export function advanceCorpseRefinement(corpse: RefinedCorpse): { ok: boolean; message: string } {
  if (corpse.refinementStage >= 9) return { ok: false, message: `${corpse.name}'s corpse is fully refined (stage 9).` };
  corpse.refinementStage++;
  corpse.combatPower = Math.round(corpse.combatPower * 1.3);
  corpse.loyalty = Math.min(1, corpse.loyalty + 0.05);
  return { ok: true, message: `${corpse.name}'s corpse advanced to refinement stage ${corpse.refinementStage}. Combat power: ${corpse.combatPower}.` };
}

// ═══ 12. SOUL LAMP (命灯) ═════════════════════════════════════════
// Sects track their members' life/death via soul lamps. If a lamp goes out,
// the member has died. Used for sect management + assassination detection.
export interface SoulLamp {
  ownerId: string;
  ownerName: string;
  lit: boolean;
  flickering: boolean;        // lamp flickers when the owner is in danger
  sectId?: string;
}

export function createSoulLamp(ownerId: string, ownerName: string, sectId?: string): SoulLamp {
  return { ownerId, ownerName, lit: true, flickering: false, sectId };
}

// ═══ 13. INCENSE SYSTEM (香火) ═══════════════════════════════════
// Bai Xiaochun's signature: he lights incense that grants various effects.
// Incense can calm the mind, ward off evil, boost cultivation, or attract
// divine attention. A unique utility system from A Will Eternal.
export type IncenseType = 'calming' | 'warding' | 'cultivation' | 'divine' | 'mortal_attracting';

export interface IncenseEffect {
  type: IncenseType;
  duration: number;           // how many years the effect lasts
  effect: string;
  qiMultiplier?: number;      // cultivation boost
  statusCure?: string;        // what status it cures
  aggroRadius?: number;       // how far it attracts/deters entities
}

export function lightIncense(rng: RNG, type: IncenseType): IncenseEffect {
  const effects: Record<IncenseType, IncenseEffect> = {
    calming: { type, duration: 1, effect: 'Calms the mind; +10% dao understanding rate; cures heart_demon', statusCure: 'heart_demon' },
    warding: { type, duration: 5, effect: 'Wards off evil; deters demonic beasts within 200 blocks', aggroRadius: 200 },
    cultivation: { type, duration: 1, effect: 'Boosts Qi gathering by 50%', qiMultiplier: 1.5 },
    divine: { type, duration: 1, effect: 'Attracts divine attention; may trigger a revelation or a tribulation' },
    mortal_attracting: { type, duration: 10, effect: 'Attracts mortal worshippers; generates faith power', aggroRadius: 1000 },
  };
  return effects[type];
}

// ═══ 14. RESTRICTED DOMAIN (禁制领域) ═════════════════════════════
// A personal domain that restricts the actions of those caught inside.
// Wang Lin and Meng Hao both use restricted domains in combat.
export interface RestrictedDomain {
  name: string;
  ownerRealm: RealmId;
  radius: number;             // in blocks
  restrictions: string[];     // what's forbidden inside (e.g., 'no flying', 'no Qi gathering', 'no teleportation')
  duration: number;           // how many turns it lasts
  active: boolean;
}

export function castRestrictedDomain(rng: RNG, ownerRealm: RealmId): RestrictedDomain {
  const allRestrictions = ['no flying', 'no Qi gathering', 'no teleportation', 'no divine sense', 'no artifact use', 'suppressed cultivation'];
  const count = Math.min(allRestrictions.length, 1 + Math.floor(realmOrder(ownerRealm) / 3));
  return {
    name: pick(rng, ['Slaughter Domain', 'Seal Domain', 'Karmic Domain', 'Void Domain', 'Time Domain']),
    ownerRealm, radius: 50 + realmOrder(ownerRealm) * 20,
    restrictions: pickN(rng, allRestrictions, count),
    duration: 3 + realmOrder(ownerRealm),
    active: true,
  };
}

// ═══ 15. BLOOD SEAL (血印) ═══════════════════════════════════════
// Wang Lin's blood seals are one-use combat items that carry his killing intent.
// They can be placed on targets to track them, or detonated for massive damage.
export interface BloodSeal {
  id: string;
  power: number;              // scales with creator's realm + killing intent
  type: 'tracking' | 'explosive' | 'binding' | 'soul_mark';
  targetName?: string;
  placed: boolean;
}

export function createBloodSeal(rng: RNG, creatorRealm: RealmId): BloodSeal {
  return {
    id: uid('seal', rng),
    power: (realmOrder(creatorRealm) + 1) * 50,
    type: pick(rng, ['tracking', 'explosive', 'binding', 'soul_mark'] as const),
    placed: false,
  };
}
