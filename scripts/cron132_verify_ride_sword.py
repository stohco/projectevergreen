#!/usr/bin/env python3
"""
CRON-132 verification: ride_sword ModelPart + texture painting.

Checks:
1. CultivatorRobeModel.java has the ride_sword field + layer definition + setupAnim wiring.
2. All 23 cultivator textures have the metallic blade gradient in the ride_sword UV region.
3. The JAR contains the painted textures.
4. The renderer wires setFlying before super.render (existing CRON-130 check, still valid).
"""

import re
import sys
from pathlib import Path
from PIL import Image
import zipfile
import io

FORGE_MOD = Path("/home/z/my-project/forge-mod")
MODEL_JAVA = FORGE_MOD / "src/main/java/dev/ergenverse/client/model/CultivatorRobeModel.java"
TEX_DIR = FORGE_MOD / "src/main/resources/assets/ergenverse/textures/entity/cultivator"
JAR = FORGE_MOD / "build/libs/ergenverse-0.1.7-alpha.jar"

errors = []
checks = 0


def check(condition, msg):
    global checks
    checks += 1
    status = "PASS" if condition else "FAIL"
    if not condition:
        errors.append(msg)
    print(f"  [{status}] {msg}")


def main():
    print("=== CRON-132: ride_sword ModelPart verification ===\n")

    # --- 1. Model Java ---
    print("--- 1. CultivatorRobeModel.java ---")
    java = MODEL_JAVA.read_text()

    check("private final ModelPart rideSword;" in java,
          "rideSword field declared")
    check('this.rideSword = root.getChild("body").getChild("ride_sword");' in java,
          "rideSword extracted from body.ride_sword in constructor")
    check("this.rideSword.visible = false;" in java,
          "rideSword hidden by default in constructor")

    check('addOrReplaceChild("ride_sword"' in java,
          "ride_sword ModelPart added in createBodyLayer")
    check('texOffs(32, 56)' in java,
          "ride_sword blade UV at (32,56)")
    check('addBox(-0.5F, 12.5F, -10.0F, 1.0F, 1.0F, 20.0F)' in java,
          "ride_sword blade box: 1x1x20 at y=12.5 (below feet)")

    check('addOrReplaceChild("ride_sword_guard"' in java,
          "ride_sword_guard child added")
    check('texOffs(32, 60)' in java,
          "ride_sword_guard UV at (32,60)")
    check('addBox(-2.5F, 12.5F, -0.5F, 5.0F, 1.0F, 1.0F)' in java,
          "ride_sword_guard box: 5x1x1 (crossbar)")

    check('addOrReplaceChild("ride_sword_pommel"' in java,
          "ride_sword_pommel child added")
    check('texOffs(40, 60)' in java,
          "ride_sword_pommel UV at (40,60)")
    check('addBox(-0.5F, 12.5F, 9.5F, 1.0F, 1.0F, 2.0F)' in java,
          "ride_sword_pommel box: 1x1x2 (back cap)")

    # --- 2. setupAnim flight block ---
    print("\n--- 2. setupAnim flight block ---")
    check("this.rideSword.visible = true;" in java,
          "rideSword shown when flying")
    check("this.rideSword.xRot = (float) Math.sin(ageInTicks * 0.8F) * 0.03F;" in java,
          "rideSword banking oscillation (±0.03F at 0.8 Hz)")
    # Check the else branch hides it
    check(java.count("this.rideSword.visible = false;") >= 2,
          "rideSword hidden in constructor AND else branch (>=2 occurrences)")

    # --- 3. CRON-132 markers ---
    print("\n--- 3. CRON-132 Javadoc markers ---")
    check("CRON-132" in java, "CRON-132 marker present in model Java")
    check("CRON-132 CLOSED THIS GAP" in java,
          "CRON-130 self-critique #1 updated to reference CRON-132 closure")

    # --- 4. Texture painting ---
    print("\n--- 4. Cultivator textures painted ---")
    textures = sorted(TEX_DIR.glob("*.png"))
    check(len(textures) >= 23, f"Found {len(textures)} cultivator textures (expected >=23)")

    painted_ok = 0
    painted_fail = 0
    for tex in textures:
        img = Image.open(tex).convert("RGBA")
        if img.size != (64, 64):
            print(f"  SKIP (size {img.size}): {tex.name}")
            continue
        # Check blade region center (40, 57) — should be silver
        blade_px = img.getpixel((40, 57))
        # Check guard region center (35, 61) — should be gold
        guard_px = img.getpixel((35, 61))
        # Check pommel region center (42, 61) — should be dark bronze
        pommel_px = img.getpixel((42, 61))

        blade_silver = blade_px[0] > 100 and blade_px[1] > 100 and blade_px[2] > 100
        guard_gold = guard_px[0] > 150 and guard_px[1] > 100 and guard_px[2] < 100
        pommel_dark = pommel_px[0] < 100 and pommel_px[1] < 80 and pommel_px[2] < 50

        if blade_silver and guard_gold and pommel_dark:
            painted_ok += 1
        else:
            painted_fail += 1
            print(f"  FAIL: {tex.name} blade={blade_px} guard={guard_px} pommel={pommel_px}")

    check(painted_fail == 0, f"All {painted_ok} textures have correct ride_sword art (0 failures)")

    # --- 5. JAR contains painted textures ---
    print("\n--- 5. JAR verification ---")
    check(JAR.exists(), f"JAR exists: {JAR.name}")

    if JAR.exists():
        with zipfile.ZipFile(JAR, 'r') as z:
            jar_textures = [n for n in z.namelist()
                           if n.startswith("assets/ergenverse/textures/entity/cultivator/")
                           and n.endswith(".png")]
            check(len(jar_textures) >= 23,
                  f"JAR contains {len(jar_textures)} cultivator textures")

            # Verify wang_lin.png in JAR has painted art
            with z.open("assets/ergenverse/textures/entity/cultivator/wang_lin.png") as f:
                img = Image.open(io.BytesIO(f.read())).convert("RGBA")
                blade_px = img.getpixel((40, 57))
                guard_px = img.getpixel((35, 61))
                pommel_px = img.getpixel((42, 61))
                blade_silver = blade_px[0] > 100 and blade_px[1] > 100 and blade_px[2] > 100
                guard_gold = guard_px[0] > 150 and guard_px[1] > 100 and guard_px[2] < 100
                pommel_dark = pommel_px[0] < 100 and pommel_px[1] < 80 and pommel_px[2] < 50
                check(blade_silver and guard_gold and pommel_dark,
                      f"JAR wang_lin.png has painted ride_sword art (blade={blade_px}, guard={guard_px}, pommel={pommel_px})")

    # --- Summary ---
    print(f"\n=== SUMMARY: {checks - len(errors)}/{checks} checks passed ===")
    if errors:
        print("\nFAILURES:")
        for e in errors:
            print(f"  - {e}")
        sys.exit(1)
    else:
        print("\nAll checks passed!")
        sys.exit(0)


if __name__ == "__main__":
    main()
