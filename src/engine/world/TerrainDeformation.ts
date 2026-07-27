/**
 * TerrainDeformation — realm-gated, material-dependent, graph-aware
 * terrain deformation system.
 *
 * Per the user's spec:
 *
 *   A = technique_power × realm_multiplier × qi_channeling × contact_quality × intent_focus
 *   R = hardness × thickness × cohesion × reinforcement × formation_factor × qi_stability
 *   damage = f(A - R)
 *
 * If A > R, the material deforms, fractures, collapses, or vaporizes
 * depending on the attack type.
 *
 * "Breakable" is not one property. Every surface/structure carries:
 *   hardness, toughness, cohesion, density, qiResistance,
 *   formationAnchoring, loadBearing, repairRate
 *
 * Realm gates scale the KIND of deformation:
 *   Mortal / Qi Condensation: dig soil, smash weak stone, break doors
 *   Foundation / Core Formation: crack boulders, collapse walls, carve caves
 *   Nascent Soul / Soul Formation: break cliff faces, redirect streams, level compounds
 *   Ascendant / Heaven Trampling: reshape mountains, sever valleys, alter coastlines
 *
 * Terrain deformation is graph-aware: every terrain region has a support
 * graph. Damage propagates through the graph — break the bottom of a cliff
 * and the top collapses.
 *
 * Deformation is delta-persisted: never mutates canon. All changes are
 * PlayerDelta or SimulationDelta, replayed on reload.
 */

// ---- Material properties -------------------------------------------------

export interface MaterialProperties {
  /** Scratch / penetration resistance (0-10). */
  hardness: number
  /** Crack propagation resistance (0-10). */
  toughness: number
  /** How well the medium hangs together (0-10). */
  cohesion: number
  /** Mass per volume (kg/m³ scaled). */
  density: number
  /** Resistance to cultivation-force effects (0-10). */
  qiResistance: number
  /** How strongly formations stabilize it (0-10). */
  formationAnchoring: number
  /** Structural support capacity (0-10). */
  loadBearing: number
  /** Natural or mystical recovery rate (0-1 per in-game hour). */
  repairRate: number
}

// Canon material library — each material has properties grounded in xianxia logic.
export const MATERIALS: Record<string, MaterialProperties> = {
  soil: {
    hardness: 1, toughness: 0.5, cohesion: 0.5, density: 1.2,
    qiResistance: 0, formationAnchoring: 0, loadBearing: 1, repairRate: 0.1,
  },
  dirt: {
    hardness: 1.5, toughness: 1, cohesion: 1, density: 1.5,
    qiResistance: 0, formationAnchoring: 0, loadBearing: 1.5, repairRate: 0.08,
  },
  loose_rock: {
    hardness: 3, toughness: 2, cohesion: 1.5, density: 2.5,
    qiResistance: 0.5, formationAnchoring: 0, loadBearing: 3, repairRate: 0.02,
  },
  granite: {
    hardness: 7, toughness: 6, cohesion: 7, density: 2.7,
    qiResistance: 1, formationAnchoring: 0, loadBearing: 8, repairRate: 0.01,
  },
  mountain_core: {
    hardness: 9, toughness: 8, cohesion: 9, density: 3.0,
    qiResistance: 2, formationAnchoring: 0, loadBearing: 9, repairRate: 0.005,
  },
  wood: {
    hardness: 2, toughness: 4, cohesion: 3, density: 0.7,
    qiResistance: 0.5, formationAnchoring: 0, loadBearing: 4, repairRate: 0.05,
  },
  jade: {
    hardness: 8, toughness: 7, cohesion: 8, density: 3.3,
    qiResistance: 5, formationAnchoring: 2, loadBearing: 7, repairRate: 0.03,
  },
  spirit_stone: {
    hardness: 6, toughness: 5, cohesion: 6, density: 2.8,
    qiResistance: 8, formationAnchoring: 3, loadBearing: 6, repairRate: 0.04,
  },
  formation_reinforced: {
    hardness: 7, toughness: 7, cohesion: 8, density: 2.7,
    qiResistance: 6, formationAnchoring: 9, loadBearing: 9, repairRate: 0.06,
  },
  bedrock: {
    hardness: 10, toughness: 10, cohesion: 10, density: 3.5,
    qiResistance: 3, formationAnchoring: 0, loadBearing: 10, repairRate: 0,
  },
  water: {
    hardness: 0, toughness: 0, cohesion: 0, density: 1.0,
    qiResistance: 0, formationAnchoring: 0, loadBearing: 0, repairRate: 1.0,
  },
}

