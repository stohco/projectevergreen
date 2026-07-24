package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BedPart;

/**
 * HengYueSectBuilder — a FULLY hand-built Heng Yue Sect (恒岳派), Wang Lin's
 * first cultivation sect. Every block is placed intentionally in Java — NOT a
 * block-swap script, NOT a placeholder marker.
 *
 * <p>Constitution: "The world is completely hand-crafted, accurate to the novels.
 * NEVER write a script that replaces vanilla blocks with other blocks as a shortcut.
 * Every structure must be hand-authored."
 *
 * <p>The sect is a mountain cultivation compound with 14 districts:
 * mountain base, stone steps, outer gate, main plaza, library pavilion (3-story
 * pagoda), alchemy courtyard, sword peak, ancestor hall, spirit spring, sword
 * tomb entrance, seclusion caves, dormitories, lanterns, defensive walls.
 *
 * <p>CRON-COMPLETIONIST-26: Block palette now uses ErgenverseBlocks — all primary
 * structure materials are canon-correct spirit stone, spirit wood, ancient spirit log,
 * formation core stone, restriction stone, scorched stone, and blood stone.
 * Path/functional blocks (cobblestone, iron bars, water, lanterns) remain vanilla
 * where no custom equivalent exists.
 *
 * <h2>Harsh Self-Critique</h2>
 * <ul>
 *   <li>The "mountain" is mostly underground — the terraces sit at y=-6..-1 and
 *       are invisible unless natural terrain slopes away. Reads as a fortified
 *       compound, not a mountain sect.</li>
 *   <li>Roofs are flat or single-layer stair eaves — not the curved, upturned-eave
 *       xianxia pagoda silhouette. No dougong brackets, no ridge tiles.</li>
 *   <li>Symmetry and repetition dominate — identical caves/dorms/pillars/gates,
 *       no weathering (mossy/cracked scatter) or ruined variation.</li>
 *   <li>Tomb/caves are carved into manually-piled stone outcrops rather than
 *       real terrain; the CHEST is empty (no loot table).</li>
 *   <li>B.STONE_BRICK, B.MOSSY_BRICK, B.CRACKED_BRICK all map to SPIRIT_STONE_BLOCK —
 *       no distinct cracked/mossy spirit stone variant exists yet. Weathering
 *       reads as uniform. Needs separate weathered spirit stone blocks.</li>
 * </ul>
 */
public final class HengYueSectBuilder {

    /**
     * Lazy-initialized BlockState holder. ErgenverseBlocks.X.get() throws NPE before
     * Forge resolves the block registry, so these cannot be static-final in the outer
     * class. This inner class loads on first reference (during build(), which runs at
     * world-gen time — well after registry resolution).
     */
    private static final class B {
        private static final BlockState SPIRIT_STONE = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
        private static final BlockState SPIRIT_STONE_SLAB = ErgenverseBlocks.SPIRIT_STONE_SLAB.get().defaultBlockState();
        private static final BlockState STONE_BRICK = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
        private static final BlockState MOSSY_BRICK = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
        private static final BlockState CRACKED_BRICK = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
        private static final BlockState BRICK_WALL = ErgenverseBlocks.SPIRIT_STONE_WALL.get().defaultBlockState();
        private static final BlockState DEEPSLATE_BRICK = ErgenverseBlocks.SCORCHED_STONE.get().defaultBlockState();
        private static final BlockState SPRUCE_PLANK = ErgenverseBlocks.SPIRIT_WOOD_PLANKS.get().defaultBlockState();
        private static final BlockState SPRUCE_LOG = ErgenverseBlocks.SPIRIT_WOOD_LOG.get().defaultBlockState();
        private static final BlockState DARK_OAK_PLANK = ErgenverseBlocks.SPIRIT_WOOD_PLANKS.get().defaultBlockState();
        private static final BlockState DARK_OAK_LOG = ErgenverseBlocks.ANCIENT_SPIRIT_LOG.get().defaultBlockState();
        private static final BlockState DARK_OAK_STAIR = ErgenverseBlocks.ANCIENT_SPIRIT_STAIRS.get().defaultBlockState();
        private static final BlockState SPRUCE_STAIR = ErgenverseBlocks.SPIRIT_WOOD_PLANKS_STAIRS.get().defaultBlockState();
        private static final BlockState BRICK_STAIR = ErgenverseBlocks.SPIRIT_STONE_STAIRS.get().defaultBlockState();
        private static final BlockState LAPIS = ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState();
        private static final BlockState OBSIDIAN = ErgenverseBlocks.RESTRICTION_STONE.get().defaultBlockState();
        private static final BlockState REDSTONE_BLOCK = ErgenverseBlocks.BLOOD_STONE.get().defaultBlockState();
    }

