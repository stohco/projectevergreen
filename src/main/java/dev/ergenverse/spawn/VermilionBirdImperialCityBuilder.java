package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * VermilionBirdImperialCityBuilder — FULLY hand-built Vermilion Bird Imperial City
 * (朱雀皇城 / Zhuque Huang Cheng).
 *
 * <p>Constitution: "The world is completely hand-crafted, accurate to the novels.
 * NEVER write a script that replaces vanilla blocks with other blocks as a shortcut.
 * Every structure must be hand-authored."
 *
 * <p>Canon basis (Renegade Immortal): The Imperial City is the seat of the Vermilion Bird
 * Dynasty, the ruling power of Planet Suzaku. It is the political, military, and cultivation
 * center of the entire planet. The Vermilion Bird Divine Sect governs from here. The city
 * is GRAND beyond all others — massive walls, towering gates, sprawling palace complexes,
 * and cultivation power that dwarfs anything in Zhao Country. The city lord is a Soul
 * Transformation cultivator. The palace contains the Vermilion Bird throne room and the
 * Dynasty's most sacred relics.
 *
 * <p>Block palette: Gold block, quartz (imperial white), redstone/red wool (vermilion
 * banners), polished deepslate, nether bricks (imperial dark accents), end rods
 * ( ceremonial spires), chiseled quartz pillars, spruce wood for structural beams.
 * The city radiates WEALTH and POWER — gold trim, white walls, red accents.
 *
 * <p>Districts (13):
 * <ol>
 *   <li>Imperial Outer Wall — massive 14-block-high quartz walls with gold crenellations</li>
 *   <li>Imperial South Gate — grand triple-arch gate with gold plaque and guard battalions</li>
 *   <li>Imperial Avenue — wide quartz processional road from gate to palace</li>
 *   <li>Central Imperial Plaza — massive plaza with vermillion bird statue (gold + netherrack)</li>
 *   <li>Vermilion Bird Palace — the Dynasty seat: multi-story quartz keep with gold roof</li>
 *   <li>Divine Sect Headquarters — cultivation hall for the Vermilion Bird Divine Sect</li>
 *   <li>Imperial Armory — weapon storage and forging for the Dynasty's military</li>
 *   <li>Spirit Treasury — underground vault sealed by restriction stone</li>
 *   <li>Noble District — grand houses for cultivation aristocracy</li>
 *   <li>Merchant Quarter — elite trading district for high-tier cultivation resources</li>
 *   <li>Mortal District — the common people's quarter, humble but orderly</li>
 *   <li>Temple of the Vermilion Bird — the Dynasty's sacred temple with eternal flame</li>
 *   <li>Imperial Gardens — cultivated gardens with rare spirit herbs and ponds</li>
 * </ol>
 *
 * <h2>Harsh Self-Critique</h2>
 * <ul>
 *   <li>The Imperial City at 130x130 is the same radius as Nan Dou — canonically it
 *       should be the LARGEST city on the planet, dwarfing all others. 130 radius is
 *       still too small for a planetary capital.</li>
 *   <li>The palace uses quartz box rooms — real imperial palaces have courtyards,
 *       side halls, audience chambers, inner sanctums. This is a single keep.</li>
 *   <li>The Vermilion Bird statue is gold blocks + netherrack — the crudest possible
 *       representation. A real divine beast statue would require custom models.</li>
 *   <li>No cultivation formations visible in the streets — the capital of the most
 *       powerful dynasty on the planet should have restriction arrays, spirit vein
 *       conduits, and defensive formations visible everywhere.</li>
 *   <li>The noble district uses the same house template repeated. Aristocratic
 *       mansions should be unique with varied layouts and private gardens.</li>
 *   <li>No underground cultivation caves, no sealed ancestral halls, no hidden
 *       passages between palace and sect headquarters.</li>
 * </ul>
 */
public final class VermilionBirdImperialCityBuilder {

    private VermilionBirdImperialCityBuilder() {}

    // ── Canonical position on Planet Suzaku ────────────────────────
    public static final int CITY_X = WangFamilyVillageBuilder.VILLAGE_X + 5200;
    public static final int CITY_Z = WangFamilyVillageBuilder.VILLAGE_Z + 600;

    private static final int CITY_RADIUS = 65;
    private static final int WALL_HEIGHT = 14;

    private static boolean built = false;

