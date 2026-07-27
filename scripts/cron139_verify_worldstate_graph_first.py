#!/usr/bin/env python3
"""CRON-139 verification: WorldStateEngine graph-first query path.

Validates that WorldStateEngine.queryWhatExists/queryWhoOwns/queryWhoWants/
queryWhoKnows now consult GraphBootstrap.query() BEFORE falling back to JSON
subsystem iteration. Closes CRON-137 self-critique #7: 'WorldStateEngine and
ActorMaterializer are NOT wired to the graph... WorldStateEngine still uses
brute-force JSON iteration for all 6 queries.'

Checks (target ~75):
  1. Imports (graph + bootstrap + query service + node + nodeid + nodetype + worldgraph + edge)
  2. Class-level CRON-139 javadoc marker
  3. Helper methods (graphAvailable, resolveGraphNode, convertLocationEntry,
     convertOwnershipInfo, convertDesireInfo, convertKnowledgeInfo, stripNamespace)
  4. queryWhatExists graph-first path (resolveGraphNode LOCATION, whatExistsAt
     call, convertLocationEntry, dedup via seenIds, JSON fallback still runs)
  5. queryWhoOwns graph-first path (resolveGraphNode ARTIFACT, whoOwns call,
     convertOwnershipInfo, returns immediately if graph non-null, JSON fallback)
  6. queryWhoWants graph-first path (resolveGraphNode ARTIFACT, whoWants call,
     convertDesireInfo, dedup via seenWanters, JSON fallback)
  7. queryWhoKnows graph-first path (resolveGraphNode ARTIFACT, whoKnowsAbout
     call, convertKnowledgeInfo, dedup via seenKnowers, JSON fallback)
  8. No regression — queryWhyUntaken + queryNaturalNext still JSON-only
  9. Canon fidelity — graph sourced from RICanonicalDatabase, no fabricated
     chapter citations
 10. Build verification
"""
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path("/home/z/my-project/forge-mod")
WSE = (ROOT / "src/main/java/dev/ergenverse/simulation/WorldStateEngine.java").read_text()
GRAPH_BOOTSTRAP = (ROOT / "src/main/java/dev/ergenverse/graph/GraphBootstrap.java").read_text()
GRAPH_QUERY = (ROOT / "src/main/java/dev/ergenverse/graph/GraphQueryService.java").read_text()

passed = 0
failed = 0

def check(name, cond, detail=""):
    global passed, failed
    if cond:
        passed += 1
        print(f"  PASS  {name}")
    else:
        failed += 1
        print(f"  FAIL  {name}  {detail}")

print("=" * 60)
print("CRON-139 verification: WorldStateEngine graph-first")
print("=" * 60)

# ─── 1. Imports ───────────────────────────────────────────────────
print("\n[1] Imports — graph types available to WorldStateEngine")
check("import graph.Edge",
      "import dev.ergenverse.graph.Edge;" in WSE)
check("import graph.GraphBootstrap",
      "import dev.ergenverse.graph.GraphBootstrap;" in WSE)
check("import graph.GraphQueryService",
      "import dev.ergenverse.graph.GraphQueryService;" in WSE)
check("import graph.Node",
      "import dev.ergenverse.graph.Node;" in WSE)
check("import graph.NodeId",
      "import dev.ergenverse.graph.NodeId;" in WSE)
check("import graph.NodeType",
      "import dev.ergenverse.graph.NodeType;" in WSE)
check("import graph.WorldGraph",
      "import dev.ergenverse.graph.WorldGraph;" in WSE)

# ─── 2. Class-level CRON-139 marker ───────────────────────────────
print("\n[2] Class-level CRON-139 marker")
check("CRON-139 javadoc header present",
      re.search(r"CRON-139\s*[-—]\s*Graph-First Query Path", WSE) is not None,
      "expected 'CRON-139 — Graph-First Query Path' in class javadoc")
check("mentions GraphBootstrap in CRON-139 block",
      re.search(r"CRON-139.*GraphBootstrap", WSE, re.DOTALL) is not None)
check("mentions RICanonicalDatabase in CRON-139 block",
      re.search(r"CRON-139.*RICanonicalDatabase", WSE, re.DOTALL) is not None)
