'use client'

import { useEffect } from 'react'
import './hud-theme.css'

import { useWorldStateQuery } from './WorldStateQuery'
import { useEngine, activateSpell } from './useEngine'

import QiBar from './QiBar'
import CultivationPanel from './CultivationPanel'
import WorldStateEngine from './WorldStateEngine'
import Minimap from './Minimap'
import Codex from './Codex'
import DebugOverlay from './DebugOverlay'
import RealmBreakthroughToast from './RealmBreakthroughToast'
import SpellHotbar from './SpellHotbar'
import DialogBox from './DialogBox'
import LoadingScreen from './LoadingScreen'
import MainMenu from './MainMenu'

/**
 * HUD — the Er Gen Verse AAA HUD root.
 *
 * Composes every HUD element absolutely-positioned over the Three.js canvas.
 * Connects to the engine via the useEngine() hook (which subscribes to engine
 * state and updates the Zustand store). Handles keyboard shortcuts:
 *
 *   1-8        — activate spell slot
 *   J          — toggle Codex
 *   F3         — toggle debug overlay
 *   ESC        — toggle main menu
 *   M          — toggle minimap (future — currently always-on)
 *
 * Canon fidelity:
 *   - Default player name "王林 Wang Lin" (protagonist, canon N01, conf 5).
 *   - Default realm: Foundation Establishment (筑基期) — mod-original starting
 *     point for playability.
 *   - NO invented chapter citations anywhere in the HUD.
 *
 * Integration: WorldCanvas (owned by another sub-agent) mounts this HUD as
 * an overlay. The HUD does NOT touch the Three.js renderer directly — all
 * engine interaction goes through the useEngine() hook and the
 * `globalThis.__ergenBridge` interface.
 */
export default function HUD() {
  // Subscribe to engine state (or run the simulation stub).
  useEngine()

  // Keyboard shortcuts.
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      // Don't intercept when typing in an input.
      const target = e.target as HTMLElement | null
      if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable)) {
        return
      }

      // 1-8: spell slots.
      if (e.key >= '1' && e.key <= '8') {
        const slot = parseInt(e.key, 10)
        const st = useWorldStateQuery.getState()
        st.selectSpellSlot(slot)
        activateSpell(slot)
        return
      }

      switch (e.key) {
        case 'j':
        case 'J': {
          e.preventDefault()
          useWorldStateQuery.getState().toggleCodex()
          break
        }
        case 'F3': {
          e.preventDefault()
          useWorldStateQuery.getState().toggleDebug()
          break
        }
        case 'Escape': {
          e.preventDefault()
          const s = useWorldStateQuery.getState()
          // Priority: dialog > codex > menu > menu.
          if (s.dialogOpen) {
            s.closeDialog()
          } else if (s.codexOpen) {
            s.closeCodex()
          } else {
            s.toggleMenu()
          }
          break
        }
        case 'm':
        case 'M': {
          // Future: minimap zoom cycle. For now, cycle through zoom presets.
          e.preventDefault()
          const s = useWorldStateQuery.getState()
          const presets = [64, 128, 256, 512, 1024]
          const idx = presets.indexOf(s.minimapZoom)
          const next = presets[(idx + 1) % presets.length] ?? 256
          s.setMinimapZoom(next)
          break
        }
        case 'b':
        case 'B': {
          // Demo trigger — realm breakthrough toast.
          e.preventDefault()
          useWorldStateQuery.getState().showBreakthrough('Core Formation', '结丹期')
          break
        }
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [])

  return (
    <div className="hud-root">
      {/* Boot loading screen (auto-dismisses once canon DB is loaded) */}
      <LoadingScreen />

      {/* Top-left — cultivation panel */}
      <CultivationPanel />

      {/* Top-right — minimap */}
      <Minimap />

      {/* Top-center — debug overlay (F3) */}
      <DebugOverlay />

      {/* Bottom-right — world state engine */}
      <WorldStateEngine />

      {/* Bottom-center — spell hotbar + qi bar */}
      <div
        style={{
          position: 'absolute',
          bottom: 24,
          left: '50%',
          transform: 'translateX(-50%)',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 12,
          pointerEvents: 'none',
        }}
      >
        <div style={{ pointerEvents: 'auto' }}>
          <SpellHotbar />
        </div>
        <QiBar />
      </div>

      {/* Bottom-center — dialog box (above hotbar) */}
      <DialogBox />

      {/* Full-screen modals / overlays */}
      <Codex />
      <MainMenu />
      <RealmBreakthroughToast />

      {/* Bottom-left — controls hint */}
      <ControlsHint />
    </div>
  )
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Controls hint (bottom-left, subtle)                                    */
/* ─────────────────────────────────────────────────────────────────────── */

function ControlsHint() {
  const keys: Array<[string, string]> = [
    ['1-8', 'Spells'],
    ['J', 'Codex'],
    ['F3', 'Debug'],
    ['M', 'Map zoom'],
    ['B', 'Breakthrough demo'],
    ['ESC', 'Menu'],
  ]
  return (
    <div
      style={{
        position: 'absolute',
        bottom: 16,
        left: 16,
        fontFamily: 'var(--font-mono)',
        fontSize: 9,
        color: 'rgba(244, 236, 216, 0.4)',
        letterSpacing: '0.08em',
        lineHeight: 1.6,
        pointerEvents: 'none',
        userSelect: 'none',
      }}
    >
      {keys.map(([k, v]) => (
        <div key={k}>
          <span style={{ color: 'var(--gold-300)', opacity: 0.7 }}>{k}</span>
          {' '}
          <span style={{ opacity: 0.5 }}>·</span>
          {' '}
          <span>{v}</span>
        </div>
      ))}
    </div>
  )
}
