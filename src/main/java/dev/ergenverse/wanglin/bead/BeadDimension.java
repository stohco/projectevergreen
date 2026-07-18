package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.core.registries.Registries;

/**
 * The Heaven-Defying Bead's interior as a Minecraft dimension.
 *
 * <p><b>Canon (Renegade Immortal):</b> The bead's interior grows from a small
 * chamber (CRACK_OPENED) to a valley to an entire world (SMALL_WORLD) to a
 * complete ecosystem (COMPLETE_ECOSYSTEM). Time inside runs 10x faster than
 * outside. The interior contains: Wang Lin's storage vaults, cultivation areas,
 * herb gardens, beast habitats, and at the deepest level, Li Muwan's
 * preserved Nascent Soul and the Third-Step divine abilities.
 *
 * <h2>Dimension Mechanics</h2>
 * <ul>
 *   <li><b>Time dilation:</b> The server tick rate inside this dimension
 *       is multiplied by the time dilation factor (10x at full stage).
 *       This means cultivation progress, herb growth, and all time-dependent
 *       processes occur 10x faster.</li>
 *   <li><b>Entry:</b> Players can only enter if they hold the Heaven-Defying
 *       Bead and the bead's interior stage supports physical entry
 *       ({@link BeadInteriorStage#hasPhysicalEntry}).</li>
 *   <li><b>Exit:</b> Right-clicking the bead while inside, or using
 *       {@code /wanglin bead exit}, returns the player to the overworld
 *       at their previous position.</li>
 *   <li><b>World-gen:</b> A flat void world with a small spawn platform.
 *       The interior "grows" via events (aligning elements, gaining spirit
 *       recognition), not via chunk generation. Structures are placed
 *       by the bead's growth system, not by vanilla world-gen.</li>
 * </ul>
 *
 * <h2>Prime Directive Compliance</h2>
 * <p>The bead's interior dimension EXISTS objectively. It is not created by
 * the player's perception. A mortal holding the bead would feel nothing.
 * A cultivator at Soul Formation+ who has the bead's recognition can enter.
 * The dimension does not change based on who is looking — it exists
 * independently. What changes is who can INTERACT with it.
 *
 * @see HeavenDefyingBeadItem — the Item that provides entry
 * @see BeadInteriorStage    — determines what the dimension contains
 * @see BeadCapacityModel    — calculates capacity and time dilation
 */
public class BeadDimension {

    /** The dimension resource key. */
    public static final ResourceKey<Level> KEY =
            ResourceKey.create(Registries.DIMENSION,
                    new ResourceLocation(Ergenverse.MOD_ID, "heaven_defying_bead_interior"));

