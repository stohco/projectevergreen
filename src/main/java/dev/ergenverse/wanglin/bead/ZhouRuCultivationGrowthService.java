package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.history.HistoryManager;
import dev.ergenverse.simulation.WorldRuntimeState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.List;

/**
 * Zhou Ru Cultivation Growth Service — CRON-COMPLETIONIST-111.
 *
 * <p>Implements the canon-faithful post-transfer cultivation arc: after
 * CRON-110's soul transfer (Li Muwan's soul placed into 周茹), 周茹 grows
 * up to become a Soul Transformation cultivator under 慕冰梅 (Mu Bingmei)
 * in the Kunxu Realm (昆虚界). This service advances 周茹's cultivation
 * realm over time when she is near Mu Bingmei — modeling the
 * disciple-master cultivation relationship.
 *
 * <h2>Canon Basis (fact-checked via web-search 2026-07-26)</h2>
 * <p>In the novel 仙逆 (Renegade Immortal) by 耳根:
 * <ul>
 *   <li>After Wang Lin places Li Muwan's soul into 周茹 (CRON-110), 周茹
 *       eventually becomes a Soul Transformation (炼虚) cultivator.</li>
 *   <li>慕冰媚 (Mu Bingmei, also known as 柳眉 Liu Mei) — Wang Lin's third
 *       wife — enters the Kunxu Realm (昆虚界) and takes 周茹 as her disciple
 *       (RICanonicalDatabase N19 + L74).</li>
 *   <li>周茹's cultivation arc (mortal → Soul Transformation) takes place
 *       in the Kunxu Realm under Mu Bingmei's guidance.</li>
 *   <li>Later, 周茹 is reincarnated on the Immortal Astral Continent (IAC
 *       arc) where she lives an ordinary life.</li>
 * </ul>
 *
 * <p><b>Canon honesty:</b> The novel clearly establishes (1) 周茹's
 * cultivation to Soul Transformation, (2) Mu Bingmei as her master in the
 * Kunxu Realm, and (3) the disciple-master relationship. The exact chapter
 * is not cited here to avoid fabrication. Sources: RICanonicalDatabase
 * N19 (Mu Bingmei), L74 (Kunxu Realm), N10 (Zhou Ru), Baidu Baike, Fandom
 * wiki.
 *
 * <h2>Mechanism</h2>
 * <p>This service runs every {@link #GROWTH_INTERVAL_TICKS} (1 MC day =
 * 24000 ticks). On each run, it:
 * <ol>
 *   <li>Finds 周茹's materialized EntityCultivator (if any). If 周茹 is
 *       not materialized (chunk not loaded), no-op — the growth only
 *       happens when 周茹 is "on-screen" (single-player maximalism: the
 *       simulation is active where the player is).</li>
 *   <li>Checks 周茹's runtime state for the
 *       {@code "pregnant_with_li_muwan_soul"} flag (set by CRON-110's
 *       ZhouRuSoulTransferEvent). If the flag is absent, no-op — the
 *       cultivation growth only happens AFTER the soul transfer.</li>
 *   <li><b>CRON-114 gate:</b> Checks 周茹's runtime state for the
 *       {@code "accepted_as_disciple"} flag (set by CRON-113's
 *       MuBingmeiAcceptanceEvent). If the flag is absent, no-op — the
 *       cultivation growth only happens AFTER Mu Bingmei has formally
 *       accepted 周茹 as her disciple. This enforces strict narrative
 *       order: transfer (CRON-110) → depart (CRON-112) → accept
 *       (CRON-113) → cultivate (CRON-111). Before CRON-114, this gate
 *       was missing — CRON-111 could theoretically advance 周茹's realm
 *       before the acceptance fired (if 周茹 wandered within 64 blocks
 *       of Mu Bingmei but not yet within 32 blocks for the acceptance).
 *       In practice the throttle differential (24000 vs 200 ticks)
 *       made this unlikely, but CRON-114 closes the gap for correctness.</li>
 *   <li>Finds Mu Bingmei's materialized EntityCultivator (if any). If
 *       Mu Bingmei is not materialized, no-op — the cultivation growth
 *       requires the master's presence.</li>
 *   <li>Checks the distance between 周茹 and Mu Bingmei. If greater than
 *       {@link #PROXIMITY_RADIUS} blocks, no-op — the disciple-master
 *       cultivation requires proximity.</li>
 *   <li>Checks 周茹's current cultivation realm. If already at or above
 *       {@link #CANON_CAP} (SOUL_TRANSFORMATION), no-op — 周茹 has
 *       reached her canon-attested cultivation level.</li>
 *   <li>If all checks pass: advances 周茹's cultivation realm by one step,
 *       spawns canon-faithful particle/sound effects, displays a bilingual
 *       message (if a player is nearby to witness it), and records the
 *       breakthrough in HistoryManager.</li>
 * </ol>
 *
 * <h2>Why a dedicated service (not inline in Ergenverse.onServerTick)</h2>
 * <p>The {@link dev.ergenverse.core.Ergenverse#onServerTick} method is
 * already a large dispatcher (Loops A-I). Inlining the Zhou Ru cultivation
 * growth logic there would couple canon-character-specific logic to the
 * general server-tick infrastructure. A dedicated static-utility class
 * keeps the tick dispatcher clean and makes the growth logic independently
 * testable. The class lives in the {@code wanglin.bead} package because
 * it's part of the Li Muwan revival arc (CRON-99 capture → CRON-110
 * transfer → CRON-111 cultivation growth → CRON-100 revival attempts →
 * CRON-102 final revival).
 *
 * <h2>Single-Player Maximalism (Article XLIII)</h2>
 * <p>The growth only fires when both 周茹 and Mu Bingmei are materialized
 * (chunks loaded). In single-player maximalism, this means the player
 * must be near both NPCs for the cultivation to advance. This is
 * canon-faithful: Wang Lin witnesses 周茹's growth under Mu Bingmei's
 * guidance. The growth rate is 1 realm per MC day — fast enough to be
 * observable in gameplay, slow enough to feel like a cultivation arc
 * (6 days from mortal to Soul Transformation).
 *
 * <h2>State Transition Diagram</h2>
 * <pre>
 *   CRON-99:  Li Muwan dies → bead.hasLiMuwanSoul = true
 *   CRON-110: Player interacts with Zhou Ru → bead.hasSoulTransferredToZhouRu = true
 *                                            → Zhou Ru runtime: pregnant_with_li_muwan_soul = true
 *   CRON-112: Player interacts with Zhou Ru (2nd right-click) → Zhou Ru teleports to KUNXU_REALM
 *                                            → Zhou Ru runtime: sent_to_kunxu = true
 *   CRON-113: Zhou Ru near Mu Bingmei (automatic tick, every 200 ticks) → accepted_as_disciple = true
 *   CRON-111: Zhou Ru near Mu Bingmei (daily tick, REQUIRES accepted_as_disciple since CRON-114)
 *            → Zhou Ru realm advances:
 *            mortal → qi_condensation → foundation → core_formation
 *            → nascent_soul → soul_formation → soul_transformation (CANON_CAP)
 *   CRON-100: Player invokes /ergenverse bead revive → revivalAttempts++
 *   CRON-102: 137th attempt with World Origin Essence → bead.isLiMuwanRevived = true
 * </pre>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see ZhouRuSoulTransferEvent (predecessor — CRON-110)
 * @see ZhouRuKunxuDepartureEvent (predecessor — CRON-112)
 * @see MuBingmeiAcceptanceEvent (predecessor — CRON-113, sets the accepted_as_disciple gate)
 * @see dev.ergenverse.runtime.CanonUUID#ZHOU_RU
 * @see dev.ergenverse.runtime.CanonUUID#MU_BINGMEI
 * @see dev.ergenverse.runtime.PlanetSuzakuBlueprint#KUNXU_REALM
 * @see dev.ergenverse.cultivation.RealmId
 */
