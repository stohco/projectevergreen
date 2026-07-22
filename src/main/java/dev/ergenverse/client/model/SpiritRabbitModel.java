package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/spirit_rabbit.png  SIZE: 32x32
/*
 * SpiritRabbitModel — small round-bodied leaping lagomorph.
 *
 * ANATOMY:
 *   - body     : round box (4 x 4 x 5) low to the ground
 *   - head     : skull (3x3x3) + nose (1x1x1) at the front, child of body
 *   - ears     : 2 long tall thin boxes (1x4x1), independently rotatable
 *   - legs     : 4 very short legs (front 1x2x1, back 2x2x2 haunches)
 *   - tail     : tiny puff box (1x1x1) at the rear
 *
 * ANIMATION:
 *   - Hop      : when moving, body bounces root.y = abs(sin(swing*0.5))*2*swingAmt;
 *                 ears flap back, front legs tuck, back legs extend.
 *   - Idle     : nose twitches (head small sin jitter on xRot), ears rotate
 *                 listening earL.zRot = sin(age*0.3)*0.2; tail wiggles.
 *   - Alert    : ears snap straight up (zRot ~ 0), body still, head up.
 *
 * HARSH SELF-CRITIQUE:
 *   - Body is a single round box; real rabbits have a distinct rounded rump
 *     and a narrower shoulders/chest. Looks like a potato with legs.
 *   - Ears are uniform 1x4x1 slabs; real rabbit ears taper to a rounded tip
 *     and have visible inner-ear pink. No curve, no taper.
 *   - No whiskers, no visible incisors, no eye boxes — face is a blank box
 *     that relies entirely on texture for features.
 *   - Legs are stubs; real rabbit hind legs are LONG (the leap engine) with
 *     a distinct thigh, hock, and long foot. Mine are 2x2x2 haunch blobs.
 *   - Hop is a vertical bounce only; real rabbits also pitch forward on the
 *     leap and tuck forelegs tight to the chest. No body pitch.
 *   - No "thumping" warning animation (rabbits thump hind feet when alarmed).
 *   - No per-ear independent alert tracking — both ears use the same idle
 *     wave just phase-offset, not true independent orientation toward sounds.
 *   - Texture UVs invented; existing spirit_rabbit.png (vanilla RabbitModel
 *     layout) will scramble on this 32x32 layout.
 */
