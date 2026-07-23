package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/qilin.png  SIZE: 64x64
/*
 * QilinModel — winged wolf-quadruped with antler horns and flowing mane.
 *
 * Canon (Renegade Immortal): qilin (麒麟) are divine beasts of extreme rarity,
 * combining wolf/lion body with deer antlers, dragon scales, and spiritual
 * authority. They appear at moments of great destiny.
 *
 * REAL QILIN MYTHOLOGY anatomy:
 *   - Body: wolf/lion hybrid — deep chest, muscular haunches
 *   - Head: wolf-like with dragon features, broad skull, noble bearing
 *   - Horns: deer-like antlers (branched, multi-tine) — NOT simple horns
 *   - Mane: flowing, sometimes flame-touched, along the neck and spine
 *   - Tail: ox-like, long and tufted
 *   - Legs: wolf paws, sometimes with scales
 *   - Wings: some depictions show feathered wings (this model includes them)
 *   - Scales: dragon scales shimmer on flanks
 *
 * This model extends SpiritWolfModel anatomy with:
 *   - Antlers (branched 3-segment chain per side, not simple cones)
 *   - Flowing mane (5 segments along neck/spine, like fire beast but longer)
 *   - Tufted tail (3-segment chain with fan tip)
 *   - Small wings (2-segment, tucked against shoulders)
 *   - Scaled flank plates (2 boxes on each side of body)
 *
 * ANIMATION:
 *   - Walk/Run: diagonal trot with spine flex (wolf base)
 *   - Flight: wings extend and flap when POSE_FLYING
 *   - Idle: mane sways, antlers still, tail slow sway
 *   - Combat: head lowers, antlers thrust forward
 *   - Death: collapse with quadratic ease-in
 *
 * HARSH SELF-CRITIQUE:
 *   - Antlers are boxy chains — real antlers are branching, tapering, irregular.
 *     Each tine should be a different length and angle.
 *   - Mane is box slabs — real flowing mane would need a chain of thin plates.
 *   - Wings are small and simple — a qilin's wings (when present) should be
 *     grand, feathered, and impressive. These look like afterthoughts.
 *   - Scales are flat boxes — no shimmer, no gradient, no overlap pattern.
 *   - Tail fan is flat boxes — no hair detail, no tuft volume.
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

public class QilinModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart bodyChest;
    private final ModelPart bodyHip;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart antlerLeftBase;
    private final ModelPart antlerLeftMid;
    private final ModelPart antlerLeftTip;
    private final ModelPart antlerRightBase;
    private final ModelPart antlerRightMid;
    private final ModelPart antlerRightTip;
    private final ModelPart mane0;
    private final ModelPart mane1;
    private final ModelPart mane2;
    private final ModelPart mane3;
    private final ModelPart mane4;
    private final ModelPart tailBase;
    private final ModelPart tailMid;
    private final ModelPart tailFan;
    private final ModelPart leftWingRoot;
    private final ModelPart rightWingRoot;
    private final ModelPart leftWingMembrane;
    private final ModelPart rightWingMembrane;
    private final ModelPart scaleFL;
    private final ModelPart scaleBL;
    private final ModelPart scaleFR;
    private final ModelPart scaleBR;
    private final ModelPart frontLeftThigh;
    private final ModelPart frontLeftShin;
    private final ModelPart frontRightThigh;
    private final ModelPart frontRightShin;
    private final ModelPart backLeftThigh;
    private final ModelPart backLeftShin;
    private final ModelPart backRightThigh;
    private final ModelPart backRightShin;

    public QilinModel(ModelPart root) {
        this.root = root;
        this.bodyChest = root.getChild("body_chest");
        this.bodyHip = root.getChild("body_hip");
        this.neck = root.getChild("neck");
        this.head = root.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.antlerLeftBase = this.head.getChild("antler_left_base");
        this.antlerLeftMid = this.antlerLeftBase.getChild("mid");
        this.antlerLeftTip = this.antlerLeftMid.getChild("tip");
        this.antlerRightBase = this.head.getChild("antler_right_base");
        this.antlerRightMid = this.antlerRightBase.getChild("mid");
        this.antlerRightTip = this.antlerRightMid.getChild("tip");
        ModelPart body = this.bodyChest; // mane is on chest body
        this.mane0 = body.getChild("mane_0");
        this.mane1 = body.getChild("mane_1");
        this.mane2 = body.getChild("mane_2");
        this.mane3 = body.getChild("mane_3");
        this.mane4 = body.getChild("mane_4");
        this.tailBase = root.getChild("tail_base");
        this.tailMid = this.tailBase.getChild("mid");
        this.tailFan = this.tailMid.getChild("fan");
        this.leftWingRoot = root.getChild("left_wing_root");
        this.rightWingRoot = root.getChild("right_wing_root");
        this.leftWingMembrane = this.leftWingRoot.getChild("membrane");
        this.rightWingMembrane = this.rightWingRoot.getChild("membrane");
        this.scaleFL = this.bodyChest.getChild("scale_fl");
        this.scaleBL = this.bodyHip.getChild("scale_bl");
        this.scaleFR = this.bodyChest.getChild("scale_fr");
        this.scaleBR = this.bodyHip.getChild("scale_br");
        this.frontLeftThigh = root.getChild("front_left_thigh");
        this.frontLeftShin = this.frontLeftThigh.getChild("shin");
        this.frontRightThigh = root.getChild("front_right_thigh");
        this.frontRightShin = this.frontRightThigh.getChild("shin");
        this.backLeftThigh = root.getChild("back_left_thigh");
        this.backLeftShin = this.backLeftThigh.getChild("shin");
        this.backRightThigh = root.getChild("back_right_thigh");
        this.backRightShin = this.backRightThigh.getChild("shin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── body_chest : front torso, deeper at shoulders ──────────────
        CubeDeformation bodyRound = new CubeDeformation(0.3F);
        PartDefinition bodyChest = root.addOrReplaceChild("body_chest",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.0F, -3.0F, -5.0F, 4.0F, 6.0F, 5.0F, bodyRound),
                PartPose.offset(0.0F, 6.0F, -2.5F));

        // ── body_hip : rear torso, narrower ────────────────────────────
        PartDefinition bodyHip = root.addOrReplaceChild("body_hip",
                CubeListBuilder.create().texOffs(0, 12)
                        .addBox(-1.5F, -2.5F, -1.0F, 3.0F, 5.0F, 6.0F, bodyRound),
                PartPose.offset(0.0F, 5.5F, 2.5F));

        // ── mane : 5 segments along neck/spine ──────────────────────────
        for (int i = 0; i < 5; i++) {
            float z = -3.0F + i * 1.5F;
            bodyChest.addOrReplaceChild("mane_" + i,
                    CubeListBuilder.create().texOffs(40, 0 + i * 6)
                            .addBox(-0.4F, -2.5F, -0.4F, 0.8F, 2.5F, 0.8F),
                    PartPose.offset(0.0F, -3.0F, z));
        }

        // Scale plates on flanks
        bodyChest.addOrReplaceChild("scale_fl",
                CubeListBuilder.create().texOffs(36, 0)
                        .addBox(-0.2F, 0.0F, -3.0F, 0.2F, 3.0F, 4.0F),
                PartPose.offset(-2.2F, -2.0F, 0.0F));
        bodyChest.addOrReplaceChild("scale_fr",
                CubeListBuilder.create().texOffs(36, 8)
                        .addBox(0.0F, 0.0F, -3.0F, 0.2F, 3.0F, 4.0F),
                PartPose.offset(2.0F, -2.0F, 0.0F));
        bodyHip.addOrReplaceChild("scale_bl",
                CubeListBuilder.create().texOffs(36, 16)
                        .addBox(-0.2F, 0.0F, -2.0F, 0.2F, 2.5F, 3.0F),
                PartPose.offset(-1.7F, -1.5F, 0.0F));
        bodyHip.addOrReplaceChild("scale_br",
                CubeListBuilder.create().texOffs(36, 24)
                        .addBox(0.0F, 0.0F, -2.0F, 0.2F, 2.5F, 3.0F),
                PartPose.offset(1.5F, -1.5F, 0.0F));

        // ── neck : tilted connector ────────────────────────────────────
        root.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(28, 0)
                        .addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(0.0F, 4.0F, -5.0F, -0.4F, 0.0F, 0.0F));

        // ── head : skull + jaw + antlers ───────────────────────────────
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 18)
                        .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.15F))
                        .texOffs(0, 26)
                        .addBox(-1.0F, 0.0F, -3.5F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -1.0F, -4.0F));
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(28, 18)
                        .addBox(-1.0F, 0.0F, -2.5F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 0.5F, -1.0F));

        // Branched antlers (3-segment chain per side)
        PartDefinition antLBase = head.addOrReplaceChild("antler_left_base",
                CubeListBuilder.create().texOffs(44, 0)
                        .addBox(-0.3F, -3.0F, -0.3F, 0.6F, 3.0F, 0.6F),
                PartPose.offsetAndRotation(-1.0F, -1.5F, 0.0F, 0.0F, 0.0F, -0.3F));
        PartDefinition antLMid = antLBase.addOrReplaceChild("mid",
                CubeListBuilder.create().texOffs(44, 6)
                        .addBox(-0.2F, -2.0F, -0.2F, 0.4F, 2.0F, 0.4F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.3F, 0.0F, -0.2F));
        antLMid.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(44, 10)
                        .addBox(-0.15F, -1.5F, -0.15F, 0.3F, 1.5F, 0.3F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.2F, 0.0F, 0.0F));

        PartDefinition antRBase = head.addOrReplaceChild("antler_right_base",
                CubeListBuilder.create().texOffs(48, 0)
                        .addBox(-0.3F, -3.0F, -0.3F, 0.6F, 3.0F, 0.6F),
                PartPose.offsetAndRotation(1.0F, -1.5F, 0.0F, 0.0F, 0.0F, 0.3F));
        PartDefinition antRMid = antRBase.addOrReplaceChild("mid",
                CubeListBuilder.create().texOffs(48, 6)
                        .addBox(-0.2F, -2.0F, -0.2F, 0.4F, 2.0F, 0.4F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.3F, 0.0F, 0.2F));
        antRMid.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(48, 10)
                        .addBox(-0.15F, -1.5F, -0.15F, 0.3F, 1.5F, 0.3F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.2F, 0.0F, 0.0F));

        // ── tail : 3-segment chain with fan tip ────────────────────────
        PartDefinition tailBase = root.addOrReplaceChild("tail_base",
                CubeListBuilder.create().texOffs(36, 8)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 5.0F, 0.3F, 0.0F, 0.0F));
        PartDefinition tailMid = tailBase.addOrReplaceChild("mid",
                CubeListBuilder.create().texOffs(36, 14)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.2F, 0.0F, 0.0F));
        tailMid.addOrReplaceChild("fan",
                CubeListBuilder.create().texOffs(50, 0)
                        .addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 1.5F),
                PartPose.offset(0.0F, 0.0F, 3.0F));

        // ── small wings (tucked against shoulders) ─────────────────────
        PartDefinition leftWingRoot = root.addOrReplaceChild("left_wing_root",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-2.0F, 4.0F, -3.0F, 0.0F, 0.0F, -0.8F));
        leftWingRoot.addOrReplaceChild("membrane",
                CubeListBuilder.create().texOffs(20, 0)
                        .addBox(-5.0F, -0.3F, -2.0F, 5.0F, 0.6F, 4.0F),
                PartPose.ZERO);
        PartDefinition rightWingRoot = root.addOrReplaceChild("right_wing_root",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(2.0F, 4.0F, -3.0F, 0.0F, 0.0F, 0.8F));
        rightWingRoot.addOrReplaceChild("membrane",
                CubeListBuilder.create().texOffs(20, 6)
                        .addBox(0.0F, -0.3F, -2.0F, 5.0F, 0.6F, 4.0F),
                PartPose.ZERO);

        // ── legs : 4 legs, thigh + shin, with CubeDeformation ────────────
        CubeDeformation legRound = new CubeDeformation(0.15F);
        root.addOrReplaceChild("front_left_thigh",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, legRound),
                PartPose.offset(-2.0F, 9.0F, -4.0F));
        root.getChild("front_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 40)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, legRound),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("front_right_thigh",
                CubeListBuilder.create().texOffs(8, 32)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, legRound),
                PartPose.offset(2.0F, 9.0F, -4.0F));
        root.getChild("front_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(8, 40)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, legRound),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("back_left_thigh",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, legRound),
                PartPose.offset(-2.0F, 9.0F, 4.0F));
        root.getChild("back_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 56)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, legRound),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("back_right_thigh",
                CubeListBuilder.create().texOffs(8, 48)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, legRound),
                PartPose.offset(2.0F, 9.0F, 4.0F));
        root.getChild("back_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(8, 56)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, legRound),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(SpiritBeastEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float yaw = netHeadYaw * 0.017453292F;
        float pitch = headPitch * 0.017453292F;
        this.head.yRot = Math.max(-1.0F, Math.min(1.0F, yaw));
        this.head.xRot = Math.max(-0.7F, Math.min(0.7F, pitch));

        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;
        boolean flying = entity.getSpiritPose() == SpiritBeastEntity.POSE_FLYING;
        boolean sprinting = entity.getSpiritPose() == SpiritBeastEntity.POSE_SPRINTING;

        ModelPart[] mane = {this.mane0, this.mane1, this.mane2, this.mane3, this.mane4};

        if (resting) {
            float breath = (float) Math.sin(ageInTicks * 0.08F) * 0.08F;
            this.root.y = -2.0F + breath;
            this.frontLeftThigh.xRot = -0.6F;
            this.frontRightThigh.xRot = -0.6F;
            this.frontLeftShin.xRot = 0.4F;
            this.frontRightShin.xRot = 0.4F;
            this.backLeftThigh.xRot = 0.3F;
            this.backRightThigh.xRot = 0.3F;
            this.backLeftShin.xRot = -0.2F;
            this.backRightShin.xRot = -0.2F;
            this.head.xRot = 0.3F;
            this.jaw.xRot = 0.0F;
            // Mane settles
            for (int i = 0; i < mane.length; i++) {
                mane[i].yScale = 0.8F;
                mane[i].yRot = (float) Math.sin(ageInTicks * 0.1F + i * 0.3F) * 0.05F;
            }
            this.tailBase.xRot = 0.5F;
            // Wings tucked tight
            this.leftWingRoot.zRot = 0.0F;
            this.rightWingRoot.zRot = 0.0F;
            this.leftWingRoot.xRot = -0.8F;
            this.rightWingRoot.xRot = 0.8F;
        } else {
            // ── walk / run / sprint gait ──────────────────────────────
            boolean running = limbSwingAmount > 0.5F;
            float swingPhase = running ? limbSwing * 1.5F : limbSwing;
            float amp = (running ? 1.2F : 0.8F) * limbSwingAmount;
            float phase = swingPhase * 0.6662F;

            if (sprinting) {
                amp = 1.4F * limbSwingAmount;
                this.root.xRot = -0.15F;
            }

            this.frontLeftThigh.xRot  = (float) Math.cos(phase) * amp;
            this.frontRightThigh.xRot = (float) Math.cos(phase + Math.PI) * amp;
            this.backLeftThigh.xRot   = (float) Math.cos(phase + Math.PI) * amp;
            this.backRightThigh.xRot  = (float) Math.cos(phase) * amp;
            this.frontLeftShin.xRot  = -0.3F + Math.max(0.0F, (float) Math.cos(phase)) * 0.6F * limbSwingAmount;
            this.frontRightShin.xRot = -0.3F + Math.max(0.0F, (float) Math.cos(phase + Math.PI)) * 0.6F * limbSwingAmount;
            this.backLeftShin.xRot   = -0.3F + Math.max(0.0F, (float) Math.cos(phase + Math.PI)) * 0.6F * limbSwingAmount;
            this.backRightShin.xRot  = -0.3F + Math.max(0.0F, (float) Math.cos(phase)) * 0.6F * limbSwingAmount;

            // Spine flex
            float spineFlex = (float) Math.sin(phase + Math.PI * 0.5F) * 0.08F * limbSwingAmount;
            this.bodyChest.xRot = spineFlex;
            this.bodyHip.xRot = -spineFlex * 0.5F;

            // Breathing
            this.root.y = (float) Math.sin(ageInTicks * 0.1F) * 0.08F;
            this.neck.xRot = -0.4F + (float) Math.sin(phase) * 0.04F * limbSwingAmount;

            // Tail sway
            this.tailBase.yRot = (float) Math.sin(ageInTicks * 0.2F) * 0.25F;
            this.tailMid.yRot = (float) Math.sin(ageInTicks * 0.2F + 0.4F) * 0.15F;
            this.tailFan.yRot = (float) Math.sin(ageInTicks * 0.2F + 0.8F) * 0.15F;

            // Mane sway
            for (int i = 0; i < mane.length; i++) {
                float p = ageInTicks * 0.5F + i * 0.4F;
                mane[i].yRot = (float) Math.sin(p) * 0.08F;
                mane[i].yScale = 1.0F + (float) Math.sin(p) * 0.05F;
            }

            // Combat head
            boolean combat = entity.getTarget() != null;
            if (combat) {
                this.head.xRot += 0.3F;
                this.jaw.xRot = 0.3F;
            } else {
                this.jaw.xRot = 0.0F;
            }

            // Wings: extend and flap when flying
            if (flying) {
                float flap = (float) Math.sin(ageInTicks * 0.8F) * 0.6F;
                this.leftWingRoot.zRot = -0.5F + flap;
                this.rightWingRoot.zRot = 0.5F - flap;
                this.leftWingRoot.xRot = -0.3F;
                this.rightWingRoot.xRot = 0.3F;
            } else {
                // Wings tucked against body when not flying
                this.leftWingRoot.zRot = 0.0F;
                this.rightWingRoot.zRot = 0.0F;
                this.leftWingRoot.xRot = -0.8F;
                this.rightWingRoot.xRot = 0.8F;
            }
        }

        // ── attack : antler thrust ────────────────────────────────────
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float strike = (float) Math.sin(atk * Math.PI);
            this.head.xRot = -strike * 0.8F;
            this.jaw.xRot = strike * 0.5F;
            this.frontLeftThigh.xRot += strike * 0.3F;
            this.frontRightThigh.xRot += strike * 0.3F;
            this.backLeftThigh.xRot -= strike * 0.2F;
            this.backRightThigh.xRot -= strike * 0.2F;
            // Antlers thrust forward with head
            this.antlerLeftBase.xRot = -strike * 0.3F;
            this.antlerRightBase.xRot = -strike * 0.3F;
        }

        // ── death collapse ──────────────────────────────────────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F);
            float collapse = t * t;
            this.root.xRot = collapse * -0.4F;
            this.root.zRot = collapse * 0.5F;
            this.head.xRot = collapse * 0.7F;
            this.head.zRot = collapse * 0.3F;
            this.frontLeftThigh.zRot = -collapse * 0.4F;
            this.frontRightThigh.zRot = collapse * 0.4F;
            this.backLeftThigh.zRot = -collapse * 0.3F;
            this.backRightThigh.zRot = collapse * 0.3F;
            this.tailBase.xRot = 0.3F + collapse * 1.0F;
            this.jaw.xRot = collapse * 0.5F;
        }
    }
}
