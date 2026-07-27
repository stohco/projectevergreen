/**
 * spirit_vein.glsl.ts — Pulsing jade-glow shader for spirit veins (灵脉).
 *
 * Spirit veins are channels of condensed spiritual qi running through the
 * world. They should radiate a soft jade-green pulse that brightens and dims
 * on a slow cycle (4s period) and have a brighter "core" along the vein
 * centerline with a softer glow halo around it.
 *
 * NO canon chapter citations.
 */

export const SPIRIT_VEIN_VERTEX_SHADER = /* glsl */ `
precision highp float;
attribute vec3 position;
attribute vec3 normal;
attribute vec2 uv;
uniform float uTime;
varying vec3 vNormal;
varying vec3 vViewDir;
varying vec2 vUv;
varying vec3 vLocalPos;
void main() {
  vUv = uv;
  vLocalPos = position;
  vec4 mv = modelViewMatrix * vec4(position, 1.0);
  vNormal = normalize(normalMatrix * normal);
  vViewDir = normalize(-mv.xyz);
  gl_Position = projectionMatrix * mv;
}
`;

export const SPIRIT_VEIN_FRAGMENT_SHADER = /* glsl */ `
precision highp float;
uniform float uTime;
uniform vec3  uCoreColor;     // bright jade
uniform vec3  uGlowColor;     // soft cyan-green
uniform float uPulseSpeed;
uniform float uIntensity;

varying vec3 vNormal;
varying vec3 vViewDir;
varying vec2 vUv;
varying vec3 vLocalPos;

float hash(vec2 p) {
  return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}
float vnoise(vec2 p) {
  vec2 i = floor(p); vec2 f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  return mix(mix(hash(i), hash(i + vec2(1,0)), f.x),
             mix(hash(i + vec2(0,1)), hash(i + vec2(1,1)), f.x), f.y);
}
float fbm(vec2 p) {
  float v = 0.0; float a = 0.5;
  for (int i = 0; i < 5; i++) { v += a * vnoise(p); p *= 2.0; a *= 0.5; }
  return v;
}

void main() {
  // Two pulses — slow global + fast trickle along the vein.
  float slowPulse = 0.5 + 0.5 * sin(uTime * uPulseSpeed);
  float trickle = 0.5 + 0.5 * sin(vUv.x * 14.0 - uTime * 2.4);
  float pulse = mix(slowPulse, trickle, 0.45);

  // Vein cross-section: bright core, fading outward.
  float cross = 1.0 - abs(vUv.y - 0.5) * 2.0;
  cross = pow(max(cross, 0.0), 1.6);

  // Fresnel halo for "qi leaking into the air".
  float fres = 1.0 - max(dot(vNormal, vViewDir), 0.0);
  fres = pow(fres, 2.0);

  // Subtle fbm shimmer.
  float shimmer = fbm(vUv * 12.0 + uTime * 0.5);

  vec3 col = mix(uGlowColor, uCoreColor, cross);
  col += uCoreColor * fres * 0.8;
  col *= (0.7 + pulse * 0.8 + shimmer * 0.25) * uIntensity;

  float alpha = clamp(cross * 0.85 + fres * 0.5, 0.0, 1.0);
  gl_FragColor = vec4(col, alpha);
}
`;
