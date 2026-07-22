package dev.ergenverse.entity.control;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.world.entity.ai.control.MoveControl;

/**
 * WaterBoundMoveControl — aquatic movement for water-dwelling beasts.
 *
 * <p>CRON-COMPLETIONIST-13: Created as part of the collision/pathing/AI subsystem.
 */
public class WaterBoundMoveControl extends MoveControl {

    private final SpiritBeastEntity beast;
    private int breathTimer;

    public WaterBoundMoveControl(SpiritBeastEntity beast) {
        super(beast);
        this.beast = beast;
    }

    @Override
    public void tick() {
        if (!this.beast.isInWater()) {
            super.tick();
            return;
        }

        // Use standard MoveControl for horizontal movement
        super.tick();

        // Breathing: swim toward surface when air is low
        breathTimer++;
        if (breathTimer > 200 && this.beast.getAirSupply() < this.beast.getMaxAirSupply() * 0.5F) {
            this.beast.setDeltaMovement(
                    this.beast.getDeltaMovement().add(0, 0.08D, 0));
            breathTimer = 0;
        } else {
            // Slight sink in water
            this.beast.setDeltaMovement(
                    this.beast.getDeltaMovement().add(0, -0.02D, 0));
        }

        this.beast.hurtMarked = true;
    }
}
