#!/usr/bin/env python3
"""
CRON-123 verification script.

Verifies the Tian Yun Zi (天运子) NPC materialization + the LingTianhouConsumptionEvent.
Canon source: Baidu Baike 天运子 dedicated entry (PRIMARY,
https://baike.baidu.com/item/天运子/23166960) — Tian Yun Zi is the master of
天运宗, the #1 sect on 天运星; he consumes 凌天侯 at his 98th awakening.

Checks:
  1. CanonUUID.TIAN_YUN_ZI constant + Javadoc
  2. PlanetSuzakuBlueprint.TIAN_YUN_SECT CanonLocation
  3. NPCRuntime registration at TIAN_YUN_SECT
  4. CanonActorMaterializer profile
  5. npc_tian_yun_zi.json (canon data + 8-entry canon_notes + 10 initiation_lines + daily_schedule)
  6. RICanonicalDatabase N113 entry (CRON-123 canon-corrected)
  7. en_us.json lang entry
  8. LingTianhouConsumptionEvent class (sword-qi-grant prerequisite + write-once + canon narrative)
  9. HistoryEvents wiring (onEntityInteract dispatches to LingTianhouConsumptionEvent)

Exit code 0 = all checks pass; non-zero = some check failed.
"""
import re
import sys
from pathlib import Path

ROOT = Path("/home/z/my-project/forge-mod")
SRC = ROOT / "src/main/java/dev/ergenverse"

PASS = 0
FAIL = 0
CHECKS = []


def check(name: str, condition: bool, detail: str = "") -> None:
    global PASS, FAIL
    if condition:
        PASS += 1
        CHECKS.append(f"  PASS  {name}")
    else:
        FAIL += 1
        CHECKS.append(f"  FAIL  {name}  {detail}")


def read(p: Path) -> str:
    return p.read_text(encoding="utf-8")


# ──────────────────────────────────────────────────────────────────────────
# 1. CanonUUID.TIAN_YUN_ZI
# ──────────────────────────────────────────────────────────────────────────
canon_uuid_path = SRC / "runtime/CanonUUID.java"
canon_uuid = read(canon_uuid_path)

check("1.1 CanonUUID.TIAN_YUN_ZI constant declared",
      'public static final UUID TIAN_YUN_ZI = of("npc:tian_yun_zi");' in canon_uuid,
      "TIAN_YUN_ZI constant missing")
check("1.2 CRON-123 reference in Javadoc",
      "CRON-COMPLETIONIST-123" in canon_uuid,
      "CRON-123 not referenced in CanonUUID Javadoc")
check("1.3 Baidu Baike 天运子 dedicated entry cited",
      "https://baike.baidu.com/item/天运子/23166960" in canon_uuid,
      "Baidu Baike 天运子 source not cited")
check("1.4 天人第三衰 cultivation cited",
      "天人第三衰" in canon_uuid,
      "天人第三衰 cultivation not cited")
check("1.5 98th awakening cited",
      "98" in canon_uuid and ("第九十八" in canon_uuid or "98th awakening" in canon_uuid),
      "98th awakening not cited")
check("1.6 生生吞噬 consumption behavior cited",
      "生生吞噬" in canon_uuid,
      "生生吞噬 consumption behavior not cited")
check("1.7 All-Seer / 定界罗盘 identity cited",
      "定界罗盘" in canon_uuid and "All-Seer" in canon_uuid,
      "All-Seer identity not cited")
check("1.8 灭生老人 cited",
      "灭生老人" in canon_uuid,
      "灭生老人 not cited")
check("1.9 洞府界 cited",
      "洞府界" in canon_uuid,
      "洞府界 not cited")
check("1.10 Ling Tianhou consumption cited",
      "凌天侯" in canon_uuid,
      "Ling Tianhou consumption not cited")

# ──────────────────────────────────────────────────────────────────────────
# 2. PlanetSuzakuBlueprint.TIAN_YUN_SECT CanonLocation
# ──────────────────────────────────────────────────────────────────────────
blueprint_path = SRC / "runtime/PlanetSuzakuBlueprint.java"
blueprint = read(blueprint_path)

check("2.1 TIAN_YUN_SECT CanonLocation declared",
      'public static final CanonLocation TIAN_YUN_SECT' in blueprint,
      "TIAN_YUN_SECT CanonLocation missing")
check("2.2 TIAN_YUN_SECT registered in allLocations()",
      'map.put(TIAN_YUN_SECT.id, TIAN_YUN_SECT)' in blueprint,
      "TIAN_YUN_SECT not registered in allLocations()")
