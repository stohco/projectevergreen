package dev.ergenverse.flora;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.perception.PerceptionTier;
import net.minecraft.util.RandomSource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FloraSpecies — the data model for a single plant species.
 *
 * <p>Loaded from JSON at {@code /data/ergenverse/flora/<id>.json}. A species
 * defines everything objective about a plant: how it grows, where it grows,
 * what it yields, what it attracts/repels, what perception tier identifies it,
 * and the canon confidence in the species' existence.
 *
 * <p>The Prime Directive: a species exists objectively. A mortal cannot
 * identify it (sees "a weed"), a Qi Condensation cultivator recognizes
 * "a spirit herb," a Nascent Soul cultivator sees "a Frost Herb, 300 years
 * old, of the Water element." The species doesn't change — the perception
 * does.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public final class FloraSpecies {

    // ── Fields ───────────────────────────────────────────────────────

    public final String speciesId;
    public final String name;
    public final String nameCn;
    public final FloraRenderTier renderTier;
    public final String defaultElement;
    public final float baseMedicinalPotency;
    public final float baseQiSaturation;
    public final BiologicalStage minStageToHarvest;
    public final String harvestItemId;
    public final String fruitItemId;       // nullable
    public final long growthDurationTicks; // SEED → MATURE
    public final long fruitingDurationTicks;
    public final float requiresQiDensity;  // 0 for mundane
    public final List<String> biomeWhitelist;
    public final List<String> ecologyAttracts;
    public final List<String> ecologyRepels;
    public final boolean canMutate;
    public final float mutationChance;
    public final PerceptionTier perceptionTierToIdentify;
    public final int canonConfidence;      // 1-5
    public final String description;

    // ── Constructor ──────────────────────────────────────────────────

    public FloraSpecies(String speciesId, String name, String nameCn, FloraRenderTier renderTier,
                        String defaultElement, float baseMedicinalPotency, float baseQiSaturation,
                        BiologicalStage minStageToHarvest, String harvestItemId, String fruitItemId,
                        long growthDurationTicks, long fruitingDurationTicks, float requiresQiDensity,
                        List<String> biomeWhitelist, List<String> ecologyAttracts, List<String> ecologyRepels,
                        boolean canMutate, float mutationChance, PerceptionTier perceptionTierToIdentify,
                        int canonConfidence, String description) {
        this.speciesId = speciesId;
        this.name = name;
        this.nameCn = nameCn;
        this.renderTier = renderTier;
        this.defaultElement = defaultElement;
        this.baseMedicinalPotency = baseMedicinalPotency;
        this.baseQiSaturation = baseQiSaturation;
        this.minStageToHarvest = minStageToHarvest;
        this.harvestItemId = harvestItemId;
        this.fruitItemId = fruitItemId;
        this.growthDurationTicks = growthDurationTicks;
        this.fruitingDurationTicks = fruitingDurationTicks;
        this.requiresQiDensity = requiresQiDensity;
        this.biomeWhitelist = biomeWhitelist == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(biomeWhitelist));
        this.ecologyAttracts = ecologyAttracts == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(ecologyAttracts));
        this.ecologyRepels = ecologyRepels == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(ecologyRepels));
        this.canMutate = canMutate;
        this.mutationChance = mutationChance;
        this.perceptionTierToIdentify = perceptionTierToIdentify;
        this.canonConfidence = canonConfidence;
        this.description = description;
    }

    // ── Queries ──────────────────────────────────────────────────────

    /** True if the plant can be harvested at this stage (must meet minimum + stage permits). */
    public boolean isHarvestableAt(BiologicalStage stage) {
        return stage.canHarvest && stage.order >= minStageToHarvest.order;
    }

    /** Yield multiplier at a given stage (delegates to {@link BiologicalStage#yieldMultiplier}). */
    public float getYieldMultiplier(BiologicalStage stage) {
        return stage.yieldMultiplier;
    }

    // ── JSON (de)serialization ───────────────────────────────────────

    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("speciesId", speciesId);
        o.addProperty("name", name);
        o.addProperty("nameCn", nameCn);
        o.addProperty("renderTier", renderTier.name());
        o.addProperty("defaultElement", defaultElement);
        o.addProperty("baseMedicinalPotency", baseMedicinalPotency);
        o.addProperty("baseQiSaturation", baseQiSaturation);
        o.addProperty("minStageToHarvest", minStageToHarvest.label);
        o.addProperty("harvestItemId", harvestItemId);
        if (fruitItemId != null) o.addProperty("fruitItemId", fruitItemId);
        o.addProperty("growthDurationTicks", growthDurationTicks);
        o.addProperty("fruitingDurationTicks", fruitingDurationTicks);
        o.addProperty("requiresQiDensity", requiresQiDensity);
        if (!biomeWhitelist.isEmpty()) o.add("biomeWhitelist", stringArray(biomeWhitelist));
        if (!ecologyAttracts.isEmpty()) o.add("ecologyAttracts", stringArray(ecologyAttracts));
        if (!ecologyRepels.isEmpty()) o.add("ecologyRepels", stringArray(ecologyRepels));
        o.addProperty("canMutate", canMutate);
        o.addProperty("mutationChance", mutationChance);
        o.addProperty("perceptionTierToIdentify", perceptionTierToIdentify.name());
        o.addProperty("canonConfidence", canonConfidence);
        o.addProperty("description", description);
        return o;
    }

    public static FloraSpecies fromJson(JsonObject o) {
        String fruitId = o.has("fruitItemId") && !o.get("fruitItemId").isJsonNull()
                ? o.get("fruitItemId").getAsString() : null;
        return new FloraSpecies(
                str(o, "speciesId", "unknown"),
                str(o, "name", "Unnamed"),
                str(o, "nameCn", ""),
                FloraRenderTier.fromName(str(o, "renderTier", "CROSS_SPRITE")),
                str(o, "defaultElement", "WOOD"),
                (float) num(o, "baseMedicinalPotency", 0.5),
                (float) num(o, "baseQiSaturation", 0.5),
                BiologicalStage.fromName(str(o, "minStageToHarvest", "mature")),
                str(o, "harvestItemId", ""),
                fruitId,
                (long) num(o, "growthDurationTicks", 96000),
                (long) num(o, "fruitingDurationTicks", 24000),
                (float) num(o, "requiresQiDensity", 0.0),
                strArray(o, "biomeWhitelist"),
                strArray(o, "ecologyAttracts"),
                strArray(o, "ecologyRepels"),
                bool(o, "canMutate", false),
                (float) num(o, "mutationChance", 0.0),
                PerceptionTier.valueOf(str(o, "perceptionTierToIdentify", "MORTAL")),
                (int) num(o, "canonConfidence", 3),
                str(o, "description", "")
        );
    }

    // ── Registry ─────────────────────────────────────────────────────

    /** Loaded species, keyed by speciesId. Populated by {@link #loadAll()}. */
    public static final Map<String, FloraSpecies> REGISTRY = new HashMap<>();

    /**
     * Curated list of known flora species IDs. In production this would be
     * read from an _index.json manifest, but for v1 we hardcode the 4
     * species shipped with the foundation.
     */
    private static final String[] KNOWN_IDS = {
            "frost_herb", "intent_herb", "sky_spirit_herb", "divine_fire_herb"
    };

    /** Load all known species from the classpath. Idempotent. */
    public static synchronized void loadAll() {
        if (!REGISTRY.isEmpty()) return;
        for (String id : KNOWN_IDS) {
            String path = "/data/ergenverse/flora/" + id + ".json";
            try (var is = FloraSpecies.class.getResourceAsStream(path)) {
                if (is == null) {
                    Ergenverse.LOGGER.warn("[Flora] Species JSON not found on classpath: {}", path);
                    continue;
                }
                Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                JsonObject o = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                FloraSpecies species = fromJson(o);
                REGISTRY.put(species.speciesId, species);
                Ergenverse.LOGGER.info("[Flora] Loaded species: {} ({})", species.speciesId, species.nameCn);
            } catch (Exception e) {
                Ergenverse.LOGGER.warn("[Flora] Failed to load {}: {}", path, e.getMessage());
            }
        }
        Ergenverse.LOGGER.info("[Flora] FloraSpecies registry: {} species loaded.", REGISTRY.size());
    }

    /** Clear the registry (called on level unload to allow fresh reload). */
    public static synchronized void clear() {
        REGISTRY.clear();
    }

    /** Look up a species by ID. Returns null if not loaded. */
    public static FloraSpecies get(String id) {
        if (id == null) return null;
        if (REGISTRY.isEmpty()) loadAll();
        return REGISTRY.get(id);
    }

    /** All loaded species (unmodifiable view). */
    public static List<FloraSpecies> all() {
        if (REGISTRY.isEmpty()) loadAll();
        return List.copyOf(REGISTRY.values());
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static JsonArray stringArray(List<String> list) {
        JsonArray a = new JsonArray();
        for (String s : list) a.add(s);
        return a;
    }

    private static String str(JsonObject o, String key, String def) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return def;
        return o.get(key).getAsString();
    }

    private static double num(JsonObject o, String key, double def) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return def;
        try { return o.get(key).getAsDouble(); } catch (Exception e) { return def; }
    }

    private static boolean bool(JsonObject o, String key, boolean def) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return def;
        try { return o.get(key).getAsBoolean(); } catch (Exception e) { return def; }
    }

    private static List<String> strArray(JsonObject o, String key) {
        if (o == null || !o.has(key) || !o.get(key).isJsonArray()) return List.of();
        List<String> out = new ArrayList<>();
        for (var el : o.getAsJsonArray(key)) out.add(el.getAsString());
        return out;
    }

    // Unused but reserved for future weighted-random spawn selection
    @SuppressWarnings("unused")
    private static FloraSpecies pickRandom(RandomSource rng) {
        if (REGISTRY.isEmpty()) loadAll();
        if (REGISTRY.isEmpty()) return null;
        List<FloraSpecies> list = new ArrayList<>(REGISTRY.values());
        return list.get(rng.nextInt(list.size()));
    }
}
