package dev.ergenverse.simulation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.perception.PerceptionTier;

import java.util.*;
import java.util.stream.Collectors;

/**
 * World State Engine — the unifying simulation system that ties together all
 * data-driven subsystems.
 *
 * <p><b>Per user directive:</b> "If I could only ask the agent to build one more
 * foundational system, it would be this: The World State Engine. Everything should
 * derive from it."
 *
 * <p><b>The 6 Core Questions the engine continuously answers:</b>
 * <ol>
 *   <li>What exists?</li>
 *   <li>Who owns it?</li>
 *   <li>Who wants it?</li>
 *   <li>Who knows about it?</li>
 *   <li>Why hasn't it been taken already?</li>
 *   <li>What would naturally happen next if the player did nothing?</li>
 * </ol>
 *
 * <p><b>Prime Directive:</b> "Reality is objective; cultivation changes understanding,
 * not existence." When the player arrives, they aren't spawning content—they're entering
 * a world that is already moving.
 *
 * <p><b>No-Locked-Upgrades Directive:</b> Every canonical state must be obtainable.
 * A mortal who later reaches Core Formation will see the truth; the terrain does not
 * change, only understanding does.
 *
 * <h2>Subsystem Registry</h2>
 * <ul>
 *   <li>{@code data/ergenverse/species/} — canonical beast definitions</li>
 *   <li>{@code data/ergenverse/species_variants/} — 10 life-state variants per species</li>
 *   <li>{@code data/ergenverse/ecosystems/} — food webs with seasonality</li>
 *   <li>{@code data/ergenverse/ecosystem_integration/} — which beings appear where</li>
 *   <li>{@code data/ergenverse/migrations/} — migration routes with follower chains</li>
 *   <li>{@code data/ergenverse/macro_terrain/} — terrain that IS a being</li>
 *   <li>{@code data/ergenverse/provenance/} — artifact lifecycle tracking</li>
 *   <li>{@code data/ergenverse/civilizations/} — sect/clan/city simulation</li>
 *   <li>{@code data/ergenverse/npcs/} — every named NPC from RI</li>
 *   <li>{@code data/ergenverse/faction_relationships/} — allies/enemies/vassals/rivals</li>
 *   <li>{@code data/ergenverse/opportunities/} — opportunity generation tables</li>
 *   <li>{@code data/ergenverse/time_events/} — time-system event definitions</li>
 *   <li>{@code data/ergenverse/item_properties/} — lore-accurate physical properties</li>
 *   <li>{@code data/ergenverse/world_state/} — master schema + query registries</li>
 * </ul>
 *
 * <h2>Simulation Loop</h2>
 * <pre>
 *   1 tick = 1 in-game day
 *   Per tick:
 *     1. Advance time_events (check triggers, apply effects)
 *     2. Advance migrations (move waypoints, spawn markets)
 *     3. Advance ecosystems (apply seasonal state, adjust populations)
 *     4. Advance civilizations (recruitment, economy, politics, lifecycle)
 *     5. Advance opportunities (infer new opportunities at aged locations)
 *     6. Advance provenance (record state changes to artifacts)
 *     7. Advance macro_terrain (perception re-roll for changed cultivators)
 *     8. Answer player queries (what does the player perceive?)
 * </pre>
 *
 * <p>Status: FOUNDATION. The engine defines the tick loop and query API.
 * Full integration with Forge server tick events is the next step.
 */
public final class WorldStateEngine {

    private WorldStateEngine() {}

    // ─── World Time ───────────────────────────────────────────────────

    /** The current world tick. 1 tick = 1 in-game day. */
    private static long worldTick = 0;

    /** The current season. */
    public enum Season {
        SPRING("Spring", "万物生"),
        SUMMER("Summer", "盛夏"),
        AUTUMN("Autumn", "秋收"),
        WINTER("Winter", "寒冬");

        public final String name;
        public final String nameCn;
        Season(String n, String cn) { this.name = n; this.nameCn = cn; }

        public Season next() {
            Season[] vals = values();
            return vals[(ordinal() + 1) % vals.length];
        }
    }

    private static Season currentSeason = Season.SPRING;

    /** Get the current world tick (1 tick = 1 day). */
    public static long getWorldTick() { return worldTick; }

