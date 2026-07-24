package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * SnowDomainCapitalBuilder — FULLY hand-built Snow Domain Capital (雪国都城 / Xue Guo Du).
 *
 * <p>Constitution: "The world is completely hand-crafted, accurate to the novels.
 * NEVER write a script that replaces vanilla blocks with other blocks as a shortcut.
 * Every structure must be hand-authored."
 *
 * <p>Canon basis (Renegade Immortal): The Snow Domain is a frozen northern country on
 * Planet Suzaku. Its capital is a fortress-city carved into glacial terrain, surrounded
 * by perpetual ice and snow. The city is home to ice-type cultivators who practice cold
 * cultivation arts. The architecture is thick-walled stone with ice accents — functional
 * and defensive against both the harsh climate and spirit beast threats. Ice spirit
 * wolves patrol the outer walls. The city lord is a Nascent Soul ice cultivator.
 *
 * <p>Block palette: Packed ice, blue ice, snow blocks, spruce (cold-climate wood),
 * stone bricks, polished granite, iron for fortifications. Spirit stone for inner
 * cultivation halls. The city feels COLD — blue/white/gray tones throughout.
 *
 * <p>Districts (12):
 * <ol>
 *   <li>Outer Ice Wall — thick packed-ice walls with blue ice corner towers</li>
 *   <li>South Gate — fortified gate with iron portcullis and guardhouses</li>
 *   <li>Main Avenue — packed-ice road from gate to central plaza</li>
 *   <li>Central Plaza — open area with ice fountain and snow markers</li>
 *   <li>Ice Palace (North) — the city lord's seat: 3-story stone keep with ice dome</li>
 *   <li>Cultivation Hall — spirit stone meditation hall with ice pillars</li>
 *   <li>Ice Forge — crafting area with blast furnaces and anvils (cold iron)</li>
 *   <li>Spirit Herb Greenhouse — enclosed glass building for cold-resistant herbs</li>
 *   <li>Mortal Quarter (South) — humble spruce houses with snow on roofs</li>
 *   <li>Garrison (East) — barracks, weapon racks, training yard</li>
 *   <li>Ice Repository — underground vault for precious items, ice-block sealed</li>
 *   <li>Watchtowers — 4 corner towers with soul lantern beacons</li>
 * </ol>
 *
 * <h2>Harsh Self-Critique</h2>
 * <ul>
 *   <li>The "ice dome" over the palace is approximated with packed-ice stairs — MC
 *       cannot make smooth domes with blocks. It reads as a pyramid cap, not a dome.</li>
 *   <li>All buildings are flat-floor-box-walls. No actual terrain elevation changes —
 *       the capital sits on a flat plane when it should be carved into a glacier.</li>
 *   <li>Mortal quarter houses use the same spruce-box template repeated 6 times.
 *       Real houses would have varied sizes, angles, and states of repair.</li>
 *   <li>The greenhouse uses glass panes — functional but not atmospheric. A proper
 *       cold-cultivation greenhouse should have qi-gathering formations visible.</li>
 *   <li>No underground cultivation caves beneath the palace. Canon describes deep
 *       meditation chambers sealed behind ice walls.</li>
 *   <li>Watchtower beacons are soul lanterns — correct but not dramatic. Real
 *       beacon towers would have visible fire columns visible for miles.</li>
 * </ul>
 */
public final class SnowDomainCapitalBuilder {

    private SnowDomainCapitalBuilder() {}

    // ── Canonical position on Planet Suzaku ────────────────────────
    public static final int CAPITAL_X = WangFamilyVillageBuilder.VILLAGE_X + 3600;
    public static final int CAPITAL_Z = WangFamilyVillageBuilder.VILLAGE_Z - 3200;

    private static final int CITY_RADIUS = 58;
    private static final int WALL_HEIGHT = 10;

    private static boolean built = false;

