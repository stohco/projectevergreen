package dev.ergenverse.graph;

/**
 * A typed, directed edge in the WorldGraph.
 *
 * <p>Every edge carries a type (what the relationship IS), provenance
 * (where we know it from), and an optional timestamp and weight. Edges
 * are the mechanism by which all engines communicate.
 *
 * <h2>Three kinds of edges</h2>
 * <ul>
 *   <li><b>Canon edges</b> — attested by the novels. E.g. "Wang Lin OWNS
 *       Heaven-Defying Bead" (RI Ch. 8). These are loaded at boot from
 *       the canon database and never modified.</li>
 *   <li><b>Simulation edges</b> — created by engine logic during gameplay.
 *       E.g. "Player KILLS Beast_42" (simulation event). These are mutable
 *       and form the Layer 3 history.</li>
 *   <li><b>Bootstrap edges</b> — structural edges created to bootstrap the
 *       world. E.g. "Heng Yue Sect LOCATED_IN Zhao Mountains" (canon
 *       geography). These are canon-derived but not directly novel-attested
 *       as a relationship — they're inferred from narrative context.</li>
 * </ul>
 *
 * <h2>Edge as history event</h2>
 * <p>An edge with a timestamp IS a history event. The provenance of a sword
 * is just a path query: FOLLOW the FORGED_BY → USED_IN → TAKEN_BY → SOLD_TO
 * → LOST_IN → RECOVERED_BY edges from the sword node. Each edge is one event
 * in the sword's life.
 *
 * @param from       the source node ID
 * @param to         the target node ID
 * @param type       the relationship type
 * @param provenance where this edge comes from
 * @param timestamp  when this edge was created (0 = unknown/at boot, game ticks during play)
 * @param weight     optional weight (relationship strength, trade volume, etc.)
 * @param properties optional key-value properties for engine-specific data
 */
public record Edge(
        NodeId from,
        NodeId to,
        EdgeType type,
        EdgeProvenance provenance,
        long timestamp,
        double weight,
        java.util.Map<String, String> properties
) {
    public Edge {
        if (from == null) throw new IllegalArgumentException("Edge requires a from NodeId");
        if (to == null) throw new IllegalArgumentException("Edge requires a to NodeId");
        if (type == null) throw new IllegalArgumentException("Edge requires an EdgeType");
        if (provenance == null) provenance = EdgeProvenance.SIMULATION;
        if (properties == null) properties = java.util.Map.of();
    }

    /** Convenience: create a canon edge with no timestamp or weight. */
    public static Edge canon(NodeId from, NodeId to, EdgeType type, String source, int confidence) {
        return new Edge(from, to, type,
                new EdgeProvenance(EdgeProvenance.Source.CANON, source, confidence),
                0, 0, java.util.Map.of());
    }

    /** Convenience: create a canon edge from a chapter reference. */
    public static Edge canonCh(NodeId from, NodeId to, EdgeType type, String chapter) {
        return new Edge(from, to, type,
                new EdgeProvenance(EdgeProvenance.Source.CANON, chapter, 5),
                0, 0, java.util.Map.of());
    }

    /** Convenience: create a simulation edge (from engine logic). */
    public static Edge sim(NodeId from, NodeId to, EdgeType type, long tick) {
        return new Edge(from, to, type, EdgeProvenance.SIMULATION, tick, 0, java.util.Map.of());
    }

    /** Convenience: create a simulation edge with a weight. */
    public static Edge sim(NodeId from, NodeId to, EdgeType type, long tick, double weight) {
        return new Edge(from, to, type, EdgeProvenance.SIMULATION, tick, weight, java.util.Map.of());
    }

    /** Convenience: create a bootstrap edge (canon-derived structural relationship). */
    public static Edge bootstrap(NodeId from, NodeId to, EdgeType type, String source) {
        return new Edge(from, to, type,
                new EdgeProvenance(EdgeProvenance.Source.BOOTSTRAP, source, 4),
                0, 0, java.util.Map.of());
    }

    /** Get a property value, or null if absent. */
    public String property(String key) {
        return properties.get(key);
    }

    /** Create a copy with an additional property. */
    public Edge withProperty(String key, String value) {
        var newProps = new java.util.LinkedHashMap<>(properties);
        newProps.put(key, value);
        return new Edge(from, to, type, provenance, timestamp, weight,
                java.util.Collections.unmodifiableMap(newProps));
    }
}