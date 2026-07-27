#!/usr/bin/env python3
"""
CRON-137 verification script — WorldGraph integration (GraphBootstrap + GraphQueryService + RumorNetwork wiring).

Verifies that:
  1. GraphBootstrap.java exists and populates the graph from RICanonicalDatabase.
  2. GraphQueryService.java exists with all simulation-ready query methods.
  3. GraphBootstrap.bootstrap() is called in Ergenverse.java constructor.
  4. RumorNetwork uses GraphQueryService.socialContacts() instead of ActorRegistry.all() brute-force.
  5. Build succeeds (./gradlew compileJava exit code 0).

Exit code: 0 if all checks pass, 1 otherwise.
"""

import os
import re
import subprocess
import sys
from pathlib import Path

PROJECT_ROOT = Path("/home/z/my-project/forge-mod")
GRAPH_BOOTSTRAP = PROJECT_ROOT / "src/main/java/dev/ergenverse/graph/GraphBootstrap.java"
GRAPH_QUERY = PROJECT_ROOT / "src/main/java/dev/ergenverse/graph/GraphQueryService.java"
ERGENVERSE = PROJECT_ROOT / "src/main/java/dev/ergenverse/core/Ergenverse.java"
RUMOR_NETWORK = PROJECT_ROOT / "src/main/java/dev/ergenverse/npc/rumor/RumorNetwork.java"

PASS = 0
FAIL = 0
ERRORS = []


def check(name, condition, detail=""):
    global PASS, FAIL
    if condition:
        PASS += 1
        print(f"  PASS  {name}")
    else:
        FAIL += 1
        ERRORS.append(f"{name}: {detail}")
        print(f"  FAIL  {name}  {detail}")


def read(path):
    return path.read_text(encoding="utf-8")


# ──────────────────────────────────────────────────────────────────────────
# 1. GraphBootstrap.java
# ──────────────────────────────────────────────────────────────────────────
print("\n[1] GraphBootstrap.java")
bootstrap_src = read(GRAPH_BOOTSTRAP) if GRAPH_BOOTSTRAP.exists() else ""

check("GraphBootstrap.java exists", GRAPH_BOOTSTRAP.exists())
check("public final class GraphBootstrap", "public final class GraphBootstrap" in bootstrap_src)
check("GRAPH static field", "public static volatile WorldGraph GRAPH" in bootstrap_src)
check("bootstrap() method", "public static synchronized void bootstrap()" in bootstrap_src)
check("query() accessor", "public static GraphQueryService query()" in bootstrap_src)
check("CRON-137 marker", "CRON-137" in bootstrap_src)
check("populates from RICanonicalDatabase", "RICanonicalDatabase.ALL_CHARACTERS" in bootstrap_src)
check("populates locations", "RICanonicalDatabase.ALL_LOCATIONS" in bootstrap_src)
check("populates artifacts", "RICanonicalDatabase.ALL_ARTIFACTS" in bootstrap_src)
check("populates techniques", "RICanonicalDatabase.ALL_TECHNIQUES" in bootstrap_src)
check("creates NPC nodes", "NodeType.NPC" in bootstrap_src)
check("creates LOCATION nodes", "NodeType.LOCATION" in bootstrap_src)
check("creates ARTIFACT nodes", "NodeType.ARTIFACT" in bootstrap_src)
check("creates TECHNIQUE nodes", "NodeType.TECHNIQUE" in bootstrap_src)
check("creates social edges", "mapRelationToEdgeType" in bootstrap_src)
check("creates ownership edges (OWNS)", "EdgeType.OWNS" in bootstrap_src)
check("creates spatial edges (LOCATED_IN)", "EdgeType.LOCATED_IN" in bootstrap_src)
check("name resolution (normalizeName)", "normalizeName" in bootstrap_src)
check("idempotent bootstrap", "volatile boolean bootstrapped" in bootstrap_src)
check("canon fidelity marker", "canon" in bootstrap_src.lower())

# ──────────────────────────────────────────────────────────────────────────
# 2. GraphQueryService.java
# ──────────────────────────────────────────────────────────────────────────
print("\n[2] GraphQueryService.java")
query_src = read(GRAPH_QUERY) if GRAPH_QUERY.exists() else ""