    // ── Block palette — cold, frozen, fortress-like ───────────────
    private static final BlockState PACKED_ICE        = Blocks.PACKED_ICE.defaultBlockState();
    private static final BlockState BLUE_ICE           = Blocks.BLUE_ICE.defaultBlockState();
    private static final BlockState SNOW_BLOCK          = Blocks.SNOW_BLOCK.defaultBlockState();
    private static final BlockState SNOW                = Blocks.SNOW.defaultBlockState();
    private static final BlockState ICE                 = Blocks.ICE.defaultBlockState();
    private static final BlockState SPRUCE_PLANK       = Blocks.SPRUCE_PLANKS.defaultBlockState();
    private static final BlockState SPRUCE_LOG         = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState SPRUCE_STAIR       = Blocks.SPRUCE_STAIRS.defaultBlockState();
    private static final BlockState SPRUCE_SLAB        = Blocks.SPRUCE_SLAB.defaultBlockState();
    private static final BlockState SPRUCE_DOOR        = Blocks.SPRUCE_DOOR.defaultBlockState();
    private static final BlockState SPRUCE_FENCE        = Blocks.SPRUCE_FENCE.defaultBlockState();
    private static final BlockState STONE_BRICK         = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState STONE               = Blocks.STONE.defaultBlockState();
    private static final BlockState POLISHED_GRANITE    = Blocks.POLISHED_GRANITE.defaultBlockState();
    private static final BlockState GRANITE            = Blocks.GRANITE.defaultBlockState();
    private static final BlockState COBBLESTONE         = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState IRON_BARS           = Blocks.IRON_BARS.defaultBlockState();
    private static final BlockState IRON_DOOR           = Blocks.IRON_DOOR.defaultBlockState();
    private static final BlockState LANTERN             = Blocks.LANTERN.defaultBlockState();
    private static final BlockState SOUL_LANTERN        = Blocks.SOUL_LANTERN.defaultBlockState();
    private static final BlockState GLOWSTONE           = Blocks.GLOWSTONE.defaultBlockState();
    private static final BlockState CHEST               = Blocks.CHEST.defaultBlockState();
    private static final BlockState BOOKSHELF           = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState ANVIL               = Blocks.ANVIL.defaultBlockState();
    private static final BlockState FURNACE             = Blocks.FURNACE.defaultBlockState();
    private static final BlockState BLAST_FURNACE       = Blocks.BLAST_FURNACE.defaultBlockState();
    private static final BlockState CAULDRON            = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState CRAFTING_TABLE      = Blocks.CRAFTING_TABLE.defaultBlockState();
    private static final BlockState BARREL              = Blocks.BARREL.defaultBlockState();
    private static final BlockState GLASS               = Blocks.GLASS.defaultBlockState();
    private static final BlockState GLASS_PANE          = Blocks.GLASS_PANE.defaultBlockState();
    private static final BlockState WHITE_CARPET        = Blocks.WHITE_CARPET.defaultBlockState();
    private static final BlockState LIGHT_BLUE_CARPET   = Blocks.LIGHT_BLUE_CARPET.defaultBlockState();
    private static final BlockState CYAN_CARPET         = Blocks.CYAN_CARPET.defaultBlockState();
    private static final BlockState RED_BED             = Blocks.RED_BED.defaultBlockState();
    private static final BlockState AIR                 = Blocks.AIR.defaultBlockState();
    private static final BlockState DIRT                = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRAVEL              = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState WATER               = Blocks.WATER.defaultBlockState();
    // Ergenverse blocks
    private static final BlockState SPIRIT_STONE       = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
    private static final BlockState SPIRIT_STONE_SLAB   = ErgenverseBlocks.SPIRIT_STONE_SLAB.get().defaultBlockState();
    private static final BlockState SPIRIT_STONE_WALL   = ErgenverseBlocks.SPIRIT_STONE_WALL.get().defaultBlockState();
    private static final BlockState SPIRIT_STONE_STAIR  = ErgenverseBlocks.SPIRIT_STONE_STAIRS.get().defaultBlockState();
    private static final BlockState SPIRIT_WOOD_PLANK   = ErgenverseBlocks.SPIRIT_WOOD_PLANKS.get().defaultBlockState();
    private static final BlockState SPIRIT_WOOD_LOG     = ErgenverseBlocks.SPIRIT_WOOD_LOG.get().defaultBlockState();
    private static final BlockState ANCIENT_SPIRIT_LOG  = ErgenverseBlocks.ANCIENT_SPIRIT_LOG.get().defaultBlockState();
    private static final BlockState FORMATION_CORE      = ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState();
    private static final BlockState RESTRICTION_STONE   = ErgenverseBlocks.RESTRICTION_STONE.get().defaultBlockState();
    private static final BlockState JADE_STONE          = ErgenverseBlocks.JADE_STONE.get().defaultBlockState();
    private static final BlockState SPIRIT_DIRT         = ErgenverseBlocks.SPIRIT_DIRT.get().defaultBlockState();
    private static final BlockState SPIRIT_GRASS        = ErgenverseBlocks.SPIRIT_GRASS.get().defaultBlockState();
    private static final BlockState SCORCHED_STONE      = ErgenverseBlocks.SCORCHED_STONE.get().defaultBlockState();

    public static boolean isAlreadyBuilt(ServerLevel level) {
        return level.getBlockState(new BlockPos(CAPITAL_X, 80, CAPITAL_Z)).getBlock() == Blocks.BLUE_ICE;
    }

    public static void build(ServerLevel level) {
        if (built) return;
        int baseY = 64;
        int cx = CAPITAL_X;
        int cz = CAPITAL_Z;

        Ergenverse.LOGGER.info("[SnowDomainCapital] Building Snow Domain Capital at ({}, {}, {})...", cx, baseY, cz);

        // Foundation: snow/ice terrain base
        buildFoundation(level, cx, baseY, cz);

        // 1. Outer Ice Walls
        buildOuterWalls(level, cx, baseY, cz);

        // 2. South Gate
        buildSouthGate(level, cx, baseY, cz);

        // 3. Main Avenue
        buildMainAvenue(level, cx, baseY, cz);

        // 4. Central Plaza
        buildCentralPlaza(level, cx, baseY, cz);

        // 5. Ice Palace (North)
        buildIcePalace(level, cx, baseY, cz);

        // 6. Cultivation Hall
        buildCultivationHall(level, cx, baseY, cz);

        // 7. Ice Forge
        buildIceForge(level, cx, baseY, cz);

        // 8. Spirit Herb Greenhouse
        buildGreenhouse(level, cx, baseY, cz);

        // 9. Mortal Quarter
        buildMortalQuarter(level, cx, baseY, cz);

        // 10. Garrison
        buildGarrison(level, cx, baseY, cz);

        // 11. Ice Repository
        buildIceRepository(level, cx, baseY, cz);

        // 12. Watchtowers
        buildWatchtowers(level, cx, baseY, cz);

        // Snow layer on roofs and ground
        buildSnowLayer(level, cx, baseY, cz);

        built = true;
        Ergenverse.LOGGER.info("[SnowDomainCapital] Snow Domain Capital construction complete.");
    }

