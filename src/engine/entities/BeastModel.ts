/**
 * BeastModel.ts — procedural AAA spirit-beast (灵兽) models for the Er Gen
 * Verse. Four attested beast archetypes (mod-original, REASONABLE_RECONSTRUCTION
 * conf 3 — the 仙逆 canon attests spirit beasts of these elements and biomes
 * but not the exact models):
 *
 *   • Frost Wolf   (snow biome)     ~1.5 m tall quadruped, frost-mote aura
 *   • Flame Tiger  (volcanic)       ~1.8 m tall quadruped, ember mane + glow
 *   • Jade Serpent (forest)         ~6.0 m long slithering serpent, jade sheen
 *   • Thunder Hawk (mountains)      ~3.0 m wingspan bird, lightning crackle
 *
 * Each beast is built from Three.js primitives with a true skeletal hierarchy
 * and procedural animations (idle, walk, run, attack, death). Element-themed
 * particle VFX ride along. NO Minecraft sheep — these are real spirit beasts.
 */
import * as THREE from 'three'

export type BeastKind = 'frost_wolf' | 'flame_tiger' | 'jade_serpent' | 'thunder_hawk'
export type BeastBodyType = 'quadruped' | 'serpent' | 'bird'
export type BeastAnimName = 'idle' | 'walk' | 'run' | 'attack' | 'death'

interface BeastSpec {
  bodyType: BeastBodyType
  scale: number
  primary: number       // main fur/scale/feather color
  secondary: number     // accent (belly, mane, stripes)
  emissive: number
  emissiveStrength: number
  element: number       // VFX particle color
}

export const BEAST_SPECS: Record<BeastKind, BeastSpec> = {
  frost_wolf:   { bodyType: 'quadruped', scale: 1.0, primary: 0xc8d4e0, secondary: 0x6a7a90, emissive: 0x88c0e0, emissiveStrength: 0.30, element: 0xcfe6ff },
  flame_tiger:  { bodyType: 'quadruped', scale: 1.2, primary: 0xd04a1a, secondary: 0x1a0a04, emissive: 0xff6020, emissiveStrength: 0.50, element: 0xffa040 },
  jade_serpent: { bodyType: 'serpent',   scale: 1.0, primary: 0x4a8c5c, secondary: 0xe8c66a, emissive: 0x2a5a3a, emissiveStrength: 0.40, element: 0x9be15d },
  thunder_hawk: { bodyType: 'bird',      scale: 1.0, primary: 0x6a5a8a, secondary: 0xe8c66a, emissive: 0xb08ce0, emissiveStrength: 0.50, element: 0xc0a0ff },
}

interface AnimState {
  name: BeastAnimName
  time: number
  blendFrom: BeastAnimName | null
  blendProgress: number
  blendDuration: number
}

/**
 * BeastModel — owns the THREE.Group, skeleton, meshes, VFX, and animation
 * state for a single beast. Call update(dt) each frame.
 */
export class BeastModel {
  readonly group: THREE.Group
  readonly kind: BeastKind
  readonly spec: BeastSpec
  readonly bones: Map<string, THREE.Bone> = new Map()
  readonly skeleton: THREE.Skeleton
  readonly windUniforms = { time: { value: 0 } }

  private anim: AnimState = {
    name: 'idle', time: 0, blendFrom: null, blendProgress: 1, blendDuration: 0.3,
  }
  private vfx: BeastVFX
  private bodyType: BeastBodyType

  constructor(kind: BeastKind) {
    this.kind = kind
    this.spec = BEAST_SPECS[kind]
    this.bodyType = this.spec.bodyType
    this.group = new THREE.Group()
    this.group.name = `beast:${kind}`
    this.group.scale.setScalar(this.spec.scale)

    // Build skeleton by body type.
    let rootBone: THREE.Bone
    if (this.bodyType === 'quadruped') {
      const r = buildQuadrupedSkeleton()
      rootBone = r.rootBone
      for (const [n, b] of r.bones) this.bones.set(n, b)
      this.buildQuadrupedMeshes()
    } else if (this.bodyType === 'serpent') {
      const r = buildSerpentSkeleton()
      rootBone = r.rootBone
      for (const [n, b] of r.bones) this.bones.set(n, b)
      this.buildSerpentMeshes()
    } else {
      const r = buildBirdSkeleton()
      rootBone = r.rootBone
      for (const [n, b] of r.bones) this.bones.set(n, b)
      this.buildBirdMeshes()
    }
    this.group.add(rootBone)
    this.skeleton = new THREE.Skeleton(Array.from(this.bones.values()))
    this.skeleton.calculateInverses()

    this.vfx = createBeastVFX(kind, this.spec, this.bones)
    this.vfx.attach(this.group)

    this.group.traverse((o) => {
      const m = o as THREE.Mesh
      if (m.isMesh) { m.castShadow = true; m.receiveShadow = true }
    })
  }

  setAnimation(name: BeastAnimName, fade = 0.3): void {
    if (this.anim.name === name) return
    this.anim.blendFrom = this.anim.name
    this.anim.name = name
    this.anim.time = 0
    this.anim.blendProgress = 0
    this.anim.blendDuration = Math.max(0.05, fade)
  }

  getAnimation(): BeastAnimName { return this.anim.name }

  update(dt: number): void {
    this.windUniforms.time.value += dt
    this.anim.time += dt
    if (this.anim.blendProgress < 1) {
      this.anim.blendProgress = Math.min(1, this.anim.blendProgress + dt / this.anim.blendDuration)
    }
    this.applyPose()
    this.vfx.update(dt, this.anim.name)
  }

