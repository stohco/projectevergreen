package dev.ergenverse.entity.ai;

import dev.ergenverse.simulation.actor.BeastIntelligence;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * BeastRestRecoverGoal — SPIRIT+ tier beasts and all cultivators rest to recover.
 *
 * <p>When safe (no hostile within 16 blocks) and health < 80%, the beast stops
 * moving and heals slowly. DEMON+ tier emits qi-gathering particles (visual
 * flavor for the cultivation rest). Heal rate scales with tier.
 *
 * <p>Self-critique: No qi recovery (entity has no recoverQi method). No "find a
 * safe lair" behavior — rests wherever it currently is. Heal rate is tier-flat,
 * not maxHP-scaled. For flying beasts, oscillates between rest and flight as
 * health crosses the 80% threshold (no hysteresis).
 */
public class BeastRestRecoverGoal extends Goal {

    private final Mob mob;
    private final BeastIntelligence tier;
    private final double healRate;
    private int restTimer;

    public BeastRestRecoverGoal(Mob mob, BeastIntelligence tier) {
        this.mob = mob;
        this.tier = tier;
        // Higher tier = faster heal
        this.healRate = 0.1D + tier.ordinal() * 0.1D;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mob.getTarget() != null) return false;
        if (mob.getHealth() >= mob.getMaxHealth() * 0.8D) return false;
        // Check no player within 16 blocks
        Player nearest = mob.level().getNearestPlayer(
                TargetingConditions.forNonCombat().range(16.0D), mob, mob.getX(), mob.getY(), mob.getZ());
        return nearest == null && mob.getRandom().nextInt(40) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return restTimer > 0 && mob.getTarget() == null && mob.getHealth() < mob.getMaxHealth();
    }

    @Override
    public void start() {
        restTimer = 100 + mob.getRandom().nextInt(100); // 5-10s rest
        mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        restTimer = 0;
    }

    @Override
    public void tick() {
        restTimer--;
        // Heal
        mob.heal((float) healRate);

        // DEMON+ qi-gathering particles
        if (tier.ordinal() >= BeastIntelligence.DEMON.ordinal() &&
                mob.level() instanceof net.minecraft.server.level.ServerLevel sl &&
                mob.tickCount % 10 == 0) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                    mob.getX(), mob.getY() + 1, mob.getZ(), 3, 0.5, 1, 0.5, 0.1);
        }
    }
}