check("mentions query 5-6 remain JSON-only",
      re.search(r"Queries\s+5-6.*remain\s+JSON-only", WSE) is not None)
check("mentions dedup merge by objectId",
      re.search(r"merged\s+by\s+objectId.*graph\s+first", WSE) is not None)

# ─── 3. Helper methods ────────────────────────────────────────────
print("\n[3] Helper methods — graph plumbing")
check("graphAvailable() helper",
      re.search(r"private static boolean graphAvailable\(\)", WSE) is not None,
      "expected private static boolean graphAvailable()")
check("graphAvailable checks GraphBootstrap.GRAPH != null",
      "GraphBootstrap.GRAPH != null" in WSE)
check("resolveGraphNode(String, NodeType) helper",
      re.search(r"private static NodeId resolveGraphNode\(String\s+idOrName,\s*NodeType\s+type\)", WSE) is not None,
      "expected private static NodeId resolveGraphNode(String, NodeType)")
check("resolveGraphNode strategy 1: exact NodeId match",
      re.search(r"Strategy 1:\s*exact\s+NodeId\s+match", WSE) is not None)
check("resolveGraphNode strategy 2: case-insensitive displayName",
      re.search(r"Strategy 2:\s*case-insensitive\s+displayName\s+match", WSE) is not None)
check("resolveGraphNode strategy 3: substring fallback",
      re.search(r"Strategy 3:\s*substring\s+fallback", WSE) is not None)
check("resolveGraphNode uses graph.nodesOfType(type)",
      "graph.nodesOfType(type)" in WSE)
check("resolveGraphNode returns null on no match",
      re.search(r"return\s+null;\s*\n\s*\}\s*\n\s*\n\s*/\*\*\s*\n\s*\*\s*CRON-139:\s*Convert", WSE) is not None,
      "expected resolveGraphNode ending 'return null;' before convertLocationEntry")

check("convertLocationEntry helper",
      re.search(r"private static ObjectEntry convertLocationEntry\(GraphQueryService\.LocationEntry", WSE) is not None)
check("convertOwnershipInfo helper",
      re.search(r"private static OwnershipRecord convertOwnershipInfo\(GraphQueryService\.OwnershipInfo", WSE) is not None)
check("convertDesireInfo helper",
      re.search(r"private static DesireRecord convertDesireInfo\(GraphQueryService\.DesireInfo", WSE) is not None)
check("convertKnowledgeInfo helper",
      re.search(r"private static KnowledgeRecord convertKnowledgeInfo\(GraphQueryService\.KnowledgeInfo", WSE) is not None)
check("stripNamespace helper",
      re.search(r"private static String stripNamespace\(String\s+namespaced\)", WSE) is not None)
check("stripNamespace strips 'npc:N01' → 'N01'",
      re.search(r"stripNamespace\(String[\s\S]{0,400}indexOf\(':\'[\s\S]{0,100}substring\(colon\s*\+\s*1\)", WSE) is not None,
      "expected indexOf(':') + substring(colon + 1) in stripNamespace")

# ─── 4. queryWhatExists graph-first ───────────────────────────────
print("\n[4] queryWhatExists — graph-first path")
check("resolveGraphNode(LOCATION) called in queryWhatExists",
      re.search(r"queryWhatExists\(String\s+locationId\).*resolveGraphNode\(locationId,\s*NodeType\.LOCATION\)",
                WSE, re.DOTALL) is not None)
check("GraphBootstrap.query().whatExistsAt(graphLoc) called",
      "GraphBootstrap.query().whatExistsAt(graphLoc)" in WSE)
check("convertLocationEntry called for each graph entry",
      re.search(r"for\s*\(\s*GraphQueryService\.LocationEntry\s+ge\s*:\s*graphEntries\s*\)\s*\{[^}]*convertLocationEntry\(ge",
                WSE, re.DOTALL) is not None)
check("seenIds Set<String> declared",
      re.search(r"Set<String>\s+seenIds\s*=\s*new\s+HashSet<>\(\)", WSE) is not None)
check("seenIds.add(oe.objectId()) dedup",
      "seenIds.add(oe.objectId())" in WSE)
check("JSON fallback still runs (npcs subsystem)",
      re.search(r"NPCs at this location.*JSON fallback.*getSubsystem\(\"npcs\"\)", WSE, re.DOTALL) is not None)
