'use client'

import { useState } from 'react'
import { Heart, Eye, ChevronRight, X } from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import {
  useWorldStateQuery,
  getRealm,
  getNextRealm,
  qiPercent,
  healthHearts,
} from './WorldStateQuery'

/**
 * CultivationPanel — top-left cultivation panel.
 *
 * Compact by default, expands on hover. Click the realm badge to open the
 * full CharacterSheet (a placeholder modal for now; another agent owns the
 * real CharacterSheet).
 */
export default function CultivationPanel() {
  const player = useWorldStateQuery((s) => s.player)
  const [hovered, setHovered] = useState(false)
  const [sheetOpen, setSheetOpen] = useState(false)

  const realm = getRealm(player.realmId)
  const next = getNextRealm(player.realmId)
  const pct = qiPercent(player)
  const hearts = healthHearts(player)

  return (
    <>
      <motion.div
        className="hud-panel hud-ornate"
        onHoverStart={() => setHovered(true)}
        onHoverEnd={() => setHovered(false)}
        animate={{
          width: hovered ? 320 : 256,
        }}
        transition={{ duration: 0.32, ease: [0.22, 1, 0.36, 1] }}
        style={{
          position: 'absolute',
          top: 16,
          left: 16,
          padding: 14,
          borderRadius: 2,
          cursor: 'default',
        }}
      >
        <div className="silk-shimmer" aria-hidden />

        {/* Header — name + realm */}
        <div className="flex items-start justify-between gap-2">
          <div className="min-w-0">
            <div className="flex items-baseline gap-2">
              <span
                className="font-cn truncate"
                style={{ fontSize: 18, color: 'var(--ivory)', fontWeight: 600, letterSpacing: '0.04em' }}
              >
                {player.nameCn}
              </span>
              <span
                className="font-en truncate"
                style={{ fontSize: 13, color: 'var(--gold-300)', fontStyle: 'italic' }}
              >
                {player.name}
              </span>
            </div>
            <div className="mt-1 flex items-center gap-2">
              <button
                type="button"
                className="realm-badge cursor-pointer"
                onClick={() => setSheetOpen(true)}
                title="Open character sheet"
              >
                <span className="font-cn">{realm.short}</span>
                <span className="font-en" style={{ opacity: 0.85 }}>{realm.en}</span>
                <ChevronRight size={11} style={{ opacity: 0.6 }} />
              </button>
            </div>
          </div>
        </div>

        <div className="hud-divider" />

        {/* Realm progression */}
        <div className="mb-2">
          <div className="mb-1 flex items-center justify-between" style={{ fontSize: 10, letterSpacing: '0.18em' }}>
            <span className="uppercase" style={{ color: 'var(--jade-300)', opacity: 0.85 }}>
              Realm Progress
            </span>
            <span style={{ fontFamily: 'var(--font-mono)', color: 'var(--ivory)', opacity: 0.85 }}>
              {Math.round(player.realmProgress * 100)}%
            </span>
          </div>
          <div className="realm-progress-track" style={{ borderRadius: 2 }}>
            <div
              className="realm-progress-fill"
              style={{ width: `${Math.round(player.realmProgress * 100)}%`, borderRadius: 2 }}
            />
          </div>
          <AnimatePresence>
            {hovered && next && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                className="mt-1"
                style={{ fontSize: 10, color: 'var(--ivory-dim)', letterSpacing: '0.08em' }}
              >
                Next · <span className="font-cn">{next.cn}</span> {next.en}
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* Qi bar (linked to global QiBar visual but compact here) */}
        <div className="mb-2">
          <div className="mb-1 flex items-center justify-between" style={{ fontSize: 10, letterSpacing: '0.18em' }}>
            <span className="uppercase" style={{ color: 'var(--jade-300)', opacity: 0.85 }}>
              元神 · Qi
            </span>
            <span style={{ fontFamily: 'var(--font-mono)', color: 'var(--ivory)', opacity: 0.85 }}>
              {Math.round(player.qi)} / {Math.round(player.maxQi)}
            </span>
          </div>
          <div className="qi-bar-track" style={{ height: 8, borderRadius: 1 }}>
            <div
              className={`qi-bar-fill ${player.qiRegenerating ? 'regen' : ''}`}
              style={{ width: `${pct}%`, borderRadius: 1 }}
            />
          </div>
        </div>

        {/* Health + spirit sense row */}
        <div className="flex items-center justify-between">
          {/* Hearts */}
          <div className="flex items-center gap-1">
            {Array.from({ length: hearts.total }).map((_, i) => {
              const filled = i < hearts.filled
              return (
                <Heart
                  key={i}
                  size={14}
                  className={`heart ${filled ? '' : 'empty'}`}
                  fill={filled ? 'var(--vermillion-bright)' : 'transparent'}
                  color={filled ? 'var(--vermillion-bright)' : 'rgba(231,76,60,0.4)'}
                  strokeWidth={2}
                />
              )
            })}
            <span
              className="ml-2"
              style={{ fontFamily: 'var(--font-mono)', fontSize: 10, color: 'var(--ivory)', opacity: 0.7 }}
            >
              {player.health}/{player.maxHealth}
            </span>
          </div>

          {/* Spirit sense range */}
          <div
            className="flex items-center gap-1"
            title="Spirit sense (神识) range — how far you can perceive entities"
            style={{ fontSize: 10, color: 'var(--jade-300)', opacity: 0.9 }}
          >
            <Eye size={11} />
            <span className="font-cn">神识</span>
            <span style={{ fontFamily: 'var(--font-mono)' }}>{player.spiritSenseRange}b</span>
          </div>
        </div>

        {/* Hover-expanded extras */}
        <AnimatePresence>
          {hovered && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              exit={{ opacity: 0, height: 0 }}
              className="mt-2"
              style={{ overflow: 'hidden' }}
            >
              <div className="hud-divider" />
              <div
                className="grid grid-cols-2 gap-x-3 gap-y-1"
                style={{ fontSize: 10, color: 'var(--ivory-dim)', letterSpacing: '0.04em' }}
              >
                <span>Faction</span>
                <span style={{ color: 'var(--ivory)' }}>{player.faction}</span>
                <span>Hostility</span>
                <span style={{ color: 'var(--ivory)' }}>{player.hostility}/100</span>
                <span>Position</span>
                <span style={{ color: 'var(--ivory)', fontFamily: 'var(--font-mono)' }}>
                  {player.position[0].toFixed(0)}, {player.position[1].toFixed(0)}, {player.position[2].toFixed(0)}
                </span>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>

      {/* Character sheet modal (placeholder) */}
      <AnimatePresence>
        {sheetOpen && <CharacterSheet onClose={() => setSheetOpen(false)} />}
      </AnimatePresence>
    </>
  )
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Character sheet modal — placeholder (full sheet owned by another agent) */
/* ─────────────────────────────────────────────────────────────────────── */

function CharacterSheet({ onClose }: { onClose: () => void }) {
  const player = useWorldStateQuery((s) => s.player)
  const realm = getRealm(player.realmId)
  const next = getNextRealm(player.realmId)
  const hearts = healthHearts(player)

  return (
    <motion.div
      className="fixed inset-0 z-50 flex items-center justify-center"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      style={{ background: 'rgba(0,0,0,0.7)', pointerEvents: 'auto' }}
      onClick={onClose}
    >
      <motion.div
        className="hud-panel hud-ornate-4"
        initial={{ scale: 0.96, y: 12 }}
        animate={{ scale: 1, y: 0 }}
        exit={{ scale: 0.96, y: 12 }}
        transition={{ duration: 0.24, ease: [0.22, 1, 0.36, 1] }}
        onClick={(e) => e.stopPropagation()}
        style={{ width: 560, padding: 24, borderRadius: 2 }}
      >
        <span className="hud-corner tl" />
        <span className="hud-corner tr" />
        <span className="hud-corner bl" />
        <span className="hud-corner br" />

        <div className="flex items-start justify-between">
          <div>
            <h2
              className="font-cn"
              style={{ fontSize: 28, color: 'var(--ivory)', letterSpacing: '0.06em', marginBottom: 2 }}
            >
              {player.nameCn}
            </h2>
            <p
              className="font-en italic"
              style={{ fontSize: 16, color: 'var(--gold-300)', letterSpacing: '0.04em' }}
            >
              {player.name}
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded p-1 transition hover:bg-white/10"
            aria-label="Close character sheet"
            style={{ color: 'var(--ivory-dim)' }}
          >
            <X size={18} />
          </button>
        </div>

        <div className="hud-divider" style={{ margin: '14px 0' }} />

        <div className="grid grid-cols-2 gap-x-8 gap-y-3" style={{ fontSize: 13 }}>
          <Row label="Realm (境界)">
            <span className="font-cn">{realm.cn}</span> {realm.en}
          </Row>
          <Row label="Realm Progress">
            {Math.round(player.realmProgress * 100)}%
            {next && (
              <span style={{ color: 'var(--ivory-dim)' }}>
                {' '}→ <span className="font-cn">{next.cn}</span> {next.en}
              </span>
            )}
          </Row>
          <Row label="Qi (元神)">
            {Math.round(player.qi)} / {Math.round(player.maxQi)}
          </Row>
          <Row label="Health">
            {Array.from({ length: hearts.total }).map((_, i) => (
              <Heart
                key={i}
                size={13}
                className={`heart ${i < hearts.filled ? '' : 'empty'}`}
                fill={i < hearts.filled ? 'var(--vermillion-bright)' : 'transparent'}
                color={i < hearts.filled ? 'var(--vermillion-bright)' : 'rgba(231,76,60,0.4)'}
                style={{ display: 'inline-block', marginRight: 2 }}
              />
            ))}
          </Row>
          <Row label="Spirit Sense (神识)">
            {player.spiritSenseRange} blocks
          </Row>
          <Row label="Faction (道统)">
            {player.faction}
          </Row>
          <Row label="Hostility">
            <span
              style={{
                color:
                  player.hostility > 60 ? 'var(--vermillion-bright)'
                  : player.hostility > 30 ? 'var(--gold-300)'
                  : 'var(--jade-300)',
              }}
            >
              {player.hostility}/100
            </span>
          </Row>
          <Row label="Position">
            <span style={{ fontFamily: 'var(--font-mono)' }}>
              {player.position[0].toFixed(0)}, {player.position[1].toFixed(0)}, {player.position[2].toFixed(0)}
            </span>
          </Row>
        </div>

        <p
          className="mt-4 font-en italic"
          style={{ fontSize: 11, color: 'var(--ivory-dim)', letterSpacing: '0.04em' }}
        >
          Full character sheet — attributes, techniques inventory, karmic ledger — TBD by a later CRON.
        </p>
      </motion.div>
    </motion.div>
  )
}

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <div
        className="uppercase"
        style={{ fontSize: 9, letterSpacing: '0.22em', color: 'var(--jade-300)', opacity: 0.85, marginBottom: 2 }}
      >
        {label}
      </div>
      <div style={{ color: 'var(--ivory)' }}>{children}</div>
    </div>
  )
}