// ---- Realm gates ---------------------------------------------------------

export type DeformationRealm =
  | 'mortal'
  | 'qi_condensation'
  | 'foundation'
  | 'core_formation'
  | 'nascent_soul'
  | 'soul_formation'
  | 'ascendant'
  | 'heaven_trampling'

export interface RealmDeformationCapability {
  /** Maximum deformation radius in meters. */
  maxRadius: number
  /** Maximum material hardness the cultivator can penetrate (0-10). */
  maxHardness: number
  /** Realm multiplier applied to attack power. */
  realmMultiplier: number
  /** What kinds of deformation are possible. */
  capabilities: DeformationCapability[]
}

export type DeformationCapability =
  | 'dig_soil'
  | 'smash_weak_stone'
  | 'break_doors'
  | 'crack_boulders'
  | 'collapse_walls'
  | 'carve_caves'
  | 'split_ridges'
  | 'break_cliff_faces'
  | 'redirect_streams'
  | 'level_compounds'
  | 'reshape_mountains'
  | 'sever_valleys'
  | 'alter_coastlines'
  | 'continent_deformation'

export const REALM_CAPABILITIES: Record<DeformationRealm, RealmDeformationCapability> = {
  mortal: {
    maxRadius: 0.5, maxHardness: 2, realmMultiplier: 0.5,
    capabilities: ['dig_soil', 'break_doors'],
  },
  qi_condensation: {
    maxRadius: 1, maxHardness: 3, realmMultiplier: 1,
    capabilities: ['dig_soil', 'smash_weak_stone', 'break_doors'],
  },
  foundation: {
    maxRadius: 3, maxHardness: 5, realmMultiplier: 3,
    capabilities: ['dig_soil', 'smash_weak_stone', 'break_doors', 'crack_boulders', 'collapse_walls'],
  },
  core_formation: {
    maxRadius: 5, maxHardness: 6, realmMultiplier: 6,
    capabilities: ['dig_soil', 'smash_weak_stone', 'break_doors', 'crack_boulders', 'collapse_walls', 'carve_caves'],
  },
  nascent_soul: {
    maxRadius: 15, maxHardness: 8, realmMultiplier: 15,
    capabilities: ['dig_soil', 'smash_weak_stone', 'break_doors', 'crack_boulders', 'collapse_walls', 'carve_caves', 'split_ridges', 'break_cliff_faces'],
  },
  soul_formation: {
    maxRadius: 30, maxHardness: 8, realmMultiplier: 30,
    capabilities: ['dig_soil', 'smash_weak_stone', 'break_doors', 'crack_boulders', 'collapse_walls', 'carve_caves', 'split_ridges', 'break_cliff_faces', 'redirect_streams', 'level_compounds'],
  },
  ascendant: {
    maxRadius: 100, maxHardness: 9, realmMultiplier: 100,
    capabilities: ['dig_soil', 'smash_weak_stone', 'break_doors', 'crack_boulders', 'collapse_walls', 'carve_caves', 'split_ridges', 'break_cliff_faces', 'redirect_streams', 'level_compounds', 'reshape_mountains'],
  },
  heaven_trampling: {
    maxRadius: 1000, maxHardness: 10, realmMultiplier: 1000,
    capabilities: ['dig_soil', 'smash_weak_stone', 'break_doors', 'crack_boulders', 'collapse_walls', 'carve_caves', 'split_ridges', 'break_cliff_faces', 'redirect_streams', 'level_compounds', 'reshape_mountains', 'sever_valleys', 'alter_coastlines', 'continent_deformation'],
  },
}

// ---- TerrainResistance interface -----------------------------------------

export interface TerrainResistance {
  /** Material at this position. */
  material: string
  /** Thickness of the material at this point (meters). */
  thickness: number
  /** Is this point inside a formation-anchored zone? */
  formationAnchored: boolean
  /** Formation anchoring strength (0-10, overrides material if higher). */
  formationFactor: number
  /** Qi stability of the region (0-10, from spirit vein proximity). */
  qiStability: number

