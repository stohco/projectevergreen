package dev.ergenverse.manipulation;

import dev.ergenverse.combat.VoxelFactorization.VoxelGeometry;

import java.util.Map;

/**
 * The selection shape system for Heaven and Earth Manipulation.
 *
 * <p>This is the engineered solution for "how does the player adjust the
 * shape of what they're lifting/moving?" Per the design document, we use
 * a hybrid of two approaches:
 *
 * <h2>Suggestion A (primary): Technique-Determined Shape — the "Dao Hand"</h2>
 *
 * <p>The default shape of your selection is determined by your technique's
 * voxel geometry. Your Dao forms a virtual "hand" whose shape matches your
 * path:
 *
 * <ul>
 *   <li><b>Sword Dao</b> → NARROW_SLICE — you cut a thin plane of blocks</li>
 *   <li><b>Earth Dao</b> → DESCENDING_CYLINDER — you grip a column of earth</li>
 *   <li><b>Fire Dao</b> → RADIAL_BURST — you grab a spherical volume</li>
 *   <li><b>Seal Dao</b> → LOCKING_CHUNK — you freeze a cube of space</li>
 *   <li><b>Wood Dao</b> → RISING_PILLAR — you lift a growing column</li>
 *   <li><b>Water Dao</b> → TIDAL_WAVE — you sweep a horizontal layer</li>
 *   <li><b>Divine Sense Dao</b> → EXPANDING_DOME — you grip a growing sphere</li>
 * </ul>
 *
 * <p>Scroll wheel adjusts SCALE (how big the hand is).
 *
 * <h2>Suggestion D (refinement): Freeform Sculpting — the "Divine Sense Hand"</h2>
 *
 * <p>Hold Shift while in manipulation mode to enter "refine" mode. A
 * ghostly divine sense hand appears at the crosshair. Left-click adds
 * blocks to the selection; right-click removes them. This lets the player
 * carve out exactly what they want — "I want THIS part of the mountain,
 * but not THAT part."
 *
 * <h2>Why this hybrid</h2>
 *
 * <ol>
 *   <li>By default, your Dao shapes your interaction (thematic, fast)</li>
 *   <li>When you need precision, you switch to refine mode (flexible, immersive)</li>
 *   <li>The scroll wheel always controls scale (one universal control)</li>
 *   <li>No menu diving — everything is done in-world with the crosshair</li>
 * </ol>
 */
public final class SelectionShape {

    private SelectionShape() {}

    /**
     * The Dao → geometry mapping.
     *
     * <p>Your Dao literally shapes how you interact with the world. A sword
     * cultivator and an earth cultivator grab mountains differently —
     * because their Daos are different.
     */
    public static final Map<String, VoxelGeometry> DAO_GEOMETRY = Map.ofEntries(
        Map.entry("sword", VoxelGeometry.NARROW_SLICE),
        Map.entry("saber", VoxelGeometry.NARROW_SLICE),
        Map.entry("earth", VoxelGeometry.DESCENDING_CYLINDER),
        Map.entry("body", VoxelGeometry.DESCENDING_CYLINDER),
        Map.entry("fire", VoxelGeometry.RADIAL_BURST),
        Map.entry("lightning", VoxelGeometry.RADIAL_BURST),
        Map.entry("seal", VoxelGeometry.LOCKING_CHUNK),
        Map.entry("space", VoxelGeometry.LOCKING_CHUNK),
        Map.entry("wood", VoxelGeometry.RISING_PILLAR),
        Map.entry("life", VoxelGeometry.RISING_PILLAR),
        Map.entry("water", VoxelGeometry.TIDAL_WAVE),
        Map.entry("ice", VoxelGeometry.FROZEN_FIELD),
        Map.entry("divine_sense", VoxelGeometry.EXPANDING_DOME),
        Map.entry("concealment", VoxelGeometry.EXPANDING_DOME),
        Map.entry("light", VoxelGeometry.STARFALL_CONE),
        Map.entry("wind", VoxelGeometry.STARFALL_CONE)
    );

    /**
     * Get the default voxel geometry for a Dao.
     *
     * <p>If the player has multiple Daos, the highest-comprehension Dao
     * determines the default shape.
     */
    public static VoxelGeometry geometryForDao(String dao) {
        return DAO_GEOMETRY.getOrDefault(dao, VoxelGeometry.EXPANDING_DOME);
    }

