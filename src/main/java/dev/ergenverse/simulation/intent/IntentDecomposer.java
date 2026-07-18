package dev.ergenverse.simulation.intent;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * IntentDecomposer — decomposes an {@link Intent} into a list of concrete
 * {@link CultivationTask}s.
 *
 * <p>Per the ChatGPT architectural review: "Intent decomposes into
 * CultivationTask list, each maps to a Minecraft Goal via TaskToGoalAdapter."
 *
 * <p>This is WHERE canon behavior becomes concrete. Each of the 17
 * IntentNature types has a specific decomposition pattern. Wang Lin's
 * AVOID_REVEALING_STRENGTH produces different tasks than a SLAUGHTER-dao
 * NPC's AMBUSH — even if both are "standing near a glade".
 *
 * <h2>Canon-faithful decomposition table</h2>
 * <pre>
 *   AVOID_REVEALING_STRENGTH:
 *     if player within 32 blocks → [MOVE_AWAY_FROM player, WAIT 200t, FACE_TARGET glade]
 *     else                       → [WAIT 600t, FACE_TARGET glade]
 *     Wang Lin pretends weak, moves off, observes from cover.
 *
 *   OBSERVE_FROM_DISTANCE:
 *     → [MOVE_TO vantage_point (24-32 blocks from target), FACE_TARGET target, WAIT 1200t]
 *     "watch from a hill, don't engage"
 *
 *   SEEK_OPPORTUNITY:
 *     → [MOVE_TO nearest_interesting_location, WAIT 200t, FACE_TARGET location]
 *     "move toward the qi fluctuation"
 *
 *   EXPLORE_CAUTIOUSLY:
 *     → [MOVE_TO random_point_within_48_blocks, WAIT 100t, MOVE_TO next_point]
 *     "poke around carefully, pause to look"
 *
 *   CULTIVATE_SECRETLY:
 *     → [MOVE_TO nearest_cover, HOLD_POSITION 6000t]
 *     "find a hidden spot, sit down for a long time"
 *
 *   RETREAT_TACTICALLY:
 *     if urgency > 0.7 → [FLEE from threat]
 *     else             → [MOVE_AWAY_FROM threat, WAIT 200t]
 *
 *   DEFEND_POSITION / PROTECT_ASSET:
 *     → [HOLD_POSITION 2400t, FACE_TARGET nearest_threat]
 *
 *   GATHER_INTEL:
 *     → [MOVE_TO vantage_point, WAIT 600t, FACE_TARGET target]
 *
 *   AMBUSH:
 *     → [MOVE_TO concealment_near_target, HOLD_POSITION 3600t, FACE_TARGET target]
 *
 *   NEGOTIATE / APPROACH_TARGET:
 *     → [APPROACH_TARGET entity, WAIT 1200t]
 *
 *   PROVOKE:
 *     → [APPROACH_TARGET entity, FACE_TARGET entity, WAIT 600t]
 *
 *   ESTABLISH_DOMINANCE:
 *     → [FACE_TARGET nearest_entity, HOLD_POSITION 600t]
 *
 *   others (MAINTAIN_COVER, TEST_JUDGMENT, DECEIVE, ADVANCE_OPPORTUNISTICALLY):
 *     → [WAIT durationTicks] (stationary framing — the action is conversational)
 * </pre>
 *
 * <p><b>Provenance: INFERRED.</b> The decomposition rules are distilled from
 * Wang Lin's behavioral patterns in Renegade Immortal. They are designed to
 * produce visibly-different Minecraft behaviors per Intent type.
 */
public final class IntentDecomposer {

    /** Default max duration (ticks) for movement tasks before timeout. */
    public static final long DEFAULT_MOVE_TIMEOUT = 600L;  // 30 seconds

    private IntentDecomposer() {}

