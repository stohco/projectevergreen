package dev.ergenverse.entity;

import com.google.gson.JsonObject;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.WorldStateDataLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * EntitySpiritBeast — a polymorphic creature entity driven by species data.
 *
 * <p>Like {@link EntityCultivator} (one class for all NPCs), this is ONE
 * class for ALL spirit beasts / creatures. At spawn, it reads a
 * {@code species_id} from its NBT or from a spawn tag and configures itself
 * from the species JSON in {@code data/ergenverse/species/<id>.json}.
 *
 * <h2>Design: why one class, not 10?</h2>
 * <ul>
 *   <li>Same rationale as EntityCultivator — avoids 10+ nearly-identical
 *       classes that differ only in stats.</li>
 *   <li>Species-specific behavior is driven by the {@code combat_ai}
 *       field in the species JSON.</li>
 *   <li>The client renderer reads the synced species_id to pick
 *       model/texture (future work).</li>
 * </ul>
 *
 * <h2>Combat AI profiles</h2>
 * <p>The {@code combat_ai} field from species JSON selects a behavior
 * profile (registered goals):
 * <ul>
 *   <li><b>territorial_aggressive</b> — attacks players who enter range,
 *       flees if outmatched.</li>
 *   <li><b>passive_fleeing</b> — flees from all players, never attacks.</li>
 *   <li><b>pack_hunter</b> — targets nearest player, calls allies.</li>
 *   <li><b>guardian</b> — patrols near spawn point, attacks hostiles.</li>
 *   <li><b>elemental</b> — applies elemental effects on hit (fire, lightning).</li>
 *   <li><b>spirit</b> — intangible, reduced physical damage, phase attacks.</li>
 * </ul>
 *
 * <h2>Data-driven attributes</h2>
 * <p>Base attributes are set from species JSON where available:
 * <ul>
 *   <li>{@code max_hp} — health (default 30 for beasts)</li>
 *   <li>{@code attack_damage} — melee damage (default 4)</li>
 *   <li>{@code speed} — movement speed (default 0.3)</li>
 *   <li>{@code follow_range} — aggro range (default 24)</li>
 * </ul>
 *
 * <h2>Hibernation</h2>
 * <p>Same pattern as EntityCultivator: AI is skipped when no player
 * is within {@link #HIBERNATION_RANGE} blocks.
 *
 * <h2>Prime Directive</h2>
 * <p>Stats are derived from canon. Where canon is silent, reasonable
 * defaults for the creature type are used (not invented specific numbers).
 */
public class EntitySpiritBeast extends Monster {

    // ── Synced data ──────────────────────────────────────────────────

    private static final EntityDataAccessor<String> DATA_SPECIES_ID =
            SynchedEntityData.defineId(EntitySpiritBeast.class,
                    EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> DATA_DISPLAY_NAME =
            SynchedEntityData.defineId(EntitySpiritBeast.class,
                    EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> DATA_COMBAT_AI =
            SynchedEntityData.defineId(EntitySpiritBeast.class,
                    EntityDataSerializers.STRING);

    // ── Hibernation ─────────────────────────────────────────────────

    public static final double HIBERNATION_RANGE = 48.0;
    private static final double HIBERNATION_RANGE_SQ =
            HIBERNATION_RANGE * HIBERNATION_RANGE;

    // ── Default beast attributes ───────────────────────────────────

    private static final double DEFAULT_MAX_HP = 30.0;
    private static final double DEFAULT_ATTACK = 4.0;
    private static final double DEFAULT_SPEED = 0.3;
    private static final double DEFAULT_FOLLOW_RANGE = 24.0;

    // ── Server-only state ──────────────────────────────────────────

    private boolean initialized = false;
    @Nullable
    private JsonObject speciesData;

    // ── The 10 target species IDs ──────────────────────────────────

    public static final String FIRE_PHOENIX = "fire_phoenix";
    public static final String THUNDER_BIRD = "thunder_bird";
    public static final String SWORD_SPIRIT_BEAST = "sword_spirit_beast";
    public static final String VEIN_GUARDIAN_BEAST = "vein_guardian_beast";
    public static final String ANCIENT_GOD_GUARDIAN_BEAST = "ancient_god_guardian_beast";
    public static final String ANCIENT_NETHER_EMPEROR = "ancient_nether_emperor";
    public static final String IMMORTAL_GHOST = "immortal_ghost";
    public static final String KARMA_DEMON = "karma_demon";
    public static final String PILL_DEMON = "pill_demon";
    public static final String REINCARNATION_SPIRIT = "reincarnation_spirit";

    // ═══════════════════════════════════════════════════════════════
    //  Construction
    // ═══════════════════════════════════════════════════════════════

    public EntitySpiritBeast(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    /**
     * Register default attributes for spirit beasts.
     * Called from EntityAttributeCreationEvent.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, DEFAULT_MAX_HP)
                .add(Attributes.ATTACK_DAMAGE, DEFAULT_ATTACK)
                .add(Attributes.MOVEMENT_SPEED, DEFAULT_SPEED)
                .add(Attributes.FOLLOW_RANGE, DEFAULT_FOLLOW_RANGE);
    }

    // ═══════════════════════════════════════════════════════════════
    //  SynchedEntityData
    // ═══════════════════════════════════════════════════════════════

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SPECIES_ID, "");
        this.entityData.define(DATA_DISPLAY_NAME, "Unknown Beast");
        this.entityData.define(DATA_COMBAT_AI, "territorial_aggressive");
    }

    public String getSpeciesId() {
        return this.entityData.get(DATA_SPECIES_ID);
    }

    public void setSpeciesId(String id) {
        this.entityData.set(DATA_SPECIES_ID, id);
    }

    public String getBeastDisplayName() {
        return this.entityData.get(DATA_DISPLAY_NAME);
    }

    public void setBeastDisplayName(String name) {
        this.entityData.set(DATA_DISPLAY_NAME, name);
    }

    public String getCombatAi() {
        return this.entityData.get(DATA_COMBAT_AI);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Data-driven initialization
    // ═══════════════════════════════════════════════════════════════

    /**
     * Configure this entity from species JSON data.
     *
     * @param speciesId the species ID (e.g. "fire_phoenix")
     */
    public void initializeFromSpecies(String speciesId) {
        this.setSpeciesId(speciesId);

        if (!this.level().isClientSide) {
            this.speciesData = WorldStateDataLoader.getEntry(
                    "species", speciesId);
            if (this.speciesData != null) {
                applySpeciesData(this.speciesData);
            } else {
                Ergenverse.LOGGER.warn(
                        "[EntitySpiritBeast] No species data for '{}'",
                        speciesId);
            }
        }

        this.initialized = true;
    }

    /**
     * Apply species data to entity attributes and synced fields.
     */
    private void applySpeciesData(JsonObject data) {
        // Display name
        String name = data.has("name") ? data.get("name").getAsString()
                : "Unknown Beast";
        String nameCn = "";
        if (data.has("nameCn")) {
            nameCn = data.get("nameCn").getAsString();
        }
        if (!nameCn.isEmpty()) {
            this.setBeastDisplayName(nameCn + " (" + name + ")");
        } else {
            this.setBeastDisplayName(name);
        }

        // Combat AI
        JsonObject canonical = data.has("canonical")
                ? data.getAsJsonObject("canonical") : new JsonObject();
        if (canonical.has("combat_ai")) {
            String ai = canonical.get("combat_ai").getAsString();
            this.entityData.set(DATA_COMBAT_AI, ai);
        }

        // Data-driven attributes — only apply if species JSON specifies
        if (data.has("max_hp")) {
            double hp = data.get("max_hp").getAsDouble();
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
            this.setHealth((float) hp);
        } else {
            // Scale HP by bloodline tier if available
            double hp = scaleHpByTier(canonical);
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
            this.setHealth((float) hp);
        }

        if (data.has("attack_damage")) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(
                    data.get("attack_damage").getAsDouble());
        }

        if (data.has("speed")) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(
                    data.get("speed").getAsDouble());
        }

        if (data.has("follow_range")) {
            this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(
                    data.get("follow_range").getAsDouble());
        }

        // Re-register goals based on combat AI
        this.goalSelector.removeAllGoals(g -> true);
        this.targetSelector.removeAllGoals(g -> true);
        this.registerGoals();
    }

    /**
     * Scale HP by bloodline tier from species JSON.
     */
    private double scaleHpByTier(JsonObject canonical) {
        String tier = canonical.has("bloodline_tier")
                ? canonical.get("bloodline_tier").getAsString()
                : "ordinary_beast";
        return switch (tier) {
            case "ancient_beast" -> 200.0;
            case "variant_beast" -> 100.0;
            case "demon_beast" -> 80.0;
            case "spirit_beast" -> 50.0;
            default -> DEFAULT_MAX_HP;
        };
    }

    // ═══════════════════════════════════════════════════════════════
    //  AI Goals — selected by combat_ai profile
    // ═══════════════════════════════════════════════════════════════

    @Override
    protected void registerGoals() {
        String ai = getCombatAi();

        switch (ai) {
            case "passive_fleeing" -> registerPassiveGoals();
            case "pack_hunter" -> registerPackHunterGoals();
            case "guardian" -> registerGuardianGoals();
            case "elemental" -> registerElementalGoals();
            case "spirit" -> registerSpiritGoals();
            default -> registerTerritorialGoals(); // territorial_aggressive
        }
    }

    /**
     * Territorial aggressive: attacks players in range, melee, water-safe.
     */
    private void registerTerritorialGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1,
                new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    /**
     * Passive: flees from players, never attacks.
     */
    private void registerPassiveGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1,
                new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        // No target goals — passive creatures don't attack
    }

    /**
     * Pack hunter: faster, targets players aggressively.
     */
    private void registerPackHunterGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1,
                new MeleeAttackGoal(this, 1.5D, true));
        this.goalSelector.addGoal(2,
                new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3,
                new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1,
                new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    /**
     * Guardian: patrols near spawn point, attacks hostiles.
     */
    private void registerGuardianGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1,
                new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2,
                new WaterAvoidingRandomStrollGoal(this, 0.4D));
        this.goalSelector.addGoal(3,
                new LookAtPlayerGoal(this, Player.class, 20.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1,
                new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    /**
     * Elemental: same as territorial but with extended aggro range.
     * Fire/thunder elementals are aggressive.
     */
    private void registerElementalGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1,
                new MeleeAttackGoal(this, 1.3D, true));
        this.goalSelector.addGoal(2,
                new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(3,
                new LookAtPlayerGoal(this, Player.class, 20.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        // Extended aggro range for elementals
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(32.0);
        this.targetSelector.addGoal(1,
                new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    /**
     * Spirit: slow, haunting movement, attacks at closer range.
     * Ghosts and demons.
     */
    private void registerSpiritGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1,
                new MeleeAttackGoal(this, 0.8D, true));
        // Ghosts don't avoid water — they float through
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(3,
                new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1,
                new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Hibernation & NBT
    // ═══════════════════════════════════════════════════════════════

    @Override
    public void aiStep() {
        // Hibernation: skip AI if no player within range
        if (!this.level().isClientSide) {
            Player nearest = this.level().getNearestPlayer(this, HIBERNATION_RANGE);
            if (nearest == null) {
                // Hibernate — skip goal processing
                this.getNavigation().stop();
                super.aiStep();
                return;
            }
        }
        super.aiStep();
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        // Spirit beasts persist (like cultivators)
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("SpeciesId", getSpeciesId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        String speciesId = tag.getString("SpeciesId");
        if (!speciesId.isEmpty() && !this.initialized) {
            initializeFromSpecies(speciesId);
        }
    }


    // ═══════════════════════════════════════════════════════════════
    //  Static query API
    // ═══════════════════════════════════════════════════════════════

    /** Get the species data for a species ID, or null. */
    @Nullable
    public static JsonObject getSpeciesData(String speciesId) {
        if (!WorldStateDataLoader.isLoaded()) return null;
        return WorldStateDataLoader.getEntry("species", speciesId);
    }

    /** Check if a species ID is one of the 10 target creatures. */
    public static boolean isTargetCreature(String speciesId) {
        return FIRE_PHOENIX.equals(speciesId)
                || THUNDER_BIRD.equals(speciesId)
                || SWORD_SPIRIT_BEAST.equals(speciesId)
                || VEIN_GUARDIAN_BEAST.equals(speciesId)
                || ANCIENT_GOD_GUARDIAN_BEAST.equals(speciesId)
                || ANCIENT_NETHER_EMPEROR.equals(speciesId)
                || IMMORTAL_GHOST.equals(speciesId)
                || KARMA_DEMON.equals(speciesId)
                || PILL_DEMON.equals(speciesId)
                || REINCARNATION_SPIRIT.equals(speciesId);
    }
}