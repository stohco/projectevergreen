package dev.ergenverse.simulation.event;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.actor.ActorTickLoop;
import dev.ergenverse.simulation.cognition.ActivityProcess;

/**
 * ActivityInterruptionSubscriber — checks every WorldEventBus event
 * against all active actors' activity interruption conditions.
 *
 * <p>This is the bridge between the WorldEventBus (the world's nervous
 * system) and the ActivityProcess model (Article XLI). When a beast
 * pack stirs, a qi disturbance erupts, or any world event fires, this
 * subscriber checks each actor's current activity to see if the event
 * should interrupt it.
 *
 * <p><b>How CE #1 works through this:</b>
 * <ol>
 *   <li>A cautious cultivator (personality.caution > 0.7) is meditating
 *       (ActivityProcess with activityType="meditation").</li>
 *   <li>A beast event fires: topic="beast.wolf_pack.stalking".</li>
 *   <li>This subscriber matches the event against the activity's
 *       interruption condition (eventTopicPrefix="beast.").</li>
 *   <li>The actor's personality trait "caution" is checked against the
 *       threshold (0.7). Since caution > 0.7, the activity is
 *       interrupted with reactionType="OBSERVE".</li>
 *   <li>The ActorTickLoop advances the reaction timer, then resumes
 *       the activity (or abandons it, depending on conditions).</li>
 *   <li>The player witnesses: a cultivator standing up from meditation,
 *       looking toward the hills, watching silently. No quest marker.
 *       Just the world happening.</li>
 * </ol>
 *
 * <p><b>Not a new Engine (Art XXVI):</b> This is a WorldEventSubscriber —
 * the same pattern as {@link QiDisturbanceSubscriber}. It uses the
 * existing WorldEventBus, ActorRegistry, and ActorTickLoop. No new bus,
 * no new engine, no new infrastructure. Just one more subscriber that
 * makes existing systems produce the Canon Experience.
 *
 * <p><b>Provenance: INFERRED.</b> The interruption-checking pattern is
 * standard event-driven simulation. The specific conditions (cautious
 * cultivator observing predators) are derived from Wang Lin's early
 * behavior in Renegade Immortal chapters 1-5.
 */
public final class ActivityInterruptionSubscriber implements WorldEventSubscriber {

    /** Maximum distance (blocks) at which an event can interrupt an activity. */
    private static final int MAX_INTERRUPT_DISTANCE = 64;

    @Override
    public String topicPrefix() {
        // Subscribe to ALL events. This subscriber checks each event
        // against each actor's specific interruption conditions.
        // The alternative (subscribing to each possible prefix) would
        // require knowing all prefixes at registration time.
        return "";
    }

    @Override
    public void onEvent(WorldEvent event) {
        // Skip low-severity events — a leaf falling doesn't interrupt meditation.
        if (event.severity() < 0.2f) return;

        for (Actor a : ActorRegistry.all()) {
            ActivityProcess ap = a.currentActivity;
            if (ap == null) continue;
            if (!ap.isInterruptible()) continue;
            if (ap.state != ActivityProcess.STATE_IN_PROGRESS) continue;

            // Distance check: events beyond 64 blocks don't interrupt.
            double distSq = Math.pow(a.blockX - event.pos().getX(), 2)
                          + Math.pow(a.blockZ - event.pos().getZ(), 2);
            if (distSq > (double) MAX_INTERRUPT_DISTANCE * MAX_INTERRUPT_DISTANCE) continue;

            // Check if any interruption condition matches this event.
            ActivityProcess.InterruptionCondition matched = ap.findMatchingCondition(event.topic());
            if (matched == null) continue;

            // The condition matched. Now check personality threshold.
            // If the actor has cognition and personality, evaluate.
            // Otherwise, use a default caution value of 0.3 (most actors
            // ignore distant events).
            float traitValue = 0.3f; // default
            if (a.cognition != null && a.cognition.personality != null) {
                // Use the "default" context (average of all context-specific values)
                // for interruption evaluation. A more sophisticated version would
                // derive context from the event topic.
                double raw = a.cognition.personality.get(matched.personalityTrait, "default");
                traitValue = (float) raw;
            }

            if (traitValue >= matched.personalityThreshold) {
                // INTERRUPT the activity.
                ap.interrupt(
                        event.topic(),
                        event.timestamp(),
                        matched.reactionType,
                        "world_event",
                        event.pos().getX(),
                        event.pos().getY(),
                        event.pos().getZ(),
                        matched.reactionDurationTicks,
                        true // resumeAfter — the caller handles abandon logic
                );

                // Mark the actor dirty so the tick loop processes the reaction immediately.
                ActorTickLoop.markDirty(a, event.timestamp());

                Ergenverse.LOGGER.debug("[Ergenverse] Activity interrupted: {} for {} " +
                                "by event '{}' (trait {} = {}, threshold {})",
                        ap.activityType, a.id, event.topic(),
                        matched.personalityTrait, traitValue, matched.personalityThreshold);
            }
        }
    }
}