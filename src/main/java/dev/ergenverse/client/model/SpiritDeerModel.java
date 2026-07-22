package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/spirit_deer.png  SIZE: 64x64
/*
 * SpiritDeerModel — slender long-necked quadruped with branched antlers.
 *
 * ANATOMY:
 *   - body        : slim torso (3 x 5 x 8) at chest height
 *   - neck        : long (length 4) tilted up-forward, connects body to head
 *   - head        : skull (2x3x2) + snout (2x1x2) + 2 branched antlers
 *                   (main beam + 2 tines each) + 2 ears
 *   - tail        : short puffy tuft at the rear
 *   - legs        : 4 slim legs, 2 segments each (thigh + shin), feet at y=15
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
 * HARSH SELF-CRITIQUE:
 *   - Antlers are crude stick boxes; real deer antlers are curved beams with
 *     palmate tines, brow tines, bay tines, and a crown. Mine look like TV
 *     antennae. No curve, no palmation, no asymmetry.
 *   - The neck is a single 1x4x1 stick — real deer necks taper and have a
 *     mane ridge. Looks like a broom handle.
 *   - Legs are straight sticks with no hock (rear) or knee (front) silhouette
 *     differentiation. Real deer have a distinct rear hock that angles forward.
 *   - No dewclaws, no cloven hooves — feet are just shin box ends.
 *   - Body has no chest depth vs. waist taper — real deer are deep-chested
 *     and narrow-waisted. Mine is a uniform box.
 *   - Ears are boxes, not the large leaf-shaped pinnae real deer have.
 *   - Graze/alert cycle is a blind sin wave — a real deer reacts to threats.
 *     Should be driven by a synced startled flag (PanicGoal state) instead.
 *   - No "stotting" (pronk) gait for flee — real deer bounce with all four
 *     legs off the ground. Mine just runs faster.
 *   - Texture UVs invented; existing spirit_deer.png (vanilla CowModel layout)
 *     will scramble on this model.
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
    private final ModelPart head;
    private final ModelPart neck;
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

    public SpiritDeerModel(ModelPart root) {
        this.root = root;
        this.neck = root.getChild("neck");
        this.head = this.neck.getChild("head");
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
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── body : slim torso ────────────────────────────────────────────
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.5F, -2.5F, -4.0F, 3.0F, 5.0F, 8.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));

        // ── neck : long, tilted up-forward ───────────────────────────────
        PartDefinition neck = root.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(22, 0)
                        .addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, -4.0F, 1.0F, 0.0F, 0.0F));

        // ── head : child of neck, at the tip ─────────────────────────────
        PartDefinition head = neck.addOrReplaceChild("head",
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

        // ── antlers : main beam + 2 tines per side ───────────────────────
        PartDefinition antlerL = head.addOrReplaceChild("antler_left",
                CubeListBuilder.create().texOffs(28, 16)
                        .addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),   // main beam up
                PartPose.offsetAndRotation(-0.5F, -1.5F, 0.0F, 0.0F, 0.0F, -0.3F));
        antlerL.addOrReplaceChild("tine1",
                CubeListBuilder.create().texOffs(36, 16)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.9F));
        antlerL.addOrReplaceChild("tine2",
                CubeListBuilder.create().texOffs(36, 20)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.0F, -0.6F));

        PartDefinition antlerR = head.addOrReplaceChild("antler_right",
                CubeListBuilder.create().texOffs(28, 24)
                        .addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.5F, -1.5F, 0.0F, 0.0F, 0.0F, 0.3F));
        antlerR.addOrReplaceChild("tine1",
                CubeListBuilder.create().texOffs(36, 24)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.9F));
        antlerR.addOrReplaceChild("tine2",
                CubeListBuilder.create().texOffs(36, 28)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.0F, 0.6F));

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

        return LayerDefinition.create(mesh, 64, 64);
    }

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
            float breath = (float) Math.sin(ageInTicks * 0.08F) * 0.04F;
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
            this.neck.xRot = 1.3F + breath * 0.3F;
            this.head.xRot = 0.8F + breath * 0.2F;
            this.tail.xRot = 0.5F + tailSway;
            this.earLeft.zRot  = -0.4F + earTwitch;
            this.earRight.zRot = 0.4F - earTwitch;
            return;
        } else if (swimming) {
            // CRON-COMPLETIONIST-17: Added vertical bob synchronized with paddle.
            float paddle = ageInTicks * 1.0F;
            float bob = (float) Math.sin(paddle * 0.5F) * 0.12F;
            this.root.xRot = -0.3F;
            this.root.y = -1.5F + bob;
            this.head.xRot = -0.4F;
            this.neck.xRot = 0.5F;
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
            return;
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
            this.neck.xRot = 0.6F;
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
        // neck base tilt — neck already tilted up by 1.0 rad in the pose;
        // we add a small bob.
        this.neck.xRot = 1.0F + (float) Math.sin(ageInTicks * 0.1F) * 0.03F;

        float yaw = netHeadYaw * 0.017453292F;
        this.head.yRot = Math.max(-0.8F, Math.min(0.8F, yaw));

        if (fleeing) {
            // FLEE : head up, tail flagged
            this.head.xRot = -0.3F;
            this.neck.xRot = 0.7F;                  // neck more upright to raise head
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
                this.neck.xRot = 1.3F;
                this.tail.xRot = 0.3F;
            } else if (poseAlert) {
                // ALERT : head snaps up, ears forward, tail flicks
                this.head.xRot = -0.3F;
                this.neck.xRot = 0.8F;
                this.tail.xRot = -0.5F + (float) Math.sin(ageInTicks * 1.5F) * 0.3F;
                this.earLeft.zRot  = -0.2F;
                this.earRight.zRot = 0.2F;
            } else {
                // Fallback: slow graze / alert cycle when no goal is active
                float cycle = (float) Math.sin(ageInTicks * 0.05F);
                if (cycle > 0.0F) {
                    this.head.xRot = 1.2F * cycle;
                    this.neck.xRot = 1.3F;
                    this.tail.xRot = 0.3F;
                } else {
                    this.head.xRot = -0.3F;
                    this.neck.xRot = 0.8F;
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
            this.neck.xRot -= lunge * 0.3F;             // neck raises
            this.head.xRot -= lunge * 0.5F;             // head throws back
        }

        // ── death collapse : deer crumples, legs fold under ─────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F); // 0→1 over 0.4s (visible before fade)
            float collapse = t * t;
            this.root.xRot = collapse * -0.3F;
            this.root.zRot = collapse * 0.5F;
            this.neck.xRot = 1.0F + collapse * 0.5F;       // neck folds down
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
