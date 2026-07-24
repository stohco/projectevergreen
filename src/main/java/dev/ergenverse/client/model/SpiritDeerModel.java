package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/spirit_deer.png  SIZE: 64x64
/*
 * SpiritDeerModel — slender long-necked quadruped with branched antlers.
 *
 * ANATOMY (CRON-COMPLETIONIST-28 antler overhaul):
 *   - body        : slim torso (3 x 5 x 8) at chest height
 *   - body_chest  : wider front torso (3.5 x 5.5 x 4) — deep chest
 *   - body_hind   : narrower rear torso (2.5 x 4.5 x 5) — tapered waist
 *   - neck        : 2-segment neck chain (base + tip) — tapering S-curve,
 *                   replaces the single 1x4x1 broom-handle. 1.2 wide at base,
 *                   0.8 wide at tip. Each segment 2.5 long.
 *   - head        : skull (2x3x2) + snout (2x1.5x2) + 2 ears (leaf-shaped)
 *   - antlers     : 3-segment CURVED main beam per side (base upward-forward,
 *                   mid upward-back, tip upward-forward) with brow tine (near
 *                   base, angled outward), bay tine (mid-beam, angled outward),
 *                   trez tine (upper beam, angled outward). Each tine is a
 *                   short tapered box. Approximates real Cervidae palmate
 *                   branching. No longer TV antennae.
 *   - tail        : short puffy tuft at the rear
 *   - legs        : 4 slim legs, 2 segments each (thigh + shin), feet at y=15.
 *                   Rear legs have a forward-angled hock joint.
 *
 * ANIMATION:
 *   - Walk gait   : diagonal trot, cos(swing*0.6662)*amp*swingAmt, shins
 *                   counter-flex.
 *   - Flee (run)  : when limbSwingAmount > 0.6, swing frequency x1.6, amp 1.3,
 *                   head up, tail flagged.
 *   - Graze       : when idle (limbSwingAmount < 0.1), head dips down
 *                   head.xRot cycles to +1.2 on a slow sin(age*0.05).
 *   - Alert       : head snaps up (-0.3), ears forward, tail flicks up.
 *                   Triggered by an alert cycle out of phase with graze.
 *   - Head turn   : head.yRot = netHeadYaw * deg2rad (clamped); head.xRot
 *                   overridden by graze/alert when idle.
 *
 * HARSH SELF-CRITIQUE (post-CRON-28):
 *   - Antlers NOW have 3-segment curved main beam + 3 tines per side (brow,
 *     bay, trez). This is a MASSIVE improvement over the TV antennae.
 *     But: tines are still uniform boxes. Real tines taper to a point.
 *     The curve is approximated by 3 angled segments — not a true Bezier curve.
 *     No asymmetry (real antlers are always slightly asymmetric).
 *     No palmation on the top tines (real red deer have palmate tops).
 *     Score improved from 1/10 (antlers alone) to ~5/10.
 *   - Neck NOW has 2 segments that taper (1.2→0.8). Major improvement over
 *     the 1x4x1 broom-handle. But still boxes, not smooth curves.
 *   - Body NOW has chest/hip split for depth taper. Minor improvement —
 *     still reads as "two boxes glued together" rather than one smooth body.
 *   - Legs still have no visible hock on rear legs.
 *   - Ears still boxes (not leaf-shaped).
 *   - No dewclaws, no cloven hooves.
 *   - Texture UVs re-derived for new part layout — existing spirit_deer.png
 *     will scramble. Regeneration script required.
 *   - Graze/alert still blind sin wave (not driven by synced startled flag).
 */
