package dev.ergenverse.alchemy;

import dev.ergenverse.block.ErgenverseBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Pill Furnace (丹炉) — the cultivator's alchemy workstation.
 *
 * <p>A 1×1×1 block that hosts a {@link PillFurnaceBlockEntity}. Right-click
 * opens the 4-slot crafting UI (2 inputs + fuel + output).
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md §3.1: this is the ONLY block where pills
 * can be refined. Canon: alchemists need a furnace/cauldron; Wang Lin's
 * early sect life revolved around the herb garden + pill furnace.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class PillFurnaceBlock extends BaseEntityBlock {

    /** Compact furnace-shaped hitbox (slightly smaller than a full block). */
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public PillFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // ENTITYBLOCK_ANIMATED forces the model to render + the BE to render too.
        // For a simple block model, INVISIBLE is wrong — use MODEL.
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PillFurnaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        // Server-side ticker only; client doesn't tick crafting.
        if (level.isClientSide()) return null;
        if (blockEntityType != dev.ergenverse.block.ErgenverseBlocks.PILL_FURNACE_ENTITY.get()) return null;
        return (lvl, pos, stt, be) -> {
            if (be instanceof PillFurnaceBlockEntity furnace) {
                furnace.tickCraft(lvl);
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PillFurnaceBlockEntity furnace) {
                MenuProvider provider = new MenuProvider() {
                    @Override
                    public net.minecraft.network.chat.Component getDisplayName() {
                        return net.minecraft.network.chat.Component.translatable(
                                "container.ergenverse.pill_furnace");
                    }

                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                            int containerId, net.minecraft.world.entity.player.Inventory playerInv,
                            Player p) {
                        return new PillFurnaceMenu(containerId, playerInv, furnace);
                    }
                };
                net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer, provider, buf -> {
                    buf.writeBlockPos(pos);
                });
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PillFurnaceBlockEntity furnace) {
                furnace.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
