/**
 * DeterministicSeedHandler — forces CANON_SEED so terrain is identical
 * every save (zero dependence on random terrain).
 *
 * Port of dev.ergenverse.world.DeterministicSeedHandler.
 */
import { createNoise2D } from 'simplex-noise'

/** Hash a string into a 32-bit seed (FNV-1a). */
export function hashStringToSeed(s: string): number {
  let h = 0x811c9dc5
  for (let i = 0; i < s.length; i++) {
    h ^= s.charCodeAt(i)
    h = (h * 0x01000193) >>> 0
  }
  return h >>> 0
}

/** The canonical Planet Suzaku seed. NEVER change — saves depend on it. */
export const CANON_SEED_STRING = 'ergenverse.planet_suzaku.v1'
export const CANON_SEED = hashStringToSeed(CANON_SEED_STRING)

/** Mulberry32 PRNG — fast, deterministic, small-state. */
export function mulberry32(seed: number): () => number {
  let a = seed >>> 0
  return () => {
    a = (a + 0x6d2b79f5) >>> 0
    let t = a
    t = Math.imul(t ^ (t >>> 15), t | 1)
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61)
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

/** A canon-seeded noise2D factory (deterministic per name). */
export function canonNoise2D(name: string): (x: number, y: number) => number {
  const seed = hashStringToSeed(CANON_SEED_STRING + ':' + name)
  const rand = mulberry32(seed)
  return createNoise2D(rand)
}