    /**
     * Decompose an Intent into a list of CultivationTasks.
     *
     * @param intent        the active Intent (may be null — returns empty list)
     * @param actorX        the actor's current block X
     * @param actorZ        the actor's current block Z
     * @param nearestPlayerUuid the UUID of the nearest player (may be null)
     * @param nearestPlayerDist the distance to the nearest player (blocks)
     * @param targetPos     the position of the intent's target (may be null)
     * @param currentTick   the current server tick
     * @return an ordered list of tasks to execute (empty if intent is null)
     */
    public static List<CultivationTask> decompose(Intent intent,
                                                   int actorX, int actorZ,
                                                   String nearestPlayerUuid,
                                                   double nearestPlayerDist,
                                                   BlockPos targetPos,
                                                   long currentTick) {
        List<CultivationTask> tasks = new ArrayList<>();
        if (intent == null) return tasks;

        String label = intent.nature().label;

        switch (intent.nature()) {
            case AVOID_REVEALING_STRENGTH:
                decomposeAvoidRevealingStrength(tasks, actorX, actorZ,
                        nearestPlayerUuid, nearestPlayerDist, targetPos, currentTick, label);
                break;
            case OBSERVE_FROM_DISTANCE:
                decomposeObserveFromDistance(tasks, actorX, actorZ, targetPos, currentTick, label);
                break;
            case SEEK_OPPORTUNITY:
                decomposeSeekOpportunity(tasks, actorX, actorZ, targetPos, currentTick, label);
                break;
            case EXPLORE_CAUTIOUSLY:
                decomposeExploreCautiously(tasks, actorX, actorZ, currentTick, label);
                break;
            case CULTIVATE_SECRETLY:
                decomposeCultivateSecretly(tasks, actorX, actorZ, currentTick, label);
                break;
            case RETREAT_TACTICALLY:
                decomposeRetreatTactically(tasks, actorX, actorZ, targetPos,
                        intent.urgency(), currentTick, label);
                break;
            case DEFEND_POSITION:
            case PROTECT_ASSET:
                decomposeDefend(tasks, targetPos, currentTick, label);
                break;
            case GATHER_INTEL:
                decomposeGatherIntel(tasks, actorX, actorZ, targetPos, currentTick, label);
                break;
            case AMBUSH:
                decomposeAmbush(tasks, actorX, actorZ, targetPos, currentTick, label);
                break;
            case NEGOTIATE:
            case ADVANCE_OPPORTUNISTICALLY:
                decomposeApproach(tasks, targetPos, currentTick, label);
                break;
            case PROVOKE:
                decomposeProvoke(tasks, targetPos, currentTick, label);
                break;
            case ESTABLISH_DOMINANCE:
                decomposeEstablishDominance(tasks, actorX, actorZ, targetPos, currentTick, label);
                break;
            case MAINTAIN_COVER:
            case TEST_JUDGMENT:
            case DECEIVE:
                // Stationary framing — the behavior is conversational/cognitive
                tasks.add(CultivationTask.wait(intent.expectedDurationTicks(), currentTick, label));
                break;
            default:
                // Fallback: wait for the intent's expected duration
                tasks.add(CultivationTask.wait(intent.expectedDurationTicks(), currentTick, label));
                break;
        }

        return tasks;
    }

    // ── Per-intent decompositions ───────────────────────────────────────

    /**
     * AVOID_REVEALING_STRENGTH — Wang Lin's signature.
     * If a player is within 32 blocks, move away to ~48 blocks, then wait and observe.
     * If no player nearby, just wait and face the target.
     */
    private static void decomposeAvoidRevealingStrength(List<CultivationTask> tasks,
            int ax, int az, String playerUuid, double playerDist,
            BlockPos targetPos, long tick, String label) {
        if (playerUuid != null && playerDist < 32.0) {
            // Player too close — slip away
            tasks.add(CultivationTask.moveAwayFrom(playerUuid, DEFAULT_MOVE_TIMEOUT, tick, label));
            tasks.add(CultivationTask.wait(200L, tick, label));
        } else {
            // Safe — hold and observe
            tasks.add(CultivationTask.wait(600L, tick, label));
        }
        if (targetPos != null) {
            tasks.add(CultivationTask.faceTarget(targetPos, tick, label));
        }
    }

    /**
     * OBSERVE_FROM_DISTANCE — move to a vantage point 24-32 blocks from target, face it, wait.
     */
    private static void decomposeObserveFromDistance(List<CultivationTask> tasks,
            int ax, int az, BlockPos targetPos, long tick, String label) {
        if (targetPos != null) {
            // Compute a vantage point offset from the target
            BlockPos vantage = computeVantagePoint(ax, az, targetPos);
            tasks.add(CultivationTask.moveTo(vantage, DEFAULT_MOVE_TIMEOUT, tick, label));
            tasks.add(CultivationTask.faceTarget(targetPos, tick, label));
        }
        tasks.add(CultivationTask.wait(1200L, tick, label));
    }