    // ── Block palette — imperial gold, quartz, red ───────────────
    private static final BlockState GOLD_BLOCK           = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState QUARTZ               = Blocks.QUARTZ_BLOCK.defaultBlockState();
    private static final BlockState QUARTZ_PILLAR        = Blocks.QUARTZ_PILLAR.defaultBlockState();
    private static final BlockState QUARTZ_STAIRS        = Blocks.QUARTZ_STAIRS.defaultBlockState();
    private static final BlockState QUARTZ_SLAB           = Blocks.QUARTZ_SLAB.defaultBlockState();
    private static final BlockState QUARTZ_BRICKS        = Blocks.QUARTZ_BRICKS.defaultBlockState();
    private static final BlockState CHISELED_QUARTZ      = Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
    private static final BlockState POLISHED_DEEPSLATE    = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
    private static final BlockState DEEPSLATE_BRICKS     = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
    private static final BlockState NETHER_BRICKS         = Blocks.NETHER_BRICKS.defaultBlockState();
    private static final BlockState RED_WOOL              = Blocks.RED_WOOL.defaultBlockState();
    private static final BlockState RED_CARPET           = Blocks.RED_CARPET.defaultBlockState();
    private static final BlockState ORANGE_CARPET        = Blocks.ORANGE_CARPET.defaultBlockState();
    private static final BlockState WHITE_CARPET         = Blocks.WHITE_CARPET.defaultBlockState();
    private static final BlockState YELLOW_CARPET        = Blocks.YELLOW_CARPET.defaultBlockState();
    private static final BlockState SPRUCE_PLANK         = Blocks.SPRUCE_PLANKS.defaultBlockState();
    private static final BlockState SPRUCE_LOG           = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState SPRUCE_STAIRS        = Blocks.SPRUCE_STAIRS.defaultBlockState();
    private static final BlockState SPRUCE_SLAB          = Blocks.SPRUCE_SLAB.defaultBlockState();
    private static final BlockState DARK_OAK_PLANK       = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState DARK_OAK_LOG         = Blocks.DARK_OAK_LOG.defaultBlockState();
    private static final BlockState STONE_BRICK           = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState COBBLESTONE           = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState IRON_BARS             = Blocks.IRON_BARS.defaultBlockState();
    private static final BlockState IRON_DOOR             = Blocks.IRON_DOOR.defaultBlockState();
    private static final BlockState LANTERN               = Blocks.LANTERN.defaultBlockState();
    private static final BlockState SOUL_LANTERN          = Blocks.SOUL_LANTERN.defaultBlockState();
    private static final BlockState GLOWSTONE             = Blocks.GLOWSTONE.defaultBlockState();
    private static final BlockState END_ROD               = Blocks.END_ROD.defaultBlockState();
    private static final BlockState CHEST                 = Blocks.CHEST.defaultBlockState();
    private static final BlockState BOOKSHELF             = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState ANVIL                 = Blocks.ANVIL.defaultBlockState();
    private static final BlockState FURNACE               = Blocks.FURNACE.defaultBlockState();
    private static final BlockState BLAST_FURNACE         = Blocks.BLAST_FURNACE.defaultBlockState();
    private static final BlockState CAULDRON              = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState CRAFTING_TABLE        = Blocks.CRAFTING_TABLE.defaultBlockState();
    private static final BlockState BARREL               = Blocks.BARREL.defaultBlockState();
    private static final BlockState BREWING_STAND        = Blocks.BREWING_STAND.defaultBlockState();
    private static final BlockState GLASS_PANE           = Blocks.GLASS_PANE.defaultBlockState();
    private static final BlockState RED_BED               = Blocks.RED_BED.defaultBlockState();
    private static final BlockState WHITE_CONCRETE        = Blocks.WHITE_CONCRETE.defaultBlockState();
    private static final BlockState AIR                   = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER                 = Blocks.WATER.defaultBlockState();
    private static final BlockState DIRT                  = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRASS                 = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState NETHERRACK            = Blocks.NETHERRACK.defaultBlockState();
    private static final BlockState MAGMA_BLOCK           = Blocks.MAGMA_BLOCK.defaultBlockState();
    // Ergenverse blocks
    private static final BlockState SPIRIT_STONE           = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
    private static final BlockState SPIRIT_STONE_SLAB     = ErgenverseBlocks.SPIRIT_STONE_SLAB.get().defaultBlockState();
    private static final BlockState SPIRIT_STONE_WALL     = ErgenverseBlocks.SPIRIT_STONE_WALL.get().defaultBlockState();
    private static final BlockState SPIRIT_STONE_STAIR    = ErgenverseBlocks.SPIRIT_STONE_STAIRS.get().defaultBlockState();
    private static final BlockState SPIRIT_WOOD_PLANK      = ErgenverseBlocks.SPIRIT_WOOD_PLANKS.get().defaultBlockState();
    private static final BlockState SPIRIT_WOOD_LOG        = ErgenverseBlocks.SPIRIT_WOOD_LOG.get().defaultBlockState();
    private static final BlockState ANCIENT_SPIRIT_LOG     = ErgenverseBlocks.ANCIENT_SPIRIT_LOG.get().defaultBlockState();
    private static final BlockState FORMATION_CORE        = ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState();
    private static final BlockState RESTRICTION_STONE     = ErgenverseBlocks.RESTRICTION_STONE.get().defaultBlockState();
    private static final BlockState JADE_STONE            = ErgenverseBlocks.JADE_STONE.get().defaultBlockState();
    private static final BlockState SPIRIT_DIRT           = ErgenverseBlocks.SPIRIT_DIRT.get().defaultBlockState();
    private static final BlockState SPIRIT_GRASS          = ErgenverseBlocks.SPIRIT_GRASS.get().defaultBlockState();
    private static final BlockState SCORCHED_STONE        = ErgenverseBlocks.SCORCHED_STONE.get().defaultBlockState();

    public static boolean isAlreadyBuilt(ServerLevel level) {
        return level.getBlockState(new BlockPos(CITY_X, 80, CITY_Z)).getBlock() == Blocks.GOLD_BLOCK;
    }

