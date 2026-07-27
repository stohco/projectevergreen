/**
 * render/index.ts — Atmosphere stack integrator.
 *
 * createAtmosphereStack(scene, camera, renderer) mounts:
 *   - SkySystem (sky dome + sun + moon + 4000 stars + 28 mansions + Milky Way)
 *   - VolumetricClouds (raymarched xianxia clouds + sea-of-clouds)
 *   - AtmosphereRenderer (distance fog + biome tint + height fog + qi aura)
 *   - WeatherSystem (clear / cloudy / rain / storm / snow / mist / tribulation)
 *   - ParticleSystem (blossoms / qi motes / snow / sand / embers / leaves / rain)
 *   - LightingRig (sun + hemi + follow + spirit vein point lights)
 *   - PostProcessing (SSAO + bloom + god rays + DOF + chromatic + vignette + grain + ACES + teal-orange)
 *
 * Returns { update(dt, timeOfDay), setWeather(state), getWeather(), dispose() }.
 *
 * Other agents (voxel terrain, cultivator models, HUD) call into the exposed
 * handles via the returned `atmosphere` object for cross-system wiring:
 *   - sky.getSunDirection() — for cultivator shadows + sword-qi reflection
 *   - sky.uniforms.uSunColor — for water + cloud tint
 *   - clouds.getCloudShadowTexture() — for terrain shader to sample
 *   - weather.getProfile() — for weather-driven overrides
 *   - lighting.addSpiritVeinLight() — for each materialized vein
 *   - postfx.setEnabled(name, on) — for perf scaling
 *
 * NO canon chapter citations.
 */

import * as THREE from 'three'
import { createSky, SkyHandle as SkySystemHandle } from './SkySystem'
export const DAY_LENGTH_SECONDS = 120 // 2min day cycle for playtesting
import { createClouds as createVolumetricClouds, CloudsHandle } from './VolumetricClouds'
import {
  createAtmosphereRenderer,
  AtmosphereHandle,
  BiomeKind,
} from './AtmosphereRenderer'
import {
  createParticleSystem,
  spawnBiomeParticleSet,
  ParticleSystemHandle,
  ParticlePreset,
} from './ParticleSystem'
import {
  createWeatherSystem,
  WeatherHandle,
  WeatherState,
} from '../effects/WeatherSystem'
import { createLightingRig, LightingRigHandle } from '../effects/LightingRig'
import { createPostFX, PostFXHandle as PostProcessingHandle } from './PostProcessing'

export interface AtmosphereStackHandle {
  sky: SkySystemHandle
  clouds: CloudsHandle
  atmosphere: AtmosphereHandle
  weather: WeatherHandle
  lighting: LightingRigHandle
  postfx: PostProcessingHandle
  particles: {
    biome: ReturnType<typeof spawnBiomeParticleSet>
    rain: ParticleSystemHandle
    snow: ParticleSystemHandle
  }
  update(dt: number, timeOfDay: number): void
  setWeather(state: WeatherState, transitionSeconds?: number): void
  getWeather(): WeatherState
  setBiome(b: BiomeKind): void
  triggerTribulation(durationSeconds: number): void
  setPlayerTarget(target: THREE.Object3D | null): void
  setPerfTier(tier: 'low' | 'medium' | 'high' | 'ultra'): void
  dispose(): void
}

export interface AtmosphereStackOptions {
  initialBiome?: BiomeKind
  initialWeather?: WeatherState
  perfTier?: 'low' | 'medium' | 'high' | 'ultra'
  enablePostFX?: boolean
  enableClouds?: boolean
}

const _sunDir = new THREE.Vector3()
const _skyHorizonColor = new THREE.Color()
const _ambientColor = new THREE.Color()
const _playerPos = new THREE.Vector3()

