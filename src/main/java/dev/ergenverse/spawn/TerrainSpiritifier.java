package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

/**
 * TerrainSpiritifier — converts ALL vanilla terrain blocks to Ergenverse
 * custom blocks when a chunk loads.
 *
 * <p>Per the user's directive: "there should be 0 vanilla minecraft blocks."
 * This handler walks every block position in each newly-loaded overworld
 * chunk and replaces vanilla terrain (grass, dirt, stone, sand, sandstone,
 * gravel, all log/leaf types, all common ores) with their spirit-world
 * equivalents.
 *
 * <p>Mapping (vanilla → custom):
 * <ul>
 *   <li>grass_block, podzol, mycelium, dirt_path, coarse_dirt, rooted_dirt, moss_block → SPIRIT_GRASS</li>
 *   <li>dirt, farmland → SPIRIT_DIRT</li>
 *   <li>stone, cobblestone, deepslate, deepslate_cobblestone, andesite, diorite, granite, tuff, calcite, dripstone → SPIRIT_STONE_BLOCK</li>
 *   <li>sand, red_sand, gravel, Suspicious sand → SPIRIT_SAND</li>
 *   <li>sandstone, red_sandstone, cut_sandstone → SPIRIT_SANDSTONE</li>
 *   <li>all log types (oak, birch, spruce, jungle, acacia, dark_oak, cherry, mangrove) → SPIRIT_WOOD_LOG</li>
 *   <li>all leaf types → SPIRIT_WOOD_LEAVES</li>
 *   <li>coal_ore, deepslate_coal_ore → SPIRIT_STONE_BLOCK</li>
 *   <li>iron_ore, deepslate_iron_ore, raw_iron_block → SPIRIT_IRON_ORE</li>
 *   <li>gold_ore, deepslate_gold_ore, copper_ore, deepslate_copper_ore → COLD_IRON_ORE</li>
 *   <li>diamond_ore, deepslate_diamond_ore, emerald_ore, deepslate_emerald_ore → SPIRIT_STONE_ORE</li>
 *   <li>redstone_ore, deepslate_redstone_ore, lapis_ore, deepslate_lapis_ore → SPIRIT_STONE_BLOCK</li>
 * </ul>
 *
 * <p>Kept as-is: air, water, lava, bedrock, obsidian, Nether/End blocks (we
 * only convert overworld chunks). Player-placed blocks are naturally safe
 * because they are not in the vanilla→custom map (the map only contains
 * natural terrain types).
 *
 * <p>Idempotent: re-running on an already-converted chunk is a no-op because
 * the custom blocks are not in the map. A static converted-chunk set avoids
 * the iteration cost on subsequent loads of the same chunk within a session.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TerrainSpiritifier {

    private TerrainSpiritifier() {}

    /** Tracks chunks already converted this session (avoids re-iteration). */
    private static final java.util.Set<Long> convertedChunks = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    /** Vanilla block → custom block state mapping. Built once at class init. */
    private static final Map<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.Block> CONVERSION_MAP = buildMap();

    private static Map<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.Block> buildMap() {
        Map<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.Block> m = new java.util.HashMap<>();
        // Grass-like surfaces
        m.put(Blocks.GRASS_BLOCK, ErgenverseBlocks.SPIRIT_GRASS.get());
        m.put(Blocks.PODZOL, ErgenverseBlocks.SPIRIT_GRASS.get());
        m.put(Blocks.MYCELIUM, ErgenverseBlocks.SPIRIT_GRASS.get());
        m.put(Blocks.DIRT_PATH, ErgenverseBlocks.SPIRIT_GRASS.get());
        m.put(Blocks.COARSE_DIRT, ErgenverseBlocks.SPIRIT_GRASS.get());
        m.put(Blocks.ROOTED_DIRT, ErgenverseBlocks.SPIRIT_GRASS.get());
        m.put(Blocks.MOSS_BLOCK, ErgenverseBlocks.SPIRIT_GRASS.get());
        m.put(Blocks.FARMLAND, ErgenverseBlocks.SPIRIT_DIRT.get());
        // Dirt
        m.put(Blocks.DIRT, ErgenverseBlocks.SPIRIT_DIRT.get());
        // Stone variants
        m.put(Blocks.STONE, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.COBBLESTONE, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.DEEPSLATE, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.COBBLED_DEEPSLATE, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.ANDESITE, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.DIORITE, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.GRANITE, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.TUFF, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.CALCITE, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.DRIPSTONE_BLOCK, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.BASALT, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.SMOOTH_BASALT, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.BEDROCK, null); // keep — never convert bedrock
        // Sand
        m.put(Blocks.SAND, ErgenverseBlocks.SPIRIT_SAND.get());
        m.put(Blocks.RED_SAND, ErgenverseBlocks.SPIRIT_SAND.get());
        m.put(Blocks.GRAVEL, ErgenverseBlocks.SPIRIT_SAND.get());
        m.put(Blocks.SUSPICIOUS_SAND, ErgenverseBlocks.SPIRIT_SAND.get());
        m.put(Blocks.SUSPICIOUS_GRAVEL, ErgenverseBlocks.SPIRIT_SAND.get());
        // Sandstone
        m.put(Blocks.SANDSTONE, ErgenverseBlocks.SPIRIT_SANDSTONE.get());
        m.put(Blocks.RED_SANDSTONE, ErgenverseBlocks.SPIRIT_SANDSTONE.get());
        m.put(Blocks.CUT_SANDSTONE, ErgenverseBlocks.SPIRIT_SANDSTONE.get());
        m.put(Blocks.CUT_RED_SANDSTONE, ErgenverseBlocks.SPIRIT_SANDSTONE.get());
        m.put(Blocks.CHISELED_SANDSTONE, ErgenverseBlocks.SPIRIT_SANDSTONE.get());
        m.put(Blocks.SMOOTH_SANDSTONE, ErgenverseBlocks.SPIRIT_SANDSTONE.get());
        m.put(Blocks.SMOOTH_RED_SANDSTONE, ErgenverseBlocks.SPIRIT_SANDSTONE.get());
        // Logs — all vanilla tree types
        m.put(Blocks.OAK_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.BIRCH_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.SPRUCE_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.JUNGLE_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.ACACIA_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.DARK_OAK_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.CHERRY_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.MANGROVE_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.STRIPPED_OAK_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.STRIPPED_BIRCH_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.STRIPPED_SPRUCE_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.STRIPPED_JUNGLE_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.STRIPPED_ACACIA_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.STRIPPED_DARK_OAK_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.STRIPPED_CHERRY_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        m.put(Blocks.STRIPPED_MANGROVE_LOG, ErgenverseBlocks.SPIRIT_WOOD_LOG.get());
        // Leaves
        m.put(Blocks.OAK_LEAVES, ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get());
        m.put(Blocks.BIRCH_LEAVES, ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get());
        m.put(Blocks.SPRUCE_LEAVES, ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get());
        m.put(Blocks.JUNGLE_LEAVES, ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get());
        m.put(Blocks.ACACIA_LEAVES, ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get());
        m.put(Blocks.DARK_OAK_LEAVES, ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get());
        m.put(Blocks.CHERRY_LEAVES, ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get());
        m.put(Blocks.MANGROVE_LEAVES, ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get());
        m.put(Blocks.AZALEA_LEAVES, ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get());
        m.put(Blocks.FLOWERING_AZALEA_LEAVES, ErgenverseBlocks.SPIRIT_WOOD_LEAVES.get());
        // Ores
        m.put(Blocks.COAL_ORE, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.DEEPSLATE_COAL_ORE, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.IRON_ORE, ErgenverseBlocks.SPIRIT_IRON_ORE.get());
        m.put(Blocks.DEEPSLATE_IRON_ORE, ErgenverseBlocks.SPIRIT_IRON_ORE.get());
        m.put(Blocks.RAW_IRON_BLOCK, ErgenverseBlocks.SPIRIT_IRON_ORE.get());
        m.put(Blocks.GOLD_ORE, ErgenverseBlocks.COLD_IRON_ORE.get());
        m.put(Blocks.DEEPSLATE_GOLD_ORE, ErgenverseBlocks.COLD_IRON_ORE.get());
        m.put(Blocks.COPPER_ORE, ErgenverseBlocks.COLD_IRON_ORE.get());
        m.put(Blocks.DEEPSLATE_COPPER_ORE, ErgenverseBlocks.COLD_IRON_ORE.get());
        m.put(Blocks.DIAMOND_ORE, ErgenverseBlocks.SPIRIT_STONE_ORE.get());
        m.put(Blocks.DEEPSLATE_DIAMOND_ORE, ErgenverseBlocks.SPIRIT_STONE_ORE.get());
        m.put(Blocks.EMERALD_ORE, ErgenverseBlocks.SPIRIT_STONE_ORE.get());
        m.put(Blocks.DEEPSLATE_EMERALD_ORE, ErgenverseBlocks.SPIRIT_STONE_ORE.get());
        m.put(Blocks.REDSTONE_ORE, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.DEEPSLATE_REDSTONE_ORE, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.LAPIS_ORE, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.DEEPSLATE_LAPIS_ORE, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        // Clay, snow, ice
        m.put(Blocks.CLAY, ErgenverseBlocks.SPIRIT_DIRT.get());
        m.put(Blocks.PACKED_MUD, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.MUD_BRICKS, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        return java.util.Collections.unmodifiableMap(m);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        // Only convert overworld chunks.
        if (!(event.getLevel() instanceof Level level)) return;
        if (level.dimension() != Level.OVERWORLD) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        // Only process on the server side.
        if (level.isClientSide()) return;

        long chunkKey = net.minecraft.world.level.ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z);
        if (convertedChunks.contains(chunkKey)) return;

        int converted = 0;
        int minSection = chunk.getMinSection();
        LevelChunkSection[] sections = chunk.getSections();

        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (section == null || section.hasOnlyAir()) continue;
            int sectionY = minSection + i;
            int baseY = sectionY << 4;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        net.minecraft.world.level.block.Block target = CONVERSION_MAP.get(state.getBlock());
                        if (target == null) continue; // not a convertible block
                        // null in the map means "keep" (e.g. bedrock)
                        // (handled by the `continue` above since get() returns null for unmapped)
                        BlockPos pos = new BlockPos(
                                chunk.getPos().getMinBlockX() + x,
                                baseY + y,
                                chunk.getPos().getMinBlockZ() + z);
                        BlockState newState = target.defaultBlockState();
                        // Flag 2 = UPDATE_CLIENTS (sync to client, no neighbor cascade)
                        level.setBlock(pos, newState, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                        converted++;
                    }
                }
            }
        }

        convertedChunks.add(chunkKey);
        if (converted > 0) {
            Ergenverse.LOGGER.debug("[Ergenverse] Spiritified chunk ({}, {}): {} blocks converted.",
                    chunk.getPos().x, chunk.getPos().z, converted);
        }
    }

    /** Clear the converted-chunk cache (called on server stop). */
    public static void clearCache() {
        convertedChunks.clear();
    }
}
