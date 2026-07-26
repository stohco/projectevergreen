package dev.ergenverse.assembly;

import dev.ergenverse.canon.structure.Anchor;
import dev.ergenverse.canon.structure.CanonBuilding;
import dev.ergenverse.canon.structure.CanonFurniture;
import dev.ergenverse.canon.structure.CanonRoom;
import dev.ergenverse.canon.structure.CanonSettlement;
import dev.ergenverse.canon.structure.CanonSettlement.BuildingPlacement;
import dev.ergenverse.canon.structure.CanonSettlement.OpenFeature;
import dev.ergenverse.canon.structure.CanonBuilding.RoomPlacement;
import dev.ergenverse.canon.structure.CanonRoom.FurniturePlacement;
import java.util.ArrayList;
import java.util.List;

/**
 * WorldAssembler — the <b>compiler</b> that turns a semantic
 * {@link CanonSettlement} tree into a flat {@link AssemblyResult} (a list of
 * {@link VoxelInstruction}s + a populated {@link AnchorRegistry}).
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>The user's "compiler analogy":
 * <blockquote>
 *   Er Gen Canon → Semantic AST (Settlement, Building, Room, Furniture) →
 *   Template Library (meaning → geometry) → Blueprint Compiler (geometry →
 *   voxels) → Minecraft Backend (voxels → BlockState). […] The semantic world
 *   never "renders." It is compiled. […] I'd probably call it WorldAssembler
 *   because it's assembling a world from semantic pieces.
 * </blockquote>
 *
 * <p>The assembler walks the semantic tree depth-first. For each object it:
 * <ol>
 *   <li>Asks the appropriate <b>library</b> for that object's relative voxel
 *       geometry — {@link BuildingLibrary} for shells, {@link FurnitureLibrary}
 *       for furniture, {@link DecorationLibrary} for open features.</li>
 *   <li>Translates every emitted voxel by the object's accumulated origin and
 *       appends it to the result list.</li>
 *   <li>Registers the object's {@link Anchor}s at their accumulated world
 *       coordinates in the {@link AnchorRegistry}.</li>
 * </ol>
 *
 * <p>The output is a flat, dumb list of (x, y, z, material, rotation, layer)
 * tuples — the intermediate representation. No Minecraft types appear anywhere
 * in this class.
 *
 * <h2>The pipeline this class realises</h2>
 * <pre>
 *   CanonSettlement ─► WorldAssembler.assemble()
 *        │                   │
 *        │                   ├─► BuildingLibrary.shell(theme, size)
 *        │                   ├─► FurnitureLibrary.voxels(furniture)
 *        │                   ├─► DecorationLibrary.voxels(feature)
 *        │                   └─► AnchorRegistry.register(...)
 *        ▼
 *   AssemblyResult { List&lt;VoxelInstruction&gt;, AnchorRegistry }
 *        │
 *        ▼
 *   VoxelMaterializer ─► ServerLevel.setBlock (MaterialResolver: MaterialID→BlockState)
 * </pre>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public final class WorldAssembler {

    private WorldAssembler() {}

    /**
     * Compiles a settlement into an {@link AssemblyResult} rooted at the given
     * world origin.
     *
     * @param settlement the semantic settlement tree
     * @param originX    world X of the settlement's (0, 0, 0)
     * @param originY    world Y of the settlement's (0, 0, 0)
     * @param originZ    world Z of the settlement's (0, 0, 0)
     */
    public static AssemblyResult assemble(CanonSettlement settlement,
                                          int originX, int originY, int originZ) {
        List<VoxelInstruction> voxels = new ArrayList<>();
        AnchorRegistry anchors = new AnchorRegistry();
        String rootId = settlement.canonId();

        // Settlement-level anchors
        registerAnchors(anchors, rootId, settlement.anchors(), originX, originY, originZ);

        // Buildings
        for (BuildingPlacement bp : settlement.buildingPlacements()) {
            int bx = originX + bp.dx();
            int by = originY + bp.dy();
            int bz = originZ + bp.dz();
            CanonBuilding building = bp.building();
            String buildingPath = rootId + "." + building.canonId();

            // Shell geometry
            for (VoxelInstruction v : BuildingLibrary.shell(
                    building.theme(), building.sizeX(), building.sizeY(), building.sizeZ())) {
                voxels.add(v.translate(bx, by, bz));
            }

            // Building-level anchors (e.g. entrance)
            registerAnchors(anchors, buildingPath, building.anchors(), bx, by, bz);

            // Rooms
            for (RoomPlacement rp : building.roomPlacements()) {
                int rx = bx + rp.dx();
                int ry = by + rp.dy();
                int rz = bz + rp.dz();
                CanonRoom room = rp.room();
                String roomPath = buildingPath + "." + room.canonId();

                // Room-level anchors
                registerAnchors(anchors, roomPath, room.anchors(), rx, ry, rz);

                // Furniture
                int furnIndex = 0;
                for (FurniturePlacement fp : room.furniturePlacements()) {
                    int fx = rx + fp.dx();
                    int fy = ry + fp.dy();
                    int fz = rz + fp.dz();
                    CanonFurniture furn = fp.furniture();

                    // Furniture geometry
                    for (VoxelInstruction v : FurnitureLibrary.voxels(furn)) {
                        voxels.add(v.translate(fx, fy, fz));
                    }

                    // Furniture anchors — disambiguate by index in case a room
                    // has two pieces of the same kind.
                    String furnPath = roomPath + "." + furn.canonId() + "#" + furnIndex;
                    registerAnchors(anchors, furnPath, furn.anchors(), fx, fy, fz);
                    furnIndex++;
                }
            }
        }

        // Open features (trees, fences, roads, path lights)
        for (OpenFeature of : settlement.openFeatures()) {
            int fx = originX + of.dx();
            int fy = originY + of.dy();
            int fz = originZ + of.dz();
            for (VoxelInstruction v : DecorationLibrary.voxels(of.type(), of.span(), of.orientation())) {
                voxels.add(v.translate(fx, fy, fz));
            }
        }

        return new AssemblyResult(voxels, anchors);
    }

    private static void registerAnchors(AnchorRegistry registry, String pathPrefix,
                                        List<Anchor> anchors, int ox, int oy, int oz) {
        for (Anchor a : anchors) {
            registry.register(pathPrefix + "." + a.id(), a.role(),
                    ox + a.offsetX(), oy + a.offsetY(), oz + a.offsetZ());
        }
    }
}