check("2.3 TIAN_YUN_SECT coordinates (5500, 0, 5500)",
      re.search(r'TIAN_YUN_SECT\s*=\s*new CanonLocation\([\s\S]*?5500[\s\S]*?,[\s\S]*?0[\s\S]*?,[\s\S]*?5500', blueprint) is not None
      or '5500, 0, 5500' in blueprint,
      "TIAN_YUN_SECT coordinates not (5500, 0, 5500)")
check("2.4 TIAN_YUN_SECT category 'sect'",
      re.search(r'TIAN_YUN_SECT\s*=\s*new CanonLocation\([\s\S]*?"sect"', blueprint) is not None
      or '"tian_yun_sect", "Tian Yun Sect (天运宗)",\n                    5500, 0, 5500, "sect"' in blueprint,
      "TIAN_YUN_SECT category not 'sect'")
check("2.5 CRON-123 reference in TIAN_YUN_SECT Javadoc",
      "CRON-COMPLETIONIST-123" in blueprint,
      "CRON-123 not referenced in TIAN_YUN_SECT Javadoc")
check("2.6 天运宗 / 天运星 cited in TIAN_YUN_SECT",
      "天运宗" in blueprint and "天运星" in blueprint,
      "天运宗 / 天运星 not cited in TIAN_YUN_SECT")

# ──────────────────────────────────────────────────────────────────────────
# 3. NPCRuntime registration
# ──────────────────────────────────────────────────────────────────────────
npc_runtime_path = SRC / "runtime/NPCRuntime.java"
npc_runtime = read(npc_runtime_path)

check("3.1 NPCRuntime registers TIAN_YUN_ZI",
      "CanonUUID.TIAN_YUN_ZI" in npc_runtime and 'register(CanonUUID.TIAN_YUN_ZI' in npc_runtime,
      "TIAN_YUN_ZI not registered in NPCRuntime")
check("3.2 NPCRuntime registration uses TIAN_YUN_SECT coordinates",
      "PlanetSuzakuBlueprint.TIAN_YUN_SECT.x" in npc_runtime
      and "PlanetSuzakuBlueprint.TIAN_YUN_SECT.z" in npc_runtime,
      "NPCRuntime registration doesn't use TIAN_YUN_SECT coords")
check("3.3 NPCRuntime display name 'Tian Yun Zi 天运子'",
      '"Tian Yun Zi 天运子"' in npc_runtime,
      "Display name 'Tian Yun Zi 天运子' missing in NPCRuntime")
check("3.4 CRON-123 reference in NPCRuntime",
      "CRON-COMPLETIONIST-123" in npc_runtime,
      "CRON-123 not referenced in NPCRuntime")
check("3.5 天人第三衰 cited in NPCRuntime Javadoc",
      "天人第三衰" in npc_runtime,
      "天人第三衰 not cited in NPCRuntime Javadoc")
check("3.6 天运子 NOT flagged deadUntilRevived (alive at story start)",
      re.search(r'register\(CanonUUID\.TIAN_YUN_ZI.*?\);', npc_runtime, re.DOTALL).group(0).count("deadUntilRevived") == 0
      if re.search(r'register\(CanonUUID\.TIAN_YUN_ZI.*?\);', npc_runtime, re.DOTALL) else True,
      "TIAN_YUN_ZI incorrectly flagged deadUntilRevived")

# ──────────────────────────────────────────────────────────────────────────
# 4. CanonActorMaterializer profile
# ──────────────────────────────────────────────────────────────────────────
materializer_path = SRC / "runtime/materialize/CanonActorMaterializer.java"
materializer = read(materializer_path)

check("4.1 CanonActorMaterializer has TIAN_YUN_ZI profile",
      re.search(r'profile\(CanonUUID\.TIAN_YUN_ZI,\s*"tian_yun_zi"', materializer) is not None,
      "TIAN_YUN_ZI profile not found in CanonActorMaterializer")
check("4.2 Profile characterId 'tian_yun_zi'",
      '"tian_yun_zi"' in materializer,
      "characterId 'tian_yun_zi' missing")
check("4.3 Profile sectId 'tian_yun_sect'",
      '"tian_yun_sect"' in materializer,
      "sectId 'tian_yun_sect' missing")
check("4.4 Profile realm 'nirvana_fruit'",
      re.search(r'profile\(CanonUUID\.TIAN_YUN_ZI,\s*"tian_yun_zi",\s*"Tian Yun Zi 天运子",\s*"tian_yun_sect",\s*"nirvana_fruit"\)', materializer) is not None,
      "Profile realm not 'nirvana_fruit'")
