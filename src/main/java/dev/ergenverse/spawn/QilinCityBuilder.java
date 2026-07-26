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
 * QilinCityBuilder — FULLY hand-built Qilin City (麒麟城 / Qilin City).
 *
 * <p>Constitution: "The world is completely hand-crafted, accurate to the novels.
 * NEVER write a script that replaces vanilla blocks with other blocks as a shortcut.
 * Every structure must be hand-authored."
 *
 * <p><b>Canon basis (Renegade Immortal, 修魔海 arc):</b> Qilin City is a
 * trading city in the 修魔海 (Sea of Devils) region. Named for the divine
 * Qilin beasts that are sacred to the city. The city is known for its wealth
 * from qilin-related trade (qilin scales, spirit beast parts, cultivation
 * resources). The city governor is a late Foundation Formation cultivator.
 * The city hosts an annual Qilin Festival where beasts are displayed and
 * traded. Per CRON-69 canon corrections: 修魔海 (not "Sea of Devils" as a
 * literal translation — the Chinese name is canonical).
 *
 * <p>Architectural style: Gold-veined quartz, jade inlay, amber stone, polished
 * spirit stone. Distinct from Tian Shui's elegant granite/birch or Teng City's
 * cobblestone/martial style. Qilin City is ORNATE — gold trim, jade pillars,
 * amber terraces, spirit stone roads. This reflects the city's wealth and its
 * sacred connection to qilin beasts.
 *
 * <h2>CRON-COMPLETIONIST-72 — chunk-scoped migration</h2>
 * Same migration as the other 3 ServerLevel-only builders (LuoHeSect,
 * SnowDomainCapital, VermilionBirdImperialCity). Key difference: this builder
 * had <b>239 raw {@code level.setBlock} calls</b> inline (no set() helper),
 * unlike the other 3 which had a single set() helper. A Python script
 * (cron72_qilin_setblock_rewrite.py) mechanically rewrote every
 * {@code level.setBlock(} to {@code set(level, }, then this class adds the
 * {@code set()} overload that delegates to {@link #sb}. This is the same
 * pattern as CRON-71's ChestHelper mechanical fix — a scriptable
 * transformation that preserves argument shapes.
 *
 * <p>Highlights:
 * <ul>
 *   <li>Coordinates now source from {@link PlanetSuzakuBlueprint#QILIN_CITY}
 *       (1800, 0, -2600). Prior to CRON-72, they were computed as
 *       {@code VILLAGE_X + 2400 = 6242} and {@code VILLAGE_Z + 800 = -384},
 *       placing the city at (6242, ?, -384) — wildly off-canon.</li>
 *   <li>All 239 placements now flow through {@link #sb} (chunk filter +
 *       provenance guard via {@link WorldDeltaStore}). The single
 *       {@code level.setBlock} call left in the file is inside {@code sb()}
 *       itself — the actual placement call.</li>
 *   <li>Marker block: GOLD_BLOCK at {@code center.above(WALL_HEIGHT + 2)} —
 *       preserves the prior landmark beacon position (which stacked
 *       QUARTZ_PILLAR + GOLD_BLOCK + SOUL_LANTERN). The isAlreadyBuilt check
 *       looks for GOLD_BLOCK at this position.</li>
 * </ul>
 *
 * <p>Districts (11): city_walls, main_streets, east_gate (main), qilin_plaza
 * (central), beast_market (northwest), merchant_district (east), mortal_quarter
 * (south), residential_district (southwest), governor_mansion (north), temple_district
 * (northeast), tavern_district (central-south), smuggler_tunnels (underground
 * entrance near port).
 *
 * <p>CRON-COMPLETIONIST-51: Fifth full hand-built settlement. Per Article XXIII,
 * this completes the 5th major canon location, bringing total settlements to 5
 * (Wang Family Village, Heng Yue Sect, Teng Family City, Tian Shui City, Qilin City).
 *
 * HARSH SELF-CRITIQUE:
 *   - All buildings are box-based (Minecraft limitation — no curved walls, no domes).
 *   - No actual NPCs spawn here — the builder only places blocks. NPC spawning is
 *     handled by the ReificationScan/ActorMaterializer system separately.
 *   - No interior furnishings in most buildings — counters, shelves, and decoration
 *     are sparse. The NBT structures (11 .nbt files) exist for richer interiors
 *     but are not loaded by this builder (the worldgen Jigsaw system handles those).
 *   - No sign text — walls are blank blocks. In canon, the city gate would have
 *     "麒麟城" carved in gold. SignBlockEntity manipulation is deferred.
 *   - City is flat — no elevation changes, no hills, no streams. A real city would
 *     have terrain variation. The TerrainSpiritifier can add some post-processing.
 *   - The smuggler tunnels are represented as a small basement, not a true underground
 *     network. A real smuggler network would span hundreds of blocks.
 *   - No connected road network to other settlements. The city exists in isolation.
 */

public final class QilinCityBuilder {

    /**
     * Lazy-initialized BlockState holder. ErgenverseBlocks.X.get() throws NPE before
     * Forge resolves the block registry, so these cannot be static-final in the outer
     * class. This inner class loads on first reference (during build(), which runs at
     * world-gen time — well after registry resolution).
     */
    private static final class B {
        private static final BlockState SPIRIT_STONE = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
        private static final BlockState SPIRIT_STONE_STAIRS = ErgenverseBlocks.SPIRIT_STONE_STAIRS.get().defaultBlockState();
        private static final BlockState SPIRIT_STONE_SLAB = ErgenverseBlocks.SPIRIT_STONE_SLAB.get().defaultBlockState();
        private static final BlockState SPIRIT_STONE_WALL = ErgenverseBlocks.SPIRIT_STONE_WALL.get().defaultBlockState();
        private static final BlockState JADE_STONE = ErgenverseBlocks.JADE_STONE.get().defaultBlockState();
        private static final BlockState SPIRIT_DIRT = ErgenverseBlocks.SPIRIT_DIRT.get().defaultBlockState();
        private static final BlockState SPIRIT_GRASS = ErgenverseBlocks.SPIRIT_GRASS.get().defaultBlockState();
    }

    private QilinCityBuilder() {}

    // ── Canonical position on Planet Suzaku (CRON-72: from blueprint) ──
    // CRON-72: prior to this round, CITY_X/Z were computed as
    // VILLAGE_X + 2400 (= 6242) and VILLAGE_Z + 800 (= -384), placing
    // the city at (6242, ?, -384) — wildly off the canon coordinate
    // (1800, 0, -2600) in PlanetSuzakuBlueprint.QILIN_CITY. Same latent
    // canon-coordinate bug as the other 3 ServerLevel-only builders.
    // Fixed by sourcing directly from the blueprint.
    public static final int CITY_X = PlanetSuzakuBlueprint.QILIN_CITY.x;
    public static final int CITY_Z = PlanetSuzakuBlueprint.QILIN_CITY.z;

    // ── Block palette ──────────────────────────────────────────────────
    // Ornate, wealthy, qilin-sacred. Gold, jade, amber, spirit stone.
    private static final BlockState QUARTZ              = Blocks.QUARTZ_BLOCK.defaultBlockState();
    private static final BlockState QUARTZ_PILLAR        = Blocks.QUARTZ_PILLAR.defaultBlockState();
    private static final BlockState QUARTZ_STAIRS       = Blocks.QUARTZ_STAIRS.defaultBlockState();
    private static final BlockState QUARTZ_SLAB         = Blocks.QUARTZ_SLAB.defaultBlockState();
    private static final BlockState QUARTZ_BRICKS       = Blocks.QUARTZ_BRICKS.defaultBlockState();
    private static final BlockState CHISELED_QUARTZ      = Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
    private static final BlockState CHISELED_QUARTZ_SLAB  = Blocks.QUARTZ_SLAB.defaultBlockState();
    private static final BlockState SPRUCE_PLANK        = Blocks.SPRUCE_PLANKS.defaultBlockState();
    private static final BlockState SPRUCE_LOG          = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState SPRUCE_STAIRS       = Blocks.SPRUCE_STAIRS.defaultBlockState();
    private static final BlockState SPRUCE_SLAB          = Blocks.SPRUCE_SLAB.defaultBlockState();
    private static final BlockState SPRUCE_DOOR         = Blocks.SPRUCE_DOOR.defaultBlockState();
    private static final BlockState DARK_OAK_PLANK      = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState DARK_OAK_LOG        = Blocks.DARK_OAK_LOG.defaultBlockState();
    private static final BlockState OAK_PLANK           = Blocks.OAK_PLANKS.defaultBlockState();
    private static final BlockState OAK_LOG             = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState OAK_STAIRS          = Blocks.OAK_STAIRS.defaultBlockState();
    private static final BlockState OAK_SLAB            = Blocks.OAK_SLAB.defaultBlockState();
    private static final BlockState BIRCH_PLANK         = Blocks.BIRCH_PLANKS.defaultBlockState();
    private static final BlockState BIRCH_LOG           = Blocks.BIRCH_LOG.defaultBlockState();
    private static final BlockState BIRCH_STAIRS        = Blocks.BIRCH_STAIRS.defaultBlockState();
    private static final BlockState BIRCH_SLAB          = Blocks.BIRCH_SLAB.defaultBlockState();
    private static final BlockState STONE_BRICK         = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState COBBLESTONE         = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState STONE               = Blocks.STONE.defaultBlockState();
    private static final BlockState STONE_STAIRS        = Blocks.STONE_STAIRS.defaultBlockState();
    private static final BlockState STONE_SLAB          = Blocks.STONE_SLAB.defaultBlockState();
    private static final BlockState DIRT                = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRASS               = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState WATER                = Blocks.WATER.defaultBlockState();
    private static final BlockState AIR                 = Blocks.AIR.defaultBlockState();
    private static final BlockState GOLD_BLOCK          = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState IRON_BARS           = Blocks.IRON_BARS.defaultBlockState();
    private static final BlockState IRON_DOOR           = Blocks.IRON_DOOR.defaultBlockState();
    private static final BlockState LANTERN             = Blocks.LANTERN.defaultBlockState();
    private static final BlockState SOUL_LANTERN        = Blocks.SOUL_LANTERN.defaultBlockState();
    private static final BlockState GLOWSTONE           = Blocks.GLOWSTONE.defaultBlockState();
    private static final BlockState CHEST               = Blocks.CHEST.defaultBlockState();
    private static final BlockState BOOKSHELF           = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState ANVIL               = Blocks.ANVIL.defaultBlockState();
    private static final BlockState CAULDRON            = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState CRAFTING_TABLE      = Blocks.CRAFTING_TABLE.defaultBlockState();
    private static final BlockState FURNACE             = Blocks.FURNACE.defaultBlockState();
    private static final BlockState BARREL              = Blocks.BARREL.defaultBlockState();
    private static final BlockState BREWING_STAND       = Blocks.BREWING_STAND.defaultBlockState();
    private static final BlockState GLASS_PANE          = Blocks.GLASS_PANE.defaultBlockState();
    private static final BlockState WHITE_CARPET        = Blocks.WHITE_CARPET.defaultBlockState();
    private static final BlockState YELLOW_CARPET       = Blocks.YELLOW_CARPET.defaultBlockState();
    private static final BlockState ORANGE_CARPET       = Blocks.ORANGE_CARPET.defaultBlockState();
    private static final BlockState RED_CARPET          = Blocks.RED_CARPET.defaultBlockState();
    private static final BlockState LIGHT_GRAY_CARPET   = Blocks.LIGHT_GRAY_CARPET.defaultBlockState();
    private static final BlockState OBSIDIAN            = Blocks.OBSIDIAN.defaultBlockState();
    private static final BlockState PRISMARINE          = Blocks.PRISMARINE.defaultBlockState();
    private static final BlockState PRISMARINE_BRICKS   = Blocks.PRISMARINE_BRICKS.defaultBlockState();
    private static final BlockState SEA_LANTERN         = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState POLISHED_GRANITE   = Blocks.POLISHED_GRANITE.defaultBlockState();
    private static final BlockState GRANITE             = Blocks.GRANITE.defaultBlockState();
    private static final BlockState GRAVEL              = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState SANDSTONE           = Blocks.SANDSTONE.defaultBlockState();
    private static final BlockState SANDSTONE_STAIRS     = Blocks.SANDSTONE_STAIRS.defaultBlockState();
    private static final BlockState WHITE_CONCRETE      = Blocks.WHITE_CONCRETE.defaultBlockState();
    private static final BlockState WHITE_CONCRETE_POWDER = Blocks.WHITE_CONCRETE_POWDER.defaultBlockState();
    // Custom Ergenverse blocks
    // ── Dimensions ────────────────────────────────────────────────────
    private static final int CITY_RADIUS = 62;
    private static final int WALL_HEIGHT = 11;
    private static final int TOWER_SIZE = 5;
    private static final int TOWER_HEIGHT = 16;

    // ── Chunk-scoped build infrastructure (CRON-COMPLETIONIST-72) ──────
    //
    // Mirrors the WangFamilyVillageBuilder (CRON-62/63) and XuanDaoSectBuilder
    // (CRON-66/70) pattern. See those classes' Javadoc for full rationale.

    /** Active chunk bounds during a buildInternal() pass, or null for full-build. */
    private static final ThreadLocal<ChunkBounds> CURRENT_BOUNDS = new ThreadLocal<>();

    /**
     * Filtered setBlock — the ONLY block-placement call site that calls
     * level.setBlock directly. Three guards: chunk filter, provenance-aware
     * rebuild guard, placement. See {@link WangFamilyVillageBuilder#sb} for
     * full Javadoc.
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
     * Resolve the city center BlockPos using the canon surface height from
     * {@link dev.ergenverse.runtime.worldgen.BlueprintChunkGenerator#canonSurfaceHeight}.
     */
    public static BlockPos getSectCenter(ServerLevel level) {
        int surfaceY = dev.ergenverse.runtime.worldgen.BlueprintChunkGenerator.canonSurfaceHeight(CITY_X, CITY_Z);
        return new BlockPos(CITY_X, surfaceY, CITY_Z);
    }

    /**
     * Chunk-scoped build entry point — invoked by the chunk-materializer for
     * each chunk that overlaps the city footprint.
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
            Ergenverse.LOGGER.debug("[Ergenverse] Qilin City already built — build() is a no-op.");
            return;
        }
        Ergenverse.LOGGER.info("[Ergenverse] Building Qilin City at {}", center);
        buildInternal(level, center);
        Ergenverse.LOGGER.info("[Ergenverse] Qilin City construction complete.");
    }

    /**
     * Check if the city is already built by looking for the marker GOLD_BLOCK
     * at {@code center.above(WALL_HEIGHT + 3)} — the position of the landmark
     * beacon's gold block (between QUARTZ_PILLAR below and SOUL_LANTERN above).
     *
     * <p><b>CRON-COMPLETIONIST-69 — provenance-aware rebuild guard.</b>
     * If the player (or simulation) has recorded a delta at the marker
     * position, returns {@code true} (city was built, then edited; don't
     * rebuild). See {@link dev.ergenverse.runtime.materialize.ProvenanceAwareRebuildGuard}.
     */
    public static boolean isAlreadyBuilt(ServerLevel level, BlockPos center) {
        BlockPos markerPos = center.above(WALL_HEIGHT + 3);
        if (level.getBlockState(markerPos).getBlock() == Blocks.GOLD_BLOCK) return true;
        if (dev.ergenverse.runtime.materialize.ProvenanceAwareRebuildGuard.shouldSkipRebuild(markerPos)) return true;
        return false;
    }

    /**
     * The actual construction body — shared by {@link #build} and {@link #buildForChunk}.
     * Placements flow through {@link #set} / {@link #sb} which apply the chunk
     * filter and provenance guard when CURRENT_BOUNDS is set.
     */
    private static void buildInternal(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int baseY = center.getY(); // CRON-72: was hardcoded 64; now canon surface Y
        int cz = center.getZ();

        ChunkBounds bounds = CURRENT_BOUNDS.get();
        if (bounds == null) {
            Ergenverse.LOGGER.info("[QilinCity] Building Qilin City (full) at ({}, {}, {})...", cx, baseY, cz);
        } else {
            Ergenverse.LOGGER.debug("[QilinCity] Building Qilin City (chunk-scoped {}) at center ({}, {}, {}).",
                    bounds, cx, baseY, cz);
        }

        // Foundation
        buildFoundation(level, cx, baseY, cz);

        // 1. City Walls with gold-trim crenellations
        buildCityWalls(level, cx, baseY, cz);

        // 2. Main Streets — spirit stone roads, cross-shaped
        buildMainStreets(level, cx, baseY, cz);

        // 3. East Gate (main entrance) — grand arch with qilin motifs
        buildEastGate(level, cx, baseY, cz);

        // 4. Qilin Plaza (central) — circular plaza with qilin statue fountain
        buildQilinPlaza(level, cx, baseY, cz);

        // 5. Beast Market (northwest) — pens, trading posts, beast stalls
        buildBeastMarket(level, cx, baseY, cz);

        // 6. Merchant District (east) — shops, auction house, warehouses
        buildMerchantDistrict(level, cx, baseY, cz);

        // 7. Mortal Quarter (south) — humble houses, well, shrine
        buildMortalQuarter(level, cx, baseY, cz);

        // 8. Residential District (southwest) — larger homes, gardens
        buildResidentialDistrict(level, cx, baseY, cz);

        // 9. Governor's Mansion (north) — grand estate, jade pillars
        buildGovernorMansion(level, cx, baseY, cz);

        // 10. Temple District (northeast) — qilin temple, prayer halls
        buildTempleDistrict(level, cx, baseY, cz);

        // 11. Tavern District (central-south) — inns, teahouses
        buildTavernDistrict(level, cx, baseY, cz);

        // 12. Smuggler Tunnels (east wall, near port area) — basement entrance
        buildSmugglerTunnels(level, cx, baseY, cz);

        // ── Landmark beacon (CRON-72: preserved + marker semantics) ────
        // The beacon stack (QUARTZ_PILLAR + GOLD_BLOCK + SOUL_LANTERN) at
        // center.above(WALL_HEIGHT + 2..4) was already in the legacy build().
        // CRON-72 preserves it AS the isAlreadyBuilt marker: GOLD_BLOCK at
        // center.above(WALL_HEIGHT + 3) is the marker isAlreadyBuilt checks.
        // The stack is placed via set() (→ sb()), so it respects the chunk
        // filter and provenance guard like every other placement.
        set(level, new BlockPos(cx, baseY + WALL_HEIGHT + 2, cz), QUARTZ_PILLAR, 2);
        set(level, new BlockPos(cx, baseY + WALL_HEIGHT + 3, cz), GOLD_BLOCK, 2);
        set(level, new BlockPos(cx, baseY + WALL_HEIGHT + 4, cz), SOUL_LANTERN, 2);
    }

    /**
     * Single-block placement helper — delegates to {@link #sb} so all setBlock
     * traffic flows through the chunk filter and provenance guard.
     *
     * <p>CRON-72: prior to this round, the 239 callsites of {@code level.setBlock}
     * were inline and bypassed both guards. A Python script mechanically
     * rewrote every {@code level.setBlock(} to {@code set(level, }; this
     * overload is the receiver. Same pattern as CRON-71's ChestHelper fix.
     */
    private static void set(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        sb(level, pos, state, flags);
    }

    // ════════════════════════════════════════════════════════════════════
    // FOUNDATION & WALLS
    // ════════════════════════════════════════════════════════════════════

    private static void buildFoundation(ServerLevel level, int cx, int baseY, int cz) {
        for (int dx = -CITY_RADIUS - 5; dx <= CITY_RADIUS + 5; dx++) {
            for (int dz = -CITY_RADIUS - 5; dz <= CITY_RADIUS + 5; dz++) {
                BlockPos p = new BlockPos(cx + dx, baseY - 1, cz + dz);
                set(level, p, B.SPIRIT_DIRT, 2);
                for (int y = baseY; y <= baseY + WALL_HEIGHT + 5; y++) {
                    set(level, new BlockPos(cx + dx, y, cz + dz), AIR, 2);
                }
            }
        }
    }

    private static void buildCityWalls(ServerLevel level, int cx, int baseY, int cz) {
        int r = CITY_RADIUS;
        int wallY = baseY;
        for (int i = -r; i <= r; i++) {
            for (int y = 0; y < WALL_HEIGHT; y++) {
                // North wall
                set(level, new BlockPos(cx + i, wallY + y, cz - r), CHISELED_QUARTZ, 2);
                // South wall
                set(level, new BlockPos(cx + i, wallY + y, cz + r), CHISELED_QUARTZ, 2);
                // West wall
                set(level, new BlockPos(cx - r, wallY + y, cz + i), CHISELED_QUARTZ, 2);
                // East wall
                set(level, new BlockPos(cx + r, wallY + y, cz + i), CHISELED_QUARTZ, 2);
            }
            // Gold-trim crenellations — every other block gets gold cap
            if (i % 2 == 0) {
                set(level, new BlockPos(cx + i, wallY + WALL_HEIGHT, cz - r), GOLD_BLOCK, 2);
                set(level, new BlockPos(cx + i, wallY + WALL_HEIGHT, cz + r), GOLD_BLOCK, 2);
                set(level, new BlockPos(cx - r, wallY + WALL_HEIGHT, cz + i), GOLD_BLOCK, 2);
                set(level, new BlockPos(cx + r, wallY + WALL_HEIGHT, cz + i), GOLD_BLOCK, 2);
            } else {
                set(level, new BlockPos(cx + i, wallY + WALL_HEIGHT, cz - r), QUARTZ_SLAB, 2);
                set(level, new BlockPos(cx + i, wallY + WALL_HEIGHT, cz + r), QUARTZ_SLAB, 2);
                set(level, new BlockPos(cx - r, wallY + WALL_HEIGHT, cz + i), QUARTZ_SLAB, 2);
                set(level, new BlockPos(cx + r, wallY + WALL_HEIGHT, cz + i), QUARTZ_SLAB, 2);
            }
            // Battlement walkway
            if (i % 4 == 0) {
                set(level, new BlockPos(cx + i, wallY + WALL_HEIGHT + 1, cz - r), AIR, 2);
                set(level, new BlockPos(cx + i, wallY + WALL_HEIGHT + 1, cz + r), AIR, 2);
                set(level, new BlockPos(cx - r, wallY + WALL_HEIGHT + 1, cz + i), AIR, 2);
                set(level, new BlockPos(cx + r, wallY + WALL_HEIGHT + 1, cz + i), AIR, 2);
            }
        }
        // Spirit stone road on top of walls (patrol walkway)
        for (int i = -r; i <= r; i++) {
            set(level, new BlockPos(cx + i, wallY + WALL_HEIGHT + 1, cz - r), B.SPIRIT_STONE, 2);
            set(level, new BlockPos(cx + i, wallY + WALL_HEIGHT + 1, cz + r), B.SPIRIT_STONE, 2);
            set(level, new BlockPos(cx - r, wallY + WALL_HEIGHT + 1, cz + i), B.SPIRIT_STONE, 2);
            set(level, new BlockPos(cx + r, wallY + WALL_HEIGHT + 1, cz + i), B.SPIRIT_STONE, 2);
        }
        // Lanterns on towers
        buildWatchtower(level, cx - r, baseY, cz - r);
        buildWatchtower(level, cx + r, baseY, cz - r);
        buildWatchtower(level, cx - r, baseY, cz + r);
        buildWatchtower(level, cx + r, baseY, cz + r);
    }

    private static void buildWatchtower(ServerLevel level, int tx, int baseY, int tz) {
        for (int y = 0; y < TOWER_HEIGHT; y++) {
            for (int dx = 0; dx < TOWER_SIZE; dx++) {
                for (int dz = 0; dz < TOWER_SIZE; dz++) {
                    boolean isEdge = dx == 0 || dx == TOWER_SIZE - 1 || dz == 0 || dz == TOWER_SIZE - 1;
                    boolean isDoor = y == 0 && dx == 2 && dz == 0;
                    if (isEdge && !isDoor) {
                        set(level, new BlockPos(tx + dx, baseY + y, tz + dz), CHISELED_QUARTZ, 2);
                    } else if (!isEdge && y == 0) {
                        set(level, new BlockPos(tx + dx, baseY, tz + dz), POLISHED_GRANITE, 2);
                    }
                }
            }
        }
        // Gold cap
        for (int dx = 0; dx < TOWER_SIZE; dx++) {
            for (int dz = 0; dz < TOWER_SIZE; dz++) {
                set(level, new BlockPos(tx + dx, baseY + TOWER_HEIGHT, tz + dz), QUARTZ_SLAB, 2);
            }
        }
        // Gold beacon on top
        set(level, new BlockPos(tx + 2, baseY + TOWER_HEIGHT + 1, tz + 2), GOLD_BLOCK, 2);
        set(level, new BlockPos(tx + 2, baseY + TOWER_HEIGHT, tz + 2), SOUL_LANTERN, 2);
    }

    // ════════════════════════════════════════════════════════════════════
    // MAIN STREETS — Spirit stone roads, gold lanterns
    // ════════════════════════════════════════════════════════════════════

    private static void buildMainStreets(ServerLevel level, int cx, int baseY, int cz) {
        int r = CITY_RADIUS;
        // N-S avenue (6 wide, spirit stone)
        for (int dz = -r + 2; dz <= r - 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                set(level, new BlockPos(cx + dx, baseY, cz + dz), B.SPIRIT_STONE, 2);
            }
        }
        // E-W avenue (6 wide)
        for (int dx = -r + 2; dx <= r - 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                set(level, new BlockPos(cx + dx, baseY, cz + dz), B.SPIRIT_STONE, 2);
            }
        }
        // Sidewalk trim — gold block lines along streets
        for (int dz = -r + 2; dz <= r - 2; dz += 6) {
            set(level, new BlockPos(cx - 3, baseY, cz + dz), GOLD_BLOCK, 2);
            set(level, new BlockPos(cx + 3, baseY, cz + dz), GOLD_BLOCK, 2);
        }
        for (int dx = -r + 2; dx <= r - 2; dx += 6) {
            set(level, new BlockPos(cx + dx, baseY, cz - 3), GOLD_BLOCK, 2);
            set(level, new BlockPos(cx + dx, baseY, cz + 3), GOLD_BLOCK, 2);
        }
        // Street lanterns every 10 blocks along N-S avenue
        for (int dz = -r + 8; dz <= r - 8; dz += 10) {
            set(level, new BlockPos(cx - 4, baseY + 1, cz + dz), IRON_BARS, 2);
            set(level, new BlockPos(cx - 4, baseY + 2, cz + dz), SOUL_LANTERN, 2);
            set(level, new BlockPos(cx + 4, baseY + 1, cz + dz), IRON_BARS, 2);
            set(level, new BlockPos(cx + 4, baseY + 2, cz + dz), SOUL_LANTERN, 2);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // EAST GATE — Grand arch with gold framing
    // ════════════════════════════════════════════════════════════════════

    private static void buildEastGate(ServerLevel level, int cx, int baseY, int cz) {
        int r = CITY_RADIUS;
        int gx = cx + r;
        // Gate opening (clear 5 wide, 8 tall)
        for (int dz = -2; dz <= 2; dz++) {
            for (int y = 0; y < 8; y++) {
                set(level, new BlockPos(gx, baseY + y, cz + dz), AIR, 2);
            }
        }
        // Gold block arch frame above gate
        for (int dz = -3; dz <= 3; dz++) {
            set(level, new BlockPos(gx, baseY + 8, cz + dz), GOLD_BLOCK, 2);
            set(level, new BlockPos(gx, baseY + 9, cz + dz), QUARTZ_STAIRS, 2);
        }
        // Jade pillar pillars flanking gate
        for (int y = 0; y < 10; y++) {
            set(level, new BlockPos(gx, baseY + y, cz - 3), B.JADE_STONE, 2);
            set(level, new BlockPos(gx, baseY + y, cz + 3), B.JADE_STONE, 2);
        }
        // Gold plaque above gate (where city name would go)
        set(level, new BlockPos(gx, baseY + 9, cz - 1), GOLD_BLOCK, 2);
        // Guard barracks (north side)
        buildGuardBarracks(level, gx - 3, baseY, cz - 6);
        // Guard barracks (south side)
        buildGuardBarracks(level, gx - 3, baseY, cz + 4);
    }

    private static void buildGuardBarracks(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 4; dz++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 4 || dz == 0 || dz == 3;
                    boolean isDoor = y < 2 && dx == 2 && dz == 0;
                    if (isEdge && !isDoor) set(level, p, DARK_OAK_PLANK, 2);
                    else if (!isEdge && y == 0) set(level, p, SPRUCE_PLANK, 2);
                }
            }
        }
        // Roof
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 4; dz++) {
                set(level, new BlockPos(x + dx, baseY + 4, z + dz), DARK_OAK_PLANK, 2);
            }
        }
        // Interior: bunks
        set(level, new BlockPos(x + 1, baseY + 1, z + 1), OAK_PLANK, 2);
        set(level, new BlockPos(x + 3, baseY + 1, z + 1), OAK_PLANK, 2);
        // Chest for guard equipment
        set(level, new BlockPos(x + 1, baseY + 1, z + 2), CHEST, 2);
    }

    // ════════════════════════════════════════════════════════════════════
    // QILIN PLAZA — Central circular plaza with qilin statue fountain
    // ════════════════════════════════════════════════════════════════════

    private static void buildQilinPlaza(ServerLevel level, int cx, int baseY, int cz) {
        // Circular plaza (radius 9) with quartz floor
        for (int dx = -9; dx <= 9; dx++) {
            for (int dz = -9; dz <= 9; dz++) {
                if (dx * dx + dz * dz <= 81) {
                    set(level, new BlockPos(cx + dx, baseY, cz + dz), QUARTZ, 2);
                    for (int y = baseY + 1; y <= baseY + WALL_HEIGHT; y++) {
                        set(level, new BlockPos(cx + dx, y, cz + dz), AIR, 2);
                    }
                }
            }
        }
        // Jade ring border
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                int dist = dx * dx + dz * dz;
                if (dist > 81 && dist <= 121) {
                    set(level, new BlockPos(cx + dx, baseY, cz + dz), B.JADE_STONE, 2);
                }
            }
        }
        // Central fountain — prismarine basin
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                    set(level, new BlockPos(cx + dx, baseY, cz + dz), PRISMARINE, 2);
                    set(level, new BlockPos(cx + dx, baseY + 1, cz + dz), PRISMARINE, 2);
                } else {
                    set(level, new BlockPos(cx + dx, baseY, cz + dz), WATER, 2);
                }
            }
        }
        // Central statue pillar (where qilin statue would go)
        set(level, new BlockPos(cx, baseY + 2, cz), OBSIDIAN, 2);
        set(level, new BlockPos(cx, baseY + 3, cz), OBSIDIAN, 2);
        set(level, new BlockPos(cx, baseY + 4, cz), GOLD_BLOCK, 2);
        set(level, new BlockPos(cx, baseY + 5, cz), SOUL_LANTERN, 2);
        // Four gold accent pillars around the fountain
        set(level, new BlockPos(cx - 5, baseY, cz - 5), GOLD_BLOCK, 2);
        set(level, new BlockPos(cx + 5, baseY, cz - 5), GOLD_BLOCK, 2);
        set(level, new BlockPos(cx - 5, baseY, cz + 5), GOLD_BLOCK, 2);
        set(level, new BlockPos(cx + 5, baseY, cz + 5), GOLD_BLOCK, 2);
        // Carpet walkways from plaza to streets
        for (int i = 10; i <= CITY_RADIUS - 5; i++) {
            set(level, new BlockPos(cx + i, baseY, cz - 3), YELLOW_CARPET, 2);
            set(level, new BlockPos(cx + i, baseY, cz + 3), YELLOW_CARPET, 2);
            set(level, new BlockPos(cx - 3, baseY, cz + i), YELLOW_CARPET, 2);
            set(level, new BlockPos(cx + 3, baseY, cz + i), YELLOW_CARPET, 2);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // BEAST MARKET — Northwest, pens and trading posts
    // ════════════════════════════════════════════════════════════════════

    private static void buildBeastMarket(ServerLevel level, int cx, int baseY, int cz) {
        int startX = cx - 55;
        int startZ = cz - 50;
        // Spirit stone floor for the whole district
        for (int dx = 0; dx < 35; dx++) {
            for (int dz = 0; dz < 30; dz++) {
                set(level, new BlockPos(startX + dx, baseY, startZ + dz), B.SPIRIT_STONE, 2);
            }
        }
        // 8 beast pens (4x2 grid), each 8×8×4
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                int px = startX + 2 + col * 9;
                int pz = startZ + 2 + row * 12;
                buildBeastPen(level, px, baseY, pz);
            }
        }
        // Trading hall (12×8×5) at the south end
        buildBeastTradingHall(level, startX + 5, baseY, startZ + 26);
        // Auction platform (10×10×1) raised platform at north end
        for (int dx = 0; dx < 10; dx++) {
            for (int dz = 0; dz < 10; dz++) {
                set(level, new BlockPos(startX + 10 + dx, baseY + 1, startZ + dz), QUARTZ_BRICKS, 2);
                set(level, new BlockPos(startX + 10 + dx, baseY, startZ + dz), QUARTZ_BRICKS, 2);
            }
        }
        set(level, new BlockPos(startX + 14, baseY + 2, startZ + 4), OBSIDIAN, 2);
        set(level, new BlockPos(startX + 14, baseY + 2, startZ + 5), GOLD_BLOCK, 2);
        set(level, new BlockPos(startX + 15, baseY + 2, startZ + 4), SOUL_LANTERN, 2);
    }

    private static void buildBeastPen(ServerLevel level, int x, int baseY, int z) {
        // Walls: iron bars (so you can see the beasts)
        for (int dx = 0; dx < 8; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                if (dx == 0 || dx == 7 || dz == 0 || dz == 7) {
                    set(level, new BlockPos(x + dx, baseY + 3, z + dz), IRON_BARS, 2);
                    set(level, new BlockPos(x + dx, baseY + 2, z + dz), IRON_BARS, 2);
                }
            }
        }
        // Floor
        for (int dx = 1; dx < 7; dx++) {
            for (int dz = 1; dz < 7; dz++) {
                set(level, new BlockPos(x + dx, baseY, z + dz), GRAVEL, 2);
            }
        }
        // Feed trough
        for (int dx = 1; dx <= 5; dx++) {
            set(level, new BlockPos(x + dx, baseY + 1, z + 1), DARK_OAK_PLANK, 2);
        }
        // Water trough
        set(level, new BlockPos(x + 1, baseY + 1, z + 5), WATER, 2);
        set(level, new BlockPos(x + 2, baseY + 1, z + 5), WATER, 2);
        set(level, new BlockPos(x + 3, baseY + 1, z + 5), WATER, 2);
        set(level, new BlockPos(x + 4, baseY + 1, z + 5), WATER, 2);
        set(level, new BlockPos(x + 5, baseY + 1, z + 5), WATER, 2);
        // Gate (iron door)
        set(level, new BlockPos(x + 3, baseY + 1, z), IRON_DOOR, 2);
        set(level, new BlockPos(x + 3, baseY + 2, z), AIR, 2);
        set(level, new BlockPos(x + 3, baseY + 3, z), AIR, 2);
    }

    private static void buildBeastTradingHall(ServerLevel level, int x, int baseY, int z) {
        // Walls: dark oak (sturdy, beast-proof)
        for (int dx = 0; dx < 12; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 11 || dz == 0 || dz == 7;
                    boolean isDoor = y < 2 && dx == 6 && dz == 0;
                    boolean isWindow = y >= 2 && y <= 3 && ((dx == 3 && dz == 0) || (dx == 8 && dz == 0));
                    if (isEdge && !isDoor && !isWindow) {
                        set(level, p, DARK_OAK_PLANK, 2);
                    } else if (isWindow) {
                        set(level, p, IRON_BARS, 2);
                    } else if (!isEdge && y == 0) {
                        set(level, p, SPRUCE_PLANK, 2);
                    }
                }
            }
        }
        // Roof
        for (int dx = 0; dx < 12; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                set(level, new BlockPos(x + dx, baseY + 5, z + dz), DARK_OAK_PLANK, 2);
            }
        }
        // Interior: counter
        for (int dx = 1; dx <= 10; dx++) {
            set(level, new BlockPos(x + dx, baseY + 1, z + 3), SPRUCE_PLANK, 2);
        }
        // Back shelves
        for (int dx = 1; dx <= 10; dx += 2) {
            set(level, new BlockPos(x + dx, baseY + 1, z + 6), BOOKSHELF, 2);
        }
        // Display cases (glass panes with gold trim)
        set(level, new BlockPos(x + 3, baseY + 1, z + 1), GLASS_PANE, 2);
        set(level, new BlockPos(x + 7, baseY + 1, z + 1), GLASS_PANE, 2);
        // Chests for beast products
        set(level, new BlockPos(x + 5, baseY + 1, z + 5), CHEST, 2);
        set(level, new BlockPos(x + 9, baseY + 1, z + 5), CHEST, 2);
    }

    // ════════════════════════════════════════════════════════════════════
    // MERCHANT DISTRICT — East, shops and warehouses
    // ══════════════════════════════════════════════════════════════════

    private static void buildMerchantDistrict(ServerLevel level, int cx, int baseY, int cz) {
        int startX = cx + 12;
        int startZ = cz - 28;
        // 12 merchant shops in 3 rows of 4
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int sx = startX + col * 8;
                int sz = startZ + row * 9;
                buildMerchantShop(level, sx, baseY, sz, row, col);
            }
        }
        // Warehouse complex (southeast) — 15×10×6
        buildWarehouse(level, startX + 5, baseY, startZ + 30);
    }

    private static void buildMerchantShop(ServerLevel level, int x, int baseY, int z, int row, int col) {
        for (int dx = 0; dx < 6; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 5 || dz == 0 || dz == 4;
                    boolean isDoor = y < 2 && dx == 3 && dz == 0;
                    boolean isWindow = y >= 2 && y <= 3 && dz == 2;
                    if (isEdge && !isDoor && !isWindow) {
                        set(level, p, BIRCH_PLANK, 2);
                    } else if (isWindow) {
                        set(level, p, GLASS_PANE, 2);
                    } else if (!isEdge && y == 0) {
                        set(level, p, OAK_PLANK, 2);
                    }
                }
            }
        }
        // Roof
        for (int dx = 0; dx < 6; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                set(level, new BlockPos(x + dx, baseY + 4, z + dz), BIRCH_STAIRS, 2);
            }
        }
        // Interior counter
        for (int dx = 1; dx <= 4; dx++) {
            set(level, new BlockPos(x + dx, baseY + 1, z + 2), SPRUCE_PLANK, 2);
        }
        // Shelf
        set(level, new BlockPos(x + 1, baseY + 1, z + 3), BOOKSHELF, 2);
        set(level, new BlockPos(x + 4, baseY + 1, z + 3), BOOKSHELF, 2);
    }

    private static void buildWarehouse(ServerLevel level, int x, int baseY, int z) {
        // Stone walls (heavy, secure)
        for (int dx = 0; dx < 15; dx++) {
            for (int dz = 0; dz < 10; dz++) {
                for (int y = 0; y < 6; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 14 || dz == 0 || dz == 9;
                    boolean isDoor = y < 3 && dx == 7 && dz == 0;
                    boolean isDoor2 = y < 3 && dx == 7 && dz == 9;
                    if (isEdge && !isDoor && !isDoor2) {
                        set(level, p, STONE_BRICK, 2);
                    } else if (!isEdge && y == 0) {
                        set(level, p, COBBLESTONE, 2);
                    }
                }
            }
        }
        // Roof
        for (int dx = 0; dx < 15; dx++) {
            for (int dz = 0; dz < 10; dz++) {
                set(level, new BlockPos(x + dx, baseY + 6, z + dz), STONE_BRICK, 2);
            }
        }
        // Crates inside
        for (int i = 0; i < 6; i++) {
            int cx = x + 1 + (i % 3) * 4;
            int cz = z + 1 + (i / 3) * 3;
            set(level, new BlockPos(cx, baseY + 1, cz), CRATE_BLOCK(), 2);
        }
        // Crates along walls
        for (int i = 0; i < 8; i++) {
            set(level, new BlockPos(x + 1 + (i % 4) * 3, baseY + 1, z + 7 + (i / 4) * 2), CRATE_BLOCK(), 2);
            set(level, new BlockPos(x + 12 + (i % 4) * 2, baseY + 1, z + 1 + (i / 4) * 3), CRATE_BLOCK(), 2);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // MORTAL QUARTER — South, humble houses
    // ══════════════════════════════════════════════════════════════════

    private static void buildMortalQuarter(ServerLevel level, int cx, int baseY, int cz) {
        int startX = cx - 28;
        int startZ = cz + 10;
        // Spirit dirt floor
        for (int dx = 0; dx < 24; dx++) {
            for (int dz = 0; dz < 22; dz++) {
                set(level, new BlockPos(startX + dx, baseY, startZ + dz), B.SPIRIT_DIRT, 2);
            }
        }
        // 12 humble houses (3 rows of 4)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int sx = startX + col * 6;
                int sz = startZ + row * 7;
                buildMortalHouse(level, sx, baseY, sz);
            }
        }
        // Community well (center)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    set(level, new BlockPos(startX + 10 + dx, baseY, startZ + 8 + dz), STONE_BRICK, 2);
                    set(level, new BlockPos(startX + 10 + dx, baseY + 1, startZ + 8 + dz), STONE_BRICK, 2);
                    set(level, new BlockPos(startX + 10 + dx, baseY + 2, startZ + 8 + dz), STONE_BRICK, 2);
                } else {
                    set(level, new BlockPos(startX + 10 + dx, baseY, startZ + 8 + dz), WATER, 2);
                }
            }
        }
        // Shrine (small stone building)
        buildShrine(level, startX + 18, baseY, startZ + 16);
    }

    private static void buildMortalHouse(ServerLevel level, int x, int baseY, int z) {
        // 5x4x4 humble house
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 4; dz++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 4 || dz == 0 || dz == 3;
                    boolean isDoor = y < 2 && dx == 2 && dz == 0;
                    if (isEdge && !isDoor) set(level, p, COBBLESTONE, 2);
                    else if (!isEdge && y == 0) set(level, p, DIRT, 2);
                }
            }
        }
        // Roof (spruce planks, simple)
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 4; dz++) {
                set(level, new BlockPos(x + dx, baseY + 4, z + dz), SPRUCE_PLANK, 2);
            }
        }
        // Bed (oak plank)
        set(level, new BlockPos(x + 1, baseY + 1, z + 2), OAK_PLANK, 2);
        // Crafting table
        set(level, new BlockPos(x + 3, baseY + 1, z + 2), CRAFTING_TABLE, 2);
        // Furnace
        set(level, new BlockPos(x + 3, baseY + 1, z + 1), FURNACE, 2);
    }

    private static void buildShrine(ServerLevel level, int x, int baseY, int z) {
        // 5x5x4 stone shrine
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 4 || dz == 0 || dz == 4;
                    boolean isDoor = y < 2 && dx == 2 && dz == 0;
                    if (isEdge && !isDoor) set(level, p, STONE_BRICK, 2);
                    else if (!isEdge && y == 0) set(level, p, STONE, 2);
                }
            }
        }
        // Pyramid roof (stone stairs)
        for (int dy = 0; dy < 3; dy++) {
            int size = 5 - dy * 2;
            for (int dx = -1; dx < size; dx++) {
                for (int dz = -1; dz < size; dz++) {
                    set(level, new BlockPos(x + dx + 2, baseY + 4 + dy, z + dz + 2), STONE_STAIRS, 2);
                }
            }
        }
        // Altar
        set(level, new BlockPos(x + 2, baseY + 1, z + 2), OBSIDIAN, 2);
        set(level, new BlockPos(x + 2, baseY + 2, z + 2), GOLD_BLOCK, 2);
        set(level, new BlockPos(x + 2, baseY + 3, z + 2), SOUL_LANTERN, 2);
    }

    // ════════════════════════════════════════════════════════════════════
    // RESIDENTIAL DISTRICT — Southwest, larger homes with gardens
    // ════════════════════════════════════════════════════════════════════

    private static void buildResidentialDistrict(ServerLevel level, int cx, int baseY, int cz) {
        int startX = cx - 52;
        int startZ = cz + 35;
        // Spirit grass floor
        for (int dx = 0; dx < 28; dx++) {
            for (int dz = 0; dz < 22; dz++) {
                set(level, new BlockPos(startX + dx, baseY, startZ + dz), B.SPIRIT_GRASS, 2);
            }
        }
        // 8 homes with gardens (2 rows of 4)
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                int sx = startX + col * 7;
                int sz = startZ + row * 11;
                buildResidentialHome(level, sx, baseY, sz);
            }
        }
    }

    private static void buildResidentialHome(ServerLevel level, int x, int baseY, int z) {
        // House 6x5x5 (larger than mortal)
        for (int dx = 0; dx < 6; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 5 || dz == 0 || dz == 4;
                    boolean isDoor = y < 2 && dx == 3 && dz == 0;
                    if (isEdge && !isDoor) set(level, p, BIRCH_PLANK, 2);
                    else if (!isEdge && y == 0) set(level, p, OAK_PLANK, 2);
                }
            }
        }
        // Roof
        for (int dx = -1; dx <= 6; dx++) {
            for (int dz = -1; dz <= 6; dz++) {
                set(level, new BlockPos(x + dx, baseY + 5, z + dz), BIRCH_STAIRS, 2);
            }
        }
        // Garden behind house (spirit grass + flowers)
        for (int dx = 0; dx < 4; dx++) {
            set(level, new BlockPos(x + 6, baseY, z + 1 + dx), B.SPIRIT_GRASS, 2);
            if (dx % 2 == 0) {
                set(level, new BlockPos(x + 6, baseY, z + 2 + dx), YELLOW_CARPET, 2);
            }
        }
        // Interior
        set(level, new BlockPos(x + 1, baseY + 1, z + 2), OAK_PLANK, 2);
        set(level, new BlockPos(x + 4, baseY + 1, z + 2), CRAFTING_TABLE, 2);
        set(level, new BlockPos(x + 4, baseY + 1, z + 3), BOOKSHELF, 2);
    }

    // ════════════════════════════════════════════════════════════════════
    // GOVERNOR'S MANSION — North, grand estate with jade pillars
    // ════════════════════════════════════════════════════════════════════

    private static void buildGovernorMansion(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx - 12;
        int gz = cz - 50;
        // Polished granite + quartz + jade estate
        // Outer walls: 20×16 footprint
        for (int dx = 0; dx < 20; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                for (int y = 0; y < 6; y++) {
                    BlockPos p = new BlockPos(gx + dx, baseY + y, gz + dz);
                    boolean isEdge = dx == 0 || dx == 19 || dz == 0 || dz == 15;
                    boolean isDoor = y < 3 && dx == 10 && dz == 0;
                    boolean isWindow = y >= 3 && y <= 4 && ((dx == 5 && dz == 0) || (dx == 14 && dz == 0) || (dz == 7));
                    if (isEdge && !isDoor && !isWindow) set(level, p, POLISHED_GRANITE, 2);
                    else if (isWindow) set(level, p, GLASS_PANE, 2);
                    else if (!isEdge && y == 0) set(level, p, POLISHED_GRANITE, 2);
                }
            }
        }
        // Second floor walls
        for (int dx = 2; dx < 18; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                for (int y = 6; y < 11; y++) {
                    BlockPos p = new BlockPos(gx + dx, baseY + y, gz + dz);
                    boolean isEdge = dx == 2 || dx == 17 || dz == 0 || dz == 15;
                    boolean isWindow = y >= 7 && y <= 9 && ((dz == 7) || (dz == 8));
                    if (isEdge && !isWindow) set(level, p, POLISHED_GRANITE, 2);
                    else if (isWindow) set(level, p, GLASS_PANE, 2);
                }
            }
        }
        // Flat roof (second floor level)
        for (int dx = 0; dx < 20; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                set(level, new BlockPos(gx + dx, baseY + 11, gz + dz), QUARTZ_SLAB, 2);
            }
        }
        // Jade pillars flanking front door
        for (int y = 0; y < 8; y++) {
            set(level, new BlockPos(gx + 9, baseY + y, gz - 1), B.JADE_STONE, 2);
            set(level, new BlockPos(gx + 9, baseY + y, gz + 1), B.JADE_STONE, 2);
        }
        // Gold plaque above front door
        set(level, new BlockPos(gx + 9, baseY + 8, gz), GOLD_BLOCK, 2);
        // Interior first floor: reception hall
        for (int dx = 2; dx < 18; dx++) {
            set(level, new BlockPos(gx + dx, baseY + 1, gz + 7), RED_CARPET, 2);
        }
        // Reception desk
        for (int dx = 5; dx <= 8; dx++) {
            set(level, new BlockPos(gx + dx, baseY + 1, gz + 4), SPRUCE_PLANK, 2);
        }
        // Interior first floor: side rooms
        buildMansionSideRoom(level, gx + 1, baseY, gz + 1, SPRUCE_PLANK);
        buildMansionSideRoom(level, gx + 1, baseY, gz + 9, DARK_OAK_PLANK);
        // Interior second floor: audience hall
        for (int dx = 4; dx < 16; dx++) {
            set(level, new BlockPos(gx + dx, baseY + 7, gz + 4), ORANGE_CARPET, 2);
        }
        // Second floor throne
        set(level, new BlockPos(gx + 10, baseY + 7, gz + 1), OBSIDIAN, 2);
        set(level, new BlockPos(gx + 10, baseY + 8, gz + 1), GOLD_BLOCK, 2);
        set(level, new BlockPos(gx + 10, baseY + 9, gz + 1), SOUL_LANTERN, 2);
        // Bookshelves along walls
        for (int y = 7; y <= 10; y++) {
            set(level, new BlockPos(gx + 2, baseY + y, gz + 1), BOOKSHELF, 2);
            set(level, new BlockPos(gx + 16, baseY + y, gz + 1), BOOKSHELF, 2);
        }
        // Chests
        set(level, new BlockPos(gx + 4, baseY + 1, gz + 2), CHEST, 2);
        set(level, new BlockPos(gx + 13, baseY + 1, gz + 2), CHEST, 2);
    }

    private static void buildMansionSideRoom(ServerLevel level, int x, int baseY, int z, BlockState floor) {
        for (int dx = 0; dx < 6; dx++) {
            for (int dz = 0; dz < 7; dz++) {
                set(level, new BlockPos(x + dx, baseY + 1, z + dz), floor, 2);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // TEMPLE DISTRICT — Northeast, qilin temple with prayer halls
    // ════════════════════════════════════════════════════════════════════

    private static void buildTempleDistrict(ServerLevel level, int cx, int baseY, int cz) {
        int startX = cx + 12;
        int startZ = cz - 50;
        // Polished granite + quartz temple
        // Spirit stone floor for the whole district
        for (int dx = 0; dx < 32; dx++) {
            for (int dz = 0; dz < 28; dz++) {
                set(level, new BlockPos(startX + dx, baseY, startZ + dz), B.SPIRIT_STONE, 2);
            }
        }
        // Main temple (14×10×8) with obsidian altar
        buildQilinTemple(level, startX + 4, baseY, startZ + 4);
        // Prayer hall (10×8×6)
        buildPrayerHall(level, startX + 20, baseY, startZ + 2);
        // Meditation garden (6×6 open with spirit grass)
        for (int dx = -3; dx <= 2; dx++) {
            for (int dz = -3; dz <= 2; dz++) {
                set(level, new BlockPos(startX + 4 + dx, baseY, startZ + 20 + dz), B.SPIRIT_GRASS, 2);
            }
        }
        // Lotus pond in meditation garden
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) >= 2 || Math.abs(dz) >= 2) {
                    set(level, new BlockPos(startX + 4 + dx, baseY, startZ + 20 + dz), PRISMARINE, 2);
                    set(level, new BlockPos(startX + 4 + dx, baseY + 1, startZ + 20 + dz), PRISMARINE, 2);
                } else {
                    set(level, new BlockPos(startX + 4 + dx, baseY, startZ + 20 + dz), WATER, 2);
                    set(level, new BlockPos(startX + 4 + dx, baseY + 2, startZ + 20 + dz), WATER, 2);
                }
            }
        }
        // Lantern posts at temple entrance
        set(level, new BlockPos(startX + 5, baseY + 1, startZ + 3), IRON_BARS, 2);
        set(level, new BlockPos(startX + 5, baseY + 2, startZ + 3), SOUL_LANTERN, 2);
        set(level, new BlockPos(startX + 13, baseY + 1, startZ + 3), IRON_BARS, 2);
        set(level, new BlockPos(startX + 13, baseY + 2, startZ + 3), SOUL_LANTERN, 2);
    }

    private static void buildQilinTemple(ServerLevel level, int x, int baseY, int z) {
        // 14×10×8 temple
        for (int dx = 0; dx < 14; dx++) {
            for (int dz = 0; dz < 10; dz++) {
                for (int y = 0; y < 8; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 13 || dz == 0 || dz == 9;
                    boolean isDoor = y < 3 && dx == 7 && dz == 0;
                    boolean isWindow = y >= 3 && y <= 5 && ((dz == 2) || (dz == 7));
                    if (isEdge && !isDoor && !isWindow) set(level, p, POLISHED_GRANITE, 2);
                    else if (isWindow) set(level, p, STAINED_GLASS(), 2);
                    else if (!isEdge && y == 0) set(level, p, POLISHED_GRANITE, 2);
                }
            }
        }
        // Pyramid roof (ascending layers, narrowing)
        for (int dy = 0; dy < 4; dy++) {
            int offset = dy * 2;
            int size = 14 - dy * 2;
            for (int dx = -1; dx < size; dx++) {
                for (int dz = -1; dz < size; dz++) {
                    set(level, new BlockPos(x + dx + offset + 7, baseY + 8 + dy, z + dz + 5), QUARTZ_STAIRS, 2);
                }
            }
        }
        // Jade pillars at entrance
        for (int y = 0; y < 8; y++) {
            set(level, new BlockPos(x + 6, baseY + y, z - 1), B.JADE_STONE, 2);
            set(level, new BlockPos(x + 6, baseY + y, z + 1), B.JADE_STONE, 2);
        }
        // Gold trim above entrance
        set(level, new BlockPos(x + 6, baseY + 8, z), GOLD_BLOCK, 2);
        // Obsidian altar (central)
        for (int dx = 4; dx <= 9; dx++) {
            for (int dz = 3; dz <= 6; dz++) {
                set(level, new BlockPos(x + dx, baseY + 1, z + dz), OBSIDIAN, 2);
            }
        }
        set(level, new BlockPos(x + 7, baseY + 2, z + 4), GOLD_BLOCK, 2);
        set(level, new BlockPos(x + 7, baseY + 3, z + 4), SOUL_LANTERN, 2);
        // Candle holders
        set(level, new BlockPos(x + 1, baseY + 1, z + 2), IRON_BARS, 2);
        set(level, new BlockPos(x + 1, baseY + 2, z + 2), SOUL_LANTERN, 2);
        set(level, new BlockPos(x + 13, baseY + 1, z + 2), IRON_BARS, 2);
        set(level, new BlockPos(x + 13, baseY + 2, z + 2), SOUL_LANTERN, 2);
    }

    private static void buildPrayerHall(ServerLevel level, int x, int baseY, int z) {
        // 10×8×6 prayer hall
        for (int dx = 0; dx < 10; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                for (int y = 0; y < 6; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 9 || dz == 0 || dz == 7;
                    boolean isDoor = y < 3 && dx == 5 && dz == 0;
                    if (isEdge && !isDoor) set(level, p, QUARTZ, 2);
                    else if (!isEdge && y == 0) set(level, p, QUARTZ, 2);
                }
            }
        }
        // Roof
        for (int dx = -1; dx <= 10; dx++) {
            for (int dz = -1; dz <= 8; dz++) {
                set(level, new BlockPos(x + dx + 5, baseY + 6, z + dz + 4), QUARTZ_STAIRS, 2);
            }
        }
        // Prayer mats (carpet)
        for (int dx = 2; dx <= 7; dx++) {
            set(level, new BlockPos(x + dx, baseY + 1, z + 2), YELLOW_CARPET, 2);
            set(level, new BlockPos(x + dx, baseY + 1, z + 5), YELLOW_CARPET, 2);
        }
        // Back altar
        set(level, new BlockPos(x + 5, baseY + 1, z + 7), OBSIDIAN, 2);
        set(level, new BlockPos(x + 5, baseY + 2, z + 7), GOLD_BLOCK, 2);
    }

    // ════════════════════════════════════════════════════════════════════
    // TAVERN DISTRICT — Central-south, inns and teahouses
    // ══════════════════════════════════════════════════════════════════

    private static void buildTavernDistrict(ServerLevel level, int cx, int baseY, int cz) {
        int startX = cx - 10;
        int startZ = cz + 10;
        // Birch plank floor
        for (int dx = 0; dx < 20; dx++) {
            for (int dz = 0; dz < 20; dz++) {
                set(level, new BlockPos(startX + dx, baseY, startZ + dz), BIRCH_PLANK, 2);
            }
        }
        // 2 inns (8×6×4 each)
        buildInn(level, startX + 1, baseY, startZ + 1, "Jade Blossom Inn", DARK_OAK_PLANK, SPRUCE_PLANK);
        buildInn(level, startX + 1, baseY, startZ + 9, "Amber Wine House", OAK_PLANK, BIRCH_PLANK);
        // Teahouse (10×6×4)
        buildTeahouse(level, startX + 1, baseY, startZ + 17);
        // Storage cellar under teahouse
        buildStorageCellar(level, startX + 1, baseY, startZ + 17);
        // Lanterns at intersections
        for (int dz = 0; dz < 20; dz += 8) {
            set(level, new BlockPos(startX + 10, baseY + 1, startZ + dz), IRON_BARS, 2);
            set(level, new BlockPos(startX + 10, baseY + 2, startZ + dz), LANTERN, 2);
        }
    }

    private static void buildInn(ServerLevel level, int x, int baseY, int z, String name, BlockState wall, BlockState floor) {
        // 8×6×4 inn
        for (int dx = 0; dx < 8; dx++) {
            for (int dz = 0; dz < 6; dz++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 7 || dz == 0 || dz == 5;
                    boolean isDoor = y < 2 && dx == 4 && dz == 0;
                    boolean isWindow = y >= 2 && y <= 3 && dz == 2;
                    if (isEdge && !isDoor && !isWindow) set(level, p, wall, 2);
                    else if (isWindow) set(level, p, GLASS_PANE, 2);
                    else if (!isEdge && y == 0) set(level, p, floor, 2);
                }
            }
        }
        // Roof
        for (int dx = -1; dx <= 8; dx++) {
            for (int dz = -1; dz <= 6; dz++) {
                set(level, new BlockPos(x + dx + 4, baseY + 4, z + dz + 3), BIRCH_STAIRS, 2);
            }
        }
        // Interior
        for (int dx = 1; dx <= 6; dx++) {
            set(level, new BlockPos(x + dx, baseY + 1, z + 2), OAK_PLANK, 2);
        }
        // Bar counter
        for (int dx = 1; dx <= 5; dx++) {
            set(level, new BlockPos(x + dx, baseY + 1, z + 4), DARK_OAK_PLANK, 2);
        }
        // Chest for valuables
        set(level, new BlockPos(x + 1, baseY + 1, z + 1), CHEST, 2);
        // Brewing stand
        set(level, new BlockPos(x + 6, baseY + 1, z + 4), BREWING_STAND, 2);
        // Furnace
        set(level, new BlockPos(x + 6, baseY + 1, z + 1), FURNACE, 2);
        // Food storage
        set(level, new BlockPos(x + 6, baseY + 1, z + 3), BARREL, 2);
        set(level, new BlockPos(x + 6, baseY + 1, z + 2), BARREL, 2);
        // Lanterns
        set(level, new BlockPos(x + 0, baseY + 1, z + 0), SOUL_LANTERN, 2);
        set(level, new BlockPos(x + 7, baseY + 1, z + 0), SOUL_LANTERN, 2);
    }

    private static void buildTeahouse(ServerLevel level, int x, int baseY, int z) {
        // 10×6×4 teahouse
        for (int dx = 0; dx < 10; dx++) {
            for (int dz = 0; dz < 6; dz++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 9 || dz == 0 || dz == 5;
                    boolean isDoor = y < 2 && dx == 5 && dz == 0;
                    boolean isWindow = y >= 2 && y <= 3 && (dz == 1 || dz == 4);
                    if (isEdge && !isDoor && !isWindow) set(level, p, BIRCH_PLANK, 2);
                    else if (isWindow) set(level, p, GLASS_PANE, 2);
                    else if (!isEdge && y == 0) set(level, p, BIRCH_PLANK, 2);
                }
            }
        }
        // Roof
        for (int dx = -1; dx <= 10; dx++) {
            for (int dz = -1; dz <= 6; dz++) {
                set(level, new BlockPos(x + dx + 5, baseY + 4, z + dz + 3), BIRCH_STAIRS, 2);
            }
        }
        // Interior: low tables for tea service
        for (int dx = 1; dx <= 8; dx++) {
            set(level, new BlockPos(x + dx, baseY + 1, z + 1), OAK_PLANK, 2);
        }
        // Tea preparation area (back)
        set(level, new BlockPos(x + 1, baseY + 1, z + 4), CAULDRON, 2);
        set(level, new BlockPos(x + 2, baseY + 1, z + 4), FURNACE, 2);
        set(level, new BlockPos(x + 3, baseY + 1, z + 4), CRAFTING_TABLE, 2);
        // Tea cups on tables
        set(level, new BlockPos(x + 2, baseY + 1, z + 2), GLASS_BOTTLE(), 2); // Approximation with glass
        set(level, new BlockPos(x + 4, baseY + 1, z + 2), GLASS_BOTTLE(), 2);
        set(level, new BlockPos(x + 6, baseY + 1, z + 2), GLASS_BOTTLE(), 2);
        // Scroll shelf
        for (int dx = 1; dx <= 3; dx++) {
            set(level, new BlockPos(x + dx, baseY + 1, z + 5), BOOKSHELF, 2);
        }
        // Lantern
        set(level, new BlockPos(x + 8, baseY + 1, z + 1), SOUL_LANTERN, 2);
    }

    private static void buildStorageCellar(ServerLevel level, int x, int baseY, int z) {
        // 8×6×3 cellar under the teahouse
        for (int dx = 0; dx < 8; dx++) {
            for (int dz = 0; dz < 6; dz++) {
                BlockPos p = new BlockPos(x + dx, baseY - 1, z + dz);
                boolean isEdge = dx == 0 || dx == 7 || dz == 0 || dz == 5;
                if (isEdge) set(level, p, STONE_BRICK, 2);
                else set(level, p, COBBLESTONE, 2);
            }
        }
        // Barrels
        set(level, new BlockPos(x + 1, baseY - 1, z + 1), BARREL, 2);
        set(level, new BlockPos(x + 3, baseY - 1, z + 1), BARREL, 2);
        set(level, new BlockPos(x + 5, baseY - 1, z + 1), BARREL, 2);
        set(level, new BlockPos(x + 6, baseY - 1, z + 3), BARREL, 2);
    }

    // ════════════════════════════════════════════════════════════════════
    // SMUGGLER TUNNELS — Entrance near east wall, underground
    // ════════════════════════════════════════════════════════════════

    private static void buildSmugglerTunnels(ServerLevel level, int cx, int baseY, int cz) {
        int r = CITY_RADIUS;
        int tx = cx + r - 8;
        int tz = cz + 25;
        // Entrance building (above-ground, disguised as warehouse)
        for (int dx = 0; dx < 8; dx++) {
            for (int dz = 0; dz < 6; dz++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos p = new BlockPos(tx + dx, baseY + y, tz + dz);
                    boolean isEdge = dx == 0 || dx == 7 || dz == 0 || dz == 5;
                    boolean isDoor = y < 2 && dx == 4 && dz == 0;
                    if (isEdge && !isDoor) set(level, p, STONE_BRICK, 2);
                    else if (!isEdge && y == 0) set(level, p, COBBLESTONE, 2);
                }
            }
        }
        // Roof
        for (int dx = 0; dx < 8; dx++) {
            for (int dz = 0; dz < 6; dz++) {
                set(level, new BlockPos(tx + dx, baseY + 4, tz + dz), STONE_SLAB, 2);
            }
        }
        // Door (iron door, hidden behind crates)
        set(level, new BlockPos(tx + 4, baseY + 1, tz), IRON_DOOR, 2);
        set(level, new BlockPos(tx + 4, baseY + 2, tz), AIR, 2);
        // Crates to disguise entrance
        set(level, new BlockPos(tx + 1, baseY + 1, tz + 1), CRATE_BLOCK(), 2);
        set(level, new BlockPos(tx + 2, baseY + 1, tz + 1), CRATE_BLOCK(), 2);
        set(level, new BlockPos(tx + 5, baseY + 1, tz + 1), CRATE_BLOCK(), 2);
        set(level, new BlockPos(tx + 6, baseY + 1, tz + 1), CRATE_BLOCK(), 2);
        // Underground tunnel entrance (trapdoor — air block that's the hole)
        set(level, new BlockPos(tx + 4, baseY, tz + 3), AIR, 2);
        // Stone stairway going down
        for (int y = 0; y < 6; y++) {
            set(level, new BlockPos(tx + 4, baseY - y - 1, tz + 3), STONE_STAIRS, 2);
        }
        // Tunnel (6×3×3 underground)
        for (int dx = 0; dx < 6; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                BlockPos p = new BlockPos(tx + dx + 1, baseY - 6, tz + dz + 1);
                boolean isEdge = dx == 0 || dx == 5 || dz == 0 || dz == 2;
                if (isEdge) set(level, p, STONE_BRICK, 2);
                else set(level, p, AIR, 2);
            }
        }
        // Lantern in tunnel
        set(level, new BlockPos(tx + 3, baseY - 6, tz + 2), SOUL_LANTERN, 2);
        // Storage shelves in tunnel
        set(level, new BlockPos(tx + 1, baseY - 6, tz + 1), BOOKSHELF, 2);
        set(level, new BlockPos(tx + 1, baseY - 6, tz + 2), BOOKSHELF, 2);
    }

    // ══════════════════════════════════════════════════════════════════
    // UTILITY — stained glass, crate block
    // ════════════════════════════════════════════════════════════════

    private static BlockState CRATE_BLOCK() {
        return Blocks.OAK_PLANKS.defaultBlockState();
    }

    private static BlockState STAINED_GLASS() {
        return Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
    }

    private static BlockState GLASS_BOTTLE() {
        return Blocks.GLASS.defaultBlockState();
    }
}
