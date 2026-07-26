package dev.ergenverse.runtime;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

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
 * <p><b>CRON-COMPLETIONIST-76: Item-frame block-break cascade.</b> When a
 * player breaks a block that has an item frame or painting attached to one of
 * its 6 faces, vanilla Minecraft removes the attached entity (it "pops off"
 * and drops as an item). Prior to this round, the tracker only recorded the
 * BLOCK delta — the entity removal was NOT journaled. On chunk reload, the
 * chunk-materializer would re-place the canon item frame (because no PLAYER
 * delta existed at the frame's position), causing the frame to reappear
 * floating where the supporting block used to be. This round closes that leak:
 * on every block break, we scan the 6 adjacent positions for item frames and
 * paintings, and if found, record a PLAYER "air" delta at the entity's
 * position. This prevents the chunk-materializer from re-placing the canon
 * entity on reload.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerBlockDeltaTracker {

    private PlayerBlockDeltaTracker() {}

    /**
     * When the player breaks a block, record it as "air" in the player delta.
     *
     * <p>CRON-76: Also scans for attached item frames and paintings on the 6
     * adjacent faces and records PLAYER "air" deltas at their positions.
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

        // CRON-76: Cascade — record PLAYER deltas for any item frames or
        // paintings attached to the broken block's 6 faces. Vanilla Minecraft
        // removes these entities when their supporting block is broken; if we
        // don't journal the removal, the chunk-materializer will re-place the
        // canon entity on reload (floating where the support used to be).
        try {
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                cascadeRecordAttachedEntities(serverLevel, pos);
            }
        } catch (Exception e) {
            Ergenverse.LOGGER.debug("[Ergenverse] PlayerBlockDeltaTracker cascade failed: {}", e.getMessage());
        }
    }

    /**
     * CRON-76: Scan the 6 faces of the broken block for attached item frames
     * and paintings. For each found, record a PLAYER "air" delta at the
     * entity's block position.
     *
     * <p>Item frames and paintings in Minecraft 1.20.1 are entities whose
     * position is at the block they hang on (with a small offset). When the
     * supporting block is broken, vanilla removes the entity. We journal the
     * removal so the chunk-materializer doesn't re-place the canon entity.
     *
     * <p>The scan uses a bounding-box query over a 3×3×3 region around the
     * broken block (the entity might be at the broken block's position OR at
     * an adjacent position, depending on facing). This is O(27) per block
     * break — negligible overhead.
     */
    private static void cascadeRecordAttachedEntities(ServerLevel level, BlockPos brokenPos) {
        // Search a 3×3×3 box around the broken block — covers all 6 face-attached
        // positions plus the broken block's own position (some frames sit at the
        // same block pos as their support, offset by facing).
        AABB searchBox = new AABB(
                brokenPos.getX() - 1, brokenPos.getY() - 1, brokenPos.getZ() - 1,
                brokenPos.getX() + 2, brokenPos.getY() + 2, brokenPos.getZ() + 2);

        List<ItemFrame> frames = level.getEntitiesOfClass(ItemFrame.class, searchBox);
        for (ItemFrame frame : frames) {
            // Only record frames whose supporting block is the broken block
            // (or the broken block itself — a frame facing into the broken
            // pos has its support at the adjacent block).
            BlockPos framePos = frame.getPos();
            Direction facing = frame.getDirection();
            BlockPos supportPos = framePos.relative(facing.getOpposite());
            if (supportPos.equals(brokenPos) || framePos.equals(brokenPos)) {
                try {
                    WorldRuntime.get().world().setPlayerBlock(
                            framePos.getX(), framePos.getY(), framePos.getZ(), "minecraft:air");
                    Ergenverse.LOGGER.debug("[Ergenverse] CRON-76 cascade: recorded PLAYER air delta " +
                            "for item frame at {} (support {} was broken)", framePos, brokenPos);
                } catch (Exception e) {
                    Ergenverse.LOGGER.debug("[Ergenverse] CRON-76 cascade frame record failed: {}", e.getMessage());
                }
            }
        }

        List<Painting> paintings = level.getEntitiesOfClass(Painting.class, searchBox);
        for (Painting painting : paintings) {
            BlockPos paintPos = painting.getPos();
            Direction facing = painting.getDirection();
            BlockPos supportPos = paintPos.relative(facing.getOpposite());
            if (supportPos.equals(brokenPos) || paintPos.equals(brokenPos)) {
                try {
                    WorldRuntime.get().world().setPlayerBlock(
                            paintPos.getX(), paintPos.getY(), paintPos.getZ(), "minecraft:air");
                    Ergenverse.LOGGER.debug("[Ergenverse] CRON-76 cascade: recorded PLAYER air delta " +
                            "for painting at {} (support {} was broken)", paintPos, brokenPos);
                } catch (Exception e) {
                    Ergenverse.LOGGER.debug("[Ergenverse] CRON-76 cascade painting record failed: {}", e.getMessage());
                }
            }
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
