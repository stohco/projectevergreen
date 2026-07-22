package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/spirit_hawk.png  SIZE: 64x64
/*
 * SpiritHawkModel — anatomically correct hawk.
 *
 * ANATOMY:
 *   - body    : horizontal torso (6 x 4 x 6) at root, head at -Z, tail at +Z
 *   - head    : skull (3x3x3) + beak (1x1x2, forward) + crest (2x1x1, on top)
 *   - wings   : 3-segment chain (shoulder -> forearm -> hand) per side, each
 *               a thin chord box, plus 3 primary feather slabs at the hand
 *   - tail    : 3 feather slabs fanning from the rear pivot
 *   - legs    : thin shin (1x3x1) + foot + 2 forward toes per leg (talons)
 *
 * ANIMATION:
 *   - Flight flap   : wings oscillate on zRot via sin(age*0.6) * (0.4 + swingAmt*0.8);
 *                     opposite signs on left/right so both tips move together.
 *                     Elbow flexes on the downstroke for thrust.
 *   - Glide         : when limbSwingAmount < 0.1, wings hold flat (~ -0.1 / +0.1 zRot)
 *                     with a slow rise-fall sin(age*0.15)*0.15.
 *   - Banking       : root.zRot gentle sin(age*0.1)*0.15 roll.
 *   - Head turn     : head.yRot = netHeadYaw * deg2rad; head.xRot = headPitch * deg2rad.
 *   - Tail fan      : tail.yRot sin sway (age*0.3)*0.2.
 *   - Leg tuck      : legs pull up slightly when flapping hard.
 *
 * HARSH SELF-CRITIQUE:
 *   - Wings are flat box slabs, NOT real feather geometry. A real raptor has
 *     10 split-tip primaries, secondaries, coverts, and an alula. My "feathers"
 *     are uniform 8x1x1 slabs with no taper, no overlap, no aerodynamic camber.
 *   - Beak is a blunt 1x1x2 box — real raptor beak is a hooked, tapered cone.
 *     The hook and the cere (waxy base) are entirely missing.
 *   - No animation for talon extension: real hawks tuck legs tight in flight
 *     and extend them forward to land/strike. Legs are static here.
 *   - Body does not pitch on the flap downstroke (real birds pitch body up
 *     on the power stroke and dip on recovery). Whole-body pitch is missing.
 *   - No per-feather spread on banking turns (real hawks fan primaries apart
 *     for control surfaces). Tail feathers do not individually spread either.
 *   - Legs have no rear toe (raptors have 3 forward + 1 hind "hallux" toe).
 *   - Texture UVs are guessed; will scramble the existing spirit_hawk.png
 *     which was authored for the vanilla ParrotModel. The texture MUST be
 *     regenerated for this UV layout.
 *   - The hawk entity is a PathfinderMob that WALKS, but this model looks
 *     like it is flying. There is no ground-perched stance. Either the entity
 *     needs a FlyingMob parent or the model needs a perched pose branch.
 */
