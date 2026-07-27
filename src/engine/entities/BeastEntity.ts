/**
 * BeastEntity.ts — runtime entity for a spirit beast (灵兽).
 *
 * Wraps a BeastModel and provides inline AI (wander → aggro → attack → flee)
 * plus the GoalOwner interface so it can be targeted by cultivator sword-qi.
 *
 * Faithful to the 仙逆 simulation contract:
 *   - Beasts spawn in biome-appropriate locations (frost wolf in snow, etc.).
 *   - On death, they drop a qi-crystal block (mod-original; canon attests
 *     beasts have qi crystals / cores that cultivators harvest).
 *   - Beasts flee when low on health (canon: weaker beasts retreat from
 *     superior opponents).
 *
 * Mod-original: REASONABLE_RECONSTRUCTION, conf 3. NO invented chapter cites.
 */
import * as THREE from 'three'
import {
  type WorldContext, type EntityHandle, type GoalOwner, type Goal,
  type Projectile,
} from '../ai/Goal'
import {
  BeastModel, createBeastModel, type BeastKind, type BeastAnimName,
} from './BeastModel'

export interface BeastEntityOptions {
  id: string
  kind: BeastKind
  position: THREE.Vector3
  homePosition?: THREE.Vector3
  aggression?: number  // 0..100
  maxHealth?: number
  damage?: number
}

/** A death-drop pickup (qi crystal / herb). */
export interface BeastPickup {
  id: string
  kind: 'qi_crystal' | 'spirit_herb'
  position: THREE.Vector3
  mesh: THREE.Object3D
  ttl: number  // seconds remaining
  spin: number
}

type BeastState = 'wander' | 'aggro' | 'attack' | 'flee' | 'dead'

const BEAST_BASE_HEALTH: Record<BeastKind, number> = {
  frost_wolf: 120,
  flame_tiger: 180,
  jade_serpent: 280,
  thunder_hawk: 140,
}

const BEAST_BASE_DAMAGE: Record<BeastKind, number> = {
  frost_wolf: 12,
  flame_tiger: 18,
  jade_serpent: 22,
  thunder_hawk: 16,
}

/**
 * BeastEntity — a single spirit beast in the world.
 */
export class BeastEntity implements GoalOwner, EntityHandle {
  readonly id: string
  readonly kind: BeastKind
  readonly faction: string = 'beast'
  name: string
  realm: string = 'rank_2'  // mod-original: beasts have rank 1..9 (canon)
  position: THREE.Vector3
  velocity: THREE.Vector3 = new THREE.Vector3()
  target: THREE.Vector3 | null = null
  qi: number = 0
  maxQi: number = 0
  health: number
  readonly maxHealth: number
  hostility: number
  alive: boolean = true

  readonly model: BeastModel
  readonly group: THREE.Group
  readonly homePosition: THREE.Vector3

  private state: BeastState = 'wander'
  private aggroTarget: EntityHandle | null = null
  private attackCooldown = 0
  private wanderTimer = 0
  private wanderDir = new THREE.Vector3()
  private fleeTimer = 0
  private damageDealt: number

  constructor(opts: BeastEntityOptions) {
    this.id = opts.id
    this.kind = opts.kind
    this.name = opts.kind.replace('_', ' ')
    this.position = opts.position.clone()
    this.homePosition = (opts.homePosition ?? opts.position).clone()
    this.hostility = opts.aggression ?? 50
    this.maxHealth = opts.maxHealth ?? BEAST_BASE_HEALTH[opts.kind]
    this.health = this.maxHealth
    this.damageDealt = opts.damage ?? BEAST_BASE_DAMAGE[opts.kind]

    this.model = createBeastModel(opts.kind)
    this.group = this.model.group
    this.group.position.copy(this.position)
    this.group.userData.entityId = this.id
    this.group.userData.entityKind = 'beast'
  }

  // ───────────────────────── lifecycle ─────────────────────────

  materialize(scene: THREE.Scene): void {
    scene.add(this.group)
  }

  dematerialize(scene: THREE.Scene): void {
    scene.remove(this.group)
  }

