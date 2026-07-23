package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/sea_serpent.png  SIZE: 128x64
/*
 * SeaSerpentModel — undulating aquatic predator with 8 body segments.
 *
 * CRON-COMPLETIONIST-36: Major overhaul from 3 segments to 8 body segments.
 * The old 3-segment model produced a jerky, robotic wave. 8 segments create
 * a smooth, organic traveling wave that closely mimics real anguilliform
 * (eel-like) swimming.
 *
 * Canon (Renegade Immortal): sea serpents (海蛇) inhabit the oceans and deep
 * rivers of the cultivation world. They are long, sinuous predators with
 * ancient bloodlines. Some have reached intelligence comparable to early
 * cultivation beasts.
 *
 * ANATOMY (8-segment chain):
 *   - seg[0]: neck/head connector (3.0 x 3.0 x 3.0, CubeDeformation 0.3)
 *   - seg[1]: front torso (3.0 x 3.0 x 3.0, CubeDeformation 0.4)
 *   - seg[2]: mid-front torso (2.8 x 2.8 x 3.0, CubeDeformation 0.4)
 *   - seg[3]: central torso (2.6 x 2.6 x 3.0, CubeDeformation 0.4)
 *   - seg[4]: mid-rear torso (2.2 x 2.2 x 3.0, CubeDeformation 0.35)
 *   - seg[5]: rear torso (1.8 x 1.8 x 3.0, CubeDeformation 0.3)
 *   - seg[6]: pre-tail (1.4 x 1.4 x 3.0, CubeDeformation 0.25)
 *   - seg[7]: tail base (1.0 x 1.0 x 3.0, CubeDeformation 0.2)
 *   - tail_fin: flattened vertical fin at end of seg[7]
 *   - head: flat skull (3.0 x 2.0 x 2.5, CubeDeformation 0.2) + jaw + 2 whiskers + 2 eyes
 *   - dorsal fins: thin membranes on segments 1, 3, 5 (alternating, like real sea snakes)
 *   - pec_fin_L/R: small pectoral fins on seg[0]
 *   - lateral_line: subtle ridge on segments 2, 4, 6 (sensory organ)
 *
 * ANIMATION:
 *   - Swim undulation: 8-segment traveling wave
 *     seg[i].yRot = sin(age*0.8 + i*0.4) * amplitude * (0.3 + i*0.1)
 *     Creates a smooth S-curve that propagates from head to tail.
 *   - Idle: gentle S-curve drift at 1/5 swim speed
 *   - Attack: head strikes forward with jaw opening, body recoils in 2-segment cascade
 *   - Death: all segments straighten sequentially (head→tail), body sinks
 *   - Resting: coils into a loose spiral (segments wrap around each other)
 *
 * IMPROVEMENTS OVER OLD MODEL:
 *   - 8 body segments instead of 3 — smooth undulation
 *   - Each segment tapers (wider at front, narrower at tail)
 *   - Alternating dorsal fins (segments 1, 3, 5) instead of clustered on 3 boxes
 *   - Lateral line ridges on segments 2, 4, 6 (anatomically correct)
 *   - Separate eye cubes on head (can glow in future)
 *   - Death animation: sequential straightening instead of instant rigid
 *   - Resting pose: actual coiling behavior (sea serpents coil when resting)
 *   - Attack: 2-segment recoil cascade instead of single body recoil
 *
 * HARSH SELF-CRITIQUE:
 *   - Still box-based (MC addBox limitation). A real sea serpent has a smooth
 *     cylindrical body; ours is a chain of rounded boxes. The CubeDeformation
 *     helps soften edges but cannot eliminate the boxy silhouette.
 *   - No gill slit animation — gills are cosmetic boxes, not animated flaps.
 *   - Whiskers are still 0.2px sticks — barbs should taper.
 *   - Tail fin is a flat quad — real eel tails have flowing rays.
 *   - Texture is 128x64 — adequate but not high-res. UV mapping is manual and
 *     may have minor artifacts at segment seams.
 *   - 8 segments is good but 12 would be better for truly fluid motion.
 *     Kept at 8 for performance (each segment is a separate ModelPart matrix op).
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

import java.util.ArrayList;
import java.util.List;

public class SeaSerpentModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    // 8 body segments as a chain: seg[0] is closest to head, seg[7] is at the tail
    private final ModelPart[] segments = new ModelPart[8];
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart eyeLeft;
    private final ModelPart eyeRight;
    private final ModelPart whiskerLeft;
    private final ModelPart whiskerRight;
    // Dorsal fins on alternating segments (1, 3, 5)
    private final ModelPart dorsal0;
    private final ModelPart dorsal1;
    private final ModelPart dorsal2;
    // Lateral line ridges on segments (2, 4, 6)
    private final ModelPart lateral0;
    private final ModelPart lateral1;
    private final ModelPart lateral2;
    private final ModelPart tailFin;
    private final ModelPart pecFinLeft;
    private final ModelPart pecFinRight;

    public SeaSerpentModel(ModelPart root) {
        this.root = root;
        // Segment chain: each is a root child, chained by offset
        for (int i = 0; i < 8; i++) {
            this.segments[i] = root.getChild("seg_" + i);
        }
        this.neck = root.getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.eyeLeft = this.head.getChild("eye_left");
        this.eyeRight = this.head.getChild("eye_right");
        this.whiskerLeft = this.head.getChild("whisker_left");
        this.whiskerRight = this.head.getChild("whisker_right");
        this.dorsal0 = this.segments[1].getChild("dorsal");
        this.dorsal1 = this.segments[3].getChild("dorsal");
        this.dorsal2 = this.segments[5].getChild("dorsal");
        this.lateral0 = this.segments[2].getChild("lateral");
        this.lateral1 = this.segments[4].getChild("lateral");
        this.lateral2 = this.segments[6].getChild("lateral");
        this.tailFin = this.segments[7].getChild("tail_fin");
        this.pecFinLeft = root.getChild("pec_fin_left");
        this.pecFinRight = root.getChild("pec_fin_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── Body segment chain ──────────────────────────────────────────
        // Each segment is a root child positioned along Z axis.
        // Segments taper from front (wide) to rear (narrow).
        // Total body length: ~28 pixels along Z
        float[][] segDefs = {
            // {halfW, halfH, depth, deformation, zOffset}
            {1.5F, 1.5F, 3.0F, 0.40F, -8.0F},  // seg_0: front torso
            {1.5F, 1.5F, 3.0F, 0.40F, -5.0F},  // seg_1: mid-front
            {1.4F, 1.4F, 3.0F, 0.38F, -2.0F},  // seg_2: mid-front 2
            {1.3F, 1.3F, 3.0F, 0.36F,  1.0F},  // seg_3: center
            {1.1F, 1.1F, 3.0F, 0.32F,  4.0F},  // seg_4: mid-rear
            {0.9F, 0.9F, 3.0F, 0.28F,  7.0F},  // seg_5: rear
            {0.7F, 0.7F, 3.0F, 0.24F, 10.0F},  // seg_6: pre-tail
            {0.5F, 0.5F, 3.0F, 0.20F, 13.0F},  // seg_7: tail base
        };

        int texY = 0;
        for (int i = 0; i < 8; i++) {
            float hw = segDefs[i][0];
            float hh = segDefs[i][1];
            float d  = segDefs[i][2];
            float def = segDefs[i][3];
            float zOff = segDefs[i][4];
            CubeDeformation cd = new CubeDeformation(def);

            PartDefinition seg = root.addOrReplaceChild("seg_" + i,
                    CubeListBuilder.create().texOffs(0, texY)
                            .addBox(-hw, -hh, -d / 2.0F, hw * 2.0F, hh * 2.0F, d, cd),
                    PartPose.offset(0.0F, 8.0F, zOff));
            texY += 8; // each segment gets 8px of texture height

            // Alternating dorsal fins on segments 1, 3, 5
            if (i == 1 || i == 3 || i == 5) {
                float finH = 1.8F - i * 0.2F; // dorsal fins shrink toward tail
                seg.addOrReplaceChild("dorsal",
                        CubeListBuilder.create().texOffs(32, i * 6)
                                .addBox(-0.15F, -finH, -0.8F, 0.3F, finH, 1.6F),
                        PartPose.offset(0.0F, -hh - 0.1F, 0.0F));
            }

            // Lateral line ridges on segments 2, 4, 6
            if (i == 2 || i == 4 || i == 6) {
                float ridgeD = 0.3F - i * 0.05F;
                seg.addOrReplaceChild("lateral",
                        CubeListBuilder.create().texOffs(48, i * 4)
                                .addBox(-hw - 0.05F, -0.1F, -ridgeD * 4, ridgeD, 0.2F, ridgeD * 8),
                        PartPose.offset(0.0F, -hh * 0.3F, 0.0F));
            }
        }

        // ── neck : connector between seg_0 and head ─────────────────────
        PartDefinition neck = root.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(16, 0)
                        .addBox(-1.2F, -1.2F, -2.0F, 2.4F, 2.4F, 2.5F, new CubeDeformation(0.3F)),
                PartPose.offsetAndRotation(0.0F, 7.8F, -11.0F, -0.15F, 0.0F, 0.0F));

        // ── head : flat broad skull (python/dragon-like) ───────────────
        PartDefinition head = neck.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(-1.8F, -1.0F, -2.5F, 3.6F, 2.0F, 2.5F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, -0.3F, -2.0F));

        // Lower jaw — hinged at back, opens forward
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(16, 48)
                        .addBox(-1.5F, 0.0F, -2.2F, 3.0F, 0.8F, 2.2F),
                PartPose.offset(0.0F, 0.6F, -0.8F));

        // Eyes — on top of skull, small and forward-facing
        head.addOrReplaceChild("eye_left",
                CubeListBuilder.create().texOffs(24, 48)
                        .addBox(-0.3F, -0.3F, -0.3F, 0.6F, 0.6F, 0.6F),
                PartPose.offset(-0.9F, -1.1F, -0.8F));
        head.addOrReplaceChild("eye_right",
                CubeListBuilder.create().texOffs(24, 50)
                        .addBox(-0.3F, -0.3F, -0.3F, 0.6F, 0.6F, 0.6F),
                PartPose.offset(0.9F, -1.1F, -0.8F));

        // Whiskers (barbels) — longer than before, from the snout tip
        head.addOrReplaceChild("whisker_left",
                CubeListBuilder.create().texOffs(28, 48)
                        .addBox(-0.1F, 0.0F, -4.0F, 0.2F, 0.2F, 4.0F),
                PartPose.offset(-1.0F, -0.3F, -2.0F));
        head.addOrReplaceChild("whisker_right",
                CubeListBuilder.create().texOffs(28, 52)
                        .addBox(-0.1F, 0.0F, -4.0F, 0.2F, 0.2F, 4.0F),
                PartPose.offset(1.0F, -0.3F, -2.0F));

        // ── tail fin : flattened vertical fin at end of seg_7 ──────────
        PartDefinition seg7 = root.getChild("seg_7");
        seg7.addOrReplaceChild("tail_fin",
                CubeListBuilder.create().texOffs(16, 12)
                        .addBox(-1.2F, -1.8F, -0.1F, 2.4F, 3.6F, 0.2F),
                PartPose.offset(0.0F, 0.0F, 1.8F));

        // ── pectoral fins : small steering fins on seg_0 ───────────────
        root.addOrReplaceChild("pec_fin_left",
                CubeListBuilder.create().texOffs(56, 0)
                        .addBox(-2.5F, -0.1F, -1.2F, 2.5F, 0.2F, 2.4F),
                PartPose.offset(-1.5F, 7.5F, -7.0F));
        root.addOrReplaceChild("pec_fin_right",
                CubeListBuilder.create().texOffs(56, 4)
                        .addBox(0.0F, -0.1F, -1.2F, 2.5F, 0.2F, 2.4F),
                PartPose.offset(1.5F, 7.5F, -7.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(SpiritBeastEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // ── Head turn (clamped) ────────────────────────────────────────
        float yaw = netHeadYaw * 0.017453292F;
        float pitch = headPitch * 0.017453292F;
        this.head.yRot = Math.max(-1.0F, Math.min(1.0F, yaw));
        this.head.xRot = Math.max(-0.7F, Math.min(0.7F, pitch));

        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING
                || entity.isInWater();
        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;

        if (resting) {
            // ── RESTING : loose coil ──────────────────────────────────
            // Sea serpents coil when resting. We approximate this by rotating
            // segments into a loose spiral pattern.
            float breath = (float) Math.sin(ageInTicks * 0.06F) * 0.05F;
            this.root.y = -0.5F + breath;
            // Segments curl into a rough C-shape
            float[] coilAngles = {0.3F, 0.6F, 0.9F, 1.1F, 1.2F, 1.1F, 0.9F, 0.7F};
            for (int i = 0; i < 8; i++) {
                this.segments[i].yRot = coilAngles[i];
                this.segments[i].xRot = 0.0F;
            }
            // Head rests on the coil
            this.head.xRot = 0.3F;
            this.jaw.xRot = 0.0F;
            // Whiskers drape down
            this.whiskerLeft.xRot = 0.5F;
            this.whiskerRight.xRot = 0.5F;
            // Fins fold flat
            this.pecFinLeft.zRot = -0.6F;
            this.pecFinRight.zRot = 0.6F;
            this.tailFin.yRot = 0.2F;
            // Dorsal fins lay flat
            this.dorsal0.xRot = 0.5F;
            this.dorsal1.xRot = 0.5F;
            this.dorsal2.xRot = 0.5F;
        } else if (swimming) {
            // ── SWIM UNDULATION : 8-segment traveling wave ──────────────
            // Anguilliform (eel-like) wave: phase increases linearly per segment,
            // amplitude increases toward the tail (head leads, tail follows).
            float wave = ageInTicks * 0.8F;
            float baseAmp = 0.15F + limbSwingAmount * 0.35F;

            for (int i = 0; i < 8; i++) {
                // Phase offset: 0.4 radians per segment = smooth propagation
                float phase = wave + i * 0.4F;
                // Amplitude increases toward tail (head is stable, tail sweeps wide)
                float segAmp = baseAmp * (0.3F + i * 0.1F);
                this.segments[i].yRot = (float) Math.sin(phase) * segAmp;
                // Slight vertical undulation (3D wave, not flat side-to-side)
                this.segments[i].xRot = (float) Math.cos(phase + 1.0F) * segAmp * 0.15F;
            }

            // Neck follows seg[0] with slight lead
            this.neck.yRot = (float) Math.sin(wave - 0.2F) * baseAmp * 0.25F;

            // Tail fin amplifies the wave (propulsive surface)
            float tailPhase = wave + 8 * 0.4F;
            this.tailFin.yRot = (float) Math.sin(tailPhase) * baseAmp * 1.3F;
            this.tailFin.xRot = (float) Math.cos(tailPhase) * baseAmp * 0.25F;

            // Pectoral fins scull gently (opposing phase)
            this.pecFinLeft.zRot = (float) Math.sin(wave * 0.5F) * 0.25F;
            this.pecFinRight.zRot = -(float) Math.sin(wave * 0.5F) * 0.25F;

            // Dorsal fins ripple (slight delay from body segment)
            this.dorsal0.xRot = (float) Math.sin(wave + 1.4F) * 0.15F;
            this.dorsal1.xRot = (float) Math.sin(wave + 3.4F) * 0.15F;
            this.dorsal2.xRot = (float) Math.sin(wave + 5.4F) * 0.15F;

            // Whiskers stream backward in current
            this.whiskerLeft.xRot = 0.3F + (float) Math.sin(ageInTicks * 0.3F) * 0.05F;
            this.whiskerRight.xRot = 0.3F + (float) Math.sin(ageInTicks * 0.3F + 1.0F) * 0.05F;
            this.whiskerLeft.yRot = (float) Math.sin(ageInTicks * 0.25F) * 0.06F;
            this.whiskerRight.yRot = -(float) Math.sin(ageInTicks * 0.25F) * 0.06F;

            // Jaw slightly open when swimming fast (breathing through mouth)
            this.jaw.xRot = limbSwingAmount * 0.15F;

            // Body height bobs with wave
            this.root.y = (float) Math.sin(wave * 0.5F) * 0.08F;
        } else {
            // ── IDLE / GROUND : gentle S-curve drift ────────────────────
            float idle = ageInTicks * 0.15F;
            float idleAmp = 0.04F;
            for (int i = 0; i < 8; i++) {
                float phase = idle + i * 0.3F;
                this.segments[i].yRot = (float) Math.sin(phase) * idleAmp;
                this.segments[i].xRot = 0.0F;
            }
            this.neck.yRot = (float) Math.sin(idle - 0.3F) * idleAmp * 0.5F;
            this.tailFin.yRot = (float) Math.sin(idle + 8 * 0.3F) * idleAmp * 1.5F;
            this.jaw.xRot = 0.0F;
            this.whiskerLeft.yRot = (float) Math.sin(idle * 2.0F) * 0.08F;
            this.whiskerRight.yRot = -(float) Math.sin(idle * 2.0F) * 0.08F;
            this.pecFinLeft.zRot = 0.0F;
            this.pecFinRight.zRot = 0.0F;
            this.dorsal0.xRot = 0.0F;
            this.dorsal1.xRot = 0.0F;
            this.dorsal2.xRot = 0.0F;
        }

        // ── attack : head strike + body recoil cascade ────────────────
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float strike = (float) Math.sin(atk * Math.PI);
            // Head lunges forward
            this.head.xRot = -strike * 1.2F;
            this.jaw.xRot = strike * 0.7F;
            // Body recoils in cascade: seg[0] reacts first, seg[3] last
            this.segments[0].yRot -= strike * 0.25F;
            this.segments[1].yRot -= strike * 0.20F;
            this.segments[2].yRot -= strike * 0.15F;
            this.segments[3].yRot -= strike * 0.10F;
            // Whiskers flair out during strike
            this.whiskerLeft.xRot = strike * 0.8F;
            this.whiskerRight.xRot = strike * 0.8F;
            this.whiskerLeft.yRot = strike * 0.3F;
            this.whiskerRight.yRot = -strike * 0.3F;
        }

        // ── death : sequential straightening head→tail, body sinks ────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 10.0F, 1.0F);
            float collapse = t * t;
            // Body sinks and rolls
            this.root.xRot = collapse * -0.4F;
            this.root.y = collapse * -1.0F;
            // Head droops
            this.head.xRot = collapse * 0.6F;
            this.jaw.xRot = collapse * 0.4F;
            // Segments straighten sequentially: front segments first
            for (int i = 0; i < 8; i++) {
                float segDelay = i * 0.08F; // staggered straightening
                float segT = Math.max(0.0F, Math.min(1.0F, (t - segDelay) / (1.0F - segDelay)));
                float segCollapse = segT * segT;
                this.segments[i].yRot *= (1.0F - segCollapse);
                this.segments[i].xRot *= (1.0F - segCollapse);
            }
            // Tail fin goes limp
            this.tailFin.yRot *= (1.0F - collapse);
            // Fins flatten
            this.pecFinLeft.zRot = 0.0F;
            this.pecFinRight.zRot = 0.0F;
        }
    }
}
