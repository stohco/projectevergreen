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
 * <p>Per Constitution Article XXII: "A canon entry that exists only as data,
 * never as experience, is a failure." This registry bridges NPC data files
 * to actual in-world spawning via {@link ReificationScan}.
 *
 * <h2>Character ID contract</h2>
 * <p>The character IDs registered here MUST match the filename stems in
 * {@code data/ergenverse/npcs/_index.json} exactly. The index lists files
 * like {@code npc_situ_nan.json}, so the map key is {@code npc_situ_nan}.
 * Registering {@code situ_nan} (without prefix) causes a null lookup in
 * {@link dev.ergenverse.simulation.WorldStateDataLoader#getEntry}.
 *
 * <h2>Location mapping</h2>
 * <p>{@link SpatialBiomeCacheIndex} resolves biome IDs to location IDs:
 * {@code ergenverse:zhao_mountains} → {@code zhao_mountains},
 * {@code ergenverse:zhao_plains} → {@code zhao_plains}, etc.
 * NPCs are registered under the resolvable location ID where their biome
 * generates.
 *
 * <p><b>Provenance: INFERRED.</b> Spawn locations inferred from NPC data
 * files' "location" field, mapped to the nearest resolvable biome region.
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
        // ── Zhao Plains (ergenverse:zhao_plains biome) ─────────────────
        // Teng Family patriarch — leads the army that attacks the Wang family
        register("zhao_plains", "npc_teng_huayuan");
        // Qing Shui — Wang Lin's first mentor, encountered in Zhao plains
        register("zhao_plains", "npc_qing_shui");

        // ── Zhao Mountains / Heng Yue Sect (ergenverse:zhao_mountains biome) ─
        // Canon: Wang Lin's first sect. Per Article XXIII (Vertical Slice
        // Completion), all named Heng Yue NPCs must be registered.
        // The sect's structures (jigsaw) generate in zhao_mountains biome.
        // NPC data files all have "faction": "heng_yue_sect".
        register("zhao_mountains", "npc_qiu_siping");        // Senior disciple
        register("zhao_mountains", "npc_wang_zhuo");         // Fellow disciple
        register("zhao_mountains", "npc_wang_hao");          // Friendly fellow disciple (CANON_RI N06)
        register("zhao_mountains", "npc_yun_fei");           // Sect disciple
        register("zhao_mountains", "npc_qian_kun");          // Sect disciple
        register("zhao_mountains", "npc_sun_dazhu");         // Rival disciple (ecology enemy)
        register("zhao_mountains", "npc_wu_yu");             // Sect disciple
        register("zhao_mountains", "npc_ye_zi");             // Sect disciple
        register("zhao_mountains", "npc_sun_zhenwei");       // Sect disciple
        register("zhao_mountains", "npc_chen_bailiang");     // Sect disciple
        register("zhao_mountains", "npc_zhang_hu");          // Sect disciple
        register("zhao_mountains", "npc_zhou_lin");          // Sect disciple

        // ── Zhao Spirit Herb Hills (ergenverse:zhao_spirit_herb_hills biome) ─
        // INFERRED: Hills with 7 herb features must have a gatherer (Article II).
        register("zhao_spirit_herb_hills", "npc_herb_gatherer_li");

        // ── Great Wang Capital (ergenverse:great_wang_dynasty biome) ─────
        // INFERRED: The Zhao Country capital must have inhabitants (Article II).
        // All NPCs are INFERRED from the 11-district city structure.
        register("great_wang_dynasty", "npc_gov_zhao_ming");       // Governor
        register("great_wang_dynasty", "npc_guard_captain_lin");   // Guard captain
        register("great_wang_dynasty", "npc_priestess_yun");       // Temple priestess
        register("great_wang_dynasty", "npc_merchant_hong");       // Market merchant
        register("great_wang_dynasty", "npc_tavern_keeper_zhou");  // Tavern keeper

        // ── Vermilion Bird Country (ergenverse:vermilion_bird_* biomes) ──
        register("vermilion_bird_country", "npc_situ_nan");       // Divine Emperor
        register("vermilion_bird_country", "npc_daoist_water");   // Ancient cultivator

        Ergenverse.LOGGER.info("[NpcSpawnRegistry] Registered {} NPCs across {} locations.",
                totalCount(), SPAWNS_BY_LOCATION.size());
    }

    /**
     * Register an NPC to spawn at a given location.
     *
     * @param locationId the resolvable location ID (e.g. "zhao_plains")
     * @param characterId the NPC character ID (MUST match _index.json filename
     *                    stem, e.g. "npc_situ_nan" NOT "situ_nan")
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
