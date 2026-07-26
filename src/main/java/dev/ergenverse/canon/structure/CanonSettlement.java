package dev.ergenverse.canon.structure;

import dev.ergenverse.block.ErgenverseBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * CanonSettlement — the top of the structure composition tree. Composed of
 * {@link CanonBuilding}s + open-area features (roads, fences, trees).
 *
 * <p><b>CRON-COMPLETIONIST-125 — STRUCTURE COMPOSITION SYSTEM (user roadmap #2)</b>
 *
 * <p>The user's vision:
 * <blockquote>
 *   Settlements become compositions. Instead of {@code VillageBuilder.java}
 *   932 lines, you eventually get Village → Buildings → Roads → Courtyards →
 *   Gardens → NPC homes → Spirit wells. Each contributes geometry independently.
 * </blockquote>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
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

    public CanonSettlement(String canonId, String canonEvidenceStr,
                           String name, String country, String era,
                           List<BuildingPlacement> buildingPlacements,
                           List<OpenFeature> openFeatures) {
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

    @Override
    public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
        for (BuildingPlacement bp : buildingPlacements) {
            bp.building().materializeInto(placer, dx + bp.dx(), dy + bp.dy(), dz + bp.dz());
        }
        for (OpenFeature of : openFeatures) {
            materializeOpenFeature(placer, dx, dy, dz, of);
        }
    }

    private void materializeOpenFeature(VolumePlacer placer, int dx, int dy, int dz,
                                         OpenFeature of) {
        switch (of.type()) {
            case SPIRIT_TREE -> materializeTree(placer, dx + of.dx(), dy + of.dy(), dz + of.dz());
            case FENCE -> materializeFence(placer, dx + of.dx(), dy + of.dy(), dz + of.dz(),
                    of.span(), of.orientation());
            case ROAD -> materializeRoad(placer, dx + of.dx(), dy + of.dy(), dz + of.dz(),
                    of.span(), of.orientation());
            case PATH_LIGHT -> materializePathLight(placer, dx + of.dx(), dy + of.dy(), dz + of.dz());
        }
    }

    private void materializeTree(VolumePlacer placer, int x, int y, int z) {
        if (!placer.contains(x, z)) return;
        var log = Blocks.OAK_LOG.defaultBlockState();
        for (int i = 0; i < 4; i++) {
            placer.placeBlock(new BlockPos(x, y + i, z), log);
        }
        var leaves = ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get().defaultBlockState();
        for (int ddx = -1; ddx <= 1; ddx++) {
            for (int ddz = -1; ddz <= 1; ddz++) {
                placer.placeBlock(new BlockPos(x + ddx, y + 4, z + ddz), leaves);
            }
        }
        placer.placeBlock(new BlockPos(x, y + 5, z), leaves);
    }

    private void materializeFence(VolumePlacer placer, int x, int y, int z,
                                    int span, Orientation orientation) {
        var fence = Blocks.OAK_FENCE.defaultBlockState();
        for (int i = 0; i < span; i++) {
            int px = x, pz = z;
            switch (orientation) {
                case NORTH -> pz = z - i;
                case SOUTH -> pz = z + i;
                case EAST -> px = x + i;
                case WEST -> px = x - i;
            }
            if (!placer.contains(px, pz)) continue;
            placer.placeBlock(new BlockPos(px, y, pz), fence);
        }
    }

    private void materializeRoad(VolumePlacer placer, int x, int y, int z,
                                   int span, Orientation orientation) {
        var sand = ErgenverseBlocks.SPIRIT_SAND.get().defaultBlockState();
        for (int i = 0; i < span; i++) {
            int px = x, pz = z;
            switch (orientation) {
                case NORTH -> pz = z - i;
                case SOUTH -> pz = z + i;
                case EAST -> px = x + i;
                case WEST -> px = x - i;
            }
            if (!placer.contains(px, pz)) continue;
            placer.placeBlock(new BlockPos(px, y, pz), sand);
        }
    }

    private void materializePathLight(VolumePlacer placer, int x, int y, int z) {
        if (!placer.contains(x, z)) return;
        placer.placeBlock(new BlockPos(x, y, z),
                Blocks.OAK_FENCE.defaultBlockState());
        placer.placeBlock(new BlockPos(x, y + 1, z),
                ErgenverseBlocks.SPIRIT_VEIN_STONE.get().defaultBlockState());
    }
}