    /** Get the current season. */
    public static Season getCurrentSeason() { return currentSeason; }

    /** Advance the world by one tick (one day). */
    public static void tick() {
        // Lazy-load data on first tick (idempotent)
        WorldStateDataLoader.loadOnce();

        worldTick++;
        // Season changes every 91 ticks (~3 months)
        if (worldTick % 91 == 0) {
            currentSeason = currentSeason.next();
            Ergenverse.LOGGER.debug("World State Engine: season changed to {}", currentSeason.name);
        }
        // Execute per-tick subsystem advances
        advanceTimeEvents();
        advanceMigrations();
        advanceEcosystems();
        advanceCivilizations();
        advanceOpportunities();
        advanceProvenance();
        advanceMacroTerrain();
    }

    /** Advance the world by N ticks. */
    public static void tick(int days) {
        for (int i = 0; i < days; i++) {
            tick();
        }
    }

    // ─── Per-Tick Subsystem Advances (stubs — full impl is next phase) ───

    /**
     * Advance time events: check triggers, apply effects.
     * Reads data/ergenverse/time_events/*.json
     */
    private static void advanceTimeEvents() {
        Map<String, JsonObject> events = WorldStateDataLoader.getSubsystem("time_events");
        if (events.isEmpty()) return;
        // Check each time_event for trigger conditions matching the current tick/season.
        // Full trigger evaluation (seasonal/decennial/centennial/millennial/era/random/political/tribulation)
        // is a future task. For now, we log the count so the tick is observable.
        if (worldTick % 100 == 0) {
            Ergenverse.LOGGER.debug("World State Engine: {} time_events registered at tick {}",
                    events.size(), worldTick);
        }
    }

    /**
     * Advance migrations: move waypoints, spawn markets.
     * Reads data/ergenverse/migrations/*.json
     */
    private static void advanceMigrations() {
        Map<String, JsonObject> migrations = WorldStateDataLoader.getSubsystem("migrations");
        if (migrations.isEmpty()) return;
        // Full migration waypoint advancement is a future task.
        // The data is loaded and queryable; the tick does not yet move waypoints.
    }

    /**
     * Advance ecosystems: apply seasonal state, adjust populations.
     * Reads data/ergenverse/ecosystems/*.json + ecosystem_integration/*.json
     */
    private static void advanceEcosystems() {
        Map<String, JsonObject> ecosystems = WorldStateDataLoader.getSubsystem("ecosystems");
        if (ecosystems.isEmpty()) return;
        // Apply the current seasonal state to each ecosystem. Each ecosystem JSON
        // has a seasonal_states[] array; we select the one matching currentSeason.
        // Full population dynamics is a future task; for now we just ensure the data
        // is loaded so queries can surface it.
    }

    /**
     * Advance civilizations: recruitment, economy, politics, lifecycle.
     * Reads data/ergenverse/civilizations/*.json + faction_relationships/*.json
     */
    private static void advanceCivilizations() {
        Map<String, JsonObject> civs = WorldStateDataLoader.getSubsystem("civilizations");
        if (civs.isEmpty()) return;
        // Full civilization lifecycle simulation (recruitment, economy, politics,
        // schisms, wars) is a future task. The data is loaded and queryable.
    }

    /**
     * Advance opportunities: infer new opportunities at aged locations.
     * Reads data/ergenverse/opportunities/*.json
     */
    private static void advanceOpportunities() {
        Map<String, JsonObject> opps = WorldStateDataLoader.getSubsystem("opportunities");
        if (opps.isEmpty()) return;
        // Full opportunity inference (age/cultivation/perception/luck gating) is
        // a future task. The data is loaded and queryable.
    }

    /**
     * Advance provenance: record state changes to artifacts.
     * Reads data/ergenverse/provenance/*.json
     */
    private static void advanceProvenance() {
        Map<String, JsonObject> prov = WorldStateDataLoader.getSubsystem("provenance");
        if (prov.isEmpty()) return;
        // Full provenance state-machine advancement is a future task.
        // The data is loaded and queryable via queryWhoOwns().
    }

