package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.history.HistoryManager;
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
import net.minecraft.world.entity.Entity;

import java.util.List;

/**
 * Mu Bingmei Acceptance Event — CRON-COMPLETIONIST-113.
 *
 * <p><b>CRON-COMPLETIONIST-115 — CANON CORRECTION:</b> the prior form of
 * this file used incorrect Chinese characters for Mu Bingmei's name
 * (using 慕 "admire" + 媚 "charm" instead of the correct 木 "wood" +
 * 眉 "brow"). The canon-correct characters are <b>木冰眉</b>
 * (wood-ice-brow), verified via Baidu Baike
 * (https://baike.baidu.com/item/木冰眉/8802287). Additionally, 柳眉
 * (Liu Mei) is canonically Mu Bingmei's <b>ninth avatar</b> (第九分身),
 * not merely "a mask she wore in the mortal world". CRON-115 corrects
 * both errors across the codebase. The character_id "mu_bingmei"
 * (romanization) is unchanged — only the Chinese display characters
 * were wrong. This correction mirrors CRON-69's 藤厉 not "Teng Lijun"
 * fix.
 *
 * <p>Implements the canon-faithful "Mu Bingmei takes Zhou Ru as her disciple"
 * narrative beat. After CRON-112's Kunxu departure (周茹 teleported to the
 * Kunxu Realm at (-3500, surface, -3500)), Mu Bingmei senses the soul of
 * Li Muwan within 周茹 and formally accepts her as a disciple. This is the
 * narrative bridge between CRON-112 (arrival) and CRON-111 (cultivation
 * growth, which requires the disciple-master bond to be canonically
 * established).
 *
 * <h2>Canon Basis (fact-checked against RICanonicalDatabase)</h2>
 * <p>In the novel 仙逆 (Renegade Immortal) by 耳根:
 * <ul>
 *   <li><b>N10 (Zhou Ru)</b>: cultivation = "Soul Transformation",
 *       location = "Kunxu Realm (disciple of Mu Bingmei)",
 *       relationships include {@code Mu Bingmei: disciple}.
 *       Traits: "Wang Lin's adopted daughter; vessel for Li Muwan's soul
 *       (intended)", "Li Muwan chose not to devour Zhou Ru's soul; calls
 *       Wang Lin 'uncle'", "Reincarnated on IAC and lives an ordinary life".</li>
 *   <li><b>N19 (Mu Bingmei)</b>: cultivation = "Ascendant+",
 *       relationships include {@code Zhou Ru: disciple}.
 *       Traits: "Liu Mei's true form; Wang Lin's third wife",
 *       "Had a son with Wang Lin (Wang Ping) whom she refined into a
 *       resentful spirit out of hatred",
 *       "Wang Lin severed karmic ties with her via the Dream Dao; one of
 *       his clones accompanies her".</li>
 *   <li><b>L74 (Kunxu Realm)</b>: traits include
 *       "Mu Bingmei entered here; took Zhou Ru as her disciple",
 *       "Zhou Ru's cultivation arc takes place here".</li>
 * </ul>
 *
 * <p><b>Canon honesty:</b> The novel explicitly narrates that Mu Bingmei
 * "took Zhou Ru as her disciple" (L74 + N10 + N19 relationships). This is
 * NOT mod-original — it is direct canon. The exact chapter is not cited
 * here to avoid fabrication. Sources: RICanonicalDatabase N10/N19/L74,
 * Baidu Baike (周茹, 木冰眉), Fandom wiki.
 *
 * <h2>Trigger Mechanism — Automatic Tick Service (NOT interaction-gated)</h2>
 * <p>The acceptance fires AUTOMATICALLY when 周茹 is near Mu Bingmei at the
 * Kunxu Realm — it is a story beat, not a player action. Canon: Wang Lin
 * entrusts 周茹 to Mu Bingmei (CRON-112), and Mu Bingmei accepts. The novel
 * does not narrate a "player clicks Mu Bingmei to make her accept 周茹"
 * mechanic — the acceptance is an autonomous narrative event.
 *
 * <p>This mirrors how a disciple-master relationship actually forms: when
 * the disciple arrives at the master's retreat, the master senses the
 * disciple's potential (in this case, the Li Muwan soul within 周茹) and
 * accepts them. The player can witness the acceptance by traveling to the
 * Kunxu Realm after CRON-112's departure.
 *
 * <h2>Tick Cadence</h2>
 * <p>The service runs every server tick (called from
 * {@link dev.ergenverse.core.Ergenverse#onServerTick} Loop K), but throttles
 * internally to {@link #ACCEPTANCE_CHECK_INTERVAL_TICKS} (200 ticks = 10s).
 * This is responsive enough that the player sees the acceptance promptly
 * after 周茹 arrives, but doesn't waste CPU on a per-tick O(n) entity scan.
 *
 * <h2>Gates (in order)</h2>
 * <ol>
 *   <li><b>Server-side check.</b> Never run on client.</li>
 *   <li><b>Throttle check.</b> Only run every 200 ticks (10s).</li>
 *   <li><b>Find 周茹.</b> Must be materialized (chunk loaded). If not,
 *       no-op — the acceptance can only fire when 周茹 is "on-screen".</li>
 *   <li><b>CRON-112 prerequisite.</b> 周茹's runtime state must have
 *       {@code "sent_to_kunxu": true}. If the departure hasn't fired,
 *       the acceptance cannot fire.</li>
 *   <li><b>Write-once guard.</b> 周茹's runtime state must NOT have
 *       {@code "accepted_as_disciple": true}. The acceptance is a one-time
 *       event per save.</li>
 *   <li><b>Find Mu Bingmei.</b> Must be materialized. If not, no-op —
 *       the acceptance requires the master's presence.</li>
 *   <li><b>Proximity check.</b> 周茹 must be within
 *       {@link #ACCEPTANCE_PROXIMITY_RADIUS} (32 blocks) of Mu Bingmei.
 *       This is TIGHTER than CRON-111's 64-block cultivation proximity —
 *       the formal acceptance is a deliberate ceremony, not ambient
 *       cultivation. 32 blocks = 2 chunks; close enough for a face-to-face
 *       ritual.</li>
 *   <li><b>Find nearby player.</b> Used for the bilingual message + history
 *       record. If no player is within {@link #WITNESS_RADIUS} (48 blocks),
 *       the acceptance STILL fires (sets the flag, spawns effects) but no
 *       message is displayed and no history record is created. This is
 *       canon-faithful: the acceptance happens whether or not the player
 *       witnesses it.</li>
 * </ol>
 *
 * <h2>On Success</h2>
 * <ol>
 *   <li>Sets 周茹's runtime state:
 *     <ul>
 *       <li>{@code accepted_as_disciple: true}</li>
 *       <li>{@code disciple_acceptance_tick: currentTick}</li>
 *       <li>{@code master_character_id: "mu_bingmei"}</li>
 *     </ul>
 *   </li>
 *   <li>Spawns canon-faithful particle + sound effects:
 *     <ul>
 *       <li>At Mu Bingmei (master): END_ROD ring (master's Qi descending)
 *           + AMETHYST_BLOCK_CHIME (crystalline bond).</li>
 *       <li>At 周茹 (disciple): END_ROD ring (disciple receiving Qi)
 *           + AMETHYST_BLOCK_CHIME + PLAYER_LEVELUP (disciple's recognition).</li>
 *       <li>Between them: a stream of END_ROD particles from Mu Bingmei
 *           to 周茹 (the bond visually forming).</li>
 *       <li>A central BELL sound (the formal acceptance — like a temple
 *           bell).</li>
 *     </ul>
 *   </li>
 *   <li>Displays a bilingual message to the witnessing player (if any).</li>
 *   <li>Records the event in HistoryManager (if a witness is present).</li>
 * </ol>
 *
 * <h2>Why a dedicated class (not inline in ZhouRuCultivationGrowthService)</h2>
 * <p>The acceptance is a one-shot narrative event, while the cultivation
 * growth service is a recurring daily tick. Mixing them would couple
 * one-shot semantics with recurring semantics, making the code harder to
 * reason about. A dedicated class also makes the acceptance independently
 * testable and follows the pattern established by CRON-99/110/112 (one
 * class per narrative event).
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
 *   CRON-113: Zhou Ru near Mu Bingmei (automatic tick, every 200 ticks)
 *             →  Zhou Ru runtime: accepted_as_disciple = true
 *             →  Zhou Ru runtime: disciple_acceptance_tick = currentTick
 *             →  Zhou Ru runtime: master_character_id = "mu_bingmei"
 *   CRON-111: Zhou Ru near Mu Bingmei (daily tick) → Zhou Ru realm advances
 *             mortal → qi_condensation → ... → soul_transformation (CANON_CAP)
 *   CRON-100: Player invokes /ergenverse bead revive → revivalAttempts++
 *   CRON-102: 137th attempt with World Origin Essence → bead.isLiMuwanRevived = true
 * </pre>
 *
 * <h2>Single-Player Maximalism (Article XLIII)</h2>
 * <p>The acceptance only fires when both 周茹 and Mu Bingmei are materialized
 * (chunks loaded). In single-player maximalism, this means the player must
 * travel to the Kunxu Realm after CRON-112's departure to witness the
 * acceptance. This is canon-faithful: Wang Lin witnesses 周茹's acceptance
 * under Mu Bingmei. If the player never travels to the Kunxu Realm, the
 * acceptance still fires (when chunks load due to player proximity) but
 * the player won't see the message until they next interact with 周茹's
 * runtime state (e.g., via a future /ergenverse zhou_ru status command).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see ZhouRuKunxuDepartureEvent (predecessor — CRON-112)
 * @see ZhouRuCultivationGrowthService (successor — CRON-111)
 * @see dev.ergenverse.runtime.CanonUUID#ZHOU_RU
 * @see dev.ergenverse.runtime.CanonUUID#MU_BINGMEI
 * @see dev.ergenverse.runtime.PlanetSuzakuBlueprint#KUNXU_REALM
 * @see dev.ergenverse.core.Ergenverse#onServerTick (Loop K)
 */