    public static void build(ServerLevel level) {
        if (built) return;
        int baseY = 64;
        int cx = CITY_X;
        int cz = CITY_Z;

        Ergenverse.LOGGER.info("[VermilionBirdCity] Building Vermilion Bird Imperial City at ({}, {}, {})...", cx, baseY, cz);

        // Foundation
        buildFoundation(level, cx, baseY, cz);

        // 1. Imperial Outer Walls
        buildOuterWalls(level, cx, baseY, cz);

        // 2. Imperial South Gate
        buildSouthGate(level, cx, baseY, cz);

        // 3. Imperial Avenue
        buildImperialAvenue(level, cx, baseY, cz);

        // 4. Central Imperial Plaza
        buildImperialPlaza(level, cx, baseY, cz);

        // 5. Vermilion Bird Palace
        buildPalace(level, cx, baseY, cz);

        // 6. Divine Sect Headquarters
        buildDivineSectHQ(level, cx, baseY, cz);

        // 7. Imperial Armory
        buildArmory(level, cx, baseY, cz);

        // 8. Spirit Treasury
        buildSpiritTreasury(level, cx, baseY, cz);

        // 9. Noble District
        buildNobleDistrict(level, cx, baseY, cz);

        // 10. Merchant Quarter
        buildMerchantQuarter(level, cx, baseY, cz);

        // 11. Mortal District
        buildMortalDistrict(level, cx, baseY, cz);

        // 12. Temple of Vermilion Bird
        buildTemple(level, cx, baseY, cz);

        // 13. Imperial Gardens
        buildGardens(level, cx, baseY, cz);

        built = true;
        Ergenverse.LOGGER.info("[VermilionBirdCity] Vermilion Bird Imperial City construction complete.");
    }

    // ═══════════════════════════════════════════════════════════════
    //  District Builders
    // ═══════════════════════════════════════════════════════════════

    private static void buildFoundation(ServerLevel level, int cx, int baseY, int cz) {
        fill(level, cx - CITY_RADIUS, baseY - 3, cz - CITY_RADIUS,
                CITY_RADIUS * 2, 3, CITY_RADIUS * 2, Blocks.STONE.defaultBlockState());
        fill(level, cx - CITY_RADIUS, baseY - 1, cz - CITY_RADIUS,
                CITY_RADIUS * 2, 1, CITY_RADIUS * 2, WHITE_CONCRETE);
    }

    private static void buildOuterWalls(ServerLevel level, int cx, int baseY, int cz) {
        int r = CITY_RADIUS;
        // Quartz walls
        fill(level, cx - r, baseY, cz - r, r * 2, WALL_HEIGHT, 3, QUARTZ_BRICKS);
        fill(level, cx - r, baseY, cz + r - 3, r * 2, WALL_HEIGHT, 3, QUARTZ_BRICKS);
        fill(level, cx - r, baseY, cz - r, 3, WALL_HEIGHT, r * 2, QUARTZ_BRICKS);
        fill(level, cx + r - 3, baseY, cz - r, 3, WALL_HEIGHT, r * 2, QUARTZ_BRICKS);
        // Gold crenellations (every 3 blocks)
        for (int i = -r; i < r; i += 3) {
            set(level, cx + i, baseY + WALL_HEIGHT, cz - r, GOLD_BLOCK);
            set(level, cx + i, baseY + WALL_HEIGHT, cz + r - 1, GOLD_BLOCK);
            set(level, cx - r + 1, baseY + WALL_HEIGHT, cz + i, GOLD_BLOCK);
            set(level, cx + r - 2, baseY + WALL_HEIGHT, cz + i, GOLD_BLOCK);
        }
        // Gold cap stones
        fill(level, cx - r, baseY + WALL_HEIGHT + 1, cz - r, r * 2, 1, 3, GOLD_BLOCK);
        fill(level, cx - r, baseY + WALL_HEIGHT + 1, cz + r - 3, r * 2, 1, 3, GOLD_BLOCK);
        fill(level, cx - r, baseY + WALL_HEIGHT + 1, cz - r, 3, 1, r * 2, GOLD_BLOCK);
        fill(level, cx + r - 3, baseY + WALL_HEIGHT + 1, cz - r, 3, 1, r * 2, GOLD_BLOCK);
        // Clear interior
        fill(level, cx - r + 3, baseY, cz - r + 3, r * 2 - 6, 1, r * 2 - 6, AIR);
        // End rod spires on corner towers
        int[][] corners = {{-r+1, -r+1}, {r-2, -r+1}, {-r+1, r-2}, {r-2, r-2}};
        for (int[] c : corners) {
            fill(level, cx + c[0], baseY + WALL_HEIGHT + 1, cz + c[1], 2, 4, 2, QUARTZ);
            set(level, cx + c[0], baseY + WALL_HEIGHT + 5, cz + c[1], END_ROD);
        }
    }