    /**
     * Get the default geometry for the player based on their Dao affinities.
     *
     * @param daoAffinities the player's Dao affinities (dao id → comprehension 0-1)
     * @return the geometry of the player's highest-comprehension Dao
     */
    public static VoxelGeometry defaultGeometryForPlayer(Map<String, Double> daoAffinities) {
        String bestDao = null;
        double bestComp = 0;
        for (Map.Entry<String, Double> e : daoAffinities.entrySet()) {
            if (e.getValue() > bestComp) {
                bestComp = e.getValue();
                bestDao = e.getKey();
            }
        }
        return bestDao != null ? geometryForDao(bestDao) : VoxelGeometry.EXPANDING_DOME;
    }

    // ─── Selection state (per-player, during manipulation) ───────────

    /**
     * The current selection state for a player actively using a manipulation
     * technique.
     *
     * <p>This tracks: the active geometry, the scale (scroll-adjusted),
     * whether refine mode is active, and the set of manually
     * added/removed blocks (for freeform sculpting).
     */
    public static final class State {
        /** The active voxel geometry (defaults to the player's Dao shape). */
        public VoxelGeometry geometry;

        /** The scale of the selection (scroll-adjusted). 1 = 1 block radius. */
        public int scale;

        /** Is refine mode active? (Shift held — freeform sculpting) */
        public boolean refineMode;

        /** Blocks manually added to the selection in refine mode. */
        public java.util.Set<long[]> addedBlocks = new java.util.HashSet<>();

        /** Blocks manually removed from the selection in refine mode. */
        public java.util.Set<long[]> removedBlocks = new java.util.HashSet<>();

        /** The rotation of the selection (for non-symmetric shapes). 0-3. */
        public int rotation;

        public State(VoxelGeometry geometry, int scale) {
            this.geometry = geometry;
            this.scale = scale;
            this.refineMode = false;
            this.rotation = 0;
        }

        /** Scroll up — increase scale. */
        public void scrollUp() {
            scale = Math.min(64, scale + 1);
        }

        /** Scroll down — decrease scale. */
        public void scrollDown() {
            scale = Math.max(1, scale - 1);
        }

        /** Enter refine mode (Shift held). */
        public void enterRefineMode() {
            refineMode = true;
        }

        /** Exit refine mode (Shift released). */
        public void exitRefineMode() {
            refineMode = false;
        }

        /** Add a block to the selection (left-click in refine mode). */
        public void addBlock(long x, long y, long z) {
            addedBlocks.add(new long[]{x, y, z});
            removedBlocks.removeIf(b -> b[0] == x && b[1] == y && b[2] == z);
        }

        /** Remove a block from the selection (right-click in refine mode). */
        public void removeBlock(long x, long y, long z) {
            removedBlocks.add(new long[]{x, y, z});
            addedBlocks.removeIf(b -> b[0] == x && b[1] == y && b[2] == z);
        }

        /** Rotate the selection (R key). */
        public void rotate() {
            rotation = (rotation + 1) % 4;
        }

        /** Reset the selection (cancel). */
        public void reset() {
            addedBlocks.clear();
            removedBlocks.clear();
            refineMode = false;
            rotation = 0;
        }

        /**
         * Get the total block count in the selection.
         *
         * <p>For the default geometry, this is the geometry's footprint at
         * the current scale. For refine mode, it's the manual additions
         * minus the manual removals.
         */
        public int totalBlocks() {
            if (refineMode && !addedBlocks.isEmpty()) {
                return addedBlocks.size();
            }
            return geometryFootprint(geometry, scale);
        }
    }

    /**
     * Compute the footprint (block count) of a geometry at a given scale.
     */
    public static int geometryFootprint(VoxelGeometry geometry, int scale) {
        int s = Math.max(1, scale);
        switch (geometry) {
            case NARROW_SLICE:          // thin plane
                return s * s;
            case DESCENDING_CYLINDER:   // vertical column
                return (int) (Math.PI * s * s * (s * 2));
            case EXPANDING_DOME:        // sphere
                return (int) ((4.0 / 3.0) * Math.PI * s * s * s);
            case RADIAL_BURST:          // sphere (slightly larger)
                return (int) ((4.0 / 3.0) * Math.PI * s * s * s * 1.2);
            case LOCKING_CHUNK:         // cube
                return (s * 2) * (s * 2) * (s * 2);
            case RISING_PILLAR:         // vertical column (taller)
                return (int) (Math.PI * s * s * (s * 4));
            case TIDAL_WAVE:            // horizontal layer
                return (s * 2) * (s * 2) * s;
            case STARFALL_CONE:         // cone from above
                return (int) ((1.0 / 3.0) * Math.PI * s * s * (s * 3));
            case FROZEN_FIELD:          // flat frozen area
                return (s * 2) * (s * 2);
            default:
                return s * s * s;
        }
    }
}