public final class ZhouRuCultivationGrowthService {

    /** Canon character ID for Zhou Ru. Must match CanonUUID.ZHOU_RU's profile. */
    public static final String ZHOU_RU_CHARACTER_ID = "zhou_ru";

    /** Canon character ID for Mu Bingmei. Must match CanonUUID.MU_BINGMEI's profile. */
    public static final String MU_BINGMEI_CHARACTER_ID = "mu_bingmei";

    /**
     * Growth interval: 1 MC day = 24000 ticks. On each interval, 周茹's
     * realm advances by one step (if near Mu Bingmei). This means the
     * full arc (mortal → soul_transformation) takes 6 MC days — fast
     * enough to be observable, slow enough to feel like a cultivation arc.
     */
    public static final long GROWTH_INTERVAL_TICKS = 24000L;

    /**
     * Maximum distance (in blocks, Euclidean) between 周茹 and Mu Bingmei
     * at which the cultivation growth can fire. 64 blocks = 4 chunks;
     * this is wide enough to cover a typical cultivation retreat but
     * narrow enough that 周茹 must be actively near Mu Bingmei.
     */
    public static final double PROXIMITY_RADIUS = 64.0;

    /** Squared proximity radius (for distance comparison without sqrt). */
    public static final double PROXIMITY_RADIUS_SQ = PROXIMITY_RADIUS * PROXIMITY_RADIUS;

