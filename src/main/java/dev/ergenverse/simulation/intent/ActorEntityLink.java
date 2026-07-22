package dev.ergenverse.simulation.intent;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.cognition.PersonalityModel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

            // ── Wire personality traits from NPC JSON data ──
            // Per Article XLI: personality drives behavior. Without this,
            // all NPCs have default 0.5 traits, and ActivityAssigner's
            // personality-gated interruption conditions produce identical
            // behavior for every NPC. The "cautious cultivator observes
            // predators" Canon Experience requires actual trait diversity.
            loadPersonalityFromData(characterId, actor.cognition.personality);
            // ── Wire desires from NPC JSON data (Art XXXI) ──
            // Without desires, the world waits. With desires, NPCs
            // generate SOCIAL goals through the existing pipeline.
            loadDesiresFromData(characterId, actor.cognition.desires);
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

    /**
     * Load personality traits from the NPC's JSON data file into the
     * actor's PersonalityModel.
     *
     * <p>NPC data files may contain a {@code "personality_traits"} object
     * mapping trait names (from {@link PersonalityModel#CANONICAL_TRAITS})
     * to numeric values in [0,1]. For example:
     * <pre>{@code
     * "personality_traits": {
     *   "caution": 0.82,
     *   "curiosity": 0.25,
     *   "patience": 0.70
     * }
     * }</pre>
     *
     * <p>If the NPC has no {@code personality_traits} field, the
     * PersonalityModel keeps its default 0.5 values. If a trait value
     * is outside [0,1], it is clamped.
     *
     * <p>Per Article XLI: this is not special-casing any character.
     * Every NPC with a data file gets the same treatment.
     *
     * @param characterId the NPC's canon character ID
     * @param personality the PersonalityModel to populate
     */
    private static void loadPersonalityFromData(String characterId, PersonalityModel personality) {
        JsonObject data = dev.ergenverse.simulation.WorldStateDataLoader.getEntry("npcs", characterId);
        if (data == null) return;

        JsonElement ptElement = data.get("personality_traits");
        if (ptElement == null || !ptElement.isJsonObject()) return;

        JsonObject pt = ptElement.getAsJsonObject();
        int loaded = 0;
        for (String traitName : PersonalityModel.CANONICAL_TRAITS) {
            JsonElement val = pt.get(traitName);
            if (val != null && val.isJsonPrimitive() && val.getAsJsonPrimitive().isNumber()) {
                double v = val.getAsDouble();
                // Set for both "default" context (used by ActivityAssigner)
                // and the trait's own name as context (used by DecisionEngine).
                personality.set(traitName, "default", v);
                personality.set(traitName, traitName, v);
                loaded++;
            }
        }
        if (loaded > 0) {
            Ergenverse.LOGGER.info("[ActorEntityLink] Loaded {} personality traits for {}",
                    loaded, characterId);
        }
    }

    /**
     * Load desire-state from NPC JSON data into the Ontology's desire list.
     *
     * <p>Per Article XXXI: "Every NPC every cycle asks: 'Do I want something
     * from someone else right now?'" The JSON {@code "desires"} array provides
     * the answer. Each entry becomes a {@link DesireState} that the
     * GoalGenerator reads to produce SOCIAL goals.
     *
     * <p>Per Article XLI: this is not special-casing any character.
     * Every NPC with desire data gets the same treatment.
     *
     * @param characterId the NPC's canon character ID
     * @param desires     the Ontology's desire list to populate
     */
    private static void loadDesiresFromData(String characterId,
            java.util.List<dev.ergenverse.simulation.cognition.DesireState> desires) {
        JsonObject data = dev.ergenverse.simulation.WorldStateDataLoader.getEntry("npcs", characterId);
        if (data == null) return;

        JsonElement desElement = data.get("desires");
        if (desElement == null || !desElement.isJsonArray()) return;

        var arr = desElement.getAsJsonArray();
        for (var elem : arr) {
            if (!elem.isJsonObject()) continue;
            JsonObject d = elem.getAsJsonObject();
            desires.add(new dev.ergenverse.simulation.cognition.DesireState(
                    jsonStr(d, "id", ""),
                    jsonStr(d, "what", ""),
                    jsonStr(d, "target", ""),
                    jsonStr(d, "why", ""),
                    jsonStr(d, "social_engine", "request"),
                    jsonDouble(d, "urgency", 0.5),
                    jsonLong(d, "cooldown_ticks", 24000),
                    -1L, // lastFulfilledTick — starts unfulfilled
                    jsonStr(d, "source", "unknown"),
                    jsonStr(d, "line", ""),
                    jsonStr(d, "mode", "line")
            ));
        }
        if (!desires.isEmpty()) {
            Ergenverse.LOGGER.info("[ActorEntityLink] Loaded {} desires for {}",
                    desires.size(), characterId);
        }
    }

    private static String jsonStr(JsonObject obj, String key, String fallback) {
        JsonElement e = obj.get(key);
        return (e != null && e.isJsonPrimitive()) ? e.getAsString() : fallback;
    }

    private static double jsonDouble(JsonObject obj, String key, double fallback) {
        JsonElement e = obj.get(key);
        return (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber())
                ? e.getAsDouble() : fallback;
    }

    private static long jsonLong(JsonObject obj, String key, long fallback) {
        JsonElement e = obj.get(key);
        return (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber())
                ? e.getAsLong() : fallback;
    }
}
