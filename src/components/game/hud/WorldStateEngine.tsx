'use client'

import { Skull, Wind, Sparkles, MapPin } from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import { useWorldStateQuery, getRealm } from './WorldStateQuery'

/**
 * WorldStateEngine — bottom-right world-state panel.
 *
 * Shows:
 *   - current location (English + Chinese)
 *   - top 5 nearby canon actors by distance (with realm + hostility dot)
 *   - local threats (beast + count within 100 blocks)
 *   - spirit-vein proximity indicator (jade dot if within 50 blocks)
 *
 * Updates every 2s via useEngine (debounced scan already runs in useEngine).
 */
export default function WorldStateEngine() {
  const world = useWorldStateQuery((s) => s.world)
  const player = useWorldStateQuery((s) => s.player)

  const loc = world.currentLocation
  const actors = world.nearbyActors
  const threats = world.nearbyThreats
  const vein = world.spiritVeinNear

  return (
    <motion.div
      className="hud-panel hud-ornate"
      initial={{ opacity: 0, x: 24 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.32, ease: [0.22, 1, 0.36, 1] }}
      style={{
        position: 'absolute',
        bottom: 16,
        right: 16,
        width: 280,
        padding: 12,
        borderRadius: 2,
      }}
    >
      <div className="silk-shimmer" aria-hidden />

      {/* Header */}
      <div className="flex items-center gap-2">
        <MapPin size={12} style={{ color: 'var(--gold-400)' }} />
        <span
          className="uppercase"
          style={{ fontSize: 9, letterSpacing: '0.28em', color: 'var(--gold-300)', opacity: 0.9 }}
        >
          World State
        </span>
      </div>

      {/* Location */}
      <div className="mt-2">
        <div
          className="font-cn"
          style={{ fontSize: 16, color: 'var(--ivory)', letterSpacing: '0.04em' }}
        >
          {loc?.nameCn ?? '—'}
        </div>
        <div
          className="font-en italic"
          style={{ fontSize: 12, color: 'var(--ivory-dim)', letterSpacing: '0.04em' }}
        >
          {loc?.name ?? '—'}
        </div>
        {loc?.biome && (
          <div
            className="mt-1 inline-block rounded px-1.5 py-0.5"
            style={{
              fontSize: 9,
              letterSpacing: '0.2em',
              textTransform: 'uppercase',
              color: 'var(--jade-300)',
              background: 'rgba(47,143,95,0.12)',
              border: '1px solid rgba(47,143,95,0.3)',
            }}
          >
            {loc.biome}
          </div>
        )}
      </div>

      <div className="hud-divider" />

      {/* Nearby actors */}
      <div>
        <div
          className="mb-1 flex items-center justify-between uppercase"
          style={{ fontSize: 9, letterSpacing: '0.22em', color: 'var(--jade-300)', opacity: 0.85 }}
        >
          <span>Nearby Cultivators</span>
          <span style={{ fontFamily: 'var(--font-mono)', opacity: 0.7 }}>{actors.length}/5</span>
        </div>
        <div className="hud-scroll" style={{ maxHeight: 140, overflowY: 'auto', paddingRight: 4 }}>
          <AnimatePresence initial={false}>
            {actors.length === 0 && (
              <motion.div
                key="empty"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                style={{ fontSize: 11, color: 'var(--ivory-dim)', fontStyle: 'italic' }}
              >
                Spirit sense finds no one nearby.
              </motion.div>
            )}
            {actors.map((a) => {
              const r = getRealm(a.realm)
              const hostilityColor =
                a.hostility >= 70 ? 'var(--vermillion-bright)'
                : a.hostility >= 40 ? 'var(--gold-300)'
                : 'var(--jade-300)'
              return (
                <motion.div
                  key={a.id}
                  layout
                  initial={{ opacity: 0, x: 12 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -8 }}
                  transition={{ duration: 0.2 }}
                  className="mb-1 flex items-center gap-2 rounded px-1 py-0.5"
                  style={{ background: 'rgba(0,0,0,0.25)' }}
                >
                  <span
                    className="hostility-dot"
                    style={{ color: hostilityColor, background: hostilityColor }}
                    title={`Hostility ${a.hostility}/100`}
                  />
                  <div className="min-w-0 flex-1">
                    <div className="flex items-baseline gap-1.5">
                      <span
                        className="font-cn truncate"
                        style={{ fontSize: 11, color: 'var(--ivory)' }}
                      >
                        {a.nameCn ?? a.name}
                      </span>
                      {a.nameCn && (
                        <span
                          className="font-en truncate"
                          style={{ fontSize: 9, color: 'var(--ivory-dim)', fontStyle: 'italic' }}
                        >
                          {a.name}
                        </span>
                      )}
                    </div>
                    <div
                      className="flex items-center gap-1.5"
                      style={{ fontSize: 9, color: 'var(--ivory-dim)', letterSpacing: '0.04em' }}
                    >
                      <span className="font-cn">{r.short}</span>
                      <span>·</span>
                      <span className="truncate">{a.faction}</span>
                    </div>
                  </div>
                  <span
                    style={{ fontFamily: 'var(--font-mono)', fontSize: 9, color: 'var(--jade-300)' }}
                  >
                    {a.distance}b
                  </span>
                </motion.div>
              )
            })}
          </AnimatePresence>
        </div>
      </div>

      {/* Threats */}
      {threats.length > 0 && (
        <>
          <div className="hud-divider" />
          <div>
            <div
              className="mb-1 flex items-center gap-1 uppercase"
              style={{ fontSize: 9, letterSpacing: '0.22em', color: 'var(--vermillion-bright)', opacity: 0.9 }}
            >
              <Skull size={10} />
              Local Threats
            </div>
            <div className="space-y-0.5">
              {threats.map((t, i) => (
                <div key={i} className="flex items-center justify-between" style={{ fontSize: 11 }}>
                  <span style={{ color: 'var(--ivory)' }}>
                    <span className="font-cn">{t.beastNameCn}</span>{' '}
                    <span style={{ color: 'var(--ivory-dim)', fontSize: 9 }}>{t.beastName}</span>
                  </span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span
                      className="rounded px-1"
                      style={{
                        fontSize: 9,
                        fontFamily: 'var(--font-mono)',
                        color: 'var(--vermillion-bright)',
                        background: 'rgba(231,76,60,0.12)',
                        border: '1px solid rgba(231,76,60,0.35)',
                      }}
                    >
                      ×{t.count}
                    </span>
                    <span style={{ fontFamily: 'var(--font-mono)', fontSize: 9, color: 'var(--ivory-dim)' }}>
                      {t.distance}b
                    </span>
                  </span>
                </div>
              ))}
            </div>
          </div>
        </>
      )}

      {/* Spirit vein proximity */}
      <div className="hud-divider" />
      <div className="flex items-center justify-between" style={{ fontSize: 10 }}>
        <span
          className="flex items-center gap-1.5 uppercase"
          style={{ letterSpacing: '0.18em', color: 'var(--jade-300)', opacity: 0.85 }}
        >
          <Sparkles size={10} />
          Spirit Vein
        </span>
        {vein ? (
          <span className="flex items-center gap-1.5" style={{ color: 'var(--ivory)' }}>
            <span
              className="hostility-dot"
              style={{ color: 'var(--jade-300)', background: 'var(--jade-300)' }}
            />
            <span className="font-cn" style={{ fontSize: 10 }}>{vein.name}</span>
            <span style={{ fontFamily: 'var(--font-mono)', fontSize: 9, color: 'var(--ivory-dim)' }}>
              {vein.distance}b · Q{vein.quality}
            </span>
          </span>
        ) : (
          <span style={{ color: 'var(--ivory-dim)', fontStyle: 'italic', fontSize: 10 }}>
            none within 50 blocks
          </span>
        )}
      </div>

      {/* Weather + time */}
      <div className="mt-2 flex items-center justify-between" style={{ fontSize: 10, color: 'var(--ivory-dim)' }}>
        <span className="flex items-center gap-1 uppercase" style={{ letterSpacing: '0.18em' }}>
          <Wind size={10} /> {world.weather}
        </span>
        <span style={{ fontFamily: 'var(--font-mono)' }}>
          Day {world.day} · {formatTime(world.time)}
        </span>
      </div>
    </motion.div>
  )
}

function formatTime(t: number): string {
  // 0 = midnight, 0.5 = noon
  const hours24 = (t * 24 + 6) % 24 // shift so 0 = 6am dawn
  const h = Math.floor(hours24)
  const m = Math.floor((hours24 - h) * 60)
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}`
}
