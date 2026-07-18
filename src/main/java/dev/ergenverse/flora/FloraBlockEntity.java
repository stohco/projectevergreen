package dev.ergenverse.flora;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FloraBlockEntity — block entity for spirit herbs that grow over time.
 * Minimal stub. Full implementation will track growth stages, Qi absorption, etc.
 */
public class FloraBlockEntity extends BlockEntity {
    private int growthStage = 0;
    
    public FloraBlockEntity(BlockPos pos, BlockState state) {
        super(null, pos, state);
    }
    
    public int getGrowthStage() { return growthStage; }
    public void setGrowthStage(int stage) { this.growthStage = stage; setChanged(); }
}
