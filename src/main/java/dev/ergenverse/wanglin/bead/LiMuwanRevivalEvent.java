package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.entity.EREntityTypes;
import dev.ergenverse.history.HistoryManager;
import dev.ergenverse.runtime.CanonUUID;
import dev.ergenverse.runtime.NPCRuntime;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.runtime.delta.WorldDeltaStore;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.core.BlockPos;

/**
 * Li Muwan's Revival Event — CRON-COMPLETIONIST-102.
 *
 * <p>Spawns Li Muwan as a living {@link EntityCultivator} at the player's
 * position after the successful revival. This is the emotional capstone of
 * the Li Muwan arc (CRON-99 soul capture → CRON-100 revival attempts →
 * CRON-101 World Origin Essence → CRON-102 Li Muwan spawns as a companion).
 *
 * <h2>Canon Basis (fact-checked via web-search 2026-07-26)</h2>
 * <p>In the novel 仙逆 by 耳根, after Wang Lin enters the Fourth Step and
 * uses 一界本源 to revive Li Muwan:
 * <ul>
 *   <li>"此后，两人踏天同行，超越生死轮回，相爱相守，生生世世" —
 *       they transcend together, beyond life and death, in love for all
 *       eternity. She is Wang Lin's eternal companion.</li>
 *   <li>"李慕婉复活后弹指灭天" — after revival, Li Muwan's power is
 *       immense: she destroys heaven with a flick of her finger. The
 *       laws of heaven shatter like glaze before her.</li>
 *   <li>Her cultivation reaches 踏天境 (Heaven-Trampling Realm = Fourth
 *       Step = TRANSCENDENCE), the same realm as Wang Lin.</li>
 * </ul>
 *
 * <p><b>NO fabricated chapter citation.</b> The post-revival companion
 * relationship and Li Muwan's transcendent power are canon-attested via
 * multiple web-search sources (Baidu Baike, 360娱乐, etc.). The exact
 * chapter is NOT cited to avoid fabrication.
 *
 * <h2>What This Event Does</h2>
 * <ol>
 *   <li><b>Dematerialize existing Li Muwan</b> (if any). Li Muwan may
 *       already be materialized at her canon home (Luo He Sect) from
 *       the NPCRuntime's day-0 registration. We dematerialize her there
 *       so she can re-materialize at the player's position.</li>
 *   <li><b>Update her ActorState</b> to the player's current (x, z).
 *       This ensures that on future chunk reloads, she re-materializes
 *       at the player's last known position (not her old Luo He Sect
 *       position).</li>
 *   <li><b>Materialize Li Muwan</b> at the player's position using the
 *       {@link dev.ergenverse.runtime.materialize.CanonActorMaterializer}.
 *       This creates a new EntityCultivator with her canon UUID, display
 *       name, and sect.</li>
 *   <li><b>Set her cultivation realm</b> to {@code "transcendence"}
 *       (踏天境 / Fourth Step). Canon: after revival, Li Muwan transcends
 *       with Wang Lin — she is his equal in power.</li>
 *   <li><b>Set her companion bond</b> to the player's UUID via
 *       {@link EntityCultivator#setFollowingPlayerUuid}. This activates
 *       the {@link dev.ergenverse.entity.ai.FollowPlayerGoal}, making
 *       her follow the player ("两人踏天同行").</li>
 *   <li><b>Display the spawn message</b> — a bilingual canon-faithful
 *       message announcing Li Muwan's appearance.</li>
 *   <li><b>Record in HistoryManager</b> — a stable subject ID for
 *       future canon-event replay systems.</li>
 * </ol>
 *
 * <h2>Mod-Fidelity Bridges (documented honestly)</h2>
 * <ul>
 *   <li><b>Li Muwan is no longer alive at Luo He Sect from day 0 (CRON-103 closed this gap).</b>
 *       Prior to CRON-103, Li Muwan was registered as a living NPC at Luo He Sect
 *       from the start (NPCRuntime.loadAll), contradicting canon (she is DEAD
 *       before the revival arc — she perishes when her Nascent Soul formation
 *       fails). CRON-103 marks her {@code deadUntilRevived=true} at registration;
 *       CanonActorMaterializer refuses to materialize her until this event fires.
 *       This event clears the flag and persists the revived state via
 *       {@link WorldDeltaStore#markActorRevived}, so on world reload the
 *       revived-actor set is applied to keep her alive. Pre-CRON-103 saves
 *       (where the bead is already revived but the revived-set is empty) are
 *       migrated by {@link #migrateRevivedFlagIfNeeded} on player login.</li>
 *   <li><b>HP is not set to a canon-attested value.</b> Canon: Li Muwan's
 *       post-revival power is "弹指灭天" (destroys heaven with a flick).
 *       The mod sets her HP to 1000 (a high value representing her
 *       transcendent state) but does not implement the "destroy heaven"
 *       mechanic. That is a future CRON.</li>
 *   <li><b>Personality/dialogue is not changed.</b> Li Muwan retains her
 *       pre-revival NPC JSON data (initiation lines, daily schedule,
 *       sect tasks). A future CRON could add a "revived" personality
 *       profile with new dialogue reflecting her transcendent state.</li>
 * </ul>
 *
 * <h2>Architecture</h2>
 * <p>This event is a static utility class (mirrors the CRON-99
 * {@link LiMuwanSoulCaptureEvent} pattern). The sole entry point is
 * {@link #spawnAtPlayer(ServerPlayer, long)}, called by
 * {@link RevivalAttemptService#doSuccessfulRevival} after the World
 * Origin Essence is consumed.
 *
 * <p>The event uses the existing {@link WorldRuntime} singleton to access
 * the {@link NPCRuntime} (for ActorState) and the
 * {@link dev.ergenverse.runtime.materialize.CanonActorMaterializer} (for
 * entity creation). It does NOT touch the WorldDeltaStore or WorldFacade
 * — the companion bond is entity-state, not world-state.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see RevivalAttemptService#doSuccessfulRevival
 * @see EntityCultivator#setFollowingPlayerUuid
 * @see dev.ergenverse.entity.ai.FollowPlayerGoal
 */
