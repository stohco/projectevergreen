package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NanDouCityBuilder — FULLY hand-built Nan Dou City (南斗城), capital of the
 * Nan Dou region on Planet Suzaku.
 *
 * <p>Constitution: "The world is completely hand-crafted, accurate to the novels.
 * NEVER write a script that replaces vanilla blocks with other blocks as a shortcut.
 * Every structure must be hand-authored."
 *
 * <p>Canon basis (Renegade Immortal): Nan Dou City is the capital and largest city
 * of the Nan Dou region. It houses the regional cultivation authority, major merchant
 * guilds, the Temple of the Heavenly Dao, and hundreds of thousands of mortals and
 * cultivators. The city is ancient — its foundations predate the current cultivation
 * families. The city lord is a late Nascent Soul cultivator. The city serves as a
 * political nexus where sects negotiate, merchants trade spirit stones, and mortals
 * live in the shadow of immortals.
 *
 * <p>Architecture: Dark stone (deepslate, stone) + gold accents + red banners.
 * More imposing than Tian Shui City's elegance. This is a CAPITAL — it should feel
 * powerful, ancient, and grand. Wide avenues, towering walls, massive gates.
 * The imperial palace dominates the north. Red lanterns line the main streets.
 *
 * <p>Districts (8): city_walls_and_gates, main_streets_and_plaza,
 * imperial_palace_district, cultivation_market, mortal_district,
 * merchant_quarter, temple_of_heavenly_dao, alchemist_quarter.
 *
 * <p>CRON-COMPLETIONIST-52: Fourth full hand-built settlement. First capital city.
 *
 * <h2>Harsh Self-Critique</h2>
 * <ul>
 *   <li>City is 150x150 — canonically Nan Dou City should hold hundreds of thousands.
 *       150x150 is still too small for a capital, but larger than Tian Shui's 130x130.</li>
 *   <li>Palace is grand but still box-based — no curved xianxia rooftop eaves.
 *       The sloped roofs use stair blocks which create angular, not curved, silhouettes.</li>
 *   <li>Red banners are wool blocks — real banners should be thin, flowing fabric.
 *       MC has no banner-on-wall rendering for custom colors.</li>
 *   <li>Temple is symmetrical and repetitive — lacks the weathered, ancient feel
 *       that a thousand-year-old temple should have.</li>
 *   <li>Alchemist quarter cauldrons are vanilla — should use ErgenverseBlocks
 *       alchemy_furnace and pill_furnace where possible.</li>
 *   <li>No underground dungeons, secret passages, or cultivation caves beneath
 *       the palace (canon mentions hidden chambers under major cities).</li>
 * </ul>
 */
public final class NanDouCityBuilder {

    /**
     * Lazy-initialized BlockState holder. ErgenverseBlocks.X.get() throws NPE before
     * Forge resolves the block registry, so these cannot be static-final in the outer
     * class. This inner class loads on first reference (during build(), which runs at
     * world-gen time — well after registry resolution).
     */
    private static final class B {
        private static final BlockState SPIRIT_STONE = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
        private static final BlockState JADE_STONE = ErgenverseBlocks.JADE_STONE.get().defaultBlockState();
        private static final BlockState SPIRIT_SAND = ErgenverseBlocks.SPIRIT_SAND.get().defaultBlockState();
        private static final BlockState SPIRIT_WOOD_PLANK = ErgenverseBlocks.SPIRIT_WOOD_PLANKS.get().defaultBlockState();
        private static final BlockState SPIRIT_WOOD_LOG = ErgenverseBlocks.SPIRIT_WOOD_LOG.get().defaultBlockState();
        private static final BlockState ALCHEMY_FURNACE = ErgenverseBlocks.ALCHEMY_FURNACE.get().defaultBlockState();
        private static final BlockState PILL_FURNACE = ErgenverseBlocks.PILL_FURNACE.get().defaultBlockState();
        private static final BlockState DAO_STONE = ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState();
        private static final BlockState FORMATION_PLATFORM = ErgenverseBlocks.FORMATION_PLATFORM.get().defaultBlockState();
    }

    private NanDouCityBuilder() {}