  /** Compute total resistance R. */
  computeResistance(): number
}

export class TerrainResistanceImpl implements TerrainResistance {
  material: string
  thickness: number
  formationAnchored: boolean
  formationFactor: number
  qiStability: number

  constructor(opts: {
    material: string
    thickness: number
    formationAnchored?: boolean
    formationFactor?: number
    qiStability?: number
  }) {
    this.material = opts.material
    this.thickness = opts.thickness
    this.formationAnchored = opts.formationAnchored ?? false
    this.formationFactor = opts.formationFactor ?? 0
    this.qiStability = opts.qiStability ?? 0
  }

  /**
   * R = hardness × thickness × cohesion × reinforcement × formation_factor × qi_stability
   *
   * The resistance is the product of material properties, thickness, and
   * mystical reinforcement. A formation-anchored sect wall has higher
   * resistance than raw stone of the same hardness.
   */
  computeResistance(): number {
    const mat = MATERIALS[this.material] ?? MATERIALS.soil
    const ff = this.formationAnchored ? Math.max(this.formationFactor, mat.formationAnchoring) : mat.formationAnchoring
    return (
      mat.hardness *
      Math.max(0.1, this.thickness) *
      mat.cohesion *
      (1 + ff * 0.5) * // reinforcement
      (1 + ff * 0.3) * // formation_factor
      (1 + this.qiStability * 0.2) // qi_stability
    )
  }
}

// ---- DeformationEvent interface ------------------------------------------

export type DeformationType =
  | 'crater'      // impact crater (punch, fall, blast)
  | 'slice'       // sword cut — thin planar cut
  | 'shatter'     // explosion — radial fragmentation
  | 'carve'       // tunnel/cave — extended volume removal
  | 'vaporize'    // energy beam — material becomes dust
  | 'terraform'   // reshape — move terrain volume
  | 'freeze'      // ice technique — material frozen
  | 'ignite'      // fire technique — material burned

export interface DeformationEvent {
  /** World position of the deformation center. */
  position: [number, number, number]
  /** Direction of the attack (for slices, beams). */
  direction: [number, number, number]
  /** Type of deformation. */
  type: DeformationType
  /** Base technique power (0-100). */
  techniquePower: number
  /** Cultivator's realm. */
  realm: DeformationRealm
  /** Qi being channeled into the technique (0-1 of max qi). */
  qiChanneling: number
  /** Quality of the contact (0-1, depends on accuracy/timing). */
  contactQuality: number
  /** Intent focus (0-1, concentration/commitment). */
  intentFocus: number
  /** Radius of effect (meters, capped by realm). */
  radius: number

  /** Compute attack power A. */
  computeAttackPower(): number
}

export class DeformationEventImpl implements DeformationEvent {
  position: [number, number, number]
  direction: [number, number, number]
  type: DeformationType
  techniquePower: number
  realm: DeformationRealm
  qiChanneling: number
  contactQuality: number
  intentFocus: number
  radius: number

  constructor(opts: Partial<DeformationEvent> & {
    position: [number, number, number]
    techniquePower: number
    realm: DeformationRealm
  }) {
    this.position = opts.position
    this.direction = opts.direction ?? [0, 1, 0]
    this.type = opts.type ?? 'crater'
    this.techniquePower = opts.techniquePower
    this.realm = opts.realm
    this.qiChanneling = opts.qiChanneling ?? 0.5
    this.contactQuality = opts.contactQuality ?? 0.7
    this.intentFocus = opts.intentFocus ?? 0.8
    this.radius = opts.radius ?? 1
  }

  /**
   * A = technique_power × realm_multiplier × qi_channeling × contact_quality × intent_focus
   *
   * The attack power is the product of the technique's raw power, the
   * cultivator's realm multiplier, and three quality factors. A mortal
   * with a good technique and perfect contact can still break soil, but
   * a Nascent Soul cultivator with the same technique shatters cliffs.
   */
  computeAttackPower(): number {
    const realmCap = REALM_CAPABILITIES[this.realm]
    return (
      this.techniquePower *
      realmCap.realmMultiplier *
      Math.max(0.01, this.qiChanneling) *
      Math.max(0.01, this.contactQuality) *
      Math.max(0.01, this.intentFocus)
    )
  }
}

