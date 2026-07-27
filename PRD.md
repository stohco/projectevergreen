# Product Requirements Document (PRD)

## Ergenverse — Three.js Single-Player Xianxia Open World

**Version:** v2.0
**Status:** Active Master Spec
**Target platform:** Desktop browser / Electron-style wrapper (initially browser-first, local single-player only)
**Engine:** Three.js from scratch
**Genre:** Single-player xianxia open-world simulation
**Source inspiration:** Er Gen / *Renegade Immortal* / related canon worldbuilding

---

# 1. Product definition

Ergenverse is a **strictly single-player**, canon-authored, simulation-first xianxia game built entirely from scratch in **Three.js**. The game begins from a fixed canonical world state and evolves through simulation, player action, and time. There is no Minecraft dependency, no multiplayer architecture, and no network replication layer in the gameplay path.

The player starts in an authored mortal world and can:

* explore a handcrafted canonical geography,
* interact with NPCs and settlements,
* cultivate and grow stronger,
* mine, build, destroy, and reshape terrain,
* influence economies, factions, and histories,
* and eventually travel off-world into space/void/astral regions.

The simulation is the source of truth. Three.js is the presentation layer and scene runtime.

---

# 2. Product vision

## 2.1 Vision statement

Create a visually beautiful, mechanically deep, canon-faithful xianxia world where the player starts in the edge of canon and lives forward into a dynamic simulation that can eventually carry them from mortal life to planetary ascent and space traversal.

## 2.2 Player fantasy

The player should feel like:

* they entered a world that already existed before them,
* people, beasts, sects, roads, ruins, and spirit veins have their own lives,
* cultivation is a real force that changes bodies, terrain, and social power,
* the world can be reshaped, but only within the laws and forces of the simulation,
* and the world remains consistent across saves because canon is fixed.

## 2.3 North-star experience

The game is successful if a player can:

1. start in a canonical mortal settlement,
2. observe NPCs behaving independently,
3. learn the world by watching and interacting,
4. cultivate, travel, fight, trade, and build,
5. reshape the land and society through power and time,
6. and eventually leave the planet and travel into space.

---

# 3. Design pillars

## Pillar A — Canon is reality

Everything explicit in the source novels is the truth of the world. Canon geography, identities, histories, and laws are authoritative.

## Pillar B — Simulation owns the world after start

Once the canonical start state is loaded, the world continues on its own. NPCs, beasts, sects, weather, politics, rumor, memory, and opportunity all evolve whether the player is nearby or not.

## Pillar C — The player can alter the world

The player can mine, build, destroy, terraform, and create structures after launch. Player changes persist separately from the canonical blueprint.

## Pillar D — Visual truth matters

The player must be able to infer meaning from the scene: danger, intent, power, status, cultivation, and social relationships should be legible by looking.

## Pillar E — Performance is a design constraint

The game must keep a stable frame rate with large visible scenes, many actors, rich terrain, and heavy VFX.

## Pillar F — Semantic authoring

The world must be authored in terms of settlements, buildings, rooms, furniture, roads, spirit veins, and regions — not raw meshes or random terrain.

## Pillar G — Single-player first

All runtime decisions can assume one player, one save, one local authority, and no network synchronization.

---

# 4. Scope

## 4.1 In scope

* custom Three.js engine
* canonical world graph
* semantic world model
* template library
* world compiler / voxel compiler
* simulation runtime
* cultivation system
* NPC cognition / body language / relationships
* terrain deformation and world editing
* settlement life and interiors
* visual effects for qi, formations, and tribulations
* off-planet / space travel support
* local save/load persistence
* strict single-player local runtime

## 4.2 Out of scope for v1

* multiplayer
* network replication
* mod ecosystem
* Minecraft integration of any kind
* procedural geography as the source of truth
* generic fantasy gameplay not tied to canon or xianxia logic
* live-service systems
* cross-platform mobile optimization before PC is solid

---

# 5. Core player loop

The core loop must support:

