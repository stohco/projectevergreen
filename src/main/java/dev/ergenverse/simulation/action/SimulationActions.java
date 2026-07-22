package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.event.EnergyType;
import dev.ergenverse.simulation.event.SemanticEventTopics;
import dev.ergenverse.simulation.event.SemanticTag;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * SimulationActions — the single entry point through which the player
 * (and NPCs) act on the world. <b>This is where the player becomes a
 * first-class actor in the simulation.</b>
 *
 * <p>Per the user's architectural directive (2026-07-23):
 * <pre>
 *   "Instead of:  Player → HistoryManager → Bus
 *    Do:          Player → WorldEventBus → EVERYTHING
 *
 *    HistoryManager shouldn't be the destination.
 *    It should be a subscriber.
 *
 *    The player should be another simulation source."
 * </pre>
 *
 * <p>Before this class existed, gameplay code called
 * {@code HistoryManager.onNpcInteraction(...)} which wrote directly to
 * siloed stores (PlayerHistory, NpcMemory, RelationshipHistory) and
 * <i>never reached the WorldEventBus</i>. The player was a special case
 * that bypassed the simulation. No NPC could remember what the player
 * did, no rumor could spread, the chronicle missed it entirely.
 *
 * <p>Now, gameplay code calls {@code SimulationActions.playerInteractedWithNpc(...)}.
 * This publishes a {@link WorldEvent} to the {@link WorldEventBus} with
 * full actor metadata (sourceActorId = player UUID, targetActorId = NPC
 * canon ID) and a semantic tag. Every subscriber then derives its own
 * consequences from that one fact:
 * <ul>
 *   <li>{@code HistorySubscriber} records it in PlayerHistory, NpcMemory,
 *       WorldHistory.</li>
 *   <li>{@code RelationshipEngine} infers a relationship delta.</li>
 *   <li>{@code MemoryEventSubscriber} records cognitive memories for
 *       nearby NPCs who witnessed it.</li>
 *   <li>{@code RumorEngineEvents} (via MemoryEventSubscriber) seeds a
 *       rumor if the severity is high enough.</li>
 *   <li>{@code ChronicleSubscriber} compiles it into the WorldChronicle.</li>
 *   <li>{@code OpportunityGenerator} may create a new opportunity
 *       (escort request, recruitment offer, etc.).</li>
 * </ul>
 *
 * <p><b>The player obeys the exact same rules as everyone else.</b>
 * A spirit fruit ripening publishes an event. A player giving a gift
 * publishes an event. An NPC giving a gift publishes an event. All
 * three flow through the same bus, to the same subscribers, with the
 * same cascade. The simulation doesn't know or care that the player
 * is special — the player is just another actor whose actions are facts.
 *
 * <h2>The semantic layer</h2>
 * <p>Besides the action-level events (gift, combat, interaction), this
 * class also publishes <b>semantic events</b> — the "meaning" layer.
 * When the player spares a defeated enemy, that's two events:
 * <ol>
 *   <li>{@code player.combat.engaged} (the action)</li>
 *   <li>{@code semantic.act_of_mercy} (the meaning)</li>
 * </ol>
 * The RelationshipEngine reacts to the semantic event (mercy → trust up,
 * fear down), while the HistorySubscriber reacts to the action event
 * (records the combat outcome). This separation lets NPCs reason about
 * <i>meaning</i>, not just physical actions.
 *
 * <h2>Actor IDs</h2>
 * <p>Actor IDs are strings that uniquely identify an actor in the
 * simulation:
 * <ul>
 *   <li>Players: their UUID string (e.g. "550e8400-e29b-...")</li>
 *   <li>NPCs: their canon ID (e.g. "wang_lin", "old_chen",
 *       "heng_yue_elder")</li>
 *   <li>Environmental: empty string "" (spirit fruit, beast migration)</li>
 * </ul>
 * The RelationshipEngine doesn't check "is this Wang Lin?" — it checks
 * "can these two actors have a relationship?" The answer is almost
 * always yes. The graph knows actors, not canon.
 */
public final class SimulationActions {

    private SimulationActions() {}

    // ═══════════════════════════════════════════════════════════════════
    //  Player actions — the player is the source actor
    // ═══════════════════════════════════════════════════════════════════

