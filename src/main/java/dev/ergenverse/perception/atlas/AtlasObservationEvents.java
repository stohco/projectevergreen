package dev.ergenverse.perception.atlas;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.simulation.opportunity.PlayerObserverRealm;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * AtlasObservationEvents — server-side auto-observation engine for the Divine Sense Atlas.
 *
 * <p>This class is the bridge between the world and the player's perception
 * record. It listens to three Forge events:
 *
 * <ol>
 *   <li><b>{@link PlayerEvent.PlayerLoggedInEvent}</b> — on login, sync the
 *       player's atlas to their client. If they've never played before,
 *       their atlas is empty; the first sync is a no-op payload that
 *       initializes the client cache.</li>
 *
 *   <li><b>{@code TickEvent.PlayerTickEvent}</b> (handled via the per-player
 *       tick gate) — every {@link #OBSERVATION_INTERVAL_TICKS} ticks, scan
 *       the player's nearby blocks for notable features and add
 *       observations for new ones. The scan is tier-limited:
 *       <ul>
 *           <li>Mortals observe village structures (doors, beds, villagers
 *               nearby, named locations).</li>
 *           <li>Foundation+ also observes spirit veins (nether quartz ore,
 *               calcite per {@link dev.ergenverse.perception.AmbientPerception#SPIRIT_VEIN_RANKS}).</li>
 *           <li>Nascent Soul+ also observes formations (TODO: future
 *               formation blocks).</li>
 *           <li>Ascendant+ also observes law-layer features (TODO: future
 *               karmic nodes / space cracks).</li>
 *       </ul>
 *       The scan uses {@code player.level().getBlockState()} only — it
 *       does NOT query world generation or structures. TODO comments
 *       mark the integration points for future worldgen hooks.</li>
 *
 *   <li><b>{@link PlayerInteractEvent.RightClickBlock}</b> — if the player
 *       right-clicks a block while sneaking with an empty main hand, add
 *       a manual observation for that block's position. The player can
 *       thus annotate landmarks ("Wang Lin's cave entrance") that the
 *       auto-observer wouldn't catch.</li>
 * </ol>
 *
 * <p>After every observation, we sync the atlas to the client (so the
 * new entry shows up immediately in the M-key UI).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AtlasObservationEvents {

    private AtlasObservationEvents() {}

    /** Auto-observation cadence: every 100 ticks (5 seconds). */
    public static final int OBSERVATION_INTERVAL_TICKS = 100;

    /** Horizontal scan radius around the player for the auto-observer. */
    public static final int SCAN_RADIUS = 24;

    /** Per-player tick gate so we don't observe every tick. */
    private static final Map<java.util.UUID, Long> LAST_OBSERVATION_TICK = new HashMap<>();

    /** Recently-observed positions (per-player) to avoid re-adding the same entry. */
    private static final Map<java.util.UUID, Set<BlockPos>> RECENT_OBSERVATIONS = new HashMap<>();

    // ─── Player login: initial sync ──────────────────────────────────

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        DivineSenseAtlas atlas = AtlasCapability.getOrNull(player);
        if (atlas == null) {
            Ergenverse.LOGGER.warn("[Ergenverse] Atlas sync skipped for {} — no capability attached.",
                    player.getName().getString());
            return;
        }
        Ergenverse.LOGGER.info("[Ergenverse] Syncing Divine Sense Atlas for {} on login ({} entries).",
                player.getName().getString(), atlas.size());
        AtlasNetworkPackets.sendToClient(player, atlas);
    }

    // ─── Player tick: periodic auto-observation ─────────────────────

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        Level level = player.level();
        if (level.isClientSide) return;

        long tick = level.getGameTime();
        Long last = LAST_OBSERVATION_TICK.get(player.getUUID());
        if (last != null && (tick - last) < OBSERVATION_INTERVAL_TICKS) return;
        LAST_OBSERVATION_TICK.put(player.getUUID(), tick);

        DivineSenseAtlas atlas = AtlasCapability.getOrNull(player);
        if (atlas == null) return;

        PlayerObserverRealm tier = atlas.getCurrentTier(player);
        boolean changed = scanNearbyBlocks(player, atlas, tier);

        if (changed) {
            // Sync the new observations to the client.
            AtlasNetworkPackets.sendToClient(player, atlas);
        }
    }

    /**
     * Scan a radius of blocks around the player. Add observations for any
     * notable block we haven't already recorded.
     *
     * <p>The scan is intentionally lightweight — only checks block states
     * at integer offsets. It does NOT call world-generation queries, chunk
     * structure scans, or feature lookups. Those integrations are TODOs
     * for future subagents.
     *
     * @return true if any new observation was added (so the caller can sync)
     */
    private static boolean scanNearbyBlocks(ServerPlayer player, DivineSenseAtlas atlas,
                                            PlayerObserverRealm tier) {
        Level level = player.level();
        BlockPos origin = player.blockPosition();
        boolean changed = false;
        Set<BlockPos> recent = RECENT_OBSERVATIONS.computeIfAbsent(player.getUUID(), k -> new HashSet<>());

        // Sample scan: instead of every block in a 48³ cube (110k+ blocks),
        // we sample on a 4-block stride (24³ = ~13k blocks → manageable).
        // Future optimization: spatial index, structure-bounding-box queries.
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx += 4) {
            for (int dy = -8; dy <= 8; dy += 4) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz += 4) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (recent.contains(pos)) continue;

                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    Block block = state.getBlock();
                    ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
                    if (blockId == null) continue;
                    String blockIdStr = blockId.toString();

                    // ── Physical-layer observations (all tiers) ──
                    AtlasEntry entry = classifyPhysicalBlock(pos, block, blockIdStr, level.getGameTime());
                    if (entry != null) {
                        atlas.observe(player, pos, entry);
                        recent.add(pos);
                        changed = true;
                        continue;
                    }

                    // ── Qi-layer observations (Foundation+) ──
                    // TODO: integrate with future spirit_vein block — for now, we
                    // detect vanilla spirit-vein stand-ins (nether quartz ore, calcite)
                    // via the AmbientPerception registry. Future subagent: add a real
                    // SpiritVeinBlock and detect it here.
                    if (tier.isAtLeast(PlayerObserverRealm.FOUNDATION)) {
                        entry = classifyQiBlock(pos, block, blockIdStr, level.getGameTime());
                        if (entry != null) {
                            atlas.observe(player, pos, entry);
                            recent.add(pos);
                            changed = true;
                            continue;
                        }
                    }

                    // ── Restriction-layer observations (Nascent Soul+) ──
                    // TODO: integrate with future formation/restriction blocks.
                    if (tier.isAtLeast(PlayerObserverRealm.NASCENT_SOUL)) {
                        entry = classifyRestrictionBlock(pos, block, blockIdStr, level.getGameTime());
                        if (entry != null) {
                            atlas.observe(player, pos, entry);
                            recent.add(pos);
                            changed = true;
                            continue;
                        }
                    }

                    // ── Law-layer observations (Ascendant+) ──
                    // TODO: integrate with future karmic_node / space_crack blocks.
                    if (tier.isAtLeast(PlayerObserverRealm.ASCENDANT_PLUS)) {
                        entry = classifyLawBlock(pos, block, blockIdStr, level.getGameTime());
                        if (entry != null) {
                            atlas.observe(player, pos, entry);
                            recent.add(pos);
                            changed = true;
                        }
                    }
                }
            }
        }

        // Cap the recent-observations cache so it doesn't grow unbounded.
        if (recent.size() > 2048) {
            recent.clear(); // simple policy: flush and let re-scan re-add.
        }
        return changed;
    }

    // ─── Block classifiers (per layer) ──────────────────────────────

    private static AtlasEntry classifyPhysicalBlock(BlockPos pos, Block block, String blockIdStr, long tick) {
        // Villages: doors and beds are the canonical "village" indicator
        if (block == Blocks.OAK_DOOR || block == Blocks.SPRUCE_DOOR || block == Blocks.BIRCH_DOOR
                || block == Blocks.ACACIA_DOOR || block == Blocks.JUNGLE_DOOR || block == Blocks.DARK_OAK_DOOR
                || block == Blocks.MANGROVE_DOOR || block == Blocks.CRIMSON_DOOR || block == Blocks.WARPED_DOOR) {
            return new AtlasEntry(pos, "Doorway", AtlasEntry.CAT_VILLAGE,
                    AtlasEntry.LAYER_PHYSICAL, "A doorway — possible village structure.",
                    tick, AtlasEntry.VIA_DIRECT_SIGHT);
        }
        if (block == Blocks.RED_BED || block == Blocks.WHITE_BED || block == Blocks.YELLOW_BED
                || block == Blocks.BLUE_BED || block == Blocks.GREEN_BED || block == Blocks.BLACK_BED
                || block == Blocks.PURPLE_BED || block == Blocks.PINK_BED || block == Blocks.CYAN_BED
                || block == Blocks.ORANGE_BED || block == Blocks.LIGHT_BLUE_BED
                || block == Blocks.LIGHT_GRAY_BED || block == Blocks.GRAY_BED
                || block == Blocks.LIME_BED || block == Blocks.MAGENTA_BED
                || block == Blocks.BROWN_BED) {
            return new AtlasEntry(pos, "Bed", AtlasEntry.CAT_VILLAGE,
                    AtlasEntry.LAYER_PHYSICAL, "A bed — signs of habitation.",
                    tick, AtlasEntry.VIA_DIRECT_SIGHT);
        }
        // Roads: cobblestone / dirt path / gravel patterns
        if (block == Blocks.COBBLESTONE || block == Blocks.MOSSY_COBBLESTONE
                || block == Blocks.DIRT_PATH || block == Blocks.GRAVEL
                || block == Blocks.STONE_BRICKS || block == Blocks.SMOOTH_STONE_SLAB) {
            return new AtlasEntry(pos, "Road Surface", AtlasEntry.CAT_ROAD,
                    AtlasEntry.LAYER_PHYSICAL, "A constructed road surface.",
                    tick, AtlasEntry.VIA_DIRECT_SIGHT);
        }
        // Rivers: water at low Y (overworld)
        if (block == Blocks.WATER) {
            return new AtlasEntry(pos, "Water", AtlasEntry.CAT_RIVER,
                    AtlasEntry.LAYER_PHYSICAL, "Open water — possibly a river or pond.",
                    tick, AtlasEntry.VIA_DIRECT_SIGHT);
        }
        // Mountains: stone / andesite / diorite / granite at high Y
        if ((block == Blocks.STONE || block == Blocks.ANDESITE || block == Blocks.DIORITE
                || block == Blocks.GRANITE || block == Blocks.COBBLESTONE)
                && pos.getY() >= 100) {
            return new AtlasEntry(pos, "Mountain Stone", AtlasEntry.CAT_MOUNTAIN,
                    AtlasEntry.LAYER_PHYSICAL, "Exposed stone at high altitude — mountain terrain.",
                    tick, AtlasEntry.VIA_DIRECT_SIGHT);
        }
        return null;
    }

    private static AtlasEntry classifyQiBlock(BlockPos pos, Block block, String blockIdStr, long tick) {
        // Spirit veins (vanilla stand-ins per AmbientPerception)
        Integer veinRank = dev.ergenverse.perception.AmbientPerception.SPIRIT_VEIN_RANKS.get(blockIdStr);
        if (veinRank != null) {
            return new AtlasEntry(pos, "Spirit Vein (Rank " + veinRank + ")",
                    AtlasEntry.CAT_SPIRIT_VEIN, AtlasEntry.LAYER_QI,
                    "Qi-rich mineral deposit. Meditation near here accelerates cultivation.",
                    tick, AtlasEntry.VIA_DIVINE_SENSE);
        }
        // Spirit herbs (vanilla stand-ins per AmbientPerception)
        var herb = dev.ergenverse.perception.AmbientPerception.SPIRIT_HERBS.get(blockIdStr);
        if (herb != null) {
            return new AtlasEntry(pos, herb.trueName + " Grove",
                    AtlasEntry.CAT_HERB_GROVE, AtlasEntry.LAYER_QI,
                    "Grade " + herb.grade + " spirit herb growing here.",
                    tick, AtlasEntry.VIA_DIVINE_SENSE);
        }
        // TODO: integrate with future SpiritVeinBlock / SpiritHerbBlock (Ergenverse blocks).
        return null;
    }

    private static AtlasEntry classifyRestrictionBlock(BlockPos pos, Block block, String blockIdStr, long tick) {
        // TODO: integrate with future FormationAnchorBlock / RestrictionSealBlock.
        // For now, no restriction-layer blocks exist in the world. The classifier
        // is here as the integration point — a future subagent will add blocks and
        // wire them here.
        return null;
    }

    private static AtlasEntry classifyLawBlock(BlockPos pos, Block block, String blockIdStr, long tick) {
        // TODO: integrate with future KarmicNodeBlock / SpaceCrackBlock.
        // For now, no law-layer blocks exist. A future subagent will add these
        // and wire them here.
        return null;
    }

    // ─── Manual observation: sneak-right-click with empty hand ──────

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.isCanceled()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!player.isShiftKeyDown()) return;

        // Must be empty-handed to avoid clobbering real block-use interactions.
        if (!player.getMainHandItem().isEmpty()) return;

        BlockPos pos = event.getPos();
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        String blockIdStr = blockId == null ? "unknown" : blockId.toString();

        DivineSenseAtlas atlas = AtlasCapability.getOrNull(player);
        if (atlas == null) {
            Ergenverse.LOGGER.warn("[Ergenverse] Atlas manual observation failed for {} — no capability.",
                    player.getName().getString());
            return;
        }

        PlayerObserverRealm tier = atlas.getCurrentTier(player);
        // Manual observations land on the Physical layer (the player is looking
        // at a physical block). If the block is also a Qi-layer feature (e.g.
        // spirit vein), we add a Qi-layer entry too so both layers show the
        // player's annotation.
        String label = "Player Marker @ " + pos.getX() + "," + pos.getY() + "," + pos.getZ();
        String note = "Manually recorded at " + blockIdStr;
        AtlasEntry entry = new AtlasEntry(pos, label, AtlasEntry.CAT_LANDMARK,
                AtlasEntry.LAYER_PHYSICAL, note,
                level.getGameTime(), AtlasEntry.VIA_JOURNAL);
        atlas.observe(player, pos, entry);

        // If the block is a known Qi-layer feature, add a Qi annotation too.
        if (tier.isAtLeast(PlayerObserverRealm.FOUNDATION)) {
            Integer veinRank = dev.ergenverse.perception.AmbientPerception.SPIRIT_VEIN_RANKS.get(blockIdStr);
            if (veinRank != null) {
                AtlasEntry qi = new AtlasEntry(pos, "Player-Marked Spirit Vein (Rank " + veinRank + ")",
                        AtlasEntry.CAT_SPIRIT_VEIN, AtlasEntry.LAYER_QI, note,
                        level.getGameTime(), AtlasEntry.VIA_JOURNAL);
                atlas.observe(player, pos, qi);
            }
        }

        AtlasNetworkPackets.sendToClient(player, atlas);

        player.sendSystemMessage(Component.literal(
                "\u00A7a[Atlas] Recorded observation at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()
                        + "\u00A7r  (\u00A77" + blockIdStr + "\u00A7r)"));
    }
}
