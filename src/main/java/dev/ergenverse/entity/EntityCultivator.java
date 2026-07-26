package dev.ergenverse.entity;

import com.google.gson.JsonObject;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.WorldStateDataLoader;
import dev.ergenverse.simulation.WorldRuntimeState;
import dev.ergenverse.wanglin.bead.LiMuwanSoulCaptureEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * EntityCultivator — a polymorphic NPC shell driven by canon data.
 *
 * <h2>Design</h2>
 * <p>Instead of 151 separate Java classes for 151 canon characters, this is
 * a single, highly polymorphic entity. At spawn, it reads a
 * {@code character_id} (e.g. {@code "wang_tiangui"}) and configures itself
 * from the canon database + runtime overrides:
 * <ol>
 *   <li>{@link WorldStateDataLoader#getEntry(String, String)} — reads the
 *       canon baseline (name, cultivation realm, HP, location, etc.) from
 *       {@code data/ergenverse/npcs/<id>.json}.</li>
 *   <li>{@link WorldRuntimeState#getNpcState(String)} — layers any t>0
 *       mutations (damage taken, position drift) on top of the baseline.</li>
 * </ol>
 *
 * <h2>Synced data</h2>
 * <p>{@link #DATA_CHARACTER_ID} is a {@link SynchedEntityData} string field,
 * synced to clients. The client renderer reads it to pick the correct
 * model/texture. Without this, every cultivator would render identically.
 *
 * <h2>Hibernation</h2>
 * <p>{@link MobCategory#MISC} entities normally despawn when players walk
 * away. We override {@link #removeWhenFarAway(double)} to return
 * {@code false} — canon entities never despawn. But to avoid ticking-entity
 * accumulation (100s of NPCs each running AI), {@link #aiStep()} short-circuits
 * when no player is within {@link #HIBERNATION_RANGE} blocks. The entity
 * persists in the world but consumes minimal CPU.
 *
 * <h2>State persistence</h2>
 * <p>On chunk unload ({@link #onRemovedFromLevel()}), the entity writes its
 * current state (HP, position) back to {@link WorldRuntimeState}. On the
 * next materialization, {@link #initializeFromData} re-reads it. The canon
 * DB is never written to.
 *
 * <h2>v1 scope</h2>
 * <p>This first version supports {@code wang_tiangui} (Wang Lin's mortal
 * father): Mortal realm, 20 HP, no combat, passive wander. Behavior AI
 * (combat, dialogue, sect routines) is deferred to v2. The shell is
 * complete; the behavior brain is not.
 *
 * <h2>Prime Directive</h2>
 * <p>"Reality is objective." The entity's stats are read from canon, not
 * invented. If canon is silent on a stat, the field is left at its Minecraft
 * default (marked with a TODO) rather than fabricated.
 */
public class EntityCultivator extends PathfinderMob {

    // ── Synced data: the character_id token ─────────────────────────────

    /**
     * Synced string identifying which canon character this entity represents.
     * The client renderer reads this to pick model/texture. Set once at
     * spawn via {@link #setCharacterId(String)}.
     */
    private static final EntityDataAccessor<String> DATA_CHARACTER_ID =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.STRING);

    /** Synced display name (canonical name, e.g. "Wang Tiangui 王天贵"). */
    private static final EntityDataAccessor<String> DATA_DISPLAY_NAME =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.STRING);

    /** Synced cultivation realm ID (e.g. "mortal", "qi_condensation"). */
    private static final EntityDataAccessor<String> DATA_CULTIVATION_REALM =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.STRING);

    /** Synced pose flag: 0=idle, 1=meditating, 2=casting/channeling.
     *  The client renderer reads this to drive CultivatorRobeModel's
     *  meditation and casting poses. Previously these were TODO flags
     *  that were never set — now they are synced and the renderer uses them.
     */
    private static final EntityDataAccessor<Integer> DATA_POSE =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.INT);

    /** Synced sect/faction ID (e.g. "heng_yue_sect", "teng_family", "independent").
     *  The client renderer reads this to select per-sect cultivator textures.
     *  CRON-COMPLETIONIST-50: This closes the 30+ round visual deficit where
     *  ALL 151+ NPCs shared one default.png texture.
     */
    private static final EntityDataAccessor<String> DATA_SECT =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.STRING);

    /**
     * CRON-COMPLETIONIST-102: The UUID of the player this cultivator is
     * following as a companion. Empty string = not following anyone (default
     * for all canon NPCs). Set to a player's UUID when Li Muwan is revived —
     * she follows Wang Lin ("两人踏天同行" — they transcend together).
     *
     * <p>Synced to the client so the renderer can display companion-specific
     * visuals (e.g., a gentle aura, a different idle pose) in a future CRON.
     * The {@link dev.ergenverse.entity.ai.FollowPlayerGoal} reads this
     * server-side to determine its target.
     */
    private static final EntityDataAccessor<String> DATA_FOLLOWING_PLAYER_UUID =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.STRING);

    // ── CRON-COMPLETIONIST-19: Cognitive Body-Language Layer ───────────
    // The user's 2026-07-25 directive: "the real bottleneck isn't AI anymore.
    // It's representation. Suppose Wang Lin decides 'Observe wolves.' That's
    // wonderful. Now ask: Can the player tell? Without debug overlay, command,
    // logs, worklog, subtitles — just looking. If the answer is 'Not really,'
    // then the AI may as well not exist."
    //
    // The fix: project the active Commitment onto the entity's pose + look
    // target in REAL TIME (per CognitionDrivenGoal tick), not just at
    // settlement-scan time. The NPC's head tracks the commitment's look
    // target (the wolves), with smooth interpolated rotation and micro-saccade
    // noise. The player walking past does NOT break the look-target — the NPC
    // is absorbed in observation.
    //
    // These three synced floats carry the world-space look target. The
    // renderer reads them and lerps the head toward them. When all three
    // are NaN, no cognitive look target is set (vanilla look control runs).

    /** Synced cognitive look-target X (world coords). NaN = no target. */
    private static final EntityDataAccessor<Float> DATA_LOOK_TARGET_X =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.FLOAT);
    /** Synced cognitive look-target Y (world coords, eye height). NaN = no target. */
    private static final EntityDataAccessor<Float> DATA_LOOK_TARGET_Y =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.FLOAT);
    /** Synced cognitive look-target Z (world coords). NaN = no target. */
    private static final EntityDataAccessor<Float> DATA_LOOK_TARGET_Z =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.FLOAT);

    // ── CRON-COMPLETIONIST-21: Acting Layer — Performance channels ──────
    // The user's 2026-07-26 review: "instead of thinking in poses, think in
    // independent channels — Head, Torso, Shoulders, Hands, Feet, Eyes,
    // Breathing, Attention, Weight. Each channel updates independently."
    //
    // These seven synced floats carry the acting directions (focus, urgency,
    // confidence, concealment, tension, patience, fatigue) from the server-side
    // Commitment to the client-side renderer. When all are NaN, no Commitment
    // is active and the renderer falls back to vanilla pose-based animation.
    // When set, the renderer drives each body part INDEPENDENTLY from the
    // channels — producing hundreds of emergent silhouettes instead of 5 fixed
    // poses. Same IntentNature + different context → different acting.
    /** Synced Performance channel: focus (concentration). NaN = no performance. */
    private static final EntityDataAccessor<Float> DATA_PERF_FOCUS =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.FLOAT);
    /** Synced Performance channel: urgency (time pressure). */
    private static final EntityDataAccessor<Float> DATA_PERF_URGENCY =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.FLOAT);
    /** Synced Performance channel: confidence (trust in one's read). */
    private static final EntityDataAccessor<Float> DATA_PERF_CONFIDENCE =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.FLOAT);
    /** Synced Performance channel: concealment (importance of staying hidden). */
    private static final EntityDataAccessor<Float> DATA_PERF_CONCEALMENT =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.FLOAT);
    /** Synced Performance channel: tension (physical readiness). */
    private static final EntityDataAccessor<Float> DATA_PERF_TENSION =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.FLOAT);
    /** Synced Performance channel: patience (willingness to hold still). */
    private static final EntityDataAccessor<Float> DATA_PERF_PATIENCE =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.FLOAT);
    /** Synced Performance channel: fatigue (accumulated tiredness). */
    private static final EntityDataAccessor<Float> DATA_PERF_FATIGUE =
            SynchedEntityData.defineId(EntityCultivator.class, EntityDataSerializers.FLOAT);

    // ── Hibernation ─────────────────────────────────────────────────────

    /**
     * Below this distance (in blocks) to the nearest player, the entity is
     * "active" (AI runs, pathfinding active). At or above this distance,
     * the entity hibernates — persists in the world, but {@link #aiStep()}
     * short-circuits. 64 blocks = 4 chunks, matching the proposal.
     */
    public static final double HIBERNATION_RANGE = 64.0;

    /** Squared hibernation range (avoid sqrt in the hot path). */
    public static final double HIBERNATION_RANGE_SQ = HIBERNATION_RANGE * HIBERNATION_RANGE;

    // ── Server-only state (not synced) ──────────────────────────────────

    /** True once {@link #initializeFromData} has configured the entity. */
    private boolean initialized = false;

    /** Cached canon data for this character (null on client). */
    @Nullable
    private JsonObject canonData;

    // ═══════════════════════════════════════════════════════════════════
    //  Construction & registration helpers
    // ═══════════════════════════════════════════════════════════════════

    public EntityCultivator(EntityType<? extends EntityCultivator> type, Level level) {
        super(type, level);
    }

    /**
     * Register default attributes. Called from
     * {@code ErgenverseClient/EREntityTypes#registerAttributes} via
     * {@code EntityAttributeCreationEvent}. v1 uses Mortal-tier defaults
     * (20 HP, slow movement). Future: data-driven attributes per character.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  SynchedEntityData
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CHARACTER_ID, "");
        this.entityData.define(DATA_DISPLAY_NAME, "Unknown Cultivator");
        this.entityData.define(DATA_CULTIVATION_REALM, "mortal");
        this.entityData.define(DATA_POSE, 0);
        this.entityData.define(DATA_SECT, "independent");
        // CRON-COMPLETIONIST-102: companion-following UUID (empty = not following).
        this.entityData.define(DATA_FOLLOWING_PLAYER_UUID, "");
        // CRON-COMPLETIONIST-19: cognitive look-target defaults to NaN (no target).
        // Float.NaN is a valid float and syncs cleanly. The renderer checks isNaN.
        this.entityData.define(DATA_LOOK_TARGET_X, Float.NaN);
        this.entityData.define(DATA_LOOK_TARGET_Y, Float.NaN);
        this.entityData.define(DATA_LOOK_TARGET_Z, Float.NaN);
        // CRON-COMPLETIONIST-21: Acting Layer — Performance channels default to
        // NaN (no active commitment). When CognitionDrivenGoal sets a Performance,
        // all seven are written together; when cleared, all seven go NaN together.
        this.entityData.define(DATA_PERF_FOCUS, Float.NaN);
        this.entityData.define(DATA_PERF_URGENCY, Float.NaN);
        this.entityData.define(DATA_PERF_CONFIDENCE, Float.NaN);
        this.entityData.define(DATA_PERF_CONCEALMENT, Float.NaN);
        this.entityData.define(DATA_PERF_TENSION, Float.NaN);
        this.entityData.define(DATA_PERF_PATIENCE, Float.NaN);
        this.entityData.define(DATA_PERF_FATIGUE, Float.NaN);
    }

    public String getCharacterId() {
        return this.entityData.get(DATA_CHARACTER_ID);
    }

    public void setCharacterId(String id) {
        this.entityData.set(DATA_CHARACTER_ID, id);
    }

    /** CRON-COMPLETIONIST-50: Returns the synced sect/faction ID for texture selection. */
    public String getSectId() {
        return this.entityData.get(DATA_SECT);
    }

    /** CRON-COMPLETIONIST-50: Sets the sect/faction ID (called during initialization). */
    public void setSectId(String sectId) {
        this.entityData.set(DATA_SECT, sectId);
    }

    /**
     * CRON-COMPLETIONIST-102: Get the UUID of the player this cultivator is
     * following as a companion. Returns an empty string if not following
     * anyone (the default for all canon NPCs except revived Li Muwan).
     *
     * @return the following player's UUID string, or empty if not following
     */
    public String getFollowingPlayerUuid() {
        return this.entityData.get(DATA_FOLLOWING_PLAYER_UUID);
    }

    /**
     * CRON-COMPLETIONIST-102: Set the UUID of the player this cultivator
     * should follow as a companion. Called by
     * {@link dev.ergenverse.wanglin.bead.LiMuwanRevivalEvent} when Li Muwan
     * is revived — she follows Wang Lin ("两人踏天同行").
     *
     * <p>Pass an empty string to stop following (clears the companion bond).
     *
     * @param playerUuid the player's UUID string, or empty to clear
     */
    public void setFollowingPlayerUuid(String playerUuid) {
        this.entityData.set(DATA_FOLLOWING_PLAYER_UUID, playerUuid == null ? "" : playerUuid);
    }

    /**
     * CRON-COMPLETIONIST-102: Check if this cultivator is currently following
     * a player as a companion.
     *
     * @return {@code true} if a following-player UUID is set
     */
    public boolean isFollowingPlayer() {
        String uuid = getFollowingPlayerUuid();
        return uuid != null && !uuid.isEmpty();
    }

    public String getDisplayNameCn() {
        return this.entityData.get(DATA_DISPLAY_NAME);
    }

    public void setDisplayNameCn(String name) {
        this.entityData.set(DATA_DISPLAY_NAME, name);
    }

    public String getCultivationRealm() {
        return this.entityData.get(DATA_CULTIVATION_REALM);
    }

    public void setCultivationRealm(String realm) {
        this.entityData.set(DATA_CULTIVATION_REALM, realm);
    }

    /** Pose constants for {@link #DATA_POSE}. */
    public static final int POSE_IDLE = 0;
    public static final int POSE_MEDITATING = 1;
    public static final int POSE_CASTING = 2;
    /** Crouched, watchful — the hidden-cultivator observing pose (Wang Lin watching wolves). */
    public static final int POSE_OBSERVING = 3;
    /** Combat-ready stance — the defender guarding the perimeter. */
    public static final int POSE_GUARDING = 4;
    /** Walking with purpose toward a target — cultivator moves decisively. CRON-COMPLETIONIST-44. */
    public static final int POSE_PURSUING = 5;
    /** Relaxed stance, facing a companion — cultivator socializes. CRON-COMPLETIONIST-44. */
    public static final int POSE_SOCIALIZING = 6;
    /**
     * CRON-130: Sword-flight pose (御剑飞行). Body leaned forward, arms swept back,
     * robe billowing upward. Active while CultivatorFlightGoal is running.
     * Canon: cultivators at Foundation Establishment (筑基) or higher may ride
     * a flying sword. Below Foundation, the cultivator must walk — Qi
     * Condensation cannot sustain the qi expenditure of sword flight.
     */
    public static final int POSE_FLYING = 7;

    public int getCultivatorPose() {
        return this.entityData.get(DATA_POSE);
    }

    public void setCultivatorPose(int pose) {
        this.entityData.set(DATA_POSE, pose);
    }

    // ── Activity lock (reasoning-engine cycle) ──────────────────────────

    /**
     * When non-zero, the entity is <b>activity-locked</b> — its AI is suppressed
     * and it holds its current position + pose. This is how the
     * {@link dev.ergenverse.simulation.settlement.ActorReasoningEngine} makes
     * an entity visibly BEHAVE its reasoning: Wang Lin freezes at the treeline
     * in POSE_OBSERVING, the patriarch freezes at the gate in POSE_GUARDING,
     * while the others flee home. The lock expires at this tick; the
     * {@link dev.ergenverse.simulation.settlement.ActorMaterializer} clears it
     * when the threat expires, resuming normal AI.
     *
     * <p>Per the user's directive: the same wolf event should produce visibly
     * different behavior. Without this lock, the entity's RandomStrollGoal
     * would wander Wang Lin away from his observing vantage immediately. The
     * lock makes the reasoning-derived position + pose <b>observable</b>.
     */
    private long activityLockExpiryTick = 0L;

    /** True if the entity is currently activity-locked (AI suppressed, holding pose). */
    public boolean isActivityLocked() {
        return activityLockExpiryTick > 0L;
    }

    /**
     * Lock the entity to a reasoning-derived activity: suppress AI, set the
     * pose, hold position until the expiry tick.
     */
    public void lockToActivity(int pose, long expiryTick) {
        this.entityData.set(DATA_POSE, pose);
        this.activityLockExpiryTick = expiryTick;
        // AI suppression happens in {@link #aiStep()} (checking
        // isActivityLocked). We deliberately do NOT use setNoAi(true) because
        // that flag persists to NBT — if the chunk unloaded while locked, the
        // entity would be stuck frozen on reload. The transient field approach
        // means the lock clears on reload (the threat will have expired or the
        // materializer will re-evaluate).
    }

    /**
     * Release the activity lock: resume normal AI. Called by the
     * {@link dev.ergenverse.simulation.settlement.ActorMaterializer} when the
     * threat has expired and the actor should return to daily rhythm.
     */
    public void releaseActivityLock() {
        if (activityLockExpiryTick > 0L) {
            this.activityLockExpiryTick = 0L;
            this.entityData.set(DATA_POSE, POSE_IDLE);
        }
    }

    /** The tick at which the current activity lock expires (0 if not locked). */
    public long getActivityLockExpiryTick() {
        return activityLockExpiryTick;
    }

    public boolean isMeditating() {
        return this.entityData.get(DATA_POSE) == POSE_MEDITATING;
    }

    public boolean isCasting() {
        return this.entityData.get(DATA_POSE) == POSE_CASTING;
    }

    /** CRON-COMPLETIONIST-30: True when the cultivator is in POSE_OBSERVING (crouched, watchful). */
    public boolean isObserving() {
        return this.entityData.get(DATA_POSE) == POSE_OBSERVING;
    }

    /** CRON-COMPLETIONIST-30: True when the cultivator is in POSE_GUARDING (combat-ready stance). */
    public boolean isGuarding() {
        return this.entityData.get(DATA_POSE) == POSE_GUARDING;
    }

    /** CRON-COMPLETIONIST-44: True when in POSE_PURSUING (walking with purpose toward a target). */
    public boolean isPursuing() {
        return this.entityData.get(DATA_POSE) == POSE_PURSUING;
    }

    /** CRON-COMPLETIONIST-44: True when in POSE_SOCIALIZING (relaxed, facing a companion). */
    public boolean isSocializing() {
        return this.entityData.get(DATA_POSE) == POSE_SOCIALIZING;
    }

    /**
     * CRON-130: True when in POSE_FLYING (sword-flight / 御剑飞行).
     * CultivatorFlightGoal sets this while flying; renderer reads it to
     * trigger the flight animation (forward lean, swept arms, robe billow).
     */
    public boolean isFlying() {
        return this.entityData.get(DATA_POSE) == POSE_FLYING;
    }

    /**
     * CRON-130: Convenience setter for the flight pose. Pass true to enter
     * POSE_FLYING; pass false to return to POSE_IDLE. Used by
     * CultivatorFlightGoal on start/stop. Direct setCultivatorPose(POSE_FLYING)
     * is also valid — this method exists for symmetry with isFlying().
     */
    public void setFlying(boolean flying) {
        this.entityData.set(DATA_POSE, flying ? POSE_FLYING : POSE_IDLE);
    }

    /**
     * CRON-130: Returns true if the cultivator's cultivation realm is at or
     * above Foundation Establishment (筑基) — the canonical minimum realm for
     * sword flight (御剑飞行). Qi Condensation (练气) and mortal cannot fly.
     *
     * <p>Realm strings come from {@link #getCultivationRealm()} (synced data,
     * populated from canon JSON {@code cultivation_realm} or {@code realm}).
     * Recognized realm strings (case-insensitive, whitespace/separator
     * agnostic): {@code foundation}, {@code core_formation}, {@code nascent_soul},
     * {@code soul_formation}, {@code soul_transformation}, {@code infant_transformation},
     * {@code ascendant}, {@code void_amassing}, and any realm containing the
     * substrings "foundation", "core", "soul", "transcend", "ascendant", "void",
     * "ancient", or "yang". Mortal and Qi Condensation explicitly return false.
     *
     * <p>Canon fidelity (web-search verified 2026-07-27):
     * <ul>
     *   <li>Baidu Baike 仙逆 — 筑基 (Foundation Establishment) is the realm at
     *       which cultivators gain the ability to fly on swords.</li>
     *   <li>Wang Lin first observes Li Muwan flying on a sword when she visits
     *       Heng Yue Sect — she is at Foundation Establishment.</li>
     *   <li>Wang Lin himself first flies after reaching Foundation Establishment
     *       mid-novel (no specific chapter cited to avoid fabrication).</li>
     * </ul>
     * NO fabricated chapter citations.
     */
    public boolean isFoundationOrHigher() {
        String realm = getCultivationRealm();
        if (realm == null || realm.isEmpty()) return false;
        String r = realm.trim().toLowerCase(java.util.Locale.ROOT);
        if (r.isEmpty()) return false;
        // Explicit mortal / qi-condensation rejection
        if (r.equals("mortal") || r.equals("mortal_body")
                || r.equals("qi_condensation") || r.equals("qi")
                || r.equals("refining_qi") || r.equals("qi_refining")
                || r.equals("练气") || r.equals("凡人")) {
            return false;
        }
        // Foundation-or-higher realm keywords (the realm can fly)
        return r.contains("foundation") || r.contains("core")
                || r.contains("soul") || r.contains("transcend")
                || r.contains("transformation") || r.contains("ascendant")
                || r.contains("void") || r.contains("ancient")
                || r.contains("yang") || r.contains("筑基")
                || r.contains("结丹") || r.contains("元婴")
                || r.contains("化神") || r.contains("婴变")
                || r.contains("问鼎") || r.contains("窥涅")
                || r.contains("净涅") || r.contains("碎涅");
    }

    // ── CRON-COMPLETIONIST-19: Cognitive look-target + attention lock ──

    /**
     * Transient (non-synced, non-persisted) flag: when true, the entity is
     * cognition-absorbed and the vanilla {@link RandomLookAroundGoal} should
     * NOT fire. Set by {@link dev.ergenverse.entity.ai.CognitionDrivenGoal}
     * when a Commitment is active. Cleared when the commitment ends.
     *
     * <p>The user's directive: an observing NPC "doesn't respond immediately
     * to player." This flag is how we honor that — without it, the vanilla
     * random-look goal would snap the NPC's head toward passing players,
     * destroying the absorbed-in-observation read.
     */
    private boolean cognitiveAttentionLock = false;

    /** True while the NPC is cognition-absorbed (RandomLookAroundGoal suppressed). */
    public boolean isCognitiveAttentionLocked() {
        return cognitiveAttentionLock;
    }

    /**
     * Set the cognitive attention lock. Called by CognitionDrivenGoal when
     * a Commitment is active (true) or ends (false). While locked, the
     * entity's head tracks ONLY the cognitive look-target, not random stuff.
     */
    public void setCognitiveAttentionLock(boolean locked) {
        this.cognitiveAttentionLock = locked;
    }

    /**
     * Set the cognitive look-target (world coordinates). Called server-side
     * by CognitionDrivenGoal each tick from the active Commitment's primary
     * perceived target. Pass NaN for any component to clear the target.
     *
     * <p>The renderer reads these and lerps the head toward them with
     * micro-saccade noise — no snap rotation.
     */
    public void setCognitiveLookTarget(float x, float y, float z) {
        this.entityData.set(DATA_LOOK_TARGET_X, x);
        this.entityData.set(DATA_LOOK_TARGET_Y, y);
        this.entityData.set(DATA_LOOK_TARGET_Z, z);
    }

    /** Clear the cognitive look-target (no target — vanilla look control runs). */
    public void clearCognitiveLookTarget() {
        this.entityData.set(DATA_LOOK_TARGET_X, Float.NaN);
        this.entityData.set(DATA_LOOK_TARGET_Y, Float.NaN);
        this.entityData.set(DATA_LOOK_TARGET_Z, Float.NaN);
    }

    /** True if a cognitive look-target is currently set. */
    public boolean hasCognitiveLookTarget() {
        return !Float.isNaN(this.entityData.get(DATA_LOOK_TARGET_X));
    }

    /** X component of the cognitive look-target (NaN if none). */
    public float getCognitiveLookTargetX() {
        return this.entityData.get(DATA_LOOK_TARGET_X);
    }

    /** Y component of the cognitive look-target (NaN if none). */
    public float getCognitiveLookTargetY() {
        return this.entityData.get(DATA_LOOK_TARGET_Y);
    }

    /** Z component of the cognitive look-target (NaN if none). */
    public float getCognitiveLookTargetZ() {
        return this.entityData.get(DATA_LOOK_TARGET_Z);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CRON-COMPLETIONIST-21: Acting Layer — Performance channel accessors
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Set the seven Performance channels atomically (from a server-side
     * Commitment interpretation). Pass NaN for all to clear (no performance).
     *
     * <p>The renderer reads these each frame and drives the body's channels
     * (head lerp-speed, saccade amplitude, glance-away frequency, torso
     * tension, breathing rate, weight shift, hand position) INDEPENDENTLY.
     * This replaces the fixed-pose projection of CRON-19 with the
     * channel-based acting the user's 2026-07-26 review demands.
     */
    public void setPerformance(float focus, float urgency, float confidence,
                               float concealment, float tension, float patience,
                               float fatigue) {
        this.entityData.set(DATA_PERF_FOCUS, focus);
        this.entityData.set(DATA_PERF_URGENCY, urgency);
        this.entityData.set(DATA_PERF_CONFIDENCE, confidence);
        this.entityData.set(DATA_PERF_CONCEALMENT, concealment);
        this.entityData.set(DATA_PERF_TENSION, tension);
        this.entityData.set(DATA_PERF_PATIENCE, patience);
        this.entityData.set(DATA_PERF_FATIGUE, fatigue);
    }

    /** Clear all Performance channels (no active commitment → vanilla pose). */
    public void clearPerformance() {
        this.entityData.set(DATA_PERF_FOCUS, Float.NaN);
        this.entityData.set(DATA_PERF_URGENCY, Float.NaN);
        this.entityData.set(DATA_PERF_CONFIDENCE, Float.NaN);
        this.entityData.set(DATA_PERF_CONCEALMENT, Float.NaN);
        this.entityData.set(DATA_PERF_TENSION, Float.NaN);
        this.entityData.set(DATA_PERF_PATIENCE, Float.NaN);
        this.entityData.set(DATA_PERF_FATIGUE, Float.NaN);
    }

    /** True if a Performance is currently set (active commitment driving acting). */
    public boolean hasPerformance() {
        return !Float.isNaN(this.entityData.get(DATA_PERF_FOCUS));
    }

    public float getPerfFocus() { return this.entityData.get(DATA_PERF_FOCUS); }
    public float getPerfUrgency() { return this.entityData.get(DATA_PERF_URGENCY); }
    public float getPerfConfidence() { return this.entityData.get(DATA_PERF_CONFIDENCE); }
    public float getPerfConcealment() { return this.entityData.get(DATA_PERF_CONCEALMENT); }
    public float getPerfTension() { return this.entityData.get(DATA_PERF_TENSION); }
    public float getPerfPatience() { return this.entityData.get(DATA_PERF_PATIENCE); }
    public float getPerfFatigue() { return this.entityData.get(DATA_PERF_FATIGUE); }

    // ═══════════════════════════════════════════════════════════════════
    //  CRON-COMPLETIONIST-21: Attention Object ownership
    // ═══════════════════════════════════════════════════════════════════
    // The user's 2026-07-26 review: "Eventually [the look target] should
    // become Commitment → Attention Object → Renderer. Then Wang Lin keeps
    // watching THAT wolf even if another wolf walks slightly closer. That
    // tiny detail makes the NPC appear to have intention rather than a
    // targeting heuristic."
    //
    // We cannot track by entity UUID (PerceivedEntity has no UUID), so we pin
    // the attention object's WORLD POSITION at commitment start. Each tick,
    // the look-target resolver prefers the perceived entity NEAREST THE PINNED
    // POSITION (not nearest the NPC). This naturally tracks the same wolf as
    // it moves. If no suitable entity is within the stickiness radius for too
    // long, the pin is released and the resolver falls back to nearest-hostile.

    /** Pinned attention-object world X. NaN = no pin (re-pin to nearest). */
    private float attentionPinX = Float.NaN;
    /** Pinned attention-object world Y. */
    private float attentionPinY = Float.NaN;
    /** Pinned attention-object world Z. */
    private float attentionPinZ = Float.NaN;
    /** Ticks since the pinned object was last seen in perception. Resets on re-sight. */
    private int attentionPinStaleTicks = 0;
    /** Max ticks the pin holds without re-sighting before it releases. */
    private static final int ATTENTION_PIN_MAX_STALE = 120; // ~6s

    /** Pin the attention object to a world position (called at commitment start). */
    public void pinAttentionObject(float x, float y, float z) {
        this.attentionPinX = x;
        this.attentionPinY = y;
        this.attentionPinZ = z;
        this.attentionPinStaleTicks = 0;
    }

    /** Update the pinned position as the tracked entity moves (re-sighted). */
    public void updateAttentionPin(float x, float y, float z) {
        this.attentionPinX = x;
        this.attentionPinY = y;
        this.attentionPinZ = z;
        this.attentionPinStaleTicks = 0;
    }

    /** Mark one tick of staleness (called when the pinned object isn't re-sighted). */
    public void ageAttentionPin() {
        this.attentionPinStaleTicks++;
        if (this.attentionPinStaleTicks > ATTENTION_PIN_MAX_STALE) {
            clearAttentionPin();
        }
    }

    /** Release the attention pin (commitment ended, or pin stale too long). */
    public void clearAttentionPin() {
        this.attentionPinX = Float.NaN;
        this.attentionPinY = Float.NaN;
        this.attentionPinZ = Float.NaN;
        this.attentionPinStaleTicks = 0;
    }

    /** True if an attention-object pin is currently held. */
    public boolean hasAttentionPin() {
        return !Float.isNaN(attentionPinX);
    }

    public float getAttentionPinX() { return attentionPinX; }
    public float getAttentionPinY() { return attentionPinY; }
    public float getAttentionPinZ() { return attentionPinZ; }

    // ═══════════════════════════════════════════════════════════════════
    //  Data-driven initialization
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Configure this entity from canon data + runtime overrides.
     *
     * <p>Called by {@code ReificationScan} after spawn. Reads:
     * <ol>
     *   <li>Canon baseline from {@code data/ergenverse/npcs/<characterId>.json}</li>
     *   <li>Runtime overrides from {@link WorldRuntimeState#getNpcState(String)}</li>
     * </ol>
     *
     * <p>Sets: display name, cultivation realm, HP (from runtime override if
     * present, else canon baseline, else default). Future: position, inventory,
     * behavior profile.
     *
     * @param characterId the canon character ID (e.g. "wang_tiangui")
     * @param runtimeOverride the runtime state tag, or {@code null} if no
     *        override exists (entity uses canon baseline only)
     */
    public void initializeFromData(String characterId, @Nullable CompoundTag runtimeOverride) {
        this.setCharacterId(characterId);

        // ── Load canon baseline (server only — client gets data via sync) ──
        if (!this.level().isClientSide) {
            this.canonData = WorldStateDataLoader.getEntry("npcs", characterId);
            if (this.canonData != null) {
                applyCanonBaseline(this.canonData);
            } else {
                Ergenverse.LOGGER.warn("[EntityCultivator] No canon data for character '{}'", characterId);
            }

            // ── Layer runtime overrides on top ──────────────────────────
            if (runtimeOverride != null && !runtimeOverride.isEmpty()) {
                applyRuntimeOverrides(runtimeOverride);
            }

            // ── Register with the ActorEntityLink ───────────────────────
            // This bridges the simulation-layer Actor to this Minecraft entity,
            // enabling the CognitionDrivenGoal to read the Actor's Intent and
            // drive the entity's physical behavior.
            dev.ergenverse.simulation.intent.ActorEntityLink.onEntitySpawn(this);
        }

        this.initialized = true;
    }

    /**
     * Apply canon baseline fields from the NPC JSON.
     * Reads: name_cn, name_en, cultivation_realm, max_hp (if canon).
     */
    private void applyCanonBaseline(JsonObject data) {
        // Display name — prefer name_cn, fall back to name_en, then ID
        String nameCn = hasString(data, "name_cn") ? data.get("name_cn").getAsString() : null;
        String nameEn = hasString(data, "name_en") ? data.get("name_en").getAsString() : null;
        String displayName = nameCn != null ? nameCn : (nameEn != null ? nameEn : "Unknown");
        if (nameCn != null && nameEn != null) {
            displayName = nameCn + " (" + nameEn + ")";
        }
        this.setDisplayNameCn(displayName);

        // Cultivation realm
        if (hasString(data, "cultivation_realm")) {
            this.setCultivationRealm(data.get("cultivation_realm").getAsString());
        } else if (hasString(data, "realm")) {
            this.setCultivationRealm(data.get("realm").getAsString());
        }

        // Max HP — only if canon specifies (don't invent)
        if (hasNumber(data, "max_hp")) {
            float maxHp = data.get("max_hp").getAsFloat();
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHp);
            this.setHealth(maxHp);
        }

        // CRON-COMPLETIONIST-50: Sect/faction — read from "sect", "faction", or "affiliation"
        String sectId = null;
        if (hasString(data, "sect")) {
            sectId = data.get("sect").getAsString();
        } else if (hasString(data, "faction")) {
            sectId = data.get("faction").getAsString();
        } else if (hasString(data, "affiliation")) {
            sectId = data.get("affiliation").getAsString();
        }
        // Normalize: lowercase, spaces to underscores, strip parentheticals
        if (sectId != null && !sectId.isEmpty()) {
            sectId = sectId.toLowerCase().replaceAll("\\s+", "_")
                    .replaceAll("[()]", "").replaceAll("_+", "_");
            // Strip trailing underscore if any
            if (sectId.endsWith("_")) sectId = sectId.substring(0, sectId.length() - 1);
            this.setSectId(sectId);
        }
        // v1: mortal default (20 HP) applies via createAttributes() if canon silent
    }

    /**
     * Apply t>0 runtime overrides (damage taken, etc.).
     * Reads: current_hp (if present).
     */
    private void applyRuntimeOverrides(CompoundTag override) {
        if (override.contains("current_hp")) {
            this.setHealth(override.getFloat("current_hp"));
        }
        // Future: position drift, inventory, learned techniques
    }

    // ── Gson null-safe helpers ──────────────────────────────────────────

    private static boolean hasString(JsonObject obj, String key) {
        return obj.has(key) && obj.get(key).isJsonPrimitive() && obj.get(key).getAsJsonPrimitive().isString();
    }

    private static boolean hasNumber(JsonObject obj, String key) {
        return obj.has(key) && obj.get(key).isJsonPrimitive() && obj.get(key).getAsJsonPrimitive().isNumber();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  AI / Goals
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected void registerGoals() {
        // ── Cognition-driven AI ──
        // CognitionDrivenGoal is the bridge from the simulation layer's Intent
        // to the Minecraft entity's physical behavior. When active, it controls
        // movement and look so the NPC acts on its current Intent (e.g.
        // AVOID_REVEALING_STRENGTH → moves away from player, OBSERVE_FROM_DISTANCE
        // → moves to vantage point, etc.). When no Intent is active, it yields
        // so the fallback goals (RandomStroll, RandomLookAround) run.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // NpcGiftOfferGoal: Article XXIV taken further — NPC walks up to the
        // player and OFFERS a gift/teaching, using the existing
        // ManifestationGiftSystem four-question gate. Priority 1 (above
        // NpcInitiationGoal=2). Only activates for NPCs with offers_gifts=true
        // in their JSON data and only after canon-faithful gates pass.
        this.goalSelector.addGoal(1, new dev.ergenverse.entity.ai.NpcGiftOfferGoal(this));
        // NpcInitiationGoal: Article XXIV — NPCs initiate gameplay (NPC→Player).
        // Priority 2, above CognitionDrivenGoal(3). Fires when player enters range,
        // sends a canon-faithful initiation line from NPC JSON data.
        this.goalSelector.addGoal(2, new dev.ergenverse.entity.ai.NpcInitiationGoal(this));
        // Desire-driven goal: Article XXXI — NPCs ACT on their desires.
        // Priority 2 (same tier as NpcInitiationGoal). Uses MOVE+LOOK so it
        // preempts LOOK-only goals when active. Reads DesireState data from
        // the Actor system. For "approach" desires, the NPC physically walks
        // to the target and speaks. For "line" desires, speaks when nearby.
        // Per Art XL: this produces a Living Moment, not architecture.
        this.goalSelector.addGoal(2, new dev.ergenverse.entity.ai.NpcDesireGoal(this));
        // NpcSectMissionGoal: Article XXIV/XXII — NPCs offer sect missions.
        // Priority 2 (same as NpcInitiationGoal; both are one-shot LOOK-only goals
        // with cooldowns, so the scheduler alternates between them naturally).
        this.goalSelector.addGoal(2, new dev.ergenverse.entity.ai.NpcSectMissionGoal(this));
        // NpcLectureGoal: Article XXIV — elder NPCs invite players to attend
        // lectures during scheduled time windows. Priority 2 (same tier as
        // NpcInitiationGoal and NpcSectMissionGoal; all use Flag.LOOK and
        // are one-shot with cooldowns, so MC scheduler alternates).
        this.goalSelector.addGoal(2, new dev.ergenverse.entity.ai.NpcLectureGoal(this));
        // CRON-COMPLETIONIST-12: NpcScheduleGoal REMOVED per Article XLV §3.
        // The deprecated timetable-based schedule has been replaced entirely by
        // CognitionDrivenGoal at priority 3. The pipeline is now:
        //   pressures → Mind → Reasoning → Commitment → Execution
        // The world owns pressures. The NPC owns priorities. A Commitment
        // persists across ticks — the NPC does not re-evaluate every tick.
        // NpcScheduleGoal.java is retained in the source tree for git history
        // but is no longer referenced by any live code.
        this.goalSelector.addGoal(3, new dev.ergenverse.entity.ai.CognitionDrivenGoal(this));
        // NpcReactToWorldGoal: Living Moments bridge — NPCs observe beast events
        // (hunts, flees, rests) and react with contextual dialogue, look-at
        // behavior, and WorldHistory memory recording. This goal subscribes
        // to the WorldEventBus on "beast.*" topics. Priority 5 (below
        // CognitionDrivenGoal so intent-based behavior takes precedence).
        this.goalSelector.addGoal(5, new dev.ergenverse.entity.ai.NpcReactToWorldGoal(this));
        // CRON-COMPLETIONIST-57: CultivatorMeditationGoal — cultivator meditates when idle.
        // Priority 6 (above RandomStroll=7, below NpcReactToWorld=5). Fires organically:
        // sets POSE_MEDITATING → triggers CultivatorRobeModel zhan zhuang animation.
        // Previously a dead stub; now fully functional with random duration + cooldown.
        this.goalSelector.addGoal(6, new dev.ergenverse.entity.ai.CultivatorMeditationGoal(this));
        // CRON-COMPLETIONIST-102: FollowPlayerGoal — companion follow AI for
        // revived Li Muwan. Priority 4 (between CognitionDrivenGoal=3 and
        // NpcReactToWorld=5). Always registered but only activates when
        // isFollowingPlayer() returns true (set by LiMuwanRevivalEvent).
        // Canon: "两人踏天同行" — Li Muwan follows Wang Lin as his eternal
        // companion after the successful revival.
        this.goalSelector.addGoal(4, new dev.ergenverse.entity.ai.FollowPlayerGoal(this));
        // CRON-130: CultivatorFlightGoal — sword-flight (御剑飞行) for Foundation+
        // cultivators. Priority 5 (above meditation=6; below react-to-world=5
        // actually we use priority 5 here so both flight and react can coexist
        // — flight claims MOVE+LOOK, react claims LOOK only, so when flight is
        // active it preempts react's LOOK via the MOVE flag. When flight is
        // inactive, react runs normally).
        // Canon: cultivators at 筑基 (Foundation Establishment) or higher may
        // ride a flying sword. Qi Condensation and mortal cultivators walk.
        // Activates only when the cultivator has a far target (combat target
        // >16 blocks away, or far navigation target). Below that, walking is
        // used (more canon-faithful — cultivators don't fly for short hops).
        this.goalSelector.addGoal(5, new dev.ergenverse.entity.ai.CultivatorFlightGoal(this));
        this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.RandomStrollGoal(this, 0.35D));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // ── Combat AI (Constitution: cultivators must fight, not just die) ──
        // Prior to these goals, EntityCultivator had ZERO combat capability —
        // a Nascent Soul elder would stand still while a zombie punched it to death.
        // CultivatorCombatGoal: melee (realm-scaled damage) + pursuit.
        // CultivatorSwordQiGoal: ranged sword-qi projectile (Qi Condensation+).
        // Both claim MOVE+LOOK, so they preempt wandering/cognition when a target exists.
        this.goalSelector.addGoal(2, new dev.ergenverse.entity.ai.CultivatorCombatGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new dev.ergenverse.entity.ai.CultivatorSwordQiGoal(this));

        // ── CRON-COMPLETIONIST-108: Ancient God combat goals (Tuo Sen only) ──
        // These goals are registered globally but no-op for every cultivator
        // except Tuo Sen (characterId == "tuo_sen"). The activation gate is
        // in each goal's canUse(): if characterId doesn't match, return false.
        // This avoids needing a per-entity TuoSenEntityCultivator subclass —
        // the standard EntityCultivator handles all canon NPCs, with character-
        // specific behavior gated inside the goals' canUse().
        //
        // AncientGodPressGoal: AoE ground pound (4-16 block range, 80 damage,
        //   leap + crash + 6-block AoE knockback). Priority 2 (same tier as
        //   CultivatorCombatGoal — they share MOVE+LOOK+JUMP flags, so MC's
        //   scheduler picks whichever is ready first; canUse() gating ensures
        //   only one fires per tick).
        // AncientGodStarGazeGoal: long-range paralysis (10-30 block range,
        //   1.5s charge + SLOWNESS IV + WEAKNESS II + DARKNESS + 30 damage).
        //   Priority 2 (LOOK only — Tuo Sen can't move during the charge, so
        //   this goal doesn't claim MOVE; the press goal claims MOVE for the
        //   closing phase).
        //
        // Canon (web-search verified 2026-07-26 via Baidu Baike + Sohu + 163):
        //   - 8-star Ancient God, born from Tu Si's Ink Flow Split Soul Technique
        //   - '拓森现身朱雀墓' — reappears at Suzaku Tomb
        //   - Ancient Gods fight with raw god-body power, not mortal weapons
        // Closes the CRON-107 self-critique #14 documented gap.
        this.goalSelector.addGoal(2, new dev.ergenverse.entity.ai.AncientGodPressGoal(this));
        this.goalSelector.addGoal(2, new dev.ergenverse.entity.ai.AncientGodStarGazeGoal(this));

        // ── Target selectors — WITHOUT these, getTarget() is always null and combat goals never fire ──
        // HurtByTargetGoal: retaliate when attacked (canon: a cultivator does not stand idle when struck).
        // NearestAttackableTargetGoal(Monster): defend the sect against hostile mobs (zombies, skeletons, etc.).
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                this, net.minecraft.world.entity.monster.Monster.class, true));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Hibernation & persistence
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Canon entities NEVER despawn due to distance. They persist in the
     * world until explicitly removed (killed, or canon event removes them).
     */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    /**
     * Hibernation: if no player is within {@link #HIBERNATION_RANGE}, skip
     * AI processing. The entity still exists in the world (rendered if in
     * view, loaded in chunk), but its brain is off — no pathfinding, no
     * goal ticking. This prevents the "100 NPCs all ticking" performance
     * death trap.
     */
    @Override
    public void aiStep() {
        if (this.level().isClientSide) {
            // Client: always animate (rendering layer handles LOD)
            super.aiStep();
            return;
        }
        // Activity lock: the reasoning engine has placed this entity at a
        // specific position with a specific pose (Wang Lin observing at the
        // treeline, the patriarch guarding the gate). Suppress all goal AI so
        // the entity holds its reasoning-derived state and the player can SEE
        // the differentiated behavior. Gravity/drowning still apply via super.
        if (isActivityLocked()) {
            this.goalSelector.getRunningGoals().forEach(g -> g.stop());
            super.aiStep();
            return;
        }
        // CRON-COMPLETIONIST-19: Cognitive attention lock. When the NPC is
        // cognition-absorbed (a Commitment is active and the CognitionDrivenGoal
        // has set the attention lock), suppress RandomLookAroundGoal so the
        // NPC's head tracks the cognitive look-target, not random stuff. The
        // user's directive: an observing NPC "doesn't respond immediately to
        // player." We do NOT stop ALL goals — CognitionDrivenGoal must keep
        // running (it owns the look-target). We only stop the random-look
        // specifically. Other goals (combat, gift-offer) still fire if their
        // conditions trigger, which is correct — an observing NPC attacked
        // by a zombie should still defend itself.
        if (cognitiveAttentionLock) {
            this.goalSelector.getRunningGoals()
                    .filter(wg -> wg.getGoal() instanceof net.minecraft.world.entity.ai.goal.RandomLookAroundGoal)
                    .forEach(wg -> wg.stop());
        }
        // Server: hibernate if no player nearby
        if (this.level().getNearestPlayer(this, HIBERNATION_RANGE) == null) {
            // Hibernating — minimal tick. Still apply gravity, drowning, etc.
            // but skip goalSelector and navigation updates.
            this.goalSelector.getRunningGoals().forEach(g -> g.stop());
            // Call super but with navigation disabled — gravity still works
            super.aiStep();
            return;
        }
        super.aiStep();
    }

    /**
     * Sync current entity state to the runtime layer.
     *
     * <p>Called from {@link #addAdditionalSaveData} (which fires on chunk
     * unload — Minecraft serializes the entity to NBT when its chunk
     * unloads) and from {@link #die} (on death). This is the dematerialization
     * step: the entity's current HP and position are written to
     * {@link WorldRuntimeState} so the next materialization reads the
     * updated state.
     *
     * <p>Note: in MC 1.20.1, {@code Entity.setRemoved(RemovalReason)} is final
     * and cannot be overridden. The NBT save path is the correct hook for
     * chunk-unload persistence — it fires at the same time and is the
     * vanilla-sanctioned way to persist entity state.
     */
    private void syncStateToRuntime() {
        if (this.level().isClientSide || !this.initialized || this.getCharacterId().isEmpty()) return;
        ServerLevel serverLevel = (ServerLevel) this.level();
        WorldRuntimeState runtime = WorldRuntimeState.get(serverLevel);
        CompoundTag state = runtime.getNpcState(this.getCharacterId());
        if (state == null) {
            state = new CompoundTag();
        }
        // Record current HP (the most common mutation: damage taken)
        state.putFloat("current_hp", this.getHealth());
        // Record last position (for re-materialization at the same spot)
        state.putDouble("last_pos_x", this.getX());
        state.putDouble("last_pos_y", this.getY());
        state.putDouble("last_pos_z", this.getZ());
        state.putLong("last_seen_tick", serverLevel.getGameTime());
        runtime.updateNpcState(this.getCharacterId(), state);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  NBT (entity save/load — for chunk unload/reload)
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("CharacterId", this.getCharacterId());
        compound.putString("DisplayName", this.getDisplayNameCn());
        compound.putString("CultivationRealm", this.getCultivationRealm());
        compound.putBoolean("Initialized", this.initialized);
        compound.putInt("CultivatorPose", this.getCultivatorPose());
        // Sync to runtime layer on every NBT save (fires on chunk unload).
        // This is the dematerialization persistence hook.
        syncStateToRuntime();
        // Sever the ActorEntityLink — the entity is being serialized/unloaded.
        // The Actor remains in the registry (simulation continues at territory
        // level), but the link to this Minecraft entity is gone.
        if (!this.level().isClientSide) {
            dev.ergenverse.simulation.intent.ActorEntityLink.onEntityUnload(this);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setCharacterId(compound.getString("CharacterId"));
        if (compound.contains("DisplayName")) {
            this.setDisplayNameCn(compound.getString("DisplayName"));
        }
        if (compound.contains("CultivationRealm")) {
            this.setCultivationRealm(compound.getString("CultivationRealm"));
        }
        this.initialized = compound.getBoolean("Initialized");
        if (compound.contains("CultivatorPose")) {
            this.setCultivatorPose(compound.getInt("CultivatorPose"));
        }
        // Re-establish the ActorEntityLink on chunk reload.
        // The entity is materializing from NBT — link it back to its Actor.
        if (!this.level().isClientSide && this.initialized && !this.getCharacterId().isEmpty()) {
            dev.ergenverse.simulation.intent.ActorEntityLink.onEntitySpawn(this);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Player Interaction — Layer 3 History Hook
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Record a player's right-click interaction with this NPC in the
     * Layer 3 emergent history (NpcMemory).
     *
     * <p>NOTE: {@code Mob.interact()} is final in MC 1.20.1, so we
     * cannot override it. Instead, {@link HistoryEvents} listens for
     * {@code PlayerInteractEvent.EntityInteract} and calls this method
     * when the target is an EntityCultivator.
     *
     * <p>v1: records an INTERACTION memory. v2: triggers dialogue system,
     * trade UI, quest offers based on trust score and NPC personality.
     *
     * @param serverPlayer the player who interacted
     */
    public void recordPlayerInteraction(net.minecraft.server.level.ServerPlayer serverPlayer) {
        if (this.level().isClientSide) return;
        if (this.getCharacterId().isEmpty()) return;

        long tick = this.level().getGameTime();
        String detail = "Player interacted with " + this.getDisplayNameCn() +
                " (" + this.getCharacterId() + ")";
        dev.ergenverse.history.HistoryManager.onNpcInteraction(
                serverPlayer, this.getCharacterId(), "RIGHT_CLICK", detail, tick);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Death handling
    // ═══════════════════════════════════════════════════════════════════

    /**
     * On death: record the death in runtime state (canon death event, or
     * player-caused divergence). The canon DB is NOT modified — the t₀
     * archive remains pristine.
     *
     * <p>Additionally, if this NPC is a known Cave World owner (e.g. the
     * Seven-Colored Daoist), transfer ownership of their world to the killer.
     * Canon: "Wang Lin's escape = kill the owner = become the new owner."
     */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide && !this.getCharacterId().isEmpty()) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            WorldRuntimeState runtime = WorldRuntimeState.get(serverLevel);
            CompoundTag state = runtime.getNpcState(this.getCharacterId());
            if (state == null) state = new CompoundTag();
            state.putBoolean("is_dead", true);
            state.putLong("death_tick", serverLevel.getGameTime());
            state.putString("death_cause", source.getMsgId());
            runtime.updateNpcState(this.getCharacterId(), state);
            Ergenverse.LOGGER.info("[EntityCultivator] Character '{}' died at tick {} (cause: {})",
                    this.getCharacterId(), serverLevel.getGameTime(), source.getMsgId());

            // ── CRON-99: Li Muwan soul capture ──
            // If this dying cultivator is Li Muwan, fire the Heaven-Defying
            // Bead soul-capture event. This is the SOLE caller of
            // HeavenDefyingBeadItem.setLiMuwanSoul — closes the CRON-95
            // known gap where setLiMuwanSoul was defined but never invoked.
            // Canon: 李慕婉 perishes after her 结婴 (Nascent Soul formation)
            // fails; Wang Lin captures her 元婴 into the 天逆珠. This becomes
            // his central motivation for the rest of the novel.
            // See dev.ergenverse.wanglin.bead.LiMuwanSoulCaptureEvent for
            // full canon basis and divergence safeguards.
            if (LiMuwanSoulCaptureEvent.CHARACTER_ID.equals(this.getCharacterId())) {
                LiMuwanSoulCaptureEvent.handleLiMuwanDeath(
                        serverLevel, source, this.blockPosition());
            }

            // ── Layer 3: Record NPC death in emergent history if player-caused ──
            if (source.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                String npcName = this.getDisplayNameCn();
                dev.ergenverse.history.HistoryManager.onNpcCombat(
                        serverPlayer, this.getCharacterId(), npcName,
                        true, serverLevel.getGameTime());

                // ── Cave World Ownership transfer ──
                // If this NPC is a known world owner (e.g. seven_colored_daoist), transfer
                // ownership to the killer. Per canon: killing the owner = becoming the owner.
                String ownedWorld = dev.ergenverse.wanglin.CaveWorldOwnership.getOwnedWorldForNpc(this.getCharacterId());
                if (ownedWorld != null && !dev.ergenverse.wanglin.CaveWorldOwnership.isOwnerReplaced(ownedWorld)) {
                    dev.ergenverse.cultivation.CultivationState cstate =
                            dev.ergenverse.cultivation.CultivationCapability.getOrThrow(serverPlayer);
                    String killerRealmId = cstate.getCurrentRealm().name().toLowerCase().replace(" ", "_");
                    dev.ergenverse.wanglin.CaveWorldOwnership.Ownership newOwner =
                            dev.ergenverse.wanglin.CaveWorldOwnership.transferOwnership(
                                    ownedWorld,
                                    serverPlayer.getUUID().toString(),
                                    serverPlayer.getName().getString(),
                                    killerRealmId,
                                    serverLevel); // persist ownership to world save

                    if (newOwner != null) {
                        serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "\u00A7c\u00A7l\u26A0 CAVE WORLD OWNERSHIP TRANSFERRED!\u00A7r\n" +
                                "\u00A7aYou have killed \u00A7f" + this.getDisplayNameCn() + "\u00A7a.\n" +
                                "\u00A7fYou are now the Lord of the " + newOwner.worldLayerName + ".\n" +
                                "\u00A77The Realm-Sealing Grand Array dissolves. The cultivation ceiling is lifted.\n" +
                                "\u00A78Joss Flame harvest now flows to YOU.\n" +
                                "\u00A77You may free the mortals... or continue the harvest."));
                        dev.ergenverse.history.HistoryManager.onDiscovery(serverPlayer,
                                "cave_world_ownership_transfer",
                                "Killed " + this.getDisplayNameCn() + " and inherited the " + newOwner.worldLayerName + ".",
                                serverLevel.getGameTime());
                        Ergenverse.LOGGER.info("[Ergenverse] Cave World ownership transferred to {} (killed {}).",
                                serverPlayer.getName().getString(), this.getCharacterId());
                    }
                }
            }
        }
    }
}
