package dev.ergenverse.simulation;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.WorldStateEngine.Season;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WorldSimState — the persisted runtime state of the macro world simulation.
 *
 * <p>This tracks the DYNAMIC portion of the WorldStateEngine's subsystems —
 * the things that change per-tick and must survive server restart:
 * <ul>
 *   <li><b>Migration progress</b> — which waypoint each migration is currently
 *       at, and how many days it has been there. When {@code daysAtWaypoint}
 *       exceeds the waypoint's {@code duration_days}, the migration advances
 *       to the next waypoint and fires a {@code migration.arrived} event.</li>
 *   <li><b>Ecosystem seasonal state</b> — the current named state of each
 *       ecosystem (e.g. "spring", "tribulation_storm"). Rotates with the
 *       world season.</li>
 *   <li><b>Civilization vitality</b> — a per-civ disciple count and economy
 *       level that drifts based on canon events and ecosystem health.</li>
 *   <li><b>Opportunity maturity</b> — how many in-game years each opportunity
 *       has aged. When it exceeds {@code age_requirement_years}, it becomes
 *       discoverable and fires {@code opportunity.matured}.</li>
 * </ul>
 *
 * <p><b>Persistence:</b> stored in {@link WorldRuntimeState} under the
 * reserved key {@code "_worldsim_state"} (mirrors the pattern used by
 * {@link dev.ergenverse.history.WorldHistory} and
 * {@link dev.ergenverse.history.WorldChronicle}).
 *
 * <p><b>Thread safety:</b> all maps are {@link ConcurrentHashMap}. The
 * per-entity state tags are defensive-copied on get/set (same pattern as
 * WorldRuntimeState).
 *
 * <p><b>Constitution compliance:</b> Article XLII (Layer 3 — Simulation
 * Delta). This is mutable runtime state layered on top of the immutable
 * canon data; the canon JSONs are never touched.
 */
public final class WorldSimState {

    private static final String TAG_WORLDSIM = "_worldsim_state";
    private static final String TAG_MIGRATIONS = "migrations";
    private static final String TAG_ECOSYSTEMS = "ecosystems";
    private static final String TAG_CIVILIZATIONS = "civilizations";
    private static final String TAG_OPPORTUNITIES = "opportunities";

    private static final String TAG_ID = "id";
    private static final String TAG_WAYPOINT_INDEX = "waypoint_index";
    private static final String TAG_DAYS_AT_WAYPOINT = "days_at_waypoint";
    private static final String TAG_SEASONAL_STATE = "seasonal_state";
    private static final String TAG_DISCIPLES = "disciples";
    private static final String TAG_ECONOMY_LEVEL = "economy_level";
    private static final String TAG_AGE_YEARS = "age_years";
    private static final String TAG_MATURED = "matured";

    // ─── Runtime state maps ───────────────────────────────────────────

    /** migrationId → {waypointIndex, daysAtWaypoint} */
    private final Map<String, CompoundTag> migrationProgress = new ConcurrentHashMap<>();
    /** ecosystemId → {seasonalState} */
    private final Map<String, CompoundTag> ecosystemState = new ConcurrentHashMap<>();
    /** civilizationId → {disciples, economyLevel} */
    private final Map<String, CompoundTag> civilizationState = new ConcurrentHashMap<>();
    /** opportunityId → {ageYears, matured} */
    private final Map<String, CompoundTag> opportunityMaturity = new ConcurrentHashMap<>();

    private static WorldSimState instance = null;

    private WorldSimState() {}

    // ═══════════════════════════════════════════════════════════════════
    //  Migration progress
    // ═══════════════════════════════════════════════════════════════════

    /** Get the current waypoint index for a migration (0-based). Returns 0 if unknown. */
    public int getMigrationWaypointIndex(String migrationId) {
        CompoundTag t = migrationProgress.get(migrationId);
        return t != null ? t.getInt(TAG_WAYPOINT_INDEX) : 0;
    }