1. observe the world,
2. learn from the world,
3. gather resources,
4. cultivate,
5. influence people and places,
6. gain access to stronger traversal layers,
7. alter terrain and settlements,
8. and eventually travel into space.

### Loop example

```text
Observe settlement
→ talk / trade / request / teach / fight / gather
→ gain resources / knowledge / relationships
→ cultivate / craft / build / deform terrain
→ unlock new routes / sect access / flight / void travel
→ continue to larger scales
```

---

# 6. World structure

## 6.1 Canonical start state

The game begins from a **fixed canonical world state**. This is not a random seed. It is a handcrafted historical snapshot.

The canonical start state includes:

* fixed geography
* fixed settlements
* fixed important NPC identities
* fixed roads, rivers, spirit veins, and landmarks
* fixed early-world relationships and resource distributions
* fixed starting inventories and settlement conditions

## 6.2 Mutable runtime world

After load, the world changes through:

* simulation deltas
* player deltas
* scripted but simulation-based events
* cultivation breakthroughs
* beasts migrating
* settlements expanding or collapsing
* terrain being reshaped

## 6.3 World layers

The world is stored as a composition of layers:

```text
Layer 1 — Canon Blueprint (immutable)
Layer 2 — Simulation Delta (world changes)
Layer 3 — Player Delta (player changes)
```

Final state at any location is:

```text
Final = Canon + SimulationDelta + PlayerDelta
```

---

# 7. Engine architecture

## 7.1 High-level pipeline

```text
Er Gen Canon
  ↓
Canonical World Graph
  ↓
Semantic World Model
  ↓
Template Library
  ↓
World Compiler / Voxel Compiler
  ↓
Runtime Deltas
  ↓
Three.js Scene Materialization
  ↓
Player
```

## 7.2 Engine modules

### Canon module

Pure lore and world facts.

### World graph module

Contains all semantic relationships and spatial/ social/ event graphs.

### Template library module

Converts semantic objects into reusable geometry/structure templates.

### Compiler module

Compiles semantic world objects into voxel/scene instructions.

### Runtime module

Simulates actors, weather, economy, relationships, ecology, memory, and cultivation.

### Delta module

Persists and merges world changes.

### Presentation module

Three.js rendering, animation, lighting, VFX, UI, camera.

---

# 8. Technical constraints

## 8.1 No Minecraft assumptions

The codebase must not rely on:

* BlockState
* BlockPos semantics
* chunk generation as a world source
* server/client gameplay replication
* entity/block lifecycle assumptions from Minecraft

## 8.2 Three.js-first rendering

All visual systems must target Three.js:

* meshes
* materials
* shaders
* animation
* camera
* postprocessing
* picking / interaction

## 8.3 Single-player only

* no packet systems
* no rollback sync
* no remote authority
* no network prediction
* no multiplayer balancing burden

## 8.4 Deterministic canonical start

Every new game must start from the same canonical state.

---

# 9. Data model

## 9.1 Canon data

Canonical data should be pure, engine-agnostic world facts.

Examples:

* `CanonPlanet`
* `CanonRegion`
* `CanonSettlement`
* `CanonBuilding`
* `CanonRoom`
* `CanonFurniture`
* `CanonCharacter`
* `CanonSpiritVein`
* `CanonRiver`
* `CanonMountain`
* `CanonRoad`
* `CanonArtifact`

## 9.2 Semantic world model

Semantic objects describe what something is, not how it is rendered.

Examples:

* settlement
* building
* room
* furniture
* road segment
* terrain ridge
* river segment
* habitat
* formation anchor
* NPC residence
* cave chamber

## 9.3 Template library

Templates define how semantic objects become geometry.

Examples:

* bed template
* meditation mat template
* sect hall shell template
* village house shell template
* mountain ridge template
* river carving template
* spirit vein prop template

## 9.4 Runtime state

Runtime state includes:

* actor positions
* actor commitments
* actor activities
* relationships
* economy
* memory
* weather
* beast ecology
* cultivation progress
* event history
* player changes

## 9.5 Delta model

