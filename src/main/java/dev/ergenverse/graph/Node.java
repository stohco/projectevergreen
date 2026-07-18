package dev.ergenverse.graph;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A node in the Ergenverse WorldGraph.
 *
 * <p>A node is the union of:
 * <ul>
 *   <li><b>Identity:</b> a typed NodeId (e.g. "artifact:heaven_defying_bead")</li>
 *   <li><b>Canon anchor:</b> optional back-link to a Layer 1 CanonicalEntry</li>
 *   <li><b>Confidence:</b> 0-5 canon confidence rating (from the bridging policy)</li>
 *   <li><b>Components:</b> a typed map of Component instances — all engine state lives here</li>
 * </ul>
 *
 * <h3>Component Examples by NodeType</h3>
 * <table>
 *   <tr><th>NodeType</th><th>Typical Components</th></tr>
 *   <tr><td>NPC</td><td>CultivationState, SoulMatrix, DaoProfile, RealInventory, PerceptionState, QiyunState, SamsaraState</td></tr>
 *   <tr><td>ARTIFACT</td><td>ArtifactStats, SoulBrand, EvolutionChain, CompositionPart</td></tr>
 *   <tr><td>TECHNIQUE</td><td>TechniqueStats, EvolutionChain</td></tr>
 *   <tr><td>LOCATION</td><td>RealityProfile, WorldLawData, JossFlameFlow</td></tr>
 *   <tr><td>BEAST</td><td>CultivationState, SoulMatrix, Composition, TamedState</td></tr>
 *   <tr><td>PLANT</td><td>Composition, HerbStats</td></tr>
 *   <tr><td>FACTION</td><td>FactionData, Treasury</td></tr>
 *   <tr><td>ESSENCE</td><td>EssenceData, DaoCompetition</td></tr>
 *   <tr><td>CAVE_WORLD</td><td>RealityProfile, WorldExitData, JossFlameFlow, DimensionalManaPool</td></tr>
 * </table>
 *
 * <h3>Immutability</h3>
 * <p>Nodes are immutable. To modify a node, call {@link #withComponent} or
 * {@link #withoutComponent} to create a new node with the change. The graph
 * then replaces the old node with the new one.
 *
 * @see Component  the typed data interface for node data
 * @see WorldGraph  the graph that contains these nodes
 */
public final class Node {

    private final NodeId id;
    private final String displayName;
    private final String registryId;     // Layer 1 back-link (null for sim-generated)
    private final int canonConfidence;   // 0-5
    private final Map<Class<? extends Component<?>>, Component<?>> components;

    private Node(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Node requires an id");
        this.displayName = builder.displayName != null ? builder.displayName : builder.id.id();
        this.registryId = builder.registryId;
        this.canonConfidence = Math.max(0, Math.min(5, builder.canonConfidence));
        this.components = Collections.unmodifiableMap(new LinkedHashMap<>(builder.components));
    }

    /** The node's typed identifier. */
    public NodeId id() { return id; }

    /** Human-readable name. Defaults to the id's bare string if not set. */
    public String displayName() { return displayName; }

    /**
     * Back-link to the Layer 1 CanonicalEntry registry.
     * Null for simulation-generated nodes.
     * When set, engines can look up full canon provenance, ownership, and transferability.
     */
    public String registryId() { return registryId; }

    /** Whether this node has a Layer 1 canon entry. */
    public boolean isCanon() { return registryId != null; }

    /** Canon confidence: 0=generated filler, 5=direct novel statement. */
    public int canonConfidence() { return canonConfidence; }

    /** The node's type (convenience for id().type()). */
    public NodeType type() { return id.type(); }

    /** Get all component types currently attached to this node. */
    public Set<Class<? extends Component<?>>> componentTypes() {
        return components.keySet();
    }

    /** Check if this node has a component of the given type. */
    public boolean hasComponent(Class<? extends Component<?>> type) {
        return components.containsKey(type);
    }

    /**
     * Get a component by type. Returns null if not present.
     * Type-safe access pattern:
     * <pre>
     *   CultivationStateComponent cult = node.getComponent(CultivationStateComponent.class);
     *   if (cult != null) { ... }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public <C extends Component<C>> C getComponent(Class<C> type) {
        return (C) components.get(type);
    }

    /**
     * Require a component — throws if absent.
     * Use when the engine assumes the component must be present.
     */
    public <C extends Component<C>> C requireComponent(Class<C> type) {
        C c = getComponent(type);
        if (c == null) {
            throw new IllegalStateException("Node " + id + " requires component "
                    + type.getSimpleName() + " but it is absent");
        }
        return c;
    }

    /**
     * Create a copy of this node with an added/replaced component.
     * The component is copied (defensive copy) before storage.
     */
    @SuppressWarnings("unchecked")
    public Node withComponent(Component<?> component) {
        Component<?> stored = component.copy();
        Builder b = toBuilder();
        b.components.put((Class<? extends Component<?>>) stored.getClass(), stored);
        return b.build();
    }

    /**
     * Create a copy of this node with a component removed.
     */
    public Node withoutComponent(Class<? extends Component<?>> type) {
        Builder b = toBuilder();
        b.components.remove(type);
        return b.build();
    }

    /** Create a copy with a different display name. */
    public Node withDisplayName(String name) {
        return toBuilder().displayName(name).build();
    }

    /** Create a copy with a different canon confidence. */
    public Node withCanonConfidence(int confidence) {
        return toBuilder().canonConfidence(confidence).build();
    }

    private Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .displayName(this.displayName)
                .registryId(this.registryId)
                .canonConfidence(this.canonConfidence)
                .components(new LinkedHashMap<>(this.components));
    }

    /** Create a new canon node (references a Layer 1 entry). */
    public static Node canon(NodeId id, String displayName, String registryId, int confidence) {
        return new Builder()
                .id(id)
                .displayName(displayName)
                .registryId(registryId)
                .canonConfidence(confidence)
                .build();
    }

    /** Create a new simulation-generated node (no Layer 1 entry). */
    public static Node generated(NodeId id, String displayName) {
        return new Builder()
                .id(id)
                .displayName(displayName)
                .canonConfidence(0)
                .build();
    }

    /** Builder for constructing nodes. */
    public static final class Builder {
        private NodeId id;
        private String displayName;
        private String registryId;
        private int canonConfidence;
        private Map<Class<? extends Component<?>>, Component<?>> components = new LinkedHashMap<>();

        public Builder id(NodeId id) { this.id = id; return this; }
        public Builder displayName(String name) { this.displayName = name; return this; }
        public Builder registryId(String id) { this.registryId = id; return this; }
        public Builder canonConfidence(int c) { this.canonConfidence = c; return this; }
        public Builder component(Component<?> c) {
            @SuppressWarnings("unchecked")
            Class<? extends Component<?>> key = (Class<? extends Component<?>>) c.getClass();
            this.components.put(key, c.copy());
            return this;
        }
        public Builder components(Map<Class<? extends Component<?>>, Component<?>> comps) {
            this.components = new LinkedHashMap<>(comps);
            return this;
        }
        public Node build() { return new Node(this); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() {
        return "Node{" + id + ", name='" + displayName + "'"
                + (registryId != null ? ", canon=" + registryId : "")
                + ", conf=" + canonConfidence
                + ", components=" + components.size() + "}";
    }
}