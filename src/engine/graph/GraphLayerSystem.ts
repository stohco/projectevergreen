/**
 * GraphLayerSystem — the 30-layer graph projection architecture.
 *
 * Per GRAPH_LAYERS.md: one canon kernel, many graph projections.
 * Each layer is responsible for one kind of truth.
 *
 * All layers share:
 *   - One node identity system (canon_id, runtime_id, graph_node_id)
 *   - One edge system (typed edges: contains, adjacent_to, owned_by, etc.)
 *   - One query bus (findByType, neighbors, path, subgraph, etc.)
 *
 * The GraphLayer interface enforces the same shape for every layer:
 *   build, query, write, invalidate, explain
 *
 * Separation rules:
 *   - No layer owns canon facts directly
 *   - No render layer mutates simulation truth
 *   - No AI layer knows rendering implementation
 *   - No geometry layer knows lore
 */

import { WorldGraph, type GraphNode, type GraphEdge, type NodeType, type EdgeType } from './WorldGraph'

// ---- Graph Layer Interface (per GRAPH_LAYERS.md) ------------------------

export interface GraphLayer {
  /** Layer name (e.g. 'spatial', 'actor', 'memory'). */
  name: string
  /** Layer index (0-29 per the 30-layer stack). */
  index: number
  /** Build this layer's projections from the canon graph + runtime state. */
  build(input: LayerBuildInput): void
  /** Query this layer. Returns layer-specific results. */
  query(q: LayerQuery): LayerQueryResult
  /** Apply a delta to this layer. */
  write(delta: LayerDelta): void
  /** Invalidate cached data for a region or node. */
  invalidate(target: InvalidateTarget): void
  /** Explain why a node is in its current state (for debugging). */
  explain(nodeId: string): LayerExplanation
}

export interface LayerBuildInput {
  /** The canonical world graph (Layer 2). */
  canonGraph: WorldGraph
  /** Runtime state (actor positions, world events, etc.). */
  runtime: RuntimeState
}

export interface RuntimeState {
  playerPosition: [number, number, number]
  playerRealm: string
  playerQi: number
  time: number // game time in seconds
  weather: string
  loadedRegions: string[]
}

export interface LayerQuery {
  type: 'findByType' | 'neighbors' | 'path' | 'subgraph' | 'influenceRadius' | 'impactChain' | 'loadedRegion' | 'actorView'
  nodeType?: NodeType
  nodeId?: string
  targetId?: string
  radius?: number
  maxDepth?: number
  filters?: Record<string, unknown>
}

export interface LayerQueryResult {
  nodes: GraphNode[]
  edges: GraphEdge[]
  metadata?: Record<string, unknown>
}

export interface LayerDelta {
  type: 'addNode' | 'removeNode' | 'addEdge' | 'removeEdge' | 'updateNode' | 'updateEdge'
  nodeId?: string
  edgeId?: string
  data?: Record<string, unknown>
  provenance: 'CANON' | 'SIMULATION' | 'PLAYER'
}

export interface InvalidateTarget {
  type: 'node' | 'region' | 'all'
  nodeId?: string
  regionId?: string
}

export interface LayerExplanation {
  nodeId: string
  layer: string
  state: string
  provenance: string
  history?: string[]
  dependencies?: string[]
}

// ---- Layer Registry -----------------------------------------------------

export class GraphLayerRegistry {
  private readonly layers: Map<string, GraphLayer> = new Map()
  private readonly graph: WorldGraph

  constructor(graph: WorldGraph) {
    this.graph = graph
  }

  register(layer: GraphLayer): void {
    if (this.layers.has(layer.name)) {
      throw new Error(`Layer already registered: ${layer.name}`)
    }
    this.layers.set(layer.name, layer)
    console.log(`[GraphLayers] registered layer ${layer.index}: ${layer.name}`)
  }

  get(name: string): GraphLayer | undefined {
    return this.layers.get(name)
  }

  all(): GraphLayer[] {
    return Array.from(this.layers.values()).sort((a, b) => a.index - b.index)
  }

  /** Build all layers from canon graph + runtime state. */
  buildAll(runtime: RuntimeState): void {
    const input: LayerBuildInput = { canonGraph: this.graph, runtime }
    for (const layer of this.all()) {
      layer.build(input)
    }
  }

  /** Query across all layers. */
  queryAll(q: LayerQuery): Map<string, LayerQueryResult> {
    const results = new Map<string, LayerQueryResult>()
    for (const layer of this.all()) {
      results.set(layer.name, layer.query(q))
    }
    return results
  }

  /** Write a delta to the appropriate layer. */
  writeDelta(delta: LayerDelta, layerName: string): void {
    const layer = this.layers.get(layerName)
    if (!layer) {
      console.warn(`[GraphLayers] unknown layer: ${layerName}`)
      return
    }
    layer.write(delta)
  }

  /** Invalidate a target across all layers. */
  invalidateAll(target: InvalidateTarget): void {
    for (const layer of this.all()) {
      layer.invalidate(target)
    }
  }

