package dev.ergenverse.graph;

/**
 * A component — a typed piece of data attached to a graph node.
 *
 * <p>Components are the mechanism by which engines store their state on
 * graph nodes. A Node is just an ID + a component map. The component map
 * is typed: each engine reads and writes components of a specific class.
 *
 * <p><b>Design principle:</b> Components are NOT inheritance. A beast has
 * CultivationState AND SoulMatrix AND Composition — these are separate
 * components on the same node, not a class hierarchy.
 *
 * <p>Components are immutable value objects. To update a component, replace
 * it in the node's map with a new instance. This ensures the history engine
 * can track what changed.
 *
 * @param <T> the component's own type (self-referencing for type-safe access)
 */
public interface Component<T extends Component<T>> {

    /**
     * The component's type key. Used as the map key in Node's component map.
     * Convention: class name without "Component" suffix, lowercase.
     * E.g. CultivationStateComponent -> "cultivation_state".
     */
    String componentKey();

    /**
     * Create a defensive copy. Called before storing in the graph to
     * prevent external mutation of graph-internal state.
     */
    T copy();
}