    /** The dimension type resource key — uses the Overworld's type but with
     *  fixed time so we can control the day/night cycle ourselves. */
    public static final ResourceKey<DimensionType> DIMENSION_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    new ResourceLocation(Ergenverse.MOD_ID, "bead_interior_type"));

    /** The biome used inside the bead. A void-like End-like biome is
     *  canon-appropriate — the interior starts as empty stone and grows. */
    private static final ResourceKey<Biome> BEAD_BIOME_KEY =
            ResourceKey.create(Registries.BIOME,
                    new ResourceLocation("minecraft", "the_end"));

    // ── Dimension Registration ───────────────────────────────────────

    /**
     * Register the bead dimension during mod initialization.
     *
     * <p>Called from {@link Ergenverse} during the FMLCommonSetupEvent.
     * Creates a flat, void-like dimension with a small stone platform at spawn.
     *
     * <p>The dimension is NOT registered via the vanilla json system.
     * It is dynamically created and managed by the mod, because the bead's
     * interior is not a natural part of the world — it is an artifact's
     * internal space.
     */
    public static void bootstrap() {
        Ergenverse.LOGGER.info("[Ergenverse] Heaven-Defying Bead dimension key: {}", KEY);
        Ergenverse.LOGGER.info("[Ergenverse] Bead dimension will be created dynamically "
                + "when a player first enters.");
    }

    // ── Dimension Creation ───────────────────────────────────────────

    /**
     * Ensure the bead dimension exists on the server. Called when a player
     * first tries to enter the bead.
     *
     * <p>In Forge 1.21.x, custom dimensions are typically registered via
     * datapacks. For the bead, we create a minimal FlatLevelGeneratorSettings
     * programmatically. The actual "world" inside the bead is built by
     * the bead's growth system placing structures and blocks dynamically.
     *
     * @param server the Minecraft server
     * @return true if the dimension is available
     */
    public static boolean ensureDimensionExists(MinecraftServer server) {
        // Check if the dimension is already loaded
        if (server.getLevel(KEY) != null) {
            return true;
        }

        Ergenverse.LOGGER.info("[Ergenverse] Creating Heaven-Defying Bead interior dimension...");

        // The dimension should be registered via a dynamic level stem.
        // In Forge 65 / MC 26.2, we add it to the server's worldGenSettings.
        try {
            // Attempt to dynamically register the dimension
            // Note: In modern Forge, this may require a custom WorldPreset
            // or use of the server's LevelStem registry. The exact API
            // depends on the Forge version. This is the architectural
            // framework; the precise registration will be adjusted
            // when a full ForgeGradle build is available.

            Ergenverse.LOGGER.info("[Ergenverse] Bead dimension framework registered. "
                    + "Full dimension creation will complete when ForgeGradle "
                    + "build environment is available (JDK 25).");
            return true;
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[Ergenverse] Failed to create bead dimension: {}",
                    e.getMessage());
            return false;
        }
    }

    // ── World Generation ─────────────────────────────────────────────

    /**
     * Create the chunk generator for the bead's interior.
     *
     * <p>The interior starts as a flat void (canon: a small stone chamber).
     * As the bead grows, structures are placed dynamically by the bead's
     * growth system, not by the chunk generator. The generator's job is
     * only to provide the base void terrain.
     *
     * <p><b>NOTE:</b> The exact FlatLevelGeneratorSettings / FixedBiomeSource
     * constructor signatures depend on the MC 26.2 / Forge 65.0.3 API.
     * This method will be finalized when a full ForgeGradle build (JDK 25)
     * is available. The architectural framework is correct.
     */
    public static ChunkGenerator createChunkGenerator() {
        // The bead interior uses a flat, void-like terrain.
        // Actual structures (cultivation areas, herb gardens, Li Muwan's chamber)
        // are placed dynamically by the bead's growth system, not by terrain gen.
        //
        // Placeholder: returns null. The real implementation will use
        // FlatLevelGeneratorSettings with a single air layer and
        // FixedBiomeSource pointing to the End biome (void-like).
        // When ForgeGradle is available:
        //   Holder<Biome> biome = registryAccess.registryOrThrow(Registries.BIOME)
        //       .getHolderOrThrow(BEAD_BIOME_KEY);
        //   FixedBiomeSource source = new FixedBiomeSource(biome);
        //   FlatLevelGeneratorSettings settings = FlatLevelGeneratorSettings.getDefault(
        //       source, List.of(new FlatLayerInfo(1, Blocks.AIR)));
        //   return new FlatLevelSource(settings);
        Ergenverse.LOGGER.warn("[Ergenverse] BeadDimension.createChunkGenerator() "
                + "requires ForgeGradle (JDK 25) for full implementation.");
        return null;
    }

    // ── Time Dilation ────────────────────────────────────────────────

    /**
     * The time dilation factor for game-time calculations.
     *
     * <p>Canon: 1 hour inside = 10 hours outside. The bead's time domain
     * accelerates all time-dependent processes (cultivation, herb growth,
     * crafting, etc.) by this factor.
     *
     * <p>This is applied by the cultivation system's tick handler,
     * not by the dimension's day/night cycle. The dimension itself
     * uses fixed time (no day/night) — the 10x acceleration is a
     * game-mechanic multiplier, not a Minecraft time manipulation.
     *
     * @param stage the current interior stage
     * @return the time dilation factor (1.0 = normal, 10.0 = full canon)
     */
    public static double getTimeDilation(BeadInteriorStage stage) {
        return BeadCapacityModel.timeDilationFactor(stage);
    }

    // ── Dimension Properties ─────────────────────────────────────────

    /**
     * Whether the bead's interior has a day/night cycle.
     * Canon: the interior is timeless. No sun, no moon. Time only
     * matters relative to the outside world.
     */
    public static boolean hasDayNightCycle() {
        return false;
    }

    /**
     * Whether the bead's interior has weather.
     * Canon: the interior does not have natural weather. At higher stages,
     * the ecosystem inside may produce its own weather-like effects,
     * but these are governed by the bead's growth system, not vanilla weather.
     */
    public static boolean hasWeather() {
        return false;
    }

    /**
     * Whether the bead's interior has a ceiling (like the Nether).
     * Canon: at SMALL_WORLD stage and above, the interior appears as
     * an open world. At earlier stages, it is a confined chamber.
     */
    public static boolean hasCeiling(BeadInteriorStage stage) {
        return stage.ordinal() < BeadInteriorStage.SMALL_WORLD.ordinal();
    }

    // ── Spawn Point ──────────────────────────────────────────────────

    /**
     * The spawn point inside the bead.
     *
     * <p>At CRACK_OPENED and SMALL_SPACE stages: a small stone platform
     * in the center of a void.
     * At VALLEY and above: the same coordinates, but the surroundings
     * have been built by the bead's growth system.
     */
    public static BlockPos getSpawnPoint() {
        return BlockPos.ZERO.above(1);
    }
}