    /** Get days spent at the current waypoint. */
    public int getMigrationDaysAtWaypoint(String migrationId) {
        CompoundTag t = migrationProgress.get(migrationId);
        return t != null ? t.getInt(TAG_DAYS_AT_WAYPOINT) : 0;
    }

    /** Set migration progress. */
    public void setMigrationProgress(String migrationId, int waypointIndex, int daysAtWaypoint) {
        CompoundTag t = new CompoundTag();
        t.putString(TAG_ID, migrationId);
        t.putInt(TAG_WAYPOINT_INDEX, waypointIndex);
        t.putInt(TAG_DAYS_AT_WAYPOINT, daysAtWaypoint);
        migrationProgress.put(migrationId, t);
    }

    /** Advance days at waypoint by 1. Returns the new days-at-waypoint count. */
    public int incrementMigrationDays(String migrationId) {
        int idx = getMigrationWaypointIndex(migrationId);
        int days = getMigrationDaysAtWaypoint(migrationId) + 1;
        setMigrationProgress(migrationId, idx, days);
        return days;
    }

    /** Reset days-at-waypoint to 0 after advancing to a new waypoint. */
    public void advanceMigrationWaypoint(String migrationId, int newIndex) {
        setMigrationProgress(migrationId, newIndex, 0);
    }

