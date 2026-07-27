# Er Gen Verse — Mathematical Implementation Plan

## The Core Equation

```
W(t) = B ⊕ S(t) ⊕ P(t)
```

Where:
- `B` = immutable blueprint (canon world — never modified)
- `S(t)` = simulation delta (weather, beasts, sect growth, erosion)
- `P(t)` = player delta (mining, building, destroying, placing)

The renderer does NOT own `W`. It samples it:

```
state(x, t) = sample(W(t), x)
```

Every visual system is a pure function of semantic state:
```
terrain   = T(blueprint, simDelta, playerDelta)
building  = H(semanticBuilding, theme, state)
character = C(actorState, outfitState, cultivationState, injuryState)
vfx       = V(eventState, qiField, camera)
```

## 1. Terrain — Continuous Fields + SDF

### Height Field (RBF)

```
h(x, z) = Σᵢ aᵢ · φ(‖(x,z) − cᵢ‖) + r(x,z)
```

Where:
- `φ` = Wendland C² RBF: `φ(r, R) = (1−r/R)⁴ · (4r/R + 1)` for `r < R`, else `0`
- `cᵢ` = canon control points (from PLANET_SUZAKU_PLACEMENT)
- `aᵢ` = amplitude per biome (mountains=40, snow=50, sea=−20, plains=0)
- `r(x,z)` = deterministic micro-detail noise (simplex, seed=CANON_SEED)

**Implementation**: `src/engine/world/field/RBFTerrain.ts` (shipped CRON-THREEJS-4)

### Solid Field (SDF)

```
terrainSolid(p) = min(
  h(p.x, p.z) − p.y,      // height field
  riverSDF(p),             // river carve
  cliffSDF(p),             // cliff features
  settlementCutoutSDF(p)   // village flattening
)
```

**Status**: height field + cliff SDF + river SDF shipped. Settlement cutout SDF pending.

### Rivers as Flow Lines

```
F(x) = −∇h(x) + ε(x)
dx/ds = normalize(F(x))
```

River path = integral curve of the descent field, constrained by:
- minimum/maximum slope
- basin connectivity
- settlement access
- spirit vein adjacency
- canon landmarks

**Status**: pending. Current river is a simple line segment.

### Erosion (post-process)

```
E = λ₁·smoothness + λ₂·drainage_error + λ₃·canon_constraint_error + λ₄·road_violation
```

- thermal erosion (talus slopes)
- hydraulic erosion (gullies, valleys)
- river incision (drainage paths)
- cliff smoothing (curvature thresholds)

**Status**: pending (Milestone 4+).

## 2. Buildings — Constraint Solving

### Layout Optimization

```
minimize:
  E_layout = α·overlap_penalty
           + β·circulation_cost
           + γ·sightline_error
           + δ·canon_shape_error
           + ε·path_to_entrance_cost

subject to:
  room adjacency constraints
  room function constraints
  owner constraints
  footprint bounds
  theme constraints
  terrain constraints
```

**Solver**: simulated annealing (not MILP — too heavy for browser).

**Status**: pending. Current village is hand-authored (WangFamilyVillage.ts).

### Rooms as Semantic Volumes

```typescript
Room {
  function: "bedroom" | "alchemy_lab" | "storage" | "courtyard" | ...
  ownerId?: string
  anchors: Anchor[]
  bounds: AABB
}
```

Materialization: expand walls → place doors → anchor furniture → place windows.

**Status**: shipped (CanonTypes.ts + SettlementCompiler.ts).

## 3. Characters — Parametric Manifolds

### Shape Manifold

```
V(α) = V̄ + Σₖ αₖ · Bₖ
```

Where:
- `V̄` = neutral base mesh
- `Bₖ` = blendshape / PCA basis vectors
- `αₖ` = semantic parameters (age, sex, cultivation stage, body refinement, injury, fatigue, role, clothing bulk)

**Status**: pending. Current model is Three.js primitives, no blendshapes.

### Dual Quaternion Skinning

```
v' = normalize(Σᵢ wᵢ · qᵢ) · v
```

Preserves volume at joints (no elbow collapse).

**Status**: pending. Current uses linear blend skinning.

### Cloth (XPBD)

Position-based dynamics for sleeves, robe hems, sashes, hair.

Driven by: body acceleration, wind field, qi pressure, pose intent.

**Status**: pending. Current uses vertex-shader wind trick.