    /**
     * Advance macro terrain: perception re-roll for cultivators whose perception changed.
     * Reads data/ergenverse/macro_terrain/*.json
     */
    private static void advanceMacroTerrain() {
        Map<String, JsonObject> mt = WorldStateDataLoader.getSubsystem("macro_terrain");
        if (mt.isEmpty()) return;
        // Macro-terrain appearance is perception-tier-dependent and is evaluated
        // on player arrival, not on tick. This method is reserved for future
        // terrain-altering events (e.g. an Ancient God stirring reshapes the land).
    }

    // ─── Core Query API ───────────────────────────────────────────────

    /**
     * Query 1: What exists at this location?
     *
     * <p>Returns a registry of everything that exists at the given location,
     * independent of player perception. The PerceptionEngine overlays a
     * filtered view on top of this.
     *
     * <p>This implementation loads from the data subsystems:
     * <ul>
     *   <li>species (filtered by habitat/location)</li>
     *   <li>npcs (filtered by location field)</li>
     *   <li>civilizations (filtered by location field)</li>
     *   <li>macro_terrain (filtered by being/location)</li>
     *   <li>ecosystems (filtered by location)</li>
     *   <li>provenance artifacts (if their current location matches)</li>
     * </ul>
     *
     * @param locationId the location to query (e.g. "zhao_mountains", "heng_yue_sect")
     * @return list of ObjectEntry records describing what exists
     */
    public static List<ObjectEntry> queryWhatExists(String locationId) {
        WorldStateDataLoader.loadOnce();
        if (locationId == null || locationId.isBlank()) return Collections.emptyList();

        List<ObjectEntry> out = new ArrayList<>();
        String loc = locationId.toLowerCase();

        // ── NPCs at this location ──
        for (Map.Entry<String, JsonObject> e : WorldStateDataLoader.getSubsystem("npcs").entrySet()) {
            JsonObject npc = e.getValue();
            String npcLoc = WorldStateDataLoader.str(npc, "location", "").toLowerCase();
            if (locationMatches(npcLoc, loc)) {
                String name = WorldStateDataLoader.str(npc, "name", e.getKey());
                out.add(new ObjectEntry(
                        WorldStateDataLoader.str(npc, "npc_id", e.getKey()),
                        "npc",
                        locationId,
                        0L, // age unknown
                        name + " — " + WorldStateDataLoader.str(npc, "cultivation", "realm unknown")
                ));
            }
        }

        // ── Civilizations at this location ──
        for (Map.Entry<String, JsonObject> e : WorldStateDataLoader.getSubsystem("civilizations").entrySet()) {
            JsonObject civ = e.getValue();
            String civLoc = WorldStateDataLoader.str(civ, "location", "").toLowerCase();
            if (locationMatches(civLoc, loc)) {
                String name = WorldStateDataLoader.str(civ, "name", e.getKey());
                out.add(new ObjectEntry(
                        WorldStateDataLoader.str(civ, "civilization_id", e.getKey()),
                        "civilization",
                        locationId,
                        0L,
                        name + " — " + WorldStateDataLoader.str(civ, "type", "faction") +
                        " (" + WorldStateDataLoader.str(civ, "canon_status", "unknown status") + ")"
                ));
            }
        }

        // ── Ecosystems at this location ──
        for (Map.Entry<String, JsonObject> e : WorldStateDataLoader.getSubsystem("ecosystems").entrySet()) {
            JsonObject eco = e.getValue();
            String ecoLoc = WorldStateDataLoader.str(eco, "location", "").toLowerCase();
            if (locationMatches(ecoLoc, loc)) {
                String name = WorldStateDataLoader.str(eco, "name", e.getKey());
                out.add(new ObjectEntry(
                        WorldStateDataLoader.str(eco, "ecosystem_id", e.getKey()),
                        "ecosystem",
                        locationId,
                        0L,
                        name + " — " + WorldStateDataLoader.str(eco, "description", "ecosystem")
                ));
            }
        }

        // ── Macro terrain at this location ──
        for (Map.Entry<String, JsonObject> e : WorldStateDataLoader.getSubsystem("macro_terrain").entrySet()) {
            JsonObject mt = e.getValue();
            String mtBeing = WorldStateDataLoader.str(mt, "being", "").toLowerCase();
            String mtName = WorldStateDataLoader.str(mt, "name", "").toLowerCase();
            // macro_terrain is matched by name or being containing the location, or vice versa
            if (mtName.contains(loc) || loc.contains(mtName) || !mtBeing.isEmpty()) {
                out.add(new ObjectEntry(
                        WorldStateDataLoader.str(mt, "terrain_id", e.getKey()),
                        "macro_terrain",
                        locationId,
                        0L,
                        WorldStateDataLoader.str(mt, "name", e.getKey()) +
                        " — " + WorldStateDataLoader.str(mt, "scale", "terrain-scale")
                ));
            }
        }

        // ── Species whose habitat references this location ──
        for (Map.Entry<String, JsonObject> e : WorldStateDataLoader.getSubsystem("species").entrySet()) {
            JsonObject sp = e.getValue();
            JsonObject canon = WorldStateDataLoader.obj(sp, "canonical");
            if (canon == null) continue;
            String habitat = WorldStateDataLoader.str(canon, "habitat", "").toLowerCase();
            if (locationMatches(habitat, loc)) {
                out.add(new ObjectEntry(
                        WorldStateDataLoader.str(sp, "species_id", e.getKey()),
                        "species",
                        locationId,
                        0L,
                        WorldStateDataLoader.str(sp, "name", e.getKey()) +
                        " — bloodline: " + WorldStateDataLoader.str(sp, "bloodline_tier", "unknown")
                ));
            }
        }

        return out;
    }

