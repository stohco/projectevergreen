/**
 * AtmosphereRenderer — distance fog + height fog + cultivator qi aura.
 *
 * The fog is driven by scene.fog (THREE.FogExp2). We re-tint its color every
 * frame based on:
 *   - the current sky horizon color (so distant mountains fade into the
 *     haze naturally),
 *   - the biome at the camera's footprint (greenish in forests, white in
 *     snow, sandy in desert, jade in spirit-vein regions),
 *   - the day phase (dawn/dusk warm tint, night cool tint),
 *   - a low-lying "valley mist" component that thickens fog at low world-Y
 *     and around water (set by setHeightFogStrength).
 *
 * The qi-aura system is a small pool of pre-built halo meshes that we attach
 * to cultivators on demand. Each halo uses the qi_aura.glsl shader and is
 * driven by a per-cultivator qi-flux value.
 *
 * NO canon chapter citations.
 */

import * as THREE from 'three'
import { QI_AURA_VERTEX_SHADER, QI_AURA_FRAGMENT_SHADER } from './shaders/qi_aura.glsl'

export type BiomeKind =
  | 'plains'
  | 'mountains'
  | 'forest'
  | 'desert'
  | 'snow'
  | 'swamp'
  | 'coast'
  | 'sea'
  | 'volcanic'

export interface AuraTarget {
  id: string
  object: THREE.Object3D
  mesh: THREE.Mesh
  pulse: number
  targetPulse: number
}

export interface AtmosphereHandle {
  group: THREE.Group
  setBiome(b: BiomeKind): void
  setSkyHorizonColor(c: THREE.Color): void
  setDayMix(m: number): void
  setDawnDuskMix(m: number): void
  setFogDensity(d: number): void
  getFogDensity(): number
  setHeightFogStrength(s: number): void
  /** Register a cultivator that should show a qi aura when casting. */
  registerAura(id: string, parent: THREE.Object3D, opts?: {
    radius?: number
    color?: THREE.ColorRepresentation
    rimColor?: THREE.ColorRepresentation
  }): AuraTarget
  unregisterAura(id: string): void
  setAuraPulse(id: string, pulse: number): void
  update(dt: number, camera: THREE.Camera): void
  dispose(): void
}

const BIOME_FOG_COLORS: Record<BiomeKind, THREE.Color> = {
  plains: new THREE.Color(0.55, 0.65, 0.45),     // greenish
  mountains: new THREE.Color(0.55, 0.58, 0.62),  // neutral gray-blue
  forest: new THREE.Color(0.32, 0.50, 0.30),     // deep green
  desert: new THREE.Color(0.85, 0.74, 0.50),     // sandy
  snow: new THREE.Color(0.92, 0.94, 0.98),       // near white
  swamp: new THREE.Color(0.35, 0.42, 0.36),      // murky green-brown
  coast: new THREE.Color(0.60, 0.72, 0.74),      // sea-foam
  sea: new THREE.Color(0.30, 0.50, 0.55),        // jade sea
  volcanic: new THREE.Color(0.45, 0.22, 0.18),   // smoky ember
}

