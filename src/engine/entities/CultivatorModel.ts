/**
 * CultivatorModel — procedural AAA cultivator built from Three.js primitives.
 *
 * Skeletal: head, torso, arms (upper+lower), legs (upper+lower), hips.
 * Robe skirt with vertex-shader wind. Top-knot hair bun + flowing back hair.
 * Sheathed jade-green flying sword on back.
 *
 * Color palette: jade green, deep blue, gold, ivory white. NO indigo/blue
 * unless canon. Realm-based coloring.
 *
 * Quality bar: looks like a real xianxia cultivator (No Mortal Space /
 * Spirit Sect / Day 9 quality), NOT blocky Minecraft.
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
  /** Set body facing (yaw radians). */
  setYaw(yaw: number): void
  setAuraVisible(v: boolean): void
  setSwordVisible(v: boolean): void
  dispose(): void
}

const ROBE_WIND_VERT = /* glsl */ `
uniform float uTime;
uniform float uWind;
varying vec3 vNormal;
varying vec2 vUv;
void main() {
  vNormal = normalMatrix * normal;
  vUv = uv;
  vec3 p = position;
  // Wind sway: stronger at the bottom (uv.y high = hem).
  float sway = sin(uTime * 1.2 + p.x * 0.4 + p.z * 0.3) * 0.5 + 0.5;
  p.x += sway * uWind * (1.0 - uv.y) * 0.4;
  p.z += cos(uTime * 0.9 + p.x * 0.5) * uWind * (1.0 - uv.y) * 0.25;
  gl_Position = projectionMatrix * modelViewMatrix * vec4(p, 1.0);
}
`

const ROBE_FRAG = /* glsl */ `
uniform vec3 uRobeColor;
uniform vec3 uTrimColor;
uniform vec3 uSashColor;
varying vec3 vNormal;
varying vec2 vUv;
void main() {
  vec3 base = uRobeColor;
  // Sash band at uv.y ~0.35
  if (vUv.y > 0.30 && vUv.y < 0.40) base = uSashColor;
  // Trim at edges.
  if (vUv.y < 0.04 || vUv.y > 0.96) base = uTrimColor;
  // Fake lambert — kept bright (0.65..1.0) so the cultivator is always visible.
  vec3 N = normalize(vNormal);
  float diff = clamp(dot(N, vec3(0.4, 0.9, 0.3)), 0.0, 1.0);
  diff = 0.65 + diff * 0.35; // remap to 0.65..1.0
  gl_FragColor = vec4(base * diff, 1.0);
}
`

