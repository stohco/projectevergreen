package dev.ergenverse.simulation.intent;

import dev.ergenverse.simulation.cognition.CognitionGoal;
import dev.ergenverse.simulation.cognition.DaoIdentity;

/**
 * Intent — the immediate strategic framing of an NPC's current behavior.
 *
 * <p>Per the ChatGPT architectural review: "Identity (rarely changes) →
 * Goals (hours) → Immediate Intent (seconds) → Decision → Planner → Tasks
 * → Minecraft Goals → World Changes."
 *
 * <p>An Intent sits BETWEEN a Goal and an Action. The Goal says WHAT the
 * NPC wants to achieve (long-term). The Intent says HOW they're approaching
 * it right now (short-term strategic framing). The Action (via Planner) is
 * the concrete step they take.
 *
 * <h2>Example: Wang Lin at a spirit fruit glade</h2>
 * <pre>
 *   Identity:  DEFIANCE Dao (survivor, defiant, cautious)
 *   Goal:      SEEKING_DAO — "acquire restriction insight from the glade's formation"
 *   Intent:    AVOID_REVEALING_STRENGTH — "observe from distance, let others
 *              compete for the fruit, study the formation's residual qi"
 *   Decision:  WAIT + OBSERVE
 *   Action:    move to vantage point, do not engage
 * </pre>
 *
 * <p>Without the Intent layer, the NPC would jump straight from "I want
 * restriction insight" to "charge at the fruit" — which is NOT how Wang
 * Lin behaves. The Intent layer is what makes him feel like Wang Lin.
 *
 * <h2>Fields</h2>
 * <ul>
 *   <li><b>nature</b> — the {@link IntentNature} (one of 17 types)</li>
 *   <li><b>targetId</b> — who/what the intent is directed at
 *       (e.g. "player", "spirit_fruit_glade", "sect_elder_liu").
 *       May be empty for self-directed intents (CULTIVATE_SECRETLY).</li>
 *   <li><b>sourceGoal</b> — the {@link CognitionGoal} this intent serves</li>
 *   <li><b>urgency</b> — 0..1, how time-pressured this intent is</li>
 *   <li><b>expectedDurationTicks</b> — how long the NPC expects to hold
 *       this intent before re-evaluating (e.g. OBSERVE_FROM_DISTANCE might
 *       be 1200 ticks = 1 minute; CULTIVATE_SECRETLY might be 24000 = 1 day)</li>
 *   <li><b>createdAtTick</b> — when this intent was formed</li>
 * </ul>
 *
 * <p><b>Immutability:</b> Intent is a record. When the NPC re-evaluates,
 * a NEW Intent is produced (the old one is not mutated). This allows the
 * cognition pipeline to compare "what I was doing" vs "what I should do now."
 */
public record Intent(
        IntentNature nature,
        String targetId,
        CognitionGoal sourceGoal,
        double urgency,
        long expectedDurationTicks,
        long createdAtTick
) {
    /**
     * Compact factory for the common case.
     */
    public static Intent of(IntentNature nature, String targetId,
                             CognitionGoal sourceGoal, double urgency,
                             long expectedDurationTicks, long currentTick) {
        return new Intent(nature, targetId, sourceGoal, urgency,
                expectedDurationTicks, currentTick);
    }

    /**
     * Whether this intent has expired and should be re-evaluated.
     *
     * @param currentTick the current server tick
     * @return true if the intent has lived past its expected duration
     */
    public boolean isExpired(long currentTick) {
        return currentTick >= createdAtTick + expectedDurationTicks;
    }

    /**
     * Whether this intent is directed at a specific target (vs self-directed).
     */
    public boolean hasTarget() {
        return targetId != null && !targetId.isEmpty();
    }

    @Override
    public String toString() {
        return "Intent[" + nature.label + " → " + targetId
                + " u=" + Math.round(urgency * 100) + "%"
                + " dur=" + expectedDurationTicks + "t]";
    }
}
