/**
 * CanonGraphLoader — bootstraps the WorldGraph from /ri_canon_database.json.
 *
 * Fetches the 630-entry canon database (160 chars + 80 locations + 178
 * artifacts + 214 techniques) at engine boot and populates the graph with
 * nodes + edges. This is the graph-first data source that powers all
 * GraphQueryService queries at O(1) NodeId lookup.
 *
 * Canon fidelity: all 630 entries come from the verified JSON snapshot.
 * CRON-69 corrections are baked into the JSON. NO invented chapter citations.
 * Mod-original placements (positions on the Planet Suzaku map) are flagged
 * in node.meta.canonStatus.
 */
import { WorldGraph, type GraphNode, type GraphEdge, type NodeType, type EdgeType, type CanonStatus } from './WorldGraph'
import type { CanonCharacter, CanonLocation, CanonArtifact, CanonTechnique } from '../canon/types'

interface CanonDbShape {
  characters: CanonCharacter[]
  locations: CanonLocation[]
  artifacts: CanonArtifact[]
  techniques: CanonTechnique[]
}

let _loaded = false
let _nodeCount = 0
let _edgeCount = 0

/**
 * Fetch /ri_canon_database.json and populate the graph.
 * Returns the number of nodes added.
 */
export async function bootstrapGraphFromCanon(graph: WorldGraph): Promise<number> {
  if (_loaded) return _nodeCount
  try {
    const res = await fetch('/ri_canon_database.json')
    if (!res.ok) throw new Error(`Canon fetch failed: ${res.status}`)
    const db = (await res.json()) as CanonDbShape

    // 1. Location nodes.
    for (const loc of db.locations ?? []) {
      const node: GraphNode = {
        id: `L:${loc.id}`,
        type: 'location' as NodeType,
        displayName: loc.name,
        displayNameCn: loc.nameCn,
        objectId: loc.id,
        canonStatus: (loc.canonConfidence >= 4 ? 'canon' : 'unverified') as CanonStatus,
        tags: [loc.type, loc.parent].filter(Boolean) as string[],
        meta: {
          type: loc.type,
          parent: loc.parent,
          description: loc.description,
          confidence: loc.canonConfidence,
        },
      }
      graph.addNode(node)
    }

    // 1b. PARENT_LOCATION edges.
    for (const loc of db.locations ?? []) {
      if (!loc.parent) continue
      const parentNode = graph.resolveByName(loc.parent)
      if (parentNode && parentNode.type === 'location') {
        graph.addEdge({
          id: `E:PL:${loc.id}:${parentNode.id}`,
          from: `L:${loc.id}`,
          to: parentNode.id,
          type: 'PARENT_LOCATION' as EdgeType,
          weight: 1,
          provenance: 'CANON' as const,
        })
      }
    }

    // 2. Character (NPC) nodes.
    for (const ch of db.characters ?? []) {
      const node: GraphNode = {
        id: `N:${ch.id}`,
        type: 'npc' as NodeType,
        displayName: ch.name,
        displayNameCn: ch.nameCn,
        objectId: ch.id,
        realm: ch.peakRealm as GraphNode['realm'],
        canonStatus: (ch.canonConfidence >= 4 ? 'canon' : 'unverified') as CanonStatus,
        firstAppearanceChapter: ch.firstAppearance ? parseInt(ch.firstAppearance, 10) || undefined : undefined,
        tags: [ch.type, ch.status].filter(Boolean) as string[],
        meta: {
          type: ch.type,
          peakRealm: ch.peakRealm,
          affiliation: ch.affiliation,
          status: ch.status,
          location: ch.location,
          confidence: ch.canonConfidence,
        },
      }
      graph.addNode(node)
    }

    // 2b. LOCATED_IN edges (NPC → location).
    for (const ch of db.characters ?? []) {
      if (!ch.location) continue
      const firstLoc = ch.location.split('/')[0].trim()
      const locNode = graph.resolveByName(firstLoc)
      if (locNode && locNode.type === 'location') {
        graph.addEdge({
          id: `E:LI:${ch.id}:${locNode.id}`,
          from: `N:${ch.id}`,
          to: locNode.id,
          type: 'LOCATED_IN' as EdgeType,
          weight: 1,
          provenance: 'CANON' as const,
        })
      }
    }

    // 2c. NPC relationship edges.
    for (const ch of db.characters ?? []) {
      if (!ch.relationships) continue
      for (const r of ch.relationships) {
        const edgeType = relationToEdgeType(r.relation)
        if (!edgeType) continue
        const target = graph.resolveByName(r.target)
        if (target && target.type === 'npc') {
          graph.addEdge({
            id: `E:RL:${ch.id}:${target.id}:${edgeType}`,
            from: `N:${ch.id}`,
            to: target.id,
            type: edgeType,
            weight: 1,
            provenance: 'CANON' as const,
          })
        }
      }
    }

    // 3. Artifact nodes + OWNS edges.
    for (const a of db.artifacts ?? []) {
      graph.addNode({
        id: `I:${a.id}`,
        type: 'item' as NodeType,
        displayName: a.name,
        displayNameCn: a.nameCn,
        objectId: a.id,
        canonStatus: (a.canonConfidence >= 4 ? 'canon' : 'unverified') as CanonStatus,
        tags: [a.type].filter(Boolean) as string[],
        meta: {
          type: a.type,
          owner: a.owner,
          description: a.description,
          confidence: a.canonConfidence,
        },
      })
      if (a.owner) {
        const owner = graph.resolveByName(a.owner)
        if (owner && owner.type === 'npc') {
          graph.addEdge({
            id: `E:OW:${owner.id}:${a.id}`,
            from: owner.id,
            to: `I:${a.id}`,
            type: 'OWNS' as EdgeType,
            weight: 1,
            provenance: 'CANON' as const,
          })
        }
      }
    }

    // 4. Technique nodes + KNOWS edges.
    for (const t of db.techniques ?? []) {
      graph.addNode({
        id: `T:${t.id}`,
        type: 'technique' as NodeType,
        displayName: t.name,
        displayNameCn: t.nameCn,
        objectId: t.id,
        canonStatus: (t.canonConfidence >= 4 ? 'canon' : 'unverified') as CanonStatus,
        tags: [t.type].filter(Boolean) as string[],
        meta: {
          type: t.type,
          practitioner: t.practitioner,
          description: t.description,
          confidence: t.canonConfidence,
        },
      })
      if (t.practitioner) {
        const p = graph.resolveByName(t.practitioner)
        if (p && p.type === 'npc') {
          graph.addEdge({
            id: `E:KN:${p.id}:${t.id}`,
            from: p.id,
            to: `T:${t.id}`,
            type: 'KNOWS' as EdgeType,
            weight: 1,
            provenance: 'CANON' as const,
          })
        }
      }
    }

    _loaded = true
    _nodeCount = graph.allNodes().length
    _edgeCount = graph.allEdges().length
    console.log(`[CanonGraphLoader] bootstrapped: ${_nodeCount} nodes, ${_edgeCount} edges`)
    return _nodeCount
  } catch (e) {
    console.error('[CanonGraphLoader] bootstrap failed', e)
    return 0
  }
}

function relationToEdgeType(relation: string): EdgeType | null {
  switch (relation) {
    case 'love_interest':
    case 'friend':
    case 'acquaintance':
    case 'rival':
      return 'FAMILIAR_WITH'
    case 'family':
      return 'FAMILY'
    case 'master':
      return 'MASTER_OF'
    case 'disciple':
      return 'DISCIPLE_OF'
    case 'enemy':
      return 'HOSTILE_TO'
    case 'debtor':
    case 'creditor':
      return 'KARMIC_DEBT'
    default:
      return 'FAMILIAR_WITH'
  }
}

export function getGraphStats() {
  return { nodes: _nodeCount, edges: _edgeCount, loaded: _loaded }
}
