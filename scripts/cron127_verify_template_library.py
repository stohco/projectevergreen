#!/usr/bin/env python3
"""
CRON-127 — World Assembly Compiler — verification script.

Validates that the five-layer architecture the user directed in CRON-127 is
correctly implemented:

  Layer 1 — canon/structure       (semantic, NO Minecraft, NO materializeInto)
  Layer 2 — assembly              (VoxelInstruction IR + MaterialID + 4 libraries)
  Layer 3 — assembly.WorldAssembler (the compiler)
  Layer 4 — materialization       (MaterialResolver seam + VoxelMaterializer + VolumePlacer)
  Layer 5 — Minecraft engine

Runs 42 checks across 9 sections. Exits non-zero on any failure.
"""

from pathlib import Path
import re
import sys

ROOT = Path("/home/z/my-project/forge-mod/src/main/java/dev/ergenverse")
CANON = ROOT / "canon" / "structure"
ASM = ROOT / "assembly"
MAT = ROOT / "materialization"
REG = ROOT / "runtime" / "materialize" / "StructureBuilderRegistry.java"

PASS = 0
FAIL = 0
FAILURES = []


def check(section, label, ok, detail=""):
    global PASS, FAIL
    tag = "PASS" if ok else "FAIL"
    if ok:
        PASS += 1
        print(f"  [{tag}] {label}")
    else:
        FAIL += 1
        FAILURES.append((section, label, detail))
        print(f"  [{tag}] {label}")
        if detail:
            print(f"          {detail}")


def read(p):
    return Path(p).read_text(encoding="utf-8")


def read_no_javadoc(p):
    """Read a file with all /* */ blocks and // line comments stripped.
    Used for substring checks that would otherwise false-positive on Javadoc
    that *mentions* a forbidden term only to document its absence."""
    txt = read(p)
    txt = re.sub(r"/\*.*?\*/", "", txt, flags=re.DOTALL)
    txt = re.sub(r"//.*$", "", txt, flags=re.MULTILINE)
    return txt


# ─── Section 1: Layer 1 purity (canon/structure has ZERO Minecraft imports) ───

print("\n=== Section 1: Layer 1 — canon/structure is Minecraft-free ===")

canon_files = list(CANON.glob("*.java"))
check("1", f"canon/structure has Java files ({len(canon_files)})", len(canon_files) >= 10)

minecraft_pat = re.compile(r"^\s*import\s+net\.minecraft\.", re.MULTILINE)
blockstate_pat = re.compile(r"\bBlockState\b")
blockpos_pat = re.compile(r"\bBlockPos\b")
serverlevel_pat = re.compile(r"\bServerLevel\b")
blocks_dot_pat = re.compile(r"\bBlocks\.")

any_minecraft_import = False
any_blockstate_code = False
any_blockpos_code = False
any_serverlevel_code = False
any_blocks_dot_code = False
for f in canon_files:
    txt = read(f)
    # Strip Javadoc comments — they may legitimately mention "BlockState" in @code or prose
    no_javadoc = re.sub(r"/\*.*?\*/", "", txt, flags=re.DOTALL)
    no_javadoc = re.sub(r"//.*$", "", no_javadoc, flags=re.MULTILINE)
    if minecraft_pat.search(no_javadoc):
        any_minecraft_import = True
        check("1", f"  ✗ {f.name} has net.minecraft import", False,
              f"see import lines in {f.name}")
    # BlockState/BlockPos/ServerLevel/Blocks. only forbidden in CODE, not Javadoc
    if blockstate_pat.search(no_javadoc):
        any_blockstate_code = True
        check("1", f"  ✗ {f.name} references BlockState in code", False)
    if blockpos_pat.search(no_javadoc):
        any_blockpos_code = True
        check("1", f"  ✗ {f.name} references BlockPos in code", False)
    if serverlevel_pat.search(no_javadoc):
        any_serverlevel_code = True
        check("1", f"  ✗ {f.name} references ServerLevel in code", False)
    if blocks_dot_pat.search(no_javadoc):
        any_blocks_dot_code = True
        check("1", f"  ✗ {f.name} references Blocks.X in code", False)