Deltas are append-only, time-stamped changes that can be replayed.

Types:

* simulation delta
* player delta
* occasional canon correction delta (rare, explicit, versioned)

---

# 10. World graph specification

The world graph is one of the core systems of the project. Existing graph-engineering concepts from the current project are a good precedent: the codebase already contains a large `WorldGraph` with BFS/weighted-walk support, many node and edge types, a typed `Component` model, and a bootstrap path from canon data into the graph.

## 10.1 Required graph domains

* canon graph
* spatial graph
* navigation graph
* settlement graph
* social graph
* influence graph
* event graph
* memory graph
* economy graph
* cultivation graph

## 10.2 Canon graph examples

* character relationships
* explicit canon locations
* artifact ownership
* historical events
* faction membership
* bloodline relations

## 10.3 Spatial graph examples

* contains
* adjacent-to
* connected-by-road
* connected-by-river
* fed-by-spirit-vein
* overlooks
* hidden-by
* reachable-by-flight

## 10.4 Event graph examples

* caused-by
* witnessed-by
* remembered-by
* disrupted-by
* resolved-by
* propagated-to

## 10.5 Graph responsibilities

The graph must answer:

* where things are
* how things are connected
* who knows whom
* which events matter to which actors
* what areas are loaded or prioritized
* what paths are feasible at a given realm

Research task for the team:

* define the minimum graph schema for world assembly, NPC reasoning, terrain deformation, rumor propagation, and off-world travel.
* identify whether additional graph layers are needed for space/orbital navigation.

---

# 11. Template library specification

## 11.1 Purpose

The template library bridges meaning to geometry.

## 11.2 Rules

* semantic objects do not place blocks/meshes directly
* templates can change visually without changing canon
* furniture and rooms compile through templates
* theme and region can affect material choice
* anchors define where templates fit inside a room/building

## 11.3 Required template categories

### Furniture templates

* bed
* meditation mat
* bookshelf
* storage chest
* hidden storage
* desk
* lamp
* furnace
* spirit well

### Building templates

* poor village home
* sect disciple room
* elder house
* alchemy lab
* storage shed
* shrine
* hall
* gatehouse

### Terrain templates

* mountain ridge
* valley
* river cut
* forest patch
* cliff face
* plateau
* basin
* cave opening

### Decoration templates

* fence
* path edging
* lantern line
* garden markers
* spirit stone accents

## 11.4 Template output

Templates compile into voxel/scene instructions, not final rendering objects.

Research task for the team:

* define the smallest reusable library that can cover the first 80% of canonical structures.
* determine how many template variants are enough before diminishing returns.

---

# 12. World compiler specification

## 12.1 Purpose

The compiler turns semantic objects into renderable geometry instructions.

## 12.2 Required compilation steps

1. load semantic object
2. resolve template
3. resolve theme/material slot
4. resolve bounds and anchors
5. emit voxel/scene instructions
6. hand output to materializer

## 12.3 Intermediate representation

The compiler should emit a neutral IR.

Example:

```text
VoxelInstruction {
  position,
  slot,
  provenance,
  priority,
  opacity,
  category
}
```

## 12.4 Determinism

The compiler must be deterministic for the same inputs.

## 12.5 Incremental rebuilds

Only dirty regions should be recompiled.

---

# 13. Geometry model

## 13.1 Terrain model

Terrain should be authored as continuous fields plus deterministic local detail.

### Macro terrain

Use:

* control points
* splines
* ridgelines
* valleys
* basin fields
* river flow fields
* erosion fields

### Micro terrain

Use deterministic variation only.

* seeded scatter
* small rock variation
* vegetation variation
* surface roughness

## 13.2 Terrain equations

A useful terrain model is:

```text
h(x,z) = Σᵢ aᵢ · φ(||(x,z) - cᵢ||) + r(x,z)
```

Where:

* `φ` is a radial basis function
* `cᵢ` are authored control points
* `r(x,z)` is deterministic micro-detail noise

## 13.3 Rivers as flow

A river should be a flow result, not a decorative line.

