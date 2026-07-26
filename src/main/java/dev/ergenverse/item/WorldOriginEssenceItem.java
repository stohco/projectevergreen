package dev.ergenverse.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * World Origin Essence (一界本源) — CRON-COMPLETIONIST-101.
 *
 * <p>The canon-attested reagent required for the final successful revival of
 * Li Muwan. Closes the CRON-100 documented mod-fidelity bridge where the
 * success path required only {@code RealmId.TRANSCENDENCE} (Fourth Step) but
 * omitted the {@code 一界本源} requirement that the novel explicitly names.
 *
 * <h2>Canon Basis (fact-checked via web-search 2026-07-26)</h2>
 * <p>In the novel 仙逆 by 耳根:
 * <ul>
 *   <li><b>Baidu Baike (李慕婉):</b> "《仙逆》结局中王林踏入第四步后，
 *       成功运用一界本源将之复活" — Wang Lin enters the Fourth Step and
 *       uses 一界本源 (the origin of a world) to revive Li Muwan.</li>
 *   <li><b>Source of the essence:</b> Wang Lin extracts the origin of a
 *       world he controls (the 逆尘界 / Ni Chen Realm — Wang Lin's own
 *       world). "抽取一界本源重塑了李慕婉的魂魄" — he extracts a world's
 *       origin to reshape Li Muwan's soul.</li>
 *   <li><b>Effect:</b> "以一界本源，加入她的肉身，将逆尘界的规则融入其身，
 *       使其超脱" — by fusing the world-origin into her body, the rules of
 *       the Ni Chen Realm integrate with her, enabling her to transcend the
 *       cycle of life and death.</li>
 *   <li><b>Deeper detail (mod-noted, not implemented):</b> the act is
 *       actually performed by 戮默 (Lu Mo), Wang Lin's clone/avatar, who
 *       loves Li Muwan most. The mod treats this as Wang Lin's act for
 *       simplicity — the player IS Wang Lin.</li>
 * </ul>
 *
 * <p><b>Canon honesty:</b> NO fabricated chapter citation. The 一界本源 is
 * canon-attested via multiple web-search sources (Baidu Baike, 360娱乐,
 * 批踢踢實業坊). The exact chapter is NOT cited to avoid fabrication. The
 * novel clearly establishes that 一界本源 is the reagent Wang Lin uses to
 * revive Li Muwan after entering the Fourth Step.
 *
 * <h2>Mod Mechanics</h2>
 * <p>The item is a <b>passive reagent</b> — it has no right-click use of its
 * own. Instead, the {@link dev.ergenverse.wanglin.bead.RevivalAttemptService}
 * checks for its presence as a 6th gate (the ESSENCE GATE) when the player
 * attempts the final successful revival (TRANSCENDENCE realm + 137 prior
 * failed attempts). On success, one essence item is consumed.
 *
 * <p><b>Item properties:</b>
 * <ul>
 *   <li>Rarity: EPIC (gold tooltip, enchanted glint)</li>
 *   <li>Stack size: 1 (each essence is a unique world-tier artifact)</li>
 *   <li>Fire-resistant: YES (a world-origin cannot be destroyed by fire)</li>
 *   <li>No right-click use — passive reagent only</li>
 * </ul>
 *
 * <h2>Obtaining the Item</h2>
 * <p><b>Current sources (CRON-101):</b>
 * <ul>
 *   <li>Creative tab (testing): "Ergenverse Items" tab.</li>
 *   <li>Command: {@code /ergenverse give world_origin_essence} (testing).</li>
 * </ul>
 *
 * <p><b>Future canon-faithful sources (NOT implemented in CRON-101):</b>
 * <ul>
 *   <li><b>Suzaku Tomb (朱雀墓) loot table:</b> the underground inheritance
 *       site where Wang Lin obtains the Heaven-Defying Bead. A natural
 *       location for a world-tier reagent to appear.</li>
 *   <li><b>Fourth Step ascension event:</b> when the player achieves
 *       TRANSCENDENCE realm, they could automatically receive one essence
 *       (representing Wang Lin's mastery over his own world).</li>
 *   <li><b>World-boss drop:</b> a world-tier boss (e.g., an Ancient God
 *       remnant) could drop the essence on defeat.</li>
 * </ul>
 *
 * <p>These future sources are documented for canon-faithfulness. CRON-101
 * ships the item, the gate, the consumption mechanic, and the creative/command
 * sources. A future CRON will add the canon-faithful acquisition path.
 *
 * <h2>Architecture</h2>
 * <p>The item is registered in {@link ErgenverseItems#WORLD_ORIGIN_ESSENCE}
 * via the Forge DeferredRegister pattern. The item class itself does NOT
 * touch the WorldDeltaStore, WorldFacade, or any layer — it is purely an
 * item-NBT artifact. This respects the CRON-69 architecture: item state is
 * Wang Lin's personal state, not world state.
 *
 * <p>The 6th gate logic lives in {@link RevivalAttemptService#attemptRevival},
 * which calls the static helper {@link #findInInventory} to locate the
 * essence in the player's inventory (mirrors the CRON-95/99/100 findBead
 * pattern). On success, the service calls {@link ItemStack#shrink}(1) on
 * the located stack to consume it.
 *
 * <h2>Single-Player Maximalism (Article XLIII)</h2>
 * <p>The item is unique to Wang Lin (the player). There is no fairness
 * concern — the player IS Wang Lin, and the World Origin Essence is his
 * to command. The single-stack-size and consumption-on-success reflect the
 * canon narrative: Wang Lin uses ONE world's origin to revive Li Muwan,
 * and that world is irreversibly consumed.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see RevivalAttemptService#attemptRevival
 * @see ErgenverseItems#WORLD_ORIGIN_ESSENCE
 */
public class WorldOriginEssenceItem extends Item {

    /**
     * The NBT tag that records which world this essence was extracted from.
     * Canon-attested example: 逆尘界 (Ni Chen Realm — Wang Lin's own world).
     * Default value: "未明之界" (Unnamed World) — represents the player's
     * yet-unnamed own world. Future CRON could let the player name their
     * world and have that name appear here.
     */
    public static final String NBT_SOURCE_WORLD = "Ergenverse.WorldOriginEssence.SourceWorld";

    /**
     * Default source-world name when the item is created without one.
     * "未明之界" = "Unnamed World" in Chinese. Represents the player's
     * yet-unnamed own world — a mod-original placeholder for the canon
     * 逆尘界 (Ni Chen Realm) that Wang Lin eventually names and rules.
     */
    public static final String DEFAULT_SOURCE_WORLD = "未明之界";

    public WorldOriginEssenceItem(Properties props) {
        super(props.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }

    // ── Tooltip ──────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        // Header — bilingual
        tooltip.add(Component.literal("一界本源  ·  World Origin Essence")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));

        // Source world (from NBT, or default)
        String sourceWorld = stack.hasTag() && stack.getTag().contains(NBT_SOURCE_WORLD)
                ? stack.getTag().getString(NBT_SOURCE_WORLD)
                : DEFAULT_SOURCE_WORLD;
        tooltip.add(Component.literal("Source World: " + sourceWorld)
                .withStyle(ChatFormatting.DARK_PURPLE));

        // Canon significance
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("The condensed origin of an entire world —")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("its rules, its essence, its very being.")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("Canon: Wang Lin uses 一界本源 to revive")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Li Muwan after entering the Fourth Step.")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Without this essence, even the Fourth")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Step cannot restore her.")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§6§lRequired for the final successful revival:")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7  · Li Muwan's soul in the bead (CRON-99)")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7  · 137 failed revival attempts (CRON-100)")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7  · Fourth Step (TRANSCENDENCE) realm")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7  · World Origin Essence (this item) — consumed")
                .withStyle(ChatFormatting.GRAY));
    }

    // ── Static helpers ───────────────────────────────────────────────

    /**
     * Create a World Origin Essence stack sourced from a specific world.
     * Used by future canon-faithful acquisition paths (e.g., the Suzaku
     * Tomb loot table, or the Fourth Step ascension event).
     *
     * @param sourceWorldName the name of the world whose origin this
     *                        essence represents (e.g., "逆尘界" for
     *                        Wang Lin's Ni Chen Realm)
     * @return a new ItemStack with the source-world NBT tag set
     */
    public static ItemStack createFromWorld(String sourceWorldName) {
        ItemStack stack = new ItemStack(ErgenverseItems.WORLD_ORIGIN_ESSENCE.get());
        stack.getOrCreateTag().putString(NBT_SOURCE_WORLD, sourceWorldName);
        return stack;
    }

    /**
     * Create a World Origin Essence stack with the default source world.
     * Used by the creative tab and the /ergenverse give command.
     *
     * @return a new ItemStack with the default source-world NBT tag
     */
    public static ItemStack createDefault() {
        return createFromWorld(DEFAULT_SOURCE_WORLD);
    }

    /**
     * Locate a World Origin Essence stack in the player's inventory.
     * Mirrors the CRON-95/99/100 findBead pattern: main-hand → off-hand
     * → main inventory. Returns {@code ItemStack.EMPTY} if none found.
     *
     * <p>Used by {@link RevivalAttemptService#attemptRevival} to check
     * the 6th gate (ESSENCE GATE) and to consume the item on success.
     *
     * @param player the server player whose inventory to scan
     * @return the located stack, or {@code ItemStack.EMPTY} if none
     */
    public static ItemStack findInInventory(net.minecraft.server.level.ServerPlayer player) {
        // Main hand
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof WorldOriginEssenceItem) {
            return mainHand;
        }
        // Off hand
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof WorldOriginEssenceItem) {
            return offHand;
        }
        // Main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof WorldOriginEssenceItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
