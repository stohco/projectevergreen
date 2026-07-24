package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * PlayerActionBridge — makes the player a <b>first-class actor</b> in the
 * event-sourced simulation architecture.
 *
 * <p>Per the user's directive (2026-07-23): "all player actions flow through
 * SimulationActions &rarr; WorldEventBus." Before this bridge, player
 * interactions with NPCs were processed by multiple independent Forge event
 * handlers ({@link dev.ergenverse.history.HistoryEvents},
 * {@link dev.ergenverse.wanglin.ai.WangLinAiTickHandler},
 * {@link dev.ergenverse.npc.dialogue.NpcDialogueTickHandler}, etc.), each
 * reading the raw Forge event directly. No WorldEvent was published. This
 * meant the RelationshipEngine, OpportunityGenerator, ChronicleSubscriber,
 * WangLinSemanticSubscriber, and all other bus subscribers never saw the
 * player's actions. The player was invisible to the simulation.
 *
 * <p>This bridge hooks into {@code PlayerInteractEvent.EntityInteract} at
 * <b>HIGHEST</b> priority (before the existing handlers) and dispatches a
 * {@code player.interaction} WorldEvent through {@link SimulationActions}
 * and {@link WorldEventBus}. Existing handlers continue to work — they
 * still receive the raw Forge event. But now the simulation also sees the
 * action.
 *
 * <h2>What this enables</h2>
 * <ul>
 *   <li>{@code HistorySubscriber} records the action to WorldHistory.</li>
 *   <li>{@code RelationshipEngine} infers relationship deltas from the
 *       interaction.</li>
 *   <li>{@code OpportunityGenerator} may create opportunities (escort
 *       request, recruitment) from observed deeds.</li>
 *   <li>{@code WangLinSemanticSubscriber} updates Wang Lin's opinion if he
 *       witnesses the interaction.</li>
 *   <li>{@code ChronicleSubscriber} compiles the event into the
 *       WorldChronicle.</li>
 * </ul>
 *
 * <h2>Design principles</h2>
 * <ul>
 *   <li><b>Article V — Everything Exists Without The Player:</b> The player's
 *       actions are now indistinguishable from NPC actions on the bus. The
 *       simulation doesn't know or care who published the event.</li>
 *   <li><b>Not a new Engine (Art XXVI):</b> This is a Forge event handler
 *       that calls existing factory methods. No new bus, no new subscriber,
 *       no new infrastructure. Pure wiring.</li>
 *   <li><b>Additive, not disruptive:</b> Registered at HIGHEST priority so it
 *       fires before existing handlers. Does NOT cancel the event — existing
 *       handlers still receive and process it normally.</li>
 *   <li><b>Performance:</b> One WorldEvent dispatch per interaction. The bus
 *       dispatch is sub-millisecond for 2-5 matching subscribers.</li>
 * </ul>
 *
 * <p>Must be registered on the Forge event bus:
 * {@code MinecraftForge.EVENT_BUS.register(PlayerActionBridge.class)}.
 *
 * @see SimulationActions#interactionEvent
 * @see WorldEventBus#dispatch
 */
public final class PlayerActionBridge {

    private PlayerActionBridge() {}

    /**
     * Bridge player entity interactions into the WorldEventBus.
     *
     * <p>When a player right-clicks an EntityCultivator, this handler:
     * <ol>
     *   <li>Builds a {@code player.interaction} WorldEvent via
     *       {@link SimulationActions#interactionEvent}.</li>
     *   <li>Dispatches it to {@link WorldEventBus#dispatch}.</li>
     * </ol>
     *
     * <p>The existing handlers (HistoryEvents, WangLinAiTickHandler, etc.)
     * still receive and process the Forge event independently. This is
     * additive — it doesn't replace any existing behavior.
     *
     * @param event the Forge entity interact event
     */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public static void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // Only server-side.
        if (event.getLevel().isClientSide()) return;
        // Only EntityCultivator targets (the mod's NPCs).
        if (!(event.getTarget() instanceof EntityCultivator cultivator)) return;
        // Only server players.
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        // Resolve the NPC's canon ID from the entity data.
        String npcCanonId = cultivator.getCharacterId();
        if (npcCanonId == null || npcCanonId.isEmpty()) {
            npcCanonId = "unknown_npc";
        }

        long tick = event.getLevel().getGameTime();

        // Build the interaction event via the factory and dispatch.
        WorldEvent worldEvent = SimulationActions.interactionEvent(
                serverPlayer, npcCanonId, "RIGHT_CLICK",
                "Player interacted with " + cultivator.getDisplayNameCn(),
                tick);

        WorldEventBus.dispatch(worldEvent);

        Ergenverse.LOGGER.debug("[PlayerActionBridge] {} interacted with {} → dispatched player.interaction",
                serverPlayer.getName().getString(), npcCanonId);
    }
}
