package dev.ergenverse.simulation.settlement;

import java.util.Collections;
import java.util.List;
/**
 * WorldSituation — a unified snapshot of the world state that <b>every actor
 * reasons over</b>.
 *
 * <p>Per the user's architectural directive:
 * <blockquote>
 * Instead of ThreatContext, I'd probably move toward something like "World
 * Situation." Example: Danger, Opportunity, Rumor, Debt, Request, Weather,
 * Politics, Resources, Relationships, History, Time.
 * <br><br>
 * Every actor reasons over the exact same world. They simply reach different
 * conclusions.
 * </blockquote>
 *
 * <p>A {@code WorldSituation} is <b>not per-actor</b>. It is the shared world.
 * The {@link ActorReasoningEngine} takes one situation and N actor profiles and
 * produces N different {@link Activity} decisions. The difference lives in the
 * actors' minds (their {@link ActorProfile}), not in the situation.
 *
 * <p>This is the architectural distinction from the prior cycle's
 * {@code PresenceContext}, which was already per-actor-flavored ("threat active
 * → go home" was baked in). A {@code WorldSituation} just describes the world:
 * "a wolf pack is 40 blocks north of the village, intensity 0.4, the village
 * is fearful, it is dusk." What to <b>do</b> about that is the actor's
 * reasoning, not the situation's.
 *
 * <h2>What it carries (minimal, per "don't invent abstractions")</h2>
 * <ul>
 *   <li><b>primaryThreat</b> — the strongest active threat to the settlement
 *       (type, intensity, distance, direction), or null if peaceful.</li>
 *   <li><b>timeOfDay</b> — the current phase (affects reasoning: night = more
 *       cautious, day = bolder).</li>
 *   <li><b>settlementMood</b> — the settlement's current personality mood
 *       (modifies embedded actors' effective courage).</li>
 *   <li><b>gameTime</b> — the current tick (for expiry calculations).</li>
 * </ul>
 *
 * <p>Future situations (opportunities, rumors, weather, politics) can be added
 * as fields without changing the reasoning engine's contract — the engine just
 * reads more of the world.
 */
public final class WorldSituation {

    /** An active threat to the settlement, or null if peaceful. */
    public final Threat primaryThreat;

    /** The current time-of-day phase. */
    public final TimeOfDay timeOfDay;

    /** The settlement's collective mood (modifies embedded actor courage). */
    public final SettlementPersonality.Mood settlementMood;

    /** The current game tick (for expiry calculations). */
    public final long gameTime;

    /**
     * Known opportunities near the settlement (immutable snapshot).
     * Populated by the ActorMaterializer from OpportunityRegistry.
     * Empty when no opportunities exist nearby. The CultivatorMind uses
     * this to generate INVESTIGATE / PURSUE candidates in peaceful situations.
     *
     * <p>Per CRON-COMPLETIONIST-43: before this field, CultivatorMind.evaluate()
     * returned null for ALL peaceful situations, even when opportunities had
     * emerged nearby. The OpportunityCarrierSubscriber pushed INVESTIGATE goals
     * to NpcGoalQueues, but the mind never reasoned over them. Now the mind
     * can choose to pursue an opportunity — the decision emerges from motivation
     * weights, not from a hardcoded behavior.
     */
    public final List<OpportunitySnapshot> nearbyOpportunities;

    public WorldSituation(Threat primaryThreat, TimeOfDay timeOfDay,
                          SettlementPersonality.Mood settlementMood, long gameTime,
                          List<OpportunitySnapshot> nearbyOpportunities) {
        this.primaryThreat = primaryThreat;
        this.timeOfDay = timeOfDay;
        this.settlementMood = settlementMood;
        this.gameTime = gameTime;
        this.nearbyOpportunities = nearbyOpportunities != null
                ? Collections.unmodifiableList(nearbyOpportunities)
                : List.of();
    }

    /** Constructor without opportunity data (backward-compatible). */
    public WorldSituation(Threat primaryThreat, TimeOfDay timeOfDay,
                          SettlementPersonality.Mood settlementMood, long gameTime) {
        this(primaryThreat, timeOfDay, settlementMood, gameTime, null);
    }

    /** A peaceful situation (no threat, no opportunities). */
    public static WorldSituation peaceful(TimeOfDay tod,
                                          SettlementPersonality.Mood mood, long gameTime) {
        return new WorldSituation(null, tod, mood, gameTime, List.of());
    }

    /** Is any threat active? */
    public boolean hasThreat() {
        return primaryThreat != null;
    }

    /** Are any opportunities known nearby? */
    public boolean hasOpportunities() {
        return !nearbyOpportunities.isEmpty();
    }

    /**
     * A lightweight snapshot of a nearby opportunity for the WorldSituation.
     *
     * <p>Extracted from OpportunityState by the ActorMaterializer. Carries
     * only the fields the CultivatorMind needs to reason over — not the full
     * FSM state machine.
     *
     * @param opportunityId the registry key
     * @param topic          the event topic (e.g. "opportunity.spirit_fruit.ripe")
     * @param category       the classification (SPIRIT_FRUIT, RESTRICTION_CAVE, etc.)
     * @param x              block X
     * @param z              block Z
     * @param intensity      0.0–1.0
     */
    public record OpportunitySnapshot(String opportunityId, String topic,
                                       String category, int x, int z,
                                       float intensity) {
        /** Distance from a given block position. */
        public double distanceFrom(int bx, int bz) {
            double dx = x - bx;
            double dz = z - bz;
            return Math.sqrt(dx * dx + dz * dz);
        }
    }

    /**
     * An active threat to a settlement.
     *
     * @param type       "wolf_pack", "predator", "apex"
     * @param intensity  0.0–1.0 (how strong the disturbance is)
     * @param distance   blocks from settlement center (0 if inside)
     * @param directionX normalized X component pointing toward the threat
     * @param directionZ normalized Z component pointing toward the threat
     * @param expiryTick when this threat expires
     */
    public record Threat(String type, float intensity, float distance,
                         float directionX, float directionZ, long expiryTick) {

        /** Is this a mortal-level threat (wolves) vs. a magical one (apex beast)? */
        public boolean isMortalLevel() {
            return "wolf_pack".equals(type);
        }

        /** Is this an apex-level threat (ancient/spirit beast)? */
        public boolean isApex() {
            return "apex".equals(type);
        }
    }
}