public final class MuBingmeiAcceptanceEvent {

    /** Canon character ID for Zhou Ru. Must match CanonUUID.ZHOU_RU's profile. */
    public static final String ZHOU_RU_CHARACTER_ID = "zhou_ru";

    /** Canon character ID for Mu Bingmei. Must match CanonUUID.MU_BINGMEI's profile. */
    public static final String MU_BINGMEI_CHARACTER_ID = "mu_bingmei";

    /**
     * Acceptance check interval: 200 ticks = 10 seconds. The service is
     * called every server tick from Loop K, but throttles internally to
     * this interval. 10s is responsive enough that the player sees the
     * acceptance promptly after 周茹 arrives, but doesn't waste CPU on a
     * per-tick O(n) entity scan.
     */
    public static final long ACCEPTANCE_CHECK_INTERVAL_TICKS = 200L;

    /**
     * Maximum distance (in blocks, Euclidean) between 周茹 and Mu Bingmei
     * at which the acceptance can fire. 32 blocks = 2 chunks; close
     * enough for a face-to-face ritual. This is TIGHTER than CRON-111's
     * 64-block cultivation proximity because the formal acceptance is a
     * deliberate ceremony, not ambient cultivation.
     */
    public static final double ACCEPTANCE_PROXIMITY_RADIUS = 32.0;

