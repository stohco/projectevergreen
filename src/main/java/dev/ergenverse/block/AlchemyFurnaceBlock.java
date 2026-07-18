package dev.ergenverse.block;

import dev.ergenverse.block.entity.AlchemyFurnaceBlockEntity;
import dev.ergenverse.block.entity.ErgenverseBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

/**
 * AlchemyFurnaceBlock — the first Minecraft crafting block in the Ergenverse.
 *
 * <p>A directional, horizontal-facing block. Right-clicking it opens the
 * Alchemy Furnace menu (server-side validated, client-side rendered).
 *
 * <p>Uses the {@link BaseEntityBlock} pattern (like the vanilla furnace):
 * <ul>
 *   <li>{@code newBlockEntity()} returns a fresh {@link AlchemyFurnaceBlockEntity}</li>
 *   <li>{@code getTicker()} returns a server-only ticker that drives the
 *       craft/fuel loop</li>
 *   <li>{@code getRenderShape()} returns {@code RenderShape.MODEL} so the
 *       block uses its blockstate JSON model, not the legacy BE renderer</li>
 *   <li>{@code use()} opens the menu via {@link NetworkHooks#openScreen}</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public class AlchemyFurnaceBlock extends BaseEntityBlock {

    /** Horizontal facing direction. North/South/East/West. */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public AlchemyFurnaceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Use the blockstate JSON model — do NOT use a legacy BE renderer.
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Face the player (like a vanilla furnace).
        return this.defaultBlockState().setValue(FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * Right-click opens the Alchemy Furnace menu on the server side.
     * The BE's {@code createMenu} supplies the {@link AlchemyFurnaceMenu}.
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AlchemyFurnaceBlockEntity furnace) {
                // BaseContainerBlockEntity implements MenuProvider — pass it
                // directly. NetworkHooks.openScreen handles the buffer write
                // (writes BlockPos) and the client-side menu factory reads it.
                NetworkHooks.openScreen((ServerPlayer) player, furnace, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /**
     * Server-only ticker. Drives the craft/fuel loop in
     * {@link AlchemyFurnaceBlockEntity#serverTick()}.
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,
                                                                   BlockState state,
                                                                   BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == ErgenverseBlockEntities.ALCHEMY_FURNACE.get()
                ? (lvl, pos, st, be) -> ((AlchemyFurnaceBlockEntity) be).serverTick()
                : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemyFurnaceBlockEntity(pos, state);
    }
}
