/**
 * WeatherSystem — dynamic weather state machine for the Er Gen Verse.
 *
 * States: clear, cloudy, rain, storm, snow, mist. Plus a special
 * "heavenly tribulation" (天劫) state that overlays on storm when a
 * cultivator breaks through a realm (visual-only — purple/gold lightning).
 *
 * Transitions are 30–60 second smooth blends — the WeatherSystem lerps a
 * "weather profile" (cloud coverage, fog density, light intensity, rain
 * strength, wind vector) from the current to the target profile so the
 * world reacts gracefully instead of snapping.
 *
 * Lightning strikes are scheduled stochastically during storm state; the
 * flash + thunder delay are exposed via getLightningFlash() so the
 * LightingRig / PostProcessing can react.
 *
 * NO canon chapter citations — the 天劫 heavenly-tribulation motif is a
 * stock xianxia trope, public domain.
 */

import * as THREE from 'three'

export type WeatherState = 'clear' | 'cloudy' | 'rain' | 'storm' | 'snow' | 'mist' | 'tribulation'

export interface WeatherProfile {
  cloudCoverage: number    // 0..1
  cloudHeight: number      // meters
  fogDensity: number       // exp2 density
  rainStrength: number     // 0..1
  snowStrength: number     // 0..1
  mistStrength: number     // 0..1
  wind: THREE.Vector3      // m/s
  sunIntensity: number     // 0..1 (multiplier on sun light)
  ambientBoost: number     // 0..1 extra hemi
  lightningRate: number    // strikes per minute
  lightningColor: THREE.Color
  tint: THREE.Color        // scene tint (e.g., snow cools, storm darkens)
}

const PROFILES: Record<WeatherState, WeatherProfile> = {
  clear: {
    cloudCoverage: 0.20,
    cloudHeight: 420,
    fogDensity: 0.0006,
    rainStrength: 0,
    snowStrength: 0,
    mistStrength: 0,
    wind: new THREE.Vector3(1.5, 0, 0.4),
    sunIntensity: 1.0,
    ambientBoost: 0.0,
    lightningRate: 0,
    lightningColor: new THREE.Color(0xffffff),
    tint: new THREE.Color(1, 1, 1),
  },
  cloudy: {
    cloudCoverage: 0.65,
    cloudHeight: 380,
    fogDensity: 0.0009,
    rainStrength: 0,
    snowStrength: 0,
    mistStrength: 0,
    wind: new THREE.Vector3(2.2, 0, 0.8),
    sunIntensity: 0.55,
    ambientBoost: 0.1,
    lightningRate: 0,
    lightningColor: new THREE.Color(0xffffff),
    tint: new THREE.Color(0.92, 0.94, 1.0),
  },
  rain: {
    cloudCoverage: 0.82,
    cloudHeight: 340,
    fogDensity: 0.0014,
    rainStrength: 0.6,
    snowStrength: 0,
    mistStrength: 0.15,
    wind: new THREE.Vector3(3.0, 0, 1.2),
    sunIntensity: 0.3,
    ambientBoost: 0.2,
    lightningRate: 0,
    lightningColor: new THREE.Color(0xffffff),
    tint: new THREE.Color(0.78, 0.82, 0.92),
  },
  storm: {
    cloudCoverage: 0.95,
    cloudHeight: 300,
    fogDensity: 0.0019,
    rainStrength: 1.0,
    snowStrength: 0,
    mistStrength: 0.25,
    wind: new THREE.Vector3(5.5, 0, 2.4),
    sunIntensity: 0.12,
    ambientBoost: 0.3,
    lightningRate: 12, // ~12 strikes/min
    lightningColor: new THREE.Color(0.85, 0.92, 1.0),
    tint: new THREE.Color(0.55, 0.58, 0.68),
  },
  snow: {
    cloudCoverage: 0.7,
    cloudHeight: 360,
    fogDensity: 0.0012,
    rainStrength: 0,
    snowStrength: 0.7,
    mistStrength: 0.2,
    wind: new THREE.Vector3(1.8, 0, 0.8),
    sunIntensity: 0.5,
    ambientBoost: 0.25,
    lightningRate: 0,
    lightningColor: new THREE.Color(0xffffff),
    tint: new THREE.Color(0.94, 0.96, 1.0),
  },
  mist: {
    cloudCoverage: 0.35,
    cloudHeight: 400,
    fogDensity: 0.0030,
    rainStrength: 0,
    snowStrength: 0,
    mistStrength: 1.0,
    wind: new THREE.Vector3(0.6, 0, 0.2),
    sunIntensity: 0.7,
    ambientBoost: 0.15,
    lightningRate: 0,
    lightningColor: new THREE.Color(0xffffff),
    tint: new THREE.Color(0.96, 0.97, 1.0),
  },
  tribulation: {
    cloudCoverage: 0.99,
    cloudHeight: 280,
    fogDensity: 0.0024,
    rainStrength: 0.8,
    snowStrength: 0,
    mistStrength: 0.3,
    wind: new THREE.Vector3(7.0, 0, 3.0),
    sunIntensity: 0.05,
    ambientBoost: 0.4,
    lightningRate: 30, // frantic strikes
    lightningColor: new THREE.Color(0.78, 0.58, 1.0), // purple-gold tribulation
    tint: new THREE.Color(0.65, 0.55, 0.85),
  },
}

