package dev.ergenverse.canon.structure;

import java.util.List;

/**
 * CanonObject — the root interface for all <b>semantic</b> canon world objects.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>Per the user's constitutional rule: <i>"Minecraft classes (BlockState,
 * BlockPos, Blocks, ServerLevel, Entity, etc.) may not appear in the Canon or
 * Semantic World layers. They are backend implementation details and belong
 * exclusively to the world assembly/materialization backend."</i>
 *
 * <p>Consequently, a {@code CanonObject} <b>knows nothing about Minecraft</b>.
 * It does not know how to place blocks, does not reference {@code BlockState},
 * does not even know strings like {@code "minecraft:oak_planks"}. It is a pure
 * domain object — lore + semantic role + relative volume + named anchors. The
 * {@link dev.ergenverse.assembly.WorldAssembler} compiles a tree of
 * {@code CanonObject}s into a flat list of
 * {@link dev.ergenverse.assembly.VoxelInstruction}s; the
 * {@link dev.ergenverse.materialization.VoxelMaterializer} turns those into
 * Minecraft blocks.
 *
 * <h2>The compilation pipeline</h2>
 * <pre>
 *   CanonSettlement  ─┐
 *   CanonBuilding     ├──► WorldAssembler ──► List&lt;VoxelInstruction&gt; ──► VoxelMaterializer ──► ServerLevel
 *   CanonRoom         ─┘        (populates AnchorRegistry)                                  (MaterialID→BlockState)
 *   CanonFurniture
 * </pre>
 *
 * <h2>What a CanonObject provides</h2>
 * <ul>
 *   <li>{@link #canonId()} — stable identifier (e.g. {@code "wang_lin_bedroom"}).</li>
 *   <li>{@link #canonEvidence()} — honest provenance: canon / inferred / mod-original.</li>
 *   <li>{@link #relativeBounds()} — the object's volume relative to its parent's
 *       origin (pure {@code int}s). Used by the assembler for chunk-culling.</li>
 *   <li>{@link #anchors()} — named attachment points ({@link Anchor}) that the
 *       assembler resolves to world coordinates for AI navigation.</li>
 * </ul>
 *
 * <p>CRON-125 originally gave {@code CanonObject} a {@code materializeInto}
 * method that emitted {@code BlockState}s into a {@code VolumePlacer}. CRON-127
 * <b>removes</b> that — materialization is now the assembler's job, and the
 * canon layer is finally Minecraft-free.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public interface CanonObject {

    /** Stable semantic identifier (e.g. {@code "wang_lin_bedroom"}). */
    String canonId();

    /** Honest provenance string: canon / inferred / mod-original. */
    String canonEvidence();

    /**
     * The object's bounding volume relative to its parent's origin, or
     * {@code null} if the object has no meaningful volume. Pure {@code int}s.
     */
    default RelativeBounds relativeBounds() {
        return null;
    }

    /**
     * Named attachment points ({@link Anchor}) relative to this object's
     * origin. The assembler resolves these to absolute world coordinates and
     * registers them for AI navigation. Defaults to none.
     */
    default List<Anchor> anchors() {
        return List.of();
    }

    /**
     * Immutable integer AABB relative to a parent's origin.
     *
     * @param minX min X (inclusive)
     * @param minY min Y (inclusive)
     * @param minZ min Z (inclusive)
     * @param maxX max X (inclusive)
     * @param maxY max Y (inclusive)
     * @param maxZ max Z (inclusive)
     */
    record RelativeBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public RelativeBounds {
            if (maxX < minX || maxY < minY || maxZ < minZ) {
                throw new IllegalArgumentException("Invalid RelativeBounds: max < min");
            }
        }

        public int sizeX() { return maxX - minX + 1; }
        public int sizeY() { return maxY - minY + 1; }
        public int sizeZ() { return maxZ - minZ + 1; }
    }
}