```text
F(x) = -∇h(x) + ε(x)
```

Then river paths follow the flow field.

## 13.4 Structures as semantic volumes

Buildings, caves, and sect halls are volumes with meaning, not collections of blocks.

## 13.5 Room geometry

Rooms should support:

* bounds
* anchors
* function
* ownership
* adjacency
* sightlines
* light access
* ritual/cultivation constraints

Research task for the team:

* choose whether the terrain backend should be voxel-only, SDF-only, or a hybrid of both.
* prototype the support-graph/collapse system for destructible terrain.

---

# 14. Physics and deformation

## 14.1 Physics goals

The world should feel physically grounded, but cultivation should still break ordinary rules.

## 14.2 Deformation model

Deformation must consider:

* attack power
* realm multiplier
* technique type
* material hardness
* toughness
* cohesion
* qi resistance
* formation anchoring
* load support

## 14.3 Damage formula concept

```text
A = technique_power × realm_multiplier × qi_channeling × contact_quality × intent_focus
R = hardness × toughness × cohesion × reinforcement × formation_factor × qi_stability
```

If `A > R`, damage occurs.

## 14.4 Deformation tiers

* mortal-scale damage
* room-scale damage
* structure-scale damage
* cliff-scale damage
* mountain-scale damage
* region-scale deformation

## 14.5 Collapse solver

When support is removed, the world must recompute collapse and propagation.

Research task for the team:

* compare voxel destruction, graph-based collapse propagation, and SDF region deformation for best results in Three.js.
* define realm thresholds for terrain destruction and reshaping.

---

# 15. Cultivation system specification

## 15.1 Core cultivation data

* current realm
* qi pool
* qi capacity
* divine sense
* karma
* dao comprehension
* spirit root quality
* breakthrough readiness
* heart demon risk
* essences comprehended

## 15.2 Cultivation progression

Cultivation is not XP. It is a simulation of state change.

## 15.3 Required gameplay outputs

* visible aura changes
* posture changes
* movement changes
* breath control
* world interaction changes
* realm-gated mechanics
* qi field effects

## 15.4 Breakthroughs

Breakthroughs require a computed probability influenced by:

* qi pool
* comprehension
* spirit root
* karma
* environmental pressure
* tribulation survival
* technique compatibility

Research task for the team:

* define the canonical realm ladder for the target story scope.
* determine what mechanics unlock at each key realm tier.
* define what space travel means in terms of realm milestones.

---

# 16. NPC cognition and acting

## 16.1 NPC cognition stack

Recommended layers:

* perception
* interpretation
* motivation
* reasoning
* commitment
* intent
* performance
* action

## 16.2 Acting layer

NPCs should communicate thought through body language.

Channels:

* attention
* urgency
* confidence
* concealment
* tension
* patience
* fatigue

## 16.3 What the player should read visually

* whether the NPC is observing,
* whether they are cautious,
* whether they are suppressing strength,
* whether they are about to flee,
* whether they are considering the player,
* whether they are interested in a world event.

## 16.4 Relationship behavior

Relationships are graph-based and context-sensitive.
Edges may include:

* trust
* respect
* fear
* debt
* grievance
* familiarity
* mentorship
* affection
* rivalry

Research task for the team:

* define the minimal body-language vocabulary needed for mortal, disciple, cultivator, and beast behavior.
* determine how to visually distinguish "watching a wolf," "watching a cultivator," and "watching the player."

---

# 17. Ecology and economy

## 17.1 Ecology

Beasts and plants must be embedded in habitat graphs.

* prey/predator webs
* spirit vein dependency
* climate and region effects
* migration and breeding
* harvest pressure

## 17.2 Economy

Local economies should include:

* food
* spirit stones
* herbs
* tools
* housing
* trade routes
* market scarcity
* faction demand

## 17.3 Settlement behavior

Settlements should have:

* mood
* prosperity
* fear
* security
* recruitment pressure
* trade pressure
* resource pressure

Research task for the team:

* define the first 20 world events for the starting village that feel alive but not scripted.

