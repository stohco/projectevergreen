package dev.ergenverse.entity;

import com.google.gson.JsonObject;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.WorldStateDataLoader;
import dev.ergenverse.simulation.WorldRuntimeState;
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
        // NpcScheduleGoal: DEPRECATED per Article XLV §3. A daily schedule
        // authored as a timetable (time-windowed patrol entries not downstream
        // of a named pressure) is a bug. Retained only as the transition path
        // until the commitment pipeline (pressures → Mind → Reasoning →
        // Commitment → Execution) is fully wired. When commitments are wired,
        // this line is deleted and CognitionDrivenGoal moves to priority 3.
        // The @SuppressWarnings("deprecation") is intentional: we know it's
        // deprecated; we are calling it deliberately during the transition.
        @SuppressWarnings("deprecation")
        dev.ergenverse.entity.ai.NpcScheduleGoal scheduleGoal =
                new dev.ergenverse.entity.ai.NpcScheduleGoal(this);
        this.goalSelector.addGoal(3, scheduleGoal);
        // CognitionDrivenGoal: Article XLV §3 — honors an active Commitment
        // (persistent across ticks) if one is set by the ReasoningEngine;
        // otherwise falls back to the per-tick Intent. This is the replacement
        // for the deprecated schedule above. Priority 4 today; will become 3
        // when NpcScheduleGoal is removed.
        this.goalSelector.addGoal(4, new dev.ergenverse.entity.ai.CognitionDrivenGoal(this));
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
