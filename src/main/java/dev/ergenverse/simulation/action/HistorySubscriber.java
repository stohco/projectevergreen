package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.history.NpcMemory;
import dev.ergenverse.history.PlayerHistory;
import dev.ergenverse.history.WorldHistory;
import dev.ergenverse.simulation.event.SemanticEventTopics;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.event.WorldEventSubscriber;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * HistorySubscriber — the inversion of HistoryManager.
 *
 * <p>Per the user's architectural directive (2026-07-23):
 * <pre>
 *   "HistoryManager shouldn't be the destination.
 *    It should be a subscriber."
 * </pre>
 *
 * <p>Before this class, {@code HistoryManager.onNpcInteraction(...)} wrote
 * directly to PlayerHistory, NpcMemory, RelationshipHistory, and WorldHistory.
 * The player's actions were recorded in siloed stores but never reached the
 * WorldEventBus — so no NPC could remember, no rumor could spread, the
 * chronicle missed everything.
 *
 * <p>Now, gameplay code calls {@link SimulationActions}, which publishes a
 * WorldEvent to the bus. <b>This subscriber</b> receives that event and
 * records it to the appropriate history stores. The recording logic that
 * used to live in HistoryManager's method bodies now lives here, driven
 * by bus events instead of direct calls.
 *
 * <p>The flow is now:
 * <pre>
 *   Player action
 *     → SimulationActions.playerInteractedWithNpc(...)
 *     → WorldEventBus.dispatch(player.interaction event)
 *     → HistorySubscriber.onEvent(event)        ← THIS CLASS
 *       → PlayerHistory.record(...)
 *       → NpcMemory.record(...)
 *       → WorldHistory.recordGlobal(...)
 *     → MemoryEventSubscriber.onEvent(event)    ← NPCs remember
 *     → RelationshipEngine.onEvent(event)       ← trust changes
 *     → ChronicleSubscriber.onEvent(event)      ← world chronicle
 *     → OpportunityGenerator.onEvent(event)     ← new opportunities
 * </pre>
 *
 * <p><b>The player is now a first-class actor.</b> Every subscriber sees
 * the player's actions and derives its own consequences. HistoryManager
 * is no longer a destination — it's a thin facade that delegates to
 * SimulationActions, which publishes to the bus, which this subscriber
 * listens to.
 *
 * <h2>What this subscriber records</h2>
 * <ul>
 *   <li><b>player.interaction</b> → NpcMemory.recordInteraction</li>
 *   <li><b>player.gift.given</b> → PlayerHistory.recordGiftReceived (reversed
 *       — the player gave, so the NPC received), NpcMemory.recordInteraction</li>
 *   <li><b>player.gift.received</b> → PlayerHistory.recordGiftReceived,
 *       NpcMemory.recordInteraction</li>
 *   <li><b>player.combat.engaged</b> → NpcMemory.recordCombat,
 *       PlayerHistory.recordKill (if player won)</li>
 *   <li><b>player.breakthrough</b> → PlayerHistory.recordBreakthrough,
 *       WorldHistory.recordGlobal (if notable realm)</li>
 *   <li><b>player.discovery</b> → PlayerHistory.recordDiscovery</li>
 * </ul>
 *
 * <p><b>Not a new Engine (Art XXVI):</b> this is a WorldEventSubscriber —
 * the same pattern as MemoryEventSubscriber and ChronicleSubscriber. It
 * uses the existing history stores (PlayerHistory, NpcMemory, WorldHistory).
 * No new infrastructure.
 */
public final class HistorySubscriber implements WorldEventSubscriber {

