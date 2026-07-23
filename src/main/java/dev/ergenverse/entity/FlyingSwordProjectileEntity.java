package dev.ergenverse.entity;

import dev.ergenverse.entity.projectile.ModProjectiles;
import dev.ergenverse.item.FlyingSwordItem;
import dev.ergenverse.item.sword.SwordEffectHelper;
import dev.ergenverse.item.sword.SwordEffectType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * FlyingSwordProjectileEntity — a streaking blade of qi launched by FlyingSwordItem.
 *
 * <p>Behavior: flies forward for 100 ticks (5s), homing toward the owner's last-hurt
 * target. On hit: deals damage + sword effect (teleport/lifesteal/poison/restriction)
 * + knockback + hit particles. After 100 ticks or on block hit: enters "returning"
 * phase — flies back to the owner (no-clip) and on arrival creates a return item.
 * If the owner is within 32 blocks and alive, the item is added directly to inventory.
 * Otherwise, the item is dropped at the projectile's current position.
 *
 * <p>Visual: Crit + Sweep + Enchant particle trail along the path.
 *
 * <p>CRON-COMPLETIONIST-55 upgrades:
 * <ul>
 *   <li>Owner UUID tracking for reliable owner resolution</li>
 *   <li>Sword effect application via SwordEffectHelper on hit</li>
 *   <li>NBT persistence of original item data (effect type, spirit name)</li>
 *   <li>Return-to-owner: creates return item and gives to owner or drops it</li>
 *   <li>Extended lifespan from 60 to 100 ticks</li>
 * </ul>
 */
public class FlyingSwordProjectileEntity extends ThrowableProjectile {

    private static final int MAX_LIFESPAN = 100;
    private static final int FORWARD_PHASE = 60;
    private static final float DEFAULT_DAMAGE = 8.0F;
    private static final double RETURN_RANGE = 32.0D;

    private float damage = DEFAULT_DAMAGE;
    private int lifespan = MAX_LIFESPAN;
    private boolean returning = false;
    private Entity owner;
    private UUID ownerUUID;

    /** Stores the original item's NBT data for effect persistence and return. */
    private CompoundTag swordData;

    /** The item stack that will be returned to the owner. */
    private ItemStack returnItem;

    public FlyingSwordProjectileEntity(EntityType<? extends FlyingSwordProjectileEntity> type, Level level) {
        super(type, level);
    }

    public FlyingSwordProjectileEntity(Level level, LivingEntity shooter) {
        super(ModProjectiles.FLYING_SWORD.get(), shooter, level);
        this.owner = shooter;
        this.ownerUUID = shooter.getUUID();
        this.setNoGravity(true);
    }

    public void setDamage(float damage) { this.damage = damage; }

    /**
     * Store the sword item's NBT data (effect type, spirit name, display name)
     * so effects persist on the return item and can be applied on hit.
     */
    public void setSwordData(CompoundTag data) {
        this.swordData = data.copy();
        // Create the return item with matching NBT
        this.returnItem = buildReturnItem();
    }

    /** Get the stored sword NBT data (may be null). */
    public CompoundTag getSwordData() {
        return swordData;
    }

    /** Get the effect type stored in sword data, or NONE. */
    public SwordEffectType getEffectType() {
        if (swordData != null && swordData.contains("SwordEffect")) {
            return SwordEffectType.byName(swordData.getString("SwordEffect"));
        }
        return SwordEffectType.NONE;
    }

