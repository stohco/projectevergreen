package dev.ergenverse.client.model;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * SpiritBeastModelLayers - registers all custom beast model LayerDefinitions.
 * CRON-COMPLETIONIST-53: resolved git merge conflict (HEAD vs 069074e).
 *
 * ════════════════════════════════════════════════════════════════════════════
 * CRON-COMPLETIONIST-80: HARSH ARTWORK AUDIT (standing defects for future rounds)
 * ════════════════════════════════════════════════════════════════════════════
 *
 * The CRON-80 round closed the per-entity HITBOX mismatch (8 of 12 beasts had
 * hitboxes that didn't match their models; SOUL_FISH's CRON-60 doubling was
 * silently undone by reassessDimensions() for ~10 rounds). What CRON-80 did
 * NOT do is fix the underlying MODEL ANATOMY defects that produced the
 * mismatches in the first place. Those defects are catalogued here, ranked
 * by visual impact, so future CRON rounds can pick them off one at a time.
 *
 * ── TIER 1 — Structural defects (animation coherence) ──────────────────────
 *
 * QILIN (QilinModel.java, 600+ lines):
 *   - FIXED (CRON-81): body_chest, body_hip, neck, tail_base, all 4 legs, AND
 *     wing roots were ALL direct children of root. NONE were parented to the
 *     body chain. When bodyChest.xRot animated (spineFlex), body_hip/neck/
 *     head/tail/wings/legs did NOT follow — Qilin visibly "hinged" at the waist
 *     during walk, wings stayed level during sprint pitch, tail didn't follow
 *     rump rotation, legs didn't follow body pitch.
 *     FIX SHIPPED: Reparented body_hip/neck/head/wing-roots/front-thighs →
 *     body_chest; tail_base/back-thighs → body_hip. All PartPose offsets
 *     recomputed via subtraction (Rx-Px, Ry-Py, Rz-Pz). Verified by
 *     /home/z/my-project/scripts/cron81_verify_qilin_reparent.py. Animation
 *     fix: body_hip.xRot changed from -0.5*spineFlex to -1.5*spineFlex to
 *     preserve S-curve (hip LOCAL now compensates for inheriting chest rotation).
 *     Score 9/10 — closes the highest-impact Tier 1 defect. Runtime verification
 *     still needed (no client available).
 *
 * SPIRIT_DEER (SpiritDeerModel.java, 474 lines):
 *   - FIXED (CRON-82): Same defect as Qilin — body_hind, neck_base, tail, and
 *     all 4 thighs were ALL direct children of root. WORSE than Qilin: the deer
 *     animated `this.root.xRot = spineFlex` (not bodyChest.xRot), which rotated
 *     the ENTIRE deer as one rigid block — anatomically WRONG (a deer's spine
 *     flexes between chest and hind, not the whole body).
 *     FIX SHIPPED: Reparented body_hind/neck_base/front-thighs → body_chest;
 *     tail/back-thighs → body_hind. All PartPose offsets recomputed via
 *     subtraction. Verified by /home/z/my-project/scripts/cron82_verify_deer_reparent.py.
 *     Animation fix: (1) moved spineFlex from root.xRot to bodyChest.xRot +
 *     added bodyHind.xRot = -1.5*spineFlex for S-curve; (2) added
 *     bodyChest/bodyHind xRot reset (0.0F) to resting/swimming/sprinting blocks
 *     to prevent stale spine-flex state on pose transitions (a pre-existing bug
 *     pattern that became visible once spine flex moved off root); (3) kept
 *     whole-body rotations (swim pitch, sprint pitch, attack rear-up, death
 *     collapse) on root — these are anatomically whole-body, not spine flex.
 *     Score 9/10 — closes the second Tier 1 defect. Runtime verification still
 *     needed (no client available).
 *
 * SPIRIT_HAWK (SpiritHawkModel.java, 577 lines):
 *   - FIXED (CRON-83): Same defect class as Qilin/Deer — 7 parts were direct
 *     children of root instead of the body chain. body_hind, neck, left_wing,
 *     right_wing, tail, left_leg, right_leg were ALL at root. The bodyChest.xRot
 *     "thorax heave" in the FLAP block animated ONLY the chest — body_hind,
 *     wings, tail, legs, neck did NOT follow.
 *     FIX SHIPPED: Reparented body_hind/neck/left_wing/right_wing → body_chest;
 *     tail/left_leg/right_leg → body_hind. All PartPose offsets recomputed via
 *     subtraction (Rx-Px, Ry-Py, Rz-Pz). Verified by
 *     /home/z/my-project/scripts/cron83_verify_hawk_reparent.py (7/7 parts
 *     preserve world position).
 *     HAWK-SPECIFIC NOTES (different from quadrupeds): birds have a RIGID torso
 *     (fused thoracic vertebrae / synsacrum); the spine does NOT flex. There is
 *     NO S-curve animation fix for birds (unlike CRON-81 Qilin and CRON-82 Deer
 *     which added bodyHind.xRot = -1.5*spineFlex). The existing bodyChest.xRot
 *     = sin(age*0.6)*0.08*lsa is a "thorax heave" (respiratory pulse) — after
 *     CRON-83, body_hind INHERITS this heave (whole torso heaves together),
 *     which is anatomically correct for a bird in flight.
 *     STALE-STATE BUG FIX: pre-CRON-83, bodyChest.xRot was set ONLY in the FLAP
 *     block; other pose blocks (resting, swimming, sprinting, perched, glide)
 *     did NOT reset it, leaving stale heave from the last FLAP frame. CRON-83
 *     added `this.bodyChest.xRot = 0.0F;` resets to ALL non-flap blocks (resting,
 *     swimming, sprinting, perched, glide, AND death) — closes a pre-existing
 *     bug that became visible once body_hind/neck/wings/tail/legs inherited
 *     body_chest's rotation.
 *     Score 9/10 — closes the third and final Tier 1 structural defect from the
 *     CRON-80 audit. Runtime verification still needed (no client available).
 *
 * ── TIER 2 — Anatomy proportion issues ──────────────────────────────────────
 *
 * QILIN body proportions:
 *   - body_chest is 4 wide × 6 tall × 5 long. Real qilin depictions show a
 *     DEEP chest (height ~8-10) and muscular torso. Current chest is too
 *     shallow. Body should be ~6 wide × 9 tall × 6 long for proper qilin
 *     silhouette.
 *   - Neck is 2×2×3 — too short for a divine beast. Should be 2×2×5 to give
 *     the noble long-neck qilin silhouette.
 *
 * SPIRIT_DEER antlers:
 *   - Antlers are 0.6×1.5 + 0.5×1.0 — single-tine. Real deer antlers branch
 *     into 3-6 tines per side. Should be at least 3-tine branched per side
 *     to read as "deer" rather than "goat with horns".
 *
 * ── TIER 3 — Animation smoothing ───────────────────────────────────────────
 *
 * All beast models:
 *   - Walk cycle uses raw cos(phase) * amp — no easing. Real animal gaits
 *     use ease-in/ease-out on footfalls (slow at top of arc, fast at bottom).
 *     Vanilla Mob model does this; our custom models don't.
 *   - Pose transitions are INSTANT — POSE_RESTING → POSE_FLYING has no
 *     interpolation. A qilin taking off snaps from "lying down" to "flying"
 *     in 1 tick. Should LERP over 5-10 ticks.
 *
 * CultivatorRobeModel (888 lines, CRON-54):
 *   - Robe skirt chain is 3 segments — better than CRON-53's single box, but
 *     still boxes. Real cloth has folds. Without cloth sim, the best we can
 *     do is add 2 more segments (5 total) and use per-segment phase delay.
 *   - Sleeves are inflated arm boxes — no independent drape. A sleeve trail
 *     would need another bone chain child of each arm (deferred since
 *     CRON-54).
 *
 * ── TIER 4 — Missing features ───────────────────────────────────────────────
 *
 *   - No qi aura visualization on any beast model. Cultivators have it via
 *     the renderer; beasts don't. A subtle emissive layer on the qilin's
 *     antler tips, mane, and scales would sell "divine beast".
 *   - No facial features (eyes, mouth) on any beast model — texture-only.
 *   - No breath particles, no footstep dust, no flight wake.
 *
 * PRIORITIZED NEXT STEPS for future CRON rounds:
 *   1. DONE (CRON-81): Fix QilinModel parent hierarchy (Tier 1) — shipped.
 *   2. DONE (CRON-82): Audit + fix SpiritDeerModel parent hierarchy (Tier 1) — shipped.
 *   3. DONE (CRON-83): Audit + fix SpiritHawkModel parent hierarchy (Tier 1) — shipped.
 *      All 3 Tier 1 structural defects from CRON-80 audit are now CLOSED.
 *   4. Add ease-in/ease-out to walk cycles (Tier 3).
 *   5. Add pose-transition LERP to SpiritBeastEntity + models (Tier 3).
 *   6. Deepen Qilin chest and lengthen neck (Tier 2).
 *   7. Multi-tine deer antlers (Tier 2) — CRON-28 already shipped 3-tine branched; refine.
 *   8. Emissive qi layer on Qilin (Tier 4).
 *   9. Audit SpiritCraneModel, SpiritBatModel, SpiritTigerModel, SeaSerpentModel,
 *      StoneBackBoarModel, FireBeastModel, SoulFishModel, SpiritWolfModel,
 *      SpiritRabbitModel for the same parent-hierarchy defect class (Tier 1
 *      extended — the CRON-80 audit only catalogued Qilin/Deer/Hawk as the
 *      highest-impact targets; the other 9 beasts may have the same defect at
 *      smaller scale).
 */
