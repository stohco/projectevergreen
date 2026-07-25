package dev.ergenverse.runtime;

/**
 * RelationshipRuntime — the NPC-to-NPC social graph.
 *
 * <p><b>Contract:</b> Every NPC has relationships with other NPCs. Wang Lin
 * has a relationship with Old Chen (master-disciple), Li Muwan (romantic),
 * Wang Zhuo (rivalry), Teng Huayuan (enmity). These relationships have:
 * <ul>
 *   <li>A strength (-100 to +100)</li>
 *   <li>A type (family, master_disciple, romantic, rivalry, enmity, ally, neutral)</li>
 *   <li>A history of events that modified it</li>
 *   <li>A trust level (how much one NPC believes the other)</li>
 *   <li>A familiarity level (how well they know each other)</li>
 * </ul>
 *
 * <p>Relationships change through MEANING — semantic events on the
 * WorldEventBus (act_of_mercy, cultivation_revealed, promise_broken)
 * update opinions based on what happened, not just that something happened.
 */
public final class RelationshipRuntime {

    private final PlanetSuzakuBlueprint blueprint;
    private boolean loaded = false;

    RelationshipRuntime(PlanetSuzakuBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    /** Load all canonical relationships. Called on WorldRuntime.initialize(). */
    void loadAll() {
        // TODO: Initialize canonical relationships.
        // Wang Lin ↔ Old Chen: master_disciple, +80 trust (canon)
        // Wang Lin ↔ Wang Zhuo: rivalry, -20 (canon — Wang Zhuo bullied Wang Lin)
        // Wang Lin ↔ Teng Huayuan: enmity, -90 (canon — Teng family massacre)
        // Wang Lin ↔ Li Muwan: romantic, evolving (canon)
        loaded = true;
    }

    public boolean isLoaded() { return loaded; }
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }
}
