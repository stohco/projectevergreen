package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TianShuiCityBuilder — FULLY hand-built Tian Shui City (天水城 / Heavenly Water City).
 *
 * <p>Constitution: "The world is completely hand-crafted, accurate to the novels.
 * NEVER write a script that replaces vanilla blocks with other blocks as a shortcut.
 * Every structure must be hand-authored."
 *
 * <p>Canon basis (Renegade Immortal): Tian Shui City is a major trade hub in
 * Zhao Country with 100,000 population. The city lord is Core Formation.
 * Situated along the Zhao Central River, it is the wealthiest city in Zhao —
 * a place where merchants, cultivators, and mortals converge.
 *
 * <p>Architecture: Polished granite + quartz + birch + white terracotta.
 * Distinct from Teng City's cobblestone/martial style. Elegant, wealthy, water-connected.
 *
 * <p>Districts (11): city_walls, main_streets, south_gate, merchant_quarter,
 * port_district, mortal_quarter, residential_district, city_lord_mansion,
 * cultivator_guild, temple_district, tavern_district.
 *
 * <p>CRON-COMPLETIONIST-39: Third full hand-built settlement.
 */
public final class TianShuiCityBuilder {

    /**
     * Lazy-initialized BlockState holder. ErgenverseBlocks.X.get() throws NPE before
     * Forge resolves the block registry, so these cannot be static-final in the outer
     * class. This inner class loads on first reference (during build(), which runs at
     * world-gen time — well after registry resolution).
     */
    private static final class B {
        private static final BlockState SPIRIT_STONE = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
        private static final BlockState JADE_STONE = ErgenverseBlocks.JADE_STONE.get().defaultBlockState();
    }

    private TianShuiCityBuilder() {}

    // ── Block palette ──────────────────────────────────────────────────
    // Elegant, wealthy trade city — distinct from Teng City's martial style.
    private static final BlockState POLISHED_GRANITE   = Blocks.POLISHED_GRANITE.defaultBlockState();
    private static final BlockState GRANITE             = Blocks.GRANITE.defaultBlockState();
    private static final BlockState QUARTZ              = Blocks.QUARTZ_BLOCK.defaultBlockState();
    private static final BlockState QUARTZ_PILLAR        = Blocks.QUARTZ_PILLAR.defaultBlockState();
    private static final BlockState QUARTZ_STAIRS       = Blocks.QUARTZ_STAIRS.defaultBlockState();
    private static final BlockState QUARTZ_SLAB         = Blocks.QUARTZ_SLAB.defaultBlockState();
    private static final BlockState BIRCH_PLANK         = Blocks.BIRCH_PLANKS.defaultBlockState();
    private static final BlockState BIRCH_LOG           = Blocks.BIRCH_LOG.defaultBlockState();
    private static final BlockState BIRCH_STAIRS        = Blocks.BIRCH_STAIRS.defaultBlockState();
    private static final BlockState BIRCH_SLAB          = Blocks.BIRCH_SLAB.defaultBlockState();
    private static final BlockState BIRCH_FENCE         = Blocks.BIRCH_FENCE.defaultBlockState();
    private static final BlockState BIRCH_DOOR           = Blocks.BIRCH_DOOR.defaultBlockState();
    private static final BlockState SPRUCE_PLANK        = Blocks.SPRUCE_PLANKS.defaultBlockState();
    private static final BlockState SPRUCE_LOG          = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState SPRUCE_STAIRS       = Blocks.SPRUCE_STAIRS.defaultBlockState();
    private static final BlockState SPRUCE_FENCE        = Blocks.SPRUCE_FENCE.defaultBlockState();
    private static final BlockState WHITE_TERRACOTTA    = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState PRISMARINE          = Blocks.PRISMARINE.defaultBlockState();
    private static final BlockState PRISMARINE_BRICKS   = Blocks.PRISMARINE_BRICKS.defaultBlockState();
    private static final BlockState DARK_PRISMARINE     = Blocks.DARK_PRISMARINE.defaultBlockState();
    private static final BlockState WATER                = Blocks.WATER.defaultBlockState();
    private static final BlockState AIR                 = Blocks.AIR.defaultBlockState();
    private static final BlockState COBBLE              = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState STONE_BRICK         = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState STONE               = Blocks.STONE.defaultBlockState();
    private static final BlockState DIRT                = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRASS               = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState OAK_PLANK           = Blocks.OAK_PLANKS.defaultBlockState();
    private static final BlockState OAK_STAIRS          = Blocks.OAK_STAIRS.defaultBlockState();
    private static final BlockState OAK_LOG             = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState DARK_OAK_PLANK      = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState IRON_BARS           = Blocks.IRON_BARS.defaultBlockState();
    private static final BlockState IRON_DOOR           = Blocks.IRON_DOOR.defaultBlockState();
    private static final BlockState LANTERN             = Blocks.LANTERN.defaultBlockState();
    private static final BlockState SOUL_LANTERN        = Blocks.SOUL_LANTERN.defaultBlockState();
    private static final BlockState GLOWSTONE           = Blocks.GLOWSTONE.defaultBlockState();
    private static final BlockState SEA_LANTERN         = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState CHEST               = Blocks.CHEST.defaultBlockState();
    private static final BlockState BOOKSHELF           = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState ANVIL               = Blocks.ANVIL.defaultBlockState();
    private static final BlockState CAULDRON            = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState CRAFTING_TABLE      = Blocks.CRAFTING_TABLE.defaultBlockState();
    private static final BlockState FURNACE             = Blocks.FURNACE.defaultBlockState();
    private static final BlockState BARREL              = Blocks.BARREL.defaultBlockState();
    private static final BlockState BREWING_STAND       = Blocks.BREWING_STAND.defaultBlockState();
    private static final BlockState RED_CARPET          = Blocks.RED_CARPET.defaultBlockState();
    private static final BlockState WHITE_CARPET        = Blocks.WHITE_CARPET.defaultBlockState();
    private static final BlockState LIGHT_GRAY_CARPET   = Blocks.LIGHT_GRAY_CARPET.defaultBlockState();
    private static final BlockState YELLOW_CARPET       = Blocks.YELLOW_CARPET.defaultBlockState();
    private static final BlockState GLASS_PANE          = Blocks.GLASS_PANE.defaultBlockState();
    private static final BlockState GOLD_BLOCK          = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState OBSIDIAN            = Blocks.OBSIDIAN.defaultBlockState();
    private static final BlockState SPRUCE_DOOR         = Blocks.SPRUCE_DOOR.defaultBlockState();
    private static final BlockState STONE_STAIRS        = Blocks.STONE_STAIRS.defaultBlockState();
    private static final BlockState STONE_SLAB          = Blocks.STONE_SLAB.defaultBlockState();
    private static final BlockState SANDSTONE           = Blocks.SANDSTONE.defaultBlockState();
    // ── Dimensions ────────────────────────────────────────────────────
    private static final int CITY_RADIUS = 65;   // half-size, so 130×130 total
    private static final int WALL_HEIGHT = 10;
    private static final int TOWER_SIZE = 5;
    private static final int TOWER_HEIGHT = 15;

    /** Tracks whether this city has already been built. */
    private static boolean built = false;

