package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/soul_fish.png  SIZE: 64x32
/*
 * SoulFishModel — small qi-infused fish found in spiritual waters.
 *
 * Canon (Renegade Immortal): soul fish (魂鱼) inhabit spirit springs, sacred
 * pools, and qi-rich waterways. They are bioluminescent, radiating faint
 * spiritual light. They are a common ingredient in alchemy and a sign of
 * a location's spiritual energy.
 *
 * REAL FISH anatomy (carp/koi-like):
 *   - Body: streamlined teardrop (wider at front, tapers to tail)
 *   - Head: rounded skull with mouth at front, eyes on sides
 *   - Fins: dorsal (top), anal (bottom rear), pectoral (sides), caudal (tail fan)
 *   - Tail: fan-shaped caudal fin for propulsion
 *   - Scales: overlapping, often iridescent
 *   - Lateral line: visible sensory line along the flanks
 *
 * ANATOMY:
 *   - body : main torso (2.5 x 2.0 x 4.0, CubeDeformation 0.3 for streamlined shape)
 *   - head : rounded skull (2.0 x 1.5 x 1.5) extending from body front
 *     - eye_left, eye_right : small spheres on sides of head
 *     - mouth : small cube at the front of the snout
 *   - dorsal_fin : single tall fin on top of body
 *   - anal_fin   : small fin on bottom rear
 *   - pec_fin_L  : pectoral fin, left side
 *   - pec_fin_R  : pectoral fin, right side
 *   - tail_root  : narrow connector behind body
 *   - tail_fan   : wide fan-shaped caudal fin (3 slabs)
 *   - qi_glow    : transparent aura box around body (rendered as overlay)
 *   - lateral_line: subtle ridge along the body flank
 *
 * ANIMATION:
 *   - Swim: tail root oscillates rapidly (sin(age*2.5)*0.6), tail fan follows.
 *     Body pitches slightly with each tail beat. Pectoral fins scull.
 *   - Idle: gentle tail sway, occasional body roll, dorsal fin wobbles
 *   - Schooling: fish tend to face the same direction (handled by AI, not model)
 *   - Death: body goes belly-up (root.zRot = PI), fins spread, sinks
 *
 * SPECIES VARIANTS (via texture swap, not model change):
 *   - Common soul fish (pale blue glow)
 *   - Spirit koi (golden glow)
 *   - Blood fish (red glow, more aggressive)
 *
 * HARSH SELF-CRITIQUE:
 *   - Body is a single rounded box — real fish have a distinct taper from
 *     gills to tail. This looks like a fat sausage.
 *   - Dorsal fin is a flat slab — real dorsal fins are curved membranes.
 *   - Tail fan is 3 flat boxes — real caudal fins are forked or fan-shaped
 *     with individual rays. These look like cardboard cutouts.
 *   - No mouth animation — real fish open/close their mouths constantly
 *     for respiration. This one's mouth is static.
 *   - Qi glow box is visible in-game as a transparent overlay — cheap effect.
 *     A proper implementation would use a custom RenderType with additive blending.
 *   - Scales are texture-only — no geometric scale pattern.
 *   - At this scale (0.3 blocks), fine details are invisible anyway.
 *     The model prioritizes silhouette readability over detail.
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

public class SoulFishModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart eyeLeft;
    private final ModelPart eyeRight;
    private final ModelPart mouth;
    private final ModelPart dorsalFin;
    private final ModelPart analFin;
    private final ModelPart pecFinLeft;
    private final ModelPart pecFinRight;
    private final ModelPart tailRoot;
    private final ModelPart tailFan;
    private final ModelPart qiGlow;
    private final ModelPart lateralLine;

    public SoulFishModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.eyeLeft = this.head.getChild("eye_left");
        this.eyeRight = this.head.getChild("eye_right");
        this.mouth = this.head.getChild("mouth");
        this.dorsalFin = this.body.getChild("dorsal_fin");
        this.analFin = this.body.getChild("anal_fin");
        this.pecFinLeft = root.getChild("pec_fin_left");
        this.pecFinRight = root.getChild("pec_fin_right");
        this.tailRoot = root.getChild("tail_root");
        this.tailFan = this.tailRoot.getChild("tail_fan");
        this.qiGlow = root.getChild("qi_glow");
        this.lateralLine = this.body.getChild("lateral_line");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        CubeDeformation bodyStreamlined = new CubeDeformation(0.3F);

        // ── body : streamlined teardrop torso ───────────────────────────
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.5F, -1.0F, -2.0F, 3.0F, 2.0F, 4.0F, bodyStreamlined),
                PartPose.offset(0.0F, 6.0F, 0.0F));

        // ── head : rounded, wider than body at the front ──────────────
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(12, 0)
                        .addBox(-1.0F, -0.8F, -1.5F, 2.0F, 1.6F, 1.5F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 0.0F, -2.5F));
        // Eyes on sides of head
        head.addOrReplaceChild("eye_left",
                CubeListBuilder.create().texOffs(20, 0)
                        .addBox(-0.2F, -0.2F, -0.2F, 0.4F, 0.4F, 0.4F),
                PartPose.offset(-0.9F, -0.4F, -0.3F));
        head.addOrReplaceChild("eye_right",
                CubeListBuilder.create().texOffs(20, 2)
                        .addBox(-0.2F, -0.2F, -0.2F, 0.4F, 0.4F, 0.4F),
                PartPose.offset(0.9F, -0.4F, -0.3F));
        // Mouth at front
        head.addOrReplaceChild("mouth",
                CubeListBuilder.create().texOffs(22, 0)
                        .addBox(-0.3F, -0.15F, -0.2F, 0.6F, 0.3F, 0.2F),
                PartPose.offset(0.0F, 0.3F, -1.3F));

        // ── dorsal fin : tall thin membrane on top ────────────────────
        body.addOrReplaceChild("dorsal_fin",
                CubeListBuilder.create().texOffs(0, 8)
                        .addBox(-0.1F, -1.2F, -0.8F, 0.2F, 1.4F, 1.6F),
                PartPose.offset(0.0F, -1.0F, -0.5F));

        // ── anal fin : small fin on bottom rear ──────────────────────
        body.addOrReplaceChild("anal_fin",
                CubeListBuilder.create().texOffs(0, 12)
                        .addBox(-0.1F, 0.0F, -0.5F, 0.2F, 0.6F, 1.0F),
                PartPose.offset(0.0F, 1.0F, 1.0F));

        // ── lateral line : subtle ridge along the flank ────────────────
        body.addOrReplaceChild("lateral_line",
                CubeListBuilder.create().texOffs(24, 0)
                        .addBox(-1.6F, -0.1F, -1.8F, 0.15F, 0.2F, 3.6F),
                PartPose.offset(0.0F, 0.2F, 0.0F));

        // ── pectoral fins : small side fins ───────────────────────────
        root.addOrReplaceChild("pec_fin_left",
                CubeListBuilder.create().texOffs(4, 8)
                        .addBox(-1.5F, -0.05F, -0.8F, 1.5F, 0.1F, 1.6F),
                PartPose.offset(-1.5F, 6.5F, -1.0F));
        root.addOrReplaceChild("pec_fin_right",
                CubeListBuilder.create().texOffs(4, 10)
                        .addBox(0.0F, -0.05F, -0.8F, 1.5F, 0.1F, 1.6F),
                PartPose.offset(1.5F, 6.5F, -1.0F));

        // ── tail root : narrow connector behind body ─────────────────
        PartDefinition tailRoot = root.addOrReplaceChild("tail_root",
                CubeListBuilder.create().texOffs(12, 4)
                        .addBox(-0.4F, -0.4F, 0.0F, 0.8F, 0.8F, 2.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 6.0F, 2.5F));

        // ── tail fan : wide caudal fin (3 slabs) ──────────────────────
        tailRoot.addOrReplaceChild("tail_fan",
                CubeListBuilder.create().texOffs(0, 14)
                        .addBox(-1.5F, -1.2F, -0.05F, 3.0F, 0.8F, 0.1F)   // top lobe
                        .texOffs(0, 16)
                        .addBox(-1.5F, 0.4F, -0.05F, 3.0F, 0.8F, 0.1F),   // bottom lobe
                PartPose.offset(0.0F, 0.0F, 2.0F));

        // ── qi glow : transparent aura around body ────────────────────
        // Rendered as a slightly larger transparent box. The renderer
        // should handle the actual glow effect via RenderType.
        root.addOrReplaceChild("qi_glow",
                CubeListBuilder.create().texOffs(0, 20)
                        .addBox(-2.0F, -1.5F, -3.5F, 4.0F, 3.0F, 7.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 6.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(SpiritBeastEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // ── Head turn ──────────────────────────────────────────────────
        this.head.yRot = netHeadYaw * 0.017453292F * 0.5F;
        this.head.xRot = headPitch * 0.017453292F * 0.3F;

        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING
                || entity.isInWater();
        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;

        if (resting) {
            // ── RESTING : fish hovers in place ──────────────────────────
            float hover = (float) Math.sin(ageInTicks * 0.05F) * 0.1F;
            this.root.y = hover;
            // Fins spread slightly for stability
            this.pecFinLeft.zRot = -0.15F;
            this.pecFinRight.zRot = 0.15F;
            // Tail still with occasional twitch
            float twitch = (ageInTicks % 80 < 3) ? (float) Math.sin(ageInTicks * 3.0F) * 0.15F : 0.0F;
            this.tailRoot.yRot = twitch;
            this.tailFan.yRot = twitch * 0.5F;
            // Dorsal fin idle
            this.dorsalFin.zRot = (float) Math.sin(ageInTicks * 0.08F) * 0.03F;
            // Qi glow pulses slowly
            this.qiGlow.yScale = 1.0F + (float) Math.sin(ageInTicks * 0.1F) * 0.05F;
        } else if (swimming) {
            // ── SWIM : tail-driven oscillation ────────────────────────
            // Fish swim by oscillating the tail root; the body follows passively.
            float tailFreq = 2.5F + limbSwingAmount * 1.5F;
            float tailPhase = ageInTicks * tailFreq;
            float tailAmp = 0.4F + limbSwingAmount * 0.3F;

            // Tail root oscillates (source of propulsion)
            this.tailRoot.yRot = (float) Math.sin(tailPhase) * tailAmp;
            // Tail fan follows with slight phase delay (creates S-shape at tail)
            this.tailFan.yRot = (float) Math.sin(tailPhase + 0.3F) * tailAmp * 0.8F;

            // Body pitches with each tail beat (reaction)
            this.body.xRot = (float) Math.sin(tailPhase + Math.PI) * tailAmp * 0.08F;

            // Pectoral fins scull (opposing phase for stability)
            this.pecFinLeft.zRot = (float) Math.sin(tailPhase * 0.4F) * 0.2F;
            this.pecFinRight.zRot = -(float) Math.sin(tailPhase * 0.4F) * 0.2F;

            // Dorsal fin stabilizes (slight flex)
            this.dorsalFin.zRot = (float) Math.sin(tailPhase * 0.3F) * 0.05F;

            // Anal fin flexes
            this.analFin.zRot = (float) Math.sin(tailPhase + 0.5F) * 0.08F;

            // Qi glow pulses faster when swimming
            this.qiGlow.yScale = 1.0F + (float) Math.sin(ageInTicks * 0.2F) * 0.08F;

            // Mouth opens slightly during fast swimming
            this.mouth.xRot = limbSwingAmount * 0.1F;
        } else {
            // ── IDLE : gentle drift ────────────────────────────────────
            float idle = ageInTicks * 0.1F;
            this.tailRoot.yRot = (float) Math.sin(idle * 0.8F) * 0.1F;
            this.tailFan.yRot = (float) Math.sin(idle * 0.8F + 0.2F) * 0.08F;
            this.pecFinLeft.zRot = 0.0F;
            this.pecFinRight.zRot = 0.0F;
            this.dorsalFin.zRot = 0.0F;
            this.qiGlow.yScale = 1.0F;
            this.mouth.xRot = 0.0F;
        }

        // ── death : belly-up, fins spread, sinks ──────────────────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F);
            float collapse = t * t;
            // Fish goes belly-up (classic fish death)
            this.root.zRot = collapse * (float) Math.PI;
            // Body sinks
            this.root.y = -collapse * 2.0F;
            // Fins spread wide
            this.pecFinLeft.zRot = -collapse * 0.8F;
            this.pecFinRight.zRot = collapse * 0.8F;
            this.dorsalFin.zRot = 0.0F;
            // Tail goes limp
            this.tailRoot.yRot *= (1.0F - collapse);
            // Qi glow fades (shrink to zero)
            this.qiGlow.yScale = 1.0F - collapse;
            // Mouth opens (gaping death)
            this.mouth.xRot = collapse * 0.3F;
        }
    }
}
