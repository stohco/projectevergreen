package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.history.HistoryManager;
import dev.ergenverse.simulation.WorldRuntimeState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * Zhou Ru Soul Transfer Event — CRON-COMPLETIONIST-110.
 *
 * <p>Implements the canon-faithful soul-transfer mechanic: after CRON-99's
 * soul-capture event, Wang Lin places Li Muwan's 元婴 (Nascent Soul) into
 * 周茹 (Zhou Ru) — a mortal vessel who becomes the host for Li Muwan's
 * reincarnation. This is the second major beat of the Li Muwan revival arc
 * (CRON-99 capture → <b>CRON-110 transfer</b> → CRON-100 revival attempts →
 * CRON-102 successful revival).
 *
 * <h2>Canon Basis (fact-checked via web-search 2026-07-26)</h2>
 * <p>In the novel 仙逆 (Renegade Immortal) by 耳根:
 * <ul>
 *   <li>After Li Muwan perishes (failed 结婴 breakthrough, CRON-99 captures
 *       her 元婴 into the 天逆珠), Wang Lin seeks a way to revive her.</li>
 *   <li>He places her soul into 周茹 (Zhou Ru) — a mortal vessel. Li Muwan's
 *       soul becomes the foundation for 周茹's life; she is the reincarnation
 *       vessel.</li>
 *   <li>Li Muwan chooses NOT to devour the host soul. She instead addresses
 *       Wang Lin as 'uncle' (王林叔叔) — a deliberate choice that preserves
 *       周茹's identity while housed Li Muwan's cultivation essence.</li>
 *   <li> 周茹 grows up to become a Soul Transformation (化神) cultivator
 *       under 慕冰梅 (Mu Bingmei) in 昆墟之境 (Kunxu Realm).</li>
 *   <li>Eventually 周茹 is reincarnated on the Immortal Astral Continent
 *       (仙遗族 / IAC arc) where she lives an ordinary life.</li>
 *   <li>Wang Lin attempts revival 137 times across millennia (CRON-100).
 *       Final success requires the Fourth Step + 一界本源 (CRON-101/102).</li>
 * </ul>
 *
 * <p><b>Canon honesty:</b> The novel clearly establishes (1) the soul
 * transfer to 周茹 as Wang Lin's first revival attempt, (2) Li Muwan's
 * choice not to devour the host soul, (3) 周茹's eventual cultivation under
 * Mu Bingmei, and (4) the 137-revival-attempt arc. The exact chapter is
 * not cited here to avoid fabrication. Sources: Baidu Baike 周茹,
 * RICanonicalDatabase entry N10, Fandom wiki.
 *
 * <h2>Trigger</h2>
 * <p>Called from {@link dev.ergenverse.history.HistoryEvents#onEntityInteract}
 * when a ServerPlayer right-clicks an EntityCultivator whose characterId
 * equals {@value #CHARACTER_ID}. The handler:
 * <ol>
 *   <li>Checks the player's main hand for a Heaven-Defying Bead. The bead
 *       must be in the MAIN HAND (not off-hand) — canon: Wang Lin actively
 *       wields the bead during the soul transfer; it's not a passive
 *       artifact.</li>
 *   <li>Checks that the bead has Li Muwan's soul captured
 *       ({@link HeavenDefyingBeadItem#hasLiMuwanSoul} == true). This is
 *       the CRON-99 prerequisite — without the soul in the bead, there's
 *       nothing to transfer.</li>
 *   <li>Checks that the soul has NOT already been transferred
 *       ({@link HeavenDefyingBeadItem#hasSoulTransferredToZhouRu} == false).
 *       The transfer is a one-time event (write-once flag).</li>
 *   <li>If all checks pass: sets the bead's
 *       {@link HeavenDefyingBeadItem#setSoulTransferredToZhouRu} flag,
 *       spawns canon-faithful particle/sound effects at 周茹's location,
 *       displays a bilingual message, and records the event in
 *       {@link HistoryManager#onDiscovery}.</li>
 *   <li>Also marks 周茹's persistent runtime state with
 *       {@code "pregnant_with_li_muwan_soul": true} — a future questline
 *       hook for her cultivation arc under Mu Bingmei.</li>
 * </ol>
 *
 * <h2>Why a dedicated class (not inline in HistoryEvents)</h2>
 * <p>{@link dev.ergenverse.history.HistoryEvents} is a thin Forge-event
 * dispatcher. Inlining the soul-transfer logic there would couple
 * canon-character-specific logic to the general event-listener
 * infrastructure. A dedicated static-utility class keeps the event
 * listener clean and makes the soul-transfer logic independently
 * testable. The class lives in the {@code wanglin.bead} package because
 * it's canonically about the bead's soul-storage function — the transfer
 * is the bead releasing Li Muwan's soul into the vessel.
 *
 * <h2>State Transition Diagram</h2>
 * <pre>
 *   CRON-99:  Li Muwan dies  →  bead.hasLiMuwanSoul = true
 *   CRON-110: Player interacts with Zhou Ru  →  bead.hasSoulTransferredToZhouRu = true
 *                                            →  Zhou Ru runtime state: pregnant_with_li_muwan_soul = true
 *   CRON-100: Player invokes /ergenverse bead revive  →  revivalAttempts++
 *   CRON-102: 137th attempt with World Origin Essence  →  bead.isLiMuwanRevived = true (write-once)
 * </pre>
 *
 * <p>Note: the bead's {@code hasLiMuwanSoul} flag is NOT cleared by the
 * transfer. Canonically, the soul remains "associated" with the bead
 * (Wang Lin's intent) even as it resides in 周茹. This keeps the revival
 * attempt service (CRON-100) operational — it gates on
 * {@code hasLiMuwanSoul}, which remains true.
 *
 * <h2>Single-Player Maximalism (Article XLIII)</h2>
 * <p>There is exactly one player (Wang Lin). The {@code serverPlayer}
 * parameter is the sole candidate. There is exactly one 周茹 (canon UUID
 * {@link dev.ergenverse.runtime.CanonUUID#ZHOU_RU}). The transfer is a
 * one-time event per save.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see HeavenDefyingBeadItem#hasLiMuwanSoul
 * @see HeavenDefyingBeadItem#hasSoulTransferredToZhouRu
 * @see HeavenDefyingBeadItem#setSoulTransferredToZhouRu
 * @see dev.ergenverse.runtime.CanonUUID#ZHOU_RU
 * @see dev.ergenverse.history.HistoryEvents#onEntityInteract
 * @see LiMuwanSoulCaptureEvent (predecessor — CRON-99)
 */
public final class ZhouRuSoulTransferEvent {

    /** Canon character ID for Zhou Ru. Must match CanonUUID.ZHOU_RU's profile. */
    public static final String CHARACTER_ID = "zhou_ru";

    /**
     * Maximum distance (in blocks, Euclidean) between the player and 周茹
     * at which the soul transfer can fire. Vanilla right-click reach is
     * ~4.5 blocks; this is a defensive upper bound in case of latency
     * desync. The actual gating is the right-click event itself.
     */
    public static final double MAX_INTERACT_DISTANCE = 8.0;

    /** Number of END_ROD particles spawned in the soul-transfer ring. */
    private static final int SOUL_RING_PARTICLE_COUNT = 40;

    /** Number of SQUID_INK particles spawned (dark god-force visual). */
    private static final int INK_PARTICLE_COUNT = 20;

    /** Number of END_ROD particles in the upward soul-stream. */
    private static final int SOUL_STREAM_PARTICLE_COUNT = 15;

    private ZhouRuSoulTransferEvent() {}

    /**
     * Handle the soul-transfer interaction. Called from
     * {@link dev.ergenverse.history.HistoryEvents#onEntityInteract} when
     * the player right-clicks an EntityCultivator whose characterId equals
     * {@value #CHARACTER_ID}.
     *
     * <p>This method is the SOLE caller of
     * {@link HeavenDefyingBeadItem#setSoulTransferredToZhouRu}. Before
     * CRON-110, the reincarnation step of the Li Muwan arc was missing —
     * the bead could capture the soul (CRON-99) but had no path to
     * transfer it to the vessel. CRON-110 closes this gap.
     *
     * @param serverPlayer the player who right-clicked 周茹
     * @param zhouRu       the EntityCultivator whose characterId is "zhou_ru"
     */
    public static void handleSoulTransfer(ServerPlayer serverPlayer,
                                          EntityCultivator zhouRu) {
        // Defensive: never run on client side.
        if (serverPlayer.level().isClientSide()) return;

        // Defensive: validate the target's characterId. The caller
        // (HistoryEvents) gates on this, but defensive validation is
        // cheap and prevents subtle bugs if the caller changes.
        if (!CHARACTER_ID.equals(zhouRu.getCharacterId())) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-110: ZhouRuSoulTransferEvent invoked "
                    + "with wrong characterId='{}'. Expected '{}'. No-op.",
                    zhouRu.getCharacterId(), CHARACTER_ID);
            return;
        }

        ServerLevel serverLevel = (ServerLevel) serverPlayer.level();

        // ── 1. Locate the Heaven-Defying Bead in the player's MAIN HAND ──
        // Canon: Wang Lin actively wields the bead during the soul transfer.
        // Main-hand-only is a deliberate canon-fidelity constraint — the
        // off-hand is for shield/essence, not the bead during ritual use.
        ItemStack mainHand = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHand.isEmpty() || !(mainHand.getItem() instanceof HeavenDefyingBeadItem beadItem)) {
            announceNoBead(serverPlayer, zhouRu);
            return;
        }

        // ── 2. Check CRON-99 prerequisite: bead must have Li Muwan's soul ──
        // Without the soul in the bead, there is nothing to transfer.
        // This is the canon-faithful gate: the transfer cannot fire before
        // CRON-99's soul-capture event.
        if (!beadItem.hasLiMuwanSoul(mainHand)) {
            announceNoSoulInBead(serverPlayer, zhouRu);
            return;
        }

        // ── 3. Check write-once gate: soul must not already be transferred ──
        // The transfer is a one-time event per save. Calling this a second
        // time is a no-op with a canon-faithful "already done" message.
        if (beadItem.hasSoulTransferredToZhouRu(mainHand)) {
            announceAlreadyTransferred(serverPlayer, zhouRu);
            return;
        }

        // ── 4. Check bead stage (DORMANT_STONE cannot release a soul) ──
        // Canon: the bead must be CRACK_OPENED before the soul can be
        // released (just as it must be CRACK_OPENED before it can capture
        // a soul — see CRON-99). This is a defensive gate; if the bead has
        // Li Muwan's soul, it was CRACK_OPENED at the CRON-99 event.
        // Included for robustness.
        BeadInteriorStage stage = beadItem.getStage(mainHand);
        if (stage == null || stage == BeadInteriorStage.DORMANT_STONE) {
            announceDormantBead(serverPlayer, zhouRu);
            return;
        }

        // ── 5. All checks pass — execute the soul transfer ──
        // This is the SOLE call to setSoulTransferredToZhouRu — closes the
        // CRON-110 gap (analogous to CRON-99 closing the setLiMuwanSoul gap).
        beadItem.setSoulTransferredToZhouRu(mainHand, true);

        // ── 6. Mark 周茹's persistent runtime state ──
        // Future questline hook: 周茹's cultivation arc under Mu Bingmei
        // reads this flag to know she's the vessel.
        markZhouRuAsVessel(serverLevel, zhouRu);

        // ── 7. Spawn canon-faithful particle + sound effects at 周茹 ──
        spawnSoulTransferEffects(serverLevel, zhouRu);

        // ── 8. Display canon-faithful bilingual message ──
        announceTransferSuccess(serverPlayer, zhouRu);

        // ── 9. Record in HistoryManager ──
        HistoryManager.onDiscovery(serverPlayer,
                "li_muwan_soul_transferred_to_zhou_ru",
                "Transferred Li Muwan's Nascent Soul from the Heaven-Defying Bead "
                        + "into Zhou Ru (周茹) — the reincarnation vessel.",
                serverLevel.getGameTime());

        Ergenverse.LOGGER.info("[Ergenverse] CRON-110: Li Muwan's soul transferred from "
                        + "the bead into Zhou Ru (周茹) at {} by player {} (bead stage={}, "
                        + "zhou_ru entity id={}). setSoulTransferredToZhouRu(stack, true) "
                        + "invoked — closes the CRON-110 reincarnation-questline gap.",
                zhouRu.blockPosition(),
                serverPlayer.getName().getString(),
                stage == null ? "null" : stage.name(),
                zhouRu.getId());
    }

    /**
     * Mark 周茹's persistent runtime state with the
     * {@code "pregnant_with_li_muwan_soul": true} flag.
     *
     * <p>This is a future-questline hook — 周茹's cultivation arc under
     * 慕冰梅 (Mu Bingmei) in 昆墟之境 (Kunxu Realm) reads this flag to know
     * she is the vessel. The flag is stored in the WorldRuntimeState's
     * per-character NPC state, which persists across saves.
     *
     * <p>If the runtime state is unavailable (e.g., world not initialized),
     * this method is a no-op — the soul transfer is still considered
     * successful (the bead's flag is the canonical record).
     *
     * @param serverLevel the server level
     * @param zhouRu      the EntityCultivator (周茹)
     */
    private static void markZhouRuAsVessel(ServerLevel serverLevel,
                                           EntityCultivator zhouRu) {
        try {
            WorldRuntimeState runtime = WorldRuntimeState.get(serverLevel);
            CompoundTag state = runtime.getNpcState(zhouRu.getCharacterId());
            if (state == null) state = new CompoundTag();
            state.putBoolean("pregnant_with_li_muwan_soul", true);
            state.putLong("soul_transfer_tick", serverLevel.getGameTime());
            runtime.updateNpcState(zhouRu.getCharacterId(), state);
        } catch (Throwable t) {
            // Defensive: the bead's NBT flag is the canonical record.
            // The runtime-state flag is a future-questline hook.
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-110: failed to mark Zhou Ru's "
                    + "runtime state as vessel: {}", t.getMessage());
        }
    }

    /**
     * Spawn canon-faithful particle + sound effects at 周茹's location.
     *
     * <p>The visual idiom:
     * <ul>
     *   <li><b>END_ROD particles in an ascending ring around 周茹</b> —
     *       the soul stream entering her body from above. END_ROD is
     *       bright white-rising — canon-faithful for a soul descending.</li>
     *   <li><b>SQUID_INK particles in a small burst at 周茹's feet</b> —
     *       the dark god-force residue of the transfer. SQUID_INK is
     *       dark-purple-black, matching the soul-flag visual idiom.</li>
     *   <li><b>END_ROD particles in a vertical stream above 周茹</b> —
     *       the soul entering from the bead (held by Wang Lin nearby).</li>
     *   <li><b>WITHER_SPAWN sound at low pitch</b> — the deep, ominous
     *       tone of a soul-transfer ritual. AVOIDS ENDERMAN_AMBIENT
     *       (too casual) and ENDER_DRAGON_GROWL (too loud/apocalyptic).</li>
     *   <li><b>AMETHYST_BLOCK_CHIME sound at high pitch</b> — the bright
     *       crystalline tone of the bead's release.</li>
     * </ul>
     *
     * @param serverLevel the server level
     * @param zhouRu      the EntityCultivator (周茹)
     */
    private static void spawnSoulTransferEffects(ServerLevel serverLevel,
                                                  EntityCultivator zhouRu) {
        double centerX = zhouRu.getX();
        double centerY = zhouRu.getY() + 1.0;  // chest height
        double centerZ = zhouRu.getZ();
        BlockPos pos = zhouRu.blockPosition();

        // ── Ascending ring of END_ROD around 周茹 ──
        // 40 particles in a ring at increasing heights (chest → 2 blocks above head)
        for (int i = 0; i < SOUL_RING_PARTICLE_COUNT; i++) {
            double theta = (i / (double) SOUL_RING_PARTICLE_COUNT) * Math.PI * 2;
            double radius = 0.8;
            double dx = Math.cos(theta) * radius;
            double dz = Math.sin(theta) * radius;
            // Distribute heights from chest (1.0) to 3 blocks above feet (3.0)
            double dy = 1.0 + ((i % 4) * 0.5);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    centerX + dx, centerY + dy, centerZ + dz, 1,
                    0.0, 0.05, 0.0, 0.0);
        }

        // ── SQUID_INK burst at 周茹's feet (dark god-force residue) ──
        serverLevel.sendParticles(ParticleTypes.SQUID_INK,
                centerX, zhouRu.getY() + 0.1, centerZ, INK_PARTICLE_COUNT,
                0.6, 0.1, 0.6, 0.02);

        // ── Vertical END_ROD stream above 周茹 (soul entering from above) ──
        for (int i = 0; i < SOUL_STREAM_PARTICLE_COUNT; i++) {
            double dy = 2.0 + (i * 0.3);  // 2 to ~6.5 blocks above feet
            // Slight horizontal jitter for an organic stream
            double dx = (i % 3 - 1) * 0.1;
            double dz = (i % 5 - 2) * 0.1;
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    centerX + dx, zhouRu.getY() + dy, centerZ + dz, 1,
                    0.0, 0.0, 0.0, 0.0);
        }

        // ── Sound: WITHER_SPAWN (deep) + AMETHYST_BLOCK_CHIME (bright) ──
        // WITHER_SPAWN: deep ominous tone — the soul-transfer ritual.
        serverLevel.playSound(null, pos,
                SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE,
                0.6F, 0.7F);  // volume 0.6, pitch 0.7 (deep)
        // AMETHYST_BLOCK_CHIME: bright crystalline — the bead's release.
        serverLevel.playSound(null, pos,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT,
                0.8F, 1.2F);  // volume 0.8, pitch 1.2 (bright)

        // ── Central flash (soul-release burst) ──
        serverLevel.sendParticles(ParticleTypes.FIREWORK,
                centerX, centerY, centerZ, 8,
                0.4, 0.4, 0.4, 0.05);
    }

    /**
     * Announce successful soul transfer with a canon-faithful bilingual
     * message. The bilingual format mirrors CRON-99's pattern: Chinese
     * (the novel's original language) first, English second, both styled
     * with light-purple + italic for the sacred/mournful tone.
     *
     * <p>The message honors Li Muwan's choice to address Wang Lin as
     * 'uncle' (王林叔叔) — a canon-attested detail from Baidu Baike.
     */
    private static void announceTransferSuccess(ServerPlayer player,
                                                 EntityCultivator zhouRu) {
        player.sendSystemMessage(
                Component.literal("─────────────────────────────────────")
                        .withStyle(ChatFormatting.DARK_PURPLE));
        player.sendSystemMessage(
                Component.literal("李慕婉的元婴自天逆珠流出，融入周茹之身。")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(
                Component.literal("Li Muwan's Nascent Soul flows from the bead into Zhou Ru.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("She does not devour the host soul. She calls you 'uncle' — 王林叔叔.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("The path of revival stretches across millennia. 137 attempts remain.")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("─────────────────────────────────────")
                        .withStyle(ChatFormatting.DARK_PURPLE));
    }

    /**
     * Announce that the player has no bead in main hand. Canon-faithful
     * safeguard: the soul transfer requires the bead to be actively
     * wielded in the main hand.
     */
    private static void announceNoBead(ServerPlayer player,
                                        EntityCultivator zhouRu) {
        player.sendSystemMessage(
                Component.literal("周茹静静地看着你，什么也没有发生。")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("Zhou Ru watches you quietly. Nothing happens.")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("(Hold the Heaven-Defying Bead in your main hand to attempt the soul transfer.)")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    /**
     * Announce that the bead has no soul to transfer. Canon-faithful
     * safeguard: the soul transfer can only fire after CRON-99's
     * soul-capture event.
     */
    private static void announceNoSoulInBead(ServerPlayer player,
                                              EntityCultivator zhouRu) {
        player.sendSystemMessage(
                Component.literal("天逆珠静默无声——其中并无元婴可转移。")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("The bead is silent — no Nascent Soul resides within to transfer.")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("(Canon: Li Muwan's soul must be captured into the bead first — CRON-99 prerequisite.)")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    /**
     * Announce that the soul has already been transferred. Canon-faithful
     * safeguard: the transfer is a one-time event per save.
     */
    private static void announceAlreadyTransferred(ServerPlayer player,
                                                    EntityCultivator zhouRu) {
        player.sendSystemMessage(
                Component.literal("李慕婉的元婴已栖于周茹之身，无需再次转移。")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("Li Muwan's soul already resides within Zhou Ru. No further transfer is needed.")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("(Canon: the soul transfer is a one-time event. Pursue the revival arc via /ergenverse bead revive.)")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    /**
     * Announce that the bead is dormant (DORMANT_STONE stage) and cannot
     * release a soul. Canon-faithful safeguard: the bead must be
     * CRACK_OPENED before the soul can be released (or captured — see
     * CRON-99).
     *
     * <p>Defensive only: if the bead has Li Muwan's soul, it was
     * CRACK_OPENED at the CRON-99 event. This branch should never fire
     * in normal gameplay but is included for robustness.
     */
    private static void announceDormantBead(ServerPlayer player,
                                             EntityCultivator zhouRu) {
        player.sendSystemMessage(
                Component.literal("天逆珠冰冷沉寂——它尚未被司徒南开启。")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("The bead is cold and dormant — it has not been cracked open.")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("(Canon: Situ Nan must crack open the bead before it can release a soul.)")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
