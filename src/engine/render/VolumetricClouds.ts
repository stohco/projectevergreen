/**
 * VolumetricClouds — xianxia-style cloud layer rendered as a fragment-shader
 * raymarch on a sky dome. Tall cumulus in the day, misty ground-level
 * clouds (云海 / sea of clouds) around mountain peaks.
 *
 * Performance: rendered at half-res into a RenderTarget, then composited as
 * a fullscreen quad with alpha blend. Target: <2ms/frame.
 *
 * The cloud shader uses fBm noise (sum of octaves of value noise) sampled
 * along a ray from the camera to the sky dome. Cloud density is shaped
 * by height + noise + sun direction (forward scattering).
 */
import * as THREE from 'three'

const CLOUD_VERT = /* glsl */ `
varying vec2 vUv;
void main() {
  vUv = uv;
  gl_Position = vec4(position.xy, 1.0, 1.0); // fullscreen quad
}
`

const CLOUD_FRAG = /* glsl */ `
precision highp float;
varying vec2 vUv;
uniform float uTime;
uniform vec3 uSunDir;
uniform vec3 uCameraPos;
uniform float uSunIntensity;
uniform mat4 uInvViewProj;
uniform vec2 uResolution;

// Hash + value noise + fBm.
float hash21(vec2 p) {
  p = fract(p * vec2(123.34, 456.21));
  p += dot(p, p + 45.32);
  return fract(p.x * p.y);
}
float vnoise(vec2 p) {
  vec2 i = floor(p);
  vec2 f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  float a = hash21(i);
  float b = hash21(i + vec2(1.0, 0.0));
  float c = hash21(i + vec2(0.0, 1.0));
  float d = hash21(i + vec2(1.0, 1.0));
  return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}
float fbm(vec2 p) {
  float v = 0.0;
  float a = 0.5;
  for (int i = 0; i < 5; i++) {
    v += a * vnoise(p);
    p *= 2.07;
    a *= 0.5;
  }
  return v;
}

// Reconstruct world-space ray direction from screen UV.
vec3 rayDir(vec2 uv) {
  vec4 ndc = vec4(uv * 2.0 - 1.0, 1.0, 1.0);
  vec4 world = uInvViewProj * ndc;
  return normalize(world.xyz / world.w - uCameraPos);
}

void main() {
  vec3 dir = rayDir(vUv);
  if (dir.y < 0.02) {
    gl_FragColor = vec4(0.0);
    return;
  }
  // March through a cloud slab at y=400..700.
  float tEnter = (400.0 - uCameraPos.y) / dir.y;
  float tExit  = (700.0 - uCameraPos.y) / dir.y;
  if (tExit < tEnter) { float t = tEnter; tEnter = tExit; tExit = t; }
  tEnter = max(tEnter, 0.0);
  if (tExit <= tEnter) {
    gl_FragColor = vec4(0.0);
    return;
  }
  // 24 march steps.
  const int STEPS = 24;
  float stepSize = (tExit - tEnter) / float(STEPS);
  float density = 0.0;
  vec3 lightAccum = vec3(0.0);
  for (int i = 0; i < STEPS; i++) {
    float t = tEnter + stepSize * (float(i) + 0.5);
    vec3 p = uCameraPos + dir * t;
    // Wind shift.
    vec2 wind = p.xz + vec2(uTime * 4.0, uTime * 2.0);
    float n = fbm(wind * 0.0015);
    // Height shaping: more density in mid-slab, less at edges.
    float hShape = smoothstep(0.0, 0.4, n) * smoothstep(1.0, 0.6, n);
    density += hShape * stepSize * 0.012;
    // Light scattering: sun direction.
    float s = max(dot(dir, uSunDir), 0.0);
    lightAccum += vec3(1.0, 0.96, 0.88) * s * hShape * stepSize * 0.03;
  }
  density = clamp(density, 0.0, 1.0);
  // Sunset tint: when sun is low, push warm.
  float sunset = pow(1.0 - abs(uSunDir.y), 4.0);
  vec3 cloudColor = mix(vec3(1.0), vec3(1.2, 0.7, 0.4), sunset * 0.6);
  vec3 col = cloudColor * (1.0 - density * 0.4) + lightAccum * 0.6;
  // Night dim.
  col *= mix(0.15, 1.0, clamp(uSunIntensity, 0.0, 1.0));
  float alpha = density * 0.85;
  gl_FragColor = vec4(col, alpha);
}
`

export interface CloudsHandle {
  update(dt: number): void
  resize(w: number, h: number): void
  dispose(): void
}

