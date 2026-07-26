#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-102 verification script.

Verifies the Li Muwan NPC spawn after successful revival — the emotional
capstone of the Li Muwan arc. Checks across 10 categories:

1. HeavenDefyingBeadItem — NBT_LI_MUWAN_REVIVED flag + accessors + tooltip
2. EntityCultivator — DATA_FOLLOWING_PLAYER_UUID field + accessors
3. FollowPlayerGoal — file, class, behavior logic
4. EntityCultivator.registerGoals — FollowPlayerGoal registered
5. LiMuwanRevivalEvent — file, class, spawn logic, canon messages
6. RevivalAttemptService — calls setLiMuwanRevived + LiMuwanRevivalEvent
7. Canon fidelity — 踏天同行, 弹指灭天, transcendence realm, no fabricated citations
8. Architecture — no direct WorldDeltaStore/WorldFacade/Layer access from event
9. HistoryManager — SUBJECT_LI_MUWAN_SPAWNED recorded
10. Integration — the full chain CRON-99 → 100 → 101 → 102
"""
import sys
import os
import re

FORGE = "/home/z/my-project/forge-mod"
SRC = f"{FORGE}/src/main/java"

failures = []
checks = 0

def check(cond, name):
    global checks
    checks += 1
    if cond:
        print(f"  ✓ {name}")
    else:
        print(f"  ✗ FAIL: {name}")
        failures.append(name)

def read(path):
    try:
        with open(path, "r", encoding="utf-8") as f:
            return f.read()
    except Exception:
        return ""

# ─── 1. HeavenDefyingBeadItem — NBT_LI_MUWAN_REVIVED ────────────────
print("\n[1] HeavenDefyingBeadItem — NBT_LI_MUWAN_REVIVED flag")
bead_path = f"{SRC}/dev/ergenverse/wanglin/bead/HeavenDefyingBeadItem.java"
bead = read(bead_path)
check("NBT_LI_MUWAN_REVIVED" in bead, "NBT_LI_MUWAN_REVIVED constant defined")
check('"Ergen.Bead.LiMuwanRevived"' in bead, "NBT tag string is Ergen.Bead.LiMuwanRevived")
check("CRON-COMPLETIONIST-102" in bead, "CRON-102 marker in javadoc")
check("两人踏天同行" in bead, "canon quote 两人踏天同行 in javadoc")
check("public boolean isLiMuwanRevived(ItemStack stack)" in bead, "isLiMuwanRevived getter")
check("public void setLiMuwanRevived(ItemStack stack, boolean revived)" in bead, "setLiMuwanRevived setter")
check("if (!revived) return;" in bead, "write-once guard (can't un-revive)")
check("Li Muwan: REVIVED" in bead, "tooltip shows 'Li Muwan: REVIVED'")
check("已复活" in bead, "tooltip shows 已复活 (Chinese)")

# ─── 2. EntityCultivator — DATA_FOLLOWING_PLAYER_UUID ───────────────
print("\n[2] EntityCultivator — DATA_FOLLOWING_PLAYER_UUID field")
ec_path = f"{SRC}/dev/ergenverse/entity/EntityCultivator.java"
ec = read(ec_path)
check("DATA_FOLLOWING_PLAYER_UUID" in ec, "DATA_FOLLOWING_PLAYER_UUID accessor defined")
check("EntityDataSerializers.STRING" in ec, "uses STRING serializer")
check("DATA_FOLLOWING_PLAYER_UUID, \"\")" in ec, "default is empty string in defineSynchedData")
check("public String getFollowingPlayerUuid()" in ec, "getFollowingPlayerUuid getter")
check("public void setFollowingPlayerUuid(String playerUuid)" in ec, "setFollowingPlayerUuid setter")
check("public boolean isFollowingPlayer()" in ec, "isFollowingPlayer helper")
check("CRON-COMPLETIONIST-102" in ec, "CRON-102 marker in EntityCultivator")
check("两人踏天同行" in ec, "canon quote 两人踏天同行 in javadoc")

# ─── 3. FollowPlayerGoal ────────────────────────────────────────────
print("\n[3] FollowPlayerGoal — file, class, behavior")
fpg_path = f"{SRC}/dev/ergenverse/entity/ai/FollowPlayerGoal.java"
fpg = read(fpg_path)
check(os.path.exists(fpg_path), "FollowPlayerGoal.java file exists")
check("package dev.ergenverse.entity.ai;" in fpg, "correct package")
check("public class FollowPlayerGoal extends Goal" in fpg, "extends Goal")
check("CRON-COMPLETIONIST-102" in fpg, "CRON-102 marker")
check("FOLLOW_DISTANCE" in fpg, "FOLLOW_DISTANCE constant")
check("PATHFIND_DISTANCE" in fpg, "PATHFIND_DISTANCE constant")
check("TELEPORT_DISTANCE" in fpg, "TELEPORT_DISTANCE constant")
check("两人踏天同行" in fpg, "canon quote 两人踏天同行 in javadoc")
check("弹指灭天" in fpg, "canon quote 弹指灭天 in javadoc")
check("踏天境" in fpg, "canon term 踏天境 (Heaven-Trampling Realm)")
check("NO fabricated chapter citation" in fpg, "canon honesty: no fabricated citation")
check("canUse" in fpg, "canUse method overridden")
check("canContinueToUse" in fpg, "canContinueToUse method overridden")
check("isFollowingPlayer()" in fpg, "checks isFollowingPlayer() in canUse")
check("getTarget() != null" in fpg, "yields to combat (checks getTarget)")
check("isActivityLocked()" in fpg, "respects activity lock")
check("teleportToPlayer" in fpg, "has teleportToPlayer method")
check("pathfindTowardPlayer" in fpg, "has pathfindTowardPlayer method")
check("DefaultRandomPos" in fpg, "uses DefaultRandomPos for pathfinding")
check("getNavigation().moveTo" in fpg, "uses navigation.moveTo")
check("getNavigation().stop()" in fpg, "stops navigation when close enough")

# ─── 4. EntityCultivator.registerGoals — FollowPlayerGoal registered ─
print("\n[4] EntityCultivator.registerGoals — FollowPlayerGoal registered")
check("FollowPlayerGoal(this)" in ec, "FollowPlayerGoal registered in goalSelector")
check("addGoal(4, new dev.ergenverse.entity.ai.FollowPlayerGoal(this))" in ec,
      "registered at priority 4 (between CognitionDrivenGoal=3 and NpcReactToWorld=5)")

# ─── 5. LiMuwanRevivalEvent ─────────────────────────────────────────
print("\n[5] LiMuwanRevivalEvent — file, class, spawn logic")
lre_path = f"{SRC}/dev/ergenverse/wanglin/bead/LiMuwanRevivalEvent.java"
lre = read(lre_path)
check(os.path.exists(lre_path), "LiMuwanRevivalEvent.java file exists")
check("package dev.ergenverse.wanglin.bead;" in lre, "correct package")
check("public final class LiMuwanRevivalEvent" in lre, "final class (utility pattern)")
check("private LiMuwanRevivalEvent() {}" in lre, "private constructor (utility class)")
check("CRON-COMPLETIONIST-102" in lre, "CRON-102 marker")
check("SUBJECT_LI_MUWAN_SPAWNED" in lre, "SUBJECT_LI_MUWAN_SPAWNED constant")
check('"li_muwan_spawned_as_companion"' in lre, "subject string correct")
check("REVIVED_REALM" in lre, "REVIVED_REALM constant")
check('"transcendence"' in lre, "realm is 'transcendence' (踏天境)")
check("REVIVED_HP" in lre, "REVIVED_HP constant")
check("1000.0F" in lre, "HP is 1000 (canon: immense power)")
check("public static boolean spawnAtPlayer(ServerPlayer player, long currentTick)" in lre,
      "spawnAtPlayer entry point signature")
check("WorldRuntime.get()" in lre, "accesses WorldRuntime singleton")
check("CanonUUID.LI_MUWAN" in lre, "uses CanonUUID.LI_MUWAN")
check("dematerializeActor" in lre, "dematerializes existing Li Muwan first")
check("state.x = playerX" in lre, "updates ActorState x to player position")
check("state.z = playerZ" in lre, "updates ActorState z to player position")
check("materializeActor" in lre, "materializes Li Muwan at new position")
check("setCultivationRealm(REVIVED_REALM)" in lre, "sets realm to transcendence")
check("setFollowingPlayerUuid" in lre, "sets following player UUID")
check("player.getUUID().toString()" in lre, "uses player's UUID for companion bond")
check("HistoryManager.onDiscovery" in lre, "records in HistoryManager")
check("两人踏天同行" in lre, "canon quote in spawn message")
check("我回来了" in lre, "Li Muwan's dialogue: 我回来了 (I have returned)")
check("NO fabricated chapter citation" in lre, "canon honesty: no fabricated citation")
check("弹指灭天" in lre, "canon detail 弹指灭天 in javadoc")
check("踏天境" in lre, "canon term 踏天境 in javadoc")

# ─── 6. RevivalAttemptService — calls LiMuwanRevivalEvent ───────────
print("\n[6] RevivalAttemptService — calls setLiMuwanRevived + LiMuwanRevivalEvent")
ras_path = f"{SRC}/dev/ergenverse/wanglin/bead/RevivalAttemptService.java"
ras = read(ras_path)
check("beadItem.setLiMuwanRevived(stack, true)" in ras,
      "calls setLiMuwanRevived(stack, true) on success")
check("LiMuwanRevivalEvent.spawnAtPlayer(player, currentTick)" in ras,
      "calls LiMuwanRevivalEvent.spawnAtPlayer")
check("CRON-COMPLETIONIST-102" in ras, "CRON-102 marker in service")
check("两人踏天同行" in ras, "canon quote in javadoc")
check("LiMuwanRevivalEvent" in ras, "references LiMuwanRevivalEvent")

# ─── 7. Canon fidelity ─────────────────────────────────────────────
print("\n[7] Canon fidelity — verified quotes and terms")
# All files combined
all_src = bead + ec + fpg + lre + ras
check("踏天同行" in all_src, "canon term 踏天同行 (transcend together)")
check("弹指灭天" in all_src, "canon detail 弹指灭天 (destroys heaven with a flick)")
check("踏天境" in all_src, "canon term 踏天境 (Heaven-Trampling Realm)")
check("transcendence" in all_src, "English realm name 'transcendence'")
check("一界本源" in all_src, "canon reagent 一界本源 mentioned")
check("天逆珠" in all_src or "Heaven-Defying Bead" in all_src,
      "bead referenced (天逆珠 or Heaven-Defying Bead)")

# ─── 8. Architecture — no direct world-state access ────────────────
print("\n[8] Architecture — no direct WorldDeltaStore/WorldFacade from event")
# LiMuwanRevivalEvent should NOT IMPORT or CALL WorldDeltaStore/WorldFacade.
# (It may mention them in javadoc saying it does NOT use them.)
check("import dev.ergenverse.runtime.delta.WorldDeltaStore" not in lre,
      "LiMuwanRevivalEvent does NOT import WorldDeltaStore")
check("import dev.ergenverse.runtime.layer.WorldFacade" not in lre,
      "LiMuwanRevivalEvent does NOT import WorldFacade")
check("WorldDeltaStore." not in lre, "LiMuwanRevivalEvent does NOT call WorldDeltaStore methods")
check("WorldFacade." not in lre, "LiMuwanRevivalEvent does NOT call WorldFacade methods")
check("world()." not in lre or "runtime.world()" not in lre,
      "LiMuwanRevivalEvent does NOT call runtime.world() (the facade)")
check("setPlayerBlock" not in lre, "LiMuwanRevivalEvent does NOT call setPlayerBlock")
check("setSimulationBlock" not in lre, "LiMuwanRevivalEvent does NOT call setSimulationBlock")
check("Provenance." not in lre, "LiMuwanRevivalEvent does NOT use Provenance (no delta)")
check("WorldRuntime.get()" in lre, "LiMuwanRevivalEvent accesses WorldRuntime (for NPCRuntime)")

# ─── 9. HistoryManager integration ─────────────────────────────────
print("\n[9] HistoryManager — SUBJECT_LI_MUWAN_SPAWNED recorded")
check("SUBJECT_LI_MUWAN_SPAWNED" in lre, "subject constant defined")
check('SUBJECT_LI_MUWAN_SPAWNED,' in lre, "subject used in onDiscovery call")
check("materialized as a living cultivator" in lre,
      "history description mentions 'living cultivator'")

# ─── 10. Integration — full chain CRON-99 → 100 → 101 → 102 ────────
print("\n[10] Integration — the full Li Muwan arc chain")
# CRON-99: soul capture (LiMuwanSoulCaptureEvent)
soul_path = f"{SRC}/dev/ergenverse/wanglin/bead/LiMuwanSoulCaptureEvent.java"
soul = read(soul_path)
check("hasLiMuwanSoul" in soul or "setLiMuwanSoul" in soul,
      "CRON-99: soul capture event exists (hasLiMuwanSoul/setLiMuwanSoul)")
# CRON-100: revival attempts
check("CANON_REVIVAL_ATTEMPT_CAP" in bead, "CRON-100: 137 attempt cap in bead")
check("NBT_REVIVAL_ATTEMPTS" in bead, "CRON-100: revival attempts NBT in bead")
# CRON-101: World Origin Essence
essence_path = f"{SRC}/dev/ergenverse/item/WorldOriginEssenceItem.java"
essence = read(essence_path)
check("WorldOriginEssenceItem" in essence, "CRON-101: World Origin Essence item exists")
check("NBT_SOURCE_WORLD" in essence, "CRON-101: NBT_SOURCE_WORLD in essence item")
# CRON-102: Li Muwan NPC spawn
check("LiMuwanRevivalEvent" in ras, "CRON-102: RevivalAttemptService calls LiMuwanRevivalEvent")
check("FollowPlayerGoal" in ec, "CRON-102: FollowPlayerGoal registered in EntityCultivator")
check("NBT_LI_MUWAN_REVIVED" in bead, "CRON-102: NBT_LI_MUWAN_REVIVED flag in bead")
# The chain: soul captured → 137 attempts → essence consumed → Li Muwan spawns
check("setLiMuwanSoul" in soul, "Step 1: CRON-99 setLiMuwanSoul (soul captured)")
check("NBT_REVIVAL_ATTEMPTS" in ras, "Step 2: CRON-100 revival attempts tracked in service")
check("WorldOriginEssenceItem" in ras, "Step 3: CRON-101 World Origin Essence in service")
check("LiMuwanRevivalEvent.spawnAtPlayer" in ras, "Step 4: CRON-102 Li Muwan spawns")

# ─── Final summary ──────────────────────────────────────────────────
print("\n" + "=" * 60)
if not failures:
    print(f"✅ ALL {checks} CHECKS PASSED.")
    sys.exit(0)
else:
    print(f"❌ {len(failures)} FAILURES out of {checks} checks:")
    for f in failures:
        print(f"   - {f}")
    sys.exit(1)
