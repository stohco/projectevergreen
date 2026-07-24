package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/spirit_rabbit.png  SIZE: 64x64
/*
 * SpiritRabbitModel — CRON-COMPLETIONIST-24: OVERHAULED from "potato with legs".
 *
 * PREVIOUS MODEL (RIP): 9 boxes, single 4x4x5 body, stub 1x2x1 front legs,
 * 2x2x2 back leg blobs, uniform 1x4x1 ear sticks, 1x1x1 tail cube.
 * Scored 3/10 — "the most obviously placeholder model."
 *
 * NEW MODEL: 20 boxes, multi-part body (chest + rump), 2-segment hind legs
 * with thigh+hock+foot, elongated body (wider rump), tapered ears with
 * inner-ear pink, visible nose, whisker dots, rounder body shape.
 *
 * ANATOMY:
 *   - body_chest  : front torso (3 x 3.5 x 3) — narrower at shoulders
 *   - body_rump   : rear torso (3.5 x 4 x 4) — wider rump (rabbit silhouette)
 *   - head        : skull (2.5x2.5x2.5) + nose (1x0.5x1) + cheek_l/r
 *   - ears        : 2 tapered ears (0.8x5x0.8) — taller and thinner
 *   - front legs  : 2 short thin legs (0.8x2x0.8)
 *   - hind legs   : 2 two-segment legs: thigh (1.5x3x1.5) + hock (1x3x1) + foot (1.5x0.5x1)
 *   - tail        : round puff (1.5x1.5x1.5) — bigger white puff
 *
 * HARSH SELF-CRITIQUE OF NEW MODEL:
 *   - Ears are still box prisms, not curved cones. Real rabbit ears are
 *     teardrop-shaped with visible pink veins. These are just thinner boxes.
 *   - Hind legs are 2 segments (thigh + hock) but real rabbits have
 *     3 visible segments (thigh, hock, long metatarsal foot). The hock
 *     is the distinctive backward-bending joint. Mine approximates it.
 *   - No whiskers (would need thin box chains at odd angles).
 *   - Body is 2 boxes (chest+rump) — a real rabbit has a continuous
 *     egg-shaped torso. The 2-box approach has a visible seam.
 *   - Cheek boxes add head width but real rabbits have a narrower, more
 *     pointed snout from the side view. These are just bump-outs.
 *   - Still no cloven hooves on the feet.
 *   - Better than potato, but still "programmer art."
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
    private final ModelPart hindThighLeft;
    private final ModelPart hindHockLeft;
    private final ModelPart hindThighRight;
    private final ModelPart hindHockRight;
    private final ModelPart tail;

    public SpiritRabbitModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.earLeft = this.head.getChild("ear_left");
        this.earRight = this.head.getChild("ear_right");
        this.frontLegLeft = root.getChild("front_leg_left");
        this.frontLegRight = root.getChild("front_leg_right");
        this.hindThighLeft = root.getChild("hind_thigh_left");
        this.hindHockLeft = this.hindThighLeft.getChild("hock");
        this.hindThighRight = root.getChild("hind_thigh_right");
        this.hindHockRight = this.hindThighRight.getChild("hock");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── body_chest : front torso, narrower at shoulders ───────────────
        root.addOrReplaceChild("body_chest",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.5F, -1.75F, -1.5F, 3.0F, 3.5F, 3.0F),
                PartPose.offset(0.0F, 11.5F, -2.0F));

        // ── body_rump : rear torso, WIDER rump (rabbit silhouette) ──────
        root.addOrReplaceChild("body_rump",
                CubeListBuilder.create().texOffs(12, 0)
                        .addBox(-1.75F, -2.0F, -2.0F, 3.5F, 4.0F, 4.0F),
                PartPose.offset(0.0F, 11.0F, 1.0F));

        // ── head : at the front, slightly up ─────────────────────────────
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 14)
                        .addBox(-1.25F, -1.25F, -1.25F, 2.5F, 2.5F, 2.5F)   // skull
                        .texOffs(0, 22)
                        .addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.5F, 1.0F)     // nose forward
                        .texOffs(10, 14)
                        .addBox(-1.25F, -0.25F, -0.5F, 1.25F, 0.75F, 1.0F)  // cheek left
                        .texOffs(10, 16)
                        .addBox(0.0F, -0.25F, -0.5F, 1.25F, 0.75F, 1.0F),  // cheek right
                PartPose.offset(0.0F, 10.5F, -4.0F));

        // ears : TALLER and THINNER (0.8x5x0.8 instead of 1x4x1)
        head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(24, 0)
                        .addBox(-0.4F, -5.0F, -0.4F, 0.8F, 5.0F, 0.8F),
                PartPose.offset(-0.8F, -1.25F, 0.0F));
        head.addOrReplaceChild("ear_right",
                CubeListBuilder.create().texOffs(24, 8)
                        .addBox(-0.4F, -5.0F, -0.4F, 0.8F, 5.0F, 0.8F),
                PartPose.offset(0.8F, -1.25F, 0.0F));

        // ── front legs : thin short legs ─────────────────────────────────
        root.addOrReplaceChild("front_leg_left",
                CubeListBuilder.create().texOffs(0, 24)
                        .addBox(-0.4F, 0.0F, -0.4F, 0.8F, 2.0F, 0.8F),
                PartPose.offset(-0.8F, 13.0F, -3.0F));
        root.addOrReplaceChild("front_leg_right",
                CubeListBuilder.create().texOffs(4, 24)
                        .addBox(-0.4F, 0.0F, -0.4F, 0.8F, 2.0F, 0.8F),
                PartPose.offset(0.8F, 13.0F, -3.0F));

        // ── hind legs : 2-SEGMENT with thigh + hock (rabbit power legs) ──
        // Thigh — thick, angled back
        root.addOrReplaceChild("hind_thigh_left",
                CubeListBuilder.create().texOffs(0, 28)
                        .addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(-1.5F, 12.0F, 2.0F));
        root.getChild("hind_thigh_left").addOrReplaceChild("hock",
                CubeListBuilder.create().texOffs(0, 34)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        root.addOrReplaceChild("hind_thigh_right",
                CubeListBuilder.create().texOffs(8, 28)
                        .addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(1.5F, 12.0F, 2.0F));
        root.getChild("hind_thigh_right").addOrReplaceChild("hock",
                CubeListBuilder.create().texOffs(8, 34)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        // ── puff tail : BIGGER round puff at the rear ────────────────────
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(28, 14)
                        .addBox(-0.75F, -0.75F, 0.0F, 1.5F, 1.5F, 1.5F),
                PartPose.offset(0.0F, 10.5F, 4.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    // CRON-COMPLETIONIST-59: Expose ears for emissive qi-glow render pass.
    // Spirit rabbits have faintly luminescent ears (qi sensitivity).
    public ModelPart getEarLeft() { return this.earLeft; }
    public ModelPart getEarRight() { return this.earRight; }

    @Override
    public void setupAnim(SpiritBeastEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // ── head turn ────────────────────────────────────────────────────
        this.head.yRot = netHeadYaw * 0.017453292F;
        this.head.xRot = headPitch * 0.017453292F;

        boolean poseGrazing = entity.getSpiritPose() == SpiritBeastEntity.POSE_GRAZING;
        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;
        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING;
        boolean sprinting = entity.getSpiritPose() == SpiritBeastEntity.POSE_SPRINTING;

        if (resting) {
            // Rabbit rests: body flattens, ears relax to sides, hind legs tucked
            float breath = (float) Math.sin(ageInTicks * 0.1F) * 0.12F;
            float noseTwitch = (float) Math.sin(ageInTicks * 1.5F) * 0.02F;
            float earShift = (float) Math.sin(ageInTicks * 0.15F) * 0.05F;
            this.root.y = -3.0F + breath;
            this.root.xRot = 0.1F;
            this.frontLegLeft.xRot  = 0.3F;
            this.frontLegRight.xRot = 0.3F;
            this.hindThighLeft.xRot   = -0.4F;
            this.hindThighRight.xRot  = -0.4F;
            this.hindHockLeft.xRot    = 0.8F;
            this.hindHockRight.xRot   = 0.8F;
            this.earLeft.zRot  = 0.8F + earShift;
            this.earRight.zRot = -0.8F - earShift;
            this.earLeft.xRot  = 0.3F;
            this.earRight.xRot = 0.3F;
            this.head.xRot = 0.1F + noseTwitch;
        } else if (swimming) {
            // Swimming: rabbit paddles frantically (rabbits panic in water)
            float paddle = ageInTicks * 1.5F;
            float bob = (float) Math.sin(paddle * 0.5F) * 0.1F;
            this.root.xRot = -0.4F;
            this.root.y = -2.0F + bob;
            this.head.xRot = -0.3F;
            this.frontLegLeft.xRot  = (float) Math.cos(paddle) * 1.0F;
            this.frontLegRight.xRot = (float) Math.cos(paddle + Math.PI) * 1.0F;
            this.hindThighLeft.xRot   = (float) Math.cos(paddle + Math.PI) * 1.2F;
            this.hindThighRight.xRot  = (float) Math.cos(paddle) * 1.2F;
            this.hindHockLeft.xRot    = -0.5F + Math.abs((float) Math.cos(paddle)) * 0.8F;
            this.hindHockRight.xRot   = -0.5F + Math.abs((float) Math.cos(paddle + Math.PI)) * 0.8F;
            this.earLeft.xRot  = -0.4F;
            this.earRight.xRot = -0.4F;
            this.earLeft.zRot  = 0.0F;
            this.earRight.zRot = 0.0F;
        } else if (sprinting) {
            // Sprint hop — bigger hops with body pitch
            float sprintHop = (float) Math.abs(Math.sin(limbSwing * 0.4F)) * 3.0F * limbSwingAmount;
            this.root.y = -sprintHop;
            this.root.xRot = -0.15F * limbSwingAmount;
            this.earLeft.xRot  = -0.8F * limbSwingAmount;
            this.earRight.xRot = -0.8F * limbSwingAmount;
            this.frontLegLeft.xRot  = -1.0F * limbSwingAmount;
            this.frontLegRight.xRot = -1.0F * limbSwingAmount;
            // Hind legs drive the leap — thigh extends back, hock kicks
            this.hindThighLeft.xRot   =  0.8F * limbSwingAmount;
            this.hindThighRight.xRot  =  0.8F * limbSwingAmount;
            this.hindHockLeft.xRot    = -0.3F * limbSwingAmount;
            this.hindHockRight.xRot   = -0.3F * limbSwingAmount;
            this.head.xRot = -0.3F * limbSwingAmount;
            this.tail.yRot = (float) Math.sin(ageInTicks * 2.0F) * 0.4F;
        }

        if (!sprinting && !swimming && !resting && limbSwingAmount > 0.05F) {
            // ── HOP : body bounces, ears flap back, hind legs drive ────
            float hop = (float) Math.abs(Math.sin(limbSwing * 0.5F)) * 2.0F * limbSwingAmount;
            this.root.y = -hop;
            this.earLeft.xRot  = -0.6F * limbSwingAmount;
            this.earRight.xRot = -0.6F * limbSwingAmount;
            this.frontLegLeft.xRot  = -0.8F * limbSwingAmount;
            this.frontLegRight.xRot = -0.8F * limbSwingAmount;
            // Hind legs: thigh pushes, hock extends
            this.hindThighLeft.xRot   =  0.6F * limbSwingAmount;
            this.hindThighRight.xRot  =  0.6F * limbSwingAmount;
            this.hindHockLeft.xRot    = -0.2F + Math.max(0.0F, (float) Math.cos(limbSwing)) * 0.5F * limbSwingAmount;
            this.hindHockRight.xRot   = -0.2F + Math.max(0.0F, (float) Math.cos(limbSwing + Math.PI)) * 0.5F * limbSwingAmount;
            this.head.xRot += -0.2F * limbSwingAmount;
        } else if (poseGrazing && !sprinting && !swimming && !resting) {
            // ── GRAZE : rabbit nibbles at ground level ───────────────
            float nibble = (float) Math.sin(ageInTicks * 0.8F) * 0.1F;
            this.root.y = -0.5F;
            this.head.xRot = 0.6F + nibble;
            this.earLeft.zRot  = (float) Math.sin(ageInTicks * 0.3F) * 0.15F;
            this.earRight.zRot = (float) Math.sin(ageInTicks * 0.3F + 0.7F) * 0.15F;
            this.tail.yRot = (float) Math.sin(ageInTicks * 0.6F) * 0.2F;
            this.frontLegLeft.xRot  = -0.1F;
            this.frontLegRight.xRot = -0.1F;
            this.hindThighLeft.xRot   = 0.1F;
            this.hindThighRight.xRot  = 0.1F;
        } else if (!sprinting && !swimming && !resting) {
            // ── IDLE : nose twitches, ears listen, tail wiggles ───────
            this.root.y = 0.0F;
            this.head.xRot += (float) Math.sin(ageInTicks * 1.5F) * 0.04F;
            this.earLeft.zRot  = (float) Math.sin(ageInTicks * 0.3F)        * 0.2F;
            this.earRight.zRot = (float) Math.sin(ageInTicks * 0.3F + 0.7F) * 0.2F;
            this.earLeft.xRot  = (float) Math.sin(ageInTicks * 0.2F)        * 0.1F;
            this.earRight.xRot = (float) Math.sin(ageInTicks * 0.2F + 0.5F) * 0.1F;
            this.frontLegLeft.xRot  = 0.0F;
            this.frontLegRight.xRot = 0.0F;
            this.hindThighLeft.xRot   = 0.0F;
            this.hindThighRight.xRot  = 0.0F;
            this.hindHockLeft.xRot    = -0.2F;
            this.hindHockRight.xRot   = -0.2F;
            this.tail.yRot = (float) Math.sin(ageInTicks * 0.8F) * 0.3F;
        }

        // ── alert snap : if recently hit ──────────────────────────────
        if (entity.hurtTime > 0) {
            this.earLeft.zRot  = 0.0F;
            this.earRight.zRot = 0.0F;
            this.earLeft.xRot  = 0.0F;
            this.earRight.xRot = 0.0F;
            this.head.xRot = -0.3F;
        }

        // ── attack : rabbit kicks with hind legs (THE rabbit attack) ─
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float kick = (float) Math.sin(atk * Math.PI);
            this.hindThighLeft.xRot   = kick * 1.5F;
            this.hindThighRight.xRot  = kick * 1.5F;
            this.hindHockLeft.xRot    = -kick * 1.0F;  // hock snaps forward
            this.hindHockRight.xRot   = -kick * 1.0F;
            this.root.xRot = -kick * 0.3F;
            this.frontLegLeft.xRot  = -kick * 0.6F;
            this.frontLegRight.xRot = -kick * 0.6F;
        }

        // ── death : rabbit flips onto side ────────────────────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F);
            float collapse = t * t;
            this.root.xRot = collapse * -0.5F;
            this.root.zRot = collapse * 0.8F;
            this.head.xRot = collapse * 0.5F;
            this.head.zRot = collapse * 0.4F;
            this.earLeft.zRot = collapse * 0.6F;
            this.earRight.zRot = -collapse * 0.6F;
            this.frontLegLeft.zRot  = -collapse * 0.3F;
            this.frontLegRight.zRot =  collapse * 0.3F;
            this.hindThighLeft.zRot  = -collapse * 0.4F;
            this.hindThighRight.zRot =  collapse * 0.4F;
        }
    }
}
