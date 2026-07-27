'use client'

import { useWorldStateQuery } from './WorldStateQuery'

/**
 * DebugOverlay — top-center debug panel (toggle with F3).
 *
 * Monospace, semi-transparent black background. Shows fps / frame-time /
 * chunks / player coords / camera direction / biome / memory / entities /
 * triangles / draw calls.
 */
export default function DebugOverlay() {
  const open = useWorldStateQuery((s) => s.debugOpen)
  const debug = useWorldStateQuery((s) => s.debug)
  const player = useWorldStateQuery((s) => s.player)
  const world = useWorldStateQuery((s) => s.world)

  if (!open) return null

  const fps = debug.fps
  const fpsClass = fps >= 55 ? 'good' : fps >= 30 ? '' : 'warn'
  const tri = debug.triangles >= 1000 ? `${(debug.triangles / 1000).toFixed(1)}k` : `${debug.triangles}`

  return (
    <div
      className="debug-overlay fade-in"
      style={{
        position: 'absolute',
        top: 8,
        left: '50%',
        transform: 'translateX(-50%)',
        padding: '8px 12px',
        borderRadius: 2,
        pointerEvents: 'none',
        minWidth: 280,
      }}
    >
      <div className="debug-row">
        <span className="debug-label">FPS</span>
        <span className={`debug-value ${fpsClass}`}>{fps} <span style={{ opacity: 0.5 }}>/ 60</span></span>
      </div>
      <div className="debug-row">
        <span className="debug-label">Frame</span>
        <span className="debug-value">{debug.frameTime.toFixed(1)} ms</span>
      </div>
      <div className="debug-row">
        <span className="debug-label">Chunks</span>
        <span className="debug-value">{debug.chunks}</span>
      </div>
      <div className="debug-row">
        <span className="debug-label">Draw calls</span>
        <span className="debug-value">{debug.drawCalls}</span>
      </div>
      <div className="debug-row">
        <span className="debug-label">Triangles</span>
        <span className="debug-value">{tri}</span>
      </div>
      <div className="debug-row">
        <span className="debug-label">Entities</span>
        <span className="debug-value">{debug.entities}</span>
      </div>
      <div className="debug-row">
        <span className="debug-label">Mem (JS)</span>
        <span className="debug-value">{debug.memoryMb > 0 ? `${debug.memoryMb} MB` : 'n/a'}</span>
      </div>
      <div className="hud-divider" style={{ margin: '4px 0' }} />
      <div className="debug-row">
        <span className="debug-label">Pos</span>
        <span className="debug-value">
          {player.position[0].toFixed(1)}, {player.position[1].toFixed(1)}, {player.position[2].toFixed(1)}
        </span>
      </div>
      <div className="debug-row">
        <span className="debug-label">Cam yaw</span>
        <span className="debug-value">{debug.cameraYaw.toFixed(1)}°</span>
      </div>
      <div className="debug-row">
        <span className="debug-label">Cam pitch</span>
        <span className="debug-value">{debug.cameraPitch.toFixed(1)}°</span>
      </div>
      <div className="debug-row">
        <span className="debug-label">Biome</span>
        <span className="debug-value">{world.biome}</span>
      </div>
      <div className="debug-row">
        <span className="debug-label">Realm</span>
        <span className="debug-value">{player.realmId} ({player.qi.toFixed(0)}/{player.maxQi} qi)</span>
      </div>
      <div className="debug-row">
        <span className="debug-label">Day</span>
        <span className="debug-value">{world.day} · {Math.floor(((world.time * 24) + 6) % 24).toString().padStart(2, '0')}:{Math.floor(((((world.time * 24) + 6) % 24) % 1) * 60).toString().padStart(2, '0')}</span>
      </div>
    </div>
  )
}
