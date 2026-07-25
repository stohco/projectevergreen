package dev.ergenverse.runtime;

/**
 * NPCRuntime — NPCs load, never spawn.
 *
 * <p><b>Contract:</b> Every canonical NPC has a permanent UUID (defined in
 * PlanetSuzakuBlueprint: wang_lin, old_chen, li_muwan, etc.). When a chunk
 * loads, the NPC is deserialized from the save (or instantiated from the
 * blueprint if this is day 0). When a chunk unloads, the NPC is serialized
 * and removed from the world — NOT despawned. Their life continues in the
 * simulation; they are simply not materialized as Minecraft entities.
 *
 * <p>This is fundamentally different from vanilla Minecraft's spawn/despawn
 * cycle. NPCs never cease to exist. They are always simulated; they are
 * only sometimes rendered.
 */
public final class NPCRuntime {

    private final PlanetSuzakuBlueprint blueprint;
    private boolean loaded = false;

    NPCRuntime(PlanetSuzakuBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    /** Load all canonical NPCs. Called on WorldRuntime.initialize(). */
    void loadAll() {
        // TODO: Instantiate all canonical NPCs from the blueprint.
        // Wang Lin starts at Wang Family Village. Old Chen at Heng Yue Sect.
        // Li Muwan at Xuan Dao Sect. Etc.
        // Each NPC gets a permanent UUID (from PlanetSuzakuBlueprint.NPC_*).
        loaded = true;
    }

    public boolean isLoaded() { return loaded; }
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }
}
