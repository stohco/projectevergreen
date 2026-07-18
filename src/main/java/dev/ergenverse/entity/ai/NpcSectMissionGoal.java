package dev.ergenverse.entity.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * NpcSectMissionGoal -- Article XXIV + XXII: NPCs initiate sect missions.
 *
 * <p>When a player approaches, the NPC offers a sect mission from their
 * JSON data ({@code sect_tasks[]}). The offer is written to player NBT so
 * the player can accept it by right-clicking the NPC. Once accepted, the
 * player delivers required items by right-clicking again. Rewards are
 * granted immediately.
 *
 * <h2>Flow</h2>
 * <ol>
 *   <li>NPC detects player within APPROACH_RANGE who has no active mission
 *       from this NPC.</li>
 *   <li>NPC sends an offer message (gold, action bar) describing the task.</li>
 *   <li>NPC writes {@code ergenverse_mission<npcId>} to player persistent data.</li>
 *   <li>Player right-clicks the NPC to ACCEPT the mission
 *       (handled by SectMissionInteraction).</li>
 *   <li>Player right-clicks again to DELIVER (items consumed, reward granted).
 *       (Also handled by SectMissionInteraction.)</li>
 * </ol>
 *
 * <h2>Data source</h2>
 * <p>Reads from the NPC's JSON under {@code "sect_tasks"} array:
 * <pre>{
 *   "sect_tasks": [{
 *     "id": "herb_gathering",
 *     "description": "Gather herbs for the Alchemy Hall.",
 *     "requires": [{"item": "ergenverse:spirit_stone", "count": 3}],
 *     "reward_item": "ergenverse:spirit_stone",
 *     "reward_count": 8
 *   }]
 * }</pre>
 *
 * <h2>Article XXVI compliance</h2>
 * <p>NO new engines or buses. This is a single Goal class that reads
 * existing NPC JSON data and writes to player NBT. Mission delivery
 * is handled by SectMissionInteraction (a Forge event handler).
 *
 * <p><b>Provenance:</b> INFERRED from RI Ch.3-8. Wang Lin performs sect
 * tasks (patrols, herb gathering) as an outer disciple. The sect mission
 * system is the core gameplay loop of early Heng Yue Sect life.
 */
public class NpcSectMissionGoal extends Goal {

    /** How close a player must be for the NPC to offer a mission. */
    private static final double APPROACH_RANGE = 8.0;

    /** Minimum ticks between mission offers to the same player. */
    private static final int OFFER_COOLDOWN_TICKS = 1200; // 60 seconds

    private final EntityCultivator cultivator;

    /** Loaded sect tasks from NPC JSON data. */
    private final List<SectTask> tasks = new ArrayList<>();

    /** Per-player cooldown tracking: player UUID -> last offer tick. */
    private final Map<String, Long> offerCooldowns = new HashMap<>();

    /** True once tasks have been loaded. */
    private boolean tasksLoaded = false;