// ---- CollapseSolver ------------------------------------------------------

export interface SupportNode {
  id: string
  position: [number, number, number]
  material: string
  /** Current structural integrity (0-1). 1 = undamaged, 0 = collapsed. */
  integrity: number
  /** Load this node is bearing (0-10). */
  load: number
  /** Is this node a foundation (root of support)? */
  isFoundation: boolean
}

export interface SupportEdge {
  from: string
  to: string
  /** Load transfer weight (0-1). */
  weight: number
}

export interface CollapseResult {
  /** Nodes that collapsed (integrity dropped to 0). */
  collapsed: string[]
  /** Nodes that lost integrity but didn't collapse. */
  damaged: string[]
  /** Whether any collapse propagated (chain reaction). */
  propagated: boolean
}

export class CollapseSolver {
  private nodes: Map<string, SupportNode> = new Map()
  private edges: SupportEdge[] = []

  addNode(node: SupportNode): void {
    this.nodes.set(node.id, node)
  }

  addEdge(edge: SupportEdge): void {
    this.edges.push(edge)
  }

  /**
   * Apply damage to a node and propagate collapse through the support graph.
   *
   * Strategy:
   *   1. Reduce the target node's integrity by the damage amount.
   *   2. If integrity <= 0, the node collapses.
   *   3. For each collapsed node, redistribute its load to supported neighbors.
   *   4. If any neighbor's load exceeds its loadBearing capacity, it collapses too.
   *   5. Repeat until no more collapses (BFS propagation).
   *
   * This is how "break the bottom of a cliff" causes a landslide, and
   * "remove a key wall" collapses a room.
   */
  applyDamage(nodeId: string, damage: number): CollapseResult {
    const collapsed: string[] = []
    const damaged: string[] = []
    let propagated = false

    // BFS: start with the directly damaged node.
    const queue: Array<{ id: string; damage: number }> = [{ id: nodeId, damage }]

    while (queue.length > 0) {
      const { id, dmg } = queue.shift()!
      const node = this.nodes.get(id)
      if (!node) continue

      // Apply damage.
      node.integrity = Math.max(0, node.integrity - dmg)

      if (node.integrity <= 0 && !collapsed.includes(id)) {
        // Node collapsed.
        collapsed.push(id)

        // Redistribute load to supported neighbors.
        const supportingEdges = this.edges.filter((e) => e.from === id)
        for (const edge of supportingEdges) {
          const neighbor = this.nodes.get(edge.to)
          if (!neighbor || collapsed.includes(edge.to)) continue

          // Transfer load from collapsed node to neighbor.
          const transferredLoad = node.load * edge.weight
          neighbor.load += transferredLoad

          // Check if neighbor can bear the extra load.
          const mat = MATERIALS[neighbor.material] ?? MATERIALS.soil
          if (neighbor.load > mat.loadBearing * neighbor.integrity) {
            // Neighbor collapses too — chain reaction.
            const collapseDamage = (neighbor.load - mat.loadBearing * neighbor.integrity) / mat.loadBearing
            queue.push({ id: edge.to, damage: collapseDamage })
            propagated = true
          } else {
            // Neighbor is damaged but holds.
            neighbor.integrity = Math.max(0.1, neighbor.integrity - 0.2)
            if (!damaged.includes(edge.to)) damaged.push(edge.to)
          }
        }
      } else if (node.integrity > 0 && node.integrity < 1 && !damaged.includes(id)) {
        damaged.push(id)
      }
    }

    return { collapsed, damaged, propagated }
  }

