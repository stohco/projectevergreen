package dev.ergenverse.canon.structure;

import dev.ergenverse.runtime.ChunkBounds;

import javax.annotation.Nullable;

/**
 * CanonObject — the root interface for all semantic canon world objects.
 *
 * <p><b>CRON-COMPLETIONIST-125 — STRUCTURE COMPOSITION SYSTEM (user roadmap #2)</b>
 *
 * <p>Every canon world object implements this interface. The defining property
 * is: <b>a CanonObject knows how to materialize itself into a
 * {@link VolumePlacer}</b>. The object is lore (owner, era, purpose, semantic
 * function); the placer is the bridge to Minecraft blocks.
 *
 * <h2>The composition pattern</h2>
 *
 * <p>A {@link CanonSettlement} is a composition of {@link CanonBuilding}s.
 * A {@link CanonBuilding} is a composition of {@link CanonRoom}s. A
 * {@link CanonRoom} is a composition of {@link CanonFurniture}s. Each level
 * delegates {@link #materializeInto} to its children, with an offset
 * translation. The result is a tree of semantic objects whose leaves emit
 * block placements.
 *
 * <h2>Why this is better than a monolithic builder</h2>
 *
 * <p>The legacy {@code WangFamilyVillageBuilder} is 1220 lines with 79 setBlock
 * call sites in a deep call tree. The world knows "stone brick at
 * (3844,72,-1182)" — it does not know "this is Wang Lin's bedroom." The
 * composition system fixes this:
 *
 * <ul>
 *   <li><b>AI reasonability.</b> A future cognition engine can query
 *       {@code village.findRoomAt(pos)} and get back a {@link CanonRoom} with
 *       function=BEDROOM, owner=wang_lin.</li>
 *   <li><b>Canon-error resilience.</b> If we discover Wang Lin's room actually
 *       faced east, we change one offset on one {@code CanonRoom} — not 50
 *       block placements in a 1220-line file.</li>
 *   <li><b>Chunk-scoping for free.</b> Each object's {@link #materializeInto}
 *       queries the placer's bounds and short-circuits if its volume does not
 *       intersect.</li>
 *   <li><b>Composability.</b> A {@link CanonSettlement} can contain another
 *       settlement.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public interface CanonObject {

    String canonId();

    String canonEvidence();

    void materializeInto(VolumePlacer placer, int dx, int dy, int dz);

    default void materializeInto(VolumePlacer placer) {
        materializeInto(placer, 0, 0, 0);
    }

    @Nullable
    default RelativeBounds relativeBounds() {
        return null;
    }

    default boolean intersectsChunk(int originX, int originZ, @Nullable ChunkBounds bounds) {
        if (bounds == null) return true;
        RelativeBounds rb = relativeBounds();
        if (rb == null) return true;
        return bounds.contains(originX + rb.minX(), originZ + rb.minZ())
                || bounds.contains(originX + rb.maxX(), originZ + rb.minZ())
                || bounds.contains(originX + rb.minX(), originZ + rb.maxZ())
                || bounds.contains(originX + rb.maxX(), originZ + rb.maxZ())
                || (bounds.minX >= originX + rb.minX() && bounds.minX <= originX + rb.maxX()
                    && bounds.minZ >= originZ + rb.minZ() && bounds.minZ <= originZ + rb.maxZ());
    }

    record RelativeBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public RelativeBounds {
            if (maxX < minX || maxY < minY || maxZ < minZ) {
                throw new IllegalArgumentException("Invalid RelativeBounds");
            }
        }
    }
}
