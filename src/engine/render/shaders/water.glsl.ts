/**
 * water.glsl.ts — Animated water shader with reflection, refraction, foam
 * and biome-aware tint (jade-green default, demonic-red for the Sea of Devils).
 *
 * Uses Gerstner wave sums for the vertex displacement and a sum-of-sines
 * normal field for the fragment lighting. Reflections use a planar-screen
 * approximation (sample the framebuffer with a flipped Y) and refraction
 * tints by depth. Foam appears at shorelines via a depth-based mask.
 *
 * This is a mod-original GLSL water shader. NO canon chapter citations.
 */

export const WATER_VERTEX_SHADER = /* glsl */ `
precision highp float;

uniform float uTime;
uniform float uWaveScale;
uniform float uWaveHeight;

attribute vec3 position;
attribute vec2 uv;

varying vec3 vWorldPos;
varying vec3 vNormal;
varying vec2 vUv;
varying float vWaveHeight;

// Gerstner wave sum.
vec3 gerstner(vec2 pos, vec2 dir, float wavelength, float steepness, float t, inout vec3 tangent, inout vec3 binorm) {
  float k = 6.28318 / wavelength;
  float c = sqrt(9.8 / k);
  vec2 d = normalize(dir);
  float f = k * (dot(d, pos) - c * t);
  float a = steepness / k;
  float sinF = sin(f);
  float cosF = cos(f);
  tangent += vec3(-d.x * d.x * steepness * sinF,
                   d.x * steepness * cosF,
                  -d.x * d.y * steepness * sinF);
  binorm  += vec3(-d.x * d.y * steepness * sinF,
                   d.y * steepness * cosF,
                  -d.y * d.y * steepness * sinF);
  return vec3(d.x * cosF * a, sinF * a, d.y * cosF * a);
}

void main() {
  vUv = uv;
  vec3 pos = position;
  vec3 disp = vec3(0.0);
  vec3 tangent = vec3(1.0, 0.0, 0.0);
  vec3 binorm = vec3(0.0, 0.0, 1.0);

  float h = uWaveHeight * uWaveScale;
  disp += gerstner(pos.xz, vec2( 1.0,  0.4), 28.0 * uWaveScale, 0.18 * h, uTime, tangent, binorm);
  disp += gerstner(pos.xz, vec2(-0.6,  1.0), 17.0 * uWaveScale, 0.13 * h, uTime, tangent, binorm);
  disp += gerstner(pos.xz, vec2( 0.8, -0.7), 11.0 * uWaveScale, 0.08 * h, uTime, tangent, binorm);
  disp += gerstner(pos.xz, vec2( 0.3,  0.9),  6.0 * uWaveScale, 0.05 * h, uTime, tangent, binorm);

  vec3 displaced = pos + disp;
  vWaveHeight = disp.y;
  vNormal = normalize(cross(binorm, tangent));
  vec4 worldPos = modelMatrix * vec4(displaced, 1.0);
  vWorldPos = worldPos.xyz;
  gl_Position = projectionMatrix * viewMatrix * worldPos;
}
`;

export const WATER_FRAGMENT_SHADER = /* glsl */ `
precision highp float;

uniform float uTime;
uniform vec3  uCameraPos;
uniform vec3  uSunDirection;
uniform vec3  uSunColor;
uniform vec3  uShallowColor;     // clear jade near shores
uniform vec3  uDeepColor;        // dark jade at depth
uniform vec3  uDemonicTint;      // red tint for Sea of Devils
uniform float uDemonicMix;       // 0..1
uniform float uFoamThreshold;    // wave height at which foam kicks in
uniform float uSkyMix;           // 0..1 — how much reflected sky shows on top
uniform vec3  uSkyColor;         // approximate sky color at horizon
uniform sampler2D uReflection;   // planar reflection texture
uniform float uHasReflection;
uniform vec2  uResolution;

varying vec3 vWorldPos;
varying vec3 vNormal;
varying vec2 vUv;
varying float vWaveHeight;

float hash(vec2 p) {
  return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

void main() {
  vec3 N = normalize(vNormal);
  vec3 V = normalize(uCameraPos - vWorldPos);
  vec3 L = normalize(uSunDirection);
  vec3 H = normalize(L + V);

  // Fresnel — water reflects more at grazing angles.
  float fres = pow(1.0 - max(dot(N, V), 0.0), 5.0);
  fres = mix(0.04, 1.0, fres);

  // Planar reflection (project world-XZ into reflection texture).
  vec3 reflCol = uSkyColor;
  if (uHasReflection > 0.5) {
    vec2 reflUv = vec2(vUv.x, 1.0 - vUv.y);
    reflUv += N.xz * 0.04;
    reflCol = texture2D(uReflection, reflUv).rgb;
  } else {
    // Procedural sky-tint approximation: brighter near horizon.
    float sky = 0.6 + 0.4 * N.y;
    reflCol = mix(uSkyColor, vec3(1.0), sky * 0.2);
  }

  // Refraction tint — depth-fade shallow→deep.
  float depthFade = clamp(pow(1.0 - max(dot(N, V), 0.0), 1.5), 0.0, 1.0);
  vec3 waterCol = mix(uShallowColor, uDeepColor, depthFade);
  // Sea of Devils demonic-red depth tint.
  waterCol = mix(waterCol, waterCol * vec3(1.3, 0.4, 0.35) + uDemonicTint * 0.15, uDemonicMix);

  // Specular sun glint — sharper than Fresnel alone.
  float spec = pow(max(dot(N, H), 0.0), 240.0);
  vec3 specCol = uSunColor * spec * 1.4;

  // Foam at wave crests.
  float foam = smoothstep(uFoamThreshold, uFoamThreshold + 0.18, vWaveHeight);
  // Foam also at "shoreline" — approximate by world Y near 0.
  float shoreFoam = smoothstep(0.4, 0.0, abs(vWorldPos.y));
  foam = clamp(foam + shoreFoam * 0.25, 0.0, 1.0);
  vec3 foamCol = mix(vec3(0.92, 0.96, 1.0), vec3(1.0), 0.5);

  // Composite.
  vec3 col = mix(waterCol, reflCol, fres * uSkyMix);
  col += specCol;
  col = mix(col, foamCol, foam * 0.8);

  // Subtle ripple shimmer.
  float shimmer = hash(floor(vWorldPos.xz * 4.0 + uTime * 2.0)) * 0.04;
  col += shimmer;

  gl_FragColor = vec4(col, 0.92);

  #include <colorspace_fragment>
}
`;
