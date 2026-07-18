package dev.ergenverse.simulation.opportunity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpportunityRegistry — the singleton world-store of all live opportunity
 * instances.
 *
 * <p>This is the central coordinator for the Opportunity Engine. It:
 * <ul>
 *   <li>Holds every active {@link OpportunityState} in the current world.</li>
 *   <li>Advances them every {@link #tickAll(ServerLevel, long)} call (driven
 *       by {@link OpportunityEngineEvents} on the Forge server tick bus).</li>
 *   <li>Answers spatial queries: "what opportunities are near this position?"</li>
 *   <li>Answers historical queries: "what opportunities happened near here
 *       in the past?" (used by the future rumor system, §6.11).</li>
 *   <li>Persists to world NBT via {@link SavedData} — survives chunk unload
 *       and server restart.</li>
 * </ul>
 *
 * <h2>Storage</h2>
 * <p>Saved to {@code <world>/data/ergenverse_opportunity_registry.dat} as a
 * {@link CompoundTag}. Each {@link OpportunityState} is stored under its
 * opportunityId; the registry preserves insertion order via a
 * {@link ConcurrentHashMap} (lookups by ID are O(1); iteration order is
 * unspecified but stable within a server session).
 *
 * <h2>Driver dispatch</h2>
 * <p>Each {@link OpportunityState} carries a {@code driverType} string.
 * On {@link #tickAll}, the registry dispatches to the appropriate driver:
 * <ul>
 *   <li>{@code "spirit_fruit_timeline"} →
 *       {@link SpiritFruitTimeline#advance}</li>
 *   <li>{@code "noop"} (or unknown) →
 *       {@link OpportunityFSM#tickOpportunity} with
 *       {@link WorldSimContext#NoOpWorldSimContext}</li>
 * </ul>
 * <p>Future driver types (inheritance cave, treasure birth, formation
 * activation, etc.) will be added here as new {@code if} branches.
 *
 * <h2>Thread safety</h2>
 * <p>All maps are {@link ConcurrentHashMap}. Mutations are confined to the
 * server tick thread (single-threaded by Minecraft contract). Reads from
 * other threads must coordinate externally (e.g., command handlers running
 * on the main thread).
 *
 * <h2>Prime Directive</h2>
 * <p>The registry does NOT create canon opportunities. It only registers
 * simulation-layer opportunity states (which may or may not correspond to
 * a canon {@link CanonOpportunityRecord}). The canon-vs-simulation split
 * (Layer 1 vs Layer 2) is preserved.
 */
public final class OpportunityRegistry extends SavedData {

    private static final String DATA_NAME = "ergenverse_opportunity_registry";
    private static final int CURRENT_VERSION = 1;

    /** All active opportunity states, keyed by opportunityId. */
    private final Map<String, OpportunityState> states = new ConcurrentHashMap<>();

    /** Singleton accessor — get the registry for the given server level. */
    public static OpportunityRegistry get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        OpportunityRegistry registry = storage.computeIfAbsent(
            OpportunityRegistry::load, OpportunityRegistry::new, DATA_NAME);
        // Wire up the dirty callback so individual state mutations mark us dirty.
        registry.rebindDirtyCallbacks();
        return registry;
    }

    public OpportunityRegistry() {
        super();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Registration & lookup
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Register a new opportunity state. If a state with the same ID already
     * exists, it is replaced (the caller should usually check first).
     *
     * @param state the state to register (must have a non-blank opportunityId)
     */
    public void register(OpportunityState state) {
        if (state == null) return;
        String id = state.getOpportunityId();
        if (id == null || id.isBlank()) return;
        state.setDirtyCallback(this::setDirty);
        states.put(id, state);
        this.setDirty();
        dev.ergenverse.core.Ergenverse.LOGGER.debug(
            "[OpportunityRegistry] Registered opportunity {} (lifecycle={}, driver={})",
            id, state.getLifecycle(), state.getDriverType());
    }

    /** Look up a state by ID. Returns null if not registered. */
    @Nullable
    public OpportunityState get(String opportunityId) {
        return states.get(opportunityId);
    }

    /** Total number of registered states (any lifecycle). */
    public int size() {
        return states.size();
    }

    /** Number of states in a given lifecycle. */
    public long countByLifecycle(OpportunityLifecycle lifecycle) {
        return states.values().stream()
            .filter(s -> s.getLifecycle() == lifecycle)
            .count();
    }

    /** Immutable snapshot of all registered states. */
    public List<OpportunityState> snapshot() {
        return List.copyOf(states.values());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Tick — driven by OpportunityEngineEvents every 100 server ticks
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Advance every registered opportunity state by one tick.
     *
     * <p>Dispatches to the appropriate driver based on each state's
     * {@code driverType}. Drivers are responsible for calling
     * {@link OpportunityFSM#tickOpportunity} internally (with their own
     * {@link WorldSimContext}).
     *
     * <p>States that have reached {@link OpportunityLifecycle#HISTORICAL}
     * are NOT removed — they persist as world memory for the rumor system
     * to query. A future garbage-collector (e.g., prune historical states
     * older than N days) may be added if memory becomes an issue.
     *
     * @param level       the server level
     * @param currentTick the current world tick
     */
    public void tickAll(ServerLevel level, long currentTick) {
        if (level == null) return;
        // Iterate over a snapshot to avoid ConcurrentModificationException
        // if a driver registers new opportunities mid-tick (rare but possible).
        for (OpportunityState state : List.copyOf(states.values())) {
            try {
                tickOne(state, level, currentTick);
            } catch (Exception ex) {
                dev.ergenverse.core.Ergenverse.LOGGER.error(
                    "[OpportunityRegistry] Error ticking opportunity {}: {}",
                    state.getOpportunityId(), ex.getMessage(), ex);
            }
        }
    }

    /** Dispatch a single state to its driver. */
    private void tickOne(OpportunityState state, ServerLevel level, long currentTick) {
        String driverType = state.getDriverType();
        if (driverType == null || driverType.isBlank()) driverType = "noop";

        switch (driverType) {
            case SpiritFruitTimeline.DRIVER_TYPE:
                SpiritFruitTimeline.advance(state, currentTick, level);
                break;
            case "noop":
            default:
                // Unknown driver — fall back to NoOp ctx so the FSM still advances.
                OpportunityFSM.tickOpportunity(state, currentTick, WorldSimContext.NoOpWorldSimContext);
                break;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Spatial queries — for player observation + rumor system
    // ═══════════════════════════════════════════════════════════════════

    /**
     * All opportunities within {@code radius} blocks of {@code center},
     * in ANY lifecycle (including HISTORICAL).
     *
     * @param center the query position
     * @param radius the search radius in blocks (Euclidean)
     * @return immutable list of matching states (possibly empty)
     */
    public List<OpportunityState> getOpportunitiesNear(BlockPos center, double radius) {
        if (center == null || radius <= 0) return List.of();
        double r2 = radius * radius;
        List<OpportunityState> result = new ArrayList<>();
        for (OpportunityState s : states.values()) {
            double dx = s.getPosX() - center.getX();
            double dy = s.getPosY() - center.getY();
            double dz = s.getPosZ() - center.getZ();
            if (dx * dx + dy * dy + dz * dz <= r2) {
                result.add(s);
            }
        }
        return List.copyOf(result);
    }

    /**
     * All HISTORICAL opportunities within {@code radius} blocks of
     * {@code center}. Used by the future rumor system (§6.11) to query
     * "what happened near here that NPCs might know about?".
     *
     * @param center the query position
     * @param radius the search radius in blocks (Euclidean)
     * @return immutable list of HISTORICAL states (possibly empty)
     */
    public List<OpportunityState> getHistoricalOpportunitiesNear(BlockPos center, double radius) {
        if (center == null || radius <= 0) return List.of();
        double r2 = radius * radius;
        List<OpportunityState> result = new ArrayList<>();
        for (OpportunityState s : states.values()) {
            if (s.getLifecycle() != OpportunityLifecycle.HISTORICAL) continue;
            double dx = s.getPosX() - center.getX();
            double dy = s.getPosY() - center.getY();
            double dz = s.getPosZ() - center.getZ();
            if (dx * dx + dy * dy + dz * dz <= r2) {
                result.add(s);
            }
        }
        return List.copyOf(result);
    }

    /**
     * All non-historical (live) opportunities within {@code radius} blocks.
     * Convenience method for player-observation queries — filters out
     * historical memory.
     */
    public List<OpportunityState> getLiveOpportunitiesNear(BlockPos center, double radius) {
        if (center == null || radius <= 0) return List.of();
        double r2 = radius * radius;
        List<OpportunityState> result = new ArrayList<>();
        for (OpportunityState s : states.values()) {
            if (s.getLifecycle() == OpportunityLifecycle.HISTORICAL) continue;
            double dx = s.getPosX() - center.getX();
            double dy = s.getPosY() - center.getY();
            double dz = s.getPosZ() - center.getZ();
            if (dx * dx + dy * dy + dz * dz <= r2) {
                result.add(s);
            }
        }
        return List.copyOf(result);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  SavedData — NBT persistence
    // ═══════════════════════════════════════════════════════════════════

    /** Re-bind the dirty callback on every loaded state. Called after load. */
    private void rebindDirtyCallbacks() {
        for (OpportunityState s : states.values()) {
            s.setDirtyCallback(this::setDirty);
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compound) {
        compound.putInt("data_version", CURRENT_VERSION);
        ListTag list = new ListTag();
        for (OpportunityState s : states.values()) {
            list.add(s.serializeNBT());
        }
        compound.put("states", list);
        return compound;
    }

    /** Load a registry from NBT (the inverse of {@link #save}). */
    public static OpportunityRegistry load(CompoundTag compound) {
        OpportunityRegistry reg = new OpportunityRegistry();
        int version = compound.getInt("data_version");
        if (version < 1) {
            dev.ergenverse.core.Ergenverse.LOGGER.info(
                "[OpportunityRegistry] Migrating save from v{} to v{}", version, CURRENT_VERSION);
        }
        ListTag list = compound.getList("states", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            try {
                OpportunityState s = OpportunityState.deserializeNBT(list.getCompound(i));
                reg.states.put(s.getOpportunityId(), s);
            } catch (Exception ex) {
                dev.ergenverse.core.Ergenverse.LOGGER.error(
                    "[OpportunityRegistry] Failed to deserialize opportunity at index {}: {}",
                    i, ex.getMessage());
            }
        }
        dev.ergenverse.core.Ergenverse.LOGGER.info(
            "[OpportunityRegistry] Loaded {} opportunity states (v{})",
            reg.states.size(), version);
        return reg;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Diagnostics
    // ═══════════════════════════════════════════════════════════════════

    /** Human-readable status for debug commands. */
    public String getStatusReport() {
        long dormant = countByLifecycle(OpportunityLifecycle.DORMANT);
        long forming = countByLifecycle(OpportunityLifecycle.FORMING);
        long contested = countByLifecycle(OpportunityLifecycle.CONTESTED);
        long resolved = countByLifecycle(OpportunityLifecycle.RESOLVED);
        long historical = countByLifecycle(OpportunityLifecycle.HISTORICAL);
        return String.format(
            "OpportunityRegistry{v=%d, total=%d, dormant=%d, forming=%d, contested=%d, resolved=%d, historical=%d}",
            CURRENT_VERSION, states.size(), dormant, forming, contested, resolved, historical);
    }
}
