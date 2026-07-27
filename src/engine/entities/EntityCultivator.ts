/**
 * EntityCultivator.ts — runtime entity for a xianxia cultivator.
 *
 * Holds the persistent simulation state (id, realm, qi, position, velocity,
 * target, goal) and the visual CultivatorModel. Implements GoalOwner so any
 * AI goal (wander, flight, sword-qi, meditate) can drive it.
 *
 * Faithful to the 仙逆 simulation contract:
 *   - Qi regen rate scales with realm (1.0 qi/s at Qi Condensation, up to
 *     14 qi/s at Ascendant). Mod-original tuning (REASONABLE_RECONSTRUCTION).
 *   - Meditation triples qi regen.
 *   - Foundation-realm cultivators and above can sword-fly (御剑飞行); below
 *     that, flight goals abort.
 *   - When a cultivator dies, the model plays the 'dead' pose and the entity
 *     is removed by the manager.
 */
import * as THREE from 'three'
import {
  type Goal, type GoalOwner, type WorldContext, type EntityHandle,
  type Projectile, type CultivatorRealm,
  CULTIVATOR_CONSTANTS, FlatWorldContext, nextProjectileId, disposeObject3D,
} from '../ai/Goal'
import {
  CultivatorModel, createCultivatorModel, type CultivatorAnimName,
} from './CultivatorModel'
import type { CanonActor } from '../canon/types'

const REALM_MAX_QI: Record<CultivatorRealm, number> = {
  qi_condensation: 100,
  foundation: 500,
  core_formation: 2000,
  nascent_soul: 8000,
  soul_transformation: 30000,
  ascendant: 100000,
}

const REALM_MAX_HEALTH: Record<CultivatorRealm, number> = {
  qi_condensation: 100,
  foundation: 200,
  core_formation: 400,
  nascent_soul: 800,
  soul_transformation: 1500,
  ascendant: 3000,
}

export interface EntityCultivatorOptions {
  id: string
  name: string
  nameCn?: string
  realm: CultivatorRealm
  position: THREE.Vector3
  faction: string
  hostility: number
  canonId?: string
  canonActor?: CanonActor
  gender?: 'male' | 'female'
  /** Optional home position for wander goal. Defaults to spawn position. */
  homePosition?: THREE.Vector3
  /** Robe color override (realm palette is the default). */
  robeColorOverride?: number
  /** Trim (gold band) color override. */
  trimColorOverride?: number
  /** Sword blade color override. */
  bladeColorOverride?: number
  /** Hair color override. */
  hairColor?: number
}

/**
 * EntityCultivator — a single cultivator in the world.
 */
export class EntityCultivator implements GoalOwner, EntityHandle {
  readonly id: string
  name: string
  nameCn?: string
  readonly realm: CultivatorRealm
  readonly faction: string
  readonly canonId?: string
  readonly canonActor?: CanonActor

  position: THREE.Vector3
  velocity: THREE.Vector3 = new THREE.Vector3()
  target: THREE.Vector3 | null = null
  qi: number
  readonly maxQi: number
  health: number
  readonly maxHealth: number
  hostility: number
  alive: boolean = true

  readonly model: CultivatorModel
  readonly group: THREE.Group
  readonly homePosition: THREE.Vector3

  private currentGoal: Goal | null = null
  private pendingGoal: Goal | null = null

  constructor(opts: EntityCultivatorOptions) {
    this.id = opts.id
    this.name = opts.name
    this.nameCn = opts.nameCn
    this.realm = opts.realm
    this.faction = opts.faction
    this.canonId = opts.canonId
    this.canonActor = opts.canonActor
    this.position = opts.position.clone()
    this.homePosition = (opts.homePosition ?? opts.position).clone()
    this.hostility = opts.hostility
    this.maxQi = REALM_MAX_QI[this.realm]
    this.qi = this.maxQi
    this.maxHealth = REALM_MAX_HEALTH[this.realm]
    this.health = this.maxHealth

    this.model = createCultivatorModel({
      realm: opts.realm,
      gender: opts.gender ?? 'male',
      robeColorOverride: opts.robeColorOverride,
      trimColorOverride: opts.trimColorOverride,
      bladeColor: opts.bladeColorOverride,
      hairColor: opts.hairColor,
    })
    this.group = this.model.group
    this.group.position.copy(this.position)
    this.group.userData.entityId = this.id
    this.group.userData.entityKind = 'cultivator'
  }

