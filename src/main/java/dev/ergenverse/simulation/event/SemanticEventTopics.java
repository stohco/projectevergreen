package dev.ergenverse.simulation.event;

/**
 * SemanticEventTopics — the canonical topic taxonomy for the WorldEventBus.
 *
 * <p>Per the user's architectural directive (2026-07-23): the player must
 * be a <b>first-class actor</b> in the simulation. Player actions publish
 * to the bus exactly like environmental events. The topic determines which
 * subscribers receive the event.
 *
 * <h2>Topic hierarchy</h2>
 * <pre>
 *   player.*        — actions performed BY the player
 *     player.interaction
 *     player.gift.given
 *     player.gift.received
 *     player.combat.engaged
 *     player.breakthrough
 *     player.discovery
 *
 *   actor.*         — actions performed BY any actor (NPC or player)
 *     actor.gift.given
 *     actor.combat.engaged
 *     actor.breakthrough
 *
 *   semantic.*      — meaning-layer events (the "what it meant" classification)
 *     semantic.act_of_mercy
 *     semantic.act_of_cruelty
 *     semantic.public_humiliation
 *     semantic.promise.made
 *     semantic.promise.broken
 *     semantic.debt.repaid
 *     semantic.debt.ignored
 *     semantic.forbidden_knowledge.witnessed
 *     semantic.technique.displayed
 *     semantic.cultivation.revealed
 * </pre>
 *
 * <h2>Subscriber routing</h2>
 * <ul>
 *   <li>{@code HistorySubscriber} subscribes to prefix {@code ""} and
 *       filters to {@code player.} and {@code actor.} topics — it records
 *       the raw action to PlayerHistory, NpcMemory, WorldHistory.</li>
 *   <li>{@code RelationshipEngine} subscribes to prefix {@code ""} and
 *       filters to {@code player.}, {@code actor.}, and {@code semantic.}
 *       topics — it infers relationship deltas from the meaning of the
 *       action, not the action itself.</li>
 *   <li>{@code OpportunityGenerator} subscribes to prefix {@code ""} and
 *       filters to {@code player.combat}, {@code player.breakthrough},
 *       and {@code semantic.} topics — it creates new opportunities
 *       (escort requests, recruitment, etc.) from observed deeds.</li>
 *   <li>{@code MemoryEventSubscriber} (existing) subscribes to prefix
 *       {@code ""} — it records cognitive memories for nearby NPCs.</li>
 *   <li>{@code ChronicleSubscriber} (existing) subscribes to prefix
 *       {@code ""} — it compiles notable events into the WorldChronicle.</li>
 * </ul>
 *
 * <p><b>Architectural test checklist (per user directive):</b>
 * Every new event topic MUST be able to answer these questions. If any
 * answer is "no," that's fine — but it must be <i>intentional</i>, not
 * because nobody wired it.
 * <ol>
 *   <li>Can memories observe it?</li>
 *   <li>Can rumors observe it?</li>
 *   <li>Can relationships observe it?</li>
 *   <li>Can history observe it?</li>
 *   <li>Can opportunities observe it?</li>
 *   <li>Can motivations observe it?</li>
 *   <li>Can dialogue observe it?</li>
 *   <li>Can ecology observe it?</li>
 *   <li>Can knowledge observe it?</li>
 * </ol>
 */
public final class SemanticEventTopics {

    private SemanticEventTopics() {}

    // ─── Player action topics (source = player) ──────────────

    /** Player interacted with an NPC (right-click, talk, etc.). */
    public static final String PLAYER_INTERACTION = "player.interaction";

    /** Player gave a gift to an NPC. */
    public static final String PLAYER_GIFT_GIVEN = "player.gift.given";

    /** Player received a gift from an NPC (the NPC is the source actor). */
    public static final String PLAYER_GIFT_RECEIVED = "player.gift.received";

    /** Player engaged in combat with an NPC. */
    public static final String PLAYER_COMBAT_ENGAGED = "player.combat.engaged";

    /** Player achieved a cultivation breakthrough. */
    public static final String PLAYER_BREAKTHROUGH = "player.breakthrough";

    /** Player discovered something noteworthy. */
    public static final String PLAYER_DISCOVERY = "player.discovery";

    // ─── Actor action topics (source = any actor: NPC or player) ────

    /** An actor (NPC) gave a gift to another actor. e.g. Wang Lin gives Li Muwan a flower. */
    public static final String ACTOR_GIFT_GIVEN = "actor.gift.given";

    /** An actor (NPC) engaged in combat. */
    public static final String ACTOR_COMBAT_ENGAGED = "actor.combat.engaged";

    /** An actor (NPC) achieved a breakthrough. */
    public static final String ACTOR_BREAKTHROUGH = "actor.breakthrough";

    // ─── Semantic topics (the "meaning" layer) ───────────────

    /** Mercy was shown — e.g. sparing a defeated enemy. */
    public static final String SEMANTIC_ACT_OF_MERCY = "semantic.act_of_mercy";

    /** Cruelty was shown — e.g. killing a surrendered foe. */
    public static final String SEMANTIC_ACT_OF_CRUELTY = "semantic.act_of_cruelty";

    /** Someone was publicly humiliated. */
    public static final String SEMANTIC_PUBLIC_HUMILIATION = "semantic.public_humiliation";

    /** A promise was made. */
    public static final String SEMANTIC_PROMISE_MADE = "semantic.promise.made";

    /** A promise was broken. */
    public static final String SEMANTIC_PROMISE_BROKEN = "semantic.promise.broken";

    /** A debt was repaid. */
    public static final String SEMANTIC_DEBT_REPAID = "semantic.debt.repaid";

    /** A debt was ignored. */
    public static final String SEMANTIC_DEBT_IGNORED = "semantic.debt.ignored";

    /** Forbidden knowledge was witnessed. */
    public static final String SEMANTIC_FORBIDDEN_KNOWLEDGE = "semantic.forbidden_knowledge.witnessed";

    /** A technique was displayed publicly. */
    public static final String SEMANTIC_TECHNIQUE_DISPLAYED = "semantic.technique.displayed";

    /** A cultivation level was revealed. */
    public static final String SEMANTIC_CULTIVATION_REVEALED = "semantic.cultivation.revealed";

    // ─── Prefix helpers ──────────────────────────────────────

    /** Prefix matching all player-sourced events. */
    public static final String PLAYER_PREFIX = "player.";

    /** Prefix matching all actor-sourced events (NPC or player). */
    public static final String ACTOR_PREFIX = "actor.";

    /** Prefix matching all semantic (meaning-layer) events. */
    public static final String SEMANTIC_PREFIX = "semantic.";

    /**
     * Whether a topic is a player or actor action (not a pure semantic
     * or environmental event).
     */
    public static boolean isActionTopic(String topic) {
        return topic != null
                && (topic.startsWith(PLAYER_PREFIX) || topic.startsWith(ACTOR_PREFIX));
    }

    /** Whether a topic is a semantic (meaning-layer) event. */
    public static boolean isSemanticTopic(String topic) {
        return topic != null && topic.startsWith(SEMANTIC_PREFIX);
    }
}
