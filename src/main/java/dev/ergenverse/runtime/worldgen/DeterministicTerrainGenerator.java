package dev.ergenverse.runtime.worldgen;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.spawn.DeterministicSeedHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

/**
 * DeterministicTerrainGenerator — fills in decorative blocks deterministically,
 * constrained by the blueprint.
 *
 * <p><b>Architectural directive (CRON-69, point 9):</b> "This is where
 * procedural generation still has a place. … You've been saying 'no randomness.'
 * I agree. But I wouldn't make every cliff by hand. Instead I'd use deterministic
 * procedural generation constrained by the blueprint. For example: Blueprint
 * says [Forest of Distorted Sense: Boundary, Climate, Elevation, Spirit density,
 * Tree species, Important landmarks, Canonical paths]. Then a deterministic
 * generator fills in: individual tree placement, rock scatter, grass, flowers,
 * fallen logs, small terrain noise. Because the inputs are fixed, the result is
 * identical every new save. You avoid spending years hand-placing millions of
 * decorative blocks while still keeping the geography canon-faithful. Reserve
 * true handcrafting for places that matter narratively."
 *
 * <p><b>What this class does today.</b> It is a <b>pure function</b> from
 * (chunk, blueprint) → a sparse set of canon accent blocks. The base terrain
 * (stone columns, dirt, grass, biome trees/flowers) is already produced
 * deterministically by minecraft:noise seeded with
 * {@link DeterministicSeedHandler#CANON_SEED}, so this class does NOT
 * regenerate that — it only layers canon-specific accents that biome features
 * would not produce: e.g. a few "distortion marker" blocks inside the Forest of
 * Distorted Sense, or bone/soul-sand accents at the Sea of Devils edge. The
 * placement is a pure hash of (chunk coords + CANON_SEED), so every new save
 * gets the identical scatter. No hand-placement, no randomness.
 *
 * <p>This is the foundation for future handcraft-free decoration (deterministic
 * tree/rock/grass scatter in canon regions). It is deliberately modest today —
 * the principle is established, the surface is extensible.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class DeterministicTerrainGenerator {

    private DeterministicTerrainGenerator() {}

    /** Square of the radius (in blocks) within which a canon geographic region gets accents. */
    private static final int REGION_RADIUS = 120;
    private static final int REGION_RADIUS_SQ = REGION_RADIUS * REGION_RADIUS;

    /**
     * Deterministically decorate a chunk with canon-region accent blocks.
     *
     * <p>Pure function of (chunkX, chunkZ, blueprint): same inputs → same output
     * across every save. Reads the surface height from the live level (which is
     * itself deterministic via CANON_SEED), so the accent blocks land on the
     * actual surface.
     */
    public static void decorate(int chunkX, int chunkZ, PlanetSuzakuBlueprint blueprint, ServerLevel level) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        // For each canon geographic region, if this chunk falls within its
        // accent radius, deterministically place a few accent blocks.
        for (PlanetSuzakuBlueprint.CanonLocation loc : List.of(
                PlanetSuzakuBlueprint.FOREST_OF_DISTORTED_SENSE,
                PlanetSuzakuBlueprint.SEA_OF_DEVILS,
                PlanetSuzakuBlueprint.SUZAKU_TOMB)) {

            // Quick reject: chunk center vs region center.
            int cx = baseX + 8, cz = baseZ + 8;
            int dx = cx - loc.x, dz = cz - loc.z;
            if (dx * dx + dz * dz > REGION_RADIUS_SQ) continue;

            // Deterministic per-chunk seed: mix CANON_SEED with chunk coords.
            long seed = mix(mix(DeterministicSeedHandler.CANON_SEED, chunkX), chunkZ) ^ loc.id.hashCode();

            switch (loc.id) {
                case "forest_of_distorted_sense" -> decorateDistortedForest(baseX, baseZ, seed, level);
                case "sea_of_devils" -> decorateSeaOfDevils(baseX, baseZ, seed, level);
                case "suzaku_tomb" -> decorateSuzakuTomb(baseX, baseZ, seed, level);
                default -> { /* no accent for this region yet */ }
            }
        }
    }

    // ── Region accents ──────────────────────────────────────────────────

    /** Forest of Distorted Sense: a sparse scatter of warped-nylium + shroomlights, canon-flavored. */
    private static void decorateDistortedForest(int baseX, int baseZ, long seed, ServerLevel level) {
        // Deterministically pick 3 accent columns in the chunk.
        for (int i = 0; i < 3; i++) {
            long s = mix(seed, i);
            int lx = (int) (s & 0xF);
            int lz = (int) ((s >>> 8) & 0xF);
            int worldX = baseX + lx, worldZ = baseZ + lz;
            int y = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(worldX, 0, worldZ)).getY();
            if (y <= level.getMinBuildHeight() + 1) continue;
            BlockState accent = (s & 1) == 0 ? Blocks.WARPED_NYLIUM.defaultBlockState()
                                             : Blocks.SHROOMLIGHT.defaultBlockState();
            trySet(level, worldX, y, worldZ, accent);
        }
    }

    /** Sea of Devils (修魔海) edge: soul-sand + bone accents, canon-faithful "demon cultivation sea". */
    private static void decorateSeaOfDevils(int baseX, int baseZ, long seed, ServerLevel level) {
        for (int i = 0; i < 2; i++) {
            long s = mix(seed, i + 7);
            int lx = (int) (s & 0xF);
            int lz = (int) ((s >>> 8) & 0xF);
            int worldX = baseX + lx, worldZ = baseZ + lz;
            int y = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(worldX, 0, worldZ)).getY();
            if (y <= level.getMinBuildHeight() + 1) continue;
            BlockState accent = (s & 1) == 0 ? Blocks.SOUL_SAND.defaultBlockState()
                                             : Blocks.BONE_BLOCK.defaultBlockState();
            trySet(level, worldX, y, worldZ, accent);
        }
    }

    /** Suzaku Tomb surface: a sparse ring of crying-obsidian markers above the underground tomb. */
    private static void decorateSuzakuTomb(int baseX, int baseZ, long seed, ServerLevel level) {
        long s = mix(seed, 13);
        int lx = (int) (s & 0xF);
        int lz = (int) ((s >>> 8) & 0xF);
        int worldX = baseX + lx, worldZ = baseZ + lz;
        int y = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(worldX, 0, worldZ)).getY();
        if (y <= level.getMinBuildHeight() + 1) return;
        trySet(level, worldX, y, worldZ, Blocks.CRYING_OBSIDIAN.defaultBlockState());
    }

    // ── helpers ─────────────────────────────────────────────────────────

    private static void trySet(ServerLevel level, int x, int y, int z, BlockState state) {
        try {
            // Only replace air or generic foliage; never overwrite a built structure.
            BlockState existing = level.getBlockState(new BlockPos(x, y, z));
            if (!existing.isAir() && !existing.getCollisionShape(level, new BlockPos(x, y, z)).isEmpty()) return;
            level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_ALL);
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] DeterministicTerrainGenerator trySet failed: {}", t.getMessage());
        }
    }

    /** Splittable mix function (xorshift-style) — pure, deterministic. */
    private static long mix(long a, long b) {
        long h = a ^ (b * 0x9E3779B97F4A7C15L);
        h ^= h >>> 30;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 27;
        h *= 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return h;
    }
}
