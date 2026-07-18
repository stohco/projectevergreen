// src/lib/sim/locations.ts
// Real Er Gen locations organized by cosmology layer. Every location is from the novels.
// This serves as the world-generation template for region/settlement creation.

import type { CosmicTier } from './types';

export interface ErGenLocation {
  name: string;
  nameCn?: string;
  novel: string;
  layer: number;              // 1-5 cosmology layer
  type: 'country' | 'sect' | 'city' | 'forbidden_land' | 'ocean' | 'mountain' | 'desert' | 'void_zone' | 'planet' | 'realm' | 'continent' | 'secret_realm';
  worldLawIntensity: number;  // L_world for this location
  description: string;
  notableFeatures: string[];
  governingFaction?: string;
  dangers: string[];
  resources: string[];
}

// ═══ LAYER 1: Azure Cloud Singular World (L=2) ═════════════════════
// Lower-tier mortal world locations from all novels' starting areas.
export const LAYER1_LOCATIONS: ErGenLocation[] = [
  { name: 'State of Zhao', nameCn: '赵国', novel: 'ISSTH/RI', layer: 1, type: 'country', worldLawIntensity: 2, description: 'A small mortal country on Planet South Heaven/Suzaku. Where both Meng Hao and Wang Lin began their journeys.', notableFeatures: ['mortal villages', 'minor spirit veins', 'outer sect outposts'], governingFaction: 'varies', dangers: ['spirit beasts', 'rogue cultivators', 'bandits'], resources: ['low-grade spirit herbs', 'mortal iron', 'Qi-Grass'] },
  { name: 'Heng Yue Sect', nameCn: '恒岳派', novel: 'Renegade Immortal', layer: 1, type: 'sect', worldLawIntensity: 2, description: 'Wang Lin\'s first sect. A minor sect in Zhao Country. Known for forging flying swords — Wealth was forged here.', notableFeatures: ['flying-sword forge', 'outer disciple quarters', 'spirit herb garden'], governingFaction: 'Heng Yue Sect', dangers: ['sect politics', 'inner disciple bullying'], resources: ['Iron-Edge Flying Swords', 'Qi-Grass', 'Clear-Mind Moss'] },
  { name: 'Mistveil Village', nameCn: '雾隐村', novel: 'Azure Cloud World', layer: 1, type: 'city', worldLawIntensity: 2, description: 'The player\'s starting village. Founded 500 years ago when a new spirit vein surfaced.', notableFeatures: ['minor spirit vein', 'herb-tribute covenant with Verdant Cloud Sect', 'defensive array'], governingFaction: 'Verdant Cloud Sect', dangers: ['spirit wolves', 'beast tides', 'rogue cultivators'], resources: ['Qi-Grass', 'Spirit-Wheat', 'Cloud-Silk Deer antlers'] },
  { name: 'Jade-Moon City', nameCn: '玉月城', novel: 'Azure Cloud World', layer: 1, type: 'city', worldLawIntensity: 2.5, description: 'The regional market city. Spirit stone economy, auction houses, merchant guilds.', notableFeatures: ['auction house', 'merchant guild', 'alchemist pavilion', 'mercenary hall'], governingFaction: 'Jade-Road Merchant Guild', dangers: ['thieves', 'scams', 'faction spies'], resources: ['spirit stones', 'pills', 'artifacts', 'information'] },
  { name: 'Spirit Stream Sect', nameCn: '灵溪宗', novel: 'A Will Eternal', layer: 1, type: 'sect', worldLawIntensity: 3, description: 'Bai Xiaochun\'s first sect. Located on Eastwood Continent. Known for beast taming and spirit cooking.', notableFeatures: ['beastbirth facilities', 'kitchen halls', 'spirit herb gardens', 'Lingxi Abyss portal'], governingFaction: 'Spirit Stream Sect', dangers: ['sect trials', 'Blood Stream Sect rivalry', 'Lingxi Abyss beasts'], resources: ['Beastbirth Seeds', 'spirit herbs', 'culinary ingredients'] },
  { name: 'Blood Stream Sect', nameCn: '血溪宗', novel: 'A Will Eternal', layer: 1, type: 'sect', worldLawIntensity: 3, description: 'The rival sect to Spirit Stream. Known for blood manipulation arts and the Blood Demon Trial.', notableFeatures: ['Blood Demon Trial grounds', 'blood refinement chambers', 'tunnel to Spirit Stream'], governingFaction: 'Blood Stream Sect', dangers: ['blood cultivators', 'Blood Demon Trial', 'sect politics'], resources: ['Blood-Refinement Grass', 'blood essence', 'Blood Stream Secret Art'] },
  { name: 'Dark Mountain Tribe', nameCn: '暗山部落', novel: 'Pursuit of Truth', layer: 1, type: 'city', worldLawIntensity: 2, description: 'Su Ming\'s home. A barbarian tribe in the wilderness. Berserker bloodline traditions.', notableFeatures: ['berserker initiation grounds', 'Dark Mountain Beast territory', 'tribal elder council'], governingFaction: 'Dark Mountain Tribe', dangers: ['Dark Mountain Beasts', 'rival tribes', 'harsh wilderness'], resources: ['berserker bloodline fragments', 'wild herbs', 'beast cores'] },
  { name: 'Sea of Devils', nameCn: '魔海', novel: 'Renegade Immortal', layer: 1, type: 'forbidden_land', worldLawIntensity: 3, description: 'A vast dangerous region on Planet Suzaku. Where Wang Lin found the Mosquito Beast. Filled with demonic cultivators and savage beasts.', notableFeatures: ['Mosquito Beast canyon', 'demonic cultivator hideouts', 'ancient ruins', 'volatile Qi'], dangers: ['Mosquito Beasts', 'Savage Beasts', 'demonic cultivators', 'Qi storms'], resources: ['Mosquito proboscis', 'savage beast cores', 'demonic herbs', 'ancient inheritances'] },
  { name: 'Lingxi Abyss', nameCn: '灵犀深渊', novel: 'A Will Eternal', layer: 1, type: 'secret_realm', worldLawIntensity: 3, description: 'A secret realm beneath the Spirit Stream Sect. Contains the Heavenhorn Ink Dragon and Beast Kings.', notableFeatures: ['Heavenhorn Ink Dragon lair', 'Beast King territories', 'ancient beast chasm'], dangers: ['Heavenspan Divine Crocodile', 'Giant Toad Bi Di', 'ancient beast hoards'], resources: ['Beast King cores', 'Heavenhorn dragon scales', 'ancient beast essences'] },
];

