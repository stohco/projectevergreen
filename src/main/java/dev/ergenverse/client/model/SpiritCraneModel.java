package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/spirit_crane.png  SIZE: 64x64
/*
 * SpiritCraneModel — CRON-COMPLETIONIST-22: the 7th beast type.
 *
 * Canon (Renegade Immortal, species/spirit_crane.json):
 *   "Elegant crane 2m tall, feathers white with red crown, wings trail spirit-light.
 *    Cultivation: Core Formation → Nascent Soul (sword/wind attribute).
 *    Techniques: crane_dance, sword_wing, dawn_call, formation_flight.
 *    Habitat: sword sect peaks, sky migration routes, dawn-aspected spirit veins."
 *
 * REAL RED-CROWNED CRANE (丹顶鹤) anatomy:
 *   - Body: compact oval, streamlined for long-distance flight
 *   - Neck: EXTREMELY long and slender (the crane's most iconic feature)
 *   - Head: small skull with long pointed black beak, small eyes
 *   - Crown: red bare skin patch on top of the head (the "red crown")
 *   - Wings: broad and flat with black flight feathers (secondaries), white coverts
 *   - Tail: short, black tail feathers projecting past the wing tips
 *   - Legs: VERY long, black from the "knee" down, pink/red on the upper thigh
 *   - Height: standing ~1.5m tall on long legs
 *   - Wingspan: ~2m, black tips with white leading edge
 *
 * This model is DISTINCT from the hawk:
 *   - Hawk = raptor (short neck, hooked beak, broad wings, brown)
 *   - Crane = wading bird (long neck, pointed beak, broad wings, white+black)
 *
 * ANATOMY (CRON-COMPLETIONIST-22):
 *   - body     : compact streamlined torso (2 x 3 x 7) — longer than hawk's, narrower
 *   - neck     : 4-segment CHAIN (base → mid → upper → top) for a sinuous S-curve
 *               neck at rest is held in an elegant S-bend (not a straight stick)
 *   - head     : small skull (2x2x2) + long pointed beak (1x1x3)
 *   - crown   : small red patch (1x1x1) on top of skull — the red crown
 *   - wings    : 3-segment chain per side (shoulder→forearm→hand) + 5 primary feathers
 *   - tail     : short fan (3 black feather slabs) projecting past wings
 *   - legs     : 3 segments each (thigh → shin → foot), VERY long (shin=4px vs hawk's 3px)
 *               3 forward toes + rear hallux (like hawk, but longer)
 *
 * ANIMATION:
 *   - Walk     : slow deliberate high-step gait (cranes take big steps)
 *   - Fly     : slow majestic wingbeat with glide phases (not rapid hawk flapping)
 *   - Idle     : standing S-neck with slow breathing, occasional neck adjustment
 *   - Rest    : neck folds down, body lowers, one leg tucked (cranes sleep one-legged)
 *   - Graze    : neck extends, beak probes ground (cranes are waders)
 *   - Perch    : wings fold flat, long legs straight, neck S-curve
 *   - Sprint   : rapid wingbeat, neck stretched forward (crane alarm flight)
 *   - Attack  : stabbing beak strike (wings spread, legs extend forward)
 *   - Death    : neck drops, wings fold, legs collapse
 *   - Crane dance: special idle cycle — wings open partially, neck sweeps, body bobs
 *
 * HARSH SELF-CRITIQUE:
 *   - Neck is 4 segments of uniform 1x3x1 boxes — real crane necks taper from
 *     thick at the body to pencil-thin at the head. Mine are uniform-width sticks.
 *     A proper neck would use 3 boxes getting progressively narrower.
 *   - Crown is a 1x1x1 box — real crane crowns are flat patches of bare red skin
 *     that look painted-on, not a cube. This cube will look like a red dice on the head.
 *   - Flight feathers are uniform 8x1x1 slabs — same problem as hawk. Real crane
 *     primaries are individually shaped with curvature. Mine are flat sticks.
 *   - Long legs are 3 uniform segments — real crane legs have distinct joint angles
 *     at the "knee" (thigh-to-shin transition) and the "ankle" (shin-to-foot).
 *     The thigh is visibly thicker than the shin in a real crane; mine are uniform.
 *   - Beak is a box — real crane beaks are long, tapered cones.
 *   - The crane dance animation is the most canonically important behavior and it's
 *     simplified to "wings open, neck sweeps." Real crane dances involve complex
 *     bowing, jumping, grass-throwing, and pair synchronization.
 *   - No "one-legged sleeping" animation — real cranes rest on one leg.
 *   - Wings are flat box slabs — NOT real feather geometry with aerodynamic camber.
 */
