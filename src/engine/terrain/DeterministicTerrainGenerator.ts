/**
 * DeterministicTerrainGenerator — deterministic, blueprint-constrained
 * terrain. Port of dev.ergenverse.world.DeterministicTerrainGenerator.
 *
 * Layered simplex noise (heightmap + temperature + humidity + mountain
 * mask) produces biomes. Canon placements bias terrain toward the
 * attested biome (Heng Yue Sect gets jagged mountains, Sea of Devils
 * gets ocean, etc.).
 *
 * Surface decoration (grass, flowers, trees, snow layers) is also
 * deterministic.
 *
 * 100% deterministic: same seed → same world, every browser, every reload.
 */
import type { VoxelChunk } from '../voxels/VoxelChunk'
import { CHUNK_SIZE_X, CHUNK_SIZE_Y, CHUNK_SIZE_Z, SEA_LEVEL } from '../voxels/VoxelChunk'
import { BlockId } from '../voxels/BlockRegistry'
import { canonNoise2D } from './DeterministicSeedHandler'
import { PLANET_SUZAKU_PLACEMENT, type PlacedLocation } from '../canon/PlanetSuzakuPlacement'

export type Biome =
  | 'plains'
  | 'mountains'
  | 'forest'
  | 'desert'
  | 'snow'
  | 'swamp'
  | 'coast'
  | 'sea'
  | 'volcanic'

export interface BiomeSample {
  biome: Biome
  height: number
  temperature: number // 0..1
  humidity: number // 0..1
  influence?: PlacedLocation
}

// Layered noise generators (deterministic per name).
const nElev = canonNoise2D('elevation')
const nElev2 = canonNoise2D('elevation.detail')
const nMountain = canonNoise2D('mountain.mask')
const nTemp = canonNoise2D('temperature')
const nHum = canonNoise2D('humidity')
const nTree = canonNoise2D('tree')
const nBush = canonNoise2D('bush')
const nFlower = canonNoise2D('flower')
const nSnow = canonNoise2D('snow.drift')

/** Sample the biome + heightmap at a world (x, z). */
export function sampleBiome(worldX: number, worldZ: number): BiomeSample {
  // Distance from spawn village origin (0,0) — Wang Lin's birthplace.
  const distFromSpawn = Math.sqrt(worldX * worldX + worldZ * worldZ)
  const SPAWN_FLATTEN_RADIUS = 48 // blocks around spawn kept gentle

  // Base elevation: large-scale rolling hills.
  const baseElev =
    nElev(worldX * 0.0035, worldZ * 0.0035) * 12 +
    nElev2(worldX * 0.012, worldZ * 0.012) * 4
  // Mountain mask: ridges at certain spots — suppressed near spawn.
  const mountainMask = Math.max(0, nMountain(worldX * 0.0025, worldZ * 0.0025))
  let mountainBonus = mountainMask * mountainMask * 60
  // Flatten near spawn: taper mountain bonus to zero within SPAWN_FLATTEN_RADIUS.
  if (distFromSpawn < SPAWN_FLATTEN_RADIUS) {
    const t = distFromSpawn / SPAWN_FLATTEN_RADIUS
    const flatten = 1 - (1 - t) * (1 - t) // quadratic falloff
    mountainBonus *= flatten
  }
  let height = SEA_LEVEL + 4 + baseElev + mountainBonus
  // Near spawn, clamp to a gentle plain so the village is walkable.
  if (distFromSpawn < SPAWN_FLATTEN_RADIUS) {
    const targetPlain = SEA_LEVEL + 8 // y=64 — a flat shelf above sea level
    const t = Math.min(1, distFromSpawn / SPAWN_FLATTEN_RADIUS)
    const blend = 1 - t * t // strong flatten at center, tapers out
    height = height * (1 - blend * 0.85) + targetPlain * (blend * 0.85)
  }
  let temp = 0.5 + nTemp(worldX * 0.0015, worldZ * 0.0015) * 0.5
  let hum = 0.5 + nHum(worldX * 0.0017, worldZ * 0.0017) * 0.5
  temp = Math.max(0, Math.min(1, temp))
  hum = Math.max(0, Math.min(1, hum))

  let biome: Biome = 'plains'
  if (temp < 0.25) biome = 'snow'
  else if (temp > 0.78 && hum < 0.3) biome = 'desert'
  else if (hum > 0.7 && temp > 0.5) biome = 'swamp'
  else if (hum > 0.55 && temp > 0.4 && temp < 0.75) biome = 'forest'
  else biome = 'plains'
  if (mountainBonus > 30) biome = 'mountains'
  if (height < SEA_LEVEL) {
    biome = height < SEA_LEVEL - 4 ? 'sea' : 'coast'
  }
  // Force plains near spawn — no mountain biome at the village.
  if (distFromSpawn < SPAWN_FLATTEN_RADIUS && biome === 'mountains') {
    biome = 'plains'
  }

  // Canon placement bias: scan all placements, find the strongest influence.
  let influence: PlacedLocation | undefined
  let bestScore = 0
  for (const loc of PLANET_SUZAKU_PLACEMENT) {
    const dx = worldX - loc.position[0]
    const dz = worldZ - loc.position[1]
    const d = Math.sqrt(dx * dx + dz * dz)
    const reach = loc.radius * 2.2
    if (d > reach) continue
    const score = (1 - d / reach) * loc.radius
    if (score > bestScore) {
      bestScore = score
      influence = loc
    }
  }
  if (influence) {
    const canonBiome = influence.biome
    const blend = Math.max(0, Math.min(1, bestScore / 60))
    if (blend > 0.4) {
      biome = canonBiome
      if (canonBiome === 'mountains') {
        height = Math.max(height, SEA_LEVEL + 30 + mountainBonus)
      } else if (canonBiome === 'sea') {
        height = Math.min(height, SEA_LEVEL - 6)
      } else if (canonBiome === 'snow') {
        height = Math.max(height, SEA_LEVEL + 20 + mountainBonus)
      } else if (canonBiome === 'swamp') {
        height = SEA_LEVEL + 2 + baseElev
      } else if (canonBiome === 'desert') {
        height = SEA_LEVEL + 6 + baseElev
      }
    }
  }
  // Volcanic biome near Ancient Demon City.
  if (influence && influence.canonId === 'mod:ancient_demon_city' && bestScore > 30) {
    biome = 'volcanic'
  }

  return { biome, height: Math.round(height), temperature: temp, humidity: hum, influence }
}

