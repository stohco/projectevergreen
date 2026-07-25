package dev.ergenverse.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * BlockDelta — a mutable map of block positions to block states.
 *
 * <p>This is the base class for both {@link SimulationDelta} and
 * {@link PlayerDelta}. Each tracks which blocks have been changed from
 * the canon blueprint, and what their new state is.
 *
 * <p><b>Key insight:</b> A block can be "changed to air" (destroyed) —
 * this is different from "not in the delta" (unchanged). The delta
 * stores the NEW state, which may be air.
 *
 * <p>The delta is keyed by a packed long position (x | (z << 24) | (y << 48))
 * for memory efficiency. With 50,000 changed blocks, this is ~400KB —
 * vastly smaller than storing millions of generated blocks.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public abstract class BlockDelta {

    /** The owner type of this delta (SIMULATION or PLAYER). */
    private final BlockOwner owner;

    /** Map of packed position → block state string (e.g. "minecraft:air", "ergenverse:spirit_stone"). */
    private final Map<Long, String> changes = new HashMap<>();

    protected BlockDelta(BlockOwner owner) {
        if (owner == BlockOwner.CANON) {
            throw new IllegalArgumentException("BlockDelta cannot be CANON — canon is immutable");
        }
        this.owner = owner;
    }

    /**
     * Record a block change at the given position.
     *
     * @param x block X coordinate
     * @param y block Y coordinate
     * @param z block Z coordinate
     * @param blockId the new block state (e.g. "minecraft:air", "ergenverse:spirit_stone")
     */
    public void set(int x, int y, int z, String blockId) {
        changes.put(pack(x, y, z), blockId);
    }

    /**
     * Get the block state at the given position, or null if no change recorded.
     *
     * @return the block state string, or null if this position is unchanged
     */
    public String get(int x, int y, int z) {
        return changes.get(pack(x, y, z));
    }

    /**
     * Check if a change has been recorded at the given position.
     */
    public boolean exists(int x, int y, int z) {
        return changes.containsKey(pack(x, y, z));
    }

    /**
     * Remove a recorded change (revert to canon). Used when the player
     * un-does a change (e.g. replaces a mined block with the original).
     */
    public void remove(int x, int y, int z) {
        changes.remove(pack(x, y, z));
    }

    /** The number of changed blocks in this delta. */
    public int size() {
        return changes.size();
    }

    /** All changes as an immutable map (for serialization). */
    public Map<Long, String> allChanges() {
        return java.util.Collections.unmodifiableMap(changes);
    }

    /** The owner type of this delta. */
    public BlockOwner owner() {
        return owner;
    }

    /** Clear all changes (used when starting a new save). */
    public void clear() {
        changes.clear();
    }

    // ── Position packing ──
    // Minecraft world: X ∈ [-30M, 30M], Y ∈ [-64, 320], Z ∈ [-30M, 30M]
    // We pack into a long: x (26 bits signed) | z (26 bits signed) | y (12 bits signed)
    // This supports X,Z ∈ [-33M, 33M] and Y ∈ [-2048, 2047] — sufficient.

    /**
     * Pack a block position into a long for use as a map key.
     * Format: bits 0-25 = x (26-bit signed), bits 26-51 = z (26-bit signed),
     * bits 52-63 = y (12-bit signed).
     */
    public static long pack(int x, int y, int z) {
        // Offset to unsigned: x+33554432, z+33554432, y+2048
        long ux = (x + 33554432L) & 0x3FFFFFFL;  // 26 bits
        long uz = (z + 33554432L) & 0x3FFFFFFL;  // 26 bits
        long uy = (y + 2048L) & 0xFFFL;           // 12 bits
        return ux | (uz << 26) | (uy << 52);
    }

    /** Unpack the X coordinate from a packed position. */
    public static int unpackX(long packed) {
        return (int) (packed & 0x3FFFFFFL) - 33554432;
    }

    /** Unpack the Z coordinate from a packed position. */
    public static int unpackZ(long packed) {
        return (int) ((packed >> 26) & 0x3FFFFFFL) - 33554432;
    }

    /** Unpack the Y coordinate from a packed position. */
    public static int unpackY(long packed) {
        return (int) ((packed >> 52) & 0xFFFL) - 2048;
    }
}
