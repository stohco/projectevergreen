package dev.ergenverse.materialization;

import dev.ergenverse.assembly.AssemblyResult;
import dev.ergenverse.assembly.MaterialID;
import dev.ergenverse.assembly.Rotation;
import dev.ergenverse.assembly.VoxelInstruction;
import dev.ergenverse.block.ErgenverseBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * MaterialResolver — the <b>single seam</b> between a backend-agnostic
 * {@link MaterialID} and a concrete Minecraft {@code BlockState}.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>The user's pipeline:
 * <blockquote>
 *   VoxelInstruction → MinecraftBackend → BlockState. […] Later you could write
 *   GodotBackend → Mesh or RendererBackend → OpenGL without changing any canon
 *   data.
 * </blockquote>
 *
 * <p>This class is the "MinecraftBackend". It is the <b>only</b> place in the
 * entire codebase where a {@link MaterialID} becomes a {@code BlockState}. If
 * the mod ever ports to another engine, this is the one class to replace —
 * every canon object, every library, and the {@link dev.ergenverse.assembly.WorldAssembler}
 * remain untouched.
 *
 * <p>Rotation is applied here to directional blocks (doors). Most materials
 * (planks, logs, carpet) are rotation-invariant and ignore the field.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class MaterialResolver {

    private MaterialResolver() {}

    /** Translates a {@link MaterialID} + {@link Rotation} into a {@code BlockState}. */
    public static BlockState resolve(MaterialID material, Rotation rotation) {
        BlockState state = baseState(material);
        return applyRotation(state, material, rotation);
    }

    private static BlockState baseState(MaterialID material) {
        return switch (material) {
            // Structural
            case AIR -> Blocks.AIR.defaultBlockState();
            case OAK_PLANKS -> Blocks.OAK_PLANKS.defaultBlockState();
            case SPRUCE_PLANKS -> Blocks.SPRUCE_PLANKS.defaultBlockState();
            case OAK_LOG -> Blocks.OAK_LOG.defaultBlockState();
            case COBBLESTONE -> Blocks.COBBLESTONE.defaultBlockState();
            case POLISHED_ANDESITE -> Blocks.POLISHED_ANDESITE.defaultBlockState();
            case FARMLAND -> Blocks.FARMLAND.defaultBlockState();
            case OAK_FENCE -> Blocks.OAK_FENCE.defaultBlockState();
            case OAK_DOOR_LOWER -> Blocks.OAK_DOOR.defaultBlockState()
                    .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
            case OAK_DOOR_UPPER -> Blocks.OAK_DOOR.defaultBlockState()
                    .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);

            // Furnishings
            case WHITE_CARPET -> Blocks.WHITE_CARPET.defaultBlockState();
            case GRAY_CARPET -> Blocks.GRAY_CARPET.defaultBlockState();
            case BOOKSHELF -> Blocks.BOOKSHELF.defaultBlockState();
            case LECTERN -> Blocks.LECTERN.defaultBlockState();
            case CHEST -> Blocks.CHEST.defaultBlockState();
            case TRAPPED_CHEST -> Blocks.TRAPPED_CHEST.defaultBlockState();
            case LANTERN -> Blocks.LANTERN.defaultBlockState();
            case WORK_TABLE_TOP -> Blocks.OAK_PLANKS.defaultBlockState();

            // Mod blocks
            case ALCHEMY_FURNACE -> ErgenverseBlocks.ALCHEMY_FURNACE.get().defaultBlockState();
            case SPIRIT_VEIN_STONE -> ErgenverseBlocks.SPIRIT_VEIN_STONE.get().defaultBlockState();
            case FORMATION_CORE_STONE -> ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState();
            case QI_GATHERING_GRASS -> ErgenverseBlocks.QI_GATHERING_GRASS.get().defaultBlockState();
            case SPIRIT_SAND -> ErgenverseBlocks.SPIRIT_SAND.get().defaultBlockState();
            case SPIRIT_WOOD_LEAVES -> ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get().defaultBlockState();
        };
    }

    /**
     * Applies rotation to directional blocks. Doors use the FACING property;
     * Rotation.NONE maps to NORTH (the canonical door orientation in this mod).
     * Non-directional blocks are returned unchanged.
     */
    private static BlockState applyRotation(BlockState state, MaterialID material, Rotation rotation) {
        if (rotation == Rotation.NONE) {
            // Doors still need an explicit facing even at NONE (default is SOUTH).
            if (material == MaterialID.OAK_DOOR_LOWER || material == MaterialID.OAK_DOOR_UPPER) {
                return state.setValue(DoorBlock.FACING, Direction.NORTH)
                        .setValue(DoorBlock.OPEN, false);
            }
            return state;
        }
        Direction facing = switch (rotation) {
            case NONE -> Direction.NORTH;
            case CW90 -> Direction.EAST;
            case CW180 -> Direction.SOUTH;
            case CW270 -> Direction.WEST;
        };
        if (material == MaterialID.OAK_DOOR_LOWER || material == MaterialID.OAK_DOOR_UPPER) {
            return state.setValue(DoorBlock.FACING, facing)
                    .setValue(DoorBlock.OPEN, false);
        }
        return state;
    }

    /** Convenience: resolve the material of a single {@link VoxelInstruction}. */
    public static BlockState resolve(VoxelInstruction v) {
        return resolve(v.material(), v.rotation());
    }

    /** Returns the total number of voxels in an assembly (for logging). */
    public static int count(AssemblyResult result) {
        return result.voxelCount();
    }
}