  private applyPose(): void {
    const poseA = computeBeastPose(this.bodyType, this.anim.name, this.anim.time)
    let pose: Map<string, THREE.Quaternion>
    if (this.anim.blendFrom && this.anim.blendProgress < 1) {
      const poseB = computeBeastPose(this.bodyType, this.anim.blendFrom, this.anim.time)
      pose = new Map()
      const t = this.anim.blendProgress
      for (const [name, qa] of poseA) {
        const qb = poseB.get(name) ?? new THREE.Quaternion()
        pose.set(name, qb.clone().slerp(qa, t))
      }
    } else {
      pose = poseA
    }
    for (const [name, q] of pose) {
      const bone = this.bones.get(name)
      if (bone) bone.quaternion.copy(q)
    }
  }

  // ───────────────────────── mesh builders ─────────────────────────

  private buildQuadrupedMeshes(): void {
    const s = this.spec
    const furMat = new THREE.MeshStandardMaterial({
      color: s.primary, roughness: 0.7, metalness: 0.05,
      emissive: s.emissive, emissiveIntensity: s.emissiveStrength * 0.4,
    })
    const bellyMat = new THREE.MeshStandardMaterial({
      color: s.secondary, roughness: 0.7, metalness: 0.05,
    })
    const eyeMat = new THREE.MeshStandardMaterial({
      color: 0x000000, roughness: 0.2, metalness: 0.3,
      emissive: s.element, emissiveIntensity: 0.6,
    })

    const spine = this.bones.get('spine')!
    const chest = this.bones.get('chest')!
    const neck = this.bones.get('neck')!
    const head = this.bones.get('head')!
    const tail = this.bones.get('tail')!

    // Body (ribcage).
    const torso = new THREE.Mesh(
      new THREE.CapsuleGeometry(0.22, 0.45, 8, 16),
      furMat,
    )
    torso.rotation.z = Math.PI / 2
    torso.position.set(0, 0, 0.10)
    spine.add(torso)
    // Lower belly.
    const belly = new THREE.Mesh(
      new THREE.CapsuleGeometry(0.16, 0.35, 6, 12),
      bellyMat,
    )
    belly.rotation.z = Math.PI / 2
    belly.position.set(0, -0.10, 0.05)
    spine.add(belly)
    // Chest.
    const chestMesh = new THREE.Mesh(
      new THREE.SphereGeometry(0.24, 16, 12),
      furMat,
    )
    chestMesh.scale.set(1, 0.9, 1.1)
    chest.add(chestMesh)
    // Neck.
    const neckMesh = new THREE.Mesh(
      new THREE.CylinderGeometry(0.10, 0.14, 0.30, 12),
      furMat,
    )
    neckMesh.rotation.x = -Math.PI / 2.6
    neckMesh.position.set(0, 0.05, 0.10)
    neck.add(neckMesh)
    // Head.
    const headMesh = new THREE.Mesh(
      new THREE.BoxGeometry(0.20, 0.18, 0.32),
      furMat,
    )
    headMesh.position.set(0, 0.04, 0.08)
    head.add(headMesh)
    // Snout.
    const snout = new THREE.Mesh(
      new THREE.BoxGeometry(0.12, 0.10, 0.18),
      bellyMat,
    )
    snout.position.set(0, -0.02, 0.25)
    head.add(snout)
    // Eyes.
    for (const side of [-1, 1]) {
      const eye = new THREE.Mesh(new THREE.SphereGeometry(0.025, 8, 6), eyeMat)
      eye.position.set(side * 0.07, 0.06, 0.12)
      head.add(eye)
    }
    // Ears.
    for (const side of [-1, 1]) {
      const ear = new THREE.Mesh(
        new THREE.ConeGeometry(0.05, 0.14, 4),
        furMat,
      )
      ear.position.set(side * 0.09, 0.16, -0.04)
      ear.rotation.z = side * -0.2
      head.add(ear)
    }
    // Fangs.
    for (const side of [-1, 1]) {
      const fang = new THREE.Mesh(
        new THREE.ConeGeometry(0.012, 0.05, 4),
        new THREE.MeshStandardMaterial({ color: 0xfff8e0, roughness: 0.3 }),
      )
      fang.position.set(side * 0.04, -0.06, 0.22)
      fang.rotation.x = Math.PI
      head.add(fang)
    }
    // Mane / stripes for tiger variant.
    if (this.kind === 'flame_tiger') {
      const maneMat = new THREE.MeshStandardMaterial({
        color: 0xff8030, roughness: 0.5, metalness: 0.1,
        emissive: 0xff4010, emissiveIntensity: 0.6,
      })
      // Stripes along the back.
      for (let i = 0; i < 5; i++) {
        const stripe = new THREE.Mesh(
          new THREE.BoxGeometry(0.45, 0.04, 0.06),
          maneMat,
        )
        stripe.position.set(0, 0.20, 0.05 - i * 0.10)
        spine.add(stripe)
      }
      // Mane around neck.
      const mane = new THREE.Mesh(
        new THREE.SphereGeometry(0.20, 12, 8),
        maneMat,
      )
      mane.position.set(0, 0.06, 0.05)
      mane.scale.set(1.1, 0.9, 1.3)
      neck.add(mane)
    }
    // Tail.
    const tailMat = furMat.clone()
    addBeastWindShader(tailMat, this.windUniforms.time, {
      amplitude: 0.06, frequency: 2.0, vRef: 0.0, vExtent: 0.50,
    })
    const tailMesh = new THREE.Mesh(
      new THREE.CylinderGeometry(0.04, 0.10, 0.50, 8, 1, true),
      tailMat,
    )
    tailMesh.rotation.x = Math.PI / 2.2
    tailMesh.position.set(0, 0, -0.25)
    tail.add(tailMesh)
    // Legs (4).
    for (const slot of ['FL', 'FR', 'BL', 'BR'] as const) {
      const upper = this.bones.get(`upperLeg${slot}`)!
      const lower = this.bones.get(`lowerLeg${slot}`)!
      const paw = this.bones.get(`paw${slot}`)!
      const upperMesh = new THREE.Mesh(
        new THREE.CapsuleGeometry(0.055, 0.20, 6, 8), furMat,
      )
      upperMesh.position.set(0, -0.12, 0)
      upper.add(upperMesh)
      const lowerMesh = new THREE.Mesh(
        new THREE.CapsuleGeometry(0.045, 0.18, 6, 8), bellyMat,
      )
      lowerMesh.position.set(0, -0.11, 0)
      lower.add(lowerMesh)
      const pawMesh = new THREE.Mesh(
        new THREE.BoxGeometry(0.085, 0.05, 0.12),
        new THREE.MeshStandardMaterial({ color: 0x1a1410, roughness: 0.6 }),
      )
      pawMesh.position.set(0, -0.02, 0.02)
      paw.add(pawMesh)
    }
  }