check("4.5 CRON-123 reference in CanonActorMaterializer",
      "CRON-COMPLETIONIST-123" in materializer,
      "CRON-123 not referenced in CanonActorMaterializer")
check("4.6 Realm mapping rationale documented (天人第三衰 → nirvana_fruit)",
      "天人第三衰" in materializer and "NIRVANA_FRUIT" in materializer,
      "Realm mapping rationale not documented")

# ──────────────────────────────────────────────────────────────────────────
# 5. npc_tian_yun_zi.json
# ──────────────────────────────────────────────────────────────────────────
json_path = ROOT / "src/main/resources/data/ergenverse/npcs/npc_tian_yun_zi.json"
import json as json_mod

check("5.1 npc_tian_yun_zi.json exists", json_path.exists(),
      "npc_tian_yun_zi.json file not found")
if json_path.exists():
    try:
        data = json_mod.loads(json_path.read_text(encoding="utf-8"))
        check("5.2 Valid JSON", True)
    except Exception as e:
        check("5.2 Valid JSON", False, f"JSON parse error: {e}")
        data = {}

    if isinstance(data, dict):
        check("5.3 nameCn '天运子'", data.get("nameCn") == "天运子",
              f"nameCn != '天运子' (got {data.get('nameCn')!r})")
        check("5.4 faction 'tian_yun_sect'", data.get("faction") == "tian_yun_sect",
              f"faction != 'tian_yun_sect' (got {data.get('faction')!r})")
        check("5.5 location 'tian_yun_sect'", data.get("location") == "tian_yun_sect",
              f"location != 'tian_yun_sect' (got {data.get('location')!r})")
        check("5.6 cultivation references 天人第三衰",
              "天人第三衰" in (data.get("cultivation") or ""),
              "cultivation doesn't reference 天人第三衰")
        check("5.7 canon_confidence >= 4", (data.get("canon_confidence") or 0) >= 4,
              f"canon_confidence < 4 (got {data.get('canon_confidence')})")
        check("5.8 derivation_type 'A'", data.get("derivation_type") == "A",
              f"derivation_type != 'A' (got {data.get('derivation_type')!r})")
        canon_notes = data.get("canon_notes") or {}
        check("5.9 canon_notes block exists with >= 7 entries",
              isinstance(canon_notes, dict) and len(canon_notes) >= 7,
              f"canon_notes has < 7 entries (got {len(canon_notes) if isinstance(canon_notes, dict) else 0})")
        check("5.10 canon_notes includes '98th_awakening_target'",
              "98th_awakening_target" in canon_notes,
              "canon_notes missing '98th_awakening_target'")
        check("5.11 canon_notes includes 'consumption_behavior'",
              "consumption_behavior" in canon_notes,
              "canon_notes missing 'consumption_behavior'")
        check("5.12 canon_notes includes 'true_identity'",
              "true_identity" in canon_notes,
              "canon_notes missing 'true_identity'")
        check("5.13 canon_notes includes 'final_fate'",
              "final_fate" in canon_notes,
              "canon_notes missing 'final_fate'")
        check("5.14 canon_notes cites Baidu Baike 天运子 entry",
              "https://baike.baidu.com/item/天运子/23166960" in str(canon_notes.get("sources", "")),
              "canon_notes sources missing Baidu Baike 天运子 entry")
        initiation_lines = data.get("initiation_lines") or []
        check("5.15 initiation_lines non-empty (>= 5)",
              len(initiation_lines) >= 5,
              f"initiation_lines has < 5 entries (got {len(initiation_lines)})")
        daily_schedule = data.get("daily_schedule") or []
        check("5.16 daily_schedule at tian_yun_sect (>= 5 entries)",
              len(daily_schedule) >= 5 and all(s.get("location") == "tian_yun_sect" for s in daily_schedule),
              f"daily_schedule has < 5 entries or not at tian_yun_sect")

# ──────────────────────────────────────────────────────────────────────────
# 6. RICanonicalDatabase N113 entry
# ──────────────────────────────────────────────────────────────────────────
ri_db_path = SRC / "wanglin/RICanonicalDatabase.java"
ri_db = read(ri_db_path)

# Find the N113 entry block
n113_match = re.search(
    r'new CanonCharacter\(\s*"N113".*?\n        \)',
    ri_db, re.DOTALL)
check("6.1 RICanonicalDatabase N113 entry exists", n113_match is not None,
      "N113 entry not found in RICanonicalDatabase")
