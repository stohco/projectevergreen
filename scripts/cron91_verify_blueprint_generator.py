#!/usr/bin/env python3
"""
CRON-91 verification script for the BlueprintChunkGenerator upgrade + biome source fix.

Verifies:
  1. BlueprintChunkGenerator.java contains the CRON-91 layer-override integration
  2. The two missing biome JSONs (snow_domain_country, xuan_wu_country) now exist
  3. planet_suzaku.json references ONLY biomes that have JSON files (no latent bugs)
  4. The existing canon surface height + codec + registration are unchanged (no regression)
  5. The layer-override code uses the correct API (CompositeWorldLayer.layersInMaterializationOrder,
     WorldLayer.getChunkContribution, ChunkContribution.blockChanges)
  6. CANON structures are NOT built in fillFromNoise (correctly deferred to materializer)
  7. The new biome JSONs are valid JSON and follow the country-biome schema

Run: python3 /home/z/my-project/forge-mod/scripts/cron91_verify_blueprint_generator.py
"""

import json
import re
import sys
from pathlib import Path

ROOT = Path("/home/z/my-project/forge-mod")
JAVA = ROOT / "src/main/java/dev/ergenverse/runtime/worldgen/BlueprintChunkGenerator.java"
DIM_JSON = ROOT / "src/main/resources/data/ergenverse/dimension/planet_suzaku.json"
BIOME_DIR = ROOT / "src/main/resources/data/ergenverse/worldgen/biome"

passed = 0
failed = 0
checks = []


def check(name, ok, detail=""):
    global passed, failed
    checks.append((name, ok, detail))
    if ok:
        passed += 1
        print(f"  PASS  {name}")
    else:
        failed += 1
        print(f"  FAIL  {name}  {detail}")


# ──────────────────────────────────────────────────────────────────────────
# 1. BlueprintChunkGenerator.java contains CRON-91 layer-override integration
# ──────────────────────────────────────────────────────────────────────────
print("\n[1] BlueprintChunkGenerator.java — CRON-91 layer-override integration")

java_src = JAVA.read_text(encoding="utf-8")

check(
    "CRON-91 javadoc header present",
    "CRON-COMPLETIONIST-91" in java_src and "BLUEPRINT+LAYERS INTEGRATION" in java_src,
)

check(
    "Two-phase fill comment present",
    "Phase 1: canon base terrain" in java_src and "Phase 2: apply PLAYER + SIMULATION layer overrides" in java_src,
)

check(
    "applyLayerOverrides method defined",
    "private void applyLayerOverrides(ChunkAccess chunk, BlockPos.MutableBlockPos pos)" in java_src,
)

check(
    "Imports CompositeWorldLayer",
    "import dev.ergenverse.runtime.layer.CompositeWorldLayer;" in java_src,
)

check(
    "Imports WorldLayer",
    "import dev.ergenverse.runtime.layer.WorldLayer;" in java_src,
)

check(
    "Imports ChunkContribution",
    "import dev.ergenverse.runtime.layer.ChunkContribution;" in java_src,
)

check(
    "Imports BlockChangeDelta",
    "import dev.ergenverse.runtime.delta.BlockChangeDelta;" in java_src,
)

check(
    "Imports WorldRuntime",
    "import dev.ergenverse.runtime.WorldRuntime;" in java_src,
)

check(
    "Imports Provenance",
    "import dev.ergenverse.runtime.Provenance;" in java_src,
)

check(
    "Calls WorldRuntime.get() defensively",
    "runtime = WorldRuntime.get();" in java_src and "if (!runtime.isInitialized()) return;" in java_src,
)

check(
    "Iterates layersInMaterializationOrder",
    "worldLayer.layersInMaterializationOrder()" in java_src,
)

check(
    "Skips CANON layer (structures need live ServerLevel)",
    "if (layer.provenance() == Provenance.CANON) continue;" in java_src,
)

check(
    "Calls getChunkContribution per layer",
    "layer.getChunkContribution(chunkX, chunkZ)" in java_src,
)

