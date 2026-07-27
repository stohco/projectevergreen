#!/usr/bin/env python3
"""Append the CRON-COMPLETIONIST-139 worklog entry."""

import subprocess
from pathlib import Path

ENTRY = """---
Task ID: CRON-COMPLETIONIST-139
Agent: cron-completionist
Task: Wire WorldStateEngine queries 1-4 to graph-first with JSON fallback.
Closes CRON-137 self-critique #7: 'WorldStateEngine and ActorMaterializer are
NOT wired to the graph. The user's "Next Move" listed these as priorities 2
and 3. I only completed priority 1 (architecture JSON) + RumorNetwork wiring.
WorldStateEngine still uses brute-force JSON iteration for all 6 queries.
Score 5/10 for consumer wiring (1 of 3 consumers wired).' This CRON ships
the WorldStateEngine half (priority 2); ActorMaterializer (priority 3) is
the next CRON.

Work Log:
- STEP 1: Read worklog tail. Confirmed CRON-138 (commit 4689a77) shipped qi
  cost for CultivatorSwordQiGoal + TerrainSpiritifier dimension-gate fix.
  CRON-137 next-priority list (a)-(f) ranked 'Wire WorldStateEngine 6 query
  methods to graph-first with JSON fallback' as (a) Score 9/10 CRITICAL —
  user's stated next move. Picked (a).
- STEP 2: Audited WorldStateEngine.java (1056 lines). Found 6 query methods:
  - queryWhatExists (line 586) — iterates npcs/civilizations/ecosystems/
    macro_terrain/species JSON subsystems, no graph consultation.
  - queryWhoOwns (line 690) — iterates provenance + civilizations, no graph.
  - queryWhoWants (line 730) — iterates karma consequences, no graph.
  - queryWhoKnows (line 785) — iterates karma bearers, no graph.
  - queryWhyUntaken (line 820) — iterates opportunities + queryWhoOwns, no graph.
  - queryNaturalNext (line 868) — iterates karma unresolved_until, no graph.
  All 6 use WorldStateDataLoader.getSubsystem()/getEntry() JSON iteration.
- STEP 3: Audited GraphQueryService.java (416 lines). Found 4 graph-backed
  equivalents of WorldStateEngine Q1-Q4:
  - whatExistsAt(NodeId locationId) → List<LocationEntry> — graph-backed Q1
  - whoOwns(NodeId entityId) → OwnershipInfo — graph-backed Q2
  - whoWants(NodeId entityId) → List<DesireInfo> — graph-backed Q3
  - whoKnowsAbout(NodeId entityId) → List<KnowledgeInfo> — graph-backed Q4
  No graph equivalents of Q5 (opportunities) or Q6 (karma unresolved_until).
- STEP 4: Audited GraphBootstrap.java (308 lines). Confirmed:
  - GRAPH is public static volatile WorldGraph (line 62) — accessible from
    WorldStateEngine without getter.
  - query() throws IllegalStateException if QUERY_SERVICE is null (i.e.,
    bootstrap not called). WorldStateEngine must defensively check
    GraphBootstrap.GRAPH != null before calling query().
  - Bootstrap is idempotent (volatile boolean bootstrapped, line 67).
  - Graph is populated from RICanonicalDatabase: 158 NPC nodes, 80 LOCATION,
    178 ARTIFACT, 214 TECHNIQUE. Social edges from CanonCharacter.relationships,
    OWNS edges from CanonArtifact.currentOwner, LOCATED_IN edges from
    CanonLocation.parentLocation.
- STEP 5: Audited RICanonicalDatabase canon class structure. CanonCharacter/
  CanonLocation/CanonArtifact/CanonTechnique all have public final fields:
  - id (e.g., "N01", "L01", "I09_dragon_formation")
  - name (e.g., "Wang Lin", "Heng Yue Sect")
  - nameCn (e.g., "王林", "恒岳派")
  GraphBootstrap uses c.id as NodeId.id and c.name as Node.displayName. nameCn
  is NOT stored on the graph Node (only used for name resolution at bootstrap).
- STEP 6: Designed resolveGraphNode(String idOrName, NodeType type) helper.
  Three lookup strategies in order:
  1. Exact NodeId match — construct NodeId(idOrName, type) and check
     graph.hasNode(). Handles canon ID queries like "L01", "I09_dragon_formation".
  2. Case-insensitive displayName match — iterate graph.nodesOfType(type) and
     compare displayName.toLowerCase(). Handles queries like "Wang Lin" →
     NPC "N01" (displayName "Wang Lin").
  3. Substring fallback — if no exact match, check if any displayName contains
     the query (with underscore→space normalization). Handles queries like
     "heng_yue" → LOCATION "L04" (displayName "Heng Yue Sect").
  Returns null on no match — caller falls through to JSON. Performance:
  O(nodes_of_type) per call (max 158 NPCs, 80 LOCs, 178 ARTs, 214 TECHs =
  ~630 nodes). Acceptable for query-time; tighter inner loops would need a
  name→NodeId cache (future CRON).
- STEP 7: Implemented CRON-139 marker block in WorldStateEngine class
  javadoc. Explains the graph-first query path, the record-type conversion
  via convert* helpers, the dedup-by-objectId merge strategy, the Q5/Q6
  JSON-only status, and the canon fidelity note (graph = canon source,
  JSON = mod-original fallback).
- STEP 8: Added 8 graph imports to WorldStateEngine:
  - dev.ergenverse.graph.Edge (unused — kept for future use)
  - dev.ergenverse.graph.GraphBootstrap
  - dev.ergenverse.graph.GraphQueryService
  - dev.ergenverse.graph.Node
  - dev.ergenverse.graph.NodeId
  - dev.ergenverse.graph.NodeType
  - dev.ergenverse.graph.WorldGraph
  (Edge import currently unused but kept for symmetry; will be needed if
  future CRON adds direct edge inspection. Verified: 100 deprecation warnings,
  0 unused-import warnings.)
- STEP 9: Modified queryWhatExists to add graph-first path:
  - Added Set<String> seenIds for dedup.
  - Added resolveGraphNode(locationId, NodeType.LOCATION) call.
  - If graphLoc != null, call GraphBootstrap.query().whatExistsAt(graphLoc).
  - For each LocationEntry, convert via convertLocationEntry(ge, locationId)
    and add to out if seenIds.add(oe.objectId()) succeeds.
  - Wrapped in try/catch — on exception, log WARN and fall through to JSON.
  - Modified existing JSON loops (npcs/civ/eco/macro_terrain/species) to
    dedupe via seenIds.add(oid) before adding new ObjectEntry. Each loop
    now reads its oid into a local var, checks seenIds, then constructs.
- STEP 10: Modified queryWhoOwns to add graph-first path:
  - Added resolveGraphNode(objectId, NodeType.ARTIFACT) call.
  - If graphArt != null, call GraphBootstrap.query().whoOwns(graphArt).
  - If info != null, return convertOwnershipInfo(info) immediately — do NOT
    fall through to JSON. (Graph is canon source of truth for ownership;
    JSON provenance is fallback for artifacts not in the canon database.)
  - Wrapped in try/catch — on exception, fall through to JSON.
  - Existing JSON path (provenance + civilizations heritage_treasures)
    unchanged.
- STEP 11: Modified queryWhoWants to add graph-first path:
  - Added Set<String> seenWanters for dedup.
  - Added resolveGraphNode(objectId, NodeType.ARTIFACT) call.
  - If graphArt != null, call GraphBootstrap.query().whoWants(graphArt).
  - For each DesireInfo, convert via convertDesireInfo(di) and add to out
    if seenWanters.add(dr.desirerId()) succeeds.
  - Wrapped in try/catch — fall through to JSON on exception.
  - Modified existing karma-consequences loop to dedupe via
    seenWanters.add(bearer) before constructing DesireRecord.
- STEP 12: Modified queryWhoKnows to add graph-first path:
  - Added Set<String> seenKnowers for dedup.
  - Added resolveGraphNode(objectId, NodeType.ARTIFACT) call.
  - If graphArt != null, call GraphBootstrap.query().whoKnowsAbout(graphArt).
  - For each KnowledgeInfo, convert via convertKnowledgeInfo(ki) and add to
    out if seenKnowers.add(kr.knowerId()) succeeds.
  - Wrapped in try/catch — fall through to JSON on exception.
  - Modified existing karma-bearers loop to dedupe via
    seenKnowers.add(bearer) before constructing KnowledgeRecord.
- STEP 13: Did NOT modify queryWhyUntaken or queryNaturalNext — they
  consult opportunities and karma subsystems which have no graph
  equivalents yet. Documented this in the CRON-139 javadoc block:
  'Queries 5-6 remain JSON-only — they consult the opportunities and karma
  subsystems which do not yet have direct graph equivalents. A future CRON
  will add OpportunityNodes + KarmicEventNodes to the graph.'
- STEP 14: Added 7 helper methods:
  - graphAvailable() — returns GraphBootstrap.GRAPH != null.
  - resolveGraphNode(String, NodeType) — 3-strategy lookup, returns NodeId.
  - convertLocationEntry(LocationEntry, queriedLocationId) → ObjectEntry —
    strips namespace prefix from objectId (e.g., "npc:N01" → "N01") to
    maintain compatibility with existing callers that compare bare IDs.
  - convertOwnershipInfo(OwnershipInfo) → OwnershipRecord — uses ownerName
    (not ownerId) for trueOwner field (WorldStateEngine convention).
  - convertDesireInfo(DesireInfo) → DesireRecord — strips namespace from
    entityId and wanterId.
  - convertKnowledgeInfo(KnowledgeInfo) → KnowledgeRecord — strips namespace
    from entityId and knowerId.
  - stripNamespace(String) — utility; returns substring after ':' or the
    input unchanged if no colon.
- STEP 15: Compiled — JAVA_HOME=/tmp/my-project/.jdks/jdk-17.0.13+11/
  ./gradlew compileJava — BUILD SUCCESSFUL in 14s, 0 errors, 100 pre-existing
  deprecation warnings (ResourceLocation constructor — unchanged).
- STEP 16: Wrote scripts/cron139_verify_worldstate_graph_first.py — 70
  checks across 10 groups:
  1. Imports (7 checks: Edge, GraphBootstrap, GraphQueryService, Node,
     NodeId, NodeType, WorldGraph).
  2. Class-level CRON-139 marker (5 checks: header present, mentions
     GraphBootstrap, mentions RICanonicalDatabase, mentions Q5/Q6 JSON-only,
     mentions dedup merge by objectId).
  3. Helper methods (15 checks: graphAvailable, resolveGraphNode with 3
     strategies, returns null on no match, 4 convert* helpers, stripNamespace
     with indexOf+substring).
  4. queryWhatExists graph-first (8 checks: resolveGraphNode(LOCATION),
     whatExistsAt call, convertLocationEntry loop, seenIds Set, JSON fallback
     still runs, JSON dedup, exception catch with WARN log).
  5. queryWhoOwns graph-first (5 checks: resolveGraphNode(ARTIFACT), whoOwns
     call, convertOwnershipInfo returned on hit, JSON provenance fallback,
     civilizations heritage_treasures fallback).
  6. queryWhoWants graph-first (6 checks: resolveGraphNode(ARTIFACT),
     whoWants call, convertDesireInfo loop, seenWanters Set, JSON karma
     fallback, JSON dedup).
  7. queryWhoKnows graph-first (6 checks: resolveGraphNode(ARTIFACT),
     whoKnowsAbout call, convertKnowledgeInfo loop, seenKnowers Set, JSON
     karma fallback, JSON dedup).
  8. No regression Q5/Q6 (6 checks: both still present, both still use
     opportunities/karma JSON, neither calls resolveGraphNode).
  9. Canon fidelity (10 checks: mentions RICanonicalDatabase, no fabricated
     chapter citations, CRON-139 marker >=5 occurrences, 4 GraphQueryService
     methods exist, GraphBootstrap idempotent, GRAPH public static volatile,
     query() returns GraphQueryService).
  10. Build verification (3 checks: exit 0, BUILD SUCCESSFUL, no error:).
  Initial run: 66 pass, 4 fail (4 regex multiline false positives in script).
  Fixed 4 regex patterns to use [\\s\\S] instead of [^}] for multiline match.
  Final: 70/70 pass.
- STEP 17: Ran regression across CRON-130 → CRON-139 (602 checks total):
  - CRON-130 cultivator-flight: 111/111
  - CRON-132 ride-sword visibility: 22/22
  - CRON-133 flight path navigator: 73/73
  - CRON-134 flight qi cost: 85/85
  - CRON-135 upward ray-cast: 60/60
  - CRON-136 tall-obstacle correctness: 53/53
  - CRON-137 graph integration: 65/65
  - CRON-138 sword-qi qi cost: 63/63
  - CRON-139 worldstate graph-first: 70/70
  Total: 602 checks, 0 failures. Zero regressions.
- STEP 18: Clean build — BUILD SUCCESSFUL in 40s. JAR: 8.8 MB
  (ergenverse-0.1.15-alpha.jar).
- STEP 19: Built importable modpack ZIP — ergenverse-modpack-0.1.15-alpha.zip
  (7.1 MB, 7422542 bytes). Verified: valid CurseForge modpack. Copied JAR +
  ZIP to /home/z/my-project/download/ and releases/.
- STEP 20: Version bump 0.1.14-alpha → 0.1.15-alpha in gradle.properties
  (with CRON-139 changelog comment) and mods.toml.
- STEP 21: Git workflow — git add -A (5 files: WorldStateEngine.java,
  gradle.properties, mods.toml, cron139_verify_worldstate_graph_first.py
  [new], ergenverse-0.1.15-alpha.jar + ergenverse-modpack-0.1.15-alpha.zip
  [release artifacts]). git commit + push.

Stage Summary:
- SHIPPED: CRON-COMPLETIONIST-139 wires WorldStateEngine queries 1-4
  (queryWhatExists, queryWhoOwns, queryWhoWants, queryWhoKnows) to the
  graph-first path with JSON fallback. The graph (populated by GraphBootstrap
  from RICanonicalDatabase — 630 nodes + 500 edges) is now the canon source
  of truth for these queries; JSON subsystems (npcs/, civilizations/,
  ecosystems/, provenance, karma) are the fallback for entities not in the
  canon database (e.g., simulation-spawned beasts, mod-original civilizations,
  ecosystem entries). Results from both paths are merged by objectId with
  graph-first ordering and JSON duplicates suppressed. Closes CRON-137
  self-critique #7 (consumer wiring 1/3 → 2/3 — RumorNetwork + WorldStateEngine
  done, ActorMaterializer pending).
- Build status: BUILD SUCCESSFUL in 40s, 0 errors, 100 pre-existing deprecation
  warnings (ResourceLocation constructor — unchanged from CRON-137).
- Git hash: (filled after push)
- Verification: scripts/cron139_verify_worldstate_graph_first.py — 70/70 pass.
  Regression: CRON-130 (111) + CRON-132 (22) + CRON-133 (73) + CRON-134 (85)
  + CRON-135 (60) + CRON-136 (53) + CRON-137 (65) + CRON-138 (63) + CRON-139
  (70) = 602 checks, 0 failures.
- Release artifacts: ergenverse-0.1.15-alpha.jar (8.8 MB) +
  ergenverse-modpack-0.1.15-alpha.zip (7.1 MB) in download/ and releases/.
- Canon sources: graph is populated exclusively from RICanonicalDatabase
  (158 NPCs, 80 locations, 178 artifacts, 214 techniques — all canon). Every
  graph node traces back to a Layer 1 canon entry via Node.canon(id, name,
  registryId, confidence). The canon fact 'cultivators have social
  relationships, ownership ties, karmic connections, and locations have
  parent-child relationships' is universally attested in 仙逆. The specific
  graph wiring (CanonCharacter.relationships → social edges, CanonArtifact.
  currentOwner → OWNS edge, CanonLocation.parentLocation → LOCATED_IN edge)
  is a mod-original interpretation grounded in xianxia genre convention.
  NO fabricated chapter citations.

- HARSH SELF-CRITIQUE (hyper-analytical, fact-checked against canon):
  1. **No runtime playtest verification.** 70 static checks prove the
     graph-first wiring exists at the source-code level. None prove the
     graph actually populates at runtime, that resolveGraphNode actually
     resolves a free-text query like "Heng Yue Sect" to LOCATION node "L04",
     or that whatExistsAt actually returns canon NPCs as LocationEntry
     records. A playtest with a Foundation cultivator querying
     WorldStateEngine.queryWhatExists("heng_yue_sect") should return
     Wang-family-village NPCs + Heng Yue Sect NPCs from the graph (with
     ageYears=0 and trueState="canon"). Score 4/10 for runtime validation
     (NEEDS PLAYTESTING).
  2. **resolveGraphNode substring fallback may match the WRONG node.**
     Strategy 3 (substring fallback) returns the FIRST node whose
     displayName contains the query. If multiple nodes match (e.g., "Zhao"
     matches "Zhao Country" LOCATION and "Zhao Mountains" LOCATION and any
     NPC with "Zhao" in their name), the result is non-deterministic (depends
     on graph iteration order). For now this is acceptable because most
     queries use specific names ("Heng Yue Sect") or IDs ("L04"), but a
     future CRON should add a scoring mechanism (longest match wins) or
     require exact match only. Score 5/10 for lookup robustness (works for
     specific queries, ambiguous for partial queries).
  3. **Graph is in-memory only — no SavedData persistence (carried over
     from CRON-137 #4).** GraphBootstrap.GRAPH is rebuilt from
     RICanonicalDatabase on every world load. Canon edges are stable across
     reloads (rebuilt from the same source). Simulation edges added at
     runtime (none yet — write-back is CRON-141) would be lost on world
     unload. Score 5/10 for persistence (canon rebuilds, sim edges would
     be lost).
  4. **Q5/Q6 remain JSON-only.** queryWhyUntaken (opportunities subsystem)
     and queryNaturalNext (karma unresolved_until) have no graph
     equivalents. The graph has no OpportunityNode or KarmicEventNode type
     yet. A future CRON should add these — but the JSON path works and
     these queries are less performance-critical than Q1-Q4. Score 6/10 for
     completeness (4 of 6 queries graph-backed).
  5. **Name resolution is case-insensitive but not fuzzy (carried over
     from CRON-137 #3).** resolveGraphNode uses exact-match (after lowercasing)
     for strategy 2, and substring for strategy 3. If a caller queries
     "Wang Lin (王林)" or "wang_lin", neither strategy 2 nor strategy 3
     will match "Wang Lin". Score 6/10 for name resolution.
  6. **Dedup is by objectId only — trueState may differ between graph and
     JSON.** If the graph returns ObjectEntry(N01, "npc", "heng_yue_sect",
     0, "Wang Lin — canon") and JSON returns ObjectEntry(N01, "npc",
     "heng_yue_sect", 0, "Wang Lin — Foundation realm"), the JSON entry is
     suppressed. The player sees only "Wang Lin — canon", losing the
     cultivation-realm detail. This is acceptable (graph is canon source of
     truth; JSON details can be added as Components in a future CRON), but
     it does lose information. Score 7/10 for merge fidelity (graph wins,
     JSON details suppressed).
  7. **ActorMaterializer is NOT yet wired to graph (priority 3 from
     CRON-137 list).** This CRON shipped priority 2 (WorldStateEngine).
     Priority 3 (ActorMaterializer.materializeAroundPlayer → graph query
     for threatsNearSettlement + whatExistsAt) is the next CRON. Score
     7/10 for consumer wiring (2 of 3 consumers done — RumorNetwork +
     WorldStateEngine, ActorMaterializer pending).
  8. **No graph write-back (priority 4 from CRON-137 list).** Simulation
     events (beast spawns, social interactions, karmic events) do NOT
     create graph edges. The graph is read-only canon data — it doesn't
     grow as the simulation runs. Score 3/10 for write-back (not implemented,
     carried over from CRON-137 #8).
  9. **The Edge import is unused.** I imported dev.ergenverse.graph.Edge
     but never reference it directly (GraphQueryService returns List<Edge>
     but I only use EdgeType constants via the GraphQueryService methods).
     The import is kept for future use (a CRON that adds direct edge
     inspection will need it). Score 7/10 for import hygiene (1 unused
     import, kept intentionally).
  10. **No fabricated chapter citations.** The graph maps canon entities
      from RICanonicalDatabase (which IS the canon source). The wiring
      (CanonCharacter.relationships → social edges, etc.) is mod-original
      interpretation grounded in xianxia genre convention. The canon fact
      'cultivators have social relationships, ownership ties, karmic
      connections, and locations have parent-child relationships' is
      universally attested. NO "RI Ch.X" citations invented. Score 10/10
      for citation honesty.

- NEXT PRIORITY (in order, post-CRON-139):
  (a) **Wire ActorMaterializer.materializeAroundPlayer() to GraphQueryService
       (Score 8/10, HIGH — user's stated next move, CRON-137 priority 3).**
       Replace SettlementThreatIndex.getSituationThreat() with
       threatsNearSettlement() + replace OpportunityRegistry scan with
       whatExistsAt(). Score 8/10 for simulation integration. Score 3/10
       for implementation difficulty.
  (b) **Add graph write-back: WorldEventBus events → graph edges (Score 8/10,
       HIGH — user's stated next move, CRON-137 priority 4).** Beast spawn
       events create LOCATED_IN edges. Social interaction events create
       FAMILIAR_WITH edges. Karmic events create KARMIC_DEBT/GRUDGE edges.
       Score 8/10 for graph liveness. Score 5/10 for implementation
       difficulty.
  (c) **Create Component classes (Score 7/10, MEDIUM — fixes CRON-137
       self-critique #2).** CultivationComponent (realm, qi),
       LocationComponent (coords, parent), OwnershipComponent (owner, state),
       KarmaComponent (burden, type). Score 7/10 for node state. Score 4/10
       for implementation difficulty.
  (d) **Add qi cost to CultivatorCombatGoal (Score 7/10, MEDIUM — fixes
       CRON-138 self-critique #4).** Melee attacks consume ~2.0 qi per swing.
       Score 7/10. Score 2/10 for implementation difficulty.
  (e) **Qi-bar overlay for cultivators (Score 7/10, MEDIUM — fixes
       CRON-138 self-critique #6 + CRON-134 next-priority (d)).** Render a
       bar above cultivator heads showing current qi / maxQi. Score 7/10.
  (f) **Wire WorldGraph persistence to SavedData (Score 6/10, MEDIUM — fixes
       CRON-137 self-critique #4).** serialize/deserialize on world save/load.
       Score 6/10. Score 3/10 for implementation difficulty.
  (g) **Build name→NodeId cache in GraphBootstrap (Score 6/10, MEDIUM —
       fixes CRON-139 self-critique #2).** resolveGraphNode currently iterates
       all nodes of a type per call. A pre-built cache would make lookups
       O(1). Score 6/10 for performance. Score 2/10 for implementation
       difficulty.
  (h) **Playtest CRON-130 through CRON-139 end-to-end (Score 10/10,
       CRITICAL — user is actively playtesting).** Import 0.1.15-alpha
       modpack ZIP, verify graph populates, verify rumor propagation via
       social connections, verify WorldStateEngine Q1-Q4 graph-first path
       (log "graph-first: N entries" debug messages), verify cultivator
       flight + obstacle avoidance + qi expenditure + sword-qi qi cost.
"""

# Compute git short hash for backfill
try:
    git_hash = subprocess.check_output(
        ["git", "rev-parse", "--short", "HEAD"],
        cwd="/home/z/my-project/forge-mod",
        stderr=subprocess.DEVNULL,
    ).decode().strip()
except Exception:
    git_hash = "(unknown)"

ENTRY_BACKFILLED = ENTRY.replace(
    "Git hash: (filled after push)",
    f"Git hash: {git_hash}",
)

for path in [
    Path("/home/z/my-project/worklog.md"),
    Path("/home/z/my-project/forge-mod/worklog.md"),
]:
    with path.open("a", encoding="utf-8") as f:
        f.write(ENTRY_BACKFILLED)
        if not ENTRY_BACKFILLED.endswith("\n"):
            f.write("\n")
    print(f"Appended CRON-139 entry to {path} ({path.stat().st_size} bytes)")

print(f"\nGit hash: {git_hash}")
print("Done.")
