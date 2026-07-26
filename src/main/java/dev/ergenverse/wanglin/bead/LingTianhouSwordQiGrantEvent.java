package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.history.HistoryManager;
import dev.ergenverse.item.ErgenverseItems;
import dev.ergenverse.item.SwordQiStrandItem;
import dev.ergenverse.simulation.WorldRuntimeState;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

/**
 * Ling Tianhou Sword Qi Grant Event — CRON-COMPLETIONIST-118.
 *
 * <p>Implements the canon-faithful "Ling Tianhou gives Wang Lin two strands
 * of sword qi" narrative beat. This is the canon-faithful prerequisite
 * acquisition for the Wang Ping redemption event (CRON-117).
 *
 * <p><b>CRON-117 self-critique #4 (closed by CRON-118):</b> the prior
 * WangPingRedemptionEvent used the bead's {@code isLiMuwanRevived} flag
 * as a proxy for "Wang Lin has the prerequisites to channel Ling Tianhou's
 * sword qi". This was chronologically INVERTED — Li Muwan is revived at the
 * END of the novel (Wang Lin at 踏天境 / Transcendence), which is FAR AFTER
 * Wang Ping's redemption (Wang Lin at 问鼎 / Ascendant). CRON-118 removes
 * the Li Muwan revived proxy and replaces it with a canon-faithful
 * acquisition: the player must obtain the Sword Qi Strand item by
 * right-clicking the Ling Tianhou NPC at the Da Luo Sword Sect.
 *
 * <h2>Canon Basis (fact-checked via web-search 2026-07-27)</h2>
 * <p>Per Sohu (https://www.sohu.com/a/849321229_568249): "当得知王林儿子王平
 * 遭遇重创时，凌天侯果断将自身两道剑气交于王林。这两道剑气成为日后王平重塑
 * 肉身的关键因素，体现了凌天侯对王林的真心帮助" — "When Ling Tianhou learned
 * that Wang Lin's son Wang Ping had been gravely injured, he decisively gave
 * his own two strands of sword qi to Wang Lin. These two strands of sword qi
 * became the key factor in Wang Ping's later body reconstruction, reflecting
 * Ling Tianhou's sincere help toward Wang Lin."
 *
 * <p>Canon sources: Baidu Baike (https://baike.baidu.com/item/凌天侯/65285935),
 * Sohu (https://www.sohu.com/a/935257158_122415633,
 * https://www.sohu.com/a/849321229_568249),
 * Zhihu (https://zhuanlan.zhihu.com/p/1957927329482383516),
 * 163.com (https://www.163.com/dy/article/K91BPTNS0556C06B.html).
 * NO fabricated chapter citation — no source explicitly names the chapter
 * of the sword qi transfer.
 *
 * <h2>Trigger Mechanism — Player Right-Click on Ling Tianhou NPC</h2>
 * <p>The grant fires when the player right-clicks the Ling Tianhou
 * EntityCultivator NPC at the Da Luo Sword Sect (5000, 5000). The
 * right-click is dispatched by
 * {@link dev.ergenverse.history.HistoryEvents#onEntityInteract} when the
 * target cultivator's characterId is {@code "ling_tianhou"}.
 *
 * <p>The grant is gated by the following (checked in
 * {@link #handleSwordQiGrant}):
 *
 * <ol>
 *   <li><b>Server-side check.</b> Never run on client.</li>
 *   <li><b>CharacterId check.</b> The target cultivator must have
 *       characterId = "ling_tianhou".</li>
 *   <li><b>Write-once guard.</b> Ling Tianhou's runtime state must NOT have
 *       {@code "sword_qi_granted": true}. The grant is a one-time event per
 *       save. This prevents the player from farming sword qi strands.</li>
 *   <li><b>Inventory space check.</b> The player must have room for 2 items
 *       in their inventory (or the items are dropped at the player's feet).</li>
 * </ol>
 *
 * <h2>On Success</h2>
 * <ol>
 *   <li>Sets Ling Tianhou's runtime state:
 *     <ul>
 *       <li>{@code sword_qi_granted: true}</li>
 *       <li>{@code sword_qi_grant_tick: currentTick}</li>
 *     </ul>
 *   </li>
 *   <li>Grants the player TWO SwordQiStrandItem stacks (one per strand):
 *     <ul>
 *       <li>Strand 1 — {@code strand_index=1}, {@code strand_type=FLESH}
 *           (化作王平的血肉之躯 — became Wang Ping's fleshly body)</li>
 *       <li>Strand 2 — {@code strand_index=2}, {@code strand_type=SOUL_GUARD}
 *           (守护其魂魄 — guarded his soul)</li>
 *     </ul>
 *     CRON-122: the two strands are canon-faithfully differentiated via
 *     {@link SwordQiStrandItem.StrandType}. The redemption prerequisite
 *     now requires exactly 1 FLESH + 1 SOUL_GUARD (not ≥2 of either).
 *     If the player's inventory is full, the items are dropped at the player's
 *     feet (defensive — the grant is write-once, so losing the items is bad).
 *   </li>
 *   <li>Spawns canon-faithful sound effects (AMETHYST_BLOCK_CHIME for the
 *       crystalline sword qi tone, PLAYER_LEVELUP for the gift received).</li>
 *   <li>Displays a bilingual message (Chinese + English).</li>
 *   <li>Records the event in HistoryManager.</li>
 * </ol>
 *
 * <h2>State Transition Diagram</h2>
 * <pre>
 *   CRON-118: Player right-clicks Ling Tianhou at Da Luo Sword Sect
 *             →  Ling Tianhou runtime: sword_qi_granted = true
 *             →  Player inventory: +1 FLESH strand + 1 SOUL_GUARD strand
 *   CRON-117/118/122: Player right-clicks INHERITED Crystal at Suzaku Tomb
 *                 with 1 FLESH + 1 SOUL_GUARD strand in inventory
 *                 + realm >= ASCENDANT (= 问鼎)
 *                 + Wang Ping deadUntilRevived=true
 *                 → Wang Ping materializes as mortal boy on Ranyun Star
 *                 → 1 FLESH + 1 SOUL_GUARD strand consumed from player inventory
 * </pre>
 *
 * <h2>Single-Player Maximalism (Article XLIII)</h2>
 * <p>The grant only fires when the player travels to the Da Luo Sword Sect
 * (5000, 5000) and right-clicks Ling Tianhou. This is a high-friction
 * canon-faithful gate — the player must:
 * <ol>
 *   <li>Travel to the remote Da Luo Sword Sect location (~5000 blocks from spawn).</li>
 *   <li>Find Ling Tianhou (materialized as an EntityCultivator).</li>
 *   <li>Right-click him.</li>
 *   <li>Carry the two sword qi strands to the Suzaku Tomb for the redemption.</li>
 * </ol>
 *
 * <h2>Architecture</h2>
 * <p>This event is a static utility class (mirrors the CRON-110
 * {@link ZhouRuSoulTransferEvent} pattern). The sole entry point is
 * {@link #handleSwordQiGrant(ServerPlayer, EntityCultivator)}, called by
 * {@link dev.ergenverse.history.HistoryEvents#onEntityInteract} when the
 * target cultivator's characterId is "ling_tianhou".
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see dev.ergenverse.history.HistoryEvents#onEntityInteract (trigger)
 * @see dev.ergenverse.item.SwordQiStrandItem (the granted item)
 * @see dev.ergenverse.block.CultivationPlanetCrystalBlock#use (consumer)
 * @see WangPingRedemptionEvent (the redemption event that consumes the sword qi)
 */