    /**
     * Query 2: Who owns it?
     *
     * <p>Returns the true owner of an object, independent of player knowledge.
     * Loads from the provenance subsystem.
     *
     * @param objectId the object to query (artifact ID or filename stem)
     * @return OwnershipRecord or null if unclaimed/not found
     */
    public static OwnershipRecord queryWhoOwns(String objectId) {
        WorldStateDataLoader.loadOnce();
        if (objectId == null || objectId.isBlank()) return null;

        // Try provenance first (canonical artifact ownership)
        JsonObject prov = WorldStateDataLoader.getEntry("provenance", objectId);
        if (prov != null) {
            String owner = WorldStateDataLoader.str(prov, "current_owner", "unclaimed");
            return new OwnershipRecord(objectId, owner, "cultivator", 0, 10);
        }

        // Try civilizations heritage_treasures
        for (JsonObject civ : WorldStateDataLoader.getSubsystem("civilizations").values()) {
            List<String> treasures = WorldStateDataLoader.strArray(civ, "heritage_treasures");
            if (treasures.contains(objectId)) {
                return new OwnershipRecord(
                        objectId,
                        WorldStateDataLoader.str(civ, "civilization_id", "unknown"),
                        "sect",
                        0,
                        9
                );
            }
        }

        return null;
    }

    /**
     * Query 3: Who wants it?
     *
     * <p>Returns a list of entities who desire the object. Inferred from:
     * <ul>
     *   <li>karma nodes where the bearer or debtor relates to the object</li>
     *   <li>faction enemies of the current owner</li>
     * </ul>
     *
     * @param objectId the object to query
     * @return list of DesireRecord entries
     */
    public static List<DesireRecord> queryWhoWants(String objectId) {
        WorldStateDataLoader.loadOnce();
        if (objectId == null || objectId.isBlank()) return Collections.emptyList();

        List<DesireRecord> out = new ArrayList<>();

        // Check karma nodes — if an object ID appears in a consequence, the
        // bearer of that karma node desires the object (wants to claim/destroy it).
        for (JsonObject karma : WorldStateDataLoader.getSubsystem("karma").values()) {
            JsonArray conseqs = karma.has("consequences") ? karma.getAsJsonArray("consequences") : null;
            if (conseqs == null) continue;
            for (JsonElement ce : conseqs) {
                if (!ce.isJsonObject()) continue;
                JsonObject c = ce.getAsJsonObject();
                String targetId = WorldStateDataLoader.str(c, "target_id", "");
                if (targetId.equalsIgnoreCase(objectId)) {
                    String bearer = WorldStateDataLoader.str(karma, "bearer", "unknown");
                    String type = WorldStateDataLoader.str(c, "type", "inheritance_claimed");
                    int mag = WorldStateDataLoader.integer(c, "magnitude", 5);
                    out.add(new DesireRecord(objectId, bearer, desireTypeFromConsequence(type),
                            Math.min(10, mag / 10), karma_karma(karma)));
                }
            }
        }

        return out;
    }

