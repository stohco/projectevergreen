/**
 * useEngine — the HUD's bridge to the Three.js engine.
 *
 * The real engine (owned by another sub-agent) will register a bridge on
 * `globalThis.__ergenBridge` with methods like:
 *   getState(): EngineState
 *   saveWorld(key): void
 *   loadWorld(key): void
 *   onTick(cb): unsubscribe
 *
 * Until that bridge exists, this hook runs a SIMULATION that:
 *   - advances the in-game clock
 *   - regenerates qi (faster if Qi Meditation is active)
 *   - drains qi if Sword Flight is active
 *   - ticks spell cooldowns
 *   - computes nearby canon actors + threats + spirit-vein proximity from
 *     the player's current position against PLANET_SUZAKU_PLACEMENT
 *   - derives plausible debug values (fps from rAF, simulated chunks/tris)
 *
 * This way the HUD has visible life even before the engine is wired.
 *
 * Canon fidelity:
 *   - The nearby-actors seed list uses only canon characters (Wang Lin's
 *     family + Heng Yue Sect elders) — mod-original placement, but the
 *     names come from RICanonicalDatabase.
 *   - No invented chapter citations.
 */
'use client'

import { useEffect, useRef } from 'react'
import {
  useWorldStateQuery,
  SPELL_SLOTS,
  type NearbyActor,
  type NearbyThreat,
  type DebugInfo,
} from './WorldStateQuery'
import { PLANET_SUZAKU_PLACEMENT } from '@/engine/canon/PlanetSuzakuPlacement'
import { SEED_ACTORS, SEED_THREATS, SEED_VEINS } from './canonSeeds'

/* ─────────────────────────────────────────────────────────────────────── */
/*  Math helpers                                                           */
/* ─────────────────────────────────────────────────────────────────────── */

function dist2D(a: [number, number], b: [number, number]): number {
  const dx = a[0] - b[0]
  const dz = a[1] - b[1]
  return Math.sqrt(dx * dx + dz * dz)
}