    /**
     * The player interacted with an NPC (right-click, talk, etc.).
     *
     * @param player         the player who interacted
     * @param npcCanonId     the canon ID of the NPC (e.g. "wang_lin")
     * @param interactionType the type of interaction (e.g. "RIGHT_CLICK", "TALK")
     * @param detail         human-readable detail
     * @param tick           the server tick
     */
    public static void playerInteractedWithNpc(ServerPlayer player, String npcCanonId,
                                                String interactionType, String detail,
                                                long tick) {
        String desc = player.getName().getString() + " interacted with " + npcCanonId
                + " (" + interactionType + "): " + detail;
        publishPlayerAction(SemanticEventTopics.PLAYER_INTERACTION, EnergyType.SOCIAL,
                player, npcCanonId, SemanticTag.INTERACTION,
                0.25f, 0.25f, desc, tick);
    }

    /**
     * The player gave a gift to an NPC.
     *
     * @param player     the player who gave the gift
     * @param npcCanonId the canon ID of the NPC who received it
     * @param itemName   the name of the gift
     * @param tick       the server tick
     */
    public static void playerGaveGift(ServerPlayer player, String npcCanonId,
                                       String itemName, long tick) {
        String desc = player.getName().getString() + " gave " + itemName
                + " to " + npcCanonId + ".";
        publishPlayerAction(SemanticEventTopics.PLAYER_GIFT_GIVEN, EnergyType.SOCIAL,
                player, npcCanonId, SemanticTag.GIFT_GIVEN,
                0.4f, 0.4f, desc, tick);
    }

    /**
     * The player received a gift from an NPC. The NPC is the source actor;
     * the player is the target.
     *
     * @param player     the player who received the gift
     * @param npcCanonId the canon ID of the NPC who gave it
     * @param itemName   the name of the gift
     * @param tick       the server tick
     */
    public static void playerReceivedGift(ServerPlayer player, String npcCanonId,
                                           String itemName, long tick) {
        String desc = npcCanonId + " gave " + itemName
                + " to " + player.getName().getString() + ".";
        // The NPC is the source; the player is the target.
        WorldEvent event = WorldEvent.simulation(
                SemanticEventTopics.PLAYER_GIFT_RECEIVED, EnergyType.SOCIAL,
                player.blockPosition(), 0.5f, 0.5f, desc,
                "SIMULATION", tick,
                npcCanonId, player.getStringUUID(),
                SemanticTag.GIFT_RECEIVED.name());
        WorldEventBus.dispatch(event);
    }

    /**
     * The player engaged in combat with an NPC.
     *
     * @param player     the player
     * @param npcCanonId the canon ID of the NPC
     * @param npcName    the display name of the NPC
     * @param playerWon  whether the player won (killed the NPC)
     * @param tick       the server tick
     */
    public static void playerEngagedCombat(ServerPlayer player, String npcCanonId,
                                            String npcName, boolean playerWon,
                                            long tick) {
        String outcome = playerWon
                ? player.getName().getString() + " killed " + npcName + " (" + npcCanonId + ")."
                : player.getName().getString() + " was defeated by " + npcName + ".";
        float severity = playerWon ? 0.7f : 0.5f;
        publishPlayerAction(SemanticEventTopics.PLAYER_COMBAT_ENGAGED, EnergyType.PHYSICAL,
                player, npcCanonId, SemanticTag.COMBAT_ENGAGED,
                0.6f, severity, outcome, tick);
    }

    /**
     * The player achieved a cultivation breakthrough.
     *
     * @param player      the player
     * @param fromRealm   the realm they broke through FROM
     * @param toRealm     the realm they broke through TO
     * @param tick        the server tick
     */
    public static void playerBreakthrough(ServerPlayer player, String fromRealm,
                                           String toRealm, long tick) {
        String desc = player.getName().getString() + " broke through from "
                + fromRealm + " to " + toRealm + ".";
        // Breakthroughs at notable realms are high-severity (detectable across regions).
        float severity = isNotableRealm(toRealm) ? 0.8f : 0.5f;
        publishPlayerAction(SemanticEventTopics.PLAYER_BREAKTHROUGH, EnergyType.SPIRITUAL,
                player, "", SemanticTag.BREAKTHROUGH,
                0.7f, severity, desc, tick);

        // Also publish a semantic "cultivation revealed" event — a breakthrough
        // is the most dramatic possible revelation of cultivation level.
        publishSemantic(SemanticEventTopics.SEMANTIC_CULTIVATION_REVEALED,
                EnergyType.SPIRITUAL, player, "",
                SemanticTag.CULTIVATION_REVEALED,
                0.7f, severity,
                player.getName().getString() + " revealed " + toRealm
                        + " cultivation through a breakthrough.", tick);
    }

