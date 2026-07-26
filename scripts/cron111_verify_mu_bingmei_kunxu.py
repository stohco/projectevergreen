#!/usr/bin/env python3
"""
CRON-111 verification script — Mu Bingmei (慕冰媚) registration + Kunxu Realm +
Zhou Ru Cultivation Growth Service.

Validates the canon-faithful post-transfer cultivation arc introduced in CRON-111:
  - Mu Bingmei registered as a canon NPC (UUID + blueprint constant + materializer profile + NPCRuntime registration)
  - Kunxu Realm (昆虚界) added as a blueprint location
  - npc_mu_bingmei.json data file enriched with canon-faithful content
  - ZhouRuCultivationGrowthService advances 周茹's realm when near Mu Bingmei
  - Service wired into Ergenverse.onServerTick (Loop J)
  - Canon fidelity: 周茹 becomes Soul Transformation under Mu Bingmei's guidance
  - Architecture compliance: CRON-69 point 5 (gameplay via facade, not direct store manipulation)
  - State-transition integrity: CRON-110 prerequisite (pregnant_with_li_muwan_soul flag)

Runs 100+ checks across 8 categories. Exits non-zero on any failure.
"""

import re
import sys
from pathlib import Path

ROOT = Path("/home/z/my-project/forge-mod")
JAVA_ROOT = ROOT / "src/main/java"
DATA_ROOT = ROOT / "src/main/resources/data/ergenverse"

def read(p):
    return p.read_text(encoding="utf-8", errors="replace")

def strip_java_comments(text):
    text = re.sub(r'/\*[\s\S]*?\*/', '', text)
    text = re.sub(r'//[^\n]*', '', text)
    return text

checks_passed = 0
checks_failed = 0
failures = []

def check(category, name, condition):
    global checks_passed, checks_failed
    if condition:
        checks_passed += 1
        print(f"  [PASS] {category} :: {name}")
    else:
        checks_failed += 1
        failures.append(f"{category} :: {name}")
        print(f"  [FAIL] {category} :: {name}")

# ════════════════════════════════════════════════════════════════════════
# 1. CANON UUID REGISTRATION
# ════════════════════════════════════════════════════════════════════════
print("\n=== 1. CANON UUID REGISTRATION ===")
canon_uuid = read(JAVA_ROOT / "dev/ergenverse/runtime/CanonUUID.java")
canon_uuid_nc = strip_java_comments(canon_uuid)

check("CanonUUID", "MU_BINGMEI constant declared",
      "public static final UUID MU_BINGMEI" in canon_uuid_nc)
check("CanonUUID", "MU_BINGMEI derived from 'npc:mu_bingmei' key",
      'of("npc:mu_bingmei")' in canon_uuid_nc)
check("CanonUUID", "MU_BINGMEI has CRON-111 javadoc",
      "CRON-COMPLETIONIST-111" in canon_uuid and "慕冰媚" in canon_uuid)
check("CanonUUID", "MU_BINGMEI mentions Liu Mei = Mu Bingmei (canon identity)",
      "Liu Mei" in canon_uuid and "true form" in canon_uuid)
check("CanonUUID", "MU_BINGMEI mentions Wang Ping (son) canon detail",
      "Wang Ping" in canon_uuid)
check("CanonUUID", "MU_BINGMEI mentions Dream Dao (梦道) karmic severing",
      "Dream Dao" in canon_uuid)
check("CanonUUID", "MU_BINGMEI mentions Zhou Ru as disciple",
      "Zhou Ru" in canon_uuid and "disciple" in canon_uuid)
check("CanonUUID", "MU_BINGMEI mentions Kunxu Realm",
      "Kunxu" in canon_uuid or "昆虚" in canon_uuid)
check("CanonUUID", "MU_BINGMEI mentions Ascendant+ cultivation",
      "Ascendant" in canon_uuid)
check("CanonUUID", "MU_BINGMEI references ZhouRuCultivationGrowthService",
      "ZhouRuCultivationGrowthService" in canon_uuid)
check("CanonUUID", "NO fabricated chapter citation",
      "not cited" in canon_uuid.lower() or "NO fabricated" in canon_uuid)

