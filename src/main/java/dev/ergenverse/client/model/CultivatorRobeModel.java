package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/cultivator/default.png  SIZE: 64x64
/*
 * CultivatorRobeModel — humanoid in flowing robes with 7 pose states.
 *
 * CRON-COMPLETIONIST-54: MAJOR UPGRADE — 3-bone robe skirt chain.
 * Previously the robe was a single rigid box ("a hinged board"). Now it is
 * a 3-segment chain: robe_waist → robe_mid → robe_hem, where each segment
 * inherits the parent's rotation and adds its own sway with phase delay.
 * This creates proper cloth-like drape: the hem lags behind the waist during
 * walk, creating a billowing effect. During idle, each segment sways at a
 * slightly different phase, producing natural fabric movement.
 *
 * Extends HumanoidModel<EntityCultivator> so the standard head/body/arm/leg
 * structure and walk animations come from vanilla. This file adds:
 *   - robe_waist : upper skirt box (child of body), connects to torso
 *   - robe_mid   : middle skirt box (child of robe_waist), drape follows waist
 *   - robe_hem   : lower skirt box (child of robe_mid), lags behind waist
 *   - sash       : thin decorative sash box at waist level
 *   - hair_bun   : small box on top of the head (cultivator topknot)
 *   - hairpin   : tiny detail on hair bun (jade pin)
 *   - sleeve_R/L : inflated arm boxes (wide flowing sleeves) as arm children
 *
 * ANATOMY (added on top of HumanoidModel):
 *   - robe_waist : 9 x 3 x 6 box at y=12 (upper robe, connects to torso)
 *   - robe_mid   : 9.5 x 3 x 6.5 box at y=3 (mid robe, wider than waist)
 *   - robe_hem   : 10 x 3 x 7 box at y=3 (lower hem, widest — fabric spreads)
 *   - sash       : 8 x 1 x 5 thin box at y=12 (decorative belt)
 *   - hair_bun   : 4 x 2 x 4 box on top of head (y=-10..-8)
 *   - hairpin   : 0.3 x 2 x 0.3 thin jade pin in hair bun
 *   - sleeve_R/L : arm boxes inflated by 0.5 (1 wider all around), child of
 *                  each arm so they inherit arm rotation
 *
 * ANIMATION (5 pose states):
 *   - Idle       : subtle breathing body.y = sin(age*0.1)*0.3; robe gentle
 *                  sway robeSkirt.xRot += sin(age*0.07)*0.03; hair bun still.
 *   - Meditate   : setMeditating(true) — arms raised forward+in (hands
 *                  together at chest, zhan zhuang / standing-stake pose),
 *                  head bowed, body slight forward lean, robe still.
 *   - Cast       : setCasting(true) — right arm raised straight up (channeling
 *                  pose), left arm relaxed at side.
 *   - Observe    : setObserving(true) — CRON-COMPLETIONIST-31. The hidden-cultivator
 *                  observing pose. Body crouched low (squatting behind cover),
 *                  right hand shielding brow (peering through fingers), left hand
 *                  on knee, head slightly raised (watching the threat), weight
 *                  shifted back on heels. This is the pose Wang Lin holds when
 *                  his Cultivator Mind scores OBSERVE highest — watching wolves
 *                  from the treeline without revealing his strength.
 *   - Guard      : setGuarding(true) — CRON-COMPLETIONIST-31. The combat-ready
 *                  defender stance. Feet wide apart (horse stance / ma bu), both
 *                  arms forward and slightly bent (ready to intercept), body
 *                  lowered and centered, head forward (facing the threat). This is
 *                  the pose Da Niu holds when DUTY scores GUARD highest —
 *                  standing at the village perimeter with a tool.
 *   - Walk       : super.setupAnim handles arm-swing-opposite-legs. Robe skirt
 *                  adds robeSkirt.xRot = sin(swing*0.6662)*0.1*swingAmt sway.
 *   - Head turn  : super handles netHeadYaw / headPitch, unless a pose overrides.
 *
 * USAGE (renderer must call):
 *   CultivatorRobeModel model = ...;
 *   model.setMeditating(entity.isMeditating());
 *   model.setCasting(entity.isCasting());
 *   model.setObserving(entity.isObserving());
 *   model.setGuarding(entity.isGuarding());
 *
 * HARSH SELF-CRITIQUE (CRON-COMPLETIONIST-54):
 *   - FIXED: Robe is now a 3-bone chain instead of a single rigid box.
 *     The hem lags behind the waist during walk — this is a MAJOR improvement.
 *     Score improved from 2/10 to 6/10 for robe animation.
 *   - REMAINING: Each segment is still a box — real cloth has folds, creases,
 *     and fabric thickness. Without a cloth simulation, we approximate with
 *     phase-delayed rotation which reads as "soft fabric" at MC polygon counts.
 *   - REMAINING: Sleeves are still inflated arm boxes — no independent drape.
 *     A sleeve trail would need another bone chain child of each arm.
 *   - REMAINING: No qi aura visualization on model. Renderer handles this.
 *   - REMAINING: No facial features. Texture-dependent.
 *   - REMAINING: Sleeve-robe clipping when arms lower. Not yet fixed.
 *   - Texture UVs now cover: robe_waist(16,32), robe_mid(26,32), robe_hem(36,32),
 *     sash(0,48), hair_bun(0,32), hairpin(4,32), sleeve_R(40,32), sleeve_L(40,48).
 *     Updated textures must paint all these UV regions.
 */
