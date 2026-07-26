package dev.ergenverse.spawn;

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
 * RanyunStarBuilder — materializes 落月村 (Luo Yue Village) on 冉云星
 * (Ranyun Star), Wang Lin's 二次化凡 (Second Mortal Life) arc star.
 *
 * <p><b>CRON-COMPLETIONIST-120 — RANYUN STAR.</b>
 *
 * <p>Prior to this round, the Wang Ping redemption event (CRON-117) fired
 * at the Suzaku Tomb — the conception site. This was a mod-original
 * condensation flagged in CRON-117 self-critique #1 and carried as
 * CRON-117/118/119 NEXT PRIORITY (a) for 3+ rounds. The canon clearly
 * establishes (Baidu Baike 仙逆编年史) that Wang Lin rebuilt Wang Ping's
 * body <b>on 冉云星</b>, not at the Suzaku Tomb. CRON-120 closes this
 * canon-fidelity gap by:
 * <ol>
 *   <li>Adding {@link PlanetSuzakuBlueprint#RANYUN_STAR} at (-5000, 0, -5000)
 *       as a remote overworld region (parallel to KUNXU_REALM and
 *       DA_LUO_SWORD_SECT).</li>
 *   <li>This builder — materializes 落月村 (the woodcarver village at the
 *       foot of 祁连峰) where Wang Lin (alias 阿木/许木) lived as a mortal
 *       woodcarver for 19 years, and where Wang Ping materializes after
 *       the redemption event.</li>
 *   <li>Refactoring {@link dev.ergenverse.wanglin.bead.WangPingRedemptionEvent}
 *       to teleport the player from the Suzaku Tomb to Ranyun Star and
 *       materialize Wang Ping at the village center.</li>
 * </ol>
 *
 * <h2>Canon Basis (fact-checked via web-search 2026-07-27)</h2>
 * <p>Per Baidu Baike 仙逆编年史 (https://baike.baidu.com/item/仙逆编年史/9845998):
 * <ul>
 *   <li><b>落月村 (Luo Yue Village)</b> — at the foot of 祁连峰 (Qi Lian
 *       Peak). Wang Lin (alias 阿木/许木) lived here as a mortal woodcarver
 *       (木雕师) for 19 years.</li>
 *   <li><b>祁连峰 (Qi Lian Peak)</b> — the mountain north of 落月村.</li>
 *   <li><b>祁水城 (Qi Shui City)</b> — distant city where Wang Ping later
 *       ruled as emperor of 天行帝国. NOT materialized by this builder
 *       (too far from the village; future CRON can add it as a separate
 *       CanonLocation + builder).</li>
 *   <li><b>宝合楼, 三大家族 (冉/孙/赵), 雷仙殿 outpost</b> — NOT materialized
 *       by this builder; future CRON can add them.</li>
 * </ul>
 *
 * <p><b>NO fabricated chapter citation.</b> 落月村 and 祁连峰 are attested
 * by Baidu Baike 仙逆编年史. The exact chapter of Wang Lin's arrival on
 * Ranyun Star is NOT cited to avoid fabrication.
 *
 * <h2>Village Layout (modest 30×30 footprint)</h2>
 * <pre>
 *   N (toward 祁连峰 — Qi Lian Peak, materialized as a stone mountain backdrop)
 *   ┌──────────────────────────────────┐
 *   │  Mountain backdrop (stone)       │
 *   │  ──── 祁连峰 base ────           │
 *   │                                  │
 *   │  Wang Lin's   Woodcarver's       │
 *   │  wood hut     bench (center)     │
 *   │  (NW)         ← Wang Ping        │
 *   │               materializes here  │
 *   │                                  │
 *   │  Commoner     Commoner hut       │
 *   │  hut (SE)     (NE)               │
 *   │                                  │
 *   │  South path → exit (to 祁水城)   │
 *   └──────────────────────────────────┘
 *   S (player approach direction)
 * </pre>
 *
 * <p>The layout is intentionally MODEST — this is a poor mortal
 * woodcarver's village, not a sect or capital. Wang Lin chose this
 * remote village precisely BECAUSE it was unremarkable. Per canon,
 * Wang Lin lived here for 19 years carving wood (木雕) in solitude,
 * echoing his own father's craft on Planet Suzaku.
 *
 * <h2>Chunk-scoped (CRON-69 point 7 full fidelity)</h2>
 * <p>Mirrors the {@link SuzakuTombBuilder} pattern (CRON-105): a
 * {@link ThreadLocal}&lt;{@link ChunkBounds}&gt; holds the active bounds
 * during a {@link #buildInternal} call; the {@link #sb} helper reads it
 * at each leaf placement and skips blocks outside the chunk. This
 * eliminates the whole-village-build-on-any-chunk-load bug.
 *
 * <h2>Provenance-aware rebuild guard (CRON-63 pattern)</h2>
 * <p>The {@link #sb} helper consults {@link WorldDeltaStore} for PLAYER
 * or SIMULATION deltas at each position. If the player has mined a block
 * (or a simulation event has altered it), the CANON placement is skipped.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public final class RanyunStarBuilder {

    private RanyunStarBuilder() {}

    // ════════════════════════════════════════════════════════════════════
    //  CANON COORDINATES
    // ════════════════════════════════════════════════════════════════════

    /**
     * The canon center of Ranyun Star, from
     * {@link PlanetSuzakuBlueprint#RANYUN_STAR}. 落月村 is centered at
     * this (x, z); the y comes from the blueprint's y field (0 = sea level,
     * but actual placement uses the heightmap-resolved surface Y).
     */
    private static final int STAR_X = PlanetSuzakuBlueprint.RANYUN_STAR.x;
    private static final int STAR_Z = PlanetSuzakuBlueprint.RANYUN_STAR.z;

    /**
     * Village half-size. The village footprint is (2*HALF)^2 = 30x30 blocks.
     * Small enough to fit in a 2-chunk footprint; large enough to feel
     * like a real woodcarver's village (not a single hut).
     */
    private static final int HALF = 15;

    /**
     * Mountain half-size. 祁连峰 is a 11×11 stone mound north of the village.
     * Modest — canon does not describe 祁连峰 as a soaring peak; it's the
     * local hill above 落月村.
     */
    private static final int MOUNTAIN_HALF = 11;

    /**
     * Mountain peak height (blocks above surface). A 12-block stone mound
     * gives the impression of a hill without obstructing the village view.
     */
    private static final int MOUNTAIN_HEIGHT = 12;

    /**
     * Mountain offset north of village center. Places the mountain base
     * just outside the village's north edge.
     */
    private static final int MOUNTAIN_OFFSET_NORTH = 25;

    // ════════════════════════════════════════════════════════════════════
    //  CHUNK-SCOPED PLACEMENT (CRON-62/63 pattern, mirrors SuzakuTombBuilder)
    // ════════════════════════════════════════════════════════════════════

    /**
     * ThreadLocal holding the active ChunkBounds during a buildInternal()
     * call. Set once at the top of {@link #buildForChunk} and read by
     * {@link #sb} at each leaf placement. Null means "full build" (no
     * chunk filtering) — used by the command/login {@link #build} path.
     */
    private static final ThreadLocal<ChunkBounds> CURRENT_BOUNDS = new ThreadLocal<>();

    /**
     * Filtered setBlock — the ONLY block-placement call site in this class.
     * Three guards, in order:
     *
     * <p><b>1. Chunk filter (CRON-62 pattern):</b> if CURRENT_BOUNDS is
     * non-null and (x, z) falls outside the bounds, skip.
     *
     * <p><b>2. Provenance-aware rebuild guard (CRON-63 pattern):</b> if
     * CURRENT_BOUNDS is non-null (chunk-materializer path), consult the
     * {@link WorldDeltaStore} for a PLAYER or SIMULATION delta at (x, y, z).
     * If either exists, skip the placement.
     *
     * <p><b>3. Placement:</b> if both guards pass, call level.setBlock.
     */
    private static void sb(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        ChunkBounds b = CURRENT_BOUNDS.get();
        if (b != null) {
            // Guard 1: chunk filter.
            if (!b.contains(pos.getX(), pos.getZ())) return;
            // Guard 2: provenance-aware rebuild guard.
            if (hasPlayerOrSimulationDelta(pos)) return;
        }
        level.setBlock(pos, state, flags);
    }

    /**
     * Provenance-aware guard helper (CRON-63 pattern). Returns true if a
     * PLAYER or SIMULATION delta is recorded at {@code pos}. O(1) per call.
     *
     * <p>Defensive: returns false (no delta → proceed with placement) if
     * the WorldRuntime is not yet initialized.
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
            Ergenverse.LOGGER.debug("[Ergenverse] RanyunStarBuilder provenance guard failed at {}: {} — proceeding.",
                    pos, t.getMessage());
            return false;
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  PUBLIC API
    // ════════════════════════════════════════════════════════════════════

    /**
     * Chunk-scoped build entry point — invoked by the chunk-materializer
     * for each chunk that overlaps the village footprint.
     *
     * <p>When {@code bounds} is non-null, only blocks whose (x, z) fall
     * inside the bounds are placed. When {@code bounds} is null, the full
     * village is built (command/login path).
     */
    public static void buildForChunk(ServerLevel level, @Nullable ChunkBounds bounds) {
        ChunkBounds prev = CURRENT_BOUNDS.get();
        CURRENT_BOUNDS.set(bounds);
        try {
            buildInternal(level);
        } finally {
            if (prev == null) CURRENT_BOUNDS.remove();
            else CURRENT_BOUNDS.set(prev);
        }
    }

    /**
     * Full build — used by command/login paths. Idempotent: guarded by
     * {@link #isAlreadyBuilt} so repeated calls are cheap.
     */
    public static void build(ServerLevel level) {
        if (isAlreadyBuilt(level)) {
            Ergenverse.LOGGER.debug("[Ergenverse] RanyunStarBuilder: village already built at ({},{}), skipping.",
                    STAR_X, STAR_Z);
            return;
        }
        buildForChunk(level, null);
    }

    /**
     * Internal build — assumes CURRENT_BOUNDS is set (or null for full build).
     * Constructs the village + mountain backdrop.
     *
     * <p>Surface Y is resolved per-column via the heightmap so the village
     * adapts to the actual terrain. Each structure's base Y is the surface
     * Y at its center.
     */
    private static void buildInternal(ServerLevel level) {
        // Resolve the village center's surface Y via MOTION_BLOCKING heightmap.
        BlockPos centerBlockPos = new BlockPos(STAR_X, 0, STAR_Z);
        // Force-load the center chunk so the heightmap query is accurate.
        level.getChunk(STAR_X >> 4, STAR_Z >> 4);
        BlockPos surfacePos = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                centerBlockPos);
        int surfaceY = surfacePos.getY();

        Ergenverse.LOGGER.info("[Ergenverse] CRON-120: Building Ranyun Star 落月村 at ({}, {}, {}).",
                STAR_X, surfaceY, STAR_Z);

        // 1. Mountain backdrop (祁连峰) — stone mound north of the village.
        buildMountain(level, surfaceY);

        // 2. Village plaza — packed earth + a woodcarver's bench at the center.
        buildVillagePlaza(level, surfaceY);

        // 3. Wang Lin's wood hut — NW quadrant, modest 5x4 hut.
        buildWoodHut(level, STAR_X - 6, surfaceY, STAR_Z - 6, "Wang Lin's Wood Hut");

        // 4. Two commoner huts — SE and NE.
        buildWoodHut(level, STAR_X + 4, surfaceY, STAR_Z + 4, "Commoner Hut");
        buildWoodHut(level, STAR_X + 4, surfaceY, STAR_Z - 6, "Commoner Hut");

        // 5. South path — dirt path leading south (toward 祁水城, off-village).
        buildSouthPath(level, surfaceY);

        // 6. Woodcarving altar — the spiritual focus where Wang Ping materializes.
        //    A crafting table on a stone brick base, with a lantern above.
        buildWoodcarvingAltar(level, surfaceY);
    }

    /**
     * Build the 祁连峰 mountain backdrop north of the village. A modest
     * stone mound — canon does not describe 祁连峰 as a soaring peak.
     */
    private static void buildMountain(ServerLevel level, int villageSurfaceY) {
        int mountainCenterX = STAR_X;
        int mountainCenterZ = STAR_Z - MOUNTAIN_OFFSET_NORTH;
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();

        // Force-load the mountain chunk so heightmap queries are accurate.
        level.getChunk(mountainCenterX >> 4, mountainCenterZ >> 4);

        // Layered mound: each layer shrinks by 1 block on each side, going up.
        for (int layer = 0; layer < MOUNTAIN_HEIGHT; layer++) {
            int layerHalf = MOUNTAIN_HALF - layer;
            if (layerHalf <= 0) break;
            int layerY = villageSurfaceY + layer;
            for (int dx = -layerHalf; dx <= layerHalf; dx++) {
                for (int dz = -layerHalf; dz <= layerHalf; dz++) {
                    int x = mountainCenterX + dx;
                    int z = mountainCenterZ + dz;
                    // Only place the outer ring + interior fill (skip air pockets).
                    // Outer 1-block ring = stone; interior = dirt (covered by grass on top).
                    BlockPos pos = new BlockPos(x, layerY, z);
                    if (layer == MOUNTAIN_HEIGHT - 1) {
                        sb(level, pos, grass, 3);  // grass on top
                    } else if (Math.abs(dx) == layerHalf || Math.abs(dz) == layerHalf) {
                        sb(level, pos, stone, 3);  // stone shell
                    } else if (layer == 0) {
                        sb(level, pos, stone, 3);  // stone base
                    }
                    // Interior dirt is skipped — keeps the mound hollow and
                    // saves block-placement calls (chunk-scoped efficiency).
                }
            }
        }
    }

    /**
     * Build the village plaza — packed earth floor + a woodcarver's bench
     * (crafting table) at the center as the village's spiritual focus.
     */
    private static void buildVillagePlaza(ServerLevel level, int surfaceY) {
        BlockState packedEarth = Blocks.DIRT_PATH.defaultBlockState();
        // 5x5 plaza floor centered at (STAR_X, STAR_Z).
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos pos = new BlockPos(STAR_X + dx, surfaceY, STAR_Z + dz);
                sb(level, pos, packedEarth, 3);
            }
        }
    }

    /**
     * Build the woodcarving altar — the spiritual focus at the village
     * center where Wang Ping materializes. A crafting table on a stone
     * brick base, with a lantern above for ambient light.
     *
     * <p>This is the canon-faithful "Wang Lin's woodcarver's bench" —
     * where he carved wood for 19 years. When the redemption event fires,
     * Wang Ping materializes standing on this altar.
     */
    private static void buildWoodcarvingAltar(ServerLevel level, int surfaceY) {
        // Stone brick base (1 block above plaza floor).
        BlockPos basePos = new BlockPos(STAR_X, surfaceY + 1, STAR_Z);
        sb(level, basePos, Blocks.STONE_BRICKS.defaultBlockState(), 3);

        // Crafting table on top (the woodcarver's bench surface).
        BlockPos benchPos = new BlockPos(STAR_X, surfaceY + 2, STAR_Z);
        sb(level, benchPos, Blocks.CRAFTING_TABLE.defaultBlockState(), 3);

        // Lantern above for ambient light (the woodcarver works at night).
        BlockPos lanternPos = new BlockPos(STAR_X, surfaceY + 4, STAR_Z);
        sb(level, lanternPos, Blocks.LANTERN.defaultBlockState(), 3);
    }

    /**
     * Build a simple wood hut — 5x4 footprint, oak logs + oak planks,
     * a door, and a flat oak-slab roof. Mortal village construction.
     *
     * @param cx       the hut's center X
     * @param surfaceY the surface Y at the hut's center
     * @param cz       the hut's center Z
     * @param name     the hut's name (for logging only)
     */
    private static void buildWoodHut(ServerLevel level, int cx, int surfaceY, int cz, String name) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState slab = Blocks.OAK_SLAB.defaultBlockState();
        BlockState door = Blocks.OAK_DOOR.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();

        // 5x4 footprint: walls form a ring, hollow interior.
        int halfX = 2, halfZ = 1;
        int floorY = surfaceY;
        int wallHeight = 3;  // 3 blocks tall walls

        // Floor: oak planks.
        for (int dx = -halfX; dx <= halfX; dx++) {
            for (int dz = -halfZ; dz <= halfZ; dz++) {
                sb(level, new BlockPos(cx + dx, floorY, cz + dz), planks, 3);
            }
        }

        // Walls: oak logs at corners, oak planks between, door on south side.
        for (int dy = 1; dy <= wallHeight; dy++) {
            for (int dx = -halfX; dx <= halfX; dx++) {
                for (int dz = -halfZ; dz <= halfZ; dz++) {
                    // Skip interior — only walls.
                    if (Math.abs(dx) != halfX && Math.abs(dz) != halfZ) continue;
                    BlockPos pos = new BlockPos(cx + dx, floorY + dy, cz + dz);
                    // Corner = log, otherwise plank.
                    if (Math.abs(dx) == halfX && Math.abs(dz) == halfZ) {
                        sb(level, pos, log, 3);
                    } else {
                        // Door on south wall (dz == halfZ), middle row, dy=1 or 2.
                        if (dz == halfZ && dx == 0 && (dy == 1 || dy == 2)) {
                            sb(level, pos, door, 3);
                        }
                        // Window on north wall (dz == -halfZ), middle row, dy=2.
                        else if (dz == -halfZ && dx == 0 && dy == 2) {
                            sb(level, pos, glass, 3);
                        } else {
                            sb(level, pos, planks, 3);
                        }
                    }
                }
            }
        }

        // Roof: oak slabs on top of walls.
        for (int dx = -halfX; dx <= halfX; dx++) {
            for (int dz = -halfZ; dz <= halfZ; dz++) {
                sb(level, new BlockPos(cx + dx, floorY + wallHeight + 1, cz + dz), slab, 3);
            }
        }
    }

    /**
     * Build the south path — a dirt path leading south from the village
     * plaza toward where 祁水城 (Qi Shui City) would be (off-village).
     */
    private static void buildSouthPath(ServerLevel level, int surfaceY) {
        BlockState path = Blocks.DIRT_PATH.defaultBlockState();
        // Path extends 15 blocks south from the plaza edge.
        for (int dz = 3; dz <= 18; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                sb(level, new BlockPos(STAR_X + dx, surfaceY, STAR_Z + dz), path, 3);
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  IDEMPOTENCY GUARD
    // ════════════════════════════════════════════════════════════════════

    /**
     * Idempotency guard — returns true if the woodcarving altar (the
     * village's centerpiece) is already in place. Used by the
     * command/login {@link #build} path to skip redundant full builds.
     *
     * <p>The chunk-scoped {@link #buildForChunk} path does NOT call this
     * — chunk-scoped placement is naturally incremental: each chunk
     * places its own slice, and re-placing an already-present block is
     * a harmless no-op.
     *
     * <p>Centerpiece = the crafting table at (STAR_X, surfaceY+2, STAR_Z).
     */
    private static boolean isAlreadyBuilt(ServerLevel level) {
        try {
            BlockPos centerBlockPos = new BlockPos(STAR_X, 0, STAR_Z);
            level.getChunk(STAR_X >> 4, STAR_Z >> 4);
            BlockPos surfacePos = level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                    centerBlockPos);
            int surfaceY = surfacePos.getY();
            BlockPos altarPos = new BlockPos(STAR_X, surfaceY + 2, STAR_Z);
            return level.getBlockState(altarPos).getBlock() == Blocks.CRAFTING_TABLE;
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] RanyunStarBuilder isAlreadyBuilt check failed: {} — assuming not built.",
                    t.getMessage());
            return false;
        }
    }
}
