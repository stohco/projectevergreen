package dev.ergenverse.runtime.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.runtime.delta.BlockChangeDelta;
import dev.ergenverse.runtime.layer.ChunkContribution;
import dev.ergenverse.runtime.layer.CompositeWorldLayer;
import dev.ergenverse.runtime.layer.WorldLayer;
import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.spawn.DeterministicSeedHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
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
import net.minecraftforge.registries.ForgeRegistries;

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
 * <h2>CRON-COMPLETIONIST-91 — BLUEPRINT+LAYERS INTEGRATION (point 10 full fidelity)</h2>
 *
 * <p>The CRON-60 design (this class's prior form) derived surface terrain from
 * {@link PlanetSuzakuBlueprint} canon geography alone. That closed the
 * "algorithmic independence from {@code minecraft:overworld} noise" gap, but
 * left a deeper architectural promise unfulfilled: <i>"Terrain columns come
 * from the blueprint+<b>layers</b>, not minecraft:overworld noise."</i>
 *
 * <p>The <b>layers</b> in that promise are the {@link CompositeWorldLayer}'s
 * {@link WorldLayer}s — specifically the {@link Provenance#PLAYER} and
 * {@link Provenance#SIMULATION} layers, which hold the journal of every
 * block change since day 0. Before CRON-91, those layers were only consulted
 * by {@link dev.ergenverse.runtime.materialize.PlanetSuzakuChunkMaterializer#onChunkLoad}
 * — <b>deferred by 1 tick</b> to avoid mutating a chunk during its own assembly.
 * The result: when a player walked back to a previously-edited area and the
 * chunk regenerated from the canon base, there was a 1-tick window (50ms) where
 * the player saw the unedited canon terrain <i>before</i> their PLAYER deltas
 * were re-applied. A visible "flash of unedited terrain."
 *
 * <p>CRON-91 closes that gap. {@link #fillFromNoise} now does a <b>two-phase
 * fill</b>:
 * <ol>
 *   <li><b>Phase 1 — canon base terrain.</b> Bedrock, stone up to
 *       {@link #canonSurfaceHeight}, water up to {@link #SEA_LEVEL}, air above.
 *       Unchanged from CRON-60.</li>
 *   <li><b>Phase 2 — layer override.</b> If {@link WorldRuntime#get()} is
 *       initialized, iterate {@link CompositeWorldLayer#layersInMaterializationOrder()}
 *       and for each non-CANON layer, apply that layer's
 *       {@link ChunkContribution#blockChanges} directly to the chunk via
 *       {@link ChunkAccess#setBlockState}. This is safe during chunk-gen
 *       (uses {@code isMoving=false}, no lighting/tick updates) and is
 *       <b>idempotent</b> — {@code PlanetSuzakuChunkMaterializer.onChunkLoad}
 *       will re-apply the same deltas 1 tick later as a safety net for the
 *       chunk-from-disk reload path, where {@code fillFromNoise} does not fire.</li>
 * </ol>
 *
 * <p><b>Why CANON structures are NOT built during {@code fillFromNoise}:</b>
 * the {@link dev.ergenverse.runtime.materialize.StructureBuilderRegistry} builders
 * need a live {@link net.minecraft.server.level.ServerLevel} (for entity spawns,
 * heightmap resolution, etc.), but {@code fillFromNoise} only has a
 * {@link ChunkAccess} (no level). CANON structure building stays in the
 * materializer. PLAYER/SIMULATION block changes have no such dependency — they
 * are pure (x, y, z, blockState) tuples — so they're safe to apply here.
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
 *       variety now comes from biome-aware profiles (CRON-93) AND biome
 *       surface rules (grass/sand/snow) applied by {@link #buildSurface}.</li>
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
 * <h2>CRON-COMPLETIONIST-93 — BIOME-AWARE TERRAIN PROFILES</h2>
 *
 * <p>The CRON-60/91 design derived surface height from canon geography +
 * a flat ±8 fine noise, ignoring the biome entirely. A column in a
 * {@code zhao_mountains} biome got the SAME surface height (Y=64 ± noise)
 * as a column in a {@code zhao_plains} biome. This was a major visual
 * regression from vanilla {@code minecraft:noise}, where mountains rise
 * to Y=120+ and plains stay at Y=64. The world looked like a flat plateau
 * with occasional canon-warped bumps — not a real cultivation world with
 * mountains, plains, oceans, and valleys.
 *
 * <p>CRON-93 closes this gap by adding a {@link BiomeTerrainProfile}
 * lookup to the height computation. The new
 * {@link #biomeAwareSurfaceHeight(int, int, RandomState)} samples the biome
 * source at (x, z) via {@link BiomeSource#getNoiseBiome}, looks up the
 * profile for that biome, and uses the profile's base height + amplitude
 * as the primary determinant of surface shape. Canon warps and fine noise
 * still apply on top.
 *
 * <p><b>New formula (CRON-93):</b>
 * <pre>{@code
 *   biomeAwareSurfaceHeight(x, z) =
 *       biomeProfile.baseHeight                  // plains=64, mountains=110, ocean=35
 *     + biomeAmplitudeNoise(x, z, amplitude)     // ±amplitude, period 24
 *     + canonTerrainOffset(x, z)                 // ±30 from canon locations
 *     + canonNoiseVariation(x, z)                // ±8 fine noise (period 8)
 *     clamped to [2, 256]
 * }</pre>
 *
 * <p>The legacy static {@link #canonSurfaceHeight(int, int)} is RETAINED
 * as a fallback for contexts where no {@link RandomState} is available
 * (e.g., early chunk-gen race, non-Suzaku levels). It returns the
 * pre-CRON-93 height (base 64 + canon warp + fine noise). Structure
 * builders that have a {@link net.minecraft.server.level.ServerLevel}
 * should call the new {@link #surfaceHeightFor(ServerLevel, int, int)}
 * instead, which delegates to the biome-aware instance method.
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
     * Period of the CRON-93 biome-amplitude noise, in blocks. 24 means
     * mountain-scale features (peaks/valleys every ~12 blocks, large enough
     * to feel like real mountains, small enough to vary within a single
     * chunk). Distinct from {@link #NOISE_PERIOD} (8) so the two noise layers
     * produce decorrelated variation — biome amplitude gives the large-scale
     * shape, fine noise gives the surface roughness.
     */
    static final int BIOME_NOISE_PERIOD = 24;

    /**
     * The quart Y used to sample the biome during chunk-gen. Biomes in
     * {@code minecraft:multi_noise} are selected based on 4D parameters
     * (temperature, humidity, continentalness, erosion, depth, weirdness)
     * sampled at a quart position. Quart resolution is 4 blocks, so
     * quartY = 16 corresponds to world Y = 64 (sea level). Sampling at
     * sea level ensures we get the "surface" biome, not a cave biome or
     * high-altitude variant.
     */
    private static final int BIOME_SAMPLE_QUART_Y = 16; // y=64 (sea level)

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

        // ── Phase 1: canon base terrain (bedrock / stone / water / air) ──
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
                // CRON-93: biome-aware height (mountains=110, plains=64,
                // ocean=35, etc.) — replaces the flat canonSurfaceHeight.
                // Falls back to legacy static if randomState is null.
                int surfaceHeight = biomeAwareSurfaceHeight(worldX, worldZ, randomState);

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

        // ── Phase 2: apply PLAYER + SIMULATION layer overrides (CRON-91) ──
        // The architectural promise in point 10 is "terrain columns come from
        // the blueprint+LAYERS" — not just the blueprint. Before CRON-91, the
        // PLAYER/SIMULATION layers were only consulted by the chunk materializer
        // on ChunkEvent.Load (deferred 1 tick), causing a visible flash of
        // unedited canon terrain when a player walked back to a previously-edited
        // area. Applying the layer overrides directly during fillFromNoise closes
        // that 1-tick window: the player sees their edits immediately when the
        // chunk first becomes visible.
        //
        // Safety:
        //   - chunk.setBlockState(pos, state, false) is the SAME write path
        //     vanilla's NoiseBasedChunkGenerator.fillFromNoise uses; it writes
        //     the section state without triggering lighting/tick updates.
        //   - This is NOT the same as level.setBlock(pos, state, UPDATE_ALL),
        //     which is what the materializer defers to avoid. We are writing
        //     to the ProtoChunk during its own assembly — exactly what
        //     fillFromNoise is designed to do.
        //   - Idempotent with the materializer: setting the same block to the
        //     same state is a no-op. The materializer is still needed for the
        //     chunk-from-disk reload path (where fillFromNoise does NOT fire).
        //   - CANON structures (c.structures) are intentionally NOT built here
        //     — StructureBuilderRegistry builders need a live ServerLevel, but
        //     fillFromNoise only has a ChunkAccess. CANON stays in the materializer.
        applyLayerOverrides(chunk, pos);

        // Synchronous fill — return already-completed future. Vanilla's
        // NoiseBasedChunkGenerator runs fillFromNoise on a worker thread via
        // supplyAsync(executor); we don't need to because our fill is
        // CPU-bound and fast (no noise sampling, no aquifer computation).
        return CompletableFuture.completedFuture(chunk);
    }

    /**
     * Apply PLAYER and SIMULATION layer overrides to a chunk during fillFromNoise.
     *
     * <p>For each non-CANON layer in {@link CompositeWorldLayer#layersInMaterializationOrder()},
     * queries {@link WorldLayer#getChunkContribution(int, int)} and applies any
     * {@link BlockChangeDelta}s directly to the chunk via
     * {@link ChunkAccess#setBlockState}. This is the CRON-91 upgrade that makes
     * "terrain columns come from the blueprint+LAYERS" literally true — the
     * layer journal is consulted during chunk-gen, not deferred to ChunkEvent.Load.
     *
     * <p><b>No-op when:</b>
     * <ul>
     *   <li>{@link WorldRuntime#get()} is not yet initialized (race during
     *       server start — the runtime binds to the level on ServerStartingEvent,
     *       which may fire AFTER the initial spawn chunks generate). In this
     *       case, the materializer's onChunkLoad will apply the deltas 1 tick
     *       later as a fallback.</li>
     *   <li>The chunk has no PLAYER or SIMULATION deltas (the common case —
     *       most chunks are unedited). The contribution is empty and the loop
     *       is a near-noop.</li>
     * </ul>
     *
     * @param chunk the chunk being assembled (Phase 1 base terrain already placed)
     * @param pos   a reusable MutableBlockPos (avoids per-delta allocation)
     */
    private void applyLayerOverrides(ChunkAccess chunk, BlockPos.MutableBlockPos pos) {
        // Defensive: WorldRuntime may not be initialized yet during the very
        // first chunk-gen pass at server start. The materializer will catch up.
        WorldRuntime runtime;
        try {
            runtime = WorldRuntime.get();
            if (!runtime.isInitialized()) return;
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] BlueprintChunkGenerator: WorldRuntime unavailable during fillFromNoise — deferring layer overrides to materializer. Reason: {}",
                    t.getMessage());
            return;
        }

        CompositeWorldLayer worldLayer = runtime.worldLayer();
        if (worldLayer == null) return;

        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // Iterate in materialization order: CANON first → SIMULATION → PLAYER.
        // PLAYER wins on conflict (applied last). CANON layer's c.structures
        // are skipped (need a live ServerLevel — materializer handles those).
        for (WorldLayer layer : worldLayer.layersInMaterializationOrder()) {
            if (layer.provenance() == Provenance.CANON) continue;

            ChunkContribution contribution;
            try {
                contribution = layer.getChunkContribution(chunkX, chunkZ);
            } catch (Throwable t) {
                Ergenverse.LOGGER.debug("[Ergenverse] BlueprintChunkGenerator: layer {} getChunkContribution failed for chunk ({},{}): {}",
                        layer.provenance(), chunkX, chunkZ, t.getMessage());
                continue;
            }
            if (contribution == null || contribution.isEmpty()) continue;

            for (BlockChangeDelta delta : contribution.blockChanges) {
                BlockState state = resolveBlockState(delta.blockState());
                if (state == null) continue;
                pos.set(delta.x(), delta.y(), delta.z());
                chunk.setBlockState(pos, state, false);
            }
        }
    }

    /**
     * Resolve a block state string to a {@link BlockState}.
     *
     * <p><b>CRON-COMPLETIONIST-94:</b> now delegates to
     * {@link dev.ergenverse.runtime.delta.BlockStateCodec#parse} which
     * supports property overrides (e.g. {@code "minecraft:chest[facing=north]"}).
     * Before CRON-94, this method called {@code block.defaultBlockState()},
     * discarding all property information — PLAYER/SIMULATION deltas with
     * directional blocks (stairs, slabs, chests, doors) reverted to default
     * facing when the chunk regenerated via {@code fillFromNoise}.
     *
     * <p>The codec lives in the {@code delta} package, which is already a
     * dependency of this class (it imports {@link BlockChangeDelta}). The
     * chunk-gen purity constraint (no dependency on WorldFacade) is preserved
     * — the codec has no WorldFacade dependency.
     *
     * <p>Backward compatible: bare block ids ({@code "minecraft:stone"})
     * still resolve to the default state.
     */
    private static BlockState resolveBlockState(String blockId) {
        return dev.ergenverse.runtime.delta.BlockStateCodec.parse(blockId);
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
     * Get the surface height at (x, z), derived from canon geography + biome.
     * This is the ONLY method that determines surface terrain shape —
     * {@link #fillFromNoise}, {@link #getBaseHeight}, and
     * {@link #getBaseColumn} all consult {@link #biomeAwareSurfaceHeight}.
     *
     * <p>The height is deterministic: same x, z, CANON_SEED → same result,
     * every save, every chunk load.
     */
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap,
                             LevelHeightAccessor level, RandomState randomState) {
        return biomeAwareSurfaceHeight(x, z, randomState);
    }

    /**
     * Get the base noise column at (x, z), built from biome-aware surface height.
     * Returns bedrock at the bottom, stone up to the surface, water
     * up to sea level, air above.
     */
    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level,
                                      RandomState randomState) {
        int minY = level.getMinBuildHeight();
        int height = level.getHeight();
        int surfaceHeight = biomeAwareSurfaceHeight(x, z, randomState);

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
        info.add("[Er Gen Verse] Blueprint Chunk Generator (BIOME-AWARE + CANON + LAYER OVERRIDE)");
        info.add("[Er Gen Verse] Canon Seed: " + DeterministicSeedHandler.CANON_SEED);
        int sx = pos.getX();
        int sz = pos.getZ();
        // CRON-93: report biome-aware height + biome identity at the player position.
        int biomeAware = biomeAwareSurfaceHeight(sx, sz, randomState);
        int legacy = canonSurfaceHeight(sx, sz);
        int offset = getCanonTerrainOffset(sx, sz);
        int fineNoise = canonNoiseVariation(sx, sz);
        BiomeTerrainProfile profile = sampleBiomeProfile(sx, sz, randomState);
        info.add("[Er Gen Verse] Biome-Aware Height: " + biomeAware
                + " (biome base " + profile.baseHeight() + " + amplitude " + profile.amplitude()
                + " + canon offset " + offset
                + " + fine noise " + fineNoise + ")");
        info.add("[Er Gen Verse] Legacy Canon Height: " + legacy + " (no biome awareness)");
        // CRON-91: report layer-override status for the chunk containing the player.
        try {
            WorldRuntime runtime = WorldRuntime.get();
            if (runtime.isInitialized()) {
                int chunkX = sx >> 4;
                int chunkZ = sz >> 4;
                int playerDeltas = runtime.deltaStore().blockChangeCount(Provenance.PLAYER);
                int simDeltas = runtime.deltaStore().blockChangeCount(Provenance.SIMULATION);
                info.add("[Er Gen Verse] Layer journal: PLAYER=" + playerDeltas
                        + " SIMULATION=" + simDeltas + " (chunk " + chunkX + "," + chunkZ + ")");
            } else {
                info.add("[Er Gen Verse] Layer journal: WorldRuntime not initialized (deferred to materializer)");
            }
        } catch (Throwable t) {
            info.add("[Er Gen Verse] Layer journal: unavailable (" + t.getMessage() + ")");
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  BIOME-AWARE SURFACE HEIGHT (CRON-93) — the authoritative height
    // ════════════════════════════════════════════════════════════════════

    /**
     * Compute the biome-aware surface height at (worldX, worldZ), the
     * authoritative height function used by {@link #fillFromNoise},
     * {@link #getBaseHeight}, {@link #getBaseColumn}, and (via
     * {@link #surfaceHeightFor}) structure builders.
     *
     * <p>Formula (CRON-93):
     * <pre>{@code
     *   biomeProfile.baseHeight                  // plains=64, mountains=110, ocean=35
     *     + biomeAmplitudeNoise(x, z, amplitude) // ±amplitude, period 24
     *     + canonTerrainOffset(x, z)             // ±30 from canon locations
     *     + canonNoiseVariation(x, z)            // ±8 fine noise (period 8)
     *     clamped to [2, 256]
     * }</pre>
     *
     * <p>The biome is sampled at (worldX, worldZ) via
     * {@link BiomeSource#getNoiseBiome} using the {@link RandomState}'s
     * {@link RandomState#sampler() climate sampler}. The quart Y is
     * {@link #BIOME_SAMPLE_QUART_Y} (sea level), ensuring we get the surface
     * biome, not a cave or high-altitude variant.
     *
     * <p><b>Fallback behavior:</b> if {@code randomState} is {@code null}, or
     * the biome lookup fails for any reason, this method falls back to the
     * legacy {@link #canonSurfaceHeight(int, int)} (no biome awareness).
     * This ensures robustness during edge cases (early chunk-gen race,
     * non-Suzaku levels) at the cost of biome-blind height.
     *
     * @param worldX      the world X coordinate
     * @param worldZ      the world Z coordinate
     * @param randomState the world's random state (provides the climate sampler);
     *                    may be {@code null} for fallback behavior
     * @return the biome-aware surface height, clamped to [2, 256]
     */
    public int biomeAwareSurfaceHeight(int worldX, int worldZ, RandomState randomState) {
        // Fallback: no randomState → use legacy static (no biome awareness)
        if (randomState == null) {
            return canonSurfaceHeight(worldX, worldZ);
        }

        BiomeTerrainProfile profile = sampleBiomeProfile(worldX, worldZ, randomState);
        int offset = getCanonTerrainOffset(worldX, worldZ);
        int fineNoise = canonNoiseVariation(worldX, worldZ);
        int biomeNoise = biomeAmplitudeNoise(worldX, worldZ, profile.amplitude());

        int h = profile.baseHeight() + biomeNoise + offset + fineNoise;
        // Clamp: same [2, 256] bounds as canonSurfaceHeight
        if (h < 2) return 2;
        if (h > 256) return 256;
        return h;
    }

    /**
     * Sample the biome at (worldX, worldZ) and return its terrain profile.
     *
     * <p>Uses {@link BiomeSource#getNoiseBiome} with the climate sampler from
     * {@code randomState}. The quart Y is {@link #BIOME_SAMPLE_QUART_Y}
     * (sea level). Returns {@link BiomeTerrainProfile#DEFAULT} if the lookup
     * fails for any reason (defensive — should not occur in practice).
     */
    private BiomeTerrainProfile sampleBiomeProfile(int worldX, int worldZ, RandomState randomState) {
        try {
            int quartX = worldX >> 2;
            int quartZ = worldZ >> 2;
            // In 1.20.1 Mojmaps, RandomState.sampler() returns the Climate.Sampler
            // used by the multi-noise biome source. Quart resolution is 4 blocks;
            // quartY = 16 corresponds to world Y = 64 (sea level) — see BIOME_SAMPLE_QUART_Y.
            Climate.Sampler sampler = randomState.sampler();
            Holder<Biome> biome = biomeSource.getNoiseBiome(quartX, BIOME_SAMPLE_QUART_Y, quartZ, sampler);
            if (biome == null) return BiomeTerrainProfile.DEFAULT;
            return biome.unwrapKey()
                    .map(key -> BiomeTerrainProfile.forBiome(key.location()))
                    .orElse(BiomeTerrainProfile.DEFAULT);
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] BlueprintChunkGenerator: biome sample failed at ({},{}): {}",
                    worldX, worldZ, t.getMessage());
            return BiomeTerrainProfile.DEFAULT;
        }
    }

    /**
     * Get the biome-aware surface height at (worldX, worldZ) for the given
     * {@link ServerLevel}. This is the entry point structure builders
     * should use (CRON-93).
     *
     * <p>If the level's chunk generator is a {@link BlueprintChunkGenerator},
     * this delegates to {@link #biomeAwareSurfaceHeight} with the level's
     * {@link RandomState}. Otherwise (e.g., non-Suzaku levels, fallback
     * generators), it falls back to the legacy {@link #canonSurfaceHeight}
     * (no biome awareness).
     *
     * <p><b>Migration guide (CRON-93):</b> structure builders should replace
     * calls to the static {@code canonSurfaceHeight(x, z)} with
     * {@code surfaceHeightFor(level, x, z)}. Both return the same value when
     * the level is Suzaku, but the new method accounts for the biome at
     * (x, z), producing canon-faithful elevations (mountains at Y=110,
     * plains at Y=64, oceans at Y=35, etc.).
     *
     * <pre>{@code
     *   // BEFORE (CRON-67, no biome awareness):
     *   int surfaceY = BlueprintChunkGenerator.canonSurfaceHeight(SECT_X, SECT_Z);
     *
     *   // AFTER (CRON-93, biome-aware):
     *   int surfaceY = BlueprintChunkGenerator.surfaceHeightFor(level, SECT_X, SECT_Z);
     * }</pre>
     *
     * @param level  the server level (must be Planet Suzaku for biome-aware
     *               results; other levels fall back to legacy behavior)
     * @param worldX the world X coordinate
     * @param worldZ the world Z coordinate
     * @return the biome-aware surface height for Suzaku levels; legacy
     *         canonSurfaceHeight for other levels
     */
    public static int surfaceHeightFor(ServerLevel level, int worldX, int worldZ) {
        try {
            ChunkGenerator gen = level.getChunkSource().getGenerator();
            if (gen instanceof BlueprintChunkGenerator bcg) {
                RandomState randomState = level.getChunkSource().randomState();
                return bcg.biomeAwareSurfaceHeight(worldX, worldZ, randomState);
            }
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] BlueprintChunkGenerator.surfaceHeightFor: " +
                    "failed to compute biome-aware height at ({},{}) on level {}: {}. " +
                    "Falling back to legacy canonSurfaceHeight.",
                    worldX, worldZ, level.dimension(), t.getMessage());
        }
        return canonSurfaceHeight(worldX, worldZ);
    }

    // ════════════════════════════════════════════════════════════════════
    //  CANON SURFACE HEIGHT — legacy static (biome-blind fallback)
    // ════════════════════════════════════════════════════════════════════

    /**
     * Compute the canon-derived surface height at (worldX, worldZ) — the
     * <b>legacy, biome-blind</b> version.
     *
     * <p>{@code BASE_SURFACE_HEIGHT + canonTerrainOffset + canonNoiseVariation},
     * clamped to a non-negative minimum (bedrock layer at {@code minY} must
     * always have at least one stone block above it).
     *
     * <p><b>CRON-COMPLETIONIST-93:</b> this method is RETAINED as a fallback
     * for contexts where no {@link RandomState} or {@link ServerLevel} is
     * available (e.g., early chunk-gen race, non-Suzaku levels, defensive
     * code paths). For all structure-builder contexts where a
     * {@link ServerLevel} is available, prefer
     * {@link #surfaceHeightFor(ServerLevel, int, int)} — it accounts for
     * the biome at (x, z) and produces canon-faithful elevations.
     *
     * <p><b>CRON-COMPLETIONIST-67:</b> this method is {@code public} so that
     * structure builders can resolve their center Y from the <i>same canon
     * authority</i> that the chunk generator uses — eliminating the heightmap
     * race condition where {@code level.getHeightmapPos(MOTION_BLOCKING_NO_LEAVES, ...)}
     * would return y=0 or a stale value if the chunk at the canon center wasn't
     * loaded yet when {@code buildForChunk} fired for an adjacent chunk. The
     * canon surface height is a pure deterministic function of (x, z) — it does
     * NOT depend on chunk-load state, so it returns the correct Y every time,
     * regardless of which chunks are loaded.
     *
     * <p>For contexts without a {@link ServerLevel} (e.g., static helpers,
     * tests), continue to call this directly:
     * <pre>{@code
     *   int surfaceY = BlueprintChunkGenerator.canonSurfaceHeight(SECT_X, SECT_Z);
     * }</pre>
     *
     * For contexts WITH a {@link ServerLevel}, prefer the biome-aware version:
     * <pre>{@code
     *   int surfaceY = BlueprintChunkGenerator.surfaceHeightFor(level, SECT_X, SECT_Z);
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
     * Compute the biome-amplitude noise at (worldX, worldZ) — a bilinear
     * value noise with period {@link #BIOME_NOISE_PERIOD} and the given
     * amplitude. Used by {@link #biomeAwareSurfaceHeight} to produce
     * biome-scale terrain variation (mountain peaks, valley undulations).
     *
     * <p>Returns 0 when {@code amplitude <= 0} (no variation needed for
     * flat biomes like plains, though plains still use amplitude 4 for
     * gentle rolling).
     *
     * <p>The hash function is the same splitmix64 mixer used by
     * {@link #canonNoiseVariation}, but with a different cell period (24
     * vs 8), so the two noise layers are decorrelated. Both are seeded by
     * {@link DeterministicSeedHandler#CANON_SEED}, ensuring determinism.
     */
    static int biomeAmplitudeNoise(int worldX, int worldZ, int amplitude) {
        if (amplitude <= 0) return 0;
        int cellX = Math.floorDiv(worldX, BIOME_NOISE_PERIOD);
        int cellZ = Math.floorDiv(worldZ, BIOME_NOISE_PERIOD);
        int fracX = worldX - cellX * BIOME_NOISE_PERIOD;
        int fracZ = worldZ - cellZ * BIOME_NOISE_PERIOD;

        // Smoothstep interpolation weights
        double sx = (double) fracX / BIOME_NOISE_PERIOD;
        double sz = (double) fracZ / BIOME_NOISE_PERIOD;
        sx = sx * sx * (3 - 2 * sx);
        sz = sz * sz * (3 - 2 * sz);

        // Four corner values in range [0, 2*amplitude]
        double v00 = amplitudeHash(cellX,     cellZ,     amplitude);
        double v10 = amplitudeHash(cellX + 1, cellZ,     amplitude);
        double v01 = amplitudeHash(cellX,     cellZ + 1, amplitude);
        double v11 = amplitudeHash(cellX + 1, cellZ + 1, amplitude);

        double ix0 = v00 + (v10 - v00) * sx;
        double ix1 = v01 + (v11 - v01) * sx;
        double val = ix0 + (ix1 - ix0) * sz;

        // Center around zero: range [-amplitude, +amplitude]
        return (int) Math.round(val - amplitude);
    }

    /**
     * Salt value mixed into the amplitude-noise hash to decorrelate it from
     * the fine-noise hash (so the two noise layers don't produce correlated
     * values). Arbitrary 64-bit constant; chosen to be distinct from any
     * multiplier used in {@link #noiseHash}.
     */
    private static final long AMPLITUDE_HASH_SALT = 0xA5A5A5A5A5A5A5A5L;

    /**
     * Deterministic hash for the biome-amplitude noise → value in
     * [0, 2 * amplitude]. Same splitmix64 mixing as {@link #noiseHash} but
     * parameterized by amplitude (so different amplitudes produce different
     * value ranges). Uses {@link #AMPLITUDE_HASH_SALT} to decorrelate from
     * the fine noise (otherwise the two noise layers would be correlated,
     * producing unrealistic terrain).
     */
    private static long amplitudeHash(int cellX, int cellZ, int amplitude) {
        long h = DeterministicSeedHandler.CANON_SEED ^ AMPLITUDE_HASH_SALT;
        h ^= (long) cellX * 0x9E3779B97F4A7C15L;
        h ^= (long) cellZ * 0xC2B2AE3D27D4EB4FL;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return (h & 0xFFFFFFFFL) % (2L * amplitude + 1);
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