  // ───────────────────────── per-frame update ─────────────────────────

  update(dt: number, ctx: WorldContext): void {
    if (!this.alive) {
      this.model.update(dt)
      return
    }
    this.aiTick(dt, ctx)
    this.position.addScaledVector(this.velocity, dt)
    // Keep the beast on the ground.
    const groundY = ctx.sampleHeight(this.position.x, this.position.z)
    const hoverOffset = this.kind === 'thunder_hawk' ? 2.5 : 0.0
    const targetY = groundY + hoverOffset
    if (this.position.y < targetY) this.position.y = targetY
    this.group.position.copy(this.position)
    this.model.update(dt)
    if (this.attackCooldown > 0) this.attackCooldown -= dt
  }

  private aiTick(dt: number, ctx: WorldContext): void {
    const hpRatio = this.health / this.maxHealth

    // Flee when low health (canon: weak beasts retreat from superior foes).
    if (hpRatio < 0.3) {
      this.stateFlee(dt, ctx)
      return
    }

    // Lose aggro if target died.
    if (this.aggroTarget && !this.aggroTarget.alive) {
      this.aggroTarget = null
      this.state = 'wander'
    }

    // Look for aggro target if none.
    if (!this.aggroTarget) {
      const nearby = ctx.entitiesNear(this.position, 14)
      for (const e of nearby) {
        if (e.hostility > 30 || e.faction === 'player') {
          this.aggroTarget = e
          this.state = 'aggro'
          break
        }
      }
    }

    if (this.aggroTarget) {
      const dist = this.position.distanceTo(this.aggroTarget.position)
      if (dist < 2.2) {
        this.stateAttack(dt, ctx)
      } else {
        this.stateAggro(dt, ctx)
      }
    } else {
      this.stateWander(dt, ctx)
    }
  }

  private stateWander(dt: number, ctx: WorldContext): void {
    if (this.state !== 'wander') {
      this.state = 'wander'
      this.wanderTimer = 1 + Math.random() * 3
      this.pickWanderDir()
      this.model.setAnimation('walk', 0.3)
    }
    this.wanderTimer -= dt
    if (this.wanderTimer <= 0) {
      // Pause or pick a new direction.
      if (Math.random() < 0.3) {
        this.model.setAnimation('idle', 0.3)
        this.velocity.set(0, 0, 0)
        this.wanderTimer = 1 + Math.random() * 2
      } else {
        this.pickWanderDir()
        this.model.setAnimation('walk', 0.3)
        this.wanderTimer = 2 + Math.random() * 4
      }
    }
    const homeDist = this.position.distanceTo(this.homePosition)
    if (homeDist > 18) {
      // Steer back home.
      this.wanderDir.subVectors(this.homePosition, this.position).setY(0).normalize()
    }
    const speed = this.kind === 'thunder_hawk' ? 4 : 2
    this.velocity.set(
      this.wanderDir.x * speed,
      0,
      this.wanderDir.z * speed,
    )
    this.faceDirection(this.wanderDir, dt)
  }

  private stateAggro(dt: number, _ctx: WorldContext): void {
    if (this.state !== 'aggro') {
      this.state = 'aggro'
      this.model.setAnimation('run', 0.3)
    }
    if (!this.aggroTarget) return
    const dir = new THREE.Vector3().subVectors(this.aggroTarget.position, this.position)
    dir.y = 0
    const dist = dir.length()
    if (dist > 0.1) dir.normalize()
    const speed = this.kind === 'thunder_hawk' ? 14 : 8
    this.velocity.set(dir.x * speed, 0, dir.z * speed)
    this.faceDirection(dir, dt)
  }

  private stateAttack(dt: number, ctx: WorldContext): void {
    if (this.state !== 'attack') {
      this.state = 'attack'
      this.model.setAnimation('attack', 0.2)
      this.attackCooldown = 1.0
    }
    this.velocity.set(0, 0, 0)
    if (this.aggroTarget) {
      const dir = new THREE.Vector3().subVectors(this.aggroTarget.position, this.position)
      dir.y = 0
      this.faceDirection(dir, dt)
      if (this.attackCooldown <= 0) {
        // Apply damage on the strike frame.
        this.aggroTarget.takeDamage(this.damageDealt, this.id)
        this.attackCooldown = 1.2
        this.model.setAnimation('attack', 0.1)
      }
    }
    // Return to aggro after the strike.
    if (this.attackCooldown < 0.6) {
      this.state = 'aggro'
      this.model.setAnimation('idle', 0.3)
    }
  }

