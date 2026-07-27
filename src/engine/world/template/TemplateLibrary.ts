/**
 * Template library — resolves semantic objects to geometry.
 *
 * The bridge between "what exists" (semantic) and "how it looks" (render).
 * Each FurnitureKind maps to a template (a set of mesh placements). Each
 * BuildingTheme maps material slots to actual Three.js material properties.
 *
 * Templates are swappable without touching canon. If we want to change the
 * village from "poor wood" to "ruined stone", we swap the theme — the canon
 * data (settlement/building/room structure) stays identical.
 */

import * as THREE from 'three'
import type {
  MaterialSlot,
  FurnitureKind,
  BuildingPurpose,
  MeshInstruction,
} from '../semantic/CanonTypes'

// ---- Theme definitions ---------------------------------------------------

export interface BuildingTheme {
  id: string
  wall: MaterialColor
  floor: MaterialColor
  roof: MaterialColor
  pillar: MaterialColor
  door: MaterialColor
}

export interface MaterialColor {
  color: number
  roughness: number
  metalness: number
  emissive?: number
  emissiveIntensity?: number
}

export const THEMES: Record<string, BuildingTheme> = {
  poor_village_wood: {
    id: 'poor_village_wood',
    wall: { color: 0x8a7050, roughness: 0.9, metalness: 0.0 }, // weathered wood
    floor: { color: 0x6a5040, roughness: 0.95, metalness: 0.0 }, // packed earth
    roof: { color: 0x4a3a2a, roughness: 0.9, metalness: 0.0 }, // thatch dark
    pillar: { color: 0x6a5040, roughness: 0.9, metalness: 0.0 },
    door: { color: 0x5a3a20, roughness: 0.85, metalness: 0.0 },
  },
  stone_well: {
    id: 'stone_well',
    wall: { color: 0x707074, roughness: 0.8, metalness: 0.0 }, // grey stone
    floor: { color: 0x505054, roughness: 0.9, metalness: 0.0 },
    roof: { color: 0x4a3a2a, roughness: 0.9, metalness: 0.0 }, // wooden frame
    pillar: { color: 0x6a5040, roughness: 0.9, metalness: 0.0 },
    door: { color: 0x5a3a20, roughness: 0.85, metalness: 0.0 },
  },
  wooden_gate: {
    id: 'wooden_gate',
    wall: { color: 0x6a5040, roughness: 0.9, metalness: 0.0 },
    floor: { color: 0x5a4030, roughness: 0.95, metalness: 0.0 },
    roof: { color: 0x4a3a2a, roughness: 0.9, metalness: 0.0 },
    pillar: { color: 0x5a4030, roughness: 0.9, metalness: 0.0 },
    door: { color: 0x4a3018, roughness: 0.85, metalness: 0.0 },
  },
}

// ---- Material cache ------------------------------------------------------

const materialCache = new Map<string, THREE.MeshStandardMaterial>()

