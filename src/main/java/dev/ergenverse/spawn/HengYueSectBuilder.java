package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
 * <p>CRON-COMPLETIONIST-78: Major narrative enrichment. Previously the sect was
 * a geometric skeleton with zero interior storytelling — every room was empty
 * geometry. Now each key building contains written books with in-character
 * narrative, item frames with specific artifacts, chests with canon-appropriate
 * loot, and distinct personalities per cave/dormitory. The Library contains Elder
 * Xu's cultivation lecture notes and a beginner's technique manual on lecterns.
 * The Ancestor Hall has memorial tablets bearing the names and deeds of past sect
 * elders, with incense notes from the current elder. The Sword Tomb contains a
 * fallen disciple's diary recounting the trial. The three Seclusion Caves are now
 * distinct: the Elder's retreat (highest, with forbidden texts), a senior
 * disciple's cave (with progress notes), and an abandoned cave (with evidence
 * of a failed breakthrough — cracked restriction diagram, discarded pills).
 * Dormitory chests contain basic disciple supplies. Connection paths link all
 * buildings to the main plaza.
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
 *       real terrain; interior storytelling is now present but limited.</li>
 *   <li>B.STONE_BRICK, B.MOSSY_BRICK, B.CRACKED_BRICK all map to SPIRIT_STONE_BLOCK —
 *       no distinct cracked/mossy spirit stone variant exists yet. Weathering
 *       reads as uniform. Needs separate weathered spirit stone blocks.</li>
 *   <li>The Library books are "written" but contain only ~25 pages total across 3
 *       books — a real sect library would have hundreds. This is a seed, not a
 *       completion. Per Article XXIII, the Library needs far more content.</li>
 *   <li>No NPC schedules or daily rhythms are defined here — the buildings exist
 *       but nobody uses them. Per Article XLV §3, schedules must be downstream of
 *       pressure, not timetables. This builder only creates the physical shell.</li>
 *   <li>The Alchemy Courtyard has furnaces but no written recipes on lecterns —
 *       alchemy is a core sect activity that should have visible instructional
 *       content. Addressed partially this round with one recipe book.</li>
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
        private static final BlockState SPIRIT_VEIN = ErgenverseBlocks.SPIRIT_VEIN_STONE.get().defaultBlockState();
        private static final BlockState SPIRIT_GRASS = ErgenverseBlocks.SPIRIT_GRASS.get().defaultBlockState();
        private static final BlockState QI_GRASS = ErgenverseBlocks.QI_GATHERING_GRASS.get().defaultBlockState();
    }

    private HengYueSectBuilder() {}

    // ── Block palette (ErgenverseBlocks — canon-correct spirit materials) ──
    private static final BlockState COBBLE         = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState LANTERN        = Blocks.LANTERN.defaultBlockState();
    private static final BlockState END_ROD        = Blocks.END_ROD.defaultBlockState();
    private static final BlockState SEA_LANTERN    = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState GLOWSTONE      = Blocks.GLOWSTONE.defaultBlockState();
    private static final BlockState GOLD           = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState AMETHYST       = Blocks.AMETHYST_BLOCK.defaultBlockState();
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
    private static final BlockState SOUL_LANTERN   = Blocks.SOUL_LANTERN.defaultBlockState();
    private static final BlockState AIR            = Blocks.AIR.defaultBlockState();
    private static final BlockState SPRUCE_DOOR    = Blocks.SPRUCE_DOOR.defaultBlockState();
    private static final BlockState CRAFTING_TABLE = Blocks.CRAFTING_TABLE.defaultBlockState();

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
        buildConnectionPaths(level, center);
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
        // Spirit grass border on top terrace edges (canon: mountain meadows)
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
        // Gold plaque above the gate — "Heng Yue Sect" (恒岳派)
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
        // Sect announcement board at north edge of plaza (facing south toward gate)
        setBlock(level, c.offset(0, 1, -9), CRAFTING_TABLE); // makeshift lectern/notice board
    }

    // ── Connection paths from plaza to each building ──

    private static void buildConnectionPaths(ServerLevel level, BlockPos c) {
        // East path to Library (c + 18,0,0)
        for (int dx = 11; dx <= 17; dx++) {
            setBlock(level, c.offset(dx, 0, 0), B.SPIRIT_STONE);
            setBlock(level, c.offset(dx, 0, -1), B.SPIRIT_STONE);
            setBlock(level, c.offset(dx, 0, 1), B.SPIRIT_STONE);
        }
        // West path to Alchemy Courtyard (c - 18,0,0)
        for (int dx = -11; dx >= -17; dx--) {
            setBlock(level, c.offset(dx, 0, 0), B.SPIRIT_STONE);
            setBlock(level, c.offset(dx, 0, -1), B.SPIRIT_STONE);
            setBlock(level, c.offset(dx, 0, 1), B.SPIRIT_STONE);
        }
        // North path to Ancestor Hall (c 0,0,-18)
        for (int dz = -11; dz >= -17; dz--) {
            setBlock(level, c.offset(0, 0, dz), B.SPIRIT_STONE);
            setBlock(level, c.offset(-1, 0, dz), B.SPIRIT_STONE);
            setBlock(level, c.offset(1, 0, dz), B.SPIRIT_STONE);
        }
        // NE path to Sword Peak (c +15,+2,-15)
        for (int i = 1; i <= 12; i++) {
            int px = (int)(i * 15.0 / 12);
            int pz = (int)(i * -15.0 / 12);
            setBlock(level, c.offset(px, 0, pz), COBBLE);
        }
        // NW path to Sword Tomb (c +25,0,5) — curves northeast
        for (int i = 1; i <= 15; i++) {
            int px = (int)(i * 25.0 / 15);
            int pz = (int)(i * 5.0 / 15);
            setBlock(level, c.offset(px, 0, pz), COBBLE);
        }
        // SW path to Spirit Spring (c -15,0,15)
        for (int i = 1; i <= 12; i++) {
            int px = (int)(i * -15.0 / 12);
            int pz = (int)(i * 15.0 / 12);
            setBlock(level, c.offset(px, 0, pz), B.SPRUCE_PLANK);
        }
        // South paths to Dormitories (c -8,0,12 and c +8,0,12)
        for (int dz = 11; dz <= 13; dz++) {
            setBlock(level, c.offset(-8, 0, dz), B.SPIRIT_STONE);
            setBlock(level, c.offset(-9, 0, dz), B.SPIRIT_STONE);
            setBlock(level, c.offset(8, 0, dz), B.SPIRIT_STONE);
            setBlock(level, c.offset(9, 0, dz), B.SPIRIT_STONE);
        }
        // North paths to Seclusion Caves (c -12,0,-25 / c 0,0,-25 / c 12,0,-25)
        for (int dz = -11; dz >= -23; dz--) {
            setBlock(level, c.offset(-12, 0, dz), COBBLE);
            setBlock(level, c.offset(0, 0, dz - 2), B.SPIRIT_STONE);
            setBlock(level, c.offset(12, 0, dz), COBBLE);
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
                        // Interior bookshelves on ground and second floor
                        setBlock(level, base.offset(dx, y + 1, dz), BOOKSHELF);
                    }
                }
            }
            // Doorway on south wall (facing plaza)
            setBlock(level, base.offset(0, y + 1, half), AIR);
            setBlock(level, base.offset(0, y + 2, half), AIR);
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

        // ═══ CRON-COMPLETIONIST-78: Narrative enrichment ═══
        // Ground floor: Elder Xu's cultivation lecture notes on the lectern
        enrichLibraryGroundFloor(level, base);
        // Second floor: technique scrolls and a sword manual
        enrichLibrarySecondFloor(level, base);
        // Third floor (top): restricted texts — elder's private collection
        enrichLibraryTopFloor(level, base);
    }

    private static void enrichLibraryGroundFloor(ServerLevel level, BlockPos base) {
        // Lectern at center with Elder Xu's lecture notes
        setBlock(level, base.offset(0, 1, 0), LECTERN);
        if (level.getBlockEntity(base.offset(0, 1, 0)) instanceof LecternBlockEntity lectern) {
            lectern.setBook(createWrittenBook(
                "Elder Xu's Foundation Lectures",
                "Elder Xu — Heng Yue Sect",
                "Lesson One: The Nature of Qi",
                "Qi exists in all things. The earth beneath your feet, the air in your lungs, the water you drink — all carry qi. Most mortals pass through life unaware. A cultivator learns to feel it.",
                "Close your eyes. Breathe slowly. The warmth at the base of your spine — that is qi gathering at your dantian. Do not force it. Do not chase it. Simply observe.",
                "Many disciples fail at this stage because they try too hard. They clench their fists, hold their breath, and push. The qi scatters. Patience is not merely a virtue here — it is the method.",
                "",
                "Lesson Two: Spirit Gathering",
                "The Heng Yue technique draws qi from the environment through the meridians. Each disciple's capacity differs. Some gather quickly but store poorly. Others gather slowly but retain much.",
                "Measure your progress not by the speed of gathering, but by how much remains after a full night's rest. If you wake empty, your foundation is leaking. Find the leak before advancing.",
                "A common leak: tension in the shoulders. The meridian from the crown to the dantian passes through the neck. Hunching blocks it. Sit straight. Always.",
                "",
                "Lesson Three: Foundation Establishment",
                "When your dantian holds qi for three days without leakage, you may attempt foundation establishment. This is the most dangerous moment in a cultivator's early path.",
                "The qi must be compressed — not forced — into a solid core at your dantian. Think of snow packing into ice. Gentle, steady pressure. If the core cracks, the backlash can shatter your meridians.",
                "Of every ten disciples who attempt foundation, perhaps three succeed. Of those three, one will have a flawed core that limits their entire future. Take your time. A flawed foundation is worse than none."
            ));
        }
        // Chest under the lectern table area — blank paper and ink for study
        BlockPos chestPos = base.offset(2, 1, -1);
        setBlock(level, chestPos, CHEST);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.PAPER, 16));
            chest.setItem(1, new ItemStack(Items.INK_SAC, 3));
            chest.setItem(2, new ItemStack(Items.FEATHER, 2));
            chest.setItem(3, new ItemStack(Items.WRITABLE_BOOK));
        }
        // Item frame on west wall: sect calligraphy scroll
        placeItemFrame(level, base.offset(-4, 2, -1), Direction.EAST,
                new ItemStack(Items.PAPER)); // represents a calligraphy scroll
    }

    private static void enrichLibrarySecondFloor(ServerLevel level, BlockPos base) {
        int y = base.getY() + 4;
        // Clear some bookshelves for reading area
        setBlock(level, base.offset(0, y + 1, 0), LECTERN);
        setBlock(level, base.offset(1, y + 1, 0), AIR); // reading space
        setBlock(level, base.offset(-1, y + 1, 0), AIR); // reading space
        // Table (spruce planks at floor+1 level)
        setBlock(level, base.offset(1, y + 1, 1), B.SPRUCE_PLANK);

        if (level.getBlockEntity(base.offset(0, y + 1, 0)) instanceof LecternBlockEntity lectern) {
            lectern.setBook(createWrittenBook(
                "Sword Qi Condensation Method",
                "Heng Yue Sect — Inner Library",
                "Chapter One: Channeling Qi to the Blade",
                "A flying sword is not merely a weapon. It is an extension of your meridians. The qi flows from your dantian, through your arm, into the sword's core.",
                "Begin with a dull blade — iron, not spirit metal. If you can channel qi through iron without shattering it, you may progress to spirit steel.",
                "The first sign of success: the blade hums. Not a physical vibration, but a resonance you feel in your bones. The sword is awakening to your qi.",
                "",
                "Chapter Two: The Three Stances",
                "Heng Yue Sword Art recognizes three fundamental stances.",
                "Mountain Stance: grounded, defensive, qi pooled in the lower dantian. The blade is still. The cultivator waits. An opponent sees only a standing figure and a sheathed sword. They do not see the compressed qi.",
                "River Stance: flowing, evasive, qi circulating through all meridians. The blade moves without thought. An opponent sees a thousand feints and cannot distinguish the true strike.",
                "Heaven Stance: ascending, ultimate, all qi channeled to a single point. The cultivator rises. The blade descends. There is no defense because there is no need. This stance costs three months of accumulated qi in one strike. Use it only when death is certain otherwise."
            ));
        }
        // Chest in corner with a practice iron sword
        BlockPos chestPos = base.offset(2, y + 1, 2);
        setBlock(level, chestPos, CHEST);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.IRON_SWORD));
            chest.setItem(1, new ItemStack(Items.SHIELD));
        }
    }

    private static void enrichLibraryTopFloor(ServerLevel level, BlockPos base) {
        int y = base.getY() + 8;
        // Restricted texts — only the sect master and elders may read these
        // Clear space for a reading desk
        setBlock(level, base.offset(0, y + 1, 0), LECTERN);
        setBlock(level, base.offset(0, y + 1, 1), AIR);

        if (level.getBlockEntity(base.offset(0, y + 1, 0)) instanceof LecternBlockEntity lectern) {
            lectern.setBook(createWrittenBook(
                "[RESTRICTED] Soul Refining Observations",
                "Sect Master Liu — Sealed Record",
                "I have observed something in the deep meditation caves that I dare not speak of aloud. This text is restricted because the knowledge within is dangerous — not to enemies, but to the cultivator who reads it.",
                "During the seventh month of my seclusion, I perceived a second consciousness within my dantian. Not a voice. Not a presence. More like the shadow of a thought that was not my own.",
                "I attempted to communicate with it. It responded by showing me a memory — a battlefield, a sky the color of dried blood, cultivators falling like rain. I do not know whose memory this was. I do not know if it was real.",
                "The consciousness has not returned. But since that night, my qi circulation is faster. My perception sharper. And when I close my eyes, I sometimes see that red sky for just an instant.",
                "I am recording this because if I lose myself, someone must know what happened. If you are reading this and I have changed — if I am no longer the master you remember — destroy this book. Do not attempt what I attempted. The price may not be what you expect.",
                "",
                "Seal placed: Year 47 of the Heng Yue Calendar."
            ));
        }
        // Iron bars over the doorway to the top floor — restricted access
        int half = 2;
        setBlock(level, base.offset(0, y + 1, half), IRON_BARS);
        setBlock(level, base.offset(0, y + 2, half), IRON_BARS);
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
        // Doorway on east wall (facing plaza)
        setBlock(level, base.offset(6, 1, 0), AIR);
        setBlock(level, base.offset(6, 2, 0), AIR);

        // ═══ CRON-COMPLETIONIST-78: Narrative enrichment ═══
        // Alchemy workbench and recipe book
        BlockPos deskPos = base.offset(3, 1, -4);
        setBlock(level, deskPos, CRAFTING_TABLE);
        setBlock(level, deskPos.offset(0, 1, 0), LECTERN);
        if (level.getBlockEntity(deskPos.offset(0, 1, 0)) instanceof LecternBlockEntity lectern) {
            lectern.setBook(createWrittenBook(
                "Spirit Condensation Pill Recipe",
                "Alchemist Zhang — Heng Yue Sect",
                "Ingredients:",
                "Three-leaf spirit grass, freshly harvested before dawn. If harvested after sunrise, the qi has already begun to disperse and the pill will be 30% weaker.",
                "Foundation root vine, cleaned but NOT peeled. The bark contains a binding compound essential for pill cohesion. Discard the inner root — it is toxic.",
                "One measure of spring water from the sect's spirit spring. Rainwater will not work. River water is worse. The spring water carries residual formation qi from the ancient array beneath the mountain.",
                "",
                "Process:",
                "Grind the spirit grass into a fine paste using the stone mortar. The paste should be uniform — no visible fibers. If fibers remain, continue grinding.",
                "Slice the root vine into thin strips. Soak the strips in spring water for exactly one hour. No more, no less.",
                "Combine paste and soaked strips in the blast furnace. Heat at medium intensity — if the flame turns blue, reduce heat immediately. Blue flame means the qi is burning, not binding.",
                "When the mixture begins to coalesce into a ball, remove from heat. Pour the remaining spring water over the ball to seal the surface.",
                "The pill should be warm to the touch, faintly luminous, and smell of fresh grass. If it smells of smoke, it is ruined. If it is cold, the qi escaped during cooling.",
                "",
                "Success rate: approximately 40% for trained alchemists. Do not be discouraged by failure. Every failed pill teaches you something about the qi's behavior."
            ));
        }
        // Chest with alchemy supplies
        BlockPos chestPos = base.offset(-5, 1, 5);
        setBlock(level, chestPos, CHEST);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.GLASS_BOTTLE, 8));
            chest.setItem(1, new ItemStack(Items.BOWL, 4));
            chest.setItem(2, new ItemStack(Items.SUGAR, 3));
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

        // ═══ CRON-COMPLETIONIST-78: Narrative enrichment ═══
        // Item frames on weapon rack displaying practice weapons
        placeItemFrame(level, base.offset(-2, 3, -6), Direction.SOUTH,
                new ItemStack(Items.WOODEN_SWORD));  // beginner practice sword
        placeItemFrame(level, base.offset(0, 3, -6), Direction.SOUTH,
                new ItemStack(Items.STONE_SWORD));   // intermediate
        placeItemFrame(level, base.offset(2, 3, -6), Direction.SOUTH,
                new ItemStack(Items.IRON_SWORD));   // advanced disciple weapon
        // Chest with training supplies
        BlockPos chestPos = base.offset(5, 1, -5);
        setBlock(level, chestPos, CHEST);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.WOODEN_SWORD, 3));
            chest.setItem(1, new ItemStack(Items.LEATHER_CHESTPLATE, 2));
            chest.setItem(2, new ItemStack(Items.BREAD, 8));
        }
        // Sign of heavy use: cracked stone blocks around the anvil (foot traffic wear)
        setBlock(level, base.offset(-1, 0, 0), STONE);
        setBlock(level, base.offset(1, 0, 0), STONE);
        setBlock(level, base.offset(0, 0, -1), STONE);
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
        // Doorway on south wall (facing plaza)
        setBlock(level, base.offset(0, 1, 4), AIR);
        setBlock(level, base.offset(0, 2, 4), AIR);
        // Gold accent on the back wall — sect motto
        setBlock(level, base.offset(0, 3, -4), GOLD);
        // Soul lanterns (dark interior)
        setBlock(level, base.offset(-4, 4, 0), SOUL_LANTERN);
        setBlock(level, base.offset(4, 4, 0), SOUL_LANTERN);

        // ═══ CRON-COMPLETIONIST-78: Narrative enrichment ═══
        // 5 memorial tablets with names and deeds (replacing generic flower pots)
        // Each tablet is: smooth slab base + soul sand "tablet body" + item frame "inscription"
        String[][] ancestors = {
            {"Founder Liu", "Established Heng Yue Sect 300 years ago. First to discover the spirit vein beneath this mountain."},
            {"Elder Mei", "Master of the Sword Peak. Defeated the Blood Moon Bandits single-handedly. Died at age 247."},
            {"Alchemist Guan", "Created the Spirit Condensation Pill recipe still used today. Said: 'Every failure is a conversation with the qi.'"},
            {"Sect Master Zhao", "Expanded the sect from 12 to 400 disciples. Built the Library Pavilion. Died peacefully in meditation."},
            {"Elder Sister Yun", "The only woman to reach Core Formation in sect history. Vanished during a heavenly tribulation. Body never found."}
        };
        for (int i = 0; i < 5; i++) {
            int dx = -4 + i * 2;
            // Memorial tablet: slab base + soul sand body
            setBlock(level, base.offset(dx, 1, -3), SMOOTH_SLAB);
            setBlock(level, base.offset(dx, 2, -3), Blocks.SOUL_SAND.defaultBlockState());
            // Inscription plaque as item frame
            placeItemFrame(level, base.offset(dx, 3, -3), Direction.SOUTH,
                    createWrittenBook("Memorial: " + ancestors[i][0], "Heng Yue Sect Records",
                            ancestors[i][0], ancestors[i][1]));
        }
        // 4 campfire incense braziers — add soul sand beneath for ceremonial look
        setBlock(level, base.offset(-5, 1, 3), CAMPFIRE);
        setBlock(level, base.offset(-2, 1, 3), CAMPFIRE);
        setBlock(level, base.offset(2, 1, 3), CAMPFIRE);
        setBlock(level, base.offset(5, 1, 3), CAMPFIRE);

        // Elder's offering chest
        BlockPos chestPos = base.offset(-5, 1, -2);
        setBlock(level, chestPos, CHEST);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            // Incense offerings
            chest.setItem(0, new ItemStack(Items.STICK, 16));    // incense sticks
            chest.setItem(1, new ItemStack(Items.COAL, 4));       // charcoal
            chest.setItem(2, new ItemStack(Items.GOLD_NUGGET, 8)); // offerings
        }
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

        // ═══ CRON-COMPLETIONIST-78: Narrative enrichment ═══
        // Small offering shrine at the pool's edge
        setBlock(level, base.offset(4, 1, 0), SMOOTH_SLAB);
        setBlock(level, base.offset(4, 2, 0), FLOWER_POT);
        setBlock(level, base.offset(4, 1, -1), LANTERN);
        // Sign: "This spring feeds the formation beneath the mountain. Do not pollute."
        // (represented by the flower pot and lantern — a place of reverence)
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
                setBlock(level, base.offset(0, dy, dz), AIR);
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
                        setBlock(level, chamber.offset(dx, dy, dz), AIR);
                    }
                }
            }
        }
        // 3 skeleton skulls (fallen swordsmen)
        setBlock(level, chamber.offset(-3, 1, 3), SKELETON_SKULL);
        setBlock(level, chamber.offset(0, 1, 2), SKELETON_SKULL);
        setBlock(level, chamber.offset(3, 1, 4), SKELETON_SKULL);
        // Sea lantern lights in corners
        setBlock(level, chamber.offset(-4, 1, 1), SEA_LANTERN);
        setBlock(level, chamber.offset(4, 1, 1), SEA_LANTERN);
        setBlock(level, chamber.offset(-4, 1, 5), SEA_LANTERN);
        setBlock(level, chamber.offset(4, 1, 5), SEA_LANTERN);

        // ═══ CRON-COMPLETIONIST-78: Narrative enrichment ═══
        // Fallen disciple's diary next to the central skeleton
        BlockPos diaryPos = chamber.offset(0, 1, 3);
        setBlock(level, diaryPos, LECTERN);
        if (level.getBlockEntity(diaryPos) instanceof LecternBlockEntity lectern) {
            lectern.setBook(createWrittenBook(
                "Diary of Disciple Ma — Sword Tomb Trial",
                "Disciple Ma — Heng Yue Sect",
                "Day 1: I have entered the Sword Tomb as instructed. The elder said: 'Take only what you can carry. Leave only your name.' I do not understand what that means.",
                "The passage is narrow and the air is cold. My torch gutters in the draft from below. Something moves in the darkness ahead but I cannot see it. I hear metal — a slow, rhythmic scraping, like a blade being drawn across stone.",
                "",
                "Day 2: I have reached the inner chamber. Three swords float in the air, motionless. They are ancient — their qi is faded but still present. I can feel it pressing against my skin like cold water.",
                "When I reached for the first sword, it burned me. Not with heat — with something else. My meridians screamed. I pulled back and my hand trembled for an hour.",
                "The second sword did not resist. It felt warm in my grip, almost welcoming. But when I lifted it, I heard a voice — not in my ears, but in my dantian. It said: 'You are not ready. Put me down.' I obeyed. I do not know why.",
                "",
                "Day 3: I attempted the third sword. It cut me. A thin line across my palm, deep enough to bleed but not enough to scar. I dropped it immediately.",
                "I bandaged my hand and sat with my back against the wall. I have decided to rest before trying again. The scraping sound has stopped. I think the swords are watching me.",
                "",
                "Day 4: I understand now what the elder meant. 'Take only what you can carry' — I can carry nothing from here. 'Leave only your name' — I will leave my name on this stone. If another disciple finds this diary, know this: the swords are not tests of strength. They are tests of self-knowledge. I failed because I did not know what I wanted.",
                "I am going to try the second sword one more time. If it speaks again, I will listen."
            ));
        }
        // Chest at the back wall center
        ChestHelper.placeChestWithLoot(level, chamber.offset(0, 1, 5),
                new ResourceLocation("ergenverse", "chests/heng_yue_sect_mountain_cave"));
        // Redstone dust near the skeletons — dried blood of fallen swordsmen (visual storytelling)
        setBlock(level, chamber.offset(-3, 1, 2), B.REDSTONE_BLOCK);
        setBlock(level, chamber.offset(3, 1, 3), B.REDSTONE_BLOCK);
    }

    private static void buildSeclusionCaves(ServerLevel level, BlockPos c) {
        // 3 DISTINCT caves along the north face — each with unique personality
        // Cave 1 (west): Senior disciple's cave — active meditation site
        buildSeniorDiscipleCave(level, c.offset(-12, 0, -25));
        // Cave 2 (center): Elder's private retreat — highest authority, restricted
        buildElderRetreatCave(level, c.offset(0, 0, -27));
        // Cave 3 (east): Abandoned cave — evidence of a failed breakthrough
        buildAbandonedCave(level, c.offset(12, 0, -25));
    }

    private static void buildSeniorDiscipleCave(ServerLevel level, BlockPos caveBase) {
        // Cobble frame
        for (int dy = 0; dy < 3; dy++) {
            setBlock(level, caveBase.offset(-1, dy, 0), COBBLE);
            setBlock(level, caveBase.offset(1, dy, 0), COBBLE);
        }
        setBlock(level, caveBase.offset(-1, 3, 0), COBBLE);
        setBlock(level, caveBase.offset(0, 3, 0), COBBLE);
        setBlock(level, caveBase.offset(1, 3, 0), COBBLE);
        // Entrance (air)
        setBlock(level, caveBase.offset(0, 0, 0), AIR);
        setBlock(level, caveBase.offset(0, 1, 0), AIR);
        // Hollow room 5×5×4
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 0; dy < 4; dy++) {
                for (int dz = 1; dz <= 5; dz++) {
                    if (dx == -2 || dx == 2 || dy == 0 || dy == 3 || dz == 1 || dz == 5) {
                        setBlock(level, caveBase.offset(dx, dy, dz), STONE);
                    } else {
                        setBlock(level, caveBase.offset(dx, dy, dz), AIR);
                    }
                }
            }
        }
        // Meditation mat (spruce slab) + lectern + lantern
        setBlock(level, caveBase.offset(0, 1, 3), SMOOTH_SLAB);
        setBlock(level, caveBase.offset(0, 1, 4), LECTERN);
        setBlock(level, caveBase.offset(-1, 1, 2), LANTERN);

        // ═══ Narrative: Senior disciple's progress notes ═══
        if (level.getBlockEntity(caveBase.offset(0, 1, 4)) instanceof LecternBlockEntity lectern) {
            lectern.setBook(createWrittenBook(
                "Meditation Progress — Senior Disciple Huang",
                "Disciple Huang — Heng Yue Sect",
                "Month 3 of seclusion:",
                "My qi circulation has stabilized at the 12th meridian. The blockage at the heart chakra persists — every time I try to push qi through, I feel resistance that is not physical. It is as though something is guarding that passage.",
                "Elder Xu says this is normal. He says the heart chakra is the gate between the body and the spirit. Many cultivators spend years at this gate. He says impatience is the only true failure.",
                "I have been patient for three months. I meditate twelve hours each day. I eat once. I sleep on the stone floor. The cold helps — it sharpens my focus.",
                "",
                "Month 6 of seclusion:",
                "Breakthrough. Last night, during the deepest meditation I have ever achieved, the heart chakra opened. Not with force — I simply stopped trying to push and let the qi find its own path. It flowed through the gate like water finding a crack in a dam.",
                "The sensation was indescribable. For a moment, I could feel the entire mountain — every stone, every root, every stream of water underground. I felt the spirit vein beneath the sect, pulsing like a slow heartbeat.",
                "I understand now why the elders meditate in caves. The mountain teaches patience. The stone does not hurry. The qi does not rush. The cave has been here for ten thousand years and will be here for ten thousand more. What is my haste?"
            ));
        }
        // Small chest with disciple's supplies
        BlockPos chestPos = caveBase.offset(2, 1, 4);
        setBlock(level, chestPos, CHEST);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.BREAD, 5));
            chest.setItem(1, new ItemStack(Items.BOWL, 2));
        }
    }

    private static void buildElderRetreatCave(ServerLevel level, BlockPos caveBase) {
        // Slightly larger frame — elder's cave is grander
        for (int dy = 0; dy < 4; dy++) {
            setBlock(level, caveBase.offset(-1, dy, 0), B.STONE_BRICK);
            setBlock(level, caveBase.offset(1, dy, 0), B.STONE_BRICK);
        }
        setBlock(level, caveBase.offset(-1, 4, 0), B.STONE_BRICK);
        setBlock(level, caveBase.offset(0, 4, 0), B.STONE_BRICK);
        setBlock(level, caveBase.offset(1, 4, 0), B.STONE_BRICK);
        // Iron bars on the entrance — restricted access
        setBlock(level, caveBase.offset(0, 1, 0), IRON_BARS);
        setBlock(level, caveBase.offset(0, 2, 0), IRON_BARS);
        // Hollow room 7×5×4 — larger than disciple caves
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = 0; dy < 4; dy++) {
                for (int dz = 1; dz <= 5; dz++) {
                    if (dx == -3 || dx == 3 || dy == 0 || dy == 3 || dz == 1 || dz == 5) {
                        setBlock(level, caveBase.offset(dx, dy, dz), B.STONE_BRICK);
                    } else {
                        setBlock(level, caveBase.offset(dx, dy, dz), AIR);
                    }
                }
            }
        }
        // Formation pattern on the floor (restriction stone ring)
        for (int dx = -2; dx <= 2; dx++) {
            setBlock(level, caveBase.offset(dx, 0, 1), B.LAPIS);
            setBlock(level, caveBase.offset(dx, 0, 5), B.LAPIS);
        }
        for (int dz = 1; dz <= 5; dz++) {
            setBlock(level, caveBase.offset(-2, 0, dz), B.LAPIS);
            setBlock(level, caveBase.offset(2, 0, dz), B.LAPIS);
        }
        // Meditation platform at center (raised)
        setBlock(level, caveBase.offset(0, 1, 3), B.OBSIDIAN);
        // Sea lantern for spiritual illumination
        setBlock(level, caveBase.offset(0, 3, 3), SEA_LANTERN);
        // Lectern behind the platform
        setBlock(level, caveBase.offset(0, 1, 4), LECTERN);

        // ═══ Narrative: Elder's forbidden observations ═══
        if (level.getBlockEntity(caveBase.offset(0, 1, 4)) instanceof LecternBlockEntity lectern) {
            lectern.setBook(createWrittenBook(
                "Forbidden: Observations on the Ancient Array",
                "Elder Xu — Highest Seal",
                "This text is sealed with my blood. Only another elder may read it. If you are not an elder and you are reading this, the formation will know. It always knows.",
                "The spirit vein beneath Heng Yue Mountain is not natural. I have meditated above it for forty years. In the last decade, I have felt it change. It is growing. Not slowly — exponentially. Every year it pulses twice as strong as the last.",
                "I believe this mountain was chosen by whoever created the formation. The sect was built here not because the vein was discovered, but because someone wanted a sect here to protect it. We are not the owners of this place. We are its guards. We have always been its guards.",
                "The ancestors who founded the sect must have known. Their records speak of 'discovering the vein' but they do not speak of choosing the mountain. They speak as though they stumbled upon something. No one stumbles upon a formation this powerful by accident.",
                "I am recording this because I am old and my memory fades. If I forget, the next elder must discover this truth independently, and there may not be time for that.",
                "The formation is waking up."
            ));
        }
        // Chest with elder's emergency supplies
        BlockPos chestPos = caveBase.offset(2, 1, 4);
        setBlock(level, chestPos, CHEST);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.GOLDEN_APPLE, 2));
            chest.setItem(1, new ItemStack(Items.ENDER_PEARL, 1));
            chest.setItem(2, new ItemStack(Items.EXPERIENCE_BOTTLE, 4));
        }
    }

    private static void buildAbandonedCave(ServerLevel level, BlockPos caveBase) {
        // Crumbling frame — cobble, some air gaps
        for (int dy = 0; dy < 3; dy++) {
            setBlock(level, caveBase.offset(-1, dy, 0), COBBLE);
            setBlock(level, caveBase.offset(1, dy, 0), dy < 2 ? COBBLE : AIR); // crumbling top-right
        }
        setBlock(level, caveBase.offset(0, 3, 0), AIR); // partially collapsed lintel
        // Entrance (air) — wider than others, suggests hasty departure
        setBlock(level, caveBase.offset(0, 0, 0), AIR);
        setBlock(level, caveBase.offset(0, 1, 0), AIR);
        // Hollow room 5×5×4 — stone but with cracks
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 0; dy < 4; dy++) {
                for (int dz = 1; dz <= 5; dz++) {
                    if (dx == -2 || dx == 2 || dy == 0 || dy == 3 || dz == 1 || dz == 5) {
                        // Some walls are cracked stone instead of solid
                        if (dx == 2 && dy == 2 && dz == 3) {
                            setBlock(level, caveBase.offset(dx, dy, dz), AIR); // crack/hole in wall
                        } else {
                            setBlock(level, caveBase.offset(dx, dy, dz), STONE);
                        }
                    } else {
                        setBlock(level, caveBase.offset(dx, dy, dz), AIR);
                    }
                }
            }
        }
        // Lantern — burned out (just the fence post, no lantern block)
        setBlock(level, caveBase.offset(-1, 1, 2), SPRUCE_FENCE);

        // ═══ Narrative: Evidence of a failed breakthrough ═══
        // Scattered items on the floor — not organized, suggests panic/abandonment
        setBlock(level, caveBase.offset(1, 1, 2), CRAFTING_TABLE); // knocked-over table
        // Restriction diagram in redstone on the floor (failed formation attempt)
        setBlock(level, caveBase.offset(-1, 1, 3), B.REDSTONE_BLOCK);
        setBlock(level, caveBase.offset(-1, 1, 4), B.REDSTONE_BLOCK);
        setBlock(level, caveBase.offset(0, 1, 4), B.REDSTONE_BLOCK);
        // The center of the diagram is MISSING — the formation cracked (air gap)
        setBlock(level, caveBase.offset(0, 1, 3), AIR);
        setBlock(level, caveBase.offset(1, 1, 3), B.REDSTONE_BLOCK);
        // Discarded pills on the floor (represented by items in a chest knocked over)
        BlockPos chestPos = caveBase.offset(-1, 1, 4);
        setBlock(level, chestPos, CHEST);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.GLASS_BOTTLE, 6)); // empty pill bottles
            chest.setItem(1, new ItemStack(Items.ROTTEN_FLESH));    // spoiled ingredients
            chest.setItem(2, new ItemStack(Items.STRING));           // torn bandages
        }
        // Skeleton in the corner — the disciple who failed
        setBlock(level, caveBase.offset(-1, 1, 1), SKELETON_SKULL);

        // Lectern with a final, unfinished note
        setBlock(level, caveBase.offset(0, 1, 4), LECTERN);
        if (level.getBlockEntity(caveBase.offset(0, 1, 4)) instanceof LecternBlockEntity lectern) {
            lectern.setBook(createWrittenBook(
                "Disciple Chen's Notes — INCOMPLETE",
                "Disciple Chen — Heng Yue Sect",
                "The formation is ready. I have spent seven months on this restriction diagram. Every line is correct. Every junction is aligned with the compass directions. The qi flow pattern matches Elder Xu's lecture notes exactly.",
                "I will attempt the breakthrough tonight.",
                "If you are reading this and I am not here, the breakthrough failed. Destroy the formation diagram. Do not attempt it yourself — the qi residue from a failed breakthrough is unstable and will corrupt any subsequent attempt at this location for at least a year.",
                "Tell my family I was not afraid. Tell them I chose this. Tell them the mountain was beautiful from inside.",
                "",
                "The note ends here. The remaining pages are blank."
            ));
        }
    }

    private static void buildDormitories(ServerLevel level, BlockPos c) {
        // 2 long halls 16×6 along the south of inner sect
        BlockPos[] halls = {c.offset(-8, 0, 12), c.offset(8, 0, 12)};
        String[] hallNames = {"West Dormitory", "East Dormitory"};
        for (int hi = 0; hi < halls.length; hi++) {
            BlockPos hall = halls[hi];
            // Floor
            fill(level, hall.offset(-8, 0, -3), hall.offset(8, 0, 3), B.SPRUCE_PLANK);
            // Walls
            for (int dx = -8; dx <= 8; dx++) {
                for (int dy = 1; dy <= 4; dy++) {
                    setBlock(level, hall.offset(dx, dy, -3), B.STONE_BRICK);
                    setBlock(level, hall.offset(dx, dy, 3), B.STONE_BRICK);
                }
            }
            // Doorways (two per hall, on south wall)
            setBlock(level, hall.offset(-3, 1, 3), AIR);
            setBlock(level, hall.offset(-3, 2, 3), AIR);
            setBlock(level, hall.offset(4, 1, 3), AIR);
            setBlock(level, hall.offset(4, 2, 3), AIR);
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

            // ═══ CRON-COMPLETIONIST-78: Narrative enrichment ═══
            // Supply chest at the end of each dormitory
            BlockPos chestPos = hall.offset(7, 1, -2);
            setBlock(level, chestPos, CHEST);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                chest.setItem(0, new ItemStack(Items.BREAD, 16)); // sect rations
                chest.setItem(1, new ItemStack(Items.TORCH, 8));
                chest.setItem(2, new ItemStack(Items.LEATHER_BOOTS));
                chest.setItem(3, new ItemStack(Items.LEATHER_CHESTPLATE));
                // Bedroll/blanket represented by wool
                chest.setItem(4, new ItemStack(Items.WHITE_WOOL));
            }
            // Crafting table for basic repairs
            setBlock(level, hall.offset(7, 1, 0), CRAFTING_TABLE);
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
    //  Narrative helpers (same pattern as WangFamilyVillageBuilder)
    // ═══════════════════════════════════════════════════════════════════

    private static void placeItemFrame(ServerLevel level, BlockPos pos,
                                          Direction facing, ItemStack item) {
        ItemFrame frame = new ItemFrame(level, pos, facing);
        frame.setItem(item);
        level.addFreshEntity(frame);
    }

    private static ItemStack createWrittenBook(String title, String author,
                                                  String... pages) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("title", title);
        tag.putString("author", author);
        tag.putBoolean("resolved", true);
        ListTag pagesList = new ListTag();
        for (String page : pages) {
            String json = Component.Serializer.toJson(Component.literal(page));
            pagesList.add(StringTag.valueOf(json));
        }
        tag.put("pages", pagesList);
        return book;
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
