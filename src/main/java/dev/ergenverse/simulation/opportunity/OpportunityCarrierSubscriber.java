package dev.ergenverse.simulation.opportunity;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.npc.goals.NpcGoalQueue;
import dev.ergenverse.npc.goals.NpcGoalTickHandler;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.event.WorldEventSubscriber;

/**
 * OpportunityCarrierSubscriber — makes NPCs AWARE of emerging opportunities.
 *
 * <p>CRON-32 (Event-Sourced Architecture Wiring): When an opportunity event
 * is published (topic {@code opportunity.*.emerged}), this subscriber finds
 * the nearest NPC actor to the event position and creates an AWARENESS in
 * their goal queue. The NPC doesn't automatically act — they become AWARE
 * of the opportunity. Their Cultivator Mind's scoring will decide whether
 * to pursue it (future work).
 *
 * <p>This is the awareness layer, separate from the claim layer
 * ({@link dev.ergenverse.simulation.action.OpportunityClaimSubscriber}).
 * The claim subscriber handles explicit claiming with motivation scoring;
 * this subscriber handles passive awareness — the NPC notices something
 * interesting happened nearby.
 *
 * <h2>Design</h2>
 * <ul>
 *   <li>Subscribes to topic prefix {@code "opportunity."}.</li>
 *   <li>Only reacts to events whose topic ends with {@code ".emerged"}.</li>
 *   <li>Finds the nearest actor within 64 blocks of the event position.</li>
 *   <li>Pushes an INVESTIGATE goal (priority 40) to the NPC's goal queue —
 *       representing "I noticed something interesting, I should look into it."</li>
 *   <li>Uses {@link NpcGoalTickHandler#pushGoal} for the static convenience API.</li>
 * </ul>
 *
 * <p><b>Not a new Engine (Art XXVI):</b> This is a WorldEventSubscriber
 * that pushes goals via the existing NpcGoalQueue. No new infrastructure.
 *
 * @see dev.ergenverse.simulation.action.OpportunityClaimSubscriber
 *     — the claim layer (inverted ownership, motivation-based)
 */
public final class OpportunityCarrierSubscriber implements WorldEventSubscriber {

    /** Search radius for the nearest aware actor (in blocks). */
    private static final int AWARENESS_RADIUS_BLOCKS = 64;

    /** Priority for the INVESTIGATE goal pushed when an NPC becomes aware. */
    private static final double AWARENESS_GOAL_PRIORITY = 40.0;

    @Override
    public String topicPrefix() {
        return "opportunity.";
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;
        String topic = event.topic();
        if (!topic.endsWith(".emerged")) return;

        // Find the nearest NPC actor within awareness radius.
        Actor nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        double eventX = event.pos().getX();
        double eventZ = event.pos().getZ();
        long radiusSq = (long) AWARENESS_RADIUS_BLOCKS * AWARENESS_RADIUS_BLOCKS;

        for (Actor actor : ActorRegistry.all()) {
            if (actor.type != dev.ergenverse.simulation.actor.ActorType.NPC) continue;

            double dx = actor.blockX - eventX;
            double dz = actor.blockZ - eventZ;
            double distSq = dx * dx + dz * dz;

            if (distSq <= radiusSq && distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = actor;
            }
        }

        if (nearest == null) return;

        // Push an INVESTIGATE goal to the nearest NPC's queue.
        // The NPC is now AWARE of the opportunity but doesn't automatically act.
        // Their Cultivator Mind will decide whether to pursue it.
        long currentTick = event.timestamp();
        String description = "Investigate emerging opportunity: " + topic
                + " near (" + event.pos().getX() + ", " + event.pos().getZ() + ")";

        NpcGoalTickHandler.pushGoal(
                nearest.id,
                NpcGoalQueue.GoalType.INVESTIGATE,
                description,
                AWARENESS_GOAL_PRIORITY,
                currentTick,
                topic, // targetId = the opportunity topic (for later correlation)
                NpcGoalQueue.GoalSource.WORLD_EVENT
        );

        Ergenverse.LOGGER.info("[OpportunityCarrier] opportunity {} emerged near ({}, {}) — actor {} is now aware",
                topic, event.pos().getX(), event.pos().getZ(), nearest.id);
    }
}
