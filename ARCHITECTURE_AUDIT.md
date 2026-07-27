# Architecture Audit — Existing Systems vs 30-Layer Graph Spec

## Methodology
For each of the 30 layers, I compare what EXISTS in the codebase against
what the GRAPH_LAYERS.md spec demands, identify gaps, overlaps, and
architectural violations, and give constructive criticism.

---

## Layer 0 — Canon source (raw .json / .md)
**Existing:** 42 MD files + 22 JSON files + 18 TS reference files.
**Verdict:** ✅ STRONG. We have 632 canon entries, 32 herbs, 21 beasts,
9 spirit veins, 178 artifacts, 214 techniques. The canon data is the
richest part of the project.
**Criticism:** The JSON files are NOT loaded by the runtime except
ri_canon_database.json (via CanonGraphLoader). The other 21 JSON files
(herbs, beasts, spirit veins, factions, techniques) are NOT ingested
at runtime. CanonSpawner hardcodes herb/beast data instead of reading
from JSON. This violates the spec: "The runtime should not know lore
by hardcoding it."

## Layer 1 — Canon kernel (immutable entities)
**Existing:** WorldGraph.ts has 632 nodes + 424 edges loaded at runtime.
CanonGraphLoader.ts bootstraps from ri_canon_database.json.
**Verdict:** ✅ PARTIAL. The graph loads, but it's a flat graph — no
distinction between "canon kernel" (immutable) and "runtime graph"
(mutable). The WorldGraph class doesn't enforce immutability of canon
nodes. A simulation delta could accidentally overwrite a canon node.
**Fix:** Split into CanonGraph (read-only) + RuntimeGraph (mutable).
CanonGraph is built once at boot and never written to.

## Layer 2 — Canon graph (typed nodes + edges)
**Existing:** WorldGraph.ts has NodeType (8 types) and EdgeType (20+ types).
Nodes have displayName, displayNameCn, canonStatus, tags, meta.
**Verdict:** ✅ GOOD. The graph structure is solid. BFS traversal,
outEdges/inEdges, resolveByName all work.
**Criticism:** Only 1 of the 10 required graph domains is implemented
(canon). The spec demands 10 domains: canon, spatial, navigation,
settlement, social, influence, event, memory, economy, cultivation.
Currently everything is jammed into one graph. The GraphLayerSystem.ts
I just created has the right interface but only 4 layers are stubbed
(spatial, actor, relationship, memory).

## Layer 3 — Semantic world graph
**Existing:** CanonTypes.ts (Settlement→Building→Room→Furniture→Anchor).
WangFamilyVillage.ts (30 buildings). SettlementCompiler.ts compiles
semantic→meshes. TemplateLibrary.ts (12 furniture templates, 3 themes).
**Verdict:** ✅ STRONG. The semantic model is well-structured and
follows the PRD. Settlement→Building→Room→Furniture→Anchor hierarchy
is correct.
**Criticism:** The semantic objects are NOT stored in the graph. They
exist as TypeScript objects in WangFamilyVillage.ts but are never
projected into WorldGraph as semantic nodes. The graph has canon NPC
nodes but no settlement/building/room nodes. This means graph queries
can't answer "what room is this NPC in?" or "what furniture is in
this building?"

## Layer 4 — Spatial graph (containment + adjacency)
**Existing:** SpatialGraphLayer stub in GraphLayerSystem.ts. PARENT_LOCATION
edges in the canon graph. PlanetSuzakuPlacement.ts has location positions.
**Verdict:** ⚜️ STUB. The layer interface exists but only builds from
PARENT_LOCATION edges. No runtime spatial queries (nearest settlement,
loaded region, visible area).
**Criticism:** The ChunkManager (voxels) and CanonSpawner both do their
own spatial logic independently. No shared spatial query bus.

## Layer 5 — Terrain field graph
**Existing:** RBFTerrain.ts (Wendland C² RBF heightmap). VoxelTerrain.ts
(solid terrain + skirt). WorldState.ts (field sample). QiField.ts (PDE).
**Verdict:** ✅ STRONG. The math is rigorous: h(x,z) = Σ aᵢφ(‖(x,z)−cᵢ‖) + r(x,z).
The qi field PDE is implemented. Voxel column sampling exists.
**Criticism:** WorldState.sample() exists but is NOT called by the
renderer. WorldCanvas calls rbfTerrainHeight() directly instead of
sampling the field. This violates the spec: "The renderer does NOT own
W. It samples it." The field inversion is architecturally present but
not wired.

