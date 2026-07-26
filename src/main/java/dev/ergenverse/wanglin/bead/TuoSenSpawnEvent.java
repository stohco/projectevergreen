package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.history.HistoryManager;
import dev.ergenverse.runtime.CanonUUID;
import dev.ergenverse.runtime.NPCRuntime;
import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.runtime.WorldRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Tuo Sen's Spawn Event — CRON-COMPLETIONIST-107.
 *
 * <p>Spawns 拓森 (Tuo Sen) as a living {@link EntityCultivator} at the
 * Suzaku Tomb chamber when the player triggers the 15th-gen Suzaku Son
 * inheritance event via {@link CultivationPlanetCrystalBlock#use}
 * (CRON-106). This is the canon-faithful reappearance of Wang Lin's
 * Ancient God rival at the inheritance site.
 *
 * <h2>Canon Basis (fact-checked via web-search 2026-07-26)</h2>
 * <p>In the novel 仙逆 by 耳根:
 * <ul>
 *   <li><b>Sohu (2024-06-17):</b> "时隔300年，王林在朱雀墓再遇拓森"
 *       — after 300 years, Wang Lin re-encounters Tuo Sen at the
 *       Suzaku Tomb. The encounter triggers Wang Lin's flight from the
 *       tomb using the Suzaku inheritance's escape power.</li>
 *   <li><b>163 (2025-07-29):</b> "拓森现身朱雀墓，获得九星古神血液，抢下修星之晶"
 *       — Tuo Sen appears at the Suzaku Tomb, obtains 9-star Ancient God
 *       blood, and seizes the Cultivation Planet Crystal (修星之晶).</li>
 *   <li><b>Baidu Baike (拓森):</b> Tuo Sen is an 8-star Ancient God,
 *       born from Tu Si's (涂司) failed Ink Flow Split Soul Technique
 *       (墨流分魂术). He inherited Tu Si's "power" portion, while Wang Lin
 *       received the "knowledge" portion. He is Wang Lin's recurring
 *       Ancient God rival throughout the series.</li>
 *   <li><b>Sohu (2025-08-06):</b> Tuo Sen contests the inheritance at
 *       the tomb, motivated by his rivalry with Wang Lin and his
 *       ambition to claim Tu Si's full legacy.</li>
 * </ul>
 *
 * <p><b>NO fabricated chapter citation.</b> The reappearance at the
 * Suzaku Tomb during the inheritance event is canon-attested via multiple
 * web-search sources (Sohu, 163, Baidu Baike). The exact chapter is NOT
 * cited to avoid fabrication.
 *
 * <h2>What This Event Does</h2>
 * <ol>
 *   <li><b>Dematerialize existing Tuo Sen</b> (if any). Tuo Sen may
 *       already be materialized elsewhere from a prior inheritance event
 *       (shouldn't happen — the Crystal is one-time-inherited — but
 *       defensive). Dematerialize before re-spawning.</li>
 *   <li><b>Update his ActorState</b> to the Suzaku Tomb position (in case
 *       the canon position drifted from a prior spawn).</li>
 *   <li><b>Clear the deadUntilRevived flag</b> (CRON-103 pattern):
 *       <ul>
 *         <li>{@code runtime.deltaStore().markActorRevived(CanonUUID.TUO_SEN)}
 *             — persists the revived state to the WorldDeltaStore journal
 *             (survives world reload).</li>
 *         <li>{@code runtime.npcs().markActorAlive(CanonUUID.TUO_SEN)}
 *             — clears the in-memory deadUntilRevived flag on ActorState.</li>
 *       </ul>
 *   </li>
 *   <li><b>Materialize Tuo Sen</b> at the Suzaku Tomb chamber. The
 *       chamber is at y=-60 (underground); the standard
 *       {@link dev.ergenverse.runtime.materialize.CanonActorMaterializer}
 *       uses a heightmap on (x, z) which would resolve to the SURFACE,
 *       not the chamber. So we materialize via the standard path (which
 *       sets the canon profile data) and then teleport the entity to
 *       the chamber Y.</li>
 *   <li><b>Set his cultivation realm</b> to "ancient" (古境 /
 *       {@link dev.ergenverse.cultivation.RealmId#ANCIENT}). Canon: an
 *       8-star Ancient God.</li>
 *   <li><b>Set his HP</b> to a high value (Tuo Sen is a major boss-tier
 *       entity). The mod represents this with 500 HP — high enough to
 *       be a serious fight, low enough that a Transcendence-realm Wang
 *       Lin can defeat him (canon: Wang Lin eventually surpasses him).</li>
 *   <li><b>Display the canon-faithful reappearance message</b> (bilingual:
 *       Chinese + English, with the CRON-102 divider pattern).</li>
 *   <li><b>Record in HistoryManager</b> (subject:
 *       {@link #SUBJECT_TUO_SEN_REAPPEARED}).</li>
 * </ol>
 *
 * <h2>Relationship to CRON-106 (CultivationPlanetCrystalBlock)</h2>
 *
 * <p>CRON-106 implements the inheritance event (right-click Crystal →
 * prerequisites checked → bead marked with Suzaku Son status). CRON-107
 * adds the canon-faithful Tuo Sen reappearance as a CONSEQUENCE of the
 * inheritance. The spawn call is injected into
 * {@link CultivationPlanetCrystalBlock#use} immediately after the
 * inheritance succeeds.
 *
 * <p>The spawn is DEFENSIVE: if Tuo Sen is already materialized (e.g.,
 * the player triggered inheritance, left, and came back — but the
 * Crystal is one-time-inherited so this shouldn't happen), the spawn
 * dematerializes the existing entity first. Idempotent.
 *
 * <h2>Canon-faithful design choices</h2>
 *
 * <ul>
 *   <li><b>Tuo Sen does NOT follow the player.</b> Unlike Li Muwan
 *       (CRON-102, who follows Wang Lin as his companion), Tuo Sen is
 *       a RIVAL. He spawns at the tomb chamber and stays there. The
 *       player must choose to engage or flee (canon: Wang Lin flees
 *       the tomb using the Suzaku inheritance's escape power).</li>
 *   <li><b>Tuo Sen's HP is 500, not 1000.</b> Canon: Wang Lin eventually
 *       surpasses and defeats Tuo Sen, but it takes hundreds of chapters
 *       and several power-ups. 500 HP represents "a serious fight for a
 *       mid-game Wang Lin, defeatable by a late-game Wang Lin." A future
 *       CRON could scale this with the player's realm.</li>
 *   <li><b>Tuo Sen's realm is "ancient" (古境).</b> This maps to
 *       {@link dev.ergenverse.cultivation.RealmId#ANCIENT} (order=15,
 *       step=3 Immortal+ Step). Canon: Tuo Sen is an 8-star Ancient
 *       God — the Ancient God tier IS the Ancient (古境) realm in the
 *       mod's unified realm ladder.</li>
 *   <li><b>Tuo Sen spawns at the chamber center, not the surface.</b>
 *       The Suzaku Tomb chamber is at y=-60 (underground). The standard
 *       CanonActorMaterializer uses a heightmap on (x, z) which resolves
 *       to the SURFACE — wrong for an underground tomb. TuoSenSpawnEvent
 *       materializes via the standard path (for profile data) then
 *       teleports to the chamber Y (TOMB_Y + 1 = -59, the Crystal's Y).</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see CultivationPlanetCrystalBlock#use
 * @see dev.ergenverse.runtime.materialize.CanonActorMaterializer
 * @see LiMuwanRevivalEvent (CRON-102, parallel pattern)
 */
public final class TuoSenSpawnEvent {

    /**
     * Stable identifier for the HistoryManager subject on Tuo Sen's
     * reappearance at the Suzaku Tomb.
     */
    public static final String SUBJECT_TUO_SEN_REAPPEARED = "tuo_sen_reappeared_at_suzaku_tomb";

    /**
     * The cultivation realm Tuo Sen is set to when he spawns.
     * Canon: 8-star Ancient God (古神). Maps to
     * {@link dev.ergenverse.cultivation.RealmId#ANCIENT} (古境).
     */
    public static final String SPAWN_REALM = "ancient";

    /**
     * Tuo Sen's HP when he spawns. Canon: a major boss-tier Ancient God.
     * The mod represents this with 500 HP — high enough to be a serious
     * fight, low enough that a Transcendence-realm Wang Lin can defeat
     * him (canon: Wang Lin eventually surpasses him).
     *
     * <p>A future CRON could scale this with the player's realm (e.g.,
     * HP = player_realm_order * 50, so a Nascent Soul player faces 200 HP
     * and a Transcendence player faces 850 HP — keeping the fight
     * challenging across the cultivation ladder).
     */
    public static final float SPAWN_HP = 500.0F;

    /**
     * The Y offset above the tomb center where Tuo Sen spawns.
     * The tomb center is at TOMB_Y = -60; the chamber floor is at
     * FLOOR_Y = -63; the Crystal is at TOMB_Y + 1 = -59. Tuo Sen
     * spawns at TOMB_Y + 1 = -59 (same Y as the Crystal, standing
     * on the pedestal next to it — canon-faithful: he contests the
     * Crystal).
     */
    private static final int SPAWN_Y_OFFSET = 1;

    private TuoSenSpawnEvent() {}

    /**
     * Spawn Tuo Sen at the Suzaku Tomb chamber. Called by
     * {@link CultivationPlanetCrystalBlock#use} immediately after the
     * inheritance event succeeds (prerequisites met, block-state
     * transitioned, bead marked).
     *
     * <p>This method is DEFENSIVE: it never throws. On any failure, it
     * logs a warning and sends a player message, but does NOT block the
     * inheritance (the inheritance already succeeded; Tuo Sen's spawn
     * is a consequence, not a prerequisite).
     *
     * @param player       the player who triggered the inheritance
     *                     (passed for message-sending and HistoryManager)
     * @param crystalPos   the position of the Cultivation Planet Crystal
     *                     (used to locate the tomb chamber; Tuo Sen
     *                     spawns near the Crystal)
     * @param currentTick  the current game tick (for HistoryManager)
     * @return {@code true} if Tuo Sen spawned successfully, {@code false}
     *         on any failure (the inheritance still succeeded)
     */
    public static boolean spawnAtSuzakuTomb(ServerPlayer player,
                                              BlockPos crystalPos,
                                              long currentTick) {
        ServerLevel level = player.serverLevel();

        // 1. Get the WorldRuntime singleton.
        WorldRuntime runtime;
        try {
            runtime = WorldRuntime.get();
        } catch (Throwable t) {
            Ergenverse.LOGGER.error("[Ergenverse] CRON-107: WorldRuntime not available — "
                    + "Tuo Sen cannot spawn. Inheritance still succeeded.", t);
            player.sendSystemMessage(Component.literal(
                    "拓森的气息在虚空中浮现，却又消散——世界运行时尚未就绪。")
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }

        // 2. Get Tuo Sen's ActorState from the NPCRuntime.
        NPCRuntime.ActorState state = runtime.npcs().getActor(CanonUUID.TUO_SEN);
        if (state == null) {
            Ergenverse.LOGGER.error("[Ergenverse] CRON-107: Tuo Sen's ActorState not found "
                    + "(canon UUID {}). Cannot spawn. Inheritance still succeeded.",
                    CanonUUID.TUO_SEN);
            player.sendSystemMessage(Component.literal(
                    "拓森的气息在虚空中浮现，却又消散——他尚未被注册。")
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }

        // 3. Dematerialize existing Tuo Sen entity (if any — defensive).
        //    This shouldn't happen (Crystal is one-time-inherited), but
        //    if Tuo Sen is already materialized (e.g., from a prior spawn
        //    that wasn't cleaned up), dematerialize before re-spawning.
        if (runtime.npcs().isMaterialized(CanonUUID.TUO_SEN)) {
            runtime.npcs().dematerializeActor(CanonUUID.TUO_SEN, runtime);
            Ergenverse.LOGGER.info("[Ergenverse] CRON-107: Dematerialized existing Tuo Sen "
                    + "before re-spawning at the Suzaku Tomb.");
        }

        // 4. Update his ActorState to the Suzaku Tomb position (in case
        //    the canon position drifted from a prior spawn or a save migration).
        //    Use the Crystal's X/Z (which equals SUZAKU_TOMB.x/z).
        state.x = crystalPos.getX();
        state.z = crystalPos.getZ();

        // 4b. CRON-107: persist the revived state and clear the deadUntilRevived flag.
        //    This mirrors the CRON-103 Li Muwan pattern: write the UUID to the
        //    WorldDeltaStore's revived-actor set (persists across world reload),
        //    and clear the in-memory deadUntilRevived flag.
        try {
            runtime.deltaStore().markActorRevived(CanonUUID.TUO_SEN);
            runtime.npcs().markActorAlive(CanonUUID.TUO_SEN);
            Ergenverse.LOGGER.info("[Ergenverse] CRON-107: marked Tuo Sen as revived in "
                    + "WorldDeltaStore (persisted) and cleared deadUntilRevived flag.");
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-107: failed to persist revived-actor "
                    + "state for Tuo Sen: {}", t.getMessage());
        }

        // 5. Materialize Tuo Sen via the standard CanonActorMaterializer.
        //    This sets the canon profile data (characterId, displayName, sectId, realm).
        int entityId = runtime.npcs().materializeActor(CanonUUID.TUO_SEN, runtime);
        if (entityId < 0) {
            Ergenverse.LOGGER.error("[Ergenverse] CRON-107: CanonActorMaterializer failed to "
                    + "spawn Tuo Sen. Inheritance still succeeded.");
            player.sendSystemMessage(Component.literal(
                    "拓森的气息在虚空中浮现，却又消散——物化失败。")
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }

        // 6. Find the spawned entity and configure it.
        EntityCultivator tuoSen = findEntityById(level, entityId);
        if (tuoSen == null) {
            Ergenverse.LOGGER.error("[Ergenverse] CRON-107: Tuo Sen entity not found after "
                    + "materialization (entityId={}). Inheritance still succeeded.", entityId);
            return false;
        }

        // 7. Set his cultivation realm to "ancient" (古境 / Ancient God tier).
        //    Canon: 8-star Ancient God. The CanonActorMaterializer already set
        //    this from the profile, but we set it again here to be explicit
        //    and to allow future scaling (e.g., realm based on player realm).
        tuoSen.setCultivationRealm(SPAWN_REALM);

        // 8. Set his HP to the spawn value (canon: a major boss-tier Ancient God).
        tuoSen.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                .setBaseValue(SPAWN_HP);
        tuoSen.setHealth(SPAWN_HP);

        // 9. Teleport him to the tomb chamber Y (underground, not the surface).
        //    The standard CanonActorMaterializer used a heightmap on (x, z) which
        //    resolved to the SURFACE — wrong for an underground tomb. We teleport
        //    to the chamber Y (TOMB_Y + SPAWN_Y_OFFSET = -59, the Crystal's Y).
        //    Spawn him 2 blocks east of the Crystal so he doesn't overlap the
        //    pedestal — canon-faithful: he CONTESTS the Crystal, not stands on it.
        int spawnX = crystalPos.getX() + 2;
        int spawnY = crystalPos.getY();
        int spawnZ = crystalPos.getZ();
        tuoSen.moveTo(spawnX + 0.5, spawnY, spawnZ + 0.5, 0.0F, 0.0F);

        // 10. Display the canon-faithful reappearance message (bilingual).
        player.sendSystemMessage(Component.literal(
                "─────────────────────────────────────")
                .withStyle(ChatFormatting.DARK_RED));
        player.sendSystemMessage(Component.literal(
                "一阵古神威压从修炼星晶后浮现。拓森，那个跨越三百年的宿敌，再次现身。")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "「王林……你的知识传承，今日我要夺回。」")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "An Ancient God's pressure manifests behind the Crystal. "
                        + "Tuo Sen — your rival across three centuries — reappears.")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "\"Wang Lin... your knowledge inheritance — today, I will reclaim it.\"")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "他踏虚而立，与修炼星晶遥遥相对。")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "He stands suspended in air, facing the Crystal across the chamber.")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "─────────────────────────────────────")
                .withStyle(ChatFormatting.DARK_RED));

        // 11. Record in HistoryManager.
        HistoryManager.onDiscovery(player, SUBJECT_TUO_SEN_REAPPEARED,
                "Tuo Sen (拓森) reappeared at the Suzaku Tomb during the 15th-gen "
                        + "Suzaku Son inheritance event. An 8-star Ancient God, rival "
                        + "to Wang Lin for Tu Si's Ancient God inheritance. Spawned "
                        + "at the tomb chamber near the Cultivation Planet Crystal. "
                        + "HP=" + SPAWN_HP + ", realm=" + SPAWN_REALM + ".",
                currentTick);

        Ergenverse.LOGGER.info("[Ergenverse] CRON-107: Tuo Sen spawned at ({}, {}, {}) for "
                        + "player {} (inheritance of the Cultivation Planet Crystal). "
                        + "Realm={}, HP={}, entityId={}.",
                spawnX, spawnY, spawnZ, player.getName().getString(),
                SPAWN_REALM, SPAWN_HP, entityId);

        return true;
    }

    /**
     * Find an entity by Minecraft entity ID in the server level.
     * (Helper — mirrors the {@link LiMuwanRevivalEvent} pattern.)
     */
    private static EntityCultivator findEntityById(ServerLevel level, int entityId) {
        for (Entity e : level.getAllEntities()) {
            if (e.getId() == entityId && e instanceof EntityCultivator ec) {
                return ec;
            }
        }
        return null;
    }
}