    public static boolean isAlreadyBuilt(ServerLevel level, BlockPos center) {
        // Check if the center block is already set to our material
        return level.getBlockState(center.above(WALL_HEIGHT + 1)).getBlock() == Blocks.QUARTZ_PILLAR;
    }

    public static void build(ServerLevel level, BlockPos center) {
        if (built) return;
        int baseY = center.getY();
        int cx = center.getX();
        int cz = center.getZ();

        Ergenverse.LOGGER.info("[TianShuiCity] Building Tian Shui City at ({}, {}, {})...", cx, baseY, cz);

        // Foundation: flatten and fill terrain
        buildFoundation(level, cx, baseY, cz);

        // 1. City Walls
        buildCityWalls(level, cx, baseY, cz);

        // 2. Main Streets + Central Plaza with fountain
        buildMainStreets(level, cx, baseY, cz);

        // 3. South Gate (main entrance)
        buildSouthGate(level, cx, baseY, cz);

        // 4. Merchant Quarter (east)
        buildMerchantQuarter(level, cx, baseY, cz);

        // 5. Port District (south, outside walls)
        buildPortDistrict(level, cx, baseY, cz);

        // 6. Mortal Quarter (west)
        buildMortalQuarter(level, cx, baseY, cz);

        // 7. Residential District (northwest)
        buildResidentialDistrict(level, cx, baseY, cz);

        // 8. City Lord's Mansion (north)
        buildCityLordMansion(level, cx, baseY, cz);

        // 9. Cultivator Guild Hall (northeast)
        buildCultivatorGuild(level, cx, baseY, cz);

        // 10. Temple District (central-east)
        buildTempleDistrict(level, cx, baseY, cz);

        // 11. Tavern District (south-central, inside walls)
        buildTavernDistrict(level, cx, baseY, cz);

        // Landmark beacon
        level.setBlock(new BlockPos(cx, baseY + WALL_HEIGHT + 2, cz), QUARTZ_PILLAR, 2);
        level.setBlock(new BlockPos(cx, baseY + WALL_HEIGHT + 3, cz), GOLD_BLOCK, 2);

        built = true;
        Ergenverse.LOGGER.info("[TianShuiCity] Tian Shui City complete at ({}, {}, {}).", cx, baseY, cz);
    }

    // ════════════════════════════════════════════════════════════════════
    // DISTRICT BUILDERS
    // ════════════════════════════════════════════════════════════════════

    private static void buildFoundation(ServerLevel level, int cx, int baseY, int cz) {
        for (int dx = -CITY_RADIUS - 5; dx <= CITY_RADIUS + 5; dx++) {
            for (int dz = -CITY_RADIUS - 5; dz <= CITY_RADIUS + 5; dz++) {
                BlockPos p = new BlockPos(cx + dx, baseY - 1, cz + dz);
                level.setBlock(p, DIRT, 2);
                // Clear above ground for building space
                for (int y = baseY; y <= baseY + WALL_HEIGHT + 3; y++) {
                    level.setBlock(new BlockPos(cx + dx, y, cz + dz), AIR, 2);
                }
            }
        }
    }

    private static void buildCityWalls(ServerLevel level, int cx, int baseY, int cz) {
        int r = CITY_RADIUS;
        for (int i = -r; i <= r; i++) {
            int wallY = baseY;
            // North wall
            for (int y = 0; y < WALL_HEIGHT; y++) {
                level.setBlock(new BlockPos(cx + i, wallY + y, cz - r), POLISHED_GRANITE, 2);
                level.setBlock(new BlockPos(cx + i, wallY + y, cz + r), POLISHED_GRANITE, 2);
                level.setBlock(new BlockPos(cx - r, wallY + y, cz + i), POLISHED_GRANITE, 2);
                level.setBlock(new BlockPos(cx + r, wallY + y, cz + i), POLISHED_GRANITE, 2);
            }
            // Crenellations on top
            if (i % 3 != 0) {
                level.setBlock(new BlockPos(cx + i, wallY + WALL_HEIGHT, cz - r), QUARTZ, 2);
                level.setBlock(new BlockPos(cx + i, wallY + WALL_HEIGHT, cz + r), QUARTZ, 2);
                level.setBlock(new BlockPos(cx - r, wallY + WALL_HEIGHT, cz + i), QUARTZ, 2);
                level.setBlock(new BlockPos(cx + r, wallY + WALL_HEIGHT, cz + i), QUARTZ, 2);
            }
            // Battlement walkway
            if (i % 3 == 0) {
                level.setBlock(new BlockPos(cx + i, wallY + WALL_HEIGHT, cz - r), QUARTZ_SLAB, 2);
                level.setBlock(new BlockPos(cx + i, wallY + WALL_HEIGHT, cz + r), QUARTZ_SLAB, 2);
                level.setBlock(new BlockPos(cx - r, wallY + WALL_HEIGHT, cz + i), QUARTZ_SLAB, 2);
                level.setBlock(new BlockPos(cx + r, wallY + WALL_HEIGHT, cz + i), QUARTZ_SLAB, 2);
            }
        }

        // Four corner watchtowers
        buildWatchtower(level, cx - r, baseY, cz - r);
        buildWatchtower(level, cx + r, baseY, cz - r);
        buildWatchtower(level, cx - r, baseY, cz + r);
        buildWatchtower(level, cx + r, baseY, cz + r);
    }

    private static void buildWatchtower(ServerLevel level, int tx, int baseY, int tz) {
        for (int y = 0; y < TOWER_HEIGHT; y++) {
            for (int dx = 0; dx < TOWER_SIZE; dx++) {
                for (int dz = 0; dz < TOWER_SIZE; dz++) {
                    boolean isEdge = dx == 0 || dx == TOWER_SIZE - 1 || dz == 0 || dz == TOWER_SIZE - 1;
                    boolean isDoor = y == 0 && dx == 2 && dz == 0;
                    if (isEdge && !isDoor) {
                        level.setBlock(new BlockPos(tx + dx, baseY + y, tz + dz), POLISHED_GRANITE, 2);
                    } else if (!isEdge && y == 0) {
                        level.setBlock(new BlockPos(tx + dx, baseY, tz + dz), POLISHED_GRANITE, 2);
                    } else if (!isEdge) {
                        level.setBlock(new BlockPos(tx + dx, baseY + y, tz + dz), AIR, 2);
                    }
                }
            }
        }
        // Gold cap on top
        for (int dx = 0; dx < TOWER_SIZE; dx++) {
            for (int dz = 0; dz < TOWER_SIZE; dz++) {
                level.setBlock(new BlockPos(tx + dx, baseY + TOWER_HEIGHT, tz + dz), QUARTZ_SLAB, 2);
            }
        }
        level.setBlock(new BlockPos(tx + 2, baseY + TOWER_HEIGHT + 1, tz + 2), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(tx + 2, baseY + TOWER_HEIGHT, tz + 2), LANTERN, 2);
    }