  private buildSerpentMeshes(): void {
    const s = this.spec
    const scaleMat = new THREE.MeshStandardMaterial({
      color: s.primary, roughness: 0.35, metalness: 0.4,
      emissive: s.emissive, emissiveIntensity: s.emissiveStrength,
    })
    addJadeSheenShader(scaleMat, s.element)
    const bellyMat = new THREE.MeshStandardMaterial({
      color: s.secondary, roughness: 0.6, metalness: 0.2,
    })
    const eyeMat = new THREE.MeshStandardMaterial({
      color: 0x000000, roughness: 0.2, metalness: 0.3,
      emissive: s.element, emissiveIntensity: 0.8,
    })
    // Body segments along the spine chain.
    const segNames = ['seg1', 'seg2', 'seg3', 'seg4', 'seg5', 'seg6']
    let r = 0.22
    for (let i = 0; i < segNames.length; i++) {
      const bone = this.bones.get(segNames[i])!
      const radius = r - i * 0.02
      const seg = new THREE.Mesh(
        new THREE.SphereGeometry(Math.max(0.05, radius), 16, 12),
        scaleMat,
      )
      seg.scale.set(1, 1, 1.4)
      bone.add(seg)
      // Belly stripe.
      const belly = new THREE.Mesh(
        new THREE.SphereGeometry(Math.max(0.04, radius * 0.7), 12, 8),
        bellyMat,
      )
      belly.scale.set(0.9, 0.5, 1.4)
      belly.position.set(0, -radius * 0.5, 0)
      bone.add(belly)
    }
    // Head (large, wedge-shaped).
    const head = this.bones.get('head')!
    const headMesh = new THREE.Mesh(
      new THREE.ConeGeometry(0.22, 0.50, 12),
      scaleMat,
    )
    headMesh.rotation.x = Math.PI / 2
    headMesh.position.set(0, 0, 0.20)
    head.add(headMesh)
    // Jaw.
    const jaw = new THREE.Mesh(
      new THREE.ConeGeometry(0.18, 0.36, 10),
      bellyMat,
    )
    jaw.rotation.x = -Math.PI / 2
    jaw.position.set(0, -0.05, 0.15)
    head.add(jaw)
    // Eyes.
    for (const side of [-1, 1]) {
      const eye = new THREE.Mesh(new THREE.SphereGeometry(0.045, 10, 8), eyeMat)
      eye.position.set(side * 0.13, 0.06, 0.10)
      head.add(eye)
    }
    // Fangs.
    for (const side of [-1, 1]) {
      const fang = new THREE.Mesh(
        new THREE.ConeGeometry(0.020, 0.10, 4),
        new THREE.MeshStandardMaterial({ color: 0xfff8e0, roughness: 0.3 }),
      )
      fang.position.set(side * 0.05, -0.08, 0.30)
      fang.rotation.x = Math.PI
      head.add(fang)
    }
    // Tongue (forked).
    const tongueMat = new THREE.MeshStandardMaterial({
      color: 0xc83060, roughness: 0.4, emissive: 0x801030, emissiveIntensity: 0.4,
    })
    const tongue = new THREE.Mesh(
      new THREE.BoxGeometry(0.04, 0.005, 0.30), tongueMat,
    )
    tongue.position.set(0, -0.04, 0.45)
    head.add(tongue)
  }

