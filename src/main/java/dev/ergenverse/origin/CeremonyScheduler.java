package dev.ergenverse.origin;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CeremonyScheduler — in-memory + NBT-persisted per-player state machine
 * for the Day 3 Heng Yue Sect aptitude-test ceremony.
 *
 * <h2>Why a separate class?</h2>
 * <p>The {@link AptitudeTestCeremonyEvent} (ServerTickEvent trigger) and
 * {@link AptitudeTestCeremonyHandler} (right-click interaction) both need
 * to read/write the same per-player ceremony state. Centralizing that
 * state here avoids two event handlers mutating independent maps and
 * racing on "has the recruiter already spawned for this player?".
 *
 * <h2>State machine</h2>
 * <pre>
 *   NOT_DUE
 *     │  (Day 3 reached, recruiter about to spawn)
 *     ▼
 *   SCHEDULED
 *     │  (EntityCultivator spawned; UUID persisted)
 *     ▼
 *   RECRUITER_SPAWNED
 *     │  (player right-clicks recruiter; ceremony runs; completeMortalOrigin)
 *     ▼
 *   COMPLETED  ──── terminal ────
 *
 *   RECRUITER_SPAWNED
 *     │  (recruiter died before player completed test)
 *     ▼
 *   RECRUITER_DIED_WAITING_RESPAWN
 *     │  (next dawn, scheduler respawns recruiter)
 *     ▼
 *   RECRUITER_SPAWNED  (loop back)
 * </pre>
 *
 * <h2>Persistence</h2>
 * <p>The in-memory {@link #playerStates} map is rebuilt on server start
 * by reading each player's persistent NBT under
 * {@link #NBT_KEY_CEREMONY_STATE}. The recruiter's entity UUID is
 * persisted alongside the state so we can detect post-restart recruiter
 * deaths and trigger a respawn.
 *
 * <p>Forge attaches {@link ServerPlayer#getPersistentData()} to every
 * player automatically. The root tag is server-side only — the data
 * never syncs to the client, which is what we want for ceremony state.
 *
 * <h2>Thread safety</h2>
 * <p>All mutations go through {@link ConcurrentHashMap} and synchronized
 * helper methods. The Forge event bus is single-threaded per-server-tick,
 * but {@link PlayerEvent.PlayerLoggedInEvent} can fire during tick
 * processing — ConcurrentHashMap prevents lost writes if two events race.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CeremonyScheduler {

    private CeremonyScheduler() {}

    // ─── NBT keys ───────────────────────────────────────────────────────

    /** Root NBT key under player.getPersistentData() for ceremony state. */
    public static final String NBT_KEY_CEREMONY_STATE = "ergenverse:ceremony_state";

    /** State enum ordinal (int) under the root compound. */
    private static final String NBT_KEY_STATE_ORDINAL = "state_ordinal";

    /** Recruiter entity UUID (string) under the root compound. Empty if none. */
    private static final String NBT_KEY_RECRUITER_UUID = "recruiter_uuid";

    /** Last-known recruiter death tick (long) under the root compound. 0 = none. */
    private static final String NBT_KEY_DEATH_TICK = "death_tick";

    // ─── State enum ─────────────────────────────────────────────────────

    /**
     * Lifecycle states for a single player's ceremony.
     *
     * <p>Ordinals are persisted to NBT — never reorder existing entries.
     * Add new states only at the end (before {@code COUNT}).
     */
    public enum CeremonyState {
        /** Player hasn't reached Day 3 yet (or origin already complete — won't fire). */
        NOT_DUE,
        /** Day 3 reached; recruiter spawn scheduled but not yet attempted this tick. */
        SCHEDULED,
        /** Recruiter EntityCultivator has been spawned into the world. UUID stored. */
        RECRUITER_SPAWNED,
        /** Player completed the aptitude test; cultivation unlocked. Terminal. */
        COMPLETED,
        /** Recruiter died before player completed test; waiting for next dawn to respawn. */
        RECRUITER_DIED_WAITING_RESPAWN;

        /** Safe deserialize by ordinal — falls back to NOT_DUE for unknown ordinals. */
        public static CeremonyState byOrdinal(int ord) {
            CeremonyState[] all = values();
            if (ord < 0 || ord >= all.length) return NOT_DUE;
            return all[ord];
        }
    }

    // ─── Per-player bundle ──────────────────────────────────────────────

    /**
     * Mutable per-player ceremony state. Held in a single object so we
     * can atomically swap state in the {@link ConcurrentHashMap}.
     */
    public static final class PlayerCeremonyData {
        public volatile CeremonyState state;
        @Nullable public volatile UUID recruiterUuid;
        public volatile long recruiterDeathTick;

        PlayerCeremonyData(CeremonyState state, @Nullable UUID recruiterUuid, long deathTick) {
            this.state = state;
            this.recruiterUuid = recruiterUuid;
            this.recruiterDeathTick = deathTick;
        }

        PlayerCeremonyData copy() {
            return new PlayerCeremonyData(state, recruiterUuid, recruiterDeathTick);
        }

        CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putInt(NBT_KEY_STATE_ORDINAL, state.ordinal());
            tag.putString(NBT_KEY_RECRUITER_UUID, recruiterUuid != null ? recruiterUuid.toString() : "");
            tag.putLong(NBT_KEY_DEATH_TICK, recruiterDeathTick);
            return tag;
        }

        static PlayerCeremonyData fromNbt(CompoundTag tag) {
            CeremonyState st = CeremonyState.byOrdinal(tag.getInt(NBT_KEY_STATE_ORDINAL));
            String uuidStr = tag.getString(NBT_KEY_RECRUITER_UUID);
            UUID uuid = (uuidStr == null || uuidStr.isEmpty()) ? null : UUID.fromString(uuidStr);
            long deathTick = tag.getLong(NBT_KEY_DEATH_TICK);
            return new PlayerCeremonyData(st, uuid, deathTick);
        }
    }

    // ─── In-memory store ────────────────────────────────────────────────

    /** Key: player UUID. Value: mutable per-player ceremony data. */
    private static final Map<UUID, PlayerCeremonyData> playerStates = new ConcurrentHashMap<>();

    // ─── Public read API ────────────────────────────────────────────────

    /**
     * Get the ceremony state for a player. Returns {@link CeremonyState#NOT_DUE}
     * for players with no recorded state (the default).
     */
    public static CeremonyState getState(UUID playerUuid) {
        PlayerCeremonyData data = playerStates.get(playerUuid);
        return data != null ? data.state : CeremonyState.NOT_DUE;
    }

    /**
     * Get the recruiter entity UUID for a player, or {@code null} if no
     * recruiter has been spawned (or it was cleared).
     */
    @Nullable
    public static UUID getRecruiterUuid(UUID playerUuid) {
        PlayerCeremonyData data = playerStates.get(playerUuid);
        return data != null ? data.recruiterUuid : null;
    }

    /** Get the last-known recruiter death tick (for respawn scheduling). */
    public static long getRecruiterDeathTick(UUID playerUuid) {
        PlayerCeremonyData data = playerStates.get(playerUuid);
        return data != null ? data.recruiterDeathTick : 0L;
    }

    /** True if a ceremony has been scheduled or has fired for this player. */
    public static boolean ceremonyAlreadyScheduledFor(UUID playerUuid) {
        CeremonyState s = getState(playerUuid);
        return s != CeremonyState.NOT_DUE;
    }

    // ─── Public write API ───────────────────────────────────────────────

    /**
     * Set the ceremony state for a player. Also persists to player NBT
     * if the player is currently online (offline players' NBT is updated
     * on next login via the load hook).
     *
     * @param player the server player (may be {@code null} for offline mutation)
     * @param playerUuid the player UUID
     * @param newState the new state
     */
    public static void setState(@Nullable ServerPlayer player, UUID playerUuid, CeremonyState newState) {
        PlayerCeremonyData data = playerStates.computeIfAbsent(playerUuid,
                k -> new PlayerCeremonyData(CeremonyState.NOT_DUE, null, 0L));
        data.state = newState;
        if (player != null) {
            persistToPlayerNbt(player, data);
        }
    }

    /**
     * Convenience overload that takes only UUID (no ServerPlayer). Use when
     * the caller doesn't have a ServerPlayer reference (e.g., during a tick
     * scan where the player may have disconnected between scan and mutation).
     * NBT will be persisted on next login.
     */
    public static void setState(UUID playerUuid, CeremonyState newState) {
        setState(null, playerUuid, newState);
    }

    /**
     * Record the recruiter's entity UUID for a player. Implies the state
     * is (or is becoming) {@link CeremonyState#RECRUITER_SPAWNED}.
     */
    public static void setRecruiterUuid(@Nullable ServerPlayer player, UUID playerUuid,
                                          @Nullable UUID recruiterUuid) {
        PlayerCeremonyData data = playerStates.computeIfAbsent(playerUuid,
                k -> new PlayerCeremonyData(CeremonyState.NOT_DUE, null, 0L));
        data.recruiterUuid = recruiterUuid;
        if (recruiterUuid != null && data.state == CeremonyState.SCHEDULED) {
            data.state = CeremonyState.RECRUITER_SPAWNED;
        }
        if (player != null) {
            persistToPlayerNbt(player, data);
        }
    }

    /**
     * Record the recruiter's death tick (set when its EntityCultivator
     * dies before the player completes the test). Implies the state is
     * (or is becoming) {@link CeremonyState#RECRUITER_DIED_WAITING_RESPAWN}.
     */
    public static void markRecruiterDied(@Nullable ServerPlayer player, UUID playerUuid,
                                          long deathTick) {
        PlayerCeremonyData data = playerStates.computeIfAbsent(playerUuid,
                k -> new PlayerCeremonyData(CeremonyState.NOT_DUE, null, 0L));
        data.recruiterDeathTick = deathTick;
        data.recruiterUuid = null; // recruiter is gone
        data.state = CeremonyState.RECRUITER_DIED_WAITING_RESPAWN;
        if (player != null) {
            persistToPlayerNbt(player, data);
        }
    }

    /**
     * Clear all ceremony state for a player (e.g., after completion, to
     * free memory — the persisted NBT remains until overwritten).
     */
    public static void clear(UUID playerUuid) {
        playerStates.remove(playerUuid);
    }

    /**
     * Clear the in-memory recruiter UUID but keep the state. Used when
     * the recruiter entity is despawned post-ceremony (after the player
     * has completed the test) so we don't keep a dangling reference.
     */
    public static void clearRecruiterUuid(UUID playerUuid) {
        PlayerCeremonyData data = playerStates.get(playerUuid);
        if (data != null) {
            data.recruiterUuid = null;
        }
    }

    // ─── NBT persistence ────────────────────────────────────────────────

    /**
     * Persist the current in-memory state to the player's persistent NBT.
     * Called after every state mutation when the player is online.
     */
    private static void persistToPlayerNbt(ServerPlayer player, PlayerCeremonyData data) {
        try {
            CompoundTag root = player.getPersistentData();
            root.put(NBT_KEY_CEREMONY_STATE, data.toNbt());
        } catch (Exception e) {
            Ergenverse.LOGGER.warn("[CeremonyScheduler] Failed to persist NBT for {}: {}",
                    player.getName().getString(), e.getMessage());
        }
    }

    /**
     * On player login, re-read ceremony state from persistent NBT into
     * the in-memory map. This is how state survives server restarts.
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        try {
            CompoundTag root = player.getPersistentData();
            if (root.contains(NBT_KEY_CEREMONY_STATE)) {
                CompoundTag stateTag = root.getCompound(NBT_KEY_CEREMONY_STATE);
                PlayerCeremonyData data = PlayerCeremonyData.fromNbt(stateTag);
                playerStates.put(player.getUUID(), data);
                Ergenverse.LOGGER.info("[CeremonyScheduler] Loaded ceremony state for {}: {} (recruiter={})",
                        player.getName().getString(), data.state,
                        data.recruiterUuid != null ? data.recruiterUuid : "none");
            }
        } catch (Exception e) {
            Ergenverse.LOGGER.warn("[CeremonyScheduler] Failed to read NBT for {}: {}",
                    player.getName().getString(), e.getMessage());
        }
    }

    /**
     * On player logout, ensure the latest in-memory state is flushed to
     * NBT (so it survives a server restart while the player is offline).
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerCeremonyData data = playerStates.get(player.getUUID());
        if (data != null) {
            persistToPlayerNbt(player, data);
        }
    }

    // ─── Debug helpers ──────────────────────────────────────────────────

    /** Total players with non-default ceremony state (for debug overlays). */
    public static int trackedPlayerCount() {
        return playerStates.size();
    }

    /**
     * Return a defensive copy of the full state map (for debug commands).
     * Not used in the hot path — O(N) per call.
     */
    public static Map<UUID, CeremonyState> snapshotStates() {
        Map<UUID, CeremonyState> out = new HashMap<>();
        playerStates.forEach((uuid, data) -> out.put(uuid, data.state));
        return out;
    }

    /**
     * ListTag adapter — included for future expansion (e.g., per-player
     * ceremony history log). Currently unused but kept as a stable API
     * surface so downstream code can serialize multiple state snapshots.
     */
    public static ListTag serializeHistoryList(java.util.Collection<PlayerCeremonyData> snapshots) {
        ListTag list = new ListTag();
        for (PlayerCeremonyData d : snapshots) {
            list.add(d.toNbt());
        }
        return list;
    }

    /**
     * Deserialize a ListTag of PlayerCeremonyData entries (counterpart
     * to {@link #serializeHistoryList}).
     */
    public static java.util.List<PlayerCeremonyData> deserializeHistoryList(Tag tag) {
        java.util.List<PlayerCeremonyData> out = new java.util.ArrayList<>();
        if (!(tag instanceof ListTag list) || list.isEmpty()) return out;
        for (Tag e : list) {
            if (e instanceof CompoundTag ct) {
                out.add(PlayerCeremonyData.fromNbt(ct));
            }
        }
        return out;
    }
}
