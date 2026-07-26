package dev.ergenverse.history;

import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.wanglin.bead.ZhouRuSoulTransferEvent;
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
 *   <li>CRON-COMPLETIONIST-110: if the target cultivator's characterId is
 *       {@code "zhou_ru"}, also dispatches to
 *       {@link ZhouRuSoulTransferEvent#handleSoulTransfer} — the soul
 *       transfer from the Heaven-Defying Bead into the reincarnation
 *       vessel (周茹). The transfer is gated by the event handler: only
 *       fires if the player holds the bead with Li Muwan's soul captured
 *       (CRON-99 prerequisite) and the transfer has not already occurred
 *       (write-once).</li>
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
     * <p>CRON-COMPLETIONIST-110: additionally, if the target is 周茹
     * (characterId="zhou_ru"), dispatch to the soul-transfer event handler.
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

        // 1. Always record the interaction in the NPC's memory.
        cultivator.recordPlayerInteraction(serverPlayer);

        // 2. CRON-110: if the target is 周茹, dispatch to the soul-transfer
        //    event handler. The handler is fully defensive — it no-ops if
        //    the player has no bead, the bead has no soul, or the transfer
        //    has already occurred. Canon: 周茹 is the reincarnation vessel
        //    for Li Muwan's soul; the transfer is a one-time event per save.
        if (ZhouRuSoulTransferEvent.CHARACTER_ID.equals(cultivator.getCharacterId())) {
            ZhouRuSoulTransferEvent.handleSoulTransfer(serverPlayer, cultivator);
        }
    }
}