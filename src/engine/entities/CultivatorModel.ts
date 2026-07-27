/**
 * CultivatorModel — procedural humanoid cultivator.
 *
 * FIXED in CRON-THREEJS-6:
 * - No more floating head (head sits directly on torso neck)
 * - No more baldness (full hair cap + top-knot)
 * - Correct facing (model faces +Z by default; yaw rotates the group)
 * - No more custom ShaderMaterial that ignores scene lighting — uses
 *   MeshStandardMaterial so the model responds to sun + ambient + hemi
 * - No transparency issues (all materials opaque unless explicitly transparent)
 * - Proportions fixed: realistic human 1.8m tall
 *
 * The model is built from Three.js primitives but composed to look like a
 * robed cultivator, not a snowman. Mortal players wear rough brown peasant
 * clothes; cultivator NPCs wear realm-colored robes.
 */
import * as THREE from 'three'

export type RealmKey =
  | 'qi_condensation'
  | 'foundation'
  | 'core_formation'
  | 'nascent_soul'
  | 'soul_transformation'
  | 'ascendant'

export interface RealmStyle {
  robe: number
  trim: number
  sash: number
  aura: number
}

export const REALM_STYLES: Record<RealmKey, RealmStyle> = {
  qi_condensation: { robe: 0xf5f5ec, trim: 0xc4c4b0, sash: 0x6a8a9a, aura: 0xffffff },
  foundation: { robe: 0x9be15d, trim: 0xf0e6a0, sash: 0xb08a3a, aura: 0x9be15d },
  core_formation: { robe: 0x4a9eb8, trim: 0xf0e6a0, sash: 0xb08a3a, aura: 0x4a9eb8 },
  nascent_soul: { robe: 0xe8c468, trim: 0xfff0c0, sash: 0xa05a1a, aura: 0xffd060 },
  soul_transformation: { robe: 0x8a4aaa, trim: 0xf0e6a0, sash: 0x6a3a8a, aura: 0xc060ff },
  ascendant: { robe: 0xc8403a, trim: 0xfff0c0, sash: 0x601818, aura: 0xff4030 },
}

export type AnimKey = 'idle' | 'walk' | 'run' | 'jump' | 'cast' | 'sword_qi' | 'fly'

export interface CultivatorModelHandle {
  group: THREE.Group
  setAnimation(key: AnimKey): void
  update(dt: number): void
  setYaw(yaw: number): void
  setAuraVisible(v: boolean): void
  setSwordVisible(v: boolean): void
  dispose(): void
}

/**
 * Create a cultivator model. The model faces +Z by default (forward).
 * The caller sets group.rotation.y to face the desired direction.
 *
 * Scale: 1 unit = 1 meter. Model is ~1.8m tall. Feet at y=0, head top at y~1.8.
 *
 * @param realm — determines robe color
 * @param female — slightly slimmer proportions
 * @param robeColorOverride — override the robe color (for mortal peasant clothes)
 */