    private HengYueSectBuilder() {}

    // ── Block palette (ErgenverseBlocks — canon-correct spirit materials) ──
    // CRON-COMPLETIONIST-26: Replaced all vanilla stand-ins with custom blocks.
    private static final BlockState COBBLE         = Blocks.COBBLESTONE.defaultBlockState(); // path material — vanilla
    private static final BlockState LANTERN        = Blocks.LANTERN.defaultBlockState(); // keep vanilla
    private static final BlockState END_ROD        = Blocks.END_ROD.defaultBlockState(); // keep vanilla
    private static final BlockState SEA_LANTERN    = Blocks.SEA_LANTERN.defaultBlockState(); // keep vanilla
    private static final BlockState GLOWSTONE      = Blocks.GLOWSTONE.defaultBlockState(); // keep vanilla
    private static final BlockState GOLD           = Blocks.GOLD_BLOCK.defaultBlockState(); // sect plaque — vanilla
    private static final BlockState AMETHYST       = Blocks.AMETHYST_BLOCK.defaultBlockState(); // keep vanilla
    private static final BlockState IRON_BARS      = Blocks.IRON_BARS.defaultBlockState();
    private static final BlockState WATER          = Blocks.WATER.defaultBlockState();
    private static final BlockState BOOKSHELF      = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState LECTERN        = Blocks.LECTERN.defaultBlockState();
    private static final BlockState BLAST_FURNACE  = Blocks.BLAST_FURNACE.defaultBlockState();
    private static final BlockState CAULDRON       = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState SMOKER         = Blocks.SMOKER.defaultBlockState();
    private static final BlockState CAMPFIRE       = Blocks.CAMPFIRE.defaultBlockState();
    private static final BlockState CHEST          = Blocks.CHEST.defaultBlockState();
    private static final BlockState SKELETON_SKULL = Blocks.SKELETON_SKULL.defaultBlockState();
    private static final BlockState FLOWER_POT     = Blocks.FLOWER_POT.defaultBlockState();
    private static final BlockState HAY            = Blocks.HAY_BLOCK.defaultBlockState();
    private static final BlockState ANVIL          = Blocks.ANVIL.defaultBlockState();
    private static final BlockState FERN           = Blocks.FERN.defaultBlockState();
    private static final BlockState AZALEA         = Blocks.FLOWERING_AZALEA.defaultBlockState();
    private static final BlockState CORNFLOWER     = Blocks.CORNFLOWER.defaultBlockState();
    private static final BlockState BLUE_ORCHID    = Blocks.BLUE_ORCHID.defaultBlockState();
    private static final BlockState GRASS          = Blocks.GRASS.defaultBlockState();
    private static final BlockState DIRT           = Blocks.DIRT.defaultBlockState();
    private static final BlockState STONE          = Blocks.STONE.defaultBlockState();
    private static final BlockState RED_BED        = Blocks.RED_BED.defaultBlockState();
    private static final BlockState BLUE_BED       = Blocks.BLUE_BED.defaultBlockState();
    private static final BlockState DARK_OAK_DOOR  = Blocks.DARK_OAK_DOOR.defaultBlockState();
    private static final BlockState SPRUCE_FENCE   = Blocks.SPRUCE_FENCE.defaultBlockState();
    private static final BlockState LILY_PAD       = Blocks.LILY_PAD.defaultBlockState();
    private static final BlockState SMOOTH_SLAB    = Blocks.SMOOTH_STONE_SLAB.defaultBlockState();