check(
    "Applies blockChanges via chunk.setBlockState",
    "for (BlockChangeDelta delta : contribution.blockChanges)" in java_src
    and "chunk.setBlockState(pos, state, false)" in java_src,
)

check(
    "resolveBlockState helper defined (default state, no property parsing)",
    "private static BlockState resolveBlockState(String blockId)" in java_src
    and "block.defaultBlockState()" in java_src,
)

check(
    "applyLayerOverrides is called from fillFromNoise",
    "applyLayerOverrides(chunk, pos);" in java_src,
)

check(
    "Debug screen info updated to report layer journal",
    "Layer journal: PLAYER=" in java_src and "SIMULATION=" in java_src,
)

# ──────────────────────────────────────────────────────────────────────────
# 2. No regression — existing CRON-60/67 surface height + codec still intact
# ──────────────────────────────────────────────────────────────────────────
print("\n[2] No regression — CRON-60/67 surface height + codec intact")

check(
    "CODEC still registered (biome_source + settings fields)",
    "BiomeSource.CODEC.fieldOf(\"biome_source\")" in java_src
    and "NoiseGeneratorSettings.CODEC.fieldOf(\"settings\")" in java_src,
)

check(
    "canonSurfaceHeight still public static (used by builders)",
    "public static int canonSurfaceHeight(int worldX, int worldZ)" in java_src,
)

check(
    "canonTerrainOffset still present",
    "static int getCanonTerrainOffset(int worldX, int worldZ)" in java_src,
)

check(
    "canonNoiseVariation still present",
    "static int canonNoiseVariation(int worldX, int worldZ)" in java_src,
)

check(
    "Canon warp table still present (heng_yue_sect → MAX_WARP_HEIGHT)",
    'case "heng_yue_sect" -> MAX_WARP_HEIGHT;' in java_src,
)

check(
    "Sea of Devils warp still -MAX_WARP_HEIGHT",
    'case "sea_of_devils" -> -MAX_WARP_HEIGHT;' in java_src,
)

check(
    "applyCarvers still delegates to wrapped",
    "wrapped.applyCarvers(" in java_src,
)

check(
    "buildSurface still delegates to wrapped",
    "wrapped.buildSurface(" in java_src,
)

# ──────────────────────────────────────────────────────────────────────────
# 3. Missing biome JSONs now exist
# ──────────────────────────────────────────────────────────────────────────
print("\n[3] Missing biome JSONs created")

snow_country = BIOME_DIR / "snow_domain_country.json"
xuan_wu_country = BIOME_DIR / "xuan_wu_country.json"

check("snow_domain_country.json exists", snow_country.exists())
check("xuan_wu_country.json exists", xuan_wu_country.exists())

# ──────────────────────────────────────────────────────────────────────────
# 4. Biome source integrity — every biome referenced in planet_suzaku.json exists
# ──────────────────────────────────────────────────────────────────────────
print("\n[4] planet_suzaku.json biome source integrity")

dim_data = json.loads(DIM_JSON.read_text(encoding="utf-8"))
biomes = dim_data["generator"]["biome_source"]["biomes"]
referenced_biomes = [b["biome"].replace("ergenverse:", "") for b in biomes]

check(f"planet_suzaku.json references {len(referenced_biomes)} biomes", len(referenced_biomes) == 15)

missing = []
for biome_id in referenced_biomes:
    biome_path = BIOME_DIR / f"{biome_id}.json"
    if not biome_path.exists():
        missing.append(biome_id)

check(
    "All referenced biomes have JSON files",
    len(missing) == 0,
    f"missing: {missing}" if missing else "",
)

# Check the two new biomes are actually referenced
check(
    "snow_domain_country is referenced in planet_suzaku.json",
    "snow_domain_country" in referenced_biomes,
)
check(
    "xuan_wu_country is referenced in planet_suzaku.json",
    "xuan_wu_country" in referenced_biomes,
)

