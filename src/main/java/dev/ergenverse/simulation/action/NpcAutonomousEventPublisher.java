package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.actor.ActorType;
import dev.ergenverse.simulation.cognition.ActivityProcess;
import dev.ergenverse.simulation.event.EnergyType;
import dev.ergenverse.simulation.event.SemanticEventTopics;
import dev.ergenverse.simulation.event.SemanticTag;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;

/**
 * NpcAutonomousEventPublisher — publishes WorldEvents when NPCs change their
 * own state, making NPC-to-NPC interactions flow through the event bus.
 *
 * <p>Per the user's 2026-07-23 directive: "The player is a first-class actor."
 * But so is every NPC. Before this class, NPC actions (meditating, completing
 * an activity, moving to a new location, choosing a new goal) were invisible
 * to the event bus. Other NPCs could not observe them. Wang Lin meditating
 * for 3 hours would not be noticed by Li Muwan walking past.
 *
 * <p>Now, when NPCs complete activities, change goals, or reach cultivation
 * milestones, this publisher creates WorldEvents that flow through the bus.
 * Every subscriber (MemoryEventSubscriber, NpcSemanticRelationshipSubscriber,
 * ChronicleSubscriber, WangLinSemanticSubscriber) can observe NPC actions
 * just like player actions.
 *
 * <h2>What gets published</h2>
 * <ul>
 *   <li><b>Activity completed</b> — NPC finished meditating, patrolling,
 *       socializing, etc. Topic: {@code actor.activity.completed}.</li>
 *   <li><b>Goal changed</b> — NPC switched from meditating to patrolling,
 *       or from patrolling to investigating. Topic: {@code actor.goal.changed}.</li>
 *   <li><b>Cultivation breakthrough</b> — NPC reached a new realm.
 *       Topic: {@code actor.breakthrough}.</li>
 *   <li><b>Position changed significantly</b> — NPC moved more than 32 blocks
 *       (patrol route, migration, pursuit). Topic: {@code actor.position.moved}.</li>
 *   <li><b>Social interaction</b> — NPC talked to, traded with, or gifted
 *       another NPC. Topic: {@code actor.social.interaction}.</li>
 * </ul>
 *
 * <h2>Design principles</h2>
 * <ul>
 *   <li><b>Article V — Everything Exists Without The Player:</b> NPCs publish
 *       events regardless of player proximity. If Li Muwan refines a pill
 *       while the player is 1000 blocks away, the event still fires.</li>
 *   <li><b>Article XVI — ONE bus:</b> NPC events use the same WorldEventBus
 *       as player events. No separate NPC event channel.</li>
 *   <li><b>Not a new Engine (Art XXVI):</b> Static factory methods that
 *       build events and dispatch them. No new infrastructure.</li>
 *   <li><b>Severity calibration:</b> NPC activity completions are 0.3
 *       (below ledger threshold of 0.45 — not every NPC meditation
 *       needs to go into WorldHistory). Breakthroughs and social
 *       interactions are 0.6+ (historically notable).</li>
 * </ul>
 */
public final class NpcAutonomousEventPublisher {

    /** Minimum distance (blocks) for a position change to be notable. */
    private static final int NOTABLE_MOVE_DISTANCE = 32;

    /** Throttle: minimum ticks between position-change events for the same actor. */
    private static final long POSITION_EVENT_COOLDOWN = 1200; // 1 minute

    /** Last tick each actor published a position-change event. */
    private static final java.util.concurrent.ConcurrentHashMap<String, Long>
            lastPositionEventTick = new java.util.concurrent.ConcurrentHashMap<>();

    private NpcAutonomousEventPublisher() {}

    // ═══════════════════════════════════════════════════════════════
    //  Activity completion events
    // ═══════════════════════════════════════════════════════════════