    private static void buildSouthGate(ServerLevel level, int cx, int baseY, int cz) {
        int gz = cz + CITY_RADIUS - 3;
        int gy = baseY;
        // Triple arch gate
        // Center arch (wider)
        fill(level, cx - 2, gy, gz, 5, WALL_HEIGHT, 3, QUARTZ_PILLAR);
        // Left arch
        fill(level, cx - 10, gy, gz, 4, WALL_HEIGHT - 2, 3, QUARTZ_PILLAR);
        // Right arch
        fill(level, cx + 7, gy, gz, 4, WALL_HEIGHT - 2, 3, QUARTZ_PILLAR);
        // Gold arch tops
        fill(level, cx - 11, gy + WALL_HEIGHT - 2, gz - 1, 6, 2, 5, GOLD_BLOCK);
        fill(level, cx - 3, gy + WALL_HEIGHT, gz - 1, 7, 2, 5, GOLD_BLOCK);
        fill(level, cx + 7, gy + WALL_HEIGHT - 2, gz - 1, 6, 2, 5, GOLD_BLOCK);
        // Clear gate openings
        fill(level, cx - 9, gy, gz, 2, WALL_HEIGHT - 3, 3, AIR);
        fill(level, cx - 1, gy, gz, 3, WALL_HEIGHT - 1, 3, AIR);
        fill(level, cx + 8, gy, gz, 2, WALL_HEIGHT - 3, 3, AIR);
        // Iron bar portcullis (center gate)
        fill(level, cx - 1, gy + 2, gz, 3, WALL_HEIGHT - 4, 1, IRON_BARS);
        // Red carpet through center gate
        fill(level, cx - 1, gy, gz, 3, 1, 3, RED_CARPET);
        // Guard battalions: two guardhouses flanking the gate
        fill(level, cx - 16, gy, gz - 3, 5, 4, 7, DEEPSLATE_BRICKS);
        fill(level, cx + 12, gy, gz - 3, 5, 4, 7, DEEPSLATE_BRICKS);
        // Guardhouse roofs (nether brick)
        fill(level, cx - 17, gy + 4, gz - 4, 7, 1, 9, NETHER_BRICKS);
        fill(level, cx + 11, gy + 4, gz - 4, 7, 1, 9, NETHER_BRICKS);
        // Gold plaque above center gate
        fill(level, cx - 2, gy + WALL_HEIGHT - 1, gz - 1, 5, 1, 1, GOLD_BLOCK);
    }

    private static void buildImperialAvenue(ServerLevel level, int cx, int baseY, int cz) {
        int gy = baseY;
        // 8-block-wide quartz processional road, N-S
        fill(level, cx - 4, gy, cz - CITY_RADIUS + 8, 9, 1, CITY_RADIUS * 2 - 16, QUARTZ);
        // E-W cross road
        fill(level, cx - CITY_RADIUS + 8, gy, cz - 4, CITY_RADIUS * 2 - 16, 1, 9, QUARTZ);
        // Red carpet center strip
        fill(level, cx - 1, gy, cz - CITY_RADIUS + 8, 3, 1, CITY_RADIUS * 2 - 16, RED_CARPET);
        // Quartz pillar lanterns every 10 blocks
        for (int i = -CITY_RADIUS + 12; i < CITY_RADIUS - 12; i += 10) {
            // N-S avenue pillars
            fill(level, cx - 5, gy, cz + i, 1, 4, 1, QUARTZ_PILLAR);
            fill(level, cx + 5, gy, cz + i, 1, 4, 1, QUARTZ_PILLAR);
            set(level, cx - 5, gy + 4, cz + i, GLOWSTONE);
            set(level, cx + 5, gy + 4, cz + i, GLOWSTONE);
            // E-W avenue pillars
            fill(level, cx + i, gy, cz - 5, 1, 4, 1, QUARTZ_PILLAR);
            fill(level, cx + i, gy, cz + 5, 1, 4, 1, QUARTZ_PILLAR);
            set(level, cx + i, gy + 4, cz - 5, GLOWSTONE);
            set(level, cx + i, gy + 4, cz + 5, GLOWSTONE);
        }
    }

    private static void buildImperialPlaza(ServerLevel level, int cx, int baseY, int cz) {
        int gy = baseY;
        // 20x20 chiseled quartz plaza
        fill(level, cx - 10, gy - 1, cz - 10, 21, 1, 21, CHISELED_QUARTZ);
        // Vermilion Bird statue at center (gold body, netherrack fire base)
        fill(level, cx - 1, gy, cz - 1, 3, 1, 3, GOLD_BLOCK);
        fill(level, cx, gy + 1, cz, 1, 3, 1, GOLD_BLOCK);
        // Wings (gold extending left and right)
        fill(level, cx - 3, gy + 2, cz, 2, 1, 1, GOLD_BLOCK);
        fill(level, cx + 2, gy + 2, cz, 2, 1, 1, GOLD_BLOCK);
        // Head
        set(level, cx, gy + 4, cz, GOLD_BLOCK);
        // Netherrack fire base
        fill(level, cx - 2, gy - 1, cz - 2, 5, 1, 5, NETHERRACK);
        // Eternal flame (magma block surrounded by gold)
        fill(level, cx - 1, gy, cz - 1, 3, 1, 3, MAGMA_BLOCK);
        // 4 jade corner markers
        set(level, cx - 9, gy, cz - 9, JADE_STONE);
        set(level, cx + 9, gy, cz - 9, JADE_STONE);
        set(level, cx - 9, gy, cz + 9, JADE_STONE);
        set(level, cx + 9, gy, cz + 9, JADE_STONE);
    }

