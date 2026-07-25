package dev.ergenverse.runtime;

/**
 * EconomyRuntime — spirit stones, trade routes, market state.
 *
 * <p><b>Contract:</b> The economy is a simulation. Spirit stones are the
 * universal currency. Trade routes connect settlements. Prices fluctuate
 * based on supply and demand. When a sect produces pills, the pill supply
 * in nearby markets increases and prices drop. When a mine runs dry, spirit
 * stone fragments become scarcer.
 *
 * <p>The player is a participant in the economy, not its center. NPC
 * merchants trade with each other whether the player is watching or not.
 */
public final class EconomyRuntime {

    private final PlanetSuzakuBlueprint blueprint;
    private boolean loaded = false;

    EconomyRuntime(PlanetSuzakuBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    /** Load all economic state. Called on WorldRuntime.initialize(). */
    void loadAll() {
        // TODO: Initialize trade routes and market state.
        // Wang Family Village: minor market (spirit herbs, basic materials).
        // Teng Family City: trade hub (spirit stones, weapons, artifacts).
        // Tian Shui City: major port (imports from Sea of Devils, exports pills).
        // Vermilion Bird Capital: imperial market (rare items, high-tier spirit stones).
        loaded = true;
    }

    public boolean isLoaded() { return loaded; }
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }
}