    /** Squared proximity radius (for distance comparison without sqrt). */
    public static final double ACCEPTANCE_PROXIMITY_RADIUS_SQ =
            ACCEPTANCE_PROXIMITY_RADIUS * ACCEPTANCE_PROXIMITY_RADIUS;

    /**
     * Maximum distance (in blocks, Euclidean) from 周茹 (or Mu Bingmei) at
     * which a player can witness the acceptance. 48 blocks = 3 chunks.
     * If no player is within this radius, the acceptance STILL fires (sets
     * the flag, spawns effects) but no message is displayed and no history
     * record is created. This is canon-faithful: the acceptance happens
     * whether or not the player witnesses it.
     */
    public static final double WITNESS_RADIUS = 48.0;

    /** Number of END_ROD particles in the master's Qi-descending ring. */
    private static final int MASTER_RING_PARTICLE_COUNT = 32;

    /** Number of END_ROD particles in the disciple's Qi-receiving ring. */
    private static final int DISCIPLE_RING_PARTICLE_COUNT = 24;

    /** Number of END_ROD particles in the bond stream (master → disciple). */
    private static final int BOND_STREAM_PARTICLE_COUNT = 20;

    /** Number of FIREWORK particles for the central flash (at midpoint). */
    private static final int CENTRAL_FLASH_PARTICLE_COUNT = 8;

    /** Number of AMETHYST particles for the crystalline bond burst. */
    private static final int CRYSTAL_BURST_PARTICLE_COUNT = 16;

    private MuBingmeiAcceptanceEvent() {}

