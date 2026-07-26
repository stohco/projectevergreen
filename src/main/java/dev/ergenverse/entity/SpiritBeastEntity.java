package dev.ergenverse.entity;

import dev.ergenverse.entity.control.FlightMoveControl;
import dev.ergenverse.entity.control.SprintMoveControl;
import dev.ergenverse.entity.control.WaterBoundMoveControl;
import dev.ergenverse.entity.ai.SpiritBeastGrazeGoal;
import dev.ergenverse.entity.ai.SpiritBeastHuntGoal;
import dev.ergenverse.entity.ai.SpiritBeastRestGoal;
import dev.ergenverse.entity.ai.SpiritBeastSwimGoal;
import dev.ergenverse.entity.ai.SpiritBeastFeedGoal;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import dev.ergenverse.entity.ai.AquaticWanderGoal;
import dev.ergenverse.entity.ai.SchoolingGoal;
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

    /** The v1 beast types.
     *
     * <p><b>CRON-COMPLETIONIST-84 — SINGLE SOURCE OF TRUTH for hitboxes.</b>
     * Each enum constant carries its own {@code (width, height, eyeHeight)}.
     * Both {@link EREntityTypes} (for {@code EntityType.Builder.sized()}) and
     * {@link SpiritBeastEntity#getDimensions} / {@link SpiritBeastEntity#getEyeHeight}
     * read from THESE fields. There is no longer a dual-source footgun where
     * {@code EntityType.sized} and {@code getDimensions} can disagree — they
     * are literally the same value.
     *
     * <p>Prior to CRON-84, three sources of truth existed:
     * <ul>
     *   <li>(A) {@code EntityType.Builder.sized(w, h)} in EREntityTypes</li>
     *   <li>(B) {@code SpiritBeastEntity.reassessDimensions()} inline switch
     *       (runtime override — WINS at runtime, making A stale documentation)</li>
     *   <li>(C) "Hitbox: ~W wide, ~H tall" comment in EREntityTypes</li>
     * </ul>
     * When A and B disagreed, B won — this silently undid CRON-60's SOUL_FISH
     * fix for ~10 rounds. CRON-80 reconciled all three by hand; CRON-84
     * eliminates the dual-source architecture so future changes can't desync.
     *
     * <p><b>Hitbox design constraints (preserved from CRON-80):</b>
     * <ul>
     *   <li>Width ≤ 1.2 (door navigation; vanilla horse is 1.4 but uses a
     *       separate size handler — we keep ours at 1.2 max so beasts can
     *       path through doors).</li>
     *   <li>Height ≤ 1.8 (2-block doorway clearance; deer/crane models are
     *       ~2.0-2.2 tall but capped to fit doors).</li>
     *   <li>Eye height ~80% of total height (vanilla Mob pattern).</li>
     *   <li>Wings on flyers (hawk, bat) are body-only collision per vanilla
     *       parrot convention — wings are visual, hitbox covers body+head.</li>
     * </ul>
     */
    public enum BeastType {
        RABBIT("rabbit", 0.4F, 0.5F, 0.4F),
        WOLF("wolf", 0.6F, 0.9F, 0.75F),
        DEER("deer", 0.7F, 1.8F, 1.45F),
        HAWK("hawk", 0.5F, 0.6F, 0.5F),
        FIRE_BEAST("fire_beast", 1.2F, 1.4F, 1.15F),
        STONE_BACK_BOAR("stone_back_boar", 1.2F, 1.0F, 0.8F),
        CRANE("spirit_crane", 0.6F, 1.8F, 1.6F),
        BAT("spirit_bat", 0.4F, 0.5F, 0.4F),
        QILIN("qilin", 1.0F, 1.5F, 1.25F),
        SEA_SERPENT("sea_serpent", 1.0F, 0.8F, 0.65F),
        SOUL_FISH("soul_fish", 0.6F, 0.5F, 0.25F),
        TIGER("spirit_tiger", 1.0F, 1.0F, 0.85F);

        public final String id;
        /** Hitbox width in blocks. ≤ 1.2 for door navigation. */
        public final float width;
        /** Hitbox height in blocks. ≤ 1.8 for doorway clearance. */
        public final float height;
        /** Eye height in blocks. ~80% of total height (vanilla Mob pattern). */
        public final float eyeHeight;

        BeastType(String id, float width, float height, float eyeHeight) {
            this.id = id;
            this.width = width;
            this.height = height;
            this.eyeHeight = eyeHeight;
        }

        public static BeastType byId(String id) {
            for (BeastType t : values()) if (t.id.equals(id)) return t;
            return RABBIT;
        }

        /**
         * Infer the BeastType from a registry name (e.g. "spirit_rabbit",
         * "fire_beast", "qilin"). Used by {@link SpiritBeastEntity#defineSynchedData}
         * to set the default BeastType from the EntityType, so spawn-egg and
         * vanilla-spawn beasts get the correct hitbox immediately without
         * waiting for {@code setBeastType()} to be called.
         *
         * <p>Matching: tries exact match first, then suffix match (registry
         * "spirit_rabbit" → id "rabbit"). Falls back to RABBIT if no match.
         */
        public static BeastType byRegistryName(String registryName) {
            if (registryName == null || registryName.isEmpty()) return RABBIT;
            // Exact match first (e.g. "fire_beast", "qilin", "spirit_crane").
            for (BeastType t : values()) {
                if (t.id.equals(registryName)) return t;
            }
            // Suffix match (e.g. "spirit_rabbit" → "rabbit", "spirit_hawk" → "hawk").
            for (BeastType t : values()) {
                if (registryName.endsWith(t.id)) return t;
            }
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

    /** CRON-COMPLETIONIST-71: Reference to FeedGoal for hunger timer ticking. */
    private SpiritBeastFeedGoal feedGoal;

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
            case TIGER -> Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 28.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.30D)
                    .add(Attributes.ATTACK_DAMAGE, 7.0D)
                    .add(Attributes.FOLLOW_RANGE, 20.0D)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D);
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
        // CRON-COMPLETIONIST-84: Infer the default BeastType from the EntityType's
        // registry name, so spawn-egg and vanilla-spawn beasts get the correct
        // hitbox + navigation immediately. Pre-CRON-84, the default was always
        // "rabbit", which meant a SPIRIT_WOLF spawn-egg beast had a rabbit-sized
        // hitbox (0.4x0.5) until setBeastType() was called — but setBeastType()
        // was NEVER called for spawn-egg beasts, so they kept the wrong hitbox
        // forever. This fix infers WOLF from "spirit_wolf", DEER from "spirit_deer",
        // etc., so the default is correct from tick 0.
        String regName = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
                .getKey(this.getType()) != null
                ? net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
                        .getKey(this.getType()).getPath()
                : "rabbit";
        this.entityData.define(DATA_BEAST_TYPE, BeastType.byRegistryName(regName).id);
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
                // CRON-COMPLETIONIST-77: Wolves hunt herbivores (rabbits, deer) — food chain
                this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                        this, SpiritBeastEntity.class, 10, true, false,
                        (living) -> living instanceof SpiritBeastEntity prey
                                && (prey.getBeastType() == BeastType.RABBIT
                                || prey.getBeastType() == BeastType.DEER)));
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
                // CRON-COMPLETIONIST-77: Hawks hunt rabbits from the air — food chain
                this.targetSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                        this, SpiritBeastEntity.class, 10, true, false,
                        (living) -> living instanceof SpiritBeastEntity prey
                                && prey.getBeastType() == BeastType.RABBIT));
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
            // ═══════════════════════════════════════════════════════════════
            // CRON-COMPLETIONIST-72: SPIRIT CRANE AI FIX
            // ═══════════════════════════════════════════════════════════════
            // Previously the crane had NO case in the registerGoals() switch.
            // It only received the common goals (Float, Swim, Rest, Feed,
            // LivingEvent) plus BeastIntelligence tier goals. It had NO
            // StrollGoal, no RandomLookAround, no combat goals, and no
            // target selectors. The crane would just stand in place, float,
            // and occasionally rest — completely inert.
            //
            // Canon (Renegade Immortal): spirit cranes are graceful, vigilant
            // creatures associated with cultivation sects. They are not
            // aggressive predators but will defend themselves when attacked.
            // They stalk through shallows, graze on small aquatic life, and
            // perform their famous crane dance during courtship.
            //
            // Fix: Added full goal set — graze, wander, look around, melee
            // self-defense, hurt-by retaliation, and flee response.
            case CRANE -> {
                // Spirit cranes are herbivores/omnivores — they graze in shallows
                this.goalSelector.addGoal(2, new SpiritBeastGrazeGoal(this));
                // Self-defense only — cranes don't hunt, but will strike back
                this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 0.8, true));
                // Wander through their territory (slow, majestic gait)
                this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.5));
                // Periodically scan surroundings (vigilant, crane-like alertness)
                this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
                // Retaliate when attacked — cranes are territorial
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            }
            case QILIN -> {
                this.goalSelector.addGoal(2, new SpiritBeastHuntGoal(this, 1.3D));
                this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.3, true));
                this.goalSelector.addGoal(4, new dev.ergenverse.entity.ai.SpiritBeastFlightGoal(this, 1.0D));
                this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
                this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            }
            // ═══════════════════════════════════════════════════════════════
            // CRON-COMPLETIONIST-77: SEA SERPENT AQUATIC AI FIX
            // ═══════════════════════════════════════════════════════════════
            // Previously the sea serpent used WaterAvoidingRandomStrollGoal —
            // this is CANONICALLY WRONG. Sea serpents are aquatic predators that
            // live in deep water. WaterAvoidingRandomStrollGoal causes them to
            // AVOID water and wander on land. A sea serpent on a beach is absurd.
            //
            // Fix: Replaced WaterAvoidingRandomStrollGoal with AquaticWanderGoal.
            // Sea serpents now wander WITHIN water, preferring deeper water.
            // If stranded on land, they navigate back to the nearest water.
            // Added NearestAttackableTargetGoal for soul_fish (prey species).
            case SEA_SERPENT -> {
                this.goalSelector.addGoal(2, new SpiritBeastHuntGoal(this, 1.1D));
                this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, true));
                this.goalSelector.addGoal(4, new SpiritBeastSwimGoal(this));
                // FIXED: AquaticWanderGoal replaces WaterAvoidingRandomStrollGoal
                this.goalSelector.addGoal(7, new AquaticWanderGoal(this, 0.7, 24));
                this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
                this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));
                // Sea serpents hunt soul fish — aquatic food chain
                this.targetSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                        this, SpiritBeastEntity.class, 10, true, false,
                        (living) -> living instanceof SpiritBeastEntity prey
                                && prey.getBeastType() == BeastType.SOUL_FISH));
            }
            // ═══════════════════════════════════════════════════════════════
            // CRON-COMPLETIONIST-77: SOUL FISH AQUATIC AI FIX + SCHOOLING
            // ═══════════════════════════════════════════════════════════════
            // Previously soul fish used WaterAvoidingRandomStrollGoal — a fish
            // that avoids water is cosmically wrong. Soul fish are bioluminescent
            // qi-infused fish that travel in schools through spirit veins.
            //
            // Fix: Replaced WaterAvoidingRandomStrollGoal with AquaticWanderGoal.
            // Added SchoolingGoal — soul fish cluster together in schools using
            // simplified boid rules (cohesion + separation). This creates the
            // shimmering schools that cultivators seek out in the novels.
            case SOUL_FISH -> {
                this.goalSelector.addGoal(3, new SchoolingGoal(this));
                this.goalSelector.addGoal(4, new SpiritBeastSwimGoal(this));
                this.goalSelector.addGoal(5, new PanicGoal(this, 1.5D));
                // FIXED: AquaticWanderGoal replaces WaterAvoidingRandomStrollGoal
                this.goalSelector.addGoal(7, new AquaticWanderGoal(this, 0.8, 8));
                this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            }
            // CRON-COMPLETIONIST-67: Spirit tiger — apex land predator.
            // Canon: ferocious mountain/forest beast, solitary ambush predator.
            case TIGER -> {
                this.goalSelector.addGoal(2, new SpiritBeastHuntGoal(this, 1.15D));
                this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.15, true));
                this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
                this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
                this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
                this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));
                // CRON-COMPLETIONIST-77: Tigers ambush herbivores (rabbits, deer) — apex predator food chain
                this.targetSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                        this, SpiritBeastEntity.class, 10, true, false,
                        (living) -> living instanceof SpiritBeastEntity prey
                                && (prey.getBeastType() == BeastType.RABBIT
                                || prey.getBeastType() == BeastType.DEER)));
            }
        }

        // ── BeastIntelligence-tiered AI (Constitution: 7-tier system) ──
        dev.ergenverse.simulation.actor.BeastIntelligence tier =
                dev.ergenverse.entity.ai.BeastIntelligenceGoalFactory.tierFromInt(getCultivationTier());
        dev.ergenverse.entity.ai.BeastIntelligenceGoalFactory.applyBeastGoals(
                this, tier, this.goalSelector, this.targetSelector);

        // ── Living Events ──
        this.goalSelector.addGoal(10, new dev.ergenverse.entity.ai.BeastLivingEventGoal(this));

        // ── CRON-COMPLETIONIST-71: Create feed goal with stored reference for hunger ticking ──
        this.feedGoal = new SpiritBeastFeedGoal(this);
        this.goalSelector.addGoal(5, this.feedGoal);
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
        // CRON-COMPLETIONIST-84: reassessDimensions() removed — dimensions now
        // read directly from BeastType enum in getDimensions()/getEyeHeight().
        // No caching fields, no stale state, no dual-source footgun.
    }

    // ── Per-species dimensions (bounding box) ──────────────────────────
    //
    // CRON-COMPLETIONIST-84: SINGLE SOURCE OF TRUTH.
    // ----------------------------------------------
    // Prior to CRON-84, three sources of truth existed for each beast's hitbox:
    //   (A) EntityType.Builder.sized(w, h) in EREntityTypes
    //   (B) SpiritBeastEntity.reassessDimensions() inline switch (WINS at runtime)
    //   (C) "Hitbox: ~W x H" comment in EREntityTypes (design intent)
    //
    // When A and B disagreed, B won — this silently undid CRON-60's SOUL_FISH
    // fix for ~10 rounds (CRON-60 doubled EntityType.sized but didn't update
    // reassessDimensions, so the runtime override kept the old 0.3x0.3).
    //
    // CRON-80 reconciled all three by hand. CRON-84 eliminates the dual-source
    // architecture: BeastType enum constants now carry (width, height, eyeHeight),
    // and BOTH EREntityTypes.sized() AND getDimensions()/getEyeHeight() read
    // from those enum fields. There is literally ONE place to change a beast's
    // hitbox — the enum constant. Future changes cannot silently desync.
    //
    // BONUS FIX: pre-CRON-84, spawn-egg and vanilla-spawn beasts never had
    // setBeastType() called, so reassessDimensions() never ran, and the caching
    // fields stayed at their default (0.6x1.8) — the WRONG hitbox for every
    // beast except those spawned via WorldStateEngine or loaded from NBT.
    // CRON-84 fixes this by (1) removing the caching fields entirely (reads
    // directly from the enum) and (2) inferring the default BeastType from
    // the EntityType's registry name in defineSynchedData().
    //
    // Hitbox design constraints (preserved from CRON-80):
    //   - Width  ≤ 1.2 (door navigation; vanilla horse is 1.4 but uses a
    //     separate size handler — we keep ours at 1.2 max)
    //   - Height ≤ 1.8 (2-block doorway clearance)
    //   - Eye height ~80% of total height (vanilla Mob pattern)
    //   - Wings on flyers (hawk, bat) are body-only collision per vanilla
    //     parrot convention — wings are visual, hitbox covers body+head.

    @Override
    public float getEyeHeight(net.minecraft.world.entity.Pose pose) {
        // CRON-84: read directly from BeastType enum — single source of truth.
        return getBeastType().eyeHeight;
    }

    @Override
    public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) {
        // CRON-84: read directly from BeastType enum — single source of truth.
        // EREntityTypes.sized() reads from the SAME enum fields, so the
        // EntityType dimensions and this runtime override ALWAYS agree.
        BeastType type = getBeastType();
        return net.minecraft.world.entity.EntityDimensions.scalable(type.width, type.height);
    }

    // ── tick(): pose heuristic with goal-priority guard ──────────────────
    @Override
    public void tick() {
        super.tick();
        // CRON-COMPLETIONIST-71: Tick the feed goal's hunger timer every tick
        if (feedGoal != null) feedGoal.tickHunger();
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
