#!/usr/bin/env python3
"""
CRON-116 verification: Wang Ping (王平) canon NPC registration.

Verifies that:
  1. CanonUUID.WANG_PING constant exists.
  2. NPCRuntime registers Wang Ping with deadUntilRevived=true.
  3. CanonActorMaterializer has a Wang Ping profile (sect="none", realm="mortal").
  4. PlanetSuzakuBlueprint has NPC_WANG_PING constant.
  5. npc_wang_ping.json resource file exists with canon-accurate content.
  6. CanonRelationshipSeeder.java no longer says "adopted son" for Wang Ping.
  7. CanonRelationshipSeeder.java no longer has the fabricated "Wang Ping ↔
     Zhou Tingsu" relationship.
  8. CanonRelationshipSeeder.java has the new "Wang Ping ↔ Mu Bingmei"
     (mother) relationship.
  9. CanonRelationshipSeeder.java has the new "Wang Ping ↔ Qing Yi" (wife)
     relationship.
  10. ri_canon_database.json N07 entry is enriched with canon citations.
  11. No remaining "adopted son" references for Wang Ping anywhere.
  12. The Baidu Baike URL for Wang Ping is cited.

Exit code 0 = all checks pass; 1 = at least one check failed.
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

FORGE_MOD = Path("/home/z/my-project/forge-mod")


def check_canon_uuid_wang_ping() -> tuple[int, int]:
    """Check 1: CanonUUID.WANG_PING constant exists."""
    path = FORGE_MOD / "src/main/java/dev/ergenverse/runtime/CanonUUID.java"
    text = path.read_text(encoding="utf-8")
    if 'public static final UUID WANG_PING = of("npc:wang_ping");' in text:
        return 1, 0
    print("  FAIL: CanonUUID.WANG_PING constant missing or wrong")
    return 0, 1


def check_npcruntime_dead_until_revived() -> tuple[int, int]:
    """Check 2: NPCRuntime registers Wang Ping with deadUntilRevived=true."""
    path = FORGE_MOD / "src/main/java/dev/ergenverse/runtime/NPCRuntime.java"
    text = path.read_text(encoding="utf-8")
    passed = 0
    failed = 0
    # Check registration
    if 'register(CanonUUID.WANG_PING, "Wang Ping 王平"' in text:
        passed += 1
    else:
        print("  FAIL: NPCRuntime.register(CanonUUID.WANG_PING, ...) missing")
        failed += 1
    # Check deadUntilRevived=true flag set
    # Pattern: ActorState wangPingState = actors.get(CanonUUID.WANG_PING);
    #         if (wangPingState != null) { wangPingState.deadUntilRevived = true; }
    if "wangPingState.deadUntilRevived = true" in text:
        passed += 1
    else:
        print("  FAIL: Wang Ping deadUntilRevived=true flag not set")
        failed += 1
    # Check placement at Suzaku Tomb
    if "PlanetSuzakuBlueprint.SUZAKU_TOMB.x" in text and "PlanetSuzakuBlueprint.SUZAKU_TOMB.z" in text:
        # Verify it's in the Wang Ping registration context (not just Tuo Sen)
        # Find the Wang Ping block and check it references SUZAKU_TOMB
        idx = text.find('register(CanonUUID.WANG_PING')
        if idx >= 0:
            block = text[idx:idx+500]
            if "SUZAKU_TOMB" in block:
                passed += 1
            else:
                print("  FAIL: Wang Ping not placed at SUZAKU_TOMB")
                failed += 1
        else:
            print("  FAIL: Wang Ping registration block not found")
            failed += 1
    else:
        print("  FAIL: SUZAKU_TOMB blueprint reference missing")
        failed += 1
    return passed, failed


def check_canon_actor_materializer_profile() -> tuple[int, int]:
    """Check 3: CanonActorMaterializer has Wang Ping profile."""
    path = FORGE_MOD / "src/main/java/dev/ergenverse/runtime/materialize/CanonActorMaterializer.java"
    text = path.read_text(encoding="utf-8")
    # Pattern: profile(CanonUUID.WANG_PING, "wang_ping", "Wang Ping 王平", "none", "mortal");
    expected = 'profile(CanonUUID.WANG_PING,     "wang_ping",     "Wang Ping 王平",         "none",          "mortal")'
    if expected in text:
        return 1, 0
    # Try with different spacing
    if 'CanonUUID.WANG_PING' in text and '"wang_ping"' in text and '"Wang Ping 王平"' in text and '"none"' in text and '"mortal"' in text:
        return 1, 0
    print("  FAIL: CanonActorMaterializer Wang Ping profile missing or wrong")
    return 0, 1


def check_planet_suzaku_blueprint_const() -> tuple[int, int]:
    """Check 4: PlanetSuzakuBlueprint has NPC_WANG_PING constant."""
    path = FORGE_MOD / "src/main/java/dev/ergenverse/runtime/PlanetSuzakuBlueprint.java"
    text = path.read_text(encoding="utf-8")
    if 'public static final String NPC_WANG_PING = "wang_ping";' in text:
        return 1, 0
    print("  FAIL: PlanetSuzakuBlueprint.NPC_WANG_PING constant missing")
    return 0, 1


def check_npc_wang_ping_json() -> tuple[int, int]:
    """Check 5: npc_wang_ping.json exists with canon-accurate content."""
    path = FORGE_MOD / "src/main/resources/data/ergenverse/npcs/npc_wang_ping.json"
    if not path.exists():
        print(f"  FAIL: {path} does not exist")
        return 0, 1
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as e:
        print(f"  FAIL: npc_wang_ping.json is not valid JSON: {e}")
        return 0, 1
    passed = 0
    failed = 0
    checks = [
        ('name', 'Wang Ping'),
        ('nameCn', '王平'),
        ('canon_id', 'N07'),
        ('faction', 'none'),
        ('location', 'suzaku_tomb'),
    ]
    for key, expected in checks:
        if data.get(key) == expected:
            passed += 1
        else:
            print(f"  FAIL: npc_wang_ping.json {key}={data.get(key)!r}, expected {expected!r}")
            failed += 1
    # Check canon_notes contains key facts
    notes = json.dumps(data.get('canon_notes', {}), ensure_ascii=False)
    for term in ['BIOLOGICAL', '怨婴', '剑气', '二次化凡', '青衣', '天逆珠', 'Ch 680', 'Ch 681']:
        if term in notes:
            passed += 1
        else:
            print(f"  FAIL: npc_wang_ping.json canon_notes missing term {term!r}")
            failed += 1
    return passed, failed


def check_no_adopted_son_in_seeder() -> tuple[int, int]:
    """Check 6: CanonRelationshipSeeder no longer says 'adopted son' for Wang Ping."""
    path = FORGE_MOD / "src/main/java/dev/ergenverse/simulation/action/CanonRelationshipSeeder.java"
    text = path.read_text(encoding="utf-8")
    # Find the Wang Lin ↔ Wang Ping block
    idx = text.find('"wang_lin", "wang_ping"')
    if idx < 0:
        print("  FAIL: Wang Lin ↔ Wang Ping relationship not found")
        return 0, 1
    # Look at the surrounding 1500 chars
    block = text[max(0, idx-200):idx+1500]
    if "adopted" in block.lower():
        print("  FAIL: 'adopted' still appears in Wang Ping relationship block")
        return 0, 1
    if "biological" in block.lower():
        return 1, 0
    print("  FAIL: 'biological' not found in Wang Ping relationship block")
    return 0, 1


def check_no_zhou_tingsu_wang_ping_link() -> tuple[int, int]:
    """Check 7: No fabricated 'Wang Ping ↔ Zhou Tingsu' relationship."""
    path = FORGE_MOD / "src/main/java/dev/ergenverse/simulation/action/CanonRelationshipSeeder.java"
    text = path.read_text(encoding="utf-8")
    # Look for the old fabricated pattern
    if '"wang_ping", "zhou_tingsu"' in text:
        print("  FAIL: fabricated 'wang_ping' ↔ 'zhou_tingsu' relationship still present")
        return 0, 1
    return 1, 0


def check_wang_ping_mu_bingmei_relationship() -> tuple[int, int]:
    """Check 8: New 'Wang Ping ↔ Mu Bingmei' (mother) relationship exists."""
    path = FORGE_MOD / "src/main/java/dev/ergenverse/simulation/action/CanonRelationshipSeeder.java"
    text = path.read_text(encoding="utf-8")
    if '"wang_ping", "mu_bingmei"' in text:
        return 1, 0
    print("  FAIL: 'wang_ping' ↔ 'mu_bingmei' (mother) relationship missing")
    return 0, 1


def check_wang_ping_qing_yi_relationship() -> tuple[int, int]:
    """Check 9: New 'Wang Ping ↔ Qing Yi' (wife) relationship exists."""
    path = FORGE_MOD / "src/main/java/dev/ergenverse/simulation/action/CanonRelationshipSeeder.java"
    text = path.read_text(encoding="utf-8")
    if '"wang_ping", "qing_yi"' in text:
        return 1, 0
    print("  FAIL: 'wang_ping' ↔ 'qing_yi' (wife) relationship missing")
    return 0, 1


def check_ri_canon_database_n07_enriched() -> tuple[int, int]:
    """Check 10: ri_canon_database.json N07 entry is enriched."""
    path = FORGE_MOD / "src/main/resources/data/ergenverse/ri_canon_database.json"
    data = json.loads(path.read_text(encoding="utf-8"))
    for char in data.get('characters', []):
        if char.get('id') == 'N07':
            passed = 0
            failed = 0
            source = char.get('source', '')
            if 'baike.baidu.com/item/王平' in source:
                passed += 1
            else:
                print(f"  FAIL: N07 source does not cite Baidu Baike Wang Ping URL")
                failed += 1
            facts = ' '.join(char.get('knownFacts', []))
            for term in ['BIOLOGICAL', '怨婴', '剑气', '二次化凡', '青衣', 'Ch 680']:
                if term in facts:
                    passed += 1
                else:
                    print(f"  FAIL: N07 knownFacts missing term {term!r}")
                    failed += 1
            if char.get('firstAppearance') and 'Ch 680' in char['firstAppearance']:
                passed += 1
            else:
                print(f"  FAIL: N07 firstAppearance not enriched with Ch 680")
                failed += 1
            return passed, failed
    print("  FAIL: N07 entry not found")
    return 0, 1


def check_no_adopted_son_anywhere() -> tuple[int, int]:
    """Check 11: No remaining 'adopted son' references for Wang Ping in mod-loaded Java."""
    # Search all Java files for "adopted son" near "Wang Ping"
    java_dir = FORGE_MOD / "src/main/java"
    failures = 0
    for path in java_dir.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        if "adopted son" in text.lower() and "wang ping" in text.lower():
            # Check if it's in a Wang Ping context
            idx = text.lower().find("adopted son")
            block = text[max(0, idx-500):idx+500]
            if "wang ping" in block.lower() or "wang_ping" in block.lower():
                print(f"  FAIL: {path.relative_to(FORGE_MOD)} has 'adopted son' near Wang Ping")
                failures += 1
    if failures == 0:
        return 1, 0
    return 0, failures


def check_baidu_baike_url_cited() -> tuple[int, int]:
    """Check 12: The Baidu Baike URL for Wang Ping is cited."""
    url = "https://baike.baidu.com/item/王平/62563845"
    files_to_check = [
        "src/main/java/dev/ergenverse/runtime/CanonUUID.java",
        "src/main/java/dev/ergenverse/runtime/NPCRuntime.java",
        "src/main/resources/data/ergenverse/npcs/npc_wang_ping.json",
        "src/main/resources/data/ergenverse/ri_canon_database.json",
    ]
    passed = 0
    failed = 0
    for rel in files_to_check:
        path = FORGE_MOD / rel
        text = path.read_text(encoding="utf-8")
        if url in text:
            passed += 1
        else:
            print(f"  FAIL: {rel} does not cite {url}")
            failed += 1
    return passed, failed


def main() -> int:
    print("=" * 70)
    print("CRON-116 Verification: Wang Ping (王平) Canon NPC Registration")
    print("=" * 70)
    print(f"Canon: biological son of Wang Lin + Mu Bingmei/Liu Mei (9th avatar)")
    print(f"Canon: conceived in Suzaku Tomb; refined into 怨婴; rebuilt from sword qi")
    print(f"Canon: lived ~73 years mortal life; 残魂 sealed into 天逆珠")
    print(f"Canon sources: Baidu Baike, Fandom wiki, newhanfu, Toutiao, 163")
    print()

    total_passed = 0
    total_failed = 0

    print("── Check 1: CanonUUID.WANG_PING constant ──")
    p, f = check_canon_uuid_wang_ping()
    total_passed += p; total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 2: NPCRuntime deadUntilRevived=true + Suzaku Tomb placement ──")
    p, f = check_npcruntime_dead_until_revived()
    total_passed += p; total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 3: CanonActorMaterializer Wang Ping profile ──")
    p, f = check_canon_actor_materializer_profile()
    total_passed += p; total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 4: PlanetSuzakuBlueprint.NPC_WANG_PING constant ──")
    p, f = check_planet_suzaku_blueprint_const()
    total_passed += p; total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 5: npc_wang_ping.json canon-accurate content ──")
    p, f = check_npc_wang_ping_json()
    total_passed += p; total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 6: CanonRelationshipSeeder no longer says 'adopted son' ──")
    p, f = check_no_adopted_son_in_seeder()
    total_passed += p; total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 7: No fabricated 'Wang Ping ↔ Zhou Tingsu' relationship ──")
    p, f = check_no_zhou_tingsu_wang_ping_link()
    total_passed += p; total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 8: New 'Wang Ping ↔ Mu Bingmei' (mother) relationship ──")
    p, f = check_wang_ping_mu_bingmei_relationship()
    total_passed += p; total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 9: New 'Wang Ping ↔ Qing Yi' (wife) relationship ──")
    p, f = check_wang_ping_qing_yi_relationship()
    total_passed += p; total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 10: ri_canon_database.json N07 entry enriched ──")
    p, f = check_ri_canon_database_n07_enriched()
    total_passed += p; total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 11: No 'adopted son' references for Wang Ping anywhere ──")
    p, f = check_no_adopted_son_anywhere()
    total_passed += p; total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 12: Baidu Baike URL for Wang Ping cited ──")
    p, f = check_baidu_baike_url_cited()
    total_passed += p; total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("=" * 70)
    print(f"TOTAL: {total_passed} passed, {total_failed} failed")
    print("=" * 70)
    if total_failed == 0:
        print("ALL CHECKS PASS — CRON-116 Wang Ping NPC registration verified.")
        return 0
    else:
        print("SOME CHECKS FAILED — see above for details.")
        return 1


if __name__ == "__main__":
    sys.exit(main())
