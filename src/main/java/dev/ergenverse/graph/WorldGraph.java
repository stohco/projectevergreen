package dev.ergenverse.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * <h1>WorldGraph — the living knowledge graph</h1>
 *
 * <p>Layer 2 of the Ergenverse three-layer architecture: a mutable, directed,
 * typed property graph recording every entity and every relationship in the
 * simulated world. Sits alongside (and references) the Layer 1 immutable
 * canon registry in {@code dev.ergenverse.wanglin.registry}.
 *
 * <h2>What the graph stores</h2>
 * <ul>
 *   <li><b>Nodes</b> — keyed by {@link NodeId}. Each is an immutable value
 *       object carrying zero or more typed {@link Component}s (cultivation
 *       state, inventory, karmic profile, qi-fortune, etc.). Mutation =
 *       replace the node via {@link #replaceNode}.</li>
 *   <li><b>Edges</b> — directed, typed by {@link EdgeType}, carrying
 *       provenance, timestamp, and weight. Stored as bidirectional adjacency
 *       indexes for fast traversal in either direction.</li>
 * </ul>
 *
 * <h2>The integration point for engines and solvers</h2>
 * <p>Every engine and every Active State Solver reads from and writes to
 * this graph. The graph is the <b>single integration point</b> — engines
 * don't call each other directly; they read each other's state via the
 * graph. This means:
 * <ul>
 *   <li>Adding a new engine = adding new node/edge types + components</li>
 *   <li>Adding a new solver = writing a graph-walker that reads existing edges</li>
 *   <li>Per-object provenance = path query (FOLLOW edges of given types)</li>
 *   <li>NPC AI state = a component read off the NPC's node</li>
 * </ul>
 *
 * <h2>Solver-facing traversal APIs</h2>
 * <p>The Active State Solvers each need to walk the graph in a specific way.
 * The graph provides:
 * <ul>
 *   <li>{@link #outEdges(NodeId, EdgeType)} / {@link #inEdges(NodeId, EdgeType)} —
 *       direct adjacency lookup (O(1) on the index)</li>
 *   <li>{@link #walk(NodeId, Set, int, boolean, boolean)} — bounded-depth BFS
 *       over edges of given types. Used by the Karmic Resonance Solver to
 *       walk depth 3-4 of GRUDGE/KARMIC_DEBT edges.</li>
 *   <li>{@link #sumWeights(NodeId, Set, int, boolean, boolean)} — sum the
 *       weights of all matching edges within a depth. Used to compute total
 *       karmic burden, net qi-fortune, total cosmic debt.</li>
 *   <li>{@link #path(NodeId, NodeId, Set, int)} — find a path between two
 *       nodes using only given edge types. Used by Provenance queries.</li>
 *   <li>{@link #subgraph(Set, Set)} — extract all nodes/edges matching the
 *       given type filters. Used by the per-object provenance UI.</li>
 *   <li>{@link #neighbors(NodeId, EdgeType, boolean)} — direct neighbors
 *       via a single edge type. Used by fast lookup paths.</li>
 * </ul>
 *
 * <h2>Immutability contract</h2>
 * <p>Nodes are immutable value objects (per the pre-existing {@link Node}
 * design). To modify a node, call {@link #replaceNode} with a new Node
 * built via {@link Node#toBuilder()} (package-private) or
 * {@link Node.Builder}. The graph swaps the old node for the new one
 * atomically. Edge mutations (add only — edges are never removed, only
 * superseded by terminal edges like KARMA_RESOLVED) are append-only.
 *
 * <h2>Cause-effect &amp; the Prime Directive</h2>
 * <p>The graph records what <b>is</b>. It does not fabricate. Every edge
 * carries a {@link EdgeProvenance}. Simulation-generated edges are marked
 * SIMULATION, not CANON. The graph is honest about the difference between
 * "Er Gen wrote this" and "the simulation created this."
 *
 * @see Node the (immutable) entities
 * @see Edge the (append-only) relationships
 * @see EdgeType the relationship vocabulary (15 families, 100+ types)
 * @see Component the typed data slots on nodes
 */
public final class WorldGraph {

    // ─── Node storage (mutable map of immutable nodes) ─────────────
    private final Map<NodeId, Node> nodes = new ConcurrentHashMap<>();

    // ─── Edge adjacency indexes (bidirectional) ────────────────────
    private final Map<NodeId, Map<EdgeType, List<Edge>>> outEdges = new ConcurrentHashMap<>();
    private final Map<NodeId, Map<EdgeType, List<Edge>>> inEdges = new ConcurrentHashMap<>();

    // ─── History log (Layer 3 — append-only provenance) ────────────
    private final List<Edge> historyLog = Collections.synchronizedList(new ArrayList<>());

    // ════════════════════════════════════════════════════════════════
    //  NODE API
    // ════════════════════════════════════════════════════════════════

    /**
     * Add a node to the graph. Returns false if a node with the same id
     * already exists (use {@link #replaceNode} to update).
     */
    public boolean addNode(Node node) {
        return nodes.putIfAbsent(node.id(), node) == null;
    }

    /**
     * Atomically replace an existing node with a new (modified) version.
     * The new node must have the same id as the old one. Used by engines
     * to update component state: read node → toBuilder → modify → replace.
     *
     * @return the previous node, or null if no node with that id existed
     */
    public Node replaceNode(Node newNode) {
        NodeId id = newNode.id();
        if (!nodes.containsKey(id)) return null;
        return nodes.put(id, newNode);
    }

    /** Get a node by id, or empty if absent. */
    public Optional<Node> getNode(NodeId id) {
        return Optional.ofNullable(nodes.get(id));
    }

    /**
     * Get a node by id, or throw if absent.
     * Use when the caller's contract requires the node to exist.
     */
    public Node requireNode(NodeId id) {
        Node n = nodes.get(id);
        if (n == null) {
            throw new IllegalStateException("WorldGraph has no node " + id);
        }
        return n;
    }

    /** Whether the graph contains a node with the given id. */
    public boolean hasNode(NodeId id) {
        return nodes.containsKey(id);
    }

    /** All nodes in the graph (unmodifiable view). */
    public Collection<Node> nodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    /** All nodes of a given type. */
    public List<Node> nodesOfType(NodeType type) {
        List<Node> result = new ArrayList<>();
        for (Node n : nodes.values()) {
            if (n.id().type() == type) result.add(n);
        }
        return result;
    }

    /** Total node count. */
    public int nodeCount() { return nodes.size(); }

    // ════════════════════════════════════════════════════════════════
    //  EDGE API
    // ════════════════════════════════════════════════════════════════

    /**
     * Add an edge to the graph. Both endpoints must already exist as nodes.
     * The edge is appended to the history log (Layer 3). Edges are never
     * removed — they're superseded by terminal edges (e.g. KARMA_RESOLVED).
     *
     * @return true if the edge was added; false if either endpoint is missing
     */
    public boolean addEdge(Edge edge) {
        if (!nodes.containsKey(edge.from())) return false;
        if (!nodes.containsKey(edge.to())) return false;
        outEdges.computeIfAbsent(edge.from(), k -> new HashMap<>())
                .computeIfAbsent(edge.type(), k -> new ArrayList<>())
                .add(edge);
        inEdges.computeIfAbsent(edge.to(), k -> new HashMap<>())
                .computeIfAbsent(edge.type(), k -> new ArrayList<>())
                .add(edge);
        historyLog.add(edge);
        return true;
    }

    /** Convenience: add a canon edge. Endpoints must exist. */
    public boolean addCanonEdge(NodeId from, NodeId to, EdgeType type, String source, int confidence) {
        return addEdge(Edge.canon(from, to, type, source, confidence));
    }

    /** Convenience: add a canon edge with a chapter reference. */
    public boolean addCanonChEdge(NodeId from, NodeId to, EdgeType type, String chapter) {
        return addEdge(Edge.canonCh(from, to, type, chapter));
    }

    /** Convenience: add a simulation edge. Endpoints must exist. */
    public boolean addSimEdge(NodeId from, NodeId to, EdgeType type, long tick) {
        return addEdge(Edge.sim(from, to, type, tick));
    }

    /** Convenience: add a weighted simulation edge. */
    public boolean addSimEdge(NodeId from, NodeId to, EdgeType type, long tick, double weight) {
        return addEdge(Edge.sim(from, to, type, tick, weight));
    }

    /** Convenience: add a bootstrap (canon-derived structural) edge. */
    public boolean addBootstrapEdge(NodeId from, NodeId to, EdgeType type, String source) {
        return addEdge(Edge.bootstrap(from, to, type, source));
    }

    /**
     * All outgoing edges of a specific type from a node.
     * Returns an empty list if the node has no such edges.
     */
    public List<Edge> outEdges(NodeId from, EdgeType type) {
        Map<EdgeType, List<Edge>> byType = outEdges.get(from);
        if (byType == null) return List.of();
        List<Edge> edges = byType.get(type);
        return edges == null ? List.of() : Collections.unmodifiableList(edges);
    }

    /**
     * All incoming edges of a specific type to a node.
     */
    public List<Edge> inEdges(NodeId to, EdgeType type) {
        Map<EdgeType, List<Edge>> byType = inEdges.get(to);
        if (byType == null) return List.of();
        List<Edge> edges = byType.get(type);
        return edges == null ? List.of() : Collections.unmodifiableList(edges);
    }

    /**
     * All outgoing edges of any of the given types from a node.
     * Used by solvers that consume a family of edge types (e.g. the Karmic
     * Resonance Solver asks for all karmic-family outgoing edges).
     */
    public List<Edge> outEdges(NodeId from, Set<EdgeType> types) {
        List<Edge> result = new ArrayList<>();
        Map<EdgeType, List<Edge>> byType = outEdges.get(from);
        if (byType == null) return result;
        for (EdgeType t : types) {
            List<Edge> edges = byType.get(t);
            if (edges != null) result.addAll(edges);
        }
        return result;
    }

    /** All incoming edges of any of the given types to a node. */
    public List<Edge> inEdges(NodeId to, Set<EdgeType> types) {
        List<Edge> result = new ArrayList<>();
        Map<EdgeType, List<Edge>> byType = inEdges.get(to);
        if (byType == null) return result;
        for (EdgeType t : types) {
            List<Edge> edges = byType.get(t);
            if (edges != null) result.addAll(edges);
        }
        return result;
    }

    /** All outgoing edges (any type) from a node. */
    public List<Edge> allOutEdges(NodeId from) {
        List<Edge> result = new ArrayList<>();
        Map<EdgeType, List<Edge>> byType = outEdges.get(from);
        if (byType == null) return result;
        for (List<Edge> edges : byType.values()) result.addAll(edges);
        return result;
    }

    /** All incoming edges (any type) to a node. */
    public List<Edge> allInEdges(NodeId to) {
        List<Edge> result = new ArrayList<>();
        Map<EdgeType, List<Edge>> byType = inEdges.get(to);
        if (byType == null) return result;
        for (List<Edge> edges : byType.values()) result.addAll(edges);
        return result;
    }

    /**
     * The set of node ids directly reachable from {@code from} via edges of
     * the given type. Convenience for fast neighbor lookup.
     *
     * @param from     the source node
     * @param type     the edge type to follow
     * @param outgoing if true, follow outgoing edges; if false, follow incoming
     */
    public Set<NodeId> neighbors(NodeId from, EdgeType type, boolean outgoing) {
        Set<NodeId> result = new LinkedHashSet<>();
        List<Edge> edges = outgoing ? outEdges(from, type) : inEdges(from, type);
        for (Edge e : edges) {
            result.add(outgoing ? e.to() : e.from());
        }
        return result;
    }

    /** Total edge count (each edge counted once). */
    public int edgeCount() { return historyLog.size(); }

    // ════════════════════════════════════════════════════════════════
    //  SOLVER-FACING TRAVERSAL APIs
    //  These are the methods the Active State Solvers call.
    // ════════════════════════════════════════════════════════════════

    /**
     * <h2>Bounded-depth graph walk</h2>
     *
     * <p>Walk the graph from a starting node, following edges whose type is
     * in {@code edgeTypes}, up to a maximum {@code depth}. Returns the set
     * of all nodes reachable (excluding the start node itself).
     *
     * <p>This is the primary API for:
     * <ul>
     *   <li><b>Karmic Resonance Solver</b> — walks depth 3-4 of GRUDGE /
     *       KARMIC_DEBT edges to compute total karmic burden.</li>
     *   <li><b>Cause-Effect Weaver</b> — walks CAUSE_OF / CONSEQUENCE_OF
     *       edges to find ripple effects of an action.</li>
     *   <li><b>Heart Demon Resolver</b> — walks PSYCHIC_WOUND_FROM /
     *       HEART_DEMON_FROM edges to enumerate unresolved wounds.</li>
     * </ul>
     *
     * @param start      the starting node id
     * @param edgeTypes  the set of edge types to follow (others ignored)
     * @param maxDepth   the maximum traversal depth (1 = direct neighbors)
     * @param followOut  if true, follow outgoing edges
     * @param followIn   if true, follow incoming edges
     * @return the set of reachable node ids (unmodifiable; excludes start)
     */
    public Set<NodeId> walk(NodeId start, Set<EdgeType> edgeTypes, int maxDepth,
                             boolean followOut, boolean followIn) {
        Set<NodeId> visited = new LinkedHashSet<>();
        if (maxDepth <= 0 || !nodes.containsKey(start)) return visited;
        List<NodeId> frontier = new ArrayList<>();
        frontier.add(start);
        visited.add(start);
        for (int depth = 0; depth < maxDepth && !frontier.isEmpty(); depth++) {
            List<NodeId> nextFrontier = new ArrayList<>();
            for (NodeId current : frontier) {
                if (followOut) {
                    for (Edge e : outEdges(current, edgeTypes)) {
                        if (visited.add(e.to())) nextFrontier.add(e.to());
                    }
                }
                if (followIn) {
                    for (Edge e : inEdges(current, edgeTypes)) {
                        if (visited.add(e.from())) nextFrontier.add(e.from());
                    }
                }
            }
            frontier = nextFrontier;
        }
        visited.remove(start);
        return Collections.unmodifiableSet(visited);
    }

    /**
     * <h2>Sum of edge weights within a bounded walk</h2>
     *
     * <p>Walk the graph from a starting node, following edges of the given
     * types up to {@code maxDepth}, and sum the {@link Edge#weight()} of
     * every edge traversed. Used by:
     * <ul>
     *   <li><b>Karmic Resonance Solver</b> — total outstanding karmic burden
     *       (sum of KARMIC_DEBT + GRUDGE weights, minus KARMIC_CREDIT +
     *       GRATITUDE, minus KARMA_RESOLVED).</li>
     *   <li><b>Qiyun Flux Engine</b> — net qi-fortune (stolen + inherited +
     *       granted − burned).</li>
     *   <li><b>Cause-Effect Weaver</b> — total cosmic debt registered against
     *       the entity by the Heavenly Dao.</li>
     * </ul>
     *
     * <p>For the karmic-burden use case, callers should call this twice:
     * once with the "negative" edge types (debts/grudges) and once with
     * the "positive" types (credits/gratitude), then subtract.
     *
     * @param start      the starting node id
     * @param edgeTypes  the set of edge types to follow
     * @param maxDepth   the maximum traversal depth
     * @param followOut  if true, follow outgoing edges (debts the start OWES)
     * @param followIn   if true, follow incoming edges (debts OWED TO the start)
     * @return the sum of weights (never negative — clamped to 0)
     */
    public double sumWeights(NodeId start, Set<EdgeType> edgeTypes, int maxDepth,
                              boolean followOut, boolean followIn) {
        double sum = 0;
        Set<NodeId> visited = new HashSet<>();
        visited.add(start);
        List<NodeId> frontier = new ArrayList<>();
        frontier.add(start);
        for (int depth = 0; depth < maxDepth && !frontier.isEmpty(); depth++) {
            List<NodeId> nextFrontier = new ArrayList<>();
            for (NodeId current : frontier) {
                if (followOut) {
                    for (Edge e : outEdges(current, edgeTypes)) {
                        sum += e.weight();
                        if (visited.add(e.to())) nextFrontier.add(e.to());
                    }
                }
                if (followIn) {
                    for (Edge e : inEdges(current, edgeTypes)) {
                        sum += e.weight();
                        if (visited.add(e.from())) nextFrontier.add(e.from());
                    }
                }
            }
            frontier = nextFrontier;
        }
        return Math.max(0, sum);
    }

    /**
     * <h2>Find a path between two nodes</h2>
     *
     * <p>BFS path-finding restricted to edges of the given types. Returns
     * the list of edges forming the shortest path, or empty if no path
     * exists within {@code maxDepth} hops.
     *
     * <p>Used by the per-object provenance UI: "show me the life of this
     * sword" = path query following FORGED_BY → USED_IN → TAKEN_BY →
     * SOLD_TO → LOST_IN → RECOVERED_BY edges from the sword node.
     *
     * @param from       the starting node
     * @param to         the target node
     * @param edgeTypes  the set of edge types allowed in the path
     * @param maxDepth   the maximum path length
     * @return the list of edges forming the path, or empty if no path
     */
    public List<Edge> path(NodeId from, NodeId to, Set<EdgeType> edgeTypes, int maxDepth) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) return List.of();
        if (from.equals(to)) return List.of();
        Map<NodeId, Edge> cameFrom = new HashMap<>();
        Set<NodeId> visited = new HashSet<>();
        List<NodeId> frontier = new ArrayList<>();
        frontier.add(from);
        visited.add(from);
        boolean found = false;
        for (int depth = 0; depth < maxDepth && !frontier.isEmpty() && !found; depth++) {
            List<NodeId> nextFrontier = new ArrayList<>();
            for (NodeId current : frontier) {
                for (Edge e : outEdges(current, edgeTypes)) {
                    if (visited.add(e.to())) {
                        cameFrom.put(e.to(), e);
                        if (e.to().equals(to)) { found = true; break; }
                        nextFrontier.add(e.to());
                    }
                }
                if (found) break;
                for (Edge e : inEdges(current, edgeTypes)) {
                    if (visited.add(e.from())) {
                        cameFrom.put(e.from(), e);
                        if (e.from().equals(to)) { found = true; break; }
                        nextFrontier.add(e.from());
                    }
                }
                if (found) break;
            }
            frontier = nextFrontier;
        }
        if (!found) return List.of();
        List<Edge> path = new ArrayList<>();
        NodeId cursor = to;
        while (!cursor.equals(from)) {
            Edge e = cameFrom.get(cursor);
            if (e == null) break;
            path.add(0, e);
            cursor = e.from().equals(cursor) ? e.to() : e.from();
        }
        return Collections.unmodifiableList(path);
    }

    /**
     * <h2>Extract a subgraph</h2>
     *
     * <p>Return all nodes whose type is in {@code nodeTypes} and all edges
     * whose type is in {@code edgeTypes}, restricted to the subgraph induced
     * by those nodes. Used by the per-object provenance UI to render a
     * focused view of, e.g., "all artifacts and their ownership history".
     *
     * @param nodeTypes the node types to include (null = all)
     * @param edgeTypes the edge types to include (null = all)
     * @return a 2-element list: [List&lt;Node&gt;, List&lt;Edge&gt;]
     */
    public List<Object> subgraph(Set<NodeType> nodeTypes, Set<EdgeType> edgeTypes) {
        List<Node> subNodes = new ArrayList<>();
        Set<NodeId> subNodeIds = new HashSet<>();
        for (Node n : nodes.values()) {
            if (nodeTypes == null || nodeTypes.contains(n.id().type())) {
                subNodes.add(n);
                subNodeIds.add(n.id());
            }
        }
        List<Edge> subEdges = new ArrayList<>();
        synchronized (historyLog) {
            for (Edge e : historyLog) {
                if (edgeTypes != null && !edgeTypes.contains(e.type())) continue;
                if (subNodeIds.contains(e.from()) && subNodeIds.contains(e.to())) {
                    subEdges.add(e);
                }
            }
        }
        return List.of(subNodes, subEdges);
    }

    /**
     * Find all nodes matching a predicate. Used by engines that need to
     * query "all cultivators above Soul Formation" or "all sects with at
     * least 100 members" or "all NPCs with unresolved karmic debt".
     */
    public List<Node> findNodes(Predicate<Node> predicate) {
        List<Node> result = new ArrayList<>();
        for (Node n : nodes.values()) {
            if (predicate.test(n)) result.add(n);
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════
    //  HISTORY LOG (Layer 3 — append-only provenance)
    // ════════════════════════════════════════════════════════════════

    /**
     * The full append-only history log. Every edge ever added to the graph
     * (including ones later "resolved" by terminal edges) appears here in
     * insertion order. This is the canonical record for Layer 3 provenance.
     *
     * <p>The per-object provenance UI reads this log filtered by node id.
     */
    public List<Edge> history() {
        synchronized (historyLog) {
            return Collections.unmodifiableList(new ArrayList<>(historyLog));
        }
    }

    /** All history edges involving a given node (in either direction). */
    public List<Edge> historyOf(NodeId node) {
        List<Edge> result = new ArrayList<>();
        synchronized (historyLog) {
            for (Edge e : historyLog) {
                if (e.from().equals(node) || e.to().equals(node)) result.add(e);
            }
        }
        return result;
    }

    /** All history edges of specific types involving a node. */
    public List<Edge> historyOf(NodeId node, Set<EdgeType> types) {
        List<Edge> result = new ArrayList<>();
        synchronized (historyLog) {
            for (Edge e : historyLog) {
                if (!types.contains(e.type())) continue;
                if (e.from().equals(node) || e.to().equals(node)) result.add(e);
            }
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════
    //  SERIALIZATION (world save / load)
    //  Nodes are immutable value objects without their own NBT methods;
    //  the graph serializes their identity + component class names. Full
    //  component NBT is delegated to each component class's Codec (TBD
    //  as engines register their components).
    // ════════════════════════════════════════════════════════════════

    /**
     * Serialize the graph's structural state (nodes + edges) to NBT.
     * Component payloads are serialized via their registered Codec (future
     * engine work). For now, this captures identity + edges, enough to
     * reconstruct topology on load.
     */
    public net.minecraft.nbt.CompoundTag serialize() {
        var tag = new net.minecraft.nbt.CompoundTag();
        tag.putInt("nodeCount", nodes.size());
        tag.putInt("edgeCount", historyLog.size());

        var nodesTag = new net.minecraft.nbt.ListTag();
        for (Node n : nodes.values()) {
            var nTag = new net.minecraft.nbt.CompoundTag();
            nTag.putString("id", n.id().namespaced());
            nTag.putString("displayName", n.displayName());
            if (n.registryId() != null) nTag.putString("registryId", n.registryId());
            nTag.putInt("canonConfidence", n.canonConfidence());
            // Component payloads: serialized via registered Codecs (future).
            // For now we record the component class names so the loader
            // knows which components were present.
            var compsTag = new net.minecraft.nbt.ListTag();
            for (Class<?> c : n.componentTypes()) {
                compsTag.add(net.minecraft.nbt.StringTag.valueOf(c.getName()));
            }
            nTag.put("componentClasses", compsTag);
            nodesTag.add(nTag);
        }
        tag.put("nodes", nodesTag);

        var edgesTag = new net.minecraft.nbt.ListTag();
        synchronized (historyLog) {
            for (Edge e : historyLog) {
                var eTag = new net.minecraft.nbt.CompoundTag();
                eTag.putString("from", e.from().namespaced());
                eTag.putString("to", e.to().namespaced());
                eTag.putString("type", e.type().name());
                eTag.putString("provenanceSource", e.provenance().source().name());
                eTag.putString("provenanceRef", e.provenance().reference());
                eTag.putInt("provenanceConfidence", e.provenance().confidence());
                eTag.putLong("timestamp", e.timestamp());
                eTag.putDouble("weight", e.weight());
                edgesTag.add(eTag);
            }
        }
        tag.put("edges", edgesTag);
        return tag;
    }

    /**
     * Deserialize the graph from NBT. Clears existing state first.
     * Note: full component reconstruction requires registered Codecs (future
     * engine work); for now this restores topology (nodes + edges) only.
     */
    public void deserialize(net.minecraft.nbt.CompoundTag tag) {
        nodes.clear();
        outEdges.clear();
        inEdges.clear();
        historyLog.clear();

        var nodesTag = tag.getList("nodes", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < nodesTag.size(); i++) {
            var nTag = nodesTag.getCompound(i);
            NodeId id = NodeId.parse(nTag.getString("id"));
            String displayName = nTag.getString("displayName");
            String registryId = nTag.contains("registryId") ? nTag.getString("registryId") : null;
            int confidence = nTag.getInt("canonConfidence");
            Node node = registryId != null
                    ? Node.canon(id, displayName, registryId, confidence)
                    : Node.generated(id, displayName);
            nodes.put(id, node);
        }

        var edgesTag = tag.getList("edges", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < edgesTag.size(); i++) {
            var eTag = edgesTag.getCompound(i);
            NodeId from = NodeId.parse(eTag.getString("from"));
            NodeId to = NodeId.parse(eTag.getString("to"));
            EdgeType type;
            try {
                type = EdgeType.valueOf(eTag.getString("type"));
            } catch (IllegalArgumentException ex) {
                continue; // unknown type in save (newer mod version) — skip
            }
            var source = EdgeProvenance.Source.valueOf(eTag.getString("provenanceSource"));
            String ref = eTag.getString("provenanceRef");
            int conf = eTag.getInt("provenanceConfidence");
            long ts = eTag.getLong("timestamp");
            double w = eTag.getDouble("weight");
            Edge edge = new Edge(from, to, type, new EdgeProvenance(source, ref, conf), ts, w, Map.of());
            if (nodes.containsKey(from) && nodes.containsKey(to)) {
                outEdges.computeIfAbsent(from, k -> new HashMap<>())
                        .computeIfAbsent(type, k -> new ArrayList<>())
                        .add(edge);
                inEdges.computeIfAbsent(to, k -> new HashMap<>())
                        .computeIfAbsent(type, k -> new ArrayList<>())
                        .add(edge);
                historyLog.add(edge);
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  DIAGNOSTICS
    // ════════════════════════════════════════════════════════════════

    /** A one-line summary of graph size, for debug overlays. */
    public String summarize() {
        return "WorldGraph{nodes=" + nodes.size() + ", edges=" + historyLog.size() + "}";
    }

    @Override
    public String toString() { return summarize(); }
}