### Body Language Controller

```
a(t) = f(performance(t), context(t), target(t))
```

Semantic channels: attention, urgency, confidence, concealment, tension, patience, fatigue.

Derived animation targets: head yaw/pitch, torso lean, breathing amplitude, hand readiness, weight shift, stance width, eye saccade frequency.

**Status**: pending. Current has 7 hardcoded animations (idle/walk/run/jump/cast/sword_qi/fly).

## 4. Materials — PBR + Semantic Parameterization

### Material Function

```
material(x) = M(theme(x), wear(x), humidity(x), qiDensity(x), age(x))
```

- sect hall: polished wood, low roughness, gold trim, emissive accents
- mortal village: rougher wood, dirty walls, high wear near doors

### Triplanar Mapping (terrain)

```
blend = weights(normal)
final = blend.top · M_top + blend.side · M_side + blend.bottom · M_bottom
```

### Wear Maps

- curvature-based edge wear
- AO dirt accumulation
- foot-traffic wear masks
- rainfall streak masks
- spirit corrosion masks

**Status**: pending. Current uses flat MeshStandardMaterial per slot.

## 5. VFX — Qi as Vector Field

### Qi Field

```
q(x, t) = scalar concentration
u(x, t) = flow vector field
```

- turbulence = curl noise
- attraction to formations = potential wells

### Formations as Boundary Conditions

```
F(x) = Σᵢ wᵢ · ψᵢ(‖x − cᵢ‖)
```

- inside radius: amplify qi
- on boundary: phase shift / refraction
- outside: dampening gradient

### Particles with Semantic Direction

Particles follow: qi flow, wind, camera facing, event importance.

**Status**: pending. Current has ParticleSystem.ts but not field-driven.

## 6. Graph Engineering — 5 Projections

### Graph Types

1. **Canon graph** — identity, lore, bloodlines, relationships
2. **Spatial graph** — containment, adjacency, roads, rivers, settlements
3. **Navigation graph** — walkable nodes, portals, climbable transitions
4. **Influence graph** — reputation, fear, debt, rumor, sect power
5. **Event graph** — caused-by, witnessed-by, remembered-by, propagated-to

### Graph Algorithms

- A* for navigation
- Dijkstra for travel cost
- BFS for discovery radius
- PageRank / eigenvector centrality for social importance
- community detection for faction clustering
- MST for road/utility backbones
- SCC for reachability

**Status**: canon graph shipped (632 nodes, 424 edges). Other 4 projections pending.

## 7. Simulation-to-Visual Coherence Invariants

- **Invariant A**: every visible object has semantic ancestry
- **Invariant B**: every semantic object has at least one visual projection
- **Invariant C**: every visual change traces to canon/sim/player
- **Invariant D**: the compiler is deterministic
- **Invariant E**: direct block/mesh edits are not authoritative — all edits become deltas

## 8. Performance Budget (60 fps)

### Chunking
- near field: fine detail, full simulation
- mid field: coarse geometry, reduced animation
- far field: impostors / LOD meshes
- very far: semantic-only updates

### Mesh Generation
- greedy meshing for blocky surfaces
- dual contouring / surface nets for smooth terrain
- instancing for repeated props
- meshlets / region batches for culling

### Update Strategy
- only rebuild dirty regions
- worker-thread compilation

## 9. Implementation Order (per spec §12)

1. ✅ Canon graph + semantic world object model
2. ✅ Template library for buildings/rooms/furniture
3. ⬜ Voxel instruction IR
4. ⬜ Material resolver (PBR + semantic parameterization)
5. ✅ Terrain SDF + spline-based world shapes (RBF shipped)
6. ⬜ Mesh compiler + chunk streaming
7. ⬜ Entity materialization + body-language layer
8. ⬜ Qi/VFX field system
9. ⬜ Graph-driven AI queries (GraphQueryService shipped, not wired)
10. ⬜ Automated blind visual QA harness

## 10. Current Status (CRON-THREEJS-4)

- **Shipped**: RBF terrain (canon control points), field-driven WorldState,
  semantic world model, template library, settlement compiler, Wang Family
  Village (5 buildings), player≠Wang Lin, graph bootstrap (632 nodes).
- **VLM critic**: 4/10 (up from 1/10).
- **Next**: wire WorldState.sample() into the renderer so terrain/buildings
  are VIEWS over the field, not direct constructions.
