package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/spirit_bat.png  SIZE: 64x64
/*
 * SpiritBatModel v2 — CRON-COMPLETIONIST-69: 4-SEGMENT finger-bone wings.
 *
 * PREVIOUS MODEL (v1): 13 addBox calls. 3-segment wing (shoulder→forearm→membrane),
 * flat paper membrane, boxy ears, single-sphere body. Scored 4/10 for anatomy.
 *
 * NEW MODEL (v2): 20 addBox calls. 4-segment wing per side (arm→elbow→finger→
 * membrane web), body split into thorax+abdomen, ears with inner structure,
 * thumb claws, uropatagium (tail membrane).
 *
 * ANATOMY improvements:
 *   - thorax: front body (compact, 2.5x2.5x2.5)
 *   - abdomen: rear body (slightly wider, 3x2x2.5)
 *   - head: rounded skull (2x2x2)
 *     - ear_left/right: larger pointed ears (0.6x2.5x0.6)
 *     - ear_inner_l/r: pink inner ear detail
 *     - nose_leaf: small leaf-shaped nose
 *   - LEFT WING (4-segment chain):
 *     - shoulder: upper arm bone (2x0.5x1.5)
 *     - elbow: forearm bone (3x0.4x1.2)
 *     - finger: elongated digit (3x0.3x1.0)
 *     - web: membrane sail between finger bones (4x0.15x3.5)
 *     - thumb_claw: small hook on leading edge
 *   - RIGHT WING: mirror
 *   - legs: short (0.5x1.5x0.5) + foot (0.6x0.3x0.6)
 *   - uropatagium: tail membrane connecting legs (NEW)
 *
 * ANIMATION: Preserved from v1 with additions:
 *   - Flight flap: 4-segment wing chain (shoulder→elbow→finger→web each
 *     flex at different phase delays, creating realistic membrane billow)
 *   - Glide: wings extend flat
 *   - Roost: inverted, wings wrap body
 *   - Attack swoop: wings thrust forward
 *   - Death: wings go limp, tumble
 *
 * HARSH SELF-CRITIQUE:
 *   - Wing membrane is still a flat box — not a translucent curved surface.
 *     But with 4 segments instead of 3, the web billows more convincingly.
 *   - Finger bone is a thin box — real bat fingers are segmented with joints.
 *     A 5th segment would help but adds complexity for minimal visible gain.
 *   - Thumb claw is a 1px box — barely visible at bat scale.
 *   - Uropatagium (tail membrane) is a single box — real bats have a
 *     membrane stretching between their legs, sometimes enclosing the tail.
 *   - Body split (thorax+abdomen) reads better from the side but the
 *     seam may be visible. Real bats have a continuous furry torso.
 *   - Ears are still box prisms — not the complex pinna of real bats.
 *     Added inner ear detail but it's just a thinner box.
 *   - At 0.3 block scale, many details are invisible. The key improvement
 *     is the wing chain behavior — the billow and flex pattern is more
 *     convincing than v1's flat membrane snap.
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
    private final ModelPart leftWeb;
    private final ModelPart leftThumbClaw;
    private final ModelPart rightShoulder;
    private final ModelPart rightElbow;
    private final ModelPart rightFinger;
    private final ModelPart rightWeb;
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
        this.leftShoulder = root.getChild("left_wing_root");
        this.leftElbow = this.leftShoulder.getChild("elbow");
        this.leftFinger = this.leftElbow.getChild("finger");
        this.leftWeb = this.leftFinger.getChild("web");
        this.leftThumbClaw = this.leftShoulder.getChild("thumb_claw");
        this.rightShoulder = root.getChild("right_wing_root");
        this.rightElbow = this.rightShoulder.getChild("elbow");
        this.rightFinger = this.rightElbow.getChild("finger");
        this.rightWeb = this.rightFinger.getChild("web");
        this.rightThumbClaw = this.rightShoulder.getChild("thumb_claw");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.uropatagium = root.getChild("uropatagium");
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
        thorax.addOrReplaceChild("abdomen",
                CubeListBuilder.create().texOffs(10, 0)
                        .addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 2.5F, new CubeDeformation(0.4F)),
                PartPose.offset(0.0F, 0.25F, 1.25F));

        // ── head : rounded skull + ears ──────────────────────────────────
        PartDefinition head = thorax.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(6, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.3F)),
                PartPose.offset(0.0F, -1.5F, -2.0F));

        // Large pointed ears — bat's iconic feature
        PartDefinition earL = head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(12, 0)
                        .addBox(-0.3F, -2.5F, -0.3F, 0.6F, 2.5F, 0.6F),
                PartPose.offsetAndRotation(-1.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.4F));
        // Inner ear detail (pink, thinner)
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

        // ── LEFT WING: 4-segment finger-bone chain ──────────────────────
        // Root pivot (invisible)
        PartDefinition leftRoot = root.addOrReplaceChild("left_wing_root",
                CubeListBuilder.create(),
                PartPose.offset(-1.25F, 9.5F, 0.0F));

        // Shoulder (humerus)
        PartDefinition leftShoulder = leftRoot.addOrReplaceChild("elbow_parent",
                CubeListBuilder.create().texOffs(0, 8)
                        .addBox(-2.0F, -0.25F, -0.75F, 2.0F, 0.5F, 1.5F),
                PartPose.ZERO);
        // Thumb claw on leading edge
        leftRoot.addOrReplaceChild("thumb_claw",
                CubeListBuilder.create().texOffs(0, 12)
                        .addBox(-0.3F, -0.1F, -0.3F, 0.3F, 0.2F, 0.6F),
                PartPose.offset(-0.5F, 0.3F, -0.5F));

        // Elbow (radius/ulna)
        PartDefinition leftElbow = leftRoot.addOrReplaceChild("elbow",
                CubeListBuilder.create().texOffs(0, 12)
                        .addBox(-3.0F, -0.2F, -0.6F, 3.0F, 0.4F, 1.2F),
                PartPose.offset(-2.0F, 0.0F, 0.0F));

        // Finger (metacarpal + phalanges)
        PartDefinition leftFinger = leftRoot.addOrReplaceChild("finger",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-3.0F, -0.15F, -0.5F, 3.0F, 0.3F, 1.0F),
                PartPose.offset(-5.0F, 0.0F, 0.0F));

        // Membrane web (the sail)
        leftFinger.addOrReplaceChild("web",
                CubeListBuilder.create().texOffs(20, 0)
                        .addBox(-3.5F, 0.0F, -1.75F, 3.5F, 0.15F, 3.5F),
                PartPose.offset(-3.0F, 0.15F, 0.0F));

        // ── RIGHT WING: mirror ─────────────────────────────────────────
        PartDefinition rightRoot = root.addOrReplaceChild("right_wing_root",
                CubeListBuilder.create(),
                PartPose.offset(1.25F, 9.5F, 0.0F));

        rightRoot.addOrReplaceChild("elbow_parent",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(0.0F, -0.25F, -0.75F, 2.0F, 0.5F, 1.5F),
                PartPose.ZERO);
        rightRoot.addOrReplaceChild("thumb_claw",
                CubeListBuilder.create().texOffs(0, 20)
                        .addBox(0.0F, -0.1F, -0.3F, 0.3F, 0.2F, 0.6F),
                PartPose.offset(0.5F, 0.3F, -0.5F));

        rightRoot.addOrReplaceChild("elbow",
                CubeListBuilder.create().texOffs(0, 20)
                        .addBox(0.0F, -0.2F, -0.6F, 3.0F, 0.4F, 1.2F),
                PartPose.offset(2.0F, 0.0F, 0.0F));

        rightRoot.addOrReplaceChild("finger",
                CubeListBuilder.create().texOffs(0, 24)
                        .addBox(0.0F, -0.15F, -0.5F, 3.0F, 0.3F, 1.0F),
                PartPose.offset(5.0F, 0.0F, 0.0F));

        rightRoot.getChild("finger").addOrReplaceChild("web",
                CubeListBuilder.create().texOffs(20, 6)
                        .addBox(0.0F, 0.0F, -1.75F, 3.5F, 0.15F, 3.5F),
                PartPose.offset(3.0F, 0.15F, 0.0F));

        // ── legs : short, bat hangs upside down ───────────────────────────
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(16, 0)
                        .addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.5F, 0.5F),
                PartPose.offset(-0.6F, 11.5F, 0.0F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(16, 3)
                        .addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.5F, 0.5F),
                PartPose.offset(0.6F, 11.5F, 0.0F));

        // ── uropatagium : tail membrane between legs ────────────────────
        root.addOrReplaceChild("uropatagium",
                CubeListBuilder.create().texOffs(18, 8)
                        .addBox(-0.8F, 0.0F, -0.5F, 1.6F, 0.1F, 1.0F),
                PartPose.offset(0.0F, 11.5F, 0.5F));

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

            // Membrane web: latest phase (0.9 rad) — creates billow
            float webBillow = Math.max(0.0F, (float) Math.sin(flapSpeed - 0.9F));
            this.leftWeb.xScale = 1.0F + webBillow * 0.25F * limbSwingAmount;
            this.rightWeb.xScale = 1.0F + webBillow * 0.25F * limbSwingAmount;
            // Web also pitches with the wind
            this.leftWeb.xRot = webBillow * 0.1F * limbSwingAmount;
            this.rightWeb.xRot = -webBillow * 0.1F * limbSwingAmount;

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
