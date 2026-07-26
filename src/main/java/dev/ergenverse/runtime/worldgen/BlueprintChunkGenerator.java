package dev.ergenverse.runtime.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.spawn.DeterministicSeedHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * BlueprintChunkGenerator — a true algorithmically-independent chunk generator
 * for Planet Suzaku whose surface terrain is derived from the
 * {@link PlanetSuzakuBlueprint}'s canon geography (plus a tiny deterministic
 * value-noise perturbation seeded by {@link DeterministicSeedHandler#CANON_SEED}),
 * <em>not</em> from {@code minecraft:overworld} noise.
 *
 * <h2>CRON-COMPLETIONIST-60 — TRUE ALGORITHMIC INDEPENDENCE (point 10)</h2>
 *
 * <p>The CRON-71 design (this class's prior form) only wrapped
 * {@link NoiseBasedChunkGenerator} and overrode {@link #getBaseHeight} to add
 * canon terrain offsets on top of vanilla noise. The surface shape was still
 * determined by {@code minecraft:overworld} noise — only nudged a few blocks
 * per canon location. That was a "noise with offset" generator, not a "blueprint"
 * generator. It failed the architectural goal of point 10: <i>"Terrain columns
 * come from the blueprint+layers, not minecraft:overworld noise."</i>
 *
 * <p>This version (CRON-COMPLETIONIST-60) closes that gap:
 * <ul>
 *   <li>{@link #fillFromNoise} is overridden to fill each column from
 *       {@link #canonSurfaceHeight(int, int)}, a pure deterministic function
 *       of the blueprint's canon locations + {@link DeterministicSeedHandler#CANON_SEED}.</li>
 *   <li>{@link #getBaseHeight} returns {@link #canonSurfaceHeight(int, int)}.</li>
 *   <li>{@link #getBaseColumn} returns a column built from
 *       {@link #canonSurfaceHeight(int, int)} — bedrock at the bottom, stone
 *       up to the canon surface, water up to sea level, air above.</li>
 * </ul>
 *
 * <p>The wrapped {@link NoiseBasedChunkGenerator} is still used for
 * {@link #applyCarvers} (caves/ravines) and {@link #buildSurface} (grass/sand/
 * snow/etc. biome surface rules). Those methods use vanilla's noise-derived
 * carving and surface data, which is independent of the surface heightmap —
 * they carve <i>through</i> whatever stone we placed, and apply surface rules
 * on top of whatever stone surface we placed. This preserves caves, ores
 * (ores are placed by feature decoration which queries existing stone), and
 * biome surface variation for free.
 *
 * <p><b>Canon-driven surface formula:</b>
 * <pre>{@code
 *   canonSurfaceHeight(x, z) =
 *       BASE_HEIGHT (64, one above sea level)
 *     + sum_over_nearby_canon_locations(getTerrainWarpForLocation(loc)
 *                                        * linearDecayFactor(distance, RADIUS=200))
 *     + canonNoiseVariation(x, z)   // bilinear value noise, 8-block period,
 *                                    // amplitude ±8, seeded by CANON_SEED
 * }</pre>
 *
 * <p>The result: Heng Yue Mountain is always raised +30 near (4200, -1400),
 * the Sea of Devils is always lowered −30 near (6000, -1184), Wang Family
 * Village sits at +8 above sea level on a hill, etc. — every playthrough,
 * regardless of vanilla noise seed. The hand-placed spawn at (3842, -1184)
 * always lands on the same hill. This is <i>true algorithmic independence</i>
 * from {@code minecraft:overworld} noise.
 *
 * <p><b>Codec &amp; registration:</b> Registered as {@code ergenverse:blueprint}
 * via {@link ErgenverseChunkGenerators#BLUEPRINT}. The dimension JSON at
 * {@code data/ergenverse/dimension/planet_suzaku.json} references the generator
 * type as {@code ergenverse:blueprint} with {@code settings: minecraft:overworld}
 * (the overworld settings are used for cave/surface-rule configuration, NOT for
 * surface terrain shape — that comes from this generator).
 *
 * <p><b>Canon fidelity (fact-checked against 仙逆):</b>
 * <ul>
 *   <li>恒岳山 (Heng Yue Mountain): Zhao Country's largest mountain, raised +30.</li>
 *   <li>修魔海 (Sea of Devils): vast perilous sea east of Zhao, lowered −30 → ocean.</li>
 *   <li>朱雀墓 (Suzaku Tomb): underground inheritance site, surface depressed −12.</li>
 *   <li>Wang Family Village: "赵国某偏僻小山村" — remote mountain village, raised +8.</li>
 *   <li>藤家城 (Teng Family City): powerful family city in Zhao, raised +10.</li>
 *   <li>雪域国 (Snow Domain): cold elevated country, raised +10.</li>
 *   <li>恒岳派 / 炼魂宗 / 玄道宗 / 洛河门: mountain sects, raised +12 to +15.</li>
 * </ul>
 *
 * <p><b>Known trade-offs (hyper-analytical self-critique):</b>
 * <ul>
 *   <li>The base noise variation is bilinear value noise (8-block period,
 *       ±8 amplitude). This is intentionally crude — its purpose is to break
 *       the perfectly-flat plateau that pure canon warping would produce
 *       between canon locations, NOT to mimic natural terrain. Real terrain
 *       variety will come from biome surface rules (grass/sand/snow) applied
 *       by {@link #buildSurface}, not from terrain noise.</li>
 *   <li>Caves are carved by vanilla carvers, which use vanilla noise to
 *       determine WHERE to carve. Because our surface is canon-shaped (not
 *       vanilla-noise-shaped), caves may occasionally carve into air (above
 *       our surface where vanilla expected stone) or fail to carve (below our
 *       surface where vanilla expected air). The net effect: caves still
 *       exist but their density/surface-entrance distribution may differ
 *       from a vanilla world. This is acceptable — the canon world has caves,
 *       they just don't align with vanilla noise.</li>
 *   <li>Ores are placed by feature decoration, which queries existing stone
 *       blocks. Since we fill stone up to the canon surface, ores will be
 *       placed normally. No loss here.</li>
 *   <li>Aquifers (underground water) are part of vanilla noise generation.
 *       Skipping vanilla {@code fillFromNoise} means we lose aquifers. This
 *       is a known limitation; aquifers are mostly invisible to the player
 *       (underground water pools) and the Sea of Devils already provides
 *       surface water via our canon fill.</li>
 *   <li>The wrapped generator's {@link NoiseBasedChunkGenerator#fillFromNoise}
 *       is NOT called, so {@code chunk.getOrCreateNoiseChunk(...)} will be
 *       lazily computed on first access (by {@code applyCarvers} or
 *       {@code buildSurface}). This is fine — vanilla supports lazy
 *       NoiseChunk computation.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class BlueprintChunkGenerator extends ChunkGenerator {

    /**
     * Codec for deserialization from the dimension JSON. Takes the same
     * fields as {@code minecraft:noise} (biome_source + noise_settings)
     * plus our registry key.
     */
    public static final Codec<BlueprintChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source")
                            .forGetter(gen -> gen.biomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings")
                            .forGetter(gen -> gen.noiseSettings)
            ).apply(instance, BlueprintChunkGenerator::new));

    /** The noise generator settings holder (e.g. "minecraft:overworld"). */
    private final Holder<NoiseGeneratorSettings> noiseSettings;

    /**
     * The wrapped NoiseBasedChunkGenerator that does the heavy lifting
     * for caves (applyCarvers) and biome surface rules (buildSurface).
     * <p><b>NOT used for {@code fillFromNoise}</b> — we override that
     * fully to control surface terrain shape from canon geography.
     */
    private final NoiseBasedChunkGenerator wrapped;

    // ════════════════════════════════════════════════════════════════════
    //  CANON SURFACE CONSTANTS
    // ════════════════════════════════════════════════════════════════════

    /**
     * Canon terrain warp radius — how far (in blocks) from a canon location
     * the terrain is warped. Within this radius, the warp decays linearly.
     */
    private static final int TERRAIN_WARP_RADIUS = 200;
    private static final int TERRAIN_WARP_RADIUS_SQ = TERRAIN_WARP_RADIUS * TERRAIN_WARP_RADIUS;

    /**
     * Maximum terrain warp magnitude in blocks. Positive = raise terrain
     * (mountains). Negative = lower terrain (sea/tomb).
     */
    private static final int MAX_WARP_HEIGHT = 30;

    /**
     * Base surface height (one block above sea level 63). All canon warps
     * and noise variation are added to this. Plains away from canon
     * locations will sit at y=64 — dry land just above water.
     */
    private static final int BASE_SURFACE_HEIGHT = 64;

    /**
     * Sea level. Columns whose canon surface is below this fill with water
     * up to sea level (oceans, seas, lakes). Vanilla overworld also uses 63.
     */
    private static final int SEA_LEVEL = 63;

    /**
     * Period of the bilinear value-noise variation, in blocks. 8 means
     * smooth rolling hills on the scale of half-a-chunk — enough to break
     * flatness between canon locations without producing noise-dominated
     * terrain that would mask canon geography.
     */
    private static final int NOISE_PERIOD = 8;

    /**
     * Amplitude of the canon value-noise variation. ±8 blocks gives gentle
     * rolling hills; large enough to feel natural, small enough that canon
     * warps (±30) dominate geography.
     */
    private static final int NOISE_AMPLITUDE = 8;

    // ════════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ════════════════════════════════════════════════════════════════════

    private BlueprintChunkGenerator(BiomeSource biomeSource,
                                     Holder<NoiseGeneratorSettings> noiseSettings) {
        super(biomeSource);
        this.noiseSettings = noiseSettings;
        this.wrapped = new NoiseBasedChunkGenerator(biomeSource, noiseSettings);
    }

    // ════════════════════════════════════════════════════════════════════
    //  CHUNK GENERATION — TRUE ALGORITHMIC INDEPENDENCE
    // ════════════════════════════════════════════════════════════════════

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    /**
     * Fill the chunk with canon-derived terrain.
     *
     * <p>This is the heart of the algorithmic independence: surface height
     * per column comes from {@link #canonSurfaceHeight(int, int)}, NOT from
     * vanilla noise. The wrapped {@link NoiseBasedChunkGenerator#fillFromNoise}
     * is NOT called — we generate terrain entirely from canon geography.
     *
     * <p>Vanilla {@link #applyCarvers} and {@link #buildSurface} will lazily
     * compute the {@code NoiseChunk} they need for cave placement and surface
     * rules; those operations do NOT depend on the surface heightmap that
     * {@code fillFromNoise} would have produced.
     */
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender,
                                                           RandomState randomState,
                                                           StructureManager structureManager,
                                                           ChunkAccess chunk) {
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();
        int minChunkX = chunk.getPos().getMinBlockX();
        int minChunkZ = chunk.getPos().getMinBlockZ();

        // Cache block states — BlockState lookup is cheap but de-virtualized
        // local refs are still marginally faster in a hot loop.
        final BlockState stone = Blocks.STONE.defaultBlockState();
        final BlockState water = Blocks.WATER.defaultBlockState();
        final BlockState bedrock = Blocks.BEDROCK.defaultBlockState();

        // MutableBlockPos avoids per-block allocation. setBlockState on a
        // ProtoChunk during chunk-gen does NOT trigger lighting/scheduled
        // tick updates — it just writes the section state.
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = minChunkX + localX;
                int worldZ = minChunkZ + localZ;
                int surfaceHeight = canonSurfaceHeight(worldX, worldZ);

                for (int y = minY; y < maxY; y++) {
                    BlockState state;
                    if (y == minY) {
                        state = bedrock;
                    } else if (y < surfaceHeight) {
                        state = stone;
                    } else if (y < SEA_LEVEL) {
                        state = water;
                    } else {
                        // Air — leave the section's default (air). Skip
                        // setBlockState to avoid 65K useless writes per
                        // column above the surface.
                        continue;
                    }

                    pos.set(worldX, y, worldZ);
                    chunk.setBlockState(pos, state, false);
                }
            }
        }

        // Synchronous fill — return already-completed future. Vanilla's
        // NoiseBasedChunkGenerator runs fillFromNoise on a worker thread via
        // supplyAsync(executor); we don't need to because our fill is
        // CPU-bound and fast (no noise sampling, no aquifer computation).
        return CompletableFuture.completedFuture(chunk);
    }

    /**
     * Apply cave carvers. Delegated to the wrapped NoiseBasedChunkGenerator
     * — caves are carved through whatever stone we placed in
     * {@link #fillFromNoise}. The wrapped generator will lazily compute
     * {@code NoiseChunk} for this chunk if it hasn't already been computed.
     */
    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState,
                              net.minecraft.world.level.biome.BiomeManager biomeManager,
                              StructureManager structureManager,
                              ChunkAccess chunk, GenerationStep.Carving step) {
        wrapped.applyCarvers(region, seed, randomState, biomeManager, structureManager, chunk, step);
    }

    /**
     * Apply biome surface rules (grass on plains, sand on beaches, snow on
     * Snow Domain, podzol/taiga in mountains, etc.). Delegated to the wrapped
     * NoiseBasedChunkGenerator — it queries the chunk's existing stone
     * surface (which we placed in {@link #fillFromNoise}) and replaces the
     * top few layers with the biome-appropriate block.
     */
    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager,
                             RandomState randomState, ChunkAccess chunk) {
        wrapped.buildSurface(region, structureManager, randomState, chunk);
    }

    @Override
    public int getGenDepth() {
        return wrapped.getGenDepth();
    }

    @Override
    public int getSeaLevel() {
        return SEA_LEVEL;
    }

    @Override
    public int getMinY() {
        return wrapped.getMinY();
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        wrapped.spawnOriginalMobs(region);
    }

    // ════════════════════════════════════════════════════════════════════
    //  CANON-AWARE HEIGHT/COLUMN QUERIES — surface shape from canon
    // ════════════════════════════════════════════════════════════════════

    /**
     * Get the surface height at (x, z), derived from canon geography.
     * This is the ONLY method that determines surface terrain shape —
     * {@link #fillFromNoise}, {@link #getBaseHeight}, and
     * {@link #getBaseColumn} all consult this.
     *
     * <p>The height is deterministic: same x, z, CANON_SEED → same result,
     * every save, every chunk load.
     */
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap,
                             LevelHeightAccessor level, RandomState randomState) {
        return canonSurfaceHeight(x, z);
    }

    /**
     * Get the base noise column at (x, z), built from canon surface height.
     * Returns bedrock at the bottom, stone up to the canon surface, water
     * up to sea level, air above.
     */
    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level,
                                      RandomState randomState) {
        int minY = level.getMinBuildHeight();
        int height = level.getHeight();
        int surfaceHeight = canonSurfaceHeight(x, z);

        BlockState[] states = new BlockState[height];
        for (int i = 0; i < height; i++) {
            int y = minY + i;
            if (y == minY) {
                states[i] = Blocks.BEDROCK.defaultBlockState();
            } else if (y < surfaceHeight) {
                states[i] = Blocks.STONE.defaultBlockState();
            } else if (y < SEA_LEVEL) {
                states[i] = Blocks.WATER.defaultBlockState();
            } else {
                states[i] = Blocks.AIR.defaultBlockState();
            }
        }
        return new NoiseColumn(minY, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        wrapped.addDebugScreenInfo(info, randomState, pos);
        info.add("[Er Gen Verse] Blueprint Chunk Generator (CANON-DRIVEN TERRAIN)");
        info.add("[Er Gen Verse] Canon Seed: " + DeterministicSeedHandler.CANON_SEED);
        int sx = pos.getX();
        int sz = pos.getZ();
        info.add("[Er Gen Verse] Canon Surface Height: " + canonSurfaceHeight(sx, sz)
                + " (offset " + getCanonTerrainOffset(sx, sz)
                + " + noise " + canonNoiseVariation(sx, sz) + ")");
    }

    // ════════════════════════════════════════════════════════════════════
    //  CANON SURFACE HEIGHT — pure deterministic function of (x, z)
    // ════════════════════════════════════════════════════════════════════

    /**
     * Compute the canon-derived surface height at (worldX, worldZ).
     *
     * <p>{@code BASE_SURFACE_HEIGHT + canonTerrainOffset + canonNoiseVariation},
     * clamped to a non-negative minimum (bedrock layer at {@code minY} must
     * always have at least one stone block above it).
     *
     * <p><b>CRON-COMPLETIONIST-67:</b> this method is now {@code public} so that
     * structure builders can resolve their center Y from the <i>same canon
     * authority</i> that the chunk generator uses — eliminating the heightmap
     * race condition where {@code level.getHeightmapPos(MOTION_BLOCKING_NO_LEAVES, ...)}
     * would return y=0 or a stale value if the chunk at the canon center wasn't
     * loaded yet when {@code buildForChunk} fired for an adjacent chunk. The
     * canon surface height is a pure deterministic function of (x, z) — it does
     * NOT depend on chunk-load state, so it returns the correct Y every time,
     * regardless of which chunks are loaded.
     *
     * <p>Builders should call this instead of {@code level.getHeightmapPos(...)}:
     * <pre>{@code
     *   int surfaceY = BlueprintChunkGenerator.canonSurfaceHeight(SECT_X, SECT_Z);
     *   return new BlockPos(SECT_X, surfaceY, SECT_Z);
     * }</pre>
     */
    public static int canonSurfaceHeight(int worldX, int worldZ) {
        int offset = getCanonTerrainOffset(worldX, worldZ);
        int noise = canonNoiseVariation(worldX, worldZ);
        int h = BASE_SURFACE_HEIGHT + offset + noise;
        // Clamp: never let the surface drop to or below 1 (need at least
        // bedrock + stone). Also never exceed a sane cap to avoid Y=320
        // pillars on canon warps (current max warp is +30, so the cap is
        // mostly a defensive measure).
        if (h < 2) return 2;
        if (h > 256) return 256;
        return h;
    }

    /**
     * Compute the canon terrain offset at (worldX, worldZ).
     * Returns the sum of all nearby canon location influences.
     * Positive = raise terrain (mountain), Negative = lower terrain (sea/tomb).
     *
     * <p>Each canon location has an influence that decays linearly from its
     * center to {@link #TERRAIN_WARP_RADIUS} blocks away. The offset is
     * deterministic and based on the blueprint's geography.
     */
    static int getCanonTerrainOffset(int worldX, int worldZ) {
        PlanetSuzakuBlueprint blueprint = PlanetSuzakuBlueprint.canonical();
        int totalOffset = 0;

        for (PlanetSuzakuBlueprint.CanonLocation loc : blueprint.allLocations().values()) {
            int dx = worldX - loc.x;
            int dz = worldZ - loc.z;
            int distSq = dx * dx + dz * dz;

            if (distSq >= TERRAIN_WARP_RADIUS_SQ) continue;

            double dist = Math.sqrt(distSq);
            double factor = 1.0 - (dist / TERRAIN_WARP_RADIUS);

            int warp = getTerrainWarpForLocation(loc);
            totalOffset += (int) (warp * factor);
        }

        return totalOffset;
    }

    /**
     * Compute a small deterministic value-noise variation at (worldX, worldZ).
     * Bilinear interpolation of a per-cell hash, scaled to ±{@link #NOISE_AMPLITUDE}.
     * Period = {@link #NOISE_PERIOD} blocks.
     *
     * <p>The hash is seeded by {@link DeterministicSeedHandler#CANON_SEED},
     * so the variation is identical every save. The purpose of this noise is
     * NOT to mimic natural terrain — it is to break the perfectly-flat plateau
     * that pure canon warping would produce between canon locations.
     */
    static int canonNoiseVariation(int worldX, int worldZ) {
        int cellX = Math.floorDiv(worldX, NOISE_PERIOD);
        int cellZ = Math.floorDiv(worldZ, NOISE_PERIOD);
        int fracX = worldX - cellX * NOISE_PERIOD;
        int fracZ = worldZ - cellZ * NOISE_PERIOD;

        // Smoothstep interpolation weights
        double sx = (double) fracX / NOISE_PERIOD;
        double sz = (double) fracZ / NOISE_PERIOD;
        sx = sx * sx * (3 - 2 * sx);
        sz = sz * sz * (3 - 2 * sz);

        // Four corner values in range [0, 2*NOISE_AMPLITUDE]
        double v00 = noiseHash(cellX,     cellZ);
        double v10 = noiseHash(cellX + 1, cellZ);
        double v01 = noiseHash(cellX,     cellZ + 1);
        double v11 = noiseHash(cellX + 1, cellZ + 1);

        double ix0 = v00 + (v10 - v00) * sx;
        double ix1 = v01 + (v11 - v01) * sx;
        double val = ix0 + (ix1 - ix0) * sz;

        // Center around zero: range [-NOISE_AMPLITUDE, +NOISE_AMPLITUDE]
        return (int) Math.round(val - NOISE_AMPLITUDE);
    }

    /**
     * Deterministic hash → value in [0, 2 * NOISE_AMPLITUDE]. Uses a
     * splitmix64-style mixing of (cellX, cellZ, CANON_SEED) so that
     * adjacent cells produce decorrelated values.
     */
    private static long noiseHash(int cellX, int cellZ) {
        long h = DeterministicSeedHandler.CANON_SEED;
        h ^= (long) cellX * 0x9E3779B97F4A7C15L;
        h ^= (long) cellZ * 0xC2B2AE3D27D4EB4FL;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        // Map to [0, 2 * NOISE_AMPLITUDE]
        return (h & 0xFFFFFFFFL) % (2L * NOISE_AMPLITUDE + 1);
    }

    /**
     * Get the terrain warp height for a canon location.
     * Positive = raise (mountains), Negative = lower (seas/tombs).
     *
     * <p><b>Canon fidelity (fact-checked against 仙逆):</b>
     * <ul>
     *   <li>恒岳山 (Heng Yue Mountain): the mountain where Heng Yue Sect is
     *       located. Canon describes it as Zhao Country's largest mountain.
     *       Raised terrain is correct.</li>
     *   <li>修魔海 (Sea of Devils): a vast, perilous sea east of Zhao Country.
     *       Lowered terrain (below sea level) creates ocean.</li>
     *   <li>朱雀墓 (Suzaku Tomb): underground inheritance site. The entrance
     *       is on the surface but leads underground. Slight depression is
     *       appropriate for the entrance area.</li>
     *   <li>Wang Family Village: "赵国某偏僻小山村" — a remote mountain village.
     *       Slight elevation fits the "mountain" description.</li>
     *   <li>藤家城 (Teng Family City): powerful family city in Zhao Country.
     *       Moderate elevation for a walled city.</li>
     * </ul>
     */
    private static int getTerrainWarpForLocation(PlanetSuzakuBlueprint.CanonLocation loc) {
        return switch (loc.id) {
            // Mountainous sect locations — raised terrain
            case "heng_yue_sect" -> MAX_WARP_HEIGHT; // 恒岳山 — largest mountain in Zhao
            case "soul_refining_sect" -> 15; // 炼魂宗 — mountain sect
            case "xuan_dao_sect" -> 15; // 玄道宗 — mountain sect
            case "luo_he_sect" -> 12; // 洛河门 — sect with spirit veins

            // Settlement cities — moderate elevation
            case "teng_family_city" -> 10; // 藤家城 — powerful family, elevated city
            case "tian_shui_city" -> 8; // 天水城 — northern military city
            case "four_sects_alliance" -> 6; // 四派联盟 — mortal towns

            // Wang Family Village — mountain village
            case "wang_family_village" -> 8; // 赵国偏僻小山村 — mountain village

            // Sea of Devils — significantly below sea level (oceanic)
            case "sea_of_devils" -> -MAX_WARP_HEIGHT; // 修魔海 — vast sea

            // Beast cities in/near Sea of Devils — slightly above sea
            case "qilin_city" -> -5; // 麒麟城 — beast-city near sea
            case "nan_dou_city" -> -5; // 南斗城 — beast-city near sea

            // Suzaku Tomb — underground, surface depression
            case "suzaku_tomb" -> -12; // 朱雀墓 — underground entrance

            // Snow Domain — slightly raised (cold highlands)
            case "snow_domain_capital" -> 10; // 雪域国 — cold elevated country

            // Vermilion Bird Capital — central, moderate terrain
            case "vermilion_bird_capital" -> 5; // 朱雀国 — central continent

            // Jue Ming Valley (决明谷) — Valley of Certain Death, slightly lower
            case "jue_ming_valley" -> -3; // 决明谷 — valley, slightly lower

            // Other countries — slight variation
            default -> 2;
        };
    }
}
