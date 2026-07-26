package dev.ergenverse.canon.structure;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * CanonRoom — a semantic room in a building. Composed of
 * {@link CanonFurniture} placements + a {@link RoomFunction} + owner +
 * bounding volume.
 *
 * <p><b>CRON-COMPLETIONIST-125 — STRUCTURE COMPOSITION SYSTEM (user roadmap #2)</b>
 *
 * <p>A {@code CanonRoom} is a semantic marker. It carries:
 * <ul>
 *   <li><b>{@link #function}</b> — the room's purpose (BEDROOM, KITCHEN,
 *       MEDITATION, STORAGE, COURTYARD, WORKSHOP, COMMON_AREA, ALCHEMY_LAB).
 *       Exposed for AI reasoning.</li>
 *   <li><b>{@link #owner}</b> — who owns this room (e.g. "wang_lin").</li>
 *   <li><b>{@link #furniturePlacements}</b> — the (furniture, offset) pairs.</li>
 *   <li><b>{@link #sizeX}, {@link #sizeY}, {@link #sizeZ}</b> — bounding volume.</li>
 * </ul>
 *
 * <p>The room does NOT place its own walls/floor/ceiling — those are the
 * building's responsibility.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class CanonRoom implements CanonObject {

    public enum RoomFunction {
        BEDROOM, KITCHEN, MEDITATION, STORAGE, COURTYARD, WORKSHOP, COMMON_AREA, ALCHEMY_LAB
    }

    public record FurniturePlacement(CanonFurniture furniture, int dx, int dy, int dz) {
        public FurniturePlacement {
            Objects.requireNonNull(furniture, "furniture");
        }
    }

    private final String canonId;
    private final String canonEvidenceStr;
    private final RoomFunction function;
    private final String owner;
    private final String name;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final List<FurniturePlacement> furniturePlacements;

    public CanonRoom(String canonId, String canonEvidenceStr, RoomFunction function,
                     String owner, String name,
                     int sizeX, int sizeY, int sizeZ,
                     List<FurniturePlacement> furniturePlacements) {
        this.canonId = Objects.requireNonNull(canonId, "canonId");
        this.canonEvidenceStr = Objects.requireNonNull(canonEvidenceStr, "canonEvidenceStr");
        this.function = Objects.requireNonNull(function, "function");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.name = Objects.requireNonNull(name, "name");
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            throw new IllegalArgumentException("Room size must be positive");
        }
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.furniturePlacements = furniturePlacements == null
                ? List.of()
                : Collections.unmodifiableList(List.copyOf(furniturePlacements));
    }

    @Override
    public String canonId() {
        return canonId;
    }

    @Override
    public String canonEvidence() {
        return canonEvidenceStr;
    }

    public RoomFunction function() {
        return function;
    }

    public String owner() {
        return owner;
    }

    public String name() {
        return name;
    }

    public List<FurniturePlacement> furniturePlacements() {
        return furniturePlacements;
    }

    @Override
    public RelativeBounds relativeBounds() {
        return new RelativeBounds(0, 0, 0, sizeX - 1, sizeY - 1, sizeZ - 1);
    }

    @Override
    public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
        if (!intersectsChunk(dx, dz, placer.bounds())) return;
        for (FurniturePlacement fp : furniturePlacements) {
            fp.furniture().materializeInto(placer, dx + fp.dx(), dy + fp.dy(), dz + fp.dz());
        }
    }
}
