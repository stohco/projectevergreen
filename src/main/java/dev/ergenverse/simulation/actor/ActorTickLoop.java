package dev.ergenverse.simulation.actor;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.cognition.ActivityProcess;
import dev.ergenverse.simulation.cognition.CognitionGoal;
import dev.ergenverse.simulation.cognition.DecisionEngine;
import dev.ergenverse.simulation.los.SimulationLevel;

/**
 * ActorTickLoop — seasonal / event-driven actor simulation tick.
 *
 * <p><b>Canon audit: NOT daily.</b> Per the Simulation Fidelity Directive,
 * actors are NOT simulated every Minecraft day. They are simulated:
 * <ul>
 *   <li><b>Seasonally</b> (every 7 MC days = ~1 season) for ACTIVE_ACTOR and below.</li>
 *   <li><b>On event</b> (when an event triggers them) for any level.</li>
 *   <li><b>On proximity</b> (when a player approaches within 4 chunks) for
 *       any level — promotes them to a higher sim level temporarily.</li>
 * </ul>
 *
 * <p>The loop reads from {@link ActorRegistry}, filters by sim level, and
 * dispatches to either:
 * <ul>
 *   <li>A light "territory/aggregated" tick for TERRITORY-level actors.</li>
 *   <li>A "goal + plan" tick for ACTIVE_ACTOR-level actors.</li>
 *   <li>A full {@link DecisionEngine#decide} call for FULL_COGNITION+ actors.</li>
 * </ul>
 */
public final class ActorTickLoop {

    /** One season = 7 MC days = 7 * 24000 ticks. */
    public static final long SEASON_TICKS = 7L * 24000L;

    /** Minimum gap between full-cognition ticks (avoid hot-spinning). */
    public static final long FULL_COGNITION_MIN_GAP = 24000L;

    private ActorTickLoop() {}

    /**
     * Run one actor-tick pass. Called from the main server tick loop.
     *
     * <p>Also syncs linked actors' positions from their Minecraft entities
     * (via {@link dev.ergenverse.simulation.intent.ActorEntityLink}) so the
     * IntentEngine's situational queries use current positions.
     *
     * @param currentTick the current server tick (overworld gameTime)
     * @param level       the overworld ServerLevel (for entity lookups)
     */
    public static void tick(long currentTick, net.minecraft.server.level.ServerLevel level) {
        // ── Position sync: pull current entity positions into Actors ──
        // This is critical for the cognition pipeline: the IntentEngine scores
        // intents based on the actor's position, and the CognitionDrivenGoal
        // needs the actor's blockX/Z to be current when it decomposes tasks.
        if (level != null) {
            for (Actor a : ActorRegistry.all()) {
                if (dev.ergenverse.simulation.intent.ActorEntityLink.isLinked(a.id)) {
                    dev.ergenverse.simulation.intent.ActorEntityLink.syncPosition(a.id, level);
                }
            }
        }

        boolean seasonalTick = (currentTick % SEASON_TICKS == 0);
        if (!seasonalTick) {
            // Event-driven path: only re-tick actors flagged dirty (lastSimulatedTick==currentTick)
            // — these are actors that have been touched by an event this tick.
            for (Actor a : ActorRegistry.all()) {
                if (a.lastSimulatedTick == currentTick) {
                    tickActor(a, currentTick);
                }
            }
            return;
        }

        for (Actor a : ActorRegistry.all()) {
            tickActor(a, currentTick);
        }
    }

    private static void tickActor(Actor a, long currentTick) {
        a.recomputeSimLevel();

        switch (a.simLevel) {
            case STATIC_DATA:
            case HISTORICAL:
                // No sim. Existence is enough.
                return;
            case TERRITORY:
                tickTerritory(a, currentTick);
                break;
            case ACTIVE_ACTOR:
                tickActive(a, currentTick);
                break;
            case FULL_COGNITION:
            case STORY_IMPORTANCE:
                tickFullCognition(a, currentTick);
                break;
        }
        a.lastSimulatedTick = currentTick;
    }

    private static void tickTerritory(Actor a, long tick) {
        // Aggregated populations handled by CausalEcology; just record an event heartbeat.
        Ergenverse.LOGGER.debug("[Ergenverse] ActorTick[territory] {} @{}", a.id, tick);
    }

    private static void tickActive(Actor a, long tick) {
        // Process one goal step.
        for (Goal g : a.goals) {
            if (g.isExpired(tick)) g.abandon();
            if (g.isActive()) break;
            if (g.status == Goal.Status.PENDING) {
                g.activate();
                break;
            }
        }
        // Tick the actor's current activity process.
        tickActivity(a, tick);
    }