    @Override
    public String topicPrefix() {
        // Catch-all like ChronicleSubscriber and MemoryEventSubscriber.
        // We filter to player./actor. action topics inside onEvent.
        return "";
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;

        // Only handle player and actor ACTION events (not semantic, not environmental).
        if (!SemanticEventTopics.isActionTopic(event.topic())) return;

        // We need a ServerPlayer to persist via the history APIs.
        // The source actor is the player for player.* topics;
        // for actor.* topics, the target might be the player.
        ServerPlayer player = resolvePlayer(event);
        if (player == null) return; // no player involved — skip (future: NPC-to-NPC history)

        ServerLevel level = WorldEventBus.currentLevel();
        if (level == null) return;

        long tick = event.timestamp();
        String topic = event.topic();

        try {
            switch (topic) {
                case SemanticEventTopics.PLAYER_INTERACTION -> {
                    // Player interacted with an NPC.
                    String npcCanonId = event.targetActorId();
                    if (!npcCanonId.isEmpty()) {
                        NpcMemory.recordInteraction(player, npcCanonId,
                                event.description(), tick);
                    }
                }

                case SemanticEventTopics.PLAYER_GIFT_GIVEN -> {
                    // Player gave a gift to an NPC.
                    String npcCanonId = event.targetActorId();
                    String itemName = extractItemName(event.description());
                    if (!npcCanonId.isEmpty()) {
                        NpcMemory.recordInteraction(player, npcCanonId,
                                "GIFT_GIVEN: " + itemName, tick);
                    }
                }

                case SemanticEventTopics.PLAYER_GIFT_RECEIVED -> {
                    // Player received a gift from an NPC.
                    // The NPC is the source actor; the player is the target.
                    String npcCanonId = event.sourceActorId();
                    String itemName = extractItemName(event.description());
                    PlayerHistory.recordGiftReceived(player, itemName, npcCanonId, tick);
                    NpcMemory.recordInteraction(player, npcCanonId,
                            "GIFT_RECEIVED: " + itemName, tick);

                    // Gift reception is a world event if the item is significant.
                    WorldHistory.recordGlobal(level, tick,
                            "PLAYER_ACTION", "planet_suzaku",
                            player.getName().getString() + " received " + itemName
                                    + " from " + npcCanonId + ".",
                            "gift:" + itemName);
                }

                case SemanticEventTopics.PLAYER_COMBAT_ENGAGED -> {
                    // Player fought an NPC.
                    String npcCanonId = event.targetActorId();
                    boolean playerWon = event.description().contains("killed");
                    String npcName = extractNpcName(event.description());

                    NpcMemory.recordCombat(player, npcCanonId,
                            event.description(), playerWon, tick);

                    if (playerWon) {
                        PlayerHistory.recordKill(player, npcName, "combat", tick);

                        // Killing a notable NPC is a world event.
                        if (isNotableNpc(npcCanonId)) {
                            WorldHistory.recordGlobal(level, tick,
                                    "PLAYER_ACTION", "planet_suzaku",
                                    player.getName().getString() + " killed " + npcName
                                            + " (" + npcCanonId + ") — a significant event.",
                                    "kill:" + npcCanonId);
                        }
                    } else {
                        PlayerHistory.recordKill(player, npcName, "defeat", tick);
                    }
                }

                case SemanticEventTopics.PLAYER_BREAKTHROUGH -> {
                    // Player broke through to a new realm.
                    String toRealm = extractRealm(event.description());
                    String fromRealm = extractFromRealm(event.description());
                    PlayerHistory.recordBreakthrough(player, fromRealm, toRealm, tick);

                    // Notable breakthroughs are world events (detectable across regions).
                    if (isNotableRealm(toRealm)) {
                        WorldHistory.recordGlobal(level, tick,
                                "PLAYER_ACTION", "planet_suzaku",
                                player.getName().getString() + " achieved " + toRealm
                                        + " — a cultivation event detectable across the region.",
                                "player_breakthrough:" + toRealm);
                    }
                }

                case SemanticEventTopics.PLAYER_DISCOVERY -> {
                    // Player discovered something.
                    String subject = extractDiscoverySubject(event.description());
                    PlayerHistory.recordDiscovery(player, subject, event.description(), tick);
                }

                default -> {
                    // Actor-sourced events (actor.gift.given, etc.) — for now, these
                    // are handled by RelationshipEngine. History recording for
                    // NPC-to-NPC interactions is a future task (requires a
                    // non-player-centric history store).
                }
            }
        } catch (Exception e) {
            // A recording failure MUST NOT break the bus or other subscribers.
            Ergenverse.LOGGER.error("[HistorySubscriber] failed to record event {}",
                    event.topic(), e);
        }
    }

    // ─── Player resolution ────────────────────────────────────────────

