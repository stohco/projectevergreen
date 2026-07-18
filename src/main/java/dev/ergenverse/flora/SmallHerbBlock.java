package dev.ergenverse.flora;

import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.effect.MobEffects;

/**
 * SmallHerbBlock — spirit herb block (cross-sprite like vanilla flowers).
 * Minimal stub.
 */
public class SmallHerbBlock extends FlowerBlock {
    public SmallHerbBlock(BlockBehaviour.Properties props) {
        super(MobEffects.SATURATION, 0, props);
    }
}
