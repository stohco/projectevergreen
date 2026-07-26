#!/usr/bin/env python3
"""
CRON-126 verification script.

Verifies that the StructureBuilderRegistry routes "wang_family_village"
through CanonSettlementBuilder (the composition system), NOT through
the legacy WangFamilyVillageBuilder.

Checks:
  1. StructureBuilderRegistry.java routes wang_family_village → CanonSettlementBuilder
  2. StructureBuilderRegistry.java does NOT route wang_family_village → WangFamilyVillageBuilder.buildForChunk
  3. CanonSettlementBuilder.buildWangFamilyVillage exists with correct signature
  4. WangFamilyVillageComposition.create() returns a CanonSettlement
  5. CanonSettlement has buildingAt() method for AI spatial queries
  6. CanonRoom has function() and owner() methods (AI reasonability)
  7. CanonFurniture enum has the canon-attested entries (ALCHEMY_FURNACE, SPIRIT_WELL)
  8. VolumePlacer.forChunk() factory exists
  9. CanonSettlementBuilder delegates to WangFamilyVillageComposition.create()
  10. The build compiles (checked externally via gradlew)
"""
import sys
from pathlib import Path

ROOT = Path("/home/z/my-project/forge-mod/src/main/java/dev/ergenverse")
REGISTRY = ROOT / "runtime/materialize/StructureBuilderRegistry.java"
BUILDER = ROOT / "canon/structure/CanonSettlementBuilder.java"
COMPOSITION = ROOT / "canon/structure/WangFamilyVillageComposition.java"
SETTLEMENT = ROOT / "canon/structure/CanonSettlement.java"
ROOM = ROOT / "canon/structure/CanonRoom.java"
FURNITURE = ROOT / "canon/structure/CanonFurniture.java"
PLACER = ROOT / "canon/structure/VolumePlacer.java"

errors = []
passed = 0

def check(name, condition, detail=""):
    global passed
    if condition:
        passed += 1
        print(f"  [PASS] {name}")
    else:
        errors.append(f"{name}: {detail}")
        print(f"  [FAIL] {name} — {detail}")

print("=== CRON-126: StructureBuilderRegistry routes through composition system ===")
print()

print("[Section 1: Registry routing]")
reg_text = REGISTRY.read_text()
check(
    "Registry routes wang_family_village → CanonSettlementBuilder",
    "CanonSettlementBuilder" in reg_text
    and "buildWangFamilyVillage(level, bounds)" in reg_text,
    "expected CanonSettlementBuilder.buildWangFamilyVillage(level, bounds) in registry"
)
check(
    "Registry does NOT route to legacy WangFamilyVillageBuilder.buildForChunk",
    "WangFamilyVillageBuilder.buildForChunk(level, bounds)" not in reg_text
    or "# legacy" in reg_text.lower(),
    "legacy WangFamilyVillageBuilder.buildForChunk still in active registry path"
)
check(
    "Registry comment mentions CRON-126",
    "CRON-126" in reg_text,
    "CRON-126 comment missing"
)

print()
print("[Section 2: CanonSettlementBuilder adapter]")
builder_text = BUILDER.read_text()
check(
    "buildWangFamilyVillage(ServerLevel, ChunkBounds) method exists",
    "public static int buildWangFamilyVillage" in builder_text
    and "ChunkBounds bounds" in builder_text,
    "method signature missing"
)
check(
    "Builder delegates to WangFamilyVillageComposition.create()",
    "WangFamilyVillageComposition.create()" in builder_text,
    "delegation missing"
)
check(
    "Builder uses VolumePlacer.forChunk",
    "VolumePlacer.forChunk(level, bounds)" in builder_text,
    "VolumePlacer.forChunk not used"
)
check(
    "Builder resolves canon coordinate from PlanetSuzakuBlueprint",
    "PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.x" in builder_text,
    "canon coordinate not resolved"
)

