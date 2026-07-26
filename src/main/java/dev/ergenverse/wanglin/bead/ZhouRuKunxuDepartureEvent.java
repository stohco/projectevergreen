package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.history.HistoryManager;
import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
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
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Zhou Ru Kunxu Departure Event — CRON-COMPLETIONIST-112.
 *
 * <p>Implements the canon-faithful post-transfer quest step: after CRON-110's
 * soul transfer (Li Muwan's 元婴 placed into 周茹), Wang Lin escorts 周茹 to
 * the Kunxu Realm (昆虚界) and entrusts her to 木冰眉 (Mu Bingmei) for
 * cultivation. This is the bridge between CRON-110 (soul transfer at
 * Vermilion Bird Capital) and CRON-111 (cultivation growth service, which
 * requires 周茹 to be near Mu Bingmei at the Kunxu Realm).
 *
 * <h2>Canon Basis (fact-checked via web-search 2026-07-26)</h2>
 * <p>In the novel 仙逆 (Renegade Immortal) by 耳根:
 * <ul>
 *   <li>After Wang Lin places Li Muwan's soul into 周茹 (CRON-110), 周茹
 *       eventually becomes a Soul Transformation (炼虚) cultivator under
 *       木冰眉 (Mu Bingmei) in the Kunxu Realm (昆虚界).</li>
 *   <li>The novel does not dwell on the logistics of how 周茹 travels to
 *       the Kunxu Realm. The Kunxu Realm is canonically a pocket-realm
 *       accessible from the Cave World — the journey is implied rather
 *       than narrated beat-by-beat.</li>
 *   <li>The disciple-master relationship between 周茹 and Mu Bingmei is
 *       canon-attested: Mu Bingmei takes 周茹 as her disciple (RICanonicalDatabase
 *       N19 + L74 + N10).</li>
 *   <li>Wang Lin's role in placing 周茹 under Mu Bingmei's care is the
 *       narrative bridge — he entrusts his "niece" (周茹 calls him
 *       王林叔叔) to his third wife (Mu Bingmei) for cultivation.</li>
 * </ul>
 *
 * <p><b>Canon honesty:</b> The novel clearly establishes (1) the soul
 * transfer to 周茹 as Wang Lin's first revival attempt (CRON-110),
 * (2) Mu Bingmei as 周茹's master in the Kunxu Realm, (3) 周茹's eventual
 * cultivation to Soul Transformation. The exact chapter of the journey
 * is not cited here to avoid fabrication. Sources: Baidu Baike 周茹,
 * RICanonicalDatabase entry N10 (Zhou Ru), N19 (Mu Bingmei), L74 (Kunxu
 * Realm), Fandom wiki.
 *
 * <h2>Trigger</h2>
 * <p>Called from {@link dev.ergenverse.history.HistoryEvents#onEntityInteract}
 * when a ServerPlayer right-clicks an EntityCultivator whose characterId
 * equals {@value #CHARACTER_ID}, AND the player's main-hand bead already
 * has {@link HeavenDefyingBeadItem#hasSoulTransferredToZhouRu} == true
 * (i.e., CRON-110's soul transfer has already fired on a prior right-click).
 *
 * <p>The handler implements the following gates:
 * <ol>
 *   <li><b>Same-tick guard.</b> If the soul transfer fired on THIS tick
 *       (i.e., {@code level.getGameTime() == soul_transfer_tick}), no-op.
 *       This prevents the departure from firing on the same right-click
 *       as the transfer — the player must right-click 周茹 a second time
 *       to send her to the Kunxu Realm. Canon-faithful: the transfer
 *       and the journey are distinct narrative beats.</li>
 *   <li><b>Write-once guard.</b> If 周茹's runtime state already has
 *       {@code "sent_to_kunxu": true}, no-op with a canon-faithful
 *       "already departed" message. The departure is a one-time event
 *       per save.</li>
 *   <li><b>CRON-110 prerequisite.</b> If the soul transfer flag is
 *       absent from 周茹's runtime state (e.g., world was reset, NPC
 *       state was cleared), the departure cannot fire. Defensive — the
 *       caller gates on the bead's {@code hasSoulTransferredToZhouRu}
 *       flag, but this double-checks the runtime state.</li>
 * </ol>
 *
 * <p>If all gates pass: teleports 周茹 to {@link PlanetSuzakuBlueprint#KUNXU_REALM}
 * (-3500, surface, -3500), sets the {@code "sent_to_kunxu": true} flag in
 * her runtime state, spawns canon-faithful particle/sound effects at both
 * the origin (departure burst) and the destination (arrival burst),
 * displays a bilingual message, and records the event in
 * {@link HistoryManager#onDiscovery}.
 *
 * <h2>Why a dedicated class (not inline in HistoryEvents)</h2>
 * <p>Mirrors CRON-99's {@link LiMuwanSoulCaptureEvent} and CRON-110's
 * {@link ZhouRuSoulTransferEvent} — a dedicated static-utility class keeps
 * the event listener clean and makes the departure logic independently
 * testable. The class lives in the {@code wanglin.bead} package because
 * the departure is gated on the bead's {@code hasSoulTransferredToZhouRu}
 * flag (the bead is the canon-faithful state record of the transfer).
 *
 * <h2>Teleportation Safety</h2>
 * <p>The destination chunk at (-3500, -3500) is typically unloaded when
 * the departure fires (the player is near 周茹 at Vermilion Bird Capital,
 * ~5000 blocks away). The handler:
 * <ol>
 *   <li>Force-loads the destination chunk via
 *       {@link ServerLevel#getChunk(int, int)} (synchronous load).</li>
 *   <li>Queries the surface Y via
 *       {@link ServerLevel#getHeightmapPos(Heightmap.Types#MOTION_BLOCKING, BlockPos)}
 *       to find a safe teleport Y.</li>
 *   <li>Teleports 周茹 via {@link EntityCultivator#teleportTo(double, double, double)}.</li>
 * </ol>
 * If the destination is in an unloaded chunk after teleport, the
 * {@link dev.ergenverse.runtime.materialize.CanonActorMaterializer}
 * will dematerialize 周茹 on chunk unload and rematerialize her when the
 * player next loads the area. The canon UUID persistence ensures 周茹
 * is the same entity across dematerialize/rematerialize cycles.
 *
 * <h2>State Transition Diagram</h2>
 * <pre>
 *   CRON-99:  Li Muwan dies  →  bead.hasLiMuwanSoul = true
 *   CRON-110: Player interacts with Zhou Ru (1st right-click)
 *             →  bead.hasSoulTransferredToZhouRu = true
 *             →  Zhou Ru runtime: pregnant_with_li_muwan_soul = true
 *             →  Zhou Ru runtime: soul_transfer_tick = currentTick
 *   CRON-112: Player interacts with Zhou Ru (2nd right-click, later tick)
 *             →  Zhou Ru teleports to KUNXU_REALM (-3500, surface, -3500)
 *             →  Zhou Ru runtime: sent_to_kunxu = true
 *             →  Zhou Ru runtime: kunxu_departure_tick = currentTick
 *   CRON-111: Zhou Ru near Mu Bingmei (daily tick) → Zhou Ru realm advances
 *             mortal → qi_condensation → ... → soul_transformation (CANON_CAP)
 *   CRON-100: Player invokes /ergenverse bead revive → revivalAttempts++
 *   CRON-102: 137th attempt with World Origin Essence → bead.isLiMuwanRevived = true
 * </pre>
 *
 * <h2>Single-Player Maximalism (Article XLIII)</h2>
 * <p>There is exactly one player (Wang Lin). The {@code serverPlayer}
 * parameter is the sole candidate. There is exactly one 周茹 (canon UUID
 * {@link dev.ergenverse.runtime.CanonUUID#ZHOU_RU}). The departure is a
 * one-time event per save. After departure, 周茹 persists at the Kunxu
 * Realm across save/load cycles via CanonActorMaterializer + WorldRuntimeState.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see ZhouRuSoulTransferEvent (predecessor — CRON-110)
 * @see ZhouRuCultivationGrowthService (successor — CRON-111)
 * @see dev.ergenverse.runtime.PlanetSuzakuBlueprint#KUNXU_REALM
 * @see dev.ergenverse.runtime.CanonUUID#ZHOU_RU
 * @see dev.ergenverse.runtime.CanonUUID#MU_BINGMEI
 * @see dev.ergenverse.history.HistoryEvents#onEntityInteract
 */
public final class ZhouRuKunxuDepartureEvent {

    /** Canon character ID for Zhou Ru. Must match CanonUUID.ZHOU_RU's profile. */
    public static final String CHARACTER_ID = "zhou_ru";

    /**
     * Maximum distance (in blocks, Euclidean) between the player and 周茹
     * at which the departure can fire. Vanilla right-click reach is ~4.5
     * blocks; this is a defensive upper bound in case of latency desync.
     * Mirrors {@link ZhouRuSoulTransferEvent#MAX_INTERACT_DISTANCE}.
     */
    public static final double MAX_INTERACT_DISTANCE = 8.0;

    /** Number of END_ROD particles in the departure ring at origin. */
    private static final int DEPARTURE_RING_PARTICLE_COUNT = 32;

    /** Number of DRAGON_BREATH particles for the rift-tear visual. */
    private static final int RIFT_PARTICLE_COUNT = 24;

    /** Number of END_ROD particles in the arrival burst at Kunxu Realm. */
    private static final int ARRIVAL_PARTICLE_COUNT = 24;

    /** Number of END_ROD particles in the ascending soul-stream column. */
    private static final int STREAM_PARTICLE_COUNT = 12;

    private ZhouRuKunxuDepartureEvent() {}

    /**
     * Handle the Kunxu departure interaction. Called from
     * {@link dev.ergenverse.history.HistoryEvents#onEntityInteract} when
     * the player right-clicks an EntityCultivator whose characterId equals
     * {@value #CHARACTER_ID} AND the player's main-hand bead already has
     * {@link HeavenDefyingBeadItem#hasSoulTransferredToZhouRu} == true.
     *
     * <p>This method is the SOLE caller that sets the {@code "sent_to_kunxu"}
     * runtime state flag. Before CRON-112, there was no path for 周茹 to
     * reach the Kunxu Realm — the player had to physically herd her across
     * thousands of blocks. CRON-112 closes this gap with a canon-faithful
     * "Wang Lin escorts 周茹 to the Kunxu Realm" quest step.
     *
     * @param serverPlayer the player who right-clicked 周茹 (Wang Lin)
     * @param zhouRu       the EntityCultivator whose characterId is "zhou_ru"
     */
    public static void handleDeparture(ServerPlayer serverPlayer,
                                        EntityCultivator zhouRu) {
        // Defensive: never run on client side.
        if (serverPlayer.level().isClientSide()) return;

        // Defensive: validate the target's characterId. The caller
        // (HistoryEvents) gates on this, but defensive validation is
        // cheap and prevents subtle bugs if the caller changes.
        if (!CHARACTER_ID.equals(zhouRu.getCharacterId())) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-112: ZhouRuKunxuDepartureEvent "
                    + "invoked with wrong characterId='{}'. Expected '{}'. No-op.",
                    zhouRu.getCharacterId(), CHARACTER_ID);
            return;
        }

        ServerLevel serverLevel = (ServerLevel) serverPlayer.level();

        // ── 1. Defensive: re-validate the bead is in main hand and the
        // transfer flag is set. The caller (HistoryEvents) gates on this,
        // but defensive validation prevents subtle bugs. ──
        ItemStack mainHand = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHand.isEmpty() || !(mainHand.getItem() instanceof HeavenDefyingBeadItem beadItem)) {
            // No bead in main hand — this branch should never fire because
            // the caller dispatches to the transfer handler (CRON-110) when
            // there's no bead or no transfer flag. Included for robustness.
            announceNoBead(serverPlayer);
            return;
        }
        if (!beadItem.hasSoulTransferredToZhouRu(mainHand)) {
            // Defensive: the transfer flag is not set. The caller should
            // have dispatched to the transfer handler instead. No-op.
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-112: ZhouRuKunxuDepartureEvent "
                    + "invoked but bead.hasSoulTransferredToZhouRu is false. No-op.");
            return;
        }

        // ── 2. Fetch 周茹's runtime state ──
        CompoundTag zhouRuState;
        try {
            WorldRuntimeState runtime = WorldRuntimeState.get(serverLevel);
            zhouRuState = runtime.getNpcState(zhouRu.getCharacterId());
            if (zhouRuState == null) zhouRuState = new CompoundTag();
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-112: failed to fetch Zhou Ru's "
                    + "runtime state: {}", t.getMessage());
            announceInternalError(serverPlayer);
            return;
        }

        // ── 3. CRON-110 prerequisite: pregnant_with_li_muwan_soul must be true ──
        // Defensive: the bead's hasSoulTransferredToZhouRu flag is the canonical
        // record, but the runtime state should also have the flag set by CRON-110.
        // If the runtime state is missing the flag (e.g., world was reset), the
        // departure cannot fire — we cannot teleport 周茹 to the Kunxu Realm
        // without the canon-faithful soul-transfer prerequisite.
        if (!zhouRuState.getBoolean("pregnant_with_li_muwan_soul")) {
            announceNoSoulTransferred(serverPlayer);
            return;
        }

        // ── 4. Write-once guard: sent_to_kunxu must be false ──
        // The departure is a one-time event per save. Calling this a second
        // time is a no-op with a canon-faithful "already departed" message.
        if (zhouRuState.getBoolean("sent_to_kunxu")) {
            announceAlreadyDeparted(serverPlayer);
            return;
        }

        // ── 5. Same-tick guard: departure must fire on a DIFFERENT right-click
        // than the soul transfer. ──
        // CRON-110 records soul_transfer_tick in 周茹's runtime state. If the
        // current tick equals that value, the transfer fired on THIS right-click
        // — the player has not yet had a chance to read the transfer message.
        // We require the departure to fire on a SUBSEQUENT right-click.
        long currentTick = serverLevel.getGameTime();
        long soulTransferTick = zhouRuState.getLong("soul_transfer_tick");
        if (currentTick == soulTransferTick) {
            // Same-tick — silently no-op. The player will right-click again
            // on a subsequent tick and the departure will fire then.
            // We do NOT print a message here — the CRON-110 transfer message
            // has already been displayed this tick.
            return;
        }

        // ── 6. All gates pass — execute the Kunxu departure ──
        // Record the origin position BEFORE teleporting (for the departure burst).
        double originX = zhouRu.getX();
        double originY = zhouRu.getY();
        double originZ = zhouRu.getZ();

        // Compute the destination surface position.
        BlockPos destBlockPos = new BlockPos(
                PlanetSuzakuBlueprint.KUNXU_REALM.x,
                PlanetSuzakuBlueprint.KUNXU_REALM.y,
                PlanetSuzakuBlueprint.KUNXU_REALM.z);

        // Force-load the destination chunk so the heightmap query is accurate
        // and the teleport destination is safe.
        serverLevel.getChunk(destBlockPos.getX() >> 4, destBlockPos.getZ() >> 4);

        // Query the surface Y via MOTION_BLOCKING heightmap (highest non-
        // motion-blocking block: ignores air, leaves, etc.; returns the
        // first solid block from the top).
        BlockPos surfacePos = serverLevel.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING, destBlockPos);

        double destX = surfacePos.getX() + 0.5;
        double destY = surfacePos.getY();
        double destZ = surfacePos.getZ() + 0.5;

        // ── 7. Spawn departure burst at origin (before teleport) ──
        spawnDepartureBurst(serverLevel, originX, originY, originZ);

        // ── 8. Teleport 周茹 to the Kunxu Realm surface ──
        // Use the simple same-dimension teleportTo(x, y, z). 周茹 is an
        // EntityCultivator (extends Mob → LivingEntity → Entity), so this
        // method is available.
        zhouRu.teleportTo(destX, destY, destZ);

        // ── 9. Spawn arrival burst at destination (after teleport) ──
        spawnArrivalBurst(serverLevel, destX, destY, destZ);

        // ── 10. Set the sent_to_kunxu flag in 周茹's runtime state ──
        zhouRuState.putBoolean("sent_to_kunxu", true);
        zhouRuState.putLong("kunxu_departure_tick", currentTick);
        zhouRuState.putInt("kunxu_x", surfacePos.getX());
        zhouRuState.putInt("kunxu_y", surfacePos.getY());
        zhouRuState.putInt("kunxu_z", surfacePos.getZ());
        try {
            WorldRuntimeState runtime = WorldRuntimeState.get(serverLevel);
            runtime.updateNpcState(zhouRu.getCharacterId(), zhouRuState);
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-112: failed to persist Zhou Ru's "
                    + "sent_to_kunxu flag: {}", t.getMessage());
        }

        // ── 11. Display canon-faithful bilingual message ──
        announceDepartureSuccess(serverPlayer, zhouRu);

        // ── 12. Record in HistoryManager ──
        HistoryManager.onDiscovery(serverPlayer,
                "zhou_ru_kunxu_departure",
                "Wang Lin sent Zhou Ru (周茹) to the Kunxu Realm (昆虚界) at "
                        + "(" + surfacePos.getX() + ", " + surfacePos.getY() + ", "
                        + surfacePos.getZ() + "), entrusting her to Mu Bingmei (木冰眉) "
                        + "for cultivation. The Li Muwan soul within her begins its "
                        + "long cultivation arc toward Soul Transformation.",
                serverLevel.getGameTime());

        Ergenverse.LOGGER.info("[Ergenverse] CRON-112: Zhou Ru (周茹) departed for the "
                        + "Kunxu Realm at tick {}. Origin: ({:.1f}, {:.1f}, {:.1f}). "
                        + "Destination: ({:.1f}, {:.1f}, {:.1f}). Player: {}. "
                        + "sent_to_kunxu=true recorded in runtime state.",
                currentTick,
                originX, originY, originZ,
                destX, destY, destZ,
                serverPlayer.getName().getString());
    }

    /**
     * Spawn the departure burst at 周茹's origin position. The visual idiom:
     * <ul>
     *   <li><b>END_ROD particles in an ascending ring</b> — the soul-stream
     *       rising as 周茹 departs. END_ROD is bright white-rising.</li>
     *   <li><b>DRAGON_BREATH particles in a magenta burst</b> — the rift
     *       tear opening. DRAGON_BREATH is purple-magenta, matching the
     *       teleportation-rift visual idiom.</li>
     *   <li><b>END_ROD particles in a vertical stream</b> — 周茹's form
     *       ascending into the rift.</li>
     *   <li><b>ENDERMAN_TELEPORT sound</b> — the classic teleport sound.</li>
     *   <li><b>AMETHYST_BLOCK_CHIME sound at high pitch</b> — the bright
     *       crystalline tone of the bead's release.</li>
     * </ul>
     */
    private static void spawnDepartureBurst(ServerLevel serverLevel,
                                              double x, double y, double z) {
        BlockPos pos = BlockPos.containing(x, y, z);

        // ── Ascending ring of END_ROD around 周茹 ──
        for (int i = 0; i < DEPARTURE_RING_PARTICLE_COUNT; i++) {
            double theta = (i / (double) DEPARTURE_RING_PARTICLE_COUNT) * Math.PI * 2;
            double radius = 0.9;
            double dx = Math.cos(theta) * radius;
            double dz = Math.sin(theta) * radius;
            double dy = 1.0 + ((i % 4) * 0.5);  // chest height to 2.5 above feet
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    x + dx, y + dy, z + dz, 1,
                    0.0, 0.05, 0.0, 0.0);
        }

        // ── DRAGON_BREATH burst (rift tear) ──
        serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                x, y + 1.0, z, RIFT_PARTICLE_COUNT,
                0.8, 0.4, 0.8, 0.05);

        // ── Vertical END_ROD stream above 周茹 (form ascending) ──
        for (int i = 0; i < STREAM_PARTICLE_COUNT; i++) {
            double dy = 2.0 + (i * 0.4);
            double dx = (i % 3 - 1) * 0.1;
            double dz = (i % 5 - 2) * 0.1;
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    x + dx, y + dy, z + dz, 1,
                    0.0, 0.0, 0.0, 0.0);
        }

        // ── Sounds ──
        // ENDERMAN_TELEPORT: the classic teleport sound.
        serverLevel.playSound(null, pos,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE,
                0.9F, 1.0F);
        // AMETHYST_BLOCK_CHIME: bright crystalline — the bead's release.
        serverLevel.playSound(null, pos,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT,
                0.8F, 1.4F);

        // ── Central flash ──
        serverLevel.sendParticles(ParticleTypes.FIREWORK,
                x, y + 1.0, z, 6,
                0.5, 0.5, 0.5, 0.05);
    }

    /**
     * Spawn the arrival burst at the Kunxu Realm destination. Mirrors the
     * departure burst but uses fewer particles (the arrival is quieter
     * than the departure — the rift closes gently).
     */
    private static void spawnArrivalBurst(ServerLevel serverLevel,
                                            double x, double y, double z) {
        BlockPos pos = BlockPos.containing(x, y, z);

        // ── END_ROD descending ring (soul-stream descending into arrival) ──
        for (int i = 0; i < ARRIVAL_PARTICLE_COUNT; i++) {
            double theta = (i / (double) ARRIVAL_PARTICLE_COUNT) * Math.PI * 2;
            double radius = 0.9;
            double dx = Math.cos(theta) * radius;
            double dz = Math.sin(theta) * radius;
            double dy = 1.0 + ((i % 3) * 0.6);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    x + dx, y + dy, z + dz, 1,
                    0.0, 0.05, 0.0, 0.0);
        }

        // ── DRAGON_BREATH small burst (rift closing) ──
        serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                x, y + 1.0, z, 12,
                0.4, 0.2, 0.4, 0.03);

        // ── Sound: ENDERMAN_TELEPORT (softer) ──
        serverLevel.playSound(null, pos,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE,
                0.6F, 1.2F);
    }

    /**
     * Announce successful departure with a canon-faithful bilingual
     * message. The bilingual format mirrors CRON-110's pattern: Chinese
     * (the novel's original language) first, English second, both styled
     * with light-purple + italic for the sacred/journey tone.
     *
     * <p>The message honors the canon-faithful narrative: Wang Lin
     * escorts 周茹 to the Kunxu Realm and entrusts her to Mu Bingmei's
     * care. The Li Muwan soul within her begins its long cultivation arc.
     */
    private static void announceDepartureSuccess(ServerPlayer player,
                                                   EntityCultivator zhouRu) {
        player.sendSystemMessage(
                Component.literal("─────────────────────────────────────")
                        .withStyle(ChatFormatting.DARK_PURPLE));
        player.sendSystemMessage(
                Component.literal("王林将周茹送往昆虚界，托付于木冰眉门下。")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(
                Component.literal("Wang Lin sends Zhou Ru to the Kunxu Realm, "
                        + "entrusting her to Mu Bingmei's care.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("From this day forth, Zhou Ru walks the path of "
                        + "cultivation under Mu Bingmei's guidance.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("The Li Muwan soul within her stirs — the path of "
                        + "revival stretches onward across millennia.")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("─────────────────────────────────────")
                        .withStyle(ChatFormatting.DARK_PURPLE));
    }

    /**
     * Announce that the player has no bead in main hand. Defensive — the
     * caller (HistoryEvents) gates on the bead presence and dispatches to
     * the transfer handler (CRON-110) when there's no bead. This branch
     * should never fire in normal gameplay.
     */
    private static void announceNoBead(ServerPlayer player) {
        player.sendSystemMessage(
                Component.literal("周茹静静地看着你，什么也没有发生。")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("Zhou Ru watches you quietly. Nothing happens.")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    /**
     * Announce that the soul has not yet been transferred. Defensive — the
     * caller dispatches to the departure handler only when the bead's
     * {@code hasSoulTransferredToZhouRu} flag is true, but the runtime
     * state's {@code pregnant_with_li_muwan_soul} flag may be missing
     * (e.g., world was reset, NPC state was cleared). This branch
     * indicates a state inconsistency that should be investigated.
     */
    private static void announceNoSoulTransferred(ServerPlayer player) {
        player.sendSystemMessage(
                Component.literal("周茹尚未承载李慕婉的元婴——此刻送往昆虚界为时尚早。")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("Zhou Ru has not yet received Li Muwan's soul — "
                        + "it is not yet time to send her to the Kunxu Realm.")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("(State inconsistency: the bead's transfer flag is set "
                        + "but Zhou Ru's runtime state is missing the vessel flag. "
                        + "This may indicate a world-state reset.)")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    /**
     * Announce that 周茹 has already departed for the Kunxu Realm. Canon-
     * faithful safeguard: the departure is a one-time event per save.
     */
    private static void announceAlreadyDeparted(ServerPlayer player) {
        player.sendSystemMessage(
                Component.literal("周茹已启程前往昆虚界，投于木冰眉门下修行。")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("Zhou Ru has already departed for the Kunxu Realm "
                        + "to cultivate under Mu Bingmei.")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("(Travel to the Kunxu Realm at (-3500, -3500) to "
                        + "witness her cultivation arc.)")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    /**
     * Announce that an internal error occurred while fetching 周茹's runtime
     * state. Defensive — should never fire in normal gameplay.
     */
    private static void announceInternalError(ServerPlayer player) {
        player.sendSystemMessage(
                Component.literal("一阵天地异象搅动了周茹的气息——请稍后再试。")
                        .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("A disturbance in the world's aura disrupts Zhou Ru's "
                        + "presence — please try again later.")
                        .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
    }
}
