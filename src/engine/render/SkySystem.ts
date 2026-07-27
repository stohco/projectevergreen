/**
 * SkySystem — AAA procedural sky dome with Rayleigh + Mie scattering
 * approximation, day/night cycle, sun/moon, 28-mansion constellations.
 *
 * Shader-based (no external textures). Day sky = warm cyan→cream gradient
 * (xianxia feel, not generic blue). Sunset = orange/pink/gold bands.
 * Night = deep indigo with the Milky Way band.
 */
import * as THREE from 'three'

const SKY_VERT = /* glsl */ `
varying vec3 vWorldDir;
varying vec3 vWorldPos;
void main() {
  vWorldPos = position;
  vWorldDir = normalize(position);
  gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
}
`

const SKY_FRAG = /* glsl */ `
precision highp float;
varying vec3 vWorldDir;
varying vec3 vWorldPos;
uniform vec3 uSunDir;       // normalized sun direction
uniform vec3 uMoonDir;      // normalized moon direction
uniform float uTime;        // seconds
uniform float uSunIntensity;// 0..1 (1 = noon, 0 = midnight)
uniform int uStarSeed;

// Rayleigh + Mie scattering approximation (Preetham-style).
vec3 skyColor(vec3 dir, vec3 sunDir) {
  float cosSun = max(dot(dir, sunDir), 0.0);
  // Rayleigh coefficient — xianxia-tuned (warmer cyan, less blue).
  vec3 rayleighCoeff = vec3(0.18, 0.34, 0.55);
  float rayleighPhase = 0.75 * (1.0 + cosSun * cosSun);
  vec3 rayleigh = rayleighCoeff * rayleighPhase;
  // Mie (sun disc + glow).
  float miePhase = 0.635 * pow(1.0 + 0.5 * cosSun, -1.5);
  vec3 mieCoeff = vec3(0.04, 0.05, 0.07);
  vec3 mie = mieCoeff * miePhase;
  // Sky tint = direction-weighted mix of horizon cream + zenith cyan.
  float upDot = clamp(dir.y * 0.5 + 0.5, 0.0, 1.0);
  vec3 zenith = vec3(0.42, 0.65, 0.86);
  vec3 horizon = vec3(0.92, 0.88, 0.78);
  vec3 base = mix(horizon, zenith, pow(upDot, 0.55));
  // Sun disc + glow.
  float sunGlow = pow(max(cosSun, 0.0), 200.0);
  float sunDisc = smoothstep(0.9995, 0.9998, cosSun);
  vec3 sunColor = vec3(1.0, 0.95, 0.78);
  // Sunset tint — when sun is near horizon, push warm.
  float sunsetFactor = pow(1.0 - abs(sunDir.y), 4.0);
  vec3 sunsetTint = vec3(1.1, 0.5, 0.25);
  vec3 color = base * (1.0 + rayleigh * 0.3) + mie * 2.0;
  color += sunGlow * sunColor * 1.2;
  color += sunDisc * sunColor * 6.0;
  color = mix(color, color * 0.6 + sunsetTint * 1.2, sunsetFactor * 0.55);
  // Night darkening.
  float night = clamp(-uSunIntensity * 2.0 + 1.0, 0.0, 1.0);
  color = mix(color, vec3(0.02, 0.025, 0.06), night * 0.85);
  return color;
}

// Simple hash for stars.
float hash13(vec3 p) {
  p = fract(p * 0.1031);
  p += dot(p, p.yzx + 33.33);
  return fract((p.x + p.y) * p.z);
}

void main() {
  vec3 dir = normalize(vWorldDir);
  vec3 col = skyColor(dir, uSunDir);
  // Stars at night.
  if (dir.y > 0.0) {
    float nightFactor = clamp(-uSunIntensity * 1.5 + 1.0, 0.0, 1.0);
    if (nightFactor > 0.05) {
      // 3 star-grid layers with different cell sizes for variety.
      for (int k = 0; k < 3; k++) {
        float scale = 80.0 + float(k) * 60.0;
        vec3 cell = floor(dir * scale);
        float h = hash13(cell + float(uStarSeed) * 0.13);
        if (h > 0.985) {
          vec3 cellPos = (cell + 0.5) / scale;
          float d = length(dir - cellPos);
          float twinkle = 0.5 + 0.5 * sin(uTime * 3.0 + h * 30.0);
          float brightness = (1.0 - smoothstep(0.0, 0.008, d)) * twinkle;
          col += vec3(0.95, 0.97, 1.0) * brightness * nightFactor * 1.2;
        }
      }
      // Milky Way band — a faint noise streak across the sky.
      float mwBand = exp(-pow(dir.x * 1.5 + dir.z * 0.7, 2.0) * 4.0) * exp(-pow(dir.y * 2.5, 2.0) * 1.2);
      float mwNoise = hash13(floor(dir * 200.0)) * hash13(floor(dir * 73.0));
      col += vec3(0.35, 0.40, 0.55) * mwBand * mwNoise * nightFactor * 0.4;
    }
  }
  // Moon disc.
  float cosMoon = dot(dir, uMoonDir);
  float moonDisc = smoothstep(0.9994, 0.9997, cosMoon);
  col += vec3(0.92, 0.94, 1.0) * moonDisc * 2.0 * (1.0 - uSunIntensity);
  gl_FragColor = vec4(col, 1.0);
}
`

