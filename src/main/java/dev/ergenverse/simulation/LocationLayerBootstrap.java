package dev.ergenverse.simulation;

import dev.ergenverse.core.Ergenverse;

/**
 * LocationLayerBootstrap — wires the Location-Layers system into the world
 * at server startup.
 *
 * <p>Called from {@link dev.ergenverse.core.Ergenverse} during commonSetup
 * (or first server tick). Seeds all canon locations, then verifies the
 * registry is non-empty.
 */
public final class LocationLayerBootstrap {

    private static boolean bootstrapped = false;

    private LocationLayerBootstrap() {}

    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        Ergenverse.LOGGER.info("[Ergenverse] Bootstrapping Location Layers system...");
        LocationLayerRegistry.clear();
        LocationLayerSeeder.seed();
        Ergenverse.LOGGER.info("[Ergenverse] Location Layers bootstrapped: {} locations.",
                LocationLayerRegistry.count());
    }
}