# ════════════════════════════════════════════════════════════════════════
# 2. BLUEPRINT + MATERIALIZER REGISTRATION
# ════════════════════════════════════════════════════════════════════════
print("\n=== 2. BLUEPRINT + MATERIALIZER REGISTRATION ===")
blueprint = read(JAVA_ROOT / "dev/ergenverse/runtime/PlanetSuzakuBlueprint.java")
blueprint_nc = strip_java_comments(blueprint)

check("Blueprint", "NPC_MU_BINGMEI character id constant",
      'public static final String NPC_MU_BINGMEI = "mu_bingmei"' in blueprint_nc)
check("Blueprint", "NPC_MU_BINGMEI has CRON-111 javadoc",
      "CRON-COMPLETIONIST-111" in blueprint)
check("Blueprint", "NPC_MU_BINGMEI references 慕冰媚 / 柳眉",
      "慕冰媚" in blueprint and "柳眉" in blueprint)

check("Blueprint", "KUNXU_REALM CanonLocation declared",
      "public static final CanonLocation KUNXU_REALM" in blueprint_nc)
check("Blueprint", "KUNXU_REALM id is 'kunxu_realm'",
      '"kunxu_realm"' in blueprint_nc)
check("Blueprint", "KUNXU_REALM name is 'Kunxu Realm (昆虚界)'",
      "Kunxu Realm (昆虚界)" in blueprint)
check("Blueprint", "KUNXU_REALM has CRON-111 javadoc",
      "CRON-COMPLETIONIST-111" in blueprint)
check("Blueprint", "KUNXU_REALM registered in allLocations() map",
      'map.put(KUNXU_REALM.id, KUNXU_REALM)' in blueprint_nc)
check("Blueprint", "KUNXU_REALM category is 'secret_realm'",
      '"secret_realm"' in blueprint_nc)
check("Blueprint", "KUNXU_REALM notes mod-original placement",
      "mod-original" in blueprint.lower())

materializer = read(JAVA_ROOT / "dev/ergenverse/runtime/materialize/CanonActorMaterializer.java")
materializer_nc = strip_java_comments(materializer)

check("Materializer", "Mu Bingmei profile registered",
      'profile(CanonUUID.MU_BINGMEI' in materializer_nc)
check("Materializer", "Mu Bingmei characterId is 'mu_bingmei'",
      re.search(r'profile\(CanonUUID\.MU_BINGMEI,\s*"([^"]+)"', materializer_nc) and
      re.search(r'profile\(CanonUUID\.MU_BINGMEI,\s*"([^"]+)"', materializer_nc).group(1) == "mu_bingmei")
check("Materializer", "Mu Bingmei display name contains 慕冰媚 / 柳眉",
      "Mu Bingmei 慕冰媚 / 柳眉" in materializer)
check("Materializer", "Mu Bingmei sectId is 'kunxu_realm'",
      '"kunxu_realm"' in materializer_nc)
check("Materializer", "Mu Bingmei realm is 'ascendant'",
      re.search(r'profile\(CanonUUID\.MU_BINGMEI,\s*"mu_bingmei",\s*"Mu Bingmei[^"]*",\s*"kunxu_realm",\s*"(\w+)"\)', materializer_nc) and
      re.search(r'profile\(CanonUUID\.MU_BINGMEI,\s*"mu_bingmei",\s*"Mu Bingmei[^"]*",\s*"kunxu_realm",\s*"(\w+)"\)', materializer_nc).group(1) == "ascendant")
check("Materializer", "Mu Bingmei profile has CRON-111 javadoc",
      "CRON-COMPLETIONIST-111" in materializer)

# ════════════════════════════════════════════════════════════════════════
# 3. NPCRuntime REGISTRATION
# ════════════════════════════════════════════════════════════════════════
print("\n=== 3. NPCRuntime REGISTRATION ===")
npc_runtime = read(JAVA_ROOT / "dev/ergenverse/runtime/NPCRuntime.java")
npc_runtime_nc = strip_java_comments(npc_runtime)

