package dev.ergenverse.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * MosquitoSwarmEntity — a composite server-side entity representing thousands
 * to hundreds of thousands of mosquito beasts as a SINGLE entity.
 *
 * <h2>Canon (Renegade Immortal)</h2>
 * <p>Blood-drinking insectoid swarm beast. Evolves from millions of
 * blood-drinking mosquitoes that absorbed spirit beast blood. Wang Lin
 * obtained his in Mosquito Valley. Worker 0.3m wingspan, Soldier 1m,
 * Elite 3m, Queen 10m, Ancient Queen 50m. Cultivation range:
 * Qi Condensation → Soul Formation.
 *
 * <h2>Design Rationale</h2>
 * <p>Spawning hundreds of thousands of individual mob entities would
 * obliterate server TPS. Instead, we model the entire swarm as a single
 * {@link Entity} with:
 * <ul>
 *   <li>A synced population counter ({@link #getMosquitoCount()})</li>
 *   <li>Population-scaled aggregate damage ({@link #getAggregateDamagePerTick()})</li>
 *   <li>Fission mechanics for swarm splitting ({@link #performFission})</li>
 *   <li>Area damage with population-based resilience ({@link #applyAreaDamage})</li>
 * </ul>
 *
 * <h2>Synced Data</h2>
 * <ul>
 *   <li>{@code DATA_POPULATION} — total mosquito count (0–500,000)</li>
 *   <li>{@code DATA_SWARM_RADIUS} — visual bounding radius (auto-scaled)</li>
 *   <li>{@code DATA_FISSION_TIMER} — countdown during fission animation</li>
 *   <li>{@code DATA_IS_CHILD} — whether this swarm was produced by fission</li>
 *   <li>{@code DATA_PARENT_ID} — entity ID of the parent swarm (if child)</li>
 *   <li>{@code DATA_PARENT_ORIGIN_X/Y/Z} — parent position at fission moment</li>
 * </ul>
 */
public class MosquitoSwarmEntity extends Entity {

    // ── Synced data accessors ──────────────────────────────────────────

    private static final EntityDataAccessor<Integer> DATA_POPULATION =
            SynchedEntityData.defineId(MosquitoSwarmEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_SWARM_RADIUS =
            SynchedEntityData.defineId(MosquitoSwarmEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_FISSION_TIMER =
            SynchedEntityData.defineId(MosquitoSwarmEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHILD =
            SynchedEntityData.defineId(MosquitoSwarmEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_PARENT_ID =
            SynchedEntityData.defineId(MosquitoSwarmEntity.class, EntityDataSerializers.INT);
    // Store parent origin for client-side interpolation during fission.
    // NOTE: EntityDataSerializers has no DOUBLE in 1.20.1 — use FLOAT (sufficient
    // precision for 2-second visual interpolation).
    private static final EntityDataAccessor<Float> DATA_PARENT_ORIGIN_X =
            SynchedEntityData.defineId(MosquitoSwarmEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PARENT_ORIGIN_Y =
            SynchedEntityData.defineId(MosquitoSwarmEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PARENT_ORIGIN_Z =
            SynchedEntityData.defineId(MosquitoSwarmEntity.class, EntityDataSerializers.FLOAT);

    // ── Swarm behavior constants ──────────────────────────────────────

    /** Below this count, the swarm is too small to be visible/threatening. */
    public static final int MIN_VISIBLE_POPULATION = 100;
    /** Maximum population cap. */
    public static final int MAX_POPULATION = 500_000;
    /** Base damage per mosquito per tick (scales with log2 of population). */
    public static final float BASE_MOSQUITO_ATTACK = 2.0F;
    /** Fission animation duration in ticks (2 seconds at 20 tps). */
    public static final int FISSION_DURATION = 40;

    // ── Pathfinding state (not synced — server-only) ───────────────────

    @Nullable
    private Vec3 targetPosition;
    private int idleTimer;

    // ── Constructor ────────────────────────────────────────────────────

    public MosquitoSwarmEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true; // swarm ignores terrain collision
        this.targetPosition = null;
        this.idleTimer = 0;
    }

    // ── Synched data ───────────────────────────────────────────────────

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_POPULATION, 10000);
        this.entityData.define(DATA_SWARM_RADIUS, 8.0F);
        this.entityData.define(DATA_FISSION_TIMER, 0);
        this.entityData.define(DATA_IS_CHILD, false);
        this.entityData.define(DATA_PARENT_ID, -1);
        this.entityData.define(DATA_PARENT_ORIGIN_X, 0.0F);
        this.entityData.define(DATA_PARENT_ORIGIN_Y, 0.0F);
        this.entityData.define(DATA_PARENT_ORIGIN_Z, 0.0F);
    }

    // ── Population ─────────────────────────────────────────────────────

    public int getMosquitoCount() {
        return this.entityData.get(DATA_POPULATION);
    }

    public void setMosquitoCount(int count) {
        this.entityData.set(DATA_POPULATION, Math.max(0, Math.min(count, MAX_POPULATION)));
        updateSwarmRadius();
    }

    // ── Swarm radius (visual bounding) ────────────────────────────────

    public float getSwarmRadius() {
        return this.entityData.get(DATA_SWARM_RADIUS);
    }

    /**
     * Radius scales with the cube root of population (volume ~ population).
     * A swarm of 10,000 has radius ~8; 100,000 has radius ~14.8; 500,000 has radius ~20.
     */
    private void updateSwarmRadius() {
        int pop = getMosquitoCount();
        float targetRadius = (float) (2.0 + 6.0 * Math.cbrt(pop / 10000.0));
        this.entityData.set(DATA_SWARM_RADIUS, targetRadius);
    }

    // ── Fission mechanics ─────────────────────────────────────────────

    public int getFissionTimer() {
        return this.entityData.get(DATA_FISSION_TIMER);
    }

    public boolean isChild() {
        return this.entityData.get(DATA_IS_CHILD);
    }

    @Nullable
    public Vec3 getParentOrigin() {
        if (this.entityData.get(DATA_PARENT_ID) < 0) return null;
        return new Vec3(
                this.entityData.get(DATA_PARENT_ORIGIN_X).doubleValue(),
                this.entityData.get(DATA_PARENT_ORIGIN_Y).doubleValue(),
                this.entityData.get(DATA_PARENT_ORIGIN_Z).doubleValue()
        );
    }

    /**
     * Server-side fission: split this swarm into parent + child.
     * Returns the new child entity (spawned at same position, moves to childTarget).
     *
     * @param childPopulation number of mosquitoes to allocate to the child
     * @param childTarget     position the child swarm will move toward
     * @return the child entity, or null if parameters are invalid
     */
    @Nullable
    public MosquitoSwarmEntity performFission(int childPopulation, Vec3 childTarget) {
        if (childPopulation >= getMosquitoCount() || childPopulation <= 0) return null;

        // Deduct from parent
        setMosquitoCount(getMosquitoCount() - childPopulation);

        // Create child at exact same position
        MosquitoSwarmEntity child = new MosquitoSwarmEntity(EREntityTypes.MOSQUITO_SWARM.get(), level());
        child.copyPosition(this);
        child.setMosquitoCount(childPopulation);
        child.entityData.set(DATA_IS_CHILD, true);
        child.entityData.set(DATA_PARENT_ID, this.getId());
        child.entityData.set(DATA_FISSION_TIMER, FISSION_DURATION);
        // Store current position as parent origin for client interpolation
        child.entityData.set(DATA_PARENT_ORIGIN_X, (float) this.getX());
        child.entityData.set(DATA_PARENT_ORIGIN_Y, (float) this.getY());
        child.entityData.set(DATA_PARENT_ORIGIN_Z, (float) this.getZ());
        child.targetPosition = childTarget;

        if (!level().isClientSide) {
            level().addFreshEntity(child);
        }
        return child;
    }

    // ── Aggregate damage ───────────────────────────────────────────────

    /**
     * Damage per tick formula: Base × log₂(population).
     * <p>A swarm of 50,000 mosquitoes deals: 2.0 × log₂(50000) ≈ 2.0 × 15.6 = 31.2 damage/tick.
     */
    public float getAggregateDamagePerTick() {
        int pop = getMosquitoCount();
        if (pop <= 0) return 0;
        return BASE_MOSQUITO_ATTACK * (float) (Math.log(pop) / Math.log(2));
    }

    /**
     * Apply area damage to the swarm. Returns the actual number of mosquitoes killed.
     * <p>Higher population swarms are more resilient per-individual — damage is
     * spread across the swarm logarithmically. Maximum 10% of the population can
     * be killed by a single hit.
     *
     * @param damageAmount raw damage incoming
     * @return number of mosquitoes killed
     */
    public int applyAreaDamage(float damageAmount) {
        int pop = getMosquitoCount();
        if (pop <= 0) return 0;

        // Higher population = more resilient per-individual (spread damage)
        float effectiveDamage = damageAmount / (1.0F + (float) Math.log10(pop));
        int killed = (int) (pop * Math.min(effectiveDamage / 100.0F, 0.1F)); // max 10% per hit
        setMosquitoCount(pop - killed);
        return killed;
    }

    // ── Tick ───────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) return; // server-only logic below

        // Advance fission timer
        if (getFissionTimer() > 0) {
            this.entityData.set(DATA_FISSION_TIMER, getFissionTimer() - 1);
        }

        // Remove if population depleted
        if (getMosquitoCount() <= 0) {
            this.discard();
            return;
        }

        // Simple pathfinding: move toward target or idle drift
        if (targetPosition != null) {
            Vec3 dir = targetPosition.subtract(this.position()).normalize();
            double speed = 0.5; // swarm moves slowly
            this.setDeltaMovement(dir.scale(speed));

            if (this.position().distanceTo(targetPosition) < 5.0) {
                targetPosition = null;
            }
        } else {
            idleTimer++;
            if (idleTimer > 200) { // pick new idle target every 10 seconds
                idleTimer = 0;
                double angle = random.nextDouble() * Math.PI * 2;
                double dist = 20 + random.nextDouble() * 40;
                targetPosition = new Vec3(
                        this.getX() + Math.cos(angle) * dist,
                        this.getY() + (random.nextDouble() - 0.5) * 10,
                        this.getZ() + Math.sin(angle) * dist
                );
            }
            // Gentle floating motion
            this.setDeltaMovement(
                    (random.nextDouble() - 0.5) * 0.1,
                    (random.nextDouble() - 0.5) * 0.05,
                    (random.nextDouble() - 0.5) * 0.1
            );
        }

        // Attack entities within swarm radius.
        // MosquitoSwarmEntity extends Entity (not LivingEntity), so getEntitiesOfClass
        // for LivingEntity will never return this swarm — no self-exclusion needed.
        float radius = getSwarmRadius();
        AABB swarmBox = getBoundingBox().inflate(radius);
        List<net.minecraft.world.entity.LivingEntity> entities = level().getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class, swarmBox
        );
        for (net.minecraft.world.entity.LivingEntity target : entities) {
            // damageSources().mobAttack() requires a LivingEntity source; the swarm is not
            // one, so pass null (generic mob-source damage). The swarm is the conceptual attacker.
            target.hurt(
                    level().damageSources().mobAttack(null),
                    getAggregateDamagePerTick() * 0.05F // per-tick portion to nearby entities
            );
        }
    }

    // ── NBT serialization ──────────────────────────────────────────────

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setMosquitoCount(tag.getInt("Population"));
        this.entityData.set(DATA_IS_CHILD, tag.getBoolean("IsChild"));
        this.entityData.set(DATA_PARENT_ID, tag.getInt("ParentId"));
        if (tag.contains("TargetX")) {
            targetPosition = new Vec3(tag.getDouble("TargetX"), tag.getDouble("TargetY"), tag.getDouble("TargetZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Population", getMosquitoCount());
        tag.putBoolean("IsChild", isChild());
        tag.putInt("ParentId", this.entityData.get(DATA_PARENT_ID));
        if (targetPosition != null) {
            tag.putDouble("TargetX", targetPosition.x);
            tag.putDouble("TargetY", targetPosition.y);
            tag.putDouble("TargetZ", targetPosition.z);
        }
    }

    // ── Combat ─────────────────────────────────────────────────────────

    @Override
    public boolean isPickable() {
        return true; // can be targeted by attacks
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide) {
            int killed = applyAreaDamage(amount);
            // Visual/audio feedback would go here
            return killed > 0;
        }
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        // When a swarm dies, could scatter mosquitoes as ambient particles (future)
    }
}