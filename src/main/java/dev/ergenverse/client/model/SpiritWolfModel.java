package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/spirit_wolf.png  SIZE: 64x64
/*
 * SpiritWolfModel — lean quadruped predator.
 *
 * ANATOMY:
 *   - body       : torso (4 x 6 x 10) at chest height, head at -Z, tail at +Z
 *   - neck       : short tilted connector between body and head
 *   - head       : skull (3x3x3) + snout (2x2x2) + 2 angled ears + lower jaw
 *                  (separate child, can open) + 2 small fangs
 *   - tail       : 3-segment bushy chain (base -> mid -> tip) curving down
 *   - legs       : 4 legs, each 2 segments (thigh + shin), feet at y=15
 *
 * ANIMATION:
 *   - Walk gait  : diagonal trot — frontLeft & backRight phase 0,
 *                  frontRight & backLeft phase PI. cos(swing*0.6662)*amp*swingAmt.
 *                  Shins counter-flex for natural knee bend.
 *   - Run        : when limbSwingAmount > 0.5, swing frequency x1.5 and amp 1.2.
 *   - Idle       : body subtle breathing via root.y sin(age*0.1)*0.1,
 *                  tail base sways sin(age*0.2)*0.3.
 *   - Combat     : when entity.getTarget() != null, head dips, jaw opens,
 *                  ears pin back, tail drops.
 *   - Head turn  : head.yRot = netHeadYaw * deg2rad (clamped); head.xRot = headPitch * deg2rad.
 *
 * HARSH SELF-CRITIQUE:
 *   - Body is a single box; real wolf has separate chest (deep) and hip (narrow)
 *     volumes. No ribcage flare, no waist tuck. Looks like a sausage.
 *   - Ears are boxy cubes, not the triangular pointed shells a real wolf has.
 *   - Fangs are 1x1x1 cubes — real canids have tapered cone teeth, and the
 *     snout should have a black nose pad at the tip (missing entirely).
 *   - Tail "bushy" look is faked with three 1x1x3 segments; a real wolf tail
 *     is a fat plume that should taper. No fur silhouette.
 *   - Knee flex is a crude cos-based counter-swing; a real canine gait has
 *     a distinct stance phase (leg straight, foot on ground) and swing phase
 *     (leg bent, foot lifted). This model blurs them.
 *   - No spine flexion: a running wolf arches its back (extends on the
 *     stride). My spine is rigid.
 *   - Combat stance uses getTarget() as a proxy — it does NOT fire on the
 *     actual MeleeAttackGoal swing, so the lunge is purely "alert posture",
 *     not a true strike animation. Needs a synced DataAccessor for attackTime.
 *   - Texture UVs are invented; existing spirit_wolf.png (authored for vanilla
 *     WolfModel) will scramble on this layout.
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

        // ── body : torso, chest height ───────────────────────────────────
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.0F, -3.0F, -5.0F, 4.0F, 6.0F, 10.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));

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
        // ── head turn (clamped) ──────────────────────────────────────────
        float yaw = netHeadYaw * 0.017453292F;
        float pitch = headPitch * 0.017453292F;
        this.head.yRot = Math.max(-1.0F, Math.min(1.0F, yaw));
        this.head.xRot = Math.max(-0.7F, Math.min(0.7F, pitch));

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

        // ── idle breathing + spine bob ──────────────────────────────────
        this.root.y = (float) Math.sin(ageInTicks * 0.1F) * 0.1F;

        // ── tail sway ────────────────────────────────────────────────────
        this.tailBase.yRot = (float) Math.sin(ageInTicks * 0.2F) * 0.3F;
        this.tailMid.yRot  = (float) Math.sin(ageInTicks * 0.2F + 0.4F) * 0.2F;
        this.tailTip.yRot  = (float) Math.sin(ageInTicks * 0.2F + 0.8F) * 0.2F;

        // ── combat stance : when targeting, head dips, jaw opens, ears pin ──
        boolean combat = entity.getTarget() != null;
        if (combat) {
            this.head.xRot += 0.3F;                          // head dips
            this.jaw.xRot = 0.45F;                            // mouth open
            this.earLeft.zRot  = -0.6F;                       // ears pin back
            this.earRight.zRot = 0.6F;
            this.tailBase.xRot = 0.9F;                        // tail drops low
        } else {
            this.jaw.xRot = 0.0F;
            this.earLeft.zRot  = -0.3F;
            this.earRight.zRot = 0.3F;
            this.tailBase.xRot = 0.3F;                        // tail relaxed
        }

        // ── attack lunge : body surges forward, head snaps, jaw wide ────
        // Mob.attackAnim is a synced float 0→1→0 during each melee swing.
        // The peak of the lunge is at attackAnim ~ 0.5 (mid-swing).
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float lunge = (float) Math.sin(atk * Math.PI); // 0→1→0 smooth arc
            this.root.xRot = -lunge * 0.6F;                 // body pitches forward
            this.head.xRot -= lunge * 0.8F;                 // head snaps forward
            this.jaw.xRot += lunge * 0.5F;                  // jaw opens wider
            // front legs push back (rearing push), back legs dig in
            this.frontLeftThigh.xRot  += lunge * 0.4F;
            this.frontRightThigh.xRot += lunge * 0.4F;
            this.backLeftThigh.xRot   -= lunge * 0.3F;
            this.backRightThigh.xRot  -= lunge * 0.3F;
        }

        // ── death collapse : body tips sideways, legs splay, head drops ──
        // LivingEntity.deathTime counts 0..20 (1 second). We collapse over
        // the first 10 ticks, then hold the pose for the remaining fade.
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F); // 0→1 over 0.4s (visible before fade)
            float collapse = t * t; // quadratic ease-in for weight
            this.root.xRot = collapse * -0.4F;                // body tips to side
            this.root.zRot = collapse * 0.6F;                 // rolls onto side
            this.head.xRot = collapse * 0.8F;                  // head droops
            this.head.zRot = collapse * 0.3F;                  // head lolls
            // legs splay outward
            this.frontLeftThigh.zRot  = -collapse * 0.5F;
            this.frontRightThigh.zRot =  collapse * 0.5F;
            this.backLeftThigh.zRot   = -collapse * 0.4F;
            this.backRightThigh.zRot  =  collapse * 0.4F;
            // tail goes limp
            this.tailBase.xRot = 0.3F + collapse * 1.2F;
            this.tailMid.xRot  = collapse * 0.5F;
            this.tailTip.xRot  = collapse * 0.3F;
            // jaw falls open (relaxed death)
            this.jaw.xRot = collapse * 0.6F;
        }
    }
}