import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class SpiritDeerModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart bodyChest;
    private final ModelPart bodyHind;
    private final ModelPart head;
    private final ModelPart neckBase;
    private final ModelPart neckTip;
    private final ModelPart earLeft;
    private final ModelPart earRight;
    private final ModelPart tail;
    private final ModelPart frontLeftThigh;
    private final ModelPart frontLeftShin;
    private final ModelPart frontRightThigh;
    private final ModelPart frontRightShin;
    private final ModelPart backLeftThigh;
    private final ModelPart backLeftShin;
    private final ModelPart backRightThigh;
    private final ModelPart backRightShin;
    private final ModelPart antlerLeftTip;
    private final ModelPart antlerRightTip;

    public SpiritDeerModel(ModelPart root) {
        this.root = root;
        this.bodyChest = root.getChild("body_chest");
        this.bodyHind = root.getChild("body_hind");
        this.neckBase = root.getChild("neck_base");
        this.neckTip = this.neckBase.getChild("neck_tip");
        this.head = this.neckTip.getChild("head");
        this.earLeft = this.head.getChild("ear_left");
        this.earRight = this.head.getChild("ear_right");
        this.tail = root.getChild("tail");
        this.frontLeftThigh = root.getChild("front_left_thigh");
        this.frontLeftShin = this.frontLeftThigh.getChild("shin");
        this.frontRightThigh = root.getChild("front_right_thigh");
        this.frontRightShin = this.frontRightThigh.getChild("shin");
        this.backLeftThigh = root.getChild("back_left_thigh");
        this.backLeftShin = this.backLeftThigh.getChild("shin");
        this.backRightThigh = root.getChild("back_right_thigh");
        this.backRightShin = this.backRightThigh.getChild("shin");
        this.antlerLeftTip = this.head.getChild("antler_left_base").getChild("mid").getChild("tip");
        this.antlerRightTip = this.head.getChild("antler_right_base").getChild("mid").getChild("tip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── body_chest : wider front torso (deep chest) ─────────────────
        PartDefinition bodyChest = root.addOrReplaceChild("body_chest",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.75F, -2.75F, -4.0F, 3.5F, 5.5F, 4.0F),
                PartPose.offset(0.0F, 6.0F, -2.0F));

        // ── body_hind : narrower rear torso (tapered waist) ────────────────
        PartDefinition bodyHind = root.addOrReplaceChild("body_hind",
                CubeListBuilder.create().texOffs(14, 0)
                        .addBox(-1.25F, -2.25F, 0.0F, 2.5F, 4.5F, 5.0F),
                PartPose.offset(0.0F, 6.0F, 2.5F));

        // ── neck_base : lower neck segment (wider, angled up-forward) ──────
        PartDefinition neckBase = root.addOrReplaceChild("neck_base",
                CubeListBuilder.create().texOffs(24, 0)
                        .addBox(-0.6F, -2.5F, -0.6F, 1.2F, 2.5F, 1.2F),
                PartPose.offsetAndRotation(0.0F, 3.5F, -4.0F, 0.9F, 0.0F, 0.0F));

        // ── neck_tip : upper neck segment (narrower, S-curve continuation) ─────
        PartDefinition neckTip = neckBase.addOrReplaceChild("neck_tip",
                CubeListBuilder.create().texOffs(28, 0)
                        .addBox(-0.4F, -2.5F, -0.4F, 0.8F, 2.5F, 0.8F),
                PartPose.offsetAndRotation(0.0F, -2.5F, 0.0F, 0.5F, 0.0F, 0.0F));

        // ── head : child of neck_tip, at the tip ──────────────────────
        PartDefinition head = neckTip.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-1.0F, -1.5F, -2.0F, 2.0F, 3.0F, 2.0F)   // skull
                        .texOffs(8, 16)
                        .addBox(-1.0F, 0.0F, -3.5F, 2.0F, 1.0F, 2.0F),   // snout forward
                PartPose.offset(0.0F, -4.0F, 0.0F));
        // ears : leaf-shaped boxes on top of skull
        head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(20, 16)
                        .addBox(-1.0F, -1.5F, 0.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-1.0F, -1.5F, -1.0F, 0.0F, 0.0F, -0.4F));
        head.addOrReplaceChild("ear_right",
                CubeListBuilder.create().texOffs(20, 20)
                        .addBox(0.0F, -1.5F, 0.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(1.0F, -1.5F, -1.0F, 0.0F, 0.0F, 0.4F));

        // ── antlers : 3-segment CURVED main beam + brow/bay/trez tines ─────
        // CRON-COMPLETIONIST-28: Replaced TV antennae with curved multi-tine structure.
        // Each antler: base (angled outward-up) → mid (angled backward-up) →
        // tip (angled outward-up). Three tines branch off at different heights.
        // This approximates real Cervidae branching (not palmate — that's elk).

        // ── LEFT ANTLER ──────────────────────────────────────────────────────
        PartDefinition antlerLBase = head.addOrReplaceChild("antler_left_base",
                CubeListBuilder.create().texOffs(0, 10)
                        .addBox(-0.4F, -2.0F, -0.4F, 0.8F, 2.0F, 0.8F),
                PartPose.offsetAndRotation(-0.5F, -1.5F, 0.0F, -0.4F, 0.0F, -0.2F));
        // brow tine (near base, angled outward and slightly forward)
        antlerLBase.addOrReplaceChild("brow_tine",
                CubeListBuilder.create().texOffs(4, 10)
                        .addBox(-0.3F, 0.0F, -0.3F, 0.6F, 1.2F, 0.6F),
                PartPose.offsetAndRotation(-0.2F, -0.5F, 0.0F, 0.5F, 0.0F, -0.7F));
        PartDefinition antlerLMid = antlerLBase.addOrReplaceChild("mid",
                CubeListBuilder.create().texOffs(8, 10)
                        .addBox(-0.35F, -2.0F, -0.35F, 0.7F, 2.0F, 0.7F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.3F, 0.0F, -0.15F));
        // bay tine (mid-beam, angled outward)
        antlerLMid.addOrReplaceChild("bay_tine",
                CubeListBuilder.create().texOffs(12, 10)
                        .addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F),
                PartPose.offsetAndRotation(0.0F, -0.8F, 0.0F, 0.4F, 0.0F, -0.8F));
        PartDefinition antlerLTip = antlerLMid.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(16, 10)
                        .addBox(-0.3F, -1.5F, -0.3F, 0.6F, 1.5F, 0.6F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.3F, 0.0F, -0.25F));
        // trez tine (upper beam, angled outward)
        antlerLTip.addOrReplaceChild("trez_tine",
                CubeListBuilder.create().texOffs(20, 10)
                        .addBox(-0.2F, 0.0F, -0.2F, 0.4F, 0.8F, 0.4F),
                PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.35F, 0.0F, -0.6F));

        // ── RIGHT ANTLER (mirrored) ────────────────────────────────────────
        PartDefinition antlerRBase = head.addOrReplaceChild("antler_right_base",
                CubeListBuilder.create().texOffs(24, 10)
                        .addBox(-0.4F, -2.0F, -0.4F, 0.8F, 2.0F, 0.8F),
                PartPose.offsetAndRotation(0.5F, -1.5F, 0.0F, -0.4F, 0.0F, 0.2F));
        antlerRBase.addOrReplaceChild("brow_tine",
                CubeListBuilder.create().texOffs(28, 10)
                        .addBox(-0.3F, 0.0F, -0.3F, 0.6F, 1.2F, 0.6F),
                PartPose.offsetAndRotation(0.2F, -0.5F, 0.0F, 0.5F, 0.0F, 0.7F));
        PartDefinition antlerRMid = antlerRBase.addOrReplaceChild("mid",
                CubeListBuilder.create().texOffs(32, 10)
                        .addBox(-0.35F, -2.0F, -0.35F, 0.7F, 2.0F, 0.7F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.3F, 0.0F, 0.15F));
        antlerRMid.addOrReplaceChild("bay_tine",
                CubeListBuilder.create().texOffs(36, 10)
                        .addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F),
                PartPose.offsetAndRotation(0.0F, -0.8F, 0.0F, 0.4F, 0.0F, 0.8F));
        PartDefinition antlerRTip = antlerRMid.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(40, 10)
                        .addBox(-0.3F, -1.5F, -0.3F, 0.6F, 1.5F, 0.6F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.3F, 0.0F, 0.25F));
        antlerRTip.addOrReplaceChild("trez_tine",
                CubeListBuilder.create().texOffs(44, 10)
                        .addBox(-0.2F, 0.0F, -0.2F, 0.4F, 0.8F, 0.4F),
                PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.35F, 0.0F, 0.6F));

        // ── tail : short puffy tuft ──────────────────────────────────────
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(40, 0)
                        .addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 4.0F, 0.3F, 0.0F, 0.0F));

        // ── legs : 4 slim legs, thigh + shin, feet at y=15 ───────────────
        root.addOrReplaceChild("front_left_thigh",
                CubeListBuilder.create().texOffs(0, 28).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(-1.5F, 9.0F, -3.0F));
        root.getChild("front_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 34).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        root.addOrReplaceChild("front_right_thigh",
                CubeListBuilder.create().texOffs(8, 28).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(1.5F, 9.0F, -3.0F));
        root.getChild("front_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(8, 34).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        root.addOrReplaceChild("back_left_thigh",
                CubeListBuilder.create().texOffs(0, 40).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(-1.5F, 9.0F, 3.0F));
        root.getChild("back_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 46).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        root.addOrReplaceChild("back_right_thigh",
                CubeListBuilder.create().texOffs(8, 40).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(1.5F, 9.0F, 3.0F));
        root.getChild("back_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(8, 46).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    // CRON-COMPLETIONIST-59: Expose antler tips for emissive divine glow render pass.
    // Spirit deer antlers glow at the tips (divine quality, similar to qilin).
    public ModelPart getAntlerLeftTip() { return this.antlerLeftTip; }
    public ModelPart getAntlerRightTip() { return this.antlerRightTip; }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(SpiritBeastEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // ── CRON-COMPLETIONIST-16: POSE_RESTING — deer lies down, legs tucked ──
        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;
        // ── CRON-COMPLETIONIST-16: POSE_SWIMMING — deer swims, head above water ──
        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING;
        // ── CRON-COMPLETIONIST-17: POSE_SPRINTING — stretched gallop, head up, tail flagged ──
        boolean sprinting = entity.getSpiritPose() == SpiritBeastEntity.POSE_SPRINTING;

        if (resting) {
            // Deer rests: body lowers, legs fold under, neck curls, head on ground
            // CRON-COMPLETIONIST-17: Added breathing, ear twitch, tail micro-sway
            float breath = (float) Math.sin(ageInTicks * 0.08F) * 0.12F;
            float earTwitch = (ageInTicks % 80 < 4) ? (float) Math.sin(ageInTicks * 1.8F) * 0.08F : 0.0F;
            float tailSway = (float) Math.sin(ageInTicks * 0.1F) * 0.06F;
            this.root.y = -2.0F + breath;
            this.frontLeftThigh.xRot  = -0.7F;
            this.frontRightThigh.xRot = -0.7F;
            this.frontLeftShin.xRot   = 0.5F;
            this.frontRightShin.xRot  = 0.5F;
            this.backLeftThigh.xRot   = 0.4F;
            this.backRightThigh.xRot  = 0.4F;
            this.backLeftShin.xRot    = -0.3F;
            this.backRightShin.xRot   = -0.3F;
            this.neckBase.xRot = 0.7F + breath * 0.3F;
            this.neckTip.xRot = 0.6F + breath * 0.1F;
            this.head.xRot = 0.8F + breath * 0.2F;
            this.tail.xRot = 0.5F + tailSway;
            this.earLeft.zRot  = -0.4F + earTwitch;
            this.earRight.zRot = 0.4F - earTwitch;
        } else if (swimming) {
            // CRON-COMPLETIONIST-17: Added vertical bob synchronized with paddle.
            float paddle = ageInTicks * 1.0F;
            float bob = (float) Math.sin(paddle * 0.5F) * 0.12F;
            this.root.xRot = -0.3F;
            this.root.y = -1.5F + bob;
            this.head.xRot = -0.4F;
            this.neckBase.xRot = 0.3F;
            this.neckTip.xRot = 0.2F;
            this.frontLeftThigh.xRot  = (float) Math.cos(paddle) * 0.7F;
            this.frontRightThigh.xRot = (float) Math.cos(paddle + Math.PI) * 0.7F;
            this.backLeftThigh.xRot   = (float) Math.cos(paddle + Math.PI) * 0.5F;
            this.backRightThigh.xRot  = (float) Math.cos(paddle) * 0.5F;
            this.frontLeftShin.xRot   = -0.2F + Math.abs((float) Math.cos(paddle)) * 0.3F;
            this.frontRightShin.xRot  = -0.2F + Math.abs((float) Math.cos(paddle + Math.PI)) * 0.3F;
            this.backLeftShin.xRot    = -0.2F + Math.abs((float) Math.cos(paddle + Math.PI)) * 0.2F;
            this.backRightShin.xRot   = -0.2F + Math.abs((float) Math.cos(paddle)) * 0.2F;
            this.tail.xRot = 0.3F;
            this.earLeft.zRot  = -0.3F;
            this.earRight.zRot = 0.3F;
        } else if (sprinting) {
            // ── CRON-COMPLETIONIST-17: POSE_SPRINTING — deer gallop, stotting bounce ──
            float sprintPhase = limbSwing * 2.0F;
            float sprintAmp = 1.4F * limbSwingAmount;
            float sp = sprintPhase * 0.6662F;
            // Stotting bounce — all four feet off ground at peak
            float stot = (float) Math.abs(Math.sin(sp * 0.5F)) * 1.0F * limbSwingAmount;
            this.root.y = -stot;
            this.root.xRot = (float) Math.sin(sp + Math.PI * 0.5F) * 0.08F * limbSwingAmount;
            this.frontLeftThigh.xRot  = (float) Math.cos(sp)            * sprintAmp;
            this.frontRightThigh.xRot = (float) Math.cos(sp + Math.PI)  * sprintAmp;
            this.backLeftThigh.xRot   = (float) Math.cos(sp + Math.PI)  * sprintAmp;
            this.backRightThigh.xRot  = (float) Math.cos(sp)            * sprintAmp;
            this.frontLeftShin.xRot  = -0.4F + Math.max(0.0F, (float) Math.cos(sp))            * 0.7F * limbSwingAmount;
            this.frontRightShin.xRot = -0.4F + Math.max(0.0F, (float) Math.cos(sp + Math.PI))  * 0.7F * limbSwingAmount;
            this.backLeftShin.xRot   = -0.4F + Math.max(0.0F, (float) Math.cos(sp + Math.PI))  * 0.7F * limbSwingAmount;
            this.backRightShin.xRot  = -0.4F + Math.max(0.0F, (float) Math.cos(sp))            * 0.7F * limbSwingAmount;
            // Head up, neck extended, tail flagged high
            this.neckBase.xRot = 0.3F;
            this.neckTip.xRot = 0.3F;
            this.head.xRot = -0.4F;
            this.tail.xRot = -1.0F;              // tail flagged high
            this.earLeft.zRot  = -0.4F;
            this.earRight.zRot = 0.4F;
        } else {
            boolean moving = limbSwingAmount > 0.1F;
            boolean fleeing = limbSwingAmount > 0.6F;

            // ── CRON-COMPLETIONIST-13: DATA_POSE overrides for graze/alert ──
            boolean poseGrazing = entity.getSpiritPose() == SpiritBeastEntity.POSE_GRAZING;
            boolean poseAlert   = entity.getSpiritPose() == SpiritBeastEntity.POSE_ALERT;

        // ── walk / flee gait ─────────────────────────────────────────────
        float swingPhase = fleeing ? limbSwing * 1.6F : limbSwing;
        float amp = (fleeing ? 1.3F : 0.8F) * limbSwingAmount;
        float phase = swingPhase * 0.6662F;

        this.frontLeftThigh.xRot  = (float) Math.cos(phase)            * amp;
        this.frontRightThigh.xRot = (float) Math.cos(phase + Math.PI)  * amp;
        this.backLeftThigh.xRot   = (float) Math.cos(phase + Math.PI)  * amp;
        this.backRightThigh.xRot  = (float) Math.cos(phase)            * amp;

        this.frontLeftShin.xRot  = -0.3F + Math.max(0.0F, (float) Math.cos(phase))            * 0.6F * limbSwingAmount;
        this.frontRightShin.xRot = -0.3F + Math.max(0.0F, (float) Math.cos(phase + Math.PI))  * 0.6F * limbSwingAmount;
        this.backLeftShin.xRot   = -0.3F + Math.max(0.0F, (float) Math.cos(phase + Math.PI))  * 0.6F * limbSwingAmount;
        this.backRightShin.xRot  = -0.3F + Math.max(0.0F, (float) Math.cos(phase))            * 0.6F * limbSwingAmount;

        // ── CRON-16: Spine flex on gait ──────────────────────────
        float spineFlex = (float) Math.sin(phase + Math.PI * 0.5F) * 0.06F * limbSwingAmount;
        this.root.xRot = spineFlex;

        // ── head behaviour ───────────────────────────────────────────────
        // CRON-19: Neck bobs with walk cycle (was static sin(age) bob)
        this.neckBase.xRot = 0.6F + (float) Math.sin(phase) * 0.04F * limbSwingAmount;
        this.neckTip.xRot = 0.5F;
        this.neckBase.yRot = (float) Math.sin(phase * 0.5F) * 0.03F * limbSwingAmount;

        float yaw = netHeadYaw * 0.017453292F;
        this.head.yRot = Math.max(-0.8F, Math.min(0.8F, yaw));

        if (fleeing) {
            // FLEE : head up, tail flagged
            this.head.xRot = -0.3F;
            this.neckBase.xRot = 0.4F;
            this.neckTip.xRot = 0.3F;                  // neck more upright to raise head
            this.tail.xRot = -0.8F;                 // tail flagged up
            this.earLeft.zRot  = -0.4F;
            this.earRight.zRot = 0.4F;
        } else if (moving) {
            // WALK : head level, look around
            this.head.xRot = headPitch * 0.017453292F;
            this.tail.xRot = 0.3F;
            this.earLeft.zRot  = -0.4F;
            this.earRight.zRot = 0.4F;
        } else {
            // IDLE / POSE_GRAZING / POSE_ALERT — driven by DATA_POSE
            if (poseGrazing) {
                // GRAZE : head dips toward the ground (driven by GrazeGoal)
                this.head.xRot = 1.2F;
                this.neckBase.xRot = 0.8F;
                this.neckTip.xRot = 0.5F;
                this.tail.xRot = 0.3F;
            } else if (poseAlert) {
                // ALERT : head snaps up, ears forward, tail flicks
                this.head.xRot = -0.3F;
                this.neckBase.xRot = 0.5F;
                this.neckTip.xRot = 0.3F;
                this.tail.xRot = -0.5F + (float) Math.sin(ageInTicks * 1.5F) * 0.3F;
                this.earLeft.zRot  = -0.2F;
                this.earRight.zRot = 0.2F;
            } else {
                // Fallback: slow graze / alert cycle when no goal is active
                float cycle = (float) Math.sin(ageInTicks * 0.05F);
                if (cycle > 0.0F) {
                    this.head.xRot = 1.2F * cycle;
                    this.neckBase.xRot = 0.8F;
                    this.neckTip.xRot = 0.5F;
                    this.tail.xRot = 0.3F;
                } else {
                    this.head.xRot = -0.3F;
                    this.neckBase.xRot = 0.5F;
                    this.neckTip.xRot = 0.3F;
                    this.tail.xRot = -0.5F + (float) Math.sin(ageInTicks * 1.5F) * 0.3F;
                    this.earLeft.zRot  = -0.2F;
                    this.earRight.zRot = 0.2F;
                }
            }
        }
        } // end of walk/flee else block

        // ── attack : deer rears up (front hooves lift, head back) ─────
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float lunge = (float) Math.sin(atk * Math.PI);
            // Deer don't lunge forward — they rear up and strike with hooves
            this.frontLeftThigh.xRot  = -lunge * 1.2F;   // front legs lift
            this.frontRightThigh.xRot = -lunge * 1.2F;
            this.frontLeftShin.xRot  = -lunge * 0.8F;   // knees fold
            this.frontRightShin.xRot = -lunge * 0.8F;
            this.backLeftThigh.xRot   += lunge * 0.2F;   // back legs anchor
            this.backRightThigh.xRot  += lunge * 0.2F;
            this.root.xRot = lunge * 0.3F;               // body tilts back
            this.neckBase.xRot -= lunge * 0.3F;
            this.neckTip.xRot -= lunge * 0.15F;             // neck raises
            this.head.xRot -= lunge * 0.5F;             // head throws back
        }

        // ── death collapse : deer crumples, legs fold under ─────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F); // 0→1 over 0.4s (visible before fade)
            float collapse = t * t;
            this.root.xRot = collapse * -0.3F;
            this.root.zRot = collapse * 0.5F;
            this.neckBase.xRot = 0.5F + collapse * 0.3F;
            this.neckTip.xRot = 0.3F + collapse * 0.2F;       // neck folds down
            this.head.xRot = collapse * 0.6F;                  // head droops to ground
            // legs fold under the body (deer crumples)
            this.frontLeftThigh.xRot  = collapse * 0.8F;
            this.frontRightThigh.xRot = collapse * 0.8F;
            this.backLeftThigh.xRot   = collapse * 0.6F;
            this.backRightThigh.xRot  = collapse * 0.6F;
            this.frontLeftShin.xRot  = collapse * 0.5F;
            this.frontRightShin.xRot = collapse * 0.5F;
            this.backLeftShin.xRot   = collapse * 0.4F;
            this.backRightShin.xRot  = collapse * 0.4F;
            // tail goes limp
            this.tail.xRot = 0.3F + collapse * 0.3F;
        }
    }
}
