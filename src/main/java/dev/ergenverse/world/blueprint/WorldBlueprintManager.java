package dev.ergenverse.world.blueprint;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.server.level.ServerLevel;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * WorldBlueprintManager — loads and serves the authored Planet Suzaku geography.
 *
 * <p><b>The Prime Directive of World Generation:</b>
 * <blockquote>
 *   The simulation is the source of truth. Minecraft is the renderer.
 *   Minecraft's random seed does not determine the Er Gen universe.
 *   The World Blueprint does.
 * </blockquote>
 *
 * <p>This is the authored geography layer — the "stage." Countries, settlements,
 * mountain ranges, rivers, spirit veins, and roads are ALL defined here as data.
 * They do not move between playthroughs. They do not depend on a random seed.
 *
 * <p>The Simulation Seed (separate, random per world) controls what varies:
 * herb placement, NPC choices, weather, events, rumors. Geography never varies.
 *
 * <p>Architecture (per user directive):
 * <pre>
 *   Er Gen Canon Database
 *           |
 *           v
 *    World Blueprint  (this class loads it)
 *           |
 *   --------+--------
 *   |               |
 *   Terrain         Simulation
 *   Generator       Engine
 *   |               |
 *   |               +-- NPCs, Ecology, Economy, Rumors, History, Opportunities
 *   |
 *   +-- Asks "What's supposed to exist here?" instead of "Roll random noise"
 * </pre>
 *
 * <p>Like Zelda, Elden Ring, Skyrim — Whiterun doesn't randomly spawn somewhere
 * else. Neither does Wang Family Village. The stage is fixed; the actors change.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public final class WorldBlueprintManager {

    private WorldBlueprintManager() {}

    private static final Gson GSON = new Gson();
    private static final String BLUEPRINT_PATH =
            "/data/ergenverse/worldgen/blueprint/planet_suzaku.json";

    private static JsonObject blueprint = null;
    private static boolean loaded = false;

    /** Fixed canon seed — makes terrain noise deterministic across all playthroughs. */
    public static final long CANON_SEED = -7283654891029384756L;

    // ── Loading ───────────────────────────────────────────────────────

    /**
     * Loads the World Blueprint from the mod's bundled resources.
     * Called once during server startup. Idempotent.
     */
    public static synchronized void load() {
        if (loaded) return;
        try (InputStream is = WorldBlueprintManager.class.getResourceAsStream(BLUEPRINT_PATH)) {
            if (is == null) {
                Ergenverse.LOGGER.error("[Ergenverse] World Blueprint not found at {}", BLUEPRINT_PATH);
                return;
            }
            JsonObject root = GSON.fromJson(
                    new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);
            blueprint = root.getAsJsonObject("blueprint");
            loaded = true;

            int countries = blueprint.getAsJsonArray("countries").size();
            int settlements = blueprint.getAsJsonArray("settlements").size();
            Ergenverse.LOGGER.info("[Ergenverse] World Blueprint loaded: {} countries, {} settlements.",
                    countries, settlements);
            Ergenverse.LOGGER.info("[Ergenverse] Canon Seed: {} (terrain is deterministic — same every playthrough).",
                    CANON_SEED);
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[Ergenverse] Failed to load World Blueprint: {}", e.getMessage(), e);
        }
    }

    /** Returns true if the blueprint has been loaded. */
    public static boolean isLoaded() {
        return loaded && blueprint != null;
    }

    /** Gets the raw blueprint JSON. Loads lazily if needed. */
    private static JsonObject ensureLoaded() {
        if (!loaded) load();
        return blueprint;
    }

    // ── Country Resolution (point-in-polygon) ─────────────────────────

    /**
     * Determines which country a world coordinate (x, z) belongs to.
     * Uses the ray-casting point-in-polygon algorithm.
     *
     * @return the country ID, or null if the point is in no country (ocean/wilderness).
     */
    public static String getCountryAt(int x, int z) {
        JsonObject bp = ensureLoaded();
        if (bp == null) return null;

        for (JsonElement el : bp.getAsJsonArray("countries")) {
            JsonObject country = el.getAsJsonObject();
            JsonArray polygon = country.getAsJsonArray("polygon");
            if (pointInPolygon(x, z, polygon)) {
                return country.get("id").getAsString();
            }
        }
        return null;
    }

    /**
     * Ray-casting point-in-polygon test.
     * @param px point X
     * @param pz point Z
     * @param polygon array of [x, z] pairs
     * @return true if point is inside (or on edge of) the polygon
     */
    private static boolean pointInPolygon(double px, double pz, JsonArray polygon) {
        int n = polygon.size();
        if (n < 3) return false;
        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            JsonArray vi = polygon.get(i).getAsJsonArray();
            JsonArray vj = polygon.get(j).getAsJsonArray();
            double xi = vi.get(0).getAsDouble(), zi = vi.get(1).getAsDouble();
            double xj = vj.get(0).getAsDouble(), zj = vj.get(1).getAsDouble();
            if (((zi > pz) != (zj > pz)) &&
                    (px < (xj - xi) * (pz - zi) / (zj - zi + 1e-10) + xi)) {
                inside = !inside;
            }
        }
        return inside;
    }

    /** Gets the biome rule for a country (e.g., "ergenverse:zhao_plains"). */
    public static String getBiomeRuleForCountry(String countryId) {
        JsonObject bp = ensureLoaded();
        if (bp == null || countryId == null) return null;
        for (JsonElement el : bp.getAsJsonArray("countries")) {
            JsonObject c = el.getAsJsonObject();
            if (c.get("id").getAsString().equals(countryId)) {
                return c.has("biome_rule") ? c.get("biome_rule").getAsString() : null;
            }
        }
        return null;
    }

    // ── Settlement Lookup ─────────────────────────────────────────────

    /**
     * Returns all settlements defined in the blueprint.
     * Each settlement has a fixed canonical (x, z) coordinate.
     */
    public static List<JsonObject> getSettlements() {
        JsonObject bp = ensureLoaded();
        if (bp == null) return List.of();
        List<JsonObject> result = new ArrayList<>();
        for (JsonElement el : bp.getAsJsonArray("settlements")) {
            result.add(el.getAsJsonObject());
        }
        return result;
    }

    /**
     * Finds a settlement by ID.
     */
    public static Optional<JsonObject> getSettlement(String id) {
        return getSettlements().stream()
                .filter(s -> s.get("id").getAsString().equals(id))
                .findFirst();
    }

    /**
     * Returns settlements within a given chunk range of a coordinate.
     * Used by the CanonGeographyPlacer to decide which settlements to build
     * when a chunk loads.
     *
     * @param centerX world X
     * @param centerZ world Z
     * @param radius  search radius in blocks
     */
    public static List<JsonObject> getSettlementsNear(int centerX, int centerZ, int radius) {
        List<JsonObject> near = new ArrayList<>();
        for (JsonObject s : getSettlements()) {
            if (!s.has("x") || !s.has("z")) continue;
            int sx = s.get("x").getAsInt();
            int sz = s.get("z").getAsInt();
            int dx = sx - centerX;
            int dz = sz - centerZ;
            if (dx * dx + dz * dz <= radius * radius) {
                near.add(s);
            }
        }
        return near;
    }

    /** Gets the player spawn point from the blueprint (the spawn settlement + offset). */
    public static int[] getSpawnPoint() {
        JsonObject bp = ensureLoaded();
        if (bp == null) return new int[]{3842, 70, -1184}; // fallback

        for (JsonElement el : bp.getAsJsonArray("settlements")) {
            JsonObject s = el.getAsJsonObject();
            if (s.has("is_spawn_point") && s.get("is_spawn_point").getAsBoolean()) {
                int baseX = s.get("x").getAsInt();
                int baseZ = s.get("z").getAsInt();
                int dx = 0, dz = 0;
                if (s.has("spawn_offset_from_center")) {
                    JsonObject off = s.getAsJsonObject("spawn_offset_from_center");
                    dx = off.get("dx").getAsInt();
                    dz = off.get("dz").getAsInt();
                }
                return new int[]{baseX + dx, 70, baseZ + dz};
            }
        }
        return new int[]{3842, 70, -1184}; // fallback
    }

    // ── Geography Lookups ─────────────────────────────────────────────

    /** Returns all mountain ranges. */
    public static List<JsonObject> getMountainRanges() {
        return getGeographyList("mountain_ranges");
    }

    /** Returns all rivers. */
    public static List<JsonObject> getRivers() {
        return getGeographyList("rivers");
    }

    /** Returns all spirit veins. */
    public static List<JsonObject> getSpiritVeins() {
        return getGeographyList("spirit_veins");
    }

    /** Returns all roads. */
    public static List<JsonObject> getRoads() {
        return getGeographyList("roads");
    }

    private static List<JsonObject> getGeographyList(String key) {
        JsonObject bp = ensureLoaded();
        if (bp == null || !bp.has("geography")) return List.of();
        JsonObject geo = bp.getAsJsonObject("geography");
        if (!geo.has(key)) return List.of();
        List<JsonObject> result = new ArrayList<>();
        for (JsonElement el : geo.getAsJsonArray(key)) {
            result.add(el.getAsJsonObject());
        }
        return result;
    }

    /** Returns all restriction zones. */
    public static List<JsonObject> getRestrictions() {
        JsonObject bp = ensureLoaded();
        if (bp == null) return List.of();
        List<JsonObject> result = new ArrayList<>();
        for (JsonElement el : bp.getAsJsonArray("restrictions")) {
            result.add(el.getAsJsonObject());
        }
        return result;
    }

    /** Checks if a coordinate is inside any restriction zone. */
    public static JsonObject getRestrictionAt(int x, int z) {
        for (JsonObject r : getRestrictions()) {
            JsonArray center = r.getAsJsonArray("center");
            int cx = center.get(0).getAsInt();
            int cz = center.get(1).getAsInt();
            int radius = r.get("radius").getAsInt();
            int dx = x - cx, dz = z - cz;
            if (dx * dx + dz * dz <= radius * radius) {
                return r;
            }
        }
        return null;
    }
}
