package dev.ergenverse.entity.control;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.world.entity.ai.control.MoveControl;

/**
 * SprintMoveControl — enhanced ground movement with sprint burst capability.
 *
 * <p>Replaces default WalkMoveControl. Behaves identically to WalkMoveControl
 * for normal movement, but supports 2x speed sprint bursts when the beast's
 * sprint flag is set (by SprintGoal or HuntGoal charge phase). Sprint lasts
 * 30 ticks and has a 60 tick cooldown.
 *
 * <p>CRON-COMPLETIONIST-13: Created as part of the collision/pathing/AI subsystem.
 */
public class SprintMoveControl extends MoveControl {

    private final SpiritBeastEntity beast;
    private int sprintTicks;
    private int sprintCooldown;

    public SprintMoveControl(SpiritBeastEntity beast) {
        super(beast);
        this.beast = beast;
    }

    /**
     * Trigger a sprint burst. Called by HuntGoal when charging.
     */
    public void startSprint() {
        if (sprintCooldown <= 0) {
            sprintTicks = 30; // 1.5 seconds of sprint
            sprintCooldown = 60; // 3 second cooldown
        }
    }

    public boolean isSprinting() {
        return sprintTicks > 0;
    }

    @Override
    public void tick() {
        boolean wasSprinting = sprintTicks > 0;
        if (sprintTicks > 0) sprintTicks--;
        if (sprintCooldown > 0) sprintCooldown--;

        // Use standard walk tick as base
        super.tick();

        // If sprinting and moving, boost speed
        if (sprintTicks > 0 && this.operation == Operation.MOVE_TO) {
            double boost = 2.0D; // 2x speed during sprint
            this.beast.setDeltaMovement(
                    this.beast.getDeltaMovement().multiply(boost, 1.0D, boost));
            this.beast.hurtMarked = true;
            // Wire POSE_SPRINTING so models can render sprint animation
            this.beast.setSpiritPose(SpiritBeastEntity.POSE_SPRINTING);
        } else if (wasSprinting && sprintTicks <= 0) {
            // Sprint just ended — clear POSE_SPRINTING back to STANDING
            // Only reset if no other goal is driving a different pose
            if (this.beast.getSpiritPose() == SpiritBeastEntity.POSE_SPRINTING) {
                this.beast.setSpiritPose(SpiritBeastEntity.POSE_STANDING);
            }
        }
    }
}
