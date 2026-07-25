package dev.ergenverse.runtime;

/**
 * MemoryRuntime — what each actor remembers. Deep memory, never forgotten.
 *
 * <p><b>Contract (Article — Deep Memory):</b> Every actor (NPC, beast,
 * player) has a memory store. Memories are never automatically deleted.
 * An NPC who saw the player steal a sword on day 1 remembers it on day 500.
 * Memories can be suppressed (by formations, by cultivation techniques) but
 * not erased by time.
 *
 * <p>Memories have:
 * <ul>
 *   <li>A timestamp (game time when formed)</li>
 *   <li>A type (visual, auditory, emotional, semantic)</li>
 *   <li>A strength (how vivid — decays slowly, never reaches 0)</li>
 *   <li>A semantic tag (what the memory MEANS — "betrayal", "kindness", "threat")</li>
 *   <li>References to other actors involved</li>
 * </ul>
 */
public final class MemoryRuntime {

    private final PlanetSuzakuBlueprint blueprint;
    private boolean loaded = false;

    MemoryRuntime(PlanetSuzakuBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    /** Load all memories from the save. Called on WorldRuntime.initialize(). */
    void loadAll() {
        // TODO: Load memory store from save data.
        // On day 0, all memories are empty (or pre-seeded with canon backstories).
        loaded = true;
    }

    public boolean isLoaded() { return loaded; }
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }
}
