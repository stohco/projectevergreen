package dev.ergenverse.perception;

import com.google.gson.JsonObject;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.network.ERNetwork;
import dev.ergenverse.network.PerceptionSyncS2CPacket;
import dev.ergenverse.simulation.WorldStateDataLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Server-side perception events — periodically computes perception data
 * for nearby EntityCultivator NPCs and syncs to each player.
 *
 * <p>Runs every {@link #SYNC_INTERVAL} server ticks (~2 seconds). For each
 * player, scans EntityCultivators within perception radius, loads canon
 * data from NPC JSON files (faction, dao_heart, personality, perception_tiers),
 * builds a rich {@link ObjectiveNature}, runs the {@link PerceptionEngine},
 * and sends a lightweight sync packet to the client.
 *
 * <p>Per the Prime Directive: the entities' ObjectiveNature never changes.
 * What changes is what the observer (player) understands. This event
 * system provides the bridge between the server's objective truth and
 * the client's perception-based rendering.
 *
 * <h2>v2 enhancement: canon NPC data enrichment</h2>
 * <p>Previously, ObjectiveNature was built with empty bloodline/sect/dao/titles.
 * Now, the system loads the NPC's JSON from {@code data/ergenverse/npcs/} and
 * extracts:
 * <ul>
 *   <li>{@code faction} → sect affiliation (e.g. "teng_family", "heng_yue_sect")</li>
 *   <li>{@code cultivation} → parsed to RealmId for accurate realm display</li>
 *   <li>{@code personality} → used as karmic context for Nascent Soul+ perception</li>
 *   <li>{@code dao_heart} → Dao affinities string for Soul Formation+ perception</li>
 *   <li>{@code relationship_to_wanglin} → karmic history for Nascent Soul+ perception</li>
 *   <li>{@code perception_tiers} → canon-accurate descriptions override the
 *       PerceptionEngine's generic text when available</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = "ergenverse", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PerceptionEvents {

    private PerceptionEvents() {}

    /** Sync interval in server ticks (40 ticks = 2 seconds). */
    private static final int SYNC_INTERVAL = 40;

    /**
     * Periodic perception sync: every SYNC_INTERVAL ticks, compute
     * perception data for nearby cultivator entities and send to players.
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        var server = event.getServer();
        if (server == null) return;
        long ticks = server.overworld().getGameTime();
        if (ticks % SYNC_INTERVAL != 0) return;

        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                syncPerceptionForPlayer(player, level);
            }
        }
    }

    /**
     * Compute perception data for all EntityCultivators near the given
     * player and send a sync packet.
     */
    private static void syncPerceptionForPlayer(ServerPlayer player, ServerLevel level) {
        var stateOpt = CultivationCapability.get(player);
        if (!stateOpt.isPresent()) return;
        CultivationState state = stateOpt.resolve().get();
        RealmId playerRealm = state.getCurrentRealm();

        // Perception radius based on player's realm
        // Mortals see 16 blocks, higher realms see further (anti-lag: capped at 64)
        int radius = Math.min(16 + playerRealm.order * 4, 64);

        List<PerceptionSyncS2CPacket.EntityPerception> entries = new ArrayList<>();

        for (EntityCultivator entity : level.getEntitiesOfClass(
                EntityCultivator.class, player.getBoundingBox().inflate(radius))) {

            // Skip entities too far away (in case AABB inflate caught extras)
            if (entity.distanceTo(player) > radius) continue;

            String characterId = entity.getCharacterId();
            if (characterId == null || characterId.isEmpty()) continue;

            // Build the ObjectiveNature server-side, enriched with canon NPC data
            ObjectiveNature nature = buildEnrichedNature(characterId, entity);

            // Build the observer context from the player's cultivation state
            ObserverContext observer = new ObserverContext(
                    playerRealm, state.getDaoComprehension(), false,
                    java.util.Set.of(), 1.0, false, false, false);

            // Run PerceptionEngine
            Objective objective = new Objective() {
                @Override
                public ObjectiveNature nature() { return nature; }
                @Override
                public PerceptionResult perceive(ObserverContext obs) {
                    return PerceptionEngine.perceive(this, obs);
                }
            };
            PerceptionResult result = PerceptionEngine.perceive(objective, observer);

            // Check for canon perception_tiers override from NPC JSON
            String perceivedName = result.perceivedName;
            String perceivedDesc = result.perceivedDescription;
            String canonOverride = getCanonPerceptionTier(characterId, playerRealm);
            if (canonOverride != null) {
                // Split on first newline if the override contains name\ndescription
                int nlIdx = canonOverride.indexOf('\n');
                if (nlIdx > 0) {
                    perceivedName = canonOverride.substring(0, nlIdx);
                    perceivedDesc = canonOverride.substring(nlIdx + 1);
                } else {
                    perceivedName = canonOverride;
                }
            }

            entries.add(new PerceptionSyncS2CPacket.EntityPerception(
                    entity.getId(), perceivedName,
                    perceivedDesc, result.concealed,
                    result.canInteract));
        }

        if (!entries.isEmpty()) {
            ERNetwork.getChannel().send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new PerceptionSyncS2CPacket(entries));
        }
    }

    // ─── Canon NPC Data Enrichment ─────────────────────────────────

    /**
     * Build an enriched ObjectiveNature for a given character.
     * Public static so that {@code DivineSensePulseC2SPacket} can reuse it
     * without duplicating the enrichment logic.
     *
     * @param characterId the canon character ID
     * @param entity       the entity (for display name fallback)
     * @param fallbackRealm the realm parsed from entity sync data
     * @return an enriched ObjectiveNature with canon data from NPC JSON
     */
    public static ObjectiveNature buildEnrichedNatureFor(String characterId,
                                                          EntityCultivator entity,
                                                          RealmId fallbackRealm) {
        String entityName = entity.getDisplayNameCn();
        String entityNameCn = entityName;
        if (entityName == null || entityName.isEmpty()) entityName = characterId;
        if (entityNameCn == null || entityNameCn.isEmpty()) entityNameCn = characterId;

        RealmId entityRealm = fallbackRealm;
        String sect = "";
        String origin = "";
        String karmicHistory = "";
        String daoAffinities = "";
        String titles = "";
        String bloodline = "";

        JsonObject npcData = WorldStateDataLoader.getEntry("npcs", characterId);
        if (npcData != null) {
            String jsonName = getString(npcData, "name");
            String jsonNameCn = getString(npcData, "nameCn");
            if (jsonName != null) entityName = jsonName;
            if (jsonNameCn != null) entityNameCn = jsonNameCn;

            String faction = getString(npcData, "faction");
            if (faction != null) sect = formatFactionName(faction);

            String cultivation = getString(npcData, "cultivation");
            if (cultivation != null) {
                RealmId parsedCultivation = parseCultivationString(cultivation);
                if (parsedCultivation != null && parsedCultivation != RealmId.MORTAL) {
                    entityRealm = parsedCultivation;
                }
            }

            String personality = getString(npcData, "personality");
            if (personality != null) origin = personality;

            String relWangLin = getString(npcData, "relationship_to_wanglin");
            if (relWangLin != null) karmicHistory = relWangLin;

            daoAffinities = extractDaoAffinities(npcData);

            String npcType = getString(npcData, "type");
            if (npcType != null && !npcType.equals("generic")) {
                titles = npcType.replace("_", " ");
            }
        }

        return ObjectiveNature.cultivator(
                entityName, entityNameCn, entityRealm,
                bloodline, origin, karmicHistory,
                dev.ergenverse.canon.CanonEngine.Confidence.NOVEL_STATEMENT,
                "npc:" + characterId,
                daoAffinities, titles, sect
        );
    }

    // ─── Private helpers ───────────────────────────────────────────

    /**
     * Build an enriched ObjectiveNature from an EntityCultivator by loading
     * its canon NPC JSON data. Delegates to {@link #buildEnrichedNatureFor}.
     */
    private static ObjectiveNature buildEnrichedNature(String characterId, EntityCultivator entity) {
        RealmId entityRealm = parseRealm(entity.getCultivationRealm());
        return buildEnrichedNatureFor(characterId, entity, entityRealm);
    }

    /**
     * Get a canon-accurate perception description from the NPC's
     * {@code perception_tiers} field in the JSON data.
     *
     * <p>The JSON format is:
     * <pre>
     *   "perception_tiers": {
     *     "mortal": "A quiet, still figure...",
     *     "qi_condensation": "A cultivator. His Qi is compressed...",
     *     ...
     *   }
     * </pre>
     *
     * <p>Returns the description for the closest matching tier, or null
     * if no canon description is available.
     */
    private static String getCanonPerceptionTier(String characterId, RealmId observerRealm) {
        JsonObject npcData = WorldStateDataLoader.getEntry("npcs", characterId);
        if (npcData == null || !npcData.has("perception_tiers")) return null;

        JsonObject tiers = npcData.getAsJsonObject("perception_tiers");
        if (tiers == null) return null;

        // Map observer realm to the perception_tiers key
        PerceptionTier tier = PerceptionTier.fromRealm(observerRealm);
        String tierKey = perceptionTierKey(tier);

        // Try exact match first
        if (tiers.has(tierKey)) {
            return tiers.get(tierKey).getAsString();
        }

        // Try the specific realm name (e.g. "core_formation" for Foundation tier)
        String realmKey = observerRealm.name().toLowerCase();
        if (tiers.has(realmKey)) {
            return tiers.get(realmKey).getAsString();
        }

        return null;
    }

    /** Map a PerceptionTier to the JSON key used in perception_tiers. */
    private static String perceptionTierKey(PerceptionTier tier) {
        switch (tier) {
            case MORTAL: return "mortal";
            case QI_CONDENSATION: return "qi_condensation";
            case FOUNDATION: return "foundation";
            case NASCENT_SOUL: return "nascent_soul";
            case SOUL_FORMATION: return "soul_formation";
            case ASCENDANT: return "ascendant";
            case TRANSCENDENCE: return "transcendence";
            default: return "mortal";
        }
    }

    /**
     * Extract Dao affinities from the NPC's dao_heart JSON object.
     *
     * <p>Converts keys with values >= 50 to a comma-separated string.
     * E.g. {"slaughter": 100, "karma": 95, "restriction": 90}
     * → "Slaughter Dao, Karma Dao, Restriction Dao"
     */
    private static String extractDaoAffinities(JsonObject npcData) {
        if (!npcData.has("dao_heart")) return "";
        JsonObject daoHeart = npcData.getAsJsonObject("dao_heart");
        if (daoHeart == null) return "";

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, com.google.gson.JsonElement> entry : daoHeart.entrySet()) {
            String key = entry.getKey();
            // Skip non-numeric fields like "stability", "cracks", "note", "last_tested_tick"
            if (key.equals("stability") || key.equals("cracks") || key.equals("note")
                    || key.equals("last_tested_tick")) continue;

            double value = 0;
            try {
                value = entry.getValue().getAsDouble();
            } catch (Exception e) {
                continue;
            }

            // Only show Dao with significant comprehension (>= 50)
            if (value >= 50) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(capitalize(key)).append(" Dao");
            }
        }
        return sb.toString();
    }

    /**
     * Parse a cultivation string from NPC JSON to a RealmId.
     *
     * <p>Handles various formats:
     * <ul>
     *   <li>"Nascent Soul" → NASCENT_SOUL</li>
     *   <li>"Half-Step Deity Transformation" → SOUL_TRANSFORMATION</li>
     *   <li>"Core Formation" → CORE_FORMATION</li>
     *   <li>"Soul Formation" → SOUL_FORMATION</li>
     * </ul>
     */
    private static RealmId parseCultivationString(String cultivation) {
        if (cultivation == null || cultivation.isEmpty()) return null;
        String lower = cultivation.toLowerCase();

        // Check for "half-step" prefix (means one tier below)
        boolean halfStep = lower.contains("half-step") || lower.contains("half step");
        String cleaned = lower.replace("half-step", "").replace("half step", "").trim();

        // Direct realm name match
        for (RealmId realm : RealmId.values()) {
            if (cleaned.contains(realm.name.toLowerCase().replace("_", " "))) {
                // Half-step means one tier below
                if (halfStep && realm.order > 0) {
                    return RealmId.byOrder(realm.order - 1);
                }
                return realm;
            }
        }

        // Special keyword matches for non-standard names in the JSON
        if (cleaned.contains("deity transformation") || cleaned.contains("soul transformation")) {
            return halfStep ? RealmId.SOUL_FORMATION : RealmId.SOUL_TRANSFORMATION;
        }
        if (cleaned.contains("deity") || cleaned.contains("transformation")) {
            return RealmId.SOUL_TRANSFORMATION;
        }
        if (cleaned.contains("nascent")) return RealmId.NASCENT_SOUL;
        if (cleaned.contains("core") || cleaned.contains("golden core") || cleaned.contains("pill")) {
            return RealmId.CORE_FORMATION;
        }
        if (cleaned.contains("foundation") || cleaned.contains("base")) return RealmId.FOUNDATION;
        if (cleaned.contains("qi condensation") || cleaned.contains("refining qi")) return RealmId.QI_CONDENSATION;
        if (cleaned.contains("soul formation") || cleaned.contains("spirit severing")) return RealmId.SOUL_FORMATION;
        if (cleaned.contains("ascendant") || cleaned.contains("combination")) return RealmId.ASCENDANT;
        if (cleaned.contains("immortal")) return RealmId.TRUE_IMMORTAL;
        if (cleaned.contains("ancient") || cleaned.contains("gu")) return RealmId.ANCIENT;
        if (cleaned.contains("paragon")) return RealmId.PARAGON;

        return null;
    }

    /** Format a faction string into a display name. */
    private static String formatFactionName(String faction) {
        if (faction == null || faction.isEmpty()) return "";
        // Convert "heng_yue_sect" → "Heng Yue Sect"
        String[] parts = faction.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(capitalize(part));
        }
        return sb.toString();
    }

    /** Capitalize the first letter of a string. */
    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /** Safe string getter for JsonObject — returns null if missing or not a string. */
    private static String getString(JsonObject obj, String key) {
        if (obj.has(key) && obj.get(key).isJsonPrimitive()
                && obj.get(key).getAsJsonPrimitive().isString()) {
            return obj.get(key).getAsString();
        }
        return null;
    }

    /** Parse a realm string to RealmId. */
    private static RealmId parseRealm(String realmStr) {
        if (realmStr == null || realmStr.isEmpty()) return RealmId.MORTAL;
        try {
            return RealmId.valueOf(realmStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RealmId.MORTAL;
        }
    }
}