package dev.ergenverse.item;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * CultivationGuideItem — a new cultivator's first item. Right-click to
 * open a multi-page guide explaining the core cultivation loop.
 *
 * <p>This is the "bootstrap" item that ties together the three Qi sources:
 * <ol>
 *   <li><b>Meditation:</b> sneak-right-click a spirit vein (nether_quartz_ore)
 *       to enter meditation. Qi regenerates slowly while meditating.</li>
 *   <li><b>Jade Slip Qi-Gathering:</b> right-click a spirit herb or spirit
 *       vein block with a Jade Slip to harvest Qi directly.</li>
 *   <li><b>Spirit Stone Consumption:</b> right-click a Spirit Stone to
 *       absorb its Qi. Scales with cultivation realm.</li>
 * </ol>
 *
 * <p><b>Canon:</b> In Renegade Immortal, Wang Lin begins as a mortal who
 * knows nothing of cultivation. He learns from Heng Yue Sect elders, from
 * observing other cultivators, and from jade slips containing technique
 * manuals. This item represents that initial guidance.
 *
 * <p>When right-clicked, it cycles through guide pages and gives the
 * player their first Jade Slip if they don't have one.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public class CultivationGuideItem extends Item {

    private static final String[] GUIDE_PAGES = {
        "\u00A76\u00A7l\u2726 The Path of Cultivation \u00A7r\u00A77\n\n" +
        "\u00A7fYou have taken your first step on the cultivation path.\n\n" +
        "\u00A77Cultivation is not leveling. It is a journey of comprehension,\n" +
        "Qi accumulation, and tribulation survival.\n\n" +
        "\u00A7e\u00A7lYour Goal:\u00A7r\u00A77 Break through the realms:\n" +
        "\u00A7f  Mortal \u2192 Qi Condensation \u2192 Foundation \u2192 Core Formation\n" +
        "\u00A7f  \u2192 Nascent Soul \u2192 Soul Formation \u2192 and beyond...\n\n" +
        "\u00A77Right-click again for Page 2: Qi Sources",

        "\u00A76\u00A7l\u2726 Qi Sources \u00A7r\u00A77\n\n" +
        "\u00A7fQi is spiritual energy. You need it to break through.\n\n" +
        "\u00A7e\u00A7l1. Meditation\u00A7r\u00A77\n" +
        "\u00A7f  Sneak + right-click a spirit vein (quartz ore)\n" +
        "\u00A7f  to enter meditation. Qi regenerates slowly.\n\n" +
        "\u00A7e\u00A7l2. Jade Slip Harvesting\u00A7r\u00A77\n" +
        "\u00A7f  Hold a Jade Slip and right-click a spirit herb\n" +
        "\u00A7f  or spirit vein block to harvest Qi directly.\n\n" +
        "\u00A7e\u00A7l3. Spirit Stone Consumption\u00A7r\u00A77\n" +
        "\u00A7f  Right-click a Spirit Stone to absorb its Qi.\n" +
        "\u00A7f  Higher realm = more Qi per stone.\n\n" +
        "\u00A77Right-click again for Page 3: Breakthrough",

        "\u00A76\u00A7l\u2726 Breakthrough \u00A7r\u00A77\n\n" +
        "\u00A7fTo break through to the next realm, you need:\n\n" +
        "\u00A7e  \u2713 Sufficient Qi\u00A7r\u00A77 (80%+ of max)\n" +
        "\u00A7e  \u2713 Dao Comprehension\u00A7r\u00A77 (varies by realm)\n" +
        "\u00A7e  \u2713 Tribulation Survival\u00A7r\u00A77 (Nascent Soul+)\n\n" +
        "\u00A7c\u00A7lWarning:\u00A7r\u00A77 Breakthrough above Core Formation\n" +
        "\u00A77triggers a Heavenly Tribulation. Lightning strikes\n" +
        "\u00A77the cultivator. A Realm-Sealing Grand Array can\n" +
        "\u00A77mitigate the damage.\n\n" +
        "\u00A77Use \u00A7e/ergen status\u00A77 to check your cultivation.\n" +
        "\u00A77Use \u00A7e/ergen advanced\u00A77 for advanced commands.\n\n" +
        "\u00A76\u00A7lMay the Dao be with you.\u00A7r"
    };

    public CultivationGuideItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }

        // Get or initialize page counter from item NBT
        int page = stack.getOrCreateTag().getInt("GuidePage");
        if (page < 0 || page >= GUIDE_PAGES.length) {
            page = 0;
        }

        // Show current page
        player.sendSystemMessage(Component.literal(GUIDE_PAGES[page]));

        // Play a book page sound
        level.playSound(null, player.blockPosition(),
                SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.5f, 1.2f);

        // Advance page (wrap around)
        int nextPage = (page + 1) % GUIDE_PAGES.length;
        stack.getOrCreateTag().putInt("GuidePage", nextPage);

        // On first use, give the player a Jade Slip if they don't have one
        if (!stack.getOrCreateTag().getBoolean("GivenJadeSlip")) {
            if (!hasJadeSlip(player)) {
                ItemStack jadeSlip = new ItemStack(ErgenverseItems.JADE_SLIP.get());
                if (!player.getInventory().add(jadeSlip)) {
                    player.drop(jadeSlip, false);
                }
                player.sendSystemMessage(Component.literal(
                        "\u00A7a\u2726 You receive a Jade Slip \u2014 your first Qi-channeling tool.")
                        .withStyle(ChatFormatting.GREEN));
            }
            stack.getOrCreateTag().putBoolean("GivenJadeSlip", true);
        }

        // Also check cultivation state — if mortal, give a hint
        CultivationState cs = CultivationCapability.getOrThrow(serverPlayer);
        if (cs.getCurrentRealm() == RealmId.MORTAL) {
            player.sendSystemMessage(Component.literal(
                    "\u00A77\u00A7oYou are a mortal. Find a spirit vein (quartz ore) and " +
                    "meditate to begin your cultivation journey.")
                    .withStyle(ChatFormatting.GRAY));
        }

        return InteractionResultHolder.success(stack);
    }

    /** Check if player already has a jade slip. */
    private boolean hasJadeSlip(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (!s.isEmpty() && s.getItem() == ErgenverseItems.JADE_SLIP.get()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int page = stack.getOrCreateTag().getInt("GuidePage");
        tooltip.add(Component.literal("\u00A77Right-click to read the cultivation guide.")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("\u00A77Page " + (page + 1) + "/" + GUIDE_PAGES.length)
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("\u00A7bGives a Jade Slip on first use.")
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