export function getMaterial(slot: MaterialSlot, themeId: string): THREE.MeshStandardMaterial {
  const theme = THEMES[themeId] ?? THEMES.poor_village_wood
  const key = `${slot}:${themeId}`
  const cached = materialCache.get(key)
  if (cached) return cached

  let mc: MaterialColor
  switch (slot) {
    case 'WALL': mc = theme.wall; break
    case 'FLOOR': mc = theme.floor; break
    case 'ROOF': mc = theme.roof; break
    case 'PILLAR': mc = theme.pillar; break
    case 'DOOR': mc = theme.door; break
    case 'BED': mc = { color: 0x8a7050, roughness: 0.9, metalness: 0.0 }; break
    case 'MAT': mc = { color: 0xc4a060, roughness: 0.95, metalness: 0.0 }; break // woven straw
    case 'LAMP': mc = { color: 0xffd060, roughness: 0.3, metalness: 0.0, emissive: 0xffaa30, emissiveIntensity: 1.5 }; break
    case 'SPIRIT_STONE': mc = { color: 0x9be15d, roughness: 0.2, metalness: 0.3, emissive: 0x9be15d, emissiveIntensity: 0.8 }; break
    case 'WOOD': mc = { color: 0x8a7050, roughness: 0.9, metalness: 0.0 }; break
    case 'STONE': mc = { color: 0x707074, roughness: 0.8, metalness: 0.0 }; break
    case 'JADE': mc = { color: 0x4e8a72, roughness: 0.3, metalness: 0.2, emissive: 0x2a5a3a, emissiveIntensity: 0.3 }; break
    case 'PAPER': mc = { color: 0xf0e6c8, roughness: 0.6, metalness: 0.0 }; break
    case 'THATCH': mc = { color: 0x8a7040, roughness: 0.95, metalness: 0.0 }; break
    case 'COBBLE': mc = { color: 0x606064, roughness: 0.85, metalness: 0.0 }; break
    case 'GRASS': mc = { color: 0x5a8a3c, roughness: 0.9, metalness: 0.0 }; break
    case 'DIRT': mc = { color: 0x6a4a2b, roughness: 0.95, metalness: 0.0 }; break
    case 'WATER': mc = { color: 0x2a6a9a, roughness: 0.1, metalness: 0.0 }; break
    default: mc = theme.wall; break
  }

  const mat = new THREE.MeshStandardMaterial({
    color: mc.color,
    roughness: mc.roughness,
    metalness: mc.metalness,
    emissive: mc.emissive ?? 0x000000,
    emissiveIntensity: mc.emissiveIntensity ?? 0,
  })
  materialCache.set(key, mat)
  return mat
}

// ---- Furniture templates -------------------------------------------------

export interface FurnitureTemplate {
  kind: FurnitureKind
  placements: MeshPlacement[]
}

export interface MeshPlacement {
  kind: 'box' | 'cylinder' | 'plane' | 'sphere'
  position: [number, number, number]
  size: [number, number, number]
  rotation?: number
  slot: MaterialSlot
}