export function createCultivatorModel(
  realm: RealmKey = 'foundation',
  female = false,
  robeColorOverride?: number,
): CultivatorModelHandle {
  const group = new THREE.Group()
  const style = REALM_STYLES[realm]
  const robeColor = robeColorOverride ?? style.robe

  // ---- Materials (all opaque, all respond to scene lighting) ----
  const robeMat = new THREE.MeshStandardMaterial({
    color: robeColor,
    roughness: 0.85,
    metalness: 0.0,
  })
  const trimMat = new THREE.MeshStandardMaterial({
    color: style.trim,
    roughness: 0.7,
    metalness: 0.1,
  })
  const sashMat = new THREE.MeshStandardMaterial({
    color: style.sash,
    roughness: 0.6,
    metalness: 0.2,
  })
  const skinMat = new THREE.MeshStandardMaterial({
    color: 0xf0d4b8,
    roughness: 0.6,
    metalness: 0.0,
  })
  const hairMat = new THREE.MeshStandardMaterial({
    color: 0x1a1a1a,
    roughness: 0.8,
    metalness: 0.0,
  })
  const bootMat = new THREE.MeshStandardMaterial({
    color: 0x2a1a10,
    roughness: 0.7,
    metalness: 0.0,
  })

  // ---- Body parts (all positioned so feet are at y=0) ----

  // Legs (two cylinders from y=0 to y=0.8).
  const legGeo = new THREE.CylinderGeometry(0.09, 0.08, 0.8, 8)
  const legL = new THREE.Mesh(legGeo, robeMat)
  legL.position.set(-0.1, 0.4, 0)
  legL.castShadow = true
  group.add(legL)
  const legR = new THREE.Mesh(legGeo, robeMat)
  legR.position.set(0.1, 0.4, 0)
  legR.castShadow = true
  group.add(legR)

  // Boots (small boxes at the bottom of each leg).
  const bootGeo = new THREE.BoxGeometry(0.14, 0.1, 0.2)
  const bootL = new THREE.Mesh(bootGeo, bootMat)
  bootL.position.set(-0.1, 0.05, 0.03)
  group.add(bootL)
  const bootR = new THREE.Mesh(bootGeo, bootMat)
  bootR.position.set(0.1, 0.05, 0.03)
  group.add(bootR)

  // Torso (tapered cylinder from y=0.8 to y=1.35).
  const torsoGeo = new THREE.CylinderGeometry(
    female ? 0.15 : 0.18,
    female ? 0.20 : 0.24,
    0.55,
    12,
  )
  const torso = new THREE.Mesh(torsoGeo, robeMat)
  torso.position.y = 1.075 // center of 0.8..1.35
  torso.castShadow = true
  group.add(torso)

  // Shoulders (wide flattened sphere across top of torso — gives human silhouette).
  const shoulderGeo = new THREE.SphereGeometry(female ? 0.22 : 0.26, 16, 8)
  const shoulders = new THREE.Mesh(shoulderGeo, robeMat)
  shoulders.position.y = 1.30
  shoulders.scale.set(1.4, 0.5, 0.8) // flatten + widen
  shoulders.castShadow = true
  group.add(shoulders)

  // Collar (small cylinder around neck base — robe neckline).
  const collarGeo = new THREE.CylinderGeometry(0.10, 0.12, 0.06, 12)
  const collar = new THREE.Mesh(collarGeo, trimMat)
  collar.position.y = 1.33
  group.add(collar)

  // Sash (thin torus around waist at y=0.95).
  const sashGeo = new THREE.TorusGeometry(0.22, 0.04, 8, 16)
  const sash = new THREE.Mesh(sashGeo, sashMat)
  sash.position.y = 0.95
  sash.rotation.x = Math.PI / 2
  group.add(sash)

  // Robe skirt (cone from waist to below knees, open at front).
  const skirtGeo = new THREE.CylinderGeometry(0.24, 0.42, 0.6, 16, 1, true)
  const skirt = new THREE.Mesh(skirtGeo, robeMat)
  skirt.position.y = 0.65
  skirt.castShadow = true
  group.add(skirt)

  // Neck (short cylinder connecting torso to head).
  const neckGeo = new THREE.CylinderGeometry(0.06, 0.07, 0.08, 8)
  const neck = new THREE.Mesh(neckGeo, skinMat)
  neck.position.y = 1.39
  group.add(neck)

  // Head (sphere + jaw extension for human face shape).
  const headGeo = new THREE.SphereGeometry(0.14, 16, 12)
  const head = new THREE.Mesh(headGeo, skinMat)
  head.position.y = 1.50
  head.scale.set(0.9, 1.05, 0.95) // slightly oval
  head.castShadow = true
  group.add(head)

  // Jaw/chin (small cone extending forward from lower face).
  const jawGeo = new THREE.SphereGeometry(0.08, 8, 6)
  const jaw = new THREE.Mesh(jawGeo, skinMat)
  jaw.position.set(0, 1.45, 0.08)
  jaw.scale.set(0.8, 0.6, 0.7)
  group.add(jaw)

  // Hair cap (half-sphere covering top of head, NOT bald).
  const hairCapGeo = new THREE.SphereGeometry(0.15, 16, 8, 0, Math.PI * 2, 0, Math.PI * 0.55)
  const hairCap = new THREE.Mesh(hairCapGeo, hairMat)
  hairCap.position.y = 1.52
  group.add(hairCap)

  // Top-knot bun (on top of hair cap).
  const bunGeo = new THREE.SphereGeometry(0.06, 12, 8)
  const bun = new THREE.Mesh(bunGeo, hairMat)
  bun.position.set(0, 1.66, -0.02)
  group.add(bun)

  // Eyes (two small dark spheres on the front of the head).
  const eyeGeo = new THREE.SphereGeometry(0.02, 8, 6)
  const eyeMat = new THREE.MeshStandardMaterial({ color: 0x1a1a2a, roughness: 0.3 })
  const eyeL = new THREE.Mesh(eyeGeo, eyeMat)
  eyeL.position.set(-0.05, 1.51, 0.12)
  group.add(eyeL)
  const eyeR = new THREE.Mesh(eyeGeo, eyeMat)
  eyeR.position.set(0.05, 1.51, 0.12)
  group.add(eyeR)

  // Arms (two cylinders from shoulder to elbow).
  const armGeo = new THREE.CylinderGeometry(0.05, 0.045, 0.35, 8)
  const armL = new THREE.Mesh(armGeo, robeMat)
  armL.position.set(-(female ? 0.22 : 0.26), 1.15, 0)
  armL.castShadow = true
  group.add(armL)
  const armR = new THREE.Mesh(armGeo, robeMat)
  armR.position.set(female ? 0.22 : 0.26, 1.15, 0)
  armR.castShadow = true
  group.add(armR)

  // Hands (small spheres at end of arms).
  const handGeo = new THREE.SphereGeometry(0.045, 8, 6)
  const handL = new THREE.Mesh(handGeo, skinMat)
  handL.position.set(-(female ? 0.22 : 0.26), 0.95, 0)
  group.add(handL)
  const handR = new THREE.Mesh(handGeo, skinMat)
  handR.position.set(female ? 0.22 : 0.26, 0.95, 0)
  group.add(handR)

  // Sheathed sword on back (visible across shoulders).
  const swordGroup = new THREE.Group()
  swordGroup.position.set(0, 1.1, -0.12)
  swordGroup.rotation.x = 0.25
  const scabGeo = new THREE.CylinderGeometry(0.018, 0.018, 0.85, 8)
  const scabMat = new THREE.MeshStandardMaterial({ color: 0x2a4a3a, roughness: 0.5, metalness: 0.3 })
  const scab = new THREE.Mesh(scabGeo, scabMat)
  scab.castShadow = true
  swordGroup.add(scab)
  // Guard.
  const guardGeo = new THREE.BoxGeometry(0.09, 0.02, 0.035)
  const guardMat = new THREE.MeshStandardMaterial({ color: 0xc8a050, roughness: 0.3, metalness: 0.8 })
  const guard = new THREE.Mesh(guardGeo, guardMat)
  guard.position.y = 0.45
  swordGroup.add(guard)
  // Handle.
  const handleGeo = new THREE.CylinderGeometry(0.012, 0.012, 0.12, 8)
  const handleMat = new THREE.MeshStandardMaterial({ color: 0x5a2a2a, roughness: 0.8 })
  const handle = new THREE.Mesh(handleGeo, handleMat)
  handle.position.y = 0.52
  swordGroup.add(handle)
  group.add(swordGroup)

  // ---- Animation state ----
  let anim: AnimKey = 'idle'
  let t = 0

  return {
    group,
    setAnimation(key: AnimKey) { anim = key },
    update(dt: number) {
      t += dt

      // Procedural animation — simple, no broken transforms.
      if (anim === 'idle') {
        // Subtle breathing — torso scales slightly.
        torso.scale.y = 1 + Math.sin(t * 1.5) * 0.015
        // Arms hang naturally.
        armL.rotation.x = Math.sin(t * 0.8) * 0.02
        armR.rotation.x = -Math.sin(t * 0.8) * 0.02
        legL.rotation.x = 0
        legR.rotation.x = 0
        group.position.y = 0 // feet on ground
      } else if (anim === 'walk') {
        const s = t * 5
        armL.rotation.x = Math.sin(s) * 0.3
        armR.rotation.x = -Math.sin(s) * 0.3
        legL.rotation.x = -Math.sin(s) * 0.3
        legR.rotation.x = Math.sin(s) * 0.3
        group.position.y = Math.abs(Math.sin(s * 2)) * 0.03
      } else if (anim === 'run') {
        const s = t * 9
        armL.rotation.x = Math.sin(s) * 0.6
        armR.rotation.x = -Math.sin(s) * 0.6
        legL.rotation.x = -Math.sin(s) * 0.6
        legR.rotation.x = Math.sin(s) * 0.6
        group.position.y = Math.abs(Math.sin(s * 2)) * 0.08
      } else if (anim === 'cast') {
        armL.rotation.x = -1.0
        armR.rotation.x = -1.0
        armL.rotation.z = 0.3
        armR.rotation.z = -0.3
      } else if (anim === 'fly') {
        armL.rotation.x = 0.8
        armR.rotation.x = 0.8
        armL.rotation.z = 0.4
        armR.rotation.z = -0.4
        legL.rotation.x = -0.2
        legR.rotation.x = -0.2
        group.position.y = Math.sin(t * 2) * 0.05
      } else {
        // jump, sword_qi — reset to idle-ish.
        armL.rotation.x = 0
        armR.rotation.x = 0
        legL.rotation.x = 0
        legR.rotation.x = 0
        group.position.y = 0
      }
    },
    setYaw(yaw: number) {
      group.rotation.y = yaw
    },
    setAuraVisible(_v: boolean) {
      // Aura not implemented in this simplified model — no broken shader.
    },
    setSwordVisible(v: boolean) {
      swordGroup.visible = v
    },
    dispose() {
      group.traverse((o) => {
        const mesh = o as THREE.Mesh
        if (mesh.geometry) mesh.geometry.dispose()
        const m = mesh.material
        if (Array.isArray(m)) m.forEach((mm) => mm.dispose())
        else if (m) (m as THREE.Material).dispose()
      })
    },
  }
}
