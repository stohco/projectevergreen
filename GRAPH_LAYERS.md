# Graph Layer Architecture — The 30-Layer Canon Kernel

## The Principle

> When in doubt: Add a graph layer, not a new engine.

One canon kernel, many graph projections. Each layer is responsible for one kind of truth. All layers share the same node identity system, edge types, and query bus.

```
Canon JSON/MD
  → Canon Kernel
  → Typed World Graph
  → Multiple Graph Projections / Layers
  → Simulation Runtime
  → Three.js Materialization
```

## The 30-Layer Stack

| Layer | Name | Purpose |
|-------|------|---------|
| 0 | Canon source | Raw .json / .md / lore data |
| 1 | Canon kernel | Immutable canonical entities and facts |
| 2 | Canon graph | Typed nodes + edges from canon facts |
| 3 | Semantic world graph | Settlements, buildings, rooms, furniture, spirit veins, roads, rivers, mountains |
| 4 | Spatial graph | Containment + adjacency + bounds |
| 5 | Terrain field graph | Height, slope, erosion, material density, qi density |
| 6 | Structure graph | Buildings, walls, doors, roofs, interiors, anchors |
| 7 | Room graph | Room ownership, function, layout, entry points, furniture anchors |
| 8 | Furniture graph | Semantic furniture, template refs, local anchors, usage state |
| 9 | Actor graph | NPCs, beasts, player, persistent identity, body state |
| 10 | Motivation graph | Ambitions, fears, duties, grudges, curiosity, survival, cultivation pressure |
| 11 | Reasoning graph | Perception → interpretation → options → score → commitment |
| 12 | Commitment graph | Persistent intentions that survive tick-to-tick noise |
| 13 | Activity graph | What actors are physically doing right now |
| 14 | Performance graph | Attention, urgency, concealment, tension, fatigue, confidence |
| 15 | Relationship graph | Trust, fear, debt, respect, grievance, mentorship, affection |
| 16 | Memory graph | Event memory, witness memory, distortion, decay, retelling |
| 17 | Event graph | World events and causal links |
| 18 | Opportunity graph | Ripening herbs, opportunities, inheritances, disasters, ambushes |
| 19 | Ecology graph | Beasts, plants, habitats, food webs, migration |
| 20 | Economy graph | Trade, supply, demand, spirit stones, markets, scarcity |
| 21 | Cultivation graph | Realm progression, qi state, comprehension, tribulation readiness |
| 22 | Formation graph | Arrays, anchors, nodes, stability, qi routing, disruption |
| 23 | Damage/deformation graph | Terrain collapse, structure damage, repair, restoration, destruction |
| 24 | Rendering graph | Materials, meshes, LOD, visibility, scene composition |
| 25 | Interaction graph | Talk, trade, teach, request, gift, challenge, follow, mine, build |
| 26 | Save/delta graph | Canon delta, simulation delta, player delta, persistence, replay |
| 27 | Debug graph | Inspectors, queries, tracing, provenance, explainability |
| 28 | QA graph | Automated visual tests, canon checks, gameplay checks, performance checks |
| 29 | Space/orbital graph | Atmospheric ascent, void traversal, astral regions, off-world routes |

## Engineering Rules

### One source of truth
Everything writes to the canonical world graph, then gets projected into layer-specific graphs.

### One node identity system
```
canon_id
runtime_id
graph_node_id
template_id
delta_id
```
No ephemeral "just a local object" for important world entities.

### One edge system
Every layer uses typed edges: `contains`, `adjacent_to`, `owned_by`, `resides_in`, `remembers`, `caused_by`, `feeds_into`, `blocks`, `supports`, `unlocks`, `transforms_into`

### One query bus
Shared query layer — no subsystem hand-rolls traversal:
```
findByType, neighbors, path, subgraph, influenceRadius,
impactChain, loadedRegion, activeOpportunity, actorView
```

## Graph Layer Interface

Every layer follows the same shape:

```typescript
interface GraphLayer {
  name: string;
  build(input): void;           // construct from canon/runtime
  query(q): Result;             // layer-specific queries
  write(delta): void;           // apply changes
  invalidate(regionOrNode): void; // mark dirty
  explain(nodeId): Explanation;  // debug/trace
}
```

## Separation Rules

- No layer may own canon facts directly (canon lives in canon data only)
- No render layer may mutate simulation truth (request through runtime only)
- No AI layer may know rendering implementation (queries semantics, not meshes)
- No geometry layer may know lore (compiles semantic objects only)

## Player Experience Balance

```
Adventure = what the player feels
Simulation = why the world feels alive
AI = how the world responds
```

The systems should make the adventure better, richer, and more surprising — never less fun.

Strong immediate verbs: move, climb, fight, gather, build, cultivate, talk, travel, fly.

Smart AI supports the adventure:
- NPCs traveling without you
- Beasts migrating
- Settlements reacting to danger
- Rumors spreading
- Factions remembering your actions
- Cultivators choosing behavior based on personality and power

Do NOT make systems so heavy they get in the way of:
- Exploration, discovery, combat readability, movement, progression, visual wonder

## Implementation Order

1. Build the canonical graph core
2. Build graph layer interfaces
3. Build semantic world projections
4. Build spatial/terrain/structure projections
5. Build actor, memory, relationship, event, economy, cultivation projections
6. Build delta storage and replay
7. Build render/materialization projections
8. Build the debug and QA projections
9. Only then deepen content
