package dev.ergenverse.spawn;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

/**
 * TerrainSpiritifier — converts ALL vanilla terrain blocks to Ergenverse
 * custom blocks near each online player, on the server tick.
 *
 * <p><b>CRASH-SAFE DESIGN:</b> The previous version used ChunkEvent.Load,
 * which crashed during world generation because setBlock() during chunk
 * load triggers recursive chunk loads. This version uses ServerTickEvent
 * (the main server thread, safe for setBlock) and converts chunks in a
 * radius around each player. Chunks are already loaded (the player is in
 * them), so there is no recursion risk.
 *
 * <p>Per the user's directive: "there should be 0 vanilla minecraft blocks."
 *
 * <p>Conversion strategy:
 * <ul>
 *   <li>Every 10 ticks (0.5 sec), for each online player, convert ONE
 *       unconverted chunk within a 3-chunk radius of the player.</li>
 *   <li>Prioritize the player's own chunk, then nearest neighbors.</li>
 *   <li>A static converted-chunk set prevents re-conversion.</li>
 *   <li>Within a chunk, iterate by section, skipping empty sections.</li>
 * </ul>
 *
 * <p>Mapping (vanilla → custom):
 * <ul>
 *   <li>grass_block, podzol, mycelium, dirt_path, coarse_dirt, rooted_dirt, moss → SPIRIT_GRASS</li>
 *   <li>dirt, farmland → SPIRIT_DIRT</li>
 *   <li>stone, cobblestone, deepslate, cobbled_deepslate, andesite, diorite, granite, tuff, calcite, dripstone, basalt → SPIRIT_STONE_BLOCK</li>
 *   <li>sand, red_sand, gravel → SPIRIT_SAND</li>
 *   <li>sandstone, red_sandstone → SPIRIT_SANDSTONE</li>
 *   <li>all logs → SPIRIT_WOOD_LOG; all leaves → SPIRIT_WOOD_LEAVES</li>
 *   <li>iron ore → SPIRIT_IRON_ORE; gold/copper ore → COLD_IRON_ORE; diamond/emerald ore → SPIRIT_STONE_ORE</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TerrainSpiritifier {

    private TerrainSpiritifier() {}

    /** Chunks already converted this session. Key = ChunkPos.asLong(). */
    private static final java.util.Set<Long> convertedChunks = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    /** Tick interval — convert every 10 ticks (0.5 sec). */
    private static final int TICK_INTERVAL = 10;

    /** Radius (in chunks) around the player to convert. 3 = 7x7 chunk area. */
    private static final int CHUNK_RADIUS = 3;

    /**
     * Vanilla → custom block mapping.
     *
     * <p><b>LAZY-INITIALIZED:</b> Cannot be a static final field because
     * {@code ErgenverseBlocks.SPIRIT_GRASS.get()} throws NullPointerException
     * if called before Forge has populated the block registry. The previous
     * static-final version crashed the mod at class-load time during
     * AutomaticEventSubscriber injection (before any registry is resolved).
     * The map is now built on first access via {@link #getConversionMap()},
     * which is only called from {@link #onServerTick} (well after registry
     * resolution).
     */
    private static volatile Map<Block, Block> CONVERSION_MAP;

    private static Map<Block, Block> getConversionMap() {
        Map<Block, Block> m = CONVERSION_MAP;
        if (m != null) return m;
        synchronized (TerrainSpiritifier.class) {
            if (CONVERSION_MAP == null) {
                CONVERSION_MAP = buildMap();
            }
            return CONVERSION_MAP;
        }
    }

    private static Map<Block, Block> buildMap() {
        Map<Block, Block> m = new java.util.HashMap<>();
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
        // Logs
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
        // Clay, mud
        m.put(Blocks.CLAY, ErgenverseBlocks.SPIRIT_DIRT.get());
        m.put(Blocks.PACKED_MUD, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        m.put(Blocks.MUD_BRICKS, ErgenverseBlocks.SPIRIT_STONE_BLOCK.get());
        return java.util.Collections.unmodifiableMap(m);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Throttle: only run every TICK_INTERVAL ticks.
        long ticks = event.getServer().overworld().getGameTime();
        if (ticks % TICK_INTERVAL != 0) return;

        // For each online player, convert the nearest unconverted chunk
        // within CHUNK_RADIUS of the player.
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.level().dimension() != Level.OVERWORLD) continue;
            ServerLevel level = (ServerLevel) player.level();
            int playerChunkX = player.chunkPosition().x;
            int playerChunkZ = player.chunkPosition().z;

            // Spiral outward from the player's chunk to find the nearest
            // unconverted chunk within CHUNK_RADIUS.
            chunkFound:
            for (int r = 0; r <= CHUNK_RADIUS; r++) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue; // ring only
                        int cx = playerChunkX + dx;
                        int cz = playerChunkZ + dz;
                        long key = net.minecraft.world.level.ChunkPos.asLong(cx, cz);
                        if (convertedChunks.contains(key)) continue;

                        // Convert this chunk.
                        convertChunk(level, cx, cz);
                        convertedChunks.add(key);
                        break chunkFound; // one chunk per player per tick
                    }
                }
            }
        }
    }

    /**
     * Convert all vanilla terrain blocks in a single chunk to spirit variants.
     */
    private static void convertChunk(ServerLevel level, int chunkX, int chunkZ) {
        // Ensure the chunk is loaded.
        LevelChunk chunk = level.getChunk(chunkX, chunkZ);
        if (chunk == null) return;

        int minSection = chunk.getMinSection();
        LevelChunkSection[] sections = chunk.getSections();
        int converted = 0;

        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (section == null || section.hasOnlyAir()) continue;
            int sectionY = minSection + i;
            int baseY = sectionY << 4;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        Block target = getConversionMap().get(state.getBlock());
                        if (target == null) continue;
                        BlockPos pos = new BlockPos(
                                chunk.getPos().getMinBlockX() + x,
                                baseY + y,
                                chunk.getPos().getMinBlockZ() + z);
                        BlockState newState = target.defaultBlockState();
                        // Flag 2 = UPDATE_CLIENTS (sync, no neighbor cascade)
                        level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
                        converted++;
                    }
                }
            }
        }

        if (converted > 0) {
            Ergenverse.LOGGER.debug("[Ergenverse] Spiritified chunk ({}, {}): {} blocks converted.",
                    chunkX, chunkZ, converted);
        }
    }

    /** Clear the converted-chunk cache (called on server stop). */
    public static void clearCache() {
        convertedChunks.clear();
    }
}
