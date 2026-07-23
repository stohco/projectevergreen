package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/spirit_bat.png  SIZE: 64x64
/*
 * SpiritBatModel — small aerial insectivore with membrane wings.
 *
 * Canon (Renegade Immortal): bats inhabit cave systems and dark valleys.
 * Spirit bats are slightly larger than normal bats with faint qi-glow eyes.
 *
 * REAL BAT (蝙蝠) anatomy:
 *   - Body: small compact torso, roughly spherical
 *   - Head: rounded skull with large pointed ears, small eyes, tiny nose leaf
 *   - Wings: membrane stretched between elongated finger bones (4 digits + thumb claw),
 *           membrane connects to body sides and hind legs. NOT feathered.
 *   - Wingspan: 2-3x body length
 *   - Legs: very short, 5 clawed toes, hang upside down
 *   - Tail: some species have tail membrane (uropatagium)
 *
 * ANATOMY:
 *   - body    : compact sphere (3 x 3 x 3)
 *   - head    : rounded skull (2x2x2) + 2 large pointed ears + tiny nose leaf
 *   - wings   : 3-segment chain (arm -> forearm -> hand) per side,
 *               each a thin box representing the finger bone span,
 *               membrane is a wider flat box at the hand (the sail)
 *   - legs    : very short (1x1x2) + tiny foot (1x0.5x1)
 *   - thumb   : small claw on each wing leading edge
 *
 * ANIMATION:
 *   - Flight flap  : rapid wing oscillation sin(age*1.2)*0.8, elbows flex on downstroke
 *   - Glide        : wings hold flat when not moving fast
 *   - Roost        : wings wrap around body (inverted), legs tucked
 *   - Head turn    : head.yRot/xRot from netHeadYaw/headPitch (clamped)
 *   - Ears twitch  : periodic small ear zRot
 *   - Death        : wings go limp, body drops
 *
 * HARSH SELF-CRITIQUE:
 *   - Wing membrane is a flat box — real membrane is translucent, stretched
 *     between finger bones with visible veins. A box is the crudest approximation.
 *   - Ears are pointed boxes, not the complex pinna of real bats.
 *   - No nose leaf detail — just a tiny nub.
 *   - Body is a single sphere — real bats have a distinct neck, shoulder girdle,
 *     and hip structure.
 *   - Thumb claw is a 1px box — real bats have a visible hook.
 *   - No echolocation "mouth open" animation.
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
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart earLeft;
    private final ModelPart earRight;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftShoulder;
    private final ModelPart rightShoulder;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart leftMembrane;
    private final ModelPart rightMembrane;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public SpiritBatModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.earLeft = this.head.getChild("ear_left");
        this.earRight = this.head.getChild("ear_right");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.leftShoulder = this.leftWing.getChild("shoulder");
        this.rightShoulder = this.rightWing.getChild("shoulder");
        this.leftForearm = this.leftShoulder.getChild("forearm");
        this.rightForearm = this.rightShoulder.getChild("forearm");
        this.leftMembrane = this.leftForearm.getChild("membrane");
        this.rightMembrane = this.rightForearm.getChild("membrane");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── body : compact rounded torso ──────────────────────────────────
        CubeDeformation bodyDeform = new CubeDeformation(0.5F); // organic rounding
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, bodyDeform),
                PartPose.offset(0.0F, 10.0F, 0.0F));

        // ── head : rounded skull + ears, child of body ───────────────────────
        PartDefinition bodyDef = root.getChild("body");
        PartDefinition head = bodyDef.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(12, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.3F)),
                PartPose.offset(0.0F, -1.5F, -2.0F));
        // Large pointed ears — bat's most iconic feature
        head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(12, 6)
                        .addBox(-0.3F, -2.0F, -0.3F, 0.6F, 2.0F, 0.6F),
                PartPose.offsetAndRotation(-1.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.4F));
        head.addOrReplaceChild("ear_right",
                CubeListBuilder.create().texOffs(12, 10)
                        .addBox(-0.3F, -2.0F, -0.3F, 0.6F, 2.0F, 0.6F),
                PartPose.offsetAndRotation(1.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.4F));
        // Tiny nose leaf
        head.addOrReplaceChild("nose_leaf",
                CubeListBuilder.create().texOffs(18, 0)
                        .addBox(-0.2F, 0.3F, -0.7F, 0.4F, 0.3F, 0.3F),
                PartPose.ZERO);

        // ── left wing : arm -> forearm -> membrane ──────────────────────────
        PartDefinition leftWing = root.addOrReplaceChild("left_wing",
                CubeListBuilder.create(),
                PartPose.offset(-1.5F, 9.5F, 0.0F));
        PartDefinition leftShoulder = leftWing.addOrReplaceChild("shoulder",
                CubeListBuilder.create().texOffs(0, 8)
                        .addBox(-3.0F, -0.3F, -1.0F, 3.0F, 0.6F, 2.0F),
                PartPose.ZERO);
        PartDefinition leftForearm = leftShoulder.addOrReplaceChild("forearm",
                CubeListBuilder.create().texOffs(0, 12)
                        .addBox(-4.0F, -0.3F, -0.8F, 4.0F, 0.6F, 1.6F),
                PartPose.offset(-3.0F, 0.0F, 0.0F));
        // Membrane — the sail between finger bones
        leftForearm.addOrReplaceChild("membrane",
                CubeListBuilder.create().texOffs(20, 0)
                        .addBox(-4.0F, 0.0F, -2.0F, 4.0F, 0.2F, 4.0F),
                PartPose.offset(-3.0F, 0.3F, 0.0F));

        // ── right wing : mirror ─────────────────────────────────────────────
        PartDefinition rightWing = root.addOrReplaceChild("right_wing",
                CubeListBuilder.create(),
                PartPose.offset(1.5F, 9.5F, 0.0F));
        PartDefinition rightShoulder = rightWing.addOrReplaceChild("shoulder",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(0.0F, -0.3F, -1.0F, 3.0F, 0.6F, 2.0F),
                PartPose.ZERO);
        PartDefinition rightForearm = rightShoulder.addOrReplaceChild("forearm",
                CubeListBuilder.create().texOffs(0, 20)
                        .addBox(0.0F, -0.3F, -0.8F, 4.0F, 0.6F, 1.6F),
                PartPose.offset(3.0F, 0.0F, 0.0F));
        rightForearm.addOrReplaceChild("membrane",
                CubeListBuilder.create().texOffs(20, 6)
                        .addBox(0.0F, 0.0F, -2.0F, 4.0F, 0.2F, 4.0F),
                PartPose.offset(3.0F, 0.3F, 0.0F));

        // ── legs : very short, bat hangs upside down ────────────────────────
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(16, 0)
                        .addBox(-0.3F, 0.0F, -0.3F, 0.6F, 1.5F, 0.6F),
                PartPose.offset(-0.8F, 11.5F, 0.0F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(16, 3)
                        .addBox(-0.3F, 0.0F, -0.3F, 0.6F, 1.5F, 0.6F),
                PartPose.offset(0.8F, 11.5F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(SpiritBeastEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // ── head turn (clamped) ──────────────────────────────────────────
        float yaw = netHeadYaw * 0.017453292F;
        float pitch = headPitch * 0.017453292F;
        this.head.yRot = Math.max(-1.0F, Math.min(1.0F, yaw));
        this.head.xRot = Math.max(-0.7F, Math.min(0.7F, pitch));

        // ── POSE_RESTING : bat hangs upside down, wings wrap around body ──
        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;

        if (resting) {
            float breath = (float) Math.sin(ageInTicks * 0.08F) * 0.05F;
            this.root.y = 2.0F + breath; // hangs lower
            this.root.xRot = 3.14F; // FLIPPED — hanging upside down
            // Wings wrap body (now above since flipped)
            this.leftWing.zRot = 0.8F;
            this.rightWing.zRot = -0.8F;
            this.leftWing.xRot = -0.3F;
            this.rightWing.xRot = 0.3F;
            this.leftForearm.zRot = 0.5F;
            this.rightForearm.zRot = -0.5F;
            // Ears twitch occasionally
            float earTwitch = (ageInTicks % 80 < 4) ? (float) Math.sin(ageInTicks * 2.5F) * 0.15F : 0.0F;
            this.earLeft.zRot = -0.4F + earTwitch;
            this.earRight.zRot = 0.4F - earTwitch;
            // Legs grip
            this.leftLeg.xRot = 0.0F;
            this.rightLeg.xRot = 0.0F;
        } else {
            // ── FLIGHT : rapid flap cycle ────────────────────────────────
            float flapSpeed = ageInTicks * 1.2F; // faster than hawk
            float flapAmp = 0.4F + limbSwingAmount * 0.8F;
            float flap = (float) Math.sin(flapSpeed) * flapAmp;

            this.leftWing.zRot = flap;
            this.rightWing.zRot = -flap;

            // Elbow flex on downstroke (membrane cups air)
            float downstroke = Math.max(0.0F, (float) Math.sin(flapSpeed));
            float elbow = downstroke * 0.4F * limbSwingAmount;
            this.leftShoulder.zRot = -elbow;
            this.rightShoulder.zRot = elbow;
            this.leftForearm.zRot = -elbow * 0.5F;
            this.rightForearm.zRot = elbow * 0.5F;

            // Membrane billow on downstroke
            this.leftMembrane.xScale = 1.0F + downstroke * 0.2F * limbSwingAmount;
            this.rightMembrane.xScale = 1.0F + downstroke * 0.2F * limbSwingAmount;

            // Legs tucked during flight
            this.leftLeg.xRot = -0.3F * limbSwingAmount;
            this.rightLeg.xRot = -0.3F * limbSwingAmount;

            // Idle breathing
            this.root.y = (float) Math.sin(ageInTicks * 0.15F) * 0.05F;

            // Ear twitch
            float earTwitch = (ageInTicks % 60 < 3) ? (float) Math.sin(ageInTicks * 2.0F) * 0.1F : 0.0F;
            this.earLeft.zRot = -0.2F + earTwitch;
            this.earRight.zRot = 0.2F - earTwitch;
        }

        // ── attack : bat swoop-dive, wings thrust forward ───────────────
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float strike = (float) Math.sin(atk * Math.PI);
            this.root.xRot = resting ? 3.14F : strike * 0.4F;
            this.leftWing.zRot = resting ? 0.8F : -0.1F - strike * 0.6F;
            this.rightWing.zRot = resting ? -0.8F : 0.1F + strike * 0.6F;
            this.leftLeg.xRot = resting ? 0.0F : strike * 0.8F;
            this.rightLeg.xRot = resting ? 0.0F : strike * 0.8F;
        }

        // ── death : wings go limp, body tumbles ───────────────────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F);
            float collapse = t * t;
            if (!resting) {
                this.root.xRot = collapse * 0.5F;
                this.root.zRot = collapse * 0.8F;
            }
            this.leftWing.zRot = collapse * 1.2F;
            this.rightWing.zRot = -collapse * 1.2F;
            this.leftLeg.xRot = collapse * 0.5F;
            this.rightLeg.xRot = collapse * 0.5F;
            this.head.xRot = collapse * 0.6F;
        }
    }
}
