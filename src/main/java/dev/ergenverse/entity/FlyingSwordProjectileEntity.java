package dev.ergenverse.entity;

import dev.ergenverse.entity.projectile.ModProjectiles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * FlyingSwordProjectileEntity — a streaking blade of qi launched by FlyingSwordItem.
 *
 * <p>Behavior: flies forward for 40 ticks (2s), homing toward the owner's last-hurt
 * target. On hit: deals damage + knockback + hit particles. After 40 ticks or on
 * block hit: enters "returning" phase — flies back to the owner (no-clip) and
 * despawns on arrival. Max lifespan 60 ticks (3s).
 *
 * <p>Visual: Crit + Sweep + Enchant particle trail along the path.
 *
 * <p>Self-critique: Return-to-owner is no-clip through walls (canon swords pathfind
 * via divine sense). Homing only tracks lastHurtMob, not a designated target.
 * No renderer registered yet — the projectile is invisible except for its particle
 * trail. A real flying sword should render as a spinning blade model.
 */
public class FlyingSwordProjectileEntity extends ThrowableProjectile {

    private static final int MAX_LIFESPAN = 60;
    private static final int FORWARD_PHASE = 40;
    private static final float DEFAULT_DAMAGE = 8.0F;

    private float damage = DEFAULT_DAMAGE;
    private int lifespan = MAX_LIFESPAN;
    private boolean returning = false;
    private Entity owner;

    public FlyingSwordProjectileEntity(EntityType<? extends FlyingSwordProjectileEntity> type, Level level) {
        super(type, level);
    }

    public FlyingSwordProjectileEntity(Level level, LivingEntity shooter) {
        super(ModProjectiles.FLYING_SWORD.get(), shooter, level);
        this.owner = shooter;
        this.setNoGravity(true);
    }

    public void setDamage(float damage) { this.damage = damage; }

    @Override
    protected void defineSynchedData() {
        // No additional synced data needed
    }

    @Override
    public void tick() {
        super.tick();
        lifespan--;

        // Particle trail
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 2, 0.1, 0.1, 0.1, 0.05);
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        }

        if (lifespan <= 0) {
            discard();
            return;
        }

        // ── Homing during forward phase ──
        if (!returning && owner instanceof LivingEntity livingOwner) {
            Entity target = livingOwner.getLastHurtMob();
            if (target != null && target.isAlive() && distanceToSqr(target) < 1600.0D) {
                Vec3 toTarget = target.position().add(0, 1, 0).subtract(position());
                Vec3 current = getDeltaMovement();
                // Lerp direction by 0.1/tick (gentle homing)
                Vec3 newDir = current.normalize().scale(0.9).add(toTarget.normalize().scale(0.1));
                setDeltaMovement(newDir.scale(current.length()));
                hurtMarked = true;
            }
        }

        // ── Returning phase: fly back to owner ──
        if (returning && owner != null && owner.isAlive()) {
            Vec3 toOwner = owner.getEyePosition().subtract(position());
            double dist = toOwner.length();
            if (dist < 2.0D) {
                discard();
                return;
            }
            setDeltaMovement(toOwner.normalize().scale(1.5D));
            hurtMarked = true;
            // No-clip during return (set no-physics)
            noPhysics = true;
        }

        // Enter returning phase after forward phase ends
        if (!returning && lifespan <= MAX_LIFESPAN - FORWARD_PHASE) {
            returning = true;
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (returning) return; // returning sword doesn't damage
        Entity target = result.getEntity();
        if (target == owner) return;
        if (target instanceof LivingEntity living) {
            living.hurt(damageSources().mobAttack(owner instanceof LivingEntity lo ? lo : null), damage);
            // Knockback
            Vec3 kb = living.position().subtract(position()).normalize().scale(0.5D);
            living.push(kb.x, 0.3D, kb.z);
        }
        // Hit particles
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY(), getZ(), 3, 0.3, 0.3, 0.3, 0);
            sl.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 5, 0.3, 0.3, 0.3, 0.1);
        }
        // Start returning after a hit
        returning = true;
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        if (returning) return;
        // Bounce once, then start returning
        if (lifespan > MAX_LIFESPAN - FORWARD_PHASE + 10) {
            // Still in early forward phase — bounce
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x * -0.3, motion.y * -0.3, motion.z * -0.3);
            hurtMarked = true;
        } else {
            returning = true;
        }
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 3, 0.2, 0.2, 0.2, 0.05);
        }
    }

    @Override
    public boolean isPickable() { return false; }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) { return false; }

    @Override
    protected float getGravity() { return 0.0F; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", damage);
        tag.putInt("Lifespan", lifespan);
        tag.putBoolean("Returning", returning);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Damage")) damage = tag.getFloat("Damage");
        if (tag.contains("Lifespan")) lifespan = tag.getInt("Lifespan");
        if (tag.contains("Returning")) returning = tag.getBoolean("Returning");
    }
}
