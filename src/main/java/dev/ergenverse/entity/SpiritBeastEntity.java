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
import dev.ergenverse.entity.control.SpiritFlightPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * SpiritBeastEntity — base entity for spirit-beast mobs (rabbit, wolf, deer, hawk,
 * fire_beast, stone_back_boar).
 *
 * <p>CRON-COMPLETIONIST-14: Major behavioral overhaul fixing 5 systemic bugs.
 * <p>CRON-COMPLETIONIST-65: Pathfinding overhaul — flyers now use FlyPathNavigation
 * and aquatics use WaterBoundPathNavigation instead of GroundPathNavigation. This
 * eliminates the "bulldozing through trees" behavior where flyers clipped through
 * terrain and aquatics walked on the ground. Bat now has combat goals (MeleeAttackGoal
 * + NearestAttackableTargetGoal). Builder dimensions in EREntityTypes now match
 * runtime getDimensions() to prevent dimension flicker on entity construction.
 */
public class SpiritBeastEntity extends PathfinderMob {

    /** The v1 beast types. */
    public enum BeastType {
        RABBIT("rabbit"),
        WOLF("wolf"),
        DEER("deer"),
        HAWK("hawk"),
        FIRE_BEAST("fire_beast"),
        STONE_BACK_BOAR("stone_back_boar"),
        CRANE("spirit_crane"),
        BAT("spirit_bat"),
        QILIN("qilin"),
        SEA_SERPENT("sea_serpent"),
        SOUL_FISH("soul_fish");

        public final String id;
        BeastType(String id) { this.id = id; }

        public static BeastType byId(String id) {
            for (BeastType t : values()) if (t.id.equals(id)) return t;
            return RABBIT;
        }

        /** Movement category for MoveControl and PathNavigation selection. */
        public boolean isFlyer()    { return this == HAWK || this == BAT || this == QILIN; }
        public boolean isAquatic()  { return this == SEA_SERPENT || this == SOUL_FISH; }
        public boolean isGround()   { return !isFlyer() && !isAquatic(); }
    }

    // ── Synced Pose System (DATA_POSE) ─────────────────────────────────────
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

    private static final int POSE_NONE = -1;