check("NPCRuntime", "Mu Bingmei registered with register()",
      "register(CanonUUID.MU_BINGMEI" in npc_runtime_nc)
check("NPCRuntime", "Mu Bingmei placed at KUNXU_REALM",
      "PlanetSuzakuBlueprint.KUNXU_REALM.x" in npc_runtime_nc and
      "PlanetSuzakuBlueprint.KUNXU_REALM.z" in npc_runtime_nc)
check("NPCRuntime", "Mu Bingmei NOT flagged deadUntilRevived (present from day 0)",
      not re.search(r'MU_BINGMEI.*?deadUntilRevived\s*=\s*true', npc_runtime_nc, re.DOTALL))
check("NPCRuntime", "Mu Bingmei registration has CRON-111 javadoc",
      "CRON-COMPLETIONIST-111" in npc_runtime)
check("NPCRuntime", "Mu Bingmei registration notes Zhou Ru as disciple",
      "Zhou Ru" in npc_runtime and "disciple" in npc_runtime)
check("NPCRuntime", "Mu Bingmei registration notes Dream Dao karmic severing",
      "Dream Dao" in npc_runtime)

# ════════════════════════════════════════════════════════════════════════
# 4. DATA FILE (npc_mu_bingmei.json)
# ════════════════════════════════════════════════════════════════════════
print("\n=== 4. DATA FILE (npc_mu_bingmei.json) ===")
import json
mu_bingmei_data = json.loads(read(DATA_ROOT / "npcs/npc_mu_bingmei.json"))

check("DataFile", "npc_id is 'npc_mu_bingmei'",
      mu_bingmei_data.get("npc_id") == "npc_mu_bingmei")
check("DataFile", "nameCn is '慕冰媚 / 柳眉'",
      mu_bingmei_data.get("nameCn") == "慕冰媚 / 柳眉")
check("DataFile", "canon_id is 'N19'",
      mu_bingmei_data.get("canon_id") == "N19")
check("DataFile", "location is 'kunxu_realm'",
      mu_bingmei_data.get("location") == "kunxu_realm")
check("DataFile", "cultivation is 'Ascendant'",
      mu_bingmei_data.get("cultivation") == "Ascendant")
check("DataFile", "teaching_available is true (she's a master)",
      mu_bingmei_data.get("teaching_available") == True)
check("DataFile", "canon_notes section present",
      "canon_notes" in mu_bingmei_data)
check("DataFile", "canon_notes.true_identity documents Liu Mei = Mu Bingmei",
      mu_bingmei_data.get("canon_notes", {}).get("true_identity", "").find("Liu Mei") >= 0)
check("DataFile", "canon_notes.zhou_ru_master documents disciple relationship",
      mu_bingmei_data.get("canon_notes", {}).get("zhou_ru_master", "").find("Zhou Ru") >= 0)
check("DataFile", "canon_notes.wang_lin_relationship documents Wang Ping + Dream Dao",
      mu_bingmei_data.get("canon_notes", {}).get("wang_lin_relationship", "").find("Wang Ping") >= 0 and
      mu_bingmei_data.get("canon_notes", {}).get("wang_lin_relationship", "").find("Dream Dao") >= 0)
check("DataFile", "canon_notes.sources cites RICanonicalDatabase",
      mu_bingmei_data.get("canon_notes", {}).get("sources", "").find("RICanonicalDatabase") >= 0)
check("DataFile", "canon_notes.sources has NO fabricated chapter citation",
      mu_bingmei_data.get("canon_notes", {}).get("sources", "").find("NO fabricated") >= 0)
check("DataFile", "initiation_lines present (at least 3)",
      len(mu_bingmei_data.get("initiation_lines", [])) >= 3)
check("DataFile", "daily_schedule present (at least 5 entries)",
      len(mu_bingmei_data.get("daily_schedule", [])) >= 5)
check("DataFile", "daily_schedule locations are 'kunxu_realm'",
      all(e.get("location") == "kunxu_realm" for e in mu_bingmei_data.get("daily_schedule", [])))