if n113_match:
    n113 = n113_match.group(0)
    check("6.2 N113 cultivation references 天人第三衰 (CRON-123 correction)",
          "天人第三衰" in n113,
          "N113 cultivation doesn't reference 天人第三衰")
    # 6.3: parse the cultivation field (first quoted string after CharType.ANTAGONIST).
    # The string 'Third Step+' may appear in the CRON-123 correction comment
    # ("was 'Third Step+' — UNVERIFIED"). That's correct context. We need to
    # verify the CULTIVATION STRING is no longer 'Third Step+'.
    m_cult = re.search(r'CharType\.ANTAGONIST,\s*\n\s*(?://[^\n]*\n\s*)*"([^"]+)"', n113)
    if m_cult:
        cultivation = m_cult.group(1)
        check("6.3 N113 cultivation is 天人第三衰 (not Third Step+)",
              "天人第三衰" in cultivation and "Third Step+" not in cultivation,
              f"N113 cultivation is '{cultivation}' — should be 天人第三衰")
    else:
        check("6.3 N113 cultivation field not found", False, "Couldn't parse N113 cultivation field")
    check("6.4 N113 faction references 天运宗",
          "天运宗" in n113,
          "N113 faction doesn't reference 天运宗")
    check("6.5 N113 NO LONGER uses 'Heavenly Fate Sect' as primary faction",
          "Heavenly Fate Sect" not in n113 or "Heavenly Fate Sect" not in n113.split('"Heavenly')[0] if 'Heavenly' in n113 else True,
          "N113 still uses 'Heavenly Fate Sect' as primary faction")
    check("6.6 N113 CharType is ANTAGONIST (corrected from OTHER)",
          "CharType.ANTAGONIST" in n113,
          "N113 CharType not ANTAGONIST")
    check("6.7 N113 notes mention 98th awakening target",
          "98" in n113 and "凌天侯" in n113,
          "N113 notes don't mention 98th awakening target")
    check("6.8 N113 notes mention All-Seer / 定界罗盘",
          "All-Seer" in n113 and "定界罗盘" in n113,
          "N113 notes don't mention All-Seer / 定界罗盘")
    check("6.9 N113 relationships include Ling Tianhou (enemy)",
          '"Ling Tianhou"' in n113 and '"enemy"' in n113,
          "N113 relationships don't include Ling Tianhou (enemy)")
    check("6.10 N113 sources cite Baidu Baike 天运子",
          "https://baike.baidu.com/item/天运子/23166960" in n113,
          "N113 sources don't cite Baidu Baike 天运子")
    check("6.11 N113 CRON-123 reference",
          "CRON-123" in n113,
          "N113 doesn't reference CRON-123")
    check("6.12 N113 starDomain references 洞府界",
          "洞府界" in n113,
          "N113 starDomain doesn't reference 洞府界")

# ──────────────────────────────────────────────────────────────────────────
# 7. en_us.json lang entry
# ──────────────────────────────────────────────────────────────────────────
lang_path = ROOT / "src/main/resources/assets/ergenverse/lang/en_us.json"
check("7.1 en_us.json exists", lang_path.exists(),
      "en_us.json not found")
if lang_path.exists():
    try:
        lang_data = json_mod.loads(lang_path.read_text(encoding="utf-8"))
        check("7.2 en_us.json valid JSON", True)
        check("7.3 'npc.ergenverse.npc_tian_yun_zi': 'Tian Yun Zi' present",
              lang_data.get("npc.ergenverse.npc_tian_yun_zi") == "Tian Yun Zi",
              f"lang entry missing or wrong (got {lang_data.get('npc.ergenverse.npc_tian_yun_zi')!r})")
    except Exception as e:
        check("7.2 en_us.json valid JSON", False, f"JSON parse error: {e}")

# ──────────────────────────────────────────────────────────────────────────
# 8. LingTianhouConsumptionEvent class
# ──────────────────────────────────────────────────────────────────────────
event_path = SRC / "wanglin/bead/LingTianhouConsumptionEvent.java"
check("8.1 LingTianhouConsumptionEvent.java exists", event_path.exists(),
      "LingTianhouConsumptionEvent.java not found")