    /**
     * Canon-attested cultivation cap for 周茹: Soul Transformation (炼虚).
     * Per RICanonicalDatabase N10: 周茹's cultivation is "Soul Transformation".
     * The growth service advances 周茹's realm up to but not beyond this cap.
     */
    public static final RealmId CANON_CAP = RealmId.SOUL_TRANSFORMATION;

    /** Number of END_ROD particles for the breakthrough burst. */
    private static final int BREAKTHROUGH_PARTICLE_COUNT = 30;

    /** Number of SQUID_INK particles for the cultivation-Qi residue. */
    private static final int QI_PARTICLE_COUNT = 15;

    private ZhouRuCultivationGrowthService() {}

    /**
     * Run one cultivation-growth tick. Called from
     * {@link dev.ergenverse.core.Ergenverse#onServerTick} every server tick
     * (phase=END). The method internally gates to
     * {@link #GROWTH_INTERVAL_TICKS} — callers do NOT need to throttle.
     *
     * @param level       the overworld ServerLevel
     * @param currentTick the current server game tick
     */
    public static void tick(ServerLevel level, long currentTick) {
        // Gate: only run every GROWTH_INTERVAL_TICKS
        if (currentTick % GROWTH_INTERVAL_TICKS != 0) return;
        if (level == null || level.isClientSide()) return;

        try {
            tickImpl(level, currentTick);
        } catch (Throwable t) {
            // Defensive: never crash the server tick loop.
            Ergenverse.LOGGER.error("[Ergenverse] CRON-111: ZhouRuCultivationGrowthService "
                    + "threw an exception: {}", t.getMessage(), t);
        }
    }

