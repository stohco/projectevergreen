package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.history.HistoryManager;
import dev.ergenverse.runtime.CanonUUID;
import dev.ergenverse.runtime.NPCRuntime;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.simulation.WorldRuntimeState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Wang Ping Mortal-Life Arc Event — CRON-COMPLETIONIST-124.
 *
 * <p>Implements the canon-faithful 5-stage "Wang Ping 二次化凡 (second mortal
 * transformation) arc" — the complete narrative of Wang Ping's mortal life
 * on 冉云星 (Ranyun Star), from woodcarving apprenticeship through marriage,
 * war, imperial reign, and final voluntary dispersal at age 72.
 *
 * <h2>Canon Basis (fact-checked via web-search 2026-07-27)</h2>
 * <p>Per Baidu Baike 仙逆编年史 (PRIMARY chronology,
 * <a href="https://baike.baidu.com/item/仙逆编年史/9845998">
 * https://baike.baidu.com/item/仙逆编年史/9845998</a>) + Zhihu timeline
 * (<a href="https://zhuanlan.zhihu.com/p/713215901">
 * https://zhuanlan.zhihu.com/p/713215901</a>) + Baidu Baike 王平
 * (<a href="https://baike.baidu.com/item/王平/62563845">
 * https://baike.baidu.com/item/王平/62563845</a>) + Baidu Baike 青宜
 * (<a href="https://baike.baidu.com/item/青宜/637430">
 * https://baike.baidu.com/item/青宜/637430</a>) + Sohu 青宜 deal
 * (<a href="https://www.sohu.com/a/1021093654_121458245">
 * https://www.sohu.com/a/1021093654_121458245</a>) + QQ News Tianxing Empire
 * (<a href="https://view.inews.qq.com/a/20260507A07ELJ00">
 * https://view.inews.qq.com/a/20260507A07ELJ00</a>):
 *
 * <p>After Wang Lin reclaims Wang Ping's remnant soul (残魂) from Liu Mei's
 * 怨婴 refinement (~100 years) and rebuilds Wang Ping's body from two
 * strands of Ling Tianhou's sword qi (CRON-117/118/120 redemption event),
 * Wang Lin lives as a mortal woodcarver (木雕师) on 冉云星 under the alias
 * 许木 (Xu Mu) for 19 years, raising Wang Ping. The full 二次化凡 arc
 * spans 72 years and unfolds in 5 canonical stages:
 *
 * <table border="1">
 *   <caption>5-Stage Mortal-Life Arc Timeline (Zhihu-attested)</caption>
 *   <tr><th>Stage</th><th>Duration</th><th>Canon Beat</th><th>Chapter</th></tr>
 *   <tr><td>1. Woodcarving Apprenticeship</td><td>19 years</td>
 *       <td>Wang Lin (许木) lives as a woodcarver at 落月村, raises Wang Ping</td>
 *       <td>Ch 680-693</td></tr>
 *   <tr><td>2. Marriage to 青宜</td><td>8 years</td>
 *       <td>Wang Ping marries 青宜 (Sun family outer-surname cultivator)</td>
 *       <td>Ch 693 《青宜》</td></tr>
 *   <tr><td>3. 25 Years of War</td><td>25 years</td>
 *       <td>Wang Ping becomes a warlord; conquers territories on 冉云星</td>
 *       <td>between Ch 693 and Ch 700</td></tr>
 *   <tr><td>4. 10-Year Reign as Emperor of 天行帝国</td><td>10 years</td>
 *       <td>Wang Ping rules as emperor; 青宜 becomes 皇后 (empress)</td>
 *       <td>Ch 700 (peak)</td></tr>
 *   <tr><td>5. Voluntary Dispersal at Age 72</td><td>(final)</td>
 *       <td>雷仙殿 envoy exposes Wang Ping's sword-qi body truth;
 *           Wang Ping voluntarily disperses his sword-qi body;
 *           残魂 sealed into 天逆珠 by Wang Lin;
 *           青宜 follows in death (殉情而亡); her soul also collected</td>
 *       <td>Ch 700 《惊变》</td></tr>
 *   <tr><td>Total</td><td>72 years</td><td colspan="3">19+8+25+10+10=72 years per Zhihu</td></tr>
 * </table>
 *
 * <p><b>青宜's 60-year deal:</b> per Sohu
 * (<a href="https://www.sohu.com/a/1021093654_121458245">
 * https://www.sohu.com/a/1021093654_121458245</a>), 青宜 made a 60-year
 * deal with Wang Lin: 60 years of companionship with Wang Ping in exchange
 * for Wang Lin (at 问鼎 / Ascendant power) elevating her from 炼气后期
 * (Qi Condensation Late Stage) to 元婴后期大圆满 (Late Nascent Soul grand
 * completion). The 60 years span from her arrival (Ch 693) to Wang Ping's
 * dispersal (Ch 700).
 *
 * <h2>Trigger Mechanism — Player Right-Click on Wang Ping or 青宜</h2>
 * <p>The arc is advanced by the player right-clicking the appropriate
 * EntityCultivator NPC at 落月村 on Ranyun Star. Each stage requires a
 * separate right-click to advance (mirrors the CRON-110→112 Zhou Ru
 * soul-transfer→Kunxu-departure event chain pattern).
 *
 * <p>Stages 1, 3, 4, 5 are advanced by right-clicking <b>Wang Ping</b>
 * (characterId = "wang_ping"). Stage 2 (marriage) is advanced by
 * right-clicking <b>青宜</b> (characterId = "qing_yi") — she is the
 * marriage partner and her consent drives the beat.
 *
 * <h3>Stage 1: Woodcarving Apprenticeship (~19 years)</h3>
 * <p>Triggered by right-click on Wang Ping. Prerequisites:
 * <ol>
 *   <li>Wang Ping's {@code deadUntilRevived} flag is false (CRON-117/120
 *       redemption has fired).</li>
 *   <li>Wang Ping's runtime state {@code mortal_arc_stage} is 0 or absent
 *       (not yet started).</li>
 * </ol>
 * On success: sets Wang Ping's runtime state {@code mortal_arc_stage = 1}
 * and {@code mortal_arc_started_tick = currentTick}. Displays a narrative
 * describing Wang Lin's 19 years as woodcarver 许木, raising Wang Ping at
 * 落月村. Records in HistoryManager.
 *
 * <h3>Stage 2: Marriage to 青宜 (~8 years)</h3>
 * <p>Triggered by right-click on 青宜. Prerequisites:
 * <ol>
 *   <li>Wang Ping's runtime state {@code mortal_arc_stage >= 1}
 *       (apprenticeship has started).</li>
 *   <li>Wang Ping's runtime state {@code mortal_arc_stage < 2}
 *       (marriage has not yet happened).</li>
 * </ol>
 * On success: sets Wang Ping's runtime state {@code mortal_arc_stage = 2}
 * and 青宜's runtime state {@code married_to_wang_ping = true}. Displays
 * a narrative describing the marriage at 落月村 (Ch 693). Records in
 * HistoryManager.
 *
 * <h3>Stage 3: 25 Years of War</h3>
 * <p>Triggered by right-click on Wang Ping. Prerequisites:
 * <ol>
 *   <li>Wang Ping's runtime state {@code mortal_arc_stage = 2}
 *       (marriage complete).</li>
 *   <li>Wang Ping's runtime state {@code mortal_arc_stage < 3}
 *       (war has not yet happened).</li>
 * </ol>
 * On success: sets Wang Ping's runtime state {@code mortal_arc_stage = 3}.
 * Displays a narrative describing Wang Ping's 25 years of war, becoming
 * a warlord on 冉云星. Records in HistoryManager.
 *
 * <h3>Stage 4: 10-Year Reign as Emperor of 天行帝国</h3>
 * <p>Triggered by right-click on Wang Ping. Prerequisites:
 * <ol>
 *   <li>Wang Ping's runtime state {@code mortal_arc_stage = 3}
 *       (war complete).</li>
 *   <li>Wang Ping's runtime state {@code mortal_arc_stage < 4}
 *       (emperor reign has not yet happened).</li>
 * </ol>
 * On success: sets Wang Ping's runtime state {@code mortal_arc_stage = 4}
 * and 青宜's runtime state {@code is_empress = true}. Displays a narrative
 * describing Wang Ping's 10-year reign as emperor of 天行帝国 (Tianxing
 * Empire), with 青宜 as 皇后 (empress). Records in HistoryManager.
 *
 * <h3>Stage 5: Voluntary Dispersal at Age 72 (Ch 700 《惊变》)</h3>
 * <p>Triggered by right-click on Wang Ping. Prerequisites:
 * <ol>
 *   <li>Wang Ping's runtime state {@code mortal_arc_stage = 4}
 *       (emperor reign complete).</li>
 *   <li>Wang Ping's runtime state {@code mortal_arc_stage < 5}
 *       (dispersal has not yet happened).</li>
 *   <li>The player holds the Heaven-Defying Bead in the main hand
 *       (so Wang Ping's 残魂 can be sealed).</li>
 *   <li>The bead is NOT in DORMANT_STONE stage (must be cracked open first).</li>
 * </ol>
 * On success:
 * <ul>
 *   <li>Sets Wang Ping's runtime state {@code mortal_arc_stage = 5}.</li>
 *   <li>Marks Wang Ping as dead in {@link NPCRuntime} (sets his
 *       {@code deadUntilRevived} flag to true). Canon: the sword-qi body
 *       is dispersed; Wang Ping perishes as a mortal.</li>
 *   <li>Dematerializes Wang Ping's EntityCultivator.</li>
 *   <li>Marks 青宜 as dead in {@link NPCRuntime} (sets her
 *       {@code deadUntilRevived} flag to true). Canon: 青宜 殉情而亡
 *       (followed Wang Ping in death for love).</li>
 *   <li>Dematerializes 青宜's EntityCultivator (if loaded).</li>
 *   <li>Sets the bead's {@code NBT_WANG_PING_SOUL} flag (seals the 残魂).</li>
 *   <li>Sets the bead's {@code NBT_QING_YI_SOUL} flag (collects 青宜's soul).</li>
 *   <li>Spawns canon-faithful particle + sound effects (sword-qi dispersal
 *       + soul sealing).</li>
 *   <li>Displays a 12-line bilingual narrative citing Ch 700 《惊变》.</li>
 *   <li>Records in HistoryManager.</li>
 * </ul>
 *
 * <h2>State Transition Diagram</h2>
 * <pre>
 *   CRON-117/120: Player right-clicks INHERITED Crystal at Suzaku Tomb
 *                 + 1 FLESH + 1 SOUL_GUARD strand + realm ≥ ASCENDANT
 *                 → Wang Ping redeemed on Ranyun Star (落月村)
 *                 → Wang Ping runtime: deadUntilRevived = false
 *
 *   CRON-121: 青宜 materialized at 落月村 (always alive at story start)
 *
 *   CRON-124 stage 1: Player right-clicks Wang Ping
 *                 → Wang Ping runtime: mortal_arc_stage = 1
 *
 *   CRON-124 stage 2: Player right-clicks 青宜
 *                 (prerequisite: Wang Ping moral_arc_stage ≥ 1)
 *                 → Wang Ping runtime: mortal_arc_stage = 2
 *                 → 青宜 runtime: married_to_wang_ping = true
 *
 *   CRON-124 stage 3: Player right-clicks Wang Ping
 *                 (prerequisite: Wang Ping moral_arc_stage = 2)
 *                 → Wang Ping runtime: mortal_arc_stage = 3
 *
 *   CRON-124 stage 4: Player right-clicks Wang Ping
 *                 (prerequisite: Wang Ping moral_arc_stage = 3)
 *                 → Wang Ping runtime: mortal_arc_stage = 4
 *                 → 青宜 runtime: is_empress = true
 *
 *   CRON-124 stage 5: Player right-clicks Wang Ping with bead in main hand
 *                 (prerequisite: Wang Ping moral_arc_stage = 4)
 *                 → Wang Ping runtime: mortal_arc_stage = 5
 *                 → Wang Ping runtime: deadUntilRevived = true
 *                 → 青宜 runtime: deadUntilRevived = true (殉情而亡)
 *                 → Wang Ping + 青宜 EntityCultivators dematerialized
 *                 → Bead NBT: wang_ping_soul = true (残魂 sealed)
 *                 → Bead NBT: qing_yi_soul = true (soul collected)
 *
 *   Future:        Player (Wang Lin) at 踏天境 (Transcendence) revives
 *                  both Wang Ping and 青宜 as ordinary mortals (Sohu-
 *                  attested final beat) — a future questline hook.
 * </pre>
 *
 * <h2>Single-Player Maximalism (Article XLIII)</h2>
 * <p>The arc only fires when the player travels to 落月村 on Ranyun Star
 * and right-clicks the appropriate NPC. This is a high-friction canon-
 * faithful gate — the player must:
 * <ol>
 *   <li>Reach Ascendant realm (问鼎).</li>
 *   <li>Complete the Suzaku Son inheritance (CRON-106).</li>
 *   <li>Obtain the 2 sword qi strands from Ling Tianhou (CRON-118).</li>
 *   <li>Trigger the Wang Ping redemption (CRON-117/120) — teleports to
 *       Ranyun Star.</li>
 *   <li>Right-click Wang Ping 4 times (stages 1, 3, 4, 5) and 青宜 once
 *       (stage 2), with the bead in main hand for stage 5.</li>
 * </ol>
 *
 * <h2>Architecture</h2>
 * <p>This event is a static utility class (mirrors the CRON-123
 * {@link LingTianhouConsumptionEvent} pattern). Two entry points:
 * <ul>
 *   <li>{@link #handleWangPingInteract(ServerPlayer, EntityCultivator)} —
 *       dispatched by {@link dev.ergenverse.history.HistoryEvents#onEntityInteract}
 *       when the player right-clicks Wang Ping. Handles stages 1, 3, 4, 5.</li>
 *   <li>{@link #handleQingYiInteract(ServerPlayer, EntityCultivator)} —
 *       dispatched when the player right-clicks 青宜. Handles stage 2
 *       (marriage).</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see WangPingRedemptionEvent (predecessor — CRON-117/120, redemption)
 * @see LingTianhouConsumptionEvent (parallel — CRON-123, consumption pattern)
 * @see ZhouRuKunxuDepartureEvent (parallel — CRON-112, multi-stage pattern)
 * @see HeavenDefyingBeadItem#NBT_WANG_PING_SOUL (stage 5 output)
 * @see HeavenDefyingBeadItem#NBT_QING_YI_SOUL (stage 5 output)
 * @see dev.ergenverse.runtime.CanonUUID#WANG_PING
 * @see dev.ergenverse.runtime.CanonUUID#QING_YI
 * @see dev.ergenverse.runtime.PlanetSuzakuBlueprint#RANYUN_STAR
 */
public final class WangPingMortalArcEvent {

    /**
     * The canon character ID for Wang Ping. Must match
     * {@link CanonUUID#WANG_PING}'s profile and the npc_wang_ping.json
     * data file.
     */
    public static final String WANG_PING_CHARACTER_ID = "wang_ping";

    /**
     * The canon character ID for 青宜. Must match
     * {@link CanonUUID#QING_YI}'s profile and the npc_qing_yi.json
     * data file.
     */
    public static final String QING_YI_CHARACTER_ID = "qing_yi";

    // ── HistoryManager subjects (one per stage) ──

    /** Stage 1: Wang Ping begins his mortal-life arc (woodcarving apprenticeship). */
    public static final String SUBJECT_STAGE1_APPRENTICESHIP =
            "wang_ping_arc_stage1_apprenticeship";

    /** Stage 2: Wang Ping marries 青宜 (Ch 693 《青宜》). */
    public static final String SUBJECT_STAGE2_MARRIAGE =
            "wang_ping_arc_stage2_marriage";

    /** Stage 3: Wang Ping becomes a warlord (25 years of war). */
    public static final String SUBJECT_STAGE3_WAR =
            "wang_ping_arc_stage3_war";

    /** Stage 4: Wang Ping reigns as emperor of 天行帝国 (10-year reign). */
    public static final String SUBJECT_STAGE4_EMPEROR =
            "wang_ping_arc_stage4_emperor";

    /** Stage 5: Wang Ping voluntarily disperses his sword-qi body at age 72 (Ch 700 《惊变》). */
    public static final String SUBJECT_STAGE5_DISPERSAL =
            "wang_ping_arc_stage5_dispersal";

    /**
     * The runtime state key tracking the current stage of Wang Ping's
     * mortal-life arc. Values: 0 (not started), 1, 2, 3, 4, 5 (complete).
     * Stored in Wang Ping's NPC runtime state via
     * {@link WorldRuntimeState#updateNpcState}.
     */
    public static final String STATE_KEY_MORTAL_ARC_STAGE = "mortal_arc_stage";

    /**
     * The runtime state key for 青宜's marriage flag. Set to {@code true}
     * in stage 2.
     */
    public static final String STATE_KEY_MARRIED = "married_to_wang_ping";

    /**
     * The runtime state key for 青宜's empress flag. Set to {@code true}
     * in stage 4.
     */
    public static final String STATE_KEY_IS_EMPRESS = "is_empress";

    /**
     * The total span of the Wang Ping mortal-life arc in years, per the
     * Zhihu timeline (19+8+25+10+10=72).
     */
    public static final int ARC_TOTAL_YEARS = 72;

    /**
     * The canon chapter for the dispersal event (Ch 700 《惊变》).
     * Baidu Baike-attested.
     */
    public static final String DISPERSAL_CHAPTER = "Vol 7 Ch 700 《惊变》";

    private WangPingMortalArcEvent() {}

    // ══════════════════════════════════════════════════════════════════
    //  Wang Ping interaction — stages 1, 3, 4, 5
    // ══════════════════════════════════════════════════════════════════

    /**
     * Handle the player's right-click on Wang Ping. Dispatched by
     * {@link dev.ergenverse.history.HistoryEvents#onEntityInteract} when
     * the target cultivator's characterId is "wang_ping".
     *
     * <p>This method advances Wang Ping's mortal-life arc through stages
     * 1 (woodcarving apprenticeship), 3 (war), 4 (emperor reign), and
     * 5 (voluntary dispersal). Stage 2 (marriage) is handled separately
     * by {@link #handleQingYiInteract} — it requires right-clicking 青宜.
     *
     * <p>The method is fully defensive — it no-ops with appropriate
     * canon-faithful messages for each gate failure.
     *
     * @param player   the server player (Wang Lin) who right-clicked
     * @param wangPing the Wang Ping EntityCultivator NPC
     */
    public static void handleWangPingInteract(ServerPlayer player,
                                                EntityCultivator wangPing) {
        // Gate 1: server-side only (defensive — HistoryEvents already checks)
        if (player.level().isClientSide) return;

        // Gate 2: characterId check (defensive — HistoryEvents already checks)
        if (!WANG_PING_CHARACTER_ID.equals(wangPing.getCharacterId())) return;

        ServerLevel level = player.serverLevel();
        WorldRuntimeState runtime = WorldRuntimeState.get(level);

        // Gate 3: Wang Ping must be redeemed (deadUntilRevived=false)
        WorldRuntime rt = WorldRuntime.get();
        if (rt.isInitialized()) {
            NPCRuntime.ActorState state = rt.npcs().getActor(CanonUUID.WANG_PING);
            if (state != null && state.deadUntilRevived) {
                player.sendSystemMessage(Component.literal(
                        "王平尚未被救赎——请先前往朱雀墓，于修炼星晶处完成救赎。")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                player.sendSystemMessage(Component.literal(
                        "Wang Ping has not yet been redeemed — travel to the Suzaku Tomb "
                                + "and complete the redemption at the Cultivation Planet Crystal first.")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                return;
            }
        }

        // Read Wang Ping's runtime state for the arc stage
        CompoundTag wangPingState = runtime.getNpcState(WANG_PING_CHARACTER_ID);
        if (wangPingState == null) {
            wangPingState = new CompoundTag();
        }
        int currentStage = wangPingState.getInt(STATE_KEY_MORTAL_ARC_STAGE);

        // Dispatch based on the current stage
        switch (currentStage) {
            case 0:
                advanceToStage1(player, wangPing, runtime, wangPingState, level.getGameTime());
                break;
            case 1:
                // Stage 1 done, stage 2 (marriage) requires right-clicking 青宜
                player.sendSystemMessage(Component.literal(
                        "王平已开始木雕学徒岁月。他仍在等待青宜的到来。")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
                player.sendSystemMessage(Component.literal(
                        "Wang Ping has begun his woodcarving apprenticeship. "
                                + "He still waits for 青宜's arrival.")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
                player.sendSystemMessage(Component.literal(
                        "[右击青宜以推进至第二阶段：婚姻。]")
                        .withStyle(ChatFormatting.DARK_GRAY));
                player.sendSystemMessage(Component.literal(
                        "[Right-click 青宜 to advance to Stage 2: Marriage.]")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                break;
            case 2:
                advanceToStage3(player, wangPing, runtime, wangPingState, level.getGameTime());
                break;
            case 3:
                advanceToStage4(player, wangPing, runtime, wangPingState, level.getGameTime());
                break;
            case 4:
                advanceToStage5(player, wangPing, runtime, wangPingState, level.getGameTime());
                break;
            case 5:
                // Arc complete — Wang Ping is dispersed
                player.sendSystemMessage(Component.literal(
                        "王平的剑气之躯已散，残魂安息于天逆珠中。")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
                player.sendSystemMessage(Component.literal(
                        "Wang Ping's sword-qi body has dispersed; his remnant soul rests "
                                + "in the Heaven-Defying Bead.")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
                break;
            default:
                // Unknown stage — reset to 0 (defensive)
                Ergenverse.LOGGER.warn("[Ergenverse] CRON-124: Wang Ping mortal_arc_stage "
                        + "had invalid value {} — resetting to 0.", currentStage);
                wangPingState.putInt(STATE_KEY_MORTAL_ARC_STAGE, 0);
                runtime.updateNpcState(WANG_PING_CHARACTER_ID, wangPingState);
                break;
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  青宜 interaction — stage 2 (marriage)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Handle the player's right-click on 青宜. Dispatched by
     * {@link dev.ergenverse.history.HistoryEvents#onEntityInteract} when
     * the target cultivator's characterId is "qing_yi".
     *
     * <p>This method handles ONLY stage 2 (marriage). All other stages
     * are advanced by right-clicking Wang Ping via
     * {@link #handleWangPingInteract}.
     *
     * <p>If the prerequisites for stage 2 are not met (Wang Ping's arc
     * stage is not 1), the method displays a hint message directing the
     * player to the correct next action.
     *
     * @param player the server player (Wang Lin) who right-clicked
     * @param qingYi the 青宜 EntityCultivator NPC
     */
    public static void handleQingYiInteract(ServerPlayer player,
                                              EntityCultivator qingYi) {
        // Gate 1: server-side only
        if (player.level().isClientSide) return;

        // Gate 2: characterId check
        if (!QING_YI_CHARACTER_ID.equals(qingYi.getCharacterId())) return;

        ServerLevel level = player.serverLevel();
        WorldRuntimeState runtime = WorldRuntimeState.get(level);

        // Read Wang Ping's runtime state to determine the arc stage
        CompoundTag wangPingState = runtime.getNpcState(WANG_PING_CHARACTER_ID);
        if (wangPingState == null) {
            wangPingState = new CompoundTag();
        }
        int wangPingStage = wangPingState.getInt(STATE_KEY_MORTAL_ARC_STAGE);

        // Read 青宜's runtime state
        CompoundTag qingYiState = runtime.getNpcState(QING_YI_CHARACTER_ID);
        if (qingYiState == null) {
            qingYiState = new CompoundTag();
        }

        if (wangPingStage < 1) {
            // Stage 1 not started — direct player to Wang Ping first
            player.sendSystemMessage(Component.literal(
                    "青宜轻声说道：「你的儿子王平，他还在等你开始他的凡人岁月。」")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
            player.sendSystemMessage(Component.literal(
                    "Qing Yi says softly: \"Your son Wang Ping — he still waits for you "
                            + "to begin his mortal life.\"")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
            player.sendSystemMessage(Component.literal(
                    "[右击王平以开启他的木雕学徒岁月（第一阶段）。]")
                    .withStyle(ChatFormatting.DARK_GRAY));
            player.sendSystemMessage(Component.literal(
                    "[Right-click Wang Ping to begin his woodcarving apprenticeship "
                            + "(Stage 1).]")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }

        if (wangPingStage >= 2) {
            // Marriage already happened
            if (wangPingStage >= 4) {
                player.sendSystemMessage(Component.literal(
                        "青宜已成为天行帝国的皇后。她以皇后的礼节向你问安。")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
                player.sendSystemMessage(Component.literal(
                        "Qing Yi has become the Empress of the Tianxing Empire. She greets "
                                + "you with empress etiquette.")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            } else {
                player.sendSystemMessage(Component.literal(
                        "青宜微笑着，她是王平的妻子。")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
                player.sendSystemMessage(Component.literal(
                        "Qing Yi smiles — she is Wang Ping's wife.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            }
            return;
        }

        // Stage 2: marriage — advance the arc
        advanceToStage2(player, qingYi, runtime, wangPingState, qingYiState,
                level.getGameTime());
    }

    // ══════════════════════════════════════════════════════════════════
    //  Stage advancement methods
    // ══════════════════════════════════════════════════════════════════

    /**
     * Stage 1: Woodcarving Apprenticeship (~19 years).
     * Wang Lin (alias 许木) lives as a woodcarver at 落月村, raising Wang Ping.
     */
    private static void advanceToStage1(ServerPlayer player,
                                          EntityCultivator wangPing,
                                          WorldRuntimeState runtime,
                                          CompoundTag wangPingState,
                                          long currentTick) {
        wangPingState.putInt(STATE_KEY_MORTAL_ARC_STAGE, 1);
        wangPingState.putLong("mortal_arc_started_tick", currentTick);
        runtime.updateNpcState(WANG_PING_CHARACTER_ID, wangPingState);

        // Display the canon-faithful bilingual narrative.
        player.sendSystemMessage(Component.literal(
                "═══════════════════════════════════════")
                .withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal(
                "你以「许木」之名，在落月村做了木雕师。")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "十九年，木屑飞扬。王平在祁连峰脚下长大，他不能哭，也不能修炼。")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "但你看着他，便觉得这世间仍有可为。")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "You take the name 'Xu Mu' and become a woodcarver at Luo Yue Village.")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Nineteen years. Wood dust flies. Wang Ping grows up at the foot of "
                        + "Qi Lian Peak — he cannot cry, he cannot cultivate.")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "But watching him, you feel there is still something in this world "
                        + "worth doing.")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "═══════════════════════════════════════")
                .withStyle(ChatFormatting.AQUA));

        // Record in HistoryManager.
        HistoryManager.onDiscovery(player, SUBJECT_STAGE1_APPRENTICESHIP,
                "Wang Ping's mortal-life arc (二次化凡) begins. Wang Lin takes the alias "
                        + "'Xu Mu' (许木) and lives as a woodcarver at 落月村 on 冉云星, "
                        + "raising Wang Ping for 19 years. Wang Ping grows up at the foot "
                        + "of 祁连峰 — his sword-qi body cannot cry and cannot cultivate, "
                        + "but Wang Lin watches him and finds purpose. (Canon: Baidu Baike "
                        + "仙逆编年史 + Zhihu timeline 19+8+25+10+10=72 years.)",
                currentTick);

        Ergenverse.LOGGER.info("[Ergenverse] CRON-124: Wang Ping mortal arc stage 1 "
                        + "(woodcarving apprenticeship) advanced by player {}.",
                player.getName().getString());
    }

    /**
     * Stage 2: Marriage to 青宜 (~8 years, Ch 693 《青宜》).
     * Wang Ping marries 青宜 (Sun family outer-surname cultivator).
     * 青宜 makes a 60-year deal with Wang Lin.
     */
    private static void advanceToStage2(ServerPlayer player,
                                          EntityCultivator qingYi,
                                          WorldRuntimeState runtime,
                                          CompoundTag wangPingState,
                                          CompoundTag qingYiState,
                                          long currentTick) {
        wangPingState.putInt(STATE_KEY_MORTAL_ARC_STAGE, 2);
        runtime.updateNpcState(WANG_PING_CHARACTER_ID, wangPingState);

        qingYiState.putBoolean(STATE_KEY_MARRIED, true);
        qingYiState.putLong("marriage_tick", currentTick);
        runtime.updateNpcState(QING_YI_CHARACTER_ID, qingYiState);

        // Display the canon-faithful bilingual narrative.
        player.sendSystemMessage(Component.literal(
                "═══════════════════════════════════════")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal(
                "青宜来到落月村，她是孙家外姓族人，炼气后期。")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "她与王林定下契约：六十年相伴王平，换取元婴后期大圆满。")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "她见了王平——那不能哭的男孩。她答应学笑，笑给他们两人看。")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "八年的凡人夫妻，落月村的木雕师家，多了一缕青色的裙角。")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Qing Yi arrives at Luo Yue Village — a Sun family outer-surname cultivator, "
                        + "Qi Condensation Late Stage.")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "She strikes a deal with Wang Lin: 60 years of companionship with Wang Ping, "
                        + "in exchange for elevation to Late Nascent Soul grand completion.")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "She meets Wang Ping — the boy who cannot cry. She promises to learn to "
                        + "smile, to smile for both of them.")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Eight years of mortal marriage. The woodcarver's hut in Luo Yue Village "
                        + "gains a hint of green robe.")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "═══════════════════════════════════════")
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        // Record in HistoryManager.
        HistoryManager.onDiscovery(player, SUBJECT_STAGE2_MARRIAGE,
                "Wang Ping marries 青宜 (Qing Yi) — Vol 7 Ch 693 《青宜》 (Baidu Baike-"
                        + "attested). 青宜 is a Sun family (孙家) outer-surname cultivator "
                        + "(外姓族人) at 炼气后期 (Qi Condensation Late Stage). She makes "
                        + "a 60-year deal with Wang Lin: 60 years of companionship with "
                        + "Wang Ping in exchange for Wang Lin (at 问鼎 / Ascendant power) "
                        + "elevating her to 元婴后期大圆满 (Late Nascent Soul grand "
                        + "completion). The 60 years span from Ch 693 to Wang Ping's "
                        + "dispersal at Ch 700. (Sources: Baidu Baike 青宜, Sohu deal, "
                        + "QQ News, Zhihu timeline.)",
                currentTick);

        Ergenverse.LOGGER.info("[Ergenverse] CRON-124: Wang Ping mortal arc stage 2 "
                        + "(marriage to 青宜) advanced by player {}.",
                player.getName().getString());
    }

    /**
     * Stage 3: 25 Years of War.
     * Wang Ping becomes a warlord; conquers territories on 冉云星.
     */
    private static void advanceToStage3(ServerPlayer player,
                                          EntityCultivator wangPing,
                                          WorldRuntimeState runtime,
                                          CompoundTag wangPingState,
                                          long currentTick) {
        wangPingState.putInt(STATE_KEY_MORTAL_ARC_STAGE, 3);
        runtime.updateNpcState(WANG_PING_CHARACTER_ID, wangPingState);

        // Display the canon-faithful bilingual narrative.
        player.sendSystemMessage(Component.literal(
                "═══════════════════════════════════════")
                .withStyle(ChatFormatting.RED));
        player.sendSystemMessage(Component.literal(
                "二十五年，王平征伐冉云星。")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "他从落月村走出，踏过祁水城，剑指三大家族的领地。")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "凡人不能修真，但王平有他的剑——你给他的剑气，化作了凡间的铁刃。")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Twenty-five years. Wang Ping conquers Ranyun Star.")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "He walks out of Luo Yue Village, past Qi Shui City, his sword aimed at "
                        + "the territories of the Three Great Families.")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "A mortal cannot cultivate, but Wang Ping has his sword — the sword qi "
                        + "you gave him has become the iron blade of the mortal world.")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "═══════════════════════════════════════")
                .withStyle(ChatFormatting.RED));

        // Record in HistoryManager.
        HistoryManager.onDiscovery(player, SUBJECT_STAGE3_WAR,
                "Wang Ping wages 25 years of war on 冉云星. He walks out of 落月村, "
                        + "passes through 祁水城, and conquers the territories of the "
                        + "Three Great Families (三大家族: 冉家 Ran, 孙家 Sun, 赵家 Zhao). "
                        + "A mortal cannot cultivate, but Wang Ping's sword — powered by "
                        + "the sword qi Wang Lin gave him — becomes the iron blade of "
                        + "the mortal world. (Canon: Baidu Baike 仙逆编年史 + Zhihu "
                        + "timeline 19+8+25+10+10=72 years.)",
                currentTick);

        Ergenverse.LOGGER.info("[Ergenverse] CRON-124: Wang Ping mortal arc stage 3 "
                        + "(25 years of war) advanced by player {}.",
                player.getName().getString());
    }

    /**
     * Stage 4: 10-Year Reign as Emperor of 天行帝国.
     * Wang Ping rules as emperor; 青宜 becomes 皇后 (empress).
     */
    private static void advanceToStage4(ServerPlayer player,
                                          EntityCultivator wangPing,
                                          WorldRuntimeState runtime,
                                          CompoundTag wangPingState,
                                          long currentTick) {
        wangPingState.putInt(STATE_KEY_MORTAL_ARC_STAGE, 4);
        runtime.updateNpcState(WANG_PING_CHARACTER_ID, wangPingState);

        // Update 青宜's state to mark her as empress
        CompoundTag qingYiState = runtime.getNpcState(QING_YI_CHARACTER_ID);
        if (qingYiState == null) {
            qingYiState = new CompoundTag();
        }
        qingYiState.putBoolean(STATE_KEY_IS_EMPRESS, true);
        runtime.updateNpcState(QING_YI_CHARACTER_ID, qingYiState);

        // Display the canon-faithful bilingual narrative.
        player.sendSystemMessage(Component.literal(
                "═══════════════════════════════════════")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal(
                "王平登基为天行帝国的帝王，青宜为皇后。")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(
                "十年帝王，祁水城的金瓦映着朝霞。")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "青宜以皇后的礼节辅佐他——那六十年之约，已过大半。")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Wang Ping ascends the throne as Emperor of the Tianxing Empire; "
                        + "Qing Yi becomes Empress.")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(
                "Ten years of reign. The golden tiles of Qi Shui City reflect the dawn.")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Qing Yi supports him with empress etiquette — the 60-year deal is more "
                        + "than half fulfilled.")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "═══════════════════════════════════════")
                .withStyle(ChatFormatting.GOLD));

        // Record in HistoryManager.
        HistoryManager.onDiscovery(player, SUBJECT_STAGE4_EMPEROR,
                "Wang Ping ascends the throne as Emperor of 天行帝国 (Tianxing Empire); "
                        + "青宜 becomes 皇后 (Empress). Ten years of reign from 祁水城 "
                        + "(Qi Shui City). 青宜 supports him with empress etiquette — "
                        + "the 60-year deal with Wang Lin is more than half fulfilled. "
                        + "(Canon: 163.com Tianxing Empire article, QQ News empress, "
                        + "Baidu Baike 仙逆编年史, Zhihu timeline.)",
                currentTick);

        Ergenverse.LOGGER.info("[Ergenverse] CRON-124: Wang Ping mortal arc stage 4 "
                        + "(10-year emperor reign) advanced by player {}.",
                player.getName().getString());
    }

    /**
     * Stage 5: Voluntary Dispersal at Age 72 (Ch 700 《惊变》).
     *
     * <p>Wang Ping voluntarily disperses his sword-qi body; Wang Lin seals
     * his 残魂 into the 天逆珠. 青宜 follows him in death (殉情而亡); her
     * soul is also collected into the 天逆珠.
     */
    private static void advanceToStage5(ServerPlayer player,
                                          EntityCultivator wangPing,
                                          WorldRuntimeState runtime,
                                          CompoundTag wangPingState,
                                          long currentTick) {
        // ── Prerequisite: player must hold the Heaven-Defying Bead ──
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHand.isEmpty()
                || !(mainHand.getItem() instanceof HeavenDefyingBeadItem beadItem)) {
            player.sendSystemMessage(Component.literal(
                    "王平望着你的空手，轻声说道：「爹，我时辰将至……请将天逆珠持于手中。」")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            player.sendSystemMessage(Component.literal(
                    "Wang Ping looks at your empty hand and says softly: \"Father, my time "
                            + "draws near... please hold the Heaven-Defying Bead in your hand.\"")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            player.sendSystemMessage(Component.literal(
                    "[将天逆珠放在主手，然后再次右击王平，以完成第五阶段。]")
                    .withStyle(ChatFormatting.DARK_GRAY));
            player.sendSystemMessage(Component.literal(
                    "[Place the Heaven-Defying Bead in your main hand, then right-click "
                            + "Wang Ping again to complete Stage 5.]")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }

        // ── Prerequisite: bead must not be dormant ──
        BeadInteriorStage beadStage = beadItem.getStage(mainHand);
        if (beadStage == null || beadStage == BeadInteriorStage.DORMANT_STONE) {
            player.sendSystemMessage(Component.literal(
                    "天逆珠仍在沉睡——它尚未被司图南破开。无法承载王平的残魂。")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            player.sendSystemMessage(Component.literal(
                    "The Heaven-Defying Bead is still dormant — it has not yet been cracked "
                            + "open by Situ Nan. It cannot carry Wang Ping's remnant soul.")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            return;
        }

        ServerLevel level = player.serverLevel();

        // ── All gates pass — execute the dispersal ──

        // Step 1: Set Wang Ping's runtime state (stage 5, write-once)
        wangPingState.putInt(STATE_KEY_MORTAL_ARC_STAGE, 5);
        wangPingState.putLong("dispersal_tick", currentTick);
        wangPingState.putBoolean("is_dead", true);
        runtime.updateNpcState(WANG_PING_CHARACTER_ID, wangPingState);

        // Step 2: Mark Wang Ping as dead in NPCRuntime (deadUntilRevived=true).
        // Canon: the sword-qi body is dispersed; Wang Ping perishes as a mortal.
        WorldRuntime rt = WorldRuntime.get();
        if (rt.isInitialized()) {
            NPCRuntime.ActorState wangPingActor = rt.npcs().getActor(CanonUUID.WANG_PING);
            if (wangPingActor != null) {
                wangPingActor.deadUntilRevived = true;
                Ergenverse.LOGGER.info("[Ergenverse] CRON-124: Wang Ping marked dead "
                        + "(deadUntilRevived=true) — voluntarily dispersed his sword-qi "
                        + "body at age 72 (Ch 700 《惊变》).");

                // Step 3: Dematerialize Wang Ping's EntityCultivator.
                try {
                    boolean dematerialized = rt.actorMaterializer()
                            .dematerializeActor(CanonUUID.WANG_PING, rt);
                    if (dematerialized) {
                        Ergenverse.LOGGER.info("[Ergenverse] CRON-124: Wang Ping "
                                + "EntityCultivator dematerialized (voluntary dispersal).");
                    } else {
                        Ergenverse.LOGGER.info("[Ergenverse] CRON-124: Wang Ping was not "
                                + "currently loaded (dematerialization not needed — likely "
                                + "the right-click came via a stale target reference).");
                    }
                } catch (Throwable t) {
                    Ergenverse.LOGGER.warn("[Ergenverse] CRON-124: dematerializeActor failed "
                            + "for Wang Ping: {}", t.getMessage());
                }
            } else {
                Ergenverse.LOGGER.warn("[Ergenverse] CRON-124: Wang Ping actor state not "
                        + "found in NPCRuntime — cannot mark dead.");
            }

            // Step 4: Mark 青宜 as dead (殉情而亡 — followed Wang Ping in death for love).
            NPCRuntime.ActorState qingYiActor = rt.npcs().getActor(CanonUUID.QING_YI);
            if (qingYiActor != null) {
                qingYiActor.deadUntilRevived = true;
                Ergenverse.LOGGER.info("[Ergenverse] CRON-124: 青宜 marked dead "
                        + "(deadUntilRevived=true) — 殉情而亡 (followed Wang Ping in death).");

                // Step 5: Dematerialize 青宜's EntityCultivator (if loaded).
                try {
                    boolean qingYiDematerialized = rt.actorMaterializer()
                            .dematerializeActor(CanonUUID.QING_YI, rt);
                    if (qingYiDematerialized) {
                        Ergenverse.LOGGER.info("[Ergenverse] CRON-124: 青宜 "
                                + "EntityCultivator dematerialized (殉情而亡).");
                    }
                } catch (Throwable t) {
                    Ergenverse.LOGGER.warn("[Ergenverse] CRON-124: dematerializeActor failed "
                            + "for 青宜: {}", t.getMessage());
                }
            } else {
                Ergenverse.LOGGER.warn("[Ergenverse] CRON-124: 青宜 actor state not "
                        + "found in NPCRuntime — cannot mark dead.");
            }
        }

        // Step 6: Set the bead's NBT flags (seal Wang Ping's 残魂 + 青宜's soul).
        beadItem.setWangPingSoul(mainHand, true);
        beadItem.setQingYiSoul(mainHand, true);

        // Step 7: Spawn canon-faithful particle + sound effects.
        spawnDispersalEffects(level, wangPing.blockPosition());

        // Step 8: Display the canon-faithful bilingual narrative.
        player.sendSystemMessage(Component.literal(
                "═══════════════════════════════════════")
                .withStyle(ChatFormatting.DARK_PURPLE));
        player.sendSystemMessage(Component.literal(
                "雷仙殿的使者揭示真相：王平的肉身是剑气所凝，非真正凡人。")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "王平七十二岁那年，他选择主动散去剑气之躯。")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "「爹……我累了。让我歇息吧。」")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(
                "剑气化作星芒散去，王平的残魂被收入天逆珠。")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "青宜望着他消散的身影，含笑而亡——殉情而亡，她的魂魄也随他入珠。")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "六十年之约已尽。二次化凡，至此终结。")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "The Thunder Immortal Hall envoy reveals the truth: Wang Ping's body is "
                        + "condensed from sword qi, not a true mortal form.")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "At age 72, Wang Ping chooses to voluntarily disperse his sword-qi body.")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "\"Father... I am tired. Let me rest.\"")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(
                "The sword qi dissolves into starlight; Wang Ping's remnant soul is "
                        + "collected into the Heaven-Defying Bead.")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Qing Yi watches his dissipating figure and dies with a smile — 殉情而亡; "
                        + "her soul follows his into the bead.")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "The 60-year deal is fulfilled. The second mortal transformation (二次化凡) "
                        + "arc is complete.")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "「" + DISPERSAL_CHAPTER + "」")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "═══════════════════════════════════════")
                .withStyle(ChatFormatting.DARK_PURPLE));

        // Step 9: Record in HistoryManager.
        HistoryManager.onDiscovery(player, SUBJECT_STAGE5_DISPERSAL,
                "Wang Ping voluntarily disperses his sword-qi body at age 72 — "
                        + DISPERSAL_CHAPTER + " (Baidu Baike-attested). The Thunder "
                        + "Immortal Hall (雷仙殿) envoy exposed the truth that Wang Ping's "
                        + "body was condensed from sword qi, not a true mortal form. Wang "
                        + "Ping chose to disperse; his 残魂 (remnant soul) was sealed into "
                        + "the Heaven-Defying Bead (天逆珠) by Wang Lin. 青宜 followed him "
                        + "in death (殉情而亡 — 'died for love'); her soul was also collected "
                        + "into the bead. The 60-year deal between Wang Lin and 青宜 is "
                        + "fulfilled. The 二次化凡 (second mortal transformation) arc — "
                        + "spanning 72 years (19 woodcarving + 8 marriage + 25 war + 10 "
                        + "emperor + 10 aftermath) — is complete. (Sources: Baidu Baike "
                        + "王平, Baidu Baike 青宜, Baidu Baike 仙逆编年史, Zhihu timeline, "
                        + "Sohu deal, QQ News.) Future: at novel's end (Wang Lin at 踏天境), "
                        + "both Wang Ping and 青宜 are revived as ordinary mortals — a "
                        + "future questline hook.",
                currentTick);

        Ergenverse.LOGGER.info("[Ergenverse] CRON-124: Wang Ping mortal arc stage 5 "
                        + "(voluntary dispersal at age 72, Ch 700 《惊变》) completed by "
                        + "player {}. Wang Ping + 青宜 souls sealed into the bead.",
                player.getName().getString());
    }

    // ══════════════════════════════════════════════════════════════════
    //  Particle + sound effects for stage 5 (dispersal + soul sealing)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Spawn the canon-faithful dispersal particle + sound effects at the
     * position where Wang Ping's sword-qi body dissipates.
     *
     * <p>The visual idiom (sword-qi dispersing into starlight + souls
     * sealing into the bead):
     * <ul>
     *   <li><b>END_ROD ascending burst</b> — the sword qi dissolving upward
     *       into starlight (mirror of the descending spiral in the
     *       WangPingRedemptionEvent — redemption descends, dispersal ascends).</li>
     *   <li><b>FIREWORK central flash</b> — the moment of dispersal (the
     *       "False Life" extinguishing).</li>
     *   <li><b>SOUL particle wisps</b> — Wang Ping's 残魂 + 青宜's soul
     *       flowing toward the player (where the bead is held).</li>
     *   <li><b>AMETHYST_BLOCK_CHIME</b> — the crystalline tone of sword qi
     *       releasing (mirrors the redemption event).</li>
     *   <li><b>WITHER_DEATH</b> — the death tone for Wang Ping's mortal form.</li>
     *   <li><b>BELL</b> — the formal final-rite tone (temple-bell ring).</li>
     * </ul>
     *
     * @param level  the server level
     * @param pos    the block position where Wang Ping dispersed
     */
    private static void spawnDispersalEffects(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 1.0;  // chest height
        double cz = pos.getZ() + 0.5;

        // ── Ascending END_ROD spiral (sword qi dissolving into starlight) ──
        // A spiral that ascends from chest height to 4 blocks above, mirroring
        // the descending spiral in the redemption event. Sword qi ascends.
        final int spiralCount = 40;
        for (int i = 0; i < spiralCount; i++) {
            double t = i / (double) spiralCount;
            double theta = t * Math.PI * 4;  // 2 full turns
            double radius = 0.8 * (1.0 - t * 0.5);  // narrowing spiral
            double dx = Math.cos(theta) * radius;
            double dz = Math.sin(theta) * radius;
            double dy = 4.0 * t;  // ascending
            level.sendParticles(ParticleTypes.END_ROD,
                    cx + dx, pos.getY() + dy, cz + dz, 1,
                    0.0, 0.08, 0.0, 0.0);
        }

        // ── SOUL particle wisps (the souls flowing upward) ──
        level.sendParticles(ParticleTypes.SOUL,
                cx, cy, cz, 24,
                0.4, 0.6, 0.4, 0.05);

        // ── Central FIREWORK flash (False Life extinguishing) ──
        level.sendParticles(ParticleTypes.FIREWORK,
                cx, cy, cz, 16,
                0.3, 0.3, 0.3, 0.05);

        // ── Sounds ──
        // AMETHYST_BLOCK_CHIME — crystalline sword qi releasing (mirrors redemption).
        level.playSound(null, pos,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT,
                1.0F, 0.8F);  // lower pitch = somber tone

        // WITHER_DEATH — the death tone for Wang Ping's mortal form.
        level.playSound(null, pos,
                SoundEvents.WITHER_DEATH, SoundSource.HOSTILE,
                0.8F, 0.6F);  // soft, low — a mortal's passing

        // BELL — the formal final-rite tone (temple-bell ring).
        level.playSound(null, pos,
                SoundEvents.BELL_BLOCK, SoundSource.AMBIENT,
                1.0F, 0.7F);  // lower pitch = solemn
    }
}
