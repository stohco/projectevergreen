package dev.ergenverse.canon.structure;

/**
 * CanonVolume — immutable AABB in pure integer world coordinates. No Minecraft.
 *
 * <p>CRON-125: the spatial primitive of Layer 1. Buildings have absolute
 * volumes; rooms have volumes relative to their building's min corner; furniture
 * has anchors relative to their room's min corner.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public final class CanonVolume {
    public final int minX, minY, minZ, maxX, maxY, maxZ;

    public CanonVolume(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (maxX < minX || maxY < minY || maxZ < minZ)
            throw new IllegalArgumentException("Invalid CanonVolume: max < min");
        this.minX = minX; this.minY = minY; this.minZ = minZ;
        this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
    }

    public int widthX() { return maxX - minX + 1; }
    public int heightY() { return maxY - minY + 1; }
    public int depthZ() { return maxZ - minZ + 1; }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public boolean intersectsXZ(int oMinX, int oMaxX, int oMinZ, int oMaxZ) {
        return oMaxX >= minX && oMinX <= maxX && oMaxZ >= minZ && oMinZ <= maxZ;
    }

    public boolean intersectsChunk(int chunkX, int chunkZ) {
        int cMinX = chunkX * 16, cMinZ = chunkZ * 16;
        return intersectsXZ(cMinX, cMinX + 15, cMinZ, cMinZ + 15);
    }

    public CanonVolume translate(int dx, int dy, int dz) {
        return new CanonVolume(minX + dx, minY + dy, minZ + dz, maxX + dx, maxY + dy, maxZ + dz);
    }

    @Override public String toString() {
        return "CanonVolume[x=" + minX + ".." + maxX + ",y=" + minY + ".." + maxY + ",z=" + minZ + ".." + maxZ + "]";
    }
}