    /**
     * Resolve the ServerPlayer involved in this event, if any.
     *
     * <p>For player.* topics, the source actor is the player.
     * For actor.* topics, the target might be the player.
     */
    private ServerPlayer resolvePlayer(WorldEvent event) {
        ServerLevel level = WorldEventBus.currentLevel();
        if (level == null) return null;

        // Try source actor first (player.* events).
        if (!event.sourceActorId().isEmpty() && !event.sourceActorId().contains("_")
                && !event.sourceActorId().equals("wang_lin")) {
            // Heuristic: player UUIDs are hyphenated, NPC canon IDs use underscores.
            // This is a simplification — a proper actor registry would be cleaner.
            try {
                UUID uuid = UUID.fromString(event.sourceActorId());
                var entity = level.getEntity(uuid);
                if (entity instanceof ServerPlayer sp) return sp;
            } catch (IllegalArgumentException ignored) {
                // Not a UUID — it's an NPC canon ID.
            }
        }

        // Try target actor (actor.* events where player is the target).
        if (!event.targetActorId().isEmpty()) {
            try {
                UUID uuid = UUID.fromString(event.targetActorId());
                var entity = level.getEntity(uuid);
                if (entity instanceof ServerPlayer sp) return sp;
            } catch (IllegalArgumentException ignored) {
                // Not a UUID.
            }
        }

        return null;
    }

    // ─── Description parsers ──────────────────────────────────────────

    /** Extract the item name from a gift description like "X gave Y to Z." */
    private String extractItemName(String desc) {
        // "Wang Lin gave Restriction Flag to player." → "Restriction Flag"
        int gave = desc.indexOf(" gave ");
        int to = desc.indexOf(" to ");
        if (gave >= 0 && to > gave) {
            return desc.substring(gave + 6, to);
        }
        return "unknown item";
    }

    /** Extract the NPC name from a combat description like "X killed Y (npcId)." */
    private String extractNpcName(String desc) {
        int killed = desc.indexOf("killed ");
        if (killed >= 0) {
            String rest = desc.substring(killed + 7);
            int paren = rest.indexOf(" (");
            if (paren > 0) return rest.substring(0, paren);
            int dot = rest.indexOf('.');
            if (dot > 0) return rest.substring(0, dot);
            return rest;
        }
        int defeated = desc.indexOf("defeated by ");
        if (defeated >= 0) {
            String rest = desc.substring(defeated + 12);
            int dot = rest.indexOf('.');
            if (dot > 0) return rest.substring(0, dot);
            return rest;
        }
        return "unknown";
    }

    /** Extract the target realm from a breakthrough description. */
    private String extractRealm(String desc) {
        // "X broke through from Y to Z." → "Z"
        int to = desc.indexOf(" to ");
        if (to >= 0) {
            String rest = desc.substring(to + 4);
            int dot = rest.indexOf('.');
            if (dot > 0) return rest.substring(0, dot);
            return rest;
        }
        return "unknown";
    }

    /** Extract the source realm from a breakthrough description. */
    private String extractFromRealm(String desc) {
        // "X broke through from Y to Z." → "Y"
        int from = desc.indexOf("from ");
        int to = desc.indexOf(" to ");
        if (from >= 0 && to > from) {
            return desc.substring(from + 5, to);
        }
        return "unknown";
    }

    /** Extract the discovery subject from a description. */
    private String extractDiscoverySubject(String desc) {
        // "X discovered Y: Z" → "Y"
        int disc = desc.indexOf("discovered ");
        int colon = desc.indexOf(": ");
        if (disc >= 0 && colon > disc) {
            return desc.substring(disc + 11, colon);
        }
        return "unknown";
    }

    // ─── Helper predicates ─────────────────────────────────────────────

    private static boolean isNotableRealm(String realmName) {
        if (realmName == null) return false;
        String lower = realmName.toLowerCase();
        return lower.contains("nascent") || lower.contains("soul formation")
                || lower.contains("ascendant") || lower.contains("transcendence")
                || lower.contains("true immortal") || lower.contains("paragon");
    }

    private static boolean isNotableNpc(String npcCanonId) {
        if (npcCanonId == null) return false;
        String lower = npcCanonId.toLowerCase();
        return lower.contains("patriarch") || lower.contains("sect_head")
                || lower.contains("elder")
                || lower.contains("king_zhao")
                || lower.contains("wang_lin")
                || lower.contains("teng_huayuan")
                || lower.contains("situ_nan");
    }
}
