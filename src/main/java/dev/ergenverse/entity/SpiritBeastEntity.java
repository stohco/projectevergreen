package dev.ergenverse.entity;

import dev.ergenverse.entity.control.FlightMoveControl;
import dev.ergenverse.entity.control.SprintMoveControl;
import dev.ergenverse.entity.control.WaterBoundMoveControl;
import dev.ergenverse.entity.ai.SpiritBeastGrazeGoal;
import dev.ergenverse.entity.ai.SpiritBeastHuntGoal;
import dev.ergenverse.entity.ai.SpiritBeastRestGoal;
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

/**
 * SpiritBeastEntity — base entity for spirit-beast mobs (rabbit, wolf, deer, hawk,
 * fire_beast, stone_back_boar).
 *
 * <p>CRON-COMPLETIONIST-14: Major behavioral overhaul fixing 5 systemic bugs:
 * <ul>
 *   <li><b>BUG FIX 1</b>: reassessMoveControl() now called from
 *       {@link #defineSynchedData()} (after DATA_BEAST_TYPE is defined), so
 *       MoveControls are actually installed on entity construction.</li>
 *   <li><b>BUG FIX 2</b>: tick() pose heuristic no longer overwrites goal-driven
 *       poses. Uses a timestamp-based priority system: goals set pose + timestamp,
 *       tick() only applies heuristic if no goal has set a pose in the last 5 ticks.</li>
 *   <li><b>BUG FIX 3</b>: Per-species attribute profiles via
 *       {@link #createBeastAttributes(BeastType)}. Each beast type now has
 *       canonically-appropriate HP, speed, damage, and follow range.</li>
 *   <li><b>BUG FIX 4</b>: SpiritBeastSwimGoal now registered for ALL beast types
 *       (was dead code). SprintMoveControl.startSprint() wired into HuntGoal.</li>
 *   <li><b>BUG FIX 5</b>: HuntGoal charge phase now calls SprintMoveControl.startSprint()
 *       on the beast's MoveControl if it is a SprintMoveControl.</li>
 * </ul>
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

    /** A pose value that means "no goal is actively driving the pose." */
    private static final int POSE_NONE = -1;

    private static final EntityDataAccessor<Integer> DATA_POSE =
            SynchedEntityData.defineId(SpiritBeastEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<String> DATA_BEAST_TYPE =
            SynchedEntityData.defineId(SpiritBeastEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Integer> DATA_CULTIVATION_TIER =
            SynchedEntityData.defineId(SpiritBeastEntity.class, EntityDataSerializers.INT);

    /**
     * Tracks the last tick a goal explicitly set the pose.
     * The tick() heuristic only applies when this is stale (>5 ticks ago).
     * This prevents tick() from overwriting goal-driven poses.
     */
    private int goalPoseTick = Integer.MIN_VALUE;

    public SpiritBeastEntity(EntityType<? extends SpiritBeastEntity> type, Level level) {
        super(type, level);
    }

    // ── Per-species attribute profiles ─────────────────────────────────────
    // Previously all beasts shared 20HP/0.28speed/2dmg/16follow — a rabbit
    // had the same stats as a fire beast. Now each type has canon-appropriate
    // attributes.
    //
    // Canon basis (Renegade Immortal):
    //   - Spirit Rabbit: small, fast, fragile. Prey animal. 4HP, 0.35 speed.
    //   - Spirit Wolf: pack predator. 16HP, 0.30 speed, 4dmg. Hunts deer/rabbits.
    //   - Spirit Deer: medium herbivore. 12HP, 0.28 speed. Flees predators.
    //   - Spirit Hawk: aerial predator. 10HP, 0.35 speed, 3dmg. Swoop attacks.
    //   - Fire Beast: demonic predator. 40HP, 0.32 speed, 8dmg. Aggro on player.
    //   - Stone Back Boar: tanky beast. 30HP, 0.25 speed, 6dmg. Armored back.

    public static AttributeSupplier.Builder createAttributes() {
        // Default (rabbit) attributes — used when type is not yet known at
        // EntityType construction time. Overridden in spawn events.
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    /**
     * Returns species-specific attribute builder. Called from spawn events and
     * from the Forge EntityAttributeCreationEvent handler to apply correct
     * stats when the beast type is known.
     */
    public static AttributeSupplier.Builder createBeastAttributes(BeastType type) {
        return switch (type) {
            case RABBIT -> Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 4.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.35D)
                    .add(Attributes.ATTACK_DAMAGE, 1.0D)
                    .add(Attributes.FOLLOW_RANGE, 12.0D);
            case WOLF -> Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 16.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.30D)
                    .add(Attributes.ATTACK_DAMAGE, 4.0D)
                    .add(Attributes.FOLLOW_RANGE, 20.0D);
            case DEER -> Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 12.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.28D)
                    .add(Attributes.ATTACK_DAMAGE, 2.0D)
                    .add(Attributes.FOLLOW_RANGE, 16.0D);
            case HAWK -> Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 10.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.35D)
                    .add(Attributes.ATTACK_DAMAGE, 3.0D)
                    .add(Attributes.FOLLOW_RANGE, 24.0D);
            case FIRE_BEAST -> Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 40.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.32D)
                    .add(Attributes.ATTACK_DAMAGE, 8.0D)
                    .add(Attributes.FOLLOW_RANGE, 24.0D)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D);
            case STONE_BACK_BOAR -> Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 30.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.25D)
                    .add(Attributes.ATTACK_DAMAGE, 6.0D)
                    .add(Attributes.FOLLOW_RANGE, 16.0D)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                    .add(Attributes.ARMOR_TOUGHNESS, 2.0D);
        };
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BEAST_TYPE, "rabbit");
        this.entityData.define(DATA_CULTIVATION_TIER, 0);
        this.entityData.define(DATA_POSE, POSE_STANDING);
        // BUG FIX 1: Install the correct MoveControl now that DATA_BEAST_TYPE
        // is defined (getBeastType() works). Previously reassessMoveControl()
        // existed but was never called — all beasts used default WalkMoveControl.
        reassessMoveControl();
    }

    public BeastType getBeastType() {
        return BeastType.byId(this.entityData.get(DATA_BEAST_TYPE));
    }

    public void setBeastType(BeastType type) {
        this.entityData.set(DATA_BEAST_TYPE, type.id);
        // Reinstall MoveControl when type changes (e.g. via spawn event)
        reassessMoveControl();
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

    /**
     * Set pose from an AI goal. Updates the timestamp so tick() heuristic
     * knows a goal is actively driving the pose and won't overwrite it.
     */
    public void setSpiritPose(int pose) {
        this.entityData.set(DATA_POSE, pose);
        if (pose != POSE_STANDING) {
            this.goalPoseTick = this.tickCount;
        }
    }

    /**
     * Returns the beast's MoveControl cast to SprintMoveControl if applicable,
     * or null if the MoveControl is not a SprintMoveControl.
     */
    public dev.ergenverse.entity.control.SprintMoveControl getSprintMoveControl() {
        return (this.moveControl instanceof dev.ergenverse.entity.control.SprintMoveControl sprint)
                ? sprint : null;
    }

    @Override
    protected void registerGoals() {
        BeastType type = getBeastType();

        // Common to all: float in water + swim goal + rest goal
        // BUG FIX 4: SwimGoal was dead code — created but never registered for any beast type
        // CRON-COMPLETIONIST-15: RestGoal now registered for all beasts (POSE_RESTING gap)
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SpiritBeastSwimGoal(this));
        this.goalSelector.addGoal(8, new SpiritBeastRestGoal(this));

        switch (type) {
            case WOLF -> {
                this.goalSelector.addGoal(2, new SpiritBeastHuntGoal(this, 1.2D));
                this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, true));
                this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
                this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            }
            case RABBIT -> {
                this.goalSelector.addGoal(2, new PanicGoal(this, 1.4));
                this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 0.8, true));
                this.goalSelector.addGoal(5, new SpiritBeastGrazeGoal(this));
                this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
                this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            }
            case DEER -> {
                this.goalSelector.addGoal(2, new PanicGoal(this, 1.4));
                this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
                this.goalSelector.addGoal(4, new SpiritBeastGrazeGoal(this));
                this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
                this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            }
            case STONE_BACK_BOAR -> {
                this.goalSelector.addGoal(2, new SpiritBeastHuntGoal(this, 1.1D));
                this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, true));
                this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
                this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
                this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));
            }
            case HAWK -> {
                this.goalSelector.addGoal(2, new SpiritBeastHuntGoal(this, 0.8D));
                this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
                this.goalSelector.addGoal(4, new dev.ergenverse.entity.ai.SpiritBeastFlightGoal(this, 0.8D));
                this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
                this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
                this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));
            }
            case FIRE_BEAST -> {
                this.goalSelector.addGoal(2, new SpiritBeastHuntGoal(this, 1.0D));
                this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
                this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
                this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
                this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));
            }
        }

        // ── BeastIntelligence-tiered AI (Constitution: 7-tier system) ──
        dev.ergenverse.simulation.actor.BeastIntelligence tier =
                dev.ergenverse.entity.ai.BeastIntelligenceGoalFactory.tierFromInt(getCultivationTier());
        dev.ergenverse.entity.ai.BeastIntelligenceGoalFactory.applyBeastGoals(
                this, tier, this.goalSelector, this.targetSelector);

        // ── Living Events: make beast behavior observable to NPCs/WorldHistory ──
        // Per the Living Moments framework: "No subsystem is considered
        // complete until it has participated in at least one observable
        // Living Moment." This goal watches pose transitions and publishes
        // events to the WorldEventBus so NPCs can react and WorldHistory
        // can record memories.
        this.goalSelector.addGoal(10, new dev.ergenverse.entity.ai.BeastLivingEventGoal(this));
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
            this.moveControl = new FlightMoveControl(this);
        } else if (type.isAquatic()) {
            this.moveControl = new WaterBoundMoveControl(this);
        } else {
            this.moveControl = new SprintMoveControl(this);
        }
    }

    // ── tick(): pose heuristic with goal-priority guard ──────────────────
    //
    // BUG FIX 2: Previously tick() ran AFTER goal.tick() every server tick
    // and unconditionally overwrote the pose. If a beast was grazing
    // (POSE_GRAZING set by GrazeGoal) but had any residual deltaMovement
    // (which happens from gravity/friction), tick() would reset it to
    // POSE_STANDING every tick. The graze animation flickered between
    // grazing and standing.
    //
    // Fix: track goalPoseTick (updated by setSpiritPose when pose != STANDING).
    // tick() only applies heuristic fallback when no goal has set a non-STANDING
    // pose in the last 5 ticks. This gives goals a 5-tick grace period where
    // their pose is safe from the heuristic.
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            int ticksSinceGoal = this.tickCount - this.goalPoseTick;
            boolean goalDrivingPose = ticksSinceGoal < 5;

            if (!goalDrivingPose) {
                // No goal has claimed the pose recently — apply heuristic
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
}
