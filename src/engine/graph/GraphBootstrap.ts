/**
 * GraphBootstrap — port of dev.ergenverse.graph.GraphBootstrap (CRON-137)
 *
 * Fills the WorldGraph from RICanonicalDatabase. Produces nodes for every
 * canon character, location, artifact, technique, faction; produces edges
 * for relationships (LOCATED_IN, MEMBER_OF, FAMILIAR_WITH, FAMILY, etc.).
 *
 * The graph is in-memory only — it's rebuilt from canon on every world
 * load (canon edges are stable). Simulation edges (write-back) are a
 * future enhancement.
 */
import { WorldGraph } from './WorldGraph'
import type { GraphNode, GraphEdge, EdgeType } from './WorldGraph'
import type {
  CanonCharacter,
  CanonLocation,
  CanonArtifact,
  CanonTechnique,
  RICanonicalDatabaseShape,
} from '../canon/types'

/** Faction name extraction: characters affiliation is a free-form string. */
function factionFromAffiliation(aff: string): string | null {
  if (!aff) return null
  // Common pattern: "Independent (multi-sect legacy: Heng Yue Sect → ...)"
  const m = aff.match(/(Heng Yue Sect|Luo He Sect|Xuan Dao Sect|Cloud Sky Sect|Soul Refining Sect|Heavenly Fate Sect|Da Lou Sword Sect|Vermilion Bird Divine Sect|Origin Sect|Great Soul Sect|Dark Scorpion Clan|Sealed Realm|Cave World|Wang Clan|Zhao Country|Snow Domain Country|Four Sect Alliance|Sea of Devils)/)
  return m ? m[1] : null
}

function charTypeToNodeType(charType: string): 'npc' {
  return 'npc' // Every character is an NPC node (including Wang Lin).
}

function locTypeToNodeType(_locType: string): 'location' {
  return 'location'
}

/** Map a relation string from canon to an EdgeType. */
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
    case 'disciple':
      return 'MASTER_OF'
    case 'enemy':
      return 'GRUDGE'
    case 'debtor':
    case 'creditor':
      return 'KARMIC_DEBT'
    default:
      return 'FAMILIAR_WITH' // default fallback
  }
}

export function bootstrapGraph(db: RICanonicalDatabaseShape): WorldGraph {
  const g = new WorldGraph()

  // 1. Location nodes.
  for (const loc of db.locations as CanonLocation[]) {
    const node: GraphNode = {
      id: `L:${loc.id}`,
      type: 'location',
      displayName: loc.name,
      displayNameCn: loc.nameCn,
      canonId: loc.id,
      attributes: {
        type: loc.type,
        parent: loc.parent,
        confidence: loc.canonConfidence,
        description: loc.description,
      },
    }
    g.addNode(node)
  }

  // 1b. Parent-location edges.
  for (const loc of db.locations as CanonLocation[]) {
    if (!loc.parent) continue
    const parentId = loc.parent
    // Try to resolve parent by name (the canon JSON uses names, not ids).
    const parentNode = g.resolveByName(parentId)
    if (parentNode && parentNode.type === 'location') {
      const edge: GraphEdge = {
        from: `L:${loc.id}`,
        to: parentNode.id,
        type: 'PARENT_LOCATION',
        weight: 1,
      }
      g.addEdge(edge)
    }
  }

  // 2. Character (NPC) nodes.
  for (const ch of db.characters as CanonCharacter[]) {
    const node: GraphNode = {
      id: `N:${ch.id}`,
      type: charTypeToNodeType(ch.type),
      displayName: ch.name,
      displayNameCn: ch.nameCn,
      canonId: ch.id,
      attributes: {
        type: ch.type,
        peakRealm: ch.peakRealm,
        affiliation: ch.affiliation,
        status: ch.status,
        confidence: ch.canonConfidence,
        location: ch.location,
      },
    }
    g.addNode(node)
  }

  // 2b. NPC → location (LOCATED_IN) edges. Canon string 'location' field
  // names a place; we resolve by name (may match multiple; take first).
  for (const ch of db.characters as CanonCharacter[]) {
    if (!ch.location) continue
    // Split on slash for compound locations; take the first part.
    const firstLoc = ch.location.split('/')[0].trim()
    const locNode = g.resolveByName(firstLoc)
    if (locNode && locNode.type === 'location') {
      const edge: GraphEdge = {
        from: `N:${ch.id}`,
        to: locNode.id,
        type: 'LOCATED_IN',
        weight: 1,
      }
      g.addEdge(edge)
    }
  }

  // 2c. NPC → NPC relationship edges (FAMILIAR_WITH / FAMILY / MASTER_OF / GRUDGE / KARMIC_DEBT).
  for (const ch of db.characters as CanonCharacter[]) {
    if (!ch.relationships) continue
    for (const r of ch.relationships) {
      const edgeType = relationToEdgeType(r.relation)
      if (!edgeType) continue
      const target = g.resolveByName(r.target)
      if (target && target.type === 'npc') {
        g.addEdge({
          from: `N:${ch.id}`,
          to: target.id,
          type: edgeType,
          weight: 1,
        })
      }
    }
  }

  // 3. Artifact nodes + OWNS edges (owner string → resolve NPC).
  for (const a of db.artifacts as CanonArtifact[]) {
    g.addNode({
      id: `I:${a.id}`,
      type: 'item',
      displayName: a.name,
      displayNameCn: a.nameCn,
      canonId: a.id,
      attributes: {
        type: a.type,
        owner: a.owner,
        confidence: a.canonConfidence,
        description: a.description,
      },
    })
    if (a.owner) {
      const owner = g.resolveByName(a.owner)
      if (owner && owner.type === 'npc') {
        g.addEdge({ from: owner.id, to: `I:${a.id}`, type: 'OWNS', weight: 1 })
      }
    }
  }

  // 4. Technique nodes + PRACTICES edges.
  for (const t of db.techniques as CanonTechnique[]) {
    g.addNode({
      id: `T:${t.id}`,
      type: 'technique',
      displayName: t.name,
      displayNameCn: t.nameCn,
      canonId: t.id,
      attributes: {
        type: t.type,
        practitioner: t.practitioner,
        confidence: t.canonConfidence,
        description: t.description,
      },
    })
    if (t.practitioner) {
      const p = g.resolveByName(t.practitioner)
      if (p && p.type === 'npc') {
        g.addEdge({ from: p.id, to: `T:${t.id}`, type: 'PRACTICES', weight: 1 })
      }
    }
  }

  return g
}
