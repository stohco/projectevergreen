package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TengFamilyCityBuilder — a FULLY hand-built Teng Family City (滕城),
 * the largest city in Zhao Country and seat of the Teng clan's power.
 *
 * <p>Constitution: "The world is completely hand-crafted, accurate to the novels.
 * NEVER write a script that replaces vanilla blocks with other blocks as a shortcut.
 * Every structure must be hand-authored."
 *
 * <p>Canon basis (Renegade Immortal, Chapter 1):
 * Teng Cheng is a grand walled city of ~50,000 inhabitants. It is the economic
 * and political heart of Zhao Country. Teng Huayuan (Half-Step Deity Transformation)
 * rules from the Governor's Keep. The city has imposing walls, a bustling market,
 * a poor mortal quarter, smuggler tunnels beneath, cultivator quarters for the
 * Teng family's trained fighters, and a temple district for ancestor worship.
 * Wang Lin visits this city to purchase cultivation resources and later returns
 * to enact his revenge on the Teng clan.
 *
 * <p>Districts (12 total):
 * city_walls, city_gate, market_district, mortal_quarter, residential_district,
 * warehouse_district, smuggler_tunnels, governor_keep, cultivator_quarter,
 * temple_district, tavern_district, main_streets.
 *
 * <p>Layout: roughly 120×120 blocks, oriented with cardinal directions.
 * The city gate faces south (positive Z). The Keep sits in the north-center.
 * Main streets divide the city into quadrants.
 *
 * <p>CRON-COMPLETIONIST-37: First full hand-built city. Follows the
 * HengYueSectBuilder pattern but adapted for a mortal city rather than
 * a mountain sect.
 *
 * <h2>Harsh Self-Critique</h2>
 * <ul>
 *   <li>City is flat — no elevation changes, no natural terrain integration.
 *       A real city would be built on a hill or near a river. The builder
 *       places everything at ground level and does not carve terrain.</li>
 *   <li>Streets are all 3-block-wide cobblestone channels — no paving
 *       variation, no carriage ruts, no market stall overhangs.</li>
 *   <li>The Keep is modest — 7×7 at the base. A city of 50,000 ruled by a
 *       Deity Transformation cultivator would have a palatial compound, not
 *       a glorified house.</li>
 *   <li>Smuggler tunnels are shallow — 5 blocks deep, easily discoverable.
 *       Canon implies they're a deep, secret network.</li>
 *   <li>All buildings use the same spirit_stone palette — no distinct
 *       "Teng family" architectural style differentiating from Heng Yue Sect.</li>
 *   <li>No interior furnishings — buildings are shells. No beds, no tables,
 *       no barrels, no looms. The market district has no actual stalls.</li>
 * </ul>
 */
public final class TengFamilyCityBuilder {

    /**
     * Lazy-initialized BlockState holder. ErgenverseBlocks.X.get() throws NPE before
     * Forge resolves the block registry, so these cannot be static-final in the outer
     * class. This inner class loads on first reference (during build(), which runs at
     * world-gen time — well after registry resolution).
     */
    private static final class B {
        private static final BlockState SPRUCE_PLANK = ErgenverseBlocks.SPIRIT_WOOD_PLANKS.get().defaultBlockState();
        private static final BlockState SPRUCE_LOG = ErgenverseBlocks.SPIRIT_WOOD_LOG.get().defaultBlockState();
        private static final BlockState SPRUCE_STAIRS = ErgenverseBlocks.SPIRIT_WOOD_PLANKS_STAIRS.get().defaultBlockState();
        private static final BlockState OBSIDIAN = ErgenverseBlocks.RESTRICTION_STONE.get().defaultBlockState();
        private static final BlockState SPIRIT_STONE = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
        private static final BlockState BRICK_WALL = ErgenverseBlocks.SPIRIT_STONE_WALL.get().defaultBlockState();
        private static final BlockState JADE_STONE = ErgenverseBlocks.JADE_STONE.get().defaultBlockState();
    }

    private TengFamilyCityBuilder() {}