    // ═══════════════════════════════════════════════════════════════
    //  District Builders
    // ═══════════════════════════════════════════════════════════════

    private static void buildFoundation(ServerLevel level, int cx, int baseY, int cz) {
        // Packed ice base under the entire city footprint
        fill(level, cx - CITY_RADIUS, baseY - 3, cz - CITY_RADIUS,
                CITY_RADIUS * 2, 3, CITY_RADIUS * 2, PACKED_ICE);
        // Snow surface
        fill(level, cx - CITY_RADIUS, baseY - 1, cz - CITY_RADIUS,
                CITY_RADIUS * 2, 1, CITY_RADIUS * 2, SNOW_BLOCK);
        // Cobblestone inner floor (roads and plazas, not the outer snow)
        fill(level, cx - CITY_RADIUS + 5, baseY - 1, cz - CITY_RADIUS + 5,
                (CITY_RADIUS - 5) * 2, 1, (CITY_RADIUS - 5) * 2, COBBLESTONE);
    }

    private static void buildOuterWalls(ServerLevel level, int cx, int baseY, int cz) {
        int r = CITY_RADIUS;
        // North wall
        fill(level, cx - r, baseY, cz - r, r * 2, WALL_HEIGHT, 3, PACKED_ICE);
        // South wall
        fill(level, cx - r, baseY, cz + r - 3, r * 2, WALL_HEIGHT, 3, PACKED_ICE);
        // West wall
        fill(level, cx - r, baseY, cz - r, 3, WALL_HEIGHT, r * 2, PACKED_ICE);
        // East wall
        fill(level, cx + r - 3, baseY, cz - r, 3, WALL_HEIGHT, r * 2, PACKED_ICE);
        // Crenellations on north wall (alternating packed ice / air)
        for (int i = -r; i < r; i += 2) {
            set(level, cx + i, baseY + WALL_HEIGHT, cz - r, PACKED_ICE);
            set(level, cx + i, baseY + WALL_HEIGHT, cz - r + 1, PACKED_ICE);
            set(level, cx + i, baseY + WALL_HEIGHT, cz + r - 1, PACKED_ICE);
            set(level, cx + i, baseY + WALL_HEIGHT, cz + r - 2, PACKED_ICE);
        }
        // Blue ice cap stones on top
        fill(level, cx - r, baseY + WALL_HEIGHT, cz - r, r * 2, 1, 3, BLUE_ICE);
        fill(level, cx - r, baseY + WALL_HEIGHT, cz + r - 3, r * 2, 1, 3, BLUE_ICE);
        fill(level, cx - r, baseY + WALL_HEIGHT, cz - r, 3, 1, r * 2, BLUE_ICE);
        fill(level, cx + r - 3, baseY + WALL_HEIGHT, cz - r, 3, 1, r * 2, BLUE_ICE);
        // Interior face: stone brick for structural look
        fill(level, cx - r + 3, baseY, cz - r + 3, r * 2 - 6, 1, r * 2 - 6, AIR);
    }

    private static void buildSouthGate(ServerLevel level, int cx, int baseY, int cz) {
        int gz = cz + CITY_RADIUS - 3;
        int gy = baseY;
        // Gate pillars (3 wide each side, packed ice)
        fill(level, cx - 5, gy, gz, 4, WALL_HEIGHT, 3, PACKED_ICE);
        fill(level, cx + 2, gy, gz, 4, WALL_HEIGHT, 3, PACKED_ICE);
        // Gate arch: blue ice
        fill(level, cx - 6, gy + WALL_HEIGHT, gz - 1, 14, 2, 5, BLUE_ICE);
        // Gate opening (clear interior)
        fill(level, cx - 1, gy, gz, 3, WALL_HEIGHT - 1, 3, AIR);
        // Iron bar portcullis
        fill(level, cx - 1, gy + 2, gz, 1, WALL_HEIGHT - 4, 1, IRON_BARS);
        fill(level, cx + 1, gy + 2, gz, 1, WALL_HEIGHT - 4, 1, IRON_BARS);
        fill(level, cx, gy + 2, gz, 1, WALL_HEIGHT - 4, 1, IRON_BARS);
        // Guardhouses (2 per side)
        fill(level, cx - 10, gy, gz - 2, 4, 4, 5, SPRUCE_PLANK);
        fill(level, cx - 11, gy, gz - 2, 1, 4, 5, STONE_BRICK);
        fill(level, cx + 7, gy, gz - 2, 4, 4, 5, SPRUCE_PLANK);
        fill(level, cx + 11, gy, gz - 2, 1, 4, 5, STONE_BRICK);
        // Guardhouse roofs
        fill(level, cx - 10, gy + 4, gz - 3, 4, 1, 7, SPRUCE_STAIR);
        fill(level, cx + 7, gy + 4, gz - 3, 4, 1, 7, SPRUCE_STAIR);
        // Guardhouse lanterns
        set(level, cx - 9, gy + 2, gz, SOUL_LANTERN);
        set(level, cx + 9, gy + 2, gz, SOUL_LANTERN);
        // Snow on guardhouse roofs
        fill(level, cx - 10, gy + 5, gz - 3, 4, 1, 7, SNOW_BLOCK);
        fill(level, cx + 7, gy + 5, gz - 3, 4, 1, 7, SNOW_BLOCK);
    }