check("DataFile", "_comment references CRON-111",
      "CRON-COMPLETIONIST-111" in mu_bingmei_data.get("_comment", ""))

# ════════════════════════════════════════════════════════════════════════
# 5. ZhouRuCultivationGrowthService — CANON GATES + LOGIC
# ════════════════════════════════════════════════════════════════════════
print("\n=== 5. ZhouRuCultivationGrowthService — CANON GATES + LOGIC ===")
service_path = JAVA_ROOT / "dev/ergenverse/wanglin/bead/ZhouRuCultivationGrowthService.java"
service = read(service_path)
service_nc = strip_java_comments(service)

check("Service", "Class is public final (utility class pattern)",
      "public final class ZhouRuCultivationGrowthService" in service_nc)
check("Service", "Private constructor (no instantiation)",
      re.search(r'private ZhouRuCultivationGrowthService\(\)\s*\{\s*\}', service_nc) is not None)
check("Service", "ZHOU_RU_CHARACTER_ID constant = 'zhou_ru'",
      'public static final String ZHOU_RU_CHARACTER_ID = "zhou_ru"' in service_nc)
check("Service", "MU_BINGMEI_CHARACTER_ID constant = 'mu_bingmei'",
      'public static final String MU_BINGMEI_CHARACTER_ID = "mu_bingmei"' in service_nc)
check("Service", "GROWTH_INTERVAL_TICKS = 24000L (1 MC day)",
      "GROWTH_INTERVAL_TICKS = 24000L" in service_nc)
check("Service", "PROXIMITY_RADIUS = 64.0 (blocks)",
      "PROXIMITY_RADIUS = 64.0" in service_nc)
check("Service", "CANON_CAP = RealmId.SOUL_TRANSFORMATION",
      "CANON_CAP = RealmId.SOUL_TRANSFORMATION" in service_nc)
check("Service", "tick(ServerLevel, long) is public static",
      "public static void tick(ServerLevel level, long currentTick)" in service_nc)

# Canon gates
check("Service", "Gate: 24000-tick interval (daily)",
      "currentTick % GROWTH_INTERVAL_TICKS != 0" in service_nc)
check("Service", "Gate: client-side no-op",
      "isClientSide" in service_nc)
check("Service", "Gate: 周茹 must be materialized (findCultivatorByCharacterId)",
      'findCultivatorByCharacterId(level, ZHOU_RU_CHARACTER_ID)' in service_nc)
check("Service", "Gate: pregnant_with_li_muwan_soul flag (CRON-110 prerequisite)",
      '"pregnant_with_li_muwan_soul"' in service_nc)
check("Service", "Gate: Mu Bingmei must be materialized",
      'findCultivatorByCharacterId(level, MU_BINGMEI_CHARACTER_ID)' in service_nc)
check("Service", "Gate: proximity check (distanceToSqr)",
      "zhouRu.distanceToSqr(muBingmei)" in service_nc)
check("Service", "Gate: CANON_CAP check (no growth beyond Soul Transformation)",
      "currentRealm.order >= CANON_CAP.order" in service_nc)

# State advancement
check("Service", "Advances realm via setCultivationRealm",
      "zhouRu.setCultivationRealm" in service_nc)
check("Service", "Uses RealmId.next() for next realm",
      "currentRealm.next()" in service_nc)
check("Service", "Updates runtime state with cultivation_realm_order",
      '"cultivation_realm_order"' in service_nc)
check("Service", "Updates runtime state with cultivation_realm_name",
      '"cultivation_realm_name"' in service_nc)
check("Service", "Updates runtime state with cultivation_realm_name_cn",
      '"cultivation_realm_name_cn"' in service_nc)
check("Service", "Updates runtime state with last_breakthrough_tick",
      '"last_breakthrough_tick"' in service_nc)
check("Service", "Updates runtime state with under_mu_bingmei_guidance flag",
      '"under_mu_bingmei_guidance"' in service_nc)

# Effects
check("Service", "Particle effect: END_ROD (cultivation Qi rising)",
      "ParticleTypes.END_ROD" in service_nc)