    private static void buildPalace(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx;
        int gz = cz - 25;
        int gy = baseY;
        // Grand foundation: quartz
        fill(level, gx - 14, gy - 1, gz - 10, 29, 1, 20, QUARTZ);
        // Outer walls: quartz bricks, 8 tall
        fill(level, gx - 14, gy, gz - 10, 1, 8, 20, QUARTZ_BRICKS);
        fill(level, gx + 14, gy, gz - 10, 1, 8, 20, QUARTZ_BRICKS);
        fill(level, gx - 14, gy, gz - 10, 29, 8, 1, QUARTZ_BRICKS);
        fill(level, gx - 14, gy, gz + 9, 29, 8, 1, QUARTZ_BRICKS);
        // Clear interior
        fill(level, gx - 13, gy, gz - 9, 27, 8, 18, AIR);
        // Grand entrance (south, center)
        fill(level, gx - 2, gy, gz + 9, 5, 5, 1, AIR);
        // Gold door frame
        fill(level, gx - 3, gy + 4, gz + 9, 7, 1, 1, GOLD_BLOCK);
        // Throne room (first floor)
        fill(level, gx - 12, gy, gz - 8, 25, 1, 16, RED_CARPET);
        // Throne platform (north)
        fill(level, gx - 5, gy + 1, gz - 8, 11, 1, 4, QUARTZ);
        fill(level, gx - 4, gy + 2, gz - 8, 9, 1, 3, QUARTZ);
        // Throne: gold block seat with quartz back
        set(level, gx, gy + 3, gz - 8, GOLD_BLOCK);
        fill(level, gx - 1, gy + 3, gz - 9, 3, 2, 1, QUARTZ_PILLAR);
        // Inner pillars (8 per side, quartz pillar)
        for (int i = 0; i < 8; i++) {
            int pz = gz - 4 + i * 2;
            fill(level, gx - 12, gy, pz, 1, 7, 1, QUARTZ_PILLAR);
            fill(level, gx + 12, gy, pz, 1, 7, 1, QUARTZ_PILLAR);
            set(level, gx - 12, gy + 7, pz, GOLD_BLOCK);
            set(level, gx + 12, gy + 7, pz, GOLD_BLOCK);
        }
        // Second floor: imperial residence
        fill(level, gx - 14, gy + 8, gz - 10, 29, 1, 20, DARK_OAK_PLANK);
        fill(level, gx - 13, gy + 9, gz - 10, 1, 5, 18, QUARTZ_BRICKS);
        fill(level, gx + 13, gy + 9, gz - 10, 1, 5, 18, QUARTZ_BRICKS);
        fill(level, gx - 13, gy + 9, gz - 10, 27, 5, 1, QUARTZ_BRICKS);
        fill(level, gx - 13, gy + 9, gz + 7, 27, 5, 1, QUARTZ_BRICKS);
        fill(level, gx - 13, gy + 9, gz - 9, 27, 1, 16, AIR);
        // White carpet on second floor
        fill(level, gx - 12, gy + 9, gz - 8, 25, 1, 14, WHITE_CARPET);
        // Emperor's chambers (NE)
        set(level, gx + 10, gy + 9, gz - 6, RED_BED);
        set(level, gx + 8, gy + 9, gz - 8, CHEST);
        set(level, gx + 9, gy + 9, gz - 8, CHEST);
        // Study (NW)
        fill(level, gx - 11, gy + 9, gz - 7, 3, 2, 1, BOOKSHELF);
        set(level, gx - 11, gy + 9, gz - 5, Blocks.LECTERN.defaultBlockState());
        // Soul lanterns
        set(level, gx, gy + 10, gz, SOUL_LANTERN);
        set(level, gx - 8, gy + 10, gz - 5, SOUL_LANTERN);
        set(level, gx + 8, gy + 10, gz - 5, SOUL_LANTERN);
        // Grand roof: gold pyramid
        fill(level, gx - 16, gy + 14, gz - 12, 33, 1, 24, QUARTZ);
        fill(level, gx - 14, gy + 15, gz - 10, 29, 1, 20, GOLD_BLOCK);
        fill(level, gx - 12, gy + 16, gz - 8, 25, 1, 16, GOLD_BLOCK);
        fill(level, gx - 10, gy + 17, gz - 6, 21, 1, 12, GOLD_BLOCK);
        fill(level, gx - 8, gy + 18, gz - 4, 17, 1, 8, GOLD_BLOCK);
        fill(level, gx - 6, gy + 19, gz - 2, 13, 1, 4, GOLD_BLOCK);
        fill(level, gx - 4, gy + 20, gz, 9, 1, 1, GOLD_BLOCK);
        // End rod spire at peak
        set(level, gx, gy + 21, gz, END_ROD);
        set(level, gx, gy + 22, gz, END_ROD);
    }

