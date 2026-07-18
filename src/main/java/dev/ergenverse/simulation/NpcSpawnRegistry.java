package dev.ergenverse.simulation;

import dev.ergenverse.core.Ergenverse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NpcSpawnRegistry — data-driven mapping from location IDs to canon NPC
 * character IDs that should materialize there.
 *
 * <p>Replaces the hardcoded {@code "zhao_plains".equals(resolvedLocation)}
 * check in {@link ReificationScan} with a registry lookup. Any location
 * can have zero or more NPCs registered to spawn there.
 *
 * <h2>Why this matters</h2>
 * <p>Per the user's architectural review: "Planet Suzaku is my number one
 * priority. Stop adding worlds. Finish one planet. 100%." The #1 blocker
 * for Suzaku feeling alive is that canon NPCs don't materialize — the
 * ReificationScan only spawned wang_tiangui. The 4 priority NPCs (Situ Nan,
 * Qing Shui, Daoist Water, Teng Huayuan) had data files but ZERO spawn
 * logic. They would NEVER appear in the world.
 *
 * <p>This registry fixes that. It maps each NPC's canonical location to
 * the biome-resolvable location ID. When a player enters a location, the
 * ReificationScan queries this registry and materializes all registered
 * NPCs whose runtime state allows it (not dead, not already spawned).
 *
 * <h2>Location mapping</h2>
 * <p>The {@link SpatialBiomeCacheIndex} resolves Minecraft biome IDs to
 * canonical location IDs. Some NPC data files specify finer-grained
 * locations (e.g. "vermilion_bird_divine_sect") that don't have matching
 * biomes. This registry maps NPCs to the BROADER resolvable region:
 * <ul>
 *   <li>Situ Nan → vermilion_bird_country (the Divine Sect is within it)</li>
 *   <li>Teng Huayuan → zhao_plains (Teng Family City structures generate
 *       within Zhao; he's encountered when his army attacks)</li>
 *   <li>Qing Shui → zhao_plains (Wang Lin's mentor; encountered early)</li>
 *   <li>Daoist Water → vermilion_bird_country (ancient cultivator of VB)</li>
 * </ul>
 *
 * <h2>Future: JSON-driven</h2>
 * <p>v1 uses hardcoded registration for the 5 priority NPCs. Future: load
 * from {@code data/ergenverse/npc_spawn_map.json} so content designers can
 * add NPC spawns without touching Java.
 *
 * <p><b>Provenance: INFERRED.</b> The spawn locations are inferred from
 * the NPC data files' "location" field, mapped to the nearest resolvable
 * biome region.
 */
public final class NpcSpawnRegistry {

    /** Map: locationId → list of NPC character IDs that spawn there. */
    private static final Map<String, List<String>> SPAWNS_BY_LOCATION = new HashMap<>();

    private NpcSpawnRegistry() {}

    /**
     * Register all canon NPCs. Called once on server start.
     *
     * <p>Each entry maps a resolvable location ID (from
     * {@link SpatialBiomeCacheIndex#mapBiomeToLocationId}) to the NPC
     * character IDs that should materialize when a player enters that
     * location.
     */
    public static void initialize() {
        register("zhao_plains", "wang_tiangui");      // Wang Lin's mortal father
        register("zhao_plains", "teng_huayuan");      // Teng Family patriarch (attacks Wang family)
        register("zhao_plains", "qing_shui");          // Wang Lin's serene mentor
        register("vermilion_bird_country", "situ_nan");       // Divine Emperor, Wang Lin's sworn brother
        register("vermilion_bird_country", "daoist_water");   // Ancient water-aspected cultivator

        Ergenverse.LOGGER.info("[NpcSpawnRegistry] Registered {} NPCs across {} locations.",
                totalCount(), SPAWNS_BY_LOCATION.size());
    }

    /**
     * Register an NPC to spawn at a given location.
     *
     * @param locationId the resolvable location ID (e.g. "zhao_plains")
     * @param characterId the NPC character ID (e.g. "situ_nan")
     */
    public static void register(String locationId, String characterId) {
        SPAWNS_BY_LOCATION
                .computeIfAbsent(locationId, k -> new ArrayList<>())
                .add(characterId);
    }

    /**
     * Get all NPC character IDs registered to spawn at the given location.
     *
     * @param locationId the resolvable location ID
     * @return an unmodifiable list of character IDs (may be empty)
     */
    public static List<String> getNpcsForLocation(String locationId) {
        List<String> npcs = SPAWNS_BY_LOCATION.get(locationId);
        return npcs != null ? Collections.unmodifiableList(npcs) : Collections.emptyList();
    }

    /**
     * Get all location IDs that have at least one registered NPC.
     *
     * @return a set of location IDs
     */
    public static java.util.Set<String> getLocationsWithSpawns() {
        return Collections.unmodifiableSet(SPAWNS_BY_LOCATION.keySet());
    }

    /** Total number of registered NPC spawns across all locations. */
    public static int totalCount() {
        int count = 0;
        for (List<String> npcs : SPAWNS_BY_LOCATION.values()) count += npcs.size();
        return count;
    }

    /** Clear the registry. Called on world unload. */
    public static void clear() {
        SPAWNS_BY_LOCATION.clear();
    }
}
