package dev.ergenverse.entity.ai;

import dev.ergenverse.simulation.actor.BeastIntelligence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * BeastSmartFleeGoal — flee from stronger opponents; counterattack if cornered.
 *
 * <p>CUNNING+ tier: if cornered (backed against a wall), turn and fight.
 * OLD_MONSTER tier: devastating AoE counter instead of simple melee.
 *
 * <p>Self-critique: Cornered detection is one-tick (easy to miss). Flee direction
 * is naive (no cliff/water avoidance). OLD_MONSTER AoE counter is flat damage
 * (canon: realm-scaled, city-destroying at peak). Priority may conflict with
 * the entity's own MeleeAttackGoal.
 */
public class BeastSmartFleeGoal extends Goal {

    private final Mob mob;
    private final BeastIntelligence tier;
    private final double speed;
    private int fleeCooldown;
    private LivingEntity attacker;

    public BeastSmartFleeGoal(Mob mob, BeastIntelligence tier, double speed) {
        this.mob = mob;
        this.tier = tier;
        this.speed = speed;
    }

    @Override
    public boolean canUse() {
        if (fleeCooldown > 0) {
            fleeCooldown--;
            return false;
        }
        attacker = mob.getLastHurtByMob();
        if (attacker == null || !attacker.isAlive()) return false;
        // Flee if attacker is stronger (more health) OR is a player
        if (attacker instanceof net.minecraft.world.entity.player.Player) return true;
        return attacker.getHealth() > mob.getHealth() * 0.8D;
    }

    @Override
    public boolean canContinueToUse() {
        return attacker != null && attacker.isAlive() && mob.distanceToSqr(attacker) < 400.0D;
    }

    @Override
    public void tick() {
        if (attacker == null) return;

        // Check if cornered (horizontal collision = wall behind)
        boolean cornered = mob.horizontalCollision;

        if (cornered && tier.ordinal() >= BeastIntelligence.CUNNING.ordinal()) {
            // Cornered — turn and fight!
            mob.setTarget(attacker);
            if (mob.distanceToSqr(attacker) < 4.0D) {
                mob.doHurtTarget(attacker);
            } else {
                mob.getNavigation().moveTo(attacker, speed * 1.2D);
            }

            // OLD_MONSTER devastating AoE counter
            if (tier == BeastIntelligence.OLD_MONSTER) {
                double radius = 6.0D;
                AABB aoe = mob.getBoundingBox().inflate(radius);
                List<LivingEntity> hit = mob.level().getEntitiesOfClass(LivingEntity.class, aoe,
                        e -> e != mob && e.isAlive());
                for (LivingEntity e : hit) {
                    e.hurt(mob.damageSources().mobAttack(mob), 15.0F);
                    // Knockback
                    Vec3 kb = e.position().subtract(mob.position()).normalize().scale(1.5D);
                    e.push(kb.x, 0.3D, kb.z);
                }
                // Visual: explosion particles
                if (mob.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                            mob.getX(), mob.getY() + 1, mob.getZ(), 5, 1, 1, 1, 0);
                }
            }
        } else {
            // Flee away from attacker
            Vec3 fleeDir = mob.position().subtract(attacker.position()).normalize();
            Vec3 fleePos = mob.position().add(fleeDir.scale(10));
            mob.getNavigation().moveTo(fleePos.x, fleePos.y, fleePos.z, speed * 1.5D);
        }
    }

    @Override
    public void stop() {
        attacker = null;
        fleeCooldown = 60; // 3s before fleeing again
    }
}
