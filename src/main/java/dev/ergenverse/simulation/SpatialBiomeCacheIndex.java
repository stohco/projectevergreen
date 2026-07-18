package dev.ergenverse.simulation;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SpatialBiomeCacheIndex — the chunk→location resolver.
 *
 * <p><b>The problem this solves:</b> the simulation engine reasons in terms of
 * canonical location IDs ({@code zhao_plains}, {@code teng_family_city}, etc.),
 * but Minecraft reasons in terms of chunk coordinates and noise-driven biomes.
 * Something has to translate {@code ChunkPos(x, z)} → {@code "zhao_plains"}.
 *
 * <h2>Why NOT a rectangle grid</h2>
 * <p>The prior design (rejected) registered fixed chunk-coordinate rectangles
 * for each location. This desyncs from reality because Planet Suzaku uses
 * {@code minecraft:multi_noise} biome source — biome boundaries are curved
 * noise contours, not axis-aligned boxes. A rectangle registered as
 * "zhao_mountains" would overlap chunks that actually generate as
 * "zhao_plains" or "sea_of_devils", causing the simulation to claim one
 * location while the voxels show another. That violates the Prime Directive
 * ("reality is objective").
 *
 * <h2>The correct approach: lazy biome sampling</h2>
 * <p>When a chunk is fully generated, its center biome is already computed
 * by worldgen. We query {@link ServerLevel#getBiome(BlockPos)} ONCE — no
 * cascading generation — and cache the result keyed by
 * {@link ChunkPos#toLong()}. Subsequent lookups are O(1) primitive map
 * reads. The cache is per-dimension (pocket dimensions have their own
 * biome→location mappings) and in-memory (rebuilt lazily on server restart,
 * which is acceptable because biomes are deterministic per seed).
 *
 * <h2>ChunkStatus guard</h2>
 * <p>{@link ChunkAccess#getStatus()} reflects the chunk's generation phase.
 * We ONLY sample biomes from chunks at {@link ChunkStatus#FULL} — anything
 * less means worldgen hasn't finished computing biomes, and calling
 * {@code getBiome} could trigger a noise sampling pass (cascade risk).
 *
 * <h2>Thread safety</h2>
 * <p>The outer {@code Map<ResourceKey<Level>, ...>} is a
 * {@link ConcurrentHashMap}. The inner {@link Long2ObjectOpenHashMap} is NOT
 * thread-safe by default, so all access is guarded by {@code synchronized}
 * blocks on the inner map instance. This is correct because the inner map
 * is per-dimension and dimension switches are rare.
 *
 * <h2>Memory</h2>
 * <p>Each entry is a {@code long} (8 bytes) → {@code String} reference (8 bytes)
 * in a fastutil open-addressing map. ~16 bytes per chunk cached. A fully
 * explored 5000×5000 block area = ~100k chunks = ~1.6 MB. Acceptable.
 */
public final class SpatialBiomeCacheIndex {

    /** Singleton instance — one cache per server. */
    private static final SpatialBiomeCacheIndex INSTANCE = new SpatialBiomeCacheIndex();

    public static SpatialBiomeCacheIndex getInstance() {
        return INSTANCE;
    }

    /**
     * Per-dimension cache: {@code ResourceKey<Level> → (chunkLong → locationId)}.
     * ConcurrentHashMap for safe computeIfAbsent on the outer map.
     */
    private final Map<ResourceKey<Level>, Long2ObjectOpenHashMap<String>> spatialCache =
            new ConcurrentHashMap<>();

    private SpatialBiomeCacheIndex() {}

    // ═══════════════════════════════════════════════════════════════════
    //  Resolution
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Resolve a chunk's canonical location ID by sampling its center biome.
     *
     * <p><b>Guard:</b> only processes chunks at {@link ChunkStatus#FULL}.
     * Returns {@code null} for non-full chunks (caller should skip).
     *
     * @param level the server level (must match the chunk's dimension)
     * @param chunk the chunk to resolve
     * @return the canonical location ID (e.g. {@code "zhao_plains"}), or
     *         {@code "unmapped_wilderness"} if the biome isn't mapped to a
     *         canon location, or {@code null} if the chunk isn't full
     */
    @Nullable
    public String resolveAndCacheChunk(ServerLevel level, ChunkAccess chunk) {
        // ── Guard: block on unfinalized chunks to prevent worldgen cascades ──
        if (!chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
            return null;
        }

        ResourceKey<Level> dimKey = level.dimension();
        Long2ObjectOpenHashMap<String> dimCache =
                spatialCache.computeIfAbsent(dimKey, k -> new Long2ObjectOpenHashMap<>());

        ChunkPos chunkPos = chunk.getPos();
        long chunkLong = chunkPos.toLong();

        // ── Fast path: cached ────────────────────────────────────────────
        synchronized (dimCache) {
            if (dimCache.containsKey(chunkLong)) {
                return dimCache.get(chunkLong);
            }
        }

        // ── Slow path: sample biome at chunk center ─────────────────────
        // Center of the chunk at y=64 (surface-ish; biomes in 1.20.1 are
        // 3D but for overworld-like dims the y=64 sample matches worldgen).
        int centerX = (chunkPos.x << 4) + 8;
        int centerZ = (chunkPos.z << 4) + 8;
        BlockPos centerPos = new BlockPos(centerX, 64, centerZ);

        Holder<Biome> biomeHolder = level.getBiome(centerPos);
        String biomeId = biomeHolder.unwrapKey()
                .map(key -> key.location().toString())
                .orElse("minecraft:ocean");

        String locationId = mapBiomeToLocationId(biomeId);

        synchronized (dimCache) {
            dimCache.put(chunkLong, locationId);
        }
        return locationId;
    }

    /**
     * Look up a cached resolution without sampling (returns null if not cached).
     * Useful for the reification scan to avoid re-sampling chunks we've seen.
     */
    @Nullable
    public String getCached(ResourceKey<Level> dimKey, long chunkLong) {
        Long2ObjectOpenHashMap<String> dimCache = spatialCache.get(dimKey);
        if (dimCache == null) return null;
        synchronized (dimCache) {
            return dimCache.get(chunkLong);
        }
    }

    /**
     * Map a Minecraft biome ID (e.g. {@code "ergenverse:zhao_plains"}) to a
     * canonical location ID.
     *
     * <p>For v1, this is a simple substring match. The biome IDs already use
     * canon location names (e.g. {@code ergenverse:zhao_mountains}), so the
     * biome ID IS the location ID with the namespace stripped. For unmapped
     * biomes (vanilla oceans, etc.), returns {@code "unmapped_wilderness"}.
     *
     * <p>Future: replace with a JSON-driven map at
     * {@code data/ergenverse/biome_to_location_map.json} for full control
     * over biome→location mapping (including sub-region resolution for
     * biomes like {@code zhao_spirit_herb_hills} → {@code zhao_plains}).
     */
    public String mapBiomeToLocationId(String biomeId) {
        // Strip namespace: "ergenverse:zhao_plains" → "zhao_plains"
        if (biomeId.startsWith("ergenverse:")) {
            String path = biomeId.substring("ergenverse:".length());

            // Map sub-region biomes to their parent country/location.
            // e.g. zhao_spirit_herb_hills, zhao_minor_spirit_vein_region → zhao_plains
            // (These are sub-biomes within Zhao; for v1 we treat them as Zhao Plains.)
            if (path.startsWith("zhao_") && !path.equals("zhao_mountains")) {
                return "zhao_plains";
            }
            // Strip the _country/_kingdom suffix for canonical location ID
            if (path.endsWith("_country") || path.endsWith("_kingdom")) {
                return path.substring(0, path.lastIndexOf('_'));
            }
            return path;
        }
        // Vanilla or unmapped biome
        return "unmapped_wilderness";
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Cache management
    // ═══════════════════════════════════════════════════════════════════

    /** Clear the entire cache (all dimensions). Called on world unload. */
    public void invalidateAll() {
        spatialCache.clear();
    }

    /** Clear one dimension's cache. */
    public void invalidateDimension(ResourceKey<Level> dimKey) {
        spatialCache.remove(dimKey);
    }

    /** Total cached entries across all dimensions (for debug). */
    public int totalCached() {
        int sum = 0;
        for (Long2ObjectOpenHashMap<String> map : spatialCache.values()) {
            synchronized (map) {
                sum += map.size();
            }
        }
        return sum;
    }
}
