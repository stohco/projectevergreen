'use client'

import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import {
  Play, Save, FolderOpen, Settings as SettingsIcon, BookOpen, LogOut, X,
} from 'lucide-react'
import { useWorldStateQuery } from './WorldStateQuery'

/**
 * MainMenu — ESC menu.
 *
 * Full-screen dim overlay. Options: Resume / Save / Load / Settings / Codex
 * / Exit. Save = serialize WorldDeltaStore to localStorage (via the engine
 * bridge if registered, else player+settings only). Load = deserialize.
 * Settings: render distance, post-FX toggles, FOV slider.
 */
export default function MainMenu() {
  const open = useWorldStateQuery((s) => s.menuOpen)
  const close = useWorldStateQuery((s) => s.closeMenu)
  const save = useWorldStateQuery((s) => s.saveGame)
  const load = useWorldStateQuery((s) => s.loadGame)
  const hasSaved = useWorldStateQuery((s) => s.hasSavedGame)
  const openCodex = useWorldStateQuery((s) => s.openCodex)
  const [panel, setPanel] = useState<'root' | 'settings' | 'save'>('root')
  const [toast, setToast] = useState<string | null>(null)

  // Reset to root panel when the menu opens.
  useEffect(() => {
    if (open) setPanel('root')
  }, [open])

  // ESC closes (HUD root also handles ESC — but the menu's own ESC closes it).
  useEffect(() => {
    if (!open) return
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && panel === 'root') {
        e.preventDefault()
        close()
      } else if (e.key === 'Escape') {
        setPanel('root')
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [open, panel, close])

  const showToast = (msg: string) => {
    setToast(msg)
    setTimeout(() => setToast(null), 2200)
  }

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          className="fixed inset-0 z-50 flex items-center justify-center"
          style={{ background: 'rgba(0,0,0,0.78)', pointerEvents: 'auto', backdropFilter: 'blur(4px)' }}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
        >
          <motion.div
            className="hud-panel hud-ornate-4"
            initial={{ scale: 0.96, y: 12 }}
            animate={{ scale: 1, y: 0 }}
            exit={{ scale: 0.96, y: 12 }}
            transition={{ duration: 0.26, ease: [0.22, 1, 0.36, 1] }}
            style={{
              width: 480,
              padding: 28,
              borderRadius: 2,
            }}
          >
            <span className="hud-corner tl" />
            <span className="hud-corner tr" />
            <span className="hud-corner bl" />
            <span className="hud-corner br" />
            <div className="silk-shimmer" aria-hidden />

            {/* Header */}
            <div className="mb-5 text-center">
              <div
                className="font-cal"
                style={{
                  fontSize: 32,
                  color: 'var(--ivory)',
                  letterSpacing: '0.2em',
                  textShadow: '0 0 16px rgba(212,175,55,0.4)',
                  lineHeight: 1,
                }}
              >
                暂停
              </div>
              <div
                className="font-en italic uppercase"
                style={{
                  fontSize: 10,
                  color: 'var(--gold-300)',
                  letterSpacing: '0.4em',
                  marginTop: 6,
                  opacity: 0.85,
                }}
              >
                Paused · Er Gen Verse
              </div>
            </div>

            <AnimatePresence mode="wait">
              {panel === 'root' && (
                <motion.div
                  key="root"
                  initial={{ opacity: 0, x: -12 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -12 }}
                  transition={{ duration: 0.18 }}
                  className="space-y-2"
                >
                  <MenuBtn icon={<Play size={14} />} cn="继续" en="Resume" hint="ESC" onClick={close} />
                  <MenuBtn icon={<Save size={14} />} cn="保存" en="Save Game" hint="writes to localStorage" onClick={() => {
                    const ok = save()
                    showToast(ok ? '世界已保存 · World saved' : '保存失败 · Save failed')
                  }} />
                  <MenuBtn
                    icon={<FolderOpen size={14} />}
                    cn="读取"
                    en="Load Game"
                    hint={hasSaved() ? 'saved game found' : 'no saved game'}
                    onClick={() => {
                      const ok = load()
                      showToast(ok ? '世界已载入 · World loaded' : '读取失败 · No save found')
                    }}
                    disabled={!hasSaved()}
                  />
                  <MenuBtn icon={<SettingsIcon size={14} />} cn="设置" en="Settings" hint="render · post-FX · FOV" onClick={() => setPanel('settings')} />
                  <MenuBtn icon={<BookOpen size={14} />} cn="典籍" en="Codex" hint="J" onClick={() => { close(); openCodex() }} />
                  <MenuBtn icon={<LogOut size={14} />} cn="离开" en="Exit" hint="closes the tab" onClick={() => {
                    if (typeof window !== 'undefined') window.close()
                  }} />
                </motion.div>
              )}

              {panel === 'settings' && (
                <SettingsPanel key="settings" onBack={() => setPanel('root')} />
              )}
            </AnimatePresence>

            {/* Toast */}
            <AnimatePresence>
              {toast && (
                <motion.div
                  initial={{ opacity: 0, y: 8 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: 8 }}
                  className="mt-4 text-center font-cn"
                  style={{ fontSize: 11, color: 'var(--jade-300)', letterSpacing: '0.1em' }}
                >
                  {toast}
                </motion.div>
              )}
            </AnimatePresence>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Menu button                                                            */
/* ─────────────────────────────────────────────────────────────────────── */

function MenuBtn({
  icon,
  cn,
  en,
  hint,
  onClick,
  disabled,
}: {
  icon: React.ReactNode
  cn: string
  en: string
  hint?: string
  onClick: () => void
  disabled?: boolean
}) {
  return (
    <motion.button
      type="button"
      onClick={onClick}
      disabled={disabled}
      whileHover={!disabled ? { x: 4 } : undefined}
      className="group flex w-full items-center gap-3 rounded transition"
      style={{
        padding: '10px 14px',
        background: 'rgba(0,0,0,0.4)',
        border: '1px solid rgba(212,175,55,0.25)',
        cursor: disabled ? 'not-allowed' : 'pointer',
        opacity: disabled ? 0.45 : 1,
        color: 'var(--ivory)',
      }}
    >
      <span style={{ color: 'var(--gold-400)', display: 'inline-flex' }}>{icon}</span>
      <span
        className="font-cn"
        style={{ fontSize: 15, letterSpacing: '0.08em', color: 'var(--ivory)' }}
      >
        {cn}
      </span>
      <span
        className="font-en italic"
        style={{ fontSize: 12, color: 'var(--ivory-dim)', flex: 1, textAlign: 'left' }}
      >
        {en}
      </span>
      {hint && (
        <span
          className="font-mono"
          style={{ fontSize: 9, color: 'var(--ivory-dim)', opacity: 0.7, textTransform: 'uppercase', letterSpacing: '0.1em' }}
        >
          {hint}
        </span>
      )}
    </motion.button>
  )
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Settings panel                                                         */
/* ─────────────────────────────────────────────────────────────────────── */

function SettingsPanel({ onBack }: { onBack: () => void }) {
  const settings = useWorldStateQuery((s) => s.settings)
  const update = useWorldStateQuery((s) => s.updateSettings)

  return (
    <motion.div
      initial={{ opacity: 0, x: 12 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: 12 }}
      transition={{ duration: 0.18 }}
    >
      <div className="mb-4 flex items-center gap-2">
        <button
          type="button"
          onClick={onBack}
          className="rounded p-1 transition hover:bg-white/10"
          style={{ color: 'var(--ivory-dim)' }}
          aria-label="Back to menu"
        >
          <X size={16} />
        </button>
        <span
          className="font-cn"
          style={{ fontSize: 18, color: 'var(--gold-300)', letterSpacing: '0.08em' }}
        >
          设置 · Settings
        </span>
      </div>

      {/* Render distance */}
      <SettingRow label="Render Distance" cn="视距" value={`${settings.renderDistance} chunks`}>
        <Slider
          min={2} max={32} step={1}
          value={settings.renderDistance}
          onChange={(v) => update({ renderDistance: v })}
        />
      </SettingRow>

      {/* FOV */}
      <SettingRow label="Field of View" cn="视野" value={`${settings.fov}°`}>
        <Slider
          min={45} max={110} step={1}
          value={settings.fov}
          onChange={(v) => update({ fov: v })}
        />
      </SettingRow>

      <div className="hud-divider" style={{ margin: '14px 0' }} />

      <div
        className="mb-2 uppercase"
        style={{ fontSize: 9, letterSpacing: '0.24em', color: 'var(--jade-300)', opacity: 0.85 }}
      >
        Post-FX
      </div>

      <ToggleRow label="Bloom" cn="光晕" value={settings.bloom} onChange={(v) => update({ bloom: v })} />
      <ToggleRow label="SSAO" cn="环境光遮蔽" value={settings.ssao} onChange={(v) => update({ ssao: v })} />
      <ToggleRow label="Motion Blur" cn="动态模糊" value={settings.motionBlur} onChange={(v) => update({ motionBlur: v })} />
      <ToggleRow label="Vignette" cn="暗角" value={settings.vignette} onChange={(v) => update({ vignette: v })} />

      <p
        className="mt-4 font-en italic"
        style={{ fontSize: 10, color: 'var(--ivory-dim)', letterSpacing: '0.04em' }}
      >
        Settings persist across sessions. Engine wiring is pending — these
        toggles will take effect when the renderer subscribes to this store.
      </p>
    </motion.div>
  )
}

function SettingRow({
  label,
  cn,
  value,
  children,
}: {
  label: string
  cn: string
  value: string
  children: React.ReactNode
}) {
  return (
    <div className="mb-4">
      <div className="mb-1 flex items-baseline justify-between">
        <span style={{ fontSize: 12, color: 'var(--ivory)' }}>
          {label} <span className="font-cn" style={{ color: 'var(--ivory-dim)', marginLeft: 6 }}>{cn}</span>
        </span>
        <span style={{ fontFamily: 'var(--font-mono)', fontSize: 11, color: 'var(--gold-300)' }}>{value}</span>
      </div>
      {children}
    </div>
  )
}

function Slider({
  min, max, step, value, onChange,
}: {
  min: number
  max: number
  step: number
  value: number
  onChange: (v: number) => void
}) {
  return (
    <input
      type="range"
      min={min}
      max={max}
      step={step}
      value={value}
      onChange={(e) => onChange(Number(e.target.value))}
      style={{ width: '100%', accentColor: 'var(--gold-400)' }}
    />
  )
}

function ToggleRow({
  label, cn, value, onChange,
}: {
  label: string
  cn: string
  value: boolean
  onChange: (v: boolean) => void
}) {
  return (
    <label
      className="mb-2 flex cursor-pointer items-center justify-between rounded px-2 py-1.5 transition"
      style={{ background: 'rgba(0,0,0,0.3)', border: '1px solid rgba(212,175,55,0.12)' }}
    >
      <span style={{ fontSize: 12, color: 'var(--ivory)' }}>
        {label} <span className="font-cn" style={{ color: 'var(--ivory-dim)', marginLeft: 4 }}>{cn}</span>
      </span>
      <button
        type="button"
        role="switch"
        aria-checked={value}
        onClick={() => onChange(!value)}
        className="relative transition"
        style={{
          width: 36,
          height: 18,
          borderRadius: 9,
          background: value ? 'rgba(47, 143, 95, 0.6)' : 'rgba(0,0,0,0.5)',
          border: `1px solid ${value ? 'rgba(110, 231, 183, 0.6)' : 'rgba(212,175,55,0.3)'}`,
        }}
      >
        <motion.span
          layout
          transition={{ duration: 0.2 }}
          style={{
            position: 'absolute',
            top: 1,
            left: value ? 18 : 1,
            width: 14,
            height: 14,
            borderRadius: 7,
            background: value ? 'var(--jade-300)' : 'var(--ivory-dim)',
            boxShadow: value ? '0 0 8px rgba(110, 231, 183, 0.6)' : 'none',
          }}
        />
      </button>
    </label>
  )
}