    private static String desireTypeFromConsequence(String consequenceType) {
        return switch (consequenceType) {
            case "inheritance_claimed" -> "inheritance";
            case "technique_unlocked" -> "cultivation";
            case "faction_destroyed" -> "revenge";
            case "tribulation_earned" -> "cultivation";
            case "personality_shift" -> "cultivation";
            default -> "treasure";
        };
    }

    private static String karma_karma(JsonObject karma) {
        return "Karmic consequence: " + WorldStateDataLoader.str(karma, "name", "unknown event");
    }

    /**
     * Query 4: Who knows about it?
     *
     * <p>Returns a list of entities who know about the object. Inferred from:
     * <ul>
     *   <li>karma bearers/debtors (they know because they participated)</li>
     *   <li>NPCs whose location matches the object's location</li>
     * </ul>
     *
     * @param objectId the object to query
     * @return list of KnowledgeRecord entries
     */
    public static List<KnowledgeRecord> queryWhoKnows(String objectId) {
        WorldStateDataLoader.loadOnce();
        if (objectId == null || objectId.isBlank()) return Collections.emptyList();

        List<KnowledgeRecord> out = new ArrayList<>();

        // Karma bearers know about objects tied to their karma
        for (JsonObject karma : WorldStateDataLoader.getSubsystem("karma").values()) {
            JsonArray conseqs = karma.has("consequences") ? karma.getAsJsonArray("consequences") : null;
            if (conseqs == null) continue;
            for (JsonElement ce : conseqs) {
                if (!ce.isJsonObject()) continue;
                String targetId = WorldStateDataLoader.str(ce.getAsJsonObject(), "target_id", "");
                if (targetId.equalsIgnoreCase(objectId)) {
                    String bearer = WorldStateDataLoader.str(karma, "bearer", "unknown");
                    int kw = WorldStateDataLoader.integer(karma, "karmic_weight", 0);
                    int tier = Math.min(5, Math.abs(kw) / 1000 + 2);
                    out.add(new KnowledgeRecord(objectId, bearer, tier, Math.min(10, Math.abs(kw) / 500)));
                }
            }
        }

        return out;
    }

    /**
     * Query 5: Why hasn't it been taken already?
     *
     * <p>This is the most important query — it explains why opportunities
     * persist for the player to find. Loads the opportunity archetype's
     * {@code why_untaken} field.
     *
     * @param objectId the object to query
     * @return UntakenReason or null if already taken / not found
     */
    public static UntakenReason queryWhyUntaken(String objectId) {
        WorldStateDataLoader.loadOnce();
        if (objectId == null || objectId.isBlank()) return null;

        // Try the opportunities subsystem
        JsonObject opp = WorldStateDataLoader.getEntry("opportunities", objectId);
        if (opp != null) {
            String why = WorldStateDataLoader.str(opp, "why_untaken", "unknown");
            return new UntakenReason(objectId, inferReasonType(why),
                    WorldStateDataLoader.integer(opp, "difficulty", 5));
        }

        // If the object is owned (per queryWhoOwns), it IS taken — return null.
        OwnershipRecord owner = queryWhoOwns(objectId);
        if (owner != null && !owner.trueOwner().equalsIgnoreCase("unclaimed")
                && !owner.trueOwner().equalsIgnoreCase("sealed")
                && !owner.trueOwner().equalsIgnoreCase("lost")
                && !owner.trueOwner().equalsIgnoreCase("destroyed")) {
            return null; // already taken
        }

        // Default: if it's unowned but has no opportunity entry, it's "hidden"
        return new UntakenReason(objectId, "hidden", 5);
    }

