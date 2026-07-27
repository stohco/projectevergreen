/**
 * particles.glsl.ts — GPU particle billboard shader.
 *
 * Used by ParticleSystem.ts for falling blossoms, floating qi motes, snow,
 * sand, embers, leaves and rain. Each particle carries a per-vertex seed,
 * size, lifetime and motion bias; the vertex shader animates them entirely
 * on the GPU (no CPU updates per frame).
 *
 * Particles billboard toward the camera in the vertex shader and fade in/out
 * by life. Color and motion bias come from uniforms so the same shader runs
 * every biome.
 *
 * NO canon chapter citations.
 */

export const PARTICLE_VERTEX_SHADER = /* glsl */ `
precision highp float;

uniform float uTime;
uniform float uPixelRatio;
uniform vec3  uOrigin;          // emitter origin (world)
uniform vec3  uWind;            // wind drift (m/s)
uniform float uGravity;         // m/s^2 (negative = falls)
uniform float uSpreadRadius;    // horizontal spawn radius (m)
uniform float uLifetime;        // seconds
uniform float uSize;            // base sprite size (m)
uniform vec2  uScreenRes;

attribute float aSeed;          // 0..1 — unique per particle
attribute vec3  aOffset;        // initial spawn offset within the emitter
attribute float aSizeMul;       // 0.5..1.5

varying float vAlpha;
varying float vSeed;
varying vec2  vLocalUv;

float hash(float n) { return fract(sin(n) * 43758.5453123); }

void main() {
  // Cycle particle through its lifetime using its seed as a phase offset.
  float lifePhase = fract(uTime / uLifetime + aSeed);
  float age = lifePhase;            // 0..1
  float ageS = age * uLifetime;     // seconds since spawn

  // Position: spawn offset + drift + wind + gravity fall.
  vec3 pos = uOrigin + aOffset;
  pos += uWind * ageS;
  pos.y += uGravity * ageS * ageS * 0.5;

  // Wrap horizontally so the field never depletes.
  vec2 wrap = mod(pos.xz - uOrigin.xz + uSpreadRadius, uSpreadRadius * 2.0) - uSpreadRadius;
  pos.x = uOrigin.x + wrap.x;
  pos.z = uOrigin.z + wrap.y;

  // Vertical wrap: respawn at top once it falls past origin.y - spreadRadius.
  float topY = uOrigin.y + uSpreadRadius * 0.5;
  float botY = uOrigin.y - uSpreadRadius * 0.5;
  float span = topY - botY;
  pos.y = botY + mod(pos.y - botY + span, span);

  vec4 mvPosition = modelViewMatrix * vec4(pos, 1.0);
  gl_Position = projectionMatrix * mvPosition;

  // Fade in (0..0.1) and out (0.8..1.0) of life.
  float fadeIn = smoothstep(0.0, 0.08, age);
  float fadeOut = 1.0 - smoothstep(0.8, 1.0, age);
  vAlpha = fadeIn * fadeOut;

  vSeed = aSeed;
  vLocalUv = vec2(0.5);

  // Distance-attenuated size — keep close particles punchy, distant ones tiny.
  float dist = max(-mvPosition.z, 1.0);
  gl_PointSize = uSize * aSizeMul * uPixelRatio * (300.0 / dist);
  gl_PointSize = clamp(gl_PointSize, 0.0, 48.0 * uPixelRatio);
}
`;

export const PARTICLE_FRAGMENT_SHADER = /* glsl */ `
precision highp float;

uniform vec3  uColor;
uniform vec3  uColor2;        // secondary color for gradient
uniform float uGlow;          // 0..1 — emissive intensity
uniform sampler2D uSprite;    // optional sprite (1x1 white = procedural)
uniform float uHasSprite;     // 0 or 1
uniform float uSoftness;      // 0 hard edge, 1 very soft

varying float vAlpha;
varying float vSeed;
varying vec2  vLocalUv;

void main() {
  vec2 uv = gl_PointCoord - 0.5;
  float d = length(uv);

  // Procedural soft sprite if no texture bound.
  float sprite;
  if (uHasSprite > 0.5) {
    sprite = texture2D(uSprite, gl_PointCoord).a;
  } else {
    float core = smoothstep(0.5, 0.0, d);
    float halo = exp(-d * 6.0);
    sprite = mix(core, halo, uSoftness);
  }

  if (sprite < 0.003) discard;

  vec3 col = mix(uColor, uColor2, vSeed);
  col += uGlow * uColor * 1.5;

  float a = sprite * vAlpha;
  gl_FragColor = vec4(col, a);
}
`;
