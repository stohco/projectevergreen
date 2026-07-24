package dev.ergenverse.simulation.event;

import java.util.Map;

/**
 * ActionDescriptors — the <b>compositional</b> semantic layer.
 *
 * <p>Per the user's architectural directive (2026-07-23, round 2):
 * <pre>
 *   "Right now ACT_OF_MERCY looks great. Until six months later.
 *    Then you have ACT_OF_MERCY, ACT_OF_CRUELTY, ACT_OF_SELF_SACRIFICE,
 *    ACT_OF_GREED, ACT_OF_COWARDICE, ACT_OF_FILIAL_PIETY...
 *    Eventually you'll have 150 enum values.
 *
 *    Instead I'd move toward something compositional.
 *
 *    intent = HELP
 *    cost = HIGH
 *    beneficiary = OTHER
 *    risk = HIGH
 *    visibility = PUBLIC
 *
 *    Now systems infer 'This qualifies as mercy.'
 *    instead of hardcoding mercy."
 * </pre>
 *
 * <p>Instead of a fixed enum of every possible moral classification, an
 * action carries <b>five orthogonal descriptors</b>. Any subscriber can
 * compose these into whatever classification it needs:
 * <ul>
 *   <li>{@link Intent#HELP} + {@link Cost#HIGH} + {@link Beneficiary#OTHER}
 *       + {@link Risk#HIGH} + {@link Visibility#PUBLIC} → "this qualifies
 *       as mercy" (a belief the BeliefFormationSubscriber might form).</li>
 *   <li>{@link Intent#HARM} + {@link Cost#NONE} + {@link Beneficiary#SELF}
 *       + {@link Risk#NONE} + {@link Visibility#PRIVATE} → "this is petty
 *       cruelty" (a different belief).</li>
 *   <li>{@link Intent#HELP} + {@link Cost#EXTREME} + {@link Beneficiary#OTHER}
 *       + {@link Risk#EXTREME} + {@link Visibility#PUBLIC} → "this is
 *       self-sacrifice" (another belief — without needing a new enum value).</li>
 * </ul>
 *
 * <p>The {@link SemanticTag} enum is kept for backward compatibility and
 * simple routing, but new code should prefer {@link ActionDescriptors}
 * for any inference that might evolve.
 *
 * <h2>The five axes</h2>
 * <ul>
 *   <li><b>Intent</b> — what the actor was trying to achieve. HELP, HARM,
 *       TRADE, NEUTRAL, SELF_GAIN, DEFEND.</li>
 *   <li><b>Cost</b> — what the actor gave up. NONE, LOW, MEDIUM, HIGH, EXTREME.</li>
 *   <li><b>Beneficiary</b> — who benefits from the action. SELF, OTHER,
 *       FACTION, WORLD, NONE.</li>
 *   <li><b>Risk</b> — danger the actor accepted. NONE, LOW, MEDIUM, HIGH, EXTREME.</li>
 *   <li><b>Visibility</b> — how observable the action was. PRIVATE, LOCAL,
 *       REGIONAL, PUBLIC, GLOBAL.</li>
 * </ul>
 *
 * <p><b>Provenance: INFERRED.</b> The five axes are derived from general
 * moral psychology and social simulation theory. They map naturally onto
 * the Er Gen universe's value systems: intent (compassion vs cruelty),
 * cost (sacrifice), beneficiary (self vs sect vs world), risk (cultivation
 * is dangerous), visibility (face and reputation).
 *
 * <p><b>Immutability:</b> this is a record — all fields are final.
 */