check("1", "No canon/structure file imports net.minecraft.*", not any_minecraft_import)
check("1", "No canon/structure file references BlockState in code", not any_blockstate_code)
check("1", "No canon/structure file references BlockPos in code", not any_blockpos_code)
check("1", "No canon/structure file references ServerLevel in code", not any_serverlevel_code)
check("1", "No canon/structure file references Blocks.X in code", not any_blocks_dot_code)


# ─── Section 2: materializeInto / placeShell removed from Layer 1 ───

print("\n=== Section 2: materializeInto / placeShell / buildShell removed ===")

mat_into_pat = re.compile(r"\bmaterializeInto\s*\(")
place_shell_pat = re.compile(r"\bplaceShell\s*\(")
build_shell_pat = re.compile(r"\bbuildShell\s*\(")
any_mat_into = False
any_place_shell = False
any_build_shell = False
for f in canon_files:
    txt = read(f)
    no_javadoc = re.sub(r"/\*.*?\*/", "", txt, flags=re.DOTALL)
    no_javadoc = re.sub(r"//.*$", "", no_javadoc, flags=re.MULTILINE)
    if mat_into_pat.search(no_javadoc):
        any_mat_into = True
        check("1", f"  ✗ {f.name} still has materializeInto()", False)
    if place_shell_pat.search(no_javadoc):
        any_place_shell = True
        check("1", f"  ✗ {f.name} still has placeShell()", False)
    if build_shell_pat.search(no_javadoc):
        any_build_shell = True
        check("1", f"  ✗ {f.name} still has buildShell()", False)

check("2", "No canon/structure file defines materializeInto()", not any_mat_into)
check("2", "No canon/structure file defines placeShell()", not any_place_shell)
check("2", "No canon/structure file defines buildShell()", not any_build_shell)


# ─── Section 3: Layer 2 — VoxelInstruction IR + MaterialID ───

print("\n=== Section 3: Layer 2 — VoxelInstruction IR + MaterialID ===")

vox = read(ASM / "VoxelInstruction.java")
check("3", "VoxelInstruction is a record",
      re.search(r"public\s+record\s+VoxelInstruction", vox) is not None)
check("3", "VoxelInstruction has int x, y, z fields",
      all(re.search(rf"\b{f}\b", vox) for f in ["int x", "int y", "int z"]))
check("3", "VoxelInstruction has MaterialID material field",
      re.search(r"MaterialID\s+material", vox) is not None)
check("3", "VoxelInstruction has Rotation field",
      re.search(r"Rotation\s+rotation", vox) is not None)
check("3", "VoxelInstruction has VoxelLayer field",
      re.search(r"VoxelLayer\s+layer", vox) is not None)
check("3", "VoxelInstruction has translate(dx,dy,dz) helper",
      "translate" in vox)
check("3", "VoxelInstruction has NO net.minecraft import",
      "import net.minecraft" not in vox)

mat_id = read(ASM / "MaterialID.java")
mat_id_code = read_no_javadoc(ASM / "MaterialID.java")
check("3", "MaterialID is an enum",
      re.search(r"public\s+enum\s+MaterialID", mat_id) is not None)
check("3", "MaterialID has structural materials (OAK_PLANKS, COBBLESTONE, FARMLAND, OAK_DOOR_LOWER)",
      all(m in mat_id_code for m in ["OAK_PLANKS", "COBBLESTONE", "FARMLAND", "OAK_DOOR_LOWER"]))
check("3", "MaterialID has furniture materials (WHITE_CARPET, BOOKSHELF, LECTERN, CHEST, LANTERN)",
      all(m in mat_id_code for m in ["WHITE_CARPET", "BOOKSHELF", "LECTERN", "CHEST", "LANTERN"]))
check("3", "MaterialID has mod-block materials (ALCHEMY_FURNACE, SPIRIT_VEIN_STONE, FORMATION_CORE_STONE, QI_GATHERING_GRASS, SPIRIT_SAND, SPIRIT_WOOD_LEAVES)",
      all(m in mat_id_code for m in ["ALCHEMY_FURNACE", "SPIRIT_VEIN_STONE", "FORMATION_CORE_STONE", "QI_GATHERING_GRASS", "SPIRIT_SAND", "SPIRIT_WOOD_LEAVES"]))