    private static void tickFullCognition(Actor a, long tick) {
        if (a.cognition == null) return;
        // Tick the actor's current activity process.
        tickActivity(a, tick);
        // Run the full DecisionEngine pipeline.
        var needs = a.cognition.computeNeedIntensities();
        var decision = DecisionEngine.decide(
                a.cognition.daoIdentity,
                needs,
                a.cognition.physical,
                a.cognition.cultivation,
                a.cognition.social,
                a.cognition.personality,
                "default"
        );
        a.cognition.activeGoal = decision.goal;
        if (decision.goal != null) {
            decision.goal.status = CognitionGoal.Status.ACTIVE;

            // ── INTENT LAYER ──
            // Derive the immediate Intent from the active Goal + Dao Identity +
            // Personality. This is the "WHY" behind the NPC's current behavior —
            // the strategic framing that makes Wang Lin feel like Wang Lin.
            // (ChatGPT architectural review: Identity → Goals → Intent → Decision → Action)
            try {
                var intent = dev.ergenverse.simulation.intent.IntentEngine.derive(
                        decision.goal,
                        a.cognition.daoIdentity,
                        a.cognition.personality,
                        a.id,
                        a.blockX, a.blockZ,
                        tick
                );
                a.cognition.activeIntent = intent;
                if (intent != null) {
                    Ergenverse.LOGGER.debug("[Ergenverse] ActorTick[cognition] {} intent: {}",
                            a.id, intent.nature().label);
                }
            } catch (Exception e) {
                Ergenverse.LOGGER.error("[Ergenverse] IntentEngine failed for {}", a.id, e);
            }
        }
        Ergenverse.LOGGER.debug("[Ergenverse] ActorTick[cognition] {} decision: {}",
                a.id, decision);
    }

    /**
     * Tick the actor's current ActivityProcess.
     *
     * <p>Per Article XLI: activity is a process, not a state.
     * This method advances the process through its lifecycle:
     *   IN_PROGRESS → advance progress
     *   REACTING → count down reaction timer, then begin RESUMING
     *   RESUMING → restore progress, transition to IN_PROGRESS
     *
     * <p>INTERRUPTED state is set by {@link dev.ergenverse.simulation.event.ActivityInterruptionSubscriber}
     * when a WorldEventBus event matches an activity's interruption conditions.
     */
    private static void tickActivity(Actor a, long tick) {
        ActivityProcess ap = a.currentActivity;
        if (ap == null) return;

        switch (ap.state) {
            case ActivityProcess.STATE_IN_PROGRESS:
                // Advance progress. Rate is ~1% per 20 ticks (6.7 seconds at 20 tps).
                // A full meditation session takes ~2000 ticks (~100 seconds).
                ap.progress = Math.min(1.0f, ap.progress + 0.0005f);
                if (ap.progress >= 1.0f) {
                    ap.complete();
                    Ergenverse.LOGGER.debug("[Ergenverse] Activity complete: {} for {}",
                            ap.activityType, a.id);
                }
                break;

            case ActivityProcess.STATE_INTERRUPTED:
                // Transitions to REACTING immediately (the subscriber set the reaction).
                ap.beginReaction(tick);
                break;

            case ActivityProcess.STATE_REACTING:
                ap.reactionTicksRemaining--;
                if (ap.reactionTicksRemaining <= 0) {
                    // Reaction over. For now, always resume. A more sophisticated
                    // version would check the InterruptionCondition.resumeAfter flag.
                    ap.beginResume(tick);
                }
                break;

            case ActivityProcess.STATE_RESUMING:
                // Restore progress and return to IN_PROGRESS.
                ap.progress = ap.progressAtInterruption;
                ap.state = ActivityProcess.STATE_IN_PROGRESS;
                Ergenverse.LOGGER.debug("[Ergenverse] Activity resumed: {} for {} at progress {}",
                        ap.activityType, a.id, ap.progress);
                break;

            case ActivityProcess.STATE_COMPLETED:
            case ActivityProcess.STATE_ABANDONED:
                // Activity is done. Clear it so the DecisionEngine can assign a new one.
                a.currentActivity = null;
                break;

            default:
                // NOT_STARTED, STARTING — no action needed. The entity AI handles start.
                break;
        }
    }

    /** Mark an actor dirty for event-driven re-tick this pass. */
    public static void markDirty(Actor a, long currentTick) {
        a.lastSimulatedTick = currentTick; // will be picked up by tick()
    }

    /** Promote an actor's sim level temporarily (player proximity). */
    public static void promoteTemporarily(Actor a, SimulationLevel floor) {
        if (a.simLevel.order < floor.order) {
            a.simLevel = floor;
        }
    }
}
