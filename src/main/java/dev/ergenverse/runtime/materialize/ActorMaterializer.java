package dev.ergenverse.runtime.materialize;

import dev.ergenverse.runtime.WorldRuntime;

import java.util.UUID;

/**
 * ActorMaterializer — the contract for materializing an actor (NPC or beast)
 * from the simulation state into a Minecraft entity.
 *
 * <p><b>Architectural directive (2026-07-25):</b> "NPC materialization is the
 * area I'd spend the next several cycles on. Not AI. Loading. For example,
 * instead of spawnEntity(...), I would literally name the API
 * materializeActor(UUID). Internally:
 * <pre>
 *   ActorRuntime
 *     ↓
 *   already exists
 *     ↓
 *   chunk loads
 *     ↓
 *   Entity created
 *     ↓
 *   links back to ActorRuntime
 * </pre>
 * When chunk unloads:
 * <pre>
 *   Entity destroyed
 *     ↓
 *   ActorRuntime continues existing
 * </pre>
 * That's the important part. The person never unloads. Only the renderable
 * body does."
 *
 * <p>The ActorMaterializer is called when a chunk loads and an actor's
 * canonical location falls within that chunk. It creates a Minecraft entity,
 * links it to the actor's simulation state (via canon UUID), and registers
 * it for serialization when the chunk unloads.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public interface ActorMaterializer {

    /**
     * Materialize an actor as a Minecraft entity.
     *
     * <p>Called when a chunk loads and an actor's canonical location is
     * within the chunk. The materializer:
     * <ol>
     *   <li>Looks up the actor's simulation state by canon UUID</li>
     *   <li>Creates a Minecraft entity of the appropriate type</li>
     *   <li>Links the entity to the simulation state (entity persistence
     *       UUID = canon UUID)</li>
     *   <li>Places the entity at its canonical location</li>
     *   <li>Returns the entity ID (or -1 if materialization failed)</li>
     * </ol>
     *
     * @param canonUuid the actor's permanent canon UUID (from {@link dev.ergenverse.runtime.CanonUUID})
     * @param runtime the WorldRuntime (provides actor simulation state)
     * @return the Minecraft entity ID of the materialized actor, or -1 on failure
     */
    int materializeActor(UUID canonUuid, WorldRuntime runtime);

    /**
     * Dematerialize an actor — serialize its current state and destroy the
     * Minecraft entity.
     *
     * <p>Called when a chunk unloads. The actor's simulation state is
     * serialized (position, inventory, cultivation progress, memories, etc.)
     * and the Minecraft entity is destroyed. The actor CONTINUES to exist
     * in the simulation — only the renderable body is gone.
     *
     * @param canonUuid the actor's permanent canon UUID
     * @param runtime the WorldRuntime
     * @return true if the actor was successfully dematerialized (state saved)
     */
    boolean dematerializeActor(UUID canonUuid, WorldRuntime runtime);

    /**
     * Check if an actor is currently materialized (has a live Minecraft entity).
     *
     * @param canonUuid the actor's permanent canon UUID
     * @param runtime the WorldRuntime
     * @return true if the actor has a live entity in the world
     */
    boolean isMaterialized(UUID canonUuid, WorldRuntime runtime);
}