  private buildBirdMeshes(): void {
    const s = this.spec
    const featherMat = new THREE.MeshStandardMaterial({
      color: s.primary, roughness: 0.55, metalness: 0.15,
      emissive: s.emissive, emissiveIntensity: s.emissiveStrength * 0.5,
    })
    const bellyMat = new THREE.MeshStandardMaterial({
      color: s.secondary, roughness: 0.6, metalness: 0.1,
    })
    const eyeMat = new THREE.MeshStandardMaterial({
      color: 0x000000, roughness: 0.2, metalness: 0.3,
      emissive: s.element, emissiveIntensity: 0.8,
    })
    const beakMat = new THREE.MeshStandardMaterial({
      color: 0xe8c66a, roughness: 0.3, metalness: 0.5,
    })

    const spine = this.bones.get('spine')!
    const chest = this.bones.get('chest')!
    const neck = this.bones.get('neck')!
    const head = this.bones.get('head')!
    // Body (fuselage).
    const body = new THREE.Mesh(
      new THREE.CapsuleGeometry(0.18, 0.30, 8, 14),
      featherMat,
    )
    body.rotation.x = Math.PI / 2
    body.scale.set(1, 1, 1.4)
    spine.add(body)
    // Chest puff.
    const chestMesh = new THREE.Mesh(
      new THREE.SphereGeometry(0.18, 12, 10), bellyMat,
    )
    chestMesh.scale.set(1, 1.1, 1.2)
    chestMesh.position.set(0, -0.02, 0.05)
    chest.add(chestMesh)
    // Neck.
    const neckMesh = new THREE.Mesh(
      new THREE.CylinderGeometry(0.05, 0.08, 0.20, 8), featherMat,
    )
    neckMesh.rotation.x = -Math.PI / 2.4
    neckMesh.position.set(0, 0.04, 0.08)
    neck.add(neckMesh)
    // Head.
    const headMesh = new THREE.Mesh(
      new THREE.SphereGeometry(0.09, 12, 10), featherMat,
    )
    headMesh.position.set(0, 0.02, 0.10)
    head.add(headMesh)
    // Beak.
    const beak = new THREE.Mesh(
      new THREE.ConeGeometry(0.03, 0.16, 6), beakMat,
    )
    beak.rotation.x = Math.PI / 2
    beak.position.set(0, -0.01, 0.22)
    head.add(beak)
    // Eyes.
    for (const side of [-1, 1]) {
      const eye = new THREE.Mesh(new THREE.SphereGeometry(0.022, 8, 6), eyeMat)
      eye.position.set(side * 0.06, 0.04, 0.10)
      head.add(eye)
    }
    // Wings — three-segment feather fans.
    for (const side of ['L', 'R'] as const) {
      const sign = side === 'L' ? -1 : 1
      const upper = this.bones.get(`wing${side}`)!
      const mid = this.bones.get(`wingMid${side}`)!
      const tip = this.bones.get(`wingTip${side}`)!
      // Upper wing bone sleeve.
      const upperMesh = new THREE.Mesh(
        new THREE.CapsuleGeometry(0.04, 0.30, 6, 8), featherMat,
      )
      upperMesh.rotation.z = sign * Math.PI / 2
      upperMesh.position.set(sign * 0.15, 0, 0)
      upper.add(upperMesh)
      // Mid wing feathers (a tapered plane).
      const midFeathers = new THREE.Mesh(
        new THREE.PlaneGeometry(0.50, 0.40), featherMat.clone(),
      )
      midFeathers.material = featherMat.clone()
      ;(midFeathers.material as THREE.MeshStandardMaterial).side = THREE.DoubleSide
      midFeathers.position.set(sign * 0.25, 0, 0)
      midFeathers.rotation.y = sign * Math.PI / 2
      mid.add(midFeathers)
      // Tip feathers.
      const tipFeathers = new THREE.Mesh(
        new THREE.PlaneGeometry(0.55, 0.35), featherMat.clone(),
      )
      ;(tipFeathers.material as THREE.MeshStandardMaterial).side = THREE.DoubleSide
      tipFeathers.position.set(sign * 0.27, 0, 0)
      tipFeathers.rotation.y = sign * Math.PI / 2
      tip.add(tipFeathers)
    }
    // Tail feathers.
    const tail = this.bones.get('tail')!
    const tailFeathers = new THREE.Mesh(
      new THREE.PlaneGeometry(0.50, 0.55, 2, 4), featherMat.clone(),
    )
    ;(tailFeathers.material as THREE.MeshStandardMaterial).side = THREE.DoubleSide
    tailFeathers.position.set(0, 0, -0.30)
    tailFeathers.rotation.x = -Math.PI / 2.4
    tail.add(tailFeathers)
    // Talons (tucked under body).
    for (const side of ['L', 'R'] as const) {
      const sign = side === 'L' ? -1 : 1
      const leg = this.bones.get(`leg${side}`)!
      const thigh = new THREE.Mesh(
        new THREE.CylinderGeometry(0.025, 0.020, 0.20, 6), bellyMat,
      )
      thigh.position.set(sign * 0.07, -0.10, 0)
      leg.add(thigh)
      const talon = new THREE.Mesh(
        new THREE.ConeGeometry(0.03, 0.10, 4), beakMat,
      )
      talon.rotation.x = Math.PI
      talon.position.set(sign * 0.07, -0.22, 0)
      leg.add(talon)
    }
  }
}

// ───────────────────────── skeletons ─────────────────────────

function buildQuadrupedSkeleton(): { rootBone: THREE.Bone; bones: Map<string, THREE.Bone> } {
  const bones = new Map<string, THREE.Bone>()
  const make = (name: string, x: number, y: number, z: number): THREE.Bone => {
    const b = new THREE.Bone()
    b.name = name
    b.position.set(x, y, z)
    bones.set(name, b)
    return b
  }
  const root = make('root', 0, 0.75, 0)
  const spine = make('spine', 0, 0, 0)
  root.add(spine)
  const chest = make('chest', 0, 0, 0.30)
  spine.add(chest)
  const neck = make('neck', 0, 0.10, 0.20)
  chest.add(neck)
  const head = make('head', 0, 0.05, 0.20)
  neck.add(head)
  const tail = make('tail', 0, 0, -0.30)
  spine.add(tail)
  // Front legs.
  for (const [slot, sx] of [['FL', -1], ['FR', 1]] as const) {
    const upper = make(`upperLeg${slot}`, sx * 0.18, -0.05, 0.25)
    chest.add(upper)
    const lower = make(`lowerLeg${slot}`, 0, -0.25, 0)
    upper.add(lower)
    const paw = make(`paw${slot}`, 0, -0.25, 0.04)
    lower.add(paw)
  }
  // Back legs.
  for (const [slot, sx] of [['BL', -1], ['BR', 1]] as const) {
    const upper = make(`upperLeg${slot}`, sx * 0.18, -0.05, -0.15)
    spine.add(upper)
    const lower = make(`lowerLeg${slot}`, 0, -0.25, 0)
    upper.add(lower)
    const paw = make(`paw${slot}`, 0, -0.25, 0.04)
    lower.add(paw)
  }
  return { rootBone: root, bones }
}

function buildSerpentSkeleton(): { rootBone: THREE.Bone; bones: Map<string, THREE.Bone> } {
  const bones = new Map<string, THREE.Bone>()
  const make = (name: string, x: number, y: number, z: number): THREE.Bone => {
    const b = new THREE.Bone()
    b.name = name
    b.position.set(x, y, z)
    bones.set(name, b)
    return b
  }
  const root = make('root', 0, 0.20, 0)
  // 6 body segments, head at the front (+z), tail at the back (-z).
  const seg1 = make('seg1', 0, 0, 0.0)
  root.add(seg1)
  const seg2 = make('seg2', 0, 0, -0.50)
  seg1.add(seg2)
  const seg3 = make('seg3', 0, 0, -0.50)
  seg2.add(seg3)
  const seg4 = make('seg4', 0, 0, -0.45)
  seg3.add(seg4)
  const seg5 = make('seg5', 0, 0, -0.40)
  seg4.add(seg5)
  const seg6 = make('seg6', 0, 0, -0.35)
  seg5.add(seg6)
  const head = make('head', 0, 0, 0.45)
  seg1.add(head)
  return { rootBone: root, bones }
}

