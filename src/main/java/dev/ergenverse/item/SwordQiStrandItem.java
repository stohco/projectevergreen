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
 * Sword Qi Strand (剑气丝) — CRON-COMPLETIONIST-118 (initial), CRON-122 (strand-type differentiation).
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
 * <h2>CRON-122 — Canon-Faithful Strand-Type Differentiation (FLESH vs SOUL_GUARD)</h2>
 *
 * <p><b>Canon source (Baidu Baike 王平 entry, PRIMARY, verified 2026-07-27):</b>
 * <a href="https://baike.baidu.com/item/王平/62563845">https://baike.baidu.com/item/王平/62563845</a>:
 * <pre>
 *   "因王平体内蕴含怨魂、无法正常修炼与转世，王林后来以大罗剑宗剑尊凌天侯的
 *    两道剑气为其重塑肉身——一道化作王平的血肉之躯，另一道守护其魂魄，
 *    使王平得以凡人身份存活于世"
 * </pre>
 * Translation: "Because Wang Ping's body contained resentment souls and could
 * not cultivate or reincarnate normally, Wang Lin later used two strands of
 * sword qi from Ling Tianhou — the Sword Venerable of Da Luo Sword Sect — to
 * rebuild his body. ONE strand became Wang Ping's fleshly body, the OTHER
 * guarded his soul, allowing Wang Ping to live as a mortal."
 *
 * <p><b>Secondary sources confirming the differentiation:</b>
 * <ul>
 *   <li>Sohu (https://www.sohu.com/a/918455069_122415633)</li>
 *   <li>Zhihu (https://zhuanlan.zhihu.com/p/1950183611853692943)</li>
 *   <li>Sohu (https://www.sohu.com/a/1018020266_122415633)</li>
 * </ul>
 *
 * <p><b>Correction of prior CRON-118 self-critique #2:</b> CRON-118's research
 * subagent had claimed "no source differentiates the two strands' individual
 * functions" and marked the differentiation as UNVERIFIED. CRON-120's deeper
 * research found the Baidu Baike 王平 dedicated entry (a primary source)
 * explicitly differentiates the two strands. CRON-122 implements the
 * canon-faithful differentiation:
 * <ul>
 *   <li><b>{@link StrandType#FLESH}</b> — the strand that "化作王平的血肉之躯"
 *       (became Wang Ping's fleshly body). Strand index 1 in the grant event.</li>
 *   <li><b>{@link StrandType#SOUL_GUARD}</b> — the strand that "守护其魂魄"
 *       (guarded his soul). Strand index 2 in the grant event.</li>
 * </ul>
 *
 * <p><b>Gameplay mechanic:</b> the Wang Ping redemption event now requires
 * <b>exactly 1 FLESH strand AND 1 SOUL_GUARD strand</b> (not ≥2 of either).
 * This is enforced in {@link dev.ergenverse.block.CultivationPlanetCrystalBlock#use}.
 * A player who somehow obtains 2 FLESH strands (e.g., via creative mode) cannot
 * trigger the redemption — both functions are canon-required. This closes
 * CRON-118 self-critique #2's "incorrectly treats the two strands as
 * functionally identical" gap.
 *
 * <h2>Item Properties</h2>
 * <ul>
 *   <li><b>Stack size</b>: 1 (each strand is unique and stackable only with
 *       strands of the same type — but in practice the player holds at most
 *       1 of each type). Set to 1 to prevent two FLESH strands stacking and
 *       bypassing the "1 of each type" requirement via stack-count checks.</li>
 *   <li><b>Rarity</b>: EPIC (gold text) — this is a 净涅后期 cultivator's
 *       intrinsic power, extracted and given as a gift. It is one of the
 *       most powerful items in the mod.</li>
 *   <li><b>Consumable</b>: NOT consumed on use — the sword qi is "used"
 *       by right-clicking the INHERITED Cultivation Planet Crystal at the
 *       Suzaku Tomb (the Wang Ping redemption event consumes both strands
 *       from the player's inventory).</li>
 *   <li><b>Tooltip</b>: displays a canon-faithful bilingual description
 *       including the strand's specific function (FLESH or SOUL_GUARD).</li>
 * </ul>
 *
 * <h2>Acquisition</h2>
 * <p>The ONLY way to obtain this item is by right-clicking the Ling Tianhou
 * NPC at the Da Luo Sword Sect (5000, 5000). The LingTianhouSwordQiGrantEvent
 * (CRON-118, updated CRON-122) handles the grant: it grants ONE FLESH strand
 * (strand_index=1) and ONE SOUL_GUARD strand (strand_index=2). The item is
 * NOT craftable, NOT lootable, and NOT tradeable — it is a unique quest reward.
 *
 * <h2>Use</h2>
 * <p>The item is NOT directly usable (no right-click effect). Instead, the
 * player carries it in their inventory and right-clicks the INHERITED
 * Cultivation Planet Crystal at the Suzaku Tomb. The CultivationPlanetCrystalBlock.use()
 * method checks for exactly 1 FLESH + 1 SOUL_GUARD strand in the player's
 * inventory as a prerequisite for the Wang Ping redemption event
 * (CRON-117/118/122).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see StrandType (the FLESH vs SOUL_GUARD differentiation)
 * @see dev.ergenverse.wanglin.bead.LingTianhouSwordQiGrantEvent (acquisition)
 * @see dev.ergenverse.block.CultivationPlanetCrystalBlock#use (consumer)
 * @see dev.ergenverse.wanglin.bead.WangPingRedemptionEvent (effect)
 */
public class SwordQiStrandItem extends Item {

    /**
     * The NBT tag storing the "strand index" (1 or 2) — canon: there are
     * exactly two strands. Used for tooltip display and to pair with
     * {@link #NBT_STRAND_TYPE} to ensure each strand has a unique function.
     *
     * <p>Strand 1 is always {@link StrandType#FLESH}; strand 2 is always
     * {@link StrandType#SOUL_GUARD}. The two fields are stored separately
     * so the consumer logic can directly check the strand type without
     * needing to map index → type at every check site.
     */
    public static final String NBT_STRAND_INDEX = "Ergen.SwordQi.StrandIndex";

    /**
     * The NBT tag storing the {@link StrandType} ordinal name (FLESH or
     * SOUL_GUARD). CRON-122 adds this tag to canon-differentiate the two
     * strands' functions.
     *
     * <p>Stored as a String (the enum name) for human-readable NBT inspection
     * via /data get. The String is parsed back via {@link StrandType#valueOf}
     * in {@link #getStrandType}; an unknown value falls back to FLESH
     * (defensive — the default strand index is 1, which pairs with FLESH).
     */
    public static final String NBT_STRAND_TYPE = "Ergen.SwordQi.StrandType";

    /**
     * Construct a SwordQiStrandItem with the given properties.
     * Callers should pass properties that set:
     * <ul>
     *   <li>{@code stacksTo(1)} — CRON-122: lowered from 2 to 1 to prevent
     *       two strands of the same type stacking and bypassing the
     *       "1 FLESH + 1 SOUL_GUARD" requirement via stack-count checks.</li>
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
     * Set the strand type on an ItemStack. CRON-122.
     *
     * <p>Used by the LingTianhouSwordQiGrantEvent when granting the two
     * strands — the first strand (index 1) gets {@link StrandType#FLESH},
     * the second (index 2) gets {@link StrandType#SOUL_GUARD}.
     *
     * @param stack  the sword qi strand stack
     * @param type   the strand type (FLESH or SOUL_GUARD)
     */
    public void setStrandType(ItemStack stack, StrandType type) {
        stack.getOrCreateTag().putString(NBT_STRAND_TYPE, type.name());
    }

    /**
     * Get the strand type from an ItemStack. CRON-122.
     *
     * <p>Returns {@link StrandType#FLESH} if the tag is missing (defensive —
     * the default strand index is 1, which pairs with FLESH). If the stored
     * value is an unknown string (e.g., a corrupted save from a future
     * version), also returns FLESH.
     *
     * @param stack the sword qi strand stack
     * @return the strand type (never null)
     */
    public StrandType getStrandType(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return StrandType.FLESH;
        String name = stack.getTag().getString(NBT_STRAND_TYPE);
        if (name == null || name.isEmpty()) return StrandType.FLESH;
        try {
            return StrandType.valueOf(name);
        } catch (IllegalArgumentException e) {
            // Unknown tag value (corrupted save or future version) —
            // fall back to FLESH (the strand index 1 default).
            return StrandType.FLESH;
        }
    }

    /**
     * Append canon-faithful bilingual tooltip lines to the item stack.
     * Displays:
     * <ul>
     *   <li>The strand index (1 of 2, or 2 of 2).</li>
     *   <li>The strand type (FLESH or SOUL_GUARD) with canon-faithful
     *       bilingual function description.</li>
     *   <li>The canon source citation.</li>
     *   <li>Usage instructions.</li>
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
        StrandType type = getStrandType(stack);

        // Strand index header
        components.add(Component.literal(
                "剑尊凌天侯的剑气 · 第 " + index + " 道 / 共 2 道")
                .withStyle(ChatFormatting.GOLD));
        components.add(Component.literal(
                "Sword Venerable Ling Tianhou's Sword Qi — Strand " + index + " of 2")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));

        // CRON-122: Strand-type-specific canon-faithful description
        components.add(type.tooltipLineCn());
        components.add(type.tooltipLineEn());

        // Usage instruction
        components.add(Component.literal(
                "右击已传承的修炼星晶以触发救赎事件。")
                .withStyle(ChatFormatting.GRAY));
        components.add(Component.literal(
                "Right-click the inherited Cultivation Planet Crystal to trigger the redemption.")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

        // Canon source citation
        components.add(Component.literal(
                "Canon: Baidu Baike 王平 — \"一道化作王平的血肉之躯，另一道守护其魂魄\"")
                .withStyle(ChatFormatting.DARK_GRAY));
        components.add(Component.literal(
                "One strand became Wang Ping's fleshly body, the other guarded his soul.")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

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

    /**
     * StrandType — the canon-faithful differentiation between the two sword
     * qi strands given by Ling Tianhou to Wang Lin for rebuilding Wang Ping's
     * body. CRON-122.
     *
     * <p><b>Canon source (Baidu Baike 王平 entry, verified 2026-07-27):</b>
     * "一道化作王平的血肉之躯，另一道守护其魂魄" — "ONE strand became Wang
     * Ping's fleshly body, the OTHER guarded his soul."
     *
     * <p>Each strand has a distinct function in the canon:
     * <ul>
     *   <li>{@link #FLESH} — "化作王平的血肉之躯" — became Wang Ping's
     *       fleshly body. This is the strand that materialized Wang Ping's
     *       physical form (a sword-qi construct that outwardly appears human).</li>
     *   <li>{@link #SOUL_GUARD} — "守护其魂魄" — guarded his soul. This is
     *       the strand that sustained Wang Ping's soul, keeping it anchored
     *       to the sword-qi body so it could not disperse or be claimed by
     *       the resentment infant's remnant will.</li>
     * </ul>
     *
     * <p>Both strands are canon-required for the Wang Ping redemption event.
     * The redemption prerequisite in
     * {@link dev.ergenverse.block.CultivationPlanetCrystalBlock#use} enforces
     * exactly 1 of each type — a player with 2 FLESH strands (e.g., via
     * creative mode) cannot trigger the redemption.
     *
     * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
     */
    public enum StrandType {
        /**
         * The FLESH strand — "化作王平的血肉之躯" (became Wang Ping's fleshly body).
         *
         * <p>Granted as strand index 1 by
         * {@link dev.ergenverse.wanglin.bead.LingTianhouSwordQiGrantEvent}.
         * This strand materialized Wang Ping's physical form — a sword-qi
         * construct that outwardly appears as a mortal boy (handsome features,
         * pure eyes) but is inwardly a sword-qi body that cannot cry, cannot
         * sire children, has no cultivation talent, and cannot sense spiritual qi.
         *
         * <p>Canon source: Baidu Baike 王平 entry — "一道化作王平的血肉之躯"
         * (one strand became Wang Ping's fleshly body).
         */
        FLESH(
            "【凝肉】此道剑气化作王平的血肉之躯，使之外貌如凡人少年。",
            "[FLESH] This strand became Wang Ping's fleshly body — outwardly a mortal boy."
        ),

        /**
         * The SOUL_GUARD strand — "守护其魂魄" (guarded his soul).
         *
         * <p>Granted as strand index 2 by
         * {@link dev.ergenverse.wanglin.bead.LingTianhouSwordQiGrantEvent}.
         * This strand sustained Wang Ping's soul — anchoring it to the
         * sword-qi body so it could not disperse or be claimed by the
         * resentment infant's remnant will. Without this strand, the
         * sword-qi body would be an empty shell; with it, Wang Ping's
         * soul persists as a "False Life" (虚假生命).
         *
         * <p>Canon source: Baidu Baike 王平 entry — "另一道守护其魂魄"
         * (the other guarded his soul).
         */
        SOUL_GUARD(
            "【凝魂】此道剑气守护王平的魂魄，使其虚假生命得以延续。",
            "[SOUL_GUARD] This strand guards Wang Ping's soul — sustaining his False Life."
        );

        private final String tooltipLineCn;
        private final String tooltipLineEn;

        StrandType(String cn, String en) {
            this.tooltipLineCn = cn;
            this.tooltipLineEn = en;
        }

        /** The canon-faithful Chinese tooltip line for this strand type. */
        public Component tooltipLineCn() {
            return Component.literal(tooltipLineCn).withStyle(ChatFormatting.LIGHT_PURPLE);
        }

        /** The canon-faithful English tooltip line for this strand type. */
        public Component tooltipLineEn() {
            return Component.literal(tooltipLineEn)
                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC);
        }
    }
}
