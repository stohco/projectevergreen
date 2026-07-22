package dev.ergenverse.history;

import dev.ergenverse.simulation.action.SimulationActions;
import net.minecraft.server.level.ServerPlayer;

/**
 * HistoryManager — now a <b>thin facade</b> that delegates to
 * {@link SimulationActions}.
 *
 * <p><b>Architectural pivot (2026-07-23):</b>
 * The user's directive:
 * <pre>
 *   "HistoryManager shouldn't be the destination.
 *    It should be a subscriber.
 *
 *    Instead of:  Player → HistoryManager → Bus
 *    Do:          Player → WorldEventBus → EVERYTHING"
 * </pre>
 *
 * <p>Before this pivot, HistoryManager's hook methods wrote directly to
 * siloed stores (PlayerHistory, NpcMemory, RelationshipHistory, WorldHistory)
 * and never reached the WorldEventBus. The player's actions were invisible
 * to every downstream cognitive system. This was the root cause of the
 * "systems exist but don't connect" disease.
 *
 * <p>Now, each hook method delegates to {@link SimulationActions}, which
 * publishes a {@link dev.ergenverse.simulation.event.WorldEvent} to the
 * {@link dev.ergenverse.simulation.event.WorldEventBus}. The recording
 * is done by {@link dev.ergenverse.simulation.action.HistorySubscriber}
 * (the inversion — HistoryManager's recording logic, now bus-driven) and
 * {@link dev.ergenverse.simulation.action.RelationshipEngine} (which
 * infers relationship deltas from the semantic tag, with NO canon checks).
 *
 * <p>The flow is now:
 * <pre>
 *   Gameplay code (EntityCultivator, CultivationEvents, etc.)
 *     → HistoryManager.onNpcInteraction(...)        ← this facade
 *     → SimulationActions.playerInteractedWithNpc(...)
 *     → WorldEventBus.dispatch(player.interaction)
 *     → HistorySubscriber.onEvent()                 ← records to history
 *     → MemoryEventSubscriber.onEvent()             ← NPCs remember
 *     → RelationshipEngine.onEvent()                ← trust inferred
 *     → ChronicleSubscriber.onEvent()               ← world chronicle
 *     → OpportunityGenerator.onEvent()              ← new possibilities
 * </pre>
 *
 * <p><b>The isManifestation() gate is GONE.</b> The old code only recorded
 * relationship affinity for "manifestation" NPCs (a string match on
 * {@code npcCanonId.contains("manifestation")}), excluding Wang Ping, Old
 * Chen, sect elders, and every non-manifestation NPC. The RelationshipEngine
 * now infers relationship deltas for ALL actors — the graph knows actors,
 * not canon.
 *
 * <h2>Why keep this class at all?</h2>
 * <p>There are 10+ existing call sites (EntityCultivator, CultivationEvents,
 * NpcGiftOfferGoal, ManifestationGiftHandler, etc.) that call
 * {@code HistoryManager.onX(...)}. Rather than update all of them in one
 * risky pass, this facade preserves the API while routing through the new
 * event-sourced architecture. New code should call {@link SimulationActions}
 * directly; this facade exists for backward compatibility.
 *
 * <h2>Migration path</h2>
 * <ol>
 *   <li>Phase 1 (this commit): HistoryManager becomes a facade. All call
 *       sites still work, but now flow through the bus.</li>
 *   <li>Phase 2 (future): update call sites to call SimulationActions
 *       directly. HistoryManager can then be deprecated/removed.</li>
 * </ol>
 */
public final class HistoryManager {

    private HistoryManager() {}

    // ─── Cultivation Events ──────────────────────────────────────────

    /**
     * Called when the player successfully breaks through to a new realm.
     * Delegates to {@link SimulationActions#playerBreakthrough}.
     */
    public static void onBreakthrough(ServerPlayer player,
                                        String fromRealmName,
                                        String toRealmName,
                                        long worldTick) {
        SimulationActions.playerBreakthrough(player, fromRealmName, toRealmName, worldTick);
    }

    // ─── NPC Interaction ──────────────────────────────────────────────

    /**
     * Called when the player right-clicks or otherwise interacts with
     * an EntityCultivator NPC. Delegates to
     * {@link SimulationActions#playerInteractedWithNpc}.
     */
    public static void onNpcInteraction(ServerPlayer player,
                                             String npcCanonId,
                                             String interactionType,
                                             String detail,
                                             long worldTick) {
        SimulationActions.playerInteractedWithNpc(player, npcCanonId,
                interactionType, detail, worldTick);
    }

    /**
     * Called when the player receives a gift from an NPC.
     * Delegates to {@link SimulationActions#playerReceivedGift}.
     */
    public static void onGiftReceived(ServerPlayer player,
                                          String protagonistId,
                                          String itemName,
                                          long worldTick) {
        SimulationActions.playerReceivedGift(player, protagonistId, itemName, worldTick);
    }

    // ─── NPC Combat ────────────────────────────────────────────────────

    /**
     * Called when the player damages or kills an EntityCultivator.
     * Delegates to {@link SimulationActions#playerEngagedCombat}.
     */
    public static void onNpcCombat(ServerPlayer player,
                                     String npcCanonId,
                                     String npcName,
                                     boolean playerWon,
                                     long worldTick) {
        SimulationActions.playerEngagedCombat(player, npcCanonId, npcName,
                playerWon, worldTick);
    }

    // ─── Discovery ────────────────────────────────────────────────────

    /**
     * Called when the player discovers something noteworthy.
     * Delegates to {@link SimulationActions#playerDiscovered}.
     */
    public static void onDiscovery(ServerPlayer player,
                                       String subject,
                                       String detail,
                                       long worldTick) {
        SimulationActions.playerDiscovered(player, subject, detail, worldTick);
    }
}