    /**
     * Publish an event when an NPC completes an activity.
     * Called by ActorTickLoop.tickActivity() when an activity reaches progress 1.0.
     *
     * @param actorId       the NPC's canon ID
     * @param activityType  the type of activity completed (e.g. "MEDITATION", "PATROL")
     * @param pos           the NPC's current position
     * @param tick          the current server tick
     */
    public static void publishActivityCompleted(String actorId, String activityType,
                                                BlockPos pos, long tick) {
        if (actorId == null || activityType == null) return;

        float severity = computeActivitySeverity(activityType);

        WorldEvent event = WorldEvent.of(
                "actor.activity.completed", mapActivityToEnergy(activityType),
                pos, 0.3f, severity,
                actorId + " completed " + activityType.toLowerCase() + ".",
                "SIMULATION", tick,
                actorId, "",
                SemanticTag.DISCOVERY.name(), // generic tag — activity completion
                Map.of("activity_type", activityType,
                        "actor_id", actorId)
        );
        WorldEventBus.dispatch(event);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Goal change events
    // ═══════════════════════════════════════════════════════════════

    /**
     * Publish an event when an NPC's active goal changes.
     * This is how other NPCs observe "Wang Lin stopped meditating and started
     * patrolling" — the goal change flows through the bus as a meaningful
     * state transition.
     *
     * @param actorId       the NPC's canon ID
     * @param oldGoalDesc   the previous goal's description (may be null)
     * @param newGoalDesc   the new goal's description
     * @param newGoalCat    the new goal's category name
     * @param pos           the NPC's current position
     * @param tick          the current server tick
     */
    public static void publishGoalChanged(String actorId, String oldGoalDesc,
                                           String newGoalDesc, String newGoalCat,
                                           BlockPos pos, long tick) {
        if (actorId == null || newGoalDesc == null) return;

        String desc = actorId + " shifted focus to: " + newGoalDesc;
        if (oldGoalDesc != null) {
            desc += " (was: " + oldGoalDesc + ")";
        }

        WorldEvent event = WorldEvent.of(
                "actor.goal.changed", EnergyType.SPIRITUAL,
                pos, 0.2f, 0.3f,
                desc, "SIMULATION", tick,
                actorId, "",
                "", // no specific semantic tag for goal changes
                Map.of("new_goal", newGoalDesc,
                        "new_goal_category", newGoalCat != null ? newGoalCat : "UNKNOWN",
                        "actor_id", actorId)
        );
        WorldEventBus.dispatch(event);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Cultivation breakthrough events (NPC)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Publish a breakthrough event when an NPC reaches a new cultivation realm.
     * This is historically notable (severity 0.7) and triggers:
     * - ChronicleSubscriber recording the event
     * - NpcSemanticRelationshipSubscriber updating witnesses' respect/fear
     * - WangLinSemanticSubscriber noticing if the NPC is in his social graph
     * - RumorNetwork potentially spreading the news
     *
     * @param actorId   the NPC's canon ID
     * @param fromRealm the previous realm name
     * @param toRealm   the new realm name
     * @param pos       the NPC's position
     * @param tick      the current server tick
     */
    public static void publishBreakthrough(String actorId, String fromRealm,
                                           String toRealm, BlockPos pos, long tick) {
        if (actorId == null) return;

        String desc = actorId + " broke through from " + fromRealm + " to " + toRealm + ".";

        // Primary action event
        WorldEvent actionEvent = WorldEvent.of(
                SemanticEventTopics.ACTOR_BREAKTHROUGH, EnergyType.SPIRITUAL,
                pos, 0.7f, 0.7f,
                desc, "SIMULATION", tick,
                actorId, "",
                SemanticTag.BREAKTHROUGH.name(),
                Map.of("from_realm", fromRealm,
                        "to_realm", toRealm,
                        "actor_id", actorId)
        );
        WorldEventBus.dispatch(actionEvent);

        // Companion semantic event: cultivation revealed
        WorldEvent semEvent = WorldEvent.of(
                SemanticEventTopics.SEMANTIC_CULTIVATION_REVEALED, EnergyType.SPIRITUAL,
                pos, 0.7f, 0.7f,
                actorId + " revealed " + toRealm + " cultivation through a breakthrough.",
                "SIMULATION", tick,
                actorId, "",
                SemanticTag.CULTIVATION_REVEALED.name(),
                Map.of("realm", toRealm,
                        "actor_id", actorId)
        );
        WorldEventBus.dispatch(semEvent);

        Ergenverse.LOGGER.info("[NpcAutoPublisher] {} broke through to {}", actorId, toRealm);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Position change events (throttled)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Publish a position-change event if the NPC has moved significantly
     * and enough time has passed since the last event. Throttled to avoid
     * flooding the bus with movement noise.
     *
     * @param actorId   the NPC's canon ID
     * @param fromX     previous block X
     * @param fromZ     previous block Z
     * @param toX       new block X
     * @param toZ       new block Z
     * @param tick      the current server tick
     */
    public static void publishPositionChanged(String actorId,
                                              int fromX, int fromZ,
                                              int toX, int toZ,
                                              long tick) {
        if (actorId == null) return;

        double distSq = (double)(toX - fromX) * (toX - fromX)
                      + (double)(toZ - fromZ) * (toZ - fromZ);
        if (distSq < (double) NOTABLE_MOVE_DISTANCE * NOTABLE_MOVE_DISTANCE) return;

        // Throttle check
        Long lastTick = lastPositionEventTick.get(actorId);
        if (lastTick != null && tick - lastTick < POSITION_EVENT_COOLDOWN) return;
        lastPositionEventTick.put(actorId, tick);

        BlockPos pos = new BlockPos(toX, 64, toZ); // approximate Y
        WorldEvent event = WorldEvent.of(
                "actor.position.moved", EnergyType.PHYSICAL,
                pos, 0.1f, 0.15f,
                actorId + " moved to (" + toX + ", " + toZ + ").",
                "SIMULATION", tick,
                actorId, "",
                "",
                Map.of("from_x", String.valueOf(fromX),
                        "from_z", String.valueOf(fromZ),
                        "to_x", String.valueOf(toX),
                        "to_z", String.valueOf(toZ))
        );
        WorldEventBus.dispatch(event);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Social interaction events (NPC-to-NPC)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Publish an event when two NPCs interact socially (conversation,
     * trade, gift exchange, teaching session, etc.).
     *
     * @param sourceActorId  the initiating NPC's canon ID
     * @param targetActorId  the other NPC's canon ID
     * @param interactionType  type of social interaction
     * @param detail         human-readable detail
     * @param pos            position of the interaction
     * @param tick           current server tick
     */
    public static void publishSocialInteraction(String sourceActorId, String targetActorId,
                                                String interactionType, String detail,
                                                BlockPos pos, long tick) {
        if (sourceActorId == null || targetActorId == null) return;

        float severity = 0.4f;
        if ("TEACHING".equals(interactionType) || "GIFT".equals(interactionType)) {
            severity = 0.5f;
        }
        if ("CONFLICT".equals(interactionType)) {
            severity = 0.65f;
        }

        WorldEvent event = WorldEvent.of(
                "actor.social.interaction", EnergyType.SOCIAL,
                pos, 0.3f, severity,
                sourceActorId + " " + detail.toLowerCase() + " with " + targetActorId + ".",
                "SIMULATION", tick,
                sourceActorId, targetActorId,
                "SOCIAL_INTERACTION",
                Map.of("interaction_type", interactionType,
                        "detail", detail,
                        "source_actor", sourceActorId,
                        "target_actor", targetActorId)
        );
        WorldEventBus.dispatch(event);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Internal helpers
    // ═══════════════════════════════════════════════════════════════

    private static float computeActivitySeverity(String activityType) {
        if (activityType == null) return 0.3f;
        String act = activityType.toLowerCase();
        // Cultivation activities are more notable than routine ones
        if (act.contains("meditat") || act.contains("cultivat") || act.contains("breakthrough"))
            return 0.5f;
        if (act.contains("combat") || act.contains("hunt") || act.contains("fight"))
            return 0.6f;
        if (act.contains("teach") || act.contains("lecture"))
            return 0.45f;
        if (act.contains("trade") || act.contains("craft") || act.contains("refine"))
            return 0.35f;
        if (act.contains("patrol") || act.contains("rest"))
            return 0.2f;
        return 0.3f; // default
    }

    private static EnergyType mapActivityToEnergy(String activityType) {
        if (activityType == null) return EnergyType.SPIRITUAL;
        String act = activityType.toLowerCase();
        if (act.contains("combat") || act.contains("hunt") || act.contains("patrol"))
            return EnergyType.PHYSICAL;
        if (act.contains("trade") || act.contains("social") || act.contains("gift"))
            return EnergyType.SOCIAL;
        if (act.contains("meditat") || act.contains("cultivat"))
            return EnergyType.SPIRITUAL;
        if (act.contains("craft") || act.contains("refine") || act.contains("alchemy"))
            return EnergyType.ACQUIRE;
        return EnergyType.SPIRITUAL;
    }
}
