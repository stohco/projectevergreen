/**
 * CultivatorSwordQiGoal.ts — sword-qi (剑气) attack goal.
 *
 * Faithful to the 仙逆 simulation contract:
 *   - Cultivators fire crescent-shaped qi projectiles at targets (canon
 *     attests sword-qi as the primary ranged attack).
 *   - Qi cost: 5.0 qi per shot (CRON-138 port).
 *   - 5% maxQi gate before firing (CRON-138 port).
 *   - Projectile: luminous jade crescent, 60 blocks/sec, 3 sec lifetime.
 *   - Cooldown: 1.5 sec between shots.
 *
 * Mod-original: REASONABLE_RECONSTRUCTION, conf 3. NO invented chapter cites.
 */
import * as THREE from 'three'
import {
  type Goal, type GoalOwner, type WorldContext,
  CULTIVATOR_CONSTANTS,
} from './Goal'
import { createSwordQiProjectile } from '../entities/EntityCultivator'

export interface CultivatorSwordQiGoalOptions {
  /** Living target handle (the goal queries its position each tick). */
  targetId: string
  /** Effective range — beyond this, the goal completes (out of range). */
  range?: number
  /** Maximum engagement duration — abort after this many seconds. */
  maxDuration?: number
}

/**
 * CultivatorSwordQiGoal — fires sword-qi at a target while it remains in
 * range. Completes when target dies, leaves range, or qi runs low.
 */
export class CultivatorSwordQiGoal implements Goal {
  readonly kind = 'sword_qi' as const
  private targetId: string
  private range: number
  private maxDuration: number
  private cooldown = 0
  private elapsed = 0
  private fired = false

  constructor(opts: CultivatorSwordQiGoalOptions) {
    this.targetId = opts.targetId
    this.range = opts.range ?? 40
    this.maxDuration = opts.maxDuration ?? 12
  }

  onEnter(owner: GoalOwner, _ctx: WorldContext): void {
    owner.setSwordDrawn(true)
    owner.setAnimation('sword_qi_fire', 0.25)
  }

  onExit(owner: GoalOwner, _ctx: WorldContext): void {
    owner.setSwordDrawn(false)
    owner.setAnimation('idle', 0.3)
    owner.velocity.set(0, 0, 0)
  }

  update(dt: number, owner: GoalOwner, ctx: WorldContext): boolean {
    this.elapsed += dt
    if (this.cooldown > 0) this.cooldown -= dt
    // Time out.
    if (this.elapsed > this.maxDuration) return true
    // Locate target.
    const target = ctx.entitiesNear(owner.position, this.range + 5)
      .find((e) => e.id === this.targetId)
    if (!target || !target.alive) return true
    // Face the target.
    const toTarget = new THREE.Vector3().subVectors(target.position, owner.position)
    const dist = toTarget.length()
    owner.faceDirection(toTarget, dt, 5.0)
    // Out of range?
    if (dist > this.range) return true
    // Stop moving while firing (cultivators brace).
    owner.velocity.multiplyScalar(1 - Math.min(1, dt * 6))
    // Qi gate (CRON-138: 5% maxQi).
    if (owner.qi < owner.maxQi * CULTIVATOR_CONSTANTS.SWORD_QI_GATE_RATIO) {
      return true
    }
    // Fire on cooldown.
    if (this.cooldown <= 0) {
      if (owner.consumeQi(CULTIVATOR_CONSTANTS.SWORD_QI_COST)) {
        const proj = createSwordQiProjectile(
          owner as unknown as Parameters<typeof createSwordQiProjectile>[0],
          target.position.clone(),
          swordQiColorForOwner(owner),
        )
        ctx.spawnProjectile(proj)
        this.cooldown = CULTIVATOR_CONSTANTS.SWORD_QI_COOLDOWN
        this.fired = true
        // Re-trigger the firing pose for the next shot.
        owner.setAnimation('sword_qi_fire', 0.1)
      } else {
        return true
      }
    }
    return false
  }

  /** Did this goal fire at least one shot? (For AI evaluation.) */
  didFire(): boolean { return this.fired }
}

/** Pick a sword-qi color from the owner's realm (matches robe palette). */
function swordQiColorForOwner(owner: GoalOwner): number {
  switch (owner.realm) {
    case 'qi_condensation':     return 0xfff8e0
    case 'foundation':          return 0x5fb88a
    case 'core_formation':      return 0x4a8cd4
    case 'nascent_soul':        return 0xe8c66a
    case 'soul_transformation': return 0xb08ce0
    case 'ascendant':           return 0xff6060
    default:                    return 0x5fb88a
  }
}