export function createAtmosphereRenderer(
  scene: THREE.Scene,
  initialBiome: BiomeKind = 'plains',
): AtmosphereHandle {
  const group = new THREE.Group()
  group.name = 'AtmosphereRenderer'
  scene.add(group)

  // Fog setup — uses THREE.FogExp2 for natural exponential falloff. We
  // mutate the color and density each frame.
  const baseDensity = 0.0009
  const fog = new THREE.FogExp2(0xbcd6ff, baseDensity)
  scene.fog = fog

  // Biome accent — added on top of the sky horizon color.
  let currentBiome: BiomeKind = initialBiome
  const biomeAccent = BIOME_FOG_COLORS[initialBiome].clone()

  let skyHorizon = new THREE.Color(0.92, 0.86, 0.70)
  let dayMix = 1
  let dawnDuskMix = 0
  let heightFogStrength = 0 // 0..1
  let density = baseDensity

  // Reusable scratch colors.
  const _c1 = new THREE.Color()
  const _c2 = new THREE.Color()
  const _nightTint = new THREE.Color(0.05, 0.06, 0.12)
  const _sunsetTint = new THREE.Color(0.95, 0.45, 0.25)

  // --- Qi aura pool --------------------------------------------------------
  const auras = new Map<string, AuraTarget>()
  const auraGroup = new THREE.Group()
  auraGroup.name = 'QiAuras'
  group.add(auraGroup)

  const _auraMatCache = new Map<string, THREE.ShaderMaterial>()

  function makeAuraMaterial(color: THREE.Color, rim: THREE.Color): THREE.ShaderMaterial {
    const key = `${color.getHexString()}_${rim.getHexString()}`
    const cached = _auraMatCache.get(key)
    if (cached) return cached
    const mat = new THREE.ShaderMaterial({
      name: 'QiAura',
      uniforms: {
        uTime: { value: 0 },
        uPulse: { value: 0 },
        uRadius: { value: 1 },
        uAuraColor: { value: color.clone() },
        uRimColor: { value: rim.clone() },
      },
      vertexShader: QI_AURA_VERTEX_SHADER,
      fragmentShader: QI_AURA_FRAGMENT_SHADER,
      transparent: true,
      depthWrite: false,
      depthTest: true,
      blending: THREE.AdditiveBlending,
      side: THREE.DoubleSide,
    })
    _auraMatCache.set(key, mat)
    return mat
  }

  // --- Per-frame update ----------------------------------------------------
  function update(dt: number, _camera: THREE.Camera) {
    // Lerp biome accent.
    const targetAccent = BIOME_FOG_COLORS[currentBiome]
    biomeAccent.lerp(targetAccent, Math.min(1, dt * 0.6))

    // Compose fog color: sky horizon + biome accent + day/dusk tint.
    _c1.copy(skyHorizon).multiplyScalar(0.55)
    _c2.copy(biomeAccent).multiplyScalar(0.45)
    _c1.add(_c2)
    // Sunset warm tint at horizon.
    _c1.lerp(_sunsetTint, dawnDuskMix * 0.35)
    // Night dim tint.
    _c1.lerp(_nightTint, (1 - dayMix) * 0.5)
    fog.color.copy(_c1)

    // Height fog: boost density when camera is low (valleys).
    const camY = _camera.position.y
    const lowAltBoost = THREE.MathUtils.clamp(1 - (camY - 8) / 60, 0, 1)
    fog.density = density * (1 + heightFogStrength * lowAltBoost * 1.8)

    // Update aura uniforms + visibility.
    for (const aura of auras.values()) {
      // Smoothly chase target pulse.
      aura.pulse += (aura.targetPulse - aura.pulse) * Math.min(1, dt * 4)
      const mat = aura.mesh.material as THREE.ShaderMaterial
      mat.uniforms.uTime.value += dt
      mat.uniforms.uPulse.value = aura.pulse
      const visible = aura.pulse > 0.02
      aura.mesh.visible = visible
    }
  }

  return {
    group,
    setBiome(b) {
      currentBiome = b
    },
    setSkyHorizonColor(c) {
      skyHorizon.copy(c)
    },
    setDayMix(m) {
      dayMix = m
    },
    setDawnDuskMix(m) {
      dawnDuskMix = m
    },
    setFogDensity(d) {
      density = d
    },
    getFogDensity() {
      return density
    },
    setHeightFogStrength(s) {
      heightFogStrength = THREE.MathUtils.clamp(s, 0, 1)
    },
    registerAura(id, parent, opts = {}) {
      const existing = auras.get(id)
      if (existing) return existing
      const color = new THREE.Color(opts.color ?? 0x9be15d) // jade green
      const rim = new THREE.Color(opts.rimColor ?? 0x6fd9d6) // cyan rim
      const radius = opts.radius ?? 1.4
      const mat = makeAuraMaterial(color, rim)
      mat.uniforms.uRadius.value = radius
      const geo = new THREE.IcosahedronGeometry(1, 4)
      const mesh = new THREE.Mesh(geo, mat)
      mesh.frustumCulled = false
      mesh.visible = false
      parent.add(mesh)
      const aura: AuraTarget = {
        id,
        object: parent,
        mesh,
        pulse: 0,
        targetPulse: 0,
      }
      auras.set(id, aura)
      return aura
    },
    unregisterAura(id) {
      const aura = auras.get(id)
      if (!aura) return
      aura.object.remove(aura.mesh)
      aura.mesh.geometry.dispose()
      auras.delete(id)
    },
    setAuraPulse(id, pulse) {
      const aura = auras.get(id)
      if (!aura) return
      aura.targetPulse = THREE.MathUtils.clamp(pulse, 0, 1)
    },
    update,
    dispose() {
      for (const aura of auras.values()) {
        aura.object.remove(aura.mesh)
        aura.mesh.geometry.dispose()
      }
      auras.clear()
      for (const m of _auraMatCache.values()) m.dispose()
      _auraMatCache.clear()
      scene.remove(group)
      if (scene.fog === fog) scene.fog = null
    },
  }
}
