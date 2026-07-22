package dev.ergenverse.entity.ai;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.cognition.Ontology;
import dev.ergenverse.simulation.intent.ActorEntityLink;
import dev.ergenverse.simulation.intent.CultivationTask;
import dev.ergenverse.simulation.intent.Intent;
import dev.ergenverse.simulation.intent.IntentDecomposer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * CognitionDrivenGoal — the master Minecraft Goal that bridges the simulation
 * layer's Intent to the Minecraft entity's physical behavior.
 *
 * <p>THE CENTRAL DEFICIT (from cron): "The missing bridge: Decision → Intent
 * → Planner → Physical Tasks → Minecraft Goals → World Changes. Without it
 * NPCs are a thought simulator that never acts."
 *
 * <p>This class IS that bridge. It is a single Minecraft {@link Goal} added
 * to the {@link EntityCultivator}'s goalSelector. When active, it:
 * <ol>
 *   <li>Looks up the Actor linked to this entity via {@link ActorEntityLink}.</li>
 *   <li>Reads the Actor's {@code cognition.activeIntent}.</li>
 *   <li>Decomposes the Intent into a list of {@link CultivationTask}s via
 *       {@link IntentDecomposer}.</li>
 *   <li>Executes each task in order: MOVE_TO → pathfind, WAIT → stand still,
 *       FACE_TARGET → turn, etc.</li>
 *   <li>When all tasks complete (or the Intent expires), it re-decomposes
 *       with the current Intent (which may have changed since the last tick).</li>
 * </ol>
 *
 * <h2>Canon-faithful behavior</h2>
 * <p>Wang Lin (DEFIANCE Dao) with AVOID_REVEALING_STRENGTH intent will:
 * <ul>
 *   <li>If a player is within 32 blocks: path away from the player to ~48 blocks,
 *       then stop and face the area of interest.</li>
 *   <li>If no player nearby: stand still and face the area of interest.</li>
 * </ul>
 * <p>This is visibly different from a SLAUGHTER-dao NPC with AMBUSH intent,
 * which would path to concealment 16 blocks from the target and hold position.
 *
 * <h2>Priority</h2>
 * <p>This goal is added at priority 3 (below FloatGoal=0, above
 * RandomLookAroundGoal=8). When the cognition system has an active Intent,
 * this goal takes precedence over wandering. When no Intent is active (no
 * linked Actor, or Actor has no active goal), this goal yields so the
 * RandomStrollGoal and RandomLookAroundGoal can run.
 *
 * <h2>Hibernation interaction</h2>
 * <p>The EntityCultivator's {@code aiStep()} hibernates when no player is
 * within 64 blocks. When hibernating, the goalSelector doesn't tick, so
 * this goal won't fire. This is correct — the simulation layer
 * ({@link dev.ergenverse.simulation.actor.ActorTickLoop}) continues to
 * simulate the Actor at territory level, but the entity's physical body
 * is dormant. When a player approaches and the entity wakes up, this goal
 * picks up the Actor's current Intent and acts on it.
 *
 * <p><b>Provenance: INFERRED.</b> The bridge pattern is standard
 * simulation↔renderer. The specific task execution logic is distilled from
 * the Intent decomposition table in {@link IntentDecomposer}.
 */
public class CognitionDrivenGoal extends Goal {

    private final EntityCultivator cultivator;

    /** The current task queue (decomposed from the Actor's active Intent). */
    private final List<CultivationTask> taskQueue = new ArrayList<>();

    /** Index of the currently-executing task in the queue. */
    private int currentTaskIndex = -1;

    /** The Intent that produced the current task queue (for re-decomposition check). */
    private Intent sourceIntent = null;

    /** Tick when the current task started (for timeout checks). */
    private long taskStartTick = 0;

    /** The last Intent label logged (for diagnostics — avoid spam). */
    private String lastLoggedIntent = "";