    public NpcSectMissionGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cultivator.level().isClientSide) return false;
        if (!loadTasksIfNeeded()) return false;
        if (tasks.isEmpty()) return false;
        return findEligiblePlayer() != null;
    }

    @Override
    public void start() {
        ServerPlayer target = findEligiblePlayer();
        if (target == null) return;

        String npcId = cultivator.getCharacterId();

        // Check if player already has an active mission from this NPC
        if (SectMissionInteraction.hasActiveMission(target, npcId)) {
            // Don't re-offer; they already accepted
            return;
        }

        // Pick a task that the player hasn't recently been offered
        SectTask task = pickTaskForPlayer(target);
        if (task == null) return;

        // Send the offer
        String name = cultivator.getDisplayNameCn();
        target.sendSystemMessage(
                Component.literal(
                        "\u00A76\u00A7l[Sect Mission] \u00A7e" + name
                        + "\u00A7r\u00A7f: \"" + task.description + "\"\n"
                        + "\u00A77  Requires: " + formatRequirements(task)
                        + "  |  Reward: " + task.rewardCount + "x "
                        + formatItemName(task.rewardItem) + "\n"
                        + "\u00A7a  Right-click to accept."),
                true);

        // Write the offer to player NBT
        SectMissionInteraction.writeOffer(target, npcId, task.id);

        // Face the player
        cultivator.getLookControl().setLookAt(target);

        // Record cooldown
        offerCooldowns.put(target.getUUID().toString(),
                cultivator.level().getGameTime());

        Ergenverse.LOGGER.info("[SectMission] {} offered task '{}' to {}",
                npcId, task.id, target.getName().getString());
    }

    @Override
    public boolean canContinueToUse() {
        // One-shot: offer and stop
        return false;
    }

    // ── Task loading ───────────────────────────────────────────────

    private boolean loadTasksIfNeeded() {
        if (tasksLoaded) return true;

        String characterId = cultivator.getCharacterId();
        if (characterId == null || characterId.isEmpty()) {
            tasksLoaded = true;
            return false;
        }

        try {
            JsonObject data = dev.ergenverse.simulation.WorldStateDataLoader
                    .getEntry("npcs", characterId);
            if (data != null && data.has("sect_tasks")
                    && data.get("sect_tasks").isJsonArray()) {
                JsonArray arr = data.getAsJsonArray("sect_tasks");
                for (JsonElement elem : arr) {
                    if (elem.isJsonObject()) {
                        SectTask task = SectTask.fromJson(elem.getAsJsonObject());
                        if (task != null) tasks.add(task);
                    }
                }
            }
        } catch (Exception e) {
            Ergenverse.LOGGER.debug("[SectMission] Failed to load tasks for {}: {}",
                    characterId, e.getMessage());
        }

        tasksLoaded = true;
        return true;
    }

    // ── Player finding ─────────────────────────────────────────────

    private ServerPlayer findEligiblePlayer() {
        long currentTick = cultivator.level().getGameTime();

        for (ServerPlayer player : cultivator.level().getEntitiesOfClass(
                ServerPlayer.class,
                cultivator.getBoundingBox().inflate(APPROACH_RANGE))) {
            String uuid = player.getUUID().toString();
            Long lastOffer = offerCooldowns.get(uuid);
            if (lastOffer == null
                    || (currentTick - lastOffer) >= OFFER_COOLDOWN_TICKS) {
                return player;
            }
        }
        return null;
    }

    private SectTask pickTaskForPlayer(ServerPlayer player) {
        // Simple: pick a random task. Future: pick based on player realm,
        // what items they already have, etc.
        if (tasks.isEmpty()) return null;
        return tasks.get(cultivator.getRandom().nextInt(tasks.size()));
    }

    // ── Formatting ─────────────────────────────────────────────────

    private String formatRequirements(SectTask task) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < task.requires.size(); i++) {
            if (i > 0) sb.append(", ");
            SectTask.Requirement r = task.requires.get(i);
            sb.append(r.count).append("x ").append(formatItemName(r.itemId));
        }
        return sb.toString();
    }

    private String formatItemName(String itemId) {
        // Strip "ergenverse:" prefix for cleaner display
        if (itemId.startsWith("ergenverse:")) {
            return itemId.substring("ergenverse:".length()).replace('_', ' ');
        }
        return itemId.replace('_', ' ');
    }

    // ── Inner data class ───────────────────────────────────────────

    /**
     * A sect task loaded from NPC JSON data.
     */
    static class SectTask {
        String id;
        String description;
        List<Requirement> requires = new ArrayList<>();
        String rewardItem;
        int rewardCount;

        @Nullable
        static SectTask fromJson(JsonObject obj) {
            try {
                SectTask t = new SectTask();
                t.id = obj.get("id").getAsString();
                t.description = obj.get("description").getAsString();
                if (obj.has("requires") && obj.get("requires").isJsonArray()) {
                    for (JsonElement e : obj.getAsJsonArray("requires")) {
                        if (e.isJsonObject()) {
                            JsonObject ro = e.getAsJsonObject();
                            Requirement r = new Requirement();
                            r.itemId = ro.get("item").getAsString();
                            r.count = ro.get("count").getAsInt();
                            t.requires.add(r);
                        }
                    }
                }
                if (obj.has("reward_item")) {
                    t.rewardItem = obj.get("reward_item").getAsString();
                }
                if (obj.has("reward_count")) {
                    t.rewardCount = obj.get("reward_count").getAsInt();
                }
                return t;
            } catch (Exception e) {
                Ergenverse.LOGGER.warn("[SectMission] Failed to parse task: {}", e.getMessage());
                return null;
            }
        }

        static class Requirement {
            String itemId;
            int count;
        }
    }
}