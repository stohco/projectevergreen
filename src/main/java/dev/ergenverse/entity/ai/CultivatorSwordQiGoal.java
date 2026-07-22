package dev.ergenverse.entity.ai;

import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.entity.EntityCultivator;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * CultivatorSwordQiGoal — ranged sword-qi attack for cultivators (Qi Condensation+).
 *
 * <p>The cultivator projects a blade of qi toward the target. Visually: a streak
 * of sweep + enchant particles along the path. Mechanically: after a 10-tick
 * "travel" delay, damage is applied to the target if still in line of sight.
 *
 * <p>This is a SIMULATED projectile (particle + delayed damage) rather than a
 * real FlyingSwordProjectileEntity — that exists as a separate item-launched
 * projectile for the player. NPC cultivators use this simplified version.
 *
 * <p>Damage scales by realm (same table as CultivatorCombatGoal, but ranged
 * gets a 0.7× multiplier — melee hits harder, ranged is safer).
 *
 * <p>Self-critique: NOT a real projectile (no entity, no block collision, no
 * dodging). LOS checked only at charge start — target can break LOS mid-charge.
 * Single-target only (canon: sword-qi can sweep multiple targets). No sound.
 * Particle trail is sparse. A real implementation should spawn FlyingSwordProjectileEntity.
 */
public class CultivatorSwordQiGoal extends Goal {

    private final Mob mob;
    private int cooldown;
    private int chargeTimer;

    public CultivatorSwordQiGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        int realm = getRealmOrdinal();
        if (realm < 1) return false; // need at least QI_CONDENSATION to project sword qi

        double distSq = mob.distanceToSqr(target);
        if (distSq < 25.0D || distSq > 324.0D) return false; // 5-18 blocks

        // Check line of sight
        return hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        return chargeTimer > 0;
    }

    @Override
    public void start() {
        chargeTimer = 10; // 0.5s charge
    }

    @Override
    public void stop() {
        chargeTimer = 0;
        cooldown = getCooldownForRealm(getRealmOrdinal());
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            chargeTimer = 0;
            return;
        }

        chargeTimer--;

        // Face the target during charge
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (chargeTimer == 5) {
            // Mid-charge: charging particles at the cultivator's hand
            if (mob.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANT,
                        mob.getX(), mob.getY() + 1.2, mob.getZ(), 8, 0.3, 0.5, 0.3, 0.05);
            }
        }

        if (chargeTimer <= 0) {
            // Fire!
            fireSwordQi(target);
            cooldown = getCooldownForRealm(getRealmOrdinal());
        }
    }

    private void fireSwordQi(LivingEntity target) {
        if (!(mob.level() instanceof ServerLevel sl)) return;

        int realm = getRealmOrdinal();
        float damage = CultivatorCombatGoal.getDamageForRealm(realm) * 0.7F; // ranged = 70% melee

        // Particle trail from cultivator to target
        Vec3 start = mob.getEyePosition();
        Vec3 end = target.getEyePosition();
        Vec3 dir = end.subtract(start);
        double dist = dir.length();
        dir = dir.normalize();

        // Spawn particles along the path
        for (double d = 0; d < dist; d += 1.0D) {
            Vec3 p = start.add(dir.scale(d));
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            if (d % 2 == 0) {
                sl.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.05);
            }
        }

        // Play a sword-swish sound
        mob.playSound(net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, 1.5F, 1.5F);

        // Apply damage if target still has line of sight (hasn't dodged behind cover)
        if (hasLineOfSight(target)) {
            target.hurt(mob.damageSources().mobAttack(mob), damage);
            // Knockback
            Vec3 kb = target.position().subtract(mob.position()).normalize().scale(0.5D);
            target.push(kb.x, 0.2D, kb.z);
        }
    }

    private boolean hasLineOfSight(LivingEntity target) {
        Vec3 eye = mob.getEyePosition();
        Vec3 targetEye = target.getEyePosition();
        ClipContext ctx = new ClipContext(eye, targetEye,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob);
        return mob.level().clip(ctx).getType() == HitResult.Type.MISS;
    }

    private int getRealmOrdinal() {
        if (mob instanceof EntityCultivator ec) {
            String realmStr = ec.getCultivationRealm();
            try {
                return RealmId.valueOf(realmStr.toUpperCase()).ordinal();
            } catch (IllegalArgumentException e) {
                return 0;
            }
        }
        return 0;
    }

    /** Higher realm = shorter cooldown (more frequent sword-qi). */
    public static int getCooldownForRealm(int realmOrdinal) {
        if (realmOrdinal <= 1) return 80;   // 4s — Qi Condensation
        if (realmOrdinal <= 3) return 60;   // 3s — Foundation/Core
        if (realmOrdinal <= 5) return 40;   // 2s — Nascent/Soul Formation
        return 25;                          // 1.25s — Soul Transformation+
    }
}
