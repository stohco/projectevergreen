package dev.ergenverse.forge;

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
 * Artifact Forge (炼器炉) — the cultivator's artifact-refining workstation.
 *
 * <p>A 1×1×1 block that hosts an {@link ArtifactForgeBlockEntity}. Right-click
 * opens the 5-slot crafting UI (blank + material + catalyst + fuel + output).
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md §3.2: this is the ONLY block where
 * artifacts are forged. 6 modes: blood-refine, soul-seal, dao-imprint,
 * immortal-guard, poison, repair. Canon: Wang Lin learned artifact-refining
 * from the Soul Refining Sect's Patriarch Situ Nan; the disciple's first
 * forge is always blood-refined.
 *
 * <p>Axiom 1 (per design doc): the Forge imbues conduction, combat, and
 * soul-binding — NOT flight. Any sword in a cultivator's hand can fly
 * once they have unlocked Active Flight.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class ArtifactForgeBlock extends BaseEntityBlock {

    /** Compact anvil-shaped hitbox (slightly smaller than a full block). */
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public ArtifactForgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // MODEL so the block renders its JSON model + the BE overlays (if any).
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArtifactForgeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        // Server-side ticker only; client doesn't tick crafting.
        if (level.isClientSide()) return null;
        if (blockEntityType != ErgenverseBlocks.ARTIFACT_FORGE_ENTITY.get()) return null;
        return (lvl, pos, stt, be) -> {
            if (be instanceof ArtifactForgeBlockEntity forge) {
                forge.tickCraft(lvl);
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ArtifactForgeBlockEntity forge) {
                MenuProvider provider = new MenuProvider() {
                    @Override
                    public net.minecraft.network.chat.Component getDisplayName() {
                        return net.minecraft.network.chat.Component.translatable(
                                "container.ergenverse.artifact_forge");
                    }

                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                            int containerId, net.minecraft.world.entity.player.Inventory playerInv,
                            Player p) {
                        return new ArtifactForgeMenu(containerId, playerInv, forge);
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
            if (be instanceof ArtifactForgeBlockEntity forge) {
                forge.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
