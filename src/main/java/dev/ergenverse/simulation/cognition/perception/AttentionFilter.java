package dev.ergenverse.simulation.cognition.perception;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.cognition.PersonalityModel;

import java.util.ArrayList;
import java.util.List;

/**
 * AttentionFilter — the second layer of the Article XXXV pipeline.
 *
 * <pre>
 *   World -> Perception -> ATTENTION -> Interpretation ->
 *   Prediction -> Goals -> Intent -> Planning -> Tasks -> Activities -> Minecraft
 * </pre>
 *
 * <p>Perception returns EVERYTHING in range. Attention filters it.
 * Without this filter, a Nascent Soul cultivator with 128-block
 * perception radius would interpret all 24 entities — rabbits, bats,
 * distant travelers, underground creatures — as equally important.
 *
 * <p>The user's directive (2026-07-24):
 * <pre>
 *   "He first notices it. Then decides if it matters. Then predicts
 *    what will happen. Then remembers previous encounters. Only then
 *    does he choose an activity."
 * </pre>
 *
 * <p>This class is the "notices" and "decides if it matters" step.
 * It computes salience for each perceived entity using
 * {@link PerceptionSnapshot.PerceivedEntity#salience} and filters
 * out everything below the actor's attention threshold. This threshold
 * scales with cultivation — mortals notice everything (low threshold);
 * ancient cultivators filter aggressively (high threshold), focusing
 * only on entities that genuinely matter.
 *
 * <p>Personality also modulates attention: a CURIOUS actor has a lower
 * threshold (notices more), while a FOCUSED actor has a higher one.
 *
 * <h2>Attention threshold by cultivation realm</h2>
 * <ul>
 *   <li>Mortal: 0.02 — notices almost everything (narrow radius helps)</li>
 *   <li>Qi Condensation: 0.05 — still very aware</li>
 *   <li>Foundation Establishment: 0.10 — filters out distant neutrals</li>
 *   <li>Core Formation: 0.15 — ignores ambient wildlife unless close</li>
 *   <li>Nascent Soul: 0.20 — Wang Lin level: only threats, witnesses, opportunities</li>
 *   <li>Soul Formation+: 0.25 — ancient cultivator: only what matters</li>
 * </ul>
 *
 * <p><b>Design principle:</b> This is NOT a new Engine (Article XXVI).
 * It is a pure function that filters a PerceptionSnapshot.
 * No state, no bus, no subscribers. Just math.
 */
public final class AttentionFilter {

    private AttentionFilter() {}

    /**
     * Apply the attention filter to a perception snapshot.
     *
     * <p>Returns a NEW PerceptionSnapshot with only the attended entities
     * and events. The original snapshot is NOT mutated.
     *
     * @param snap        the raw perception (all entities in range)
     * @param realmOrder  the actor's cultivation realm order (0=mortal, 5=Soul Formation+)
     * @param personality the actor's personality model (may be null)
     * @return an attended snapshot with filtered entities and updated flags
     */
    public static PerceptionSnapshot attend(PerceptionSnapshot snap, int realmOrder,
                                             PersonalityModel personality) {
        // Compute the attention threshold for this actor's cultivation level.
        double threshold = baseThreshold(realmOrder);

        // Personality modulates attention.
        // Curious actors notice more; focused/obsessive actors notice less.
        if (personality != null) {
            double curiosity = personality.get("curiosity", "attention_filter");
            double focus = personality.get("patience", "attention_filter"); // patience ≈ focus
            // Curious: lower threshold (notice more)
            threshold -= (curiosity - 0.5) * 0.08;
            // Patient/Focused: higher threshold (filter more)
            threshold += (focus - 0.5) * 0.06;
        }

        threshold = Math.max(0.01, Math.min(0.5, threshold));

        // Filter entities by salience.
        List<PerceptionSnapshot.PerceivedEntity> attended = new ArrayList<>();
        PerceptionSnapshot.PerceivedEntity mostSalient = null;
        double maxSalience = Double.NEGATIVE_INFINITY;

        for (PerceptionSnapshot.PerceivedEntity e : snap.nearbyEntities) {
            double s = PerceptionSnapshot.salience(e);
            if (s >= threshold) {
                attended.add(e);
            }
            if (s > maxSalience) {
                maxSalience = s;
                mostSalient = e;
            }
        }

        // Re-evaluate the boolean flags based on attended entities only.
        boolean hasThreat = false;
        boolean hasOpportunity = false;
        boolean isObserved = false;
        boolean isAlone = attended.isEmpty();

        for (PerceptionSnapshot.PerceivedEntity e : attended) {
            switch (e.classification) {
                case "hostile" -> hasThreat = true;
                case "prey", "opportunity" -> hasOpportunity = true;
                case "witness", "neutral" -> isObserved = true;
            }
        }

        // Events are filtered by recency: keep only events from the last 5 minutes.
        List<PerceptionSnapshot.PerceivedEvent> attendedEvents = new ArrayList<>();
        PerceptionSnapshot.PerceivedEvent focusEvt = null;
        for (PerceptionSnapshot.PerceivedEvent evt : snap.nearbyEvents) {
            if (evt.ageTicks < 6000L) { // 5 minutes
                attendedEvents.add(evt);
                if (focusEvt == null) focusEvt = evt;
            }
        }

        // Rebuild the snapshot with attended data.
        return new PerceptionSnapshot(
                snap.actorId, snap.tick,
                attended, attendedEvents,
                snap.perceptionRadiusBlocks,
                snap.isNight, snap.isUnderground, snap.biomeOrLocation,
                hasThreat, hasOpportunity, isObserved, isAlone,
                snap.buildTimeNanos
        );
    }

    /**
     * Get the attention threshold for a given realm order.
     * Higher realm = higher threshold = more selective attention.
     */
    private static double baseThreshold(int realmOrder) {
        return switch (realmOrder) {
            case 0 -> 0.02;  // Mortal
            case 1 -> 0.05;  // Qi Condensation
            case 2 -> 0.10;  // Foundation Establishment
            case 3 -> 0.15;  // Core Formation
            case 4 -> 0.20;  // Nascent Soul (Wang Lin)
            case 5 -> 0.25;  // Soul Formation
            default -> 0.30; // Ascendant
        };
    }
}
