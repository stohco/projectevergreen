package dev.ergenverse.item;

import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * SpiritStoneConsumeEvent — right-click with Spirit Stone to consume it
 * for a Qi boost. Canon: spirit stones are the universal Qi-supply medium;
 * cultivators absorb their energy to fuel cultivation.
 *
 * <p>Qi yield scales with cultivation realm (higher realm = more efficient
 * absorption). This is the player's primary early-game Qi source alongside
 * meditation and jade-slip qi-gathering.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
@Mod.EventBusSubscriber(modid = "ergenverse", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpiritStoneConsumeEvent {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        InteractionHand hand = event.getHand();
        ItemStack stack = event.getItemStack();

        // Only react to spirit_stone
        if (stack.getItem() != ErgenverseItems.SPIRIT_STONE.get()) {
            return;
        }

        // Server-side only
        if (level.isClientSide()) {
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // Get cultivation state via capability
        CultivationState cs = CultivationCapability.getOrThrow(serverPlayer);

        // Only cultivators at Qi Condensation+ can absorb spirit stones
        RealmId realm = cs.getCurrentRealm();
        if (realm == RealmId.MORTAL) {
            player.sendSystemMessage(Component.literal("\u00a77You lack the cultivation to absorb spirit stone Qi."));
            return;
        }

        // Qi yield scales with realm
        double qiYield = getQiYieldForRealm(realm);

        // Add Qi (addQi takes absolute amount)
        cs.addQi(qiYield);

        // Consume one spirit stone
        stack.shrink(1);
        if (stack.isEmpty()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }

        // Visual + audio feedback
        level.playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 1.5f);
        level.playSound(null, player.blockPosition(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3f, 0.8f);

        player.sendSystemMessage(Component.literal(
                String.format("\u00a7b\u2726 You absorb the spirit stone's Qi. \u00a77(+%.0f Qi)", qiYield)));

        // Swing arm
        player.swing(hand);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);
    }

    /**
     * Qi yield per spirit stone, scaling with cultivation realm.
     * Higher-realm cultivators extract more Qi from the same stone.
     */
    private static double getQiYieldForRealm(RealmId realm) {
        switch (realm) {
            case QI_CONDENSATION:      return 50.0;
            case FOUNDATION:           return 100.0;
            case CORE_FORMATION:       return 200.0;
            case NASCENT_SOUL:         return 400.0;
            case SOUL_FORMATION:       return 800.0;
            case SOUL_TRANSFORMATION:  return 1500.0;
            case ASCENDANT:            return 3000.0;
            default:                   return 50.0; // Higher tiers: spirit stones trivial
        }
    }
}