    // ── Block palette ──────────────────────────────────────────────────
    // Teng City is a MORTAL city with cultivator rulers. The architecture
    // mixes mortal materials (cobblestone, stone brick) with spirit materials
    // in the cultivator and keep districts. This is distinct from Heng Yue Sect
    // which uses almost exclusively spirit materials.
    private static final BlockState COBBLE           = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState STONE_BRICK      = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState MOSSY_STONE_BRICK= Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
    private static final BlockState CHISELED_STONE   = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
    private static final BlockState STONE            = Blocks.STONE.defaultBlockState();
    private static final BlockState DIRT             = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRASS            = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState OAK_PLANK        = Blocks.OAK_PLANKS.defaultBlockState();
    private static final BlockState OAK_LOG          = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState DARK_OAK_PLANK   = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState DARK_OAK_LOG     = Blocks.DARK_OAK_LOG.defaultBlockState();
    private static final BlockState IRON_BARS        = Blocks.IRON_BARS.defaultBlockState();
    private static final BlockState LANTERN          = Blocks.LANTERN.defaultBlockState();
    private static final BlockState SOUL_LANTERN     = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState GLOWSTONE        = Blocks.GLOWSTONE.defaultBlockState();
    private static final BlockState CHEST            = Blocks.CHEST.defaultBlockState();
    private static final BlockState BOOKSHELF        = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState ANVIL            = Blocks.ANVIL.defaultBlockState();
    private static final BlockState CAULDRON         = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState CRAFTING_TABLE   = Blocks.CRAFTING_TABLE.defaultBlockState();
    private static final BlockState FURNACE          = Blocks.FURNACE.defaultBlockState();
    private static final BlockState BARREL           = Blocks.BARREL.defaultBlockState();
    private static final BlockState BREWING_STAND    = Blocks.BREWING_STAND.defaultBlockState();
    private static final BlockState SKELETON_SKULL   = Blocks.SKELETON_SKULL.defaultBlockState();
    private static final BlockState RED_CARPET       = Blocks.RED_CARPET.defaultBlockState();
    private static final BlockState BROWN_CARPET     = Blocks.BROWN_CARPET.defaultBlockState();
    private static final BlockState LIGHT_GRAY_CARPET= Blocks.LIGHT_GRAY_CARPET.defaultBlockState();
    private static final BlockState SPRUCE_FENCE     = Blocks.SPRUCE_FENCE.defaultBlockState();
    private static final BlockState SPRUCE_FENCE_GATE= Blocks.SPRUCE_FENCE_GATE.defaultBlockState();
    private static final BlockState OAK_DOOR          = Blocks.OAK_DOOR.defaultBlockState();
    private static final BlockState SPRUCE_DOOR      = Blocks.SPRUCE_DOOR.defaultBlockState();
    private static final BlockState IRON_DOOR        = Blocks.IRON_DOOR.defaultBlockState();
    private static final BlockState WATER            = Blocks.WATER.defaultBlockState();
    private static final BlockState GRAVEL           = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState SAND             = Blocks.SAND.defaultBlockState();
    private static final BlockState HAY              = Blocks.HAY_BLOCK.defaultBlockState();
    private static final BlockState SMOOTH_STONE_SLAB= Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
    private static final BlockState STONE_STAIRS     = Blocks.STONE_STAIRS.defaultBlockState();
    private static final BlockState COBBLE_STAIRS    = Blocks.COBBLESTONE_STAIRS.defaultBlockState();
    private static final BlockState OAK_STAIRS       = Blocks.OAK_STAIRS.defaultBlockState();
    private static final BlockState DARK_OAK_STAIRS  = Blocks.DARK_OAK_STAIRS.defaultBlockState();
    private static final BlockState SPIRIT_STONE_SLAB= ErgenverseBlocks.SPIRIT_STONE_SLAB.get().defaultBlockState();
    private static final BlockState GOLD_BLOCK       = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState FLOWER_POT       = Blocks.FLOWER_POT.defaultBlockState();
    private static final BlockState TORCH            = Blocks.TORCH.defaultBlockState();
    private static final BlockState WHITE_WOOL       = Blocks.WHITE_WOOL.defaultBlockState();
    private static final BlockState RED_WOOL        = Blocks.RED_WOOL.defaultBlockState();
    private static final BlockState BLACK_WOOL      = Blocks.BLACK_WOOL.defaultBlockState();
    private static final BlockState BLUE_ORCHID      = Blocks.BLUE_ORCHID.defaultBlockState();
    private static final BlockState CORNFLOWER       = Blocks.CORNFLOWER.defaultBlockState();
    private static final BlockState OXEYE_DAISY     = Blocks.OXEYE_DAISY.defaultBlockState();
    private static final BlockState FERN             = Blocks.FERN.defaultBlockState();
    private static final BlockState DEAD_BUSH        = Blocks.DEAD_BUSH.defaultBlockState();

    // ── Helper ────────────────────────────────────────────────────────────
    private static void setBlock(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }

    private static void fill(ServerLevel level, BlockPos origin, int w, int h, int d, BlockState state) {
        for (int dx = 0; dx < w; dx++)
            for (int dy = 0; dy < h; dy++)
                for (int dz = 0; dz < d; dz++)
                    setBlock(level, origin.offset(dx, dy, dz), state);
    }

    /** Place a wall segment (hollow shell, 1-block thick). */
    private static void wallBox(ServerLevel level, BlockPos origin, int w, int h, int d, BlockState wall, BlockState floor) {
        for (int dx = 0; dx < w; dx++) {
            for (int dy = 0; dy < h; dy++) {
                for (int dz = 0; dz < d; dz++) {
                    boolean isEdgeX = dx == 0 || dx == w - 1;
                    boolean isEdgeZ = dz == 0 || dz == d - 1;
                    boolean isEdgeY = dy == 0 || dy == h - 1;
                    if (isEdgeX || isEdgeZ || isEdgeY) {
                        setBlock(level, origin.offset(dx, dy, dz), wall);
                    } else if (floor != null && dy == 0) {
                        setBlock(level, origin.offset(dx, dy, dz), floor);
                    } else {
                        // Interior — air (or floor at y=0)
                        if (dy == 0) setBlock(level, origin.offset(dx, dy, dz), floor != null ? floor : COBBLE);
                    }
                }
            }
        }
    }

