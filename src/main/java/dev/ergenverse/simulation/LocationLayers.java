package dev.ergenverse.simulation;

import java.util.HashMap;
import java.util.Map;

/**
 * LocationLayers — the 15-layer component of a Location.
 *
 * <p>Per canon audit, every Location in the world is composed of 15 layers,
 * each describing one dimension of the location's reality. The
 * {@link LocationLayerRegistry} holds the registered layers; the
 * {@link LocationLayerSeeder} populates the canon locations at bootstrap.
 *
 * <p>The 15 layers:
 * <ol>
 *   <li>PHYSICAL_TERRAIN   — block-level terrain, biomes, elevation</li>
 *   <li>SPIRITUAL_QI       — ambient Qi density / spirit vein output</li>
 *   <li>FLORA              — spirit herb / spirit grass distribution</li>
 *   <li>FAUNA              — spirit beast populations (per trophic level)</li>
 *   <li>WEATHER            — local weather (storm, tribulation clouds, miasma)</li>
 *   <li>FORMATIONS         — active formations inscribed in this location</li>
 *   <li>RESTRICTIONS       — active restrictions / seals</li>
 *   <li>OWNERSHIP          — who controls / claims this location</li>
 *   <li>HISTORY            — past events recorded here</li>
 *   <li>SOCIAL             — faction / settlement activity</li>
 *   <li>ECONOMIC           — trade routes, joss flame economy, market</li>
 *   <li>KARMIC             — accumulated karmic weight in this location</li>
 *   <li>DAO_RESIDUE        — Dao imprints left by past cultivators</li>
 *   <li>DIVINE_SENSE_ECHOES— divine-sense scans currently active</li>
 *   <li>STORY               — canon story events anchored here</li>
 * </ol>
 */
public final class LocationLayers {

    public enum LayerId {
        PHYSICAL_TERRAIN,
        SPIRITUAL_QI,
        FLORA,
        FAUNA,
        WEATHER,
        FORMATIONS,
        RESTRICTIONS,
        OWNERSHIP,
        HISTORY,
        SOCIAL,
        ECONOMIC,
        KARMIC,
        DAO_RESIDUE,
        DIVINE_SENSE_ECHOES,
        STORY
    }

    /** Canonical count — canon audit requires 15. */
    public static final int CANON_LAYER_COUNT = 15;

    private final Map<LayerId, String> layerData = new HashMap<>();

    public LocationLayers() {
        // Empty by default; populated by the seeder.
    }

    public void put(LayerId id, String data) {
        layerData.put(id, data);
    }

    public String get(LayerId id) {
        return layerData.get(id);
    }

    public String getOrDefault(LayerId id, String fallback) {
        return layerData.getOrDefault(id, fallback);
    }

    public boolean has(LayerId id) {
        return layerData.containsKey(id);
    }

    public Map<LayerId, String> all() {
        return layerData;
    }

    public int populatedCount() {
        return layerData.size();
    }
}