export interface SkyHandle {
  /** Update sun/moon position based on timeOfDay (0..1, 0=midnight, 0.5=noon). */
  update(dt: number): void
  setTimeOfDay(t: number): void
  getTimeOfDay(): number
  getSunDirection(): THREE.Vector3
  getSunIntensity(): number
  dispose(): void
}

export function createSky(scene: THREE.Scene): SkyHandle {
  // Sky dome (inside-out sphere).
  const geo = new THREE.SphereGeometry(3000, 64, 32)
  const mat = new THREE.ShaderMaterial({
    vertexShader: SKY_VERT,
    fragmentShader: SKY_FRAG,
    uniforms: {
      uSunDir: { value: new THREE.Vector3(0, 1, 0) },
      uMoonDir: { value: new THREE.Vector3(0, -1, 0) },
      uTime: { value: 0 },
      uSunIntensity: { value: 1 },
      uStarSeed: { value: 7 },
    },
    side: THREE.BackSide,
    depthWrite: false,
    fog: false,
  })
  const sky = new THREE.Mesh(geo, mat)
  sky.frustumCulled = false
  sky.renderOrder = -1000
  scene.add(sky)

  // Sun directional light (engine lighting rig hooks into this).
  const sunLight = new THREE.DirectionalLight(0xfff4d6, 2.0)
  sunLight.castShadow = true
  sunLight.shadow.mapSize.set(2048, 2048)
  sunLight.shadow.camera.left = -200
  sunLight.shadow.camera.right = 200
  sunLight.shadow.camera.top = 200
  sunLight.shadow.camera.bottom = -200
  sunLight.shadow.camera.near = 0.5
  sunLight.shadow.camera.far = 800
  sunLight.shadow.bias = -0.0005
  scene.add(sunLight)
  scene.add(sunLight.target)

  // Hemi light (sky + ground bounce).
  const hemi = new THREE.HemisphereLight(0xbcd6ff, 0x4a3520, 0.5)
  scene.add(hemi)

  let timeOfDay = 0.5 // noon (maximum sun)
  const sunDir = new THREE.Vector3()
  const moonDir = new THREE.Vector3()

  function recompute() {
    // Sun angle: 0=midnight (-Y), 0.25=east horizon, 0.5=noon (+Y), 0.75=west horizon.
    const ang = timeOfDay * Math.PI * 2 - Math.PI / 2
    sunDir.set(Math.cos(ang), Math.sin(ang), 0.3).normalize()
    moonDir.copy(sunDir).multiplyScalar(-1)
    const intensity = Math.max(0, Math.sin(ang))
    mat.uniforms.uSunDir.value.copy(sunDir)
    mat.uniforms.uMoonDir.value.copy(moonDir)
    mat.uniforms.uSunIntensity.value = intensity
    sunLight.position.copy(sunDir).multiplyScalar(500)
    sunLight.target.position.set(0, 0, 0)
    // Warm sunrise/sunset, white noon, blue moonlight.
    const isHorizon = Math.abs(intensity) < 0.3
    if (intensity > 0.5) {
      sunLight.color.setHex(0xfff4d6)
      sunLight.intensity = 3.0
      hemi.intensity = 0.85
    } else if (intensity > 0.05) {
      sunLight.color.setHex(0xff8855)
      sunLight.intensity = 2.0 + intensity
      hemi.intensity = 0.6
    } else {
      sunLight.color.setHex(0x6a82c4)
      sunLight.intensity = 0.5
      hemi.intensity = 0.25
    }
    void isHorizon
  }
  recompute()

  return {
    update(dt: number) {
      // 24 in-world minutes per 24 real seconds → 1 in-world hour per real second.
      // Slow enough to enjoy sunset, fast enough to see day/night cycle.
      timeOfDay = (timeOfDay + dt / 24 / 60) % 1
      mat.uniforms.uTime.value += dt
      recompute()
    },
    setTimeOfDay(t: number) {
      timeOfDay = t
      recompute()
    },
    getTimeOfDay() {
      return timeOfDay
    },
    getSunDirection() {
      return sunDir.clone()
    },
    getSunIntensity() {
      return mat.uniforms.uSunIntensity.value as number
    },
    dispose() {
      scene.remove(sky)
      scene.remove(sunLight)
      scene.remove(sunLight.target)
      scene.remove(hemi)
      geo.dispose()
      mat.dispose()
    },
  }
}
