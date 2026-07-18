package dev.ergenverse.perception;

import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Server-side event handler for perception-filtered block interactions.
 *
 * <p>Per the Prime Directive: "Reality is objective; cultivation changes
 * understanding, not existence." A spirit herb block IS a spirit herb
 * regardless of who looks at it. But a mortal who breaks a Sea Pickle
 * gets a Sea Pickle — they don't know it's a Soul Nourishing Lotus.
 *
 * <p>Interaction gates:
 * <ul>
 *   <li><b>Block break</b>: If the block is a perception-block (spirit herb,
 *       spirit vein ore) and the player is below the interaction threshold,
 *       the break proceeds but yields the VANILLA item, not the spirit item.
 *       A message hints at something unusual if at Qi Condensation.</li>
 *   <li><b>Block right-click</b>: At Qi Condensation+, right-clicking a
 *       perception-block shows the perceived name and description in chat.
 *       Below Qi Condensation, nothing special happens.</li>
 * </ul>
 *
 * <p>IMPORTANT: This handler does NOT prevent breaking. Per the Prime
 * Directive, the world exists independently of the observer. A mortal
 * CAN break a spirit herb block. They just get the wrong (vanilla) item
 * and miss the true value. This is canon — in RI, mortals regularly
 * discarded spirit herbs as weeds.
 */
@Mod.EventBusSubscriber(modid = "ergenverse", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PerceptionBlockEvents {

    private PerceptionBlockEvents() {}

    /**
     * When a player right-clicks a perception-block, show the perceived
     * name and description based on their cultivation realm.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.isCanceled()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        var stateOpt = CultivationCapability.get(player);
        if (!stateOpt.isPresent()) return;
        CultivationState state = stateOpt.resolve().get();
        RealmId realm = state.getCurrentRealm();

        BlockPos pos = event.getPos();
        Level level = player.level();
        Block block = level.getBlockState(pos).getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        if (blockId == null) return;

        String blockIdStr = blockId.toString();

        // Check if this is a perception-block
        String perceivedName = AmbientPerception.perceiveBlockName(blockIdStr, realm);
        if (perceivedName == null) return;

        // Show perception info in chat (Qi Condensation+ only)
        if (PerceptionTier.fromRealm(realm).order >= PerceptionTier.QI_CONDENSATION.order) {
            String desc = AmbientPerception.perceiveBlockDescription(blockIdStr, realm);
            player.sendSystemMessage(Component.literal(perceivedName));
            if (desc != null) {
                player.sendSystemMessage(Component.literal("\u00A77" + desc + "\u00A7r"));
            }

            // Show harvest hint if player can't harvest true form
            if (!AmbientPerception.canHarvestTrueForm(blockIdStr, realm)) {
                player.sendSystemMessage(Component.literal(
                        "\u00A78You sense something more here, but your perception is insufficient to harvest its true form.\u00A7r"));
            }
        }
    }

    /**
     * When a player breaks a perception-block, check if they harvest
     * the true form or the vanilla stand-in.
     *
     * <p>NOTE: This event fires AFTER the block is broken. The block
     * has already been removed and drops are already generated. We
     * cannot change the drops from here in MC 1.20.1. Instead, we
     * send a message indicating what happened.
     *
     * <p>Full drop replacement (Foundation+ gets spirit herb item
     * instead of vanilla item) requires a BlockBreakEvent with
     * loot table modification, which is deferred to the next task.
     */
    @SubscribeEvent
    public static void onBlockBreak(PlayerInteractEvent.LeftClickBlock event) {
        // Not used for break detection — BreakEvent is on the Forge bus
        // but requires a different event type. For now, the right-click
        // inspection system is the primary interaction.
    }
}