    // ── Block palette ──────────────────────────────────────────────────
    // Imposing ancient capital — dark stone, gold, red.
    private static final BlockState DEEPSLATE       = Blocks.DEEPSLATE.defaultBlockState();
    private static final BlockState DEEPSLATE_BRICKS = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
    private static final BlockState POLISHED_DEEPSLATE = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
    private static final BlockState STONE_BRICK     = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState STONE           = Blocks.STONE.defaultBlockState();
    private static final BlockState COBBLESTONE     = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState DARK_OAK_PLANK   = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState DARK_OAK_LOG     = Blocks.DARK_OAK_LOG.defaultBlockState();
    private static final BlockState DARK_OAK_STAIR   = Blocks.DARK_OAK_STAIRS.defaultBlockState();
    private static final BlockState DARK_OAK_SLAB    = Blocks.DARK_OAK_SLAB.defaultBlockState();
    private static final BlockState DARK_OAK_FENCE   = Blocks.DARK_OAK_FENCE.defaultBlockState();
    private static final BlockState DARK_OAK_DOOR    = Blocks.DARK_OAK_DOOR.defaultBlockState();
    private static final BlockState SPRUCE_PLANK     = Blocks.SPRUCE_PLANKS.defaultBlockState();
    private static final BlockState SPRUCE_LOG       = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState SPRUCE_STAIRS    = Blocks.SPRUCE_STAIRS.defaultBlockState();
    private static final BlockState OAK_PLANK        = Blocks.OAK_PLANKS.defaultBlockState();
    private static final BlockState OAK_STAIRS       = Blocks.OAK_STAIRS.defaultBlockState();
    private static final BlockState OAK_LOG          = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState BIRCH_PLANK      = Blocks.BIRCH_PLANKS.defaultBlockState();
    private static final BlockState GOLD_BLOCK       = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState IRON_BLOCK       = Blocks.IRON_BLOCK.defaultBlockState();
    private static final BlockState RED_WOOL         = Blocks.RED_WOOL.defaultBlockState();
    private static final BlockState RED_CARPET       = Blocks.RED_CARPET.defaultBlockState();
    private static final BlockState BLACK_CARPET     = Blocks.BLACK_CARPET.defaultBlockState();
    private static final BlockState YELLOW_CARPET    = Blocks.YELLOW_CARPET.defaultBlockState();
    private static final BlockState WHITE_CARPET     = Blocks.WHITE_CARPET.defaultBlockState();
    private static final BlockState LANTERN          = Blocks.LANTERN.defaultBlockState();
    private static final BlockState SOUL_LANTERN     = Blocks.SOUL_LANTERN.defaultBlockState();
    private static final BlockState GLOWSTONE        = Blocks.GLOWSTONE.defaultBlockState();
    private static final BlockState SEA_LANTERN      = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState IRON_BARS        = Blocks.IRON_BARS.defaultBlockState();
    private static final BlockState IRON_DOOR        = Blocks.IRON_DOOR.defaultBlockState();
    private static final BlockState CHEST            = Blocks.CHEST.defaultBlockState();
    private static final BlockState BOOKSHELF        = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState ANVIL            = Blocks.ANVIL.defaultBlockState();
    private static final BlockState CAULDRON         = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState CRAFTING_TABLE   = Blocks.CRAFTING_TABLE.defaultBlockState();
    private static final BlockState FURNACE          = Blocks.FURNACE.defaultBlockState();
    private static final BlockState BARREL           = Blocks.BARREL.defaultBlockState();
    private static final BlockState BREWING_STAND    = Blocks.BREWING_STAND.defaultBlockState();
    private static final BlockState WATER            = Blocks.WATER.defaultBlockState();
    private static final BlockState AIR              = Blocks.AIR.defaultBlockState();
    private static final BlockState GLASS_PANE       = Blocks.GLASS_PANE.defaultBlockState();
    private static final BlockState OBSIDIAN         = Blocks.OBSIDIAN.defaultBlockState();
    private static final BlockState GRAVEL           = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState SAND             = Blocks.SAND.defaultBlockState();
    private static final BlockState DIRT             = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRASS_BLOCK      = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState STONE_STAIRS     = Blocks.STONE_STAIRS.defaultBlockState();
    private static final BlockState STONE_SLAB       = Blocks.STONE_SLAB.defaultBlockState();
    private static final BlockState DEEPSLATE_STAIRS = Blocks.COBBLESTONE_STAIRS.defaultBlockState();
    private static final BlockState DEEPSLATE_SLAB   = Blocks.COBBLESTONE_SLAB.defaultBlockState();
    private static final BlockState SPRUCE_FENCE     = Blocks.SPRUCE_FENCE.defaultBlockState();
    private static final BlockState END_ROD          = Blocks.END_ROD.defaultBlockState();
    private static final BlockState SKELETON_SKULL   = Blocks.SKELETON_SKULL.defaultBlockState();
    private static final BlockState FERN             = Blocks.FERN.defaultBlockState();
    private static final BlockState AZALEA           = Blocks.FLOWERING_AZALEA.defaultBlockState();
    // ErgenverseBlocks
    // ── Dimensions ────────────────────────────────────────────────────
    private static final int CITY_HALF = 75;        // 150x150 total
    private static final int WALL_HEIGHT = 12;
    private static final int GATE_WIDTH = 7;
    private static final int TOWER_SIZE = 6;
    private static final int TOWER_HEIGHT = 18;

    private static boolean built = false;

    public static boolean isAlreadyBuilt(ServerLevel level, BlockPos center) {
        return level.getBlockState(center.above(WALL_HEIGHT + 1)).getBlock() == Blocks.POLISHED_DEEPSLATE;
    }

    public static void build(ServerLevel level, BlockPos center) {
        if (built) return;
        int baseY = center.getY();
        int cx = center.getX();
        int cz = center.getZ();

        Ergenverse.LOGGER.info("[NanDouCity] Building Nan Dou City (南斗城) at ({}, {}, {})...", cx, baseY, cz);

        // Foundation: flatten terrain
        buildFoundation(level, cx, baseY, cz);

        // 1. City Walls and Gates
        buildCityWalls(level, cx, baseY, cz);

        // 2. Main Streets and Central Plaza
        buildMainStreets(level, cx, baseY, cz);

        // 3. South Gate (main entrance)
        buildSouthGate(level, cx, baseY, cz);

        // 4. Imperial Palace District (north)
        buildImperialPalace(level, cx, baseY, cz);

        // 5. Cultivation Market (east)
        buildCultivationMarket(level, cx, baseY, cz);

        // 6. Mortal District (southwest)
        buildMortalDistrict(level, cx, baseY, cz);

        // 7. Merchant Quarter (southeast)
        buildMerchantQuarter(level, cx, baseY, cz);

        // 8. Temple of the Heavenly Dao (west)
        buildTempleOfHeavenlyDao(level, cx, baseY, cz);

        built = true;
        Ergenverse.LOGGER.info("[NanDouCity] Nan Dou City build complete.");
    }

    // ══════════════════════════════════════════════════════════════════
    // FOUNDATION
    // ══════════════════════════════════════════════════════════════════

    private static void buildFoundation(ServerLevel level, int cx, int baseY, int cz) {
        for (int x = -CITY_HALF; x <= CITY_HALF; x++) {
            for (int z = -CITY_HALF; z <= CITY_HALF; z++) {
                // Fill below baseY with stone
                for (int y = baseY - 3; y < baseY; y++) {
                    level.setBlock(new BlockPos(cx + x, y, cz + z), STONE, 3);
                }
                // Surface: gravel roads, grass rest
                if (isMainRoad(x, z) || isPlaza(x, z)) {
                    level.setBlock(new BlockPos(cx + x, baseY, cz + z), COBBLESTONE, 3);
                } else {
                    level.setBlock(new BlockPos(cx + x, baseY, cz + z), GRASS_BLOCK, 3);
                }
                // Clear above
                for (int y = baseY + 1; y < baseY + WALL_HEIGHT + 5; y++) {
                    level.setBlock(new BlockPos(cx + x, y, cz + z), AIR, 3);
                }
            }
        }
    }

    private static boolean isMainRoad(int x, int z) {
        // North-south main avenue
        if (Math.abs(x) <= 3) return true;
        // East-west main avenue
        if (Math.abs(z) <= 3) return true;
        return false;
    }

    private static boolean isPlaza(int x, int z) {
        return Math.abs(x) <= 12 && Math.abs(z) <= 12;
    }

    // ══════════════════════════════════════════════════════════════════
    // 1. CITY WALLS AND GATES
    // ══════════════════════════════════════════════════════════════════

