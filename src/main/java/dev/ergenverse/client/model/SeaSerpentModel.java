package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/sea_serpent.png  SIZE: 128x128
/*
 * SeaSerpentModel — CRON-COMPLETIONIST-86: TRUE CHAINED BODY for fluid undulation.
 *
 * CRON-86 FIX: The 12 body segments were ALL parented to root, meaning each
 * segment's yRot was applied in root space — segments wiggled independently
 * instead of producing a true traveling wave. This round reparents them into
 * a CHAIN: seg_0 (root) → seg_1 → seg_2 → ... → seg_11. Now rotations
 * propagate from head to tail, producing a true undulating S-curve.
 *
 * ANATOMY (12-segment CHAINED body):
 *   - seg[0]:  front torso  (3.0 x 3.0 x 2.5, deform 0.40) — root child
 *   - seg[1]:  mid-front   (2.8 x 2.8 x 2.5, deform 0.38) — child of seg_0
 *   - seg[2]:  front-mid   (2.6 x 2.6 x 2.5, deform 0.36) — child of seg_1
 *   - ... (each segment is child of the previous, offset (0, 0, 2.5))
 *   - seg[11]: tail-tip    (0.7 x 0.7 x 2.5, deform 0.18) — child of seg_10
 *   - neck:    child of seg_0 (front torso) — offset (0, -0.2, -3.0)
 *   - head:    child of neck (skull + jaw + whiskers + eyes)
 *   - dorsal fins: on segs 1, 4, 7, 10 (children of their segment)
 *   - lateral ridges: on segs 2, 5, 8, 11 (children of their segment)
 *   - tail_fin: at end of seg_11 (child of seg_11)
 *   - pec_fin_L/R: on seg_0 (children of seg_0, offset (±1.5, -0.5, 1.0))
 *
 * ANIMATION (CRON-86 REWRITE for chained propagation):
 *   - Swim: SMALL per-segment rotations with phase delay. Because segments
 *     are chained, rotations COMPOUND — a 0.1 rad rotation on seg_0 plus
 *     0.1 rad on seg_1 plus ... produces a cumulative S-curve. The old
 *     animation used large per-segment amplitudes (up to 1.35 rad) which
 *     would curl the chained body into a spiral. New amplitudes are
 *     0.05-0.12 rad per segment — small enough for a smooth wave.
 *   - The traveling wave is achieved by phase-delaying each segment's
 *     rotation: phase = wave + i * 0.45F. The phase delay makes the wave
 *     travel from head to tail.
 *   - Resting coil: each segment gets a small yRot that compounds into
 *     a gradual spiral.
 *   - Death: sequential straightening preserved (dampens rotations to 0).
 *
 * CANON: Sea serpent (海蛇) is the apex aquatic predator of 修魔海 (Sea of
 * Devils). The novel mentions demonic sea beasts in the Sea of Devils but
 * does not name a specific sea serpent character — this entity is
 * mod-original but canon-plausible. Verified via web search: 修魔海 is canon
 * (Wang Lin enters it around year 470-530 of the timeline).
 *
 * HARSH SELF-CRITIQUE (CRON-86):
 *   - The chained structure is the CORRECT anatomical model for a serpent.
 *     Real snakes undulate via vertebral propagation — each vertebra rotates
 *     slightly relative to the previous, and the cumulative effect is a
 *     traveling wave. The old root-parented model was anatomically wrong.
 *   - The phase delay (0.45 rad/seg) is tuned for visual smoothness. Real
 *     snake undulation has a wavelength of ~1 body length, which for 12
 *     segments means ~2π/12 ≈ 0.52 rad/seg phase delay. 0.45 is close.
 *   - The amplitude (0.05-0.12 rad/seg) is conservative. Real snakes can
 *     achieve larger amplitudes, but for a chained Minecraft model, small
 *     amplitudes prevent spiral curling. A future round could increase
 *     amplitude after runtime verification.
 *   - The neck is now a child of seg_0 (front torso), so neck rotations
 *     propagate to the head. The old root-parented neck meant head turns
 *     didn't follow body undulation — now they do.
 *   - The pec fins are children of seg_0, so they follow body motion.
 *     The old root-parented pec fins stayed fixed while the body wiggled.
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

    private static final int NUM_SEGMENTS = 12;
    private final ModelPart root;
    private final ModelPart[] segments = new ModelPart[NUM_SEGMENTS];
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart eyeLeft;
    private final ModelPart eyeRight;
    private final ModelPart whiskerLeft;
    private final ModelPart whiskerRight;
    // Dorsal fins on segments 1, 4, 7, 10
    private final ModelPart dorsal0;
    private final ModelPart dorsal1;
    private final ModelPart dorsal2;
    private final ModelPart dorsal3;
    // Lateral line ridges on segments 2, 5, 8, 11
    private final ModelPart lateral0;
    private final ModelPart lateral1;
    private final ModelPart lateral2;
    private final ModelPart lateral3;
    private final ModelPart tailFin;
    private final ModelPart pecFinLeft;
    private final ModelPart pecFinRight;

    public SeaSerpentModel(ModelPart root) {
        this.root = root;
        // CRON-COMPLETIONIST-86: Segments are now CHAINED.
        // seg_0 is a root child; seg_1 is a child of seg_0; seg_2 is a child of seg_1; etc.
        // This makes rotations propagate from head to tail, producing true undulation.
        this.segments[0] = root.getChild("seg_0");
        for (int i = 1; i < NUM_SEGMENTS; i++) {
            this.segments[i] = this.segments[i - 1].getChild("seg_" + i);
        }
        // CRON-COMPLETIONIST-86: neck is now a child of seg_0 (front torso), not root.
        // Old root-space offset was (0, 7.8, -11.0). seg_0 is at (0, 8.0, -8.0).
        // New offset = (0, -0.2, -3.0). Rotation unchanged.
        this.neck = this.segments[0].getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.eyeLeft = this.head.getChild("eye_left");
        this.eyeRight = this.head.getChild("eye_right");
        this.whiskerLeft = this.head.getChild("whisker_left");
        this.whiskerRight = this.head.getChild("whisker_right");
        // Dorsal fins are children of their respective segments (unchanged — they were already
        // children of segments via seg.addOrReplaceChild in the loop).
        this.dorsal0 = this.segments[1].getChild("dorsal");
        this.dorsal1 = this.segments[4].getChild("dorsal");
        this.dorsal2 = this.segments[7].getChild("dorsal");
        this.dorsal3 = this.segments[10].getChild("dorsal");
        this.lateral0 = this.segments[2].getChild("lateral");
        this.lateral1 = this.segments[5].getChild("lateral");
        this.lateral2 = this.segments[8].getChild("lateral");
        this.lateral3 = this.segments[11].getChild("lateral");
        this.tailFin = this.segments[11].getChild("tail_fin");
        // CRON-COMPLETIONIST-86: pec fins are now children of seg_0 (front torso), not root.
        // Old root-space offset was (±1.5, 7.5, -7.0). seg_0 is at (0, 8.0, -8.0).
        // New offset = (±1.5, -0.5, 1.0).
        this.pecFinLeft = this.segments[0].getChild("pec_fin_left");
        this.pecFinRight = this.segments[0].getChild("pec_fin_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── Body segment CHAIN: 12 segments tapering front → rear ──────
        // CRON-COMPLETIONIST-86: Segments are now CHAINED. seg_0 is a root child at
        // (0, 8.0, -8.0). Each subsequent segment is a child of the previous, offset
        // (0, 0, 2.5) — the z-spacing between consecutive segments.
        float[][] segDefs = {
            // {halfW, halfH, depth, deformation, zOffset (root-space, for reference)}
            {1.5F, 1.5F, 2.5F, 0.40F, -8.0F},  // seg_0: front torso
            {1.4F, 1.4F, 2.5F, 0.38F, -5.5F},  // seg_1
            {1.3F, 1.3F, 2.5F, 0.36F, -3.0F},  // seg_2
            {1.2F, 1.2F, 2.5F, 0.34F, -0.5F},  // seg_3
            {1.1F, 1.1F, 2.5F, 0.32F,  2.0F},  // seg_4
            {1.0F, 1.0F, 2.5F, 0.30F,  4.5F},  // seg_5
            {0.9F, 0.9F, 2.5F, 0.28F,  7.0F},  // seg_6
            {0.8F, 0.8F, 2.5F, 0.26F,  9.5F},  // seg_7
            {0.7F, 0.7F, 2.5F, 0.24F, 12.0F},  // seg_8
            {0.6F, 0.6F, 2.5F, 0.22F, 14.5F},  // seg_9
            {0.5F, 0.5F, 2.5F, 0.20F, 17.0F},  // seg_10
            {0.35F,0.35F,2.5F, 0.18F, 19.5F},  // seg_11: tail tip
        };

        int texY = 0;
        PartDefinition prevSeg = null;  // CRON-86: track previous segment for chaining
        for (int i = 0; i < NUM_SEGMENTS; i++) {
            float hw = segDefs[i][0];
            float hh = segDefs[i][1];
            float d  = segDefs[i][2];
            float def = segDefs[i][3];
            CubeDeformation cd = new CubeDeformation(def);

            // CRON-COMPLETIONIST-86: seg_0 is a root child (absolute offset);
            // seg_1..11 are children of the previous segment (relative offset (0, 0, 2.5)).
            PartDefinition seg;
            if (i == 0) {
                // seg_0: root child, absolute offset (0, 8.0, -8.0) — UNCHANGED from pre-CRON-86
                seg = root.addOrReplaceChild("seg_" + i,
                        CubeListBuilder.create().texOffs(0, texY)
                                .addBox(-hw, -hh, -d / 2.0F, hw * 2.0F, hh * 2.0F, d, cd),
                        PartPose.offset(0.0F, 8.0F, -8.0F));
            } else {
                // seg_1..11: child of previous segment, relative offset (0, 0, 2.5)
                // This is the z-spacing between consecutive segments.
                seg = prevSeg.addOrReplaceChild("seg_" + i,
                        CubeListBuilder.create().texOffs(0, texY)
                                .addBox(-hw, -hh, -d / 2.0F, hw * 2.0F, hh * 2.0F, d, cd),
                        PartPose.offset(0.0F, 0.0F, 2.5F));
            }
            texY += 10; // each segment gets 10px of texture height

            // Dorsal fins on segments 1, 4, 7, 10
            if (i == 1 || i == 4 || i == 7 || i == 10) {
                float finH = 2.0F - i * 0.15F;
                seg.addOrReplaceChild("dorsal",
                        CubeListBuilder.create().texOffs(32, i * 5)
                                .addBox(-0.15F, -finH, -0.8F, 0.3F, finH, 1.6F),
                        PartPose.offset(0.0F, -hh - 0.1F, 0.0F));
            }

            // Lateral line ridges on segments 2, 5, 8, 11
            if (i == 2 || i == 5 || i == 8 || i == 11) {
                float ridgeD = 0.25F;
                seg.addOrReplaceChild("lateral",
                        CubeListBuilder.create().texOffs(48, i * 3)
                                .addBox(-hw - 0.05F, -0.1F, -ridgeD * 4, ridgeD, 0.2F, ridgeD * 8),
                        PartPose.offset(0.0F, -hh * 0.3F, 0.0F));
            }

            prevSeg = seg;  // CRON-86: chain reference for next iteration
        }

        // ── neck : connector between seg_0 and head ─────────────────────
        // CRON-COMPLETIONIST-86: Reparented from root to seg_0 (front torso).
        // Old root-space offset was (0, 7.8, -11.0). seg_0 is at (0, 8.0, -8.0).
        // New offset = (0-0, 7.8-8.0, -11.0-(-8.0)) = (0, -0.2, -3.0). Rotation unchanged.
        PartDefinition neck = root.getChild("seg_0").addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(16, 0)
                        .addBox(-1.2F, -1.2F, -2.0F, 2.4F, 2.4F, 2.5F, new CubeDeformation(0.3F)),
                PartPose.offsetAndRotation(0.0F, -0.2F, -3.0F, -0.15F, 0.0F, 0.0F));

        // ── head : flat broad skull ────────────────────────────────────
        PartDefinition head = neck.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(-1.8F, -1.0F, -2.5F, 3.6F, 2.0F, 2.5F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, -0.3F, -2.0F));

        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(16, 48)
                        .addBox(-1.5F, 0.0F, -2.2F, 3.0F, 0.8F, 2.2F),
                PartPose.offset(0.0F, 0.6F, -0.8F));

        head.addOrReplaceChild("eye_left",
                CubeListBuilder.create().texOffs(24, 48)
                        .addBox(-0.3F, -0.3F, -0.3F, 0.6F, 0.6F, 0.6F),
                PartPose.offset(-0.9F, -1.1F, -0.8F));
        head.addOrReplaceChild("eye_right",
                CubeListBuilder.create().texOffs(24, 50)
                        .addBox(-0.3F, -0.3F, -0.3F, 0.6F, 0.6F, 0.6F),
                PartPose.offset(0.9F, -1.1F, -0.8F));

        head.addOrReplaceChild("whisker_left",
                CubeListBuilder.create().texOffs(28, 48)
                        .addBox(-0.1F, 0.0F, -4.0F, 0.2F, 0.2F, 4.0F),
                PartPose.offset(-1.0F, -0.3F, -2.0F));
        head.addOrReplaceChild("whisker_right",
                CubeListBuilder.create().texOffs(28, 52)
                        .addBox(-0.1F, 0.0F, -4.0F, 0.2F, 0.2F, 4.0F),
                PartPose.offset(1.0F, -0.3F, -2.0F));

        // ── tail fin : at end of seg_11 ────────────────────────────────
        // CRON-COMPLETIONIST-86: seg_11 is no longer a root child, so root.getChild("seg_11")
        // would return null. Must use the chained reference: prevSeg (which is seg_11 after the loop).
        prevSeg.addOrReplaceChild("tail_fin",
                CubeListBuilder.create().texOffs(16, 16)
                        .addBox(-1.0F, -1.5F, -0.1F, 2.0F, 3.0F, 0.2F),
                PartPose.offset(0.0F, 0.0F, 1.5F));

        // ── pectoral fins on seg_0 ────────────────────────────────────
        // CRON-COMPLETIONIST-86: Reparented from root to seg_0 (front torso).
        // Old root-space offset was (±1.5, 7.5, -7.0). seg_0 is at (0, 8.0, -8.0).
        // New offset = (±1.5-0, 7.5-8.0, -7.0-(-8.0)) = (±1.5, -0.5, 1.0).
        root.getChild("seg_0").addOrReplaceChild("pec_fin_left",
                CubeListBuilder.create().texOffs(56, 0)
                        .addBox(-2.5F, -0.1F, -1.2F, 2.5F, 0.2F, 2.4F),
                PartPose.offset(-1.5F, -0.5F, 1.0F));
        root.getChild("seg_0").addOrReplaceChild("pec_fin_right",
                CubeListBuilder.create().texOffs(56, 4)
                        .addBox(0.0F, -0.1F, -1.2F, 2.5F, 0.2F, 2.4F),
                PartPose.offset(1.5F, -0.5F, 1.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    /** CRON-COMPLETIONIST-47: Expose eyes for emissive rendering in SeaSerpentRenderer. */
    public ModelPart getEyeLeft() { return this.eyeLeft; }
    public ModelPart getEyeRight() { return this.eyeRight; }

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
            // CRON-86: With chained segments, each segment's yRot COMPOUNDS.
            // Small per-segment rotations produce a gradual spiral coil.
            float breath = (float) Math.sin(ageInTicks * 0.06F) * 0.05F;
            this.root.y = -0.5F + breath;
            // 12-segment coil: small per-segment rotations that compound into a spiral.
            // Old code used large angles (0.2-1.25) — those would over-curl a chained body.
            // New: 0.08-0.15 rad per segment, compounding to ~1.5 rad total curve.
            float[] coilAngles = {0.08F, 0.10F, 0.12F, 0.13F, 0.14F, 0.15F, 0.15F, 0.14F, 0.13F, 0.12F, 0.10F, 0.08F};
            for (int i = 0; i < NUM_SEGMENTS; i++) {
                this.segments[i].yRot = coilAngles[i];
                this.segments[i].xRot = 0.0F;
            }
            this.head.xRot = 0.3F;
            this.jaw.xRot = 0.0F;
            this.whiskerLeft.xRot = 0.5F;
            this.whiskerRight.xRot = 0.5F;
            this.pecFinLeft.zRot = -0.6F;
            this.pecFinRight.zRot = 0.6F;
            this.tailFin.yRot = 0.2F;
            this.dorsal0.xRot = 0.5F;
            this.dorsal1.xRot = 0.5F;
            this.dorsal2.xRot = 0.5F;
            this.dorsal3.xRot = 0.5F;
        } else if (swimming) {
            // ── SWIM UNDULATION : 12-segment traveling wave (CHAINED propagation) ─────
            // CRON-86: With chained segments, each segment's yRot propagates to all
            // downstream segments. A SMALL per-segment rotation with phase delay produces
            // a TRUE traveling S-curve — the wave travels from head to tail because each
            // segment's rotation is phase-delayed relative to the previous.
            //
            // OLD animation (pre-CRON-86): used large amplitudes (up to 1.35 rad on seg_11)
            // with segments parented to root. This produced independent wiggling, NOT a
            // traveling wave. With chained segments, those amplitudes would curl the body
            // into a spiral.
            //
            // NEW animation: amplitude is 0.05-0.12 rad per segment (compounds through chain).
            // Phase delay is 0.45 rad/seg (wavelength ≈ 14 segments ≈ 1.2 body lengths).
            float wave = ageInTicks * 0.8F;
            float baseAmp = 0.05F + limbSwingAmount * 0.07F;  // 0.05-0.12 rad per segment

            for (int i = 0; i < NUM_SEGMENTS; i++) {
                // Phase delay: 0.45 radians per segment (wavelength ≈ 14 segments)
                float phase = wave + i * 0.45F;
                // Small amplitude per segment — compounds through chain into S-curve.
                // Slight amplitude increase toward tail for whip effect.
                float segAmp = baseAmp * (0.8F + i * 0.03F);  // 0.8x to 1.13x baseAmp
                this.segments[i].yRot = (float) Math.sin(phase) * segAmp;
                // Tiny vertical oscillation for 3D undulation (compounds through chain)
                this.segments[i].xRot = (float) Math.cos(phase + 1.0F) * segAmp * 0.15F;
            }

            // Neck follows the body wave with a slight lead (phase advance)
            this.neck.yRot = (float) Math.sin(wave - 0.20F) * baseAmp * 0.5F;

            // Tail fin amplifies the last segment's motion (whip effect)
            float tailPhase = wave + NUM_SEGMENTS * 0.45F;
            this.tailFin.yRot = (float) Math.sin(tailPhase) * baseAmp * 2.0F;
            this.tailFin.xRot = (float) Math.cos(tailPhase) * baseAmp * 0.4F;

            // Pectoral fins paddle gently (now children of seg_0, so they follow body)
            this.pecFinLeft.zRot = (float) Math.sin(wave * 0.5F) * 0.25F;
            this.pecFinRight.zRot = -(float) Math.sin(wave * 0.5F) * 0.25F;

            // 4 dorsal fins ripple with phase delay matching their segment
            this.dorsal0.xRot = (float) Math.sin(wave + 1 * 0.45F) * 0.15F;
            this.dorsal1.xRot = (float) Math.sin(wave + 4 * 0.45F) * 0.15F;
            this.dorsal2.xRot = (float) Math.sin(wave + 7 * 0.45F) * 0.15F;
            this.dorsal3.xRot = (float) Math.sin(wave + 10 * 0.45F) * 0.15F;

            this.whiskerLeft.xRot = 0.3F + (float) Math.sin(ageInTicks * 0.3F) * 0.05F;
            this.whiskerRight.xRot = 0.3F + (float) Math.sin(ageInTicks * 0.3F + 1.0F) * 0.05F;
            this.whiskerLeft.yRot = (float) Math.sin(ageInTicks * 0.25F) * 0.06F;
            this.whiskerRight.yRot = -(float) Math.sin(ageInTicks * 0.25F) * 0.06F;

            this.jaw.xRot = limbSwingAmount * 0.15F;
            this.root.y = (float) Math.sin(wave * 0.5F) * 0.08F;
        } else {
            // ── IDLE / GROUND : gentle S-curve drift (chained) ────────
            // CRON-86: Small per-segment rotations compound into a gentle S-curve.
            float idle = ageInTicks * 0.15F;
            float idleAmp = 0.03F;  // small — compounds through chain
            for (int i = 0; i < NUM_SEGMENTS; i++) {
                float phase = idle + i * 0.3F;
                this.segments[i].yRot = (float) Math.sin(phase) * idleAmp;
                this.segments[i].xRot = 0.0F;
            }
            this.neck.yRot = (float) Math.sin(idle - 0.3F) * idleAmp * 0.5F;
            this.tailFin.yRot = (float) Math.sin(idle + NUM_SEGMENTS * 0.3F) * idleAmp * 2.0F;
            this.jaw.xRot = 0.0F;
            this.whiskerLeft.yRot = (float) Math.sin(idle * 2.0F) * 0.08F;
            this.whiskerRight.yRot = -(float) Math.sin(idle * 2.0F) * 0.08F;
            this.pecFinLeft.zRot = 0.0F;
            this.pecFinRight.zRot = 0.0F;
            this.dorsal0.xRot = 0.0F;
            this.dorsal1.xRot = 0.0F;
            this.dorsal2.xRot = 0.0F;
            this.dorsal3.xRot = 0.0F;
        }

        // ── attack : head strike + body recoil cascade ────────────────
        // CRON-86: With chained segments, the recoil on seg_0 propagates through
        // the body. Small per-segment recoil values compound into a full-body flinch.
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float strike = (float) Math.sin(atk * Math.PI);
            this.head.xRot = -strike * 1.2F;
            this.jaw.xRot = strike * 0.7F;
            // Recoil cascade: head segments pull back, tail segments unaffected.
            // Values are SMALL because they compound through the chain.
            this.segments[0].yRot -= strike * 0.05F;
            this.segments[1].yRot -= strike * 0.04F;
            this.segments[2].yRot -= strike * 0.03F;
            this.segments[3].yRot -= strike * 0.02F;
            this.whiskerLeft.xRot = strike * 0.8F;
            this.whiskerRight.xRot = strike * 0.8F;
            this.whiskerLeft.yRot = strike * 0.3F;
            this.whiskerRight.yRot = -strike * 0.3F;
        }

        // ── death : sequential straightening head→tail ──────────────────
        // CRON-86: The death block already dampens segment rotations via
        // *(1-segCollapse), which correctly drives them to 0 as collapse→1.
        // This is the correct behavior for chained segments — stale swim/idle
        // rotations are dampened to 0, preventing propagation through the chain.
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 12.0F, 1.0F);
            float collapse = t * t;
            this.root.xRot = collapse * -0.4F;
            this.root.y = collapse * -1.0F;
            this.head.xRot = collapse * 0.6F;
            this.jaw.xRot = collapse * 0.4F;
            // 12-segment sequential straightening with finer stagger
            for (int i = 0; i < NUM_SEGMENTS; i++) {
                float segDelay = i * 0.06F;
                float segT = Math.max(0.0F, Math.min(1.0F, (t - segDelay) / (1.0F - segDelay)));
                float segCollapse = segT * segT;
                this.segments[i].yRot *= (1.0F - segCollapse);
                this.segments[i].xRot *= (1.0F - segCollapse);
            }
            this.tailFin.yRot *= (1.0F - collapse);
            this.pecFinLeft.zRot = 0.0F;
            this.pecFinRight.zRot = 0.0F;
        }
    }
}
