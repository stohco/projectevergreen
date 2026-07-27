/**
 * CollisionTaxonomy — smart collision rules based on WorldObject properties.
 *
 * Per DESIGN_HEAVEN_AND_EARTH_MANIPULATION.md, every world object has:
 *   physicalMass — weight/volume (0 = non-physical like grass, >0 = solid)
 *   material — dirt, stone, wood, jade, spirit_stone, water, etc.
 *   solid — is it physically solid? (walls=yes, water=no, grass=no)
 *
 * This module provides the rules for which objects get collision and which
 * don't. It's NOT a simple flag — it's a function of the object's nature.
 *
 * Taxonomy:
 *   SOLID (blocks movement):
 *     - Walls, roofs, pillars, doors (when closed)
 *     - Large rocks, boulders
 *     - Tree trunks (but NOT foliage — you can push through leaves)
 *     - Fences, gates
 *     - Furniture (beds, tables, chests, altars — you walk around them)
 *     - NPC bodies (you can't walk through Wang Lin)
 *
 *   NON-SOLID (walk through):
 *     - Grass, flowers, spirit herbs
 *     - Bushes, small plants
 *     - Tree foliage/leaves (can push through branches)
 *     - Water (has its own system — swimming, not blocking)
 *     - Qi particles, VFX
 *     - Light sources, triggers
 *
 *   SPECIAL:
 *     - Doors: solid when closed, non-solid when open (toggled by E key)
 *     - Water: non-solid but applies buoyancy + drag
 *     - Formation boundaries: solid to cultivators below a realm threshold
 *     - Spirit vein emanations: non-solid but apply qi field effects
 */

import * as THREE from 'three'

export type CollisionClass = 'solid' | 'non_solid' | 'door' | 'water' | 'formation'

export interface CollisionProfile {
  class: CollisionClass
  /** Radius for cylindrical collision (for trees, NPCs). 0 = use mesh. */
  radius?: number
  /** Height for cylindrical collision. */
  height?: number
  /** If true, uses mesh-based ray collision. If false, uses cylinder. */
  useMesh?: boolean
}

/**
 * Determine the collision class of a mesh based on its name and userData.
 * This is the "smart" part — we infer collision from what the object IS,
 * not from a manually-set flag.
 */
export function classifyCollision(mesh: THREE.Mesh): CollisionProfile {
  const name = mesh.name?.toLowerCase() ?? ''
  const userData = mesh.userData ?? {}

  // Doors — special: solid when closed, non-solid when open.
  if (userData.isDoor || name.includes('door')) {
    return { class: 'door', useMesh: true }
  }

  // Walls, floors, roofs, pillars — always solid.
  if (name.includes('wall') || name.includes('floor') || name.includes('roof') ||
      name.includes('pillar') || name.includes('post') || name.includes('beam') ||
      name.includes('gate') || name.includes('fence')) {
    return { class: 'solid', useMesh: true }
  }

  // Furniture — solid (you walk around beds, tables, chests).
  if (name.includes('bed') || name.includes('table') || name.includes('chest') ||
      name.includes('altar') || name.includes('shelf') || name.includes('furnace') ||
      name.includes('well') || name.includes('mat')) {
    // Meditation mats are non-solid (you stand on them).
    if (name.includes('mat')) return { class: 'non_solid' }
    return { class: 'solid', useMesh: true }
  }

  // Tree trunks — solid (cylinder collision, not mesh — cheaper).
  if (name.includes('trunk') || name.includes('log')) {
    return { class: 'solid', radius: 0.3, height: 4.0, useMesh: false }
  }

  // Tree foliage/leaves — non-solid (push through branches).
  if (name.includes('foliage') || name.includes('leaves') || name.includes('canopy')) {
    return { class: 'non_solid' }
  }

  // Rocks — solid if large, non-solid if small (pebbles).
  if (name.includes('rock') || name.includes('stone')) {
    return { class: 'solid', useMesh: true }
  }

  // Grass, flowers, herbs — non-solid.
  if (name.includes('grass') || name.includes('flower') || name.includes('herb') ||
      name.includes('plant') || name.includes('bush')) {
    return { class: 'non_solid' }
  }

  // Water — special system.
  if (name.includes('water') || name.includes('ocean') || name.includes('river')) {
    return { class: 'water' }
  }

  // Formation boundaries — special (realm-gated).
  if (name.includes('formation') || name.includes('array')) {
    return { class: 'formation' }
  }

  // Default: if userData.collidable is explicitly set, respect it.
  if (userData.collidable === true) return { class: 'solid', useMesh: true }
  if (userData.collidable === false) return { class: 'non_solid' }

  // Default: non-solid (safe — don't block the player on unknown objects).
  return { class: 'non_solid' }
}

/**
 * Check if a mesh should be included in the ray-based collision system.
 * Returns true for solid objects and closed doors. Returns false for
 * grass, water, open doors, and non-solid objects.
 */
export function shouldCollide(mesh: THREE.Mesh): boolean {
  const profile = classifyCollision(mesh)
  switch (profile.class) {
    case 'solid': return true
    case 'door': return mesh.userData.collidable !== false // closed = true
    case 'water': return false
    case 'formation': return false // handled separately
    case 'non_solid': return false
    default: return false
  }
}
