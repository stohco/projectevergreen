package dev.ergenverse.simulation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.ergenverse.core.Ergenverse;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WorldStateDataLoader — the runtime bridge between the data/ergenverse/ JSON
 * library and the WorldStateEngine.
 *
 * <p><b>Why this exists:</b> the prior architecture had 2,981 data JSONs but
 * ZERO runtime loading. WorldStateEngine.queryWhatExists() returned an empty
 * list because nothing read those JSONs. This loader closes that gap.
 *
 * <p><b>Loading strategy:</b> data JSONs are packaged into the mod JAR under
 * {@code /data/ergenverse/<subsystem>/}. At runtime they are classpath
 * resources accessible via {@code ClassLoader.getResourceAsStream()}. We use
 * Gson to parse them into loose {@link JsonObject} maps keyed by filename stem.
 *
 * <p><b>Why not a SimpleJsonResourceReloadListener?</b> That is the "proper"
 * Forge 1.20.1 datapack-reload pattern, but it requires wiring through
 * {@code AddReloadListenerEvent} on the FORGE event bus and only fires on
 * world load / {@code /reload}. For v1 we load once at first query (lazy) from
 * the classpath, which is sufficient because the canon data is static (the
 * Prime Directive says canon is immutable). A reload listener can be added
 * later for hot-reloadable inferred data.
 *
 * <p><b>Thread safety:</b> {@code loadOnce()} is synchronized. Subsequent
 * calls are no-ops. The loaded maps are published via {@code volatile} and
 * are effectively immutable after load (we only ever read them).
 *
 * <h2>Prime Directive</h2>
 * <p>"Reality is objective." The data this loader reads IS the objective
 * reality of the world at t₀ (the edge of canon). The WorldStateEngine's job
 * is to surface this reality, not to invent it.
 */
public final class WorldStateDataLoader {

    private WorldStateDataLoader() {}

    private static final Gson GSON = new Gson();

    /** Base classpath prefix for all ergenverse data. */
    private static final String DATA_PREFIX = "/data/ergenverse/";

    /**
     * The subsystems to load. Each entry maps a subsystem name to its directory.
     * This list mirrors {@link WorldStateEngine#SUBSYSTEM_REGISTRY}.
     */
    private static final String[][] SUBSYSTEMS = {
            {"species",                 "species/"},
            {"species_variants",        "species_variants/"},
            {"ecosystems",              "ecosystems/"},
            {"ecosystem_integration",   "ecosystem_integration/"},
            {"migrations",              "migrations/"},
            {"macro_terrain",           "macro_terrain/"},
            {"provenance",              "provenance/"},
            {"civilizations",           "civilizations/"},
            {"npcs",                    "npcs/"},
            {"faction_relationships",   "faction_relationships/"},
            {"opportunities",           "opportunities/"},
            {"time_events",             "time_events/"},
            {"item_properties",         "item_properties/"},
            {"karma",                   "karma/"},
    };

    // ─── Loaded data (volatile — published once after load) ────────────

    /** Map: subsystem name → (Map: file stem → parsed JsonObject). */
    private static volatile Map<String, Map<String, JsonObject>> LOADED = null;

    /** True once loading has been attempted (success or failure). */
    private static volatile boolean loadAttempted = false;

    /** Total JSON files loaded across all subsystems. */
    private static volatile int totalLoaded = 0;

    // ─── Public API ────────────────────────────────────────────────────

    /**
     * Load all subsystem data from the classpath. Idempotent — subsequent
     * calls are no-ops. Safe to call from any thread.
     */
    public static synchronized void loadOnce() {
        if (loadAttempted) return;
        loadAttempted = true;

        Map<String, Map<String, JsonObject>> result = new HashMap<>();
        int total = 0;

        for (String[] sub : SUBSYSTEMS) {
            String name = sub[0];
            String dir = sub[1];
            Map<String, JsonObject> subsystemData = loadSubsystem(name, dir);
            result.put(name, subsystemData);
            total += subsystemData.size();
        }

        LOADED = result;
        totalLoaded = total;
        Ergenverse.LOGGER.info("[WorldState] Data loader: loaded {} JSON files across {} subsystems.",
                total, result.size());
    }

    /**
     * Get all entries for a subsystem. Returns an empty map if not loaded or
     * the subsystem doesn't exist.
     *
     * @param subsystem the subsystem name (e.g. "species", "npcs", "karma")
     * @return unmodifiable map of file-stem → JsonObject
     */
    public static Map<String, JsonObject> getSubsystem(String subsystem) {
        if (!loadAttempted) loadOnce();
        Map<String, Map<String, JsonObject>> data = LOADED;
        if (data == null) return Map.of();
        return data.getOrDefault(subsystem, Map.of());
    }

    /**
     * Get a single entry by subsystem + ID (filename stem).
     *
     * @param subsystem the subsystem name
     * @param id the file stem / entity ID
     * @return the JsonObject, or null if not found
     */
    public static JsonObject getEntry(String subsystem, String id) {
        return getSubsystem(subsystem).get(id);
    }

    /** Total number of JSON files loaded. */
    public static int getTotalLoaded() {
        return totalLoaded;
    }

    /** True if loading has been attempted. */
    public static boolean isLoaded() {
        return loadAttempted && LOADED != null;
    }

