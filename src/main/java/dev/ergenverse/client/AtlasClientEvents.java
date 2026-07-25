package dev.ergenverse.client;

import dev.ergenverse.client.screen.DivineSenseAtlasScreen;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.perception.atlas.AtlasCapability;
import dev.ergenverse.perception.atlas.AtlasEntry;
import dev.ergenverse.perception.atlas.DivineSenseAtlas;
import dev.ergenverse.simulation.opportunity.PlayerObserverRealm;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AtlasClientEvents — client-side event handler for the Divine Sense Atlas.
 *
 * <p><b>SINGLE-PLAYER ARCHITECTURE (2026-07-25 pivot):</b> There is no
 * networking. The atlas lives on the player's capability, which is the
 * single source of truth. This class owns a <b>render cache</b> that is
 * refreshed directly from the local player's capability — no packets,
 * no serialization, no registration, no login sync.
 *
 * <p>The data flow is:
 * <pre>
 *   World
 *     ↓
 *   Player Capability (DivineSenseAtlas)
 *     ↓
 *   AtlasClientEvents.refreshFromLocalPlayer()
 *     ↓
 *   render cache (clientEntries, clientTier, clientRumors)
 *     ↓
 *   DivineSenseAtlasScreen.draw()
 * </pre>
 *
 * <h2>Cache fields</h2>
 * <ul>
 *   <li>{@link #clientEntries} — tier-filtered list of entries the player
 *       can perceive.</li>
 *   <li>{@link #clientTier} — the player's current perception tier (used
 *       by the screen to decide which layer toggles are unlocked).</li>
 *   <li>{@link #clientRumors} — rumor IDs the player has heard.</li>
 *   <li>{@link #lastSyncTick} — client tick of the most recent refresh.</li>
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

    // ─── Render cache (refreshed from local player capability) ──

    private static volatile List<AtlasEntry> clientEntries = Collections.emptyList();
    private static volatile PlayerObserverRealm clientTier = PlayerObserverRealm.MORTAL;
    private static volatile List<String> clientRumors = Collections.emptyList();
    private static volatile long lastSyncTick = -1L;

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

    /** Client tick of the most recent refresh, or -1 if never refreshed. */
    public static long getLastSyncTick() {
        return lastSyncTick;
    }

    /**
     * SINGLE-PLAYER refresh: reads the atlas state directly from the local
     * player's capability. No network packet needed. Called by the
     * DivineSenseAtlasScreen refresh button and by the tick handler when
     * the screen is open.
     *
     * <p>This is the ONLY way the render cache is populated. There is no
     * packet handler, no login sync, no server push. The client pulls.
     */
    public static void refreshFromLocalPlayer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        try {
            // Compute tier client-side from cultivation state (getCurrentTier
            // takes ServerPlayer, but we're on the client with a LocalPlayer).
            PlayerObserverRealm tier = PlayerObserverRealm.MORTAL;
            var cultivationOpt = CultivationCapability.get(mc.player);
            if (cultivationOpt.isPresent()) {
                CultivationState state = cultivationOpt.resolve().get();
                RealmId realm = state.getCurrentRealm();
                tier = DivineSenseAtlas.fromRealmId(realm);
            }

            final PlayerObserverRealm finalTier = tier;
            LazyOptional<DivineSenseAtlas> opt = mc.player.getCapability(AtlasCapability.ATLAS);
            opt.ifPresent(atlas -> {
                List<AtlasEntry> visible = atlas.getEntriesForTier(finalTier);
                clientEntries = new ArrayList<>(visible);
                clientRumors = new ArrayList<>(atlas.rumorIds());
                clientTier = finalTier;
                if (mc.level != null) {
                    lastSyncTick = mc.level.getGameTime();
                }
            });
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] Atlas local refresh failed: {}", t.getMessage());
        }
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
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        if (mc.player == null) return;

        // SINGLE-PLAYER: if the atlas screen is open, refresh from local
        // capability every 10 ticks (0.5s) so new observations appear live.
        if (mc.screen instanceof DivineSenseAtlasScreen) {
            if (mc.level != null && mc.level.getGameTime() % 10 == 0) {
                refreshFromLocalPlayer();
            }
            return; // don't process keybind while screen is open
        }

        if (AtlasKeybind.ATLAS_KEY.consumeClick()) {
            // Refresh before opening so the screen has fresh data.
            refreshFromLocalPlayer();
            mc.setScreen(new DivineSenseAtlasScreen());
        }
    }
}
