package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * WangFamilyVillageBuilder — a FULLY hand-built Wang Family Village (王氏村),
 * Wang Lin's birthplace in Zhao Country. Every block placed intentionally.
 *
 * <p><b>Constitution:</b> "The world is completely hand-built. NEVER write a script
 * that replaces vanilla blocks with other blocks. Every structure must be
 * hand-authored." Per Art XXI — "The World Is The Main Character." Per
 * Art XXIII — "A finished region is worth more than ten partially implemented
 * systems." Per Art XXVII — "Completion Is Proven By Life."
 *
 * <p><b>Canonical location:</b> ALWAYS at (3842, surface, -1184). Every world,
 * every seed, every player. The village exists before any player joins.
 *
 * <p><b>Canon (Renegade Immortal, Ch.1-10):</b>
 * Wang Lin's village is a small, poor mortal farming village in Zhao Country.
 * His family is ordinary — father Wang Tian (deceased), mother, younger
 * brother. The village elder is respected. Spirit herbs grow in hidden
 * patches near the village. A small spirit vein slumbers beneath the village
 * center (Wang Lin later discovers it during his cultivation journey).
 *
 * <h2>Village Layout (82x82 footprint, radius 41)</h2>
 * <pre>
 *   N (toward Heng Yue Sect, ~1600 blocks)
 *   ┌──────────────────────────────────────────────┐
 *   │  Forest edge  │  North farm plots  │  Forest │
 *   │               │                    │         │
 *   │  Wang home    │  Spirit well       │  Elder  │
 *   │  (distinct)   │  (village center)  │  home   │
 *   │               │                    │  (nicer) │
 *   │  Commoner     │  Central plaza     │  Commoner│
 *   │  homes x6     │  + formation array │  homes x4│
 *   │               │                    │         │
 *   │  Herb garden  │  South path →exit  │  Storage │
 *   │  (hidden)     │                    │  sheds  │
 *   │               │  South farm plots  │         │
 *   │  Forest edge  │                    │  Forest │
 *   └──────────────────────────────────────────────┘
 *   S (player approach direction)
 * </pre>
 *
 * <h2>Districts (14 total):</h2>
 * <ol>
 *   <li>Central plaza — spirit stone + spirit vein centerpiece + 4 formation
 *       cores + village well (spirit vein stone column)</li>
 *   <li>Wang family home — NW quadrant, modest 7x5 house with alchemy
 *       furnace (Wang Lin's father kept one), identifiable by stone path</li>
 *   <li>Village elder's home — NE quadrant, slightly larger 7x7 house with
 *       formation flag base (elder's status symbol), jade stone steps</li>
 *   <li>Six commoner homes — scattered around, 5x5 each, spirit wood
 *       construction, each with a small herb pot outside</li>
 *   <li>Four additional commoner homes — E side, same construction</li>
 *   <li>North farm plots — 3 rectangular fields of spirit grass with
 *       qi-gathering grass rows (the village grows spirit herbs as
 *       mortals grow rice — they don't know what they are)</li>
 *   <li>South farm plots — 2 more fields</li>
 *   <li>Herb garden (hidden) — behind Wang family home, enclosed by spirit
 *       wood fence, rare herbs (five-color ginseng, nine-leaf clover)</li>
 *   <li>Storage sheds — 2 small 3x3 structures behind the village</li>
 *   <li>Village well — at the center, spirit vein stone pillar 3 blocks tall</li>
 *   <li>Main road — spirit sand path N-S through center, E-W crossroad</li>
 *   <li>Perimeter fence — spirit wood log fence, gaps at N and S entries</li>
 *   <li>Spirit trees — 12 trees around perimeter and inside village</li>
 *   <li>Light markers — spirit vein stones along paths (replaces torches)</li>
 * </ol>
 *
 * <h2>Harsh Self-Critique</h2>
 * <ul>
 *   <li>Houses are still box-shaped — no xianxia curved roofs, no dougong
 *       brackets. This is a mortal village, so flat roofs are MORE canonically
 *       correct than pagoda eaves. Mortal villages don't have upturned eaves.</li>
 *   <li>No interior furniture — tables, chairs, beds are not placed.
 *       The Alchemy Furnace in Wang's home is the only interior content.
 *       Future: add crafting tables (as mortals' woodworking), beds, chests.</li>
 *   <li>Farms are flat spirit-grass rectangles — no crop rows, no irrigation
 *       channels. Real mortal farms have tilled earth rows and water channels.</li>
 *   <li>No NPCs spawn here yet — the buildings exist but nobody lives in them.
 *       The NpcSpawnRegistry needs wang_family_village entries.</li>
 *   <li>The "hidden herb garden" is visible from above — not truly hidden.
 *       Future: place it behind a terrain feature or inside a hollow.</li>
 *   <li>All houses use the same spirit wood palette — no material variation
 *       between rich (elder) and poor (commoner) homes. The elder's home
 *       is larger and has jade steps, but the wall material is the same.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public final class WangFamilyVillageBuilder {

    /**
     * Lazy-initialized BlockState holder. ErgenverseBlocks.X.get() throws NPE before
     * Forge resolves the block registry, so these cannot be static-final in the outer
     * class. This inner class loads on first reference (during build(), which runs at
     * world-gen time — well after registry resolution).
     */
    private static final class B {
        private static final BlockState SPIRIT_GRASS = ErgenverseBlocks.SPIRIT_GRASS.get().defaultBlockState();
        private static final BlockState SPIRIT_DIRT = ErgenverseBlocks.SPIRIT_DIRT.get().defaultBlockState();
        private static final BlockState SPIRIT_STONE = ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
        private static final BlockState SPIRIT_SAND = ErgenverseBlocks.SPIRIT_SAND.get().defaultBlockState();
        private static final BlockState JADE_STONE = ErgenverseBlocks.JADE_STONE.get().defaultBlockState();
        private static final BlockState FORMATION_CORE = ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState();
        private static final BlockState SPIRIT_VEIN = ErgenverseBlocks.SPIRIT_VEIN_STONE.get().defaultBlockState();
        private static final BlockState PLANKS = ErgenverseBlocks.SPIRIT_WOOD_PLANKS.get().defaultBlockState();
        private static final BlockState LOG = ErgenverseBlocks.SPIRIT_WOOD_LOG.get().defaultBlockState();
        private static final BlockState LEAVES = ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get().defaultBlockState();
        private static final BlockState QI_GRASS = ErgenverseBlocks.QI_GATHERING_GRASS.get().defaultBlockState();
        private static final BlockState SNOW_HERB = ErgenverseBlocks.SNOW_HEART_HERB.get().defaultBlockState();
        private static final BlockState FIVE_GINSENG = ErgenverseBlocks.FIVE_COLOR_GINSENG.get().defaultBlockState();
        private static final BlockState NINE_CLOVER = ErgenverseBlocks.NINE_LEAF_CLOVER.get().defaultBlockState();
        private static final BlockState SOUL_LOTUS = ErgenverseBlocks.SOUL_NOURISHING_LOTUS.get().defaultBlockState();
        private static final BlockState FIRE_LOTUS = ErgenverseBlocks.FIRE_BLOOM_LOTUS.get().defaultBlockState();
        private static final BlockState VERMILION_GINSENG = ErgenverseBlocks.VERMILION_BLOOD_GINSENG.get().defaultBlockState();
        private static final BlockState SWORD_MOSS = ErgenverseBlocks.SWORD_EDGE_MOSS.get().defaultBlockState();
        private static final BlockState DAO_VINE = ErgenverseBlocks.DAO_TRACE_VINE.get().defaultBlockState();
        private static final BlockState FOUNDATION_VINE = ErgenverseBlocks.FOUNDATION_ROOT_VINE.get().defaultBlockState();
    }

    private WangFamilyVillageBuilder() {}

    /** Village half-extent. Total footprint = (2*RADIUS+1) squared = 83x83. */
    public static final int VILLAGE_RADIUS = 41;

    /** Canonical village X coordinate. Fixed for every world/seed/player. */
    public static final int VILLAGE_X = 3842;

    /** Canonical village Z coordinate. Fixed for every world/seed/player. */
    public static final int VILLAGE_Z = -1184;

    /**
     * The village center is the fixed canonical position. The Y coordinate
     * is found by scanning the surface height at (VILLAGE_X, VILLAGE_Z).
     */
    public static BlockPos getVillageCenter(ServerLevel level) {
        int surfaceY = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(VILLAGE_X, 0, VILLAGE_Z)).getY();
        return new BlockPos(VILLAGE_X, surfaceY, VILLAGE_Z);
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

    // ── Block palette ─────────────────────────────────────────────────────
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState FARMLAND = Blocks.FARMLAND.defaultBlockState();

    // ── Herb blocks ─────────────────────────────────────────────────────
    /**
     * Build the full village. Flattens an 83x83 area and places all 14 districts.
     */
    public static void build(ServerLevel level) {
        BlockPos center = getVillageCenter(level);
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        Ergenverse.LOGGER.info("[Ergenverse] Building Wang Family Village v2 at ({}, {}, {}).",
                cx, cy, cz);

        // ── 1. Flatten and terrain the area ───────────────────────────
        flattenTerrain(level, cx, cy, cz);

        // ── 2. Main roads (N-S and E-W) ──────────────────────────────
        buildRoads(level, cx, cy, cz);

        // ── 3. Central plaza + spirit vein well ────────────────────
        buildCentralPlaza(level, cx, cy, cz);

        // ── 4. Perimeter fence ───────────────────────────────────────
        buildPerimeterFence(level, cx, cy, cz);

        // ── 5. Wang family home (NW) ────────────────────────────────
        buildWangFamilyHome(level, cx - 18, cy, cz - 18);

        // ── 6. Village elder's home (NE) ────────────────────────────
        buildElderHome(level, cx + 10, cy, cz - 18);

        // ── 7. Commoner homes (10 total, scattered) ──────────────────
        buildCommonerHome(level, cx - 18, cy, cz - 4);   // W1
        buildCommonerHome(level, cx - 18, cy, cz + 6);   // W2
        buildCommonerHome(level, cx + 6, cy, cz - 4);    // E1
        buildCommonerHome(level, cx + 6, cy, cz + 6);    // E2
        buildCommonerHome(level, cx - 10, cy, cz - 28);  // NW1
        buildCommonerHome(level, cx + 2, cy, cz - 28);   // NW2
        buildCommonerHome(level, cx + 16, cy, cz - 28);   // NE1
        buildCommonerHome(level, cx - 10, cy, cz + 12);   // SW1
        buildCommonerHome(level, cx + 2, cy, cz + 12);    // SW2
        buildCommonerHome(level, cx + 16, cy, cz + 12);   // SE1

        // ── 8. Farm plots ───────────────────────────────────────────
        buildNorthFarms(level, cx, cy, cz - 35);
        buildSouthFarms(level, cx, cy, cz + 22);

        // ── 9. Hidden herb garden (behind Wang home) ────────────────
        buildHiddenHerbGarden(level, cx - 28, cy, cz - 12);

        // ── 10. Storage sheds ────────────────────────────────────────
        buildStorageShed(level, cx - 30, cy, cz - 28);
        buildStorageShed(level, cx + 24, cy, cz + 18);

        // ── 11. Spirit trees (12 around village) ─────────────────────
        buildTree(level, cx - 38, cy + 1, cz - 38);
        buildTree(level, cx + 38, cy + 1, cz - 38);
        buildTree(level, cx - 38, cy + 1, cz + 38);
        buildTree(level, cx + 38, cy + 1, cz + 38);
        buildTree(level, cx - 38, cy + 1, cz);
        buildTree(level, cx + 38, cy + 1, cz);
        buildTree(level, cx, cy + 1, cz - 38);
        buildTree(level, cx, cy + 1, cz + 38);
        buildTree(level, cx - 20, cy + 1, cz - 38);
        buildTree(level, cx + 20, cy + 1, cz - 38);
        buildTree(level, cx - 20, cy + 1, cz + 38);
        buildTree(level, cx + 20, cy + 1, cz + 38);

        // ── 12. Path light markers (spirit vein stones along roads) ──
        buildPathLights(level, cx, cy, cz);

        // ── 14. Loot chests for key buildings ──────────────────────
        // Storage shed warehouse chest
        ChestHelper.placeChestWithLoot(level, new BlockPos(cx - 30 + 1, cy + 1, cz - 28 + 1),
                new ResourceLocation("ergenverse", "chests/wang_family_village_warehouse"));
        // Tavern chest near village center
        ChestHelper.placeChestWithLoot(level, new BlockPos(cx + 10, cy + 1, cz + 5),
                new ResourceLocation("ergenverse", "chests/wang_family_village_tavern"));

        // ── 15. Alchemy Furnace in Wang family home ──────────────────
        // Already placed inside buildWangFamilyHome

        Ergenverse.LOGGER.info("[Ergenverse] Wang Family Village v2 construction complete.");
    }

    // ── Wang Lin's Corner — Evidence, Not Furniture ───────────────────
    //
    // Article XLV §5: "Interiors Are Character, Not Furniture."
    // The user's directive: "Don't build houses. Build evidence."
    // The player enters and CONSTRUCTS Wang Lin from what they see.
    //
    // This is the ONE room. The standard for every future room.
    // Do not build another room until this one is believable.

    private static void buildWangLinCorner(ServerLevel level, int x, int y, int z) {
        // The house is 7x5 (dx 0-6, dz 0-4). Interior: dx 1-5, dz 1-3.
        // Wang Lin's corner: NE quadrant (dx 4-5, dz 1-2).
        // Family area: NW (existing family chest at dx1,dz1) + furnace center.

        // ── 1. Sleeping mat (white carpet) ───────────────────────────
        // A poor family. Wang Lin sleeps on the floor in the NE corner.
        // The carpet is thin, worn, placed against the east wall.
        level.setBlock(new BlockPos(x + 5, y + 1, z + 1),
                Blocks.WHITE_CARPET.defaultBlockState(), 3);

        // ── 2. Hidden private journal (trapped chest) ────────────────
        // Tucked behind the sleeping mat, against the east wall.
        // A trapped chest — if opened carelessly, it triggers a redstone
        // signal (Wang Lin would know). The journal inside is private.
        BlockPos journalPos = new BlockPos(x + 4, y + 1, z + 1);
        level.setBlock(journalPos, Blocks.TRAPPED_CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(journalPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, createWrittenBook(
                    "Private Journal",
                    "Wang Lin",
                    "I must not let Mother see this.",
                    "The restriction diagram still fails. Third attempt. The lines will not hold.",
                    "Father's furnace grows cold. I cannot reignite it. I am not strong enough.",
                    "Wang Hao looked at me strangely today. I do not trust him.",
                    "The wolves came closer last night. I heard them behind the elder's house.",
                    "Old Chen's dog is missing. He asked me if I had seen it. I had not.",
                    "If anyone reads this, I will deny it."
            ));
        }

        // ── 3. Cultivation notes (lectern with worn book) ───────────
        // A lectern next to the sleeping mat. Wang Lin studies here
        // before dawn. The book is his own handwriting — observations,
        // not technique. He is self-taught.
        BlockPos lecternPos = new BlockPos(x + 5, y + 1, z + 2);
        level.setBlock(lecternPos, Blocks.LECTERN.defaultBlockState(), 2);
        if (level.getBlockEntity(lecternPos) instanceof LecternBlockEntity lectern) {
            lectern.setBook(createWrittenBook(
                    "Cultivation Notes",
                    "Wang Lin",
                    "Qi Gathering. Breathe in through the nose, out through the mouth.",
                    "The first layer requires stillness. I am not still. My mind moves like water.",
                    "Father said: 'Observe Heaven. All patterns come from observation.'",
                    "I try to observe the ants. They work without complaint. I do not understand how.",
                    "Spirit density near the well is higher. I feel it when the wind stops.",
                    "I must not tell anyone what I feel. They will think I am cursed."
            ));
        }

        // ── 4. Repaired farming tool (item frame on north wall) ─────
        // A damaged iron hoe, hung on the wall above the family chest.
        // Wang Lin repairs the family's tools. The hoe is worn but
        // functional — he fixed it, he didn't replace it.
        ItemStack damagedHoe = new ItemStack(Items.IRON_HOE);
        damagedHoe.setDamageValue(118); // well-worn, nearly half durability used
        // Frame on interior face of north wall (dz=0), above the family chest (dx=1, dz=1)
        // Frame hangs at (x+1, y+2, z+1), faces SOUTH (into the room)
        placeItemFrame(level, new BlockPos(x + 1, y + 2, z + 1), Direction.SOUTH, damagedHoe);

        // ── 5. Worn shoes by the door (item frame near doorway) ──────
        // Leather boots, placed by the door where Wang Lin leaves them
        // when he comes in. Worn, damaged — he walks far to gather herbs.
        ItemStack wornBoots = new ItemStack(Items.LEATHER_BOOTS);
        wornBoots.setDamageValue(38);
        // Door is at dx=3, dz=4. Frame on interior face of south wall,
        // just east of the door. Frame at (x+4, y+1, z+3), faces SOUTH.
        placeItemFrame(level, new BlockPos(x + 4, y + 1, z + 3), Direction.SOUTH, wornBoots);

        // ── 6. Unfinished restriction diagram (redstone on floor) ───
        // Two redstone dust pieces on the floor between the family chest
        // and the furnace. An incomplete line — Wang Lin was practicing
        // restriction formations and gave up mid-attempt. The diagram
        // is deliberately unfinished. It tells the player: someone here
        // is trying to learn something they were not taught.
        level.setBlock(new BlockPos(x + 2, y + 1, z + 1),
                Blocks.REDSTONE_WIRE.defaultBlockState(), 3);
        level.setBlock(new BlockPos(x + 3, y + 1, z + 1),
                Blocks.REDSTONE_WIRE.defaultBlockState(), 3);
        // The line stops at x+3. It should continue to x+4 but doesn't.
        // That gap is the story — he couldn't finish it.
    }

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

    // ── Terrain ──────────────────────────────────────────────────────────

    private static void flattenTerrain(ServerLevel level, int cx, int cy, int cz) {
        for (int dx = -VILLAGE_RADIUS; dx <= VILLAGE_RADIUS; dx++) {
            for (int dz = -VILLAGE_RADIUS; dz <= VILLAGE_RADIUS; dz++) {
                BlockPos ground = new BlockPos(cx + dx, cy, cz + dz);
                // Clear 6 blocks above for headroom + tree space.
                for (int h = 1; h <= 6; h++) {
                    level.setBlock(ground.above(h), AIR, 3);
                }
                // Ground: spirit grass everywhere inside the village.
                level.setBlock(ground, B.SPIRIT_GRASS, 3);
            }
        }
    }

    // ── Roads ────────────────────────────────────────────────────────────

    private static void buildRoads(ServerLevel level, int cx, int cy, int cz) {
        // N-S main road (width 3, running full N-S length of village)
        for (int dz = -VILLAGE_RADIUS; dz <= VILLAGE_RADIUS; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                level.setBlock(new BlockPos(cx + dx, cy, cz + dz), B.SPIRIT_SAND, 3);
            }
        }
        // E-W crossroad (width 3)
        for (int dx = -VILLAGE_RADIUS; dx <= VILLAGE_RADIUS; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(new BlockPos(cx + dx, cy, cz + dz), B.SPIRIT_SAND, 3);
            }
        }
        // Narrow paths to key buildings (width 1)
        // Path to Wang home (NW)
        for (int i = 1; i <= 14; i++) {
            level.setBlock(new BlockPos(cx - i, cy, cz - 14), B.SPIRIT_SAND, 3);
        }
        // Path to Elder home (NE)
        for (int i = 1; i <= 14; i++) {
            level.setBlock(new BlockPos(cx + i, cy, cz - 14), B.SPIRIT_SAND, 3);
        }
        // Path to SW homes
        for (int i = 1; i <= 8; i++) {
            level.setBlock(new BlockPos(cx - 8, cy, cz + i), B.SPIRIT_SAND, 3);
        }
        // Path to SE homes
        for (int i = 1; i <= 8; i++) {
            level.setBlock(new BlockPos(cx + 8, cy, cz + i), B.SPIRIT_SAND, 3);
        }
    }

    // ── Central Plaza ───────────────────────────────────────────────────

    private static void buildCentralPlaza(ServerLevel level, int cx, int cy, int cz) {
        // 9x9 spirit stone plaza around center (wider than road)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                level.setBlock(new BlockPos(cx + dx, cy, cz + dz), B.SPIRIT_STONE, 3);
            }
        }

        // Spirit vein centerpiece (the hidden vein the village sits on)
        level.setBlock(new BlockPos(cx, cy, cz), B.SPIRIT_VEIN, 3);
        level.setBlock(new BlockPos(cx, cy + 1, cz), B.SPIRIT_VEIN, 3);

        // 4 formation core stones at cardinal positions (2 blocks out)
        level.setBlock(new BlockPos(cx, cy, cz - 3), B.FORMATION_CORE, 3);
        level.setBlock(new BlockPos(cx, cy, cz + 3), B.FORMATION_CORE, 3);
        level.setBlock(new BlockPos(cx - 3, cy, cz), B.FORMATION_CORE, 3);
        level.setBlock(new BlockPos(cx + 3, cy, cz), B.FORMATION_CORE, 3);

        // Village well: 3x3 water pit with spirit stone rim, 2 blocks deep
        BlockPos wellCenter = new BlockPos(cx + 6, cy, cz + 1);
        // Rim (spirit stone)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue; // water in center
                level.setBlock(wellCenter.offset(dx, 0, dz), B.SPIRIT_STONE, 3);
            }
        }
        // Water inside
        level.setBlock(wellCenter, WATER, 3);
        // Well column above water (spirit vein stone)
        level.setBlock(wellCenter.above(), B.SPIRIT_VEIN, 3);
        level.setBlock(wellCenter.above(2), B.SPIRIT_VEIN, 3);

        // 8 spirit vein light stones around plaza perimeter
        int[][] lightPositions = {
            {-6, -6}, {-6, 6}, {6, -6}, {6, 6},
            {-6, 0}, {6, 0}, {0, -6}, {0, 6}
        };
        for (int[] pos : lightPositions) {
            level.setBlock(new BlockPos(cx + pos[0], cy + 1, cz + pos[1]), B.SPIRIT_VEIN, 3);
        }
    }

    // ── Perimeter Fence ──────────────────────────────────────────────────

    private static void buildPerimeterFence(ServerLevel level, int cx, int cy, int cz) {
        int r = VILLAGE_RADIUS;
        // Place fence posts every 2 blocks along perimeter, with gaps at N and S entries.
        for (int angle = 0; angle < 360; angle += 5) {
            double rad = Math.toRadians(angle);
            int fx = cx + (int) Math.round(Math.cos(rad) * r);
            int fz = cz + (int) Math.round(Math.sin(rad) * r);
            // Skip gaps at N (angle ≈ 270°) and S (angle ≈ 90°) entries (10° wide each)
            if ((angle >= 265 && angle <= 275) || (angle >= 85 && angle <= 95)) continue;
            level.setBlock(new BlockPos(fx, cy + 1, fz), B.LOG, 3);
        }
    }

    // ── Wang Family Home ─────────────────────────────────────────────────

    /**
     * Wang Lin's family home. Modest 7x5 house with stone foundation steps,
     * spirit wood construction. Contains an Alchemy Furnace (Wang Tian's).
     */
    private static void buildWangFamilyHome(ServerLevel level, int x, int y, int z) {
        // Stone foundation (slightly wider than house)
        for (int dx = -1; dx <= 7; dx++) {
            for (int dz = -1; dz <= 5; dz++) {
                level.setBlock(new BlockPos(x + dx, y - 1, z + dz), B.SPIRIT_STONE, 3);
            }
        }
        // Floor
        for (int dx = 0; dx < 7; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                level.setBlock(new BlockPos(x + dx, y, z + dz), B.PLANKS, 3);
            }
        }
        // Walls (y+1, y+2, y+3) with doorway on south face (dz=4, dx=3)
        for (int dy = 1; dy <= 4; dy++) {
            for (int dx = 0; dx < 7; dx++) {
                for (int dz = 0; dz < 5; dz++) {
                    BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                    boolean edge = dx == 0 || dx == 6 || dz == 0 || dz == 4;
                    boolean corner = edge && (dx == 0 || dx == 6) && (dz == 0 || dz == 4);
                    boolean isDoorway = dz == 4 && dx == 3 && dy <= 3;
                    boolean isRoof = dy == 4;

                    if (isDoorway) {
                        level.setBlock(pos, AIR, 3);
                    } else if (isRoof) {
                        // Roof: spirit wood leaves with log ridge beam on top
                        if (dz == 2) {
                            level.setBlock(pos, B.LOG, 3); // ridge beam
                        } else {
                            level.setBlock(pos, B.LEAVES, 3);
                        }
                    } else if (corner) {
                        level.setBlock(pos, B.LOG, 3);
                    } else if (edge) {
                        level.setBlock(pos, B.PLANKS, 3);
                    } else {
                        level.setBlock(pos, AIR, 3);
                    }
                }
            }
        }
        // Alchemy Furnace inside (Wang Tian's legacy)
        level.setBlock(new BlockPos(x + 3, y + 1, z + 2),
                ErgenverseBlocks.ALCHEMY_FURNACE.get().defaultBlockState(), 3);
        // Herb pot outside the door
        level.setBlock(new BlockPos(x + 5, y + 1, z + 5), B.QI_GRASS, 3);
        // Chest with family keepsakes
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 1, y + 1, z + 1),
                new ResourceLocation("ergenverse", "chests/wang_family_village_main"));

        // ── Wang Lin's corner: evidence, not furniture ───────────────
        // Article XLV §5. The ONE room. Do not build another until this
        // one is believable. The player enters and constructs Wang Lin
        // from what they see: sleeping mat, hidden journal, cultivation
        // notes, repaired hoe, worn shoes, unfinished restriction diagram.
        buildWangLinCorner(level, x, y, z);
    }

    // ── Elder's Home ────────────────────────────────────────────────────

    /**
     * Village elder's home. Slightly larger 7x7 house with jade stone steps,
     * formation flag base (status symbol), nicer construction.
     */
    private static void buildElderHome(ServerLevel level, int x, int y, int z) {
        // Jade stone steps at entrance
        for (int dx = 2; dx <= 4; dx++) {
            level.setBlock(new BlockPos(x + dx, y - 1, z + 7), B.JADE_STONE, 3);
        }
        // Stone foundation
        for (int dx = -1; dx <= 7; dx++) {
            for (int dz = -1; dz <= 7; dz++) {
                level.setBlock(new BlockPos(x + dx, y - 1, z + dz), B.SPIRIT_STONE, 3);
            }
        }
        // Floor
        for (int dx = 0; dx < 7; dx++) {
            for (int dz = 0; dz < 7; dz++) {
                level.setBlock(new BlockPos(x + dx, y, z + dz), B.PLANKS, 3);
            }
        }
        // Walls (y+1..y+3), roof at y+4, doorway on south (dz=6, dx=3)
        for (int dy = 1; dy <= 4; dy++) {
            for (int dx = 0; dx < 7; dx++) {
                for (int dz = 0; dz < 7; dz++) {
                    BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                    boolean edge = dx == 0 || dx == 6 || dz == 0 || dz == 6;
                    boolean corner = edge && (dx == 0 || dx == 6) && (dz == 0 || dz == 6);
                    boolean isDoorway = dz == 6 && dx == 3 && dy <= 3;
                    boolean isRoof = dy == 4;

                    if (isDoorway) {
                        level.setBlock(pos, AIR, 3);
                    } else if (isRoof) {
                        if (dz == 3) {
                            level.setBlock(pos, B.LOG, 3); // ridge beam
                        } else {
                            level.setBlock(pos, B.LEAVES, 3);
                        }
                    } else if (corner) {
                        level.setBlock(pos, B.LOG, 3);
                    } else if (edge) {
                        level.setBlock(pos, B.PLANKS, 3);
                    } else {
                        level.setBlock(pos, AIR, 3);
                    }
                }
            }
        }
        // Formation Flag Base inside (elder's status symbol)
        level.setBlock(new BlockPos(x + 3, y + 1, z + 3),
                ErgenverseBlocks.FORMATION_FLAG_BASE.get().defaultBlockState(), 3);
        // Jade stone decoration outside
        level.setBlock(new BlockPos(x + 3, y + 1, z + 7), B.JADE_STONE, 3);
        // Chest with elder's valuables
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 5, y + 1, z + 1),
                new ResourceLocation("ergenverse", "chests/wang_family_village_governor_mansion"));
    }

    // ── Commoner Homes ──────────────────────────────────────────────────

    /**
     * A standard 5x5 commoner home. Spirit wood construction, simple design.
     * Each has a small herb pot outside the door.
     */
    private static void buildCommonerHome(ServerLevel level, int x, int y, int z) {
        // Floor
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                level.setBlock(new BlockPos(x + dx, y, z + dz), B.PLANKS, 3);
            }
        }
        // Walls (y+1, y+2), roof at y+3, doorway on south (dz=4, dx=2)
        for (int dy = 1; dy <= 3; dy++) {
            for (int dx = 0; dx < 5; dx++) {
                for (int dz = 0; dz < 5; dz++) {
                    BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                    boolean edge = dx == 0 || dx == 4 || dz == 0 || dz == 4;
                    boolean corner = edge && (dx == 0 || dx == 4) && (dz == 0 || dz == 4);
                    boolean isDoorway = dz == 4 && dx == 2 && dy <= 2;
                    boolean isRoof = dy == 3;

                    if (isDoorway) {
                        level.setBlock(pos, AIR, 3);
                    } else if (isRoof) {
                        level.setBlock(pos, B.LEAVES, 3);
                    } else if (corner) {
                        level.setBlock(pos, B.LOG, 3);
                    } else if (edge) {
                        level.setBlock(pos, B.PLANKS, 3);
                    } else {
                        level.setBlock(pos, AIR, 3);
                    }
                }
            }
        }
        // Small herb pot outside each door
        BlockState[] herbs = {B.QI_GRASS, B.SNOW_HERB, B.DAO_VINE, B.FOUNDATION_VINE,
                B.FIRE_LOTUS, B.SOUL_LOTUS, B.SWORD_MOSS, B.NINE_CLOVER, B.FIVE_GINSENG,
                B.VERMILION_GINSENG};
        level.setBlock(new BlockPos(x + 2, y + 1, z + 5),
                herbs[(x * 7 + z) % herbs.length], 3);
        // Chest inside for personal belongings
        ChestHelper.placeChestWithLoot(level, new BlockPos(x + 1, y + 1, z + 1),
                new ResourceLocation("ergenverse", "chests/wang_family_village_residential"));
    }

    // ── Farm Plots ──────────────────────────────────────────────────────

    /**
     * North farm plots: 3 rectangular fields of farmland with spirit herb
     * rows. The village grows spirit herbs without knowing what they are.
     */
    private static void buildNorthFarms(ServerLevel level, int cx, int cy, int cz) {
        buildFarmField(level, cx - 18, cy, cz, 14, 10);
        buildFarmField(level, cx + 4, cy, cz, 14, 10);
    }

    private static void buildSouthFarms(ServerLevel level, int cx, int cy, int cz) {
        buildFarmField(level, cx - 12, cy, cz, 10, 8);
        buildFarmField(level, cx + 2, cy, cz, 10, 8);
    }

    /**
     * Build a single farm field. Farmland base with herb rows.
     * Rows alternate: farmland + spirit grass border + herb every 3 blocks.
     */
    private static void buildFarmField(ServerLevel level, int x, int y, int z,
                                         int width, int depth) {
        BlockState[] rowHerbs = {B.QI_GRASS, B.DAO_VINE, B.FOUNDATION_VINE, B.QI_GRASS};
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                BlockPos pos = new BlockPos(x + dx, y, z + dz);
                // Spirit grass border around field
                if (dx == 0 || dx == width - 1 || dz == 0 || dz == depth - 1) {
                    level.setBlock(pos, B.SPIRIT_GRASS, 3);
                } else {
                    level.setBlock(pos, FARMLAND, 3);
                }
            }
        }
        // Plant herbs in every 3rd row
        for (int dz = 1; dz < depth - 1; dz += 3) {
            for (int dx = 1; dx < width - 1; dx += 2) {
                level.setBlock(new BlockPos(x + dx, y + 1, z + dz),
                        rowHerbs[(dx + dz) % rowHerbs.length], 3);
            }
        }
    }

    // ── Hidden Herb Garden ─────────────────────────────────────────────

    /**
     * Hidden herb garden behind Wang family home. Enclosed by spirit wood
     * log fence. Contains rare herbs that the village doesn't know are valuable.
     */
    private static void buildHiddenHerbGarden(ServerLevel level, int x, int y, int z) {
        // 10x8 garden enclosed by log fence
        // Floor: spirit dirt (herbs grow better here)
        for (int dx = 0; dx < 10; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                level.setBlock(new BlockPos(x + dx, y, z + dz), B.SPIRIT_DIRT, 3);
            }
        }
        // Fence (log pillars at y+1 and y+2)
        for (int dx = -1; dx <= 10; dx++) {
            for (int dz = -1; dz <= 8; dz++) {
                boolean edge = dx == -1 || dx == 10 || dz == -1 || dz == 8;
                if (!edge) continue;
                level.setBlock(new BlockPos(x + dx, y + 1, z + dz), B.LOG, 3);
                // Gate gap at south (dz=8, dx=4..5)
                if (dz == 8 && (dx == 4 || dx == 5)) {
                    level.setBlock(new BlockPos(x + dx, y + 1, z + dz), AIR, 3);
                }
            }
        }
        // Plant rare herbs in rows
        BlockState[] rareHerbs = {
                B.FIVE_GINSENG, B.NINE_CLOVER, B.VERMILION_GINSENG,
                B.SOUL_LOTUS, B.FIRE_LOTUS, B.SWORD_MOSS,
                B.FIVE_GINSENG, B.NINE_CLOVER
        };
        int idx = 0;
        for (int dz = 1; dz <= 7; dz += 2) {
            for (int dx = 1; dx <= 9; dx += 2) {
                level.setBlock(new BlockPos(x + dx, y + 1, z + dz), rareHerbs[idx % rareHerbs.length], 3);
                idx++;
            }
        }
    }

    // ── Storage Sheds ───────────────────────────────────────────────────

    /**
     * Small 3x3 storage shed. No interior contents.
     */
    private static void buildStorageShed(ServerLevel level, int x, int y, int z) {
        for (int dx = 0; dx < 3; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                level.setBlock(new BlockPos(x + dx, y, z + dz), B.PLANKS, 3); // floor
                level.setBlock(new BlockPos(x + dx, y + 2, z + dz), B.LEAVES, 3); // roof
            }
        }
        // Walls
        for (int dy = 1; dy <= 1; dy++) {
            for (int dx = 0; dx < 3; dx++) {
                for (int dz = 0; dz < 3; dz++) {
                    BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                    boolean edge = dx == 0 || dx == 2 || dz == 0 || dz == 2;
                    boolean isDoorway = dz == 2 && dx == 1;
                    if (isDoorway) {
                        level.setBlock(pos, AIR, 3);
                    } else if (edge) {
                        level.setBlock(pos, B.PLANKS, 3);
                    } else {
                        level.setBlock(pos, AIR, 3);
                    }
                }
            }
        }
    }

    // ── Spirit Tree ────────────────────────────────────────────────────

    /**
     * A decorative spirit wood tree (5-block trunk + leaf canopy).
     */
    private static void buildTree(ServerLevel level, int x, int y, int z) {
        // trunk (5 blocks)
        for (int dy = 0; dy < 5; dy++) {
            level.setBlock(new BlockPos(x, y + dy, z), B.LOG, 3);
        }
        // canopy (radius 3, layers 4-7)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = 4; dy <= 7; dy++) {
                    if (dx == 0 && dz == 0 && dy <= 4) continue;
                    double dist = Math.sqrt((double) dx * dx + (double) dz * dz);
                    if (dist <= 3.0) {
                        BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                        if (level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, B.LEAVES, 3);
                        }
                    }
                }
            }
        }
    }

    // ── Path Lights ───────────────────────────────────────────────────

    private static void buildPathLights(ServerLevel level, int cx, int cy, int cz) {
        // Spirit vein lights along N-S road every 8 blocks
        for (int dz = -VILLAGE_RADIUS + 4; dz <= VILLAGE_RADIUS - 4; dz += 8) {
            level.setBlock(new BlockPos(cx - 2, cy + 1, cz + dz), B.SPIRIT_VEIN, 3);
            level.setBlock(new BlockPos(cx + 2, cy + 1, cz + dz), B.SPIRIT_VEIN, 3);
        }
        // Along E-W road
        for (int dx = -VILLAGE_RADIUS + 4; dx <= VILLAGE_RADIUS - 4; dx += 8) {
            level.setBlock(new BlockPos(cx + dx, cy + 1, cz - 2), B.SPIRIT_VEIN, 3);
            level.setBlock(new BlockPos(cx + dx, cy + 1, cz + 2), B.SPIRIT_VEIN, 3);
        }
    }
}
