package dev.ergenverse.canon.structure;

/**
 * CanonAnchor — a named attachment point relative to a room's min corner.
 *
 * <p>CRON-125: per the user's vision, "Furniture then binds itself to anchors.
 * This lets you resize rooms without rewriting furniture placement."
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public final class CanonAnchor {
    public final String name;
    public final int offsetX, offsetY, offsetZ;

    public CanonAnchor(String name, int offsetX, int offsetY, int offsetZ) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("CanonAnchor name blank");
        this.name = name;
        this.offsetX = offsetX; this.offsetY = offsetY; this.offsetZ = offsetZ;
    }

    public static CanonAnchor at(String name, int x, int z) { return new CanonAnchor(name, x, 0, z); }

    @Override public String toString() { return "CanonAnchor[" + name + " +" + offsetX + ",+" + offsetY + ",+" + offsetZ + "]"; }
}
