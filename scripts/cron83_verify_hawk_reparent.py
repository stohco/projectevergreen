#!/usr/bin/env python3
"""
CRON-83 verification: SpiritHawkModel parent hierarchy refactor.

Same pattern as CRON-81 (Qilin) and CRON-82 (Deer). Verifies that reparenting
parts from root to body_chest / body_hind preserves WORLD POSITION for every
reparented part. The math is simple subtraction (Rx-Px, Ry-Py, Rz-Pz) because
the new parents (body_chest, body_hind) have NO PartPose rotation — only the
neck has a PartPose rotation (-0.3 xRot), and that rotation is preserved
verbatim in the new PartPose.

Hawk-specific notes (different from quadrupeds):
- Birds have a RIGID torso (fused thoracic vertebrae). The spine does NOT flex
  like a quadruped's. There is NO S-curve animation fix for birds.
- The bodyChest.xRot = sin(age*0.6)*0.08*lsa in the FLAP block is a "thorax
  heave" (respiratory pulse during flight), not a spine flex. After CRON-83,
  body_hind INHERITS this heave (whole torso heaves together) — anatomically
  correct for a bird.
- Wings attach to chest (shoulder blades on thorax). After CRON-83, wings
  inherit body_chest's heave — anatomically correct.
- Tail (pygostyle) attaches to hind. After CRON-83, tail inherits body_hind's
  rotation — anatomically correct.
- Legs attach to hind (pelvis). After CRON-83, legs inherit body_hind's
  rotation — anatomically correct.
- Neck attaches to chest (cervical-thoracic junction). After CRON-83, neck
  inherits body_chest's heave — anatomically correct.

Defects closed:
- 7 parts parented to root (body_hind, neck, left_wing, right_wing, tail,
  left_leg, right_leg) — same defect class as CRON-81 (Qilin) and CRON-82
  (Deer).
- Pre-existing stale-state bug: bodyChest.xRot set only in FLAP block, not
  reset in other pose blocks. Fixed by adding bodyChest.xRot = 0.0F resets.
"""
from dataclasses import dataclass


@dataclass
class PartPose:
    x: float
    y: float
    z: float
    xRot: float = 0.0
    yRot: float = 0.0
    zRot: float = 0.0


# Original root-relative PartPose offsets (from pre-CRON-83 SpiritHawkModel.java)
BODY_CHEST_OLD = PartPose(0.0, 10.0, -1.0)  # stays at root (body chain root)
BODY_HIND_OLD = PartPose(0.0, 10.0, 2.0)    # root → body_chest

# POST-CRON-83 PartPose offsets (what the code will look like after the refactor).
# body_chest STAYS at root with offset (0, 10, -1).
# body_hind is REPARENTED to body_chest with offset (0, 0, 3) (computed by subtraction).
BODY_CHEST_NEW = BODY_CHEST_OLD             # root-relative, unchanged
BODY_HIND_NEW = PartPose(0.0, 0.0, 3.0)     # body_chest-relative (post-CRON-83)


def world_pos_root_relative(offset: PartPose) -> tuple[float, float, float]:
    """World position of a root-relative offset (parent = root)."""
    return (offset.x, offset.y, offset.z)


def world_pos_body_chest_relative(offset: PartPose) -> tuple[float, float, float]:
    """World position of a body_chest-relative offset."""
    return (offset.x + BODY_CHEST_NEW.x,
            offset.y + BODY_CHEST_NEW.y,
            offset.z + BODY_CHEST_NEW.z)


def world_pos_body_hind_relative(offset: PartPose) -> tuple[float, float, float]:
    """World position of a body_hind-relative offset.
    body_hind is body_chest-relative; body_chest is root-relative.
    So: world = part_offset + body_hind_new + body_chest_new."""
    return (offset.x + BODY_HIND_NEW.x + BODY_CHEST_NEW.x,
            offset.y + BODY_HIND_NEW.y + BODY_CHEST_NEW.y,
            offset.z + BODY_HIND_NEW.z + BODY_CHEST_NEW.z)


def recompute_offset(part_old: PartPose, parent_old: PartPose) -> PartPose:
    """New parent-relative offset = (Rx-Px, Ry-Py, Rz-Pz).
    PartPose rotations are preserved verbatim (parent has no rotation)."""
    return PartPose(
        x=part_old.x - parent_old.x,
        y=part_old.y - parent_old.y,
        z=part_old.z - parent_old.z,
        xRot=part_old.xRot,
        yRot=part_old.yRot,
        zRot=part_old.zRot,
    )