    /**
     * The player discovered something noteworthy.
     *
     * @param player  the player
     * @param subject the subject of the discovery
     * @param detail  human-readable detail
     * @param tick    the server tick
     */
    public static void playerDiscovered(ServerPlayer player, String subject,
                                         String detail, long tick) {
        String desc = player.getName().getString() + " discovered " + subject
                + ": " + detail;
        publishPlayerAction(SemanticEventTopics.PLAYER_DISCOVERY, EnergyType.ACQUIRE,
                player, "", SemanticTag.DISCOVERY,
                0.4f, 0.4f, desc, tick);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  NPC actions — an NPC is the source actor (same rules as player)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * An NPC gave a gift to another actor (player or NPC).
     * e.g. Wang Lin gives Li Muwan a flower.
     *
     * <p>This is the exact same event type as a player giving a gift —
     * the simulation doesn't distinguish. The only difference is the
     * sourceActorId is an NPC canon ID instead of a player UUID.
     *
     * @param sourceNpcCanonId  the canon ID of the NPC giving the gift
     * @param targetActorId     the actor receiving (player UUID or NPC canon ID)
     * @param itemName          the name of the gift
     * @param pos               where the gift was given
     * @param tick              the server tick
     */
    public static void actorGaveGift(String sourceNpcCanonId, String targetActorId,
                                      String itemName, BlockPos pos, long tick) {
        String desc = sourceNpcCanonId + " gave " + itemName
                + " to " + targetActorId + ".";
        WorldEvent event = WorldEvent.simulation(
                SemanticEventTopics.ACTOR_GIFT_GIVEN, EnergyType.SOCIAL,
                pos, 0.4f, 0.4f, desc,
                "SIMULATION", tick,
                sourceNpcCanonId, targetActorId,
                SemanticTag.GIFT_GIVEN.name());
        WorldEventBus.dispatch(event);
    }

    /**
     * An NPC engaged in combat with another actor.
     *
     * @param sourceNpcCanonId  the canon ID of the NPC
     * @param targetActorId     the actor they fought
     * @param pos               where the combat occurred
     * @param tick              the server tick
     */
    public static void actorEngagedCombat(String sourceNpcCanonId, String targetActorId,
                                           BlockPos pos, long tick) {
        String desc = sourceNpcCanonId + " engaged in combat with " + targetActorId + ".";
        WorldEvent event = WorldEvent.simulation(
                SemanticEventTopics.ACTOR_COMBAT_ENGAGED, EnergyType.PHYSICAL,
                pos, 0.6f, 0.6f, desc,
                "SIMULATION", tick,
                sourceNpcCanonId, targetActorId,
                SemanticTag.COMBAT_ENGAGED.name());
        WorldEventBus.dispatch(event);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Semantic events — the "meaning" layer
    // ═══════════════════════════════════════════════════════════════════

    /**
     * An act of mercy was performed — e.g. sparing a defeated enemy,
     * feeding the hungry, healing the wounded.
     *
     * <p>Effects on relationships (handled by RelationshipEngine):
     * <ul>
     *   <li>Target: trust +10, fear -5</li>
     *   <li>Witnesses who value mercy: respect +3</li>
     *   <li>Witnesses who value strength: respect -2</li>
     * </ul>
     */
    public static void actOfMercy(ServerPlayer actor, String targetActorId,
                                   String detail, long tick) {
        publishSemantic(SemanticEventTopics.SEMANTIC_ACT_OF_MERCY,
                EnergyType.SOCIAL, actor, targetActorId,
                SemanticTag.ACT_OF_MERCY,
                0.6f, 0.6f,
                actor.getName().getString() + " showed mercy to " + targetActorId
                        + ": " + detail, tick);
    }

    /**
     * An act of cruelty was performed — e.g. killing a surrendered foe,
     * destroying a home, torturing.
     */
    public static void actOfCruelty(ServerPlayer actor, String targetActorId,
                                     String detail, long tick) {
        publishSemantic(SemanticEventTopics.SEMANTIC_ACT_OF_CRUELTY,
                EnergyType.KARMA, actor, targetActorId,
                SemanticTag.ACT_OF_CRUELTY,
                0.7f, 0.7f,
                actor.getName().getString() + " committed cruelty against " + targetActorId
                        + ": " + detail, tick);
    }

    /**
     * A cultivation technique was displayed publicly — drawing attention.
     * This is distinct from a breakthrough; it's a deliberate or accidental
     * display of power.
     */
    public static void techniqueDisplayed(ServerPlayer actor, String techniqueName,
                                           long tick) {
        publishSemantic(SemanticEventTopics.SEMANTIC_TECHNIQUE_DISPLAYED,
                EnergyType.SPIRITUAL, actor, "",
                SemanticTag.TECHNIQUE_DISPLAYED,
                0.6f, 0.6f,
                actor.getName().getString() + " displayed technique: " + techniqueName, tick);
    }

    /**
     * A cultivation level was revealed — changing how others perceive the actor.
     */
    public static void cultivationRevealed(ServerPlayer actor, String realm,
                                            long tick) {
        publishSemantic(SemanticEventTopics.SEMANTIC_CULTIVATION_REVEALED,
                EnergyType.SPIRITUAL, actor, "",
                SemanticTag.CULTIVATION_REVEALED,
                0.6f, 0.6f,
                actor.getName().getString() + " revealed cultivation: " + realm, tick);
    }

    /**
     * A promise was made between two actors.
     */
    public static void promiseMade(ServerPlayer actor, String targetActorId,
                                    String promiseDetail, long tick) {
        publishSemantic(SemanticEventTopics.SEMANTIC_PROMISE_MADE,
                EnergyType.SOCIAL, actor, targetActorId,
                SemanticTag.PROMISE_MADE,
                0.5f, 0.5f,
                actor.getName().getString() + " made a promise to " + targetActorId
                        + ": " + promiseDetail, tick);
    }

    /**
     * A previously made promise was broken.
     */
    public static void promiseBroken(ServerPlayer actor, String targetActorId,
                                     String promiseDetail, long tick) {
        publishSemantic(SemanticEventTopics.SEMANTIC_PROMISE_BROKEN,
                EnergyType.KARMA, actor, targetActorId,
                SemanticTag.PROMISE_BROKEN,
                0.7f, 0.7f,
                actor.getName().getString() + " broke a promise to " + targetActorId
                        + ": " + promiseDetail, tick);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Internal publishing helpers
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Publish a player-sourced action event. The player is the source actor.
     */
    private static void publishPlayerAction(String topic, EnergyType energy,
                                             ServerPlayer player, String targetActorId,
                                             SemanticTag semanticTag,
                                             float intensity, float severity,
                                             String desc, long tick) {
        WorldEvent event = WorldEvent.simulation(
                topic, energy, player.blockPosition(),
                intensity, severity, desc, "SIMULATION", tick,
                player.getStringUUID(), targetActorId,
                semanticTag.name());
        WorldEventBus.dispatch(event);
        Ergenverse.LOGGER.debug("[SimulationActions] Published {} from player {} → {}",
                topic, player.getName().getString(),
                targetActorId.isEmpty() ? "(no target)" : targetActorId);
    }

    /**
     * Publish a semantic (meaning-layer) event. The player is the source actor.
     */
    private static void publishSemantic(String topic, EnergyType energy,
                                         ServerPlayer player, String targetActorId,
                                         SemanticTag semanticTag,
                                         float intensity, float severity,
                                         String desc, long tick) {
        WorldEvent event = WorldEvent.simulation(
                topic, energy, player.blockPosition(),
                intensity, severity, desc, "SIMULATION", tick,
                player.getStringUUID(), targetActorId,
                semanticTag.name());
        WorldEventBus.dispatch(event);
    }

    // ─── Helper predicates ─────────────────────────────────────────────

    /** Realms notable enough to be high-severity (detectable across regions). */
    private static boolean isNotableRealm(String realmName) {
        if (realmName == null) return false;
        String lower = realmName.toLowerCase();
        return lower.contains("nascent") || lower.contains("soul formation")
                || lower.contains("ascendant") || lower.contains("transcendence")
                || lower.contains("true immortal") || lower.contains("paragon");
    }
}
