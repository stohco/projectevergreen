/**
 * entities/index.ts — entity-system entry point.
 *
 * createEntityManager(scene, graph?) wires up:
 *   • CanonActorMaterializer — spawns Wang Lin, Teng Li, Heng Yue / Luo He
 *     sect disciples at their placed Planet-Suzaku positions.
 *   • Beast spawner — spawns biome-appropriate 灵兽 (Frost Wolf in snow,
 *     Flame Tiger in desert, Jade Serpent in swamp, Thunder Hawk in
 *     mountains).
 *   • Per-frame update for all cultivators + beasts + projectiles + pickups.
 *
 * The manager owns a FlatWorldContextWithEntities (the world-context
 * surface that goals may touch). When the voxel-terrain sub-agent ships,
 * the manager can swap in a terrain-aware context that implements
 * sampleHeight + rayCast against real colliders.
 *
 * Canon fidelity: persistence UUID = canon UUID (so NPCs rematerialize at
 * the same spot on reload). Mod-original: REASONABLE_RECONSTRUCTION, conf 3.
 * NO invented chapter citations.
 */
import * as THREE from 'three'
import {
  type WorldContext, type GoalOwner, FlatWorldContext,
} from '../ai/Goal'
import { EntityCultivator, FlatWorldContextWithEntities } from './EntityCultivator'
import { BeastEntity, type BeastPickup, type BeastEntityOptions } from './BeastEntity'
import { type BeastKind, BEAST_SPECS } from './BeastModel'
import { CanonActorMaterializer } from './CanonActorMaterializer'
import { PLANET_SUZAKU_PLACEMENT, type PlacedLocation } from '../canon/PlanetSuzakuPlacement'
import type { WorldGraph } from '../graph/WorldGraph'

/** A cultivator or beast — anything the manager tracks. */
export type AnyEntity = EntityCultivator | BeastEntity

export interface EntityManager {
  /** Per-frame update — call this from the render loop. */
  update(dt: number): void
  /** Spawn a beast at a specific position. */
  spawnBeast(kind: BeastKind, pos: THREE.Vector3, opts?: Partial<BeastEntityOptions>): BeastEntity
  /** Kill an entity (sets health to 0, plays death animation). */
  killEntity(id: string): void
  /** Get any entity (cultivator or beast) by id. */
  getEntity(id: string): AnyEntity | undefined
  /** Get a cultivator by id (Wang Lin is 'N01'). */
  getCultivator(id: string): EntityCultivator | undefined
  /** Get a beast by id. */
  getBeast(id: string): BeastEntity | undefined
  /** All live entities. */
  getAllEntities(): AnyEntity[]
  /** All canon cultivators (Wang Lin, Teng Li, sect disciples). */
  getCanonCultivators(): EntityCultivator[]
  /** The world context (for goals / VFX to query). */
  getContext(): WorldContext
  /** Cleanup — removes all entities + pickups from the scene. */
  dispose(): void
}

/**
 * WorldContext implementation with entity registry + projectile management.
 * Subclass of FlatWorldContext so it works without terrain.
 */
class EntityManagerContext extends FlatWorldContextWithEntities {
  // Inherits registerEntity / unregisterEntity / updateProjectiles / entitiesNear.
}

/** Biome → beast-kind mapping. */
const BIOME_BEAST: Partial<Record<PlacedLocation['biome'], BeastKind>> = {
  snow: 'frost_wolf',
  desert: 'flame_tiger',
  swamp: 'jade_serpent',
  mountains: 'thunder_hawk',
  forest: 'jade_serpent',
  volcanic: 'flame_tiger',
}

/**
 * createEntityManager — wires the canon-actor materializer + beast spawner
 * and returns the public manager API.
 */