// ═══ LAYER 2: Planet Suzaku True World (L=3.2) ════════════════════
export const LAYER2_LOCATIONS: ErGenLocation[] = [
  { name: 'Planet Suzaku', nameCn: '朱雀星', novel: 'Renegade Immortal', layer: 2, type: 'planet', worldLawIntensity: 3.2, description: 'The True World of Renegade Immortal. A full cultivation planet with multiple countries, sects, and the Sea of Devils.', notableFeatures: ['multiple countries', 'ancient sect ruins', 'spirit vein networks'], dangers: ['high-tier beasts', 'sect wars', 'tribulation zones'], resources: ['mid-tier spirit herbs', 'beast cores', 'spirit ore'] },
  { name: 'Planet South Heaven', nameCn: '南天星', novel: 'ISSTH', layer: 2, type: 'planet', worldLawIntensity: 3.2, description: 'One of the four planets of the Ninth Mountain and Sea. Meng Hao\'s home world. Contains the Southern Domain, Northern Reaches, Eastern Lands, and Western Desert.', notableFeatures: ['four continents', 'Reliance Sect ruins', 'Crow Scout Tribe lands', 'Black Lands'], governingFaction: 'various', dangers: ['demon beasts', 'rival sects', 'ancient curses'], resources: ['spirit herbs', 'demon cores', 'ancient inheritances'] },
  { name: 'Heavenly Fate Sect', nameCn: '天运宗', novel: 'Renegade Immortal', layer: 2, type: 'sect', worldLawIntensity: 3.5, description: 'The sect led by the All-Seer on Planet Tian Yun. Known for divination and fate-manipulation arts.', notableFeatures: ['fate-reading halls', 'Purple Cloud Pavilion', 'All-Seer\'s chambers'], governingFaction: 'Heavenly Fate Sect', dangers: ['the All-Seer\'s schemes', 'fate manipulation', 'sect politics'], resources: ['fate-reading jade slips', 'karma-manipulation techniques', 'divination pills'] },
  { name: 'Cloud Sky Sect', nameCn: '云天宗', novel: 'Renegade Immortal', layer: 2, type: 'sect', worldLawIntensity: 3.2, description: 'A major sect Wang Lin eventually leads. Known for cloud-aspect techniques and the Cloud Beast.', notableFeatures: ['cloud cultivation chambers', 'Cloud Beast habitat', 'sect leader\'s palace'], governingFaction: 'Cloud Sky Sect', dangers: ['Cloud Beasts', 'sect succession wars'], resources: ['Cloud Beast cores', 'cloud-aspect herbs', 'flying sword forges'] },
  { name: 'Demon Immortal Sect', nameCn: '魔仙宗', novel: 'ISSTH', layer: 2, type: 'sect', worldLawIntensity: 3.5, description: 'Ancient ruins of a once-great demon sect. Meng Hao explored its ruins. Contains sealed demon spirits and ancient inheritances.', notableFeatures: ['ancient demon seals', 'ruined halls', 'demon spirit gate'], dangers: ['sealed demons', 'ancient traps', 'demonic Qi'], resources: ['demon spirit cores', 'ancient demon techniques', 'sealed inheritances'] },
  { name: 'Southern Domain', nameCn: '南域', novel: 'ISSTH', layer: 2, type: 'continent', worldLawIntensity: 3.2, description: 'One of the four continents of Planet South Heaven. Contains 2 major cultivation states and numerous sects.', notableFeatures: ['multiple sects', 'spirit vein networks', 'mortal kingdoms'], dangers: ['sect wars', 'demon beasts', 'ancient ruins'], resources: ['spirit herbs', 'beast cores', 'spirit ore'] },
  { name: 'Eastern Lands', nameCn: '东土', novel: 'ISSTH', layer: 2, type: 'continent', worldLawIntensity: 3.2, description: 'The largest continent of Planet South Heaven with 216 states. Center of political power.', notableFeatures: ['216 states', 'imperial court', 'major sect alliances'], dangers: ['political intrigue', 'imperial armies', 'cultivator-king conflicts'], resources: ['imperial-grade herbs', 'royal spirit stones', 'ancient imperial techniques'] },
  { name: 'Starry Sky Dao Polarity Sect', nameCn: '星空道极宗', novel: 'A Will Eternal', layer: 2, type: 'sect', worldLawIntensity: 4, description: 'The ancient sect that controls the eastern cultivation world in AWE. Has four subsidiary courts.', notableFeatures: ['four courts', 'starry sky cultivation chambers', 'Heavenspan Realm gate'], governingFaction: 'Starry Sky Dao Polarity Sect', dangers: ['chosen-level disciples', 'sect politics', 'heaven-span tribulations'], resources: ['starry sky techniques', 'heaven-grade pills', 'cosmic artifacts'] },
  { name: 'Heavenspan Realm', nameCn: '天渊', novel: 'A Will Eternal', layer: 2, type: 'realm', worldLawIntensity: 4, description: 'A massive region in AWE. Contains the Lower Reaches, Middle Reaches, and Wildlands. Separated by the Heavenspan Sea.', notableFeatures: ['Heavenspan Sea', 'Lower/Middle Reaches', 'Wildlands', 'River-Defying Sect'], dangers: ['Wildlands beasts', 'heaven-span cultivators', 'ancient sealed beings'], resources: ['all resource tiers', 'ancient inheritances', 'Wildlands beast cores'] },
  { name: 'Wildlands', nameCn: '蛮荒', novel: 'A Will Eternal', layer: 2, type: 'forbidden_land', worldLawIntensity: 4, description: 'The dangerous frontier of the Heavenspan Realm. Filled with powerful beasts and ancient ruins.', notableFeatures: ['beast hordes', 'ancient ruins', 'tribal settlements'], dangers: ['beast tides', 'ancient beasts', 'harsh environment'], resources: ['high-tier beast cores', 'ancient herbs', 'ruined sect inheritances'] },
];