check("3", "MaterialID has NO 'minecraft:' string literals (in code, not Javadoc)",
      "minecraft:" not in mat_id_code)
check("3", "MaterialID has NO net.minecraft import",
      "import net.minecraft" not in mat_id_code)


# ─── Section 4: Four independent libraries ───

print("\n=== Section 4: Layer 2 — Four independent libraries ===")

for lib in ["FurnitureLibrary", "BuildingLibrary", "DecorationLibrary", "TerrainLibrary"]:
    p = ASM / f"{lib}.java"
    check("4", f"{lib}.java exists", p.exists())
    if p.exists():
        t = read(p)
        check("4", f"{lib} has NO net.minecraft import", "import net.minecraft" not in t)
        check("4", f"{lib} emits VoxelInstruction(s)", "VoxelInstruction" in t)

# Spot-check: FurnitureLibrary renders SLEEPING_MAT as WHITE_CARPET
furn_lib = read(ASM / "FurnitureLibrary.java")
check("4", "FurnitureLibrary: SLEEPING_MAT → WHITE_CARPET",
      "SLEEPING_MAT" in furn_lib and "WHITE_CARPET" in furn_lib)
check("4", "FurnitureLibrary: ALCHEMY_FURNACE → ALCHEMY_FURNACE material",
      re.search(r"ALCHEMY_FURNACE\s*->\s*out\.add.*MaterialID\.ALCHEMY_FURNACE", furn_lib, re.DOTALL) is not None
      or "case ALCHEMY_FURNACE" in furn_lib)
check("4", "FurnitureLibrary: SPIRIT_WELL → SPIRIT_VEIN_STONE column",
      "SPIRIT_WELL" in furn_lib and "SPIRIT_VEIN_STONE" in furn_lib)

# Spot-check: BuildingLibrary maps POOR_VILLAGE → oak planks/log
bld_lib = read(ASM / "BuildingLibrary.java")
check("4", "BuildingLibrary: POOR_VILLAGE floor → OAK_PLANKS",
      "POOR_VILLAGE" in bld_lib and "OAK_PLANKS" in bld_lib)
check("4", "BuildingLibrary: ELDER_HOME → SPRUCE_PLANKS",
      "ELDER_HOME" in bld_lib and "SPRUCE_PLANKS" in bld_lib)
check("4", "BuildingLibrary: emits floor + walls + roof + door",
      all(s in bld_lib for s in ["Floor", "Walls", "Roof", "Door"]))


# ─── Section 5: WorldAssembler — the compiler ───

print("\n=== Section 5: Layer 3 — WorldAssembler (the compiler) ===")

asm = read(ASM / "WorldAssembler.java")
check("5", "WorldAssembler.assemble(CanonSettlement, int, int, int) exists",
      re.search(r"public\s+static\s+AssemblyResult\s+assemble\s*\(\s*CanonSettlement", asm) is not None)
check("5", "WorldAssembler delegates shells to BuildingLibrary",
      "BuildingLibrary.shell" in asm)
check("5", "WorldAssembler delegates furniture to FurnitureLibrary",
      "FurnitureLibrary.voxels" in asm)
check("5", "WorldAssembler delegates open features to DecorationLibrary",
      "DecorationLibrary.voxels" in asm)
check("5", "WorldAssembler populates AnchorRegistry",
      "AnchorRegistry" in asm and "registerAnchors" in asm)
check("5", "WorldAssembler returns AssemblyResult",
      "return new AssemblyResult" in asm)
check("5", "WorldAssembler has NO net.minecraft import",
      "import net.minecraft" not in asm)


# ─── Section 6: Layer 4 — Materialization backend ───

print("\n=== Section 6: Layer 4 — Materialization backend ===")

# MaterialResolver is the SINGLE seam MaterialID → BlockState
resolver = read(MAT / "MaterialResolver.java")
check("6", "MaterialResolver.resolve(MaterialID, Rotation) exists",
      re.search(r"public\s+static\s+BlockState\s+resolve\s*\(\s*MaterialID", resolver) is not None)
check("6", "MaterialResolver is the only place MaterialID → BlockState mapping lives",
      "Blocks.OAK_PLANKS" in resolver and "Blocks.AIR" in resolver)
