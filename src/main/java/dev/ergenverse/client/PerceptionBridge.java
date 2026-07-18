package dev.ergenverse.client;

import dev.ergenverse.canon.CanonEngine;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.network.ClientCultivationCache;
import dev.ergenverse.network.PerceptionSyncS2CPacket;
import dev.ergenverse.perception.*;

/**
 * Bridge between Minecraft entities and the Perception Engine.
 *
 * <p>Converts an {@link EntityCultivator}'s synced data into an
 * {@link ObjectiveNature} + {@link ObserverContext}, runs it through
 * {@link PerceptionEngine#perceive(Objective, ObserverContext)}, and
 * returns the {@link PerceptionResult}.
 *
 * <p>This is the client-side wiring that makes the Prime Directive
 * visible in-game: the same NPC looks different to a mortal vs. a
 * Soul Formation cultivator, because the observer's understanding
 * changes — not the entity.
 *
 * <h2>Data flow</h2>
 * <pre>
 *   EntityCultivator (synced: characterId, displayName, realm)
 *       → PerceptionBridge.toObjectiveNature()
 *       → ObjectiveNature (kind, trueName, trueRealm, etc.)
 *       → PerceptionEngine.perceive(nature, observer)
 *       → PerceptionResult (perceivedName, perceivedDescription)
 *       → Rendered on name tag / tooltip
 * </pre>
 *
 * <h2>v1 scope</h2>
 * <p>Uses the synced characterId to look up the NPC JSON for richer
 * data (bloodline, origin). If the NPC JSON is unavailable (not yet
 * loaded, or this is a generic cultivator), falls back to the synced
 * display name and realm. Bloodline, origin, and karmic history default
 * to empty (revealed only at higher perception tiers — so the fallback
 * is correct: mortals see less, which is canon).
 */
public final class PerceptionBridge {

    private PerceptionBridge() {}

    /**
     * Build an ObjectiveNature from an EntityCultivator's synced data.
     *
     * <p>The entity's cultivation realm is parsed from the synced string.
     * If parsing fails, defaults to MORTAL. Bloodline, origin, and
     * karmic history are empty for v1 (no per-NPC canon lookup on
     * the client yet — that requires the data loader to be extended).
     */
    public static ObjectiveNature toObjectiveNature(EntityCultivator cultivator) {
        String displayName = cultivator.getDisplayNameCn();
        if (displayName == null || displayName.isEmpty()) {
            displayName = cultivator.getCharacterId();
        }
        if (displayName == null || displayName.isEmpty()) {
            displayName = "Unknown Cultivator";
        }

        // Parse cultivation realm from synced string
        RealmId entityRealm = parseRealm(cultivator.getCultivationRealm());

        // Determine kind: cultivators use CULTIVATOR kind so the
        // PerceptionEngine's perceiveCultivatorEntity() handles them
        // with tiered perception (mortal sees "a person", Soul Formation
        // sees full name + realm + Dao affinities).
        ObjectiveNature.Kind kind = entityRealm == RealmId.MORTAL
                ? ObjectiveNature.Kind.MORTAL_ENTITY
                : ObjectiveNature.Kind.CULTIVATOR;

        // Estimate a "rank" from the realm (0 for mortal, realm order for others)
        int perceivedRank = entityRealm == RealmId.MORTAL ? 0 : entityRealm.order;

        // Build a description from available synced data.
        // Future: load from NPC JSON for richer bloodline/origin/karmic data.
        String realmDesc = entityRealm != RealmId.MORTAL
                ? "Cultivator at " + entityRealm.name
                : "Mortal";

        return ObjectiveNature.cultivator(
                displayName,
                displayName, // trueNameCn = same for v1
                entityRealm,
                "",   // bloodline — v1: not loaded from NPC JSON on client
                realmDesc, // origin — brief description for v1
                "",   // karmicHistory — v1: not loaded
                CanonEngine.Confidence.NOVEL_STATEMENT,
                "npc:" + cultivator.getCharacterId(),
                "",   // daoAffinities — v1: not loaded
                "",   // titles — v1: not loaded
                ""    // sect — v1: not loaded
        );
    }

    /**
     * Build an ObserverContext from the local player's cached cultivation state.
     * Includes Dao comprehension and divine sense status from the sync data.
     */
    public static ObserverContext toObserverContext() {
        if (!ClientCultivationCache.isAvailable()) {
            return ObserverContext.mortal();
        }
        RealmId playerRealm = ClientCultivationCache.getRealm();
        var cache = ClientCultivationCache.get();

        // Build Dao comprehension map from sync data
        java.util.Map<String, Double> daoMap = new java.util.HashMap<>();
        for (int i = 0; i < cache.daoKeys.size(); i++) {
            daoMap.put(cache.daoKeys.get(i), (double) cache.daoValues.get(i));
        }

        // Divine sense is active when the player has used it
        boolean divineSenseActive = cache.divineSense > 0.01;

        // Detect specializations from Dao comprehension
        boolean isBeastTamer = daoMap.getOrDefault("beast", 0.0) >= 0.3;
        boolean isAlchemist = daoMap.getOrDefault("alchemy", 0.0) >= 0.3;
        boolean isFormationMaster = daoMap.getOrDefault("formation", 0.0) >= 0.3
                || daoMap.getOrDefault("restriction", 0.0) >= 0.3;

        return new ObserverContext(playerRealm, daoMap, divineSenseActive,
                java.util.Set.of(), 1.0, isBeastTamer, isAlchemist, isFormationMaster);
    }

