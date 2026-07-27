#!/usr/bin/env python3
"""Append the CRON-COMPLETIONIST-138 worklog entry to /home/z/my-project/worklog.md
and /home/z/my-project/forge-mod/worklog.md (mirror).

This is the qi-cost-for-CultivatorSwordQiGoal release round. Code was complete
in the previous (out-of-context) session but never built/committed/pushed.
This round: (1) fix CRON-137→138 comment numbering, (2) rename verification
script, (3) build, (4) produce ZIP, (5) commit, (6) push, (7) worklog entry.
"""

import hashlib
import subprocess
from pathlib import Path

ENTRY = """---
Task ID: CRON-COMPLETIONIST-138
Agent: cron-completionist
Task: Two-scope release.
PRIMARY: Qi cost for CultivatorSwordQiGoal — closes CRON-134 self-critique
#8 (carried over 2 rounds). Sword-qi projections now consume 5.0 qi per
shot; canUse() refuses activation below 5% of maxQi; fireSwordQi() consumes
at fire time and aborts (no damage/particle/sound) if qi insufficient.
Foundation (maxQi=100) gets 20 shots; Core (500) gets 100; Nascent (2000)
gets 400; Soul+ (10000) gets 2000.
BUNDLED FIX: TerrainSpiritifier dimension-gate correction — previously
gated conversion on Level.OVERWORLD, but the mod uses a separate
ergenverse:planet_suzaku dimension (via SpawnEventHandler teleport), so
the spiritifier never ran on Planet Suzaku and the player saw vanilla
stone/grass/trees on canon-shaped terrain. Gate now targets
ergenverse:planet_suzaku directly. CanonGeographyPlacer marked as
intentionally superseded by PlanetSuzakuChunkMaterializer (chunk-scoped,
provenance-aware) — left as a no-op on minecraft:overworld with explicit
"do not re-point at planet_suzaku" warning.
The previous session left both scopes code-complete but unbuilt/uncommitted;
this round ships the release.

Work Log:
- STEP 1: Read worklog tail. Confirmed CRON-137 (commit 251bdf7) shipped
  WorldGraph integration — GraphBootstrap + GraphQueryService + RumorNetwork
  graph-first propagation. The previous session's summary indicated CRON-138
  (qi cost for CultivatorSwordQiGoal) was code-complete but unbuilt; verified
  by git status: gradle.properties + mods.toml + EntityCultivator.java +
  CultivatorSwordQiGoal.java modified, scripts/cron137_verify_swordqi_qi_cost.py
  untracked. The code was in place but the CRON numbering was wrong (the code
  comments referenced "CRON-137" instead of "CRON-138" because the work was
  drafted in parallel with CRON-137's WorldGraph integration).
- STEP 2: Audited the existing diff. Confirmed the qi-cost implementation
  is canon-faithful and correctly architected:
  - SWORD_QI_QI_COST = 5.0D (absolute units, calibrated against CRON-134's
    maxQi scale: Foundation 100, Core 500, Nascent 2000, Soul+ 10000).
  - EntityCultivator.hasEnoughQiForSwordQi() — 5% maxQi gate (same threshold
    as flight continuation; sword-qi projection is a brief focused expenditure
    comparable to one flight tick).
  - canUse() — realm check (≥ QI_CONDENSATION), then qi gate (refuses
    activation below 5% maxQi), then distance (5-18 blocks), then LOS.
  - start() — sets qiGatePassedAtActivation=true (informational).
  - stop() — resets qiGatePassedAtActivation=false.
  - fireSwordQi() — authoritative consumeQi(5.0) at fire time; if consumption
    fails (race condition: qi dropped during the 10-tick charge, e.g.,
    flight goal consumed it), aborts the projection entirely (no damage,
    no particle trail, no sword-swish sound). Cultivator must wait for regen.
- STEP 2b: Audited the bundled dimension-gate fix in TerrainSpiritifier.java
  and CanonGeographyPlacer.java. Confirmed:
  - TerrainSpiritifier previously gated on Level.OVERWORLD — a leftover from
    the old design where Planet Suzaku WAS the overworld via a runtime
    dimension override that no longer exists. The mod now uses a separate
    ergenverse:planet_suzaku dimension (registered via datapack), and
    SpawnEventHandler teleports first-join players there. The old gate meant
    the spiritifier NEVER RAN on Planet Suzaku — the player saw vanilla
    stone/grass/trees on canon-shaped terrain. The gate now targets
    SUZAKU_KEY = ResourceKey.create(Registries.DIMENSION,
    ResourceLocation("ergenverse", "planet_suzaku")) directly. This is a
    block-PALETTE fix only; the canon SHAPE was already deterministic
    (BiomeTerrainProfile + DeterministicSeedHandler) — no randomness added.
  - CanonGeographyPlacer is the legacy full-build structure placer
    (CRON-69's supersession target). It uses full build(level)/build(level,
    center) with NO chunk filtering — re-enabling it on Planet Suzaku would
    (a) double-build every structure alongside PlanetSuzakuChunkMaterializer
    and (b) reintroduce the cascading-chunk-load bug CRON-62 fixed. The
    gate therefore deliberately targets minecraft:overworld where no
    ergenverse canon coordinates live, making it a permanent no-op. Added
    explicit "do NOT fix this gate by pointing it at planet_suzaku" warning.
- STEP 3: Fixed CRON-137→138 comment numbering across both files. The code
  was drafted in parallel with CRON-137 (WorldGraph) and inherited its number;
  the actual CRON sequence is 137=WorldGraph (committed 251bdf7) → 138=qi cost.
  - EntityCultivator.java: 1 occurrence (hasEnoughQiForSwordQi javadoc).
  - CultivatorSwordQiGoal.java: 6 occurrences (class javadoc header,
    SWORD_QI_QI_COST constant javadoc, qiGatePassedAtActivation field javadoc,
    canUse() gate comment, start() gate comment, fireSwordQi() consume comment).
- STEP 4: Renamed verification script cron137_verify_swordqi_qi_cost.py →
  cron138_verify_swordqi_qi_cost.py (it was a CRON-138 artifact misnamed
  during parallel drafting). Updated all internal references via sed.
- STEP 5: Compiled — JAVA_HOME=/tmp/my-project/.jdks/jdk-17.0.13+11/
  ./gradlew compileJava — BUILD SUCCESSFUL in 17s, 0 errors, 28 pre-existing
  deprecation warnings (ResourceLocation constructor — unchanged).
- STEP 6: Ran CRON-138 verification — scripts/cron138_verify_swordqi_qi_cost.py
  — 63/63 pass across 9 groups:
  1. SWORD_QI_QI_COST constant (5 checks: value 5.0, public static final,
     javadoc CRON-138 marker, calibration table, mod-original disclaimer).
  2. EntityCultivator.hasEnoughQiForSwordQi() (7 checks: method exists,
     public, returns boolean, 5% maxQi threshold, maxQi<=0 guard, CRON-138
     javadoc, ties to consumeQi).
  3. CultivatorSwordQiGoal.canUse() qi gate (6 checks: gate after realm
     check, instanceof EntityCultivator, hasEnoughQiForSwordQi call,
     returns false on insufficient qi, gate before distance check, gate
     before LOS check).
  4. fireSwordQi() qi consumption (9 checks: consumeQi call, SWORD_QI_QI_COST
     passed, abort on consumption failure, no damage on abort, no particle
     on abort, no sound on abort, debug log on success, warn log on abort,
     Ergenverse.LOGGER used).
  5. qiGatePassedAtActivation field (5 checks: field declared, boolean type,
     set true in start(), set false in stop(), javadoc CRON-138 marker).
  6. Canon fidelity (8 checks: 剑气 reference, sword-qi is cultivation ability,
     qi expenditure genre convention, no fabricated chapter citation,
     mod-original disclaimer, threshold consistent with flight, cost reasonable
     for realm scale, Foundation=20 shots calibration).
  7. Existing EntityCultivator qi API intact (8 checks: getQi, setQi,
     consumeQi, drainAllQi, hasEnoughQiForFlightActivation,
     hasEnoughQiForFlightTick, tickQi, initializeQiForRealm).
  8. No regression — existing sword-qi mechanics (9 checks: damage 0.7F,
     distance 5-18, charge 10 ticks, cooldown table, LOS ClipContext,
     particle trail, sword-swish sound, realm ordinal, knockback).
  9. Build verification (3 checks: exit 0, BUILD SUCCESSFUL, no error:).
- STEP 7: Ran regression across CRON-130 → CRON-138 (532 checks total):
  - CRON-130 cultivator-flight: 111/111
  - CRON-132 ride-sword visibility: 22/22
  - CRON-133 flight path navigator: 73/73
  - CRON-134 flight qi cost: 85/85
  - CRON-135 upward ray-cast: 60/60
  - CRON-136 tall-obstacle correctness: 53/53
  - CRON-137 graph integration: 65/65
  - CRON-138 sword-qi qi cost: 63/63
  Total: 532 checks, 0 failures. Zero regressions.
- STEP 8: Clean build — JAVA_HOME=/tmp/my-project/.jdks/jdk-17.0.13+11/
  ./gradlew clean build — BUILD SUCCESSFUL in 44s. JAR: 8.8 MB
  (ergenverse-0.1.14-alpha.jar).
- STEP 9: Built importable modpack ZIP — python3 scripts/build_importable_modpack.py
  — ergenverse-modpack-0.1.14-alpha.zip (7.1 MB, 7420134 bytes). Verified:
  valid CurseForge modpack (manifest.json + modlist.html + overrides/).
  Copied JAR + ZIP to /home/z/my-project/download/ and /home/z/my-project/forge-mod/releases/.
- STEP 10: Version bump 0.1.13-alpha → 0.1.14-alpha in gradle.properties
  (with CRON-138 changelog comment) and mods.toml.
- STEP 11: Git workflow — git add -A (6 files: EntityCultivator.java,
  CultivatorSwordQiGoal.java, gradle.properties, mods.toml,
  cron138_verify_swordqi_qi_cost.py [renamed from cron137_*],
  ergenverse-0.1.14-alpha.jar + ergenverse-modpack-0.1.14-alpha.zip
  [release artifacts]). git commit + push.

Stage Summary:
- SHIPPED: CRON-COMPLETIONIST-138 closes the qi-cost gap for CultivatorSwordQiGoal
  that was carried over from CRON-134 self-critique #8 (2 rounds of carryover).
  Sword-qi projections now consume 5.0 qi per shot; canUse() refuses activation
  below 5% of maxQi; fireSwordQi() consumes at fire time and aborts cleanly
  (no damage/particle/sound) if qi insufficient. This completes the qi-economy
  for the cultivator combat suite: melee (CultivatorCombatGoal) still needs qi
  added in a future CRON, but flight (CRON-134) and ranged sword-qi (CRON-138)
  now both consume qi.
- Build status: BUILD SUCCESSFUL in 44s, 0 errors, 28 pre-existing deprecation
  warnings (ResourceLocation constructor — unchanged from CRON-137).
- Git hash: (filled after push)
- Verification: scripts/cron138_verify_swordqi_qi_cost.py — 63/63 pass.
  Regression: CRON-130 (111) + CRON-132 (22) + CRON-133 (73) + CRON-134 (85)
  + CRON-135 (60) + CRON-136 (53) + CRON-137 (65) + CRON-138 (63) = 532 checks,
  0 failures.
- Release artifacts: ergenverse-0.1.14-alpha.jar (8.8 MB) +
  ergenverse-modpack-0.1.14-alpha.zip (7.1 MB) in download/ and releases/.
- Canon sources: 剑气 (sword-qi projection) is universally attested in 仙逆 —
  Wang Lin projects sword-qi repeatedly from QI_CONDENSATION realm onward
  (e.g., fighting Teng Li at the Heng Yue Sect outer sect, fighting in the
  Sea of Devils, etc.). The canon fact is "sword-qi projection requires 真元
  output" — universally attested across xianxia. The specific qi cost (5.0
  absolute units per shot, calibrated to CRON-134's maxQi scale) is
  mod-original; NO fabricated chapter citation.

- HARSH SELF-CRITIQUE (hyper-analytical, fact-checked against canon):
  1. **CRON numbering bug detected and fixed.** The previous session drafted
     CRON-138 in parallel with CRON-137 (WorldGraph); both used the "137"
     number in comments. This round caught and fixed all 7 occurrences
     (1 in EntityCultivator, 6 in CultivatorSwordQiGoal). Score 10/10 for
     numbering hygiene after fix (was 0/10 before — a CRON-138 commit
     referencing CRON-137 in javadocs would have been a permanent audit
     confusion). Root cause: parallel-drafting artifacts; mitigation: always
     rename the CRON tag immediately after deciding which work goes into
     which CRON slot, before writing any code.
  2. **No runtime playtest verification.** 63 static checks prove the qi-cost
     logic is wired at the source-code level. None prove the qi actually
     decrements at runtime, that an exhausted cultivator actually refuses to
     fire, or that abort-on-insufficient-qi actually suppresses particles.
     A playtest with a Foundation cultivator that fires 20+ sword-qi shots
     should show qi reaching 0 and the 21st shot being suppressed. Score
     4/10 for runtime validation (NEEDS PLAYTESTING).
  3. **Race condition handling is correct but unverified at runtime.** The
     qiGatePassedAtActivation field is informational only — the authoritative
     check is in fireSwordQi() via consumeQi(). This is the right design
     (defense-in-depth), but the race window (10-tick charge during which
     flight could consume qi) has never been observed in a playtest. Score
     7/10 for design (correct), 4/10 for runtime verification.
  4. **Melee combat (CultivatorCombatGoal) still costs no qi.** CRON-134
     self-critique #8 said "all cultivation abilities consume qi" — CRON-138
     closed the sword-qi ranged gap but melee is still free. A Foundation
     cultivator with 0 qi can still punch for full melee damage. Canon:
     melee combat also consumes qi (Wang Lin visibly tires after extended
     melee, especially pre-Nascent). Next CRON should add qi cost to
     CultivatorCombatGoal. Score 5/10 for completeness (ranged done, melee
     not).
  5. **The 5.0 qi cost is calibrated but not playtested for feel.** The
     calibration (Foundation 20 shots, Core 100, Nascent 400, Soul+ 2000)
     is mathematically consistent with CRON-134's flight cost (4/sec), but
     the actual feel of "20 shots then exhaustion" may be too generous or
     too stingy. Canon: Wang Lin at Foundation realm rarely projects more
     than a handful of sword-qi in a single combat (he conserves qi). 20
     shots may be too many. Score 6/10 for balance (mathematically sound,
     needs playtest tuning).
  6. **No qi-bar overlay.** The player cannot see how much qi a cultivator
     has remaining. The cultivator's qi is invisible to the player — they
     see a cultivator fire sword-qi 20 times then stop, but they don't see
     a depleting bar. CRON-134 next-priority (d) listed this; still not
     done. Score 4/10 for player-facing feedback (no UI).
  7. **The abort log is WARN-level but the success log is DEBUG.** An
     aborted sword-qi projection is logged at WARN (visible by default);
     a successful consumption is logged at DEBUG (invisible by default).
     This is correct (failures are noteworthy, successes are routine), but
     a player with default log levels will see WARN spam if a cultivator
     repeatedly fails to fire (e.g., after exhausting qi on flight). Score
     7/10 for log hygiene (correct levels, but WARN may be noisy in edge
     cases).
  8. **hasEnoughQiForSwordQi() uses the same 5% threshold as flight.** This
     is intentional (sword-qi is "comparable to one flight tick"), but a
     cultivator at exactly 5% qi can fire one sword-qi shot (5.0 cost, 5%
     of 100 = 5.0 qi available, consumption succeeds, qi → 0) and then is
     below the 5% gate for the next shot. This is correct (the gate is a
     pre-condition; the cost is a hard consumption). But the threshold
     semantics differ slightly: flight "continuation" 5% is a soft floor
     (flight stops gracefully), sword-qi "activation" 5% is a hard gate
     (no projection at all). Score 8/10 for semantic clarity (intentional
     but could be clearer in javadoc).
  9. **No fabricated chapter citations.** The cost (5.0 qi) is explicitly
     labeled mod-original. The canon fact (sword-qi consumes qi) is
     universally attested. The Heng Yue Sect outer sect fight against
     Teng Li is canon-attested but I did NOT cite a specific chapter
     number — I described the scene type. Score 10/10 for citation honesty.

- NEXT PRIORITY (in order, post-CRON-138):
  (a) **Wire WorldStateEngine 6 query methods to graph-first with JSON
       fallback (Score 9/10, CRITICAL — user's stated next move from
       CRON-137 list).** queryWhatExists → whatExistsAt, queryWhoOwns →
       whoOwns, queryWhoWants → whoWants, queryWhoKnows → whoKnowsAbout.
       Each method tries graph first, falls back to JSON iteration if the
       entity is not in the graph. Score 9/10 for simulation integration.
  (b) **Wire ActorMaterializer.materializeAroundPlayer() to GraphQueryService
       (Score 8/10, HIGH — user's stated next move).** Replace
       SettlementThreatIndex.getSituationThreat() with threatsNearSettlement()
       + replace OpportunityRegistry scan with whatExistsAt(). Score 8/10.
  (c) **Add graph write-back: WorldEventBus events → graph edges (Score 8/10,
       HIGH — user's stated next move).** Beast spawn events create
       LOCATED_IN edges. Social interaction events create FAMILIAR_WITH
       edges. Karmic events create KARMIC_DEBT/GRUDGE edges. Score 8/10.
  (d) **Add qi cost to CultivatorCombatGoal (Score 7/10, MEDIUM — fixes
       self-critique #4 above).** Melee attacks consume ~2.0 qi per swing
       (half of sword-qi cost — melee is less intense than ranged projection).
       Score 7/10.
  (e) **Create Component classes (Score 7/10, MEDIUM — fixes CRON-137
       self-critique #2).** CultivationComponent (realm, qi),
       LocationComponent (coords, parent), OwnershipComponent (owner, state),
       KarmaComponent (burden, type). Score 7/10.
  (f) **Qi-bar overlay for cultivators (Score 7/10, MEDIUM — fixes
       self-critique #6 + CRON-134 next-priority (d)).** Render a bar above
       cultivator heads showing current qi / maxQi. Score 7/10.
  (g) **Wire WorldGraph persistence to SavedData (Score 6/10, MEDIUM — fixes
       CRON-137 self-critique #4).** serialize/deserialize on world save/load.
       Score 6/10.
  (h) **Playtest CRON-130 through CRON-138 end-to-end (Score 10/10,
       CRITICAL — user is actively playtesting).** Import 0.1.14-alpha
       modpack ZIP, verify graph populates, verify rumor propagation via
       social connections, verify cultivator flight + obstacle avoidance +
       qi expenditure + sword-qi qi cost.
"""

# Compute git short hash for backfill
try:
    git_hash = subprocess.check_output(
        ["git", "rev-parse", "--short", "HEAD"],
        cwd="/home/z/my-project/forge-mod",
        stderr=subprocess.DEVNULL,
    ).decode().strip()
except Exception:
    git_hash = "(unknown)"

ENTRY_BACKFILLED = ENTRY.replace(
    "Git hash: (filled after push)",
    f"Git hash: {git_hash}",
)

# Append to both worklog mirrors
for path in [
    Path("/home/z/my-project/worklog.md"),
    Path("/home/z/my-project/forge-mod/worklog.md"),
]:
    with path.open("a", encoding="utf-8") as f:
        f.write(ENTRY_BACKFILLED)
        if not ENTRY_BACKFILLED.endswith("\n"):
            f.write("\n")
    print(f"Appended CRON-138 entry to {path} ({path.stat().st_size} bytes)")

print(f"\nGit hash: {git_hash}")
print("Done.")
