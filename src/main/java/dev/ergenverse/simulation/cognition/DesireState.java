package dev.ergenverse.simulation.cognition;

/**
 * DesireState — a social want directed at a specific target.
 *
 * <p>Per Article XXXI: "Every NPC every cycle asks: 'Do I want something
 * from someone else right now?'" Desires are HOW the world desires.
 * They are NOT quests, NOT triggers, NOT scripts. They are data that the
 * existing GoalGenerator reads to produce SOCIAL goals.
 *
 * <p>The 10 social engines (Art XXXI.1) are DATA SCHEMAS, not Java classes.
 * The {@code socialEngine} field names which engine this desire draws from:
 * request, offer, teaching, challenge, recruitment, rumor, mentorship,
 * gift, debt, favor.
 *
 * <p>Provenance: INFERRED for all desires in NPC JSON files (they are
 * derived from canon character traits and situation). The JSON source
 * field records the canon basis.
 *
 * <p>This is a record (data), not an engine. Art XXVI compliant.
 */
public record DesireState(
    /** Unique ID within this NPC's desire set (e.g. "warn_about_teng"). */
    String id,
    /** What the NPC wants (human-readable, e.g. "Warn someone about Teng grain purchases"). */
    String what,
    /** Target: who the desire is directed toward. Examples:
     *  "any_family_member" — any NPC in the same faction
     *  "npc_teng_huayuan" — specific NPC
     *  "player" — any player
     *  "nearby_cultivator" — any cultivator within perception range */
    String target,
    /** Why this desire exists (character motivation, canon-grounded). */
    String why,
    /** Which social engine this desire draws from (Art XXXI.1):
     *  request, offer, teaching, challenge, recruitment, rumor,
     *  mentorship, gift, debt, favor. */
    String socialEngine,
    /** Urgency 0..1. Desires below 0.3 are dormant. */
    double urgency,
    /** Minimum ticks between re-activation after fulfillment/cooldown. */
    long cooldownTicks,
    /** Tick when this desire was last fulfilled. -1 if never. */
    long lastFulfilledTick,
    /** Canon source explaining why this desire exists (for audit trail). */
    String source
) {
    /** Convenience constructor without lastFulfilledTick (defaults to -1). */
    public DesireState(String id, String what, String target, String why,
                        String socialEngine, double urgency, long cooldownTicks,
                        String source) {
        this(id, what, target, why, socialEngine, urgency, cooldownTicks, -1, source);
    }

    /** Whether this desire is currently active (not on cooldown). */
    public boolean isActive(long currentTick) {
        if (lastFulfilledTick < 0) return urgency >= 0.3;
        return currentTick - lastFulfilledTick >= cooldownTicks;
    }

    /** Mark this desire as fulfilled at the given tick. */
    public DesireState fulfilled(long tick) {
        return new DesireState(id, what, target, why, socialEngine,
                urgency, cooldownTicks, tick, source);
    }
}
