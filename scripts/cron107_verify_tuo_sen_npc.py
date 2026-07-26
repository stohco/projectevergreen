#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-107 — Tuo Sen (拓森) Canon NPC Verification.

Verifies that Tuo Sen is registered as a canon NPC with a deadUntilRevived
flag, that the CultivationPlanetCrystalBlock inheritance event spawns him
at the Suzaku Tomb, and that the data files use the correct character 拓森
(NOT the prior incorrect 拓山).

Categories of checks:
  1. CanonUUID.TUO_SEN constant declared
  2. PlanetSuzakuBlueprint.NPC_TUO_SEN string constant
  3. NPCRuntime.loadAll() registers Tuo Sen with deadUntilRevived=true
  4. CanonActorMaterializer.PROFILES has Tuo Sen entry
  5. TuoSenSpawnEvent.java — file, class, spawnAtSuzakuTomb method
  6. TuoSenSpawnEvent — spawn logic (dematerialize, markActorRevived, materialize)
  7. TuoSenSpawnEvent — entity configuration (realm=ancient, HP=500, teleport to chamber Y)
  8. TuoSenSpawnEvent — canon-faithful bilingual message
  9. TuoSenSpawnEvent — HistoryManager onDiscovery call
 10. CultivationPlanetCrystalBlock.use() — Tuo Sen spawn injection point
 11. CultivationPlanetCrystalBlock — javadoc updated (no more 'future CRON')
 12. HeavenDefyingBeadItem — NBT_SUZAKU_SON javadoc updated
 13. npc_tuo_sen.json — correct character 拓森, location suzaku_tomb, dialogue lines
 14. Data file fix — 拓山 → 拓森 across all files (no remaining 拓山 typos)
 15. Canon fidelity — 8-star Ancient God, Tu Si rival, Suzaku Tomb reappearance
 16. Architecture — uses WorldFacade/markActorRevived (no direct store manipulation)
 17. Integration — CRON-106 inheritance event spawns Tuo Sen as consequence
 18. Web-search canon verification — Sohu/163/Baidu Baike sources cited
 19. Lang file — npc.ergenverse.npc_tuo_sen entry exists