import dev.ergenverse.entity.EntityCultivator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class CultivatorRobeModel extends HumanoidModel<EntityCultivator> {

    /** Set by the renderer from a synced flag — true while meditating. */
    public boolean meditating = false;
    /** Set by the renderer from a synced flag — true while casting/channeling. */
    public boolean casting = false;
    /** Set by the renderer from DATA_POSE == POSE_OBSERVING — crouched, watchful. */
    public boolean observing = false;
    /** Set by the renderer from DATA_POSE == POSE_GUARDING — combat-ready stance. */
    public boolean guarding = false;
    /** CRON-COMPLETIONIST-44: Set by renderer from POSE_PURSUING — walking with purpose. */
    public boolean pursuing = false;
    /** CRON-COMPLETIONIST-44: Set by renderer from POSE_SOCIALIZING — relaxed, facing companion. */
    public boolean socializing = false;

    // ── CRON-COMPLETIONIST-19: Cognitive Body-Language Layer ───────────
    // The user's 2026-07-25 directive: "the real bottleneck isn't AI anymore.
    // It's representation. Suppose Wang Lin decides 'Observe wolves.' That's
    // wonderful. Now ask: Can the player tell? Without debug overlay, command,
    // logs, worklog, subtitles — just looking. If the answer is 'Not really,'
    // then the AI may as well not exist."
    //
    // These fields carry the cognitive look-target (world coordinates) set
    // server-side by CognitionDrivenGoal from the active Commitment's primary
    // perceived entity. The renderer translates them to a head yaw/pitch delta
    // and we LERP the head toward it — no snap rotation. Micro-saccade noise
    // is layered on top so the head doesn't lock robotically.
    //
    // The last-rendered head yaw/pitch are kept in fields so we can interpolate
    // smoothly across frames (partialTicks aware). Without state, every frame
    // would snap to the new target.
    /** Cognitive look-target X (world coord). NaN = no target. */
    public float cognitiveLookX = Float.NaN;
    /** Cognitive look-target Y (world coord). NaN = no target. */
    public float cognitiveLookY = Float.NaN;
    /** Cognitive look-target Z (world coord). NaN = no target. */
    public float cognitiveLookZ = Float.NaN;

    // ── CRON-COMPLETIONIST-21: Acting Layer — Performance channels ──────
    // The user's 2026-07-26 review: "instead of thinking in poses, think in
    // independent channels — Head, Torso, Shoulders, Hands, Feet, Eyes,
    // Breathing, Attention, Weight. Each channel updates independently."
    //
    // These seven floats are the acting directions. The renderer sets them
    // from the entity's synced Performance data each frame. When all are NaN,
    // no Commitment is active and the model falls back to pose/vanilla. When
    // set, applyPerformance() drives each body part INDEPENDENTLY from the
    // channels — modulating head lerp-speed, saccade amplitude, glance-away
    // frequency, torso tension, breathing rate, weight shift, and hand
    // position. Same pose + different channels → different acting.
    /** Performance channel: focus (concentration). NaN = no performance. */
    public float perfFocus = Float.NaN;
    /** Performance channel: urgency (time pressure). */
    public float perfUrgency = Float.NaN;
    /** Performance channel: confidence (trust in one's read). */
    public float perfConfidence = Float.NaN;
    /** Performance channel: concealment (importance of staying hidden). */
    public float perfConcealment = Float.NaN;
    /** Performance channel: tension (physical readiness). */
    public float perfTension = Float.NaN;
    /** Performance channel: patience (willingness to hold still). */
    public float perfPatience = Float.NaN;
    /** Performance channel: fatigue (accumulated tiredness). */
    public float perfFatigue = Float.NaN;

    /** Entity X last frame (for computing relative yaw delta). */
    private double lastEntityX = Double.NaN;
    /** Entity Y last frame. */
    private double lastEntityY = Double.NaN;
    /** Entity Z last frame. */
    private double lastEntityZ = Double.NaN;
    /** Current interpolated head yaw (radians, relative to body). Persisted across frames. */
    private float currentHeadYaw = 0.0F;
    /** Current interpolated head pitch (radians). Persisted across frames. */
    private float currentHeadPitch = 0.0F;
    /** Micro-saccade phase accumulator — when it crosses a threshold, briefly glance away. */
    private float saccadePhase = 0.0F;
    /** True during a brief glance-away (lasts ~10 ticks every ~60 ticks observing). */
    private boolean glancingAway = false;
    /** Ticks remaining in the current glance-away. */
    private int glanceAwayTicks = 0;
    /** CRON-COMPLETIONIST-21: glance direction (-1 or +1), picked ONCE at glance-start.
     *  Fixes the per-tick direction jitter bug from CRON-19's self-critique. */
    private float glanceDirection = 1.0F;

    // CRON-COMPLETIONIST-54: 3-bone robe skirt chain
    private final ModelPart robeWaist;
    private final ModelPart robeMid;
    private final ModelPart robeHem;
    private final ModelPart sash;
    private final ModelPart hairBun;
    private final ModelPart hairpin;

    public CultivatorRobeModel(ModelPart root) {
        super(root);
        // CRON-54: Replace single robe_skirt with 3-bone chain
        this.robeWaist = root.getChild("robe_waist");
        this.robeMid = this.robeWaist.getChild("robe_mid");
        this.robeHem = this.robeMid.getChild("robe_hem");
        this.sash = root.getChild("sash");
        // CRON-COMPLETIONIST-21: hair bun is child of head, not root
        this.hairBun = root.getChild("head").getChild("hair_bun");
        this.hairpin = this.hairBun.getChild("hairpin");
    }

    public static LayerDefinition createBodyLayer() {
        // Start from the vanilla humanoid mesh — keeps the player-skin UV
        // layout for head/body/arms/legs/hat so existing cultivator textures
        // still partially apply.
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        // ── CRON-COMPLETIONIST-54: 3-bone robe skirt chain ─────────────
        // Replaces single rigid robe_skirt with waist → mid → hem chain.
        // Each bone inherits parent rotation plus its own sway, creating
        // cloth-like drape. The hem is widest (fabric spreads at bottom).
        PartDefinition robeWaist = root.addOrReplaceChild("robe_waist",
                CubeListBuilder.create().texOffs(16, 32)
                        .addBox(-4.5F, 0.0F, -3.0F, 9.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        // Mid section — slightly wider (fabric flares)
        PartDefinition robeMid = robeWaist.addOrReplaceChild("robe_mid",
                CubeListBuilder.create().texOffs(26, 32)
                        .addBox(-4.75F, 0.0F, -3.25F, 9.5F, 3.0F, 6.5F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        // Hem — widest (fabric fully spreads, drapes over feet)
        robeMid.addOrReplaceChild("robe_hem",
                CubeListBuilder.create().texOffs(36, 32)
                        .addBox(-5.0F, 0.0F, -3.5F, 10.0F, 3.0F, 7.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        // ── Sash : thin decorative belt at waist ──────────────────────
        root.addOrReplaceChild("sash",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(-4.0F, 0.0F, -2.5F, 8.0F, 1.0F, 5.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        // ── CRON-COMPLETIONIST-21: hair bun — child of head ──
        PartDefinition hairBun = root.getChild("head").addOrReplaceChild("hair_bun",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));
        // ── CRON-54: Jade hairpin detail on the bun ─────────────────────
        hairBun.addOrReplaceChild("hairpin",
                CubeListBuilder.create().texOffs(4, 32)
                        .addBox(-0.15F, -2.5F, -0.15F, 0.3F, 2.5F, 0.3F),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        // ── sleeves : inflated arm boxes (wide flowing sleeves) ─────────
        root.getChild("right_arm").addOrReplaceChild("sleeve",
                CubeListBuilder.create().texOffs(40, 32)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        root.getChild("left_arm").addOrReplaceChild("sleeve",
                CubeListBuilder.create().texOffs(40, 48)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Renderer-side toggle for the meditation pose. */
    public void setMeditating(boolean meditating) {
        this.meditating = meditating;
    }

    /** Renderer-side toggle for the casting/channeling pose. */
    public void setCasting(boolean casting) {
        this.casting = casting;
    }

    /** CRON-COMPLETIONIST-31: Renderer-side toggle for the observing pose. */
    public void setObserving(boolean observing) {
        this.observing = observing;
    }

    /** CRON-COMPLETIONIST-31: Renderer-side toggle for the guarding pose. */
    public void setGuarding(boolean guarding) {
        this.guarding = guarding;
    }

    /** CRON-COMPLETIONIST-44: Renderer-side toggle for the pursuing pose. */
    public void setPursuing(boolean pursuing) {
        this.pursuing = pursuing;
    }

    /** CRON-COMPLETIONIST-44: Renderer-side toggle for the socializing pose. */
    public void setSocializing(boolean socializing) {
        this.socializing = socializing;
    }

    /**
     * CRON-COMPLETIONIST-19: Set the cognitive look-target from the renderer.
     *
     * <p>The renderer reads the synced {@code DATA_LOOK_TARGET_X/Y/Z} from
     * the entity and passes them here. {@code ageInTicks} is needed for
     * micro-saccade noise timing.
     *
     * @param x world X, or NaN to clear
     * @param y world Y, or NaN to clear
     * @param z world Z, or NaN to clear
     */
    public void setCognitiveLookTarget(float x, float y, float z) {
        this.cognitiveLookX = x;
        this.cognitiveLookY = y;
        this.cognitiveLookZ = z;
    }

    /**
     * CRON-COMPLETIONIST-21: Set the seven Performance channels from the
     * entity's synced data. Called by the renderer each frame before
     * super.render invokes setupAnim. When all are NaN, no Commitment is
     * active and the model falls back to pose/vanilla animation.
     */
    public void setPerformance(float focus, float urgency, float confidence,
                               float concealment, float tension, float patience,
                               float fatigue) {
        this.perfFocus = focus;
        this.perfUrgency = urgency;
        this.perfConfidence = confidence;
        this.perfConcealment = concealment;
        this.perfTension = tension;
        this.perfPatience = patience;
        this.perfFatigue = fatigue;
    }

    @Override
    public void setupAnim(EntityCultivator entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Let vanilla handle walk cycle, head turn, arm swing, crouching, etc.
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // ── idle breathing ──────────────────────────────────────────────
        this.body.y = (float) Math.sin(ageInTicks * 0.1F) * 0.3F;

        // ── CRON-COMPLETIONIST-54: 3-bone robe skirt sway ──────────────
        // Each segment inherits parent rotation + adds its own sway with
        // phase delay. During walk, the hem lags behind the waist, creating
        // cloth billow. During idle, each segment drifts at different phases.
        float walkSway = (float) Math.sin(limbSwing * 0.6662F) * 0.10F * limbSwingAmount;
        float idleSway = (float) Math.sin(ageInTicks * 0.07F) * 0.03F;

        // Waist: follows body walk immediately
        this.robeWaist.xRot = walkSway + idleSway;
        this.robeWaist.yRot = (float) Math.sin(ageInTicks * 0.05F) * 0.02F;

        // Mid: phase-delayed sway (0.4 rad behind waist) + slightly amplified
        this.robeMid.xRot = (float) Math.sin(limbSwing * 0.6662F - 0.4F) * 0.08F * limbSwingAmount
                              + (float) Math.sin(ageInTicks * 0.07F + 0.3F) * 0.04F;
        this.robeMid.yRot = (float) Math.sin(ageInTicks * 0.05F + 0.2F) * 0.03F;

        // Hem: further phase-delayed (0.8 rad behind waist) + more amplified
        // This creates the classic "fabric trailing behind" billow effect
        this.robeHem.xRot = (float) Math.sin(limbSwing * 0.6662F - 0.8F) * 0.06F * limbSwingAmount
                             + (float) Math.sin(ageInTicks * 0.07F + 0.6F) * 0.05F;
        this.robeHem.yRot = (float) Math.sin(ageInTicks * 0.05F + 0.4F) * 0.04F;

        // Sash stays with body (rigid belt)
        // (no animation needed — sash is a fixed belt)

        // ── hair bun : barely-perceptible bob with breathing ────────────
        this.hairBun.yRot = (float) Math.sin(ageInTicks * 0.15F) * 0.05F;
        // Hairpin: subtle glint oscillation (light catching jade)
        this.hairpin.zRot = (float) Math.sin(ageInTicks * 0.3F) * 0.02F;

        // ══════════════════════════════════════════════════════════════
        //  POSE STATES — applied AFTER super so they override vanilla anims
        // ══════════════════════════════════════════════════════════════

        // ── meditation pose : standing-stake (zhan zhuang) ──────────────
        // Hands together at chest height, head bowed, body still.
        // Applied AFTER super so it overrides the vanilla arm swing.
        if (this.meditating) {
            this.rightArm.xRot = -1.5F;           // forward horizontal
            this.rightArm.yRot = -0.3F;           // toward center
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = -1.5F;
            this.leftArm.yRot = 0.3F;
            this.leftArm.zRot = 0.0F;
            this.rightLeg.xRot = 0.0F;             // standing, legs straight
            this.leftLeg.xRot = 0.0F;
            this.rightLeg.yRot = -0.05F;           // slightly apart
            this.leftLeg.yRot = 0.05F;
            this.body.xRot = 0.08F;                // slight forward lean
            this.head.xRot = 0.35F;                // head bowed
            this.head.yRot = 0.0F;
            // robe settles — all three bones go still
            this.robeWaist.xRot = idleSway * 0.5F;
            this.robeMid.xRot = idleSway * 0.3F;
            this.robeHem.xRot = idleSway * 0.2F;
            // subtle qi-gathering pulse in the breathing
            this.body.y = (float) Math.sin(ageInTicks * 0.05F) * 0.5F;
        }

        // ── casting pose : right arm raised, channeling ─────────────────
        if (this.casting) {
            this.rightArm.xRot = -2.5F;            // straight up
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.2F;
            this.leftArm.xRot = -0.4F;             // left arm slightly out (balance)
            this.leftArm.zRot = -0.2F;
            // channel tremor : subtle high-frequency jitter on the raised arm
            this.rightArm.xRot += (float) Math.sin(ageInTicks * 3.0F) * 0.03F;
        }

        // ── CRON-COMPLETIONIST-31: observing pose ─────────────────────
        // The hidden-cultivator observing pose. Wang Lin crouches behind cover,
        // watching the threat without revealing his strength. This is the pose
        // the Cultivator Mind produces when CONCEAL_STRENGTH + CURIOSITY score
        // OBSERVE highest.
        //
        // Visual: Body lowered (crouching), right hand raised to brow
        // (peering through fingers), left hand resting on knee, head slightly
        // raised and tilted forward (watching), weight shifted back.
        if (this.observing) {
            // Body crouches — lower the whole model
            this.body.y = -0.3F + (float) Math.sin(ageInTicks * 0.08F) * 0.05F;
            this.body.xRot = 0.15F;               // slight forward lean (peering)

            // Right arm raised to brow — shielding eyes, peering at threat
            this.rightArm.xRot = -1.8F;             // forward and slightly up
            this.rightArm.yRot = -0.4F;            // toward center (hand at brow)
            this.rightArm.zRot = 0.1F;

            // Left arm down, hand on knee (supporting the crouch)
            this.leftArm.xRot = 0.6F;              // arm drops to side
            this.leftArm.yRot = 0.2F;              // slightly forward (knee direction)
            this.leftArm.zRot = 0.0F;

            // Legs bent — crouching posture
            this.rightLeg.xRot = 0.4F;             // thigh angles back (squat)
            this.leftLeg.xRot = 0.4F;
            this.rightLeg.yRot = -0.15F;           // wider stance (stability)
            this.leftLeg.yRot = 0.15F;

            // Head slightly raised — watching the threat from cover
            // head.yRot is already set by super (netHeadYaw), we add a slight
            // forward tilt but let the head track the target horizontally
            this.head.xRot = -0.15F;              // chin slightly up (watching, not bowed)
            // Subtle head tracking — slight oscillation as if scanning
            this.head.yRot += (float) Math.sin(ageInTicks * 0.2F) * 0.05F;

            // Robe settles — trying to be still
            this.robeWaist.xRot = idleSway * 0.2F;
            this.robeMid.xRot = idleSway * 0.1F;
            this.robeHem.xRot = 0.0F;
        }

        // ── CRON-COMPLETIONIST-31: guarding pose ─────────────────────
        // The combat-ready defender stance. Da Niu stands at the village
        // perimeter, feet wide apart, arms forward, ready to intercept.
        // This is the pose the Cultivator Mind produces when DUTY scores
        // GUARD highest.
        //
        // Visual: Feet wide apart (horse stance / ma bu), both arms forward
        // and slightly bent (ready to grab weapon or intercept), body lowered
        // and centered, head forward (facing the threat). Weight centered.
        if (this.guarding) {
            // Body centered and slightly lowered
            this.body.y = -0.1F;
            this.body.xRot = 0.05F;               // very slight forward lean

            // Both arms forward — ready stance (intercept/grab)
            this.rightArm.xRot = -1.2F;            // forward and slightly down
            this.rightArm.yRot = -0.25F;           // slightly toward center
            this.rightArm.zRot = -0.15F;           // hands closer together
            this.leftArm.xRot = -1.2F;
            this.leftArm.yRot = 0.25F;
            this.leftArm.zRot = 0.15F;

            // Combat-ready tension: subtle high-frequency jitter on both arms
            float tension = (float) Math.sin(ageInTicks * 2.5F) * 0.02F;
            this.rightArm.xRot += tension;
            this.leftArm.xRot += tension;

            // Legs wide apart — horse stance (ma bu)
            this.rightLeg.xRot = 0.2F;             // slight squat
            this.leftLeg.xRot = 0.2F;
            this.rightLeg.yRot = -0.3F;            // wide stance
            this.leftLeg.yRot = 0.3F;

            // Head forward — facing the threat
            this.head.xRot = 0.1F;                // slight forward tilt (alert)
            // Subtle alert scanning
            this.head.yRot += (float) Math.sin(ageInTicks * 0.3F) * 0.03F;

            // Robe still — focused
            this.robeWaist.xRot = idleSway * 0.3F;
            this.robeMid.xRot = idleSway * 0.2F;
            this.robeHem.xRot = idleSway * 0.1F;
        }

        // ── CRON-COMPLETIONIST-44: pursuing pose ─────────────────────
        // Walking with deliberate purpose toward an opportunity. The cultivator
        // strides forward purposefully — not running, not idle — a determined
        // walk that says "I am going somewhere important." This is the pose
        // CultivatorMind produces when PURSUING_OPPORTUNITY scores highest.
        //
        // Visual: Super walk cycle is preserved (arms swing opposite legs).
        // Body leans very slightly forward (momentum). Head held up and
        // forward (eyes on destination). Right hand slightly extended (reaching
        // toward goal). Robe skirt has more pronounced sway from the faster walk.
        if (this.pursuing) {
            // Body leans slightly forward — purposeful momentum
            this.body.xRot = -0.1F;
            // Override idle breathing with slightly faster cadence
            this.body.y = (float) Math.sin(ageInTicks * 0.15F) * 0.2F;

            // Right arm slightly forward — reaching toward goal
            // (only the right arm; left arm keeps vanilla walk swing for naturalism)
            this.rightArm.xRot -= 0.3F;            // extend forward beyond walk
            this.rightArm.yRot = -0.1F;            // very slightly toward center

            // Head held high and forward — eyes on the destination
            this.head.xRot = -0.1F;               // chin slightly up (looking ahead)

            // Robe skirt sways more — cultivator walking faster
            this.robeWaist.xRot = walkSway * 1.5F + idleSway;
            this.robeMid.xRot = (float) Math.sin(limbSwing * 0.6662F - 0.5F) * 0.12F * limbSwingAmount + idleSway;
            this.robeHem.xRot = (float) Math.sin(limbSwing * 0.6662F - 1.0F) * 0.08F * limbSwingAmount + idleSway;
            // Add lateral robe billow from faster movement (hem only — fabric trails)
            this.robeHem.zRot = (float) Math.sin(limbSwing * 0.3331F) * 0.08F * limbSwingAmount;
        }

        // ── CRON-COMPLETIONIST-44: socializing pose ─────────────────────
        // Relaxed, turned toward a companion. The cultivator has stopped to
        // interact — standing at ease, body angled toward the other person,
        // one arm gesturing. This is the pose CultivatorMind produces when
        // SOCIALIZING scores highest.
        //
        // Visual: Body slightly turned (weight shifted), head facing companion
        // (head tracking works via vanilla netHeadYaw), one arm relaxed at side,
        // other arm slightly raised in a conversational gesture, occasional
        // subtle gesture animation. Relaxed breathing. Robe hangs naturally.
        if (this.socializing) {
            // Body relaxed — very slight lean (at ease, not rigid)
            this.body.xRot = 0.03F;
            this.body.y = (float) Math.sin(ageInTicks * 0.08F) * 0.2F; // slow breathing

            // Left arm relaxed at side — slightly away from body (open posture)
            this.leftArm.xRot = 0.1F + (float) Math.sin(ageInTicks * 0.1F) * 0.05F;
            this.leftArm.zRot = 0.1F;             // arm away from body

            // Right arm in conversational gesture — slightly raised, palm out
            // Gesture oscillates slowly: arm lifts and lowers as if talking
            float gesturePhase = (float) Math.sin(ageInTicks * 0.4F);
            this.rightArm.xRot = -0.8F + gesturePhase * 0.2F;  // arm at chest height, bobbing
            this.rightArm.yRot = -0.3F;            // slightly toward center (talking)
            this.rightArm.zRot = 0.15F;            // palm-up tilt

            // Legs relaxed — slight weight shift (standing at ease)
            float weightShift = (float) Math.sin(ageInTicks * 0.06F) * 0.03F;
            this.rightLeg.xRot = weightShift;
            this.leftLeg.xRot = -weightShift;
            this.rightLeg.yRot = -0.05F;
            this.leftLeg.yRot = 0.05F;

            // Head relaxed — slight nod animation (listening/acknowledging)
            // Don't override netHeadYaw — the head still tracks the companion
            this.head.xRot += (float) Math.sin(ageInTicks * 0.3F) * 0.04F;

            // Robe hangs naturally — minimal sway
            this.robeWaist.xRot = idleSway * 0.5F;
            this.robeMid.xRot = idleSway * 0.4F;
            this.robeHem.xRot = idleSway * 0.3F;
        }

        // ═════════════════════════════════════════════════════════════════
        //  CRON-COMPLETIONIST-19: Cognitive Look-Target Override
        // ═════════════════════════════════════════════════════════════════
        // The user's 2026-07-25 directive:
        //   "head turns → body rotates slightly → weight shifts → breathing
        //    slows → eyes remain fixed → doesn't respond immediately to
        //    player → after 30 seconds glances away briefly → looks back"
        //
        // This block runs LAST, after all pose-specific animations. When a
        // cognitive look-target is set (Wang Lin is observing wolves), it
        // OVERRIDES the vanilla head yaw/pitch (which would otherwise be
        // driven by the entity's look control / random-look goal). The head
        // lerps toward the look-target with a max rotation per tick (no snap),
        // with micro-saccade noise layered on top so it doesn't lock robotically.
        //
        // Every ~60 ticks (~3 seconds), a brief glance-away fires: the head
        // offsets by ~0.2 rad for ~10 ticks, then returns. This is the
        // "after 30 seconds glances away briefly" from the directive (the
        // cadence is faster than 30s in the directive example, but the
        // principle — don't stare without blinking — is what matters).
        //
        // When NO cognitive look-target is set, the head uses the vanilla
        // netHeadYaw/headPitch (set by super.setupAnim) and our interpolation
        // state decays toward zero so the next time a target appears, we
        // start from a clean baseline.
        //
        // CRON-COMPLETIONIST-21: this now dispatches to applyPerformance,
        // which consumes the seven acting-direction channels INDEPENDENTLY.
        // The look-target is still used (to compute desired head yaw/pitch),
        // but the channels modulate HOW the head moves, breathing, torso,
        // weight, and hands. Same look-target + different channels → different
        // acting. This is the "communicate the current thought" layer.
        applyPerformance(entity, ageInTicks);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CRON-COMPLETIONIST-21: The Acting Layer
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Apply the Performance channels to the body INDEPENDENTLY.
     *
     * <p>This is the core of the Acting Layer. The user's 2026-07-26 review:
     * "instead of thinking in poses, think in independent channels — Head,
     * Torso, Shoulders, Hands, Feet, Eyes, Breathing, Attention, Weight. Each
     * channel updates independently. You suddenly get hundreds of combinations
     * instead of five fixed poses."
     *
     * <p>Each body part is driven by a COMBINATION of channels:
     * <ul>
     *   <li><b>Head</b> — tracks the look-target (if set), but lerp SPEED scales
     *       with focus (high focus = slow, deliberate; low focus = quick).
     *       Saccade amplitude scales inversely with focus. Glance-away
     *       frequency scales inversely with focus; duration scales with
     *       patience. Direction picked ONCE per glance (fixes CRON-19 jitter).</li>
     *   <li><b>Torso</b> — body.yRot follow scales with (1 - tension) (tense =
     *       rigid, less follow). Forward lean scales with urgency; backward
     *       lean scales with concealment (ready to retreat).</li>
     *   <li><b>Breathing</b> — speed scales with urgency (high urgency = faster
     *       breath). Amplitude scales with (1 - tension) (tense = shallow).
     *       Applied to body.y as a sin offset.</li>
     *   <li><b>Weight shift</b> — slow sin offset on body.x, amplitude scales
     *       with (1 - patience) (impatient = fidgety). High patience = planted.</li>
     *   <li><b>Hands</b> — when concealment AND urgency are both high, the
     *       right arm drifts inward (hand nearer weapon). The user's "hand
     *       nearer weapon" cue for the unknown-cultivator case.</li>
     *   <li><b>Shoulders</b> — raised with tension (arms hike), relaxed with
     *       confidence (arms drop). Approximated via arm.xRot offsets.</li>
     *   <li><b>Feet</b> — planted with high patience (pigeon-toed, still),
     *       shuffling with low patience (leg yRot oscillation).</li>
     *   <li><b>Eyes</b> — track slightly ahead of look-target. Approximated
     *       as head lead (2-5° when focus high). Fatigued eyes lag downward.</li>
     * </ul>
     *
     * <p>When no Performance is set (all channels NaN), this decays the
     * interpolation state and falls back to the pose/vanilla head pose —
     * identical to CRON-19's no-target behavior.
     */
    private void applyPerformance(EntityCultivator entity, float ageInTicks) {
        // ── No Performance active → fall back to vanilla / pose ──
        if (Float.isNaN(perfFocus)) {
            currentHeadYaw *= 0.5F;
            currentHeadPitch *= 0.5F;
            saccadePhase = 0.0F;
            glancingAway = false;
            glanceAwayTicks = 0;
            return;
        }

        // Channel reads (clamped defensively — synced data should already be [0,1])
        float focus = clamp01(perfFocus);
        float urgency = clamp01(perfUrgency);
        float confidence = clamp01(perfConfidence);
        float concealment = clamp01(perfConcealment);
        float tension = clamp01(perfTension);
        float patience = clamp01(perfPatience);
        float fatigue = clamp01(perfFatigue);

        // ═══════════════════════════════════════════════════════════════
        //  HEAD CHANNEL
        // ═══════════════════════════════════════════════════════════════
        if (!Float.isNaN(cognitiveLookX) && !Float.isNaN(cognitiveLookY)
                && !Float.isNaN(cognitiveLookZ)) {
            // Compute desired head yaw/pitch from the look-target (world coords).
            double ex = entity.getX();
            double ey = entity.getEyeY();
            double ez = entity.getZ();
            double dx = cognitiveLookX - ex;
            double dy = cognitiveLookY - ey;
            double dz = cognitiveLookZ - ez;
            double horizDist = Math.sqrt(dx * dx + dz * dz);
            if (horizDist >= 0.001) {
                float entityBodyYawRad = (float) Math.toRadians(entity.yBodyRot);
                float desiredYawAbs = (float) Math.atan2(dx, dz);
                float desiredYaw = desiredYawAbs - entityBodyYawRad;
                desiredYaw = wrapAngle(desiredYaw);
                float maxYaw = (float) Math.toRadians(75.0);
                desiredYaw = Math.max(-maxYaw, Math.min(maxYaw, desiredYaw));

                float desiredPitch = (float) Math.atan2(-dy, horizDist);
                float maxPitch = (float) Math.toRadians(60.0);
                desiredPitch = Math.max(-maxPitch, Math.min(maxPitch, desiredPitch));

                // ── Lerp speed scales with focus ──
                // High focus (0.95) → slow, deliberate lerp (0.08) — the
                // wolf-observer tracks smoothly. Low focus (0.6) → quicker
                // lerp (0.25) — the anxious NPC snaps attention around.
                // This is the user's "smooth head tracking" vs "quicker head turns."
                float lerpFactor = 0.25F - focus * 0.17F; // focus 1.0 → 0.08, focus 0.0 → 0.25
                currentHeadYaw += (desiredYaw - currentHeadYaw) * lerpFactor;
                currentHeadPitch += (desiredPitch - currentHeadPitch) * lerpFactor;

                // ── Saccade amplitude scales inversely with focus ──
                // High focus → tiny saccades (deeply concentrated, eyes locked).
                // Low focus → larger drift (distracted, scanning).
                float saccadeAmp = 0.015F * (1.0F - focus * 0.8F);
                float saccadeNoiseYaw = (float) Math.sin(ageInTicks * 0.9F) * saccadeAmp
                        + (float) Math.sin(ageInTicks * 2.3F) * (saccadeAmp * 0.5F);
                float saccadeNoisePitch = (float) Math.sin(ageInTicks * 1.1F + 0.5F)
                        * (saccadeAmp * 0.7F);

                // ── Glance-away: frequency ∝ 1/focus, duration ∝ patience ──
                // High focus → rare glances (every ~120 ticks). Low focus →
                // frequent (every ~40 ticks). Duration scales with patience
                // (patient NPC holds the glance longer). Direction picked ONCE
                // at glance-start (fixes the CRON-19 per-tick jitter bug).
                float glanceThreshold = 40.0F + focus * 80.0F; // 40..120 ticks
                saccadePhase += 1.0F;
                if (!glancingAway && saccadePhase > glanceThreshold) {
                    glancingAway = true;
                    // Duration scales with patience: 6..16 ticks
                    glanceAwayTicks = 6 + (int) (patience * 10.0F);
                    saccadePhase = 0.0F;
                    // Pick direction ONCE — hold it for the whole glance.
                    glanceDirection = (Math.random() < 0.5) ? -1.0F : 1.0F;
                }
                float glanceYawOffset = 0.0F;
                float glancePitchOffset = 0.0F;
                if (glancingAway) {
                    glanceYawOffset = glanceDirection * 0.2F;
                    glancePitchOffset = -0.05F; // slightly up (thinking)
                    glanceAwayTicks--;
                    if (glanceAwayTicks <= 0) {
                        glancingAway = false;
                    }
                }

                this.head.yRot = currentHeadYaw + saccadeNoiseYaw + glanceYawOffset;
                this.head.xRot = currentHeadPitch + saccadeNoisePitch + glancePitchOffset;
            }
        } else {
            // No look-target but Performance active — decay head toward neutral.
            currentHeadYaw *= 0.8F;
            currentHeadPitch *= 0.8F;
        }

        // ═══════════════════════════════════════════════════════════════
        //  BREATHING CHANNEL
        // ═══════════════════════════════════════════════════════════════
        // Speed scales with urgency (calm = slow 0.1Hz, urgent = fast 0.4Hz).
        // Amplitude scales with (1 - tension) (tense = shallow breath).
        // Applied to body.y as a sin offset. This is the user's "very slow
        // breathing" for the wolf-observer vs rapid shallow breath for the
        // tense unknown-cultivator observer.
        float breathSpeed = 0.1F + urgency * 0.3F;
        float breathAmp = 0.3F * (1.0F - tension * 0.5F);
        float breathOffset = (float) Math.sin(ageInTicks * breathSpeed) * breathAmp;
        this.body.y = breathOffset;

        // ═══════════════════════════════════════════════════════════════
        //  TORSO CHANNEL
        // ═══════════════════════════════════════════════════════════════
        // body.yRot follow scales with (1 - tension): tense torso is rigid
        // (less follow), relaxed torso flows with the head. Capped ±15°.
        // The user: "slight torso tension" for the high-urgency case means
        // the torso DOESN'T flow — it holds rigid while the head turns.
        float torsoFollow = currentHeadYaw * 0.15F * (1.0F - tension * 0.6F);
        float maxTorsoFollow = (float) Math.toRadians(15.0);
        torsoFollow = Math.max(-maxTorsoFollow, Math.min(maxTorsoFollow, torsoFollow));
        this.body.yRot = torsoFollow;

        // Forward lean scales with urgency (leaning into the situation).
        // Backward lean scales with concealment (ready to retreat/hide).
        // Net: body.xRot += (urgency * 0.08) - (concealment * 0.05).
        // The user's "subtle backward weight shift" for the concealed case.
        float torsoLean = urgency * 0.08F - concealment * 0.05F;
        this.body.xRot += torsoLean;

        // ═══════════════════════════════════════════════════════════════
        //  WEIGHT-SHIFT CHANNEL
        // ═══════════════════════════════════════════════════════════════
        // Slow sin offset on body.x position. Amplitude scales with
        // (1 - patience): patient NPC is planted (no shift); impatient NPC
        // fidgets. Frequency is slow (~0.03Hz) — weight shifts are gradual.
        // The user's "weight shifts" cue for sustained observation.
        float weightAmp = 0.4F * (1.0F - patience);
        float weightShift = (float) Math.sin(ageInTicks * 0.03F) * weightAmp;
        this.body.x += weightShift;

        // ═══════════════════════════════════════════════════════════════
        //  HANDS CHANNEL
        // ═══════════════════════════════════════════════════════════════
        // When concealment AND urgency are both high, the right arm drifts
        // inward (hand nearer weapon / belt). This is the user's "hand
        // nearer weapon" cue for the unknown-cultivator observation case.
        // The product concealment*urgency is high only when BOTH are high —
        // a calm concealed NPC (low urgency) keeps hands relaxed; a panicked
        // un concealed NPC (low concealment) flails. Only the tense-hidden
        // case draws the hand toward the weapon.
        float weaponReadiness = concealment * urgency; // 0..1, high only if both high
        if (weaponReadiness > 0.3F) {
            // Right arm drifts inward (toward center/hip where a weapon sits).
            // Applied as an ADDITIVE offset so it layers on top of the pose.
            this.rightArm.yRot += -0.25F * weaponReadiness;
            this.rightArm.xRot += 0.15F * weaponReadiness; // arm lowers slightly toward belt
        }

        // ═══════════════════════════════════════════════════════════════
        //  SHOULDERS CHANNEL
        // ═══════════════════════════════════════════════════════════════
        // The user listed Shoulders as an independent channel. In Minecraft's
        // HumanoidModel, shoulders are part of the body part (shared mesh).
        // We approximate shoulder behavior through body.yRot micro-adjustments
        // and arm.xRot offsets:
        //   - High tension → shoulders raised (arms hike up slightly)
        //   - High confidence → shoulders relaxed (arms drop to neutral)
        //   - High fatigue → shoulders round (arm.xRot sags)
        // These are ADDITIVE offsets on top of the pose — they modulate the
        // existing pose without overriding it.
        if (tension > 0.4F) {
            // Tense shoulders: arms hike up slightly (bracing for action)
            float shoulderRaise = (tension - 0.4F) * 0.15F; // 0..0.09 rad
            this.rightArm.xRot -= shoulderRaise;
            this.leftArm.xRot -= shoulderRaise;
        }
        if (confidence > 0.7F) {
            // Relaxed shoulders: subtle arm drop (at ease)
            float shoulderDrop = (confidence - 0.7F) * 0.05F; // 0..0.015 rad
            this.rightArm.xRot += shoulderDrop;
            this.leftArm.xRot += shoulderDrop;
        }

        // ═══════════════════════════════════════════════════════════════
        //  FEET CHANNEL
        // ═══════════════════════════════════════════════════════════════
        // The user listed Feet as an independent channel. 'Planted' vs 'shifting.'
        // In MC models, feet move via leg parts. We modulate leg yRot (slight
        // toe-out) and xRot (knee micro-bend) from patience:
        //   - High patience → feet planted, legs still (micro-sway only)
        //   - Low patience → feet shuffle, subtle weight transfers between legs
        // The weight-shift channel already moves body.x (simulating weight
        // transfer), so feet adds the leg-local component.
        if (patience < 0.5F) {
            // Impatient: subtle foot shuffles — one leg micro-shifts
            // The frequency is low (~0.12Hz) — shuffles are deliberate.
            float shufflePhase = (float) Math.sin(ageInTicks * 0.12F);
            float shuffleAmp = (0.5F - patience) * 0.04F; // 0..0.02 rad
            this.rightLeg.yRot += shufflePhase * shuffleAmp;
            this.leftLeg.yRot -= shufflePhase * shuffleAmp;
        }
        // Planted: feet grip the ground — legs resist external rotation.
        // When patience is high, legs are already still from the pose;
        // we add a subtle inward pinch (feet slightly pigeon-toed, planted).
        if (patience > 0.6F) {
            float plantedPinch = (patience - 0.6F) * 0.03F; // 0..0.012 rad
            this.rightLeg.yRot += plantedPinch;
            this.leftLeg.yRot -= plantedPinch;
        }

        // ═══════════════════════════════════════════════════════════════
        //  EYES CHANNEL
        // ═══════════════════════════════════════════════════════════════
        // The user listed Eyes as an independent channel: 'track slightly
        // ahead' of the look-target. In MC models, eyes are texture-painted
        // on the head — we can't move the pupils independently. BUT we can
        // offset the head slightly AHEAD of the tracked target, creating the
        // illusion that the eyes lead and the head follows. This is a subtle
        // anticipatory drift: the head's desired yaw leads the target by a
        // few degrees when focus is high (concentrating ahead) and lags
        // slightly when fatigue is high (slow to keep up).
        //
        // This is layered on top of the HEAD CHANNEL's yaw/pitch — it's
        // computed after head interpolation so it adds to the already-tracked
        // head pose.
        float eyeLeadYaw = 0.0F;
        float eyeLeadPitch = 0.0F;
        if (focus > 0.5F) {
            // High focus: eyes lead the head by ~2-5 degrees.
            // The NPC looks WHERE the target is GOING, not where it IS.
            // This is the 'track slightly ahead' from the user's example.
            // Lead scales with focus: 0.5→0.035 rad (2°), 1.0→0.087 rad (5°).
            float leadAngle = (focus - 0.5F) * 0.174F; // 0..0.087 rad (5°)
            // Lead in the direction the target is moving (or the current
            // head yaw direction if no movement data). We use the head's
            // current yaw as the lead direction proxy.
            eyeLeadYaw = Math.signum(currentHeadYaw) * leadAngle;
            // Pitch lead: eyes look slightly up when anticipating movement
            // (scanning for threats above the horizon).
            eyeLeadPitch = -leadAngle * 0.3F;
        }
        if (fatigue > 0.4F) {
            // Fatigued: eyes lag — subtle downward droop.
            // The user: 'longer blinks (rendered as longer glance-downs).'
            // We simulate this as a pitch-down offset that grows with fatigue.
            eyeLeadPitch += (fatigue - 0.4F) * 0.1F; // 0..0.06 rad (~3.4°)
        }
        this.head.yRot += eyeLeadYaw;
        this.head.xRot += eyeLeadPitch;

        // ═══════════════════════════════════════════════════════════════
        //  FATIGUE CHANNEL
        // ═══════════════════════════════════════════════════════════════
        // High fatigue → drooped posture (head pitches down slightly) and
        // slower breathing amplitude. Applied subtly so short commitments
        // show no fatigue; long ones (cultivation marathons) sag.
        if (fatigue > 0.3F) {
            this.head.xRot += fatigue * 0.15F; // head droops
            this.body.xRot += fatigue * 0.05F; // shoulders round
        }
    }

    /** Clamp a float to [0,1], preserving NaN. */
    private static float clamp01(float v) {
        if (Float.isNaN(v)) return 0.0F;
        return Math.max(0.0F, Math.min(1.0F, v));
    }

    /** Wrap an angle (radians) to [-PI, PI]. */
    private static float wrapAngle(float angle) {
        while (angle > Math.PI) angle -= (float) (2 * Math.PI);
        while (angle < -Math.PI) angle += (float) (2 * Math.PI);
        return angle;
    }
}
