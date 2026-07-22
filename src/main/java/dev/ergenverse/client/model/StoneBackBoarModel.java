package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/stone_back_boar.png  SIZE: 64x64
/*
 * StoneBackBoarModel — stocky low-slung quadruped with a stone plate on its back.
 *
 * ANATOMY:
 *   - body        : thick torso (5 x 5 x 10) low to the ground
 *   - stone_plate : flat wide box (6 x 1 x 8) on top of body — gray-textured
 *                   region (separate texOffs) representing the mineral carapace
 *   - head        : skull (3x3x3) + snout (3x2x3 with a flat disc tip) +
 *                   2 curved tusks (each 2 angled boxes) + 2 small ears
 *   - legs        : 4 short thick legs (1.5 x 3 x 1.5), feet at y=15
 *   - tail        : short thin box at the rear
 *
 * ANIMATION:
 *   - Walk gait   : slow heavy trot — cos(swing*0.5)*0.6*swingAmt. Lower
 *                   frequency than wolf to feel heavy. Shins barely flex.
 *   - Charge      : when limbSwingAmount > 0.55, head lowers (xRot +0.8),
 *                   legs pump fast (freq x1.8, amp 1.1), body pitches forward
 *                   (root.xRot -0.1), tail straight back.
 *   - Idle root   : when stationary (swingAmt < 0.1), head dips to ground
 *                   (head.xRot +1.0), body bobs slowly as if rooting.
 *   - Head turn   : head.yRot = netHeadYaw * deg2rad (clamped).
 *
 * HARSH SELF-CRITIQUE:
 *   - Stone plate is a flat slab, not a sculpted mineral carapace. A real
 *     "stone back" should have cracked facets, raised ridges, mossy seams.
 *     Mine is a single 1-thick box that looks like a slice of bread on its back.
 *   - Tusks are 2 boxes each angled — real boar tusks are curved spirals that
 *     sharpen to a point and yellow with age. Mine are blunt sticks.
 *   - Snout is a box; real boar snouts are mobile cartilage discs used for
 *     digging. No disc flexibility, no rooting motion on the snout itself.
 *   - Body is one box — no bristly shoulder hump, no taper to the hindquarters.
 *     Boars have a distinctive silhouetted hump behind the head that is missing.
 *   - Legs are uniform thickness; real boar legs are thicker at the forearm
 *     and end in cloven hooves. No hooves modeled.
 *   - No bristles/mane along the spine — real boars have a raised bristle
 *     crest that puffs when angered. Stone plate replaces it here, which is
 *     canon-defensible but loses the boar silhouette.
 *   - Charge animation is driven by speed (limbSwingAmount) not by an actual
 *     charge AI state. A real charge should be triggered by a synced
 *     "charging" DataAccessor on the entity.
 *   - No snort / dust particles when charging — needs particle emission hooks
 *     in the renderer, not the model.
 *   - Texture UVs invented; existing stone_back_boar.png (vanilla PigModel
 *     layout) will scramble on this model.
 */