## Layer 6 — Structure graph (buildings, walls, doors, roofs)
**Existing:** SettlementCompiler.ts builds walls/roofs/doors/pillars/floors.
CollisionTaxonomy.ts classifies meshes by name (wall, door, roof, pillar).
**Verdict:** ⚜️ PARTIAL. Structures are compiled to meshes but not to
graph nodes. There's no "structure graph" that says "this wall is part
of this building, which is part of this settlement." The collision
system knows about meshes but not about structural relationships.
**Criticism:** Mining a wall just hides the mesh. The structure graph
should track which walls are load-bearing and trigger collapse
(TerrainDeformation.ts CollapseSolver exists but isn't wired to buildings).

## Layer 7 — Room graph (ownership, function, layout, anchors)
**Existing:** CanonTypes.ts has Room with function, ownerId, anchors.
WangFamilyVillage.ts has rooms with furniture and anchors.
**Verdict:** ✅ GOOD. The room data model is correct.
**Criticism:** Rooms are not queryable at runtime. The NPCCognition
doesn't know which room Wang Lin is in. The anchor system (bed,
meditation, storage, door) exists in data but NPCs don't use anchors
for navigation. The NPCCognition wanders randomly instead of going
to the meditation mat or bed.

## Layer 8 — Furniture graph
**Existing:** TemplateLibrary.ts has 12 furniture templates.
SettlementCompiler places furniture from templates.
**Verdict:** ✅ GOOD. Template→geometry pipeline works.
**Criticism:** Furniture has no runtime state. A bed can't be "occupied."
A chest can't be "opened" or "looted." The interaction graph (Layer 25)
should connect player verbs to furniture anchors.

## Layer 9 — Actor graph (NPCs, beasts, player)
**Existing:** ActorGraphLayer stub in GraphLayerSystem.ts.
CanonActorMaterializer.ts (exists but not wired). EntityCultivator.ts.
BeastEntity.ts. PlayerEntity.ts. CanonSpawner.ts spawns beasts+herbs.
NPCCognition.ts drives Wang Lin.
**Verdict:** ⚜️ PARTIAL. Actors exist as Three.js objects but not as
graph nodes with persistent identity. CanonActorMaterializer was
ported from Java but isn't called. The actor layer should be the
single source of truth for "who exists and where are they?"
**Criticism:** CanonSpawner hardcodes beast data instead of reading
from ri_canon_beast_ecology.json. This violates the spec.

## Layer 10 — Motivation graph
**Existing:** NPCCognition.ts has motivation field (curiosity, caution,
hostility, etc.). AI goals (CultivatorFlightGoal, CultivatorSwordQiGoal,
CultivatorWanderGoal, Goal.ts) exist but are NOT wired.
**Verdict:** ⚜️ STUB. Motivation exists in NPCCognition but only for
Wang Lin. No graph structure — motivations are not edges between actors.
**Criticism:** The AI goal system (Goal.ts, EntityCultivator.ts) was
ported from Java but is completely disconnected from the Three.js
runtime. It's dead code. The NPCCognition reimplements what Goal.ts
was supposed to do, but simpler.

## Layer 11 — Reasoning graph (perception → commitment)
**Existing:** NPCCognition.ts has the full perception→interpretation→
motivation→commitment→action pipeline.
**Verdict:** ✅ GOOD for Wang Lin. But only one NPC has it.
**Criticism:** The reasoning is per-NPC, not graph-based. The spec
wants reasoning to be a graph layer (shared query bus). Currently
each NPC has its own isolated cognition loop.

## Layer 12 — Commitment graph
**Existing:** NPCCognition.ts has commitment field (observing,
approaching, retreating, patrolling, meditating).
**Verdict:** ⚜️ PARTIAL. Commitments exist but are ephemeral — they
don't persist across saves. If you save while Wang Lin is meditating,
he won't be meditating on reload.
**Criticism:** Commitments should be graph edges (actor → activity)
that persist in the delta store.

## Layer 13 — Activity graph
**Existing:** NPCCognition.ts executeCommitment() drives physical
activity (walking, standing, meditating). CanonSpawner.update() drives
beast wandering.
**Verdict:** ⚜️ PARTIAL. Activities happen but aren't queryable.
"Is anyone currently mining?" can't be answered.

## Layer 14 — Performance graph (body language)
**Existing:** NPCCognition.ts has attention, urgency, confidence,
concealment, tension, patience, fatigue channels.
**Verdict:** ✅ GOOD. All 7 body language channels from the PRD are
implemented. They drive aura visibility and animation choice.
**Criticism:** The channels don't drive fine-grained model adjustments
(head yaw, torso lean, breathing amplitude). The model just plays
canned animations (idle/walk/run/cast).