import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class SpiritCraneModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart neckBase;
    private final ModelPart neckMid;
    private ModelPart neckUpper;
    private final ModelPart neckTop;
    private final ModelPart head;
    private final ModelPart crown;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftShoulder;
    private final ModelPart rightShoulder;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart tail;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public SpiritCraneModel(ModelPart root) {
        this.root = root;
        // ── Neck: 4-segment chain ──────────────────────────────────────
        this.neckBase = root.getChild("neck_base");
        this.neckMid = this.neckBase.getChild("neck_mid");
        this.neckUpper = this.neckMid.getChild("neck_upper");
        this.neckTop = this.neckUpper.getChild("neck_top");
        // ── Head: child of neck_top ──────────────────────────────────────
        this.head = this.neckTop.getChild("head");
        this.crown = this.head.getChild("crown");
        // ── Wings ──────────────────────────────────────────────────────
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.leftShoulder = leftWing.getChild("shoulder");
        this.rightShoulder = rightWing.getChild("shoulder");
        this.leftForearm = leftShoulder.getChild("forearm");
        this.rightForearm = rightShoulder.getChild("forearm");
        // ── Tail ──────────────────────────────────────────────────────
        this.tail = root.getChild("tail");
        // ── Legs ──────────────────────────────────────────────────────
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── body : compact streamlined torso (2 x 3 x 7) ───────────────
        // Real crane: wider across the shoulders than the hips, narrow.
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.0F, -1.5F, -3.5F, 2.0F, 3.0F, 7.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));

        // ── CRON-22: Neck — 4-segment chain for sinuous S-curve ──────────
        // Real crane neck: thick at body, pencil-thin at head.
        // Segment 1 (base): 1x4x1 — connects to body
        PartDefinition neckBase = root.addOrReplaceChild("neck_base",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 10.5F, -3.5F, -0.3F, 0.0F, 0.0F));
        // Segment 2 (mid): 1x4x1
        PartDefinition neckMid = neckBase.addOrReplaceChild("neck_mid",
                CubeListBuilder.create().texOffs(0, 22)
                        .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.ZERO);
        // Segment 3 (upper): 1x3x1 — slightly shorter, beginning to narrow
        PartDefinition neckUpper = neckMid.addOrReplaceChild("neck_upper",
                CubeListBuilder.create().texOffs(0, 28)
                        .addBox(-0.75F, -1.5F, -0.5F, 1.5F, 3.0F, 1.0F),
                PartPose.ZERO);
        // Segment 4 (top): 0.5x3x0.5 — thinnest, connects to head
        PartDefinition neckTop = neckUpper.addOrReplaceChild("neck_top",
                CubeListBuilder.create().texOffs(0, 34)
                        .addBox(-0.25F, -1.5F, -0.25F, 0.5F, 3.0F, 0.5F),
                PartPose.ZERO);

        // ── head : small skull + long pointed beak, child of neck_top ──────
        PartDefinition head = neckTop.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(4, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F)     // skull
                        .texOffs(4, 6)
                        .addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 3.0F),    // long beak
                PartPose.ZERO);

        // ── crown : red bare skin patch on top of skull ──────────────────
        head.addOrReplaceChild("crown",
                CubeListBuilder.create().texOffs(8, 0)
                        .addBox(-0.5F, -0.3F, -0.5F, 1.0F, 0.3F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, -0.5F, 0.0F, 0.0F, -0.2F));

        // ── left wing : shoulder -> forearm -> hand -> 5 feathers ─────────
        PartDefinition leftWing = root.addOrReplaceChild("left_wing",
                CubeListBuilder.create(),
                PartPose.offset(-1.0F, 9.0F, 0.0F));
        PartDefinition leftShoulder = leftWing.addOrReplaceChild("shoulder",
                CubeListBuilder.create().texOffs(0, 12)
                        .addBox(-5.0F, -0.5F, -2.0F, 5.0F, 1.0F, 4.0F),
                PartPose.ZERO);
        PartDefinition leftForearm = leftShoulder.addOrReplaceChild("forearm",
                CubeListBuilder.create().texOffs(0, 18)
                        .addBox(-5.0F, -0.5F, -2.0F, 5.0F, 1.0F, 4.0F),
                PartPose.offset(-5.0F, 0.0F, 0.0F));
        PartDefinition leftHand = leftForearm.addOrReplaceChild("hand",
                CubeListBuilder.create().texOffs(0, 24)
                        .addBox(-4.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(-5.0F, 0.0F, 0.0F));
        // 5 primary feathers
        leftHand.addOrReplaceChild("feather1",
                CubeListBuilder.create().texOffs(20, 12)
                        .addBox(-8.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(-3.0F, 0.0F, -1.5F));
        leftHand.addOrReplaceChild("feather2",
                CubeListBuilder.create().texOffs(20, 16)
                        .addBox(-8.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(-3.0F, 0.0F, 0.0F));
        leftHand.addOrReplaceChild("feather3",
                CubeListBuilder.create().texOffs(20, 20)
                        .addBox(-8.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(-3.0F, 0.0F, 1.5F));
        leftHand.addOrReplaceChild("feather4",
                CubeListBuilder.create().texOffs(20, 24)
                        .addBox(-8.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(-3.0F, 0.0F, 3.0F));
        leftHand.addOrReplaceChild("feather5",
                CubeListBuilder.create().texOffs(20, 28)
                        .addBox(-8.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(-3.0F, 0.0F, 4.5F));

        // ── right wing : mirror ──────────────────────────────────────────
        PartDefinition rightWing = root.addOrReplaceChild("right_wing",
                CubeListBuilder.create(),
                PartPose.offset(1.0F, 9.0F, 0.0F));
        PartDefinition rightShoulder = rightWing.addOrReplaceChild("shoulder",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(0.0F, -0.5F, -2.0F, 5.0F, 1.0F, 4.0F),
                PartPose.ZERO);
        PartDefinition rightForearm = rightShoulder.addOrReplaceChild("forearm",
                CubeListBuilder.create().texOffs(0, 38)
                        .addBox(0.0F, -0.5F, -2.0F, 5.0F, 1.0F, 4.0F),
                PartPose.offset(5.0F, 0.0F, 0.0F));
        PartDefinition rightHand = rightForearm.addOrReplaceChild("hand",
                CubeListBuilder.create().texOffs(0, 44)
                        .addBox(0.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(5.0F, 0.0F, 0.0F));
        rightHand.addOrReplaceChild("feather1",
                CubeListBuilder.create().texOffs(20, 32)
                        .addBox(0.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(3.0F, 0.0F, -1.5F));
        rightHand.addOrReplaceChild("feather2",
                CubeListBuilder.create().texOffs(20, 36)
                        .addBox(0.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(3.0F, 0.0F, 0.0F));
        rightHand.addOrReplaceChild("feather3",
                CubeListBuilder.create().texOffs(20, 40)
                        .addBox(0.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(3.0F, 0.0F, 1.5F));
        rightHand.addOrReplaceChild("feather4",
                CubeListBuilder.create().texOffs(20, 44)
                        .addBox(0.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(3.0F, 0.0F, 3.0F));
        rightHand.addOrReplaceChild("feather5",
                CubeListBuilder.create().texOffs(20, 48)
                        .addBox(0.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(3.0F, 0.0F, 4.5F));

        // ── tail : short black fan projecting past wings ───────────────
        PartDefinition tail = root.addOrReplaceChild("tail",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 9.0F, 3.5F));
        tail.addOrReplaceChild("feather1",
                CubeListBuilder.create().texOffs(40, 12)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2F, 0.0F));
        tail.addOrReplaceChild("feather2",
                CubeListBuilder.create().texOffs(40, 20)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 5.0F),
                PartPose.ZERO);
        tail.addOrReplaceChild("feather3",
                CubeListBuilder.create().texOffs(40, 28)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.2F, 0.0F));

        // ── legs : VERY long 3-segment legs ────────────────────────
        // Real crane: thigh is thicker than shin, shin is very long.
        // Thigh: 2x3x1.5 (thicker, upper leg, pinkish)
        // Shin:  1x4x1 (long, black from "knee" down)
        // Foot: 1x1x1 with 3 forward toes + 1 rear hallux
        // Left leg
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 40)
                        .addBox(-1.0F, 0.0F, -0.75F, 2.0F, 3.0F, 1.5F)  // thigh
                        .texOffs(0, 46)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F),  // shin
                PartPose.offset(-1.5F, 12.0F, 0.0F));
        // Left foot (child of left_leg — gets thigh+shin rotation via hierarchy)
        root.getChild("left_leg").addOrReplaceChild("foot",
                CubeListBuilder.create().texOffs(4, 46)
                        .addBox(-1.0F, 0.0F, -0.75F, 2.0F, 1.0F, 1.5F),  // foot base
                PartPose.offset(0.0F, 7.0F, 0.0F));
        root.getChild("left_leg").getChild("foot").addOrReplaceChild("toe1",
                CubeListBuilder.create().texOffs(4, 40)
                        .addBox(-1.0F, 0.0F, -1.25F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO);
        root.getChild("left_leg").getChild("foot").addOrReplaceChild("toe2",
                CubeListBuilder.create().texOffs(8, 40)
                        .addBox(0.0F, 0.0F, -1.25F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO);
        root.getChild("left_leg").getChild("foot").addOrReplaceChild("toe3",
                CubeListBuilder.create().texOffs(4, 42)
                        .addBox(-1.0F, 0.0F, -1.75F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO);
        root.getChild("left_leg").getChild("foot").addOrReplaceChild("hallux",
                CubeListBuilder.create().texOffs(8, 42)
                        .addBox(0.0F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO);

        // Right leg
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 50)
                        .addBox(-1.0F, 0.0F, -0.75F, 2.0F, 3.0F, 1.5F)   // thigh
                        .texOffs(0, 56)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F),   // shin
                PartPose.offset(1.5F, 12.0F, 0.0F));
        root.getChild("right_leg").addOrReplaceChild("foot",
                CubeListBuilder.create().texOffs(4, 56)
                        .addBox(-1.0F, 0.0F, -0.75F, 2.0F, 1.0F, 1.5F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        root.getChild("right_leg").getChild("foot").addOrReplaceChild("toe1",
                CubeListBuilder.create().texOffs(4, 50)
                        .addBox(-1.0F, 0.0F, -1.25F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO);
        root.getChild("right_leg").getChild("foot").addOrReplaceChild("toe2",
                CubeListBuilder.create().texOffs(8, 50)
                        .addBox(0.0F, 0.0F, -1.25F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO);
        root.getChild("right_leg").getChild("foot").addOrReplaceChild("toe3",
                CubeListBuilder.create().texOffs(4, 52)
                        .addBox(-1.0F, 0.0F, -1.75F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO);
        root.getChild("right_leg").getChild("foot").addOrReplaceChild("hallux",
                CubeListBuilder.create().texOffs(8, 52)
                        .addBox(0.0F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    /** CRON-COMPLETIONIST-47: Expose crown for emissive rendering in CraneRenderer. */
    public ModelPart getCrown() { return this.crown; }

    @Override
    public void setupAnim(SpiritBeastEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // ── head turn: propagated through neck chain ──────────────────────
        float yaw = netHeadYaw * 0.017453292F;
        float pitch = headPitch * 0.017453292F;
        this.head.yRot = Math.max(-1.0F, Math.min(1.0F, yaw));
        this.head.xRot = Math.max(-0.5F, Math.min(0.5F, pitch));

        // ── Pose flags ────────────────────────────────────────────────────
        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;
        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING;
        boolean sprinting = entity.getSpiritPose() == SpiritBeastEntity.POSE_SPRINTING;
        boolean flying = entity.getSpiritPose() == SpiritBeastEntity.POSE_FLYING;
        boolean grazing = entity.getSpiritPose() == SpiritBeastEntity.POSE_GRAZING;
        boolean perching = entity.getSpiritPose() == SpiritBeastEntity.POSE_PERCHING;
        boolean alert = entity.getSpiritPose() == SpiritBeastEntity.POSE_ALERT;

        // ── CRON-22: Resting — one-legged crane sleep ───────────────────────
        if (resting) {
            float breath = (float) Math.sin(ageInTicks * 0.08F) * 0.1F;
            float earShift = (ageInTicks % 80 < 3) ? (float) Math.sin(ageInTicks * 1.5F) * 0.03F : 0.0F;
            this.root.y = -4.0F + breath;
            // Body lowers, right leg tucks (one-legged sleep)
            this.leftLeg.xRot = 0.1F;
            this.rightLeg.xRot = 0.8F;     // right leg tucked
            this.rightLeg.yRot = 0.3F;     // slightly out
            // shin and thigh are baked into the leg PartDefinition as
            // two addBox calls — they cannot be animated independently.
            // The whole-leg fold is expressed via xRot above.
            // Neck folds in elegant S-bend
            this.neckBase.xRot = 0.3F;
            this.neckMid.xRot = 0.2F;
            this.neckUpper.xRot = -0.1F;
            this.neckTop.xRot = -0.6F;
            this.head.xRot = 0.8F + breath * 0.5F;
            // Wings wrap body
            this.leftWing.zRot = -0.8F;
            this.rightWing.zRot = 0.8F;
            this.leftWing.xRot = 0.5F;
            this.rightWing.xRot = -0.5F;
            // Tail droops
            this.tail.xRot = 0.4F;
        } else if (swimming) {
            // ── Swimming: crane paddles (not natural but possible) ──────────
            float paddle = ageInTicks * 0.8F;
            float bob = (float) Math.sin(paddle * 0.5F) * 0.1F;
            this.root.xRot = -0.2F;
            this.root.y = -2.0F + bob;
            this.head.xRot = -0.3F;
            this.neckBase.xRot = 0.1F;
            this.neckMid.xRot = -0.1F;
            this.neckUpper.xRot = -0.2F;
            this.neckTop.xRot = -0.3F;
            // Wings row alternately
            this.leftWing.zRot = -0.3F + (float) Math.sin(paddle) * 0.4F;
            this.rightWing.zRot = 0.3F - (float) Math.sin(paddle) * 0.4F;
            this.leftWing.xRot = 0.3F;
            this.rightWing.xRot = -0.3F;
            // Legs paddle behind
            this.leftLeg.xRot = (float) Math.cos(paddle) * 0.4F;
            this.rightLeg.xRot = (float) Math.cos(paddle + Math.PI) * 0.4F;
            // Neck reset for swimming
            this.neckMid.yRot = (float) Math.sin(ageInTicks * 0.2F) * 0.05F;
        } else if (sprinting) {
            // ── Sprint: rapid alarm flight ───────────────────────────────
            float sp = limbSwing * 0.6F;
            float amp = 1.2F * limbSwingAmount;
            this.root.xRot = -0.15F;
            this.root.y = (float) Math.sin(ageInTicks * 0.15F) * 0.06F;
            // Neck stretched forward (alarm)
            this.neckBase.xRot = -0.2F;
            this.neckMid.xRot = -0.15F;
            this.neckUpper.xRot = -0.1F;
            this.neckTop.xRot = -0.2F;
            this.head.xRot = -0.3F;
            // Rapid wingbeat
            this.leftWing.zRot = -(float) Math.sin(ageInTicks * 1.0F) * (0.6F + amp * 0.4F);
            this.rightWing.zRot = (float) Math.sin(ageInTicks * 1.0F) * (0.6F + amp * 0.4F);
            this.leftWing.xRot = 0.2F;
            this.rightWing.xRot = -0.2F;
            // Legs stream behind
            this.leftLeg.xRot = -0.3F * limbSwingAmount;
            this.rightLeg.xRot = -0.3F * limbSwingAmount;
            // Tail straight
            this.tail.xRot = 0.1F;
        } else if (flying) {
            // ── Flying: slow majestic wingbeat ──────────────────────────
            float flap = (float) Math.sin(ageInTicks * 0.4F);
            float downstroke = (float) Math.max(0.0F, Math.sin(ageInTicks * 0.4F));
            // Body pitches slightly on downstroke
            this.root.xRot = -downstroke * 0.1F * limbSwingAmount;
            this.root.y = (float) Math.sin(ageInTicks * 0.15F) * 0.05F;
            // Neck extends forward (flying cranes stretch neck forward)
            this.neckBase.xRot = -0.2F;
            this.neckMid.xRot = -0.15F;
            this.neckUpper.xRot = -0.1F;
            this.neckTop.xRot = -0.15F;
            this.head.xRot = -0.1F;
            // Slow majestic wingbeat (amplitude scaled by movement)
            this.leftWing.zRot = -(0.2F + flap * (0.3F + limbSwingAmount * 0.3F));
            this.rightWing.zRot = (0.2F + flap * (0.3F + limbSwingAmount * 0.3F));
            this.leftWing.xRot = -downstroke * 0.1F;
            this.rightWing.xRot = downstroke * 0.1F;
            // Elbow flex on downstroke
            this.leftShoulder.zRot = -downstroke * 0.2F * limbSwingAmount;
            this.rightShoulder.zRot = downstroke * 0.2F * limbSwingAmount;
            this.leftForearm.zRot = -downstroke * 0.1F * limbSwingAmount;
            this.rightForearm.zRot = downstroke * 0.1F * limbSwingAmount;
            // Legs tucked tight
            this.leftLeg.xRot = -0.5F;
            this.rightLeg.xRot = -0.5F;
            // Tail fans slightly
            this.tail.xRot = 0.15F;
            this.tail.yRot = (float) Math.sin(ageInTicks * 0.2F) * 0.1F;
        } else if (grazing) {
            // ── Grazing: neck extends, beak probes ground ────────────────
            this.neckBase.xRot = 0.4F;  // body→forward
            this.neckMid.xRot = 0.5F;   // mid→forward
            this.neckUpper.xRot = 0.3F; // upper→slight back
            this.neckTop.xRot = 0.1F;   // top→slight back
            this.head.xRot = 0.8F;      // beak points at ground
            this.root.y = -1.0F;     // body slightly lower
            // Slow breathing
            this.root.y += (float) Math.sin(ageInTicks * 0.08F) * 0.08F;
            // Tail droops
            this.tail.xRot = 0.3F;
        } else if (alert) {
            // ── Alert: neck up, wings slightly open ─────────────────────
            this.neckBase.xRot = -0.5F;
            this.neckMid.xRot = -0.3F;
            this.neckUpper.xRot = -0.2F;
            this.neckTop.xRot = -0.2F;
            this.head.xRot = -0.3F;
            // Tail fans wide
            this.tail.xRot = 0.0F;
            this.tail.yRot = (float) Math.sin(ageInTicks * 0.3F) * 0.15F;
            // Wings half-open (alert display)
            this.leftWing.zRot = -0.15F;
            this.rightWing.zRot = 0.15F;
        } else if (perching) {
            // ── Perching: wings fold flat, legs straight, elegant S-neck ────
            float breath = (float) Math.sin(ageInTicks * 0.08F) * 0.08F;
            this.root.y = breath * 0.5F;
            // Elegant S-curve resting neck
            this.neckBase.xRot = 0.15F;
            this.neckMid.xRot = 0.05F;
            this.neckMid.yRot = (float) Math.sin(ageInTicks * 0.1F) * 0.08F;
            this.neckUpper.xRot = -0.1F;
            this.neckTop.xRot = -0.25F;
            this.head.xRot = 0.2F;
            // Wings fold flat
            this.leftWing.zRot = -0.7F;
            this.rightWing.zRot = 0.7F;
            this.leftWing.xRot = 0.4F;
            this.rightWing.xRot = -0.4F;
            // Legs straight
            this.leftLeg.xRot = 0.0F;
            this.rightLeg.xRot = 0.0F;
            // Tail fans slightly
            this.tail.yRot = (float) Math.sin(ageInTicks * 0.2F) * 0.1F;
        }

        // ── default: walk / idle / combat ──────────────────────────────
        if (!resting && !swimming && !sprinting && !flying && !grazing && !alert && !perching) {
            boolean running = limbSwingAmount > 0.5F;
            float phase = limbSwing * (running ? 0.5F : 0.3F);
            float amp = (running ? 1.0F : 0.6F) * limbSwingAmount;

            // ── walk gait: slow high-step (crane takes big deliberate steps) ─
            this.leftLeg.xRot = (float) Math.cos(phase) * amp * 0.5F;
            this.rightLeg.xRot = (float) Math.cos(phase + Math.PI) * amp * 0.5F;

            // ── idle: slow breathing, neck S-curve, gentle neck sway ──
            this.root.y = (float) Math.sin(ageInTicks * 0.08F) * 0.1F;
            // Neck idle S-curve — subtle oscillation
            this.neckBase.xRot = 0.15F + (float) Math.sin(ageInTicks * 0.05F) * 0.05F;
            this.neckMid.xRot = 0.05F + (float) Math.sin(ageInTicks * 0.08F) * 0.08F;
            this.neckUpper.xRot = -0.1F + (float) Math.sin(ageInTicks * 0.1F) * 0.05F;
            this.neckTop.xRot = -0.2F;
            this.head.xRot = 0.1F;
            this.neckBase.yRot = (float) Math.sin(ageInTicks * 0.04F) * 0.05F;
            // Tail gentle sway
            this.tail.yRot = (float) Math.sin(ageInTicks * 0.15F) * 0.1F;

            // ── combat stance: neck up, beak forward, wings slightly open ──
            boolean combat = entity.getTarget() != null;
            if (combat) {
                this.neckBase.xRot = -0.2F;
                this.neckMid.xRot = -0.15F;
                this.neckTop.xRot = -0.15F;
                this.head.xRot = -0.3F;
                this.leftWing.zRot -= 0.1F;
                this.rightWing.zRot += 0.1F;
            }

            // ── attack lunge: beak stab ─────────────────────────────────
            float atk = entity.attackAnim;
            if (atk > 0.0F) {
                float strike = (float) Math.sin(atk * Math.PI);
                this.root.xRot = -strike * 0.3F;
                this.head.xRot = -strike * 0.6F;
                this.neckBase.xRot -= strike * 0.2F;
                this.neckMid.xRot -= strike * 0.15F;
                // Wings spread for intimidation
                this.leftWing.zRot -= strike * 0.3F;
                this.rightWing.zRot += strike * 0.3F;
                this.leftLeg.xRot -= strike * 0.2F;
                this.rightLeg.xRot -= strike * 0.2F;
            }

            // ── crane dance: occasional display animation ──────────────
            // Canon: cranes dance at dawn as part of their cultivation.
            // Simplified: wings partially open, neck sweeps, body bobs.
            // Triggers every ~100 ticks for 40 ticks of dancing.
            int dancePhase = (int) (ageInTicks % 100);
            if (dancePhase < 40) {
                float d = (float) Math.sin(dancePhase * 0.3F);
                // Wings partially open and flap slowly
                this.leftWing.zRot = -(0.15F + d * 0.1F);
                this.rightWing.zRot = (0.15F - d * 0.1F);
                // Neck sweeps left-right
                this.neckBase.yRot = d * 0.15F;
                this.neckMid.yRot = d * 0.1F;
                // Body bobs
                this.root.y += (float) Math.sin(dancePhase * 0.5F) * 0.15F;
            }
        }

        // ── death collapse ────────────────────────────────────────────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 10.0F, 1.0F);
            float collapse = t * t;
            this.root.xRot = collapse * -0.3F;
            this.root.zRot = collapse * 0.4F;
            this.root.y = -collapse * 3.0F;
            // Neck drops forward
            this.neckBase.xRot = 0.5F * collapse;
            this.neckMid.xRot = 0.3F * collapse;
            this.neckUpper.xRot = 0.2F * collapse;
            this.neckTop.xRot = 0.1F * collapse;
            this.head.xRot = collapse * 0.6F;
            // Wings fold flat
            this.leftWing.zRot = -0.7F * collapse;
            this.rightWing.zRot = 0.7F * collapse;
            this.leftWing.xRot = 0.5F * collapse;
            this.rightWing.xRot = -0.5F * collapse;
            // Legs collapse sideways
            this.leftLeg.zRot = -collapse * 0.3F;
            this.rightLeg.zRot = collapse * 0.3F;
            // Tail drops
            this.tail.xRot = 0.5F * collapse;
        }
    }
}