    /**
     * SEEK_OPPORTUNITY — move toward the target (opportunity location).
     */
    private static void decomposeSeekOpportunity(List<CultivationTask> tasks,
            int ax, int az, BlockPos targetPos, long tick, String label) {
        if (targetPos != null) {
            tasks.add(CultivationTask.moveTo(targetPos, DEFAULT_MOVE_TIMEOUT, tick, label));
            tasks.add(CultivationTask.wait(200L, tick, label));
            tasks.add(CultivationTask.faceTarget(targetPos, tick, label));
        } else {
            // No specific target — wander toward a random nearby point
            BlockPos wander = new BlockPos(ax + (int)((Math.random() - 0.5) * 48), 0, az + (int)((Math.random() - 0.5) * 48));
            tasks.add(CultivationTask.moveTo(wander, DEFAULT_MOVE_TIMEOUT, tick, label));
            tasks.add(CultivationTask.wait(200L, tick, label));
        }
    }

    /**
     * EXPLORE_CAUTIOUSLY — move to a random point, pause, move again.
     */
    private static void decomposeExploreCautiously(List<CultivationTask> tasks,
            int ax, int az, long tick, String label) {
        BlockPos p1 = new BlockPos(ax + (int)((Math.random() - 0.5) * 48), 0, az + (int)((Math.random() - 0.5) * 48));
        tasks.add(CultivationTask.moveTo(p1, DEFAULT_MOVE_TIMEOUT, tick, label));
        tasks.add(CultivationTask.wait(100L, tick, label));
        BlockPos p2 = new BlockPos(ax + (int)((Math.random() - 0.5) * 48), 0, az + (int)((Math.random() - 0.5) * 48));
        tasks.add(CultivationTask.moveTo(p2, DEFAULT_MOVE_TIMEOUT, tick, label));
        tasks.add(CultivationTask.wait(100L, tick, label));
    }

    /**
     * CULTIVATE_SECRETLY — find cover (move to a random nearby hidden spot), hold for a long time.
     */
    private static void decomposeCultivateSecretly(List<CultivationTask> tasks,
            int ax, int az, long tick, String label) {
        // Move to a "concealed" position (offset from current, preferably away from players)
        BlockPos cover = new BlockPos(ax + (int)((Math.random() - 0.5) * 32), 0, az + (int)((Math.random() - 0.5) * 32));
        tasks.add(CultivationTask.moveTo(cover, DEFAULT_MOVE_TIMEOUT, tick, label));
        tasks.add(CultivationTask.holdPosition(6000L, tick, label));
    }

