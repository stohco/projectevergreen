package dev.ergenverse.entity.ai;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.history.HistoryManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
 * SectMissionInteraction -- handles right-click accept/deliver for sect missions.
 *
 * <p>Wired into the Forge event bus. When a player right-clicks an NPC:
 * <ol>
 *   <li>If the player has an ACTIVE mission from this NPC, check inventory.
 *       If requirements met: consume items, grant reward, clear mission, record history.</li>
 *   <li>If the player has a PENDING OFFER from this NPC, convert to ACTIVE mission
 *       and send acceptance message.</li>
 *   <li>Otherwise, do nothing (the NpcSectMissionGoal handles offering).</li>
 * </ol>
 *
 * <h2>Player NBT structure</h2>
 * <ul>
 *   <li>{@code ergenverse_mission_offer<npcId>} -- pending offer: {id, task_id}</li>
 *   <li>{@code ergenverse_mission_active<npcId>} -- active mission: {id, task_id, accepted_tick}</li>
 * </ul>
 *
 * <h2>Article XXVI compliance</h2>
 * <p>NO new engines. Pure Forge event handler + NBT + inventory ops.
 */
public class SectMissionInteraction {

    private SectMissionInteraction() {}

    // ── NBT keys ────────────────────────────────────────────────────

    private static String offerKey(String npcId) {
        return "ergenverse_mission_offer_" + npcId;
    }

    private static String activeKey(String npcId) {
        return "ergenverse_mission_active_" + npcId;
    }

    // ── Public API for NpcSectMissionGoal ───────────────────────────

    /**
     * Write a pending mission offer to player NBT.
     */
    public static void writeOffer(ServerPlayer player, String npcId, String taskId) {
        CompoundTag tag = player.getPersistentData();
        CompoundTag offer = new CompoundTag();
        offer.putString("npc_id", npcId);
        offer.putString("task_id", taskId);
        tag.put(offerKey(npcId), offer);

        // Clear any previous offer (only one at a time)
        for (String key : tag.getAllKeys()) {
            if (key.startsWith("ergenverse_mission_offer_") && !key.equals(offerKey(npcId))) {
                tag.remove(key);
            }
        }
    }

    /**
     * Check if the player has an active mission from the given NPC.
     */
    public static boolean hasActiveMission(ServerPlayer player, String npcId) {
        return player.getPersistentData().contains(activeKey(npcId));
    }

    // ── Event handler ───────────────────────────────────────────────

