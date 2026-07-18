package dev.ergenverse.simulation.cognition;

import java.util.EnumMap;
import java.util.Map;

/**
 * ReputationRegistry — 13-dimensional relationship model.
 *
 * <p>For each actor the cognition system tracks, the registry holds a
 * 13-dimensional vector describing this actor's standing with every other
 * actor (or faction) it has interacted with.
 *
 * <p>The 13 dimensions (canon audit, RI-canon-specific):
 * <ol>
 *   <li>TRUST — believes the other will keep their word.</li>
 *   <li>RESPECT — acknowledges the other's power / wisdom.</li>
 *   <li>FEAR — afraid of the other.</li>
 *   <li>DEBT — owes the other a favor.</li>
 *   <li>AFFECTION — emotional warmth.</li>
 *   <li>RIVALRY — competitive tension.</li>
 *   <li>CURIOSITY — wants to learn more about the other.</li>
 *   <li>SUSPICION — believes the other is hiding something.</li>
 *   <li>GRATITUDE — owes gratitude (not the same as DEBT).</li>
 *   <li>HATRED — deep animosity.</li>
 *   <li>SHARED_DAO — both walk a related Dao (mutual understanding).</li>
 *   <li>SHARED_HISTORY — have been through events together.</li>
 *   <li>KARMIC_ENTANGLEMENT — karmically linked (causally tied together).</li>
 * </ol>
 *
 * <p>Each dimension is in [−1, +1] except DEBT, GRATITUDE, SHARED_HISTORY,
 * and KARMIC_ENTANGLEMENT which are in [0, +1].
 */
public final class ReputationRegistry {

    public enum Dimension {
        TRUST, RESPECT, FEAR, DEBT, AFFECTION, RIVALRY, CURIOSITY,
        SUSPICION, GRATITUDE, HATRED, SHARED_DAO, SHARED_HISTORY, KARMIC_ENTANGLEMENT
    }

    /** A 13-D relationship vector from one actor's perspective. */
    public static final class Relationship {
        public final String otherActorId;
        public final Map<Dimension, Double> values = new EnumMap<>(Dimension.class);

        public Relationship(String otherActorId) {
            this.otherActorId = otherActorId;
            for (Dimension d : Dimension.values()) values.put(d, 0.0);
        }

        public double get(Dimension d) {
            return values.getOrDefault(d, 0.0);
        }

        public void set(Dimension d, double v) {
            values.put(d, clamp(d, v));
        }

        public void adjust(Dimension d, double delta) {
            values.put(d, clamp(d, values.getOrDefault(d, 0.0) + delta));
        }

        /** Net disposition — a coarse summary used by quick-decision heuristics. */
        public double netDisposition() {
            return get(Dimension.TRUST) * 0.2
                 + get(Dimension.RESPECT) * 0.15
                 + get(Dimension.AFFECTION) * 0.2
                 + get(Dimension.GRATITUDE) * 0.15
                 + get(Dimension.SHARED_DAO) * 0.1
                 + get(Dimension.SHARED_HISTORY) * 0.1
                 - get(Dimension.FEAR) * 0.1
                 - get(Dimension.SUSPICION) * 0.2
                 - get(Dimension.HATRED) * 0.4
                 - get(Dimension.RIVALRY) * 0.1;
        }

        private static double clamp(Dimension d, double v) {
            // DEBT, GRATITUDE, SHARED_HISTORY, KARMIC_ENTANGLEMENT ∈ [0,1]
            // All others ∈ [−1,+1]
            switch (d) {
                case DEBT: case GRATITUDE: case SHARED_HISTORY: case KARMIC_ENTANGLEMENT:
                    return v < 0 ? 0 : (v > 1 ? 1 : v);
                default:
                    return v < -1 ? -1 : (v > 1 ? 1 : v);
            }
        }
    }

    private final Map<String, Map<String, Relationship>> relations = new java.util.HashMap<>();

    /** Get (or create) the relationship from {@code actorId}'s perspective toward {@code otherId}. */
    public Relationship toward(String actorId, String otherId) {
        return relations
                .computeIfAbsent(actorId, k -> new java.util.HashMap<>())
                .computeIfAbsent(otherId, Relationship::new);
    }

    public java.util.Collection<Relationship> allToward(String actorId) {
        return relations.getOrDefault(actorId, new java.util.HashMap<>()).values();
    }

    public int actorCount() {
        return relations.size();
    }
}
