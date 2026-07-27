/**
 * WaterRenderer — AAA water surface for lakes / Sea of Devils / coastlines.
 *
 * Uses a custom Gerstner-wave vertex shader (see water.glsl.ts) for
 * displacement and a fragment shader that mixes planar reflection,
 * Fresnel-driven sky reflection, refraction tint by depth, sun glint,
 * foam at wave crests + shorelines, and biome-aware color tint:
 *   - default: jade-green (deep) → clear (shallow)
 *   - Sea of Devils: subtle demonic-red depth tint (uDemonicMix)
 *
 * Reflections can be provided by an external ReflectorRT (caller can call
 * setReflectionTexture) or fall back to a procedural sky-tint approximation.
 *
 * NO canon chapter citations.
 */

import * as THREE from 'three'
import { WATER_VERTEX_SHADER, WATER_FRAGMENT_SHADER } from './shaders/water.glsl'

export interface WaterOptions {
  size?: number            // plane size in meters (square)
  segments?: number
  waveHeight?: number
  waveScale?: number
  demonicMix?: number      // 0..1 — Sea of Devils red tint
  skyColor?: THREE.ColorRepresentation
}

export interface WaterHandle {
  mesh: THREE.Mesh
  setSunDirection(d: THREE.Vector3): void
  setSunColor(c: THREE.Color): void
  setSkyColor(c: THREE.Color): void
  setShallowColor(c: THREE.Color): void
  setDeepColor(c: THREE.Color): void
  setDemonicMix(m: number): void
  setReflectionTexture(t: THREE.Texture | null): void
  setWaveHeight(h: number): void
  setWaveScale(s: number): void
  update(dt: number): void
  dispose(): void
}

export function createWaterRenderer(
  scene: THREE.Scene,
  options: WaterOptions = {},
): WaterHandle {
  const size = options.size ?? 2000
  const segments = options.segments ?? 200
  const waveHeight = options.waveHeight ?? 1.0
  const waveScale = options.waveScale ?? 1.0
  const demonicMix = options.demonicMix ?? 0
  const skyColor = new THREE.Color(options.skyColor ?? 0x88aacc)

  const geo = new THREE.PlaneGeometry(size, size, segments, segments)
  geo.rotateX(-Math.PI / 2)

  const uniforms = {
    uTime: { value: 0 },
    uCameraPos: { value: new THREE.Vector3() },
    uSunDirection: { value: new THREE.Vector3(0, 1, 0) },
    uSunColor: { value: new THREE.Color(1.0, 0.92, 0.78) },
    uShallowColor: { value: new THREE.Color(0.30, 0.65, 0.55) }, // clear jade shallows
    uDeepColor: { value: new THREE.Color(0.04, 0.16, 0.18) },   // dark jade deep
    uDemonicTint: { value: new THREE.Color(0.55, 0.10, 0.08) }, // demonic red depth
    uDemonicMix: { value: demonicMix },
    uFoamThreshold: { value: 0.55 },
    uSkyMix: { value: 0.8 },
    uSkyColor: { value: skyColor },
    uReflection: { value: null as THREE.Texture | null },
    uHasReflection: { value: 0 },
    uResolution: { value: new THREE.Vector2(1, 1) },
    uWaveScale: { value: waveScale },
    uWaveHeight: { value: waveHeight },
  }

  const mat = new THREE.ShaderMaterial({
    name: 'Water',
    uniforms,
    vertexShader: WATER_VERTEX_SHADER,
    fragmentShader: WATER_FRAGMENT_SHADER,
    transparent: true,
    side: THREE.DoubleSide,
    depthWrite: false,
  })

  const mesh = new THREE.Mesh(geo, mat)
  mesh.receiveShadow = true
  mesh.renderOrder = 100
  scene.add(mesh)

  return {
    mesh,
    setSunDirection(d) {
      uniforms.uSunDirection.value.copy(d).normalize()
    },
    setSunColor(c) {
      uniforms.uSunColor.value.copy(c)
    },
    setSkyColor(c) {
      uniforms.uSkyColor.value.copy(c)
    },
    setShallowColor(c) {
      uniforms.uShallowColor.value.copy(c)
    },
    setDeepColor(c) {
      uniforms.uDeepColor.value.copy(c)
    },
    setDemonicMix(m) {
      uniforms.uDemonicMix.value = m
    },
    setReflectionTexture(t) {
      uniforms.uReflection.value = t
      uniforms.uHasReflection.value = t ? 1 : 0
    },
    setWaveHeight(h) {
      uniforms.uWaveHeight.value = h
    },
    setWaveScale(s) {
      uniforms.uWaveScale.value = s
    },
    update(dt) {
      uniforms.uTime.value += dt
    },
    dispose() {
      geo.dispose()
      mat.dispose()
      scene.remove(mesh)
    },
  }
}
