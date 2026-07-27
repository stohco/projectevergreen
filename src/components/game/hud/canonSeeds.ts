/**
 * canonSeeds — shared mod-original placement of canon characters, beasts,
 * and spirit veins near the spawn. Used by useEngine (for world-state scans)
 * and by the Minimap (for rendering).
 *
 * Canon fidelity:
 *   - All character names are attested in 仙逆 (drawn from
 *     RICanonicalDatabase — N01 Wang Lin, Li Muwan, Situ Nan, Teng Huayuan,
 *     Zhou Yitang, etc.).
 *   - The precise (x, z) positions are mod-original reconstruction
 *     (REASONABLE_RECONSTRUCTION, conf 3).
 *   - NO invented chapter citations.
 *
 * Beast entries (Ironback Wolf, Frost Mosquito, Mountain Ape) are
 * mod-original ecosystem placeholders grounded in xianxia genre convention
 * (spirit beasts of ranks 1-3 inhabit mortal-tier wilds). They are NOT
 * canon entries from RICanonicalDatabase — flagged here so future CRONs
 * can replace them with canon-attested beasts (e.g. the Mosquito Beast,
 * the Vermilion Bird, the Restriction Sword beast).
 */

export interface SeedActor {
  id: string
  name: string
  nameCn: string
  realm: string
  faction: string
  hostility: number
  pos: [number, number] // [x, z] world blocks
}

export const SEED_ACTORS: SeedActor[] = [
  { id: 'seed:wang-father',  name: "Wang Lin's Father", nameCn: '王林之父',  realm: 'Mortal',                    faction: 'Wang Family',   hostility: 0,  pos: [4, 6] },
  { id: 'seed:wang-mother',  name: "Wang Lin's Mother", nameCn: '王林之母',  realm: 'Mortal',                    faction: 'Wang Family',   hostility: 0,  pos: [4, 8] },
  { id: 'seed:fourth-uncle', name: 'Fourth Uncle',       nameCn: '四叔',      realm: 'Mortal',                    faction: 'Wang Family',   hostility: 5,  pos: [-12, 14] },
  { id: 'seed:wang-hao',     name: 'Wang Hao',           nameCn: '王浩',      realm: 'Qi Condensation',           faction: 'Wang Family',   hostility: 60, pos: [6, -10] },
  { id: 'seed:zhou-yi-tang', name: 'Zhou Yitang',        nameCn: '周一堂',    realm: 'Foundation Establishment',  faction: 'Heng Yue Sect', hostility: 25, pos: [640, -480] },
  { id: 'seed:li-muwan',     name: 'Li Muwan',           nameCn: '李慕婉',    realm: 'Foundation Establishment',  faction: 'Luo He Sect',   hostility: 0,  pos: [-720, -200] },
  { id: 'seed:situ-nan',     name: 'Situ Nan',           nameCn: '司徒南',    realm: 'Nascent Soul',              faction: 'Independent',   hostility: 15, pos: [-80, -120] },
  { id: 'seed:teng-huayuan', name: 'Teng Huayuan',       nameCn: '滕化元',    realm: 'Core Formation',            faction: 'Teng Family',   hostility: 85, pos: [1280, 320] },
  { id: 'seed:qiu-siping',   name: 'Qiu Siping',         nameCn: '邱思平',    realm: 'Foundation Establishment',  faction: 'Heng Yue Sect', hostility: 30, pos: [620, -460] },
]

export interface SeedThreat {
  beastName: string
  beastNameCn: string
  rank: string
  pos: [number, number]
  count: number
}

export const SEED_THREATS: SeedThreat[] = [
  { beastName: 'Ironback Wolf',  beastNameCn: '铁背狼',  rank: 'Rank 1', pos: [120, -60],   count: 3 },
  { beastName: 'Frost Mosquito', beastNameCn: '寒霜蚊',  rank: 'Rank 2', pos: [-200, 180],  count: 8 },
  { beastName: 'Mountain Ape',   beastNameCn: '山猿',    rank: 'Rank 3', pos: [540, -420],  count: 1 },
  { beastName: 'Ironback Wolf',  beastNameCn: '铁背狼',  rank: 'Rank 1', pos: [-90, 220],   count: 2 },
]

export interface SeedVein {
  name: string
  position: [number, number]
  quality: number
  element: string
}

export const SEED_VEINS: SeedVein[] = [
  { name: 'Eastern Stream Vein',  position: [80, 40],     quality: 4, element: 'water' },
  { name: 'Jade Mountain Vein',   position: [640, -480],  quality: 7, element: 'wood' },
  { name: 'Bitter Cliff Vein',    position: [-80, -120],  quality: 5, element: 'earth' },
  { name: 'Luomen Vein',          position: [-720, -200], quality: 6, element: 'metal' },
  { name: 'Snowdrift Vein',       position: [0, -2400],   quality: 8, element: 'ice' },
]
