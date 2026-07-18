package dev.ergenverse.graph;

/**
 * NodeId — a typed, namespaced identifier for graph nodes.
 *
 * <p>The type discriminator prevents ID collisions between different
 * entity kinds. "wang_lin" as ARTIFACT is different from "wang_lin" as NPC.
 * In practice, the namespace prefix (e.g. "artifact:wang_lin", "npc:wang_lin")
 * is used in the string form, but the type is stored separately for fast
 * dispatch without parsing.
 *
 * <p>For canon entities, the id matches the CanonicalEntry id (e.g.
 * "heaven_defying_bead", "I09_dragon_formation"). For simulation-generated
 * entities, the id is a UUID or sequential ID.
 *
 * <p>This is a value type: two NodeIds are equal if and only if their
 * type and id strings are equal.
 */
public final class NodeId implements Comparable<NodeId> {

    private final String id;
    private final NodeType type;

    public NodeId(String id, NodeType type) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("NodeId requires a non-blank id");
        if (type == null) throw new IllegalArgumentException("NodeId requires a NodeType");
        this.id = id;
        this.type = type;
    }

    /** Parse a namespaced ID like "artifact:heaven_defying_bead" back into a NodeId. */
    public static NodeId parse(String namespaced) {
        if (namespaced == null) throw new IllegalArgumentException("Cannot parse null NodeId");
        int colon = namespaced.indexOf(':');
        if (colon < 0) {
            // No namespace — assume GENERIC
            return new NodeId(namespaced, NodeType.GENERIC);
        }
        String prefix = namespaced.substring(0, colon);
        String id = namespaced.substring(colon + 1);
        NodeType type = NodeType.fromPrefix(prefix);
        return new NodeId(id, type);
    }

    /** Create a NodeId for a canon entity (type inferred from the CanonicalCategory). */
    public static NodeId canon(String id, NodeType type) {
        return new NodeId(id, type);
    }

    /** Create a NodeId for a simulation-generated entity. */
    public static NodeId generated(String id, NodeType type) {
        return new NodeId(id, type);
    }

    /** The bare ID (without namespace prefix). */
    public String id() { return id; }

    /** The node type. */
    public NodeType type() { return type; }

    /** The namespaced form: "artifact:heaven_defying_bead". */
    public String namespaced() {
        return type.prefix() + ":" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeId other)) return false;
        return id.equals(other.id) && type == other.type;
    }

    @Override
    public int hashCode() {
        return 31 * id.hashCode() + type.hashCode();
    }

    @Override
    public String toString() {
        return namespaced();
    }

    @Override
    public int compareTo(NodeId other) {
        int tc = type.prefix().compareTo(other.type.prefix());
        if (tc != 0) return tc;
        return id.compareTo(other.id);
    }
}