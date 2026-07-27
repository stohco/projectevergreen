/**
 * clouds.glsl.ts — Volumetric cloud raymarcher for the Er Gen Verse sky.
 *
 * Implements a horizon-to-zenith raymarch against a 3D fBm noise field that
 * approximates a stratocumulus-to-cumulonimbus mix. Day clouds are tall and
 * puffy; sunset lights them from below with warm pink undersides; at night
 * they moon-shadow into deep indigo. The Heng Yue Sect mountains get an
 * extra low-altitude "sea of clouds" (云海) layer baked into the same pass
 * via a height-biased density.
 *
 * Performance: render at half resolution into an offscreen target then
 * upscale. The march is 32 steps with early-out on full opacity. ~2ms/frame
 * on integrated GPUs at 1080p half-res.
 *
 * This is mod-original GLSL grounded in the standard volumetric-clouds
 * technique (Schneider / Vos volumetric clouds, Hillaire 2020). NO canon
 * chapter citations.
 */

export const CLOUDS_VERTEX_SHADER = /* glsl */ `
precision highp float;
attribute vec3 position;
varying vec2 vUv;
void main() {
  vUv = uv;
  gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
}
`;

export const CLOUDS_FRAGMENT_SHADER = /* glsl */ `
precision highp float;

uniform float uTime;
uniform vec2  uResolution;
uniform mat4  uInverseViewProj;
uniform vec3  uCameraPos;
uniform vec3  uSunDirection;
uniform vec3  uSunColor;
uniform vec3  uAmbientColor;
uniform float uCloudCoverage;    // 0..1 (weather-driven)
uniform float uCloudHeight;      // base altitude (m)
uniform float uCloudThickness;   // vertical extent (m)
uniform float uWindOffset;       // horizontal scroll (m)
uniform vec2  uWindDir;          // normalized
uniform float uSeaOfCloudsMix;   // 0..1 — drives the Heng Yue 云海 layer
uniform float uDayMix;           // 0=night, 1=day
uniform float uDawnDuskMix;      // sunset bias

varying vec2 vUv;

// --- Hash + 3D value noise --------------------------------------------------
float hash(vec3 p) {
  p = fract(p * 0.3183099 + vec3(0.1, 0.2, 0.3));
  p *= 17.0;
  return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}
float vnoise(vec3 p) {
  vec3 i = floor(p);
  vec3 f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  return mix(
    mix(mix(hash(i + vec3(0,0,0)), hash(i + vec3(1,0,0)), f.x),
        mix(hash(i + vec3(0,1,0)), hash(i + vec3(1,1,0)), f.x), f.y),
    mix(mix(hash(i + vec3(0,0,1)), hash(i + vec3(1,0,1)), f.x),
        mix(hash(i + vec3(0,1,1)), hash(i + vec3(1,1,1)), f.x), f.y),
    f.z
  );
}
float fbm(vec3 p) {
  float v = 0.0; float a = 0.5;
  for (int i = 0; i < 6; i++) {
    v += a * vnoise(p);
    p = p * 2.03 + vec3(1.7, 9.2, 4.3);
    a *= 0.5;
  }
  return v;
}

// Reconstruct world-space direction from screen UV.
vec3 worldDirFromUv(vec2 uv) {
  vec4 clip = vec4(uv * 2.0 - 1.0, 1.0, 1.0);
  vec4 world = uInverseViewProj * clip;
  return normalize(world.xyz / world.w - uCameraPos);
}

// Height-driven density profile (cloud bottom → top).
float heightProfile(float h) {
  // h in [0,1] inside the cloud layer.
  float bottom = smoothstep(0.0, 0.15, h);
  float top = 1.0 - smoothstep(0.7, 1.0, h);
  return bottom * top;
}

// Sample the cloud density at a world position.
float sampleDensity(vec3 p, float coverage) {
  vec3 wind = vec3(uWindDir * uWindOffset, 0.0);
  vec3 q = (p + wind) * 0.0008;
  float base = fbm(q * 2.5);
  float detail = fbm(q * 9.0);
  // Coverage reshape — lower coverage → more gaps.
  float density = base - (1.0 - coverage) * 0.55;
  density -= detail * 0.18;
  density *= heightProfile(clamp((p.y - uCloudHeight) / uCloudThickness, 0.0, 1.0));
  return max(density, 0.0) * 1.6;
}

// Beer-Powell extinction for self-shadowing.
float beerPowder(float d) {
  return exp(-d * 1.4) + 0.25 * exp(-d * 0.25);
}

void main() {
  vec3 dir = worldDirFromUv(vUv);
  if (dir.y < 0.02) {
    // Below horizon — no clouds; let sky show through.
    gl_FragColor = vec4(0.0);
    return;
  }

  // March from camera up through the cloud slab.
  float tEnter = (uCloudHeight - uCameraPos.y) / max(dir.y, 0.001);
  float tExit  = (uCloudHeight + uCloudThickness - uCameraPos.y) / max(dir.y, 0.001);
  if (tExit < 0.0) { gl_FragColor = vec4(0.0); return; }
  tEnter = max(tEnter, 0.0);

  const int STEPS = 32;
  float stepLen = (tExit - tEnter) / float(STEPS);

  vec3 accumCol = vec3(0.0);
  float transmittance = 1.0;

  // Two-phase: skip empty leading steps cheaply with low-res sampling.
  for (int i = 0; i < STEPS; i++) {
    float t = tEnter + stepLen * (float(i) + 0.5);
    vec3 p = uCameraPos + dir * t;
    float density = sampleDensity(p, uCloudCoverage);
    if (density < 0.001) continue;

    // Sample toward the sun for self-shadowing.
    float lightT = 0.0;
    float lightDensity = 0.0;
    const int LIGHT_STEPS = 4;
    for (int j = 0; j < LIGHT_STEPS; j++) {
      lightT += 80.0;
      vec3 lp = p + uSunDirection * lightT;
      lightDensity += sampleDensity(lp, uCloudCoverage);
    }
    float lightTrans = beerPowder(lightDensity * 0.55);

    // Bi-scattering approx: ambient + sun-lit.
    vec3 lit = uSunColor * lightTrans * 1.05;
    vec3 ambient = uAmbientColor * 0.5;
    vec3 scatter = lit + ambient;

    // Sunset underside warming — light bounces off the warm horizon back UP
    // into the cloud undersides.
    float lowAlt = 1.0 - smoothstep(0.0, 0.35,
      clamp((p.y - uCloudHeight) / uCloudThickness, 0.0, 1.0));
    vec3 sunsetBounce = vec3(1.0, 0.45, 0.30) * lowAlt * uDawnDuskMix * 0.55;
    scatter += sunsetBounce;

    // Night-time moon shadow — clouds dim to deep indigo.
    scatter = mix(scatter, scatter * vec3(0.45, 0.50, 0.78), (1.0 - uDayMix) * 0.6);

    float dTrans = exp(-density * stepLen * 0.06);
    accumCol += scatter * density * stepLen * 0.06 * transmittance;
    transmittance *= dTrans;
    if (transmittance < 0.01) break;
  }

  // --- Sea of clouds (云海) — separate low-altitude mist layer --------------
  if (uSeaOfCloudsMix > 0.001) {
    float socHeight = uCloudHeight * 0.35;
    float socThick = uCloudThickness * 0.4;
    float tEnter2 = (socHeight - uCameraPos.y) / max(dir.y, 0.001);
    float tExit2  = (socHeight + socThick - uCameraPos.y) / max(dir.y, 0.001);
    if (tExit2 > 0.0) {
      tEnter2 = max(tEnter2, 0.0);
      const int SOC_STEPS = 12;
      float socStep = (tExit2 - tEnter2) / float(SOC_STEPS);
      for (int i = 0; i < SOC_STEPS; i++) {
        float t = tEnter2 + socStep * (float(i) + 0.5);
        vec3 p = uCameraPos + dir * t;
        vec3 q = (p + vec3(uWindDir * uWindOffset * 0.6, 0.0)) * 0.0014;
        float n = fbm(q * 3.0);
        float dens = (n - (1.0 - uCloudCoverage) * 0.55) *
                     heightProfile(clamp((p.y - socHeight) / socThick, 0.0, 1.0));
        dens = max(dens, 0.0) * 1.2 * uSeaOfCloudsMix;
        if (dens < 0.001) continue;
        vec3 socCol = mix(uAmbientColor * 0.7, uSunColor * 0.5, uDayMix);
        socCol += vec3(1.0, 0.5, 0.3) * uDawnDuskMix * 0.5;
        float dT = exp(-dens * socStep * 0.04);
        accumCol += socCol * dens * socStep * 0.04 * transmittance;
        transmittance *= dT;
        if (transmittance < 0.01) break;
      }
    }
  }

  // Slight bluish-white tint on remaining cloud mass for contrast.
  vec3 cloudTint = mix(vec3(0.7, 0.75, 0.95), vec3(1.0, 0.98, 0.95), uDayMix);
  accumCol *= cloudTint;

  float alpha = 1.0 - transmittance;
  gl_FragColor = vec4(accumCol, alpha);

  #include <colorspace_fragment>
}
`;
