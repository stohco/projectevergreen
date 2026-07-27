'use client'

import { useEffect, useMemo, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Search, X, BookOpen, MapPin, Star, Scroll, Sparkles } from 'lucide-react'
import { useWorldStateQuery } from './WorldStateQuery'
import { loadCanonDatabase } from '@/engine/canon/RICanonicalDatabase'
import { PLANET_SUZAKU_PLACEMENT } from '@/engine/canon/PlanetSuzakuPlacement'
import { SEED_ACTORS } from './canonSeeds'

type Tab = 'characters' | 'locations' | 'artifacts' | 'techniques'

/* ─────────────────────────────────────────────────────────────────────── */
/*  Codex entry — flexible shape matching the actual canon JSON            */
/* ─────────────────────────────────────────────────────────────────────── */

interface CodexEntry {
  id: string
  name: string
  nameCn?: string
  type?: string
  canonConfidence?: number
  description?: string
  raw: Record<string, unknown>
}

function toEntries(arr: any[], tab: Tab): CodexEntry[] {
  return (arr ?? []).map((e) => {
    const desc = describe(e, tab)
    return {
      id: e.id,
      name: e.name,
      nameCn: e.nameCn,
      type: e.type,
      canonConfidence: e.canonConfidence,
      description: desc,
      raw: e,
    } as CodexEntry
  })
}