    private static void buildMainStreets(ServerLevel level, int cx, int baseY, int cz) {
        int r = CITY_RADIUS;
        // N-S avenue (6 wide, birch plank)
        for (int dz = -r + 2; dz <= r - 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                level.setBlock(new BlockPos(cx + dx, baseY, cz + dz), BIRCH_PLANK, 2);
            }
        }
        // E-W avenue (5 wide, light gray carpet center)
        for (int dx = -r + 2; dx <= r - 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlock(new BlockPos(cx + dx, baseY, cz + dz), BIRCH_PLANK, 2);
            }
            level.setBlock(new BlockPos(cx + dx, baseY + 1, cz - 1), LIGHT_GRAY_CARPET, 2);
            level.setBlock(new BlockPos(cx + dx, baseY + 1, cz), LIGHT_GRAY_CARPET, 2);
            level.setBlock(new BlockPos(cx + dx, baseY + 1, cz + 1), LIGHT_GRAY_CARPET, 2);
        }

        // Central circular plaza (radius 8) with fountain
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                if (dx * dx + dz * dz <= 64) {
                    level.setBlock(new BlockPos(cx + dx, baseY, cz + dz), QUARTZ, 2);
                    // Clear building space
                    for (int y = baseY + 1; y <= baseY + WALL_HEIGHT; y++) {
                        level.setBlock(new BlockPos(cx + dx, y, cz + dz), AIR, 2);
                    }
                }
            }
        }
        // Fountain (prismarine basin with water)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                    level.setBlock(new BlockPos(cx + dx, baseY, cz + dz), PRISMARINE, 2);
                    level.setBlock(new BlockPos(cx + dx, baseY + 1, cz + dz), PRISMARINE, 2);
                } else {
                    level.setBlock(new BlockPos(cx + dx, baseY, cz + dz), WATER, 2);
                    level.setBlock(new BlockPos(cx + dx, baseY + 1, cz + dz), AIR, 2);
                }
            }
        }
        level.setBlock(new BlockPos(cx, baseY + 2, cz), QUARTZ_PILLAR, 2);
        level.setBlock(new BlockPos(cx, baseY + 3, cz), SEA_LANTERN, 2);

        // Street lanterns every 8 blocks along N-S avenue
        for (int dz = -r + 10; dz <= r - 10; dz += 8) {
            level.setBlock(new BlockPos(cx - 3, baseY + 1, cz + dz), IRON_BARS, 2);
            level.setBlock(new BlockPos(cx - 3, baseY + 2, cz + dz), LANTERN, 2);
            level.setBlock(new BlockPos(cx + 3, baseY + 1, cz + dz), IRON_BARS, 2);
            level.setBlock(new BlockPos(cx + 3, baseY + 2, cz + dz), LANTERN, 2);
        }
    }

    private static void buildSouthGate(ServerLevel level, int cx, int baseY, int cz) {
        int r = CITY_RADIUS;
        int gz = cz + r;
        // Gate opening (clear 5 wide, 7 tall in the wall)
        for (int dx = -2; dx <= 2; dx++) {
            for (int y = 0; y < 7; y++) {
                level.setBlock(new BlockPos(cx + dx, baseY + y, gz), AIR, 2);
            }
        }
        // Quartz frame arch
        for (int dx = -3; dx <= 3; dx++) {
            level.setBlock(new BlockPos(cx + dx, baseY + 7, gz), QUARTZ, 2);
            level.setBlock(new BlockPos(cx + dx, baseY + 8, gz), QUARTZ_STAIRS, 2);
        }
        // Guard barracks (west side, 5×4×4)
        buildGuardBarracks(level, cx - 7, baseY, gz - 2);
        // Guard barracks (east side)
        buildGuardBarracks(level, cx + 3, baseY, gz - 2);
        // Gold plaque above gate
        level.setBlock(new BlockPos(cx, baseY + 8, gz - 1), GOLD_BLOCK, 2);
        // Iron doors
        level.setBlock(new BlockPos(cx - 1, baseY + 1, gz), IRON_BARS, 2);
        level.setBlock(new BlockPos(cx + 1, baseY + 1, gz), IRON_BARS, 2);
    }

    private static void buildGuardBarracks(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 4; dz++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 4 || dz == 0 || dz == 3;
                    boolean isDoor = y < 2 && dx == 2 && dz == 0;
                    if (isEdge && !isDoor) level.setBlock(p, POLISHED_GRANITE, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, COBBLE, 2);
                    else if (!isEdge && y > 0) level.setBlock(p, AIR, 2);
                }
            }
        }
        // Roof
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 4; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 4, z + dz), QUARTZ_STAIRS, 2);
            }
        }
        // Interior: bunks
        level.setBlock(new BlockPos(x + 1, baseY + 1, z + 1), OAK_PLANK, 2);
        level.setBlock(new BlockPos(x + 3, baseY + 1, z + 1), OAK_PLANK, 2);
    }

    private static void buildMerchantQuarter(ServerLevel level, int cx, int baseY, int cz) {
        int startX = cx + 10;
        int startZ = cz - 30;
        // 16 merchant shops in 2 rows of 8
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 8; col++) {
                int sx = startX + col * 8;
                int sz = startZ + row * 10;
                buildMerchantShop(level, sx, baseY, sz, row, col);
            }
        }
        // Auction house (10×8×6)
        buildAuctionHouse(level, startX + 10, baseY, startZ + 25);
        // Trading post
        buildTradingPost(level, startX + 30, baseY, startZ + 25);
        // Market square fountain
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    level.setBlock(new BlockPos(startX + 40 + dx, baseY, startZ + 10 + dz), PRISMARINE, 2);
                } else {
                    level.setBlock(new BlockPos(startX + 40 + dx, baseY, startZ + 10 + dz), WATER, 2);
                }
            }
        }
    }

    private static void buildMerchantShop(ServerLevel level, int x, int baseY, int z, int row, int col) {
        for (int dx = 0; dx < 6; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 5 || dz == 0 || dz == 4;
                    boolean isDoor = y < 2 && dx == 3 && dz == 0;
                    boolean isWindow = y >= 2 && y <= 3 && ((dx == 0 && dz == 2) || (dx == 5 && dz == 2));
                    if (isEdge && !isDoor && !isWindow) {
                        level.setBlock(p, BIRCH_PLANK, 2);
                    } else if (isWindow) {
                        level.setBlock(p, GLASS_PANE, 2);
                    } else if (!isEdge && y == 0) {
                        level.setBlock(p, BIRCH_PLANK, 2);
                    } else if (!isEdge) {
                        level.setBlock(p, AIR, 2);
                    }
                }
            }
        }
        // Roof
        for (int dx = 0; dx < 6; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 4, z + dz), BIRCH_STAIRS, 2);
            }
        }
        // Counter (spruce plank)
        for (int dx = 1; dx <= 4; dx++) {
            level.setBlock(new BlockPos(x + dx, baseY + 1, z + 2), SPRUCE_PLANK, 2);
        }
        // Barrel storage
        if (col % 3 == 0) level.setBlock(new BlockPos(x + 1, baseY + 1, z + 3), BARREL, 2);
        if (col % 3 == 1) level.setBlock(new BlockPos(x + 4, baseY + 1, z + 3), BARREL, 2);
        // Chest
        if (col % 2 == 0) ChestHelper.placeChestWithLoot(level, new BlockPos(x + 1, baseY + 1, z + 1),
                new ResourceLocation("ergenverse", "chests/tian_shui_city_market_district"));
    }

    private static void buildAuctionHouse(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 10; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                for (int y = 0; y < 6; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 9 || dz == 0 || dz == 7;
                    boolean isDoor = y < 3 && dx == 5 && dz == 0;
                    boolean isWindow = y >= 3 && y <= 4 && dz == 0 && (dx == 3 || dx == 7);
                    if (isEdge && !isDoor && !isWindow) level.setBlock(p, BIRCH_PLANK, 2);
                    else if (isWindow) level.setBlock(p, GLASS_PANE, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, BIRCH_PLANK, 2);
                    else if (!isEdge) level.setBlock(p, AIR, 2);
                }
            }
        }
        // Birch stage (front platform)
        for (int dx = 2; dx <= 7; dx++) {
            for (int dz = 1; dz <= 2; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 1, z + dz), WHITE_CARPET, 2);
            }
        }
        // Glowstone chandeliers
        level.setBlock(new BlockPos(x + 4, baseY + 5, z + 3), GLOWSTONE, 2);
        level.setBlock(new BlockPos(x + 5, baseY + 5, z + 3), GLOWSTONE, 2);
        // Roof
        for (int dx = 0; dx < 10; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 6, z + dz), BIRCH_STAIRS, 2);
            }
        }
        // Benches (oak plank rows)
        for (int dz = 3; dz <= 5; dz++) {
            level.setBlock(new BlockPos(x + 1, baseY + 1, z + dz), OAK_PLANK, 2);
            level.setBlock(new BlockPos(x + 8, baseY + 1, z + dz), OAK_PLANK, 2);
        }
    }

    private static void buildTradingPost(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 7; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 6 || dz == 0 || dz == 4;
                    boolean isDoor = y < 3 && dx == 3 && dz == 0;
                    if (isEdge && !isDoor) level.setBlock(p, SPRUCE_PLANK, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, COBBLE, 2);
                    else if (!isEdge) level.setBlock(p, AIR, 2);
                }
            }
        }
        // Counter
        for (int dx = 1; dx <= 5; dx++) {
            level.setBlock(new BlockPos(x + dx, baseY + 1, z + 3), SPRUCE_PLANK, 2);
        }
        // Storage chests
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 1, baseY + 1, z + 1),
                new ResourceLocation("ergenverse", "chests/tian_shui_market"));
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 5, baseY + 1, z + 1),
                new ResourceLocation("ergenverse", "chests/tian_shui_market"));
        // Roof
        for (int dx = 0; dx < 7; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 5, z + dz), SPRUCE_STAIRS, 2);
            }
        }
    }

    private static void buildPortDistrict(ServerLevel level, int cx, int baseY, int cz) {
        int r = CITY_RADIUS;
        int pz = cz + r + 3;
        // Prismarine piers extending south (20 blocks long)
        for (int pier = 0; pier < 4; pier++) {
            int px = cx - 15 + pier * 12;
            for (int dz = 0; dz < 20; dz++) {
                for (int dx = 0; dx < 3; dx++) {
                    level.setBlock(new BlockPos(px + dx, baseY - 1, pz + dz), PRISMARINE_BRICKS, 2);
                    level.setBlock(new BlockPos(px + dx, baseY, pz + dz), AIR, 2);
                    // Mooring posts
                    if (dz == 19) level.setBlock(new BlockPos(px + 1, baseY + 1, pz + dz), SPRUCE_FENCE, 2);
                }
            }
            // Warehouse at the pier base
            buildDockWarehouse(level, px - 1, baseY, pz - 8);
        }
        // Customs house
        buildCustomsHouse(level, cx - 5, baseY, pz + 2);
    }

    private static void buildDockWarehouse(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 8; dx++) {
            for (int dz = 0; dz < 6; dz++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 7 || dz == 0 || dz == 5;
                    boolean isDoor = y < 4 && dx == 4 && dz == 0;
                    if (isEdge && !isDoor) level.setBlock(p, BIRCH_PLANK, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, PRISMARINE_BRICKS, 2);
                    else if (!isEdge) level.setBlock(p, AIR, 2);
                }
            }
        }
        for (int dx = 0; dx < 8; dx++) {
            for (int dz = 0; dz < 6; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 5, z + dz), BIRCH_STAIRS, 2);
            }
        }
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 2, baseY + 1, z + 2),
                new ResourceLocation("ergenverse", "chests/tian_shui_city_port_docks"));
        level.setBlock(new BlockPos(x + 5, baseY + 1, z + 3), BARREL, 2);
        level.setBlock(new BlockPos(x + 6, baseY + 1, z + 4), BARREL, 2);
    }

    private static void buildCustomsHouse(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 7; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 6 || dz == 0 || dz == 4;
                    boolean isDoor = y < 4 && dx == 3 && dz == 0;
                    boolean isWindow = y >= 2 && dz == 0 && (dx == 2 || dx == 5);
                    if (isEdge && !isDoor && !isWindow) level.setBlock(p, BIRCH_PLANK, 2);
                    else if (isWindow) level.setBlock(p, GLASS_PANE, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, QUARTZ, 2);
                    else if (!isEdge) level.setBlock(p, AIR, 2);
                }
            }
        }
        // Gold accent on corners
        level.setBlock(new BlockPos(x, baseY + 4, z), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x + 6, baseY + 4, z), GOLD_BLOCK, 2);
        // Counter
        for (int dx = 1; dx <= 5; dx++) {
            level.setBlock(new BlockPos(x + dx, baseY + 1, z + 3), BIRCH_PLANK, 2);
        }
        // Chest for confiscated goods
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 1, baseY + 1, z + 1),
                new ResourceLocation("ergenverse", "chests/tian_shui_city_port_docks"));
        // Roof
        for (int dx = 0; dx < 7; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 5, z + dz), QUARTZ_STAIRS, 2);
            }
        }
    }

    private static void buildMortalQuarter(ServerLevel level, int cx, int baseY, int cz) {
        int startX = cx - 50;
        int startZ = cz - 30;
        // 24 row houses in 4 rows of 6
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 6; col++) {
                int hx = startX + col * 8;
                int hz = startZ + row * 8;
                buildRowHouse(level, hx, baseY, hz, col);
            }
        }
        // Communal well
        int wellX = startX + 25;
        int wellZ = startZ + 34;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(new BlockPos(wellX + dx, baseY, wellZ + dz), COBBLE, 2);
                if (Math.abs(dx) + Math.abs(dz) == 2) {
                    level.setBlock(new BlockPos(wellX + dx, baseY + 1, wellZ + dz), COBBLE, 2);
                }
            }
        }
        level.setBlock(new BlockPos(wellX, baseY + 1, wellZ), WATER, 2);
        // Beggar's corner
        for (int dx = 0; dx < 3; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                level.setBlock(new BlockPos(startX + 40 + dx, baseY, startZ + 5 + dz), DIRT, 2);
            }
        }
        level.setBlock(new BlockPos(startX + 41, baseY + 1, startZ + 6), SPRUCE_FENCE, 2);
        // Laundry cauldrons
        level.setBlock(new BlockPos(startX + 2, baseY, startZ + 34), COBBLE, 2);
        level.setBlock(new BlockPos(startX + 2, baseY + 1, startZ + 34), CAULDRON, 2);
    }

    private static void buildRowHouse(ServerLevel level, int x, int baseY, int z, int variant) {
        int h = 3 + (variant % 2); // 3 or 4 tall
        for (int dx = 0; dx < 4; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                for (int y = 0; y < h; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 3 || dz == 0 || dz == 2;
                    boolean isDoor = y < 2 && dx == 2 && dz == 0;
                    boolean isWindow = y >= 2 && dz == 0 && dx == 1;
                    if (isEdge && !isDoor && !isWindow) level.setBlock(p, COBBLE, 2);
                    else if (isWindow) level.setBlock(p, GLASS_PANE, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, COBBLE, 2);
                    else if (!isEdge) level.setBlock(p, AIR, 2);
                }
            }
        }
        // Roof
        for (int dx = 0; dx < 4; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + h, z + dz), OAK_STAIRS, 2);
            }
        }
    }

    private static void buildResidentialDistrict(ServerLevel level, int cx, int baseY, int cz) {
        int startX = cx - 50;
        int startZ = cz + 10;
        // 10 courtyard houses (8×8×4)
        for (int i = 0; i < 10; i++) {
            int row = i / 5;
            int col = i % 5;
            int hx = startX + col * 14;
            int hz = startZ + row * 16;
            buildCourtyardHouse(level, hx, baseY, hz, i);
        }
        // 3 wealthy merchant mansions (10×10×5, 2 floors)
        buildMerchantMansion(level, startX + 2, baseY, startZ + 34);
        buildMerchantMansion(level, startX + 18, baseY, startZ + 34);
        buildMerchantMansion(level, startX + 34, baseY, startZ + 34);
    }

    private static void buildCourtyardHouse(ServerLevel level, int x, int baseY, int z, int idx) {
        int size = 8;
        // Walls
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == size - 1 || dz == 0 || dz == size - 1;
                    boolean isDoor = y < 2 && dx == 4 && dz == 0;
                    boolean isCourtyard = dx >= 2 && dx <= 5 && dz >= 2 && dz <= 5;
                    boolean isWindow = y >= 2 && y <= 3 && (dx == 0 || dx == size - 1) && dz >= 2 && dz <= 5;
                    if (isEdge && !isDoor && !isWindow) {
                        level.setBlock(p, BIRCH_PLANK, 2);
                    } else if (isWindow) {
                        level.setBlock(p, GLASS_PANE, 2);
                    } else if (isCourtyard && y == 0) {
                        level.setBlock(p, BIRCH_PLANK, 2);
                    } else if (!isEdge && !isCourtyard && y == 0) {
                        level.setBlock(p, BIRCH_PLANK, 2);
                    } else {
                        level.setBlock(p, AIR, 2);
                    }
                }
            }
        }
        // Roof
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 4, z + dz), BIRCH_STAIRS, 2);
            }
        }
        // Birch tree in courtyard
        level.setBlock(new BlockPos(x + 3, baseY + 1, z + 3), BIRCH_LOG, 2);
        level.setBlock(new BlockPos(x + 3, baseY + 2, z + 3), BIRCH_LOG, 2);
        level.setBlock(new BlockPos(x + 3, baseY + 3, z + 3), BIRCH_LOG, 2);
        // Carpet
        level.setBlock(new BlockPos(x + 3, baseY + 1, z + 4), WHITE_CARPET, 2);
        // Chest
        if (idx % 3 == 0) ChestHelper.placeChestWithLoot(level, new BlockPos(x + 1, baseY + 1, z + 1),
                new ResourceLocation("ergenverse", "chests/tian_shui_city_residential_district"));
    }

    private static void buildMerchantMansion(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 10; dx++) {
            for (int dz = 0; dz < 10; dz++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 9 || dz == 0 || dz == 9;
                    boolean isDoor = y < 3 && dx == 5 && dz == 0;
                    boolean isWindow = y >= 2 && y <= 4 && (dz == 0 && (dx == 3 || dx == 7) || (dx == 0 || dx == 9) && dz >= 3 && dz <= 6);
                    if (isEdge && !isDoor && !isWindow) level.setBlock(p, BIRCH_PLANK, 2);
                    else if (isWindow) level.setBlock(p, GLASS_PANE, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, BIRCH_PLANK, 2);
                    else if (!isEdge) level.setBlock(p, AIR, 2);
                }
            }
        }
        // Gold trim on corners
        level.setBlock(new BlockPos(x, baseY + 4, z), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x + 9, baseY + 4, z), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x, baseY + 4, z + 9), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x + 9, baseY + 4, z + 9), GOLD_BLOCK, 2);
        // Second floor
        for (int dx = 1; dx <= 8; dx++) {
            for (int dz = 1; dz <= 8; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 5, z + dz), BIRCH_PLANK, 2);
            }
        }
        // Roof
        for (int dx = 0; dx < 10; dx++) {
            for (int dz = 0; dz < 10; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 6, z + dz), BIRCH_STAIRS, 2);
            }
        }
        // Red carpet aisle
        for (int dz = 1; dz <= 8; dz++) {
            level.setBlock(new BlockPos(x + 5, baseY + 1, z + dz), RED_CARPET, 2);
        }
        // Bookshelves
        level.setBlock(new BlockPos(x + 1, baseY + 1, z + 1), BOOKSHELF, 2);
        level.setBlock(new BlockPos(x + 8, baseY + 1, z + 1), BOOKSHELF, 2);
        level.setBlock(new BlockPos(x + 1, baseY + 1, z + 8), BOOKSHELF, 2);
        level.setBlock(new BlockPos(x + 8, baseY + 1, z + 8), BOOKSHELF, 2);
        // Chests
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 2, baseY + 1, z + 5),
                new ResourceLocation("ergenverse", "chests/tian_shui_merchant"));
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 7, baseY + 1, z + 5),
                new ResourceLocation("ergenverse", "chests/tian_shui_merchant"));
        // Staircase to 2nd floor
        for (int sy = 0; sy < 4; sy++) {
            level.setBlock(new BlockPos(x + 8 - sy, baseY + sy + 1, z + 8), OAK_STAIRS, 2);
        }
    }

    private static void buildCityLordMansion(ServerLevel level, int cx, int baseY, int cz) {
        int mx = cx - 18;
        int mz = cz - 60;
        // Raised stone platform (3 blocks high)
        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                for (int dy = 0; dy < 3; dy++) {
                    level.setBlock(new BlockPos(mx + dx, baseY - 1 - dy, mz + dz), GRANITE, 2);
                }
            }
        }
        // Outer compound wall (quartz, 25×25)
        for (int i = -12; i <= 12; i++) {
            for (int y = 0; y < 5; y++) {
                level.setBlock(new BlockPos(mx + i, baseY + y, mz - 12), QUARTZ, 2);
                level.setBlock(new BlockPos(mx + i, baseY + y, mz + 12), QUARTZ, 2);
                level.setBlock(new BlockPos(mx - 12, baseY + y, mz + i), QUARTZ, 2);
                level.setBlock(new BlockPos(mx + 12, baseY + y, mz + i), QUARTZ, 2);
            }
        }
        // Gate
        for (int y = 0; y < 4; y++) {
            level.setBlock(new BlockPos(mx, baseY + y, mz - 12), AIR, 2);
            level.setBlock(new BlockPos(mx + 1, baseY + y, mz - 12), AIR, 2);
        }
        // Main Hall (15×10×8)
        buildMainHall(level, mx - 7, baseY, mz - 5);
        // Private chambers
        buildPrivateChamber(level, mx + 8, baseY, mz - 4);
        // War room
        buildWarRoom(level, mx - 11, baseY, mz + 3);
        // Guard barracks
        buildGuardBarracks(level, mx + 8, baseY, mz + 8);
        // Treasury vault
        buildTreasuryVault(level, mx - 11, baseY, mz + 8);
    }

    private static void buildMainHall(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 15; dx++) {
            for (int dz = 0; dz < 10; dz++) {
                for (int y = 0; y < 8; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 14 || dz == 0 || dz == 9;
                    boolean isDoor = y < 4 && dx >= 6 && dx <= 8 && dz == 0;
                    boolean isWindow = y >= 4 && y <= 6 && dz == 0 && (dx == 3 || dx == 11);
                    if (isEdge && !isDoor && !isWindow) level.setBlock(p, BIRCH_PLANK, 2);
                    else if (isWindow) level.setBlock(p, GLASS_PANE, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, QUARTZ, 2);
                    else if (!isEdge) level.setBlock(p, AIR, 2);
                }
            }
        }
        // Roof
        for (int dx = 0; dx < 15; dx++) {
            for (int dz = 0; dz < 10; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 8, z + dz), BIRCH_STAIRS, 2);
            }
        }
        // Gold pillars (4)
        level.setBlock(new BlockPos(x + 3, baseY + 1, z + 2), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x + 3, baseY + 2, z + 2), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x + 3, baseY + 3, z + 2), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x + 11, baseY + 1, z + 2), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x + 11, baseY + 2, z + 2), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x + 11, baseY + 3, z + 2), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x + 3, baseY + 1, z + 7), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x + 3, baseY + 2, z + 7), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x + 3, baseY + 3, z + 7), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x + 11, baseY + 1, z + 7), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x + 11, baseY + 2, z + 7), GOLD_BLOCK, 2);
        level.setBlock(new BlockPos(x + 11, baseY + 3, z + 7), GOLD_BLOCK, 2);
        // Red carpet aisle
        for (int dz = 1; dz <= 8; dz++) {
            level.setBlock(new BlockPos(x + 7, baseY + 1, z + dz), RED_CARPET, 2);
        }
        // Throne (gold block on quartz platform)
        for (int dx = 6; dx <= 8; dx++) {
            for (int dz = 7; dz <= 8; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 1, z + dz), QUARTZ_SLAB, 2);
            }
        }
        level.setBlock(new BlockPos(x + 7, baseY + 2, z + 8), GOLD_BLOCK, 2);
        // Bookshelves
        level.setBlock(new BlockPos(x + 1, baseY + 1, z + 4), BOOKSHELF, 2);
        level.setBlock(new BlockPos(x + 1, baseY + 2, z + 4), BOOKSHELF, 2);
        level.setBlock(new BlockPos(x + 13, baseY + 1, z + 4), BOOKSHELF, 2);
        level.setBlock(new BlockPos(x + 13, baseY + 2, z + 4), BOOKSHELF, 2);
        // Lanterns
        level.setBlock(new BlockPos(x + 7, baseY + 7, z + 5), GLOWSTONE, 2);
        level.setBlock(new BlockPos(x + 7, baseY + 7, z + 4), GLOWSTONE, 2);
    }

    private static void buildPrivateChamber(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 6; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 5 || dz == 0 || dz == 4;
                    boolean isDoor = y < 3 && dx == 3 && dz == 0;
                    if (isEdge && !isDoor) level.setBlock(p, BIRCH_PLANK, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, BIRCH_PLANK, 2);
                    else level.setBlock(p, AIR, 2);
                }
            }
        }
        for (int dx = 0; dx < 6; dx++) for (int dz = 0; dz < 5; dz++) level.setBlock(new BlockPos(x + dx, baseY + 5, z + dz), BIRCH_STAIRS, 2);
        level.setBlock(new BlockPos(x + 3, baseY + 1, z + 3), RED_CARPET, 2);
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 1, baseY + 1, z + 2),
                new ResourceLocation("ergenverse", "chests/tian_shui_city_governor_mansion"));
    }

    private static void buildWarRoom(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 8; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 7 || dz == 0 || dz == 7;
                    boolean isDoor = y < 3 && dx == 4 && dz == 0;
                    if (isEdge && !isDoor) level.setBlock(p, STONE_BRICK, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, STONE_BRICK, 2);
                    else level.setBlock(p, AIR, 2);
                }
            }
        }
        for (int dx = 0; dx < 8; dx++) for (int dz = 0; dz < 8; dz++) level.setBlock(new BlockPos(x + dx, baseY + 4, z + dz), STONE_STAIRS, 2);
        // Strategy table
        level.setBlock(new BlockPos(x + 4, baseY + 1, z + 4), OAK_PLANK, 2);
        // Map walls (bookshelves as scroll storage)
        level.setBlock(new BlockPos(x + 1, baseY + 1, z + 3), BOOKSHELF, 2);
        level.setBlock(new BlockPos(x + 6, baseY + 1, z + 3), BOOKSHELF, 2);
    }

    private static void buildTreasuryVault(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 4 || dz == 0 || dz == 4;
                    if (isEdge) level.setBlock(p, OBSIDIAN, 2);
                    else if (y == 0) level.setBlock(p, OBSIDIAN, 2);
                    else level.setBlock(p, AIR, 2);
                }
            }
        }
        // Iron door
        level.setBlock(new BlockPos(x + 2, baseY + 1, z), IRON_DOOR, 2);
        level.setBlock(new BlockPos(x + 3, baseY + 1, z), AIR, 2);
        // Chests
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 1, baseY + 1, z + 2),
                new ResourceLocation("ergenverse", "chests/tian_shui_city_governor_mansion"));
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 3, baseY + 1, z + 3),
                new ResourceLocation("ergenverse", "chests/tian_shui_city_governor_mansion"));
        // Lantern
        level.setBlock(new BlockPos(x + 2, baseY + 3, z + 2), GLOWSTONE, 2);
    }

    private static void buildCultivatorGuild(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx + 10;
        int gz = cz - 55;
        // Training yard (20×20, hay targets, weapon racks)
        for (int dx = 0; dx < 20; dx++) {
            for (int dz = 0; dz < 20; dz++) {
                level.setBlock(new BlockPos(gx + dx, baseY, gz + dz), COBBLE, 2);
                // Clear above
                for (int y = baseY + 1; y <= baseY + 10; y++) {
                    level.setBlock(new BlockPos(gx + dx, y, gz + dz), AIR, 2);
                }
            }
        }
        // Hay bale targets (north wall)
        for (int i = 0; i < 5; i++) {
            level.setBlock(new BlockPos(gx + 2 + i * 4, baseY + 1, gz + 1), HAY_BLOCK_PLACEHOLDER, 2);
        }
        // Weapon racks (stone brick along east wall)
        for (int dz = 2; dz < 18; dz += 4) {
            level.setBlock(new BlockPos(gx + 19, baseY + 1, gz + dz), STONE_BRICK, 2);
            level.setBlock(new BlockPos(gx + 19, baseY + 2, gz + dz), IRON_BARS, 2);
        }
        // Meditation hall (10×8×6)
        buildMeditationHall(level, gx + 2, baseY, gz + 22);
        // Library (8×6×5)
        buildGuildLibrary(level, gx + 22, baseY, gz + 22);
        // Guild master's office
        buildGuildMasterOffice(level, gx + 22, baseY, gz + 2);
    }

    private static final BlockState HAY_BLOCK_PLACEHOLDER = Blocks.HAY_BLOCK.defaultBlockState();

    private static void buildMeditationHall(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 10; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                for (int y = 0; y < 6; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 9 || dz == 0 || dz == 7;
                    boolean isDoor = y < 3 && dx == 5 && dz == 0;
                    if (isEdge && !isDoor) level.setBlock(p, BIRCH_PLANK, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, BIRCH_PLANK, 2);
                    else level.setBlock(p, AIR, 2);
                }
            }
        }
        for (int dx = 0; dx < 10; dx++) for (int dz = 0; dz < 8; dz++) level.setBlock(new BlockPos(x + dx, baseY + 6, z + dz), BIRCH_STAIRS, 2);
        // Meditation mats (white carpet)
        for (int dx = 2; dx <= 7; dx++) {
            for (int dz = 2; dz <= 5; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 1, z + dz), WHITE_CARPET, 2);
            }
        }
        // Glowstone lanterns
        level.setBlock(new BlockPos(x + 5, baseY + 5, z + 4), GLOWSTONE, 2);
    }

    private static void buildGuildLibrary(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 8; dx++) {
            for (int dz = 0; dz < 6; dz++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 7 || dz == 0 || dz == 5;
                    boolean isDoor = y < 3 && dx == 4 && dz == 0;
                    if (isEdge && !isDoor) level.setBlock(p, BIRCH_PLANK, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, BIRCH_PLANK, 2);
                    else level.setBlock(p, AIR, 2);
                }
            }
        }
        for (int dx = 0; dx < 8; dx++) for (int dz = 0; dz < 6; dz++) level.setBlock(new BlockPos(x + dx, baseY + 5, z + dz), BIRCH_STAIRS, 2);
        // Bookshelves on all walls
        for (int dz = 1; dz <= 4; dz++) {
            level.setBlock(new BlockPos(x + 1, baseY + 1, z + dz), BOOKSHELF, 2);
            level.setBlock(new BlockPos(x + 1, baseY + 2, z + dz), BOOKSHELF, 2);
            level.setBlock(new BlockPos(x + 6, baseY + 1, z + dz), BOOKSHELF, 2);
            level.setBlock(new BlockPos(x + 6, baseY + 2, z + dz), BOOKSHELF, 2);
        }
        for (int dx = 2; dx <= 5; dx++) {
            level.setBlock(new BlockPos(x + dx, baseY + 1, z + 1), BOOKSHELF, 2);
            level.setBlock(new BlockPos(x + dx, baseY + 2, z + 1), BOOKSHELF, 2);
            level.setBlock(new BlockPos(x + dx, baseY + 1, z + 4), BOOKSHELF, 2);
            level.setBlock(new BlockPos(x + dx, baseY + 2, z + 4), BOOKSHELF, 2);
        }
        // Enchanting table = technique copy desk
        level.setBlock(new BlockPos(x + 4, baseY + 1, z + 3), ENCHANTING_TABLE_PLACEHOLDER, 2);
        // Lantern
        level.setBlock(new BlockPos(x + 4, baseY + 4, z + 3), LANTERN, 2);
    }

    private static final BlockState ENCHANTING_TABLE_PLACEHOLDER = Blocks.ENCHANTING_TABLE.defaultBlockState();

    private static void buildGuildMasterOffice(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 6; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 5 || dz == 0 || dz == 4;
                    boolean isDoor = y < 3 && dx == 3 && dz == 0;
                    if (isEdge && !isDoor) level.setBlock(p, BIRCH_PLANK, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, BIRCH_PLANK, 2);
                    else level.setBlock(p, AIR, 2);
                }
            }
        }
        for (int dx = 0; dx < 6; dx++) for (int dz = 0; dz < 5; dz++) level.setBlock(new BlockPos(x + dx, baseY + 4, z + dz), BIRCH_STAIRS, 2);
        level.setBlock(new BlockPos(x + 3, baseY + 1, z + 3), RED_CARPET, 2);
        level.setBlock(new BlockPos(x + 1, baseY + 1, z + 1), BOOKSHELF, 2);
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 4, baseY + 1, z + 1),
                new ResourceLocation("ergenverse", "chests/tian_shui_city_cultivator_quarter"));
    }

    private static void buildTempleDistrict(ServerLevel level, int cx, int baseY, int cz) {
        int tx = cx + 10;
        int tz = cz - 12;
        // Grand temple (12×8×8)
        buildGrandTemple(level, tx, baseY, tz);
        // Ancestor memorial hall (8×6×5)
        buildMemorialHall(level, tx + 14, baseY, tz);
        // Shrine garden
        buildShrineGarden(level, tx + 14, baseY, tz + 10);
    }

    private static void buildGrandTemple(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 12; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                for (int y = 0; y < 8; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 11 || dz == 0 || dz == 7;
                    boolean isDoor = y < 5 && dx >= 5 && dx <= 6 && dz == 0;
                    if (isEdge && !isDoor) level.setBlock(p, QUARTZ, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, QUARTZ, 2);
                    else level.setBlock(p, AIR, 2);
                }
            }
        }
        // Tiered roof
        for (int dx = 0; dx < 12; dx++) for (int dz = 0; dz < 8; dz++) level.setBlock(new BlockPos(x + dx, baseY + 8, z + dz), QUARTZ_STAIRS, 2);
        for (int dx = 1; dx < 11; dx++) for (int dz = 1; dz < 7; dz++) level.setBlock(new BlockPos(x + dx, baseY + 9, z + dz), QUARTZ_STAIRS, 2);
        // Red carpet aisle
        for (int dz = 1; dz <= 6; dz++) {
            level.setBlock(new BlockPos(x + 6, baseY + 1, z + dz), RED_CARPET, 2);
            level.setBlock(new BlockPos(x + 5, baseY + 1, z + dz), RED_CARPET, 2);
        }
        // Altar with cauldron
        for (int dx = 4; dx <= 7; dx++) {
            level.setBlock(new BlockPos(x + dx, baseY + 1, z + 6), QUARTZ_SLAB, 2);
        }
        level.setBlock(new BlockPos(x + 5, baseY + 2, z + 6), CAULDRON, 2);
        level.setBlock(new BlockPos(x + 6, baseY + 2, z + 6), CAULDRON, 2);
        // Soul lanterns
        level.setBlock(new BlockPos(x + 1, baseY + 7, z + 4), SOUL_LANTERN, 2);
        level.setBlock(new BlockPos(x + 10, baseY + 7, z + 4), SOUL_LANTERN, 2);
    }

    private static void buildMemorialHall(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 8; dx++) {
            for (int dz = 0; dz < 6; dz++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 7 || dz == 0 || dz == 5;
                    boolean isDoor = y < 3 && dx == 4 && dz == 0;
                    if (isEdge && !isDoor) level.setBlock(p, DARK_OAK_PLANK, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, DARK_OAK_PLANK, 2);
                    else level.setBlock(p, AIR, 2);
                }
            }
        }
        for (int dx = 0; dx < 8; dx++) for (int dz = 0; dz < 6; dz++) level.setBlock(new BlockPos(x + dx, baseY + 5, z + dz), SPRUCE_STAIRS, 2);
        // Ancestor tablets (bookshelves)
        for (int dz = 1; dz <= 4; dz++) {
            level.setBlock(new BlockPos(x + 1, baseY + 1, z + dz), BOOKSHELF, 2);
            level.setBlock(new BlockPos(x + 1, baseY + 2, z + dz), BOOKSHELF, 2);
            level.setBlock(new BlockPos(x + 6, baseY + 1, z + dz), BOOKSHELF, 2);
            level.setBlock(new BlockPos(x + 6, baseY + 2, z + dz), BOOKSHELF, 2);
        }
        // Soul lanterns
        level.setBlock(new BlockPos(x + 4, baseY + 4, z + 3), SOUL_LANTERN, 2);
        level.setBlock(new BlockPos(x + 3, baseY + 4, z + 3), SOUL_LANTERN, 2);
    }

    private static void buildShrineGarden(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 10; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY, z + dz), GRASS, 2);
            }
        }
        // Stone path
        for (int dz = 0; dz < 8; dz++) {
            level.setBlock(new BlockPos(x + 5, baseY, z + dz), SANDSTONE, 2);
        }
        // Flowers (using poppy and bone meal grass)
        level.setBlock(new BlockPos(x + 2, baseY + 1, z + 2), Blocks.POPPY.defaultBlockState(), 2);
        level.setBlock(new BlockPos(x + 7, baseY + 1, z + 4), Blocks.POPPY.defaultBlockState(), 2);
        level.setBlock(new BlockPos(x + 3, baseY + 1, z + 6), Blocks.RED_TULIP.defaultBlockState(), 2);
        level.setBlock(new BlockPos(x + 8, baseY + 1, z + 1), Blocks.OXEYE_DAISY.defaultBlockState(), 2);
        // Birch fence border
        for (int dx = 0; dx < 10; dx++) {
            level.setBlock(new BlockPos(x + dx, baseY, z), BIRCH_FENCE, 2);
            level.setBlock(new BlockPos(x + dx, baseY, z + 7), BIRCH_FENCE, 2);
        }
        for (int dz = 0; dz < 8; dz++) {
            level.setBlock(new BlockPos(x, baseY, z + dz), BIRCH_FENCE, 2);
            level.setBlock(new BlockPos(x + 9, baseY, z + dz), BIRCH_FENCE, 2);
        }
    }

    private static void buildTavernDistrict(ServerLevel level, int cx, int baseY, int cz) {
        int tx = cx - 5;
        int tz = cz + 20;
        // Drunk Dragon Inn (12×8×5)
        buildDrunkDragonInn(level, tx, baseY, tz);
        // Tea house (6×5×4)
        buildTeaHouse(level, tx - 14, baseY, tz);
    }

    private static void buildDrunkDragonInn(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 12; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 11 || dz == 0 || dz == 7;
                    boolean isDoor = y < 3 && dx == 6 && dz == 0;
                    boolean isWindow = y >= 2 && dz == 0 && (dx == 3 || dx == 9);
                    if (isEdge && !isDoor && !isWindow) level.setBlock(p, SPRUCE_PLANK, 2);
                    else if (isWindow) level.setBlock(p, GLASS_PANE, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, SPRUCE_PLANK, 2);
                    else level.setBlock(p, AIR, 2);
                }
            }
        }
        // Roof
        for (int dx = 0; dx < 12; dx++) for (int dz = 0; dz < 8; dz++) level.setBlock(new BlockPos(x + dx, baseY + 5, z + dz), SPRUCE_STAIRS, 2);
        // 2nd floor (partial, balcony side)
        for (int dx = 1; dx <= 10; dx++) {
            for (int dz = 1; dz <= 6; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 5, z + dz), OAK_PLANK, 2);
            }
        }
        // Balcony railing
        for (int dx = 1; dx <= 10; dx++) {
            level.setBlock(new BlockPos(x + dx, baseY + 6, z + 1), SPRUCE_FENCE, 2);
        }
        // Bar counter
        for (int dx = 2; dx <= 9; dx++) {
            level.setBlock(new BlockPos(x + dx, baseY + 1, z + 6), SPRUCE_PLANK, 2);
        }
        // Brewing stands
        level.setBlock(new BlockPos(x + 3, baseY + 2, z + 6), BREWING_STAND, 2);
        level.setBlock(new BlockPos(x + 8, baseY + 2, z + 6), BREWING_STAND, 2);
        // Barrel storage
        level.setBlock(new BlockPos(x + 1, baseY + 1, z + 2), BARREL, 2);
        level.setBlock(new BlockPos(x + 10, baseY + 1, z + 2), BARREL, 2);
        level.setBlock(new BlockPos(x + 1, baseY + 1, z + 5), BARREL, 2);
        level.setBlock(new BlockPos(x + 10, baseY + 1, z + 5), BARREL, 2);
        // Common room tables (oak plank blocks)
        level.setBlock(new BlockPos(x + 3, baseY + 1, z + 3), OAK_PLANK, 2);
        level.setBlock(new BlockPos(x + 5, baseY + 1, z + 3), OAK_PLANK, 2);
        level.setBlock(new BlockPos(x + 7, baseY + 1, z + 3), OAK_PLANK, 2);
        level.setBlock(new BlockPos(x + 4, baseY + 1, z + 5), OAK_PLANK, 2);
        level.setBlock(new BlockPos(x + 6, baseY + 1, z + 5), OAK_PLANK, 2);
        level.setBlock(new BlockPos(x + 8, baseY + 1, z + 5), OAK_PLANK, 2);
        // Lanterns
        level.setBlock(new BlockPos(x + 6, baseY + 4, z + 4), LANTERN, 2);
    }

    private static void buildTeaHouse(ServerLevel level, int x, int baseY, int z) {
        for (int dx = 0; dx < 6; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                for (int y = 0; y < 4; y++) {
                    BlockPos p = new BlockPos(x + dx, baseY + y, z + dz);
                    boolean isEdge = dx == 0 || dx == 5 || dz == 0 || dz == 4;
                    boolean isDoor = y < 3 && dx == 3 && dz == 0;
                    boolean isWindow = y >= 2 && dz == 0 && (dx == 2 || dx == 4);
                    if (isEdge && !isDoor && !isWindow) level.setBlock(p, BIRCH_PLANK, 2);
                    else if (isWindow) level.setBlock(p, GLASS_PANE, 2);
                    else if (!isEdge && y == 0) level.setBlock(p, BIRCH_PLANK, 2);
                    else level.setBlock(p, AIR, 2);
                }
            }
        }
        for (int dx = 0; dx < 6; dx++) for (int dz = 0; dz < 5; dz++) level.setBlock(new BlockPos(x + dx, baseY + 4, z + dz), BIRCH_STAIRS, 2);
        // White terracotta floor (peaceful)
        for (int dx = 1; dx <= 4; dx++) {
            for (int dz = 1; dz <= 3; dz++) {
                level.setBlock(new BlockPos(x + dx, baseY + 1, z + dz), WHITE_CARPET, 2);
            }
        }
        // Tea table
        level.setBlock(new BlockPos(x + 3, baseY + 1, z + 2), BIRCH_PLANK, 2);
        // Cauldron for tea
        level.setBlock(new BlockPos(x + 1, baseY + 1, z + 1), CAULDRON, 2);
        // Lantern
        level.setBlock(new BlockPos(x + 3, baseY + 3, z + 3), LANTERN, 2);
    }
}
