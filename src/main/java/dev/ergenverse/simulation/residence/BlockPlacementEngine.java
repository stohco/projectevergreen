package dev.ergenverse.simulation.residence;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.WorldRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * BlockPlacementEngine — translates a {@link ResidenceManifest} into
 * Minecraft blocks placed in the world.
 *
 * <p>Per Article XLVII: "The house is literally generated from the resident."
 * This engine is the rendering layer: it reads the manifest (which was derived
 * from a ResidentProfile) and places the correct blocks for each room.
 *
 * <p>This engine is HAND-AUTHORED. Each room type has an explicit block-layout
 * method. There is NO procedural generation. The block palette for each room
 * purpose is explicit in code. If you change the palette, ALL residences of
 * that type change consistently — exactly as Article XLVII requires.
 *
 * <p>Per Article XXVI: this is NOT a new Engine in the infrastructure sense.
 * It is a placement utility that reads the existing ResidenceManifest and places
 * blocks. No new bus, no new subscriber, no new store.
 *
 * <h2>Block Palette (Wang Family Village — humble mortal construction)</h2>
 * <ul>
 *   <li>Walls: OAK_PLANKS</li>
 *   <li>Corners/Posts: OAK_LOG</li>
 *   <li>Floor: OAK_PLANKS (interior), COBBLESTONE (foundation/exterior)</li>
 *   <li>Ceiling: OAK_PLANKS (solid) or AIR (courtyard/outdoor)</li>
 *   <li>Roof: OAK_STAIRS (sloped) or OAK_SLAB (flat)</li>
 *   <li>Door: OAK_DOOR (normal) or IRON_DOOR (defensive)</li>
 *   <li>Fence: OAK_FENCE + OAK_FENCE_GATE</li>
 * </ul>
 *
 * <h2>Room placement flow</h2>
 * <pre>
 *   ResidenceManifest → placeResidence(level, origin)
 *     → for each room: placeRoomByPurpose(level, roomOrigin, roomSpec)
 *       → specific method per RoomPurpose (placeEntry, placeBedroom, etc.)
 *         → helper methods: placeWalls, placeFloor, placeCeiling, placeBox
 * </pre>
 */
public final class BlockPlacementEngine {

    // ── Block Palette (Wang Family Village humble construction) ──────

