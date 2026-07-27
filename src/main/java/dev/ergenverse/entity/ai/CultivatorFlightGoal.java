package dev.ergenverse.entity.ai;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.entity.control.CultivatorFlightNavigator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.UUID;

/**
 * CultivatorFlightGoal — sword-flight (御剑飞行) for Foundation+ cultivators.
 *
 * <p><b>CRON-130 — THE ICONIC CULTIVATOR VISUAL.</b> The single most
 * recognizable image of a Chinese cultivation-novel cultivator is a figure
 * standing on a flying sword, robes billowing in the wind. Prior to
 * CRON-130, every cultivator in this mod — including Foundation, Core
 * Formation, Nascent Soul, Soul Formation elders — walked everywhere like
 * a mortal. This goal closes that gap. When a Foundation+ cultivator has
 * a target beyond walking range, they take to the sky on their sword.
 *
 * <h2>Canon fidelity</h2>
 * <p>Web-search verified 2026-07-27 against Baidu Baike 仙逆 and cultivation-
 * novel convention:
 * <ul>
 *   <li><b>筑基 (Foundation Establishment)</b> is the canonical minimum realm
 *       at which a cultivator can sustain sword flight. Qi Condensation
 *       (练气) cultivators lack the qi reserves to lift themselves on a sword.</li>
 *   <li>Wang Lin observes Li Muwan flying to Heng Yue Sect on a sword when
 *       she visits — she is at Foundation Establishment.</li>
 *   <li>Wang Lin himself first flies after reaching Foundation Establishment
 *       mid-novel (no specific chapter cited here to avoid fabrication).</li>
 *   <li>Sword flight is faster than walking and bypasses terrain obstacles
 *       (mountains, rivers, walls) — cultivators fly in straight lines.</li>
 * </ul>
 * NO fabricated chapter citations. The realm gate is the canon-faithful rule;
 * the rest is the universally-attested visual.
 *
 * <h2>Activation</h2>
 * <p>The goal activates when ALL of the following are true:
 * <ul>
 *   <li>Cultivator's realm is Foundation Establishment or higher
 *       ({@link EntityCultivator#isFoundationOrHigher()}).</li>
 *   <li><b>CRON-134:</b> Cultivator has &ge;10% of maxQi in reserves
 *       ({@link EntityCultivator#hasEnoughQiForFlightActivation()}). A
 *       cultivator at near-zero qi cannot lift off; they must rest first.</li>
 *   <li>Cultivator is NOT currently in a higher-priority pose: not meditating,
 *       not casting, not observing, not guarding, not activity-locked.</li>
 *   <li>A flight-eligible target exists:
 *       <ul>
 *           <li>Combat target (mob.getTarget()) at distance &gt; {@value #ACTIVATE_DIST}
 *               blocks — combat yields beyond 18 blocks (CRON-130 surgical
 *               change to CultivatorCombatGoal.canUse), so flight takes over.</li>
 *           <li>OR following-player UUID is set and the player is at distance
 *               &gt; {@value #ACTIVATE_DIST} blocks (companion catch-up).</li>
 *           <li>OR a far navigation target was set by a higher-level goal
 *               (CognitionDrivenGoal) at distance &gt; {@value #ACTIVATE_DIST}.</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <p>The goal yields when:
 * <ul>
 *   <li>Target comes within {@value #YIELD_DIST} blocks (close enough for
 *       combat's mid-range path or melee).</li>
 *   <li>Target dies or is lost.</li>
 *   <li>Flight exceeds {@value #MAX_FLIGHT_TICKS} ticks (30s timeout —
 *       target unreachable, give up and revert to walking).</li>
 *   <li>A higher-priority pose activates (meditation, cast, etc.).</li>
 *   <li><b>CRON-134:</b> Qi drops below 5% of maxQi mid-flight (qi exhaustion
 *       — the cultivator cannot sustain flight and must land). The canUse()
 *       gate then prevents re-activation until qi regens above 10%.</li>
 * </ul>
 *
 * <h2>Movement</h2>
 * <p>Flight bypasses Minecraft's ground pathfinder entirely. Each tick, the
 * goal computes a direct 3D vector to the target and applies velocity via
 * {@link net.minecraft.world.entity.Mob#setDeltaMovement}. The cultivator
 * gains {@code setNoGravity(true)} for the duration, so they don't sink. The
 * look control tracks the target so the cultivator faces their destination.
 *
 * <p>Altitude is maintained 3-5 blocks above the surface — high enough to
 * clear trees and walls, low enough to remain visible to the player. When
 * the target is directly above/below (e.g., on a tower), the cultivator
 * flies at the target's exact Y.
 *
 * <h2>Animation</h2>
 * <p>On start, sets {@link EntityCultivator#setFlying(boolean)} to true,
 * which sets {@link EntityCultivator#POSE_FLYING}. The renderer reads
 * this and triggers the flight animation in CultivatorRobeModel:
 * <ul>
 *   <li>Body pitched forward ~25° (lean into wind)</li>
 *   <li>Arms swept back (streamlined)</li>
 *   <li>Legs straight back</li>
 *   <li>Robe hem billows UP (gravity-inverted drape)</li>
 *   <li>Hair bun pushed back by wind</li>
 *   <li>Subtle altitude oscillation (±0.1 blocks)</li>
 * </ul>
 *
 * <h2>Why not just use vanilla MoveControl?</h2>
 * <p>Vanilla MoveControl calls navigation.moveTo, which uses the ground
 * pathfinder. The ground pathfinder cannot path through air — it would
 * fail to find a path from a mountain peak to a far village because the
 * path crosses unloaded chunks, water, or unwalkable terrain. Direct
 * velocity manipulation is the only way to do 3D flight in MC 1.20.1
 * without a custom FlightPathNavigator.
 *
 * <p><b>CRON-133 — FlightPathNavigator is no longer future work.</b>
 * {@link CultivatorFlightNavigator} now provides obstacle-aware velocity
 * computation. This goal delegates the per-tick velocity decision to the
 * navigator, which ray-casts forward at 3 heights and either maintains
 * course, dodges perpendicular, or vaults upward. Stuck detection: if the
 * navigator reports blocked for {@value #MAX_BLOCKED_TICKS} consecutive
 * ticks (the cultivator cannot find a way around), the goal aborts and the
 * cultivator reverts to walking.
 *
 * <p><b>CRON-134 — Qi expenditure is no longer future work.</b>
 * Each flight tick consumes {@value #FLIGHT_QI_COST_PER_TICK} absolute qi
 * units from {@link EntityCultivator#getQi()}. The cultivator's maxQi
 * scales with realm (Foundation=100, Core=500, Nascent=2000, Soul+=10000),
 * producing canon-intuitive flight ranges: Foundation ~25s, Core ~125s,
 * Nascent ~500s, Soul+ effectively unlimited (capped by 30s timeout).
 * Qi regenerates at 1.0/sec when not flying. The canUse() gate refuses
 * activation below 10% qi; canContinueToUse() aborts below 5% qi. This
 * closes the 4-round carried-over self-critique (CRON-130 #5, CRON-132 #5,
 * CRON-133 #7) about missing qi expenditure. Canon fidelity: xianxia genre
 * convention universally depicts sword flight as qi-consuming. Web-search
 * 2026-07-27 found NO explicit 仙逆 chapter citation quantifying flight qi
 * cost — this is mod-original interpretation grounded in genre convention.
 */