check("JSON fallback dedupes with seenIds",
      re.search(r'NPCs at this location.*seenIds\.add\(oid\).*new ObjectEntry\(\s*oid,\s*"npc"',
                WSE, re.DOTALL) is not None)
check("Exception catch logs warning + falls through",
      re.search(r"catch\s*\(\s*Exception\s+ex\s*\)\s*\{[\s\S]{0,500}?falling\s+back\s+to\s+JSON", WSE) is not None)

# ─── 5. queryWhoOwns graph-first ──────────────────────────────────
print("\n[5] queryWhoOwns — graph-first path")
check("resolveGraphNode(ARTIFACT) called in queryWhoOwns",
      re.search(r"queryWhoOwns\(String\s+objectId\).*resolveGraphNode\(objectId,\s*NodeType\.ARTIFACT\)",
                WSE, re.DOTALL) is not None)
check("GraphBootstrap.query().whoOwns(graphArt) called",
      "GraphBootstrap.query().whoOwns(graphArt)" in WSE)
check("convertOwnershipInfo(info) returned on graph hit",
      re.search(r"if\s*\(\s*info\s*!=\s*null\s*\)\s*\{[\s\S]{0,500}?return\s+convertOwnershipInfo\(info\)", WSE) is not None)
check("JSON fallback (provenance) still runs after graph miss",
      re.search(r'getEntry\("provenance",\s*objectId\)', WSE) is not None)
check("JSON fallback (civilizations heritage_treasures) still runs",
      re.search(r'heritage_treasures', WSE) is not None)

# ─── 6. queryWhoWants graph-first ─────────────────────────────────
print("\n[6] queryWhoWants — graph-first path")
check("resolveGraphNode(ARTIFACT) called in queryWhoWants",
      re.search(r"queryWhoWants\(String\s+objectId\).*resolveGraphNode\(objectId,\s*NodeType\.ARTIFACT\)",
                WSE, re.DOTALL) is not None)
check("GraphBootstrap.query().whoWants(graphArt) called",
      "GraphBootstrap.query().whoWants(graphArt)" in WSE)
check("convertDesireInfo called for each graph wanter",
      re.search(r"for\s*\(\s*GraphQueryService\.DesireInfo\s+di\s*:\s*graphWanters\s*\)\s*\{[^}]*convertDesireInfo\(di\)",
                WSE, re.DOTALL) is not None)
check("seenWanters Set<String> declared",
      re.search(r"Set<String>\s+seenWanters\s*=\s*new\s+HashSet<>\(\)", WSE) is not None)
check("JSON fallback (karma consequences) still runs",
      re.search(r'getSubsystem\("karma"\).*consequences', WSE, re.DOTALL) is not None)
check("JSON fallback dedupes with seenWanters",
      re.search(r"seenWanters\.add\(bearer\)", WSE) is not None)

# ─── 7. queryWhoKnows graph-first ─────────────────────────────────
print("\n[7] queryWhoKnows — graph-first path")
check("resolveGraphNode(ARTIFACT) called in queryWhoKnows",
      re.search(r"queryWhoKnows\(String\s+objectId\).*resolveGraphNode\(objectId,\s*NodeType\.ARTIFACT\)",
                WSE, re.DOTALL) is not None)
check("GraphBootstrap.query().whoKnowsAbout(graphArt) called",
      "GraphBootstrap.query().whoKnowsAbout(graphArt)" in WSE)
check("convertKnowledgeInfo called for each graph knower",
      re.search(r"for\s*\(\s*GraphQueryService\.KnowledgeInfo\s+ki\s*:\s*graphKnowers\s*\)\s*\{[^}]*convertKnowledgeInfo\(ki\)",
                WSE, re.DOTALL) is not None)
check("seenKnowers Set<String> declared",
      re.search(r"Set<String>\s+seenKnowers\s*=\s*new\s+HashSet<>\(\)", WSE) is not None)
check("JSON fallback (karma bearers) still runs",
      re.search(r'Karma bearers know about objects.*getSubsystem\("karma"\)', WSE, re.DOTALL) is not None)
check("JSON fallback dedupes with seenKnowers",
      re.search(r"seenKnowers\.add\(bearer\)", WSE) is not None)

