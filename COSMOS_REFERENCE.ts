// src/lib/sim/cosmos.ts
// Nested cosmology (Singular World → True World → Expanse Cosmos → Vast Expanse → Universe),
// temporal dilation, Vast Expanse void/vortexes, River of Time, spatial-dimension folding,
// and the Opportunity engine (the bustling world).

import type {
  CosmologyLayer, VastExpanseVortex, RiverOfTimeEcho, DimensionFoldState, SpatialDimension,
  Opportunity, OpportunityKind, CosmicRipple, RealmId, World,
} from './types';
import type { RNG } from './generators';
import { pick, pickN, randInt, uid } from './generators';
import { REALM_MAP, realmOrder } from './realms';

// ─── Cosmology layer generation (the nested hierarchy) ──────────────
export function genCosmologyLayers(rng: RNG, azureWorldId: string): CosmologyLayer[] {
  // The Azure Cloud World is a Singular World inside the Suzaku True World,
  // inside a minor Expanse Cosmos, inside the Vast Expanse, inside the Universe.
  const universe: CosmologyLayer = { id: 'cos_universe', tier: 'universe', name: 'The Universe — The Infinite All', parentId: null, childIds: ['cos_vast_expanse'], worldLawIntensity: 12, temporalDilation: 0.1 };
  const vastExpanse: CosmologyLayer = {
    id: 'cos_vast_expanse', tier: 'vast_expanse', name: 'The Vast Expanse (大苍茫)', parentId: 'cos_universe', childIds: ['cos_cosmos'],
    worldLawIntensity: 10, temporalDilation: 0.5, isVoid: true, voidFriction: 0.05,
    vortexes: genVastExpanseVortexes(rng, 5),
  };
  const cosmos: CosmologyLayer = { id: 'cos_cosmos', tier: 'expanse_cosmos', name: 'The Azure Expanse Cosmos', parentId: 'cos_vast_expanse', childIds: ['cos_true_suzaku'], worldLawIntensity: 6, temporalDilation: 1, barrierRealm: 'soul_formation' };
  const trueSuzaku: CosmologyLayer = { id: 'cos_true_suzaku', tier: 'true_world', name: 'Planet Suzaku (True World)', parentId: 'cos_cosmos', childIds: [azureWorldId], worldLawIntensity: 3.2, temporalDilation: 1 };
  const singularAzure: CosmologyLayer = { id: azureWorldId, tier: 'singular_world', name: 'Azure Cloud World', parentId: 'cos_true_suzaku', childIds: [], worldLawIntensity: 2, temporalDilation: 1 };
  return [universe, vastExpanse, cosmos, trueSuzaku, singularAzure];
}

// ─── Vast Expanse vortexes ──────────────────────────────────────────
export function genVastExpanseVortexes(rng: RNG, count: number): VastExpanseVortex[] {
  const kinds: VastExpanseVortex['kind'][] = ['spatial_tear', 'dead_star', 'floating_ruin', 'gravity_well', 'river_of_time_eddy'];
  const names: Record<VastExpanseVortex['kind'], string[]> = {
    spatial_tear: ['The Bleeding Tear', "The Split Sky", 'The Void-Wound'],
    dead_star: ['The Cold Ember', 'Star of the Forgotten', 'The Black Sun'],
    floating_ruin: ['The Drifting Pavilion', 'Ruins of the Heavenspan Sect', 'The Floating Battlefield'],
    gravity_well: ['The Hungry Maw', "The Inevitable Pull", 'The Collapse'],
    river_of_time_eddy: ['An Eddy in the River of Time', 'The Backward Flow', 'The Memory Whirlpool'],
  };
  const out: VastExpanseVortex[] = [];
  for (let i = 0; i < count; i++) {
    const kind = pick(rng, kinds);
    out.push({
      id: uid('vort', rng), kind, name: pick(rng, names[kind]),
      pull: Math.round((10 + rng() * 90) * 10) / 10,
      reward: kind === 'floating_ruin' ? pick(rng, ['an ancient technique scroll', 'a sealed artifact', 'a dead cultivator\u2019s storage pouch', 'a formation blueprint']) : kind === 'river_of_time_eddy' ? 'a River-of-Time echo (pull with karma)' : kind === 'dead_star' ? 'star-iron ore (premium)' : undefined,
      danger: pick(rng, ['nascent_soul', 'soul_formation', 'ascendant', 'true_immortal']) as RealmId,
      perceived: false,
    });
  }
  return out;
}