    /**
     * Build the full Heng Yue Sect centered at (x, groundY, z).
     * @param level the server overworld
     * @param center the plaza center block position (at ground level)
     */
    public static void build(ServerLevel level, BlockPos center) {
        if (isAlreadyBuilt(level, center)) return;
        dev.ergenverse.core.Ergenverse.LOGGER.info("[Ergenverse] Building Heng Yue Sect at {}", center);
        buildMountainBase(level, center);
        buildStoneSteps(level, center);
        buildOuterGate(level, center);
        buildMainPlaza(level, center);
        buildLibraryPavilion(level, center);
        buildAlchemyCourtyard(level, center);
        buildSwordPeak(level, center);
        buildAncestorHall(level, center);
        buildSpiritSpring(level, center);
        buildSwordTombEntrance(level, center);
        buildSeclusionCaves(level, center);
        buildDormitories(level, center);
        buildLanterns(level, center);
        buildDefensiveWalls(level, center);
        dev.ergenverse.core.Ergenverse.LOGGER.info("[Ergenverse] Heng Yue Sect construction complete.");
    }

    public static boolean isAlreadyBuilt(ServerLevel level, BlockPos center) {
        return level.getBlockState(center.above()).getBlock() == Blocks.SMOOTH_STONE;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  District builders
    // ═══════════════════════════════════════════════════════════════════

    private static void buildMountainBase(ServerLevel level, BlockPos c) {
        // 3 terraces rising from y=-6 to y=-1, 60×60 footprint
        for (int terrace = 0; terrace < 3; terrace++) {
            int r = 30 - terrace * 8;
            int y = c.getY() - 6 + terrace * 2;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    setBlock(level, c.offset(dx, y, dz), terrace == 0 ? STONE : COBBLE);
                }
            }
        }
        // Grass border on top terrace edges
        for (int dx = -30; dx <= 30; dx += 2) {
            setBlock(level, c.offset(dx, c.getY() - 1, -30), Blocks.GRASS_BLOCK.defaultBlockState());
            setBlock(level, c.offset(dx, c.getY() - 1, 30), Blocks.GRASS_BLOCK.defaultBlockState());
            setBlock(level, c.offset(-30, c.getY() - 1, dx), Blocks.GRASS_BLOCK.defaultBlockState());
            setBlock(level, c.offset(30, c.getY() - 1, dx), Blocks.GRASS_BLOCK.defaultBlockState());
        }
    }

    private static void buildStoneSteps(ServerLevel level, BlockPos c) {
        // Wide staircase 7 blocks wide, from z=+30 (base) to z=+10 (plaza level)
        for (int step = 0; step < 10; step++) {
            int z = c.getZ() + 30 - step * 2;
            int y = c.getY() - 6 + step;
            for (int dx = -3; dx <= 3; dx++) {
                setBlock(level, new BlockPos(c.getX() + dx, y, z), B.BRICK_STAIR);
            }
            // Flanking walls + lanterns every 4 steps
            setBlock(level, new BlockPos(c.getX() - 4, y, z), B.BRICK_WALL);
            setBlock(level, new BlockPos(c.getX() + 4, y, z), B.BRICK_WALL);
            if (step % 4 == 0) {
                setBlock(level, new BlockPos(c.getX() - 4, y + 1, z), LANTERN);
                setBlock(level, new BlockPos(c.getX() + 4, y + 1, z), LANTERN);
            }
        }
    }

    private static void buildOuterGate(ServerLevel level, BlockPos c) {
        int gx = c.getX(), gz = c.getZ() + 30, gy = c.getY();
        // Two pillars 2×1×6
        for (int dy = 0; dy < 6; dy++) {
            setBlock(level, new BlockPos(gx - 4, gy + dy, gz), B.STONE_BRICK);
            setBlock(level, new BlockPos(gx - 5, gy + dy, gz), B.STONE_BRICK);
            setBlock(level, new BlockPos(gx + 4, gy + dy, gz), B.STONE_BRICK);
            setBlock(level, new BlockPos(gx + 5, gy + dy, gz), B.STONE_BRICK);
        }
        // Lintel (dark oak logs across the top)
        for (int dx = -5; dx <= 5; dx++) {
            setBlock(level, new BlockPos(gx + dx, gy + 6, gz), B.DARK_OAK_LOG);
        }
        // Gold plaque above the gate
        setBlock(level, new BlockPos(gx, gy + 7, gz), GOLD);
        // Double dark oak door at the gap
        setBlock(level, new BlockPos(gx - 1, gy, gz), DARK_OAK_DOOR);
        setBlock(level, new BlockPos(gx, gy, gz), DARK_OAK_DOOR);
        setBlock(level, new BlockPos(gx - 1, gy + 1, gz), DARK_OAK_DOOR);
        setBlock(level, new BlockPos(gx, gy + 1, gz), DARK_OAK_DOOR);
        // Stone lion guardians (2-tall stylized)
        buildStoneLion(level, new BlockPos(gx - 7, gy, gz + 1));
        buildStoneLion(level, new BlockPos(gx + 7, gy, gz + 1));
    }

    private static void buildStoneLion(ServerLevel level, BlockPos base) {
        setBlock(level, base, COBBLE);
        setBlock(level, base.above(), B.STONE_BRICK);
        setBlock(level, base.above().above(), B.STONE_BRICK);
        setBlock(level, base.east(), COBBLE);
        setBlock(level, base.east().above(), COBBLE);
    }

    private static void buildMainPlaza(ServerLevel level, BlockPos c) {
        // 20×20 spirit stone floor
        fill(level, c.offset(-10, 0, -10), c.offset(10, 0, 10), B.SPIRIT_STONE);
        // B.LAPIS formation ring radius 5 at center
        ring(level, c, 5, 0, B.LAPIS);
        // Gold foundation anchor at dead center
        setBlock(level, c, GOLD);
        // Four corner pillars 2×2×4 with END_ROD lights
        int[][] corners = {{-9, -9}, {9, -9}, {-9, 9}, {9, 9}};
        for (int[] corner : corners) {
            for (int dy = 1; dy <= 4; dy++) {
                setBlock(level, c.offset(corner[0], dy, corner[1]), B.STONE_BRICK);
                setBlock(level, c.offset(corner[0] + (corner[0] < 0 ? 1 : -1), dy, corner[1]), B.STONE_BRICK);
            }
            setBlock(level, c.offset(corner[0], 5, corner[1]), END_ROD);
        }
    }

    private static void buildLibraryPavilion(ServerLevel level, BlockPos c) {
        BlockPos base = c.offset(18, 0, 0);
        // 3-story pagoda, each story inset 1 block: 9×9, 7×7, 5×5
        int[] sizes = {9, 7, 5};
        for (int story = 0; story < 3; story++) {
            int s = sizes[story];
            int y = base.getY() + story * 4;
            int half = s / 2;
            // Floor
            fill(level, base.offset(-half, y, -half), base.offset(half, y, half), B.SPRUCE_PLANK);
            // Walls (bookshelves interior, stone brick exterior)
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    if (dx == -half || dx == half || dz == -half || dz == half) {
                        for (int dy = 1; dy <= 3; dy++) {
                            setBlock(level, base.offset(dx, y + dy, dz), B.STONE_BRICK);
                        }
                    } else if (story < 2) {
                        // Interior bookshelves on ground floor
                        setBlock(level, base.offset(dx, y + 1, dz), BOOKSHELF);
                    }
                }
            }
            // Lectern at center
            setBlock(level, base.offset(0, y + 1, 0), LECTERN);
            // Sea lantern light in corners
            setBlock(level, base.offset(-half + 1, y + 1, -half + 1), SEA_LANTERN);
            setBlock(level, base.offset(half - 1, y + 1, -half + 1), SEA_LANTERN);
            setBlock(level, base.offset(-half + 1, y + 1, half - 1), SEA_LANTERN);
            setBlock(level, base.offset(half - 1, y + 1, half - 1), SEA_LANTERN);
            // Pagoda roof: stair ring facing outward, 2 layers
            for (int dx = -half; dx <= half; dx++) {
                setBlock(level, base.offset(dx, y + 4, -half), B.DARK_OAK_STAIR);
                setBlock(level, base.offset(dx, y + 4, half),
                        B.DARK_OAK_STAIR.setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH));
            }
            for (int dz = -half; dz <= half; dz++) {
                setBlock(level, base.offset(-half, y + 4, dz),
                        B.DARK_OAK_STAIR.setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.EAST));
                setBlock(level, base.offset(half, y + 4, dz),
                        B.DARK_OAK_STAIR.setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.WEST));
            }
        }
        // Amethyst + end-rod finial on top
        setBlock(level, base.offset(0, 12, 0), AMETHYST);
        setBlock(level, base.offset(0, 13, 0), END_ROD);
    }

    private static void buildAlchemyCourtyard(ServerLevel level, BlockPos c) {
        BlockPos base = c.offset(-18, 0, 0);
        // 12×12 stone brick floor
        fill(level, base.offset(-6, 0, -6), base.offset(6, 0, 6), B.STONE_BRICK);
        // Furnaces along the west wall
        setBlock(level, base.offset(-6, 1, -3), BLAST_FURNACE);
        setBlock(level, base.offset(-6, 1, -1), BLAST_FURNACE);
        setBlock(level, base.offset(-6, 1, 1), BLAST_FURNACE);
        setBlock(level, base.offset(-6, 1, 3), SMOKER);
        setBlock(level, base.offset(-6, 2, -2), CAULDRON);
        setBlock(level, base.offset(-6, 2, 2), CAULDRON);
        // Water channel through the middle
        for (int dz = -5; dz <= 5; dz++) {
            setBlock(level, base.offset(0, 0, dz), WATER);
        }
        // Herb garden along the south edge (terraced 2 levels)
        for (int dx = -5; dx <= 5; dx++) {
            setBlock(level, base.offset(dx, 1, 5), B.SPRUCE_PLANK);
            setBlock(level, base.offset(dx, 2, 5), switch ((dx + 5) % 4) {
                case 0 -> AZALEA;
                case 1 -> CORNFLOWER;
                case 2 -> BLUE_ORCHID;
                default -> GRASS;
            });
        }
    }

    private static void buildSwordPeak(ServerLevel level, BlockPos c) {
        BlockPos base = c.offset(15, 2, -15);
        // 12×12 cobble platform
        fill(level, base.offset(-6, 0, -6), base.offset(6, 0, 6), COBBLE);
        // 4 hay training dummies
        int[][] dummies = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] d : dummies) {
            setBlock(level, base.offset(d[0], 1, d[1]), SPRUCE_FENCE);
            setBlock(level, base.offset(d[0], 2, d[1]), HAY);
        }
        // Iron bars sword-formation circle radius 4
        ring(level, base, 4, 1, IRON_BARS);
        // Anvil at center
        setBlock(level, base.offset(0, 1, 0), ANVIL);
        // Weapon rack (spruce planks shelf)
        for (int dx = -2; dx <= 2; dx++) {
            setBlock(level, base.offset(dx, 2, -6), B.SPRUCE_PLANK);
        }
    }

    private static void buildAncestorHall(ServerLevel level, BlockPos c) {
        BlockPos base = c.offset(0, 0, -18);
        // 12×8 dark oak floor
        fill(level, base.offset(-6, 0, -4), base.offset(6, 0, 4), B.DARK_OAK_PLANK);
        // Stone brick walls
        for (int dx = -6; dx <= 6; dx++) {
            for (int dy = 1; dy <= 4; dy++) {
                setBlock(level, base.offset(dx, dy, -4), B.STONE_BRICK);
                setBlock(level, base.offset(dx, dy, 4), B.STONE_BRICK);
            }
        }
        for (int dz = -4; dz <= 4; dz++) {
            for (int dy = 1; dy <= 4; dy++) {
                setBlock(level, base.offset(-6, dy, dz), B.STONE_BRICK);
                setBlock(level, base.offset(6, dy, dz), B.STONE_BRICK);
            }
        }
        // 5 memorial tablets (flower pot on smooth stone slab) along north wall
        for (int i = 0; i < 5; i++) {
            int dx = -4 + i * 2;
            setBlock(level, base.offset(dx, 1, -3), SMOOTH_SLAB);
            setBlock(level, base.offset(dx, 2, -3), FLOWER_POT);
        }
        // 4 campfire incense braziers
        setBlock(level, base.offset(-5, 1, 3), CAMPFIRE);
        setBlock(level, base.offset(-2, 1, 3), CAMPFIRE);
        setBlock(level, base.offset(2, 1, 3), CAMPFIRE);
        setBlock(level, base.offset(5, 1, 3), CAMPFIRE);
        // Gold accent on the back wall
        setBlock(level, base.offset(0, 3, -4), GOLD);
        // Soul lanterns (dark interior)
        setBlock(level, base.offset(-4, 4, 0), Blocks.SOUL_LANTERN.defaultBlockState());
        setBlock(level, base.offset(4, 4, 0), Blocks.SOUL_LANTERN.defaultBlockState());
    }

    private static void buildSpiritSpring(ServerLevel level, BlockPos c) {
        BlockPos base = c.offset(-15, 0, 15);
        // 7×7 pool, 2 deep
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                setBlock(level, base.offset(dx, -2, dz), GLOWSTONE);
                setBlock(level, base.offset(dx, -1, dz), WATER);
                setBlock(level, base.offset(dx, 0, dz), WATER);
                // Smooth stone rim
                if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                    setBlock(level, base.offset(dx, 1, dz), B.SPIRIT_STONE);
                }
            }
        }
        // Lily pads on the surface
        setBlock(level, base.offset(-1, 1, -1), LILY_PAD);
        setBlock(level, base.offset(2, 1, 1), LILY_PAD);
    }

    private static void buildSwordTombEntrance(ServerLevel level, BlockPos c) {
        BlockPos base = c.offset(25, 0, 5);
        // 3×4 deepslate doorway frame
        for (int dy = 0; dy < 4; dy++) {
            setBlock(level, base.offset(-1, dy, 0), B.DEEPSLATE_BRICK);
            setBlock(level, base.offset(1, dy, 0), B.DEEPSLATE_BRICK);
        }
        setBlock(level, base.offset(-1, 4, 0), B.DEEPSLATE_BRICK);
        setBlock(level, base.offset(0, 4, 0), B.DEEPSLATE_BRICK);
        setBlock(level, base.offset(1, 4, 0), B.DEEPSLATE_BRICK);
        // Iron bars gate
        setBlock(level, base.offset(0, 0, 0), IRON_BARS);
        setBlock(level, base.offset(0, 1, 0), IRON_BARS);
        // Passage 3 wide × 4 tall × 8 deep into stone
        for (int dz = 1; dz <= 8; dz++) {
            for (int dy = 0; dy < 4; dy++) {
                setBlock(level, base.offset(0, dy, dz), Blocks.AIR.defaultBlockState());
            }
        }
        // Hollow chamber 10×10×6 at the end
        BlockPos chamber = base.offset(0, 0, 9);
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = 0; dy < 6; dy++) {
                for (int dz = 0; dz <= 6; dz++) {
                    if (dx == -5 || dx == 5 || dy == 0 || dy == 5 || dz == 0 || dz == 6) {
                        setBlock(level, chamber.offset(dx, dy, dz), B.DEEPSLATE_BRICK);
                    } else {
                        setBlock(level, chamber.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        // 3 skeleton skulls (fallen swordsmen)
        setBlock(level, chamber.offset(-3, 1, 3), SKELETON_SKULL);
        setBlock(level, chamber.offset(0, 1, 2), SKELETON_SKULL);
        setBlock(level, chamber.offset(3, 1, 4), SKELETON_SKULL);
        // Chest at the back wall center
        ChestHelper.placeChestWithLoot(level, chamber.offset(0, 1, 5),
                new ResourceLocation("ergenverse", "chests/heng_yue_sect_mountain_cave"));
        // Sea lantern lights in corners
        setBlock(level, chamber.offset(-4, 1, 1), SEA_LANTERN);
        setBlock(level, chamber.offset(4, 1, 1), SEA_LANTERN);
        setBlock(level, chamber.offset(-4, 1, 5), SEA_LANTERN);
        setBlock(level, chamber.offset(4, 1, 5), SEA_LANTERN);
    }

    private static void buildSeclusionCaves(ServerLevel level, BlockPos c) {
        // 3 cave entrances along the north face
        int[] xs = {-12, 0, 12};
        for (int x : xs) {
            BlockPos caveBase = c.offset(x, 0, -25);
            // Cobble frame
            for (int dy = 0; dy < 3; dy++) {
                setBlock(level, caveBase.offset(-1, dy, 0), COBBLE);
                setBlock(level, caveBase.offset(1, dy, 0), COBBLE);
            }
            setBlock(level, caveBase.offset(-1, 3, 0), COBBLE);
            setBlock(level, caveBase.offset(0, 3, 0), COBBLE);
            setBlock(level, caveBase.offset(1, 3, 0), COBBLE);
            // Entrance (air)
            setBlock(level, caveBase.offset(0, 0, 0), Blocks.AIR.defaultBlockState());
            setBlock(level, caveBase.offset(0, 1, 0), Blocks.AIR.defaultBlockState());
            // Hollow room 5×5×4
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = 0; dy < 4; dy++) {
                    for (int dz = 1; dz <= 5; dz++) {
                        if (dx == -2 || dx == 2 || dy == 0 || dy == 3 || dz == 1 || dz == 5) {
                            setBlock(level, caveBase.offset(dx, dy, dz), STONE);
                        } else {
                            setBlock(level, caveBase.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
            // Meditation mat (spruce slab) + lectern + lantern
            setBlock(level, caveBase.offset(0, 1, 3), SMOOTH_SLAB);
            setBlock(level, caveBase.offset(0, 1, 4), LECTERN);
            setBlock(level, caveBase.offset(-1, 1, 2), LANTERN);
        }
    }

    private static void buildDormitories(ServerLevel level, BlockPos c) {
        // 2 long halls 16×6 along the south of inner sect
        BlockPos[] halls = {c.offset(-8, 0, 12), c.offset(8, 0, 12)};
        for (BlockPos hall : halls) {
            // Floor
            fill(level, hall.offset(-8, 0, -3), hall.offset(8, 0, 3), B.SPRUCE_PLANK);
            // Walls
            for (int dx = -8; dx <= 8; dx++) {
                for (int dy = 1; dy <= 4; dy++) {
                    setBlock(level, hall.offset(dx, dy, -3), B.STONE_BRICK);
                    setBlock(level, hall.offset(dx, dy, 3), B.STONE_BRICK);
                }
            }
            // Roof (dark oak stairs)
            for (int dx = -8; dx <= 8; dx++) {
                setBlock(level, hall.offset(dx, 5, -3),
                        B.DARK_OAK_STAIR.setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.SOUTH));
                setBlock(level, hall.offset(dx, 5, 3),
                        B.DARK_OAK_STAIR.setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH));
                setBlock(level, hall.offset(dx, 6, 0), B.DARK_OAK_PLANK);
            }
            // 6 beds per hall (alternating red/blue)
            for (int i = 0; i < 6; i++) {
                int dx = -7 + i * 3;
                BlockState bed = (i % 2 == 0 ? RED_BED : BLUE_BED)
                        .setValue(BedBlock.PART, BedPart.HEAD)
                        .setValue(BedBlock.FACING, Direction.NORTH);
                setBlock(level, hall.offset(dx, 1, -2), bed);
                BlockState bedFoot = (i % 2 == 0 ? RED_BED : BLUE_BED)
                        .setValue(BedBlock.PART, BedPart.FOOT)
                        .setValue(BedBlock.FACING, Direction.NORTH);
                setBlock(level, hall.offset(dx, 1, -1), bedFoot);
            }
            // Lanterns
            for (int dx = -6; dx <= 6; dx += 4) {
                setBlock(level, hall.offset(dx, 4, 0), LANTERN);
            }
        }
    }

    private static void buildLanterns(ServerLevel level, BlockPos c) {
        // Plaza perimeter lanterns
        for (int i = -10; i <= 10; i += 4) {
            setBlock(level, c.offset(i, 1, -10), LANTERN);
            setBlock(level, c.offset(i, 1, 10), LANTERN);
            setBlock(level, c.offset(-10, 1, i), LANTERN);
            setBlock(level, c.offset(10, 1, i), LANTERN);
        }
        // END_ROD spirit lights at building entrances
        setBlock(level, c.offset(0, 1, -11), END_ROD);  // ancestor hall
        setBlock(level, c.offset(11, 1, 0), END_ROD);   // library
        setBlock(level, c.offset(-11, 1, 0), END_ROD);  // alchemy
    }

    private static void buildDefensiveWalls(ServerLevel level, BlockPos c) {
        // 70×70 ring, 3 tall, with 4 cardinal gates
        int r = 32;
        for (int dx = -r; dx <= r; dx++) {
            // North + South walls (skip gate gaps)
            if (Math.abs(dx) > 1) {
                for (int dy = 1; dy <= 3; dy++) {
                    setBlock(level, c.offset(dx, dy, -r), B.BRICK_WALL);
                    setBlock(level, c.offset(dx, dy, r), B.BRICK_WALL);
                }
            }
        }
        for (int dz = -r; dz <= r; dz++) {
            if (Math.abs(dz) > 1) {
                for (int dy = 1; dy <= 3; dy++) {
                    setBlock(level, c.offset(-r, dy, dz), B.BRICK_WALL);
                    setBlock(level, c.offset(r, dy, dz), B.BRICK_WALL);
                }
            }
        }
        // 4 gate pillars (flanking the gaps) with lanterns on top
        int[][] gateXZ = {{0, -r}, {0, r}, {-r, 0}, {r, 0}};
        for (int[] g : gateXZ) {
            for (int dx = -2; dx <= 2; dx += 4) {
                for (int dz = -2; dz <= 2; dz += 4) {
                    for (int dy = 1; dy <= 4; dy++) {
                        setBlock(level, c.offset(g[0] + dx, dy, g[1] + dz), B.STONE_BRICK);
                    }
                    setBlock(level, c.offset(g[0] + dx, 5, g[1] + dz), LANTERN);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════════

    private static void setBlock(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 3);
    }

    private static void fill(ServerLevel level, BlockPos from, BlockPos to, BlockState state) {
        BlockPos.betweenClosedStream(from, to).forEach(p -> level.setBlock(p, state, 3));
    }

    private static void ring(ServerLevel level, BlockPos center, int radius, int yOffset, BlockState state) {
        for (int angle = 0; angle < 360; angle += 5) {
            double rad = Math.toRadians(angle);
            int dx = (int) Math.round(Math.cos(rad) * radius);
            int dz = (int) Math.round(Math.sin(rad) * radius);
            setBlock(level, center.offset(dx, yOffset, dz), state);
        }
    }
}