---

# 18. Terrain and world deformation at scale

## 18.1 Local deformation

Digging, building, and combat damage affect nearby material fields and support graphs.

## 18.2 Regional deformation

High-level cultivation should enable:

* cliff cutting
* trench making
* mountain splitting
* river redirection
* wall collapse
* cave opening
* settlement reshaping

## 18.3 Planetary deformation

Later-stage events may allow:

* continent-scale alteration
* ocean/void boundary effects
* region-level formation rewriting
* planetary ascent-related changes

Research task for the team:

* define the stepwise deformation powers by realm so the game remains believable from mortal to planetary scale.

---

# 19. Off-planet / space progression

## 19.1 Requirement

The game must eventually support a progression path from planetary surface to space traversal.

## 19.2 Required systems

* atmospheric transition
* orbital or high-altitude traversal
* void/astral or equivalent xianxia travel zones
* off-world loading and rendering
* long-distance navigation
* high-scale visual effects

## 19.3 Research requirement

The team must determine the best canon-faithful interpretation of the first space milestone and how it maps to realm progression.

Research task for the team:

* identify whether space travel should be represented as orbital ascent, void travel, astral gate transit, or a combination.
* choose which canon world region or milestone should be the first off-planet bridge.

---

# 20. Rendering specification

## 20.1 Rendering goals

* visually beautiful
* readable at a glance
* regionally distinct
* xianxia-authentic
* stable frame time

## 20.2 Scene layers

* terrain
* architecture
* foliage
* actors
* VFX
* UI
* atmosphere

## 20.3 Materials

Use physically based material principles with semantic overrides:

* base color
* roughness
* metallic
* emissive
* normal
* AO
* transmission
* anisotropy

## 20.4 Region-specific look language

Each region should have a clear visual language.
Examples:

* mortal villages: rough, worn, earthy
* sects: polished, disciplined, symbolic
* forbidden lands: scarred, unstable, ominous
* advanced cultivation spaces: luminous, controlled, surreal

## 20.5 VFX language

* qi = flow / pressure / glow
* formations = boundaries / distortions / runes
* tribulation = atmospheric violence
* sword qi = directional streaks and cutting light
* divine sense = subtle spatial shimmer

Research task for the team:

* benchmark Three.js material, lighting, fog, and postprocessing techniques suitable for AAA-style stylized realism.
* determine the best method to represent qi and cultivation pressure visually without making the scene noisy.

---

# 21. User interface and interaction

## 21.1 UI principles

* minimal clutter
* context-aware
* readable in motion
* xianxia tone
* supports discovery rather than over-explaining

## 21.2 Key UI systems

* health/qi/divine sense indicators
* inventory
* cultivation interface
* relationship interface
* world map / travel map
* debug console
* interaction prompts
* conversation affordance wheel or similar system

## 21.3 Interaction verbs

The world should expose verbs such as:

* talk
* ask
* offer
* request
* teach
* trade
* challenge
* recruit
* gift
* follow
* investigate
* cultivate
* mine
* build
* break
* gather

## 21.4 Debug UX

Must support inspectable runtime data:

* actor state
* settlement state
* world events
* memory traces
* relationships
* physics/deformation status
* performance metrics

---

# 22. Save/load specification

## 22.1 Save data

Save files must include:

* canonical version reference
* runtime world state
* world deltas
* player deltas
* actor states
* settlement states
* graph state
* memory state
* event history
* economy state
* terrain deformation state

## 22.2 Reload behavior

* canonical world remains the same on new save
* runtime deltas persist when reloading the same save
* player deltas persist when reloading the same save
* a new save begins from the same canonical state

## 22.3 Save integrity tests

Must verify:

* destroy a wall
* save/load
* wall remains destroyed in same save
* new save restores the original wall

Research task for the team:

* choose a world save format that supports append-only deltas, deterministic replay, and efficient region-level loading.

---

# 23. Performance specification

## 23.1 Target

* 60 fps target on a typical gaming desktop
* stable frame pacing
* no large allocation spikes during normal play