  // ───────────────────────── lifecycle ─────────────────────────

  materialize(scene: THREE.Scene): void {
    scene.add(this.group)
  }

  dematerialize(scene: THREE.Scene): void {
    scene.remove(this.group)
    // Dispose geometries/materials to avoid GPU memory leaks.
    disposeObject3D(this.group)
  }

  /** Apply a new goal; the previous goal's onExit is called after the
   *  current update() tick completes. */
  requestGoal(goal: Goal, _ctx: WorldContext): void {
    this.pendingGoal = goal
  }

  /** Synchronous goal override (used by the manager / player). */
  setGoalDirect(goal: Goal, ctx: WorldContext): void {
    if (this.currentGoal?.onExit) this.currentGoal.onExit(this, ctx)
    this.currentGoal = goal
    if (goal.onEnter) goal.onEnter(this, ctx)
  }

  getGoalKind(): string | null {
    return this.currentGoal?.kind ?? null
  }

  // ───────────────────────── per-frame update ─────────────────────────

  update(dt: number, ctx: WorldContext): void {
    if (!this.alive) {
      this.model.update(dt)
      return
    }
    // Regen qi (mod-original rate scaling).
    this.regenQi(dt)

    // Run active goal.
    if (this.currentGoal) {
      const done = this.currentGoal.update(dt, this, ctx)
      if (done) {
        if (this.currentGoal.onExit) this.currentGoal.onExit(this, ctx)
        this.currentGoal = null
      }
    }
    // Swap in pending goal (set by a goal mid-tick).
    if (this.pendingGoal) {
      this.currentGoal = this.pendingGoal
      this.pendingGoal = null
      if (this.currentGoal.onEnter) this.currentGoal.onEnter(this, ctx)
    }
    // Default fallback: idle. The manager decides on a wander goal.
    if (!this.currentGoal) {
      this.model.setAnimation('idle')
    }

    // Integrate velocity into position.
    this.position.addScaledVector(this.velocity, dt)
    // Keep cultivator above ground (terrain or flat).
    const groundY = ctx.sampleHeight(this.position.x, this.position.z)
    if (this.position.y < groundY) this.position.y = groundY
    this.group.position.copy(this.position)

    // Wind-shader + skeletal animation tick.
    this.model.update(dt)
  }

  // ───────────────────────── combat / resources ─────────────────────────

  takeDamage(amount: number, _sourceId?: string): void {
    if (!this.alive) return
    this.health -= amount
    if (this.health <= 0) {
      this.health = 0
      this.alive = false
      this.velocity.set(0, 0, 0)
      this.model.setAnimation('dead', 0.4)
      this.model.setRidingSword(false)
      this.model.setSwordDrawn(false)
    }
  }

  consumeQi(amount: number): boolean {
    if (this.qi < amount) return false
    this.qi -= amount
    return true
  }

  regenQi(dt: number): void {
    const rate = CULTIVATOR_CONSTANTS.QI_REGEN[this.realm]
    const anim = this.model.getAnimation()
    const multiplier = anim === 'meditate'
      ? CULTIVATOR_CONSTANTS.MEDITATE_REGEN_MULTIPLIER
      : 1.0
    this.qi = Math.min(this.maxQi, this.qi + rate * multiplier * dt)
  }

  // ───────────────────────── visual hooks ─────────────────────────

  setAnimation(name: string, fade?: number): void {
    this.model.setAnimation(name as CultivatorAnimName, fade)
  }