import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class StoneBackBoarModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart frontLeftThigh;
    private final ModelPart frontLeftShin;
    private final ModelPart frontRightThigh;
    private final ModelPart frontRightShin;
    private final ModelPart backLeftThigh;
    private final ModelPart backLeftShin;
    private final ModelPart backRightThigh;
    private final ModelPart backRightShin;

    public StoneBackBoarModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
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

        // ── body : thick low torso ───────────────────────────────────────
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.5F, -2.5F, -5.0F, 5.0F, 5.0F, 10.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));

        // ── stone plate : flat wide slab on the back, gray-textured region ──
        body.addOrReplaceChild("stone_plate",
                CubeListBuilder.create().texOffs(40, 0)
                        .addBox(-3.0F, -1.0F, -4.0F, 6.0F, 1.0F, 8.0F),
                PartPose.offset(0.0F, -2.5F, 0.0F));

        // ── head : skull + snout + tusks + ears, at the front ────────────
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-1.5F, -1.5F, -2.5F, 3.0F, 3.0F, 3.0F)   // skull
                        .texOffs(0, 24)
                        .addBox(-1.5F, 0.0F, -5.0F, 3.0F, 2.0F, 3.0F),   // snout forward
                PartPose.offset(0.0F, 6.0F, -5.0F));
        // snout disc (the flat cartilage tip)
        head.addOrReplaceChild("snout_disc",
                CubeListBuilder.create().texOffs(24, 16)
                        .addBox(-1.5F, 0.5F, -6.0F, 3.0F, 1.5F, 1.0F),
                PartPose.ZERO);
        // small ears
        head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(20, 16)
                        .addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-1.5F, -1.5F, -1.0F, 0.0F, 0.0F, -0.4F));
        head.addOrReplaceChild("ear_right",
                CubeListBuilder.create().texOffs(20, 20)
                        .addBox(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, -1.5F, -1.0F, 0.0F, 0.0F, 0.4F));

        // ── tusks : curved, 2 angled boxes each ──────────────────────────
        PartDefinition tuskL = head.addOrReplaceChild("tusk_left",
                CubeListBuilder.create().texOffs(36, 16)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.5F, 1.0F),
                PartPose.offsetAndRotation(-1.5F, 1.5F, -3.5F, 0.3F, 0.0F, -0.2F));
        tuskL.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition tuskR = head.addOrReplaceChild("tusk_right",
                CubeListBuilder.create().texOffs(36, 20)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.5F, 1.0F),
                PartPose.offsetAndRotation(1.5F, 1.5F, -3.5F, 0.3F, 0.0F, 0.2F));
        tuskR.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(40, 20)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -0.9F, 0.0F, 0.0F));

        // ── tail : short thin box at the rear ────────────────────────────
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(40, 24)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 5.0F, 0.4F, 0.0F, 0.0F));

        // ── legs : 4 short thick legs, feet at y=15 ──────────────────────
        root.addOrReplaceChild("front_left_thigh",
                CubeListBuilder.create().texOffs(0, 32).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(-2.0F, 12.0F, -3.0F));
        root.getChild("front_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 38).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("front_right_thigh",
                CubeListBuilder.create().texOffs(8, 32).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(2.0F, 12.0F, -3.0F));
        root.getChild("front_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(8, 38).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("back_left_thigh",
                CubeListBuilder.create().texOffs(0, 44).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(-2.0F, 12.0F, 3.0F));
        root.getChild("back_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 50).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("back_right_thigh",
                CubeListBuilder.create().texOffs(8, 44).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
                PartPose.offset(2.0F, 12.0F, 3.0F));
        root.getChild("back_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(8, 50).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F),
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
        boolean moving = limbSwingAmount > 0.1F;
        boolean charging = limbSwingAmount > 0.55F
                || entity.getSpiritPose() == SpiritBeastEntity.POSE_CHARGING;

        // ── head turn ────────────────────────────────────────────────────
        this.head.yRot = Math.max(-0.8F, Math.min(0.8F, netHeadYaw * 0.017453292F));

        // ── CRON-COMPLETIONIST-16: POSE_RESTING — boar lies down heavily ──
        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;
        // ── CRON-COMPLETIONIST-16: POSE_SWIMMING — boar swims (pigs can swim) ──
        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING;

        if (resting) {
            // Boar rests: heavy body lowers, thick legs fold, head on ground
            this.root.y = -2.5F;
            this.root.xRot = 0.05F;
            this.frontLeftThigh.xRot  = -0.5F;
            this.frontRightThigh.xRot = -0.5F;
            this.frontLeftShin.xRot   = 0.3F;
            this.frontRightShin.xRot  = 0.3F;
            this.backLeftThigh.xRot   = 0.2F;
            this.backRightThigh.xRot  = 0.2F;
            this.backLeftShin.xRot    = -0.15F;
            this.backRightShin.xRot   = -0.15F;
            this.head.xRot = 0.8F;
            this.tail.xRot = 0.3F;
            return;
        } else if (swimming) {
            // Boar swims: body buoyant, legs paddle, snout up
            this.root.xRot = -0.2F;
            this.root.y = -1.5F;
            this.head.xRot = -0.4F;
            float paddle = ageInTicks * 0.9F;
            this.frontLeftThigh.xRot  = (float) Math.cos(paddle) * 0.6F;
            this.frontRightThigh.xRot = (float) Math.cos(paddle + Math.PI) * 0.6F;
            this.backLeftThigh.xRot   = (float) Math.cos(paddle + Math.PI) * 0.4F;
            this.backRightThigh.xRot  = (float) Math.cos(paddle) * 0.4F;
            this.frontLeftShin.xRot   = -0.1F + Math.abs((float) Math.cos(paddle)) * 0.2F;
            this.frontRightShin.xRot  = -0.1F + Math.abs((float) Math.cos(paddle + Math.PI)) * 0.2F;
            this.backLeftShin.xRot    = -0.1F + Math.abs((float) Math.cos(paddle + Math.PI)) * 0.15F;
            this.backRightShin.xRot   = -0.1F + Math.abs((float) Math.cos(paddle)) * 0.15F;
            this.tail.xRot = 0.2F;
            return;
        }

        // ── walk / charge gait : slow & heavy by default ─────────────────
        float swingPhase = charging ? limbSwing * 1.8F : limbSwing;
        float freq = charging ? 0.8F : 0.5F;            // slower base frequency
        float amp = (charging ? 1.1F : 0.6F) * limbSwingAmount;
        float phase = swingPhase * freq;

        this.frontLeftThigh.xRot  = (float) Math.cos(phase)            * amp;
        this.frontRightThigh.xRot = (float) Math.cos(phase + Math.PI)  * amp;
        this.backLeftThigh.xRot   = (float) Math.cos(phase + Math.PI)  * amp;
        this.backRightThigh.xRot  = (float) Math.cos(phase)            * amp;
        // boars barely flex their shins — stiff-legged stomp
        this.frontLeftShin.xRot  = -0.1F + Math.max(0.0F, (float) Math.cos(phase))            * 0.2F * limbSwingAmount;
        this.frontRightShin.xRot = -0.1F + Math.max(0.0F, (float) Math.cos(phase + Math.PI))  * 0.2F * limbSwingAmount;
        this.backLeftShin.xRot   = -0.1F + Math.max(0.0F, (float) Math.cos(phase + Math.PI))  * 0.2F * limbSwingAmount;
        this.backRightShin.xRot  = -0.1F + Math.max(0.0F, (float) Math.cos(phase))            * 0.2F * limbSwingAmount;

        // ── body pitch + head behaviour ─────────────────────────────────
        if (charging) {
            // CHARGE : head lowers, body pitches forward, tail straight back
            this.root.xRot = -0.10F;
            this.head.xRot = 0.8F;
            this.tail.xRot = 0.0F;
        } else if (moving) {
            // WALK : head level, tail slight curl
            this.root.xRot = 0.0F;
            this.head.xRot = headPitch * 0.017453292F;
            this.tail.xRot = 0.4F;
        } else {
            // IDLE ROOT : head dips snout to ground, body slow bob
            this.root.xRot = 0.0F;
            this.root.y = (float) Math.sin(ageInTicks * 0.15F) * 0.15F;   // rooting bob
            this.head.xRot = 1.0F;
            this.tail.xRot = 0.4F + (float) Math.sin(ageInTicks * 0.4F) * 0.2F;
        }

        // ── attack lunge : boar head-butts, stone plate shifts ─────────
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float lunge = (float) Math.sin(atk * Math.PI);
            this.root.xRot -= lunge * 0.3F;              // body pitches forward
            this.head.xRot += lunge * 0.5F;               // head drives down further
            // heavy boar: front legs barely move, back legs dig
            this.frontLeftThigh.xRot  -= lunge * 0.2F;
            this.frontRightThigh.xRot -= lunge * 0.2F;
            this.backLeftThigh.xRot   += lunge * 0.3F;
            this.backRightThigh.xRot  += lunge * 0.3F;
        }

        // ── death collapse : heavy body slumps hard ───────────────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F); // 0→1 over 0.4s (visible before fade)
            float collapse = t * t;
            this.root.xRot = collapse * -0.3F;
            this.root.zRot = collapse * 0.4F;
            this.head.xRot = collapse * 0.7F;
            this.head.zRot = collapse * 0.2F;
            // thick legs buckle outward under weight
            this.frontLeftThigh.zRot  = -collapse * 0.4F;
            this.frontRightThigh.zRot =  collapse * 0.4F;
            this.backLeftThigh.zRot   = -collapse * 0.35F;
            this.backRightThigh.zRot  =  collapse * 0.35F;
            // shins splay (boar's short legs spread on death)
            this.frontLeftShin.zRot  = -collapse * 0.3F;
            this.frontRightShin.zRot =  collapse * 0.3F;
            this.backLeftShin.zRot   = -collapse * 0.25F;
            this.backRightShin.zRot  =  collapse * 0.25F;
            this.tail.xRot = 0.0F; // tail goes limp
        }
    }
}