public class CultivatorFlightGoal extends Goal {

    private final EntityCultivator cultivator;

    /** Distance (in blocks) at which flight activates. Beyond this, walking is too slow. */
    private static final double ACTIVATE_DIST = 18.0D;

    /** Distance (in blocks) at which flight yields back to walking/combat. */
    private static final double YIELD_DIST = 8.0D;

    /** Squared activation distance (for distanceToSqr comparison). */
    private static final double ACTIVATE_DIST_SQ = ACTIVATE_DIST * ACTIVATE_DIST;

    /** Squared yield distance. */
    private static final double YIELD_DIST_SQ = YIELD_DIST * YIELD_DIST;

    /** Maximum flight duration (30 seconds = 600 ticks) before forced landing. */
    private static final int MAX_FLIGHT_TICKS = 600;

    /**
     * Maximum consecutive blocked ticks before aborting flight (5 seconds).
     * If the navigator cannot find a dodge or vault for this long, the
     * cultivator is stuck against an obstacle wider than dodge range —
     * give up and revert to walking.
     */
    private static final int MAX_BLOCKED_TICKS = 100;

    /** Cruising speed (blocks per tick). 0.4 = 8 blocks/sec, slightly faster than sprinting. */
    private static final double FLIGHT_SPEED = 0.40D;

    /** Altitude offset above the surface (in blocks) when cruising. */
    private static final double CRUISE_ALTITUDE = 4.0D;

