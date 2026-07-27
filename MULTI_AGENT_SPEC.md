# Multi-Agent Operating Spec
## Ergenverse Three.js — Single-Player Engine

**System goal:** Build a canon-faithful, single-player, Three.js open-world xianxia engine using one shared world model, five specialized agents, and a strict PRD/Audit loop.

**Core invariant:** One shared truth, many specialized views.

```
Canon JSON/MD
  → Canon Kernel
  → World Graph + Semantic Model
  → Simulation Runtime
  → Delta Layers
  → Three.js Presentation
```

---

## 1. Operating Principles

### 1.1 Single Source of Truth
All agents read from and write to the same shared project state. No agent may invent a parallel truth.

### 1.2 Strict Domain Ownership
Each agent owns one domain. No agent may directly mutate another agent's domain except through agreed interfaces.

### 1.3 Read-Before-Write
Every cycle: read PRD, PRD audit, latest worklog, current project state.

### 1.4 Evidence-First
No success reported without: build result, test result, screenshot, log, or identified reason verification was impossible.

### 1.5 No Hidden Side Effects
Agent must document: what changed, why, what it affects, what needs verification.

---

## 2. Shared System Layers

### 2.1 Canon Layer — Immutable lore from .json and .md
### 2.2 Graph Layer — Canonical world graph + projections
### 2.3 Runtime Layer — Mutable simulation state
### 2.4 Delta Layers — Append-only world changes (canon/sim/player)
### 2.5 Presentation Layer — Three.js output only

---

## 3. Shared Contracts

### 3.1 InputState (immutable for the frame)
```typescript
interface InputState {
  move: { x: number; z: number };
  camera: { yaw: number; pitch: number; locked: boolean };
  interact?: boolean;
  attack?: boolean;
  openInventory?: boolean;
  debug?: string;
}
```

### 3.2 WorldState (authoritative runtime state)
```typescript
interface WorldState {
  canonicalVersion: string;
  runtimeTick: number;
  actors: ActorState[];
  settlements: SettlementState[];
  terrain: TerrainState;
  events: WorldEvent[];
  deltas: WorldDelta[];
}
```

### 3.3 RenderSnapshot (projection for drawing)
```typescript
interface RenderSnapshot {
  visibleActors: VisibleActor[];
  visibleTerrainChunks: VisibleChunk[];
  visibleProps: VisibleProp[];
  lighting: LightingState;
  weather: WeatherState;
}
```

### 3.4 WorldDelta (append-only changes)
```typescript
interface WorldDelta {
  id: string;
  kind: string;
  provenance: "CANON" | "SIMULATION" | "PLAYER";
  targetIds: string[];
  tick: number;
  payload: unknown;
}
```

### 3.5 AuditEntry
```typescript
interface AuditEntry {
  phase: string;
  status: "PASS" | "WARN" | "FAIL" | "BLOCKED";
  evidence: string[];
  gaps: string[];
  nextAction: string;
}
```

---

## 4. Frame/Update Contract

```
InputSystem → InputState → SimulationSystem → WorldState → RenderSystem
```

- **InputSystem** captures keyboard/mouse/UI. Produces InputState only. Does NOT move player, open doors, resolve collision, trigger NPC behavior.
- **SimulationSystem** consumes InputState + dt. Does locomotion, collision, doors, cognition, AI, interactions, delta writes. Does NOT touch DOM, render, draw UI.
- **RenderSystem** consumes WorldState projections. Draws world, animates entities, shows VFX, renders UI. Does NOT decide gameplay, process input, mutate state.

---

## 5. Agent Roster & Specifications

### Agent 1 — Simulation Engine
**Owns:** player locomotion, collision, doors, NPC cognition, beast AI, save/load, delta writes, interaction resolution, time, body-facing.
**Reads:** InputState, WorldState, graph queries, PRD, QA feedback.
**Writes:** WorldState transitions, SimulationDelta, actor state, save/load.
**Forbidden:** DOM access, rendering, asset creation, graph schema, content registration, audit editing.
**Acceptance:** input changes simulation state, state persists, NPCs act, doors/collision correct, body facing correct, save/load works.
**Fails if:** movement is DOM mutation, sim/render diverge, no gameplay change, hidden unserialized state.

### Agent 2 — Graph & Data Layer
**Owns:** canon ingestion, node identity, graph layers, query service, schemas, delta persistence for graph.
**Reads:** all canon .json/.md, PRD, audit gaps, simulation/content needs.
**Writes:** graph projections, schemas, bootstrap, query utilities.
**Forbidden:** rendering, animation, simulation control, hardcoded lore.
**Acceptance:** canon loads into graph, layers queryable, stable IDs, projections support gameplay.
**Fails if:** graph unconnected to gameplay, queries scan everything, lore hardcoded, no provenance.

