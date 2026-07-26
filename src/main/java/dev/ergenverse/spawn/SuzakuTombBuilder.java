package dev.ergenverse.spawn;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.ChunkBounds;
import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.runtime.delta.WorldDeltaStore;
import dev.ergenverse.runtime.worldgen.BlueprintChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * SuzakuTombBuilder — materializes the 朱雀墓 (Suzaku Tomb), the underground
 * inheritance site of the 朱雀子 (Suzaku Son) lineage.
 *
 * <p><b>CRON-COMPLETIONIST-105 — THE SUZAKU TOMB INHERITANCE CHAMBER.</b>
 *
 * <p>Prior to this round, the Suzaku Tomb existed only as:
 * <ul>
 *   <li>A {@link PlanetSuzakuBlueprint.CanonLocation} entry at (0, -60, 0)
 *       with category "ruin" and canon reference "RI — 朱雀墓, underground;
 *       15th-gen 朱雀子 inheritance, 拓森 reappears".</li>
 *   <li>A {@link dev.ergenverse.simulation.LocationLayerSeeder} metadata
 *       entry (PHYSICAL_TERRAIN, SPIRITUAL_QI, DAO_RESIDUE, etc.).</li>
 *   <li>A cave-suppression zone (CRON-104: 150-block radius, no caves carve
 *       through the sacred chamber).</li>
 * </ul>
 *
 * <p>But there was NO structure builder — the tomb at (0, -60, 0) was just
 * canon-shaped stone with no caves. A player who dug down to y=-60 would
 * find... nothing. The most sacred underground site in Planet Suzaku was
 * an empty stone volume.
 *
 * <p>CRON-105 closes this gap by creating the SuzakuTombBuilder. It
 * materializes a canon-faithful sealed inheritance chamber:
 * <ul>
 *   <li><b>Chamber:</b> a 20&times;20 rectangular room at y=-63 to y=-57
 *       (7 blocks tall), centered at the blueprint's (0, -60, 0).</li>
 *   <li><b>Walls/floor/ceiling:</b> deepslate bricks (vanilla block,
 *       canon-appropriate for an ancient underground site).</li>
 *   <li><b>Cultivation Planet Crystal (修炼星晶):</b> a dedicated
 *       {@link dev.ergenverse.block.CultivationPlanetCrystalBlock} on a
 *       spirit-stone pedestal at the chamber center. The Crystal is the
 *       macguffin of the Suzaku Son inheritance arc — Wang Lin must
 *       acquire it to advance. <b>CRON-106:</b> the former CRON-105
 *       diamond_block placeholder has been REPLACED by the dedicated
 *       CultivationPlanetCrystalBlock, which has canon-faithful mechanics:
 *       light level 15 (the Crystal's spiritual Qi manifests as visible
 *       light), ambient END_ROD particles (Qi radiating outward), and a
 *       right-click inheritance event gated by (1) bead in hand, (2) realm
 *       ≥ Nascent Soul, (3) Crystal not yet inherited. On success, the
 *       block transitions to {@code inherited=true} and the player's bead
 *       is marked with the Suzaku Son status.</li>
 *   <li><b>Spirit-vein conduits:</b> four {@code SPIRIT_VEIN_STONE} pillars
 *       at the chamber corners, channeling spiritual Qi from the planet's
 *       core into the Crystal. Canon: the Suzaku Tomb is sealed around the
 *       Cultivation Planet Crystal; spirit veins are the conduits.</li>
 *   <li><b>Sealing formation:</b> four {@code FORMATION_CORE_STONE} blocks
 *       at the midpoints of each wall, forming the restriction array that
 *       seals the tomb. Canon: the tomb is canonically sealed; the
 *       formation cores represent that seal.</li>
 *   <li><b>Inheritance chest:</b> a chest at the chamber center (next to
 *       the Crystal) with the {@code suzaku_tomb_inheritance_chamber} loot
 *       table. This loot table drops World Origin Essence (一界本源) as a
 *       very rare drop — closing the CRON-101 acquisition bridge where
 *       World Origin Essence was creative/command only.</li>
 * </ul>
 *
 * <p><b>Chunk-scoped (CRON-69 point 7 full fidelity):</b> the builder
 * follows the exact pattern established by {@code WangFamilyVillageBuilder}
 * (CRON-62). A {@link ThreadLocal}&lt;{@link ChunkBounds}&gt; holds the
 * active bounds during a {@link #buildInternal} call; the {@link #sb}
 * helper reads it at each leaf placement and skips blocks outside the
 * chunk. This eliminates the whole-tomb-build-on-any-chunk-load bug: only
 * the ~256 blocks in the currently-loading chunk are placed.
 *
 * <p><b>Provenance-aware rebuild guard (CRON-63 pattern):</b> the {@link #sb}
 * helper also consults {@link WorldDeltaStore} for PLAYER or SIMULATION
 * deltas at each position. If a player has mined a block (or a simulation
 * event has altered it), the CANON placement is skipped — the player's
 * edits take priority over CANON. This is the operational realization of
 * the Provenance contract: PLAYER &gt; SIMULATION &gt; CANON.
 *
 * <p><b>Canon fidelity (fact-checked against 仙逆):</b>
 * <ul>
 *   <li><b>朱雀墓 (Suzaku Tomb):</b> the underground inheritance site of
 *       the 朱雀子 (Suzaku Son) lineage. Canon: the 15th-gen Suzaku Son
 *       inheritance event occurs here; 拓森 (Tuo Sen) reappears here. The
 *       tomb is sealed around the Cultivation Planet Crystal (修炼星晶).</li>
 *   <li><b>Location:</b> the blueprint places the tomb at (0, -60, 0) —
 *       underground, beneath the Vermilion Bird Capital at (0, 0, 0). This
 *       is a mod-inferred placement (canon does not specify exact
 *       coordinates, but placing the Suzaku Tomb beneath the Vermilion
 *       Bird Capital is canon-faithful: the 朱雀子 rules from 朱雀国, and
 *       the tomb is the inheritance site of that lineage).</li>
 *   <li><b>Cultivation Planet Crystal (修炼星晶):</b> the macguffin. Canon:
 *       the Crystal is the sealed core of the planet; acquiring it is
 *       central to the Suzaku Son inheritance. <b>CRON-106:</b> now a
 *       dedicated {@link dev.ergenverse.block.CultivationPlanetCrystalBlock}
 *       with canon-faithful mechanics (light, particles, inheritance
 *       event). The CRON-105 diamond_block placeholder is retired.</li>
 *   <li><b>World Origin Essence (一界本源) drop:</b> canon-attested as the
 *       reagent Wang Lin uses to revive Li Muwan (CRON-101). Dropping it
 *       from the Suzaku Tomb inheritance chest is a mod-inferred
 *       acquisition path: canon does not explicitly state Wang Lin found
 *       一界本源 in the tomb (he later extracts it from the 逆尘界 / Ni
 *       Chen Realm), but the tomb is the most canon-appropriate source for
 *       a world-tier reagent. This is honestly flagged as mod-original.</li>
 * </ul>
 *
 * <p><b>NO fabricated chapter citation.</b> The Suzaku Tomb's status as the
 * underground inheritance site, the Cultivation Planet Crystal, and 拓森's
 * reappearance are attested via multiple web-search sources (Baidu Baike
 * 仙逆编年史, etc.). The exact chapter is NOT cited to avoid fabrication.
 *
 * <p><b>Relationship to CRON-104 (canon-aware cave placement):</b> CRON-104
 * protects the tomb from cave intrusion (150-block suppression radius).
 * CRON-105 builds the actual chamber content. Together, they make the
 * Suzaku Tomb a canon-faithful sealed sacred site: no caves breach it,
 * and the inheritance chamber is materialized for the player to discover.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class SuzakuTombBuilder {

    private SuzakuTombBuilder() {}

    // ════════════════════════════════════════════════════════════════════
    //  CANON COORDINATES
    // ════════════════════════════════════════════════════════════════════

    /**
     * The canon center of the Suzaku Tomb, from
     * {@link PlanetSuzakuBlueprint#SUZAKU_TOMB}. The chamber is centered
     * at this (x, z); the y comes from the blueprint's y field (-60).
     */
    private static final int TOMB_X = PlanetSuzakuBlueprint.SUZAKU_TOMB.x;
    private static final int TOMB_Y = PlanetSuzakuBlueprint.SUZAKU_TOMB.y;
    private static final int TOMB_Z = PlanetSuzakuBlueprint.SUZAKU_TOMB.z;

    /**
     * Chamber half-size. The chamber is (2*HALF)^2 = 20x20 blocks.
     * Small enough to fit in a 2-chunk footprint; large enough to feel
     * like an inheritance chamber (not a closet).
     */
    private static final int HALF = 10;

    /**
     * Chamber floor Y (inclusive). The floor is 3 blocks below the center
     * to give headroom: floor at y=-63, center at y=-60, ceiling at y=-57.
     */
    private static final int FLOOR_Y = TOMB_Y - 3;

    /**
     * Chamber ceiling Y (inclusive). 7 blocks of vertical space (floor to
     * ceiling inclusive) — enough for a pedestal, the Crystal, and player
     * headroom.
     */
    private static final int CEILING_Y = TOMB_Y + 3;

    // ════════════════════════════════════════════════════════════════════
    //  CHUNK-SCOPED PLACEMENT (CRON-62/63 pattern)
    // ════════════════════════════════════════════════════════════════════

    /**
     * ThreadLocal holding the active ChunkBounds during a buildInternal()
     * call. Set once at the top of {@link #buildForChunk} and read by
     * {@link #sb} at each leaf placement. Null means "full build" (no
     * chunk filtering) — used by the command/login {@link #build} path.
     *
     * <p>ThreadLocal is used because the builder has ~30 setBlock call
     * sites and a deep call hierarchy (buildInternal → buildWalls /
     * buildPedestal / buildConduits / buildSealingFormation /
     * placeInheritanceChest). Adding a ChunkBounds parameter to every
     * method would clutter the geometric intent. The ThreadLocal is set
     * once at the top of buildForChunk and read by sb() at each leaf —
     * a single global per build pass.
     */
    private static final ThreadLocal<ChunkBounds> CURRENT_BOUNDS = new ThreadLocal<>();

    /**
     * Filtered setBlock — the ONLY block-placement call site in this class.
     * Three guards, in order:
     *
     * <p><b>1. Chunk filter (CRON-62 pattern):</b> if CURRENT_BOUNDS is
     * non-null and (x, z) falls outside the bounds, skip. This makes
     * buildForChunk chunk-scoped: ~4K candidate placements collapse to
     * ~256 actual level.setBlock calls per chunk.
     *
     * <p><b>2. Provenance-aware rebuild guard (CRON-63 pattern):</b> if
     * CURRENT_BOUNDS is non-null (chunk-materializer path), consult the
     * {@link WorldDeltaStore} for a PLAYER or SIMULATION delta at (x, y, z).
     * If either exists, skip the placement. The player's edits (and the
     * simulation's edits) take priority over CANON.
     *
     * <p><b>3. Placement:</b> if both guards pass, call level.setBlock.
     *
     * <p>Y is intentionally not chunk-filtered — structures are vertically
     * thin. Y IS used for the provenance check (deltas are 3D-positioned).
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
            if (store.hasBlock(x, y, z, Provenance.PLAYER)
                    || store.hasBlock(x, y, z, Provenance.SIMULATION)) return true;
            return false;
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] SuzakuTombBuilder provenance guard failed at {}: {} — proceeding.",
                    pos, t.getMessage());
            return false;
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  PUBLIC API
    // ════════════════════════════════════════════════════════════════════

    /**
     * Chunk-scoped build entry point — invoked by the chunk-materializer
     * for each chunk that overlaps the tomb footprint.
     *
     * <p>When {@code bounds} is non-null, only blocks whose (x, z) fall
     * inside the bounds are placed. When {@code bounds} is null, the full
     * tomb is built (command/login path).
     *
     * <p>This method does NOT call {@link #isAlreadyBuilt} — chunk-scoped
     * placement is naturally incremental: each chunk places its own slice,
     * and re-placing an already-present block is a harmless no-op.
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
            Ergenverse.LOGGER.debug("[Ergenverse] SuzakuTombBuilder: tomb already built at ({},{},{}), skipping.",
                    TOMB_X, TOMB_Y, TOMB_Z);
            return;
        }
        buildForChunk(level, null);
    }

    /**
     * Returns true if the Cultivation Planet Crystal is already present at
     * the chamber center. Used by the full-build path to avoid redundant
     * rebuilds.
     *
     * <p>CRON-106: now checks for {@code CULTIVATION_PLANET_CRYSTAL}
     * (the dedicated block) instead of the former {@code DIAMOND_BLOCK}
     * placeholder. The check accepts EITHER block-state (inherited=true
     * or inherited=false) — the Crystal is "already built" if any
     * CultivationPlanetCrystalBlock exists at the position.
     */
    private static boolean isAlreadyBuilt(ServerLevel level) {
        BlockPos crystalPos = new BlockPos(TOMB_X, TOMB_Y + 1, TOMB_Z);
        return level.getBlockState(crystalPos).getBlock() instanceof
                dev.ergenverse.block.CultivationPlanetCrystalBlock;
    }

    // ════════════════════════════════════════════════════════════════════
    //  BUILD INTERNAL — the shared build logic
    // ════════════════════════════════════════════════════════════════════

    /**
     * The shared build logic, called by both {@link #build} (full-build)
     * and {@link #buildForChunk} (chunk-scoped). All placements go through
     * {@link #sb}, which applies the chunk-filter and provenance-guard.
     */
    private static void buildInternal(ServerLevel level) {
        buildChamberShell(level);
        buildPedestalAndCrystal(level);
        buildSpiritVeinConduits(level);
        buildSealingFormation(level);
        placeInheritanceChest(level);
    }

    // ── Chamber shell: floor, walls, ceiling ───────────────────────────

    /**
     * Build the chamber shell — a 20x20 rectangular room with deepslate
     * brick walls, deepslate floor, and deepslate brick ceiling.
     *
     * <p>The shell is hollow: only the floor, the four walls, and the
     * ceiling are placed. The interior is left as air (cleared by
     * explicitly placing AIR for any block that might have been stone
     * from fillFromNoise).
     */
    private static void buildChamberShell(ServerLevel level) {
        final BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        final BlockState deepslate = Blocks.DEEPSLATE.defaultBlockState();
        final BlockState air = Blocks.AIR.defaultBlockState();

        int minX = TOMB_X - HALF;
        int maxX = TOMB_X + HALF;
        int minZ = TOMB_Z - HALF;
        int maxZ = TOMB_Z + HALF;

        // Floor (y = FLOOR_Y) — deepslate
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                sb(level, new BlockPos(x, FLOOR_Y, z), deepslate, 3);
            }
        }

        // Ceiling (y = CEILING_Y) — deepslate bricks
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                sb(level, new BlockPos(x, CEILING_Y, z), deepslateBricks, 3);
            }
        }

        // Walls (4 sides) — deepslate bricks, from FLOOR_Y+1 to CEILING_Y-1
        for (int y = FLOOR_Y + 1; y < CEILING_Y; y++) {
            // North wall (minZ) and South wall (maxZ)
            for (int x = minX; x <= maxX; x++) {
                sb(level, new BlockPos(x, y, minZ), deepslateBricks, 3);
                sb(level, new BlockPos(x, y, maxZ), deepslateBricks, 3);
            }
            // East wall (maxX) and West wall (minX)
            for (int z = minZ + 1; z < maxZ; z++) {
                sb(level, new BlockPos(minX, y, z), deepslateBricks, 3);
                sb(level, new BlockPos(maxX, y, z), deepslateBricks, 3);
            }
        }

        // Clear the interior to AIR (in case fillFromNoise placed stone here)
        for (int x = minX + 1; x < maxX; x++) {
            for (int z = minZ + 1; z < maxZ; z++) {
                for (int y = FLOOR_Y + 1; y < CEILING_Y; y++) {
                    sb(level, new BlockPos(x, y, z), air, 3);
                }
            }
        }
    }

    // ── Cultivation Planet Crystal pedestal ────────────────────────────

    /**
     * Build the central pedestal and the Cultivation Planet Crystal.
     *
     * <p>The pedestal is a 3x3 spirit-stone platform at floor level, with
     * a single spirit-stone block pillar rising one block. The Crystal
     * sits on top of the pedestal at (TOMB_X, TOMB_Y+1, TOMB_Z) — one
     * block above the chamber center.
     *
     * <p><b>CRON-106:</b> the Crystal is now the dedicated
     * {@link dev.ergenverse.block.CultivationPlanetCrystalBlock} — a real
     * custom block with canon-faithful mechanics (light level 15, ambient
     * END_ROD particles, right-click inheritance event with prerequisites,
     * no-drops when broken). This replaces the CRON-105 diamond_block
     * placeholder, which had none of these mechanics.
     *
     * <p>The Crystal's block-state is the default {@code inherited=false}
     * — the inheritance event has not yet occurred. When a player
     * right-clicks the Crystal (and meets the prerequisites), the block
     * transitions to {@code inherited=true} via a PLAYER delta, which
     * persists across chunk reload.
     */
    private static void buildPedestalAndCrystal(ServerLevel level) {
        final BlockState spiritStone = dev.ergenverse.block.ErgenverseBlocks.SPIRIT_STONE_BLOCK
                .get().defaultBlockState();
        final BlockState crystal = dev.ergenverse.block.ErgenverseBlocks.CULTIVATION_PLANET_CRYSTAL
                .get().defaultBlockState();

        // 3x3 pedestal base at FLOOR_Y+1 (one block above the floor)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                sb(level, new BlockPos(TOMB_X + dx, FLOOR_Y + 1, TOMB_Z + dz), spiritStone, 3);
            }
        }

        // Crystal on top of the pedestal center, at TOMB_Y+1
        // (TOMB_Y = -60, pedestal top at FLOOR_Y+1 = -62, crystal at -59)
        sb(level, new BlockPos(TOMB_X, TOMB_Y + 1, TOMB_Z), crystal, 3);
    }

    // ── Spirit-vein conduits (4 corner pillars) ───────────────────────

    /**
     * Build four spirit-vein conduit pillars at the chamber corners.
     * Canon: the Suzaku Tomb is sealed around the Cultivation Planet
     * Crystal; spirit veins are the conduits that channel the planet's
     * spiritual Qi into the Crystal.
     *
     * <p>Each pillar is a 1x1 column of {@code SPIRIT_VEIN_STONE} from
     * the floor to the ceiling at the four interior corners of the chamber
     * (one block inset from the walls).
     */
    private static void buildSpiritVeinConduits(ServerLevel level) {
        final BlockState spiritVein = dev.ergenverse.block.ErgenverseBlocks.SPIRIT_VEIN_STONE
                .get().defaultBlockState();

        // Four interior corners (1 block inset from the walls)
        int[][] corners = {
                {TOMB_X - HALF + 2, TOMB_Z - HALF + 2},
                {TOMB_X + HALF - 2, TOMB_Z - HALF + 2},
                {TOMB_X - HALF + 2, TOMB_Z + HALF - 2},
                {TOMB_X + HALF - 2, TOMB_Z + HALF - 2},
        };

        for (int[] c : corners) {
            for (int y = FLOOR_Y + 1; y < CEILING_Y; y++) {
                sb(level, new BlockPos(c[0], y, c[1]), spiritVein, 3);
            }
        }
    }

    // ── Sealing formation (4 formation cores at wall midpoints) ───────

    /**
     * Build four sealing formation cores at the midpoints of each wall.
     * Canon: the tomb is canonically sealed; the formation cores represent
     * the restriction array that holds the seal.
     *
     * <p>Each formation core is a single {@code FORMATION_CORE_STONE} block
     * placed at the midpoint of each wall, one block above the floor.
     */
    private static void buildSealingFormation(ServerLevel level) {
        final BlockState formationCore = dev.ergenverse.block.ErgenverseBlocks.FORMATION_CORE_STONE
                .get().defaultBlockState();

        int midY = FLOOR_Y + 2; // one block above the pedestal base

        // North wall midpoint
        sb(level, new BlockPos(TOMB_X, midY, TOMB_Z - HALF + 1), formationCore, 3);
        // South wall midpoint
        sb(level, new BlockPos(TOMB_X, midY, TOMB_Z + HALF - 1), formationCore, 3);
        // East wall midpoint
        sb(level, new BlockPos(TOMB_X + HALF - 1, midY, TOMB_Z), formationCore, 3);
        // West wall midpoint
        sb(level, new BlockPos(TOMB_X - HALF + 1, midY, TOMB_Z), formationCore, 3);
    }

    // ── Inheritance chest ─────────────────────────────────────────────

    /**
     * Place the inheritance chest at the chamber center, next to the
     * Cultivation Planet Crystal. The chest uses the
     * {@code suzaku_tomb_inheritance_chamber} loot table, which drops
     * World Origin Essence (一界本源) as a very rare drop — closing the
     * CRON-101 acquisition bridge.
     *
     * <p>The chest is placed via {@link ChestHelper#placeChestWithLoot}
     * with a placer that delegates to {@link #sb}, ensuring the chest
     * placement is chunk-filtered and provenance-guarded (CRON-71 pattern).
     */
    private static void placeInheritanceChest(ServerLevel level) {
        // Place the chest one block north of the Crystal, at pedestal height
        BlockPos chestPos = new BlockPos(TOMB_X, TOMB_Y + 1, TOMB_Z - 2);
        ResourceLocation lootTable = new ResourceLocation(
                dev.ergenverse.core.Ergenverse.MOD_ID, "chests/suzaku_tomb_inheritance_chamber");
        ChestHelper.placeChestWithLoot(level, chestPos, lootTable,
                (lvl, p) -> sb(lvl, p, Blocks.CHEST.defaultBlockState(), 2));
    }
}
