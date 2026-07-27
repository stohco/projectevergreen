/**
 * CultivatorWanderGoal.ts — idle / wander goal for cultivators.
 *
 * Faithful to the 仙逆 simulation contract:
 *   - Cultivators walk slowly around their home position (canon: mortal and
 *     low-realm cultivators spend most of their time at home / in their
 *     sect, not flying).
 *   - Occasionally they pause, and occasionally they sit and meditate to
 *     regen qi (meditation triples regen — see EntityCultivator.regenQi).
 *   - If a target is far, switch to FlightGoal (御剑飞行) — but only at
 *     Foundation realm and above (Qi Condensation cannot fly).
 *   - If a target is hostile and in range, switch to SwordQiGoal (剑气).
 *
 * Mod-original: REASONABLE_RECONSTRUCTION, conf 3.
 */
import * as THREE from 'three'
import { type Goal, type GoalOwner, type WorldContext } from './Goal'
import { CultivatorFlightGoal } from './CultivatorFlightGoal'
import { CultivatorSwordQiGoal } from './CultivatorSwordQiGoal'

type WanderSubState = 'walking' | 'pausing' | 'meditating'

export interface CultivatorWanderGoalOptions {
  /** Center of the wander area. */
  homePosition?: THREE.Vector3
  /** Wander radius (default 8 blocks). */
  radius?: number
}

const SWORD_QI_RANGE = 40
const FLIGHT_TRIGGER_DIST = 60

/**
 * CultivatorWanderGoal — wander around home, occasionally meditate, and
 * hand off to combat / flight goals when a target appears.
 */
export class CultivatorWanderGoal implements Goal {
  readonly kind = 'wander' as const
  private home: THREE.Vector3
  private radius: number
  private sub: WanderSubState = 'walking'
  private subTimer = 1.0
  private wanderTarget: THREE.Vector3 = new THREE.Vector3()

  constructor(opts: CultivatorWanderGoalOptions = {}) {
    this.home = opts.homePosition ?? new THREE.Vector3()
    this.radius = opts.radius ?? 8
    this.pickWanderTarget()
  }

  onEnter(owner: GoalOwner, _ctx: WorldContext): void {
    if (this.home.lengthSq() < 1e-4) this.home.copy(owner.position)
    owner.setAnimation('walk', 0.4)
    this.sub = 'walking'
    this.subTimer = 2 + Math.random() * 3
  }

  onExit(owner: GoalOwner, _ctx: WorldContext): void {
    owner.setAnimation('idle', 0.3)
    owner.velocity.set(0, 0, 0)
  }

  update(dt: number, owner: GoalOwner, ctx: WorldContext): boolean {
    // ── Combat transitions ──────────────────────────────────────────
    // Find nearby entities; pick a hostile one.
    const nearby = ctx.entitiesNear(owner.position, FLIGHT_TRIGGER_DIST + 5)
    const hostile = nearby.find((e) => e.alive && (e.hostility > 30 || e.faction !== owner.faction) && e.id !== owner.id)

    if (hostile) {
      const dist = owner.position.distanceTo(hostile.position)
      owner.target = hostile.position.clone()
      // If hostile and in sword-qi range, fire.
      if (dist <= SWORD_QI_RANGE && owner.qi > owner.maxQi * 0.10) {
        owner.requestGoal(
          new CultivatorSwordQiGoal({ targetId: hostile.id, range: SWORD_QI_RANGE }),
          ctx,
        )
        return true
      }
      // If hostile is far and we can fly, chase.
      if (
        dist > SWORD_QI_RANGE + 5 &&
        owner.realm !== 'qi_condensation' &&
        owner.qi > owner.maxQi * 0.20
      ) {
        owner.requestGoal(
          new CultivatorFlightGoal({ target: hostile.position.clone() }),
          ctx,
        )
        return true
      }
    }

    // ── Wander sub-state machine ────────────────────────────────────
    this.subTimer -= dt
    if (this.subTimer <= 0) {
      this.transitionSubState(owner)
    }

    if (this.sub === 'walking') {
      // Walk toward the wander target.
      const toTarget = new THREE.Vector3().subVectors(this.wanderTarget, owner.position)
      toTarget.y = 0
      const dist = toTarget.length()
      if (dist < 0.8) {
        this.pickWanderTarget()
      } else {
        toTarget.normalize()
        const speed = 1.6
        owner.velocity.set(toTarget.x * speed, 0, toTarget.z * speed)
        owner.faceDirection(toTarget, dt, 3.0)
        // Keep cultivator on the ground.
        const groundY = ctx.sampleHeight(owner.position.x, owner.position.z)
        if (owner.position.y < groundY) owner.position.y = groundY
        if (owner.getAnimationName() !== 'walk') {
          owner.setAnimation('walk', 0.3)
        }
      }
    } else if (this.sub === 'pausing') {
      owner.velocity.multiplyScalar(1 - Math.min(1, dt * 5))
      // Subtle idle look-around.
      const look = new THREE.Vector3(Math.sin(this.subTimer * 0.6), 0, Math.cos(this.subTimer * 0.6))
      owner.faceDirection(look, dt, 1.0)
    } else if (this.sub === 'meditating') {
      owner.velocity.set(0, 0, 0)
      // Sit on the ground.
      const groundY = ctx.sampleHeight(owner.position.x, owner.position.z)
      owner.position.y = groundY
    }

    return false
  }

  private transitionSubState(owner: GoalOwner): void {
    // Pick a new sub-state weighted toward walking.
    const roll = Math.random()
    if (roll < 0.55) {
      this.sub = 'walking'
      this.subTimer = 3 + Math.random() * 4
      this.pickWanderTarget()
      owner.setAnimation('walk', 0.4)
    } else if (roll < 0.80) {
      this.sub = 'pausing'
      this.subTimer = 2 + Math.random() * 3
      owner.setAnimation('idle', 0.4)
    } else {
      // Meditate only if qi is below 80%.
      if (owner.qi < owner.maxQi * 0.8) {
        this.sub = 'meditating'
        this.subTimer = 6 + Math.random() * 6
        owner.setAnimation('meditate', 0.6)
      } else {
        this.sub = 'pausing'
        this.subTimer = 2 + Math.random() * 2
        owner.setAnimation('idle', 0.4)
      }
    }
  }

  private pickWanderTarget(): void {
    const angle = Math.random() * Math.PI * 2
    const r = Math.random() * this.radius
    this.wanderTarget.set(
      this.home.x + Math.cos(angle) * r,
      this.home.y,
      this.home.z + Math.sin(angle) * r,
    )
  }
}
