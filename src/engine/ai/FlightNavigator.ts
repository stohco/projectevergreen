/**
 * FlightNavigator.ts — sword-flight pathfinding + obstacle avoidance for
 * cultivators (port of CRON-133/135/136 from the Forge codebase).
 *
 *   • Multi-height ray-cast (3 samples at 0.0, 1.0, 2.0 above current pos)
 *     to detect obstacles in front of the cultivator.
 *   • Tall-obstacle vault (TALL_VAULT 3.0D): if the upper sample (2.0D) hits
 *     close but the lower samples are clear, the cultivator vaults upward
 *     rather than steering around — preserves forward momentum.
 *   • Smooth steering: exponential-damping acceleration toward the desired
 *     velocity, capped at max speed.
 *
 * Mod-original: REASONABLE_RECONSTRUCTION, conf 3. NO invented chapter cites.
 */
import * as THREE from 'three'
import {
  type WorldContext,
  CULTIVATOR_CONSTANTS,
} from './Goal'

export interface ObstacleReport {
  blocked: boolean
  distance: number         // -1 if no hit
  vault: boolean           // true if obstacle is tall (2.0D sample hit close)
  hitPoint: THREE.Vector3 | null
  hitNormal: THREE.Vector3 | null
}

export interface SteerResult {
  shouldVault: boolean
  /** The post-steering velocity (same reference as the input `velocity`). */
  velocity: THREE.Vector3
  /** Whether the cultivator is currently banking (turning hard). */
  banking: number  // -1..1, negative = left roll, positive = right roll
}

export class FlightNavigator {
  /** Multi-sample ray-cast obstacle detection. */
  static detectObstacle(
    pos: THREE.Vector3,
    dir: THREE.Vector3,
    ctx: WorldContext,
    maxDist = 12.0,
  ): ObstacleReport {
    const samples = [0.0, 1.0, 2.0]
    let minDist = Infinity
    let vault = false
    let hitPoint: THREE.Vector3 | null = null
    let hitNormal: THREE.Vector3 | null = null
    for (const yOff of samples) {
      const origin = pos.clone()
      origin.y += yOff
      const hit = ctx.rayCast(origin, dir, maxDist)
      if (hit) {
        if (hit.distance < minDist) {
          minDist = hit.distance
          hitPoint = hit.point.clone()
          hitNormal = hit.normal.clone()
        }
        // CRON-136: tall-obstacle detection — if the 2.0D sample hits close
        // but the lower samples are clear, the cultivator should vault.
        if (yOff >= 2.0 && hit.distance < 8.0) {
          vault = true
        }
      }
    }
    return {
      blocked: minDist < 6.0,
      distance: minDist === Infinity ? -1 : minDist,
      vault,
      hitPoint,
      hitNormal,
    }
  }

  /**
   * Compute the desired velocity toward `target`, with obstacle avoidance
   * and smooth steering. Mutates `velocity` in-place.
   */
  static steer(
    pos: THREE.Vector3,
    target: THREE.Vector3,
    velocity: THREE.Vector3,
    maxSpeed: number,
    ctx: WorldContext,
    dt: number,
  ): SteerResult {
    const toTarget = new THREE.Vector3().subVectors(target, pos)
    const dist = toTarget.length()
    const desired = new THREE.Vector3()
    if (dist > 0.5) {
      desired.copy(toTarget).multiplyScalar(1 / dist).multiplyScalar(maxSpeed)
    }
    // Forward ray-cast for obstacle detection.
    const fwd = desired.clone()
    if (fwd.lengthSq() > 1e-4) fwd.normalize()
    else fwd.set(0, 0, 1)
    const obstacle = FlightNavigator.detectObstacle(pos, fwd, ctx)
    let shouldVault = false
    if (obstacle.blocked) {
      if (obstacle.vault) {
        // CRON-136: tall-obstacle vault — pop upward and continue forward.
        desired.y += CULTIVATOR_CONSTANTS.TALL_VAULT * 2.5
        shouldVault = true
      } else {
        // Steer around — pick the perpendicular direction closer to the
        // current heading (avoid reversing).
        const right = new THREE.Vector3(-fwd.z, 0, fwd.x).normalize()
        const left = right.clone().multiplyScalar(-1)
        // Prefer the side that requires less turn from current velocity.
        const rightScore = right.dot(velocity) + right.dot(desired)
        const leftScore = left.dot(velocity) + left.dot(desired)
        const side = rightScore > leftScore ? right : left
        desired.add(side.multiplyScalar(maxSpeed * 0.8))
        desired.y += 0.5 // slight climb to clear low obstacles
      }
    }
    // Smooth steering: exponential-damping toward desired velocity.
    const lambda = 6.0
    velocity.x = damp(velocity.x, desired.x, lambda, dt)
    velocity.y = damp(velocity.y, desired.y, lambda, dt)
    velocity.z = damp(velocity.z, desired.z, lambda, dt)
    // Cap to max speed.
    const speed = velocity.length()
    if (speed > maxSpeed) velocity.multiplyScalar(maxSpeed / speed)
    // Compute banking — dot product of horizontal velocity change with the
    // right vector (positive = banking right).
    const horizontalVel = new THREE.Vector3(velocity.x, 0, velocity.z)
    const right2 = new THREE.Vector3(-fwd.z, 0, fwd.x).normalize()
    const bank = horizontalVel.dot(right2) / Math.max(speed, 0.001)
    return { shouldVault, velocity, banking: THREE.MathUtils.clamp(bank, -1, 1) }
  }
}

/** Frame-rate-independent damping (exponential approach). */
function damp(current: number, target: number, lambda: number, dt: number): number {
  return THREE.MathUtils.lerp(current, target, 1 - Math.exp(-lambda * dt))
}
