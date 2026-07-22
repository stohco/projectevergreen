package dev.ergenverse.entity.ai;

import dev.ergenverse.simulation.actor.BeastIntelligence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * SpiritBeastAmbushGoal — CUNNING-tier beasts hide and leap at prey.
 *
 * <p>Behavior: stop moving (hide), wait 2-4 seconds, then leap toward prey
 * with a burst of speed. After the leap, cooldown 10-20 seconds.
 *
 * <p>Self-critique: "Hide" is just holding still (no model flag for crouch).
 * Leap arc is fixed upward. No stealth check (beast hides in the open).
 * A real ambush AI would pathfind to cover, crouch, and use line-of-sight breaks.
 */
public class SpiritBeastAmbushGoal extends Goal {

    private final Mob mob;
    private final double leapSpeed;
    private int state; // 0=idle, 1=hiding, 2=leaping, 3=recovering
    private int stateTimer;
    private LivingEntity prey;

    public SpiritBeastAmbushGoal(Mob mob, double leapSpeed) {
        this.mob = mob;
        this.leapSpeed = leapSpeed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (state != 0) return false;
        // Find weaker prey within 16 blocks
        List<LivingEntity> nearby = mob.level().getEntitiesOfClass(LivingEntity.class,
                mob.getBoundingBox().inflate(16.0D),
                e -> e != mob && e.isAlive() && e.getHealth() < mob.getHealth());
        if (nearby.isEmpty()) return false;
        prey = nearby.get(mob.getRandom().nextInt(nearby.size()));
        return mob.getRandom().nextInt(60) == 0; // ~3% chance per tick
    }

    @Override
    public boolean canContinueToUse() {
        return state != 0 && (prey == null || prey.isAlive());
    }

    @Override
    public void start() {
        state = 1; // hiding
        stateTimer = 40 + mob.getRandom().nextInt(40); // 2-4 seconds
        mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        state = 0;
        stateTimer = 0;
        prey = null;
    }

    @Override
    public void tick() {
        if (prey == null || !prey.isAlive()) {
            state = 0;
            return;
        }

        // Look at prey while hiding
        mob.getLookControl().setLookAt(prey, 30.0F, 30.0F);

        if (state == 1) {
            // Hiding — hold still, wait for timer
            stateTimer--;
            if (stateTimer <= 0) {
                state = 2; // leap!
            }
        } else if (state == 2) {
            // Leap toward prey
            Vec3 leap = prey.position().subtract(mob.position()).normalize().scale(leapSpeed);
            leap = leap.add(0, 0.45D, 0); // arc upward
            mob.setDeltaMovement(leap);
            mob.hurtMarked = true;
            state = 3;
            stateTimer = 10; // brief recovery after leap
        } else if (state == 3) {
            stateTimer--;
            if (mob.distanceToSqr(prey) < 4.0D) {
                mob.doHurtTarget(prey);
            }
            if (stateTimer <= 0) {
                // Cooldown before next ambush
                state = 0;
                stateTimer = 200 + mob.getRandom().nextInt(200); // 10-20s
            }
        }
    }
}
