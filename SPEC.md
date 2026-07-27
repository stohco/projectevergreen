# Er Gen Verse — Executable Specification

## Spec-Driven Development (github/spec-kit philosophy)
The canon data IS the spec. This document is the executable specification
that drives all implementation. Every system traces back to canon research.

## Current State (CRON-THREEJS-16)

### Implemented Systems
| System | File | Status | Math |
|--------|------|--------|------|
| Canon Database | ri_canon_database.json | ✅ 632 entries | — |
| WorldGraph | src/engine/graph/WorldGraph.ts | ✅ 632 nodes, 424 edges | BFS traversal |
| GraphQueryService | src/engine/graph/GraphQueryService.ts | ✅ 7 query methods | O(1) NodeId lookup |
| CanonGraphLoader | src/engine/graph/CanonGraphLoader.ts | ✅ runtime bootstrap | — |
| Semantic World Model | src/engine/world/semantic/CanonTypes.ts | ✅ Settlement→Building→Room→Furniture | — |
| Template Library | src/engine/world/template/TemplateLibrary.ts | ✅ 12 furniture templates, 3 themes | — |
| Settlement Compiler | src/engine/world/compiler/SettlementCompiler.ts | ✅ compiles semantic→meshes | — |
| Wang Family Village | src/engine/canon/settlements/WangFamilyVillage.ts | ✅ 25 buildings, canon-faithful | — |
| RBF Terrain | src/engine/world/field/RBFTerrain.ts | ✅ Wendland C² RBF | h(x,z) = Σ aᵢφ(‖(x,z)−cᵢ‖) + r(x,z) |
| WorldState Field | src/engine/world/field/WorldState.ts | ✅ W(t) = B ⊕ S(t) ⊕ P(t) | sample(x,z,t) |
| Smooth Terrain Mesh | src/engine/world/SmoothTerrain.ts | ✅ heightmap + vertex colors | — |
| Spirit Pines | src/engine/world/SmoothTerrain.ts | ✅ instanced icosahedron spheres | — |
| Grass Tufts | src/engine/world/SmoothTerrain.ts | ✅ NMS-style blade clusters | Poisson-disk distribution |
| Rocks + Flowers | src/engine/world/SmoothTerrain.ts | ✅ instanced | — |
| Mesh Collision | src/engine/world/CollisionSystem.ts | ✅ 8-direction ray + movement ray | ray-mesh intersection |
| Collision Taxonomy | src/engine/world/CollisionTaxonomy.ts | ✅ auto-classify by name | solid/non_solid/door/water |
| Terrain Deformation | src/engine/world/TerrainDeformation.ts | ✅ A vs R + CollapseSolver | A=tech×realm×qi×contact×intent, R=hard×thick×cohes×reinforce×form×qi |
| Cultivator Model | src/engine/entities/CultivatorModel.ts | ✅ procedural humanoid | — |
| Player Entity | src/engine/entities/PlayerEntity.ts | ✅ Lu Feizhen, mortal start | qi=0, maxQi=0 |
| Character Creation | src/components/game/CharacterCreation.tsx | ✅ name input | — |
| World Canvas | src/components/game/WorldCanvas.tsx | ✅ Three.js mount | — |
| Sky System | src/engine/render/SkySystem.ts | ✅ Rayleigh+Mie scattering | — |
| Post-FX | src/engine/render/PostProcessing.ts | ✅ bloom+vignette+colorGrade | — |
| Procedural Textures | src/engine/render/ProceduralTextures.ts | ✅ 46-tile atlas | — |
| HUD | src/components/game/hud/ | ✅ 12 components | — |
| Door Interaction | WorldCanvas + SettlementCompiler | ✅ E key toggle | — |
| Camera (Y lock, RMB orbit) | WorldCanvas | ✅ NMS-style | — |
| Ocean | WorldCanvas | ✅ vast plane | — |

### Pending Systems (Priority Order)
| System | Spec Source | Math |
|--------|------------|------|
| Qi Field PDE | DESIGN_NMS_SYNTHESIS.md | ∂q/∂t = -∇·(uq) + D∇²q + S - R |
| NPC Memory (OptMem port) | OptMem concept | append-only log + binary tree |
| Canon Herb Spawning | ri_canon_herbs.json (32 herbs) | biome-gated placement |
| Canon Beast Spawning | ri_canon_beast_ecology.json (21 species) | region-gated placement |
| Combat System | DESIGN_HITBOXES_AND_FORMATIONS.md | 9 voxel geometries + orientation |
| Cultivation Breakthrough | DESIGN_NMS_SYNTHESIS.md | P = f(qiPool, comprehension, spiritRoot, karma, tribulation) |
| Dual Cultivation | canon: Wang Lin + Li Muwan | graph edge: ALLIED_WITH + dual_cultivation |
| 5 Graph Projections | MATH_IMPLEMENTATION_PLAN.md | canon/spatial/nav/influence/event |
| Hand-crafted Planet Suzaku | CANON_RI_COMPLETE_WORLD.md | canon geography → RBF control points |
| Worker-thread Meshing | MATH_IMPLEMENTATION_PLAN.md | Transferable ArrayBuffer |
| Character Blendshapes | MATH_IMPLEMENTATION_PLAN.md | V(α) = V̄ + Σ αₖBₖ |
| XPBD Cloth | MATH_IMPLEMENTATION_PLAN.md | position-based dynamics |

## Architecture Pipeline
```
Canon (42 MD + 22 JSON + 18 TS)
  ↓
Canonical World Graph (632 nodes, 424 edges)
  ↓
Semantic World Model (Settlement→Building→Room→Furniture→Anchor)
  ↓
Template Library (12 furniture + 3 themes + material slots)
  ↓
Settlement Compiler (semantic → Three.js meshes)
  ↓
Simulation Runtime (W(t) = B ⊕ S(t) ⊕ P(t))
  ├─ CanonDelta (blueprint, never modified)
  ├─ SimulationDelta (weather, beasts, sect growth)
  └─ PlayerDelta (mining, building, destroying)
  ↓
Three.js Presentation Layer (renderer = view over field, not owner)
  ↓
Player (Lu Feizhen, mortal)
```

## Math Foundations
- **Terrain**: h(x,z) = Σᵢ aᵢ·φ(‖(x,z)−cᵢ‖) + r(x,z) — Wendland C² RBF
- **World State**: W(t) = B ⊕ S(t) ⊕ P(t) — state(x,t) = sample(W(t), x)
- **Deformation**: A = tech×realm×qi×contact×intent, R = hard×thick×cohes×reinforce×form×qi
- **Collapse**: BFS load redistribution through support graph
- **Collision**: 8-direction ray + movement ray against actual meshes
- **Qi Field** (pending): ∂q/∂t = -∇·(uq) + D∇²q + S(x,t) - R(x,t)
- **Breakthrough** (pending): P = f(qiPool, comprehension, spiritRoot, karma, tribulation)
- **NPC Memory** (pending): wake(log) + note(event) + recall(query) → MEMORY edges
