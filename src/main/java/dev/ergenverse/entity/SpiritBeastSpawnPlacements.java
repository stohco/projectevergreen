package dev.ergenverse.entity;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * SpiritBeastSpawnPlacements — registers SpawnPlacements for all 11 spirit
 * beast types so they can spawn naturally via biome spawners.
 *
 * <p>CRON-86: Previously, the 11 spirit beast entity types (RABBIT through
 * SOUL_FISH) had full AI, models, renderers, attributes, AND biome_modifier
 * JSONs, but NO SpawnPlacements were registered. Without SpawnPlacements,
 * Minecraft's spawn pipeline throws an {@link IllegalStateException}
 * ("SpawnPlacements not registered for ...") when a biome attempts to
 * spawn the entity. The spawn is silently aborted and the entity never
 * appears in the world. The 6 existing biome_modifier JSONs (rabbit, wolf,
 * hawk, deer, fire_beast, stone_back_boar) were effectively no-ops.
 *
 * <p>This class closes that gap. Each beast type gets:
 * <ul>
 *   <li>A {@link SpawnPlacements.Type} — ON_GROUND for terrestrial and
 *       aerial beasts (aerials spawn on ground then take off via AI),
 *       IN_WATER for aquatic beasts.</li>
 *   <li>A {@link Heightmap.Types} — MOTION_BLOCKING for ground spawns,
 *       OCEAN_FLOOR for water spawns.</li>
 *   <li>A {@link SpawnPlacements.SpawnPredicate} — delegates to
 *       {@link Mob#checkMobSpawnRules} for the basic "is there solid
 *       ground below and breathable air" check (works for any light
 *       level since spirit beasts are CREATURE category, not MONSTER).
 *       Aquatic types additionally require water fluid at the spawn
 *       position via a custom predicate.</li>
 * </ul>
 *
 * <p>Registered on the MOD event bus via {@code @Mod.EventBusSubscriber}.
 * Fires once during mod lifecycle, before world load.
 *
 * <p>Canon note: each beast's biome assignment is in its respective
 * biome_modifier JSON under {@code data/ergenverse/forge/biome_modifier/}.
 * SpawnPlacements are biome-agnostic — they only define WHERE on the column
 * an entity can spawn (ground/water/air), not WHICH biomes.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SpiritBeastSpawnPlacements {

    private SpiritBeastSpawnPlacements() {}

    @SubscribeEvent
    public static void onSpawnPlacementRegister(SpawnPlacementRegisterEvent event) {
        // ── Terrestrial herbivores ──────────────────────────────────────────
        // Rabbit + deer spawn on solid ground, any light. They are the bottom
        // of the spirit-beast food chain (prey for wolves, hawks, tigers).
        event.register(EREntityTypes.SPIRIT_RABBIT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING,
                SpiritBeastSpawnPlacements::checkGroundSpawn,
                Operation.REPLACE);

        event.register(EREntityTypes.SPIRIT_DEER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING,
                SpiritBeastSpawnPlacements::checkGroundSpawn,
                Operation.REPLACE);

        // ── Terrestrial predators ───────────────────────────────────────────
        // Wolf, stone-back boar, fire beast. All hunt on the ground. Fire
        // beast is fire-immune so it can spawn on hot blocks (netherrack,
        // magma) but the placement rule is still ON_GROUND.
        event.register(EREntityTypes.SPIRIT_WOLF.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING,
                SpiritBeastSpawnPlacements::checkGroundSpawn,
                Operation.REPLACE);

        event.register(EREntityTypes.STONE_BACK_BOAR.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING,
                SpiritBeastSpawnPlacements::checkGroundSpawn,
                Operation.REPLACE);

        event.register(EREntityTypes.FIRE_BEAST.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING,
                SpiritBeastSpawnPlacements::checkGroundSpawn,
                Operation.REPLACE);

        // ── Wetland bird ────────────────────────────────────────────────────
        // Crane spawns on solid ground near water (its AI handles wading).
        event.register(EREntityTypes.SPIRIT_CRANE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING,
                SpiritBeastSpawnPlacements::checkGroundSpawn,
                Operation.REPLACE);

        // ── Divine beast ────────────────────────────────────────────────────
        // Qilin — rare, spawns on ground in the Sea of Devils region per
        // canon (Wang Lin encounters the 麒灵 beast-city in the 修魔海).
        // Rarity is enforced by biome_modifier weight=1, not by placement.
        event.register(EREntityTypes.QILIN.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING,
                SpiritBeastSpawnPlacements::checkGroundSpawn,
                Operation.REPLACE);

        // ── Aerial beasts ───────────────────────────────────────────────────
        // Hawk + bat use ON_GROUND placement (same as vanilla parrots/bats)
        // because they roost on the ground before taking flight. Their AI
        // goals handle aerial movement after spawn.
        event.register(EREntityTypes.SPIRIT_HAWK.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING,
                SpiritBeastSpawnPlacements::checkGroundSpawn,
                Operation.REPLACE);

        event.register(EREntityTypes.SPIRIT_BAT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING,
                SpiritBeastSpawnPlacements::checkGroundSpawn,
                Operation.REPLACE);

        // ── Aquatic beasts ──────────────────────────────────────────────────
        // Sea serpent + soul fish spawn IN_WATER on the ocean floor. They
        // are restricted to the Sea of Devils (修魔海) via biome_modifier.
        // Custom predicate requires water fluid at the spawn position.
        event.register(EREntityTypes.SEA_SERPENT.get(),
                SpawnPlacements.Type.IN_WATER,
                Heightmap.Types.OCEAN_FLOOR,
                SpiritBeastSpawnPlacements::checkWaterSpawn,
                Operation.REPLACE);

        event.register(EREntityTypes.SOUL_FISH.get(),
                SpawnPlacements.Type.IN_WATER,
                Heightmap.Types.OCEAN_FLOOR,
                SpiritBeastSpawnPlacements::checkWaterSpawn,
                Operation.REPLACE);

        Ergenverse.LOGGER.info("[Ergenverse] Registered SpawnPlacements for 11 Spirit Beast types.");
    }

    /**
     * Ground spawn predicate — delegates to {@link Mob#checkMobSpawnRules}
     * which verifies that the block below is solid and the spawn position
     * is breathable. Works for any light level (CREATURE category doesn't
     * require darkness, unlike MONSTER).
     *
     * <p>Used by: rabbit, deer, wolf, stone_back_boar, fire_beast, crane,
     * qilin, hawk, bat (9 types).
     */
    private static boolean checkGroundSpawn(EntityType<SpiritBeastEntity> type,
                                            ServerLevelAccessor level,
                                            MobSpawnType spawnType,
                                            BlockPos pos,
                                            RandomSource random) {
        return Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    /**
     * Water spawn predicate — requires water fluid at the spawn position
     * AND the standard mob spawn rules. Used by sea_serpent and soul_fish
     * (2 types). Without the fluid check, aquatic beasts could spawn on
     * dry land if the heightmap happened to place them there.
     */
    private static boolean checkWaterSpawn(EntityType<SpiritBeastEntity> type,
                                           ServerLevelAccessor level,
                                           MobSpawnType spawnType,
                                           BlockPos pos,
                                           RandomSource random) {
        return level.getFluidState(pos).is(FluidTags.WATER)
                && Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }
}
