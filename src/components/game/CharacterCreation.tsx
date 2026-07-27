'use client'

import { useState, useEffect } from 'react'

/**
 * CharacterCreation — name entry screen shown before the game starts.
 *
 * The player types their name (English, per user directive). The name is
 * stored in globalThis.__ergenPlayerName and read by WorldCanvas when
 * creating the player avatar. The player starts as a MORTAL — no cultivation,
 * no qi, just a peasant in Zhao Country.
 *
 * This is NOT Wang Lin. The player is a separate first-class actor who will
 * encounter Wang Lin's manifestation in the world.
 */

const STORAGE_KEY = 'ergenverse.playerName'

export function getPlayerName(): string {
  if (typeof localStorage === 'undefined') return 'Mortal'
  return localStorage.getItem(STORAGE_KEY) ?? ''
}

export default function CharacterCreation({ onComplete }: { onComplete: (name: string) => void }) {
  const [name, setName] = useState('')
  const [spiritRoot, setSpiritRoot] = useState<'metal' | 'wood' | 'water' | 'fire' | 'earth' | 'void'>('wood')

  useEffect(() => {
    // If a name was already entered, skip straight to game.
    const saved = getPlayerName()
    if (saved) {
      onComplete(saved)
    }
  }, [onComplete])

  const handleSubmit = () => {
    const finalName = name.trim() || 'Mortal'
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(STORAGE_KEY, finalName)
    }
    ;(globalThis as { __ergenPlayerName?: string }).__ergenPlayerName = finalName
    ;(globalThis as { __ergenPlayerSpiritRoot?: string }).__ergenPlayerSpiritRoot = spiritRoot
    onComplete(finalName)
  }

  const spiritRoots: Array<{ key: typeof spiritRoot; label: string; element: string; desc: string }> = [
    { key: 'metal', label: 'Metal', element: '金', desc: 'Sharp, unyielding. Bonus to sword qi.' },
    { key: 'wood', label: 'Wood', element: '木', desc: 'Vital, growing. Bonus to healing arts.' },
    { key: 'water', label: 'Water', element: '水', desc: 'Flowing, adaptable. Bonus to defense.' },
    { key: 'fire', label: 'Fire', element: '火', desc: 'Fierce, explosive. Bonus to attack.' },
    { key: 'earth', label: 'Earth', element: '土', desc: 'Steady, enduring. Bonus to body refinement.' },
    { key: 'void', label: 'Void', element: '空', desc: 'Rare. Mod-original. Unknown potential.' },
  ]

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-gradient-to-b from-stone-950 via-amber-950/30 to-black">
      <div className="w-full max-w-md rounded-lg border border-amber-700/40 bg-stone-950/90 p-8 backdrop-blur-sm">
        {/* Title */}
        <div className="mb-6 text-center">
          <h1 className="font-serif text-3xl tracking-wider text-amber-200">Er Gen Verse</h1>
          <p className="mt-1 text-xs uppercase tracking-[0.4em] text-amber-100/40">Renegade Immortal</p>
          <div className="mx-auto mt-4 h-px w-32 bg-gradient-to-r from-transparent via-amber-600/50 to-transparent" />
        </div>

        {/* Name input */}
        <label className="mb-2 block text-xs uppercase tracking-widest text-amber-100/60">
          Your Name
        </label>
        <input
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          onKeyDown={(e) => { if (e.key === 'Enter') handleSubmit() }}
          placeholder="Enter your name..."
          maxLength={20}
          className="mb-6 w-full rounded-md border border-amber-700/40 bg-stone-900/80 px-4 py-3 text-amber-100 placeholder-amber-100/30 outline-none focus:border-amber-500/60 focus:ring-1 focus:ring-amber-500/30"
          autoFocus
        />

        {/* Spirit root selection */}
        <label className="mb-2 block text-xs uppercase tracking-widest text-amber-100/60">
          Spirit Root
        </label>
        <div className="mb-6 grid grid-cols-3 gap-2">
          {spiritRoots.map((sr) => (
            <button
              key={sr.key}
              onClick={() => setSpiritRoot(sr.key)}
              className={`rounded-md border px-3 py-2 text-left transition-colors ${
                spiritRoot === sr.key
                  ? 'border-amber-500/60 bg-amber-900/30 text-amber-200'
                  : 'border-stone-700/50 bg-stone-900/50 text-stone-300 hover:border-amber-700/40'
              }`}
            >
              <div className="text-sm font-medium">{sr.label}</div>
              <div className="text-xs text-stone-400">{sr.element}</div>
            </button>
          ))}
        </div>
        <p className="mb-6 text-xs italic text-stone-400">
          {spiritRoots.find((sr) => sr.key === spiritRoot)?.desc}
        </p>

        {/* Begin button */}
        <button
          onClick={handleSubmit}
          className="w-full rounded-md border border-amber-600/50 bg-gradient-to-b from-amber-800/40 to-amber-900/40 px-4 py-3 font-serif text-lg tracking-wider text-amber-100 transition-all hover:from-amber-700/50 hover:to-amber-800/50 hover:shadow-lg hover:shadow-amber-900/30"
        >
          Begin Your Journey
        </button>

        {/* Lore footer */}
        <p className="mt-6 text-center text-[10px] text-stone-500">
          You are a mortal in Zhao Country. The cultivation world awaits.
        </p>
      </div>
    </div>
  )
}
