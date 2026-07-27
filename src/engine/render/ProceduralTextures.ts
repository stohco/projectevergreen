/**
 * ProceduralTextures — generates AAA PBR textures procedurally on a canvas
 * (no external image fetches — keeps the project self-contained).
 *
 * Produces a single atlas texture (albedo) + a flat-perturbed normal atlas.
 * Atlas layout matches BlockRegistry.ATLAS_TILES_PER_ROW (8 cols) and
 * ATLAS_TILE_PIXELS (256). Tile index order matches BlockRegistry.TILE_KEYS.
 *
 * Each tile is 256x256, mipmapped, tileable (seamless).
 */
import * as THREE from 'three'
import { ATLAS_TILES_PER_ROW, ATLAS_TILE_PIXELS, TILE_KEYS } from '../voxels/BlockRegistry'

export const TILE_SIZE = ATLAS_TILE_PIXELS
export const ATLAS_COLS = ATLAS_TILES_PER_ROW
export const ATLAS_ROWS = ATLAS_TILES_PER_ROW
export const ATLAS_TILES = ATLAS_COLS * ATLAS_ROWS

function createCanvas(size: number): [HTMLCanvasElement, CanvasRenderingContext2D] {
  const c = document.createElement('canvas')
  c.width = size
  c.height = size
  const ctx = c.getContext('2d')!
  return [c, ctx]
}

function rng(seed: number): () => number {
  let s = seed >>> 0
  return () => {
    s = (s * 1664525 + 1013904223) >>> 0
    return s / 0xffffffff
  }
}

function tileableNoise(
  ctx: CanvasRenderingContext2D,
  size: number,
  seed: number,
  scale: number,
  baseColor: [number, number, number],
  variation: number,
  ox = 0,
  oy = 0,
) {
  const rand = rng(seed)
  const img = ctx.createImageData(size, size)
  const grid = Math.max(2, Math.floor(size / scale))
  const cells: Array<[number, number, number]> = []
  for (let i = 0; i < grid * grid; i++) {
    const v = (rand() - 0.5) * 2 * variation
    cells.push([
      Math.max(0, Math.min(255, baseColor[0] + v)),
      Math.max(0, Math.min(255, baseColor[1] + v)),
      Math.max(0, Math.min(255, baseColor[2] + v)),
    ])
  }
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      const gx = (x / size) * grid
      const gy = (y / size) * grid
      const x0 = Math.floor(gx) % grid
      const y0 = Math.floor(gy) % grid
      const x1 = (x0 + 1) % grid
      const y1 = (y0 + 1) % grid
      const fx = gx - Math.floor(gx)
      const fy = gy - Math.floor(gy)
      const sx = fx * fx * (3 - 2 * fx)
      const sy = fy * fy * (3 - 2 * fy)
      const c00 = cells[y0 * grid + x0]
      const c10 = cells[y0 * grid + x1]
      const c01 = cells[y1 * grid + x0]
      const c11 = cells[y1 * grid + x1]
      const r = c00[0] * (1 - sx) * (1 - sy) + c10[0] * sx * (1 - sy) + c01[0] * (1 - sx) * sy + c11[0] * sx * sy
      const g = c00[1] * (1 - sx) * (1 - sy) + c10[1] * sx * (1 - sy) + c01[1] * (1 - sx) * sy + c11[1] * sx * sy
      const b = c00[2] * (1 - sx) * (1 - sy) + c10[2] * sx * (1 - sy) + c01[2] * (1 - sx) * sy + c11[2] * sx * sy
      const i = (y * size + x) * 4
      img.data[i] = r
      img.data[i + 1] = g
      img.data[i + 2] = b
      img.data[i + 3] = 255
    }
  }
  // putImageData ignores ctx transforms, so use ox/oy to write at the absolute tile position.
  ctx.putImageData(img, ox, oy)
}