  count(): number {
    return this.layers.size
  }
}

// ---- Concrete Layer Implementations (stubs to be filled) ----------------

/**
 * Layer 4 — Spatial Graph.
 * Containment + adjacency + bounds. Answers: what contains what,
 * what is adjacent to what, what regions are loaded.
 */
export class SpatialGraphLayer implements GraphLayer {
  name = 'spatial'
  index = 4
  private containment: Map<string, string[]> = new Map() // parent → children
  private adjacency: Map<string, string[]> = new Map() // node → neighbors

  build(input: LayerBuildInput): void {
    // Build containment from PARENT_LOCATION edges.
    for (const edge of input.canonGraph.allEdges()) {
      if (edge.type === 'PARENT_LOCATION') {
        const children = this.containment.get(edge.to) ?? []
        children.push(edge.from)
        this.containment.set(edge.to, children)
      }
      if (edge.type === 'NEAR' || edge.type === 'FAMILIAR_WITH') {
        const a = this.adjacency.get(edge.from) ?? []
        a.push(edge.to)
        this.adjacency.set(edge.from, a)
        const b = this.adjacency.get(edge.to) ?? []
        b.push(edge.from)
        this.adjacency.set(edge.to, b)
      }
    }
  }

  query(q: LayerQuery): LayerQueryResult {
    if (q.type === 'neighbors' && q.nodeId) {
      const neighbors = this.adjacency.get(q.nodeId) ?? []
      return { nodes: [], edges: [], metadata: { neighbors } }
    }
    if (q.type === 'findByType' && q.nodeType === 'location') {
      // Return all location nodes.
      return { nodes: [], edges: [] }
    }
    return { nodes: [], edges: [] }
  }

  write(delta: LayerDelta): void {
    if (delta.type === 'addEdge' && delta.data?.edgeType === 'NEAR') {
      // Add adjacency.
    }
  }

  invalidate(_target: InvalidateTarget): void {
    // Mark cached spatial queries as dirty.
  }

  explain(nodeId: string): LayerExplanation {
    return {
      nodeId,
      layer: this.name,
      state: `contained by: ${this.findContainer(nodeId)}`,
      provenance: 'CANON',
      dependencies: this.adjacency.get(nodeId) ?? [],
    }
  }

  private findContainer(nodeId: string): string {
    for (const [parent, children] of this.containment) {
      if (children.includes(nodeId)) return parent
    }
    return 'none'
  }
}

/**
 * Layer 9 — Actor Graph.
 * NPCs, beasts, player. Persistent identity, body state, current position.
 */
export class ActorGraphLayer implements GraphLayer {
  name = 'actor'
  index = 9
  private actors: Map<string, ActorState> = new Map()

  build(input: LayerBuildInput): void {
    // Bootstrap actors from canon graph NPC nodes.
    for (const node of input.canonGraph.nodesByType('npc')) {
      this.actors.set(node.id, {
        id: node.id,
        name: node.displayName,
        realm: (node.realm as string) ?? 'mortal',
        position: (node.meta?.position as [number, number, number]) ?? [0, 0, 0],
        isAlive: true,
        currentActivity: 'idle',
      })
    }
    // Add the player.
    this.actors.set('player', {
      id: 'player',
      name: 'Lu Feizhen',
      realm: 'mortal',
      position: input.runtime.playerPosition,
      isAlive: true,
      currentActivity: 'idle',
    })
  }

  query(q: LayerQuery): LayerQueryResult {
    if (q.type === 'findByType') {
      return {
        nodes: Array.from(this.actors.values()).map((a) => ({
          id: a.id,
          type: 'npc' as NodeType,
          displayName: a.name,
          objectId: a.id,
          canonStatus: 'canon' as const,
          tags: [a.realm],
          meta: { position: a.position, realm: a.realm, activity: a.currentActivity },
        })),
        edges: [],
      }
    }
    if (q.type === 'actorView' && q.nodeId) {
      const actor = this.actors.get(q.nodeId)
      if (!actor) return { nodes: [], edges: [] }
      return {
        nodes: [],
        edges: [],
        metadata: { actor },
      }
    }
    return { nodes: [], edges: [] }
  }

  write(delta: LayerDelta): void {
    if (delta.type === 'updateNode' && delta.nodeId) {
      const actor = this.actors.get(delta.nodeId)
      if (actor && delta.data) {
        if (delta.data.position) actor.position = delta.data.position as [number, number, number]
        if (delta.data.realm) actor.realm = delta.data.realm as string
        if (delta.data.activity) actor.currentActivity = delta.data.activity as string
      }
    }
  }

  invalidate(target: InvalidateTarget): void {
    if (target.type === 'node' && target.nodeId) {
      // Force re-query of this actor's state.
    }
  }