public final class LiMuwanRevivalEvent {

    /**
     * Stable identifier for the HistoryManager subject on Li Muwan's
     * revival (the NPC-spawn beat, distinct from the general success beat
     * and the essence-consumed beat).
     */
    public static final String SUBJECT_LI_MUWAN_SPAWNED = "li_muwan_spawned_as_companion";

    /**
     * The cultivation realm Li Muwan attains after revival.
     * Canon: 踏天境 (Heaven-Trampling Realm = Fourth Step = TRANSCENDENCE).
     * After revival, she transcends with Wang Lin — she is his equal.
     */
    public static final String REVIVED_REALM = "transcendence";

    /**
     * Li Muwan's HP after revival. Canon: "弹指灭天" — her power is
     * immense. The mod represents this with a high HP value. A future
     * CRON could implement the actual "destroy heaven" mechanic.
     */
    public static final float REVIVED_HP = 1000.0F;

    private LiMuwanRevivalEvent() {}

    /**
     * Spawn Li Muwan as a living NPC at the player's position. Called
     * by {@link RevivalAttemptService#doSuccessfulRevival} after the
     * World Origin Essence is consumed.
     *
     * @param player     the server player (Wang Lin) at whose position
     *                   Li Muwan should spawn
     * @param currentTick the current game tick (for HistoryManager)
     * @return {@code true} if Li Muwan was successfully spawned;
     *         {@code false} if the spawn failed (logged at WARN level)
     */
    public static boolean spawnAtPlayer(ServerPlayer player, long currentTick) {
        ServerLevel level = player.serverLevel();

        // 1. Get the WorldRuntime singleton.
        WorldRuntime runtime;
        try {
            runtime = WorldRuntime.get();
        } catch (Throwable t) {
            Ergenverse.LOGGER.error("[Ergenverse] CRON-102: WorldRuntime not available — "
                    + "cannot spawn Li Muwan. Error: {}", t.getMessage());
            player.sendSystemMessage(Component.literal(
                    "李慕婉的元婴在天逆珠中叹息——世界运行时尚未就绪。")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        // 2. Get Li Muwan's ActorState from the NPCRuntime.
        NPCRuntime.ActorState state = runtime.npcs().getActor(CanonUUID.LI_MUWAN);
        if (state == null) {
            Ergenverse.LOGGER.error("[Ergenverse] CRON-102: Li Muwan's ActorState not found "
                    + "in NPCRuntime — cannot spawn.");
            player.sendSystemMessage(Component.literal(
                    "李慕婉的数据未在世界中注册。")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        // 3. Dematerialize existing Li Muwan entity (if any).
        //    She may be materialized at Luo He Sect from the NPCRuntime's
        //    day-0 registration. We dematerialize her there so we can
        //    re-materialize her at the player's position.
        if (runtime.npcs().isMaterialized(CanonUUID.LI_MUWAN)) {
            runtime.npcs().dematerializeActor(CanonUUID.LI_MUWAN, runtime);
            Ergenverse.LOGGER.info("[Ergenverse] CRON-102: Dematerialized existing Li Muwan "
                    + "entity (was at Luo He Sect) to re-spawn at player position.");
        }

        // 4. Update her ActorState to the player's position.
        //    This ensures future chunk reloads re-materialize her at the
        //    player's last known position.
        int playerX = player.blockPosition().getX();
        int playerZ = player.blockPosition().getZ();
        state.x = playerX;
        state.z = playerZ;

        // 4b. CRON-103: persist the revived state and clear the deadUntilRevived
        //     flag. Without this, CanonActorMaterializer would refuse to
        //     materialize her (step 5 below would return -1). The markActorRevived
        //     call writes her UUID into the WorldDeltaStore's revived-actor set,
        //     which is serialized to NBT on world save. On world reload,
        //     WorldRuntime.initialize applies the revived set to clear the
        //     deadUntilRevived flag for revived actors — so a revived Li Muwan
        //     stays alive across reloads.
        try {
            runtime.deltaStore().markActorRevived(CanonUUID.LI_MUWAN);
            runtime.npcs().markActorAlive(CanonUUID.LI_MUWAN);
            Ergenverse.LOGGER.info("[Ergenverse] CRON-103: marked Li Muwan as revived in WorldDeltaStore "
                    + "(persisted) and cleared deadUntilRevived flag in NPCRuntime.");
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-103: failed to persist revived-actor state: {}",
                    t.getMessage());
            // Non-fatal: we still attempt the materialization below. If it
            // fails because deadUntilRevived is still true, the failure path
            // below handles it. The next revival attempt will retry the persist.
        }

        // 5. Materialize Li Muwan at the player's position.
        int entityId = runtime.npcs().materializeActor(CanonUUID.LI_MUWAN, runtime);
        if (entityId < 0) {
            Ergenverse.LOGGER.error("[Ergenverse] CRON-102: CanonActorMaterializer failed to "
                    + "spawn Li Muwan at ({}, {}).", playerX, playerZ);
            player.sendSystemMessage(Component.literal(
                    "李慕婉的元婴未能凝聚成形。")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        // 6. Find the spawned entity and configure it.
        EntityCultivator liMuwan = findEntityById(level, entityId);
        if (liMuwan == null) {
            Ergenverse.LOGGER.error("[Ergenverse] CRON-102: Li Muwan entity (id={}) not found "
                    + "after materialization.", entityId);
            return false;
        }

        // 7. Set her cultivation realm to TRANSCENDENCE (踏天境).
        //    Canon: after revival, she transcends with Wang Lin.
        liMuwan.setCultivationRealm(REVIVED_REALM);

        // 8. Set her HP to the revived value (canon: immense power).
        try {
            liMuwan.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                    .setBaseValue(REVIVED_HP);
            liMuwan.setHealth(REVIVED_HP);
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-102: Failed to set Li Muwan's HP: {}",
                    t.getMessage());
        }

        // 9. Set her companion bond to the player's UUID.
        //    This activates the FollowPlayerGoal — she follows Wang Lin
        //    ("两人踏天同行").
        liMuwan.setFollowingPlayerUuid(player.getUUID().toString());

        // 10. Teleport her to the player's exact position (the materializer
        //     may have placed her at a heightmap position slightly off).
        liMuwan.moveTo(player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot());

        // 11. Display the spawn message.
        player.sendSystemMessage(Component.literal(
                "─────────────────────────────────────")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal(
                "李慕婉睁开了双眼。她踏虚而立，在你身旁。")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "「王林……」她轻声唤道，「我回来了。」")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "Li Muwan opens her eyes. She stands beside you, suspended in air.")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "\"Wang Lin...\" she whispers. \"I have returned.\"")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "她将与你踏天同行，超越生死轮回。")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "She will follow you beyond the cycle of life and death.")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal(
                "─────────────────────────────────────")
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        // 12. Record in HistoryManager.
        HistoryManager.onDiscovery(player, SUBJECT_LI_MUWAN_SPAWNED,
                "Li Muwan materialized as a living cultivator at Wang Lin's position. "
                        + "Her cultivation reached the Heaven-Trampling Realm (踏天境). "
                        + "She now follows Wang Lin as his eternal companion — \"两人踏天同行\".",
                currentTick);

        Ergenverse.LOGGER.info("[Ergenverse] CRON-102: Li Muwan spawned at ({}, {}, {}) for "
                        + "player {}. Realm={}, HP={}, followingPlayer={}.",
                player.getX(), player.getY(), player.getZ(),
                player.getName().getString(), REVIVED_REALM, REVIVED_HP,
                player.getUUID());

        return true;
    }

