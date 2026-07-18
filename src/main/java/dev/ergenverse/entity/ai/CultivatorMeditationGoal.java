package dev.ergenverse.entity.ai;

import dev.ergenverse.entity.EntityCultivator;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * CultivatorMeditationGoal — NPC cultivator meditation AI goal.
 * Minimal stub. Full implementation will use CultivatorActivity system.
 */
public class CultivatorMeditationGoal extends Goal {
    private final EntityCultivator cultivator;
    
    public CultivatorMeditationGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
    }
    
    @Override
    public boolean canUse() {
        return false; // TODO: check if cultivator should meditate
    }
    
    @Override
    public void tick() {
        // TODO: meditation logic
    }
}
