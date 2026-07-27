/**
 * WorldGraph — port of dev.ergenverse.graph.WorldGraph (CRON-137)
 *
 * In-memory directed graph of canon + simulation entities (NPCs, locations,
 * factions, items, techniques). 630 nodes + 500 edges in the Java version
 * (GraphBootstrap fills from RICanonicalDatabase); this TS port reads the
 * same JSON snapshot and produces a parallel structure.
 *
 * Graph engineering is our friend: graph-first query path runs at O(1)
 * lookup for cached names and O(E) for neighborhood scans, which is what
 * lets the world stay at 60fps with many things on screen simulating.
 */
import type {
  CanonCharacter,
  CanonLocation,
  CanonFaction,
} from '../canon/types'

export type NodeType =
  | 'npc'
  | 'location'
  | 'faction'
  | 'item'
  | 'technique'
  | 'spirit_vein'
  | 'landmark'
  | 'event'

export type EdgeType =
  | 'LOCATED_IN' // npc → location
  | 'MEMBER_OF' // npc → faction
  | 'LEADS' // npc → faction (npc is the leader)
  | 'FAMILIAR_WITH' // npc → npc (acquaintance)
  | 'FAMILY' // npc → npc (blood relation)
  | 'MASTER_OF' // npc → npc (master-disciple)
  | 'KARMIC_DEBT' // npc → npc
  | 'GRUDGE' // npc → npc
  | 'PARENT_LOCATION' // location → location
  | 'CONTROLS' // faction → location
  | 'OWNS' // npc → item
  | 'PRACTICES' // npc → technique
  | 'THREATENS' // npc → location (military threat)

export interface GraphNode {
  id: string
  type: NodeType
  displayName: string
  displayNameCn?: string
  canonId?: string
  /** Free-form attributes (cultivation realm, faction, position, etc.). */
  attributes?: Record<string, unknown>
}

export interface GraphEdge {
  from: string
  to: string
  type: EdgeType
  weight?: number
  attributes?: Record<string, unknown>
}

export class WorldGraph {
  private readonly nodes: Map<string, GraphNode> = new Map()
  private readonly edges: GraphEdge[] = []
  /** Adjacency: from-id → outgoing edges */
  private readonly outAdj: Map<string, GraphEdge[]> = new Map()
  /** Reverse adjacency: to-id → incoming edges */
  private readonly inAdj: Map<string, GraphEdge[]> = new Map()
  /** name → node-id cache (lowercased) */
  private readonly nameIndex: Map<string, string> = new Map()
  /** canon-id → node-id cache */
  private readonly canonIndex: Map<string, string> = new Map()
  /** type → set of node-ids cache */
  private readonly typeIndex: Map<NodeType, Set<string>> = new Map()

  addNode(node: GraphNode): void {
    if (this.nodes.has(node.id)) return
    this.nodes.set(node.id, node)
    if (!this.outAdj.has(node.id)) this.outAdj.set(node.id, [])
    if (!this.inAdj.has(node.id)) this.inAdj.set(node.id, [])
    this.nameIndex.set(node.displayName.toLowerCase(), node.id)
    if (node.displayNameCn) this.nameIndex.set(node.displayNameCn, node.id)
    if (node.canonId) this.canonIndex.set(node.canonId, node.id)
    if (!this.typeIndex.has(node.type)) this.typeIndex.set(node.type, new Set())
    this.typeIndex.get(node.type)!.add(node.id)
  }

  addEdge(edge: GraphEdge): void {
    // Skip duplicates (same from/to/type).
    const exists = this.edges.some(
      (e) => e.from === edge.from && e.to === edge.to && e.type === edge.type,
    )
    if (exists) return
    this.edges.push(edge)
    if (!this.outAdj.has(edge.from)) this.outAdj.set(edge.from, [])
    if (!this.inAdj.has(edge.to)) this.inAdj.set(edge.to, [])
    this.outAdj.get(edge.from)!.push(edge)
    this.inAdj.get(edge.to)!.push(edge)
  }

  getNode(id: string): GraphNode | undefined {
    return this.nodes.get(id)
  }

  resolveByCanonId(canonId: string): GraphNode | undefined {
    const id = this.canonIndex.get(canonId)
    return id ? this.nodes.get(id) : undefined
  }

  resolveByName(name: string): GraphNode | undefined {
    // Strategy 1: exact lowercase match.
    const id = this.nameIndex.get(name.toLowerCase())
    if (id) return this.nodes.get(id)
    // Strategy 2: substring match (first hit, non-deterministic on ties).
    for (const [k, v] of this.nameIndex) {
      if (k.includes(name.toLowerCase())) return this.nodes.get(v)
    }
    return undefined
  }

  nodesByType(type: NodeType): GraphNode[] {
    const set = this.typeIndex.get(type)
    if (!set) return []
    return Array.from(set).map((id) => this.nodes.get(id)!)
  }

  outEdges(id: string): GraphEdge[] {
    return this.outAdj.get(id) ?? []
  }

  inEdges(id: string): GraphEdge[] {
    return this.inAdj.get(id) ?? []
  }

  outEdgesOfType(id: string, type: EdgeType): GraphEdge[] {
    return (this.outAdj.get(id) ?? []).filter((e) => e.type === type)
  }

  /** All edges, for serialization or stats. */
  allEdges(): GraphEdge[] {
    return [...this.edges]
  }

  allNodes(): GraphNode[] {
    return Array.from(this.nodes.values())
  }

  nodeCount(): number {
    return this.nodes.size
  }

  edgeCount(): number {
    return this.edges.length
  }
}
