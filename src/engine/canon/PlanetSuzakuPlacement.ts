/**
 * PlanetSuzakuPlacement — mod-original placement of canon locations on the
 * 3D Planet Suzaku world map. The canon novel 仙逆 attests the existence
 * and hierarchy of these locations; the precise (x, z) coordinates are
 * mod-original reconstruction (REASONABLE_RECONSTRUCTION, conf 3).
 *
 * Layout philosophy:
 *   - Wang Lin's home village sits in a quiet Zhao Country valley (the
 *     novel says "a remote mountain village in Zhao Country"; we place it
 *     at the origin of the playable area for narrative anchoring).
 *   - Heng Yue Sect sits on a mountain ~800m to the north-east (Wang Lin
 *     walks there from the village).
 *   - Luo He Sect (Li Muwan's sect) sits across a mountain range to the
 *     west.
 *   - Sea of Devils (修魔海) is the southern coastline and beyond.
 *   - Snow Domain Country (雪域国) is the far-north snowy biome.
 *
 * All positions are in WORLD BLOCKS (1 block = 1 meter).
 */

export interface PlacedLocation {
  canonId: string
  name: string
  nameCn?: string
  position: [number, number] // [x, z] in world blocks
  radius: number
  qiDensity: number // 1-10
  biome: 'plains' | 'mountains' | 'forest' | 'desert' | 'snow' | 'swamp' | 'coast' | 'sea' | 'volcanic'
  /** Type of structure to materialize. */
  structureKind: 'village' | 'sect' | 'city' | 'ruin' | 'cave' | 'landmark'
}

/**
 * The canonical Planet Suzaku placement. Each entry is attested in the
 * novel; the precise coordinates are mod-original (REASONABLE_RECONSTRUCTION).
 */
export const PLANET_SUZAKU_PLACEMENT: PlacedLocation[] = [
  {
    canonId: 'mod:zhao_village',
    name: 'Remote Village in Zhao Country',
    nameCn: '赵国偏僻山村',
    position: [0, 0],
    radius: 60,
    qiDensity: 2,
    biome: 'plains',
    structureKind: 'village',
  },
  {
    canonId: 'mod:heng_yue_sect',
    name: 'Heng Yue Sect',
    nameCn: '恒岳派',
    position: [640, -480],
    radius: 90,
    qiDensity: 6,
    biome: 'mountains',
    structureKind: 'sect',
  },
  {
    canonId: 'mod:luo_he_sect',
    name: 'Luo He Sect',
    nameCn: '洛河门',
    position: [-720, -200],
    radius: 80,
    qiDensity: 6,
    biome: 'mountains',
    structureKind: 'sect',
  },
  {
    canonId: 'mod:xuan_dao_sect',
    name: 'Xuan Dao Sect',
    nameCn: '玄道宗',
    position: [320, -960],
    radius: 90,
    qiDensity: 7,
    biome: 'mountains',
    structureKind: 'sect',
  },
  {
    canonId: 'mod:zhao_country_capital',
    name: 'Zhao Country Capital',
    nameCn: '赵国都城',
    position: [1280, 320],
    radius: 140,
    qiDensity: 3,
    biome: 'plains',
    structureKind: 'city',
  },
  {
    canonId: 'mod:sea_of_devils',
    name: 'Sea of Devils',
    nameCn: '修魔海',
    position: [0, 1800],
    radius: 800,
    qiDensity: 4,
    biome: 'sea',
    structureKind: 'landmark',
  },
  {
    canonId: 'mod:snow_domain_country',
    name: 'Snow Domain Country',
    nameCn: '雪域国',
    position: [0, -2400],
    radius: 600,
    qiDensity: 7,
    biome: 'snow',
    structureKind: 'landmark',
  },
  {
    canonId: 'mod:jue_ming_valley',
    name: 'Jue Ming Valley (Valley of Certain Death)',
    nameCn: '决明谷',
    position: [-1600, 480],
    radius: 120,
    qiDensity: 5,
    biome: 'swamp',
    structureKind: 'landmark',
  },
  {
    canonId: 'mod:ancient_demon_city',
    name: 'Ancient Demon City',
    nameCn: '古魔城',
    position: [-480, 1600],
    radius: 200,
    qiDensity: 5,
    biome: 'desert',
    structureKind: 'city',
  },
  {
    canonId: 'mod:cliff_where_wang_lin_found_bead',
    name: 'Cliff of the Heaven-Defying Bead',
    nameCn: '天逆珠崖',
    position: [-80, -120],
    radius: 30,
    qiDensity: 4,
    biome: 'mountains',
    structureKind: 'landmark',
  },
]

/** Find a placed location by its canon id. */
export function getPlacedLocation(canonId: string): PlacedLocation | undefined {
  return PLANET_SUZAKU_PLACEMENT.find((p) => p.canonId === canonId)
}

/** Find all placed locations whose 2D position is inside a chunk. */
export function placedLocationsInChunk(chunkX: number, chunkZ: number): PlacedLocation[] {
  const minCx = chunkX * 16
  const minCz = chunkZ * 16
  const maxCx = minCx + 15
  const maxCz = minCz + 15
  return PLANET_SUZAKU_PLACEMENT.filter((p) => {
    const [px, pz] = p.position
    return (
      px + p.radius >= minCx &&
      px - p.radius <= maxCx &&
      pz + p.radius >= minCz &&
      pz - p.radius <= maxCz
    )
  })
}

/** The Wang Lin home village — the player spawns here. */
export const SPAWN_LOCATION = PLANET_SUZAKU_PLACEMENT[0]