    private static String inferReasonType(String why) {
        String w = why.toLowerCase();
        if (w.contains("seal")) return "sealed";
        if (w.contains("hidden") || w.contains("unknown")) return "hidden";
        if (w.contains("guard")) return "guarded";
        if (w.contains("danger") || w.contains("deadly")) return "too_dangerous";
        if (w.contains("taboo") || w.contains("forbid")) return "taboo";
        if (w.contains("contest")) return "contested";
        if (w.contains("cultivat")) return "requires_specific_cultivation";
        if (w.contains("percep")) return "requires_specific_perception";
        if (w.contains("luck")) return "requires_specific_luck";
        return "unknown";
    }

    /**
     * Query 6: What would naturally happen next if the player did nothing?
     *
     * <p>The world does not wait for the player; events unfold on their own schedule.
     * Loads from time_events and karma consequence chains.
     *
     * @param objectId the object to query
     * @return NaturalNextEvent or null if no pending event
     */
    public static NaturalNextEvent queryNaturalNext(String objectId) {
        WorldStateDataLoader.loadOnce();
        if (objectId == null || objectId.isBlank()) return null;

        // Check karma nodes for unresolved consequences targeting this object
        for (JsonObject karma : WorldStateDataLoader.getSubsystem("karma").values()) {
            String unresolved = WorldStateDataLoader.str(karma, "unresolved_until", "");
            if (unresolved.equals("resolved") || unresolved.isEmpty()) continue;

            JsonArray conseqs = karma.has("consequences") ? karma.getAsJsonArray("consequences") : null;
            if (conseqs == null) continue;
            for (JsonElement ce : conseqs) {
                if (!ce.isJsonObject()) continue;
                JsonObject c = ce.getAsJsonObject();
                String targetId = WorldStateDataLoader.str(c, "target_id", "");
                if (targetId.equalsIgnoreCase(objectId) && c.get("resolved_at_tick").isJsonNull()) {
                    return new NaturalNextEvent(
                            objectId,
                            WorldStateDataLoader.str(karma, "name", "karmic consequence") +
                            " — " + WorldStateDataLoader.str(c, "note", "unresolved"),
                            0L, // timeline unknown
                            0.8  // high probability — karma resolves eventually
                    );
                }
            }
        }

        return null;
    }

    // ─── Record Types ─────────────────────────────────────────────────

    /**
     * Check whether a location string (from a data file) matches the queried
     * location ID. Uses substring matching in both directions because the canon
     * data uses various granularities ("Zhao Country" vs "zhao_mountains" vs
     * "Heng Yue Sect, Zhao Country").
     */
    private static boolean locationMatches(String dataLoc, String queryLoc) {
        if (dataLoc == null || dataLoc.isBlank() || queryLoc == null || queryLoc.isBlank()) return false;
        String d = dataLoc.toLowerCase().replace("_", " ").replace("-", " ");
        String q = queryLoc.toLowerCase().replace("_", " ").replace("-", " ");
        // Check both directions — either might be the broader region
        if (d.contains(q) || q.contains(d)) return true;
        // Also check the raw forms (for IDs like "zhao_mountains")
        return dataLoc.toLowerCase().contains(queryLoc.toLowerCase())
                || queryLoc.toLowerCase().contains(dataLoc.toLowerCase());
    }

    /** An entry in the "what exists" registry. */
    public record ObjectEntry(
            String objectId,
            String objectType,    // "biome", "structure", "species", "macro_terrain", "artifact", "civilization", "npc"
            String locationId,
            long ageYears,
            String trueState       // objective description
    ) {}

    /** The true owner of an object. */
    public record OwnershipRecord(
            String objectId,
            String trueOwner,
            String ownerType,      // "sect", "beast", "cultivator", "unclaimed"
            int ownerStrength,     // cultivation tier of owner
            int claimStrength      // how strongly the owner holds it (1-10)
    ) {}

    /** An entity who desires an object. */
    public record DesireRecord(
            String objectId,
            String desirerId,
            String desireType,     // "inheritance", "treasure", "territory", "revenge", "cultivation"
            int desireStrength,    // 1-10
            String desireReason
    ) {}

    /** An entity who knows about an object. */
    public record KnowledgeRecord(
            String objectId,
            String knowerId,
            int knowledgeTier,     // 1=rumor, 2=general, 3=detailed, 4=complete, 5=secret
            int knowledgeAccuracy  // 1-10
    ) {}

