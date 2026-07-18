package dev.ergenverse.client;

import dev.ergenverse.client.screen.DivineSenseAtlasScreen;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.perception.atlas.AtlasEntry;
import dev.ergenverse.perception.atlas.AtlasNetworkPackets;
import dev.ergenverse.simulation.opportunity.PlayerObserverRealm;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AtlasClientEvents — Forge-bus client-side event handler for the Divine Sense Atlas.
 *
 * <p>Owns the <b>client-side cached atlas state</b> (per PA-5 spec: "handles
 * receiving {@link AtlasNetworkPackets.AtlasSyncS2CPacket} and updating
 * client-side cached atlas state"). The cache fields are deliberately typed
 * to NOT reference client-only Minecraft classes — this keeps the class
 * loadable on both sides (server loads it because the packet handler
 * references {@link #receiveSync}; client renders from it).
 *
 * <h2>Cache fields</h2>
 * <ul>
 *   <li>{@link #clientEntries} — tier-filtered list of entries the player
 *       can perceive (already filtered server-side; the client just renders).</li>
 *   <li>{@link #clientTier} — the player's current perception tier (used
 *       by the screen to decide which layer toggles are unlocked).</li>
 *   <li>{@link #clientRumors} — rumor IDs the player has heard (rendered
 *       in a future "rumors" tab; tracked now for forward compatibility).</li>
 *   <li>{@link #lastSyncTick} — client tick of the most recent sync (debug).</li>
 * </ul>
 *
 * <p>The screen reads these via {@link #getClientEntries()} and
 * {@link #getClientTier()} on every render frame (cheap; just field reads).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AtlasClientEvents {

    private AtlasClientEvents() {}

    // ─── Cached client state (no client-only Minecraft types in field declarations) ──
    // These fields are referenced from AtlasNetworkPackets.AtlasSyncS2CPacket.handle(),
    // so they must not require client-only classes at class-load time. The Minecraft
    // client class is referenced inside method bodies only (lazy resolved at runtime).

    private static volatile List<AtlasEntry> clientEntries = Collections.emptyList();
    private static volatile PlayerObserverRealm clientTier = PlayerObserverRealm.MORTAL;
    private static volatile List<String> clientRumors = Collections.emptyList();
    private static volatile long lastSyncTick = -1L;

    /**
     * Receive an atlas sync from the server. Called from the S2C packet handler
     * on the client render thread (after enqueueWork). Stashes a defensive copy
     * of the entries so the server can mutate its own state without affecting
     * the client's snapshot mid-render.
     */
    public static void receiveSync(AtlasNetworkPackets.AtlasSyncS2CPacket packet) {
        if (packet == null) return;
        clientEntries = new ArrayList<>(packet.entries);
        clientRumors = new ArrayList<>(packet.rumorIds);
        clientTier = PlayerObserverRealm.values()
                [Math.min(Math.max(packet.tierOrder, 0), PlayerObserverRealm.values().length - 1)];
        // Best-effort tick stamp (only available on the client; safe to call here
        // because this method is only invoked on the client).
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                lastSyncTick = mc.level.getGameTime();
            }
        } catch (Throwable ignored) {
            // Defensive — should never happen on the client, but guard anyway.
        }
        Ergenverse.LOGGER.debug("[Ergenverse] Atlas sync received: {} entries, tier={}.",
                clientEntries.size(), clientTier.label);
    }

    /** Latest cached entries (immutable view). */
    public static List<AtlasEntry> getClientEntries() {
        return clientEntries;
    }

    /** Latest cached perception tier. */
    public static PlayerObserverRealm getClientTier() {
        return clientTier;
    }

    /** Latest cached rumor IDs. */
    public static List<String> getClientRumors() {
        return clientRumors;
    }

    /** Client tick of the most recent sync, or -1 if never synced. */
    public static long getLastSyncTick() {
        return lastSyncTick;
    }

    // ─── M-key handler ──────────────────────────────────────────────

    /**
     * On each client tick (END phase), check if the M-key was pressed.
     * If so, open the {@link DivineSenseAtlasScreen}. Using
     * {@link TickEvent.ClientTickEvent} (not {@code InputEvent.Key}) so
     * we get reliable per-tick polling — the screen can be re-opened
     * immediately after closing without missing a key event.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        // Don't open if a screen is already showing (would stack screens).
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        if (mc.screen != null) return;
        if (mc.player == null) return;

        if (AtlasKeybind.ATLAS_KEY.consumeClick()) {
            mc.setScreen(new DivineSenseAtlasScreen());
        }
    }
}