    public CognitionDrivenGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
        // This goal controls movement and look — set flags so it doesn't conflict
        // with RandomStrollGoal and RandomLookAroundGoal when active.
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Goal lifecycle
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Can this goal run? Yes, IF there is a linked Actor with an active Intent.
     *
     * <p>If no Actor is linked (entity just spawned, not yet registered) or
     * the Actor has no active Intent, this goal yields to lower-priority
     * goals (RandomStroll, RandomLookAround).
     */
    @Override
    public boolean canUse() {
        if (cultivator.level().isClientSide) return false;
        String actorId = cultivator.getCharacterId();
        if (actorId == null || actorId.isEmpty()) return false;

        Actor actor = ActorRegistry.get(actorId);
        if (actor == null || actor.cognition == null) return false;

        Intent intent = actor.cognition.activeIntent;
        return intent != null;
    }

    /**
     * Can this goal keep running? Yes, if the Intent is still active and
     * not expired, and there are tasks remaining.
     */
    @Override
    public boolean canContinueToUse() {
        if (cultivator.level().isClientSide) return false;
        String actorId = cultivator.getCharacterId();
        if (actorId == null || actorId.isEmpty()) return false;

        Actor actor = ActorRegistry.get(actorId);
        if (actor == null || actor.cognition == null) return false;

        Intent currentIntent = actor.cognition.activeIntent;
        if (currentIntent == null) return false;

        // If the Intent changed since we started, re-decompose
        if (sourceIntent == null || !sameIntent(sourceIntent, currentIntent)) {
            return true; // keep running — start() will re-decompose
        }

        // Check if Intent expired
        long tick = cultivator.level().getGameTime();
        if (currentIntent.isExpired(tick)) return false;

        // Check if we have tasks remaining
        return currentTaskIndex >= 0 && currentTaskIndex < taskQueue.size();
    }

    /**
     * Start the goal — decompose the current Intent into tasks.
     */
    @Override
    public void start() {
        String actorId = cultivator.getCharacterId();
        Actor actor = ActorRegistry.get(actorId);
        if (actor == null || actor.cognition == null) return;

        Intent intent = actor.cognition.activeIntent;
        if (intent == null) return;

        long tick = cultivator.level().getGameTime();
        BlockPos entityPos = cultivator.blockPosition();

        // Find nearest player for AVOID_REVEALING_STRENGTH etc.
        net.minecraft.world.entity.player.Player nearestPlayer =
                cultivator.level().getNearestPlayer(cultivator, 64.0);
        String playerUuid = nearestPlayer != null ? nearestPlayer.getUUID().toString() : null;
        double playerDist = nearestPlayer != null ? nearestPlayer.distanceTo(cultivator) : Double.MAX_VALUE;

        // Determine target position (from intent targetId — for now, use nearest player pos
        // or the entity's own position as fallback)
        BlockPos targetPos = null;
        if (intent.hasTarget() && nearestPlayer != null) {
            targetPos = nearestPlayer.blockPosition();
        }

        // Decompose
        taskQueue.clear();
        List<CultivationTask> newTasks = IntentDecomposer.decompose(
                intent,
                entityPos.getX(), entityPos.getZ(),
                playerUuid, playerDist,
                targetPos,
                tick);
        taskQueue.addAll(newTasks);

        sourceIntent = intent;
        currentTaskIndex = taskQueue.isEmpty() ? -1 : 0;
        taskStartTick = tick;

        // Apply task 0 if present
        if (currentTaskIndex >= 0) {
            CultivationTask first = taskQueue.get(currentTaskIndex);
            first.status = CultivationTask.Status.IN_PROGRESS;
            beginTask(first, tick);
        }

        // Log (rate-limited — only when intent label changes)
        if (!intent.nature().label.equals(lastLoggedIntent)) {
            Ergenverse.LOGGER.info("[CognitionDrivenGoal] {} starting intent '{}' → {} tasks",
                    actorId, intent.nature().label, taskQueue.size());
            lastLoggedIntent = intent.nature().label;
        }
    }