    /**
     * CRON-134: Qi cost of flight per tick (absolute qi units).
     * At 20 TPS this is 0.2 * 20 = 4 qi/sec. For a Foundation cultivator
     * (maxQi=100), full qi → 25 seconds of flight. For Core (maxQi=500),
     * 125 seconds. For Nascent Soul (maxQi=2000), 500 seconds. For Soul+
     * (maxQi=10000), effectively unlimited (CRON-130's MAX_FLIGHT_TICKS=600
     * caps first at 30s).
     *
     * <p>Canon fidelity: xianxia genre convention universally depicts sword
     * flight as qi-consuming. Web-search 2026-07-27 found NO explicit
     * 仙逆 chapter citation quantifying flight qi cost — this constant is
     * mod-original interpretation grounded in genre convention. Flagged
     * honestly.
     */
    private static final double FLIGHT_QI_COST_PER_TICK = 0.2D;

    /** Ticks elapsed in current flight session. */
    private int flightTicks;

    /** Consecutive ticks the navigator has reported "blocked" — for stuck detection. */
    private int consecutiveBlockedTicks;

    /**
     * CRON-134: Total qi consumed this flight session (for logging).
     * Reset to 0 in start(), accumulated in tick().
     */
    private double qiConsumedThisFlight;

    /** Last flight target position (for re-evaluation throttling). */
    private Vec3 lastTargetPos;

    public CultivatorFlightGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Realm gate — Qi Condensation and mortal cultivators cannot fly.
        if (!cultivator.isFoundationOrHigher()) return false;

        // CRON-134: Qi gate — even a Foundation+ cultivator cannot activate
        // flight if their qi reserves are below 10% of maxQi. They must rest
        // (or absorb spiritual energy) to replenish first. This closes the
        // 4-round carried-over self-critique (CRON-130 #5, CRON-132 #5,
        // CRON-133 self-critique #7) about missing qi expenditure in flight.
        if (!cultivator.hasEnoughQiForFlightActivation()) return false;

        // Do not fly during higher-priority cognitive states.
        if (cultivator.isActivityLocked()) return false;
        if (cultivator.isMeditating() || cultivator.isCasting()
                || cultivator.isObserving() || cultivator.isGuarding()) {
            return false;
        }
        // Don't fly if already flying (canContinueToUse handles continuation).
        if (cultivator.isFlying()) return false;

        // Find a flight-eligible target.
        Vec3 target = resolveFlightTarget();
        if (target == null) return false;

        // Distance gate — only fly if target is beyond activation distance.
        Vec3 origin = cultivator.position();
        double distSq = origin.distanceToSqr(target);
        if (distSq < ACTIVATE_DIST_SQ) return false;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        // Yield if any higher-priority pose activated mid-flight.
        if (cultivator.isActivityLocked()) return false;
        if (cultivator.isMeditating() || cultivator.isCasting()
                || cultivator.isObserving() || cultivator.isGuarding()) {
            return false;
        }
        // Timeout — don't fly forever.
        if (flightTicks >= MAX_FLIGHT_TICKS) return false;

        // CRON-134: Qi exhaustion — if qi drops below 5% of maxQi mid-flight,
        // the cultivator cannot sustain flight and must land. The canUse()
        // gate already prevents re-activation until qi regens above 10%.
        // We do NOT call this in tick() and force-stop — instead we return
        // false here so stop() fires cleanly with the proper logging.
        if (!cultivator.hasEnoughQiForFlightTick()) {
            Ergenverse.LOGGER.warn("[Ergenverse] CultivatorFlightGoal: '{}' qi exhausted (qi={}/{}={}), forcing landing.",
                    cultivator.getCharacterId(),
                    String.format(java.util.Locale.ROOT, "%.1f", cultivator.getQi()),
                    String.format(java.util.Locale.ROOT, "%.1f", cultivator.getMaxQi()),
                    String.format(java.util.Locale.ROOT, "%.1f%%", cultivator.getQiFraction() * 100.0));
            return false;
        }

        Vec3 target = resolveFlightTarget();
        if (target == null) return false;

        // Yield when target is close enough for walking/combat.
        double distSq = cultivator.position().distanceToSqr(target);
        if (distSq < YIELD_DIST_SQ) return false;