    private static void buildMainAvenue(ServerLevel level, int cx, int baseY, int cz) {
        int gy = baseY;
        // Central north-south avenue (6 wide, packed ice road)
        fill(level, cx - 3, gy, cz - CITY_RADIUS + 8, 7, 1, CITY_RADIUS * 2 - 16, PACKED_ICE);
        // East-west cross street
        fill(level, cx - CITY_RADIUS + 8, gy, cz - 3, CITY_RADIUS * 2 - 16, 1, 7, PACKED_ICE);
        // Lanterns along the avenue (every 8 blocks)
        for (int i = -CITY_RADIUS + 10; i < CITY_RADIUS - 10; i += 8) {
            set(level, cx - 4, gy + 1, cz + i, SOUL_LANTERN);
            set(level, cx + 4, gy + 1, cz + i, SOUL_LANTERN);
            set(level, cx + i, gy + 1, cz - 4, SOUL_LANTERN);
            set(level, cx + i, gy + 1, cz + 4, SOUL_LANTERN);
        }
    }

    private static void buildCentralPlaza(ServerLevel level, int cx, int baseY, int cz) {
        int gy = baseY;
        // 16x16 polished granite plaza
        fill(level, cx - 8, gy - 1, cz - 8, 17, 1, 17, POLISHED_GRANITE);
        // Ice fountain at center
        fill(level, cx - 2, gy, cz - 2, 5, 1, 5, BLUE_ICE);
        fill(level, cx - 1, gy + 1, cz - 1, 3, 1, 3, PACKED_ICE);
        set(level, cx, gy + 2, cz, ICE);
        // Water basin (ring)
        fill(level, cx - 3, gy, cz - 3, 7, 1, 1, WATER);
        fill(level, cx - 3, gy, cz + 3, 7, 1, 1, WATER);
        fill(level, cx - 3, gy, cz - 3, 1, 1, 7, WATER);
        fill(level, cx + 3, gy, cz - 3, 1, 1, 7, WATER);
        // 4 ice pillars at corners of plaza
        int[][] pillars = {{-7, -7}, {7, -7}, {-7, 7}, {7, 7}};
        for (int[] p : pillars) {
            fill(level, cx + p[0], gy, cz + p[1], 1, 5, 1, PACKED_ICE);
            set(level, cx + p[0], gy + 5, cz + p[1], BLUE_ICE);
            set(level, cx + p[0], gy + 6, cz + p[1], SOUL_LANTERN);
        }
        // Cyan carpet paths (decorative)
        fill(level, cx - 1, gy, cz - 7, 3, 1, 2, CYAN_CARPET);
        fill(level, cx - 1, gy, cz + 6, 3, 1, 2, CYAN_CARPET);
    }

