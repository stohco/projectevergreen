package dev.ergenverse.simulation.event;

/**
 * SemanticTag — the <b>meaning</b> layer of the simulation.
 *
 * <p>Per the user's architectural directive (2026-07-23):
 * <pre>
 *   "Right now you have Gift, Combat, Interaction, Breakthrough.
 *    Those are actions. But the world actually reacts to meaning.
 *    Instead I'd introduce semantic events."
 * </pre>
 *
 * <p>Actions describe <i>what physically happened</i>. Semantic tags
 * describe <i>what it meant</i> — the social, moral, and existential
 * significance that NPCs actually reason about. Wang Lin doesn't care
 * that "the player interacted." He cares that "the player revealed his
 * cultivation publicly." Those aren't the same thing.
 *
 * <h2>The taxonomy</h2>
 * <ul>
 *   <li><b>Action-level tags</b> — classify the physical action:
 *       {@link #INTERACTION}, {@link #GIFT_GIVEN}, {@link #GIFT_RECEIVED},
 *       {@link #COMBAT_ENGAGED}, {@link #BREAKTHROUGH}, {@link #DISCOVERY}.</li>
 *   <li><b>Semantic-level tags</b> — classify the meaning, per the user's
 *       list: {@link #ACT_OF_MERCY}, {@link #ACT_OF_CRUELTY},
 *       {@link #PUBLIC_HUMILIATION}, {@link #PROMISE_MADE},
 *       {@link #PROMISE_BROKEN}, {@link #DEBT_REPAID}, {@link #DEBT_IGNORED},
 *       {@link #FORBIDDEN_KNOWLEDGE_WITNESSED}, {@link #TECHNIQUE_DISPLAYED},
 *       {@link #CULTIVATION_REVEALED}.</li>
 * </ul>
 *
 * <p>A single WorldEvent carries ONE semantic tag, but a single player
 * action can publish MULTIPLE events — e.g. sparing a defeated enemy
 * publishes both a {@code player.combat.engaged} event (the action)
 * and a {@code semantic.act_of_mercy} event (the meaning). Subscribers
 * that care about the action (HistorySubscriber) see the first;
 * subscribers that care about the meaning (RelationshipEngine,
 * OpportunityGenerator) see the second.
 *
 * <p><b>Provenance: INFERRED.</b> The semantic categories are derived
 * from the Er Gen novel's social dynamics: mercy, cruelty, debt, face,
 * forbidden knowledge, and the ever-present danger of revealing one's
 * cultivation level are all recurring themes that drive NPC reactions.
 */
public enum SemanticTag {
    // ─── Action-level tags ───────────────────────────────────
    /** A basic social interaction (right-click, talk, etc.). */
    INTERACTION,
    /** An actor gave a gift to another actor. */
    GIFT_GIVEN,
    /** An actor received a gift from another actor. */
    GIFT_RECEIVED,
    /** Combat occurred between two actors. */
    COMBAT_ENGAGED,
    /** A cultivation breakthrough was achieved. */
    BREAKTHROUGH,
    /** Something noteworthy was discovered. */
    DISCOVERY,

    // ─── Semantic-level tags (the "meaning" layer) ──────────
    /** Mercy was shown — e.g. sparing a defeated enemy, feeding the hungry. */
    ACT_OF_MERCY,
    /** Cruelty was shown — e.g. killing a surrendered foe, destroying a home. */
    ACT_OF_CRUELTY,
    /** Someone was publicly humiliated — face lost, reputation damaged. */
    PUBLIC_HUMILIATION,
    /** A promise was made between two actors. */
    PROMISE_MADE,
    /** A previously made promise was broken. */
    PROMISE_BROKEN,
    /** A debt (material or life-saving) was repaid. */
    DEBT_REPAID,
    /** A debt was ignored or refused repayment. */
    DEBT_IGNORED,
    /** Forbidden knowledge (soul arts, restriction secrets) was witnessed. */
    FORBIDDEN_KNOWLEDGE_WITNESSED,
    /** A cultivation technique was displayed publicly — drawing attention. */
    TECHNIQUE_DISPLAYED,
    /** A cultivation level was revealed — changing how others perceive the actor. */
    CULTIVATION_REVEALED;

    /**
     * Parse a semantic tag name, returning null if the string is empty
     * or doesn't match a known tag.
     */
    public static SemanticTag fromString(String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            return SemanticTag.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
