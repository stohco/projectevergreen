'use client'

import { useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { useWorldStateQuery } from './WorldStateQuery'

/**
 * RealmBreakthroughToast — cinematic realm-breakthrough notification.
 *
 * When the player breaks through a realm (e.g. Foundation → Core Formation),
 * show a full-screen golden flash + dragon motif + calligraphy title.
 * Auto-dismisses after 4 seconds.
 */
export default function RealmBreakthroughToast() {
  const bt = useWorldStateQuery((s) => s.breakthrough)
  const clear = useWorldStateQuery((s) => s.clearBreakthrough)

  useEffect(() => {
    if (!bt) return
    const id = setTimeout(clear, 4000)
    return () => clearTimeout(id)
  }, [bt, clear])

  return (
    <AnimatePresence>
      {bt && (
        <motion.div
          className="breakthrough-overlay"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.4 }}
        >
          {/* Rays */}
          <div className="bt-rays" aria-hidden />

          {/* Dragon motif — 龍 in calligraphy, faint gold */}
          <div className="bt-dragon" style={{ top: '15%', left: '8%' }} aria-hidden>龍</div>
          <div className="bt-dragon" style={{ bottom: '15%', right: '8%', transform: 'scaleX(-1)' }} aria-hidden>龍</div>

          {/* Title */}
          <motion.div
            key={bt.timestamp}
            className="bt-title"
            initial={{ scale: 0.85, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.6, ease: [0.22, 1, 0.36, 1] }}
            style={{ position: 'relative', zIndex: 2 }}
          >
            <div
              className="uppercase"
              style={{
                fontFamily: 'var(--font-serif-en)',
                fontSize: 14,
                color: 'var(--gold-200)',
                letterSpacing: '0.5em',
                marginBottom: 8,
                opacity: 0.85,
              }}
            >
              ✦ Breakthrough ✦
            </div>
            <div
              className="font-cal"
              style={{
                fontSize: 84,
                color: 'var(--gold-300)',
                textShadow:
                  '0 0 24px rgba(212, 175, 55, 0.9), 0 0 48px rgba(212, 175, 55, 0.6), 0 4px 8px rgba(0,0,0,0.5)',
                lineHeight: 1,
                letterSpacing: '0.12em',
              }}
            >
              {bt.realmCn}
            </div>
            <div
              className="font-en italic"
              style={{
                fontSize: 22,
                color: 'var(--ivory)',
                marginTop: 12,
                letterSpacing: '0.16em',
                textShadow: '0 0 12px rgba(0,0,0,0.8)',
              }}
            >
              {bt.realmEn}
            </div>
            <motion.div
              initial={{ width: 0 }}
              animate={{ width: 320 }}
              transition={{ duration: 1.4, ease: 'easeOut', delay: 0.3 }}
              style={{
                height: 1,
                margin: '20px auto 0',
                background:
                  'linear-gradient(90deg, transparent, var(--gold-400), transparent)',
              }}
            />
            <div
              className="font-cn"
              style={{
                fontSize: 13,
                color: 'var(--jade-200)',
                marginTop: 14,
                letterSpacing: '0.3em',
                opacity: 0.7,
              }}
            >
              天道無親 · 常與善人
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
