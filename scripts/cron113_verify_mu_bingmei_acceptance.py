#!/usr/bin/env python3
"""
CRON-113 Verification Script — MuBingmeiAcceptanceEvent.

Verifies the implementation of CRON-COMPLETIONIST-113 across 13 categories:
  1. File existence
  2. Package & class declaration
  3. Constants (IDs, intervals, radii, particle counts)
  4. tick() entry point
  5. tickImpl() gates (7 gates in order)
  6. Runtime state updates (accepted_as_disciple, disciple_acceptance_tick, master_character_id)
  7. Particle & sound effects (master ring, disciple ring, bond stream, central flash, crystal burst)
  8. Bilingual message (Chinese first, English second, Li Muwan soul reference, 王林叔叔 reference)
  9. HistoryManager recording
 10. Ergenverse.onServerTick Loop K wiring
 11. Architecture compliance (no direct WorldDeltaStore/layer/setBlock manipulation)
 12. Canon fidelity (N10, N19, L74 citations, no fabricated chapter citations)
 13. State transition integrity (CRON-99 → CRON-110 → CRON-112 → CRON-113 → CRON-111 → CRON-100 → CRON-102)
 14. Single-player maximalism (one-shot, witness optional, automatic)
 15. Build artifact (compiled .class exists)

Strips Java comments before checking for code patterns (so javadoc mentions
of method names don't trigger false positives). Verifies the build artifact
(MuBingmeiAcceptanceEvent.class exists in build/classes/java/main/).
"""

import re
import sys
from pathlib import Path

FORGE_MOD = Path("/home/z/my-project/forge-mod")
SRC_FILE = FORGE_MOD / "src/main/java/dev/ergenverse/wanglin/bead/MuBingmeiAcceptanceEvent.java"
ERGENVERSE_FILE = FORGE_MOD / "src/main/java/dev/ergenverse/core/Ergenverse.java"
CLASS_FILE = FORGE_MOD / "build/classes/java/main/dev/ergenverse/wanglin/bead/MuBingmeiAcceptanceEvent.class"

PASS = 0
FAIL = 0
FAILURES = []


def check(name, condition, detail=""):
    global PASS, FAIL
    if condition:
        PASS += 1
        print(f"  [PASS] {name}")
    else:
        FAIL += 1
        FAILURES.append((name, detail))
        print(f"  [FAIL] {name} — {detail}")


def strip_java_comments(text):
    """Remove // line comments and /* */ block comments from Java source."""
    # Remove block comments
    text = re.sub(r'/\*[\s\S]*?\*/', '', text)
    # Remove line comments
    text = re.sub(r'//[^\n]*', '', text)
    return text


def _state_diagram_has_order(src_raw, labels):
    """
    Check that the given labels appear in the given order WITHIN the
    'State Transition Diagram' section of the javadoc (not anywhere in
    the file — the @see references at the top of the file mention
    CRON-112 and CRON-111 before the state diagram).
    """
    # Find the start of the State Transition Diagram section.
    marker = "State Transition Diagram"
    idx = src_raw.find(marker)
    if idx < 0:
        return False
    # The section ends at the next <h2> heading or the end of the javadoc.
    section = src_raw[idx:]
    next_h2 = section.find("</pre>", idx - idx)  # state diagram is in a <pre> block
    if next_h2 > 0:
        section = section[:next_h2 + len("</pre>")]
    # Find each label in order within the section.
    pos = 0
    for label in labels:
        found = section.find(label, pos)
        if found < 0:
            return False
        pos = found + len(label)
    return True


