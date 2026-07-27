package dev.ergenverse.entity.ai;

import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
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
 * <p><b>CRON-138 — qi expenditure (closes CRON-134 self-critique #8).</b>
 * Sword-qi projections now consume qi (5.0 absolute units per shot). The
 * canUse() gate refuses activation below 5% of maxQi (same threshold as
 * flight continuation). fireSwordQi() consumes the qi at fire time; if
 * consumption fails (race condition: qi dropped between canUse and fire),
 * the projection is aborted (no damage, no particle, no sound) and the
 * cultivator must wait for regen. Canon: all cultivation abilities consume
 * qi — a cultivator who exhausted their qi on flight cannot immediately
 * project sword-qi. The cost is calibrated against CRON-134's maxQi scale:
 * Foundation (maxQi=100) gets 20 shots before exhaustion; Core (500) gets
 * 100; Nascent (2000) gets 400; Soul+ (10000) gets 2000 — effectively
 * unlimited at high realms. NO explicit 仙逆 chapter citation quantifying
 * sword-qi qi cost; the mechanic is mod-original interpretation grounded
 * in xianxia genre convention (universally attested — sword-qi projection
 * requires 真元 output).
 *
 * <p>Self-critique: NOT a real projectile (no entity, no block collision, no
 * dodging). LOS checked only at charge start — target can break LOS mid-charge.
 * Single-target only (canon: sword-qi can sweep multiple targets). No sound.
 * Particle trail is sparse. A real implementation should spawn FlyingSwordProjectileEntity.
 */
public class CultivatorSwordQiGoal extends Goal {

    /**
     * CRON-138: Qi cost per sword-qi projection (absolute units). 5.0 qi per
     * shot — calibrated against CRON-134's maxQi scale:
     * <ul>
     *   <li>Foundation (maxQi=100): 20 shots before exhaustion</li>
     *   <li>Core (maxQi=500): 100 shots</li>
     *   <li>Nascent Soul (maxQi=2000): 400 shots</li>
     *   <li>Soul+ (maxQi=10000): 2000 shots (effectively unlimited)</li>
     * </ul>
     * The 5.0 value is ≈2.5s of flight equivalent (flight costs 0.2/tick =
     * 4/sec). A sword-qi projection is a brief, focused expenditure — more
     * intense than a single flight tick but less than sustained flight.
     * Mod-original; NO canon citation.
     */
    public static final double SWORD_QI_QI_COST = 5.0D;

    private final Mob mob;
    private int cooldown;
    private int chargeTimer;
    /**
     * CRON-138: Tracks whether the qi gate passed at canUse() time. If qi
     * drops between canUse and fireSwordQi (e.g., flight goal consumed it),
     * fireSwordQi will re-check and abort. This field is informational —
     * the authoritative check is in fireSwordQi via consumeQi.
     */
    private boolean qiGatePassedAtActivation;

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

        // CRON-138: Qi activation gate. Refuses activation below 5% of maxQi.
        // Closes CRON-134 self-critique #8: sword-qi now costs qi.
        if (mob instanceof EntityCultivator ec) {
            if (!ec.hasEnoughQiForSwordQi()) {
                return false;
            }
        }

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
        qiGatePassedAtActivation = true; // CRON-138: gate checked in canUse()
    }

    @Override
    public void stop() {
        chargeTimer = 0;
        qiGatePassedAtActivation = false;
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

        // CRON-138: Consume qi at fire time. This is the authoritative check —
        // even if canUse() passed, qi may have dropped during the 10-tick
        // charge (e.g., flight goal consumed it). If consumption fails, abort
        // the projection entirely (no damage, no particle, no sound). The
        // cultivator must wait for regen before retrying.
        if (mob instanceof EntityCultivator ec) {
            boolean consumed = ec.consumeQi(SWORD_QI_QI_COST);
            if (!consumed) {
                Ergenverse.LOGGER.warn("[Ergenverse] CultivatorSwordQiGoal: qi insufficient at fire time " +
                                "(needed {}, had {}) — aborting projection. Cultivator must regen before retrying.",
                        String.format(java.util.Locale.ROOT, "%.1f", SWORD_QI_QI_COST),
                        String.format(java.util.Locale.ROOT, "%.1f", ec.getQi()));
                return;
            }
            Ergenverse.LOGGER.debug("[Ergenverse] CultivatorSwordQiGoal: fired sword-qi, consumed {} qi, remaining {}/{} ({})",
                    String.format(java.util.Locale.ROOT, "%.1f", SWORD_QI_QI_COST),
                    String.format(java.util.Locale.ROOT, "%.1f", ec.getQi()),
                    String.format(java.util.Locale.ROOT, "%.1f", ec.getMaxQi()),
                    String.format(java.util.Locale.ROOT, "%.0f%%", ec.getQiFraction() * 100));
        }

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