    /**
     * Full perception pipeline: entity → ObjectiveNature → PerceptionEngine → PerceptionResult.
     *
     * <p>Returns null if the perception system is not available (no sync received yet).
     */
    @org.jetbrains.annotations.Nullable
    public static PerceptionResult perceiveEntity(EntityCultivator cultivator) {
        if (!ClientCultivationCache.isAvailable()) return null;

        ObjectiveNature nature = toObjectiveNature(cultivator);
        ObserverContext observer = toObserverContext();

        // Create a simple Objective wrapper
        Objective objective = new Objective() {
            @Override
            public ObjectiveNature nature() {
                return nature;
            }

            @Override
            public PerceptionResult perceive(ObserverContext observer) {
                return PerceptionEngine.perceive(this, observer);
            }
        };

        return PerceptionEngine.perceive(objective, observer);
    }

    /**
     * Get the perception-based display name for an entity.
     *
     * <p><b>Performance:</b> First checks the server-synced perception cache
     * (updated every ~2 seconds by PerceptionEvents). If a cached entry
     * exists for this entity ID, uses it directly — no PerceptionEngine
     * run. Falls back to local PerceptionEngine only if cache misses.
     */
    public static String getPerceivedName(EntityCultivator cultivator, String fallback) {
        // Fast path: use server-synced cache if available
        PerceptionSyncS2CPacket.EntityPerception cached =
                ClientCultivationCache.getPerception(cultivator.getId());
        if (cached != null) {
            if (cached.concealed) {
                return "\u00A78???";
            }
            return cached.perceivedName;
        }

        // Fallback: run PerceptionEngine locally (for divine sense pulse overlay)
        PerceptionResult result = perceiveEntity(cultivator);
        if (result == null) return fallback;

        if (result.concealed) {
            return "\u00A78???";
        }

        // Color-code based on observer tier
        PerceptionTier tier = ClientCultivationCache.getPerceptionTier();
        String color = switch (tier) {
            case MORTAL -> "\u00A77";      // gray
            case QI_CONDENSATION -> "\u00A7a"; // green
            case FOUNDATION -> "\u00A7e";    // yellow
            case NASCENT_SOUL -> "\u00A7b";  // aqua
            case SOUL_FORMATION -> "\u00A75"; // purple
            case ASCENDANT -> "\u00A76";     // gold
            case TRANSCENDENCE -> "\u00A7f"; // white
        };

        return color + result.perceivedName;
    }

    /**
     * Get the perception-based description for an entity.
     *
     * <p><b>Performance:</b> Uses server-synced cache when available,
     * falls back to local PerceptionEngine on cache miss.
     *
     * @return the perceived description, or null if unavailable
     */
    @org.jetbrains.annotations.Nullable
    public static String getPerceivedDescription(EntityCultivator cultivator) {
        // Fast path: use server-synced cache
        PerceptionSyncS2CPacket.EntityPerception cached =
                ClientCultivationCache.getPerception(cultivator.getId());
        if (cached != null) {
            if (cached.concealed) return null;
            return cached.perceivedDescription;
        }

        // Fallback: local PerceptionEngine
        PerceptionResult result = perceiveEntity(cultivator);
        if (result == null) return null;
        return result.perceivedDescription;
    }

    /** Parse a realm string (e.g. "qi_condensation", "soul_formation") to RealmId. */
    private static RealmId parseRealm(String realmStr) {
        if (realmStr == null || realmStr.isEmpty()) return RealmId.MORTAL;
        try {
            return RealmId.valueOf(realmStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try common variations
            if (realmStr.contains("qi")) return RealmId.QI_CONDENSATION;
            if (realmStr.contains("foundation")) return RealmId.FOUNDATION;
            if (realmStr.contains("core")) return RealmId.CORE_FORMATION;
            if (realmStr.contains("nascent")) return RealmId.NASCENT_SOUL;
            if (realmStr.contains("soul") && realmStr.contains("transform")) return RealmId.SOUL_TRANSFORMATION;
            if (realmStr.contains("soul")) return RealmId.SOUL_FORMATION;
            if (realmStr.contains("ascend")) return RealmId.ASCENDANT;
            if (realmStr.contains("illusory")) return RealmId.ILLUSORY_YIN;
            if (realmStr.contains("corporeal")) return RealmId.CORPOREAL_YANG;
            if (realmStr.contains("nirvana")) return RealmId.NIRVANA_SCRYER;
            return RealmId.MORTAL;
        }
    }
}