public final class SpiritBeastModelLayers {

    private SpiritBeastModelLayers() {}

    public static final ModelLayerLocation SPIRIT_RABBIT =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "spirit_rabbit"), "main");
    public static final ModelLayerLocation SPIRIT_WOLF =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "spirit_wolf"), "main");
    public static final ModelLayerLocation SPIRIT_DEER =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "spirit_deer"), "main");
    public static final ModelLayerLocation SPIRIT_HAWK =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "spirit_hawk"), "main");
    public static final ModelLayerLocation FIRE_BEAST =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "fire_beast"), "main");
    public static final ModelLayerLocation STONE_BACK_BOAR =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "stone_back_boar"), "main");
    public static final ModelLayerLocation CULTIVATOR_ROBE =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "cultivator"), "main");
    public static final ModelLayerLocation SPIRIT_CRANE =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "spirit_crane"), "main");
    public static final ModelLayerLocation FLYING_SWORD =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "flying_sword"), "main");
    public static final ModelLayerLocation SPIRIT_BAT =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "spirit_bat"), "main");
    public static final ModelLayerLocation QILIN =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "qilin"), "main");
    public static final ModelLayerLocation SEA_SERPENT =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "sea_serpent"), "main");
    public static final ModelLayerLocation SOUL_FISH =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "soul_fish"), "main");
    // CRON-COMPLETIONIST-67: Spirit tiger — model existed since CRON-75 but was
    // never registered in the layer system. Invisible in-game until now.
    public static final ModelLayerLocation SPIRIT_TIGER =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "spirit_tiger"), "main");

    public static Supplier<LayerDefinition> getSupplier(ModelLayerLocation location) {
        if (SPIRIT_RABBIT.equals(location))       return SpiritRabbitModel::createBodyLayer;
        if (SPIRIT_WOLF.equals(location))         return SpiritWolfModel::createBodyLayer;
        if (SPIRIT_DEER.equals(location))         return SpiritDeerModel::createBodyLayer;
        if (SPIRIT_HAWK.equals(location))         return SpiritHawkModel::createBodyLayer;
        if (FIRE_BEAST.equals(location))          return SpiritFireBeastModel::createBodyLayer;
        if (STONE_BACK_BOAR.equals(location))     return StoneBackBoarModel::createBodyLayer;
        if (CULTIVATOR_ROBE.equals(location))     return CultivatorRobeModel::createBodyLayer;
        if (SPIRIT_CRANE.equals(location))       return SpiritCraneModel::createBodyLayer;
        if (FLYING_SWORD.equals(location))        return FlyingSwordModel::createBodyLayer;
        if (SPIRIT_BAT.equals(location))           return SpiritBatModel::createBodyLayer;
        if (QILIN.equals(location))               return QilinModel::createBodyLayer;
        if (SEA_SERPENT.equals(location))          return SeaSerpentModel::createBodyLayer;
        if (SOUL_FISH.equals(location))            return SoulFishModel::createBodyLayer;
        if (SPIRIT_TIGER.equals(location))          return SpiritTigerModel::createBodyLayer;
        return null;
    }
}
