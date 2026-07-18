package dev.ergenverse.history;

import dev.ergenverse.entity.EntityCultivator;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * HistoryEvents — Forge event listeners that feed the Layer 3 history system.
 *
 * <p>This class captures game events and routes them to the appropriate
 * history recording methods. It exists because some events (like entity
 * interaction) cannot be handled by overriding methods (e.g. {@code
 * Mob.interact()} is final in MC 1.20.1).
 *
 * <h2>Registered events</h2>
 * <ul>
 *   <li>{@code PlayerInteractEvent.EntityInteract} — when a player
 *       right-clicks an EntityCultivator NPC, records the interaction
 *       in NpcMemory via {@link EntityCultivator#recordPlayerInteraction}.</li>
 * </ul>
 *
 * <p>Must be registered on the Forge event bus:
 * {@code MinecraftForge.EVENT_BUS.register(HistoryEvents.class)}.
 */
public class HistoryEvents {

    private HistoryEvents() {}

    /**
     * When a player right-clicks an EntityCultivator, record the
     * interaction in the NPC's memory (Layer 3: NpcMemory).
     *
     * <p>This is the Forge-event equivalent of overriding interact(),
     * which is final on Mob in MC 1.20.1.
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // Only handle server-side interactions with cultivator entities
        if (event.getLevel().isClientSide()) return;
        if (!(event.getTarget() instanceof EntityCultivator cultivator)) return;
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;

        cultivator.recordPlayerInteraction(serverPlayer);
    }
}