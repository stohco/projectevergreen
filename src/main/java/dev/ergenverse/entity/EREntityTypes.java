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

    // ── Spirit Beast entities (v1: rabbit, wolf, deer, hawk) ───────────
    // All four use the unified SpiritBeastEntity shell; the renderer picks
    // the texture from the synced BeastType. They are MobCategory.CREATURE
    // so they spawn naturally in the world.

    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_RABBIT =
            ENTITY_TYPES.register("spirit_rabbit", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.6F)
                            .clientTrackingRange(5)
                            .updateInterval(3)
                            .build("spirit_rabbit"));

    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_WOLF =
            ENTITY_TYPES.register("spirit_wolf", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 0.9F)
                            .clientTrackingRange(5)
                            .updateInterval(3)
                            .build("spirit_wolf"));

    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_DEER =
            ENTITY_TYPES.register("spirit_deer", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.4F)
                            .clientTrackingRange(5)
                            .updateInterval(3)
                            .build("spirit_deer"));

    public static final RegistryObject<EntityType<SpiritBeastEntity>> FIRE_BEAST =
            ENTITY_TYPES.register("fire_beast", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.0F)
                            .clientTrackingRange(12)
                            .build("fire_beast"));

    public static final RegistryObject<EntityType<SpiritBeastEntity>> STONE_BACK_BOAR =
            ENTITY_TYPES.register("stone_back_boar", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 0.9F)
                            .clientTrackingRange(10)
                            .build("stone_back_boar"));

    public static final RegistryObject<EntityType<SpiritBeastEntity>> SPIRIT_HAWK =
            ENTITY_TYPES.register("spirit_hawk", () ->
                    EntityType.Builder.<SpiritBeastEntity>of(SpiritBeastEntity::new, MobCategory.CREATURE)
                            .sized(0.8F, 0.8F)
                            .clientTrackingRange(5)
                            .updateInterval(3)
                            .build("spirit_hawk"));
}