### Agent 3 — Visual AAA & Models
**Owns:** character models, settlement rendering, terrain visuals, ocean/sky/atmosphere, postprocessing, beast models, materials, lighting, LOD.
**Reads:** render snapshots, model defs, template outputs, QA criticism, canon tone.
**Writes:** models, materials, shaders, scene composition, VFX, screenshots.
**Forbidden:** gameplay logic, canon rewriting, simulation authority, graph writes.
**Acceptance:** canon-faithful look, readable at gameplay distance, body language visible, handcrafted feel, performant.
**Fails if:** generic fantasy, unreadable silhouettes, noisy materials, scale problems, disconnected from simulation.

### Agent 4 — World Content & Systems
**Owns:** items, techniques, artifacts, crafting, cultivation, beasts, herbs, economy, ecology, recipes.
**Reads:** canon JSON/MD, graph projections, PRD, gameplay gaps.
**Writes:** item registries, content schemas, cultivation content, spawn data, technique defs, drop tables.
**Forbidden:** hardcoded content where data exists, visual-only work, simulation logic, graph schema edits.
**Acceptance:** content data-driven, items/beasts/herbs/techniques usable, flows into runtime+graph, canon preserved.
**Fails if:** content exists only as data, duplicated in code+data, disconnected from lore, no graph projection.

### Agent 5 — QA & Debug Critic
**Owns:** smoke tests, visual QA, canon audit, performance, architecture compliance, bug hunting, PRD audit, red-flag escalation.
**Reads:** build logs, runtime logs, screenshots, PRD, audit, worklog, agent outputs.
**Writes:** QA audit entries, PRD_AUDIT.md, failure reports, regression notes, BLOCKED flags.
**Forbidden:** pretending success, implementing features instead of testing, suppressing issues, vague language.
**Acceptance:** can reproduce/disprove bugs, gives specific evidence, honest audit state, flags violations early.
**Fails if:** optimistic reporting without proof, missing screenshots/logs, not updating audit, hiding debt.

---

## 6. Data Ownership Rules

| Domain | Owner |
|--------|-------|
| Canon data → graph | Agent 2 only |
| Runtime state mutations | Agent 1 only |
| Content registries | Agent 4 only |
| Visual presentation | Agent 3 only |
| Audit ledger | Agent 5 only |

---

## 7. Write Surfaces by Agent

**Agent 1 writes:** simulation runtime, delta store, movement/collision/cognition state, save/load state.
**Agent 2 writes:** graph schema, bootstrap, projection data, canon ingestion utilities.
**Agent 3 writes:** model code, materials, shaders, VFX, scene composition.
**Agent 4 writes:** item/skill/technique systems, crafting, economy data, gameplay registries.
**Agent 5 writes:** audit, test results, blocker notes, verification reports.

---

## 8. No-Go Zones

- Agent 1: no canon data changes, no model redesign, no content definitions.
- Agent 2: no gameplay behavior, no rendering, no hardcoded lore.
- Agent 3: no game logic, no canon meaning changes, no spawn/relationship rules.
- Agent 4: no bypassing simulation/graph, no hardcoded lore, no assets without template mapping.
- Agent 5: no unverified claims, no downgrading issues without proof, no hiding regressions.

---

## 9. Per-Agent Output Format

```
AGENT:
DOMAIN:
INPUTS READ:
CHANGES MADE:
VERIFICATION:
CANON IMPACT:
GAMEPLAY IMPACT:
PERFORMANCE IMPACT:
RISKS:
NEXT STEP:
```

---

## 10. Implementation Order

### Phase A — Engine and Truth
- Agent 2: graph ingestion + stable IDs
- Agent 1: input/simulation split, movement/collision, save/load
- Agent 5: verification + audit baseline

### Phase B — World Legibility
- Agent 3: model quality, material language, body language
- Agent 4: herbs, beasts, items, techniques, cultivation data

### Phase C — Systemic Depth
- Agent 1: NPC cognition, interactions, world change
- Agent 2: memory, opportunity, event, economy, ecology graphs
- Agent 4: crafting, dual cultivation, combat, drops

### Phase D — Scale
- Agent 3: LOD, culling, dense scenes, environment quality
- Agent 1: larger traversal, deformation, combat scaling
- Agent 2: more graph projections for regions, travel, space

### Phase E — Endgame
- Off-planet progression, astral/void travel, high-scale deformation, deep sect warfare, massive persistence.

---

## 11. Final Operating Principle

- If a task can be answered by a graph layer, do not invent a separate engine.
- If a task can be answered by a template, do not hardcode geometry.
- If a task can be answered by a delta, do not mutate canon.
- If a task can be answered by QA, do not ship on optimism.
- If a task can be answered by simulation, do not let rendering decide it.