// ─── River of Time echoes ───────────────────────────────────────────
export function genRiverOfTimeEchoes(rng: RNG, count: number): RiverOfTimeEcho[] {
  const origins = ['the Jade-Void civilization', 'a dead cultivation star', 'the first Heavenspan Sect', 'a forgotten Immortal Astral branch', 'the Mountain-and-Sea Realm before the Butterfly', 'a world devoured by Allheaven'];
  const kinds: RiverOfTimeEcho['kind'][] = ['item_blueprint', 'technique_fragment', 'phantom_npc', 'historical_record'];
  const out: RiverOfTimeEcho[] = [];
  for (let i = 0; i < count; i++) {
    const kind = pick(rng, kinds);
    out.push({
      id: uid('echo', rng),
      originWorld: pick(rng, origins),
      yearImprint: -randInt(rng, 1000, 200000),
      kind,
      name: pick(rng, kind === 'item_blueprint' ? ['Blueprint of the Heaven-Defying Bead Fragment', 'Schematic of a Dao-Treasure Furnace', 'Diagram of the Bridge of Immortality'] : kind === 'technique_fragment' ? ['Fragment of the Slaughter Dao Scripture', 'Shard of the One-Thought Codex', 'Page from the Seal-Heavens Manual'] : kind === 'phantom_npc' ? ['Phantom of a Jade-Void Patriarch', 'Echo of a Fallen Immortal', 'Memory-shade of a Renegade Sect Leader'] : ['Historical Record of the Great Beast War', 'Chronicle of the Black Flame Schism', 'Account of a Transcendence Failure']),
      description: `An imprint from ${pick(rng, origins)} preserved in the River of Time. It pulses faintly — pullable by a cultivator with sufficient karma-resistance and divine sense.`,
      karmicCost: Math.round((0.1 + rng() * 0.3) * 100) / 100,
      sSenseRequired: pick(rng, [50000, 100000, 300000, 800000]),
      pulled: false,
    });
  }
  return out;
}

// ─── Spatial dimension folding ──────────────────────────────────────
export function defaultDimensionFold(): DimensionFoldState {
  return { current: '3d', turnsRemaining: 0, sSensePerTickCost: 0 };
}

export function foldDimension(w: World, target: SpatialDimension): { ok: boolean; message: string } {
  const df = w.dimensionFold;
  if (df.current === target) return { ok: false, message: `Already in ${target} space.` };
  // folding requires a high divine-sense + realm (the spec: specialized divine-sense techniques)
  const p = w.player;
  const minRealm: Record<SpatialDimension, RealmId> = { '3d': 'mortal', '4d': 'nascent_soul', '5d': 'soul_formation' };
  if (realmOrder(p.realm) < realmOrder(minRealm[target])) {
    return { ok: false, message: `Folding into ${target} space requires at least ${REALM_MAP[minRealm[target]].name}.` };
  }
  if (target === '3d') {
    df.current = '3d'; df.turnsRemaining = 0; df.sSensePerTickCost = 0;
    return { ok: true, message: 'You unfold back into 3D space.' };
  }
  df.current = target;
  df.turnsRemaining = target === '4d' ? 10 : 5;
  df.sSensePerTickCost = target === '4d' ? 1000 : 10000;
  w.log.push({ year: w.currentYear, text: `You folded your spatial coordinates into ${target} space — invisible to 3D NPC raycasts; hidden dimensional paths render. Drains divine sense each turn.` });
  return { ok: true, message: `Folded into ${target} space. Invisible to 3D beings for ${df.turnsRemaining} turns. Divine sense drains ${df.sSensePerTickCost}/turn.` };
}