## 23.2 Performance strategy

* worker-thread geometry compilation
* chunk/region caching
* instancing for repetitive props
* LOD for terrain and actors
* culling by visibility and graph relevance
* dirty-region rebuild only

## 23.3 Simulation LOD

Non-nearby simulation can be simplified, but must remain consistent.

Examples:

* far NPCs simulate at lower frequency
* offscreen beasts use habitat-level logic
* distant settlements update as summaries instead of per-frame detail

## 23.4 Memory discipline

* pool common geometry
* reuse VFX emitters
* avoid per-frame object churn
* avoid full-scene scans each tick

Research task for the team:

* establish the maximum safe scene density for early village, mid-settlement, and late-game sect environments.
* define a practical performance budget for AI, geometry, and VFX separately.

---

# 24. Art production requirements

## 24.1 Asset classes

* humans / cultivators
* beasts
* buildings
* interior furniture
* terrain features
* spirit effects
* weapons / artifacts
* UI icons and overlays

## 24.2 Art requirements

* canon-faithful silhouette
* readable from gameplay camera
* regionally consistent palette
* high-quality materials
* robust animation compatibility

## 24.3 Quality gates

Every major asset family must pass:

* visual readability
* xianxia tone fidelity
* shape language consistency
* animation integration
* performance integration

Research task for the team:

* define the first asset bible for human cultivators, mortal villagers, spirit beasts, and basic sect architecture.

---

# 25. Production phases

## Phase 1 — Engine skeleton

* Three.js app boots
* data loads
* semantic world graph loads
* placeholder world materializes
* camera and input work

## Phase 2 — Canon village vertical slice

* one canonical village loads from semantic data
* one canonical home loads from templates
* one NPC exists with body language and daily behavior
* player can interact, walk, mine, place, and save/load

## Phase 3 — Cultivation and world deformation

* qi fields
* realm progression
* terrain resistance model
* destruction and collapse
* visible body-language and VFX

## Phase 4 — Regional simulation

* roads
* settlements
* trade
* beasts
* rumors
* memory and relationship propagation

## Phase 5 — Flight and sect travel

* aerial movement
* higher-level traversal
* sect access and local politics
* larger visual range

## Phase 6 — Planetary ascent and space travel

* atmospheric exit
* off-planet travel spaces
* large-scale visual effects
* high-scale navigation and persistence

---

# 26. Success metrics

The game is on the right track if the following become true:

* A new save always begins from the same canon world.
* The player can recognize the world as Er Gen-inspired without explanation.
* NPCs clearly live independently.
* Player edits persist and matter.
* Settlements feel like living places, not prop clusters.
* Cultivation is visible, powerful, and readable.
* The player can travel from mortal village life into space.
* The engine maintains strong frame time under load.

---

# 27. Research tasks for the team

These should be actively researched during implementation:

1. **Three.js open-world rendering**

   * streaming
   * chunking
   * instancing
   * postprocessing
   * shader architecture
   * worker-thread meshing

2. **Voxel and terrain techniques**

   * greedy meshing
   * surface nets
   * dual contouring
   * SDF terrain
   * collapse propagation

3. **xianxia visual language**

   * robes
   * architecture
   * talismans
   * qi glow
   * formation geometry
   * beast aesthetics

4. **Canon research**

   * geography
   * characters
   * realm progression
   * off-world milestones
   * cultivation mechanics

5. **Systems research**

   * graph traversal
   * memory systems
   * relationship models
   * event propagation
   * world-state persistence

6. **Performance research**

   * GPU/CPU budgets
   * scene density management
   * memory reuse
   * streaming architecture

---

# 28. Definition of done

This PRD is only actionable if it can support the project from first engine boot to off-planet travel without collapsing into vague abstraction.

The product is complete when a player can:

* start in a faithful canonical mortal world,
* live inside that world,
* change it,
* watch it change independently,
* progress through cultivation and world scale,
* and eventually leave the planet and travel into space,

all in a system that is visually beautiful, technically performant, and canon-faithful.
