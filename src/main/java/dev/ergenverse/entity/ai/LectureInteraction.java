package dev.ergenverse.entity.ai;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.history.HistoryManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ergenverse.simulation.WorldStateDataLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * LectureInteraction — handles right-click attendance for elder lectures.
 *
 * <p>Wired into the Forge event bus. When a player right-clicks an NPC
 * who has invited them to a lecture:
 * <ol>
 *   <li>Load the lecture data from the NPC's JSON.</li>
 *   <li>Send the lecture content as chat messages (description + insight).</li>
 *   <li>Grant the lecture reward (if any).</li>
 *   <li>Mark the lecture as attended in player NBT (prevents re-attending).</li>
 *   <li>Record the event in player history.</li>
 * </ol>
 *
 * <h2>Player NBT structure</h2>
 * <ul>
 *   <li>{@code ergenverse_lecture_invite_<npcId>} — pending invite: {npc_id, lecture_id, topic}</li>
 *   <li>{@code ergenverse_lecture_attended_<lectureId>} — boolean, true after attending</li>
 * </ul>
 *
 * <h2>Article XXVI compliance</h2>
 * <p>NO new engines. Pure Forge event handler + NBT + inventory ops.
 */
public class LectureInteraction {

    private LectureInteraction() {}

    // ── NBT keys ────────────────────────────────────────────────────

    private static String inviteKey(String npcId) {
        return "ergenverse_lecture_invite_" + npcId;
    }

    private static String attendedKey(String lectureId) {
        return "ergenverse_lecture_attended_" + lectureId;
    }

    // ── Event handler ───────────────────────────────────────────────

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof EntityCultivator cultivator)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        String npcId = cultivator.getCharacterId();
        if (npcId == null || npcId.isEmpty()) return;

        CompoundTag playerData = player.getPersistentData();
        String key = inviteKey(npcId);

        // Check if player has a pending lecture invite from this NPC
        if (!playerData.contains(key, 10)) { // 10 = CompoundTag
            return;
        }

        CompoundTag inviteTag = playerData.getCompound(key);
        String lectureId = inviteTag.getString("lecture_id");
        String topic = inviteTag.getString("topic");

        // Load the full lecture data from NPC JSON
        NpcLectureGoal.LectureData lecture = findLecture(npcId, lectureId);
        if (lecture == null) {
            player.sendSystemMessage(
                    Component.literal("\u00A77The lecture has already ended."), true);
            playerData.remove(key);
            return;
        }

        // Check if already attended
        if (playerData.getBoolean(attendedKey(lectureId))) {
            player.sendSystemMessage(
                    Component.literal("\u00A77You have already attended this lecture."), true);
            playerData.remove(key);
            return;
        }

        // ── Attend the lecture ──────────────────────────────────

        // Remove the invite
        playerData.remove(key);

        // Mark as attended
        playerData.putBoolean(attendedKey(lectureId), true);

        // Send lecture content as chat messages (multi-line for immersion)
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
                "\u00A76\u00A7l=== Lecture: " + lecture.topic + " ===\u00A7r"));
        player.sendSystemMessage(Component.literal(
                "\u00A77Speaker: \u00A7e" + cultivator.getDisplayNameCn()
                + "\u00A77 | Location: \u00A7b" + lecture.location + "\u00A77"));
        player.sendSystemMessage(Component.literal(""));

        // Send description lines (split by newline if present)
        if (!lecture.description.isEmpty()) {
            for (String line : lecture.description.split("\n")) {
                player.sendSystemMessage(Component.literal("\u00A7f  " + line.trim()));
            }
        }

        player.sendSystemMessage(Component.literal(""));

        // Send insight (the "gain" from attending)
        if (!lecture.insight.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7a\u2728 Insight: \u00A7f" + lecture.insight));
        }

        // Grant reward
        if (lecture.rewardItem != null && !lecture.rewardItem.isEmpty() && lecture.rewardCount > 0) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(lecture.rewardItem));
            if (item != null) {
                ItemStack reward = new ItemStack(item, lecture.rewardCount);
                player.getInventory().add(reward);
                player.sendSystemMessage(Component.literal(
                        "\u00A76Received: \u00A7f" + lecture.rewardCount + "x "
                        + reward.getDisplayName().getString()));
            } else {
                Ergenverse.LOGGER.warn("[LectureInteraction] Reward item not registered: {}",
                        lecture.rewardItem);
            }
        }

        player.sendSystemMessage(Component.literal(
                "\u00A76\u00A7l=== Lecture Concluded ===\u00A7r"));
        player.sendSystemMessage(Component.literal(""));

        // Record in history
        HistoryManager.onDiscovery(player,
                "lecture:" + lectureId,
                "Attended " + cultivator.getDisplayNameCn() + "'s lecture on '"
                        + lecture.topic + "' at " + lecture.location,
                player.level().getGameTime());

        Ergenverse.LOGGER.info("[LectureInteraction] {} attended lecture '{}' by {}",
                player.getName().getString(), lectureId, npcId);
    }

    // ── Lecture data lookup ─────────────────────────────────────────

    @org.jetbrains.annotations.Nullable
    private static NpcLectureGoal.LectureData findLecture(String npcId, String lectureId) {
        try {
            JsonObject data = WorldStateDataLoader.getEntry("npcs", npcId);
            if (data == null || !data.has("lectures")
                    || !data.get("lectures").isJsonArray()) {
                return null;
            }
            JsonArray arr = data.getAsJsonArray("lectures");
            for (JsonElement elem : arr) {
                if (!elem.isJsonObject()) continue;
                JsonObject obj = elem.getAsJsonObject();
                String id = obj.has("id") ? obj.get("id").getAsString() : null;
                if (lectureId.equals(id)) {
                    return NpcLectureGoal.LectureData.fromJson(obj);
                }
            }
        } catch (Exception e) {
            Ergenverse.LOGGER.debug("[LectureInteraction] Failed to load lecture for {}: {}",
                    npcId, e.getMessage());
        }
        return null;
    }
}