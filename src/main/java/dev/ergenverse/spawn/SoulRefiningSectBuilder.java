package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

/**
 * SoulRefiningSectBuilder — a FULLY hand-built Soul Refining Sect (炼魂宗 /
 * Lian Hun Zong) from Renegade Immortal (仙逆). Every block is placed
 * intentionally in Java — NOT a block-swap script, NOT a placeholder marker.
 *
 * <p>Constitution: "The world is completely hand-crafted, accurate to the novels.
 * NEVER write a script that replaces vanilla blocks with other blocks as a shortcut.
 * Every structure must be hand-authored."
 *
 * <p>Canon basis (Renegade Immortal): The Soul Refining Sect is a DARK, SINISTER sect
 * that specializes in refining souls into cultivation resources. Located deep in a misty,
 * dark valley surrounded by dead trees, its atmosphere is oppressive — heavy black/dark
 * grey stone with purple-black accents, narrow corridors, and underground chambers.
 * The central structure is the Soul Refining Furnace Hall, a massive chamber housing
 * the sect's prize artifact. The sect radiates menace: soul lanterns line every corridor,
 * ghost-blue fires flicker in braziers, and bone-white skull decorations serve as grim
 * reminders of the souls consumed within.
 *
 * <p>Block palette uses ErgenverseBlocks — all primary structure materials are
 * canon-correct spirit stone, spirit wood, ancient spirit log, formation core stone,
 * restriction stone, scorched stone, and blood stone. Path/functional blocks (cobblestone,
 * iron bars, lanterns, netherrack, etc.) remain vanilla where no custom equivalent exists.
 * The Soul Refining Sect's palette emphasizes darkness: scorched stone (dark stone),
 * restriction stone (obsidian), blood stone (red accents), and vanilla blackstone,
 * nether bricks, soul lanterns, and bone blocks for its gothic, tomb-like aesthetic.
 *
 * <p>Districts (13 total):
 * valley_base — dark valley floor with dead trees, netherrack, and scattered soul lanterns;
 * outer_gate — imposing blackstone gate with soul lanterns, iron bar portcullis, skull decorations;
 * main_plaza — obsidian-bordered plaza with soul pillars and central soul fire;
 * furnace_hall — THE central structure, a 20×15×20 chamber with blast furnace centerpiece;
 * library — forbidden technique storage behind iron bars with skull guards;
 * alchemy_courtyard — soul refinement area with cauldrons, netherrack, blast furnaces;
 * ancestor_hall — dark worship hall with bone altars and skull arrangements;
 * sword_peak — weapons of darkness display with iron bar racks;
 * spirit_beast_pens — cages of iron bars and dark oak fences for captured beasts;
 * dormitories — spartan dark cells with dark oak beds and minimal furnishings;
 * seclusion_caves — meditation cells carved into valley walls with soul lanterns;
 * underground_passage — secret tunnel of nether brick with hidden chests;
 * trial_grounds — dangerous testing area with netherrack and lava hazards.
 *
 * <h2>Harsh Self-Critique</h2>
 * <ul>
 *   <li>The "valley" is a flat depression — there is no actual terrain carving or
 *       cliff face generation. Dead trees are just stripped spruce logs sticking out
 *       of a flat dark stone floor. A real valley would require terrain modification
 *       or a custom biome, neither of which this builder does.</li>
 *   <li>Every interior is a box. The furnace hall is a 20×15×20 hollow cube with
 *       some pillars and a blast furnace — not the dramatic cathedral vault described
 *       in the novel. No arches, no vaulted ceilings, no apse. Reads as a warehouse,
 *       not a sacred furnace chamber.</li>
 *   <li>Soul lantern placement is formulaic — same spacing, same height, every room.
 *       Real oppressive atmosphere would have irregular pools of dim blue light,
 *       flickering soul campfires at odd heights, darkness in corners. This is
 *       evenly-lit-by-convention.</li>
 *   <li>The underground passage is a straight tunnel with nether brick walls. No
 *       branching paths, no dead ends, no collapsed sections, no hidden rooms behind
 *       breakable walls. A secret passage should feel labyrinthine.</li>
 *   <li>Trial grounds use single lava blocks for "hazards" — trivially avoidable.
 *       In canon, trial grounds are lethal spirit formations that kill most who enter.
 *       Single-block lava pools read like a mild nether parkour course.</li>
 *   <li>All districts use the same flat-floor-then-walls pattern. No stairs connecting
 *       districts at different elevations. No underground rooms beneath buildings.
 *       The entire sect sits on one plane, when canon describes multi-level
 *       underground chambers and ascending peak structures.</li>
 *   <li>BONE_BLOCK altars in the ancestor hall are just flat bone blocks with skulls
 *       on top — no elaborate ancestor worship setup with incense, tablets, offerings.
 *       Reads as "bone block + skull = done."</li>
 *   <li>Chest loot tables are wired via ChestHelper, but the loot table contents
 *       themselves are not defined here. If those tables are empty or generic, the
 *       entire reward structure of exploring the sect collapses.</li>
 *   <li>NO custom mob spawners. A soul-refining sect should have soul-guardian mobs,
 *       wraiths, or at minimum zombie/skeleton spawners in key chambers. This is
 *       an empty, lifeless shell of a sect — all architecture, no inhabitants.</li>
 * </ul>
 */
