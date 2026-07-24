package dev.ergenverse.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

/**
 * Utility class for placing chest blocks with loot tables pre-assigned.
 * MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public final class ChestHelper {

    private ChestHelper() {}

    /**
     * Places a chest block at the given position and assigns a loot table
     * to its {@link RandomizableContainerBlockEntity}. The block is placed
     * with flag 2 so the block entity is created immediately.
     *
     * @param level      the server level
     * @param pos        the position to place the chest
     * @param lootTable  the resource location of the loot table (e.g.
     *                   {@code new ResourceLocation("ergenverse", "chests/some_table")})
     */
    public static void placeChestWithLoot(ServerLevel level, BlockPos pos, ResourceLocation lootTable) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(lootTable, level.random.nextLong());
        }
    }
}
