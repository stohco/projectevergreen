package dev.ergenverse.runtime;

/**
 * PackedPos — pack a block position into a single {@code long} for use as a
 * hash-map key.
 *
 * <p>Format (64 bits): bits 0-25 = x (26-bit signed, offset +33554432),
 * bits 26-51 = z (26-bit signed), bits 52-63 = y (12-bit signed, offset +2048).
 *
 * <p>This supports X,Z &isin; [-33,554,432, +33,554,431] and
 * Y &isin; [-2048, +2047]. Minecraft 1.20.1 world border is &plusmn;30M and
 * height is [-64, 320], so this is sufficient with headroom.
 *
 * <p>Extracted from the former {@code BlockDelta} so the packing utility is
 * available to all layer/delta code without dragging along delta storage
 * semantics. ~50,000 changed blocks packed this way occupy ~400KB of map
 * overhead — vastly smaller than storing millions of generated blocks.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class PackedPos {

    private PackedPos() {}

    /** Pack a block position into a long. */
    public static long pack(int x, int y, int z) {
        long ux = (x + 33554432L) & 0x3FFFFFFL;  // 26 bits
        long uz = (z + 33554432L) & 0x3FFFFFFL;  // 26 bits
        long uy = (y + 2048L)     & 0xFFFL;       // 12 bits
        return ux | (uz << 26) | (uy << 52);
    }

    /** Unpack X. */
    public static int unpackX(long packed) {
        return (int) (packed & 0x3FFFFFFL) - 33554432;
    }

    /** Unpack Z. */
    public static int unpackZ(long packed) {
        return (int) ((packed >> 26) & 0x3FFFFFFL) - 33554432;
    }

    /** Unpack Y. */
    public static int unpackY(long packed) {
        return (int) ((packed >> 52) & 0xFFFL) - 2048;
    }
}