    private static void buildDivineSectHQ(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx - 28;
        int gz = cz - 18;
        int gy = baseY;
        // Floor
        fill(level, gx - 8, gy - 1, gz - 6, 17, 1, 13, SPIRIT_STONE);
        // Walls: spirit stone
        fill(level, gx - 9, gy, gz - 6, 1, 6, 13, SPIRIT_STONE);
        fill(level, gx + 8, gy, gz - 6, 1, 6, 13, SPIRIT_STONE);
        fill(level, gx - 9, gy, gz - 6, 17, 6, 1, SPIRIT_STONE);
        fill(level, gx - 9, gy, gz + 6, 17, 6, 1, SPIRIT_STONE);
        // Interior
        fill(level, gx - 8, gy, gz - 5, 16, 6, 11, AIR);
        // Formation pillars (4)
        fill(level, gx - 7, gy, gz - 4, 1, 5, 1, FORMATION_CORE);
        fill(level, gx + 7, gy, gz - 4, 1, 5, 1, FORMATION_CORE);
        fill(level, gx - 7, gy, gz + 4, 1, 5, 1, FORMATION_CORE);
        fill(level, gx + 7, gy, gz + 4, 1, 5, 1, FORMATION_CORE);
        // Red carpet
        fill(level, gx - 6, gy, gz - 3, 13, 1, 7, RED_CARPET);
        // Lecterns (cultivation teaching)
        set(level, gx - 4, gy, gz, Blocks.LECTERN.defaultBlockState());
        set(level, gx + 4, gy, gz, Blocks.LECTERN.defaultBlockState());
        // Bookshelves
        fill(level, gx - 8, gy, gz - 2, 1, 4, 4, BOOKSHELF);
        fill(level, gx + 7, gy, gz - 2, 1, 4, 4, BOOKSHELF);
        // Chests (sect scriptures)
        set(level, gx - 6, gy, gz + 3, CHEST);
        set(level, gx + 5, gy, gz + 3, CHEST);
        // Soul lanterns
        set(level, gx, gy + 5, gz - 3, SOUL_LANTERN);
        set(level, gx, gy + 5, gz + 3, SOUL_LANTERN);
        // Roof
        fill(level, gx - 10, gy + 6, gz - 7, 19, 1, 15, QUARTZ_STAIRS);
        fill(level, gx - 9, gy + 7, gz - 6, 17, 1, 13, QUARTZ_STAIRS);
        // Gold trim on roof
        fill(level, gx - 10, gy + 7, gz - 7, 19, 1, 1, GOLD_BLOCK);
        fill(level, gx - 10, gy + 7, gz + 7, 19, 1, 1, GOLD_BLOCK);
    }

    private static void buildArmory(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx + 25;
        int gz = cz - 18;
        int gy = baseY;
        // Floor
        fill(level, gx - 6, gy - 1, gz - 5, 13, 1, 11, POLISHED_DEEPSLATE);
        // Walls: deepslate bricks
        fill(level, gx - 7, gy, gz - 5, 1, 5, 11, DEEPSLATE_BRICKS);
        fill(level, gx + 6, gy, gz - 5, 1, 5, 11, DEEPSLATE_BRICKS);
        fill(level, gx - 7, gy, gz - 5, 13, 5, 1, DEEPSLATE_BRICKS);
        fill(level, gx - 7, gy, gz + 5, 13, 5, 1, DEEPSLATE_BRICKS);
        // Interior
        fill(level, gx - 6, gy, gz - 4, 12, 5, 9, AIR);
        // Blast furnaces (3)
        fill(level, gx - 4, gy, gz - 3, 2, 2, 2, BLAST_FURNACE);
        set(level, gx, gy, gz - 3, BLAST_FURNACE);
        // Anvils (2)
        set(level, gx + 3, gy, gz - 3, ANVIL);
        set(level, gx + 3, gy, gz - 1, ANVIL);
        // Crafting tables
        set(level, gx - 4, gy, gz + 1, CRAFTING_TABLE);
        set(level, gx + 4, gy, gz + 1, CRAFTING_TABLE);
        // Chests for weapons
        set(level, gx - 4, gy, gz + 3, CHEST);
        set(level, gx + 4, gy, gz + 3, CHEST);
        // Cauldron
        set(level, gx, gy, gz + 2, CAULDRON);
        // Weapon rack (iron bars in walls)
        fill(level, gx - 7, gy + 1, gz - 2, 1, 2, 3, IRON_BARS);
        fill(level, gx + 6, gy + 1, gz - 2, 1, 2, 3, IRON_BARS);
        // Lantern
        set(level, gx, gy + 4, gz, GLOWSTONE);
        // Roof
        fill(level, gx - 8, gy + 5, gz - 6, 15, 1, 13, NETHER_BRICKS);
        fill(level, gx - 7, gy + 6, gz - 5, 13, 1, 11, NETHER_BRICKS);
    }

    private static void buildSpiritTreasury(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx + 25;
        int gz = cz - 18;
        int gy = baseY;
        // Underground vault beneath armory
        fill(level, gx - 4, gy - 5, gz - 3, 9, 1, 7, RESTRICTION_STONE);
        // Vault walls
        fill(level, gx - 5, gy - 5, gz - 3, 1, 5, 7, RESTRICTION_STONE);
        fill(level, gx + 4, gy - 5, gz - 3, 1, 5, 7, RESTRICTION_STONE);
        fill(level, gx - 5, gy - 5, gz - 3, 9, 5, 1, RESTRICTION_STONE);
        fill(level, gx - 5, gy - 5, gz + 3, 9, 5, 1, RESTRICTION_STONE);
        // Interior
        fill(level, gx - 4, gy - 5, gz - 2, 8, 5, 5, AIR);
        // Treasury chests (gold trim — chest row)
        set(level, gx - 3, gy - 5, gz - 1, CHEST);
        set(level, gx - 1, gy - 5, gz - 1, CHEST);
        set(level, gx + 1, gy - 5, gz - 1, CHEST);
        set(level, gx + 3, gy - 5, gz - 1, CHEST);
        // Formation core lock
        set(level, gx, gy - 4, gz, FORMATION_CORE);
        // Entrance seal: restriction stone blocking access from above
        fill(level, gx - 1, gy - 1, gz - 1, 3, 1, 3, RESTRICTION_STONE);
        // Soul lantern
        set(level, gx, gy - 3, gz + 1, SOUL_LANTERN);
    }

