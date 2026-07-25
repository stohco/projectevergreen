package dev.ergenverse.runtime.delta;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * WorldDeltaCodec — registry mapping a {@link WorldDelta#type()} string to the
 * factory that deserializes it.
 *
 * <p>This is the deserialization half of the unified delta language. The
 * serialization half lives on each {@link WorldDelta#serialize} implementation.
 * Adding a new delta kind = register one factory here + implement
 * {@code serialize}/{@code apply} on the class. No central switch statement.
 *
 * <p>On load, a stored delta NBT looks like:
 * <pre>
 *   { type:"block_change", id:&lt;uuid&gt;, provenance:"PLAYER", &lt;payload&gt; }
 * </pre>
 * The codec reads {@code type}, looks up the factory, and the factory reads the
 * remaining payload to reconstruct the delta.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class WorldDeltaCodec {

    private WorldDeltaCodec() {}

    /** Function that rebuilds a WorldDelta from its payload tag (type/id/provenance already stripped). */
    public interface Factory extends Function<CompoundTag, WorldDelta> {}

    private static final Map<String, Factory> FACTORIES = new HashMap<>();

    /** Register a factory for a delta type. Idempotent; later registrations win. */
    public static void register(String type, Factory factory) {
        FACTORIES.put(type, factory);
    }

    /** Serialize a delta to a full NBT compound (type + id + provenance + payload). */
    public static CompoundTag toNbt(WorldDelta delta) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", delta.type());
        tag.putUUID("id", delta.id());
        tag.putString("provenance", delta.provenance().name());
        delta.serialize(tag);
        return tag;
    }

    /**
     * Deserialize a delta from a full NBT compound. Returns null (and logs) if
     * the type is unknown — a forward-compatibility guard so an old save with
     * an unregistered delta type never bricks the world.
     */
    public static WorldDelta fromNbt(CompoundTag tag) {
        String type = tag.getString("type");
        Factory f = FACTORIES.get(type);
        if (f == null) {
            Ergenverse.LOGGER.warn("[Ergenverse] WorldDeltaCodec: unknown delta type '{}' — skipping (forward-compat).", type);
            return null;
        }
        try {
            return f.apply(tag);
        } catch (Throwable t) {
            Ergenverse.LOGGER.error("[Ergenverse] WorldDeltaCodec: failed to deserialize delta type '{}': {}", type, t.getMessage());
            return null;
        }
    }

    /** Read the id field written by {@link #toNbt}. */
    public static UUID readId(CompoundTag tag) {
        return tag.hasUUID("id") ? tag.getUUID("id") : new UUID(0, 0);
    }

    /** Read the provenance field written by {@link #toNbt}. */
    public static dev.ergenverse.runtime.Provenance readProvenance(CompoundTag tag) {
        try {
            return dev.ergenverse.runtime.Provenance.valueOf(tag.getString("provenance"));
        } catch (IllegalArgumentException e) {
            return dev.ergenverse.runtime.Provenance.SIMULATION;
        }
    }
}
