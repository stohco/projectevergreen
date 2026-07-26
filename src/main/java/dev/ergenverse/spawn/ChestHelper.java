package dev.ergenverse.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiConsumer;

/**
 * Utility class for placing chest blocks with loot tables pre-assigned.
 * MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 *
 * <h2>CRON-COMPLETIONIST-71 — Placer Abstraction</h2>
 * <p>Prior to CRON-71, {@link #placeChestWithLoot} called
 * {@code level.setBlock(pos, ...)} directly. This bypassed the
 * {@code sb()} helper in {@code WangFamilyVillageBuilder} (and the
 * analogous {@code sb()} helpers in the 6 other chunk-scoped builders),
 * which meant:
 * <ul>
 *   <li>The placement was NOT chunk-filtered — a chest at (cx-29, cy+1, cz-27)
 *       would be placed even when {@code buildForChunk} was building an
 *       unrelated chunk, causing cascading chunk-loads.</li>
 *   <li>The placement was NOT provenance-guarded — if a player broke the
 *       chest, the chunk-materializer would re-place it on next chunk-load,
 *       undoing the player's edit. This is the same bug class that
 *       CRON-69's {@code ProvenanceAwareRebuildGuard} fixed for the
 *       {@code isAlreadyBuilt} full-build path, but it persisted in the
 *       per-block chunk-scoped path for chest placements.</li>
 * </ul>
 *
 * <p>The fix introduces a <b>placer</b> parameter — a
 * {@link BiConsumer}&lt;{@link ServerLevel}, {@link BlockPos}&gt; that
 * performs the actual block placement. The default implementation
 * ({@link #DEFAULT_PLACER}) calls {@code level.setBlock(pos, state, 2)}
 * directly, preserving backward compatibility for any caller that does
 * not need chunk-scoping or provenance guarding (e.g., test code, or
 * the legacy {@code build(level, center)} entry points of older builders).
 *
 * <p>Chunk-scoped builders (e.g., {@code WangFamilyVillageBuilder}) pass a
 * placer that delegates to their {@code sb()} helper:
 * <pre>{@code
 *   ChestHelper.placeChestWithLoot(level, pos, lootTable,
 *       (lvl, p) -> sb(lvl, p, Blocks.CHEST.defaultBlockState(), 2));
 * }</pre>
 *
 * <p><b>Loot-table assignment is independent of placement.</b> Whether or
 * not the placer actually places the block (it may skip due to chunk-filter
 * or provenance guard), if a chest block entity exists at {@code pos}
 * after the placer call, the loot table is assigned. This handles the case
 * where the chest was already placed (by a prior chunk-load, or by the
 * player) — assigning the loot table is harmless if the chest has already
 * been opened (Minecraft sets the loot table to null after first open).
 * However, see the <b>loot-respawn bug</b> note below.
 *
 * <p><b>Loot-respawn bug (CRON-71 fix in callers, not here):</b> if a player
 * breaks a chest, that creates a PLAYER delta at the chest position. On
 * next chunk-load, the placer's provenance guard skips the chest placement
 * (correct — don't undo the player's edit). But if the caller then runs
 * {@code if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest)
 * chest.setItem(...)}, that would OVERWRITE the player's chest contents
 * with CANON loot. The fix is in the CALLER: callers must guard their
 * {@code setItem}/{@code setBook} calls with a provenance check
 * ({@code hasPlayerOrSimulationDelta(pos)}). See
 * {@code WangFamilyVillageBuilder.buildWangLinCorner} for the canonical
 * pattern.
 */
public final class ChestHelper {

    private ChestHelper() {}

    /**
     * The default placer — calls {@code level.setBlock(pos, state, 2)}
     * directly. Used by callers that do not need chunk-scoping or
     * provenance guarding (test code, legacy full-build paths).
     */
    public static final BiConsumer<ServerLevel, BlockPos> DEFAULT_PLACER =
            (level, pos) -> level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);

    /**
     * Places a chest block at the given position and assigns a loot table
     * to its {@link RandomizableContainerBlockEntity}. The block is placed
     * with flag 2 so the block entity is created immediately.
     *
     * <p>This overload uses {@link #DEFAULT_PLACER} (direct
     * {@code level.setBlock}). For chunk-scoped or provenance-guarded
     * placement, use {@link #placeChestWithLoot(ServerLevel, BlockPos,
     * ResourceLocation, BiConsumer)}.
     *
     * @param level      the server level
     * @param pos        the position to place the chest
     * @param lootTable  the resource location of the loot table (e.g.
     *                   {@code new ResourceLocation("ergenverse", "chests/some_table")})
     */
    public static void placeChestWithLoot(ServerLevel level, BlockPos pos, ResourceLocation lootTable) {
        placeChestWithLoot(level, pos, lootTable, DEFAULT_PLACER);
    }

    /**
     * Places a chest block at the given position using the supplied
     * {@code placer}, then assigns a loot table to its
     * {@link RandomizableContainerBlockEntity} if a chest block entity
     * exists at {@code pos} after the placer call.
     *
     * <p>The placer is responsible for chunk-filtering and provenance
     * guarding. If the placer skips the placement (e.g., the position is
     * outside the current chunk bounds, or a PLAYER/SIMULATION delta
     * exists at the position), the loot-table assignment is also skipped
     * (no chest block entity to assign to).
     *
     * <p>If the placer places a non-chest block (caller error), the
     * loot-table assignment is silently skipped (the {@code instanceof
     * RandomizableContainerBlockEntity} check fails).
     *
     * @param level      the server level
     * @param pos        the position to place the chest
     * @param lootTable  the resource location of the loot table
     * @param placer     the block-placement callback (typically a lambda
     *                   delegating to the caller's {@code sb()} helper)
     */
    public static void placeChestWithLoot(ServerLevel level, BlockPos pos,
                                          ResourceLocation lootTable,
                                          BiConsumer<ServerLevel, BlockPos> placer) {
        placer.accept(level, pos);
        if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(lootTable, level.random.nextLong());
        }
    }
}
