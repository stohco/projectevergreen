package dev.ergenverse.combat;

/**
 * The orientation of a technique's voxel geometry — the plane or axis the
 * shape is aligned to.
 *
 * <p>Per the design: "if I'm doing a slashing move that has sword effects
 * that goes in a horizontal way, the thing being cut isn't going to be cut
 * vertically."
 *
 * <p>The voxel geometry defines the SHAPE (slice, cylinder, dome, etc.).
 * The orientation defines the PLANE/AXIS that shape is aligned to. A
 * NARROW_SLICE can be horizontal (a flat cutting plane parallel to the
 * ground) or vertical (an upright cutting plane perpendicular to the
 * ground) — these are completely different attacks with different hitboxes.
 *
 * <p>The animation MUST match the orientation. The hitbox MUST match the
 * animation. All three are derived from the same technique definition.
 */
public enum VoxelOrientation {
    /** Flat — parallel to the ground. A horizontal sword slash. */
    HORIZONTAL("Horizontal", "Flat, parallel to the ground"),

    /** Upright — perpendicular to the ground. A vertical sword cleave. */
    VERTICAL("Vertical", "Upright, perpendicular to the ground"),

    /** Diagonal, rising from left to right. */
    DIAGONAL_RISING("Diagonal Rising", "Rising from left to right"),

    /** Diagonal, falling from left to right. */
    DIAGONAL_FALLING("Diagonal Falling", "Falling from left to right"),

    /** Symmetric — orientation doesn't matter (spheres, cubes). */
    OMNI("Omni", "Symmetric — orientation doesn't matter");

    public final String label;
    public final String description;

    VoxelOrientation(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /**
     * Does this orientation matter for the given geometry?
     *
     * <p>Some geometries are symmetric (spheres, cubes) — orientation
     * doesn't change the hitbox. Others (slices, waves, cones) are
     * orientation-dependent.
     */
    public static boolean mattersFor(VoxelFactorization.VoxelGeometry geometry) {
        switch (geometry) {
            case NARROW_SLICE:      // horizontal slice vs vertical slice — completely different
            case TIDAL_WAVE:        // horizontal wave vs vertical wave — different
            case STARFALL_CONE:     // cone direction matters (from above vs from below)
                return true;
            case DESCENDING_CYLINDER: // always vertical
            case EXPANDING_DOME:      // symmetric
            case RADIAL_BURST:        // symmetric
            case LOCKING_CHUNK:       // cube — could orient but usually omni
            case RISING_PILLAR:       // always vertical
            case FROZEN_FIELD:        // always flat
            default:
                return false;
        }
    }
}