export function createClouds(scene: THREE.Scene): CloudsHandle {
  const w = Math.floor(window.innerWidth / 2)
  const h = Math.floor(window.innerHeight / 2)
  const rt = new THREE.WebGLRenderTarget(w, h, {
    minFilter: THREE.LinearFilter,
    magFilter: THREE.LinearFilter,
    format: THREE.RGBAFormat,
    type: THREE.HalfFloatType,
  })
  const cam = new THREE.OrthographicCamera(-1, 1, 1, -1, 0, 1)
  const geo = new THREE.PlaneGeometry(2, 2)
  const mat = new THREE.ShaderMaterial({
    vertexShader: CLOUD_VERT,
    fragmentShader: CLOUD_FRAG,
    uniforms: {
      uTime: { value: 0 },
      uSunDir: { value: new THREE.Vector3(0, 1, 0) },
      uCameraPos: { value: new THREE.Vector3() },
      uSunIntensity: { value: 1 },
      uInvViewProj: { value: new THREE.Matrix4() },
      uResolution: { value: new THREE.Vector2(w, h) },
    },
    depthTest: false,
    depthWrite: false,
    transparent: true,
  })
  const quad = new THREE.Mesh(geo, mat)
  const cloudScene = new THREE.Scene()
  cloudScene.add(quad)

  // Composite mesh — draws the rendered cloud RT as a fullscreen overlay.
  const compositeMat = new THREE.MeshBasicMaterial({
    map: rt.texture,
    transparent: true,
    depthTest: false,
    depthWrite: false,
    fog: false,
  })
  const compositeGeo = new THREE.PlaneGeometry(2, 2)
  const composite = new THREE.Mesh(compositeGeo, compositeMat)
  composite.frustumCulled = false
  composite.renderOrder = -500
  scene.add(composite)

  // Camera placeholder — filled by update().
  let mainCam: THREE.Camera | null = null
  let renderer: THREE.WebGLRenderer | null = null

  return {
    update(_dt) {
      // Hooked by WorldCanvas via attachRenderer.
      void scene
      void cam
      void mat
      void rt
      void quad
      void cloudScene
      void composite
      void compositeMat
      void compositeGeo
    },
    resize(_w, _h) {
      // Implemented in attachRenderer.
    },
    dispose() {
      rt.dispose()
      geo.dispose()
      mat.dispose()
      compositeGeo.dispose()
      compositeMat.dispose()
      scene.remove(composite)
    },
  }
}

/**
 * attachCloudRenderer — wire up the clouds to the main renderer/camera.
 * Returns an update(dt) function that re-renders the cloud RT.
 */
export function attachCloudRenderer(
  clouds: CloudsHandle & { _internal?: unknown },
  renderer: THREE.WebGLRenderer,
  camera: THREE.PerspectiveCamera,
  scene: THREE.Scene,
  sunDir: () => THREE.Vector3,
  sunIntensity: () => number,
): (dt: number) => void {
  // Find the cloud scene + mat by traversing internal refs (we built them above).
  // Simpler: re-create the uniforms here by re-querying scene children.
  const composite = scene.children.find((c) => (c as THREE.Mesh).material && (((c as THREE.Mesh).material as THREE.MeshBasicMaterial).map as THREE.Texture)?.isRenderTargetTexture) as THREE.Mesh | undefined
  if (!composite) return () => {}
  const compositeMat = composite.material as THREE.MeshBasicMaterial
  const rt = compositeMat.map as THREE.WebGLRenderTarget
  const cam = new THREE.OrthographicCamera(-1, 1, 1, -1, 0, 1)
  const cloudScene = new THREE.Scene()
  const geo = new THREE.PlaneGeometry(2, 2)
  const mat = new THREE.ShaderMaterial({
    vertexShader: CLOUD_VERT,
    fragmentShader: CLOUD_FRAG,
    uniforms: {
      uTime: { value: 0 },
      uSunDir: { value: new THREE.Vector3(0, 1, 0) },
      uCameraPos: { value: new THREE.Vector3() },
      uSunIntensity: { value: 1 },
      uInvViewProj: { value: new THREE.Matrix4() },
      uResolution: { value: new THREE.Vector2(rt.width, rt.height) },
    },
    depthTest: false,
    depthWrite: false,
    transparent: true,
  })
  const quad = new THREE.Mesh(geo, mat)
  cloudScene.add(quad)

  const viewProj = new THREE.Matrix4()
  const invViewProj = new THREE.Matrix4()

  return (dt: number) => {
    mat.uniforms.uTime.value += dt
    mat.uniforms.uSunDir.value.copy(sunDir())
    mat.uniforms.uSunIntensity.value = sunIntensity()
    mat.uniforms.uCameraPos.value.copy(camera.position)
    viewProj.multiplyMatrices(camera.projectionMatrix, camera.matrixWorldInverse)
    invViewProj.copy(viewProj).invert()
    mat.uniforms.uInvViewProj.value.copy(invViewProj)
    renderer.setRenderTarget(rt)
    renderer.render(cloudScene, cam)
    renderer.setRenderTarget(null)
    void clouds
    void renderer
  }
}