// ═══ LAYER 3: Azure Expanse Cosmos (L=6) ══════════════════════════
export const LAYER3_LOCATIONS: ErGenLocation[] = [
  { name: 'Immortal Astral Continent', nameCn: '仙罡大陆', novel: 'Renegade Immortal', layer: 3, type: 'continent', worldLawIntensity: 6, description: 'The continent Wang Lin rebuilt. A massive landmass in the Vast Expanse. Law tier 6.', notableFeatures: ['Wang Lin\'s legacy', 'ancient god ruins', 'immortal-grade spirit veins'], dangers: ['immortal-grade beasts', 'ancient god remnants', 'law-level threats'], resources: ['immortal-grade herbs', 'immortal pills', 'Dao Treasures'] },
  { name: 'Mountain and Sea Realm', nameCn: '山海界', novel: 'ISSTH', layer: 3, type: 'realm', worldLawIntensity: 6, description: 'The Nine Mountains and Nine Seas around a Butterfly. Meng Hao\'s realm. A pocket-universe with its own laws.', notableFeatures: ['Nine Mountains', 'Nine Seas', 'Mountain-Sea Butterfly', 'Paragon Immortal World'], dangers: ['Allheaven\'s will', 'Outsider cultivators', 'Paragon-level beasts'], resources: ['Paragon-grade materials', 'Dao fruits', 'Mountain-Sea essence'] },
  { name: 'Paragon Immortal World', nameCn: '至尊仙界', novel: 'ISSTH', layer: 3, type: 'realm', worldLawIntensity: 7, description: 'A major realm within the Vast Expanse. Contains 33 heavens separated by dimensional barriers.', notableFeatures: ['33 heavens', 'dimensional barriers', 'Outsider invasion points'], dangers: ['Outsider cultivators', 'dimensional rifts', 'heaven-level beasts'], resources: ['heaven-grade resources', 'law fragments', 'transcendence materials'] },
  { name: 'Crow Scout Tribe', nameCn: '乌蛮部落', novel: 'ISSTH', layer: 3, type: 'city', worldLawIntensity: 5, description: 'A nomadic tribe in the Black Lands of ISSTH. Where Meng Hao raised Big Hairy (Greenwood Wolf).', notableFeatures: ['nomadic camps', 'beast-taming traditions', 'Blood Mastiff territory'], dangers: ['rival tribes', 'wild beasts', 'harsh desert'], resources: ['Greenwood Wolf cores', 'tribal techniques', 'beast-taming secrets'] },
  { name: 'Black Lands', nameCn: '黑地', novel: 'ISSTH', layer: 3, type: 'forbidden_land', worldLawIntensity: 5, description: 'A dangerous wilderness region. Contains the Ruins of the Ancient Demon Immortal Sect.', notableFeatures: ['Demon Immortal Sect ruins', 'nomadic tribes', 'volatile Qi'], dangers: ['demon spirits', 'ancient traps', 'tribal warfare'], resources: ['demon spirit cores', 'ancient inheritances', 'rare herbs'] },
  { name: 'River-Defying Sect', nameCn: '逆河宗', novel: 'A Will Eternal', layer: 3, type: 'sect', worldLawIntensity: 5, description: 'Bai Xiaochun\'s merged sect (Spirit Stream + Blood Stream). Located at the mouth of the Heavenspan Sea.', notableFeatures: ['merged sect traditions', 'Beastbirth facilities', 'blood manipulation arts'], governingFaction: 'River-Defying Sect', dangers: ['rival sects', 'Wildlands incursions'], resources: ['merged techniques', 'Beastbirth Seeds', 'blood-refinement pills'] },
];

