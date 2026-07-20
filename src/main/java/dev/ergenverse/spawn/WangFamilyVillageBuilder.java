package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.item.ErgenverseItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * WangFamilyVillageBuilder — places the player's starting village at world
 * spawn using the mod's custom blocks.
 *
 * <p>The village is the player's first experience of the Ergenverse. Per the
 * user's directive, it must be built from <b>custom</b> blocks (spirit wood,
 * spirit stone, custom herbs, alchemy furnace, formation stones, spirit vein)
 * so the player immediately sees they are not in vanilla Minecraft.
 *
 * <p>Layout (23x23 footprint, centered on world spawn):
 * <ul>
 *   <li>Central plaza (spirit stone) with a spirit vein centerpiece +
 *       4 formation core stones forming a small array.</li>
 *   <li>4 corner houses (spirit wood planks + log pillars + leaves roof):
 *     <ul>
 *       <li>NW — Alchemy Pavilion (contains an Alchemy Furnace)</li>
 *       <li>NE — Formation Hall (contains a Formation Flag Base)</li>
 *       <li>SW — Storage (contains a Chest with starter gear)</li>
 *       <li>SE — Dwelling (empty, for the player)</li>
 *     </ul></li>
 *   <li>Herb garden south of the plaza (6 custom herb species).</li>
 *   <li>3 decorative spirit wood trees at the edges.</li>
 *   <li>Torches around the plaza for light.</li>
 * </ul>
 *
 * <p>Idempotent: {@link #isAlreadyBuilt(ServerLevel)} checks whether the
 * spirit vein centerpiece is already in place, so the village is only built
 * once per world.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public final class WangFamilyVillageBuilder {

    private WangFamilyVillageBuilder() {}

    /** Village half-extent. Total footprint = (2*RADIUS+1) squared. */
    public static final int VILLAGE_RADIUS = 11;

    /**
     * The village center is the world shared spawn position. The player
     * spawns here, so the village is built around them.
     */
    public static BlockPos getVillageCenter(ServerLevel level) {
        return level.getSharedSpawnPos();
    }

    /**
     * Returns true if the village has already been built (the spirit vein
     * centerpiece block is present above the spawn point).
     */
    public static boolean isAlreadyBuilt(ServerLevel level) {
        BlockPos center = getVillageCenter(level);
        return level.getBlockState(center.above()).getBlock()
                == ErgenverseBlocks.SPIRIT_VEIN_STONE.get();
    }

    /**
     * Build the village. Flattens a 23x23 area at spawn height and places
     * all structures. Safe to call once per world (guard with
     * {@link #isAlreadyBuilt}).
     */
    public static void build(ServerLevel level) {
        BlockPos center = getVillageCenter(level);
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        Ergenverse.LOGGER.info("[Ergenverse] Building Wang Family Village at ({}, {}, {}).",
                cx, cy, cz);

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState spiritGrass = ErgenverseBlocks.SPIRIT_GRASS.get().defaultBlockState();
        BlockState spiritStone = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
        BlockState spiritSand = ErgenverseBlocks.SPIRIT_SAND.get().defaultBlockState();

        // ── 1. Flatten the area ────────────────────────────────────────
        // Ground level = cy. Clear 4 blocks above for headroom.
        for (int dx = -VILLAGE_RADIUS; dx <= VILLAGE_RADIUS; dx++) {
            for (int dz = -VILLAGE_RADIUS; dz <= VILLAGE_RADIUS; dz++) {
                BlockPos ground = new BlockPos(cx + dx, cy, cz + dz);
                // Clear above
                for (int h = 1; h <= 4; h++) {
                    level.setBlock(ground.above(h), air, 3);
                }
                // Floor
                double dist = Math.sqrt((double) dx * dx + (double) dz * dz);
                if (dist <= 3.5) {
                    // central plaza
                    level.setBlock(ground, spiritStone, 3);
                } else if (Math.abs(dx) <= 1 || Math.abs(dz) <= 1) {
                    // cross paths
                    level.setBlock(ground, spiritSand, 3);
                } else {
                    level.setBlock(ground, spiritGrass, 3);
                }
            }
        }

        // ── 2. Central spirit vein + formation array ───────────────────
        level.setBlock(center.above(),
                ErgenverseBlocks.SPIRIT_VEIN_STONE.get().defaultBlockState(), 3);
        level.setBlock(center.above().north(2),
                ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState(), 3);
        level.setBlock(center.above().south(2),
                ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState(), 3);
        level.setBlock(center.above().east(2),
                ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState(), 3);
        level.setBlock(center.above().west(2),
                ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState(), 3);

        // ── 3. Four corner houses ──────────────────────────────────────
        // Each house is 5x5, floor at cy, walls cy+1..cy+2, roof at cy+3.
        buildHouse(level, cx - 8, cy, cz - 8, "alchemy");     // NW
        buildHouse(level, cx + 4, cy, cz - 8, "formation");   // NE
        buildHouse(level, cx - 8, cy, cz + 4, "storage");     // SW
        buildHouse(level, cx + 4, cy, cz + 4, "dwelling");    // SE

        // ── 4. Herb garden (south of plaza) ────────────────────────────
        buildHerbGarden(level, cx, cy + 1, cz + 6);

        // ── 5. Decorative spirit wood trees ────────────────────────────
        buildTree(level, cx - VILLAGE_RADIUS + 1, cy + 1, cz - VILLAGE_RADIUS + 1);
        buildTree(level, cx + VILLAGE_RADIUS - 1, cy + 1, cz - VILLAGE_RADIUS + 1);
        buildTree(level, cx - VILLAGE_RADIUS + 1, cy + 1, cz + VILLAGE_RADIUS - 1);
        buildTree(level, cx + VILLAGE_RADIUS - 1, cy + 1, cz + VILLAGE_RADIUS - 1);

        // ── 6. Spirit Vein Stones around the plaza (custom light markers) ──
        //     (Replaces vanilla torches — 0 vanilla blocks in the village.)
        for (int angle = 0; angle < 360; angle += 45) {
            double rad = Math.toRadians(angle);
            int tx = cx + (int) Math.round(Math.cos(rad) * 5);
            int tz = cz + (int) Math.round(Math.sin(rad) * 5);
            level.setBlock(new BlockPos(tx, cy + 1, tz),
                    ErgenverseBlocks.SPIRIT_VEIN_STONE.get().defaultBlockState(), 3);
        }

        Ergenverse.LOGGER.info("[Ergenverse] Wang Family Village construction complete.");
    }

    /**
     * Build a 5x5 house. Floor at {@code y}, walls at y+1 and y+2, roof at
     * y+3. Corner pillars are spirit wood logs; walls are spirit wood planks;
     * roof is spirit wood leaves. A 2-block-tall air gap on the south face
     * (dz+4) serves as a doorway.
     *
     * @param type "alchemy", "formation", "storage", or "dwelling"
     */
    private static void buildHouse(ServerLevel level, int x, int y, int z, String type) {
        BlockState planks = ErgenverseBlocks.SPIRIT_WOOD_PLANKS.get().defaultBlockState();
        BlockState log = ErgenverseBlocks.SPIRIT_WOOD_LOG.get().defaultBlockState();
        BlockState leaves = ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                for (int dy = 0; dy <= 3; dy++) {
                    BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                    boolean edge = dx == 0 || dx == 4 || dz == 0 || dz == 4;
                    boolean corner = (dx == 0 || dx == 4) && (dz == 0 || dz == 4);
                    boolean isDoorway = dz == 4 && dx == 2 && (dy == 1 || dy == 2);

                    if (dy == 0) {
                        // floor
                        level.setBlock(pos, planks, 3);
                    } else if (dy == 3) {
                        // roof
                        level.setBlock(pos, leaves, 3);
                    } else if (isDoorway) {
                        // doorway gap
                        level.setBlock(pos, air, 3);
                    } else if (corner) {
                        // corner pillar
                        level.setBlock(pos, log, 3);
                    } else if (edge) {
                        // wall
                        level.setBlock(pos, planks, 3);
                    } else {
                        // interior air
                        level.setBlock(pos, air, 3);
                    }
                }
            }
        }

        // House-specific interior contents at the center (x+2, y+1, z+2)
        BlockPos interior = new BlockPos(x + 2, y + 1, z + 2);
        switch (type) {
            case "alchemy" -> level.setBlock(interior,
                    ErgenverseBlocks.ALCHEMY_FURNACE.get().defaultBlockState(), 3);
            case "formation" -> level.setBlock(interior,
                    ErgenverseBlocks.FORMATION_FLAG_BASE.get().defaultBlockState(), 3);
            case "storage" -> {
                // No vanilla chest — place a Formation Flag Base as a marker.
                // Starter gear is given directly to the player on first spawn
                // (see SpawnEventHandler).
                level.setBlock(interior,
                        ErgenverseBlocks.FORMATION_FLAG_BASE.get().defaultBlockState(), 3);
            }
            case "dwelling" -> {
                // empty dwelling for the player — leave air so they can
                // place their own bed. (Beds are 2-tall and need directional
                // placement; avoided for robustness.)
            }
            default -> { /* air */ }
        }
    }

    /**
     * Build a 5x3 herb garden at the given position (herbs placed at y,
     * sitting on the grass at y-1).
     */
    private static void buildHerbGarden(ServerLevel level, int x, int y, int z) {
        BlockState[] herbs = {
                ErgenverseBlocks.QI_GATHERING_GRASS.get().defaultBlockState(),
                ErgenverseBlocks.SNOW_HEART_HERB.get().defaultBlockState(),
                ErgenverseBlocks.FIVE_COLOR_GINSENG.get().defaultBlockState(),
                ErgenverseBlocks.NINE_LEAF_CLOVER.get().defaultBlockState(),
                ErgenverseBlocks.SOUL_NOURISHING_LOTUS.get().defaultBlockState(),
                ErgenverseBlocks.FIRE_BLOOM_LOTUS.get().defaultBlockState(),
                ErgenverseBlocks.VERMILION_BLOOD_GINSENG.get().defaultBlockState(),
                ErgenverseBlocks.SWORD_EDGE_MOSS.get().defaultBlockState(),
        };
        int i = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(new BlockPos(x + dx, y, z + dz),
                        herbs[i % herbs.length], 3);
                i++;
            }
        }
    }

    /**
     * Build a small decorative spirit wood tree (4-block trunk + leaf canopy).
     */
    private static void buildTree(ServerLevel level, int x, int y, int z) {
        BlockState log = ErgenverseBlocks.SPIRIT_WOOD_LOG.get().defaultBlockState();
        BlockState leaves = ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get().defaultBlockState();

        // trunk
        for (int dy = 0; dy < 4; dy++) {
            level.setBlock(new BlockPos(x, y + dy, z), log, 3);
        }
        // canopy
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 3; dy <= 5; dy++) {
                    if (dx == 0 && dz == 0 && dy <= 3) continue;
                    double dist = Math.sqrt((double) dx * dx + (double) dz * dz);
                    if (dist <= 2.0) {
                        BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                        if (level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, leaves, 3);
                        }
                    }
                }
            }
        }
    }
}
