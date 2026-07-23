package dev.ergenverse.entity.control;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.world.entity.ai.control.MoveControl;

/**
 * WaterBoundMoveControl — aquatic movement for water-dwelling beasts.
 *
 * <p>CRON-COMPLETIONIST-13: Created as part of the collision/pathing/AI subsystem.
 *
 * <p>Breath fix: when air below 20, force upward immediately. At surface,
 * restore air faster. Reduced sink rate from -0.02 to -0.008.
 */
public class WaterBoundMoveControl extends MoveControl {

    private final SpiritBeastEntity beast;

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

        // Breathing: when air is critically low (< 20), force upward immediately
        int air = this.beast.getAirSupply();
        int maxAir = this.beast.getMaxAirSupply();

        if (air < 20) {
            // Force strong upward impulse to reach surface
            this.beast.setDeltaMovement(
                    this.beast.getDeltaMovement().add(0, 0.12D, 0));
        } else {
            // Check if at or near surface (water surface is at block Y + 0.8)
            double headY = this.beast.getY() + this.beast.getEyeHeight();
            int surfaceY = this.beast.blockPosition().above().getY();
            if (headY >= surfaceY - 0.2D && air < maxAir) {
                // At surface: restore air faster
                this.beast.setAirSupply(Math.min(air + 5, maxAir));
            }
        }

        // Use standard MoveControl for horizontal movement
        super.tick();

        // Reduced sink rate in water (from -0.02 to -0.008)
        this.beast.setDeltaMovement(
                this.beast.getDeltaMovement().add(0, -0.008D, 0));

        this.beast.hurtMarked = true;
    }
}
