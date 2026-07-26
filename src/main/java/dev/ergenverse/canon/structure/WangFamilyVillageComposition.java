package dev.ergenverse.canon.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * WangFamilyVillageComposition — authors Wang Family Village as a
 * {@link CanonSettlement} composed of buildings → rooms → furniture.
 *
 * <p><b>CRON-COMPLETIONIST-125 — STRUCTURE COMPOSITION SYSTEM (user roadmap #2)</b>
 * <b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>This is the semantic replacement for the legacy 1220-line
 * {@code WangFamilyVillageBuilder}. The legacy builder placed ~80,000 blocks
 * via 79 {@code setBlock} call sites; this composition class expresses the
 * village as a tree of lore objects. CRON-127 made the tree fully Minecraft-free
 * — no {@code materializeInto}, no {@code BlockState}, no {@code BlockPos}. The
 * {@link dev.ergenverse.assembly.WorldAssembler} compiles the tree into a flat
 * list of {@link dev.ergenverse.assembly.VoxelInstruction}s.
 *
 * <h2>Canon fidelity — honest classification</h2>
 *
 * <p>Per Article I ("Canon is reality"), every element is honestly classified:
 *
 * <ul>
 *   <li><b>The settlement "Wang Family Village" (王家村):</b> <b>mod-original.</b>
 *       Canon 仙逆 attests only "赵国某偏僻小山村". The novel does not name the
 *       village "Wang Family Village" — that is a mod invention.</li>
 *   <li><b>Wang Lin's family:</b> <b>canon.</b> Wang Lin's father (Wang Tian,
 *       deceased), mother, younger brother are canon-attested. Wang Tian's
 *       alchemy furnace is canon-attested.</li>
 *   <li><b>The village elder:</b> <b>mod-original.</b> Canon mentions a
 *       village elder implicitly; no specific elder NPC is named.</li>
 *   <li><b>"Old Chen":</b> <b>mod-original.</b> Referenced in the legacy
 *       builder's Wang Lin journal.</li>
 *   <li><b>The spirit vein beneath the village well:</b> <b>canon.</b> Wang
 *       Lin later discovers a spirit vein beneath the village.</li>
 *   <li><b>The 10 commoner homes, 2 storage sheds, 5 farm plots, 12 spirit
 *       trees, perimeter fence, main roads:</b> <b>inferred.</b></li>
 * </ul>
 *
 * <p><b>No fabricated chapter citations.</b>
 *
 * <h2>Migration path</h2>
 *
 * <p>CRON-125 shipped the composition system alongside the legacy builder. CRON-126
 * switched {@link dev.ergenverse.runtime.materialize.StructureBuilderRegistry} to
 * route {@code wang_family_village} through
 * {@link dev.ergenverse.materialization.CanonSettlementBuilder} (the adapter that
 * compiles + materializes a CanonSettlement). CRON-127 rewrote the adapter
 * internals to use the {@link dev.ergenverse.assembly.WorldAssembler} +
 * {@link dev.ergenverse.assembly.VoxelInstruction} IR +
 * {@link dev.ergenverse.materialization.VoxelMaterializer} pipeline, removing
 * the old direct {@code materializeInto(VolumePlacer)} call. The legacy
 * 1220-line {@code WangFamilyVillageBuilder} is RETAINED as reference + fallback
 * but is no longer on the live materialization path. The composition system is
 * the live path for every chunk load in the Wang Family Village footprint.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class WangFamilyVillageComposition {

    private WangFamilyVillageComposition() {}

    public static CanonSettlement create() {
        List<CanonSettlement.BuildingPlacement> buildings = new ArrayList<>();
        List<CanonSettlement.OpenFeature> features = new ArrayList<>();

        // 1. CENTRAL PLAZA — spirit vein well
        CanonRoom plazaRoom = new CanonRoom(
                "central_plaza",
                "inferred — a village center with the spirit well is implied by canon",
                CanonRoom.RoomFunction.COMMON_AREA,
                "village",
                "Central Plaza",
                9, 1, 9,
                List.of(new CanonRoom.FurniturePlacement(
                        CanonFurniture.SPIRIT_WELL, 4, 0, 4))
        );
        CanonBuilding plaza = new CanonBuilding(
                "central_plaza",
                "mod-original — the plaza layout is invented; the spirit well is canon",
                "village",
                "Early Zhao",
                "communal",
                BuildingTheme.COMMON_PLAZA,
                "Central Plaza",
                9, 1, 9,
                List.of(new CanonBuilding.RoomPlacement(plazaRoom, 0, 0, 0))
        );
        buildings.add(new CanonSettlement.BuildingPlacement(plaza, 0, 0, 0));

        // 2. WANG FAMILY HOME
        CanonRoom wangLinBedroom = new CanonRoom(
                "wang_lin_bedroom",
                "inferred — Wang Lin has a sleeping place; the room is inferred from poverty",
                CanonRoom.RoomFunction.BEDROOM,
                "wang_lin",
                "Wang Lin's Bedroom",
                3, 3, 3,
                List.of(
                        new CanonRoom.FurniturePlacement(CanonFurniture.HIDDEN_STORAGE, 0, 0, 0),
                        new CanonRoom.FurniturePlacement(CanonFurniture.MEDITATION_MAT, 1, 1, 0),
                        new CanonRoom.FurniturePlacement(CanonFurniture.LECTERN, 0, 1, 1)
                )
        );
        CanonRoom wangKitchen = new CanonRoom(
                "wang_kitchen",
                "inferred — every mortal home has a kitchen",
                CanonRoom.RoomFunction.KITCHEN,
                "wang_family",
                "Wang Family Kitchen",
                3, 3, 2,
                List.of(
                        new CanonRoom.FurniturePlacement(CanonFurniture.WORK_TABLE, 1, 0, 0),
                        new CanonRoom.FurniturePlacement(CanonFurniture.STORAGE_CHEST, 0, 0, 0)
                )
        );
        CanonRoom wangAlchemyLab = new CanonRoom(
                "wang_alchemy_lab",
                "canon — Wang Tian kept an alchemy furnace; it grew cold after he stopped practicing",
                CanonRoom.RoomFunction.ALCHEMY_LAB,
                "wang_tian",
                "Wang Tian's Alchemy Room",
                3, 3, 2,
                List.of(
                        new CanonRoom.FurniturePlacement(CanonFurniture.ALCHEMY_FURNACE, 1, 0, 0),
                        new CanonRoom.FurniturePlacement(CanonFurniture.BOOKSHELF, 0, 0, 0)
                )
        );
        CanonBuilding wangHome = new CanonBuilding(
                "wang_home",
                "mod-original — the specific layout is invented; the alchemy furnace is canon",
                "wang_family",
                "Early Zhao",
                "residence",
                BuildingTheme.POOR_VILLAGE,
                "Wang Family Home",
                7, 4, 5,
                List.of(
                        new CanonBuilding.RoomPlacement(wangLinBedroom, 4, 1, 1),
                        new CanonBuilding.RoomPlacement(wangKitchen, 0, 1, 0),
                        new CanonBuilding.RoomPlacement(wangAlchemyLab, 0, 1, 2)
                )
        );
        buildings.add(new CanonSettlement.BuildingPlacement(wangHome, -18, 0, -18));

        // 3. ELDER'S HOME
        CanonRoom elderBedroom = new CanonRoom(
                "elder_bedroom",
                "mod-original — the elder's bedroom is invented",
                CanonRoom.RoomFunction.BEDROOM,
                "elder",
                "Elder's Bedroom",
                4, 3, 3,
                List.of(
                        new CanonRoom.FurniturePlacement(CanonFurniture.SLEEPING_MAT, 0, 0, 0),
                        new CanonRoom.FurniturePlacement(CanonFurniture.STORAGE_CHEST, 2, 0, 0)
                )
        );
        CanonRoom elderCourtyard = new CanonRoom(
                "elder_courtyard",
                "mod-original — the elder's courtyard with formation core is invented",
                CanonRoom.RoomFunction.COURTYARD,
                "elder",
                "Elder's Courtyard",
                3, 1, 3,
                List.of(new CanonRoom.FurniturePlacement(CanonFurniture.FORMATION_CORE, 1, 0, 1))
        );
        CanonBuilding elderHome = new CanonBuilding(
                "elder_home",
                "mod-original — the elder's home is invented; canon attests only 'a village elder' implicitly",
                "elder",
                "Early Zhao",
                "residence",
                BuildingTheme.ELDER_HOME,
                "Elder's Home",
                7, 4, 7,
                List.of(
                        new CanonBuilding.RoomPlacement(elderBedroom, 0, 1, 0),
                        new CanonBuilding.RoomPlacement(elderCourtyard, 0, 0, 4)
                )
        );
        buildings.add(new CanonSettlement.BuildingPlacement(elderHome, 10, 0, -18));

        // 4. COMMONER HOMES (10)
        int[][] commonerOffsets = {
                {-18, -4}, { -18, 6}, {  6, -4}, {  6, 6},
                {-10, -28}, { 2, -28}, {16, -28},
                {-10, 12},  { 2, 12},  {16, 12}
        };
        for (int i = 0; i < commonerOffsets.length; i++) {
            CanonRoom bedroom = new CanonRoom(
                    "commoner_" + (i + 1) + "_bedroom",
                    "inferred — a commoner family would have a sleeping place",
                    CanonRoom.RoomFunction.BEDROOM,
                    "commoner_" + (i + 1),
                    "Commoner " + (i + 1) + " Bedroom",
                    3, 3, 2,
                    List.of(new CanonRoom.FurniturePlacement(CanonFurniture.SLEEPING_MAT, 0, 0, 0))
            );
            CanonRoom kitchen = new CanonRoom(
                    "commoner_" + (i + 1) + "_kitchen",
                    "inferred — every home has a kitchen",
                    CanonRoom.RoomFunction.KITCHEN,
                    "commoner_" + (i + 1),
                    "Commoner " + (i + 1) + " Kitchen",
                    3, 3, 2,
                    List.of(new CanonRoom.FurniturePlacement(CanonFurniture.WORK_TABLE, 0, 0, 0))
            );
            CanonBuilding home = new CanonBuilding(
                    "commoner_home_" + (i + 1),
                    "inferred — a poor village has multiple commoner homes",
                    "commoner_" + (i + 1),
                    "Early Zhao",
                    "residence",
                    BuildingTheme.POOR_VILLAGE,
                    "Commoner Home " + (i + 1),
                    5, 4, 5,
                    List.of(
                            new CanonBuilding.RoomPlacement(bedroom, 0, 1, 0),
                            new CanonBuilding.RoomPlacement(kitchen, 0, 1, 2)
                    )
            );
            buildings.add(new CanonSettlement.BuildingPlacement(
                    home, commonerOffsets[i][0], 0, commonerOffsets[i][1]));
        }

        // 5. STORAGE SHEDS (2)
        CanonBuilding shed1 = makeStorageShed("storage_shed_1", "Storage Shed 1", -30, -28);
        CanonBuilding shed2 = makeStorageShed("storage_shed_2", "Storage Shed 2", 24, 18);
        buildings.add(new CanonSettlement.BuildingPlacement(shed1, -30, 0, -28));
        buildings.add(new CanonSettlement.BuildingPlacement(shed2, 24, 0, 18));

        // 6. FARM PLOTS (5)
        int[][] farmOffsets = {
                {-15, -35}, {0, -35}, {15, -35},
                {-10, 22},  {10, 22}
        };
        for (int i = 0; i < farmOffsets.length; i++) {
            List<CanonRoom.FurniturePlacement> cells = new ArrayList<>();
            for (int fx = 0; fx < 5; fx++) {
                for (int fz = 0; fz < 5; fz++) {
                    cells.add(new CanonRoom.FurniturePlacement(
                            CanonFurniture.FARM_PLOT_CELL, fx, 0, fz));
                }
            }
            CanonRoom farmRoom = new CanonRoom(
                    "farm_plot_" + (i + 1),
                    "canon — the village grows spirit herbs unknowingly",
                    CanonRoom.RoomFunction.COMMON_AREA,
                    "village",
                    "Farm Plot " + (i + 1),
                    5, 1, 5,
                    cells
            );
            CanonBuilding farm = new CanonBuilding(
                    "farm_plot_" + (i + 1),
                    "canon — spirit herb farms; layout is mod-original",
                    "village",
                    "Early Zhao",
                    "farming",
                    BuildingTheme.FARM_PLOT,
                    "Farm Plot " + (i + 1),
                    5, 1, 5,
                    List.of(new CanonBuilding.RoomPlacement(farmRoom, 0, 0, 0))
            );
            buildings.add(new CanonSettlement.BuildingPlacement(
                    farm, farmOffsets[i][0], 0, farmOffsets[i][1]));
        }

        // 7. SPIRIT TREES (12 around perimeter)
        int[][] treePositions = {
                {-38, -38}, {38, -38}, {-38, 38}, {38, 38},
                {-38, 0},   {38, 0},   {0, -38},  {0, 38},
                {-20, -38}, {20, -38}, {-20, 38}, {20, 38}
        };
        for (int[] t : treePositions) {
            features.add(new CanonSettlement.OpenFeature(
                    CanonSettlement.FeatureType.SPIRIT_TREE,
                    t[0], 1, t[1], 1, CanonSettlement.Orientation.NORTH));
        }

        // 8. PERIMETER FENCE
        features.add(new CanonSettlement.OpenFeature(
                CanonSettlement.FeatureType.FENCE,
                -40, 1, -40, 40, CanonSettlement.Orientation.EAST));
        features.add(new CanonSettlement.OpenFeature(
                CanonSettlement.FeatureType.FENCE,
                -40, 1, 40, 40, CanonSettlement.Orientation.EAST));
        features.add(new CanonSettlement.OpenFeature(
                CanonSettlement.FeatureType.FENCE,
                -40, 1, -40, 80, CanonSettlement.Orientation.SOUTH));
        features.add(new CanonSettlement.OpenFeature(
                CanonSettlement.FeatureType.FENCE,
                40, 1, -40, 80, CanonSettlement.Orientation.SOUTH));

        // 9. MAIN ROADS
        features.add(new CanonSettlement.OpenFeature(
                CanonSettlement.FeatureType.ROAD,
                0, 0, -40, 80, CanonSettlement.Orientation.SOUTH));
        features.add(new CanonSettlement.OpenFeature(
                CanonSettlement.FeatureType.ROAD,
                -40, 0, 0, 80, CanonSettlement.Orientation.EAST));

        // 10. PATH LIGHTS along roads
        for (int i = -32; i <= 32; i += 16) {
            features.add(new CanonSettlement.OpenFeature(
                    CanonSettlement.FeatureType.PATH_LIGHT,
                    1, 0, i, 1, CanonSettlement.Orientation.NORTH));
            features.add(new CanonSettlement.OpenFeature(
                    CanonSettlement.FeatureType.PATH_LIGHT,
                    -1, 0, i, 1, CanonSettlement.Orientation.NORTH));
            features.add(new CanonSettlement.OpenFeature(
                    CanonSettlement.FeatureType.PATH_LIGHT,
                    i, 0, 1, 1, CanonSettlement.Orientation.NORTH));
            features.add(new CanonSettlement.OpenFeature(
                    CanonSettlement.FeatureType.PATH_LIGHT,
                    i, 0, -1, 1, CanonSettlement.Orientation.NORTH));
        }

        return new CanonSettlement(
                "wang_family_village",
                "mod-original — 'Wang Family Village' is a mod name; canon attests only '赵国某偏僻小山村'",
                "Wang Family Village",
                "Zhao Country",
                "Early Zhao",
                buildings,
                features
        );
    }

    private static CanonBuilding makeStorageShed(String id, String name, int ox, int oz) {
        CanonRoom interior = new CanonRoom(
                id + "_interior",
                "inferred — a farming village has storage sheds",
                CanonRoom.RoomFunction.STORAGE,
                "village",
                name + " Interior",
                3, 3, 3,
                List.of(new CanonRoom.FurniturePlacement(CanonFurniture.STORAGE_CHEST, 0, 0, 0))
        );
        return new CanonBuilding(
                id,
                "inferred — a farming village has storage sheds",
                "village",
                "Early Zhao",
                "storage",
                BuildingTheme.STORAGE_SHED,
                name,
                3, 3, 3,
                List.of(new CanonBuilding.RoomPlacement(interior, 0, 0, 0))
        );
    }
}
