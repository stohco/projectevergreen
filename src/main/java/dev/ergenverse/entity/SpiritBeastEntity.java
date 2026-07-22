package dev.ergenverse.entity;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.ai.SpiritBeastGrazeGoal;
import dev.ergenverse.entity.ai.SpiritBeastHuntGoal;
import dev.ergenverse.entity.ai.SpiritBeastSwimGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * SpiritBeastEntity — base entity for spirit-beast mobs (rabbit, wolf, deer, hawk,
 * fire_beast, stone_back_boar).
 *
 * <p>CRON-COMPLETIONIST-13: Added DATA_POSE synced pose system (10 constants),
 * 3 new AI goals (GrazeGoal, HuntGoal, SwimGoal), type-appropriate MoveControls
 * (FlightMoveControl, WaterBoundMoveControl, SprintMoveControl), and tick()
 * heuristic pose auto-update.
 *
 * <p>Each type has its own per-species renderer in
 * {@link dev.ergenverse.client.render.SpiritBeastRenderers} which uses a
 * custom anatomically-correct model (SpiritWolfModel, SpiritHawkModel, etc.).
 */
public class SpiritBeastEntity extends PathfinderMob {

    /** The v1 beast types. */
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

        /** Movement category for MoveControl selection. */
        public boolean isFlyer()    { return this == HAWK; }
        public boolean isAquatic()  { return false; /* future: SEA_SERPENT, SOUL_FISH */ }
        public boolean isGround()   { return !isFlyer() && !isAquatic(); }
    }

    // ── Synced Pose System (DATA_POSE) ─────────────────────────────────────
    // 10 canonical pose constants. AI goals set the pose; models read it.
    // This is the bridge between AI state and visual animation.
    public static final int POSE_STANDING  = 0;
    public static final int POSE_GRAZING   = 1;
    public static final int POSE_RESTING    = 2;
    public static final int POSE_FLYING    = 3;
    public static final int POSE_SWIMMING  = 4;
    public static final int POSE_SPRINTING = 5;
    public static final int POSE_PERCHING  = 6;
    public static final int POSE_PLAYING   = 7;
    public static final int POSE_ALERT     = 8;
    public static final int POSE_CHARGING  = 9;

    private static final EntityDataAccessor<Integer> DATA_POSE =
            SynchedEntityData.defineId(SpiritBeastEntity.class, EntityDataSerializers.INT);

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
        this.entityData.define(DATA_POSE, POSE_STANDING);
    }

    public BeastType getBeastType() {
        return BeastType.byId(this.entityData.get(DATA_BEAST_TYPE));
    }

    public void setBeastType(BeastType type) {
        this.entityData.set(DATA_BEAST_TYPE, type.id);
    }

    /** Reassess MoveControl after entityData is fully initialized. */
    public void reassessMoveControlPublic() {
        reassessMoveControl();
    }

    public int getCultivationTier() {
        return this.entityData.get(DATA_CULTIVATION_TIER);
    }

    public void setCultivationTier(int tier) {
        this.entityData.set(DATA_CULTIVATION_TIER, tier);
    }

    // ── Pose accessors ──────────────────────────────────────────────────
    public int getSpiritPose() {
        return this.entityData.get(DATA_POSE);
    }

    public void setSpiritPose(int pose) {
        this.entityData.set(DATA_POSE, pose);
    }

    @Override
    protected void registerGoals() {
        BeastType type = getBeastType();

        // Common to all: float in water
        this.goalSelector.addGoal(0, new FloatGoal(this));

        switch (type) {
            case WOLF -> {
                this.goalSelector.addGoal(1, new SpiritBeastHuntGoal(this, 1.2D));
                this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
                this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
                this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            }
            case RABBIT -> {
                this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
                this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.8, true));
                this.goalSelector.addGoal(4, new SpiritBeastGrazeGoal(this));
                this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
                this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            }
            case DEER -> {
                this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
                this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
                this.goalSelector.addGoal(3, new SpiritBeastGrazeGoal(this));
                this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
                this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            }
            case STONE_BACK_BOAR -> {
                this.goalSelector.addGoal(1, new SpiritBeastHuntGoal(this, 1.1D));
                this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, true));
                this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.7));
                this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
                this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));
            }
            case HAWK -> {
                this.goalSelector.addGoal(1, new SpiritBeastHuntGoal(this, 0.8D));
                this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
                this.goalSelector.addGoal(3, new dev.ergenverse.entity.ai.SpiritBeastFlightGoal(this, 0.8D));
                this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
                this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
                this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));
            }
            case FIRE_BEAST -> {
                this.goalSelector.addGoal(1, new SpiritBeastHuntGoal(this, 1.0D));
                this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
                this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8));
                this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
                this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));
            }
        }

        // ── BeastIntelligence-tiered AI (Constitution: 7-tier system) ──
        dev.ergenverse.simulation.actor.BeastIntelligence tier =
                dev.ergenverse.entity.ai.BeastIntelligenceGoalFactory.tierFromInt(getCultivationTier());
        dev.ergenverse.entity.ai.BeastIntelligenceGoalFactory.applyBeastGoals(
                this, tier, this.goalSelector, this.targetSelector);
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
        compound.putInt("SpiritPose", getSpiritPose());
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
        if (compound.contains("SpiritPose")) {
            setSpiritPose(compound.getInt("SpiritPose"));
        }
    }

    // ── MoveControl: replace default WalkMoveControl per beast type ───────
    private void reassessMoveControl() {
        BeastType type = getBeastType();
        if (type.isFlyer()) {
            this.moveControl = new dev.ergenverse.entity.control.FlightMoveControl(this);
        } else if (type.isAquatic()) {
            this.moveControl = new dev.ergenverse.entity.control.WaterBoundMoveControl(this);
        } else {
            this.moveControl = new dev.ergenverse.entity.control.SprintMoveControl(this);
        }
    }

    // ── tick(): auto-update pose as heuristic fallback ─────────────────────
    @Override
    public void tick() {
        super.tick();
        // Pose auto-update: heuristic fallback when no AI goal is driving pose.
        // Goals call setSpiritPose() every tick they run, so goal wins (last writer).
        if (!this.level().isClientSide()) {
            if (this.isInWater()) {
                setSpiritPose(POSE_SWIMMING);
            } else if (!this.onGround()) {
                setSpiritPose(POSE_FLYING);
            } else if (this.getDeltaMovement().lengthSqr() > 0.01D) {
                setSpiritPose(POSE_STANDING);
            }
        }
    }
}