    private static void buildNobleDistrict(ServerLevel level, int cx, int baseY, int cz) {
        int gy = baseY;
        // 4 noble mansions (NW quadrant)
        int[][] mansions = {
            {cx - 30, cz - 30}, {cx - 18, cz - 30},
            {cx - 30, cz - 42}, {cx - 18, cz - 42}
        };
        for (int[] m : mansions) {
            int mx = m[0];
            int mz = m[1];
            // Floor: quartz
            fill(level, mx - 4, gy - 1, mz - 4, 9, 1, 9, QUARTZ);
            // Walls: quartz bricks, 5 tall
            fill(level, mx - 5, gy, mz - 5, 1, 5, 10, QUARTZ_BRICKS);
            fill(level, mx + 4, gy, mz - 5, 1, 5, 10, QUARTZ_BRICKS);
            fill(level, mx - 5, gy, mz - 5, 10, 5, 1, QUARTZ_BRICKS);
            fill(level, mx - 5, gy, mz + 4, 10, 5, 1, QUARTZ_BRICKS);
            // Interior
            fill(level, mx - 4, gy, mz - 4, 8, 5, 8, AIR);
            // Orange carpet
            fill(level, mx - 3, gy, mz - 3, 7, 1, 6, ORANGE_CARPET);
            // Door
            fill(level, mx - 1, gy, mz + 4, 3, 4, 1, AIR);
            // Windows
            fill(level, mx - 5, gy + 1, mz - 1, 1, 1, 1, GLASS_PANE);
            fill(level, mx + 4, gy + 1, mz - 1, 1, 1, 1, GLASS_PANE);
            // Bed
            set(level, mx + 2, gy, mz - 2, RED_BED);
            // Bookshelf
            fill(level, mx - 3, gy, mz - 3, 2, 3, 1, BOOKSHELF);
            // Chest
            set(level, mx + 2, gy, mz - 3, CHEST);
            // Lantern
            set(level, mx, gy + 4, mz, GLOWSTONE);
            // Roof
            fill(level, mx - 6, gy + 5, mz - 6, 13, 1, 13, QUARTZ_STAIRS);
            fill(level, mx - 5, gy + 6, mz - 5, 11, 1, 11, QUARTZ_STAIRS);
        }
    }

    private static void buildMerchantQuarter(ServerLevel level, int cx, int baseY, int cz) {
        int gy = baseY;
        // Merchant stalls along the E-W road, south side
        for (int i = 0; i < 6; i++) {
            int sx = cx - 25 + i * 10;
            int sz = cz + 14;
            // Stall floor
            fill(level, sx - 2, gy - 1, sz, 5, 1, 4, COBBLESTONE);
            // Counter
            fill(level, sx - 1, gy, sz, 3, 1, 1, SPRUCE_PLANK);
            // Back wall
            fill(level, sx - 2, gy, sz + 3, 5, 3, 1, SPRUCE_PLANK);
            // Awning
            fill(level, sx - 3, gy + 3, sz - 1, 7, 1, 5, RED_WOOL);
            // Chest (merchant wares)
            set(level, sx, gy + 1, sz + 1, CHEST);
            set(level, sx + 1, gy + 1, sz + 1, BARREL);
        }
    }

    private static void buildMortalDistrict(ServerLevel level, int cx, int baseY, int cz) {
        int gy = baseY;
        // 8 humble houses (SE quadrant)
        int[][] houses = {
            {cx + 15, cz + 20}, {cx + 25, cz + 20},
            {cx + 35, cz + 20}, {cx + 45, cz + 20},
            {cx + 15, cz + 30}, {cx + 25, cz + 30},
            {cx + 35, cz + 30}, {cx + 45, cz + 30}
        };
        for (int[] h : houses) {
            int hx = h[0];
            int hz = h[1];
            // Floor
            fill(level, hx - 2, gy - 1, hz - 2, 5, 1, 5, COBBLESTONE);
            // Walls: spruce
            fill(level, hx - 3, gy, hz - 3, 1, 4, 6, SPRUCE_PLANK);
            fill(level, hx + 2, gy, hz - 3, 1, 4, 6, SPRUCE_PLANK);
            fill(level, hx - 3, gy, hz - 3, 6, 4, 1, SPRUCE_PLANK);
            fill(level, hx - 3, gy, hz + 2, 6, 4, 1, SPRUCE_PLANK);
            // Interior
            fill(level, hx - 2, gy, hz - 2, 4, 4, 4, AIR);
            // Bed
            set(level, hx - 1, gy, hz + 1, RED_BED);
            // Chest
            set(level, hx + 1, gy, hz + 1, CHEST);
            // Roof
            fill(level, hx - 4, gy + 4, hz - 4, 7, 1, 8, SPRUCE_STAIRS);
        }
    }