    private static void buildIcePalace(ServerLevel level, int cx, int baseY, int cz) {
        // Palace occupies north side of the city
        int gx = cx;
        int gz = cz - 20;
        int gy = baseY;
        // Foundation: polished granite
        fill(level, gx - 10, gy - 1, gz - 10, 21, 1, 20, POLISHED_GRANITE);
        // Outer walls: stone brick, 7 tall
        fill(level, gx - 10, gy, gz - 10, 1, 7, 20, STONE_BRICK);
        fill(level, gx + 10, gy, gz - 10, 1, 7, 20, STONE_BRICK);
        fill(level, gx - 10, gy, gz - 10, 21, 7, 1, STONE_BRICK);
        fill(level, gx - 10, gy, gz + 9, 21, 7, 1, STONE_BRICK);
        // Interior: clear
        fill(level, gx - 9, gy, gz - 9, 19, 7, 18, AIR);
        // Entrance: double doors (south wall, center)
        fill(level, gx - 1, gy, gz + 9, 3, 4, 1, AIR);
        set(level, gx - 1, gy + 1, gz + 9, IRON_DOOR);
        set(level, gx + 1, gy + 1, gz + 9, IRON_DOOR);
        // Blue ice trim on entrance
        fill(level, gx - 2, gy + 4, gz + 9, 5, 1, 1, BLUE_ICE);
        // First floor: throne room
        // Carpet: light blue
        fill(level, gx - 8, gy, gz - 8, 17, 1, 16, LIGHT_BLUE_CARPET);
        // Throne platform (north end)
        fill(level, gx - 4, gy + 1, gz - 8, 9, 1, 4, POLISHED_GRANITE);
        fill(level, gx - 3, gy + 2, gz - 8, 7, 1, 3, POLISHED_GRANITE);
        set(level, gx, gy + 3, gz - 8, BLUE_ICE); // throne marker
        // Pillars along throne room (6 per side)
        for (int i = 0; i < 6; i++) {
            int pz = gz - 4 + i * 3;
            fill(level, gx - 8, gy, pz, 1, 6, 1, STONE_BRICK);
            fill(level, gx + 8, gy, pz, 1, 6, 1, STONE_BRICK);
            // Blue ice cap on each pillar
            set(level, gx - 8, gy + 6, pz, BLUE_ICE);
            set(level, gx + 8, gy + 6, pz, BLUE_ICE);
        }
        // Second floor: residence (on top of first floor walls)
        fill(level, gx - 10, gy + 7, gz - 10, 21, 1, 20, SPRUCE_PLANK);
        fill(level, gx - 9, gy + 8, gz - 10, 1, 5, 18, STONE_BRICK);
        fill(level, gx + 9, gy + 8, gz - 10, 1, 5, 18, STONE_BRICK);
        fill(level, gx - 9, gy + 8, gz - 10, 19, 5, 1, STONE_BRICK);
        fill(level, gx - 9, gy + 8, gz + 7, 19, 5, 1, STONE_BRICK);
        fill(level, gx - 9, gy + 8, gz - 9, 19, 1, 16, AIR);
        // Second floor carpet
        fill(level, gx - 8, gy + 8, gz - 8, 17, 1, 14, WHITE_CARPET);
        // Bed in lord's chamber (NE corner)
        set(level, gx + 6, gy + 8, gz - 6, RED_BED);
        // Chests (lord's treasury)
        set(level, gx - 6, gy + 8, gz - 8, CHEST);
        set(level, gx - 5, gy + 8, gz - 8, CHEST);
        // Bookshelves (study)
        fill(level, gx - 7, gy + 8, gz + 2, 3, 2, 1, BOOKSHELF);
        // Lanterns
        set(level, gx, gy + 9, gz, SOUL_LANTERN);
        set(level, gx - 5, gy + 9, gz - 3, SOUL_LANTERN);
        set(level, gx + 5, gy + 9, gz - 3, SOUL_LANTERN);
        // Roof: spruce stair pyramid
        fill(level, gx - 12, gy + 13, gz - 12, 25, 1, 24, SPRUCE_PLANK);
        fill(level, gx - 10, gy + 14, gz - 10, 21, 1, 20, SPRUCE_STAIR);
        fill(level, gx - 8, gy + 15, gz - 8, 17, 1, 16, SPRUCE_STAIR);
        fill(level, gx - 6, gy + 16, gz - 6, 13, 1, 12, SPRUCE_STAIR);
        fill(level, gx - 4, gy + 17, gz - 4, 9, 1, 8, SPRUCE_STAIR);
        fill(level, gx - 2, gy + 18, gz - 2, 5, 1, 4, SPRUCE_STAIR);
        // Ice cap at peak
        fill(level, gx - 1, gy + 19, gz - 1, 3, 1, 3, PACKED_ICE);
        set(level, gx, gy + 20, gz, BLUE_ICE);
        // Snow on roof
        fill(level, gx - 12, gy + 20, gz - 12, 25, 1, 24, SNOW_BLOCK);
    }

    private static void buildCultivationHall(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx - 25;
        int gz = cz - 15;
        int gy = baseY;
        // Floor
        fill(level, gx - 6, gy - 1, gz - 5, 13, 1, 11, SPIRIT_STONE);
        // Walls: spirit stone + packed ice corners
        fill(level, gx - 7, gy, gz - 5, 1, 5, 11, SPIRIT_STONE);
        fill(level, gx + 6, gy, gz - 5, 1, 5, 11, SPIRIT_STONE);
        fill(level, gx - 7, gy, gz - 5, 13, 5, 1, SPIRIT_STONE);
        fill(level, gx - 7, gy, gz + 5, 13, 5, 1, SPIRIT_STONE);
        // Interior
        fill(level, gx - 6, gy, gz - 4, 12, 5, 9, AIR);
        // 4 ice pillars inside
        fill(level, gx - 5, gy, gz - 3, 1, 4, 1, PACKED_ICE);
        fill(level, gx + 5, gy, gz - 3, 1, 4, 1, PACKED_ICE);
        fill(level, gx - 5, gy, gz + 3, 1, 4, 1, PACKED_ICE);
        fill(level, gx + 5, gy, gz + 3, 1, 4, 1, PACKED_ICE);
        // Blue ice cap on pillars
        set(level, gx - 5, gy + 4, gz - 3, BLUE_ICE);
        set(level, gx + 5, gy + 4, gz - 3, BLUE_ICE);
        set(level, gx - 5, gy + 4, gz + 3, BLUE_ICE);
        set(level, gx + 5, gy + 4, gz + 3, BLUE_ICE);
        // Meditation cushions (light blue carpet)
        fill(level, gx - 3, gy, gz - 2, 7, 1, 5, LIGHT_BLUE_CARPET);
        // Formation core at center (cold qi gathering)
        set(level, gx, gy + 1, gz, FORMATION_CORE);
        // Roof
        fill(level, gx - 8, gy + 5, gz - 6, 15, 1, 13, SPRUCE_STAIR);
        fill(level, gx - 7, gy + 6, gz - 5, 13, 1, 11, SPRUCE_STAIR);
        // Snow on roof
        fill(level, gx - 8, gy + 7, gz - 6, 15, 1, 13, SNOW_BLOCK);
        // Entrance (south wall center)
        fill(level, gx - 1, gy, gz + 5, 3, 3, 1, AIR);
        // Lectern with cultivation scroll
        set(level, gx + 3, gy, gz + 1, Blocks.LECTERN.defaultBlockState());
    }

