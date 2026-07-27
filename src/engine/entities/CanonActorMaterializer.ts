/**
 * CanonActorMaterializer.ts — spawns canon NPCs as EntityCultivators at
 * their placed Planet-Suzaku positions.
 *
 * Faithful to the 仙逆 simulation contract (canon fidelity rules):
 *   - Wang Lin (王林, canon N01) at the remote Zhao village (0, 0). Foundation
 *     realm, jade-green robes, jade flying sword. The player's avatar /
 *     ally. (Canon: Wang Lin starts as a mortal and joins Heng Yue Sect;
 *     we materialize him at Foundation — his sect realm after his first
 *     major breakthrough — for an interesting sim start. The canon peak
 *     realm is Heaven Trampling; we don't simulate that here.)
 *   - Teng Li (藤立, canon N84) at the village. Qi Condensation, hostile to
 *     Wang Lin, dark robes. He is Wang Lin's first kill in the novel; we
 *     materialize him at the village as an early antagonist. (Canon peak
 *     realm: late Foundation; we start him at Qi Condensation for a fair
 *     early-game fight. Mod-original gameplay tuning, REASONABLE_RECONSTRUCTION
 *     conf 3.)
 *   - Heng Yue Sect (恒岳派) disciples at (640, -480). Foundation realm,
 *     blue-green robes.
 *   - Luo He Sect (洛河门) disciples at (-720, -200). Foundation realm,
 *     white robes.
 *
 * Persistence UUID = canon UUID (so they rematerialize at the same spot
 * on reload). NO invented chapter citations.
 */
import * as THREE from 'three'
import { type WorldContext, type CultivatorRealm } from '../ai/Goal'
import { EntityCultivator } from './EntityCultivator'
import { CultivatorWanderGoal } from '../ai/CultivatorWanderGoal'

export interface CanonActorSpec {
  /** Persistence UUID (canon id, e.g. 'N01'). */
  id: string
  name: string
  nameCn?: string
  realm: CultivatorRealm
  position: THREE.Vector3
  faction: string
  hostility: number
  gender: 'male' | 'female'
  robeColorOverride?: number
  trimColorOverride?: number
  bladeColorOverride?: number
  /** Optional canon character id for graph linkage. */
  canonId?: string
}

/**
 * Hard-coded canon actor specs for the simulation start. These are the
 * minimum canon fidelity set: Wang Lin (N01) at the village, his first
 * antagonist Teng Li (N84), and generic disciples at Heng Yue and Luo He
 * sects. Additional canon NPCs may be added in future CRONs.
 */
export const CANON_ACTOR_SPECS: CanonActorSpec[] = [
  // Wang Lin — protagonist at the Zhao village (Foundation realm, jade robe).
  {
    id: 'N01',
    canonId: 'N01',
    name: 'Wang Lin',
    nameCn: '王林',
    realm: 'foundation',
    position: new THREE.Vector3(0, 0, 0),
    faction: 'wang_clan',
    hostility: 0,
    gender: 'male',
    robeColorOverride: 0x4a8c5c,  // jade green
    bladeColorOverride: 0x5fb88a, // jade
  },
  // Teng Li — first antagonist at the village (Qi Condensation, hostile).
  {
    id: 'N84',
    canonId: 'N84',
    name: 'Teng Li',
    nameCn: '藤立',
    realm: 'qi_condensation',
    position: new THREE.Vector3(22, 0, 10),
    faction: 'teng_clan',
    hostility: 85,
    gender: 'male',
    robeColorOverride: 0x2a1a2a,  // dark
    bladeColorOverride: 0x4a3030, // dull crimson
  },
  // Heng Yue Sect disciples (恒岳派) — Foundation, blue-green robes.
  ...generateDisciples({
    faction: 'heng_yue_sect',
    sectName: 'Heng Yue Disciple',
    sectNameCn: '恒岳弟子',
    centerX: 640,
    centerZ: -480,
    count: 4,
    realm: 'foundation',
    robeColor: 0x3a7a6a, // blue-green (canon permits blue for Heng Yue)
    spread: 28,
    bladeColor: 0x6aa0c0,
  }),
  // Luo He Sect disciples (洛河门) — Foundation, white robes.
  ...generateDisciples({
    faction: 'luo_he_sect',
    sectName: 'Luo He Disciple',
    sectNameCn: '洛河弟子',
    centerX: -720,
    centerZ: -200,
    count: 4,
    realm: 'foundation',
    robeColor: 0xf2ecd8, // ivory white
    spread: 26,
    bladeColor: 0xb0c8e0,
  }),
]

function generateDisciples(opts: {
  faction: string
  sectName: string
  sectNameCn: string
  centerX: number
  centerZ: number
  count: number
  realm: CultivatorRealm
  robeColor: number
  bladeColor: number
  spread: number
}): CanonActorSpec[] {
  const specs: CanonActorSpec[] = []
  for (let i = 0; i < opts.count; i++) {
    const angle = (i / opts.count) * Math.PI * 2 + Math.random() * 0.3
    const r = opts.spread * (0.5 + Math.random() * 0.5)
    specs.push({
      id: `mod:${opts.faction}_disciple_${i + 1}`,
      name: `${opts.sectName} ${i + 1}`,
      nameCn: `${opts.sectNameCn} ${i + 1}`,
      realm: opts.realm,
      position: new THREE.Vector3(
        opts.centerX + Math.cos(angle) * r,
        0,
        opts.centerZ + Math.sin(angle) * r,
      ),
      faction: opts.faction,
      hostility: 10,
      gender: i % 3 === 2 ? 'female' : 'male',
      robeColorOverride: opts.robeColor,
      bladeColorOverride: opts.bladeColor,
    })
  }
  return specs
}

/**
 * CanonActorMaterializer — spawns all CANON_ACTOR_SPECS as EntityCultivators
 * in the scene. Dematerializes on dispose.
 */
export class CanonActorMaterializer {
  readonly cultivators: Map<string, EntityCultivator> = new Map()
  private scene: THREE.Scene

  constructor(scene: THREE.Scene) {
    this.scene = scene
  }

  /** Spawn all canon NPCs. Returns the map of id → EntityCultivator. */
  materializeAll(ctx: WorldContext): Map<string, EntityCultivator> {
    for (const spec of CANON_ACTOR_SPECS) {
      if (this.cultivators.has(spec.id)) continue
      const c = new EntityCultivator({
        id: spec.id,
        name: spec.name,
        nameCn: spec.nameCn,
        realm: spec.realm,
        position: spec.position,
        faction: spec.faction,
        hostility: spec.hostility,
        canonId: spec.canonId,
        gender: spec.gender,
        homePosition: spec.position.clone(),
        robeColorOverride: spec.robeColorOverride,
        trimColorOverride: spec.trimColorOverride,
        bladeColorOverride: spec.bladeColorOverride,
      })
      c.materialize(this.scene)
      // Register the cultivator in the world context (for aggro + projectile hits).
      ctx.registerEntity(c)
      // Set the initial goal: wander around home.
      c.setGoalDirect(
        new CultivatorWanderGoal({ homePosition: spec.position.clone(), radius: 8 }),
        ctx,
      )
      this.cultivators.set(spec.id, c)
    }
    return this.cultivators
  }

  /** Remove all canon NPCs from the scene. */
  dematerializeAll(): void {
    for (const c of this.cultivators.values()) {
      c.dematerialize(this.scene)
    }
    this.cultivators.clear()
  }

  /** Get a canon NPC by id. */
  get(id: string): EntityCultivator | undefined {
    return this.cultivators.get(id)
  }
}
