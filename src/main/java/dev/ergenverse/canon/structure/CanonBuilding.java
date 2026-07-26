package dev.ergenverse.canon.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * CanonBuilding — a building composed of {@link CanonRoom}s + a building shell
 * (walls, floor, roof, door).
 *
 * <p><b>CRON-COMPLETIONIST-125 — STRUCTURE COMPOSITION SYSTEM (user roadmap #2)</b>
 *
 * <p>A {@code CanonBuilding} carries:
 * <ul>
 *   <li><b>{@link #owner}</b>, <b>{@link #era}</b>, <b>{@link #purpose}</b> — lore.</li>
 *   <li><b>{@link #shellType}</b> — construction style (POOR_HOUSE, ELDER_HOUSE,
 *       STORAGE_SHED, COMMON_PLAZA, FARM_PLOT). Determines the block palette.</li>
 *   <li><b>{@link #sizeX}, {@link #sizeY}, {@link #sizeZ}</b> — exterior dimensions.</li>
 *   <li><b>{@link #roomPlacements}</b> — the rooms inside.</li>
 * </ul>
 *
 * <p>The shell is intentionally simple — flat roofs, box walls. Per the legacy
 * builder self-critique: "Mortal villages don't have upturned eaves."
 *
 * <h2>Canon fidelity</h2>
 *
 * <p>Wang Family Village itself is mod-original. Canon attests only "赵国某偏僻
 * 小山村". The specific buildings are inferred.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class CanonBuilding implements CanonObject {

    public enum ShellType {
        POOR_HOUSE, ELDER_HOUSE, STORAGE_SHED, COMMON_PLAZA, FARM_PLOT
    }

    public record RoomPlacement(CanonRoom room, int dx, int dy, int dz) {
        public RoomPlacement {
            Objects.requireNonNull(room, "room");
        }
    }

    private final String canonId;
    private final String canonEvidenceStr;
    private final String owner;
    private final String era;
    private final String purpose;
    private final ShellType shellType;
    private final String name;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final List<RoomPlacement> roomPlacements;

    public CanonBuilding(String canonId, String canonEvidenceStr,
                         String owner, String era, String purpose,
                         ShellType shellType, String name,
                         int sizeX, int sizeY, int sizeZ,
                         List<RoomPlacement> roomPlacements) {
        this.canonId = Objects.requireNonNull(canonId, "canonId");
        this.canonEvidenceStr = Objects.requireNonNull(canonEvidenceStr, "canonEvidenceStr");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.era = Objects.requireNonNull(era, "era");
        this.purpose = Objects.requireNonNull(purpose, "purpose");
        this.shellType = Objects.requireNonNull(shellType, "shellType");
        this.name = Objects.requireNonNull(name, "name");
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            throw new IllegalArgumentException("Building size must be positive");
        }
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.roomPlacements = roomPlacements == null
                ? List.of()
                : Collections.unmodifiableList(List.copyOf(roomPlacements));
    }

    @Override
    public String canonId() {
        return canonId;
    }

    @Override
    public String canonEvidence() {
        return canonEvidenceStr;
    }

    public String owner() {
        return owner;
    }

    public String era() {
        return era;
    }

    public String purpose() {
        return purpose;
    }

    public ShellType shellType() {
        return shellType;
    }

    public String name() {
        return name;
    }

    public List<RoomPlacement> roomPlacements() {
        return roomPlacements;
    }

    @Override
    public RelativeBounds relativeBounds() {
        return new RelativeBounds(0, 0, 0, sizeX - 1, sizeY - 1, sizeZ - 1);
    }

    @Override
    public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
        if (!intersectsChunk(dx, dz, placer.bounds())) return;
        buildShell(placer, dx, dy, dz);
        for (RoomPlacement rp : roomPlacements) {
            rp.room().materializeInto(placer, dx + rp.dx(), dy + rp.dy(), dz + rp.dz());
        }
    }

    private void buildShell(VolumePlacer placer, int dx, int dy, int dz) {
        BlockState floor = shellFloor();
        BlockState wall = shellWall();
        BlockState roof = shellRoof();

        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                placer.placeBlock(new BlockPos(dx + x, dy, dz + z), floor);
            }
        }

        if (shellType != ShellType.COMMON_PLAZA && shellType != ShellType.FARM_PLOT) {
            int wallTop = sizeY - 2;
            for (int y = 1; y <= wallTop; y++) {
                for (int x = 0; x < sizeX; x++) {
                    placer.placeBlock(new BlockPos(dx + x, dy + y, dz), wall);
                    placer.placeBlock(new BlockPos(dx + x, dy + y, dz + sizeZ - 1), wall);
                }
                for (int z = 1; z < sizeZ - 1; z++) {
                    placer.placeBlock(new BlockPos(dx, dy + y, dz + z), wall);
                    placer.placeBlock(new BlockPos(dx + sizeX - 1, dy + y, dz + z), wall);
                }
            }
            for (int x = 0; x < sizeX; x++) {
                for (int z = 0; z < sizeZ; z++) {
                    placer.placeBlock(new BlockPos(dx + x, dy + sizeY - 1, dz + z), roof);
                }
            }
            int doorX = dx + sizeX / 2;
            if (shellType == ShellType.STORAGE_SHED) {
                placer.placeBlock(new BlockPos(doorX, dy + 1, dz + sizeZ - 1),
                        Blocks.AIR.defaultBlockState());
                if (sizeY >= 4) {
                    placer.placeBlock(new BlockPos(doorX, dy + 2, dz + sizeZ - 1),
                            Blocks.AIR.defaultBlockState());
                }
            } else {
                placer.placeBlock(new BlockPos(doorX, dy + 1, dz + sizeZ - 1),
                        Blocks.OAK_DOOR.defaultBlockState()
                                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                                .setValue(DoorBlock.FACING, net.minecraft.core.Direction.NORTH)
                                .setValue(DoorBlock.OPEN, false));
                if (sizeY >= 4) {
                    placer.placeBlock(new BlockPos(doorX, dy + 2, dz + sizeZ - 1),
                            Blocks.OAK_DOOR.defaultBlockState()
                                    .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                                    .setValue(DoorBlock.FACING, net.minecraft.core.Direction.NORTH)
                                    .setValue(DoorBlock.OPEN, false));
                }
            }
        }
    }

    private BlockState shellFloor() {
        return switch (shellType) {
            case POOR_HOUSE, ELDER_HOUSE -> Blocks.OAK_PLANKS.defaultBlockState();
            case STORAGE_SHED -> Blocks.COBBLESTONE.defaultBlockState();
            case COMMON_PLAZA -> Blocks.POLISHED_ANDESITE.defaultBlockState();
            case FARM_PLOT -> Blocks.FARMLAND.defaultBlockState();
        };
    }

    private BlockState shellWall() {
        return switch (shellType) {
            case POOR_HOUSE -> Blocks.OAK_LOG.defaultBlockState();
            case ELDER_HOUSE -> Blocks.SPRUCE_PLANKS.defaultBlockState();
            case STORAGE_SHED -> Blocks.OAK_LOG.defaultBlockState();
            case COMMON_PLAZA, FARM_PLOT -> Blocks.AIR.defaultBlockState();
        };
    }

    private BlockState shellRoof() {
        return switch (shellType) {
            case POOR_HOUSE, STORAGE_SHED -> Blocks.OAK_PLANKS.defaultBlockState();
            case ELDER_HOUSE -> Blocks.SPRUCE_PLANKS.defaultBlockState();
            case COMMON_PLAZA, FARM_PLOT -> Blocks.AIR.defaultBlockState();
        };
    }
}