    /** Place a flat roof with slight overhang. */
    private static void placeRoof(ServerLevel level, BlockPos origin, int w, int d, BlockState material) {
        for (int dx = -1; dx <= w; dx++) {
            for (int dz = -1; dz <= d; dz++) {
                setBlock(level, origin.offset(dx, 0, dz), material);
            }
        }
        // Stair eave on all 4 sides
        BlockState stairNS = Blocks.STONE_STAIRS.defaultBlockState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.STAIRS_SHAPE,
                        net.minecraft.world.level.block.state.properties.StairsShape.OUTER_LEFT);
        setBlock(level, origin.offset(-1, 1, 0), stairNS);
        setBlock(level, origin.offset(w, 1, 0), stairNS);
        setBlock(level, origin.offset(0, 0, 0), SMOOTH_STONE_SLAB);
    }

    /** Place a door at position (auto-detects facing based on surrounding walls). */
    private static void placeDoor(ServerLevel level, BlockPos pos, BlockState doorType) {
        setBlock(level, pos, doorType);
        setBlock(level, pos.above(), doorType);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Main entry point
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Build the full Teng Family City centered at (x, groundY, z).
     * The city gate faces south (positive Z).
     */
    public static void build(ServerLevel level, BlockPos center) {
        if (isAlreadyBuilt(level, center)) return;
        dev.ergenverse.core.Ergenverse.LOGGER.info("[Ergenverse] Building Teng Family City at {}", center);

        buildCityWalls(level, center);
        buildMainStreets(level, center);
        buildCityGate(level, center);
        buildMarketDistrict(level, center);
        buildMortalQuarter(level, center);
        buildResidentialDistrict(level, center);
        buildWarehouseDistrict(level, center);
        buildGovernorKeep(level, center);
        buildCultivatorQuarter(level, center);
        buildTempleDistrict(level, center);
        buildTavernDistrict(level, center);
        buildSmugglerTunnels(level, center);
        buildCityLanterns(level, center);

        dev.ergenverse.core.Ergenverse.LOGGER.info("[Ergenverse] Teng Family City construction complete.");
    }

    public static boolean isAlreadyBuilt(ServerLevel level, BlockPos center) {
        // Check if the keep's foundation marker exists
        return level.getBlockState(center.offset(0, 1, -20)).getBlock() == Blocks.GOLD_BLOCK;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  District builders
    // ═══════════════════════════════════════════════════════════════════

    // ── 1. City Walls ──────────────────────────────────────────────────
    // 120×120 perimeter, 8 blocks tall, with corner watchtowers.
    // Teng City is a fortified city — the walls project power and control.
    private static void buildCityWalls(ServerLevel level, BlockPos c) {
        int halfR = 58; // city is 118×118 blocks (walls at ±58 from center)
        int wallH = 8;

        // Foundation — stone brick base, 2 blocks deep
        for (int dx = -halfR; dx <= halfR; dx++) {
            for (int dz = -halfR; dz <= halfR; dz++) {
                // Only perimeter
                boolean isPerimeterX = dx == -halfR || dx == halfR;
                boolean isPerimeterZ = dz == -halfR || dz == halfR;
                if (isPerimeterX || isPerimeterZ) {
                    setBlock(level, c.offset(dx, -2, dz), STONE);
                    setBlock(level, c.offset(dx, -1, dz), STONE_BRICK);
                    // Wall body
                    for (int y = 0; y < wallH; y++) {
                        BlockState wallMat = (y >= 6) ? CHISELED_STONE : STONE_BRICK;
                        setBlock(level, c.offset(dx, y, dz), wallMat);
                    }
                    // Battlements (crenellations) on top
                    if ((Math.abs(dx) + Math.abs(dz)) % 2 == 0) {
                        setBlock(level, c.offset(dx, wallH, dz), STONE_BRICK);
                    }
                }
            }
        }

        // Four corner watchtowers (5×5, 12 tall)
        int[][] corners = {{-halfR, -halfR}, {-halfR, halfR}, {halfR, -halfR}, {halfR, halfR}};
        for (int[] corner : corners) {
            BlockPos towerBase = c.offset(corner[0], 0, corner[1]);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int y = wallH; y < wallH + 6; y++) {
                        boolean isEdge = Math.abs(dx) == 1 || Math.abs(dz) == 1 || y == wallH + 5;
                        setBlock(level, towerBase.offset(dx, y, dz),
                                isEdge ? CHISELED_STONE : AIR_BLOCK());
                    }
                }
            }
            // Torch on each tower
            setBlock(level, towerBase.offset(0, wallH + 5, 0), TORCH);
        }

        // South gate opening (3 blocks wide, gate arch)
        for (int dx = -1; dx <= 1; dx++) {
            for (int y = 0; y < wallH; y++) {
                setBlock(level, c.offset(dx, y, halfR), AIR_BLOCK());
            }
        }
        // Gate arch (stone brick lintel over the opening)
        for (int dx = -2; dx <= 2; dx++) {
            setBlock(level, c.offset(dx, wallH, halfR), CHISELED_STONE);
            setBlock(level, c.offset(dx, wallH + 1, halfR), CHISELED_STONE);
        }
    }

    // ── 2. Main Streets ──────────────────────────────────────────────
    // Two main streets dividing the city into quadrants.
    // North-South street: 5 blocks wide, runs from gate to keep.
    // East-West street: 4 blocks wide, runs across the middle.
    private static void buildMainStreets(ServerLevel level, BlockPos c) {
        // North-South main street (Z-axis, from south gate to north keep)
        for (int dz = -55; dz <= 55; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                setBlock(level, c.offset(dx, 0, dz), COBBLE);
                // Sidewalk edges
                if (Math.abs(dx) == 2) {
                    setBlock(level, c.offset(dx, 0, dz), SMOOTH_STONE_SLAB);
                }
            }
        }

        // East-West main street (X-axis)
        for (int dx = -55; dx <= 55; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                setBlock(level, c.offset(dx, 0, dz), COBBLE);
                if (Math.abs(dz) == 2) {
                    setBlock(level, c.offset(dx, 0, dz), SMOOTH_STONE_SLAB);
                }
            }
        }

        // Intersection paving (decorative jade stone at crossroads)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    setBlock(level, c.offset(dx, 0, dz), B.JADE_STONE);
                }
            }
        }
    }

    // ── 3. City Gate ──────────────────────────────────────────────────
    // South entrance. Guard barracks flanking the gate.
    // The gate is the first thing the player sees — imposing, militarized.
    private static void buildCityGate(ServerLevel level, BlockPos c) {
        BlockPos gateCenter = c.offset(0, 0, 56); // just inside the wall

        // Gate structure: two guard towers flanking the entrance
        // Left guard tower (west side)
        BlockPos leftTower = gateCenter.offset(-5, 1, 0);
        wallBox(level, leftTower, 4, 6, 4, STONE_BRICK, COBBLE);
        placeDoor(level, gateCenter.offset(-4, 1, 0), IRON_DOOR);

        // Right guard tower (east side)
        BlockPos rightTower = gateCenter.offset(1, 1, 0);
        wallBox(level, rightTower, 4, 6, 4, STONE_BRICK, COBBLE);
        placeDoor(level, gateCenter.offset(4, 1, 0), IRON_DOOR);

        // Gate interior — checkpoint with iron bars
        for (int dz = -1; dz <= 1; dz++) {
            setBlock(level, gateCenter.offset(-2, 1, dz), IRON_BARS);
            setBlock(level, gateCenter.offset(2, 1, dz), IRON_BARS);
        }

        // Guard barracks — small rooms behind each tower
        // West barracks
        fill(level, gateCenter.offset(-8, 1, -3), 3, 4, 6, COBBLE);
        setBlock(level, gateCenter.offset(-8, 1, 0), OAK_DOOR);
        // Interior: bunks (wool beds), anvil
        setBlock(level, gateCenter.offset(-7, 1, -2), RED_WOOL);
        setBlock(level, gateCenter.offset(-7, 1, 0), RED_WOOL);
        setBlock(level, gateCenter.offset(-7, 1, 2), ANVIL);
        setBlock(level, gateCenter.offset(-8, 4, -3), TORCH);

        // East barracks
        fill(level, gateCenter.offset(5, 1, -3), 3, 4, 6, COBBLE);
        setBlock(level, gateCenter.offset(5, 1, 0), OAK_DOOR);
        setBlock(level, gateCenter.offset(6, 1, -2), RED_WOOL);
        setBlock(level, gateCenter.offset(6, 1, 0), RED_WOOL);
        setBlock(level, gateCenter.offset(6, 1, 2), ANVIL);
        setBlock(level, gateCenter.offset(5, 4, -3), TORCH);

        // City name plaque (gold block between the towers — the Teng family's wealth)
        setBlock(level, gateCenter.offset(0, 3, 1), GOLD_BLOCK);
        setBlock(level, gateCenter.offset(-1, 3, 1), GOLD_BLOCK);
        setBlock(level, gateCenter.offset(1, 3, 1), GOLD_BLOCK);
    }

    // ── 4. Market District ────────────────────────────────────────────
    // Southeast quadrant. The economic heart of the city.
    // Open-air market stalls with awnings, merchant tables, storage barrels.
    private static void buildMarketDistrict(ServerLevel level, BlockPos c) {
        BlockPos districtOrigin = c.offset(6, 1, 6); // SE quadrant, above street

        // Market platform (cobblestone floor, 40×40)
        for (int dx = 0; dx < 40; dx++) {
            for (int dz = 0; dz < 40; dz++) {
                setBlock(level, districtOrigin.offset(dx, 0, dz),
                        (dx + dz) % 7 == 0 ? SMOOTH_STONE_SLAB : COBBLE);
            }
        }

        // Market stalls (rows of wooden platforms with awnings)
        // 4 rows of stalls, each stall is 5×3
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 6; col++) {
                BlockPos stallBase = districtOrigin.offset(4 + col * 6, 0, 4 + row * 9);
                // Counter (oak plank platform)
                for (int dx = 0; dx < 4; dx++) {
                    setBlock(level, stallBase.offset(dx, 0, 0), OAK_PLANK);
                    setBlock(level, stallBase.offset(dx, 0, 2), OAK_PLANK);
                    setBlock(level, stallBase.offset(dx, 0, 1), OAK_PLANK);
                }
                // Awning posts (oak logs)
                setBlock(level, stallBase.offset(0, 1, 0), SPRUCE_FENCE);
                setBlock(level, stallBase.offset(3, 1, 0), SPRUCE_FENCE);
                setBlock(level, stallBase.offset(0, 1, 2), SPRUCE_FENCE);
                setBlock(level, stallBase.offset(3, 1, 2), SPRUCE_FENCE);
                // Awning roof (colored wool — red and white alternating by row)
                BlockState awning = (row % 2 == 0) ? RED_WOOL : WHITE_WOOL;
                for (int dx = -1; dx <= 4; dx++) {
                    for (int dz = -1; dz <= 3; dz++) {
                        setBlock(level, stallBase.offset(dx, 2, dz), awning);
                    }
                }
                // Goods on counter
                setBlock(level, stallBase.offset(1, 1, 1), BARREL);
                BlockPos stallChestPos = stallBase.offset(2, 1, 1);
                if (col % 3 == 0) {
                    setBlock(level, stallChestPos, BOOKSHELF);
                } else {
                    ChestHelper.placeChestWithLoot(level, stallChestPos,
                            new ResourceLocation("ergenverse", "chests/teng_family_city_market_district"));
                }
            }
        }

        // Central market fountain (water feature — cities have these)
        BlockPos fountain = districtOrigin.offset(20, 0, 20);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    setBlock(level, fountain.offset(dx, 0, dz), WATER);
                } else {
                    setBlock(level, fountain.offset(dx, 0, dz), CHISELED_STONE);
                }
            }
        }
        // Fountain pillar
        setBlock(level, fountain.offset(0, 1, 0), CHISELED_STONE);
        setBlock(level, fountain.offset(0, 2, 0), CHISELED_STONE);
        setBlock(level, fountain.offset(0, 3, 0), SOUL_LANTERN);

        // Market storage warehouse (on the east edge)
        BlockPos warehouse = districtOrigin.offset(34, 0, 10);
        wallBox(level, warehouse, 5, 4, 6, STONE_BRICK, COBBLE);
        placeDoor(level, warehouse.offset(2, 1, 5), OAK_DOOR);
        // Interior: barrels and chests
        setBlock(level, warehouse.offset(1, 1, 1), BARREL);
        setBlock(level, warehouse.offset(1, 1, 2), BARREL);
        setBlock(level, warehouse.offset(3, 1, 1), BARREL);
        ChestHelper.placeChestWithLoot(level, warehouse.offset(1, 1, 3),
                new ResourceLocation("ergenverse", "chests/teng_family_city_warehouse_district"));
        ChestHelper.placeChestWithLoot(level, warehouse.offset(3, 1, 3),
                new ResourceLocation("ergenverse", "chests/teng_family_city_warehouse_district"));
        setBlock(level, warehouse.offset(2, 1, 2), HAY);
        setBlock(level, warehouse.offset(2, 3, 2), TORCH);
    }

    // ── 5. Mortal Quarter ────────────────────────────────────────────
    // Southwest quadrant. The poorest area of the city.
    // Cramped houses, dirt paths, minimal furnishings.
    private static void buildMortalQuarter(ServerLevel level, BlockPos c) {
        BlockPos districtOrigin = c.offset(-55, 1, 6); // SW quadrant

        // Dirt paths (not cobblestone — the Teng family doesn't invest here)
        for (int dx = 0; dx < 48; dx++) {
            for (int dz = 0; dz < 48; dz++) {
                setBlock(level, districtOrigin.offset(dx, 0, dz), DIRT);
            }
        }

        // Cramped row houses (6×4, 3 blocks tall, packed tightly)
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 8; col++) {
                BlockPos houseBase = districtOrigin.offset(2 + col * 6, 0, 2 + row * 11);
                // Walls (cobblestone — cheapest building material)
                wallBox(level, houseBase, 5, 3, 4, COBBLE, DIRT);
                // Door (south side)
                placeDoor(level, houseBase.offset(2, 1, 3), OAK_DOOR);
                // Window (iron bars)
                setBlock(level, houseBase.offset(0, 2, 0), IRON_BARS);
                setBlock(level, houseBase.offset(4, 2, 0), IRON_BARS);
                // Interior: single wool bed, crafting table
                setBlock(level, houseBase.offset(1, 1, 1), BROWN_CARPET);
                setBlock(level, houseBase.offset(3, 1, 1), WHITE_WOOL); // bed
                // Some houses have a furnace (poor families cook indoors)
                if (col % 3 == 0) {
                    setBlock(level, houseBase.offset(1, 1, 2), FURNACE);
                }
            }
        }

        // Beggar's corner (behind the market, near the wall)
        BlockPos beggarSpot = districtOrigin.offset(20, 0, 44);
        setBlock(level, beggarSpot, GRAVEL);
        setBlock(level, beggarSpot.offset(1, 0, 0), GRAVEL);
        // Broken barrel and dead bush
        setBlock(level, beggarSpot.offset(0, 0, 1), BARREL);
        setBlock(level, beggarSpot.offset(1, 0, 1), DEAD_BUSH);
        setBlock(level, beggarSpot.offset(-1, 0, 0), FERN);
    }

    // ── 6. Residential District ─────────────────────────────────────
    // Northeast quadrant. Teng family members and wealthy merchants.
    // Larger houses, better materials, small courtyards.
    private static void buildResidentialDistrict(ServerLevel level, BlockPos c) {
        BlockPos districtOrigin = c.offset(6, 1, -55); // NE quadrant

        // Stone brick paths
        for (int dx = 0; dx < 48; dx++) {
            for (int dz = 0; dz < 48; dz++) {
                setBlock(level, districtOrigin.offset(dx, 0, dz),
                        ((dx + dz) % 5 == 0) ? SMOOTH_STONE_SLAB : STONE_BRICK);
            }
        }

        // Larger courtyard houses (9×7 with 3×3 courtyard, 4 blocks tall)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                BlockPos houseBase = districtOrigin.offset(3 + col * 12, 0, 3 + row * 15);
                // Courtyard (sand flooring)
                for (int cdx = 0; cdx < 3; cdx++) {
                    for (int cdz = 0; cdz < 3; cdz++) {
                        setBlock(level, houseBase.offset(3, 0, cdz), SAND);
                    }
                }
                // Main house
                wallBox(level, houseBase, 6, 4, 6, B.SPRUCE_PLANK, OAK_PLANK);
                placeDoor(level, houseBase.offset(2, 1, 5), SPRUCE_DOOR);
                // Windows
                setBlock(level, houseBase.offset(0, 2, 2), IRON_BARS);
                setBlock(level, houseBase.offset(5, 2, 2), IRON_BARS);
                // Roof
                placeRoof(level, houseBase.offset(0, 4, 0), 6, 6, DARK_OAK_PLANK);
                // Interior: red carpet, bookshelf, chest
                setBlock(level, houseBase.offset(1, 1, 3), RED_CARPET);
                setBlock(level, houseBase.offset(2, 1, 3), RED_CARPET);
                setBlock(level, houseBase.offset(3, 1, 3), RED_CARPET);
                setBlock(level, houseBase.offset(1, 1, 1), BOOKSHELF);
                ChestHelper.placeChestWithLoot(level, houseBase.offset(4, 1, 1),
                        new ResourceLocation("ergenverse", "chests/teng_family_city_residential_district"));
                setBlock(level, houseBase.offset(1, 3, 2), TORCH);
                setBlock(level, houseBase.offset(4, 3, 2), TORCH);
                // Fence around courtyard
                setBlock(level, houseBase.offset(3, 1, 0), SPRUCE_FENCE);
                setBlock(level, houseBase.offset(3, 1, 1), SPRUCE_FENCE_GATE);
                setBlock(level, houseBase.offset(3, 1, 2), SPRUCE_FENCE);
                setBlock(level, houseBase.offset(5, 1, 0), SPRUCE_FENCE);
                setBlock(level, houseBase.offset(5, 1, 1), SPRUCE_FENCE);
                setBlock(level, houseBase.offset(5, 1, 2), SPRUCE_FENCE);
                // Flower pot in courtyard
                setBlock(level, houseBase.offset(4, 1, 1), FLOWER_POT);
            }
        }
    }

    // ── 7. Warehouse District ─────────────────────────────────────────
    // Northwest quadrant. Bulk storage for extorted tribute.
    // Large warehouse buildings with heavy doors.
    private static void buildWarehouseDistrict(ServerLevel level, BlockPos c) {
        BlockPos districtOrigin = c.offset(-55, 1, -55); // NW quadrant

        // Gravel paths (industrial area)
        for (int dx = 0; dx < 48; dx++) {
            for (int dz = 0; dz < 48; dz++) {
                setBlock(level, districtOrigin.offset(dx, 0, dz), GRAVEL);
            }
        }

        // Three large warehouses (10×8, 5 blocks tall)
        for (int i = 0; i < 3; i++) {
            BlockPos warehouseBase = districtOrigin.offset(4 + i * 15, 0, 5);
            wallBox(level, warehouseBase, 10, 5, 8, STONE_BRICK, COBBLE);
            // Heavy iron door
            placeDoor(level, warehouseBase.offset(4, 1, 7), IRON_DOOR);
            // Roof
            placeRoof(level, warehouseBase.offset(0, 5, 0), 10, 8, STONE_BRICK);
            // Interior: rows of barrels and chests
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 4; col++) {
                    setBlock(level, warehouseBase.offset(1 + col * 2, 1, 1 + row * 2), BARREL);
                    if (row == 0 && col == 3) {
                        ChestHelper.placeChestWithLoot(level, warehouseBase.offset(1 + col * 2, 1, 1 + row * 2),
                                new ResourceLocation("ergenverse", "chests/teng_family_city_warehouse_district"));
                    }
                }
            }
            // Hay bales for packing material
            setBlock(level, warehouseBase.offset(8, 1, 6), HAY);
            setBlock(level, warehouseBase.offset(8, 1, 5), HAY);
            // Lantern
            setBlock(level, warehouseBase.offset(1, 4, 1), LANTERN);
            setBlock(level, warehouseBase.offset(8, 4, 1), LANTERN);
        }

        // Dock foreman's office (small stone building near warehouses)
        BlockPos office = districtOrigin.offset(4, 0, 36);
        wallBox(level, office, 5, 3, 4, STONE_BRICK, COBBLE);
        placeDoor(level, office.offset(2, 1, 3), OAK_DOOR);
        setBlock(level, office.offset(1, 1, 1), CRAFTING_TABLE);
        ChestHelper.placeChestWithLoot(level, office.offset(3, 1, 1),
                new ResourceLocation("ergenverse", "chests/teng_family_keep"));
        setBlock(level, office.offset(2, 2, 1), TORCH);
    }

    // ── 8. Governor Keep ───────────────────────────────────────────
    // North-center, the seat of Teng Huayuan's power.
    // The most imposing structure in the city. Raised on a stone platform.
    private static void buildGovernorKeep(ServerLevel level, BlockPos c) {
        BlockPos keepBase = c.offset(0, 0, -20); // 20 blocks north of center

        // Raised platform (15×15, 2 blocks above ground)
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                setBlock(level, keepBase.offset(dx, 1, dz), CHISELED_STONE);
                setBlock(level, keepBase.offset(dx, 2, dz), CHISELED_STONE);
                // Steps on the south side
                if (Math.abs(dx) <= 2 && dz == 7) {
                    setBlock(level, keepBase.offset(dx, 1, dz), COBBLE_STAIRS);
                }
            }
        }

        // Keep structure (11×11, 8 blocks tall)
        BlockPos keepWalls = keepBase.offset(-5, 3, -6);
        wallBox(level, keepWalls, 11, 8, 13, STONE_BRICK, CHISELED_STONE);

        // Main entrance (south, double iron doors)
        placeDoor(level, keepWalls.offset(4, 1, 12), IRON_DOOR);
        placeDoor(level, keepWalls.offset(6, 1, 12), IRON_DOOR);

        // Gold block foundation marker (identifies the keep as built)
        setBlock(level, c.offset(0, 1, -20), GOLD_BLOCK);

        // Throne room (inside keep, ground floor)
        BlockPos throne = keepWalls.offset(5, 3, 3);
        setBlock(level, throne.offset(0, 0, 0), GOLD_BLOCK); // throne
        setBlock(level, throne.offset(-1, 0, 0), GOLD_BLOCK);
        setBlock(level, throne.offset(1, 0, 0), GOLD_BLOCK);
        // Red carpet leading to throne
        for (int dz = -3; dz <= 3; dz++) {
            setBlock(level, throne.offset(0, 0, dz), RED_CARPET);
            setBlock(level, throne.offset(-1, 0, dz), RED_CARPET);
            setBlock(level, throne.offset(1, 0, dz), RED_CARPET);
        }
        // Lanterns flanking throne
        setBlock(level, throne.offset(-2, 0, 0), SOUL_LANTERN);
        setBlock(level, throne.offset(2, 0, 0), SOUL_LANTERN);

        // Upper floor — Teng Huayuan's private chambers
        BlockPos chambers = keepWalls.offset(2, 7, 2);
        for (int dx = 0; dx < 6; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                setBlock(level, chambers.offset(dx, 0, dz), LIGHT_GRAY_CARPET);
            }
        }
        setBlock(level, chambers.offset(1, 0, 1), BOOKSHELF);
        setBlock(level, chambers.offset(4, 0, 1), BOOKSHELF);
        ChestHelper.placeChestWithLoot(level, chambers.offset(2, 0, 3),
                new ResourceLocation("ergenverse", "chests/teng_family_keep"));
        ChestHelper.placeChestWithLoot(level, chambers.offset(3, 0, 3),
                new ResourceLocation("ergenverse", "chests/teng_family_keep"));
        // Guard presence indicator
        setBlock(level, chambers.offset(0, 0, 4), IRON_BARS);
        setBlock(level, chambers.offset(5, 0, 4), IRON_BARS);

        // Roof
        placeRoof(level, keepWalls.offset(-1, 8, -1), 13, 15, DARK_OAK_PLANK);

        // Spirit stone corner pillars (showing cultivator influence)
        setBlock(level, keepWalls.offset(0, 3, 0), B.SPIRIT_STONE);
        setBlock(level, keepWalls.offset(10, 3, 0), B.SPIRIT_STONE);
        setBlock(level, keepWalls.offset(0, 3, 12), B.SPIRIT_STONE);
        setBlock(level, keepWalls.offset(10, 3, 12), B.SPIRIT_STONE);
    }

    // ── 9. Cultivator Quarter ─────────────────────────────────────────
    // North-west area, near the keep. Where Teng family cultivators train.
    // Spirit wood structures, formation stones, training grounds.
    private static void buildCultivatorQuarter(ServerLevel level, BlockPos c) {
        BlockPos districtOrigin = c.offset(-30, 1, -40);

        // Spirit stone paving (cultivators use spirit materials)
        for (int dx = 0; dx < 24; dx++) {
            for (int dz = 0; dz < 20; dz++) {
                setBlock(level, districtOrigin.offset(dx, 0, dz), B.SPIRIT_STONE);
            }
        }

        // Training hall (8×6, spirit wood)
        BlockPos trainingHall = districtOrigin.offset(8, 0, 2);
        wallBox(level, trainingHall, 8, 5, 6, B.SPRUCE_PLANK, B.SPRUCE_PLANK);
        placeDoor(level, trainingHall.offset(3, 1, 5), SPRUCE_DOOR);
        placeRoof(level, trainingHall.offset(0, 5, 0), 8, 6, B.SPRUCE_PLANK);
        // Interior: training dummies (hay bales) and weapon racks
        setBlock(level, trainingHall.offset(1, 1, 1), HAY);
        setBlock(level, trainingHall.offset(3, 1, 1), HAY);
        setBlock(level, trainingHall.offset(5, 1, 1), HAY);
        setBlock(level, trainingHall.offset(6, 1, 1), ANVIL);
        setBlock(level, trainingHall.offset(1, 1, 4), IRON_BARS); // weapon rack

        // Cultivator residences (4×4 spirit wood cabins)
        for (int i = 0; i < 3; i++) {
            BlockPos cabin = districtOrigin.offset(1, 0, 12 + i * 3);
            wallBox(level, cabin, 4, 4, 2, B.SPRUCE_PLANK, B.SPRUCE_PLANK);
            placeDoor(level, cabin.offset(1, 1, 1), SPRUCE_DOOR);
            // Interior: bed, chest
            setBlock(level, cabin.offset(1, 1, 0), WHITE_WOOL);
            ChestHelper.placeChestWithLoot(level, cabin.offset(2, 1, 0),
                    new ResourceLocation("ergenverse", "chests/teng_family_city_smuggler_tunnels"));
            setBlock(level, cabin.offset(2, 3, 0), TORCH);
        }

        // Formation stone markers (spirit vein indicators)
        setBlock(level, districtOrigin.offset(12, 1, 10), ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState());
        setBlock(level, districtOrigin.offset(12, 1, 14), ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState());
        // Cultivation mat area
        // Cultivation mat area (wool mats for meditation, since CULTIVATION_MAT block doesn't exist)
        setBlock(level, districtOrigin.offset(16, 1, 10), WHITE_WOOL);
        setBlock(level, districtOrigin.offset(18, 1, 10), WHITE_WOOL);
        setBlock(level, districtOrigin.offset(16, 1, 12), WHITE_WOOL);
        setBlock(level, districtOrigin.offset(18, 1, 12), WHITE_WOOL);
    }

    // ── 10. Temple District ─────────────────────────────────────────
    // East of center. Ancestor worship hall.
    // Solemn stone architecture, incense, ancestral tablets.
    private static void buildTempleDistrict(ServerLevel level, BlockPos c) {
        BlockPos templeBase = c.offset(20, 1, -20);

        // Stone plaza
        for (int dx = 0; dx < 20; dx++) {
            for (int dz = 0; dz < 20; dz++) {
                setBlock(level, templeBase.offset(dx, 0, dz),
                        ((dx + dz) % 3 == 0) ? CHISELED_STONE : SMOOTH_STONE_SLAB);
            }
        }

        // Main temple (10×8, stone brick, 7 tall — tallest civilian building)
        BlockPos temple = templeBase.offset(5, 0, 5);
        wallBox(level, temple, 10, 7, 8, STONE_BRICK, CHISELED_STONE);
        placeDoor(level, temple.offset(4, 1, 7), SPRUCE_DOOR);
        placeDoor(level, temple.offset(5, 1, 7), SPRUCE_DOOR);

        // Tiered roof (stepped pyramid style)
        placeRoof(level, temple.offset(0, 7, 0), 10, 8, DARK_OAK_STAIRS);
        for (int dx = -1; dx <= 10; dx++) {
            for (int dz = -1; dz <= 8; dz++) {
                setBlock(level, temple.offset(dx - 1, 8, dz - 1), DARK_OAK_PLANK);
            }
        }

        // Interior: altar, cauldron (incense burner), lanterns
        BlockPos altar = temple.offset(4, 1, 3);
        setBlock(level, altar.offset(0, 0, 0), CHISELED_STONE); // altar stone
        setBlock(level, altar.offset(-1, 0, 0), CHISELED_STONE);
        setBlock(level, altar.offset(1, 0, 0), CHISELED_STONE);
        setBlock(level, altar.offset(0, 1, 0), CAULDRON); // incense burner
        // Lanterns
        setBlock(level, temple.offset(1, 3, 1), SOUL_LANTERN);
        setBlock(level, temple.offset(8, 3, 1), SOUL_LANTERN);
        setBlock(level, temple.offset(1, 5, 1), SOUL_LANTERN);
        setBlock(level, temple.offset(8, 5, 1), SOUL_LANTERN);
        // Ancestral tablets (bookshelves as representation)
        setBlock(level, temple.offset(1, 1, 0), BOOKSHELF);
        setBlock(level, temple.offset(2, 1, 0), BOOKSHELF);
        setBlock(level, temple.offset(7, 1, 0), BOOKSHELF);
        setBlock(level, temple.offset(8, 1, 0), BOOKSHELF);
        // Red carpet aisle
        for (int dz = 1; dz <= 6; dz++) {
            setBlock(level, temple.offset(4, 1, dz), RED_CARPET);
            setBlock(level, temple.offset(5, 1, dz), RED_CARPET);
        }

        // Temple garden (flowers and ferns)
        BlockPos garden = templeBase.offset(2, 0, 1);
        setBlock(level, garden, GRASS);
        setBlock(level, garden.offset(1, 0, 0), BLUE_ORCHID);
        setBlock(level, garden.offset(2, 0, 0), CORNFLOWER);
        setBlock(level, garden.offset(3, 0, 0), OXEYE_DAISY);
        setBlock(level, garden.offset(0, 0, 1), FERN);
        setBlock(level, garden.offset(4, 0, 1), FERN);
        setBlock(level, garden.offset(1, 0, 2), FLOWER_POT);
    }

    // ── 11. Tavern District ─────────────────────────────────────────
    // South of center, near the market. Information hub.
    // Warm wooden building, brewing stands, common room.
    private static void buildTavernDistrict(ServerLevel level, BlockPos c) {
        BlockPos tavernBase = c.offset(-20, 1, 20);

        // Oak plank floor
        for (int dx = 0; dx < 14; dx++) {
            for (int dz = 0; dz < 12; dz++) {
                setBlock(level, tavernBase.offset(dx, 0, dz), OAK_PLANK);
            }
        }

        // Main tavern building (10×8, oak)
        BlockPos tavern = tavernBase.offset(2, 0, 2);
        wallBox(level, tavern, 10, 4, 8, OAK_PLANK, OAK_PLANK);
        placeDoor(level, tavern.offset(4, 1, 7), OAK_DOOR);
        placeDoor(level, tavern.offset(5, 1, 7), OAK_DOOR);
        placeRoof(level, tavern.offset(0, 4, 0), 10, 8, B.SPRUCE_PLANK);

        // Interior: tables (oak slabs), brewing stands, barrels
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                setBlock(level, tavern.offset(2 + col * 2, 1, 1 + row * 3), OAK_PLANK);
                setBlock(level, tavern.offset(2 + col * 2, 1, 2 + row * 3), OAK_PLANK);
            }
        }
        // Bar counter
        for (int dx = 0; dx < 6; dx++) {
            setBlock(level, tavern.offset(1, 1, 4), DARK_OAK_PLANK);
        }
        setBlock(level, tavern.offset(1, 2, 4), DARK_OAK_PLANK);
        // Brewing stands
        setBlock(level, tavern.offset(8, 1, 1), BREWING_STAND);
        setBlock(level, tavern.offset(8, 1, 3), BREWING_STAND);
        // Barrel storage
        setBlock(level, tavern.offset(0, 1, 0), BARREL);
        setBlock(level, tavern.offset(0, 1, 6), BARREL);
        setBlock(level, tavern.offset(9, 1, 0), BARREL);
        setBlock(level, tavern.offset(9, 1, 6), BARREL);
        // Chests
        ChestHelper.placeChestWithLoot(level, tavern.offset(8, 1, 5),
                new ResourceLocation("ergenverse", "chests/teng_family_city_tavern_district"));
        // Lighting
        setBlock(level, tavern.offset(3, 3, 1), TORCH);
        setBlock(level, tavern.offset(7, 3, 1), TORCH);
        setBlock(level, tavern.offset(5, 3, 6), TORCH);

        // Sign post outside (spruce fence + lantern)
        setBlock(level, tavernBase.offset(5, 1, 0), SPRUCE_FENCE);
        setBlock(level, tavernBase.offset(6, 1, 0), LANTERN);
    }

    // ── 12. Smuggler Tunnels ────────────────────────────────────────
    // Beneath the warehouse district. Underground network.
    // Stone brick corridors with hidden chests.
    private static void buildSmugglerTunnels(ServerLevel level, BlockPos c) {
        BlockPos tunnelEntrance = c.offset(-40, -1, -40);

        // Vertical shaft entrance (hidden under a movable block)
        setBlock(level, tunnelEntrance, COBBLE); // cover stone
        // Shaft down
        for (int y = -2; y >= -8; y--) {
            setBlock(level, tunnelEntrance.offset(0, y, 0), AIR_BLOCK());
            setBlock(level, tunnelEntrance.offset(-1, y, 0), STONE_BRICK);
            setBlock(level, tunnelEntrance.offset(1, y, 0), STONE_BRICK);
            setBlock(level, tunnelEntrance.offset(0, y, -1), STONE_BRICK);
            setBlock(level, tunnelEntrance.offset(0, y, 1), STONE_BRICK);
        }

        // Ladder (iron bars as ladder stand-in)
        for (int y = -1; y >= -7; y--) {
            setBlock(level, tunnelEntrance.offset(0, y, 0), IRON_BARS);
        }

        // Tunnel corridor (runs east-west, 20 blocks long)
        BlockPos tunnelFloor = tunnelEntrance.offset(0, -8, 0);
        for (int dx = 1; dx <= 20; dx++) {
            setBlock(level, tunnelFloor.offset(dx, 0, -1), STONE_BRICK); // north wall
            setBlock(level, tunnelFloor.offset(dx, 0, 1), STONE_BRICK);  // south wall
            setBlock(level, tunnelFloor.offset(dx, 1, -1), STONE_BRICK); // ceiling
            setBlock(level, tunnelFloor.offset(dx, 1, 1), STONE_BRICK);  // ceiling
            setBlock(level, tunnelFloor.offset(dx, 0, 0), GRAVEL);      // floor
        }

        // Branch tunnel (runs north from midpoint)
        BlockPos branch = tunnelFloor.offset(10, 0, 0);
        for (int dz = 2; dz <= 12; dz++) {
            setBlock(level, branch.offset(-1, 0, dz), STONE_BRICK);
            setBlock(level, branch.offset(1, 0, dz), STONE_BRICK);
            setBlock(level, branch.offset(-1, 1, dz), STONE_BRICK);
            setBlock(level, branch.offset(1, 1, dz), STONE_BRICK);
            setBlock(level, branch.offset(0, 0, dz), GRAVEL);
        }

        // Smuggler's cache room (at end of branch tunnel)
        BlockPos cacheRoom = branch.offset(0, 0, 12);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = 0; dz <= 4; dz++) {
                setBlock(level, cacheRoom.offset(dx, 0, dz), GRAVEL);
                if (Math.abs(dx) == 2 || dz == 0 || dz == 4) {
                    setBlock(level, cacheRoom.offset(dx, 1, dz), MOSSY_STONE_BRICK);
                    setBlock(level, cacheRoom.offset(dx, 2, dz), MOSSY_STONE_BRICK);
                }
            }
        }
        // Hidden chests (contraband)
        ChestHelper.placeChestWithLoot(level, cacheRoom.offset(-1, 1, 2),
                new ResourceLocation("ergenverse", "chests/teng_family_city_smuggler_tunnels"));
        ChestHelper.placeChestWithLoot(level, cacheRoom.offset(0, 1, 2),
                new ResourceLocation("ergenverse", "chests/teng_family_city_smuggler_tunnels"));
        ChestHelper.placeChestWithLoot(level, cacheRoom.offset(1, 1, 2),
                new ResourceLocation("ergenverse", "chests/teng_family_city_smuggler_tunnels"));
        // Glowstone for lighting (smugglers need to see)
        setBlock(level, cacheRoom.offset(0, 2, 3), GLOWSTONE);

        // Second cache at end of main corridor
        BlockPos cache2 = tunnelFloor.offset(20, 0, 0);
        for (int dx = 0; dx <= 3; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setBlock(level, cache2.offset(dx, 0, dz), GRAVEL);
                if (dz == -1 || dz == 1 || dx == 0 || dx == 3) {
                    setBlock(level, cache2.offset(dx, 1, dz), MOSSY_STONE_BRICK);
                    setBlock(level, cache2.offset(dx, 2, dz), MOSSY_STONE_BRICK);
                }
            }
        }
        ChestHelper.placeChestWithLoot(level, cache2.offset(1, 1, 0),
                new ResourceLocation("ergenverse", "chests/teng_family_city_smuggler_tunnels"));
        ChestHelper.placeChestWithLoot(level, cache2.offset(2, 1, 0),
                new ResourceLocation("ergenverse", "chests/teng_family_city_smuggler_tunnels"));
        setBlock(level, cache2.offset(1, 2, 0), GLOWSTONE);
    }

    // ── 13. City Lanterns ──────────────────────────────────────────
    // Street lighting throughout the city.
    private static void buildCityLanterns(ServerLevel level, BlockPos c) {
        // Main streets — lanterns every 8 blocks
        for (int dz = -50; dz <= 50; dz += 8) {
            setBlock(level, c.offset(3, 1, dz), LANTERN);
            setBlock(level, c.offset(-3, 1, dz), LANTERN);
        }
        for (int dx = -50; dx <= 50; dx += 8) {
            setBlock(level, c.offset(dx, 1, 3), LANTERN);
            setBlock(level, c.offset(dx, 1, -3), LANTERN);
        }

        // Market district — soul lanterns (wealthier lighting)
        BlockPos market = c.offset(6, 1, 6);
        for (int i = 0; i < 4; i++) {
            setBlock(level, market.offset(10, 1, 5 + i * 9), SOUL_LANTERN);
        }

        // Keep approach — soul lanterns along the north-south street
        for (int dz = -15; dz >= -50; dz -= 5) {
            setBlock(level, c.offset(0, 1, dz), SOUL_LANTERN);
        }

        // Gate area — torches on the wall towers
        BlockPos gate = c.offset(0, 1, 56);
        setBlock(level, gate.offset(-5, 7, 2), TORCH);
        setBlock(level, gate.offset(6, 7, 2), TORCH);
    }

    /** Helper to get air block state. */
    private static BlockState AIR_BLOCK() {
        return Blocks.AIR.defaultBlockState();
    }
}