function drawTile(
  ctx: CanvasRenderingContext2D,
  tileIndex: number,
  drawer: (tileCtx: CanvasRenderingContext2D, ox: number, oy: number) => void,
) {
  const col = tileIndex % ATLAS_COLS
  const row = Math.floor(tileIndex / ATLAS_COLS)
  const px = col * TILE_SIZE
  const py = row * TILE_SIZE
  ctx.save()
  ctx.beginPath()
  ctx.rect(px, py, TILE_SIZE, TILE_SIZE)
  ctx.clip()
  // Translate so the painter can use 0..TILE_SIZE coords with normal drawing ops,
  // but also pass ox/oy for putImageData (which ignores transforms).
  ctx.translate(px, py)
  drawer(ctx, px, py)
  ctx.restore()
}

type Painter = (ctx: CanvasRenderingContext2D, seed: number, ox: number, oy: number) => void

const PAINTERS: Record<string, Painter> = {
  stone: (ctx, s, ox, oy) => tileableNoise(ctx, TILE_SIZE, s, 32, [110, 110, 115], 25, ox, oy),
  marble: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 24, [230, 230, 235], 12, ox, oy)
    ctx.strokeStyle = 'rgba(120,140,160,0.6)'
    ctx.lineWidth = 2
    const rand = rng(s + 1)
    for (let i = 0; i < 6; i++) {
      ctx.beginPath()
      ctx.moveTo(rand() * TILE_SIZE, 0)
      ctx.bezierCurveTo(rand() * TILE_SIZE, TILE_SIZE * 0.33, rand() * TILE_SIZE, TILE_SIZE * 0.66, rand() * TILE_SIZE, TILE_SIZE)
      ctx.stroke()
    }
  },
  jade_stone: (ctx, s, ox, oy) => tileableNoise(ctx, TILE_SIZE, s, 28, [120, 180, 140], 25, ox, oy),
  spirit_vein_ore: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 30, [80, 110, 100], 30, ox, oy)
    const rand = rng(s + 7)
    for (let i = 0; i < 12; i++) {
      const x = rand() * TILE_SIZE
      const y = rand() * TILE_SIZE
      const r = 4 + rand() * 8
      const grad = ctx.createRadialGradient(x, y, 0, x, y, r)
      grad.addColorStop(0, 'rgba(180, 255, 200, 0.9)')
      grad.addColorStop(1, 'rgba(80, 220, 130, 0)')
      ctx.fillStyle = grad
      ctx.beginPath()
      ctx.arc(x, y, r, 0, Math.PI * 2)
      ctx.fill()
    }
  },
  dirt: (ctx, s, ox, oy) => tileableNoise(ctx, TILE_SIZE, s, 20, [110, 80, 55], 20, ox, oy),
  grass_top: (ctx, s, ox, oy) => tileableNoise(ctx, TILE_SIZE, s, 16, [95, 145, 75], 18, ox, oy),
  grass_side: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 20, [110, 80, 55], 20, ox, oy)
    const grad = ctx.createLinearGradient(0, 0, 0, 50)
    grad.addColorStop(0, 'rgba(95,145,75,1)')
    grad.addColorStop(1, 'rgba(95,145,75,0)')
    ctx.fillStyle = grad
    ctx.fillRect(0, 0, TILE_SIZE, 50)
    const rand = rng(s + 3)
    ctx.strokeStyle = 'rgba(70,120,60,0.8)'
    ctx.lineWidth = 1
    for (let i = 0; i < 80; i++) {
      const x = rand() * TILE_SIZE
      const h = 4 + rand() * 6
      ctx.beginPath()
      ctx.moveTo(x, 0)
      ctx.lineTo(x + (rand() - 0.5) * 4, h)
      ctx.stroke()
    }
  },
  sand: (ctx, s, ox, oy) => tileableNoise(ctx, TILE_SIZE, s, 18, [225, 205, 145], 12, ox, oy),
  snow: (ctx, s, ox, oy) => tileableNoise(ctx, TILE_SIZE, s, 24, [240, 245, 250], 8, ox, oy),
  swamp_mud: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 18, [70, 65, 50], 18, ox, oy)
    const rand = rng(s + 5)
    ctx.fillStyle = 'rgba(40,55,40,0.5)'
    for (let i = 0; i < 20; i++) {
      ctx.beginPath()
      ctx.arc(rand() * TILE_SIZE, rand() * TILE_SIZE, 2 + rand() * 4, 0, Math.PI * 2)
      ctx.fill()
    }
  },
  pine_wood_side: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 40, [110, 80, 55], 10, ox, oy)
    const rand = rng(s + 2)
    for (let x = 0; x < TILE_SIZE; x += 6) {
      ctx.strokeStyle = `rgba(80,55,35,${0.3 + rand() * 0.4})`
      ctx.lineWidth = 1 + rand() * 2
      ctx.beginPath()
      ctx.moveTo(x, 0)
      ctx.lineTo(x + (rand() - 0.5) * 4, TILE_SIZE)
      ctx.stroke()
    }
  },
  pine_wood_top: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 36, [120, 85, 55], 12, ox, oy)
    ctx.strokeStyle = 'rgba(80,55,35,0.4)'
    for (let r = 20; r < TILE_SIZE; r += 24) {
      ctx.beginPath()
      ctx.arc(TILE_SIZE / 2, TILE_SIZE / 2, r, 0, Math.PI * 2)
      ctx.stroke()
    }
  },
  willow_wood_side: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 40, [140, 110, 75], 8, ox, oy)
    const rand = rng(s + 2)
    for (let x = 0; x < TILE_SIZE; x += 8) {
      ctx.strokeStyle = `rgba(110,80,55,${0.25 + rand() * 0.3})`
      ctx.lineWidth = 1
      ctx.beginPath()
      ctx.moveTo(x, 0)
      ctx.bezierCurveTo(x + 2, TILE_SIZE / 3, x - 2, TILE_SIZE * 2 / 3, x, TILE_SIZE)
      ctx.stroke()
    }
  },
  willow_wood_top: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 36, [150, 115, 80], 10, ox, oy)
    ctx.strokeStyle = 'rgba(100,70,45,0.4)'
    for (let r = 20; r < TILE_SIZE; r += 24) {
      ctx.beginPath()
      ctx.arc(TILE_SIZE / 2, TILE_SIZE / 2, r, 0, Math.PI * 2)
      ctx.stroke()
    }
  },
  bamboo_side: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 32, [150, 175, 95], 6, ox, oy)
    ctx.strokeStyle = 'rgba(80,100,50,0.6)'
    ctx.lineWidth = 2
    for (let y = 32; y < TILE_SIZE; y += 64) {
      ctx.beginPath()
      ctx.moveTo(0, y)
      ctx.lineTo(TILE_SIZE, y)
      ctx.stroke()
    }
  },
  bamboo_top: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 32, [155, 180, 100], 6, ox, oy)
    ctx.strokeStyle = 'rgba(80,100,50,0.5)'
    ctx.lineWidth = 2
    ctx.beginPath()
    ctx.arc(TILE_SIZE / 2, TILE_SIZE / 2, TILE_SIZE / 3, 0, Math.PI * 2)
    ctx.stroke()
  },
  jade_wood_side: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 40, [130, 170, 130], 10, ox, oy)
    ctx.strokeStyle = 'rgba(90,140,100,0.4)'
    for (let x = 0; x < TILE_SIZE; x += 8) {
      ctx.beginPath()
      ctx.moveTo(x, 0)
      ctx.lineTo(x, TILE_SIZE)
      ctx.stroke()
    }
  },
  jade_wood_top: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 36, [140, 180, 140], 10, ox, oy)
    ctx.strokeStyle = 'rgba(90,140,100,0.4)'
    for (let r = 20; r < TILE_SIZE; r += 24) {
      ctx.beginPath()
      ctx.arc(TILE_SIZE / 2, TILE_SIZE / 2, r, 0, Math.PI * 2)
      ctx.stroke()
    }
  },
  pine_leaves: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 12, [50, 95, 60], 25, ox, oy)
    const rand = rng(s + 4)
    ctx.fillStyle = 'rgba(35,75,45,0.6)'
    for (let i = 0; i < 60; i++) {
      ctx.beginPath()
      ctx.arc(rand() * TILE_SIZE, rand() * TILE_SIZE, 3 + rand() * 4, 0, Math.PI * 2)
      ctx.fill()
    }
  },
  willow_leaves: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 14, [110, 160, 90], 20, ox, oy)
    const rand = rng(s + 4)
    ctx.strokeStyle = 'rgba(70,120,60,0.7)'
    ctx.lineWidth = 1
    for (let i = 0; i < 40; i++) {
      const x = rand() * TILE_SIZE
      const y = rand() * TILE_SIZE
      ctx.beginPath()
      ctx.moveTo(x, y)
      ctx.lineTo(x + (rand() - 0.5) * 8, y + 8 + rand() * 4)
      ctx.stroke()
    }
  },
  bamboo_leaves: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 10, [105, 155, 80], 18, ox, oy)
    const rand = rng(s + 4)
    ctx.strokeStyle = 'rgba(75,125,55,0.7)'
    ctx.lineWidth = 2
    for (let i = 0; i < 30; i++) {
      const x = rand() * TILE_SIZE
      const y = rand() * TILE_SIZE
      const len = 8 + rand() * 8
      const ang = rand() * Math.PI
      ctx.beginPath()
      ctx.moveTo(x, y)
      ctx.lineTo(x + Math.cos(ang) * len, y + Math.sin(ang) * len)
      ctx.stroke()
    }
  },
  jade_wood_leaves: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 12, [120, 200, 130], 22, ox, oy)
    const rand = rng(s + 4)
    ctx.fillStyle = 'rgba(80,180,110,0.5)'
    for (let i = 0; i < 50; i++) {
      ctx.beginPath()
      ctx.arc(rand() * TILE_SIZE, rand() * TILE_SIZE, 3 + rand() * 5, 0, Math.PI * 2)
      ctx.fill()
    }
  },
  stone_bricks: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 30, [120, 120, 125], 8, ox, oy)
    ctx.strokeStyle = 'rgba(70,70,75,0.7)'
    ctx.lineWidth = 2
    for (let y = 0; y < TILE_SIZE; y += 32) {
      ctx.beginPath()
      ctx.moveTo(0, y)
      ctx.lineTo(TILE_SIZE, y)
      ctx.stroke()
    }
    for (let y = 0; y < TILE_SIZE; y += 32) {
      const offset = (y / 32) % 2 === 0 ? 0 : 32
      for (let x = offset; x < TILE_SIZE; x += 64) {
        ctx.beginPath()
        ctx.moveTo(x, y)
        ctx.lineTo(x, y + 32)
        ctx.stroke()
      }
    }
  },
  jade_bricks: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 30, [130, 180, 145], 8, ox, oy)
    ctx.strokeStyle = 'rgba(70,120,90,0.7)'
    ctx.lineWidth = 2
    for (let y = 0; y < TILE_SIZE; y += 32) {
      ctx.beginPath()
      ctx.moveTo(0, y)
      ctx.lineTo(TILE_SIZE, y)
      ctx.stroke()
    }
    for (let y = 0; y < TILE_SIZE; y += 32) {
      const offset = (y / 32) % 2 === 0 ? 0 : 32
      for (let x = offset; x < TILE_SIZE; x += 64) {
        ctx.beginPath()
        ctx.moveTo(x, y)
        ctx.lineTo(x, y + 32)
        ctx.stroke()
      }
    }
  },
  planks: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 40, [145, 105, 70], 8, ox, oy)
    ctx.strokeStyle = 'rgba(95,65,40,0.6)'
    ctx.lineWidth = 1
    for (let y = 0; y < TILE_SIZE; y += 32) {
      ctx.beginPath()
      ctx.moveTo(0, y)
      ctx.lineTo(TILE_SIZE, y)
      ctx.stroke()
    }
    const rand = rng(s + 2)
    for (let y = 0; y < TILE_SIZE; y += 32) {
      const x = rand() * TILE_SIZE
      ctx.beginPath()
      ctx.moveTo(x, y)
      ctx.lineTo(x, y + 32)
      ctx.stroke()
    }
  },
  paper_wall: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 50, [235, 225, 195], 6, ox, oy)
    ctx.strokeStyle = 'rgba(80,55,35,0.7)'
    ctx.lineWidth = 4
    for (let i = 0; i <= TILE_SIZE; i += 64) {
      ctx.beginPath()
      ctx.moveTo(0, i)
      ctx.lineTo(TILE_SIZE, i)
      ctx.stroke()
      ctx.beginPath()
      ctx.moveTo(i, 0)
      ctx.lineTo(i, TILE_SIZE)
      ctx.stroke()
    }
  },
  tiled_roof: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 24, [165, 85, 55], 10, ox, oy)
    ctx.strokeStyle = 'rgba(95,45,30,0.7)'
    ctx.lineWidth = 2
    for (let y = 0; y < TILE_SIZE; y += 16) {
      ctx.beginPath()
      ctx.moveTo(0, y)
      ctx.lineTo(TILE_SIZE, y)
      ctx.stroke()
    }
    for (let x = 0; x < TILE_SIZE; x += 32) {
      for (let y = 0; y < TILE_SIZE; y += 16) {
        const off = (y / 16) % 2 === 0 ? 0 : 16
        ctx.beginPath()
        ctx.moveTo(x + off, y)
        ctx.lineTo(x + off, y + 16)
        ctx.stroke()
      }
    }
  },
  water: (ctx, s, ox, oy) => tileableNoise(ctx, TILE_SIZE, s, 32, [60, 130, 180], 12, ox, oy),
  deep_water: (ctx, s, ox, oy) => tileableNoise(ctx, TILE_SIZE, s, 32, [30, 80, 130], 10, ox, oy),
  lava: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 24, [220, 90, 30], 25, ox, oy)
    const rand = rng(s + 3)
    ctx.fillStyle = 'rgba(255,200,80,0.8)'
    for (let i = 0; i < 30; i++) {
      ctx.beginPath()
      ctx.arc(rand() * TILE_SIZE, rand() * TILE_SIZE, 3 + rand() * 6, 0, Math.PI * 2)
      ctx.fill()
    }
  },
  qi_crystal: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 20, [120, 220, 160], 30, ox, oy)
    const rand = rng(s + 7)
    ctx.strokeStyle = 'rgba(180,255,200,0.9)'
    ctx.lineWidth = 1.5
    for (let i = 0; i < 12; i++) {
      ctx.beginPath()
      ctx.moveTo(rand() * TILE_SIZE, rand() * TILE_SIZE)
      ctx.lineTo(rand() * TILE_SIZE, rand() * TILE_SIZE)
      ctx.stroke()
    }
  },
  spirit_vein_glow: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 24, [40, 100, 70], 25, ox, oy)
    const rand = rng(s + 7)
    for (let i = 0; i < 25; i++) {
      const x = rand() * TILE_SIZE
      const y = rand() * TILE_SIZE
      const r = 6 + rand() * 14
      const grad = ctx.createRadialGradient(x, y, 0, x, y, r)
      grad.addColorStop(0, 'rgba(150,255,180,1)')
      grad.addColorStop(1, 'rgba(80,220,130,0)')
      ctx.fillStyle = grad
      ctx.beginPath()
      ctx.arc(x, y, r, 0, Math.PI * 2)
      ctx.fill()
    }
  },
  formation_stone: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 28, [80, 80, 95], 12, ox, oy)
    const rand = rng(s + 9)
    ctx.strokeStyle = 'rgba(180,255,200,0.7)'
    ctx.lineWidth = 1.5
    for (let i = 0; i < 8; i++) {
      const x = rand() * TILE_SIZE
      const y = rand() * TILE_SIZE
      ctx.beginPath()
      ctx.moveTo(x, y)
      ctx.lineTo(x + 8, y + 16)
      ctx.lineTo(x - 4, y + 16)
      ctx.closePath()
      ctx.stroke()
    }
  },
  cobblestone: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 16, [110, 110, 115], 18, ox, oy)
    const rand = rng(s + 1)
    ctx.strokeStyle = 'rgba(70,70,75,0.6)'
    ctx.lineWidth = 1.5
    for (let i = 0; i < 24; i++) {
      const x = rand() * TILE_SIZE
      const y = rand() * TILE_SIZE
      const r = 6 + rand() * 10
      ctx.beginPath()
      ctx.arc(x, y, r, 0, Math.PI * 2)
      ctx.stroke()
    }
  },
  mossy_stone: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 16, [100, 110, 90], 18, ox, oy)
    const rand = rng(s + 2)
    ctx.fillStyle = 'rgba(60,110,55,0.6)'
    for (let i = 0; i < 30; i++) {
      ctx.beginPath()
      ctx.arc(rand() * TILE_SIZE, rand() * TILE_SIZE, 4 + rand() * 6, 0, Math.PI * 2)
      ctx.fill()
    }
  },
  ice: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 24, [180, 220, 240], 10, ox, oy)
    const rand = rng(s + 2)
    ctx.strokeStyle = 'rgba(255,255,255,0.7)'
    ctx.lineWidth = 1
    for (let i = 0; i < 8; i++) {
      ctx.beginPath()
      ctx.moveTo(rand() * TILE_SIZE, rand() * TILE_SIZE)
      ctx.lineTo(rand() * TILE_SIZE, rand() * TILE_SIZE)
      ctx.stroke()
    }
  },
  volcanic_rock: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 22, [55, 45, 45], 18, ox, oy)
    const rand = rng(s + 3)
    ctx.fillStyle = 'rgba(180,60,40,0.6)'
    for (let i = 0; i < 16; i++) {
      ctx.beginPath()
      ctx.arc(rand() * TILE_SIZE, rand() * TILE_SIZE, 3 + rand() * 5, 0, Math.PI * 2)
      ctx.fill()
    }
  },
  ash: (ctx, s, ox, oy) => tileableNoise(ctx, TILE_SIZE, s, 18, [70, 65, 65], 14, ox, oy),
  red_sand: (ctx, s, ox, oy) => tileableNoise(ctx, TILE_SIZE, s, 18, [200, 110, 75], 14, ox, oy),
  snow_cap: (ctx, s, ox, oy) => tileableNoise(ctx, TILE_SIZE, s, 22, [250, 252, 255], 6, ox, oy),
  pine_bark: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 24, [70, 50, 30], 18, ox, oy)
    const rand = rng(s + 1)
    ctx.strokeStyle = 'rgba(40,25,15,0.6)'
    ctx.lineWidth = 1.5
    for (let x = 0; x < TILE_SIZE; x += 4) {
      ctx.beginPath()
      ctx.moveTo(x, 0)
      ctx.lineTo(x + (rand() - 0.5) * 6, TILE_SIZE)
      ctx.stroke()
    }
  },
  jade_ore: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 28, [110, 110, 115], 12, ox, oy)
    const rand = rng(s + 4)
    ctx.fillStyle = 'rgba(120,220,160,0.85)'
    for (let i = 0; i < 12; i++) {
      ctx.beginPath()
      ctx.arc(rand() * TILE_SIZE, rand() * TILE_SIZE, 3 + rand() * 5, 0, Math.PI * 2)
      ctx.fill()
    }
  },
  gold_ore: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 28, [110, 110, 115], 12, ox, oy)
    const rand = rng(s + 5)
    ctx.fillStyle = 'rgba(255,215,80,0.85)'
    for (let i = 0; i < 12; i++) {
      ctx.beginPath()
      ctx.arc(rand() * TILE_SIZE, rand() * TILE_SIZE, 2 + rand() * 4, 0, Math.PI * 2)
      ctx.fill()
    }
  },
  iron_ore: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 28, [110, 110, 115], 12, ox, oy)
    const rand = rng(s + 6)
    ctx.fillStyle = 'rgba(180,140,90,0.85)'
    for (let i = 0; i < 14; i++) {
      ctx.beginPath()
      ctx.arc(rand() * TILE_SIZE, rand() * TILE_SIZE, 2 + rand() * 4, 0, Math.PI * 2)
      ctx.fill()
    }
  },
  crystal_floor: (ctx, s, ox, oy) => {
    tileableNoise(ctx, TILE_SIZE, s, 30, [180, 220, 230], 12, ox, oy)
    ctx.strokeStyle = 'rgba(255,255,255,0.6)'
    ctx.lineWidth = 1.5
    for (let i = 0; i < 6; i++) {
      const cx = 32 + (i % 3) * 96
      const cy = 32 + Math.floor(i / 3) * 96
      ctx.beginPath()
      ctx.moveTo(cx, cy - 16)
      ctx.lineTo(cx + 16, cy)
      ctx.lineTo(cx, cy + 16)
      ctx.lineTo(cx - 16, cy)
      ctx.closePath()
      ctx.stroke()
    }
  },
  bedrock: (ctx, s, ox, oy) => tileableNoise(ctx, TILE_SIZE, s, 16, [50, 50, 55], 18, ox, oy),
}