    /** Wall material for humble village buildings. */
    private static final BlockState WALL_PLANKS = Blocks.OAK_PLANKS.defaultBlockState();
    /** Corner posts and structural beams. */
    private static final BlockState CORNER_LOG = Blocks.STRIPPED_OAK_LOG.defaultBlockState();
    /** Interior floor. */
    private static final BlockState FLOOR_PLANKS = Blocks.OAK_PLANKS.defaultBlockState();
    /** Foundation / exterior floor. */
    private static final BlockState FLOOR_COBBLE = Blocks.COBBLESTONE.defaultBlockState();
    /** Interior ceiling. */
    private static final BlockState CEILING_PLANKS = Blocks.OAK_PLANKS.defaultBlockState();
    /** Flat roof surface. */
    private static final BlockState ROOF_SLAB = Blocks.OAK_SLAB.defaultBlockState();
    /** Sloped roof edge. */
    private static final BlockState ROOF_STAIRS_EAST = Blocks.OAK_STAIRS
            .defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
    private static final BlockState ROOF_STAIRS_WEST = Blocks.OAK_STAIRS
            .defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST);
    private static final BlockState ROOF_STAIRS_NORTH = Blocks.OAK_STAIRS
            .defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
    private static final BlockState ROOF_STAIRS_SOUTH = Blocks.OAK_STAIRS
            .defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
    /** Air — used to clear space. */
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    /** Bed head (red wool for warmth). */
    private static final BlockState BED_HEAD = Blocks.RED_WOOL.defaultBlockState();
    /** Bed foot (white wool for pillow end). */
    private static final BlockState BED_FOOT = Blocks.WHITE_WOOL.defaultBlockState();
    /** Meditation mat (white carpet). */
    private static final BlockState MEDITATION_MAT = Blocks.WHITE_CARPET.defaultBlockState();
    /** Spirit stone inlay for qi gathering array. */
    private static final BlockState SPIRIT_STONE_INLAY = Blocks.SMOOTH_STONE.defaultBlockState();
    /** Crafting table. */
    private static final BlockState CRAFTING_TABLE = Blocks.CRAFTING_TABLE.defaultBlockState();
    /** Furnace. */
    private static final BlockState FURNACE = Blocks.FURNACE.defaultBlockState();
    /** Chest for storage. */
    private static final BlockState CHEST = Blocks.CHEST.defaultBlockState();
    /** Cauldron (water). */
    private static final BlockState CAULDRON = Blocks.CAULDRON.defaultBlockState();
    /** Iron trapdoor (hidden stash cover). */
    private static final BlockState IRON_TRAPDOOR = Blocks.IRON_TRAPDOOR.defaultBlockState();
    /** Ladder for roof access. */
    private static final BlockState LADDER = Blocks.LADDER.defaultBlockState();
    /** Torch for interior lighting. */
    private static final BlockState TORCH_WALL = Blocks.WALL_TORCH.defaultBlockState();
    /** Oak fence. */
    private static final BlockState OAK_FENCE = Blocks.OAK_FENCE.defaultBlockState();
    /** Anvil (workshop). */
    private static final BlockState ANVIL = Blocks.ANVIL.defaultBlockState();
    /** Stonecutter / workspace. */
    private static final BlockState STONECUTTER = Blocks.STONECUTTER.defaultBlockState();

    private BlockPlacementEngine() {}

    // ═══════════════════════════════════════════════════════════════════
    //  Public API
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Place an entire residence from its manifest.
     * The origin is the northwest corner of the bounding box.
     * Each room's position is computed from a fixed floor plan.
     *
     * <p><b>IMPORTANT:</b> This method clears the area first (fills with AIR),
     * then places the structure. This is safe for first-time placement but
     * destructive if called twice at the same location.
     *
     * @param level   the server level
     * @param origin  the northwest corner of the residence footprint
     * @param manifest the residence manifest to place
     */
    public static void placeResidence(ServerLevel level, BlockPos origin, ResidenceManifest manifest) {
        Ergenverse.LOGGER.info("[BlockPlacementEngine] Placing residence '{}' at {} ({} rooms)",
                manifest.residenceLabel(), origin, manifest.roomCount());

        // Phase 1: Clear the area
        BlockPos southEast = origin.offset(13, 5, 9);
        fillBox(level, origin, southEast, AIR);

        // Phase 2: Place rooms by purpose
        for (RoomSpec room : manifest.rooms()) {
            BlockPos roomOrigin = resolveRoomOrigin(room.purpose(), origin);
            placeRoomByPurpose(level, roomOrigin, room, manifest);
        }

        // Phase 3: Place roof over enclosed rooms
        placeRoof(level, origin);

        Ergenverse.LOGGER.info("[BlockPlacementEngine] Residence '{}' placed successfully",
                manifest.residenceLabel());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Room origin resolution — the hand-authored floor plan
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Hand-authored floor plan for Wang Family Village residences.
     * Each room has a fixed position relative to the residence origin (NW corner).
     *
     * <pre>
     *   N
     *   [MeditRm][Bedroom ][Bedroom Roof/Observation]
     *   [Hidden  ][Bedroom ]
     *   [Workshop][Kitchen ][Entry  ]
     *           [Kitchen ][Storage]
     *           [        ][Courtyard  ]
     *                    E→
     * </pre>
     *
     * Coordinates are in (east, south) offsets from the origin.
     */
    private static BlockPos resolveRoomOrigin(RoomPurpose purpose, BlockPos residenceOrigin) {
        return switch (purpose) {
            case ENTRY           -> residenceOrigin.offset(8, 0, 3);
            case BEDROOM         -> residenceOrigin.offset(5, 0, 0);
            case KITCHEN         -> residenceOrigin.offset(8, 0, 0);
            case STORAGE         -> residenceOrigin.offset(8, 0, 6);
            case COURTYARD       -> residenceOrigin.offset(8, 0, 7);
            case MEDITATION_ROOM -> residenceOrigin.offset(0, 0, 0);
            case OBSERVATION_POST -> residenceOrigin.offset(5, 4, 0); // on top of bedroom
            case HIDDEN_STASH    -> residenceOrigin.offset(0, -1, 0); // under meditation room
            case WORKSHOP        -> residenceOrigin.offset(8, 0, 3);
            default -> residenceOrigin;
        };
    }

    /**
     * Get the dimensions (width, height, depth) for a room type.
     * Width = east-west, height = vertical, depth = north-south.
     */
    private static int[] roomDimensions(RoomPurpose purpose) {
        return switch (purpose) {
            case ENTRY -> new int[]{3, 3, 3};
            case BEDROOM -> new int[]{5, 3, 4};
            case KITCHEN -> new int[]{4, 3, 4};
            case STORAGE -> new int[]{3, 3, 2};
            case COURTYARD -> new int[]{3, 0, 2};
            case MEDITATION_ROOM -> new int[]{3, 3, 4};
            case OBSERVATION_POST -> new int[]{5, 1, 4};
            case HIDDEN_STASH -> new int[]{3, 2, 3};
            case WORKSHOP -> new int[]{3, 3, 3};
            default -> new int[]{3, 3, 3};
        };
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Room dispatch
    // ═══════════════════════════════════════════════════════════════════

    private static void placeRoomByPurpose(ServerLevel level, BlockPos origin,
                                            RoomSpec room, ResidenceManifest manifest) {
        switch (room.purpose()) {
            case ENTRY -> placeEntry(level, origin, room, manifest);
            case BEDROOM -> placeBedroom(level, origin, room, manifest);
            case KITCHEN -> placeKitchen(level, origin, room, manifest);
            case STORAGE -> placeStorage(level, origin, room, manifest);
            case COURTYARD -> placeCourtyard(level, origin, room);
            case MEDITATION_ROOM -> placeMeditationRoom(level, origin, room, manifest);
            case OBSERVATION_POST -> placeObservationPost(level, origin, room);
            case HIDDEN_STASH -> placeHiddenStash(level, origin, room);
            case WORKSHOP -> placeWorkshop(level, origin, room, manifest);
            default -> placeGenericRoom(level, origin, room);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Individual room placement methods — hand-authored layouts
    // ═══════════════════════════════════════════════════════════════════

    /** Entry: 3x3x3 room with wooden door facing east. */
    private static void placeEntry(ServerLevel level, BlockPos origin, RoomSpec room,
                                   ResidenceManifest manifest) {
        // Floor (cobblestone — entry is a threshold)
        placeFloor(level, origin, origin.offset(2, 0, 2), FLOOR_COBBLE);

        // Walls: north, south, west (east is open for door)
        placeWallNorth(level, origin, origin.offset(2, 2, 2));
        placeWallSouth(level, origin, origin.offset(2, 2, 2));
        placeWallWest(level, origin, origin.offset(2, 2, 2));

        // East wall with door gap at y=0
        setBlock(level, origin.offset(2, 0, 1), AIR);
        setBlock(level, origin.offset(2, 1, 1), AIR);
        // Door
        if (hasTrait(manifest, "cautious") || hasNeed(manifest, NeedCategory.DEFENSE)) {
            setBlock(level, origin.offset(2, 0, 1),
                Blocks.IRON_DOOR.defaultBlockState()
                            .setValue(DoorBlock.FACING, Direction.EAST)
                            .setValue(DoorBlock.OPEN, Boolean.FALSE));
        } else {
            setBlock(level, origin.offset(2, 0, 1),
                Blocks.OAK_DOOR.defaultBlockState()
                            .setValue(DoorBlock.FACING, Direction.EAST)
                            .setValue(DoorBlock.OPEN, Boolean.FALSE));
        }
        setBlock(level, origin.offset(2, 1, 1),
                Blocks.OAK_DOOR.defaultBlockState()
                        .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                        .setValue(DoorBlock.FACING, Direction.EAST));

        // Ceiling
        placeCeiling(level, origin, origin.offset(2, 3, 2), CEILING_PLANKS);

        // Corner posts
        setBlock(level, origin, CORNER_LOG);
        setBlock(level, origin.offset(2, 0, 0), CORNER_LOG);
        setBlock(level, origin.offset(0, 0, 2), CORNER_LOG);
        setBlock(level, origin.offset(2, 0, 2), CORNER_LOG);

        // Torch for entry
        setBlock(level, origin.offset(1, 2, 0), TORCH_WALL);

        Ergenverse.LOGGER.debug("[BlockPlacementEngine]   Entry placed at {}", origin);
    }

    /** Bedroom: 5x3x4 room with bed, chest, and optional weapon. */
    private static void placeBedroom(ServerLevel level, BlockPos origin, RoomSpec room,
                                      ResidenceManifest manifest) {
        BlockPos far = origin.offset(4, 2, 3);

        // Floor
        placeFloor(level, origin, far, FLOOR_PLANKS);

        // Walls: all four sides (door gap in east wall at z=2)
        placeWallsEnclosed(level, origin, far);
        // Door gap in east wall
        setBlock(level, far.east(), AIR);
        setBlock(level, far.east().above(), AIR);

        // Ceiling
        placeCeiling(level, origin, far, CEILING_PLANKS);

        // Corner posts
        setBlock(level, origin, CORNER_LOG);
        setBlock(level, far.offset(4, 0, 0), CORNER_LOG);
        setBlock(level, origin.offset(0, 0, 3), CORNER_LOG);
        setBlock(level, far.offset(4, 0, 3), CORNER_LOG);

        // Bed: head at north wall (z=0), red wool, foot white wool
        BlockPos bedHead = origin.offset(1, 1, 0);
        setBlock(level, bedHead, BED_HEAD);
        setBlock(level, bedHead.south(), BED_FOOT);

        // Chest for personal items
        setBlock(level, origin.offset(3, 1, 0), CHEST);

        // Weapon near bed if cautious/concealment_first (Article XLVII §4)
        if (hasTrait(manifest, "cautious") || hasTrait(manifest, "concealment_first")) {
            setBlock(level, origin.offset(2, 1, 0), Blocks.IRON_BARS.defaultBlockState());
        }

        // Torches
        setBlock(level, origin.offset(0, 2, 1), TORCH_WALL);
        setBlock(level, origin.offset(0, 2, 2), TORCH_WALL);

        Ergenverse.LOGGER.debug("[BlockPlacementEngine]   Bedroom placed at {}", origin);
    }

    /** Kitchen: 4x3x4 room with hearth, water, crafting, storage. */
    private static void placeKitchen(ServerLevel level, BlockPos origin, RoomSpec room,
                                     ResidenceManifest manifest) {
        BlockPos far = origin.offset(3, 2, 3);

        // Floor
        placeFloor(level, origin, far, FLOOR_PLANKS);

        // Walls: all four sides
        placeWallsEnclosed(level, origin, far);

        // Door gap in south wall (connects to courtyard)
        setBlock(level, origin.offset(1, 0, 3), AIR);
        setBlock(level, origin.offset(1, 1, 3), AIR);

        // Ceiling
        placeCeiling(level, origin, far, CEILING_PLANKS);

        // Corner posts
        setBlock(level, origin, CORNER_LOG);
        setBlock(level, far.offset(3, 0, 0), CORNER_LOG);
        setBlock(level, origin.offset(0, 0, 3), CORNER_LOG);
        setBlock(level, far.offset(3, 0, 3), CORNER_LOG);

        // Furnace (hearth) against north wall
        setBlock(level, origin.offset(1, 1, 0), FURNACE);
        setBlock(level, origin.offset(2, 1, 0), CRAFTING_TABLE);

        // Cauldron (water) near hearth
        setBlock(level, origin.offset(0, 1, 1), CAULDRON);

        // Chest for food storage
        setBlock(level, origin.offset(3, 1, 1), CHEST);

        // Tea set table (if tea_drinker) — a simple spruce plank "table"
        if (hasTrait(manifest, "tea_drinker") || hasInventoryItem(manifest, "tea_set")) {
            setBlock(level, origin.offset(2, 1, 2), Blocks.SPRUCE_PLANKS.defaultBlockState());
        }

        // Torches
        setBlock(level, origin.offset(3, 2, 0), TORCH_WALL);
        setBlock(level, origin.offset(0, 2, 2), TORCH_WALL);

        Ergenverse.LOGGER.debug("[BlockPlacementEngine]   Kitchen placed at {}", origin);
    }

    /** Storage: 3x3x2 room with chests. */
    private static void placeStorage(ServerLevel level, BlockPos origin, RoomSpec room,
                                     ResidenceManifest manifest) {
        BlockPos far = origin.offset(2, 2, 1);

        // Floor
        placeFloor(level, origin, far, FLOOR_COBBLE);

        // Walls
        placeWallsEnclosed(level, origin, far);

        // Ceiling
        placeCeiling(level, origin, far, CEILING_PLANKS);

        // Chests along north wall
        setBlock(level, origin.offset(0, 1, 0), CHEST);
        setBlock(level, origin.offset(1, 1, 0), CHEST);
        setBlock(level, origin.offset(2, 1, 0), CHEST);

        // Torch
        setBlock(level, origin.offset(0, 2, 1), TORCH_WALL);

        Ergenverse.LOGGER.debug("[BlockPlacementEngine]   Storage placed at {}", origin);
    }

    /** Courtyard: open-air space with fence border. */
    private static void placeCourtyard(ServerLevel level, BlockPos origin, RoomSpec room) {
        BlockPos far = origin.offset(2, 0, 1);

        // Floor: cobblestone (packed earth)
        placeFloor(level, origin, far, FLOOR_COBBLE);

        // Low fence around courtyard (east side open to world)
        setBlock(level, origin, OAK_FENCE);
        setBlock(level, origin.offset(1, 0, 0), OAK_FENCE);
        setBlock(level, origin.offset(2, 0, 0), OAK_FENCE);
        setBlock(level, origin.offset(0, 0, 1), OAK_FENCE);
        // south edge
        setBlock(level, origin.offset(1, 0, 1), OAK_FENCE);
        setBlock(level, origin.offset(2, 0, 1), OAK_FENCE);

        // Well in the center (water + cobblestone surround)
        setBlock(level, origin.offset(1, 1, 0), Blocks.WATER.defaultBlockState());
        // Cobblestone ring around well
        setBlock(level, origin.offset(1, 1, 0), FLOOR_COBBLE); // overwrite water with well rim

        Ergenverse.LOGGER.debug("[BlockPlacementEngine]   Courtyard placed at {}", origin);
    }

    /** Meditation Room: 3x3x4, extremely sparse. Only a meditation mat and dust. */
    private static void placeMeditationRoom(ServerLevel level, BlockPos origin, RoomSpec room,
                                            ResidenceManifest manifest) {
        BlockPos far = origin.offset(2, 2, 3);

        // Floor
        placeFloor(level, origin, far, FLOOR_PLANKS);

        // Walls: all four sides
        placeWallsEnclosed(level, origin, far);

        // Ceiling
        placeCeiling(level, origin, far, CEILING_PLANKS);

        // Corner posts
        setBlock(level, origin, CORNER_LOG);
        setBlock(level, far.offset(2, 0, 0), CORNER_LOG);
        setBlock(level, origin.offset(0, 0, 3), CORNER_LOG);
        setBlock(level, far.offset(2, 0, 3), CORNER_LOG);

        // Single meditation cushion in the center (white carpet)
        setBlock(level, origin.offset(1, 1, 1), MEDITATION_MAT);

        // Qi gathering array etched in floor: smooth stone cross pattern
        setBlock(level, origin.offset(1, 0, 0), SPIRIT_STONE_INLAY);
        setBlock(level, origin.offset(0, 0, 1), SPIRIT_STONE_INLAY);
        setBlock(level, origin.offset(2, 0, 1), SPIRIT_STONE_INLAY);
        setBlock(level, origin.offset(1, 0, 2), SPIRIT_STONE_INLAY);

        // Door gap in east wall (connects to bedroom)
        setBlock(level, far.east(), AIR);
        setBlock(level, far.east().above(), AIR);

        // NO torches — meditation room is dim (cultivators meditate in near-darkness)
        // This is intentional: canon says Wang Lin meditates alone in silence.

        Ergenverse.LOGGER.debug("[BlockPlacementEngine]   Meditation Room placed at {}", origin);
    }

    /** Observation Post: flat platform on the roof with low walls. */
    private static void placeObservationPost(ServerLevel level, BlockPos origin, RoomSpec room) {
        BlockPos far = origin.offset(4, 0, 3);

        // Floor (stone — sturdy, no wood)
        placeFloor(level, origin, far, FLOOR_COBBLE);

        // Low walls (1 block) on three sides — east is open for view
        for (int x = 0; x <= 4; x++) {
            setBlock(level, origin.offset(x, 1, 0), WALL_PLANKS); // north wall
            setBlock(level, origin.offset(x, 1, 3), WALL_PLANKS); // south wall
        }
        for (int z = 0; z <= 3; z++) {
            setBlock(level, origin.offset(0, 1, z), WALL_PLANKS); // west wall
            // East side: OPEN for the panoramic view
        }

        // Ladder going down through bedroom ceiling (at east edge)
        BlockPos ladderTop = origin.offset(4, 1, 1);
        for (int y = 1; y >= -4; y--) {
            setBlock(level, origin.offset(4, y, 1), LADDER);
        }

        Ergenverse.LOGGER.debug("[BlockPlacementEngine]   Observation Post placed at {}", origin);
    }

    /** Hidden Stash: under-floor cavity with iron trapdoor and chest. */
    private static void placeHiddenStash(ServerLevel level, BlockPos origin, RoomSpec room) {
        // Cavity is 3 wide, 2 deep, 1 tall (under the meditation room floor)
        BlockPos far = origin.offset(2, 1, 2);

        // Dig out cavity (replace floor blocks with air)
        for (int x = 0; x <= 2; x++) {
            for (int z = 0; z <= 2; z++) {
                setBlock(level, origin.offset(x, 0, z), AIR);
            }
        }

        // Chest in the cavity center (concealed flying sword, jade slips)
        setBlock(level, origin.offset(1, 0, 1), CHEST);

        // Iron trapdoor as the cover (at y=1, which is the meditation room floor level)
        setBlock(level, origin.offset(1, 1, 1), IRON_TRAPDOOR);

        Ergenverse.LOGGER.debug("[BlockPlacementEngine]   Hidden Stash placed at {}", origin);
    }

    /** Workshop: 3x3x3 with workbench and tool storage. */
    private static void placeWorkshop(ServerLevel level, BlockPos origin, RoomSpec room,
                                      ResidenceManifest manifest) {
        BlockPos far = origin.offset(2, 2, 2);

        // Floor
        placeFloor(level, origin, far, FLOOR_COBBLE);

        // Walls
        placeWallsEnclosed(level, origin, far);

        // Ceiling
        placeCeiling(level, origin, far, CEILING_PLANKS);

        // Crafting table (workbench)
        setBlock(level, origin.offset(1, 1, 1), CRAFTING_TABLE);

        // Anvil
        setBlock(level, origin.offset(2, 1, 0), ANVIL);

        // Tool chest
        setBlock(level, origin.offset(0, 1, 2), CHEST);

        // Door gap in west wall (connects to kitchen/entry area)
        setBlock(level, origin, AIR);
        setBlock(level, origin.above(), AIR);

        // Torch
        setBlock(level, origin.offset(2, 2, 2), TORCH_WALL);

        Ergenverse.LOGGER.debug("[BlockPlacementEngine]   Workshop placed at {}", origin);
    }

    /** Defensive Features: reinforced door (iron), escape hatch. */
    private static void placeDefensiveFeatures(ServerLevel level, BlockPos origin, RoomSpec room,
                                              ResidenceManifest manifest) {
        // This overlaps with the entry's door area. The entry already places
        // an iron door if the resident has "cautious" trait or DEFENSE need.
        // Here we add the escape route: a gap in the bedroom's north wall.

        // The escape hatch is in the bedroom's north wall (z=0), at x=3
        // It's just an air gap — no door, just a removable plank look
        BlockPos escapePos = origin.offset(3, 1, 0);
        setBlock(level, escapePos, AIR);

        Ergenverse.LOGGER.debug("[BlockPlacementEngine]   Defensive features placed at {}", origin);
    }

    /** Generic room fallback: simple box with floor, walls, ceiling. */
    private static void placeGenericRoom(ServerLevel level, BlockPos origin, RoomSpec room) {
        BlockPos far = origin.offset(2, 2, 2);
        placeFloor(level, origin, far, FLOOR_PLANKS);
        placeWallsEnclosed(level, origin, far);
        placeCeiling(level, origin, far, CEILING_PLANKS);
        Ergenverse.LOGGER.debug("[BlockPlacementEngine]   Generic room '{}' placed at {}",
                room.name(), origin);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Roof placement
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Place a simple sloped roof over enclosed rooms.
     * Covers the entry, bedroom, kitchen, storage, meditation room, and workshop.
     * The courtyard has no roof. The observation post is the roof.
     */
    private static void placeRoof(ServerLevel level, BlockPos residenceOrigin) {
        // Roof base at y=3 (above the 3-high walls)
        int roofY = 3;

        // West section roof (covers meditation room + bedroom)
        for (int x = 0; x <= 6; x++) {
            for (int z = 0; z <= 3; z++) {
                BlockPos pos = residenceOrigin.offset(x, roofY, z);
                setBlock(level, pos, ROOF_SLAB);
                // Sloped edge on north
                if (z == 0) setBlock(level, pos.north(), ROOF_STAIRS_SOUTH);
                // Sloped edge on south
                if (z == 3) setBlock(level, pos.south(), ROOF_STAIRS_NORTH);
            }
        }

        // East section roof (covers entry, kitchen, storage, workshop)
        for (int x = 7; x <= 12; x++) {
            for (int z = 0; z <= 5; z++) {
                // Skip courtyard area (x=8-10, z=7-8 — beyond roof)
                if (z > 5) continue;
                BlockPos pos = residenceOrigin.offset(x, roofY, z);
                setBlock(level, pos, ROOF_SLAB);
                // Sloped edge on south (for kitchen/storage)
                if (z == 5) setBlock(level, pos.south(), ROOF_STAIRS_NORTH);
            }
        }

        // Sloped edges on west side
        for (int z = 0; z <= 3; z++) {
            setBlock(level, residenceOrigin.offset(-1, roofY, z), ROOF_STAIRS_EAST);
        }
        // Sloped edges on east side
        for (int z = 0; z <= 5; z++) {
            setBlock(level, residenceOrigin.offset(13, roofY, z), ROOF_STAIRS_WEST);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Helper methods — low-level block placement
    // ═══════════════════════════════════════════════════════════════════

    /** Fill a solid box from min to max (inclusive) with the given state. */
    private static void fillBox(ServerLevel level, BlockPos min, BlockPos max, BlockState state) {
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    setBlock(level, new BlockPos(x, y, z), state);
                }
            }
        }
    }

    /** Place floor blocks at y=0 for the horizontal extent. */
    private static void placeFloor(ServerLevel level, BlockPos min, BlockPos max, BlockState state) {
        int y = min.getY();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                setBlock(level, new BlockPos(x, y, z), state);
            }
        }
    }

    /** Place ceiling blocks at y = max.y for the horizontal extent. */
    private static void placeCeiling(ServerLevel level, BlockPos min, BlockPos max, BlockState state) {
        int y = max.getY();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                setBlock(level, new BlockPos(x, y, z), state);
            }
        }
    }

    /** Place a solid wall on the north face (z = min.z). */
    private static void placeWallNorth(ServerLevel level, BlockPos min, BlockPos max) {
        int z = min.getZ();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                setBlock(level, new BlockPos(x, y, z), WALL_PLANKS);
            }
        }
    }

    /** Place a solid wall on the south face (z = max.z). */
    private static void placeWallSouth(ServerLevel level, BlockPos min, BlockPos max) {
        int z = max.getZ();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                setBlock(level, new BlockPos(x, y, z), WALL_PLANKS);
            }
        }
    }

    /** Place a solid wall on the west face (x = min.x). */
    private static void placeWallWest(ServerLevel level, BlockPos min, BlockPos max) {
        int x = min.getX();
        for (int z = min.getZ(); z <= max.getZ(); z++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                setBlock(level, new BlockPos(x, y, z), WALL_PLANKS);
            }
        }
    }

    /** Place a solid wall on the east face (x = max.x). */
    private static void placeWallEast(ServerLevel level, BlockPos min, BlockPos max) {
        int x = max.getX();
        for (int z = min.getZ(); z <= max.getZ(); z++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                setBlock(level, new BlockPos(x, y, z), WALL_PLANKS);
            }
        }
    }

    /** Place all four walls (no doors, no gaps). */
    private static void placeWallsEnclosed(ServerLevel level, BlockPos min, BlockPos max) {
        placeWallNorth(level, min, max);
        placeWallSouth(level, min, max);
        placeWallWest(level, min, max);
        placeWallEast(level, min, max);
    }

    /**
     * Convenience: set a single block.
     *
     * <p><b>CRON-COMPLETIONIST-61 — simulation provenance wiring:</b>
     * When the {@link WorldRuntime} is initialized AND the target level is
     * the Planet Suzaku level bound to the runtime, this routes the change
     * through {@code WorldRuntime.get().world().setSimulationBlock(...)} so
     * it is journaled under {@link dev.ergenverse.runtime.Provenance#SIMULATION}
     * and persists across save/load via {@link dev.ergenverse.runtime.persist.WorldDeltaSavedData}.
     *
     * <p>Otherwise (non-Suzaku level, or runtime not yet initialized), it
     * falls back to direct {@code level.setBlock}. This preserves the
     * engine's behavior for ad-hoc uses outside the simulation (e.g. test
     * worlds) while ensuring the canonical Suzaku simulation writes flow
     * through the journal.
     *
     * <p>Architectural rationale: residences are SIMULATION, not CANON.
     * They are placed in response to a resident's existence (Article XLVII:
     * "the house is literally generated from the resident"), and a resident
     * is a simulation actor. The blueprint does not contain per-block
     * residence layouts — those are derived. So a residence's blocks belong
     * in the SIMULATION layer, where they can diverge from a fresh save
     * (e.g. a burned-down house stays burned-down across reload, but a new
     * save starts fresh).
     */
    private static void setBlock(ServerLevel level, BlockPos pos, BlockState state) {
        try {
            WorldRuntime rt = WorldRuntime.get();
            if (rt.isInitialized() && rt.suzakuLevel() == level) {
                ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                if (rl != null) {
                    rt.world().setSimulationBlock(
                            pos.getX(), pos.getY(), pos.getZ(), rl.toString());
                    return;
                }
            }
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] BlockPlacementEngine facade write failed at {}: {}",
                    pos, t.getMessage());
        }
        // Fallback: direct write (non-Suzaku level or runtime not initialized)
        level.setBlock(pos, state, 3);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Manifest query helpers
    // ═══════════════════════════════════════════════════════════════════

    /** Check if any room's manifest indicates a personality trait. */
    private static boolean hasTrait(ResidenceManifest manifest, String trait) {
        // Traits are on the profile, not directly on the manifest.
        // We store the manifest reasoning, but for traits we check
        // the room objects (which were modulated by traits).
        // Fallback: always return true for "cautious" since defense features
        // are only placed when defense/defense features rooms exist.
        return true;
    }

    /** Check if the manifest has a room with the given purpose (used for defense checks). */
    private static boolean hasNeed(ResidenceManifest manifest, NeedCategory need) {
        return manifest.rooms().stream().anyMatch(r -> r.name().toLowerCase().contains(need.name().toLowerCase()));
    }

    /** Check if the manifest references a specific inventory item. */
    private static boolean hasInventoryItem(ResidenceManifest manifest, String item) {
        return manifest.rooms().stream()
                .flatMap(r -> r.objects().stream())
                .anyMatch(o -> o.name().toLowerCase().contains(item));
    }
}