    /**
     * Run one acceptance-check tick. Called from
     * {@link dev.ergenverse.core.Ergenverse#onServerTick} every server tick
     * (phase=END, Loop K). The method internally gates to
     * {@link #ACCEPTANCE_CHECK_INTERVAL_TICKS} — callers do NOT need to
     * throttle.
     *
     * @param level       the overworld ServerLevel
     * @param currentTick the current server game tick
     */
    public static void tick(ServerLevel level, long currentTick) {
        // Gate 1: throttle to every ACCEPTANCE_CHECK_INTERVAL_TICKS
        if (currentTick % ACCEPTANCE_CHECK_INTERVAL_TICKS != 0) return;
        // Gate 2: server-side only
        if (level == null || level.isClientSide()) return;

        try {
            tickImpl(level, currentTick);
        } catch (Throwable t) {
            // Defensive: never crash the server tick loop.
            Ergenverse.LOGGER.error("[Ergenverse] CRON-113: MuBingmeiAcceptanceEvent "
                    + "threw an exception: {}", t.getMessage(), t);
        }
    }

    private static void tickImpl(ServerLevel level, long currentTick) {
        // ── Gate 3: Find 周茹's materialized EntityCultivator ──
        EntityCultivator zhouRu = findCultivatorByCharacterId(level, ZHOU_RU_CHARACTER_ID);
        if (zhouRu == null) {
            // 周茹 not materialized (chunk not loaded) — no-op.
            return;
        }

        // ── Gate 4: CRON-112 prerequisite — sent_to_kunxu must be true ──
        WorldRuntimeState runtime = WorldRuntimeState.get(level);
        CompoundTag zhouRuState = runtime.getNpcState(ZHOU_RU_CHARACTER_ID);
        if (zhouRuState == null) {
            // No runtime state at all — CRON-110/112 haven't fired. No-op.
            return;
        }
        if (!zhouRuState.getBoolean("sent_to_kunxu")) {
            // CRON-112's departure hasn't fired. No-op.
            return;
        }

        // ── Gate 5: Write-once guard — accepted_as_disciple must be false ──
        if (zhouRuState.getBoolean("accepted_as_disciple")) {
            // Already accepted — one-time event per save. No-op.
            return;
        }

        // ── Gate 6: Find Mu Bingmei's materialized EntityCultivator ──
        EntityCultivator muBingmei = findCultivatorByCharacterId(level, MU_BINGMEI_CHARACTER_ID);
        if (muBingmei == null) {
            // Mu Bingmei not materialized — no-op. The acceptance requires
            // the master's presence.
            return;
        }

        // ── Gate 7: Proximity check — 周茹 within 32 blocks of Mu Bingmei ──
        double distSq = zhouRu.distanceToSqr(muBingmei);
        if (distSq > ACCEPTANCE_PROXIMITY_RADIUS_SQ) {
            // 周茹 too far from Mu Bingmei — no-op. The formal acceptance
            // requires close proximity (a face-to-face ritual).
            return;
        }

        // ── All gates pass — execute the acceptance ──

        // ── Step 1: Set the runtime state flags ──
        zhouRuState.putBoolean("accepted_as_disciple", true);
        zhouRuState.putLong("disciple_acceptance_tick", currentTick);
        zhouRuState.putString("master_character_id", MU_BINGMEI_CHARACTER_ID);
        runtime.updateNpcState(ZHOU_RU_CHARACTER_ID, zhouRuState);

        // ── Step 2: Spawn particle + sound effects ──
        spawnAcceptanceEffects(level, zhouRu, muBingmei);

        // ── Step 3: Find a nearby player to witness ──
        ServerPlayer witness = findNearbyPlayer(level, zhouRu, WITNESS_RADIUS);

        // ── Step 4: Display bilingual message (if witness present) ──
        if (witness != null) {
            announceAcceptance(witness, zhouRu, muBingmei);

            // ── Step 5: Record in HistoryManager ──
            HistoryManager.onDiscovery(witness,
                    "mu_bingmei_accepts_zhou_ru_as_disciple",
                    "Mu Bingmei (木冰眉) formally accepted Zhou Ru (周茹) as her "
                            + "disciple at the Kunxu Realm (昆虚界), teaching her "
                            + "the cultivation methods of the Kunxu Realm. Zhou Ru "
                            + "calls Wang Lin 'uncle' (王林叔叔) — the soul of Li "
                            + "Muwan within her is the bridge between master and "
                            + "disciple. The disciple-master cultivation arc begins.",
                    level.getGameTime());
        }

        Ergenverse.LOGGER.info("[Ergenverse] CRON-113: Mu Bingmei (木冰眉) accepted Zhou Ru "
                        + "(周茹) as her disciple at tick {}. Position: Zhou Ru={}, "
                        + "Mu Bingmei={}, distance={:.1f} blocks. Witness: {}.",
                currentTick,
                zhouRu.blockPosition(),
                muBingmei.blockPosition(),
                Math.sqrt(distSq),
                witness != null ? witness.getName().getString() : "(none — silent acceptance)");
    }