import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class SpiritHawkModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftShoulder;
    private final ModelPart rightShoulder;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart tail;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public SpiritHawkModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.leftShoulder = leftWing.getChild("shoulder");
        this.rightShoulder = rightWing.getChild("shoulder");
        this.leftForearm = leftShoulder.getChild("forearm");
        this.rightForearm = rightShoulder.getChild("forearm");
        this.tail = root.getChild("tail");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── body : horizontal torso, hovering around y=10 ────────────────
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, 10.0F, 0.0F));

        // ── head : skull + beak + crest, at front of body (-Z) ───────────
        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(24, 0)
                        .addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 3.0F)   // skull
                        .texOffs(24, 8)
                        .addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 2.0F)    // beak forward
                        .texOffs(36, 0)
                        .addBox(-1.0F, -2.5F, -1.5F, 2.0F, 1.0F, 1.0F),  // crest on top
                PartPose.offset(0.0F, 9.0F, -4.0F));

        // ── left wing : shoulder -> forearm -> hand -> 3 feathers ────────
        PartDefinition leftWing = root.addOrReplaceChild("left_wing",
                CubeListBuilder.create(),
                PartPose.offset(-3.0F, 9.0F, 0.0F));
        PartDefinition leftShoulder = leftWing.addOrReplaceChild("shoulder",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-5.0F, -0.5F, -2.0F, 5.0F, 1.0F, 4.0F),
                PartPose.ZERO);
        PartDefinition leftForearm = leftShoulder.addOrReplaceChild("forearm",
                CubeListBuilder.create().texOffs(0, 24)
                        .addBox(-5.0F, -0.5F, -2.0F, 5.0F, 1.0F, 4.0F),
                PartPose.offset(-5.0F, 0.0F, 0.0F));
        PartDefinition leftHand = leftForearm.addOrReplaceChild("hand",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-4.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(-5.0F, 0.0F, 0.0F));
        // primary feathers : flat slabs fanning off the trailing edge
        leftHand.addOrReplaceChild("feather1",
                CubeListBuilder.create().texOffs(20, 16)
                        .addBox(-8.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(-3.0F, 0.0F, -1.5F));
        leftHand.addOrReplaceChild("feather2",
                CubeListBuilder.create().texOffs(20, 20)
                        .addBox(-8.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(-3.0F, 0.0F, 0.0F));
        leftHand.addOrReplaceChild("feather3",
                CubeListBuilder.create().texOffs(20, 24)
                        .addBox(-8.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(-3.0F, 0.0F, 1.5F));

        // ── right wing : mirror ──────────────────────────────────────────
        PartDefinition rightWing = root.addOrReplaceChild("right_wing",
                CubeListBuilder.create(),
                PartPose.offset(3.0F, 9.0F, 0.0F));
        PartDefinition rightShoulder = rightWing.addOrReplaceChild("shoulder",
                CubeListBuilder.create().texOffs(0, 40)
                        .addBox(0.0F, -0.5F, -2.0F, 5.0F, 1.0F, 4.0F),
                PartPose.ZERO);
        PartDefinition rightForearm = rightShoulder.addOrReplaceChild("forearm",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(0.0F, -0.5F, -2.0F, 5.0F, 1.0F, 4.0F),
                PartPose.offset(5.0F, 0.0F, 0.0F));
        PartDefinition rightHand = rightForearm.addOrReplaceChild("hand",
                CubeListBuilder.create().texOffs(0, 56)
                        .addBox(0.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(5.0F, 0.0F, 0.0F));
        rightHand.addOrReplaceChild("feather1",
                CubeListBuilder.create().texOffs(20, 40)
                        .addBox(0.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(3.0F, 0.0F, -1.5F));
        rightHand.addOrReplaceChild("feather2",
                CubeListBuilder.create().texOffs(20, 44)
                        .addBox(0.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(3.0F, 0.0F, 0.0F));
        rightHand.addOrReplaceChild("feather3",
                CubeListBuilder.create().texOffs(20, 48)
                        .addBox(0.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(3.0F, 0.0F, 1.5F));

        // ── tail : 3 feather slabs fanning from the rear (+Z) ────────────
        PartDefinition tail = root.addOrReplaceChild("tail",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 9.0F, 3.0F));
        tail.addOrReplaceChild("feather1",
                CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3F, 0.0F));
        tail.addOrReplaceChild("feather2",
                CubeListBuilder.create().texOffs(40, 24)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F),
                PartPose.ZERO);
        tail.addOrReplaceChild("feather3",
                CubeListBuilder.create().texOffs(40, 32)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3F, 0.0F));

        // ── legs : thin shin + foot + 2 forward talons ───────────────────
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(50, 16)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F)    // shin
                        .texOffs(50, 22)
                        .addBox(-1.0F, 3.0F, -0.5F, 2.0F, 1.0F, 1.0F)    // foot
                        .texOffs(50, 26)
                        .addBox(-1.0F, 3.0F, -1.5F, 1.0F, 1.0F, 1.0F)    // toe 1
                        .texOffs(50, 30)
                        .addBox(0.0F, 3.0F, -1.5F, 1.0F, 1.0F, 1.0F),    // toe 2
                PartPose.offset(-1.5F, 12.0F, 0.0F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(50, 36)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F)
                        .texOffs(50, 42)
                        .addBox(-1.0F, 3.0F, -0.5F, 2.0F, 1.0F, 1.0F)
                        .texOffs(50, 46)
                        .addBox(-1.0F, 3.0F, -1.5F, 1.0F, 1.0F, 1.0F)
                        .texOffs(50, 50)
                        .addBox(0.0F, 3.0F, -1.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(1.5F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(SpiritBeastEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // ── head turn ────────────────────────────────────────────────────
        this.head.yRot = netHeadYaw * 0.017453292F;
        this.head.xRot = headPitch * 0.017453292F;

        // ── flap intensity : faster flap when moving ─────────────────────
        float flapAmp = 0.4F + limbSwingAmount * 0.8F;
        float flap = (float) Math.sin(ageInTicks * 0.6F) * flapAmp;

        if (limbSwingAmount < 0.1F) {
            // GLIDE : wings hold flat with slow rise-fall
            float glide = (float) Math.sin(ageInTicks * 0.15F) * 0.15F;
            this.leftWing.zRot = -0.1F + glide;
            this.rightWing.zRot = 0.1F - glide;
            this.leftShoulder.zRot = 0.0F;
            this.rightShoulder.zRot = 0.0F;
            this.leftForearm.zRot = 0.0F;
            this.rightForearm.zRot = 0.0F;
            // legs hang loose on glide
            this.leftLeg.xRot = 0.0F;
            this.rightLeg.xRot = 0.0F;
        } else {
            // FLAP : full oscillation, elbow flexes on the downstroke
            this.leftWing.zRot = flap;
            this.rightWing.zRot = -flap;
            float downstroke = (float) Math.max(0.0F, Math.sin(ageInTicks * 0.6F));
            float elbow = downstroke * 0.3F * limbSwingAmount;
            this.leftShoulder.zRot = -elbow;
            this.rightShoulder.zRot = elbow;
            this.leftForearm.zRot = -elbow * 0.5F;
            this.rightForearm.zRot = elbow * 0.5F;
            // legs tuck up when flapping hard
            this.leftLeg.xRot = -0.4F * limbSwingAmount;
            this.rightLeg.xRot = -0.4F * limbSwingAmount;
        }

        // ── banking : gentle roll on the whole body ──────────────────────
        this.root.zRot = (float) Math.sin(ageInTicks * 0.1F) * 0.15F;

        // ── tail fan sway ────────────────────────────────────────────────
        this.tail.yRot = (float) Math.sin(ageInTicks * 0.3F) * 0.2F;

        // ── attack : hawk talon-strikes (wings sweep, legs extend) ─────
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float strike = (float) Math.sin(atk * Math.PI);
            // wings sweep down and forward (diving strike)
            this.leftWing.zRot = -0.1F - strike * 0.8F;
            this.rightWing.zRot = 0.1F + strike * 0.8F;
            // elbows tuck (wings fold partially)
            this.leftShoulder.zRot = -strike * 0.4F;
            this.rightShoulder.zRot = strike * 0.4F;
            this.leftForearm.zRot = -strike * 0.2F;
            this.rightForearm.zRot = strike * 0.2F;
            // body pitches down (stoop)
            this.root.xRot = strike * 0.5F;
            // head tilts down, beak forward
            this.head.xRot = strike * 0.4F;
            // legs extend for talon grab
            this.leftLeg.xRot = strike * 1.2F;
            this.rightLeg.xRot = strike * 1.2F;
        }

        // ── death : wings fold tight, body drops ─────────────────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F); // 0→1 over 0.4s (visible before fade)
            float collapse = t * t;
            // body pitches forward and rolls
            this.root.xRot = collapse * 0.5F;
            this.root.zRot = collapse * -0.4F;
            // head droops
            this.head.xRot = collapse * 0.7F;
            this.head.zRot = collapse * 0.3F;
            // wings fold flat against body
            this.leftWing.zRot = 0.0F - collapse * 0.1F;
            this.rightWing.zRot = 0.0F + collapse * 0.1F;
            this.leftWing.xRot = collapse * 0.8F;   // wings fold down
            this.rightWing.xRot = -collapse * 0.8F;
            // tail drops
            this.tail.xRot = collapse * 0.5F;
            // legs go limp
            this.leftLeg.xRot = collapse * 0.3F;
            this.rightLeg.xRot = collapse * 0.3F;
        }
    }
}