export interface LightningStrike {
  time: number       // seconds since strike started
  duration: number   // total seconds
  position: THREE.Vector3
  color: THREE.Color
  intensity: number
}

export interface WeatherHandle {
  getState(): WeatherState
  setState(s: WeatherState, transitionSeconds?: number): void
  /** Current interpolated profile (call every frame to read live values). */
  getProfile(): WeatherProfile
  getLightningFlash(): number // 0..1 brightness pulse for global lightning
  getActiveStrike(): LightningStrike | null
  setBiome(b: 'plains' | 'mountains' | 'forest' | 'desert' | 'snow' | 'swamp' | 'coast' | 'sea' | 'volcanic'): void
  /** Trigger a heavenly tribulation (天劫) burst for a duration. */
  triggerTribulation(durationSeconds: number): void
  update(dt: number, cameraPos: THREE.Vector3): void
  dispose(): void
}

export function createWeatherSystem(initial: WeatherState = 'clear'): WeatherHandle {
  let current = initial
  let target = initial
  let transitionRemaining = 0
  let transitionTotal = 0
  let fromProfile = cloneProfile(PROFILES[initial])
  const liveProfile = cloneProfile(PROFILES[initial])

  // Lightning scheduling.
  let nextStrikeIn = 60 / Math.max(0.1, PROFILES[initial].lightningRate)
  let activeStrike: LightningStrike | null = null
  let lightningFlash = 0
  let biome: 'plains' | 'mountains' | 'forest' | 'desert' | 'snow' | 'swamp' | 'coast' | 'sea' | 'volcanic' = 'plains'
  let tribulationEndsAt = -1

  function cloneProfile(p: WeatherProfile): WeatherProfile {
    return {
      cloudCoverage: p.cloudCoverage,
      cloudHeight: p.cloudHeight,
      fogDensity: p.fogDensity,
      rainStrength: p.rainStrength,
      snowStrength: p.snowStrength,
      mistStrength: p.mistStrength,
      wind: p.wind.clone(),
      sunIntensity: p.sunIntensity,
      ambientBoost: p.ambientBoost,
      lightningRate: p.lightningRate,
      lightningColor: p.lightningColor.clone(),
      tint: p.tint.clone(),
    }
  }

  function lerpProfile(a: WeatherProfile, b: WeatherProfile, t: number): WeatherProfile {
    const out = liveProfile
    out.cloudCoverage = THREE.MathUtils.lerp(a.cloudCoverage, b.cloudCoverage, t)
    out.cloudHeight = THREE.MathUtils.lerp(a.cloudHeight, b.cloudHeight, t)
    out.fogDensity = THREE.MathUtils.lerp(a.fogDensity, b.fogDensity, t)
    out.rainStrength = THREE.MathUtils.lerp(a.rainStrength, b.rainStrength, t)
    out.snowStrength = THREE.MathUtils.lerp(a.snowStrength, b.snowStrength, t)
    out.mistStrength = THREE.MathUtils.lerp(a.mistStrength, b.mistStrength, t)
    out.wind.lerpVectors(a.wind, b.wind, t)
    out.sunIntensity = THREE.MathUtils.lerp(a.sunIntensity, b.sunIntensity, t)
    out.ambientBoost = THREE.MathUtils.lerp(a.ambientBoost, b.ambientBoost, t)
    out.lightningRate = THREE.MathUtils.lerp(a.lightningRate, b.lightningRate, t)
    out.lightningColor.lerpColors(a.lightningColor, b.lightningColor, t)
    out.tint.lerpColors(a.tint, b.tint, t)
    return out
  }

  function update(dt: number, cameraPos: THREE.Vector3) {
    if (transitionRemaining > 0) {
      transitionRemaining -= dt
      const t = 1 - Math.max(0, transitionRemaining) / Math.max(0.001, transitionTotal)
      lerpProfile(fromProfile, PROFILES[target], t)
    } else {
      current = target
      const p = PROFILES[target]
      liveProfile.cloudCoverage = p.cloudCoverage
      liveProfile.cloudHeight = p.cloudHeight
      liveProfile.fogDensity = p.fogDensity
      liveProfile.rainStrength = p.rainStrength
      liveProfile.snowStrength = p.snowStrength
      liveProfile.mistStrength = p.mistStrength
      liveProfile.wind.copy(p.wind)
      liveProfile.sunIntensity = p.sunIntensity
      liveProfile.ambientBoost = p.ambientBoost
      liveProfile.lightningRate = p.lightningRate
      liveProfile.lightningColor.copy(p.lightningColor)
      liveProfile.tint.copy(p.tint)
    }

    // Lightning scheduling.
    if (liveProfile.lightningRate > 0.1) {
      nextStrikeIn -= dt
      if (nextStrikeIn <= 0) {
        // Strike near the player so they see it.
        const angle = Math.random() * Math.PI * 2
        const dist = 60 + Math.random() * 220
        activeStrike = {
          time: 0,
          duration: 0.55 + Math.random() * 0.35,
          position: new THREE.Vector3(
            cameraPos.x + Math.cos(angle) * dist,
            220,
            cameraPos.z + Math.sin(angle) * dist,
          ),
          color: liveProfile.lightningColor.clone(),
          intensity: 0.8 + Math.random() * 0.6,
        }
        lightningFlash = 1.0
        nextStrikeIn = 60 / liveProfile.lightningRate * (0.4 + Math.random() * 1.2)
      }
    } else {
      activeStrike = null
    }
    if (activeStrike) {
      activeStrike.time += dt
      if (activeStrike.time > activeStrike.duration) {
        activeStrike = null
      }
    }
    lightningFlash = Math.max(0, lightningFlash - dt * 3.5)

    // End tribulation if expired.
    if (tribulationEndsAt > 0 && performance.now() / 1000 > tribulationEndsAt) {
      tribulationEndsAt = -1
      // Return to a sensible prior state — defaults to stormy after a
      // tribulation (the canon aftermath is always exhaustion).
      setState('storm', 8)
    }

    // Blizzard boost in snow biome during storms.
    if (biome === 'snow' && current === 'storm') {
      liveProfile.snowStrength = Math.min(1, liveProfile.snowStrength + 0.6)
      liveProfile.wind.multiplyScalar(1.6)
    }
  }

  function setState(s: WeatherState, transitionSeconds = 40) {
    if (s === target) return
    fromProfile = cloneProfile(liveProfile)
    target = s
    transitionTotal = transitionSeconds
    transitionRemaining = transitionSeconds
    if (s === 'tribulation') {
      // Reset lightning timer for fast first strike.
      nextStrikeIn = 0.5
    }
  }

  function triggerTribulation(durationSeconds: number) {
    setState('tribulation', 4)
    tribulationEndsAt = performance.now() / 1000 + durationSeconds
  }

  return {
    getState() { return current },
    setState,
    getProfile() { return liveProfile },
    getLightningFlash() { return lightningFlash },
    getActiveStrike() { return activeStrike },
    setBiome(b) { biome = b },
    triggerTribulation,
    update,
    dispose() { /* no GPU resources to free directly */ },
  }
}