    private static void buildIceForge(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx + 20;
        int gz = cz - 15;
        int gy = baseY;
        // Floor
        fill(level, gx - 5, gy - 1, gz - 4, 11, 1, 9, COBBLESTONE);
        // Walls: stone brick
        fill(level, gx - 6, gy, gz - 4, 1, 4, 9, STONE_BRICK);
        fill(level, gx + 5, gy, gz - 4, 1, 4, 9, STONE_BRICK);
        fill(level, gx - 6, gy, gz - 4, 11, 4, 1, STONE_BRICK);
        fill(level, gx - 6, gy, gz + 4, 11, 4, 1, STONE_BRICK);
        // Interior
        fill(level, gx - 5, gy, gz - 3, 10, 4, 7, AIR);
        // Blast furnace (main forge)
        fill(level, gx - 3, gy, gz - 2, 2, 2, 2, BLAST_FURNACE);
        // Anvil
        set(level, gx + 1, gy, gz - 2, ANVIL);
        // Crafting table
        set(level, gx + 3, gy, gz - 2, CRAFTING_TABLE);
        // Chest for materials
        set(level, gx + 3, gy, gz + 1, CHEST);
        // Barrel for fuel
        set(level, gx - 3, gy, gz + 1, BARREL);
        // Cauldron for quenching
        set(level, gx + 1, gy, gz + 1, CAULDRON);
        // Second furnace
        set(level, gx - 3, gy, gz + 3, FURNACE);
        // Ice block decoration (cold forge theme)
        fill(level, gx - 5, gy, gz - 3, 1, 3, 1, PACKED_ICE);
        fill(level, gx + 4, gy, gz - 3, 1, 3, 1, PACKED_ICE);
        // Lantern
        set(level, gx, gy + 3, gz, SOUL_LANTERN);
        // Roof
        fill(level, gx - 7, gy + 4, gz - 5, 13, 1, 11, SPRUCE_STAIR);
        // Snow on roof
        fill(level, gx - 7, gy + 5, gz - 5, 13, 1, 11, SNOW_BLOCK);
    }

    private static void buildGreenhouse(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx - 25;
        int gz = cz + 12;
        int gy = baseY;
        // Floor: dirt for growing
        fill(level, gx - 5, gy - 1, gz - 4, 11, 1, 9, DIRT);
        // Glass walls
        fill(level, gx - 6, gy, gz - 4, 1, 4, 9, GLASS_PANE);
        fill(level, gx + 5, gy, gz - 4, 1, 4, 9, GLASS_PANE);
        fill(level, gx - 6, gy, gz - 4, 11, 4, 1, GLASS);
        fill(level, gx - 6, gy, gz + 4, 11, 4, 1, GLASS);
        // Stone brick corner pillars (structural support)
        fill(level, gx - 6, gy, gz - 4, 1, 5, 1, STONE_BRICK);
        fill(level, gx + 5, gy, gz - 4, 1, 5, 1, STONE_BRICK);
        fill(level, gx - 6, gy, gz + 4, 1, 5, 1, STONE_BRICK);
        fill(level, gx + 5, gy, gz + 4, 1, 5, 1, STONE_BRICK);
        // Snow heart herbs and frost herbs (represented by flowers/ferns)
        set(level, gx - 3, gy, gz - 2, Blocks.BLUE_ORCHID.defaultBlockState());
        set(level, gx - 1, gy, gz - 1, Blocks.BLUE_ORCHID.defaultBlockState());
        set(level, gx + 1, gy, gz - 2, Blocks.AZALEA.defaultBlockState());
        set(level, gx + 3, gy, gz - 1, Blocks.BLUE_ORCHID.defaultBlockState());
        set(level, gx - 2, gy, gz + 1, Blocks.FERN.defaultBlockState());
        set(level, gx, gy, gz + 1, Blocks.FERN.defaultBlockState());
        set(level, gx + 2, gy, gz + 1, Blocks.FERN.defaultBlockState());
        // Water irrigation
        fill(level, gx - 4, gy, gz, 1, 1, 5, WATER);
        fill(level, gx + 4, gy, gz, 1, 1, 5, WATER);
        // Glass roof
        fill(level, gx - 6, gy + 5, gz - 4, 11, 1, 9, GLASS);
        // Spruce beam supports on roof
        fill(level, gx - 6, gy + 4, gz, 11, 1, 1, SPRUCE_PLANK);
        fill(level, gx, gy + 4, gz - 4, 1, 1, 9, SPRUCE_PLANK);
    }

