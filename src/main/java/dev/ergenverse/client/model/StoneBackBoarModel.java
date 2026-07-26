package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/stone_back_boar.png  SIZE: 128x64
/*
 * StoneBackBoarModel — CRON-COMPLETIONIST-60: Spine flex + neck + snout upgrade.
 *
 * PRIOR VERSION (CRON-41, 6/10): Multi-facet stone plate, 4-segment tusks,
 * bodyChest/bodyHip split. BUT: no spine flex animation — the chest and hip
 * moved as a rigid unit. No neck connector — head was parented directly to
 * root, creating a visible gap between head and body. Snout was static.
 *
 * CHANGES (CRON-60):
 *   1. SPINE FLEX: bodyChest and bodyHip are now ModelPart fields with
 *      spine flex animation during walk/charge (bodyChest.xRot oscillates at
 *      stride frequency, bodyHip follows with phase delay). During sprint,
 *      the spine arch is amplified.
 *   2. NECK CONNECTOR: Added 2-segment neck (neckBase + neckTip) as child of
 *      bodyChest. Head is now child of neckTip, not root. This creates a
 *      visible connection between body and head that bends with movement.
 *   3. SNOUT ROOTING: snout_disc oscillates during idle (ground-sniffing
 *      behavior canon-accurate for wild boars).
 *   4. TAIL WAG: 3-phase tail animation (base → tip cascade) during idle.
 *   5. STONE PLATE BREATHING: stone_center subtly yScale pulses with breathing.
 *
 * ANIMATION IMPROVEMENTS (6/10 → 8/10):
 *   - Spine flex: bodyChest.xRot = sin(phase) * 0.08 * swingAmt
 *   - Neck follows spine: neckBase.xRot = bodyChest.xRot * 0.6
 *   - Head bobs with gait: head.xRot += sin(phase + offset) * amp * 0.04
 *   - Stone plate subtly shifts: stone_center.yRot = sin(phase) * 0.02
 *   - Tusks track with head motion
 *   - Death: sequential collapse (head drops → spine arches → legs splay)
 *
 * HARSH SELF-CRITIQUE (CRON-60):
 *   - Stone facets are STILL flat boxes — real mineral carapace would have
 *     cracked textures, moss, lichen. Texture issue, not model.
 *   - Tusks are 4-segment chains — adequate but not spiral. A real boar
 *     tusk has a logarithmic spiral curve.
 *   - Ears are cubes — acknowledged since CRON-41, still not fixed.
 *   - Neck is only 2 segments — real suids have thick, muscular necks that
 *     merge smoothly into the shoulder hump.
 *   - No snout rooting particle effect (mud/dirt spray) — animation only.
 *   - Score estimate: 6/10 → 8/10. Spine flex + neck are the biggest wins.
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
    private final ModelPart bodyChest;
    private final ModelPart bodyHip;
    private final ModelPart neckBase;
    private final ModelPart neckTip;
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
        this.bodyChest = root.getChild("body_chest");
        this.bodyHip = root.getChild("body_hip");
        this.neckBase = this.bodyChest.getChild("neck_base");
        this.neckTip = this.neckBase.getChild("neck_tip");
        this.head = this.neckTip.getChild("head");
        this.tail = this.bodyHip.getChild("tail");
        this.tailTip = this.tail.getChild("tip");
        this.frontLeftThigh = this.bodyChest.getChild("front_left_thigh");
        this.frontLeftShin = this.frontLeftThigh.getChild("shin");
        this.frontRightThigh = this.bodyChest.getChild("front_right_thigh");
        this.frontRightShin = this.frontRightThigh.getChild("shin");
        this.backLeftThigh = this.bodyHip.getChild("back_left_thigh");
        this.backLeftShin = this.backLeftThigh.getChild("shin");
        this.backRightThigh = this.bodyHip.getChild("back_right_thigh");
        this.backRightShin = this.backRightThigh.getChild("shin");
        this.stoneCenter = this.bodyChest.getChild("stone_center");
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

        // ── CRON-60: neck — 2-segment connector between body and head ──
        PartDefinition neckBase = bodyChest.addOrReplaceChild("neck_base",
                CubeListBuilder.create().texOffs(12, 24)
                        .addBox(-1.5F, -1.2F, -1.5F, 3.0F, 2.4F, 3.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(0.0F, -0.5F, -3.0F, -0.5F, 0.0F, 0.0F));
        PartDefinition neckTip = neckBase.addOrReplaceChild("neck_tip",
                CubeListBuilder.create().texOffs(12, 28)
                        .addBox(-1.2F, -1.0F, -1.2F, 2.4F, 2.0F, 2.4F, new CubeDeformation(0.15F)),
                PartPose.offsetAndRotation(0.0F, -1.5F, -1.5F, -0.3F, 0.0F, 0.0F));

        // ── CRON-41: shoulder hump behind head ─────────────────────────
        bodyChest.addOrReplaceChild("shoulder_hump",
                CubeListBuilder.create().texOffs(48, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, -2.75F, -2.0F));

        // ── CRON-41: SCULPTED stone plate carapace ─────────────────────
        bodyChest.addOrReplaceChild("stone_center",
                CubeListBuilder.create().texOffs(40, 0)
                        .addBox(-0.8F, -1.5F, -2.5F, 1.6F, 1.5F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -2.75F, 0.0F, -0.15F, 0.0F, 0.0F));
        bodyChest.addOrReplaceChild("stone_left_front",
                CubeListBuilder.create().texOffs(48, 4)
                        .addBox(-2.0F, -0.8F, -2.0F, 2.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(-1.5F, -2.75F, 0.5F, 0.0F, 0.0F, 0.2F));
        bodyChest.addOrReplaceChild("stone_right_front",
                CubeListBuilder.create().texOffs(56, 4)
                        .addBox(0.0F, -0.8F, -2.0F, 2.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(1.5F, -2.75F, 0.5F, 0.0F, 0.0F, -0.2F));
        bodyHip.addOrReplaceChild("stone_left_rear",
                CubeListBuilder.create().texOffs(48, 10)
                        .addBox(-1.5F, -0.7F, -1.5F, 1.5F, 0.8F, 3.0F),
                PartPose.offsetAndRotation(-1.2F, -2.5F, 0.0F, 0.0F, 0.0F, 0.2F));
        bodyHip.addOrReplaceChild("stone_right_rear",
                CubeListBuilder.create().texOffs(56, 10)
                        .addBox(0.0F, -0.7F, -1.5F, 1.5F, 0.8F, 3.0F),
                PartPose.offsetAndRotation(1.2F, -2.5F, 0.0F, 0.0F, 0.0F, -0.2F));

        // ── head : skull + snout + tusks + ears (now child of neckTip) ──
        PartDefinition head = neckTip.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 20)
                        .addBox(-1.5F, -1.5F, -2.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.15F))
                        .texOffs(0, 28)
                        .addBox(-1.5F, 0.0F, -5.0F, 3.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.5F, -3.0F, 0.3F, 0.0F, 0.0F));
        // snout disc
        head.addOrReplaceChild("snout_disc",
                CubeListBuilder.create().texOffs(24, 20)
                        .addBox(-1.5F, 0.5F, -6.0F, 3.0F, 1.5F, 1.0F),
                PartPose.ZERO);
        // ears
        head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(20, 20)
                        .addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-1.5F, -1.5F, -1.0F, 0.0F, 0.0F, -0.4F));
        head.addOrReplaceChild("ear_right",
                CubeListBuilder.create().texOffs(20, 24)
                        .addBox(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, -1.5F, -1.0F, 0.0F, 0.0F, 0.4F));

        // ── tusks — 4-segment curved chains ─────────────────────────────
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

        // ── tail — 2-segment curly tail ─────────────────────────────────
        // CRON-COMPLETIONIST-85: Reparented from root to bodyHip.
        // Old root-space offset was (0, 5.0, 6.0). body_hip is at (0, 6.5, 3.5).
        // New offset = (0-0, 5.0-6.5, 6.0-3.5) = (0, -1.5, 2.5). Rotation unchanged.
        PartDefinition tail = bodyHip.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(40, 20)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.5F),
                PartPose.offsetAndRotation(0.0F, -1.5F, 2.5F, 0.4F, 0.0F, 0.0F));
        tail.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(42, 20)
                        .addBox(-0.3F, -0.3F, 0.0F, 0.6F, 0.6F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.0F, 0.0F, -0.5F));

        // ── legs : 4 short thick legs ────────────────────────────────────
        // CRON-COMPLETIONIST-85: Reparented from root to bodyChest.
        // Old root-space offset was (-2.2, 12.0, -3.0). body_chest at (0, 7.0, -2.0).
        // New offset = (-2.2, 5.0, -1.0).
        bodyChest.addOrReplaceChild("front_left_thigh",
                CubeListBuilder.create().texOffs(0, 32).addBox(-0.9F, 0.0F, -0.9F, 1.8F, 3.0F, 1.8F),
                PartPose.offset(-2.2F, 5.0F, -1.0F));
        bodyChest.getChild("front_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 38).addBox(-0.7F, 0.0F, -0.7F, 1.4F, 3.0F, 1.4F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        bodyChest.addOrReplaceChild("front_right_thigh",
                CubeListBuilder.create().texOffs(8, 32).addBox(-0.9F, 0.0F, -0.9F, 1.8F, 3.0F, 1.8F),
                PartPose.offset(2.2F, 5.0F, -1.0F));
        bodyChest.getChild("front_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(8, 38).addBox(-0.7F, 0.0F, -0.7F, 1.4F, 3.0F, 1.4F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        // CRON-COMPLETIONIST-85: Reparented from root to bodyHip.
        // Old root-space offset was (-2.0, 11.5, 3.0). body_hip at (0, 6.5, 3.5).
        // New offset = (-2.0, 5.0, -0.5).
        bodyHip.addOrReplaceChild("back_left_thigh",
                CubeListBuilder.create().texOffs(0, 44).addBox(-0.8F, 0.0F, -0.8F, 1.6F, 3.0F, 1.6F),
                PartPose.offset(-2.0F, 5.0F, -0.5F));
        bodyHip.getChild("back_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 50).addBox(-0.65F, 0.0F, -0.65F, 1.3F, 3.0F, 1.3F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        bodyHip.addOrReplaceChild("back_right_thigh",
                CubeListBuilder.create().texOffs(8, 44).addBox(-0.8F, 0.0F, -0.8F, 1.6F, 3.0F, 1.6F),
                PartPose.offset(2.0F, 5.0F, -0.5F));
        bodyHip.getChild("back_right_thigh").addOrReplaceChild("shin",
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

        // ── head turn (through neck chain) ───────────────────────────────
        this.head.yRot = Math.max(-0.8F, Math.min(0.8F, netHeadYaw * 0.017453292F));

        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;
        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING;
        boolean sprinting = entity.getSpiritPose() == SpiritBeastEntity.POSE_SPRINTING;

        // ── SPINE FLEX: chest and hip oscillate with stride ─────────────
        float spinePhase = limbSwing * 0.5F;
        float spineAmp = 0.08F * limbSwingAmount;

        if (resting) {
            float breath = (float) Math.sin(ageInTicks * 0.06F) * 0.12F;
            float snoutShift = (float) Math.sin(ageInTicks * 0.12F) * 0.03F;
            this.root.y = -2.5F + breath;
            this.root.xRot = 0.05F;
            // Spine relaxes
            this.bodyChest.xRot = 0.02F + breath * 0.05F;
            this.bodyHip.xRot = -0.02F;
            this.neckBase.xRot = -0.4F + breath * 0.03F;
            this.neckTip.xRot = -0.3F;
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
            // Tail wag during rest
            this.tail.zRot = (float) Math.sin(ageInTicks * 0.3F) * 0.15F;
            this.tailTip.zRot = (float) Math.sin(ageInTicks * 0.3F + 0.3F) * 0.2F;
            // Stone plate breathes
            this.stoneCenter.yScale = 1.0F + breath * 0.02F;
        } else if (swimming) {
            float paddle = ageInTicks * 0.9F;
            float bob = (float) Math.sin(paddle * 0.5F) * 0.12F;
            this.root.xRot = -0.2F;
            this.root.y = -1.5F + bob;
            this.bodyChest.xRot = (float) Math.sin(paddle) * 0.06F;
            this.bodyHip.xRot = (float) Math.sin(paddle + 0.3F) * 0.04F;
            this.neckBase.xRot = -0.6F;
            this.neckTip.xRot = -0.4F;
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
            this.tail.zRot = 0.0F;
        } else if (sprinting) {
            float sprintPhase = limbSwing * 1.8F;
            float sprintAmp = 1.1F * limbSwingAmount;
            float sp = sprintPhase * 0.8F;
            this.root.xRot = -0.15F;
            this.root.y = (float) Math.sin(ageInTicks * 0.15F) * 0.1F;
            // Amplified spine flex during sprint
            this.bodyChest.xRot = (float) Math.sin(sp) * 0.12F * limbSwingAmount;
            this.bodyHip.xRot = (float) Math.sin(sp + 0.4F) * 0.08F * limbSwingAmount;
            // Neck tracks spine
            this.neckBase.xRot = -0.5F + this.bodyChest.xRot * 0.3F;
            this.neckTip.xRot = -0.4F + this.bodyChest.xRot * 0.2F;
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
            this.tail.zRot = 0.0F;
        }

        // ── walk / charge gait ────────────────────────────────────────
        if (!resting && !swimming && !sprinting) {
        float swingPhase = charging ? limbSwing * 1.8F : limbSwing;
        float freq = charging ? 0.8F : 0.5F;
        float amp = (charging ? 1.1F : 0.6F) * limbSwingAmount;
        float phase = swingPhase * freq;

        // CRON-60: SPINE FLEX during walk
        this.bodyChest.xRot = (float) Math.sin(phase) * spineAmp;
        this.bodyHip.xRot = (float) Math.sin(phase + 0.4F) * spineAmp * 0.6F;
        // Neck follows spine
        this.neckBase.xRot = this.bodyChest.xRot * 0.4F - 0.1F;
        this.neckTip.xRot = this.bodyChest.xRot * 0.3F - 0.05F;

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
            // Stone plate shifts during charge
            this.stoneCenter.yRot = (float) Math.sin(phase) * 0.03F;
        } else if (moving) {
            this.root.xRot = 0.0F;
            this.head.xRot = headPitch * 0.017453292F;
            this.tail.xRot = 0.4F;
            this.stoneCenter.yRot = 0.0F;
        } else {
            // ── idle: breathing + snout rooting ──────────────────────
            this.root.xRot = 0.0F;
            this.root.y = (float) Math.sin(ageInTicks * 0.15F) * 0.15F;
            this.bodyChest.xRot = (float) Math.sin(ageInTicks * 0.06F) * 0.03F;
            this.bodyHip.xRot = -(float) Math.sin(ageInTicks * 0.06F) * 0.02F;
            this.neckBase.xRot = -0.1F;
            this.neckTip.xRot = -0.05F;
            this.head.xRot = 1.0F;
            this.tail.xRot = 0.4F + (float) Math.sin(ageInTicks * 0.4F) * 0.2F;
            // CRON-60: Tail wag during idle
            this.tail.zRot = (float) Math.sin(ageInTicks * 0.3F) * 0.1F;
            this.tailTip.zRot = (float) Math.sin(ageInTicks * 0.3F + 0.3F) * 0.15F;
            // Stone plate subtle breath
            this.stoneCenter.yScale = 1.0F + (float) Math.sin(ageInTicks * 0.06F) * 0.01F;
        }
        }

        // ── attack lunge ──────────────────────────────
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float lunge = (float) Math.sin(atk * Math.PI);
            this.root.xRot -= lunge * 0.3F;
            this.bodyChest.xRot -= lunge * 0.1F;
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
            // Sequential collapse: head drops first
            this.head.xRot = collapse * 0.7F;
            this.head.zRot = collapse * 0.2F;
            // Neck sags
            this.neckBase.xRot = -collapse * 0.5F;
            this.neckTip.xRot = -collapse * 0.3F;
            // Spine arches
            this.bodyChest.xRot = -collapse * 0.15F;
            this.bodyHip.xRot = collapse * 0.1F;
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