function clamp(v: number, lo: number, hi: number): number {
  return Math.max(lo, Math.min(hi, v))
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Hook                                                                   */
/* ─────────────────────────────────────────────────────────────────────── */

export function useEngine(): void {
  const fpsRef = useRef<{ frames: number; lastTs: number; fps: number; frameTime: number }>({
    frames: 0,
    lastTs: performance.now(),
    fps: 60,
    frameTime: 16.6,
  })

  // ── Frame loop — fps / frame-time / draw-call telemetry ──────────────
  useEffect(() => {
    let rafId = 0
    const tick = () => {
      rafId = requestAnimationFrame(tick)
      const now = performance.now()
      const dt = now - fpsRef.current.lastTs
      fpsRef.current.lastTs = now
      fpsRef.current.frames++
      fpsRef.current.frameTime = fpsRef.current.frameTime * 0.9 + dt * 0.1
      // Once a second, push fps to the store.
      if (fpsRef.current.frames % 30 === 0) {
        fpsRef.current.fps = Math.round(1000 / Math.max(1, fpsRef.current.frameTime))
      }
    }
    rafId = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(rafId)
  }, [])

  // ── 2 Hz engine simulation tick (qi regen, spell cooldowns, debug) ───
  useEffect(() => {
    const interval = setInterval(() => {
      const s = useWorldStateQuery.getState()
      const p = s.player
      const dt = 0.5 // seconds

      // Bridge takes priority if registered.
      const bridge = (globalThis as any).__ergenBridge
      if (bridge?.getState) {
        try {
          const es = bridge.getState()
          if (es?.player) s.updatePlayer(es.player)
          if (es?.world) s.updateWorld(es.world)
          if (es?.debug) s.updateDebug(es.debug)
          return
        } catch {
          /* fall through to simulation */
        }
      }

      // ── Qi regen / drain ─────────────────────────────────────────────
      let qi = p.qi
      let regen = p.qiRegenerating
      const meditating = !!s.spellActive[4]
      const flying = !!s.spellActive[2]
      if (flying) {
        qi = Math.max(0, qi - 2 * dt)
        if (qi <= 0) {
          s.setSpellActive(2, false)
        }
      } else if (regen) {
        const rate = meditating ? 9 : 3 // 3x meditation
        qi = Math.min(p.maxQi, qi + rate * dt)
      }
      if (qi !== p.qi || regen !== p.qiRegenerating) {
        s.updatePlayer({ qi, qiRegenerating: regen })
      }

      // ── Time advance (1 sim sec = ~2 in-game min, 24h day in ~12 min) ─
      const time = (s.world.time + (dt / 720)) % 1
      const day = time < s.world.time ? s.world.day + 1 : s.world.day
      s.updateWorld({ time, day })

      // ── Debug info ───────────────────────────────────────────────────
      const dbg: Partial<DebugInfo> = {
        fps: fpsRef.current.fps,
        frameTime: Math.round(fpsRef.current.frameTime * 10) / 10,
        // Plausible simulated values until the engine reports real ones.
        chunks: clamp(Math.round(s.settings.renderDistance * s.settings.renderDistance * 4), 0, 4096),
        cameraYaw: (s.debug.cameraYaw + 0.3) % 360,
        cameraPitch: clamp(s.debug.cameraPitch + Math.sin(Date.now() / 4000) * 0.5, -45, 45),
        memoryMb: typeof performance !== 'undefined' && (performance as any).memory
          ? Math.round(((performance as any).memory.usedJSHeapSize / 1048576) * 10) / 10
          : 0,
        entities: s.world.nearbyActors.length + s.world.nearbyThreats.reduce((a, t) => a + t.count, 0),
        triangles: clamp(80000 + Math.round(Math.sin(Date.now() / 3000) * 12000), 0, 9999999),
        drawCalls: clamp(180 + Math.round(Math.sin(Date.now() / 2000) * 30), 0, 9999),
      }
      s.updateDebug(dbg)
    }, 500)
    return () => clearInterval(interval)
  }, [])

  // ── 0.5 Hz world-state scan (actors / threats / veins / location) ────
  useEffect(() => {
    const scan = () => {
      const s = useWorldStateQuery.getState()
      const pos = s.player.position
      const p2: [number, number] = [pos[0], pos[2]]

      // Nearby actors — top 5 by distance, within spirit-sense range.
      const actorRange = s.player.spiritSenseRange
      const actors: NearbyActor[] = SEED_ACTORS.map((a) => ({
        id: a.id,
        name: a.name,
        nameCn: a.nameCn,
        realm: a.realm,
        faction: a.faction,
        hostility: a.hostility,
        distance: Math.round(dist2D(p2, a.pos)),
      }))
        .filter((a) => a.distance <= actorRange * 4)
        .sort((a, b) => a.distance - b.distance)
        .slice(0, 5)
      s.updateWorld({ nearbyActors: actors })

      // Nearby threats — within 100 blocks.
      const threats: NearbyThreat[] = SEED_THREATS.map((t) => ({
        beastName: t.beastName,
        beastNameCn: t.beastNameCn,
        rank: t.rank,
        count: t.count,
        distance: Math.round(dist2D(p2, t.pos)),
      }))
        .filter((t) => t.distance <= 100)
        .sort((a, b) => a.distance - b.distance)
      s.updateWorld({ nearbyThreats: threats })

      // Spirit vein proximity — within 50 blocks.
      let vein = null as null | { name: string; distance: number; quality: number; element: string }
      for (const v of SEED_VEINS) {
        const d = Math.round(dist2D(p2, v.position))
        if (d <= 50) {
          if (!vein || d < vein.distance) {
            vein = { name: v.name, distance: d, quality: v.quality, element: v.element }
          }
        }
      }
      s.updateWorld({ spiritVeinNear: vein })

      // Current location — closest placed location within its radius.
      let best: { name: string; nameCn?: string; biome: string; d: number } | null = null
      for (const loc of PLANET_SUZAKU_PLACEMENT) {
        const d = dist2D(p2, loc.position)
        if (d <= loc.radius + 40) {
          if (!best || d < best.d) {
            best = { name: loc.name, nameCn: loc.nameCn, biome: loc.biome, d }
          }
        }
      }
      s.updateWorld({
        currentLocation: best
          ? { name: best.name, nameCn: best.nameCn, biome: best.biome }
          : { name: 'Wilderness of Zhao Country', nameCn: '赵国荒野', biome: s.world.biome },
        biome: best?.biome ?? s.world.biome,
      })
    }
    scan() // initial
    const id = setInterval(scan, 2000)
    return () => clearInterval(id)
  }, [])
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Spell activation — called by HUD keyboard / click handlers             */
/* ─────────────────────────────────────────────────────────────────────── */

export function activateSpell(slot: number): void {
  const s = useWorldStateQuery.getState()
  const def = SPELL_SLOTS.find((sp) => sp.id === slot)
  if (!def || def.empty) return

  // Cooldown gate.
  const readyAt = s.spellCooldowns[slot] ?? 0
  if (readyAt > Date.now()) return

  // Qi gate.
  if (def.cost > 0 && s.player.qi < def.cost) {
    // Cannot cast — QiBar will visually flash via the qi-critical state.
    return
  }

  if (def.toggle) {
    const next = !s.spellActive[slot]
    s.setSpellActive(slot, next)
    if (next && def.cost > 0) {
      s.updatePlayer({ qi: s.player.qi - def.cost })
    }
    return
  }

  // Instant spell.
  if (def.cost > 0) {
    s.updatePlayer({ qi: s.player.qi - def.cost })
  }
  if (def.cooldown > 0) {
    s.triggerSpellCooldown(slot, def.cooldown)
  }

  // Spell-specific side-effects:
  if (slot === 3) {
    // Divine Sense — temporarily double spirit-sense range for 6s.
    const baseRange = 64
    s.updatePlayer({ spiritSenseRange: baseRange * 2 })
    setTimeout(() => {
      useWorldStateQuery.getState().updatePlayer({ spiritSenseRange: baseRange })
    }, 6000)
  }
}