    private static void buildMortalQuarter(ServerLevel level, int cx, int baseY, int cz) {
        int gy = baseY;
        // 6 houses south of central plaza
        int[][] houses = {
            {cx - 20, cz + 14}, {cx - 12, cz + 14}, {cx - 4, cz + 14},
            {cx + 4, cz + 14}, {cx + 12, cz + 14}, {cx + 20, cz + 14}
        };
        for (int[] h : houses) {
            int hx = h[0];
            int hz = h[1];
            // Floor
            fill(level, hx - 2, gy - 1, hz - 2, 5, 1, 5, COBBLESTONE);
            // Spruce walls (4 tall)
            fill(level, hx - 3, gy, hz - 3, 1, 4, 6, SPRUCE_PLANK);
            fill(level, hx + 2, gy, hz - 3, 1, 4, 6, SPRUCE_PLANK);
            fill(level, hx - 3, gy, hz - 3, 6, 4, 1, SPRUCE_PLANK);
            fill(level, hx - 3, gy, hz + 2, 6, 4, 1, SPRUCE_PLANK);
            // Interior
            fill(level, hx - 2, gy, hz - 2, 4, 4, 4, AIR);
            // Door (south wall center)
            fill(level, hx, gy, hz + 2, 1, 3, 1, AIR);
            set(level, hx, gy + 1, hz + 2, SPRUCE_DOOR);
            // Window (iron bars)
            fill(level, hx - 3, gy + 1, hz - 1, 1, 1, 1, IRON_BARS);
            fill(level, hx + 2, gy + 1, hz - 1, 1, 1, 1, IRON_BARS);
            // Bed
            set(level, hx - 1, gy, hz - 1, RED_BED);
            // Chest
            set(level, hx + 1, gy, hz - 1, CHEST);
            // Roof (spruce stairs + snow)
            fill(level, hx - 4, gy + 4, hz - 4, 7, 1, 8, SPRUCE_STAIR);
            fill(level, hx - 3, gy + 5, hz - 3, 5, 1, 6, SPRUCE_STAIR);
            fill(level, hx - 4, gy + 5, hz - 4, 7, 1, 8, SNOW_BLOCK);
            // Snow on ground near house
            fill(level, hx - 4, gy, hz - 4, 8, 1, 8, SNOW_BLOCK);
        }
    }

    private static void buildGarrison(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx + 22;
        int gz = cz + 5;
        int gy = baseY;
        // Barracks floor
        fill(level, gx - 7, gy - 1, gz - 5, 15, 1, 11, COBBLESTONE);
        // Walls: stone brick
        fill(level, gx - 8, gy, gz - 5, 1, 5, 11, STONE_BRICK);
        fill(level, gx + 7, gy, gz - 5, 1, 5, 11, STONE_BRICK);
        fill(level, gx - 8, gy, gz - 5, 15, 5, 1, STONE_BRICK);
        fill(level, gx - 8, gy, gz + 5, 15, 5, 1, STONE_BRICK);
        // Interior
        fill(level, gx - 7, gy, gz - 4, 14, 5, 9, AIR);
        // 4 bed bays (2 per side)
        for (int i = 0; i < 2; i++) {
            int bz = gz - 3 + i * 5;
            // Left bay
            set(level, gx - 6, gy, bz, RED_BED);
            set(level, gx - 4, gy, bz, RED_BED);
            set(level, gx - 2, gy, bz, RED_BED);
            // Right bay
            set(level, gx + 2, gy, bz, RED_BED);
            set(level, gx + 4, gy, bz, RED_BED);
            set(level, gx + 6, gy, bz, RED_BED);
        }
        // Weapon racks (iron bars in walls)
        fill(level, gx - 8, gy + 1, gz - 2, 1, 2, 2, IRON_BARS);
        fill(level, gx - 8, gy + 1, gz + 2, 1, 2, 2, IRON_BARS);
        // Armory chest
        set(level, gx, gy, gz, CHEST);
        // Crafting area
        set(level, gx + 5, gy, gz, ANVIL);
        set(level, gx + 5, gy, gz + 2, CRAFTING_TABLE);
        // Furnace for arrow/sword repair
        set(level, gx - 5, gy, gz, FURNACE);
        // Lanterns
        set(level, gx, gy + 4, gz - 3, SOUL_LANTERN);
        set(level, gx, gy + 4, gz + 3, SOUL_LANTERN);
        // Roof
        fill(level, gx - 9, gy + 5, gz - 6, 17, 1, 13, SPRUCE_STAIR);
        // Snow on roof
        fill(level, gx - 9, gy + 6, gz - 6, 17, 1, 13, SNOW_BLOCK);
        // Training yard in front (packed ice sparring area)
        fill(level, gx - 6, gy - 1, gz + 7, 13, 1, 8, PACKED_ICE);
        // Training dummies (spruce fence posts)
        for (int i = 0; i < 3; i++) {
            fill(level, gx - 4 + i * 4, gy, gz + 8, 1, 3, 1, SPRUCE_FENCE);
            set(level, gx - 4 + i * 4, gy + 3, gz + 8, Blocks.PUMPKIN.defaultBlockState()); // target head
        }
    }

