package dev.ergenverse.cultivation;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.network.ERNetwork;
import dev.ergenverse.network.MeditationStateS2CPacket;
import dev.ergenverse.network.CultivationSyncS2CPacket;
import dev.ergenverse.simulation.SpatialBiomeCacheIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Qi Gathering via Meditation at Spirit Veins.
 *
 * <p>Piece 1 of the Cultivation Vertical Slice. When a player
 * sneak-right-clicks a spirit vein block (quartz_ore), they
 * enter meditation. While meditating:
 * <ul>
 *   <li>Movement is locked (Slowness V)</li>
 *   <li>Qi accumulates based on the vein grade (from location biome)</li>
 *   <li>Breakthrough progress slowly increases</li>
 *   <li>Drifting too far or taking damage breaks meditation</li>
 * </ul>
 *
 * <p>Mortals CAN meditate. This is how Wang Lin began — sitting
 * before a spirit vein in the Heng Yue Sect's outer mountains.
 */
public final class MeditationHandler {

    private MeditationHandler() {}

    /** Spirit vein blocks — calcite is our stand-in for spirit veins. */
    private static final Set<Block> SPIRIT_VEIN_BLOCKS = new java.util.HashSet<>();

    static {
        SPIRIT_VEIN_BLOCKS.add(Blocks.NETHER_QUARTZ_ORE);
        SPIRIT_VEIN_BLOCKS.add(Blocks.CALCITE);
        // Custom herb blocks provide minor Qi (canon: meditating near spirit herbs is beneficial)
        dev.ergenverse.block.ErgenverseBlocks.BLOCK_ITEMS.getEntries().stream()
                .map(ro -> ro.get())
                .filter(item -> item instanceof net.minecraft.world.item.BlockItem)
                .forEach(item -> SPIRIT_VEIN_BLOCKS.add(((net.minecraft.world.item.BlockItem) item).getBlock()));
    }

    /** Active meditation sessions: player UUID to session. */
    private static final Map<UUID, MeditationSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * Qi-per-tick (normalized 0-1) by vein grade. The absolute Qi added
     * each tick is this value times the player's maxQi.
     */
    private static final double[] QI_PER_TICK_BY_GRADE = {
            0.0002,  // Grade 0: barren (unused — veins start at grade 1)
            0.0005,  // Grade 1: faint vein (wilderness)
            0.001,   // Grade 2: minor vein (village/country)
            0.002,   // Grade 3: moderate vein (sect territory)
            0.004,   // Grade 4: major vein (blessed land / noble family)
            0.008    // Grade 5: ancestral vein (Vermilion Bird Divine Sect)
    };

    /** Breakthrough progress per tick while meditating. Very slow —
     *  canon meditation takes months to years. */
    private static final double BREAKTHROUGH_PROGRESS_PER_TICK = 0.00002;

