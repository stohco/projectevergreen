package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.ChunkBounds;
import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.runtime.delta.WorldDeltaStore;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * LuoHeSectBuilder — FULLY hand-built Luo He Sect (洛河门 / Luo He Men).
 *
 * <p>Constitution: "The world is completely hand-crafted, accurate to the novels.
 * NEVER write a script that replaces vanilla blocks with other blocks as a shortcut.
 * Every structure must be hand-authored."
 *
 * <p><b>Canon (CRON-COMPLETIONIST-72 correction):</b> Luo He Sect (洛河门) appears
 * in Er Gen's "Renegade Immortal" (仙逆), NOT "A Will Eternal" (一念永恒) as the
 * prior Javadoc incorrectly stated. Verified via Baidu Baike + Tencent Video +
 * Sohu: 洛河门 is a cultivation sect in 火焚国 (Huo Fen Country), known for its
 * alchemy (炼丹术) tradition. The sect's founding ancestor (始祖) is 兰若 (Lan
 * Ruo), a Nascent Soul middle-stage (元婴中期) cultivator who wields the Green
 * Lotus Yin Fire (青莲阴火). 李慕婉 (Li Muwan) — Wang Lin's lifelong attachment
 * — is a disciple of this sect before she later joins 云天宗 (Yun Tian Sect) in
 * 楚国 (Chu Country). The sect's water-cultivation motif in this builder is
 * mod-original theming derived from the 洛 (river) character — the novel does
 * not detail a specific architectural style, so the prismarine-and-canal palette
 * is an aesthetic inference, not canon.
 *
 * <p>Architectural style (mod-original): Prismarine (water affinity), sea
 * lanterns, blue/cyan tones, spruce and birch (river valley woods), stone
 * bricks, quartz (elegant sect), water channels, lily pads, azaleas. The sect
 * feels WET and serene — water channels run through every district, mist hangs
 * in courtyards. This is an architectural inference from the sect's name; canon
 * does not specify architectural details.
 *
 * <h2>CRON-COMPLETIONIST-72 — chunk-scoped migration</h2>
 * This builder was the last of the 4 "(ServerLevel)-only" builders (alongside
 * QilinCity, SnowDomainCapital, VermilionBirdImperialCity) still using the
 * legacy full-build path with raw {@code level.setBlock} calls and no chunk
 * filter / no provenance guard. CRON-72 migrates it to the canonical chunk-scoped
 * pattern established by WangFamilyVillageBuilder (CRON-62/63) and applied to
 * 6 other builders (HengYue CRON-65, TengFamily/TianShui/NanDou/SoulRefining
 * CRON-66, XuanDao CRON-67/70). All placements now flow through {@link #sb}
 * which applies the chunk filter (CURRENT_BOUNDS) and provenance-aware rebuild
 * guard (PLAYER/SIMULATION delta). The canon coordinate now sources from
 * {@link PlanetSuzakuBlueprint#LUO_HE_SECT} (3000, 0, 2400) — the prior
 * computation {@code WangFamilyVillageBuilder.VILLAGE_X - 2400 = 1442} placed
 * the sect at (1442, ?, 416), wildly off-canon. The chunk-materializer only
 * invokes this builder when chunks at the CANON coordinate load, so the prior
 * mis-placement meant the sect NEVER materialized (the builder ran at (1442, ?,
 * 416) but no chunk-load event at (1442, ?, 416) triggered it — it was only
 * invoked via SpawnEventHandler server-start or /ergenverse build).
 *
 * <p>Districts (11):
 * <ol>
 *   <li>Entry Bridge — stone arch bridge over river canal leading to outer gate</li>
 *   <li>Outer Gate — elegant arch with water flowing beneath it</li>
 *   <li>River Canal — water channel running N-S through the entire sect</li>
 *   <li>Central Courtyard — open plaza with jade pond and waterfall feature</li>
 *   <li>Pill Refinery — water-based alchemy hall with cauldrons and brewing</li>
 *   <li>Talisman Workshop — inscription hall with anvils and crafting</li>
 *   <li>Library of Flowing Waters — scroll repository with aquatic motifs</li>
 *   <li>Elder Pavilion — raised meeting hall overlooking the canal</li>
 *   <li>Disciple Dormitories — 6 rooms along the canal (3 per side)</li>
 *   <li>Spirit Herb Garden — waterside garden with aquatic herbs</li>
 *   <li>Outer Wall — perimeter wall with water-filled moat</li>
 * </ol>
 *
 * <h2>Harsh Self-Critique</h2>
 * <ul>
 *   <li><b>Canon (CRON-72 corrected):</b> 洛河门 is from 仙逆 (Renegade
 *       Immortal), NOT 一念永恒 (A Will Eternal) as prior Javadoc claimed.
 *       The founding ancestor is 兰若 (Lan Ruo, Nascent Soul middle stage).
 *       Architecture and layout are mod-original inferences from the sect's
 *       water-cultivation name — canon does not detail specific buildings.</li>
 *   <li>The "waterfall" is a vertical column of water source blocks — crudest
 *       possible representation. Real waterfalls need flowing water physics which
 *       MC handles, but the column itself is ungraceful.</li>
 *   <li>The canal is a flat trench of water source blocks — functional but
 *       lacks natural riverbank shaping, current variation, or depth changes.</li>
 *   <li>Moat is just water next to walls — no sloped banks, no hidden underwater
 *       entrances, no drawbridge mechanism.</li>
 *   <li>The "bridge" is stone blocks over water — no arch, no railing, no
 *       decorative carving. Reads as "stone floor over water."</li>
 *   <li>All rooms use the same flat-floor-walls pattern. A water-cultivation sect
 *       should have flooded rooms, underwater meditation chambers, tide-locked
 *       doors. None of that exists here.</li>
 * </ul>
 */
public final class LuoHeSectBuilder {

    private LuoHeSectBuilder() {}

    // ── Canonical position on Planet Suzaku (CRON-72: from blueprint) ──
    // CRON-72: prior to this round, SECT_X/SECT_Z were computed as
    // WangFamilyVillageBuilder.VILLAGE_X - 2400 (= 1442) and
    // VILLAGE_Z + 1600 (= 416). This placed the sect at (1442, ?, 416),
    // WILDLY OFF the canon coordinate (3000, 0, 2400) declared in
    // PlanetSuzakuBlueprint.LUO_HE_SECT. The chunk-materializer invokes this
    // builder when chunks at the CANON coordinate load, so the prior
    // mis-placement meant the sect would be built at (1442, ?, 416) IF the
    // builder was invoked, but the materializer's chunk-load trigger was at
    // (3000, ?, 2400) — meaning the sect NEVER materialized via the normal
    // chunk-load path. It only appeared if SpawnEventHandler's server-start
    // hook or /ergenverse build command invoked it explicitly. Even then,
    // the structure appeared at (1442, ?, 416) instead of (3000, ?, 2400).
    // This is a latent canon-coordinate bug, fixed by sourcing the constants
    // directly from PlanetSuzakuBlueprint.LUO_HE_SECT.
    public static final int SECT_X = PlanetSuzakuBlueprint.LUO_HE_SECT.x;
    public static final int SECT_Z = PlanetSuzakuBlueprint.LUO_HE_SECT.z;

    private static final int SECT_RADIUS = 45;
    private static final int WALL_HEIGHT = 8;

    // ── Chunk-scoped build infrastructure (CRON-COMPLETIONIST-72) ──────
    //
    // Mirrors the WangFamilyVillageBuilder (CRON-62/63) and XuanDaoSectBuilder
    // (CRON-66/70) pattern. The chunk-materializer invokes buildForChunk(level,
    // bounds) for EACH chunk that overlaps the sect footprint. The ThreadLocal
    // CURRENT_BOUNDS holds the active bounds during a buildInternal() call;
    // the sb() helper checks it and skips any placement outside the bounds.
    // When bounds is null (full-build path — commands, login events), no
    // filtering occurs.

    /** Active chunk bounds during a buildInternal() pass, or null for full-build. */
    private static final ThreadLocal<ChunkBounds> CURRENT_BOUNDS = new ThreadLocal<>();

    /**
     * Filtered setBlock — the ONLY block-placement call site in this class.
     * Three guards, in order:
     *
     * <p><b>1. Chunk filter (CRON-62 pattern):</b> if CURRENT_BOUNDS is non-null
     * and (x, z) falls outside the bounds, skip.
     *
     * <p><b>2. Provenance-aware rebuild guard (CRON-63 pattern):</b> if
     * CURRENT_BOUNDS is non-null, consult the {@link WorldDeltaStore} for a
     * PLAYER or SIMULATION delta at (x, y, z). If either exists, skip the
     * placement — player/sim intent wins over CANON.
     *
     * <p><b>3. Placement:</b> if both guards pass, call level.setBlock.
     */
    private static void sb(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        ChunkBounds b = CURRENT_BOUNDS.get();
        if (b != null) {
            if (!b.contains(pos.getX(), pos.getZ())) return;
            if (hasPlayerOrSimulationDelta(pos)) return;
        }
        level.setBlock(pos, state, flags);
    }

    /**
     * Provenance-aware guard helper (CRON-63 pattern). Returns true if a
     * PLAYER or SIMULATION delta is recorded at {@code pos}. O(1) per call.
     * Defensive: returns false if WorldRuntime is not initialized.
     */
    private static boolean hasPlayerOrSimulationDelta(BlockPos pos) {
        try {
            WorldRuntime runtime = WorldRuntime.get();
            if (!runtime.isInitialized()) return false;
            WorldDeltaStore store = runtime.deltaStore();
            int x = pos.getX(), y = pos.getY(), z = pos.getZ();
            return store.hasBlock(x, y, z, Provenance.PLAYER)
                    || store.hasBlock(x, y, z, Provenance.SIMULATION);
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] Provenance guard failed at {}: {} — proceeding with placement.",
                    pos, t.getMessage());
            return false;
        }
    }

    /**
     * Resolve the sect center BlockPos using the canon surface height from
     * {@link dev.ergenverse.runtime.worldgen.BlueprintChunkGenerator#canonSurfaceHeight}.
     * Eliminates the prior hardcoded y=64 placeholder.
     */
    public static BlockPos getSectCenter(ServerLevel level) {
        int surfaceY = dev.ergenverse.runtime.worldgen.BlueprintChunkGenerator.surfaceHeightFor(level, SECT_X, SECT_Z);
        return new BlockPos(SECT_X, surfaceY, SECT_Z);
    }

    /**
     * Chunk-scoped build entry point — invoked by the chunk-materializer for
     * each chunk that overlaps the sect footprint.
     */
    public static void buildForChunk(ServerLevel level, @Nullable ChunkBounds bounds) {
        ChunkBounds prev = CURRENT_BOUNDS.get();
        CURRENT_BOUNDS.set(bounds);
        try {
            buildInternal(level, getSectCenter(level));
        } finally {
            if (prev == null) CURRENT_BOUNDS.remove();
            else CURRENT_BOUNDS.set(prev);
        }
    }

    /**
     * Full-build entry point — used by SpawnEventHandler (server-start) and
     * ErgenverseCommand. Idempotent: guarded by {@link #isAlreadyBuilt}.
     */
    public static void build(ServerLevel level) {
        BlockPos center = getSectCenter(level);
        if (isAlreadyBuilt(level, center)) {
            Ergenverse.LOGGER.debug("[Ergenverse] Luo He Sect already built — build() is a no-op.");
            return;
        }
        Ergenverse.LOGGER.info("[Ergenverse] Building Luo He Sect at {}", center);
        buildInternal(level, center);
        Ergenverse.LOGGER.info("[Ergenverse] Luo He Sect construction complete.");
    }

    /**
     * Check if the sect is already built by looking for the marker PRISMARINE
     * at {@code center.above(WALL_HEIGHT + 1)} — a stable position above the
     * outer wall cap.
     *
     * <p><b>CRON-COMPLETIONIST-69 — provenance-aware rebuild guard.</b>
     * If the player (or simulation) has recorded a delta at the marker
     * position, returns {@code true} (sect was built, then edited; don't
     * rebuild). See {@link dev.ergenverse.runtime.materialize.ProvenanceAwareRebuildGuard}.
     */
    public static boolean isAlreadyBuilt(ServerLevel level, BlockPos center) {
        BlockPos markerPos = center.above(WALL_HEIGHT + 1);
        if (level.getBlockState(markerPos).getBlock() == Blocks.PRISMARINE) return true;
        if (dev.ergenverse.runtime.materialize.ProvenanceAwareRebuildGuard.shouldSkipRebuild(markerPos)) return true;
        return false;
    }

    // ── Block palette — water, jade, river, serene ──────────────────
    private static final BlockState PRISMARINE            = Blocks.PRISMARINE.defaultBlockState();
    private static final BlockState PRISMARINE_BRICKS     = Blocks.PRISMARINE_BRICKS.defaultBlockState();
    private static final BlockState SEA_LANTERN          = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState DARK_PRISMARINE       = Blocks.DARK_PRISMARINE.defaultBlockState();
    private static final BlockState WATER                = Blocks.WATER.defaultBlockState();
    private static final BlockState LILY_PAD             = Blocks.LILY_PAD.defaultBlockState();
    private static final BlockState BIRCH_PLANK           = Blocks.BIRCH_PLANKS.defaultBlockState();
    private static final BlockState BIRCH_LOG             = Blocks.BIRCH_LOG.defaultBlockState();
    private static final BlockState BIRCH_STAIRS          = Blocks.BIRCH_STAIRS.defaultBlockState();
    private static final BlockState BIRCH_SLAB            = Blocks.BIRCH_SLAB.defaultBlockState();
    private static final BlockState SPRUCE_PLANK          = Blocks.SPRUCE_PLANKS.defaultBlockState();
    private static final BlockState SPRUCE_LOG           = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState SPRUCE_STAIRS        = Blocks.SPRUCE_STAIRS.defaultBlockState();
    private static final BlockState STONE_BRICK           = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState POLISHED_GRANITE     = Blocks.POLISHED_GRANITE.defaultBlockState();
    private static final BlockState COBBLESTONE           = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState QUARTZ               = Blocks.QUARTZ_BLOCK.defaultBlockState();
    private static final BlockState QUARTZ_PILLAR        = Blocks.QUARTZ_PILLAR.defaultBlockState();
    private static final BlockState QUARTZ_STAIRS        = Blocks.QUARTZ_STAIRS.defaultBlockState();
    private static final BlockState QUARTZ_SLAB           = Blocks.QUARTZ_SLAB.defaultBlockState();
    private static final BlockState IRON_BARS             = Blocks.IRON_BARS.defaultBlockState();
    private static final BlockState LANTERN               = Blocks.LANTERN.defaultBlockState();
    private static final BlockState GLOWSTONE             = Blocks.GLOWSTONE.defaultBlockState();
    private static final BlockState CHEST                 = Blocks.CHEST.defaultBlockState();
    private static final BlockState BOOKSHELF             = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState ANVIL                 = Blocks.ANVIL.defaultBlockState();
    private static final BlockState FURNACE               = Blocks.FURNACE.defaultBlockState();
    private static final BlockState BLAST_FURNACE         = Blocks.BLAST_FURNACE.defaultBlockState();
    private static final BlockState CAULDRON              = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState CRAFTING_TABLE        = Blocks.CRAFTING_TABLE.defaultBlockState();
    private static final BlockState BARREL               = Blocks.BARREL.defaultBlockState();
    private static final BlockState BREWING_STAND        = Blocks.BREWING_STAND.defaultBlockState();
    private static final BlockState GLASS_PANE           = Blocks.GLASS_PANE.defaultBlockState();
    private static final BlockState LIGHT_BLUE_CARPET    = Blocks.LIGHT_BLUE_CARPET.defaultBlockState();
    private static final BlockState CYAN_CARPET          = Blocks.CYAN_CARPET.defaultBlockState();
    private static final BlockState BLUE_CARPET           = Blocks.BLUE_CARPET.defaultBlockState();
    private static final BlockState WHITE_CARPET          = Blocks.WHITE_CARPET.defaultBlockState();
    private static final BlockState RED_BED               = Blocks.RED_BED.defaultBlockState();
    private static final BlockState AIR                   = Blocks.AIR.defaultBlockState();
    private static final BlockState DIRT                  = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRASS                 = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState SAND                  = Blocks.SAND.defaultBlockState();
    private static final BlockState SANDSTONE             = Blocks.SANDSTONE.defaultBlockState();
    // Ergenverse blocks
    private static final BlockState SPIRIT_STONE          = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
    private static final BlockState SPIRIT_STONE_SLAB     = ErgenverseBlocks.SPIRIT_STONE_SLAB.get().defaultBlockState();
    private static final BlockState SPIRIT_STONE_WALL     = ErgenverseBlocks.SPIRIT_STONE_WALL.get().defaultBlockState();
    private static final BlockState SPIRIT_STONE_STAIR    = ErgenverseBlocks.SPIRIT_STONE_STAIRS.get().defaultBlockState();
    private static final BlockState SPIRIT_WOOD_PLANK     = ErgenverseBlocks.SPIRIT_WOOD_PLANKS.get().defaultBlockState();
    private static final BlockState SPIRIT_WOOD_LOG       = ErgenverseBlocks.SPIRIT_WOOD_LOG.get().defaultBlockState();
    private static final BlockState ANCIENT_SPIRIT_LOG    = ErgenverseBlocks.ANCIENT_SPIRIT_LOG.get().defaultBlockState();
    private static final BlockState FORMATION_CORE       = ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState();
    private static final BlockState RESTRICTION_STONE     = ErgenverseBlocks.RESTRICTION_STONE.get().defaultBlockState();
    private static final BlockState JADE_STONE            = ErgenverseBlocks.JADE_STONE.get().defaultBlockState();
    private static final BlockState SPIRIT_DIRT           = ErgenverseBlocks.SPIRIT_DIRT.get().defaultBlockState();
    private static final BlockState SPIRIT_GRASS          = ErgenverseBlocks.SPIRIT_GRASS.get().defaultBlockState();
    private static final BlockState SCORCHED_STONE        = ErgenverseBlocks.SCORCHED_STONE.get().defaultBlockState();

    /**
     * The actual construction body — shared by {@link #build} and {@link #buildForChunk}.
     * Placements flow through {@link #sb} which applies the chunk filter and
     * provenance guard when CURRENT_BOUNDS is set.
     */
    private static void buildInternal(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int baseY = center.getY(); // CRON-72: was hardcoded 64; now canon surface Y
        int cz = center.getZ();

        ChunkBounds bounds = CURRENT_BOUNDS.get();
        if (bounds == null) {
            Ergenverse.LOGGER.info("[LuoHeSect] Building Luo He Sect (full) at ({}, {}, {})...", cx, baseY, cz);
        } else {
            Ergenverse.LOGGER.debug("[LuoHeSect] Building Luo He Sect (chunk-scoped {}) at center ({}, {}, {}).",
                    bounds, cx, baseY, cz);
        }

        // Foundation
        buildFoundation(level, cx, baseY, cz);

        // 1. River Canal (runs through center, build first so other things can bridge it)
        buildRiverCanal(level, cx, baseY, cz);

        // 2. Outer Wall with moat
        buildOuterWall(level, cx, baseY, cz);

        // 3. Entry Bridge
        buildEntryBridge(level, cx, baseY, cz);

        // 4. Outer Gate
        buildOuterGate(level, cx, baseY, cz);

        // 5. Central Courtyard with jade pond
        buildCentralCourtyard(level, cx, baseY, cz);

        // 6. Pill Refinery
        buildPillRefinery(level, cx, baseY, cz);

        // 7. Talisman Workshop
        buildTalismanWorkshop(level, cx, baseY, cz);

        // 8. Library of Flowing Waters
        buildLibrary(level, cx, baseY, cz);

        // 9. Elder Pavilion
        buildElderPavilion(level, cx, baseY, cz);

        // 10. Disciple Dormitories
        buildDiscipleDormitories(level, cx, baseY, cz);

        // 11. Spirit Herb Garden
        buildSpiritHerbGarden(level, cx, baseY, cz);

        // ── Marker block (CRON-72) ─────────────────────────────────────
        // Place a PRISMARINE at center.above(WALL_HEIGHT + 1) — the position
        // isAlreadyBuilt checks. Prior to CRON-72, isAlreadyBuilt checked
        // (SECT_X, 80, SECT_Z) for PRISMARINE, but no PRISMARINE was placed
        // at that exact position during build() — the marker was implicit
        // (any PRISMARINE in the sect would satisfy the check, since PRISMARINE
        // is the dominant wall material). This meant isAlreadyBuilt could
        // return true even if the sect was only partially built (e.g., if a
        // chunk-load built only the outer wall). The new marker is at a
        // specific, stable position (cy + WALL_HEIGHT + 1, directly above the
        // outer wall cap) and is placed at the END of buildInternal — so its
        // presence guarantees the entire build completed.
        sb(level, center.above(WALL_HEIGHT + 1), PRISMARINE, 3);
    }

    // ═══════════════════════════════════════════════════════════════
    //  District Builders
    // ═══════════════════════════════════════════════════════════════

    private static void buildFoundation(ServerLevel level, int cx, int baseY, int cz) {
        fill(level, cx - SECT_RADIUS, baseY - 3, cz - SECT_RADIUS,
                SECT_RADIUS * 2, 3, SECT_RADIUS * 2, Blocks.STONE.defaultBlockState());
        fill(level, cx - SECT_RADIUS, baseY - 1, cz - SECT_RADIUS,
                SECT_RADIUS * 2, 1, SECT_RADIUS * 2, COBBLESTONE);
    }

    private static void buildRiverCanal(ServerLevel level, int cx, int baseY, int cz) {
        int gy = baseY;
        // Main N-S canal through the center: 5 wide, 3 deep
        fill(level, cx - 2, gy - 4, cz - SECT_RADIUS + 5, 5, 3, SECT_RADIUS * 2 - 10, WATER);
        // Prismarine canal walls
        fill(level, cx - 3, gy - 4, cz - SECT_RADIUS + 5, 1, 4, SECT_RADIUS * 2 - 10, PRISMARINE);
        fill(level, cx + 3, gy - 4, cz - SECT_RADIUS + 5, 1, 4, SECT_RADIUS * 2 - 10, PRISMARINE);
        // Canal floor
        fill(level, cx - 2, gy - 5, cz - SECT_RADIUS + 5, 5, 1, SECT_RADIUS * 2 - 10, PRISMARINE_BRICKS);
        // Lily pads scattered in canal
        for (int z = cz - SECT_RADIUS + 10; z < cz + SECT_RADIUS - 10; z += 7) {
            set(level, cx - 1, gy - 1, z, LILY_PAD);
            set(level, cx + 2, gy - 1, z + 3, LILY_PAD);
        }
        // Sea lanterns at canal bottom for underwater glow
        for (int z = cz - SECT_RADIUS + 8; z < cz + SECT_RADIUS - 8; z += 10) {
            set(level, cx, gy - 5, z, SEA_LANTERN);
        }
    }

    private static void buildOuterWall(ServerLevel level, int cx, int baseY, int cz) {
        int r = SECT_RADIUS;
        int gy = baseY;
        // Prismarine walls (skip canal area in center south)
        fill(level, cx - r, gy, cz - r, r * 2, WALL_HEIGHT, 3, PRISMARINE_BRICKS);
        fill(level, cx - r, gy, cz + r - 3, r * 2, WALL_HEIGHT, 3, PRISMARINE_BRICKS);
        fill(level, cx - r, gy, cz - r, 3, WALL_HEIGHT, r * 2, PRISMARINE_BRICKS);
        fill(level, cx + r - 3, gy, cz - r, 3, WALL_HEIGHT, r * 2, PRISMARINE_BRICKS);
        // Clear interior
        fill(level, cx - r + 3, gy, cz - r + 3, r * 2 - 6, 1, r * 2 - 6, AIR);
        // Moat (water outside walls)
        fill(level, cx - r - 2, gy - 1, cz - r - 2, r * 2 + 5, 2, 2, WATER);
        fill(level, cx - r - 2, gy - 1, cz + r, r * 2 + 5, 2, 2, WATER);
        fill(level, cx - r - 2, gy - 1, cz - r, 2, 2, r * 2 + 5, WATER);
        fill(level, cx + r, gy - 1, cz - r, 2, 2, r * 2 + 5, WATER);
        // Prismarine cap on walls
        fill(level, cx - r, gy + WALL_HEIGHT, cz - r, r * 2, 1, 3, DARK_PRISMARINE);
        fill(level, cx - r, gy + WALL_HEIGHT, cz + r - 3, r * 2, 1, 3, DARK_PRISMARINE);
        fill(level, cx - r, gy + WALL_HEIGHT, cz - r, 3, 1, r * 2, DARK_PRISMARINE);
        fill(level, cx + r - 3, gy + WALL_HEIGHT, cz - r, 3, 1, r * 2, DARK_PRISMARINE);
        // Sea lanterns on wall tops
        for (int i = -r + 5; i < r - 5; i += 8) {
            set(level, cx + i, gy + WALL_HEIGHT + 1, cz - r + 1, SEA_LANTERN);
            set(level, cx + i, gy + WALL_HEIGHT + 1, cz + r - 2, SEA_LANTERN);
            set(level, cx - r + 1, gy + WALL_HEIGHT + 1, cz + i, SEA_LANTERN);
            set(level, cx + r - 2, gy + WALL_HEIGHT + 1, cz + i, SEA_LANTERN);
        }
    }

    private static void buildEntryBridge(ServerLevel level, int cx, int baseY, int cz) {
        int gy = baseY;
        int gz = cz + SECT_RADIUS - 3;
        // Stone arch bridge over moat
        fill(level, cx - 3, gy, gz, 7, 1, 5, STONE_BRICK);
        // Bridge railings
        fill(level, cx - 3, gy + 1, gz, 1, 2, 5, STONE_BRICK);
        fill(level, cx + 3, gy + 1, gz, 1, 2, 5, STONE_BRICK);
        // Clear water under bridge
        fill(level, cx - 2, gy, gz, 5, 1, 5, AIR);
        // Bridge connects to gate
        fill(level, cx - 3, gy, gz + 4, 7, 1, 3, COBBLESTONE);
    }

    private static void buildOuterGate(ServerLevel level, int cx, int baseY, int cz) {
        int gz = cz + SECT_RADIUS + 2;
        int gy = baseY;
        // Gate pillars (prismarine)
        fill(level, cx - 4, gy, gz, 2, WALL_HEIGHT, 3, PRISMARINE);
        fill(level, cx + 3, gy, gz, 2, WALL_HEIGHT, 3, PRISMARINE);
        // Gate arch
        fill(level, cx - 5, gy + WALL_HEIGHT, gz - 1, 11, 2, 5, PRISMARINE);
        // Gate opening
        fill(level, cx - 2, gy, gz, 5, WALL_HEIGHT, 3, AIR);
        // Iron bar gates
        fill(level, cx - 2, gy + 2, gz, 2, WALL_HEIGHT - 4, 1, IRON_BARS);
        fill(level, cx + 1, gy + 2, gz, 2, WALL_HEIGHT - 4, 1, IRON_BARS);
        // Jade stone plaque above gate (sect name marker)
        fill(level, cx - 2, gy + WALL_HEIGHT - 1, gz - 1, 5, 1, 1, JADE_STONE);
        // Lanterns
        set(level, cx - 3, gy + WALL_HEIGHT - 2, gz, SEA_LANTERN);
        set(level, cx + 4, gy + WALL_HEIGHT - 2, gz, SEA_LANTERN);
    }

    private static void buildCentralCourtyard(ServerLevel level, int cx, int baseY, int cz) {
        int gy = baseY;
        // 16x16 polished granite plaza (split by canal)
        fill(level, cx - 10, gy - 1, cz - 8, 7, 1, 17, POLISHED_GRANITE);
        fill(level, cx + 4, gy - 1, cz - 8, 7, 1, 17, POLISHED_GRANITE);
        // Jade pond (west of canal)
        fill(level, cx - 8, gy, cz - 5, 5, 1, 5, WATER);
        // Pond border: quartz
        fill(level, cx - 9, gy, cz - 6, 7, 1, 1, QUARTZ);
        fill(level, cx - 9, gy, cz, 7, 1, 1, QUARTZ);
        fill(level, cx - 9, gy, cz - 6, 1, 1, 6, QUARTZ);
        fill(level, cx - 3, gy, cz - 6, 1, 1, 6, QUARTZ);
        // Lily pads in jade pond
        set(level, cx - 7, gy, cz - 4, LILY_PAD);
        set(level, cx - 5, gy, cz - 3, LILY_PAD);
        set(level, cx - 6, gy, cz - 2, LILY_PAD);
        // Waterfall feature (water source column dropping into pond)
        fill(level, cx - 8, gy + 1, cz - 3, 1, 4, 1, WATER);
        // Quartz pillar behind waterfall
        set(level, cx - 9, gy + 1, cz - 3, QUARTZ_PILLAR);
        set(level, cx - 9, gy + 2, cz - 3, QUARTZ_PILLAR);
        set(level, cx - 9, gy + 3, cz - 3, QUARTZ_PILLAR);
        set(level, cx - 9, gy + 4, cz - 3, QUARTZ_PILLAR);
        // Cyan carpet paths
        fill(level, cx - 5, gy, cz - 7, 5, 1, 2, CYAN_CARPET);
        fill(level, cx + 1, gy, cz - 7, 5, 1, 2, CYAN_CARPET);
    }

    private static void buildPillRefinery(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx + 6;
        int gz = cz - 18;
        int gy = baseY;
        // Floor
        fill(level, gx - 5, gy - 1, gz - 4, 11, 1, 9, POLISHED_GRANITE);
        // Walls: prismarine
        fill(level, gx - 6, gy, gz - 4, 1, 5, 9, PRISMARINE);
        fill(level, gx + 5, gy, gz - 4, 1, 5, 9, PRISMARINE);
        fill(level, gx - 6, gy, gz - 4, 11, 5, 1, PRISMARINE);
        fill(level, gx - 6, gy, gz + 4, 11, 5, 1, PRISMARINE);
        // Interior
        fill(level, gx - 5, gy, gz - 3, 10, 5, 7, AIR);
        // Brewing stands (3 — pill refinement)
        fill(level, gx - 3, gy, gz - 2, 1, 2, 1, BREWING_STAND);
        set(level, gx - 1, gy, gz - 2, BREWING_STAND);
        set(level, gx + 1, gy, gz - 2, BREWING_STAND);
        // Cauldrons (3 — mixing)
        fill(level, gx - 3, gy, gz + 1, 1, 1, 1, CAULDRON);
        set(level, gx - 1, gy, gz + 1, CAULDRON);
        set(level, gx + 1, gy, gz + 1, CAULDRON);
        // Blast furnace (pill calcination)
        set(level, gx + 3, gy, gz - 2, BLAST_FURNACE);
        // Chests for ingredients
        set(level, gx - 3, gy, gz + 3, CHEST);
        set(level, gx - 1, gy, gz + 3, CHEST);
        set(level, gx + 3, gy, gz + 3, BARREL);
        // Water channel through refinery (small)
        fill(level, gx - 5, gy, gz, 1, 1, 1, WATER);
        set(level, gx - 5, gy, gz + 2, WATER);
        // Lantern
        set(level, gx, gy + 4, gz, SEA_LANTERN);
        // Roof
        fill(level, gx - 7, gy + 5, gz - 5, 13, 1, 11, BIRCH_STAIRS);
        fill(level, gx - 6, gy + 6, gz - 4, 11, 1, 9, BIRCH_STAIRS);
    }

    private static void buildTalismanWorkshop(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx - 18;
        int gz = cz - 18;
        int gy = baseY;
        // Floor
        fill(level, gx - 5, gy - 1, gz - 4, 11, 1, 9, COBBLESTONE);
        // Walls: stone brick
        fill(level, gx - 6, gy, gz - 4, 1, 4, 9, STONE_BRICK);
        fill(level, gx + 5, gy, gz - 4, 1, 4, 9, STONE_BRICK);
        fill(level, gx - 6, gy, gz - 4, 11, 4, 1, STONE_BRICK);
        fill(level, gx - 6, gy, gz + 4, 11, 4, 1, STONE_BRICK);
        // Interior
        fill(level, gx - 5, gy, gz - 3, 10, 4, 7, AIR);
        // Anvils (inscription)
        fill(level, gx - 3, gy, gz - 2, 2, 1, 1, ANVIL);
        set(level, gx + 2, gy, gz - 2, ANVIL);
        // Crafting tables
        set(level, gx - 3, gy, gz + 1, CRAFTING_TABLE);
        set(level, gx + 2, gy, gz + 1, CRAFTING_TABLE);
        // Furnace (talisman firing)
        set(level, gx + 3, gy, gz - 2, FURNACE);
        // Chests for materials
        set(level, gx - 3, gy, gz + 3, CHEST);
        set(level, gx + 2, gy, gz + 3, CHEST);
        // Formation core (inscription focus)
        set(level, gx, gy + 1, gz, FORMATION_CORE);
        // Lantern
        set(level, gx, gy + 3, gz, LANTERN);
        // Roof
        fill(level, gx - 7, gy + 4, gz - 5, 13, 1, 11, SPRUCE_STAIRS);
    }

    private static void buildLibrary(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx - 18;
        int gz = cz - 8;
        int gy = baseY;
        // Floor
        fill(level, gx - 5, gy - 1, gz - 5, 11, 1, 11, BIRCH_PLANK);
        // Walls: birch + quartz
        fill(level, gx - 6, gy, gz - 5, 1, 5, 11, BIRCH_PLANK);
        fill(level, gx + 5, gy, gz - 5, 1, 5, 11, BIRCH_PLANK);
        fill(level, gx - 6, gy, gz - 5, 11, 5, 1, QUARTZ);
        fill(level, gx - 6, gy, gz + 5, 11, 5, 1, QUARTZ);
        // Interior
        fill(level, gx - 5, gy, gz - 4, 10, 5, 9, AIR);
        // Bookshelves lining walls (3 per side)
        fill(level, gx - 5, gy, gz - 3, 1, 3, 7, BOOKSHELF);
        fill(level, gx + 4, gy, gz - 3, 1, 3, 7, BOOKSHELF);
        fill(level, gx - 3, gy, gz - 4, 7, 3, 1, BOOKSHELF);
        // Lecterns (reading)
        set(level, gx - 2, gy, gz, Blocks.LECTERN.defaultBlockState());
        set(level, gx + 2, gy, gz, Blocks.LECTERN.defaultBlockState());
        // Blue carpet (water motif)
        fill(level, gx - 4, gy, gz - 3, 9, 1, 7, BLUE_CARPET);
        // Chests (scroll storage)
        set(level, gx - 4, gy, gz + 3, CHEST);
        set(level, gx + 3, gy, gz + 3, CHEST);
        // Sea lanterns
        set(level, gx, gy + 4, gz - 3, SEA_LANTERN);
        set(level, gx, gy + 4, gz + 3, SEA_LANTERN);
        // Roof
        fill(level, gx - 7, gy + 5, gz - 6, 13, 1, 13, BIRCH_STAIRS);
        fill(level, gx - 6, gy + 6, gz - 5, 11, 1, 11, BIRCH_STAIRS);
    }

    private static void buildElderPavilion(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx + 6;
        int gz = cz + 10;
        int gy = baseY;
        // Raised platform: quartz
        fill(level, gx - 6, gy - 1, gz - 5, 13, 2, 11, QUARTZ);
        // Pavilion floor
        fill(level, gx - 5, gy + 1, gz - 4, 11, 1, 9, POLISHED_GRANITE);
        // 4 corner pillars
        fill(level, gx - 5, gy + 1, gz - 4, 1, 5, 1, QUARTZ_PILLAR);
        fill(level, gx + 5, gy + 1, gz - 4, 1, 5, 1, QUARTZ_PILLAR);
        fill(level, gx - 5, gy + 1, gz + 4, 1, 5, 1, QUARTZ_PILLAR);
        fill(level, gx + 5, gy + 1, gz + 4, 1, 5, 1, QUARTZ_PILLAR);
        // Pillar caps
        set(level, gx - 5, gy + 6, gz - 4, QUARTZ);
        set(level, gx + 5, gy + 6, gz - 4, QUARTZ);
        set(level, gx - 5, gy + 6, gz + 4, QUARTZ);
        set(level, gx + 5, gy + 6, gz + 4, QUARTZ);
        // Pyramidal roof
        fill(level, gx - 6, gy + 7, gz - 5, 13, 1, 11, BIRCH_STAIRS);
        fill(level, gx - 5, gy + 8, gz - 4, 11, 1, 9, BIRCH_STAIRS);
        fill(level, gx - 4, gy + 9, gz - 3, 9, 1, 7, BIRCH_STAIRS);
        fill(level, gx - 3, gy + 10, gz - 2, 7, 1, 5, BIRCH_STAIRS);
        fill(level, gx - 2, gy + 11, gz - 1, 5, 1, 3, BIRCH_STAIRS);
        set(level, gx, gy + 12, gz, SEA_LANTERN);
        // Meeting table inside
        fill(level, gx - 2, gy + 1, gz, 5, 1, 1, BIRCH_PLANK);
        // Chairs (stairs as seats)
        set(level, gx - 3, gy + 1, gz - 1, BIRCH_STAIRS);
        set(level, gx + 3, gy + 1, gz - 1, BIRCH_STAIRS);
        set(level, gx - 3, gy + 1, gz + 2, BIRCH_STAIRS);
        set(level, gx + 3, gy + 1, gz + 2, BIRCH_STAIRS);
        // Elder chest (sect treasury access)
        set(level, gx + 3, gy + 1, gz - 3, CHEST);
        // Steps up to pavilion (south)
        fill(level, gx - 2, gy, gz + 5, 5, 1, 2, QUARTZ_SLAB);
        // Light blue carpet inside
        fill(level, gx - 4, gy + 1, gz - 3, 9, 1, 7, LIGHT_BLUE_CARPET);
    }

    private static void buildDiscipleDormitories(ServerLevel level, int cx, int baseY, int cz) {
        int gy = baseY;
        // 6 rooms along the canal (3 per side)
        // West side
        for (int i = 0; i < 3; i++) {
            int rx = cx - 15;
            int rz = cz - 12 + i * 6;
            // Floor
            fill(level, rx - 2, gy - 1, rz - 2, 5, 1, 5, COBBLESTONE);
            // Walls: birch
            fill(level, rx - 3, gy, rz - 2, 1, 4, 5, BIRCH_PLANK);
            fill(level, rx + 2, gy, rz - 2, 1, 4, 5, BIRCH_PLANK);
            fill(level, rx - 3, gy, rz - 2, 5, 4, 1, BIRCH_PLANK);
            fill(level, rx - 3, gy, rz + 2, 5, 4, 1, BIRCH_PLANK);
            // Interior
            fill(level, rx - 2, gy, rz - 1, 4, 4, 3, AIR);
            // Bed
            set(level, rx - 1, gy, rz + 1, RED_BED);
            // Chest
            set(level, rx + 1, gy, rz + 1, CHEST);
            // Window facing canal (glass pane)
            fill(level, rx + 2, gy + 1, rz, 1, 1, 1, GLASS_PANE);
            // Roof
            fill(level, rx - 3, gy + 4, rz - 3, 7, 1, 7, BIRCH_STAIRS);
        }
        // East side (mirrored)
        for (int i = 0; i < 3; i++) {
            int rx = cx + 10;
            int rz = cz - 12 + i * 6;
            fill(level, rx - 2, gy - 1, rz - 2, 5, 1, 5, COBBLESTONE);
            fill(level, rx - 3, gy, rz - 2, 1, 4, 5, BIRCH_PLANK);
            fill(level, rx + 2, gy, rz - 2, 1, 4, 5, BIRCH_PLANK);
            fill(level, rx - 3, gy, rz - 2, 5, 4, 1, BIRCH_PLANK);
            fill(level, rx - 3, gy, rz + 2, 5, 4, 1, BIRCH_PLANK);
            fill(level, rx - 2, gy, rz - 1, 4, 4, 3, AIR);
            set(level, rx - 1, gy, rz + 1, RED_BED);
            set(level, rx + 1, gy, rz + 1, CHEST);
            fill(level, rx - 3, gy + 1, rz, 1, 1, 1, GLASS_PANE);
            fill(level, rx - 3, gy + 4, rz - 3, 7, 1, 7, BIRCH_STAIRS);
        }
    }

    private static void buildSpiritHerbGarden(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx - 15;
        int gz = cz + 12;
        int gy = baseY;
        // Garden floor: dirt
        fill(level, gx - 6, gy - 1, gz - 4, 13, 1, 9, DIRT);
        // Water channel through garden
        fill(level, gx, gy, gz - 3, 1, 1, 7, WATER);
        // Herb beds (dirt strips)
        fill(level, gx - 5, gy, gz - 2, 4, 1, 2, DIRT);
        fill(level, gx - 5, gy, gz + 2, 4, 1, 2, DIRT);
        fill(level, gx + 2, gy, gz - 2, 4, 1, 2, DIRT);
        fill(level, gx + 2, gy, gz + 2, 4, 1, 2, DIRT);
        // Spirit herbs
        set(level, gx - 4, gy, gz - 1, Blocks.AZALEA.defaultBlockState());
        set(level, gx - 2, gy, gz - 1, Blocks.BLUE_ORCHID.defaultBlockState());
        set(level, gx + 3, gy, gz - 1, Blocks.FERN.defaultBlockState());
        set(level, gx + 5, gy, gz - 1, Blocks.ALLIUM.defaultBlockState());
        set(level, gx - 4, gy, gz + 3, Blocks.AZALEA.defaultBlockState());
        set(level, gx - 2, gy, gz + 3, Blocks.FERN.defaultBlockState());
        set(level, gx + 3, gy, gz + 3, Blocks.BLUE_ORCHID.defaultBlockState());
        set(level, gx + 5, gy, gz + 3, Blocks.ROSE_BUSH.defaultBlockState());
        // Lily pads in water channel
        set(level, gx, gy, gz - 2, LILY_PAD);
        set(level, gx, gy, gz + 1, LILY_PAD);
        set(level, gx, gy, gz + 3, LILY_PAD);
        // Stone border
        fill(level, gx - 7, gy, gz - 4, 1, 1, 9, STONE_BRICK);
        fill(level, gx + 7, gy, gz - 4, 1, 1, 9, STONE_BRICK);
        fill(level, gx - 7, gy, gz - 4, 14, 1, 1, STONE_BRICK);
        fill(level, gx - 7, gy, gz + 4, 14, 1, 1, STONE_BRICK);
        // Lectern (herb identification)
        set(level, gx - 5, gy, gz, Blocks.LECTERN.defaultBlockState());
        // Cauldron (herb preparation)
        set(level, gx + 5, gy, gz, CAULDRON);
        // Chest (harvested herbs)
        set(level, gx + 5, gy, gz + 3, CHEST);
    }

    // ── Helper methods ────────────────────────────────────────────────

    /**
     * Single-block placement helper — delegates to {@link #sb} so all setBlock
     * traffic flows through the chunk filter and provenance guard. CRON-72:
     * prior to this round, set() called level.setBlock directly, bypassing
     * both guards. Now this is the ONLY non-sb setBlock call site, and it
     * delegates to sb.
     */
    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        sb(level, new BlockPos(x, y, z), state, 3);
    }

    private static void fill(ServerLevel level, int x, int y, int z,
                            int dx, int dy, int dz, BlockState state) {
        for (int ix = 0; ix < dx; ix++) {
            for (int iy = 0; iy < dy; iy++) {
                for (int iz = 0; iz < dz; iz++) {
                    set(level, x + ix, y + iy, z + iz, state);
                }
            }
        }
    }
}