    private static void buildTemple(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx - 28;
        int gz = cz + 8;
        int gy = baseY;
        // Temple floor
        fill(level, gx - 7, gy - 1, gz - 6, 15, 1, 13, QUARTZ);
        // Walls: quartz bricks
        fill(level, gx - 8, gy, gz - 6, 1, 7, 13, QUARTZ_BRICKS);
        fill(level, gx + 7, gy, gz - 6, 1, 7, 13, QUARTZ_BRICKS);
        fill(level, gx - 8, gy, gz - 6, 15, 7, 1, QUARTZ_BRICKS);
        fill(level, gx - 8, gy, gz + 6, 15, 7, 1, QUARTZ_BRICKS);
        // Interior
        fill(level, gx - 7, gy, gz - 5, 14, 7, 11, AIR);
        // Altar (north)
        fill(level, gx - 3, gy + 1, gz - 5, 7, 1, 3, GOLD_BLOCK);
        // Eternal flame (netherrack + fire)
        fill(level, gx - 2, gy + 2, gz - 4, 5, 1, 1, NETHERRACK);
        set(level, gx, gy + 3, gz - 4, MAGMA_BLOCK);
        // Red carpet approach
        fill(level, gx - 1, gy, gz - 2, 3, 1, 7, RED_CARPET);
        // Pillars (6 per side)
        for (int i = 0; i < 6; i++) {
            int pz = gz - 4 + i * 2;
            fill(level, gx - 7, gy, pz, 1, 6, 1, QUARTZ_PILLAR);
            fill(level, gx + 6, gy, pz, 1, 6, 1, QUARTZ_PILLAR);
            set(level, gx - 7, gy + 6, pz, GOLD_BLOCK);
            set(level, gx + 6, gy + 6, pz, GOLD_BLOCK);
        }
        // Lanterns
        set(level, gx - 5, gy + 5, gz, SOUL_LANTERN);
        set(level, gx + 5, gy + 5, gz, SOUL_LANTERN);
        // Roof
        fill(level, gx - 9, gy + 7, gz - 7, 17, 1, 15, QUARTZ_STAIRS);
        fill(level, gx - 8, gy + 8, gz - 6, 15, 1, 13, QUARTZ_STAIRS);
        fill(level, gx - 7, gy + 9, gz - 5, 13, 1, 11, GOLD_BLOCK);
        // End rod spire
        set(level, gx, gy + 10, gz, END_ROD);
    }

    private static void buildGardens(ServerLevel level, int cx, int baseY, int cz) {
        int gx = cx + 20;
        int gz = cz + 8;
        int gy = baseY;
        // Garden floor: grass
        fill(level, gx - 10, gy - 1, gz - 6, 21, 1, 13, GRASS);
        // Spirit herb beds (dirt strips)
        fill(level, gx - 8, gy, gz - 4, 4, 1, 3, DIRT);
        fill(level, gx - 8, gy, gz + 1, 4, 1, 3, DIRT);
        fill(level, gx + 2, gy, gz - 4, 4, 1, 3, DIRT);
        fill(level, gx + 2, gy, gz + 1, 4, 1, 3, DIRT);
        // Spirit herbs
        set(level, gx - 7, gy, gz - 3, Blocks.ALLIUM.defaultBlockState());
        set(level, gx - 5, gy, gz - 2, Blocks.AZALEA.defaultBlockState());
        set(level, gx + 3, gy, gz - 3, Blocks.BLUE_ORCHID.defaultBlockState());
        set(level, gx + 5, gy, gz - 2, Blocks.FERN.defaultBlockState());
        set(level, gx - 7, gy, gz + 2, Blocks.ROSE_BUSH.defaultBlockState());
        set(level, gx + 3, gy, gz + 2, Blocks.LILY_OF_THE_VALLEY.defaultBlockState());
        // Water pond (center)
        fill(level, gx - 2, gy, gz - 1, 5, 1, 5, WATER);
        fill(level, gx - 3, gy, gz - 2, 7, 1, 1, QUARTZ);
        fill(level, gx - 3, gy, gz + 3, 7, 1, 1, QUARTZ);
        fill(level, gx - 3, gy, gz - 2, 1, 1, 6, QUARTZ);
        fill(level, gx + 4, gy, gz - 2, 1, 1, 6, QUARTZ);
        // Spruce trees in corners
        int[][] trees = {{gx - 9, gz - 5}, {gx + 8, gz - 5}, {gx - 9, gz + 5}, {gx + 8, gz + 5}};
        for (int[] t : trees) {
            fill(level, t[0], gy, t[1], 1, 5, 1, SPRUCE_LOG);
            set(level, t[0], gy + 5, t[1], GLOWSTONE);
        }
        // Stone path through garden
        fill(level, gx - 1, gy, gz - 6, 3, 1, 1, STONE_BRICK);
        // Lectern
        set(level, gx + 7, gy, gz - 5, Blocks.LECTERN.defaultBlockState());
        // Cauldron
        set(level, gx - 9, gy, gz - 3, CAULDRON);
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
