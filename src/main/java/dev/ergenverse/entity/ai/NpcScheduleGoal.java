package dev.ergenverse.entity.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.simulation.WorldStateDataLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * NpcScheduleGoal - Article XXIII (Vertical Slice Completion).
 *
 * <h2>What this does</h2>
 * <p>Makes Heng Yue Sect NPCs follow a daily schedule defined in their
 * NPC JSON data. At dawn they cultivate at Sword Peak, at noon they eat
 * at the Main Hall, in the afternoon they patrol or study, at night
 * they sleep in the dormitory.
 *
 * <h2>Canon basis</h2>
 * <p>INFERRED from RI Chapters 1-50. Heng Yue Sect has a strict daily
 * rhythm: dawn cultivation, morning lectures, daytime study/patrol,
 * evening meal, night sleep. Night patrol sentries walk the perimeter.
 * Before this goal, NPCs either stood still or wandered randomly.
 *
 * <h2>How it works</h2>
 * <ol>
 *   <li>NPC JSON contains a {@code daily_schedule} array with entries
 *       having {@code t0}, {@code t1}, {@code act}, {@code dir}, {@code dist}.</li>
 *   <li>The goal pathfinds the NPC to home + direction + distance.</li>
 *   <li>On arrival, the NPC stands still until the window expires.</li>
 *   <li>When the window expires, it transitions to the next entry or yields.</li>
 * </ol>
 *
 * <h2>Priority design</h2>
 * <p>Priority 3 (below NpcInitiationGoal=2, above CognitionDrivenGoal=4).
 * The schedule is the DEFAULT behavior. CognitionDrivenGoal can override
 * it for situational responses.
 *
 * <h2>Article XXVI compliance</h2>
 * <p>NO new Engine/Bus/Subscriber. Single Goal class reusing
 * WorldStateDataLoader for JSON access.
 */
public class NpcScheduleGoal extends Goal {

    /** MC day = 24000 ticks = 20 minutes real time. */
    private static final long MC_DAY_TICKS = 24000L;

    /** Squared arrival threshold (2 blocks squared = 4.0). */
    private static final double ARRIVE_DIST_SQ = 4.0;

    /** Squared close-enough threshold for look-at (2.5 blocks squared). */
    private static final double CLOSE_DIST_SQ = 6.25;

    private final EntityCultivator cultivator;

    /** Cached schedule entries loaded from NPC JSON. */
    private final List<ScheduleEntry> schedule = new ArrayList<>();

    /** True once schedule data has been loaded (or attempted). */
    private boolean scheduleLoaded = false;

    /** The home position (where the NPC "lives" - recorded on first load). */
    private BlockPos homePos = null;

    /** Target position for the current schedule entry. */
    private BlockPos targetPos = null;

    /** The current schedule entry being followed. */
    private ScheduleEntry currentEntry = null;

    /** Tick when we started moving toward the target. */
    private long moveStartTick = 0L;

    public NpcScheduleGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cultivator.level().isClientSide) return false;
        if (!loadScheduleIfNeeded()) return false;
        if (schedule.isEmpty()) return false;

        long now = cultivator.level().getGameTime();
        long timeOfDay = now % MC_DAY_TICKS;

        ScheduleEntry entry = findCurrentEntry(timeOfDay);
        if (entry == null) {
            currentEntry = null;
            targetPos = null;
            return false;
        }

        // Stationary activities with no direction: NPC stays put,
        // lower-priority goals handle free time.
        if (entry.dir == null && entry.dist == 0) return false;

        // Record home position on first activation
        if (homePos == null) {
            homePos = cultivator.blockPosition();
        }

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
        if (isSinglePlayerGone()) return false;

        long now = cultivator.level().getGameTime();
        long timeOfDay = now % MC_DAY_TICKS;

        // Handle wrap-around at midnight
        boolean windowExpired;
        if (currentEntry.t1 <= currentEntry.t0) {
            // Wraps midnight: active when timeOfDay >= t0 OR timeOfDay < t1
            windowExpired = (timeOfDay >= currentEntry.t1 && timeOfDay < currentEntry.t0);
        } else {
            windowExpired = timeOfDay >= currentEntry.t1;
        }

        if (windowExpired) {
            ScheduleEntry next = findCurrentEntry(timeOfDay);
            if (next != null && next.dir != null && next.dist > 0) {
                currentEntry = next;
                targetPos = computeTarget(next);
                moveStartTick = now;
                return targetPos != null;
            }
            return false;
        }

        // Arrived at target?
        double distSq = cultivator.distanceToSqr(
                targetPos.getX() + 0.5, cultivator.getY(), targetPos.getZ() + 0.5);
        if (distSq < ARRIVE_DIST_SQ) {
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
        Ergenverse.LOGGER.debug("[NpcSchedule] {} following schedule: {} ({}-{}) dir={} dist={} -> ({},{},{})",
                cultivator.getCharacterId(), currentEntry.act,
                currentEntry.t0, currentEntry.t1,
                currentEntry.dir, currentEntry.dist,
                targetPos.getX(), targetPos.getY(), targetPos.getZ());
    }

    @Override
    public void tick() {
        if (currentEntry == null || targetPos == null) return;

        double distSq = cultivator.distanceToSqr(
                targetPos.getX() + 0.5, cultivator.getY(), targetPos.getZ() + 0.5);
        if (distSq < CLOSE_DIST_SQ) {
            cultivator.getNavigation().stop();
            if (homePos != null) {
                cultivator.getLookControl().setLookAt(
                        homePos.getX() + 0.5, homePos.getY() + 0.5, homePos.getZ() + 0.5);
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

    // =================================================================

    /**
     * Find the schedule entry active at the given time-of-day.
     * Handles entries that wrap around midnight (t1 < t0).
     */
    private ScheduleEntry findCurrentEntry(long timeOfDay) {
        for (ScheduleEntry e : schedule) {
            if (e.t1 <= e.t0) {
                // Wraps midnight: active when timeOfDay >= t0 OR timeOfDay < t1
                if (timeOfDay >= e.t0 || timeOfDay < e.t1) return e;
            } else {
                if (timeOfDay >= e.t0 && timeOfDay < e.t1) return e;
            }
        }
        return null;
    }

    private void walkTowardTarget() {
        if (targetPos == null) return;
        cultivator.getNavigation().moveTo(
                targetPos.getX() + 0.5, cultivator.getY(), targetPos.getZ() + 0.5, 0.8D);
    }

    /**
     * Compute the target BlockPos from home + direction + distance.
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

    private boolean isSinglePlayerGone() {
        var server = cultivator.level().getServer();
        if (server == null) return true;
        return server.getPlayerList().getPlayers().isEmpty();
    }

    // -- Schedule loading ------------------------------------------------

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
                String dir = obj.has("dir") && !obj.get("dir").isJsonNull()
                        ? obj.get("dir").getAsString() : null;
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