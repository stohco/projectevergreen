package dev.ergenverse.simulation.opportunity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Random;

/**
 * SpiritFruitTimeline — the worked example from PROJECT_MASTER.md §5.4.2.
 *
 * <p>Each instance represents ONE spirit fruit ripening event in the world.
 * The timeline is canon-faithful: at NO point does the engine create a
 * "quest". It simply advances world state.
 *
 * <h2>The canon timeline (§5.4.2)</h2>
 * <pre>
 *   Spirit Fruit ripens             t = 0h      (worldTickRipened)
 *   +2h  (2400 ticks)  — Spirit insects gather (3-5 insects)
 *   +4h  (4800 ticks)  — Local deer herd changes route (avoids the glade)
 *   +6h  (7200 ticks)  — Wolf spirit beast notices prey concentration
 *   +8h  (9600 ticks)  — Wandering Qi Condensation cultivator senses fluctuation
 *   +12h (14400 ticks) — Two factions dispatch scouts
 *   +16h (19200 ticks) — Conflict becomes possible
 * </pre>
 *
 * <p>Per §5.4.2: "At NO point did the engine create a quest. It simply
 * advanced world state. A curious player who notices the deer route change
 * at +4h might find the glade at +6h (early, peaceful observation). A late
 * player arriving at +18h finds corpses, scent of blood, and a powerful
 * advanced wolf beast — completely different story, both emergent."
 *
 * <h2>How this class drives the FSM</h2>
 * <p>The timeline is the WORLD SIMULATION for a spirit fruit opportunity.
 * It implements the {@link WorldSimContext} contract (via an inner ctx
 * class) so the {@link OpportunityFSM} can query it for transition gates:
 * <ul>
 *   <li><b>isPerceptible</b> → true once the fruit has ripened
 *       (currentTick ≥ worldTickRipened).</li>
 *   <li><b>getFirstCompetitor</b> → "wolf_spirit_beast" once the +6h
 *       milestone fires. Before that, only insects are present (and they
 *       are NOT competitors — they're pests feeding on the fruit, not
 *       contenders for it).</li>
 *   <li><b>getResolution</b> → "claimed_by_<strongest>" once the +16h
 *       conflict-possible milestone fires. The strongest present actor
 *       wins. If no competitor is present (impossible in the deterministic
 *       timeline, but possible if randomization is added later), the
 *       fruit rots.</li>
 * </ul>
 *
 * <h2>Entity spawning — DEFERRED</h2>
 * <p>The actual entity types ({@code spirit_insect}, {@code wolf_spirit_beast},
 * {@code wandering_qi_cultivator}, scouts) may not exist yet — they will be
 * created by future subagents (PA-B for NPCs, PA-C for ecosystem entities).
 * Until they exist, this class does NOT spawn entities. Instead, it
 * <b>queues spawn actions</b> on the {@link OpportunityState} (see
 * {@link OpportunityState#queueSpawnAction(String)}). A future subagent
 * can drain the queue and spawn real entities once the entity types exist.
 *
 * <h2>Two API styles</h2>
 * <ul>
 *   <li><b>Instance API</b> — {@code new SpiritFruitTimeline(gladePos,
 *       worldTickRipened)} creates a fresh timeline and produces an
 *       {@link OpportunityState} via {@link #toOpportunityState()}.
 *       Use this at opportunity-birth time (e.g. when a spirit fruit
 *       block ripens, or when the OpportunityEngineEvents spawn-cadence
 *       generates a new opportunity near the player).</li>
 *   <li><b>Static driver</b> — {@link #advance(OpportunityState, long,
 *       ServerLevel)} advances an EXISTING state by one tick. Called by
 *       {@link OpportunityRegistry#tickAll} for any state whose
 *       {@code driverType == DRIVER_TYPE}.</li>
 * </ul>
 */
public final class SpiritFruitTimeline {

    /** The driverType string used to identify spirit-fruit opportunities. */
    public static final String DRIVER_TYPE = "spirit_fruit_timeline";

    // ─── Timeline milestones (in ticks relative to worldTickRipened) ───
    // 1 MC hour = 1000 ticks (24,000 ticks = 1 MC day = 24 in-world hours).
    // Per spec, +2h uses 2400 ticks (≈ 2.4 in-world hours ≈ "2h"); we keep
    // the spec's exact numbers for canon-faithful testing.

