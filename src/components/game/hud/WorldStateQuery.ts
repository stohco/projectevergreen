/**
 * WorldStateQuery — the Zustand store backing the Er Gen Verse HUD.
 *
 * Three slices:
 *   1. player — persisted to localStorage (name, realm, qi, health, etc.)
 *   2. world  — transient (time, weather, nearby actors / threats / veins)
 *   3. ui     — transient (codex / debug / menu / dialog / spells / minimap)
 *
 * Player state only is persisted (per task spec). Settings are also
 * persisted. World/UI state resets every boot — it's the live simulation
 * surface.
 *
 * Canon fidelity:
 *   - Default player: 王林 Wang Lin, the protagonist (canon N01, conf 5).
 *   - Default realm: Foundation Establishment (筑基期) — mod-original
 *     starting point (Wang Lin reaches this around chapter 80; we start him
 *     here for playability).
 *   - Default spawn: Remote Village in Zhao Country (赵国偏僻山村), the
 *     novel-attested origin. Coordinates come from PlanetSuzakuPlacement.
 *   - Default faction: Heng Yue Sect (恒岳派) — Wang Lin's first sect.
 *   - NO invented chapter citations.
 */
'use client'

import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'

/* ─────────────────────────────────────────────────────────────────────── */
/*  Cultivation realms (canon order)                                       */
/* ─────────────────────────────────────────────────────────────────────── */

export interface RealmInfo {
  id: string
  en: string
  cn: string
  short: string
  order: number
}

export const CULTIVATION_REALMS: RealmInfo[] = [
  { id: 'qi_condensation',  en: 'Qi Condensation',           cn: '凝气期', short: '凝气', order: 0 },
  { id: 'foundation',       en: 'Foundation Establishment',  cn: '筑基期', short: '筑基', order: 1 },
  { id: 'core_formation',   en: 'Core Formation',            cn: '结丹期', short: '结丹', order: 2 },
  { id: 'nascent_soul',     en: 'Nascent Soul',              cn: '元婴期', short: '元婴', order: 3 },
  { id: 'soul_transform',   en: 'Soul Transformation',       cn: '化神期', short: '化神', order: 4 },
  { id: 'ascendant',        en: 'Ascendant',                 cn: '问鼎期', short: '问鼎', order: 5 },
  { id: 'illusory_yin',     en: 'Illusory Yin',              cn: '窥涅期', short: '窥涅', order: 6 },
  { id: 'nirvana_scraper',  en: 'Nirvana Scraper',           cn: '净涅期', short: '净涅', order: 7 },
  { id: 'arcane',           en: 'Arcane',                    cn: '空境期', short: '空境', order: 8 },
  { id: 'spirit_severing',  en: 'Spirit Severing',           cn: '天境期', short: '天境', order: 9 },
  { id: 'heaven_trampling', en: 'Heaven Trampling',          cn: '步天期', short: '步天', order: 10 },
]