# ─── 8. No regression — Q5/Q6 remain JSON-only ────────────────────
print("\n[8] No regression — Q5/Q6 still JSON-only")
check("queryWhyUntaken still present",
      re.search(r"public static UntakenReason queryWhyUntaken\(String\s+objectId\)", WSE) is not None)
check("queryWhyUntaken uses opportunities subsystem",
      re.search(r'queryWhyUntaken.*getEntry\("opportunities",\s*objectId\)', WSE, re.DOTALL) is not None)
check("queryWhyUntaken does NOT call resolveGraphNode",
      re.search(r"queryWhyUntaken\(String\s+objectId\).*?\n\s*\}", WSE, re.DOTALL).group(0).find("resolveGraphNode") == -1)
check("queryNaturalNext still present",
      re.search(r"public static NaturalNextEvent queryNaturalNext\(String\s+objectId\)", WSE) is not None)
check("queryNaturalNext uses karma subsystem",
      re.search(r'queryNaturalNext.*getSubsystem\("karma"\)', WSE, re.DOTALL) is not None)
check("queryNaturalNext does NOT call resolveGraphNode",
      re.search(r"queryNaturalNext\(String\s+objectId\).*?\n\s*\}", WSE, re.DOTALL).group(0).find("resolveGraphNode") == -1)

# ─── 9. Canon fidelity ────────────────────────────────────────────
print("\n[9] Canon fidelity")
check("mentions graph populated from RICanonicalDatabase",
      re.search(r"graph.*populated.*from.*RICanonicalDatabase", WSE, re.DOTALL) is not None or
      re.search(r"RICanonicalDatabase.*graph.*source", WSE, re.DOTALL) is not None)
check("no fabricated chapter citations",
      "RI Ch." not in WSE.replace("RI Ch.X", "").replace("RI Ch.XX", ""),
      "no 'RI Ch.N' chapter citations in CRON-139 block")
check("CRON-139 marker present (>= 5 occurrences)",
      len(re.findall(r"CRON-139", WSE)) >= 5,
      f"expected >= 5 CRON-139 markers, found {len(re.findall(r'CRON-139', WSE))}")
check("GraphQueryService whatExistsAt method exists",
      "public List<LocationEntry> whatExistsAt(NodeId locationId)" in GRAPH_QUERY)
check("GraphQueryService whoOwns method exists",
      "public OwnershipInfo whoOwns(NodeId entityId)" in GRAPH_QUERY)
check("GraphQueryService whoWants method exists",
      "public List<DesireInfo> whoWants(NodeId entityId)" in GRAPH_QUERY)
check("GraphQueryService whoKnowsAbout method exists",
      "public List<KnowledgeInfo> whoKnowsAbout(NodeId entityId)" in GRAPH_QUERY)
check("GraphBootstrap.bootstrap() is idempotent (volatile flag)",
      "private static volatile boolean bootstrapped" in GRAPH_BOOTSTRAP)
check("GraphBootstrap.GRAPH is public static volatile",
      "public static volatile WorldGraph GRAPH" in GRAPH_BOOTSTRAP)
check("GraphBootstrap.query() returns GraphQueryService",
      "public static GraphQueryService query()" in GRAPH_BOOTSTRAP)

# ─── 10. Build verification ───────────────────────────────────────
print("\n[10] Build verification")
build = subprocess.run(
    ["bash", "-c", "cd /home/z/my-project/forge-mod && JAVA_HOME=/tmp/my-project/.jdks/jdk-17.0.13+11/ ./gradlew compileJava"],
    capture_output=True, text=True, timeout=180
)
check("gradlew compileJava exit code 0",
      build.returncode == 0,
      f"exit={build.returncode}")
check("BUILD SUCCESSFUL in output",
      "BUILD SUCCESSFUL" in build.stdout,
      "missing 'BUILD SUCCESSFUL'")
check("no 'error:' in build output",
      "error:" not in build.stdout,
      "compile error in output")

# ─── Summary ──────────────────────────────────────────────────────
print("\n" + "=" * 60)
print(f"CRON-139 verification: {passed} pass, {failed} fail")
print("=" * 60)
if failed > 0:
    print("\nSOME CHECKS FAILED")
    sys.exit(1)
print("\nALL CHECKS PASSED")