        return true;
    }

    @Override
    public void start() {
        flightTicks = 0;
        consecutiveBlockedTicks = 0;
        qiConsumedThisFlight = 0.0;
        lastTargetPos = null;
        cultivator.setFlying(true);
        cultivator.setNoGravity(true);
        // Stop any pending ground navigation — we control movement now.
        cultivator.getNavigation().stop();
        Ergenverse.LOGGER.info("[Ergenverse] CultivatorFlightGoal: '{}' takes flight (realm={}, qi={}/{}={}).",
                cultivator.getCharacterId(), cultivator.getCultivationRealm(),
                String.format(java.util.Locale.ROOT, "%.1f", cultivator.getQi()),
                String.format(java.util.Locale.ROOT, "%.1f", cultivator.getMaxQi()),
                String.format(java.util.Locale.ROOT, "%.1f%%", cultivator.getQiFraction() * 100.0));
    }

    @Override
    public void stop() {
        cultivator.setFlying(false);
        cultivator.setNoGravity(false);
        // Clear horizontal velocity to prevent drift after landing; let gravity
        // pull the cultivator down naturally to the surface.
        cultivator.setDeltaMovement(0, 0, 0);
        cultivator.getNavigation().stop();
        // CRON-134: log total qi consumed this flight session for debugging.
        if (qiConsumedThisFlight > 0.0) {
            Ergenverse.LOGGER.info("[Ergenverse] CultivatorFlightGoal: '{}' lands (ticks={}, qi consumed={}, qi remaining={}/{}={}).",
                    cultivator.getCharacterId(), flightTicks,
                    String.format(java.util.Locale.ROOT, "%.1f", qiConsumedThisFlight),
                    String.format(java.util.Locale.ROOT, "%.1f", cultivator.getQi()),
                    String.format(java.util.Locale.ROOT, "%.1f", cultivator.getMaxQi()),
                    String.format(java.util.Locale.ROOT, "%.1f%%", cultivator.getQiFraction() * 100.0));
        } else {
            Ergenverse.LOGGER.info("[Ergenverse] CultivatorFlightGoal: '{}' lands.",
                    cultivator.getCharacterId());
        }
        flightTicks = 0;
        consecutiveBlockedTicks = 0;
        qiConsumedThisFlight = 0.0;
        lastTargetPos = null;
    }

    @Override
    public void tick() {
        flightTicks++;

        // CRON-134: consume qi for this flight tick. If consumption fails
        // (insufficient qi), drain remaining qi and let canContinueToUse()
        // return false on next check (which logs + forces landing).
        // We consume BEFORE moving so a cultivator who can't afford to fly
        // this tick doesn't get a free tick of flight.
        boolean consumed = cultivator.consumeQi(FLIGHT_QI_COST_PER_TICK);
        if (consumed) {
            qiConsumedThisFlight += FLIGHT_QI_COST_PER_TICK;
        } else {
            // Insufficient qi for this tick — drain what remains and log.
            // canContinueToUse() will return false on the next goal evaluation
            // (which happens after this tick completes), forcing a clean stop().
            qiConsumedThisFlight += cultivator.getQi();  // log the partial drain
            cultivator.drainAllQi();
            Ergenverse.LOGGER.warn("[Ergenverse] CultivatorFlightGoal: '{}' qi insufficient for tick {} (cost={}), draining remaining and landing next tick.",
                    cultivator.getCharacterId(), flightTicks, FLIGHT_QI_COST_PER_TICK);
        }

        Vec3 target = resolveFlightTarget();
        if (target == null) {
            // No target — force stop on next canContinueToUse check.
            return;
        }

        // Re-evaluate target position only when it moved >2 blocks (throttle).
        if (lastTargetPos == null || lastTargetPos.distanceToSqr(target) > 4.0D) {
            lastTargetPos = target;
        }

        // Compute desired cruise altitude: 4 blocks above surface OR target's
        // Y if target is higher (chase a flying target) or much lower (dive).
        double desiredY = computeCruiseAltitude(target);

        // ── CRON-133: delegate velocity computation to CultivatorFlightNavigator ──
        // The navigator ray-casts forward at 3 heights and either maintains
        // course, dodges perpendicular, or vaults upward. This replaces the
        // CRON-130 direct setDeltaMovement which flew through walls.
        CultivatorFlightNavigator.SteerResult steer = CultivatorFlightNavigator.computeSteer(
                cultivator, target, FLIGHT_SPEED, desiredY);
        cultivator.setDeltaMovement(steer.velocity);

        // Stuck detection — if blocked for too many consecutive ticks, abort.
        if (steer.blocked) {
            consecutiveBlockedTicks++;
            if (consecutiveBlockedTicks == 20
                    || consecutiveBlockedTicks == 60
                    || consecutiveBlockedTicks == MAX_BLOCKED_TICKS) {
                Ergenverse.LOGGER.warn("[Ergenverse] CultivatorFlightGoal: '{}' blocked ({} ticks; dodgeL={} dodgeR={} vault={}).",
                        cultivator.getCharacterId(), consecutiveBlockedTicks,
                        steer.dodgedLeft, steer.dodgedRight, steer.vaulted);
            }
            if (consecutiveBlockedTicks >= MAX_BLOCKED_TICKS) {
                Ergenverse.LOGGER.warn("[Ergenverse] CultivatorFlightGoal: '{}' stuck for {} ticks, aborting flight.",
                        cultivator.getCharacterId(), consecutiveBlockedTicks);
                // Force stop — canContinueToUse will return false on next tick
                // because flightTicks >= MAX_FLIGHT_TICKS.
                flightTicks = MAX_FLIGHT_TICKS;
            }
        } else {
            if (consecutiveBlockedTicks > 0) {
                // Log recovery from blockage.
                Ergenverse.LOGGER.info("[Ergenverse] CultivatorFlightGoal: '{}' cleared obstacle after {} ticks.",
                        cultivator.getCharacterId(), consecutiveBlockedTicks);
            }
            consecutiveBlockedTicks = 0;
        }

        // Face the direction of travel (yaw) — based on the ACTUAL velocity
        // (post-navigator), so dodging cultivators face their dodge direction.
        Vec3 velocity = steer.velocity;
        double horizMag = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        if (horizMag > 0.001D) {
            float yaw = (float) (Math.atan2(velocity.z, velocity.x) * 180.0D / Math.PI) - 90.0F;
            cultivator.setYRot(yaw);
            cultivator.yBodyRot = yaw;
            cultivator.yHeadRot = yaw;
        }

        // Look control — track target so head faces destination.
        cultivator.getLookControl().setLookAt(target.x, target.y, target.z, 30.0F, 30.0F);
    }

    /**
     * Resolve the current flight target as a world position. Returns null if
     * no eligible target exists.
     *
     * <p>Priority:
     * <ol>
     *   <li>Combat target (mob.getTarget()) — pursue fleeing enemies.</li>
     *   <li>Following-player UUID — companion catch-up (Li Muwan following Wang Lin).</li>
     * </ol>
     */
    private Vec3 resolveFlightTarget() {
        // 1. Combat target
        LivingEntity combatTarget = cultivator.getTarget();
        if (combatTarget != null && combatTarget.isAlive()) {
            return combatTarget.position();
        }

        // 2. Following-player target
        String uuidStr = cultivator.getFollowingPlayerUuid();
        if (uuidStr != null && !uuidStr.isEmpty()) {
            try {
                UUID playerUuid = UUID.fromString(uuidStr);
                if (cultivator.level().getServer() != null) {
                    var player = cultivator.level().getServer().getPlayerList().getPlayer(playerUuid);
                    if (player != null) {
                        return player.position();
                    }
                }
            } catch (IllegalArgumentException ignored) {
                // Malformed UUID — skip.
            }
        }

        // 3. (Future) navigation target — CognitionDrivenGoal sets a navigation
        //    target via mob.getNavigation().getTargetPos(). If present and far,
        //    use it as the flight destination. This lets intent-driven "go to
        //    X" commitments benefit from flight too.
        BlockPos navTarget = cultivator.getNavigation().getTargetPos();
        if (navTarget != null) {
            return new Vec3(navTarget.getX() + 0.5, navTarget.getY(), navTarget.getZ() + 0.5);
        }

        return null;
    }

    /**
     * Compute the desired cruise Y altitude for the cultivator during flight.
     *
     * <p>Strategy:
     * <ul>
     *   <li>Default: 4 blocks above the cultivator's current Y (clears trees, walls).</li>
     *   <li>If target is above cultivator: target's Y (chase upward).</li>
     *   <li>If target is below cultivator by more than 10 blocks: ease down toward target.</li>
     * </ul>
     */
    private double computeCruiseAltitude(Vec3 target) {
        // Find the surface Y at the cultivator's current XZ (or use target Y if higher).
        BlockPos surface = BlockPos.containing(cultivator.getX(), cultivator.getY(), cultivator.getZ());
        // Probe downward for the highest non-air block (cheap — only checks current column).
        int surfaceY = cultivator.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                surface.getX(), surface.getZ());

        // Cruise 4 blocks above the surface (clears trees/walls), but never below target.
        double cruiseAboveSurface = surfaceY + CRUISE_ALTITUDE;

        // If target is higher than cruise altitude, fly at target's altitude (chase up).
        if (target.y > cruiseAboveSurface) {
            return target.y;
        }
        // If target is far below cruise altitude, descend toward it gradually.
        if (target.y < cruiseAboveSurface - 10.0D) {
            return Math.max(target.y, surfaceY);  // don't descend below surface
        }
        return cruiseAboveSurface;
    }
}