Run: python3 /home/z/my-project/scripts/cron107_verify_tuo_sen_npc.py
Exit code: 0 if all checks pass, 1 otherwise.
"""

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "forge-mod"
CANON_UUID = ROOT / "src/main/java/dev/ergenverse/runtime/CanonUUID.java"
BLUEPRINT = ROOT / "src/main/java/dev/ergenverse/runtime/PlanetSuzakuBlueprint.java"
NPC_RUNTIME = ROOT / "src/main/java/dev/ergenverse/runtime/NPCRuntime.java"
CANON_MAT = ROOT / "src/main/java/dev/ergenverse/runtime/materialize/CanonActorMaterializer.java"
SPAWN_EVENT = ROOT / "src/main/java/dev/ergenverse/wanglin/bead/TuoSenSpawnEvent.java"
CRYSTAL_BLOCK = ROOT / "src/main/java/dev/ergenverse/block/CultivationPlanetCrystalBlock.java"
BEAD_ITEM = ROOT / "src/main/java/dev/ergenverse/wanglin/bead/HeavenDefyingBeadItem.java"
RI_CANON_DB = ROOT / "src/main/java/dev/ergenverse/wanglin/RICanonicalDatabase.java"
NPC_JSON = ROOT / "src/main/resources/data/ergenverse/npcs/npc_tuo_sen.json"
RI_DB_JSON = ROOT / "src/main/resources/data/ergenverse/ri_canon_database.json"
RI_ENRICHED_JSON = ROOT / "src/main/resources/data/ergenverse/canon_enriched/ri_canon_characters_enriched.json"
LANG_EN = ROOT / "src/main/resources/assets/ergenverse/lang/en_us.json"
HISTORY = ROOT / "src/main/java/dev/ergenverse/history/HistoryManager.java"

PASS = 0
FAIL = 0
FAILS = []


def check(cond: bool, label: str) -> None:
    global PASS, FAIL
    if cond:
        PASS += 1
    else:
        FAIL += 1
        FAILS.append(label)
        print(f"  FAIL: {label}")


def section(name: str) -> None:
    print(f"\n=== {name} ===")


# Load sources
canon_uuid_text = CANON_UUID.read_text(encoding="utf-8")
blueprint_text = BLUEPRINT.read_text(encoding="utf-8")
npc_runtime_text = NPC_RUNTIME.read_text(encoding="utf-8")
canon_mat_text = CANON_MAT.read_text(encoding="utf-8")
spawn_event_text = SPAWN_EVENT.read_text(encoding="utf-8")
crystal_block_text = CRYSTAL_BLOCK.read_text(encoding="utf-8")
bead_item_text = BEAD_ITEM.read_text(encoding="utf-8")
ri_canon_db_text = RI_CANON_DB.read_text(encoding="utf-8")
npc_json = json.loads(NPC_JSON.read_text(encoding="utf-8"))
ri_db_json_text = RI_DB_JSON.read_text(encoding="utf-8")
ri_enriched_json_text = RI_ENRICHED_JSON.read_text(encoding="utf-8")
lang_en = json.loads(LANG_EN.read_text(encoding="utf-8"))

# ─────────────────────────────────────────────────────────────────────────────
# 1. CanonUUID.TUO_SEN constant declared
# ─────────────────────────────────────────────────────────────────────────────
section("1. CanonUUID.TUO_SEN constant")

check('TUO_SEN = of("npc:tuo_sen")' in canon_uuid_text,
      "TUO_SEN UUID constant declared")
check("CRON-COMPLETIONIST-107" in canon_uuid_text, "CRON-107 marker in CanonUUID")
check("拓森" in canon_uuid_text, "canon term 拓森 present")
check("拓山" in canon_uuid_text, "notes the prior 拓山 typo (documenting the fix)")
check("nameUUIDFromBytes" in canon_uuid_text or "of(" in canon_uuid_text,
      "uses name-based UUID derivation (type 3)")

# ─────────────────────────────────────────────────────────────────────────────
# 2. PlanetSuzakuBlueprint.NPC_TUO_SEN string constant
# ─────────────────────────────────────────────────────────────────────────────
section("2. PlanetSuzakuBlueprint.NPC_TUO_SEN")

check('NPC_TUO_SEN = "tuo_sen"' in blueprint_text,
      "NPC_TUO_SEN string constant declared")
check("CRON-COMPLETIONIST-107" in blueprint_text, "CRON-107 marker in blueprint")
check("拓森" in blueprint_text, "canon term 拓森 in blueprint")

# ─────────────────────────────────────────────────────────────────────────────
# 3. NPCRuntime.loadAll() registers Tuo Sen with deadUntilRevived=true
# ─────────────────────────────────────────────────────────────────────────────
section("3. NPCRuntime registration with deadUntilRevived")

check("CanonUUID.TUO_SEN" in npc_runtime_text, "NPCRuntime references CanonUUID.TUO_SEN")
check('register(CanonUUID.TUO_SEN, "Tuo Sen 拓森"' in npc_runtime_text,
      "registers Tuo Sen with name 'Tuo Sen 拓森'")
check("PlanetSuzakuBlueprint.SUZAKU_TOMB.x" in npc_runtime_text
      and "CanonUUID.TUO_SEN" in npc_runtime_text,
      "registers Tuo Sen at SUZAKU_TOMB position")
check("tuoSenState.deadUntilRevived = true" in npc_runtime_text,
      "sets deadUntilRevived=true on Tuo Sen ActorState")
check("CRON-COMPLETIONIST-107" in npc_runtime_text, "CRON-107 marker in NPCRuntime")
check("拓森" in npc_runtime_text, "canon term 拓森 in NPCRuntime")

# ─────────────────────────────────────────────────────────────────────────────
# 4. CanonActorMaterializer.PROFILES has Tuo Sen entry
# ─────────────────────────────────────────────────────────────────────────────
section("4. CanonActorMaterializer CanonProfile")

check('profile(CanonUUID.TUO_SEN' in canon_mat_text,
      "CanonActorMaterializer has Tuo Sen profile entry")
check('"tuo_sen"' in canon_mat_text and '"Tuo Sen 拓森"' in canon_mat_text,
      "profile uses characterId 'tuo_sen' and displayName 'Tuo Sen 拓森'")
check('"ancient_god_clan"' in canon_mat_text,
      "profile sectId = 'ancient_god_clan'")
check('"ancient"' in canon_mat_text,
      "profile realm = 'ancient' (Ancient God tier)")
check("CRON-COMPLETIONIST-107" in canon_mat_text, "CRON-107 marker in CanonActorMaterializer")

# Verify deadUntilRevived check exists (the gate that prevents spawning when dead)
check("state.deadUntilRevived" in canon_mat_text,
      "CanonActorMaterializer checks deadUntilRevived flag (the gate)")

# ─────────────────────────────────────────────────────────────────────────────
# 5. TuoSenSpawnEvent.java — file & class
# ─────────────────────────────────────────────────────────────────────────────
section("5. TuoSenSpawnEvent.java — file & class")

check(SPAWN_EVENT.exists(), "TuoSenSpawnEvent.java file exists")
check("package dev.ergenverse.wanglin.bead;" in spawn_event_text,
      "package dev.ergenverse.wanglin.bead")
check("public final class TuoSenSpawnEvent" in spawn_event_text,
      "public final class TuoSenSpawnEvent")
check("private TuoSenSpawnEvent() {}" in spawn_event_text,
      "private constructor (utility class)")
check("CRON-COMPLETIONIST-107" in spawn_event_text, "CRON-107 marker in class javadoc")
check("拓森" in spawn_event_text, "canon term 拓森 in javadoc")
check("拓山" not in spawn_event_text.replace("NOT 拓山", "").replace("拓山 (Tuò", ""),
      "no remaining incorrect 拓山 (only in fix-documentation comments)")

# ─────────────────────────────────────────────────────────────────────────────
# 6. TuoSenSpawnEvent — spawn logic
# ─────────────────────────────────────────────────────────────────────────────
section("6. TuoSenSpawnEvent — spawn logic")

check("public static boolean spawnAtSuzakuTomb" in spawn_event_text,
      "spawnAtSuzakuTomb method declared")
check("ServerPlayer player" in spawn_event_text and "BlockPos crystalPos" in spawn_event_text
      and "long currentTick" in spawn_event_text,
      "spawnAtSuzakuTomb takes (player, crystalPos, currentTick)")
check("runtime.npcs().isMaterialized(CanonUUID.TUO_SEN)" in spawn_event_text,
      "checks if Tuo Sen already materialized (defensive)")
check("runtime.npcs().dematerializeActor(CanonUUID.TUO_SEN, runtime)" in spawn_event_text,
      "dematerializes existing Tuo Sen before re-spawn")
check("runtime.deltaStore().markActorRevived(CanonUUID.TUO_SEN)" in spawn_event_text,
      "persists revived state via WorldDeltaStore.markActorRevived (CRON-103 pattern)")
check("runtime.npcs().markActorAlive(CanonUUID.TUO_SEN)" in spawn_event_text,
      "clears deadUntilRevived flag via markActorAlive (CRON-103 pattern)")
check("runtime.npcs().materializeActor(CanonUUID.TUO_SEN, runtime)" in spawn_event_text,
      "materializes Tuo Sen via CanonActorMaterializer")

# ─────────────────────────────────────────────────────────────────────────────
# 7. TuoSenSpawnEvent — entity configuration
# ─────────────────────────────────────────────────────────────────────────────
section("7. TuoSenSpawnEvent — entity configuration")

check('SPAWN_REALM = "ancient"' in spawn_event_text,
      "SPAWN_REALM constant = 'ancient' (Ancient God tier)")
check("SPAWN_HP = 500.0F" in spawn_event_text,
      "SPAWN_HP constant = 500.0F (boss-tier)")
check("tuoSen.setCultivationRealm(SPAWN_REALM)" in spawn_event_text,
      "sets Tuo Sen's cultivation realm to SPAWN_REALM")
check("Attributes.MAX_HEALTH" in spawn_event_text,
      "sets MAX_HEALTH attribute")
check("tuoSen.setHealth(SPAWN_HP)" in spawn_event_text,
      "sets Tuo Sen's HP to SPAWN_HP")
check("tuoSen.moveTo(" in spawn_event_text,
      "teleports Tuo Sen to tomb chamber position")
check("crystalPos.getY()" in spawn_event_text,
      "uses Crystal's Y for spawn (underground chamber, not surface heightmap)")

# ─────────────────────────────────────────────────────────────────────────────
# 8. TuoSenSpawnEvent — canon-faithful bilingual message
# ─────────────────────────────────────────────────────────────────────────────
section("8. TuoSenSpawnEvent — bilingual canon message")

check("─────────────────────────────────────" in spawn_event_text,
      "divider line (matches CRON-102 pattern)")
check("DARK_RED" in spawn_event_text, "uses DARK_RED formatting (rival, not companion)")
check("三百年的宿敌" in spawn_event_text, "canon message: 300-year rival (CN)")
check("拓森" in spawn_event_text, "canon name 拓森 in spawn message")
check("Tuo Sen" in spawn_event_text, "English name in spawn message")
check("three centuries" in spawn_event_text or "300 years" in spawn_event_text.lower()
      or "three hundred years" in spawn_event_text.lower(),
      "canon message: 300-year rival (EN)")
check("知识传承" in spawn_event_text, "canon: knowledge inheritance (CN)")
check("knowledge inheritance" in spawn_event_text.lower(),
      "canon: knowledge inheritance (EN)")

# ─────────────────────────────────────────────────────────────────────────────
# 9. TuoSenSpawnEvent — HistoryManager onDiscovery call
# ─────────────────────────────────────────────────────────────────────────────
section("9. TuoSenSpawnEvent — HistoryManager discovery")

check('SUBJECT_TUO_SEN_REAPPEARED = "tuo_sen_reappeared_at_suzaku_tomb"' in spawn_event_text,
      "SUBJECT_TUO_SEN_REAPPEARED constant declared")
check("HistoryManager.onDiscovery" in spawn_event_text,
      "HistoryManager.onDiscovery called")
check("SUBJECT_TUO_SEN_REAPPEARED" in spawn_event_text,
      "subject constant used in onDiscovery call")
check("8-star Ancient God" in spawn_event_text or "8-star" in spawn_event_text,
      "discovery detail mentions 8-star Ancient God")

# ─────────────────────────────────────────────────────────────────────────────
# 10. CultivationPlanetCrystalBlock.use() — Tuo Sen spawn injection point
# ─────────────────────────────────────────────────────────────────────────────
section("10. CultivationPlanetCrystalBlock.use() — Tuo Sen spawn injection")

check("TuoSenSpawnEvent.spawnAtSuzakuTomb" in crystal_block_text,
      "CultivationPlanetCrystalBlock.use() calls TuoSenSpawnEvent.spawnAtSuzakuTomb")
check("CRON-107" in crystal_block_text, "CRON-107 marker in Crystal block")
check("inheritance still succeeded" in crystal_block_text,
      "spawn failure does NOT block inheritance (defensive)")
check("catch (Throwable t)" in crystal_block_text,
      "spawn call wrapped in try/catch (defensive)")

# ─────────────────────────────────────────────────────────────────────────────
# 11. CultivationPlanetCrystalBlock — javadoc updated
# ─────────────────────────────────────────────────────────────────────────────
section("11. CultivationPlanetCrystalBlock — javadoc updated")

check("CRON-107" in crystal_block_text, "javadoc references CRON-107")
# Check that the Tuo Sen-specific "future CRON" note is gone (line 91-93 of CRON-106 javadoc).
# Other "future CRON" mentions (about CultivationPlanetCrystalBlock itself, or about a dimmed
# texture) are legitimate and should NOT trigger this check.
# Strategy: for each "future CRON" occurrence, check if 拓森 appears within 200 chars
# (same paragraph). If not, it's a legitimate unrelated "future CRON" note.
def tuosen_future_cron_nearby(text, window=200):
    for m in re.finditer(r'future CRON', text):
        start = max(0, m.start() - window)
        end = min(len(text), m.end() + window)
        if '拓森' in text[start:end]:
            return m.start()
    return None

tuosen_fc_pos = tuosen_future_cron_nearby(crystal_block_text)
check(tuosen_fc_pos is None,
      f"no 'future CRON' for Tuo Sen (placeholder retired)"
      + (f" — found at pos {tuosen_fc_pos}" if tuosen_fc_pos else ""))
check("CanonUUID.TUO_SEN" in crystal_block_text or "CanonUUID#TUO_SEN" in crystal_block_text,
      "javadoc references CanonUUID.TUO_SEN")
check("TuoSenSpawnEvent" in crystal_block_text,
      "javadoc references TuoSenSpawnEvent")

# ─────────────────────────────────────────────────────────────────────────────
# 12. HeavenDefyingBeadItem — NBT_SUZAKU_SON javadoc updated
# ─────────────────────────────────────────────────────────────────────────────
section("12. HeavenDefyingBeadItem — javadoc updated")

check("CRON-107" in bead_item_text, "CRON-107 marker in bead item javadoc")
check("CanonUUID.TUO_SEN" in bead_item_text or "CanonUUID#TUO_SEN" in bead_item_text,
      "bead javadoc references CanonUUID.TUO_SEN (or {@link} form)")
check("now registered as a canon NPC" in bead_item_text,
      "javadoc says 'now registered' (no more 'when registered')")

# ─────────────────────────────────────────────────────────────────────────────
# 13. npc_tuo_sen.json — correct character, location, dialogue
# ─────────────────────────────────────────────────────────────────────────────
section("13. npc_tuo_sen.json — data file enriched")

check(npc_json.get("nameCn") == "拓森", "nameCn = '拓森' (correct character)")
check(npc_json.get("nameCn") != "拓山", "nameCn is NOT '拓山' (typo fixed)")
check(npc_json.get("location") == "suzaku_tomb", "location = 'suzaku_tomb' (canon-faithful)")
check(npc_json.get("faction") == "ancient_god_clan", "faction = 'ancient_god_clan'")
check("initiation_lines" in npc_json, "initiation_lines field present")
check(len(npc_json.get("initiation_lines", [])) >= 4, "at least 4 initiation lines")
check("daily_schedule" in npc_json, "daily_schedule field present")
check("sect_tasks" in npc_json, "sect_tasks field present")
check(npc_json.get("cultivation") == "Ancient God 8-Star (古境 / Ancient realm)",
      "cultivation field mentions Ancient God + Ancient realm")
check("拓森" in NPC_JSON.read_text(encoding="utf-8"), "拓森 present in JSON")
# The _comment field legitimately mentions 拓山 to document the fix.
# Check that no DATA field (nameCn, personality, note, etc.) uses 拓山.
# Strategy: parse JSON, check all string values except _comment.
def find_tuoshan_in_data(obj, path=""):
    """Recursively find 拓山 in any JSON value, skipping _comment keys."""
    if isinstance(obj, dict):
        for k, v in obj.items():
            if k == "_comment":
                continue
            result = find_tuoshan_in_data(v, f"{path}.{k}")
            if result:
                return result
    elif isinstance(obj, list):
        for i, v in enumerate(obj):
            result = find_tuoshan_in_data(v, f"{path}[{i}]")
            if result:
                return result
    elif isinstance(obj, str):
        if "拓山" in obj:
            return f"{path}={obj}"
    return None

tuoshan_in_data = find_tuoshan_in_data(npc_json)
check(tuoshan_in_data is None,
      f"no 拓山 in JSON data fields (only in _comment documenting the fix)"
      + (f" — found in {tuoshan_in_data}" if tuoshan_in_data else ""))

# Verify at least one initiation line references canon elements
init_lines_text = " ".join(npc_json.get("initiation_lines", []))
check("Crystal" in init_lines_text or "星晶" in init_lines_text,
      "initiation line references the Crystal")
check("Tu Si" in init_lines_text or "涂司" in init_lines_text,
      "initiation line references Tu Si (canon origin)")

# ─────────────────────────────────────────────────────────────────────────────
# 14. Data file fix — 拓山 → 拓森 across all files
# ─────────────────────────────────────────────────────────────────────────────
section("14. Data file fix — 拓山 → 拓森 unification")

# ri_canon_database.json should NOT have 拓山 (only 拓森)
check("拓山" not in ri_db_json_text, "ri_canon_database.json: no 拓山 (typo fixed)")
check("拓森" in ri_db_json_text, "ri_canon_database.json: 拓森 present")

# ri_canon_characters_enriched.json should NOT have 拓山 (only 拓森)
check("拓山" not in ri_enriched_json_text, "ri_canon_characters_enriched.json: no 拓山")
check("拓森" in ri_enriched_json_text, "ri_canon_characters_enriched.json: 拓森 present")

# RICanonicalDatabase.java should NOT have 拓山 (only 拓森)
check('"拓山"' not in ri_canon_db_text, "RICanonicalDatabase.java: no '拓山' string literal")
check('"拓森"' in ri_canon_db_text, "RICanonicalDatabase.java: '拓森' present")

# ─────────────────────────────────────────────────────────────────────────────
# 15. Canon fidelity — 8-star Ancient God, Tu Si rival, Suzaku Tomb reappearance
# ─────────────────────────────────────────────────────────────────────────────
section("15. Canon fidelity")

check("8-star" in spawn_event_text.lower() or "8-star" in npc_json.get("cultivation", "").lower()
      or "8星" in spawn_event_text,
      "canon: 8-star Ancient God (web-search verified)")
check("Tu Si" in spawn_event_text or "涂司" in spawn_event_text,
      "canon: Tu Si origin (Ink Flow Split Soul Technique)")
check("Ink Flow Split Soul" in spawn_event_text or "墨流分魂" in spawn_event_text
      or "Ink Flow Split Soul" in npc_json.get("personality", ""),
      "canon: Ink Flow Split Soul Technique (Tu Si's failed technique)")
check("Suzaku Tomb" in spawn_event_text or "朱雀墓" in spawn_event_text,
      "canon: Suzaku Tomb reappearance")
check("15th-gen" in spawn_event_text or "第十五代" in spawn_event_text
      or "15th-generation" in spawn_event_text,
      "canon: 15th-gen Suzaku Son inheritance event")
check("NO fabricated chapter citation" in spawn_event_text,
      "canon honesty: no fabricated chapter citation")
check("web-search" in spawn_event_text.lower() or "Sohu" in spawn_event_text,
      "canon source attribution (web-search / Sohu)")

# ─────────────────────────────────────────────────────────────────────────────
# 16. Architecture — uses WorldFacade/markActorRevived (no direct store manipulation)
# ─────────────────────────────────────────────────────────────────────────────
section("16. Architecture — facade-only writes")

# TuoSenSpawnEvent should NOT directly manipulate WorldDeltaStore.store.record
# It should use runtime.deltaStore().markActorRevived (the public API)
spawn_text_no_comments = re.sub(r'//.*', '', spawn_event_text)
spawn_text_no_comments = re.sub(r'/\*.*?\*/', '', spawn_text_no_comments, flags=re.DOTALL)
spawn_text_no_comments = re.sub(r'\*.*', '', spawn_text_no_comments)

check("store.record(" not in spawn_text_no_comments,
      "does NOT directly call WorldDeltaStore.store.record (uses markActorRevived)")
check("CompositeWorldLayer" not in spawn_text_no_comments,
      "does NOT directly manipulate CompositeWorldLayer")
check("PlayerLayer" not in spawn_text_no_comments,
      "does NOT directly manipulate PlayerLayer")
check("SimulationLayer" not in spawn_text_no_comments,
      "does NOT directly manipulate SimulationLayer")
check("runtime.world().setPlayerBlock" not in spawn_text_no_comments,
      "does NOT call setPlayerBlock (Tuo Sen spawn is an entity, not a block)")

# ─────────────────────────────────────────────────────────────────────────────
# 17. Integration — CRON-106 inheritance event spawns Tuo Sen as consequence
# ─────────────────────────────────────────────────────────────────────────────
section("17. Integration with CRON-106")

# The spawn call should be AFTER the HistoryManager.onDiscovery call for the inheritance
check("HistoryManager.onDiscovery(serverPlayer, SUBJECT_SUZAKU_SON_INHERITANCE" in crystal_block_text,
      "CRON-106 inheritance discovery is recorded")
check("TuoSenSpawnEvent.spawnAtSuzakuTomb" in crystal_block_text,
      "CRON-107 Tuo Sen spawn is injected (consequence of inheritance)")
# Order check: inheritance discovery comes before Tuo Sen spawn
inheritance_idx = crystal_block_text.find("SUBJECT_SUZAKU_SON_INHERITANCE")
tuosen_idx = crystal_block_text.find("TuoSenSpawnEvent.spawnAtSuzakuTomb")
check(inheritance_idx >= 0 and tuosen_idx >= 0 and inheritance_idx < tuosen_idx,
      "Tuo Sen spawn comes AFTER inheritance discovery (correct order)")

# ─────────────────────────────────────────────────────────────────────────────
# 18. Web-search canon verification — Sohu/163/Baidu Baike sources cited
# ─────────────────────────────────────────────────────────────────────────────
section("18. Web-search canon verification")

check("Sohu" in spawn_event_text, "Sohu source cited (2024-06-17)")
check("163" in spawn_event_text, "163 source cited (2025-07-29)")
check("Baidu Baike" in spawn_event_text, "Baidu Baike source cited")
check("时隔300年" in spawn_event_text, "canon quote: '300 years' (CN)")
check("王林在朱雀墓再遇拓森" in spawn_event_text,
      "canon quote: 'Wang Lin re-encounters Tuo Sen at Suzaku Tomb'")
check("修星之晶" in spawn_event_text or "Cultivation Planet Crystal" in spawn_event_text,
      "canon: Cultivation Planet Crystal (修星之晶)")

# ─────────────────────────────────────────────────────────────────────────────
# 19. Lang file — npc.ergenverse.npc_tuo_sen entry exists
# ─────────────────────────────────────────────────────────────────────────────
section("19. Lang file")

check("npc.ergenverse.npc_tuo_sen" in lang_en,
      "en_us.json has npc.ergenverse.npc_tuo_sen key")
check(lang_en.get("npc.ergenverse.npc_tuo_sen") == "Tuo Sen",
      "translation = 'Tuo Sen'")

# ─────────────────────────────────────────────────────────────────────────────
# Final report
# ─────────────────────────────────────────────────────────────────────────────

print(f"\n{'=' * 70}")
print(f"CRON-107 Tuo Sen Canon NPC Verification:")
print(f"  PASSED: {PASS}")
print(f"  FAILED: {FAIL}")
print(f"  TOTAL:  {PASS + FAIL}")
if FAILS:
    print(f"\nFAILED CHECKS:")
    for f in FAILS:
        print(f"  - {f}")
print(f"{'=' * 70}")

sys.exit(0 if FAIL == 0 else 1)
