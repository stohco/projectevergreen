'use client'

import { useState, useEffect } from 'react'
import dynamic from 'next/dynamic'

/**
 * Er Gen Verse — Three.js entry.
 *
 * Flow:
 *   1. Character Creation (name + spirit root selection)
 *   2. World Canvas (Three.js scene mounts)
 *   3. HUD overlay
 *
 * The player is NOT Wang Lin. The player is a mortal who types their own
 * name at the start. Wang Lin exists as a manifestation NPC in the world.
 */
const WorldCanvas = dynamic(() => import('@/components/game/WorldCanvas'), {
  ssr: false,
  loading: () => <LoadingShell />,
})

const HUD = dynamic(() => import('@/components/game/hud/HUD'), {
  ssr: false,
})

const CharacterCreation = dynamic(() => import('@/components/game/CharacterCreation'), {
  ssr: false,
})

export default function Page() {
  const [characterCreated, setCharacterCreated] = useState(false)

  // Check if a name was already saved (skip character creation on revisit).
  useEffect(() => {
    if (typeof localStorage !== 'undefined') {
      const saved = localStorage.getItem('ergenverse.playerName')
      if (saved) setCharacterCreated(true)
    }
  }, [])

  return (
    <main className="relative h-screen w-screen overflow-hidden bg-black">
      {characterCreated ? (
        <>
          <WorldCanvas />
          <HUD />
        </>
      ) : (
        <CharacterCreation onComplete={() => setCharacterCreated(true)} />
      )}
    </main>
  )
}

function LoadingShell() {
  return (
    <div className="flex h-full w-full items-center justify-center bg-gradient-to-b from-stone-950 to-black text-white">
      <div className="text-center">
        <div className="mx-auto mb-4 h-12 w-12 animate-spin rounded-full border-2 border-amber-400/40 border-t-amber-400" />
        <p className="font-serif text-2xl tracking-widest text-amber-200">
          Loading the World
        </p>
        <p className="mt-2 text-xs uppercase tracking-[0.4em] text-amber-100/50">
          Er Gen Verse · Renegade Immortal
        </p>
      </div>
    </div>
  )
}
