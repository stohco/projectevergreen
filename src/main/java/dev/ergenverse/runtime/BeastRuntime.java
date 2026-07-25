package dev.ergenverse.runtime;

/**
 * BeastRuntime — spirit beasts live in the world, not spawned on demand.
 *
 * <p><b>Contract:</b> Spirit beasts are canon entities. A wolf pack near
 * Wang Family Village has existed for years before the player arrives.
 * When the player enters their territory, the pack is materialized from
 * the simulation state (not spawned randomly). When the player leaves,
 * the pack is serialized and continues its simulated life.
 *
 * <p>Beast populations are tracked in the simulation. If a pack is hunted
 * to extinction, no new wolves spawn — the population is gone. This is
 * canon-faithful: the world evolves, it doesn't respawn.
 */
public final class BeastRuntime {

    private final PlanetSuzakuBlueprint blueprint;
    private boolean loaded = false;

    BeastRuntime(PlanetSuzakuBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    /** Load all canonical beast populations. Called on WorldRuntime.initialize(). */
    void loadAll() {
        // TODO: Initialize beast populations at canonical territories.
        // Spirit wolves near Wang Family Village forest.
        // Spirit deer in Zhao plains.
        // Spirit tigers in Zhao mountains.
        // Fire beasts in Fire Burn Country.
        // Sea serpents in Sea of Devils.
        // Soul fish in spirit waters.
        // Bats in cave systems.
        // Cranes near Heng Yue Sect peaks.
        // Hawks in mountain ranges.
        // Qilin (extremely rare — maybe 1-2 in the whole world).
        // Stone back boars in rocky regions.
        loaded = true;
    }

    public boolean isLoaded() { return loaded; }
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }
}
