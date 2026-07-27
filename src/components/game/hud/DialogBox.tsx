'use client'

import { useEffect, useRef, useState, useCallback } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { useWorldStateQuery } from './WorldStateQuery'

/**
 * DialogBox — bottom-center NPC dialog box.
 *
 * 600px wide × 120px tall. Speaker name + 80×80 portrait. Typewriter
 * animation. Click to advance; ESC to skip.
 *
 * Jade-green silk border + gold corners (xianxia-styled frame).
 *
 * The typewriter is extracted into its own `<Typewriter>` component keyed by
 * the line index — this way React remounts it on each new line and the
 * useState initializer handles the reset (no setState-in-effect needed).
 */
export default function DialogBox() {
  const dialog = useWorldStateQuery((s) => s.dialog)
  const open = useWorldStateQuery((s) => s.dialogOpen)
  const advance = useWorldStateQuery((s) => s.advanceDialog)
  const close = useWorldStateQuery((s) => s.closeDialog)
  const [done, setDone] = useState(false)

  const currentLine = dialog ? dialog.lines[dialog.lineIndex] ?? '' : ''

  // Reset `done` when the line index changes.
  const lineIndex = dialog?.lineIndex ?? 0
  const lineRef = useRef(lineIndex)
  if (lineRef.current !== lineIndex) {
    lineRef.current = lineIndex
    if (done) setDone(false)
  }

  // Stable callback for the typewriter to signal completion.
  const handleTypedDone = useCallback(() => setDone(true), [])

  // ESC to skip / close.
  useEffect(() => {
    if (!open) return
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        if (!done) {
          setDone(true)
        } else {
          close()
        }
      } else if (e.key === 'Enter' || e.key === ' ' || e.key === 'e' || e.key === 'E') {
        e.preventDefault()
        if (!done) {
          setDone(true)
        } else {
          advance()
        }
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [open, done, advance, close])

  return (
    <AnimatePresence>
      {open && dialog && (
        <motion.div
          className="absolute left-1/2 z-40"
          style={{ bottom: 180, transform: 'translateX(-50%)', pointerEvents: 'auto' }}
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: 24 }}
          transition={{ duration: 0.28, ease: [0.22, 1, 0.36, 1] }}
        >
          <div
            className="dialog-frame"
            style={{
              width: 600,
              minHeight: 120,
              padding: 14,
              display: 'flex',
              gap: 14,
            }}
            onClick={() => {
              if (!done) {
                setDone(true)
              } else {
                advance()
              }
            }}
            role="button"
            tabIndex={0}
            aria-label={`Dialog from ${dialog.speaker}. ${currentLine}`}
          >
            <span className="dialog-corner tl" />
            <span className="dialog-corner tr" />
            <span className="dialog-corner bl" />
            <span className="dialog-corner br" />
            <div className="silk-shimmer" aria-hidden />

            {/* Portrait */}
            <div
              style={{
                width: 80,
                height: 80,
                flexShrink: 0,
                borderRadius: 2,
                border: '1px solid rgba(212,175,55,0.5)',
                background: 'linear-gradient(135deg, rgba(47,143,95,0.18), rgba(0,0,0,0.6))',
                overflow: 'hidden',
                position: 'relative',
                boxShadow: 'inset 0 0 12px rgba(0,0,0,0.7), 0 0 16px rgba(47,143,95,0.2)',
              }}
            >
              {dialog.portrait ? (
                <img
                  src={dialog.portrait}
                  alt=""
                  style={{ width: '100%', height: '100%', objectFit: 'cover', opacity: 0.95 }}
                />
              ) : (
                <div
                  className="flex h-full w-full items-center justify-center font-cal"
                  style={{ fontSize: 36, color: 'var(--gold-300)', textShadow: '0 0 12px rgba(212,175,55,0.5)' }}
                  aria-hidden
                >
                  {(dialog.speakerCn ?? dialog.speaker).slice(0, 1)}
                </div>
              )}
            </div>

            {/* Text */}
            <div className="min-w-0 flex-1" style={{ display: 'flex', flexDirection: 'column' }}>
              <div className="flex items-baseline gap-2">
                <span
                  className="font-cn"
                  style={{ fontSize: 16, color: 'var(--gold-300)', letterSpacing: '0.06em', fontWeight: 600 }}
                >
                  {dialog.speakerCn ?? dialog.speaker}
                </span>
                {dialog.speakerCn && (
                  <span
                    className="font-en italic"
                    style={{ fontSize: 11, color: 'var(--ivory-dim)' }}
                  >
                    {dialog.speaker}
                  </span>
                )}
                <span
                  className="ml-auto rounded px-1.5"
                  style={{
                    fontFamily: 'var(--font-mono)',
                    fontSize: 9,
                    color: 'var(--ivory-dim)',
                    background: 'rgba(0,0,0,0.4)',
                    border: '1px solid rgba(212,175,55,0.25)',
                  }}
                >
                  {dialog.lineIndex + 1} / {dialog.lines.length}
                </span>
              </div>
              <div
                className="hud-divider"
                style={{ margin: '4px 0 6px' }}
              />
              <div
                className="font-cn"
                style={{
                  fontSize: 14,
                  color: 'var(--ivory)',
                  lineHeight: 1.7,
                  letterSpacing: '0.03em',
                  minHeight: 48,
                  fontFamily: 'var(--font-serif-mix)',
                }}
              >
                <Typewriter key={lineIndex} text={currentLine} onDone={handleTypedDone} done={done} />
              </div>
              <div
                className="mt-auto flex items-center justify-end gap-3"
                style={{ fontSize: 9, color: 'var(--ivory-dim)', letterSpacing: '0.16em', textTransform: 'uppercase' }}
              >
                <span>
                  {done
                    ? (dialog.lineIndex + 1 >= dialog.lines.length ? 'Click / Space → close' : 'Click / Space → next')
                    : 'Click / Space → skip'}
                </span>
                <span style={{ opacity: 0.5 }}>ESC → close</span>
              </div>
            </div>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}

/**
 * Typewriter — animates text char-by-char. Remounted via `key` per line so
 * useState initializes fresh (no setState-in-effect needed).
 */
function Typewriter({ text, onDone, done }: { text: string; onDone: () => void; done: boolean }) {
  const [count, setCount] = useState(0)
  const rafRef = useRef<number>(0)
  const startRef = useRef<number>(0)

  useEffect(() => {
    if (done) return
    const speed = 22
    const tick = (ts: number) => {
      if (!startRef.current) startRef.current = ts
      const elapsed = ts - startRef.current
      const n = Math.floor(elapsed / speed)
      if (n >= text.length) {
        onDone()
        return
      }
      setCount(n)
      rafRef.current = requestAnimationFrame(tick)
    }
    rafRef.current = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(rafRef.current)
  }, [text, done, onDone])

  const shown = done ? text : text.slice(0, count)
  return (
    <>
      {shown}
      {!done && <span className="typewriter-caret">&nbsp;</span>}
    </>
  )
}
