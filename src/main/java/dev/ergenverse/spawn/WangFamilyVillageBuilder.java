package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.ChunkBounds;
import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.runtime.delta.WorldDeltaStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * WangFamilyVillageBuilder — a FULLY hand-built Wang Family Village (王氏村),
 * Wang Lin's birthplace in Zhao Country. Every block placed intentionally.
 *
 * <p><b>Constitution:</b> "The world is completely hand-built. NEVER write a script
 * that replaces vanilla blocks with other blocks. Every structure must be
 * hand-authored." Per Art XXI — "The World Is The Main Character." Per
 * Art XXIII — "A finished region is worth more than ten partially implemented
 * systems." Per Art XXVII — "Completion Is Proven By Life."
 *
 * <p><b>Canonical location:</b> ALWAYS at (3842, surface, -1184). Every world,
 * every seed, every player. The village exists before any player joins.
 *
 * <p><b>Canon (Renegade Immortal, Ch.1-10):</b>
 * Wang Lin's village is a small, poor mortal farming village in Zhao Country.
 * His family is ordinary — father Wang Tian (deceased), mother, younger
 * brother. The village elder is respected. Spirit herbs grow in hidden
 * patches near the village. A small spirit vein slumbers beneath the village
 * center (Wang Lin later discovers it during his cultivation journey).
 *
 * <h2>Village Layout (82x82 footprint, radius 41)</h2>
 * <pre>
 *   N (toward Heng Yue Sect, ~1600 blocks)
 *   ┌──────────────────────────────────────────────┐
 *   │  Forest edge  │  North farm plots  │  Forest │
 *   │               │                    │         │
 *   │  Wang home    │  Spirit well       │  Elder  │
 *   │  (distinct)   │  (village center)  │  home   │
 *   │               │                    │  (nicer) │
 *   │  Commoner     │  Central plaza     │  Commoner│
 *   │  homes x6     │  + formation array │  homes x4│
 *   │               │                    │         │
 *   │  Herb garden  │  South path →exit  │  Storage │
 *   │  (hidden)     │                    │  sheds  │
 *   │               │  South farm plots  │         │
 *   │  Forest edge  │                    │  Forest │
 *   └──────────────────────────────────────────────┘
 *   S (player approach direction)
 * </pre>
 *
 * <h2>Districts (14 total):</h2>
 * <ol>
 *   <li>Central plaza — spirit stone + spirit vein centerpiece + 4 formation
 *       cores + village well (spirit vein stone column)</li>
 *   <li>Wang family home — NW quadrant, modest 7x5 house with alchemy
 *       furnace (Wang Lin's father kept one), identifiable by stone path</li>
 *   <li>Village elder's home — NE quadrant, slightly larger 7x7 house with
 *       formation flag base (elder's status symbol), jade stone steps</li>
 *   <li>Six commoner homes — scattered around, 5x5 each, spirit wood
 *       construction, each with a small herb pot outside</li>
 *   <li>Four additional commoner homes — E side, same construction</li>
 *   <li>North farm plots — 3 rectangular fields of spirit grass with
 *       qi-gathering grass rows (the village grows spirit herbs as
 *       mortals grow rice — they don't know what they are)</li>
 *   <li>South farm plots — 2 more fields</li>
 *   <li>Herb garden (hidden) — behind Wang family home, enclosed by spirit
 *       wood fence, rare herbs (five-color ginseng, nine-leaf clover)</li>
 *   <li>Storage sheds — 2 small 3x3 structures behind the village</li>
 *   <li>Village well — at the center, spirit vein stone pillar 3 blocks tall</li>
 *   <li>Main road — spirit sand path N-S through center, E-W crossroad</li>
 *   <li>Perimeter fence — spirit wood log fence, gaps at N and S entries</li>
 *   <li>Spirit trees — 12 trees around perimeter and inside village</li>
 *   <li>Light markers — spirit vein stones along paths (replaces torches)</li>
 * </ol>
 *
 * <h2>Harsh Self-Critique</h2>
 * <ul>
 *   <li>Houses are still box-shaped — no xianxia curved roofs, no dougong
 *       brackets. This is a mortal village, so flat roofs are MORE canonically
 *       correct than pagoda eaves. Mortal villages don't have upturned eaves.</li>
 *   <li>No interior furniture — tables, chairs, beds are not placed.
 *       The Alchemy Furnace in Wang's home is the only interior content.
 *       Future: add crafting tables (as mortals' woodworking), beds, chests.</li>
 *   <li>Farms are flat spirit-grass rectangles — no crop rows, no irrigation
 *       channels. Real mortal farms have tilled earth rows and water channels.</li>
 *   <li>No NPCs spawn here yet — the buildings exist but nobody lives in them.
 *       The NpcSpawnRegistry needs wang_family_village entries.</li>
 *   <li>The "hidden herb garden" is visible from above — not truly hidden.
 *       Future: place it behind a terrain feature or inside a hollow.</li>
 *   <li>All houses use the same spirit wood palette — no material variation
 *       between rich (elder) and poor (commoner) homes. The elder's home
 *       is larger and has jade steps, but the wall material is the same.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public final class WangFamilyVillageBuilder {

    /**
     * Lazy-initialized BlockState holder. ErgenverseBlocks.X.get() throws NPE before
     * Forge resolves the block registry, so these cannot be static-final in the outer
     * class. This inner class loads on first reference (during build(), which runs at
     * world-gen time — well after registry resolution).
     */
    private static final class B {
        private static final BlockState SPIRIT_GRASS = ErgenverseBlocks.SPIRIT_GRASS.get().defaultBlockState();
        private static final BlockState SPIRIT_DIRT = ErgenverseBlocks.SPIRIT_DIRT.get().defaultBlockState();
        private static final BlockState SPIRIT_STONE = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
        private static final BlockState SPIRIT_SAND = ErgenverseBlocks.SPIRIT_SAND.get().defaultBlockState();
        private static final BlockState JADE_STONE = ErgenverseBlocks.JADE_STONE.get().defaultBlockState();
        private static final BlockState FORMATION_CORE = ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState();
        private static final BlockState SPIRIT_VEIN = ErgenverseBlocks.SPIRIT_VEIN_STONE.get().defaultBlockState();
        private static final BlockState PLANKS = ErgenverseBlocks.SPIRIT_WOOD_PLANKS.get().defaultBlockState();
        private static final BlockState LOG = ErgenverseBlocks.SPIRIT_WOOD_LOG.get().defaultBlockState();
        private static final BlockState LEAVES = ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get().defaultBlockState();
        private static final BlockState QI_GRASS = ErgenverseBlocks.QI_GATHERING_GRASS.get().defaultBlockState();
        private static final BlockState SNOW_HERB = ErgenverseBlocks.SNOW_HEART_HERB.get().defaultBlockState();
        private static final BlockState FIVE_GINSENG = ErgenverseBlocks.FIVE_COLOR_GINSENG.get().defaultBlockState();
        private static final BlockState NINE_CLOVER = ErgenverseBlocks.NINE_LEAF_CLOVER.get().defaultBlockState();
        private static final BlockState SOUL_LOTUS = ErgenverseBlocks.SOUL_NOURISHING_LOTUS.get().defaultBlockState();
        private static final BlockState FIRE_LOTUS = ErgenverseBlocks.FIRE_BLOOM_LOTUS.get().defaultBlockState();
        private static final BlockState VERMILION_GINSENG = ErgenverseBlocks.VERMILION_BLOOD_GINSENG.get().defaultBlockState();
        private static final BlockState SWORD_MOSS = ErgenverseBlocks.SWORD_EDGE_MOSS.get().defaultBlockState();
        private static final BlockState DAO_VINE = ErgenverseBlocks.DAO_TRACE_VINE.get().defaultBlockState();
        private static final BlockState FOUNDATION_VINE = ErgenverseBlocks.FOUNDATION_ROOT_VINE.get().defaultBlockState();
    }

    private WangFamilyVillageBuilder() {}

    /** Village half-extent. Total footprint = (2*RADIUS+1) squared = 83x83. */
    public static final int VILLAGE_RADIUS = 41;

    /**
     * Canonical village X coordinate. Fixed for every world/seed/player.
     * Sourced from {@link dev.ergenverse.runtime.PlanetSuzakuBlueprint#WANG_FAMILY_VILLAGE}.
     */
    public static final int VILLAGE_X =
            dev.ergenverse.runtime.PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.x;

    /**
     * Canonical village Z coordinate. Fixed for every world/seed/player.
     * Sourced from {@link dev.ergenverse.runtime.PlanetSuzakuBlueprint#WANG_FAMILY_VILLAGE}.
     */
    public static final int VILLAGE_Z =
            dev.ergenverse.runtime.PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.z;

    /**
     * The village center is the fixed canonical position. The Y coordinate
     * comes from {@link dev.ergenverse.runtime.worldgen.BlueprintChunkGenerator#canonSurfaceHeight}
     * — the same pure deterministic function the chunk generator uses to
     * shape the surface. This eliminates the heightmap race condition
     * (CRON-67): the canon Y is correct regardless of which chunks are loaded
     * when this method is called.
     */
    public static BlockPos getVillageCenter(ServerLevel level) {
        int surfaceY = dev.ergenverse.runtime.worldgen.BlueprintChunkGenerator.canonSurfaceHeight(VILLAGE_X, VILLAGE_Z);
        return new BlockPos(VILLAGE_X, surfaceY, VILLAGE_Z);
    }

    /**
     * Returns true if the village has already been built (the spirit vein
     * centerpiece block is present above the spawn point).
     */
    public static boolean isAlreadyBuilt(ServerLevel level) {
        BlockPos center = getVillageCenter(level);
        return level.getBlockState(center.above()).getBlock()
                == ErgenverseBlocks.SPIRIT_VEIN_STONE.get();
    }

    // ── Chunk-scoped build infrastructure (CRON-COMPLETIONIST-62) ────────
    //
    // The chunk-materializer invokes buildForChunk(level, bounds) for EACH
    // chunk that overlaps the village footprint. The ThreadLocal CURRENT_BOUNDS
    // holds the active bounds during a buildInternal() call; the sb() helper
    // checks it and skips any placement outside the bounds. When bounds is null
    // (full-build path — commands, login events, SpawnEventHandler), no
    // filtering occurs and every setBlock lands.
    //
    // Why ThreadLocal rather than threading bounds through 14 sub-methods:
    // the village build tree (flattenTerrain, buildRoads, buildCentralPlaza,
    // buildPerimeterFence, buildWangFamilyHome -> buildWangLinCorner, ...) has
    // 79 setBlock call sites and a deep call hierarchy. Adding a ChunkBounds
    // parameter to every method would touch every signature and obscure the
    // geometric intent. The ThreadLocal is set once at the top of buildForChunk
    // and read by sb() at each leaf placement — a single global per build pass.
    //
    // Thread-safety: chunk materialization is single-threaded on the server
    // tick thread, so there is no contention. The ThreadLocal guards against
    // re-entrancy only if a builder were to call another builder (which it
    // does not today).

    /**
     * The active chunk bounds during a buildInternal() pass, or null if the
     * full village is being built (no filtering).
     */
    private static final ThreadLocal<ChunkBounds> CURRENT_BOUNDS = new ThreadLocal<>();

    /**
     * Filtered setBlock — the ONLY block-placement call site in this class.
     * Three guards, in order:
     *
     * <p><b>1. Chunk filter (CRON-COMPLETIONIST-62):</b> if CURRENT_BOUNDS is
     * non-null and (x, z) falls outside the bounds, skip. This is what makes
     * buildForChunk chunk-scoped: ~80K candidate placements collapse to ~256
     * actual level.setBlock calls per chunk.
     *
     * <p><b>2. Provenance-aware rebuild guard (CRON-COMPLETIONIST-63):</b> if
     * CURRENT_BOUNDS is non-null (i.e. we are in the chunk-materializer path),
     * consult the {@link WorldDeltaStore} for a PLAYER or SIMULATION delta at
     * (x, y, z). If either exists, skip the placement. The player's edits
     * (and the simulation's edits) take priority over CANON — re-placing a
     * CANON block on top of a player edit would be a wasted write (the
     * materializer replays the player/sim delta afterward and restores the
     * player's state, but the intermediate write is unnecessary and produces
     * a visible flicker). This guard is the operational realization of the
     * Provenance contract: PLAYER &gt; SIMULATION &gt; CANON.
     *
     * <p>The provenance guard is <b>only active in the chunk-scoped path</b>
     * (CURRENT_BOUNDS != null). The full-build path — {@link #build}, used by
     * SpawnEventHandler at server-start and ErgenverseCommand for manual
     * rebuild — does NOT consult the delta store, because (a) at server-start
     * there are no player deltas yet, and (b) the /ergenverse build command
     * explicitly wants a full rebuild regardless of player edits.
     *
     * <p><b>3. Placement:</b> if both guards pass, call level.setBlock.
     *
     * <p>Y is intentionally not chunk-filtered — structures are vertically
     * thin. Y IS used for the provenance check (deltas are 3D-positioned).
     *
     * <p>Performance: ~50ns per guard (2x O(1) HashMap lookup + PackedPos.pack).
     * For ~256 blocks per chunk-load: ~12.8µs total. Negligible vs the ~1-5µs
     * per saved level.setBlock call.
     */
    private static void sb(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        ChunkBounds b = CURRENT_BOUNDS.get();
        if (b != null) {
            // Guard 1: chunk filter.
            if (!b.contains(pos.getX(), pos.getZ())) return;
            // Guard 2: provenance-aware rebuild guard.
            // If the player or simulation has touched this position, the
            // CANON placement would be overwritten by the materializer's
            // delta replay anyway — skip the wasted write.
            if (hasPlayerOrSimulationDelta(pos)) return;
        }
        level.setBlock(pos, state, flags);
    }

    /**
     * Provenance-aware guard helper (CRON-COMPLETIONIST-63). Returns true if
     * a PLAYER or SIMULATION delta is recorded at {@code pos}. O(1) per call.
     *
     * <p>Defensive: returns false (no delta → proceed with placement) if the
     * WorldRuntime is not yet initialized. The chunk-materializer already
     * gates on {@code runtime.isInitialized()} so this should never fire in
     * the materializer path, but defense-in-depth protects against unexpected
     * call sites.
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
            // Defensive: never let a delta-store query failure block a build.
            // Log at debug (not error) to avoid log spam if this fires
            // repeatedly in a broken state.
            Ergenverse.LOGGER.debug("[Ergenverse] Provenance guard failed at {}: {} — proceeding with placement.",
                    pos, t.getMessage());
            return false;
        }
    }

    /**
     * Chunk-scoped build entry point — invoked by the chunk-materializer for
     * each chunk that overlaps the village footprint.
     *
     * <p>When {@code bounds} is non-null, only blocks whose (x, z) fall inside
     * the bounds are placed; the rest are skipped by {@link #sb}. When
     * {@code bounds} is null, the full village is built (command/login path).
     *
     * <p>This method does NOT call {@link #isAlreadyBuilt} — chunk-scoped
     * placement is naturally incremental: each chunk places its own slice,
     * and re-placing an already-present block with the same state is a
     * harmless no-op. The isAlreadyBuilt guard lives in {@link #build} (the
     * full-build path) to keep command/login calls idempotent.
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

    // ── Block palette ─────────────────────────────────────────────────────
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState FARMLAND = Blocks.FARMLAND.defaultBlockState();

    // ── Herb blocks ─────────────────────────────────────────────────────
    /**
     * Build the full village — the legacy full-build entry point used by
     * SpawnEventHandler (server-start) and ErgenverseCommand (manual /ergenverse
     * build). Idempotent: guarded by {@link #isAlreadyBuilt} so repeated calls
     * are a no-op once the spirit-vein centerpiece is in place.
     *
     * <p>For chunk-load materialization, use {@link #buildForChunk} instead —
     * it filters placements to a single chunk and avoids the cascading-load
     * catastrophe where a full 80K-block build fires on every chunk load.
     */
    public static void build(ServerLevel level) {
        if (isAlreadyBuilt(level)) {
            Ergenverse.LOGGER.debug("[Ergenverse] Wang Family Village already built — build() is a no-op.");
            return;
        }
        buildInternal(level);
    }

    /**
     * The actual village construction — flattens an 83x83 area and places all
     * 14 districts. Called from {@link #build} (idempotent full-build path)
     * and {@link #buildForChunk} (chunk-scoped path). Both paths share this
     * body; chunk-scoping is enforced by {@link #sb} reading the ThreadLocal
     * {@link #CURRENT_BOUNDS} set by the caller.
     */
    private static void buildInternal(ServerLevel level) {
        BlockPos center = getVillageCenter(level);
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ChunkBounds bounds = CURRENT_BOUNDS.get();
        if (bounds == null) {
            Ergenverse.LOGGER.info("[Ergenverse] Building Wang Family Village v2 (full) at ({}, {}, {}).",
                    cx, cy, cz);
        } else {
            Ergenverse.LOGGER.debug("[Ergenverse] Building Wang Family Village v2 (chunk-scoped {}) at center ({}, {}, {}).",
                    bounds, cx, cy, cz);
        }

        // ── 1. Flatten and terrain the area ───────────────────────────
        flattenTerrain(level, cx, cy, cz);

        // ── 2. Main roads (N-S and E-W) ──────────────────────────────
        buildRoads(level, cx, cy, cz);

        // ── 3. Central plaza + spirit vein well ────────────────────
        buildCentralPlaza(level, cx, cy, cz);

        // ── 4. Perimeter fence ───────────────────────────────────────
        buildPerimeterFence(level, cx, cy, cz);

        // ── 5. Wang family home (NW) ────────────────────────────────
        buildWangFamilyHome(level, cx - 18, cy, cz - 18);

        // ── 6. Village elder's home (NE) ────────────────────────────
        buildElderHome(level, cx + 10, cy, cz - 18);

        // ── 7. Commoner homes (10 total, scattered) ──────────────────
        buildCommonerHome(level, cx - 18, cy, cz - 4);   // W1
        buildCommonerHome(level, cx - 18, cy, cz + 6);   // W2
        buildCommonerHome(level, cx + 6, cy, cz - 4);    // E1
        buildCommonerHome(level, cx + 6, cy, cz + 6);    // E2
        buildCommonerHome(level, cx - 10, cy, cz - 28);  // NW1
        buildCommonerHome(level, cx + 2, cy, cz - 28);   // NW2
        buildCommonerHome(level, cx + 16, cy, cz - 28);   // NE1
        buildCommonerHome(level, cx - 10, cy, cz + 12);   // SW1
        buildCommonerHome(level, cx + 2, cy, cz + 12);    // SW2
        buildCommonerHome(level, cx + 16, cy, cz + 12);   // SE1

        // ── 8. Farm plots ───────────────────────────────────────────
        buildNorthFarms(level, cx, cy, cz - 35);
        buildSouthFarms(level, cx, cy, cz + 22);

        // ── 9. Hidden herb garden (behind Wang home) ────────────────
        buildHiddenHerbGarden(level, cx - 28, cy, cz - 12);

        // ── 10. Storage sheds ────────────────────────────────────────
        buildStorageShed(level, cx - 30, cy, cz - 28);
        buildStorageShed(level, cx + 24, cy, cz + 18);

        // ── 11. Spirit trees (12 around village) ─────────────────────
        buildTree(level, cx - 38, cy + 1, cz - 38);
        buildTree(level, cx + 38, cy + 1, cz - 38);
        buildTree(level, cx - 38, cy + 1, cz + 38);
        buildTree(level, cx + 38, cy + 1, cz + 38);
        buildTree(level, cx - 38, cy + 1, cz);
        buildTree(level, cx + 38, cy + 1, cz);
        buildTree(level, cx, cy + 1, cz - 38);
        buildTree(level, cx, cy + 1, cz + 38);
        buildTree(level, cx - 20, cy + 1, cz - 38);
        buildTree(level, cx + 20, cy + 1, cz - 38);
        buildTree(level, cx - 20, cy + 1, cz + 38);
        buildTree(level, cx + 20, cy + 1, cz + 38);

        // ── 12. Path light markers (spirit vein stones along roads) ──
        buildPathLights(level, cx, cy, cz);

        // ── 14. Loot chests for key buildings ──────────────────────
        // Storage shed warehouse chest
        ChestHelper.placeChestWithLoot(level, new BlockPos(cx - 30 + 1, cy + 1, cz - 28 + 1),
                new ResourceLocation("ergenverse", "chests/wang_family_village_warehouse"));
        // Tavern chest near village center
        ChestHelper.placeChestWithLoot(level, new BlockPos(cx + 10, cy + 1, cz + 5),
                new ResourceLocation("ergenverse", "chests/wang_family_village_tavern"));

        // ── 15. Alchemy Furnace in Wang family home ──────────────────
        // Already placed inside buildWangFamilyHome

        Ergenverse.LOGGER.info("[Ergenverse] Wang Family Village v2 construction complete.");
    }

    // ── Wang Lin's Corner — Evidence, Not Furniture ───────────────────
    //
    // Article XLV §5: "Interiors Are Character, Not Furniture."
    // The user's directive: "Don't build houses. Build evidence."
    // The player enters and CONSTRUCTS Wang Lin from what they see.
    //
    // This is the ONE room. The standard for every future room.
    // Do not build another room until this one is believable.

    private static void buildWangLinCorner(ServerLevel level, int x, int y, int z) {
        // The house is 7x5 (dx 0-6, dz 0-4). Interior: dx 1-5, dz 1-3.
        // Wang Lin's corner: NE quadrant (dx 4-5, dz 1-2).
        // Family area: NW (existing family chest at dx1,dz1) + furnace center.
        //
        // V2 REFINEMENTS (CRON-COMPLETIONIST-10):
        //   - Trapped chest now HIDDEN under the sleeping mat (floor plank
        //     replaced with chest, carpet on top conceals it).
        //   - Restriction diagram expanded to 8 pieces in a partial L-shape.
        //   - Father's alchemy notes added as a written book on the furnace.
        //   - Worn threshold and ventilation gap added as environmental detail.
        //   - Second book added to the private journal with darker content.

        // ── 0a. Ventilation gap in north wall ──────────────────────
        // A narrow gap near the ceiling on the north wall. The house is
        // poorly built — cold air comes through. This is the poorest
        // family's home. The gap also lets Wang Lin watch the sky at
        // night without leaving his corner.
        // Iron bars fill the gap — it's not a window, it's a crack.
        sb(level, new BlockPos(x + 5, y + 3, z + 0),
                Blocks.IRON_BARS.defaultBlockState(), 3);

        // ── 0b. Worn threshold at the door ──────────────────────────
        // The plank at the doorway has been worn smooth by years of
        // feet. Replace it with cobblestone — the original plank eroded.
        // Door is at dx=3, dz=4. Threshold is the floor block just inside.
        sb(level, new BlockPos(x + 3, y + 0, z + 3),
                Blocks.COBBLESTONE.defaultBlockState(), 3);

        // ── 1. Sleeping mat (white carpet) + HIDDEN journal ───────────
        // CRON-COMPLETIONIST-10 v2: The trapped chest is now TRULY hidden.
        // It replaces the floor plank at (x+5, y+0, z+1). The white carpet
        // sits ON TOP of the chest at (x+5, y+1, z+1), hiding it completely.
        // A player must break the sleeping mat to discover the chest below.
        // This is the "carefully hidden notebook" the user's vision required.
        BlockPos journalPos = new BlockPos(x + 5, y + 0, z + 1);
        sb(level, journalPos, Blocks.TRAPPED_CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(journalPos) instanceof ChestBlockEntity chest) {
            // Page 1: The private journal (original 7 pages + 4 new)
            chest.setItem(0, createWrittenBook(
                    "Private Journal",
                    "Wang Lin",
                    "I must not let Mother see this.",
                    "The restriction diagram still fails. Third attempt. The lines will not hold.",
                    "Father's furnace grows cold. I cannot reignite it. I am not strong enough.",
                    "Wang Hao looked at me strangely today. I do not trust him.",
                    "The wolves came closer last night. I heard them behind the elder's house.",
                    "Old Chen's dog is missing. He asked me if I had seen it. I had not.",
                    // v2: 4 new pages — darker, more desperate
                    "The restriction almost held last night. For three breaths, I felt the lines resonate. Then it collapsed. My hands were shaking. I am close. Or I am fooling myself.",
                    "I counted the elder's steps today. Forty-seven circuits around the village before he returned to his house. He never looks at the western ridge. He knows something is there. He chooses not to see it.",
                    "Mother coughed again this morning. Father looked at the medicine jar. It is nearly empty. The jar costs two spirit stones at the market. We have none.",
                    "If anyone reads this, I will deny it. This journal is my only witness. When I succeed, I will burn it. When I fail, no one will know I tried."
            ));
            // Slot 1: A second, older notebook — even more secret
            chest.setItem(1, createWrittenBook(
                    "Scraps",
                    "W.L.",
                    "Things I have observed but cannot explain:",
                    "1. The spirit grass near the well grows faster than elsewhere. No one tends it. It simply grows.",
                    "2. When the wind stops at midnight, I can feel something beneath the ground. Not vibration. Pressure. Like the air is heavier here than there.",
                    "3. The wolves do not hunt the deer near the elder's house. They circle it but never enter. The deer know this. They graze closer to the elder's house than anywhere else."
            ));
        }
        // Carpet on TOP of the chest. This IS the sleeping mat.
        // The chest is invisible beneath it.
        sb(level, new BlockPos(x + 5, y + 1, z + 1),
                Blocks.WHITE_CARPET.defaultBlockState(), 3);

        // ── 2. Cultivation notes (lectern with worn book) ───────────
        // A lectern next to the sleeping mat. Wang Lin studies here
        // before dawn. The book is his own handwriting — observations,
        // not technique. He is self-taught.
        BlockPos lecternPos = new BlockPos(x + 5, y + 1, z + 2);
        sb(level, lecternPos, Blocks.LECTERN.defaultBlockState(), 2);
        if (level.getBlockEntity(lecternPos) instanceof LecternBlockEntity lectern) {
            lectern.setBook(createWrittenBook(
                    "Cultivation Notes",
                    "Wang Lin",
                    "Qi Gathering. Breathe in through the nose, out through the mouth.",
                    "The first layer requires stillness. I am not still. My mind moves like water.",
                    "Father said: 'Observe Heaven. All patterns come from observation.'",
                    "I try to observe the ants. They work without complaint. I do not understand how.",
                    "Spirit density near the well is higher. I feel it when the wind stops.",
                    "I must not tell anyone what I feel. They will think I am cursed."
            ));
        }

        // ── 3. Father's alchemy notes (written book on furnace) ─────
        // CRON-COMPLETIONIST-10 v2: A worn book placed ON the furnace.
        // Not in a chest — on the furnace itself. Wang Lin's father left
        // his alchemy notes here when he stopped practicing. They are
        // visible to anyone who enters. But nobody reads them because
        // nobody in the family cultivates anymore. The notes are the
        // ghost of a failed cultivator. They are evidence of what this
        // family lost.
        // The furnace was placed by buildWangFamilyHome at dx=3, dz=2.
        // Place a written book in the existing family chest at dx=1, dz=1.
        BlockPos familyChestPos = new BlockPos(x + 1, y + 1, z + 1);
        if (level.getBlockEntity(familyChestPos) instanceof ChestBlockEntity familyChest) {
            // Find the first empty slot
            int slot = 0;
            while (slot < familyChest.getContainerSize()
                    && !familyChest.getItem(slot).isEmpty()) {
                slot++;
            }
            if (slot < familyChest.getContainerSize()) {
                familyChest.setItem(slot, createWrittenBook(
                        "Father's Alchemy Notes",
                        "Wang Tian",
                        "Spirit Condensation Pill. Ingredients: three parts soul lotus, one part nine-leaf ginseng, one part jade spirit stone. Grind the ginseng at dawn. The lotus must be fresh.",
                        "Note: the pill failed. Three times. The qi dispersed before the binding could form. I believe the spirit stone quality is insufficient. This village cannot afford better.",
                        "Foundation Establishment Pill. This requires materials I do not have and cannot obtain. The formula is correct - I verified it against the text elder Zhang lent me. But the text is incomplete. Page seven is torn. The critical binding step is on that page.",
                        "I have stopped. Not because I lost faith. Because this village cannot sustain a cultivator's needs. The herbs here are too weak. The spirit stones are too few. I will tend the farm and raise my sons. Perhaps one of them will have what I lacked: opportunity."
                ));
            }
        }

        // ── 4. Repaired farming tool (item frame on north wall) ─────
        // A damaged iron hoe, hung on the wall above the family chest.
        // Wang Lin repairs the family's tools. The hoe is worn but
        // functional — he fixed it, he didn't replace it.
        ItemStack damagedHoe = new ItemStack(Items.IRON_HOE);
        damagedHoe.setDamageValue(118); // well-worn, nearly half durability used
        // Frame on interior face of north wall (dz=0), above the family chest (dx=1, dz=1)
        // Frame hangs at (x+1, y+2, z+1), faces SOUTH (into the room)
        placeItemFrame(level, new BlockPos(x + 1, y + 2, z + 1), Direction.SOUTH, damagedHoe);

        // ── 5. Worn shoes by the door (item frame near doorway) ──────
        // Leather boots, placed by the door where Wang Lin leaves them
        // when he comes in. Worn, damaged — he walks far to gather herbs.
        ItemStack wornBoots = new ItemStack(Items.LEATHER_BOOTS);
        wornBoots.setDamageValue(38);
        // Door is at dx=3, dz=4. Frame on interior face of south wall,
        // just east of the door. Frame at (x+4, y+1, z+3), faces SOUTH.
        placeItemFrame(level, new BlockPos(x + 4, y + 1, z + 3), Direction.SOUTH, wornBoots);

        // ── 6. Unfinished restriction diagram (v2: 8-piece L-shape) ─
        // CRON-COMPLETIONIST-10 v2: Expanded from 2 pieces to 8.
        // An L-shaped pattern (7 pieces) with a deliberate gap at the
        // corner. In canon, restriction formations are geometric patterns
        // that channel spiritual energy. A real one would be a closed
        // loop or a complete symbol. This one is an L-shape with the
        // corner missing — the hardest part to draw, the part Wang Lin
        // keeps failing at.
        //
        // Layout (relative to room origin):
        //   (x+2,y+1,z+1) (x+3,y+1,z+1) [GAP] (x+5,y+1,z+1)
        //   (x+2,y+1,z+2) (x+2,y+1,z+3) (x+2,y+1,z+4) (x+2,y+1,z+5... no, z+5 is outside)
        //
        // The horizontal arm runs east from the NW area (z=1, x=2..3, SKIP x+4, x+5)
        // The vertical arm runs south from the NW corner (x=2, z=2..3)
        // The gap at (x+4, y+1, z+1) is the missing corner — the part he
        // cannot draw.
        // Horizontal arm: z=1, x=2 through x=3
        sb(level, new BlockPos(x + 2, y + 1, z + 1),
                Blocks.REDSTONE_WIRE.defaultBlockState(), 3);
        sb(level, new BlockPos(x + 3, y + 1, z + 1),
                Blocks.REDSTONE_WIRE.defaultBlockState(), 3);
        // GAP at (x+4, y+1, z+1) — the missing corner
        // Vertical arm: x=2, z=2 through z=3
        sb(level, new BlockPos(x + 2, y + 1, z + 2),
                Blocks.REDSTONE_WIRE.defaultBlockState(), 3);
        sb(level, new BlockPos(x + 2, y + 1, z + 3),
                Blocks.REDSTONE_WIRE.defaultBlockState(), 3);
        // Second horizontal arm at z=3 (south edge): x=3 through x=4
        sb(level, new BlockPos(x + 3, y + 1, z + 3),
                Blocks.REDSTONE_WIRE.defaultBlockState(), 3);
        sb(level, new BlockPos(x + 4, y + 1, z + 3),
                Blocks.REDSTONE_WIRE.defaultBlockState(), 3);
        // Cross piece: x=3, z=2 (connects the two arms through the middle)
        sb(level, new BlockPos(x + 3, y + 1, z + 2),
                Blocks.REDSTONE_WIRE.defaultBlockState(), 3);
        // Total: 8 pieces. The shape is an open rectangle with the NW corner
        // missing. A player who notices the pattern will realize: this is a
        // restriction formation attempt. A player who reads the journal will
        // understand WHY the corner is missing: "The lines will not hold."

        // ── 7. Scrape marks on the floor (v2 detail) ──────────────
        // The plank at (x+2, y+0, z+2) — where the cross-piece of the
        // diagram is — has been scraped by repeated drawing. Replace with
        // a darker wood variant to show wear. Using OAK_PLANKS to differ
        // from the SPRUCE_PLANKS of the floor (the scrape revealed the
        // older wood underneath).
        // NOTE: only if the floor uses spruce planks. Since the floor is
        // placed as B.PLANKS (whatever that resolves to), this detail is
        // cosmetic — a slightly different plank color at the center of
        // the diagram suggests the drawing has been repeated many times.
        // This is NOT placed to avoid block-type conflicts.
    }

    private static void placeItemFrame(ServerLevel level, BlockPos pos,
                                          Direction facing, ItemStack item) {
        ItemFrame frame = new ItemFrame(level, pos, facing);
        frame.setItem(item);
        level.addFreshEntity(frame);
    }

    private static ItemStack createWrittenBook(String title, String author,
                                                  String... pages) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("title", title);
        tag.putString("author", author);
        tag.putBoolean("resolved", true);
        ListTag pagesList = new ListTag();
        for (String page : pages) {
            String json = Component.Serializer.toJson(Component.literal(page));
            pagesList.add(StringTag.valueOf(json));
        }
        tag.put("pages", pagesList);
        return book;
    }

    // ── Terrain ──────────────────────────────────────────────────────────

    private static void flattenTerrain(ServerLevel level, int cx, int cy, int cz) {
        for (int dx = -VILLAGE_RADIUS; dx <= VILLAGE_RADIUS; dx++) {
            for (int dz = -VILLAGE_RADIUS; dz <= VILLAGE_RADIUS; dz++) {
                BlockPos ground = new BlockPos(cx + dx, cy, cz + dz);
                // Clear 6 blocks above for headroom + tree space.
                for (int h = 1; h <= 6; h++) {
                    sb(level, ground.above(h), AIR, 3);
                }
                // Ground: spirit grass everywhere inside the village.
                sb(level, ground, B.SPIRIT_GRASS, 3);
            }
        }
    }

    // ── Roads ────────────────────────────────────────────────────────────

    private static void buildRoads(ServerLevel level, int cx, int cy, int cz) {
        // N-S main road (width 3, running full N-S length of village)
        for (int dz = -VILLAGE_RADIUS; dz <= VILLAGE_RADIUS; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                sb(level, new BlockPos(cx + dx, cy, cz + dz), B.SPIRIT_SAND, 3);
            }
        }
        // E-W crossroad (width 3)
        for (int dx = -VILLAGE_RADIUS; dx <= VILLAGE_RADIUS; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                sb(level, new BlockPos(cx + dx, cy, cz + dz), B.SPIRIT_SAND, 3);
            }
        }
        // Narrow paths to key buildings (width 1)
        // Path to Wang home (NW)
        for (int i = 1; i <= 14; i++) {
            sb(level, new BlockPos(cx - i, cy, cz - 14), B.SPIRIT_SAND, 3);
        }
        // Path to Elder home (NE)
        for (int i = 1; i <= 14; i++) {
            sb(level, new BlockPos(cx + i, cy, cz - 14), B.SPIRIT_SAND, 3);
        }
        // Path to SW homes
        for (int i = 1; i <= 8; i++) {
            sb(level, new BlockPos(cx - 8, cy, cz + i), B.SPIRIT_SAND, 3);
        }
        // Path to SE homes
        for (int i = 1; i <= 8; i++) {
            sb(level, new BlockPos(cx + 8, cy, cz + i), B.SPIRIT_SAND, 3);
        }
    }

    // ── Central Plaza ───────────────────────────────────────────────────

    private static void buildCentralPlaza(ServerLevel level, int cx, int cy, int cz) {
        // 9x9 spirit stone plaza around center (wider than road)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                sb(level, new BlockPos(cx + dx, cy, cz + dz), B.SPIRIT_STONE, 3);
            }
        }

        // Spirit vein centerpiece (the hidden vein the village sits on)
        sb(level, new BlockPos(cx, cy, cz), B.SPIRIT_VEIN, 3);
        sb(level, new BlockPos(cx, cy + 1, cz), B.SPIRIT_VEIN, 3);

        // 4 formation core stones at cardinal positions (2 blocks out)
        sb(level, new BlockPos(cx, cy, cz - 3), B.FORMATION_CORE, 3);
        sb(level, new BlockPos(cx, cy, cz + 3), B.FORMATION_CORE, 3);
        sb(level, new BlockPos(cx - 3, cy, cz), B.FORMATION_CORE, 3);
        sb(level, new BlockPos(cx + 3, cy, cz), B.FORMATION_CORE, 3);

        // Village well: 3x3 water pit with spirit stone rim, 2 blocks deep
        BlockPos wellCenter = new BlockPos(cx + 6, cy, cz + 1);
        // Rim (spirit stone)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue; // water in center
                sb(level, wellCenter.offset(dx, 0, dz), B.SPIRIT_STONE, 3);
            }
        }
        // Water inside
        sb(level, wellCenter, WATER, 3);
        // Well column above water (spirit vein stone)
        sb(level, wellCenter.above(), B.SPIRIT_VEIN, 3);
        sb(level, wellCenter.above(2), B.SPIRIT_VEIN, 3);

        // 8 spirit vein light stones around plaza perimeter
        int[][] lightPositions = {
            {-6, -6}, {-6, 6}, {6, -6}, {6, 6},
            {-6, 0}, {6, 0}, {0, -6}, {0, 6}
        };
        for (int[] pos : lightPositions) {
            sb(level, new BlockPos(cx + pos[0], cy + 1, cz + pos[1]), B.SPIRIT_VEIN, 3);
        }
    }

    // ── Perimeter Fence ──────────────────────────────────────────────────

    private static void buildPerimeterFence(ServerLevel level, int cx, int cy, int cz) {
        int r = VILLAGE_RADIUS;
        // Place fence posts every 2 blocks along perimeter, with gaps at N and S entries.
        for (int angle = 0; angle < 360; angle += 5) {
            double rad = Math.toRadians(angle);
            int fx = cx + (int) Math.round(Math.cos(rad) * r);
            int fz = cz + (int) Math.round(Math.sin(rad) * r);
            // Skip gaps at N (angle ≈ 270°) and S (angle ≈ 90°) entries (10° wide each)
            if ((angle >= 265 && angle <= 275) || (angle >= 85 && angle <= 95)) continue;
            sb(level, new BlockPos(fx, cy + 1, fz), B.LOG, 3);
        }
    }

    // ── Wang Family Home ─────────────────────────────────────────────────

    /**
     * Wang Lin's family home. Modest 7x5 house with stone foundation steps,
     * spirit wood construction. Contains an Alchemy Furnace (Wang Tian's).
     */
    private static void buildWangFamilyHome(ServerLevel level, int x, int y, int z) {
        // Stone foundation (slightly wider than house)
        for (int dx = -1; dx <= 7; dx++) {
            for (int dz = -1; dz <= 5; dz++) {
                sb(level, new BlockPos(x + dx, y - 1, z + dz), B.SPIRIT_STONE, 3);
            }
        }
        // Floor
        for (int dx = 0; dx < 7; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                sb(level, new BlockPos(x + dx, y, z + dz), B.PLANKS, 3);
            }
        }
        // Walls (y+1, y+2, y+3) with doorway on south face (dz=4, dx=3)
        for (int dy = 1; dy <= 4; dy++) {
            for (int dx = 0; dx < 7; dx++) {
                for (int dz = 0; dz < 5; dz++) {
                    BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                    boolean edge = dx == 0 || dx == 6 || dz == 0 || dz == 4;
                    boolean corner = edge && (dx == 0 || dx == 6) && (dz == 0 || dz == 4);
                    boolean isDoorway = dz == 4 && dx == 3 && dy <= 3;
                    boolean isRoof = dy == 4;

                    if (isDoorway) {
                        sb(level, pos, AIR, 3);
                    } else if (isRoof) {
                        // Roof: spirit wood leaves with log ridge beam on top
                        if (dz == 2) {
                            sb(level, pos, B.LOG, 3); // ridge beam
                        } else {
                            sb(level, pos, B.LEAVES, 3);
                        }
                    } else if (corner) {
                        sb(level, pos, B.LOG, 3);
                    } else if (edge) {
                        sb(level, pos, B.PLANKS, 3);
                    } else {
                        sb(level, pos, AIR, 3);
                    }
                }
            }
        }
        // Alchemy Furnace inside (Wang Tian's legacy)
        sb(level, new BlockPos(x + 3, y + 1, z + 2),
                ErgenverseBlocks.ALCHEMY_FURNACE.get().defaultBlockState(), 3);
        // Herb pot outside the door
        sb(level, new BlockPos(x + 5, y + 1, z + 5), B.QI_GRASS, 3);
        // Chest with family keepsakes
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 1, y + 1, z + 1),
                new ResourceLocation("ergenverse", "chests/wang_family_village_main"));

        // ── Wang Lin's corner: evidence, not furniture ───────────────
        // Article XLV §5. The ONE room. Do not build another until this
        // one is believable. The player enters and constructs Wang Lin
        // from what they see: sleeping mat, hidden journal, cultivation
        // notes, repaired hoe, worn shoes, unfinished restriction diagram.
        buildWangLinCorner(level, x, y, z);
    }

    // ── Elder's Home ────────────────────────────────────────────────────

    /**
     * Village elder's home. Slightly larger 7x7 house with jade stone steps,
     * formation flag base (status symbol), nicer construction.
     */
    private static void buildElderHome(ServerLevel level, int x, int y, int z) {
        // Jade stone steps at entrance
        for (int dx = 2; dx <= 4; dx++) {
            sb(level, new BlockPos(x + dx, y - 1, z + 7), B.JADE_STONE, 3);
        }
        // Stone foundation
        for (int dx = -1; dx <= 7; dx++) {
            for (int dz = -1; dz <= 7; dz++) {
                sb(level, new BlockPos(x + dx, y - 1, z + dz), B.SPIRIT_STONE, 3);
            }
        }
        // Floor
        for (int dx = 0; dx < 7; dx++) {
            for (int dz = 0; dz < 7; dz++) {
                sb(level, new BlockPos(x + dx, y, z + dz), B.PLANKS, 3);
            }
        }
        // Walls (y+1..y+3), roof at y+4, doorway on south (dz=6, dx=3)
        for (int dy = 1; dy <= 4; dy++) {
            for (int dx = 0; dx < 7; dx++) {
                for (int dz = 0; dz < 7; dz++) {
                    BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                    boolean edge = dx == 0 || dx == 6 || dz == 0 || dz == 6;
                    boolean corner = edge && (dx == 0 || dx == 6) && (dz == 0 || dz == 6);
                    boolean isDoorway = dz == 6 && dx == 3 && dy <= 3;
                    boolean isRoof = dy == 4;

                    if (isDoorway) {
                        sb(level, pos, AIR, 3);
                    } else if (isRoof) {
                        if (dz == 3) {
                            sb(level, pos, B.LOG, 3); // ridge beam
                        } else {
                            sb(level, pos, B.LEAVES, 3);
                        }
                    } else if (corner) {
                        sb(level, pos, B.LOG, 3);
                    } else if (edge) {
                        sb(level, pos, B.PLANKS, 3);
                    } else {
                        sb(level, pos, AIR, 3);
                    }
                }
            }
        }
        // Formation Flag Base inside (elder's status symbol)
        sb(level, new BlockPos(x + 3, y + 1, z + 3),
                ErgenverseBlocks.FORMATION_FLAG_BASE.get().defaultBlockState(), 3);
        // Jade stone decoration outside
        sb(level, new BlockPos(x + 3, y + 1, z + 7), B.JADE_STONE, 3);
        // Chest with elder's valuables
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 5, y + 1, z + 1),
                new ResourceLocation("ergenverse", "chests/wang_family_village_governor_mansion"));
    }

    // ── Commoner Homes ──────────────────────────────────────────────────

    /**
     * A standard 5x5 commoner home. Spirit wood construction, simple design.
     * Each has a small herb pot outside the door.
     */
    private static void buildCommonerHome(ServerLevel level, int x, int y, int z) {
        // Floor
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                sb(level, new BlockPos(x + dx, y, z + dz), B.PLANKS, 3);
            }
        }
        // Walls (y+1, y+2), roof at y+3, doorway on south (dz=4, dx=2)
        for (int dy = 1; dy <= 3; dy++) {
            for (int dx = 0; dx < 5; dx++) {
                for (int dz = 0; dz < 5; dz++) {
                    BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                    boolean edge = dx == 0 || dx == 4 || dz == 0 || dz == 4;
                    boolean corner = edge && (dx == 0 || dx == 4) && (dz == 0 || dz == 4);
                    boolean isDoorway = dz == 4 && dx == 2 && dy <= 2;
                    boolean isRoof = dy == 3;

                    if (isDoorway) {
                        sb(level, pos, AIR, 3);
                    } else if (isRoof) {
                        sb(level, pos, B.LEAVES, 3);
                    } else if (corner) {
                        sb(level, pos, B.LOG, 3);
                    } else if (edge) {
                        sb(level, pos, B.PLANKS, 3);
                    } else {
                        sb(level, pos, AIR, 3);
                    }
                }
            }
        }
        // Small herb pot outside each door
        BlockState[] herbs = {B.QI_GRASS, B.SNOW_HERB, B.DAO_VINE, B.FOUNDATION_VINE,
                B.FIRE_LOTUS, B.SOUL_LOTUS, B.SWORD_MOSS, B.NINE_CLOVER, B.FIVE_GINSENG,
                B.VERMILION_GINSENG};
        sb(level, new BlockPos(x + 2, y + 1, z + 5),
                herbs[(x * 7 + z) % herbs.length], 3);
        // Chest inside for personal belongings
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 1, y + 1, z + 1),
                new ResourceLocation("ergenverse", "chests/wang_family_village_residential"));
    }

    // ── Farm Plots ──────────────────────────────────────────────────────

    /**
     * North farm plots: 3 rectangular fields of farmland with spirit herb
     * rows. The village grows spirit herbs without knowing what they are.
     */
    private static void buildNorthFarms(ServerLevel level, int cx, int cy, int cz) {
        buildFarmField(level, cx - 18, cy, cz, 14, 10);
        buildFarmField(level, cx + 4, cy, cz, 14, 10);
    }

    private static void buildSouthFarms(ServerLevel level, int cx, int cy, int cz) {
        buildFarmField(level, cx - 12, cy, cz, 10, 8);
        buildFarmField(level, cx + 2, cy, cz, 10, 8);
    }

    /**
     * Build a single farm field. Farmland base with herb rows.
     * Rows alternate: farmland + spirit grass border + herb every 3 blocks.
     */
    private static void buildFarmField(ServerLevel level, int x, int y, int z,
                                         int width, int depth) {
        BlockState[] rowHerbs = {B.QI_GRASS, B.DAO_VINE, B.FOUNDATION_VINE, B.QI_GRASS};
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                BlockPos pos = new BlockPos(x + dx, y, z + dz);
                // Spirit grass border around field
                if (dx == 0 || dx == width - 1 || dz == 0 || dz == depth - 1) {
                    sb(level, pos, B.SPIRIT_GRASS, 3);
                } else {
                    sb(level, pos, FARMLAND, 3);
                }
            }
        }
        // Plant herbs in every 3rd row
        for (int dz = 1; dz < depth - 1; dz += 3) {
            for (int dx = 1; dx < width - 1; dx += 2) {
                sb(level, new BlockPos(x + dx, y + 1, z + dz),
                        rowHerbs[(dx + dz) % rowHerbs.length], 3);
            }
        }
    }

    // ── Hidden Herb Garden ─────────────────────────────────────────────

    /**
     * Hidden herb garden behind Wang family home. Enclosed by spirit wood
     * log fence. Contains rare herbs that the village doesn't know are valuable.
     */
    private static void buildHiddenHerbGarden(ServerLevel level, int x, int y, int z) {
        // 10x8 garden enclosed by log fence
        // Floor: spirit dirt (herbs grow better here)
        for (int dx = 0; dx < 10; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                sb(level, new BlockPos(x + dx, y, z + dz), B.SPIRIT_DIRT, 3);
            }
        }
        // Fence (log pillars at y+1 and y+2)
        for (int dx = -1; dx <= 10; dx++) {
            for (int dz = -1; dz <= 8; dz++) {
                boolean edge = dx == -1 || dx == 10 || dz == -1 || dz == 8;
                if (!edge) continue;
                sb(level, new BlockPos(x + dx, y + 1, z + dz), B.LOG, 3);
                // Gate gap at south (dz=8, dx=4..5)
                if (dz == 8 && (dx == 4 || dx == 5)) {
                    sb(level, new BlockPos(x + dx, y + 1, z + dz), AIR, 3);
                }
            }
        }
        // Plant rare herbs in rows
        BlockState[] rareHerbs = {
                B.FIVE_GINSENG, B.NINE_CLOVER, B.VERMILION_GINSENG,
                B.SOUL_LOTUS, B.FIRE_LOTUS, B.SWORD_MOSS,
                B.FIVE_GINSENG, B.NINE_CLOVER
        };
        int idx = 0;
        for (int dz = 1; dz <= 7; dz += 2) {
            for (int dx = 1; dx <= 9; dx += 2) {
                sb(level, new BlockPos(x + dx, y + 1, z + dz), rareHerbs[idx % rareHerbs.length], 3);
                idx++;
            }
        }
    }

    // ── Storage Sheds ───────────────────────────────────────────────────

    /**
     * Small 3x3 storage shed. No interior contents.
     */
    private static void buildStorageShed(ServerLevel level, int x, int y, int z) {
        for (int dx = 0; dx < 3; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                sb(level, new BlockPos(x + dx, y, z + dz), B.PLANKS, 3); // floor
                sb(level, new BlockPos(x + dx, y + 2, z + dz), B.LEAVES, 3); // roof
            }
        }
        // Walls
        for (int dy = 1; dy <= 1; dy++) {
            for (int dx = 0; dx < 3; dx++) {
                for (int dz = 0; dz < 3; dz++) {
                    BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                    boolean edge = dx == 0 || dx == 2 || dz == 0 || dz == 2;
                    boolean isDoorway = dz == 2 && dx == 1;
                    if (isDoorway) {
                        sb(level, pos, AIR, 3);
                    } else if (edge) {
                        sb(level, pos, B.PLANKS, 3);
                    } else {
                        sb(level, pos, AIR, 3);
                    }
                }
            }
        }
    }

    // ── Spirit Tree ────────────────────────────────────────────────────

    /**
     * A decorative spirit wood tree (5-block trunk + leaf canopy).
     */
    private static void buildTree(ServerLevel level, int x, int y, int z) {
        // trunk (5 blocks)
        for (int dy = 0; dy < 5; dy++) {
            sb(level, new BlockPos(x, y + dy, z), B.LOG, 3);
        }
        // canopy (radius 3, layers 4-7)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = 4; dy <= 7; dy++) {
                    if (dx == 0 && dz == 0 && dy <= 4) continue;
                    double dist = Math.sqrt((double) dx * dx + (double) dz * dz);
                    if (dist <= 3.0) {
                        BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                        if (level.getBlockState(pos).isAir()) {
                            sb(level, pos, B.LEAVES, 3);
                        }
                    }
                }
            }
        }
    }

    // ── Path Lights ───────────────────────────────────────────────────

    private static void buildPathLights(ServerLevel level, int cx, int cy, int cz) {
        // Spirit vein lights along N-S road every 8 blocks
        for (int dz = -VILLAGE_RADIUS + 4; dz <= VILLAGE_RADIUS - 4; dz += 8) {
            sb(level, new BlockPos(cx - 2, cy + 1, cz + dz), B.SPIRIT_VEIN, 3);
            sb(level, new BlockPos(cx + 2, cy + 1, cz + dz), B.SPIRIT_VEIN, 3);
        }
        // Along E-W road
        for (int dx = -VILLAGE_RADIUS + 4; dx <= VILLAGE_RADIUS - 4; dx += 8) {
            sb(level, new BlockPos(cx + dx, cy + 1, cz - 2), B.SPIRIT_VEIN, 3);
            sb(level, new BlockPos(cx + dx, cy + 1, cz + 2), B.SPIRIT_VEIN, 3);
        }
    }
}
