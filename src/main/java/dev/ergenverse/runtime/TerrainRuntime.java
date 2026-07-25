package dev.ergenverse.runtime;

/**
 * TerrainRuntime — what blocks exist where.
 *
 * <p><b>Contract:</b> Terrain is LOADED from the blueprint, not GENERATED
 * from noise. The chunk generator asks this runtime "what already exists?"
 * and places blocks accordingly. Noise is used only as a base layer (the
 * deterministic canon seed ensures consistent terrain); the blueprint
 * overlays hand-authored structures at fixed canonical coordinates.
 *
 * <p>The save stores only DELTAS — blocks the simulation has changed since
 * day 0. Everything else comes from the blueprint.
 */
public final class TerrainRuntime {

    private final PlanetSuzakuBlueprint blueprint;
    private boolean loaded = false;

    TerrainRuntime(PlanetSuzakuBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    /** Load terrain data from the blueprint. Called on WorldRuntime.initialize(). */
    void loadFromBlueprint() {
        // TODO: Implement blueprint-driven chunk loading.
        // For now, the deterministic seed + minecraft:overworld noise settings
        // provide the base terrain. Hand-authored structures are placed by
        // the settlement builders (WangFamilyVillageBuilder, etc.) at the
        // canonical coordinates defined in PlanetSuzakuBlueprint.
        loaded = true;
    }

    public boolean isLoaded() { return loaded; }
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }
}
