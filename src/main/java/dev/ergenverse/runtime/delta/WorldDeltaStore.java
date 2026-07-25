package dev.ergenverse.runtime.delta;

import dev.ergenverse.runtime.PackedPos;
import dev.ergenverse.runtime.Provenance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WorldDeltaStore — the simulation's journal of every change since day 0.
 *
 * <p><b>Architectural directive (CRON-69, point 1):</b> the unified delta
 * language means <i>one</i> persistence mechanism, not fifteen. This store is
 * that mechanism. Every {@link WorldDelta} ever recorded — block changes, actor
 * moves, relationship shifts, memories formed — lives here, serialized to a
 * single NBT list on world save, and replayed on load.
 *
 * <p><b>Block-change indexing.</b> Although every block change is a
 * {@code WorldDelta} (for the one-language contract), they are additionally
 * indexed <i>per provenance</i> by packed position, so the
 * {@link dev.ergenverse.runtime.layer.PlayerLayer} and
 * {@link dev.ergenverse.runtime.layer.SimulationLayer} can answer
 * {@code getBlock(pos)} in O(1) and {@code getChangesInChunk} in O(changes-in-layer)
 * without scanning the whole journal. Re-recording a change at the same
 * position + provenance overwrites the prior entry (latest-wins within a
 * provenance), so the index never grows unbounded from repeat edits to one spot.
 *
 * <p><b>Non-block deltas.</b> Future delta kinds (actor moves, relationships,
 * memories) are appended to a flat journal list and serialized alongside block
 * changes. They are not position-indexed (their queries are domain-specific).
 *
 * <p><b>Persistence.</b> {@link #serialize(CompoundTag)} writes the whole
 * journal; {@link #deserialize(CompoundTag)} rebuilds it. The
 * {@link dev.ergenverse.runtime.persist.WorldDeltaSavedData} wires this into
 * Minecraft's per-level {@code SavedData} so the journal is saved and loaded
 * with the world automatically. On a fresh save the journal is empty — every
 * playthrough begins from pure canon. The blueprint is never rewritten.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class WorldDeltaStore {

    /** Per-provenance index of the latest block change at each packed position. */
    private final Map<Provenance, Map<Long, BlockChangeDelta>> blockIndex = new EnumMap<>(Provenance.class);

    /** Journal of all non-block deltas (actor moves, relationships, memories, …). */
    private final List<WorldDelta> journal = new ArrayList<>();

    public WorldDeltaStore() {
        for (Provenance p : Provenance.values()) {
            blockIndex.put(p, new HashMap<>());
        }
    }

    // ── Recording ───────────────────────────────────────────────────────

    /**
     * Record a delta into the journal. If it is a {@link BlockChangeDelta},
     * also update the per-provenance position index (overwriting any prior
     * change at the same position + provenance). Non-block deltas are appended.
     *
     * <p>Idempotent for block changes: re-recording the same (pos, provenance)
     * just overwrites with the new state (which is usually identical).
     */
    public synchronized void record(WorldDelta delta) {
        if (delta.provenance() == Provenance.CANON) {
            // Canon is immutable — no runtime delta is ever CANON. Defensive guard.
            return;
        }
        if (delta instanceof BlockChangeDelta bcd) {
            blockIndex.get(bcd.provenance()).put(PackedPos.pack(bcd.x(), bcd.y(), bcd.z()), bcd);
        } else {
            journal.add(delta);
        }
    }

    // ── Block queries (used by the layers) ──────────────────────────────

    /** Latest block state recorded at pos for the given provenance, or null. */
    public synchronized String getBlock(int x, int y, int z, Provenance p) {
        BlockChangeDelta d = blockIndex.get(p).get(PackedPos.pack(x, y, z));
        return d == null ? null : d.blockState();
    }

    /** True if a block change is recorded at pos for the given provenance. */
    public synchronized boolean hasBlock(int x, int y, int z, Provenance p) {
        return blockIndex.get(p).containsKey(PackedPos.pack(x, y, z));
    }

    /** Immutable view of all block changes for a provenance (packed-pos → delta). */
    public synchronized Map<Long, BlockChangeDelta> blockChanges(Provenance p) {
        return Collections.unmodifiableMap(new HashMap<>(blockIndex.get(p)));
    }

    /** Number of block changes recorded for a provenance. */
    public synchronized int blockChangeCount(Provenance p) {
        return blockIndex.get(p).size();
    }

    /**
     * All block changes (across PLAYER + SIMULATION) whose position falls in
     * the given chunk. Used by the {@link dev.ergenverse.runtime.materialize.ChunkMaterializer}
     * to replay changes after canon blocks are placed.
     *
     * <p>O(total changes) linear scan per call — acceptable for play-driven
     * change volumes (thousands, not millions). The quadtree spatial index
     * extension (CRON-68 next-priority) will make this O(log n) when needed.
     */
    public synchronized List<BlockChangeDelta> getBlockChangesInChunk(int chunkX, int chunkZ) {
        int minX = chunkX * 16, minZ = chunkZ * 16;
        int maxX = minX + 15, maxZ = minZ + 15;
        List<BlockChangeDelta> out = new ArrayList<>();
        for (Provenance p : new Provenance[]{ Provenance.PLAYER, Provenance.SIMULATION }) {
            for (BlockChangeDelta d : blockIndex.get(p).values()) {
                if (d.x() >= minX && d.x() <= maxX && d.z() >= minZ && d.z() <= maxZ) {
                    out.add(d);
                }
            }
        }
        return out;
    }

    /** Total number of recorded deltas (block + non-block), for diagnostics. */
    public synchronized int size() {
        int n = journal.size();
        for (Provenance p : Provenance.values()) n += blockIndex.get(p).size();
        return n;
    }

    /** Wipe the journal (used when starting a brand-new save). */
    public synchronized void clear() {
        for (Provenance p : Provenance.values()) blockIndex.get(p).clear();
        journal.clear();
    }

    // ── NBT persistence ─────────────────────────────────────────────────

    /** Serialize the whole journal into {@code tag} under key {@code "deltas"}. */
    public synchronized CompoundTag serialize(CompoundTag tag) {
        ListTag list = new ListTag();
        // Block changes (PLAYER + SIMULATION; CANON is never recorded).
        for (Provenance p : new Provenance[]{ Provenance.PLAYER, Provenance.SIMULATION }) {
            for (BlockChangeDelta d : blockIndex.get(p).values()) {
                list.add(WorldDeltaCodec.toNbt(d));
            }
        }
        // Non-block deltas.
        for (WorldDelta d : journal) {
            list.add(WorldDeltaCodec.toNbt(d));
        }
        tag.put("deltas", list);
        return tag;
    }

    /** Rebuild the journal from {@code tag} (the companion of {@link #serialize}). */
    public synchronized void deserialize(CompoundTag tag) {
        clear();
        ListTag list = tag.getList("deltas", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            WorldDelta d = WorldDeltaCodec.fromNbt(list.getCompound(i));
            if (d != null) record(d);
        }
    }
}
