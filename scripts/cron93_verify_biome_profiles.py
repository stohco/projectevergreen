#!/usr/bin/env python3
"""
CRON-93 verification script — checks that biome-aware terrain profiles are
correctly integrated into the BlueprintChunkGenerator and all 14 callers
migrated to surfaceHeightFor(level, x, z).

Runs 50+ checks across 6 categories:
1. BiomeTerrainProfile.java — record structure, forBiome() factory, all 15 biomes mapped
2. BlueprintChunkGenerator.java — biomeAwareSurfaceHeight, surfaceHeightFor,
   biomeAmplitudeNoise, amplitudeHash, BIOME_NOISE_PERIOD, BIOME_SAMPLE_QUART_Y,
   fillFromNoise/getBaseHeight/getBaseColumn use biomeAwareSurfaceHeight,
   legacy canonSurfaceHeight retained as fallback, Climate import, no climateSampler() refs
3. All 12 callers (11 builders + SpawnEventHandler) migrated to surfaceHeightFor
4. No remaining canonSurfaceHeight CALLS outside BlueprintChunkGenerator.java (javadoc refs OK)
5. planet_suzaku.json biome source integrity — all 15 biomes still referenced
6. Canon fidelity — biome profile base heights match canon (mountains=110, plains=64, ocean=35)
"""

import re
import sys
from pathlib import Path

FORGE_ROOT = Path("/home/z/my-project/forge-mod")
SRC_ROOT = FORGE_ROOT / "src/main/java/dev/ergenverse"
RES_ROOT = FORGE_ROOT / "src/main/resources/data/ergenverse"

PASS = 0
FAIL = 0
FAILS = []

def check(name: str, condition: bool, detail: str = ""):
    global PASS, FAIL
    if condition:
        PASS += 1
        # print(f"  PASS  {name}")
    else:
        FAIL += 1
        FAILS.append(f"  FAIL  {name} — {detail}")
        print(f"  FAIL  {name} — {detail}")

def read(path: Path) -> str:
    return path.read_text()

def strip_comments(text: str) -> str:
    """Strip // line comments and /* */ block comments (rough — for grep purposes)."""
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.DOTALL)
    text = re.sub(r"//[^\n]*", "", text)
    return text

# ════════════════════════════════════════════════════════════════════
# CATEGORY 1: BiomeTerrainProfile.java
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 1] BiomeTerrainProfile.java")

profile_path = SRC_ROOT / "runtime/worldgen/BiomeTerrainProfile.java"
check("BiomeTerrainProfile.java exists", profile_path.exists())

if profile_path.exists():
    text = read(profile_path)
    check("Is a record", "public record BiomeTerrainProfile" in text)
    check("Has baseHeight field", "int baseHeight" in text)
    check("Has amplitude field", "int amplitude" in text)
    check("Has forBiome(ResourceLocation)", "public static BiomeTerrainProfile forBiome(ResourceLocation" in text)
    check("Has DEFAULT constant", "BiomeTerrainProfile DEFAULT" in text)
    check("Mentions CRON-93", "CRON-COMPLETIONIST-93" in text)
    check("References BlueprintChunkGenerator", "BlueprintChunkGenerator" in text)

    # All 15 biomes from planet_suzaku.json must be mapped
    biomes = [
        ("snow_domain_country", 95, 20),
        ("xuan_wu_country", 80, 12),
        ("qing_shui_ruin", 70, 8),
        ("pilu_kingdom", 70, 8),
        ("fire_burn_country", 75, 10),
        ("vermilion_bird_country", 78, 10),
        ("zhao_mountains", 110, 25),
        ("zhao_plains", 64, 4),
        ("chu_country", 72, 10),
        ("sky_demon_country", 95, 18),
        ("fire_demon_country", 95, 18),
        ("sea_of_devils", 35, 3),
        ("jue_ming_valley", 55, 5),
        ("jue_ming_valley_depths", 40, 4),
        ("jue_ming_valley_abyss", 25, 3),
    ]
    for biome_id, expected_base, expected_amp in biomes:
        # Each biome appears in a case statement with the right base/amplitude
        pattern = rf'case "{biome_id}" -> new BiomeTerrainProfile\({expected_base}, {expected_amp}\)'
        check(f"Biome {biome_id} → base={expected_base}, amp={expected_amp}",
              re.search(pattern, text) is not None,
              f"expected pattern: {pattern}")

# ════════════════════════════════════════════════════════════════════
# CATEGORY 2: BlueprintChunkGenerator.java
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 2] BlueprintChunkGenerator.java")

bcg_path = SRC_ROOT / "runtime/worldgen/BlueprintChunkGenerator.java"
check("BlueprintChunkGenerator.java exists", bcg_path.exists())