export function getRealm(id: string): RealmInfo {
  return CULTIVATION_REALMS.find((r) => r.id === id) ?? CULTIVATION_REALMS[1]
}
export function getNextRealm(id: string): RealmInfo | null {
  const r = getRealm(id)
  return CULTIVATION_REALMS.find((n) => n.order === r.order + 1) ?? null
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Slice types                                                            */
/* ─────────────────────────────────────────────────────────────────────── */

export interface PlayerState {
  name: string
  nameCn: string
  realmId: string
  realmProgress: number // 0-1 within the current realm
  qi: number
  maxQi: number
  qiRegenerating: boolean
  health: number
  maxHealth: number
  spiritSenseRange: number // blocks
  position: [number, number, number]
  faction: string
  hostility: number // 0-100 — threat rating toward the player
}

export interface NearbyActor {
  id: string
  name: string
  nameCn?: string
  realm: string
  hostility: number // 0-100
  distance: number // blocks
  faction: string
}

export interface NearbyThreat {
  beastName: string
  beastNameCn?: string
  rank: string
  count: number
  distance: number
}

export interface SpiritVeinProximity {
  name: string
  distance: number
  quality: number // 1-10
  element: string
}

export interface CurrentLocation {
  name: string
  nameCn?: string
  biome: string
}

export type WeatherKind = 'clear' | 'cloudy' | 'rain' | 'storm' | 'snow' | 'mist'

export interface WorldState {
  time: number // 0..1 (0 = midnight, 0.5 = noon)
  day: number
  weather: WeatherKind
  biome: string
  nearbyActors: NearbyActor[]
  nearbyThreats: NearbyThreat[]
  spiritVeinNear: SpiritVeinProximity | null
  currentLocation: CurrentLocation | null
}

export interface UIDialog {
  speaker: string
  speakerCn?: string
  portrait?: string // url or data-url; 80x80
  lines: string[]
  lineIndex: number
}

export interface SpellSlot {
  id: number // 1..8
  name: string
  nameCn: string
  icon: string // lucide icon key (resolved by SpellHotbar)
  cost: number // instant qi cost
  costPerSec?: number // for toggles
  cooldown: number // ms
  toggle?: boolean
  description: string
  empty?: boolean
}

export const SPELL_SLOTS: SpellSlot[] = [
  { id: 1, name: 'Sword Qi',         nameCn: '剑气',       icon: 'sword',  cost: 5, cooldown: 1500, description: 'Ranged sword-strike of compressed qi. Costs 5 qi.' },
  { id: 2, name: 'Sword Flight',     nameCn: '御剑飞行',   icon: 'wing',   cost: 0, costPerSec: 2, cooldown: 0,    toggle: true, description: 'Mount your flying sword. Drains 2 qi/sec while active.' },
  { id: 3, name: 'Divine Sense',     nameCn: '神识',       icon: 'eye',    cost: 8, cooldown: 6000, description: 'Pulse your spirit sense — reveals nearby entities for 6 seconds.' },
  { id: 4, name: 'Qi Meditation',    nameCn: '打坐',       icon: 'lotus',  cost: 0, costPerSec: 0, cooldown: 0, toggle: true, description: 'Cross-legged cultivation. 3× qi regen. Cannot move.' },
  { id: 5, name: '', nameCn: '', icon: '', cost: 0, cooldown: 0, description: '', empty: true },
  { id: 6, name: '', nameCn: '', icon: '', cost: 0, cooldown: 0, description: '', empty: true },
  { id: 7, name: '', nameCn: '', icon: '', cost: 0, cooldown: 0, description: '', empty: true },
  { id: 8, name: '', nameCn: '', icon: '', cost: 0, cooldown: 0, description: '', empty: true },
]

export interface MinimapWaypoint {
  x: number
  z: number
  label: string
  color?: string
}

export interface DebugInfo {
  fps: number
  frameTime: number // ms
  chunks: number
  cameraYaw: number // degrees
  cameraPitch: number
  memoryMb: number
  entities: number
  triangles: number
  drawCalls: number
}

export interface Settings {
  renderDistance: number // chunks
  fov: number
  bloom: boolean
  ssao: boolean
  motionBlur: boolean
  vignette: boolean
}

export interface BreakthroughToast {
  realmEn: string
  realmCn: string
  timestamp: number
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Defaults                                                               */
/* ─────────────────────────────────────────────────────────────────────── */

const DEFAULT_PLAYER: PlayerState = {
  name: 'Wang Lin',
  nameCn: '王林',
  realmId: 'foundation',
  realmProgress: 0.12,
  qi: 120,
  maxQi: 500,
  qiRegenerating: true,
  health: 100,
  maxHealth: 100,
  spiritSenseRange: 64,
  position: [0, 70, 0],
  faction: 'Heng Yue Sect (disciple)',
  hostility: 10,
}

const DEFAULT_WORLD: WorldState = {
  time: 0.35,
  day: 1,
  weather: 'clear',
  biome: 'plains',
  nearbyActors: [],
  nearbyThreats: [],
  spiritVeinNear: null,
  currentLocation: {
    name: 'Remote Village in Zhao Country',
    nameCn: '赵国偏僻山村',
    biome: 'plains',
  },
}

const DEFAULT_DEBUG: DebugInfo = {
  fps: 0,
  frameTime: 0,
  chunks: 0,
  cameraYaw: 0,
  cameraPitch: 0,
  memoryMb: 0,
  entities: 0,
  triangles: 0,
  drawCalls: 0,
}

const DEFAULT_SETTINGS: Settings = {
  renderDistance: 8,
  fov: 70,
  bloom: true,
  ssao: false,
  motionBlur: false,
  vignette: true,
}

/* ─────────────────────────────────────────────────────────────────────── */
/*  Store                                                                  */
/* ─────────────────────────────────────────────────────────────────────── */

export interface HUDStore {
  // Persisted
  player: PlayerState
  settings: Settings

  // Transient
  world: WorldState

  // UI
  codexOpen: boolean
  debugOpen: boolean
  menuOpen: boolean
  dialog: UIDialog | null
  dialogOpen: boolean
  selectedSpellSlot: number | null
  spellCooldowns: Record<number, number> // slotId -> ready-at timestamp
  spellActive: Record<number, boolean> // for toggles
  minimapZoom: number // radius in blocks
  minimapWaypoint: MinimapWaypoint | null
  loadingProgress: number // 0..1
  loadingPhase: string
  bootComplete: boolean
  breakthrough: BreakthroughToast | null
  debug: DebugInfo

  // Actions — player
  updatePlayer: (patch: Partial<PlayerState>) => void
  // Actions — world
  updateWorld: (patch: Partial<WorldState>) => void
  // Actions — ui
  openCodex: () => void
  closeCodex: () => void
  toggleCodex: () => void
  openMenu: () => void
  closeMenu: () => void
  toggleMenu: () => void
  toggleDebug: () => void
  showDialog: (d: { speaker: string; speakerCn?: string; portrait?: string; lines: string[] }) => void
  advanceDialog: () => void
  closeDialog: () => void
  selectSpellSlot: (slot: number | null) => void
  triggerSpellCooldown: (slot: number, ms: number) => void
  setSpellActive: (slot: number, active: boolean) => void
  setMinimapZoom: (r: number) => void
  setMinimapWaypoint: (w: MinimapWaypoint | null) => void
  setLoading: (progress: number, phase: string) => void
  setBootComplete: (v: boolean) => void
  showBreakthrough: (realmEn: string, realmCn: string) => void
  clearBreakthrough: () => void
  updateDebug: (patch: Partial<DebugInfo>) => void
  updateSettings: (patch: Partial<Settings>) => void
  saveGame: () => boolean
  loadGame: () => boolean
  hasSavedGame: () => boolean
}

const WORLD_STORE_KEY = 'ergenverse.worldstore'
const HUD_PERSIST_KEY = 'ergenverse.hud'

export const useWorldStateQuery = create<HUDStore>()(
  persist(
    (set, get) => ({
      player: { ...DEFAULT_PLAYER },
      settings: { ...DEFAULT_SETTINGS },
      world: { ...DEFAULT_WORLD },

      codexOpen: false,
      debugOpen: false,
      menuOpen: false,
      dialog: null,
      dialogOpen: false,
      selectedSpellSlot: null,
      spellCooldowns: {},
      spellActive: {},
      minimapZoom: 256,
      minimapWaypoint: null,
      loadingProgress: 0,
      loadingPhase: 'Initializing',
      bootComplete: false,
      breakthrough: null,
      debug: { ...DEFAULT_DEBUG },

      updatePlayer: (patch) =>
        set((s) => ({ player: { ...s.player, ...patch } })),

      updateWorld: (patch) =>
        set((s) => ({ world: { ...s.world, ...patch } })),

      openCodex: () => set({ codexOpen: true }),
      closeCodex: () => set({ codexOpen: false }),
      toggleCodex: () => set((s) => ({ codexOpen: !s.codexOpen })),

      openMenu: () => set({ menuOpen: true }),
      closeMenu: () => set({ menuOpen: false }),
      toggleMenu: () => set((s) => ({ menuOpen: !s.menuOpen })),

      toggleDebug: () => set((s) => ({ debugOpen: !s.debugOpen })),

      showDialog: (d) =>
        set({
          dialog: { ...d, lineIndex: 0 },
          dialogOpen: true,
        }),
      advanceDialog: () =>
        set((s) => {
          if (!s.dialog) return { dialogOpen: false }
          const next = s.dialog.lineIndex + 1
          if (next >= s.dialog.lines.length) {
            return { dialog: null, dialogOpen: false }
          }
          return { dialog: { ...s.dialog, lineIndex: next } }
        }),
      closeDialog: () => set({ dialog: null, dialogOpen: false }),

      selectSpellSlot: (slot) => set({ selectedSpellSlot: slot }),
      triggerSpellCooldown: (slot, ms) =>
        set((s) => ({
          spellCooldowns: { ...s.spellCooldowns, [slot]: Date.now() + ms },
        })),
      setSpellActive: (slot, active) =>
        set((s) => ({
          spellActive: { ...s.spellActive, [slot]: active },
        })),

      setMinimapZoom: (r) =>
        set({ minimapZoom: Math.max(64, Math.min(1024, r)) }),
      setMinimapWaypoint: (w) => set({ minimapWaypoint: w }),

      setLoading: (progress, phase) =>
        set({ loadingProgress: progress, loadingPhase: phase }),
      setBootComplete: (v) => set({ bootComplete: v }),

      showBreakthrough: (realmEn, realmCn) =>
        set({ breakthrough: { realmEn, realmCn, timestamp: Date.now() } }),
      clearBreakthrough: () => set({ breakthrough: null }),

      updateDebug: (patch) =>
        set((s) => ({ debug: { ...s.debug, ...patch } })),

      updateSettings: (patch) =>
        set((s) => ({ settings: { ...s.settings, ...patch } })),

      saveGame: () => {
        try {
          // Persist the player + settings (zustand persist handles this
          // automatically, but we re-flush here to be explicit).
          const state = get()
          localStorage.setItem(
            HUD_PERSIST_KEY,
            JSON.stringify({
              player: state.player,
              settings: state.settings,
            }),
          )
          // Delegate world-delta persistence to WorldDeltaStore if a
          // runtime bridge has registered one (see useEngine.ts).
          const bridge = (globalThis as any).__ergenBridge
          if (bridge?.saveWorld) bridge.saveWorld(WORLD_STORE_KEY)
          return true
        } catch (e) {
          console.warn('[HUD] saveGame failed', e)
          return false
        }
      },

      loadGame: () => {
        try {
          const raw = localStorage.getItem(HUD_PERSIST_KEY)
          if (!raw) return false
          const parsed = JSON.parse(raw) as {
            player?: Partial<PlayerState>
            settings?: Partial<Settings>
          }
          if (parsed.player) {
            set((s) => ({ player: { ...s.player, ...parsed.player! } }))
          }
          if (parsed.settings) {
            set((s) => ({ settings: { ...s.settings, ...parsed.settings! } }))
          }
          const bridge = (globalThis as any).__ergenBridge
          if (bridge?.loadWorld) bridge.loadWorld(WORLD_STORE_KEY)
          return true
        } catch (e) {
          console.warn('[HUD] loadGame failed', e)
          return false
        }
      },

      hasSavedGame: () => {
        try {
          return !!localStorage.getItem(HUD_PERSIST_KEY)
        } catch {
          return false
        }
      },
    }),
    {
      name: HUD_PERSIST_KEY,
      storage: createJSONStorage(() => localStorage),
      // Persist only player + settings — world/ui state resets per boot.
      partialize: (s) => ({ player: s.player, settings: s.settings }),
      version: 1,
    },
  ),
)

/* ─────────────────────────────────────────────────────────────────────── */
/*  Selectors / helpers                                                    */
/* ─────────────────────────────────────────────────────────────────────── */

export function qiPercent(p: PlayerState): number {
  return p.maxQi > 0 ? (p.qi / p.maxQi) * 100 : 0
}

export function qiCritical(p: PlayerState): boolean {
  return qiPercent(p) < 5
}

export function healthHearts(p: PlayerState): { filled: number; total: number } {
  const total = Math.max(1, Math.round(p.maxHealth / 20))
  const filled = Math.round(p.health / 20)
  return { filled, total }
}