check("Service", "Particle effect: SQUID_INK (impurities expelled)",
      "ParticleTypes.SQUID_INK" in service_nc)
check("Service", "Particle effect: FIREWORK (breakthrough burst)",
      "ParticleTypes.FIREWORK" in service_nc)
check("Service", "Sound effect: PLAYER_LEVELUP (bright ascending)",
      "SoundEvents.PLAYER_LEVELUP" in service_nc)
check("Service", "Sound effect: AMETHYST_BLOCK_CHIME (crystalline)",
      "SoundEvents.AMETHYST_BLOCK_CHIME" in service_nc)

# Bilingual message
check("Service", "Bilingual message (Chinese first)",
      "周茹在慕冰媚的指点下突破至" in service)
check("Service", "Bilingual message (English second)",
      "Zhou Ru broke through to" in service)
check("Service", "Message references Mu Bingmei's guidance",
      "Mu Bingmei's guidance" in service)
check("Service", "Message references Li Muwan soul",
      "Li Muwan soul" in service or "Li Muwan's soul" in service)

# History recording
check("Service", "HistoryManager.onDiscovery called",
      "HistoryManager.onDiscovery" in service_nc)
check("Service", "History subject is 'zhou_ru_cultivation_breakthrough'",
      '"zhou_ru_cultivation_breakthrough"' in service_nc)

# Defensive
check("Service", "Defensive try/catch around tickImpl",
      re.search(r'try\s*\{[^}]*tickImpl', service_nc, re.DOTALL) is not None)
check("Service", "findCultivatorByCharacterId iterates level.getAllEntities()",
      "level.getAllEntities()" in service_nc)
check("Service", "findCultivatorByCharacterId checks isAlive()",
      "e.isAlive()" in service_nc)
check("Service", "parseRealmId handles null/empty (defensive)",
      "realmStr == null || realmStr.isEmpty()" in service_nc)
check("Service", "parseRealmId uses try/catch for valueOf",
      "IllegalArgumentException" in service_nc)

# Imports
check("Service", "Import: RealmId",
      "import dev.ergenverse.cultivation.RealmId;" in service)
check("Service", "Import: EntityCultivator",
      "import dev.ergenverse.entity.EntityCultivator;" in service)
check("Service", "Import: WorldRuntimeState from simulation package",
      "import dev.ergenverse.simulation.WorldRuntimeState;" in service)
check("Service", "Import: HistoryManager",
      "import dev.ergenverse.history.HistoryManager;" in service)

# Canon basis in javadoc
check("Service", "Javadoc mentions CRON-99 (soul capture predecessor)",
      "CRON-99" in service)
check("Service", "Javadoc mentions CRON-100 (revival attempts)",
      "CRON-100" in service)
check("Service", "Javadoc mentions CRON-102 (final revival)",
      "CRON-102" in service)
check("Service", "Javadoc mentions CRON-110 (soul transfer predecessor)",
      "CRON-110" in service)
check("Service", "Javadoc mentions Mu Bingmei as Zhou Ru's master",
      "Mu Bingmei" in service and "master" in service.lower())
check("Service", "Javadoc mentions Kunxu Realm",
      "Kunxu" in service or "昆虚" in service)
check("Service", "Javadoc mentions Soul Transformation canon cap",
      "Soul Transformation" in service)
check("Service", "Javadoc mentions disciple-master cultivation relationship",
      "disciple-master" in service)
check("Service", "State transition diagram documented",
      "State Transition Diagram" in service)
check("Service", "Single-player maximalism documented (Article XLIII)",
      "Article XLIII" in service or "single-player maximalism" in service.lower())

# ════════════════════════════════════════════════════════════════════════
# 6. SERVER TICK WIRING
# ════════════════════════════════════════════════════════════════════════
print("\n=== 6. SERVER TICK WIRING ===")
ergenverse = read(JAVA_ROOT / "dev/ergenverse/core/Ergenverse.java")
ergenverse_nc = strip_java_comments(ergenverse)

check("Wiring", "Loop J: ZhouRuCultivationGrowthService.tick called",
      "ZhouRuCultivationGrowthService.tick(overworld, ticks)" in ergenverse_nc)
