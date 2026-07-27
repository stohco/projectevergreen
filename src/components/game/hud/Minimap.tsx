'use client'

import { useEffect, useRef } from 'react'
import { useWorldStateQuery } from './WorldStateQuery'
import { PLANET_SUZAKU_PLACEMENT } from '@/engine/canon/PlanetSuzakuPlacement'
import { SEED_ACTORS, SEED_THREATS, SEED_VEINS } from './canonSeeds'

const SIZE = 200
const HALF = SIZE / 2
const MIN_ZOOM = 64
const MAX_ZOOM = 1024

/**
 * Minimap — top-right circular minimap.
 *
 * - 200×200 circular, north-up.
 * - Player at center (gold dot + direction arrow using cameraYaw).
 * - Canon locations as labeled icons (village/sect/city/landmark/ruin/cave).
 * - Beasts as red dots (size = threat level / count).
 * - NPCs as blue dots.
 * - Spirit veins as jade dots.
 * - Terrain elevation as concentric contour rings (procedural noise hint).
 * - Scroll to zoom 64–1024 blocks.
 * - Border with xianxia gold-jade ornament.
 */
export default function Minimap() {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const wrapRef = useRef<HTMLDivElement>(null)
  const player = useWorldStateQuery((s) => s.player)
  const zoom = useWorldStateQuery((s) => s.minimapZoom)
  const setZoom = useWorldStateQuery((s) => s.setMinimapZoom)
  const waypoint = useWorldStateQuery((s) => s.minimapWaypoint)
  const debug = useWorldStateQuery((s) => s.debug)

  // ── Redraw on every state change ────────────────────────────────────
  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    const dpr = Math.min(window.devicePixelRatio || 1, 2)
    canvas.width = SIZE * dpr
    canvas.height = SIZE * dpr
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
    ctx.clearRect(0, 0, SIZE, SIZE)

    // Clip to circle.
    ctx.save()
    ctx.beginPath()
    ctx.arc(HALF, HALF, HALF - 2, 0, Math.PI * 2)
    ctx.clip()

    // Background — radial ink wash.
    const bg = ctx.createRadialGradient(HALF, HALF, 0, HALF, HALF, HALF)
    bg.addColorStop(0, '#0a1a12')
    bg.addColorStop(0.7, '#050a08')
    bg.addColorStop(1, '#000000')
    ctx.fillStyle = bg
    ctx.fillRect(0, 0, SIZE, SIZE)

    // ── Terrain contour rings ─────────────────────────────────────────
    // Procedural elevation hint — concentric jade-tinted rings centered
    // on the player. Real contour data comes from the voxel terrain agent
    // later; this is a placeholder that reads as elevation.
    ctx.lineWidth = 0.5
    for (let i = 1; i <= 5; i++) {
      const r = (HALF - 4) * (i / 6)
      ctx.beginPath()
      ctx.arc(HALF, HALF, r, 0, Math.PI * 2)
      ctx.strokeStyle = `rgba(47, 143, 95, ${0.05 + i * 0.02})`
      ctx.stroke()
    }
    // Crosshair (N/E/S/W).
    ctx.strokeStyle = 'rgba(212, 175, 55, 0.18)'
    ctx.lineWidth = 0.5
    ctx.beginPath()
    ctx.moveTo(HALF, 2); ctx.lineTo(HALF, SIZE - 2)
    ctx.moveTo(2, HALF); ctx.lineTo(SIZE - 2, HALF)
    ctx.stroke()

    // World→minimap scale.
    const scale = (HALF - 4) / zoom
    const px = player.position[0]
    const pz = player.position[2]

    function w2m(x: number, z: number): [number, number] {
      // World delta → minimap coords. North-up: +z world = south on map.
      return [HALF + (x - px) * scale, HALF + (z - pz) * scale]
    }

    // ── Canon locations as labeled icons ──────────────────────────────
    for (const loc of PLANET_SUZAKU_PLACEMENT) {
      const [mx, my] = w2m(loc.position[0], loc.position[1])
      if (mx < -10 || mx > SIZE + 10 || my < -10 || my > SIZE + 10) continue
      drawLocationIcon(ctx, mx, my, loc.structureKind, loc.nameCn ?? loc.name)
    }

    // ── Spirit veins (jade dots) ──────────────────────────────────────
    for (const v of SEED_VEINS) {
      const [mx, my] = w2m(v.position[0], v.position[1])
      if (mx < 0 || mx > SIZE || my < 0 || my > SIZE) continue
      const r = 2 + v.quality * 0.5
      ctx.beginPath()
      ctx.arc(mx, my, r, 0, Math.PI * 2)
      const g = ctx.createRadialGradient(mx, my, 0, mx, my, r * 3)
      g.addColorStop(0, 'rgba(110, 231, 183, 0.95)')
      g.addColorStop(0.5, 'rgba(47, 143, 95, 0.6)')
      g.addColorStop(1, 'rgba(47, 143, 95, 0)')
      ctx.fillStyle = g
      ctx.fillRect(mx - r * 3, my - r * 3, r * 6, r * 6)
      ctx.beginPath()
      ctx.arc(mx, my, r * 0.5, 0, Math.PI * 2)
      ctx.fillStyle = '#a7f3d0'
      ctx.fill()
    }

    // ── Beasts (red dots, size = count) ───────────────────────────────
    for (const t of SEED_THREATS) {
      const [mx, my] = w2m(t.pos[0], t.pos[1])
      if (mx < 0 || mx > SIZE || my < 0 || my > SIZE) continue
      const r = 2 + Math.min(t.count, 6) * 0.6
      ctx.beginPath()
      ctx.arc(mx, my, r, 0, Math.PI * 2)
      const g = ctx.createRadialGradient(mx, my, 0, mx, my, r * 2.5)
      g.addColorStop(0, 'rgba(231, 76, 60, 0.95)')
      g.addColorStop(0.6, 'rgba(192, 57, 43, 0.5)')
      g.addColorStop(1, 'rgba(192, 57, 43, 0)')
      ctx.fillStyle = g
      ctx.fillRect(mx - r * 2.5, my - r * 2.5, r * 5, r * 5)
    }

    // ── NPCs (blue dots — soft jade-blue, NOT indigo) ─────────────────
    for (const a of SEED_ACTORS) {
      const [mx, my] = w2m(a.pos[0], a.pos[1])
      if (mx < 0 || mx > SIZE || my < 0 || my > SIZE) continue
      const hostile = a.hostility >= 60
      const color = hostile ? 'rgba(231, 76, 60, 0.9)' : 'rgba(110, 231, 183, 0.85)'
      ctx.beginPath()
      ctx.arc(mx, my, 2.2, 0, Math.PI * 2)
      ctx.fillStyle = color
      ctx.fill()
      ctx.strokeStyle = 'rgba(0,0,0,0.7)'
      ctx.lineWidth = 0.6
      ctx.stroke()
    }

    // ── Waypoint (from Codex click) ───────────────────────────────────
    if (waypoint) {
      const [mx, my] = w2m(waypoint.x, waypoint.z)
      const clampedX = Math.max(6, Math.min(SIZE - 6, mx))
      const clampedY = Math.max(6, Math.min(SIZE - 6, my))
      // Ring.
      ctx.beginPath()
      ctx.arc(clampedX, clampedY, 6, 0, Math.PI * 2)
      ctx.strokeStyle = waypoint.color ?? 'rgba(252, 211, 77, 0.9)'
      ctx.lineWidth = 1.4
      ctx.stroke()
      // Diamond.
      ctx.beginPath()
      ctx.moveTo(clampedX, clampedY - 4)
      ctx.lineTo(clampedX + 4, clampedY)
      ctx.lineTo(clampedX, clampedY + 4)
      ctx.lineTo(clampedX - 4, clampedY)
      ctx.closePath()
      ctx.fillStyle = waypoint.color ?? '#fcd34d'
      ctx.fill()
      // Label.
      if (waypoint.label) {
        ctx.font = '9px "Noto Serif SC", "Songti SC", serif'
        ctx.fillStyle = 'rgba(244, 236, 216, 0.9)'
        ctx.strokeStyle = 'rgba(0,0,0,0.85)'
        ctx.lineWidth = 2.5
        ctx.strokeText(waypoint.label.slice(0, 18), clampedX + 8, clampedY + 3)
        ctx.fillText(waypoint.label.slice(0, 18), clampedX + 8, clampedY + 3)
      }
    }

    // ── Player at center (gold dot + direction arrow) ─────────────────
    // North-up: cameraYaw rotates the arrow. Yaw 0 = facing -z (north).
    const yaw = (debug.cameraYaw * Math.PI) / 180
    ctx.save()
    ctx.translate(HALF, HALF)
    ctx.rotate(-yaw)
    // Glow.
    const pg = ctx.createRadialGradient(0, 0, 0, 0, 0, 12)
    pg.addColorStop(0, 'rgba(252, 211, 77, 0.6)')
    pg.addColorStop(1, 'rgba(252, 211, 77, 0)')
    ctx.fillStyle = pg
    ctx.fillRect(-12, -12, 24, 24)
    // Arrow.
    ctx.beginPath()
    ctx.moveTo(0, -6)
    ctx.lineTo(3.5, 4)
    ctx.lineTo(0, 2)
    ctx.lineTo(-3.5, 4)
    ctx.closePath()
    ctx.fillStyle = '#fcd34d'
    ctx.fill()
    ctx.strokeStyle = '#d4af37'
    ctx.lineWidth = 0.6
    ctx.stroke()
    ctx.restore()

    // North marker on the rim.
    ctx.font = 'bold 9px ui-monospace, monospace'
    ctx.fillStyle = 'rgba(212, 175, 55, 0.85)'
    ctx.textAlign = 'center'
    ctx.fillText('N', HALF, 11)
    ctx.textAlign = 'start'

    ctx.restore()

    // Rim ornament (drawn outside clip).
    ctx.beginPath()
    ctx.arc(HALF, HALF, HALF - 2, 0, Math.PI * 2)
    ctx.strokeStyle = 'rgba(212, 175, 55, 0.55)'
    ctx.lineWidth = 1
    ctx.stroke()
    // Tick marks at N/E/S/W.
    ctx.strokeStyle = 'rgba(212, 175, 55, 0.5)'
    for (let i = 0; i < 4; i++) {
      const a = (i * Math.PI) / 2 - Math.PI / 2
      ctx.beginPath()
      ctx.moveTo(HALF + Math.cos(a) * (HALF - 2), HALF + Math.sin(a) * (HALF - 2))
      ctx.lineTo(HALF + Math.cos(a) * (HALF - 6), HALF + Math.sin(a) * (HALF - 6))
      ctx.stroke()
    }
  }, [player, zoom, waypoint, debug.cameraYaw])

  // ── Continuous redraw for pulsing waypoint (when active) ────────────
  // The canvas redraws on player/zoom/yaw state changes; for the waypoint
  // pulse we use a CSS overlay ring instead, so no rAF needed here.

  // ── Wheel zoom ──────────────────────────────────────────────────────
  useEffect(() => {
    const wrap = wrapRef.current
    if (!wrap) return
    const onWheel = (e: WheelEvent) => {
      e.preventDefault()
      const step = e.shiftKey ? 32 : 64
      const next = e.deltaY > 0 ? zoom + step : zoom - step
      setZoom(Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, next)))
    }
    wrap.addEventListener('wheel', onWheel, { passive: false })
    return () => wrap.removeEventListener('wheel', onWheel as EventListener)
  }, [zoom, setZoom])

  return (
    <div
      style={{
        position: 'absolute',
        top: 16,
        right: 16,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 4,
      }}
    >
      <div ref={wrapRef} className="minimap-wrap" style={{ cursor: 'crosshair' }}>
        <canvas
          ref={canvasRef}
          style={{ width: SIZE, height: SIZE, display: 'block' }}
          aria-label="Minimap"
        />
        <div className="minimap-ornament" aria-hidden />
      </div>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 8,
          fontFamily: 'var(--font-mono)',
          fontSize: 10,
          color: 'var(--ivory-dim)',
          letterSpacing: '0.08em',
        }}
      >
        <span style={{ color: 'var(--gold-300)' }}>R</span>
        <span>{zoom}b</span>
        <span style={{ opacity: 0.4 }}>·</span>
        <span style={{ color: 'var(--jade-300)' }}>{player.position[0].toFixed(0)}, {player.position[2].toFixed(0)}</span>
      </div>
    </div>
  )
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Location icon renderer                                                 */
/* ─────────────────────────────────────────────────────────────────────── */

