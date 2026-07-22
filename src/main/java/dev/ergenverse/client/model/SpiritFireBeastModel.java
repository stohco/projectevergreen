package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/fire_beast.png  SIZE: 64x64
/*
 * SpiritFireBeastModel — wolf-like predator wreathed in flickering flame.
 *
 * ANATOMY:
 *   - body        : bulky torso (5 x 6 x 10) at chest height
 *   - neck        : short thick connector
 *   - head        : skull (3x3x3) + wide jaw (separate child) + 2 ember eyes
 *                   (small bright boxes) + 2 small horns
 *   - flame mane  : 5 tall thin boxes along the spine, each flickers
 *                   independently via per-segment phase offset
 *   - tail        : 2-segment bony tail + flame tip (3 angled flame slabs)
 *   - legs        : 4 legs, 2 segments each, feet at y=15
 *
 * ANIMATION:
 *   - Walk gait   : diagonal trot, cos(swing*0.6662)*amp*swingAmt, shins
 *                   counter-flex.
 *   - Run         : swing frequency x1.5, amp 1.3 when swingAmt > 0.5.
 *   - Flame mane  : each segment flickers maneSeg[i].yRot = sin(age*0.8 + i*0.5)*0.1
 *                   and pulses scale maneSeg[i].yScale = 1 + sin(age*0.8 + i*0.5)*0.15.
 *                   The pulse gives a "lickering flame" feel without particles.
 *   - Rage roar   : when entity.getTarget() != null, head rears up, jaw drops
 *                   wide, mane flares (yScale boosted), tail flame elongates.
 *   - Head turn   : head.yRot = netHeadYaw * deg2rad (clamped); head.xRot
 *                   = headPitch * deg2rad, plus rage override.
 *   - Idle        : breathing + mane slow flicker; ember eyes static glow.
 *
 * HARSH SELF-CRITIQUE:
 *   - "Flames" are flat box slabs with scale pulsing — they look like
 *     wobbling cards, not fire. A real flame effect needs particle emitters,
 *     a scrolling shader, or at least gradient-textured tapered cones with
 *     additive blending. This is the cheapest possible fake.
 *   - Ember eyes are just small cubes; they should be full-bright (rendered
 *     with FullBright light) but this model has no control over the light
 *     parameter per-part. The renderer must override to force 15728880 light
 *     on the eye cubes, OR a separate emissive layer is needed.
 *   - Body is a single box — no muscle definition, no ribcage, no haunches.
 *     A "fire beast" should look more demonic / less like a boxy wolf.
 *   - Jaw is one piece; real predator jaws have an upper and lower that
 *     separate. The upper jaw is fused to the skull here.
 *   - Horns are tiny 1x1x1 cubes — should be curved tapered cones sweeping
 *     back from the brow.
 *   - Flame mane does not emit light to the world (no DynamicLight hook).
 *     Canon: a fire beast SHOULD illuminate its surroundings. Needs a
 *     DynamicLight integration, which is out of scope for a model file.
 *   - No heat-distortion, no smoke, no ember particles falling off.
 *   - Rage roar is triggered by getTarget() which fires on aggro, not on the
 *     actual roar sound/event. Needs a synced flag from the entity's AI.
 *   - Texture UVs invented; existing fire_beast.png (laid out for vanilla
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

public class SpiritFireBeastModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart head;

    /** Public accessor for the emissive renderer to re-render the head at fullbright. */
    public ModelPart getHeadPart() { return this.head; }
    private final ModelPart jaw;
    private final ModelPart mane0;
    private final ModelPart mane1;
    private final ModelPart mane2;
    private final ModelPart mane3;
    private final ModelPart mane4;
    private final ModelPart tailBase;
    private final ModelPart flameTip;
    private final ModelPart frontLeftThigh;
    private final ModelPart frontLeftShin;
    private final ModelPart frontRightThigh;
    private final ModelPart frontRightShin;
    private final ModelPart backLeftThigh;
    private final ModelPart backLeftShin;
    private final ModelPart backRightThigh;
    private final ModelPart backRightShin;

    public SpiritFireBeastModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.jaw = this.head.getChild("jaw");
        // mane segments are children of the body (attached to the spine),
        // so fetch the body first, then its mane children.
        ModelPart body = root.getChild("body");
        this.mane0 = body.getChild("mane_0");
        this.mane1 = body.getChild("mane_1");
        this.mane2 = body.getChild("mane_2");
        this.mane3 = body.getChild("mane_3");
        this.mane4 = body.getChild("mane_4");
        this.tailBase = root.getChild("tail_base");
        this.flameTip = this.tailBase.getChild("flame_tip");
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

        // ── body : bulky torso ───────────────────────────────────────────
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.5F, -3.0F, -5.0F, 5.0F, 6.0F, 10.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));

        // ── flame mane : 5 segments along the spine ──────────────────────
        // Each is a tall thin box standing on the back, flickering in setupAnim.
        for (int i = 0; i < 5; i++) {
            float z = -4.0F + i * 2.0F;       // along the spine from neck to hip
            body.addOrReplaceChild("mane_" + i,
                    CubeListBuilder.create().texOffs(40, 0 + i * 6)
                            .addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                    PartPose.offset(0.0F, -3.0F, z));
        }

        // ── neck : short thick connector ─────────────────────────────────
        root.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(28, 0)
                        .addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, -5.0F, -0.4F, 0.0F, 0.0F));

        // ── head : skull + jaw + eyes + horns (child of neck) ────────────
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 18)
                        .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F)    // skull
                        .texOffs(0, 26)
                        .addBox(-1.5F, 0.0F, -3.5F, 3.0F, 1.5F, 2.0F),    // upper jaw / snout
                PartPose.offset(0.0F, -1.0F, -4.0F));
        // lower jaw : separate, can drop
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(28, 18)
                        .addBox(-1.5F, 0.0F, -3.0F, 3.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 0.5F, -0.5F));
        // ember eyes : small bright boxes
        head.addOrReplaceChild("eye_left",
                CubeListBuilder.create().texOffs(44, 32)
                        .addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(-1.0F, -0.5F, -1.2F));
        head.addOrReplaceChild("eye_right",
                CubeListBuilder.create().texOffs(48, 32)
                        .addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(1.0F, -0.5F, -1.2F));
        // small horns sweeping back from the brow
        head.addOrReplaceChild("horn_left",
                CubeListBuilder.create().texOffs(52, 0)
                        .addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-1.2F, -1.5F, 0.5F, -0.5F, 0.0F, -0.3F));
        head.addOrReplaceChild("horn_right",
                CubeListBuilder.create().texOffs(52, 4)
                        .addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(1.2F, -1.5F, 0.5F, -0.5F, 0.0F, 0.3F));

        // ── tail : bony base + flame tip ─────────────────────────────────
        PartDefinition tailBase = root.addOrReplaceChild("tail_base",
                CubeListBuilder.create().texOffs(36, 8)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 5.0F, 0.2F, 0.0F, 0.0F));
        PartDefinition flameTip = tailBase.addOrReplaceChild("flame_tip",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 3.0F));
        flameTip.addOrReplaceChild("flame1",
                CubeListBuilder.create().texOffs(40, 36)
                        .addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        flameTip.addOrReplaceChild("flame2",
                CubeListBuilder.create().texOffs(44, 36)
                        .addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.4F, 0.0F));
        flameTip.addOrReplaceChild("flame3",
                CubeListBuilder.create().texOffs(48, 36)
                        .addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.4F, 0.0F));

        // ── legs : 4 legs, thigh + shin, feet at y=15 ────────────────────
        root.addOrReplaceChild("front_left_thigh",
                CubeListBuilder.create().texOffs(0, 32).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(-2.5F, 9.0F, -4.0F));
        root.getChild("front_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("front_right_thigh",
                CubeListBuilder.create().texOffs(8, 32).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(2.5F, 9.0F, -4.0F));
        root.getChild("front_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(8, 40).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("back_left_thigh",
                CubeListBuilder.create().texOffs(0, 48).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(-2.5F, 9.0F, 4.0F));
        root.getChild("back_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 56).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("back_right_thigh",
                CubeListBuilder.create().texOffs(8, 48).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(2.5F, 9.0F, 4.0F));
        root.getChild("back_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(8, 56).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
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

        // ── CRON-COMPLETIONIST-16: POSE_RESTING — fire beast lies down, flames dim ──
        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;
        // ── CRON-COMPLETIONIST-16: POSE_SWIMMING — fire beast swims (reluctantly) ──
        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING;

        ModelPart[] mane = { this.mane0, this.mane1, this.mane2, this.mane3, this.mane4 };

        if (resting) {
            // Fire beast rests: body lowers, legs fold, flames shrink to embers
            this.root.y = -2.0F;
            this.frontLeftThigh.xRot  = -0.6F;
            this.frontRightThigh.xRot = -0.6F;
            this.frontLeftShin.xRot   = 0.4F;
            this.frontRightShin.xRot  = 0.4F;
            this.backLeftThigh.xRot   = 0.3F;
            this.backRightThigh.xRot  = 0.3F;
            this.backLeftShin.xRot    = -0.2F;
            this.backRightShin.xRot   = -0.2F;
            this.head.xRot = 0.3F;
            this.jaw.xRot = 0.0F;
            // Flames dim to low embers
            for (int i = 0; i < mane.length; i++) {
                mane[i].yScale = 0.3F;
                mane[i].xScale = 0.5F;
            }
            this.flameTip.yScale = 0.2F;
            this.tailBase.xRot = 0.5F;
            return;
        } else if (swimming) {
            // Fire beast swims: body pitches, legs paddle, flames sputter
            this.root.xRot = -0.3F;
            this.root.y = -1.0F;
            this.head.xRot = -0.4F;
            float paddle = ageInTicks * 1.0F;
            this.frontLeftThigh.xRot  = (float) Math.cos(paddle) * 0.7F;
            this.frontRightThigh.xRot = (float) Math.cos(paddle + Math.PI) * 0.7F;
            this.backLeftThigh.xRot   = (float) Math.cos(paddle + Math.PI) * 0.5F;
            this.backRightThigh.xRot  = (float) Math.cos(paddle) * 0.5F;
            this.frontLeftShin.xRot   = -0.2F + Math.abs((float) Math.cos(paddle)) * 0.3F;
            this.frontRightShin.xRot  = -0.2F + Math.abs((float) Math.cos(paddle + Math.PI)) * 0.3F;
            this.backLeftShin.xRot    = -0.2F + Math.abs((float) Math.cos(paddle + Math.PI)) * 0.2F;
            this.backRightShin.xRot   = -0.2F + Math.abs((float) Math.cos(paddle)) * 0.2F;
            // Flames sputter in water (reduced scale, fast flicker)
            for (int i = 0; i < mane.length; i++) {
                float p = ageInTicks * 2.0F + i * 0.5F;
                mane[i].yScale = 0.5F + (float) Math.sin(p) * 0.1F;
                mane[i].yRot   = (float) Math.sin(p) * 0.15F;
            }
            this.flameTip.yScale = 0.3F;
            this.jaw.xRot = 0.0F;
            return;
        }

        // ── walk / run gait ──────────────────────────────────────────────
        boolean running = limbSwingAmount > 0.5F;
        float swingPhase = running ? limbSwing * 1.5F : limbSwing;
        float amp = (running ? 1.3F : 0.8F) * limbSwingAmount;
        float phase = swingPhase * 0.6662F;

        this.frontLeftThigh.xRot  = (float) Math.cos(phase)            * amp;
        this.frontRightThigh.xRot = (float) Math.cos(phase + Math.PI)  * amp;
        this.backLeftThigh.xRot   = (float) Math.cos(phase + Math.PI)  * amp;
        this.backRightThigh.xRot  = (float) Math.cos(phase)            * amp;

        this.frontLeftShin.xRot  = -0.3F + Math.max(0.0F, (float) Math.cos(phase))            * 0.6F * limbSwingAmount;
        this.frontRightShin.xRot = -0.3F + Math.max(0.0F, (float) Math.cos(phase + Math.PI))  * 0.6F * limbSwingAmount;
        this.backLeftShin.xRot   = -0.3F + Math.max(0.0F, (float) Math.cos(phase + Math.PI))  * 0.6F * limbSwingAmount;
        this.backRightShin.xRot  = -0.3F + Math.max(0.0F, (float) Math.cos(phase))            * 0.6F * limbSwingAmount;

        // ── flame mane flicker : per-segment phase offset ───────────────
        // Rotation flicker + vertical scale pulse = "lickering flame" feel.
        // CRON-COMPLETIONIST-13: Check DATA_POSE in addition to getTarget()
        boolean rage = entity.getTarget() != null
                || entity.getSpiritPose() == SpiritBeastEntity.POSE_CHARGING;
        float flare = rage ? 0.4F : 0.0F;            // mane flares bigger when raging
        for (int i = 0; i < mane.length; i++) {
            float p = ageInTicks * 0.8F + i * 0.5F;
            mane[i].yRot   = (float) Math.sin(p) * 0.10F;
            mane[i].xRot   = (float) Math.cos(p) * 0.05F;
            mane[i].yScale = 1.0F + (float) Math.sin(p) * 0.15F + flare;
            mane[i].xScale = 1.0F + (float) Math.cos(p) * 0.08F;
            mane[i].zScale = 1.0F + (float) Math.sin(p + 1.0F) * 0.08F;
        }

        // ── tail flame tip flicker ───────────────────────────────────────
        float tf = ageInTicks * 1.0F;
        this.flameTip.yRot      = (float) Math.sin(tf) * 0.2F;
        this.flameTip.yScale    = 1.0F + (float) Math.sin(tf) * 0.2F + flare;
        this.tailBase.yRot      = (float) Math.sin(ageInTicks * 0.2F) * 0.2F;

        // ── breathing ────────────────────────────────────────────────────
        this.root.y = (float) Math.sin(ageInTicks * 0.1F) * 0.1F;

        // ── rage roar : head up, jaw wide ────────────────────────────────
        if (rage) {
            this.head.xRot -= 0.4F;             // head rears up
            this.jaw.xRot = 0.7F;                // jaw drops wide
        } else {
            this.jaw.xRot = 0.0F;
        }

        // ── attack lunge : fire beast surges forward, mane flares ──────
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float lunge = (float) Math.sin(atk * Math.PI);
            this.root.xRot = -lunge * 0.7F;                 // body pitches forward
            this.head.xRot -= lunge * 1.0F;                 // head snaps forward aggressively
            this.jaw.xRot = lunge * 0.8F;                    // jaw wide open
            // front legs push, back legs anchor
            this.frontLeftThigh.xRot  += lunge * 0.5F;
            this.frontRightThigh.xRot += lunge * 0.5F;
            this.backLeftThigh.xRot   -= lunge * 0.4F;
            this.backRightThigh.xRot  -= lunge * 0.4F;
            // mane flares bigger during attack
            float atkFlare = lunge * 0.6F;
            for (int i = 0; i < mane.length; i++) {
                mane[i].yScale += atkFlare;
                mane[i].xScale += atkFlare * 0.5F;
            }
            // flame tip extends
            this.flameTip.yScale += lunge * 0.5F;
        }

        // ── death collapse : fire beast slumps, flames wither ─────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F); // 0→1 over 0.4s (visible before fade)
            float collapse = t * t;
            this.root.xRot = collapse * -0.5F;
            this.root.zRot = collapse * 0.5F;
            this.head.xRot = collapse * 0.6F;
            this.head.zRot = collapse * 0.4F;
            this.frontLeftThigh.zRot  = -collapse * 0.5F;
            this.frontRightThigh.zRot =  collapse * 0.5F;
            this.backLeftThigh.zRot   = -collapse * 0.4F;
            this.backRightThigh.zRot  =  collapse * 0.4F;
            // jaw falls open, flames die down
            this.jaw.xRot = collapse * 0.8F;
            for (int i = 0; i < mane.length; i++) {
                mane[i].yScale *= (1.0F - collapse * 0.7F);
                mane[i].xScale *= (1.0F - collapse * 0.7F);
            }
            this.flameTip.yScale *= (1.0F - collapse * 0.8F);
        }
    }
}