export function createEntityManager(scene: THREE.Scene, _graph?: WorldGraph): EntityManager {
  const ctx = new EntityManagerContext(scene)
  const materializer = new CanonActorMaterializer(scene)
  const beasts = new Map<string, BeastEntity>()
  const pickups: BeastPickup[] = []
  const deathTimers = new Map<string, number>()  // entity id → seconds until removal
  let beastCounter = 0

  // ── Spawn canon NPCs ──────────────────────────────────────────────
  materializer.materializeAll(ctx)

  // ── Spawn biome-appropriate beasts ────────────────────────────────
  // For each placed location whose biome maps to a beast kind, spawn a few
  // beasts at offset positions (away from the sect center).
  const beastsToSpawn: Array<{ kind: BeastKind; pos: THREE.Vector3; home: THREE.Vector3 }> = []
  for (const loc of PLANET_SUZAKU_PLACEMENT) {
    const kind = BIOME_BEAST[loc.biome]
    if (!kind) continue
    // Spawn 3 beasts per biome location, in a ring around the location.
    const count = 3
    for (let i = 0; i < count; i++) {
      const angle = (i / count) * Math.PI * 2 + Math.random() * 0.5
      const r = loc.radius + 20 + Math.random() * 30
      const px = loc.position[0] + Math.cos(angle) * r
      const pz = loc.position[1] + Math.sin(angle) * r
      beastsToSpawn.push({
        kind,
        pos: new THREE.Vector3(px, 0, pz),
        home: new THREE.Vector3(px, 0, pz),
      })
    }
  }
  // Also spawn a couple of Jade Serpents near the Cliff of the Heaven-Defying
  // Bead (mountains) for player-encounter flavor.
  beastsToSpawn.push({
    kind: 'jade_serpent',
    pos: new THREE.Vector3(-110, 0, -130),
    home: new THREE.Vector3(-110, 0, -130),
  })
  beastsToSpawn.push({
    kind: 'frost_wolf',
    pos: new THREE.Vector3(40, 0, -60),
    home: new THREE.Vector3(40, 0, -60),
  })
  for (const b of beastsToSpawn) {
    spawnBeastInternal(b.kind, b.pos, { homePosition: b.home })
  }

  // ── Public API ────────────────────────────────────────────────────
  function spawnBeastInternal(
    kind: BeastKind,
    pos: THREE.Vector3,
    opts?: Partial<BeastEntityOptions>,
  ): BeastEntity {
    beastCounter += 1
    const id = `beast:${kind}:${beastCounter}`
    const beast = new BeastEntity({
      id,
      kind,
      position: pos.clone(),
      homePosition: (opts?.homePosition ?? pos).clone(),
      aggression: opts?.aggression ?? 50,
      maxHealth: opts?.maxHealth,
      damage: opts?.damage,
    })
    beast.materialize(scene)
    ctx.registerEntity(beast)
    beasts.set(id, beast)
    return beast
  }

  function removeEntity(id: string): void {
    const c = materializer.get(id)
    if (c) {
      c.dematerialize(scene)
      ctx.unregisterEntity(id)
      return
    }
    const b = beasts.get(id)
    if (b) {
      // Spawn death pickup.
      const pickup = b.spawnDeathPickup()
      pickups.push(pickup)
      scene.add(pickup.mesh)
      b.dematerialize(scene)
      ctx.unregisterEntity(id)
      beasts.delete(id)
    }
  }

  function update(dt: number): void {
    // Update all canon cultivators.
    for (const c of materializer.cultivators.values()) {
      c.update(dt, ctx)
      // Track death timers.
      if (!c.alive) {
        const t = (deathTimers.get(c.id) ?? 5.0) - dt
        deathTimers.set(c.id, t)
        if (t <= 0) {
          removeEntity(c.id)
          deathTimers.delete(c.id)
        }
      }
    }
    // Update all beasts.
    for (const b of beasts.values()) {
      b.update(dt, ctx)
      if (!b.alive) {
        const t = (deathTimers.get(b.id) ?? 5.0) - dt
        deathTimers.set(b.id, t)
        if (t <= 0) {
          removeEntity(b.id)
          deathTimers.delete(b.id)
        }
      }
    }
    // Update projectiles (CRON-138 port).
    ctx.updateProjectiles(dt)
    // Update pickups (rotate, bob, TTL).
    for (let i = pickups.length - 1; i >= 0; i--) {
      const p = pickups[i]
      p.ttl -= dt
      if (p.ttl <= 0) {
        scene.remove(p.mesh)
        pickups.splice(i, 1)
        continue
      }
      p.mesh.rotation.y += p.spin * dt
      p.mesh.rotation.x += p.spin * 0.5 * dt
      p.mesh.position.y = p.position.y + Math.sin(performance.now() * 0.002 + i) * 0.15
      // Fade out in the last 5 seconds.
      if (p.ttl < 5) {
        p.mesh.traverse((o) => {
          const m = o as THREE.Mesh
          if (m.material) {
            const mat = m.material as THREE.MeshStandardMaterial
            mat.transparent = true
            mat.opacity = Math.max(0, p.ttl / 5)
          }
        })
      }
    }
  }

  function killEntity(id: string): void {
    const c = materializer.get(id)
    if (c) { c.takeDamage(c.health); return }
    const b = beasts.get(id)
    if (b) { b.takeDamage(b.health); return }
  }

  function getEntity(id: string): AnyEntity | undefined {
    return materializer.get(id) ?? beasts.get(id)
  }

  function getCultivator(id: string): EntityCultivator | undefined {
    return materializer.get(id)
  }

  function getBeast(id: string): BeastEntity | undefined {
    return beasts.get(id)
  }

  function getAllEntities(): AnyEntity[] {
    return [
      ...Array.from(materializer.cultivators.values()),
      ...Array.from(beasts.values()),
    ]
  }

  function getCanonCultivators(): EntityCultivator[] {
    return Array.from(materializer.cultivators.values())
  }

  function dispose(): void {
    materializer.dematerializeAll()
    for (const b of beasts.values()) b.dematerialize(scene)
    beasts.clear()
    for (const p of pickups) scene.remove(p.mesh)
    pickups.length = 0
    deathTimers.clear()
  }

  return {
    update,
    spawnBeast: spawnBeastInternal,
    killEntity,
    getEntity,
    getCultivator,
    getBeast,
    getAllEntities,
    getCanonCultivators,
    getContext: () => ctx,
    dispose,
  }
}

/** Re-exports for the WorldCanvas to consume. */
export { EntityCultivator } from './EntityCultivator'
export { BeastEntity } from './BeastEntity'
export { CanonActorMaterializer, CANON_ACTOR_SPECS } from './CanonActorMaterializer'
export { createCultivatorModel } from './CultivatorModel'
export type { CultivatorModelHandle as CultivatorModel, RealmKey } from './CultivatorModel'
export { BeastModel, createBeastModel, BEAST_SPECS } from './BeastModel'
export type { BeastKind } from './BeastModel'
export { CultivatorFlightGoal } from '../ai/CultivatorFlightGoal'
export { CultivatorSwordQiGoal } from '../ai/CultivatorSwordQiGoal'
export { CultivatorWanderGoal } from '../ai/CultivatorWanderGoal'
export { FlightNavigator } from '../ai/FlightNavigator'
export { CULTIVATOR_CONSTANTS } from '../ai/Goal'
export type { CultivatorRealm, WorldContext, GoalOwner, Goal } from '../ai/Goal'
export type { RealmKey as CultivatorGender } from './CultivatorModel'
