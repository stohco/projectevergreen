package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.event.ActionDescriptors;
import dev.ergenverse.simulation.event.EnergyType;
import dev.ergenverse.simulation.event.SemanticEventTopics;
import dev.ergenverse.simulation.event.SemanticTag;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * SimulationActions — <b>pure factory methods</b> that build WorldEvents
 * for player and NPC actions. Callers dispatch the event to the bus.
 *
 * <p><b>Architectural evolution (2026-07-23, round 2):</b>
 * The user's critique: "Why does SimulationActions exist? If it's just
 * packaging a WorldEvent, then it's becoming another coordinator."
 *
 * <p>Resolved: SimulationActions is now a <b>factory</b>, not a coordinator.
 * Every method returns a {@link WorldEvent} (or a list of them). The caller
 * dispatches:
 * <pre>
 *   WorldEvent event = SimulationActions.interactionEvent(player, npcId, "RIGHT_CLICK", detail, tick);
 *   WorldEventBus.dispatch(event);
 * </pre>
 * The world only knows about events. Not who published them. The bus is
 * the single coordinator.
 *
 * <p>For backward compatibility, the old {@code player*} methods (called by
 * the HistoryManager facade) still dispatch internally. New code should
 * prefer the {@code *Event} factory methods and dispatch explicitly.
 *
 * <h2>Compositional descriptors + structured metadata</h2>
 * <p>Every event built here carries:
 * <ul>
 *   <li><b>{@link ActionDescriptors}</b> — intent, cost, beneficiary, risk,
 *       visibility. Compositional, not enum-ossified. Subscribers infer
 *       "this qualifies as mercy" from HELP+HIGH+OTHER+HIGH, rather than
 *       reading a hardcoded ACT_OF_MERCY tag.</li>
 *   <li><b>Metadata Map</b> — structured payload. e.g. for a gift:
 *       {@code {"item"="Spirit Grass", "quality"="MEDIUM", "quantity"="1"}}.
 *       Subscribers read from this instead of parsing the description string.
 *       No string parsing ever again.</li>
 * </ul>
 *
 * <h2>Actor IDs</h2>
 * <ul>
 *   <li>Players: UUID string</li>
 *   <li>NPCs: canon ID (e.g. "wang_lin", "old_chen")</li>
 *   <li>Environmental: "" (empty)</li>
 * </ul>
 */
public final class SimulationActions {

    private SimulationActions() {}

