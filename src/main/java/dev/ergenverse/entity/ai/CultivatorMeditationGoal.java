package dev.ergenverse.entity.ai;

import dev.ergenverse.assembly.AnchorRegistry;
import dev.ergenverse.assembly.AnchorRegistryService;
import dev.ergenverse.canon.structure.SemanticRole;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * CultivatorMeditationGoal — NPC cultivator meditation AI goal.
 *
 * <p>CRON-COMPLETIONIST-57: Fully implemented. Previously a dead stub (canUse() always false).
 * Now: cultivator enters meditation when idle, not in combat, not locked, and not pursuing.
 * Duration: 200-600 ticks (10-30 seconds) randomized per session. Sets POSE_MEDITATING
 * on start, restores POSE_IDLE on stop. This fires the CultivatorRobeModel meditation
 * animation (zhan zhuang / standing-stake pose) organically.
 *
 * <p><b>CRON-129 — ANCHOR-DRIVEN NAVIGATION.</b> When the cultivator's
 * settlement has a compiled {@link AnchorRegistry} with at least one
 * {@link SemanticRole#MEDITATION} anchor, this goal now walks the
 * cultivator to that anchor before meditating. This realizes the user's
 * directive:
 * <blockquote>
 *   AI never searches blocks. Find Wang Lin → Find House → Find Bedroom
 *   → Find Bed → Compiler Anchor → Navigation Target.
 * </blockquote>
 *
 * <p>State machine:
 * <ol>
 *   <li><b>SEEKING</b> — walking toward the anchor. Re-paths every 60
 *       ticks. Aborts if the cultivator is interrupted (combat, activity
 *       lock) or after 600 ticks (timeout — anchor unreachable).</li>
 *   <li><b>MEDITATING</b> — arrived at the anchor (within 2 blocks),
 *       set POSE_MEDITATING, hold for the meditation duration.</li>
 * </ol>
 *
 * <p>If no anchor is found (no registry for the settlement, no
 * MEDITATION anchor, or cultivator has no settlement id), the goal
 * falls back to CRON-57 behavior: meditate in place. This preserves
 * backward compatibility for NPCs outside compiled settlements.
 *
 * <p>Canon: Cultivators meditate (打坐/冥想) to circulate qi, comprehend dao fragments,
 * and consolidate their cultivation base. Wang Lin meditates for hours in the novel.
 * This goal simulates that behavior at Minecraft timescale.
 *
 * <p>The anchor-driven path is canon-faithful: Wang Lin's bedroom has a
 * meditation mat (CanonFurniture.MEDITATION_MAT) at a concrete position
 * in WangFamilyVillageComposition. A cultivator meditating AT that mat
 * is canon-accurate; a cultivator meditating in the middle of the
 * village plaza is not.
 */
public class CultivatorMeditationGoal extends Goal {
    private final EntityCultivator cultivator;

    /** Duration of current meditation session in ticks. */
    private int meditationDuration;

    /** Ticks elapsed in current session. */
    private int meditationTimer;

    /** Cooldown between meditation sessions (prevents non-stop meditation). */
    private int cooldown;

    // ── CRON-129: anchor-driven navigation state ──────────────────────

    /** Internal state machine. */
    private enum Phase { SEEKING, MEDITATING }

    /** Current phase. */
    private Phase phase = Phase.MEDITATING;

    /** Target anchor for the SEEKING phase (null if meditating in place). */
    private AnchorRegistry.ResolvedAnchor targetAnchor;

    /** Ticks spent in SEEKING phase (for timeout). */
    private int seekTicks;

    /** Tick counter for re-pathing. */
    private int repathTicks;

    /** Squared arrival threshold (within 2 blocks = 4.0). */
    private static final double ARRIVE_DIST_SQ = 4.0;

    /** Maximum seek time before giving up and meditating in place (30s). */
    private static final int MAX_SEEK_TICKS = 600;

    /** Re-path interval during SEEKING. */
    private static final int REPATH_INTERVAL = 60;

    /** Maximum horizontal distance to search for an anchor (64 blocks). */
    private static final int MAX_ANCHOR_DISTANCE_SQ = 64 * 64;

    public CultivatorMeditationGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
        this.meditationDuration = 0;
        this.meditationTimer = 0;
        this.cooldown = 0;
    }

    @Override
    public boolean canUse() {
        // Cannot meditate if: locked by activity system, in combat, or cooling down
        if (cultivator.isActivityLocked()) return false;
        if (cultivator.getTarget() != null) return false;
        if (cooldown > 0) return false;
        if (cultivator.getRandom().nextInt(200) != 0) return false; // ~0.5%/tick chance
        // Only meditate when idle (not walking, not pursuing)
        if (cultivator.getCultivatorPose() != EntityCultivator.POSE_IDLE) return false;

        // CRON-129: try to resolve a MEDITATION anchor for this cultivator's
        // settlement. If found, start in SEEKING phase. If not found, fall
        // back to meditating in place (preserves CRON-57 behavior).
        targetAnchor = resolveMeditationAnchor();
        phase = (targetAnchor != null) ? Phase.SEEKING : Phase.MEDITATING;
        seekTicks = 0;
        repathTicks = 0;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        // Stop if: target appeared, activity locked, duration exceeded
        if (cultivator.isActivityLocked()) return false;
        if (cultivator.getTarget() != null) return false;

        if (phase == Phase.SEEKING) {
            // Abort seeking if we've timed out — fall through to in-place meditation
            if (seekTicks >= MAX_SEEK_TICKS) {
                Ergenverse.LOGGER.debug("[Ergenverse] CultivatorMeditationGoal: seek timeout for '{}' — meditating in place.",
                        cultivator.getCharacterId());
                transitionToMeditating();
            }
            return true;  // continue seeking or meditating after transition
        }
        // MEDITATING phase
        return meditationTimer < meditationDuration;
    }

    @Override
    public void start() {
        if (phase == Phase.SEEKING && targetAnchor != null) {
            // Begin walking toward the anchor.
            Ergenverse.LOGGER.info("[Ergenverse] CultivatorMeditationGoal: '{}' walking to meditation anchor at ({}, {}, {}).",
                    cultivator.getCharacterId(), targetAnchor.x(), targetAnchor.y(), targetAnchor.z());
            // Do NOT set POSE_MEDITATING yet — the cultivator is walking.
            // POSE_IDLE is the walking pose; we set POSE_MEDITATING on arrival.
            walkTowardAnchor();
        } else {
            // In-place meditation (no anchor found).
            beginMeditation();
        }
    }

    @Override
    public void stop() {
        // Restore idle pose
        cultivator.setCultivatorPose(EntityCultivator.POSE_IDLE);
        cultivator.getNavigation().stop();
        // Set cooldown before next meditation: 400-1200 ticks (20-60 seconds)
        cooldown = 400 + cultivator.getRandom().nextInt(800);
        meditationTimer = 0;
        targetAnchor = null;
        phase = Phase.MEDITATING;
    }

    @Override
    public void tick() {
        if (phase == Phase.SEEKING) {
            tickSeeking();
        } else {
            tickMeditating();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    // ── CRON-129: SEEKING phase ───────────────────────────────────────

    private void tickSeeking() {
        seekTicks++;
        if (targetAnchor == null) {
            transitionToMeditating();
            return;
        }

        // Check arrival
        double distSq = cultivator.distanceToSqr(
                targetAnchor.x() + 0.5,
                cultivator.getY(),
                targetAnchor.z() + 0.5);

        if (distSq < ARRIVE_DIST_SQ) {
            // Arrived — transition to meditation.
            Ergenverse.LOGGER.info("[Ergenverse] CultivatorMeditationGoal: '{}' arrived at meditation anchor after {} ticks.",
                    cultivator.getCharacterId(), seekTicks);
            transitionToMeditating();
            return;
        }

        // Re-path periodically (handles stuck navigation, blocked paths)
        repathTicks++;
        if (repathTicks >= REPATH_INTERVAL || cultivator.getNavigation().isDone()) {
            repathTicks = 0;
            walkTowardAnchor();
        }
    }

    private void walkTowardAnchor() {
        if (targetAnchor == null) return;
        cultivator.getNavigation().moveTo(
                targetAnchor.x() + 0.5,
                cultivator.getY(),
                targetAnchor.z() + 0.5,
                0.6D);
    }

    private void transitionToMeditating() {
        phase = Phase.MEDITATING;
        cultivator.getNavigation().stop();
        beginMeditation();
    }

    // ── CRON-129: MEDITATING phase (was the entire goal pre-129) ──────

    private void beginMeditation() {
        // Random duration: 200-600 ticks (10-30 seconds)
        meditationDuration = 200 + cultivator.getRandom().nextInt(400);
        meditationTimer = 0;
        // Set meditation pose — triggers CultivatorRobeModel zhan zhuang animation
        cultivator.setCultivatorPose(EntityCultivator.POSE_MEDITATING);
        // Stop any active navigation
        cultivator.getNavigation().stop();
    }

    private void tickMeditating() {
        meditationTimer++;
        // Keep cultivator stationary
        cultivator.getNavigation().stop();
    }

    // ── CRON-129: anchor resolution ───────────────────────────────────

    /**
     * Resolve the nearest MEDITATION anchor for this cultivator.
     *
     * <p>Strategy:
     * <ol>
     *   <li>Query {@link AnchorRegistryService#get} with the cultivator's
     *       settlement id (derived from {@link EntityCultivator#getSectId()}).</li>
     *   <li>If a registry exists, find the nearest MEDITATION anchor
     *       within {@link #MAX_ANCHOR_DISTANCE_SQ}.</li>
     *   <li>If no settlement registry exists, fall back to a global
     *       search across all registered settlements (useful for
     *       independent wanderers).</li>
     *   <li>If still no anchor found, return null — the goal will
     *       meditate in place (CRON-57 behavior).</li>
     * </ol>
     *
     * @return the nearest MEDITATION anchor, or null if none found
     */
    private AnchorRegistry.ResolvedAnchor resolveMeditationAnchor() {
        try {
            int cx = cultivator.blockPosition().getX();
            int cy = cultivator.blockPosition().getY();
            int cz = cultivator.blockPosition().getZ();

            String settlementId = cultivator.getSectId();
            AnchorRegistryService service = AnchorRegistryService.get();

            AnchorRegistry.ResolvedAnchor anchor = null;
            if (settlementId != null && !settlementId.isEmpty()
                    && !"independent".equals(settlementId)) {
                anchor = service.findNearest(settlementId, SemanticRole.MEDITATION, cx, cy, cz);
            }

            if (anchor == null) {
                // Fall back to a global search across all settlements.
                anchor = service.findNearestGlobal(SemanticRole.MEDITATION, cx, cy, cz);
            }

            if (anchor == null) return null;

            // Distance check — don't activate SEEKING for far-away anchors
            long dx = anchor.x() - cx;
            long dz = anchor.z() - cz;
            long distSq = dx * dx + dz * dz;
            if (distSq > MAX_ANCHOR_DISTANCE_SQ) {
                Ergenverse.LOGGER.debug("[Ergenverse] CultivatorMeditationGoal: nearest MEDITATION anchor for '{}' "
                                + "is at ({}, {}, {}) — {} blocks away (over limit {}). Meditating in place.",
                        cultivator.getCharacterId(), anchor.x(), anchor.y(), anchor.z(),
                        Math.sqrt(distSq), Math.sqrt(MAX_ANCHOR_DISTANCE_SQ));
                return null;
            }

            return anchor;
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] CultivatorMeditationGoal: anchor resolution failed for '{}': {}",
                    cultivator.getCharacterId(), t.getMessage());
            return null;
        }
    }
}
