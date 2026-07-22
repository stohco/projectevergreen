package dev.ergenverse.history;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.event.EnergyType;
import dev.ergenverse.simulation.event.SemanticEventTopics;
import dev.ergenverse.simulation.event.SemanticTag;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * HistoryManager — the <b>single entry point</b> where gameplay code
 * publishes player and NPC actions to the WorldEventBus.
 *
 * <p><b>Per user directive (2026-07-23 #2):</b>
 * <pre>
 *   "I'd remove SimulationActions.
 *    The world should only know about events. Not who published them.
 *
 *    PlayerController → publish(WorldEvent) → Bus
 *    NpcAI → publish(WorldEvent) → Bus
 *
 *    No special 'simulation action' layer."
 * </pre>
 *
 * <p>This class is the answer to "who calls WorldEventBus.dispatch()?"
 * Gameplay code (EntityCultivator, CultivationEvents, NpcGiftOfferGoal, etc.)
 * calls these static methods, which construct and dispatch WorldEvents
 * directly. There is no intermediate coordinator. The event flows:
 * <pre>
 *   Gameplay code
 *     → HistoryManager.onNpcInteraction(...)
 *     → WorldEventBus.dispatch(player.interaction WorldEvent)
 *     → ALL subscribers observe it
 * </pre>
 *
 * <p>Each method constructs a WorldEvent with:
 * <ul>
 *   <li>The correct topic from {@link SemanticEventTopics}</li>
 *   <li>The appropriate {@link EnergyType} for propagation radius</li>
 *   <li>Actor metadata (sourceActorId = player UUID, targetActorId = NPC canon ID)</li>
 *   <li>A {@link SemanticTag} for the meaning layer</li>
 *   <li>Structured metadata (no more string parsing by subscribers)</li>
 * </ul>
 *
 * <p><b>Structured metadata (per user directive):</b> Each method packs
 * machine-readable data into the event's metadata map. Subscribers read
 * this directly — no parsing description strings. Examples:
 * <ul>
 *   <li>{@link #onNpcInteraction} → metadata: interactionType, npcCanonId</li>
 *   <li>{@link #onGiftReceived} → metadata: itemName, sourceNpcId</li>
 *   <li>{@link #onNpcCombat} → metadata: npcName, npcCanonId, combatOutcome</li>
 *   <li>{@link #onBreakthrough} → metadata: fromRealm, toRealm</li>
 * </ul>
 *
 * <p><b>The isManifestation() gate is GONE.</b> Every NPC gets relationship
 * inference via RelationshipEngine, not just manifestations.
 */
public final class HistoryManager {

    private HistoryManager() {}

    // ─── Cultivation Events ──────────────────────────────────────────

    /**
     * Called when the player breaks through to a new realm.
     * Publishes a player.breakthrough event AND a semantic.cultivation_revealed event.
     */
    public static void onBreakthrough(ServerPlayer player,
                                        String fromRealmName,
                                        String toRealmName,
                                        long worldTick) {
        float severity = isNotableRealm(toRealmName) ? 0.8f : 0.5f;

        // Action event
        WorldEventBus.dispatch(WorldEvent.of(
                SemanticEventTopics.PLAYER_BREAKTHROUGH, EnergyType.SPIRITUAL,
                player.blockPosition(), 0.7f, severity,
                player.getName().getString() + " broke through from "
                        + fromRealmName + " to " + toRealmName + ".",
                "SIMULATION", worldTick,
                player.getStringUUID(), "", SemanticTag.BREAKTHROUGH.name(),
                Map.of(
                        "from_realm", fromRealmName,
                        "to_realm", toRealmName,
                        "player_name", player.getName().getString()
                )));

        // Semantic event — a breakthrough IS cultivation revealed.
        WorldEventBus.dispatch(WorldEvent.of(
                SemanticEventTopics.SEMANTIC_CULTIVATION_REVEALED, EnergyType.SPIRITUAL,
                player.blockPosition(), 0.7f, severity,
                player.getName().getString() + " revealed " + toRealmName
                        + " cultivation through a breakthrough.",
                "SIMULATION", worldTick,
                player.getStringUUID(), "", SemanticTag.CULTIVATION_REVEALED.name(),
                Map.of(
                        "realm", toRealmName,
                        "player_name", player.getName().getString()
                )));
    }

    // ─── NPC Interaction ──────────────────────────────────────────────

    /**
     * Called when the player right-clicks an EntityCultivator NPC.
     */
    public static void onNpcInteraction(ServerPlayer player,
                                             String npcCanonId,
                                             String interactionType,
                                             String detail,
                                             long worldTick) {
        WorldEventBus.dispatch(WorldEvent.of(
                SemanticEventTopics.PLAYER_INTERACTION, EnergyType.SOCIAL,
                player.blockPosition(), 0.25f, 0.25f,
                player.getName().getString() + " interacted with " + npcCanonId
                        + " (" + interactionType + "): " + detail,
                "SIMULATION", worldTick,
                player.getStringUUID(), npcCanonId, SemanticTag.INTERACTION.name(),
                Map.of(
                        "interaction_type", interactionType,
                        "npc_canon_id", npcCanonId,
                        "player_name", player.getName().getString()
                )));
    }

    // ─── Gift ─────────────────────────────────────────────────────────

    /**
     * Called when the player receives a gift from an NPC.
     * The NPC is the source actor; the player is the target.
     */
    public static void onGiftReceived(ServerPlayer player,
                                          String protagonistId,
                                          String itemName,
                                          long worldTick) {
        WorldEventBus.dispatch(WorldEvent.of(
                SemanticEventTopics.PLAYER_GIFT_RECEIVED, EnergyType.SOCIAL,
                player.blockPosition(), 0.5f, 0.5f,
                protagonistId + " gave " + itemName
                        + " to " + player.getName().getString() + ".",
                "SIMULATION", worldTick,
                protagonistId, player.getStringUUID(), SemanticTag.GIFT_RECEIVED.name(),
                Map.of(
                        "item_name", itemName,
                        "source_npc_id", protagonistId,
                        "player_name", player.getName().getString()
                )));
    }

    // ─── NPC Combat ────────────────────────────────────────────────────

    /**
     * Called when the player damages or kills an EntityCultivator.
     */
    public static void onNpcCombat(ServerPlayer player,
                                     String npcCanonId,
                                     String npcName,
                                     boolean playerWon,
                                     long worldTick) {
        String outcome = playerWon ? "player_won" : "player_lost";
        float severity = playerWon ? 0.7f : 0.5f;
        String desc = playerWon
                ? player.getName().getString() + " killed " + npcName
                + " (" + npcCanonId + ")."
                : player.getName().getString() + " was defeated by " + npcName + ".";

        WorldEventBus.dispatch(WorldEvent.of(
                SemanticEventTopics.PLAYER_COMBAT_ENGAGED, EnergyType.PHYSICAL,
                player.blockPosition(), 0.6f, severity, desc,
                "SIMULATION", worldTick,
                player.getStringUUID(), npcCanonId, SemanticTag.COMBAT_ENGAGED.name(),
                Map.of(
                        "npc_name", npcName,
                        "npc_canon_id", npcCanonId,
                        "combat_outcome", outcome,
                        "player_won", String.valueOf(playerWon),
                        "player_name", player.getName().getString()
                )));
    }

    // ─── Discovery ────────────────────────────────────────────────────

    /**
     * Called when the player discovers something noteworthy.
     */
    public static void onDiscovery(ServerPlayer player,
                                       String subject,
                                       String detail,
                                       long worldTick) {
        WorldEventBus.dispatch(WorldEvent.of(
                SemanticEventTopics.PLAYER_DISCOVERY, EnergyType.ACQUIRE,
                player.blockPosition(), 0.4f, 0.4f,
                player.getName().getString() + " discovered " + subject
                        + ": " + detail,
                "SIMULATION", worldTick,
                player.getStringUUID(), "", SemanticTag.DISCOVERY.name(),
                Map.of(
                        "subject", subject,
                        "player_name", player.getName().getString()
                )));
    }

    // ─── Actor-to-actor (for NPC-NPC events, called from NPC AI) ───

    /**
     * An NPC gave a gift to another actor.
     * e.g. Wang Lin gives Li Muwan a flower.
     * The simulation doesn't distinguish player vs NPC sources —
     * both flow through the same bus.
     */
    public static void onActorGift(String sourceNpcCanonId, String targetActorId,
                                     String itemName, net.minecraft.core.BlockPos pos,
                                     long worldTick) {
        WorldEventBus.dispatch(WorldEvent.of(
                SemanticEventTopics.ACTOR_GIFT_GIVEN, EnergyType.SOCIAL,
                pos, 0.4f, 0.4f,
                sourceNpcCanonId + " gave " + itemName
                        + " to " + targetActorId + ".",
                "SIMULATION", worldTick,
                sourceNpcCanonId, targetActorId, SemanticTag.GIFT_GIVEN.name(),
                Map.of(
                        "item_name", itemName,
                        "source_npc_id", sourceNpcCanonId
                )));
    }

    /**
     * An NPC engaged in combat with another actor.
     */
    public static void onActorCombat(String sourceNpcCanonId, String targetActorId,
                                      String combatOutcome, net.minecraft.core.BlockPos pos,
                                      long worldTick) {
        WorldEventBus.dispatch(WorldEvent.of(
                SemanticEventTopics.ACTOR_COMBAT_ENGAGED, EnergyType.PHYSICAL,
                pos, 0.6f, 0.6f,
                sourceNpcCanonId + " engaged in combat with "
                        + targetActorId + " (" + combatOutcome + ").",
                "SIMULATION", worldTick,
                sourceNpcCanonId, targetActorId, SemanticTag.COMBAT_ENGAGED.name(),
                Map.of(
                        "combat_outcome", combatOutcome,
                        "source_npc_id", sourceNpcCanonId
                )));
    }

    // ─── Semantic events (meaning layer) ────────────────────────────

    /** An act of mercy — sparing a defeated enemy, healing, feeding. */
    public static void onActOfMercy(ServerPlayer player, String targetActorId,
                                        String detail, long worldTick) {
        WorldEventBus.dispatch(WorldEvent.of(
                SemanticEventTopics.SEMANTIC_ACT_OF_MERCY, EnergyType.SOCIAL,
                player.blockPosition(), 0.6f, 0.6f,
                player.getName().getString() + " showed mercy to "
                        + targetActorId + ": " + detail,
                "SIMULATION", worldTick,
                player.getStringUUID(), targetActorId,
                SemanticTag.ACT_OF_MERCY.name(),
                Map.of("target_id", targetActorId)));
    }

    /** An act of cruelty — killing surrendered foe, torture, etc. */
    public static void onActOfCruelty(ServerPlayer player, String targetActorId,
                                         String detail, long worldTick) {
        WorldEventBus.dispatch(WorldEvent.of(
                SemanticEventTopics.SEMANTIC_ACT_OF_CRUELTY, EnergyType.KARMA,
                player.blockPosition(), 0.7f, 0.7f,
                player.getName().getString() + " committed cruelty against "
                        + targetActorId + ": " + detail,
                "SIMULATION", worldTick,
                player.getStringUUID(), targetActorId,
                SemanticTag.ACT_OF_CRUELTY.name(),
                Map.of("target_id", targetActorId)));
    }

    /** A technique displayed publicly. */
    public static void onTechniqueDisplayed(ServerPlayer player, String techniqueName,
                                             long worldTick) {
        WorldEventBus.dispatch(WorldEvent.of(
                SemanticEventTopics.SEMANTIC_TECHNIQUE_DISPLAYED, EnergyType.SPIRITUAL,
                player.blockPosition(), 0.6f, 0.6f,
                player.getName().getString() + " displayed technique: "
                        + techniqueName,
                "SIMULATION", worldTick,
                player.getStringUUID(), "",
                SemanticTag.TECHNIQUE_DISPLAYED.name(),
                Map.of("technique_name", techniqueName)));
    }

    /** A cultivation level revealed publicly. */
    public static void onCultivationRevealed(ServerPlayer player, String realm,
                                              long worldTick) {
        WorldEventBus.dispatch(WorldEvent.of(
                SemanticEventTopics.SEMANTIC_CULTIVATION_REVEALED, EnergyType.SPIRITUAL,
                player.blockPosition(), 0.6f, 0.6f,
                player.getName().getString() + " revealed cultivation: "
                        + realm,
                "SIMULATION", worldTick,
                player.getStringUUID(), "",
                SemanticTag.CULTIVATION_REVEALED.name(),
                Map.of("realm", realm)));
    }

    /** A promise made between two actors. */
    public static void onPromiseMade(ServerPlayer player, String targetActorId,
                                       String promiseDetail, long worldTick) {
        WorldEventBus.dispatch(WorldEvent.of(
                SemanticEventTopics.SEMANTIC_PROMISE_MADE, EnergyType.SOCIAL,
                player.blockPosition(), 0.5f, 0.5f,
                player.getName().getString() + " made a promise to "
                        + targetActorId + ": " + promiseDetail,
                "SIMULATION", worldTick,
                player.getStringUUID(), targetActorId,
                SemanticTag.PROMISE_MADE.name(),
                Map.of("promise_detail", promiseDetail)));
    }

    /** A previously made promise was broken. */
    public static void onPromiseBroken(ServerPlayer player, String targetActorId,
                                         String promiseDetail, long worldTick) {
        WorldEventBus.dispatch(WorldEvent.of(
                SemanticEventTopics.SEMANTIC_PROMISE_BROKEN, EnergyType.KARMA,
                player.blockPosition(), 0.7f, 0.7f,
                player.getName().getString() + " broke a promise to "
                        + targetActorId + ": " + promiseDetail,
                "SIMULATION", worldTick,
                player.getStringUUID(), targetActorId,
                SemanticTag.PROMISE_BROKEN.name(),
                Map.of("promise_detail", promiseDetail)));
    }

    // ─── Helper predicates ─────────────────────────────────────────────

    private static boolean isNotableRealm(String realmName) {
        if (realmName == null) return false;
        String lower = realmName.toLowerCase();
        return lower.contains("nascent") || lower.contains("soul formation")
                || lower.contains("ascendant") || lower.contains("transcendence")
                || lower.contains("true immortal") || lower.contains("paragon");
    }
}