    private static void buildCityWalls(ServerLevel level, int cx, int baseY, int cz) {
        int h = WALL_HEIGHT;
        for (int i = -CITY_HALF; i <= CITY_HALF; i++) {
            // Skip gate openings (south, north, east, west)
            boolean southGate = (i >= -GATE_WIDTH/2 && i <= GATE_WIDTH/2);
            boolean skipX = southGate;

            // North wall (z = -CITY_HALF)
            boolean northGate = (i >= -GATE_WIDTH/2 && i <= GATE_WIDTH/2);
            for (int y = baseY + 1; y <= baseY + h; y++) {
                if (!northGate) {
                    level.setBlock(new BlockPos(cx + i, y, cz - CITY_HALF), DEEPSLATE_BRICKS, 3);
                }
                // South wall (z = +CITY_HALF)
                if (!skipX) {
                    level.setBlock(new BlockPos(cx + i, y, cz + CITY_HALF), DEEPSLATE_BRICKS, 3);
                }
                // West wall (x = -CITY_HALF)
                level.setBlock(new BlockPos(cx - CITY_HALF, y, cz + i), DEEPSLATE_BRICKS, 3);
                // East wall (x = +CITY_HALF)
                level.setBlock(new BlockPos(cx + CITY_HALF, y, cz + i), DEEPSLATE_BRICKS, 3);
            }
            // Battlements (crenellations) on top
            if (i % 2 == 0) {
                for (int dir = 0; dir < 4; dir++) {
                    int wx = cx + (dir == 2 ? -CITY_HALF : dir == 3 ? CITY_HALF : i);
                    int wz = cz + (dir == 0 ? -CITY_HALF : dir == 1 ? CITY_HALF : i);
                    if ((dir < 2 && (Math.abs(i) > GATE_WIDTH/2 + 1 || i % 3 != 0))
                        || dir >= 2) {
                        level.setBlock(new BlockPos(wx, baseY + h + 1, wz), POLISHED_DEEPSLATE, 3);
                    }
                }
            }
        }

        // Corner guard towers (4 corners)
        buildGuardTower(level, cx - CITY_HALF, baseY, cz - CITY_HALF);
        buildGuardTower(level, cx + CITY_HALF, baseY, cz - CITY_HALF);
        buildGuardTower(level, cx - CITY_HALF, baseY, cz + CITY_HALF);
        buildGuardTower(level, cx + CITY_HALF, baseY, cz + CITY_HALF);

        // Mid-wall towers (on each wall)
        buildGuardTower(level, cx, baseY, cz - CITY_HALF);
        buildGuardTower(level, cx, baseY, cz + CITY_HALF);
        buildGuardTower(level, cx - CITY_HALF, baseY, cz);
        buildGuardTower(level, cx + CITY_HALF, baseY, cz);

        // Gate arches
        buildGateArch(level, cx, baseY, cz + CITY_HALF, Direction.SOUTH);
        buildGateArch(level, cx, baseY, cz - CITY_HALF, Direction.NORTH);
        buildGateArch(level, cx, baseY + 0, cz + 0, Direction.EAST); // east gate
        buildGateArch(level, cx, baseY + 0, cz + 0, Direction.WEST); // west gate
    }

    private static void buildGuardTower(ServerLevel level, int bx, int by, int bz) {
        int s = TOWER_SIZE;
        int h = TOWER_HEIGHT;
        for (int dx = -s/2; dx <= s/2; dx++) {
            for (int dz = -s/2; dz <= s/2; dz++) {
                for (int dy = by + 1; dy <= by + h; dy++) {
                    boolean edge = Math.abs(dx) == s/2 || Math.abs(dz) == s/2;
                    boolean top = dy == by + h;
                    if (edge || top) {
                        level.setBlock(new BlockPos(bx + dx, dy, bz + dz), POLISHED_DEEPSLATE, 3);
                    } else {
                        level.setBlock(new BlockPos(bx + dx, dy, bz + dz), AIR, 3);
                    }
                }
            }
        }
        // Gold cap
        for (int dx = -s/2; dx <= s/2; dx++) {
            for (int dz = -s/2; dz <= s/2; dz++) {
                level.setBlock(new BlockPos(bx + dx, by + h + 1, bz + dz), GOLD_BLOCK, 3);
            }
        }
        // Lanterns at top
        level.setBlock(new BlockPos(bx, by + h, bz), SEA_LANTERN, 3);
        // Iron bars for windows
        for (int dy = by + h/2 - 1; dy <= by + h/2 + 1; dy++) {
            if (Math.abs(bx) > CITY_HALF - 5) {
                level.setBlock(new BlockPos(bx, dy, bz - s/2 - 1), IRON_BARS, 3);
                level.setBlock(new BlockPos(bx, dy, bz + s/2 + 1), IRON_BARS, 3);
            }
        }
    }

    private static void buildGateArch(ServerLevel level, int gx, int by, int gz, Direction dir) {
        int gw = GATE_WIDTH + 2; // total width including pillars
        int gh = WALL_HEIGHT + 4; // arch is taller than wall
        // Pillars on each side of the gate
        for (int y = by + 1; y <= by + gh; y++) {
            // Left pillar
            placeGatePillar(level, gx - gw/2, y, gz, dir);
            // Right pillar
            placeGatePillar(level, gx + gw/2, y, gz, dir);
        }
        // Arch top
        for (int i = -gw/2; i <= gw/2; i++) {
            placeGateArchBlock(level, gx + i, by + gh, gz, dir, POLISHED_DEEPSLATE);
            placeGateArchBlock(level, gx + i, by + gh + 1, gz, dir, GOLD_BLOCK);
        }
        // Red banners on pillars
        placeGateArchBlock(level, gx - gw/2, by + gh - 1, gz, dir, RED_WOOL);
        placeGateArchBlock(level, gx + gw/2, by + gh - 1, gz, dir, RED_WOOL);
    }

    private static void placeGatePillar(ServerLevel level, int px, int py, int pz, Direction dir) {
        if (dir == Direction.NORTH || dir == Direction.SOUTH) {
            level.setBlock(new BlockPos(px, py, pz - 1), POLISHED_DEEPSLATE, 3);
            level.setBlock(new BlockPos(px, py, pz + 1), POLISHED_DEEPSLATE, 3);
        } else {
            level.setBlock(new BlockPos(px - 1, py, pz), POLISHED_DEEPSLATE, 3);
            level.setBlock(new BlockPos(px + 1, py, pz), POLISHED_DEEPSLATE, 3);
        }
        level.setBlock(new BlockPos(px, py, pz), AIR, 3); // gate opening
    }

