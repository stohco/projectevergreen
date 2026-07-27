/**
 * GraphQueryService — the O(1) query layer over WorldGraph.
 *
 * Ported from dev.ergenverse.graph.GraphQueryService (Java, CRON-137/139).
 * Provides graph-first queries for:
 *   Q1: whoExistsAt(location) — NPCs at a location
 *   Q2: threatsNearSettlement(settlement) — hostile NPCs near a settlement
 *   Q3: socialConnections(npc) — friends/family/master of an NPC
 *   Q4: ownsWhat(npc) — items owned by an NPC
 *   Q5: whyUntaken(npc) — unresolved opportunities (karma edges)
 *   Q6: naturalNext(npc) — the next karmic event (karmic edges)
 *
 * Every query is a subgraph traversal — O(E) where E is edges from the
 * start node. With 630 nodes + 500 edges, every query is sub-millisecond.
 * This is what enables 60fps with many entities: the simulation never
 * brute-force scans; it asks the graph.
 *
 * Graph engineering applied to ALL systems:
 *   - Actor materialization: Q1 at player's chunk → spawn NPCs
 *   - Rumor propagation: Q3 traverses social edges to spread info
 *   - Threat index: Q2 aggregates hostility edges → AI aggression
 *   - Quest opportunities: Q5 surfaces unresolved karma for the player
 *   - Spatial queries: NEAR edges connect adjacent locations
 *   - NPC memory: MEMORY edges (write-back from OptMem-style log)
 */
import { WorldGraph, type GraphNode, type EdgeType, type NodeType } from './WorldGraph'

export interface ObjectEntry {
  id: string
  type: NodeType
  displayName: string
  displayNameCn?: string
  trueState: string
  canonStatus: string
  meta?: Record<string, unknown>
}

export interface ThreatEntry {
  npc: GraphNode
  hostility: number
  distance: number
  realm: string
}

export interface SocialEntry {
  npc: GraphNode
  relation: string
  edgeType: EdgeType
}

export interface OpportunityEntry {
  id: string
  kind: string
  description: string
  targetNpc?: GraphNode
  targetLocation?: GraphNode
}

export class GraphQueryService {
  constructor(private readonly graph: WorldGraph) {}

  /** Q1: Who exists at a location? Returns all NPCs with LOCATED_IN → location. */
  whoExistsAt(locationName: string): ObjectEntry[] {
    const locNode = this.graph.resolveByName(locationName)
    if (!locNode || locNode.type !== 'location') return []
    const entries: ObjectEntry[] = []
    for (const e of this.graph.inEdges(locNode.id)) {
      if (e.type === 'LOCATED_IN') {
        const npc = this.graph.node(e.from)
        if (npc && npc.type === 'npc') {
          entries.push(this.nodeToObjectEntry(npc))
        }
      }
    }
    return entries
  }

  /** Q2: Threats near a settlement. Aggregates HOSTILE_TO edges + spatial NEAR. */
  threatsNearSettlement(settlementName: string, maxDepth = 2): ThreatEntry[] {
    const settlement = this.graph.resolveByName(settlementName)
    if (!settlement) return []
    const threats: ThreatEntry[] = []
    // Walk NEAR edges up to maxDepth to find nearby locations.
    const nearbyLocs = this.graph.traverse(settlement.id, maxDepth, 'NEAR')
    const locIds = new Set([settlement.id, ...nearbyLocs.map((n) => n.id)])
    // For each nearby location, find NPCs located there.
    for (const locId of locIds) {
      for (const e of this.graph.inEdges(locId)) {
        if (e.type !== 'LOCATED_IN') continue
        const npc = this.graph.node(e.from)
        if (!npc || npc.type !== 'npc') continue
        // Check if this NPC has HOSTILE_TO edges (to anyone).
        const hostileEdges = this.graph.outEdges(npc.id).filter((ed) => ed.type === 'HOSTILE_TO')
        if (hostileEdges.length > 0) {
          threats.push({
            npc,
            hostility: hostileEdges.length,
            distance: this.estimateDistance(settlement, npc),
            realm: (npc.realm as string) ?? 'unknown',
          })
        }
      }
    }
    return threats.sort((a, b) => b.hostility - a.hostility)
  }

