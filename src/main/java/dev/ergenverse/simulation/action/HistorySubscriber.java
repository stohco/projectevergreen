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
 * HistorySubscriber — records bus events into the history stores
 * (PlayerHistory, NpcMemory, WorldHistory).
 *
 * <p>Per user directive (2026-07-23 #2):
 * "The world should only know about events. Not who published them."
 * This subscriber doesn't care WHO published the event — it reacts to
 * any event with the right topic and records it to the appropriate
 * history store.
 *
 * <p><b>Structured metadata (no more string parsing):</b>
 * This subscriber reads the event's metadata map directly instead of
 * parsing the description string. For example:
 * <ul>
 *   <li>Gift event: reads {@code event.meta("item")} instead of
 *       parsing "X gave Y to Z."</li>
 *   <li>Combat event: reads {@code event.meta("outcome")} and
 *       {@code event.meta("npc_name")} instead of parsing the description.</li>
 *   <li>Breakthrough event: reads {@code event.meta("from_realm")} and
 *       {@code event.meta("to_realm")}.</li>
 * </ul>
 */
public final class HistorySubscriber implements WorldEventSubscriber {

    @Override
    public String topicPrefix() {
        return ""; // filter inside onEvent
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;

        // Only handle action events (not semantic, not environmental).
        if (!SemanticEventTopics.isActionTopic(event.topic())) return;

        ServerPlayer player = resolvePlayer(event);
        if (player == null) return;

        ServerLevel level = WorldEventBus.currentLevel();
        if (level == null) return;

        long tick = event.timestamp();
        String topic = event.topic();

        try {
            switch (topic) {
                case SemanticEventTopics.PLAYER_INTERACTION -> {
                    String npcCanonId = event.meta("npc_canon_id",
                            event.targetActorId());
                    if (!npcCanonId.isEmpty()) {
                        NpcMemory.recordInteraction(player, npcCanonId,
                                event.description(), tick);
                    }
                }

                case SemanticEventTopics.PLAYER_GIFT_GIVEN -> {
                    String npcCanonId = event.targetActorId();
                    String itemName = event.meta("item", "unknown item");
                    if (!npcCanonId.isEmpty()) {
                        NpcMemory.recordInteraction(player, npcCanonId,
                                "GIFT_GIVEN: " + itemName, tick);
                    }
                }

                case SemanticEventTopics.PLAYER_GIFT_RECEIVED -> {
                    String npcCanonId = event.meta("giver",
                            event.sourceActorId());
                    String itemName = event.meta("item", "unknown item");
                    PlayerHistory.recordGiftReceived(player, itemName, npcCanonId, tick);
                    NpcMemory.recordInteraction(player, npcCanonId,
                            "GIFT_RECEIVED: " + itemName, tick);

                    WorldHistory.recordGlobal(level, tick,
                            "PLAYER_ACTION", "planet_suzaku",
                            player.getName().getString() + " received " + itemName
                                    + " from " + npcCanonId + ".",
                            "gift:" + itemName);
                }

                case SemanticEventTopics.PLAYER_COMBAT_ENGAGED -> {
                    String npcCanonId = event.targetActorId();
                    String npcName = event.meta("npc_name",
                            event.meta("npc_canon_id", "unknown"));
                    boolean playerWon = "VICTORY".equals(
                            event.meta("outcome", ""));

                    NpcMemory.recordCombat(player, npcCanonId,
                            event.description(), playerWon, tick);

                    if (playerWon) {
                        PlayerHistory.recordKill(player, npcName, "combat", tick);
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
                    String toRealm = event.meta("to_realm", "unknown");
                    String fromRealm = event.meta("from_realm", "unknown");
                    PlayerHistory.recordBreakthrough(player, fromRealm, toRealm, tick);

                    if (isNotableRealm(toRealm)) {
                        WorldHistory.recordGlobal(level, tick,
                                "PLAYER_ACTION", "planet_suzaku",
                                player.getName().getString() + " achieved " + toRealm
                                        + " — detectable across the region.",
                                "player_breakthrough:" + toRealm);
                    }
                }

                case SemanticEventTopics.PLAYER_DISCOVERY -> {
                    String subject = event.meta("subject", "unknown");
                    PlayerHistory.recordDiscovery(player, subject, event.description(), tick);
                }

                default -> {
                    // Actor-sourced events: for now, logged at debug.
                    Ergenverse.LOGGER.debug("[HistorySubscriber] Skipping actor event: {}", topic);
                }
            }
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[HistorySubscriber] failed to record event {}",
                    event.topic(), e);
        }
    }

    private ServerPlayer resolvePlayer(WorldEvent event) {
        ServerLevel level = WorldEventBus.currentLevel();
        if (level == null) return null;

        ServerPlayer sp = tryResolvePlayer(level, event.sourceActorId());
        if (sp != null) return sp;
        return tryResolvePlayer(level, event.targetActorId());
    }

    private ServerPlayer tryResolvePlayer(ServerLevel level, String actorId) {
        if (actorId == null || actorId.isEmpty()) return null;
        try {
            UUID uuid = UUID.fromString(actorId);
            var entity = level.getEntity(uuid);
            if (entity instanceof ServerPlayer sp) return sp;
        } catch (IllegalArgumentException ignored) {
            // Not a UUID.
        }
        return null;
    }

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