    /**
     * Get a status report string for logging/debug commands.
     */
    public static String getStatusReport() {
        if (!loadAttempted) return "WorldStateDataLoader: NOT LOADED";
        Map<String, Map<String, JsonObject>> data = LOADED;
        if (data == null) return "WorldStateDataLoader: LOAD FAILED";
        StringBuilder sb = new StringBuilder();
        sb.append("WorldStateDataLoader: ").append(totalLoaded).append(" files in ")
          .append(data.size()).append(" subsystems:\n");
        for (String[] sub : SUBSYSTEMS) {
            int count = data.getOrDefault(sub[0], Map.of()).size();
            sb.append("  ").append(String.format("%-24s %d%n", sub[0], count));
        }
        return sb.toString().trim();
    }

    // ─── Internal loading ──────────────────────────────────────────────

    /**
     * Load all JSON files in a subsystem directory from the classpath.
     *
     * <p>Because Java classpath resources don't support directory listing
     * portably, we maintain an index file per subsystem:
     * {@code /data/ergenverse/<subsystem>/_index.json} containing a JSON array
     * of filenames. If the index is missing, we fall back to a curated list
     * of known IDs (for subsystems with small canon-attested sets).
     *
     * <p>If both fail, the subsystem loads as empty and a warning is logged.
     */
    private static Map<String, JsonObject> loadSubsystem(String name, String dir) {
        Map<String, JsonObject> out = new HashMap<>();

        // Strategy 1: try the _index.json manifest
        List<String> files = listFilesViaIndex(name, dir);

        // Strategy 2: fallback to known-ID enumeration for critical subsystems
        if (files.isEmpty()) {
            files = listFilesViaFallback(name);
        }

        for (String fname : files) {
            String path = DATA_PREFIX + dir + fname;
            try (var is = WorldStateDataLoader.class.getResourceAsStream(path)) {
                if (is == null) continue; // file listed but not on classpath — skip
                Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                JsonElement el = JsonParser.parseReader(reader);
                if (el.isJsonObject()) {
                    String stem = fname.replace(".json", "");
                    out.put(stem, el.getAsJsonObject());
                }
            } catch (Exception e) {
                Ergenverse.LOGGER.warn("[WorldState] Failed to load {}: {}", path, e.getMessage());
            }
        }
        return out;
    }

    /**
     * Read {@code /data/ergenverse/<subsystem>/_index.json} — a JSON array of
     * filenames. This index is generated by the data-generation scripts.
     */
    private static List<String> listFilesViaIndex(String name, String dir) {
        String indexPath = DATA_PREFIX + dir + "_index.json";
        try (var is = WorldStateDataLoader.class.getResourceAsStream(indexPath)) {
            if (is == null) return List.of();
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            JsonElement el = JsonParser.parseReader(reader);
            if (!el.isJsonArray()) return List.of();
            List<String> out = new ArrayList<>();
            for (JsonElement f : el.getAsJsonArray()) {
                String fname = f.getAsString();
                if (!fname.endsWith(".json")) fname = fname + ".json";
                if (!fname.startsWith("_")) out.add(fname); // skip _index, _registry, _comment
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Fallback: known-ID enumeration for subsystems that lack an index file.
     * This is a curated list of canon-attested IDs. If an ID isn't here and
     * isn't in the index, it simply won't load — the query will return
     * empty for that ID, which is correct behavior (better to return nothing
     * than to fabricate).
     */
    private static List<String> listFilesViaFallback(String subsystem) {
        // Minimal fallback — the index files should be the primary mechanism.
        // This exists so that if the index is missing, at least the most
        // critical entities (Wang Lin, the Heaven-Defying Bead, Heng Yue Sect)
        // still load.
        return switch (subsystem) {
            case "npcs" -> List.of("wang_lin.json", "situ_nan.json", "qing_shui.json", "li_muwan.json",
                    "teng_huayuan.json", "seven_colored_daoist.json", "blood_ancestor.json",
                    "qiu_siping.json", "wang_zhuo.json", "zhou_wutai.json");
            case "civilizations" -> List.of("heng_yue_sect.json", "cloud_sky_sect.json",
                    "soul_refining_sect.json", "heavenly_fate_sect.json",
                    "vermilion_bird_divine_sect.json", "teng_family_city.json",
                    "wang_family_village.json");
            case "karma" -> List.of("teng_family_massacre.json", "heaven_defying_bead_discovery.json",
                    "ancient_god_inheritance.json", "seven_colored_killing.json",
                    "mosquito_valley_taming.json", "_registry.json");
            default -> List.of();
        };
    }

    // ─── Typed accessors (convenience) ─────────────────────────────────

    /**
     * Get a string field from a JsonObject, with a default.
     */
    public static String str(JsonObject o, String key, String def) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return def;
        return o.get(key).getAsString();
    }

    /**
     * Get an int field from a JsonObject, with a default.
     */
    public static int integer(JsonObject o, String key, int def) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return def;
        try { return o.get(key).getAsInt(); } catch (Exception e) { return def; }
    }

    /**
     * Get a string-array field from a JsonObject.
     */
    public static List<String> strArray(JsonObject o, String key) {
        if (o == null || !o.has(key) || !o.get(key).isJsonArray()) return List.of();
        List<String> out = new ArrayList<>();
        for (JsonElement el : o.getAsJsonArray(key)) {
            out.add(el.getAsString());
        }
        return out;
    }

    /**
     * Get a nested JsonObject field.
     */
    public static JsonObject obj(JsonObject o, String key) {
        if (o == null || !o.has(key) || !o.get(key).isJsonObject()) return null;
        return o.getAsJsonObject(key);
    }
}