    /**
     * Called when a player right-clicks an EntityCultivator.
     * Handles mission acceptance and delivery.
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getTarget() instanceof EntityCultivator cultivator)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String npcId = cultivator.getCharacterId();
        if (npcId == null || npcId.isEmpty()) return;

        CompoundTag playerData = player.getPersistentData();

        // 1) Check for ACTIVE mission delivery
        String aKey = activeKey(npcId);
        if (playerData.contains(aKey)) {
            CompoundTag active = playerData.getCompound(aKey);
            String taskId = active.getString("task_id");

            // Load task definition from NPC JSON
            NpcSectMissionGoal.SectTask task = loadTask(npcId, taskId);
            if (task == null) {
                player.sendSystemMessage(
                        Component.literal("\u00A7c[Sect Mission] Task data not found. Mission cancelled."),
                        true);
                playerData.remove(aKey);
                return;
            }

            // Check if player has required items
            if (hasRequiredItems(player, task)) {
                // Consume items
                consumeRequiredItems(player, task);
                // Grant reward
                grantReward(player, task, cultivator);
                // Clear mission
                playerData.remove(aKey);
                // Record history
                HistoryManager.onDiscovery(player,
                        "sect_mission_complete",
                        "Completed sect mission '" + taskId + "' for "
                                + cultivator.getDisplayNameCn() + ".",
                        player.level().getGameTime());
                Ergenverse.LOGGER.info("[SectMission] {} delivered task '{}' to {}",
                        player.getName().getString(), taskId, npcId);
            } else {
                // Not enough items yet
                player.sendSystemMessage(
                        Component.literal(
                                "\u00A77[Sect Mission] You don't have the required items yet. "
                                + formatRequirements(task)),
                        true);
            }
            return;
        }

        // 2) Check for PENDING offer (player accepts)
        String oKey = offerKey(npcId);
        if (playerData.contains(oKey)) {
            CompoundTag offer = playerData.getCompound(oKey);
            String taskId = offer.getString("task_id");

            // Load task for display
            NpcSectMissionGoal.SectTask task = loadTask(npcId, taskId);
            if (task == null) {
                playerData.remove(oKey);
                return;
            }

            // Convert offer to active mission
            CompoundTag active = new CompoundTag();
            active.putString("npc_id", npcId);
            active.putString("task_id", taskId);
            active.putLong("accepted_tick", player.level().getGameTime());
            playerData.put(aKey, active);

            // Remove offer
            playerData.remove(oKey);

            // Send acceptance message
            player.sendSystemMessage(
                    Component.literal(
                            "\u00A7a\u00A7l[Sect Mission Accepted]\u00A7r\n"
                            + "\u00A7f" + task.description + "\n"
                            + "\u00A77  Bring: " + formatRequirements(task) + "\n"
                            + "\u00A77  Reward: " + task.rewardCount + "x "
                            + formatItemName(task.rewardItem) + "\n"
                            + "\u00A7a  Return to " + cultivator.getDisplayNameCn()
                            + " when ready."),
                    false); // chat message, not action bar

            Ergenverse.LOGGER.info("[SectMission] {} accepted task '{}' from {}",
                    player.getName().getString(), taskId, npcId);
        }
    }

    // ── Inventory helpers ───────────────────────────────────────────

    private static boolean hasRequiredItems(Player player, NpcSectMissionGoal.SectTask task) {
        for (NpcSectMissionGoal.SectTask.Requirement req : task.requires) {
            if (countItem(player, req.itemId) < req.count) return false;
        }
        return true;
    }

    private static void consumeRequiredItems(Player player, NpcSectMissionGoal.SectTask task) {
        for (NpcSectMissionGoal.SectTask.Requirement req : task.requires) {
            int remaining = req.count;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (matchesItem(stack, req.itemId)) {
                    int toTake = Math.min(remaining, stack.getCount());
                    stack.shrink(toTake);
                    remaining -= toTake;
                    if (remaining <= 0) break;
                }
            }
        }
    }

    private static void grantReward(ServerPlayer player, NpcSectMissionGoal.SectTask task,
                                     EntityCultivator npc) {
        if (task.rewardItem == null || task.rewardItem.isEmpty()) return;

        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(task.rewardItem));
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            Ergenverse.LOGGER.warn("[SectMission] Reward item not found: {}", task.rewardItem);
            player.sendSystemMessage(
                    Component.literal("\u00A7c[Sect Mission] Reward item could not be granted."),
                    false);
            return;
        }

        ItemStack reward = new ItemStack(item, task.rewardCount);
        if (!player.getInventory().add(reward)) {
            player.drop(reward, false);
        }

        player.sendSystemMessage(
                Component.literal(
                        "\u00A7a\u00A7l[Sect Mission Complete!]\u00A7r\n"
                        + "\u00A7f" + npc.getDisplayNameCn()
                        + " nods approvingly and hands you your reward.\n"
                        + "\u00A7e  Received: " + task.rewardCount + "x "
                        + formatItemName(task.rewardItem)),
                false); // chat message

        // Also record in history
        HistoryManager.onDiscovery(player,
                "sect_mission_reward",
                "Received " + task.rewardCount + "x " + task.rewardItem
                        + " from " + npc.getCharacterId(),
                player.level().getGameTime());
    }

    private static int countItem(Player player, String itemId) {
        ResourceLocation rl = new ResourceLocation(itemId);
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (matchesItem(stack, itemId)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static boolean matchesItem(ItemStack stack, String itemId) {
        ResourceLocation rl = new ResourceLocation(itemId);
        return ForgeRegistries.ITEMS.getKey(stack.getItem()) != null
                && ForgeRegistries.ITEMS.getKey(stack.getItem()).equals(rl);
    }

    private static NpcSectMissionGoal.SectTask loadTask(String npcId, String taskId) {
        try {
            JsonObject data = dev.ergenverse.simulation.WorldStateDataLoader
                    .getEntry("npcs", npcId);
            if (data == null || !data.has("sect_tasks")) return null;
            JsonArray arr = data.getAsJsonArray("sect_tasks");
            for (com.google.gson.JsonElement elem : arr) {
                if (elem.isJsonObject()) {
                    NpcSectMissionGoal.SectTask task =
                            NpcSectMissionGoal.SectTask.fromJson(elem.getAsJsonObject());
                    if (task != null && task.id.equals(taskId)) return task;
                }
            }
        } catch (Exception e) {
            Ergenverse.LOGGER.warn("[SectMission] Failed to load task {} for {}: {}",
                    taskId, npcId, e.getMessage());
        }
        return null;
    }

    // ── Formatting ──────────────────────────────────────────────────

    static String formatRequirements(NpcSectMissionGoal.SectTask task) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < task.requires.size(); i++) {
            if (i > 0) sb.append(", ");
            NpcSectMissionGoal.SectTask.Requirement r = task.requires.get(i);
            sb.append(r.count).append("x ").append(formatItemName(r.itemId));
        }
        return sb.toString();
    }

    static String formatItemName(String itemId) {
        if (itemId.startsWith("ergenverse:")) {
            return itemId.substring("ergenverse:".length()).replace('_', ' ');
        }
        return itemId.replace('_', ' ');
    }
}