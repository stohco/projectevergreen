package dev.ergenverse.runtime;

import javax.annotation.Nullable;

/**
 * ChunkBounds — an inclusive rectangular bounding box in <b>block</b> coordinates,
 * used to filter structure-builder placements to a single chunk.
 *
 * <p><b>Architectural directive (CRON-COMPLETIONIST-62, priority c):</b>
 * Prior to this round, {@link dev.ergenverse.runtime.materialize.StructureBuilderRegistry#build}
 * invoked each builder with only a {@code ServerLevel} — no chunk context. The
 * builder then placed its <i>entire</i> structure (e.g. Wang Family Village,
 * 83&times;83 = 6,889 ground blocks + ~70K building/road/fence blocks spanning
 * a 6&times;6-chunk footprint) on <i>every</i> chunk load that overlapped the
 * structure. Most writes landed in unloaded chunks, forcing cascading
 * synchronous chunk loads. The result was a per-chunk-load catastrophe: each
 * of the 36 village chunks triggered a full ~80K-block rebuild.
 *
 * <p>{@code ChunkBounds} is the value type that closes this gap. The
 * {@link dev.ergenverse.runtime.materialize.PlanetSuzakuChunkMaterializer}
 * derives a {@code ChunkBounds} from the loaded {@code ChunkPos} and passes it
 * to {@link dev.ergenverse.runtime.materialize.StructureBuilderRegistry#build}.
 * Each builder's {@code buildForChunk(level, bounds)} filters its placements
 * via {@link #contains(int, int)} — blocks outside the bounds are skipped
 * before the expensive {@code level.setBlock} call.
 *
 * <p><b>Null semantics:</b> a {@code null} {@code ChunkBounds} means "no
 * filtering — place all blocks". This is the full-build path used by commands
 * and login events. Builders treat null as a sentinel for "build everything"
 * rather than charging the caller with constructing an infinite bounds.
 *
 * <p><b>Coordinate system:</b> all coordinates are absolute world block
 * coordinates, NOT chunk coordinates. {@code minX} and {@code minZ} are
 * inclusive lower bounds; {@code maxX} and {@code maxZ} are inclusive upper
 * bounds. For a chunk at {@code (chunkX, chunkZ)}:
 * <pre>
 *   ChunkBounds chunk = new ChunkBounds(
 *       chunkX * 16,        chunkZ * 16,
 *       chunkX * 16 + 15,   chunkZ * 16 + 15);
 * </pre>
 *
 * <p>This class is a tiny immutable value type. No allocation optimization
 * needed — at most one instance per chunk-load event.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class ChunkBounds {

    /** Inclusive lower X bound (world block coordinate). */
    public final int minX;
    /** Inclusive lower Z bound (world block coordinate). */
    public final int minZ;
    /** Inclusive upper X bound (world block coordinate). */
    public final int maxX;
    /** Inclusive upper Z bound (world block coordinate). */
    public final int maxZ;

    /**
     * Construct an inclusive block-coordinate bounds.
     *
     * @param minX inclusive lower X
     * @param minZ inclusive lower Z
     * @param maxX inclusive upper X (must be &gt;= minX)
     * @param maxZ inclusive upper Z (must be &gt;= minZ)
     */
    public ChunkBounds(int minX, int minZ, int maxX, int maxZ) {
        if (maxX < minX || maxZ < minZ) {
            throw new IllegalArgumentException(
                    "Invalid ChunkBounds: maxX(" + maxX + ") < minX(" + minX
                            + ") or maxZ(" + maxZ + ") < minZ(" + minZ + ")");
        }
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
    }

    /**
     * Convenience factory: build the ChunkBounds for a chunk coordinate.
     *
     * @param chunkX chunk X (block X / 16)
     * @param chunkZ chunk Z (block Z / 16)
     * @return the 16&times;16 block bounds covering that chunk
     */
    public static ChunkBounds forChunk(int chunkX, int chunkZ) {
        return new ChunkBounds(
                chunkX * 16, chunkZ * 16,
                chunkX * 16 + 15, chunkZ * 16 + 15);
    }

    /**
     * Return true iff the given block coordinate is inside this bounds
     * (inclusive on all sides). Y is intentionally not checked — structures
     * are vertically thin enough that Y filtering adds no value, and some
     * builders place blocks at Y offsets relative to a runtime surface scan.
     *
     * @param x world block X
     * @param z world block Z
     * @return true if (x, z) is inside this rectangle
     */
    public boolean contains(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    /**
     * Return true iff this bounds is fully nested inside {@code other}
     * (or equal to it). Useful for sanity checks.
     */
    public boolean isInside(@Nullable ChunkBounds other) {
        if (other == null) return false;
        return other.minX <= minX && other.maxX >= maxX
                && other.minZ <= minZ && other.maxZ >= maxZ;
    }

    @Override
    public String toString() {
        return "ChunkBounds[x=" + minX + ".." + maxX + ", z=" + minZ + ".." + maxZ + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChunkBounds other)) return false;
        return minX == other.minX && minZ == other.minZ
                && maxX == other.maxX && maxZ == other.maxZ;
    }

    @Override
    public int hashCode() {
        int r = minX;
        r = 31 * r + minZ;
        r = 31 * r + maxX;
        r = 31 * r + maxZ;
        return r;
    }
}
