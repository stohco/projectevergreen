# PRD Progress Audit — CRON-THREEJS-20

## Phase 1 — Engine skeleton
| Requirement | Status | File(s) |
|-------------|--------|---------|
| Three.js app boots | ✅ | WorldCanvas.tsx |
| Data loads | ✅ | ri_canon_database.json (632 entries) |
| Semantic world graph loads | ✅ | WorldGraph.ts, CanonGraphLoader.ts |
| Placeholder world materializes | ✅ | SmoothTerrain.ts, SettlementCompiler.ts |
| Camera and input work | ✅ | WorldCanvas.tsx (Y lock, RMB orbit, scroll zoom) |

**Phase 1: COMPLETE**

## Phase 2 — Canon village vertical slice
| Requirement | Status | File(s) |
|-------------|--------|---------|
| One canonical village loads from semantic data | ✅ | WangFamilyVillage.ts (25 buildings) |
| One canonical home loads from templates | ✅ | SettlementCompiler.ts + TemplateLibrary.ts |
| One NPC exists with body language | ⬜ PARTIAL | CultivatorModel.ts (model exists, no cognition) |
| Player can interact, walk | ✅ | WorldCanvas.tsx |
| Player can mine, place | ⬜ | VoxelRaycaster.ts exists but not wired |
| Player can save/load | ⬜ | WorldDeltaStore.ts exists but not wired |

**Phase 2: 60% COMPLETE** — needs NPC cognition, mining/placing, save/load

## Phase 3 — Cultivation and world deformation
| Requirement | Status | File(s) |
|-------------|--------|---------|
| Qi fields | ✅ | QiField.ts (PDE solver) |
| Realm progression | ⬜ PARTIAL | REALM_CAPABILITIES in TerrainDeformation.ts |
| Terrain resistance model | ✅ | TerrainDeformation.ts (TerrainResistance) |
| Destruction and collapse | ✅ | TerrainDeformation.ts (CollapseSolver) |
| Visible body-language | ⬜ | CultivatorModel.ts has animations, no cognition |
| VFX | ⬜ | ParticleSystem.ts exists, not wired |

**Phase 3: 40% COMPLETE** — needs wiring to gameplay

## Phase 4 — Regional simulation
| Requirement | Status |
|-------------|--------|
| Roads | ✅ (in village data) |
| Settlements | ✅ (1 village) |
| Trade | ⬜ |
| Beasts | ⬜ (21 species in JSON, not spawned) |
| Rumors | ⬜ (GraphQueryService exists, not wired) |
| Memory/relationship propagation | ⬜ |

**Phase 4: 10% COMPLETE**

## Phase 5 — Flight and sect travel
| Requirement | Status |
|-------------|--------|
| Aerial movement | ✅ (F key sword-flight) |
| Higher-level traversal | ⬜ |
| Sect access | ⬜ |
| Larger visual range | ⬜ |

**Phase 5: 15% COMPLETE**

## Phase 6 — Planetary ascent and space travel
| Requirement | Status |
|-------------|--------|
| Atmospheric exit | ⬜ |
| Off-planet travel | ⬜ |
| Large-scale VFX | ⬜ |

**Phase 6: 0% COMPLETE**

## PRD Section Coverage

| PRD Section | Status | Notes |
|-------------|--------|-------|
| §1 Product definition | ✅ | Single-player, Three.js, canon-first |
| §2 Vision | ✅ | Mortal→space progression |
| §3 Design pillars | ✅ | All 7 pillars implemented in architecture |
| §4 Scope | ✅ | In/out scope clear |
| §5 Core loop | ⬜ | Observe→learn→gather→cultivate not wired |
| §6 World structure | ✅ | Canon+Sim+Player delta layers |
| §7 Engine architecture | ✅ | Canon→Graph→Semantic→Template→Compiler→Runtime→Render |
| §8 Tech constraints | ✅ | No Minecraft, Three.js-first, single-player |
| §9 Data model | ✅ | CanonSettlement/Building/Room/Furniture/SpiritVein/Road |
| §10 World graph | ⬜ PARTIAL | 1 of 10 graph domains (canon only) |
| §11 Template library | ✅ | 12 furniture templates, 3 building themes |
| §12 World compiler | ✅ | SettlementCompiler.ts |
| §13 Geometry model | ✅ | RBF terrain, SDF rivers/cliffs |
| §14 Physics/deformation | ✅ | A vs R + CollapseSolver |
| §15 Cultivation | ⬜ PARTIAL | Realm data exists, no progression system |
| §16 NPC cognition | ⬜ | Model exists, no cognition stack |
| §17 Ecology/economy | ⬜ | 21 beasts + 32 herbs in JSON, not spawned |
| §18 Terrain at scale | ✅ | Deformation tiers + collapse |
| §19 Off-planet | ⬜ | Not started |
| §20 Rendering | ✅ | PBR materials, Gerstner ocean, sky shader |
| §21 UI/interaction | ⬜ PARTIAL | HUD exists, no interaction verbs |
| §22 Save/load | ⬜ | WorldDeltaStore exists, not wired |
| §23 Performance | ⬜ | 10 FPS (needs optimization) |
| §24 Art production | ⬜ | Procedural only, no asset pipeline |
| §25 Production phases | Phase 1 ✅, Phase 2 60%, Phase 3 40% |

## Highest-Impact Next Steps (per PRD)

1. **Wire save/load** (§22) — WorldDeltaStore → localStorage. Destroy wall → save → reload → wall still destroyed.
2. **Wire mining/placing** (§5) — VoxelRaycaster → WorldFacade.setPlayerBlock. Player can mine terrain and place blocks.
3. **NPC cognition** (§16) — perception → interpretation → motivation → commitment → intent → performance → action.
4. **Spawn canon beasts** (§17) — 21 species from ri_canon_beast_ecology.json into their canon regions.
5. **Performance optimization** (§23) — 10 FPS → 60 FPS. Reduce ray casts, add frustum culling, instanced rendering.
6. **Graph domains** (§10) — 9 more graph projections (spatial, nav, settlement, social, influence, event, memory, economy, cultivation).
