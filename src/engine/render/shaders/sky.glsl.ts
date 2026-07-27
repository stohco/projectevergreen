/**
 * sky.glsl.ts — Custom sky-dome GLSL shaders for the Er Gen Verse atmosphere.
 *
 * Implements a Rayleigh + Mie single-scattering approximation on a large
 * inverted sphere. The day sky uses a warm cyan-to-cream gradient (xianxia
 * feel — NOT generic blue), sunsets render orange/pink/gold bands, and the
 * night sky fades to deep indigo with a procedural Milky Way band.
 *
 * Sun position, sun direction, day-phase (0=dawn, 0.25=noon, 0.5=dusk,
 * 0.75=midnight), moon phase and a star-intensity factor are all passed in
 * as uniforms. The star field, moon disc and 28-mansion constellations are
 * rendered as separate points clouds but they share the same dome math, so
 * the constants here must stay in sync with SkySystem.ts.
 *
 * This is mod-original GLSL grounded in standard atmospheric scattering
 * theory (Preetham / Hosek / Hill sRGB approximations). NO canon chapter
 * citations.
 */

/** Vertex shader — passes world direction and world position to the fragment. */
export const SKY_VERTEX_SHADER = /* glsl */ `
precision highp float;

uniform float uTime;
uniform vec3 uSunDirection;   // normalized, points FROM horizon TO sun
uniform vec3 uMoonDirection;  // normalized
uniform float uStarIntensity; // 0..1, day → 0, night → 1

varying vec3 vWorldDir;
varying vec3 vWorldPos;
varying float vHeight;        // normalized 0..1 (horizon → zenith)

attribute vec3 position;

void main() {
  vec4 worldPos = modelMatrix * vec4(position, 1.0);
  vWorldPos = worldPos.xyz;
  vWorldDir = normalize(worldPos.xyz - cameraPosition);
  vHeight = clamp(vWorldDir.y * 0.5 + 0.5, 0.0, 1.0);
  gl_Position = projectionMatrix * viewMatrix * worldPos;
  gl_Position.z = gl_Position.w; // force sky to far plane
}
`;

