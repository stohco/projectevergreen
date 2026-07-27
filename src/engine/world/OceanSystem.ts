/**
 * OceanSystem — Gerstner wave ocean with proper water physics.
 *
 * Planet Suzaku is ocean-dominated (CANON_RI_COMPLETE_WORLD.md L14).
 * This system renders a vast ocean with:
 *   - Gerstner waves (sum of sine waves with direction, amplitude, wavelength)
 *   - Vertex displacement on a high-res plane mesh
 *   - Fresnel-based reflection/refraction
 *   - Depth-based color gradient (shallow = clear, deep = dark)
 *   - Foam at wave crests
 *   - No transparency (opaque, depth-sorted correctly)
 *
 * Math: Gerstner waves displace vertices both vertically AND horizontally:
 *   x' = x + Σ Qᵢ · Aᵢ · Dᵢ.x · cos(ωᵢ · Dᵢ · x + φᵢ · t)
 *   z' = z + Σ Qᵢ · Aᵢ · Dᵢ.z · cos(ωᵢ · Dᵢ · x + φᵢ · t)
 *   y' = Σ Aᵢ · sin(ωᵢ · Dᵢ · x + φᵢ · t)
 *
 * Where:
 *   Aᵢ = amplitude of wave i
 *   Dᵢ = direction (normalized 2D vector)
 *   ωᵢ = frequency = 2π / wavelength
 *   φᵢ = phase = speed × ωᵢ
 *   Qᵢ = steepness (0 = flat sine, 1 = sharp peak)
 *
 * Multiple waves at different frequencies/directions create realistic ocean motion.
 */

import * as THREE from 'three'

const OCEAN_VERT = /* glsl */ `
  uniform float uTime;
  uniform float uWaveAmp1;
  uniform float uWaveAmp2;
  uniform float uWaveAmp3;
  uniform vec2 uWaveDir1;
  uniform vec2 uWaveDir2;
  uniform vec2 uWaveDir3;
  uniform float uWaveLen1;
  uniform float uWaveLen2;
  uniform float uWaveLen3;
  uniform float uWaveSpeed;
  uniform float uWaveSteepness;

  varying vec3 vWorldPos;
  varying vec3 vNormal;
  varying float vWaveHeight;
  varying float vFoam;

  // Gerstner wave displacement.
  vec3 gerstner(vec2 pos, float t, float amp, vec2 dir, float wavelength, float steepness, out vec3 normal) {
    float omega = 6.28318 / wavelength;
    float phase = omega * dot(dir, pos) + uWaveSpeed * omega * t;
    float c = cos(phase);
    float s = sin(phase);
    float Q = steepness / (omega * amp);

    vec3 displacement;
    displacement.x = Q * amp * dir.x * c;
    displacement.z = Q * amp * dir.y * c;
    displacement.y = amp * s;

    // Normal (derivative of Gerstner wave).
    float WA = omega * amp;
    normal.x = -dir.x * WA * c;
    normal.z = -dir.y * WA * c;
    normal.y = 1.0 - steepness * WA * s;

    return displacement;
  }

  void main() {
    vec3 pos = position;
    vec3 normal1, normal2, normal3;

    // Sum 3 Gerstner waves at different frequencies + directions.
    vec3 d1 = gerstner(pos.xz, uTime, uWaveAmp1, uWaveDir1, uWaveLen1, uWaveSteepness, normal1);
    vec3 d2 = gerstner(pos.xz, uTime, uWaveAmp2, uWaveDir2, uWaveLen2, uWaveSteepness * 0.7, normal2);
    vec3 d3 = gerstner(pos.xz, uTime, uWaveAmp3, uWaveDir3, uWaveLen3, uWaveSteepness * 0.5, normal3);

    pos += d1 + d2 + d3;
    vNormal = normalize(normal1 + normal2 + normal3);
    vWaveHeight = pos.y;
    vFoam = smoothstep(0.8, 1.5, pos.y); // foam at crests

    vec4 worldPos = modelMatrix * vec4(pos, 1.0);
    vWorldPos = worldPos.xyz;
    gl_Position = projectionMatrix * viewMatrix * worldPos;
  }
`

const OCEAN_FRAG = /* glsl */ `
  uniform vec3 uShallowColor;
  uniform vec3 uDeepColor;
  uniform vec3 uSkyColor;
  uniform float uTime;

  varying vec3 vWorldPos;
  varying vec3 vNormal;
  varying float vWaveHeight;
  varying float vFoam;

  void main() {
    vec3 N = normalize(vNormal);
    vec3 V = normalize(cameraPosition - vWorldPos);

    // Fresnel: reflect more at grazing angles.
    float fresnel = pow(1.0 - max(dot(N, V), 0.0), 3.0);
    fresnel = mix(0.1, 1.0, fresnel);

    // Depth-based color: shallow = clear blue, deep = dark navy.
    float depthFactor = smoothstep(-2.0, 5.0, vWaveHeight);
    vec3 waterColor = mix(uDeepColor, uShallowColor, depthFactor);

    // Sky reflection (simplified).
    vec3 reflectColor = uSkyColor;

    // Mix water + reflection by fresnel.
    vec3 color = mix(waterColor, reflectColor, fresnel * 0.4);

    // Add foam at wave crests.
    color = mix(color, vec3(0.95, 0.97, 1.0), vFoam * 0.6);

    // Subtle specular highlight (sun glint).
    vec3 sunDir = normalize(vec3(0.5, 0.8, 0.3));
    vec3 H = normalize(sunDir + V);
    float spec = pow(max(dot(N, H), 0.0), 80.0);
    color += vec3(1.0, 0.95, 0.8) * spec * 0.5;

    gl_FragColor = vec4(color, 1.0); // opaque, no transparency
  }
`