public record ActionDescriptors(
        Intent intent,
        Cost cost,
        Beneficiary beneficiary,
        Risk risk,
        Visibility visibility
) {
    /** A "no descriptors" sentinel for environmental events. */
    public static final ActionDescriptors EMPTY = new ActionDescriptors(
            Intent.NEUTRAL, Cost.NONE, Beneficiary.NONE, Risk.NONE, Visibility.PRIVATE);

    /** Builder for ergonomic construction. */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Infer a {@link SemanticTag} from these descriptors.
     *
     * <p>This is the "systems infer 'this qualifies as mercy'" principle.
     * Instead of hardcoding the semantic tag at publish time, subscribers
     * can call this to derive a classification from the compositional
     * descriptors. The mapping is conservative — only high-confidence
     * patterns are classified.
     */
    public SemanticTag inferSemanticTag() {
        // HELP + HIGH cost + OTHER beneficiary + HIGH risk = mercy
        if (intent == Intent.HELP && cost.ordinal() >= Cost.HIGH.ordinal()
                && beneficiary == Beneficiary.OTHER
                && risk.ordinal() >= Risk.HIGH.ordinal()) {
            return SemanticTag.ACT_OF_MERCY;
        }
        // HARM + surrendered/defenseless target = cruelty
        if (intent == Intent.HARM && beneficiary == Beneficiary.NONE) {
            return SemanticTag.ACT_OF_CRUELTY;
        }
        // HELP + SELF gain = normal trade/interaction, not mercy
        if (intent == Intent.HELP) return SemanticTag.GIFT_GIVEN;
        if (intent == Intent.HARM) return SemanticTag.COMBAT_ENGAGED;
        if (intent == Intent.DEFEND) return SemanticTag.COMBAT_ENGAGED;
        return null; // no confident classification
    }

    // ─── The five axes ─────────────────────────────────────────────

    /** What the actor was trying to achieve. */
    public enum Intent {
        HELP,       // Aiding another actor
        HARM,       // Attacking or hindering another actor
        TRADE,      // Mutual exchange
        DEFEND,     // Protecting self or others
        SELF_GAIN,  // Pursuing personal benefit
        NEUTRAL     // No clear intent (environmental, accidental)
    }

    /** What the actor gave up to perform the action. */
    public enum Cost {
        NONE,     // No cost (a word, a gesture)
        LOW,      // Trivial cost (a common herb, a few minutes)
        MEDIUM,   // Moderate cost (a useful item, significant time)
        HIGH,     // Significant cost (a rare treasure, great effort)
        EXTREME   // Life-changing cost (a life-saving artifact, years of cultivation)
    }

    /** Who benefits from the action. */
    public enum Beneficiary {
        SELF,     // The actor themselves
        OTHER,    // A specific other actor
        FACTION,  // The actor's sect/family/faction
        WORLD,    // The world at large (ecology, karma)
        NONE      // No beneficiary (destruction, waste)
    }

    /** Danger the actor accepted by performing the action. */
    public enum Risk {
        NONE,     // No risk
        LOW,      // Minor risk (social embarrassment)
        MEDIUM,   // Moderate risk (minor injury, reputation loss)
        HIGH,     // Significant risk (serious injury, sect expulsion)
        EXTREME   // Life-threatening risk (death, soul destruction)
    }

    /** How observable the action was — determines rumor propagation. */
    public enum Visibility {
        PRIVATE,   // Only the actors involved saw it
        LOCAL,     // Nearby NPCs (~64 blocks) saw it
        REGIONAL,  // The whole region will hear within a day
        PUBLIC,    // The whole country will hear (sect war, patriarch breakthrough)
        GLOBAL     // The entire cultivation world will feel it (ascension)
    }

    // ─── Builder ──────────────────────────────────────────────────

    /** Ergonomic builder for ActionDescriptors. */
    public static final class Builder {
        private Intent intent = Intent.NEUTRAL;
        private Cost cost = Cost.NONE;
        private Beneficiary beneficiary = Beneficiary.NONE;
        private Risk risk = Risk.NONE;
        private Visibility visibility = Visibility.PRIVATE;

        public Builder intent(Intent v) { this.intent = v; return this; }
        public Builder cost(Cost v) { this.cost = v; return this; }
        public Builder beneficiary(Beneficiary v) { this.beneficiary = v; return this; }
        public Builder risk(Risk v) { this.risk = v; return this; }
        public Builder visibility(Visibility v) { this.visibility = v; return this; }

        public ActionDescriptors build() {
            return new ActionDescriptors(intent, cost, beneficiary, risk, visibility);
        }
    }
}