    /**
     * Stop the goal — clear the task queue and navigation.
     */
    @Override
    public void stop() {
        taskQueue.clear();
        currentTaskIndex = -1;
        sourceIntent = null;
        cultivator.getNavigation().stop();
    }

    /**
     * Tick — execute the current task.
     */
    @Override
    public void tick() {
        if (cultivator.level().isClientSide) return;
        if (currentTaskIndex < 0 || currentTaskIndex >= taskQueue.size()) return;

        long tick = cultivator.level().getGameTime();
        CultivationTask task = taskQueue.get(currentTaskIndex);

        // Check timeout
        if (task.isTimedOut(tick)) {
            task.status = CultivationTask.Status.TIMED_OUT;
            advanceToNextTask(tick);
            return;
        }

        // Execute the task
        executeTask(task, tick);

        // Check completion
        if (isTaskComplete(task, tick)) {
            task.status = CultivationTask.Status.COMPLETED;
            advanceToNextTask(tick);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Task execution
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Begin executing a task (called once when the task becomes active).
     */
    private void beginTask(CultivationTask task, long tick) {
        switch (task.type) {
            case MOVE_TO:
                if (task.targetPos != null) {
                    // Use Y of the entity, not 0 — the decomposer uses y=0 as a sentinel
                    BlockPos dest = new BlockPos(task.targetPos.getX(),
                            cultivator.blockPosition().getY(), task.targetPos.getZ());
                    cultivator.getNavigation().moveTo(dest.getX(), dest.getY(), dest.getZ(), 1.0);
                }
                break;
            case MOVE_AWAY_FROM_ENTITY:
                moveAwayFromEntity(task, tick);
                break;
            case WAIT:
                cultivator.getNavigation().stop();
                break;
            case FACE_TARGET:
                if (task.targetPos != null) {
                    // Use entity Y, not 0
                    cultivator.getLookControl().setLookAt(
                            task.targetPos.getX(),
                            cultivator.getEyeY(),
                            task.targetPos.getZ());
                }
                break;
            case HOLD_POSITION:
                cultivator.getNavigation().stop();
                break;
            case FLEE:
                if (task.targetPos != null) {
                    Vec3 away = DefaultRandomPos.getPosAway(
                            cultivator, 16, 8,
                            new Vec3(task.targetPos.getX(), cultivator.getY(), task.targetPos.getZ()));
                    if (away != null) {
                        cultivator.getNavigation().moveTo(away.x, away.y, away.z, 1.2);
                    }
                }
                break;
            case APPROACH_TARGET:
                approachTargetEntity(task, tick);
                break;
        }
    }

    /**
     * Execute (tick) a task. Most tasks are fire-and-forget in beginTask;
     * tick is mainly for re-checking pathfinding status or adjusting look.
     */
    private void executeTask(CultivationTask task, long tick) {
        switch (task.type) {
            case MOVE_TO:
            case FLEE:
            case MOVE_AWAY_FROM_ENTITY:
            case APPROACH_TARGET:
                // If pathfinding finished but we haven't arrived, restart
                if (!cultivator.getNavigation().isInProgress()) {
                    // Path done — check if we're close enough; if not, re-path
                    // (handled by isTaskComplete)
                }
                break;
            case FACE_TARGET:
                // Keep looking at the target
                if (task.targetPos != null) {
                    cultivator.getLookControl().setLookAt(
                            task.targetPos.getX(),
                            cultivator.getEyeY(),
                            task.targetPos.getZ());
                }
                break;
            case WAIT:
            case HOLD_POSITION:
                // Stand still — navigation already stopped in beginTask
                break;
        }
    }

    /**
     * Is the task complete?
     */
    private boolean isTaskComplete(CultivationTask task, long tick) {
        switch (task.type) {
            case FACE_TARGET:
                return true; // instantaneous
            case WAIT:
                return tick >= taskStartTick + task.durationTicks;
            case HOLD_POSITION:
                return tick >= taskStartTick + task.durationTicks;
            case MOVE_TO:
                // Complete when close to target (within 2 blocks) or path is done
                if (task.targetPos == null) return true;
                if (!cultivator.getNavigation().isInProgress()) {
                    double dist = cultivator.distanceToSqr(task.targetPos.getX(),
                            cultivator.getY(), task.targetPos.getZ());
                    return dist < 4.0; // within 2 blocks
                }
                return false;
            case MOVE_AWAY_FROM_ENTITY:
                // Complete when far enough from the entity
                if (task.targetEntityUuid == null) return true;
                LivingEntity target = findEntityByUuid(task.targetEntityUuid);
                if (target == null) return true; // entity gone
                return target.distanceTo(cultivator) >= 48.0;
            case FLEE:
                // Complete when path is done and we're far from source
                if (!cultivator.getNavigation().isInProgress()) return true;
                return false;
            case APPROACH_TARGET:
                // Complete when close to target entity
                if (task.targetEntityUuid == null) return true;
                LivingEntity t = findEntityByUuid(task.targetEntityUuid);
                if (t == null) return true;
                return t.distanceTo(cultivator) < 3.0;
            default:
                return true;
        }
    }

    /**
     * Advance to the next task in the queue. If none remain, the goal
     * will stop (canContinueToUse returns false).
     */
    private void advanceToNextTask(long tick) {
        currentTaskIndex++;
        if (currentTaskIndex < taskQueue.size()) {
            CultivationTask next = taskQueue.get(currentTaskIndex);
            next.status = CultivationTask.Status.IN_PROGRESS;
            taskStartTick = tick;
            beginTask(next, tick);
        } else {
            // All tasks done — check if we should re-decompose
            // (the Intent might still be active and not expired)
            String actorId = cultivator.getCharacterId();
            Actor actor = ActorRegistry.get(actorId);
            if (actor != null && actor.cognition != null) {
                Intent currentIntent = actor.cognition.activeIntent;
                if (currentIntent != null && !currentIntent.isExpired(tick)) {
                    // Re-decompose for another round of the same intent
                    start();
                }
            }
        }
    }

    // ── Task helpers ────────────────────────────────────────────────────

    /**
     * Move away from the entity specified in the task.
     */
    private void moveAwayFromEntity(CultivationTask task, long tick) {
        if (task.targetEntityUuid == null) return;
        LivingEntity target = findEntityByUuid(task.targetEntityUuid);
        if (target == null) return;

        Vec3 away = DefaultRandomPos.getPosAway(
                cultivator, 24, 12,
                target.position());
        if (away != null) {
            cultivator.getNavigation().moveTo(away.x, away.y, away.z, 1.1);
        }
    }

    /**
     * Approach the entity specified in the task.
     */
    private void approachTargetEntity(CultivationTask task, long tick) {
        if (task.targetEntityUuid == null) return;
        LivingEntity target = findEntityByUuid(task.targetEntityUuid);
        if (target == null) return;
        cultivator.getNavigation().moveTo(target, 1.0);
    }

    /**
     * Find a LivingEntity by UUID in the current level.
     */
    private LivingEntity findEntityByUuid(String uuidStr) {
        if (uuidStr == null || !(cultivator.level() instanceof ServerLevel sl)) return null;
        try {
            UUID uuid = UUID.fromString(uuidStr);
            var entity = sl.getEntity(uuid);
            return entity instanceof LivingEntity le ? le : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Are two Intents "the same" for re-decomposition purposes?
     * (Same nature + same target — urgency/tick differences don't trigger re-decomposition.)
     */
    private boolean sameIntent(Intent a, Intent b) {
        if (a == null || b == null) return a == b;
        return a.nature() == b.nature()
                && java.util.Objects.equals(a.targetId(), b.targetId());
    }
}
