package dev.ergenverse.runtime.delta;

import dev.ergenverse.runtime.PackedPos;
import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.WorldRuntime;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * EntityPlacementDelta — a player or simulation placement/removal of a
 * decoration entity (ItemFrame, Painting) at a block position.
 *
 * <p><b>Architectural directive (CRON-69, point 1):</b> "Eventually you'll
 * need deltas for: Block changes, Actor state, Relationships, Ownership,
 * Inventories, Cultivation, Weather, Politics, Markets, Rumors, Knowledge,
 * Memory, Territory, Spirit veins, Formation integrity. If everything starts
 * becoming its own delta system … you'll end up with fifteen different
 * persistence mechanisms. Instead I'd introduce something like WorldDelta."
 *
 * <p>CRON-COMPLETIONIST-78 introduces this delta kind to close the
 * <b>placement-direction provenance leak</b> identified in CRON-76 critique
 * #10 and deferred twice (CRON-76, CRON-77). Prior to this round, the
 * journal tracked only BLOCK changes — player-placed ItemFrames and
 * Paintings were not journaled, so the journal was NOT the single source of
 * truth for player state (vanilla chunk NBT was). This delta kind brings
 * entity placements under the same unified language as block changes.
 *
 * <p><b>Actions.</b> A single delta kind with two actions:
 * <ul>
 *   <li>{@link Action#PLACE} — the player placed an entity at (x, y, z).
 *       The {@code entityNbt} carries the full entity state (facing, item,
 *       rotation, variant) via {@code entity.saveWithoutId(tag)}.</li>
 *   <li>{@link Action#REMOVE} — the player removed an entity at (x, y, z)
 *       (e.g., by attacking it directly). The {@code entityNbt} is null;
 *       the delta just marks "the entity here is gone".</li>
 * </ul>
 * Latest-wins by (position, provenance): re-recording at the same position
 * overwrites the prior delta. So place→remove→place at the same position
 * yields a single PLACE delta with the latest NBT.
 *
 * <p><b>Idempotency.</b> {@link #apply} is idempotent: for PLACE, it checks
 * if an entity already exists at the position (vanilla may have re-created
 * it from chunk NBT) and skips if so; for REMOVE, it finds any entity at
 * the position and discards it (no-op if none).
 *
 * <p><b>Interaction with CRON-76 cascade.</b> When a player breaks the
 * support block of a canon ItemFrame, the CRON-76 cascade records a PLAYER
 * "air" BLOCK delta at the entity's position. When a player attacks a canon
 * ItemFrame directly, this delta kind records a PLAYER REMOVE ENTITY delta
 * at the entity's position. Both cases cause the canon builder's
 * {@code hasPlayerOrSimulationDelta} guard to skip re-placement — the
 * block-index check catches the cascade, the entity-index check catches the
 * direct attack.
 *
 * <p><b>Interaction with canon entity re-spawn.</b> When the chunk
 * materializer runs on reload, canon builders re-place canon ItemFrames via
 * {@code placeItemFrame}. The {@code hasPlayerOrSimulationDelta} guard
 * (updated in CRON-78 to also check the entity index) prevents re-placement
 * at positions where the player has placed OR removed an entity. This
 * closes the long-standing leak where canon ItemFrames in Heng Yue Sect
 * (5 sites) would re-spawn after the player removed them — the
 * {@code HengYueSectBuilder.placeItemFrame} method was missing the guard
 * (only {@code WangFamilyVillageBuilder.placeItemFrame} had it, from
 * CRON-71).
 *
 * <p><b>Storage.</b> Like {@link BlockChangeDelta}, this delta is
 * position-indexed in {@link WorldDeltaStore} (via the {@code entityIndex}
 * map added in CRON-78) for O(1) lookup and O(chunk) replay. The
 * "one delta language" contract is preserved without sacrificing the
 * performance of the packed-long map.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class EntityPlacementDelta implements WorldDelta {

    public static final String TYPE = "entity_placement";

    /** The two actions a single delta kind can represent. */
    public enum Action {
        /** Player or simulation placed an entity at (x, y, z). */
        PLACE,
        /** Player or simulation removed an entity at (x, y, z). */
        REMOVE
    }

    private final int x, y, z;
    private final Action action;
    private final CompoundTag entityNbt;  // null if REMOVE, non-null if PLACE
    private final Provenance provenance;
    private final UUID id;

    /**
     * @param x          block X of the entity's hanging position
     * @param y          block Y
     * @param z          block Z
     * @param action     PLACE or REMOVE
     * @param entityNbt  full entity NBT (via {@code entity.saveWithoutId}) for PLACE;
     *                   null for REMOVE
     * @param provenance SIMULATION or PLAYER (never CANON)
     */
    public EntityPlacementDelta(int x, int y, int z, Action action,
                                  CompoundTag entityNbt, Provenance provenance) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.action = action;
        this.entityNbt = entityNbt;
        this.provenance = provenance;
        // Deterministic id from position + provenance so re-recording at the
        // same position produces the same id (latest-wins overwrite in store).
        this.id = deriveId(x, y, z, provenance);
    }

    private EntityPlacementDelta(int x, int y, int z, Action action,
                                   CompoundTag entityNbt, Provenance provenance, UUID id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.action = action;
        this.entityNbt = entityNbt;
        this.provenance = provenance;
        this.id = id;
    }

    /**
     * Deterministic id from position + provenance.
     *
     * <p>Uses the same scheme as {@link BlockChangeDelta#deriveId} but sets
     * bit 0 of the high word to distinguish entity deltas from block deltas
     * (defensive — the store uses position-based indexing, not id-based, but
     * this keeps the ids globally unique for log correlation).
     */
    private static UUID deriveId(int x, int y, int z, Provenance p) {
        long packed = PackedPos.pack(x, y, z);
        long prov = (long) p.ordinal() << 62;
        long high = prov | (packed >>> 2) | 0x1L;  // bit 0 set for entity deltas
        long low = packed & 0x3FFFFFFFFFFFFFFFL;
        return new UUID(high, low);
    }

    public int x() { return x; }
    public int y() { return y; }
    public int z() { return z; }
    public Action action() { return action; }
    public CompoundTag entityNbt() { return entityNbt; }

    @Override public String type() { return TYPE; }
    @Override public UUID id() { return id; }
    @Override public Provenance provenance() { return provenance; }

    @Override
    public void apply(WorldRuntime runtime) {
        // Defer to the runtime's world facade so the live Minecraft world is
        // updated through the same channel a freshly-recorded change uses.
        runtime.world().applyEntityPlacement(x, y, z, action, entityNbt, provenance);
    }

    @Override
    public void serialize(CompoundTag tag) {
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        tag.putByte("action", (byte) action.ordinal());
        if (entityNbt != null) {
            tag.put("nbt", entityNbt);
        }
    }

    /** Factory used by {@link WorldDeltaCodec} to rebuild from NBT. */
    public static EntityPlacementDelta deserialize(CompoundTag tag) {
        int x = tag.getInt("x");
        int y = tag.getInt("y");
        int z = tag.getInt("z");
        Action action = Action.values()[tag.getByte("action") & 0xFF];
        CompoundTag nbt = tag.contains("nbt") ? tag.getCompound("nbt") : null;
        Provenance p = WorldDeltaCodec.readProvenance(tag);
        UUID id = WorldDeltaCodec.readId(tag);
        return new EntityPlacementDelta(x, y, z, action, nbt, p, id);
    }

    static {
        WorldDeltaCodec.register(TYPE, EntityPlacementDelta::deserialize);
    }
}
