'use client'

import dynamic from 'next/dynamic'

/**
 * Er Gen Verse — Three.js entry.
 *
 * The world mounts client-side (Three.js touches `window`).
 * A loading shell is rendered server-side so first paint is instant.
 */
const WorldCanvas = dynamic(() => import('@/components/game/WorldCanvas'), {
  ssr: false,
  loading: () => <LoadingShell />,
})

const HUD = dynamic(() => import('@/components/game/hud/HUD'), {
  ssr: false,
})

export default function Page() {
  return (
    <main className="relative h-screen w-screen overflow-hidden bg-black">
      <WorldCanvas />
      <HUD />
    </main>
  )
}

function LoadingShell() {
  return (
    <div className="flex h-full w-full items-center justify-center bg-gradient-to-b from-slate-950 via-emerald-950 to-black text-white">
      <div className="text-center">
        <div className="mx-auto mb-4 h-12 w-12 animate-spin rounded-full border-2 border-amber-400/40 border-t-amber-400" />
        <p className="font-serif text-2xl tracking-widest text-amber-200">
          凝聚元神 · CONJURING THE WORLD
        </p>
        <p className="mt-2 text-xs uppercase tracking-[0.4em] text-amber-100/50">
          Er Gen Verse · Renegade Immortal
        </p>
      </div>
    </div>
  )
}