function buildBirdSkeleton(): { rootBone: THREE.Bone; bones: Map<string, THREE.Bone> } {
  const bones = new Map<string, THREE.Bone>()
  const make = (name: string, x: number, y: number, z: number): THREE.Bone => {
    const b = new THREE.Bone()
    b.name = name
    b.position.set(x, y, z)
    bones.set(name, b)
    return b
  }
  const root = make('root', 0, 1.5, 0)
  const spine = make('spine', 0, 0, 0)
  root.add(spine)
  const chest = make('chest', 0, 0, 0.20)
  spine.add(chest)
  const neck = make('neck', 0, 0.05, 0.10)
  chest.add(neck)
  const head = make('head', 0, 0.05, 0.15)
  neck.add(head)
  const tail = make('tail', 0, 0, -0.20)
  spine.add(tail)
  // Wings.
  for (const side of ['L', 'R'] as const) {
    const sign = side === 'L' ? -1 : 1
    const wing = make(`wing${side}`, sign * 0.15, 0, 0)
    chest.add(wing)
    const mid = make(`wingMid${side}`, sign * 0.30, 0, 0)
    wing.add(mid)
    const tip = make(`wingTip${side}`, sign * 0.30, 0, 0)
    mid.add(tip)
  }
  // Legs.
  for (const side of ['L', 'R'] as const) {
    const sign = side === 'L' ? -1 : 1
    const leg = make(`leg${side}`, sign * 0.10, -0.05, 0)
    spine.add(leg)
  }
  return { rootBone: root, bones }
}

// ───────────────────────── shaders ─────────────────────────

function addBeastWindShader(
  material: THREE.MeshStandardMaterial,
  timeUniform: { value: number },
  opts: { amplitude: number; frequency: number; vRef: number; vExtent: number },
): void {
  material.onBeforeCompile = (shader) => {
    shader.uniforms.uTime = timeUniform
    shader.uniforms.uAmp = { value: opts.amplitude }
    shader.uniforms.uFreq = { value: opts.frequency }
    shader.uniforms.uVRef = { value: opts.vRef }
    shader.uniforms.uVExtent = { value: opts.vExtent }
    shader.vertexShader = shader.vertexShader
      .replace('#include <common>', `#include <common>
        uniform float uTime; uniform float uAmp; uniform float uFreq;
        uniform float uVRef; uniform float uVExtent;`)
      .replace('#include <begin_vertex>', `#include <begin_vertex>
        float yn = clamp((position.y - uVRef) / max(uVExtent, 0.001), 0.0, 1.0);
        float sf = 1.0 - yn;
        float ph = uTime * uFreq + position.x * 0.5;
        transformed.x += sin(ph) * uAmp * sf;
        transformed.z += cos(ph * 0.8) * uAmp * sf * 0.6;`)
  }
  material.needsUpdate = true
}

/** Jade-sheen fresnel-like rim glow (for serpent scales). */
function addJadeSheenShader(material: THREE.MeshStandardMaterial, glowColor: number): void {
  material.onBeforeCompile = (shader) => {
    shader.uniforms.uGlow = { value: new THREE.Color(glowColor) }
    shader.fragmentShader = shader.fragmentShader
      .replace('#include <common>', `#include <common>
        uniform vec3 uGlow;`)
      .replace(
        '#include <dithering_fragment>',
        `#include <dithering_fragment>
         float fres = pow(1.0 - dot(normalize(vNormal), normalize(vViewPosition)), 2.5);
         gl_FragColor.rgb += uGlow * fres * 0.7;`,
      )
  }
  material.needsUpdate = true
}

// ───────────────────────── beast VFX ─────────────────────────

interface BeastVFX {
  attach(group: THREE.Group): void
  update(dt: number, anim: BeastAnimName): void
}

function createBeastVFX(kind: BeastKind, spec: BeastSpec, _bones: Map<string, THREE.Bone>): BeastVFX {
  switch (kind) {
    case 'frost_wolf':   return new FrostMoteVFX(spec.element)
    case 'flame_tiger':  return new EmberManeVFX(spec.element)
    case 'jade_serpent': return new JadeShimmerVFX(spec.element)
    case 'thunder_hawk': return new LightningCrackleVFX(spec.element)
  }
}

/** Frost motes — slow-orbiting crystalline particles. */
class FrostMoteVFX implements BeastVFX {
  private points: THREE.Points
  private velocities: Float32Array
  constructor(color: number) {
    const N = 60
    const pos = new Float32Array(N * 3)
    this.velocities = new Float32Array(N * 3)
    for (let i = 0; i < N; i++) {
      const r = 0.6 + Math.random() * 0.6
      const a = Math.random() * Math.PI * 2
      const y = Math.random() * 1.2
      pos[i * 3] = Math.cos(a) * r
      pos[i * 3 + 1] = y
      pos[i * 3 + 2] = Math.sin(a) * r
      this.velocities[i * 3] = (Math.random() - 0.5) * 0.1
      this.velocities[i * 3 + 1] = (Math.random() - 0.5) * 0.05 + 0.02
      this.velocities[i * 3 + 2] = (Math.random() - 0.5) * 0.1
    }
    const geo = new THREE.BufferGeometry()
    geo.setAttribute('position', new THREE.BufferAttribute(pos, 3))
    const mat = new THREE.PointsMaterial({
      color, size: 0.08, transparent: true, opacity: 0.7,
      blending: THREE.AdditiveBlending, depthWrite: false,
    })
    this.points = new THREE.Points(geo, mat)
    this.points.position.y = 0.5
  }
  attach(g: THREE.Group) { g.add(this.points) }
  update(dt: number) {
    const attr = this.points.geometry.getAttribute('position') as THREE.BufferAttribute
    const arr = attr.array as Float32Array
    for (let i = 0; i < arr.length / 3; i++) {
      arr[i * 3] += this.velocities[i * 3] * dt
      arr[i * 3 + 1] += this.velocities[i * 3 + 1] * dt
      arr[i * 3 + 2] += this.velocities[i * 3 + 2] * dt
      if (arr[i * 3 + 1] > 1.6) {
        arr[i * 3 + 1] = 0.0
        arr[i * 3] = (Math.random() - 0.5) * 1.2
        arr[i * 3 + 2] = (Math.random() - 0.5) * 1.2
      }
    }
    attr.needsUpdate = true
    this.points.rotation.y += dt * 0.3
  }
}

