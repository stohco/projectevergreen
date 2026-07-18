package dev.ergenverse.simulation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ergenverse.core.Ergenverse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * EnrichedCanonLoader — loads enriched canon JSON files at runtime.
 *
 * <p>The Ergenverse canon database ships as a set of JSON files in
 * {@code data/ergenverse/canon/}. Each file is a single {@link JsonObject}
 * containing one canon entry (NPC, sect, technique, item, formation, etc.).
 *
 * <p>This loader reads them all into a static registry at bootstrap so other
 * systems can look them up by id without re-reading files.
 */
public final class EnrichedCanonLoader {

    private static final Map<String, JsonObject> ENTRIES = new HashMap<>();
    private static final Gson GSON = new Gson();

    private EnrichedCanonLoader() {}

    /**
     * Load a single canon entry from {@code data/ergenverse/canon/<id>.json}.
     * Returns null if the file does not exist or cannot be parsed.
     */
    public static JsonObject load(String id) {
        // Check cache.
        JsonObject cached = ENTRIES.get(id);
        if (cached != null) return cached;

        String path = "/data/ergenverse/canon/" + id + ".json";
        try (InputStream in = EnrichedCanonLoader.class.getResourceAsStream(path)) {
            if (in == null) {
                Ergenverse.LOGGER.warn("[Ergenverse] EnrichedCanonLoader: missing {}", path);
                return null;
            }
            try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonElement el = GSON.fromJson(reader, JsonElement.class);
                if (el == null || !el.isJsonObject()) {
                    Ergenverse.LOGGER.warn("[Ergenverse] EnrichedCanonLoader: {} is not a JSON object.", path);
                    return null;
                }
                JsonObject obj = el.getAsJsonObject();
                ENTRIES.put(id, obj);
                return obj;
            }
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[Ergenverse] EnrichedCanonLoader: failed to load {}: {}", path, e.getMessage());
            return null;
        }
    }

    /** Cached lookup. */
    public static JsonObject get(String id) {
        return ENTRIES.get(id);
    }

    /** Pre-load a batch of canon entries. */
    public static void preload(java.util.Collection<String> ids) {
        for (String id : ids) load(id);
    }

    public static int count() {
        return ENTRIES.size();
    }

    public static void clear() {
        ENTRIES.clear();
    }
}
