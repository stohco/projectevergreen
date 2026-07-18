package dev.ergenverse.formation.crafting;
import dev.ergenverse.block.ErgenverseBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public class FormationPlatformBlock extends BaseEntityBlock {
    public FormationPlatformBlock(Properties props) { super(props); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FormationPlatformBlockEntity(pos, state); }
    @Override public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) {
        if (l.isClientSide()) return null;
        if (t != dev.ergenverse.block.ErgenverseBlocks.FORMATION_PLATFORM_ENTITY.get()) return null;
        return null; // TODO: fix ticker
    }
    @Override public InteractionResult use(BlockState s, Level l, BlockPos p, Player pl, InteractionHand h, BlockHitResult hit) {
        if (!l.isClientSide && pl instanceof ServerPlayer sp) {
            BlockEntity be = l.getBlockEntity(p);
            if (be instanceof FormationPlatformBlockEntity e) net.minecraftforge.network.NetworkHooks.openScreen(sp, e, p);
        }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
