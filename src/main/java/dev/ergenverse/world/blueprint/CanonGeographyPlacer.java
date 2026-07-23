package dev.ergenverse.world.blueprint;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.spawn.WangFamilyVillageBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CanonGeographyPlacer — places AUTHORED settlements at their fixed canonical
 * coordinates during chunk generation.
 *
 * <p><b>Core principle:</b> The chunk generator asks "What's supposed to exist
 * here?" instead of "Roll random noise." When a chunk loads that contains a
 * canonical settlement's footprint, this placer builds that settlement.
 *
 * <p>Every settlement in the World Blueprint has a FIXED (x, z) coordinate.
 * Wang Family Village is ALWAYS at (3842, -1184). Heng Yue Sect is ALWAYS at
 * (5400, -1900). Teng City is ALWAYS at (6800, -1000). Every playthrough.
 * Every seed. The stage is fixed.
 *
 * <p>Currently builds:
 * <ul>
 *   <li>Wang Family Village (via WangFamilyVillageBuilder — already implemented)</li>
 *   <li>Heng Yue Sect (via HengYueSectBuilder — full sect builder)</li>
 *   <li>Teng City (via TengFamilyCityBuilder — full city builder)</li>
 *   <li>Tian Shui City (via TianShuiCityBuilder — full city builder)</li>
 *   <li>Zhao Capital marker (placeholder)</li>
 *   <li>Canonical spirit vein stone markers at each spirit vein location</li>
 *   <li>Restriction zone boundary markers</li>
 * </ul>
 *
 * <p>Future: full structure builders for each settlement type, road paving,
 * river carving, mountain shaping. The blueprint data is the source of truth;
 * the builders just render it into Minecraft blocks.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CanonGeographyPlacer {

    private CanonGeographyPlacer() {}

    /** Tracks which settlements have already been built (idempotency). */
    private static final Set<String> BUILT_SETTLEMENTS = ConcurrentHashMap.newKeySet();

    /** Search radius for settlements when a chunk loads (blocks). */
    private static final int SETTLEMENT_BUILD_RADIUS = 200;

    /**
     * When a chunk loads, check if any canonical settlement is near and build
     * it if not already built. This is how authored geography enters the world.
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.isClientSide()) return;

        // Only run in the overworld (which is Planet Suzaku via our dimension override).
        if (!level.dimension().location().toString().equals("minecraft:overworld")) return;

        int chunkX = event.getChunk().getPos().x;
        int chunkZ = event.getChunk().getPos().z;
        int worldX = chunkX * 16 + 8;
        int worldZ = chunkZ * 16 + 8;

        // Find settlements near this chunk.
        List<com.google.gson.JsonObject> nearby =
                WorldBlueprintManager.getSettlementsNear(worldX, worldZ, SETTLEMENT_BUILD_RADIUS);

        for (com.google.gson.JsonObject settlement : nearby) {
            String id = settlement.get("id").getAsString();
            if (BUILT_SETTLEMENTS.contains(id)) continue;

            // Mark as building immediately to prevent duplicate triggers.
            BUILT_SETTLEMENTS.add(id);

            // Delay by 5 ticks to avoid recursive chunk loading.
            int delayTicks = 5;
            level.getServer().tell(new TickTask(
                    level.getServer().getTickCount() + delayTicks,
                    () -> buildSettlement(level, settlement)));
        }
    }

    /**
     * Builds a settlement at its canonical coordinate.
     * Dispatches to the appropriate builder based on type.
     */
    private static void buildSettlement(ServerLevel level, com.google.gson.JsonObject settlement) {
        String id = settlement.get("id").getAsString();
        String name = settlement.has("name") ? settlement.get("name").getAsString() : id;
        String type = settlement.has("type") ? settlement.get("type").getAsString() : "unknown";
        int x = settlement.get("x").getAsInt();
        int z = settlement.get("z").getAsInt();

        Ergenverse.LOGGER.info("[Ergenverse] Placing canonical settlement '{}' ({}) at ({}, ?) — type: {}",
                name, id, x, z, type);

        try {
            switch (id) {
                case "wang_family_village" -> {
                    // Use the existing detailed village builder.
                    if (!WangFamilyVillageBuilder.isAlreadyBuilt(level)) {
                        WangFamilyVillageBuilder.build(level);
                    }
                }
                case "heng_yue_sect" -> {
                    // Full hand-built Heng Yue Sect (恒岳派) — Wang Lin's first cultivation sect.
                    // Constitution: the world is completely hand-crafted, NOT a block-swap script.
                    // Every block placed intentionally in Java via HengYueSectBuilder.
                    int hengYueY = level.getHeightmapPos(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            new BlockPos(x, 0, z)).getY();
                    BlockPos center = new BlockPos(x, hengYueY, z);
                    if (!dev.ergenverse.spawn.HengYueSectBuilder.isAlreadyBuilt(level, center)) {
                        dev.ergenverse.spawn.HengYueSectBuilder.build(level, center);
                    }
                }
                case "teng_city" -> {
                    // Full hand-built Teng Family City (滕城) — largest city in Zhao Country.
                    // Constitution: the world is completely hand-crafted, NOT a block-swap script.
                    // Every block placed intentionally in Java via TengFamilyCityBuilder.
                    int tengY = level.getHeightmapPos(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            new BlockPos(x, 0, z)).getY();
                    BlockPos tengCenter = new BlockPos(x, tengY, z);
                    if (!dev.ergenverse.spawn.TengFamilyCityBuilder.isAlreadyBuilt(level, tengCenter)) {
                        dev.ergenverse.spawn.TengFamilyCityBuilder.build(level, tengCenter);
                    }
                }
                case "tian_shui_city" -> {
                    // Full hand-built Tian Shui City (天水城) — greatest trade hub in Zhao Country.
                    // Constitution: the world is completely hand-crafted, NOT a block-swap script.
                    // Every block placed intentionally in Java via TianShuiCityBuilder.
                    int tsY = level.getHeightmapPos(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            new BlockPos(x, 0, z)).getY();
                    BlockPos tsCenter = new BlockPos(x, tsY, z);
                    if (!dev.ergenverse.spawn.TianShuiCityBuilder.isAlreadyBuilt(level, tsCenter)) {
                        dev.ergenverse.spawn.TianShuiCityBuilder.build(level, tsCenter);
                    }
                }
                case "soul_refining_sect" -> {
                    // Full hand-built Soul Refining Sect (炼魂宗) — dark, sinister sect
                    // specializing in soul refinement. Canon: one of the most dangerous
                    // locations in Zhao Country. Constitution: every block hand-authored.
                    int srY = level.getHeightmapPos(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            new BlockPos(x, 0, z)).getY();
                    BlockPos srCenter = new BlockPos(x, srY, z);
                    if (!dev.ergenverse.spawn.SoulRefiningSectBuilder.isAlreadyBuilt(level, srCenter)) {
                        dev.ergenverse.spawn.SoulRefiningSectBuilder.build(level, srCenter);
                    }
                }
                case "zhao_capital" -> buildZhaoCapitalMarker(level, x, z, settlement);
                default -> buildSettlementMarker(level, x, z, settlement);
            }
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[Ergenverse] Failed to build settlement '{}': {}", id, e.getMessage(), e);
        }
    }

    // ── Settlement Markers ────────────────────────────────────────────
    // These are PLACEHOLDER structures. Full builders come later.
    // The marker establishes the canonical location with a visible landmark
    // so the player (and developer) can verify the blueprint is working.

    // NOTE: buildHengYueSectMarker is now dead code — Heng Yue Sect uses the full builder.
    // Retained for reference. Do NOT call from new code.
    @SuppressWarnings("unused")
    private static void buildHengYueSectMarker(ServerLevel level, int x, int z, com.google.gson.JsonObject s) {
        int y = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(x, 0, z)).getY();

        // Spirit stone platform with formation core stone marker.
        var spiritStone = dev.ergenverse.block.ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
        var coreStone = dev.ergenverse.block.ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState();
        var spiritVeinStone = dev.ergenverse.block.ErgenverseBlocks.SPIRIT_VEIN_STONE.get().defaultBlockState();

        // 5x5 platform
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlock(new BlockPos(x + dx, y - 1, z + dz), spiritStone, 2);
            }
        }
        // Central formation core stone (the sect's foundation marker)
        level.setBlock(new BlockPos(x, y, z), coreStone, 2);
        // Four corner spirit vein stones (marking the sect's spirit vein boundary)
        level.setBlock(new BlockPos(x - 2, y, z - 2), spiritVeinStone, 2);
        level.setBlock(new BlockPos(x + 2, y, z - 2), spiritVeinStone, 2);
        level.setBlock(new BlockPos(x - 2, y, z + 2), spiritVeinStone, 2);
        level.setBlock(new BlockPos(x + 2, y, z + 2), spiritVeinStone, 2);

        Ergenverse.LOGGER.info("[Ergenverse] Heng Yue Sect marker placed at ({}, {}, {}) — full sect builder TODO.",
                x, y, z);
    }

    private static void buildTengCityMarker(ServerLevel level, int x, int z, com.google.gson.JsonObject s) {
        int y = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(x, 0, z)).getY();

        var spiritStone = dev.ergenverse.block.ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
        var jadeStone = dev.ergenverse.block.ErgenverseBlocks.JADE_STONE.get().defaultBlockState();

        // 7x7 jade-stone platform (cities are grander than sects)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setBlock(new BlockPos(x + dx, y - 1, z + dz), spiritStone, 2);
            }
        }
        // Jade stone corners
        level.setBlock(new BlockPos(x - 3, y, z - 3), jadeStone, 2);
        level.setBlock(new BlockPos(x + 3, y, z - 3), jadeStone, 2);
        level.setBlock(new BlockPos(x - 3, y, z + 3), jadeStone, 2);
        level.setBlock(new BlockPos(x + 3, y, z + 3), jadeStone, 2);

        Ergenverse.LOGGER.info("[Ergenverse] Teng City marker placed at ({}, {}, {}) — full city builder TODO.",
                x, y, z);
    }

    private static void buildZhaoCapitalMarker(ServerLevel level, int x, int z, com.google.gson.JsonObject s) {
        int y = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(x, 0, z)).getY();

        var bloodStone = dev.ergenverse.block.ErgenverseBlocks.BLOOD_STONE.get().defaultBlockState();
        var spiritStone = dev.ergenverse.block.ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();

        // 9x9 platform (capitals are grandest)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                level.setBlock(new BlockPos(x + dx, y - 1, z + dz), spiritStone, 2);
            }
        }
        // Blood stone center (royal marker)
        level.setBlock(new BlockPos(x, y, z), bloodStone, 2);

        Ergenverse.LOGGER.info("[Ergenverse] Zhao Capital marker placed at ({}, {}, {}) — full capital builder TODO.",
                x, y, z);
    }

    private static void buildSettlementMarker(ServerLevel level, int x, int z, com.google.gson.JsonObject s) {
        int y = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(x, 0, z)).getY();

        var spiritStone = dev.ergenverse.block.ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().defaultBlockState();
        level.setBlock(new BlockPos(x, y - 1, z), spiritStone, 2);

        String name = s.has("name") ? s.get("name").getAsString() : s.get("id").getAsString();
        Ergenverse.LOGGER.info("[Ergenverse] Settlement '{}' marker placed at ({}, {}, {}).", name, x, y, z);
    }

    /** Resets the built-settlements tracking (for testing). */
    public static void reset() {
        BUILT_SETTLEMENTS.clear();
    }
}