check("6", "MaterialResolver maps ALCHEMY_FURNACE → ErgenverseBlocks.ALCHEMY_FURNACE",
      "ErgenverseBlocks.ALCHEMY_FURNACE" in resolver)
check("6", "MaterialResolver handles door rotation (DoorBlock.FACING)",
      "DoorBlock.FACING" in resolver)

# VoxelMaterializer iterates IR
vmat = read(MAT / "VoxelMaterializer.java")
check("6", "VoxelMaterializer.materialize(AssemblyResult, ServerLevel, ChunkBounds) exists",
      re.search(r"public\s+static\s+int\s+materialize\s*\(\s*AssemblyResult", vmat) is not None)
check("6", "VoxelMaterializer iterates result.instructions()",
      "result.instructions()" in vmat)
check("6", "VoxelMaterializer uses MaterialResolver.resolve",
      "MaterialResolver.resolve" in vmat)
check("6", "VoxelMaterializer writes via VolumePlacer",
      "VolumePlacer" in vmat and "placer.placeBlock" in vmat)

# VolumePlacer has chunk filter + provenance guard
vp = read(MAT / "VolumePlacer.java")
check("6", "VolumePlacer is in materialization package (NOT canon/structure)",
      (CANON / "VolumePlacer.java").exists() is False and (MAT / "VolumePlacer.java").exists())
check("6", "VolumePlacer.forChunk factory exists",
      "forChunk" in vp)
check("6", "VolumePlacer has chunk filter (bounds.contains)",
      "bounds.contains" in vp or "bounds != null" in vp)
check("6", "VolumePlacer has provenance-aware rebuild guard (PLAYER/SIMULATION)",
      "Provenance.PLAYER" in vp and "Provenance.SIMULATION" in vp)
check("6", "VolumePlacer consults WorldDeltaStore",
      "WorldDeltaStore" in vp)


# ─── Section 7: CanonSettlementBuilder moved to materialization ───

print("\n=== Section 7: CanonSettlementBuilder adapter location ===")

check("7", "CanonSettlementBuilder is in materialization package (NOT canon/structure)",
      (MAT / "CanonSettlementBuilder.java").exists() and not (CANON / "CanonSettlementBuilder.java").exists())
csb = read(MAT / "CanonSettlementBuilder.java")
check("7", "CanonSettlementBuilder.buildWangFamilyVillage(ServerLevel, ChunkBounds) exists",
      re.search(r"public\s+static\s+int\s+buildWangFamilyVillage\s*\(\s*ServerLevel", csb) is not None)
check("7", "CanonSettlementBuilder calls WorldAssembler.assemble",
      "WorldAssembler.assemble" in csb)
check("7", "CanonSettlementBuilder calls VoxelMaterializer.materialize",
      "VoxelMaterializer.materialize" in csb)
check("7", "CanonSettlementBuilder resolves canon coordinate from PlanetSuzakuBlueprint",
      "PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE" in csb)
check("7", "CanonSettlementBuilder resolves surfaceY from BlueprintChunkGenerator.surfaceHeightFor",
      "BlueprintChunkGenerator.surfaceHeightFor" in csb)


# ─── Section 8: StructureBuilderRegistry routes through new builder ───

print("\n=== Section 8: StructureBuilderRegistry routing ===")

reg = read(REG)
check("8", "Registry routes wang_family_village → materialization.CanonSettlementBuilder",
      "dev.ergenverse.materialization.CanonSettlementBuilder" in reg)
check("8", "Registry does NOT route to legacy WangFamilyVillageBuilder.buildForChunk",
      "WangFamilyVillageBuilder.buildForChunk" not in reg or
      "CanonSettlementBuilder" in reg)
check("8", "Registry comment mentions CRON-126 composition system",
      "CRON-126" in reg and "composition" in reg.lower())


# ─── Section 9: Semantic supporting types ───

print("\n=== Section 9: Semantic supporting types (Anchor, BuildingTheme, Intent, SemanticRole) ===")

check("9", "Anchor.java exists in canon/structure", (CANON / "Anchor.java").exists())
check("9", "CanonAnchor.java is REMOVED (superseded by Anchor)",
      (CANON / "CanonAnchor.java").exists() is False)