public final class SoulRefiningSectBuilder {

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
        private static final BlockState BRICK_WALL = ErgenverseBlocks.SPIRIT_STONE_WALL.get().defaultBlockState();
        private static final BlockState DEEPSLATE = ErgenverseBlocks.SCORCHED_STONE.get().defaultBlockState();
        private static final BlockState DEEPSLATE_BRICK = ErgenverseBlocks.SCORCHED_STONE.get().defaultBlockState();
        private static final BlockState SPRUCE_PLANK = ErgenverseBlocks.SPIRIT_WOOD_PLANKS.get().defaultBlockState();
        private static final BlockState DARK_OAK_PLANK = ErgenverseBlocks.SPIRIT_WOOD_PLANKS.get().defaultBlockState();
        private static final BlockState DARK_OAK_LOG = ErgenverseBlocks.ANCIENT_SPIRIT_LOG.get().defaultBlockState();
        private static final BlockState DARK_OAK_STAIR = ErgenverseBlocks.ANCIENT_SPIRIT_STAIRS.get().defaultBlockState();
        private static final BlockState SPRUCE_STAIR = ErgenverseBlocks.SPIRIT_WOOD_PLANKS_STAIRS.get().defaultBlockState();
        private static final BlockState BRICK_STAIR = ErgenverseBlocks.SPIRIT_STONE_STAIRS.get().defaultBlockState();
        private static final BlockState OBSIDIAN = ErgenverseBlocks.RESTRICTION_STONE.get().defaultBlockState();
        private static final BlockState REDSTONE_BLOCK = ErgenverseBlocks.BLOOD_STONE.get().defaultBlockState();
        private static final BlockState LAPIS = ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState();
    }

    private SoulRefiningSectBuilder() {}

    // ── Block palette (ErgenverseBlocks — canon-correct spirit materials) ──
    private static final BlockState COBBLE          = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState LANTERN         = Blocks.LANTERN.defaultBlockState();
    private static final BlockState SOUL_LANTERN    = Blocks.SOUL_LANTERN.defaultBlockState();
    private static final BlockState SEA_LANTERN     = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState IRON_BARS       = Blocks.IRON_BARS.defaultBlockState();
    private static final BlockState WATER           = Blocks.WATER.defaultBlockState();
    private static final BlockState BOOKSHELF       = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState CHEST           = Blocks.CHEST.defaultBlockState();
    private static final BlockState SKELETON_SKULL  = Blocks.SKELETON_SKULL.defaultBlockState();
    private static final BlockState BONE_BLOCK      = Blocks.BONE_BLOCK.defaultBlockState();
    private static final BlockState GLOWSTONE       = Blocks.GLOWSTONE.defaultBlockState();
    private static final BlockState NETHERRACK      = Blocks.NETHERRACK.defaultBlockState();
    private static final BlockState NETHER_BRICK    = Blocks.NETHER_BRICKS.defaultBlockState();
    private static final BlockState BLACKSTONE      = Blocks.BLACKSTONE.defaultBlockState();
    private static final BlockState CAULDRON        = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState BLAST_FURNACE   = Blocks.BLAST_FURNACE.defaultBlockState();
    private static final BlockState CAMPFIRE        = Blocks.SOUL_CAMPFIRE.defaultBlockState();
    private static final BlockState FERN            = Blocks.DEAD_BUSH.defaultBlockState();
    private static final BlockState DARK_OAK_DOOR   = Blocks.DARK_OAK_DOOR.defaultBlockState();
    private static final BlockState SPRUCE_FENCE    = Blocks.SPRUCE_FENCE.defaultBlockState();
    private static final BlockState SPRUCE_LOG      = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState DARK_BED        = Blocks.BLACK_BED.defaultBlockState();
    private static final BlockState LAVA            = Blocks.LAVA.defaultBlockState();
    private static final BlockState AIR             = Blocks.AIR.defaultBlockState();

    // ── Loot table resource locations ──
    private static final ResourceLocation LOOT_OUTER_GATE =
            new ResourceLocation("ergenverse", "soul_refining_sect_outer_gate");
    private static final ResourceLocation LOOT_MAIN_PLAZA =
            new ResourceLocation("ergenverse", "soul_refining_sect_main_plaza");
    private static final ResourceLocation LOOT_LIBRARY =
            new ResourceLocation("ergenverse", "soul_refining_sect_library");
    private static final ResourceLocation LOOT_ALCHEMY =
            new ResourceLocation("ergenverse", "soul_refining_sect_alchemy_courtyard");
    private static final ResourceLocation LOOT_ANCESTOR_HALL =
            new ResourceLocation("ergenverse", "soul_refining_sect_ancestor_hall");
    private static final ResourceLocation LOOT_SWORD_PEAK =
            new ResourceLocation("ergenverse", "soul_refining_sect_sword_peak");
    private static final ResourceLocation LOOT_BEAST_PENS =
            new ResourceLocation("ergenverse", "soul_refining_sect_spirit_beast_pens");
    private static final ResourceLocation LOOT_DORMITORIES =
            new ResourceLocation("ergenverse", "soul_refining_sect_disciple_dormitories");
    private static final ResourceLocation LOOT_SECLUSION =
            new ResourceLocation("ergenverse", "soul_refining_sect_inner_sect");
    private static final ResourceLocation LOOT_UNDERGROUND =
            new ResourceLocation("ergenverse", "soul_refining_sect_underground_passage");
    private static final ResourceLocation LOOT_TRIAL =
            new ResourceLocation("ergenverse", "soul_refining_sect_trial_grounds");
    private static final ResourceLocation LOOT_FURNACE =
            new ResourceLocation("ergenverse", "soul_refining_furnace");

    // ═══════════════════════════════════════════════════════════════════
    //  Entry point
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Build the full Soul Refining Sect centered at (x, groundY, z).
     * @param level the server overworld
     * @param center the valley center block position (at ground level)
     */
    public static void build(ServerLevel level, BlockPos center) {
        if (isAlreadyBuilt(level, center)) return;
        dev.ergenverse.core.Ergenverse.LOGGER.info("[Ergenverse] Building Soul Refining Sect at {}", center);

        buildValleyBase(level, center);
        buildOuterGate(level, center);
        buildMainPlaza(level, center);
        buildFurnaceHall(level, center);
        buildLibrary(level, center);
        buildAlchemyCourtyard(level, center);
        buildAncestorHall(level, center);
        buildSwordPeak(level, center);
        buildSpiritBeastPens(level, center);
        buildDormitories(level, center);
        buildSeclusionCaves(level, center);
        buildUndergroundPassage(level, center);
        buildTrialGrounds(level, center);

        dev.ergenverse.core.Ergenverse.LOGGER.info("[Ergenverse] Soul Refining Sect construction complete.");
    }

    public static boolean isAlreadyBuilt(ServerLevel level, BlockPos center) {
        return level.getBlockState(center.above()).getBlock() == Blocks.BLACKSTONE;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  District builders
    // ═══════════════════════════════════════════════════════════════════

    // ── a. Valley Base ────────────────────────────────────────────────
    private static void buildValleyBase(ServerLevel level, BlockPos c) {
        // Dark valley floor: 60×60 scorched stone base, depressed 2 blocks into ground
        for (int dx = -30; dx <= 30; dx++) {
            for (int dz = -30; dz <= 30; dz++) {
                // Two-layer floor: deepslate under scorched stone
                setBlock(level, c.offset(dx, c.getY() - 2, dz), B.DEEPSLATE);
                setBlock(level, c.offset(dx, c.getY() - 1, dz), B.DEEPSLATE_BRICK);
            }
        }
        // Netherrack patches (scarlet earth — blood-soaked ground motif)
        int[][] patches = {
                {-20, -20}, {-15, -10}, {10, -25}, {25, -5}, {-8, 15},
                {20, 20}, {-25, 5}, {5, 25}, {0, -18}, {-18, -2},
                {15, 10}, {-10, 22}, {28, -15}, {-5, -28}
        };
        for (int[] p : patches) {
            // 3×3 netherrack patch
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    setBlock(level, c.offset(p[0] + dx, c.getY() - 1, p[1] + dz), NETHERRACK);
                }
            }
        }
        // Dead trees (stripped spruce logs — no leaves, leafless corpses)
        int[][] trees = {
                {-25, -25}, {-20, 0}, {-15, 20}, {0, -25}, {10, -22},
                {25, -20}, {25, 0}, {20, 25}, {-25, 18}, {-10, 28},
                {28, 12}, {-28, -12}, {5, -30}, {-5, 30}
        };
        for (int[] t : trees) {
            // Trunk: 5 blocks tall spruce log
            for (int dy = 0; dy < 5; dy++) {
                setBlock(level, c.offset(t[0], c.getY() + dy, t[1]), SPRUCE_LOG);
            }
            // Bare branch stubs on sides
            if (t[0] % 2 == 0) {
                setBlock(level, c.offset(t[0] + 1, c.getY() + 3, t[1]), SPRUCE_LOG);
                setBlock(level, c.offset(t[0] - 1, c.getY() + 4, t[1]), SPRUCE_LOG);
            } else {
                setBlock(level, c.offset(t[0], c.getY() + 3, t[1] + 1), SPRUCE_LOG);
                setBlock(level, c.offset(t[0], c.getY() + 4, t[1] - 1), SPRUCE_LOG);
            }
        }
        // Scattered soul lanterns on the ground — ghost-fire glow in the mist
        int[][] lanterns = {
                {-18, -18}, {-10, -12}, {0, 0}, {12, -8}, {20, 5},
                {15, 18}, {-5, 22}, {-22, 10}, {8, -20}, {-15, 0},
                {25, -10}, {-8, -25}
        };
        for (int[] l : lanterns) {
            setBlock(level, c.offset(l[0], c.getY(), l[1]), SOUL_LANTERN);
        }
        // Dead bushes scattered on netherrack patches
        for (int[] p : patches) {
            setBlock(level, c.offset(p[0], c.getY(), p[1]), FERN);
        }
        // Blackstone border walls around the valley edge (low, 2 tall)
        for (int dx = -30; dx <= 30; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                setBlock(level, c.offset(dx, c.getY() + dy, -30), BLACKSTONE);
                setBlock(level, c.offset(dx, c.getY() + dy, 30), BLACKSTONE);
            }
        }
        for (int dz = -30; dz <= 30; dz++) {
            for (int dy = 0; dy <= 2; dy++) {
                setBlock(level, c.offset(-30, c.getY() + dy, dz), BLACKSTONE);
                setBlock(level, c.offset(30, c.getY() + dy, dz), BLACKSTONE);
            }
        }
        // Corner soul lantern sentries on the border
        int[][] corners = {{-29, -29}, {29, -29}, {-29, 29}, {29, 29}};
        for (int[] cr : corners) {
            setBlock(level, c.offset(cr[0], c.getY() + 3, cr[1]), SOUL_LANTERN);
        }
    }

    // ── b. Outer Gate ────────────────────────────────────────────────
    private static void buildOuterGate(ServerLevel level, BlockPos c) {
        int gx = c.getX(), gz = c.getZ() + 30, gy = c.getY();
        // Blackstone gate: two massive pillars 3×2×8
        for (int dy = 0; dy < 8; dy++) {
            for (int dx = 0; dx < 3; dx++) {
                setBlock(level, new BlockPos(gx - 6 + dx, gy + dy, gz), BLACKSTONE);
                setBlock(level, new BlockPos(gx + 4 + dx, gy + dy, gz), BLACKSTONE);
            }
        }
        // Blood stone trim at the base (2 blocks high on each pillar)
        for (int dy = 0; dy < 2; dy++) {
            setBlock(level, new BlockPos(gx - 6, gy + dy, gz), B.REDSTONE_BLOCK);
            setBlock(level, new BlockPos(gx + 6, gy + dy, gz), B.REDSTONE_BLOCK);
        }
        // Lintel: dark oak log beam across the top
        for (int dx = -7; dx <= 7; dx++) {
            setBlock(level, new BlockPos(gx + dx, gy + 8, gz), B.DARK_OAK_LOG);
        }
        // Second lintel layer for thickness
        for (int dx = -6; dx <= 6; dx++) {
            setBlock(level, new BlockPos(gx + dx, gy + 9, gz), B.DARK_OAK_LOG);
        }
        // Soul lanterns flanking the gate entrance on top of each pillar
        setBlock(level, new BlockPos(gx - 5, gy + 9, gz), SOUL_LANTERN);
        setBlock(level, new BlockPos(gx + 5, gy + 9, gz), SOUL_LANTERN);
        // Iron bar portcullis (the gate bars)
        for (int dx = -2; dx <= 2; dx++) {
            setBlock(level, new BlockPos(gx + dx, gy, gz), IRON_BARS);
            setBlock(level, new BlockPos(gx + dx, gy + 1, gz), IRON_BARS);
            setBlock(level, new BlockPos(gx + dx, gy + 2, gz), IRON_BARS);
            setBlock(level, new BlockPos(gx + dx, gy + 3, gz), IRON_BARS);
            setBlock(level, new BlockPos(gx + dx, gy + 4, gz), IRON_BARS);
            setBlock(level, new BlockPos(gx + dx, gy + 5, gz), IRON_BARS);
        }
        // Cross-bar iron bars at top and bottom
        for (int dz = -1; dz <= 1; dz++) {
            setBlock(level, new BlockPos(gx, gy, gz + dz), IRON_BARS);
            setBlock(level, new BlockPos(gx - 1, gy, gz + dz), IRON_BARS);
            setBlock(level, new BlockPos(gx + 1, gy, gz + dz), IRON_BARS);
            setBlock(level, new BlockPos(gx, gy + 5, gz + dz), IRON_BARS);
            setBlock(level, new BlockPos(gx - 1, gy + 5, gz + dz), IRON_BARS);
            setBlock(level, new BlockPos(gx + 1, gy + 5, gz + dz), IRON_BARS);
        }
        // Skull decorations on the gate pillars (facing inward)
        setBlock(level, new BlockPos(gx - 5, gy + 4, gz + 1), SKELETON_SKULL);
        setBlock(level, new BlockPos(gx + 5, gy + 4, gz + 1), SKELETON_SKULL);
        // Two more skulls lower, flanking the entrance
        setBlock(level, new BlockPos(gx - 3, gy + 3, gz), SKELETON_SKULL);
        setBlock(level, new BlockPos(gx + 3, gy + 3, gz), SKELETON_SKULL);
        // Steps leading up to gate (outside, south side)
        for (int step = 0; step < 3; step++) {
            for (int dx = -3; dx <= 3; dx++) {
                setBlock(level, new BlockPos(gx + dx, gy - step, gz + 1 + step), BLACKSTONE);
            }
        }
        // Soul campfire braziers flanking the steps
        setBlock(level, new BlockPos(gx - 5, gy, gz + 3), CAMPFIRE);
        setBlock(level, new BlockPos(gx + 5, gy, gz + 3), CAMPFIRE);
        // Chests behind gate pillars (hidden guard supplies)
        ChestHelper.placeChestWithLoot(level, new BlockPos(gx - 7, gy, gz), LOOT_OUTER_GATE);
        ChestHelper.placeChestWithLoot(level, new BlockPos(gx + 7, gy, gz), LOOT_OUTER_GATE);
    }

    // ── c. Main Plaza ─────────────────────────────────────────────────
    private static void buildMainPlaza(ServerLevel level, BlockPos c) {
        // 24×24 blackstone floor (larger than Heng Yue — the sect's heart)
        fill(level, c.offset(-12, -1, -12), c.offset(12, -1, 12), BLACKSTONE);
        fill(level, c.offset(-12, 0, -12), c.offset(12, 0, 12), B.DEEPSLATE_BRICK);
        // Obsidian border around the plaza edge (1 block wide)
        for (int dx = -12; dx <= 12; dx++) {
            setBlock(level, c.offset(dx, 0, -12), B.OBSIDIAN);
            setBlock(level, c.offset(dx, 0, 12), B.OBSIDIAN);
        }
        for (int dz = -12; dz <= 12; dz++) {
            setBlock(level, c.offset(-12, 0, dz), B.OBSIDIAN);
            setBlock(level, c.offset(12, 0, dz), B.OBSIDIAN);
        }
        // Formation core stone (lapis) ring at radius 8
        ring(level, c, 8, 0, B.LAPIS);
        // Blood stone ring at radius 4
        ring(level, c, 4, 0, B.REDSTONE_BLOCK);
        // Central soul fire (soul campfire)
        setBlock(level, c.offset(0, 1, 0), CAMPFIRE);
        // 8 soul pillars (obsidian column + sea lantern on top) in a ring radius 6
        for (int angle = 0; angle < 360; angle += 45) {
            double rad = Math.toRadians(angle);
            int px = (int) Math.round(Math.cos(rad) * 6);
            int pz = (int) Math.round(Math.sin(rad) * 6);
            // Obsidian pillar, 5 blocks tall
            for (int dy = 1; dy <= 5; dy++) {
                setBlock(level, c.offset(px, dy, pz), B.OBSIDIAN);
            }
            // Sea lantern on top (ghost-light glow)
            setBlock(level, c.offset(px, 6, pz), SEA_LANTERN);
        }
        // Bone block decorations at cardinal points inside the ring
        int[][] bonePoints = {{-6, 0}, {6, 0}, {0, -6}, {0, 6}};
        for (int[] bp : bonePoints) {
            setBlock(level, c.offset(bp[0], 1, bp[1]), BONE_BLOCK);
            setBlock(level, c.offset(bp[0], 2, bp[1]), SKELETON_SKULL);
        }
        // Soul lanterns along the obsidian border
        for (int i = -10; i <= 10; i += 4) {
            setBlock(level, c.offset(i, 1, -11), SOUL_LANTERN);
            setBlock(level, c.offset(i, 1, 11), SOUL_LANTERN);
            setBlock(level, c.offset(-11, 1, i), SOUL_LANTERN);
            setBlock(level, c.offset(11, 1, i), SOUL_LANTERN);
        }
        // Chests at the inner corners (plaza offerings)
        ChestHelper.placeChestWithLoot(level, c.offset(-10, 1, -10), LOOT_MAIN_PLAZA);
        ChestHelper.placeChestWithLoot(level, c.offset(10, 1, 10), LOOT_MAIN_PLAZA);
        ChestHelper.placeChestWithLoot(level, c.offset(-10, 1, 10), LOOT_MAIN_PLAZA);
    }

    // ── d. Furnace Hall (THE central structure) ────────────────────────
    private static void buildFurnaceHall(ServerLevel level, BlockPos c) {
        BlockPos base = c.offset(0, 0, -20);
        // Massive chamber: 20 wide (x) × 15 tall (y) × 20 deep (z)
        // Floor: blackstone, slightly recessed
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                setBlock(level, base.offset(dx, -1, dz), B.DEEPSLATE);
                setBlock(level, base.offset(dx, 0, dz), BLACKSTONE);
            }
        }
        // Walls: nether brick + obsidian corners
        for (int dx = -10; dx <= 10; dx++) {
            for (int dy = 1; dy <= 14; dy++) {
                // North and south walls
                setBlock(level, base.offset(dx, dy, -10),
                        (Math.abs(dx) <= 2) ? B.OBSIDIAN : NETHER_BRICK);
                setBlock(level, base.offset(dx, dy, 10),
                        (Math.abs(dx) <= 2) ? B.OBSIDIAN : NETHER_BRICK);
            }
        }
        for (int dz = -10; dz <= 10; dz++) {
            for (int dy = 1; dy <= 14; dy++) {
                // East and west walls
                setBlock(level, base.offset(-10, dy, dz),
                        (Math.abs(dz) <= 2) ? B.OBSIDIAN : NETHER_BRICK);
                setBlock(level, base.offset(10, dy, dz),
                        (Math.abs(dz) <= 2) ? B.OBSIDIAN : NETHER_BRICK);
            }
        }
        // Ceiling: blackstone with sea lantern lights
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                setBlock(level, base.offset(dx, 15, dz), BLACKSTONE);
            }
        }
        // Ceiling sea lanterns in a grid
        for (int dx = -8; dx <= 8; dx += 4) {
            for (int dz = -8; dz <= 8; dz += 4) {
                setBlock(level, base.offset(dx, 14, dz), SEA_LANTERN);
            }
        }
        // 4 massive obsidian corner pillars (2×2 × 14 tall)
        int[][] pillarCorners = {{-9, -9}, {9, -9}, {-9, 9}, {9, 9}};
        for (int[] pc : pillarCorners) {
            for (int ox = 0; ox < 2; ox++) {
                for (int oz = 0; oz < 2; oz++) {
                    for (int dy = 1; dy <= 14; dy++) {
                        setBlock(level, base.offset(pc[0] + ox, dy, pc[1] + oz), B.OBSIDIAN);
                    }
                }
            }
        }
        // 4 intermediate obsidian pillars (1×1 × 10 tall) along walls
        int[][] midPillars = {{-5, -9}, {5, -9}, {-5, 9}, {5, 9}};
        for (int[] mp : midPillars) {
            for (int dy = 1; dy <= 10; dy++) {
                setBlock(level, base.offset(mp[0], dy, mp[1]), B.OBSIDIAN);
            }
            setBlock(level, base.offset(mp[0], 11, mp[1]), SEA_LANTERN);
        }
        // Central altar: raised platform of obsidian with blast furnace centerpiece
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                setBlock(level, base.offset(dx, 1, dz), B.OBSIDIAN);
                setBlock(level, base.offset(dx, 2, dz), B.OBSIDIAN);
            }
        }
        // Blast furnace (the Soul Refining Furnace itself)
        setBlock(level, base.offset(0, 3, 0), BLAST_FURNACE);
        // Cauldrons flanking the furnace (soul collection basins)
        setBlock(level, base.offset(-2, 3, 0), CAULDRON);
        setBlock(level, base.offset(2, 3, 0), CAULDRON);
        setBlock(level, base.offset(0, 3, -2), CAULDRON);
        setBlock(level, base.offset(0, 3, 2), CAULDRON);
        // Soul lanterns around the altar platform
        setBlock(level, base.offset(-3, 3, -3), SOUL_LANTERN);
        setBlock(level, base.offset(3, 3, -3), SOUL_LANTERN);
        setBlock(level, base.offset(-3, 3, 3), SOUL_LANTERN);
        setBlock(level, base.offset(3, 3, 3), SOUL_LANTERN);
        // Blood stone accent line along the floor approaching the altar
        for (int dz = -8; dz <= -3; dz++) {
            setBlock(level, base.offset(0, 1, dz), B.REDSTONE_BLOCK);
        }
        // Skull sentries at the entrance (north wall opening)
        setBlock(level, base.offset(-2, 1, -9), SKELETON_SKULL);
        setBlock(level, base.offset(2, 1, -9), SKELETON_SKULL);
        // Iron bar gate at the entrance (can be opened)
        for (int dy = 1; dy <= 4; dy++) {
            setBlock(level, base.offset(-1, dy, -10), IRON_BARS);
            setBlock(level, base.offset(0, dy, -10), IRON_BARS);
            setBlock(level, base.offset(1, dy, -10), IRON_BARS);
        }
        // Clear the entrance passage (air blocks in the wall gap)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 1; dy <= 6; dy++) {
                setBlock(level, base.offset(dx, dy, -9), AIR);
            }
        }
        // Chests: sect's most precious artifacts
        ChestHelper.placeChestWithLoot(level, base.offset(-7, 1, -7), LOOT_FURNACE);
        ChestHelper.placeChestWithLoot(level, base.offset(7, 1, -7), LOOT_FURNACE);
        ChestHelper.placeChestWithLoot(level, base.offset(-7, 1, 7), LOOT_FURNACE);
        ChestHelper.placeChestWithLoot(level, base.offset(7, 1, 7), LOOT_FURNACE);
        // Soul campfire braziers near the chests
        setBlock(level, base.offset(-7, 1, -8), CAMPFIRE);
        setBlock(level, base.offset(7, 1, -8), CAMPFIRE);
        setBlock(level, base.offset(-7, 1, 8), CAMPFIRE);
        setBlock(level, base.offset(7, 1, 8), CAMPFIRE);
    }

    // ── e. Library ────────────────────────────────────────────────────
    private static void buildLibrary(ServerLevel level, BlockPos c) {
        BlockPos base = c.offset(-25, 0, 0);
        // 14×10 dark stone floor
        fill(level, base.offset(-7, 0, -5), base.offset(7, 0, 5), BLACKSTONE);
        // Nether brick walls, 5 tall
        for (int dx = -7; dx <= 7; dx++) {
            for (int dy = 1; dy <= 5; dy++) {
                setBlock(level, base.offset(dx, dy, -5), NETHER_BRICK);
                setBlock(level, base.offset(dx, dy, 5), NETHER_BRICK);
            }
        }
        for (int dz = -5; dz <= 5; dz++) {
            for (int dy = 1; dy <= 5; dy++) {
                setBlock(level, base.offset(-7, dy, dz), NETHER_BRICK);
                setBlock(level, base.offset(7, dy, dz), NETHER_BRICK);
            }
        }
        // Ceiling: blackstone
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                setBlock(level, base.offset(dx, 6, dz), BLACKSTONE);
            }
        }
        // Bookshelves behind iron bars (forbidden techniques — locked away)
        // West wall bookshelf row
        for (int dz = -3; dz <= 3; dz++) {
            for (int dy = 1; dy <= 3; dy++) {
                setBlock(level, base.offset(-6, dy, dz), BOOKSHELF);
                setBlock(level, base.offset(-5, dy, dz), IRON_BARS);
            }
        }
        // East wall bookshelf row
        for (int dz = -3; dz <= 3; dz++) {
            for (int dy = 1; dy <= 3; dy++) {
                setBlock(level, base.offset(6, dy, dz), BOOKSHELF);
                setBlock(level, base.offset(5, dy, dz), IRON_BARS);
            }
        }
        // Center aisle: bookshelves on both sides with iron bars in front
        for (int dx = -2; dx <= -1; dx++) {
            for (int dz = -3; dz <= 3; dz += 2) {
                setBlock(level, base.offset(dx, 1, dz), BOOKSHELF);
                setBlock(level, base.offset(dx, 2, dz), BOOKSHELF);
                setBlock(level, base.offset(dx, 3, dz), BOOKSHELF);
                setBlock(level, base.offset(dx + 1, 1, dz), IRON_BARS);
                setBlock(level, base.offset(dx + 1, 2, dz), IRON_BARS);
                setBlock(level, base.offset(dx + 1, 3, dz), IRON_BARS);
            }
        }
        for (int dx = 1; dx <= 2; dx++) {
            for (int dz = -3; dz <= 3; dz += 2) {
                setBlock(level, base.offset(dx, 1, dz), BOOKSHELF);
                setBlock(level, base.offset(dx, 2, dz), BOOKSHELF);
                setBlock(level, base.offset(dx, 3, dz), BOOKSHELF);
                setBlock(level, base.offset(dx - 1, 1, dz), IRON_BARS);
                setBlock(level, base.offset(dx - 1, 2, dz), IRON_BARS);
                setBlock(level, base.offset(dx - 1, 3, dz), IRON_BARS);
            }
        }
        // Skull guards flanking the entrance
        setBlock(level, base.offset(-2, 1, -4), SKELETON_SKULL);
        setBlock(level, base.offset(2, 1, -4), SKELETON_SKULL);
        // Soul lanterns for dim blue lighting
        setBlock(level, base.offset(0, 5, 0), SOUL_LANTERN);
        setBlock(level, base.offset(-4, 4, -4), SOUL_LANTERN);
        setBlock(level, base.offset(4, 4, -4), SOUL_LANTERN);
        setBlock(level, base.offset(-4, 4, 4), SOUL_LANTERN);
        setBlock(level, base.offset(4, 4, 4), SOUL_LANTERN);
        // Dark oak door at entrance
        setBlock(level, base.offset(0, 1, 5), DARK_OAK_DOOR);
        setBlock(level, base.offset(0, 2, 5), DARK_OAK_DOOR);
        // Clear entrance gap
        setBlock(level, base.offset(0, 1, 5), AIR);
        setBlock(level, base.offset(0, 2, 5), AIR);
        // Chests with forbidden technique loot
        ChestHelper.placeChestWithLoot(level, base.offset(-3, 1, 4), LOOT_LIBRARY);
        ChestHelper.placeChestWithLoot(level, base.offset(3, 1, 4), LOOT_LIBRARY);
        ChestHelper.placeChestWithLoot(level, base.offset(0, 1, -3), LOOT_LIBRARY);
    }

    // ── f. Alchemy Courtyard ─────────────────────────────────────────
    private static void buildAlchemyCourtyard(ServerLevel level, BlockPos c) {
        BlockPos base = c.offset(25, 0, 0);
        // 16×12 courtyard floor: nether brick (fire-resistant)
        fill(level, base.offset(-8, 0, -6), base.offset(8, 0, 6), NETHER_BRICK);
        // Scorched stone underlayer
        fill(level, base.offset(-8, -1, -6), base.offset(8, -1, 6), B.DEEPSLATE_BRICK);
        // Netherrack patches (for eternal fire — soul refinement fires that never go out)
        int[][] firePatches = {{-6, -3}, {-6, 3}, {6, -3}, {6, 3}, {0, 0}};
        for (int[] fp : firePatches) {
            setBlock(level, base.offset(fp[0], 0, fp[1]), NETHERRACK);
            setBlock(level, base.offset(fp[0], 1, fp[1]), CAMPFIRE);
        }
        // Blast furnaces along the west wall (soul refinement kilns)
        for (int dz = -4; dz <= 4; dz += 2) {
            setBlock(level, base.offset(-7, 1, dz), BLAST_FURNACE);
        }
        // Cauldrons along the east wall (soul essence collection)
        for (int dz = -4; dz <= 4; dz += 2) {
            setBlock(level, base.offset(7, 1, dz), CAULDRON);
        }
        // Obsidian workbenches in the center area
        for (int dx = -2; dx <= 2; dx += 2) {
            setBlock(level, base.offset(dx, 1, -1), B.OBSIDIAN);
            setBlock(level, base.offset(dx, 1, 1), B.OBSIDIAN);
        }
        // Blood stone channels connecting furnaces to cauldrons (essence conduits)
        for (int dz = -4; dz <= 4; dz += 2) {
            setBlock(level, base.offset(-5, 0, dz), B.REDSTONE_BLOCK);
            setBlock(level, base.offset(-3, 0, dz), B.REDSTONE_BLOCK);
            setBlock(level, base.offset(3, 0, dz), B.REDSTONE_BLOCK);
            setBlock(level, base.offset(5, 0, dz), B.REDSTONE_BLOCK);
        }
        // Nether brick walls enclosing the courtyard (3 tall)
        for (int dx = -8; dx <= 8; dx++) {
            for (int dy = 1; dy <= 3; dy++) {
                setBlock(level, base.offset(dx, dy, -6), NETHER_BRICK);
                setBlock(level, base.offset(dx, dy, 6), NETHER_BRICK);
            }
        }
        for (int dz = -6; dz <= 6; dz++) {
            for (int dy = 1; dy <= 3; dy++) {
                setBlock(level, base.offset(-8, dy, dz), NETHER_BRICK);
                setBlock(level, base.offset(8, dy, dz), NETHER_BRICK);
            }
        }
        // Soul lanterns on the walls
        setBlock(level, base.offset(-7, 4, 0), SOUL_LANTERN);
        setBlock(level, base.offset(7, 4, 0), SOUL_LANTERN);
        setBlock(level, base.offset(0, 4, -5), SOUL_LANTERN);
        setBlock(level, base.offset(0, 4, 5), SOUL_LANTERN);
        // Entrance gaps (south wall)
        setBlock(level, base.offset(0, 1, 6), AIR);
        setBlock(level, base.offset(0, 2, 6), AIR);
        setBlock(level, base.offset(-1, 1, 6), AIR);
        setBlock(level, base.offset(-1, 2, 6), AIR);
        // Chests with alchemy materials
        ChestHelper.placeChestWithLoot(level, base.offset(-6, 1, 0), LOOT_ALCHEMY);
        ChestHelper.placeChestWithLoot(level, base.offset(6, 1, 0), LOOT_ALCHEMY);
        ChestHelper.placeChestWithLoot(level, base.offset(0, 1, -5), LOOT_ALCHEMY);
    }

    // ── g. Ancestor Hall ──────────────────────────────────────────────
    private static void buildAncestorHall(ServerLevel level, BlockPos c) {
        BlockPos base = c.offset(0, 0, 25);
        // 14×10 dark floor
        fill(level, base.offset(-7, 0, -5), base.offset(7, 0, 5), B.DEEPSLATE_BRICK);
        // Nether brick walls, 5 tall (taller than alchemy — more reverent)
        for (int dx = -7; dx <= 7; dx++) {
            for (int dy = 1; dy <= 5; dy++) {
                setBlock(level, base.offset(dx, dy, -5), NETHER_BRICK);
                setBlock(level, base.offset(dx, dy, 5), NETHER_BRICK);
            }
        }
        for (int dz = -5; dz <= 5; dz++) {
            for (int dy = 1; dy <= 5; dy++) {
                setBlock(level, base.offset(-7, dy, dz), NETHER_BRICK);
                setBlock(level, base.offset(7, dy, dz), NETHER_BRICK);
            }
        }
        // Blackstone ceiling
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                setBlock(level, base.offset(dx, 6, dz), BLACKSTONE);
            }
        }
        // Back wall (north): bone block altar with skull arrangements
        for (int dx = -3; dx <= 3; dx++) {
            setBlock(level, base.offset(dx, 1, -4), BONE_BLOCK);
            setBlock(level, base.offset(dx, 2, -4), BONE_BLOCK);
        }
        // Central ancestor skull (larger arrangement)
        setBlock(level, base.offset(-1, 3, -4), SKELETON_SKULL);
        setBlock(level, base.offset(0, 3, -4), SKELETON_SKULL);
        setBlock(level, base.offset(1, 3, -4), SKELETON_SKULL);
        // Blood stone offerings in front of the altar
        setBlock(level, base.offset(0, 1, -3), B.REDSTONE_BLOCK);
        setBlock(level, base.offset(-2, 1, -3), B.REDSTONE_BLOCK);
        setBlock(level, base.offset(2, 1, -3), B.REDSTONE_BLOCK);
        // 4 soul campfire incense braziers
        setBlock(level, base.offset(-5, 1, -2), CAMPFIRE);
        setBlock(level, base.offset(-5, 1, 2), CAMPFIRE);
        setBlock(level, base.offset(5, 1, -2), CAMPFIRE);
        setBlock(level, base.offset(5, 1, 2), CAMPFIRE);
        // Side altars (bone block + skull)
        setBlock(level, base.offset(-5, 1, 0), BONE_BLOCK);
        setBlock(level, base.offset(-5, 2, 0), SKELETON_SKULL);
        setBlock(level, base.offset(5, 1, 0), BONE_BLOCK);
        setBlock(level, base.offset(5, 2, 0), SKELETON_SKULL);
        // Dim soul lantern lighting (only 4 — deliberately dark)
        setBlock(level, base.offset(-6, 5, 0), SOUL_LANTERN);
        setBlock(level, base.offset(6, 5, 0), SOUL_LANTERN);
        setBlock(level, base.offset(0, 5, -4), SOUL_LANTERN);
        setBlock(level, base.offset(0, 5, 4), SOUL_LANTERN);
        // Kneeling pads (spirit stone slabs) in two rows
        for (int dx = -3; dx <= 3; dx++) {
            setBlock(level, base.offset(dx, 1, -1), B.SPIRIT_STONE_SLAB);
            setBlock(level, base.offset(dx, 1, 1), B.SPIRIT_STONE_SLAB);
        }
        // Entrance (south wall gap)
        setBlock(level, base.offset(0, 1, 5), AIR);
        setBlock(level, base.offset(0, 2, 5), AIR);
        setBlock(level, base.offset(-1, 1, 5), AIR);
        setBlock(level, base.offset(-1, 2, 5), AIR);
        // Chests at side altars (ancestor offerings)
        ChestHelper.placeChestWithLoot(level, base.offset(-6, 1, -3), LOOT_ANCESTOR_HALL);
        ChestHelper.placeChestWithLoot(level, base.offset(6, 1, -3), LOOT_ANCESTOR_HALL);
        ChestHelper.placeChestWithLoot(level, base.offset(-6, 1, 3), LOOT_ANCESTOR_HALL);
        ChestHelper.placeChestWithLoot(level, base.offset(6, 1, 3), LOOT_ANCESTOR_HALL);
    }

    // ── h. Sword Peak ────────────────────────────────────────────────
    private static void buildSwordPeak(ServerLevel level, BlockPos c) {
        BlockPos base = c.offset(25, 0, -20);
        // 12×12 raised platform: blackstone
        fill(level, base.offset(-6, 0, -6), base.offset(6, 0, 6), BLACKSTONE);
        // Scorched stone sublayer
        fill(level, base.offset(-6, -1, -6), base.offset(6, -1, 6), B.DEEPSLATE);
        // Nether brick walls, 4 tall
        for (int dx = -6; dx <= 6; dx++) {
            for (int dy = 1; dy <= 4; dy++) {
                setBlock(level, base.offset(dx, dy, -6), NETHER_BRICK);
                setBlock(level, base.offset(dx, dy, 6), NETHER_BRICK);
            }
        }
        for (int dz = -6; dz <= 6; dz++) {
            for (int dy = 1; dy <= 4; dy++) {
                setBlock(level, base.offset(-6, dy, dz), NETHER_BRICK);
                setBlock(level, base.offset(6, dy, dz), NETHER_BRICK);
            }
        }
        // Blackstone ceiling
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                setBlock(level, base.offset(dx, 5, dz), BLACKSTONE);
            }
        }
        // Weapon display racks: iron bar grids on walls
        // North wall rack
        for (int dx = -4; dx <= 4; dx++) {
            setBlock(level, base.offset(dx, 2, -5), IRON_BARS);
            setBlock(level, base.offset(dx, 3, -5), IRON_BARS);
        }
        // South wall rack
        for (int dx = -4; dx <= 4; dx++) {
            setBlock(level, base.offset(dx, 2, 5), IRON_BARS);
            setBlock(level, base.offset(dx, 3, 5), IRON_BARS);
        }
        // West wall rack
        for (int dz = -4; dz <= 4; dz++) {
            setBlock(level, base.offset(-5, 2, dz), IRON_BARS);
            setBlock(level, base.offset(-5, 3, dz), IRON_BARS);
        }
        // East wall rack
        for (int dz = -4; dz <= 4; dz++) {
            setBlock(level, base.offset(5, 2, dz), IRON_BARS);
            setBlock(level, base.offset(5, 3, dz), IRON_BARS);
        }
        // Central weapon anvil on obsidian
        setBlock(level, base.offset(0, 1, 0), B.OBSIDIAN);
        setBlock(level, base.offset(0, 2, 0), Blocks.ANVIL.defaultBlockState());
        // Soul lanterns for eerie lighting
        setBlock(level, base.offset(-4, 5, -4), SOUL_LANTERN);
        setBlock(level, base.offset(4, 5, -4), SOUL_LANTERN);
        setBlock(level, base.offset(-4, 5, 4), SOUL_LANTERN);
        setBlock(level, base.offset(4, 5, 4), SOUL_LANTERN);
        // Skull trophy shelf
        for (int dx = -2; dx <= 2; dx++) {
            setBlock(level, base.offset(dx, 1, -4), SKELETON_SKULL);
        }
        // Entrance (south wall gap)
        setBlock(level, base.offset(0, 1, 6), AIR);
        setBlock(level, base.offset(0, 2, 6), AIR);
        setBlock(level, base.offset(-1, 1, 6), AIR);
        setBlock(level, base.offset(-1, 2, 6), AIR);
        // Chests with weapon loot
        ChestHelper.placeChestWithLoot(level, base.offset(-4, 1, 0), LOOT_SWORD_PEAK);
        ChestHelper.placeChestWithLoot(level, base.offset(4, 1, 0), LOOT_SWORD_PEAK);
        ChestHelper.placeChestWithLoot(level, base.offset(0, 1, 4), LOOT_SWORD_PEAK);
    }

    // ── i. Spirit Beast Pens ──────────────────────────────────────────
    private static void buildSpiritBeastPens(ServerLevel level, BlockPos c) {
        BlockPos base = c.offset(-25, 0, -20);
        // 18×14 area: 3 separate pens side by side
        // Floor: netherrack (savage, primal)
        fill(level, base.offset(-9, 0, -7), base.offset(9, 0, 7), NETHERRACK);
        // Deepslate underlayer
        fill(level, base.offset(-9, -1, -7), base.offset(9, -1, 7), B.DEEPSLATE);
        // Pen dividers: iron bars running east-west (3 pens)
        // Divider 1 at x = -3
        for (int dz = -7; dz <= 7; dz++) {
            for (int dy = 1; dy <= 3; dy++) {
                setBlock(level, base.offset(-3, dy, dz), IRON_BARS);
            }
        }
        // Divider 2 at x = 3
        for (int dz = -7; dz <= 7; dz++) {
            for (int dy = 1; dy <= 3; dy++) {
                setBlock(level, base.offset(3, dy, dz), IRON_BARS);
            }
        }
        // Outer walls: dark oak fence + nether brick
        for (int dx = -9; dx <= 9; dx++) {
            for (int dy = 1; dy <= 3; dy++) {
                setBlock(level, base.offset(dx, dy, -7), NETHER_BRICK);
                setBlock(level, base.offset(dx, dy, 7), NETHER_BRICK);
            }
        }
        for (int dz = -7; dz <= 7; dz++) {
            for (int dy = 1; dy <= 3; dy++) {
                setBlock(level, base.offset(-9, dy, dz), NETHER_BRICK);
                setBlock(level, base.offset(9, dy, dz), NETHER_BRICK);
            }
        }
        // Spruce fence posts at corners (cage reinforcement)
        int[][] penCorners = {
                {-8, -6}, {-2, -6}, {2, -6}, {8, -6},
                {-8, 6}, {-2, 6}, {2, 6}, {8, 6}
        };
        for (int[] pc : penCorners) {
            for (int dy = 1; dy <= 4; dy++) {
                setBlock(level, base.offset(pc[0], dy, pc[1]), SPRUCE_FENCE);
            }
        }
        // Soul campfires in each pen (warmth for the beasts — or punishment)
        setBlock(level, base.offset(-6, 1, 0), CAMPFIRE);
        setBlock(level, base.offset(0, 1, 0), CAMPFIRE);
        setBlock(level, base.offset(6, 1, 0), CAMPFIRE);
        // Soul lanterns overhead
        setBlock(level, base.offset(-6, 4, 0), SOUL_LANTERN);
        setBlock(level, base.offset(0, 4, 0), SOUL_LANTERN);
        setBlock(level, base.offset(6, 4, 0), SOUL_LANTERN);
        // Skull decorations (previous failed beasts)
        setBlock(level, base.offset(-7, 1, -5), SKELETON_SKULL);
        setBlock(level, base.offset(-1, 1, -5), SKELETON_SKULL);
        setBlock(level, base.offset(5, 1, -5), SKELETON_SKULL);
        setBlock(level, base.offset(-7, 1, 5), SKELETON_SKULL);
        setBlock(level, base.offset(1, 1, 5), SKELETON_SKULL);
        setBlock(level, base.offset(7, 1, 5), SKELETON_SKULL);
        // Chests in each pen (beast handler supplies)
        ChestHelper.placeChestWithLoot(level, base.offset(-7, 1, 3), LOOT_BEAST_PENS);
        ChestHelper.placeChestWithLoot(level, base.offset(0, 1, 5), LOOT_BEAST_PENS);
        ChestHelper.placeChestWithLoot(level, base.offset(7, 1, 3), LOOT_BEAST_PENS);
    }

    // ── j. Dormitories ───────────────────────────────────────────────
    private static void buildDormitories(ServerLevel level, BlockPos c) {
        // Two long halls on the east side of the valley
        BlockPos[] halls = {c.offset(18, 0, 18), c.offset(18, 0, 25)};
        for (BlockPos hall : halls) {
            // Floor: dark oak planks (dark, spartan)
            fill(level, hall.offset(-7, 0, -3), hall.offset(7, 0, 3), B.DARK_OAK_PLANK);
            // Nether brick walls, 4 tall
            for (int dx = -7; dx <= 7; dx++) {
                for (int dy = 1; dy <= 4; dy++) {
                    setBlock(level, hall.offset(dx, dy, -3), NETHER_BRICK);
                    setBlock(level, hall.offset(dx, dy, 3), NETHER_BRICK);
                }
            }
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = 1; dy <= 4; dy++) {
                    setBlock(level, hall.offset(-7, dy, dz), NETHER_BRICK);
                    setBlock(level, hall.offset(7, dy, dz), NETHER_BRICK);
                }
            }
            // Blackstone ceiling
            for (int dx = -7; dx <= 7; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    setBlock(level, hall.offset(dx, 5, dz), BLACKSTONE);
                }
            }
            // Dark beds: 6 per hall (all black beds — no comfort)
            for (int i = 0; i < 6; i++) {
                int dx = -5 + i * 2;
                BlockState bedHead = DARK_BED
                        .setValue(BedBlock.PART, BedPart.HEAD)
                        .setValue(BedBlock.FACING, Direction.SOUTH);
                BlockState bedFoot = DARK_BED
                        .setValue(BedBlock.PART, BedPart.FOOT)
                        .setValue(BedBlock.FACING, Direction.SOUTH);
                setBlock(level, hall.offset(dx, 1, -2), bedHead);
                setBlock(level, hall.offset(dx, 1, -1), bedFoot);
            }
            // Soul lantern for dim lighting (one per hall — grim)
            setBlock(level, hall.offset(0, 4, 0), SOUL_LANTERN);
            // Soul campfire at the end (heat)
            setBlock(level, hall.offset(0, 1, 2), CAMPFIRE);
        }
        // Entrance gaps (south wall of each hall)
        setBlock(level, c.offset(18, 1, 21), AIR);
        setBlock(level, c.offset(18, 2, 21), AIR);
        setBlock(level, c.offset(18, 1, 28), AIR);
        setBlock(level, c.offset(18, 2, 28), AIR);
        // Chests in each dormitory
        ChestHelper.placeChestWithLoot(level, c.offset(12, 1, 20), LOOT_DORMITORIES);
        ChestHelper.placeChestWithLoot(level, c.offset(24, 1, 20), LOOT_DORMITORIES);
        ChestHelper.placeChestWithLoot(level, c.offset(12, 1, 27), LOOT_DORMITORIES);
        ChestHelper.placeChestWithLoot(level, c.offset(24, 1, 27), LOOT_DORMITORIES);
    }

    // ── k. Seclusion Caves ────────────────────────────────────────────
    private static void buildSeclusionCaves(ServerLevel level, BlockPos c) {
        // 4 meditation cells carved into the valley's east wall
        int[] xs = {-22, -22, -22, -22};
        int[] zs = {-10, 0, 10, 20};
        for (int i = 0; i < 4; i++) {
            BlockPos caveBase = c.offset(xs[i], 0, zs[i]);
            // Nether brick frame
            for (int dy = 0; dy < 3; dy++) {
                setBlock(level, caveBase.offset(-1, dy, 0), NETHER_BRICK);
                setBlock(level, caveBase.offset(1, dy, 0), NETHER_BRICK);
            }
            // Arch top
            setBlock(level, caveBase.offset(-1, 3, 0), NETHER_BRICK);
            setBlock(level, caveBase.offset(0, 3, 0), NETHER_BRICK);
            setBlock(level, caveBase.offset(1, 3, 0), NETHER_BRICK);
            // Hollow chamber 5×5×4
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = 0; dy < 4; dy++) {
                    for (int dz = 1; dz <= 5; dz++) {
                        if (dx == -2 || dx == 2 || dy == 0 || dy == 3 || dz == 5) {
                            setBlock(level, caveBase.offset(dx, dy, dz), B.DEEPSLATE_BRICK);
                        } else {
                            setBlock(level, caveBase.offset(dx, dy, dz), AIR);
                        }
                    }
                }
            }
            // Entrance (air)
            setBlock(level, caveBase.offset(0, 0, 0), AIR);
            setBlock(level, caveBase.offset(0, 1, 0), AIR);
            setBlock(level, caveBase.offset(0, 2, 0), AIR);
            // Meditation mat (spirit stone slab)
            setBlock(level, caveBase.offset(0, 1, 3), B.SPIRIT_STONE_SLAB);
            // Soul lantern (one per cave — dim, isolated)
            setBlock(level, caveBase.offset(-1, 2, 3), SOUL_LANTERN);
            // Soul campfire brazier
            setBlock(level, caveBase.offset(0, 1, 4), CAMPFIRE);
        }
        // Chests in caves 1 and 3 (inner sect resources)
        ChestHelper.placeChestWithLoot(level, c.offset(-22, 1, -7), LOOT_SECLUSION);
        ChestHelper.placeChestWithLoot(level, c.offset(-22, 1, 3), LOOT_SECLUSION);
        ChestHelper.placeChestWithLoot(level, c.offset(-22, 1, 13), LOOT_SECLUSION);
        ChestHelper.placeChestWithLoot(level, c.offset(-22, 1, 23), LOOT_SECLUSION);
    }

    // ── l. Underground Passage ─────────────────────────────────────────
    private static void buildUndergroundPassage(ServerLevel level, BlockPos c) {
        BlockPos base = c.offset(0, -3, 0);
        // Secret tunnel running north-south, 3 wide × 4 tall × 40 long
        // Nether brick walls and ceiling, deepslate floor
        for (int dz = -20; dz <= 20; dz++) {
            // Floor
            for (int dx = -1; dx <= 1; dx++) {
                setBlock(level, base.offset(dx, 0, dz), B.DEEPSLATE);
            }
            // Ceiling
            for (int dx = -1; dx <= 1; dx++) {
                setBlock(level, base.offset(dx, 4, dz), NETHER_BRICK);
            }
            // Walls
            setBlock(level, base.offset(-2, 1, dz), NETHER_BRICK);
            setBlock(level, base.offset(-2, 2, dz), NETHER_BRICK);
            setBlock(level, base.offset(-2, 3, dz), NETHER_BRICK);
            setBlock(level, base.offset(2, 1, dz), NETHER_BRICK);
            setBlock(level, base.offset(2, 2, dz), NETHER_BRICK);
            setBlock(level, base.offset(2, 3, dz), NETHER_BRICK);
            // Air inside
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = 1; dy <= 3; dy++) {
                    setBlock(level, base.offset(dx, dy, dz), AIR);
                }
            }
        }
        // Soul lanterns every 8 blocks (sparse, oppressive)
        for (int dz = -16; dz <= 16; dz += 8) {
            setBlock(level, base.offset(-1, 3, dz), SOUL_LANTERN);
            setBlock(level, base.offset(1, 3, dz), SOUL_LANTERN);
        }
        // Skeleton skull warnings at the tunnel ends
        setBlock(level, base.offset(0, 1, -19), SKELETON_SKULL);
        setBlock(level, base.offset(0, 1, 19), SKELETON_SKULL);
        // Dead side chambers (2 small rooms branching off)
        // Room 1 at dz = -10 (west)
        for (int dx = 3; dx <= 7; dx++) {
            for (int dz = -12; dz <= -8; dz++) {
                for (int dy = 0; dy <= 3; dy++) {
                    if (dx == 3 || dx == 7 || dz == -12 || dz == -8 || dy == 0 || dy == 3) {
                        setBlock(level, base.offset(dx, dy, dz), NETHER_BRICK);
                    } else {
                        setBlock(level, base.offset(dx, dy, dz), AIR);
                    }
                }
            }
        }
        // Room 2 at dz = 10 (east)
        for (int dx = -7; dx <= -3; dx++) {
            for (int dz = 8; dz <= 12; dz++) {
                for (int dy = 0; dy <= 3; dy++) {
                    if (dx == -7 || dx == -3 || dz == 8 || dz == 12 || dy == 0 || dy == 3) {
                        setBlock(level, base.offset(dx, dy, dz), NETHER_BRICK);
                    } else {
                        setBlock(level, base.offset(dx, dy, dz), AIR);
                    }
                }
            }
        }
        // Connect side rooms to main tunnel
        setBlock(level, base.offset(2, 1, -10), AIR);
        setBlock(level, base.offset(2, 2, -10), AIR);
        setBlock(level, base.offset(-2, 1, 10), AIR);
        setBlock(level, base.offset(-2, 2, 10), AIR);
        // Soul lanterns in side rooms
        setBlock(level, base.offset(5, 3, -10), SOUL_LANTERN);
        setBlock(level, base.offset(-5, 3, 10), SOUL_LANTERN);
        // Chests in the side rooms (hidden treasury)
        ChestHelper.placeChestWithLoot(level, base.offset(5, 1, -11), LOOT_UNDERGROUND);
        ChestHelper.placeChestWithLoot(level, base.offset(5, 1, -9), LOOT_UNDERGROUND);
        ChestHelper.placeChestWithLoot(level, base.offset(-5, 1, 9), LOOT_UNDERGROUND);
        ChestHelper.placeChestWithLoot(level, base.offset(-5, 1, 11), LOOT_UNDERGROUND);
    }

    // ── m. Trial Grounds ──────────────────────────────────────────────
    private static void buildTrialGrounds(ServerLevel level, BlockPos c) {
        BlockPos base = c.offset(-25, 0, 20);
        // 14×14 netherrack floor (hellish trial arena)
        fill(level, base.offset(-7, 0, -7), base.offset(7, 0, 7), NETHERRACK);
        // Deepslate underlayer
        fill(level, base.offset(-7, -1, -7), base.offset(7, -1, 7), B.DEEPSLATE);
        // Blackstone border walls, 4 tall
        for (int dx = -7; dx <= 7; dx++) {
            for (int dy = 1; dy <= 4; dy++) {
                setBlock(level, base.offset(dx, dy, -7), BLACKSTONE);
                setBlock(level, base.offset(dx, dy, 7), BLACKSTONE);
            }
        }
        for (int dz = -7; dz <= 7; dz++) {
            for (int dy = 1; dy <= 4; dy++) {
                setBlock(level, base.offset(-7, dy, dz), BLACKSTONE);
                setBlock(level, base.offset(7, dy, dz), BLACKSTONE);
            }
        }
        // Lava hazard blocks (single lava blocks scattered on netherrack floor)
        int[][] lavaPatches = {
                {-4, -4}, {-2, 2}, {3, -3}, {0, -5}, {5, 1},
                {-3, 5}, {2, -6}, {-5, 0}, {4, 4}, {0, 3},
                {-6, -2}, {1, -2}, {-1, 6}, {6, -1}
        };
        for (int[] lp : lavaPatches) {
            setBlock(level, base.offset(lp[0], 1, lp[1]), LAVA);
        }
        // Soul campfire markers around the edges (warning beacons)
        setBlock(level, base.offset(-5, 1, -5), CAMPFIRE);
        setBlock(level, base.offset(5, 1, -5), CAMPFIRE);
        setBlock(level, base.offset(-5, 1, 5), CAMPFIRE);
        setBlock(level, base.offset(5, 1, 5), CAMPFIRE);
        // Observation platforms: 2×3 elevated stands on north and south walls
        for (int dx = -2; dx <= 2; dx++) {
            setBlock(level, base.offset(dx, 1, -6), B.OBSIDIAN);
        }
        for (int dx = -2; dx <= 2; dx++) {
            setBlock(level, base.offset(dx, 1, 6), B.OBSIDIAN);
        }
        // Iron bar railings on observation platforms
        for (int dx = -2; dx <= 2; dx++) {
            setBlock(level, base.offset(dx, 2, -6), IRON_BARS);
            setBlock(level, base.offset(dx, 2, 6), IRON_BARS);
        }
        // Skull trophy display on the walls (failed trial participants)
        for (int dx = -5; dx <= 5; dx += 2) {
            setBlock(level, base.offset(dx, 3, -7), SKELETON_SKULL);
        }
        // Soul lanterns on the walls
        setBlock(level, base.offset(-6, 4, 0), SOUL_LANTERN);
        setBlock(level, base.offset(6, 4, 0), SOUL_LANTERN);
        setBlock(level, base.offset(0, 4, -6), SOUL_LANTERN);
        setBlock(level, base.offset(0, 4, 6), SOUL_LANTERN);
        // Blood stone lines forming a formation pattern on the floor
        for (int dx = -6; dx <= 6; dx += 2) {
            setBlock(level, base.offset(dx, 1, -6), B.REDSTONE_BLOCK);
            setBlock(level, base.offset(dx, 1, 6), B.REDSTONE_BLOCK);
        }
        for (int dz = -6; dz <= 6; dz += 2) {
            setBlock(level, base.offset(-6, 1, dz), B.REDSTONE_BLOCK);
            setBlock(level, base.offset(6, 1, dz), B.REDSTONE_BLOCK);
        }
        // Entrance gap (east wall)
        setBlock(level, base.offset(7, 1, 0), AIR);
        setBlock(level, base.offset(7, 2, 0), AIR);
        setBlock(level, base.offset(7, 3, 0), AIR);
        // Chests near observation platforms
        ChestHelper.placeChestWithLoot(level, base.offset(-1, 2, -5), LOOT_TRIAL);
        ChestHelper.placeChestWithLoot(level, base.offset(1, 2, -5), LOOT_TRIAL);
        ChestHelper.placeChestWithLoot(level, base.offset(-1, 2, 5), LOOT_TRIAL);
        ChestHelper.placeChestWithLoot(level, base.offset(1, 2, 5), LOOT_TRIAL);
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