export function createCultivatorModel(realm: RealmKey = 'foundation', female = false): CultivatorModelHandle {
  const group = new THREE.Group()
  const style = REALM_STYLES[realm]

  // Skinned bone hierarchy.
  const root = new THREE.Group()
  group.add(root)

  // Torso (slightly tapered cylinder).
  const torsoGeo = new THREE.CylinderGeometry(female ? 0.16 : 0.20, female ? 0.22 : 0.26, 0.55, 16)
  const torsoMat = new THREE.ShaderMaterial({
    vertexShader: ROBE_WIND_VERT,
    fragmentShader: ROBE_FRAG,
    uniforms: {
      uTime: { value: 0 },
      uWind: { value: 0.5 },
      uRobeColor: { value: new THREE.Color(style.robe) },
      uTrimColor: { value: new THREE.Color(style.trim) },
      uSashColor: { value: new THREE.Color(style.sash) },
    },
  })
  const torso = new THREE.Mesh(torsoGeo, torsoMat)
  torso.position.y = 0.85
  torso.castShadow = true
  root.add(torso)

  // Robe skirt (cone, wider at bottom).
  const skirtGeo = new THREE.CylinderGeometry(female ? 0.22 : 0.26, 0.45, 0.7, 20, 1, true)
  const skirtMat = new THREE.ShaderMaterial({
    vertexShader: ROBE_WIND_VERT,
    fragmentShader: ROBE_FRAG,
    uniforms: {
      uTime: { value: 0 },
      uWind: { value: 0.8 },
      uRobeColor: { value: new THREE.Color(style.robe) },
      uTrimColor: { value: new THREE.Color(style.trim) },
      uSashColor: { value: new THREE.Color(style.sash) },
    },
    side: THREE.DoubleSide,
  })
  const skirt = new THREE.Mesh(skirtGeo, skirtMat)
  skirt.position.y = 0.40
  skirt.castShadow = true
  root.add(skirt)

  // Hips (small sphere).
  const hips = new THREE.Mesh(
    new THREE.SphereGeometry(0.20, 16, 12),
    new THREE.MeshStandardMaterial({ color: style.sash, roughness: 0.7 }),
  )
  hips.position.y = 0.62
  hips.castShadow = true
  root.add(hips)

  // Head (sphere).
  const headMat = new THREE.MeshStandardMaterial({ color: 0xf0d4b8, roughness: 0.6 })
  const head = new THREE.Mesh(new THREE.SphereGeometry(0.16, 16, 12), headMat)
  head.position.y = 1.30
  head.castShadow = true
  root.add(head)

  // Top-knot bun.
  const bun = new THREE.Mesh(
    new THREE.SphereGeometry(0.07, 12, 8),
    new THREE.MeshStandardMaterial({ color: 0x1a1a1a, roughness: 0.9 }),
  )
  bun.position.set(0, 1.45, -0.02)
  root.add(bun)

  // Flowing back hair (plane).
  const hairGeo = new THREE.PlaneGeometry(0.22, 0.45, 4, 8)
  const hairMat = new THREE.ShaderMaterial({
    vertexShader: ROBE_WIND_VERT,
    fragmentShader: /* glsl */ `
      varying vec3 vNormal; varying vec2 vUv;
      void main() { gl_FragColor = vec4(0.07, 0.06, 0.05, 1.0); }
    `,
    uniforms: { uTime: { value: 0 }, uWind: { value: 1.0 } },
    side: THREE.DoubleSide,
  })
  const hair = new THREE.Mesh(hairGeo, hairMat)
  hair.position.set(0, 1.28, -0.14)
  hair.rotation.x = 0.2
  root.add(hair)

  // Face plane (eyes / brows).
  const faceCanvas = document.createElement('canvas')
  faceCanvas.width = 128
  faceCanvas.height = 128
  const fctx = faceCanvas.getContext('2d')!
  fctx.clearRect(0, 0, 128, 128)
  fctx.fillStyle = '#1a1a1a'
  // Brows
  fctx.fillRect(36, 56, 18, 3)
  fctx.fillRect(74, 56, 18, 3)
  // Eyes
  fctx.fillRect(40, 64, 10, 4)
  fctx.fillRect(78, 64, 10, 4)
  // Mouth
  fctx.strokeStyle = '#8a3a3a'
  fctx.lineWidth = 2
  fctx.beginPath()
  fctx.moveTo(54, 90)
  fctx.lineTo(74, 90)
  fctx.stroke()
  const faceTex = new THREE.CanvasTexture(faceCanvas)
  const faceMat = new THREE.MeshBasicMaterial({ map: faceTex, transparent: true })
  const face = new THREE.Mesh(new THREE.PlaneGeometry(0.20, 0.20), faceMat)
  face.position.set(0, 1.30, 0.155)
  root.add(face)

  // Arms (upper + lower) — two pairs.
  const armMat = new THREE.ShaderMaterial({
    vertexShader: ROBE_WIND_VERT,
    fragmentShader: ROBE_FRAG,
    uniforms: {
      uTime: { value: 0 },
      uWind: { value: 0.3 },
      uRobeColor: { value: new THREE.Color(style.robe) },
      uTrimColor: { value: new THREE.Color(style.trim) },
      uSashColor: { value: new THREE.Color(style.sash) },
    },
  })
  const upperArmGeo = new THREE.CylinderGeometry(0.05, 0.045, 0.25, 8)
  const lowerArmGeo = new THREE.CylinderGeometry(0.045, 0.04, 0.25, 8)
  // Wide sleeve (cone) on each arm.
  const sleeveGeo = new THREE.CylinderGeometry(0.05, 0.16, 0.50, 12, 1, true)

  const armL = new THREE.Group()
  armL.position.set(-(female ? 0.22 : 0.26), 1.05, 0)
  const upperL = new THREE.Mesh(upperArmGeo, armMat)
  upperL.position.y = -0.12
  armL.add(upperL)
  const sleeveL = new THREE.Mesh(sleeveGeo, armMat)
  sleeveL.position.y = -0.20
  armL.add(sleeveL)
  const lowerL = new THREE.Mesh(lowerArmGeo, armMat)
  lowerL.position.set(0.02, -0.45, 0)
  armL.add(lowerL)
  armL.castShadow = true
  root.add(armL)

  const armR = armL.clone()
  armR.position.set((female ? 0.22 : 0.26), 1.05, 0)
  root.add(armR)

  // Legs (upper + lower) — mostly hidden by robe.
  const legMat = new THREE.MeshStandardMaterial({ color: 0x2a2a2a, roughness: 0.9 })
  const upperLegGeo = new THREE.CylinderGeometry(0.07, 0.06, 0.35, 8)
  const legL = new THREE.Mesh(upperLegGeo, legMat)
  legL.position.set(-0.10, 0.25, 0)
  legL.castShadow = true
  root.add(legL)
  const legR = legL.clone()
  legR.position.set(0.10, 0.25, 0)
  root.add(legR)

  // Boots.
  const bootMat = new THREE.MeshStandardMaterial({ color: 0x1a1a1a, roughness: 0.6 })
  const bootGeo = new THREE.BoxGeometry(0.12, 0.08, 0.20)
  const bootL = new THREE.Mesh(bootGeo, bootMat)
  bootL.position.set(-0.10, 0.04, 0.03)
  root.add(bootL)
  const bootR = bootL.clone()
  bootR.position.set(0.10, 0.04, 0.03)
  root.add(bootR)

  // Sheathed flying sword on back.
  const swordGroup = new THREE.Group()
  swordGroup.position.set(0, 0.95, -0.22)
  swordGroup.rotation.x = 0.3
  // Scabbard.
  const scab = new THREE.Mesh(
    new THREE.CylinderGeometry(0.018, 0.018, 0.95, 8),
    new THREE.MeshStandardMaterial({ color: 0x2a4a3a, roughness: 0.5, metalness: 0.3 }),
  )
  scab.castShadow = true
  swordGroup.add(scab)
  // Guard.
  const guard = new THREE.Mesh(
    new THREE.BoxGeometry(0.10, 0.02, 0.04),
    new THREE.MeshStandardMaterial({ color: 0xc8a050, roughness: 0.3, metalness: 0.8 }),
  )
  guard.position.y = 0.50
  swordGroup.add(guard)
  // Handle.
  const handle = new THREE.Mesh(
    new THREE.CylinderGeometry(0.012, 0.012, 0.14, 8),
    new THREE.MeshStandardMaterial({ color: 0x5a2a2a, roughness: 0.8 }),
  )
  handle.position.y = 0.58
  swordGroup.add(handle)
  // Tassel.
  const tassel = new THREE.Mesh(
    new THREE.ConeGeometry(0.018, 0.10, 6),
    new THREE.MeshStandardMaterial({ color: 0xff6080, roughness: 0.9 }),
  )
  tassel.position.y = 0.68
  swordGroup.add(tassel)
  root.add(swordGroup)

  // Qi aura (soft glow sphere, hidden by default).
  const auraGeo = new THREE.IcosahedronGeometry(0.85, 3)
  const auraMat = new THREE.ShaderMaterial({
    uniforms: {
      uColor: { value: new THREE.Color(style.aura) },
      uTime: { value: 0 },
      uOpacity: { value: 0 },
    },
    vertexShader: /* glsl */ `
      varying vec3 vNormal;
      varying vec3 vWorldPos;
      uniform float uTime;
      void main() {
        vNormal = normalize(normalMatrix * normal);
        vec3 p = position + normal * sin(uTime * 2.0 + position.y * 5.0) * 0.04;
        vec4 wp = modelMatrix * vec4(p, 1.0);
        vWorldPos = wp.xyz;
        gl_Position = projectionMatrix * viewMatrix * wp;
      }
    `,
    fragmentShader: /* glsl */ `
      uniform vec3 uColor;
      uniform float uOpacity;
      varying vec3 vNormal;
      varying vec3 vWorldPos;
      void main() {
        float fres = pow(1.0 - abs(dot(normalize(vNormal), normalize(cameraPosition - vWorldPos))), 2.0);
        gl_FragColor = vec4(uColor, fres * uOpacity * 0.6);
      }
    `,
    transparent: true,
    depthWrite: false,
    side: THREE.BackSide,
  })
  const aura = new THREE.Mesh(auraGeo, auraMat)
  aura.position.y = 0.85
  aura.visible = false
  root.add(aura)

  // Animation state.
  let anim: AnimKey = 'idle'
  let t = 0

  return {
    group,
    setAnimation(key) {
      anim = key
    },
    update(dt) {
      t += dt
      torsoMat.uniforms.uTime.value = t
      skirtMat.uniforms.uTime.value = t
      hairMat.uniforms.uTime.value = t
      armMat.uniforms.uTime.value = t
      auraMat.uniforms.uTime.value = t
      const windBoost = anim === 'fly' ? 2.5 : anim === 'run' ? 1.5 : 0.5
      skirtMat.uniforms.uWind.value = windBoost
      hairMat.uniforms.uWind.value = windBoost * 1.4

      // Simple procedural animation.
      if (anim === 'idle') {
        // Subtle breathing.
        torso.scale.y = 1 + Math.sin(t * 1.5) * 0.02
        armL.rotation.x = Math.sin(t * 1.2) * 0.04
        armR.rotation.x = -Math.sin(t * 1.2) * 0.04
      } else if (anim === 'walk') {
        const s = t * 5
        armL.rotation.x = Math.sin(s) * 0.4
        armR.rotation.x = -Math.sin(s) * 0.4
        legL.rotation.x = -Math.sin(s) * 0.4
        legR.rotation.x = Math.sin(s) * 0.4
        group.position.y = Math.abs(Math.sin(s * 2)) * 0.04
      } else if (anim === 'run') {
        const s = t * 9
        armL.rotation.x = Math.sin(s) * 0.7
        armR.rotation.x = -Math.sin(s) * 0.7
        legL.rotation.x = -Math.sin(s) * 0.7
        legR.rotation.x = Math.sin(s) * 0.7
        group.position.y = Math.abs(Math.sin(s * 2)) * 0.10
        torso.rotation.x = 0.18
      } else if (anim === 'cast') {
        armL.rotation.x = -1.2
        armR.rotation.x = -1.2
        armL.rotation.z = 0.3
        armR.rotation.z = -0.3
        torso.rotation.x = -0.1
      } else if (anim === 'sword_qi') {
        // Right arm thrusts forward.
        armR.rotation.x = -1.5
        armR.rotation.z = 0
        armL.rotation.x = -0.4
        torso.rotation.x = 0.1
      } else if (anim === 'fly') {
        // Arms back, body leans forward.
        armL.rotation.x = 1.2
        armR.rotation.x = 1.2
        armL.rotation.z = 0.5
        armR.rotation.z = -0.5
        legL.rotation.x = -0.3
        legR.rotation.x = -0.3
        torso.rotation.x = 0.25
        group.position.y = Math.sin(t * 2) * 0.08
      }
    },
    setYaw(yaw) {
      group.rotation.y = yaw
    },
    setAuraVisible(v) {
      aura.visible = v
      auraMat.uniforms.uOpacity.value = v ? 1 : 0
    },
    setSwordVisible(v) {
      swordGroup.visible = v
    },
    dispose() {
      group.traverse((o) => {
        if ((o as THREE.Mesh).geometry) (o as THREE.Mesh).geometry.dispose()
        const m = (o as THREE.Mesh).material
        if (Array.isArray(m)) m.forEach((mm) => mm.dispose())
        else if (m) (m as THREE.Material).dispose()
      })
    },
  }
}