/** Ember mane — rising orange embers from the spine. */
class EmberManeVFX implements BeastVFX {
  private points: THREE.Points
  private velocities: Float32Array
  constructor(color: number) {
    const N = 80
    const pos = new Float32Array(N * 3)
    this.velocities = new Float32Array(N * 3)
    for (let i = 0; i < N; i++) {
      pos[i * 3] = (Math.random() - 0.5) * 0.4
      pos[i * 3 + 1] = Math.random() * 1.2
      pos[i * 3 + 2] = (Math.random() - 0.5) * 0.8
      this.velocities[i * 3 + 1] = 0.3 + Math.random() * 0.4
    }
    const geo = new THREE.BufferGeometry()
    geo.setAttribute('position', new THREE.BufferAttribute(pos, 3))
    const mat = new THREE.PointsMaterial({
      color, size: 0.10, transparent: true, opacity: 0.85,
      blending: THREE.AdditiveBlending, depthWrite: false,
    })
    this.points = new THREE.Points(geo, mat)
    this.points.position.y = 0.0
  }
  attach(g: THREE.Group) { g.add(this.points) }
  update(dt: number) {
    const attr = this.points.geometry.getAttribute('position') as THREE.BufferAttribute
    const arr = attr.array as Float32Array
    for (let i = 0; i < arr.length / 3; i++) {
      arr[i * 3 + 1] += this.velocities[i * 3 + 1] * dt
      if (arr[i * 3 + 1] > 1.6) {
        arr[i * 3 + 1] = 0.0
        arr[i * 3] = (Math.random() - 0.5) * 0.4
        arr[i * 3 + 2] = (Math.random() - 0.5) * 0.8
      }
    }
    attr.needsUpdate = true
  }
}

/** Jade shimmer — sparkles along the serpent body. */
class JadeShimmerVFX implements BeastVFX {
  private points: THREE.Points
  constructor(color: number) {
    const N = 50
    const pos = new Float32Array(N * 3)
    for (let i = 0; i < N; i++) {
      const z = (Math.random() - 0.5) * 3.0
      const angle = Math.random() * Math.PI * 2
      const r = 0.22
      pos[i * 3] = Math.cos(angle) * r
      pos[i * 3 + 1] = Math.sin(angle) * r * 0.6
      pos[i * 3 + 2] = z
    }
    const geo = new THREE.BufferGeometry()
    geo.setAttribute('position', new THREE.BufferAttribute(pos, 3))
    const mat = new THREE.PointsMaterial({
      color, size: 0.06, transparent: true, opacity: 0.6,
      blending: THREE.AdditiveBlending, depthWrite: false,
    })
    this.points = new THREE.Points(geo, mat)
    this.points.position.y = 0.20
  }
  attach(g: THREE.Group) { g.add(this.points) }
  update(dt: number, _anim: BeastAnimName) {
    this.points.rotation.z += dt * 0.6
    const mat = this.points.material as THREE.PointsMaterial
    mat.opacity = 0.45 + Math.sin(performance.now() * 0.003) * 0.2
  }
}

/** Lightning crackle — branching line segments around the wings. */
class LightningCrackleVFX implements BeastVFX {
  private lines: THREE.LineSegments
  private nextCrackle = 0
  private visible = false
  constructor(color: number) {
    // Pre-build a 30-segment crackle pattern in a sphere around the bird.
    const N = 30
    const positions = new Float32Array(N * 6)  // 30 segments * 2 endpoints * 3 coords
    for (let i = 0; i < N; i++) {
      const r = 0.8 + Math.random() * 0.6
      const a1 = Math.random() * Math.PI * 2
      const a2 = a1 + (Math.random() - 0.5) * 0.8
      const y1 = (Math.random() - 0.5) * 0.6
      const y2 = y1 + (Math.random() - 0.5) * 0.3
      positions[i * 6 + 0] = Math.cos(a1) * r
      positions[i * 6 + 1] = y1
      positions[i * 6 + 2] = Math.sin(a1) * r
      positions[i * 6 + 3] = Math.cos(a2) * r * 0.9
      positions[i * 6 + 4] = y2
      positions[i * 6 + 5] = Math.sin(a2) * r * 0.9
    }
    const geo = new THREE.BufferGeometry()
    geo.setAttribute('position', new THREE.BufferAttribute(positions, 3))
    const mat = new THREE.LineBasicMaterial({
      color, transparent: true, opacity: 0.9,
      blending: THREE.AdditiveBlending, depthWrite: false,
    })
    this.lines = new THREE.LineSegments(geo, mat)
    this.lines.visible = false
    this.lines.position.y = 0.0
  }
  attach(g: THREE.Group) { g.add(this.lines) }
  update(dt: number, anim: BeastAnimName) {
    this.nextCrackle -= dt
    if (this.nextCrackle <= 0) {
      this.visible = !this.visible
      this.lines.visible = this.visible
      this.nextCrackle = this.visible ? 0.06 + Math.random() * 0.10 : 0.5 + Math.random() * 1.2
      // Re-randomize positions each time it flashes.
      if (this.visible) {
        const attr = this.lines.geometry.getAttribute('position') as THREE.BufferAttribute
        const arr = attr.array as Float32Array
        for (let i = 0; i < arr.length / 6; i++) {
          const r = 0.8 + Math.random() * 0.6
          const a1 = Math.random() * Math.PI * 2
          const a2 = a1 + (Math.random() - 0.5) * 0.8
          const y1 = (Math.random() - 0.5) * 0.6
          arr[i * 6 + 0] = Math.cos(a1) * r
          arr[i * 6 + 1] = y1
          arr[i * 6 + 2] = Math.sin(a1) * r
          arr[i * 6 + 3] = Math.cos(a2) * r * 0.9
          arr[i * 6 + 4] = y1 + (Math.random() - 0.5) * 0.3
          arr[i * 6 + 5] = Math.sin(a2) * r * 0.9
        }
        attr.needsUpdate = true
      }
    }
    if (anim === 'run' || anim === 'attack') {
      this.nextCrackle = Math.min(this.nextCrackle, 0.05)
    }
  }
}

