package dev.ergenverse.entity.ai;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * SpiritBeastHuntGoal — predators (wolf, hawk, fire_beast, boar) hunt prey.
 *
 * <p>Behavior: scan for weaker living entities within follow range. When prey
 * found, enter stalking phase (state 1: set POSE_CHARGING, creep toward prey
 * at 0.6x speed). When within attack range (3 blocks), transition to charging
 * phase (state 2: set POSE_CHARGING, sprint toward prey at 1.3x speed).
 * On stop, set POSE_STANDING.
 *
 * <p>This goal sets the beast's target when prey is found, which allows
 * MeleeAttackGoal to handle the actual damage dealing. HuntGoal handles
 * the STALKING behavior that MeleeAttackGoal doesn't have.
 *
 * <p>CRON-COMPLETIONIST-13: New goal to close the AI→pose→model loop.
 * Previously, combat animation was driven by getTarget() != null, which
 * only fires when the beast is actively attacking. Now HuntGoal explicitly
 * sets POSE_CHARGING during the stalk, giving the model a "predator creeping"
 * animation before the attack.
 */
public class SpiritBeastHuntGoal extends Goal {

    private final SpiritBeastEntity beast;
    private final double speed;
    private int state; // 0=idle, 1=stalking, 2=charging
    private int stateTimer;
    private LivingEntity prey;

    public SpiritBeastHuntGoal(SpiritBeastEntity beast, double speed) {
        this.beast = beast;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (state != 0) return false;
        // Find weaker prey within follow range
        double range = beast.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE);
        List<LivingEntity> candidates = beast.level().getEntitiesOfClass(
                LivingEntity.class,
                beast.getBoundingBox().inflate(range, 4.0D, range),
                e -> e != beast && e.isAlive()
                        && !(e instanceof net.minecraft.world.entity.player.Player)
                        && e.getHealth() < beast.getHealth() * 1.5D);
        if (candidates.isEmpty()) return false;
        prey = candidates.get(beast.getRandom().nextInt(candidates.size()));
        return beast.getRandom().nextInt(40) == 0; // ~2.5% per tick
    }

    @Override
    public boolean canContinueToUse() {
        return prey != null && prey.isAlive() && state != 0;
    }

    @Override
    public void start() {
        beast.setTarget(prey);
        state = 1; // stalking
        stateTimer = 40 + beast.getRandom().nextInt(40); // 2-4 seconds of stalking
    }

    @Override
    public void stop() {
        state = 0;
        stateTimer = 0;
        prey = null;
        beast.setSpiritPose(SpiritBeastEntity.POSE_STANDING);
        // Don't clear target — MeleeAttackGoal may take over
    }

    @Override
    public void tick() {
        if (prey == null || !prey.isAlive()) {
            stop();
            return;
        }

        stateTimer--;

        if (state == 1) {
            // STALKING: creep toward prey at reduced speed
            beast.setSpiritPose(SpiritBeastEntity.POSE_CHARGING);
            beast.getLookControl().setLookAt(prey, 30.0F, 30.0F);
            beast.getNavigation().moveTo(prey, speed * 0.6D);

            if (beast.distanceToSqr(prey) < 9.0D || stateTimer <= 0) {
                state = 2; // switch to charging
                stateTimer = 20;
            }
        } else if (state == 2) {
            // CHARGING: sprint toward prey
            beast.setSpiritPose(SpiritBeastEntity.POSE_CHARGING);
            beast.getLookControl().setLookAt(prey, 30.0F, 30.0F);
            // BUG FIX 5: Trigger sprint burst via SprintMoveControl if available.
            // Previously SprintMoveControl.startSprint() was never called by any goal.
            var sprintCtrl = beast.getSprintMoveControl();
            if (sprintCtrl != null) sprintCtrl.startSprint();
            beast.getNavigation().moveTo(prey, speed * 1.3D);

            if (stateTimer <= 0) {
                state = 0;
            }
        }
    }
}
