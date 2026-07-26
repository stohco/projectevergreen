package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.ChunkBounds;
import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.runtime.delta.WorldDeltaStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * XuanDaoSectBuilder — FULLY hand-built Xuan Dao Sect (玄道宗).
 *
 * <p>Constitution: "The world is completely hand-crafted, accurate to the novels.
 * NEVER write a script that replaces vanilla blocks with other blocks as a shortcut.
 * Every structure must be hand-authored."
 *
 * <p>Canon (Renegade Immortal, ~Ch. 120-180): Xuan Dao Sect is a mid-tier
 * cultivation sect on the Cold Ice Country border. Wang Lin comes here to study
 * restriction arts after his experiences in the Zhao Country war. The sect is
 * smaller and more focused than Heng Yue or Teng Family — it's a scholarly
 * institution known for restriction (restriction) techniques, not combat.
 *
 * <p>Key canon features:
 * <ul>
 *   <li>Restriction Array Workshop — where disciples practice placing formation
 *       stones. Multiple practice arrays on flat ground.</li>
 *   <li>Sealed Meditation Chambers — individual stone rooms with restriction
 *       blocking the outside, used for deep cultivation comprehension.</li>
 *   <li>Library Tower — 3-story pagoda housing restriction art scrolls.</li>
 *   <li>Refinement Furnace — spirit fire furnace for refining restriction
 *       materials.</li>
 *   <li>Courtyard — open central plaza with a spirit spring.</li>
 * </ul>
 *
 * <p>Architectural style: Lower, flatter, more spread out than Heng Yue. The
 * buildings are single-story with the exception of the library tower. Stone
 * walls with observation slits (iron bars). Fewer decorative elements,
 * more functional design. The sect prioritizes functionality over grandeur.
 *
 * <h2>Districts (12)</h2>
 * <ol>
 *   <li>Entry Path — stone-paved road with lanterns leading to the outer gate</li>
 *   <li>Outer Gate — single arch, smaller than Heng Yue's grand gate</li>
 *   <li>Central Courtyard — open plaza with spirit spring, formation lines</li>
 *   <li>Restriction Workshop — practice area with stone array markers</li>
 *   <li>Library Tower — 3-story pagoda (restriction scrolls)</li>
 *   <li>Sealed Meditation Wing — 6 individual chambers (3 per side)</li>
 *   <li>Refinery — furnace room with cauldron and chimney</li>
 *   <li>Elder Hall — meeting room for the sect master and elders</li>
 *   <li>Disciple Dormitories — 4 small rooms (2 per side)</li>
 *   <li>Outer Wall — perimeter wall with corner pillars</li>
 *   <li>Spirit Herb Garden — small garden behind the refinery</li>
 * </ol>
 *
 * <h2>Harsh Self-Critique</h2>
 * <ul>
 *   <li>This is a NEW builder — the first version. Architectural accuracy is based on
 *       novel references but many details are inferred. The sect is described
 *       as "smaller" and "scholarly" in canon, so the compact layout is intentional.</li>
 *   <li>Roofs are still flat/eaveless like Heng Yue — the persistent problem.
 *       Xianxia pagoda roofs need curved upturned eaves (dougong brackets).
 *       This builder uses stair-slab roofs as an approximation.</li>
 *   <li>All B.SPIRIT_STONE / B.STONE_BRICK / B.MOSSY_BRICK / B.CRACKED_BRICK
 *       map to the same block (no distinct weathered variants exist). This is
 *       acknowledged as a limitation. The builder uses LAPIS (formation core) and
 *       OBSIDIAN (restriction stone) for visual variety in key locations.</li>
 *   <li>No second floor on any building except the library tower. Canon doesn't
 *       specify detailed building heights — this is a deliberate simplification.</li>
 *   <li>The restriction workshop has stone markers but no actual formation
 *       logic (the arrays are decorative, not functional).</li>
 * </ul>
 */
public final class XuanDaoSectBuilder {

