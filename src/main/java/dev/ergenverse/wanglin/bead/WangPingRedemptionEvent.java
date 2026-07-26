package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.history.HistoryManager;
import dev.ergenverse.runtime.CanonUUID;
import dev.ergenverse.runtime.NPCRuntime;
import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.runtime.delta.WorldDeltaStore;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Wang Ping Redemption Event — CRON-COMPLETIONIST-117.
 *
 * <p>Implements the canon-faithful "Wang Lin rebuilds Wang Ping's body from
 * sword qi" narrative beat — the redemption of Wang Ping (王平), Wang Lin's
 * biological son by 木冰眉 / 柳眉 (Mu Bingmei's ninth avatar Liu Mei).
 *
 * <p>CRON-116 registered Wang Ping as a canon NPC at the Suzaku Tomb
 * (his conception site) with {@code deadUntilRevived=true} — meaning
 * CanonActorMaterializer refused to spawn him on chunk load. CRON-117
 * closes this gap by clearing the flag and materializing him as a
 * mortal boy when the player (Wang Lin) triggers the redemption event.
 *
 * <h2>Canon Basis (fact-checked via web-search 2026-07-27)</h2>
 * <p>In the novel 仙逆 by 耳根, after Wang Lin slays Liu Mei and reclaims
 * Wang Ping's remnant soul (残魂) — which had been refined into a 怨婴
 * (resentment infant) by Liu Mei for ~100 years — Wang Lin rebuilds
 * Wang Ping's body using:
 *
 * <ul>
 *   <li><b>Two strands of sword qi (两道剑气)</b> from <b>凌天侯 (Ling Tianhou)</b>,
 *       the Sword Venerable (剑尊) and founder of the 大罗剑宗 (Da Luo Sword
 *       Sect). Ling Tianhou is at 净涅后期 (Quiet Nirvana late stage); his
 *       true identity is a clone/servant of 灭生老人 (Mie Sheng Lao Ren),
 *       a Fourth-Step cultivator of the Ni Chen Realm. Ling Tianhou
 *       <b>personally</b> gave the sword qi to Wang Lin (canon: Sohu
 *       https://www.sohu.com/a/849321229_568249) — not via an intermediary
 *       disciple. (The earlier CRON-117 note that it was "originally a
 *       life-saving treasure given by Ling Tianhou to his disciple" was
 *       an UNVERIFIED claim from a research subagent and is now retracted;
 *       the canon-faithful path is direct grant from Ling Tianhou to
 *       Wang Lin, modeled in {@link LingTianhouSwordQiGrantEvent}.)</li>
 *   <li><b>One strand condensed into flesh; the other guarded his soul.</b>
 *       CRON-122 canon verification: Baidu Baike 王平 entry (PRIMARY,
 *       https://baike.baidu.com/item/王平/62563845) — "一道化作王平的血肉之躯，
 *       另一道守护其魂魄". Multiple secondary sources (Sohu, Zhihu, 163.com)
 *       confirm. The result is a <b>"False Life" (虚假生命)</b>:
 *       outwardly human (handsome features, pure eyes, "no different from
 *       an ordinary person"), but inwardly a sword-qi construct that
 *       <b>cannot cry, cannot sire children, has no cultivation talent,
 *       and cannot sense spiritual qi</b>.</li>
 * </ul>
 *
 * <p><b>Canon chapter citations</b> (Baidu Baike-attested):
 * <ul>
 *   <li>Vol 7 Ch 680 《柳眉的特殊法宝》 — Wang Ping first appears as "厉儿
 *       (Li'er)", Liu Mei deploys the resentment infant against Wang Lin.</li>
 *   <li>Vol 7 Ch 681 — Wang Lin names him 王平 (Wang Ping).</li>
 *   <li>Ch 695 《王平的要求》 — references the Sun family on Ranyun Star.</li>
 *   <li>Ch 700 《惊变》 / Ch 701 《修为》 — arc beats.</li>
 * </ul>
 *
 * <p>Sources: Baidu Baike (https://baike.baidu.com/item/王平/62563845),
 * Sohu (https://www.sohu.com/a/1015521494_122415633),
 * Baidu Baike for 凌天侯 (https://baike.baidu.com/item/凌天侯/65285935),
 * Fandom wiki (https://xian-ni.fandom.com/wiki/Wang_Ping).
 *
 * <h2>Canon-Faithful Location Note (mod-original condensation)</h2>
 * <p>In canon, Wang Ping's body reconstruction happens on <b>冉云星
 * (Ranyun Star)</b> in the <b>罗天星域 (Luo Tian Star Domain)</b>, NOT
 * at the Suzaku Tomb. Suzaku Tomb is only Wang Ping's <b>conception</b>
 * site (Ch 443-450+, Wang Lin's union with Liu Mei under Mei Ji's
 * aphrodisiac influence). The 100-year refining into 怨婴 by Liu Mei
 * and the later sword-qi reconstruction both occur off-world, on
 * Ranyun Star, during Wang Lin's 问鼎 (Ascendant) stage.
 *
 * <p><b>Mod-original condensation</b>: this event triggers at the
 * Cultivation Planet Crystal at the Suzaku Tomb (the conception site),
 * not on Ranyun Star. <b>CRON-120 closes this canon-fidelity gap:</b>
 * the right-click on the Crystal at the Suzaku Tomb is still the trigger
 * (the Crystal is the spiritual-focus point), but on success the player
 * is TELEPORTED to {@link PlanetSuzakuBlueprint#RANYUN_STAR}
 * (-5000, surface, -5000) — a remote overworld region representing
 * 冉云星 — and Wang Ping materializes at 落月村 (Luo Yue Village, the
 * woodcarver village at the foot of 祁连峰, materialized by
 * {@link dev.ergenverse.spawn.RanyunStarBuilder}). The Suzaku Tomb
 * remains the conception site; the redemption happens on Ranyun Star
 * (canon-faithful). The mod-original condensation is RETIRED in CRON-120.
 *
 * <h2>Trigger Mechanism — Player Right-Click on the Inherited Crystal</h2>
 * <p>The redemption fires when the player right-clicks the
 * {@link dev.ergenverse.block.CultivationPlanetCrystalBlock} (in the
 * {@code INHERITED=true} state) at the Suzaku Tomb. The right-click is
 * gated by all of the following (checked in
 * {@link dev.ergenverse.block.CultivationPlanetCrystalBlock#use}):
 *
 * <ol>
 *   <li><b>Crystal is inherited.</b> Canon-chronological: Wang Lin
 *       inherits the Suzaku Son status (Ch 443-450+) BEFORE Wang Ping's
 *       redemption (Ch 680+). The inheritance is a strict prerequisite.</li>
 *   <li><b>Player has exactly 1 FLESH strand AND 1 SOUL_GUARD strand in inventory.</b>
 *       CRON-118 canon correction: the prior CRON-117 implementation
 *       checked the bead's {@code isLiMuwanRevived} flag as a proxy for
 *       "Wang Lin has the prerequisites to channel Ling Tianhou's sword
 *       qi". This was chronologically INVERTED — Li Muwan is revived at
 *       the END of the novel (Wang Lin at 踏天境), which is FAR AFTER
 *       Wang Ping's redemption (Wang Lin at 问鼎). CRON-118 removes the
 *       Li Muwan revived proxy and replaces it with the canon-faithful
 *       Sword Qi Strand item (obtained from Ling Tianhou at the Da Luo
 *       Sword Sect, CRON-118).<br>
 *       CRON-122 canon-faithful strand-type differentiation: the prior
 *       CRON-118 check was "≥2 sword qi strands of any type". This
 *       incorrectly accepted 2 strands of the same type. CRON-122
 *       tightens the check to "exactly 1 FLESH + 1 SOUL_GUARD strand",
 *       matching the Baidu Baike 王平 entry: "一道化作王平的血肉之躯，
 *       另一道守护其魂魄" (one strand became Wang Ping's fleshly body,
 *       the other guarded his soul). Both functions are canon-required;
 *       the 2 strands (1 FLESH + 1 SOUL_GUARD) are consumed on successful
 *       redemption. See {@link dev.ergenverse.item.SwordQiStrandItem.StrandType}
 *       and {@link dev.ergenverse.block.CultivationPlanetCrystalBlock#use}.</li>
 *   <li><b>Player's cultivation realm ≥ {@link dev.ergenverse.cultivation.RealmId#ASCENDANT}
 *       (问鼎 / Ascendant).</b> Canon: Wang Lin is at 问鼎 (Ascendant)
 *       during the redemption arc, breaking through to 问鼎中期 (Ascendant
 *       middle stage) by the arc's end. CRON-119 corrected the
 *       {@code RealmId.ASCENDANT.nameCn} from {@code "合体"} (a realm from
 *       凡人修仙传, NOT 仙逆) to the canon {@code "问鼎"} (WenDing). The
 *       intent is "Wang Lin has reached the Ascendant-tier power needed
 *       to wield Ling Tianhou's sword qi".</li>
 *   <li><b>Wang Ping's {@code deadUntilRevived} flag is true.</b> The
 *       redemption is a one-time event per save. Once Wang Ping is
 *       redeemed, the flag is cleared (persisted via
 *       {@link WorldDeltaStore#markActorRevived}) and the right-click
 *       falls through to the "Crystal is silent" message.</li>
 * </ol>
 *
 * <h2>On Success</h2>
 * <ol>
 *   <li>Clears Wang Ping's {@code deadUntilRevived} flag (in-memory via
 *       {@link NPCRuntime#markActorAlive}, persisted via
 *       {@link WorldDeltaStore#markActorRevived}).</li>
 *   <li>Updates Wang Ping's ActorState position to the Crystal's (x, z)
 *       so future chunk reloads re-materialize him at the Suzaku Tomb.</li>
 *   <li>Materializes Wang Ping as a living {@link EntityCultivator} at
 *       the Crystal's position, with:
 *     <ul>
 *       <li>characterId = "wang_ping"</li>
 *       <li>displayName = "Wang Ping 王平"</li>
 *       <li>realm = "mortal" (canon: the sword-qi body has NO cultivation
 *           talent — Wang Ping cannot cultivate)</li>
 *       <li>HP = 20.0F (mortal — he is a child)</li>
 *     </ul>
 *   </li>
 *   <li>Spawns canon-faithful particle + sound effects (see below).</li>
 *   <li>Displays a bilingual message (Chinese + English).</li>
 *   <li>Records the event in HistoryManager.</li>
 * </ol>
 *
 * <h2>Particle + Sound Design (canon-faithful: sword-qi coalescing)</h2>
 * <ul>
 *   <li><b>END_ROD spiral ascending</b> at the Crystal — sword qi
 *       condensing downward into a body (descending spiral, mirror of
 *       the MuBingmeiAcceptanceEvent's ascending spiral).</li>
 *   <li><b>END_ROD ring at Wang Ping's spawn point</b> — the body
 *       forming, ring of condensing sword qi.</li>
 *   <li><b>FIREWORK central flash</b> at Wang Ping's chest — the moment
 *       of reconstruction (the "False Life" igniting).</li>
 *   <li><b>AMETHYST_BLOCK_CHIME sound at both ends</b> — the crystalline
 *       tone of sword qi (sword qi is canonically "crystalline" in
 *       texture — Ling Tianhou is the Sword Venerable of Da Luo Sword
 *       Sect, and the sword qi manifests as a crystalline tone).</li>
 *   <li><b>BELL sound at the midpoint</b> — the formal moment of
 *       redemption (like a temple bell, mirroring the MuBingmeiAcceptanceEvent).</li>
 *   <li><b>PLAYER_LEVELUP sound at Wang Ping</b> — the bright ascending
 *       tone of new life (a child's first breath).</li>
 * </ul>
 *
 * <p>Deliberately AVOIDS:
 * <ul>
 *   <li>ENDER_DRAGON_GROWL — too apocalyptic for a redemption arc.</li>
 *   <li>WITHER_SPAWN — too dark; Wang Ping's redemption is a positive
 *       narrative beat (father-son love), not a dark event.</li>
 *   <li>DRAGON_BREATH particles — too aggressive.</li>
 *   <li>LIGHTNING_BOLT — too destructive; this is a creation event,
 *       not a destruction event.</li>
 * </ul>
 *
 * <h2>State Transition Diagram</h2>
 * <pre>
 *   CRON-99:  Li Muwan dies → bead.hasLiMuwanSoul = true
 *   CRON-100/101/102: Player invokes /ergenverse bead revive (137 attempts)
 *             → bead.isLiMuwanRevived = true
 *             → Li Muwan materializes as companion at player position
 *   CRON-106: Player right-clicks Crystal with bead + realm ≥ NASCENT_SOUL
 *             → Crystal.INHERITED = true
 *             → bead.isSuzakuSon = true
 *             → Tuo Sen spawns (CRON-107)
 *   CRON-117: Player right-clicks INHERITED Crystal with bead (Li Muwan revived)
 *             + realm ≥ ASCENDANT + Wang Ping deadUntilRevived=true
 *             → Wang Ping deadUntilRevived = false (persisted)
 *             → Wang Ping materializes at Suzaku Tomb as mortal boy
 *             → (future CRON: Wang Ping mortal-life arc — woodcarving,
 *                marriage to 青宜, emperor reign, body dispersion,
 *                残魂 sealing into 天逆珠)
 * </pre>
 *
 * <h2>Single-Player Maximalism (Article XLIII)</h2>
 * <p>The redemption only fires when the player is at the Suzaku Tomb
 * with all prerequisites met. In single-player maximalism, this means
 * the player must:
 * <ol>
 *   <li>Reach Ascendant realm (问鼎).</li>
 *   <li>Complete the Suzaku Son inheritance (CRON-106).</li>
 *   <li>Complete Li Muwan's revival (CRON-100/101/102).</li>
 *   <li>Return to the Suzaku Tomb and right-click the (now-inherited)
 *       Crystal with the bead.</li>
 * </ol>
 * <p>This is a high-friction canon-faithful gate — the redemption is
 * a mid-late-game narrative beat, not a day-1 event.
 *
 * <h2>Architecture</h2>
 * <p>This event is a static utility class (mirrors the CRON-102
 * {@link LiMuwanRevivalEvent} and CRON-107 {@link TuoSenSpawnEvent}
 * patterns). The sole entry point is
 * {@link #redeemAtSuzakuTomb(ServerPlayer, BlockPos, long)}, called by
 * {@link dev.ergenverse.block.CultivationPlanetCrystalBlock#use} after
 * the prerequisites are checked.
 *
 * <p>The event uses the existing {@link WorldRuntime} singleton to access
 * the {@link NPCRuntime} (for ActorState) and the
 * {@link dev.ergenverse.runtime.materialize.CanonActorMaterializer} (for
 * entity creation). It persists the revived state via
 * {@link WorldDeltaStore#markActorRevived} (CRON-103 pattern).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see dev.ergenverse.block.CultivationPlanetCrystalBlock#use (trigger)
 * @see LiMuwanRevivalEvent (predecessor — CRON-102, revival pattern)
 * @see TuoSenSpawnEvent (parallel — CRON-107, post-inheritance spawn)
 * @see dev.ergenverse.runtime.CanonUUID#WANG_PING
 * @see dev.ergenverse.runtime.PlanetSuzakuBlueprint#SUZAKU_TOMB
 */
public final class WangPingRedemptionEvent {

    /**
     * Stable identifier for the HistoryManager subject on Wang Ping's
     * redemption (the body reconstruction beat). Distinct from other
     * subjects so subscribers can react specifically to this beat.
     *
     * <p>CRON-120: subject retained as the legacy id for backward
     * compatibility with prior CRON-117 history records. The actual
     * event now fires on Ranyun Star — see {@link #redeemAtRanyunStar}.
     */
    public static final String SUBJECT_WANG_PING_REDEEMED = "wang_ping_redeemed_at_suzaku_tomb";

    /**
     * CRON-120: the new HistoryManager subject reflecting the canon-
     * faithful location (Ranyun Star). Both the legacy id and this new
     * id are written on a successful redemption — the legacy id for
     * subscriber backward-compat, this new id for canon-faithful
     * attribution. Subscribers can listen to either.
     */
    public static final String SUBJECT_WANG_PING_REDEEMED_AT_RANYUN_STAR =
            "wang_ping_redeemed_at_ranyun_star";

    /**
     * The cultivation realm Wang Ping attains after redemption.
     * Canon: the sword-qi body has NO cultivation talent — Wang Ping
     * cannot cultivate. The realm is "mortal" (凡人) for life.
     *
     * <p>Wang Ping twice asked Wang Lin to learn cultivation and was
     * refused both times — because cultivating the sword-qi body would
     * detonate the residual resentment and kill him (Baidu Baike,
     * Fandom wiki). The mod honors this: realm = "mortal" permanently.
     */
    public static final String REDEEMED_REALM = "mortal";

    /**
     * Wang Ping's HP after redemption. Canon: he is a mortal child
     * with a sword-qi body — outwardly human but inwardly a construct.
     * The mod uses the standard mortal HP (20.0 = 10 hearts) to reflect
     * his mortal vulnerability. He is NOT a cultivator; he cannot
     * withstand combat with cultivation-tier threats.
     *
     * <p>Canon: Wang Ping's sword-qi body is a "False Life" — it does
     * not age normally, but it can be "killed" by dispersing the sword
     * qi. At age 72, Wang Ping voluntarily disperses his sword-qi body.
     * The mod does not implement the dispersion event (future CRON).
     */
    public static final float REDEEMED_HP = 20.0F;

    /**
     * Number of END_ROD particles in the descending sword-qi spiral
     * at the Crystal. The spiral represents sword qi condensing
     * downward into a body — the inverse of the MuBingmeiAcceptanceEvent's
     * ascending spiral (which represents Qi rising).
     */
    private static final int CRYSTAL_SPIRAL_PARTICLE_COUNT = 40;

    /**
     * Number of END_ROD particles in the body-formation ring at Wang
     * Ping's spawn point. The ring represents the body coalescing
     * from the descending sword qi.
     */
    private static final int BODY_RING_PARTICLE_COUNT = 32;

    /**
     * Number of END_ROD particles in the stream from the Crystal to
     * Wang Ping's spawn point. The stream represents the sword qi
     * flowing from the Crystal (the spiritual core) to the body
     * being reconstructed.
     */
    private static final int SWORD_QI_STREAM_PARTICLE_COUNT = 24;

    /**
     * Number of FIREWORK particles for the central flash at Wang Ping's
     * chest — the moment of "False Life" ignition.
     */
    private static final int CENTRAL_FLASH_PARTICLE_COUNT = 12;

    /**
     * Number of AMETHYST particles for the crystalline sword-qi burst
     * at both ends (Crystal + Wang Ping).
     */
    private static final int CRYSTAL_BURST_PARTICLE_COUNT = 16;

    /**
     * Vertical offset (blocks) above Wang Ping's feet where the body
     * forms. 1.0 = chest height. Used for particle spawning.
     */
    private static final double BODY_FORMATION_HEIGHT = 1.0;

    private WangPingRedemptionEvent() {}

    /**
     * Redeem Wang Ping — clear his {@code deadUntilRevived} flag and
     * materialize him as a mortal boy on Ranyun Star (冉云星).
     *
     * <p><b>CRON-COMPLETIONIST-120 — CANON-FAITHFUL RELOCATION.</b>
     * Prior to CRON-120, this method materialized Wang Ping at the
     * Suzaku Tomb (the conception site) — a mod-original condensation
     * flagged in CRON-117 self-critique #1. The canon clearly establishes
     * (Baidu Baike 仙逆编年史) that Wang Lin rebuilt Wang Ping's body
     * <b>on 冉云星 (Ranyun Star)</b> in the 罗天星域 / 雷之仙界 domain,
     * NOT at the Suzaku Tomb. CRON-120 closes this canon-fidelity gap:
     *
     * <p>The redemption now fires when the player right-clicks the
     * Cultivation Planet Crystal at the Suzaku Tomb (still the spiritual-
     * focus trigger), but on success the player is TELEPORTED to Ranyun
     * Star and Wang Ping is materialized at 落月村 (Luo Yue Village, the
     * woodcarver village at the foot of 祁连峰). The Suzaku Tomb remains
     * the conception site (Ch 443-450+); the redemption happens on
     * Ranyun Star (Ch 680+).
     *
     * <p>Called by {@link dev.ergenverse.block.CultivationPlanetCrystalBlock#use}
     * AFTER all prerequisites are verified (Crystal inherited, ≥2 Sword
     * Qi Strand items in inventory, player realm ≥ ASCENDANT, Wang Ping's
     * deadUntilRevived flag still true). This method performs the
     * canon-faithful redemption:
     * <ol>
     *   <li>Validates prerequisites (defensive — caller already checked).</li>
     *   <li>Force-loads the Ranyun Star destination chunk.</li>
     *   <li>Spawns a departure burst at the Crystal (Suzaku Tomb).</li>
     *   <li>Teleports the player to Ranyun Star (落月村 village center).</li>
     *   <li>Updates Wang Ping's ActorState.x/z to the Ranyun Star position.</li>
     *   <li>Persists the redeemed state and clears deadUntilRevived.</li>
     *   <li>Materializes Wang Ping at Ranyun Star via CanonActorMaterializer.</li>
     *   <li>Configures Wang Ping's realm="mortal" and HP=20.0F.</li>
     *   <li>Spawns redemption particle + sound effects at Ranyun Star.</li>
     *   <li>Displays the canon-faithful bilingual message (referencing 冉云星).</li>
     *   <li>Records in HistoryManager (both legacy + new subject ids).</li>
     * </ol>
     *
     * <p>Defensive: any failure (WorldRuntime unavailable, ActorState
     * missing, materialization failure) is logged at WARN level and the
     * method returns {@code false}. The caller (CultivationPlanetCrystalBlock)
     * does NOT undo the right-click — the player can retry by right-clicking
     * again. The redemption is idempotent: if it succeeds once, the flag
     * is cleared and subsequent right-clicks fall through to the "Crystal
     * is silent" message.
     *
     * @param player     the server player (Wang Lin) who triggered the
     *                   redemption by right-clicking the Crystal
     * @param crystalPos the position of the Cultivation Planet Crystal
     *                   at the Suzaku Tomb (the spiritual-focus trigger
     *                   point; the player is teleported FROM here TO
     *                   Ranyun Star)
     * @param currentTick the current game tick (for HistoryManager)
     * @return {@code true} if Wang Ping was successfully redeemed and
     *         materialized on Ranyun Star; {@code false} on any failure
     *         (logged at WARN)
     */
    public static boolean redeemAtRanyunStar(ServerPlayer player,
                                              BlockPos crystalPos,
                                              long currentTick) {
        ServerLevel level = player.serverLevel();

        // 1. Get the WorldRuntime singleton (defensive — mirrors LiMuwanRevivalEvent).
        WorldRuntime runtime;
        try {
            runtime = WorldRuntime.get();
        } catch (Throwable t) {
            Ergenverse.LOGGER.error("[Ergenverse] CRON-120: WorldRuntime not available — "
                    + "cannot redeem Wang Ping. Error: {}", t.getMessage(), t);
            player.sendSystemMessage(Component.literal(
                    "王平的残魂在天逆珠中叹息——世界运行时尚未就绪。")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        // 2. Get Wang Ping's ActorState from the NPCRuntime.
        NPCRuntime.ActorState state = runtime.npcs().getActor(CanonUUID.WANG_PING);
        if (state == null) {
            Ergenverse.LOGGER.error("[Ergenverse] CRON-120: Wang Ping's ActorState not found "
                    + "in NPCRuntime (canon UUID {}). Cannot redeem.", CanonUUID.WANG_PING);
            player.sendSystemMessage(Component.literal(
                    "王平的数据未在世界中注册。")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        // 3. Idempotency guard: if Wang Ping is already redeemed (deadUntilRevived=false),
        //    this method should not have been called. But defensive: check and return.
        if (!state.deadUntilRevived) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-120: Wang Ping is already redeemed "
                    + "(deadUntilRevived=false). The right-click should have fallen through "
                    + "to the 'Crystal is silent' message. Treating as a no-op.");
            return false;
        }

        // 4. Dematerialize existing Wang Ping entity (if any — defensive).
        if (runtime.npcs().isMaterialized(CanonUUID.WANG_PING)) {
            runtime.npcs().dematerializeActor(CanonUUID.WANG_PING, runtime);
            Ergenverse.LOGGER.info("[Ergenverse] CRON-120: Dematerialized existing Wang Ping "
                    + "entity before re-spawning on Ranyun Star.");
        }

        // ── 5. CRON-120: Compute Ranyun Star destination ──
        // The destination is the 落月村 village center at the foot of 祁连峰.
        // We force-load the destination chunk and query the surface Y via the
        // MOTION_BLOCKING heightmap (mirrors the ZhouRuKunxuDepartureEvent
        // teleport-safety pattern from CRON-112).
        int destX = PlanetSuzakuBlueprint.RANYUN_STAR.x;
        int destZ = PlanetSuzakuBlueprint.RANYUN_STAR.z;
        BlockPos destBlockPos = new BlockPos(destX, 0, destZ);

        // Force-load the destination chunk so the heightmap query is accurate
        // and the teleport destination is safe.
        level.getChunk(destBlockPos.getX() >> 4, destBlockPos.getZ() >> 4);

        // Query the surface Y via MOTION_BLOCKING heightmap (highest non-
        // motion-blocking block: ignores air, leaves, etc.).
        BlockPos surfacePos = level.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING, destBlockPos);
        int destSurfaceY = surfacePos.getY();

        double destCenterX = destX + 0.5;
        double destCenterY = destSurfaceY + 1.0;  // stand on the surface
        double destCenterZ = destZ + 0.5;

        // ── 6. CRON-120: Spawn departure burst at the Crystal (Suzaku Tomb) ──
        // The Crystal flares as Wang Lin channels the sword qi — the visual
        // cue that the player is about to be teleported to Ranyun Star.
        spawnDepartureBurst(level, crystalPos);

        // ── 7. CRON-120: Teleport the PLAYER to Ranyun Star ──
        // Mirrors ZhouRuKunxuDepartureEvent's teleport pattern, but for
        // ServerPlayer (not EntityCultivator). ServerPlayer extends Player
        // extends LivingEntity extends Entity, so teleportTo(x, y, z) is
        // available. Same-dimension teleport (overworld → overworld, just
        // from Suzaku Tomb to Ranyun Star region).
        //
        // Canon narrative: Wang Lin channels Ling Tianhou's sword qi through
        // the Crystal at the Suzaku Tomb; the Crystal tears open a void-rift
        // to Ranyun Star (where the redemption will happen). The player
        // steps through the rift and arrives at 落月村.
        player.teleportTo(destCenterX, destCenterY, destCenterZ);
        Ergenverse.LOGGER.info("[Ergenverse] CRON-120: Teleported player {} from Suzaku Tomb "
                        + "({}, {}, {}) to Ranyun Star ({}, {}, {}).",
                player.getName().getString(),
                crystalPos.getX(), crystalPos.getY(), crystalPos.getZ(),
                destX, destSurfaceY, destZ);

        // ── 8. Update Wang Ping's ActorState to the Ranyun Star position ──
        // Future chunk reloads will re-materialize Wang Ping at 落月村.
        state.x = destX;
        state.z = destZ;

        // 9. CRON-117/120: persist the redeemed state and clear the deadUntilRevived
        //    flag. Mirrors the CRON-103 Li Muwan pattern + CRON-107 Tuo Sen pattern:
        //    write the UUID to the WorldDeltaStore's revived-actor set (persists
        //    across world reload), and clear the in-memory deadUntilRevived flag.
        try {
            runtime.deltaStore().markActorRevived(CanonUUID.WANG_PING);
            runtime.npcs().markActorAlive(CanonUUID.WANG_PING);
            Ergenverse.LOGGER.info("[Ergenverse] CRON-120: marked Wang Ping as redeemed in "
                    + "WorldDeltaStore (persisted) and cleared deadUntilRevived flag in NPCRuntime.");
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-120: failed to persist redeemed-actor "
                    + "state for Wang Ping: {}", t.getMessage());
            // Non-fatal: we still attempt the materialization below.
        }

        // 10. Materialize Wang Ping at Ranyun Star via the standard
        //     CanonActorMaterializer. This sets the canon profile data
        //     (characterId="wang_ping", displayName="Wang Ping 王平",
        //     sectId="none", realm="mortal"). The materializer will use
        //     the updated state.x/z (Ranyun Star) to spawn him.
        int entityId = runtime.npcs().materializeActor(CanonUUID.WANG_PING, runtime);
        if (entityId < 0) {
            Ergenverse.LOGGER.error("[Ergenverse] CRON-120: CanonActorMaterializer failed to "
                    + "spawn Wang Ping at Ranyun Star ({}, {}).", destX, destZ);
            player.sendSystemMessage(Component.literal(
                    "王平的剑气未能凝聚成形——物化失败。")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        // 11. Find the spawned entity and configure it.
        EntityCultivator wangPing = findEntityById(level, entityId);
        if (wangPing == null) {
            Ergenverse.LOGGER.error("[Ergenverse] CRON-120: Wang Ping entity (id={}) not found "
                    + "after materialization on Ranyun Star.", entityId);
            return false;
        }

        // 12. Set his cultivation realm to "mortal" (canon: sword-qi body
        //     has NO cultivation talent).
        wangPing.setCultivationRealm(REDEEMED_REALM);

        // 13. Set his HP to the mortal value (canon: mortal child).
        try {
            wangPing.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                    .setBaseValue(REDEEMED_HP);
            wangPing.setHealth(REDEEMED_HP);
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-120: Failed to set Wang Ping's HP: {}",
                    t.getMessage());
        }

        // 14. Move Wang Ping to a position near the village center (offset by
        //     1 block east so he stands beside the woodcarver's bench, not on
        //     top of it). The materializer may have placed him at a slightly
        //     off position.
        double spawnX = destX + 1.0;
        double spawnY = destSurfaceY + 1.0;  // stand on the surface
        double spawnZ = destZ + 0.5;
        wangPing.moveTo(spawnX, spawnY, spawnZ,
                0.0F, 0.0F);  // facing south (toward the south path exit)

        // 15. Spawn the redemption particle + sound effects at Ranyun Star.
        //     Use a virtual "Crystal position" at the destination (the player
        //     is now at Ranyun Star; the effects should appear around Wang Ping
        //     and the woodcarver's altar).
        BlockPos ranyunCrystalPos = new BlockPos(destX, destSurfaceY, destZ);
        spawnRedemptionEffects(level, ranyunCrystalPos, wangPing);

        // 16. Display the canon-faithful redemption message (bilingual).
        //     CRON-120: text updated to reference 冉云星 (Ranyun Star) and
        //     落月村 (Luo Yue Village), not the Suzaku Tomb.
        player.sendSystemMessage(Component.literal(
                "─────────────────────────────────────")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal(
                "天逆珠中两道剑气迸发，撕裂虚空——朱雀墓与冉云星相连。")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "你踏过虚空裂缝，降临在罗天星域·冉云星·落月村。")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "剑气凝肉，剑气凝魂——王平的肉身在木雕师的山村中重塑。")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "「爹……」孩子睁开双眼，那双眼眸清明如水。")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Two strands of sword qi burst from the Heaven-Defying Bead, "
                        + "tearing the void — Suzaku Tomb and Ranyun Star are joined.")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "You step through the rift, arriving at Luo Yue Village on "
                        + "Ranyun Star in the Luo Tian Star Domain.")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Sword qi condenses into flesh, sword qi condenses into soul — "
                        + "Wang Ping's body is rebuilt in the woodcarver's village.")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "\"Father...\" The child opens his eyes — their clarity like water.")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "他不能哭，不能修炼，不能感受灵气——但他是你的儿子。")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "He cannot cry, cannot cultivate, cannot sense spiritual qi — "
                        + "but he is your son.")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "─────────────────────────────────────")
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        // 17. Record in HistoryManager — write BOTH subject ids for backward
        //     compatibility (legacy id) and canon-faithful attribution (new id).
        HistoryManager.onDiscovery(player, SUBJECT_WANG_PING_REDEEMED,
                "Wang Ping (王平) materialized as a mortal boy on Ranyun Star (冉云星), "
                        + "at 落月村 (Luo Yue Village, the woodcarver village at the foot of "
                        + "祁连峰). His body was rebuilt from two strands of sword qi (剑气) "
                        + "from Ling Tianhou (凌天侯), channeled through the Heaven-Defying "
                        + "Bead at the Cultivation Planet Crystal at the Suzaku Tomb. The "
                        + "Crystal tore open a void-rift; Wang Lin stepped through to Ranyun "
                        + "Star where the redemption took place. The sword-qi body is a "
                        + "'False Life' (虚假生命) — outwardly human but unable to cry, "
                        + "cultivate, or sense spiritual qi. Wang Lin has reclaimed his son "
                        + "from Liu Mei's resentment infant (怨婴). The mortal-life arc "
                        + "(二次化凡) may now begin on Ranyun Star.",
                currentTick);
        HistoryManager.onDiscovery(player, SUBJECT_WANG_PING_REDEEMED_AT_RANYUN_STAR,
                "Wang Ping redeemed on Ranyun Star (CRON-120 canon-faithful relocation). "
                        + "Redemption location: 落月村 at (" + destX + ", " + destSurfaceY
                        + ", " + destZ + ").",
                currentTick);

        Ergenverse.LOGGER.info("[Ergenverse] CRON-120: Wang Ping redeemed on Ranyun Star "
                        + "({}, {}, {}) for player {}. Realm={}, HP={}, characterId={}.",
                destX, destSurfaceY, destZ,
                player.getName().getString(), REDEEMED_REALM, REDEEMED_HP,
                wangPing.getCharacterId());

        return true;
    }

    /**
     * <b>CRON-COMPLETIONIST-120: DEPRECATED.</b> Use
     * {@link #redeemAtRanyunStar(ServerPlayer, BlockPos, long)} instead.
     *
     * <p>CRON-117's original implementation materialized Wang Ping at the
     * Suzaku Tomb — a mod-original condensation flagged in CRON-117 self-
     * critique #1 as canon-incorrect (canon places the redemption on
     * Ranyun Star, not at the Suzaku Tomb). CRON-120 closes this canon-
     * fidelity gap: this method now delegates to
     * {@link #redeemAtRanyunStar}, which teleports the player to Ranyun
     * Star and materializes Wang Ping at 落月村.
     *
     * <p>Retained as a {@code @Deprecated} delegator for backward
     * compatibility with any callers that still reference the old name.
     * The canonical entry point is now
     * {@link #redeemAtRanyunStar(ServerPlayer, BlockPos, long)}.
     *
     * @param player     the server player (Wang Lin)
     * @param crystalPos the position of the Cultivation Planet Crystal
     * @param currentTick the current game tick
     * @return {@code true} if Wang Ping was successfully redeemed
     * @deprecated since CRON-120; use {@link #redeemAtRanyunStar}
     */
    @Deprecated(since = "CRON-120", forRemoval = false)
    public static boolean redeemAtSuzakuTomb(ServerPlayer player,
                                               BlockPos crystalPos,
                                               long currentTick) {
        return redeemAtRanyunStar(player, crystalPos, currentTick);
    }

    /**
     * CRON-120: spawn a departure burst at the Crystal (Suzaku Tomb) before
     * teleporting the player to Ranyun Star. The visual idiom: the Crystal
     * flares as Wang Lin channels the sword qi — the rift to Ranyun Star
     * opens. Mirrors the ZhouRuKunxuDepartureEvent departure-burst pattern.
     */
    private static void spawnDepartureBurst(ServerLevel level, BlockPos crystalPos) {
        double cx = crystalPos.getX() + 0.5;
        double cy = crystalPos.getY() + 1.0;
        double cz = crystalPos.getZ() + 0.5;

        // END_ROD outward burst (the rift opening)
        level.sendParticles(ParticleTypes.END_ROD,
                cx, cy, cz, 24,
                0.6, 0.8, 0.6, 0.05);

        // PORTAL particles (the void-rift color)
        level.sendParticles(ParticleTypes.DRAGON_BREATH,
                cx, cy, cz, 16,
                0.4, 0.6, 0.4, 0.02);

        // AMETHYST_BLOCK_CHIME (the crystalline tone of sword qi)
        level.playSound(null, crystalPos,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT,
                1.2F, 0.7F);  // louder + lower pitch = heavier tone (the rift opening)

        // ENDERMAN_TELEPORT (the teleport sound — the player is about to leave)
        level.playSound(null, crystalPos,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.AMBIENT,
                1.0F, 0.8F);
    }

    /**
     * Find an entity by Minecraft entity ID in the server level.
     * (Helper — ServerLevel has no direct getEntity(int) in 1.20.1; iteration
     * is the supported approach. The entity count is small at spawn time.)
     * Mirrors the {@link LiMuwanRevivalEvent#findEntityById} pattern.
     *
     * @param level    the server level
     * @param entityId the Minecraft entity ID
     * @return the EntityCultivator, or null if not found
     */
    private static EntityCultivator findEntityById(ServerLevel level, int entityId) {
        for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
            if (e.getId() == entityId && e instanceof EntityCultivator ec) {
                return ec;
            }
        }
        return null;
    }

    /**
     * Spawn canon-faithful particle + sound effects for the redemption.
     * The visual idiom (sword-qi coalescing into a body):
     * <ul>
     *   <li><b>END_ROD descending spiral at the Crystal</b> — sword qi
     *       condensing downward from the Crystal into the world.</li>
     *   <li><b>END_ROD ring at Wang Ping's spawn point</b> — the body
     *       coalescing from the descending sword qi.</li>
     *   <li><b>END_ROD stream from Crystal to Wang Ping</b> — the sword
     *       qi flowing from the spiritual core to the body being built.</li>
     *   <li><b>FIREWORK central flash at Wang Ping's chest</b> — the
     *       moment of "False Life" ignition.</li>
     *   <li><b>AMETHYST_BLOCK_CHIME at both ends</b> — the crystalline
     *       tone of sword qi (Ling Tianhou is the Sword Venerable; his
     *       sword qi manifests as a crystalline tone).</li>
     *   <li><b>BELL at midpoint</b> — the formal moment of redemption
     *       (like a temple bell, mirroring MuBingmeiAcceptanceEvent).</li>
     *   <li><b>PLAYER_LEVELUP at Wang Ping</b> — the bright ascending
     *       tone of new life (a child's first breath).</li>
     * </ul>
     *
     * @param level     the server level
     * @param crystalPos the Crystal's block position (sword-qi source)
     * @param wangPing  the materialized Wang Ping entity (sword-qi destination)
     */
    private static void spawnRedemptionEffects(ServerLevel level,
                                                 BlockPos crystalPos,
                                                 EntityCultivator wangPing) {
        double crystalX = crystalPos.getX() + 0.5;
        double crystalY = crystalPos.getY() + 1.0;  // chest height
        double crystalZ = crystalPos.getZ() + 0.5;

        double bodyX = wangPing.getX();
        double bodyY = wangPing.getY() + BODY_FORMATION_HEIGHT;
        double bodyZ = wangPing.getZ();

        double midX = (crystalX + bodyX) / 2.0;
        double midY = (crystalY + bodyY) / 2.0;
        double midZ = (crystalZ + bodyZ) / 2.0;

        // ── Descending END_ROD spiral at the Crystal (sword qi condensing) ──
        // A spiral that descends from 4 blocks above the Crystal to chest height,
        // representing sword qi condensing downward into the world.
        for (int i = 0; i < CRYSTAL_SPIRAL_PARTICLE_COUNT; i++) {
            double t = i / (double) CRYSTAL_SPIRAL_PARTICLE_COUNT;
            double theta = t * Math.PI * 4;  // 2 full turns
            double radius = 0.8 * (1.0 - t * 0.5);  // narrowing spiral
            double dx = Math.cos(theta) * radius;
            double dz = Math.sin(theta) * radius;
            // Descending from 4 blocks above to chest height
            double dy = 4.0 * (1.0 - t);
            level.sendParticles(ParticleTypes.END_ROD,
                    crystalX + dx, crystalPos.getY() + dy, crystalZ + dz, 1,
                    0.0, -0.08, 0.0, 0.0);
        }

        // ── Body-formation END_ROD ring at Wang Ping (body coalescing) ──
        // A ring at chest height, expanding outward — the body forming.
        for (int i = 0; i < BODY_RING_PARTICLE_COUNT; i++) {
            double theta = (i / (double) BODY_RING_PARTICLE_COUNT) * Math.PI * 2;
            double radius = 0.6;
            double dx = Math.cos(theta) * radius;
            double dz = Math.sin(theta) * radius;
            // Slight vertical jitter for a "body forming" effect
            double dy = (i % 3 - 1) * 0.15;
            level.sendParticles(ParticleTypes.END_ROD,
                    bodyX + dx, bodyY + dy, bodyZ + dz, 1,
                    0.0, 0.02, 0.0, 0.0);
        }

        // ── END_ROD stream from Crystal to Wang Ping (sword qi flowing) ──
        for (int i = 0; i < SWORD_QI_STREAM_PARTICLE_COUNT; i++) {
            double t = i / (double) (SWORD_QI_STREAM_PARTICLE_COUNT - 1);
            double px = crystalX + (bodyX - crystalX) * t;
            double py = crystalY + (bodyY - crystalY) * t;
            double pz = crystalZ + (bodyZ - crystalZ) * t;
            // Small lateral jitter for visual richness
            double jitterX = (i % 3 - 1) * 0.08;
            double jitterZ = (i % 5 - 2) * 0.08;
            level.sendParticles(ParticleTypes.END_ROD,
                    px + jitterX, py, pz + jitterZ, 1,
                    0.0, 0.0, 0.0, 0.0);
        }

        // ── AMETHYST crystalline burst at both ends (sword qi tone) ──
        level.sendParticles(ParticleTypes.END_ROD,
                crystalX, crystalY, crystalZ, CRYSTAL_BURST_PARTICLE_COUNT,
                0.4, 0.4, 0.4, 0.05);
        level.sendParticles(ParticleTypes.END_ROD,
                bodyX, bodyY, bodyZ, CRYSTAL_BURST_PARTICLE_COUNT,
                0.4, 0.4, 0.4, 0.05);

        // ── Central FIREWORK flash at Wang Ping's chest (False Life igniting) ──
        level.sendParticles(ParticleTypes.FIREWORK,
                bodyX, bodyY, bodyZ, CENTRAL_FLASH_PARTICLE_COUNT,
                0.3, 0.3, 0.3, 0.05);

        // ── Sounds ──
        // AMETHYST_BLOCK_CHIME at the Crystal (crystalline sword-qi source)
        level.playSound(null, crystalPos,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT,
                0.9F, 1.0F);  // volume 0.9, pitch 1.0 (clear crystalline tone)

        // AMETHYST_BLOCK_CHIME at Wang Ping (crystalline sword-qi coalescing)
        level.playSound(null, wangPing.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT,
                0.8F, 1.3F);  // volume 0.8, pitch 1.3 (slightly brighter at body)

        // PLAYER_LEVELUP at Wang Ping (bright ascending — new life)
        level.playSound(null, wangPing.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT,
                0.7F, 1.4F);  // volume 0.7, pitch 1.4 (bright, ascending)

        // BELL at midpoint (formal redemption — like a temple bell)
        level.playSound(null, BlockPos.containing(midX, midY, midZ),
                SoundEvents.BELL_BLOCK, SoundSource.AMBIENT,
                1.0F, 1.0F);  // volume 1.0, pitch 1.0 (clear bell tone)
    }
}