  getAnimationName(): string {
    return this.model.getAnimation()
  }

  setSwordDrawn(drawn: boolean): void {
    this.model.setSwordDrawn(drawn)
  }

  setRidingSword(riding: boolean): void {
    this.model.setRidingSword(riding)
  }

  faceDirection(dir: THREE.Vector3, dt: number, turnRate = 4.0): void {
    const len = dir.lengthSq()
    if (len < 1e-6) return
    const targetYaw = Math.atan2(dir.x, dir.z)
    let delta = targetYaw - this.group.rotation.y
    while (delta > Math.PI) delta -= Math.PI * 2
    while (delta < -Math.PI) delta += Math.PI * 2
    const step = Math.sign(delta) * Math.min(Math.abs(delta), turnRate * dt)
    this.group.rotation.y += step
  }

  teleportTo(pos: THREE.Vector3): void {
    this.position.copy(pos)
    this.group.position.copy(pos)
  }

  // ───────────────────────── EntityHandle impl ─────────────────────────

  // (EntityHandle is a structural subset of GoalOwner — already implemented.)
}

// ───────────────────────── sword-qi projectile factory ─────────────────────────

/**
 * Build a luminous jade-crescent projectile mesh for the sword-qi attack.
 * Mod-original: REASONABLE_RECONSTRUCTION (no novel chapter cites for the
 * visual; canon attests cultivators fire sword-qi as a ranged attack).
 */
export function buildSwordQiCrescent(color: number): THREE.Mesh {
  // A flat ring segment — a crescent shape.
  const geo = new THREE.RingGeometry(0.20, 0.55, 24, 1, Math.PI * 0.35, Math.PI * 0.30)
  const mat = new THREE.MeshBasicMaterial({
    color, transparent: true, opacity: 0.85, side: THREE.DoubleSide,
    blending: THREE.AdditiveBlending, depthWrite: false,
  })
  const mesh = new THREE.Mesh(geo, mat)
  // Add an inner brighter core.
  const core = new THREE.Mesh(
    new THREE.RingGeometry(0.30, 0.45, 16, 1, Math.PI * 0.35, Math.PI * 0.30),
    new THREE.MeshBasicMaterial({
      color: 0xffffff, transparent: true, opacity: 0.6, side: THREE.DoubleSide,
      blending: THREE.AdditiveBlending, depthWrite: false,
    }),
  )
  mesh.add(core)
  return mesh
}

/** Convenience: create a sword-qi projectile ready to fire. */
export function createSwordQiProjectile(
  source: EntityCultivator,
  target: THREE.Vector3,
  color: number = 0x5fb88a,
): Projectile {
  const origin = source.position.clone()
  origin.y += 1.4 // chest height
  const dir = new THREE.Vector3().subVectors(target, origin)
  dir.y *= 0.5 // flatten the arc slightly
  dir.normalize()
  const mesh = buildSwordQiCrescent(color)
  mesh.position.copy(origin)
  // Orient crescent to face the direction of travel.
  mesh.lookAt(origin.clone().add(dir))
  return {
    id: nextProjectileId(),
    sourceId: source.id,
    origin,
    position: origin.clone(),
    direction: dir,
    speed: CULTIVATOR_CONSTANTS.SWORD_QI_PROJECTILE_SPEED,
    damage: CULTIVATOR_CONSTANTS.SWORD_QI_DAMAGE,
    lifetime: CULTIVATOR_CONSTANTS.SWORD_QI_PROJECTILE_LIFETIME,
    radius: 0.55,
    mesh,
  }
}

/**
 * FlatWorldContextWithEntities — a FlatWorldContext that also knows how to
 * register/unregister cultivator + beast entities for projectile hit-checks
 * and entity-near queries. Used by the entity manager.
 */
export class FlatWorldContextWithEntities extends FlatWorldContext {
  registerCultivator(c: EntityCultivator): void {
    this.registerEntity(c)
  }
}