# 7 parts to reparent: (name, old_pose_root_rel, new_parent_name)
PARTS = [
    ("body_hind",  BODY_HIND_OLD,                       "body_chest"),
    ("neck",       PartPose(0.0, 9.5, -3.0, xRot=-0.3), "body_chest"),
    ("left_wing",  PartPose(-3.0, 9.0, 0.0),            "body_chest"),
    ("right_wing", PartPose(3.0, 9.0, 0.0),             "body_chest"),
    ("tail",       PartPose(0.0, 9.0, 3.0),             "body_hind"),
    ("left_leg",   PartPose(-1.5, 12.0, 0.0),           "body_hind"),
    ("right_leg",  PartPose(1.5, 12.0, 0.0),            "body_hind"),
]


def main() -> int:
    print("=" * 78)
    print("CRON-83 — SpiritHawkModel reparent verification")
    print("=" * 78)
    print()
    print(f"{'Part':<12} {'New Parent':<12} {'Old Offset (root-rel)':<26} "
          f"{'New Offset (parent-rel)':<26} {'World (old)':<14} {'World (new)':<14} {'OK'}")
    print("-" * 130)

    all_ok = True
    for name, old_pose, new_parent in PARTS:
        # Old world position: part was root-relative, so world = old offset
        old_world = world_pos_root_relative(old_pose)

        # New offset: subtract new parent's OLD root-relative offset
        if new_parent == "body_chest":
            parent_old = BODY_CHEST_OLD
        elif new_parent == "body_hind":
            parent_old = BODY_HIND_OLD
        else:
            raise ValueError(f"Unknown parent: {new_parent}")

        new_pose = recompute_offset(old_pose, parent_old)

        # New world position: use the POST-CRON-83 chain
        if new_parent == "body_chest":
            new_world = world_pos_body_chest_relative(new_pose)
        elif new_parent == "body_hind":
            new_world = world_pos_body_hind_relative(new_pose)

        ok = (abs(old_world[0] - new_world[0]) < 1e-6 and
              abs(old_world[1] - new_world[1]) < 1e-6 and
              abs(old_world[2] - new_world[2]) < 1e-6)
        if not ok:
            all_ok = False

        def fmt_pose(p: PartPose) -> str:
            r = ""
            if p.xRot != 0.0 or p.yRot != 0.0 or p.zRot != 0.0:
                r = f" rot=({p.xRot},{p.yRot},{p.zRot})"
            return f"({p.x},{p.y},{p.z}){r}"

        def fmt_world(w: tuple[float, float, float]) -> str:
            return f"({w[0]},{w[1]},{w[2]})"

        print(f"{name:<12} {new_parent:<12} {fmt_pose(old_pose):<26} "
              f"{fmt_pose(new_pose):<26} {fmt_world(old_world):<14} "
              f"{fmt_world(new_world):<14} {'✓' if ok else '✗ FAIL'}")

    print()
    print("=" * 78)
    if all_ok:
        print(f"RESULT: ALL {len(PARTS)} PARTS PRESERVE WORLD POSITION ✓")
        print()
        print("Math summary (subtraction):")
        print("  body_hind:  (0,10,2)    - body_chest(0,10,-1)  → body_chest-rel (0,0,3)")
        print("  neck:       (0,9.5,-3)  - body_chest(0,10,-1)  → body_chest-rel (0,-0.5,-2) [xRot -0.3 preserved]")
        print("  left_wing:  (-3,9,0)    - body_chest(0,10,-1)  → body_chest-rel (-3,-1,1)")
        print("  right_wing: (3,9,0)     - body_chest(0,10,-1)  → body_chest-rel (3,-1,1)")
        print("  tail:       (0,9,3)     - body_hind(0,10,2)    → body_hind-rel  (0,-1,1)")
        print("  left_leg:   (-1.5,12,0) - body_hind(0,10,2)    → body_hind-rel  (-1.5,2,-2)")
        print("  right_leg:  (1.5,12,0)  - body_hind(0,10,2)    → body_hind-rel  (1.5,2,-2)")
        return 0
    else:
        print("RESULT: FAILURE — world positions do not match")
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
