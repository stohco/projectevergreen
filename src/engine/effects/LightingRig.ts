/**
 * LightingRig — cinematic light setup for the Er Gen Verse.
 *
 * Owns:
 *   - Sun directional light (warm 5500K daytime, 2200K sunset, dim 4100K
 *     moonlight at night). The SkySystem owns its own sun light; this rig
 *     accepts a reference to that light so it can color/position it.
 *   - Hemisphere light (sky color + ground bounce).
 *   - Player follow-light (subtle fill so cultivator is always visible).
 *   - Spirit vein point lights (jade-green point lights at each vein
 *     position, soft falloff). The integrator calls addSpiritVeinLight()
 *     for each vein once the world layer is materialized.
 *
 * Lightning flashes during storms are exposed via applyLightningFlash()
 * which momentarily boosts the ambient/hemi intensity.
 *
 * NO canon chapter citations.
 */

import * as THREE from 'three'

export interface SpiritVeinLight {
  id: string
  light: THREE.PointLight
  baseIntensity: number
  phase: number
}

export interface LightingRigHandle {
  group: THREE.Group
  hemi: THREE.HemisphereLight
  followLight: THREE.PointLight
  sunLight: THREE.DirectionalLight | null
  setSunLight(light: THREE.DirectionalLight | null): void
  setSunDirection(d: THREE.Vector3): void
  setSunColor(c: THREE.Color): void
  setSunIntensity(i: number): void
  setSkyColor(c: THREE.Color): void
  setGroundColor(c: THREE.Color): void
  setHemiIntensity(i: number): void
  setFollowTarget(t: THREE.Object3D | null): void
  applyLightningFlash(flash: number, color: THREE.Color): void
  addSpiritVeinLight(id: string, position: THREE.Vector3, opts?: {
    color?: THREE.ColorRepresentation
    intensity?: number
    distance?: number
  }): SpiritVeinLight
  removeSpiritVeinLight(id: string): void
  update(dt: number): void
  dispose(): void
}

export function createLightingRig(scene: THREE.Scene): LightingRigHandle {
  const group = new THREE.Group()
  group.name = 'LightingRig'
  scene.add(group)

  // Hemisphere light — sky + ground bounce.
  const hemi = new THREE.HemisphereLight(0xbcd6ff, 0x4a3520, 0.65)
  group.add(hemi)

  // Player follow light — soft warm fill.
  const followLight = new THREE.PointLight(0xfff0c4, 0.45, 60, 2.0)
  followLight.position.set(0, 12, 4)
  group.add(followLight)

  let sunLight: THREE.DirectionalLight | null = null
  let followTarget: THREE.Object3D | null = null
  const spiritVeinLights = new Map<string, SpiritVeinLight>()
  const veinGroup = new THREE.Group()
  veinGroup.name = 'SpiritVeinLights'
  group.add(veinGroup)

  // Lightning flash accumulators.
  let flashBoost = 0
  let baseHemiIntensity = 0.65
  const _flashColor = new THREE.Color(1, 1, 1)
  const _followPos = new THREE.Vector3()
  const _hemiBaseColor = new THREE.Color(0xbcd6ff)

  const handle: LightingRigHandle = {
    group,
    hemi,
    followLight,
    sunLight,
    setSunLight(light) {
      sunLight = light
      handle.sunLight = light
    },
    setSunDirection(d) {
      if (sunLight) {
        sunLight.position.copy(d).multiplyScalar(800)
        sunLight.target.position.set(0, 0, 0)
        sunLight.target.updateMatrixWorld()
      }
    },
    setSunColor(c) {
      if (sunLight) sunLight.color.copy(c)
    },
    setSunIntensity(i) {
      if (sunLight) sunLight.intensity = i
    },
    setSkyColor(c) {
      _hemiBaseColor.copy(c)
      if (flashBoost <= 0) hemi.color.copy(c)
    },
    setGroundColor(c) {
      hemi.groundColor.copy(c)
    },
    setHemiIntensity(i) {
      baseHemiIntensity = i
      if (flashBoost <= 0) hemi.intensity = i
    },
    setFollowTarget(t) {
      followTarget = t
    },
    applyLightningFlash(flash, color) {
      flashBoost = Math.max(flashBoost, flash)
      _flashColor.copy(color)
    },
    addSpiritVeinLight(id, position, opts = {}) {
      const existing = spiritVeinLights.get(id)
      if (existing) return existing
      const color = new THREE.Color(opts.color ?? 0x6fd98a)
      const intensity = opts.intensity ?? 1.6
      const distance = opts.distance ?? 80
      const light = new THREE.PointLight(color, intensity, distance, 2.0)
      light.position.copy(position)
      veinGroup.add(light)
      const vein: SpiritVeinLight = {
        id,
        light,
        baseIntensity: intensity,
        phase: Math.random() * Math.PI * 2,
      }
      spiritVeinLights.set(id, vein)
      return vein
    },
    removeSpiritVeinLight(id) {
      const vein = spiritVeinLights.get(id)
      if (!vein) return
      veinGroup.remove(vein.light)
      spiritVeinLights.delete(id)
    },
    update(dt) {
      // Follow light tracks target.
      if (followTarget) {
        _followPos.set(
          followTarget.position.x,
          followTarget.position.y + 10,
          followTarget.position.z + 4,
        )
        followLight.position.lerp(_followPos, Math.min(1, dt * 6))
      }
      // Spirit vein lights pulse.
      const t = performance.now() / 1000
      for (const vein of spiritVeinLights.values()) {
        vein.light.intensity =
          vein.baseIntensity * (0.65 + 0.35 * Math.sin(t * 1.6 + vein.phase))
      }
      // Decay lightning flash.
      if (flashBoost > 0) {
        hemi.color.lerp(_flashColor, 0.6)
        flashBoost = Math.max(0, flashBoost - dt * 4.0)
        hemi.intensity = baseHemiIntensity + flashBoost * 3.0
      } else {
        hemi.color.lerp(_hemiBaseColor, Math.min(1, dt * 2))
        hemi.intensity = baseHemiIntensity
      }
    },
    dispose() {
      for (const vein of spiritVeinLights.values()) {
        veinGroup.remove(vein.light)
      }
      spiritVeinLights.clear()
      scene.remove(group)
    },
  }

  return handle
}