    /** +2h — Spirit insects gather. */
    public static final long TICK_INSECTS_GATHER       = 2400L;
    /** +4h — Local deer herd changes route (avoids the glade). */
    public static final long TICK_DEER_AVOID           = 4800L;
    /** +6h — Wolf spirit beast notices prey concentration. (FIRST COMPETITOR) */
    public static final long TICK_WOLF_NOTICES         = 7200L;
    /** +8h — Wandering Qi Condensation cultivator senses fluctuation. */
    public static final long TICK_WANDERING_CULTIVATOR = 9600L;
    /** +12h — Two factions dispatch scouts. */
    public static final long TICK_SCOUTS_DISPATCHED    = 14400L;
    /** +16h — Conflict becomes possible (resolution may fire). */
    public static final long TICK_CONFLICT_POSSIBLE    = 19200L;
    /** +24h — Rot fallback: if still unresolved, the fruit rots. */
    public static final long TICK_ROT_FALLBACK         = 28800L;

    // ─── Milestone names (stored in OpportunityState.firedMilestones) ──
    public static final String MILESTONE_INSECTS    = "insects_gather";
    public static final String MILESTONE_DEER       = "deer_avoid";
    public static final String MILESTONE_WOLF       = "wolf_notices";
    public static final String MILESTONE_CULTIVATOR = "wandering_cultivator";
    public static final String MILESTONE_SCOUTS     = "scouts_dispatched";
    public static final String MILESTONE_CONFLICT   = "conflict_possible";

    // ─── Actor IDs (added to OpportunityState.currentActors) ───────────
    public static final String ACTOR_WOLF            = "wolf_spirit_beast";
    public static final String ACTOR_WANDERING_CULT  = "wandering_qi_cultivator";
    public static final String ACTOR_SCOUT_FACTION_A = "scout_faction_A";
    public static final String ACTOR_SCOUT_FACTION_B = "scout_faction_B";

    // ═══════════════════════════════════════════════════════════════════
    //  Instance fields & factory
    // ═══════════════════════════════════════════════════════════════════

    /** The world position of the glade where the fruit ripened. */
    public final BlockPos gladePos;
    /** The world tick at which the fruit ripened (t = 0h of the timeline). */
    public final long worldTickRipened;
    /** The stable opportunity ID (derived from the glade position). */
    public final String opportunityId;

    /**
     * Create a fresh spirit-fruit-timeline instance.
     *
     * @param gladePos         the glade where the fruit ripened
     * @param worldTickRipened the world tick at which the fruit ripened (t=0h)
     */
    public SpiritFruitTimeline(BlockPos gladePos, long worldTickRipened) {
        if (gladePos == null) gladePos = BlockPos.ZERO;
        this.gladePos = gladePos;
        this.worldTickRipened = worldTickRipened;
        this.opportunityId = makeOpportunityId(gladePos);
    }

    /** Generate a stable opportunity ID for a spirit-fruit at the given glade. */
    public static String makeOpportunityId(BlockPos pos) {
        return "ergenverse:spirit_fruit_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
    }

