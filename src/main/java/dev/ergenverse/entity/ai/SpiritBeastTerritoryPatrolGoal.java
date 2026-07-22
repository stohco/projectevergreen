package dev.ergenverse.entity.ai;

import dev.ergenverse.simulation.actor.BeastIntelligence;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * SpiritBeastTerritoryPatrolGoal — SPIRIT+ tier beasts patrol and defend territory.
 *
 * <p>Patrols a ring of waypoints around a home position (the beast's spawn point).
 * If an intruder enters the territory radius, the beast switches to defense —
 * attacks if the intruder is weaker, flees if stronger. ANCIENT+ tier emits an
 * intimidation roar (particle + sound effect) when an intruder enters.
 *
 * <p>Territory radius scales with tier: SPIRIT=16, DEMON=24, ANCIENT=40, OLD_MONSTER=64.
 *
 * <p>Self-critique: Home position is first-start position (real fix needs NBT
 * DataAccessor on entity). Patrol yields permanently when target is set; if the
 * entity has no MeleeAttackGoal, patrol sets target and nothing attacks. Player
 * power is hardcoded. Roar cooldown is flat regardless of intruder count.
 */
public class SpiritBeastTerritoryPatrolGoal extends Goal {

    private final Mob mob;
    private final BeastIntelligence tier;
    private final double speed;
    private final double radius;
    private Vec3 home;
    private int waypointIndex;
    private int patrolCooldown;
    private int roarCooldown;

    public SpiritBeastTerritoryPatrolGoal(Mob mob, BeastIntelligence tier, double speed) {
        this.mob = mob;
        this.tier = tier;
        this.speed = speed;
        this.radius = BeastIntelligenceGoalFactory.territoryRadius(tier);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mob.getTarget() != null) return false; // don't patrol while in combat
        return mob.getRandom().nextInt(100) == 0; // ~5% chance per tick
    }

    @Override
    public boolean canContinueToUse() {
        return mob.getTarget() == null && patrolCooldown > 0;
    }

    @Override
    public void start() {
        if (home == null) {
            home = mob.position();
        }
        patrolCooldown = 200 + mob.getRandom().nextInt(200); // 10-20s patrol
        pickNextWaypoint();
    }

    @Override
    public void stop() {
        patrolCooldown = 0;
    }

    @Override
    public void tick() {
        patrolCooldown--;

        // ── Scan for intruders ──
        if (roarCooldown > 0) roarCooldown--;
        AABB territory = new AABB(home.x - radius, home.y - 8, home.z - radius,
                                   home.x + radius, home.y + 8, home.z + radius);
        List<LivingEntity> intruders = mob.level().getEntitiesOfClass(LivingEntity.class, territory,
                e -> e != mob && e.isAlive() && e instanceof net.minecraft.world.entity.player.Player);

        if (!intruders.isEmpty()) {
            LivingEntity intruder = intruders.get(0);

            // ANCIENT+ intimidation roar
            if (tier.ordinal() >= BeastIntelligence.ANCIENT.ordinal() && roarCooldown <= 0) {
                roarCooldown = 200; // 10s
                mob.playSound(net.minecraft.sounds.SoundEvents.RAVAGER_ROAR, 2.0F, 0.7F);
                if (mob.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER,
                            mob.getX(), mob.getY() + 2, mob.getZ(), 8, 1, 1, 1, 0.1);
                }
            }

            // Attack if intruder is weaker, flee if stronger
            if (intruder.getHealth() < mob.getHealth() * 0.9D) {
                mob.setTarget(intruder);
            } else if (tier.ordinal() >= BeastIntelligence.CUNNING.ordinal()) {
                // Flee — BeastSmartFleeGoal will handle it
                patrolCooldown = 0;
            }
            return;
        }

        // ── Patrol toward current waypoint ──
        if (home == null) return;
        double angle = (waypointIndex * Math.PI / 3.0); // 6 waypoints in a hexagon
        double wx = home.x + Math.cos(angle) * radius * 0.7;
        double wz = home.z + Math.sin(angle) * radius * 0.7;

        if (mob.distanceToSqr(wx, mob.getY(), wz) < 16.0D) {
            waypointIndex = (waypointIndex + 1) % 6;
        } else {
            mob.getNavigation().moveTo(wx, mob.getY(), wz, speed);
        }
    }

    private void pickNextWaypoint() {
        waypointIndex = mob.getRandom().nextInt(6);
    }
}