let _atlas: THREE.Texture | null = null
let _normalAtlas: THREE.Texture | null = null

export function getAlbedoAtlas(): THREE.Texture {
  if (_atlas) return _atlas
  const w = ATLAS_COLS * TILE_SIZE
  const h = ATLAS_ROWS * TILE_SIZE
  const [canvas, ctx] = createCanvas(w)
  // Default fill: stone-grey so any missing tile is grey (not magenta).
  ctx.fillStyle = '#808080'
  ctx.fillRect(0, 0, w, h)
  try {
    TILE_KEYS.forEach((key, i) => {
      const painter = PAINTERS[key] ?? ((c) => tileableNoise(c, TILE_SIZE, i * 31 + 1, 24, [200, 200, 200], 12))
      drawTile(ctx, i, (tc, ox, oy) => painter(tc, i * 31 + 1, ox, oy))
    })
    // Debug: expose the atlas canvas for inspection.
    ;(globalThis as { __ergenAtlasCanvas?: HTMLCanvasElement }).__ergenAtlasCanvas = canvas
    console.log('[ProceduralTextures] atlas built', w, 'x', h, 'tiles:', TILE_KEYS.length)
  } catch (e) {
    console.error('[ProceduralTextures] atlas build FAILED', e)
  }
  const tex = new THREE.CanvasTexture(canvas)
  tex.magFilter = THREE.LinearFilter
  tex.minFilter = THREE.LinearMipmapLinearFilter
  tex.wrapS = THREE.RepeatWrapping
  tex.wrapT = THREE.RepeatWrapping
  tex.generateMipmaps = true
  tex.anisotropy = 8
  // NOTE: Do NOT set colorSpace = SRGB here. The renderer's outputColorSpace
  // handles the final conversion; setting it on the texture causes the
  // MeshStandardMaterial to decode the texture as sRGB→linear, which then
  // gets re-encoded as sRGB on output — but with vertexColors in linear
  // space, the multiplication darkens incorrectly. Leave the texture as
  // linear (no colorSpace set) so vertex colors multiply correctly.
  _atlas = tex
  return tex
}

