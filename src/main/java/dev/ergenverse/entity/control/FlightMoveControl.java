package dev.ergenverse.entity.control;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.world.entity.ai.control.MoveControl;

/**
 * FlightMoveControl — 3D aerial movement for flying beasts (Hawk).
 *
 * <p>CRON-COMPLETIONIST-13: Created to close the behavioral gap where
 * flyers bulldozed through terrain with direct setDeltaMovement.
 */
public class FlightMoveControl extends MoveControl {

    private final SpiritBeastEntity beast;

    public FlightMoveControl(SpiritBeastEntity beast) {
        super(beast);
        this.beast = beast;
    }

    @Override
    public void tick() {
        // If on ground and not flying, use standard walk
        if (this.beast.onGround() && this.beast.getDeltaMovement().y <= 0.0D) {
            super.tick();
            return;
        }

        // In flight: delegate to standard MoveControl for ground-plane movement,
        // then add vertical component. This avoids needing protected field access.
        super.tick();

        // Don't fly too high (Y=320) or below world floor
        if (this.beast.getY() > 320) {
            this.beast.setDeltaMovement(
                    this.beast.getDeltaMovement().add(0, -0.15D, 0));
        } else if (this.beast.getY() < this.beast.level().getMinBuildHeight() + 5) {
            this.beast.setDeltaMovement(
                    this.beast.getDeltaMovement().add(0, 0.15D, 0));
        } else {
            // Gentle upward drift to maintain altitude
            this.beast.setDeltaMovement(
                    this.beast.getDeltaMovement().add(0, 0.02D, 0));
        }

        this.beast.hurtMarked = true;
    }
}
