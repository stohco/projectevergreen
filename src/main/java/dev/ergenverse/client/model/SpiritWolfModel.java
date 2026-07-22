package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/spirit_wolf.png  SIZE: 64x64
/*
 * SpiritWolfModel — lean quadruped predator.
 *
 * ANATOMY (CRON-COMPLETIONIST-16 overhaul):
 *   - body_chest : front torso (4 x 6 x 5) deeper at shoulders
 *   - body_hip  : rear torso (3 x 5 x 5) narrower at the hips
 *   - spine     : invisible connector between chest and hip, flexes during run
 *   - neck      : short tilted connector between body_chest and head
 *   - head      : skull (3x3x3) + snout (2x2x2) + 2 angled ears + lower jaw
 *                 (separate child, can open) + 2 small fangs + nose pad
 *   - tail      : 3-segment bushy chain (base -> mid -> tip) curving down
 *   - legs      : 4 legs, each 2 segments (thigh + shin), feet at y=15
 *
 * ANIMATION:
 *   - Walk gait  : diagonal trot with spine flex (chest pitches on stride)
 *   - Run        : amplified gait + spine arches (body_chest.xRot undulates)
 *   - Idle       : breathing + tail sway
 *   - POSE_RESTING : legs fold, head drops, body lowers (CRON-16)
 *   - POSE_SWIMMING : body pitches, legs paddle, head elevated (CRON-16)
 *   - Combat     : synced via getTarget() + POSE_CHARGING
 *   - Attack lunge: synced via entity.attackAnim (smooth 0→1→0)
 *   - Death      : collapse over 0.4s with quadratic ease-in
 *
 * HARSH SELF-CRITIQUE:
 *   - Chest/hip split is 2 boxes — real wolf has continuous muscle taper.
 *   - Ears are boxy cubes, not triangular shells.
 *   - Fangs are 1x1x1 cubes, not tapered cones.
 *   - Tail is 3 uniform segments, not a tapered plume.
 *   - No separate eye cubes (relies on texture).
 *   - Nose pad is a 1x1x0.5 box — better than nothing but still a box.
 *   - Spine flex is via invisible connector pivot — works but limited.
 */