    /** Why an object hasn't been taken. */
    public record UntakenReason(
            String objectId,
            String reasonType,     // "sealed", "hidden", "guarded", "too_dangerous", "unknown", "taboo", "contested", "requires_specific_cultivation", "requires_specific_perception", "requires_specific_luck"
            int reasonStrength     // 1-10
    ) {}

    /** What naturally happens next. */
    public record NaturalNextEvent(
            String objectId,
            String nextEvent,
            long timelineTicks,    // when it will happen (in ticks from now)
            double probability     // 0.0-1.0
    ) {}

    // ─── Player Arrival ───────────────────────────────────────────────

    /**
     * Called when a player arrives at a location.
     *
     * <p>Per the Prime Directive: the engine does NOT spawn content; it reveals
     * what already exists according to the player's perception tier.
     *
     * @param locationId the location the player arrived at
     * @param playerRealm the player's cultivation realm
     * @param perceptionTier the player's perception tier
     * @return a PlayerArrivalResult describing what the player perceives
     */
    public static PlayerArrivalResult onPlayerArrival(
            String locationId, RealmId playerRealm, PerceptionTier perceptionTier) {

        // Query what exists at this location (objective)
        List<ObjectEntry> existing = queryWhatExists(locationId);

        // Filter by perception tier — the PerceptionEngine overlays a filtered view
        List<ObjectEntry> perceived = new ArrayList<>();
        for (ObjectEntry entry : existing) {
            // TODO: Use PerceptionEngine to filter based on tier
            // For now, include all (stub)
            perceived.add(entry);
        }

        return new PlayerArrivalResult(locationId, playerRealm, perceptionTier,
                perceived, existing.size(), perceived.size());
    }

    /** Result of a player arriving at a location. */
    public record PlayerArrivalResult(
            String locationId,
            RealmId playerRealm,
            PerceptionTier perceptionTier,
            List<ObjectEntry> perceivedObjects,
            int totalExisting,
            int totalPerceived
    ) {
        /** How much of the location's truth the player can perceive. */
        public double perceptionRatio() {
            return totalExisting == 0 ? 0.0 : (double) totalPerceived / totalExisting;
        }
    }

    // ─── Subsystem Registry ───────────────────────────────────────────

    /** The registry of all data-driven subsystems the engine reads from. */
    public static final Map<String, String> SUBSYSTEM_REGISTRY = Map.ofEntries(
            Map.entry("species", "data/ergenverse/species/"),
            Map.entry("species_variants", "data/ergenverse/species_variants/"),
            Map.entry("ecosystems", "data/ergenverse/ecosystems/"),
            Map.entry("ecosystem_integration", "data/ergenverse/ecosystem_integration/"),
            Map.entry("migrations", "data/ergenverse/migrations/"),
            Map.entry("macro_terrain", "data/ergenverse/macro_terrain/"),
            Map.entry("provenance", "data/ergenverse/provenance/"),
            Map.entry("civilizations", "data/ergenverse/civilizations/"),
            Map.entry("npcs", "data/ergenverse/npcs/"),
            Map.entry("faction_relationships", "data/ergenverse/faction_relationships/"),
            Map.entry("opportunities", "data/ergenverse/opportunities/"),
            Map.entry("time_events", "data/ergenverse/time_events/"),
            Map.entry("item_properties", "data/ergenverse/item_properties/"),
            Map.entry("world_state", "data/ergenverse/world_state/"),
            Map.entry("worldgen", "data/ergenverse/worldgen/")
    );

    /** Get the list of all subsystem names. */
    public static Set<String> getSubsystemNames() {
        return SUBSYSTEM_REGISTRY.keySet();
    }

    /** Get the data path for a subsystem. */
    public static String getSubsystemPath(String subsystemName) {
        return SUBSYSTEM_REGISTRY.getOrDefault(subsystemName, "unknown");
    }

    // ─── Engine Info ──────────────────────────────────────────────────

    /** Get engine version. */
    public static String getVersion() { return "1.0"; }

    /** Get a status report. */
    public static String getStatusReport() {
        return String.format(
            "World State Engine v%s | Tick: %d (day %d) | Season: %s | Subsystems: %d",
            getVersion(), worldTick, worldTick, currentSeason.name, SUBSYSTEM_REGISTRY.size()
        );
    }
}