    /** All migration IDs with recorded progress. */
    public java.util.Set<String> migrationIds() {
        return java.util.Collections.unmodifiableSet(migrationProgress.keySet());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Ecosystem seasonal state
    // ═══════════════════════════════════════════════════════════════════

    /** Get the current seasonal state name for an ecosystem (e.g. "spring"). */
    public String getEcosystemSeasonalState(String ecosystemId) {
        CompoundTag t = ecosystemState.get(ecosystemId);
        return t != null ? t.getString(TAG_SEASONAL_STATE) : "spring";
    }

    /** Set the ecosystem's current seasonal state. */
    public void setEcosystemSeasonalState(String ecosystemId, String stateName) {
        CompoundTag t = new CompoundTag();
        t.putString(TAG_ID, ecosystemId);
        t.putString(TAG_SEASONAL_STATE, stateName);
        ecosystemState.put(ecosystemId, t);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Civilization vitality
    // ═══════════════════════════════════════════════════════════════════

    /** Get a civ's current disciple count (or -1 if untracked). */
    public int getCivilizationDisciples(String civId) {
        CompoundTag t = civilizationState.get(civId);
        return t != null ? t.getInt(TAG_DISCIPLES) : -1;
    }

    /** Get a civ's economy level (0=collapsed, 1=poor, 2=moderate, 3=rich, 4=flourishing). */
    public int getCivilizationEconomyLevel(String civId) {
        CompoundTag t = civilizationState.get(civId);
        return t != null ? t.getInt(TAG_ECONOMY_LEVEL) : 2;
    }

    /** Set civ vitality. */
    public void setCivilizationState(String civId, int disciples, int economyLevel) {
        CompoundTag t = new CompoundTag();
        t.putString(TAG_ID, civId);
        t.putInt(TAG_DISCIPLES, disciples);
        t.putInt(TAG_ECONOMY_LEVEL, economyLevel);
        civilizationState.put(civId, t);
    }

    /** All tracked civ IDs. */
    public java.util.Set<String> civilizationIds() {
        return java.util.Collections.unmodifiableSet(civilizationState.keySet());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Opportunity maturity
    // ═══════════════════════════════════════════════════════════════════

    /** Get the age (in-game years) of an opportunity. */
    public int getOpportunityAgeYears(String oppId) {
        CompoundTag t = opportunityMaturity.get(oppId);
        return t != null ? t.getInt(TAG_AGE_YEARS) : 0;
    }

    /** Has this opportunity matured (become discoverable)? */
    public boolean isOpportunityMatured(String oppId) {
        CompoundTag t = opportunityMaturity.get(oppId);
        return t != null && t.getBoolean(TAG_MATURED);
    }

    /** Set opportunity maturity state. */
    public void setOpportunityMaturity(String oppId, int ageYears, boolean matured) {
        CompoundTag t = new CompoundTag();
        t.putString(TAG_ID, oppId);
        t.putInt(TAG_AGE_YEARS, ageYears);
        t.putBoolean(TAG_MATURED, matured);
        opportunityMaturity.put(oppId, t);
    }

    /** All tracked opportunity IDs. */
    public java.util.Set<String> opportunityIds() {
        return java.util.Collections.unmodifiableSet(opportunityMaturity.keySet());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Serialization
    // ═══════════════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put(TAG_MIGRATIONS, serializeMap(migrationProgress));
        tag.put(TAG_ECOSYSTEMS, serializeMap(ecosystemState));
        tag.put(TAG_CIVILIZATIONS, serializeMap(civilizationState));
        tag.put(TAG_OPPORTUNITIES, serializeMap(opportunityMaturity));
        return tag;
    }

    public static WorldSimState load(CompoundTag tag) {
        WorldSimState s = new WorldSimState();
        if (tag == null) return s;
        deserializeMap(tag.getList(TAG_MIGRATIONS, Tag.TAG_COMPOUND), s.migrationProgress);
        deserializeMap(tag.getList(TAG_ECOSYSTEMS, Tag.TAG_COMPOUND), s.ecosystemState);
        deserializeMap(tag.getList(TAG_CIVILIZATIONS, Tag.TAG_COMPOUND), s.civilizationState);
        deserializeMap(tag.getList(TAG_OPPORTUNITIES, Tag.TAG_COMPOUND), s.opportunityMaturity);
        return s;
    }

    private static ListTag serializeMap(Map<String, CompoundTag> map) {
        ListTag list = new ListTag();
        for (Map.Entry<String, CompoundTag> e : map.entrySet()) {
            list.add(e.getValue());
        }
        return list;
    }

    private static void deserializeMap(ListTag list, Map<String, CompoundTag> target) {
        target.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            String id = t.getString(TAG_ID);
            if (!id.isEmpty()) target.put(id, t);
        }
    }

    // ─── World-global access (mirrors WorldChronicle / CanonDivergenceRecorder) ───

    /** Get the world-global sim state, loading from WorldRuntimeState if needed. */
    public static WorldSimState get(ServerLevel level) {
        if (instance != null) return instance;
        WorldRuntimeState runtime = WorldRuntimeState.get(level);
        CompoundTag tag = runtime.getPlayerMutation(TAG_WORLDSIM);
        instance = load(tag);
        return instance;
    }

    /** Persist the sim state to WorldRuntimeState. */
    public static void persist(ServerLevel level) {
        if (instance == null) return;
        WorldRuntimeState runtime = WorldRuntimeState.get(level);
        CompoundTag tag = runtime.getPlayerMutation(TAG_WORLDSIM);
        CompoundTag saved = instance.save();
        tag.put(TAG_MIGRATIONS, saved.getList(TAG_MIGRATIONS, Tag.TAG_LIST));
        tag.put(TAG_ECOSYSTEMS, saved.getList(TAG_ECOSYSTEMS, Tag.TAG_LIST));
        tag.put(TAG_CIVILIZATIONS, saved.getList(TAG_CIVILIZATIONS, Tag.TAG_LIST));
        tag.put(TAG_OPPORTUNITIES, saved.getList(TAG_OPPORTUNITIES, Tag.TAG_LIST));
        runtime.updatePlayerMutation(TAG_WORLDSIM, tag);
    }

    /** Clear the cached instance (on world unload). */
    public static void clearCache() { instance = null; }

    /** A short status string for debug commands. */
    public String getStatusReport() {
        return String.format("WorldSimState{migrations=%d, ecosystems=%d, civs=%d, opps=%d}",
                migrationProgress.size(), ecosystemState.size(),
                civilizationState.size(), opportunityMaturity.size());
    }
}
