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
     * <p>Bounding box: 0.6×1.8 (humanoid proportions, same as vanilla Player).
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
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(5)
                            .updateInterval(3)
                            .fireImmune()
                            .build("cultivator")
            );

    // ── Spirit Beast entities — per-type bounding boxes matching custom model anatomy ──
    // All use the unified SpiritBeastEntity shell. The custom models
    // (SpiritWolfModel, SpiritHawkModel, etc.) define the visual shape;
    // the bounding box here is sized to the model's actual footprint.
    // MobCategory.CREATURE so they spawn naturally.

    // SpiritRabbitModel: body 4x4x5, feet at y=15, model origin y=11. Hitbox: ~0.5 wide, ~0.5 tall.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_RABBIT =
            ENTITY_TYPES.register("spirit_rabbit", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(0.4F, 0.4F)
                            .clientTrackingRange(5)
                            .updateInterval(3)
                            .build("spirit_rabbit"));

    // SpiritWolfModel: body 4x6x10, legs to y=15. Hitbox: ~1.0 wide, ~0.9 tall.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_WOLF =
            ENTITY_TYPES.register("spirit_wolf", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.7F)
                            .clientTrackingRange(5)
                            .updateInterval(3)
                            .build("spirit_wolf"));

    // SpiritDeerModel: body 3x5x8, long neck, legs to y=15. Hitbox: ~0.8 wide, ~1.6 tall (with antlers).
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_DEER =
            ENTITY_TYPES.register("spirit_deer", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(0.7F, 1.8F)
                            .clientTrackingRange(5)
                            .updateInterval(3)
                            .build("spirit_deer"));

    // SpiritFireBeastModel: body 5x6x10, flame mane extends higher. Hitbox: ~1.2 wide, ~1.2 tall.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> FIRE_BEAST =
            ENTITY_TYPES.register("fire_beast", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(1.2F, 1.2F)
                            .clientTrackingRange(12)
                            .updateInterval(2)
                            .fireImmune()
                            .build("fire_beast"));

    // StoneBackBoarModel: body 5x5x10, stone plate adds width. Hitbox: ~1.2 wide, ~1.0 tall.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> STONE_BACK_BOAR =
            ENTITY_TYPES.register("stone_back_boar", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(1.2F, 1.0F)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build("stone_back_boar"));

    // SpiritHawkModel: body 6x4x6, wingspan 14+ each side. Hitbox: ~1.0 wide (body only, wings are visual), ~0.8 tall.
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_HAWK =
            ENTITY_TYPES.register("spirit_hawk", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(2.0F, 0.8F)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("spirit_hawk"));

    // CRON-COMPLETIONIST-22/24: SpiritCraneModel — red-crowned crane, tall on long legs.
    // Hitbox: ~0.6 wide, ~1.6 tall (standing height with long neck).
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_CRANE =
            ENTITY_TYPES.register("spirit_crane", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.6F)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("spirit_crane"));

    // CRON-COMPLETIONIST-33: SpiritBatModel — small aerial insectivore.
    // Hitbox: ~0.4 wide, ~0.4 tall (compact body, wings visual only).
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_BAT =
            ENTITY_TYPES.register("spirit_bat", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(0.4F, 0.4F)
                            .clientTrackingRange(5)
                            .updateInterval(2)
                            .build("spirit_bat"));

    // CRON-COMPLETIONIST-33: QilinModel — winged wolf-quadruped with antlers.
    // Hitbox: ~1.0 wide, ~1.2 tall (similar to wolf but slightly larger).
    public static final RegistryObject<EntityType<SpiritBeastEntity>> QILIN =
            ENTITY_TYPES.register("qilin", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(1.0F, 1.2F)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .fireImmune()
                            .build("qilin"));

    // CRON-COMPLETIONIST-33: SeaSerpentModel — undulating aquatic predator.
    // Hitbox: ~0.8 wide, ~0.8 tall (cylindrical body, visual segments extend).
    public static final RegistryObject<EntityType<SpiritBeastEntity>> SEA_SERPENT =
            ENTITY_TYPES.register("sea_serpent", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(0.8F, 0.8F)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("sea_serpent"));
}