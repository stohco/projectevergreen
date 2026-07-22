package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.event.WorldEventSubscriber;
import dev.ergenverse.simulation.intent.ActorEntityLink;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpportunityClaimSubscriber — the inverted-ownership piece.
 *
 * <p>Per the user's directive (2026-07-23 #2):
 * <pre>
 *   "NPCs should search for opportunities.
 *
 *    EscortOpportunityCreated gets published. Nobody owns it.
 *    Nearby NPCs evaluate it.
 *    One thinks 'I need protection.'
 *    Another thinks 'I hate that player.'
 *    Another thinks 'I already hired somebody.'
 *    Only one claims it.
 *
 *    That's pure emergence."
 * </pre>
 *
 * <p>This subscriber listens for {@code opportunity.*.emerged} events
 * (published by OpportunityGenerator) and finds nearby linked actors via
 * the ActorRegistry. Each actor evaluates whether they want to claim
 * the opportunity based on their current motivations. The actor with
 * the highest motivation score claims it, and the claim is published
 * as a follow-up event: {@code opportunity.*.claimed_by}.
 *
 * <p>Unclaimed opportunities expire after a configurable timeout (default:
 * 6000 ticks = 5 minutes). This creates natural urgency: an escort
 * request exists for a while, then it fades if nobody acts on it.
 *
 * <p><b>Not a new Engine (Art XXVI):</b> this is a WorldEventSubscriber
 * that publishes follow-up events. No new infrastructure.
 */
public final class OpportunityClaimSubscriber implements WorldEventSubscriber {

    /** Search radius for candidate actors (in blocks). */
    private static final int SEARCH_RADIUS_BLOCKS = 64;

    /** Opportunities expire after this many ticks if unclaimed. */
    private static final long EXPIRY_TICKS = 6000L;

    /** Track unclaimed opportunities: "topic" → tick published. */
    private final ConcurrentHashMap<String, Long> unclaimed = new ConcurrentHashMap<>();

    /** Track which opportunities have been claimed (prevent double-claims). */
    private final ConcurrentHashMap<String, String> claimedBy = new ConcurrentHashMap<>();

    @Override
    public String topicPrefix() {
        return "opportunity.";
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;
        String topic = event.topic();
        if (!topic.endsWith(".emerged")) return;

        // Check if already claimed or expired.
        if (claimedBy.containsKey(topic)) return;
        Long published = unclaimed.get(topic);
        if (published != null && (event.timestamp() - published) > EXPIRY_TICKS) {
            unclaimed.remove(topic);
            Ergenverse.LOGGER.debug("[OpportunityClaim] Opportunity expired: {}", topic);
            return;
        }
        unclaimed.put(topic, event.timestamp());

        // Find nearby linked actors who could claim this.
        String playerUuid = event.meta("player_uuid", "");
        Actor bestClaimer = null;
        float bestMotivation = 0.3f; // minimum threshold to claim

        for (Actor actor : ActorRegistry.all()) {
            if (!ActorEntityLink.isLinked(actor.id)) continue;

            // Distance check.
            double distSq = Math.pow(actor.blockX - event.pos().getX(), 2)
                          + Math.pow(actor.blockZ - event.pos().getZ(), 2);
            if (distSq > (double) SEARCH_RADIUS_BLOCKS * SEARCH_RADIUS_BLOCKS) continue;

            // Skip the player themselves (they don't need to claim their own opportunity).
            if (actor.id.equals(playerUuid)) continue;

            // Evaluate motivation: do they WANT this?
            float motivation = evaluateMotivation(actor, event);
            if (motivation > bestMotivation) {
                bestMotivation = motivation;
                bestClaimer = actor;
            }
        }

        if (bestClaimer != null && bestMotivation >= 0.3f) {
            // Claimed!
            claimedBy.put(topic, bestClaimer.id);
            unclaimed.remove(topic);

            WorldEventBus.dispatch(WorldEvent.of(
                    topic.replace(".emerged", ".claimed_by"),
                    event.energyType(), event.pos(),
                    0.3f, 0.3f,
                    bestClaimer.id + " claimed opportunity: "
                            + event.description(),
                    "SIMULATION:OpportunityClaim", event.timestamp(),
                    bestClaimer.id, playerUuid, "OPPORTUNITY_CLAIMED",
                    Map.of(
                            "opportunity_topic", topic,
                            "claimer_actor_id", bestClaimer.id,
                            "motivation", String.valueOf(bestMotivation),
                            "player_uuid", playerUuid
                    )));

            Ergenverse.LOGGER.info("[OpportunityClaim] {} claimed by {} (motivation={})",
                    topic, bestClaimer.id, bestMotivation);
        } else {
            Ergenverse.LOGGER.debug("[OpportunityClaim] No claimer found for: {}", topic);
        }
    }

    /**
     * Evaluate an actor's motivation to pursue this opportunity.
     * This is where personality, needs, and existing relationships
     * come into play. A villager who needs an escort has high motivation
     * for ESCORT_REQUEST. A sect elder who dislikes the player has
     * negative motivation for GRATITUDE_FAVOR.
     *
     * <p><b>Future: this should query the actor's personality model
     * and current needs from the cognition system.</b> For now, a
     * simple heuristic based on opportunity type.
     */
    private float evaluateMotivation(Actor actor, WorldEvent event) {
        String oppType = event.meta("opportunity_type", "");
        float base = 0.4f;

        // Add some randomness so outcomes aren't deterministic.
        float noise = (float) (Math.random() * 0.2 - 0.1);

        return switch (oppType) {
            case "ESCORT_REQUEST" -> base + 0.2f + noise; // villagers want escorts
            case "RECRUITMENT_OFFER" -> base + 0.1f + noise; // elders are cautious
            case "GRATITUDE_FAVOR" -> base + 0.1f + noise; // depends on alignment
            case "CHALLENGE_ISSUED" -> base - 0.2f + noise; // most NPCs avoid fights
            case "FACTION_INTEREST" -> base + 0.0f + noise; // neutral
            default -> base + noise;
        };
    }
}