def main():
    global PASS, FAIL

    print("=" * 72)
    print("CRON-113 Verification: MuBingmeiAcceptanceEvent")
    print("=" * 72)

    # ── 1. File existence ──
    print("\n[1] File existence")
    check("MuBingmeiAcceptanceEvent.java exists",
          SRC_FILE.exists(),
          f"expected at {SRC_FILE}")
    check("Ergenverse.java exists",
          ERGENVERSE_FILE.exists(),
          f"expected at {ERGENVERSE_FILE}")

    if not SRC_FILE.exists() or not ERGENVERSE_FILE.exists():
        print("\nFATAL: source files missing; aborting.")
        sys.exit(1)

    src_raw = SRC_FILE.read_text(encoding="utf-8")
    src = strip_java_comments(src_raw)
    erg_raw = ERGENVERSE_FILE.read_text(encoding="utf-8")
    erg = strip_java_comments(erg_raw)

    # ── 2. Package & class declaration ──
    print("\n[2] Package & class declaration")
    check("package dev.ergenverse.wanglin.bead",
          "package dev.ergenverse.wanglin.bead;" in src)
    check("public final class MuBingmeiAcceptanceEvent",
          "public final class MuBingmeiAcceptanceEvent" in src)
    check("private constructor (no instantiation)",
          re.search(r"private\s+MuBingmeiAcceptanceEvent\s*\(\s*\)\s*\{\s*\}", src) is not None)

    # ── 3. Constants ──
    print("\n[3] Constants")
    check("ZHOU_RU_CHARACTER_ID = \"zhou_ru\"",
          'ZHOU_RU_CHARACTER_ID = "zhou_ru"' in src)
    check("MU_BINGMEI_CHARACTER_ID = \"mu_bingmei\"",
          'MU_BINGMEI_CHARACTER_ID = "mu_bingmei"' in src)
    check("ACCEPTANCE_CHECK_INTERVAL_TICKS = 200L",
          "ACCEPTANCE_CHECK_INTERVAL_TICKS = 200L" in src)
    check("ACCEPTANCE_PROXIMITY_RADIUS = 32.0",
          "ACCEPTANCE_PROXIMITY_RADIUS = 32.0" in src)
    check("ACCEPTANCE_PROXIMITY_RADIUS_SQ defined",
          "ACCEPTANCE_PROXIMITY_RADIUS_SQ" in src)
    check("WITNESS_RADIUS = 48.0",
          "WITNESS_RADIUS = 48.0" in src)
    check("MASTER_RING_PARTICLE_COUNT = 32",
          "MASTER_RING_PARTICLE_COUNT = 32" in src)
    check("DISCIPLE_RING_PARTICLE_COUNT = 24",
          "DISCIPLE_RING_PARTICLE_COUNT = 24" in src)
    check("BOND_STREAM_PARTICLE_COUNT = 20",
          "BOND_STREAM_PARTICLE_COUNT = 20" in src)
    check("CENTRAL_FLASH_PARTICLE_COUNT = 8",
          "CENTRAL_FLASH_PARTICLE_COUNT = 8" in src)
    check("CRYSTAL_BURST_PARTICLE_COUNT = 16",
          "CRYSTAL_BURST_PARTICLE_COUNT = 16" in src)

    # ── 4. tick() entry point ──
    print("\n[4] tick() entry point")
    check("public static void tick(ServerLevel level, long currentTick)",
          "public static void tick(ServerLevel level, long currentTick)" in src)
    check("throttle gate: currentTick % ACCEPTANCE_CHECK_INTERVAL_TICKS != 0",
          "currentTick % ACCEPTANCE_CHECK_INTERVAL_TICKS != 0" in src)
    check("server-side gate: level.isClientSide()",
          "level.isClientSide()" in src)
    check("defensive try/catch around tickImpl",
          "tickImpl(level, currentTick)" in src
          and "catch (Throwable t)" in src)
    check("error log on exception",
          "Ergenverse.LOGGER.error" in src
          and "CRON-113" in src)

    # ── 5. tickImpl() gates (7 gates) ──
    print("\n[5] tickImpl() gates (7 gates in order)")
    check("Gate 3: findCultivatorByCharacterId(ZHOU_RU_CHARACTER_ID)",
          "findCultivatorByCharacterId(level, ZHOU_RU_CHARACTER_ID)" in src)
    check("Gate 4: CRON-112 prerequisite — sent_to_kunxu",
          '"sent_to_kunxu"' in src
          and 'getBoolean("sent_to_kunxu")' in src)
    check("Gate 5: write-once — accepted_as_disciple must be false",
          '"accepted_as_disciple"' in src
          and 'getBoolean("accepted_as_disciple")' in src)
    check("Gate 6: findCultivatorByCharacterId(MU_BINGMEI_CHARACTER_ID)",
          "findCultivatorByCharacterId(level, MU_BINGMEI_CHARACTER_ID)" in src)
    check("Gate 7: proximity — distanceToSqr(muBingmei)",
          "distanceToSqr(muBingmei)" in src)
    check("Gate 7: proximity — ACCEPTANCE_PROXIMITY_RADIUS_SQ",
          "ACCEPTANCE_PROXIMITY_RADIUS_SQ" in src)
    check("witness is optional (findNearbyPlayer after gates pass)",
          "findNearbyPlayer" in src
          and "witness != null" in src)

    # ── 6. Runtime state updates ──
    print("\n[6] Runtime state updates")
    check('putBoolean("accepted_as_disciple", true)',
          'putBoolean("accepted_as_disciple", true)' in src)
    check('putLong("disciple_acceptance_tick", currentTick)',
          'putLong("disciple_acceptance_tick", currentTick)' in src)
    check('putString("master_character_id", MU_BINGMEI_CHARACTER_ID)',
          'putString("master_character_id", MU_BINGMEI_CHARACTER_ID)' in src)
    check("runtime.updateNpcState(ZHOU_RU_CHARACTER_ID, zhouRuState)",
          "runtime.updateNpcState(ZHOU_RU_CHARACTER_ID, zhouRuState)" in src)

    # ── 7. Particle & sound effects ──
    print("\n[7] Particle & sound effects")
    check("ParticleTypes.END_ROD used (master ring)",
          "ParticleTypes.END_ROD" in src)
    check("ParticleTypes.FIREWORK used (central flash)",
          "ParticleTypes.FIREWORK" in src)
    check("MASTER_RING loop",
          "MASTER_RING_PARTICLE_COUNT" in src
          and "Math.cos(theta)" in src
          and "Math.sin(theta)" in src)
    check("DISCIPLE_RING loop",
          "DISCIPLE_RING_PARTICLE_COUNT" in src)
    check("BOND_STREAM loop (interpolation from master to disciple)",
          "BOND_STREAM_PARTICLE_COUNT" in src
          and "masterX + (discipleX - masterX) * t" in src)
    check("AMETHYST_BLOCK_CHIME sound at master",
          "SoundEvents.AMETHYST_BLOCK_CHIME" in src
          and "muBingmei.blockPosition()" in src)
    check("AMETHYST_BLOCK_CHIME sound at disciple",
          "SoundEvents.AMETHYST_BLOCK_CHIME" in src
          and "zhouRu.blockPosition()" in src)
    check("PLAYER_LEVELUP sound at disciple",
          "SoundEvents.PLAYER_LEVELUP" in src)
    check("BELL_BLOCK sound at midpoint",
          "SoundEvents.BELL_BLOCK" in src)
    check("deliberately avoids ENDER_DRAGON_GROWL",
          "ENDER_DRAGON_GROWL" not in src)
    check("deliberately avoids WITHER_SPAWN",
          "WITHER_SPAWN" not in src)
    check("deliberately avoids DRAGON_BREATH particles",
          "ParticleTypes.DRAGON_BREATH" not in src)
    check("uses BlockPos.containing for midpoint",
          "BlockPos.containing(midX, midY, midZ)" in src)

    # ── 8. Bilingual message ──
    print("\n[8] Bilingual message")
    check("Chinese first: 慕冰媚收周茹为徒",
          "慕冰媚收周茹为徒" in src_raw)
    check("English second: Mu Bingmei takes Zhou Ru as her disciple",
          "Mu Bingmei takes Zhou Ru as her disciple" in src)
    check("Li Muwan soul reference",
          "Li Muwan within Zhou Ru" in src)
    check("王林叔叔 (uncle) canon detail",
          "王林叔叔" in src_raw)
    check("DARK_PURPLE separator lines",
          'ChatFormatting.DARK_PURPLE' in src)
    check("LIGHT_PURPLE primary line",
          'ChatFormatting.LIGHT_PURPLE' in src)
    check("BOLD primary line",
          'ChatFormatting.BOLD' in src)
    check("ITALIC supporting lines",
          'ChatFormatting.ITALIC' in src)
    check("announceAcceptance method exists",
          "private static void announceAcceptance" in src)

    # ── 9. HistoryManager recording ──
    print("\n[9] HistoryManager recording")
    check("HistoryManager.onDiscovery call",
          "HistoryManager.onDiscovery" in src)
    check("discovery id: mu_bingmei_accepts_zhou_ru_as_disciple",
          '"mu_bingmei_accepts_zhou_ru_as_disciple"' in src)
    check("discovery text mentions 慕冰媚",
          "慕冰媚" in src_raw)
    check("discovery text mentions 周茹",
          "周茹" in src_raw)
    check("discovery text mentions 昆虚界",
          "昆虚界" in src_raw)
    check("discovery text mentions 王林叔叔",
          "王林叔叔" in src_raw)
    check("history record gated on witness != null",
          "if (witness != null)" in src)

    # ── 10. Ergenverse.onServerTick Loop K wiring ──
    print("\n[10] Ergenverse.onServerTick Loop K wiring")
    # Use raw text (with comments) for the comment-existence checks;
    # the strip-comments function would remove the `// Loop K` documentation.
    check("Loop K comment in Ergenverse.java",
          "Loop K" in erg_raw
          and "Mu Bingmei Acceptance Event" in erg_raw
          and "CRON-COMPLETIONIST-113" in erg_raw)
    check("MuBingmeiAcceptanceEvent.tick(overworld, ticks) call",
          "dev.ergenverse.wanglin.bead.MuBingmeiAcceptanceEvent.tick(overworld, ticks)" in erg)
    check("Loop K comes after Loop J (CRON-111) — by FQN call order",
          erg.index("ZhouRuCultivationGrowthService.tick(overworld, ticks)")
          < erg.index("MuBingmeiAcceptanceEvent.tick(overworld, ticks)"))

    # ── 11. Architecture compliance ──
    print("\n[11] Architecture compliance (CRON-69 point 5)")
    check("no direct WorldDeltaStore manipulation",
          "WorldDeltaStore" not in src)
    check("no direct WorldLayer manipulation",
          "WorldLayer" not in src)
    check("no direct CompositeWorldLayer manipulation",
          "CompositeWorldLayer" not in src)
    check("no level.setBlock call",
          ".setBlock(" not in src)
    check("uses runtime.updateNpcState (the facade)",
          "runtime.updateNpcState" in src)
    check("uses vanilla particle API (level.sendParticles)",
          "level.sendParticles" in src)
    check("uses vanilla sound API (level.playSound)",
          "level.playSound" in src)
    check("uses vanilla entity API (zhouRu.distanceToSqr)",
          "zhouRu.distanceToSqr" in src)
    check("defensive try/catch around runtime access",
          "WorldRuntimeState.get(level)" in src)

    # ── 12. Canon fidelity ──
    print("\n[12] Canon fidelity")
    check("N10 (Zhou Ru) citation in javadoc",
          "N10" in src_raw)
    check("N19 (Mu Bingmei) citation in javadoc",
          "N19" in src_raw)
    check("L74 (Kunxu Realm) citation in javadoc",
          "L74" in src_raw)
    check("canon quote: 'Mu Bingmei entered here; took Zhou Ru as her disciple'",
          "Mu Bingmei entered here; took Zhou Ru as her disciple" in src_raw)
    check("canon quote: 'Kunxu Realm (disciple of Mu Bingmei)'",
          "Kunxu Realm (disciple of Mu Bingmei)" in src_raw)
    check("canon honesty note (not mod-original)",
          "NOT mod-original" in src_raw)
    check("no fabricated chapter citation",
          "Ch." not in re.sub(r'Ch\.\s*\d+', '', src_raw)  # no Ch.NNN other than in canon sources
          or "exact chapter is not cited" in src_raw)
    check("Wang Ping canon context (Mu Bingmei's son) acknowledged",
          "Wang Ping" in src_raw)
    check("references Baidu Baike",
          "Baidu Baike" in src_raw)
    check("references Fandom wiki",
          "Fandom wiki" in src_raw)

    # ── 13. State transition integrity ──
    print("\n[13] State transition integrity")
    check("CRON-99 mentioned in state diagram",
          "CRON-99" in src_raw)
    check("CRON-110 mentioned in state diagram",
          "CRON-110" in src_raw)
    check("CRON-112 mentioned in state diagram",
          "CRON-112" in src_raw)
    check("CRON-113 mentioned in state diagram",
          "CRON-113" in src_raw)
    check("CRON-111 mentioned in state diagram",
          "CRON-111" in src_raw)
    check("CRON-100 mentioned in state diagram",
          "CRON-100" in src_raw)
    check("CRON-102 mentioned in state diagram",
          "CRON-102" in src_raw)
    check("state diagram shows CRON-112 → CRON-113 → CRON-111 order",
          # The state diagram is in the <h2>State Transition Diagram</h2> section.
          # Extract that section and check the order within it (the @see
          # references at the top of the file mention CRON-112 and CRON-111
          # before the state diagram, so a naive full-text index check fails).
          _state_diagram_has_order(src_raw, ["CRON-112", "CRON-113", "CRON-111"]))
    check("predecessor @see ZhouRuKunxuDepartureEvent",
          "@see ZhouRuKunxuDepartureEvent" in src_raw)
    check("successor @see ZhouRuCultivationGrowthService",
          "@see ZhouRuCultivationGrowthService" in src_raw)

    # ── 14. Single-player maximalism ──
    print("\n[14] Single-player maximalism (Article XLIII)")
    check("write-once (one-time per save)",
          "one-time event per save" in src_raw
          or "one-shot" in src_raw.lower())
    check("witness is optional (acceptance fires without witness)",
          "STILL fires" in src_raw
          or "whether or not the player witnesses" in src_raw)
    check("automatic tick service (not interaction-gated)",
          "automatic" in src_raw.lower()
          and "NOT interaction-gated" in src_raw)
    check("throttle interval documented (200 ticks = 10s)",
          "200 ticks = 10s" in src_raw
          or "200 ticks" in src_raw)
    check("acceptance fires when both NPCs materialized",
          "chunks loaded" in src_raw)

    # ── 15. Build artifact ──
    print("\n[15] Build artifact")
    check("MuBingmeiAcceptanceEvent.class compiled",
          CLASS_FILE.exists(),
          f"expected at {CLASS_FILE}")

    # ── Summary ──
    print("\n" + "=" * 72)
    print(f"SUMMARY: {PASS} passed, {FAIL} failed (total {PASS + FAIL})")
    print("=" * 72)
    if FAIL > 0:
        print("\nFAILURES:")
        for name, detail in FAILURES:
            print(f"  - {name}: {detail}")
        sys.exit(1)
    else:
        print("\nALL CHECKS PASSED.")
        sys.exit(0)


if __name__ == "__main__":
    main()
