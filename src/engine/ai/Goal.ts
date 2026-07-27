/**
 * Goal.ts — shared AI contracts for the Er Gen Verse entity system.
 *
 * A Goal is a short-lived behaviour an entity pursues (wander, fly, fire
 * sword-qi, meditate). The entity owns a single active goal at a time; the
 * AI layer (CanonActorMaterializer + beast spawner) chooses goals based on
 * world state.
 *
 * Faithful to the 仙逆 source: cultivators meditate to regen qi, fly on
 * swords at Foundation realm and above, fire sword-qi (剑气) as their
 * primary ranged attack. Beasts wander, aggro, attack, and flee.
 *
 * Mod-original: REASONABLE_RECONSTRUCTION, conf 3 (no novel chapter cites).
 */
import type * as THREE from 'three'

export type Vec3 = [number, number, number]

/** Goal kind discriminator. */
export type GoalKind =
  | 'idle'
  | 'wander'
  | 'flight'
  | 'sword_qi'
  | 'meditate'
  | 'dead'
  | 'beast_wander'
  | 'beast_aggro'
  | 'beast_attack'
  | 'beast_flee'

/** Cultivation realms attested in 仙逆. */
export type CultivatorRealm =
  | 'qi_condensation'
  | 'foundation'
  | 'core_formation'
  | 'nascent_soul'
  | 'soul_transformation'
  | 'ascendant'

/** Qi cost / regen constants — mod-original (REASONABLE_RECONSTRUCTION, conf 3)
 *  ported from the Forge CRON-130..139 subsystems. */
export const CULTIVATOR_CONSTANTS = {
  // Qi regen (qi per second) by realm. Mod-original tuning.
  QI_REGEN: {
    qi_condensation: 1.0,
    foundation: 1.8,
    core_formation: 3.0,
    nascent_soul: 5.0,
    soul_transformation: 8.0,
    ascendant: 14.0,
  } as Record<CultivatorRealm, number>,
  // Sword-flight speed (blocks/sec) by realm.
  FLIGHT_SPEED: {
    qi_condensation: 0, // cannot fly
    foundation: 40,
    core_formation: 60,
    nascent_soul: 90,
    soul_transformation: 140,
    ascendant: 220,
  } as Record<CultivatorRealm, number>,
  FLIGHT_QI_PER_SEC: 2.0, // CRON-134
  FLIGHT_QI_ABORT_RATIO: 0.05, // abort if qi < 5% maxQi
  SWORD_QI_COST: 5.0, // CRON-138
  SWORD_QI_GATE_RATIO: 0.05, // 5% maxQi gate
  SWORD_QI_COOLDOWN: 1.5, // seconds
  SWORD_QI_PROJECTILE_SPEED: 60, // blocks/sec
  SWORD_QI_PROJECTILE_LIFETIME: 3.0, // seconds
  SWORD_QI_DAMAGE: 25,
  MEDITATE_REGEN_MULTIPLIER: 3.0,
  TALL_VAULT: 3.0, // CRON-136 tall-obstacle vault distance
} as const

/**
 * GoalOwner — the surface area a Goal may touch on its owning entity.
 * Both EntityCultivator and BeastEntity implement this interface, which
 * keeps the AI layer decoupled from concrete entity classes (no runtime
 * import cycle).
 */
export interface GoalOwner {
  id: string
  name: string
  realm: string
  position: THREE.Vector3
  velocity: THREE.Vector3
  target: THREE.Vector3 | null
  hostility: number // 0..100
  health: number
  maxHealth: number
  qi: number
  maxQi: number
  faction: string
  alive: boolean

  // Visual / animation hooks
  setAnimation(name: string, fade?: number): void
  setSwordDrawn(drawn: boolean): void
  setRidingSword(riding: boolean): void
  /** Returns the current animation name (e.g. 'walk', 'idle', 'meditate'). */
  getAnimationName(): string

  // Combat / resource
  takeDamage(amount: number, sourceId?: string): void
  consumeQi(amount: number): boolean
  faceDirection(dir: THREE.Vector3, dt: number, turnRate?: number): void
  /** Teleport the entity (used by spawn / dematerialize). */
  teleportTo(pos: THREE.Vector3): void

  // Goal lifecycle
  /** Request a new goal; the swap happens at the end of the current tick. */
  requestGoal(goal: Goal, ctx: WorldContext): void
}

/**
 * WorldContext — surface area goals may touch the world. Provides terrain
 * height queries, ray-cast for obstacle avoidance, projectile spawning, and
 * nearby-entity queries. The default implementation (FlatWorldContext) is
 * used until the voxel-terrain sub-agent wires in a real one.
 */
