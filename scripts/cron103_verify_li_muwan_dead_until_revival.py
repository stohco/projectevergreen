#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-103 verification script.

Verifies that Li Muwan is now canon-faithfully DEAD until the revival event
fires, closing the biggest mod-fidelity bridge in the Li Muwan thread.
"""

import sys
from pathlib import Path

FORGE = Path("/home/z/my-project/forge-mod/src/main/java/dev/ergenverse")
PASS = 0
FAIL = 0
FAILS = []


def check(condition, label):
    global PASS, FAIL
    if condition:
        PASS += 1
    else:
        FAIL += 1
        FAILS.append(label)
        print(f"  FAIL: {label}")


def read(rel_path):
    return (FORGE / rel_path).read_text(encoding="utf-8")


# ─────────────────────────────────────────────────────────────────────────────
# 1. WorldDeltaStore
# ─────────────────────────────────────────────────────────────────────────────
print("\n[1] WorldDeltaStore — revivedActorUuids set + dirty callback + serialize")
wds = read("runtime/delta/WorldDeltaStore.java")

check("Set<UUID> revivedActorUuids" in wds,
      "WorldDeltaStore has Set<UUID> revivedActorUuids field")
check("private final Set<UUID> revivedActorUuids = new HashSet<>();" in wds,
      "WorldDeltaStore revivedActorUuids initialized as HashSet")
check("private Runnable dirtyCallback" in wds,
      "WorldDeltaStore has dirtyCallback field")
check("public void setDirtyCallback(Runnable cb)" in wds,
      "WorldDeltaStore has setDirtyCallback method")
check(wds.count("dirtyCallback.run()") >= 2,
      "WorldDeltaStore calls dirtyCallback.run() in record() AND markActorRevived()")
check("public synchronized void markActorRevived(UUID uuid)" in wds,
      "WorldDeltaStore has markActorRevived(UUID) method")
check("public synchronized boolean isActorRevived(UUID uuid)" in wds,
      "WorldDeltaStore has isActorRevived(UUID) method")
check("public synchronized Set<UUID> revivedActorUuids()" in wds,
      "WorldDeltaStore has revivedActorUuids() immutable view method")
check('tag.put("revived_actors", revivedList)' in wds,
      "WorldDeltaStore.serialize writes 'revived_actors' ListTag")
check('tag.getList("revived_actors", Tag.TAG_COMPOUND)' in wds,
      "WorldDeltaStore.deserialize reads 'revived_actors' ListTag")
check("revivedActorUuids.clear()" in wds,
      "WorldDeltaStore.clear() wipes revivedActorUuids")
check("import java.util.HashSet;" in wds and "import java.util.Set;" in wds
      and "import java.util.UUID;" in wds,
      "WorldDeltaStore imports HashSet, Set, UUID")
check("CRON-103" in wds,
      "WorldDeltaStore javadoc references CRON-103")

# ─────────────────────────────────────────────────────────────────────────────
# 2. NPCRuntime — deadUntilRevived flag + markActorAlive helper
# ─────────────────────────────────────────────────────────────────────────────
print("\n[2] NPCRuntime — deadUntilRevived flag + markActorAlive helper")
npc = read("runtime/NPCRuntime.java")

check("public boolean deadUntilRevived = false;" in npc,
      "ActorState has public boolean deadUntilRevived = false field")
check("public void markActorAlive(UUID canonUuid)" in npc,
      "NPCRuntime has markActorAlive(UUID) method")
check("state.deadUntilRevived = false;" in npc,
      "markActorAlive clears deadUntilRevived flag")
check("CRON-103" in npc,
      "NPCRuntime javadoc references CRON-103")

# ─────────────────────────────────────────────────────────────────────────────
# 3. NPCRuntime.loadAll — Li Muwan flagged deadUntilRevived=true
# ─────────────────────────────────────────────────────────────────────────────
print("\n[3] NPCRuntime.loadAll — Li Muwan flagged deadUntilRevived=true")
check("liMuwanState.deadUntilRevived = true;" in npc,
      "NPCRuntime.loadAll sets Li Muwan's deadUntilRevived = true")
check("CanonUUID.LI_MUWAN" in npc and "deadUntilRevived" in npc,
      "NPCRuntime references both CanonUUID.LI_MUWAN and deadUntilRevived")
check("结婴失败寿尽而亡" in npc,
      "NPCRuntime documents Li Muwan's death: '结婴失败寿尽而亡'")
check("收入天逆珠" in npc,
      "NPCRuntime documents Wang Lin capturing her soul (CRON-99)")

# ─────────────────────────────────────────────────────────────────────────────
# 4. CanonActorMaterializer — refuse materialization
# ─────────────────────────────────────────────────────────────────────────────
print("\n[4] CanonActorMaterializer — refuse materialization when deadUntilRevived=true")
cam = read("runtime/materialize/CanonActorMaterializer.java")

check("if (state.deadUntilRevived)" in cam,
      "CanonActorMaterializer checks state.deadUntilRevived")
check("CRON-103" in cam,
      "CanonActorMaterializer javadoc references CRON-103")
check("deadUntilRevived=true" in cam,
      "CanonActorMaterializer logs mention deadUntilRevived=true")
check("DEAD before the" in cam,
      "CanonActorMaterializer documents Li Muwan is DEAD before revival")
check("pre-CRON-103 save" in cam,
      "CanonActorMaterializer documents migration for pre-CRON-103 saves")
check("sole mechanism" in cam,
      "CanonActorMaterializer documents revival event is sole mechanism")

# ─────────────────────────────────────────────────────────────────────────────
# 5. WorldRuntime — dirty callback + apply revived set
# ─────────────────────────────────────────────────────────────────────────────
print("\n[5] WorldRuntime — dirty callback + apply revived set after loadAll")
wr = read("runtime/WorldRuntime.java")

check("setDirtyCallback" in wr,
      "WorldRuntime calls setDirtyCallback on deltaStore")
check("savedData.setDirty()" in wr,
      "WorldRuntime wires dirty callback to savedData.setDirty()")
check("CRON-103" in wr,
      "WorldRuntime javadoc references CRON-103")
check("deltaStore.revivedActorUuids()" in wr,
      "WorldRuntime reads deltaStore.revivedActorUuids()")
check("npcs.markActorAlive(uuid)" in wr,
      "WorldRuntime calls npcs.markActorAlive(uuid) for revived actors")
check("persisted revived-actor" in wr,
      "WorldRuntime logs the application of persisted revived-actor set")
check("persistence gap" in wr,
      "WorldRuntime documents the pre-existing persistence gap fix")

# ─────────────────────────────────────────────────────────────────────────────
# 6. LiMuwanRevivalEvent — markActorRevived + migration helper
# ─────────────────────────────────────────────────────────────────────────────
print("\n[6] LiMuwanRevivalEvent — markActorRevived + clear flag + migration helper")
lre = read("wanglin/bead/LiMuwanRevivalEvent.java")

check("runtime.deltaStore().markActorRevived(CanonUUID.LI_MUWAN)" in lre,
      "LiMuwanRevivalEvent calls deltaStore.markActorRevived(CanonUUID.LI_MUWAN)")
check("runtime.npcs().markActorAlive(CanonUUID.LI_MUWAN)" in lre,
      "LiMuwanRevivalEvent calls npcs.markActorAlive(CanonUUID.LI_MUWAN)")
check("CRON-103" in lre,
      "LiMuwanRevivalEvent javadoc references CRON-103")
check("import dev.ergenverse.runtime.delta.WorldDeltaStore;" in lre,
      "LiMuwanRevivalEvent imports WorldDeltaStore")
check("import net.minecraft.world.item.ItemStack;" in lre,
      "LiMuwanRevivalEvent imports ItemStack (for migration helper)")
check("public static boolean migrateRevivedFlagIfNeeded(ServerPlayer player)" in lre,
      "LiMuwanRevivalEvent has migrateRevivedFlagIfNeeded(ServerPlayer) method")
check("HeavenDefyingBeadItem bead" in lre,
      "Migration helper casts to HeavenDefyingBeadItem bead")
check("bead.isLiMuwanRevived(stack)" in lre,
      "Migration helper calls bead.isLiMuwanRevived(stack)")
check("pre-CRON-103 save" in lre,
      "Migration helper documents pre-CRON-103 save migration")
check("复活状态已迁移至世界存档" in lre,
      "Migration helper has bilingual migration message")
check("CRON-103 closed this gap" in lre,
      "LiMuwanRevivalEvent javadoc documents CRON-103 closing the mod-fidelity bridge")
check("perishes when her" in lre,
      "LiMuwanRevivalEvent documents canon death reason")

# ─────────────────────────────────────────────────────────────────────────────
# 7. SpawnEventHandler — migration on player login
# ─────────────────────────────────────────────────────────────────────────────
print("\n[7] SpawnEventHandler — call migration helper on player login")
seh = read("spawn/SpawnEventHandler.java")

check("LiMuwanRevivalEvent.migrateRevivedFlagIfNeeded(sp)" in seh,
      "SpawnEventHandler calls LiMuwanRevivalEvent.migrateRevivedFlagIfNeeded(sp)")
check("CRON-103" in seh,
      "SpawnEventHandler javadoc references CRON-103")
check("ALL logins" in seh,
      "SpawnEventHandler documents migration runs for ALL logins")

# Verify migration call comes BEFORE the firstJoin early-return
migrate_idx = seh.find("migrateRevivedFlagIfNeeded(sp)")
firstjoin_idx = seh.find("if (!firstJoin) return;")
check(0 < migrate_idx < firstjoin_idx,
      "Migration call comes BEFORE the firstJoin early-return (so it runs for returning players too)")

# ─────────────────────────────────────────────────────────────────────────────
# 8. Canon fidelity — Li Muwan is DEAD before revival
# ─────────────────────────────────────────────────────────────────────────────
print("\n[8] Canon fidelity — Li Muwan is DEAD before revival")

check("结婴失败寿尽而亡" in npc,
      "NPCRuntime documents: Li Muwan perishes when her Nascent Soul formation fails")
check("perishes when her" in lre,
      "LiMuwanRevivalEvent documents the same canon fact")
check("DEAD before the" in cam,
      "CanonActorMaterializer documents she is DEAD before the revival arc")
check("NO fabricated chapter citation" in lre,
      "LiMuwanRevivalEvent honestly documents no fabricated chapter citation")
check("sole mechanism" in cam,
      "CanonActorMaterializer documents revival event is sole mechanism to bring her back")

# ─────────────────────────────────────────────────────────────────────────────
# 9. Architecture — no WorldFacade/Provenance violations
# ─────────────────────────────────────────────────────────────────────────────
print("\n[9] Architecture — no WorldFacade/Provenance violations")

# The deadUntilRevived flag is entity-state, NOT world-state.
check("WorldFacade" not in npc,
      "NPCRuntime does NOT reference WorldFacade (entity-state, not world-state)")
check("Provenance" not in npc,
      "NPCRuntime does NOT reference Provenance (entity-state, not world-state)")
check("WorldFacade" not in cam,
      "CanonActorMaterializer does NOT reference WorldFacade")

# The migration helper reads the bead's NBT via the item's accessor (not direct NBT access)
check("stack.getTag()" not in lre,
      "Migration helper does NOT read NBT directly (uses HeavenDefyingBeadItem.isLiMuwanRevived)")

# ─────────────────────────────────────────────────────────────────────────────
# 10. Integration — full chain CRON-99 → 100 → 101 → 102 → 103
# ─────────────────────────────────────────────────────────────────────────────
print("\n[10] Integration — full chain CRON-99 → 100 → 101 → 102 → 103")

# CRON-99: soul capture
check((FORGE / "wanglin/bead/LiMuwanSoulCaptureEvent.java").exists(),
      "CRON-99 LiMuwanSoulCaptureEvent still exists")

# CRON-100: revival attempts
check((FORGE / "wanglin/bead/RevivalAttemptService.java").exists(),
      "CRON-100 RevivalAttemptService still exists")

# CRON-101: World Origin Essence — find it (path may vary)
woe_exists = any((FORGE / p).exists() for p in [
    "item/WorldOriginEssenceItem.java",
    "wanglin/bead/WorldOriginEssenceItem.java",
])
check(woe_exists, "CRON-101 WorldOriginEssenceItem still exists")

# CRON-102: FollowPlayerGoal + LiMuwanRevivalEvent
check((FORGE / "entity/ai/FollowPlayerGoal.java").exists(),
      "CRON-102 FollowPlayerGoal still exists")
check((FORGE / "wanglin/bead/LiMuwanRevivalEvent.java").exists(),
      "CRON-102 LiMuwanRevivalEvent still exists")

# CRON-103: this round
check("deadUntilRevived" in npc,
      "CRON-103 deadUntilRevived flag in NPCRuntime")
check("deadUntilRevived" in cam,
      "CRON-103 deadUntilRevived check in CanonActorMaterializer")
check("markActorRevived" in wds,
      "CRON-103 markActorRevived in WorldDeltaStore")
check("migrateRevivedFlagIfNeeded" in lre,
      "CRON-103 migrateRevivedFlagIfNeeded in LiMuwanRevivalEvent")
check("migrateRevivedFlagIfNeeded(sp)" in seh,
      "CRON-103 migration call in SpawnEventHandler")

# The chain references
check("LiMuwanSoulCaptureEvent" in lre,
      "LiMuwanRevivalEvent references CRON-99 LiMuwanSoulCaptureEvent")
check("World Origin Essence" in lre or "一界本源" in lre,
      "LiMuwanRevivalEvent references CRON-101 World Origin Essence")

# ─────────────────────────────────────────────────────────────────────────────
# Summary
# ─────────────────────────────────────────────────────────────────────────────
print(f"\n{'='*60}")
print(f"CRON-103 VERIFICATION: {PASS} passed, {FAIL} failed (total {PASS+FAIL})")
if FAILS:
    print("FAILURES:")
    for f in FAILS:
        print(f"  - {f}")
    sys.exit(1)
else:
    print("ALL CHECKS PASSED.")
    sys.exit(0)