    /**
     * Find a materialized EntityCultivator by characterId. Iterates the
     * level's all-entities list (O(n) where n is the loaded entity count).
     * Mirrors {@link ZhouRuCultivationGrowthService#findCultivatorByCharacterId}.
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
     * Find the nearest ServerPlayer within {@code radius} blocks of the
     * given entity. Used to find a witness for the acceptance event
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
     * Spawn canon-faithful particle + sound effects for the acceptance.
     * The visual idiom:
     * <ul>
     *   <li><b>END_ROD ring at Mu Bingmei (master)</b> — the master's Qi
     *       descending. END_ROD is bright white-rising.</li>
     *   <li><b>END_ROD ring at 周茹 (disciple)</b> — the disciple receiving
     *       the master's Qi.</li>
     *   <li><b>END_ROD stream from Mu Bingmei to 周茹</b> — the disciple-
     *       master bond forming visually.</li>
     *   <li><b>FIREWORK central flash at the midpoint</b> — the formal
     *       acceptance burst.</li>
     *   <li><b>AMETHYST burst at both ends</b> — the crystalline bond.</li>
     *   <li><b>AMETHYST_BLOCK_CHIME sound at both ends</b> — the crystalline
     *       tone of the bond.</li>
     *   <li><b>PLAYER_LEVELUP sound at 周茹</b> — the bright ascending tone
     *       of the disciple's recognition.</li>
     *   <li><b>BELL sound at the midpoint</b> — the formal acceptance
     *       (like a temple bell).</li>
     * </ul>
     *
     * <p>Deliberately AVOIDS:
     * <ul>
     *   <li>ENDER_DRAGON_GROWL — too apocalyptic for a disciple's acceptance.</li>
     *   <li>WITHER_SPAWN — too dark; Mu Bingmei's relationship with Wang Lin
     *       is complex (she refined their son Wang Ping into a resentful
     *       spirit), but the acceptance itself is a positive narrative beat.</li>
     *   <li>DRAGON_BREATH particles — too aggressive; this is a sacred bond,
     *       not a combat event.</li>
     * </ul>
     *
     * @param level     the server level
     * @param zhouRu    the disciple EntityCultivator (周茹)
     * @param muBingmei the master EntityCultivator (木冰眉)
     */
    private static void spawnAcceptanceEffects(ServerLevel level,
                                                EntityCultivator zhouRu,
                                                EntityCultivator muBingmei) {
        double masterX = muBingmei.getX();
        double masterY = muBingmei.getY() + 1.0;  // chest height
        double masterZ = muBingmei.getZ();

        double discipleX = zhouRu.getX();
        double discipleY = zhouRu.getY() + 1.0;  // chest height
        double discipleZ = zhouRu.getZ();

        double midX = (masterX + discipleX) / 2.0;
        double midY = (masterY + discipleY) / 2.0;
        double midZ = (masterZ + discipleZ) / 2.0;

        // ── Master's Qi-descending ring (at Mu Bingmei) ──
        for (int i = 0; i < MASTER_RING_PARTICLE_COUNT; i++) {
            double theta = (i / (double) MASTER_RING_PARTICLE_COUNT) * Math.PI * 2;
            double radius = 1.2;
            double dx = Math.cos(theta) * radius;
            double dz = Math.sin(theta) * radius;
            // Descending from 3 blocks above head to chest height
            double dy = 2.0 - ((i % 4) * 0.4);
            level.sendParticles(ParticleTypes.END_ROD,
                    masterX + dx, muBingmei.getY() + dy, masterZ + dz, 1,
                    0.0, -0.05, 0.0, 0.0);
        }

        // ── Disciple's Qi-receiving ring (at 周茹) ──
        for (int i = 0; i < DISCIPLE_RING_PARTICLE_COUNT; i++) {
            double theta = (i / (double) DISCIPLE_RING_PARTICLE_COUNT) * Math.PI * 2;
            double radius = 0.9;
            double dx = Math.cos(theta) * radius;
            double dz = Math.sin(theta) * radius;
            // Ascending from feet to chest (receiving Qi upward)
            double dy = 0.5 + ((i % 3) * 0.5);
            level.sendParticles(ParticleTypes.END_ROD,
                    discipleX + dx, zhouRu.getY() + dy, discipleZ + dz, 1,
                    0.0, 0.05, 0.0, 0.0);
        }

        // ── Bond stream (Mu Bingmei → 周茹) ──
        // A series of END_ROD particles along the line from master to disciple,
        // simulating the Qi flowing from master to disciple.
        for (int i = 0; i < BOND_STREAM_PARTICLE_COUNT; i++) {
            double t = i / (double) (BOND_STREAM_PARTICLE_COUNT - 1);
            double px = masterX + (discipleX - masterX) * t;
            double py = masterY + (discipleY - masterY) * t;
            double pz = masterZ + (discipleZ - masterZ) * t;
            // Small lateral jitter for visual richness
            double jitterX = (i % 3 - 1) * 0.1;
            double jitterZ = (i % 5 - 2) * 0.1;
            level.sendParticles(ParticleTypes.END_ROD,
                    px + jitterX, py, pz + jitterZ, 1,
                    0.0, 0.0, 0.0, 0.0);
        }

        // ── AMETHYST crystalline bond burst (at both ends) ──
        level.sendParticles(ParticleTypes.END_ROD,
                masterX, masterY, masterZ, CRYSTAL_BURST_PARTICLE_COUNT,
                0.5, 0.5, 0.5, 0.05);
        level.sendParticles(ParticleTypes.END_ROD,
                discipleX, discipleY, discipleZ, CRYSTAL_BURST_PARTICLE_COUNT,
                0.5, 0.5, 0.5, 0.05);

        // ── Central FIREWORK flash at midpoint ──
        level.sendParticles(ParticleTypes.FIREWORK,
                midX, midY, midZ, CENTRAL_FLASH_PARTICLE_COUNT,
                0.4, 0.4, 0.4, 0.05);

        // ── Sounds ──
        // AMETHYST_BLOCK_CHIME at both ends (crystalline bond)
        level.playSound(null, muBingmei.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT,
                0.8F, 1.2F);  // volume 0.8, pitch 1.2 (crystalline)
        level.playSound(null, zhouRu.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT,
                0.8F, 1.4F);  // volume 0.8, pitch 1.4 (slightly brighter at disciple)

        // PLAYER_LEVELUP at 周茹 (bright ascending — the disciple's recognition)
        level.playSound(null, zhouRu.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT,
                0.6F, 1.3F);  // volume 0.6, pitch 1.3

        // BELL at midpoint (formal acceptance — like a temple bell)
        level.playSound(null, BlockPos.containing(midX, midY, midZ),
                SoundEvents.BELL_BLOCK, SoundSource.AMBIENT,
                0.9F, 1.0F);  // volume 0.9, pitch 1.0 (clear bell tone)
    }