    private static void tickImpl(ServerLevel level, long currentTick) {
        // ── 1. Find 周茹's materialized EntityCultivator ──
        EntityCultivator zhouRu = findCultivatorByCharacterId(level, ZHOU_RU_CHARACTER_ID);
        if (zhouRu == null) {
            // 周茹 not materialized (chunk not loaded) — no-op.
            return;
        }

        // ── 2. Check 周茹's runtime state for the soul-transferred flag (CRON-110) ──
        WorldRuntimeState runtime = WorldRuntimeState.get(level);
        CompoundTag zhouRuState = runtime.getNpcState(ZHOU_RU_CHARACTER_ID);
        if (zhouRuState == null || !zhouRuState.getBoolean("pregnant_with_li_muwan_soul")) {
            // Soul transfer (CRON-110) has not yet fired — no-op.
            return;
        }

        // ── 3. CRON-114 gate: Check 周茹's runtime state for the accepted_as_disciple flag (CRON-113) ──
        // Before CRON-114, this gate was missing. CRON-111 could theoretically
        // advance 周茹's realm before CRON-113's acceptance fired (if 周茹
        // wandered within 64 blocks of Mu Bingmei but not yet within 32 blocks
        // for the acceptance). In practice the throttle differential
        // (24000 vs 200 ticks) made this unlikely, but CRON-114 closes the
        // gap for correctness. The strict narrative order is now:
        //   transfer (CRON-110) → depart (CRON-112) → accept (CRON-113) → cultivate (CRON-111)
        if (!zhouRuState.getBoolean("accepted_as_disciple")) {
            // Mu Bingmei has not yet formally accepted 周茹 as her disciple — no-op.
            // The cultivation growth only begins AFTER the disciple-master bond
            // is canonically established (CRON-113).
            return;
        }

        // ── 4. Find Mu Bingmei's materialized EntityCultivator ──
        EntityCultivator muBingmei = findCultivatorByCharacterId(level, MU_BINGMEI_CHARACTER_ID);
        if (muBingmei == null) {
            // Mu Bingmei not materialized — no-op. The cultivation growth
            // requires the master's presence.
            return;
        }

        // ── 5. Check proximity (周茹 must be near Mu Bingmei) ──
        double distSq = zhouRu.distanceToSqr(muBingmei);
        if (distSq > PROXIMITY_RADIUS_SQ) {
            // 周茹 too far from Mu Bingmei — no-op.
            return;
        }

        // ── 6. Check 周茹's current cultivation realm ──
        String currentRealmStr = zhouRu.getCultivationRealm();
        RealmId currentRealm = parseRealmId(currentRealmStr);
        if (currentRealm == null) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-111: Zhou Ru has unparseable realm "
                    + "'{}'. Expected a RealmId name (lowercase). No-op.", currentRealmStr);
            return;
        }

        // ── 7. Check canon cap ──
        if (currentRealm.order >= CANON_CAP.order) {
            // 周茹 has reached the canon-attested cultivation cap (Soul Transformation).
            // No further growth.
            return;
        }

        // ── 8. All checks pass — advance 周茹's cultivation realm ──
        RealmId nextRealm = currentRealm.next();
        if (nextRealm == null) {
            // Already at the top of the ladder — defensive (shouldn't happen
            // since CANON_CAP check above would have caught it).
            return;
        }

        // Update 周茹's entity cultivation realm.
        zhouRu.setCultivationRealm(nextRealm.name().toLowerCase());

        // ── 9. Update 周茹's persistent runtime state ──
        zhouRuState.putInt("cultivation_realm_order", nextRealm.order);
        zhouRuState.putString("cultivation_realm_name", nextRealm.name());
        zhouRuState.putString("cultivation_realm_name_cn", nextRealm.nameCn);
        zhouRuState.putLong("last_breakthrough_tick", currentTick);
        zhouRuState.putBoolean("under_mu_bingmei_guidance", true);
        runtime.updateNpcState(ZHOU_RU_CHARACTER_ID, zhouRuState);

        // ── 10. Spawn breakthrough particle + sound effects at 周茹 ──
        spawnBreakthroughEffects(level, zhouRu, nextRealm);

        // ── 11. Display bilingual message to nearby players (if any) ──
        ServerPlayer nearbyPlayer = findNearbyPlayer(level, zhouRu, 32.0);
        if (nearbyPlayer != null) {
            announceBreakthrough(nearbyPlayer, zhouRu, nextRealm);

            // ── 12. Record in HistoryManager ──
            HistoryManager.onDiscovery(nearbyPlayer,
                    "zhou_ru_cultivation_breakthrough",
                    "Zhou Ru (周茹) broke through to " + nextRealm.name
                            + " (" + nextRealm.nameCn + ") under Mu Bingmei's guidance "
                            + "in the Kunxu Realm.",
                    level.getGameTime());
        }

        Ergenverse.LOGGER.info("[Ergenverse] CRON-111: Zhou Ru (周茹) broke through to "
                        + "{} ({}) at tick {} under Mu Bingmei's guidance. Position: {}. "
                        + "Distance to master: {:.1f} blocks.",
                nextRealm.name, nextRealm.nameCn, currentTick,
                zhouRu.blockPosition(), Math.sqrt(distSq));
    }

    /**
     * Find a materialized EntityCultivator by characterId. Iterates the
     * level's all-entities list (O(n) where n is the loaded entity count).
     * This is the same pattern used by CanonActorMaterializer.findEntityByUuid
     * and LiMuwanRevivalEvent.
     *
     * @param level       the server level
     * @param characterId the canon character id (e.g. "zhou_ru", "mu_bingmei")
     * @return the EntityCultivator, or null if not found / not materialized
     */
    private static EntityCultivator findCultivatorByCharacterId(ServerLevel level,
                                                                  String characterId) {
        for (Entity e : level.getAllEntities()) {
            if (!(e instanceof EntityCultivator cultivator)) continue;
            if (!e.isAlive()) continue;
            if (characterId.equals(cultivator.getCharacterId())) {
                return cultivator;
            }
        }
        return null;
    }

    /**
     * Parse a realm string (lowercase, e.g. "mortal", "qi_condensation")
     * into a RealmId. Returns null if the string doesn't match any RealmId.
     *
     * @param realmStr the realm string (case-insensitive)
     * @return the RealmId, or null if unparseable
     */
    private static RealmId parseRealmId(String realmStr) {
        if (realmStr == null || realmStr.isEmpty()) return null;
        try {
            return RealmId.valueOf(realmStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Find the nearest ServerPlayer within {@code radius} blocks of the
     * given entity. Used to find a witness for the breakthrough event
     * (for the bilingual message + HistoryManager recording).
     *
     * <p>In single-player maximalism, there is exactly one player. This
     * method is defensive for future MP support.
     *
     * @param level  the server level
     * @param entity the entity to search around
     * @param radius the search radius (blocks, Euclidean)
     * @return the nearest ServerPlayer, or null if none in radius
     */
    private static ServerPlayer findNearbyPlayer(ServerLevel level,
                                                   EntityCultivator entity,
                                                   double radius) {
        List<ServerPlayer> players = level.players();
        ServerPlayer nearest = null;
        double nearestDistSq = radius * radius;
        for (ServerPlayer p : players) {
            double dSq = p.distanceToSqr(entity);
            if (dSq <= nearestDistSq) {
                nearestDistSq = dSq;
                nearest = p;
            }
        }
        return nearest;
    }

    /**
     * Spawn canon-faithful particle + sound effects at 周茹's location for
     * a cultivation breakthrough.
     *
     * <p>The visual idiom:
     * <ul>
     *   <li><b>END_ROD particles in an ascending ring around 周茹</b> —
     *       the cultivation Qi rising. END_ROD is bright white-rising,
     *       matching the cultivation-breakthrough visual idiom.</li>
     *   <li><b>SQUID_INK particles in a small burst at 周茹's feet</b> —
     *       the impurities expelled during breakthrough. SQUID_INK is
     *       dark-purple-black, matching the cultivation-residue idiom.</li>
     *   <li><b>PLAYER_LEVELUP sound at high pitch</b> — the bright
     *       ascending tone of a breakthrough. AVOIDS ENDER_DRAGON_GROWL
     *       (too apocalyptic for a disciple's breakthrough).</li>
     *   <li><b>AMETHYST_BLOCK_CHIME sound at high pitch</b> — the
     *       crystalline tone of the Qi crystallizing into the new realm.</li>
     * </ul>
     *
     * @param level     the server level
     * @param zhouRu    the EntityCultivator (周茹)
     * @param newRealm  the newly-achieved realm
     */
    private static void spawnBreakthroughEffects(ServerLevel level,
                                                   EntityCultivator zhouRu,
                                                   RealmId newRealm) {
        double centerX = zhouRu.getX();
        double centerY = zhouRu.getY() + 1.0;  // chest height
        double centerZ = zhouRu.getZ();

        // ── Ascending ring of END_ROD around 周茹 ──
        for (int i = 0; i < BREAKTHROUGH_PARTICLE_COUNT; i++) {
            double theta = (i / (double) BREAKTHROUGH_PARTICLE_COUNT) * Math.PI * 2;
            double radius = 1.0;
            double dx = Math.cos(theta) * radius;
            double dz = Math.sin(theta) * radius;
            // Distribute heights from chest (1.0) to 3 blocks above feet (3.0)
            double dy = 1.0 + ((i % 4) * 0.5);
            level.sendParticles(ParticleTypes.END_ROD,
                    centerX + dx, centerY + dy, centerZ + dz, 1,
                    0.0, 0.05, 0.0, 0.0);
        }

        // ── SQUID_INK burst at 周茹's feet (impurities expelled) ──
        level.sendParticles(ParticleTypes.SQUID_INK,
                centerX, zhouRu.getY() + 0.1, centerZ, QI_PARTICLE_COUNT,
                0.5, 0.1, 0.5, 0.02);

        // ── Sound: PLAYER_LEVELUP (bright) + AMETHYST_BLOCK_CHIME (crystalline) ──
        level.playSound(null, zhouRu.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT,
                0.6F, 1.5F);  // volume 0.6, pitch 1.5 (bright ascending)
        level.playSound(null, zhouRu.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT,
                0.7F, 1.3F);  // volume 0.7, pitch 1.3 (crystalline)

        // ── Central flash (breakthrough burst) ──
        level.sendParticles(ParticleTypes.FIREWORK,
                centerX, centerY, centerZ, 6,
                0.3, 0.3, 0.3, 0.05);
    }

    /**
     * Announce 周茹's breakthrough with a canon-faithful bilingual message.
     * The bilingual format mirrors CRON-99/110's pattern: Chinese (the
     * novel's original language) first, English second, both styled with
     * light-purple + italic for the cultivation-breakthrough tone.
     *
     * <p>The message honors the disciple-master relationship (周茹 under
     * Mu Bingmei's guidance) — a canon-attested detail from RICanonicalDatabase.
     *
     * @param player   the witnessing player
     * @param zhouRu   the EntityCultivator (周茹)
     * @param newRealm the newly-achieved realm
     */
    private static void announceBreakthrough(ServerPlayer player,
                                              EntityCultivator zhouRu,
                                              RealmId newRealm) {
        player.sendSystemMessage(
                Component.literal("─────────────────────────────────────")
                        .withStyle(ChatFormatting.DARK_PURPLE));
        player.sendSystemMessage(
                Component.literal("周茹在慕冰媚的指点下突破至 " + newRealm.nameCn + " 境。")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(
                Component.literal("Zhou Ru broke through to " + newRealm.name
                        + " (" + newRealm.nameCn + ") under Mu Bingmei's guidance.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("The Li Muwan soul within her stirs. The cultivation arc continues.")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("─────────────────────────────────────")
                        .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
