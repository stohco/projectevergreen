package dev.ergenverse.history;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.entity.EntityCultivator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * HistoryManager — the cross-system wiring layer for Layer 3 (Emergent History).
 *
 * <p>Provides static hook methods that other game systems call when
 * significant events happen. Each method writes to the appropriate
 * history system (PlayerHistory, NpcMemory, RelationshipHistory,
 * WorldHistory) and persists everything via WorldRuntimeState.
 *
 * <h2>Wiring points</h2>
 * <ul>
 *   <li>{@link #onBreakthrough} — called from CultivationEvents when
 *       the player achieves a new realm.</li>
 *   *   <li>{@link #onNpcInteraction} — called from PerceptionBlockEvents or
 *       EntityCultivator right-click when the player interacts with an NPC.</li>
 *   *   <li>{@link #onGiftReceived} — called from ManifestationGiftHandler
 *       when the manifestation grants an item.</li>
 *   <li>{@link #onNpcCombat} — called when the player damages/kills
 *       an EntityCultivator.</li>
 * </ul>
 *
 * <p>The tick integration (WorldHistory advancing canon consequences over time)
 * is handled directly in Ergenverse.onServerTick via WorldStateEngine.
 * This class is purely event-driven — it records what happened, it does
 * not tick.
 */
public final class HistoryManager {

    private HistoryManager() {}

    // ─── Cultivation Events ──────────────────────────────────────────

    /**
     * Called when the player successfully breaks through to a new realm.
     * Records in PlayerHistory.
     */
    public static void onBreakthrough(ServerPlayer player,
                                        String fromRealmName,
                                        String toRealmName,
                                        long worldTick) {
        PlayerHistory.recordBreakthrough(player, fromRealmName, toRealmName, worldTick);

        // Breakthroughs are visible in the world — record in WorldHistory too
        // if the player is at a notable realm
        if (isNotableRealm(toRealmName)) {
            String playerName = player.getName().getString();
            WorldHistory.recordGlobal(player.serverLevel(), worldTick,
                    "PLAYER_ACTION", "planet_suzaku",
                    playerName + " achieved " + toRealmName +
                    " — a cultivation event detectable across the region.",
                    "player_breakthrough:" + toRealmName);
        }
    }

    // ─── NPC Interaction ──────────────────────────────────────────────

    /**
     * Called when the player right-clicks or otherwise interacts with
     * an EntityCultivator NPC.
     */
    public static void onNpcInteraction(ServerPlayer player,
                                             String npcCanonId,
                                             String interactionType,
                                             String detail,
                                             long worldTick) {
        // Record in NpcMemory
        NpcMemory.recordInteraction(player, npcCanonId,
                interactionType + ": " + detail, worldTick);

        // If the NPC is a manifestation companion, also record in RelationshipHistory
        if (isManifestation(npcCanonId)) {
            RelationshipHistory.recordAffinityEarned(player, npcCanonId,
                    detail, 1, worldTick);
        }
    }

    /**
     * Called when the player receives a gift from a manifestation NPC.
     * Records in PlayerHistory, RelationshipHistory, and NpcMemory.
     */
    public static void onGiftReceived(ServerPlayer player,
                                          String protagonistId,
                                          String itemName,
                                          long worldTick) {
        PlayerHistory.recordGiftReceived(player, itemName, protagonistId, worldTick);
        RelationshipHistory.recordGiftReceived(player, protagonistId, itemName, 5, worldTick);
        NpcMemory.recordInteraction(player, protagonistId,
                "GIFT_RECEIVED: " + itemName, worldTick);

        // Gift reception is a world event (if the item is significant)
        WorldHistory.recordGlobal(player.serverLevel(), worldTick,
                "PLAYER_ACTION", "planet_suzaku",
                player.getName().getString() + " received " + itemName +
                " from a manifestation companion.",
                "gift:" + itemName);
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
        String outcome = playerWon
                ? "Killed " + npcName + "."
                : "Defeated by " + npcName + ".";

        NpcMemory.recordCombat(player, npcCanonId, outcome, playerWon, worldTick);
        PlayerHistory.recordKill(player, npcName,
                playerWon ? "combat" : "defeat", worldTick);

        // Killing an NPC is a world event (especially if they're notable)
        if (playerWon && isNotableNpc(npcCanonId)) {
            WorldHistory.recordGlobal(player.serverLevel(), worldTick,
                    "PLAYER_ACTION", "planet_suzaku",
                    player.getName().getString() + " killed " + npcName +
                    " (" + npcCanonId + ") — a significant event.",
                    "kill:" + npcCanonId);
        }
    }

    // ─── Discovery ────────────────────────────────────────────────────

    /**
     * Called when the player discovers something noteworthy.
     */
    public static void onDiscovery(ServerPlayer player,
                                       String subject,
                                       String detail,
                                       long worldTick) {
        PlayerHistory.recordDiscovery(player, subject, detail, worldTick);
    }

    // ─── Helper predicates ─────────────────────────────────────────────

    /** Realms that are notable enough to record as world-history events. */
    private static boolean isNotableRealm(String realmName) {
        if (realmName == null) return false;
        String lower = realmName.toLowerCase();
        // Nascent Soul and above are detectable across regions
        return lower.contains("nascent") || lower.contains("soul formation")
                || lower.contains("ascendant") || lower.contains("transcendence")
                || lower.contains("true immortal") || lower.contains("paragon");
    }

    /** Check if an NPC is a manifestation companion (Wang Lin's manifestation, etc.). */
    private static boolean isManifestation(String npcCanonId) {
        if (npcCanonId == null) return false;
        return npcCanonId.contains("manifestation") || npcCanonId.equals("wang_lin_manifestation");
    }

    /** Check if an NPC is a notable canon character whose death would be world-news. */
    private static boolean isNotableNpc(String npcCanonId) {
        if (npcCanonId == null) return false;
        String lower = npcCanonId.toLowerCase();
        // Sect heads, patriarchs, country rulers, Wang Lin
        return lower.contains("patriarch") || lower.contains("sect_head")
                || lower.contains("elder")
                || lower.contains("king_zhao")
                || lower.contains("wang_lin")
                || lower.contains("teng_huayuan")
                || lower.contains("situ_nan");
    }
}