/** Fragment shader — Rayleigh + Mie + sun disc + sunset bands + night sky + Milky Way. */
export const SKY_FRAGMENT_SHADER = /* glsl */ `
precision highp float;

uniform float uTime;
uniform vec3 uSunDirection;
uniform vec3 uMoonDirection;
uniform float uStarIntensity;       // 0..1
uniform float uSunElevation;        // -1..1 (sin of altitude)
uniform float uDayMix;              // 0..1 (0=night, 1=full day)
uniform float uDawnDuskMix;         // 0..1 (peaks near sunrise/sunset)
uniform vec3 uDayZenith;            // warm cyan
uniform vec3 uDayHorizon;           // warm cream
uniform vec3 uSunsetTop;            // gold
uniform vec3 uSunsetMid;            // orange
uniform vec3 uSunsetLow;            // deep pink/magenta
uniform vec3 uNightZenith;          // deep indigo
uniform vec3 uNightHorizon;         // smoky violet
uniform vec3 uSunColor;             // bright warm white
uniform float uSunIntensity;
uniform float uMoonIntensity;

varying vec3 vWorldDir;
varying vec3 vWorldPos;
varying float vHeight;

// --- Hash / noise helpers ----------------------------------------------------
float hash11(float p) {
  p = fract(p * 0.1031);
  p *= p + 33.33;
  p *= p + p;
  return fract(p);
}
float hash13(vec3 p) {
  p = fract(p * 0.1031);
  p += dot(p, p.zyx + 31.32);
  return fract((p.x + p.y) * p.z);
}
float vnoise(vec3 p) {
  vec3 i = floor(p);
  vec3 f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  float n000 = hash13(i + vec3(0,0,0));
  float n100 = hash13(i + vec3(1,0,0));
  float n010 = hash13(i + vec3(0,1,0));
  float n110 = hash13(i + vec3(1,1,0));
  float n001 = hash13(i + vec3(0,0,1));
  float n101 = hash13(i + vec3(1,0,1));
  float n011 = hash13(i + vec3(0,1,1));
  float n111 = hash13(i + vec3(1,1,1));
  return mix(
    mix(mix(n000, n100, f.x), mix(n010, n110, f.x), f.y),
    mix(mix(n001, n101, f.x), mix(n011, n111, f.x), f.y),
    f.z
  );
}
float fbm(vec3 p) {
  float v = 0.0;
  float a = 0.5;
  for (int i = 0; i < 5; i++) {
    v += a * vnoise(p);
    p = p * 2.02 + vec3(1.7, 9.2, 4.3);
    a *= 0.5;
  }
  return v;
}

// --- Atmospheric scattering -------------------------------------------------
// Rayleigh coefficients (tuned for xianxia warmth, not physically accurate).
const vec3 RAYLEIGH_BETA = vec3(5.8e-3, 12.0e-3, 19.5e-3);
const vec3 MIE_BETA = vec3(21.0e-3, 21.0e-3, 21.0e-3);
const float MIE_G = 0.76;          // anisotropy (forward peak)
const float ATMOSPHERE_HEIGHT = 1.0;

float rayleighPhase(float cosTheta) {
  return 0.0596831 * (1.0 + cosTheta * cosTheta);
}
float miePhase(float cosTheta, float g) {
  float g2 = g * g;
  return 0.1193662 * ((1.0 - g2) * (1.0 + cosTheta * cosTheta)) /
         ((2.0 + g2) * pow(1.0 + g2 - 2.0 * g * cosTheta, 1.5));
}

// Smoothstep helper.
float sm(float a, float b, float x) {
  return clamp((x - a) / (b - a), 0.0, 1.0);
}

void main() {
  vec3 dir = normalize(vWorldDir);
  float cosSun = max(dot(dir, uSunDirection), 0.0);
  float cosMoon = max(dot(dir, uMoonDirection), 0.0);

  // Optical depth along the view ray (thinner at zenith, thicker at horizon).
  float od = mix(1.0 / max(dir.y, 0.02), 1.0 / max(abs(dir.y) + 0.001, 0.001), 0.0);
  od = clamp(od, 0.0, 36.0);
  vec3 rayleigh = RAYLEIGH_BETA * od;
  vec3 mie = MIE_BETA * od;

  // Inscattering from the sun.
  vec3 sunTransmittance = exp(-(rayleigh + mie) * 1.0);
  vec3 skyColor = sunTransmittance * (rayleigh * rayleighPhase(cosSun) +
                                       mie * miePhase(cosSun, MIE_G));
  skyColor *= 18.0; // overall scale

  // --- Mix in the xianxia gradient based on elevation + day phase ------------
  float h = clamp(dir.y, 0.0, 1.0);
  float horizonBand = 1.0 - smoothstep(0.0, 0.45, abs(dir.y));
  vec3 dayCol = mix(uDayHorizon, uDayZenith, sm(0.0, 0.55, h));
  vec3 nightCol = mix(uNightHorizon, uNightZenith, sm(0.0, 0.6, h));

  // Sunset bands — strongest at horizon, biased toward sun azimuth.
  float sunsetMask = horizonBand * uDawnDuskMix;
  float sunAzimuthBand = pow(max(dot(normalize(vec3(dir.x, 0.0, dir.z)),
                                      normalize(vec3(uSunDirection.x, 0.0, uSunDirection.z))), 0.0), 2.0);
  vec3 sunsetCol = mix(uSunsetLow, uSunsetMid, sm(0.0, 0.5, sunAzimuthBand));
  sunsetCol = mix(sunsetCol, uSunsetTop, sm(0.4, 1.0, sunAzimuthBand) * sm(0.0, 0.6, h));

  // Composite: day ↔ night with sunset band overlaid.
  vec3 base = mix(nightCol, dayCol, uDayMix);
  base = mix(base, sunsetCol, sunsetMask * 0.85);

  // Blend the physical scattering on top — gives the natural blue-hour glow.
  base += skyColor * 0.12 * uDayMix;

  // --- Sun disc -------------------------------------------------------------
  float sunDisc = smoothstep(0.99955, 0.99972, cosSun);
  // Outer glow.
  float sunGlow = pow(cosSun, 350.0) * 0.6 + pow(cosSun, 32.0) * 0.15;
  vec3 sunAdd = uSunColor * (sunDisc * uSunIntensity + sunGlow * uSunIntensity * 0.5);

  // --- Moon disc (subtle — main moon render is in MoonMesh) -----------------
  float moonDisc = smoothstep(0.9994, 0.99955, cosMoon);
  vec3 moonAdd = vec3(0.85, 0.88, 0.95) * moonDisc * uMoonIntensity * 0.6;
  float moonGlow = pow(cosMoon, 1024.0) * 0.4 + pow(cosMoon, 64.0) * 0.15;
  moonAdd += vec3(0.55, 0.62, 0.85) * moonGlow * uMoonIntensity * 0.35;

  // --- Milky Way band (procedural noise) ------------------------------------
  // Project sky direction onto a tilted galactic plane and stretch noise.
  vec3 galDir = normalize(vec3(0.3, 0.4, 1.0));
  vec3 side = normalize(cross(galDir, vec3(0, 1, 0)));
  vec3 up = cross(galDir, side);
  float galLat = abs(dot(dir, up));
  float galLon = dot(dir, side);
  float galBand = exp(-galLat * galLat * 38.0);
  float milky = fbm(vec3(galLon * 6.0, dir.y * 4.0, uTime * 0.0008)) ;
  milky = pow(milky, 2.4) * galBand * 1.6;
  vec3 milkyCol = mix(vec3(0.20, 0.22, 0.36), vec3(0.55, 0.50, 0.42), milky);
  vec3 milkyAdd = milkyCol * milky * uStarIntensity * 0.65;

  // --- Horizon haze layer (warm mist at low altitudes) ----------------------
  float haze = horizonBand * (0.25 + 0.45 * uDayMix);
  vec3 hazeCol = mix(uNightHorizon * 0.6, uDayHorizon * 0.9, uDayMix);
  hazeCol = mix(hazeCol, uSunsetMid * 1.05, uDawnDuskMix * 0.6);
  base = mix(base, hazeCol, haze * 0.35);

  // --- Final composite ------------------------------------------------------
  vec3 final = base + sunAdd + moonAdd + milkyAdd;
  // Slight contrast lift so the day sky feels photographic, not flat.
  final = pow(final, vec3(0.92));
  gl_FragColor = vec4(final, 1.0);

  #include <colorspace_fragment>
}
`;