// ───────────────────────── animations ─────────────────────────

function qE(x: number, y: number, z: number): THREE.Quaternion {
  return new THREE.Quaternion().setFromEuler(new THREE.Euler(x, y, z, 'XYZ'))
}

function computeBeastPose(
  bodyType: BeastBodyType,
  name: BeastAnimName,
  time: number,
): Map<string, THREE.Quaternion> {
  const p = new Map<string, THREE.Quaternion>()
  switch (bodyType) {
    case 'quadruped':
      if (name === 'idle')       quadIdle(p, time)
      else if (name === 'walk')  quadWalk(p, time, 1.0)
      else if (name === 'run')   quadWalk(p, time, 1.7)
      else if (name === 'attack')quadAttack(p, time)
      else                       quadDeath(p, time)
      break
    case 'serpent':
      if (name === 'idle')       serpIdle(p, time)
      else if (name === 'walk')  serpSlither(p, time, 1.0)
      else if (name === 'run')   serpSlither(p, time, 1.7)
      else if (name === 'attack')serpStrike(p, time)
      else                       serpDeath(p, time)
      break
    case 'bird':
      if (name === 'idle')       birdIdle(p, time)
      else if (name === 'walk')  birdGlide(p, time, 0.6)
      else if (name === 'run')   birdGlide(p, time, 1.4)
      else if (name === 'attack')birdStrike(p, time)
      else                       birdDeath(p, time)
      break
  }
  return p
}

function quadIdle(p: Map<string, THREE.Quaternion>, t: number): void {
  const breath = Math.sin(t * 1.2) * 0.02
  p.set('spine', qE(breath, 0, 0))
  p.set('chest', qE(breath * 0.5, 0, 0))
  p.set('neck', qE(-0.10 + breath * 0.5, 0, 0.02 * Math.sin(t * 0.4)))
  p.set('head', qE(-0.05, 0.02 * Math.sin(t * 0.3), 0))
  p.set('tail', qE(0, 0.10 * Math.sin(t * 1.0), 0))
  p.set('upperLegFL', qE(0.05, 0, 0.04))
  p.set('upperLegFR', qE(0.05, 0, -0.04))
  p.set('upperLegBL', qE(-0.05, 0, 0.04))
  p.set('upperLegBR', qE(-0.05, 0, -0.04))
}

function quadWalk(p: Map<string, THREE.Quaternion>, t: number, scale: number): void {
  const ph = t * 5.0 * scale
  const sw = 0.45 * scale
  // Diagonal gait: FL+BR forward together, FR+BL back together.
  p.set('upperLegFL', qE(Math.sin(ph) * sw, 0, 0.04))
  p.set('upperLegBR', qE(Math.sin(ph) * sw, 0, -0.04))
  p.set('upperLegFR', qE(-Math.sin(ph) * sw, 0, -0.04))
  p.set('upperLegBL', qE(-Math.sin(ph) * sw, 0, 0.04))
  p.set('lowerLegFL', qE(Math.max(0, -Math.sin(ph)) * 0.7 * scale, 0, 0))
  p.set('lowerLegFR', qE(Math.max(0, Math.sin(ph)) * 0.7 * scale, 0, 0))
  p.set('lowerLegBL', qE(Math.max(0, Math.sin(ph)) * 0.7 * scale, 0, 0))
  p.set('lowerLegBR', qE(Math.max(0, -Math.sin(ph)) * 0.7 * scale, 0, 0))
  p.set('spine', qE(0.04 + Math.abs(Math.sin(ph)) * 0.02, Math.sin(ph) * 0.03, 0))
  p.set('neck', qE(-0.05, Math.sin(ph) * 0.03, 0))
  p.set('head', qE(-0.04, 0, 0))
  p.set('tail', qE(0, Math.sin(ph) * 0.15, 0))
}

function quadAttack(p: Map<string, THREE.Quaternion>, t: number): void {
  // Lunge forward + bite: front legs extend, head drives forward.
  const cycle = (t % 1.2) / 1.2
  const lunge = Math.sin(cycle * Math.PI)
  p.set('spine', qE(0.15 - lunge * 0.20, 0, 0))
  p.set('chest', qE(-0.10 - lunge * 0.20, 0, 0))
  p.set('neck', qE(-0.60 - lunge * 0.30, 0, 0))
  p.set('head', qE(-0.50, 0, 0))
  p.set('upperLegFL', qE(-0.40 + lunge * 0.30, 0, 0.04))
  p.set('upperLegFR', qE(-0.40 + lunge * 0.30, 0, -0.04))
  p.set('upperLegBL', qE(0.30 - lunge * 0.40, 0, 0.04))
  p.set('upperLegBR', qE(0.30 - lunge * 0.40, 0, -0.04))
  p.set('tail', qE(0, 0.30 * Math.sin(t * 8.0), 0))
}