## Layer 15 — Relationship graph
**Existing:** RelationshipGraphLayer stub. WorldGraph has FAMILIAR_WITH,
FAMILY, MASTER_OF, HOSTILE_TO, ALLIED_WITH, KARMIC_DEBT, GRUDGE edges.
**Verdict:** ✅ GOOD. 424 edges include relationship types.
**Criticism:** Relationships are static (from canon). They don't
change based on player actions. If the player helps Wang Lin, the
relationship graph should add a trust edge. Currently it can't.

## Layer 16 — Memory graph
**Existing:** MemoryGraphLayer with remember() and recall() methods.
OptMem concept (append-only log).
**Verdict:** ⚜️ STUB. The memory layer exists but is NOT wired to
NPCCognition. Wang Lin doesn't actually remember anything.
**Criticism:** The OptMem port is architecturally correct but
non-functional. Need to wire MemoryGraphLayer.remember() into
NPCCognition events, and recall() into dialogue.

## Layer 17 — Event graph
**Existing:** Nothing.
**Verdict:** ❌ MISSING. No event tracking, no causal links.
**Criticism:** This is a significant gap. Events (beast attacks, NPC
deaths, cultivation breakthroughs, player mining) should be nodes
with caused_by/witnessed_by/remembered_by edges.

## Layer 18 — Opportunity graph
**Existing:** Nothing. (CanonSpawner spawns herbs but no opportunity
tracking.)
**Verdict:** ❌ MISSING.
**Criticism:** The canon data has 32 herbs + 178 artifacts. These
should be opportunities in the graph (ripening herb → gatherable,
ancient artifact → discoverable).

## Layer 19 — Ecology graph
**Existing:** CanonSpawner spawns 12 beasts + 33 herbs. ri_canon_beast_ecology.json has 21 species with food webs.
**Verdict:** ⚜️ PARTIAL. Beasts exist but don't have ecology (no food
webs, no migration, no habitat-based spawning from JSON).
**Criticism:** CanonSpawner hardcodes beast data. Should read from
ri_canon_beast_ecology.json.

## Layer 20 — Economy graph
**Existing:** Nothing.
**Verdict:** ❌ MISSING.

## Layer 21 — Cultivation graph
**Existing:** PlayerEntity has realm, qi, maxQi. TerrainDeformation has
REALM_CAPABILITIES. QiField.ts has the PDE solver.
**Verdict:** ⚜️ PARTIAL. Cultivation data exists but no progression
system. No breakthrough mechanic. No tribulation.
**Criticism:** The player starts as mortal (qi=0) but has no path to
gain qi. Need a cultivation interaction (meditate at shrine → sense qi →
gain qi pool).

## Layer 22 — Formation graph
**Existing:** DESIGN_HITBOXES_AND_FORMATIONS.md describes formation
arrays. BlockRegistry has FORMATION_JADE block. No runtime formation system.
**Verdict:** ❌ MISSING (design only).

## Layer 23 — Damage/deformation graph
**Existing:** TerrainDeformation.ts (A vs R, CollapseSolver, 11 materials,
8 realm tiers). Mining/placing wired (L-click/R-click).
**Verdict:** ✅ STRONG. The math is rigorous and the system is wired.
**Criticism:** CollapseSolver isn't wired to buildings. Mining a wall
just hides the mesh — it should trigger structural collapse.

## Layer 24 — Rendering graph
**Existing:** WorldCanvas.tsx (god component), SkySystem, PostProcessing,
OceanSystem, VoxelTerrain, SmoothTerrain, CultivatorModel, etc.
**Verdict:** ⚜️ WORKING but architecturally messy. WorldCanvas.tsx is
570+ lines and does everything: rendering, input, collision, mining,
doors, NPC cognition, HUD bridge.
**Criticism:** WorldCanvas violates the separation rule: "No render
layer may mutate simulation truth." It directly modifies player position,
door state, and mesh visibility instead of going through the runtime.
Should be split into: RenderSystem (pure rendering), InputSystem (input
→ deltas), SimulationSystem (NPC cognition, ecology, weather).

## Layer 25 — Interaction graph
**Existing:** Mining (L-click), placing (R-click), doors (E key).
**Verdict:** ⚜️ PARTIAL. 3 interaction verbs work. PRD wants 16+ verbs
(talk, ask, offer, request, teach, trade, challenge, recruit, gift,
follow, investigate, cultivate, mine, build, break, gather).