function describe(e: any, tab: Tab): string {
  switch (tab) {
    case 'characters':
      return [
        e.affiliation ? `Affiliation: ${e.affiliation}` : null,
        e.peakRealm ? `Peak realm: ${e.peakRealm}` : null,
        e.status ? `Status: ${e.status}` : null,
        (e.knownFacts as string[] | undefined)?.slice(0, 3).join(' / '),
      ]
        .filter(Boolean)
        .join(' · ')
    case 'locations':
      return [
        e.parentLocation ? `Parent: ${e.parentLocation}` : null,
        e.cosmologyLayer ? `Layer: ${e.cosmologyLayer}` : null,
        (e.knownFacts as string[] | undefined)?.slice(0, 3).join(' / '),
      ]
        .filter(Boolean)
        .join(' · ')
    case 'artifacts':
      return [
        e.currentOwner ? `Owner: ${e.currentOwner}` : null,
        e.category ? `Category: ${e.category}` : null,
        (e.abilities as string[] | undefined)?.slice(0, 3).join(' / '),
      ]
        .filter(Boolean)
        .join(' · ')
    case 'techniques':
      return [
        e.origin ? `Origin: ${e.origin}` : null,
        (e.effects as string[] | undefined)?.slice(0, 3).join(' / '),
        (e.knownUsers as string[] | undefined)?.length
          ? `Users: ${(e.knownUsers as string[]).slice(0, 3).join(', ')}`
          : null,
      ]
        .filter(Boolean)
        .join(' · ')
  }
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Confidence badge                                                       */
/* ─────────────────────────────────────────────────────────────────────── */

const CONF_LABELS: Record<number, { label: string; cls: string }> = {
  5: { label: 'CANON', cls: 'conf-5' },
  4: { label: 'IMPLIED', cls: 'conf-4' },
  3: { label: 'RECONSTRUCTION', cls: 'conf-3' },
  2: { label: 'SPECULATION', cls: 'conf-2' },
  1: { label: 'SPECULATION', cls: 'conf-1' },
}

function ConfBadge({ conf }: { conf?: number }) {
  const c = conf ?? 3
  const info = CONF_LABELS[c] ?? CONF_LABELS[3]
  return (
    <span className={`conf-badge ${info.cls}`} title={`Canon confidence ${c}/5 — ${info.label}`}>
      <Sparkles size={9} />
      {c}/5 · {info.label}
    </span>
  )
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Fuzzy match (subsequence + scored)                                     */
/* ─────────────────────────────────────────────────────────────────────── */

function fuzzyMatch(query: string, text: string): number {
  if (!query) return 1
  const q = query.toLowerCase()
  const t = text.toLowerCase()
  if (t.includes(q)) return 100 - (t.indexOf(q) === 0 ? 0 : 5)
  // Subsequence match.
  let qi = 0
  let score = 0
  let lastIdx = -1
  for (let i = 0; i < t.length && qi < q.length; i++) {
    if (t[i] === q[qi]) {
      score += lastIdx === -1 ? 10 : Math.max(0, 10 - (i - lastIdx - 1))
      lastIdx = i
      qi++
    }
  }
  return qi === q.length ? score : 0
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Component                                                              */
/* ─────────────────────────────────────────────────────────────────────── */

export default function Codex() {
  const open = useWorldStateQuery((s) => s.codexOpen)
  const close = useWorldStateQuery((s) => s.closeCodex)
  const setWaypoint = useWorldStateQuery((s) => s.setMinimapWaypoint)

  const [tab, setTab] = useState<Tab>('characters')
  const [query, setQuery] = useState('')
  const [selected, setSelected] = useState<CodexEntry | null>(null)
  const [db, setDb] = useState<RICanonDB | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)

  useEffect(() => {
    if (!open) return
    let cancelled = false
    loadCanonDatabase()
      .then((d) => {
        if (!cancelled) setDb(d as unknown as RICanonDB)
      })
      .catch((e) => {
        if (!cancelled) setLoadError(String(e))
      })
    return () => {
      cancelled = true
    }
  }, [open])

  const entries = useMemo<CodexEntry[]>(() => {
    if (!db) return []
    switch (tab) {
      case 'characters': return toEntries(db.characters, 'characters')
      case 'locations':  return toEntries(db.locations, 'locations')
      case 'artifacts':  return toEntries(db.artifacts, 'artifacts')
      case 'techniques': return toEntries(db.techniques, 'techniques')
    }
  }, [db, tab])

  const filtered = useMemo(() => {
    const scored = entries
      .map((e) => {
        const score = Math.max(
          fuzzyMatch(query, e.name),
          fuzzyMatch(query, e.nameCn ?? ''),
          fuzzyMatch(query, e.type ?? ''),
        )
        return { e, score }
      })
      .filter((r) => r.score > 0)
      .sort((a, b) => b.score - a.score)
    return scored.map((r) => r.e)
  }, [entries, query])

  const counts = useMemo(
    () => ({
      characters: db?.characters?.length ?? 0,
      locations: db?.locations?.length ?? 0,
      artifacts: db?.artifacts?.length ?? 0,
      techniques: db?.techniques?.length ?? 0,
    }),
    [db],
  )

  // ── Click entry → set minimap waypoint ──────────────────────────────
  const focusEntry = (e: CodexEntry) => {
    setSelected(e)
    // Try to resolve a position for the waypoint.
    const pos = resolvePosition(e, tab)
    if (pos) {
      setWaypoint({
        x: pos[0],
        z: pos[1],
        label: e.nameCn ?? e.name,
        color: '#fcd34d',
      })
    }
  }

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          className="fixed inset-0 z-50 flex items-center justify-center"
          style={{ background: 'rgba(0,0,0,0.78)', pointerEvents: 'auto', padding: 24 }}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onClick={close}
        >
          <motion.div
            className="codex-modal hud-ornate-4"
            initial={{ scale: 0.97, y: 16, opacity: 0 }}
            animate={{ scale: 1, y: 0, opacity: 1 }}
            exit={{ scale: 0.97, y: 16, opacity: 0 }}
            transition={{ duration: 0.28, ease: [0.22, 1, 0.36, 1] }}
            onClick={(e) => e.stopPropagation()}
            style={{
              width: 'min(1100px, 96vw)',
              height: 'min(720px, 88vh)',
              borderRadius: 2,
              display: 'flex',
              flexDirection: 'column',
            }}
          >
            <span className="hud-corner tl" />
            <span className="hud-corner tr" />
            <span className="hud-corner bl" />
            <span className="hud-corner br" />
            <div className="silk-shimmer" aria-hidden />

            {/* Header */}
            <div className="flex items-center justify-between" style={{ padding: '14px 18px', borderBottom: '1px solid rgba(212,175,55,0.25)' }}>
              <div className="flex items-center gap-3">
                <BookOpen size={18} style={{ color: 'var(--gold-400)' }} />
                <div>
                  <h2
                    className="font-cn"
                    style={{ fontSize: 22, color: 'var(--ivory)', letterSpacing: '0.06em', lineHeight: 1 }}
                  >
                    仙逆典 · Codex
                  </h2>
                  <p
                    className="font-en italic"
                    style={{ fontSize: 11, color: 'var(--ivory-dim)', letterSpacing: '0.06em' }}
                  >
                    Renegade Immortal canon — 630 attested entries
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={close}
                className="rounded p-1.5 transition hover:bg-white/10"
                aria-label="Close codex"
                style={{ color: 'var(--ivory-dim)' }}
              >
                <X size={18} />
              </button>
            </div>

            {/* Tabs + search */}
            <div
              className="flex items-center gap-3"
              style={{ padding: '10px 18px', borderBottom: '1px solid rgba(212,175,55,0.15)' }}
            >
              <TabBtn icon={<Scroll size={12} />} label="Characters" cn="人物" count={counts.characters} active={tab === 'characters'} onClick={() => { setTab('characters'); setSelected(null) }} />
              <TabBtn icon={<MapPin size={12} />} label="Locations" cn="地界" count={counts.locations} active={tab === 'locations'} onClick={() => { setTab('locations'); setSelected(null) }} />
              <TabBtn icon={<Star size={12} />} label="Artifacts" cn="法宝" count={counts.artifacts} active={tab === 'artifacts'} onClick={() => { setTab('artifacts'); setSelected(null) }} />
              <TabBtn icon={<Sparkles size={12} />} label="Techniques" cn="功法" count={counts.techniques} active={tab === 'techniques'} onClick={() => { setTab('techniques'); setSelected(null) }} />
              <div className="ml-auto flex items-center gap-2" style={{ position: 'relative' }}>
                <Search size={13} style={{ color: 'var(--ivory-dim)', position: 'absolute', left: 8 }} />
                <input
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  placeholder="Search entries…"
                  className="rounded"
                  style={{
                    background: 'rgba(0,0,0,0.5)',
                    border: '1px solid rgba(212,175,55,0.3)',
                    color: 'var(--ivory)',
                    fontFamily: 'var(--font-serif-en)',
                    fontSize: 13,
                    padding: '5px 10px 5px 26px',
                    width: 220,
                    outline: 'none',
                  }}
                />
              </div>
            </div>

            {/* Body — list + detail */}
            <div className="flex min-h-0 flex-1">
              {/* List */}
              <div
                className="hud-scroll"
                style={{
                  width: 340,
                  overflowY: 'auto',
                  borderRight: '1px solid rgba(212,175,55,0.15)',
                }}
              >
                {loadError && (
                  <div style={{ padding: 16, fontSize: 12, color: 'var(--vermillion-bright)' }}>
                    Failed to load canon DB: {loadError}
                  </div>
                )}
                {!loadError && !db && (
                  <div style={{ padding: 16, fontSize: 12, color: 'var(--ivory-dim)', fontStyle: 'italic' }}>
                    Loading canon database…
                  </div>
                )}
                {db && filtered.length === 0 && (
                  <div style={{ padding: 16, fontSize: 12, color: 'var(--ivory-dim)', fontStyle: 'italic' }}>
                    No entries match “{query}”.
                  </div>
                )}
                {filtered.map((e) => (
                  <button
                    key={e.id}
                    type="button"
                    onClick={() => focusEntry(e)}
                    className="block w-full text-left transition"
                    style={{
                      padding: '8px 14px',
                      borderBottom: '1px solid rgba(212,175,55,0.06)',
                      background:
                        selected?.id === e.id
                          ? 'linear-gradient(90deg, rgba(212,175,55,0.12), transparent)'
                          : 'transparent',
                      borderLeft:
                        selected?.id === e.id
                          ? '2px solid var(--gold-400)'
                          : '2px solid transparent',
                    }}
                    onMouseEnter={(ev) => {
                      if (selected?.id !== e.id) ev.currentTarget.style.background = 'rgba(212,175,55,0.06)'
                    }}
                    onMouseLeave={(ev) => {
                      if (selected?.id !== e.id) ev.currentTarget.style.background = 'transparent'
                    }}
                  >
                    <div className="flex items-baseline justify-between gap-2">
                      <span
                        className="font-cn"
                        style={{ fontSize: 13, color: 'var(--ivory)', fontWeight: 500 }}
                      >
                        {e.nameCn ?? e.name}
                      </span>
                      <span
                        style={{
                          fontFamily: 'var(--font-mono)',
                          fontSize: 9,
                          color: 'var(--ivory-dim)',
                        }}
                      >
                        {e.id}
                      </span>
                    </div>
                    {e.nameCn && (
                      <div
                        className="font-en italic truncate"
                        style={{ fontSize: 11, color: 'var(--ivory-dim)', marginTop: 1 }}
                      >
                        {e.name}
                      </div>
                    )}
                    {e.type && (
                      <div
                        className="mt-1"
                        style={{ fontSize: 9, color: 'var(--jade-300)', letterSpacing: '0.1em', textTransform: 'uppercase' }}
                      >
                        {e.type}
                      </div>
                    )}
                  </button>
                ))}
              </div>

              {/* Detail */}
              <div className="min-w-0 flex-1" style={{ padding: 18, overflowY: 'auto' }}>
                {selected ? (
                  <CodexDetail entry={selected} tab={tab} />
                ) : (
                  <div
                    className="flex h-full items-center justify-center"
                    style={{ color: 'var(--ivory-dim)', fontStyle: 'italic', fontSize: 13 }}
                  >
                    Select an entry to read its canon.
                  </div>
                )}
              </div>
            </div>

            {/* Footer */}
            <div
              className="flex items-center justify-between"
              style={{ padding: '8px 18px', borderTop: '1px solid rgba(212,175,55,0.15)', fontSize: 10, color: 'var(--ivory-dim)', letterSpacing: '0.1em' }}
            >
              <span className="uppercase">Canon Confidence 1-5 · NO fabricated chapter citations</span>
              <span className="font-mono">{filtered.length} / {entries.length} entries</span>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Detail panel                                                           */
/* ─────────────────────────────────────────────────────────────────────── */

function CodexDetail({ entry, tab }: { entry: CodexEntry; tab: Tab }) {
  const r = entry.raw
  return (
    <motion.div
      key={entry.id}
      className="codex-entry fade-in"
      initial={{ opacity: 0, y: 6 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.2 }}
    >
      <div className="flex items-start justify-between gap-4">
        <div>
          <h3
            className="font-cn"
            style={{ fontSize: 24, color: 'var(--ivory)', letterSpacing: '0.04em' }}
          >
            {entry.nameCn ?? entry.name}
          </h3>
          {entry.nameCn && (
            <p
              className="font-en italic"
              style={{ fontSize: 15, color: 'var(--gold-300)', letterSpacing: '0.04em', marginTop: 2 }}
            >
              {entry.name}
            </p>
          )}
        </div>
        <ConfBadge conf={entry.canonConfidence} />
      </div>

      <div
        className="mt-1 flex items-center gap-2"
        style={{ fontSize: 10, color: 'var(--jade-300)', letterSpacing: '0.16em', textTransform: 'uppercase' }}
      >
        <span className="font-mono">{entry.id}</span>
        <span style={{ opacity: 0.4 }}>·</span>
        <span>{entry.type ?? 'unknown'}</span>
      </div>

      {entry.description && (
        <p
          className="mt-3 font-en"
          style={{ fontSize: 13, color: 'var(--ivory)', lineHeight: 1.7, opacity: 0.95 }}
        >
          {entry.description}
        </p>
      )}

      <DetailFacts tab={tab} raw={r} />

      {hasPosition(entry, tab) && (
        <p
          className="mt-4 font-en italic"
          style={{ fontSize: 11, color: 'var(--jade-300)', letterSpacing: '0.04em' }}
        >
          <MapPin size={10} style={{ display: 'inline', marginRight: 4 }} />
          Waypoint set on minimap.
        </p>
      )}

      {r.source && (
        <p
          className="mt-4 font-en"
          style={{ fontSize: 10, color: 'var(--ivory-dim)', fontStyle: 'italic', borderTop: '1px dashed rgba(212,175,55,0.18)', paddingTop: 8 }}
        >
          Source: {String(r.source)}
        </p>
      )}
    </motion.div>
  )
}

function DetailFacts({ tab, raw }: { tab: Tab; raw: Record<string, unknown> }) {
  const sections: { title: string; items: string[] }[] = []
  const knownFacts = (raw.knownFacts as string[] | undefined) ?? []
  if (knownFacts.length) sections.push({ title: 'Known Facts', items: knownFacts })

  if (tab === 'characters') {
    const rel = (raw.relationships as Array<{ target: string; relation: string }> | undefined) ?? []
    if (rel.length) {
      sections.push({ title: 'Relationships', items: rel.map((r) => `${r.target} — ${r.relation}`) })
    }
  }
  if (tab === 'artifacts') {
    const ab = (raw.abilities as string[] | undefined) ?? []
    if (ab.length) sections.push({ title: 'Abilities', items: ab })
  }
  if (tab === 'techniques') {
    const ef = (raw.effects as string[] | undefined) ?? []
    if (ef.length) sections.push({ title: 'Effects', items: ef })
    const users = (raw.knownUsers as string[] | undefined) ?? []
    if (users.length) sections.push({ title: 'Known Users', items: users })
  }
  if (tab === 'locations') {
    const ev = (raw.keyEvents as string[] | undefined) ?? []
    if (ev.length) sections.push({ title: 'Key Events', items: ev })
    const fac = (raw.associatedFactions as string[] | undefined) ?? []
    if (fac.length) sections.push({ title: 'Associated Factions', items: fac })
  }

  return (
    <div className="mt-4 space-y-3">
      {sections.map((s) => (
        <div key={s.title}>
          <div
            className="mb-1 uppercase"
            style={{ fontSize: 9, letterSpacing: '0.24em', color: 'var(--gold-300)', opacity: 0.85 }}
          >
            {s.title}
          </div>
          <ul className="space-y-1">
            {s.items.map((it, i) => (
              <li
                key={i}
                className="font-en"
                style={{
                  fontSize: 12,
                  color: 'var(--ivory)',
                  lineHeight: 1.6,
                  paddingLeft: 12,
                  position: 'relative',
                }}
              >
                <span
                  style={{
                    position: 'absolute',
                    left: 0,
                    top: 7,
                    width: 4,
                    height: 4,
                    background: 'var(--jade-300)',
                    transform: 'rotate(45deg)',
                  }}
                />
                {it}
              </li>
            ))}
          </ul>
        </div>
      ))}
    </div>
  )
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Position resolver — for the minimap waypoint                           */
/* ─────────────────────────────────────────────────────────────────────── */

function hasPosition(entry: CodexEntry, tab: Tab): boolean {
  return resolvePosition(entry, tab) !== null
}

function resolvePosition(entry: CodexEntry, tab: Tab): [number, number] | null {
  if (tab === 'characters') {
    const seed = SEED_ACTORS.find(
      (a) => a.name === entry.name || a.nameCn === entry.nameCn,
    )
    if (seed) return seed.pos
  }
  if (tab === 'locations') {
    const placed = PLANET_SUZAKU_PLACEMENT.find(
      (p) => p.name === entry.name || p.nameCn === entry.nameCn,
    )
    if (placed) return placed.position
  }
  return null
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Tab button                                                             */
/* ─────────────────────────────────────────────────────────────────────── */

function TabBtn({
  icon,
  label,
  cn,
  count,
  active,
  onClick,
}: {
  icon: React.ReactNode
  label: string
  cn: string
  count: number
  active: boolean
  onClick: () => void
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="flex items-center gap-1.5 rounded transition"
      style={{
        padding: '4px 10px',
        background: active ? 'rgba(212,175,55,0.14)' : 'transparent',
        border: active ? '1px solid rgba(212,175,55,0.5)' : '1px solid transparent',
        color: active ? 'var(--gold-300)' : 'var(--ivory-dim)',
        cursor: 'pointer',
      }}
    >
      {icon}
      <span className="font-en" style={{ fontSize: 12, letterSpacing: '0.04em' }}>{label}</span>
      <span className="font-cn" style={{ fontSize: 11, opacity: 0.7 }}>{cn}</span>
      <span
        className="ml-1 rounded"
        style={{
          fontFamily: 'var(--font-mono)',
          fontSize: 9,
          padding: '0 4px',
          background: 'rgba(0,0,0,0.4)',
          color: 'var(--ivory-dim)',
        }}
      >
        {count}
      </span>
    </button>
  )
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Types                                                                  */
/* ─────────────────────────────────────────────────────────────────────── */

interface RICanonDB {
  characters: any[]
  locations: any[]
  artifacts: any[]
  techniques: any[]
}