// ═══ LAYER 4: The Vast Expanse (L=10) ═════════════════════════════
export const LAYER4_LOCATIONS: ErGenLocation[] = [
  { name: 'The Vast Expanse', nameCn: '大苍茫', novel: 'All Novels', layer: 4, type: 'void_zone', worldLawIntensity: 10, description: 'The cold gray void between cosmoses. Soul-depletion drains those without Ascendant+ capability. Floating ruins and dead stars drift here.', notableFeatures: ['void friction', 'floating ruins', 'dead stars', 'River of Time', 'spatial tears'], dangers: ['void friction', 'Void Mosquito Swarms', 'Nether Beasts', 'spatial tears', 'Moongazer Serpents'], resources: ['void essence', 'ancient blueprints', 'cosmic materials'] },
  { name: 'River of Time', nameCn: '光阴长河', novel: 'All Novels', layer: 4, type: 'void_zone', worldLawIntensity: 10, description: 'A physical fluid-like stream winding through the Vast Expanse. Contains the historical imprints of every dead world.', notableFeatures: ['echo imprints', 'time-aspect Qi', 'fluid-like voxels'], dangers: ['time distortion', 'karmic leeches', 'Time Echo Phantoms'], resources: ['ancient blueprints', 'technique fragments', 'phantom NPCs', 'historical records'] },
  { name: 'Bridge of Immortality', nameCn: '仙桥', novel: 'ISSTH', layer: 4, type: 'void_zone', worldLawIntensity: 11, description: 'A bridge spanning the Vast Expanse. Crossing it leads to Transcendence. Guarded by heavenly tests.', notableFeatures: ['9 heaven-trampling steps', 'karmic barriers', 'reincarnation cycle'], dangers: ['karmic backlash', 'reincarnation traps', 'heaven-trampling tribulation'], resources: ['Transcendence fragments', 'karmic resolution'] },
  { name: 'Seven-Colored Realm', nameCn: '七彩界', novel: 'Renegade Immortal', layer: 4, type: 'secret_realm', worldLawIntensity: 10, description: 'A pocket dimension within the Nether Beast. Contains forbidden execution techniques.', notableFeatures: ['Nether Beast interior', 'forbidden technique vaults', 'acid fluid'], dangers: ['Nether Beast awakening', 'acid environment', 'parasitic larvae'], resources: ['forbidden execution techniques', 'Nether Core', 'primordial bone'] },
];

