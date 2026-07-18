package dev.ergenverse.entity.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.simulation.WorldStateDataLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * NpcScheduleGoal — Article XXIII (Vertical Slice Completion).
 *
 * <h2>What this does</h2>
 * <p>Makes Heng Yue Sect NPCs follow a daily schedule — they cultivate at
 * dawn, eat at the main hall at noon, study at the library in the
 * afternoon, sleep at the dormitory at night, patrol after dark.
 * Each NPC can have a canon-faithful schedule defined in its JSON data.
 *
 * <h2>Canon basis</h2>
 * <p>In Renegade Immortal Chapters 1-50, Heng Yue Sect has a strict daily
 * rhythm:</p>
 * <ul>
 *   <li>Dawn: outer disciples cultivate at Sword Peak (Wang Lin's daily routine).</li>
 *   <li>Morning: eat at the Main Hall, then attend elder lectures.</li>
 *   <li>Daytime: study at the Library or patrol the sect perimeter.</li>
 *   <li>Afternoon: free cultivation time or sparring.</li>
 *   <li>Dusk: evening meal at Main Hall.</li>
 *   <li>Night: sleep at Disciple Dormitory. Night patrol sentries walk the
 *       perimeter.</li>
 * </ul>
 * Before this goal, ALL NPCs (except Wang Tiangui) either stood still
 * (meditating) or wandered randomly. Now they have canon-faithful daily
 * rhythms.
 *
 * <h2>How it works</h2>
 * <ol>
 *   <li>The NPC's JSON data contains a {@code daily_schedule} array. Each
 *       entry has: {@code t0} (start MC tick), {@code t1} (end MC tick),
 *       {@code act} (activity name), {@code dir} (direction), {@code dist}
 *       (blocks).</li>
 *   <li>The goal reads the current schedule entry and pathfinds the NPC
 *       to the target location (home + direction + distance).</li>
 *   <li>When the NPC arrives at the target, it stands still for the
 *       duration (scheduled activity = stationary).</li>
 *   <li>When the time window expires, the goal re-evaluates and either
 *       moves to the next location or yields (letting lower-priority goals
 *       like CognitionDrivenGoal or RandomStrollGoal handle free time).</li>
 * </ol>
 *
 * <h2>Priority design</h2>
 * <ul>
 *   <li>Priority 3 (below NpcInitiationGoal=2, above CognitionDrivenGoal=4).</li>
 *   <li>The schedule is the DEFAULT behavior — NPCs follow their routine.</li>
 *   <li>CognitionDrivenGoal (priority 4) can OVERRIDE the schedule when
 *       the intent system fires (e.g., spirit fruit detected, player nearby).
 *       This is correct: the schedule is the routine; the intent system
 *       handles situational responses. After the situation resolves, the
 *       schedule resumes.</li>
 * </ul>
 *
 * <h2>Data shape (NPC JSON)</h2>
 * <pre>{
 *   "daily_schedule": [
 *     {"t0": 0, "t1": 2000, "act": "cultivating", "dir": "north", "dist": 20},
 *     {"t0": 2000, "t1": 3000, "act": "eating", "dir": null, "dist": 0},
 *     {"t0": 3000, "t1": 6000, "act": "patrolling", "dir": "east", "dist": 30}
 *   ]
 * }</pre>
 * If {@code dir} is null or {@code dist} is 0, the NPC stays in place
 * (no pathfinding — lower-priority goals handle free time).
 *
 * <h2>Article XXVI compliance</h2>
 * <p>NO new Engine/Bus/Subscriber. This is a single Minecraft Goal class.
 * It reuses:
 * <ul>
 *   <li>{@link WorldStateDataLoader} — the existing NPC JSON loader.</li>
 *   <li>{@link dev.ergenverse.simulation.CultivatorActivityResolver} — the
 *       existing activity resolver (extended to read schedules).</li>
 * </ul>
 *
 * <p><b>Provenance:</b> INFERRED from RI Chapters 1-50 (Heng Yue Sect
 * daily rhythm).
 */
public class NpcScheduleGoal extends Goal {

    /** MC day = 24000 ticks = 20 minutes real time. */
    private static final long MC_DAY_TICKS = 24000L;

    private final EntityCultivator cultivator;

    /** Cached schedule entries loaded from NPC JSON. */
    private final List<ScheduleEntry> schedule = new ArrayList<>();

    /** True once schedule data has been loaded (or attempted). */
    private boolean scheduleLoaded = false;

    /** The home position (where the NPC "lives" — recorded on first load). */
    private BlockPos homePos = null;

    /** Target position for the current schedule entry. */
    private BlockPos targetPos = null;

    /** The current schedule entry being followed. */
    private ScheduleEntry currentEntry = null;

    /** Tick when we started moving toward the target. */
    private long moveStartTick = 0L;

    public NpcScheduleGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cultivator.level().isClientSide) return false;
        if (!loadScheduleIfNeeded()) return false;
        if (schedule.isEmpty()) return false;

        long now = cultivator.level().getGameTime();
        long timeOfDay = now % MC_DAY_TICKS;

        // Find the current schedule entry
        ScheduleEntry entry = findCurrentEntry(timeOfDay);
        if (entry == null) {
            // No scheduled activity right now — let lower goals handle free time
            currentEntry = null;
            targetPos = null;
            return false;
        }

        // Stationary activities with no direction = NPC stays put.
        // Let CognitionDrivenGoal or RandomStrollGoal handle it.
        if (entry.dir == null && entry.dist == 0) return false;

        // Record home position on first activation
        if (homePos == null) {
            homePos = cultivator.blockPosition();
        }

        // Compute target position: home + direction + distance
        targetPos = computeTarget(entry);
        if (targetPos == null) return false;

        currentEntry = entry;
        moveStartTick = now;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (cultivator.level().isClientSide) return false;
        if (currentEntry == null || targetPos == null) return false;
        if (targetPlayerDisconnected()) return false;

        // Time window expired?
        long now = cultivator.level().getGameTime();
        long timeOfDay = now % MC_DAY_TICKS;
        if (timeOfDay >= currentEntry.t1) {
            // Window expired — check if next entry is actionable
            ScheduleEntry next = findCurrentEntry(timeOfDay);
            if (next != null && next.dir != null && next.dist > 0) {
                // Transition: update target, re-path
                currentEntry = next;
                targetPos = computeTarget(next);
                moveStartTick = now;
                return targetPos != null;
            }
            // No more scheduled entries — yield to free time
            return false;
        }

        // Arrived at target?
        double dist = cultivator.distanceTo(
                targetPos.getX(), cultivator.getY(), targetPos.getZ());
        if (dist < 2.0) {
            // Arrived — stop moving, stand still. canContinueToUse will
            // keep returning true (NPC stands still) until window expires.
            cultivator.getNavigation().stop();
            return true;
        }

        // Re-path if stuck or timeout
        if (cultivator.getNavigation().isDone()
                || (now - moveStartTick) > 400L) {
            walkTowardTarget();
        }

        return true;
    }

    @Override
    public void start() {
        walkTowardTarget();
        Ergenverse.LOGGER.debug("[NpcSchedule] {} following schedule: {} ({}→{}) → ({}, {}, {})",
                cultivator.getCharacterId(), currentEntry.act,
                currentEntry.t0, currentEntry.t1,
                currentEntry.dir, currentEntry.dist,
                targetPos.getX(), targetPos.getY(), targetPos.getZ());
    }

    @Override
    public void tick() {
        if (currentEntry == null || targetPos == null) return;

        // Already at target — stand still and look around
        double dist = cultivator.distanceTo(
                targetPos.getX(), cultivator.getY(), targetPos.getZ());
        if (dist < 2.5) {
            cultivator.getNavigation().stop();
            // Face the direction the NPC came from
            if (homePos != null) {
                cultivator.getLookControl().setLookAt(homePos);
            }
            return;
        }

        // Re-path periodically
        if (cultivator.getNavigation().isDone()
                || cultivator.tickCount % 60 == 0) {
            walkTowardTarget();
        }
    }

    @Override
    public void stop() {
        currentEntry = null;
        targetPos = null;
        cultivator.getNavigation().stop();
    }

    // ═════════════════════════════════════════════════════════════

    private void walkTowardTarget() {
        if (targetPos == null) return;
        cultivator.getNavigation().moveTo(
                targetPos.getX(), cultivator.getY(), targetPos.getZ(), 0.8D);
    }

    /**
     * Compute the target BlockPos from home + direction + distance.
     * Direction is one of: north, south, east, west, northeast, northwest,
     * southeast, southwest (or null = stay put).
     */
    private BlockPos computeTarget(ScheduleEntry entry) {
        if (homePos == null || entry.dir == null || entry.dist <= 0) return null;
        int dx = 0, dz = 0;
        switch (entry.dir) {
            case "north":     dz = 1; break;
            case "south":     dz = -1; break;
            case "east":      dx = 1; break;
            case "west":      dx = -1; break;
            case "northeast":  dx = 1; dz = 1; break;
            case "northwest":  dx = -1; dz = 1; break;
            case "southeast": dx = 1; dz = -1; break;
            case "southwest": dx = -1; dz = -1; break;
            default: return null;
        }
        return new BlockPos(
                homePos.getX() + dx * entry.dist,
                homePos.getY(),
                homePos.getZ() + dz * entry.dist);
    }

    private boolean targetPlayerDisconnected() {
        // Single-player game — check if the single player is gone
        var server = cultivator.level().getServer();
        if (server == null) return true;
        var players = server.getPlayerList().getPlayers();
        return players.isEmpty();
    }

    // ── Schedule loading ───────────────────────────────────────

    private boolean loadScheduleIfNeeded() {
        if (scheduleLoaded) return !schedule.isEmpty();

        scheduleLoaded = true;
        String characterId = cultivator.getCharacterId();
        if (characterId == null || characterId.isEmpty()) return false;

        try {
            JsonObject data = WorldStateDataLoader.getEntry("npcs", characterId);
            if (data == null || !data.has("daily_schedule")
                    || !data.get("daily_schedule").isJsonArray()) {
                return false;
            }
            JsonArray arr = data.getAsJsonArray("daily_schedule");
            schedule.clear();
            for (var elem : arr) {
                if (!elem.isJsonObject()) continue;
                JsonObject obj = elem.getAsJsonObject();
                int t0 = obj.has("t0") ? obj.get("t0").getAsInt() : 0;
                int t1 = obj.has("t1") ? obj.get("t1").getAsInt() : 24000;
                String act = obj.has("act") ? obj.get("act").getAsString() : "";
                String dir = obj.has("dir") ? obj.get("dir").getAsString() : null;
                int dist = obj.has("dist") ? obj.get("dist").getAsInt() : 0;
                if (t1 > 24000) t1 = 24000;
                schedule.add(new ScheduleEntry(t0, t1, act, dir, dist));
            }
            Ergenverse.LOGGER.info("[NpcSchedule] Loaded {} schedule entries for {}",
                    schedule.size(), characterId);
            return !schedule.isEmpty();
        } catch (Exception e) {
            Ergenverse.LOGGER.debug("[NpcSchedule] Failed to load schedule for {}: {}",
                    characterId, e.getMessage());
            return false;
        }
    }

    /** Immutable schedule entry. */
    private static final class ScheduleEntry {
        final int t0, t1;
        final String act;
        final String dir;
        final int dist;
        ScheduleEntry(int t0, int t1, String act, String dir, int dist) {
            this.t0 = t0;
            this.t1 = t1;
            this.act = act;
            this.dir = dir;
            this.dist = dist;
        }
    }
}