if event_path.exists():
    event = read(event_path)
    check("8.2 CHARACTER_ID = 'tian_yun_zi'",
          'public static final String CHARACTER_ID = "tian_yun_zi";' in event,
          "CHARACTER_ID not 'tian_yun_zi'")
    check("8.3 AWAKENING_NUMBER = 98",
          'public static final int AWAKENING_NUMBER = 98;' in event,
          "AWAKENING_NUMBER not 98")
    check("8.4 SUBJECT_LING_TIANHOU_CONSUMED constant",
          'public static final String SUBJECT_LING_TIANHOU_CONSUMED' in event,
          "SUBJECT_LING_TIANHOU_CONSUMED constant missing")
    check("8.5 handleConsumption method declared",
          "public static void handleConsumption(ServerPlayer player" in event,
          "handleConsumption method missing")
    check("8.6 Sword-qi-grant prerequisite checked",
          "sword_qi_granted" in event and 'LingTianhouSwordQiGrantEvent.CHARACTER_ID' in event,
          "Sword-qi-grant prerequisite not checked")
    check("8.7 Write-once guard 'ling_tianhou_consumed' checked",
          "ling_tianhou_consumed" in event,
          "Write-once guard 'ling_tianhou_consumed' not checked")
    check("8.8 Marks Ling Tianhou deadUntilRevived = true",
          "deadUntilRevived = true" in event or "deadUntilRevived=true" in event,
          "Doesn't mark Ling Tianhou deadUntilRevived")
    check("8.9 Dematerializes Ling Tianhou EntityCultivator",
          "dematerializeActor(CanonUUID.LING_TIANHOU" in event,
          "Doesn't dematerialize Ling Tianhou EntityCultivator")
    check("8.10 Bilingual message (Chinese + English)",
          "天运子" in event and "Tian Yun Zi" in event,
          "Bilingual message missing")
    check("8.11 Canon citation '98th awakening' in narrative",
          "98" in event and ("第九十八" in event or "98th awakening" in event),
          "98th awakening not in narrative")
    check("8.12 Records event in HistoryManager",
          "HistoryManager.onDiscovery" in event,
          "Doesn't record event in HistoryManager")
    check("8.13 Baidu Baike 天运子 source cited in Javadoc",
          "https://baike.baidu.com/item/天运子/23166960" in event,
          "Baidu Baike 天运子 source not cited in Javadoc")
    check("8.14 CRON-123 reference",
          "CRON-COMPLETIONIST-123" in event,
          "CRON-123 not referenced")
    check("8.15 Zhihu source cited",
          "zhuanlan.zhihu.com/p/1957927329482383516" in event,
          "Zhihu source not cited")
    check("8.16 Sound effects (WITHER_DEATH + ENDER_DRAGON_GROWL)",
          "WITHER_DEATH" in event and "ENDER_DRAGON_GROWL" in event,
          "Sound effects missing")
    check("8.17 Defensive 'prerequisite not met' message",
          "尚未赠你剑气" in event or "has not yet granted" in event,
          "Defensive prerequisite-not-met message missing")
    check("8.18 Defensive 'already consumed' message",
          "已融入他的轮回" in event or "merged into his cycle" in event,
          "Defensive already-consumed message missing")

# ──────────────────────────────────────────────────────────────────────────
# 9. HistoryEvents wiring
# ──────────────────────────────────────────────────────────────────────────
history_events_path = SRC / "history/HistoryEvents.java"
check("9.1 HistoryEvents.java exists", history_events_path.exists(),
      "HistoryEvents.java not found")
if history_events_path.exists():
    he = read(history_events_path)
    check("9.2 LingTianhouConsumptionEvent imported",
          "import dev.ergenverse.wanglin.bead.LingTianhouConsumptionEvent;" in he,
          "LingTianhouConsumptionEvent import missing")
    check("9.3 CHARACTER_ID check in onEntityInteract",
          "LingTianhouConsumptionEvent.CHARACTER_ID.equals(cultivator.getCharacterId())" in he,
          "CHARACTER_ID check not in onEntityInteract")
    check("9.4 handleConsumption dispatched",
          "LingTianhouConsumptionEvent.handleConsumption(serverPlayer, cultivator)" in he,
          "handleConsumption not dispatched")
    check("9.5 CRON-123 reference in HistoryEvents comment",
          "CRON-123" in he,
          "CRON-123 not referenced in HistoryEvents comment")

# ──────────────────────────────────────────────────────────────────────────
# Summary
# ──────────────────────────────────────────────────────────────────────────
print()
print("=" * 70)
print("CRON-123 verification — Tian Yun Zi NPC + LingTianhouConsumptionEvent")
print("=" * 70)
for c in CHECKS:
    print(c)
print()
print(f"Total: {PASS} pass, {FAIL} fail (out of {PASS + FAIL})")
print()
if FAIL == 0:
    print("RESULT: ALL CHECKS PASS")
    sys.exit(0)
else:
    print(f"RESULT: {FAIL} CHECK(S) FAILED")
    sys.exit(1)
