/**
 * qi_aura.glsl.ts — Cultivator qi-field shader.
 *
 * Renders a soft jade-green/cyan halo around a cultivator when they cast
 * (or whenever their qi flux is high). The halo is a billboarded sphere with
 * a fresnel-weighted additive shell — brighter at the silhouette, fading to
 * transparent at the center, so the cultivator's body still reads through it.
 *
 * The shader also generates a slow churning noise pattern so the qi field
 * looks alive (cultivators are literally "moving the qi" in xianxia canon).
 *
 * NO canon chapter citations.
 */

export const QI_AURA_VERTEX_SHADER = /* glsl */ `
precision highp float;
attribute vec3 position;
attribute vec3 normal;
uniform float uTime;
uniform float uPulse;          // 0..1 — current qi flux (drives intensity + scale)
uniform float uRadius;
varying vec3 vNormal;
varying vec3 vViewDir;
varying vec3 vLocalPos;
void main() {
  vec3 p = position * uRadius;
  // Subtle breathing scale.
  float breath = 1.0 + 0.04 * sin(uTime * 1.8) + uPulse * 0.12;
  p *= breath;
  vec4 mv = modelViewMatrix * vec4(p, 1.0);
  vNormal = normalize(normalMatrix * normal);
  vViewDir = normalize(-mv.xyz);
  vLocalPos = position;
  gl_Position = projectionMatrix * mv;
}
`;

export const QI_AURA_FRAGMENT_SHADER = /* glsl */ `
precision highp float;
uniform float uTime;
uniform float uPulse;
uniform vec3  uAuraColor;       // jade green default
uniform vec3  uRimColor;        // cyan rim
varying vec3 vNormal;
varying vec3 vViewDir;
varying vec3 vLocalPos;

float hash(vec3 p) {
  p = fract(p * 0.3183099 + vec3(0.1, 0.2, 0.3));
  p *= 17.0;
  return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}
float vnoise(vec3 p) {
  vec3 i = floor(p); vec3 f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  return mix(
    mix(mix(hash(i), hash(i + vec3(1,0,0)), f.x),
        mix(hash(i + vec3(0,1,0)), hash(i + vec3(1,1,0)), f.x), f.y),
    mix(mix(hash(i + vec3(0,0,1)), hash(i + vec3(1,0,1)), f.x),
        mix(hash(i + vec3(0,1,1)), hash(i + vec3(1,1,1)), f.x), f.y), f.z);
}
float fbm(vec3 p) {
  float v = 0.0; float a = 0.5;
  for (int i = 0; i < 4; i++) { v += a * vnoise(p); p *= 2.1; a *= 0.5; }
  return v;
}

void main() {
  float fres = 1.0 - max(dot(vNormal, vViewDir), 0.0);
  fres = pow(fres, 1.6);

  // Churning qi pattern — fbm scrolls upward (cultivators cycle qi from
  // feet → crown, a stock xianxia visual).
  vec3 q = vLocalPos * 3.0 + vec3(0.0, -uTime * 0.6, 0.0);
  float churn = fbm(q);
  churn = pow(churn, 1.5);

  // Spiral arms — qi winds around the cultivator.
  float ang = atan(vLocalPos.z, vLocalPos.x);
  float spiral = sin(ang * 3.0 + uTime * 1.4 + vLocalPos.y * 4.0) * 0.5 + 0.5;
  spiral = pow(spiral, 2.0);

  float density = (fres * 0.7 + churn * 0.4 + spiral * 0.3) * (0.6 + uPulse * 0.8);
  density = clamp(density, 0.0, 1.4);

  vec3 col = mix(uAuraColor, uRimColor, fres * 0.6);
  col += uRimColor * spiral * 0.25 * uPulse;

  float alpha = clamp(density * (0.4 + uPulse * 0.6), 0.0, 0.95);
  gl_FragColor = vec4(col * (0.8 + uPulse * 1.3), alpha);
}
`;