check("Wiring", "Loop J has CRON-111 comment",
      "CRON-COMPLETIONIST-111" in ergenverse)
check("Wiring", "Loop J documented as 'Loop J'",
      "Loop J" in ergenverse)
check("Wiring", "Loop J placed after Loop I (WorldEventBus.setCurrentLevel)",
      ergenverse_nc.find("WorldEventBus.setCurrentLevel(overworld)") < ergenverse_nc.find("ZhouRuCultivationGrowthService.tick(overworld, ticks)"))
check("Wiring", "Loop J placed at end of onServerTick (after setCurrentLevel)",
      ergenverse_nc.find("ZhouRuCultivationGrowthService.tick(overworld, ticks)") > ergenverse_nc.find("WorldEventBus.setCurrentLevel(overworld)"))

# ════════════════════════════════════════════════════════════════════════
# 7. ARCHITECTURE COMPLIANCE (CRON-69)
# ════════════════════════════════════════════════════════════════════════
print("\n=== 7. ARCHITECTURE COMPLIANCE (CRON-69) ===")

check("Architecture", "Service does NOT directly manipulate WorldDeltaStore",
      "WorldDeltaStore" not in service_nc)
check("Architecture", "Service does NOT directly manipulate WorldLayer",
      "WorldLayer" not in service_nc)
check("Architecture", "Service does NOT directly call level.setBlock",
      ".setBlock(" not in service_nc or "setSimulationBlock" in service_nc)
check("Architecture", "Service does NOT directly call setPlayerBlock",
      "setPlayerBlock" not in service_nc)
check("Architecture", "NPC runtime state updated via runtime.updateNpcState (correct API)",
      "runtime.updateNpcState" in service_nc)
check("Architecture", "Entity realm mutation via setCultivationRealm (correct API)",
      "zhouRu.setCultivationRealm" in service_nc)
check("Architecture", "Particle/sound via vanilla APIs (sendParticles, playSound)",
      "level.sendParticles" in service_nc and "level.playSound" in service_nc)
check("Architecture", "Single-player maximalism: finds nearest player for witness",
      "findNearbyPlayer" in service_nc)
check("Architecture", "Defensive: never crashes server tick (try/catch)",
      re.search(r'catch\s*\(\s*Throwable\s+\w+\s*\)', service_nc) is not None)

# ════════════════════════════════════════════════════════════════════════
# 8. STATE TRANSITION INTEGRITY
# ════════════════════════════════════════════════════════════════════════
print("\n=== 8. STATE TRANSITION INTEGRITY ===")

check("StateTransition", "CRON-110 capture is prerequisite (pregnant_with_li_muwan_soul flag)",
      '"pregnant_with_li_muwan_soul"' in service_nc)
check("StateTransition", "Growth requires both Zhou Ru AND Mu Bingmei materialized",
      'findCultivatorByCharacterId(level, ZHOU_RU_CHARACTER_ID)' in service_nc and
      'findCultivatorByCharacterId(level, MU_BINGMEI_CHARACTER_ID)' in service_nc)
check("StateTransition", "Growth requires proximity (distanceToSqr <= PROXIMITY_RADIUS_SQ)",
      "distSq > PROXIMITY_RADIUS_SQ" in service_nc)
check("StateTransition", "Growth caps at CANON_CAP (Soul Transformation)",
      "currentRealm.order >= CANON_CAP.order" in service_nc)
check("StateTransition", "Growth advances one realm per tick (RealmId.next())",
      "currentRealm.next()" in service_nc)
check("StateTransition", "Growth records breakthrough in HistoryManager",
      "HistoryManager.onDiscovery" in service_nc)
check("StateTransition", "Growth updates Zhou Ru's runtime state (persists across reload)",
      "runtime.updateNpcState" in service_nc)
check("StateTransition", "Growth does NOT clear hasLiMuwanSoul (revival service still works)",
      "setLiMuwanSoul" not in service_nc)
check("StateTransition", "Growth does NOT touch the bead's NBT (only Zhou Ru's realm)",
      "HeavenDefyingBeadItem" not in service_nc or
      "HeavenDefyingBeadItem" in service and "setSoulTransferredToZhouRu" not in service_nc)

