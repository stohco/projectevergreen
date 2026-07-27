/**
 * CultivatorFlightGoal.ts — sword-flight (御剑飞行) goal.
 *
 * Faithful to the 仙逆 simulation contract:
 *   - Foundation-realm cultivators and above can sword-fly (canon).
 *   - Flight speed scales with realm (40 blocks/sec at Foundation → 220 at
 *     Ascendant). Mod-original tuning (REASONABLE_RECONSTRUCTION, conf 3).
 *   - Qi cost: 2.0 qi/sec while flying (CRON-134 port).
 *   - Abort if qi < 5% of maxQi (CRON-134 port).
 *   - Obstacle avoidance via FlightNavigator: upward ray-cast + TALL_VAULT
 *     3.0D dodge (CRON-135/136 port).
 *   - Smooth banking turns: roll the cultivator up to 30 degrees based on
 *     turn rate (mod-original flourish for AAA feel).
 */
import * as THREE from 'three'
import {
  type Goal, type GoalOwner, type WorldContext, type CultivatorRealm,
  CULTIVATOR_CONSTANTS,
} from './Goal'
import { FlightNavigator } from './FlightNavigator'

export interface CultivatorFlightGoalOptions {
  /** Where to fly to. */
  target: THREE.Vector3
  /** Arrival radius — goal completes when within this distance. */
  arrivalRadius?: number
  /** Hover altitude above terrain. */
  cruiseAltitude?: number
}

/**
 * CultivatorFlightGoal — flies the cultivator to `target` on a ride-sword.
 */
export class CultivatorFlightGoal implements Goal {
  readonly kind = 'flight' as const
  private target: THREE.Vector3
  private arrivalRadius: number
  private cruiseAltitude: number
  private elapsed = 0

  constructor(opts: CultivatorFlightGoalOptions) {
    this.target = opts.target.clone()
    this.arrivalRadius = opts.arrivalRadius ?? 3.0
    this.cruiseAltitude = opts.cruiseAltitude ?? 8.0
  }

  onEnter(owner: GoalOwner, _ctx: WorldContext): void {
    // Qi Condensation cultivators cannot fly — abort immediately.
    if (owner.realm === 'qi_condensation') return
    owner.setRidingSword(true)
    owner.setSwordDrawn(false)
    owner.setAnimation('sword_flight', 0.4)
  }

  onExit(owner: GoalOwner, _ctx: WorldContext): void {
    owner.setRidingSword(false)
    owner.setAnimation('idle', 0.3)
    owner.velocity.set(0, 0, 0)
  }

  update(dt: number, owner: GoalOwner, ctx: WorldContext): boolean {
    this.elapsed += dt
    // Qi-condensation cannot fly.
    if (owner.realm === 'qi_condensation') return true
    // Abort if qi below 5% (CRON-134).
    if (owner.qi < owner.maxQi * CULTIVATOR_CONSTANTS.FLIGHT_QI_ABORT_RATIO) {
      return true
    }
    // Consume qi (CRON-134: 2.0 qi/sec).
    if (!owner.consumeQi(CULTIVATOR_CONSTANTS.FLIGHT_QI_PER_SEC * dt)) {
      return true
    }
    // Arrived?
    const distToTarget = owner.position.distanceTo(this.target)
    if (distToTarget < this.arrivalRadius) {
      return true
    }
    // Cruise altitude: lift the cultivator to cruise height above ground.
    const groundY = ctx.sampleHeight(owner.position.x, owner.position.z)
    const cruiseY = groundY + this.cruiseAltitude
    const flightTarget = this.target.clone()
    flightTarget.y = Math.max(flightTarget.y, ctx.sampleHeight(this.target.x, this.target.z) + this.cruiseAltitude)
    // Steer.
    const maxSpeed = CULTIVATOR_CONSTANTS.FLIGHT_SPEED[owner.realm as CultivatorRealm] ?? 40
    const result = FlightNavigator.steer(
      owner.position, flightTarget, owner.velocity, maxSpeed, ctx, dt,
    )
    // Apply smooth banking — roll the cultivator up to 30 degrees.
    const targetRoll = -result.banking * Math.PI / 6 // 30° max
    // Owner.group is the cultivator's group; we set rotation.z for roll.
    // (Access via the underlying Object3D on the owner.)
    ;(owner as unknown as { group: THREE.Group }).group.rotation.z =
      THREE.MathUtils.lerp(
        (owner as unknown as { group: THREE.Group }).group.rotation.z,
        targetRoll,
        1 - Math.exp(-8 * dt),
      )
    // Face direction of travel.
    const horizVel = new THREE.Vector3(owner.velocity.x, 0, owner.velocity.z)
    if (horizVel.lengthSq() > 1.0) {
      owner.faceDirection(horizVel, dt, 3.5)
    }
    // If we're well below cruise altitude, nudge upward explicitly.
    if (owner.position.y < cruiseY - 1.0) {
      owner.velocity.y += 10 * dt
    }
    return false
  }
}
