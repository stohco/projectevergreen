package dev.ergenverse.simulation.residence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.ergenverse.core.Ergenverse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ResidentProfileLoader — reads resident profile JSON files from the mod's
 * classpath resources and produces ResidentProfile records.
 *
 * <p>Profile files live at:
 * {@code /data/ergenverse/residents/{settlement_id}/{resident_id}.json}
 *
 * <p>Loading strategy matches {@code WorldStateDataLoader}: resources are
 * packaged into the mod JAR and accessed via
 * {@code ClassLoader.getResourceAsStream()}. An index file lists available
 * profiles per settlement.
 *
 * <p>Per Article XXVI: this is NOT a new Engine. It is a data loader that
 * feeds the existing ResidenceManifestBuilder. No new infrastructure.
 *
 * <h2>Usage</h2>
 * <pre>
 *   Map&lt;String, ResidentProfile&gt; all = ResidentProfileLoader.loadAll();
 *   ResidentProfile wangLin = all.get("wang_lin");
 * </pre>
 */
public final class ResidentProfileLoader {

    private static final String DATA_PREFIX = "/data/ergenverse/residents/";

    /** Cached profiles after first load. */
    private static Map<String, ResidentProfile> cachedProfiles = null;

    private ResidentProfileLoader() {}

    /**
     * Load all resident profiles from the classpath.
     * Scans index files under data/ergenverse/residents/ for settlement folders.
     *
     * @return map of residentId → ResidentProfile
     */
    public static Map<String, ResidentProfile> loadAll() {
        if (cachedProfiles != null) return cachedProfiles;

        Map<String, ResidentProfile> profiles = new HashMap<>();

        // Try loading from known settlements
        String[] knownSettlements = {"wang_family_village"};
        for (String settlement : knownSettlements) {
            loadSettlement(profiles, settlement);
        }

        cachedProfiles = profiles;
        Ergenverse.LOGGER.info("[ResidentProfileLoader] Loaded {} resident profiles", profiles.size());
        return profiles;
    }

    /**
     * Load a single resident profile by settlement and resident ID.
     *
     * @param settlementId the settlement folder name (e.g. "wang_family_village")
     * @param residentId   the resident file name without .json (e.g. "wang_lin")
     * @return the parsed profile, or null if not found
     */
    public static ResidentProfile loadOne(String settlementId, String residentId) {
        String path = DATA_PREFIX + settlementId + "/" + residentId + ".json";
        try (InputStream in = ResidentProfileLoader.class.getResourceAsStream(path)) {
            if (in == null) {
                Ergenverse.LOGGER.warn("[ResidentProfileLoader] Profile not found: {}", path);
                return null;
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                return parseProfile(json);
            }
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[ResidentProfileLoader] Error loading {}: {}", path, e.getMessage());
            return null;
        }
    }

    /**
     * Load all profiles from a settlement directory.
     * Looks for an index file: data/ergenverse/residents/{settlement}/_index.json
     * listing resident IDs. Falls back to known profiles if no index exists.
     */
    private static void loadSettlement(Map<String, ResidentProfile> profiles, String settlement) {
        String indexPath = DATA_PREFIX + settlement + "/_index.json";
        List<String> residentIds;

        try (InputStream in = ResidentProfileLoader.class.getResourceAsStream(indexPath)) {
            if (in != null) {
                try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    JsonArray arr = JsonParser.parseReader(reader).getAsJsonArray();
                    residentIds = new ArrayList<>();
                    for (JsonElement el : arr) {
                        residentIds.add(el.getAsString());
                    }
                }
            } else {
                // Fallback: try known profiles for this settlement
                residentIds = fallbackResidents(settlement);
            }
        } catch (Exception e) {
            residentIds = fallbackResidents(settlement);
        }

        for (String id : residentIds) {
            ResidentProfile profile = loadOne(settlement, id);
            if (profile != null) {
                profiles.put(profile.residentId(), profile);
            }
        }
    }

    /** Fallback resident IDs when no index file exists. */
    private static List<String> fallbackResidents(String settlement) {
        return switch (settlement) {
            case "wang_family_village" -> List.of("wang_lin", "old_chen", "wang_tianlong");
            default -> List.of();
        };
    }

    /**
     * Deserialize a JSON object into a ResidentProfile record.
     * Maps each JSON field to the corresponding record component.
     */
    static ResidentProfile parseProfile(JsonObject json) {
        String residentId = json.has("residentId") ? json.get("residentId").getAsString() : "unknown";
        String displayName = json.has("displayName") ? json.get("displayName").getAsString() : "Unknown";
        String settlementId = json.has("settlementId") ? json.get("settlementId").getAsString() : "unknown";
        String occupation = json.has("occupation") ? json.get("occupation").getAsString() : "none";
        String cultivationStyle = json.has("cultivationStyle") ? json.get("cultivationStyle").getAsString() : "none";
        boolean canonSourced = json.has("canonSourced") && json.get("canonSourced").getAsBoolean();

        List<String> personalityTraits = parseStringList(json, "personalityTraits");
        List<String> inventory = parseStringList(json, "inventory");
        List<String> fears = parseStringList(json, "fears");
        List<String> habits = parseStringList(json, "habits");
        List<String> relationships = parseStringList(json, "relationships");

        // Needs: JSON strings → NeedCategory enums
        List<NeedCategory> needs = new ArrayList<>();
        if (json.has("needs")) {
            for (JsonElement el : json.getAsJsonArray("needs")) {
                try {
                    needs.add(NeedCategory.valueOf(el.getAsString()));
                } catch (IllegalArgumentException e) {
                    Ergenverse.LOGGER.warn("[ResidentProfileLoader] Unknown need '{}' for resident {}",
                            el.getAsString(), residentId);
                }
            }
        }

        // History: JSON objects → ResidenceMemory records
        List<ResidenceMemory> history = new ArrayList<>();
        if (json.has("history")) {
            for (JsonElement el : json.getAsJsonArray("history")) {
                JsonObject obj = el.getAsJsonObject();
                String event = obj.has("event") ? obj.get("event").getAsString() : "";
                String location = obj.has("location") ? obj.get("location").getAsString() : "";
                String evidence = obj.has("evidence") ? obj.get("evidence").getAsString() : "inferred";
                String emotionalWeight = obj.has("emotionalWeight") ? obj.get("emotionalWeight").getAsString() : "";
                history.add(new ResidenceMemory(event, location, evidence, emotionalWeight));
            }
        }

        return new ResidentProfile(residentId, displayName, settlementId, occupation,
                personalityTraits, cultivationStyle, needs, inventory, fears, habits,
                relationships, history, canonSourced);
    }

    private static List<String> parseStringList(JsonObject json, String field) {
        List<String> list = new ArrayList<>();
        if (json.has(field)) {
            for (JsonElement el : json.getAsJsonArray(field)) {
                list.add(el.getAsString());
            }
        }
        return list;
    }
}
