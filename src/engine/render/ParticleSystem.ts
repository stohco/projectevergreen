/**
 * ParticleSystem — GPU particle system for biome + weather flavor.
 *
 * Each "system" is a THREE.Points cloud with a custom shader (see
 * particles.glsl.ts). Particles are animated entirely on the GPU — the
 * CPU only updates the uniform `uTime` and the per-system wind/origin.
 *
 * Supported presets:
 *   - blossoms   — falling cherry blossom petals (plains / Wang Lin's village)
 *   - qiMotes    — floating jade motes around spirit veins (soft glow)
 *   - snow       — gentle snow (snow biome, calm)
 *   - sand       — swirling sand (desert / Ancient Demon City)
 *   - embers     — rising ember sparks (volcanic)
 *   - leaves     — swirling leaves (forest)
 *   - rain       — falling rain streaks (stormy weather)
 *
 * Hard cap: 5000 particles per system. The same shader runs all presets —
 * only uniforms differ (color, gravity, wind, size, lifetime, softness).
 *
 * NO canon chapter citations.
 */

import * as THREE from 'three'
import { PARTICLE_VERTEX_SHADER, PARTICLE_FRAGMENT_SHADER } from './shaders/particles.glsl'

export type ParticlePreset =
  | 'blossoms'
  | 'qiMotes'
  | 'snow'
  | 'sand'
  | 'embers'
  | 'leaves'
  | 'rain'

export interface ParticleSystemOptions {
  preset: ParticlePreset
  count?: number
  origin?: THREE.Vector3
  spreadRadius?: number
  pixelRatio?: number
}

export interface ParticleSystemHandle {
  points: THREE.Points
  preset: ParticlePreset
  setOrigin(o: THREE.Vector3): void
  setWind(w: THREE.Vector3): void
  setIntensity(i: number): void
  setVisible(v: boolean): void
  setCount(n: number): void // soft-cap (rebuilds buffer)
  update(dt: number): void
  dispose(): void
}

interface PresetConfig {
  color: THREE.Color
  color2: THREE.Color
  gravity: number // m/s^2 (negative = falls)
  wind: THREE.Vector3
  size: number // sprite size in meters
  lifetime: number // seconds
  softness: number
  glow: number
  defaultCount: number
}

const PRESETS: Record<ParticlePreset, PresetConfig> = {
  blossoms: {
    color: new THREE.Color(0xffc4d6),
    color2: new THREE.Color(0xffd9e6),
    gravity: -1.2,
    wind: new THREE.Vector3(1.4, -0.3, 0.6),
    size: 0.28,
    lifetime: 14,
    softness: 0.6,
    glow: 0.15,
    defaultCount: 1200,
  },
  qiMotes: {
    color: new THREE.Color(0x9be15d),
    color2: new THREE.Color(0x6fd9d6),
    gravity: 0.0, // float upward (wind.y positive)
    wind: new THREE.Vector3(0.2, 0.6, 0.2),
    size: 0.18,
    lifetime: 9,
    softness: 0.9,
    glow: 0.6,
    defaultCount: 2500,
  },
  snow: {
    color: new THREE.Color(0xffffff),
    color2: new THREE.Color(0xc4d6f5),
    gravity: -1.6,
    wind: new THREE.Vector3(0.8, -0.2, 0.4),
    size: 0.22,
    lifetime: 12,
    softness: 0.7,
    glow: 0.05,
    defaultCount: 3500,
  },
  sand: {
    color: new THREE.Color(0xd9b878),
    color2: new THREE.Color(0xb8945c),
    gravity: -0.3,
    wind: new THREE.Vector3(3.4, 0.1, 1.6),
    size: 0.14,
    lifetime: 6,
    softness: 0.5,
    glow: 0.0,
    defaultCount: 3500,
  },
  embers: {
    color: new THREE.Color(0xff7a3a),
    color2: new THREE.Color(0xffc04a),
    gravity: 0.6, // rise (negative of gravity convention — see shader)
    wind: new THREE.Vector3(0.4, 1.6, 0.4),
    size: 0.14,
    lifetime: 5,
    softness: 0.85,
    glow: 0.9,
    defaultCount: 1800,
  },
  leaves: {
    color: new THREE.Color(0xc46a2a),
    color2: new THREE.Color(0xe2a04a),
    gravity: -0.6,
    wind: new THREE.Vector3(1.8, -0.2, 1.0),
    size: 0.30,
    lifetime: 10,
    softness: 0.5,
    glow: 0.1,
    defaultCount: 1400,
  },
  rain: {
    color: new THREE.Color(0x9eb6c4),
    color2: new THREE.Color(0xb6c8d4),
    gravity: -22.0,
    wind: new THREE.Vector3(2.0, -0.4, 1.0),
    size: 0.08,
    lifetime: 2.2,
    softness: 0.0,
    glow: 0.0,
    defaultCount: 5000,
  },
}

const _v3 = new THREE.Vector3()

