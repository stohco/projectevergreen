package dev.ergenverse.runtime.spatial;

import java.util.List;

/**
 * SpatialIndex — a spatial query interface for the PlanetSuzakuBlueprint.
 *
 * <p><b>Architectural directive (2026-07-25):</b> "Right now you have
 * Blueprint → Runtime. I'd insert Blueprint → Spatial Index → Runtime.
 * PlanetSuzakuBlueprint contains 50,000 objects. You don't want
 * for(allObjects) every chunk load. Instead R-tree, BVH, Quadtree, KD-tree —
 * any spatial index. Then loading chunk (3842,-1184) becomes
 * blueprint.query(chunkBounds) instead of for(everything). Huge scalability
 * improvement."
 *
 * <p>The spatial index allows O(log n) queries like "what objects intersect
 * this chunk?" instead of O(n) linear scans. This is critical for chunk
 * materialization — every chunk load queries the blueprint.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 *
 * @param <T> the type of spatial object stored in the index
 */
public interface SpatialIndex<T> {

    /**
     * Insert a spatial object with its bounding box.
     *
     * @param obj the object to store
     * @param minX minimum X coordinate (block coords)
     * @param minZ minimum Z coordinate (block coords)
     * @param maxX maximum X coordinate (block coords)
     * @param maxZ maximum Z coordinate (block coords)
     */
    void insert(T obj, int minX, int minZ, int maxX, int maxZ);

    /**
     * Query all objects whose bounding box intersects the given rectangle.
     *
     * <p>This is the primary operation used by the ChunkMaterializer:
     * <pre>
     *   List<Structure> structs = index.query(chunkMinX, chunkMinZ, chunkMaxX, chunkMaxZ);
     * </pre>
     *
     * @param minX query rectangle minimum X
     * @param minZ query rectangle minimum Z
     * @param maxX query rectangle maximum X
     * @param maxZ query rectangle maximum Z
     * @return list of objects intersecting the query rectangle (may be empty)
     */
    List<T> query(int minX, int minZ, int maxX, int maxZ);

    /**
     * Remove an object from the index.
     *
     * @param obj the object to remove
     * @return true if the object was found and removed
     */
    boolean remove(T obj);

    /**
     * The number of objects in the index.
     */
    int size();

    /**
     * Remove all objects from the index.
     */
    void clear();
}
