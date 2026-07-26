package dev.ergenverse.entity;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * EREntityTypes — DeferredRegister for all Ergenverse entity types.
 *
 * <p>Mosquito Beast swarms are represented as a single composite server-side
 * entity. The swarm is canonically millions of blood-drinking mosquitoes that
 * absorbed spirit beast blood in Mosquito Valley. Rather than spawning
 * thousands of individual mob entities (which would obliterate server TPS),
 * we model the entire swarm as one Entity with a synced population counter,
 * aggregate damage, and fission mechanics.
 *
 * <p>Registered via {@code EREntityTypes.ENTITY_TYPES.register(modEventBus)}
 * in the {@link Ergenverse} constructor.
 */
public class EREntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Ergenverse.MOD_ID);

    /**
     * Mosquito Swarm — a single composite entity representing thousands to
     * hundreds of thousands of mosquito beasts.
     *
     * <p>Bounding box: 20 blocks wide, 10 blocks tall — large enough that the
     * swarm visually envelopes anything inside it.
     *
     * <p>Client tracking range: 16 chunks (256 blocks) — swarms are massive
     * and should be visible from very far away.
     *
     * <p>Update interval: 1 tick — the swarm needs frequent updates for
     * population-based damage ticks and pathfinding.
     */
    public static final RegistryObject<EntityType<MosquitoSwarmEntity>> MOSQUITO_SWARM =
            ENTITY_TYPES.register("mosquito_swarm", () ->
                    EntityType.Builder.<MosquitoSwarmEntity>of(MosquitoSwarmEntity::new, MobCategory.MISC)
                            .sized(20.0F, 10.0F)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .build("mosquito_swarm")
            );

    /**
     * Cultivator — a polymorphic NPC shell driven by canon data.
     *
     * <p>One entity class for all 151+ canon characters. At spawn, it reads a
     * {@code character_id} (synced via SynchedEntityData) and configures itself
     * from the canon DB + WorldRuntimeState overrides.
     *
     * <p>Bounding box: 0.7×2.0 (slightly larger than vanilla Player to match the
     * robe model visual — FIX RE-APPLY-PHASE1, was 0.6×1.8).
     *
     * <p>Client tracking range: 5 chunks (80 blocks) — standard for humanoids.
     *
     * <p>Update interval: 3 ticks — cultivators don't need per-tick updates
     * (hibernation handles distant entities). Saves bandwidth.
     *
     * <p>MobCategory.MISC so they don't count against the creature cap
     * (which would prevent normal animal spawns). {@code removeWhenFarAway}
     * is overridden to return false — canon entities never despawn.
     */
    public static final RegistryObject<EntityType<EntityCultivator>> CULTIVATOR =
            ENTITY_TYPES.register("cultivator", () ->
                    EntityType.Builder.<EntityCultivator>of(EntityCultivator::new, MobCategory.MISC)
                            .sized(0.7F, 2.0F)
                            .clientTrackingRange(5)
                            .updateInterval(3)
                            .fireImmune()
                            .build("cultivator")
            );

    // ── Spirit Beast entities ──────────────────────────────────────────
    // CRON-COMPLETIONIST-84: SINGLE SOURCE OF TRUTH for hitboxes.
    // Each .sized(w, h) call reads from SpiritBeastEntity.BeastType.XXX.width/height.
    // SpiritBeastEntity.getDimensions() reads from the SAME enum fields.
    // There is ONE place to change a beast's hitbox: the BeastType enum constant.
    // All use the unified SpiritBeastEntity shell. The custom models
    // (SpiritWolfModel, SpiritHawkModel, etc.) define the visual shape;
    // the bounding box here is sized to the model's actual footprint.
    // MobCategory.CREATURE so they spawn naturally.

    // SpiritRabbitModel: body 4x4x5, feet at y=15, model origin y=11.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_RABBIT =
            ENTITY_TYPES.register("spirit_rabbit", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(SpiritBeastEntity.BeastType.RABBIT.width,
                                   SpiritBeastEntity.BeastType.RABBIT.height)
                            .clientTrackingRange(5)
                            .updateInterval(3)
                            .build("spirit_rabbit"));

    // SpiritWolfModel: body 4x6x10, legs to y=15.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_WOLF =
            ENTITY_TYPES.register("spirit_wolf", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(SpiritBeastEntity.BeastType.WOLF.width,
                                   SpiritBeastEntity.BeastType.WOLF.height)
                            .clientTrackingRange(5)
                            .updateInterval(3)
                            .build("spirit_wolf"));

    // SpiritDeerModel: body 3x5x8, long neck + antlers. Height capped at 1.8
    // (doorway clearance) even though model stands 2.2.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_DEER =
            ENTITY_TYPES.register("spirit_deer", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(SpiritBeastEntity.BeastType.DEER.width,
                                   SpiritBeastEntity.BeastType.DEER.height)
                            .clientTrackingRange(5)
                            .updateInterval(3)
                            .build("spirit_deer"));

    // SpiritFireBeastModel: body 5x6x10, flame mane extends higher. Biggest predator.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> FIRE_BEAST =
            ENTITY_TYPES.register("fire_beast", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(SpiritBeastEntity.BeastType.FIRE_BEAST.width,
                                   SpiritBeastEntity.BeastType.FIRE_BEAST.height)
                            .clientTrackingRange(12)
                            .updateInterval(2)
                            .fireImmune()
                            .build("fire_beast"));

    // StoneBackBoarModel: body 5x5x10, stone plate adds width.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> STONE_BACK_BOAR =
            ENTITY_TYPES.register("stone_back_boar", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(SpiritBeastEntity.BeastType.STONE_BACK_BOAR.width,
                                   SpiritBeastEntity.BeastType.STONE_BACK_BOAR.height)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build("stone_back_boar"));

    // SpiritHawkModel: body 6x4x6, wingspan 14+ each side (visual only).
    // Body-only collision (vanilla parrot convention — wings are visual).
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_HAWK =
            ENTITY_TYPES.register("spirit_hawk", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(SpiritBeastEntity.BeastType.HAWK.width,
                                   SpiritBeastEntity.BeastType.HAWK.height)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("spirit_hawk"));

    // CRON-COMPLETIONIST-22/24: SpiritCraneModel — red-crowned crane, long neck, narrow body.
    // Height capped at 1.8 (doorway clearance; model stands 2.0).
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_CRANE =
            ENTITY_TYPES.register("spirit_crane", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(SpiritBeastEntity.BeastType.CRANE.width,
                                   SpiritBeastEntity.BeastType.CRANE.height)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("spirit_crane"));

    // CRON-COMPLETIONIST-33: SpiritBatModel — small aerial insectivore.
    // Compact body, wings visual only.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_BAT =
            ENTITY_TYPES.register("spirit_bat", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(SpiritBeastEntity.BeastType.BAT.width,
                                   SpiritBeastEntity.BeastType.BAT.height)
                            .clientTrackingRange(5)
                            .updateInterval(2)
                            .build("spirit_bat"));

    // CRON-COMPLETIONIST-33: QilinModel — winged wolf-quadruped with antlers + mane.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> QILIN =
            ENTITY_TYPES.register("qilin", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(SpiritBeastEntity.BeastType.QILIN.width,
                                   SpiritBeastEntity.BeastType.QILIN.height)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .fireImmune()
                            .build("qilin"));

    // CRON-COMPLETIONIST-33: SeaSerpentModel — undulating aquatic predator.
    // Elongated body, visual segments extend. Flatter than tall when swimming.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SEA_SERPENT =
            ENTITY_TYPES.register("sea_serpent", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(SpiritBeastEntity.BeastType.SEA_SERPENT.width,
                                   SpiritBeastEntity.BeastType.SEA_SERPENT.height)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("sea_serpent"));

    // CRON-COMPLETIONIST-60: SoulFishModel v3 — doubled model dimensions.
    // CRON-80: reconciled EntityType.sized with runtime getDimensions.
    // CRON-84: both now read from BeastType.SOUL_FISH — single source of truth.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SOUL_FISH =
            ENTITY_TYPES.register("soul_fish", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(SpiritBeastEntity.BeastType.SOUL_FISH.width,
                                   SpiritBeastEntity.BeastType.SOUL_FISH.height)
                            .clientTrackingRange(5)
                            .updateInterval(3)
                            .build("soul_fish"));

    // CRON-COMPLETIONIST-67: SpiritTigerModel — barrel-chested apex land predator.
    // Model existed since CRON-75 but entity type was never registered. NOW FIXED.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_TIGER =
            ENTITY_TYPES.register("spirit_tiger", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(SpiritBeastEntity.BeastType.TIGER.width,
                                   SpiritBeastEntity.BeastType.TIGER.height)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("spirit_tiger"));
}