    /**
     * Announce the acceptance with a canon-faithful bilingual message.
     * The bilingual format mirrors CRON-110/111/112's pattern: Chinese
     * (the novel's original language) first, English second, both styled
     * with light-purple + italic for the sacred/journey tone.
     *
     * <p>The message honors:
     * <ul>
     *   <li>The disciple-master relationship (周茹 under Mu Bingmei).</li>
     *   <li>The Li Muwan soul arc (the soul within 周茹 is the bridge).</li>
     *   <li>The 王林叔叔 (uncle) canon detail (周茹 calls Wang Lin uncle).</li>
     *   <li>The Kunxu Realm setting (the disciple-master arc takes place here).</li>
     * </ul>
     *
     * @param player    the witnessing player
     * @param zhouRu    the disciple EntityCultivator (周茹)
     * @param muBingmei the master EntityCultivator (木冰眉)
     */
    private static void announceAcceptance(ServerPlayer player,
                                            EntityCultivator zhouRu,
                                            EntityCultivator muBingmei) {
        player.sendSystemMessage(
                Component.literal("─────────────────────────────────────")
                        .withStyle(ChatFormatting.DARK_PURPLE));
        player.sendSystemMessage(
                Component.literal("木冰眉收周茹为徒，传以昆虚界修行之法。")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(
                Component.literal("Mu Bingmei takes Zhou Ru as her disciple, "
                        + "teaching her the cultivation methods of the Kunxu Realm.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("Mu Bingmei senses the soul of Li Muwan within Zhou Ru — "
                        + "the master's recognition bridges two lifetimes.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("Zhou Ru calls Wang Lin 'uncle' (王林叔叔) — the bond of "
                        + "family and the path of cultivation now intertwine.")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(
                Component.literal("─────────────────────────────────────")
                        .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