  private stateFlee(dt: number, ctx: WorldContext): void {
    if (this.state !== 'flee') {
      this.state = 'flee'
      this.fleeTimer = 4 + Math.random() * 3
      this.model.setAnimation('run', 0.3)
    }
    this.fleeTimer -= dt
    // Run directly away from the aggro target (or random direction).
    let dir: THREE.Vector3
    if (this.aggroTarget) {
      dir = new THREE.Vector3().subVectors(this.position, this.aggroTarget.position)
    } else {
      dir = this.wanderDir.clone()
    }
    dir.y = 0
    if (dir.lengthSq() < 1e-4) dir.set(Math.random() - 0.5, 0, Math.random() - 0.5)
    dir.normalize()
    const speed = this.kind === 'thunder_hawk' ? 16 : 10
    this.velocity.set(dir.x * speed, 0, dir.z * speed)
    this.faceDirection(dir, dt)
    if (this.fleeTimer <= 0) {
      this.aggroTarget = null
      this.state = 'wander'
    }
  }

  private pickWanderDir(): void {
    const angle = Math.random() * Math.PI * 2
    this.wanderDir.set(Math.cos(angle), 0, Math.sin(angle))
  }

  // ───────────────────────── combat / death ─────────────────────────

  takeDamage(amount: number, sourceId?: string): void {
    if (!this.alive) return
    this.health -= amount
    // Aggro onto the attacker.
    if (sourceId) {
      // The manager's nearby query will pick this up next tick.
    }
    if (this.health <= 0) {
      this.health = 0
      this.alive = false
      this.velocity.set(0, 0, 0)
      this.state = 'dead'
      this.model.setAnimation('death', 0.3)
    }
  }

  consumeQi(_amount: number): boolean { return true }
  regenQi(_dt: number): void { /* beasts regen via food, not qi */ }

  // ───────────────────────── visual hooks ─────────────────────────

  setAnimation(name: string, fade?: number): void {
    this.model.setAnimation(name as BeastAnimName, fade)
  }
  getAnimationName(): string {
    return this.model.getAnimation()
  }
  setSwordDrawn(_drawn: boolean): void { /* no-op */ }
  setRidingSword(_riding: boolean): void { /* no-op */ }

  faceDirection(dir: THREE.Vector3, dt: number, turnRate = 5.0): void {
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

  // GoalOwner no-ops (beasts use inline AI, not the goal system).
  requestGoal(_goal: Goal, _ctx: WorldContext): void { /* no-op */ }
  setGoalDirect(_goal: Goal, _ctx: WorldContext): void { /* no-op */ }
  getGoalKind(): string | null { return this.state }

  // ───────────────────────── death drops ─────────────────────────

  /** Build a qi-crystal pickup at the beast's death position. */
  spawnDeathPickup(): BeastPickup {
    const isCrystal = Math.random() < 0.7
    const color = isCrystal ? 0x9be15d : 0xe8c66a
    const geo = isCrystal
      ? new THREE.OctahedronGeometry(0.18, 0)
      : new THREE.IcosahedronGeometry(0.16, 0)
    const mat = new THREE.MeshStandardMaterial({
      color, roughness: 0.15, metalness: 0.4,
      emissive: color, emissiveIntensity: 0.6,
      transparent: true, opacity: 0.92,
    })
    const mesh = new THREE.Mesh(geo, mat)
    mesh.position.copy(this.position)
    mesh.position.y = (this.position.y) + 0.4
    return {
      id: `pickup-${this.id}`,
      kind: isCrystal ? 'qi_crystal' : 'spirit_herb',
      position: mesh.position.clone(),
      mesh,
      ttl: 60,
      spin: 1 + Math.random() * 1.5,
    }
  }
}