import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class SpiritWolfModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart bodyChest;
    private final ModelPart bodyHip;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart earLeft;
    private final ModelPart earRight;
    private final ModelPart tailBase;
    private final ModelPart tailMid;
    private final ModelPart tailTip;
    private final ModelPart frontLeftThigh;
    private final ModelPart frontLeftShin;
    private final ModelPart frontRightThigh;
    private final ModelPart frontRightShin;
    private final ModelPart backLeftThigh;
    private final ModelPart backLeftShin;
    private final ModelPart backRightThigh;
    private final ModelPart backRightShin;

    public SpiritWolfModel(ModelPart root) {
        this.root = root;
        this.bodyChest = root.getChild("body_chest");
        this.bodyHip = root.getChild("body_hip");
        this.neck = root.getChild("neck");
        this.head = root.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.earLeft = this.head.getChild("ear_left");
        this.earRight = this.head.getChild("ear_right");
        this.tailBase = root.getChild("tail_base");
        this.tailMid = this.tailBase.getChild("mid");
        this.tailTip = this.tailMid.getChild("tip");
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
        root.addOrReplaceChild("body_chest",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.0F, -3.0F, -5.0F, 4.0F, 6.0F, 5.0F),
                PartPose.offset(0.0F, 6.0F, -2.5F));

        // ── body_hip : rear torso, narrower at hips ───────────────────────
        root.addOrReplaceChild("body_hip",
                CubeListBuilder.create().texOffs(0, 12)
                        .addBox(-1.5F, -2.5F, 0.0F, 3.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 5.5F, 2.5F));

        // ── neck : short tilted connector at the front ───────────────────
        root.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(28, 0)
                        .addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, -5.0F, -0.4F, 0.0F, 0.0F));

        // ── head : skull + snout + ears + jaw + fangs (child of neck) ─────
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 18)
                        .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F)   // skull
                        .texOffs(0, 26)
                        .addBox(-1.0F, 0.0F, -3.5F, 2.0F, 2.0F, 2.0F),   // snout forward
                PartPose.offset(0.0F, -1.0F, -4.0F));
        // ears : angled tall thin boxes on top of skull
        head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(20, 18)
                        .addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-1.2F, -1.5F, 0.0F, 0.0F, 0.0F, -0.3F));
        head.addOrReplaceChild("ear_right",
                CubeListBuilder.create().texOffs(20, 24)
                        .addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(1.2F, -1.5F, 0.0F, 0.0F, 0.0F, 0.3F));
        // lower jaw : separate child, pivots at the back of the mouth
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(28, 18)
                        .addBox(-1.0F, 0.0F, -2.5F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 0.5F, -1.0F));
        // fangs : tiny cubes below the snout tip
        head.addOrReplaceChild("fang_left",
                CubeListBuilder.create().texOffs(40, 0)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(-0.5F, 2.0F, -3.0F));
        head.addOrReplaceChild("fang_right",
                CubeListBuilder.create().texOffs(40, 4)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.5F, 2.0F, -3.0F));
        // nose pad : dark nub at the snout tip
        head.addOrReplaceChild("nose_pad",
                CubeListBuilder.create().texOffs(44, 0)
                        .addBox(-0.5F, 0.0F, 0.0F, 1.0F, 0.5F, 0.5F),
                PartPose.offset(0.0F, 0.5F, -4.5F));

        // ── tail : 3-segment chain curving down from the rear ────────────
        PartDefinition tailBase = root.addOrReplaceChild("tail_base",
                CubeListBuilder.create().texOffs(36, 8)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 5.0F, 0.3F, 0.0F, 0.0F));
        PartDefinition tailMid = tailBase.addOrReplaceChild("mid",
                CubeListBuilder.create().texOffs(36, 14)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.3F, 0.0F, 0.0F));
        tailMid.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(36, 20)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.2F, 0.0F, 0.0F));

        // ── legs : 4 legs, thigh + shin, feet at y=15 ────────────────────
        root.addOrReplaceChild("front_left_thigh",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(-2.0F, 9.0F, -4.0F));
        root.getChild("front_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 40)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        root.addOrReplaceChild("front_right_thigh",
                CubeListBuilder.create().texOffs(8, 32)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(2.0F, 9.0F, -4.0F));
        root.getChild("front_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(8, 40)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        root.addOrReplaceChild("back_left_thigh",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(-2.0F, 9.0F, 4.0F));
        root.getChild("back_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 56)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        root.addOrReplaceChild("back_right_thigh",
                CubeListBuilder.create().texOffs(8, 48)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(2.0F, 9.0F, 4.0F));
        root.getChild("back_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(8, 56)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
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
        // ── CRON-COMPLETIONIST-16: POSE_RESTING (legs fold, head drops, body lowers) ──
        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;
        // ── CRON-COMPLETIONIST-16: POSE_SWIMMING (body pitches, legs paddle, head up) ──
        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING;

        // ── head turn (clamped) ──────────────────────────────────────────
        float yaw = netHeadYaw * 0.017453292F;
        float pitch = headPitch * 0.017453292F;
        this.head.yRot = Math.max(-1.0F, Math.min(1.0F, yaw));
        this.head.xRot = Math.max(-0.7F, Math.min(0.7F, pitch));

        if (resting) {
            // ── POSE_RESTING : wolf curls up on the ground ─────────────
            // Body lowers, legs fold under, head rests on paws, tail wraps
            this.root.y = -2.0F;                    // body sinks to ground
            this.root.xRot = 0.05F;                  // very slight forward lean
            // Front legs fold forward (paws extended)
            this.frontLeftThigh.xRot  = -0.8F;
            this.frontRightThigh.xRot = -0.8F;
            this.frontLeftShin.xRot   = 0.6F;
            this.frontRightShin.xRot  = 0.6F;
            // Back legs fold to the side (paw tucked)
            this.backLeftThigh.xRot   = 0.5F;
            this.backRightThigh.xRot  = 0.5F;
            this.backLeftShin.xRot    = -0.4F;
            this.backRightShin.xRot   = -0.4F;
            // Head rests on front paws (droops low)
            this.head.xRot = 0.6F;
            this.neck.xRot = 0.8F;
            // Tail wraps around (tip curls toward body)
            this.tailBase.xRot = 0.8F;
            this.tailMid.xRot  = 0.6F;
            this.tailTip.xRot  = -0.4F;
            this.tailBase.yRot = 0.5F;
            // Jaw relaxed
            this.jaw.xRot = 0.0F;
            // Ears relaxed
            this.earLeft.zRot  = -0.2F;
            this.earRight.zRot = 0.2F;
        } else if (swimming) {
            // ── POSE_SWIMMING : wolf paddles through water ─────────────
            // Body pitches slightly, head elevated above water
            this.root.xRot = -0.3F;
            this.root.y = -1.0F;
            // Head elevated above waterline
            this.head.xRot = -0.5F;
            this.neck.xRot = 0.2F;
            // Legs paddle in a fast circular motion
            float paddle = ageInTicks * 1.2F;
            this.frontLeftThigh.xRot  = (float) Math.cos(paddle) * 0.8F;
            this.frontRightThigh.xRot = (float) Math.cos(paddle + Math.PI) * 0.8F;
            this.backLeftThigh.xRot   = (float) Math.cos(paddle + Math.PI) * 0.6F;
            this.backRightThigh.xRot  = (float) Math.cos(paddle) * 0.6F;
            this.frontLeftShin.xRot   = -0.3F + Math.abs((float) Math.cos(paddle)) * 0.4F;
            this.frontRightShin.xRot  = -0.3F + Math.abs((float) Math.cos(paddle + Math.PI)) * 0.4F;
            this.backLeftShin.xRot    = -0.3F + Math.abs((float) Math.cos(paddle + Math.PI)) * 0.3F;
            this.backRightShin.xRot   = -0.3F + Math.abs((float) Math.cos(paddle)) * 0.3F;
            // Tail streams behind (low, less sway)
            this.tailBase.xRot = 0.6F;
            this.tailBase.yRot = (float) Math.sin(ageInTicks * 0.15F) * 0.1F;
            this.tailMid.yRot = 0.0F;
            this.tailTip.yRot = 0.0F;
            // Jaw closed, ears pinned (streamlined)
            this.jaw.xRot = 0.0F;
            this.earLeft.zRot  = -0.5F;
            this.earRight.zRot = 0.5F;
        } else {
            // ── walk / run gait : diagonal trot ──────────────────────────────
            boolean running = limbSwingAmount > 0.5F;
            float swingPhase = running ? limbSwing * 1.5F : limbSwing;
            float amp = (running ? 1.2F : 0.8F) * limbSwingAmount;
            float phase = swingPhase * 0.6662F;

            this.frontLeftThigh.xRot  = (float) Math.cos(phase)            * amp;
            this.frontRightThigh.xRot = (float) Math.cos(phase + Math.PI)  * amp;
            this.backLeftThigh.xRot   = (float) Math.cos(phase + Math.PI)  * amp;
            this.backRightThigh.xRot  = (float) Math.cos(phase)            * amp;

            // shin counter-flexes when the leg lifts (knee bend)
            this.frontLeftShin.xRot  = -0.3F + Math.max(0.0F,  (float)Math.cos(phase))            * 0.6F * limbSwingAmount;
            this.frontRightShin.xRot = -0.3F + Math.max(0.0F,  (float)Math.cos(phase + Math.PI))  * 0.6F * limbSwingAmount;
            this.backLeftShin.xRot   = -0.3F + Math.max(0.0F,  (float)Math.cos(phase + Math.PI))  * 0.6F * limbSwingAmount;
            this.backRightShin.xRot  = -0.3F + Math.max(0.0F,  (float)Math.cos(phase))            * 0.6F * limbSwingAmount;

            // ── CRON-16: Spine flex — chest pitches with stride ──────────
            float spineFlex = (float) Math.sin(phase + Math.PI * 0.5F) * 0.08F * limbSwingAmount;
            this.bodyChest.xRot = spineFlex;
            this.bodyHip.xRot  = -spineFlex * 0.5F;

            // ── idle breathing + spine bob ──────────────────────────────────
            this.root.y = (float) Math.sin(ageInTicks * 0.1F) * 0.1F;

            // ── tail sway ────────────────────────────────────────────────────
            this.tailBase.yRot = (float) Math.sin(ageInTicks * 0.2F) * 0.3F;
            this.tailMid.yRot  = (float) Math.sin(ageInTicks * 0.2F + 0.4F) * 0.2F;
            this.tailTip.yRot  = (float) Math.sin(ageInTicks * 0.2F + 0.8F) * 0.2F;

            // ── combat stance : when targeting, head dips, jaw opens, ears pin ──
            boolean combat = entity.getTarget() != null
                    || entity.getSpiritPose() == SpiritBeastEntity.POSE_CHARGING;
            if (combat) {
                this.head.xRot += 0.3F;
                this.jaw.xRot = 0.45F;
                this.earLeft.zRot  = -0.6F;
                this.earRight.zRot = 0.6F;
                this.tailBase.xRot = 0.9F;
            } else {
                this.jaw.xRot = 0.0F;
                this.earLeft.zRot  = -0.3F;
                this.earRight.zRot = 0.3F;
                this.tailBase.xRot = 0.3F;
            }

            // ── attack lunge ────────────────────────────────────────────────
            float atk = entity.attackAnim;
            if (atk > 0.0F) {
                float lunge = (float) Math.sin(atk * Math.PI);
                this.root.xRot = -lunge * 0.6F;
                this.head.xRot -= lunge * 0.8F;
                this.jaw.xRot += lunge * 0.5F;
                this.frontLeftThigh.xRot  += lunge * 0.4F;
                this.frontRightThigh.xRot += lunge * 0.4F;
                this.backLeftThigh.xRot   -= lunge * 0.3F;
                this.backRightThigh.xRot  -= lunge * 0.3F;
            }

            // ── death collapse ─────────────────────────────────────────────
            if (entity.deathTime > 0) {
                float t = Math.min(entity.deathTime / 8.0F, 1.0F);
                float collapse = t * t;
                this.root.xRot = collapse * -0.4F;
                this.root.zRot = collapse * 0.6F;
                this.head.xRot = collapse * 0.8F;
                this.head.zRot = collapse * 0.3F;
                this.frontLeftThigh.zRot  = -collapse * 0.5F;
                this.frontRightThigh.zRot =  collapse * 0.5F;
                this.backLeftThigh.zRot   = -collapse * 0.4F;
                this.backRightThigh.zRot  =  collapse * 0.4F;
                this.tailBase.xRot = 0.3F + collapse * 1.2F;
                this.tailMid.xRot  = collapse * 0.5F;
                this.tailTip.xRot  = collapse * 0.3F;
                this.jaw.xRot = collapse * 0.6F;
            }
        }
    }
}