/** Surface block for biome. */
function surfaceBlock(biome: Biome): number {
  switch (biome) {
    case 'plains': return BlockId.GRASS
    case 'mountains': return BlockId.STONE
    case 'forest': return BlockId.GRASS
    case 'desert': return BlockId.SAND
    case 'snow': return BlockId.SNOW
    case 'swamp': return BlockId.SWAMP_MUD
    case 'coast': return BlockId.SAND
    case 'sea': return BlockId.DIRT
    case 'volcanic': return BlockId.VOLCANIC_ROCK
  }
}

/** Sub-surface block for biome. */
function subSurfaceBlock(biome: Biome): number {
  switch (biome) {
    case 'desert':
    case 'coast': return BlockId.SAND
    case 'snow': return BlockId.DIRT
    case 'volcanic': return BlockId.STONE
    case 'mountains': return BlockId.STONE
    default: return BlockId.DIRT
  }
}

/** Fill a chunk with the deterministic terrain + decoration. */
export function generateChunk(chunk: VoxelChunk, chunkX: number, chunkZ: number): void {
  for (let lx = 0; lx < CHUNK_SIZE_X; lx++) {
    for (let lz = 0; lz < CHUNK_SIZE_Z; lz++) {
      const wx = chunkX * CHUNK_SIZE_X + lx
      const wz = chunkZ * CHUNK_SIZE_Z + lz
      const sample = sampleBiome(wx, wz)
      const height = Math.max(1, Math.min(CHUNK_SIZE_Y - 8, sample.height))

      // Bedrock floor at y=0.
      chunk.set(lx, 0, lz, BlockId.BEDROCK)

      // Subsurface up to height - 1.
      const sub = subSurfaceBlock(sample.biome)
      for (let y = 1; y < height - 1; y++) {
        if (sub === BlockId.STONE) {
          const oreNoise = nElev2(wx * 0.5 + y * 0.3, wz * 0.5)
          if (y < 16 && oreNoise > 0.85) chunk.set(lx, y, lz, BlockId.GOLD_ORE)
          else if (y < 32 && oreNoise < -0.85) chunk.set(lx, y, lz, BlockId.IRON_ORE)
          else if (oreNoise > 0.6 && oreNoise < 0.7) chunk.set(lx, y, lz, BlockId.JADE_ORE)
          else chunk.set(lx, y, lz, sub)
        } else {
          chunk.set(lx, y, lz, sub)
        }
      }

      // Surface block.
      const surf = surfaceBlock(sample.biome)
      chunk.set(lx, height - 1, lz, surf)

      // Snow cap on high altitude.
      if (sample.biome !== 'snow' && sample.biome !== 'desert' && sample.biome !== 'volcanic' && sample.biome !== 'sea') {
        if (height > SEA_LEVEL + 35 || sample.temperature < 0.25) {
          chunk.set(lx, height, lz, BlockId.SNOW_CAP)
        }
      }

      // Water fill up to sea level.
      if (height < SEA_LEVEL) {
        for (let y = height; y < SEA_LEVEL; y++) {
          chunk.set(lx, y, lz, BlockId.WATER)
        }
      }

      // Spirit vein ore deep underground near sects.
      if (sample.influence && sample.influence.structureKind === 'sect') {
        for (let y = 4; y < 16; y++) {
          const veinNoise = nElev2(wx * 0.3 + y * 0.7, wz * 0.3)
          if (veinNoise > 0.9) chunk.set(lx, y, lz, BlockId.SPIRIT_VEIN_ORE)
        }
      }

      // Decoration: trees in forest/plains.
      // Near spawn, plant a spirit-pine grove at moderate density so the village
      // feels sheltered but the camera sightlines stay clear.
      const distFromSpawn2 = Math.sqrt(wx * wx + wz * wz)
      const nearSpawn = distFromSpawn2 < 48
      if (sample.biome === 'forest' || (sample.biome === 'plains' && (sample.humidity > 0.6 || nearSpawn))) {
        // Trees avoid the village plaza (20-block radius around origin) so the
        // cultivator + camera sightlines + future huts have open ground.
        const inPlaza = distFromSpawn2 < 20
        const treeThreshold = nearSpawn ? 0.78 : 0.75
        const treeNoise = nTree(wx * 0.7, wz * 0.7)
        if (!inPlaza && treeNoise > treeThreshold && height > SEA_LEVEL && height < CHUNK_SIZE_Y - 16) {
          plantTree(chunk, lx, height, lz, nearSpawn ? 'pine' : (sample.biome === 'plains' ? 'pine' : pickForestTree(wx, wz)))
        }
        if (nBush(wx * 1.1, wz * 1.1) > 0.85 && !inPlaza) {
          chunk.set(lx, height, lz, BlockId.PINE_LEAVES)
        }
      } else if (sample.biome === 'swamp' && nTree(wx * 0.6, wz * 0.6) > 0.8) {
        chunk.set(lx, height, lz, BlockId.PINE_BARK)
        chunk.set(lx, height + 1, lz, BlockId.PINE_BARK)
        chunk.set(lx, height + 2, lz, BlockId.PINE_BARK)
      } else if (sample.biome === 'snow' && nTree(wx * 0.4, wz * 0.4) > 0.88) {
        plantTree(chunk, lx, height, lz, 'pine')
      }

      // Spirit-grass in plains.
      if (sample.biome === 'plains') {
        const flowerNoise = nFlower(wx * 1.5, wz * 1.5)
        if (flowerNoise > 0.92) chunk.set(lx, height, lz, BlockId.QI_CRYSTAL)
      }

      // Snow drifts in snow biome.
      if (sample.biome === 'snow') {
        const drift = Math.floor(nSnow(wx * 0.3, wz * 0.3) * 3)
        for (let d = 0; d < drift; d++) {
          chunk.set(lx, height + d, lz, BlockId.SNOW)
        }
      }
    }
  }
  chunk.dirty = true
  chunk.generated = true
  // Tag the dominant biome for HUD display.
  const centerSample = sampleBiome(chunkX * CHUNK_SIZE_X + 8, chunkZ * CHUNK_SIZE_Z + 8)
  chunk.biomeTag = centerSample.biome
}