    /**
     * Convert this timeline into a fresh {@link OpportunityState} for
     * registration with the {@link OpportunityRegistry}.
     * <p>The state is created in DORMANT (the fruit has just ripened, but
     * the FSM hasn't ticked yet). On the first FSM tick, the
     * {@link WorldSimContext#isPerceptible} gate returns true and the state
     * transitions to FORMING.
     */
    public OpportunityState toOpportunityState() {
        OpportunityState state = new OpportunityState(opportunityId, gladePos, worldTickRipened);
        state.setDriverType(DRIVER_TYPE);
        state.setDriverTickZero(worldTickRipened);
        return state;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Static driver — called by OpportunityRegistry.tickAll
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Advance a spirit-fruit-timeline opportunity state by one tick.
     *
     * <p>This method:
     * <ol>
     *   <li>Fires any due timeline milestones (adding actors / queuing
     *       spawn actions on the state).</li>
     *   <li>Calls {@link OpportunityFSM#tickOpportunity} with a
     *       {@link SpiritFruitTimelineCtx} so the FSM can advance the
     *       lifecycle state by at most one transition.</li>
     * </ol>
     *
     * @param state       the opportunity state to advance (must have
     *                    {@code driverType == DRIVER_TYPE})
     * @param currentTick the current world tick
     * @param level       the server level (used for RNG)
     */
    public static void advance(OpportunityState state, long currentTick, ServerLevel level) {
        if (state == null || level == null) return;
        if (!DRIVER_TYPE.equals(state.getDriverType())) {
            dev.ergenverse.core.Ergenverse.LOGGER.warn(
                "[SpiritFruitTimeline] advance() called on state with driverType='{}' (expected '{}') — skipping",
                state.getDriverType(), DRIVER_TYPE);
            return;
        }

        long ripened = state.getDriverTickZero();
        long elapsed = currentTick - ripened;

        // ── Fire due milestones (in chronological order) ───────────────
        // Each milestone fires AT MOST ONCE per state (idempotent via
        // markMilestoneFired returning false if already fired).

        fireInsectsGather(state, elapsed, level);
        fireDeerAvoid(state, elapsed);
        fireWolfNotices(state, elapsed);
        fireWanderingCultivator(state, elapsed);
        fireScoutsDispatched(state, elapsed);
        fireConflictPossible(state, elapsed);

        // ── Hand off to the FSM for the lifecycle transition ──────────
        SpiritFruitTimelineCtx ctx = new SpiritFruitTimelineCtx(state, currentTick);
        OpportunityFSM.tickOpportunity(state, currentTick, ctx);
    }

    // ─── Individual milestone firers ───────────────────────────────────

    private static void fireInsectsGather(OpportunityState state, long elapsed, ServerLevel level) {
        if (elapsed < TICK_INSECTS_GATHER) return;
        if (!state.markMilestoneFired(MILESTONE_INSECTS)) return;

        // Spawn 3-5 spirit insects (deterministic count from level RNG).
        int count = 3 + level.getRandom().nextInt(3);
        for (int i = 0; i < count; i++) {
            String actorId = "spirit_insect_" + i;
            state.addActor(actorId);
            // TODO: replace with Ergenverse spirit_insect entity once PA-C creates it
            state.queueSpawnAction("spirit_insect@" + state.getPosX() + "," + state.getPosY() + "," + state.getPosZ());
        }
        dev.ergenverse.core.Ergenverse.LOGGER.debug(
            "[SpiritFruitTimeline] {} +2h milestone fired: {} spirit insects gathered",
            state.getOpportunityId(), count);
    }

    private static void fireDeerAvoid(OpportunityState state, long elapsed) {
        if (elapsed < TICK_DEER_AVOID) return;
        if (!state.markMilestoneFired(MILESTONE_DEER)) return;

        // The deer herd changes route to avoid the glade. We don't add a
        // deer actor (they're LEAVING); instead, we queue a flag action
        // that a future animal-AI subagent can read.
        // TODO: replace with actual animal-AI flag-setting once PA-B/C creates it
        state.queueSpawnAction("flag:deer_avoiding_glade@" + state.getPosX() + "," + state.getPosY() + "," + state.getPosZ());
        dev.ergenverse.core.Ergenverse.LOGGER.debug(
            "[SpiritFruitTimeline] {} +4h milestone fired: deer herd avoiding glade",
            state.getOpportunityId());
    }

    private static void fireWolfNotices(OpportunityState state, long elapsed) {
        if (elapsed < TICK_WOLF_NOTICES) return;
        if (!state.markMilestoneFired(MILESTONE_WOLF)) return;

        state.addActor(ACTOR_WOLF);
        // TODO: replace with Ergenverse wolf_spirit_beast entity once PA-C creates it
        state.queueSpawnAction("wolf_spirit_beast@" + state.getPosX() + "," + state.getPosY() + "," + state.getPosZ());
        dev.ergenverse.core.Ergenverse.LOGGER.debug(
            "[SpiritFruitTimeline] {} +6h milestone fired: wolf spirit beast arrived",
            state.getOpportunityId());
    }

    private static void fireWanderingCultivator(OpportunityState state, long elapsed) {
        if (elapsed < TICK_WANDERING_CULTIVATOR) return;
        if (!state.markMilestoneFired(MILESTONE_CULTIVATOR)) return;

        state.addActor(ACTOR_WANDERING_CULT);
        // TODO: replace with Ergenverse wandering_qi_cultivator entity once PA-B creates it
        state.queueSpawnAction("wandering_qi_cultivator@" + state.getPosX() + "," + state.getPosY() + "," + state.getPosZ());
        dev.ergenverse.core.Ergenverse.LOGGER.debug(
            "[SpiritFruitTimeline] {} +8h milestone fired: wandering Qi Condensation cultivator arrived",
            state.getOpportunityId());
    }

    private static void fireScoutsDispatched(OpportunityState state, long elapsed) {
        if (elapsed < TICK_SCOUTS_DISPATCHED) return;
        if (!state.markMilestoneFired(MILESTONE_SCOUTS)) return;

        state.addActor(ACTOR_SCOUT_FACTION_A);
        state.addActor(ACTOR_SCOUT_FACTION_B);
        // TODO: replace with Ergenverse scout entities once PA-B creates them
        state.queueSpawnAction("scout_faction_A@" + state.getPosX() + "," + state.getPosY() + "," + state.getPosZ());
        state.queueSpawnAction("scout_faction_B@" + state.getPosX() + "," + state.getPosY() + "," + state.getPosZ());
        dev.ergenverse.core.Ergenverse.LOGGER.debug(
            "[SpiritFruitTimeline] {} +12h milestone fired: two factions dispatched scouts",
            state.getOpportunityId());
    }

    private static void fireConflictPossible(OpportunityState state, long elapsed) {
        if (elapsed < TICK_CONFLICT_POSSIBLE) return;
        if (!state.markMilestoneFired(MILESTONE_CONFLICT)) return;

        // No new actors — combat MAY begin between existing actors.
        // The FSM's getResolution will now return a resolution on subsequent ticks.
        dev.ergenverse.core.Ergenverse.LOGGER.debug(
            "[SpiritFruitTimeline] {} +16h milestone fired: conflict becomes possible",
            state.getOpportunityId());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  WorldSimContext implementation (inner class — per-state, per-tick)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * A per-state, per-tick {@link WorldSimContext} that answers the FSM's
     * transition queries based on the spirit-fruit timeline state.
     *
     * <p>Created fresh each tick (cheap — three field assignments). The
     * underlying {@link OpportunityState} is shared, not copied.
     */
    private static final class SpiritFruitTimelineCtx implements WorldSimContext {
        private final OpportunityState state;
        private final long currentTick;
        private final long ripenedTick;
        private final Random rng;

        SpiritFruitTimelineCtx(OpportunityState state, long currentTick) {
            this.state = state;
            this.currentTick = currentTick;
            this.ripenedTick = state.getDriverTickZero();
            // Deterministic seed per-opportunity per-tick — reproducible for debugging.
            // Mixing opportunityId hash + tick so different opportunities at the
            // same tick get different RNGs.
            this.rng = new Random(state.getOpportunityId().hashCode() * 31L + currentTick);
        }

        @Override
        public boolean isPerceptible(String opportunityId) {
            // The fruit is perceptible once it has ripened (currentTick ≥ ripenedTick).
            // Before ripening, the fruit is growing and not perceptible.
            return currentTick >= ripenedTick;
        }

        @Override
        public String getFirstCompetitor(String opportunityId) {
            // First competitor = the wolf spirit beast (arrives at +6h).
            // Insects (which arrive at +2h) are NOT competitors — they are
            // pests feeding on the fruit, not contenders for it.
            if (state.hasFiredMilestone(MILESTONE_WOLF)) {
                return ACTOR_WOLF;
            }
            return null;
        }

        @Override
        public String getResolution(String opportunityId) {
            long elapsed = currentTick - ripenedTick;
            List<String> actors = state.getCurrentActors();

            // Once conflict is possible (+16h), the strongest present actor
            // claims the fruit.
            if (state.hasFiredMilestone(MILESTONE_CONFLICT)) {
                // Strength order (rough canon): wandering cultivator > wolf > scouts
                if (actors.contains(ACTOR_WANDERING_CULT)) {
                    return "claimed_by_" + ACTOR_WANDERING_CULT;
                }
                if (actors.contains(ACTOR_WOLF)) {
                    return "claimed_by_" + ACTOR_WOLF;
                }
                if (actors.contains(ACTOR_SCOUT_FACTION_A) || actors.contains(ACTOR_SCOUT_FACTION_B)) {
                    // Two factions fought; one wins (randomly for now — a future
                    // combat-resolution subagent can replace this).
                    return rng.nextBoolean()
                        ? "claimed_by_" + ACTOR_SCOUT_FACTION_A
                        : "claimed_by_" + ACTOR_SCOUT_FACTION_B;
                }
                // No competitor present at +16h — the fruit rots.
                return "rotted";
            }

            // Rot fallback: if 24h have passed and no conflict was possible
            // (e.g., the timeline was somehow blocked), the fruit rots.
            if (elapsed >= TICK_ROT_FALLBACK) {
                return "rotted";
            }

            return null;  // still contested
        }

        @Override public long currentTick() { return currentTick; }
        @Override public Random random() { return rng; }
    }
}
