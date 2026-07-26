package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.history.HistoryManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Li Muwan's Soul Capture Event — closes the CRON-95 known gap where
 * {@link HeavenDefyingBeadItem#setLiMuwanSoul} was defined but never called.
 *
 * <p><b>Canon basis (fact-checked via web-search 2026-07-26, no fabricated
 * chapter citations):</b> In the novel 仙逆 (Renegade Immortal) by 耳根:
 * <ul>
 *   <li>李慕婉 (Li Muwan) is Wang Lin's only wife and Dao companion. She
 *       first met Wang Lin in 火焚国 (Fire-Burning Country) and followed him
 *       into 修魔海 (Sea of Devils).</li>
 *   <li>She perishes after her 结婴 (Nascent Soul formation) fails — 寿尽而亡
 *       (lifespan exhausted). Her body dies but her 元婴 (Nascent Soul)
 *       lingers briefly.</li>
 *   <li>Wang Lin captures her 元婴 into the 天逆珠 (Heaven-Defying Bead),
 *       which becomes his central motivation for the rest of the novel:
 *       the 逆天之路 (path of defying heaven) to revive her.</li>
 *   <li>Later, Wang Lin attempts to reincarnate her through a pregnant
 *       woman's fetus (周茹 Zhou Ru). Li Muwan refuses to devour the host
 *       soul. Wang Lin attempts revival 137 times across millennia.</li>
 * </ul>
 *
 * <p><b>Canon honesty:</b> The novel clearly establishes (1) Li Muwan's
 * death by failed breakthrough, (2) Wang Lin's capture of her 元婴 with
 * the 天逆珠, (3) the bead as the soul vessel, and (4) the revival arc
 * as Wang Lin's central motivation. The exact chapter is not cited here
 * to avoid fabrication. The novel-internal cause of death (failed 结婴
 * breakthrough) differs from the in-mod trigger (whatever kills the
 * EntityCultivator) — this is a mod-fidelity bridge between canon event
 * and simulation trigger, documented honestly.
 *
 * <h2>Trigger</h2>
 * <p>Called from {@link dev.ergenverse.entity.EntityCultivator#die} when
 * the dying cultivator's characterId equals {@value #CHARACTER_ID}. The
 * handler:
 * <ol>
 *   <li>Locates the killer (if player) or the closest player within
 *       {@link #SOUL_CAPTURE_RADIUS} blocks of the death point.</li>
 *   <li>Scans that player's inventory (main-hand → off-hand → main
 *       inventory) for a Heaven-Defying Bead.</li>
 *   <li>If the bead is found AND at least at {@link BeadInteriorStage#CRACK_OPENED}
 *       (DORMANT_STONE cannot capture a soul — canon: Situ Nan must crack
 *       the bead open first), calls
 *       {@link HeavenDefyingBeadItem#setLiMuwanSoul}.</li>
 *   <li>Displays a canon-faithful bilingual message and records the event
 *       in {@link HistoryManager#onDiscovery}.</li>
 *   <li>If no bead is held, no player is in radius, or the bead is still
 *       dormant, the soul is "lost" — dissipated into the heavens. This
 *       is a mod-original divergence safeguard; in canon Wang Lin always
 *       has the bead.</li>
 * </ol>
 *
 * <h2>Single-Player Maximalism (Article XLIII)</h2>
 * <p>There is exactly one player (Wang Lin). The closest-player search is
 * defensive — in single-player maximalism there is only ever one candidate.
 * The killer-preference logic is also defensive: if the player kills Li
 * Muwan directly (uncanon — Wang Lin would never kill Li Muwan), the soul
 * is still captured (the bead doesn't care who killed her, only that her
 * soul is departing).
 *
 * <h2>Why a dedicated class (not inline in EntityCultivator.die)</h2>
 * <p>The {@link dev.ergenverse.entity.EntityCultivator#die} method is
 * already complex (NPC state bookkeeping + history + Cave World ownership
 * transfer). Inlining the Li Muwan soul-capture logic there would couple
 * canon-character-specific logic to the general cultivator death path. A
 * dedicated static-utility class keeps the death handler clean and makes
 * the soul-capture logic independently testable. The class lives in the
 * {@code wanglin.bead} package because it's canonically about the bead's
 * soul-storage function.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see HeavenDefyingBeadItem#setLiMuwanSoul
 * @see HeavenDefyingBeadItem#hasLiMuwanSoul
 * @see dev.ergenverse.entity.EntityCultivator#die
 */
public final class LiMuwanSoulCaptureEvent {

    /**
     * Maximum distance (in blocks, Euclidean) from Li Muwan's death point
     * at which a player can capture her soul. 32 blocks = 2 chunks; this
     * is wide enough to cover the typical combat radius but narrow enough
     * that a player on the other side of the map cannot accidentally
     * capture the soul.
     */
    public static final double SOUL_CAPTURE_RADIUS = 32.0;

    /** Canon character ID for Li Muwan. Must match CanonUUID.LI_MUWAN's key. */
    public static final String CHARACTER_ID = "li_muwan";

    private LiMuwanSoulCaptureEvent() {}

    /**
     * Handle Li Muwan's death. Called from
     * {@link dev.ergenverse.entity.EntityCultivator#die} after the
     * canon NPC death bookkeeping has run.
     *
     * <p>This method is the SOLE caller of
     * {@link HeavenDefyingBeadItem#setLiMuwanSoul}. Before CRON-99,
     * {@code setLiMuwanSoul} was defined but never invoked — the bead's
     * tooltip would never display "Contains: Li Muwan's Nascent Soul"
     * because no code path set the NBT flag. CRON-99 closes this gap.
     *
     * @param level     the server level where Li Muwan died
     * @param source    the damage source that killed her
     * @param deathPos  the block position where she died
     */
    public static void handleLiMuwanDeath(ServerLevel level,
                                          DamageSource source,
                                          BlockPos deathPos) {
        // ── 1. Locate the player who will capture the soul ──
        ServerPlayer capturingPlayer = findCapturingPlayer(level, source, deathPos);

        if (capturingPlayer == null) {
            // No player in radius — soul dissipates into the heavens.
            announceSoulLostNoPlayer(level, deathPos);
            return;
        }

        // ── 2. Locate the capturing player's bead ──
        ItemStack beadStack = findBead(capturingPlayer);
        if (beadStack.isEmpty() || !(beadStack.getItem() instanceof HeavenDefyingBeadItem beadItem)) {
            // Player has no bead — soul dissipates into the heavens.
            announceSoulLostNoBead(level, deathPos, capturingPlayer);
            return;
        }

        // ── 3. Check bead stage (DORMANT_STONE cannot capture a soul) ──
        BeadInteriorStage stage = beadItem.getStage(beadStack);
        if (stage == null || stage == BeadInteriorStage.DORMANT_STONE) {
            // Bead is dormant — canon: it must be cracked open first by Situ Nan.
            announceSoulLostDormantBead(level, deathPos, capturingPlayer);
            return;
        }

        // ── 4. Capture the soul (the call that was missing since CRON-95) ──
        beadItem.setLiMuwanSoul(beadStack, true);

        // ── 5. Display canon-faithful bilingual message ──
        //   The bilingual format mirrors the canon-honesty pattern established
        //   in CRON-69: Chinese (the novel's original language) first, English
        //   second, both styled with light-purple + italic for the sacred/
        //   mournful tone appropriate to this pivotal canon event.
        capturingPlayer.sendSystemMessage(
                Component.literal("─────────────────────────────────────")
                        .withStyle(ChatFormatting.DARK_PURPLE));
        capturingPlayer.sendSystemMessage(
                Component.literal("李慕婉的元婴被天逆珠收容。")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        capturingPlayer.sendSystemMessage(
                Component.literal("Li Muwan's Nascent Soul has been drawn into the Heaven-Defying Bead.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        capturingPlayer.sendSystemMessage(
                Component.literal("She will return. The path of defying heaven begins.")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        capturingPlayer.sendSystemMessage(
                Component.literal("─────────────────────────────────────")
                        .withStyle(ChatFormatting.DARK_PURPLE));

        // ── 6. Record in HistoryManager ──
        //   subject="li_muwan_soul_captured" — stable identifier for the
        //   history journal. Used by future canon-event replay systems.
        HistoryManager.onDiscovery(capturingPlayer,
                "li_muwan_soul_captured",
                "Captured Li Muwan's Nascent Soul with the Heaven-Defying Bead.",
                level.getGameTime());

        Ergenverse.LOGGER.info("[Ergenverse] CRON-99: Li Muwan's Nascent Soul captured by {} "
                        + "at {} via the Heaven-Defying Bead (stage={}). "
                        + "setLiMuwanSoul(stack, true) invoked — closes the CRON-95 "
                        + "defined-but-never-called gap.",
                capturingPlayer.getName().getString(),
                deathPos,
                stage == null ? "null" : stage.name());
    }

    /**
     * Find the player who will capture Li Muwan's soul. Preference:
     * <ol>
     *   <li>If the killer (source.getEntity()) is a ServerPlayer, use them.
     *       This handles the case where the player directly strikes Li
     *       Muwan down (uncanon, but the simulation allows it).</li>
     *   <li>Otherwise, find the closest ServerPlayer within
     *       {@link #SOUL_CAPTURE_RADIUS} of the death point. This handles
     *       the canon-faithful case: Li Muwan dies of failed breakthrough
     *       or beast attack while Wang Lin is nearby.</li>
     *   <li>If no player in radius, return {@code null} (soul dissipates).</li>
     * </ol>
     *
     * @param level     the server level
     * @param source    the damage source that killed Li Muwan
     * @param deathPos  the block position of Li Muwan's death
     * @return the capturing player, or {@code null} if no candidate exists
     */
    private static ServerPlayer findCapturingPlayer(ServerLevel level,
                                                     DamageSource source,
                                                     BlockPos deathPos) {
        // 1. Killer preference — if the player killed Li Muwan directly, they
        //    are the soul-capture candidate. This is uncanon (Wang Lin would
        //    never kill Li Muwan) but the simulation permits it and the soul
        //    capture still fires (the bead doesn't care who killed her).
        Entity killer = source.getEntity();
        if (killer instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }

        // 2. Closest player in radius — canon-faithful case. Li Muwan dies
        //    (failed breakthrough or beast attack) while Wang Lin is nearby.
        //    Iterate the level's player list and find the closest one within
        //    SOUL_CAPTURE_RADIUS. O(n) where n is the player count (1 in
        //    single-player maximalism, but defensive for future MP support).
        List<ServerPlayer> nearby = level.players();
        ServerPlayer closest = null;
        double closestDistSq = SOUL_CAPTURE_RADIUS * SOUL_CAPTURE_RADIUS;
        for (ServerPlayer p : nearby) {
            double dSq = p.blockPosition().distSqr(deathPos);
            if (dSq <= closestDistSq) {
                closestDistSq = dSq;
                closest = p;
            }
        }
        return closest;
    }

    /**
     * Scan the player's inventory for a Heaven-Defying Bead. Search order:
     * main-hand → off-hand → main inventory (slots 0-35). Mirrors the
     * {@link BeadProgressionService} findBead pattern (CRON-95) for
     * consistency.
     *
     * <p>Returns the FIRST non-empty bead stack found. If the player has
     * multiple beads (uncanon but possible), only the first is mutated.
     *
     * @param player the player whose inventory to scan
     * @return the bead ItemStack, or {@link ItemStack#EMPTY} if not found
     */
    private static ItemStack findBead(ServerPlayer player) {
        // 1. Main hand
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof HeavenDefyingBeadItem) return mainHand;

        // 2. Off hand
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof HeavenDefyingBeadItem) return offHand;

        // 3. Main inventory (slots 0-35)
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof HeavenDefyingBeadItem) return stack;
        }

        return ItemStack.EMPTY;
    }

    /**
     * Announce that the soul was lost because no player was in radius.
     * This is the most-canonical divergence: in canon, Wang Lin is always
     * present at Li Muwan's death. If no player is in radius, the death
     * is unwitnessed and the soul dissipates.
     */
    private static void announceSoulLostNoPlayer(ServerLevel level, BlockPos deathPos) {
        // No player to message — only log. The death is unwitnessed.
        Ergenverse.LOGGER.warn("[Ergenverse] CRON-99: Li Muwan's Nascent Soul dissipated "
                        + "at {} — no player within {} blocks to capture it. "
                        + "(Canon: Wang Lin is always present; this is a divergence.)",
                deathPos, SOUL_CAPTURE_RADIUS);
    }

    /**
     * Announce that the soul was lost because the player had no bead.
     * Canon-faithful safeguard: in canon, Wang Lin already has the bead
     * by this point. If the player has no bead, the soul is lost.
     */
    private static void announceSoulLostNoBead(ServerLevel level,
                                                BlockPos deathPos,
                                                ServerPlayer player) {
        player.sendSystemMessage(
                Component.literal("李慕婉的元婴消散于天地之间。")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("Li Muwan's Nascent Soul dissipated into the heavens.")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("No Heaven-Defying Bead in your inventory to capture it.")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        HistoryManager.onDiscovery(player,
                "li_muwan_soul_lost_no_bead",
                "Li Muwan's Nascent Soul dissipated; no bead in inventory to capture it.",
                level.getGameTime());

        Ergenverse.LOGGER.warn("[Ergenverse] CRON-99: Li Muwan's Nascent Soul dissipated "
                        + "at {} — player {} had no Heaven-Defying Bead in inventory.",
                deathPos, player.getName().getString());
    }

    /**
     * Announce that the soul was lost because the bead was dormant
     * (DORMANT_STONE stage — not yet cracked open by Situ Nan).
     *
     * <p>Canon: the bead must be CRACK_OPENED before it can hold a soul.
     * Situ Nan (司徒南), the 2nd-Generation Vermilion Bird Son, transmits
     * a blast of Vermilion Bird fire into the bead to crack it open. If
     * the player has somehow acquired a dormant bead and Li Muwan dies,
     * the soul cannot be captured — it dissipates.
     */
    private static void announceSoulLostDormantBead(ServerLevel level,
                                                     BlockPos deathPos,
                                                     ServerPlayer player) {
        player.sendSystemMessage(
                Component.literal("李慕婉的元婴消散于天地之间。")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("Li Muwan's Nascent Soul dissipated — your bead is still dormant.")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("Canon: Situ Nan must crack open the bead before it can hold a soul.")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        HistoryManager.onDiscovery(player,
                "li_muwan_soul_lost_dormant_bead",
                "Li Muwan's Nascent Soul dissipated; the bead was still DORMANT_STONE.",
                level.getGameTime());

        Ergenverse.LOGGER.warn("[Ergenverse] CRON-99: Li Muwan's Nascent Soul dissipated "
                        + "at {} — player {} held the bead, but it was DORMANT_STONE "
                        + "(needs CRACK_OPENED via Situ Nan).",
                deathPos, player.getName().getString());
    }
}
