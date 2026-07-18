package dev.ergenverse.simulation.intent;

import net.minecraft.core.BlockPos;

/**
 * CultivationTask — a concrete, executable unit of work produced by
 * decomposing an {@link Intent}.
 *
 * <p>Per the ChatGPT architectural review: "Intent decomposes into
 * CultivationTask list, each maps to a Minecraft Goal via TaskToGoalAdapter."
 *
 * <p>A CultivationTask is the bridge between the simulation layer (Intent,
 * which is abstract strategic framing) and the Minecraft layer (Goals, which
 * are concrete entity behaviors). Each task is a SINGLE executable step:
 * "move to this position", "wait here for N ticks", "face this entity".
 *
 * <h2>Task types</h2>
 * <ul>
 *   <li><b>MOVE_TO</b> — path to a target block position. Used by
 *       SEEK_OPPORTUNITY (move toward interesting location),
 *       OBSERVE_FROM_DISTANCE (move to vantage point),
 *       RETREAT_TACTICALLY (move away from threat),
 *       EXPLORE_CAUTIOUSLY (move in a direction).</li>
 *   <li><b>MOVE_AWAY_FROM_ENTITY</b> — path away from a specific entity
 *       (usually the nearest player). Used by AVOID_REVEALING_STRENGTH
 *       when a player is too close.</li>
 *   <li><b>WAIT</b> — stand still for N ticks. Used by OBSERVE_FROM_DISTANCE
 *       after reaching a vantage point, CULTIVATE_SECRETLY after finding
 *       cover, TEST_JUDGMENT while observing a target.</li>
 *   <li><b>FACE_TARGET</b> — turn to face a specific block position.
 *       Used by OBSERVE_FROM_DISTANCE, PROVOKE, ESTABLISH_DOMINANCE.</li>
 *   <li><b>HOLD_POSITION</b> — do not move, defend this spot. Used by
 *       DEFEND_POSITION, PROTECT_ASSET.</li>
 *   <li><b>FLEE</b> — panic run from a position. Used by RETREAT_TACTICALLY
 *       when urgency is very high.</li>
 *   <li><b>APPROACH_TARGET</b> — path toward a specific entity (for trade,
 *       negotiate, teach). Used by NEGOTIATE, OFFER_DISCIPLE.</li>
 * </ul>
 *
 * <h2>Completion</h2>
 * <p>Each task has a {@link Type#isInstantaneous} flag. Instantaneous tasks
 * (FACE_TARGET) complete on the first tick. Duration tasks (WAIT) complete
 * after N ticks. Movement tasks (MOVE_TO, FLEE, APPROACH) complete when the
 * entity reaches the target OR a max-duration timeout expires (prevents
 * stuck entities from blocking the cognition pipeline forever).
 *
 * <p><b>Provenance: INFERRED.</b> The task types are distilled from Wang Lin's
 * behavioral patterns. The decomposition rules (which intents produce which
 * tasks) are in {@link IntentDecomposer}.
 */
public final class CultivationTask {

    /** The type of executable behavior this task represents. */
    public enum Type {
        /** Path to a target block position. Completes on arrival or timeout. */
        MOVE_TO(false),
        /** Path away from a specific entity (by UUID string). Completes when safe distance reached. */
        MOVE_AWAY_FROM_ENTITY(false),
        /** Stand still for {@link #durationTicks} ticks. */
        WAIT(false),
        /** Turn to face a target block position. Instantaneous (1 tick). */
        FACE_TARGET(true),
        /** Do not move; defend the current position. Lasts {@link #durationTicks}. */
        HOLD_POSITION(false),
        /** Panic-run from a source position. Completes when far enough or timeout. */
        FLEE(false),
        /** Path toward a specific entity (by UUID string). Completes on arrival. */
        APPROACH_TARGET(false);

        private final boolean instantaneous;
        Type(boolean instantaneous) { this.instantaneous = instantaneous; }
        public boolean isInstantaneous() { return instantaneous; }
    }

    /** Task status — tracked by the executor (CognitionDrivenGoal). */
    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, TIMED_OUT
    }

    public final Type type;
    public final BlockPos targetPos;       // for MOVE_TO, FACE_TARGET, FLEE (source), HOLD_POSITION (anchor)
    public final String targetEntityUuid;  // for MOVE_AWAY_FROM_ENTITY, APPROACH_TARGET
    public final long durationTicks;       // for WAIT, HOLD_POSITION; max-timeout for movement tasks
    public final long createdAtTick;
    public final String sourceIntentLabel; // for diagnostics — which Intent produced this task

    public Status status = Status.PENDING;

    private CultivationTask(Type type, BlockPos targetPos, String targetEntityUuid,
                            long durationTicks, long createdAtTick, String sourceIntentLabel) {
        this.type = type;
        this.targetPos = targetPos;
        this.targetEntityUuid = targetEntityUuid;
        this.durationTicks = durationTicks;
        this.createdAtTick = createdAtTick;
        this.sourceIntentLabel = sourceIntentLabel;
    }

    // ── Factory methods ─────────────────────────────────────────────────

    /** Move to a specific block position. */
    public static CultivationTask moveTo(BlockPos pos, long maxDurationTicks, long tick, String intentLabel) {
        return new CultivationTask(Type.MOVE_TO, pos, null, maxDurationTicks, tick, intentLabel);
    }

    /** Move away from a specific entity (by UUID). */
    public static CultivationTask moveAwayFrom(String entityUuid, long maxDurationTicks, long tick, String intentLabel) {
        return new CultivationTask(Type.MOVE_AWAY_FROM_ENTITY, null, entityUuid, maxDurationTicks, tick, intentLabel);
    }

    /** Wait (stand still) for N ticks. */
    public static CultivationTask wait(long durationTicks, long tick, String intentLabel) {
        return new CultivationTask(Type.WAIT, null, null, durationTicks, tick, intentLabel);
    }

    /** Face a specific block position (instantaneous). */
    public static CultivationTask faceTarget(BlockPos pos, long tick, String intentLabel) {
        return new CultivationTask(Type.FACE_TARGET, pos, null, 1L, tick, intentLabel);
    }

    /** Hold position (defend) for N ticks. */
    public static CultivationTask holdPosition(long durationTicks, long tick, String intentLabel) {
        return new CultivationTask(Type.HOLD_POSITION, null, null, durationTicks, tick, intentLabel);
    }

    /** Flee from a source position. */
    public static CultivationTask flee(BlockPos fromPos, long maxDurationTicks, long tick, String intentLabel) {
        return new CultivationTask(Type.FLEE, fromPos, null, maxDurationTicks, tick, intentLabel);
    }

    /** Approach a specific entity (by UUID). */
    public static CultivationTask approachTarget(String entityUuid, long maxDurationTicks, long tick, String intentLabel) {
        return new CultivationTask(Type.APPROACH_TARGET, null, entityUuid, maxDurationTicks, tick, intentLabel);
    }

    /** Has this task exceeded its max duration (for timeout checks)? */
    public boolean isTimedOut(long currentTick) {
        if (type.isInstantaneous()) return false;
        return currentTick >= createdAtTick + durationTicks;
    }

    @Override
    public String toString() {
        return "Task[" + type + (targetPos != null ? " @" + targetPos.getX() + "," + targetPos.getZ() : "")
                + (targetEntityUuid != null ? " entity=" + targetEntityUuid.substring(0, Math.min(8, targetEntityUuid.length())) : "")
                + " dur=" + durationTicks + "t"
                + " from=" + sourceIntentLabel
                + " " + status + "]";
    }
}
