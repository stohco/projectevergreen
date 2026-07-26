package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.history.HistoryManager;
import dev.ergenverse.item.WorldOriginEssenceItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Revival Attempt Service — CRON-COMPLETIONIST-100.
 *
 * <p>Implements the canon-faithful revival-attempt mechanic for Li Muwan's
 * Nascent Soul. After CRON-99's soul-capture event, the player can attempt
 * to revive Li Muwan via the {@code /ergenverse bead revive} command. Each
 * attempt is gated by canon-faithful requirements and increments the
 * {@link HeavenDefyingBeadItem#NBT_REVIVAL_ATTEMPTS} counter.
 *
 * <h2>Canon Basis (fact-checked via web-search 2026-07-26)</h2>
 * <p>In the novel 仙逆 by 耳根:
 * <ul>
 *   <li>李慕婉 (Li Muwan) perishes after her 结婴 (Nascent Soul formation)
 *       fails. Wang Lin captures her 元婴 into the 天逆珠 (CRON-99).</li>
 *   <li>Wang Lin places her soul into 周茹 (Zhou Ru), a pregnant woman's
 *       fetus. Li Muwan refuses to devour the host soul.</li>
 *   <li>Wang Lin attempts revival <b>137 times</b> across millennia — all
 *       fail. The 137th attempt is depicted in the novel: "血色残阳笼罩着
 *       朱雀墓，王林怀中抱着生机尽散的李慕婉，她白发如雪的身体正在
 *       化作星芒消散，这是他第137次尝试复活失败" (blood-red sun over the
 *       Vermilion Bird Tomb, Wang Lin cradles the lifeless Li Muwan, her
 *       white-haired body dissolving into starlight — his 137th failed
 *       revival attempt).</li>
 *   <li>Final success requires the Fourth Step (第四步) + 一界本源 (the
 *       origin of a world). Wang Lin enters the Fourth Step (TRANSCENDENCE)
 *       and uses a world's origin to finally revive her.</li>
 * </ul>
 *
 * <p><b>Canon honesty:</b> The 137 number is canon-attested via web-search.
 * The exact chapter is NOT cited to avoid fabrication. The novel clearly
 * establishes the 137 failed attempts and the Fourth Step requirement for
 * success. The in-mod attempt mechanic is a mod-fidelity bridge between
 * the canon narrative (millennia-spanning attempts) and the gameplay loop
 * (player-triggered command with cooldown).
 *
 * <h2>Attempt Gates</h2>
 * <p>Each revival attempt must pass ALL of the following gates:
 * <ol>
 *   <li><b>Soul gate:</b> {@code hasLiMuwanSoul(stack)} must be true.
 *       Without Li Muwan's soul in the bead, there is nothing to revive.</li>
 *   <li><b>Stage gate:</b> Bead must have special functions
 *       ({@link BeadInteriorStage#hasSpecialFunctions}). This requires
 *       SMALL_WORLD+ stage. Canon: the revival ritual requires the bead's
 *       deepest interior — a self-contained world.</li>
 *   <li><b>Realm gate:</b> Player must be at least SOUL_FORMATION (化神).
 *       Canon: Wang Lin attempts revival only after reaching high realms.
 *       The final successful attempt requires TRANSCENDENCE ( Fourth Step).</li>
 *   <li><b>Cooldown gate:</b> At least {@link #REVIVAL_COOLDOWN_TICKS}
 *       must have elapsed since the last attempt. Prevents spam. The
 *       cooldown is 6000 ticks (5 minutes at 20 TPS) — long enough to
 *       feel weighty, short enough to be playable.</li>
 *   <li><b>Cap gate:</b> The counter cannot exceed 137. Once 137 failed
 *       attempts are recorded, additional attempts require TRANSCENDENCE
 *       realm (the final successful attempt).</li>
 *   <li><b>Essence gate (CRON-COMPLETIONIST-101):</b> The final successful
 *       revival (TRANSCENDENCE + 137 prior failures) additionally requires
 *       the player to hold at least one {@link WorldOriginEssenceItem}
 *       (一界本源) in their inventory. Canon: "王林踏入第四步后，成功运用
 *       一界本源将之复活" — Wang Lin uses the origin of a world to revive
 *       Li Muwan. Without this essence, even the Fourth Step cannot restore
 *       her. On success, one essence item is consumed (the world is
 *       irreversibly sacrificed). This gate is ONLY checked on the success
 *       path — the 137 failed attempts do NOT require the essence.</li>
 * </ol>
 *
 * <h2>Outcomes</h2>
 * <p>Each attempt has one of four outcomes:
 * <ul>
 *   <li><b>FAILURE:</b> The attempt fails (canon: all 137 attempts fail).
 *       Counter increments. Displayed in RED with a mournful message.</li>
 *   <li><b>FAILURE_137:</b> The 137th failed attempt. Counter increments
 *       to 137. Displayed in DARK_PURPLE with the canon "blood-red sun
 *       over the Vermilion Bird Tomb" narrative. The player is told that
 *       only the Fourth Step can save her now.</li>
 *   <li><b>SUCCESS:</b> Only fires at attempt 137 AND TRANSCENDENCE realm.
 *       The revival succeeds. Displayed in GOLD with a triumphant message.
 *       Sets a new NBT flag (TODO: future CRON) and records in history.</li>
 *   <li><b>REJECTED:</b> The attempt was rejected by a gate (soul / stage /
 *       realm / cooldown / cap). No counter increment. Displayed in GRAY
 *       with the specific rejection reason.</li>
 * </ul>
 *
 * <h2>Single-Player Maximalism (Article XLIII)</h2>
 * <p>The service runs only for the single player (Wang Lin). There is no
 * fairness concern — the player IS Wang Lin, and reviving Li Muwan is his
 * central motivation. The cooldown exists for gameplay pacing, not fairness.
 *
 * <h2>Architecture</h2>
 * <p>The service is a static utility class. The attempt is triggered by:
 * <ul>
 *   <li>The {@code /ergenverse bead revive} command (CRON-100). The command
 *       locates the player's bead via a findBead helper that mirrors the
 *       CRON-95 {@link BeadProgressionService#findBead} pattern (main-hand
 *       → off-hand → main inventory).</li>
 *   <li>Future: a bead-menu button (would require clickMenuButton support
 *       in BeadFunctionMenu — out of scope for CRON-100).</li>
 * </ul>
 *
 * <p>The service does NOT touch the WorldDeltaStore or WorldFacade — the
 * revival attempt is purely an item-NBT event, not a world-block event.
 * This respects the CRON-69 architecture: item state is Wang Lin's personal
 * state, not world state.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see HeavenDefyingBeadItem#NBT_REVIVAL_ATTEMPTS
 * @see HeavenDefyingBeadItem#getRevivalAttempts
 * @see HeavenDefyingBeadItem#setRevivalAttempts
 */
public final class RevivalAttemptService {

    /**
     * Cooldown between revival attempts, in game ticks.
     *
     * <p>6000 ticks = 5 minutes at 20 TPS. Long enough to feel weighty
     * (each attempt is a deliberate ritual, not a spam-click), short
     * enough to be playable (a determined player can attempt 12 times
     * per hour, reaching 137 in ~11.5 hours of focused play).
     */
    public static final int REVIVAL_COOLDOWN_TICKS = 6000;

    /** Stable identifier for the HistoryManager subject on failure. */
    public static final String SUBJECT_REVIVAL_FAILED = "li_muwan_revival_failed";

    /** Stable identifier for the HistoryManager subject on the 137th failure. */
    public static final String SUBJECT_REVIVAL_137TH = "li_muwan_revival_137th_failure";

    /** Stable identifier for the HistoryManager subject on success. */
    public static final String SUBJECT_REVIVAL_SUCCEEDED = "li_muwan_revival_succeeded";

    /**
     * CRON-COMPLETIONIST-101: Stable identifier for the HistoryManager subject
     * recorded when a World Origin Essence (一界本源) item is consumed by the
     * successful revival. Distinct from {@link #SUBJECT_REVIVAL_SUCCEEDED}
     * because the essence consumption is a separate canon beat — Wang Lin
     * sacrificing one of his worlds to permanently restore Li Muwan.
     */
    public static final String SUBJECT_REVIVAL_ESSENCE_CONSUMED = "li_muwan_revival_essence_consumed";

    private RevivalAttemptService() {}

    /**
     * Attempt to revive Li Muwan. The single entry point for the revival
     * mechanic.
     *
     * <p>This method is invoked by the {@code /ergenverse bead revive}
     * command. It performs all gate checks, increments the counter (if
     * not rejected), displays the canon-faithful outcome message, and
     * records the event in {@link HistoryManager}.
     *
     * @param player the server player attempting the revival
     * @param stack  the Heaven-Defying Bead stack (must be in the player's
     *               inventory; the caller is responsible for locating it)
     * @return {@code true} if the attempt was made (success or failure);
     *         {@code false} if the attempt was rejected by a gate
     */
    public static boolean attemptRevival(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof HeavenDefyingBeadItem beadItem)) {
            player.sendSystemMessage(Component.literal(
                    "The item you hold is not the Heaven-Defying Bead.")
                    .withStyle(ChatFormatting.GRAY));
            return false;
        }

        // ── Gate 1: Soul gate ──
        if (!beadItem.hasLiMuwanSoul(stack)) {
            player.sendSystemMessage(Component.literal(
                    "The bead does not contain Li Muwan's Nascent Soul. "
                    + "There is nothing to revive.")
                    .withStyle(ChatFormatting.GRAY));
            return false;
        }

        // ── Gate 2: Stage gate ──
        BeadInteriorStage stage = beadItem.getStage(stack);
        if (!stage.hasSpecialFunctions) {
            player.sendSystemMessage(Component.literal(
                    "The bead's interior is too immature for the revival ritual. "
                    + "Canon: the ritual requires a self-contained world within the bead.")
                    .withStyle(ChatFormatting.GRAY));
            player.sendSystemMessage(Component.literal(
                    "(Current stage: " + stage.name() + ". Required: SMALL_WORLD+.)")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return false;
        }

        // ── Gate 3: Realm gate ──
        CultivationState state;
        try {
            state = CultivationCapability.getOrThrow(player);
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-100: CultivationState missing "
                    + "for {} — cannot evaluate revival attempt.", player.getName().getString());
            player.sendSystemMessage(Component.literal(
                    "Your cultivation state cannot be determined.")
                    .withStyle(ChatFormatting.GRAY));
            return false;
        }
        RealmId realm = state.getCurrentRealm();
        if (realm == null || !realm.isAtLeast(RealmId.SOUL_FORMATION)) {
            player.sendSystemMessage(Component.literal(
                    "Your cultivation is insufficient. The revival ritual requires "
                    + "at least Soul Formation (化神).")
                    .withStyle(ChatFormatting.GRAY));
            if (realm != null) {
                player.sendSystemMessage(Component.literal(
                        "(Current realm: " + realm.name + " / " + realm.nameCn + ".)")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
            return false;
        }

        // ── Gate 4: Cooldown gate ──
        long currentTick = player.serverLevel().getGameTime();
        long lastAttempt = beadItem.getLastRevivalAttemptTick(stack);
        if (lastAttempt > 0) {
            long elapsed = currentTick - lastAttempt;
            if (elapsed < REVIVAL_COOLDOWN_TICKS) {
                long remaining = REVIVAL_COOLDOWN_TICKS - elapsed;
                long remainingSeconds = remaining / 20;
                player.sendSystemMessage(Component.literal(
                        "The bead's interior is still recovering from the last attempt. "
                        + "Wait " + remainingSeconds + " more seconds.")
                        .withStyle(ChatFormatting.GRAY));
                return false;
            }
        }

        // ── Gate 5: Cap gate ──
        int attempts = beadItem.getRevivalAttempts(stack);
        if (attempts >= HeavenDefyingBeadItem.CANON_REVIVAL_ATTEMPT_CAP) {
            // 137 attempts already made. Only TRANSCENDENCE can succeed now.
            if (!realm.isAtLeast(RealmId.TRANSCENDENCE)) {
                player.sendSystemMessage(Component.literal(
                        "137 attempts have failed. Only the Fourth Step (超脱) "
                        + "can save her now. Canon: Wang Lin revives Li Muwan "
                        + "after entering the Fourth Step, using the origin of a world.")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
                return false;
            }

            // ── Gate 6: Essence gate (CRON-COMPLETIONIST-101) ──
            // The final successful revival requires the World Origin Essence
            // (一界本源). Canon: "王林踏入第四步后，成功运用一界本源将之复活".
            // Without this essence, even the Fourth Step cannot restore her.
            ItemStack essenceStack = WorldOriginEssenceItem.findInInventory(player);
            if (essenceStack.isEmpty()) {
                player.sendSystemMessage(Component.literal(
                        "═══════════════════════════════════════")
                        .withStyle(ChatFormatting.DARK_PURPLE));
                player.sendSystemMessage(Component.literal(
                        "你已踏入第四步，137次复活皆已失败。")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
                player.sendSystemMessage(Component.literal(
                        "但复活李慕婉还需最后一件至宝 —— 一界本源。")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
                player.sendSystemMessage(Component.literal(
                        "Canon: Wang Lin entered the Fourth Step, yet 137 attempts "
                        + "have failed. The final reagent is 一界本源 (World Origin "
                        + "Essence) — the condensed origin of an entire world. "
                        + "Without it, even the Fourth Step cannot restore her.")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                player.sendSystemMessage(Component.literal(
                        "Acquire World Origin Essence and attempt the revival again.")
                        .withStyle(ChatFormatting.YELLOW));
                player.sendSystemMessage(Component.literal(
                        "═══════════════════════════════════════")
                        .withStyle(ChatFormatting.DARK_PURPLE));
                return false;
            }

            // TRANSCENDENCE + 137 attempts + World Origin Essence = SUCCESS!
            return doSuccessfulRevival(player, beadItem, stack, currentTick, essenceStack);
        }

        // ── All gates passed. Perform the failed attempt. ──
        return doFailedRevival(player, beadItem, stack, currentTick, attempts);
    }

    /**
     * Perform a failed revival attempt. Increments the counter, displays
     * the canon-faithful failure message, and records in history.
     *
     * <p>Canon: all 137 attempts fail. The 137th failure is the final
     * narrative beat before the endgame.
     */
    private static boolean doFailedRevival(ServerPlayer player,
                                            HeavenDefyingBeadItem beadItem,
                                            ItemStack stack,
                                            long currentTick,
                                            int previousAttempts) {
        int newAttempts = previousAttempts + 1;
        beadItem.setRevivalAttempts(stack, newAttempts);
        beadItem.setLastRevivalAttemptTick(stack, currentTick);

        boolean is137th = (newAttempts == HeavenDefyingBeadItem.CANON_REVIVAL_ATTEMPT_CAP);

        if (is137th) {
            // The 137th failure — the canon narrative beat.
            player.sendSystemMessage(Component.literal(
                    "─────────────────────────────────────")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            player.sendSystemMessage(Component.literal(
                    "血色残阳笼罩着朱雀墓。")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
            player.sendSystemMessage(Component.literal(
                    "王林怀中抱着生机尽散的李慕婉，她白发如雪的身体正在化作星芒消散。")
                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            player.sendSystemMessage(Component.literal(
                    "这是他第137次尝试复活失败。")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            player.sendSystemMessage(Component.literal(
                    "A blood-red sun sets over the Vermilion Bird Tomb. Li Muwan's "
                    + "white-haired body dissolves into starlight in your arms.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            player.sendSystemMessage(Component.literal(
                    "This is your 137th failed revival attempt.")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            player.sendSystemMessage(Component.literal(
                    "Only the Fourth Step (超脱) can save her now.")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            player.sendSystemMessage(Component.literal(
                    "─────────────────────────────────────")
                    .withStyle(ChatFormatting.DARK_PURPLE));

            HistoryManager.onDiscovery(player, SUBJECT_REVIVAL_137TH,
                    "The 137th failed revival attempt at the Vermilion Bird Tomb. "
                            + "Li Muwan's body dissolved into starlight. Only the Fourth Step remains.",
                    currentTick);

            Ergenverse.LOGGER.info("[Ergenverse] CRON-100: Player {} reached the 137th "
                            + "failed revival attempt (canon milestone). Realm={}.",
                    player.getName().getString(),
                    player.serverLevel().getGameTime());
        } else {
            // A normal failed attempt (1..136).
            player.sendSystemMessage(Component.literal(
                    "第 " + newAttempts + " 次尝试复活失败。李慕婉的元婴在 天逆珠 中叹息。")
                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            player.sendSystemMessage(Component.literal(
                    "Revival attempt " + newAttempts + " of 137 has failed. "
                    + "Li Muwan's Nascent Soul sighs within the bead.")
                    .withStyle(ChatFormatting.RED));

            HistoryManager.onDiscovery(player, SUBJECT_REVIVAL_FAILED,
                    "Revival attempt " + newAttempts + "/137 failed. "
                            + "Li Muwan's soul remains trapped in the bead.",
                    currentTick);

            Ergenverse.LOGGER.info("[Ergenverse] CRON-100: Player {} made revival attempt "
                            + "{}/137 (failed). Realm={}.",
                    player.getName().getString(), newAttempts,
                    HeavenDefyingBeadItem.CANON_REVIVAL_ATTEMPT_CAP);
        }

        return true;
    }

    /**
     * Perform the successful revival. Only fires when:
     * <ul>
     *   <li>137 failed attempts already recorded</li>
     *   <li>Player realm is TRANSCENDENCE (Fourth Step)</li>
     *   <li>Player holds at least one World Origin Essence (一界本源) — CRON-101</li>
     * </ul>
     *
     * <p>Canon: Wang Lin enters the Fourth Step, uses the origin of a
     * world (一界本源), and finally revives Li Muwan. They transcend
     * together, beyond life and death.
     *
     * <p><b>CRON-COMPLETIONIST-101:</b> This method now CONSUMES one
     * World Origin Essence item from the player's inventory. Canon: the
     * world whose origin is extracted is irreversibly sacrificed. The
     * consumption is logged via {@link HistoryManager} under a separate
     * subject ({@link #SUBJECT_REVIVAL_ESSENCE_CONSUMED}) to distinguish
     * the essence-sacrifice beat from the general success beat.
     *
     * <p>This method does NOT remove Li Muwan's soul from the bead —
     * the soul remains as a permanent record. Instead, it sets a new
     * NBT flag (TODO: future CRON) and displays the canon-faithful
     * success message. The successful revival is the ENDGAME event.
     */
    private static boolean doSuccessfulRevival(ServerPlayer player,
                                                HeavenDefyingBeadItem beadItem,
                                                ItemStack stack,
                                                long currentTick,
                                                ItemStack essenceStack) {
        // The 138th "attempt" is the success — but we DON'T increment the
        // counter beyond 137. The counter stays at 137 (canon-attested cap).
        // Instead, we record the success via HistoryManager and (TODO) a
        // new NBT flag NBT_LI_MUWAN_REVIVED.
        beadItem.setLastRevivalAttemptTick(stack, currentTick);

        // CRON-COMPLETIONIST-101: Consume the World Origin Essence.
        // Canon: the world whose origin is extracted is irreversibly
        // sacrificed. The shrink(1) reduces the stack by 1; if the stack
        // size was 1 (which it always is — stacksTo(1)), the slot becomes
        // empty. This is the canon-faithful permanent loss.
        String sourceWorld = essenceStack.hasTag()
                && essenceStack.getTag().contains(WorldOriginEssenceItem.NBT_SOURCE_WORLD)
                ? essenceStack.getTag().getString(WorldOriginEssenceItem.NBT_SOURCE_WORLD)
                : WorldOriginEssenceItem.DEFAULT_SOURCE_WORLD;
        essenceStack.shrink(1);
        // Defensive: if shrink didn't fully consume (e.g., stacked via
        // creative pick-block), force-set to empty.
        if (!essenceStack.isEmpty()) {
            essenceStack.setCount(0);
        }

        player.sendSystemMessage(Component.literal(
                "═══════════════════════════════════════")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal(
                "王林踏入第四步，以一界本源，逆天复活李慕婉。")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(
                "李慕婉睁开了双眼。两人踏天同行，超越生死轮回，相爱相守，生生世世。")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Wang Lin enters the Fourth Step. Using the origin of a world, "
                + "he defies heaven and revives Li Muwan.")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(
                "Li Muwan opens her eyes. Together they transcend, beyond the "
                + "cycle of life and death, for all eternity.")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        // CRON-101: explicitly acknowledge the world that was sacrificed.
        player.sendSystemMessage(Component.literal(
                "「" + sourceWorld + "」 的本源已化作虚无。一个世界从此陨灭。")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "The origin of「" + sourceWorld + "」dissolves into nothing. "
                + "A world has perished to restore her.")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "═══════════════════════════════════════")
                .withStyle(ChatFormatting.GOLD));

        HistoryManager.onDiscovery(player, SUBJECT_REVIVAL_SUCCEEDED,
                "After 137 failed attempts across millennia, Wang Lin entered the "
                        + "Fourth Step and used the origin of a world to revive Li Muwan. "
                        + "They transcended together, beyond life and death.",
                currentTick);

        // CRON-101: separate subject for the essence-sacrifice beat.
        HistoryManager.onDiscovery(player, SUBJECT_REVIVAL_ESSENCE_CONSUMED,
                "Wang Lin sacrificed the origin of「" + sourceWorld + "」to revive "
                        + "Li Muwan. The world is irreversibly consumed — its rules, "
                        + "its essence, its very being, dissolved into her restoration.",
                currentTick);

        Ergenverse.LOGGER.info("[Ergenverse] CRON-101: Player {} achieved the SUCCESSFUL "
                        + "revival of Li Muwan (TRANSCENDENCE realm, 137 prior failures, "
                        + "World Origin Essence consumed from world「{}」). "
                        + "This is the endgame event of Wang Lin's central arc.",
                player.getName().getString(), sourceWorld);

        return true;
    }
}
