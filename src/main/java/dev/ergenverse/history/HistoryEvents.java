package dev.ergenverse.history;

import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.wanglin.bead.HeavenDefyingBeadItem;
import dev.ergenverse.wanglin.bead.LingTianhouConsumptionEvent;
import dev.ergenverse.wanglin.bead.LingTianhouSwordQiGrantEvent;
import dev.ergenverse.wanglin.bead.WangPingMortalArcEvent;
import dev.ergenverse.wanglin.bead.ZhouRuKunxuDepartureEvent;
import dev.ergenverse.wanglin.bead.ZhouRuSoulTransferEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * HistoryEvents — Forge event listeners that feed the Layer 3 history system.
 *
 * <p>This class captures game events and routes them to the appropriate
 * history recording methods. It exists because some events (like entity
 * interaction) cannot be handled by overriding methods (e.g. {@code
 * Mob.interact()} is final in MC 1.20.1).
 *
 * <h2>Registered events</h2>
 * <ul>
 *   <li>{@code PlayerInteractEvent.EntityInteract} — when a player
 *       right-clicks an EntityCultivator NPC, records the interaction
 *       in NpcMemory via {@link EntityCultivator#recordPlayerInteraction}.</li>
 *   <li>CRON-COMPLETIONIST-110: if the target cultivator's characterId is
 *       {@code "zhou_ru"} and the player's main-hand bead has NOT yet had
 *       its soul transferred, dispatches to
 *       {@link ZhouRuSoulTransferEvent#handleSoulTransfer} — the soul
 *       transfer from the Heaven-Defying Bead into the reincarnation
 *       vessel (周茹). The transfer is gated by the event handler: only
 *       fires if the player holds the bead with Li Muwan's soul captured
 *       (CRON-99 prerequisite) and the transfer has not already occurred
 *       (write-once).</li>
 *   <li>CRON-COMPLETIONIST-112: if the target cultivator's characterId is
 *       {@code "zhou_ru"} and the player's main-hand bead ALREADY has its
 *       soul transferred ({@link HeavenDefyingBeadItem#hasSoulTransferredToZhouRu}
 *       == true), dispatches to
 *       {@link ZhouRuKunxuDepartureEvent#handleDeparture} — the Kunxu
 *       Realm departure quest step. Teleports 周茹 to the Kunxu Realm
 *       (-3500, surface, -3500) and marks her runtime state with
 *       {@code "sent_to_kunxu": true}. The departure fires on a subsequent
 *       right-click after the transfer (same-tick guard prevents the
 *       departure from firing on the same right-click as the transfer).</li>
 *   <li>CRON-COMPLETIONIST-118: if the target cultivator's characterId is
 *       {@code "ling_tianhou"}, dispatches to
 *       {@link LingTianhouSwordQiGrantEvent#handleSwordQiGrant} — Ling
 *       Tianhou grants the player two strands of sword qi (两道剑气) for
 *       Wang Ping's redemption. The grant is write-once per save. The
 *       sword qi item is the canon-faithful prerequisite for the Wang
 *       Ping redemption event (CRON-117/118), replacing the chronologically-
 *       inverted Li Muwan revived proxy.</li>
 * </ul>
 *
 * <p>Must be registered on the Forge event bus:
 * {@code MinecraftForge.EVENT_BUS.register(HistoryEvents.class)}.
 */
public class HistoryEvents {

    private HistoryEvents() {}

    /**
     * When a player right-clicks an EntityCultivator, record the
     * interaction in the NPC's memory (Layer 3: NpcMemory).
     *
     * <p>CRON-COMPLETIONIST-110/112: additionally, if the target is 周茹
     * (characterId="zhou_ru"), dispatch to either the soul-transfer event
     * handler (CRON-110, if the bead's transfer flag is not yet set) or
     * the Kunxu departure event handler (CRON-112, if the bead's transfer
     * flag is already set).
     *
     * <p>CRON-COMPLETIONIST-118: additionally, if the target is Ling Tianhou
     * (characterId="ling_tianhou"), dispatch to the sword qi grant event
     * handler. The grant is write-once per save.
     *
     * <p>This is the Forge-event equivalent of overriding interact(),
     * which is final on Mob in MC 1.20.1.
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // Only handle server-side interactions with cultivator entities
        if (event.getLevel().isClientSide()) return;
        if (!(event.getTarget() instanceof EntityCultivator cultivator)) return;
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        // 1. Always record the interaction in the NPC's memory.
        cultivator.recordPlayerInteraction(serverPlayer);

        // 2. CRON-110/112: if the target is 周茹, dispatch to either the
        //    soul-transfer event handler (CRON-110) or the Kunxu departure
        //    event handler (CRON-112), based on the bead's transfer flag.
        //    Canon: 周茹 is the reincarnation vessel for Li Muwan's soul;
        //    the transfer is a one-time event per save; the Kunxu departure
        //    is a one-time event per save that fires AFTER the transfer.
        if (ZhouRuSoulTransferEvent.CHARACTER_ID.equals(cultivator.getCharacterId())) {
            ItemStack mainHand = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
            boolean isBead = !mainHand.isEmpty()
                    && mainHand.getItem() instanceof HeavenDefyingBeadItem;
            boolean alreadyTransferred = isBead
                    && ((HeavenDefyingBeadItem) mainHand.getItem())
                            .hasSoulTransferredToZhouRu(mainHand);

            if (alreadyTransferred) {
                // CRON-112: soul already transferred — dispatch to Kunxu
                // departure event. The handler is fully defensive — it
                // no-ops if the same-tick guard fires (transfer just
                // happened this tick), if the runtime state's
                // sent_to_kunxu flag is already true, or if the runtime
                // state's pregnant_with_li_muwan_soul flag is missing.
                ZhouRuKunxuDepartureEvent.handleDeparture(serverPlayer, cultivator);
            } else {
                // CRON-110: soul not yet transferred (or no bead, or no
                // soul captured, or dormant bead) — dispatch to the
                // transfer event. The handler is fully defensive — it
                // no-ops with appropriate canon-faithful messages for
                // each gate failure.
                ZhouRuSoulTransferEvent.handleSoulTransfer(serverPlayer, cultivator);
            }
            return;  // 周茹 interaction fully handled — don't fall through
        }

        // 3. CRON-118: if the target is Ling Tianhou (凌天侯), dispatch to
        //    the sword qi grant event handler. The handler is fully
        //    defensive — it no-ops with a "already granted" message if
        //    the grant has already fired (write-once per save).
        //    Canon: Ling Tianhou personally gave Wang Lin two strands of
        //    sword qi (两道剑气) to rebuild Wang Ping's body. The grant
        //    is the canon-faithful prerequisite for the Wang Ping
        //    redemption event (CRON-117/118).
        if (LingTianhouSwordQiGrantEvent.CHARACTER_ID.equals(cultivator.getCharacterId())) {
            LingTianhouSwordQiGrantEvent.handleSwordQiGrant(serverPlayer, cultivator);
            return;  // Ling Tianhou interaction fully handled
        }

        // 4. CRON-123: if the target is 天运子 (Tian Yun Zi), dispatch to
        //    the Ling Tianhou consumption event handler. The handler is
        //    fully defensive — it no-ops with a "prerequisite not met"
        //    message if Ling Tianhou has not yet granted the sword qi,
        //    and a "already consumed" message if the consumption has
        //    already fired (write-once per save).
        //    Canon: 天运子 (master of 天运宗, the #1 sect on 天运星) needs
        //    to consume cultivators (生生吞噬) to reincarnate repeatedly.
        //    His 98th awakening target is 凌天侯 (Ling Tianhou). The
        //    consumption marks Ling Tianhou as dead and dematerializes
        //    his EntityCultivator (if loaded). 大罗剑宗 declines thereafter.
        if (LingTianhouConsumptionEvent.CHARACTER_ID.equals(cultivator.getCharacterId())) {
            LingTianhouConsumptionEvent.handleConsumption(serverPlayer, cultivator);
            return;  // 天运子 interaction fully handled
        }

        // 5. CRON-124: if the target is Wang Ping (王平) or 青宜 (Qing Yi),
        //    dispatch to the Wang Ping mortal-life arc event handler.
        //    The arc is a 5-stage event chain:
        //      Stage 1: Woodcarving apprenticeship (right-click Wang Ping)
        //      Stage 2: Marriage to 青宜 (right-click 青宜)
        //      Stage 3: 25 years of war (right-click Wang Ping)
        //      Stage 4: 10-year emperor reign (right-click Wang Ping)
        //      Stage 5: Voluntary dispersal at age 72 (right-click Wang Ping
        //               with the Heaven-Defying Bead in main hand)
        //    The handler is fully defensive — it no-ops with appropriate
        //    canon-faithful messages for each gate failure (e.g., wrong
        //    stage, missing bead, dormant bead). Each stage is write-once
        //    per save (the mortal_arc_stage runtime flag advances forward
        //    only).
        //    Canon: the 二次化凡 arc spans 72 years on 冉云星 (Ranyun Star),
        //    per Zhihu timeline 19+8+25+10+10=72. Stage 5 dispersal is
        //    Vol 7 Ch 700 《惊变》 (Baidu Baike-attested). 青宜 follows Wang
        //    Ping in death (殉情而亡); both souls sealed into the 天逆珠.
        if (WangPingMortalArcEvent.WANG_PING_CHARACTER_ID.equals(cultivator.getCharacterId())) {
            WangPingMortalArcEvent.handleWangPingInteract(serverPlayer, cultivator);
            return;  // Wang Ping interaction fully handled
        }

        if (WangPingMortalArcEvent.QING_YI_CHARACTER_ID.equals(cultivator.getCharacterId())) {
            WangPingMortalArcEvent.handleQingYiInteract(serverPlayer, cultivator);
            return;  // 青宜 interaction fully handled
        }
    }
}