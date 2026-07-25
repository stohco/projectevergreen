package dev.ergenverse.runtime.delta;

import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.runtime.PackedPos;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * WorldDelta — the unified language of every change in the simulation.
 *
 * <p><b>Architectural directive (CRON-69, point 1):</b> "I would not think in
 * terms of 'BlockDelta'. I'd think in terms of <b>WorldDelta</b>. … Eventually
 * you'll need deltas for: Block changes, Actor state, Relationships, Ownership,
 * Inventories, Cultivation, Weather, Politics, Markets, Rumors, Knowledge,
 * Memory, Territory, Spirit veins, Formation integrity. If everything starts
 * becoming its own delta system … you'll end up with fifteen different
 * persistence mechanisms. Instead I'd introduce something like:
 * <pre>
 *   interface WorldDelta {
 *       void apply(WorldRuntime runtime);
 *       void serialize(NbtCompound tag);
 *       UUID id();
 *   }
 * </pre>
 * Then {@code BlockPlacedDelta implements WorldDelta},
 * {@code RelationshipChangedDelta implements WorldDelta}, etc. Now everything
 * in the simulation speaks one language."
 *
 * <p>Every mutation of the world — a block change, an actor moving, a
 * relationship shifting, a memory forming — is a {@code WorldDelta}. Deltas
 * are appended to a {@link WorldDeltaStore} (the simulation's journal), applied
 * to the live world via {@link #apply}, and serialized to NBT for persistence.
 * The {@link Provenance} on each delta records <i>where the change came from</i>
 * (CANON &mdash; never, since canon is immutable; SIMULATION &mdash; the world
 * evolving; PLAYER &mdash; the player acting).
 *
 * <p><b>Deserialization contract.</b> Each concrete delta declares a stable
 * {@link #type()} string. On load, {@link WorldDeltaCodec} looks up the factory
 * registered for that type string and reconstructs the delta from the stored
 * NBT. Adding a new delta kind = register a new type string + factory. No
 * central switch statement to edit.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public interface WorldDelta {

    /**
     * The stable discriminator used to pick the right deserializer on load.
     * Must be unique across all WorldDelta implementations. Examples:
     * {@code "block_change"}, {@code "actor_move"}, {@code "relationship"}.
     */
    String type();

    /**
     * A unique id for this delta event. Used for idempotency (so the same delta
     * is never applied twice), for log correlation, and for deduplication when
     * the same change is recorded by multiple subsystems. For deterministic
     * changes (e.g. a block edit at a fixed position) the id may be derived
     * from the position; for event-driven changes, a fresh UUID is fine.
     */
    UUID id();

    /**
     * Where this change came from — SIMULATION or PLAYER. Never CANON (canon is
     * immutable; you never produce a CANON delta at runtime).
     */
    Provenance provenance();

    /**
     * Apply this delta's effect to the live world. Called when the delta is
     * first recorded (so the live world reflects it immediately) and again on
     * chunk reload if the affected region was unloaded and is being rematerialized.
     *
     * <p>Implementations must be <b>idempotent</b> — applying the same delta
     * twice must yield the same world state.
     */
    void apply(WorldRuntime runtime);

    /**
     * Serialize this delta into {@code tag}. The {@code tag} will already
     * contain the {@link #type()} and {@link #id()}; the implementation writes
     * only its own payload. Paired with the factory registered in
     * {@link WorldDeltaCodec}.
     */
    void serialize(CompoundTag tag);
}
