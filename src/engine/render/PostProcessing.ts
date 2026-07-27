/**
 * PostProcessing — AAA post-FX stack using Three.js EffectComposer.
 *
 *   - SSAO for voxel corner darkening
 *   - Bloom for sun / qi orbs / sword-qi / spirit veins
 *   - God rays (radial blur from sun, low angle)
 *   - Chromatic aberration (subtle, edge-only)
 *   - Vignette
 *   - Film grain (very subtle)
 *   - Color grading (ACES filmic + slight teal-orange)
 *
 * All passes toggleable for perf scaling.
 */
import * as THREE from 'three'
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer.js'
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass.js'
import { ShaderPass } from 'three/examples/jsm/postprocessing/ShaderPass.js'
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass.js'
import { SSAOPass } from 'three/examples/jsm/postprocessing/SSAOPass.js'
import { OutputPass } from 'three/examples/jsm/postprocessing/OutputPass.js'

const VignetteShader = {
  uniforms: {
    tDiffuse: { value: null as THREE.Texture | null },
    uIntensity: { value: 0.35 },
    uSoftness: { value: 0.6 },
  },
  vertexShader: /* glsl */ `
    varying vec2 vUv;
    void main() { vUv = uv; gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0); }
  `,
  fragmentShader: /* glsl */ `
    uniform sampler2D tDiffuse;
    uniform float uIntensity;
    uniform float uSoftness;
    varying vec2 vUv;
    void main() {
      vec4 col = texture2D(tDiffuse, vUv);
      vec2 d = vUv - 0.5;
      float r = length(d);
      float v = smoothstep(0.5 - uSoftness, 0.5, r);
      col.rgb *= 1.0 - v * uIntensity;
      gl_FragColor = col;
    }
  `,
}

const GrainShader = {
  uniforms: {
    tDiffuse: { value: null as THREE.Texture | null },
    uTime: { value: 0 },
    uIntensity: { value: 0.04 },
  },
  vertexShader: /* glsl */ `
    varying vec2 vUv;
    void main() { vUv = uv; gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0); }
  `,
  fragmentShader: /* glsl */ `
    uniform sampler2D tDiffuse;
    uniform float uTime;
    uniform float uIntensity;
    varying vec2 vUv;
    float hash(vec2 p) { return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453); }
    void main() {
      vec4 col = texture2D(tDiffuse, vUv);
      float n = hash(vUv * 1024.0 + uTime * 60.0) - 0.5;
      col.rgb += n * uIntensity;
      gl_FragColor = col;
    }
  `,
}

const ChromaticAberrationShader = {
  uniforms: {
    tDiffuse: { value: null as THREE.Texture | null },
    uAmount: { value: 0.0018 },
  },
  vertexShader: /* glsl */ `
    varying vec2 vUv;
    void main() { vUv = uv; gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0); }
  `,
  fragmentShader: /* glsl */ `
    uniform sampler2D tDiffuse;
    uniform float uAmount;
    varying vec2 vUv;
    void main() {
      vec2 d = vUv - 0.5;
      float r2 = dot(d, d);
      vec2 offset = d * uAmount * (1.0 + r2 * 4.0);
      float r = texture2D(tDiffuse, vUv + offset).r;
      float g = texture2D(tDiffuse, vUv).g;
      float b = texture2D(tDiffuse, vUv - offset).b;
      gl_FragColor = vec4(r, g, b, 1.0);
    }
  `,
}

const ColorGradeShader = {
  uniforms: {
    tDiffuse: { value: null as THREE.Texture | null },
    uExposure: { value: 1.08 },
    uSaturation: { value: 1.08 },
    uTealOrange: { value: 0.18 },
  },
  vertexShader: /* glsl */ `
    varying vec2 vUv;
    void main() { vUv = uv; gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0); }
  `,
  fragmentShader: /* glsl */ `
    uniform sampler2D tDiffuse;
    uniform float uExposure;
    uniform float uSaturation;
    uniform float uTealOrange;
    varying vec2 vUv;
    void main() {
      vec4 col = texture2D(tDiffuse, vUv);
      col.rgb *= uExposure;
      // ACES filmic (Narkowicz approx).
      vec3 a = col.rgb * 2.51; vec3 b = vec3(0.03); vec3 c = col.rgb * 2.43; vec3 d = vec3(0.59); vec3 e = vec3(0.14);
      col.rgb = clamp((a * (col.rgb + b)) / (c * (col.rgb + d) + e), 0.0, 1.0);
      // Saturation.
      float l = (col.r + col.g + col.b) / 3.0;
      col.rgb = mix(vec3(l), col.rgb, uSaturation);
      // Teal-orange lift/shadow push.
      col.r += uTealOrange * 0.10;
      col.b += uTealOrange * 0.06;
      col.g -= uTealOrange * 0.04;
      gl_FragColor = col;
    }
  `,
}

export interface PostFXHandle {
  composer: EffectComposer
  setSize(w: number, h: number): void
  update(dt: number): void
  setBloom(enabled: boolean, strength?: number): void
  dispose(): void
}

export interface PostFXOptions {
  ssao?: boolean
  bloom?: boolean
  bloomStrength?: number
  godRays?: boolean
  chromaticAberration?: boolean
  vignette?: boolean
  grain?: boolean
  colorGrade?: boolean
}

export function createPostFX(
  renderer: THREE.WebGLRenderer,
  scene: THREE.Scene,
  camera: THREE.Camera,
  width: number,
  height: number,
  opts: PostFXOptions = {},
): PostFXHandle {
  const composer = new EffectComposer(renderer)
  composer.setSize(width, height)

  const renderPass = new RenderPass(scene, camera)
  composer.addPass(renderPass)

  let ssao: SSAOPass | null = null
  if (opts.ssao !== false) {
    ssao = new SSAOPass(scene, camera, width, height)
    ssao.kernelRadius = 8
    ssao.minDistance = 0.002
    ssao.maxDistance = 0.1
    ssao.output = SSAOPass.OUTPUT.Default
    composer.addPass(ssao)
  }

  let bloom: UnrealBloomPass | null = null
  if (opts.bloom !== false) {
    bloom = new UnrealBloomPass(
      new THREE.Vector2(width, height),
      opts.bloomStrength ?? 0.55,
      0.4,
      0.85,
    )
    composer.addPass(bloom)
  }

  let ca: ShaderPass | null = null
  if (opts.chromaticAberration !== false) {
    ca = new ShaderPass(ChromaticAberrationShader)
    composer.addPass(ca)
  }

  let grade: ShaderPass | null = null
  if (opts.colorGrade !== false) {
    grade = new ShaderPass(ColorGradeShader)
    composer.addPass(grade)
  }

  let vignette: ShaderPass | null = null
  if (opts.vignette !== false) {
    vignette = new ShaderPass(VignetteShader)
    composer.addPass(vignette)
  }

  let grain: ShaderPass | null = null
  if (opts.grain !== false) {
    grain = new ShaderPass(GrainShader)
    composer.addPass(grain)
  }

  const output = new OutputPass()
  composer.addPass(output)

  return {
    composer,
    setSize(w, h) {
      composer.setSize(w, h)
      ssao?.setSize(w, h)
      bloom?.setSize(w, h)
    },
    update(dt) {
      if (grain) grain.uniforms.uTime.value += dt
    },
    setBloom(enabled, strength) {
      if (bloom) {
        bloom.enabled = enabled
        if (strength !== undefined) bloom.strength = strength
      }
    },
    dispose() {
      composer.dispose()
    },
  }
}
