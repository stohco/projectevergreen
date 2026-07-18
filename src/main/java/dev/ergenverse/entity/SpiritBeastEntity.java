package dev.ergenverse.entity;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.Level;

/**
 * SpiritBeastEntity — base entity for spirit-beast mobs (rabbit, wolf, deer, hawk).
 *
 * <p>This is the unified shell for spirit-beast entities. The {@link BeastType}
 * enum distinguishes the four canon v1 beasts (RABBIT, WOLF, DEER, HAWK).
 * Each type selects its own texture in the {@link dev.ergenverse.client.render.SpiritBeastRenderer}.
 *
 * <h2>Canon</h2>
 * <p>Spirit beasts are animals that have absorbed ambient Qi and developed
 * spiritual nature. Their physical appearance is similar to mortal animals
 * (a Spirit Rabbit looks like a rabbit) but with spiritual markings, faint
 * glows, and (at higher tiers) cultivation. Mortals see "a strange rabbit";
 * cultivators see the spirit beast.
 *
 * <p>v1: four BeastTypes, simple wander AI, no combat yet. The renderer uses
 * the vanilla PigModel (suitable for a quadruped). v2 will add per-type models.
 *
 * <h2>Synced data</h2>
 * <p>{@link #DATA_BEAST_TYPE} is a synced string ("rabbit" | "wolf" | "deer" |
 * "hawk") used by the client renderer to pick the texture.
 */
public class SpiritBeastEntity extends PathfinderMob {

    /** The four v1 beast types. */
    public enum BeastType {
        RABBIT("rabbit"),
        WOLF("wolf"),
        DEER("deer"),
        HAWK("hawk"),
        FIRE_BEAST("fire_beast"),
        STONE_BACK_BOAR("stone_back_boar");

        public final String id;
        BeastType(String id) { this.id = id; }

        public static BeastType byId(String id) {
            for (BeastType t : values()) if (t.id.equals(id)) return t;
            return RABBIT;
        }
    }

    private static final EntityDataAccessor<String> DATA_BEAST_TYPE =
            SynchedEntityData.defineId(SpiritBeastEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Integer> DATA_CULTIVATION_TIER =
            SynchedEntityData.defineId(SpiritBeastEntity.class, EntityDataSerializers.INT);

    public SpiritBeastEntity(EntityType<? extends SpiritBeastEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BEAST_TYPE, "rabbit");
        this.entityData.define(DATA_CULTIVATION_TIER, 0);
    }

    public BeastType getBeastType() {
        return BeastType.byId(this.entityData.get(DATA_BEAST_TYPE));
    }

    public void setBeastType(BeastType type) {
        this.entityData.set(DATA_BEAST_TYPE, type.id);
    }

    public int getCultivationTier() {
        return this.entityData.get(DATA_CULTIVATION_TIER);
    }

    public void setCultivationTier(int tier) {
        this.entityData.set(DATA_CULTIVATION_TIER, tier);
    }

    @Override
    protected void registerGoals() {
        BeastType type = getBeastType();
        
        // Common to all: float in water
        this.goalSelector.addGoal(0, new FloatGoal(this));
        
        switch (type) {
            case WOLF -> {
                this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
                this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
                this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            }
            case RABBIT, DEER, STONE_BACK_BOAR -> {
                this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
                this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
                this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
            }
            case HAWK -> {
                this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
                this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
            }
            case FIRE_BEAST -> {
                this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
                this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8));
                this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
                this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        // Spirit beasts persist like canon entities — no despawn.
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("BeastType", getBeastType().id);
        compound.putInt("CultivationTier", getCultivationTier());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("BeastType")) {
            setBeastType(BeastType.byId(compound.getString("BeastType")));
        }
        if (compound.contains("CultivationTier")) {
            setCultivationTier(compound.getInt("CultivationTier"));
        }
    }
}
