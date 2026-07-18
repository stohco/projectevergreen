package dev.ergenverse.network;

import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.perception.PerceptionTier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Client-side cache of the player's cultivation state.
 *
 * <p>This is the ONLY source of truth on the client for cultivation data.
 * It is NEVER written to — only updated by incoming S2C sync packets.
 * All client-side systems (HUD overlay, perception checks, name tag
 * translation) read from here.
 *
 * <p>Thread safety: only modified from the client network thread via
 * {@code enqueueWork}, which runs on the render thread. All reads
 * happen on the render thread. No concurrent access.
 */
public final class ClientCultivationCache {

    private ClientCultivationCache() {}

    private static CultivationSyncS2CPacket current = null;

    /** Transient visual flags — set by lightweight S2C packets, NOT persisted. */
    private static boolean meditatingVisual = false;
    private static int tribulationBoltFlashTimer = 0;
    private static int lastBoltIndex = 0;
    private static int lastTotalBolts = 0;

    public static boolean isAvailable() {
        return current != null;
    }

    /** Called from the S2C packet handler (client render thread). */
    public static void receiveSync(CultivationSyncS2CPacket packet) {
        current = packet;
    }

    /** Get the cached sync data. Null if no sync received yet. */
    public static CultivationSyncS2CPacket get() {
        return current;
    }

    /** Get the player's current RealmId from cache. */
    public static RealmId getRealm() {
        return current != null ? RealmId.byOrder(current.realmOrder) : RealmId.MORTAL;
    }

    /** Get the player's perception tier from cache. */
    public static PerceptionTier getPerceptionTier() {
        return PerceptionTier.fromRealm(getRealm());
    }

    /** Is the meditation visual active (closed-eye overlay)? */
    public static boolean isMeditatingVisual() {
        return meditatingVisual;
    }

    public static void setMeditatingVisual(boolean value) {
        meditatingVisual = value;
    }

    /** Tribulation bolt flash — returns remaining flash ticks, decremented each render frame. */
    public static int getTribulationFlashTimer() {
        return tribulationBoltFlashTimer;
    }

    public static int getLastBoltIndex() {
        return lastBoltIndex;
    }

    public static int getLastTotalBolts() {
        return lastTotalBolts;
    }

    /** Called from TribulationBoltS2CPacket handler. */
    public static void onTribulationBolt(TribulationBoltS2CPacket packet) {
        tribulationBoltFlashTimer = 10; // 10 frames of white flash
        lastBoltIndex = packet.boltIndex;
        lastTotalBolts = packet.totalBolts;
    }

    /** Called each render frame to decay the flash timer. */
    public static void tickClientVisuals() {
        if (tribulationBoltFlashTimer > 0) tribulationBoltFlashTimer--;
        // Decay divine sense HUD display timer
        if (divineSenseDisplayTicks > 0) divineSenseDisplayTicks--;
    }

    // ── Divine Sense Pulse Result Cache ────────────────────────────────

    /** The last divine sense pulse result, or null if none. */
    private static DivineSenseResultS2CPacket lastDivineSenseResult = null;

    /** How many ticks to display the divine sense HUD (100 ticks = 5 seconds). */
    private static int divineSenseDisplayTicks = 0;

    /** Called from DivineSenseResultS2CPacket handler. */
    public static void onDivineSenseResult(DivineSenseResultS2CPacket result) {
        lastDivineSenseResult = result;
        divineSenseDisplayTicks = 100; // 5 seconds
    }

    /** Get the current divine sense result for HUD rendering. */
    public static DivineSenseResultS2CPacket getDivineSenseResult() {
        if (divineSenseDisplayTicks <= 0) return null;
        return lastDivineSenseResult;
    }

    /** Is the divine sense HUD visible? */
    public static boolean isDivineSenseActive() {
        return divineSenseDisplayTicks > 0 && lastDivineSenseResult != null;
    }

    // ── Perception Sync Cache ──────────────────────────────────────────
    //
    // Server sends perception data for nearby EntityCultivators every ~2
    // seconds via PerceptionSyncS2CPacket. The client caches it here so
    // that name tag rendering and tooltip display don't need to re-run
    // the PerceptionEngine every frame.

    /** Cached perception data: entity ID → perception entry. */
    private static final Map<Integer, PerceptionSyncS2CPacket.EntityPerception> perceptionCache =
            new HashMap<>();

    /** Called from PerceptionSyncS2CPacket handler. Replaces the entire cache. */
    public static void onPerceptionSync(PerceptionSyncS2CPacket packet) {
        perceptionCache.clear();
        for (var entry : packet.entries) {
            perceptionCache.put(entry.entityId, entry);
        }
    }

    /** Get cached perception data for an entity, or null if not cached. */
    @org.jetbrains.annotations.Nullable
    public static PerceptionSyncS2CPacket.EntityPerception getPerception(int entityId) {
        return perceptionCache.get(entityId);
    }

    /** Get the full perception cache (unmodifiable). For HUD rendering. */
    public static Map<Integer, PerceptionSyncS2CPacket.EntityPerception> getPerceptionCache() {
        return Collections.unmodifiableMap(perceptionCache);
    }

    /** Clear the perception cache (e.g., on dimension change). */
    public static void clearPerceptionCache() {
        perceptionCache.clear();
    }

    // ── Workstation progress stubs ─────────────────────────────────────
    // These return 0/false until the sync packets are properly wired.
    // TODO: wire these to the actual sync packet handlers.
    
    public static float getAlchemyProgress() { return 0.0f; }
    public static boolean isAlchemyIncomplete() { return false; }
    public static float getForgeProgress() { return 0.0f; }
    public static boolean isForgeIncomplete() { return false; }
    public static float getForgeMode() { return 0.0f; }
    public static float getTalismanProgress() { return 0.0f; }
    public static boolean isTalismanIncomplete() { return false; }
    public static float getFormationProgress() { return 0.0f; }
    public static boolean isFormationIncomplete() { return false; }
    public static float getRestrictionProgress() { return 0.0f; }
    public static boolean isRestrictionIncomplete() { return false; }
    public static float getBodyRefiningProgress() { return 0.0f; }
    public static boolean isBodyRefiningIncomplete() { return false; }
    public static float getPuppetProgress() { return 0.0f; }
    public static boolean isPuppetIncomplete() { return false; }
    public static float getSoulRefiningProgress() { return 0.0f; }
    public static boolean isSoulRefiningIncomplete() { return false; }
    public static float getBeastPactProgress() { return 0.0f; }
    public static boolean isBeastPactIncomplete() { return false; }

}
