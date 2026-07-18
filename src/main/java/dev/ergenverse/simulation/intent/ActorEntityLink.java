package dev.ergenverse.simulation.intent;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ActorEntityLink — the bridge between the simulation-layer {@link Actor} and
 * the Minecraft-layer {@link EntityCultivator}.
 *
 * <p>THE CENTRAL DEFICIT (from cron): "NPC runtime behavior is grade F.
 * ActorTickLoop.tick() is NEVER called — 5,800 LOC of cognition is DEAD CODE.
 * The missing bridge: Decision → Intent → Planner → Physical Tasks →
 * Minecraft Goals → World Changes."
 *
 * <p>This class is one half of that bridge. It maintains a bidirectional
 * mapping between Actor IDs (simulation) and EntityCultivator UUIDs
 * (Minecraft), so that:
 * <ul>
 *   <li>The {@link CognitionDrivenGoal} on an EntityCultivator can look up
 *       the Actor's active Intent and execute its decomposed tasks.</li>
 *   <li>The {@link dev.ergenverse.simulation.actor.ActorTickLoop} can find
 *       the Minecraft entity for an Actor (to sync position, to apply
 *       goals, to check if the entity is still loaded).</li>
 * </ul>
 *
 * <h2>Registration lifecycle</h2>
 * <ul>
 *   <li>When an {@link EntityCultivator} materializes (spawn or chunk reload),
 *       it calls {@link #onEntitySpawn} with its character_id and UUID.</li>
 *   <li>If a matching Actor exists in {@link ActorRegistry}, the link is
 *       established. If not, a new Actor is created and registered (the
 *       entity "brings its simulation self into existence").</li>
 *   <li>When the entity unloads (chunk unload or death),
 *       {@link #onEntityUnload} is called. The link is severed, but the
 *       Actor remains in the registry (so the simulation can continue
 *       simulating it at territory level even when the entity isn't
 *       rendered).</li>
 * </ul>
 *
 * <h2>Thread safety</h2>
 * <p>The link maps are {@link ConcurrentHashMap} because entity spawn/unload
 * events can fire from the server tick thread while the ActorTickLoop reads
 * from a different code path. The maps must tolerate concurrent access.
 *
 * <p><b>Provenance: INFERRED.</b> The bridge pattern is a standard
 * simulation↔renderer separation. The specific linkage points
 * (materialization, chunk unload, death) are dictated by Minecraft's
 * entity lifecycle.
 */
public final class ActorEntityLink {

    /** Map: Actor ID → EntityCultivator UUID (simulation → Minecraft). */
    private static final ConcurrentHashMap<String, UUID> ACTOR_TO_ENTITY = new ConcurrentHashMap<>();

    /** Map: EntityCultivator UUID → Actor ID (Minecraft → simulation). */
    private static final ConcurrentHashMap<UUID, String> ENTITY_TO_ACTOR = new ConcurrentHashMap<>();

    private ActorEntityLink() {}

    /**
     * Called when an EntityCultivator materializes in the world.
     *
     * <p>If the entity has a non-empty character_id, look up (or create)
     * the corresponding Actor and establish the bidirectional link.
     *
     * @param entity the materialized EntityCultivator
     */
    public static void onEntitySpawn(EntityCultivator entity) {
        if (entity == null) return;
        String characterId = entity.getCharacterId();
        if (characterId == null || characterId.isEmpty()) return;

        UUID entityUuid = entity.getUUID();

        // Look up existing Actor
        Actor actor = ActorRegistry.get(characterId);
        if (actor == null) {
            // Actor doesn't exist yet — create a minimal one.
            // The entity's canon data (realm, name) will be read by the
            // cognition pipeline as it ticks. For now, we just need a
            // shell so the ActorTickLoop can find it.
            actor = new dev.ergenverse.simulation.actor.Actor(
                    characterId,
                    dev.ergenverse.simulation.actor.ActorType.NPC,
                    "canon:" + characterId,
                    "NOVEL_STATEMENT"
            );
            actor.displayName = entity.getDisplayNameCn();
            actor.cognition = new dev.ergenverse.simulation.cognition.Ontology(
                    new dev.ergenverse.simulation.cognition.Ontology.Identity(
                            characterId, entity.getDisplayNameCn(), "npc"));
            // Default Dao identity — canon-specific overrides can be applied later
            actor.cognition.daoIdentity = dev.ergenverse.simulation.cognition.DaoIdentity.SEEKING_DAO;
            // Grant WANDER capability so the Planner can generate movement-based actions
            actor.grant(dev.ergenverse.simulation.actor.Capability.WANDER);
            // Set position from entity
            actor.blockX = entity.blockPosition().getX();
            actor.blockY = entity.blockPosition().getY();
            actor.blockZ = entity.blockPosition().getZ();
            ActorRegistry.register(actor);
            Ergenverse.LOGGER.info("[ActorEntityLink] Created Actor for entity '{}' ({})",
                    characterId, entityUuid);
        }

        // Establish bidirectional link
        ACTOR_TO_ENTITY.put(characterId, entityUuid);
        ENTITY_TO_ACTOR.put(entityUuid, characterId);

        // Sync entity position to Actor
        actor.blockX = entity.blockPosition().getX();
        actor.blockY = entity.blockPosition().getY();
        actor.blockZ = entity.blockPosition().getZ();

        Ergenverse.LOGGER.debug("[ActorEntityLink] Linked actor '{}' ↔ entity {}",
                characterId, entityUuid);
    }

    /**
     * Called when an EntityCultivator unloads (chunk unload or death).
     *
     * <p>Severs the link but does NOT unregister the Actor — the simulation
     * continues at territory level even when the entity isn't rendered.
     *
     * @param entity the unloading EntityCultivator
     */
    public static void onEntityUnload(EntityCultivator entity) {
        if (entity == null) return;
        UUID entityUuid = entity.getUUID();
        String actorId = ENTITY_TO_ACTOR.remove(entityUuid);
        if (actorId != null) {
            ACTOR_TO_ENTITY.remove(actorId);
            Ergenverse.LOGGER.debug("[ActorEntityLink] Unlinked actor '{}' ↔ entity {}",
                    actorId, entityUuid);
        }
    }

    /**
     * Get the EntityCultivator for an Actor, if loaded in the given level.
     *
     * @param actorId the Actor ID (character_id)
     * @param level   the server level to search in
     * @return the EntityCultivator, or null if not linked or not loaded
     */
    public static EntityCultivator getEntity(String actorId, ServerLevel level) {
        if (actorId == null || level == null) return null;
        UUID uuid = ACTOR_TO_ENTITY.get(actorId);
        if (uuid == null) return null;
        Entity entity = level.getEntity(uuid);
        return entity instanceof EntityCultivator ec ? ec : null;
    }

    /**
     * Get the Actor ID linked to a specific entity UUID.
     *
     * @param entityUuid the entity's UUID
     * @return the Actor ID, or null if not linked
     */
    public static String getActorId(UUID entityUuid) {
        if (entityUuid == null) return null;
        return ENTITY_TO_ACTOR.get(entityUuid);
    }

    /**
     * Is the given Actor currently linked to a loaded entity?
     */
    public static boolean isLinked(String actorId) {
        return actorId != null && ACTOR_TO_ENTITY.containsKey(actorId);
    }

    /**
     * Sync the entity's position back to the Actor (called each tick by
     * the ActorTickLoop for linked actors).
     *
     * @param actorId the Actor ID
     * @param level   the server level
     */
    public static void syncPosition(String actorId, ServerLevel level) {
        EntityCultivator entity = getEntity(actorId, level);
        if (entity == null) return;
        Actor actor = ActorRegistry.get(actorId);
        if (actor == null) return;
        actor.blockX = entity.blockPosition().getX();
        actor.blockY = entity.blockPosition().getY();
        actor.blockZ = entity.blockPosition().getZ();
    }

    /** Number of active (loaded) entity links. */
    public static int linkedCount() {
        return ACTOR_TO_ENTITY.size();
    }
}
