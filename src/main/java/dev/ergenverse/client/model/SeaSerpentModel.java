package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/sea_serpent.png  SIZE: 64x64
/*
 * SeaSerpentModel — undulating aquatic predator with fin and whiskers.
 *
 * Canon (Renegade Immortal): sea serpents (海蛇) inhabit the oceans and deep
 * rivers of the cultivation world. They are long, sinuous predators with
 * ancient bloodlines.
 *
 * REAL SEA SERPENT anatomy:
 *   - Body: very long, cylindrical, tapering toward the tail
 *   - Head: flat, broad with hinged jaws (like a python), small eyes on top
 *   - Whiskers: barbels on the snout (like a catfish or dragon)
 *   - Fins: dorsal fin ridge along the spine, small pectoral fins
 *   - Tail: flattened vertically (like an eel) for undulating propulsion
 *   - Scales: overlapping, iridescent
 *
 * ANATOMY (boxy approximation):
 *   - body_mid  : central torso (3 x 3 x 4)
 *   - body_front: front torso (2.5 x 2.5 x 3)
 *   - body_rear : rear torso (2 x 2 x 3)
 *   - tail_base : connector to tail
 *   - tail_fin  : flattened vertical fin (2 x 3 x 0.3)
 *   - head      : flat skull (3 x 2 x 2) + lower jaw (separate) + 2 whiskers
 *   - dorsal    : 3 fin segments along the spine ridge
 *   - pec_fin_L/R : small pectoral fins
 *
 * ANIMATION:
 *   - Swim undulation: body segments offset by phase = sin(age*0.8 + segmentIdx*0.5)*amplitude
 *     creating a traveling wave down the body. Tail fin leads the wave.
 *   - Idle: gentle S-curve drift, whiskers sway
 *   - Attack: head strikes forward, body recoils
 *   - Death: body goes rigid, sinks (collapse flat)
 *
 * HARSH SELF-CRITIQUE:
 *   - Body is 3 connected boxes — a real sea serpent should have 8-10 segments
 *     for smooth undulation. 3 segments produce a jerky wave.
 *   - Dorsal fin is flat boxes — real dorsal fins are thin membranes.
 *   - Whiskers are 1px sticks — real barbels have length and taper.
 *   - No pectoral fin animation — they should scull during turning.
 *   - Head is a box — should be flat and broad like a python.
 *   - Tail fin is flat and thin — adequate for an eel-like swimmer.
 *   - Missing: lateral line (sensory organ), gill slits, eye placement.
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

public class SeaSerpentModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart bodyFront;
    private final ModelPart bodyMid;
    private final ModelPart bodyRear;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart whiskerLeft;
    private final ModelPart whiskerRight;
    private final ModelPart dorsal0;
    private final ModelPart dorsal1;
    private final ModelPart dorsal2;
    private final ModelPart tailBase;
    private final ModelPart tailFin;
    private final ModelPart pecFinLeft;
    private final ModelPart pecFinRight;

    public SeaSerpentModel(ModelPart root) {
        this.root = root;
        this.bodyFront = root.getChild("body_front");
        this.bodyMid = root.getChild("body_mid");
        this.bodyRear = root.getChild("body_rear");
        this.neck = root.getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.whiskerLeft = this.head.getChild("whisker_left");
        this.whiskerRight = this.head.getChild("whisker_right");
        this.dorsal0 = this.bodyFront.getChild("dorsal_0");
        this.dorsal1 = this.bodyMid.getChild("dorsal_1");
        this.dorsal2 = this.bodyRear.getChild("dorsal_2");
        this.tailBase = root.getChild("tail_base");
        this.tailFin = this.tailBase.getChild("tail_fin");
        this.pecFinLeft = root.getChild("pec_fin_left");
        this.pecFinRight = root.getChild("pec_fin_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        CubeDeformation bodyRound = new CubeDeformation(0.4F);

        // ── body_front : front torso ─────────────────────────────────────
        PartDefinition bodyFront = root.addOrReplaceChild("body_front",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 4.0F, bodyRound),
                PartPose.offset(0.0F, 8.0F, -5.0F));
        bodyFront.addOrReplaceChild("dorsal_0",
                CubeListBuilder.create().texOffs(24, 0)
                        .addBox(-0.2F, -2.0F, -1.0F, 0.4F, 1.5F, 2.0F),
                PartPose.offset(0.0F, -1.5F, 0.0F));

        // ── body_mid : central torso ─────────────────────────────────────
        PartDefinition bodyMid = root.addOrReplaceChild("body_mid",
                CubeListBuilder.create().texOffs(0, 8)
                        .addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 4.0F, bodyRound),
                PartPose.offset(0.0F, 8.0F, -1.0F));
        bodyMid.addOrReplaceChild("dorsal_1",
                CubeListBuilder.create().texOffs(24, 4)
                        .addBox(-0.2F, -2.0F, -0.8F, 0.4F, 1.5F, 1.6F),
                PartPose.offset(0.0F, -1.5F, 0.0F));

        // ── body_rear : rear torso ──────────────────────────────────────
        PartDefinition bodyRear = root.addOrReplaceChild("body_rear",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 3.0F, bodyRound),
                PartPose.offset(0.0F, 8.0F, 3.0F));
        bodyRear.addOrReplaceChild("dorsal_2",
                CubeListBuilder.create().texOffs(24, 8)
                        .addBox(-0.2F, -1.5F, -0.6F, 0.4F, 1.2F, 1.2F),
                PartPose.offset(0.0F, -1.0F, 0.0F));

        // ── neck : connector to head ────────────────────────────────────
        PartDefinition neck = root.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(12, 0)
                        .addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.3F)),
                PartPose.offsetAndRotation(0.0F, 7.5F, -8.0F, -0.2F, 0.0F, 0.0F));

        // ── head : flat skull + jaw + whiskers ──────────────────────────
        PartDefinition head = neck.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 24)
                        .addBox(-1.5F, -1.0F, -2.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, -0.5F, -2.0F));
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(0, 30)
                        .addBox(-1.2F, 0.0F, -2.0F, 2.4F, 0.8F, 2.0F),
                PartPose.offset(0.0F, 0.5F, -0.5F));
        // Whiskers (barbels) — long thin sticks from the snout
        head.addOrReplaceChild("whisker_left",
                CubeListBuilder.create().texOffs(28, 0)
                        .addBox(-0.1F, 0.0F, -3.0F, 0.2F, 0.2F, 3.0F),
                PartPose.offset(-0.8F, -0.2F, -1.5F));
        head.addOrReplaceChild("whisker_right",
                CubeListBuilder.create().texOffs(28, 4)
                        .addBox(-0.1F, 0.0F, -3.0F, 0.2F, 0.2F, 3.0F),
                PartPose.offset(0.8F, -0.2F, -1.5F));

        // ── tail : base + flattened fin ────────────────────────────────
        PartDefinition tailBase = root.addOrReplaceChild("tail_base",
                CubeListBuilder.create().texOffs(12, 6)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.3F)),
                PartPose.offsetAndRotation(0.0F, 8.0F, 5.0F, 0.2F, 0.0F, 0.0F));
        tailBase.addOrReplaceChild("tail_fin",
                CubeListBuilder.create().texOffs(20, 0)
                        .addBox(-1.0F, -1.5F, -0.1F, 2.0F, 3.0F, 0.2F),
                PartPose.offset(0.0F, 0.0F, 3.0F));

        // ── pectoral fins : small steering fins ─────────────────────────
        root.addOrReplaceChild("pec_fin_left",
                CubeListBuilder.create().texOffs(24, 12)
                        .addBox(-2.0F, -0.1F, -1.0F, 2.0F, 0.2F, 2.0F),
                PartPose.offset(-1.5F, 7.5F, -4.0F));
        root.addOrReplaceChild("pec_fin_right",
                CubeListBuilder.create().texOffs(24, 16)
                        .addBox(0.0F, -0.1F, -1.0F, 2.0F, 0.2F, 2.0F),
                PartPose.offset(1.5F, 7.5F, -4.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(SpiritBeastEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float yaw = netHeadYaw * 0.017453292F;
        float pitch = headPitch * 0.017453292F;
        this.head.yRot = Math.max(-1.0F, Math.min(1.0F, yaw));
        this.head.xRot = Math.max(-0.7F, Math.min(0.7F, pitch));

        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING
                || entity.isInWater();

        if (swimming) {
            // ── SWIM UNDULATION : traveling wave down the body ──────────
            float wave = ageInTicks * 0.8F;
            float amp = 0.3F + limbSwingAmount * 0.4F;

            // Each body segment offset by phase for traveling wave
            this.neck.yRot = (float) Math.sin(wave) * amp * 0.5F;
            this.bodyFront.yRot = (float) Math.sin(wave + 0.5F) * amp * 0.4F;
            this.bodyMid.yRot = (float) Math.sin(wave + 1.0F) * amp * 0.6F;
            this.bodyRear.yRot = (float) Math.sin(wave + 1.5F) * amp * 0.8F;
            this.tailBase.yRot = (float) Math.sin(wave + 2.0F) * amp;
            this.tailFin.yRot = (float) Math.sin(wave + 2.5F) * amp * 1.2F;

            // Tail fin also pitches for thrust
            this.tailFin.xRot = (float) Math.cos(wave + 2.5F) * amp * 0.3F;

            // Pectoral fins scull gently
            this.pecFinLeft.zRot = (float) Math.sin(wave * 0.5F) * 0.2F;
            this.pecFinRight.zRot = -(float) Math.sin(wave * 0.5F) * 0.2F;

            // Whiskers stream backward
            this.whiskerLeft.xRot = 0.3F;
            this.whiskerRight.xRot = 0.3F;
            this.whiskerLeft.yRot = (float) Math.sin(ageInTicks * 0.3F) * 0.05F;
            this.whiskerRight.yRot = -(float) Math.sin(ageInTicks * 0.3F) * 0.05F;

            // Jaw slightly open when swimming fast
            this.jaw.xRot = limbSwingAmount * 0.15F;
        } else {
            // ── LAND / IDLE : gentle S-curve drift ──────────────────────
            float idle = ageInTicks * 0.15F;
            this.bodyFront.yRot = (float) Math.sin(idle) * 0.05F;
            this.bodyMid.yRot = (float) Math.sin(idle + 0.3F) * 0.06F;
            this.bodyRear.yRot = (float) Math.sin(idle + 0.6F) * 0.05F;
            this.tailBase.yRot = (float) Math.sin(idle + 0.9F) * 0.08F;
            this.tailFin.yRot = (float) Math.sin(idle + 1.2F) * 0.1F;
            this.jaw.xRot = 0.0F;

            // Whiskers sway gently
            this.whiskerLeft.yRot = (float) Math.sin(idle * 2.0F) * 0.08F;
            this.whiskerRight.yRot = -(float) Math.sin(idle * 2.0F) * 0.08F;

            // Fins flat
            this.pecFinLeft.zRot = 0.0F;
            this.pecFinRight.zRot = 0.0F;
        }

        // ── attack : head strike forward ──────────────────────────────
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float strike = (float) Math.sin(atk * Math.PI);
            this.head.xRot = -strike * 1.0F;
            this.jaw.xRot = strike * 0.6F;
            // Body recoils
            this.bodyFront.yRot -= strike * 0.2F;
            this.bodyMid.yRot -= strike * 0.15F;
        }

        // ── death : body goes rigid, sinks ───────────────────────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F);
            float collapse = t * t;
            this.root.xRot = collapse * -0.3F;
            this.head.xRot = collapse * 0.5F;
            this.jaw.xRot = collapse * 0.4F;
            this.tailFin.yRot = collapse * 0.3F;
            // Body straightens out
            this.bodyFront.yRot *= (1.0F - collapse);
            this.bodyMid.yRot *= (1.0F - collapse);
            this.bodyRear.yRot *= (1.0F - collapse);
            this.tailBase.yRot *= (1.0F - collapse);
        }
    }
}
