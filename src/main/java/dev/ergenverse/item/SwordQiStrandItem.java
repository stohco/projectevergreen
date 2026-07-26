package dev.ergenverse.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Sword Qi Strand (剑气丝) — CRON-COMPLETIONIST-118.
 *
 * <p>A strand of sword qi extracted from 凌天侯 (Ling Tianhou), the 剑尊
 * (Sword Venerable) of 大罗剑宗 (Da Luo Sword Sect). Canon (web-search
 * verified 2026-07-27, Baidu Baike https://baike.baidu.com/item/凌天侯/65285935
 * + Sohu https://www.sohu.com/a/849321229_568249): Ling Tianhou PERSONALLY
 * gave Wang Lin TWO STRANDS OF SWORD QI (两道剑气) to rebuild Wang Ping's
 * body after Liu Mei refined him into a 怨婴 (resentment infant) for ~100
 * years.
 *
 * <p>The sword qi is described as "自身两道剑气" (his own two strands of sword
 * qi) — suggesting it is intrinsic power extracted from Ling Tianhou's body,
 * NOT a pre-existing physical treasure. The mod represents it as an item for
 * gameplay purposes (the player carries it in inventory and uses it at the
 * Suzaku Tomb to trigger the Wang Ping redemption event).
 *
 * <h2>Canon Basis</h2>
 * <p>Per Sohu (https://www.sohu.com/a/849321229_568249): "当得知王林儿子王平
 * 遭遇重创时，凌天侯果断将自身两道剑气交于王林。这两道剑气成为日后王平重塑
 * 肉身的关键因素，体现了凌天侯对王林的真心帮助" — "When Ling Tianhou learned
 * that Wang Lin's son Wang Ping had been gravely injured, he decisively gave
 * his own two strands of sword qi to Wang Lin. These two strands of sword qi
 * became the key factor in Wang Ping's later body reconstruction, reflecting
 * Ling Tianhou's sincere help toward Wang Lin."
 *
 * <p>The web-search subagent's report flagged two UNVERIFIED claims from
 * CRON-117 that this item does NOT implement:
 * <ul>
 *   <li><b>"One qi condenses flesh, one condenses soul"</b> — UNVERIFIED.
 *       No source differentiates the two strands' individual functions. All
 *       sources treat the two strands as a collective unit used for body
 *       reconstruction. The mod treats them as a single item type (stack size 2).</li>
 *   <li><b>"Originally a life-saving treasure given to a disciple"</b> —
 *       UNVERIFIED. All sources say Ling Tianhou gave the sword qi DIRECTLY
 *       to Wang Lin, not through an intermediary disciple.</li>
 * </ul>
 *
 * <h2>Item Properties</h2>
 * <ul>
 *   <li><b>Stack size</b>: 2 (canon: exactly two strands). The player can
 *       hold at most 2 in a single stack.</li>
 *   <li><b>Rarity</b>: EPIC (gold text) — this is a 净涅后期 cultivator's
 *       intrinsic power, extracted and given as a gift. It is one of the
 *       most powerful items in the mod.</li>
 *   <li><b>Consumable</b>: NOT consumed on use — the sword qi is "used"
 *       by right-clicking the INHERITED Cultivation Planet Crystal at the
 *       Suzaku Tomb (the Wang Ping redemption event consumes both strands
 *       from the player's inventory).</li>
 *   <li><b>Tooltip</b>: displays a canon-faithful bilingual description.</li>
 * </ul>
 *
 * <h2>Acquisition</h2>
 * <p>The ONLY way to obtain this item is by right-clicking the Ling Tianhou
 * NPC at the Da Luo Sword Sect (5000, 5000). The LingTianhouSwordQiGrantEvent
 * (CRON-118) handles the grant. The item is NOT craftable, NOT lootable,
 * and NOT tradeable — it is a unique quest reward.
 *
 * <h2>Use</h2>
 * <p>The item is NOT directly usable (no right-click effect). Instead, the
 * player carries it in their inventory and right-clicks the INHERITED
 * Cultivation Planet Crystal at the Suzaku Tomb. The CultivationPlanetCrystalBlock.use()
 * method checks for this item in the player's inventory as a prerequisite
 * for the Wang Ping redemption event (CRON-117/118).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see dev.ergenverse.wanglin.bead.LingTianhouSwordQiGrantEvent (acquisition)
 * @see dev.ergenverse.block.CultivationPlanetCrystalBlock#use (consumption)
 * @see dev.ergenverse.wanglin.bead.WangPingRedemptionEvent (effect)
 */
public class SwordQiStrandItem extends Item {

    /**
     * The NBT tag storing the "strand index" (1 or 2) — canon: there are
     * exactly two strands. Used for tooltip display only; the strands are
     * functionally identical (the "one flesh, one soul" differentiation is
     * UNVERIFIED and not implemented).
     */
    public static final String NBT_STRAND_INDEX = "Ergen.SwordQi.StrandIndex";

    /**
     * Construct a SwordQiStrandItem with the given properties.
     * Callers should pass properties that set:
     * <ul>
     *   <li>{@code stacksTo(2)} — canon: exactly two strands</li>
     *   <li>{@code rarity(Rarity.EPIC)} — gold text, reflects the item's power</li>
     * </ul>
     *
     * @param properties item behavior properties
     */
    public SwordQiStrandItem(Properties properties) {
        super(properties);
    }

    /**
     * Set the strand index on an ItemStack. Used by the
     * LingTianhouSwordQiGrantEvent when granting the two strands — the
     * first strand gets index 1, the second gets index 2.
     *
     * @param stack  the sword qi strand stack
     * @param index  the strand index (1 or 2)
     */
    public void setStrandIndex(ItemStack stack, int index) {
        stack.getOrCreateTag().putInt(NBT_STRAND_INDEX, index);
    }

    /**
     * Get the strand index from an ItemStack. Returns 1 if the tag is
     * missing (defensive — default to strand 1).
     *
     * @param stack the sword qi strand stack
     * @return the strand index (1 or 2)
     */
    public int getStrandIndex(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return 1;
        return stack.getTag().getInt(NBT_STRAND_INDEX);
    }

    /**
     * Append canon-faithful bilingual tooltip lines to the item stack.
     * Displays:
     * <ul>
     *   <li>The strand index (1 of 2, or 2 of 2).</li>
     *   <li>A bilingual description of the sword qi's origin and purpose.</li>
     * </ul>
     *
     * @param stack        the item stack
     * @param level        the level (nullable)
     * @param components   the tooltip components list (append to this)
     * @param tooltipFlag  the tooltip flag (creative/advanced)
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                  List<Component> components, TooltipFlag tooltipFlag) {
        int index = getStrandIndex(stack);
        components.add(Component.literal(
                "剑尊凌天侯的剑气 · 第 " + index + " 道 / 共 2 道")
                .withStyle(ChatFormatting.GOLD));
        components.add(Component.literal(
                "Sword Venerable Ling Tianhou's Sword Qi — Strand " + index + " of 2")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        components.add(Component.literal(
                "用于在朱雀墓重塑王平的肉身。")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        components.add(Component.literal(
                "Used to rebuild Wang Ping's body at the Suzaku Tomb.")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        components.add(Component.literal(
                "右击已传承的修炼星晶以触发救赎事件。")
                .withStyle(ChatFormatting.GRAY));
        components.add(Component.literal(
                "Right-click the inherited Cultivation Planet Crystal to trigger the redemption.")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, components, tooltipFlag);
    }

    /**
     * Whether the item is enchantable. Returns false — sword qi is intrinsic
     * power, not a tool that can be enchanted.
     *
     * @param stack the item stack
     * @return false (never enchantable)
     */
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    /**
     * Whether the item is book-enchantable (enchanting table). Returns false.
     *
     * @param stack the item stack
     * @return false (never enchantable via enchanting table)
     */
    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }
}