if bcg_path.exists():
    text = read(bcg_path)
    code = strip_comments(text)

    # CRON-93 javadoc
    check("Has CRON-93 javadoc header", "CRON-COMPLETIONIST-93" in text)
    check("Mentions biome-aware terrain profiles", "BIOME-AWARE TERRAIN PROFILES" in text)

    # Imports
    check("Imports Climate", "import net.minecraft.world.level.biome.Climate;" in text)
    check("Imports Biome", "import net.minecraft.world.level.biome.Biome;" in text)
    check("Imports ServerLevel", "import net.minecraft.server.level.ServerLevel;" in text)

    # Constants
    check("Has BIOME_NOISE_PERIOD = 24", re.search(r"static final int BIOME_NOISE_PERIOD = 24", code) is not None)
    check("Has BIOME_SAMPLE_QUART_Y = 16", re.search(r"static final int BIOME_SAMPLE_QUART_Y = 16", code) is not None)
    check("Has AMPLITUDE_HASH_SALT", "AMPLITUDE_HASH_SALT" in code)

    # New methods
    check("Has biomeAwareSurfaceHeight method", "public int biomeAwareSurfaceHeight(" in code)
    check("Has surfaceHeightFor(ServerLevel,...) method", "public static int surfaceHeightFor(ServerLevel level" in code)
    check("Has sampleBiomeProfile method", "private BiomeTerrainProfile sampleBiomeProfile(" in code)
    check("Has biomeAmplitudeNoise method", "static int biomeAmplitudeNoise(" in code)
    check("Has amplitudeHash method", "private static long amplitudeHash(" in code)

    # Biome lookup uses correct API
    check("Uses randomState.sampler() (not climateSampler)", "randomState.sampler()" in code)
    check("Uses biomeSource.getNoiseBiome", "biomeSource.getNoiseBiome(" in code)
    check("Does NOT use climateSampler()", "climateSampler()" not in code,
          "found climateSampler() — should be sampler()")

    # Legacy canonSurfaceHeight retained as fallback
    check("Legacy canonSurfaceHeight retained", "public static int canonSurfaceHeight(int worldX, int worldZ)" in code)
    check("canonSurfaceHeight javadoc says RETAINED", "RETAINED" in text)

    # fillFromNoise uses biomeAwareSurfaceHeight
    check("fillFromNoise calls biomeAwareSurfaceHeight",
          "int surfaceHeight = biomeAwareSurfaceHeight(worldX, worldZ, randomState);" in code)

    # getBaseHeight uses biomeAwareSurfaceHeight
    check("getBaseHeight calls biomeAwareSurfaceHeight",
          "return biomeAwareSurfaceHeight(x, z, randomState);" in code)

    # getBaseColumn uses biomeAwareSurfaceHeight
    check("getBaseColumn calls biomeAwareSurfaceHeight",
          "int surfaceHeight = biomeAwareSurfaceHeight(x, z, randomState);" in code)

    # addDebugScreenInfo reports biome-aware height
    check("addDebugScreenInfo mentions BIOME-AWARE",
          "BIOME-AWARE" in code and "Biome-Aware Height" in code)

    # surfaceHeightFor falls back to canonSurfaceHeight for non-BlueprintChunkGenerator
    check("surfaceHeightFor falls back to canonSurfaceHeight",
          "return canonSurfaceHeight(worldX, worldZ);" in code)

    # biomeAmplitudeNoise early-return for amplitude <= 0
    check("biomeAmplitudeNoise handles amplitude<=0", "if (amplitude <= 0) return 0;" in code)

# ════════════════════════════════════════════════════════════════════
# CATEGORY 3: All 12 callers migrated
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 3] Caller migration")

callers = [
    ("spawn/LuoHeSectBuilder.java",               "level"),
    ("spawn/SoulRefiningSectBuilder.java",        "level"),
    ("spawn/VermilionBirdImperialCityBuilder.java","level"),
    ("spawn/TianShuiCityBuilder.java",            "level"),
    ("spawn/HengYueSectBuilder.java",             "level"),
    ("spawn/WangFamilyVillageBuilder.java",       "level"),
    ("spawn/XuanDaoSectBuilder.java",             "level"),
    ("spawn/NanDouCityBuilder.java",              "level"),
    ("spawn/SnowDomainCapitalBuilder.java",       "level"),
    ("spawn/QilinCityBuilder.java",               "level"),
    ("spawn/TengFamilyCityBuilder.java",          "level"),
    ("spawn/SpawnEventHandler.java",              "suzakuLevel"),
]

