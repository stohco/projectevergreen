// src/lib/sim/world-memory.ts
// World Memory System. Stores "what changed" not "what happened."
// Only significant events persist. No rabbit-moved-2-blocks. No per-NPC trivia.
// This creates the feeling that history is continuously being written.

import type { RealmId } from './types';

// ─── World Memory Entry ─────────────────────────────────────────────
export type MemorySignificance = 'legendary' | 'major' | 'notable' | 'minor';

export interface WorldMemory {
  id: string;
  year: number;
  era: string;
  significance: MemorySignificance;
  category: 'player_action' | 'world_event' | 'faction_change' | 'ecology_shift' | 'cosmic' | 'discovery' | 'destruction' | 'creation';
  title: string;
  // What changed (the delta, not the full state):
  changes: { field: string; from: string; to: string }[];
  location?: string;
  // How NPCs reference it in dialogue:
  npcDialogue: string;
  // How long this memory persists (years; -1 = permanent):
  persistence: number;
}

// ─── World Memory Registry ──────────────────────────────────────────
// A living record of significant events that shapes NPC dialogue and world state.
export class WorldMemorySystem {
  memories: WorldMemory[] = [];

  record(memory: Omit<WorldMemory, 'id'>): WorldMemory {
    const m: WorldMemory = { ...memory, id: `mem_${Date.now()}_${Math.random().toString(36).slice(2, 7)}` };
    this.memories.push(m);
    // Sort by year descending (most recent first)
    this.memories.sort((a, b) => b.year - a.year);
    // Cap at 100 memories (keep the most significant)
    if (this.memories.length > 100) {
      this.memories.sort((a, b) => {
        const sigOrder = { legendary: 0, major: 1, notable: 2, minor: 3 };
        return sigOrder[a.significance] - sigOrder[b.significance];
      });
      this.memories = this.memories.slice(0, 100);
    }
    return m;
  }

  // Get memories that NPCs at a location would reference
  getRelevantMemories(location: string, currentYear: number): WorldMemory[] {
    return this.memories.filter((m) => {
      if (m.persistence !== -1 && currentYear - m.year > m.persistence) return false;
      // NPCs reference nearby memories + cosmic ones
      if (m.location === location) return true;
      if (m.significance === 'legendary' || m.significance === 'cosmic') return true;
      return false;
    });
  }

  // Get dialogue lines for NPCs based on world memory
  getDialogueForNPC(location: string, currentYear: number): string[] {
    const relevant = this.getRelevantMemories(location, currentYear);
    return relevant.slice(0, 3).map((m) => m.npcDialogue);
  }

  // Tick: decay old minor memories
  tick(years: number, currentYear: number): void {
    this.memories = this.memories.filter((m) => {
      if (m.persistence === -1) return true; // permanent
      return currentYear - m.year < m.persistence;
    });
  }

  // Serialize for persistence
  toJSON(): WorldMemory[] { return this.memories; }
  fromJSON(data: WorldMemory[]): void { this.memories = data || []; }
}

// ─── Helper: record common events ───────────────────────────────────
export function recordPlayerAction(
  memSys: WorldMemorySystem,
  year: number,
  era: string,
  action: string,
  changes: { field: string; from: string; to: string }[],
  location?: string,
  significance: MemorySignificance = 'notable',
): void {
  const dialogueMap: Record<MemorySignificance, string> = {
    legendary: `Legends still speak of the year ${year}, when ${action.toLowerCase()}.`,
    major: `Do you remember? In year ${year}, ${action.toLowerCase()}.`,
    notable: `A few years back, ${action.toLowerCase()}.`,
    minor: `I heard something happened recently... ${action.toLowerCase()}.`,
  };
  memSys.record({
    year, era, significance,
    category: 'player_action',
    title: action,
    changes,
    location,
    npcDialogue: dialogueMap[significance],
    persistence: significance === 'legendary' ? -1 : significance === 'major' ? 500 : significance === 'notable' ? 100 : 20,
  });
}

export function recordWorldEvent(
  memSys: WorldMemorySystem,
  year: number,
  era: string,
  event: string,
  changes: { field: string; from: string; to: string }[],
  location?: string,
  significance: MemorySignificance = 'major',
): void {
  memSys.record({
    year, era, significance,
    category: 'world_event',
    title: event,
    changes,
    location,
    npcDialogue: `In year ${year}, ${event.toLowerCase()}.`,
    persistence: significance === 'legendary' ? -1 : significance === 'major' ? 500 : 100,
  });
}