function drawLocationIcon(
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  kind: string,
  label: string,
): void {
  ctx.save()
  ctx.translate(x, y)
  // Soft glow.
  const g = ctx.createRadialGradient(0, 0, 0, 0, 0, 8)
  g.addColorStop(0, 'rgba(212, 175, 55, 0.45)')
  g.addColorStop(1, 'rgba(212, 175, 55, 0)')
  ctx.fillStyle = g
  ctx.fillRect(-8, -8, 16, 16)

  ctx.fillStyle = '#fcd34d'
  ctx.strokeStyle = '#d4af37'
  ctx.lineWidth = 0.8

  switch (kind) {
    case 'village':
      // House — square + triangle roof.
      ctx.beginPath()
      ctx.moveTo(-3, 1); ctx.lineTo(3, 1); ctx.lineTo(3, -2); ctx.lineTo(0, -4); ctx.lineTo(-3, -2); ctx.closePath()
      ctx.fill(); ctx.stroke()
      break
    case 'sect':
      // Mountain — triangle.
      ctx.beginPath()
      ctx.moveTo(-4, 3); ctx.lineTo(0, -4); ctx.lineTo(4, 3); ctx.closePath()
      ctx.fill(); ctx.stroke()
      break
    case 'city':
      // Wall — three battlements.
      ctx.beginPath()
      ctx.moveTo(-4, 2); ctx.lineTo(-4, -2); ctx.lineTo(-2.5, -2); ctx.lineTo(-2.5, -3.5); ctx.lineTo(-1, -3.5); ctx.lineTo(-1, -2)
      ctx.lineTo(1, -2); ctx.lineTo(1, -3.5); ctx.lineTo(2.5, -3.5); ctx.lineTo(2.5, -2); ctx.lineTo(4, -2); ctx.lineTo(4, 2); ctx.closePath()
      ctx.fill(); ctx.stroke()
      break
    case 'ruin':
    case 'cave':
      // Diamond (ruin / cave).
      ctx.beginPath()
      ctx.moveTo(0, -4); ctx.lineTo(4, 0); ctx.lineTo(0, 4); ctx.lineTo(-4, 0); ctx.closePath()
      ctx.fill(); ctx.stroke()
      break
    case 'landmark':
    default:
      // Star — 5-point.
      ctx.beginPath()
      for (let i = 0; i < 5; i++) {
        const a = (i * 2 * Math.PI) / 5 - Math.PI / 2
        const r = i % 1 === 0 ? 4 : 1.6
        ctx.lineTo(Math.cos(a) * 4, Math.sin(a) * 4)
        const a2 = a + Math.PI / 5
        ctx.lineTo(Math.cos(a2) * 1.6, Math.sin(a2) * 1.6)
      }
      ctx.closePath()
      ctx.fill(); ctx.stroke()
      break
  }

  // Label.
  ctx.font = '9px "Noto Serif SC", "Songti SC", serif'
  ctx.fillStyle = 'rgba(244, 236, 216, 0.92)'
  ctx.strokeStyle = 'rgba(0, 0, 0, 0.85)'
  ctx.lineWidth = 2.5
  ctx.strokeText(label, 6, 3)
  ctx.fillText(label, 6, 3)

  ctx.restore()
}