  explain(nodeId: string): LayerExplanation {
    const actor = this.actors.get(nodeId)
    return {
      nodeId,
      layer: this.name,
      state: actor ? `${actor.name} (${actor.realm}) at ${actor.position}, ${actor.currentActivity}` : 'unknown',
      provenance: 'CANON',
    }
  }
}

export interface ActorState {
  id: string
  name: string
  realm: string
  position: [number, number, number]
  isAlive: boolean
  currentActivity: string
}

/**
 * Layer 15 — Relationship Graph.
 * Trust, fear, debt, respect, grievance, mentorship, affection.
 */
export class RelationshipGraphLayer implements GraphLayer {
  name = 'relationship'
  index = 15
  private relationships: Map<string, Array<{ target: string; type: string; weight: number }>> = new Map()

  build(input: LayerBuildInput): void {
    // Build from canon graph social edges.
    for (const edge of input.canonGraph.allEdges()) {
      if (['FAMILIAR_WITH', 'FAMILY', 'MASTER_OF', 'DISCIPLE_OF', 'HOSTILE_TO', 'ALLIED_WITH', 'KARMIC_DEBT', 'GRUDGE'].includes(edge.type)) {
        const rels = this.relationships.get(edge.from) ?? []
        rels.push({ target: edge.to, type: edge.type, weight: edge.weight })
        this.relationships.set(edge.from, rels)
      }
    }
  }

  query(q: LayerQuery): LayerQueryResult {
    if (q.type === 'neighbors' && q.nodeId) {
      const rels = this.relationships.get(q.nodeId) ?? []
      return { nodes: [], edges: [], metadata: { relationships: rels } }
    }
    return { nodes: [], edges: [] }
  }

  write(delta: LayerDelta): void {
    if (delta.type === 'addEdge' && delta.data) {
      const from = delta.data.from as string
      const rels = this.relationships.get(from) ?? []
      rels.push({
        target: delta.data.to as string,
        type: delta.data.edgeType as string,
        weight: delta.data.weight as number ?? 1,
      })
      this.relationships.set(from, rels)
    }
  }

  invalidate(_target: InvalidateTarget): void {}

  explain(nodeId: string): LayerExplanation {
    const rels = this.relationships.get(nodeId) ?? []
    return {
      nodeId,
      layer: this.name,
      state: `${rels.length} relationships`,
      provenance: 'CANON',
      dependencies: rels.map((r) => `${r.type}:${r.target}`),
    }
  }
}

/**
 * Layer 16 — Memory Graph.
 * Event memory, witness memory, distortion, decay, retelling.
 * Per PRD §16: NPCs remember events. Per OptMem: append-only log + recall.
 */
export class MemoryGraphLayer implements GraphLayer {
  name = 'memory'
  index = 16
  /** actorId → array of memories (append-only, like OptMem LOG.txt). */
  private memories: Map<string, MemoryEntry[]> = new Map()

  build(_input: LayerBuildInput): void {
    // Memories start empty — they accumulate during simulation.
  }

  query(q: LayerQuery): LayerQueryResult {
    if (q.type === 'actorView' && q.nodeId) {
      const mems = this.memories.get(q.nodeId) ?? []
      return { nodes: [], edges: [], metadata: { memories: mems.slice(-10) } } // last 10
    }
    return { nodes: [], edges: [] }
  }

  write(delta: LayerDelta): void {
    if (delta.type === 'addNode' && delta.data) {
      const actorId = delta.data.actorId as string
      const mems = this.memories.get(actorId) ?? []
      mems.push({
        id: delta.data.memoryId as string,
        event: delta.data.event as string,
        timestamp: delta.data.timestamp as number,
        importance: delta.data.importance as number ?? 0.5,
        distortion: 0, // starts accurate, decays over time
      })
      this.memories.set(actorId, mems)
    }
  }

  invalidate(_target: InvalidateTarget): void {}

  explain(nodeId: string): LayerExplanation {
    const mems = this.memories.get(nodeId) ?? []
    return {
      nodeId,
      layer: this.name,
      state: `${mems.length} memories`,
      provenance: 'SIMULATION',
      history: mems.slice(-5).map((m) => `${m.event} (imp=${m.importance})`),
    }
  }

  /** Add a memory to an actor (called by simulation events). */
  remember(actorId: string, event: string, importance: number = 0.5): void {
    const mems = this.memories.get(actorId) ?? []
    mems.push({
      id: `mem:${actorId}:${mems.length}`,
      event,
      timestamp: Date.now(),
      importance,
      distortion: 0,
    })
    this.memories.set(actorId, mems)
  }

  /** Recall memories matching a query (regex search, like OptMem recall). */
  recall(actorId: string, query: string): MemoryEntry[] {
    const mems = this.memories.get(actorId) ?? []
    const lower = query.toLowerCase()
    return mems.filter((m) => m.event.toLowerCase().includes(lower))
  }
}

export interface MemoryEntry {
  id: string
  event: string
  timestamp: number
  importance: number
  distortion: number // 0 = accurate, 1 = completely distorted
}