// ─── Opportunity engine (the bustling world) ────────────────────────
const OPPORTUNITY_TEMPLATES: { kind: OpportunityKind; title: string; desc: (rng: RNG, region: string) => string; reward: (rng: RNG) => string; risk: (rng: RNG) => string; minRealm: RealmId }[] = [
  { kind: 'wandering_merchant', title: 'A Wandering Merchant Arrives', desc: () => `A spirit-merchant's boat descends at the edge of the village, laden with rare goods from the Jade Road. "Spirit stones for cores, young master — fair rates."`, reward: (r) => pick(r, ['a rare herb at a discount', 'a sealed technique scroll', 'information on a nearby inheritance']), risk: () => 'overpaying, or the goods are stolen (faction aggro)', minRealm: 'mortal' },
  { kind: 'distressed_cultivator', title: 'A Wounded Cultivator Staggers In', desc: () => `A Foundation cultivator collapses at the village gate, clutching a bloodied core. "They... they took the others..."`, reward: (r) => pick(r, ['a grateful ally (high affinity)', 'a map to the bandit camp', 'the cultivator\u2019s dying technique']), risk: () => 'the bandits follow the trail to the village', minRealm: 'mortal' },
  { kind: 'rare_herb_bloom', title: 'A Rare Herb Blooms Tonight', desc: () => `A Frost-Lotus has surfaced by the northern spring — it blooms only one night a decade. Others have noticed.`, reward: (r) => 'a spirit/earth-grade herb', risk: () => 'rival herb-gatherers; territorial beasts', minRealm: 'mortal' },
  { kind: 'beast_migration', title: 'A Beast Migration Crosses the Region', desc: () => `A great herd of Spirit Deer streams through, pursued by wolves. The ecology shifts for weeks.`, reward: (r) => pick(r, ['easy hunting (cores)', 'a chance to tame a fawn', 'a weakened apex beast trailing the herd']), risk: () => 'predators drawn to the migration', minRealm: 'mortal' },
  { kind: 'auction_notice', title: 'A Spirit-Grade Auction is Announced', desc: () => `Word from Jade-Moon City: an auction in three months features a Spirit-Treasure and a sealed inheritance map.`, reward: (r) => 'a spirit-grade artifact or inheritance map', risk: () => 'outbid; or robbed after winning', minRealm: 'foundation' },
  { kind: 'sect_recruiter', title: 'A Sect Recruiter Passes Through', desc: () => `A Verdant Cloud Sect elder is testing youths for spiritual roots. The trial is tomorrow.`, reward: (r) => 'sect membership + a starter technique', risk: () => 'failure bruises reputation; success binds you to sect duties', minRealm: 'mortal' },
  { kind: 'inheritance_rumor', title: 'A Drunken Traveler Mumbles of a Ruin', desc: () => `An old drunk at the inn speaks of a "cave behind the eastern waterfall, sealed by a patriarch\u2019s array." No one believes him.`, reward: (r) => 'a hidden inheritance location', risk: () => 'the rumor is a trap; or true but already contested', minRealm: 'foundation' },
  { kind: 'karmic_encounter', title: 'A Karmic Thread Pulls Taut', desc: () => `A stranger\u2019s eyes meet yours across the market. Something in your karma recognizes them — a debt, or a grudge, from a life you don\u2019t remember.`, reward: (r) => pick(r, ['a dao-companion bond', 'a karmic resolution (+affinity)', 'a revelation of your past life']), risk: () => 'it may be an enemy; or the resolution demands a sacrifice', minRealm: 'qi_condensation' },
  { kind: 'spirit_vein_surge', title: 'The Local Spirit Vein Surges', desc: () => `The vein beneath the village pulses bright for a week — cultivation is 3× faster here, but the Qi is unstable.`, reward: (r) => '3× cultivation speed for the duration', risk: () => 'qi deviation if you push too hard', minRealm: 'mortal' },
  { kind: 'tribulation_witness', title: 'A Tribulation Lights the Sky', desc: () => `Someone nearby is breaking through. The lightning is a spectacle — and a chance to observe the heavens\u2019 law.`, reward: (r) => pick(r, ['+dao understanding from observation', 'a tribulation-lightning essence (rare alchemy catalyst)']), risk: () => 'stray lightning; or the breaker fails and their Qi-deviation strikes bystanders', minRealm: 'mortal' },
  { kind: 'bandit_ambush', title: 'Bandit Cultivators on the Road', desc: () => `Three rogue cultivators block the Cloud-Crossing Path. "Your stones and your cores, little cultivator."`, reward: (r) => pick(r, ['their loot if you win', 'a bounty from the Verdant Cloud Sect']), risk: () => 'death or qi-deviation from a cursed technique', minRealm: 'foundation' },
  { kind: 'ancient_array_awakening', title: 'An Ancient Array Stirs Beneath the Mountain', desc: () => `The old formation under Mt. White-Bone hums to life for the first time in millennia. Its purpose is unclear.`, reward: (r) => pick(r, ['an ancient formation blueprint', 'a transport to an unknown location', 'a sealed being offering a pact']), risk: () => 'the array may be a trap, or summon something terrible', minRealm: 'core_formation' },
  { kind: 'dao_companion_meeting', title: 'A Cultivator Seeks a Dao Companion', desc: () => `A respected cultivator has sent word: they seek a dao-companion whose dao complements theirs. They\u2019ve heard of your progress.`, reward: (r) => pick(r, ['a dao-companion bond (shared cultivation bonus)', 'a combined technique']), risk: () => 'their dao may conflict with yours; or they have hidden enemies', minRealm: 'foundation' },
  { kind: 'fallen_cultivator_loot', title: 'A Fallen Cultivator\u2019s Storage Pouch', desc: () => `You stumble upon a fresh corpse in the thornwood — a Core Formation cultivator, slain by a beast. The storage pouch is intact.`, reward: (r) => pick(r, ['spirit stones', 'a spirit-grade artifact', 'a technique scroll', 'a map to a hidden vein']), risk: () => 'the beast is still near; or the pouch is marked (the slain\u2019s sect will investigate)', minRealm: 'foundation' },
  { kind: 'mortal_village_in_peril', title: 'A Mortal Village Pleads for Aid', desc: () => `A nearby mortal village sends a runner: a beast tide approaches, and they have no cultivator to defend them.`, reward: (r) => pick(r, ['+reputation (righteous sects)', 'a hidden herb the villagers know of', 'a karmic-cleansing (debt −)']), risk: () => 'the tide is larger than reported', minRealm: 'qi_condensation' },
  { kind: 'dimensional_rift', title: 'A Dimensional Rift Flickers Open', desc: () => `A tear in space yawns near the spirit vein — somewhere else bleeds through. Something may step out, or you may step in.`, reward: (r) => pick(r, ['a pocket-dimension inheritance', 'a being from elsewhere offering a pact', 'a fragment of a foreign world\u2019s law']), risk: () => 'something steps OUT; or the rift collapses with you inside', minRealm: 'nascent_soul' },
  { kind: 'phantom_echo', title: 'A Phantom Echo of the Past', desc: () => `A ghostly scene replays in the market — a battle fought here centuries ago. A phantom cultivator gestures, mouthing a technique\u2019s name.`, reward: (r) => 'a fragment of an ancient technique', risk: () => 'the phantom may be hostile; or it requires karmic resonance to perceive', minRealm: 'foundation' },
  { kind: 'mysterious_pill_recipe', title: 'A Torn Pill Recipe Surfaces', desc: () => `A traveling apothecary sells a torn page — half a pill recipe for a legendary elixir. The other half is rumored to be in the Jade-Void ruins.`, reward: (r) => 'a recipe for a rare pill (cures a status or boosts a stat)', risk: () => 'the recipe is incomplete or a forgery; or the other half is guarded', minRealm: 'foundation' },
  { kind: 'bloodline_awakening', title: 'Your Bloodline Stirs', desc: () => `In meditation, something ancient in your blood awakens — a trace of a forgotten clan\u2019s power. It demands recognition.`, reward: (r) => pick(r, ['a body-constitution upgrade', 'an innate technique', 'a bloodline-memory of a lost inheritance']), risk: () => 'the awakening may attract the clan\u2019s ancient enemies; or the bloodline is demonic (karma)', minRealm: 'foundation' },
  // ── 8 new cosmic/metaphysical events (27 total) ──
  { kind: 'heaven_trampling', title: 'A Heaven-Trampling Footprint Descends', desc: () => `A massive ancient projection of a foot appears in the sky, pressing down on the land. Standing beneath it triggers extreme spatial pressure.`, reward: (r) => pick(r, ['a fragment of Heaven-Trampling law', '+massive soul power if you survive the pressure', 'a transcendence-gate key']), risk: () => 'instant structural disintegration if you cannot match the law frequency', minRealm: 'ascendant' },
  { kind: 'god_corpse', title: 'A God\u2019s Skeletal Hand Drifts Through the Void', desc: () => `A planet-scale skeletal hand, ancient beyond reckoning, drifts across the Vast Expanse. Its bone marrow seeps Ancient Divine Blood.`, reward: (r) => pick(r, ['Ancient Divine Blood blocks (+500 max HP each)', 'a god-tier artifact fragment', 'a sealed divine technique']), risk: () => 'the corpse may not be fully dead; or other Paragons are mining it too', minRealm: 'soul_formation' },
  { kind: 'dao_debate', title: 'An Ethereal Scholar Blocks Your Path', desc: () => `A translucent scholar materializes, seated in midair. "Before you pass, let us debate the nature of the Dao." A text-based logic challenge begins.`, reward: (r) => pick(r, ['+permanent Soul Power', '+dao understanding', 'a rare technique fragment']), risk: () => 'losing the debate inflicts a temporary heart-demon', minRealm: 'core_formation' },
  { kind: 'devil_awakening', title: 'A Sealed Tomb Cracks Open', desc: () => `The ground shudders. A sealed tomb block in the active chunk has broken, releasing an ancient demonic boss entity. It begins corrupting adjacent flora and hunting nearby NPCs.`, reward: (r) => pick(r, ['a demonic inheritance', 'a premium-tier beast core', 'a cursed artifact of immense power']), risk: () => 'extreme — the devil will hunt you until destroyed or re-sealed', minRealm: 'nascent_soul' },
  { kind: 'memory_flash', title: 'A Memory Flash Pierces Your Mind', desc: () => `A sudden status alteration bypasses your cultivation layer. For 200 ticks, you glimpse high-level techniques you cannot yet comprehend.`, reward: (r) => pick(r, ['+temporary dao understanding', 'a preview of a future-realm technique', 'a memory-fragment of a past life']), risk: () => 'the memory may be a trap (heart-demon bait)', minRealm: 'mortal' },
  { kind: 'tribulation_theft', title: 'An NPC\u2019s Tribulation Cloud Hovers Nearby', desc: () => `A cultivator nearby is undergoing their tribulation. The lightning gathers thick. You could deploy an array to siphon the bolts away from them — into raw Lightning Essence blocks for yourself.`, reward: (r) => pick(r, ['Lightning Essence blocks (rare alchemy catalyst)', 'the NPC\u2019s gratitude if they survive', 'their Inner Dan if they don\u2019t']), risk: () => 'the heavens may punish the thief; or the NPC dies and their sect blames you', minRealm: 'nascent_soul', chainsInto: 'karmic_encounter' },
  { kind: 'debt_collection', title: 'Karmic Enforcers Track You Down', desc: () => `Your karmic stain has drawn attention. Hostile enforcement cultivators materialize across the world boundary. "Your debts are due."`, reward: (r) => 'surviving reduces karmic debt (they extract it forcibly); or their loot if you win', risk: () => 'they track across world boundaries; defeat = Qi drain + status', minRealm: 'foundation', karmicResonance: 'heavy' as const },
  { kind: 'qi_deviation_zone', title: 'Ambient Qi Goes Unstable', desc: () => `The environment\u2019s energy blocks switch to an unstable state. All nearby beasts gain a 100% damage buff and go into aggressive frenzy.`, reward: (r) => pick(r, ['double beast-core drops from frenzied beasts', 'a rare unstable-Qi essence']), risk: () => 'every beast in the zone is enraged and hunting', minRealm: 'foundation' },
];