    private static void buildIceRepository(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx + 22;
        int gz = cz - 15;
        int gy = baseY;
        // Underground vault (below ice forge, carved into packed ice)
        // Entrance: stairs down from ice forge area
        fill(level, gx, gy, gz + 3, 2, 4, 1, AIR);
        fill(level, gx, gy - 4, gz + 3, 2, 4, 1, SPRUCE_PLANK); // staircase floor
        fill(level, gx, gy - 4, gz + 4, 2, 1, 3, COBBLESTONE); // vault floor
        // Vault walls: packed ice
        fill(level, gx - 1, gy - 4, gz + 4, 1, 4, 3, PACKED_ICE);
        fill(level, gx + 2, gy - 4, gz + 4, 1, 4, 3, PACKED_ICE);
        fill(level, gx - 1, gy - 4, gz + 4, 4, 4, 1, PACKED_ICE);
        fill(level, gx - 1, gy - 4, gz + 6, 4, 4, 1, PACKED_ICE);
        // Vault interior
        fill(level, gx, gy - 4, gz + 4, 2, 4, 3, AIR);
        // Chests (treasury)
        set(level, gx, gy - 4, gz + 5, CHEST);
        set(level, gx + 1, gy - 4, gz + 5, CHEST);
        // Blue ice seal block (vault seal)
        fill(level, gx, gy, gz + 4, 2, 1, 3, BLUE_ICE); // ice blocking entrance from above
        // Soul lantern for ambiance
        set(level, gx, gy - 3, gz + 5, SOUL_LANTERN);
    }

    private static void buildWatchtowers(ServerLevel level, int cx, int baseY, int cz) {
        int r = CITY_RADIUS;
        int gy = baseY;
        // 4 corner watchtowers
        int[][] towers = {
            {cx - r + 5, cz - r + 5},
            {cx + r - 5, cz - r + 5},
            {cx - r + 5, cz + r - 5},
            {cx + r - 5, cz + r - 5}
        };
        for (int[] t : towers) {
            int tx = t[0];
            int tz = t[1];
            // 5x5 packed ice base
            fill(level, tx - 2, gy, tz - 2, 5, 14, 5, PACKED_ICE);
            // Interior hollow
            fill(level, tx - 1, gy + 1, tz - 1, 3, 12, 3, AIR);
            // Stone brick interior floor
            fill(level, tx - 1, gy + 7, tz - 1, 3, 1, 3, STONE_BRICK);
            fill(level, tx - 1, gy, tz - 1, 3, 1, 3, STONE_BRICK);
            // Ladder access (spruce fence as ladder substitute)
            fill(level, tx, gy + 1, tz - 1, 1, 13, 1, SPRUCE_FENCE);
            // Second floor: lookouts
            set(level, tx - 1, gy + 8, tz - 1, SOUL_LANTERN);
            set(level, tx + 1, gy + 8, tz - 1, SOUL_LANTERN);
            // Top beacon: blue ice + soul lantern
            fill(level, tx - 2, gy + 14, tz - 2, 5, 1, 5, BLUE_ICE);
            set(level, tx, gy + 15, tz, SOUL_LANTERN);
            // Snow on top
            fill(level, tx - 2, gy + 16, tz - 2, 5, 1, 5, SNOW_BLOCK);
        }
    }

    private static void buildSnowLayer(ServerLevel level, int cx, int baseY, int cz) {
        // Scatter snow blocks on outer terrain (beyond walls)
        for (int dx = -CITY_RADIUS - 8; dx <= CITY_RADIUS + 8; dx += 3) {
            for (int dz = -CITY_RADIUS - 8; dz <= CITY_RADIUS + 8; dz += 3) {
                // Only place snow in a ring around the city, not inside
                if (Math.abs(dx) > CITY_RADIUS - 2 || Math.abs(dz) > CITY_RADIUS - 2) {
                    if ((dx + dz) % 5 == 0) {
                        set(level, cx + dx, baseY, cz + dz, SNOW_BLOCK);
                    }
                }
            }
        }
        // Dead spruce trees around the city (stripped logs sticking out of snow)
        int[][] trees = {
            {cx - 65, cz - 40}, {cx - 60, cz + 30}, {cx + 50, cz - 55},
            {cx + 65, cz + 45}, {cx - 50, cz + 55}, {cx + 60, cz - 25}
        };
        for (int[] t : trees) {
            fill(level, t[0], baseY, t[1], 1, 6, 1, SPRUCE_LOG);
            set(level, t[0], baseY + 6, t[1], SNOW_BLOCK);
            set(level, t[0], baseY + 7, t[1], SNOW_BLOCK);
        }
    }

    // ── Helper methods ────────────────────────────────────────────────

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, 3);
    }

    private static void fill(ServerLevel level, int x, int y, int z,
                            int dx, int dy, int dz, BlockState state) {
        for (int ix = 0; ix < dx; ix++) {
            for (int iy = 0; iy < dy; iy++) {
                for (int iz = 0; iz < dz; iz++) {
                    set(level, x + ix, y + iy, z + iz, state);
                }
            }
        }
    }
}
