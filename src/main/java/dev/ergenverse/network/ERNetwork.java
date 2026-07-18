package dev.ergenverse.network;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;


/**
 * Central network channel for the Ergenverse mod.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 uses {@link SimpleChannel} for typed
 * bidirectional messaging. Protocol versioning ensures client/server
 * handshake fails fast on mismatch.
 *
 * <p>Packet registration happens in {@link #register()} during
 * {@code FMLCommonSetupEvent::enqueueWork} — safe to call from the
 * main thread's deferred work queue.
 */
public final class ERNetwork {

    private ERNetwork() {}

    /** Increment on any packet schema change. */
    public static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Ergenverse.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    /** Register all packets. Called once during FMLCommonSetup. */
    public static void register() {
        // ── S2C: Server → Client ──────────────────────────────────

        // 0: CultivationState full sync (realm, qi, karma, etc.)
        CHANNEL.registerMessage(
                nextId(),
                CultivationSyncS2CPacket.class,
                CultivationSyncS2CPacket::encode,
                CultivationSyncS2CPacket::new,
                CultivationSyncS2CPacket::handle
        );

        // 1: Meditation state toggle (start/stop visual feedback)
        CHANNEL.registerMessage(
                nextId(),
                MeditationStateS2CPacket.class,
                MeditationStateS2CPacket::encode,
                MeditationStateS2CPacket::new,
                MeditationStateS2CPacket::handle
        );

        // 2: Tribulation bolt visual (client-side VFX only)
        CHANNEL.registerMessage(
                nextId(),
                TribulationBoltS2CPacket.class,
                TribulationBoltS2CPacket::encode,
                TribulationBoltS2CPacket::new,
                TribulationBoltS2CPacket::handle
        );

        // ── C2S: Client → Server ──────────────────────────────────

        // 3: Breakthrough request (keybind-triggered)
        CHANNEL.registerMessage(
                nextId(),
                BreakthroughRequestC2SPacket.class,
                BreakthroughRequestC2SPacket::encode,
                BreakthroughRequestC2SPacket::new,
                BreakthroughRequestC2SPacket::handle
        );

        // 4: Toggle meditation (sneak-right-click)
        CHANNEL.registerMessage(
                nextId(),
                ToggleMeditationC2SPacket.class,
                ToggleMeditationC2SPacket::encode,
                ToggleMeditationC2SPacket::new,
                ToggleMeditationC2SPacket::handle
        );

        // 5: Divine sense pulse request (C2S)
        CHANNEL.registerMessage(
                nextId(),
                DivineSensePulseC2SPacket.class,
                DivineSensePulseC2SPacket::encode,
                DivineSensePulseC2SPacket::new,
                DivineSensePulseC2SPacket::handle
        );

        // 6: Divine sense pulse result (S2C)
        CHANNEL.registerMessage(
                nextId(),
                DivineSenseResultS2CPacket.class,
                DivineSenseResultS2CPacket::encode,
                DivineSenseResultS2CPacket::new,
                DivineSenseResultS2CPacket::handle
        );

        // 7: Perception sync (S2C) — periodic entity perception data
        CHANNEL.registerMessage(
                nextId(),
                PerceptionSyncS2CPacket.class,
                PerceptionSyncS2CPacket::encode,
                PerceptionSyncS2CPacket::new,
                PerceptionSyncS2CPacket::handle
        );

        Ergenverse.LOGGER.info("[Ergenverse] Network channel registered ({} packets, protocol v{})",
                packetId, PROTOCOL_VERSION);
    }

    private static int nextId() {
        return packetId++;
    }

    public static SimpleChannel getChannel() {
        return CHANNEL;
    }
}