public final class LingTianhouSwordQiGrantEvent {

    /**
     * The canon character ID for Ling Tianhou. Must match
     * {@link dev.ergenverse.runtime.CanonUUID#LING_TIANHOU}'s profile and
     * {@link dev.ergenverse.runtime.PlanetSuzakuBlueprint#NPC_LING_TIANHOU}.
     */
    public static final String CHARACTER_ID = "ling_tianhou";

    /**
     * HistoryManager subject for the sword qi grant event. Distinct from
     * other subjects so subscribers can react specifically to this beat.
     */
    public static final String SUBJECT_SWORD_QI_GRANTED = "ling_tianhou_granted_sword_qi_to_wanglin";

    /**
     * The number of sword qi strands granted (canon: exactly 2). The
     * player receives 2 separate ItemStacks (one per strand, each with
     * a distinct NBT strand_index for tooltip display).
     */
    public static final int SWORD_QI_STRAND_COUNT = 2;

    private LingTianhouSwordQiGrantEvent() {}

    /**
     * Handle the player's right-click on Ling Tianhou. Called by
     * {@link dev.ergenverse.history.HistoryEvents#onEntityInteract} when
     * the target cultivator's characterId is "ling_tianhou".
     *
     * <p>The method is fully defensive — it no-ops with appropriate
     * canon-faithful messages for each gate failure. The grant is
     * write-once (per save): once {@code sword_qi_granted} is true in
     * Ling Tianhou's runtime state, subsequent right-clicks display a
     * "already granted" message and no-op.
     *
     * @param player     the server player (Wang Lin) who right-clicked
     * @param lingTianhou the Ling Tianhou EntityCultivator NPC
     */
    public static void handleSwordQiGrant(ServerPlayer player,
                                            EntityCultivator lingTianhou) {
        // Gate 1: server-side only (defensive — HistoryEvents already checks)
        if (player.level().isClientSide) return;

        // Gate 2: characterId check (defensive — HistoryEvents already checks)
        if (!CHARACTER_ID.equals(lingTianhou.getCharacterId())) return;

        ServerLevel level = player.serverLevel();

        // Gate 3: write-once guard — check Ling Tianhou's runtime state
        WorldRuntimeState runtime = WorldRuntimeState.get(level);
        CompoundTag lingTianhouState = runtime.getNpcState(CHARACTER_ID);
        if (lingTianhouState == null) {
            lingTianhouState = new CompoundTag();
        }
        if (lingTianhouState.getBoolean("sword_qi_granted")) {
            // Already granted — write-once. Display a canon-faithful message.
            player.sendSystemMessage(Component.literal(
                    "凌天侯缓缓摇头：「剑气已赠，不可再予。」")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            player.sendSystemMessage(Component.literal(
                    "Ling Tianhou shakes his head slowly: \"The sword qi has "
                            + "already been given. It cannot be given again.\"")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            return;
        }

        // ── All gates pass — execute the grant ──

        // Step 1: Set the runtime state flags (write-once)
        lingTianhouState.putBoolean("sword_qi_granted", true);
        lingTianhouState.putLong("sword_qi_grant_tick", level.getGameTime());
        runtime.updateNpcState(CHARACTER_ID, lingTianhouState);

        // Step 2: Grant the player TWO SwordQiStrandItem stacks
        // CRON-122: canon-faithful strand-type differentiation —
        // strand 1 = FLESH (化作王平的血肉之躯), strand 2 = SOUL_GUARD (守护其魂魄).
        // The grant event already narrates this distinction in the chat
        // message ("一道凝肉，一道凝魂"); CRON-122 makes the items themselves
        // canon-faithfully distinct so the redemption prerequisite can
        // enforce exactly 1 of each type.
        int granted = 0;
        for (int i = 1; i <= SWORD_QI_STRAND_COUNT; i++) {
            ItemStack strand = new ItemStack(ErgenverseItems.SWORD_QI_STRAND.get(), 1);
            // Set the strand index NBT for tooltip display
            if (strand.getItem() instanceof SwordQiStrandItem swordQiItem) {
                swordQiItem.setStrandIndex(strand, i);
                // CRON-122: set the strand type to match the canon-faithful
                // strand-index → strand-type pairing.
                //   strand 1 → FLESH (化作王平的血肉之躯)
                //   strand 2 → SOUL_GUARD (守护其魂魄)
                SwordQiStrandItem.StrandType type = (i == 1)
                        ? SwordQiStrandItem.StrandType.FLESH
                        : SwordQiStrandItem.StrandType.SOUL_GUARD;
                swordQiItem.setStrandType(strand, type);
            }
            // Try to add to the player's inventory; if full, drop at feet
            if (!player.getInventory().add(strand)) {
                player.drop(strand, false);  // drop at player's feet
                Ergenverse.LOGGER.warn("[Ergenverse] CRON-118: Player inventory full — "
                        + "dropped sword qi strand {} at {}'s feet.", i, player.getName().getString());
            }
            granted++;
        }

        // Step 3: Spawn canon-faithful sound effects
        // AMETHYST_BLOCK_CHIME — the crystalline tone of sword qi
        level.playSound(null, lingTianhou.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT,
                1.0F, 1.2F);  // volume 1.0, pitch 1.2 (bright crystalline)
        // PLAYER_LEVELUP at the player — the bright ascending tone of receiving a gift
        level.playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT,
                0.7F, 1.3F);  // volume 0.7, pitch 1.3

        // Step 4: Display the canon-faithful bilingual message
        player.sendSystemMessage(Component.literal(
                "─────────────────────────────────────")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal(
                "凌天侯抬手，两道剑气从他指尖飞出，落入你的掌中。")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "「这两道剑气，乃我自身所凝。一道凝肉，一道凝魂。」")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "「带往朱雀墓，于修炼星晶前释放，可重塑你子王平的肉身。」")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "「他将活——但那是虚假生命。不能哭，不能修炼，不能生育。」")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Ling Tianhou raises his hand; two strands of sword qi fly "
                        + "from his fingertips into your palm.")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "\"These two strands of sword qi are condensed from my own "
                        + "essence. One condenses flesh, one condenses soul.\"")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "\"Take them to the Suzaku Tomb, release them before the "
                        + "Cultivation Planet Crystal, and your son Wang Ping's "
                        + "body shall be rebuilt.\"")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "\"He will live — but it will be a False Life. He cannot cry, "
                        + "cannot cultivate, cannot sire children.\"")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "你已成为大罗剑宗长老。两道剑气已在你的物品栏中。")
                .withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal(
                "You are now an Elder of Da Luo Sword Sect. The two sword qi "
                        + "strands are in your inventory.")
                .withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal(
                "─────────────────────────────────────")
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        // Step 5: Record in HistoryManager
        HistoryManager.onDiscovery(player, SUBJECT_SWORD_QI_GRANTED,
                "Ling Tianhou (凌天侯), the Sword Venerable (剑尊) of Da Luo Sword "
                        + "Sect (大罗剑宗), personally granted Wang Lin two strands "
                        + "of his own sword qi (两道剑气) to rebuild Wang Ping's body. "
                        + "He also granted Wang Lin Elder (长老) status in the sect. "
                        + "The sword qi is to be taken to the Suzaku Tomb and "
                        + "released before the Cultivation Planet Crystal to trigger "
                        + "the Wang Ping redemption event.",
                level.getGameTime());

        Ergenverse.LOGGER.info("[Ergenverse] CRON-118: Ling Tianhou granted {} sword qi strands "
                        + "to player {} at {}. Strand indices: 1, 2.",
                granted, player.getName().getString(), lingTianhou.blockPosition());
    }
}
