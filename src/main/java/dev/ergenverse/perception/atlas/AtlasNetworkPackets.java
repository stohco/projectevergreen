package dev.ergenverse.perception.atlas;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.network.ERNetwork;
import dev.ergenverse.simulation.opportunity.PlayerObserverRealm;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Atlas network packets — client/server sync for the Divine Sense Atlas.
 *
 * <p>Two packets live in this file (kept together for cohesion; both
 * registered in {@link ERNetwork} as IDs 26 and 27):
 * <ul>
 *   <li>{@link AtlasSyncS2CPacket} — server → client. Sends the player's
 *       full atlas state on login and on each new observation. The
 *       payload is already tier-filtered server-side: a mortal never
 *       receives Qi-layer entries. This keeps the wire small and
 *       prevents leaking knowledge the player hasn't earned.</li>
 *   <li>{@link AtlasRequestC2SPacket} — client → server. Lets the
 *       player request a refresh (e.g. after realm breakthrough, the
 *       client may suspect the server hasn't synced yet). The server
 *       replies with a fresh {@link AtlasSyncS2CPacket}.</li>
 * </ul>
 *
 * <h2>Packet IDs (assigned in {@link ERNetwork})</h2>
 * <ul>
 *   <li>26 — {@link AtlasSyncS2CPacket}</li>
 *   <li>27 — {@link AtlasRequestC2SPacket}</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class AtlasNetworkPackets {

    private AtlasNetworkPackets() {}

    // ════════════════════════════════════════════════════════════════
    //  S2C: server → client — full atlas sync
    // ════════════════════════════════════════════════════════════════

    /**
     * Server → Client: full atlas sync. Sends all entries the player
     * can perceive at their current tier, plus the canonical rumor IDs.
     *
     * <p>Sent on:
     * <ul>
     *   <li>Player login (see {@code AtlasObservationEvents}).</li>
     *   <li>Each new observation added by {@code AtlasObservationEvents}.</li>
     *   <li>Player realm change (so newly-visible layers sync). TODO: wire this.</li>
     *   <li>Client request via {@link AtlasRequestC2SPacket}.</li>
     * </ul>
     */
    public static final class AtlasSyncS2CPacket {

        public final int tierOrder;                 // PlayerObserverRealm.order
        public final String tierLabel;              // PlayerObserverRealm.label
        public final List<AtlasEntry> entries;      // tier-filtered
        public final List<String> rumorIds;

        public AtlasSyncS2CPacket(PlayerObserverRealm tier,
                                  List<AtlasEntry> entries,
                                  List<String> rumorIds) {
            this.tierOrder = tier == null ? 0 : tier.order;
            this.tierLabel = tier == null ? "Mortal" : tier.label;
            this.entries = entries == null ? new ArrayList<>() : new ArrayList<>(entries);
            this.rumorIds = rumorIds == null ? new ArrayList<>() : new ArrayList<>(rumorIds);
        }

        public AtlasSyncS2CPacket(FriendlyByteBuf buf) {
            this.tierOrder = buf.readVarInt();
            this.tierLabel = buf.readUtf(64);
            int n = buf.readVarInt();
            this.entries = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                this.entries.add(AtlasEntry.read(buf));
            }
            int r = buf.readVarInt();
            this.rumorIds = new ArrayList<>(r);
            for (int i = 0; i < r; i++) {
                this.rumorIds.add(buf.readUtf(128));
            }
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(tierOrder);
            buf.writeUtf(tierLabel, 64);
            buf.writeVarInt(entries.size());
            for (AtlasEntry e : entries) e.write(buf);
            buf.writeVarInt(rumorIds.size());
            for (String r : rumorIds) buf.writeUtf(r, 128);
        }

        public boolean handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                // Client-side handler: stash the snapshot for the atlas screen to render.
                // Route through AtlasClientEvents (per PA-5 spec, it owns the cached
                // atlas state). JVM lazy class loading means AtlasClientEvents is only
                // loaded when this handle() method is invoked — and Forge only invokes
                // S2C handle() on the receiving client. The dedicated server never
                // loads AtlasClientEvents, so its client-only Minecraft references are
                // never resolved on the server classpath.
                dev.ergenverse.client.AtlasClientEvents.receiveSync(AtlasNetworkPackets.AtlasSyncS2CPacket.this);
            });
            ctx.get().setPacketHandled(true);
            return true;
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  C2S: client → server — refresh request
    // ════════════════════════════════════════════════════════════════

    /**
     * Client → Server: request atlas refresh. The server replies with a
     * fresh {@link AtlasSyncS2CPacket}.
     *
     * <p>No payload — the server already knows the player and their tier.
     * Useful after a realm breakthrough (when the player should suddenly
     * see new layers) or as a manual "resync" button in the atlas UI.
     */
    public static final class AtlasRequestC2SPacket {

        public AtlasRequestC2SPacket() {}

        public AtlasRequestC2SPacket(FriendlyByteBuf buf) {
            // No payload
        }

        public void encode(FriendlyByteBuf buf) {
            // No payload
        }

        public boolean handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null) return;
                DivineSenseAtlas atlas = AtlasCapability.getOrNull(player);
                if (atlas == null) {
                    Ergenverse.LOGGER.warn("[Ergenverse] Atlas refresh request from {} but no capability attached.",
                            player.getName().getString());
                    return;
                }
                PlayerObserverRealm tier = atlas.getCurrentTier(player);
                List<AtlasEntry> visible = atlas.getEntriesForTier(tier);
                List<String> rumors = new ArrayList<>(atlas.rumorIds());
                AtlasSyncS2CPacket reply = new AtlasSyncS2CPacket(tier, visible, rumors);
                ERNetwork.getChannel().send(
                        PacketDistributor.PLAYER.with(() -> player), reply);
            });
            ctx.get().setPacketHandled(true);
            return true;
        }
    }

    /**
     * Server-side helper: send a fresh atlas sync to the given player.
     *
     * <p>Called from {@code AtlasObservationEvents} after each new
     * observation, and on player login.
     */
    public static void sendToClient(ServerPlayer player, DivineSenseAtlas atlas) {
        if (player == null || atlas == null) return;
        PlayerObserverRealm tier = atlas.getCurrentTier(player);
        List<AtlasEntry> visible = atlas.getEntriesForTier(tier);
        List<String> rumors = new ArrayList<>(atlas.rumorIds());
        AtlasSyncS2CPacket packet = new AtlasSyncS2CPacket(tier, visible, rumors);
        ERNetwork.getChannel().send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
