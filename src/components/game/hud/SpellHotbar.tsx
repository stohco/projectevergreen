'use client'

import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import { Sword, Feather, Eye, Flower2 } from 'lucide-react'
import {
  useWorldStateQuery,
  SPELL_SLOTS,
  type SpellSlot,
} from './WorldStateQuery'
import { activateSpell } from './useEngine'

const ICONS: Record<string, React.ComponentType<{ size?: number }>> = {
  sword: Sword,
  wing: Feather,
  eye: Eye,
  lotus: Flower2,
}

/**
 * SpellHotbar — bottom-center spell hotbar (above the qi bar).
 *
 * 8 slots, keys 1-8 to activate. Selected slot has a gold border; active
 * toggles have a jade border. Each slot has an icon + cooldown overlay +
 * keybind label + qi cost.
 */
export default function SpellHotbar() {
  const selected = useWorldStateQuery((s) => s.selectedSpellSlot)
  const select = useWorldStateQuery((s) => s.selectSpellSlot)
  const cooldowns = useWorldStateQuery((s) => s.spellCooldowns)
  const active = useWorldStateQuery((s) => s.spellActive)
  const player = useWorldStateQuery((s) => s.player)

  // Re-render every 100ms so cooldown veils animate smoothly.
  const [, force] = useState(0)
  useEffect(() => {
    const id = setInterval(() => force((n) => n + 1), 100)
    return () => clearInterval(id)
  }, [])

  return (
    <div
      className="flex items-end gap-2"
      style={{ pointerEvents: 'auto' }}
    >
      {SPELL_SLOTS.map((slot) => (
        <SpellSlotView
          key={slot.id}
          slot={slot}
          selected={selected === slot.id}
          active={!!active[slot.id]}
          readyAt={cooldowns[slot.id] ?? 0}
          canAfford={slot.cost <= 0 || player.qi >= slot.cost}
          onSelect={() => {
            select(slot.id)
            activateSpell(slot.id)
          }}
        />
      ))}
    </div>
  )
}

function SpellSlotView({
  slot,
  selected,
  active,
  readyAt,
  canAfford,
  onSelect,
}: {
  slot: SpellSlot
  selected: boolean
  active: boolean
  readyAt: number
  canAfford: boolean
  onSelect: () => void
}) {
  const now = Date.now()
  const onCd = readyAt > now
  const cdRemaining = onCd ? readyAt - now : 0
  const cdTotal = slot.cooldown || 1
  const cdPct = onCd ? Math.max(0, Math.min(100, (cdRemaining / cdTotal) * 100)) : 0

  const Icon = ICONS[slot.icon]

  const classes = [
    'spell-slot',
    selected ? 'selected' : '',
    active ? 'active-toggle' : '',
    slot.empty ? 'empty' : '',
    !canAfford && !slot.empty ? 'low-qi' : '',
  ]
    .filter(Boolean)
    .join(' ')

  return (
    <motion.button
      type="button"
      className={classes}
      onClick={slot.empty ? undefined : onSelect}
      disabled={slot.empty}
      whileHover={!slot.empty ? { y: -2 } : undefined}
      whileTap={!slot.empty ? { scale: 0.96 } : undefined}
      style={{
        borderRadius: 2,
        cursor: slot.empty ? 'default' : 'pointer',
        padding: 0,
      }}
      aria-label={slot.empty ? `Empty slot ${slot.id}` : `${slot.name} (${slot.nameCn}) — key ${slot.id}`}
      title={slot.empty ? undefined : `${slot.name} · ${slot.nameCn}\n${slot.description}`}
    >
      {/* Keybind */}
      <span className="spell-keybind">{slot.id}</span>

      {/* Icon */}
      {!slot.empty && (
        <div
          className="absolute inset-0 flex items-center justify-center"
          style={{
            opacity: canAfford ? 0.92 : 0.4,
            color: active ? 'var(--jade-300)' : 'var(--gold-200)',
          }}
        >
          {Icon ? <Icon size={24} /> : null}
        </div>
      )}

      {/* Toggle / cost indicator */}
      {!slot.empty && slot.cost > 0 && (
        <span className="spell-cost">{slot.cost}qi</span>
      )}
      {!slot.empty && slot.toggle && slot.costPerSec && slot.costPerSec > 0 && (
        <span className="spell-cost">-{slot.costPerSec}/s</span>
      )}
      {!slot.empty && slot.toggle && !slot.costPerSec && (
        <span className="spell-cost" style={{ color: active ? 'var(--jade-300)' : 'var(--ivory-dim)' }}>
          {active ? 'ON' : 'OFF'}
        </span>
      )}

      {/* Name strip — only on hover */}
      {!slot.empty && (
        <div
          className="absolute left-0 right-0 text-center"
          style={{
            bottom: -16,
            fontSize: 9,
            fontFamily: 'var(--font-serif-cn)',
            color: 'var(--ivory-dim)',
            letterSpacing: '0.06em',
            opacity: 0,
            transition: 'opacity 200ms',
            pointerEvents: 'none',
            whiteSpace: 'nowrap',
          }}
        >
          <span className="font-cn">{slot.nameCn}</span>
        </div>
      )}

      {/* Cooldown veil */}
      {onCd && (
        <div className="spell-cooldown-veil" style={{ borderRadius: 2 }}>
          <div
            style={{
              position: 'absolute',
              left: 0,
              right: 0,
              bottom: 0,
              height: `${cdPct}%`,
              background: 'linear-gradient(180deg, rgba(212,175,55,0.18), rgba(212,175,55,0.06))',
            }}
          />
          <div
            className="absolute inset-0 flex items-center justify-center"
            style={{
              fontFamily: 'var(--font-mono)',
              fontSize: 14,
              color: 'var(--gold-300)',
              textShadow: '0 1px 3px rgba(0,0,0,0.9)',
            }}
          >
            {(cdRemaining / 1000).toFixed(1)}
          </div>
        </div>
      )}

      {/* Low-qi red veil */}
      {!slot.empty && !canAfford && !onCd && (
        <div
          className="absolute inset-0"
          style={{
            background: 'rgba(231, 76, 60, 0.18)',
            borderRadius: 2,
            pointerEvents: 'none',
          }}
        />
      )}
    </motion.button>
  )
}