/** Stars shader — twinkle + atmospheric extinction near horizon. */
export const STARS_VERTEX_SHADER = /* glsl */ `
precision highp float;

uniform float uTime;
uniform float uIntensity;     // 0..1, fades in at night
uniform float uPixelRatio;
uniform vec3 uSunDirection;

attribute vec3 position;
attribute float aMagnitude;   // 0..1 brightness seed
attribute float aTwinklePhase;
attribute float aSize;

varying float vMag;
varying float vTwinkle;
varying float vIntensity;

void main() {
  vec4 mvPosition = modelViewMatrix * vec4(position, 1.0);
  gl_Position = projectionMatrix * mvPosition;

  float twinkle = 0.65 + 0.35 * sin(uTime * (0.8 + aTwinklePhase * 1.6) + aTwinklePhase * 31.0);
  vTwinkle = twinkle;
  vMag = aMagnitude;

  // Stars fade out near the sun direction and below the horizon.
  float sunFalloff = 1.0 - max(dot(normalize(position), uSunDirection), 0.0);
  sunFalloff = pow(sunFalloff, 6.0);
  float horizonFade = smoothstep(-0.05, 0.18, normalize(position).y);
  vIntensity = uIntensity * sunFalloff * horizonFade;

  float baseSize = aSize * (0.7 + aMagnitude * 1.8);
  gl_PointSize = baseSize * uPixelRatio * (300.0 / max(-mvPosition.z, 1.0));
  gl_PointSize = clamp(gl_PointSize, 0.0, 6.0 * uPixelRatio);
}
`;

export const STARS_FRAGMENT_SHADER = /* glsl */ `
precision highp float;
varying float vMag;
varying float vTwinkle;
varying float vIntensity;

void main() {
  vec2 uv = gl_PointCoord - 0.5;
  float d = length(uv);
  float core = smoothstep(0.5, 0.0, d);
  float halo = smoothstep(0.5, 0.1, d) * 0.4;
  float a = (core + halo) * vTwinkle * vIntensity * (0.5 + vMag * 0.8);
  if (a < 0.01) discard;

  // Slight color variation: warmer for brighter stars (canonical chinese
  // astro chart aesthetic — 角宿 fires warmer, 危宿 cooler).
  vec3 col = mix(vec3(0.78, 0.86, 1.0), vec3(1.0, 0.92, 0.78), vMag);
  gl_FragColor = vec4(col, a);
}
`;

/** Moon disc shader — phase + soft craters. */
export const MOON_VERTEX_SHADER = /* glsl */ `
precision highp float;
attribute vec3 position;
varying vec3 vPos;
void main() {
  vPos = position;
  gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
}
`;

export const MOON_FRAGMENT_SHADER = /* glsl */ `
precision highp float;
uniform float uPhase;       // 0..1 (0=new, 0.5=full, 1=new)
uniform float uIntensity;
uniform vec3 uMoonDirection;
varying vec3 vPos;

float hash(vec2 p) {
  return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}
float vnoise(vec2 p) {
  vec2 i = floor(p);
  vec2 f = fract(p);
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
  vec3 n = normalize(vPos);
  // Lit side faces the sun (opposite of moon direction).
  vec3 sunDir = -uMoonDirection;
  float lit = dot(n, sunDir);
  float terminator = smoothstep(-0.05, 0.05, lit);

  // Phase mask — terminator shifts across the disc as the phase varies.
  float phaseShift = (uPhase - 0.5) * 2.6;
  float phaseLit = smoothstep(-0.05, 0.05, lit + phaseShift);

  // Craters via fbm.
  float craters = fbm(n.xy * 8.0 + n.z * 2.0);
  craters = smoothstep(0.35, 0.85, craters);

  vec3 darkMare = vec3(0.62, 0.62, 0.68);
  vec3 brightHigh = vec3(0.95, 0.94, 0.92);
  vec3 moonCol = mix(darkMare, brightHigh, craters);

  // Earthshine on dark side — subtle blue glow.
  vec3 earthshine = vec3(0.05, 0.06, 0.10);

  vec3 final = mix(earthshine, moonCol, phaseLit) * uIntensity;
  float alpha = 1.0;
  gl_FragColor = vec4(final, alpha);

  #include <colorspace_fragment>
}
`;