export function createAtmosphereStack(
  scene: THREE.Scene,
  camera: THREE.PerspectiveCamera,
  renderer: THREE.WebGLRenderer,
  options: AtmosphereStackOptions = {},
): AtmosphereStackHandle {
  const initialBiome = options.initialBiome ?? 'plains'
  const initialWeather = options.initialWeather ?? 'clear'
  const perfTier = options.perfTier ?? 'high'
  const enablePostFX = options.enablePostFX ?? true
  const enableClouds = options.enableClouds ?? true

  // --- Mount all subsystems ----------------------------------------------
  const sky = createSky(scene)
  const clouds = createVolumetricClouds(scene)
  const atmosphere = createAtmosphereRenderer(scene, initialBiome)
  const weather = createWeatherSystem(initialWeather)
  const lighting = createLightingRig(scene)
  const postfx = enablePostFX
    ? createPostFX(renderer, scene, camera, {
        enabled: {
          ssao: perfTier !== 'low',
          bloom: true,
          godRays: perfTier !== 'low',
          dof: false,
          chromatic: perfTier !== 'low',
          vignette: true,
          grain: true,
          tonemap: true,
          colorGrade: true,
        },
      })
    : null

  // Hand the sky's sun light to the lighting rig (so the rig colors it).
  const skyAny = sky as unknown as { sun?: THREE.DirectionalLight; sunMesh?: THREE.Mesh }
  if (skyAny.sun) lighting.setSunLight(skyAny.sun)
  if (postfx && skyAny.sunMesh) postfx.setSunMesh(skyAny.sunMesh)

  // Particle systems — biome-flavored + rain + snow extras.
  const biomeParticles = spawnBiomeParticleSet(scene, initialBiome)
  const rainSystem = createParticleSystem(scene, {
    preset: 'rain',
    count: perfTier === 'low' ? 2000 : 5000,
  })
  rainSystem.setVisible(false)
  const snowSystem = createParticleSystem(scene, {
    preset: 'snow',
    count: perfTier === 'low' ? 1500 : 3500,
  })
  snowSystem.setVisible(false)

  // --- Per-frame update --------------------------------------------------
  function update(dt: number, timeOfDay: number) {
    // 1. Advance the sky first — its uniforms drive everything else.
    sky.update(dt, timeOfDay)
    _sunDir.copy(sky.getSunDirection())

    // 2. Weather ticks next — produces a live profile for everyone.
    _playerPos.copy(camera.position)
    weather.update(dt, _playerPos)
    const profile = weather.getProfile()

    // 3. Lighting rig follows the sun direction + weather multipliers.
    lighting.setSunDirection(_sunDir)
    lighting.setSunColor(sky.getSunColor())
    lighting.setSunIntensity(sky.sun.intensity * profile.sunIntensity)
    // Hemi intensity scales with weather (overcast = more bounce).
    lighting.setHemiIntensity(0.55 + profile.ambientBoost)
    // Lightning flash → boost hemi.
    const flash = weather.getLightningFlash()
    if (flash > 0.01) {
      lighting.applyLightningFlash(flash, profile.lightningColor)
    }
    lighting.update(dt)

    // 4. Clouds follow the sun + weather coverage.
    if (enableClouds) {
      clouds.setCoverage(profile.cloudCoverage)
      clouds.setSunDirection(_sunDir)
      clouds.setSunColor(sky.getSunColor())
      // Ambient cloud color tracks the sky horizon (warm at sunset).
      _ambientColor.copy(sky.uniforms.uDayHorizon.value as THREE.Color)
      _ambientColor.lerp(sky.uniforms.uSunsetMid.value as THREE.Color, sky.getDawnDuskMix() * 0.6)
      clouds.setAmbientColor(_ambientColor)
      clouds.setDayMix(sky.getDayMix())
      clouds.setDawnDuskMix(sky.getDawnDuskMix())
      // Sea-of-clouds boost in mountains biome.
      clouds.setSeaOfCloudsMix(currentBiome === 'mountains' ? 0.8 : 0.0)
      clouds.update(dt, camera)
    }

    // 5. Atmosphere (fog) — tint by sky horizon + biome accent.
    _skyHorizonColor.copy(sky.uniforms.uDayHorizon.value as THREE.Color)
    atmosphere.setSkyHorizonColor(_skyHorizonColor)
    atmosphere.setDayMix(sky.getDayMix())
    atmosphere.setDawnDuskMix(sky.getDawnDuskMix())
    atmosphere.setFogDensity(profile.fogDensity)
    // Mist strengthens height fog.
    atmosphere.setHeightFogStrength(profile.mistStrength)
    atmosphere.update(dt, camera)

    // 6. Particles — weather-driven rain + snow, plus biome flavor.
    biomeParticles.update(dt)
    rainSystem.setIntensity(profile.rainStrength)
    rainSystem.update(dt)
    snowSystem.setIntensity(profile.snowStrength)
    snowSystem.update(dt)
    // Wind affects particles.
    for (const s of biomeParticles.systems) {
      s.setWind(profile.wind)
    }
    rainSystem.setWind(new THREE.Vector3(profile.wind.x * 1.4, -2.0, profile.wind.z * 1.4))
    snowSystem.setWind(profile.wind)

    // 7. Post-processing (if enabled).
    if (postfx) postfx.render(dt)
  }

  let currentBiome: BiomeKind = initialBiome
  function setBiome(b: BiomeKind) {
    if (b === currentBiome) return
    currentBiome = b
    atmosphere.setBiome(b)
    biomeParticles.setBiome(b)
    weather.setBiome(b)
    // In snow biome, gently ramp up snow even at "clear" weather.
    if (b === 'snow' && weather.getState() === 'clear') {
      snowSystem.setVisible(true)
      snowSystem.setIntensity(0.4)
    } else if (weather.getState() === 'clear') {
      snowSystem.setVisible(false)
    }
  }

  function setWeather(state: WeatherState, transitionSeconds = 40) {
    weather.setState(state, transitionSeconds)
    // Particle visibility will be driven by the weather profile each frame.
    if (state === 'rain' || state === 'storm' || state === 'tribulation') {
      rainSystem.setVisible(true)
    }
    if (state === 'snow' || state === 'storm') {
      snowSystem.setVisible(true)
    }
  }

  function getWeather(): WeatherState {
    return weather.getState()
  }

  function triggerTribulation(durationSeconds: number) {
    weather.triggerTribulation(durationSeconds)
  }

  function setPlayerTarget(target: THREE.Object3D | null) {
    lighting.setFollowTarget(target)
    if (target) {
      _playerPos.copy(target.position)
      // Re-center particle systems on the player so they always wrap around
      // the cultivator rather than the spawn origin.
      biomeParticles.systems.forEach((s) => s.setOrigin(_playerPos))
      rainSystem.setOrigin(_playerPos)
      snowSystem.setOrigin(_playerPos)
    }
  }

  function setPerfTier(tier: 'low' | 'medium' | 'high' | 'ultra') {
    if (!postfx) return
    postfx.setEnabled('ssao', tier !== 'low')
    postfx.setEnabled('godRays', tier !== 'low')
    postfx.setEnabled('chromatic', tier !== 'low')
    postfx.setEnabled('dof', tier === 'ultra')
    postfx.setEnabled('grain', tier !== 'low')
    postfx.setBloomIntensity(tier === 'low' ? 0.7 : 1.0)
  }

  function dispose() {
    sky.dispose()
    clouds.dispose()
    atmosphere.dispose()
    biomeParticles.dispose()
    rainSystem.dispose()
    snowSystem.dispose()
    lighting.dispose()
    if (postfx) postfx.dispose()
  }

  return {
    sky,
    clouds,
    atmosphere,
    weather,
    lighting,
    postfx: postfx as unknown as PostProcessingHandle,
    particles: {
      biome: biomeParticles,
      rain: rainSystem,
      snow: snowSystem,
    },
    update,
    setWeather,
    getWeather,
    setBiome,
    triggerTribulation,
    setPlayerTarget,
    setPerfTier,
    dispose,
  }
}

export type { ParticlePreset, WeatherState, BiomeKind }
export { DAY_LENGTH_SECONDS }
