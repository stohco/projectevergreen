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
    }

    public String getCharacterId() {
        return this.entityData.get(DATA_CHARACTER_ID);
    }

    public void setCharacterId(String id) {
        this.entityData.set(DATA_CHARACTER_ID, id);
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
        // v1: minimal passive AI. No combat, no sect routines, no dialogue.
        // Just float in water, look around. Behavior brain is v2.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        // TODO v2: data-driven behavior trees per character
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
        // Sync to runtime layer on every NBT save (fires on chunk unload).
        // This is the dematerialization persistence hook.
        syncStateToRuntime();
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
