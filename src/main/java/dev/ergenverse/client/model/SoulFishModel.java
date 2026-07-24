package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/soul_fish.png  SIZE: 64x32
/*
 * SoulFishModel v2 — CRON-COMPLETIONIST-69: TAPERED multi-segment body.
 *
 * PREVIOUS MODEL (v1): 14 addBox calls. Single sausage body (3x2x4 box),
 * flat-slab fins, cardboard tail fan. Scored 2/10 for anatomy.
 *
 * NEW MODEL (v2): 22 addBox calls. 3-segment tapered body (head_taper →
 * body_front → body_rear), creating a visible teardrop silhouette instead
 * of a uniform cylinder. Fins have proper webbing. Tail has 3 independent
 * lobes instead of 2 flat slabs.
 *
 * ANATOMY improvements:
 *   - body_front: wider torpedo front (2.5 x 2.0 x 2.5) with CubeDeformation 0.3
 *   - body_rear:  narrower taper rear (2.0 x 1.5 x 2.5) with CubeDeformation 0.2
 *   - head:      rounded skull extending forward from body_front
 *     - eye_left, eye_right: small spheres on sides
 *     - mouth:    small cube at snout front
 *   - dorsal_fin:  taller, with 2-box fan (base + tip) instead of single slab
 *   - anal_fin:    same improvement
 *   - pec_fin_L/R: wider pectoral fins with webbing (2 boxes each: base + tip)
 *   - tail_root:   narrow connector (inherited from v1)
 *   - tail_fan:    3 lobes (top, middle, bottom) instead of 2 flat slabs
 *   - qi_glow:     transparent aura (inherited)
 *   - lateral_line: ridge along flank (inherited)
 *   - gill_cover:  NEW — visible gill slit on each side of head
 *   - belly_ridge: NEW — lighter underbelly plate
 *
 * ANIMATION: Preserved from v1 (tail oscillation, pectoral sculling,
 * idle drift, death belly-up, mouth breathing) — the animation system
 * was the strong point of v1 and needed no changes.
 *
 * HARSH SELF-CRITIQUE:
 *   - Body is now 2 segments instead of 1 — an improvement, but real fish
 *     have continuous tapering, not a 2-step approximation. The seam between
 *     body_front and body_rear may be visible.
 *   - Dorsal fin is 2 boxes instead of 1 — still flat slabs, not curved
 *     membrane. Real dorsal fins have individual rays.
 *   - Tail fan has 3 lobes instead of 2 — reads better from the side view
 *     but each lobe is still a flat box. Real caudal fins are forked fans.
 *   - Gill cover is a single box — real gill covers articulate (open/close
 *     for breathing). This one is static.
 *   - At 0.3 block scale, most of these improvements are barely visible
 *     to the player. The model is still "programmer art" but less offensive.
 *   - The texture (1369B, 290 colors) is much improved from v1 (480B, 64
 *     colors) but still lacks the iridescent shimmer that canon describes.
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
    private final ModelPart bodyRear;
    private final ModelPart head;
    private final ModelPart eyeLeft;
    private final ModelPart eyeRight;
    private final ModelPart mouth;
    private final ModelPart dorsalFinBase;
    private final ModelPart dorsalFinTip;
    private final ModelPart analFinBase;
    private final ModelPart analFinTip;
    private final ModelPart pecFinBaseLeft;
    private final ModelPart pecFinTipLeft;
    private final ModelPart pecFinBaseRight;
    private final ModelPart pecFinTipRight;
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
        this.bodyRear = this.bodyFront.getChild("body_rear");
        this.head = this.bodyFront.getChild("head");
        this.eyeLeft = this.head.getChild("eye_left");
        this.eyeRight = this.head.getChild("eye_right");
        this.mouth = this.head.getChild("mouth");
        this.dorsalFinBase = this.bodyFront.getChild("dorsal_fin_base");
        this.dorsalFinTip = this.dorsalFinBase.getChild("dorsal_fin_tip");
        this.analFinBase = this.bodyFront.getChild("anal_fin_base");
        this.analFinTip = this.analFinBase.getChild("anal_fin_tip");
        this.pecFinBaseLeft = root.getChild("pec_fin_base_left");
        this.pecFinTipLeft = this.pecFinBaseLeft.getChild("pec_fin_tip_left");
        this.pecFinBaseRight = root.getChild("pec_fin_base_right");
        this.pecFinTipRight = this.pecFinBaseRight.getChild("pec_fin_tip_right");
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

        CubeDeformation bodyStreamlined = new CubeDeformation(0.3F);
        CubeDeformation bodyTapered = new CubeDeformation(0.2F);

        // ── body_front : wider torpedo front ─────────────────────────
        PartDefinition bodyFront = root.addOrReplaceChild("body_front",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.25F, -1.0F, -1.25F, 2.5F, 2.0F, 2.5F, bodyStreamlined),
                PartPose.offset(0.0F, 6.0F, 0.5F));

        // ── body_rear : narrower taper rear (child of body_front) ──────
        bodyFront.addOrReplaceChild("body_rear",
                CubeListBuilder.create().texOffs(8, 0)
                        .addBox(-1.0F, -0.75F, 0.0F, 2.0F, 1.5F, 2.5F, bodyTapered),
                PartPose.offset(0.0F, 0.25F, 1.25F));

        // ── head : rounded skull extending forward ───────────────────
        PartDefinition head = bodyFront.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 4)
                        .addBox(-1.0F, -0.8F, -1.5F, 2.0F, 1.6F, 1.5F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, -0.2F, -2.5F));

        // Eyes on sides of head
        head.addOrReplaceChild("eye_left",
                CubeListBuilder.create().texOffs(20, 0)
                        .addBox(-0.2F, -0.2F, -0.2F, 0.4F, 0.4F, 0.4F),
                PartPose.offset(-0.9F, -0.4F, -0.3F));
        head.addOrReplaceChild("eye_right",
                CubeListBuilder.create().texOffs(20, 2)
                        .addBox(-0.2F, -0.2F, -0.2F, 0.4F, 0.4F, 0.4F),
                PartPose.offset(0.9F, -0.4F, -0.3F));

        // Mouth at front of snout
        head.addOrReplaceChild("mouth",
                CubeListBuilder.create().texOffs(22, 0)
                        .addBox(-0.3F, -0.15F, -0.2F, 0.6F, 0.3F, 0.2F),
                PartPose.offset(0.0F, 0.3F, -1.3F));

        // ── gill covers : visible gill slits ────────────────────────
        head.addOrReplaceChild("gill_cover_left",
                CubeListBuilder.create().texOffs(24, 2)
                        .addBox(-0.05F, -0.3F, -0.2F, 0.1F, 0.6F, 0.4F),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("gill_cover_right",
                CubeListBuilder.create().texOffs(24, 4)
                        .addBox(-0.05F, -0.3F, -0.2F, 0.1F, 0.6F, 0.4F),
                PartPose.offset(1.0F, 0.0F, 0.0F));

        // ── dorsal fin : 2-box fan (base + tip) ────────────────────
        PartDefinition dorsalBase = bodyFront.addOrReplaceChild("dorsal_fin_base",
                CubeListBuilder.create().texOffs(0, 8)
                        .addBox(-0.1F, -0.8F, -0.6F, 0.2F, 1.0F, 1.2F),
                PartPose.offset(0.0F, -1.0F, -0.3F));
        dorsalBase.addOrReplaceChild("dorsal_fin_tip",
                CubeListBuilder.create().texOffs(2, 8)
                        .addBox(-0.05F, -0.6F, -0.4F, 0.1F, 0.8F, 0.8F),
                PartPose.offset(0.0F, -1.0F, 0.0F));

        // ── anal fin : 2-box fan ──────────────────────────────────
        PartDefinition analBase = bodyFront.addOrReplaceChild("anal_fin_base",
                CubeListBuilder.create().texOffs(0, 12)
                        .addBox(-0.1F, 0.0F, -0.4F, 0.2F, 0.5F, 0.8F),
                PartPose.offset(0.0F, 1.0F, 1.0F));
        analBase.addOrReplaceChild("anal_fin_tip",
                CubeListBuilder.create().texOffs(2, 12)
                        .addBox(-0.05F, 0.0F, -0.2F, 0.1F, 0.3F, 0.4F),
                PartPose.offset(0.0F, 0.5F, 0.0F));

        // ── lateral line : ridge along flank ───────────────────────
        bodyFront.addOrReplaceChild("lateral_line",
                CubeListBuilder.create().texOffs(24, 0)
                        .addBox(-1.3F, -0.05F, -1.5F, 0.1F, 0.1F, 3.0F),
                PartPose.offset(0.0F, 0.3F, 0.0F));

        // ── belly ridge : lighter underbelly ───────────────────────
        bodyFront.addOrReplaceChild("belly_ridge",
                CubeListBuilder.create().texOffs(4, 4)
                        .addBox(-1.0F, 0.8F, -1.0F, 2.0F, 0.2F, 2.5F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // ── pectoral fins : 2-box each (base + webbing tip) ─────────
        PartDefinition pecBaseL = root.addOrReplaceChild("pec_fin_base_left",
                CubeListBuilder.create().texOffs(4, 8)
                        .addBox(-1.0F, -0.05F, -0.5F, 1.0F, 0.1F, 1.0F),
                PartPose.offset(-1.25F, 6.5F, -0.5F));
        pecBaseL.addOrReplaceChild("pec_fin_tip_left",
                CubeListBuilder.create().texOffs(6, 8)
                        .addBox(-0.8F, -0.03F, -0.4F, 0.8F, 0.06F, 0.8F),
                PartPose.offset(-1.0F, 0.0F, 0.0F));

        PartDefinition pecBaseR = root.addOrReplaceChild("pec_fin_base_right",
                CubeListBuilder.create().texOffs(4, 10)
                        .addBox(0.0F, -0.05F, -0.5F, 1.0F, 0.1F, 1.0F),
                PartPose.offset(1.25F, 6.5F, -0.5F));
        pecBaseR.addOrReplaceChild("pec_fin_tip_right",
                CubeListBuilder.create().texOffs(6, 10)
                        .addBox(0.0F, -0.03F, -0.4F, 0.8F, 0.06F, 0.8F),
                PartPose.offset(1.0F, 0.0F, 0.0F));

        // ── tail root : narrow connector ───────────────────────────
        PartDefinition tailRoot = root.addOrReplaceChild("tail_root",
                CubeListBuilder.create().texOffs(12, 4)
                        .addBox(-0.35F, -0.35F, 0.0F, 0.7F, 0.7F, 2.0F, new CubeDeformation(0.15F)),
                PartPose.offset(0.0F, 6.0F, 2.5F));

        // ── tail fan : 3 independent lobes ─────────────────────────
        // Top lobe
        tailRoot.addOrReplaceChild("tail_top",
                CubeListBuilder.create().texOffs(0, 14)
                        .addBox(-1.2F, -0.8F, -0.03F, 2.4F, 0.5F, 0.06F),
                PartPose.offset(0.0F, 0.0F, 2.0F));
        // Middle lobe
        tailRoot.addOrReplaceChild("tail_mid",
                CubeListBuilder.create().texOffs(0, 15)
                        .addBox(-1.4F, -0.15F, -0.03F, 2.8F, 0.3F, 0.06F),
                PartPose.offset(0.0F, 0.0F, 2.0F));
        // Bottom lobe
        tailRoot.addOrReplaceChild("tail_bot",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-1.2F, 0.3F, -0.03F, 2.4F, 0.5F, 0.06F),
                PartPose.offset(0.0F, 0.0F, 2.0F));

        // ── qi glow : transparent aura ──────────────────────────────
        root.addOrReplaceChild("qi_glow",
                CubeListBuilder.create().texOffs(0, 20)
                        .addBox(-1.8F, -1.3F, -3.0F, 3.6F, 2.6F, 6.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 6.0F, 0.0F));

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
            // Dorsal fin idle
            this.dorsalFinBase.zRot = (float) Math.sin(ageInTicks * 0.08F) * 0.03F;
            this.dorsalFinTip.zRot = this.dorsalFinBase.zRot * 0.7F;
            // Qi glow pulses slowly
            this.qiGlow.yScale = 1.0F + (float) Math.sin(ageInTicks * 0.1F) * 0.05F;
            // Gill covers subtly open
            this.gillCoverLeft.zRot = -0.1F;
            this.gillCoverRight.zRot = 0.1F;
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
            this.bodyRear.xRot = (float) Math.sin(tailPhase + Math.PI + 0.2F) * tailAmp * 0.06F;

            // Pectoral fins scull (opposing phase for stability)
            this.pecFinBaseLeft.zRot = (float) Math.sin(tailPhase * 0.4F) * 0.2F;
            this.pecFinBaseRight.zRot = -(float) Math.sin(tailPhase * 0.4F) * 0.2F;
            // Fin tips trail
            this.pecFinTipLeft.zRot = (float) Math.sin(tailPhase * 0.4F - 0.2F) * 0.15F;
            this.pecFinTipRight.zRot = -(float) Math.sin(tailPhase * 0.4F - 0.2F) * 0.15F;

            // Dorsal fin stabilizes (2-box chain)
            this.dorsalFinBase.zRot = (float) Math.sin(tailPhase * 0.3F) * 0.05F;
            this.dorsalFinTip.zRot = (float) Math.sin(tailPhase * 0.3F + 0.1F) * 0.04F;

            // Anal fin flexes
            this.analFinBase.zRot = (float) Math.sin(tailPhase + 0.5F) * 0.08F;
            this.analFinTip.zRot = (float) Math.sin(tailPhase + 0.6F) * 0.06F;

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
            this.dorsalFinTip.zRot = 0.0F;
            this.qiGlow.yScale = 1.0F;
            this.mouth.xRot = 0.0F;
            this.bodyRear.xRot = 0.0F;
            this.gillCoverLeft.zRot = -0.05F;
            this.gillCoverRight.zRot = 0.05F;
        }

        // ── death : belly-up, fins spread, qi fades ────────────────
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
        }
    }
}