export function getNormalAtlas(): THREE.Texture {
  if (_normalAtlas) return _normalAtlas
  const w = ATLAS_COLS * TILE_SIZE
  const h = ATLAS_ROWS * TILE_SIZE
  const [canvas, ctx] = createCanvas(w)
  ctx.fillStyle = '#8080ff'
  ctx.fillRect(0, 0, w, h)
  TILE_KEYS.forEach((_key, i) => {
    drawTile(ctx, i, (tc) => {
      const img = tc.createImageData(TILE_SIZE, TILE_SIZE)
      const rand = rng(i * 17 + 3)
      for (let p = 0; p < img.data.length; p += 4) {
        const n = (rand() - 0.5) * 30
        img.data[p] = 128 + n
        img.data[p + 1] = 128 + n
        img.data[p + 2] = 255
        img.data[p + 3] = 255
      }
      tc.putImageData(img, 0, 0)
    })
  })
  const tex = new THREE.CanvasTexture(canvas)
  tex.magFilter = THREE.LinearFilter
  tex.minFilter = THREE.LinearMipmapLinearFilter
  tex.wrapS = THREE.RepeatWrapping
  tex.wrapT = THREE.RepeatWrapping
  tex.generateMipmaps = true
  _normalAtlas = tex
  return tex
}
