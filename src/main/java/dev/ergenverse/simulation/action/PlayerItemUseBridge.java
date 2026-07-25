package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.item.ErgenverseItems;
import dev.ergenverse.simulation.event.EnergyType;
import dev.ergenverse.simulation.event.SemanticTag;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

/**
 * PlayerItemUseBridge — publishes {@code player.item.used} WorldEvents when a
 * player finishes using (eating, drinking, activating) any ergenverse mod item.
 *
 * <p>Per the 2026-07-23 event-sourced pivot: "all player actions flow through
 * SimulationActions &rarr; WorldEventBus." The custom item classes (FlyingSword,
 * SoulBead, SpiritPill, Talisman) already publish their own {@code player.spell.cast}
 * events internally. This bridge catches the REMAINING mod items — generic
 * crafting materials, scrolls, banners, and any future items — that use vanilla
 * Item behavior without custom event publishing.
 *
 * <p>This bridge is ADDITIVE — it does not replace or cancel any existing
 * behavior. It publishes a low-severity event (0.2) that is below the
 * ledger threshold (0.45) so routine item use doesn't flood WorldHistory.
 * But subscribers like MemoryEventSubscriber, BeliefFormationSubscriber,
 * and the SemanticEventReactor can still observe the action.
 *
 * <h2>Event topic: {@code player.item.used}</h2>
 * <p>Carries the item's registry name, display name, and player UUID as
 * metadata. Subscribers can filter by item category (scroll, banner, etc.)
 * to react differently to different item types.
 *
 * <h2>Exclusions</h2>
 * <p>Items that already publish their own events (FlyingSword, SoulBead,
 * SpiritPill, Talisman) are excluded to prevent duplicate events. The
 * exclusion is by item class, not registry name — if a new item class
 * publishes events, it should be added to the exclusion set.
 *
 * <p><b>Not a new Engine (Art XXVI):</b> Pure Forge event handler that
 * dispatches a WorldEvent. No new bus, no new subscriber, no new store.
 */
public final class PlayerItemUseBridge {

    private PlayerItemUseBridge() {}

    /**
     * Bridge item-use finish to the WorldEventBus.
     *
     * <p>When a player finishes using an ergenverse mod item, publish a
     * {@code player.item.used} event. Only triggers for mod items that
     * don't already publish their own events.
     *
     * @param event the Forge LivingEntityUseItemEvent.Finish event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        // Only server-side.
        if (event.getEntity() == null) return;
        if (event.getEntity().level().isClientSide()) return;

        // Only player actions.
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        ItemStack stack = event.getItem();
        if (stack.isEmpty()) return;

        // Only ergenverse mod items.
        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null || !itemId.getNamespace().equals(Ergenverse.MOD_ID)) return;

        // Exclude items that publish their own events (to prevent duplicates).
        // These items call SimulationActions.spellCast() internally.
        if (isSelfPublishing(stack)) return;

        long tick = event.getEntity().level().getGameTime();

        WorldEvent worldEvent = WorldEvent.of(
                "player.item.used", EnergyType.ACQUIRE,
                event.getEntity().blockPosition(),
                0.2f, 0.2f,
                serverPlayer.getName().getString() + " used " + stack.getHoverName().getString() + ".",
                "PLAYER_ACTION", tick,
                serverPlayer.getStringUUID(), "",
                SemanticTag.INTERACTION.name(),
                Map.of(
                        "item_id", itemId.toString(),
                        "item_name", stack.getHoverName().getString(),
                        "duration_ticks", String.valueOf(event.getDuration())
                )
        );
        WorldEventBus.dispatch(worldEvent);

        Ergenverse.LOGGER.debug("[PlayerItemUseBridge] {} used {} → dispatched player.item.used",
                serverPlayer.getName().getString(), itemId);
    }

    /**
     * Check if the item publishes its own events on the WorldEventBus.
     * Items in this set call SimulationActions.spellCast() internally
     * and should not produce a duplicate generic event from this bridge.
     */
    private static boolean isSelfPublishing(ItemStack stack) {
        // Check by item class — these classes publish events in their use() or finishUsingItem().
        return stack.getItem() instanceof dev.ergenverse.item.FlyingSwordItem
                || stack.getItem() instanceof dev.ergenverse.item.SoulBeadItem
                || stack.getItem() instanceof dev.ergenverse.item.SpiritPillItem
                || stack.getItem() instanceof dev.ergenverse.item.TalismanItem;
    }
}