  /**
   * Build a support graph from a terrain region. Each terrain cell is a node,
   * and load transfers downward (gravity) + laterally (adjacency).
   */
  buildTerrainSupportGraph(
    cells: Array<{ id: string; position: [number, number, number]; material: string }>,
  ): void {
    this.nodes.clear()
    this.edges = []

    // Add all cells as nodes.
    for (const cell of cells) {
      const mat = MATERIALS[cell.material] ?? MATERIALS.soil
      this.nodes.set(cell.id, {
        id: cell.id,
        position: cell.position,
        material: cell.material,
        integrity: 1,
        load: mat.loadBearing * 0.5, // start at 50% load
        isFoundation: cell.position[1] <= 0, // bedrock = foundation
      })
    }

    // Add edges: each cell supports the cell above it (gravity) and is
    // supported by cells to the sides (adjacency).
    for (let i = 0; i < cells.length; i++) {
      for (let j = i + 1; j < cells.length; j++) {
        const a = cells[i]
        const b = cells[j]
        const dx = a.position[0] - b.position[0]
        const dy = a.position[1] - b.position[1]
        const dz = a.position[2] - b.position[2]
        const dist = Math.sqrt(dx * dx + dy * dy + dz * dz)

        if (dist > 2) continue // only adjacent cells

        if (Math.abs(dy) > 0.5 && a.position[1] > b.position[1]) {
          // A is above B — B supports A (load transfers downward).
          this.edges.push({ from: b.id, to: a.id, weight: 0.7 })
        } else if (Math.abs(dy) > 0.5 && b.position[1] > a.position[1]) {
          // B is above A — A supports B.
          this.edges.push({ from: a.id, to: b.id, weight: 0.7 })
        } else if (Math.abs(dy) <= 0.5) {
          // Same height — lateral support.
          this.edges.push({ from: a.id, to: b.id, weight: 0.3 })
          this.edges.push({ from: b.id, to: a.id, weight: 0.3 })
        }
      }
    }
  }

  getNode(id: string): SupportNode | undefined {
    return this.nodes.get(id)
  }

  nodeCount(): number {
    return this.nodes.size
  }

  edgeCount(): number {
    return this.edges.length
  }
}

// ---- Deformation resolver ------------------------------------------------

export interface DeformationResult {
  /** Whether the deformation succeeded (A > R). */
  success: boolean
  /** Attack power A. */
  attackPower: number
  /** Resistance R. */
  resistance: number
  /** Damage dealt (A - R, clamped to 0). */
  damage: number
  /** Deformation type that occurred. */
  type: DeformationType
  /** Actual radius of effect (may be smaller than requested if A barely > R). */
  effectiveRadius: number
  /** Whether collapse propagated to neighbors. */
  propagated: boolean
  /** IDs of collapsed support nodes. */
  collapsedNodes: string[]
}

/**
 * Resolve a deformation event against a terrain resistance.
 *
 * damage = f(A - R)
 *
 * If A > R, the material deforms. The type of deformation depends on the
 * attack type and the margin of A over R:
 *   - Small margin: surface cracking
 *   - Medium margin: fracture + partial collapse
 *   - Large margin: complete vaporization/penetration
 */
export function resolveDeformation(
  event: DeformationEvent,
  resistance: TerrainResistance,
  collapseSolver?: CollapseSolver,
  targetNodeId?: string,
): DeformationResult {
  const A = event.computeAttackPower()
  const R = resistance.computeResistance()
  const realmCap = REALM_CAPABILITIES[event.realm]

  // Check realm gate: can this realm even affect this material?
  const mat = MATERIALS[resistance.material] ?? MATERIALS.soil
  if (mat.hardness > realmCap.maxHardness) {
    return {
      success: false,
      attackPower: A,
      resistance: R,
      damage: 0,
      type: event.type,
      effectiveRadius: 0,
      propagated: false,
      collapsedNodes: [],
    }
  }

  // Cap radius to realm capability.
  const effectiveRadius = Math.min(event.radius, realmCap.maxRadius)

  if (A <= R) {
    // Not enough power — no deformation.
    return {
      success: false,
      attackPower: A,
      resistance: R,
      damage: 0,
      type: event.type,
      effectiveRadius: 0,
      propagated: false,
      collapsedNodes: [],
    }
  }

  // A > R — deformation occurs.
  const damage = A - R

  // Collapse propagation through the support graph.
  let propagated = false
  let collapsedNodes: string[] = []
  if (collapseSolver && targetNodeId) {
    // Normalize damage to 0-1 integrity reduction.
    const integrityDamage = Math.min(1, damage / (R * 2))
    const result = collapseSolver.applyDamage(targetNodeId, integrityDamage)
    propagated = result.propagated
    collapsedNodes = result.collapsed
  }

  return {
    success: true,
    attackPower: A,
    resistance: R,
    damage,
    type: event.type,
    effectiveRadius,
    propagated,
    collapsedNodes,
  }
}