// ═══ LAYER 5: The Universe (L=12) ═════════════════════════════════
export const LAYER5_LOCATIONS: ErGenLocation[] = [
  { name: 'The Universe', nameCn: '宇宙', novel: 'All Novels', layer: 5, type: 'realm', worldLawIntensity: 12, description: 'The absolute ceiling of the cosmology. Contains the entirety of the Vast Expanse. Coordinate data types transition to BigInteger.', notableFeatures: ['infinite size', 'River of Space and Time', 'all timelines concurrent'], dangers: ['conceptual entities', 'universe-level threats', 'reality alteration'], resources: ['Source Energy', 'Dao fruits', 'cosmic-level materials'] },
  { name: 'River of Space and Time', nameCn: '时空长河', novel: 'All Novels', layer: 5, type: 'void_zone', worldLawIntensity: 12, description: 'The ultimate macro-stream where all timelines, parallel possibilities, and spatial arrays exist concurrently. Crossover events originate here.', notableFeatures: ['all timelines', 'parallel possibilities', 'cross-over event triggers'], dangers: ['paradox entities', 'timeline collapses', 'fate-weaver spiders'], resources: ['fate-thread silk', 'timeline fragments', 'cosmic ripples'] },
];

export const ALL_LOCATIONS: ErGenLocation[] = [
  ...LAYER1_LOCATIONS,  // 9
  ...LAYER2_LOCATIONS,  // 10
  ...LAYER3_LOCATIONS,  // 6
  ...LAYER4_LOCATIONS,  // 4
  ...LAYER5_LOCATIONS,  // 2
];

export function locationsByLayer(layer: number): ErGenLocation[] {
  return ALL_LOCATIONS.filter((l) => l.layer === layer);
}

export function randomLocation(layer: number, rng: () => number): ErGenLocation {
  const pool = locationsByLayer(layer);
  return pool[Math.floor(rng() * pool.length)] ?? ALL_LOCATIONS[0];
}