    /**
     * Find an entity by Minecraft entity ID in the server level.
     * (Helper — ServerLevel has no direct getEntity(int) in 1.20.1; iteration
     * is the supported approach. The entity count is small at spawn time.)
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
     * CRON-103: Migration helper for pre-CRON-103 saves.
     *
     * <p>Prior to CRON-103, Li Muwan was registered as a living NPC at Luo He
     * Sect from day 0 (no {@code deadUntilRevived} flag). A player who revived
     * her in a pre-CRON-103 save has a bead with {@code NBT_LI_MUWAN_REVIVED=true}
     * but the new {@link WorldDeltaStore#revivedActorUuids()} set is empty
     * (because the old code never wrote to it). On world reload with CRON-103,
     * Li Muwan would be re-flagged as dead and refuse to materialize — a
     * regression for that save.
     *
     * <p>This method scans the player's inventory (main + offhand) for a
     * Heaven-Defying Bead with {@code NBT_LI_MUWAN_REVIVED=true}. If found
     * AND {@link WorldDeltaStore#isActorRevived} returns false for Li Muwan,
     * the method writes her UUID into the revived-actor set (persisting it)
     * and clears her {@code deadUntilRevived} flag in the NPCRuntime.
     *
     * <p>Idempotent: if the revived-actor set already contains Li Muwan, this
     * is a no-op. Safe to call on every player login.
     *
     * <p>Called from {@link dev.ergenverse.spawn.SpawnEventHandler#onPlayerLogin}
     * after the WorldRuntime is initialized. This is a one-time migration —
     * once the revived-actor set is populated, subsequent logins skip the work.
     *
     * @param player the server player whose inventory to scan
     * @return true if a migration was performed (revived-actor set was empty
     *         and is now populated); false if no migration was needed
     */
    public static boolean migrateRevivedFlagIfNeeded(ServerPlayer player) {
        try {
            WorldRuntime runtime = WorldRuntime.get();
            if (!runtime.isInitialized()) return false;

            // If Li Muwan is already marked revived in the delta store, no migration needed.
            if (runtime.deltaStore().isActorRevived(CanonUUID.LI_MUWAN)) {
                return false;
            }

            // Scan the player's inventory for a Heaven-Defying Bead with NBT_LI_MUWAN_REVIVED=true.
            boolean foundRevivedBead = false;
            net.minecraft.world.entity.player.Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;
                if (!(stack.getItem() instanceof HeavenDefyingBeadItem bead)) continue;
                if (bead.isLiMuwanRevived(stack)) {
                    foundRevivedBead = true;
                    break;
                }
            }

            if (!foundRevivedBead) return false;

            // Migration: write Li Muwan's UUID into the revived-actor set and clear the flag.
            runtime.deltaStore().markActorRevived(CanonUUID.LI_MUWAN);
            runtime.npcs().markActorAlive(CanonUUID.LI_MUWAN);
            Ergenverse.LOGGER.info("[Ergenverse] CRON-103: migrated Li Muwan's revived state from bead NBT "
                    + "to WorldDeltaStore (pre-CRON-103 save detected for player {}).",
                    player.getName().getString());
            player.sendSystemMessage(Component.literal(
                    "李慕婉的复活状态已迁移至世界存档。")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            return true;
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-103: migrateRevivedFlagIfNeeded failed: {}",
                    t.getMessage());
            return false;
        }
    }
}
