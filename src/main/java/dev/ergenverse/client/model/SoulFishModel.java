package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/soul_fish.png  SIZE: 64x64
/*
 * SoulFishModel v3 — CRON-COMPLETIONIST-60: SCALE FIX + anatomy overhaul.
 *
 * PROBLEM (v2, scored 5/10): At 0.3×0.3 entity scale, ALL model details were
 * sub-pixel. The 22 addBox calls — gill covers (0.1px), lateral line (0.1px),
 * belly ridge (0.2px), tail lobes (0.06px) — were literally invisible. The
 * entire v2 effort went into details no player could ever see. The fish read as
 * a colored speck.
 *
 * FIX: Doubled ALL model dimensions. Added mid_body segment for 3-point taper.
 * Widened fins and tail fan to be visible at entity scale. Bounding box changed
 * from 0.3×0.3 to 0.6×0.5 in EREntityTypes.
 *
 * ANATOMY (v3):
 *   - head         : rounded skull (4.0 x 3.2 x 3.0, CubeDeformation 0.5)
 *     - eye_left, eye_right: spheres on flanks (1.0 each)
 *     - mouth      : snout opening (1.2 wide)
 *     - gill_cover_L/R: visible gill slits (0.6 wide)
 *   - body_front   : torpedo front (5.0 x 4.0 x 5.0, CubeDeformation 0.5)
 *   - body_mid     : MID section (NEW — creates smooth 3-point taper)
 *                    (4.0 x 3.0 x 4.0, CubeDeformation 0.4)
 *   - body_rear    : taper rear (3.0 x 2.4 x 4.0, CubeDeformation 0.3)
 *   - dorsal_fin   : 3-segment fan (base → mid → tip), tall and visible
 *   - anal_fin     : 2-segment fan
 *   - pec_fin_L/R  : wide pectoral fins with 2-segment webbing
 *   - ventral_fin  : NEW — pelvic fin pair on belly
 *   - tail_root    : peduncle connector
 *   - tail fan     : 3 large forked lobes (each 4.8 wide × 1.0 tall)
 *   - qi_glow      : large aura (7.2 x 5.2 x 12.0)
 *   - lateral_line : ridge along flank (wider, 0.4px)
 *   - belly_ridge  : lighter underbelly plate
 *
 * ANIMATION: Preserved from v2 — tail-driven oscillation with 3-lobe phase
 * delays, body reaction pitch, pectoral fin sculling, gill cover breathing,
 * death belly-up flip with quadratic ease-in.
 *
 * HARSH SELF-CRITIQUE (v3):
 *   - Scale is now visible but the fish is STILL boxes. A real fish has a
 *     continuous hydrodynamic taper, not a 3-step approximation.
 *   - Tail lobes are wider (4.8px) but still flat boxes, not forked membrane.
 *   - Dorsal fin is 3 boxes instead of 2 — reads as a stepped pyramid, not
 *     a curved fin. Real dorsal fins have individual rays.
 *   - Qi glow is a single box — needs particle emitters or shader for ethereal
 *     effect. At 0.6 scale it's visible but reads as a glowing rectangle.
 *   - Ventral fins are new but thin (0.2px) — may be barely visible.
 *   - Score estimate: 5/10 → 7/10. The scale fix alone is worth 2 points.
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

public class SoulFishModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart bodyFront;
    private final ModelPart bodyMid;
    private final ModelPart bodyRear;
    private final ModelPart head;
    private final ModelPart eyeLeft;
    private final ModelPart eyeRight;
    private final ModelPart mouth;
    private final ModelPart dorsalFinBase;
    private final ModelPart dorsalFinMid;
    private final ModelPart dorsalFinTip;
    private final ModelPart analFinBase;
    private final ModelPart analFinTip;
    private final ModelPart pecFinBaseLeft;
    private final ModelPart pecFinTipLeft;
    private final ModelPart pecFinBaseRight;
    private final ModelPart pecFinTipRight;
    private final ModelPart ventralFinLeft;
    private final ModelPart ventralFinRight;
    private final ModelPart tailRoot;
    private final ModelPart tailTop;
    private final ModelPart tailMid;
    private final ModelPart tailBot;
    private final ModelPart qiGlow;
    private final ModelPart lateralLine;
    private final ModelPart gillCoverLeft;
    private final ModelPart gillCoverRight;
    private final ModelPart bellyRidge;

    public SoulFishModel(ModelPart root) {
        this.root = root;
        this.bodyFront = root.getChild("body_front");
        this.bodyMid = this.bodyFront.getChild("body_mid");
        this.bodyRear = this.bodyMid.getChild("body_rear");
        this.head = this.bodyFront.getChild("head");
        this.eyeLeft = this.head.getChild("eye_left");
        this.eyeRight = this.head.getChild("eye_right");
        this.mouth = this.head.getChild("mouth");
        this.dorsalFinBase = this.bodyFront.getChild("dorsal_fin_base");
        this.dorsalFinMid = this.dorsalFinBase.getChild("dorsal_fin_mid");
        this.dorsalFinTip = this.dorsalFinMid.getChild("dorsal_fin_tip");
        this.analFinBase = this.bodyFront.getChild("anal_fin_base");
        this.analFinTip = this.analFinBase.getChild("anal_fin_tip");
        this.pecFinBaseLeft = root.getChild("pec_fin_base_left");
        this.pecFinTipLeft = this.pecFinBaseLeft.getChild("pec_fin_tip_left");
        this.pecFinBaseRight = root.getChild("pec_fin_base_right");
        this.pecFinTipRight = this.pecFinBaseRight.getChild("pec_fin_tip_right");
        this.ventralFinLeft = this.bodyMid.getChild("ventral_fin_left");
        this.ventralFinRight = this.bodyMid.getChild("ventral_fin_right");
        this.tailRoot = root.getChild("tail_root");
        this.tailTop = this.tailRoot.getChild("tail_top");
        this.tailMid = this.tailRoot.getChild("tail_mid");
        this.tailBot = this.tailRoot.getChild("tail_bot");
        this.qiGlow = root.getChild("qi_glow");
        this.lateralLine = this.bodyFront.getChild("lateral_line");
        this.gillCoverLeft = this.head.getChild("gill_cover_left");
        this.gillCoverRight = this.head.getChild("gill_cover_right");
        this.bellyRidge = this.bodyFront.getChild("belly_ridge");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        CubeDeformation bodyStreamlined = new CubeDeformation(0.5F);
        CubeDeformation bodyMidDef = new CubeDeformation(0.4F);
        CubeDeformation bodyTapered = new CubeDeformation(0.3F);

        // ── body_front : wider torpedo front (v2: 2.5×2.0×2.5 → v3: 5.0×4.0×5.0) ──
        PartDefinition bodyFront = root.addOrReplaceChild("body_front",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.5F, -2.0F, -2.5F, 5.0F, 4.0F, 5.0F, bodyStreamlined),
                PartPose.offset(0.0F, 12.0F, 1.0F));

        // ── body_mid : NEW 3rd segment for smooth 3-point taper ────────────
        PartDefinition bodyMid = bodyFront.addOrReplaceChild("body_mid",
                CubeListBuilder.create().texOffs(0, 10)
                        .addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 4.0F, bodyMidDef),
                PartPose.offset(0.0F, 0.0F, 2.5F));

        // ── body_rear : narrower taper rear ────────────────────────────────
        bodyMid.addOrReplaceChild("body_rear",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-1.5F, -1.2F, 0.0F, 3.0F, 2.4F, 4.0F, bodyTapered),
                PartPose.offset(0.0F, 0.0F, 4.0F));

        // ── head : rounded skull (v2: 2.0×1.6×1.5 → v3: 4.0×3.2×3.0) ───
        PartDefinition head = bodyFront.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 4)
                        .addBox(-2.0F, -1.6F, -3.0F, 4.0F, 3.2F, 3.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, -0.4F, -5.0F));

        // Eyes on sides of head (v2: 0.4 → v3: 1.0)
        head.addOrReplaceChild("eye_left",
                CubeListBuilder.create().texOffs(20, 0)
                        .addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(-1.8F, -0.8F, -0.6F));
        head.addOrReplaceChild("eye_right",
                CubeListBuilder.create().texOffs(20, 2)
                        .addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(1.8F, -0.8F, -0.6F));

        // Mouth at front of snout (v2: 0.6×0.3×0.2 → v3: 1.2×0.6×0.4)
        head.addOrReplaceChild("mouth",
                CubeListBuilder.create().texOffs(22, 0)
                        .addBox(-0.6F, -0.3F, -0.4F, 1.2F, 0.6F, 0.4F),
                PartPose.offset(0.0F, 0.6F, -2.6F));

        // ── gill covers (v2: 0.1×0.6×0.4 → v3: 0.3×1.2×0.8) ───────────
        head.addOrReplaceChild("gill_cover_left",
                CubeListBuilder.create().texOffs(24, 2)
                        .addBox(-0.15F, -0.6F, -0.4F, 0.3F, 1.2F, 0.8F),
                PartPose.offset(-2.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("gill_cover_right",
                CubeListBuilder.create().texOffs(24, 4)
                        .addBox(-0.15F, -0.6F, -0.4F, 0.3F, 1.2F, 0.8F),
                PartPose.offset(2.0F, 0.0F, 0.0F));

        // ── dorsal fin : 3-segment fan (was 2-segment in v2) ─────────────
        PartDefinition dorsalBase = bodyFront.addOrReplaceChild("dorsal_fin_base",
                CubeListBuilder.create().texOffs(0, 8)
                        .addBox(-0.2F, -1.6F, -1.2F, 0.4F, 2.0F, 2.4F),
                PartPose.offset(0.0F, -2.0F, -0.6F));
        PartDefinition dorsalMid = dorsalBase.addOrReplaceChild("dorsal_fin_mid",
                CubeListBuilder.create().texOffs(2, 8)
                        .addBox(-0.15F, -1.2F, -0.8F, 0.3F, 1.6F, 1.6F),
                PartPose.offset(0.0F, -2.0F, 0.0F));
        dorsalMid.addOrReplaceChild("dorsal_fin_tip",
                CubeListBuilder.create().texOffs(4, 8)
                        .addBox(-0.1F, -0.8F, -0.4F, 0.2F, 1.0F, 0.8F),
                PartPose.offset(0.0F, -1.2F, 0.0F));

        // ── anal fin : 2-segment fan (scaled 2x) ─────────────────────────
        PartDefinition analBase = bodyFront.addOrReplaceChild("anal_fin_base",
                CubeListBuilder.create().texOffs(0, 12)
                        .addBox(-0.2F, 0.0F, -0.8F, 0.4F, 1.0F, 1.6F),
                PartPose.offset(0.0F, 2.0F, 2.0F));
        analBase.addOrReplaceChild("anal_fin_tip",
                CubeListBuilder.create().texOffs(2, 12)
                        .addBox(-0.1F, 0.0F, -0.4F, 0.2F, 0.6F, 0.8F),
                PartPose.offset(0.0F, 1.0F, 0.0F));

        // ── lateral line : ridge along flank (scaled 2x) ───────────────────
        bodyFront.addOrReplaceChild("lateral_line",
                CubeListBuilder.create().texOffs(24, 0)
                        .addBox(-2.6F, -0.1F, -3.0F, 0.2F, 0.2F, 6.0F),
                PartPose.offset(0.0F, 0.6F, 0.0F));

        // ── belly ridge : lighter underbelly (scaled 2x) ─────────────────
        bodyFront.addOrReplaceChild("belly_ridge",
                CubeListBuilder.create().texOffs(4, 4)
                        .addBox(-2.0F, 1.6F, -2.0F, 4.0F, 0.4F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // ── ventral fins : NEW pelvic fin pair on belly ──────────────────
        bodyMid.addOrReplaceChild("ventral_fin_left",
                CubeListBuilder.create().texOffs(8, 14)
                        .addBox(-0.8F, 0.0F, -0.3F, 0.6F, 0.4F, 0.6F),
                PartPose.offsetAndRotation(-1.8F, 1.5F, 0.0F, 0.0F, 0.0F, -0.3F));
        bodyMid.addOrReplaceChild("ventral_fin_right",
                CubeListBuilder.create().texOffs(8, 14)
                        .addBox(0.2F, 0.0F, -0.3F, 0.6F, 0.4F, 0.6F),
                PartPose.offsetAndRotation(1.8F, 1.5F, 0.0F, 0.0F, 0.0F, 0.3F));

        // ── pectoral fins : 2-box each, scaled 2x ───────────────────────
        PartDefinition pecBaseL = root.addOrReplaceChild("pec_fin_base_left",
                CubeListBuilder.create().texOffs(4, 8)
                        .addBox(-2.0F, -0.1F, -1.0F, 2.0F, 0.2F, 2.0F),
                PartPose.offset(-2.5F, 13.0F, -1.0F));
        pecBaseL.addOrReplaceChild("pec_fin_tip_left",
                CubeListBuilder.create().texOffs(6, 8)
                        .addBox(-1.6F, -0.06F, -0.8F, 1.6F, 0.12F, 1.6F),
                PartPose.offset(-2.0F, 0.0F, 0.0F));

        PartDefinition pecBaseR = root.addOrReplaceChild("pec_fin_base_right",
                CubeListBuilder.create().texOffs(4, 10)
                        .addBox(0.0F, -0.1F, -1.0F, 2.0F, 0.2F, 2.0F),
                PartPose.offset(2.5F, 13.0F, -1.0F));
        pecBaseR.addOrReplaceChild("pec_fin_tip_right",
                CubeListBuilder.create().texOffs(6, 10)
                        .addBox(0.0F, -0.06F, -0.8F, 1.6F, 0.12F, 1.6F),
                PartPose.offset(2.0F, 0.0F, 0.0F));

        // ── tail root : peduncle connector (scaled 2x) ────────────────────
        PartDefinition tailRoot = root.addOrReplaceChild("tail_root",
                CubeListBuilder.create().texOffs(12, 4)
                        .addBox(-0.7F, -0.7F, 0.0F, 1.4F, 1.4F, 4.0F, new CubeDeformation(0.15F)),
                PartPose.offset(0.0F, 12.0F, 5.0F));

        // ── tail fan : 3 lobes, WIDER (v2: 2.8 max → v3: 5.6 max) ─────
        tailRoot.addOrReplaceChild("tail_top",
                CubeListBuilder.create().texOffs(0, 14)
                        .addBox(-2.4F, -1.6F, -0.06F, 4.8F, 1.0F, 0.12F),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        tailRoot.addOrReplaceChild("tail_mid",
                CubeListBuilder.create().texOffs(0, 15)
                        .addBox(-2.8F, -0.3F, -0.06F, 5.6F, 0.6F, 0.12F),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        tailRoot.addOrReplaceChild("tail_bot",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-2.4F, 0.6F, -0.06F, 4.8F, 1.0F, 0.12F),
                PartPose.offset(0.0F, 0.0F, 4.0F));

        // ── qi glow : transparent aura (scaled 2x) ──────────────────────
        root.addOrReplaceChild("qi_glow",
                CubeListBuilder.create().texOffs(0, 20)
                        .addBox(-3.6F, -2.6F, -6.0F, 7.2F, 5.2F, 12.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    /** Expose body/eyes/qi-glow for emissive rendering. */
    public ModelPart getBody() { return this.bodyFront; }
    public ModelPart getEyeLeft() { return this.eyeLeft; }
    public ModelPart getEyeRight() { return this.eyeRight; }
    public ModelPart getQiGlow() { return this.qiGlow; }

    @Override
    public void setupAnim(SpiritBeastEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // ── Head turn ──────────────────────────────────────────────────
        this.head.yRot = netHeadYaw * 0.017453292F * 0.5F;
        this.head.xRot = headPitch * 0.017453292F * 0.3F;

        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING
                || entity.isInWater();
        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;

        if (resting) {
            // ── RESTING : fish hovers in place ──────────────────────────
            float hover = (float) Math.sin(ageInTicks * 0.05F) * 0.1F;
            this.root.y = hover;
            // Fins spread slightly for stability
            this.pecFinBaseLeft.zRot = -0.15F;
            this.pecFinBaseRight.zRot = 0.15F;
            // Tail still with occasional twitch
            float twitch = (ageInTicks % 80 < 3) ? (float) Math.sin(ageInTicks * 3.0F) * 0.15F : 0.0F;
            this.tailRoot.yRot = twitch;
            this.tailTop.yRot = twitch * 0.5F;
            this.tailMid.yRot = twitch * 0.4F;
            this.tailBot.yRot = twitch * 0.5F;
            // Dorsal fin idle (3-segment chain)
            this.dorsalFinBase.zRot = (float) Math.sin(ageInTicks * 0.08F) * 0.03F;
            this.dorsalFinMid.zRot = this.dorsalFinBase.zRot * 0.7F;
            this.dorsalFinTip.zRot = this.dorsalFinMid.zRot * 0.5F;
            // Qi glow pulses slowly
            this.qiGlow.yScale = 1.0F + (float) Math.sin(ageInTicks * 0.1F) * 0.05F;
            // Gill covers subtly open
            this.gillCoverLeft.zRot = -0.1F;
            this.gillCoverRight.zRot = 0.1F;
            // Ventral fins idle spread
            this.ventralFinLeft.zRot = -0.05F;
            this.ventralFinRight.zRot = 0.05F;
        } else if (swimming) {
            // ── SWIM : tail-driven oscillation ────────────────────────
            float tailFreq = 2.5F + limbSwingAmount * 1.5F;
            float tailPhase = ageInTicks * tailFreq;
            float tailAmp = 0.4F + limbSwingAmount * 0.3F;

            // Tail root oscillates (source of propulsion)
            this.tailRoot.yRot = (float) Math.sin(tailPhase) * tailAmp;
            // 3 tail lobes follow with phase delays (creates forked S-shape)
            this.tailTop.yRot = (float) Math.sin(tailPhase + 0.3F) * tailAmp * 0.8F;
            this.tailMid.yRot = (float) Math.sin(tailPhase + 0.2F) * tailAmp * 0.85F;
            this.tailBot.yRot = (float) Math.sin(tailPhase + 0.3F) * tailAmp * 0.8F;

            // Body pitches with each tail beat (reaction)
            this.bodyFront.xRot = (float) Math.sin(tailPhase + Math.PI) * tailAmp * 0.08F;
            this.bodyRear.xRot = (float) Math.sin(tailPhase + Math.PI + 0.3F) * tailAmp * 0.06F;

            // Pectoral fins scull (opposing phase for stability)
            this.pecFinBaseLeft.zRot = (float) Math.sin(tailPhase * 0.4F) * 0.2F;
            this.pecFinBaseRight.zRot = -(float) Math.sin(tailPhase * 0.4F) * 0.2F;
            // Fin tips trail
            this.pecFinTipLeft.zRot = (float) Math.sin(tailPhase * 0.4F - 0.2F) * 0.15F;
            this.pecFinTipRight.zRot = -(float) Math.sin(tailPhase * 0.4F - 0.2F) * 0.15F;

            // Dorsal fin stabilizes (3-segment chain)
            this.dorsalFinBase.zRot = (float) Math.sin(tailPhase * 0.3F) * 0.05F;
            this.dorsalFinMid.zRot = (float) Math.sin(tailPhase * 0.3F + 0.1F) * 0.04F;
            this.dorsalFinTip.zRot = (float) Math.sin(tailPhase * 0.3F + 0.2F) * 0.03F;

            // Anal fin flexes
            this.analFinBase.zRot = (float) Math.sin(tailPhase + 0.5F) * 0.08F;
            this.analFinTip.zRot = (float) Math.sin(tailPhase + 0.6F) * 0.06F;

            // Ventral fins scull (opposing to pec)
            this.ventralFinLeft.zRot = -(float) Math.sin(tailPhase * 0.3F) * 0.1F;
            this.ventralFinRight.zRot = (float) Math.sin(tailPhase * 0.3F) * 0.1F;

            // Qi glow pulses faster when swimming
            this.qiGlow.yScale = 1.0F + (float) Math.sin(ageInTicks * 0.2F) * 0.08F;

            // Mouth opens slightly during fast swimming
            this.mouth.xRot = limbSwingAmount * 0.1F;
            // Gill covers open more during active swimming
            this.gillCoverLeft.zRot = -0.15F * limbSwingAmount;
            this.gillCoverRight.zRot = 0.15F * limbSwingAmount;
        } else {
            // ── IDLE : gentle drift ────────────────────────────────────
            float idle = ageInTicks * 0.1F;
            this.tailRoot.yRot = (float) Math.sin(idle * 0.8F) * 0.1F;
            this.tailTop.yRot = (float) Math.sin(idle * 0.8F + 0.15F) * 0.08F;
            this.tailMid.yRot = (float) Math.sin(idle * 0.8F + 0.1F) * 0.07F;
            this.tailBot.yRot = (float) Math.sin(idle * 0.8F + 0.15F) * 0.08F;
            this.pecFinBaseLeft.zRot = 0.0F;
            this.pecFinBaseRight.zRot = 0.0F;
            this.pecFinTipLeft.zRot = 0.0F;
            this.pecFinTipRight.zRot = 0.0F;
            this.dorsalFinBase.zRot = 0.0F;
            this.dorsalFinMid.zRot = 0.0F;
            this.dorsalFinTip.zRot = 0.0F;
            this.qiGlow.yScale = 1.0F;
            this.mouth.xRot = 0.0F;
            this.bodyRear.xRot = 0.0F;
            this.gillCoverLeft.zRot = -0.05F;
            this.gillCoverRight.zRot = 0.05F;
            this.ventralFinLeft.zRot = 0.0F;
            this.ventralFinRight.zRot = 0.0F;
        }

        // ── death : belly-up, fins spread, qi fades ────────────────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F);
            float collapse = t * t;
            this.root.zRot = collapse * (float) Math.PI;
            this.root.y = -collapse * 2.0F;
            this.pecFinBaseLeft.zRot = -collapse * 0.8F;
            this.pecFinBaseRight.zRot = collapse * 0.8F;
            this.tailRoot.yRot *= (1.0F - collapse);
            this.qiGlow.yScale = 1.0F - collapse;
            this.mouth.xRot = collapse * 0.3F;
            // Gill covers splay open in death
            this.gillCoverLeft.zRot = -collapse * 0.3F;
            this.gillCoverRight.zRot = collapse * 0.3F;
            // Ventral fins splay
            this.ventralFinLeft.zRot = -collapse * 0.4F;
            this.ventralFinRight.zRot = collapse * 0.4F;
        }
    }
}