function pickForestTree(wx: number, wz: number): 'pine' | 'willow' | 'bamboo' | 'jade' {
  const n = nTree(wx * 0.13, wz * 0.13)
  if (n > 0.5) return 'pine'
  if (n > 0.0) return 'willow'
  if (n > -0.5) return 'bamboo'
  return 'jade'
}

function plantTree(chunk: VoxelChunk, lx: number, baseY: number, lz: number, kind: 'pine' | 'willow' | 'bamboo' | 'jade'): void {
  const trunkH = kind === 'bamboo' ? 8 : kind === 'pine' ? 7 : 6
  const woodBlock = kind === 'pine' ? BlockId.PINE_WOOD
    : kind === 'willow' ? BlockId.WILLOW_WOOD
    : kind === 'bamboo' ? BlockId.BAMBOO
    : BlockId.JADE_WOOD
  const leafBlock = kind === 'pine' ? BlockId.PINE_LEAVES
    : kind === 'willow' ? BlockId.WILLOW_LEAVES
    : kind === 'bamboo' ? BlockId.BAMBOO_LEAVES
    : BlockId.JADE_WOOD_LEAVES

  for (let y = 0; y < trunkH; y++) {
    chunk.set(lx, baseY + y, lz, woodBlock)
  }
  const canopyBase = baseY + trunkH - 2
  const canopyTop = baseY + trunkH + 1
  for (let y = canopyBase; y <= canopyTop; y++) {
    const r = y === canopyTop ? 1 : 2
    for (let dx = -r; dx <= r; dx++) {
      for (let dz = -r; dz <= r; dz++) {
        if (dx === 0 && dz === 0 && y < canopyTop) continue
        const dist = Math.abs(dx) + Math.abs(dz)
        if (dist > r + 1) continue
        const lx2 = lx + dx
        const lz2 = lz + dz
        if (lx2 < 0 || lx2 >= 16 || lz2 < 0 || lz2 >= 16) continue
        const existing = chunk.get(lx2, y, lz2)
        if (existing === 0) {
          chunk.set(lx2, y, lz2, leafBlock)
        }
      }
    }
  }
}
