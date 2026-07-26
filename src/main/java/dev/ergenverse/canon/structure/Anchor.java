package dev.ergenverse.canon.structure;

import java.util.Objects;

/**
 * Anchor — a named attachment point with a {@link SemanticRole}, expressed as a
 * relative offset from its owning object's origin.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>The user's vision:
 * <blockquote>
 *   Anchors are incredibly important. {@code Anchor { String id; SemanticRole role; }}.
 *   The compiler assigns world coordinates. AI queries anchors. […] Suppose you
 *   redesign Wang Lin's house. Old house: Bed x=4. New house: Bed x=11. Nothing
 *   breaks. AI still asks BED. Compiler returns 11. Done.
 * </blockquote>
 *
 * <p>An anchor is <b>pure semantic</b>: an id (e.g. {@code "wang_lin_bed"}), a
 * role (e.g. {@link SemanticRole#BED}), and a relative offset
 * {@code (offsetX, offsetY, offsetZ)} from the owning room/building's min
 * corner. The {@link dev.ergenverse.assembly.AnchorRegistry} resolves these to
 * absolute world coordinates during compilation; AI queries the registry by
 * role or id — it never inspects blocks.
 *
 * <p>This supersedes the CRON-125 {@code CanonAnchor} (which had no role). All
 * offsets are plain {@code int}s — no {@code BlockPos}.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public final class Anchor {
    private final String id;
    private final SemanticRole role;
    private final int offsetX;
    private final int offsetY;
    private final int offsetZ;

    public Anchor(String id, SemanticRole role, int offsetX, int offsetY, int offsetZ) {
        this.id = Objects.requireNonNull(id, "id");
        if (id.isBlank()) throw new IllegalArgumentException("Anchor id blank");
        this.role = Objects.requireNonNull(role, "role");
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    /** Convenience: anchor at floor level (y=0). */
    public static Anchor at(String id, SemanticRole role, int x, int z) {
        return new Anchor(id, role, x, 0, z);
    }

    public String id() { return id; }
    public SemanticRole role() { return role; }
    public int offsetX() { return offsetX; }
    public int offsetY() { return offsetY; }
    public int offsetZ() { return offsetZ; }

    /** Returns a new anchor translated by (dx, dy, dz). */
    public Anchor translate(int dx, int dy, int dz) {
        return new Anchor(id, role, offsetX + dx, offsetY + dy, offsetZ + dz);
    }

    @Override
    public String toString() {
        return "Anchor[" + id + "/" + role + " +" + offsetX + ",+" + offsetY + ",+" + offsetZ + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Anchor a)) return false;
        return offsetX == a.offsetX && offsetY == a.offsetY && offsetZ == a.offsetZ
                && id.equals(a.id) && role == a.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role, offsetX, offsetY, offsetZ);
    }
}
