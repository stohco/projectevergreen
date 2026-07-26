package dev.ergenverse.canon.structure;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * CanonSettlement — the top of the <b>semantic</b> structure composition tree.
 * Composed of {@link CanonBuilding}s + open-area features (roads, fences,
 * trees) + named anchors.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>The user's vision:
 * <blockquote>
 *   Settlements become compositions. Village → Buildings → Roads → Courtyards →
 *   Gardens → NPC homes → Spirit wells. Each contributes geometry independently.
 * </blockquote>
 *
 * <p>A {@code CanonSettlement} is a pure domain object. It declares what
 * buildings and open features it contains, but does <b>not</b> know how to
 * render any of them. The {@link dev.ergenverse.assembly.WorldAssembler} walks
 * the settlement tree, delegates shell geometry to the
 * {@link dev.ergenverse.assembly.BuildingLibrary}, furniture to the
 * {@link dev.ergenverse.assembly.FurnitureLibrary}, and open features to the
 * {@link dev.ergenverse.assembly.DecorationLibrary}, producing a flat list of
 * {@link dev.ergenverse.assembly.VoxelInstruction}s.
 *
 * <p>CRON-125's {@code materializeInto} + {@code materializeTree} +
 * {@code materializeFence} + {@code materializeRoad} +
 * {@code materializePathLight} (which referenced {@code Blocks.OAK_LOG},
 * {@code ErgenverseBlocks.SPIRIT_WOOD_LEAVES}, etc.) are removed. The canon
 * layer is now Minecraft-free.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public final class CanonSettlement implements CanonObject {

    public record BuildingPlacement(CanonBuilding building, int dx, int dy, int dz) {
        public BuildingPlacement {
            Objects.requireNonNull(building, "building");
        }
    }

    public record OpenFeature(FeatureType type, int dx, int dy, int dz, int span,
                              Orientation orientation) {
        public OpenFeature {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(orientation, "orientation");
            if (span < 1) span = 1;
        }
    }

    public enum FeatureType {
        SPIRIT_TREE, FENCE, ROAD, PATH_LIGHT
    }

    public enum Orientation {
        NORTH, SOUTH, EAST, WEST
    }

    private final String canonId;
    private final String canonEvidenceStr;
    private final String name;
    private final String country;
    private final String era;
    private final List<BuildingPlacement> buildingPlacements;
    private final List<OpenFeature> openFeatures;
    private final List<Anchor> anchors;

    public CanonSettlement(String canonId, String canonEvidenceStr,
                           String name, String country, String era,
                           List<BuildingPlacement> buildingPlacements,
                           List<OpenFeature> openFeatures) {
        this(canonId, canonEvidenceStr, name, country, era,
                buildingPlacements, openFeatures, List.of());
    }

    public CanonSettlement(String canonId, String canonEvidenceStr,
                           String name, String country, String era,
                           List<BuildingPlacement> buildingPlacements,
                           List<OpenFeature> openFeatures,
                           List<Anchor> anchors) {
        this.canonId = Objects.requireNonNull(canonId, "canonId");
        this.canonEvidenceStr = Objects.requireNonNull(canonEvidenceStr, "canonEvidenceStr");
        this.name = Objects.requireNonNull(name, "name");
        this.country = Objects.requireNonNull(country, "country");
        this.era = Objects.requireNonNull(era, "era");
        this.buildingPlacements = buildingPlacements == null
                ? List.of()
                : Collections.unmodifiableList(List.copyOf(buildingPlacements));
        this.openFeatures = openFeatures == null
                ? List.of()
                : Collections.unmodifiableList(List.copyOf(openFeatures));
        this.anchors = anchors == null
                ? List.of()
                : Collections.unmodifiableList(List.copyOf(anchors));
    }

    @Override
    public String canonId() {
        return canonId;
    }

    @Override
    public String canonEvidence() {
        return canonEvidenceStr;
    }

    public String name() {
        return name;
    }

    public String country() {
        return country;
    }

    public String era() {
        return era;
    }

    public List<BuildingPlacement> buildingPlacements() {
        return buildingPlacements;
    }

    public List<OpenFeature> openFeatures() {
        return openFeatures;
    }

    @Override
    public List<Anchor> anchors() {
        return anchors;
    }

    /**
     * Returns the building whose translated relative bounds contain the given
     * local coordinates, or {@code null}. Used by AI / queries to answer
     * "which building is at this position?"
     */
    public CanonBuilding buildingAt(int localX, int localY, int localZ) {
        for (BuildingPlacement bp : buildingPlacements) {
            RelativeBounds rb = bp.building().relativeBounds();
            if (localX >= bp.dx() + rb.minX() && localX <= bp.dx() + rb.maxX()
                    && localY >= bp.dy() + rb.minY() && localY <= bp.dy() + rb.maxY()
                    && localZ >= bp.dz() + rb.minZ() && localZ <= bp.dz() + rb.maxZ()) {
                return bp.building();
            }
        }
        return null;
    }
}