# ──────────────────────────────────────────────────────────────────────────
# 5. New biome JSONs follow country-biome schema
# ──────────────────────────────────────────────────────────────────────────
print("\n[5] New biome JSON schema validation")

for biome_file, expected_temp_range, expected_name in [
    (snow_country, (-1.0, -0.4), "snow_domain_country"),
    (xuan_wu_country, (-0.6, -0.1), "xuan_wu_country"),
]:
    if not biome_file.exists():
        continue
    try:
        data = json.loads(biome_file.read_text(encoding="utf-8"))
    except json.JSONDecodeError as e:
        check(f"{expected_name}: valid JSON", False, str(e))
        continue

    check(f"{expected_name}: valid JSON", True)

    check(
        f"{expected_name}: has temperature",
        "temperature" in data and isinstance(data["temperature"], (int, float)),
    )
    check(
        f"{expected_name}: temperature in cold range",
        expected_temp_range[0] <= data.get("temperature", 999) <= expected_temp_range[1],
        f"got {data.get('temperature')}",
    )
    check(f"{expected_name}: has downfall", "downfall" in data)
    check(f"{expected_name}: has_precipitation is true", data.get("has_precipitation") is True)
    check(f"{expected_name}: has effects.sky_color", "effects" in data and "sky_color" in data["effects"])
    check(f"{expected_name}: has effects.fog_color", "effects" in data and "fog_color" in data["effects"])
    check(f"{expected_name}: has spawners", "spawners" in data and "monster" in data["spawners"])
    check(f"{expected_name}: has carvers", "carvers" in data and "air" in data["carvers"])
    check(f"{expected_name}: has features list", "features" in data and isinstance(data["features"], list))
    check(
        f"{expected_name}: references spirit_vein_quartz_ore feature",
        any("spirit_vein_quartz_ore" in str(s) for s in data.get("features", [])),
    )
    check(
        f"{expected_name}: _comment mentions CRON-91",
        "CRON-91" in data.get("_comment", ""),
    )

# Snow Domain should spawn strays (cold-country monster)
if snow_country.exists():
    data = json.loads(snow_country.read_text(encoding="utf-8"))
    monsters = data.get("spawners", {}).get("monster", [])
    has_stray = any(m.get("type") == "minecraft:stray" for m in monsters)
    check("snow_domain_country: spawns strays (cold-country monster)", has_stray)

# ──────────────────────────────────────────────────────────────────────────
# 6. Architectural invariant — CANON structures NOT built in fillFromNoise
# ──────────────────────────────────────────────────────────────────────────
print("\n[6] Architectural invariants")

# Make sure fillFromNoise does NOT call StructureBuilderRegistry (CANON structures
# need a live ServerLevel — only the materializer can build them).
# We strip javadoc and line comments before checking, so that mentioning the
# class in documentation doesn't trigger a false positive.
java_stripped = re.sub(r"/\*\*.*?\*/", "", java_src, flags=re.DOTALL)  # block comments
java_stripped = re.sub(r"//[^\n]*", "", java_stripped)  # line comments
check(
    "fillFromNoise does NOT call StructureBuilderRegistry (code, not comments)",
    "StructureBuilderRegistry" not in java_stripped,
)

# Make sure resolveBlockState is private (not exposed as part of public API)
check(
    "resolveBlockState is private static (not public API)",
    "private static BlockState resolveBlockState" in java_src,
)

# Make sure applyLayerOverrides is private
check(
    "applyLayerOverrides is private (internal helper)",
    "private void applyLayerOverrides" in java_src,
)

# ──────────────────────────────────────────────────────────────────────────
# 7. Summary
# ──────────────────────────────────────────────────────────────────────────
print("\n" + "=" * 60)
print(f"RESULT: {passed} passed, {failed} failed")
print("=" * 60)

if failed > 0:
    print("\nFAILED CHECKS:")
    for name, ok, detail in checks:
        if not ok:
            print(f"  - {name}  {detail}")
    sys.exit(1)
else:
    print("\nALL CHECKS PASSED")
    sys.exit(0)
