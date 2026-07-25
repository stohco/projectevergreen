package dev.ergenverse.runtime.delta;

import dev.ergenverse.runtime.PackedPos;
import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.WorldRuntime;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * BlockChangeDelta — a single block position set to a new state.
 *
 * <p><b>Architectural directive (CRON-69, point 4):</b> "I would NOT save
 * removed blocks. … Suppose blueprint contains Stone. Player mines it. Instead
 * of RemovedBlock, I'd store Air. Everything becomes Position → Current State.
 * No special removal object. Air is just another block state. Much simpler."
 *
 * <p>So there is exactly one block-change delta kind — this one. Mining a block
 * = {@code BlockChangeDelta(pos, "minecraft:air", PLAYER)}. Placing a block =
 * {@code BlockChangeDelta(pos, "minecraft:stone", PLAYER)}. A beast harvesting
 * an herb = {@code BlockChangeDelta(pos, "minecraft:air", SIMULATION)}. A sect
 * raising a wall = {@code BlockChangeDelta(pos, "ergenverse:spirit_stone_bricks", SIMULATION)}.
 * There is no {@code BlockRemovedDelta}. Air is a state like any other.
 *
 * <p><b>Idempotency.</b> {@link #apply} sets the block to the recorded state
 * (idempotent by construction — setting the same state twice is a no-op). The
 * delta's {@link #id()} is derived deterministically from the packed position +
 * provenance, so re-recording the same change at the same position produces the
 * same delta id and deduplicates in the {@link WorldDeltaStore}.
 *
 * <p><b>Storage.</b> Although every block change is a {@code WorldDelta} for
 * the unified-language contract, the {@link WorldDeltaStore} indexes
 * {@code BlockChangeDelta}s by packed position for O(1) lookup and O(chunk)
 * replay (see {@link WorldDeltaStore#getBlockChangesInChunk}). The
 * "one delta language" contract is preserved without sacrificing the
 * performance of the packed-long map.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class BlockChangeDelta implements WorldDelta {

    public static final String TYPE = "block_change";

    private final int x, y, z;
    private final String blockState;
    private final Provenance provenance;
    private final UUID id;

    /**
     * @param x          block X
     * @param y          block Y
     * @param z          block Z
     * @param blockState the new state as a registry id, e.g. {@code "minecraft:air"}
     * @param provenance SIMULATION or PLAYER (never CANON)
     */
    public BlockChangeDelta(int x, int y, int z, String blockState, Provenance provenance) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockState = blockState;
        this.provenance = provenance;
        // Deterministic id from position + provenance so re-recording the same
        // change deduplicates instead of appending a second journal entry.
        this.id = deriveId(x, y, z, provenance);
    }

    private BlockChangeDelta(int x, int y, int z, String blockState, Provenance provenance, UUID id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockState = blockState;
        this.provenance = provenance;
        this.id = id;
    }

    /** Deterministic id: the packed position's high bits encode provenance. */
    private static UUID deriveId(int x, int y, int z, Provenance p) {
        long packed = PackedPos.pack(x, y, z);
        long prov = (long) p.ordinal() << 62;
        long low = packed & 0x3FFFFFFFFFFFFFFFL;
        return new UUID(prov | (packed >>> 2), low);
    }

    public int x() { return x; }
    public int y() { return y; }
    public int z() { return z; }
    public String blockState() { return blockState; }

    @Override public String type() { return TYPE; }
    @Override public UUID id() { return id; }
    @Override public Provenance provenance() { return provenance; }

    @Override
    public void apply(WorldRuntime runtime) {
        // Defer to the runtime's world facade so the live Minecraft chunk is
        // updated through the same channel a freshly-recorded change uses.
        // The facade routes PLAYER/SIMULATION block writes to the live level
        // and to the delta store simultaneously.
        runtime.world().applyBlockChange(x, y, z, blockState, provenance);
    }

    @Override
    public void serialize(CompoundTag tag) {
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        tag.putString("state", blockState);
    }

    /** Factory used by {@link WorldDeltaCodec} to rebuild from NBT. */
    public static BlockChangeDelta deserialize(CompoundTag tag) {
        int x = tag.getInt("x");
        int y = tag.getInt("y");
        int z = tag.getInt("z");
        String state = tag.getString("state");
        Provenance p = WorldDeltaCodec.readProvenance(tag);
        UUID id = WorldDeltaCodec.readId(tag);
        return new BlockChangeDelta(x, y, z, state, p, id);
    }

    static {
        WorldDeltaCodec.register(TYPE, BlockChangeDelta::deserialize);
    }
}