export interface WorldContext {
  scene: THREE.Scene
  /** Ground height y at world (x, z). Fallback: 0. */
  sampleHeight(x: number, z: number): number
  /** Ray-cast against world colliders; null if no hit. */
  rayCast(
    origin: THREE.Vector3,
    dir: THREE.Vector3,
    maxDist: number,
  ): { distance: number; point: THREE.Vector3; normal: THREE.Vector3 } | null
  /** Spawn a transient projectile (sword-qi crescent, etc.). */
  spawnProjectile(p: Projectile): void
  /** Query live entities within `radius` of `pos`. */
  entitiesNear(pos: THREE.Vector3, radius: number): EntityHandle[]
  /** Wall-clock seconds since boot (for VFX phase). */
  now(): number
  /** Register an entity handle so goals can find it. */
  registerEntity(e: EntityHandle): void
  /** Unregister an entity (e.g. after death / dematerialize). */
  unregisterEntity(id: string): void
  /** Update all spawned projectiles; despawn expired ones. */
  updateProjectiles(dt: number): void
}

/** Lightweight read handle on a nearby entity (for aggro / targeting). */
export interface EntityHandle {
  id: string
  name: string
  position: THREE.Vector3
  hostility: number
  faction: string
  alive: boolean
  /** Apply damage; sourceId is the attacker's entity id (for karma). */
  takeDamage(amount: number, sourceId?: string): void
}

/** Mod-original projectile (sword-qi crescent). REASONABLE_RECONSTRUCTION. */
export interface Projectile {
  id: string
  sourceId: string
  origin: THREE.Vector3
  position: THREE.Vector3
  direction: THREE.Vector3 // normalized
  speed: number
  damage: number
  lifetime: number // seconds remaining
  radius: number // hit radius
  mesh: THREE.Object3D
  /** Optional per-frame hook (e.g. trail update). */
  update?(dt: number, ctx: WorldContext): void
}

/**
 * Goal — a single behaviour. Returns true from update() when complete.
 */
export interface Goal {
  kind: GoalKind
  onEnter?(owner: GoalOwner, ctx: WorldContext): void
  onExit?(owner: GoalOwner, ctx: WorldContext): void
  update(dt: number, owner: GoalOwner, ctx: WorldContext): boolean
}

/** A flat-ground fallback world context — used until terrain ships. */
export class FlatWorldContext implements WorldContext {
  scene: THREE.Scene
  private projectiles: Projectile[] = []
  private entities: EntityHandle[] = []
  private startTime = performance.now() / 1000

  constructor(scene: THREE.Scene) {
    this.scene = scene
  }

  sampleHeight(_x: number, _z: number): number {
    return 0
  }

  rayCast(
    _origin: THREE.Vector3,
    _dir: THREE.Vector3,
    _maxDist: number,
  ): { distance: number; point: THREE.Vector3; normal: THREE.Vector3 } | null {
    return null
  }

  spawnProjectile(p: Projectile): void {
    this.projectiles.push(p)
    this.scene.add(p.mesh)
  }

  entitiesNear(pos: THREE.Vector3, radius: number): EntityHandle[] {
    const r2 = radius * radius
    return this.entities.filter((e) => {
      if (!e.alive) return false
      const dx = e.position.x - pos.x
      const dy = e.position.y - pos.y
      const dz = e.position.z - pos.z
      return dx * dx + dy * dy + dz * dz <= r2
    })
  }

  now(): number {
    return performance.now() / 1000 - this.startTime
  }

  /** Register an entity handle so goals can find it. */
  registerEntity(e: EntityHandle): void {
    this.entities.push(e)
  }

  unregisterEntity(id: string): void {
    this.entities = this.entities.filter((e) => e.id !== id)
  }

  /** Update all projectiles; despawn expired ones. */
  updateProjectiles(dt: number): void {
    const survivors: Projectile[] = []
    for (const p of this.projectiles) {
      p.lifetime -= dt
      if (p.lifetime <= 0) {
        this.scene.remove(p.mesh)
        disposeObject3D(p.mesh)
        continue
      }
      const step = p.direction.clone().multiplyScalar(p.speed * dt)
      p.position.add(step)
      p.mesh.position.copy(p.position)
      // Hit check against entities (skip source).
      const r2 = p.radius * p.radius
      let hit = false
      for (const e of this.entities) {
        if (!e.alive || e.id === p.sourceId) continue
        const dx = e.position.x - p.position.x
        const dy = e.position.y - p.position.y
        const dz = e.position.z - p.position.z
        if (dx * dx + dy * dy + dz * dz <= r2) {
          e.takeDamage(p.damage, p.sourceId)
          hit = true
          break
        }
      }
      if (hit) {
        this.scene.remove(p.mesh)
        disposeObject3D(p.mesh)
        continue
      }
      if (p.update) p.update(dt, this)
      survivors.push(p)
    }
    this.projectiles = survivors
  }
}

/** Recursively dispose geometries / materials in an Object3D subtree. */
export function disposeObject3D(obj: THREE.Object3D): void {
  obj.traverse((child) => {
    const mesh = child as THREE.Mesh
    if (mesh.geometry) mesh.geometry.dispose()
    const mat = (mesh as THREE.Mesh).material
    if (mat) {
      if (Array.isArray(mat)) mat.forEach((m) => m.dispose())
      else mat.dispose()
    }
  })
}

/** Deterministic id generator for projectiles (no uuid dep needed). */
let _projCounter = 0
export function nextProjectileId(): string {
  _projCounter += 1
  return `proj-${_projCounter}`
}