    /**
     * RETREAT_TACTICALLY — if high urgency, flee; else move away and regroup.
     */
    private static void decomposeRetreatTactically(List<CultivationTask> tasks,
            int ax, int az, BlockPos threatPos, double urgency, long tick, String label) {
        if (urgency > 0.7) {
            if (threatPos != null) {
                tasks.add(CultivationTask.flee(threatPos, DEFAULT_MOVE_TIMEOUT, tick, label));
            } else {
                BlockPos away = new BlockPos(ax + (int)((Math.random() - 0.5) * 64), 0, az + (int)((Math.random() - 0.5) * 64));
                tasks.add(CultivationTask.moveTo(away, DEFAULT_MOVE_TIMEOUT, tick, label));
            }
        } else {
            if (threatPos != null) {
                // Move in the opposite direction from the threat
                int dx = ax - threatPos.getX();
                int dz = az - threatPos.getZ();
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 0.1) {
                    int tx = ax + (int)((dx / len) * 32);
                    int tz = az + (int)((dz / len) * 32);
                    tasks.add(CultivationTask.moveTo(new BlockPos(tx, 0, tz), DEFAULT_MOVE_TIMEOUT, tick, label));
                }
            }
            tasks.add(CultivationTask.wait(200L, tick, label));
        }
    }

    /**
     * DEFEND_POSITION / PROTECT_ASSET — hold position, face the nearest threat.
     */
    private static void decomposeDefend(List<CultivationTask> tasks,
            BlockPos threatPos, long tick, String label) {
        if (threatPos != null) {
            tasks.add(CultivationTask.faceTarget(threatPos, tick, label));
        }
        tasks.add(CultivationTask.holdPosition(2400L, tick, label));
    }

    /**
     * GATHER_INTEL — move to vantage, wait, face target.
     */
    private static void decomposeGatherIntel(List<CultivationTask> tasks,
            int ax, int az, BlockPos targetPos, long tick, String label) {
        if (targetPos != null) {
            BlockPos vantage = computeVantagePoint(ax, az, targetPos);
            tasks.add(CultivationTask.moveTo(vantage, DEFAULT_MOVE_TIMEOUT, tick, label));
            tasks.add(CultivationTask.faceTarget(targetPos, tick, label));
        }
        tasks.add(CultivationTask.wait(600L, tick, label));
    }

    /**
     * AMBUSH — move to concealment near target, hold, face target.
     */
    private static void decomposeAmbush(List<CultivationTask> tasks,
            int ax, int az, BlockPos targetPos, long tick, String label) {
        if (targetPos != null) {
            // Position ~16 blocks from target (closer than observe — ambush range)
            int dx = targetPos.getX() - ax;
            int dz = targetPos.getZ() - az;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 16.0) {
                // Move to 16 blocks away
                double ratio = (len - 16.0) / len;
                int tx = ax + (int)(dx * ratio);
                int tz = az + (int)(dz * ratio);
                tasks.add(CultivationTask.moveTo(new BlockPos(tx, 0, tz), DEFAULT_MOVE_TIMEOUT, tick, label));
            }
            tasks.add(CultivationTask.faceTarget(targetPos, tick, label));
        }
        tasks.add(CultivationTask.holdPosition(3600L, tick, label));
    }

    /**
     * NEGOTIATE / ADVANCE_OPPORTUNISTICALLY — approach the target entity, wait.
     */
    private static void decomposeApproach(List<CultivationTask> tasks,
            BlockPos targetPos, long tick, String label) {
        if (targetPos != null) {
            tasks.add(CultivationTask.moveTo(targetPos, DEFAULT_MOVE_TIMEOUT, tick, label));
        }
        tasks.add(CultivationTask.wait(1200L, tick, label));
    }

    /**
     * PROVOKE — approach, face, wait (provocation is usually verbal/visual).
     */
    private static void decomposeProvoke(List<CultivationTask> tasks,
            BlockPos targetPos, long tick, String label) {
        if (targetPos != null) {
            tasks.add(CultivationTask.moveTo(targetPos, DEFAULT_MOVE_TIMEOUT, tick, label));
            tasks.add(CultivationTask.faceTarget(targetPos, tick, label));
        }
        tasks.add(CultivationTask.wait(600L, tick, label));
    }

    /**
     * ESTABLISH_DOMINANCE — face the target, hold position (stand ground, display power).
     */
    private static void decomposeEstablishDominance(List<CultivationTask> tasks,
            int ax, int az, BlockPos targetPos, long tick, String label) {
        if (targetPos != null) {
            tasks.add(CultivationTask.faceTarget(targetPos, tick, label));
        }
        tasks.add(CultivationTask.holdPosition(600L, tick, label));
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    /**
     * Compute a vantage point ~28 blocks from the target, offset from the actor's
     * current position so the NPC moves to a watching position rather than
     * standing on top of the target.
     */
    private static BlockPos computeVantagePoint(int actorX, int actorZ, BlockPos target) {
        int dx = target.getX() - actorX;
        int dz = target.getZ() - actorZ;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 1.0) {
            // Actor is basically on the target — pick a random offset
            return new BlockPos(target.getX() + 28, 0, target.getZ());
        }
        // Move to 28 blocks away from the target (in the direction from target → actor, extended)
        double ratio = 28.0 / len;
        int vx = target.getX() + (int)(dx * ratio * (Math.random() * 0.4 + 0.8));
        int vz = target.getZ() + (int)(dz * ratio * (Math.random() * 0.4 + 0.8));
        return new BlockPos(vx, 0, vz);
    }
}
