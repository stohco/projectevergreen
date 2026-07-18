package dev.ergenverse.simulation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * LocationLayerRegistry — central registry of all Location-Layer components.
 *
 * <p>Keyed by location id. Each entry is a {@link LocationLayers} instance
 * populated by the {@link LocationLayerSeeder} (for canon locations) or by
 * on-demand procedural generation (for non-canon locations).
 */
public final class LocationLayerRegistry {

    private static final Map<String, LocationLayers> BY_LOCATION_ID = new HashMap<>();

    private LocationLayerRegistry() {}

    public static void register(String locationId, LocationLayers layers) {
        BY_LOCATION_ID.put(locationId, layers);
    }

    public static LocationLayers get(String locationId) {
        return BY_LOCATION_ID.get(locationId);
    }

    public static LocationLayers getOrCreate(String locationId) {
        return BY_LOCATION_ID.computeIfAbsent(locationId, k -> new LocationLayers());
    }

    public static Collection<String> allLocationIds() {
        return BY_LOCATION_ID.keySet();
    }

    public static int count() {
        return BY_LOCATION_ID.size();
    }

    public static void clear() {
        BY_LOCATION_ID.clear();
    }
}