export function createParticleSystem(
  scene: THREE.Scene,
  opts: ParticleSystemOptions,
): ParticleSystemHandle {
  const preset = opts.preset
  const config = PRESETS[preset]
  const count = Math.min(opts.count ?? config.defaultCount, 5000)
  const origin = (opts.origin ?? new THREE.Vector3(0, 30, 0)).clone()
  const spreadRadius = opts.spreadRadius ?? 80
  const pixelRatio = opts.pixelRatio ?? (typeof window !== 'undefined' ? window.devicePixelRatio : 1)

  // Per-particle attributes.
  const positions = new Float32Array(count * 3)
  const seeds = new Float32Array(count)
  const offsets = new Float32Array(count * 3)
  const sizeMuls = new Float32Array(count)
  for (let i = 0; i < count; i++) {
    positions[i * 3] = 0
    positions[i * 3 + 1] = 0
    positions[i * 3 + 2] = 0
    seeds[i] = Math.random()
    offsets[i * 3] = (Math.random() - 0.5) * spreadRadius * 2
    offsets[i * 3 + 1] = (Math.random() - 0.5) * spreadRadius * 2
    offsets[i * 3 + 2] = (Math.random() - 0.5) * spreadRadius * 2
    sizeMuls[i] = 0.5 + Math.random() * 1.0
  }
  const geo = new THREE.BufferGeometry()
  geo.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  geo.setAttribute('aSeed', new THREE.BufferAttribute(seeds, 1))
  geo.setAttribute('aOffset', new THREE.BufferAttribute(offsets, 3))
  geo.setAttribute('aSizeMul', new THREE.BufferAttribute(sizeMuls, 1))

  const wind = config.wind.clone()
  const uniforms = {
    uTime: { value: 0 },
    uPixelRatio: { value: pixelRatio },
    uOrigin: { value: origin.clone() },
    uWind: { value: wind },
    uGravity: { value: config.gravity },
    uSpreadRadius: { value: spreadRadius },
    uLifetime: { value: config.lifetime },
    uSize: { value: config.size },
    uColor: { value: config.color.clone() },
    uColor2: { value: config.color2.clone() },
    uGlow: { value: config.glow },
    uSprite: { value: null },
    uHasSprite: { value: 0 },
    uSoftness: { value: config.softness },
    uScreenRes: { value: new THREE.Vector2(1, 1) },
    uIntensity: { value: 1.0 },
  }
  const mat = new THREE.ShaderMaterial({
    name: `Particles_${preset}`,
    uniforms,
    vertexShader: PARTICLE_VERTEX_SHADER,
    fragmentShader: PARTICLE_FRAGMENT_SHADER,
    transparent: true,
    depthWrite: false,
    blending: THREE.AdditiveBlending,
    toneMapped: false,
  })

  const points = new THREE.Points(geo, mat)
  points.frustumCulled = false
  points.renderOrder = 700
  scene.add(points)

  return {
    points,
    preset,
    setOrigin(o) {
      uniforms.uOrigin.value.copy(o)
    },
    setWind(w) {
      uniforms.uWind.value.copy(w)
    },
    setIntensity(i) {
      uniforms.uIntensity.value = i
      points.visible = i > 0.01
    },
    setVisible(v) {
      points.visible = v
    },
    setCount(n) {
      // Soft-cap: we don't realloc the buffer; instead we toggle visibility
      // of surplus particles by shrinking the draw range.
      const capped = Math.max(0, Math.min(n, count))
      geo.setDrawRange(0, capped)
    },
    update(dt) {
      uniforms.uTime.value += dt
    },
    dispose() {
      geo.dispose()
      mat.dispose()
      scene.remove(points)
    },
  }
}

/**
 * Helper: spawn a full biome-appropriate particle system set based on the
 * player's current biome. Returns all systems + a switch helper.
 */
export function spawnBiomeParticleSet(scene: THREE.Scene, biome: string): {
  systems: ParticleSystemHandle[]
  setBiome(b: string): void
  update(dt: number): void
  dispose(): void
} {
  const all: ParticleSystemHandle[] = []
  const byPreset = new Map<ParticlePreset, ParticleSystemHandle>()

  function ensure(preset: ParticlePreset) {
    let sys = byPreset.get(preset)
    if (!sys) {
      sys = createParticleSystem(scene, { preset })
      sys.setVisible(false)
      byPreset.set(preset, sys)
      all.push(sys)
    }
    return sys
  }

  ensure('blossoms')
  ensure('qiMotes')
  ensure('snow')
  ensure('sand')
  ensure('embers')
  ensure('leaves')

  const PRESET_BY_BIOME: Record<string, ParticlePreset[]> = {
    plains: ['blossoms'],
    forest: ['leaves'],
    desert: ['sand'],
    snow: ['snow'],
    volcanic: ['embers'],
    mountains: ['blossoms'],
    swamp: ['blossoms'],
    coast: [],
    sea: [],
  }

  function setBiome(b: string) {
    const active = PRESET_BY_BIOME[b] ?? []
    for (const [preset, sys] of byPreset) {
      sys.setVisible(active.includes(preset))
    }
  }

  setBiome(biome)
  return {
    systems: all,
    setBiome,
    update(dt: number) {
      for (const s of all) s.update(dt)
    },
    dispose() {
      for (const s of all) s.dispose()
      all.length = 0
      byPreset.clear()
    },
  }
}

/** Re-exported for integrator convenience. */
export function applyWindToSystem(sys: ParticleSystemHandle, wind: THREE.Vector3) {
  sys.setWind(_v3.copy(wind))
}
