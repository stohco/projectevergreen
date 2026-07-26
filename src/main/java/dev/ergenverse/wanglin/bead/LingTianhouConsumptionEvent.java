package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.history.HistoryManager;
import dev.ergenverse.runtime.CanonUUID;
import dev.ergenverse.runtime.NPCRuntime;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.simulation.WorldRuntimeState;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Ling Tianhou Consumption Event — CRON-COMPLETIONIST-123.
 *
 * <p>Implements the canon-faithful "天运子 consumes 凌天侯" narrative beat.
 * 天运子 (Tian Yun Zi), the master of 天运宗 (Tian Yun Sect) on 天运星,
 * needs to constantly consume other cultivators (生生吞噬) to reincarnate
 * repeatedly, growing stronger with each awakening. His 98th awakening
 * target is 凌天侯 (Ling Tianhou), the Sword Venerable (剑尊) of 大罗剑宗.
 *
 * <h2>Canon Basis (fact-checked via web-search 2026-07-27)</h2>
 * <p>Per Baidu Baike dedicated entry (PRIMARY, https://baike.baidu.com/item/天运子/23166960):
 * "天运子作为《仙逆》中的核心反派之一，其身份被描述为天道碎片化成的定界罗盘器灵"
 * — "天运子, as one of the core antagonists of 仙逆, is described as the artifact
 * spirit of the Realm-Defining Compass, formed from fragments of the Heavenly Dao."
 *
 * <p>Per Zhihu (https://zhuanlan.zhihu.com/p/1957927329482383516): "天运子选定
 * 凌天侯作为第九十八觉醒的吞噬对象，在二人爆发最后一次冲突后，成功将凌天侯吞噬"
 * — "天运子 selected 凌天侯 as his 98th awakening's consumption target; after
 * their final conflict, he successfully consumed 凌天侯."
 *
 * <p>Per Sohu (https://www.sohu.com/a/935257158_122415633): "天运子精心谋划后出手，
 * 发动诡异仙法将凌天侯强行笼罩...最终被天运子生生吞噬，大罗剑宗也因失去主心骨，
 * 在后续岁月里逐渐衰败凋零" — "天运子 carefully planned and struck, deploying
 * bizarre immortal arts to envelop 凌天侯... ultimately 凌天侯 was consumed alive
 * by 天运子, and 大罗剑宗, having lost its pillar, gradually declined and withered
 * in the years that followed."
 *
 * <p>Per 163.com (https://www.163.com/dy/article/K98V89NE0556C06B.html): "最后凌天侯
 * 被天运子吞噬、天运子被王林凝为本源" — "In the end, 凌天侯 was consumed by 天运子,
 * and 天运子 was condensed into origin by 王林."
 *
 * <p>Canon sources:
 * <ul>
 *   <li>Baidu Baike 天运子 (https://baike.baidu.com/item/天运子/23166960)</li>
 *   <li>Baidu Baike 凌天侯 (https://baike.baidu.com/item/凌天侯/65285935)</li>
 *   <li>Sohu (https://www.sohu.com/a/961903590_568249,
 *       https://www.sohu.com/a/935257158_122415633)</li>
 *   <li>Zhihu (https://zhuanlan.zhihu.com/p/1957927329482383516)</li>
 *   <li>163.com (https://www.163.com/dy/article/K98V89NE0556C06B.html)</li>
 * </ul>
 * NO fabricated chapter citation — no source explicitly names the chapter of the
 * consumption event.
 *
 * <h2>Trigger Mechanism — Player Right-Click on 天运子 NPC</h2>
 * <p>The consumption fires when the player right-clicks the 天运子
 * EntityCultivator NPC at the Tian Yun Sect (5500, 5500). The right-click
 * is dispatched by {@link dev.ergenverse.history.HistoryEvents#onEntityInteract}
 * when the target cultivator's characterId is {@code "tian_yun_zi"}.
 *
 * <p>The consumption is gated by the following (checked in
 * {@link #handleConsumption}):
 *
 * <ol>
 *   <li><b>Server-side check.</b> Never run on client.</li>
 *   <li><b>CharacterId check.</b> The target cultivator must have
 *       characterId = "tian_yun_zi".</li>
 *   <li><b>Sword-qi-grant prerequisite.</b> Ling Tianhou's runtime state
 *       must have {@code "sword_qi_granted": true} (set by
 *       {@link LingTianhouSwordQiGrantEvent}). Canon-chronological: Ling
 *       Tianhou must have ALREADY given Wang Lin the two sword qi strands
 *       (for Wang Ping's redemption) BEFORE 天运子 consumes him. This
 *       enforces the canonical narrative order: (1) Ling Tianhou grants
 *       sword qi, (2) Wang Ping is redeemed, (3) Ling Tianhou is consumed
 *       by 天运子. (Strictly, the consumption may happen before or after
 *       the redemption in canon — sources are ambiguous. The mod requires
 *       the grant to have happened, but does NOT require the redemption
 *       to have happened, leaving the player free to choose the order.)</li>
 *   <li><b>Write-once guard.</b> 天运子's runtime state must NOT have
 *       {@code "ling_tianhou_consumed": true}. The consumption is a one-time
 *       event per save. This prevents the player from farming the event.</li>
 * </ol>
 *
 * <h2>On Success</h2>
 * <ol>
 *   <li>Sets 天运子's runtime state:
 *     <ul>
 *       <li>{@code ling_tianhou_consumed: true}</li>
 *       <li>{@code ling_tianhou_consumed_tick: currentTick}</li>
 *     </ul>
 *   </li>
 *   <li>Marks Ling Tianhou as dead in {@link NPCRuntime} (sets his
 *       {@code deadUntilRevived} flag to true). Canon: after consumption,
 *       Ling Tianhou's avatar (分身) perishes; 大罗剑宗 declines. He is
 *       NOT revived — his 本体 (true body) is with 灭生老人 in 逆尘界.
 *       A future CRON could implement the "本体 reappears" arc if needed.</li>
 *   <li>Dematerializes Ling Tianhou's EntityCultivator (if currently loaded)
 *       via {@link dev.ergenverse.runtime.materialize.CanonActorMaterializer#dematerializeActor}.
 *       Canon: the consumed cultivator visibly disappears.</li>
 *   <li>Spawns canon-faithful sound effects (WITHER_DEATH for the consumption
 *       sound, ENDER_DRAGON_GROWL for 天运子's predatory dominance).</li>
 *   <li>Displays a bilingual message (Chinese + English) narrating the
 *       consumption event.</li>
 *   <li>Records the event in HistoryManager.</li>
 * </ol>
 *
 * <h2>State Transition Diagram</h2>
 * <pre>
 *   CRON-118: Player right-clicks Ling Tianhou at Da Luo Sword Sect
 *             →  Ling Tianhou runtime: sword_qi_granted = true
 *             →  Player inventory: +1 FLESH strand + 1 SOUL_GUARD strand
 *
 *   CRON-123: Player right-clicks 天运子 at Tian Yun Sect
 *             (prerequisite: sword_qi_granted = true)
 *             →  天运子 runtime: ling_tianhou_consumed = true
 *             →  Ling Tianhou runtime: deadUntilRevived = true
 *             →  Ling Tianhou EntityCultivator: dematerialized (if loaded)
 *             →  大罗剑宗 declines (future questline hook)
 *
 *   Future:   Player (Wang Lin) eventually condenses 天运子 into origin (本源)
 *             in the Primordial Divine Realm — a future questline.
 * </pre>
 *
 * <h2>Single-Player Maximalism (Article XLIII)</h2>
 * <p>The consumption only fires when the player travels to the Tian Yun Sect
 * (5500, 5500) and right-clicks 天运子. This is a high-friction canon-faithful
 * gate — the player must:
 * <ol>
 *   <li>Travel to the Da Luo Sword Sect (5000, 5000) and right-click Ling
 *       Tianhou to obtain the sword qi strands (CRON-118).</li>
 *   <li>Travel ~700 blocks east to the Tian Yun Sect (5500, 5500).</li>
 *   <li>Find 天运子 (materialized as an EntityCultivator).</li>
 *   <li>Right-click him to witness the consumption event.</li>
 * </ol>
 *
 * <h2>Architecture</h2>
 * <p>This event is a static utility class (mirrors the CRON-118
 * {@link LingTianhouSwordQiGrantEvent} pattern). The sole entry point is
 * {@link #handleConsumption(ServerPlayer, EntityCultivator)}, called by
 * {@link dev.ergenverse.history.HistoryEvents#onEntityInteract} when the
 * target cultivator's characterId is "tian_yun_zi".
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see dev.ergenverse.history.HistoryEvents#onEntityInteract (trigger)
 * @see LingTianhouSwordQiGrantEvent (prerequisite — sword qi grant)
 * @see dev.ergenverse.runtime.CanonUUID#TIAN_YUN_ZI (天运子 canon UUID)
 * @see dev.ergenverse.runtime.CanonUUID#LING_TIANHOU (凌天侯 canon UUID)
 */
public final class LingTianhouConsumptionEvent {

    /**
     * The canon character ID for 天运子. Must match
     * {@link CanonUUID#TIAN_YUN_ZI}'s profile and
     * {@link dev.ergenverse.runtime.PlanetSuzakuBlueprint}'s TIAN_YUN_SECT.
     */
    public static final String CHARACTER_ID = "tian_yun_zi";

    /**
     * HistoryManager subject for the consumption event. Distinct from
     * other subjects so subscribers can react specifically to this beat.
     */
    public static final String SUBJECT_LING_TIANHOU_CONSUMED = "tian_yun_zi_consumed_ling_tianhou";

    /**
     * The awakening number — canon: 天运子's 98th awakening target is
     * 凌天侯. Per Zhihu: "天运子选定凌天侯作为第九十八觉醒的吞噬对象".
     */
    public static final int AWAKENING_NUMBER = 98;

    private LingTianhouConsumptionEvent() {}

    /**
     * Handle the player's right-click on 天运子. Called by
     * {@link dev.ergenverse.history.HistoryEvents#onEntityInteract} when
     * the target cultivator's characterId is "tian_yun_zi".
     *
     * <p>The method is fully defensive — it no-ops with appropriate
     * canon-faithful messages for each gate failure. The consumption is
     * write-once (per save): once {@code ling_tianhou_consumed} is true
     * in 天运子's runtime state, subsequent right-clicks display a
     * "already consumed" message and no-op.
     *
     * @param player    the server player (Wang Lin) who right-clicked
     * @param tianYunZi the 天运子 EntityCultivator NPC
     */
    public static void handleConsumption(ServerPlayer player,
                                          EntityCultivator tianYunZi) {
        // Gate 1: server-side only (defensive — HistoryEvents already checks)
        if (player.level().isClientSide) return;

        // Gate 2: characterId check (defensive — HistoryEvents already checks)
        if (!CHARACTER_ID.equals(tianYunZi.getCharacterId())) return;

        ServerLevel level = player.serverLevel();

        // Gate 3: sword-qi-grant prerequisite — Ling Tianhou must have
        // already granted the sword qi. Canon-chronological: the grant
        // happens BEFORE the consumption (Ling Tianhou must be alive to
        // grant the sword qi; after consumption he is gone).
        WorldRuntimeState runtime = WorldRuntimeState.get(level);
        CompoundTag lingTianhouState = runtime.getNpcState(LingTianhouSwordQiGrantEvent.CHARACTER_ID);
        boolean swordQiGranted = lingTianhouState != null
                && lingTianhouState.getBoolean("sword_qi_granted");
        if (!swordQiGranted) {
            // Prerequisite not met — Ling Tianhou has not yet granted the sword qi.
            player.sendSystemMessage(Component.literal(
                    "天运子瞥了你一眼，冷笑：「凌天侯尚未赠你剑气，你来此何为？」")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            player.sendSystemMessage(Component.literal(
                    "Tian Yun Zi glances at you and sneers: \"Ling Tianhou has not yet "
                            + "granted you the sword qi. Why have you come here?\"")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            player.sendSystemMessage(Component.literal(
                    "[先前往大罗剑宗 (5000, 5000) 右击凌天侯获得剑气。]")
                    .withStyle(ChatFormatting.DARK_GRAY));
            player.sendSystemMessage(Component.literal(
                    "[Travel to the Da Luo Sword Sect (5000, 5000) and right-click "
                            + "Ling Tianhou to obtain the sword qi first.]")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }

        // Gate 4: write-once guard — check 天运子's runtime state
        CompoundTag tianYunZiState = runtime.getNpcState(CHARACTER_ID);
        if (tianYunZiState == null) {
            tianYunZiState = new CompoundTag();
        }
        if (tianYunZiState.getBoolean("ling_tianhou_consumed")) {
            // Already consumed — write-once. Display a canon-faithful message.
            player.sendSystemMessage(Component.literal(
                    "天运子闭目养神，周身气息愈发深邃。凌天侯的剑意已融入他的轮回。")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            player.sendSystemMessage(Component.literal(
                    "Tian Yun Zi sits with eyes closed in meditation, his aura growing "
                            + "ever deeper. Ling Tianhou's sword intent has merged into "
                            + "his cycle of reincarnation.")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            return;
        }

        // ── All gates pass — execute the consumption ──

        // Step 1: Set 天运子's runtime state flags (write-once)
        tianYunZiState.putBoolean("ling_tianhou_consumed", true);
        tianYunZiState.putLong("ling_tianhou_consumed_tick", level.getGameTime());
        tianYunZiState.putInt("awakening_number", AWAKENING_NUMBER);
        runtime.updateNpcState(CHARACTER_ID, tianYunZiState);

        // Step 2: Mark Ling Tianhou as dead in NPCRuntime (deadUntilRevived=true).
        // Canon: after consumption, Ling Tianhou's avatar perishes; he is NOT
        // revived (his 本体 is with 灭生老人 in 逆尘界).
        WorldRuntime rt = WorldRuntime.get();
        if (rt.isInitialized()) {
            NPCRuntime.ActorState lingTianhouActor = rt.npcs().getActor(CanonUUID.LING_TIANHOU);
            if (lingTianhouActor != null) {
                lingTianhouActor.deadUntilRevived = true;
                Ergenverse.LOGGER.info("[Ergenverse] CRON-123: Ling Tianhou marked dead "
                        + "(deadUntilRevived=true) — consumed by 天运子 at the 98th awakening.");

                // Step 3: Dematerialize Ling Tianhou's EntityCultivator (if loaded).
                // Canon: the consumed cultivator visibly disappears.
                try {
                    boolean dematerialized = rt.actorMaterializer()
                            .dematerializeActor(CanonUUID.LING_TIANHOU, rt);
                    if (dematerialized) {
                        Ergenverse.LOGGER.info("[Ergenverse] CRON-123: Ling Tianhou "
                                + "EntityCultivator dematerialized (consumed by 天运子).");
                    } else {
                        Ergenverse.LOGGER.info("[Ergenverse] CRON-123: Ling Tianhou was not "
                                + "currently loaded (no dematerialization needed).");
                    }
                } catch (Throwable t) {
                    Ergenverse.LOGGER.warn("[Ergenverse] CRON-123: dematerializeActor failed "
                            + "for Ling Tianhou: {}", t.getMessage());
                }
            } else {
                Ergenverse.LOGGER.warn("[Ergenverse] CRON-123: Ling Tianhou actor state not "
                        + "found in NPCRuntime — cannot mark dead.");
            }
        }

        // Step 4: Spawn canon-faithful sound effects.
        // WITHER_DEATH — the consumption sound (a major cultivator perishing).
        level.playSound(null, tianYunZi.blockPosition(),
                SoundEvents.WITHER_DEATH, SoundSource.HOSTILE,
                1.0F, 0.5F);  // volume 1.0, pitch 0.5 (deep, ominous)
        // ENDER_DRAGON_GROWL — 天运子's predatory dominance after consumption.
        level.playSound(null, tianYunZi.blockPosition(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE,
                0.8F, 0.7F);  // volume 0.8, pitch 0.7 (predatory growl)

        // Step 5: Display the canon-faithful bilingual message.
        player.sendSystemMessage(Component.literal(
                "═══════════════════════════════════════")
                .withStyle(ChatFormatting.DARK_RED));
        player.sendSystemMessage(Component.literal(
                "天运子缓缓抬手，一道诡异仙法从他掌心蔓延而出。")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "凌天侯的剑意在他周身激荡，却被那无形的力量一层层剥离。")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "「第九十八次觉醒...凌天侯，你的剑意，我收下了。」")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(
                "凌天侯的身影在天运子的吞噬之下渐渐消散。")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "大罗剑宗失去了它的剑尊，从此走向衰败。")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Tian Yun Zi slowly raises his hand; a bizarre immortal art "
                        + "spreads from his palm.")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Ling Tianhou's sword intent surges around him, but the "
                        + "formless power peels it away layer by layer.")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "\"The 98th awakening... Ling Tianhou, your sword intent is mine.\"")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(
                "Ling Tianhou's figure slowly dissipates under 天运子's consumption.")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Da Luo Sword Sect has lost its Sword Venerable; from this day, "
                        + "it shall decline.")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "═══════════════════════════════════════")
                .withStyle(ChatFormatting.DARK_RED));

        // Step 6: Record in HistoryManager.
        HistoryManager.onDiscovery(player, SUBJECT_LING_TIANHOU_CONSUMED,
                "天运子 (Tian Yun Zi), master of 天运宗 (Tian Yun Sect) on 天运星, "
                        + "consumed 凌天侯 (Ling Tianhou), the Sword Venerable (剑尊) "
                        + "of 大罗剑宗, at his 98th awakening. Ling Tianhou's avatar "
                        + "perished; Da Luo Sword Sect lost its pillar and began to "
                        + "decline. (Canon: 天运子 is a clone of the All-Seer — the "
                        + "artifact spirit of the Realm-Defining Compass. He needs to "
                        + "consume cultivators to reincarnate repeatedly. Wang Lin "
                        + "eventually condenses him into origin in the Primordial "
                        + "Divine Realm — a future event.)",
                level.getGameTime());

        Ergenverse.LOGGER.info("[Ergenverse] CRON-123: 天运子 consumed Ling Tianhou "
                        + "(98th awakening) at player {}'s witness, position {}.",
                player.getName().getString(), tianYunZi.blockPosition());
    }
}