export const FURNITURE_TEMPLATES: Record<FurnitureKind, FurnitureTemplate> = {
  BED: {
    kind: 'BED',
    placements: [
      // Frame
      { kind: 'box', position: [0, 0.25, 0], size: [1.2, 0.5, 2.0], slot: 'WOOD' },
      // Mattress (straw)
      { kind: 'box', position: [0, 0.55, 0], size: [1.1, 0.15, 1.9], slot: 'THATCH' },
      // Pillow
      { kind: 'box', position: [0, 0.68, -0.8], size: [0.5, 0.1, 0.3], slot: 'PAPER' },
    ],
  },
  MEDITATION_MAT: {
    kind: 'MEDITATION_MAT',
    placements: [
      // Flat mat
      { kind: 'box', position: [0, 0.02, 0], size: [1.0, 0.05, 1.0], slot: 'MAT' },
      // Small cushion
      { kind: 'box', position: [0, 0.08, 0], size: [0.4, 0.1, 0.4], slot: 'MAT' },
    ],
  },
  BOOKSHELF: {
    kind: 'BOOKSHELF',
    placements: [
      { kind: 'box', position: [0, 1.0, 0], size: [1.5, 2.0, 0.4], slot: 'WOOD' },
      // Shelves
      { kind: 'box', position: [0, 0.5, 0.1], size: [1.3, 0.05, 0.2], slot: 'WOOD' },
      { kind: 'box', position: [0, 1.0, 0.1], size: [1.3, 0.05, 0.2], slot: 'WOOD' },
      { kind: 'box', position: [0, 1.5, 0.1], size: [1.3, 0.05, 0.2], slot: 'WOOD' },
    ],
  },
  HIDDEN_STORAGE: {
    kind: 'HIDDEN_STORAGE',
    placements: [
      { kind: 'box', position: [0, 0.3, 0], size: [0.8, 0.6, 0.8], slot: 'WOOD' },
    ],
  },
  ALCHEMY_FURNACE: {
    kind: 'ALCHEMY_FURNACE',
    placements: [
      // Furnace body
      { kind: 'cylinder', position: [0, 0.6, 0], size: [0.5, 1.2, 0.5], slot: 'STONE' },
      // Top opening
      { kind: 'cylinder', position: [0, 1.25, 0], size: [0.3, 0.1, 0.3], slot: 'STONE' },
    ],
  },
  SPIRIT_WELL: {
    kind: 'SPIRIT_WELL',
    placements: [
      // Well wall
      { kind: 'cylinder', position: [0, 0.5, 0], size: [1.0, 1.0, 1.0], slot: 'COBBLE' },
      // Water surface
      { kind: 'cylinder', position: [0, 0.3, 0], size: [0.8, 0.05, 0.8], slot: 'WATER' },
    ],
  },
  TABLE: {
    kind: 'TABLE',
    placements: [
      // Top
      { kind: 'box', position: [0, 0.75, 0], size: [1.5, 0.1, 0.8], slot: 'WOOD' },
      // Legs
      { kind: 'box', position: [-0.65, 0.375, -0.3], size: [0.1, 0.75, 0.1], slot: 'WOOD' },
      { kind: 'box', position: [0.65, 0.375, -0.3], size: [0.1, 0.75, 0.1], slot: 'WOOD' },
      { kind: 'box', position: [-0.65, 0.375, 0.3], size: [0.1, 0.75, 0.1], slot: 'WOOD' },
      { kind: 'box', position: [0.65, 0.375, 0.3], size: [0.1, 0.75, 0.1], slot: 'WOOD' },
    ],
  },
  LAMP: {
    kind: 'LAMP',
    placements: [
      // Post
      { kind: 'cylinder', position: [0, 0.75, 0], size: [0.06, 1.5, 0.06], slot: 'WOOD' },
      // Lamp body
      { kind: 'box', position: [0, 1.6, 0], size: [0.3, 0.3, 0.3], slot: 'LAMP' },
      // Top
      { kind: 'box', position: [0, 1.8, 0], size: [0.35, 0.05, 0.35], slot: 'WOOD' },
    ],
  },
  CHEST: {
    kind: 'CHEST',
    placements: [
      // Body
      { kind: 'box', position: [0, 0.4, 0], size: [0.9, 0.8, 0.6], slot: 'WOOD' },
      // Lid
      { kind: 'box', position: [0, 0.85, 0], size: [0.9, 0.15, 0.6], slot: 'WOOD' },
    ],
  },
  ALTAR: {
    kind: 'ALTAR',
    placements: [
      // Table
      { kind: 'box', position: [0, 0.6, 0], size: [1.2, 0.1, 0.5], slot: 'WOOD' },
      // Legs
      { kind: 'box', position: [-0.5, 0.3, -0.2], size: [0.08, 0.6, 0.08], slot: 'WOOD' },
      { kind: 'box', position: [0.5, 0.3, -0.2], size: [0.08, 0.6, 0.08], slot: 'WOOD' },
      { kind: 'box', position: [-0.5, 0.3, 0.2], size: [0.08, 0.6, 0.08], slot: 'WOOD' },
      { kind: 'box', position: [0.5, 0.3, 0.2], size: [0.08, 0.6, 0.08], slot: 'WOOD' },
    ],
  },
  INCENSE_BURNER: {
    kind: 'INCENSE_BURNER',
    placements: [
      // Bowl
      { kind: 'cylinder', position: [0, 0.15, 0], size: [0.15, 0.3, 0.15], slot: 'STONE' },
      // Incense stick
      { kind: 'cylinder', position: [0, 0.5, 0], size: [0.01, 0.7, 0.01], slot: 'WOOD' },
    ],
  },
  SWORD_RACK: {
    kind: 'SWORD_RACK',
    placements: [
      // Stand
      { kind: 'box', position: [0, 0.5, 0], size: [0.8, 1.0, 0.2], slot: 'WOOD' },
      // Arms
      { kind: 'box', position: [-0.3, 1.0, 0], size: [0.1, 0.2, 0.15], slot: 'WOOD' },
      { kind: 'box', position: [0.3, 1.0, 0], size: [0.1, 0.2, 0.15], slot: 'WOOD' },
    ],
  },
}