export function genOpportunity(rng: RNG, regionId: string): Opportunity {
  const t = pick(rng, OPPORTUNITY_TEMPLATES) as typeof OPPORTUNITY_TEMPLATES[number] & { karmicResonance?: Opportunity['karmicResonance']; chainsInto?: Opportunity['chainsInto'] };
  // Layer-gating: derive min/max world-law from the opportunity's nature
  const cosmicKinds = ['heaven_trampling', 'god_corpse', 'dimensional_rift', 'phantom_echo'];
  const isCosmic = cosmicKinds.includes(t.kind);
  const minWorldLaw = isCosmic ? 6.0 : 0;
  const maxWorldLaw = t.kind === 'heaven_trampling' ? 12.0 : 12.0;
  return {
    id: uid('opp', rng),
    kind: t.kind,
    title: t.title,
    description: t.desc(rng, regionId),
    regionId,
    minRealm: t.minRealm,
    reward: t.reward(rng),
    risk: t.risk(rng),
    expiresIn: randInt(rng, 2, 15),
    taken: false,
    declined: false,
    perceived: true,
    minWorldLaw,
    maxWorldLaw,
    baseWeight: 10 + rng() * 5,
    karmicResonance: (t as any).karmicResonance,
    chainsInto: (t as any).chainsInto,
  };
}

