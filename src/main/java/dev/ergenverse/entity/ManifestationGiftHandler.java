package dev.ergenverse.entity;

import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.affinity.ManifestationGiftSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Server-side handler for the Manifestation Gift interaction.
 *
 * <p>When a player right-clicks (interacts with) the Wang Lin manifestation
 * EntityCultivator, this handler evaluates the best available gift
 * using the {@link ManifestationGiftSystem}'s four-question engine and,
 * if the decision is OFFERED, grants the item to the player's inventory.
 *
 * <p>The interaction flow:
 * <ol>
 *   <li>Player right-clicks the manifestation entity</li>
 *   <li>Server evaluates the player's best available gift (highest affinity
 *       that passes all four questions)</li>
 *   <li>If a gift is offered: play the offer dialogue, grant the item
 *       (if it maps to a registered Forge item), send system message</li>
 *   <li>If refused: play the refusal dialogue, explain why</li>
 * </ol>
 *
 * <p>Registered on the FORGE bus. Event: PlayerInteractEvent.EntityInteract.
 *
 * <h2>Item granting</h2>
 * <p>If the gift's {@code canonOriginId} matches a registered item in
 * ForgeRegistries.ITEMS (under the "ergenverse" namespace), the player
 * receives one copy of that item. The item is added to the player's
 * inventory (or dropped at their feet if full).
 *
 * <p>For technique gifts (CANONICAL_TECHNIQUE), no physical item is granted.
 * Instead, the player receives a "technique scroll" item or a system message
 * confirming they've learned the technique. In v1, we send a system message.
 *
 * <p>For post-canon gifts with no canonOriginId, we grant a generic
 * "spirit essence" item (if one exists) or just the dialogue.
 *
 * <h2>v1 scope</h2>
 * <ul>
 *   <li>Only processes EntityCultivator with character_id starting with "npc_wang_lin_manifestation"</li>
 *   <li>Only the FIRST available gift that passes is offered (not all at once)</li>
 *   <li>Cooldown of 6000 ticks (5 minutes) between gift requests</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = "ergenverse", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ManifestationGiftHandler {

    private ManifestationGiftHandler() {}

    /** Minimum ticks between gift requests (5 minutes). */
    private static final int GIFT_COOLDOWN_TICKS = 6000;

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // Must be on server, must have entity and player
        if (event.getLevel().isClientSide()) return;
        if (!(event.getTarget() instanceof EntityCultivator cultivator)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String characterId = cultivator.getCharacterId();
        if (!characterId.startsWith("npc_wang_lin_manifestation")) return;

        // Check cooldown via player persistent data
        long currentTime = player.level().getGameTime();
        long lastGiftTime = player.getPersistentData().getLong("ergenverse_last_gift_time");
        if (currentTime - lastGiftTime < GIFT_COOLDOWN_TICKS) {
            long remaining = (GIFT_COOLDOWN_TICKS - (currentTime - lastGiftTime)) / 20;
            player.sendSystemMessage(Component.literal(
                "\u00A77Wang Lin's manifestation says nothing. Come back in " + remaining + " seconds."));
            return;
        }

        // Get player's cultivation state
        var stateOpt = CultivationCapability.get(player);
        if (!stateOpt.isPresent()) {
            player.sendSystemMessage(Component.literal(
                "\u00A77The manifestation watches you silently. You feel nothing from it."));
            return;
        }
        CultivationState state = stateOpt.resolve().get();
        RealmId realm = state.getCurrentRealm();

        // Build PlayerStateSnapshot
        ManifestationGiftSystem.PlayerStateSnapshot snapshot = new ManifestationGiftSystem.PlayerStateSnapshot() {
            @Override
            public int getAffinity(String protagonistId) {
                // v1: Wang Lin manifestation affinity = 60 (mid-trust, enough for most gifts)
                // Other protagonists = 20
                if ("wang_lin".equals(protagonistId)) return 60;
                return 20;
            }
            @Override
            public int getRealmTier() {
                return realm.order;
            }
            @Override
            public boolean hasDao(String daoId) {
                var daoMap = state.getDaoComprehension();
                return daoMap.containsKey(daoId) && daoMap.get(daoId) > 0.1;
            }
            @Override
            public int getObservedActionTrust(String protagonistId) {
                // v1: same as affinity for simplicity
                return getAffinity(protagonistId);
            }
        };

        // Get Wang Lin's gifts, sorted by affinity threshold (lowest first = most accessible)
        List<ManifestationGiftSystem.GiftRecord> wangLinGifts =
                ManifestationGiftSystem.getGiftsByProtagonist("wang_lin");
        wangLinGifts.sort((a, b) -> Integer.compare(a.affinityThreshold, b.affinityThreshold));

        // Find the first gift that passes evaluation
        ManifestationGiftSystem.GiftRecord offeredGift = null;
        for (var gift : wangLinGifts) {
            ManifestationGiftSystem.GiftDecision decision =
                    ManifestationGiftSystem.evaluateGift(gift, snapshot);
            if (decision == ManifestationGiftSystem.GiftDecision.OFFERED) {
                offeredGift = gift;
                break;
            }
            // Log the refusal for debugging
            Ergenverse.LOGGER.debug("[ManifestationGift] Gift {} refused: {}",
                    gift.giftId, decision.name());
        }

        if (offeredGift == null) {
            // No gifts available — explain why
            // Check the first gift's refusal reason for a meaningful message
            if (!wangLinGifts.isEmpty()) {
                var firstGift = wangLinGifts.get(0);
                var firstDecision = ManifestationGiftSystem.evaluateGift(firstGift, snapshot);
                String dialogue = ManifestationGiftSystem.getDialogueFor(firstGift, firstDecision);
                player.sendSystemMessage(Component.literal("\u00A78Wang Lin's manifestation looks at you in silence, then says:"));
                player.sendSystemMessage(Component.literal(dialogue));
            } else {
                player.sendSystemMessage(Component.literal(
                        "\u00A77The manifestation has nothing to offer you yet."));
            }
            return;
        }

        // Gift is offered! Play the dialogue
        String offerDialogue = offeredGift.offerDialogue;
        player.sendSystemMessage(Component.literal("\u00A7a\u00A7lWang Lin's manifestation: \u00A7r"));
        player.sendSystemMessage(Component.literal(offerDialogue));

        // Attempt to grant the item if it maps to a registered Forge item
        if (offeredGift.canonOriginId != null) {
            // Try to find the item in the mod's registry
            var itemKey = new net.minecraft.resources.ResourceLocation("ergenverse", offeredGift.canonOriginId);
            Item item = ForgeRegistries.ITEMS.getValue(itemKey);
            if (item != null) {
                ItemStack stack = new ItemStack(item, 1);
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false, false);
                }
                player.sendSystemMessage(Component.literal(
                        "\u00A7eReceived: " + offeredGift.name + "\u00A77 (exact copy)"));
            } else {
                // Item not yet registered — inform player
                player.sendSystemMessage(Component.literal(
                        "\u00A77He offers " + offeredGift.name +
                        ", but the item is not yet registered in the world. (Coming soon.)"));
            }
        } else {
            // Technique or post-canon gift — no physical item
            player.sendSystemMessage(Component.literal(
                    "\u00A77He offers to teach you: " + offeredGift.name));
        }

        // Record cooldown
        player.getPersistentData().putLong("ergenverse_last_gift_time", currentTime);
        player.getPersistentData().putBoolean("ergenverse_gift_dirty", true);

        // ── Layer 3: Record in emergent history ──
        String protagonistId = "wang_lin"; // v1: all manifestations are Wang Lin's
        String itemName = offeredGift.name != null ? offeredGift.name : "unknown";
        dev.ergenverse.history.HistoryManager.onGiftReceived(
                player, protagonistId, itemName, currentTime);

        Ergenverse.LOGGER.info("[ManifestationGift] Gift {} offered to player {}",
                offeredGift.giftId, player.getName().getString());
    }
}