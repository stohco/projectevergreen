package dev.ergenverse.assembly;

import dev.ergenverse.canon.structure.BuildingTheme;
import java.util.ArrayList;
import java.util.List;

/**
 * BuildingLibrary — translates a semantic {@link BuildingTheme} + dimensions
 * into the <b>shell</b> voxel geometry (floor, walls, roof, door).
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>This is one of the user's "four independent libraries":
 * <blockquote>
 *   Building Library: Poor House → Rooms → Shell. […] Even themes shouldn't
 *   know blocks. {@code BuildingTheme { POOR_VILLAGE, ELDER_HOME, … }}. Then
 *   the backend decides: POOR_VILLAGE → oak planks, spruce roof, stone
 *   foundation. Later maybe you redesign every Zhao house — nothing above
 *   changes.
 * </blockquote>
 *
 * <p>The library is the <b>only</b> place that knows a {@code POOR_VILLAGE}
 * house has oak-plank floors, oak-log walls, and an oak-plank roof. Changing
 * the visual style of every poor house means editing one switch arm here — the
 * canon {@link dev.ergenverse.canon.structure.CanonBuilding} data is untouched.
 *
 * <p>The shell is intentionally simple — flat roofs, box walls. Per the legacy
 * builder self-critique: "Mortal villages don't have upturned eaves." Open
 * themes ({@code COMMON_PLAZA}, {@code FARM_PLOT}) emit only a floor.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public final class BuildingLibrary {

    private BuildingLibrary() {}

    /**
     * Returns the shell voxel geometry for a building of the given theme and
     * exterior dimensions, relative to the building origin (0, 0, 0). The door
     * is placed on the south wall (z = sizeZ - 1) at x = sizeX / 2.
     */
    public static List<VoxelInstruction> shell(BuildingTheme theme, int sizeX, int sizeY, int sizeZ) {
        List<VoxelInstruction> out = new ArrayList<>();
        MaterialID floor = floorMaterial(theme);
        MaterialID wall = wallMaterial(theme);
        MaterialID roof = roofMaterial(theme);

        // Floor
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                out.add(VoxelInstruction.at(x, 0, z, floor, VoxelLayer.FLOOR));
            }
        }

        boolean enclosed = theme != BuildingTheme.COMMON_PLAZA && theme != BuildingTheme.FARM_PLOT;
        if (!enclosed) return out;

        // Walls
        int wallTop = sizeY - 2;
        for (int y = 1; y <= wallTop; y++) {
            for (int x = 0; x < sizeX; x++) {
                out.add(VoxelInstruction.at(x, y, 0, wall, VoxelLayer.WALL));
                out.add(VoxelInstruction.at(x, y, sizeZ - 1, wall, VoxelLayer.WALL));
            }
            for (int z = 1; z < sizeZ - 1; z++) {
                out.add(VoxelInstruction.at(0, y, z, wall, VoxelLayer.WALL));
                out.add(VoxelInstruction.at(sizeX - 1, y, z, wall, VoxelLayer.WALL));
            }
        }

        // Roof
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                out.add(VoxelInstruction.at(x, sizeY - 1, z, roof, VoxelLayer.ROOF));
            }
        }

        // Door on south wall (z = sizeZ - 1) at x = sizeX / 2
        int doorX = sizeX / 2;
        int doorZ = sizeZ - 1;
        if (theme == BuildingTheme.STORAGE_SHED) {
            out.add(VoxelInstruction.at(doorX, 1, doorZ, MaterialID.AIR, VoxelLayer.DOOR));
            if (sizeY >= 4) {
                out.add(VoxelInstruction.at(doorX, 2, doorZ, MaterialID.AIR, VoxelLayer.DOOR));
            }
        } else {
            out.add(new VoxelInstruction(doorX, 1, doorZ, MaterialID.OAK_DOOR_LOWER, Rotation.NONE, VoxelLayer.DOOR));
            if (sizeY >= 4) {
                out.add(new VoxelInstruction(doorX, 2, doorZ, MaterialID.OAK_DOOR_UPPER, Rotation.NONE, VoxelLayer.DOOR));
            }
        }
        return out;
    }

    private static MaterialID floorMaterial(BuildingTheme theme) {
        return switch (theme) {
            case POOR_VILLAGE, ELDER_HOME -> MaterialID.OAK_PLANKS;
            case STORAGE_SHED -> MaterialID.COBBLESTONE;
            case COMMON_PLAZA -> MaterialID.POLISHED_ANDESITE;
            case FARM_PLOT -> MaterialID.FARMLAND;
            default -> MaterialID.OAK_PLANKS;
        };
    }

    private static MaterialID wallMaterial(BuildingTheme theme) {
        return switch (theme) {
            case POOR_VILLAGE -> MaterialID.OAK_LOG;
            case ELDER_HOME -> MaterialID.SPRUCE_PLANKS;
            case STORAGE_SHED -> MaterialID.OAK_LOG;
            case COMMON_PLAZA, FARM_PLOT -> MaterialID.AIR;
            default -> MaterialID.OAK_LOG;
        };
    }

    private static MaterialID roofMaterial(BuildingTheme theme) {
        return switch (theme) {
            case POOR_VILLAGE, STORAGE_SHED -> MaterialID.OAK_PLANKS;
            case ELDER_HOME -> MaterialID.SPRUCE_PLANKS;
            case COMMON_PLAZA, FARM_PLOT -> MaterialID.AIR;
            default -> MaterialID.OAK_PLANKS;
        };
    }
}
