'use client'

import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { useWorldStateQuery } from './WorldStateQuery'
import { loadCanonDatabase } from '@/engine/canon/RICanonicalDatabase'

/**
 * LoadingScreen — boot loading screen.
 *
 * Full-screen black with 凝聚元神 (CONJURING THE WORLD) text + progress bar
 * + spinning taiji. Calls loadCanonDatabase() to drive the progress bar.
 */
export default function LoadingScreen() {
  const progress = useWorldStateQuery((s) => s.loadingProgress)
  const phase = useWorldStateQuery((s) => s.loadingPhase)
  const setProgress = useWorldStateQuery((s) => s.setLoading)
  const setBoot = useWorldStateQuery((s) => s.setBootComplete)
  const boot = useWorldStateQuery((s) => s.bootComplete)
  const [failed, setFailed] = useState<string | null>(null)

  // Drive the loading sequence.
  useEffect(() => {
    if (boot) return
    let cancelled = false
    const phases: Array<[string, number, number]> = [
      ['Gathering qi 凝聚元神', 0.05, 250],
      ['Loading canon database 加载典籍', 0.25, 300],
      ['Building world graph 构筑境界图', 0.55, 350],
      ['Materializing Planet Suzaku 物化朱雀星', 0.85, 300],
      ['Conjuring spawn point 召唤生地', 1.0, 200],
    ]
    let i = 0
    const run = async () => {
      // Kick off canon DB load.
      const dbPromise = loadCanonDatabase().catch((e) => {
        if (!cancelled) setFailed(String(e))
        return null
      })
      for (const [label, target, dur] of phases) {
        if (cancelled) return
        setProgress(target, label)
        // Smoothly ramp from current to target.
        const start = performance.now()
        const from = useWorldStateQuery.getState().loadingProgress
        await new Promise<void>((resolve) => {
          const step = (now: number) => {
            if (cancelled) return resolve()
            const t = Math.min(1, (now - start) / dur)
            setProgress(from + (target - from) * t, label)
            if (t < 1) requestAnimationFrame(step)
            else resolve()
          }
          requestAnimationFrame(step)
        })
        i++
      }
      await dbPromise
      if (cancelled) return
      setProgress(1, 'Ready')
      setTimeout(() => {
        if (!cancelled) setBoot(true)
      }, 400)
    }
    run()
    return () => {
      cancelled = true
    }
  }, [boot, setBoot, setProgress])

  return (
    <AnimatePresence>
      {!boot && (
        <motion.div
          className="fixed inset-0 z-50 flex flex-col items-center justify-center"
          style={{ background: '#000', pointerEvents: 'auto' }}
          initial={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.6 }}
        >
          {/* Faint starfield backdrop */}
          <div
            aria-hidden
            style={{
              position: 'absolute',
              inset: 0,
              backgroundImage:
                'radial-gradient(1px 1px at 20% 30%, rgba(212,175,55,0.5), transparent), radial-gradient(1px 1px at 70% 60%, rgba(110,231,183,0.4), transparent), radial-gradient(1px 1px at 40% 80%, rgba(244,236,216,0.4), transparent), radial-gradient(2px 2px at 85% 20%, rgba(212,175,55,0.3), transparent), radial-gradient(1px 1px at 10% 70%, rgba(244,236,216,0.3), transparent)',
              backgroundSize: '600px 600px, 800px 800px, 700px 700px, 900px 900px, 500px 500px',
              opacity: 0.7,
            }}
          />

          {/* Taiji spinner */}
          <div style={{ position: 'relative', marginBottom: 36 }}>
            <div className="loading-taiji">
              <div className="loading-taiji-eye-top" />
              <div className="loading-taiji-eye-bot" />
            </div>
            {/* Outer ring with gold tick marks */}
            <svg
              width="120"
              height="120"
              style={{ position: 'absolute', top: -16, left: -16, pointerEvents: 'none' }}
              viewBox="0 0 120 120"
            >
              <circle
                cx="60" cy="60" r="58"
                fill="none"
                stroke="rgba(212, 175, 55, 0.25)"
                strokeWidth="0.5"
              />
              {Array.from({ length: 24 }).map((_, i) => {
                const a = (i * Math.PI) / 12
                const x1 = 60 + Math.cos(a) * 56
                const y1 = 60 + Math.sin(a) * 56
                const x2 = 60 + Math.cos(a) * (i % 6 === 0 ? 50 : 53)
                const y2 = 60 + Math.sin(a) * (i % 6 === 0 ? 50 : 53)
                return (
                  <line
                    key={i}
                    x1={x1} y1={y1} x2={x2} y2={y2}
                    stroke={i % 6 === 0 ? 'rgba(212,175,55,0.7)' : 'rgba(212,175,55,0.3)'}
                    strokeWidth="0.6"
                  />
                )
              })}
            </svg>
          </div>

          {/* Title */}
          <motion.div
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.2 }}
            style={{ textAlign: 'center', marginBottom: 32 }}
          >
            <div
              className="font-cal"
              style={{
                fontSize: 48,
                color: 'var(--ivory)',
                letterSpacing: '0.25em',
                textShadow: '0 0 24px rgba(212, 175, 55, 0.4), 0 0 48px rgba(47, 143, 95, 0.2)',
                lineHeight: 1,
              }}
            >
              凝聚元神
            </div>
            <div
              className="font-en italic"
              style={{
                fontSize: 14,
                color: 'var(--gold-300)',
                letterSpacing: '0.4em',
                marginTop: 12,
                textTransform: 'uppercase',
              }}
            >
              Conjuring the World
            </div>
            <div
              className="font-en"
              style={{
                fontSize: 10,
                color: 'var(--ivory-dim)',
                letterSpacing: '0.5em',
                marginTop: 6,
                textTransform: 'uppercase',
                opacity: 0.6,
              }}
            >
              Er Gen Verse · Renegade Immortal
            </div>
          </motion.div>

          {/* Progress bar */}
          <div className="loading-progress-track">
            <div
              className="loading-progress-fill"
              style={{ width: `${Math.round(progress * 100)}%` }}
            />
          </div>
          <div
            className="mt-3 flex w-full items-center justify-between"
            style={{ width: 360, fontFamily: 'var(--font-mono)', fontSize: 10, color: 'var(--ivory-dim)', letterSpacing: '0.08em' }}
          >
            <span className="font-cn" style={{ color: 'var(--jade-300)' }}>{phase}</span>
            <span style={{ color: 'var(--gold-300)' }}>{Math.round(progress * 100)}%</span>
          </div>

          {failed && (
            <div
              className="mt-4"
              style={{ fontSize: 11, color: 'var(--vermillion-bright)', maxWidth: 360, textAlign: 'center' }}
            >
              Canon database load failed — running in offline mode. ({failed})
            </div>
          )}

          <div
            className="absolute bottom-6 font-cn"
            style={{ fontSize: 11, color: 'var(--ivory-dim)', opacity: 0.5, letterSpacing: '0.3em' }}
          >
            天地不仁 · 以万物为刍狗
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
