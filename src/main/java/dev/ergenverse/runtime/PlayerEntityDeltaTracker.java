package dev.ergenverse.runtime;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.delta.EntityPlacementDelta;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * PlayerEntityDeltaTracker — records player-placed and player-removed
 * decoration entities (ItemFrame, GlowItemFrame, Painting) into the
 * {@link WorldDeltaStore} via the {@link WorldFacade}.
 *
 * <p><b>CRON-COMPLETIONIST-78.</b> This class closes the
 * <b>placement-direction provenance leak</b> identified in CRON-76 critique
 * #10 and deferred twice (CRON-76, CRON-77). The companion class
 * {@link PlayerBlockDeltaTracker} handles BLOCK changes (break, place) but
 * entity placements and removals were NOT journaled — the journal was not the
 * single source of truth for player state (vanilla chunk NBT was).
 *
 * <p><b>Two directions, two events:</b>
 * <ul>
 *   <li><b>Placement</b> — when the player right-clicks a block with an
 *       ItemFrame, GlowItemFrame, or Painting in hand, vanilla spawns the
 *       entity. The {@link PlayerInteractEvent.RightClickBlock} fires BEFORE
 *       the entity exists, so we schedule a 1-tick task (via
 *       {@code ServerLevel.getServer().tell(TickTask)}) to find the
 *       newly-spawned entity and record a PLAYER placement delta with the
 *       entity's NBT (captured via {@code entity.saveWithoutId}).</li>
 *   <li><b>Removal</b> — when the player attacks an ItemFrame or Painting
 *       directly (left-click), vanilla calls {@code entity.discard()} which
 *       sets the removal reason to {@link Entity.RemovalReason#DISCARDED}.
 *       The {@link EntityLeaveLevelEvent} fires, and we record a PLAYER
 *       removal delta at the entity's position. This prevents the canon
 *       builder from re-spawning the canon entity on next chunk-load.</li>
 * </ul>
 *
 * <p><b>Removal-reason filter.</b> {@link EntityLeaveLevelEvent} fires for
 * <i>every</i> entity departure, including chunk unload
 * ({@link Entity.RemovalReason#UNLOADED_TO_CHUNK}), player logout
 * ({@link Entity.RemovalReason#UNLOADED_WITH_PLAYER}), and dimension change
 * ({@link Entity.RemovalReason#CHANGED_DIMENSION}). We only record a PLAYER
 * removal delta for {@link Entity.RemovalReason#DISCARDED} — the
 * player-attack case. Other reasons are vanilla lifecycle events, not player
 * actions, and recording them would pollute the journal with spurious
 * "removals" every time a chunk unloads.
 *
 * <p><b>Interaction with CRON-76 cascade.</b> When a player breaks the
 * support block of a canon ItemFrame, the CRON-76 cascade (in
 * {@link PlayerBlockDeltaTracker}) records a PLAYER "air" BLOCK delta at the
 * entity's position. Vanilla then removes the entity (reason DISCARDED),
 * which fires {@link EntityLeaveLevelEvent} — and this tracker would ALSO
 * record a PLAYER REMOVE ENTITY delta at the same position. The duplicate
 * is harmless: both deltas agree ("no entity here"), and the canon builder's
 * {@code hasPlayerOrSimulationDelta} guard (updated in CRON-78 to check the
 * entity index too) skips re-placement regardless of which delta triggered
 * the guard.
 *
 * <p><b>Interaction with canon builder.</b> When a player directly attacks a
 * CANON ItemFrame (no support break), the CRON-76 cascade does NOT fire
 * (the player didn't break a block). This tracker is the ONLY record of the
 * removal. Without it, the canon builder would re-spawn the canon entity on
 * next chunk-load — exactly the bug that CRON-76 critique #10 mis-claimed
 * was closed. CRON-78 closes it for real: this tracker records the remove
 * delta, and {@code HengYueSectBuilder.placeItemFrame} (also fixed in
 * CRON-78) checks the entity index.
 *
 * <p><b>Idempotency.</b> Placement recording checks
 * {@link WorldDeltaStore#hasEntityPlacement} before recording — if a delta
 * already exists at the position (e.g., the player placed, removed, placed
 * again rapidly), we overwrite with the latest NBT via latest-wins in the
 * store. Removal recording always overwrites any prior placement delta at
 * the same position (latest-wins).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerEntityDeltaTracker {

    private PlayerEntityDeltaTracker() {}

    // ── Placement tracking ─────────────────────────────────────────────

    /**
     * When the player right-clicks a block with an ItemFrame, GlowItemFrame,
     * or Painting in hand, vanilla spawns the entity. Schedule a 1-tick task
     * to find the newly-spawned entity and record a PLAYER placement delta.
     *
     * <p>The 1-tick deferral mirrors {@link PlanetSuzakuChunkMaterializer}'s
     * pattern — vanilla needs the current tick to spawn the entity before we
     * can find it via {@code level.getEntitiesOfClass}.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer)) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        // Filter: only ItemFrame, GlowItemFrame, or Painting items.
        if (!isHangingEntityItem(stack)) return;

        BlockPos clickPos = event.getPos();
        ServerLevel level = (ServerLevel) event.getLevel();

        // Schedule 1-tick task to find the newly-spawned entity.
        // The entity doesn't exist yet at this moment — vanilla spawns it
        // during the interaction, which completes after this event returns.
        try {
            level.getServer().tell(new net.minecraft.server.TickTask(
                    level.getServer().getTickCount() + 1,
                    () -> {
                        try {
                            recordPlacementIfSpawned(level, clickPos);
                        } catch (Throwable t) {
                            Ergenverse.LOGGER.debug("[Ergenverse] PlayerEntityDeltaTracker " +
                                    "placement task failed at {}: {}", clickPos, t.getMessage());
                        }
                    }));
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] PlayerEntityDeltaTracker schedule failed: {}", t.getMessage());
        }
    }

    /**
     * Returns true if the stack is an ItemFrame, GlowItemFrame, or Painting
     * item — the three hanging-entity items in vanilla MC 1.20.1.
     */
    private static boolean isHangingEntityItem(ItemStack stack) {
        return stack.is(Items.ITEM_FRAME)
                || stack.is(Items.GLOW_ITEM_FRAME)
                || stack.is(Items.PAINTING);
    }

    /**
     * Search a small box around {@code clickPos} for newly-spawned ItemFrames
     * and Paintings. For each found, record a PLAYER placement delta with the
     * entity's full NBT (facing, item, rotation, variant).
     *
     * <p>The search box is the click position inflated by 1 block in each
     * direction — covers the case where the entity is placed on an adjacent
     * face (e.g., right-click on the side of a block places the frame on the
     * clicked block's face, which is at the same block pos).
     */
    private static void recordPlacementIfSpawned(ServerLevel level, BlockPos clickPos) {
        WorldRuntime runtime = WorldRuntime.get();
        if (!runtime.isInitialized()) return;

        // Search box: 3x3x3 around the click position. The frame's hanging
        // position is the block it's attached to, which is the clicked block
        // itself (when right-clicking a face). The 3x3x3 box covers all
        // possible adjacent placements (e.g., if the player clicked the top
        // of a block, the frame might hang on the side of an adjacent block).
        AABB searchBox = new AABB(clickPos).inflate(1.0);

        // ItemFrames (includes GlowItemFrame, which extends ItemFrame).
        List<ItemFrame> frames = level.getEntitiesOfClass(ItemFrame.class, searchBox);
        for (ItemFrame frame : frames) {
            BlockPos fpos = frame.getPos();
            try {
                // Skip if already tracked (idempotent — the 1-tick task might
                // fire multiple times if the player right-clicks rapidly).
                if (runtime.deltaStore().hasEntityPlacement(
                        fpos.getX(), fpos.getY(), fpos.getZ(), Provenance.PLAYER)) {
                    continue;
                }
                CompoundTag nbt = new CompoundTag();
                frame.saveWithoutId(nbt);
                runtime.world().recordPlayerEntityPlacement(
                        fpos.getX(), fpos.getY(), fpos.getZ(), nbt);
                Ergenverse.LOGGER.debug("[Ergenverse] PlayerEntityDeltaTracker: recorded " +
                        "placement of {} at {}.", frame.getType().toShortString(), fpos);
            } catch (Throwable t) {
                Ergenverse.LOGGER.debug("[Ergenverse] PlayerEntityDeltaTracker frame record " +
                        "failed at {}: {}", fpos, t.getMessage());
            }
        }

        // Paintings.
        List<Painting> paintings = level.getEntitiesOfClass(Painting.class, searchBox);
        for (Painting painting : paintings) {
            BlockPos ppos = painting.getPos();
            try {
                if (runtime.deltaStore().hasEntityPlacement(
                        ppos.getX(), ppos.getY(), ppos.getZ(), Provenance.PLAYER)) {
                    continue;
                }
                CompoundTag nbt = new CompoundTag();
                painting.saveWithoutId(nbt);
                runtime.world().recordPlayerEntityPlacement(
                        ppos.getX(), ppos.getY(), ppos.getZ(), nbt);
                Ergenverse.LOGGER.debug("[Ergenverse] PlayerEntityDeltaTracker: recorded " +
                        "placement of Painting at {}.", ppos);
            } catch (Throwable t) {
                Ergenverse.LOGGER.debug("[Ergenverse] PlayerEntityDeltaTracker painting record " +
                        "failed at {}: {}", ppos, t.getMessage());
            }
        }
    }

    // ── Removal tracking ───────────────────────────────────────────────

    /**
     * When an entity leaves the level, check if it's an ItemFrame or Painting
     * removed by player attack (removal reason DISCARDED). If so, record a
     * PLAYER removal delta at the entity's position.
     *
     * <p>This is the only signal we have for "player directly attacked a
     * canon ItemFrame" — the CRON-76 cascade only fires when the support
     * BLOCK is broken, not when the entity itself is attacked. Without this
     * tracker, canon ItemFrames in Heng Yue Sect (5 sites) would re-spawn
     * after the player removed them.
     *
     * <p>Filter: only {@link Entity.RemovalReason#DISCARDED}. Skip
     * {@link Entity.RemovalReason#UNLOADED_TO_CHUNK} (chunk unload),
     * {@link Entity.RemovalReason#UNLOADED_WITH_PLAYER} (player logout),
     * {@link Entity.RemovalReason#CHANGED_DIMENSION} (dimension change), and
     * {@link Entity.RemovalReason#KILLED} (death — not applicable to hanging
     * entities). These are vanilla lifecycle events, not player actions.
     */
    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        Entity entity = event.getEntity();
        if (entity == null) return;
        if (!(entity instanceof ItemFrame) && !(entity instanceof Painting)) return;

        // Filter: only DISCARDED (player attack or other discard).
        Entity.RemovalReason reason = entity.getRemovalReason();
        if (reason != Entity.RemovalReason.DISCARDED) return;

        // Get the entity's hanging position (the block it was attached to).
        BlockPos epos;
        if (entity instanceof ItemFrame frame) {
            epos = frame.getPos();
        } else if (entity instanceof Painting painting) {
            epos = painting.getPos();
        } else {
            epos = entity.blockPosition();
        }

        try {
            WorldRuntime runtime = WorldRuntime.get();
            if (!runtime.isInitialized()) return;
            runtime.world().recordPlayerEntityRemoval(
                    epos.getX(), epos.getY(), epos.getZ());
            Ergenverse.LOGGER.debug("[Ergenverse] PlayerEntityDeltaTracker: recorded " +
                    "removal of {} at {} (reason DISCARDED).",
                    entity.getType().toShortString(), epos);
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] PlayerEntityDeltaTracker removal record " +
                    "failed at {}: {}", epos, t.getMessage());
        }
    }
}