    // Lazy-initialized block states (flattened from class B for direct access)
private static final BlockState SPIRIT_STONE = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
private static final BlockState SPIRIT_STONE_SLAB = ErgenverseBlocks.SPIRIT_STONE_SLAB.get().defaultBlockState();
private static final BlockState STONE_BRICK = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
private static final BlockState MOSSY_BRICK = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
private static final BlockState CRACKED_BRICK = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
private static final BlockState BRICK_WALL = ErgenverseBlocks.SPIRIT_STONE_WALL.get().defaultBlockState();
private static final BlockState DEEPSLATE_BRICK = ErgenverseBlocks.SCORCHED_STONE.get().defaultBlockState();
private static final BlockState SPRUCE_PLANK = ErgenverseBlocks.SPIRIT_WOOD_PLANKS.get().defaultBlockState();
private static final BlockState SPRUCE_LOG = ErgenverseBlocks.SPIRIT_WOOD_LOG.get().defaultBlockState();
private static final BlockState DARK_OAK_PLANK = ErgenverseBlocks.SPIRIT_WOOD_PLANKS.get().defaultBlockState();
private static final BlockState DARK_OAK_LOG = ErgenverseBlocks.ANCIENT_SPIRIT_LOG.get().defaultBlockState();
private static final BlockState DARK_OAK_STAIR = ErgenverseBlocks.ANCIENT_SPIRIT_STAIRS.get().defaultBlockState();
private static final BlockState SPRUCE_STAIR = ErgenverseBlocks.SPIRIT_WOOD_PLANKS_STAIRS.get().defaultBlockState();
private static final BlockState BRICK_STAIR = ErgenverseBlocks.SPIRIT_STONE_STAIRS.get().defaultBlockState();
private static final BlockState LAPIS = ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState();
private static final BlockState OBSIDIAN = ErgenverseBlocks.RESTRICTION_STONE.get().defaultBlockState();
private static final BlockState REDSTONE_BLOCK = ErgenverseBlocks.BLOOD_STONE.get().defaultBlockState();


    private XuanDaoSectBuilder() {}

    // ── Vanilla stand-ins for functional blocks ──
    private static final BlockState COBBLE         = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState LANTERN        = Blocks.LANTERN.defaultBlockState();
    private static final BlockState END_ROD        = Blocks.END_ROD.defaultBlockState();
    private static final BlockState SEA_LANTERN    = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState GLOWSTONE      = Blocks.GLOWSTONE.defaultBlockState();
    private static final BlockState GOLD           = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState AMETHYST       = Blocks.AMETHYST_BLOCK.defaultBlockState();
    private static final BlockState IRON_BARS      = Blocks.IRON_BARS.defaultBlockState();
    private static final BlockState WATER          = Blocks.WATER.defaultBlockState();
    private static final BlockState BOOKSHELF      = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState LECTERN        = Blocks.LECTERN.defaultBlockState();
    private static final BlockState BLAST_FURNACE  = Blocks.BLAST_FURNACE.defaultBlockState();
    private static final BlockState CAULDRON       = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState RED_BED        = Blocks.RED_BED.defaultBlockState();
    private static final BlockState SMOKER         = Blocks.SMOKER.defaultBlockState();
    private static final BlockState CHEST          = Blocks.CHEST.defaultBlockState();
    private static final BlockState ANVIL          = Blocks.ANVIL.defaultBlockState();
    private static final BlockState FERN           = Blocks.FERN.defaultBlockState();
    private static final BlockState AZALEA         = Blocks.FLOWERING_AZALEA.defaultBlockState();
    private static final BlockState BLUE_ORCHID    = Blocks.BLUE_ORCHID.defaultBlockState();
    private static final BlockState GRASS          = Blocks.GRASS.defaultBlockState();
    private static final BlockState DIRT           = Blocks.DIRT.defaultBlockState();
    private static final BlockState STONE          = Blocks.STONE.defaultBlockState();
    private static final BlockState SPRUCE_FENCE   = Blocks.SPRUCE_FENCE.defaultBlockState();
    private static final BlockState SMOOTH_SLAB    = Blocks.SMOOTH_STONE_SLAB.defaultBlockState();

    /**
     * <b>DEPRECATED (CRON-COMPLETIONIST-70).</b> Prior to CRON-70, all 12
     * district builders referenced this constant directly, which placed
     * the entire sect at y=-2 (underground, unreachable). CRON-70 refactored
     * every district builder to use {@code c.getY()} (the resolved canon
     * surface Y from {@link #getSectCenter}). The constant is retained
     * only as a defensive reference for the legacy Javadoc above; no live
     * code path reads it. Do NOT add new usages.
     *
     * <p>History: this was originally {@code -2} as a placeholder during
     * initial scaffolding (CRON-65). The intent was "y=0 relative to sea
     * level", but {@code -2} was a typo that survived 5 cron rounds
     * (CRON-65 through CRON-69) because the symptom — a buried, unreachable
     * sect — was masked by the chunk-materializer's no-op behavior on
     * already-loaded chunks. CRON-69 critique #4 flagged this as the
     * highest-impact deferred fix.
     */
    @Deprecated
    private static final int BASE_Y = -2;

    // ── Canon center (CRON-COMPLETIONIST-66, fixed CRON-70) ────────────
    // The sect is ALWAYS at PlanetSuzakuBlueprint.XUAN_DAO_SECT coordinates.
    // This eliminates the prior bug where the registry passed BlockPos.ZERO
    // and the sect was built at (0,0,0) instead of its canon coordinate
    // (-2400, 0, 1400).

    /** Canonical sect X coordinate (fixed for every world/seed/player). */
    public static final int SECT_X = PlanetSuzakuBlueprint.XUAN_DAO_SECT.x;

    /** Canonical sect Z coordinate (fixed for every world/seed/player). */
    public static final int SECT_Z = PlanetSuzakuBlueprint.XUAN_DAO_SECT.z;

