package dev.ergenverse.canon.structure;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * CanonBuilding — a <b>semantic</b> building composed of {@link CanonRoom}s +
 * a {@link BuildingTheme} + owner + era + purpose + bounding volume.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>Per the user's directive:
 * <blockquote>
 *   Even themes shouldn't know blocks. Instead of
 *   {@code BuildingTheme { wallBlock, roofBlock, floorBlock }} I'd do
 *   {@code BuildingTheme { POOR_VILLAGE, ELDER_HOME, … }}. Then the backend
 *   decides: POOR_VILLAGE → oak planks, spruce roof, stone foundation.
 * </blockquote>
 *
 * <p>A {@code CanonBuilding} carries:
 * <ul>
 *   <li><b>{@link #owner}</b>, <b>{@link #era}</b>, <b>{@link #purpose}</b> — lore.</li>
 *   <li><b>{@link #theme}</b> — a pure-semantic {@link BuildingTheme}. The
 *       {@link dev.ergenverse.assembly.BuildingLibrary} is the only place that
 *       maps a theme to {@link dev.ergenverse.assembly.MaterialID}s and shell
 *       geometry.</li>
 *   <li><b>{@link #sizeX}, {@link #sizeY}, {@link #sizeZ}</b> — exterior dims.</li>
 *   <li><b>{@link #roomPlacements}</b> — the rooms inside.</li>
 *   <li><b>{@link #anchors}</b> — named attachment points (e.g. the entrance).</li>
 * </ul>
 *
 * <p>The building does <b>not</b> build its own shell — that is the
 * {@code BuildingLibrary}'s job. CRON-125's {@code materializeInto} +
 * {@code buildShell} (which referenced {@code Blocks.OAK_PLANKS},
 * {@code DoorBlock}, etc.) are removed. The canon layer is now Minecraft-free.
 *
 * <p>CRON-125 used a nested {@code ShellType} enum; CRON-127 promotes it to the
 * top-level {@link BuildingTheme} and adds the future themes
 * ({@code SECT_DISCIPLE}, {@code CORE_ELDER}, {@code ANCIENT_RUIN},
 * {@code IMMORTAL_PALACE}) the user specified.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public final class CanonBuilding implements CanonObject {

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
    private final BuildingTheme theme;
    private final String name;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final List<RoomPlacement> roomPlacements;
    private final List<Anchor> anchors;

    public CanonBuilding(String canonId, String canonEvidenceStr,
                         String owner, String era, String purpose,
                         BuildingTheme theme, String name,
                         int sizeX, int sizeY, int sizeZ,
                         List<RoomPlacement> roomPlacements) {
        this(canonId, canonEvidenceStr, owner, era, purpose, theme, name,
                sizeX, sizeY, sizeZ, roomPlacements, List.of());
    }

    public CanonBuilding(String canonId, String canonEvidenceStr,
                         String owner, String era, String purpose,
                         BuildingTheme theme, String name,
                         int sizeX, int sizeY, int sizeZ,
                         List<RoomPlacement> roomPlacements,
                         List<Anchor> anchors) {
        this.canonId = Objects.requireNonNull(canonId, "canonId");
        this.canonEvidenceStr = Objects.requireNonNull(canonEvidenceStr, "canonEvidenceStr");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.era = Objects.requireNonNull(era, "era");
        this.purpose = Objects.requireNonNull(purpose, "purpose");
        this.theme = Objects.requireNonNull(theme, "theme");
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

    public String owner() {
        return owner;
    }

    public String era() {
        return era;
    }

    public String purpose() {
        return purpose;
    }

    public BuildingTheme theme() {
        return theme;
    }

    public String name() {
        return name;
    }

    public List<RoomPlacement> roomPlacements() {
        return roomPlacements;
    }

    public int sizeX() { return sizeX; }
    public int sizeY() { return sizeY; }
    public int sizeZ() { return sizeZ; }

    @Override
    public RelativeBounds relativeBounds() {
        return new RelativeBounds(0, 0, 0, sizeX - 1, sizeY - 1, sizeZ - 1);
    }

    @Override
    public List<Anchor> anchors() {
        return anchors;
    }
}