    // ═══════════════════════════════════════════════════════════════════
    //  Player action EVENTS — factory methods returning WorldEvent
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Build a player-interaction event. The player is the source actor.
     */
    public static WorldEvent interactionEvent(ServerPlayer player, String npcCanonId,
                                               String interactionType, String detail, long tick) {
        String desc = player.getName().getString() + " interacted with " + npcCanonId
                + " (" + interactionType + "): " + detail;
        ActionDescriptors desc_ = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.NEUTRAL)
                .cost(ActionDescriptors.Cost.NONE)
                .beneficiary(ActionDescriptors.Beneficiary.OTHER)
                .risk(ActionDescriptors.Risk.NONE)
                .visibility(ActionDescriptors.Visibility.LOCAL)
                .build();
        Map<String,String> meta = Map.of(
                "interaction_type", interactionType,
                "detail", detail);
        return buildPlayerEvent(SemanticEventTopics.PLAYER_INTERACTION, EnergyType.SOCIAL,
                player, npcCanonId, SemanticTag.INTERACTION,
                0.25f, 0.25f, desc, tick, desc_, meta);
    }

    /**
     * Build a player-gave-gift event.
     */
    public static WorldEvent giftGivenEvent(ServerPlayer player, String npcCanonId,
                                             String itemName, String quality, int quantity, long tick) {
        String desc = player.getName().getString() + " gave " + itemName
                + " to " + npcCanonId + ".";
        ActionDescriptors.Cost cost = mapQualityToCost(quality);
        ActionDescriptors desc_ = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.HELP)
                .cost(cost)
                .beneficiary(ActionDescriptors.Beneficiary.OTHER)
                .risk(ActionDescriptors.Risk.NONE)
                .visibility(ActionDescriptors.Visibility.PRIVATE)
                .build();
        Map<String,String> meta = Map.of(
                "item", itemName,
                "quality", quality != null ? quality : "UNKNOWN",
                "quantity", String.valueOf(quantity));
        return buildPlayerEvent(SemanticEventTopics.PLAYER_GIFT_GIVEN, EnergyType.SOCIAL,
                player, npcCanonId, SemanticTag.GIFT_GIVEN,
                0.4f, 0.4f, desc, tick, desc_, meta);
    }

    /**
     * Build a player-received-gift event. The NPC is the source; the player
     * is the target.
     */
    public static WorldEvent giftReceivedEvent(ServerPlayer player, String npcCanonId,
                                                String itemName, String quality, int quantity, long tick) {
        String desc = npcCanonId + " gave " + itemName
                + " to " + player.getName().getString() + ".";
        ActionDescriptors.Cost cost = mapQualityToCost(quality);
        ActionDescriptors desc_ = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.HELP)
                .cost(cost)
                .beneficiary(ActionDescriptors.Beneficiary.OTHER)
                .risk(ActionDescriptors.Risk.NONE)
                .visibility(ActionDescriptors.Visibility.PRIVATE)
                .build();
        Map<String,String> meta = Map.of(
                "item", itemName,
                "quality", quality != null ? quality : "UNKNOWN",
                "quantity", String.valueOf(quantity),
                "giver", npcCanonId);
        return WorldEvent.of(
                SemanticEventTopics.PLAYER_GIFT_RECEIVED, EnergyType.SOCIAL,
                player.blockPosition(), 0.5f, 0.5f, desc,
                "SIMULATION", tick,
                npcCanonId, player.getStringUUID(),
                SemanticTag.GIFT_RECEIVED.name(), withDescriptors(desc_, meta));
    }

    /**
     * Build a player-combat event.
     */
    public static WorldEvent combatEvent(ServerPlayer player, String npcCanonId,
                                          String npcName, boolean playerWon, long tick) {
        String outcome = playerWon
                ? player.getName().getString() + " killed " + npcName + " (" + npcCanonId + ")."
                : player.getName().getString() + " was defeated by " + npcName + ".";
        float severity = playerWon ? 0.7f : 0.5f;
        ActionDescriptors desc_ = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.HARM)
                .cost(playerWon ? ActionDescriptors.Cost.LOW : ActionDescriptors.Cost.HIGH)
                .beneficiary(ActionDescriptors.Beneficiary.SELF)
                .risk(playerWon ? ActionDescriptors.Risk.LOW : ActionDescriptors.Risk.HIGH)
                .visibility(ActionDescriptors.Visibility.LOCAL)
                .build();
        Map<String,String> meta = Map.of(
                "npc_id", npcCanonId,
                "npc_name", npcName,
                "outcome", playerWon ? "VICTORY" : "DEFEAT");
        return buildPlayerEvent(SemanticEventTopics.PLAYER_COMBAT_ENGAGED, EnergyType.PHYSICAL,
                player, npcCanonId, SemanticTag.COMBAT_ENGAGED,
                0.6f, severity, outcome, tick, desc_, meta);
    }

    /**
     * Build a player-breakthrough event. Also returns the companion
     * cultivation-revealed semantic event.
     */
    public static WorldEvent[] breakthroughEvents(ServerPlayer player, String fromRealm,
                                                   String toRealm, long tick) {
        String desc = player.getName().getString() + " broke through from "
                + fromRealm + " to " + toRealm + ".";
        float severity = isNotableRealm(toRealm) ? 0.8f : 0.5f;
        ActionDescriptors.Visibility vis = isNotableRealm(toRealm)
                ? ActionDescriptors.Visibility.REGIONAL
                : ActionDescriptors.Visibility.LOCAL;
        ActionDescriptors desc_ = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.SELF_GAIN)
                .cost(ActionDescriptors.Cost.HIGH)
                .beneficiary(ActionDescriptors.Beneficiary.SELF)
                .risk(ActionDescriptors.Risk.HIGH)
                .visibility(vis)
                .build();
        Map<String,String> meta = Map.of(
                "from_realm", fromRealm,
                "to_realm", toRealm,
                "notable", String.valueOf(isNotableRealm(toRealm)));

        WorldEvent actionEvent = buildPlayerEvent(SemanticEventTopics.PLAYER_BREAKTHROUGH,
                EnergyType.SPIRITUAL, player, "", SemanticTag.BREAKTHROUGH,
                0.7f, severity, desc, tick, desc_, meta);

        // Companion semantic event: cultivation revealed.
        ActionDescriptors semDesc = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.SELF_GAIN)
                .cost(ActionDescriptors.Cost.NONE)
                .beneficiary(ActionDescriptors.Beneficiary.SELF)
                .risk(ActionDescriptors.Risk.MEDIUM)
                .visibility(vis)
                .build();
        WorldEvent semEvent = WorldEvent.of(
                SemanticEventTopics.SEMANTIC_CULTIVATION_REVEALED, EnergyType.SPIRITUAL,
                player.blockPosition(), 0.7f, severity,
                player.getName().getString() + " revealed " + toRealm
                        + " cultivation through a breakthrough.",
                "SIMULATION", tick,
                player.getStringUUID(), "",
                SemanticTag.CULTIVATION_REVEALED.name(), withDescriptors(semDesc, meta));

        return new WorldEvent[] { actionEvent, semEvent };
    }

    /**
     * Build a player-discovery event.
     */
    public static WorldEvent discoveryEvent(ServerPlayer player, String subject,
                                             String detail, long tick) {
        String desc = player.getName().getString() + " discovered " + subject
                + ": " + detail;
        ActionDescriptors desc_ = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.SELF_GAIN)
                .cost(ActionDescriptors.Cost.LOW)
                .beneficiary(ActionDescriptors.Beneficiary.SELF)
                .risk(ActionDescriptors.Risk.LOW)
                .visibility(ActionDescriptors.Visibility.PRIVATE)
                .build();
        Map<String,String> meta = Map.of(
                "subject", subject,
                "detail", detail);
        return buildPlayerEvent(SemanticEventTopics.PLAYER_DISCOVERY, EnergyType.ACQUIRE,
                player, "", SemanticTag.DISCOVERY,
                0.4f, 0.4f, desc, tick, desc_, meta);
    }

    /**
     * CRON-COMPLETIONIST-7: Build a player-spell-cast event.
     *
     * <p>Per the 2026-07-23 event-sourced pivot: every spirit-artifact activation
     * (flying sword launch, talisman activation, soul bead discharge, etc.) flows
     * through this factory so that the WangLinReasoningEngine, CanonDivergenceRecorder,
     * QiDisturbanceSubscriber, and ChronicleSubscriber can react. The player is a
     * first-class actor — never write to a siloed store directly.
     *
     * @param player        the player casting the spell
     * @param spellName     human-readable name (e.g. "Flying Sword", "Teleport Talisman")
     * @param spellSchool   category tag (e.g. "sword_art", "talisman", "pill", "soul_art")
     * @param qiCost        spiritual qi expended (drives qi-disturbance intensity)
     * @param visibility    how observable the spell was (LOCAL, REGIONAL, PUBLIC)
     * @param tick          server tick
     * @return the constructed WorldEvent (caller dispatches via {@link WorldEventBus#dispatch})
     */
    public static WorldEvent spellCastEvent(ServerPlayer player, String spellName,
                                             String spellSchool, float qiCost,
                                             ActionDescriptors.Visibility visibility,
                                             long tick) {
        String desc = player.getName().getString() + " cast " + spellName
                + " (" + spellSchool + ").";
        float intensity = Math.min(1.0f, 0.3f + qiCost * 0.1f);
        float severity = Math.min(0.85f, 0.4f + qiCost * 0.05f);
        ActionDescriptors desc_ = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.SELF_GAIN)
                .cost(qiCost >= 5.0f ? ActionDescriptors.Cost.HIGH
                        : qiCost >= 2.0f ? ActionDescriptors.Cost.MEDIUM
                        : ActionDescriptors.Cost.LOW)
                .beneficiary(ActionDescriptors.Beneficiary.SELF)
                .risk(qiCost >= 5.0f ? ActionDescriptors.Risk.MEDIUM
                        : ActionDescriptors.Risk.LOW)
                .visibility(visibility)
                .build();
        Map<String,String> meta = new HashMap<>();
        meta.put("spell_name", spellName);
        meta.put("spell_school", spellSchool);
        meta.put("qi_cost", String.valueOf(qiCost));
        return buildPlayerEvent(SemanticEventTopics.PLAYER_SPELL_CAST, EnergyType.QI,
                player, "", SemanticTag.SPELL_CAST,
                intensity, severity, desc, tick, desc_, meta);
    }

    /**
     * CRON-COMPLETIONIST-7: Convenience dispatcher — build AND dispatch a spell-cast event.
     * Use this from item right-click handlers so the wiring is uniform across all artifacts.
     */
    public static void spellCast(ServerPlayer player, String spellName, String spellSchool,
                                  float qiCost, ActionDescriptors.Visibility visibility) {
        WorldEventBus.dispatch(spellCastEvent(player, spellName, spellSchool, qiCost,
                visibility, player.serverLevel().getGameTime()));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Semantic events — the "meaning" layer (factory methods)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Build an act-of-mercy semantic event.
     */
    public static WorldEvent actOfMercyEvent(ServerPlayer actor, String targetActorId,
                                              String detail, long tick) {
        ActionDescriptors desc_ = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.HELP)
                .cost(ActionDescriptors.Cost.HIGH)
                .beneficiary(ActionDescriptors.Beneficiary.OTHER)
                .risk(ActionDescriptors.Risk.HIGH)
                .visibility(ActionDescriptors.Visibility.PUBLIC)
                .build();
        Map<String,String> meta = Map.of("detail", detail);
        return buildPlayerEvent(SemanticEventTopics.SEMANTIC_ACT_OF_MERCY,
                EnergyType.SOCIAL, actor, targetActorId, SemanticTag.ACT_OF_MERCY,
                0.6f, 0.6f,
                actor.getName().getString() + " showed mercy to " + targetActorId
                        + ": " + detail, tick, desc_, meta);
    }

    /**
     * Build an act-of-cruelty semantic event.
     */
    public static WorldEvent actOfCrueltyEvent(ServerPlayer actor, String targetActorId,
                                                String detail, long tick) {
        ActionDescriptors desc_ = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.HARM)
                .cost(ActionDescriptors.Cost.NONE)
                .beneficiary(ActionDescriptors.Beneficiary.NONE)
                .risk(ActionDescriptors.Risk.NONE)
                .visibility(ActionDescriptors.Visibility.PUBLIC)
                .build();
        Map<String,String> meta = Map.of("detail", detail);
        return buildPlayerEvent(SemanticEventTopics.SEMANTIC_ACT_OF_CRUELTY,
                EnergyType.KARMA, actor, targetActorId, SemanticTag.ACT_OF_CRUELTY,
                0.7f, 0.7f,
                actor.getName().getString() + " committed cruelty against " + targetActorId
                        + ": " + detail, tick, desc_, meta);
    }

    /**
     * Build a technique-displayed semantic event.
     */
    public static WorldEvent techniqueDisplayedEvent(ServerPlayer actor, String techniqueName,
                                                      long tick) {
        ActionDescriptors desc_ = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.SELF_GAIN)
                .cost(ActionDescriptors.Cost.NONE)
                .beneficiary(ActionDescriptors.Beneficiary.SELF)
                .risk(ActionDescriptors.Risk.MEDIUM)
                .visibility(ActionDescriptors.Visibility.REGIONAL)
                .build();
        Map<String,String> meta = Map.of("technique", techniqueName);
        return buildPlayerEvent(SemanticEventTopics.SEMANTIC_TECHNIQUE_DISPLAYED,
                EnergyType.SPIRITUAL, actor, "", SemanticTag.TECHNIQUE_DISPLAYED,
                0.6f, 0.6f,
                actor.getName().getString() + " displayed technique: " + techniqueName,
                tick, desc_, meta);
    }

    /**
     * Build a promise-made semantic event.
     */
    public static WorldEvent promiseMadeEvent(ServerPlayer actor, String targetActorId,
                                               String promiseDetail, long tick) {
        ActionDescriptors desc_ = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.HELP)
                .cost(ActionDescriptors.Cost.NONE)
                .beneficiary(ActionDescriptors.Beneficiary.OTHER)
                .risk(ActionDescriptors.Risk.MEDIUM)
                .visibility(ActionDescriptors.Visibility.PRIVATE)
                .build();
        Map<String,String> meta = Map.of("promise", promiseDetail);
        return buildPlayerEvent(SemanticEventTopics.SEMANTIC_PROMISE_MADE,
                EnergyType.SOCIAL, actor, targetActorId, SemanticTag.PROMISE_MADE,
                0.5f, 0.5f,
                actor.getName().getString() + " made a promise to " + targetActorId
                        + ": " + promiseDetail, tick, desc_, meta);
    }

    /**
     * Build a promise-broken semantic event.
     */
    public static WorldEvent promiseBrokenEvent(ServerPlayer actor, String targetActorId,
                                                 String promiseDetail, long tick) {
        ActionDescriptors desc_ = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.HARM)
                .cost(ActionDescriptors.Cost.NONE)
                .beneficiary(ActionDescriptors.Beneficiary.SELF)
                .risk(ActionDescriptors.Risk.MEDIUM)
                .visibility(ActionDescriptors.Visibility.LOCAL)
                .build();
        Map<String,String> meta = Map.of("promise", promiseDetail);
        return buildPlayerEvent(SemanticEventTopics.SEMANTIC_PROMISE_BROKEN,
                EnergyType.KARMA, actor, targetActorId, SemanticTag.PROMISE_BROKEN,
                0.7f, 0.7f,
                actor.getName().getString() + " broke a promise to " + targetActorId
                        + ": " + promiseDetail, tick, desc_, meta);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  NPC action EVENTS — same factory pattern, NPC is the source
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Build an NPC-gave-gift event. e.g. Wang Lin gives Li Muwan a flower.
     */
    public static WorldEvent actorGaveGiftEvent(String sourceNpcCanonId, String targetActorId,
                                                 String itemName, String quality, int quantity,
                                                 BlockPos pos, long tick) {
        String desc = sourceNpcCanonId + " gave " + itemName
                + " to " + targetActorId + ".";
        ActionDescriptors desc_ = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.HELP)
                .cost(mapQualityToCost(quality))
                .beneficiary(ActionDescriptors.Beneficiary.OTHER)
                .risk(ActionDescriptors.Risk.NONE)
                .visibility(ActionDescriptors.Visibility.PRIVATE)
                .build();
        Map<String,String> meta = Map.of(
                "item", itemName,
                "quality", quality != null ? quality : "UNKNOWN",
                "quantity", String.valueOf(quantity));
        return WorldEvent.of(
                SemanticEventTopics.ACTOR_GIFT_GIVEN, EnergyType.SOCIAL,
                pos, 0.4f, 0.4f, desc, "SIMULATION", tick,
                sourceNpcCanonId, targetActorId,
                SemanticTag.GIFT_GIVEN.name(), withDescriptors(desc_, meta));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Backward-compatible dispatch methods (used by HistoryManager facade)
    //  New code should prefer the *Event factory methods + explicit dispatch.
    // ═══════════════════════════════════════════════════════════════════

    public static void playerInteractedWithNpc(ServerPlayer player, String npcCanonId,
                                                String interactionType, String detail, long tick) {
        WorldEventBus.dispatch(interactionEvent(player, npcCanonId, interactionType, detail, tick));
    }

    public static void playerGaveGift(ServerPlayer player, String npcCanonId,
                                       String itemName, long tick) {
        WorldEventBus.dispatch(giftGivenEvent(player, npcCanonId, itemName, "UNKNOWN", 1, tick));
    }

    public static void playerReceivedGift(ServerPlayer player, String npcCanonId,
                                           String itemName, long tick) {
        WorldEventBus.dispatch(giftReceivedEvent(player, npcCanonId, itemName, "UNKNOWN", 1, tick));
    }

    public static void playerEngagedCombat(ServerPlayer player, String npcCanonId,
                                            String npcName, boolean playerWon, long tick) {
        WorldEventBus.dispatch(combatEvent(player, npcCanonId, npcName, playerWon, tick));
    }

    public static void playerBreakthrough(ServerPlayer player, String fromRealm,
                                           String toRealm, long tick) {
        for (WorldEvent e : breakthroughEvents(player, fromRealm, toRealm, tick)) {
            WorldEventBus.dispatch(e);
        }
    }

    public static void playerDiscovered(ServerPlayer player, String subject,
                                         String detail, long tick) {
        WorldEventBus.dispatch(discoveryEvent(player, subject, detail, tick));
    }

    public static void actOfMercy(ServerPlayer actor, String targetActorId,
                                   String detail, long tick) {
        WorldEventBus.dispatch(actOfMercyEvent(actor, targetActorId, detail, tick));
    }

    public static void actOfCruelty(ServerPlayer actor, String targetActorId,
                                     String detail, long tick) {
        WorldEventBus.dispatch(actOfCrueltyEvent(actor, targetActorId, detail, tick));
    }

    public static void techniqueDisplayed(ServerPlayer actor, String techniqueName, long tick) {
        WorldEventBus.dispatch(techniqueDisplayedEvent(actor, techniqueName, tick));
    }

    public static void promiseMade(ServerPlayer actor, String targetActorId,
                                    String promiseDetail, long tick) {
        WorldEventBus.dispatch(promiseMadeEvent(actor, targetActorId, promiseDetail, tick));
    }

    public static void promiseBroken(ServerPlayer actor, String targetActorId,
                                     String promiseDetail, long tick) {
        WorldEventBus.dispatch(promiseBrokenEvent(actor, targetActorId, promiseDetail, tick));
    }

    public static void actorGaveGift(String sourceNpcCanonId, String targetActorId,
                                      String itemName, BlockPos pos, long tick) {
        WorldEventBus.dispatch(actorGaveGiftEvent(sourceNpcCanonId, targetActorId,
                itemName, "UNKNOWN", 1, pos, tick));
    }

    public static void actorEngagedCombat(String sourceNpcCanonId, String targetActorId,
                                           BlockPos pos, long tick) {
        String desc = sourceNpcCanonId + " engaged in combat with " + targetActorId + ".";
        ActionDescriptors desc_ = ActionDescriptors.builder()
                .intent(ActionDescriptors.Intent.HARM)
                .cost(ActionDescriptors.Cost.MEDIUM)
                .beneficiary(ActionDescriptors.Beneficiary.SELF)
                .risk(ActionDescriptors.Risk.HIGH)
                .visibility(ActionDescriptors.Visibility.LOCAL)
                .build();
        WorldEvent event = WorldEvent.of(
                SemanticEventTopics.ACTOR_COMBAT_ENGAGED, EnergyType.PHYSICAL,
                pos, 0.6f, 0.6f, desc, "SIMULATION", tick,
                sourceNpcCanonId, targetActorId,
                SemanticTag.COMBAT_ENGAGED.name(), withDescriptors(desc_, Map.of()));
        WorldEventBus.dispatch(event);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Internal helpers
    // ═══════════════════════════════════════════════════════════════════

    private static WorldEvent buildPlayerEvent(String topic, EnergyType energy,
                                                ServerPlayer player, String targetActorId,
                                                SemanticTag semanticTag,
                                                float intensity, float severity,
                                                String desc, long tick,
                                                ActionDescriptors descriptors,
                                                Map<String,String> metadata) {
        return WorldEvent.of(
                topic, energy, player.blockPosition(),
                intensity, severity, desc, "SIMULATION", tick,
                player.getStringUUID(), targetActorId,
                semanticTag.name(), withDescriptors(descriptors, metadata));
    }

    /**
     * Merge ActionDescriptors fields into the metadata map so subscribers
     * can reconstruct the descriptors from the WorldEvent's metadata.
     */
    private static Map<String, String> withDescriptors(ActionDescriptors desc,
                                                      Map<String, String> meta) {
        Map<String, String> merged = new HashMap<>(meta);
        merged.put("_desc_intent", desc.intent().name());
        merged.put("_desc_cost", desc.cost().name());
        merged.put("_desc_beneficiary", desc.beneficiary().name());
        merged.put("_desc_risk", desc.risk().name());
        merged.put("_desc_visibility", desc.visibility().name());
        return merged;
    }

    private static ActionDescriptors.Cost mapQualityToCost(String quality) {
        if (quality == null) return ActionDescriptors.Cost.LOW;
        return switch (quality.toUpperCase()) {
            case "NONE", "UNKNOWN" -> ActionDescriptors.Cost.NONE;
            case "LOW", "COMMON" -> ActionDescriptors.Cost.LOW;
            case "MEDIUM", "UNCOMMON" -> ActionDescriptors.Cost.MEDIUM;
            case "HIGH", "RARE" -> ActionDescriptors.Cost.HIGH;
            case "EXTREME", "LEGENDARY", "DIVINE" -> ActionDescriptors.Cost.EXTREME;
            default -> ActionDescriptors.Cost.LOW;
        };
    }

    private static boolean isNotableRealm(String realmName) {
        if (realmName == null) return false;
        String lower = realmName.toLowerCase();
        return lower.contains("nascent") || lower.contains("soul formation")
                || lower.contains("ascendant") || lower.contains("transcendence")
                || lower.contains("true immortal") || lower.contains("paragon");
    }
}
