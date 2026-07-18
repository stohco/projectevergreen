package dev.ergenverse.simulation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WorldRuntimeState — the mutable t>0 overlay layer.
 *
 * <p><b>Purpose:</b> the canon database ({@code data/ergenverse/} JSONs loaded by
 * {@link WorldStateDataLoader}) is the read-only t₀ archive. It is packaged
 * inside the mod JAR and is NOT writable at runtime. Every post-t₀ mutation —
 * an NPC took damage, a faction was destroyed at simulation time, an item
 * changed hands, a karma consequence fired, a player killed someone — must be
 * persisted somewhere that survives chunk unload and server restart.
 *
 * <p>This class is that "somewhere." It extends Minecraft's {@link SavedData},
 * which serializes to {@code <world>/data/ergenverse_runtime_state.dat} via NBT.
 * The canon DB is never touched. Reads consult canon first; runtime overrides
 * layer on top. Writes go only here.
 *
 * <h2>Schema (v1)</h2>
 * <ul>
 *   <li>{@code npcOverrides} — {@code characterId → CompoundTag}: mutated NPC
 *       state (current HP, position drift, learned techniques, injuries).</li>
 *   <li>{@code factionOverrides} — {@code factionId → CompoundTag}: faction
 *       state changes (destroyed/declined at t>0, membership changes).</li>
 *   <li>{@code itemOwnershipOverrides} — {@code itemId → CompoundTag}:
 *       provenance changes (Wang Lin gave the Karma Whip to Ling'er).</li>
 *   <li>{@code karmaResolutionState} — {@code karmaNodeId → CompoundTag}:
 *       which karma consequences have fired, resolution tick, outcome.</li>
 *   <li>{@code playerMutations} — {@code playerUuid → CompoundTag}:
 *       per-player t>0 actions (kills, loot, discoveries). Keyed by UUID
 *       for multiplayer correctness.</li>
 *   <li>{@code divergenceCounter} — how many t>0 simulation ticks have
 *       elapsed since t₀. The canonical measure of "how far has the world
 *       diverged from the novel."</li>
 * </ul>
 *
 * <h2>Thread safety</h2>
 * <p>All maps are {@link ConcurrentHashMap}. Getters return defensive
 * {@link CompoundTag#copy()} copies to prevent the "phantom mutation" bug
 * (caller mutates the returned tag, {@code setDirty()} is never called,
 * the change is lost on save). Setters also copy on write, so the caller's
 * tag cannot be mutated after handoff.
 *
 * <h2>Versioning</h2>
 * <p>The NBT carries a {@code data_version} int. {@link #load(CompoundTag)}
 * checks the version and runs migration blocks for legacy saves. v1 is the
 * initial schema; future schema changes increment {@link #CURRENT_VERSION}
 * and add migration logic in the {@code if (version < N)} blocks.
 *
 * <h2>Prime Directive</h2>
 * <p>"Reality is objective." This layer does not create reality — it records
 * how reality has changed since the objective t₀ starting state. The canon
 * DB is the truth at t₀; this layer is the truth at t>0.
 */
public class WorldRuntimeState extends SavedData {

    private static final String DATA_NAME = "ergenverse_runtime_state";
    private static final int CURRENT_VERSION = 1;

    // ─── t>0 mutation maps (all ConcurrentHashMap, all keyed by String) ───
    private final Map<String, CompoundTag> npcOverrides = new ConcurrentHashMap<>();
    private final Map<String, CompoundTag> factionOverrides = new ConcurrentHashMap<>();
    private final Map<String, CompoundTag> itemOwnershipOverrides = new ConcurrentHashMap<>();
    private final Map<String, CompoundTag> karmaResolutionState = new ConcurrentHashMap<>();
    private final Map<String, CompoundTag> playerMutations = new ConcurrentHashMap<>();
    private final Map<String, CompoundTag> caveWorldOwnershipOverrides = new ConcurrentHashMap<>();

    private volatile long divergenceCounter = 0;

    // ═══════════════════════════════════════════════════════════════════
    //  Access
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Get the world-global runtime state. Attached to the overworld's
     * {@link DimensionDataStorage} so it persists across dimensions and
     * survives server restart. Lazy-creates on first access.
     */
    public static WorldRuntimeState get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(WorldRuntimeState::load, WorldRuntimeState::new, DATA_NAME);
    }

    // ─── NPC overrides ──────────────────────────────────────────────────

    /**
     * Get the runtime override state for a character, or {@code null} if no
     * override exists (caller should fall back to canon baseline).
     *
     * <p>Returns a defensive copy — mutations to the returned tag will NOT
     * be persisted. Call {@link #updateNpcState} to persist changes.
     */
    @Nullable
    public CompoundTag getNpcState(String characterId) {
        CompoundTag state = npcOverrides.get(characterId);
        return state != null ? state.copy() : null;
    }

    /**
     * Set or replace the runtime override for a character. Copies the input
     * tag so the caller retains ownership of its reference.
     */
    public void updateNpcState(String characterId, CompoundTag updatedData) {
        npcOverrides.put(characterId, updatedData.copy());
        this.setDirty();
    }

    /** Remove a character's runtime override (they fall back to canon). */
    public void removeNpcState(String characterId) {
        if (npcOverrides.remove(characterId) != null) {
            this.setDirty();
        }
    }

    /** Check whether any runtime override exists for a character. */
    public boolean hasNpcOverride(String characterId) {
        return npcOverrides.containsKey(characterId);
    }

    // ─── Faction overrides ──────────────────────────────────────────────

    @Nullable
    public CompoundTag getFactionState(String factionId) {
        CompoundTag state = factionOverrides.get(factionId);
        return state != null ? state.copy() : null;
    }

    public void updateFactionState(String factionId, CompoundTag updatedData) {
        factionOverrides.put(factionId, updatedData.copy());
        this.setDirty();
    }

    // ─── Item ownership overrides ───────────────────────────────────────

    @Nullable
    public CompoundTag getItemOwnership(String itemId) {
        CompoundTag state = itemOwnershipOverrides.get(itemId);
        return state != null ? state.copy() : null;
    }

    public void updateItemOwnership(String itemId, CompoundTag updatedData) {
        itemOwnershipOverrides.put(itemId, updatedData.copy());
        this.setDirty();
    }

    // ─── Karma resolution ───────────────────────────────────────────────

    @Nullable
    public CompoundTag getKarmaResolution(String karmaNodeId) {
        CompoundTag state = karmaResolutionState.get(karmaNodeId);
        return state != null ? state.copy() : null;
    }

    public void markKarmaResolved(String karmaNodeId, long resolvedTick, String outcome) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("resolved_tick", resolvedTick);
        tag.putString("outcome", outcome);
        karmaResolutionState.put(karmaNodeId, tag);
        this.setDirty();
    }

    public boolean isKarmaResolved(String karmaNodeId) {
        return karmaResolutionState.containsKey(karmaNodeId);
    }

    // ─── Player mutations ───────────────────────────────────────────────

    /**
     * Get a player's mutation tag. Returns a defensive copy, or a fresh
     * empty tag if the player has no recorded mutations yet (never null).
     */
    public CompoundTag getPlayerMutation(String playerUuid) {
        CompoundTag state = playerMutations.get(playerUuid);
        return state != null ? state.copy() : new CompoundTag();
    }

    public void updatePlayerMutation(String playerUuid, CompoundTag updatedData) {
        playerMutations.put(playerUuid, updatedData.copy());
        this.setDirty();
    }

    // ─── Divergence counter ─────────────────────────────────────────────

    /** How many t>0 simulation ticks have elapsed since t₀. */
    public long getDivergenceCounter() {
        return divergenceCounter;
    }

    /** Advance the divergence counter by one (called by WorldStateEngine.tick). */
    public void incrementDivergence() {
        divergenceCounter++;
        this.setDirty();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Serialization
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compound) {
        compound.putInt("data_version", CURRENT_VERSION);
        compound.putLong("divergence_counter", divergenceCounter);

        compound.put("npc_overrides", serializeMap(npcOverrides));
        compound.put("faction_overrides", serializeMap(factionOverrides));
        compound.put("item_ownership_overrides", serializeMap(itemOwnershipOverrides));
        compound.put("karma_resolution", serializeMap(karmaResolutionState));
        compound.put("player_mutations", serializeMap(playerMutations));
        compound.put("cave_world_ownership", serializeMap(caveWorldOwnershipOverrides));

        return compound;
    }

    public static WorldRuntimeState load(CompoundTag compound) {
        WorldRuntimeState state = new WorldRuntimeState();
        int version = compound.getInt("data_version");

        // ── Migration pathway ───────────────────────────────────────────
        // v0 = pre-versioning saves (no data_version key → getInt returns 0).
        // Future: if (version < 2) { migrate v1 → v2 }
        if (version < 1) {
            // v0 → v1: no structural changes (v1 is the initial schema),
            // but we log the migration for auditability.
            dev.ergenverse.core.Ergenverse.LOGGER.info(
                    "[WorldRuntimeState] Migrating save from v{} to v{}", version, CURRENT_VERSION);
        }

        state.divergenceCounter = compound.getLong("divergence_counter");

        deserializeMap(compound.getList("npc_overrides", Tag.TAG_COMPOUND), state.npcOverrides);
        deserializeMap(compound.getList("faction_overrides", Tag.TAG_COMPOUND), state.factionOverrides);
        deserializeMap(compound.getList("item_ownership_overrides", Tag.TAG_COMPOUND), state.itemOwnershipOverrides);
        deserializeMap(compound.getList("karma_resolution", Tag.TAG_COMPOUND), state.karmaResolutionState);
        deserializeMap(compound.getList("player_mutations", Tag.TAG_COMPOUND), state.playerMutations);
        deserializeMap(compound.getList("cave_world_ownership", Tag.TAG_COMPOUND), state.caveWorldOwnershipOverrides);

        return state;
    }

    // ─── NBT map (de)serialization helpers ──────────────────────────────

    private static ListTag serializeMap(Map<String, CompoundTag> map) {
        ListTag list = new ListTag();
        for (Map.Entry<String, CompoundTag> entry : map.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("KeyId", entry.getKey());
            entryTag.put("ValueData", entry.getValue());
            list.add(entryTag);
        }
        return list;
    }

    private static void deserializeMap(ListTag list, Map<String, CompoundTag> targetMap) {
        targetMap.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            String key = entryTag.getString("KeyId");
            CompoundTag value = entryTag.getCompound("ValueData");
            if (!key.isEmpty()) {
                targetMap.put(key, value);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Diagnostics
    // ═══════════════════════════════════════════════════════════════════

    // ─── Cave World Ownership Overrides ─────────────────────────────

    /** Get the runtime Cave World ownership override, or null. */
    @Nullable
    public CompoundTag getCaveWorldOwnership(String worldLayerId) {
        CompoundTag tag = caveWorldOwnershipOverrides.get(worldLayerId);
        return tag != null ? tag.copy() : null;
    }

    /** Set a Cave World ownership override (e.g. after killing the owner). */
    public void setCaveWorldOwnership(String worldLayerId, CompoundTag ownershipData) {
        caveWorldOwnershipOverrides.put(worldLayerId, ownershipData.copy());
        this.setDirty();
    }

    /** Check if a Cave World ownership override exists. */
    public boolean hasCaveWorldOwnershipOverride(String worldLayerId) {
        return caveWorldOwnershipOverrides.containsKey(worldLayerId);
    }

    /** Get all Cave World ownership overrides. */
    public Map<String, CompoundTag> getAllCaveWorldOwnershipOverrides() {
        return java.util.Collections.unmodifiableMap(caveWorldOwnershipOverrides);
    }

    /** A human-readable status string for debug commands. */
    public String getStatusReport() {
        return String.format(
                "WorldRuntimeState{v=%d, divergence=%d, npc=%d, faction=%d, item=%d, karma=%d, player=%d}",
                CURRENT_VERSION, divergenceCounter,
                npcOverrides.size(), factionOverrides.size(),
                itemOwnershipOverrides.size(), karmaResolutionState.size(),
                playerMutations.size(), caveWorldOwnershipOverrides.size());
    }
}