    /**
     * Build the ItemStack that will be returned to the owner.
     * CRON-COMPLETIONIST-57: Uses stored SwordRegistryName to recreate the CORRECT
     * sword type. Previously hardcoded wealth_flying_sword, meaning launching a
     * Core Treasure Sword returned a Wealth Flying Sword.
     */
    private ItemStack buildReturnItem() {
        // Determine the correct item from stored registry name
        net.minecraft.world.item.Item swordItem = null;
        if (swordData != null && swordData.contains("SwordRegistryName")) {
            String regName = swordData.getString("SwordRegistryName");
            swordItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                    new net.minecraft.resources.ResourceLocation(regName));
        }
        // Fallback to wealth_flying_sword if registry lookup fails
        if (swordItem == null) {
            swordItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                    new net.minecraft.resources.ResourceLocation("ergenverse", "wealth_flying_sword"));
        }
        ItemStack toReturn = new ItemStack(swordItem != null ? swordItem
                : net.minecraft.world.item.Items.AIR, 1);
        if (swordData != null) {
            toReturn.getOrCreateTag().put("SwordData", swordData.copy());
        }
        return toReturn;
    }

    /**
     * Resolve the owner entity from the stored UUID.
     * Falls back to the direct owner reference if UUID lookup fails.
     */
    private LivingEntity resolveOwner() {
        if (owner != null && owner.isAlive()) {
            return owner instanceof LivingEntity le ? le : null;
        }
        if (ownerUUID != null && level() instanceof ServerLevel sl) {
            Entity resolved = sl.getEntity(ownerUUID);
            if (resolved instanceof LivingEntity le) {
                owner = le;
                return le;
            }
        }
        return null;
    }

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
            returnSwordToOwner();
            return;
        }

        // ── Homing during forward phase ──
        if (!returning) {
            LivingEntity resolvedOwner = resolveOwner();
            if (resolvedOwner != null) {
                Entity target = resolvedOwner.getLastHurtMob();
                if (target != null && target.isAlive() && distanceToSqr(target) < 1600.0D) {
                    Vec3 toTarget = target.position().add(0, 1, 0).subtract(position());
                    Vec3 current = getDeltaMovement();
                    // Lerp direction by 0.1/tick (gentle homing)
                    Vec3 newDir = current.normalize().scale(0.9).add(toTarget.normalize().scale(0.1));
                    setDeltaMovement(newDir.scale(current.length()));
                    hurtMarked = true;
                }
            }
        }

        // ── Returning phase: fly back to owner ──
        if (returning) {
            LivingEntity resolvedOwner = resolveOwner();
            if (resolvedOwner != null) {
                Vec3 toOwner = resolvedOwner.getEyePosition().subtract(position());
                double dist = toOwner.length();
                if (dist < 2.0D) {
                    giveItemToOwner(resolvedOwner);
                    return;
                }
                setDeltaMovement(toOwner.normalize().scale(1.5D));
                hurtMarked = true;
                // No-clip during return (set no-physics)
                noPhysics = true;
            } else {
                // Owner is gone or unloaded — drop the item at current position
                dropItemAtCurrentPosition();
                return;
            }
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
        if (target == owner || (ownerUUID != null && target.getUUID().equals(ownerUUID))) return;

        LivingEntity attacker = resolveOwner();
        if (target instanceof LivingEntity living) {
            living.hurt(damageSources().mobAttack(attacker != null ? attacker : null), damage);
            // Knockback
            Vec3 kb = living.position().subtract(position()).normalize().scale(0.5D);
            living.push(kb.x, 0.3D, kb.z);

            // ── Apply sword effect if present ──
            SwordEffectType effectType = getEffectType();
            if (effectType != SwordEffectType.NONE && level() instanceof ServerLevel sl) {
                SwordEffectHelper.applyEffect(sl, living,
                        attacker instanceof Player player ? player : null,
                        damage, effectType);
            }
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

    /**
     * Return the flying sword to its owner.
     * If owner is within 32 blocks and alive, add item to inventory.
     * Otherwise, drop at current position.
     */
    private void returnSwordToOwner() {
        LivingEntity resolvedOwner = resolveOwner();
        if (resolvedOwner != null && distanceToSqr(resolvedOwner) <= RETURN_RANGE * RETURN_RANGE) {
            giveItemToOwner(resolvedOwner);
        } else {
            dropItemAtCurrentPosition();
        }
    }

    /**
     * Give the return item directly to the owner's inventory.
     */
    private void giveItemToOwner(LivingEntity ownerEntity) {
        if (ownerEntity instanceof ServerPlayer serverPlayer && returnItem != null) {
            if (!serverPlayer.getInventory().add(returnItem.copy())) {
                // Inventory full — drop at player's feet
                serverPlayer.drop(returnItem.copy(), false);
            }
        } else if (ownerEntity instanceof Player player && returnItem != null) {
            if (!player.getInventory().add(returnItem.copy())) {
                player.drop(returnItem.copy(), false);
            }
        }
        discard();
    }

    /**
     * Drop the return item at the projectile's current position.
     */
    private void dropItemAtCurrentPosition() {
        if (returnItem != null && !level().isClientSide) {
            spawnAtLocation(returnItem.copy());
        }
        discard();
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
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        if (swordData != null) {
            tag.put("SwordData", swordData.copy());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Damage")) damage = tag.getFloat("Damage");
        if (tag.contains("Lifespan")) lifespan = tag.getInt("Lifespan");
        if (tag.contains("Returning")) returning = tag.getBoolean("Returning");
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
            if (level() instanceof ServerLevel sl) {
                Entity resolved = sl.getEntity(ownerUUID);
                if (resolved instanceof LivingEntity le) {
                    owner = le;
                }
            }
        }
        if (tag.contains("SwordData") && tag.get("SwordData") instanceof CompoundTag dataTag) {
            this.swordData = dataTag;
            this.returnItem = buildReturnItem();
        }
    }
}