    /**
     * Resolve the sect center BlockPos using the canon surface height from
     * {@link dev.ergenverse.runtime.worldgen.BlueprintChunkGenerator#canonSurfaceHeight}.
     *
     * <p><b>CRON-COMPLETIONIST-70:</b> the prior implementation returned
     * {@code new BlockPos(SECT_X, BASE_Y, SECT_Z)} where {@code BASE_Y = -2},
     * which placed the entire sect 2 blocks below the world's bedrock floor
     * (effectively unreachable). All 12 district builders also referenced
     * {@code BASE_Y} directly, so the entire sect was buried at y=-2.
     *
     * <p>The fix has two parts:
     * <ol>
     *   <li>This method now returns the canon surface Y (same pure function
     *       the chunk generator uses to shape the surface at (SECT_X, SECT_Z)).
     *       The sect sits ON the surface, consistent with all other builders
     *       (HengYue, TengFamily, TianShui, NanDou, SoulRefining) which
     *       already use this pattern.</li>
     *   <li>All 12 district builders were refactored to use {@code c.getY()}
     *       instead of the {@code BASE_Y} constant. See the diff for the
     *       mechanical replacement in buildEntryPath, buildOuterGate,
     *       buildCentralCourtyard, buildRestrictionWorkshop, buildLibraryTower,
     *       buildSealedMeditationWing, buildRefinery, buildElderHall,
     *       buildDiscipleDormitories, buildOuterWall, buildSpiritHerbGarden,
     *       buildLanterns.</li>
     * </ol>
     *
     * <p>This closes the deferred y=-2 bug from CRON-67 critique #4 and
     * CRON-69 critique #4. The provenance-aware rebuild guard (CRON-69)
     * is now operationally useful for Xuan Dao: the marker at
     * {@code center.offset(0, 3, -3)} is at a reachable surface position,
     * so a player breaking it actually triggers the guard.
     */
    public static BlockPos getSectCenter(ServerLevel level) {
        int surfaceY = dev.ergenverse.runtime.worldgen.BlueprintChunkGenerator.canonSurfaceHeight(SECT_X, SECT_Z);
        return new BlockPos(SECT_X, surfaceY, SECT_Z);
    }