function quadDeath(p: Map<string, THREE.Quaternion>, _t: number): void {
  p.set('root', qE(-Math.PI / 2, 0, 0.10))
  p.set('spine', qE(0.20, 0, 0.10))
  p.set('neck', qE(-0.60, 0.30, 0))
  p.set('head', qE(-0.40, 0, 0))
  p.set('upperLegFL', qE(0.80, 0, 0.30))
  p.set('upperLegFR', qE(0.80, 0, -0.30))
  p.set('upperLegBL', qE(-0.40, 0, 0.30))
  p.set('upperLegBR', qE(-0.40, 0, -0.30))
}

function serpIdle(p: Map<string, THREE.Quaternion>, t: number): void {
  // Coiled; subtle breathing wave along the spine.
  for (let i = 1; i <= 6; i++) {
    p.set(`seg${i}`, qE(0, Math.sin(t * 0.6 + i * 0.4) * 0.04, 0))
  }
  p.set('head', qE(0, 0.02 * Math.sin(t * 0.4), 0))
}

function serpSlither(p: Map<string, THREE.Quaternion>, t: number, scale: number): void {
  // Travelling sine wave along the body.
  const ph = t * 3.5 * scale
  for (let i = 1; i <= 6; i++) {
    const amp = 0.20 + i * 0.04
    p.set(`seg${i}`, qE(0, Math.sin(ph - i * 0.6) * amp * scale, 0))
  }
  p.set('head', qE(-0.05, Math.sin(ph) * 0.10, 0))
}

function serpStrike(p: Map<string, THREE.Quaternion>, t: number): void {
  const cycle = (t % 1.0) / 1.0
  const strike = Math.sin(cycle * Math.PI)
  // Coil back then lunge.
  for (let i = 1; i <= 6; i++) {
    p.set(`seg${i}`, qE(0, -0.20 * (1 - strike) + 0.05, 0))
  }
  p.set('head', qE(-0.40 - strike * 0.50, 0, 0))
}

function serpDeath(p: Map<string, THREE.Quaternion>, _t: number): void {
  for (let i = 1; i <= 6; i++) {
    p.set(`seg${i}`, qE(0, (i % 2 === 0 ? 0.5 : -0.5), 0))
  }
  p.set('head', qE(-0.60, 0, 0))
}

function birdIdle(p: Map<string, THREE.Quaternion>, t: number): void {
  // Hover with slow wing flap.
  const flap = Math.sin(t * 2.0) * 0.30
  p.set('wingL', qE(0, 0, flap + 0.20))
  p.set('wingR', qE(0, 0, -flap - 0.20))
  p.set('wingMidL', qE(0, 0, 0.10))
  p.set('wingMidR', qE(0, 0, -0.10))
  p.set('wingTipL', qE(0, 0, 0.20))
  p.set('wingTipR', qE(0, 0, -0.20))
  p.set('spine', qE(0.05, 0, 0.02 * Math.sin(t * 0.6)))
  p.set('neck', qE(0, 0.05 * Math.sin(t * 0.4), 0))
  p.set('head', qE(0, 0, 0))
  p.set('tail', qE(0.10, 0.05 * Math.sin(t * 1.2), 0))
}

function birdGlide(p: Map<string, THREE.Quaternion>, t: number, scale: number): void {
  // Faster flap when running; extended when gliding.
  const flap = Math.sin(t * 5.0 * scale) * 0.45
  p.set('wingL', qE(0, 0, flap + 0.10))
  p.set('wingR', qE(0, 0, -flap - 0.10))
  p.set('wingMidL', qE(0, 0, 0.05))
  p.set('wingMidR', qE(0, 0, -0.05))
  p.set('wingTipL', qE(0, 0, 0.30 + flap * 0.4))
  p.set('wingTipR', qE(0, 0, -0.30 - flap * 0.4))
  p.set('spine', qE(0.10, Math.sin(t * 3.0 * scale) * 0.04, 0))
  p.set('neck', qE(-0.10, 0, 0))
  p.set('tail', qE(0.20, Math.sin(t * 3.0 * scale) * 0.10, 0))
}

function birdStrike(p: Map<string, THREE.Quaternion>, t: number): void {
  const cycle = (t % 1.0) / 1.0
  const stoop = Math.sin(cycle * Math.PI)
  // Wings fold back, talons extend.
  p.set('wingL', qE(0, 0, 0.60 - stoop * 0.30))
  p.set('wingR', qE(0, 0, -0.60 + stoop * 0.30))
  p.set('wingMidL', qE(0, 0, 0.40))
  p.set('wingMidR', qE(0, 0, -0.40))
  p.set('wingTipL', qE(0, 0, 0.40))
  p.set('wingTipR', qE(0, 0, -0.40))
  p.set('spine', qE(0.30 + stoop * 0.20, 0, 0))
  p.set('neck', qE(-0.40, 0, 0))
  p.set('head', qE(-0.30, 0, 0))
  p.set('tail', qE(0.40, 0, 0))
}

function birdDeath(p: Map<string, THREE.Quaternion>, _t: number): void {
  // Wings fold in, body tumbles.
  p.set('root', qE(Math.PI / 2, 0, 0.30))
  p.set('wingL', qE(0, 0, 0.80))
  p.set('wingR', qE(0, 0, -0.80))
  p.set('wingMidL', qE(0, 0, 0.60))
  p.set('wingMidR', qE(0, 0, -0.60))
  p.set('wingTipL', qE(0, 0, 0.40))
  p.set('wingTipR', qE(0, 0, -0.40))
  p.set('neck', qE(-0.50, 0, 0))
  p.set('head', qE(-0.30, 0, 0))
}

// ───────────────────────── factory ─────────────────────────

export function createBeastModel(kind: BeastKind): BeastModel {
  return new BeastModel(kind)
}
