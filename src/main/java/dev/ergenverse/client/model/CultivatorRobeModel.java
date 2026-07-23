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
    }
}