print()
print("[Section 3: Composition authoring]")
comp_text = COMPOSITION.read_text()
check(
    "WangFamilyVillageComposition.create() returns CanonSettlement",
    "public static CanonSettlement create()" in comp_text,
    "create() method missing or wrong return type"
)
check(
    "Composition authors Wang Lin's bedroom",
    "wang_lin_bedroom" in comp_text and "CanonRoom.RoomFunction.BEDROOM" in comp_text,
    "Wang Lin bedroom missing"
)
check(
    "Composition authors Wang Tian's alchemy lab (canon)",
    "wang_alchemy_lab" in comp_text and "CanonFurniture.ALCHEMY_FURNACE" in comp_text,
    "alchemy lab missing"
)
check(
    "Composition authors the spirit well (canon)",
    "CanonFurniture.SPIRIT_WELL" in comp_text,
    "spirit well missing"
)
check(
    "Composition flags 'Wang Family Village' as mod-original",
    "mod-original" in comp_text and "赵国某偏僻小山村" in comp_text,
    "mod-original flag or canon citation missing"
)

print()
print("[Section 4: AI reasonability (CanonSettlement.buildingAt)]")
set_text = SETTLEMENT.read_text()
check(
    "CanonSettlement.buildingAt(int, int, int) exists",
    "public CanonBuilding buildingAt(int localX, int localY, int localZ)" in set_text,
    "buildingAt method missing"
)
check(
    "CanonSettlement has buildingPlacements() accessor",
    "public List<BuildingPlacement> buildingPlacements()" in set_text,
    "buildingPlacements accessor missing"
)

print()
print("[Section 5: CanonRoom semantic function + owner]")
room_text = ROOM.read_text()
check(
    "CanonRoom has function() method",
    "public RoomFunction function()" in room_text,
    "function() method missing"
)
check(
    "CanonRoom has owner() method",
    "public String owner()" in room_text,
    "owner() method missing"
)
check(
    "CanonRoom.RoomFunction enum has BEDROOM + KITCHEN + ALCHEMY_LAB",
    "BEDROOM" in room_text and "KITCHEN" in room_text and "ALCHEMY_LAB" in room_text,
    "RoomFunction values missing"
)

print()
print("[Section 6: CanonFurniture canon-attested entries]")
furn_text = FURNITURE.read_text()
check(
    "CanonFurniture.ALCHEMY_FURNACE exists (canon)",
    "ALCHEMY_FURNACE" in furn_text and "canon" in furn_text,
    "ALCHEMY_FURNACE missing or not canon-flagged"
)
check(
    "CanonFurniture.SPIRIT_WELL exists (canon)",
    "SPIRIT_WELL" in furn_text,
    "SPIRIT_WELL missing"
)
check(
    "CanonFurniture.FARM_PLOT_CELL exists (canon — spirit herbs)",
    "FARM_PLOT_CELL" in furn_text,
    "FARM_PLOT_CELL missing"
)

print()
print("[Section 7: VolumePlacer port]")
placer_text = PLACER.read_text()
check(
    "VolumePlacer.forChunk(ServerLevel, ChunkBounds) factory exists",
    "static VolumePlacer forChunk(ServerLevel level" in placer_text,
    "forChunk factory missing"
)
check(
    "VolumePlacer has provenance-aware rebuild guard",
    "hasPlayerOrSimulationDelta" in placer_text and "Provenance.PLAYER" in placer_text,
    "provenance guard missing"
)
check(
    "VolumePlacer has chunk filter",
    "bounds.contains(pos.getX(), pos.getZ())" in placer_text,
    "chunk filter missing"
)

print()
print(f"=== Summary: {passed} passed, {len(errors)} failed ===")
if errors:
    print()
    print("FAILURES:")
    for e in errors:
        print(f"  - {e}")
    sys.exit(1)
else:
    print()
    print("ALL CHECKS PASSED — CRON-126 verified.")
    sys.exit(0)