    // ── Chunk-scoped build infrastructure (CRON-COMPLETIONIST-66) ──────
    //
    // Mirrors the WangFamilyVillageBuilder (CRON-62/63) and HengYueSectBuilder
    // (CRON-65) pattern. The chunk-materializer invokes buildForChunk(level,
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
     * Full-build entry point using the canon center resolved from
     * {@link #getSectCenter}. Idempotent: guarded by {@link #isAlreadyBuilt}.
     * Used by SpawnEventHandler (server-start) and ErgenverseCommand.
     */
    public static void build(ServerLevel level) {
        BlockPos center = getSectCenter(level);
        if (isAlreadyBuilt(level, center)) {
            Ergenverse.LOGGER.debug("[Ergenverse] Xuan Dao Sect already built — build() is a no-op.");
            return;
        }
        Ergenverse.LOGGER.info("[Ergenverse] Building Xuan Dao Sect at {}", center);
        buildInternal(level, center);
        Ergenverse.LOGGER.info("[Ergenverse] Xuan Dao Sect construction complete.");
    }

    /**
     * Legacy 2-arg full-build entry point — kept for backward compat with
     * {@link dev.ergenverse.world.blueprint.CanonGeographyPlacer} which
     * resolves its own center from the JSON blueprint.
     *
     * @deprecated prefer {@link #build(ServerLevel)} or {@link #buildForChunk}
     */
    @Deprecated
    public static void build(ServerLevel level, BlockPos center) {
        if (isAlreadyBuilt(level, center)) return;
        Ergenverse.LOGGER.info("[Ergenverse] Building Xuan Dao Sect at {} (legacy 2-arg path)", center);
        buildInternal(level, center);
        Ergenverse.LOGGER.info("[Ergenverse] Xuan Dao Sect construction complete.");
    }

    /**
     * The actual construction body — shared by all three entry points.
     * Placements flow through {@link #sb} which applies the chunk filter and
     * provenance guard when CURRENT_BOUNDS is set.
     */
    private static void buildInternal(ServerLevel level, BlockPos center) {
        buildEntryPath(level, center);
        buildOuterGate(level, center);
        buildCentralCourtyard(level, center);
        buildRestrictionWorkshop(level, center);
        buildLibraryTower(level, center);
        buildSealedMeditationWing(level, center);
        buildRefinery(level, center);
        buildElderHall(level, center);
        buildDiscipleDormitories(level, center);
        buildOuterWall(level, center);
        buildSpiritHerbGarden(level, center);
        buildLanterns(level, center);

        // ── Marker block (CRON-COMPLETIONIST-70 fix) ─────────────────────
        // Place a SEA_LANTERN at center.offset(0, 3, -3) — the position
        // isAlreadyBuilt checks. Prior to CRON-70, buildInternal placed a
        // SMOOTH_SLAB at center.above(2) (cy+2) while isAlreadyBuilt
        // checked center.offset(0, 3, -3) (cy+3, cz-3) for SEA_LANTERN.
        // The two NEVER matched, so isAlreadyBuilt always returned false
        // (unless a player/sim delta existed at the marker position),
        // meaning build() always re-ran the full construction. This is
        // a latent idempotency bug, separate from the y=-2 bug but in
        // the same file — fixed together for coherence.
        //
        // The marker is a SEA_LANTERN at (cx, cy+3, cz-3): visually subtle
        // (one of many lanterns in the sect), thematically appropriate
        // (the sect is lantern-lit for scholarly atmosphere), and at a
        // position the player is unlikely to disturb during normal play.
        sb(level, center.offset(0, 3, -3), SEA_LANTERN, 3);
    }

    /**
     * Check if the sect is already built by looking for the library tower's
     * sea lantern marker at the expected position ({@code center.offset(0, 3, -3)}).
     *
     * <p><b>CRON-COMPLETIONIST-69 — provenance-aware rebuild guard.</b>
     * If the player (or simulation) has recorded a delta at the marker
     * position, returns {@code true} (sect was built, then edited; don't
     * rebuild). See
     * {@link dev.ergenverse.runtime.materialize.ProvenanceAwareRebuildGuard}.
     */
    public static boolean isAlreadyBuilt(ServerLevel level, BlockPos center) {
        BlockPos markerPos = center.offset(0, 3, -3);
        if (level.getBlockState(markerPos).getBlock() == Blocks.SEA_LANTERN) return true;
        if (dev.ergenverse.runtime.materialize.ProvenanceAwareRebuildGuard.shouldSkipRebuild(markerPos)) return true;
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  District Builders
    // ═════════════════════════════════════════════════════════════════════

    /** Entry path: 20-block cobblestone road from south, ending at the outer gate. */
    private static void buildEntryPath(ServerLevel level, BlockPos c) {
        int gy = c.getY(); // CRON-70: was BASE_Y (-2), now canon surface Y
        // 20-block cobblestone path from south
        for (int i = 0; i < 10; i++) {
            set(level, c.getX() - 1, gy, c.getZ() + i * 2, COBBLE);
            set(level, c.getX() + 1, gy, c.getZ() + i * 2, COBBLE);
        }
        // Decorative lanterns along the path
        for (int i = 1; i < 10; i += 2) {
            set(level, c.getX() - 2, gy + 1, c.getZ() + i * 2, SEA_LANTERN);
            set(level, c.getX() + 2, gy + 1, c.getZ() + i * 2, SEA_LANTERN);
        }
    }

    /** Outer gate: single arch, 5-wide × 5-tall, with iron-bar doors. */
    private static void buildOuterGate(ServerLevel level, BlockPos c) {
        int gz = c.getZ();
        int gy = c.getY(); // CRON-70: was BASE_Y
        // Pillars (2 blocks thick on each side)
        fill(level, c.getX() - 3, gy, gz - 1, 7, 5, BRICK_WALL);
        fill(level, c.getX() + 3, gy, gz - 1, 7, 5, BRICK_WALL);
        // Arch top slab
        fill(level, c.getX() - 4, gy + 5, gz - 2, 9, 1, BRICK_STAIR);
        // Arch (3 blocks wide interior)
        fill(level, c.getX() - 1, gy, gz, 3, 5, Blocks.AIR.defaultBlockState());
        // Iron bars as "door" (restriction sect — everything is restricted)
        fill(level, c.getX() - 1, gy + 2, gz, 1, 2, IRON_BARS);
        fill(level, c.getX() + 1, gy + 2, gz, 1, 2, IRON_BARS);
        // Floor
        fill(level, c.getX() - 3, gy - 1, gz - 1, 7, 1, COBBLE);
        // Gold plaque above gate (sect name marker)
        fill(level, c.getX() - 2, gy + 5, gz - 1, 5, 1, GOLD);
    }

    /** Central courtyard: 20×16 open plaza with cobblestone floor, spirit spring. */
    private static void buildCentralCourtyard(ServerLevel level, BlockPos c) {
        int gz = c.getZ();
        int gy = c.getY(); // CRON-70: was BASE_Y
        // Floor
        fill(level, c.getX() - 10, gy - 1, gz - 8, 20, 16, COBBLE);
        // Spirit spring pool (5×3 water source at center-north)
        fill(level, c.getX() - 2, gy, gz + 6, 4, 3, WATER);
        // Pool border of lapis (formation core stone)
        fill(level, c.getX() - 3, gy, gz + 5, 6, 5, LAPIS);
        fill(level, c.getX() + 3, gy, gz + 9, 6, 1, LAPIS);
        fill(level, c.getX() - 3, gy, gz + 10, 6, 1, LAPIS);
        // Lapis accent markers at 4 corners of the courtyard
        set(level, c.getX() - 9, gy, gz + 7, LAPIS);
        set(level, c.getX() + 9, gy, gz + 7, LAPIS);
        set(level, c.getX() - 9, gy, gz - 7, LAPIS);
        set(level, c.getX() + 9, gy, gz - 7, LAPIS);
        // Formation line markings on courtyard floor (obsidian — restriction theme)
        for (int i = 0; i < 3; i++) {
            int z1 = gz - 4 + i * 5;
            fill(level, c.getX() - 8, gy, z1, 16, 1, 1, OBSIDIAN);
            fill(level, c.getX() + 9, gy, z1, 16, 1, 1, OBSIDIAN);
        }
    }

    /** Restriction workshop: 12×10 practice area with stone array markers. */
    private static void buildRestrictionWorkshop(ServerLevel level, BlockPos c) {
        int gz = c.getZ() + 18; // East of courtyard
        int gy = c.getY(); // CRON-70: was BASE_Y
        // Floor
        fill(level, c.getX() - 6, gy - 1, gz, 12, 10, COBBLE);
        // 4 practice array markers (2×2 lapis pillars)
        int[][] arrays = {
            {-5, -5}, {-2, -5}, {-5, 0}, {-2, 0},
            {-5, 4}, {-2, 4}, {-5, 9}, {-2, 9}
        };
        for (int[] off : arrays) {
            // 4 corner posts
            set(level, c.getX() + off[0], gy, gz + off[1], LAPIS);
            set(level, c.getX() + off[0] + 1, gy, gz + off[1], LAPIS);
            set(level, c.getX() + off[0], gy + 1, gz + off[1], LAPIS);
            set(level, c.getX() + off[0] + 1, gy + 1, gz + off[1], LAPIS);
            // Center observation lapis
            set(level, c.getX() + off[0] + 1, gy + 2, gz + off[1] + 1, LAPIS);
            // Anvil at workbench (practicing inscription on stone)
            set(level, c.getX() + 3, gy, gz + 3, ANVIL);
            // Chest for practice materials
            set(level, c.getX() + 5, gy, gz + 3, CHEST);
        }
        // Sign: formation core marker
        fill(level, c.getX() - 6, gy, gz + 9, 3, 1, OBSIDIAN);
        fill(level, c.getX() - 5, gy, gz + 9, 1, 1, LAPIS);
    }

    /** Library tower: 3-story pagoda housing restriction art scrolls. */
    private static void buildLibraryTower(ServerLevel level, BlockPos c) {
        int gx = c.getX() - 12;
        int gz = c.getZ() - 12;
        int gy = c.getY(); // CRON-70: was BASE_Y
        // Foundation (5×5)
        fill(level, gx - 2, gy - 1, gz - 2, 5, 5, BRICK_WALL);
        fill(level, gx - 2, gy - 1, gz + 3, 5, 1, BRICK_WALL);
        // Floor
        fill(level, gx - 1, gy, gz, 3, 4, SPRUCE_PLANK);
        // First floor (y=0..3): bookshelves lining walls
        fill(level, gx - 1, gy, gz, 1, 3, BOOKSHELF);
        fill(level, gx + 1, gy, gz, 1, 3, BOOKSHELF);
        fill(level, gx - 1, gy + 1, gz, 1, 3, BOOKSHELF);
        fill(level, gx + 1, gy + 1, gz, 1, 3, BOOKSHELF);
        // Lectern (reading desk)
        fill(level, gx, gy + 1, gz + 2, 2, 1, LECTERN);
        // Chest (restriction scrolls)
        set(level, gx + 2, gy, gz, CHEST);
        // Second floor (y=4..6): more shelves + dark oak detail
        fill(level, gx - 2, gy + 4, gz - 2, 5, 1, DARK_OAK_PLANK);
        fill(level, gx - 2, gy + 4, gz + 3, 5, 1, DARK_OAK_PLANK);
        fill(level, gx - 1, gy + 4, gz, 1, 3, BOOKSHELF);
        fill(level, gx + 1, gy + 4, gz, 1, 3, BOOKSHELF);
        fill(level, gx - 1, gy + 5, gz, 1, 3, BOOKSHELF);
        fill(level, gx + 1, gy + 5, gz, 1, 3, BOOKSHELF);
        // Lectern second floor
        fill(level, gx, gy + 5, gz + 2, 2, 1, LECTERN);
        // Third floor (y=8..10): sealed archive — restriction scrolls behind bars
        fill(level, gx - 2, gy + 8, gz - 2, 5, 1, DARK_OAK_PLANK);
        fill(level, gx - 2, gy + 8, gz + 3, 5, 1, DARK_OAK_PLANK);
        fill(level, gx - 1, gy + 8, gz, 1, 3, BOOKSHELF);
        fill(level, gx + 1, gy + 8, gz, 1, 3, BOOKSHELF);
        // Archive chest
        set(level, gx + 2, gy + 8, gz, CHEST);
        // Iron bars sealing the archive
        fill(level, gx - 2, gy + 9, gz + 1, 5, 1, IRON_BARS);
        fill(level, gx + 3, gy + 9, gz + 1, 5, 1, IRON_BARS);
        // Roof: flat slab roof with stair-slab "eaves"
        fill(level, gx - 3, gy + 10, gz - 3, 7, 1, BRICK_STAIR);
        fill(level, gx - 3, gy + 10, gz + 4, 7, 1, BRICK_STAIR);
        // Label on each floor
        set(level, gx, gy + 1, gz - 1, LAPIS);
        set(level, gx, gy + 5, gz - 1, LAPIS);
        set(level, gx, gy + 9, gz - 1, LAPIS);
    }

    /** Sealed meditation wing: 6 individual chambers (3 per side, mirrored). */
    private static void buildSealedMeditationWing(ServerLevel level, BlockPos c) {
        int gz = c.getZ() + 12;
        int gy = c.getY(); // CRON-70: was BASE_Y
        // East side — 3 chambers
        for (int i = 0; i < 3; i++) {
            int cz = gz + i * 4;
            // Floor
            fill(level, c.getX() + 7, gy, cz, 4, 3, COBBLE);
            // Walls (3 sides, iron bars on the open side facing courtyard)
            fill(level, c.getX() + 6, gy, cz, 1, 3, BRICK_WALL);
            fill(level, c.getX() + 6, gy + 3, cz, 4, 1, BRICK_WALL);
            fill(level, c.getX() + 10, gy, cz, 1, 3, BRICK_WALL);
            // Iron bar "door" (restriction chamber — you must break in)
            fill(level, c.getX() + 7, gy + 1, cz + 1, 2, 2, IRON_BARS);
            // Lectern inside each chamber
            fill(level, c.getX() + 8, gy + 1, cz + 1, 2, 1, LECTERN);
            // Glowstone for ambient light (cultivator meditation)
            set(level, c.getX() + 8, gy + 2, cz + 2, GLOWSTONE);
            // AZALEA decoration (spiritual atmosphere)
            set(level, c.getX() + 9, gy + 2, cz + 1, AZALEA);
        }
        // West side — 3 chambers (mirrored)
        for (int i = 0; i < 3; i++) {
            int cz = gz + i * 4;
            fill(level, c.getX() - 11, gy, cz, 4, 3, COBBLE);
            fill(level, c.getX() - 10, gy, cz, 1, 3, BRICK_WALL);
            fill(level, c.getX() - 10, gy + 3, cz, 4, 1, BRICK_WALL);
            fill(level, c.getX() - 14, gy, cz, 1, 3, BRICK_WALL);
            fill(level, c.getX() - 11, gy + 1, cz + 1, 2, 2, IRON_BARS);
            fill(level, c.getX() - 10, gy + 1, cz + 1, 2, 1, LECTERN);
            set(level, c.getX() - 10, gy + 2, cz + 2, GLOWSTONE);
            set(level, c.getX() - 11, gy + 2, cz + 1, AZALEA);
        }
        // Central corridor between meditation wings
        fill(level, c.getX() - 2, gy - 1, gz, 4, 12, COBBLE);
    }

    /** Refinery: furnace room with cauldron and chimney. */
    private static void buildRefinery(ServerLevel level, BlockPos c) {
        int gx = c.getX() + 8;
        int gz = c.getZ() + 10;
        int gy = c.getY(); // CRON-70: was BASE_Y
        // Floor
        fill(level, gx - 3, gy - 1, gz - 2, 7, 5, COBBLE);
        // Walls
        fill(level, gx - 4, gy, gz - 2, 1, 5, 4, BRICK_WALL);
        fill(level, gx + 3, gy, gz - 2, 1, 5, 4, BRICK_WALL);
        fill(level, gx - 4, gy + 4, gz - 2, 9, 1, BRICK_WALL);
        fill(level, gx + 3, gy + 4, gz - 2, 9, 1, BRICK_WALL);
        fill(level, gx - 4, gy + 4, gz + 3, 7, 1, BRICK_STAIR);
        fill(level, gx - 3, gy + 4, gz + 3, 7, 1, BRICK_STAIR);
        // Blast furnace (main refining apparatus)
        fill(level, gx, gy + 1, gz, 2, 2, BLAST_FURNACE);
        // Cauldron
        set(level, gx + 3, gy + 1, gz + 1, CAULDRON);
        // Smoker (chimney)
        fill(level, gx - 2, gy + 4, gz, 1, 1, SMOKER);
        // Anvil (inscribing tools)
        set(level, gx - 1, gy + 1, gz + 2, ANVIL);
        // Chest for refined materials
        set(level, gx + 3, gy + 1, gz + 2, CHEST);
        // Iron bars on furnace observation window
        fill(level, gx, gy + 2, gz, 2, 1, IRON_BARS);
    }

    /** Elder hall: meeting room for sect master and elders. */
    private static void buildElderHall(ServerLevel level, BlockPos c) {
        int gx = c.getX() - 8;
        int gz = c.getZ() - 14;
        int gy = c.getY(); // CRON-70: was BASE_Y
        // Floor
        fill(level, gx - 3, gy - 1, gz - 2, 7, 6, SPRUCE_PLANK);
        // Walls
        fill(level, gx - 4, gy, gz - 2, 1, 6, 4, SPRUCE_PLANK);
        fill(level, gx + 4, gy, gz - 2, 1, 6, 4, SPRUCE_PLANK);
        fill(level, gx - 4, gy + 3, gz - 2, 7, 1, SPRUCE_PLANK);
        fill(level, gx + 4, gy + 3, gz - 2, 7, 1, SPRUCE_PLANK);
        // Roof — dark oak for scholarly atmosphere
        fill(level, gx - 5, gy + 4, gz - 3, 10, 1, DARK_OAK_PLANK);
        fill(level, gx - 5, gy + 4, gz + 4, 10, 1, DARK_OAK_PLANK);
        // Gold plaque at the front
        fill(level, gx, gy + 1, gz - 1, 3, 1, GOLD);
        // Meeting table (spruce plank)
        fill(level, gx - 1, gy, gz + 1, 3, 1, SPRUCE_PLANK);
        // 4 chairs (2 per side)
        fill(level, gx - 3, gy, gz, 1, 1, DARK_OAK_STAIR);
        fill(level, gx + 2, gy, gz, 1, 1, DARK_OAK_STAIR);
        fill(level, gx - 2, gy, gz + 2, 1, 1, DARK_OAK_STAIR);
        fill(level, gx + 1, gy, gz + 2, 1, 1, DARK_OAK_STAIR);
        // Chest (sect treasury)
        set(level, gx + 3, gy, gz + 1, CHEST);
        // Bookshelf — elder's reference
        fill(level, gx + 3, gy + 1, gz, 1, 3, BOOKSHELF);
        // Lamps
        set(level, gx - 1, gy + 2, gz, SEA_LANTERN);
        set(level, gx + 1, gy + 2, gz, SEA_LANTERN);
    }

    /** Disciple dormitories: 4 small rooms (2 per side of courtyard). */
    private static void buildDiscipleDormitories(ServerLevel level, BlockPos c) {
        int gy = c.getY(); // CRON-70: was BASE_Y
        // South side — 2 rooms
        int[][] southRooms = {{-6, -6}, {-2, -6}};
        for (int[] off : southRooms) {
            int gx = c.getX() + off[0];
            int gz = c.getZ() + off[1];
            fill(level, gx, gy - 1, gz - 1, 3, 3, COBBLE);
            fill(level, gx - 1, gy, gz - 1, 1, 3, BRICK_WALL);
            fill(level, gx + 2, gy, gz - 1, 1, 3, BRICK_WALL);
            fill(level, gx - 1, gy + 2, gz - 1, 3, 1, BRICK_WALL);
            fill(level, gx + 2, gy + 2, gz - 1, 3, 1, BRICK_WALL);
            fill(level, gx - 1, gy + 2, gz + 2, 3, 1, DARK_OAK_STAIR);
            // Bed
            set(level, gx, gy, gz, RED_BED);
        }
        // North side — 2 rooms
        int[][] northRooms = {{-6, 10}, {-2, 10}};
        for (int[] off : northRooms) {
            int gx = c.getX() + off[0];
            int gz = c.getZ() + off[1];
            fill(level, gx, gy - 1, gz - 1, 3, 3, COBBLE);
            fill(level, gx - 1, gy, gz - 1, 1, 3, BRICK_WALL);
            fill(level, gx + 2, gy, gz - 1, 1, 3, BRICK_WALL);
            fill(level, gx - 1, gy + 2, gz - 1, 3, 1, BRICK_WALL);
            fill(level, gx + 2, gy + 2, gz - 1, 3, 1, BRICK_WALL);
            fill(level, gx - 1, gy + 2, gz + 2, 3, 1, DARK_OAK_STAIR);
            set(level, gx, gy, gz, RED_BED);
        }
    }

    /** Outer wall: perimeter wall with corner pillars and lanterns. */
    private static void buildOuterWall(ServerLevel level, BlockPos c) {
        int gy = c.getY(); // CRON-70: was BASE_Y
        // South wall (entry side)
        fill(level, c.getX() - 16, gy, c.getZ() - 8, 32, 4, BRICK_WALL);
        // North wall
        fill(level, c.getX() - 16, gy, c.getZ() + 8, 32, 4, BRICK_WALL);
        // West wall
        fill(level, c.getX() - 16, gy, c.getZ() - 8, 32, 32, BRICK_WALL);
        // East wall
        fill(level, c.getX() + 16, gy, c.getZ() - 8, 32, 32, BRICK_WALL);
        // Corner pillars (taller stone, notched)
        int[][] corners = {{-16, -8}, {16, -8}, {-16, 8}, {16, 8}};
        for (int[] corner : corners) {
            fill(level, c.getX() + corner[0], gy, c.getZ() + corner[1], 2, 5, BRICK_WALL);
        }
        // Lanterns at wall midpoints
        int[][] lanterns = {{-16, 0}, {16, 0}, {0, -8}, {0, 8}};
        for (int[] lp : lanterns) {
            set(level, c.getX() + lp[0], gy + 4, c.getZ() + lp[1], SEA_LANTERN);
        }
    }

    /** Spirit herb garden: small garden behind the refinery. */
    private static void buildSpiritHerbGarden(ServerLevel level, BlockPos c) {
        int gx = c.getX() + 10;
        int gz = c.getZ() + 10;
        int gy = c.getY(); // CRON-70: was BASE_Y
        // Soil floor
        fill(level, gx - 2, gy - 1, gz - 2, 5, 4, DIRT);
        // Water irrigation channel
        fill(level, gx - 2, gy, gz - 2, 1, 4, WATER);
        fill(level, gx + 3, gy, gz - 2, 1, 4, WATER);
        // Spirit herbs (flower pots / cultivated herbs)
        set(level, gx, gy, gz, AZALEA);     // Spirit Flower
        set(level, gx + 1, gy, gz, AZALEA);
        set(level, gx + 2, gy, gz, BLUE_ORCHID); // Spirit Orchid
        set(level, gx, gy, gz + 1, AZALEA);
        set(level, gx + 2, gy, gz + 1, AZALEA);
        set(level, gx, gy, gz + 2, FERN);      // Spirit Fern
        set(level, gx + 1, gy, gz + 2, FERN);
        set(level, gx + 2, gy, gz + 2, FERN);
        // Garden border (low stone wall)
        fill(level, gx - 2, gy, gz - 2, 1, 4, STONE);
        fill(level, gx + 3, gy, gz - 2, 1, 4, STONE);
        fill(level, gx - 2, gy, gz + 2, 1, 4, STONE);
        fill(level, gx + 3, gy, gz + 2, 1, 4, STONE);
        fill(level, gx - 2, gy, gz + 3, 5, 1, STONE);
        // Lectern (herb identification)
        fill(level, gx - 1, gy, gz + 1, 2, 1, LECTERN);
    }

    /** Lanterns throughout the sect for atmosphere. */
    private static void buildLanterns(ServerLevel level, BlockPos c) {
        int gy = c.getY(); // CRON-70: was BASE_Y
        // Along the entry path
        for (int i = 1; i < 10; i += 2) {
            set(level, c.getX() - 2, gy + 1, c.getZ() + i * 2, SEA_LANTERN);
            set(level, c.getX() + 2, gy + 1, c.getZ() + i * 2, SEA_LANTERN);
        }
        // Around courtyard
        int[][] courtyardLanterns = {
                {-8, -6}, {-8, -2}, {-8, 2}, {-8, 6},
                {8, -6}, {8, -2}, {8, 2}, {8, 6},
                {0, -7}, {0, 7}};
        for (int[] lp : courtyardLanterns) {
            set(level, c.getX() + lp[0], gy + 1, c.getZ() + lp[1], SEA_LANTERN);
        }
        // Library entrance lanterns
        set(level, c.getX() - 13, gy + 1, c.getZ() - 12, SEA_LANTERN);
        set(level, c.getX() - 13, gy + 1, c.getZ() - 8, SEA_LANTERN);
        // Meditation wing entrance lanterns
        set(level, c.getX() - 3, gy + 1, c.getZ() + 12, SEA_LANTERN);
        set(level, c.getX() + 3, gy + 1, c.getZ() + 12, SEA_LANTERN);
        // Elder hall lanterns
        set(level, c.getX() - 9, gy + 1, c.getZ() - 14, SEA_LANTERN);
        set(level, c.getX() - 9, gy + 1, c.getZ() - 8, SEA_LANTERN);
        set(level, c.getX() - 9, gy + 1, c.getZ() - 2, SEA_LANTERN);
        set(level, c.getX() + 9, gy + 1, c.getZ() - 14, SEA_LANTERN);
        set(level, c.getX() + 9, gy + 1, c.getZ() - 8, SEA_LANTERN);
    }

    // ── Helper methods ────────────────────────────────────────────────

    /**
     * Filtered setBlock — delegates to {@link #sb} so every set call gets the
     * chunk filter and provenance guard. (CRON-COMPLETIONIST-66 delegation
     * pattern — avoids mechanical replace_all of every set() callsite.)
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

    /** 2D wall fill: dx wide x dy tall, 1 block deep (flat wall in X-Y plane). */
    private static void fill(ServerLevel level, int x, int y, int z,
                            int dx, int dy, BlockState state) {
        fill(level, x, y, z, dx, dy, 1, state);
    }
}
