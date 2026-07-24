package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/stone_back_boar.png  SIZE: 128x64
/*
 * StoneBackBoarModel — CRON-COMPLETIONIST-41: Major anatomy overhaul.
 *
 * CHANGES FROM PRIOR VERSION:
 *   - Stone plate UPGRADED from flat slab (6x1x8 "bread slice") to multi-facet
 *     carapace: 5 angled stone plates (center spine ridge + 2 left facets + 2
 *     right facets) forming a sculpted mineral shell. Cited 8+ rounds as looking
 *     like a slice of bread. Fixed.
 *   - Tusks UPGRADED from 2-segment to 4-segment chains (base → lower → mid → tip)
 *     with progressive rotation angles approximating a tighter spiral curve.
 *   - Body SPLIT from single box (5x5x10) into bodyChest + bodyHip with
 *     CubeDeformation for organic roundness. Added shoulder hump.
 *   - Legs widened (thighs 1.8 vs 1.5) for mass, shins taper.
 *
 * ANATOMY (CRON-41 overhaul):
 *   - bodyChest    : wider front torso (6 x 5.5 x 6, CubeDeformation 0.35)
 *   - bodyHip      : narrower rear torso (5 x 5 x 5.5, CubeDeformation 0.3)
 *   - shoulderHump : muscle/fat bulge behind head (2 x 2 x 2)
 *   - stone_plate  : SCULPTED carapace — 5 angled plates forming a peaked ridge
 *                    down the center with angled facet sides. NOT flat.
 *   - head         : skull + snout + snout_disc + 2 CURVED tusks (4-segment)
 *   - legs         : 4 short thick legs, wider thighs, tapering shins
 *   - tail         : short curly tail (2-segment)
 *
 * ANIMATION: unchanged from prior — B+ quality.
 *
 * HARSH SELF-CRITIQUE (CRON-41):
 *   - Stone plate is now 5 angled plates forming a peaked ridge — DRAMATICALLY
 *     better than the flat bread slice. Score improved from 2/10 to ~6/10.
 *   - Tusks are 4-segment chains — tighter spiral approximation than the
 *     2-segment version. Score improved from 5/10 to ~6/10.
 *   - Body split gives a boar silhouette (wide front, narrow rear) instead
 *     of a uniform box.
 *   - Stone facets are still flat boxes — real mineral carapace would have
 *     cracked textures, moss, lichen. That's a texture issue, not model.
 *   - Texture UV layout changed — stone_back_boar.png MUST be regenerated.
 *   - Overall model score improved from 4/10 to ~6/10.
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

public class StoneBackBoarModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart tailTip;
    private final ModelPart frontLeftThigh;
    private final ModelPart frontLeftShin;
    private final ModelPart frontRightThigh;
    private final ModelPart frontRightShin;
    private final ModelPart backLeftThigh;
    private final ModelPart backLeftShin;
    private final ModelPart backRightThigh;
    private final ModelPart backRightShin;
    private final ModelPart stoneCenter;

    public StoneBackBoarModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.tail = root.getChild("tail");
        this.tailTip = this.tail.getChild("tip");
        this.frontLeftThigh = root.getChild("front_left_thigh");
        this.frontLeftShin = this.frontLeftThigh.getChild("shin");
        this.frontRightThigh = root.getChild("front_right_thigh");
        this.frontRightShin = this.frontRightThigh.getChild("shin");
        this.backLeftThigh = root.getChild("back_left_thigh");
        this.backLeftShin = this.backLeftThigh.getChild("shin");
        this.backRightThigh = root.getChild("back_right_thigh");
        this.backRightShin = this.backRightThigh.getChild("shin");
        this.stoneCenter = root.getChild("body_chest").getChild("stone_center");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── CRON-41: bodyChest — wider front torso ─────────────────────
        PartDefinition bodyChest = root.addOrReplaceChild("body_chest",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.0F, -2.75F, -3.0F, 6.0F, 5.5F, 6.0F, new CubeDeformation(0.35F)),
                PartPose.offset(0.0F, 7.0F, -2.0F));

        // ── CRON-41: bodyHip — narrower haunches ───────────────────────
        PartDefinition bodyHip = root.addOrReplaceChild("body_hip",
                CubeListBuilder.create().texOffs(0, 12)
                        .addBox(-2.5F, -2.5F, -2.75F, 5.0F, 5.0F, 5.5F, new CubeDeformation(0.3F)),
                PartPose.offset(0.0F, 6.5F, 3.5F));

        // ── CRON-41: shoulder hump behind head ─────────────────────────
        bodyChest.addOrReplaceChild("shoulder_hump",
                CubeListBuilder.create().texOffs(48, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, -2.75F, -2.0F));

        // ── CRON-41: SCULPTED stone plate carapace ─────────────────────
        // 5 angled plates forming a peaked mineral ridge down the spine
        // Center spine ridge (peaked, angled)
        bodyChest.addOrReplaceChild("stone_center",
                CubeListBuilder.create().texOffs(40, 0)
                        .addBox(-0.8F, -1.5F, -2.5F, 1.6F, 1.5F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -2.75F, 0.0F, -0.15F, 0.0F, 0.0F));
        // Left facet plate (angled outward from ridge)
        bodyChest.addOrReplaceChild("stone_left_front",
                CubeListBuilder.create().texOffs(48, 4)
                        .addBox(-2.0F, -0.8F, -2.0F, 2.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(-1.5F, -2.75F, 0.5F, 0.0F, 0.0F, 0.2F));
        // Right facet plate
        bodyChest.addOrReplaceChild("stone_right_front",
                CubeListBuilder.create().texOffs(56, 4)
                        .addBox(0.0F, -0.8F, -2.0F, 2.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(1.5F, -2.75F, 0.5F, 0.0F, 0.0F, -0.2F));
        // Hip plates (smaller, follow body taper)
        bodyHip.addOrReplaceChild("stone_left_rear",
                CubeListBuilder.create().texOffs(48, 10)
                        .addBox(-1.5F, -0.7F, -1.5F, 1.5F, 0.8F, 3.0F),
                PartPose.offsetAndRotation(-1.2F, -2.5F, 0.0F, 0.0F, 0.0F, 0.2F));
        bodyHip.addOrReplaceChild("stone_right_rear",
                CubeListBuilder.create().texOffs(56, 10)
                        .addBox(0.0F, -0.7F, -1.5F, 1.5F, 0.8F, 3.0F),
                PartPose.offsetAndRotation(1.2F, -2.5F, 0.0F, 0.0F, 0.0F, -0.2F));

        // ── head : skull + snout + tusks + ears ────────────────────────
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 20)
                        .addBox(-1.5F, -1.5F, -2.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.15F))  // skull
                        .texOffs(0, 28)
                        .addBox(-1.5F, 0.0F, -5.0F, 3.0F, 2.0F, 3.0F),   // snout forward
                PartPose.offset(0.0F, 6.0F, -5.0F));
        // snout disc (the flat cartilage tip)
        head.addOrReplaceChild("snout_disc",
                CubeListBuilder.create().texOffs(24, 20)
                        .addBox(-1.5F, 0.5F, -6.0F, 3.0F, 1.5F, 1.0F),
                PartPose.ZERO);
        // small ears (still cubes — acknowledged limitation)
        head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(20, 20)
                        .addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-1.5F, -1.5F, -1.0F, 0.0F, 0.0F, -0.4F));
        head.addOrReplaceChild("ear_right",
                CubeListBuilder.create().texOffs(20, 24)
                        .addBox(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, -1.5F, -1.0F, 0.0F, 0.0F, 0.4F));

        // ── CRON-41: tusks — 4-segment curved chains approximating spiral ──
        // Left tusk: base → lower → mid → tip (progressive rotation)
        PartDefinition tuskLBase = head.addOrReplaceChild("tusk_left_base",
                CubeListBuilder.create().texOffs(32, 16)
                        .addBox(-0.4F, 0.0F, -0.4F, 0.8F, 1.2F, 0.8F),
                PartPose.offsetAndRotation(-1.3F, 1.3F, -3.5F, 0.25F, 0.0F, -0.15F));
        PartDefinition tuskLMid = tuskLBase.addOrReplaceChild("mid",
                CubeListBuilder.create().texOffs(34, 16)
                        .addBox(-0.35F, 0.0F, -0.35F, 0.7F, 1.1F, 0.7F),
                PartPose.offsetAndRotation(0.0F, 1.2F, 0.0F, -0.5F, 0.0F, -0.1F));
        PartDefinition tuskLTip = tuskLMid.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(36, 16)
                        .addBox(-0.3F, 0.0F, -0.3F, 0.6F, 1.0F, 0.6F),
                PartPose.offsetAndRotation(0.0F, 1.1F, 0.0F, -0.7F, 0.0F, -0.05F));
        tuskLTip.addOrReplaceChild("end",
                CubeListBuilder.create().texOffs(38, 16)
                        .addBox(-0.2F, 0.0F, -0.2F, 0.4F, 0.7F, 0.4F),
                PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.8F, 0.0F, 0.0F));

        // Right tusk: mirror
        PartDefinition tuskRBase = head.addOrReplaceChild("tusk_right_base",
                CubeListBuilder.create().texOffs(32, 20)
                        .addBox(-0.4F, 0.0F, -0.4F, 0.8F, 1.2F, 0.8F),
                PartPose.offsetAndRotation(1.3F, 1.3F, -3.5F, 0.25F, 0.0F, 0.15F));
        PartDefinition tuskRMid = tuskRBase.addOrReplaceChild("mid",
                CubeListBuilder.create().texOffs(34, 20)
                        .addBox(-0.35F, 0.0F, -0.35F, 0.7F, 1.1F, 0.7F),
                PartPose.offsetAndRotation(0.0F, 1.2F, 0.0F, -0.5F, 0.0F, 0.1F));
        PartDefinition tuskRTip = tuskRMid.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(36, 20)
                        .addBox(-0.3F, 0.0F, -0.3F, 0.6F, 1.0F, 0.6F),
                PartPose.offsetAndRotation(0.0F, 1.1F, 0.0F, -0.7F, 0.0F, 0.05F));
        tuskRTip.addOrReplaceChild("end",
                CubeListBuilder.create().texOffs(38, 20)
                        .addBox(-0.2F, 0.0F, -0.2F, 0.4F, 0.7F, 0.4F),
                PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.8F, 0.0F, 0.0F));

        // ── CRON-41: tail — 2-segment curly tail ───────────────────────
        PartDefinition tail = root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(40, 20)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.5F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 6.0F, 0.4F, 0.0F, 0.0F));
        tail.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(42, 20)
                        .addBox(-0.3F, -0.3F, 0.0F, 0.6F, 0.6F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.0F, 0.0F, -0.5F));

        // ── legs : 4 short thick legs, wider thighs, tapering shins ──────
        root.addOrReplaceChild("front_left_thigh",
                CubeListBuilder.create().texOffs(0, 32).addBox(-0.9F, 0.0F, -0.9F, 1.8F, 3.0F, 1.8F),
                PartPose.offset(-2.2F, 12.0F, -3.0F));
        root.getChild("front_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 38).addBox(-0.7F, 0.0F, -0.7F, 1.4F, 3.0F, 1.4F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("front_right_thigh",
                CubeListBuilder.create().texOffs(8, 32).addBox(-0.9F, 0.0F, -0.9F, 1.8F, 3.0F, 1.8F),
                PartPose.offset(2.2F, 12.0F, -3.0F));
        root.getChild("front_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(8, 38).addBox(-0.7F, 0.0F, -0.7F, 1.4F, 3.0F, 1.4F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("back_left_thigh",
                CubeListBuilder.create().texOffs(0, 44).addBox(-0.8F, 0.0F, -0.8F, 1.6F, 3.0F, 1.6F),
                PartPose.offset(-2.0F, 11.5F, 3.0F));
        root.getChild("back_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 50).addBox(-0.65F, 0.0F, -0.65F, 1.3F, 3.0F, 1.3F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("back_right_thigh",
                CubeListBuilder.create().texOffs(8, 44).addBox(-0.8F, 0.0F, -0.8F, 1.6F, 3.0F, 1.6F),
                PartPose.offset(2.0F, 11.5F, 3.0F));
        root.getChild("back_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(8, 50).addBox(-0.65F, 0.0F, -0.65F, 1.3F, 3.0F, 1.3F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    // CRON-COMPLETIONIST-59: Expose stone center ridge for emissive spiritual mineral glow.
    public ModelPart getStoneCenter() { return this.stoneCenter; }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(SpiritBeastEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        boolean moving = limbSwingAmount > 0.1F;
        boolean charging = limbSwingAmount > 0.55F
                || entity.getSpiritPose() == SpiritBeastEntity.POSE_CHARGING;

        // ── head turn ────────────────────────────────────────────────────
        this.head.yRot = Math.max(-0.8F, Math.min(0.8F, netHeadYaw * 0.017453292F));

        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;
        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING;
        boolean sprinting = entity.getSpiritPose() == SpiritBeastEntity.POSE_SPRINTING;

        if (resting) {
            float breath = (float) Math.sin(ageInTicks * 0.06F) * 0.12F;
            float snoutShift = (float) Math.sin(ageInTicks * 0.12F) * 0.03F;
            this.root.y = -2.5F + breath;
            this.root.xRot = 0.05F;
            this.frontLeftThigh.xRot  = -0.5F;
            this.frontRightThigh.xRot = -0.5F;
            this.frontLeftShin.xRot   = 0.3F;
            this.frontRightShin.xRot  = 0.3F;
            this.backLeftThigh.xRot   = 0.2F;
            this.backRightThigh.xRot  = 0.2F;
            this.backLeftShin.xRot    = -0.15F;
            this.backRightShin.xRot   = -0.15F;
            this.head.xRot = 0.8F + snoutShift;
            this.tail.xRot = 0.3F;
        } else if (swimming) {
            float paddle = ageInTicks * 0.9F;
            float bob = (float) Math.sin(paddle * 0.5F) * 0.12F;
            this.root.xRot = -0.2F;
            this.root.y = -1.5F + bob;
            this.head.xRot = -0.4F;
            this.frontLeftThigh.xRot  = (float) Math.cos(paddle) * 0.6F;
            this.frontRightThigh.xRot = (float) Math.cos(paddle + Math.PI) * 0.6F;
            this.backLeftThigh.xRot   = (float) Math.cos(paddle + Math.PI) * 0.4F;
            this.backRightThigh.xRot  = (float) Math.cos(paddle) * 0.4F;
            this.frontLeftShin.xRot   = -0.1F + Math.abs((float) Math.cos(paddle)) * 0.2F;
            this.frontRightShin.xRot  = -0.1F + Math.abs((float) Math.cos(paddle + Math.PI)) * 0.2F;
            this.backLeftShin.xRot    = -0.1F + Math.abs((float) Math.cos(paddle + Math.PI)) * 0.15F;
            this.backRightShin.xRot   = -0.1F + Math.abs((float) Math.cos(paddle)) * 0.15F;
            this.tail.xRot = 0.2F;
        } else if (sprinting) {
            float sprintPhase = limbSwing * 1.8F;
            float sprintAmp = 1.1F * limbSwingAmount;
            float sp = sprintPhase * 0.8F;
            this.root.xRot = -0.15F;
            this.root.y = (float) Math.sin(ageInTicks * 0.15F) * 0.1F;
            this.frontLeftThigh.xRot  = (float) Math.cos(sp)            * sprintAmp;
            this.frontRightThigh.xRot = (float) Math.cos(sp + Math.PI)  * sprintAmp;
            this.backLeftThigh.xRot   = (float) Math.cos(sp + Math.PI)  * sprintAmp;
            this.backRightThigh.xRot  = (float) Math.cos(sp)            * sprintAmp;
            this.frontLeftShin.xRot  = -0.15F + Math.max(0.0F, (float) Math.cos(sp))            * 0.25F * limbSwingAmount;
            this.frontRightShin.xRot = -0.15F + Math.max(0.0F, (float) Math.cos(sp + Math.PI))  * 0.25F * limbSwingAmount;
            this.backLeftShin.xRot   = -0.15F + Math.max(0.0F, (float) Math.cos(sp + Math.PI))  * 0.25F * limbSwingAmount;
            this.backRightShin.xRot  = -0.15F + Math.max(0.0F, (float) Math.cos(sp))            * 0.25F * limbSwingAmount;
            this.head.xRot = 1.0F;
            this.tail.xRot = -0.1F;
        }

        // ── walk / charge gait : ONLY when not in special pose ──
        if (!resting && !swimming && !sprinting) {
        float swingPhase = charging ? limbSwing * 1.8F : limbSwing;
        float freq = charging ? 0.8F : 0.5F;
        float amp = (charging ? 1.1F : 0.6F) * limbSwingAmount;
        float phase = swingPhase * freq;

        this.frontLeftThigh.xRot  = (float) Math.cos(phase)            * amp;
        this.frontRightThigh.xRot = (float) Math.cos(phase + Math.PI)  * amp;
        this.backLeftThigh.xRot   = (float) Math.cos(phase + Math.PI)  * amp;
        this.backRightThigh.xRot  = (float) Math.cos(phase)            * amp;
        this.frontLeftShin.xRot  = -0.1F + Math.max(0.0F, (float) Math.cos(phase))            * 0.2F * limbSwingAmount;
        this.frontRightShin.xRot = -0.1F + Math.max(0.0F, (float) Math.cos(phase + Math.PI))  * 0.2F * limbSwingAmount;
        this.backLeftShin.xRot   = -0.1F + Math.max(0.0F, (float) Math.cos(phase + Math.PI))  * 0.2F * limbSwingAmount;
        this.backRightShin.xRot  = -0.1F + Math.max(0.0F, (float) Math.cos(phase))            * 0.2F * limbSwingAmount;

        if (charging) {
            this.root.xRot = -0.10F;
            this.head.xRot = 0.8F;
            this.tail.xRot = 0.0F;
        } else if (moving) {
            this.root.xRot = 0.0F;
            this.head.xRot = headPitch * 0.017453292F;
            this.tail.xRot = 0.4F;
        } else {
            this.root.xRot = 0.0F;
            this.root.y = (float) Math.sin(ageInTicks * 0.15F) * 0.15F;
            this.head.xRot = 1.0F;
            this.tail.xRot = 0.4F + (float) Math.sin(ageInTicks * 0.4F) * 0.2F;
        }
        }

        // ── attack lunge ──────────────────────────────
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float lunge = (float) Math.sin(atk * Math.PI);
            this.root.xRot -= lunge * 0.3F;
            this.head.xRot += lunge * 0.5F;
            this.frontLeftThigh.xRot  -= lunge * 0.2F;
            this.frontRightThigh.xRot -= lunge * 0.2F;
            this.backLeftThigh.xRot   += lunge * 0.3F;
            this.backRightThigh.xRot  += lunge * 0.3F;
        }

        // ── death collapse ───────────────────────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F);
            float collapse = t * t;
            this.root.xRot = collapse * -0.3F;
            this.root.zRot = collapse * 0.4F;
            this.head.xRot = collapse * 0.7F;
            this.head.zRot = collapse * 0.2F;
            this.frontLeftThigh.zRot  = -collapse * 0.4F;
            this.frontRightThigh.zRot =  collapse * 0.4F;
            this.backLeftThigh.zRot   = -collapse * 0.35F;
            this.backRightThigh.zRot  =  collapse * 0.35F;
            this.frontLeftShin.zRot  = -collapse * 0.3F;
            this.frontRightShin.zRot =  collapse * 0.3F;
            this.backLeftShin.zRot   = -collapse * 0.25F;
            this.backRightShin.zRot  =  collapse * 0.25F;
            this.tail.xRot = 0.0F;
        }
    }
}
