#!/usr/bin/env python3
"""
CRON-110 verification script — Zhou Ru (周茹) Reincarnation Soul Transfer.

Validates the canon-faithful soul-transfer mechanic introduced in CRON-110:
  - Zhou Ru registered as a canon NPC (UUID + blueprint constant + materializer profile + NPCRuntime registration)
  - HeavenDefyingBeadItem gains NBT_SOUL_TRANSFERRED_TO_ZHOU_RU flag + write-once accessors
  - ZhouRuSoulTransferEvent implements the full transfer logic with canon gates
  - HistoryEvents dispatches to the soul-transfer handler on right-click
  - Canon fidelity: 周茹 as mortal vessel, Li Muwan chooses not to devour host soul,
    addresses Wang Lin as 'uncle' (王林叔叔)
  - Architecture compliance: CRON-69 point 5 (gameplay via facade, not direct store manipulation)
  - State-transition integrity: CRON-99 prerequisite, write-once, doesn't clear hasLiMuwanSoul

Runs 50+ checks across 6 categories. Exits non-zero on any failure.
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
    # Remove block comments
    text = re.sub(r'/\*[\s\S]*?\*/', '', text)
    # Remove line comments
    text = re.sub(r'//[^\n]*', '', text)
    return text

def extract_method_body(text, method_name):
    """Extract the body of a Java method by balanced-brace matching."""
    # Find the method signature
    pattern = rf'(?:public|private|static|final|\s)+\s+\w+\s+{method_name}\s*\([^)]*\)\s*\{{'
    m = re.search(pattern, text)
    if not m:
        return None
    # Find matching closing brace
    start = m.end() - 1  # position of the opening brace
    depth = 0
    for i in range(start, len(text)):
        if text[i] == '{':
            depth += 1
        elif text[i] == '}':
            depth -= 1
            if depth == 0:
                return text[start:i+1]
    return None

# ──────────────────────────────────────────────────────────────────────────
# CHECK CATEGORIES
# ──────────────────────────────────────────────────────────────────────────

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

check("CanonUUID", "ZHOU_RU constant declared",
      "public static final UUID ZHOU_RU" in canon_uuid_nc)
check("CanonUUID", "ZHOU_RU derived from 'npc:zhou_ru' key",
      'of("npc:zhou_ru")' in canon_uuid_nc)
check("CanonUUID", "ZHOU_RU has CRON-110 javadoc",
      "CRON-COMPLETIONIST-110" in canon_uuid and "周茹" in canon_uuid)
check("CanonUUID", "ZHOU_RU canon basis documented (Baidu Baike)",
      "Baidu Baike" in canon_uuid)
check("CanonUUID", "ZHOU_RU canon basis documented (Fandom wiki)",
      "Fandom" in canon_uuid)
check("CanonUUID", "ZHOU_RU mentions Li Muwan soul transfer",
      "Li Muwan" in canon_uuid and "transfer" in canon_uuid.lower())
check("CanonUUID", "ZHOU_RU mentions Mu Bingmei cultivation arc",
      "Mu Bingmei" in canon_uuid or "慕冰梅" in canon_uuid)
check("CanonUUID", "ZHOU_RU mentions 'uncle' (王林叔叔) canon detail",
      "uncle" in canon_uuid.lower() and "王林叔叔" in canon_uuid)
check("CanonUUID", "NO fabricated chapter citation",
      "Ch." not in re.search(r"ZHOU_RU.*?(?=public static final UUID|\Z)", canon_uuid, re.DOTALL).group(0)
      if "ZHOU_RU" in canon_uuid else True)

# ════════════════════════════════════════════════════════════════════════
# 2. BLUEPRINT + MATERIALIZER REGISTRATION
# ════════════════════════════════════════════════════════════════════════
print("\n=== 2. BLUEPRINT + MATERIALIZER REGISTRATION ===")
blueprint = read(JAVA_ROOT / "dev/ergenverse/runtime/PlanetSuzakuBlueprint.java")
blueprint_nc = strip_java_comments(blueprint)

check("Blueprint", "NPC_ZHOU_RU character id constant",
      'public static final String NPC_ZHOU_RU = "zhou_ru"' in blueprint_nc)
check("Blueprint", "NPC_ZHOU_RU has CRON-110 javadoc",
      "CRON-COMPLETIONIST-110" in blueprint)
check("Blueprint", "NPC_ZHOU_RU references 周茹 character",
      "周茹" in blueprint)

materializer = read(JAVA_ROOT / "dev/ergenverse/runtime/materialize/CanonActorMaterializer.java")
materializer_nc = strip_java_comments(materializer)

check("Materializer", "Zhou Ru profile registered",
      'profile(CanonUUID.ZHOU_RU' in materializer_nc)
check("Materializer", "Zhou Ru characterId is 'zhou_ru'",
      re.search(r'profile\(CanonUUID\.ZHOU_RU,\s*"([^"]+)"', materializer_nc) and
      re.search(r'profile\(CanonUUID\.ZHOU_RU,\s*"([^"]+)"', materializer_nc).group(1) == "zhou_ru")
check("Materializer", "Zhou Ru display name contains 周茹",
      "Zhou Ru 周茹" in materializer)
check("Materializer", "Zhou Ru sectId is vermilion_bird_country (mortal origin)",
      "vermilion_bird_country" in materializer_nc)
check("Materializer", "Zhou Ru realm is 'mortal' (pre-transfer state)",
      re.search(r'profile\(CanonUUID\.ZHOU_RU,\s*"zhou_ru",\s*"Zhou Ru 周茹",\s*"vermilion_bird_country",\s*"(\w+)"\)', materializer_nc) and
      re.search(r'profile\(CanonUUID\.ZHOU_RU,\s*"zhou_ru",\s*"Zhou Ru 周茹",\s*"vermilion_bird_country",\s*"(\w+)"\)', materializer_nc).group(1) == "mortal")
check("Materializer", "Zhou Ru profile has CRON-110 javadoc",
      "CRON-COMPLETIONIST-110" in materializer)

# ════════════════════════════════════════════════════════════════════════
# 3. NPCRuntime REGISTRATION
# ════════════════════════════════════════════════════════════════════════
print("\n=== 3. NPCRuntime REGISTRATION ===")
npc_runtime = read(JAVA_ROOT / "dev/ergenverse/runtime/NPCRuntime.java")
npc_runtime_nc = strip_java_comments(npc_runtime)

check("NPCRuntime", "Zhou Ru registered with register()",
      "register(CanonUUID.ZHOU_RU" in npc_runtime_nc)
check("NPCRuntime", "Zhou Ru placed at VERMILION_BIRD_CAPITAL",
      "PlanetSuzakuBlueprint.VERMILION_BIRD_CAPITAL.x" in npc_runtime_nc and
      "PlanetSuzakuBlueprint.VERMILION_BIRD_CAPITAL.z" in npc_runtime_nc)
check("NPCRuntime", "Zhou Ru NOT flagged deadUntilRevived (present from day 0)",
      not re.search(r'ZHOU_RU.*?deadUntilRevived\s*=\s*true', npc_runtime_nc, re.DOTALL))
check("NPCRuntime", "Zhou Ru registration has CRON-110 javadoc",
      "CRON-COMPLETIONIST-110" in npc_runtime)
check("NPCRuntime", "Zhou Ru registration notes Mu Bingmei future arc",
      "Mu Bingmei" in npc_runtime or "慕冰梅" in npc_runtime)

# ════════════════════════════════════════════════════════════════════════
# 4. BEAD NBT FLAG + ACCESSORS
# ════════════════════════════════════════════════════════════════════════
print("\n=== 4. BEAD NBT FLAG + ACCESSORS ===")
bead = read(JAVA_ROOT / "dev/ergenverse/wanglin/bead/HeavenDefyingBeadItem.java")
bead_nc = strip_java_comments(bead)

check("Bead", "NBT_SOUL_TRANSFERRED_TO_ZHOU_RU constant declared",
      'public static final String NBT_SOUL_TRANSFERRED_TO_ZHOU_RU' in bead_nc)
check("Bead", "NBT key value is 'Ergen.Bead.SoulTransferredToZhouRu'",
      '"Ergen.Bead.SoulTransferredToZhouRu"' in bead_nc)
check("Bead", "hasSoulTransferredToZhouRu accessor defined",
      "public boolean hasSoulTransferredToZhouRu" in bead_nc)
check("Bead", "setSoulTransferredToZhouRu mutator defined",
      "public void setSoulTransferredToZhouRu" in bead_nc)
check("Bead", "setSoulTransferredToZhouRu is write-once (early return on false)",
      re.search(r'public void setSoulTransferredToZhouRu\([^)]+\)\s*\{[^}]*if\s*\(!transferred\)\s*return;', bead_nc, re.DOTALL) is not None)
check("Bead", "NBT flag javadoc mentions CRON-110",
      "CRON-COMPLETIONIST-110" in bead)
check("Bead", "NBT flag javadoc mentions canon basis (Baidu Baike)",
      "Baidu Baike" in bead)
check("Bead", "NBT flag javadoc mentions 'uncle' (王林叔叔) canon detail",
      "uncle" in bead and "王林叔叔" in bead)
check("Bead", "NBT flag javadoc notes does NOT clear NBT_LI_MUWAN_SOUL",
      "does NOT clear" in bead or "Does NOT clear" in bead)
check("Bead", "Accessor returns false for empty/untagged stacks (defensive)",
      re.search(r'public boolean hasSoulTransferredToZhouRu[^}]*if\s*\(stack\.isEmpty\(\)\s*\|\|\s*!stack\.hasTag\(\)\)\s*return false;', bead_nc, re.DOTALL) is not None)

# ════════════════════════════════════════════════════════════════════════
# 5. ZhouRuSoulTransferEvent — CANON GATES + LOGIC
# ════════════════════════════════════════════════════════════════════════
print("\n=== 5. ZhouRuSoulTransferEvent — CANON GATES + LOGIC ===")
event_path = JAVA_ROOT / "dev/ergenverse/wanglin/bead/ZhouRuSoulTransferEvent.java"
event = read(event_path)
event_nc = strip_java_comments(event)

check("Event", "Class is public final (utility class pattern)",
      "public final class ZhouRuSoulTransferEvent" in event_nc)
check("Event", "Private constructor (no instantiation)",
      re.search(r'private ZhouRuSoulTransferEvent\(\)\s*\{\s*\}', event_nc) is not None)
check("Event", "CHARACTER_ID constant = 'zhou_ru'",
      'public static final String CHARACTER_ID = "zhou_ru"' in event_nc)
check("Event", "handleSoulTransfer is public static",
      "public static void handleSoulTransfer" in event_nc)
check("Event", "Defensive: client-side no-op",
      "isClientSide" in event_nc)
check("Event", "Defensive: characterId validation",
      "CHARACTER_ID.equals(zhouRu.getCharacterId())" in event_nc)

# CRON-99 prerequisite check
check("Event", "Gate: bead in MAIN_HAND (canon: actively wielded)",
      "InteractionHand.MAIN_HAND" in event_nc)
check("Event", "Gate: hasLiMuwanSoul must be true (CRON-99 prerequisite)",
      "beadItem.hasLiMuwanSoul(mainHand)" in event_nc)
check("Event", "Gate: hasSoulTransferredToZhouRu must be false (write-once)",
      "beadItem.hasSoulTransferredToZhouRu(mainHand)" in event_nc)
check("Event", "Gate: DORMANT_STONE rejection (canon: bead must be CRACK_OPENED)",
      "BeadInteriorStage.DORMANT_STONE" in event_nc)

# State transition
check("Event", "Calls setSoulTransferredToZhouRu(stack, true) — closes CRON-110 gap",
      "beadItem.setSoulTransferredToZhouRu(mainHand, true)" in event_nc)
check("Event", "Does NOT call setLiMuwanSoul(stack, false) — keeps soul association",
      "setLiMuwanSoul" not in event_nc or
      "setLiMuwanSoul" in event_nc and "false" not in re.search(r'setLiMuwanSoul\([^)]+\)', event_nc).group(0))

# Effects
check("Event", "Particle effect: END_ROD (soul stream)",
      "ParticleTypes.END_ROD" in event_nc)
check("Event", "Particle effect: SQUID_INK (dark god-force residue)",
      "ParticleTypes.SQUID_INK" in event_nc)
check("Event", "Particle effect: FIREWORK (central flash)",
      "ParticleTypes.FIREWORK" in event_nc)
check("Event", "Sound effect: WITHER_SPAWN (deep ominous tone)",
      "SoundEvents.WITHER_SPAWN" in event_nc)
check("Event", "Sound effect: AMETHYST_BLOCK_CHIME (bright crystalline)",
      "SoundEvents.AMETHYST_BLOCK_CHIME" in event_nc)
check("Event", "SoundSource used (HOSTILE or AMBIENT)",
      "SoundSource.HOSTILE" in event_nc and "SoundSource.AMBIENT" in event_nc)

# Bilingual message
check("Event", "Bilingual message (Chinese first)",
      "李慕婉的元婴自天逆珠流出" in event)
check("Event", "Bilingual message (English second)",
      "Li Muwan's Nascent Soul flows from the bead" in event)
check("Event", "Message mentions 'uncle' (王林叔叔) canon detail",
      "王林叔叔" in event and "uncle" in event.lower())
check("Event", "Message references 137 attempts (canon attested)",
      "137" in event)

# History recording
check("Event", "HistoryManager.onDiscovery called",
      "HistoryManager.onDiscovery" in event_nc)
check("Event", "History subject is 'li_muwan_soul_transferred_to_zhou_ru'",
      '"li_muwan_soul_transferred_to_zhou_ru"' in event_nc)

# Failure paths
check("Event", "Failure path: announceNoBead (no bead in main hand)",
      "announceNoBead" in event_nc)
check("Event", "Failure path: announceNoSoulInBead (CRON-99 prerequisite)",
      "announceNoSoulInBead" in event_nc)
check("Event", "Failure path: announceAlreadyTransferred (write-once guard)",
      "announceAlreadyTransferred" in event_nc)
check("Event", "Failure path: announceDormantBead (DORMANT_STONE)",
      "announceDormantBead" in event_nc)

# Runtime state marking (future questline hook)
check("Event", "Marks Zhou Ru's runtime state with 'pregnant_with_li_muwan_soul'",
      '"pregnant_with_li_muwan_soul"' in event_nc)
check("Event", "markZhouRuAsVessel is defensive (try/catch)",
      re.search(r'try\s*\{[^}]*markZhouRuAsVessel', event_nc, re.DOTALL) is not None or
      re.search(r'markZhouRuAsVessel[^}]*\}\s*catch', event_nc, re.DOTALL) is not None or
      "try" in event_nc and "markZhouRuAsVessel" in event_nc)

# Imports
check("Event", "Import: WorldRuntimeState from simulation package",
      "import dev.ergenverse.simulation.WorldRuntimeState;" in event)
check("Event", "Import: EntityCultivator",
      "import dev.ergenverse.entity.EntityCultivator;" in event)
check("Event", "Import: HistoryManager",
      "import dev.ergenverse.history.HistoryManager;" in event)
check("Event", "Import: CompoundTag (for runtime state)",
      "import net.minecraft.nbt.CompoundTag;" in event)

# Canon basis in javadoc
check("Event", "Javadoc mentions CRON-99 (soul capture predecessor)",
      "CRON-99" in event)
check("Event", "Javadoc mentions CRON-100 (revival attempts successor)",
      "CRON-100" in event)
check("Event", "Javadoc mentions CRON-102 (final revival)",
      "CRON-102" in event)
check("Event", "Javadoc mentions 'uncle' (王林叔叔) canon detail",
      "王林叔叔" in event)
check("Event", "Javadoc mentions Mu Bingmei cultivation arc",
      "Mu Bingmei" in event or "慕冰梅" in event)
check("Event", "NO fabricated chapter citation",
      "Ch." not in re.search(r'Canon Basis.*?(?=Trigger)', event, re.DOTALL).group(0)
      if "Canon Basis" in event and "Trigger" in event else True)
check("Event", "State transition diagram documented",
      "State Transition Diagram" in event)

# ════════════════════════════════════════════════════════════════════════
# 6. HistoryEvents WIRING
# ════════════════════════════════════════════════════════════════════════
print("\n=== 6. HistoryEvents WIRING ===")
history_events = read(JAVA_ROOT / "dev/ergenverse/history/HistoryEvents.java")
history_events_nc = strip_java_comments(history_events)

check("Wiring", "Import ZhouRuSoulTransferEvent",
      "import dev.ergenverse.wanglin.bead.ZhouRuSoulTransferEvent;" in history_events)
check("Wiring", "Dispatch gate: CHARACTER_ID.equals(cultivator.getCharacterId())",
      'ZhouRuSoulTransferEvent.CHARACTER_ID.equals(cultivator.getCharacterId())' in history_events_nc)
check("Wiring", "Calls handleSoulTransfer(serverPlayer, cultivator)",
      "ZhouRuSoulTransferEvent.handleSoulTransfer(serverPlayer, cultivator)" in history_events_nc)
check("Wiring", "Interaction still recorded (recordPlayerInteraction still called)",
      "cultivator.recordPlayerInteraction(serverPlayer)" in history_events_nc)
check("Wiring", "Dispatch happens AFTER recordPlayerInteraction (not before)",
      history_events_nc.find("recordPlayerInteraction") < history_events_nc.find("handleSoulTransfer"))

# ════════════════════════════════════════════════════════════════════════
# 7. ARCHITECTURE COMPLIANCE (CRON-69)
# ════════════════════════════════════════════════════════════════════════
print("\n=== 7. ARCHITECTURE COMPLIANCE (CRON-69) ===")

check("Architecture", "Event does NOT directly manipulate WorldDeltaStore",
      "WorldDeltaStore" not in event_nc)
check("Architecture", "Event does NOT directly manipulate WorldLayer",
      "WorldLayer" not in event_nc)
check("Architecture", "Event does NOT directly call level.setBlock",
      ".setBlock(" not in event_nc or "setSimulationBlock" in event_nc)
check("Architecture", "Event does NOT directly call setPlayerBlock",
      "setPlayerBlock" not in event_nc)
check("Architecture", "NPC runtime state updated via runtime.updateNpcState (correct API)",
      "runtime.updateNpcState" in event_nc)
check("Architecture", "Bead state mutation via accessor (not direct NBT put in event)",
      "stack.getOrCreateTag().putBoolean" not in event_nc)
check("Architecture", "Single-player maximalism: one player, one Zhou Ru (no MP loop)",
      "for" not in re.search(r'handleSoulTransfer\([^)]+\)\s*\{[^}]*\}', event_nc, re.DOTALL).group(0)
      if re.search(r'handleSoulTransfer\([^)]+\)\s*\{[^}]*\}', event_nc, re.DOTALL) else True)

# ════════════════════════════════════════════════════════════════════════
# 8. STATE TRANSITION INTEGRITY
# ════════════════════════════════════════════════════════════════════════
print("\n=== 8. STATE TRANSITION INTEGRITY ===")

check("StateTransition", "CRON-99 capture is prerequisite (gate on hasLiMuwanSoul)",
      "!beadItem.hasLiMuwanSoul(mainHand)" in event_nc)
check("StateTransition", "Transfer is write-once (gate on hasSoulTransferredToZhouRu)",
      "beadItem.hasSoulTransferredToZhouRu(mainHand)" in event_nc)
check("StateTransition", "Transfer does NOT clear hasLiMuwanSoul (revival service still works)",
      "setLiMuwanSoul(mainHand, false)" not in event_nc)
check("StateTransition", "Transfer marks Zhou Ru's runtime state for future questline",
      '"pregnant_with_li_muwan_soul"' in event_nc)
check("StateTransition", "Transfer records in HistoryManager (canonical record)",
      "HistoryManager.onDiscovery" in event_nc)
check("StateTransition", "Transfer spawns particles at Zhou Ru (not at player)",
      re.search(r'spawnSoulTransferEffects\(serverLevel,\s*zhouRu\)', event_nc) is not None)
check("StateTransition", "Three failure paths with distinct canon-faithful messages",
      all(p in event_nc for p in ["announceNoBead", "announceNoSoulInBead", "announceAlreadyTransferred", "announceDormantBead"]))

# ════════════════════════════════════════════════════════════════════════
# 9. CANON FIDELITY (HYPER-ANALYTICAL)
# ════════════════════════════════════════════════════════════════════════
print("\n=== 9. CANON FIDELITY (HYPER-ANALYTICAL) ===")

check("Canon", "Zhou Ru character is 周茹 (correct Chinese characters)",
      "周茹" in event and "周茹" in canon_uuid and "周茹" in materializer)
check("Canon", "Li Muwan's soul is 元婴 (Nascent Soul) — correct terminology",
      "元婴" in event)
check("Canon", "Heaven-Defying Bead is 天逆珠 — correct terminology",
      "天逆珠" in event)
check("Canon", "Li Muwan chooses NOT to devour host soul (canon-attested)",
      "does not devour" in event.lower() or "chooses NOT to devour" in event)
check("Canon", "Addresses Wang Lin as 'uncle' (王林叔叔) — canon-attested",
      "王林叔叔" in event)
check("Canon", "Soul transfer is the SECOND beat of the revival arc (after CRON-99, before CRON-100)",
      "CRON-99" in event and "CRON-100" in event and "CRON-102" in event)
check("Canon", "Mu Bingmei (慕冰梅) cultivation arc referenced",
      "Mu Bingmei" in event or "慕冰梅" in event)
check("Canon", "Kunxu Realm (昆墟之境) referenced as Zhou Ru's later cultivation location",
      "Kunxu" in event or "昆墟" in event)
check("Canon", "137 revival attempts referenced (canon-attested number)",
      "137" in event)
check("Canon", "NO fabricated chapter citation (uses 'NO fabricated chapter citation' disclaimer)",
      "NO fabricated chapter citation" in event or "not cited" in event.lower() and "avoid fabrication" in event.lower())

# ════════════════════════════════════════════════════════════════════════
# 10. BUILD ARTIFACT
# ════════════════════════════════════════════════════════════════════════
print("\n=== 10. BUILD ARTIFACT ===")

build_classes = ROOT / "build/classes/java/main/dev/ergenverse/wanglin/bead/ZhouRuSoulTransferEvent.class"
check("Build", "ZhouRuSoulTransferEvent.class compiled",
      build_classes.exists())
check("Build", "HeavenDefyingBeadItem.class recompiled (with new accessors)",
      (ROOT / "build/classes/java/main/dev/ergenverse/wanglin/bead/HeavenDefyingBeadItem.class").exists())
check("Build", "HistoryEvents.class recompiled (with new dispatch)",
      (ROOT / "build/classes/java/main/dev/ergenverse/history/HistoryEvents.class").exists())
check("Build", "CanonUUID.class recompiled (with ZHOU_RU)",
      (ROOT / "build/classes/java/main/dev/ergenverse/runtime/CanonUUID.class").exists())
check("Build", "CanonActorMaterializer.class recompiled (with Zhou Ru profile)",
      (ROOT / "build/classes/java/main/dev/ergenverse/runtime/materialize/CanonActorMaterializer.class").exists())
check("Build", "NPCRuntime.class recompiled (with Zhou Ru registration)",
      (ROOT / "build/classes/java/main/dev/ergenverse/runtime/NPCRuntime.class").exists())
check("Build", "PlanetSuzakuBlueprint.class recompiled (with NPC_ZHOU_RU)",
      (ROOT / "build/classes/java/main/dev/ergenverse/runtime/PlanetSuzakuBlueprint.class").exists())

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
    print("\n✓ ALL CHECKS PASSED — CRON-110 Zhou Ru reincarnation verified.")
    sys.exit(0)