check("GraphQueryService.java exists", GRAPH_QUERY.exists())
check("public final class GraphQueryService", "public final class GraphQueryService" in query_src)
check("CRON-137 marker", "CRON-137" in query_src)
check("socialContacts method", "public List<NodeId> socialContacts(NodeId npcId)" in query_src)
check("socialNetwork method", "public Set<NodeId> socialNetwork(NodeId npcId, int maxDepth)" in query_src)
check("settlementResidents method", "public List<NodeId> settlementResidents(NodeId settlementId)" in query_src)
check("locationContents method", "public List<NodeId> locationContents(NodeId locationId)" in query_src)
check("findOwner method", "public NodeId findOwner(NodeId entityId)" in query_src)
check("ownedEntities method", "public List<NodeId> ownedEntities(NodeId npcId)" in query_src)
check("netKarmicBurden method", "public int netKarmicBurden(NodeId npcId)" in query_src)
check("karmicConnections method", "public Set<NodeId> karmicConnections(NodeId npcId)" in query_src)
check("threatsNearSettlement method", "public List<NodeId> threatsNearSettlement(NodeId settlementId, int maxDepth)" in query_src)
check("whatExistsAt method", "public List<LocationEntry> whatExistsAt(NodeId locationId)" in query_src)
check("whoOwns method", "public OwnershipInfo whoOwns(NodeId entityId)" in query_src)
check("whoWants method", "public List<DesireInfo> whoWants(NodeId entityId)" in query_src)
check("whoKnowsAbout method", "public List<KnowledgeInfo> whoKnowsAbout(NodeId entityId)" in query_src)
check("describeNode method", "public String describeNode(NodeId nodeId)" in query_src)
check("graph() accessor", "public WorldGraph graph()" in query_src)
check("SOCIAL_EDGES set", "SOCIAL_EDGES" in query_src)
check("KARMIC_EDGES set", "KARMIC_EDGES" in query_src)
check("LocationEntry record", "public record LocationEntry" in query_src)
check("OwnershipInfo record", "public record OwnershipInfo" in query_src)
check("DesireInfo record", "public record DesireInfo" in query_src)
check("KnowledgeInfo record", "public record KnowledgeInfo" in query_src)
check("FAMILIAR_WITH in SOCIAL_EDGES", "EdgeType.FAMILIAR_WITH" in query_src)
check("ALLY_OF in SOCIAL_EDGES", "EdgeType.ALLY_OF" in query_src)
check("OWNS edge type used", "EdgeType.OWNS" in query_src)
check("LOCATED_IN edge type used", "EdgeType.LOCATED_IN" in query_src)

# ──────────────────────────────────────────────────────────────────────────
# 3. Ergenverse.java wiring
# ──────────────────────────────────────────────────────────────────────────
print("\n[3] Ergenverse.java wiring")
erg_src = read(ERGENVERSE)

check("GraphBootstrap.bootstrap() called", "GraphBootstrap.bootstrap()" in erg_src)
check("called after WangLinMasterRegistry.bootstrap()",
      erg_src.index("WangLinMasterRegistry.bootstrap()") < erg_src.index("GraphBootstrap.bootstrap()"),
      "GraphBootstrap should be called after WangLinMasterRegistry")
check("CRON-137 comment in Ergenverse", "CRON-137" in erg_src)

# ──────────────────────────────────────────────────────────────────────────
# 4. RumorNetwork graph integration
# ──────────────────────────────────────────────────────────────────────────
print("\n[4] RumorNetwork graph integration")
rumor_src = read(RUMOR_NETWORK)

check("GraphBootstrap import", "import dev.ergenverse.graph.GraphBootstrap" in rumor_src)
check("NodeId import", "import dev.ergenverse.graph.NodeId" in rumor_src)
check("NodeType import", "import dev.ergenverse.graph.NodeType" in rumor_src)
check("CRON-137 marker", "CRON-137" in rumor_src)
check("uses GraphBootstrap.GRAPH", "GraphBootstrap.GRAPH" in rumor_src)
check("uses GraphBootstrap.query()", "GraphBootstrap.query()" in rumor_src)
check("uses socialContacts()", "socialContacts" in rumor_src)
check("uses NodeId for source NPC", "new NodeId(sourceId, NodeType.NPC)" in rumor_src)
check("graph-first propagation comment", "graph-first propagation" in rumor_src.lower() or "Graph-first propagation" in rumor_src)
check("fallback to ActorRegistry.all()", "ActorRegistry.all()" in rumor_src,
      "should retain fallback for procedural NPCs")
check("social propagation vs spatial", "social" in rumor_src.lower() and "spatial" in rumor_src.lower())

# ──────────────────────────────────────────────────────────────────────────
# 5. Build verification
# ──────────────────────────────────────────────────────────────────────────
print("\n[5] Build verification")
env = os.environ.copy()
env["JAVA_HOME"] = "/tmp/my-project/.jdks/jdk-17.0.13+11/"
try:
    result = subprocess.run(
        ["./gradlew", "compileJava"],
        cwd=str(PROJECT_ROOT),
        capture_output=True,
        text=True,
        env=env,
        timeout=120,
    )
    build_output = result.stdout + result.stderr
    check("gradlew compileJava exit code 0", result.returncode == 0, f"exit code {result.returncode}")
    check("BUILD SUCCESSFUL in output", "BUILD SUCCESSFUL" in build_output, "expected 'BUILD SUCCESSFUL'")
    check("no 'error:' in build output", "error:" not in build_output, "found compilation error")
except subprocess.TimeoutExpired:
    check("gradlew compileJava exit code 0", False, "timed out after 120s")
    check("BUILD SUCCESSFUL in output", False, "timed out")
    check("no 'error:' in build output", False, "timed out")

# ──────────────────────────────────────────────────────────────────────────
# Summary
# ──────────────────────────────────────────────────────────────────────────
print(f"\n{'='*60}")
print(f"CRON-137 verification: {PASS} pass, {FAIL} fail")
print(f"{'='*60}")
if FAIL > 0:
    print("\nFAILURES:")
    for e in ERRORS:
        print(f"  - {e}")
    sys.exit(1)
else:
    print("\nALL CHECKS PASSED")
    sys.exit(0)
