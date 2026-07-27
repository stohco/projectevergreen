package dev.ergenverse.graph;

import dev.ergenverse.core.Ergenverse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * GraphQueryService — wraps {@link WorldGraph} traversal into simulation-ready queries.
 *
 * <p><b>CRON-137.</b> The WorldGraph has been populated by {@link GraphBootstrap} but
 * simulation consumers (RumorNetwork, WorldStateEngine, ActorMaterializer) need
 * higher-level queries — not raw edge traversal. This service provides those queries:
 * social contacts, settlement residents, ownership, karmic burden, threats, and the
 * WorldStateEngine Q1–Q4 equivalents (whatExistsAt, whoOwns, whoWants, whoKnowsAbout).
 *
 * <h2>Query categories</h2>
 * <ul>
 *   <li><b>Social</b> — {@link #socialContacts(NodeId)}, {@link #socialNetwork(NodeId, int)}:
 *       BFS walk of FAMILIAR_WITH, ALLY_OF, FAMILY_OF, MENTORED_BY, DAO_COMPANION,
 *       SWORN_SIBLING, SERVES, DISCIPLE_OF, SAVED, LOVES, RESPECTS edges.</li>
 *   <li><b>Spatial</b> — {@link #settlementResidents(NodeId)}, {@link #locationContents(NodeId)},
 *       {@link #whatExistsAt(NodeId)}: incoming LOCATED_IN edges.</li>
 *   <li><b>Ownership</b> — {@link #findOwner(NodeId)}, {@link #ownedEntities(NodeId)},
 *       {@link #whoOwns(NodeId)}: incoming/outgoing OWNS edges.</li>
 *   <li><b>Karmic</b> — {@link #netKarmicBurden(NodeId)}, {@link #karmicConnections(NodeId)},
 *       {@link #whoWants(NodeId)}: KARMIC_DEBT, GRUDGE, VENGEANCE_OBLIGATION, GRATITUDE,
 *       SAVED, KARMIC_THREAD edges.</li>
 *   <li><b>Threats</b> — {@link #threatsNearSettlement(NodeId, int)}: BEAST nodes via
 *       LOCATED_IN walk.</li>
 *   <li><b>Information</b> — {@link #whoKnowsAbout(NodeId)}: WITNESSED + FAMILIAR_WITH edges.</li>
 *   <li><b>Diagnostic</b> — {@link #describeNode(Node)}: human-readable summary.</li>
 * </ul>
 *
 * <h2>Canon fidelity</h2>
 * <p>All queries read from canon-populated edges (EdgeProvenance.CANON). Simulation
 * edges (EdgeProvenance.SIMULATION) are also traversed — the graph is a live structure
 * that grows as the simulation runs. No fabricated data — every result traces back to
 * a {@link dev.ergenverse.wanglin.RICanonicalDatabase} entry or a simulation event.
 *
 * <h2>Performance</h2>
 * <p>All queries are O(edges) — the graph uses ConcurrentHashMap-backed adjacency
 * lists, so edge lookups are O(1) and traversal is O(degree). The BFS walk in
 * {@link #socialNetwork(NodeId, int)} is O(nodes^depth) — keep depth ≤ 3 for real-time use.
 */
public final class GraphQueryService {

    private final WorldGraph graph;

    // Edge type sets for efficient lookup.
    private static final Set<EdgeType> SOCIAL_EDGES = Set.of(
            EdgeType.FAMILIAR_WITH, EdgeType.ALLY_OF, EdgeType.FAMILY_OF,
            EdgeType.MENTORED_BY, EdgeType.DAO_COMPANION, EdgeType.SWORN_SIBLING,
            EdgeType.SERVES, EdgeType.DISCIPLE_OF, EdgeType.SAVED,
            EdgeType.LOVES, EdgeType.RESPECTS
    );

    private static final Set<EdgeType> KARMIC_EDGES = Set.of(
            EdgeType.KARMIC_DEBT, EdgeType.GRUDGE, EdgeType.VENGEANCE_OBLIGATION,
            EdgeType.GRATITUDE, EdgeType.SAVED, EdgeType.KARMIC_THREAD,
            EdgeType.HATES, EdgeType.ENEMY_OF, EdgeType.HEAVENLY_ENMITY,
            EdgeType.BLOODLINE_OF, EdgeType.REINCARNATION_OF
    );

    private static final Set<EdgeType> NEGATIVE_KARMIC_EDGES = Set.of(
            EdgeType.KARMIC_DEBT, EdgeType.GRUDGE, EdgeType.VENGEANCE_OBLIGATION,
            EdgeType.HATES, EdgeType.ENEMY_OF, EdgeType.HEAVENLY_ENMITY
    );

    private static final Set<EdgeType> POSITIVE_KARMIC_EDGES = Set.of(
            EdgeType.GRATITUDE, EdgeType.SAVED, EdgeType.ALLY_OF,
            EdgeType.LOVES, EdgeType.FAMILY_OF, EdgeType.SWORN_SIBLING
    );

    private static final Set<EdgeType> DESIRE_EDGES = Set.of(
            EdgeType.KARMIC_DEBT, EdgeType.GRUDGE, EdgeType.VENGEANCE_OBLIGATION,
            EdgeType.HATES, EdgeType.ENEMY_OF
    );

    private static final Set<EdgeType> KNOWLEDGE_EDGES = Set.of(
            EdgeType.WITNESSED, EdgeType.FAMILIAR_WITH, EdgeType.PARTICIPATED_IN
    );

    GraphQueryService(WorldGraph graph) {
        this.graph = graph;
    }

    // ──────────────────────────────────────────────────────────────────
    // Social queries
    // ──────────────────────────────────────────────────────────────────

    /**
     * Get the direct social contacts of an NPC — NPCs connected by any social edge
     * (FAMILIAR_WITH, ALLY_OF, FAMILY_OF, MENTORED_BY, etc.).
     *
     * @param npcId the NPC node ID
     * @return list of NPC NodeIds that are direct social contacts (outgoing edges only)
     */
    public List<NodeId> socialContacts(NodeId npcId) {
        Set<NodeId> neighbors = graph.neighbors(npcId, null, true);
        // Filter to social edges only — neighbors() with null returns all edge types,
        // so we need to check each edge individually.
        List<NodeId> result = new ArrayList<>();
        for (Edge e : graph.allOutEdges(npcId)) {
            if (SOCIAL_EDGES.contains(e.type())) {
                result.add(e.to());
            }
        }
        return result;
    }

    /**
     * Get the social network of an NPC via BFS walk of social edges.
     *
     * @param npcId the starting NPC node ID
     * @param maxDepth maximum BFS depth (keep ≤ 3 for real-time use)
     * @return set of all reachable NPC NodeIds within maxDepth hops
     */
    public Set<NodeId> socialNetwork(NodeId npcId, int maxDepth) {
        return graph.walk(npcId, SOCIAL_EDGES, maxDepth, true, false);
    }

    // ──────────────────────────────────────────────────────────────────
    // Spatial queries
    // ──────────────────────────────────────────────────────────────────

    /**
     * Get all NPCs/entities that are LOCATED_IN a settlement or location.
     * This is the graph-backed equivalent of WorldStateEngine.queryWhatExists.
     *
     * @param locationId the location/settlement node ID
     * @return list of NodeIds that have a LOCATED_IN edge to this location
     */
    public List<NodeId> locationContents(NodeId locationId) {
        List<NodeId> result = new ArrayList<>();
        for (Edge e : graph.inEdges(locationId, EdgeType.LOCATED_IN)) {
            result.add(e.from());
        }
        return result;
    }

    /**
     * Alias for {@link #locationContents(NodeId)} — semantic clarity for settlement queries.
     */
    public List<NodeId> settlementResidents(NodeId settlementId) {
        return locationContents(settlementId);
    }

    /**
     * Graph-backed WorldStateEngine Query 1: what exists at a location?
     * Returns human-readable entries for all entities LOCATED_IN the location.
     */
    public List<LocationEntry> whatExistsAt(NodeId locationId) {
        List<LocationEntry> result = new ArrayList<>();
        for (Edge e : graph.inEdges(locationId, EdgeType.LOCATED_IN)) {
            Node node = graph.getNode(e.from()).orElse(null);
            if (node == null) continue;
            result.add(new LocationEntry(
                    e.from().namespaced(),
                    node.type().name().toLowerCase(),
                    node.displayName(),
                    0L,  // ageYears — not tracked in graph (would need a Component)
                    "canon"  // trueState — canon nodes are "canon"
            ));
        }
        return result;
    }

    // ──────────────────────────────────────────────────────────────────
    // Ownership queries
    // ──────────────────────────────────────────────────────────────────

    /**
     * Find the owner of an entity (artifact, technique, etc.) — the NPC that has
     * an outgoing OWNS edge to this entity.
     *
     * @param entityId the entity node ID (ARTIFACT, TECHNIQUE, etc.)
     * @return the owner's NodeId, or null if no owner edge exists
     */
    public NodeId findOwner(NodeId entityId) {
        List<Edge> ownsEdges = graph.inEdges(entityId, EdgeType.OWNS);
        if (ownsEdges.isEmpty()) return null;
        return ownsEdges.get(0).from();
    }

    /**
     * Get all entities owned by an NPC.
     *
     * @param npcId the owner NPC node ID
     * @return list of entity NodeIds that this NPC owns
     */
    public List<NodeId> ownedEntities(NodeId npcId) {
        List<NodeId> result = new ArrayList<>();
        for (Edge e : graph.outEdges(npcId, EdgeType.OWNS)) {
            result.add(e.to());
        }
        return result;
    }

    /**
     * Graph-backed WorldStateEngine Query 2: who owns this entity?
     */
    public OwnershipInfo whoOwns(NodeId entityId) {
        NodeId ownerId = findOwner(entityId);
        if (ownerId == null) return null;
        Node ownerNode = graph.getNode(ownerId).orElse(null);
        String ownerName = ownerNode != null ? ownerNode.displayName() : ownerId.id();
        String ownerType = ownerId.type().name().toLowerCase();
        return new OwnershipInfo(entityId.namespaced(), ownerId.namespaced(), ownerName, ownerType, 5, 10);
    }

    // ──────────────────────────────────────────────────────────────────
    // Karmic queries
    // ──────────────────────────────────────────────────────────────────

    /**
     * Calculate the net karmic burden of an NPC — the weighted sum of all karmic edges.
     * Negative karmic edges (GRUDGE, KARMIC_DEBT, VENGEANCE_OBLIGATION, HATES, ENEMY_OF)
     * contribute +1 per edge; positive karmic edges (GRATITUDE, SAVED, ALLY_OF) contribute -1.
     *
     * @param npcId the NPC node ID
     * @return net karmic burden (positive = more negative karma, negative = more positive karma)
     */
    public int netKarmicBurden(NodeId npcId) {
        int burden = 0;
        for (EdgeType t : NEGATIVE_KARMIC_EDGES) {
            burden += graph.outEdges(npcId, t).size();
            burden += graph.inEdges(npcId, t).size();
        }
        for (EdgeType t : POSITIVE_KARMIC_EDGES) {
            burden -= graph.outEdges(npcId, t).size();
            burden -= graph.inEdges(npcId, t).size();
        }
        return burden;
    }

    /**
     * Get all karmic connections of an NPC — NPCs connected by karmic edges.
     */
    public Set<NodeId> karmicConnections(NodeId npcId) {
        return graph.walk(npcId, KARMIC_EDGES, 1, true, true);
    }

    /**
     * Graph-backed WorldStateEngine Query 3: who wants this entity?
     * Returns NPCs with desire edges (KARMIC_DEBT, GRUDGE, VENGEANCE_OBLIGATION, HATES, ENEMY_OF)
     * pointing toward the entity or its owner.
     */
    public List<DesireInfo> whoWants(NodeId entityId) {
        List<DesireInfo> result = new ArrayList<>();
        // Direct desire edges to the entity
        for (EdgeType t : DESIRE_EDGES) {
            for (Edge e : graph.inEdges(entityId, t)) {
                Node wanterNode = graph.getNode(e.from()).orElse(null);
                String wanterName = wanterNode != null ? wanterNode.displayName() : e.from().id();
                result.add(new DesireInfo(
                        entityId.namespaced(),
                        e.from().namespaced(),
                        wanterName,
                        e.type().name().toLowerCase(),
                        5,
                        "karmic connection via " + e.type().name()
                ));
            }
        }
        // If no direct edges, check if someone wants the OWNER of this entity
        if (result.isEmpty()) {
            NodeId ownerId = findOwner(entityId);
            if (ownerId != null) {
                for (EdgeType t : DESIRE_EDGES) {
                    for (Edge e : graph.inEdges(ownerId, t)) {
                        Node wanterNode = graph.getNode(e.from()).orElse(null);
                        String wanterName = wanterNode != null ? wanterNode.displayName() : e.from().id();
                        result.add(new DesireInfo(
                                entityId.namespaced(),
                                e.from().namespaced(),
                                wanterName,
                                e.type().name().toLowerCase(),
                                3,
                                "wants owner " + ownerId.namespaced() + " via " + e.type().name()
                        ));
                    }
                }
            }
        }
        return result;
    }

    // ──────────────────────────────────────────────────────────────────
    // Threat queries
    // ──────────────────────────────────────────────────────────────────

    /**
     * Find BEAST nodes that are located in or near a settlement.
     * Uses a LOCATED_IN walk to find beasts within the settlement's location subtree.
     *
     * @param settlementId the settlement/location node ID
     * @param maxDepth maximum depth of the location subtree walk (1 = direct, 2 = child locations)
     * @return list of BEAST NodeIds found near the settlement
     */
    public List<NodeId> threatsNearSettlement(NodeId settlementId, int maxDepth) {
        List<NodeId> result = new ArrayList<>();
        // Walk the location subtree to find all child locations
        Set<NodeId> locationSubtree = graph.walk(settlementId,
                Set.of(EdgeType.LOCATED_IN), maxDepth, false, true);
        locationSubtree.add(settlementId);
        // For each location in the subtree, find BEASTs located there
        for (NodeId locId : locationSubtree) {
            for (Edge e : graph.inEdges(locId, EdgeType.LOCATED_IN)) {
                if (e.from().type() == NodeType.BEAST) {
                    result.add(e.from());
                }
            }
        }
        return result;
    }

    // ──────────────────────────────────────────────────────────────────
    // Information queries
    // ──────────────────────────────────────────────────────────────────

    /**
     * Graph-backed WorldStateEngine Query 4: who knows about this entity?
     * Returns NPCs with WITNESSED or FAMILIAR_WITH edges to the entity.
     */
    public List<KnowledgeInfo> whoKnowsAbout(NodeId entityId) {
        List<KnowledgeInfo> result = new ArrayList<>();
        for (EdgeType t : KNOWLEDGE_EDGES) {
            for (Edge e : graph.inEdges(entityId, t)) {
                Node knowerNode = graph.getNode(e.from()).orElse(null);
                String knowerName = knowerNode != null ? knowerNode.displayName() : e.from().id();
                int tier = e.type() == EdgeType.WITNESSED ? 1 : 2;  // witnessed = direct knowledge
                int accuracy = (int) Math.max(1, e.weight() * 10);
                result.add(new KnowledgeInfo(
                        entityId.namespaced(),
                        e.from().namespaced(),
                        knowerName,
                        tier,
                        accuracy
                ));
            }
        }
        return result;
    }

    // ──────────────────────────────────────────────────────────────────
    // Diagnostic queries
    // ──────────────────────────────────────────────────────────────────

    /**
     * Produce a human-readable summary of a node — its type, name, and edge counts.
     */
    public String describeNode(NodeId nodeId) {
        Node node = graph.getNode(nodeId).orElse(null);
        if (node == null) return "Node " + nodeId.namespaced() + " not found";
        int outCount = graph.allOutEdges(nodeId).size();
        int inCount = graph.allInEdges(nodeId).size();
        return String.format("%s [%s] — %s (canon confidence %d, %d out-edges, %d in-edges)",
                node.displayName(),
                node.type().name(),
                node.registryId() != null ? "registry:" + node.registryId() : "generated",
                node.canonConfidence(),
                outCount,
                inCount);
    }

    /**
     * Get the underlying graph (for advanced queries not covered by this service).
     */
    public WorldGraph graph() {
        return graph;
    }

    // ──────────────────────────────────────────────────────────────────
    // Result records (graph-backed equivalents of WorldStateEngine records)
    // ──────────────────────────────────────────────────────────────────

    /** Graph-backed equivalent of WorldStateEngine.ObjectEntry. */
    public record LocationEntry(
            String objectId,
            String objectType,
            String displayName,
            long ageYears,
            String trueState
    ) {}

    /** Graph-backed equivalent of WorldStateEngine.OwnershipRecord. */
    public record OwnershipInfo(
            String entityId,
            String ownerId,
            String ownerName,
            String ownerType,
            int ownerStrength,
            int claimStrength
    ) {}

    /** Graph-backed equivalent of WorldStateEngine.DesireRecord. */
    public record DesireInfo(
            String entityId,
            String wanterId,
            String wanterName,
            String desireType,
            int desireStrength,
            String desireReason
    ) {}

    /** Graph-backed equivalent of WorldStateEngine.KnowledgeRecord. */
    public record KnowledgeInfo(
            String entityId,
            String knowerId,
            String knowerName,
            int knowledgeTier,
            int knowledgeAccuracy
    ) {}
}
