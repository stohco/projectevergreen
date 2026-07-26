package dev.ergenverse.runtime;

/**
 * WeatherRuntime — canon weather: Snow Domain blizzards, Sea of Devils storms.
 *
 * <p><b>Contract:</b> Weather is canon-driven, not random. The Snow Domain
 * has perpetual blizzards. The Sea of Devils has spiritual storms. The
 * Jue Ming Valley (决明谷) has death-law mists. Weather
 * affects cultivation (storms can disrupt meditation), beast behavior
 * (predators hunt more aggressively in storms), and travel (blizzards
 * reduce visibility and movement speed).
 *
 * <p>Weather patterns are part of the blueprint, but their exact timing
 * evolves in the simulation (a drought can develop, a storm can linger).
 */
public final class WeatherRuntime {

    private final PlanetSuzakuBlueprint blueprint;
    private boolean started = false;

    WeatherRuntime(PlanetSuzakuBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    /** Start the weather simulation. Called on WorldRuntime.initialize(). */
    void start() {
        // TODO: Initialize canon weather patterns.
        // Snow Domain: perpetual blizzard (snow + wind + reduced visibility)
        // Sea of Devils: spiritual storms (rain + lightning + qi disruption)
        // Jue Ming Valley (决明谷): death-law mist (fog + soul oppression)
        // Zhao Plains: temperate, seasonal
        // Fire Burn Country: hot, dry, occasional fire storms
        started = true;
    }

    public boolean isStarted() { return started; }
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }
}