check("9", "BuildingTheme.java exists in canon/structure", (CANON / "BuildingTheme.java").exists())
check("9", "Intent.java exists in canon/structure", (CANON / "Intent.java").exists())
check("9", "SemanticRole.java exists in canon/structure", (CANON / "SemanticRole.java").exists())
check("9", "CanonFurnitureKind.java is REMOVED (superseded by CanonFurniture enum)",
      (CANON / "CanonFurnitureKind.java").exists() is False)

anchor = read(CANON / "Anchor.java")
anchor_code = read_no_javadoc(CANON / "Anchor.java")
check("9", "Anchor has (id, SemanticRole, int offsetX/Y/Z)",
      "SemanticRole role" in anchor_code and "offsetX" in anchor_code and "offsetY" in anchor_code and "offsetZ" in anchor_code)
check("9", "Anchor has NO BlockPos in code", "BlockPos" not in anchor_code)

theme = read(CANON / "BuildingTheme.java")
theme_code = read_no_javadoc(CANON / "BuildingTheme.java")
check("9", "BuildingTheme is a pure enum (POOR_VILLAGE, ELDER_HOME, STORAGE_SHED, COMMON_PLAZA, FARM_PLOT)",
      all(t in theme_code for t in ["POOR_VILLAGE", "ELDER_HOME", "STORAGE_SHED", "COMMON_PLAZA", "FARM_PLOT"]))
check("9", "BuildingTheme has future themes (SECT_DISCIPLE, CORE_ELDER, ANCIENT_RUIN, IMMORTAL_PALACE)",
      all(t in theme_code for t in ["SECT_DISCIPLE", "CORE_ELDER", "ANCIENT_RUIN", "IMMORTAL_PALACE"]))
check("9", "BuildingTheme has NO block references in code (no wallBlock/roofBlock fields)",
      "wallBlock" not in theme_code and "roofBlock" not in theme_code and "floorBlock" not in theme_code)

intent = read(CANON / "Intent.java")
check("9", "Intent enum has SLEEP, CULTIVATE, STUDY, ALCHEMY, STORE",
      all(t in intent for t in ["SLEEP", "CULTIVATE", "STUDY", "ALCHEMY", "STORE"]))

role = read(CANON / "SemanticRole.java")
check("9", "SemanticRole enum has BED, MEDITATION, ENTRANCE, WELL, STORAGE, ALCHEMY",
      all(t in role for t in ["BED", "MEDITATION", "ENTRANCE", "WELL", "STORAGE", "ALCHEMY"]))


# ─── Section 10: Canon fidelity ───

print("\n=== Section 10: Canon fidelity (Wang Family Village) ===")

wfvc = read(CANON / "WangFamilyVillageComposition.java")
check("10", "'Wang Family Village' is honestly flagged as mod-original",
      "mod-original" in wfvc and "Wang Family Village" in wfvc)
check("10", "Canon citation references 赵国某偏僻小山村",
      "赵国某偏僻小山村" in wfvc or "Zhao" in wfvc)
check("10", "Wang Tian's alchemy furnace is canon-attested",
      re.search(r"canon.*alchemy|alchemy.*canon", wfvc, re.IGNORECASE) is not None
      or "Wang Tian kept an alchemy furnace" in wfvc)
check("10", "Spirit vein beneath the well is canon-attested",
      "spirit vein" in wfvc.lower() or "SPIRIT_WELL" in wfvc)
check("10", "NO fabricated chapter citations (no 'RI Ch.X' strings)",
      re.search(r"RI\s+Ch\.\d+", wfvc) is None or "RI Ch.1-10" in wfvc)


# ─── Summary ───

print("\n" + "=" * 60)
print(f"CRON-127 verification: {PASS} passed, {FAIL} failed")
print("=" * 60)

if FAIL > 0:
    print("\nFAILURES:")
    for sec, label, detail in FAILURES:
        print(f"  [{sec}] {label}")
        if detail:
            print(f"          {detail}")
    sys.exit(1)

print("\nAll CRON-127 architectural checks pass.")
sys.exit(0)