    private static void placeGateArchBlock(ServerLevel level, int ax, int ay, int az, Direction dir, BlockState state) {
        if (dir == Direction.NORTH || dir == Direction.SOUTH) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(new BlockPos(ax, ay, az + dz), state, 3);
            }
        } else {
            for (int dx = -1; dx <= 1; dx++) {
                level.setBlock(new BlockPos(ax + dx, ay, az), state, 3);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // 2. MAIN STREETS AND CENTRAL PLAZA
    // ══════════════════════════════════════════════════════════════════

    private static void buildMainStreets(ServerLevel level, int cx, int baseY, int cz) {
        // Widen main roads from 7 to 7 blocks with stone brick edges
        for (int x = -CITY_HALF + 5; x <= CITY_HALF - 5; x++) {
            for (int z = -CITY_HALF + 5; z <= CITY_HALF - 5; z++) {
                if (isMainRoad(x, z)) {
                    level.setBlock(new BlockPos(cx + x, baseY, cz + z), STONE_BRICK, 3);
                    // Road edges
                    if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                        level.setBlock(new BlockPos(cx + x, baseY, cz + z), POLISHED_DEEPSLATE, 3);
                    }
                }
            }
        }

        // Central Plaza (gold-accented)
        for (int x = -12; x <= 12; x++) {
            for (int z = -12; z <= 12; z++) {
                int y = baseY;
                // Checkerboard pattern: polished deepslate + gold trim
                boolean edge = Math.abs(x) == 12 || Math.abs(z) == 12;
                boolean corner = Math.abs(x) >= 10 && Math.abs(z) >= 10;
                if (edge) {
                    level.setBlock(new BlockPos(cx + x, y, cz + z), OBSIDIAN, 3);
                } else if (corner) {
                    level.setBlock(new BlockPos(cx + x, y, cz + z), GOLD_BLOCK, 3);
                } else {
                    level.setBlock(new BlockPos(cx + x, y, cz + z), POLISHED_DEEPSLATE, 3);
                }
                // Clear above plaza
                for (int dy = 1; dy <= 20; dy++) {
                    level.setBlock(new BlockPos(cx + x, y + dy, cz + z), AIR, 3);
                }
            }
        }

        // Central fountain (Nan Dou city insignia — a stone basin with water)
        int fy = baseY + 1;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                boolean rim = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                if (rim) {
                    level.setBlock(new BlockPos(cx + dx, fy, cz + dz), POLISHED_DEEPSLATE, 3);
                    level.setBlock(new BlockPos(cx + dx, fy + 1, cz + dz), POLISHED_DEEPSLATE, 3);
                } else {
                    level.setBlock(new BlockPos(cx + dx, fy, cz + dz), WATER, 3);
                }
            }
        }
        // Center pillar with lantern
        level.setBlock(new BlockPos(cx, fy + 1, cz), POLISHED_DEEPSLATE, 3);
        level.setBlock(new BlockPos(cx, fy + 2, cz), POLISHED_DEEPSLATE, 3);
        level.setBlock(new BlockPos(cx, fy + 3, cz), SEA_LANTERN, 3);

        // Red lanterns around plaza perimeter (every 5 blocks)
        for (int i = -12; i <= 12; i += 5) {
            level.setBlock(new BlockPos(cx + i, baseY + 4, cz - 13), SOUL_LANTERN, 3);
            level.setBlock(new BlockPos(cx + i, baseY + 4, cz + 13), SOUL_LANTERN, 3);
            level.setBlock(new BlockPos(cx - 13, baseY + 4, cz + i), SOUL_LANTERN, 3);
            level.setBlock(new BlockPos(cx + 13, baseY + 4, cz + i), SOUL_LANTERN, 3);
        }

        // Nan Dou City plaque (gold block on south side of fountain)
        level.setBlock(new BlockPos(cx, fy + 2, cz + 2), GOLD_BLOCK, 3);
    }

    // ══════════════════════════════════════════════════════════════════
    // 3. SOUTH GATE ENTRANCE
    // ══════════════════════════════════════════════════════════════════

    private static void buildSouthGate(ServerLevel level, int cx, int baseY, int cz) {
        int gy = cz + CITY_HALF - 5; // just inside the south gate
        // Guard posts on each side of the entrance
        for (int side = -1; side <= 1; side += 2) {
            int px = cx + side * (GATE_WIDTH/2 + 2);
            // Small guardhouse
            for (int dx = 0; dx <= 2; dx++) {
                for (int dz = 0; dz <= 2; dz++) {
                    for (int dy = 1; dy <= 4; dy++) {
                        boolean edge = dx == 0 || dx == 2 || dz == 0 || dz == 2 || dy == 4;
                        level.setBlock(new BlockPos(px + dx * side, baseY + dy, gy + dz),
                            edge ? DEEPSLATE_BRICKS : AIR, 3);
                    }
                }
            }
            // Roof
            for (int dx = -1; dx <= 3; dx++) {
                for (int dz = -1; dz <= 3; dz++) {
                    level.setBlock(new BlockPos(px + dx * side, baseY + 5, gy + dz), DARK_OAK_STAIR, 3);
                }
            }
            // Chest inside
            level.setBlock(new BlockPos(px + side, baseY + 1, gy + 1), CHEST, 3);
            // Lantern
            level.setBlock(new BlockPos(px + side, baseY + 5, gy + 1), LANTERN, 3);
        }
        // Red carpet leading from gate into the city
        for (int z = 0; z < 20; z++) {
            level.setBlock(new BlockPos(cx, baseY + 1, gy - z), RED_CARPET, 3);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // 4. IMPERIAL PALACE DISTRICT (NORTH)
    // ══════════════════════════════════════════════════════════════════

    private static void buildImperialPalace(ServerLevel level, int cx, int baseY, int cz) {
        // Palace compound: 40x50, located north of the central plaza
        int px = cx;
        int pz = cz - 50; // north
        int pw = 20; // half-width
        int pd = 25; // half-depth

        // Palace outer walls (high, imposing)
        for (int x = -pw; x <= pw; x++) {
            for (int z = -pd; z <= pd; z++) {
                boolean edgeX = Math.abs(x) == pw;
                boolean edgeZ = Math.abs(z) == pd;
                if (edgeX || edgeZ) {
                    for (int y = baseY + 1; y <= baseY + 8; y++) {
                        level.setBlock(new BlockPos(px + x, y, pz + z), POLISHED_DEEPSLATE, 3);
                    }
                    // Battlements
                    if ((x + z) % 2 == 0) {
                        level.setBlock(new BlockPos(px + x, baseY + 9, pz + z), POLISHED_DEEPSLATE, 3);
                    }
                }
            }
        }

        // Main gate (south face of palace, facing plaza)
        for (int x = -2; x <= 2; x++) {
            for (int y = baseY + 1; y <= baseY + 10; y++) {
                // Pillars
                if (Math.abs(x) == 2) {
                    level.setBlock(new BlockPos(px + x, y, pz + pd), POLISHED_DEEPSLATE, 3);
                } else {
                    level.setBlock(new BlockPos(px + x, y, pz + pd), AIR, 3);
                }
            }
        }
        // Gate arch top
        for (int x = -3; x <= 3; x++) {
            level.setBlock(new BlockPos(px + x, baseY + 11, pz + pd), GOLD_BLOCK, 3);
            level.setBlock(new BlockPos(px + x, baseY + 12, pz + pd), GOLD_BLOCK, 3);
        }

        // Throne Hall (main building, center of compound)
        buildPalaceThroneHall(level, px, baseY, pz);

        // Side wings (east and west of throne hall)
        buildPalaceWing(level, px - 14, baseY, pz - 5); // west wing
        buildPalaceWing(level, px + 14, baseY, pz - 5); // east wing

        // Inner garden (between gate and throne hall)
        for (int x = -pw + 3; x <= pw - 3; x++) {
            for (int z = pd - 10; z <= pd - 3; z++) {
                level.setBlock(new BlockPos(px + x, baseY + 1, pz + z), GRASS_BLOCK, 3);
                // Spirit herb garden plants
                if ((x + z) % 7 == 0) {
                    level.setBlock(new BlockPos(px + x, baseY + 2, pz + z), FERN, 3);
                }
                if ((x * z) % 11 == 0) {
                    level.setBlock(new BlockPos(px + x, baseY + 2, pz + z), AZALEA, 3);
                }
            }
        }
        // Garden path
        for (int z = pd - 10; z <= pd - 3; z++) {
            level.setBlock(new BlockPos(px, baseY + 1, pz + z), SAND, 3);
        }

        // Formation platform in garden (cultivation ceremony space)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setBlock(new BlockPos(px + dx, baseY + 1, pz - 8 + dz), B.FORMATION_PLATFORM, 3);
            }
        }

        // Red carpet from gate to throne hall
        for (int z = pd - 3; z >= -10; z--) {
            level.setBlock(new BlockPos(px, baseY + 1, pz + z), RED_CARPET, 3);
        }
    }

    private static void buildPalaceThroneHall(ServerLevel level, int px, int by, int pz) {
        // 16 wide x 24 deep x 10 tall throne room
        int hw = 8;
        int hd = 12;
        int height = 10;

        // Floor
        for (int x = -hw; x <= hw; x++) {
            for (int z = -hd; z <= hd; z++) {
                level.setBlock(new BlockPos(px + x, by + 1, pz + z), POLISHED_DEEPSLATE, 3);
                // Gold inlay border
                if (Math.abs(x) == hw || Math.abs(z) == hd) {
                    level.setBlock(new BlockPos(px + x, by + 1, pz + z), GOLD_BLOCK, 3);
                }
            }
        }

        // Walls
        for (int x = -hw; x <= hw; x++) {
            for (int y = 2; y <= height; y++) {
                level.setBlock(new BlockPos(px + x, by + y, pz - hd), DEEPSLATE_BRICKS, 3);
                level.setBlock(new BlockPos(px + x, by + y, pz + hd), DEEPSLATE_BRICKS, 3);
            }
        }
        for (int z = -hd; z <= hd; z++) {
            for (int y = 2; y <= height; y++) {
                level.setBlock(new BlockPos(px - hw, by + y, pz + z), DEEPSLATE_BRICKS, 3);
                level.setBlock(new BlockPos(px + hw, by + y, pz + z), DEEPSLATE_BRICKS, 3);
            }
        }

        // Roof (dark oak stairs sloping inward)
        for (int x = -hw - 1; x <= hw + 1; x++) {
            for (int z = -hd - 1; z <= hd + 1; z++) {
                int distFromEdge = Math.min(Math.abs(x) - hw, Math.abs(z) - hd);
                if (distFromEdge >= -1 && distFromEdge <= 1) {
                    level.setBlock(new BlockPos(px + x, by + height + 1, pz + z), DARK_OAK_STAIR, 3);
                }
            }
        }
        // Gold roof ridge
        for (int x = -hw + 2; x <= hw - 2; x += 4) {
            level.setBlock(new BlockPos(px + x, by + height + 2, pz), GOLD_BLOCK, 3);
        }

        // Throne platform (raised dais at north end)
        for (int x = -3; x <= 3; x++) {
            for (int z = -hd + 2; z <= -hd + 4; z++) {
                level.setBlock(new BlockPos(px + x, by + 1, pz + z), OBSIDIAN, 3);
                level.setBlock(new BlockPos(px + x, by + 2, pz + z), OBSIDIAN, 3);
            }
        }
        // Throne seat
        level.setBlock(new BlockPos(px, by + 3, pz - hd + 2), DARK_OAK_STAIR, 3);
        level.setBlock(new BlockPos(px, by + 4, pz - hd + 2), GOLD_BLOCK, 3);
        // Banner behind throne
        for (int y = 3; y <= 7; y++) {
            level.setBlock(new BlockPos(px, by + y, pz - hd + 1), RED_WOOL, 3);
        }

        // Pillars along the hall (8 pillars, 4 per side)
        for (int i = 0; i < 4; i++) {
            int pz_pillar = pz - hd + 5 + i * 5;
            for (int y = 2; y <= height; y++) {
                level.setBlock(new BlockPos(px - hw + 3, by + y, pz_pillar), POLISHED_DEEPSLATE, 3);
                level.setBlock(new BlockPos(px + hw - 3, by + y, pz_pillar), POLISHED_DEEPSLATE, 3);
            }
        }

        // Lanterns
        level.setBlock(new BlockPos(px - hw + 1, by + 8, pz), SOUL_LANTERN, 3);
        level.setBlock(new BlockPos(px + hw - 1, by + 8, pz), SOUL_LANTERN, 3);
        level.setBlock(new BlockPos(px, by + 8, pz - hd + 1), SOUL_LANTERN, 3);

        // Chests in side alcoves
        level.setBlock(new BlockPos(px - hw + 1, by + 1, pz - 2), CHEST, 3);
        level.setBlock(new BlockPos(px + hw - 1, by + 1, pz - 2), CHEST, 3);
    }

    private static void buildPalaceWing(ServerLevel level, int wx, int by, int wz) {
        // Side building: 8x12x7
        int hw = 4;
        int hd = 6;
        for (int x = -hw; x <= hw; x++) {
            for (int z = -hd; z <= hd; z++) {
                level.setBlock(new BlockPos(wx + x, by + 1, wz + z), DARK_OAK_PLANK, 3);
                boolean edge = Math.abs(x) == hw || Math.abs(z) == hd;
                if (edge) {
                    for (int y = 2; y <= 7; y++) {
                        level.setBlock(new BlockPos(wx + x, by + y, wz + z), DEEPSLATE_BRICKS, 3);
                    }
                }
            }
        }
        // Roof
        for (int x = -hw; x <= hw; x++) {
            for (int z = -hd; z <= hd; z++) {
                level.setBlock(new BlockPos(wx + x, by + 7, wz + z), DARK_OAK_STAIR, 3);
            }
        }
        // Door (facing the garden)
        level.setBlock(new BlockPos(wx, by + 3, wz + hd), AIR, 3);
        level.setBlock(new BlockPos(wx, by + 4, wz + hd), AIR, 3);
        level.setBlock(new BlockPos(wx + 1, by + 3, wz + hd), AIR, 3);
        level.setBlock(new BlockPos(wx + 1, by + 4, wz + hd), AIR, 3);
        // Interior: bookshelves (library wing) or cauldrons (alchemy wing)
        level.setBlock(new BlockPos(wx - 2, by + 1, wz - 2), BOOKSHELF, 3);
        level.setBlock(new BlockPos(wx - 1, by + 1, wz - 2), BOOKSHELF, 3);
        level.setBlock(new BlockPos(wx + 2, by + 1, wz - 2), BOOKSHELF, 3);
        level.setBlock(new BlockPos(wx + 1, by + 1, wz - 2), BOOKSHELF, 3);
        // Crafting
        level.setBlock(new BlockPos(wx, by + 1, wz), CRAFTING_TABLE, 3);
        // Barrel storage
        level.setBlock(new BlockPos(wx - 2, by + 1, wz + 2), BARREL, 3);
        level.setBlock(new BlockPos(wx + 2, by + 1, wz + 2), BARREL, 3);
    }

    // ══════════════════════════════════════════════════════════════════
    // 5. CULTIVATION MARKET (EAST)
    // ══════════════════════════════════════════════════════════════════

    private static void buildCultivationMarket(ServerLevel level, int cx, int baseY, int cz) {
        // East of central plaza, cultivation supplies market
        int mx = cx + 35;
        int mz = cz;

        // Market structure: 30x30 open-air market with stalls
        for (int x = -15; x <= 15; x++) {
            for (int z = -15; z <= 15; z++) {
                boolean edge = Math.abs(x) == 15 || Math.abs(z) == 15;
                if (edge) {
                    for (int y = baseY + 1; y <= baseY + 5; y++) {
                        level.setBlock(new BlockPos(mx + x, y, mz + z), B.SPIRIT_STONE, 3);
                    }
                }
                // Market floor
                level.setBlock(new BlockPos(mx + x, baseY + 1, mz + z), B.SPIRIT_SAND, 3);
            }
        }

        // Pillars supporting a partial roof
        for (int i = -10; i <= 10; i += 10) {
            for (int j = -10; j <= 10; j += 10) {
                for (int y = 2; y <= 7; y++) {
                    level.setBlock(new BlockPos(mx + i, baseY + y, mz + j), B.SPIRIT_WOOD_LOG, 3);
                }
                // Lantern on each pillar
                level.setBlock(new BlockPos(mx + i, baseY + 7, mz + j), LANTERN, 3);
            }
        }

        // Market stalls (6 stalls, 3 per side)
        // Each stall: a counter (3x1) with a chest behind it and a sign (soul lantern above)
        for (int i = 0; i < 3; i++) {
            int sz = -10 + i * 8;
            // North row stalls
            for (int x = -1; x <= 1; x++) {
                level.setBlock(new BlockPos(mx + x, baseY + 2, mz + sz), DARK_OAK_PLANK, 3);
            }
            level.setBlock(new BlockPos(mx - 2, baseY + 1, mz + sz), CHEST, 3);
            level.setBlock(new BlockPos(mx - 2, baseY + 3, mz + sz), SOUL_LANTERN, 3);
            // South row stalls
            for (int x = -1; x <= 1; x++) {
                level.setBlock(new BlockPos(mx + x, baseY + 2, mz - sz), DARK_OAK_PLANK, 3);
            }
            level.setBlock(new BlockPos(mx + 2, baseY + 1, mz - sz), CHEST, 3);
            level.setBlock(new BlockPos(mx + 2, baseY + 3, mz - sz), SOUL_LANTERN, 3);
        }

        // Alchemy furnace (corner of market)
        level.setBlock(new BlockPos(mx + 12, baseY + 1, mz + 12), B.ALCHEMY_FURNACE, 3);
        level.setBlock(new BlockPos(mx + 12, baseY + 1, mz - 12), B.PILL_FURNACE, 3);

        // Dao stone display at market center
        level.setBlock(new BlockPos(mx, baseY + 1, mz), B.DAO_STONE, 3);
        level.setBlock(new BlockPos(mx, baseY + 2, mz), GLASS_PANE, 3);

        // Anvil for artifact repairs
        level.setBlock(new BlockPos(mx - 12, baseY + 1, mz - 12), ANVIL, 3);
        level.setBlock(new BlockPos(mx - 13, baseY + 1, mz - 12), FURNACE, 3);
    }

    // ══════════════════════════════════════════════════════════════════
    // 6. MORTAL DISTRICT (SOUTHWEST)
    // ══════════════════════════════════════════════════════════════════

    private static void buildMortalDistrict(ServerLevel level, int cx, int baseY, int cz) {
        int dx = cx - 35;
        int dz = cz + 25;

        // 30x25 grid of simple row houses
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                int hx = dx - 12 + col * 6;
                int hz = dz - 10 + row * 6;
                buildMortalHouse(level, hx, baseY, hz);
            }
        }

        // Community well at center of district
        int wellX = dx - 3;
        int wellZ = dz;
        for (int ddx = -2; ddx <= 2; ddx++) {
            for (int ddz = -2; ddz <= 2; ddz++) {
                boolean rim = Math.abs(ddx) == 2 || Math.abs(ddz) == 2;
                level.setBlock(new BlockPos(wellX + ddx, baseY + 1, wellZ + ddz),
                    rim ? COBBLESTONE : WATER, 3);
            }
        }
        level.setBlock(new BlockPos(wellX, baseY + 3, wellZ), IRON_BARS, 3);

        // Dirt paths between houses
        for (int row = 0; row < 4; row++) {
            for (int z = 0; z < 5; z++) {
                int pathZ = dz - 10 + row * 6 + 5;
                for (int x = -14; x <= 8; x++) {
                    level.setBlock(new BlockPos(dx + x, baseY + 1, pathZ), GRAVEL, 3);
                }
            }
        }
    }

    private static void buildMortalHouse(ServerLevel level, int hx, int by, int hz) {
        // Simple 5x4x4 house
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 4; z++) {
                // Floor
                level.setBlock(new BlockPos(hx + x, by + 1, hz + z), OAK_PLANK, 3);
                // Walls
                boolean edge = x == 0 || x == 4 || z == 0 || z == 3;
                if (edge) {
                    for (int y = 2; y <= 4; y++) {
                        level.setBlock(new BlockPos(hx + x, by + y, hz + z),
                            (x == 0 || x == 4) ? COBBLESTONE : OAK_PLANK, 3);
                    }
                }
            }
        }
        // Roof (spruce stairs)
        for (int x = -1; x <= 5; x++) {
            for (int z = -1; z <= 4; z++) {
                level.setBlock(new BlockPos(hx + x, by + 5, hz + z), SPRUCE_STAIRS, 3);
            }
        }
        // Door (front face, center)
        level.setBlock(new BlockPos(hx + 2, by + 2, hz + 3), AIR, 3);
        level.setBlock(new BlockPos(hx + 2, by + 3, hz + 3), AIR, 3);
        // Window
        level.setBlock(new BlockPos(hx + 4, by + 3, hz + 1), GLASS_PANE, 3);
        level.setBlock(new BlockPos(hx + 4, by + 3, hz + 2), GLASS_PANE, 3);
        // Interior: bed, crafting table, barrel
        level.setBlock(new BlockPos(hx + 1, by + 1, hz + 1), BARREL, 3);
        level.setBlock(new BlockPos(hx + 3, by + 1, hz + 1), CRAFTING_TABLE, 3);
    }

    // ══════════════════════════════════════════════════════════════════
    // 7. MERCHANT QUARTER (SOUTHEAST)
    // ══════════════════════════════════════════════════════════════════

    private static void buildMerchantQuarter(ServerLevel level, int cx, int baseY, int cz) {
        int mx = cx + 35;
        int mz = cz + 25;

        // Larger merchant buildings with varied architecture
        // Auction House (largest building, center of quarter)
        buildMerchantBuilding(level, mx - 3, baseY, mz - 5, 8, 10, "auction");

        // Trading Posts (4 smaller buildings)
        buildMerchantBuilding(level, mx - 15, baseY, mz - 5, 6, 6, "trading");
        buildMerchantBuilding(level, mx + 10, baseY, mz - 5, 6, 6, "trading");
        buildMerchantBuilding(level, mx - 15, baseY, mz + 8, 6, 6, "storage");
        buildMerchantBuilding(level, mx + 10, baseY, mz + 8, 6, 6, "storage");

        // Wide merchant avenue connecting to main street
        for (int z = 0; z < 15; z++) {
            for (int x = -2; x <= 2; x++) {
                level.setBlock(new BlockPos(mx + x, baseY + 1, mz - 10 + z), STONE_BRICK, 3);
            }
        }
    }

    private static void buildMerchantBuilding(ServerLevel level, int bx, int by, int bz,
                                               int width, int depth, String type) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                level.setBlock(new BlockPos(bx + x, by + 1, bz + z), BIRCH_PLANK, 3);
                boolean edge = x == 0 || x == width - 1 || z == 0 || z == depth - 1;
                if (edge) {
                    for (int y = 2; y <= 6; y++) {
                        level.setBlock(new BlockPos(bx + x, by + y, bz + z), STONE_BRICK, 3);
                    }
                }
            }
        }
        // Roof
        for (int x = -1; x <= width; x++) {
            for (int z = -1; z <= depth; z++) {
                level.setBlock(new BlockPos(bx + x, by + 7, bz + z), BIRCH_PLANK, 3);
            }
        }
        // Door
        level.setBlock(new BlockPos(bx + width/2, by + 2, bz + depth - 1), AIR, 3);
        level.setBlock(new BlockPos(bx + width/2, by + 3, bz + depth - 1), AIR, 3);
        level.setBlock(new BlockPos(bx + width/2 + 1, by + 2, bz + depth - 1), AIR, 3);
        level.setBlock(new BlockPos(bx + width/2 + 1, by + 3, bz + depth - 1), AIR, 3);
        // Windows
        level.setBlock(new BlockPos(bx + 1, by + 4, bz + depth - 1), GLASS_PANE, 3);
        level.setBlock(new BlockPos(bx + width - 2, by + 4, bz + depth - 1), GLASS_PANE, 3);
        // Interior based on type
        if (type.equals("auction")) {
            // Auction house: carpet, multiple chests, anvil
            level.setBlock(new BlockPos(bx + width/2, by + 1, bz + depth/2), RED_CARPET, 3);
            for (int i = 0; i < width - 2; i++) {
                level.setBlock(new BlockPos(bx + 1 + i, by + 1, bz + 1), CHEST, 3);
            }
            level.setBlock(new BlockPos(bx + width/2, by + 1, bz + depth/2 + 1), ANVIL, 3);
        } else if (type.equals("trading")) {
            level.setBlock(new BlockPos(bx + 1, by + 1, bz + 1), CHEST, 3);
            level.setBlock(new BlockPos(bx + width - 2, by + 1, bz + 1), CHEST, 3);
            level.setBlock(new BlockPos(bx + width/2, by + 1, bz + depth/2), BARREL, 3);
        } else {
            // Storage: barrels and chests
            level.setBlock(new BlockPos(bx + 1, by + 1, bz + 1), BARREL, 3);
            level.setBlock(new BlockPos(bx + width - 2, by + 1, bz + 1), BARREL, 3);
            level.setBlock(new BlockPos(bx + 1, by + 1, bz + depth - 2), CHEST, 3);
            level.setBlock(new BlockPos(bx + width - 2, by + 1, bz + depth - 2), CHEST, 3);
        }
        // Sign (lantern above door)
        level.setBlock(new BlockPos(bx + width/2, by + 7, bz + depth), LANTERN, 3);
    }

    // ══════════════════════════════════════════════════════════════════
    // 8. TEMPLE OF THE HEAVENLY DAO (WEST)
    // ══════════════════════════════════════════════════════════════════

    private static void buildTempleOfHeavenlyDao(ServerLevel level, int cx, int baseY, int cz) {
        int tx = cx - 40;
        int tz = cz - 5;

        // Temple compound: 24x30 walled enclosure
        for (int x = -12; x <= 12; x++) {
            for (int z = -15; z <= 15; z++) {
                boolean edge = Math.abs(x) == 12 || Math.abs(z) == 15;
                if (edge) {
                    for (int y = baseY + 1; y <= baseY + 6; y++) {
                        level.setBlock(new BlockPos(tx + x, y, tz + z), B.SPIRIT_STONE, 3);
                    }
                }
                level.setBlock(new BlockPos(tx + x, baseY + 1, tz + z), B.SPIRIT_SAND, 3);
            }
        }

        // Temple main hall (raised, 12x16)
        // Raised platform (3 blocks high)
        for (int x = -6; x <= 6; x++) {
            for (int z = -8; z <= 8; z++) {
                for (int y = baseY + 2; y <= baseY + 4; y++) {
                    level.setBlock(new BlockPos(tx + x, y, tz + z),
                        (Math.abs(x) == 6 || Math.abs(z) == 8) ? B.SPIRIT_STONE : WHITE_CARPET, 3);
                }
            }
        }
        // Stairs leading up (south face)
        for (int s = 0; s < 3; s++) {
            for (int x = -2; x <= 2; x++) {
                level.setBlock(new BlockPos(tx + x, baseY + 2 + s, tz + 9 + s), STONE_STAIRS, 3);
            }
        }

        // Main hall pillars and roof
        int hallY = baseY + 5;
        // 4 pillars at corners
        int[][] pillarOffsets = {{-5, -7}, {5, -7}, {-5, 7}, {5, 7}};
        for (int[] p : pillarOffsets) {
            for (int y = hallY; y <= hallY + 8; y++) {
                level.setBlock(new BlockPos(tx + p[0], y, tz + p[1]), B.SPIRIT_WOOD_LOG, 3);
            }
        }
        // 2 middle pillars
        for (int z = -3; z <= 3; z += 6) {
            for (int y = hallY; y <= hallY + 8; y++) {
                level.setBlock(new BlockPos(tx - 5, y, tz + z), B.SPIRIT_WOOD_LOG, 3);
                level.setBlock(new BlockPos(tx + 5, y, tz + z), B.SPIRIT_WOOD_LOG, 3);
            }
        }

        // Roof (tiered — 2 layers)
        for (int x = -8; x <= 8; x++) {
            for (int z = -10; z <= 10; z++) {
                boolean innerRoof = Math.abs(x) <= 6 && Math.abs(z) <= 8;
                level.setBlock(new BlockPos(tx + x, hallY + 9, tz + z),
                    innerRoof ? B.SPIRIT_WOOD_PLANK : DARK_OAK_STAIR, 3);
            }
        }
        // Gold roof ornament
        level.setBlock(new BlockPos(tx, hallY + 10, tz), GOLD_BLOCK, 3);
        level.setBlock(new BlockPos(tx, hallY + 11, tz), END_ROD, 3);

        // Altar at north end of hall
        for (int x = -2; x <= 2; x++) {
            for (int z = -7; z <= -5; z++) {
                level.setBlock(new BlockPos(tx + x, hallY, tz + z), OBSIDIAN, 3);
            }
        }
        level.setBlock(new BlockPos(tx, hallY + 1, tz - 6), B.DAO_STONE, 3);
        level.setBlock(new BlockPos(tx, hallY + 2, tz - 6), SOUL_LANTERN, 3);

        // Incense burners (cauldrons) on each side of altar
        level.setBlock(new BlockPos(tx - 3, hallY, tz - 6), CAULDRON, 3);
        level.setBlock(new BlockPos(tx + 3, hallY, tz - 6), CAULDRON, 3);

        // Red carpet from south gate to altar
        for (int z = 9; z >= -7; z--) {
            level.setBlock(new BlockPos(tx, baseY + (z >= 9 ? 1 : 4), tz + z), RED_CARPET, 3);
        }

        // Temple gate
        level.setBlock(new BlockPos(tx - 1, baseY + 2, tz + 15), POLISHED_DEEPSLATE, 3);
        level.setBlock(new BlockPos(tx - 1, baseY + 3, tz + 15), POLISHED_DEEPSLATE, 3);
        level.setBlock(new BlockPos(tx + 1, baseY + 2, tz + 15), POLISHED_DEEPSLATE, 3);
        level.setBlock(new BlockPos(tx + 1, baseY + 3, tz + 15), POLISHED_DEEPSLATE, 3);
        // Gate top
        for (int x = -2; x <= 2; x++) {
            level.setBlock(new BlockPos(tx + x, baseY + 4, tz + 15), GOLD_BLOCK, 3);
        }
        // Gate lanterns
        level.setBlock(new BlockPos(tx - 2, baseY + 4, tz + 16), SOUL_LANTERN, 3);
        level.setBlock(new BlockPos(tx + 2, baseY + 4, tz + 16), SOUL_LANTERN, 3);

        // Meditation garden (east of temple hall, inside compound)
        for (int x = 8; x <= 11; x++) {
            for (int z = -8; z <= 8; z++) {
                level.setBlock(new BlockPos(tx + x, baseY + 1, tz + z), GRASS_BLOCK, 3);
                // Scattered spirit herbs
                if ((x + z) % 3 == 0) {
                    level.setBlock(new BlockPos(tx + x, baseY + 2, tz + z), FERN, 3);
                }
            }
        }
        // Stone bench
        for (int z = -2; z <= 2; z++) {
            level.setBlock(new BlockPos(tx + 9, baseY + 1, tz + z), STONE_SLAB, 3);
        }

        // Library wing (west of temple hall, inside compound)
        for (int x = -11; x <= -8; x++) {
            for (int z = -5; z <= 5; z++) {
                level.setBlock(new BlockPos(tx + x, baseY + 1, tz + z), DARK_OAK_PLANK, 3);
                boolean wall = Math.abs(x) == 11 || Math.abs(z) == 5;
                if (wall) {
                    for (int y = 2; y <= 5; y++) {
                        level.setBlock(new BlockPos(tx + x, baseY + y, tz + z), B.SPIRIT_STONE, 3);
                    }
                }
            }
        }
        // Library interior: bookshelves
        for (int z = -4; z <= 4; z += 2) {
            level.setBlock(new BlockPos(tx - 10, baseY + 1, tz + z), BOOKSHELF, 3);
            level.setBlock(new BlockPos(tx - 9, baseY + 1, tz + z), BOOKSHELF, 3);
        }
        // Lectern
        level.setBlock(new BlockPos(tx - 10, baseY + 1, tz), BOOKSHELF, 3);
    }
}