## Layer 26 — Save/delta graph
**Existing:** WorldDeltaStore (localStorage), WorldFacade (save/load).
F5/F9 wired.
**Verdict:** ✅ WORKING but incomplete. Deltas are recorded but not
replayed on load. Mining a wall saves the delta but doesn't restore
the wall's hidden state on reload.
**Criticism:** The save/load system records deltas but doesn't apply
them. Need delta replay: on load, walk all deltas and re-apply.

## Layer 27 — Debug graph
**Existing:** CanonFidelityChecker.ts. No runtime debug inspector.
**Verdict:** ⚜️ PARTIAL. Canon fidelity checks run at boot. No
interactive debug (inspect actor state, view graph, trace events).

## Layer 28 — QA graph
**Existing:** Nothing automated.
**Verdict:** ❌ MISSING. Need automated visual tests (agent-browser +
VLM critic is manual, not automated).

## Layer 29 — Space/orbital graph
**Existing:** Nothing.
**Verdict:** ❌ MISSING (Phase 6 per PRD).

---

## Summary

| Status | Count | Layers |
|--------|-------|--------|
| ✅ STRONG | 5 | 0 (canon source), 2 (canon graph), 3 (semantic), 5 (terrain), 23 (deformation) |
| ✅ GOOD | 4 | 7 (rooms), 8 (furniture), 14 (body language), 15 (relationships) |
| ⚜️ PARTIAL | 8 | 1 (canon kernel), 4 (spatial), 6 (structure), 9 (actors), 10 (motivation), 12 (commitment), 13 (activity), 19 (ecology), 21 (cultivation), 24 (rendering), 25 (interaction), 26 (save/delta), 27 (debug) |
| ❌ MISSING | 5 | 17 (events), 18 (opportunities), 20 (economy), 22 (formations), 28 (QA), 29 (space) |

## Top 5 Architectural Violations

1. **WorldCanvas.tsx is a god component** — violates "no render layer
   may mutate simulation truth." It directly modifies player position,
   door state, mesh visibility, and NPC cognition. Should be split into
   RenderSystem + InputSystem + SimulationSystem.

2. **CanonSpawner hardcodes lore** — violates "the runtime should not
   know lore by hardcoding it." It should read from ri_canon_herbs.json
   and ri_canon_beast_ecology.json at runtime.

3. **Save/load doesn't replay deltas** — WorldDeltaStore records deltas
   but on load, the deltas are not applied to the scene. Mining a wall
   saves but doesn't persist visually.

4. **WorldState.sample() is not called by the renderer** — the field
   inversion (renderer = view over field) is architecturally present
   but not wired. WorldCanvas calls rbfTerrainHeight() directly.

5. **Graph layers exist but aren't wired** — GraphLayerSystem.ts has
   the right interface and 4 stubbed layers, but nothing calls buildAll()
   or queryAll() at runtime. The layers are dead code.

## What Our System Does Better Than the Spec

1. **Collision system** — our MeshCollisionSystem with spatial filtering
   + sub-stepped continuous collision is more sophisticated than the
   spec's generic "graph layer" approach. The spec doesn't mention
   ray-based collision at all.

2. **Terrain deformation math** — our A vs R formula with 8 material
   properties, 8 realm tiers, and CollapseSolver is more detailed than
   the spec's generic "damage graph" layer.

3. **Qi field PDE** — the advection-diffusion solver is more rigorous
   than the spec's "terrain field graph" which just mentions "qi density"
   as a field property.

4. **NPC cognition** — our 7-channel body language system (attention,
   urgency, confidence, concealment, tension, patience, fatigue) is
   more concrete than the spec's "performance graph" layer.

## What the Spec Does Better Than Our System

1. **Separation of concerns** — the 30-layer spec enforces clean
   boundaries. Our WorldCanvas.tsx violates every boundary.

2. **Graph as single source of truth** — the spec demands all state
   live in graph projections. Our state is scattered across React
   state, Three.js objects, and localStorage.

3. **Query bus** — the spec demands a shared query layer. Our queries
   are ad-hoc (CanonSpawner.getNearby, GraphQueryService, etc.) with
   no unified interface.

4. **Explainability** — every layer has explain(nodeId). Our system
   has no debug inspector for "why is Wang Lin meditating?"

## Recommendation

Don't rewrite everything. Instead:
1. Split WorldCanvas.tsx into 3 systems (render/input/sim)
2. Wire GraphLayerSystem.buildAll() at boot
3. Make CanonSpawner read from JSON instead of hardcoding
4. Wire WorldState.sample() into the renderer
5. Wire delta replay on load
6. Wire MemoryGraphLayer into NPCCognition
7. Add the missing layers one at a time, starting with events (17)
   and opportunities (18)