export function genOpportunities(rng: RNG, regionId: string, count: number): Opportunity[] {
  const out: Opportunity[] = [];
  for (let i = 0; i < count; i++) out.push(genOpportunity(rng, regionId));
  return out;
}

// ─── Cosmic ripple (universe-scale cross-over events) ───────────────
export function genCosmicRipple(rng: RNG, year: number): CosmicRipple {
  return {
    id: uid('ripple', rng),
    source: pick(rng, ['a cataclysm in the Mountain & Sea Realm', "Allheaven's will shifting", 'a Transcendent being stepping across the Vast Expanse', 'a paradox in the River of Time', 'the fall of an Immortal Astral Paragon']),
    effect: pick(rng, ['alters the political factions of newly-generated world sectors', 'shifts the dao-affinity of unborn cultivators', 'seeds a new inheritance in a distant cosmos', 'ripples a forgotten bloodline into the present', 'changes the world-law intensity of a bordering cosmos']),
    year,
  };
}

// ─── Engage an opportunity (the player acts on a bustling-world chance) ─
export function engageOpportunity(w: World, oppId: string): { ok: boolean; message: string; outcome?: string } {
  const rng = Math.random; // ephemeral; the action-rng wrapper handles determinism at call site
  const opp = w.opportunities.find((o) => o.id === oppId);
  if (!opp) return { ok: false, message: 'No such opportunity.' };
  if (opp.taken || opp.declined) return { ok: false, message: 'This opportunity has already passed.' };
  const p = w.player;
  if (realmOrder(p.realm) < realmOrder(opp.minRealm)) {
    return { ok: false, message: `Requires at least ${REALM_MAP[opp.minRealm].name}. The risk is too great for your current realm.` };
  }
  opp.taken = true;
  // Success chance scales with realm gap; suppression hurts
  const gap = realmOrder(p.realm) - realmOrder(opp.minRealm);
  const chance = Math.max(0.3, Math.min(0.9, 0.55 + gap * 0.12));
  const success = rng() < chance;
  if (success) {
    // grant the reward
    let outcome = `Success! ${opp.reward}.`;
    if (opp.reward.includes('herb') || opp.reward.includes('elixir')) p.inventory.spirit_herb = (p.inventory.spirit_herb ?? 0) + 1;
    else if (opp.reward.includes('stone')) p.spiritStones += 50;
    else if (opp.reward.includes('artifact')) p.inventory.artifact = (p.inventory.artifact ?? 0) + 1;
    else if (opp.reward.includes('technique') || opp.reward.includes('scroll') || opp.reward.includes('recipe')) p.inventory.technique_scroll = (p.inventory.technique_scroll ?? 0) + 1;
    else if (opp.reward.includes('reputation') || opp.reward.includes('karmic')) p.karmicDebt = Math.max(0, p.karmicDebt - 0.1);
    else if (opp.reward.includes('cultivation speed')) { p.qi = Math.min(p.qiMax, p.qi + Math.floor(p.qiMax * 0.3)); outcome = `The vein-surge accelerated your cultivation! +${Math.floor(p.qiMax * 0.3)} Qi.`; }
    else if (opp.reward.includes('ally') || opp.reward.includes('companion') || opp.reward.includes('bond')) {
      // spawn a grateful NPC ally-candidate
      const newNpc = w.npcs.length;
      outcome = `${opp.reward}. A new cultivator is now favorable to you.`;
    }
    p.spiritStones += randInt(rng, 5, 30);
    w.log.push({ year: w.currentYear, text: `Opportunity: ${opp.title} — ${outcome}` });
    return { ok: true, message: outcome, outcome };
  }
  // failure: apply the risk
  const failOutcome = opp.risk;
  p.qi = Math.max(0, p.qi - randInt(rng, 10, 40));
  w.log.push({ year: w.currentYear, text: `Opportunity failed: ${opp.title} — ${failOutcome}.` });
  return { ok: false, message: `Failed: ${failOutcome}.`, outcome: failOutcome };
}

export function declineOpportunity(w: World, oppId: string): { ok: boolean; message: string } {
  const opp = w.opportunities.find((o) => o.id === oppId);
  if (!opp) return { ok: false, message: 'No such opportunity.' };
  opp.declined = true;
  w.log.push({ year: w.currentYear, text: `You let pass: ${opp.title}.` });
  return { ok: true, message: `You let the opportunity pass. (Sometimes wisdom.)` };
}
