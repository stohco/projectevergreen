/**
 * PlayerEntity — the first-class actor that IS the user.
 *
 * CRITICAL (user correction): The player is NOT Wang Lin. Wang Lin exists
 * in the world as a manifestation NPC (his real self is on the Immortal
 * Astral Continent / 仙罡大陆). The player is a separate, independent
 * actor who writes through WorldFacade as PLAYER provenance.
 *
 * The player has their own:
 *   - Cultivation realm (starts at Qi Condensation — mortal-tier)
 *   - Qi pool (starts at 100, grows with realm)
 *   - Health (100)
 *   - Spirit root aptitude (mod-original: 5 elements, player picks at chargen)
 *   - Position + velocity (physics-driven)
 *   - Inventory (future)
 *   - Karma (tracked via WorldGraph MEMORY + KARMIC_DEBT edges)
 *
 * The player avatar uses the same CultivatorModel as NPCs but with a
 * distinct color palette (ivory white robes — mortal cultivator) so the
 * user can visually distinguish themselves from Wang Lin (jade green).
 */
import * as THREE from 'three'
import { createCultivatorModel, type CultivatorModelHandle, type RealmKey, type AnimKey } from './CultivatorModel'

export type SpiritRoot = 'metal' | 'wood' | 'water' | 'fire' | 'earth' | 'void'

export interface PlayerState {
  name: string
  realm: RealmKey
  qi: number
  maxQi: number
  health: number
  maxHealth: number
  spiritRoot: SpiritRoot
  spiritRootAptitude: number // 1-10 (10 = perfect single root)
  position: THREE.Vector3
  velocity: THREE.Vector3
  yaw: number
  isFlying: boolean
  isMeditating: boolean
}

export interface PlayerHandle {
  group: THREE.Group
  model: CultivatorModelHandle
  state: PlayerState
  update(dt: number): void
  setAnimation(anim: AnimKey): void
  setPosition(x: number, y: number, z: number): void
  setYaw(yaw: number): void
  setFlying(flying: boolean): void
  setMeditating(meditating: boolean): void
  consumeQi(amount: number): boolean
  regenQi(dt: number): void
  takeDamage(amount: number): void
  dispose(): void
}

const REALM_MAX_QI: Record<RealmKey, number> = {
  qi_condensation: 100,
  foundation: 500,
  core_formation: 2000,
  nascent_soul: 8000,
  soul_transformation: 30000,
  ascendant: 100000,
}

const REALM_QI_REGEN: Record<RealmKey, number> = {
  qi_condensation: 1.0,
  foundation: 3.0,
  core_formation: 8.0,
  nascent_soul: 20.0,
  soul_transformation: 50.0,
  ascendant: 150.0,
}

/**
 * Create the player avatar. Default: ivory-white robes (mortal cultivator),
 * male, Qi Condensation realm. The player is a traveler who has just begun
 * the cultivation path — NOT Wang Lin, NOT a sect disciple.
 */
export function createPlayer(opts?: {
  name?: string
  spiritRoot?: SpiritRoot
  startPosition?: [number, number, number]
}): PlayerHandle {
  const group = new THREE.Group()
  // Player starts as a MORTAL — no cultivation yet. Uses qi_condensation model
  // (the lowest realm) but with mortal stats: 0 qi, 0 maxQi, cannot cast.
  // The player must discover cultivation in-world (NMS-style: find a sect,
  // learn from an elder, or find a technique scroll).
  // Mortal wears rough brown peasant clothes (robeColorOverride).
  const model = createCultivatorModel('qi_condensation', false, 0x8a7a5a)

  group.add(model.group)

  const startPos = opts?.startPosition ?? [8, 65, 12]
  group.position.set(startPos[0], startPos[1], startPos[2])
  model.group.position.set(0, 0, 0)

  const state: PlayerState = {
    name: opts?.name ?? 'Mortal',
    realm: 'qi_condensation', // model key only — player is functionally a mortal
    qi: 0,           // MORTAL: no qi yet. Must discover cultivation in-world.
    maxQi: 0,        // MORTAL: no qi pool until first cultivation technique learned.
    health: 100,
    maxHealth: 100,
    spiritRoot: opts?.spiritRoot ?? 'wood',
    spiritRootAptitude: 7,
    position: new THREE.Vector3(startPos[0], startPos[1], startPos[2]),
    velocity: new THREE.Vector3(),
    yaw: 0,
    isFlying: false,
    isMeditating: false,
  }

  return {
    group,
    model,
    state,
    update(dt: number) {
      model.update(dt)
      // Sync group position from state.
      group.position.copy(state.position)
      group.rotation.y = state.yaw
      // Qi regen.
      if (!state.isFlying) {
        this.regenQi(dt)
      }
    },
    setAnimation(anim: AnimKey) {
      model.setAnimation(anim)
    },
    setPosition(x: number, y: number, z: number) {
      state.position.set(x, y, z)
      group.position.set(x, y, z)
    },
    setYaw(yaw: number) {
      state.yaw = yaw
      group.rotation.y = yaw
    },
    setFlying(flying: boolean) {
      state.isFlying = flying
      model.setAnimation(flying ? 'fly' : 'idle')
      model.setSwordVisible(flying)
    },
    setMeditating(meditating: boolean) {
      state.isMeditating = meditating
      if (meditating) {
        model.setAnimation('cast')
        model.setAuraVisible(true)
      } else {
        model.setAnimation('idle')
        model.setAuraVisible(false)
      }
    },
    consumeQi(amount: number): boolean {
      if (state.qi < amount) return false
      state.qi -= amount
      return true
    },
    regenQi(dt: number) {
      const baseRegen = REALM_QI_REGEN[state.realm]
      const medMultiplier = state.isMeditating ? 3.0 : 1.0
      state.qi = Math.min(state.maxQi, state.qi + baseRegen * medMultiplier * dt)
    },
    takeDamage(amount: number) {
      state.health = Math.max(0, state.health - amount)
    },
    dispose() {
      model.dispose()
    },
  }
}

export { REALM_MAX_QI, REALM_QI_REGEN }
