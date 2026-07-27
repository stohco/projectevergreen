package dev.ergenverse.entity.control;

import dev.ergenverse.entity.EntityCultivator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * CultivatorFlightNavigator — obstacle-aware velocity computation for
 * cultivator sword-flight (御剑飞行).
 *
 * <p><b>CRON-133 — closes CRON-130 self-critique #2 and CRON-132 self-critique #6.</b>
 * Prior to CRON-133, {@link dev.ergenverse.entity.ai.CultivatorFlightGoal}
 * computed a direct 3D velocity vector to the target and called
 * {@code setDeltaMovement} — no obstacle detection. A Foundation+ cultivator
 * flying toward a far target would fly THROUGH trees, walls, and mountains,
 * the ride_sword clipping through geometry. This class provides the ray-cast
 * lookahead and dodge/vault logic that the goal calls each tick.
 *
 * <h2>Algorithm</h2>
 * <ol>
 *   <li>Compute the straight-line horizontal direction to the target
 *       (the "wish dir").</li>
 *   <li>Ray-cast forward {@value #LOOKAHEAD} blocks at three height samples
 *       (feet=0.5, chest=1.0, head=1.5 above entity Y) — if any sample hits
 *       a solid block, the path is blocked.</li>
 *   <li><b>CRON-135:</b> Also ray-cast forward {@value #LOOKAHEAD} blocks at
 *       three UPWARD height samples (+3, +6, +9 above entity Y) — if any of
 *       these hits a solid block, the cultivator is approaching a TALL
 *       obstacle (mountainside, cliff, tower) that they will crash into
 *       unless they vault NOW. The forward-only check (step 2) cannot detect
 *       this because the cultivator's body height (0.5-1.5) is below the
 *       obstacle's leading edge.</li>
 *   <li>If blocked (forward OR upward), probe perpendicular dodge directions
 *       (left and right) at {@value #DODGE_PROBE_DIST} blocks. <b>CRON-136:</b>
 *       Each dodge direction is now probed at THREE heights (chest +1.0, +3.0,
 *       +6.0 above entity Y) — not just chest height — to prevent dodging
 *       INTO a tall obstacle that curves into the dodge path. If exactly one
 *       direction is clear at all three heights, dodge that way. If both are
 *       clear, pick one based on the cultivator's entity ID parity
 *       (deterministic per-cultivator bias to prevent oscillation — left if
 *       ID is even, right if odd).</li>
 *   <li>If neither dodge is clear, vault upward (strong vertical impulse) —
 *       the cultivator rises above the obstacle. When the upward ray-cast
 *       detected the obstacle (tall obstacle case), the vault impulse is
 *       STRONGER ({@value #TALL_VAULT_SPEED_SCALE}) because the cultivator
 *       needs to gain more altitude to clear it.</li>
 *   <li>If the path is clear, return the wish-dir velocity at cruise speed.</li>
 * </ol>
 *
 * <h2>Canon fidelity</h2>
 * <p>Sword flight in 仙逆 bypasses terrain obstacles — cultivators fly in
 * straight lines over mountains and walls. They do NOT phase through solid
 * matter. A cultivator crashing into a mountain side would be a canon
 * violation (and a visual bug — the ride_sword would clip through the rock).
 * This navigator preserves the canon: cultivators fly in straight lines when
 * the path is clear, and curve around or vault over obstacles when it is not.
 *
 * <p>Web-search verified 2026-07-27: Wang Lin's sword flights in 仙逆 are
 * consistently described as skimming above treetops and around mountain peaks,
 * never phasing through them. Foundation Establishment (筑基) is the canonical
 * minimum realm (Baidu Baike-verified in CRON-130). NO fabricated chapter
 * citations.
 *
 * <h2>Statelessness</h2>
 * <p>This is a stateless utility class. State tracking (consecutive blocked
 * ticks, last dodge direction) lives in {@link dev.ergenverse.entity.ai.CultivatorFlightGoal}
 * so the goal can abort if the cultivator gets stuck.
 */
public final class CultivatorFlightNavigator {

    private CultivatorFlightNavigator() {
        // No instances — pure static utility.
    }

    /** Ray-cast lookahead distance (blocks). 3.0 = detect obstacle ~3 ticks early at cruise speed. */
    public static final double LOOKAHEAD = 3.0D;

    /** Vertical sample offsets (relative to entity Y) for forward ray-cast. */
    public static final double[] HEIGHT_SAMPLES = {0.5D, 1.0D, 1.5D};

    /**
     * CRON-135: Upward sample offsets (relative to entity Y) for forward
     * ray-cast at the same LOOKAHEAD distance. These detect TALL obstacles
     * (mountainsides, cliffs, towers) whose leading edge is ABOVE the
     * cultivator's body height (1.5). Without these, a cultivator flying
     * at cruise altitude (4 blocks above surface) toward a mountain peak
     * would fly into the mountainside because the forward-only check at
     * 0.5/1.0/1.5 returns 'clear' (the mountainside is above 1.5).
     *
     * <p>Offsets chosen to catch obstacles 3-9 blocks above the cultivator:
     * <ul>
     *   <li>+3.0: catches 3-block-tall walls (sect perimeter walls, pagodas)</li>
     *   <li>+6.0: catches 6-block-tall structures (gatehouses, watchtowers)</li>
     *   <li>+9.0: catches mountain leading edges and tall cliffs</li>
     * </ul>
     * Beyond +9, the cultivator is high enough that mountain collision is
     * unlikely (cruise altitude is surface+4, so the cultivator is already
     * 4 blocks up; +9 above entity = 13 blocks above surface, which is
     * above most mountain leading edges in the village biome).
     */
    public static final double[] UPWARD_SAMPLES = {3.0D, 6.0D, 9.0D};

    /** Perpendicular probe distance for dodge detection (blocks). */
    public static final double DODGE_PROBE_DIST = 2.0D;

    /** Dodge velocity scale (relative to flight speed). */
    public static final double DODGE_SPEED_SCALE = 0.7D;

    /** Vault upward impulse scale (relative to flight speed) — standard vault for short obstacles. */
    public static final double VAULT_SPEED_SCALE = 0.8D;

    /**
     * CRON-135: Vault upward impulse scale for TALL obstacles detected by
     * the upward ray-cast. Stronger than standard vault because the cultivator
     * needs to gain more altitude to clear a mountainside or cliff.
     *
     * <p><b>CRON-136 — math-derived value (closes CRON-135 self-critique #2).</b>
     * The warning window is {@code LOOKAHEAD / cruiseSpeed} = {@code 3.0 / 0.4}
     * = 7.5 ticks. The upward ray-cast detects obstacles up to +9 above entity
     * Y. To clear a +9 obstacle, the cultivator must gain 9 blocks of altitude
     * in 7.5 ticks = 1.2 blocks/tick upward velocity. At cruise speed 0.4
     * blocks/tick, that requires a scale of {@code 1.2 / 0.4} = <b>3.0</b>.
     *
     * <p>The CRON-135 value of 1.2D produced only {@code 0.4 * 1.2} = 0.48
     * blocks/tick (3.6 blocks in 7.5 ticks) — sufficient for +3 obstacles but
     * crashing into +6 and +9 obstacles. The CRON-136 value of 3.0D produces
     * {@code 0.4 * 3.0} = 1.2 blocks/tick (9.0 blocks in 7.5 ticks) — exactly
     * enough to clear the maximum +9 sample.
     *
     * <p><b>Trade-off:</b> 3.0D is a strong vertical impulse (3× cruise speed).
     * At Foundation cruise speed 0.4, the cultivator gains 1.2 blocks/tick = 24
     * blocks/sec upward — visually a steep climb but canon-faithful (cultivators
     * vault mountainsides rapidly in 仙逆). The alternative (lower scale, accept
     * crashing into +6/+9) is a canon violation. 3.0D is the minimum value that
     * clears the maximum detected obstacle.
     */
    public static final double TALL_VAULT_SPEED_SCALE = 3.0D;

    /** Forward speed reduction when vaulting (slow forward progress while rising). */
    public static final double VAULT_FORWARD_SCALE = 0.3D;

    /**
     * CRON-136: Height samples (relative to entity Y) for dodge-path probing.
     * Probes the dodge direction at chest height AND at two upward heights to
     * prevent dodging INTO a tall obstacle (mountainside curving left/right).
     *
     * <p>Closes CRON-135 self-critique #4: 'No UPWARD ray-cast DURING dodge.
     * The dodge path probes perpendicular at chest height only. If the
     * cultivator dodges left to avoid a forward obstacle, but the left path
     * has a TALL obstacle, the dodge probe returns clear (chest height is
     * below the mountainside's leading edge) and the cultivator dodges INTO
     * the mountainside.'
     *
     * <p>Samples:
     * <ul>
     *   <li>+1.0: chest height (existing — catches short obstacles)</li>
     *   <li>+3.0: catches 3-block-tall walls in the dodge path</li>
     *   <li>+6.0: catches 6-block-tall structures in the dodge path</li>
     * </ul>
     * +9 is omitted because at {@link #DODGE_PROBE_DIST} = 2.0 (perpendicular),
     * a +9 obstacle in the dodge direction is rare and the cultivator can vault
     * instead. Probing 3 heights (vs 1) is the minimum to prevent the
     * dodge-INTO-mountain regression.
     */
    public static final double[] DODGE_HEIGHT_SAMPLES = {1.0D, 3.0D, 6.0D};

    /** Upward bias applied during diagonal dodge (helps clear the obstacle). */
    public static final double DODGE_UPWARD_BIAS_SCALE = 0.15D;

    /** Forward bias during diagonal dodge (cultivator still progresses toward target). */
    public static final double DODGE_FORWARD_BIAS_SCALE = 0.2D;

    /**
     * Result of a single navigator tick. Immutable.
     */
    public static final class SteerResult {
        /** The desired delta-movement vector to apply via setDeltaMovement. */
        public final Vec3 velocity;
        /** True if the forward ray-cast hit a solid block. */
        public final boolean blocked;
        /**
         * CRON-135: True if the UPWARD ray-cast hit a solid block (tall
         * obstacle detected). When true, the cultivator vaults with a
         * STRONGER impulse (TALL_VAULT_SPEED_SCALE) to clear the obstacle.
         * Always false when {@link #blocked} is false.
         */
        public final boolean tallObstacle;
        /** True if the cultivator dodged left this tick. */
        public final boolean dodgedLeft;
        /** True if the cultivator dodged right this tick. */
        public final boolean dodgedRight;
        /** True if the cultivator vaulted upward this tick (no dodge available). */
        public final boolean vaulted;

        public SteerResult(Vec3 velocity, boolean blocked, boolean tallObstacle,
                           boolean dodgedLeft, boolean dodgedRight, boolean vaulted) {
            this.velocity = velocity;
            this.blocked = blocked;
            this.tallObstacle = tallObstacle;
            this.dodgedLeft = dodgedLeft;
            this.dodgedRight = dodgedRight;
            this.vaulted = vaulted;
        }
    }

    /**
     * Compute the desired flight velocity for a cultivator flying toward a target.
     *
     * @param cultivator   the flying cultivator (provides position, level, entity ID)
     * @param target       the target position (combat target, follow target, etc.)
     * @param flightSpeed  cruise speed (blocks per tick)
     * @param desiredY     desired cruise Y (computed by caller via computeCruiseAltitude)
     * @return SteerResult containing the desired velocity + telemetry flags
     */
    public static SteerResult computeSteer(EntityCultivator cultivator, Vec3 target,
                                           double flightSpeed, double desiredY) {
        Vec3 origin = cultivator.position();
        Vec3 toTarget = target.subtract(origin);

        double dx = toTarget.x;
        double dz = toTarget.z;
        double horizMag = Math.sqrt(dx * dx + dz * dz);

        // Edge case: directly above/below — ascend/descend vertically.
        // No horizontal obstacle possible.
        if (horizMag < 0.001D) {
            double vy = Math.signum(toTarget.y) * flightSpeed * 0.5D;
            return new SteerResult(new Vec3(0, vy, 0), false, false, false, false, false);
        }

        double nx = dx / horizMag;
        double nz = dz / horizMag;

        // Ray-cast forward at 3 body heights (feet/chest/head).
        boolean forwardBlocked = isForwardBlocked(cultivator, nx, nz);

        // CRON-135: Ray-cast forward at 3 UPWARD heights (+3/+6/+9 above entity Y).
        // This detects tall obstacles (mountainsides, cliffs, towers) whose
        // leading edge is above the cultivator's body. Without this check, a
        // cultivator at cruise altitude (surface+4) flying toward a mountain
        // would crash into the mountainside because the forward-only check
        // at 0.5/1.0/1.5 returns 'clear' (the mountainside is above 1.5).
        boolean upwardBlocked = isUpwardBlocked(cultivator, nx, nz);

        // The path is 'blocked' if EITHER ray-cast hits a solid block.
        boolean blocked = forwardBlocked || upwardBlocked;
        // 'tallObstacle' is true only when the upward ray-cast detected the
        // obstacle (the forward ray-cast may or may not also be blocked).
        boolean tallObstacle = upwardBlocked;

        if (!blocked) {
            // Clear path — direct toward target at cruise speed, ease toward desired Y.
            double vx = nx * flightSpeed;
            double vz = nz * flightSpeed;
            double vy = clampVy((desiredY - origin.y) * 0.1D, flightSpeed);
            return new SteerResult(new Vec3(vx, vy, vz), false, false, false, false, false);
        }

        // Path is blocked — probe perpendicular dodge options.
        // Perpendicular vector (rotate 90° CCW): (nx, nz) → (nz, -nx) = "left"
        // The "right" perpendicular is the negation: (-nz, nx).
        double leftPerpX = nz;
        double leftPerpZ = -nx;
        boolean canDodgeLeft = isDodgeClear(cultivator, leftPerpX, leftPerpZ);
        boolean canDodgeRight = isDodgeClear(cultivator, -leftPerpX, -leftPerpZ);

        if (canDodgeLeft || canDodgeRight) {
            // Pick a dodge direction.
            // If only one is clear, use it. If both are clear, use a deterministic
            // per-cultivator bias (entity ID parity) to prevent oscillation.
            boolean goLeft;
            if (canDodgeLeft && !canDodgeRight) {
                goLeft = true;
            } else if (canDodgeRight && !canDodgeLeft) {
                goLeft = false;
            } else {
                // Both clear — deterministic per-cultivator bias.
                goLeft = (cultivator.getId() & 1) == 0;
            }

            double perpX = goLeft ? leftPerpX : -leftPerpX;
            double perpZ = goLeft ? leftPerpZ : -leftPerpZ;

            // Dodge velocity = perpendicular * scale + forward bias + upward bias.
            // When the obstacle is tall (upward-blocked), increase the upward
            // bias so the dodge also gains altitude — this helps the cultivator
            // clear the tall obstacle while dodging around it.
            double vx = perpX * flightSpeed * DODGE_SPEED_SCALE
                    + nx * flightSpeed * DODGE_FORWARD_BIAS_SCALE;
            double vz = perpZ * flightSpeed * DODGE_SPEED_SCALE
                    + nz * flightSpeed * DODGE_FORWARD_BIAS_SCALE;
            double upwardBias = tallObstacle
                    ? flightSpeed * DODGE_UPWARD_BIAS_SCALE * 2.0D  // double upward bias for tall obstacles
                    : flightSpeed * DODGE_UPWARD_BIAS_SCALE;
            double vy = upwardBias;

            return new SteerResult(new Vec3(vx, vy, vz), true, tallObstacle, goLeft, !goLeft, false);
        }

        // No dodge available — vault upward to clear the obstacle.
        // CRON-135: When the obstacle is tall (detected by upward ray-cast),
        // use a STRONGER vault impulse (TALL_VAULT_SPEED_SCALE) because the
        // cultivator needs to gain more altitude to clear a mountainside or
        // cliff. Standard vault (VAULT_SPEED_SCALE) is for short obstacles
        // detected only by the forward ray-cast.
        double vx = nx * flightSpeed * VAULT_FORWARD_SCALE;
        double vz = nz * flightSpeed * VAULT_FORWARD_SCALE;
        double vaultScale = tallObstacle ? TALL_VAULT_SPEED_SCALE : VAULT_SPEED_SCALE;
        double vy = flightSpeed * vaultScale;
        return new SteerResult(new Vec3(vx, vy, vz), true, tallObstacle, false, false, true);
    }

    /**
     * Ray-cast forward {@link #LOOKAHEAD} blocks at 3 heights (feet/chest/head).
     * Returns true if any sample hits a solid-render block.
     *
     * <p>Uses {@link BlockState#isSolidRender(Level, BlockPos)} which matches
     * the same test used by {@link FlightMoveControl#isBlockedAt} for beast
     * flight — consistent collision semantics across flyers.
     */
    private static boolean isForwardBlocked(EntityCultivator cultivator, double nx, double nz) {
        Level level = cultivator.level();
        double baseX = cultivator.getX();
        double baseY = cultivator.getY();
        double baseZ = cultivator.getZ();
        for (double h : HEIGHT_SAMPLES) {
            double checkX = baseX + nx * LOOKAHEAD;
            double checkY = baseY + h;
            double checkZ = baseZ + nz * LOOKAHEAD;
            BlockPos pos = BlockPos.containing(checkX, checkY, checkZ);
            BlockState state = level.getBlockState(pos);
            if (state.isSolidRender(level, pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * CRON-135: Ray-cast forward {@link #LOOKAHEAD} blocks at 3 UPWARD heights
     * (+3/+6/+9 above entity Y). Returns true if any sample hits a solid-render
     * block — indicating a TALL obstacle (mountainside, cliff, tower) whose
     * leading edge is above the cultivator's body height (1.5).
     *
     * <p>This catches the case the forward-only check misses: a cultivator
     * flying at cruise altitude (surface+4) toward a mountain. The forward
     * check at heights 0.5/1.0/1.5 returns 'clear' because the mountainside
     * is above 1.5 — but the cultivator will crash into the mountainside
     * because their cruise altitude is below the mountain's leading edge.
     *
     * <p>The upward check at +3/+6/+9 detects the mountainside's leading
     * edge (which is typically 3-9 blocks above the cultivator at cruise
     * altitude), triggering a vault BEFORE collision.
     */
    private static boolean isUpwardBlocked(EntityCultivator cultivator, double nx, double nz) {
        Level level = cultivator.level();
        double baseX = cultivator.getX();
        double baseY = cultivator.getY();
        double baseZ = cultivator.getZ();
        for (double h : UPWARD_SAMPLES) {
            double checkX = baseX + nx * LOOKAHEAD;
            double checkY = baseY + h;
            double checkZ = baseZ + nz * LOOKAHEAD;
            BlockPos pos = BlockPos.containing(checkX, checkY, checkZ);
            BlockState state = level.getBlockState(pos);
            if (state.isSolidRender(level, pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Probe a perpendicular dodge direction. Returns true if the dodge path
     * is clear (no solid block at {@link #DODGE_PROBE_DIST} perpendicular at
     * ALL dodge height samples).
     *
     * <p><b>CRON-136:</b> Previously probed only at chest height (Y+1.0). Now
     * probes at {@link #DODGE_HEIGHT_SAMPLES} (chest + two upward heights) to
     * prevent dodging INTO a tall obstacle. Closes CRON-135 self-critique #4:
     * a cultivator dodging left to avoid a forward obstacle, where the left
     * path has a mountainside curving left, would previously see 'clear' at
     * chest height but crash into the mountainside. Now the probe at +3 and +6
     * catches the mountainside's leading edge and refuses the dodge, forcing a
     * vault instead.
     *
     * <p>Returns true only if ALL height samples are clear. If any sample hits
     * a solid block, the dodge path is considered blocked (returns false).
     */
    private static boolean isDodgeClear(EntityCultivator cultivator, double perpX, double perpZ) {
        Level level = cultivator.level();
        double baseX = cultivator.getX() + perpX * DODGE_PROBE_DIST;
        double baseZ = cultivator.getZ() + perpZ * DODGE_PROBE_DIST;
        for (double h : DODGE_HEIGHT_SAMPLES) {
            double checkY = cultivator.getY() + h;
            BlockPos pos = BlockPos.containing(baseX, checkY, baseZ);
            BlockState state = level.getBlockState(pos);
            if (state.isSolidRender(level, pos)) {
                return false;  // this height is blocked — dodge path is NOT clear
            }
        }
        return true;  // all heights clear — dodge path is clear
    }

    /**
     * Clamp vertical velocity to ±flightSpeed for smooth ascent/descent.
     */
    private static double clampVy(double vy, double flightSpeed) {
        return Math.max(-flightSpeed, Math.min(flightSpeed, vy));
    }
}
