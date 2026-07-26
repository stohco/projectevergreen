#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-85: Verification script for beast model parent-hierarchy refactor.

Verifies that the reparenting of parts from `root` to `body_chest`/`body_hip`/`body`/
`body_rump` preserves world coordinates for ALL reparented parts across 6 models:
  - SpiritTigerModel
  - SpiritWolfModel
  - SpiritFireBeastModel
  - StoneBackBoarModel
  - SpiritRabbitModel
  - SpiritCraneModel

PRINCIPLE: When a part is reparented from root to a new parent, its new PartPose offset
must be: new_offset = old_offset - parent_offset
(since root is at origin with no rotation, world_pos = parent_world_pos + part_local_pos)

This script verifies:
  1. The mathematical invariant: new_offset == old_offset - parent_offset (for each part)
  2. The Java source files contain the expected new offsets (grep-based check)
  3. The constructor reads from the NEW parent (not root) for each reparented part
  4. Stale-state animation fixes are present where needed (death block resets)

Based on cron81_verify_qilin_reparent.py methodology.
"""

import re
import sys
from pathlib import Path

MODELS_DIR = Path("/home/z/my-project/forge-mod/src/main/java/dev/ergenverse/client/model")

# ============================================================================
# MODEL DEFINITIONS: (part_name, old_parent, new_parent, old_offset, new_offset, has_rotation)
# Offsets are (x, y, z). For parts with rotation, rotation is (xRot, yRot, zRot).
# ============================================================================

MODELS = {
    "SpiritTigerModel": {
        "file": "SpiritTigerModel.java",
        "body_chest_offset": (0.0, 5.5, -3.0),
        "body_hip_offset": (0.0, 5.0, 2.5),
        "parts": [
            # (name, old_parent, new_parent, old_offset, new_offset, rotation)
            ("neck", "root", "body_chest", (0.0, 3.5, -6.0), (0.0, -2.0, -3.0), (-0.3, 0.0, 0.0)),
            ("tail_base", "root", "body_hip", (0.0, 3.5, 5.5), (0.0, -1.5, 3.0), (0.25, 0.0, 0.0)),
            ("front_left_thigh", "root", "body_chest", (-2.5, 8.5, -5.0), (-2.5, 3.0, -2.0), None),
            ("front_right_thigh", "root", "body_chest", (2.5, 8.5, -5.0), (2.5, 3.0, -2.0), None),
            ("back_left_thigh", "root", "body_hip", (-2.0, 8.5, 4.5), (-2.0, 3.5, 2.0), None),
            ("back_right_thigh", "root", "body_hip", (2.0, 8.5, 4.5), (2.0, 3.5, 2.0), None),
        ],
        "stale_state_fix": True,  # death block needs bodyChest/bodyHip resets
    },
    "SpiritWolfModel": {
        "file": "SpiritWolfModel.java",
        "body_chest_offset": (0.0, 6.0, -2.5),
        "body_hip_offset": (0.0, 5.5, 2.5),
        "parts": [
            ("neck", "root", "body_chest", (0.0, 4.0, -5.0), (0.0, -2.0, -2.5), (-0.4, 0.0, 0.0)),
            ("tail_base", "root", "body_hip", (0.0, 4.0, 5.0), (0.0, -1.5, 2.5), (0.3, 0.0, 0.0)),
            ("front_left_thigh", "root", "body_chest", (-2.0, 9.0, -4.0), (-2.0, 3.0, -1.5), None),
            ("front_right_thigh", "root", "body_chest", (2.0, 9.0, -4.0), (2.0, 3.0, -1.5), None),
            ("back_left_thigh", "root", "body_hip", (-2.0, 9.0, 4.0), (-2.0, 3.5, 1.5), None),
            ("back_right_thigh", "root", "body_hip", (2.0, 9.0, 4.0), (2.0, 3.5, 1.5), None),
        ],
        "stale_state_fix": True,  # death block needs bodyChest/bodyHip resets
    },
    "SpiritFireBeastModel": {
        "file": "SpiritFireBeastModel.java",
        "body_chest_offset": (0.0, 5.5, -1.0),
        "body_hip_offset": (0.0, 5.0, 5.0),
        "parts": [
            ("neck", "root", "body_chest", (0.0, 3.5, -5.0), (0.0, -2.0, -4.0), (-0.2, 0.0, 0.0)),
            ("tail_base", "root", "body_hip", (0.0, 4.0, 7.5), (0.0, -1.0, 2.5), (0.2, 0.0, 0.0)),
            ("front_left_thigh", "root", "body_chest", (-2.8, 8.5, -3.5), (-2.8, 3.0, -2.5), None),
            ("front_right_thigh", "root", "body_chest", (2.8, 8.5, -3.5), (2.8, 3.0, -2.5), None),
            ("back_left_thigh", "root", "body_hip", (-2.2, 8.0, 4.5), (-2.2, 3.0, -0.5), None),
            ("back_right_thigh", "root", "body_hip", (2.2, 8.0, 4.5), (2.2, 3.0, -0.5), None),
        ],
        "stale_state_fix": False,  # no spine flex animation, no stale-state bug
    },
    "StoneBackBoarModel": {
        "file": "StoneBackBoarModel.java",
        "body_chest_offset": (0.0, 7.0, -2.0),
        "body_hip_offset": (0.0, 6.5, 3.5),
        "parts": [
            # neck_base already correctly parented to body_chest — not in this list
            ("tail", "root", "body_hip", (0.0, 5.0, 6.0), (0.0, -1.5, 2.5), (0.4, 0.0, 0.0)),
            ("front_left_thigh", "root", "body_chest", (-2.2, 12.0, -3.0), (-2.2, 5.0, -1.0), None),
            ("front_right_thigh", "root", "body_chest", (2.2, 12.0, -3.0), (2.2, 5.0, -1.0), None),
            ("back_left_thigh", "root", "body_hip", (-2.0, 11.5, 3.0), (-2.0, 5.0, -0.5), None),
            ("back_right_thigh", "root", "body_hip", (2.0, 11.5, 3.0), (2.0, 5.0, -0.5), None),
        ],
        "stale_state_fix": False,  # death block already sets bodyChest/bodyHip
    },
    "SpiritRabbitModel": {
        "file": "SpiritRabbitModel.java",
        "body_chest_offset": (0.0, 11.5, -2.0),
        "body_rump_offset": (0.0, 11.0, 1.0),  # Note: "body_rump" not "body_hip"
        "parts": [
            ("head", "root", "body_chest", (0.0, 10.5, -4.0), (0.0, -1.0, -2.0), None),
            ("front_leg_left", "root", "body_chest", (-0.8, 13.0, -3.0), (-0.8, 1.5, -1.0), None),
            ("front_leg_right", "root", "body_chest", (0.8, 13.0, -3.0), (0.8, 1.5, -1.0), None),
            ("hind_thigh_left", "root", "body_rump", (-1.5, 12.0, 2.0), (-1.5, 1.0, 1.0), None),
            ("hind_thigh_right", "root", "body_rump", (1.5, 12.0, 2.0), (1.5, 1.0, 1.0), None),
            ("tail", "root", "body_rump", (0.0, 10.5, 4.0), (0.0, -0.5, 3.0), None),
        ],
        "stale_state_fix": False,  # no spine flex animation
    },
    "SpiritCraneModel": {
        "file": "SpiritCraneModel.java",
        "body_offset": (0.0, 9.0, 0.0),  # Note: "body" not "body_chest"
        "parts": [
            ("neck_base", "root", "body", (0.0, 10.5, -3.5), (0.0, 1.5, -3.5), (-0.3, 0.0, 0.0)),
            ("left_wing", "root", "body", (-1.0, 9.0, 0.0), (-1.0, 0.0, 0.0), None),
            ("right_wing", "root", "body", (1.0, 9.0, 0.0), (1.0, 0.0, 0.0), None),
            ("tail", "root", "body", (0.0, 9.0, 3.5), (0.0, 0.0, 3.5), None),
            ("left_leg", "root", "body", (-1.5, 12.0, 0.0), (-1.5, 3.0, 0.0), None),
            ("right_leg", "root", "body", (1.5, 12.0, 0.0), (1.5, 3.0, 0.0), None),
        ],
        "stale_state_fix": False,  # no body rotation animation
    },
}


def fmt(v):
    """Format a float for Java comparison: 0.0F, -2.0F, 3.5F, 0.25F, etc."""
    # Use up to 2 decimal places, stripping trailing zeros
    s = f"{v:.2f}"
    # Strip trailing zero after decimal: 0.20 -> 0.2, 3.00 -> 3.0
    if '.' in s:
        if s.endswith('0'):
            s = s[:-1]
            if s.endswith('.'):
                s = s + '0'
    if s == "-0.0":
        s = "0.0"
    return s + "F"


def check_math_invariant(model_name, model_def):
    """Check that new_offset = old_offset - parent_offset for each part."""
    errors = []
    for part_name, old_parent, new_parent, old_off, new_off, rotation in model_def["parts"]:
        # Determine parent offset
        if new_parent == "body_chest":
            parent_off = model_def["body_chest_offset"]
        elif new_parent == "body_hip":
            parent_off = model_def["body_hip_offset"]
        elif new_parent == "body_rump":
            parent_off = model_def["body_rump_offset"]
        elif new_parent == "body":
            parent_off = model_def["body_offset"]
        else:
            errors.append(f"  {part_name}: unknown parent '{new_parent}'")
            continue

        # Verify: new = old - parent
        expected = (
            old_off[0] - parent_off[0],
            old_off[1] - parent_off[1],
            old_off[2] - parent_off[2],
        )
        if abs(expected[0] - new_off[0]) > 0.001 or abs(expected[1] - new_off[1]) > 0.001 or abs(expected[2] - new_off[2]) > 0.001:
            errors.append(
                f"  {part_name}: MATH FAIL. old={old_off}, parent={parent_off}, "
                f"expected new={expected}, got new={new_off}"
            )
    return errors


def check_java_source(model_name, model_def):
    """Check that the Java source file contains the expected new offsets and parent references."""
    errors = []
    filepath = MODELS_DIR / model_def["file"]
    if not filepath.exists():
        errors.append(f"  FILE NOT FOUND: {filepath}")
        return errors

    source = filepath.read_text()

    for part_name, old_parent, new_parent, old_off, new_off, rotation in model_def["parts"]:
        # Check that the part is now a child of the new parent in createBodyLayer()
        # Pattern: newParent.addOrReplaceChild("part_name", ...)
        # But the new parent could be a variable (bodyChest, bodyHip) or root
        parent_var_map = {
            "body_chest": "bodyChest",
            "body_hip": "bodyHip",
            "body_rump": "bodyRump",
            "body": "body",
        }
        parent_var = parent_var_map.get(new_parent, new_parent)

        # Check for the OLD pattern (root.addOrReplaceChild) — should NOT exist for this part
        old_pattern = f'root.addOrReplaceChild("{part_name}"'
        if old_pattern in source:
            errors.append(f"  {part_name}: STILL has root.addOrReplaceChild (old pattern present)")

        # Check for the NEW pattern (parentVar.addOrReplaceChild)
        new_pattern = f'{parent_var}.addOrReplaceChild("{part_name}"'
        if new_pattern not in source:
            errors.append(f"  {part_name}: MISSING {parent_var}.addOrReplaceChild pattern")

        # Check that the new offset is present in the source
        # Look for PartPose.offset(x, y, z) or PartPose.offsetAndRotation(x, y, z, ...)
        new_x = fmt(new_off[0])
        new_y = fmt(new_off[1])
        new_z = fmt(new_off[2])

        # Build a regex to find the part's PartPose
        # The pattern is: parentVar.addOrReplaceChild("part_name", ... PartPose.offset(X.0F, Y.0F, Z.0F))
        # or PartPose.offsetAndRotation(X.0F, Y.0F, Z.0F, xRot, 0.0F, 0.0F)
        if rotation:
            xrot = fmt(rotation[0])
            yrot = fmt(rotation[1])
            zrot = fmt(rotation[2])
            pose_pattern = rf'PartPose\.offsetAndRotation\(\s*{re.escape(new_x)}\s*,\s*{re.escape(new_y)}\s*,\s*{re.escape(new_z)}\s*,\s*{re.escape(xrot)}\s*,\s*{re.escape(yrot)}\s*,\s*{re.escape(zrot)}\s*\)'
        else:
            pose_pattern = rf'PartPose\.offset\(\s*{re.escape(new_x)}\s*,\s*{re.escape(new_y)}\s*,\s*{re.escape(new_z)}\s*\)'

        # Search within a window after the part's addOrReplaceChild call
        part_idx = source.find(f'{parent_var}.addOrReplaceChild("{part_name}"')
        if part_idx >= 0:
            window = source[part_idx:part_idx + 1000]
            if not re.search(pose_pattern, window):
                errors.append(
                    f"  {part_name}: new PartPose not found. Expected ({new_x}, {new_y}, {new_z})"
                    + (f", rot=({xrot},{yrot},{zrot})" if rotation else "")
                )

        # Check that the constructor reads from the new parent
        # Pattern: this.fieldName = root.getChild("part_name") should be CHANGED
        # to this.fieldName = parentVar.getChild("part_name")
        # But the field name might differ from part_name. Let's check generically.
        # We look for: root.getChild("part_name") — should NOT exist for reparented parts
        old_constructor = f'root.getChild("{part_name}")'
        if old_constructor in source:
            # Check if it's in a comment (CRON comment might mention old pattern)
            # Simple heuristic: if the line doesn't start with // or *, it's code
            for line in source.split('\n'):
                stripped = line.strip()
                if old_constructor in line and not stripped.startswith('//') and not stripped.startswith('*'):
                    errors.append(f"  {part_name}: constructor STILL uses root.getChild (line: {stripped[:80]})")
                    break

    return errors


def check_stale_state_fix(model_name, model_def):
    """For models with stale_state_fix=True, verify the death block resets bodyChest/bodyHip."""
    if not model_def.get("stale_state_fix"):
        return []

    errors = []
    filepath = MODELS_DIR / model_def["file"]
    source = filepath.read_text()

    # Find the death block: if (entity.deathTime > 0) { ... }
    death_match = re.search(r'if\s*\(entity\.deathTime\s*>\s*0\)\s*\{([^}]*?)\}', source, re.DOTALL)
    if not death_match:
        errors.append("  DEATH BLOCK NOT FOUND")
        return errors

    death_block = death_match.group(1)

    # Check for bodyChest.xRot = 0 (or close to 0)
    if not re.search(r'bodyChest\.xRot\s*=\s*0\.0F', death_block):
        errors.append("  death block MISSING bodyChest.xRot = 0.0F reset")

    # Check for bodyHip.xRot = 0 (or close to 0)
    if not re.search(r'bodyHip\.xRot\s*=\s*0\.0F', death_block):
        errors.append("  death block MISSING bodyHip.xRot = 0.0F reset")

    return errors


def main():
    print("=" * 72)
    print("CRON-COMPLETIONIST-85: Beast Model Parent-Hierarchy Refactor Verification")
    print("Verifies 6 models: Tiger, Wolf, FireBeast, Boar, Rabbit, Crane")
    print("=" * 72)

    all_errors = []
    total_parts = 0

    for model_name, model_def in MODELS.items():
        print(f"\n--- {model_name} ---")
        total_parts += len(model_def["parts"])

        # Check 1: Math invariant
        math_errors = check_math_invariant(model_name, model_def)
        if math_errors:
            print("  MATH CHECK: FAIL")
            for e in math_errors:
                print(e)
            all_errors.extend(math_errors)
        else:
            print(f"  MATH CHECK: PASS ({len(model_def['parts'])} parts verified)")

        # Check 2: Java source
        java_errors = check_java_source(model_name, model_def)
        if java_errors:
            print("  SOURCE CHECK: FAIL")
            for e in java_errors:
                print(e)
            all_errors.extend(java_errors)
        else:
            print(f"  SOURCE CHECK: PASS")

        # Check 3: Stale-state fix (if applicable)
        stale_errors = check_stale_state_fix(model_name, model_def)
        if model_def.get("stale_state_fix"):
            if stale_errors:
                print("  STALE-STATE CHECK: FAIL")
                for e in stale_errors:
                    print(e)
                all_errors.extend(stale_errors)
            else:
                print(f"  STALE-STATE CHECK: PASS (death block resets bodyChest/bodyHip)")
        else:
            print(f"  STALE-STATE CHECK: N/A (no spine flex animation)")

    print("\n" + "=" * 72)
    print(f"SUMMARY: {total_parts} parts across {len(MODELS)} models")
    if all_errors:
        print(f"RESULT: FAIL ({len(all_errors)} errors)")
        for e in all_errors:
            print(f"  {e}")
        sys.exit(1)
    else:
        print(f"RESULT: ALL CHECKS PASSED")
        sys.exit(0)


if __name__ == "__main__":
    main()
