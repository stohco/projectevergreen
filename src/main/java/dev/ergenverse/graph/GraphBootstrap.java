package dev.ergenverse.graph;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.wanglin.RICanonicalDatabase;
import dev.ergenverse.wanglin.RICanonicalDatabase.CanonCharacter;
import dev.ergenverse.wanglin.RICanonicalDatabase.CanonLocation;
import dev.ergenverse.wanglin.RICanonicalDatabase.CanonArtifact;
import dev.ergenverse.wanglin.RICanonicalDatabase.CanonTechnique;
import dev.ergenverse.wanglin.RICanonicalDatabase.RelationShip;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * GraphBootstrap — populates the {@link WorldGraph} from Layer-1 canon data.
 *
 * <p><b>CRON-137.</b> The WorldGraph infrastructure (Node, Edge, NodeId, EdgeType,
 * WorldGraph) has existed since CRON-120 but was never populated or wired into
 * the simulation. This class is the bridge: it reads {@link RICanonicalDatabase}
 * (158 characters, 80 locations, 178 artifacts, 214 techniques) and creates
 * graph nodes + edges that {@link GraphQueryService} can traverse.
 *
 * <h2>What gets populated</h2>
 * <ul>
 *   <li><b>NPC nodes</b> — one per {@code CanonCharacter} (id="N01" → {@code npc:N01}).</li>
 *   <li><b>LOCATION nodes</b> — one per {@code CanonLocation} (id="L01" → {@code location:L01}).</li>
 *   <li><b>ARTIFACT nodes</b> — one per {@code CanonArtifact} (id="I01" → {@code artifact:I01}).</li>
 *   <li><b>TECHNIQUE nodes</b> — one per {@code CanonTechnique} (id="T01" → {@code technique:T01}).</li>
 *   <li><b>Social edges</b> — from {@code CanonCharacter.relationships}: love_interest→LOVES,
 *       family→FAMILY_OF, master→MENTORED_BY, enemy→ENEMY_OF, disciple→DISCIPLE_OF,
 *       rival→RIVALS, ally→ALLY_OF, friend→FAMILIAR_WITH.</li>
 *   <li><b>Ownership edges</b> — from {@code CanonArtifact.currentOwner}: OWNS edge
 *       from the owner NPC to the artifact.</li>
 *   <li><b>Spatial edges</b> — from {@code CanonLocation.parentLocation}: LOCATED_IN edge
 *       from the child location to the parent location.</li>
 * </ul>
 *
 * <h2>Name resolution</h2>
 * <p>Canon data uses free-text NAMES for relationship targets, artifact owners, and
 * parent locations — not IDs. This class builds a case-insensitive name→CanonCharacter
 * and name→CanonLocation map at bootstrap time to resolve these references. Unresolved
 * names are logged as warnings and the edge is skipped (canon-fidelity: do not invent
 * edges to non-existent entities).
 *
 * <h2>Graph holder</h2>
 * <p>The graph is held as {@link #GRAPH} — a static field on this class. This is the
 * single integration point: consumers call {@code GraphBootstrap.GRAPH} to get the
 * graph, and {@link GraphQueryService#query()} to get the query service.
 *
 * <h2>Canon fidelity</h2>
 * <p>All nodes are created via {@link Node#canon(NodeId, String, String, int)} with
 * the canon confidence from the source data. All edges are created via
 * {@link Edge#canon(NodeId, NodeId, EdgeType, String, int)} with confidence from
 * the source. No fabricated data — every node and edge traces back to a
 * {@link RICanonicalDatabase} entry.
 */
public final class GraphBootstrap {

    /** The single shared WorldGraph instance, populated at bootstrap. */
    public static volatile WorldGraph GRAPH;

    /** The query service wrapping {@link #GRAPH}. Null until bootstrap completes. */
    private static volatile GraphQueryService QUERY_SERVICE;

    private static volatile boolean bootstrapped = false;

    private GraphBootstrap() {}

    /**
     * Populate the graph from Layer-1 canon data. Idempotent — safe to call
     * multiple times (subsequent calls are no-ops).
     */
    public static synchronized void bootstrap() {
        if (bootstrapped) {
            Ergenverse.LOGGER.info("[GraphBootstrap] Already bootstrapped — skipping.");
            return;
        }
        WorldGraph graph = new WorldGraph();
        long startTime = System.currentTimeMillis();

        // Build name→entity lookup maps for relationship/owner resolution.
        Map<String, CanonCharacter> charByName = buildCharacterNameMap();
        Map<String, CanonLocation> locByName = buildLocationNameMap();

        int npcCount = populateCharacters(graph, charByName);
        int locCount = populateLocations(graph, locByName);
        int artCount = populateArtifacts(graph, charByName);
        int techCount = populateTechniques(graph);

        int socialEdges = populateSocialEdges(graph, charByName);
        int ownsEdges = populateOwnershipEdges(graph, charByName);
        int spatialEdges = populateSpatialEdges(graph, locByName);

        GRAPH = graph;
        QUERY_SERVICE = new GraphQueryService(graph);
        bootstrapped = true;

        long elapsed = System.currentTimeMillis() - startTime;
        Ergenverse.LOGGER.info("[GraphBootstrap] Populated WorldGraph in {}ms: {} nodes ({} NPC, {} LOC, {} ART, {} TECH), {} edges ({} social, {} owns, {} spatial)",
                elapsed,
                graph.nodeCount(),
                npcCount, locCount, artCount, techCount,
                graph.edgeCount(),
                socialEdges, ownsEdges, spatialEdges);
    }

    /**
     * Get the query service for the bootstrapped graph. Throws if bootstrap
     * has not been called yet.
     */
    public static GraphQueryService query() {
        if (QUERY_SERVICE == null) {
            throw new IllegalStateException("GraphBootstrap.bootstrap() has not been called yet");
        }
        return QUERY_SERVICE;
    }

    // ──────────────────────────────────────────────────────────────────
    // Node population
    // ──────────────────────────────────────────────────────────────────

    private static int populateCharacters(WorldGraph graph, Map<String, CanonCharacter> charByName) {
        int count = 0;
        for (CanonCharacter c : RICanonicalDatabase.ALL_CHARACTERS) {
            NodeId id = new NodeId(c.id, NodeType.NPC);
            Node node = Node.canon(id, c.name, c.id, c.canonConfidence);
            graph.addNode(node);
            charByName.put(normalizeName(c.name), c);
            if (c.nameCn != null && !c.nameCn.isEmpty()) {
                charByName.put(normalizeName(c.nameCn), c);
            }
            count++;
        }
        return count;
    }

    private static int populateLocations(WorldGraph graph, Map<String, CanonLocation> locByName) {
        int count = 0;
        for (CanonLocation l : RICanonicalDatabase.ALL_LOCATIONS) {
            NodeId id = new NodeId(l.id, NodeType.LOCATION);
            Node node = Node.canon(id, l.name, l.id, l.canonConfidence);
            graph.addNode(node);
            locByName.put(normalizeName(l.name), l);
            if (l.nameCn != null && !l.nameCn.isEmpty()) {
                locByName.put(normalizeName(l.nameCn), l);
            }
            count++;
        }
        return count;
    }

    private static int populateArtifacts(WorldGraph graph, Map<String, CanonCharacter> charByName) {
        int count = 0;
        for (CanonArtifact a : RICanonicalDatabase.ALL_ARTIFACTS) {
            NodeId id = new NodeId(a.id, NodeType.ARTIFACT);
            Node node = Node.canon(id, a.name, a.id, a.canonConfidence);
            graph.addNode(node);
            count++;
        }
        return count;
    }

    private static int populateTechniques(WorldGraph graph) {
        int count = 0;
        for (CanonTechnique t : RICanonicalDatabase.ALL_TECHNIQUES) {
            NodeId id = new NodeId(t.id, NodeType.TECHNIQUE);
            Node node = Node.canon(id, t.name, t.id, t.canonConfidence);
            graph.addNode(node);
            count++;
        }
        return count;
    }

    // ──────────────────────────────────────────────────────────────────
    // Edge population
    // ──────────────────────────────────────────────────────────────────

    private static int populateSocialEdges(WorldGraph graph, Map<String, CanonCharacter> charByName) {
        int count = 0;
        for (CanonCharacter c : RICanonicalDatabase.ALL_CHARACTERS) {
            if (c.relationships == null) continue;
            NodeId fromId = new NodeId(c.id, NodeType.NPC);
            for (RelationShip rel : c.relationships) {
                CanonCharacter target = charByName.get(normalizeName(rel.target));
                if (target == null) {
                    Ergenverse.LOGGER.debug("[GraphBootstrap] Unresolved relationship target '{}' for {}", rel.target, c.name);
                    continue;
                }
                EdgeType edgeType = mapRelationToEdgeType(rel.relation);
                if (edgeType == null) {
                    Ergenverse.LOGGER.debug("[GraphBootstrap] Unmapped relation type '{}' for {}", rel.relation, c.name);
                    continue;
                }
                NodeId toId = new NodeId(target.id, NodeType.NPC);
                if (graph.addCanonEdge(fromId, toId, edgeType, c.source, c.canonConfidence)) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int populateOwnershipEdges(WorldGraph graph, Map<String, CanonCharacter> charByName) {
        int count = 0;
        for (CanonArtifact a : RICanonicalDatabase.ALL_ARTIFACTS) {
            if (a.currentOwner == null || a.currentOwner.isEmpty()) continue;
            CanonCharacter owner = charByName.get(normalizeName(a.currentOwner));
            if (owner == null) {
                Ergenverse.LOGGER.debug("[GraphBootstrap] Unresolved artifact owner '{}' for {}", a.currentOwner, a.name);
                continue;
            }
            NodeId fromId = new NodeId(owner.id, NodeType.NPC);
            NodeId toId = new NodeId(a.id, NodeType.ARTIFACT);
            if (graph.addCanonEdge(fromId, toId, EdgeType.OWNS, a.source, a.canonConfidence)) {
                count++;
            }
        }
        return count;
    }

    private static int populateSpatialEdges(WorldGraph graph, Map<String, CanonLocation> locByName) {
        int count = 0;
        for (CanonLocation l : RICanonicalDatabase.ALL_LOCATIONS) {
            if (l.parentLocation == null || l.parentLocation.isEmpty()) continue;
            CanonLocation parent = locByName.get(normalizeName(l.parentLocation));
            if (parent == null) {
                // Try by ID directly
                if (l.parentLocation.matches("L\\d+")) {
                    NodeId fromId = new NodeId(l.id, NodeType.LOCATION);
                    NodeId toId = new NodeId(l.parentLocation, NodeType.LOCATION);
                    if (graph.addCanonEdge(fromId, toId, EdgeType.LOCATED_IN, l.source, l.canonConfidence)) {
                        count++;
                    }
                }
                continue;
            }
            NodeId fromId = new NodeId(l.id, NodeType.LOCATION);
            NodeId toId = new NodeId(parent.id, NodeType.LOCATION);
            if (graph.addCanonEdge(fromId, toId, EdgeType.LOCATED_IN, l.source, l.canonConfidence)) {
                count++;
            }
        }
        return count;
    }

    // ──────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────

    private static Map<String, CanonCharacter> buildCharacterNameMap() {
        Map<String, CanonCharacter> map = new HashMap<>();
        for (CanonCharacter c : RICanonicalDatabase.ALL_CHARACTERS) {
            map.put(normalizeName(c.name), c);
            if (c.nameCn != null && !c.nameCn.isEmpty()) {
                map.put(normalizeName(c.nameCn), c);
            }
        }
        return map;
    }

    private static Map<String, CanonLocation> buildLocationNameMap() {
        Map<String, CanonLocation> map = new HashMap<>();
        for (CanonLocation l : RICanonicalDatabase.ALL_LOCATIONS) {
            map.put(normalizeName(l.name), l);
            if (l.nameCn != null && !l.nameCn.isEmpty()) {
                map.put(normalizeName(l.nameCn), l);
            }
        }
        return map;
    }

    /**
     * Normalize a name for case-insensitive lookup: lowercase, trim, collapse spaces.
     */
    private static String normalizeName(String name) {
        if (name == null) return "";
        return name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    /**
     * Map a free-text relation string from CanonCharacter.relationships to an EdgeType.
     * Returns null if the relation type is unrecognized.
     */
    private static EdgeType mapRelationToEdgeType(String relation) {
        if (relation == null) return null;
        String r = relation.trim().toLowerCase(Locale.ROOT);
        return switch (r) {
            case "love_interest", "lover", "spouse", "wife", "husband" -> EdgeType.LOVES;
            case "family", "parent", "child", "sibling", "brother", "sister", "father", "mother", "son", "daughter" -> EdgeType.FAMILY_OF;
            case "master", "teacher" -> EdgeType.MENTORED_BY;
            case "disciple", "student" -> EdgeType.DISCIPLE_OF;
            case "enemy", "nemesis" -> EdgeType.ENEMY_OF;
            case "rival" -> EdgeType.RIVALS;
            case "ally", "companion", "friend" -> EdgeType.ALLY_OF;
            case "familiar", "acquaintance" -> EdgeType.FAMILIAR_WITH;
            case "saves", "saved_by" -> EdgeType.SAVED;
            case "serves" -> EdgeType.SERVES;
            case "betrays", "betrayed_by" -> EdgeType.BETRAYED;
            case "fears" -> EdgeType.FEARS;
            case "respects" -> EdgeType.RESPECTS;
            case "sworn_sibling" -> EdgeType.SWORN_SIBLING;
            case "dao_companion" -> EdgeType.DAO_COMPANION;
            default -> null;
        };
    }
}
