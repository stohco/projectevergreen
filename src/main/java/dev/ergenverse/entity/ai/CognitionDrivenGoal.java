package dev.ergenverse.entity.ai;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.cognition.Ontology;
import dev.ergenverse.simulation.cognition.perception.PerceptionSnapshot;
import dev.ergenverse.simulation.intent.ActorEntityLink;
import dev.ergenverse.simulation.intent.Commitment;
import dev.ergenverse.simulation.intent.CommitmentContext;
import dev.ergenverse.simulation.intent.CultivationTask;
import dev.ergenverse.simulation.intent.Intent;
import dev.ergenverse.simulation.intent.IntentDecomposer;
import dev.ergenverse.simulation.intent.IntentNature;
import dev.ergenverse.simulation.intent.Performance;

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
     * Can this goal run? Yes, IF there is a linked Actor with an active
     * Intent OR an actionable Commitment.
     *
     * <p>Article XLV §3: when a Commitment is active (FORMED or ACTIVE
     * status), this goal honors the commitment and derives its Intent
     * from {@link Commitment#toIntent}. The commitment persists across
     * ticks — the NPC does not re-evaluate every tick. This is the
     * difference between an NPC that dithers and an NPC that holds its
     * course.
     *
     * <p>If no Actor is linked, or the Actor has neither a commitment
     * nor an Intent, this goal yields to lower-priority goals
     * (RandomStroll, RandomLookAround, and the deprecated
     * any lower-priority idle goals).
     */
    @Override
    public boolean canUse() {
        if (cultivator.level().isClientSide) return false;
        String actorId = cultivator.getCharacterId();
        if (actorId == null || actorId.isEmpty()) return false;

        Actor actor = ActorRegistry.get(actorId);
        if (actor == null || actor.cognition == null) return false;

        // Honor an active commitment first — it persists across ticks.
        Commitment commitment = actor.cognition.activeCommitment;
        if (commitment != null && commitment.isActionable()) {
            return true;
        }

        // Fall back to per-tick Intent (flickers, but better than nothing).
        Intent intent = actor.cognition.activeIntent;
        return intent != null;
    }

    /**
     * Can this goal keep running? Yes, if the Commitment is still
     * actionable OR the Intent is still active and not expired, and
     * there are tasks remaining.
     *
     * <p>Commitment-driven continuation: if a commitment is active, the
     * goal continues as long as the commitment is not expired, abandoned,
     * or fulfilled. The per-tick Intent may flicker (e.g. AVOID_PLAYER
     * when a player walks past), but the commitment holds — the goal
     * does NOT re-decompose on every Intent change, only on commitment
     * expiry/abandonment.
     */
    @Override
    public boolean canContinueToUse() {
        if (cultivator.level().isClientSide) return false;
        String actorId = cultivator.getCharacterId();
        if (actorId == null || actorId.isEmpty()) return false;

        Actor actor = ActorRegistry.get(actorId);
        if (actor == null || actor.cognition == null) return false;

        long tick = cultivator.level().getGameTime();

        // Commitment-driven path: continue if the commitment is still actionable.
        // Per the user's design review (CRON-COMPLETIONIST-12): the world
        // decides when a commitment ends, not a timer. We evaluate the
        // commitment's success and abandon predicates against the current
        // CommitmentContext each tick. The timer is merely bug insurance.
        Commitment commitment = actor.cognition.activeCommitment;
        if (commitment != null && commitment.isActionable()) {
            CommitmentContext ctx = buildCommitmentContext(actor, tick);

            // Success condition fired → COMPLETED (the actor achieved it).
            if (commitment.isFulfilled(ctx)) {
                commitment.status = Commitment.Status.COMPLETED;
                actor.cognition.activeCommitment = null;
                logCommitmentEnd(actor, commitment, "success");
                return false;
            }
            // Abandon condition fired (or safety-net max duration elapsed)
            // → ABANDONED (or COMPLETED via MAX_DURATION_ELAPSED).
            if (commitment.shouldAbandon(ctx)) {
                if (commitment.endReason == Commitment.CompletionReason.MAX_DURATION_ELAPSED) {
                    commitment.status = Commitment.Status.COMPLETED;
                } else {
                    commitment.status = Commitment.Status.ABANDONED;
                }
                actor.cognition.activeCommitment = null;
                logCommitmentEnd(actor, commitment,
                        commitment.endReason == Commitment.CompletionReason.MAX_DURATION_ELAPSED
                                ? "safety-net-expired" : "abandoned");
                return false;
            }
            return currentTaskIndex >= 0 && currentTaskIndex < taskQueue.size();
        }

        // Intent-driven fallback path (per-tick flicker).
        Intent currentIntent = actor.cognition.activeIntent;
        if (currentIntent == null) return false;

        // If the Intent changed since we started, re-decompose
        if (sourceIntent == null || !sameIntent(sourceIntent, currentIntent)) {
            return true; // keep running — start() will re-decompose
        }

        // Check if Intent expired
        if (currentIntent.isExpired(tick)) return false;

        // Check if we have tasks remaining
        return currentTaskIndex >= 0 && currentTaskIndex < taskQueue.size();
    }

    /**
     * Start the goal — derive the effective Intent (from Commitment if
     * active, else from activeIntent) and decompose it into tasks.
     *
     * <p>Commitment-driven start: if a commitment is actionable, mark
     * it ACTIVE and derive the per-tick Intent from
     * {@link Commitment#toIntent}. This Intent has a duration equal to
     * the commitment's remaining persistence, so it will NOT expire
     * before the commitment does. The goal then decomposes this Intent
     * as usual.
     *
     * <p>CRON-COMPLETIONIST-19: Also projects the commitment onto the
     * entity's body language in real time. The pose is set from the
     * IntentNature, the attention lock is engaged so RandomLookAroundGoal
     * is suppressed, and the look-target is initialized from the actor's
     * last perception. This is the bridge the user named: "Suppose Wang
     * Lin decides 'Observe wolves.' That's wonderful. Now ask: Can the
     * player tell? Without debug overlay, command, logs — just looking."
     */
    @Override
    public void start() {
        String actorId = cultivator.getCharacterId();
        Actor actor = ActorRegistry.get(actorId);
        if (actor == null || actor.cognition == null) return;

        long tick = cultivator.level().getGameTime();

        // Derive the effective Intent — commitment takes precedence.
        Commitment commitment = actor.cognition.activeCommitment;
        Intent intent;
        boolean fromCommitment = false;
        if (commitment != null && commitment.isActionable()) {
            if (commitment.status == Commitment.Status.FORMED) {
                commitment.status = Commitment.Status.ACTIVE;
            }
            intent = commitment.toIntent(tick);
            fromCommitment = true;
        } else {
            intent = actor.cognition.activeIntent;
        }
        if (intent == null) return;

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

        // ── CRON-COMPLETIONIST-19: project the commitment onto body language ──
        // This is the missing bridge the user named. The pose is set in real
        // time from the active IntentNature, NOT only at settlement-scan time.
        // The attention lock engages so the vanilla RandomLookAroundGoal is
        // suppressed (the user: "doesn't respond immediately to player").
        // The look-target is initialized from the actor's last perception and
        // updated each tick in tick().
        //
        // ── CRON-COMPLETIONIST-21: Acting Layer ──
        // The pose is still set (as a coarse fallback), but the PRIMARY driver
        // is now the Performance channel bundle. The user's 2026-07-26 review:
        // "instead of thinking in poses, think in independent channels." Same
        // IntentNature + different context → different Performance → different
        // acting. The renderer consumes the seven channels independently.
        if (fromCommitment) {
            int pose = poseForIntent(intent.nature());
            cultivator.setCultivatorPose(pose);
            cultivator.setCognitiveAttentionLock(true);

            // Compute the Performance from the commitment + context. The
            // threat/concealment scalars are read from the actor's last
            // perception/situation (0 if absent — neutral modulation).
            float threatIntensity = deriveThreatIntensity(actor);
            float concealmentPressure = deriveConcealmentPressure(actor, intent.nature());
            Performance perf = Performance.interpret(
                    intent.nature(), commitment.targetId,
                    threatIntensity, concealmentPressure);
            cultivator.setPerformance(
                    perf.focus, perf.urgency, perf.confidence,
                    perf.concealment, perf.tension, perf.patience, perf.fatigue);

            // CRON-COMPLETIONIST-21: pin the attention object at commitment
            // start. The look-target resolver will track THIS entity across
            // ticks (nearest to the pin, not nearest to the NPC) so Wang Lin
            // keeps watching the alpha wolf even if a lesser wolf wanders
            // closer. The user: "that tiny detail makes the NPC appear to have
            // intention rather than a targeting heuristic."
            PerceptionSnapshot.PerceivedEntity initialTarget = pickAttentionObject(actor, null);
            if (initialTarget != null) {
                cultivator.pinAttentionObject(
                        (float) initialTarget.posX,
                        (float) (initialTarget.posY + 1.0),
                        (float) initialTarget.posZ);
            } else {
                cultivator.clearAttentionPin();
            }

            updateCognitiveLookTarget(actor);
            Ergenverse.LOGGER.info("[CognitionDrivenGoal] {} acting: pose={} perf={} (from {})",
                    actorId, poseName(pose), perf, intent.nature().label);
        }

        // Log (rate-limited — only when intent label changes)
        if (!intent.nature().label.equals(lastLoggedIntent)) {
            Ergenverse.LOGGER.info("[CognitionDrivenGoal] {} starting {} '{}' → {} tasks",
                    actorId,
                    fromCommitment ? "commitment-driven intent" : "intent",
                    intent.nature().label, taskQueue.size());
            lastLoggedIntent = intent.nature().label;
        }
    }

    /**
     * Stop the goal — clear the task queue and navigation.
     *
     * <p>CRON-COMPLETIONIST-19: Also clear the cognitive look-target and
     * release the attention lock so vanilla look control resumes. This is
     * critical: if we don't clear these, the NPC's head stays locked on the
     * last-known wolf position forever, even after the commitment ends.
     */
    @Override
    public void stop() {
        taskQueue.clear();
        currentTaskIndex = -1;
        sourceIntent = null;
        cultivator.getNavigation().stop();
        // CRON-COMPLETIONIST-19: release body-language state.
        cultivator.setCognitiveAttentionLock(false);
        cultivator.clearCognitiveLookTarget();
        // CRON-COMPLETIONIST-21: release the Acting Layer state too. The
        // Performance channels go NaN (renderer falls back to pose/vanilla),
        // and the attention-object pin is released so the next commitment
        // starts fresh.
        cultivator.clearPerformance();
        cultivator.clearAttentionPin();
        // Restore idle pose only if we were cognition-driven. If the entity
        // is activity-locked (settlement-scan pose), leave the pose alone —
        // the materializer owns it in that case.
        if (!cultivator.isActivityLocked()) {
            cultivator.setCultivatorPose(EntityCultivator.POSE_IDLE);
        }
    }

    /**
     * Tick — execute the current task.
     *
     * <p>CRON-COMPLETIONIST-19: also refreshes the cognitive look-target
     * each tick from the actor's latest perception. As the wolves move,
     * Wang Lin's head tracks them. This is the real-time body-language
     * projection the user's directive demands.
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

        // CRON-COMPLETIONIST-19: refresh the cognitive look-target from the
        // actor's latest perception. This runs every tick so the NPC's head
        // tracks moving targets (wolves stalking through the treeline).
        String actorId = cultivator.getCharacterId();
        Actor actor = ActorRegistry.get(actorId);
        if (actor != null && actor.cognition != null
                && actor.cognition.activeCommitment != null
                && actor.cognition.activeCommitment.isActionable()) {
            updateCognitiveLookTarget(actor);
        }

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

    /**
     * Build a {@link CommitmentContext} for the current tick. The context
     * carries the world state the commitment's success/abandon predicates
     * evaluate against.
     *
     * <p>Per the user's design review: "The world should decide when a
     * commitment ends. Not a timer." The context IS the world, handed
     * to the predicates. Predicates null-check what they need.
     *
     * <p>The situation is fetched from the current WorldEventBus level
     * (if available). The perception is the actor's last filtered
     * perception. Both may be null — predicates must handle that.
     */
    private CommitmentContext buildCommitmentContext(Actor actor, long tick) {
        // CRON-COMPLETIONIST-15: Use the actor's stashed WorldSituation.
        // Previously this was always null (ActorMaterializer didn't stash it).
        // Now predicates that need the situation (threat intensity, time-of-day,
        // nearby opportunities) can read ctx.situation() instead of returning
        // false.
        return new CommitmentContext(
                tick,
                actor,
                actor.lastSituation,
                actor.lastPerception);
    }

    /**
     * Log a commitment's terminal transition (rate-limited to avoid spam).
     */
    private void logCommitmentEnd(Actor actor, Commitment commitment, String how) {
        Ergenverse.LOGGER.info("[CognitionDrivenGoal] {} commitment ended: {} → {} ({}). Reason: \"{}\"",
                actor.id,
                commitment.intentNature.label,
                how,
                commitment.endReason,
                commitment.reason);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CRON-COMPLETIONIST-19: Cognitive Body-Language projection
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Map an {@link IntentNature} to an EntityCultivator pose constant.
     *
     * <p>This is the real-time projection the user's 2026-07-25 directive
     * demands. When a Commitment is active, the entity's pose is set from
     * its IntentNature EVERY time the goal starts — not just at settlement
     * scan. Wang Lin committing to OBSERVE_FROM_DISTANCE immediately shows
     * POSE_OBSERVING (crouched, hand at brow, head raised).
     *
     * <p>The mapping is designed so that the player can READ the NPC's
     * cognitive state from its silhouette alone:
     * <ul>
     *   <li>Observation intents (OBSERVE_FROM_DISTANCE, GATHER_INTEL,
     *       EXPLORE_CAUTIOUSLY, AVOID_REVEALING_STRENGTH,
     *       CULTIVATE_SECRETLY) → POSE_OBSERVING — crouched, watchful,
     *       hand shielding brow, head raised. "He's watching something."</li>
     *   <li>Defensive intents (PROTECT_ASSET, DEFEND_POSITION,
     *       ESTABLISH_DOMINANCE, AMBUSH, DECEIVE, PROVOKE) →
     *       POSE_GUARDING — feet wide, arms forward, combat-ready tension.
     *       "He's braced for something."</li>
     *   <li>Purposeful-movement intents (SEEK_OPPORTUNITY,
     *       ADVANCE_OPPORTUNISTICALLY, RETREAT_TACTICALLY) →
     *       POSE_PURSUING — body leaning forward, eyes on destination.
     *       "He's going somewhere with intent."</li>
     *   <li>Social intents (NEGOTIATE, TEST_JUDGMENT, MAINTAIN_COVER) →
     *       POSE_SOCIALIZING — relaxed, gesturing, weight shifted.
     *       "He's talking to someone."</li>
     *   <li>Stealth cultivation (CULTIVATE_SECRETLY in some contexts) →
     *       POSE_MEDITATING — hands at chest, head bowed.
     *       "He's cultivating."</li>
     * </ul>
     *
     * @param nature the active IntentNature
     * @return the EntityCultivator pose constant
     */
    private static int poseForIntent(IntentNature nature) {
        if (nature == null) return EntityCultivator.POSE_IDLE;
        return switch (nature) {
            case OBSERVE_FROM_DISTANCE, GATHER_INTEL, EXPLORE_CAUTIOUSLY,
                 AVOID_REVEALING_STRENGTH -> EntityCultivator.POSE_OBSERVING;
            case PROTECT_ASSET, DEFEND_POSITION, ESTABLISH_DOMINANCE,
                 AMBUSH, DECEIVE, PROVOKE -> EntityCultivator.POSE_GUARDING;
            case SEEK_OPPORTUNITY, ADVANCE_OPPORTUNISTICALLY,
                 RETREAT_TACTICALLY -> EntityCultivator.POSE_PURSUING;
            case NEGOTIATE, TEST_JUDGMENT, MAINTAIN_COVER -> EntityCultivator.POSE_SOCIALIZING;
            case CULTIVATE_SECRETLY -> EntityCultivator.POSE_MEDITATING;
        };
    }

    /** Human-readable pose name for logging. */
    private static String poseName(int pose) {
        return switch (pose) {
            case EntityCultivator.POSE_IDLE -> "IDLE";
            case EntityCultivator.POSE_MEDITATING -> "MEDITATING";
            case EntityCultivator.POSE_CASTING -> "CASTING";
            case EntityCultivator.POSE_OBSERVING -> "OBSERVING";
            case EntityCultivator.POSE_GUARDING -> "GUARDING";
            case EntityCultivator.POSE_PURSUING -> "PURSUING";
            case EntityCultivator.POSE_SOCIALIZING -> "SOCIALIZING";
            default -> "POSE_" + pose;
        };
    }

    /**
     * Update the cognitive look-target from the actor's latest perception.
     *
     * <p>CRON-COMPLETIONIST-21: Attention Object ownership. If an attention
     * pin is held (set at commitment start), the resolver prefers the
     * perceived entity NEAREST THE PINNED POSITION — not the nearest to the
     * NPC. This is the user's directive: "Wang Lin keeps watching THAT wolf
     * even if another wolf walks slightly closer. That tiny detail makes the
     * NPC appear to have intention rather than a targeting heuristic." The
     * pin updates as the tracked entity moves (re-sighted each tick). If the
     * pinned entity vanishes from perception for too long, the pin releases
     * and the resolver falls back to nearest-hostile.
     *
     * <p>If no pin is held (or it was just released), pick the highest-priority
     * perceived entity (hostile > prey > ally > witness) and establish a new
     * pin from it. This is the CRON-19 behavior, preserved as the fallback.
     *
     * <p>If no perceived entity is available at all, the look-target is cleared
     * (the NPC's head returns to vanilla look control) AND the pin ages. This
     * is correct: an observing NPC with no wolves in sight shouldn't stare at
     * a fixed point in the void — but he REMEMBERS which wolf he was watching,
     * so if it reappears within the staleness window, he snaps back to it
     * rather than re-pinning to a different wolf.
     */
    private void updateCognitiveLookTarget(Actor actor) {
        PerceptionSnapshot perception = actor.lastPerception;
        if (perception == null || perception.nearbyEntities == null
                || perception.nearbyEntities.isEmpty()) {
            // No perception — clear the look target so the head doesn't
            // freeze on a stale position, but AGE the pin (don't clear it
            // yet — the wolf may reappear within the staleness window).
            cultivator.clearCognitiveLookTarget();
            if (cultivator.hasAttentionPin()) {
                cultivator.ageAttentionPin();
            }
            return;
        }

        // ── Attention-object ownership path ──
        // If we hold a pin, find the perceived entity nearest the PIN (not
        // the NPC). Within the stickiness radius, that's our target — update
        // the pin to the entity's current position and set the look-target.
        PerceptionSnapshot.PerceivedEntity target = null;
        if (cultivator.hasAttentionPin()) {
            double pinX = cultivator.getAttentionPinX();
            double pinY = cultivator.getAttentionPinY();
            double pinZ = cultivator.getAttentionPinZ();
            double bestPinDist = Double.MAX_VALUE;
            double stickinessRadius = 8.0; // blocks — a wolf within 8 blocks
                                            // of where we last saw it is "the same wolf"
            for (PerceptionSnapshot.PerceivedEntity e : perception.nearbyEntities) {
                int pri = classificationPriority(e.classification);
                if (pri < 0) continue;
                double dx = e.posX - pinX;
                double dy = e.posY - (pinY - 1.0); // un-offset the eye-height
                double dz = e.posZ - pinZ;
                double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (d < stickinessRadius && d < bestPinDist) {
                    bestPinDist = d;
                    target = e;
                }
            }
            if (target != null) {
                // Re-sighted — update the pin to the entity's current position
                // and set the look-target. The pin now tracks the moving wolf.
                cultivator.updateAttentionPin(
                        (float) target.posX,
                        (float) (target.posY + 1.0),
                        (float) target.posZ);
                cultivator.setCognitiveLookTarget(
                        (float) target.posX,
                        (float) (target.posY + 1.0),
                        (float) target.posZ);
                return;
            }
            // Pin held but no entity near it — age the pin. If it ages out,
            // hasAttentionPin() will be false next tick and we fall through
            // to the re-pin path below.
            cultivator.ageAttentionPin();
            if (cultivator.hasAttentionPin()) {
                // Pin still held (not yet stale) but nothing to look at —
                // clear the look-target this tick; the head drifts naturally.
                cultivator.clearCognitiveLookTarget();
                return;
            }
            // Pin released — fall through to re-pin from nearest-hostile.
        }

        // ── Fallback / re-pin path: pick by priority + proximity to NPC ──
        target = pickAttentionObject(actor, null);
        if (target == null) {
            cultivator.clearCognitiveLookTarget();
            return;
        }
        // Establish a fresh pin from this entity.
        cultivator.pinAttentionObject(
                (float) target.posX,
                (float) (target.posY + 1.0),
                (float) target.posZ);
        cultivator.setCognitiveLookTarget(
                (float) target.posX,
                (float) (target.posY + 1.0),
                (float) target.posZ);
    }

    /**
     * Pick the best perceived entity to attend to, by priority then proximity.
     *
     * <p>Used both at commitment start (to establish the initial pin) and as
     * the fallback when a pin has released (to re-pin). The {@code exclude}
     * parameter is reserved for future use (e.g. avoiding re-pinning to an
     * entity that just fled); pass null for now.
     */
    private PerceptionSnapshot.PerceivedEntity pickAttentionObject(
            Actor actor, PerceptionSnapshot.PerceivedEntity exclude) {
        PerceptionSnapshot perception = actor.lastPerception;
        if (perception == null || perception.nearbyEntities == null
                || perception.nearbyEntities.isEmpty()) {
            return null;
        }
        PerceptionSnapshot.PerceivedEntity target = null;
        int bestPriority = -1;
        double bestDist = Double.MAX_VALUE;
        for (PerceptionSnapshot.PerceivedEntity e : perception.nearbyEntities) {
            if (e == exclude) continue;
            int pri = classificationPriority(e.classification);
            if (pri < 0) continue;
            if (pri > bestPriority
                    || (pri == bestPriority && e.distanceBlocks < bestDist)) {
                bestPriority = pri;
                bestDist = e.distanceBlocks;
                target = e;
            }
        }
        return target;
    }

    /**
     * Derive a 0–1 threat-intensity scalar from the actor's last perception
     * and situation. Used to modulate the Performance (higher threat → more
     * urgency/tension, less confidence).
     *
     * <p>Heuristic: if the perception flags a threat, base 0.5. Add up to 0.3
     * for nearby hostiles (closer = more). Add up to 0.2 from the situation's
     * threat intensity if available. Clamped to [0,1]. Returns 0 if no data.
     */
    private float deriveThreatIntensity(Actor actor) {
        float t = 0.0f;
        if (actor.lastPerception != null) {
            if (actor.lastPerception.hasThreat) t += 0.5f;
            // Closer hostiles raise the intensity
            if (actor.lastPerception.nearbyEntities != null) {
                double closestHostile = Double.MAX_VALUE;
                for (PerceptionSnapshot.PerceivedEntity e : actor.lastPerception.nearbyEntities) {
                    if ("hostile".equals(e.classification) && e.distanceBlocks < closestHostile) {
                        closestHostile = e.distanceBlocks;
                    }
                }
                if (closestHostile < Double.MAX_VALUE) {
                    // within 16 blocks → up to +0.3; at 0 blocks → +0.3, at 16+ → +0
                    t += Math.max(0.0f, 0.3f * (1.0f - (float) (closestHostile / 16.0)));
                }
            }
        }
        if (actor.lastSituation != null && actor.lastSituation.primaryThreat != null) {
            // The situation carries a primaryThreat record with an intensity
            // scalar (0–1). Add up to 0.2 from it.
            float sit = actor.lastSituation.primaryThreat.intensity();
            if (sit > 0) t += Math.min(0.2f, sit * 0.2f);
        }
        return Math.max(0.0f, Math.min(1.0f, t));
    }

    /**
     * Derive a 0–1 concealment-pressure scalar — how important it is for this
     * NPC to stay hidden right now. Used to modulate the Performance (higher
     * pressure → more concealment/tension).
     *
     * <p>Heuristic: certain IntentNatures inherently demand concealment
     * (AVOID_REVEALING_STRENGTH, CULTIVATE_SECRETLY, AMBUSH, DECEIVE,
     * MAINTAIN_COVER). For those, base 0.7. For observation intents near a
     * player, add 0.2. Otherwise base 0.2. Clamped to [0,1].
     */
    private float deriveConcealmentPressure(Actor actor, IntentNature nature) {
        float c = 0.2f;
        if (nature == IntentNature.AVOID_REVEALING_STRENGTH
                || nature == IntentNature.CULTIVATE_SECRETLY
                || nature == IntentNature.AMBUSH
                || nature == IntentNature.DECEIVE
                || nature == IntentNature.MAINTAIN_COVER) {
            c = 0.7f;
        }
        // If a player is nearby, concealment matters more for observation intents
        if (nature == IntentNature.OBSERVE_FROM_DISTANCE
                || nature == IntentNature.GATHER_INTEL
                || nature == IntentNature.EXPLORE_CAUTIOUSLY) {
            net.minecraft.world.entity.player.Player nearest =
                    cultivator.level().getNearestPlayer(cultivator, 32.0);
            if (nearest != null) {
                c += 0.2f;
            }
        }
        return Math.max(0.0f, Math.min(1.0f, c));
    }

    /**
     * Priority of a perceived entity classification for look-target selection.
     * Higher = more important to look at. -1 = ignore (not a look target).
     */
    private static int classificationPriority(String classification) {
        if (classification == null) return -1;
        return switch (classification) {
            case "hostile" -> 4;   // the wolf — always look at it
            case "prey" -> 3;      // hunted creature
            case "ally" -> 2;      // family member (for social commitments)
            case "witness" -> 1;   // bystander
            case "neutral" -> 0;   // neutral creature
            default -> -1;         // "unknown" — ignore
        };
    }
}
