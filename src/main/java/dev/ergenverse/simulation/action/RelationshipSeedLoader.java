package dev.ergenverse.simulation.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.server.level.ServerLevel;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * RelationshipSeedLoader — loads canon relationship graph seeds from JSON
 * into the ActorRelationshipStore on first world load.
 *
 * <p>Per Article XXXIV: relationships are multi-axis graphs, not numbers.
 * Per Article XXXI.3: Wang Family Village must have believable pre-existing
 * relationships that the player discovers through observation. Per Article
 * XLVII §6: these seeds are part of the "lives" that produce the village.
 *
 * <p>The seed JSON is at:
 * {@code data/ergenverse/living_chapters/chapter_1_wang_family_village/relationship_graph_seeds.json}.
 *
 * <p>Each entry has npc_id, target_id, and a dimensions object with:
 * trust, respect, fear, debt, grievance. Familiarity is derived from
 * shared_history (capped at 100).
 *
 * <p><b>Not a new Engine (Art XXVI):</b> This is a one-shot data loader
 * called during world initialization. No new bus, subscriber, or store.
 * It writes to the existing ActorRelationshipStore.
 *
 * <p><b>Idempotent:</b> If a relationship already exists in the store
 * (from a prior session), the seed is NOT applied — canon divergence
 * is respected. Seeds only apply to "blank slate" relationships.
 */
public final class RelationshipSeedLoader {

    private static final String SEED_PATH =
            "data/ergenverse/living_chapters/chapter_1_wang_family_village/relationship_graph_seeds.json";
    private static volatile boolean loaded = false;

    private RelationshipSeedLoader() {}

    /**
     * Load relationship seeds if not already loaded. Called once during
     * world initialization (Ergenverse.serverStarting).
     *
     * @param level the server level (for ActorRelationshipStore access)
     * @return number of relationships seeded
     */
    public static int loadIfNeeded(ServerLevel level) {
        if (loaded) return 0;
        loaded = true;

        ActorRelationshipStore store = ActorRelationshipStore.get(level);
        if (store == null) {
            Ergenverse.LOGGER.warn("[RelSeedLoader] ActorRelationshipStore not available, skipping seeds");
            return 0;
        }

        try {
            var resourceOptional = level.getServer().getResourceManager()
                    .getResource(new net.minecraft.resources.ResourceLocation("ergenverse", SEED_PATH));
            if (!resourceOptional.isPresent()) {
                Ergenverse.LOGGER.warn("[RelSeedLoader] Seed file not found: {}", SEED_PATH);
                return 0;
            }
            try (InputStream is = resourceOptional.get().open()) {
            JsonObject root = com.google.gson.JsonParser.parseReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray graphs = root.getAsJsonArray("graphs");

            int seeded = 0;
            for (JsonElement elem : graphs) {
                JsonObject entry = elem.getAsJsonObject();
                String npcId = entry.get("npc_id").getAsString();
                String targetId = entry.get("target_id").getAsString();

                JsonObject dims = entry.getAsJsonObject("dimensions");
                int trust = dims.has("trust") ? dims.get("trust").getAsInt() : 0;
                int respect = dims.has("respect") ? dims.get("respect").getAsInt() : 0;
                int fear = dims.has("fear") ? dims.get("fear").getAsInt() : 0;
                int debt = dims.has("debt") ? dims.get("debt").getAsInt() : 0;
                int grievance = dims.has("grievance") ? dims.get("grievance").getAsInt() : 0;
                // Familiarity derived from shared_history
                int familiarity = dims.has("shared_history")
                        ? Math.min(100, dims.get("shared_history").getAsInt())
                        : 0;

                // Only seed if relationship doesn't already exist (idempotent).
                // If it exists, the world has diverged — respect that.
                if (store.hasRelationship(npcId, targetId)) {
                    continue;
                }

                // Use recordMultiAxis with explicit initial values (not deltas).
                // Since the relationship doesn't exist, we set absolute values.
                // recordMultiAxis expects deltas, so we need to force-initialize.
                // Workaround: call the internal API to create from scratch.
                initializeRelationship(store, npcId, targetId,
                        trust, respect, fear, familiarity, debt, grievance);
                seeded++;
            }

            Ergenverse.LOGGER.info("[RelSeedLoader] Seeded {} NPC relationships from canon data", seeded);
            return seeded;
            } // end try-with-resources

        } catch (Exception e) {
            Ergenverse.LOGGER.error("[RelSeedLoader] Failed to load relationship seeds", e);
            return 0;
        }
    }

    /**
     * Initialize a relationship with absolute values (not deltas).
     * Since ActorRelationshipStore.recordMultiAxis() applies deltas
     * (incremental changes), and a fresh relationship starts at 0,
     * we can use the delta API to set the initial values by passing
     * the seed values as deltas.
     */
    private static void initializeRelationship(ActorRelationshipStore store,
                                                String npcId, String targetId,
                                                int trust, int respect, int fear,
                                                int familiarity, int debt,
                                                int grievance) {
        String reason = "Canon seed (game-start relationship)";
        long tick = 0; // game-start tick
        store.recordMultiAxis(npcId, targetId,
                trust, respect, fear, familiarity, debt, grievance,
                reason, tick);
    }

    /** Reset the loaded flag (for testing/debugging). */
    public static void reset() {
        loaded = false;
    }
}
