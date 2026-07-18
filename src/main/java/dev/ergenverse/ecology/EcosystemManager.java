package dev.ergenverse.ecology;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.LocationLayerRegistry;
import dev.ergenverse.simulation.LocationLayers;

import java.util.UUID;

/**
 * EcosystemManager — connects {@link CausalEcology} to the Location Layers system.
 *
 * <p>Each canon location in the {@link LocationLayerRegistry} that has a
 * FLORA / FAUNA layer is bound to a {@link CausalEcology.Ecosystem}, keyed
 * by a synthetic UUID derived from the location id. This means the
 * ecological simulation (Lotka-Volterra trophic cascades) is driven by the
 * FLORA/FAUNA layer values, and any ecosystem event is written back to
 * the HISTORY layer.
 */
public final class EcosystemManager {

    private EcosystemManager() {}

    /** Bind all canon locations that have a FLORA+FAUNA layer to a CausalEcology.Ecosystem. */
    public static void bindAll() {
        for (String locId : LocationLayerRegistry.allLocationIds()) {
            bind(locId);
        }
    }

    /** Bind a single location to an ecosystem, if not already bound. */
    public static void bind(String locationId) {
        LocationLayers layers = LocationLayerRegistry.get(locationId);
        if (layers == null) return;
        String flora = layers.get(LocationLayers.LayerId.FLORA);
        String fauna = layers.get(LocationLayers.LayerId.FAUNA);
        if (flora == null && fauna == null) return;

        UUID id = uuidFromLocation(locationId);
        if (CausalEcology.get(id) != null) return; // already bound

        double qi = parseQi(layers.get(LocationLayers.LayerId.SPIRITUAL_QI));
        CausalEcology.Ecosystem eco = new CausalEcology.Ecosystem(
                id, locationId, qi,
                parsePopulation(flora, 50.0),
                parsePopulation(fauna, 20.0, "herbivore"),
                parsePopulation(fauna, 5.0,  "predator"),
                parsePopulation(fauna, 0.0,  "apex"),
                parseSectCultivators(layers),
                parseMerchants(layers)
        );
        CausalEcology.register(eco);
        Ergenverse.LOGGER.debug("[Ergenverse] EcosystemManager bound {} to ecosystem.", locationId);
    }

    /** Write recent events from each ecosystem back to its location's HISTORY layer. */
    public static void syncHistories() {
        for (CausalEcology.Ecosystem eco : CausalEcology.all()) {
            LocationLayers layers = LocationLayerRegistry.get(eco.name);
            if (layers == null) continue;
            if (eco.recentEvents.isEmpty()) continue;
            String hist = layers.get(LocationLayers.LayerId.HISTORY);
            String top = eco.recentEvents.get(0);
            String updated = (hist == null ? "" : hist + " | ") + top;
            layers.put(LocationLayers.LayerId.HISTORY, updated);
        }
    }

    // ── Parsing helpers ───────────────────────────────────────────────

    /** Cheap deterministic UUID derivation from a string (type-3 name UUID). */
    private static UUID uuidFromLocation(String locationId) {
        byte[] bytes = locationId.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return UUID.nameUUIDFromBytes(bytes);
    }

    private static double parseQi(String spiritualQiString) {
        if (spiritualQiString == null) return 0.3;
        // format: "qi_density=0.45,vein=sv_mosquito"
        for (String token : spiritualQiString.split(",")) {
            token = token.trim();
            if (token.startsWith("qi_density=")) {
                try {
                    return Double.parseDouble(token.substring("qi_density=".length()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return 0.3;
    }

    private static double parsePopulation(String layerValue, double fallback) {
        if (layerValue == null) return fallback;
        if (layerValue.startsWith("high"))    return 100.0;
        if (layerValue.startsWith("moderate"))return 30.0;
        if (layerValue.startsWith("low"))     return 10.0;
        if (layerValue.startsWith("none"))    return 0.0;
        return fallback;
    }

    private static double parsePopulation(String layerValue, double fallback, String substring) {
        if (layerValue == null) return fallback;
        // format: "high:herbivore,moderate:predator,swarm:mosquito"
        for (String token : layerValue.split(",")) {
            token = token.trim();
            if (token.contains(substring)) {
                if (token.startsWith("high"))     return 100.0;
                if (token.startsWith("moderate")) return 30.0;
                if (token.startsWith("low"))      return 10.0;
                if (token.startsWith("none"))     return 0.0;
                return fallback;
            }
        }
        return fallback;
    }

    private static double parseSectCultivators(LocationLayers layers) {
        String social = layers.get(LocationLayers.LayerId.SOCIAL);
        if (social == null) return 0;
        if (social.contains("active")) return 50.0;
        if (social.contains("moderate")) return 20.0;
        if (social.contains("low")) return 5.0;
        return 0;
    }

    private static double parseMerchants(LocationLayers layers) {
        String econ = layers.get(LocationLayers.LayerId.ECONOMIC);
        if (econ == null) return 0;
        if (econ.contains("active")) return 10.0;
        if (econ.contains("moderate")) return 3.0;
        return 0;
    }
}
