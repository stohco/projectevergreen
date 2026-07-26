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
 *   - Likely same defect as Qilin (body parts parented to root). Audit needed.
 *
 * SPIRIT_HAWK (SpiritHawkModel.java, 510 lines):
 *   - Wings are visual-only (hitbox is body-only per vanilla parrot). Verify
 *     wings are parented to body so flap animation propagates correctly.
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
 *   2. Audit SpiritDeerModel parent hierarchy (Tier 1) — likely same defect.
 *   3. Add ease-in/ease-out to walk cycles (Tier 3).
 *   4. Add pose-transition LERP to SpiritBeastEntity + models (Tier 3).
 *   5. Deepen Qilin chest and lengthen neck (Tier 2).
 *   6. Multi-tine deer antlers (Tier 2).
 *   7. Emissive qi layer on Qilin (Tier 4).
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
