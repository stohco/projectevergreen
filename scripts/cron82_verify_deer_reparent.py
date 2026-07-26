#!/usr/bin/env python3
"""
CRON-82 verification: SpiritDeerModel parent-hierarchy refactor.

Same defect as CRON-81 (Qilin), but SpiritDeerModel has a DIFFERENT and WORSE
animation problem: it animates `this.root.xRot = spineFlex` instead of
`this.bodyChest.xRot = spineFlex`. This means the entire deer rotates as one
rigid block — anatomically WRONG (a deer's spine flexes between chest and hind,
not the whole body).

This script verifies that reparenting body_hind, neck_base, tail, and all 4
thighs from `root` to body_chest/body_hind preserves world positions.

Math (same as CRON-81):
  - World pos of a part at root-relative offset (Rx,Ry,Rz) is just (Rx,Ry,Rz).
  - After reparenting to a parent at root-relative offset (Px,Py,Pz), the new
    parent-relative offset must be (Rx-Px, Ry-Py, Rz-Pz) to preserve world pos.

For parts whose parent ALSO has a PartPose rotation, the math is more complex
(inverse rotation), but in SpiritDeerModel only `neck_base` (+0.9 xRot) and
`tail` (+0.3 xRot) have PartPose rotations among the parts being reparented.
Both are reparented to parents with NO PartPose rotation (body_chest,
body_hind), so the simple subtraction still applies to the OFFSET. The rotation
is preserved verbatim in the new PartPose.offsetAndRotation call.

Run: python3 /home/z/my-project/scripts/cron82_verify_deer_reparent.py
"""
import sys

# Original root-relative PartPose offsets (from SpiritDeerModel.java pre-CRON-82)
PARTS = {
    # name: (root_x, root_y, root_z, parent_pre)
    "body_chest":         (0.0,  6.0, -2.0, "root"),
    "body_hind":          (0.0,  6.0,  2.5, "root"),
    "neck_base":          (0.0,  3.5, -4.0, "root"),
    "tail":               (0.0,  4.0,  4.0, "root"),
    "front_left_thigh":   (-1.5, 9.0, -3.0, "root"),
    "front_right_thigh":  ( 1.5, 9.0, -3.0, "root"),
    "back_left_thigh":    (-1.5, 9.0,  3.0, "root"),
    "back_right_thigh":   ( 1.5, 9.0,  3.0, "root"),
}

# New parent assignments (post-CRON-82)
NEW_PARENT = {
    "body_chest":         "root",
    "body_hind":          "body_chest",
    "neck_base":          "body_chest",
    "tail":               "body_hind",
    "front_left_thigh":   "body_chest",
    "front_right_thigh":  "body_chest",
    "back_left_thigh":    "body_hind",
    "back_right_thigh":   "body_hind",
}

def world_pos(name, parts=PARTS):
    """Compute world position (pre-CRON-82: all parts children of root)."""
    x, y, z, _ = parts[name]
    return (x, y, z)

def new_local_offset(name):
    """Compute the new parent-relative offset that preserves world position."""
    wx, wy, wz = world_pos(name)
    parent = NEW_PARENT[name]
    if parent == "root":
        return (wx, wy, wz)
    px, py, pz = world_pos(parent)
    return (wx - px, wy - py, wz - pz)

def verify():
    print("=" * 72)
    print("CRON-82 SpiritDeerModel reparenting verification")
    print("=" * 72)
    all_ok = True
    for name in PARTS:
        old_world = world_pos(name)
        new_local = new_local_offset(name)
        parent = NEW_PARENT[name]
        if parent == "root":
            new_world = new_local
        else:
            px, py, pz = world_pos(parent)
            new_world = (new_local[0] + px, new_local[1] + py, new_local[2] + pz)
        ok = all(abs(a-b) < 1e-6 for a, b in zip(old_world, new_world))
        status = "OK" if ok else "FAIL"
        if not ok:
            all_ok = False
        print(f"  {name:20s} parent={parent:11s} "
              f"old_world=({old_world[0]:+.2f},{old_world[1]:+.2f},{old_world[2]:+.2f}) "
              f"new_local=({new_local[0]:+.2f},{new_local[1]:+.2f},{new_local[2]:+.2f}) "
              f"[{status}]")
    print("=" * 72)
    if all_ok:
        print("ALL PARTS PRESERVE WORLD POSITION  ✓")
        sys.exit(0)
    else:
        print("WORLD POSITION MISMATCH DETECTED  ✗")
        sys.exit(1)

if __name__ == "__main__":
    verify()
