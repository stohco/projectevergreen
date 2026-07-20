package dev.ergenverse.simulation.actor;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.npc.memory.NpcCognitiveMemory;
import dev.ergenverse.npc.memory.NpcMemoryTickHandler;
import dev.ergenverse.simulation.cognition.ActivityProcess;
import dev.ergenverse.simulation.cognition.CognitionGoal;
import dev.ergenverse.simulation.cognition.DecisionEngine;
import dev.ergenverse.simulation.los.SimulationLevel;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;

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

    /** Minimum gap between full-cognition ticks for the SAME actor.
     *  600 ticks = 30 seconds. This is fast enough that a player who
     *  walks up to a meditating NPC sees the NPC make a new decision
     *  within half a minute, but slow enough to avoid hot-spinning.
     *  Before this was 24000 (20 min) — so slow that the pipeline
     *  was effectively dead for linked actors. */
    public static final long FULL_COGNITION_MIN_GAP = 600L;

    private ActorTickLoop() {}

    /**
     * Run one actor-tick pass. Called from the main server tick loop.
     *
     * <p>Also syncs linked actors' positions from their Minecraft entities
     * (via {@link dev.ergenverse.simulation.intent.ActorEntityLink}) so the
     * IntentEngine's situational queries use current positions.
     *
     * <p><b>Importance score computation (CE #1 prerequisite):</b>
     * For each linked actor, we compute the distance-to-nearest-player
     * contribution and set storySignificance for canon NPCs. Without this,
     * all actors remain at STATIC_DATA (score 0) and the cognition pipeline
     * never fires. Per Article XLI: the simulation must work — this is the
     * minimum wiring to make it so.
     *
     * @param currentTick the current server tick (overworld gameTime)
     * @param level       the overworld ServerLevel (for entity lookups)
     */
    public static void tick(long currentTick, ServerLevel level) {
        // ── Position sync + importance score computation ──
        // Before this fix, SimulationImportanceScore was never written to,
        // so all actors had score 0 → STATIC_DATA → no cognition ever ran.
        // Now we compute distance for linked actors so player proximity
        // promotes them to ACTIVE_ACTOR or FULL_COGNITION.
        if (level != null) {
            List<ServerPlayer> players = level.players();
            for (Actor a : ActorRegistry.all()) {
                if (dev.ergenverse.simulation.intent.ActorEntityLink.isLinked(a.id)) {
                    dev.ergenverse.simulation.intent.ActorEntityLink.syncPosition(a.id, level);

                    // Compute distance to nearest player (0..1, 1 = closest).
                    // MAX_IMPORTANCE_DISTANCE = 128 blocks (8 chunks).
                    // Within 16 blocks → distance = 1.0 (max).
                    // At 128+ blocks → distance = 0.0 (no contribution).
                    double nearestDistSq = Double.MAX_VALUE;
                    for (ServerPlayer p : players) {
                        double dx = a.blockX + 0.5 - p.getX();
                        double dz = a.blockZ + 0.5 - p.getZ();
                        double distSq = dx * dx + dz * dz;
                        if (distSq < nearestDistSq) nearestDistSq = distSq;
                    }
                    double distBlocks = Math.sqrt(nearestDistSq);
                    a.importance.distance = Math.max(0.0, 1.0 - (distBlocks / 128.0));

                    // Canon NPCs get a base story significance so they can
                    // reach FULL_COGNITION when near a player.
                    // Per Article XLI: no character special-casing — ALL
                    // canon NPCs get this, not just Wang Lin.
                    if (a.isCanon() && a.importance.storySignificance < 0.4) {
                        a.importance.storySignificance = 0.4;
                    }
                }
            }
        }

        boolean seasonalTick = (currentTick % SEASON_TICKS == 0);
        if (!seasonalTick) {
            // Non-seasonal: tick actors that are (a) flagged dirty by events, or
            // (b) linked + promoted to at least ACTIVE_ACTOR by player proximity.
            // Without (b), the cognition pipeline never fires for nearby NPCs.
            for (Actor a : ActorRegistry.all()) {
                boolean dirty = a.lastSimulatedTick == currentTick;
                boolean linkedAndActive = dev.ergenverse.simulation.intent.ActorEntityLink.isLinked(a.id)
                        && a.simLevel.order >= SimulationLevel.ACTIVE_ACTOR.order;
                if (dirty || linkedAndActive) {
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
        // Tick the actor's current activity process EVERY tick
        // (interruption checking, progress, reaction, resume).
        tickActivity(a, tick);

        // DecisionEngine: throttle to avoid hot-spinning.
        // Check using the seasonal tick gate — if this is a non-seasonal
        // tick AND the actor already has an active activity, skip the
        // full decision. This way, the decision fires on seasonal ticks
        // (every 7 MC days) and when the actor has no activity yet.
        boolean hasActiveActivity = a.currentActivity != null
                && !a.currentActivity.isComplete()
                && !a.currentActivity.isAbandoned();
        boolean seasonalTick = (tick % SEASON_TICKS == 0);
        if (!seasonalTick && hasActiveActivity) {
            return;
        }
        // Run the full DecisionEngine pipeline.
        var needs = a.cognition.computeNeedIntensities();
        var decision = DecisionEngine.decide(
                a.cognition.daoIdentity,
                needs,
                a.cognition.physical,
                a.cognition.cultivation,
                a.cognition.social,
                a.cognition.personality,
                "default",
                a.cognition.desires  // Art XXXI: desires produce SOCIAL goals
        );
        a.cognition.activeGoal = decision.goal;
        if (decision.goal != null) {
            decision.goal.status = CognitionGoal.Status.ACTIVE;

            // ── ACTIVITY ASSIGNMENT (Article XLI) ──
            // Map the cognition goal to an ActivityProcess so the
            // interruption pipeline can manage it. Not all goals
            // produce activities (e.g. FLEE is handled by entity AI).
            if (a.currentActivity == null || a.currentActivity.isComplete()
                    || a.currentActivity.isAbandoned()) {
                dev.ergenverse.simulation.cognition.ActivityAssigner.assign(
                        a, decision.goal, tick);
            }

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
                    // Art XXXI.5: NPCs remember what they did.
                    // Activity completion creates a memory so the world
                    // can reference past actions.
                    recordActivityMemory(a, ap, tick);
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

    // ── Memory recording (Art XXXI.5) ──

    /**
     * Record a memory when an NPC completes an activity.
     * Social activities → SOCIAL memory. Meditation → EMOTIONAL.
     * Other activities → OBSERVATION. Tier: medium-term (weeks).
     *
     * <p>This is the other half of the Memory Metric bridge:
     * MemoryEventSubscriber handles world events → memories.
     * This handles NPC actions → memories. Together, the world
     * remembers both what happened TO it and what its inhabitants DID.
     */
    private static void recordActivityMemory(Actor a, ActivityProcess ap, long tick) {
        if (ap.activityType == null) return;

        NpcCognitiveMemory.MemoryCategory category;
        String desc;

        String act = ap.activityType.toLowerCase();
        if (act.contains("social") || act.contains("trade") || act.contains("gift")) {
            category = NpcCognitiveMemory.MemoryCategory.SOCIAL;
            desc = "Completed: " + ap.activityType;
        } else if (act.contains("meditat") || act.contains("cultivat") || act.contains("breath")) {
            category = NpcCognitiveMemory.MemoryCategory.EMOTIONAL;
            desc = "Finished meditating — mind is calm";
        } else if (act.contains("combat") || act.contains("fight") || act.contains("hunt")) {
            category = NpcCognitiveMemory.MemoryCategory.COMBAT;
            desc = "Finished: " + ap.activityType;
        } else {
            category = NpcCognitiveMemory.MemoryCategory.OBSERVATION;
            desc = "Completed: " + ap.activityType;
        }

        NpcMemoryTickHandler.recordMediumTerm(a.id, category, desc, "self", tick);
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
