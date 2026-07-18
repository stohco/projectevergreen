package dev.ergenverse.simulation.los;

/**
 * SimulationLevel — 6 levels of simulation fidelity for a given entity.
 *
 * <p>The Level-of-Simulation (LoS) engine decides HOW MUCH simulation effort
 * to spend on each entity in the world. Distant, irrelevant entities get
 * STATIC_DATA; the player's antagonist gets FULL_COGNITION. This is the
 * core scalability mechanism for a living world with thousands of entities.
 *
 * <p>The 6 levels (canon audit + RI canon fidelity directive):
 * <ol>
 *   <li>{@code STATIC_DATA} — entity is a row in a table; no per-tick sim.</li>
 *   <li>{@code HISTORICAL} — entity has a timeline of past events; no present sim.</li>
 *   <li>{@code TERRITORY} — entity has a territory + aggregated populations;
 *       simulated at seasonal resolution.</li>
 *   <li>{@code ACTIVE_ACTOR} — entity is a participant in the world; runs
 *       goals + plans but with simple personality, not full curves.</li>
 *   <li>{@code FULL_COGNITION} — entity runs the entire cognition pipeline:
 *       needs → goals → plans → personality-scored actions → memory.</li>
 *   <li>{@code STORY_IMPORTANCE} — entity is a canon protagonist / antagonist;
 *       gets every system, full memory, full reputation, full Dao identity.</li>
 * </ol>
 */
public enum SimulationLevel {
    STATIC_DATA      (0, "Static data only"),
    HISTORICAL       (1, "Timeline / history only"),
    TERRITORY        (2, "Territory + aggregated populations"),
    ACTIVE_ACTOR     (3, "Goals + plans + simple personality"),
    FULL_COGNITION   (4, "Full cognition pipeline"),
    STORY_IMPORTANCE (5, "Canon protagonist / antagonist — all systems");

    public final int order;
    public final String label;

    SimulationLevel(int order, String label) {
        this.order = order;
        this.label = label;
    }

    public boolean atLeast(SimulationLevel other) {
        return this.order >= other.order;
    }

    public static SimulationLevel byOrder(int order) {
        for (SimulationLevel l : values()) if (l.order == order) return l;
        return order >= 5 ? STORY_IMPORTANCE : STATIC_DATA;
    }
}