for rel_path, level_var in callers:
    path = SRC_ROOT / rel_path
    check(f"{rel_path} exists", path.exists())
    if not path.exists():
        continue
    text = read(path)
    code = strip_comments(text)
    # Must contain a surfaceHeightFor call with the right level var
    pattern = rf"surfaceHeightFor\(\s*{level_var}\s*,"
    check(f"{rel_path} calls surfaceHeightFor({level_var}, ...)",
          re.search(pattern, code) is not None,
          f"expected pattern: {pattern}")
    # Must NOT contain any canonSurfaceHeight CALL (javadoc refs OK)
    call_pattern = r"\.canonSurfaceHeight\("
    matches = re.findall(call_pattern, code)
    check(f"{rel_path} has NO canonSurfaceHeight calls in code",
          len(matches) == 0,
          f"found {len(matches)} canonSurfaceHeight calls in code (excluding comments)")

# HengYueSectBuilder has TWO call sites (getSectCenter + buildMysteriousStoneDiscovery)
heng_path = SRC_ROOT / "spawn/HengYueSectBuilder.java"
if heng_path.exists():
    code = strip_comments(read(heng_path))
    count = len(re.findall(r"surfaceHeightFor\(\s*level\s*,", code))
    check("HengYueSectBuilder has 2 surfaceHeightFor calls (getSectCenter + buildMysteriousStoneDiscovery)",
          count == 2, f"found {count} surfaceHeightFor calls")

# ════════════════════════════════════════════════════════════════════
# CATEGORY 4: No remaining canonSurfaceHeight calls outside BlueprintChunkGenerator
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 4] Global audit — no canonSurfaceHeight calls in code")

remaining = []
for java_file in SRC_ROOT.rglob("*.java"):
    if java_file.name == "BlueprintChunkGenerator.java":
        continue
    text = read(java_file)
    code = strip_comments(text)
    if re.search(r"\.canonSurfaceHeight\(", code):
        remaining.append(str(java_file.relative_to(SRC_ROOT)))

check("No canonSurfaceHeight CALLS outside BlueprintChunkGenerator.java",
      len(remaining) == 0,
      f"files with remaining calls: {remaining}")

# ════════════════════════════════════════════════════════════════════
# CATEGORY 5: planet_suzaku.json biome source integrity
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 5] planet_suzaku.json biome source integrity")

ps_path = RES_ROOT / "dimension/planet_suzaku.json"
check("planet_suzaku.json exists", ps_path.exists())
if ps_path.exists():
    text = read(ps_path)
    # Generator type is still ergenverse:blueprint
    check("Generator type is ergenverse:blueprint", '"type": "ergenverse:blueprint"' in text)
    # All 15 biomes referenced
    for biome_id, _, _ in biomes:
        check(f"planet_suzaku.json references {biome_id}",
              f'"biome": "ergenverse:{biome_id}"' in text,
              f"missing biome reference: {biome_id}")

# ════════════════════════════════════════════════════════════════════
# CATEGORY 6: Canon fidelity sanity checks
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 6] Canon fidelity sanity checks")

if profile_path.exists():
    text = read(profile_path)

    # Mountains > Plains > Ocean
    check("zhao_mountains base (110) > zhao_plains base (64)",
          "BiomeTerrainProfile(110, 25)" in text and "BiomeTerrainProfile(64, 4)" in text)
    check("zhao_plains base (64) > sea_of_devils base (35)",
          "BiomeTerrainProfile(64, 4)" in text and "BiomeTerrainProfile(35, 3)" in text)
    check("sea_of_devils base (35) > jue_ming_valley_abyss base (25)",
          "BiomeTerrainProfile(35, 3)" in text and "BiomeTerrainProfile(25, 3)" in text)

    # Jue Ming Valley tiered descent
    check("jue_ming_valley (55) > depths (40) > abyss (25)",
          "BiomeTerrainProfile(55, 5)" in text
          and "BiomeTerrainProfile(40, 4)" in text
          and "BiomeTerrainProfile(25, 3)" in text)

    # Snow domain is elevated (cold countries are high-altitude in vanilla MC)
    check("snow_domain_country base (95) > zhao_plains (64)",
          "BiomeTerrainProfile(95, 20)" in text)

    # Canon mentions
    check("Mentions 恒岳山", "恒岳山" in text)
    check("Mentions 修魔海", "修魔海" in text)
    check("Mentions 雪域国", "雪域国" in text)
    check("Mentions 决明谷", "决明谷" in text)
    check("Mentions 朱雀国", "朱雀国" in text)
    check("Mentions 楚国", "楚国" in text)

# ════════════════════════════════════════════════════════════════════
# SUMMARY
# ════════════════════════════════════════════════════════════════════
print(f"\n{'='*70}")
print(f"RESULTS: {PASS} passed, {FAIL} failed, {PASS + FAIL} total")
if FAILS:
    print(f"\nFAILURES:")
    for f in FAILS:
        print(f)
    sys.exit(1)
else:
    print("\nALL CHECKS PASSED.")
    sys.exit(0)
