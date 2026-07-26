package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/spirit_bat.png  SIZE: 64x64
/*
 * SpiritBatModel v4 — CRON-COMPLETIONIST-83: wingspan fix + hierarchy fix.
 *
 * v3 PROBLEMS (identified by CRON-83 artwork critique):
 *   1. Wingspan:body ratio was ~7:1 (should be ~2.5-3:1 for real bats).
 *      Each wing was 8px of bone chain vs 5px body — cartoonishly oversized.
 *   2. Wings were parented under root, not thorax — body roll didn't
 *      move the wings (floating beside body, not attached).
 *   3. ZERO CubeDeformation on wing bones — they were sharp-edged sticks.
 *
 * v4 FIXES:
 *   - Reduced bone chain from 2+3+3=8px to 1.5+2+2=5.5px per side.
 *     Wingspan:body ratio is now ~14:5 = 2.8:1 (realistic for microchiroptera).
 *   - Web panels reduced proportionally (3.5→2.0, 3.0→1.5, 2.5→1.0).
 *   - Wing roots now children of thorax, not root. Body roll moves wings.
 *   - Added CubeDeformation(0.1F) to all wing bones for subtle rounding.
 *   - Legs now children of abdomen (correct anatomy — legs hang from rear).
 *
 * CRON-COMPLETIONIST-87 (v5): reparented left_leg, right_leg, uropatagium from
 *   root to abdomen. v4 attempted this but reverted due to a misunderstood
 *   scoping concern (the comment claimed 'Java resolves the local abdomen to
 *   the instance field' — this is WRONG; in a static method, local variables
 *   shadow instance fields, exactly as CRON-86's Tiger/Wolf fix demonstrated).
 *   v5 completes the abdomen hierarchy. World-coordinate-preservation invariant:
 *   new_offset = old_root_offset - abdomen_root_offset where abdomen_root = (0, 10.25, 1.25).
 *     - left_leg:    (-0.6, 11.25, 0.0)  → (-0.6, 1.0, -1.25)
 *     - right_leg:   ( 0.6, 11.25, 0.0)  → ( 0.6, 1.0, -1.25)
 *     - uropatagium: ( 0.0, 11.25, 0.5)  → ( 0.0, 1.0, -0.75)
 *   Now legs + uropatagium follow abdomen (and thus thorax) rotations — previously
 *   they were frozen in root space and didn't follow body roll/pitch. Closes the
 *   parent-hierarchy defect class for SpiritBat (11 of 12 models done; only SoulFish
 *   remains, fixed in the same CRON-87 round).
 *
 * ANATOMY (v4):
 *   - thorax: 2.5x2.5x2.5, abdomen: 3x2x2.5, head: 2x2x2
 *   - ears: 0.6x2.5x0.6 with inner detail, nose leaf
 *   - LEFT WING (4-segment chain, child of thorax):
 *     - shoulder: 1.5x0.5x1.2 (reduced from 2x0.5x1.5)
 *     - elbow: 2.0x0.4x1.0 at offset -1.5 (reduced from 3.0 at -2.0)
 *     - finger: 2.0x0.3x0.8 at offset -2.0 (reduced from 3.0 at -3.0)
 *     - web_proximal: 2.0x0.1x2.2 (reduced from 3.5x0.1x3.0)
 *     - web_mid: 1.5x0.1x1.8 (reduced from 3.0x0.1x2.5)
 *     - web_distal: 1.0x0.1x1.4 (reduced from 2.5x0.1x1.8)
 *     - thumb_claw: retained
 *   - RIGHT WING: mirror
 *   - legs: children of abdomen (fixed attachment)
 *   - uropatagium: child of abdomen
 *
 * HARSH SELF-CRITIQUE (v4):
 *   - Wingspan ratio fixed from 7:1 to 2.8:1 — now reads as a bat, not
 *     a mouse with blankets. Score: 5/10 → 6/10.
 *   - Hierarchy fixed — wings move with body. Score: +0.5.
 *   - Wing bone CubeDeformation is minimal (0.1F) — barely visible.
 *   - Web panels are still flat boxes — fundamentally limited by MC box model.
 *   - Uropatagium still single box — needs 2-3 panel drape.
 *   - Ears still box prisms with no pinna detail.
 *   - Honest score: 6/10. The proportions are now correct; the detail is
 *     still limited by Minecraft's box-based geometry.
 */