export interface OceanHandle {
  mesh: THREE.Mesh
  update(dt: number): void
  setWaveParameters(opts: Partial<WaveParameters>): void
  dispose(): void
}

export interface WaveParameters {
  amplitude1: number
  amplitude2: number
  amplitude3: number
  direction1: [number, number]
  direction2: [number, number]
  direction3: [number, number]
  wavelength1: number
  wavelength2: number
  wavelength3: number
  speed: number
  steepness: number
}

/**
 * Create a Gerstner-wave ocean mesh.
 *
 * @param size — world size of the ocean plane (meters)
 * @param segments — grid resolution (higher = more detailed waves)
 */
export function createOcean(
  size: number,
  segments: number,
  params?: Partial<WaveParameters>,
): OceanHandle {
  const defaults: WaveParameters = {
    amplitude1: 0.8,
    amplitude2: 0.4,
    amplitude3: 0.2,
    direction1: [1.0, 0.3],
    direction2: [0.7, 1.0],
    direction3: [-0.5, 0.8],
    wavelength1: 25.0,
    wavelength2: 12.0,
    wavelength3: 6.0,
    speed: 1.0,
    steepness: 0.6,
  }
  const p = { ...defaults, ...params }

  const geo = new THREE.PlaneGeometry(size, size, segments, segments)
  geo.rotateX(-Math.PI / 2)

  const mat = new THREE.ShaderMaterial({
    vertexShader: OCEAN_VERT,
    fragmentShader: OCEAN_FRAG,
    uniforms: {
      uTime: { value: 0 },
      uWaveAmp1: { value: p.amplitude1 },
      uWaveAmp2: { value: p.amplitude2 },
      uWaveAmp3: { value: p.amplitude3 },
      uWaveDir1: { value: new THREE.Vector2(p.direction1[0], p.direction1[1]).normalize() },
      uWaveDir2: { value: new THREE.Vector2(p.direction2[0], p.direction2[1]).normalize() },
      uWaveDir3: { value: new THREE.Vector2(p.direction3[0], p.direction3[1]).normalize() },
      uWaveLen1: { value: p.wavelength1 },
      uWaveLen2: { value: p.wavelength2 },
      uWaveLen3: { value: p.wavelength3 },
      uWaveSpeed: { value: p.speed },
      uWaveSteepness: { value: p.steepness },
      uShallowColor: { value: new THREE.Color(0x3a8ab8) },
      uDeepColor: { value: new THREE.Color(0x0a2a4a) },
      uSkyColor: { value: new THREE.Color(0xbcd6ff) },
    },
    side: THREE.DoubleSide,
    transparent: false, // OPAQUE — no depth-sorting artifacts
  })

  const mesh = new THREE.Mesh(geo, mat)
  mesh.name = 'ocean'
  mesh.receiveShadow = true
  mesh.frustumCulled = false // ocean is always visible

  let time = 0
  return {
    mesh,
    update(dt: number) {
      time += dt
      mat.uniforms.uTime.value = time
    },
    setWaveParameters(opts: Partial<WaveParameters>) {
      if (opts.amplitude1 !== undefined) mat.uniforms.uWaveAmp1.value = opts.amplitude1
      if (opts.amplitude2 !== undefined) mat.uniforms.uWaveAmp2.value = opts.amplitude2
      if (opts.amplitude3 !== undefined) mat.uniforms.uWaveAmp3.value = opts.amplitude3
      if (opts.speed !== undefined) mat.uniforms.uWaveSpeed.value = opts.speed
      if (opts.steepness !== undefined) mat.uniforms.uWaveSteepness.value = opts.steepness
      if (opts.direction1) mat.uniforms.uWaveDir1.value.set(opts.direction1[0], opts.direction1[1]).normalize()
      if (opts.direction2) mat.uniforms.uWaveDir2.value.set(opts.direction2[0], opts.direction2[1]).normalize()
      if (opts.direction3) mat.uniforms.uWaveDir3.value.set(opts.direction3[0], opts.direction3[1]).normalize()
    },
    dispose() {
      geo.dispose()
      mat.dispose()
    },
  }
}

/**
 * Query the ocean wave height at a world (x, z) position at time t.
 * Used for: buoyancy (boats, swimming creatures), physics (floating objects).
 *
 * h(x, z, t) = Σ Aᵢ · sin(ωᵢ · (Dᵢ.x · x + Dᵢ.z · z) + φᵢ · t)
 */
export function sampleWaveHeight(
  x: number,
  z: number,
  t: number,
  params?: Partial<WaveParameters>,
): number {
  const p = { ...{
    amplitude1: 0.8, amplitude2: 0.4, amplitude3: 0.2,
    direction1: [1.0, 0.3] as [number, number],
    direction2: [0.7, 1.0] as [number, number],
    direction3: [-0.5, 0.8] as [number, number],
    wavelength1: 25.0, wavelength2: 12.0, wavelength3: 6.0,
    speed: 1.0, steepness: 0.6,
  }, ...params }

  const waves = [
    { A: p.amplitude1, D: p.direction1, L: p.wavelength1 },
    { A: p.amplitude2, D: p.direction2, L: p.wavelength2 },
    { A: p.amplitude3, D: p.direction3, L: p.wavelength3 },
  ]

  let h = 0
  for (const w of waves) {
    const len = Math.sqrt(w.D[0] ** 2 + w.D[1] ** 2)
    const dx = w.D[0] / len
    const dz = w.D[1] / len
    const omega = (2 * Math.PI) / w.L
    const phase = omega * (dx * x + dz * z) + p.speed * omega * t
    h += w.A * Math.sin(phase)
  }
  return h
}
