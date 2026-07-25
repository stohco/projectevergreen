package dev.ergenverse.runtime;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * PlayerBlockDeltaTracker — records player block changes into the PlayerDelta.
 *
 * <p><b>Contract (2026-07-26 directive):</b> "The player adds another layer.
 * Mine stone. Cut tree. Build house. Destroy mountain. Dig tunnel. Plant
 * herbs. Create formation. Kill beasts. Start sect. Destroy village. Whatever.
 * Again, this doesn't modify the blueprint. It modifies the save."
 *
 * <p>This handler listens to Forge's {@link BlockEvent.BreakEvent} (player
 * mines a block) and {@link BlockEvent.EntityPlaceEvent} (player places a
 * block) and records the change through the {@link dev.ergenverse.runtime.layer.WorldFacade}
 * — the simulation's single front door (CRON-69 point 5: gameplay never
 * touches the delta store directly; it writes {@code runtime.world().setBlock}).
 *
 * <p>When the player mines a block at (x,y,z), we record a PLAYER
 * {@code BlockChangeDelta(pos, "minecraft:air")} (CRON-69 point 4: there is no
 * "removed" object — air is just a state). When the player places a block, we
 * record {@code BlockChangeDelta(pos, blockId)}. The facade journals the delta
 * AND mirrors it into the live level. On reload, the persisted journal (via
 * {@link dev.ergenverse.runtime.persist.WorldDeltaSavedData}) replays the
 * change, so it survives save/load without modifying the blueprint.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerBlockDeltaTracker {

    private PlayerBlockDeltaTracker() {}

    /**
     * When the player breaks a block, record it as "air" in the player delta.
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getPlayer() instanceof net.minecraft.server.level.ServerPlayer)) return;
        if (event.getPlayer().isCreative()) return; // creative mode doesn't persist changes

        BlockPos pos = event.getPos();
        // Record the break as a PLAYER block-change delta (air = "removed").
        try {
            WorldRuntime.get().world().setPlayerBlock(pos.getX(), pos.getY(), pos.getZ(), "minecraft:air");
        } catch (Exception e) {
            // Non-fatal — the block still breaks in Minecraft, we just don't track it
            Ergenverse.LOGGER.debug("[Ergenverse] PlayerBlockDeltaTracker break failed: {}", e.getMessage());
        }
    }

    /**
     * When the player places a block, record the new block state in the player delta.
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer)) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getPlacedBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockId == null) return;

        try {
            WorldRuntime.get().world().setPlayerBlock(pos.getX(), pos.getY(), pos.getZ(), blockId.toString());
        } catch (Exception e) {
            Ergenverse.LOGGER.debug("[Ergenverse] PlayerBlockDeltaTracker place failed: {}", e.getMessage());
        }
    }
}