import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class SpiritBatModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart thorax;
    private final ModelPart abdomen;
    private final ModelPart head;
    private final ModelPart earLeft;
    private final ModelPart earInnerLeft;
    private final ModelPart earRight;
    private final ModelPart earInnerRight;
    private final ModelPart leftShoulder;
    private final ModelPart leftElbow;
    private final ModelPart leftFinger;
    private final ModelPart leftWebProximal;
    private final ModelPart leftWebMid;
    private final ModelPart leftWebDistal;
    private final ModelPart leftThumbClaw;
    private final ModelPart rightShoulder;
    private final ModelPart rightElbow;
    private final ModelPart rightFinger;
    private final ModelPart rightWebProximal;
    private final ModelPart rightWebMid;
    private final ModelPart rightWebDistal;
    private final ModelPart rightThumbClaw;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart uropatagium;

    public SpiritBatModel(ModelPart root) {
        this.root = root;
        this.thorax = root.getChild("thorax");
        this.abdomen = this.thorax.getChild("abdomen");
        this.head = this.thorax.getChild("head");
        this.earLeft = this.head.getChild("ear_left");
        this.earInnerLeft = this.earLeft.getChild("ear_inner");
        this.earRight = this.head.getChild("ear_right");
        this.earInnerRight = this.earRight.getChild("ear_inner");
        // v4: wings are now children of thorax (not root)
        this.leftShoulder = this.thorax.getChild("left_wing_root");
        this.leftElbow = this.leftShoulder.getChild("elbow");
        this.leftFinger = this.leftElbow.getChild("finger");
        this.leftWebProximal = this.leftFinger.getChild("web_proximal");
        this.leftWebMid = this.leftWebProximal.getChild("web_mid");
        this.leftWebDistal = this.leftWebMid.getChild("web_distal");
        this.leftThumbClaw = this.leftShoulder.getChild("thumb_claw");
        this.rightShoulder = this.thorax.getChild("right_wing_root");
        this.rightElbow = this.rightShoulder.getChild("elbow");
        this.rightFinger = this.rightElbow.getChild("finger");
        this.rightWebProximal = this.rightFinger.getChild("web_proximal");
        this.rightWebMid = this.rightWebProximal.getChild("web_mid");
        this.rightWebDistal = this.rightWebMid.getChild("web_distal");
        this.rightThumbClaw = this.rightShoulder.getChild("thumb_claw");
        // CRON-87: legs + uropatagium are now children of abdomen (correct anatomy —
        // legs hang from the rear of the torso, uropatagium drapes between them).
        // v4 left these as root children due to a misunderstood scoping concern;
        // the same static-method-local-shadows-instance-field pattern as CRON-86's
        // Tiger/Wolf fix works here too.
        this.leftLeg = this.abdomen.getChild("left_leg");
        this.rightLeg = this.abdomen.getChild("right_leg");
        this.uropatagium = this.abdomen.getChild("uropatagium");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        CubeDeformation bodyDeform = new CubeDeformation(0.5F);

        // ── thorax : compact front torso ────────────────────────────────
        PartDefinition thorax = root.addOrReplaceChild("thorax",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.25F, -1.25F, -1.25F, 2.5F, 2.5F, 2.5F, bodyDeform),
                PartPose.offset(0.0F, 10.0F, 0.0F));

        // ── abdomen : slightly wider rear torso ────────────────────────
        // CRON-87: captured as local variable so the static createBodyLayer() method
        // can reference it for legs/uropatagium reparenting (same pattern as CRON-86
        // Tiger/Wolf bodyChest/bodyHip local capture). The instance field this.abdomen
        // is set in the constructor via this.thorax.getChild("abdomen").
        PartDefinition abdomen = thorax.addOrReplaceChild("abdomen",
                CubeListBuilder.create().texOffs(10, 0)
                        .addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 2.5F, new CubeDeformation(0.4F)),
                PartPose.offset(0.0F, 0.25F, 1.25F));

        // ── head : rounded skull + ears ──────────────────────────────────
        PartDefinition head = thorax.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(6, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.3F)),
                PartPose.offset(0.0F, -1.5F, -2.0F));

        // Large pointed ears
        PartDefinition earL = head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(12, 0)
                        .addBox(-0.3F, -2.5F, -0.3F, 0.6F, 2.5F, 0.6F),
                PartPose.offsetAndRotation(-1.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.4F));
        earL.addOrReplaceChild("ear_inner",
                CubeListBuilder.create().texOffs(14, 0)
                        .addBox(-0.15F, -2.0F, -0.15F, 0.3F, 2.0F, 0.3F),
                PartPose.ZERO);

        PartDefinition earR = head.addOrReplaceChild("ear_right",
                CubeListBuilder.create().texOffs(12, 6)
                        .addBox(-0.3F, -2.5F, -0.3F, 0.6F, 2.5F, 0.6F),
                PartPose.offsetAndRotation(1.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.4F));
        earR.addOrReplaceChild("ear_inner",
                CubeListBuilder.create().texOffs(14, 6)
                        .addBox(-0.15F, -2.0F, -0.15F, 0.3F, 2.0F, 0.3F),
                PartPose.ZERO);

        // Tiny nose leaf
        head.addOrReplaceChild("nose_leaf",
                CubeListBuilder.create().texOffs(18, 0)
                        .addBox(-0.2F, 0.3F, -0.6F, 0.4F, 0.3F, 0.3F),
                PartPose.ZERO);

        // ── LEFT WING: 4-segment finger-bone chain (child of THORAX, not root)
        //    v4: reduced bone lengths for 2.8:1 wingspan:body ratio (was 7:1).
        CubeDeformation wingBoneDeform = new CubeDeformation(0.1F);

        PartDefinition leftRoot = thorax.addOrReplaceChild("left_wing_root",
                CubeListBuilder.create(),
                PartPose.offset(-1.25F, -0.5F, 0.0F));

        // Shoulder (humerus) — reduced from 2.0 to 1.5
        leftRoot.addOrReplaceChild("elbow_parent",
                CubeListBuilder.create().texOffs(0, 8)
                        .addBox(-1.5F, -0.25F, -0.6F, 1.5F, 0.5F, 1.2F, wingBoneDeform),
                PartPose.ZERO);
        // Thumb claw on leading edge
        leftRoot.addOrReplaceChild("thumb_claw",
                CubeListBuilder.create().texOffs(24, 8)
                        .addBox(-0.3F, -0.1F, -0.3F, 0.3F, 0.2F, 0.6F),
                PartPose.offset(-0.4F, 0.3F, -0.4F));

        // Elbow (radius/ulna) — reduced: bone 3.0→2.0, offset 2.0→1.5
        PartDefinition leftElbow = leftRoot.addOrReplaceChild("elbow",
                CubeListBuilder.create().texOffs(0, 12)
                        .addBox(-2.0F, -0.2F, -0.5F, 2.0F, 0.4F, 1.0F, wingBoneDeform),
                PartPose.offset(-1.5F, 0.0F, 0.0F));

        // Finger (metacarpal) — reduced: bone 3.0→2.0, offset 3.0→2.0
        PartDefinition leftFinger = leftElbow.addOrReplaceChild("finger",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-2.0F, -0.15F, -0.4F, 2.0F, 0.3F, 0.8F, wingBoneDeform),
                PartPose.offset(-2.0F, 0.0F, 0.0F));

        // ── 3-panel membrane sail (reduced proportions) ──
        PartDefinition leftWebP = leftFinger.addOrReplaceChild("web_proximal",
                CubeListBuilder.create().texOffs(20, 0)
                        .addBox(-2.0F, 0.0F, -1.1F, 2.0F, 0.1F, 2.2F),
                PartPose.offsetAndRotation(-2.0F, 0.1F, 0.0F, 0.05F, 0.0F, 0.0F));
        leftWebP.addOrReplaceChild("web_mid",
                CubeListBuilder.create().texOffs(20, 4)
                        .addBox(-1.5F, 0.0F, -0.9F, 1.5F, 0.1F, 1.8F),
                PartPose.offsetAndRotation(-1.5F, 0.0F, 0.0F, 0.15F, 0.0F, 0.0F));
        leftWebP.getChild("web_mid").addOrReplaceChild("web_distal",
                CubeListBuilder.create().texOffs(20, 8)
                        .addBox(-1.0F, 0.0F, -0.7F, 1.0F, 0.1F, 1.4F),
                PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.30F, 0.0F, 0.0F));

        // ── RIGHT WING: mirror (child of THORAX, reduced proportions) ──
        PartDefinition rightRoot = thorax.addOrReplaceChild("right_wing_root",
                CubeListBuilder.create(),
                PartPose.offset(1.25F, -0.5F, 0.0F));

        rightRoot.addOrReplaceChild("elbow_parent",
                CubeListBuilder.create().texOffs(30, 8)
                        .addBox(0.0F, -0.25F, -0.6F, 1.5F, 0.5F, 1.2F, wingBoneDeform),
                PartPose.ZERO);
        rightRoot.addOrReplaceChild("thumb_claw",
                CubeListBuilder.create().texOffs(40, 8)
                        .addBox(0.0F, -0.1F, -0.3F, 0.3F, 0.2F, 0.6F),
                PartPose.offset(0.4F, 0.3F, -0.4F));

        PartDefinition rightElbow = rightRoot.addOrReplaceChild("elbow",
                CubeListBuilder.create().texOffs(0, 20)
                        .addBox(0.0F, -0.2F, -0.5F, 2.0F, 0.4F, 1.0F, wingBoneDeform),
                PartPose.offset(1.5F, 0.0F, 0.0F));

        rightElbow.addOrReplaceChild("finger",
                CubeListBuilder.create().texOffs(0, 24)
                        .addBox(0.0F, -0.15F, -0.4F, 2.0F, 0.3F, 0.8F, wingBoneDeform),
                PartPose.offset(2.0F, 0.0F, 0.0F));

        // ── 3-panel membrane sail: RIGHT wing (reduced proportions) ──
        PartDefinition rightWebP = rightElbow.getChild("finger").addOrReplaceChild("web_proximal",
                CubeListBuilder.create().texOffs(32, 0)
                        .addBox(0.0F, 0.0F, -1.1F, 2.0F, 0.1F, 2.2F),
                PartPose.offsetAndRotation(2.0F, 0.1F, 0.0F, -0.05F, 0.0F, 0.0F));
        rightWebP.addOrReplaceChild("web_mid",
                CubeListBuilder.create().texOffs(32, 4)
                        .addBox(0.0F, 0.0F, -0.9F, 1.5F, 0.1F, 1.8F),
                PartPose.offsetAndRotation(1.5F, 0.0F, 0.0F, -0.15F, 0.0F, 0.0F));
        rightWebP.getChild("web_mid").addOrReplaceChild("web_distal",
                CubeListBuilder.create().texOffs(32, 8)
                        .addBox(0.0F, 0.0F, -0.7F, 1.0F, 0.1F, 1.4F),
                PartPose.offsetAndRotation(1.0F, 0.0F, 0.0F, -0.30F, 0.0F, 0.0F));

        // ── legs : short, hanging from abdomen bottom ─────────────────────
        //    CRON-87: reparented from root to abdomen. New offsets preserve world
        //    position: abdomen_root=(0, 10.25, 1.25), so leg offset (0.6, 1.0, -1.25)
        //    places the leg at world (0.6, 11.25, 0.0) — identical to v4. The legs
        //    now follow abdomen (and thorax) rotations instead of being frozen
        //    in root space.
        abdomen.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(16, 0)
                        .addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.5F, 0.5F),
                PartPose.offset(-0.6F, 1.0F, -1.25F));
        abdomen.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(16, 3)
                        .addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.5F, 0.5F),
                PartPose.offset(0.6F, 1.0F, -1.25F));

        // ── uropatagium : tail membrane draping between legs ────────────────
        //    CRON-87: reparented from root to abdomen. New offset (0, 1.0, -0.75)
        //    preserves world position (0, 11.25, 0.5).
        abdomen.addOrReplaceChild("uropatagium",
                CubeListBuilder.create().texOffs(18, 8)
                        .addBox(-0.8F, 0.0F, -0.5F, 1.6F, 0.1F, 1.0F),
                PartPose.offset(0.0F, 1.0F, -0.75F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() { return this.root; }

    /** Expose ears for emissive qi-glow rendering. */
    public ModelPart getEarLeft() { return this.earLeft; }
    public ModelPart getEarRight() { return this.earRight; }

    @Override
    public void setupAnim(SpiritBeastEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // ── head turn (clamped) ──────────────────────────────────────────
        float yaw = netHeadYaw * 0.017453292F;
        float pitch = headPitch * 0.017453292F;
        this.head.yRot = Math.max(-1.0F, Math.min(1.0F, yaw));
        this.head.xRot = Math.max(-0.7F, Math.min(0.7F, pitch));

        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;

        if (resting) {
            float breath = (float) Math.sin(ageInTicks * 0.08F) * 0.05F;
            this.root.y = 2.0F + breath;
            this.root.xRot = 3.14F; // FLIPPED
            // Wings wrap body (now above since flipped)
            this.leftShoulder.zRot = 0.8F;
            this.rightShoulder.zRot = -0.8F;
            this.leftShoulder.xRot = -0.3F;
            this.rightShoulder.xRot = 0.3F;
            this.leftElbow.zRot = 0.5F;
            this.rightElbow.zRot = -0.5F;
            this.leftFinger.zRot = 0.3F;
            this.rightFinger.zRot = -0.3F;
            // Ears twitch occasionally
            float earTwitch = (ageInTicks % 80 < 4) ? (float) Math.sin(ageInTicks * 2.5F) * 0.15F : 0.0F;
            this.earLeft.zRot = -0.4F + earTwitch;
            this.earRight.zRot = 0.4F - earTwitch;
            this.leftLeg.xRot = 0.0F;
            this.rightLeg.xRot = 0.0F;
            // Uropatagium drapes between legs when roosting
            this.uropatagium.xRot = -0.3F;
        } else {
            // ── FLIGHT : 4-segment wing chain flap ────────────────────
            float flapSpeed = ageInTicks * 1.2F;
            float flapAmp = 0.4F + limbSwingAmount * 0.8F;
            float flap = (float) Math.sin(flapSpeed) * flapAmp;

            // Shoulder: primary flap driver
            this.leftShoulder.zRot = flap;
            this.rightShoulder.zRot = -flap;

            // Elbow: phase-delayed (0.3 rad behind shoulder)
            float elbowFlex = (float) Math.sin(flapSpeed - 0.3F) * flapAmp * 0.7F;
            this.leftElbow.zRot = -elbowFlex;
            this.rightElbow.zRot = elbowFlex;

            // Finger: further phase delay (0.6 rad)
            float fingerFlex = (float) Math.sin(flapSpeed - 0.6F) * flapAmp * 0.5F;
            this.leftFinger.zRot = -fingerFlex;
            this.rightFinger.zRot = fingerFlex;

            // 3-panel membrane billow: cascading phase delay (0.9 → 1.1 → 1.3)
            float webBillow = Math.max(0.0F, (float) Math.sin(flapSpeed - 0.9F));
            float webMidBillow = Math.max(0.0F, (float) Math.sin(flapSpeed - 1.1F));
            float webDistBillow = Math.max(0.0F, (float) Math.sin(flapSpeed - 1.3F));
            // Proximal panel: subtle billow
            this.leftWebProximal.xScale = 1.0F + webBillow * 0.25F * limbSwingAmount;
            this.rightWebProximal.xScale = 1.0F + webBillow * 0.25F * limbSwingAmount;
            // Mid panel: medium billow
            this.leftWebMid.xScale = 1.0F + webMidBillow * 0.35F * limbSwingAmount;
            this.rightWebMid.xScale = 1.0F + webMidBillow * 0.35F * limbSwingAmount;
            // Distal panel: strongest billow (tip catches most air)
            this.leftWebDistal.xScale = 1.0F + webDistBillow * 0.45F * limbSwingAmount;
            this.rightWebDistal.xScale = 1.0F + webDistBillow * 0.45F * limbSwingAmount;

            // Thumb claws track shoulder but slightly delayed
            this.leftThumbClaw.zRot = (float) Math.sin(flapSpeed - 0.15F) * flapAmp * 0.3F;
            this.rightThumbClaw.zRot = -(float) Math.sin(flapSpeed - 0.15F) * flapAmp * 0.3F;

            // Legs tucked during flight
            this.leftLeg.xRot = -0.3F * limbSwingAmount;
            this.rightLeg.xRot = -0.3F * limbSwingAmount;

            // Idle breathing
            this.root.y = (float) Math.sin(ageInTicks * 0.15F) * 0.05F;

            // Ear twitch
            float earTwitch = (ageInTicks % 60 < 3) ? (float) Math.sin(ageInTicks * 2.0F) * 0.1F : 0.0F;
            this.earLeft.zRot = -0.2F + earTwitch;
            this.earRight.zRot = 0.2F - earTwitch;

            // Uropatagium streams behind
            this.uropatagium.xRot = 0.2F * limbSwingAmount;
        }

        // ── attack : bat swoop-dive ───────────────────────────────────
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float strike = (float) Math.sin(atk * Math.PI);
            this.root.xRot = resting ? 3.14F : strike * 0.4F;
            this.leftShoulder.zRot = resting ? 0.8F : -0.1F - strike * 0.6F;
            this.rightShoulder.zRot = resting ? -0.8F : 0.1F + strike * 0.6F;
            this.leftElbow.zRot = resting ? 0.5F : -strike * 0.3F;
            this.rightElbow.zRot = resting ? -0.5F : strike * 0.3F;
            this.leftFinger.zRot = resting ? 0.3F : -strike * 0.2F;
            this.rightFinger.zRot = resting ? -0.3F : strike * 0.2F;
            this.leftLeg.xRot = resting ? 0.0F : strike * 0.8F;
            this.rightLeg.xRot = resting ? 0.0F : strike * 0.8F;
        }

        // ── death : wings go limp, body tumbles ─────────────────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F);
            float collapse = t * t;
            if (!resting) {
                this.root.xRot = collapse * 0.5F;
                this.root.zRot = collapse * 0.8F;
            }
            this.leftShoulder.zRot = collapse * 1.2F;
            this.rightShoulder.zRot = -collapse * 1.2F;
            this.leftElbow.zRot = collapse * 0.6F;
            this.rightElbow.zRot = -collapse * 0.6F;
            this.leftFinger.zRot = collapse * 0.4F;
            this.rightFinger.zRot = -collapse * 0.4F;
            this.leftLeg.xRot = collapse * 0.5F;
            this.rightLeg.xRot = collapse * 0.5F;
            this.head.xRot = collapse * 0.6F;
        }
    }
}