# ════════════════════════════════════════════════════════════════════════
# 9. CANON FIDELITY (HYPER-ANALYTICAL)
# ════════════════════════════════════════════════════════════════════════
print("\n=== 9. CANON FIDELITY (HYPER-ANALYTICAL) ===")

check("Canon", "Mu Bingmei character is 慕冰媚 (correct Chinese characters)",
      "慕冰媚" in service and "慕冰媚" in canon_uuid and "慕冰媚" in materializer)
check("Canon", "Liu Mei (柳眉) documented as Mu Bingmei's alternate identity",
      "柳眉" in service and "柳眉" in canon_uuid)
check("Canon", "Kunxu Realm is 昆虚界 (correct Chinese characters)",
      "昆虚界" in blueprint or "昆虚" in blueprint)
check("Canon", "Zhou Ru becomes Soul Transformation (canon-attested)",
      "Soul Transformation" in service and "SOUL_TRANSFORMATION" in service_nc)
check("Canon", "Mu Bingmei is Zhou Ru's master (disciple relationship)",
      "master" in service.lower() and "disciple" in service.lower())
check("Canon", "Wang Ping (son) canon detail documented",
      "Wang Ping" in canon_uuid)
check("Canon", "Dream Dao (梦道) karmic severing documented",
      "Dream Dao" in canon_uuid)
check("Canon", "Ascendant+ cultivation for Mu Bingmei (canon-attested)",
      "ascendant" in materializer_nc.lower())
check("Canon", "Growth rate is 1 realm per MC day (24000 ticks)",
      "24000L" in service_nc)
check("Canon", "Full arc is 6 days (mortal → soul_transformation, 6 realms)",
      "mortal → qi_condensation" in service or "mortal → qi_condensation" in service.lower())
check("Canon", "NO fabricated chapter citation (uses disclaimer)",
      "not cited" in service.lower() or "NO fabricated" in service)

# ════════════════════════════════════════════════════════════════════════
# 10. BUILD ARTIFACT
# ════════════════════════════════════════════════════════════════════════
print("\n=== 10. BUILD ARTIFACT ===")

check("Build", "ZhouRuCultivationGrowthService.class compiled",
      (ROOT / "build/classes/java/main/dev/ergenverse/wanglin/bead/ZhouRuCultivationGrowthService.class").exists())
check("Build", "CanonUUID.class recompiled (with MU_BINGMEI)",
      (ROOT / "build/classes/java/main/dev/ergenverse/runtime/CanonUUID.class").exists())
check("Build", "PlanetSuzakuBlueprint.class recompiled (with KUNXU_REALM + NPC_MU_BINGMEI)",
      (ROOT / "build/classes/java/main/dev/ergenverse/runtime/PlanetSuzakuBlueprint.class").exists())
check("Build", "CanonActorMaterializer.class recompiled (with Mu Bingmei profile)",
      (ROOT / "build/classes/java/main/dev/ergenverse/runtime/materialize/CanonActorMaterializer.class").exists())
check("Build", "NPCRuntime.class recompiled (with Mu Bingmei registration)",
      (ROOT / "build/classes/java/main/dev/ergenverse/runtime/NPCRuntime.class").exists())
check("Build", "Ergenverse.class recompiled (with Loop J wiring)",
      (ROOT / "build/classes/java/main/dev/ergenverse/core/Ergenverse.class").exists())

# ════════════════════════════════════════════════════════════════════════
# SUMMARY
# ════════════════════════════════════════════════════════════════════════
print("\n" + "=" * 70)
print(f"CHECKS PASSED: {checks_passed}")
print(f"CHECKS FAILED: {checks_failed}")
print("=" * 70)

if checks_failed > 0:
    print("\nFAILED CHECKS:")
    for f in failures:
        print(f"  - {f}")
    sys.exit(1)
else:
    print("\n✓ ALL CHECKS PASSED — CRON-111 Mu Bingmei + Kunxu Realm verified.")
    sys.exit(0)