    /**
     * Toggle meditation at the target block. Called from ToggleMeditationC2SPacket handler.
     */
    public static void toggleMeditation(ServerPlayer player, BlockPos targetPos) {
        UUID uuid = player.getUUID();
        if (activeSessions.containsKey(uuid)) {
            stopMeditation(player, "You open your eyes. The flow of Qi fades.");
            return;
        }

        // Validate: target must be a spirit vein block
        if (!SPIRIT_VEIN_BLOCKS.contains(player.level().getBlockState(targetPos).getBlock())) {
            player.sendSystemMessage(Component.literal(
                    "\u00A77This rock holds no spiritual energy."));
            return;
        }

        // Validate: player must be within 5 blocks
        if (player.blockPosition().distSqr(targetPos) > 25) {
            player.sendSystemMessage(Component.literal(
                    "\u00A77Too far from the spirit vein."));
            return;
        }

        // Determine vein grade from location
        int grade = determineVeinGrade(player, targetPos);
        MeditationSession session = new MeditationSession(targetPos, grade, player.level().getGameTime());
        activeSessions.put(uuid, session);

        // Lock movement and close eyes
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 99999, 4, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 99999, 0, false, false, true));

        // Notify client of meditation state
        ERNetwork.getChannel().send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new MeditationStateS2CPacket(true)
        );

        player.sendSystemMessage(Component.literal(
                String.format("\u00A7aYou close your eyes and sink into meditation...\u00A77 (Vein Grade %d)", grade + 1)));
        player.sendSystemMessage(Component.literal(
                "\u00A78Qi flows into your meridians like a gentle stream. Sneak-right-click again or move to break focus."));

        Ergenverse.LOGGER.debug("[Ergenverse] {} begins meditating at {} (grade {})",
                player.getName().getString(), targetPos.toShortString(), grade);
    }

    /**
     * Stop meditation for a player. Called from toggle, damage, or movement events.
     */
    public static void stopMeditation(ServerPlayer player, String reason) {
        UUID uuid = player.getUUID();
        MeditationSession session = activeSessions.remove(uuid);
        if (session == null) return;

        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.BLINDNESS);

        ERNetwork.getChannel().send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new MeditationStateS2CPacket(false)
        );

        player.sendSystemMessage(Component.literal("\u00A77" + reason));
        Ergenverse.LOGGER.debug("[Ergenverse] {} stops meditating: {}",
                player.getName().getString(), reason);
    }

    /**
     * Tick active meditation for a player. Called from CultivationEvents.onPlayerTick.
     * This is where Qi actually accumulates.
     */
    public static void tickMeditation(ServerPlayer player) {
        UUID uuid = player.getUUID();
        MeditationSession session = activeSessions.get(uuid);
        if (session == null) return;

        CultivationState state = CultivationCapability.get(player).orElse(null);
        if (state == null) return;

        // Base qi rate from vein grade
        double qiPerTick = QI_PER_TICK_BY_GRADE[session.veinGrade];

        // Mortals get half rate — untrained meridians
        if (state.getCurrentRealm() == RealmId.MORTAL) {
            qiPerTick *= 0.5;
        }
        // Qi Condensation: higher layers absorb more efficiently
        if (state.getCurrentRealm() == RealmId.QI_CONDENSATION) {
            qiPerTick *= (1.0 + state.getQi() * 0.5);
        }

        state.addQi(qiPerTick * state.getMaxQi());

        // Breakthrough progress: meditation contributes slowly, only when Qi is sufficient
        if (state.getQi() >= 0.80) {
            state.addBreakthroughProgress(BREAKTHROUGH_PROGRESS_PER_TICK);
        }

        // Validate: player must stay within 5 blocks of the vein
        if (player.blockPosition().distSqr(session.veinPos) > 25) {
            stopMeditation(player, "You drifted too far from the spirit vein. Meditation broken.");
            return;
        }

        // Validate: vein block must still exist
        if (!SPIRIT_VEIN_BLOCKS.contains(player.level().getBlockState(session.veinPos).getBlock())) {
            stopMeditation(player, "The spirit vein's energy has been disrupted!");
            return;
        }

        // Periodic sync to client (every 2 seconds)
        session.tickCount++;
        if (session.tickCount % 40 == 0) {
            syncToClient(player);
        }
    }

    /** Whether a player is currently meditating. */
    public static boolean isMeditating(UUID playerId) {
        return activeSessions.containsKey(playerId);
    }

    /** Sync full cultivation state to the client. */
    public static void syncToClient(ServerPlayer player) {
        CultivationCapability.get(player).ifPresent(state -> {
            boolean meditating = activeSessions.containsKey(player.getUUID());
            ERNetwork.getChannel().send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new CultivationSyncS2CPacket(state, meditating)
            );
        });
    }

    /**
     * Determine the vein grade from the player's location.
     * Uses the biome→location mapping from SpatialBiomeCacheIndex.
     */
    private static int determineVeinGrade(ServerPlayer player, BlockPos pos) {
        try {
            String locationId = SpatialBiomeCacheIndex.getInstance()
                    .mapBiomeToLocationId(
                            player.level().getBiome(pos).unwrapKey()
                                    .map(k -> k.location().getPath())
                                    .orElse("plains"));

            // Known high-grade locations
            if (locationId.contains("heng_yue") || locationId.contains("heavenly_fate")) return 3;
            if (locationId.contains("soul_refining") || locationId.contains("teng")) return 4;
            if (locationId.contains("vermilion_bird")) return 5;
            if (locationId.contains("zhao")) return 2;
            // Default: faint vein
        } catch (Exception e) {
            Ergenverse.LOGGER.debug("[Ergenverse] Could not determine vein grade: {}", e.getMessage());
        }
        return 1;
    }

    /** Per-player meditation session. */
    private static final class MeditationSession {
        final BlockPos veinPos;
        final int veinGrade;
        final long startTick;
        int tickCount;

        MeditationSession(BlockPos veinPos, int veinGrade, long startTick) {
            this.veinPos = veinPos;
            this.veinGrade = veinGrade;
            this.startTick = startTick;
            this.tickCount = 0;
        }
    }
}