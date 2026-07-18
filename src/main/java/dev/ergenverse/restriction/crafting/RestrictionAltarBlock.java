package dev.ergenverse.restriction.crafting;

import dev.ergenverse.block.ErgenverseBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
 * Restriction Altar (\u7981\u5236\u53f0) — Wang Lin's signature workstation
 * for inscribing restrictions onto Restriction Flags.
 *
 * <p>A 1x1x1 block with an altar-shaped hitbox. Hosts a
 * {@link RestrictionAltarBlockEntity}. Right-click opens the inscription UI.
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md \u00a73.5 + \u00a713.2:
 * 9 layers \u00d7 11,111 restrictions = 99,999. 5-second inscription sessions.
 * Realm-gated, divine-sense-scaled.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class RestrictionAltarBlock extends BaseEntityBlock {

    /** Tall altar shape. */
    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);

    public RestrictionAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RestrictionAltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) return null;
        if (blockEntityType != dev.ergenverse.block.ErgenverseBlocks.RESTRICTION_ALTAR_ENTITY.get()) return null;
        return (lvl, pos, stt, be) -> {
            if (be instanceof RestrictionAltarBlockEntity altar) {
                altar.tickSession(lvl);
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RestrictionAltarBlockEntity altar) {
                MenuProvider provider = new MenuProvider() {
                    @Override
                    public net.minecraft.network.chat.Component getDisplayName() {
                        return net.minecraft.network.chat.Component.translatable(
                                "container.ergenverse.restriction_altar");
                    }

                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                            int containerId, net.minecraft.world.entity.player.Inventory playerInv,
                            Player p) {
                        return new RestrictionAltarMenu(containerId, playerInv, altar);
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
            if (be instanceof RestrictionAltarBlockEntity altar) {
                altar.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}