import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class SpiritRabbitModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart earLeft;
    private final ModelPart earRight;
    private final ModelPart frontLegLeft;
    private final ModelPart frontLegRight;
    private final ModelPart backLegLeft;
    private final ModelPart backLegRight;
    private final ModelPart tail;

    public SpiritRabbitModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.earLeft = this.head.getChild("ear_left");
        this.earRight = this.head.getChild("ear_right");
        this.frontLegLeft = root.getChild("front_leg_left");
        this.frontLegRight = root.getChild("front_leg_right");
        this.backLegLeft = root.getChild("back_leg_left");
        this.backLegRight = root.getChild("back_leg_right");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── body : round, low to the ground ──────────────────────────────
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.0F, -2.0F, -2.5F, 4.0F, 4.0F, 5.0F),
                PartPose.offset(0.0F, 11.0F, 0.0F));

        // ── head : at the front, slightly up ─────────────────────────────
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 10)
                        .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F)   // skull
                        .texOffs(14, 10)
                        .addBox(-0.5F, 0.0F, -2.5F, 1.0F, 1.0F, 1.0F),   // nose forward
                PartPose.offset(0.0F, 10.0F, -3.0F));
        // ears : long tall thin boxes, can rotate independently
        head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(14, 14)
                        .addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offset(-1.0F, -1.5F, 0.0F));
        head.addOrReplaceChild("ear_right",
                CubeListBuilder.create().texOffs(20, 14)
                        .addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offset(1.0F, -1.5F, 0.0F));

        // ── legs : 4 short (front stubs, back haunches) ──────────────────
        root.addOrReplaceChild("front_leg_left",
                CubeListBuilder.create().texOffs(0, 17)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(-1.0F, 13.0F, -2.0F));
        root.addOrReplaceChild("front_leg_right",
                CubeListBuilder.create().texOffs(5, 17)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(1.0F, 13.0F, -2.0F));
        root.addOrReplaceChild("back_leg_left",
                CubeListBuilder.create().texOffs(10, 17)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(-1.5F, 12.0F, 2.0F));
        root.addOrReplaceChild("back_leg_right",
                CubeListBuilder.create().texOffs(20, 17)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(1.5F, 12.0F, 2.0F));

        // ── puff tail : at the rear ──────────────────────────────────────
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(28, 10)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 11.0F, 3.0F));

        return LayerDefinition.create(mesh, 32, 32);
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

        // CRON-COMPLETIONIST-13: DATA_POSE for grazing posture
        boolean poseGrazing = entity.getSpiritPose() == SpiritBeastEntity.POSE_GRAZING;

        // ── CRON-COMPLETIONIST-16: POSE_RESTING — rabbit crouches flat ──
        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;
        // ── CRON-COMPLETIONIST-16: POSE_SWIMMING — rabbit paddles ──
        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING;

        if (resting) {
            // Rabbit rests: body flattens, ears relax to sides, legs tucked
            this.root.y = -3.0F;
            this.root.xRot = 0.1F;
            this.frontLegLeft.xRot  = 0.3F;
            this.frontLegRight.xRot = 0.3F;
            this.backLegLeft.xRot   = 0.2F;
            this.backLegRight.xRot  = 0.2F;
            this.earLeft.zRot  = 0.8F;   // ears flop to sides
            this.earRight.zRot = -0.8F;
            this.earLeft.xRot  = 0.3F;
            this.earRight.xRot = 0.3F;
            this.head.xRot = 0.1F;
            return;
        } else if (swimming) {
            // Rabbit swims: body pitches up, head above water, legs paddle frantically
            this.root.xRot = -0.4F;
            this.root.y = -2.0F;
            this.head.xRot = -0.3F;
            float paddle = ageInTicks * 1.5F;
            this.frontLegLeft.xRot  = (float) Math.cos(paddle) * 1.0F;
            this.frontLegRight.xRot = (float) Math.cos(paddle + Math.PI) * 1.0F;
            this.backLegLeft.xRot   = (float) Math.cos(paddle + Math.PI) * 1.2F;
            this.backLegRight.xRot  = (float) Math.cos(paddle) * 1.2F;
            this.earLeft.xRot  = -0.4F;   // ears pinned back
            this.earRight.xRot = -0.4F;
            this.earLeft.zRot  = 0.0F;
            this.earRight.zRot = 0.0F;
            return;
        }

        if (limbSwingAmount > 0.05F) {
            // ── HOP : body bounces, ears flap back, legs tuck/extend ──────
            float hop = (float) Math.abs(Math.sin(limbSwing * 0.5F)) * 2.0F * limbSwingAmount;
            this.root.y = -hop;                       // body rises (negative Y = up)
            // ears flap backward with the leap
            this.earLeft.xRot  = -0.6F * limbSwingAmount;
            this.earRight.xRot = -0.6F * limbSwingAmount;
            // front legs tuck up, back legs extend for push-off
            this.frontLegLeft.xRot  = -0.8F * limbSwingAmount;
            this.frontLegRight.xRot = -0.8F * limbSwingAmount;
            this.backLegLeft.xRot   =  0.6F * limbSwingAmount;
            this.backLegRight.xRot  =  0.6F * limbSwingAmount;
            // head pitches slightly forward in the leap
            this.head.xRot += -0.2F * limbSwingAmount;
        } else {
            // ── IDLE : nose twitches, ears listen, tail wiggles ───────────
            this.root.y = 0.0F;
            // nose twitch — small rapid jitter on head pitch
            this.head.xRot += (float) Math.sin(ageInTicks * 1.5F) * 0.04F;
            // ears rotate listening (independent phases)
            this.earLeft.zRot  = (float) Math.sin(ageInTicks * 0.3F)        * 0.2F;
            this.earRight.zRot = (float) Math.sin(ageInTicks * 0.3F + 0.7F) * 0.2F;
            this.earLeft.xRot  = (float) Math.sin(ageInTicks * 0.2F)        * 0.1F;
            this.earRight.xRot = (float) Math.sin(ageInTicks * 0.2F + 0.5F) * 0.1F;
            // legs relaxed
            this.frontLegLeft.xRot  = 0.0F;
            this.frontLegRight.xRot = 0.0F;
            this.backLegLeft.xRot   = 0.0F;
            this.backLegRight.xRot  = 0.0F;
            // tail wiggle
            this.tail.yRot = (float) Math.sin(ageInTicks * 0.8F) * 0.3F;
        }

 // ── alert snap : if recently hit (hurtTime > 0), ears straight up ──
        if (entity.hurtTime > 0) {
            this.earLeft.zRot  = 0.0F;
            this.earRight.zRot = 0.0F;
            this.earLeft.xRot  = 0.0F;
            this.earRight.xRot = 0.0F;
            this.head.xRot = -0.3F;
        }

        // ── attack : rabbit kicks with back legs ────────────────────
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float kick = (float) Math.sin(atk * Math.PI);
            // back legs extend in a powerful kick
            this.backLegLeft.xRot  = kick * 1.5F;
            this.backLegRight.xRot = kick * 1.5F;
            // body pitches slightly back (recoil)
            this.root.xRot = -kick * 0.3F;
            // front legs tuck tight
            this.frontLegLeft.xRot  = -kick * 0.6F;
            this.frontLegRight.xRot = -kick * 0.6F;
        }

        // ── death : rabbit flips onto side, legs splay ────────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F);
            float collapse = t * t;
            // body tips and rolls
            this.root.xRot = collapse * -0.5F;
            this.root.zRot = collapse * 0.8F;
            // head droops
            this.head.xRot = collapse * 0.5F;
            this.head.zRot = collapse * 0.4F;
            // ears flatten
            this.earLeft.zRot = collapse * 0.6F;
            this.earRight.zRot = -collapse * 0.6F;
            // all legs splay
            this.frontLegLeft.zRot  = -collapse * 0.3F;
            this.frontLegRight.zRot =  collapse * 0.3F;
            this.backLegLeft.zRot   = -collapse * 0.4F;
            this.backLegRight.zRot  =  collapse * 0.4F;
        }
    }
}