    private static final EntityDataAccessor<Integer> DATA_POSE =
            SynchedEntityData.defineId(SpiritBeastEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<String> DATA_BEAST_TYPE =
            SynchedEntityData.defineId(SpiritBeastEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Integer> DATA_CULTIVATION_TIER =
            SynchedEntityData.defineId(SpiritBeastEntity.class, EntityDataSerializers.INT);

    private int goalPoseTick = Integer.MIN_VALUE;

    public SpiritBeastEntity(EntityType<? extends SpiritBeastEntity> type, Level level) {
        super(type, level);
    }

    // ── Per-species attribute profiles ─────────────────────────────────────
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

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
            case CRANE -> Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 14.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.30D)
                    .add(Attributes.ATTACK_DAMAGE, 3.0D)
                    .add(Attributes.FOLLOW_RANGE, 20.0D);
            case BAT -> Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 6.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.40D)
                    .add(Attributes.ATTACK_DAMAGE, 1.0D)
                    .add(Attributes.FOLLOW_RANGE, 10.0D);
            case QILIN -> Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 60.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.35D)
                    .add(Attributes.ATTACK_DAMAGE, 10.0D)
                    .add(Attributes.FOLLOW_RANGE, 24.0D)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                    .add(Attributes.ARMOR_TOUGHNESS, 3.0D);
            case SEA_SERPENT -> Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 30.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.28D)
                    .add(Attributes.ATTACK_DAMAGE, 6.0D)
                    .add(Attributes.FOLLOW_RANGE, 16.0D);
            case SOUL_FISH -> Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 2.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.45D)
                    .add(Attributes.ATTACK_DAMAGE, 0.0D)
                    .add(Attributes.FOLLOW_RANGE, 8.0D);
        };
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CRON-COMPLETIONIST-65: 3D PathNavigation — the single biggest behavior fix
    // ═══════════════════════════════════════════════════════════════════════
    //
    // PROBLEM: SpiritBeastEntity extends PathfinderMob, which uses
    // GroundPathNavigation by default. GroundPathNavigation ONLY paths on
    // the XZ plane at the entity's feet Y level. It ignores altitude entirely.
    //
    // CONSEQUENCE: Flying beasts (hawk, bat, qilin) with FlightGoal set
    // noGravity=true and fly at Y=ground+15, but their MeleeAttackGoal
    // and HuntGoal use GroundPathNavigation to path to targets ON THE GROUND.
    // The entity tries to reach a ground-level path node while floating 15
    // blocks up — resulting in the entity drifting sideways toward the target
    // but never descending, or clipping through terrain when FlightGoal
    // bulldozes with setDeltaMovement.
    //
    // Aquatic beasts (sea_serpent, soul_fish) also use GroundPathNavigation,
    // so their HuntGoal paths them along the ocean floor instead of through
    // the water column. A sea serpent chasing fish on the sea floor looks
    // absurd — it should swim through 3D water space.
    //
    // FIX: Override createNavigation() to return:
    //   - FlyPathNavigation for flyers (HAWK, BAT, QILIN) — this checks
    //     canMoveTo in 3D, allowing paths that go over/around obstacles
    //   - WaterBoundPathNavigation for aquatics (SEA_SERPENT, SOUL_FISH)
    //     — this prefers water blocks and paths through water volumes
    //   - GroundPathNavigation for ground beasts (existing, no change)
    //
    // FlyPathNavigation exists in MC 1.20.1 (used by Phantom, Ghast).
    // WaterBoundPathNavigation exists in MC 1.20.1 (used by Dolphin, Fish).
    // We simply USE them instead of reinventing the wheel.
    //
    // This is the most impactful single change because it makes ALL goals
    // (not just FlightGoal) respect 3D space. MeleeAttackGoal, HuntGoal,
    // PatrolGoal, FleeGoal — ALL now path correctly in 3D for flyers and
    // aquatics.

    @Override
    protected PathNavigation createNavigation(Level level) {
        // Navigation is selected based on the CURRENT beast type.
        // This is called during super() constructor, before defineSynchedData(),
        // so we cannot read DATA_BEAST_TYPE yet. Default to ground navigation
        // here — it will be overridden by reassessNavigation() after type is set.
        // (PathfinderMob constructor calls createNavigation before defineSynchedData)
        return new GroundPathNavigation(this, level);
    }

    /**
     * CRON-COMPLETIONIST-65: Replace the PathNavigation with the correct
     * 3D variant based on beast type. Called from setBeastType() and
     * defineSynchedData() after DATA_BEAST_TYPE is available.
     *
     * This is separate from reassessMoveControl() because the navigation
     * is a different system from the move control. MoveControl handles
     * the physics of movement (altitude maintenance, obstacle vaulting).
     * Navigation handles the pathfinding (finding a route to the target).
     */
    private void reassessNavigation() {
        BeastType type = getBeastType();
        if (type.isFlyer()) {
            this.navigation = new SpiritFlightPathNavigation(this, this.level());
        } else if (type.isAquatic()) {
            this.navigation = new WaterBoundPathNavigation(this, this.level());
        }
        // Ground beasts keep GroundPathNavigation from createNavigation()
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BEAST_TYPE, "rabbit");
        this.entityData.define(DATA_CULTIVATION_TIER, 0);
        this.entityData.define(DATA_POSE, POSE_STANDING);
        reassessMoveControl();
        reassessNavigation();
    }

    public BeastType getBeastType() {
        return BeastType.byId(this.entityData.get(DATA_BEAST_TYPE));
    }

    public void setBeastType(BeastType type) {
        this.entityData.set(DATA_BEAST_TYPE, type.id);
        reassessMoveControl();
        reassessNavigation();
    }

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
        if (pose != POSE_STANDING) {
            this.goalPoseTick = this.tickCount;
        }
    }

    public dev.ergenverse.entity.control.SprintMoveControl getSprintMoveControl() {
        return (this.moveControl instanceof dev.ergenverse.entity.control.SprintMoveControl sprint)
                ? sprint : null;
    }

    @Override
    protected void registerGoals() {
        BeastType type = getBeastType();

        // Common to all: float in water + swim goal + rest goal
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
            // ═══════════════════════════════════════════════════════════════
            // CRON-COMPLETIONIST-65: BAT COMBAT FIX
            // ═══════════════════════════════════════════════════════════════
            // Previously the bat had ZERO combat goals — it could fly but
            // never attacked anything. It had FlightGoal + stroll + lookAround
            // and a HurtByTargetGoal that could only aggro it if hurt first.
            //
            // Canon: Spirit bats in Renegade Immortal are aggressive nocturnal
            // predators that swarm and drain qi. They should attack small prey
            // (rabbits, fish) and retaliate against attackers.
            //
            // Fix: Added MeleeAttackGoal (priority 3) so the bat can damage
            // targets it reaches, and NearestAttackableTargetGoal for rabbits
            // and fish (priority 3, optional=true) so it hunts small prey.
            // Kept HurtByTargetGoal (priority 1) for retaliation.
            case BAT -> {
                this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true)); // NEW: can now attack
                this.goalSelector.addGoal(4, new dev.ergenverse.entity.ai.SpiritBeastFlightGoal(this, 0.8D));
                this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.2));
                this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
                // NEW: Bats hunt small prey (rabbits, fish, deer) — canon-accurate
                this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                        this, SpiritBeastEntity.class, 10, true, false,
                        (living) -> living instanceof SpiritBeastEntity prey
                                && (prey.getBeastType() == BeastType.RABBIT
                                || prey.getBeastType() == BeastType.SOUL_FISH
                                || prey.getBeastType() == BeastType.DEER)));
            }
            case QILIN -> {
                this.goalSelector.addGoal(2, new SpiritBeastHuntGoal(this, 1.3D));
                this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.3, true));
                this.goalSelector.addGoal(4, new dev.ergenverse.entity.ai.SpiritBeastFlightGoal(this, 1.0D));
                this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
                this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            }
            case SEA_SERPENT -> {
                this.goalSelector.addGoal(2, new SpiritBeastHuntGoal(this, 1.1D));
                this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, true));
                this.goalSelector.addGoal(4, new SpiritBeastSwimGoal(this));
                this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
                this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
                this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));
            }
            case SOUL_FISH -> {
                this.goalSelector.addGoal(4, new SpiritBeastSwimGoal(this));
                this.goalSelector.addGoal(5, new PanicGoal(this, 1.5D));
                this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
                this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            }
        }

        // ── BeastIntelligence-tiered AI (Constitution: 7-tier system) ──
        dev.ergenverse.simulation.actor.BeastIntelligence tier =
                dev.ergenverse.entity.ai.BeastIntelligenceGoalFactory.tierFromInt(getCultivationTier());
        dev.ergenverse.entity.ai.BeastIntelligenceGoalFactory.applyBeastGoals(
                this, tier, this.goalSelector, this.targetSelector);

        // ── Living Events ──
        this.goalSelector.addGoal(10, new dev.ergenverse.entity.ai.BeastLivingEventGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
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
        this.reassessDimensions();
    }

    // ── Per-species dimensions (bounding box) ──────────────────────────
    private float beastWidth = 0.6F;
    private float beastHeight = 1.8F;
    private float beastEyeHeight = 1.6F;

    private void reassessDimensions() {
        switch (getBeastType()) {
            case RABBIT -> { beastWidth = 0.4F; beastHeight = 0.5F; beastEyeHeight = 0.4F; }
            case WOLF   -> { beastWidth = 0.7F; beastHeight = 1.0F; beastEyeHeight = 0.85F; }
            case DEER   -> { beastWidth = 0.8F; beastHeight = 1.4F; beastEyeHeight = 1.2F; }
            case HAWK   -> { beastWidth = 0.5F; beastHeight = 0.6F; beastEyeHeight = 0.45F; }
            case FIRE_BEAST -> { beastWidth = 1.0F; beastHeight = 1.4F; beastEyeHeight = 1.1F; }
            case STONE_BACK_BOAR -> { beastWidth = 1.0F; beastHeight = 1.0F; beastEyeHeight = 0.8F; }
            case CRANE  -> { beastWidth = 0.6F; beastHeight = 1.6F; beastEyeHeight = 1.4F; }
            case BAT    -> { beastWidth = 0.4F; beastHeight = 0.5F; beastEyeHeight = 0.35F; }
            case QILIN  -> { beastWidth = 1.0F; beastHeight = 1.4F; beastEyeHeight = 1.2F; }
            case SEA_SERPENT -> { beastWidth = 0.8F; beastHeight = 1.0F; beastEyeHeight = 0.8F; }
            case SOUL_FISH -> { beastWidth = 0.3F; beastHeight = 0.3F; beastEyeHeight = 0.15F; }
            default -> { beastWidth = 0.6F; beastHeight = 1.8F; beastEyeHeight = 1.6F; }
        }
    }

    @Override
    public float getEyeHeight(net.minecraft.world.entity.Pose pose) {
        return this.beastEyeHeight;
    }

    @Override
    public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) {
        return net.minecraft.world.entity.EntityDimensions.scalable(beastWidth, beastHeight);
    }

    // ── tick(): pose heuristic with goal-priority guard ──────────────────
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            int ticksSinceGoal = this.tickCount - this.goalPoseTick;
            boolean goalDrivingPose = ticksSinceGoal < 5;

            if (!goalDrivingPose) {
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