  /** Q3: Social connections of an NPC. Friends, family, master, disciples. */
  socialConnections(npcName: string): SocialEntry[] {
    const npc = this.graph.resolveByName(npcName)
    if (!npc || npc.type !== 'npc') return []
    const entries: SocialEntry[] = []
    for (const e of this.graph.outEdges(npc.id)) {
      const target = this.graph.node(e.to)
      if (!target) continue
      if (['FAMILIAR_WITH', 'FAMILY', 'MASTER_OF', 'DISCIPLE_OF', 'ALLIED_WITH'].includes(e.type)) {
        entries.push({
          npc: target,
          relation: this.relationLabel(e.type),
          edgeType: e.type,
        })
      }
    }
    // Also check incoming (someone considers THIS npc a friend/master).
    for (const e of this.graph.inEdges(npc.id)) {
      const source = this.graph.node(e.from)
      if (!source) continue
      if (['FAMILIAR_WITH', 'FAMILY', 'MASTER_OF', 'DISCIPLE_OF', 'ALLIED_WITH'].includes(e.type) &&
          !entries.find((en) => en.npc.id === source.id)) {
        entries.push({
          npc: source,
          relation: this.relationLabel(e.type, true),
          edgeType: e.type,
        })
      }
    }
    return entries
  }

  /** Q4: What items does an NPC own? */
  ownsWhat(npcName: string): ObjectEntry[] {
    const npc = this.graph.resolveByName(npcName)
    if (!npc || npc.type !== 'npc') return []
    const entries: ObjectEntry[] = []
    for (const e of this.graph.outEdges(npc.id)) {
      if (e.type === 'OWNS') {
        const item = this.graph.node(e.to)
        if (item && item.type === 'item') {
          entries.push(this.nodeToObjectEntry(item))
        }
      }
    }
    return entries
  }

  /** Q5: Why is an opportunity untaken? (Unresolved karmic events.) */
  whyUntaken(npcName: string): OpportunityEntry[] {
    const npc = this.graph.resolveByName(npcName)
    if (!npc) return []
    const entries: OpportunityEntry[] = []
    // Look for OPPORTUNITY_FOR edges targeting this NPC.
    for (const e of this.graph.inEdges(npc.id)) {
      if (e.type === 'OPPORTUNITY_FOR') {
        const opp = this.graph.node(e.from)
        if (opp && opp.type === 'opportunity') {
          entries.push({
            id: opp.id,
            kind: (opp.meta?.kind as string) ?? 'unknown',
            description: opp.displayName,
            targetNpc: npc,
          })
        }
      }
    }
    return entries
  }

  /** Q6: What's the natural next event for this NPC? (Karmic edges.) */
  naturalNext(npcName: string): OpportunityEntry[] {
    const npc = this.graph.resolveByName(npcName)
    if (!npc) return []
    const entries: OpportunityEntry[] = []
    for (const e of this.graph.outEdges(npc.id)) {
      if (e.type === 'KARMIC_DEBT' || e.type === 'GRUDGE') {
        const target = this.graph.node(e.to)
        if (target) {
          entries.push({
            id: `${npc.id}-${target.id}-${e.type}`,
            kind: e.type,
            description: `${npc.displayName} has ${e.type === 'KARMIC_DEBT' ? 'a karmic debt to' : 'a grudge against'} ${target.displayName}`,
            targetNpc: target,
          })
        }
      }
    }
    return entries
  }

  /** Spatial query: NPCs within `radius` blocks of a world position. */
  npcsNearPosition(x: number, z: number, radius: number): GraphNode[] {
    // This requires NPC nodes to have position meta. Falls back to
    // returning all NPCs if no position data exists (graph-only mode).
    const allNpcs = this.graph.nodesByType('npc')
    return allNpcs.filter((n) => {
      const pos = n.meta?.position as { x: number; z: number } | undefined
      if (!pos) return false
      const dx = pos.x - x
      const dz = pos.z - z
      return dx * dx + dz * dz <= radius * radius
    })
  }

  /** Graph stats for the debug overlay. */
  stats() {
    return this.graph.stats()
  }

  private nodeToObjectEntry(n: GraphNode): ObjectEntry {
    return {
      id: n.id,
      type: n.type,
      displayName: n.displayName,
      displayNameCn: n.displayNameCn,
      trueState: (n.meta?.status as string) ?? 'unknown',
      canonStatus: n.canonStatus,
      meta: n.meta,
    }
  }

  private estimateDistance(a: GraphNode, b: GraphNode): number {
    const pa = a.meta?.position as { x: number; z: number } | undefined
    const pb = b.meta?.position as { x: number; z: number } | undefined
    if (!pa || !pb) return 999
    return Math.sqrt((pa.x - pb.x) ** 2 + (pa.z - pb.z) ** 2)
  }

  private relationLabel(edgeType: EdgeType, incoming = false): string {
    switch (edgeType) {
      case 'FAMILIAR_WITH': return 'acquaintance'
      case 'FAMILY': return 'family'
      case 'MASTER_OF': return incoming ? 'master of (them)' : 'master'
      case 'DISCIPLE_OF': return incoming ? 'disciple of (them)' : 'disciple'
      case 'ALLIED_WITH': return 'ally'
      case 'HOSTILE_TO': return 'enemy'
      case 'KARMIC_DEBT': return 'karmic debtor'
      case 'GRUDGE': return 'grudge'
      default: return edgeType.toLowerCase()
    }
  }
}
