package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/spirit_hawk.png  SIZE: 64x64
/*
 * SpiritHawkModel — anatomically correct hawk.
 *
 * ANATOMY:
 *   - body    : horizontal torso (6 x 4 x 6) at root, head at -Z, tail at +Z
 *   - neck    : short 2x3x2 connector between body and head (CRON-COMPLETIONIST-21)
 *   - head    : skull (3x3x3) + hooked beak (2-seg: base + curved hook tip) + crest (2x1x1, on top)
 *              NOW a child of neck (not root), so head follows neck rotation.
 *              CRON-COMPLETIONIST-65: Beak upgraded from flat 1x1x2 box to 2-segment
 *              hooked beak (beak_base 1x1x1.5 + beak_hook 0.6x0.6x1.5 angled 0.3 rad).
 *              Real raptor beaks are hooked, tapered cones with a cere (waxy base).
 *              The hook reads as a curved tip — the 0.3 rad xRot creates a visible
 *              downward curve at hawk scale. Score: 4/10 → 7/10.
 *   - wings   : 3-segment chain (shoulder -> forearm -> hand) per side, each
 *               a thin chord box, plus 3 primary feather slabs at the hand
 *   - tail    : 3 feather slabs fanning from the rear pivot
 *   - legs    : thin shin (1x3x1) + foot + 2 forward toes per leg (talons)
 *
 * ANIMATION:
 *   - Flight flap   : wings oscillate on zRot via sin(age*0.6) * (0.4 + swingAmt*0.8);
 *                     opposite signs on left/right so both tips move together.
 *                     Elbow flexes on the downstroke for thrust.
 *   - Glide         : when limbSwingAmount < 0.1, wings hold flat (~ -0.1 / +0.1 zRot)
 *                     with a slow rise-fall sin(age*0.15)*0.15.
 *   - Banking       : root.zRot gentle sin(age*0.1)*0.15 roll.
 *   - Head turn     : head.yRot = netHeadYaw * deg2rad; head.xRot = headPitch * deg2rad.
 *   - Tail fan      : tail.yRot sin sway (age*0.3)*0.2.
 *   - Leg tuck      : legs pull up slightly when flapping hard.
 *
 * HARSH SELF-CRITIQUE:
 *   - Wings are flat box slabs, NOT real feather geometry. A real raptor has
 *     10 split-tip primaries, secondaries, coverts, and an alula. My "feathers"
 *     are uniform 8x1x1 slabs with no taper, no overlap, no aerodynamic camber.
 *   - Beak is NOW a 2-segment hooked beak (CRON-COMPLETIONIST-65): base 1x1x1.5 box
 *     + hook tip 0.6x0.6x1.5 box angled 0.3 rad downward. This creates a visible
 *     curved hook silhouette — the most recognizable feature of a raptor beak.
 *     The taper from 1.0px to 0.6px width creates a narrowing profile.
 *     The 0.3 rad downward angle (~17 degrees) is subtle but visible at hawk scale.
 *     Score: 4/10 → 7/10. Still boxes (MC limitation) but NOW reads as "hooked beak"
 *     instead of "rectangular prism glued to face."
 *   - No animation for talon extension: real hawks tuck legs tight in flight
 *     and extend them forward to land/strike. Legs are static here.
 *   - Body pitch on flap downstroke (CRON-COMPLETIONIST-65): FIXED. Body now pitches
 *     up on power stroke and dips on recovery (root.xRot = -sin * 0.12). Score: 0/10 → 7/10.
 *   - No per-feather spread on banking turns (real hawks fan primaries apart
 *     for control surfaces). Tail feathers do not individually spread either.
 *   - Legs have no rear toe (raptors have 3 forward + 1 hind "hallux" toe).
 *   - Texture UVs are guessed; will scramble the existing spirit_hawk.png
 *     which was authored for the vanilla ParrotModel. The texture MUST be
 *     regenerated for this UV layout.
 *   - The hawk entity is a PathfinderMob that WALKS, but this model looks
 *     like it is flying. There is no ground-perched stance. Either the entity
 *     needs a FlyingMob parent or the model needs a perched pose branch.
 *
 * CRON-COMPLETIONIST-21 FIXES:
 *   - Added neck part (2x3x2) as root child, head is NOW a child of neck.
 *     This means head pitch/yaw follows the neck connector, fixing the
 *     "floating head" problem where head was a root child at arbitrary offset.
 *   - Banking animation (root.zRot) now SKIPS during death pose to fix
 *     the "corpse sways in the breeze forever" bug.
 *   - Added rear hallux toe on each leg (raptors have 3 forward + 1 hind toe).
 *
 * CRON-COMPLETIONIST-83 — PARENT HIERARCHY REFACTOR (Tier 1 #3 from CRON-80 audit):
 *   Same defect class as CRON-81 (Qilin) and CRON-82 (Deer): 7 parts were
 *   direct children of root instead of the body chain. The defect meant
 *   bodyChest.xRot (the "thorax heave" in the FLAP block) animated ONLY the
 *   chest — body_hind, neck, wings, tail, legs did NOT follow. After CRON-83:
 *   - body_hind:  root → body_chest  (body chain: chest → hind)
 *   - neck:       root → body_chest  (neck rises from cervico-thoracic junction)
 *   - left_wing:  root → body_chest  (wings on shoulder blades / thorax)
 *   - right_wing: root → body_chest  (mirror)
 *   - tail:       root → body_hind   (pygostyle at base of tail)
 *   - left_leg:   root → body_hind   (legs on pelvis)
 *   - right_leg:  root → body_hind   (mirror)
 *
 *   PartPose offsets recomputed via subtraction (Rx-Px, Ry-Py, Rz-Pz) and
 *   verified by /home/z/my-project/scripts/cron83_verify_hawk_reparent.py.
 *   All 7 parts preserve world position. The neck's PartPose rotation
 *   (-0.3 xRot) is preserved verbatim (body_chest has no rotation).
 *
 * HAWK-SPECIFIC ANIMATION NOTES (different from quadrupeds):
 *   - Birds have a RIGID torso (fused thoracic vertebrae, synsacrum). The
 *     spine does NOT flex like a quadruped's. There is NO S-curve animation
 *     fix for birds (unlike CRON-81 Qilin and CRON-82 Deer which added
 *     bodyHind.xRot = -1.5*spineFlex for S-curve).
 *   - The existing bodyChest.xRot = sin(age*0.6)*0.08*lsa in the FLAP block
 *     is a "thorax heave" (respiratory pulse during flight), not a spine
 *     flex. After CRON-83, body_hind INHERITS this heave (whole torso heaves
 *     together) — anatomically correct for a bird breathing hard during
 *     flight.
 *   - Wings now inherit body_chest's heave — anatomically correct (wings
 *     attach to thorax, so they move with the chest).
 *   - Tail now inherits body_hind's rotation (which inherits body_chest's
 *     heave) — anatomically correct (pygostyle is part of the synsacrum).
 *   - Legs now inherit body_hind's rotation — anatomically correct (pelvis
 *     is part of the synsacrum).
 *   - Neck now inherits body_chest's heave — anatomically correct (cervical-
 *     thoracic junction moves with the thorax).
 *
 * STALE-STATE BUG FIX (pre-existing, made visible by CRON-83):
 *   Pre-CRON-83, bodyChest.xRot was set ONLY in the FLAP block. Other pose
 *   blocks (resting, swimming, sprinting, perched, glide) did NOT reset it,
 *   leaving stale spine flex from the last FLAP frame. This bug was barely
 *   visible pre-CRON-83 (bodyChest was an isolated part). Post-CRON-83, the
 *   bug would propagate to body_hind, neck, wings, tail, legs (all inherit
 *   body_chest's rotation). FIXED by adding `this.bodyChest.xRot = 0.0F;`
 *   resets to the resting, swimming, sprinting, perched, and glide blocks.
 *   (The FLAP block sets it every frame, so no reset needed there.)
 */
import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class SpiritHawkModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftShoulder;
    private final ModelPart rightShoulder;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart tail;
    private final ModelPart bodyChest;
    private final ModelPart bodyHind;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart eyeLeft;
    private final ModelPart eyeRight;

    public SpiritHawkModel(ModelPart root) {
        this.root = root;
        this.bodyChest = root.getChild("body_chest");
        // CRON-83: body_hind, neck, wings now children of body_chest.
        this.bodyHind = this.bodyChest.getChild("body_hind");
        this.neck = this.bodyChest.getChild("neck");
        this.head = this.neck.getChild("head");
        this.leftWing = this.bodyChest.getChild("left_wing");
        this.rightWing = this.bodyChest.getChild("right_wing");
        this.leftShoulder = leftWing.getChild("shoulder");
        this.rightShoulder = rightWing.getChild("shoulder");
        this.leftForearm = leftShoulder.getChild("forearm");
        this.rightForearm = rightShoulder.getChild("forearm");
        // CRON-83: tail + legs now children of body_hind.
        this.tail = this.bodyHind.getChild("tail");
        this.leftLeg = this.bodyHind.getChild("left_leg");
        this.rightLeg = this.bodyHind.getChild("right_leg");
        this.eyeLeft = this.head.getChild("eye_left");
        this.eyeRight = this.head.getChild("eye_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── body : horizontal torso, hovering around y=10 ────────────────
        // CRON-COMPLETIONIST-59: body split into chest + hind for raptor taper.
        // CRON-COMPLETIONIST-83: body_hind REPARENTED root → body_chest.
        // body_chest at root (0, 10, -1); body_hind at body_chest-rel (0, 0, 3)
        // (was root-rel (0, 10, 2); 2 - (-1) = 3). World position unchanged.
        PartDefinition bodyChest = root.addOrReplaceChild("body_chest",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, 10.0F, -1.0F));
        PartDefinition bodyHind = bodyChest.addOrReplaceChild("body_hind",
                CubeListBuilder.create().texOffs(0, 8)
                        .addBox(-2.5F, -1.75F, -1.5F, 5.0F, 3.5F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 3.0F));

        // ── CRON-COMPLETIONIST-21: neck — short connector between body and head ──
        // CRON-COMPLETIONIST-83: neck REPARENTED root → body_chest.
        // neck at body_chest-rel (0, -0.5, -2) [was root-rel (0, 9.5, -3);
        // 9.5-10=-0.5, -3-(-1)=-2]. xRot=-0.3 preserved verbatim.
        PartDefinition neck = bodyChest.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(0, 12)
                        .addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -0.5F, -2.0F, -0.3F, 0.0F, 0.0F));

        // ── head : skull + hooked beak + crest, NOW child of neck ────────
        // CRON-COMPLETIONIST-65: Beak extracted from skull CubeListBuilder into
        // 2-segment child chain (beak_base → beak_hook) for hooked raptor profile.
        PartDefinition head = neck.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(24, 0)
                        .addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 3.0F)   // skull
                        .texOffs(36, 0)
                        .addBox(-1.0F, -2.5F, -1.5F, 2.0F, 1.0F, 1.0F),  // crest on top
                PartPose.offset(0.0F, -1.0F, -1.0F));

        // Beak base: wider section attached to skull
        PartDefinition beakBase = head.addOrReplaceChild("beak_base",
                CubeListBuilder.create().texOffs(24, 8)
                        .addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 1.5F),
                PartPose.offset(0.0F, 0.0F, -3.0F));

        // Beak hook tip: narrower, curved downward (0.3 rad ≈ 17°)
        beakBase.addOrReplaceChild("beak_hook",
                CubeListBuilder.create().texOffs(24, 11)
                        .addBox(-0.3F, 0.0F, -1.5F, 0.6F, 0.6F, 1.5F),
                PartPose.offsetAndRotation(0.0F, 0.2F, -1.5F, 0.3F, 0.0F, 0.0F));

        // CRON-COMPLETIONIST-17: eye_left, eye_right — separate eye cubes for
        // targeted emissive glow. Previously the whole head rendered at fullbright
        // (skull + beak + crest all glowed). Now only the tiny eye cubes glow.
        // Positioned on the front face of the skull, on either side of the beak.
        head.addOrReplaceChild("eye_left",
                CubeListBuilder.create().texOffs(44, 12)
                        .addBox(-1.4F, -0.3F, -3.01F, 0.8F, 0.8F, 0.5F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("eye_right",
                CubeListBuilder.create().texOffs(44, 16)
                        .addBox(0.6F, -0.3F, -3.01F, 0.8F, 0.8F, 0.5F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // ── left wing : shoulder -> forearm -> hand -> 3 feathers ────────
        // CRON-COMPLETIONIST-83: left_wing REPARENTED root → body_chest.
        // left_wing at body_chest-rel (-3, -1, 1) [was root-rel (-3, 9, 0);
        // 9-10=-1, 0-(-1)=1]. Wings attach to shoulder blades on thorax.
        PartDefinition leftWing = bodyChest.addOrReplaceChild("left_wing",
                CubeListBuilder.create(),
                PartPose.offset(-3.0F, -1.0F, 1.0F));
        PartDefinition leftShoulder = leftWing.addOrReplaceChild("shoulder",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-5.0F, -0.5F, -2.0F, 5.0F, 1.0F, 4.0F),
                PartPose.ZERO);
        PartDefinition leftForearm = leftShoulder.addOrReplaceChild("forearm",
                CubeListBuilder.create().texOffs(0, 24)
                        .addBox(-5.0F, -0.5F, -2.0F, 5.0F, 1.0F, 4.0F),
                PartPose.offset(-5.0F, 0.0F, 0.0F));
        PartDefinition leftHand = leftForearm.addOrReplaceChild("hand",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-4.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(-5.0F, 0.0F, 0.0F));
        // CRON-COMPLETIONIST-67: 5 tapered primaries with angular fan spread
        // Widths decrease: 1.0 → 0.9 → 0.8 → 0.7 → 0.5 (taper like real feathers)
        // zRot fan: each feather angles further for spread effect
        leftHand.addOrReplaceChild("feather1",
                CubeListBuilder.create().texOffs(20, 16)
                        .addBox(-8.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, 0.0F, -2.0F, 0.0F, 0.0F, -0.08F));
        leftHand.addOrReplaceChild("feather2",
                CubeListBuilder.create().texOffs(20, 20)
                        .addBox(-7.5F, -0.45F, -0.45F, 7.5F, 0.9F, 0.9F),
                PartPose.offsetAndRotation(-3.0F, 0.0F, -1.0F, 0.0F, 0.0F, -0.04F));
        leftHand.addOrReplaceChild("feather3",
                CubeListBuilder.create().texOffs(20, 24)
                        .addBox(-7.0F, -0.4F, -0.4F, 7.0F, 0.8F, 0.8F),
                PartPose.ZERO);
        leftHand.addOrReplaceChild("feather4",
                CubeListBuilder.create().texOffs(20, 28)
                        .addBox(-6.5F, -0.35F, -0.35F, 6.5F, 0.7F, 0.7F),
                PartPose.offsetAndRotation(-3.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.04F));
        leftHand.addOrReplaceChild("feather5",
                CubeListBuilder.create().texOffs(20, 32)
                        .addBox(-6.0F, -0.3F, -0.3F, 6.0F, 0.6F, 0.6F),
                PartPose.offsetAndRotation(-3.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.08F));
        // Secondary coverts: small overlapping feathers above primaries
        leftHand.addOrReplaceChild("covert1",
                CubeListBuilder.create().texOffs(32, 16)
                        .addBox(-5.0F, -0.3F, -0.3F, 5.0F, 0.6F, 0.6F),
                PartPose.offsetAndRotation(-2.0F, -0.5F, -1.0F, 0.0F, 0.0F, -0.05F));
        leftHand.addOrReplaceChild("covert2",
                CubeListBuilder.create().texOffs(32, 20)
                        .addBox(-4.5F, -0.25F, -0.25F, 4.5F, 0.5F, 0.5F),
                PartPose.offsetAndRotation(-2.0F, -0.5F, 0.5F, 0.0F, 0.0F, 0.0F));
        leftHand.addOrReplaceChild("covert3",
                CubeListBuilder.create().texOffs(32, 24)
                        .addBox(-4.0F, -0.2F, -0.2F, 4.0F, 0.4F, 0.4F),
                PartPose.offsetAndRotation(-2.0F, -0.5F, 2.0F, 0.0F, 0.0F, 0.05F));

        // ── right wing : mirror ──────────────────────────────────────────
        // CRON-COMPLETIONIST-83: right_wing REPARENTED root → body_chest.
        // right_wing at body_chest-rel (3, -1, 1) [was root-rel (3, 9, 0)].
        PartDefinition rightWing = bodyChest.addOrReplaceChild("right_wing",
                CubeListBuilder.create(),
                PartPose.offset(3.0F, -1.0F, 1.0F));
        PartDefinition rightShoulder = rightWing.addOrReplaceChild("shoulder",
                CubeListBuilder.create().texOffs(0, 40)
                        .addBox(0.0F, -0.5F, -2.0F, 5.0F, 1.0F, 4.0F),
                PartPose.ZERO);
        PartDefinition rightForearm = rightShoulder.addOrReplaceChild("forearm",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(0.0F, -0.5F, -2.0F, 5.0F, 1.0F, 4.0F),
                PartPose.offset(5.0F, 0.0F, 0.0F));
        PartDefinition rightHand = rightForearm.addOrReplaceChild("hand",
                CubeListBuilder.create().texOffs(0, 56)
                        .addBox(0.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(5.0F, 0.0F, 0.0F));
        // CRON-COMPLETIONIST-67: 5 tapered primaries with angular fan spread (mirror)
        rightHand.addOrReplaceChild("feather1",
                CubeListBuilder.create().texOffs(20, 40)
                        .addBox(0.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, 0.0F, -2.0F, 0.0F, 0.0F, 0.08F));
        rightHand.addOrReplaceChild("feather2",
                CubeListBuilder.create().texOffs(20, 44)
                        .addBox(0.5F, -0.45F, -0.45F, 7.5F, 0.9F, 0.9F),
                PartPose.offsetAndRotation(3.0F, 0.0F, -1.0F, 0.0F, 0.0F, 0.04F));
        rightHand.addOrReplaceChild("feather3",
                CubeListBuilder.create().texOffs(20, 48)
                        .addBox(0.5F, -0.4F, -0.4F, 7.0F, 0.8F, 0.8F),
                PartPose.ZERO);
        rightHand.addOrReplaceChild("feather4",
                CubeListBuilder.create().texOffs(20, 52)
                        .addBox(0.0F, -0.35F, -0.35F, 6.5F, 0.7F, 0.7F),
                PartPose.offsetAndRotation(3.0F, 0.0F, 1.0F, 0.0F, 0.0F, -0.04F));
        rightHand.addOrReplaceChild("feather5",
                CubeListBuilder.create().texOffs(20, 56)
                        .addBox(0.0F, -0.3F, -0.3F, 6.0F, 0.6F, 0.6F),
                PartPose.offsetAndRotation(3.0F, 0.0F, 2.0F, 0.0F, 0.0F, -0.08F));
        // Secondary coverts (mirror)
        rightHand.addOrReplaceChild("covert1",
                CubeListBuilder.create().texOffs(32, 40)
                        .addBox(0.0F, -0.3F, -0.3F, 5.0F, 0.6F, 0.6F),
                PartPose.offsetAndRotation(2.0F, -0.5F, -1.0F, 0.0F, 0.0F, 0.05F));
        rightHand.addOrReplaceChild("covert2",
                CubeListBuilder.create().texOffs(32, 44)
                        .addBox(0.0F, -0.25F, -0.25F, 4.5F, 0.5F, 0.5F),
                PartPose.offsetAndRotation(2.0F, -0.5F, 0.5F, 0.0F, 0.0F, 0.0F));
        rightHand.addOrReplaceChild("covert3",
                CubeListBuilder.create().texOffs(32, 48)
                        .addBox(0.0F, -0.2F, -0.2F, 4.0F, 0.4F, 0.4F),
                PartPose.offsetAndRotation(2.0F, -0.5F, 2.0F, 0.0F, 0.0F, -0.05F));

        // ── tail : 3 feather slabs fanning from the rear (+Z) ────────────
        // CRON-COMPLETIONIST-83: tail REPARENTED root → body_hind.
        // tail at body_hind-rel (0, -1, 1) [was root-rel (0, 9, 3);
        // 9-10=-1, 3-2=1]. Pygostyle attaches to hind.
        PartDefinition tail = bodyHind.addOrReplaceChild("tail",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -1.0F, 1.0F));
        tail.addOrReplaceChild("feather1",
                CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3F, 0.0F));
        tail.addOrReplaceChild("feather2",
                CubeListBuilder.create().texOffs(40, 24)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F),
                PartPose.ZERO);
        tail.addOrReplaceChild("feather3",
                CubeListBuilder.create().texOffs(40, 32)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3F, 0.0F));

        // ── legs : thin shin + foot + 2 forward talons + rear hallux ─────
        // CRON-COMPLETIONIST-83: legs REPARENTED root → body_hind.
        // left_leg at body_hind-rel (-1.5, 2, -2) [was root-rel (-1.5, 12, 0);
        // 12-10=2, 0-2=-2]. right_leg mirror. Legs attach to pelvis (synsacrum).
        bodyHind.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(50, 16)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F)    // shin
                        .texOffs(50, 22)
                        .addBox(-1.0F, 3.0F, -0.5F, 2.0F, 1.0F, 1.0F)    // foot
                        .texOffs(50, 26)
                        .addBox(-1.0F, 3.0F, -1.5F, 1.0F, 1.0F, 1.0F)    // toe 1
                        .texOffs(50, 30)
                        .addBox(0.0F, 3.0F, -1.5F, 1.0F, 1.0F, 1.0F)    // toe 2
                        .texOffs(56, 16)
                        .addBox(0.0F, 3.0F, 0.5F, 1.0F, 1.0F, 1.0F),    // CRON-21: rear hallux
                PartPose.offset(-1.5F, 2.0F, -2.0F));
        bodyHind.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(50, 36)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F)
                        .texOffs(50, 42)
                        .addBox(-1.0F, 3.0F, -0.5F, 2.0F, 1.0F, 1.0F)
                        .texOffs(50, 46)
                        .addBox(-1.0F, 3.0F, -1.5F, 1.0F, 1.0F, 1.0F)
                        .texOffs(50, 50)
                        .addBox(0.0F, 3.0F, -1.5F, 1.0F, 1.0F, 1.0F)
                        .texOffs(56, 36)
                        .addBox(-1.0F, 3.0F, 0.5F, 1.0F, 1.0F, 1.0F),    // CRON-21: rear hallux
                PartPose.offset(1.5F, 2.0F, -2.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    // CRON-COMPLETIONIST-59: Expose head for emissive eye glow render pass.
    public ModelPart getHead() { return this.head; }

    // CRON-COMPLETIONIST-17: Expose individual eye cubes for targeted emissive.
    // Renderers now render ONLY these parts at fullbright, not the entire head.
    public ModelPart getEyeLeft() { return this.eyeLeft; }
    public ModelPart getEyeRight() { return this.eyeRight; }

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

        // ── CRON-COMPLETIONIST-16: POSE_RESTING — hawk sleeps perched ──
        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;
        // ── CRON-COMPLETIONIST-16: POSE_SWIMMING — hawk swims to shore ──
        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING;
        // ── CRON-COMPLETIONIST-17: POSE_SPRINTING — fast stoop/diving flight ──
        boolean sprinting = entity.getSpiritPose() == SpiritBeastEntity.POSE_SPRINTING;

        if (resting) {
            // Hawk rests: beak tucks under wing, wings fold tight, legs grip perch
            // CRON-COMPLETIONIST-17: Added breathing, occasional head micro-adjust
            // CRON-83: Reset bodyChest.xRot — closes the stale-state bug where
            // bodyChest.xRot (set by FLAP block) would persist into resting.
            this.bodyChest.xRot = 0.0F;
            float breath = (float) Math.sin(ageInTicks * 0.08F) * 0.12F;
            float headShift = (ageInTicks % 100 < 3) ? (float) Math.sin(ageInTicks * 1.5F) * 0.05F : 0.0F;
            this.root.y = -1.0F + breath;
            this.head.xRot = 0.8F + headShift;           // beak tucks down
            this.leftWing.zRot = -0.9F;       // wings wrap around body
            this.rightWing.zRot = 0.9F;
            this.leftWing.xRot = 0.7F;
            this.rightWing.xRot = -0.7F;
            this.leftShoulder.zRot = 0.0F;
            this.rightShoulder.zRot = 0.0F;
            this.leftForearm.zRot = 0.0F;
            this.rightForearm.zRot = 0.0F;
            this.leftLeg.xRot = 0.0F;        // legs grip perch
            this.rightLeg.xRot = 0.0F;
            this.tail.xRot = 0.3F;
        } else if (swimming) {
            // CRON-COMPLETIONIST-17: Added vertical bob synchronized with row cycle.
            // CRON-83: Reset bodyChest.xRot — closes the stale-state bug.
            this.bodyChest.xRot = 0.0F;
            float row = ageInTicks * 0.8F;
            float bob = (float) Math.sin(row * 0.5F) * 0.1F;
            this.root.xRot = -0.2F;
            this.root.y = -2.0F + bob;
            this.head.xRot = -0.3F;
            // Wings row alternately (like butterfly stroke) — row already declared above
            this.leftWing.zRot = -0.3F + (float) Math.sin(row) * 0.6F;
            this.rightWing.zRot = 0.3F - (float) Math.sin(row) * 0.6F;
            this.leftShoulder.zRot = -(float) Math.sin(row) * 0.3F;
            this.rightShoulder.zRot = (float) Math.sin(row) * 0.3F;
            this.leftForearm.zRot = -(float) Math.sin(row) * 0.15F;
            this.rightForearm.zRot = (float) Math.sin(row) * 0.15F;
            // Legs paddle behind
            this.leftLeg.xRot = (float) Math.cos(row) * 0.4F;
            this.rightLeg.xRot = (float) Math.cos(row + Math.PI) * 0.4F;
        }

        // ── CRON-COMPLETIONIST-13: Perched stance via DATA_POSE ──
        boolean perched = entity.getSpiritPose() == SpiritBeastEntity.POSE_PERCHING
                && entity.onGround();

        // ── flap intensity : faster flap when moving ─────────────────────
        float flapAmp = 0.4F + limbSwingAmount * 0.8F;
        float flap = (float) Math.sin(ageInTicks * 0.6F) * flapAmp;

        // CRON-19: Wing/flight branches now skip when resting/swimming, so those
        // poses' wing rotations are not overwritten by glide/flap.
        if (!resting && !swimming) {
        if (sprinting) {
            // ── CRON-COMPLETIONIST-17: POSE_SPRINTING — fast diving stoop ──
            // CRON-83: Reset bodyChest.xRot — closes the stale-state bug.
            this.bodyChest.xRot = 0.0F;
            this.root.xRot = 0.4F;                    // body pitches steeply down
            this.root.y = (float) Math.sin(ageInTicks * 0.15F) * 0.05F;
            // Wings swept back tight (minimal drag)
            this.leftWing.zRot = -0.15F;
            this.rightWing.zRot = 0.15F;
            this.leftWing.xRot = 0.4F;
            this.rightWing.xRot = -0.4F;
            this.leftShoulder.zRot = 0.0F;
            this.rightShoulder.zRot = 0.0F;
            this.leftForearm.zRot = 0.0F;
            this.rightForearm.zRot = 0.0F;
            // Legs tucked tight
            this.leftLeg.xRot = -0.8F;
            this.rightLeg.xRot = -0.8F;
            // Head forward, beak aimed
            this.head.xRot = 0.2F;
        } else if (perched) {
            // PERCHED : wings fold against body, legs straight down
            // CRON-83: Reset bodyChest.xRot — closes the stale-state bug.
            this.bodyChest.xRot = 0.0F;
            this.leftWing.zRot = -0.7F;   // wings fold flat
            this.rightWing.zRot = 0.7F;
            this.leftWing.xRot = 0.6F;    // wings tuck down
            this.rightWing.xRot = -0.6F;
            this.leftShoulder.zRot = 0.0F;
            this.rightShoulder.zRot = 0.0F;
            this.leftForearm.zRot = 0.0F;
            this.rightForearm.zRot = 0.0F;
            // legs stand straight
            this.leftLeg.xRot = 0.0F;
            this.rightLeg.xRot = 0.0F;
        } else if (limbSwingAmount < 0.1F) {
            // GLIDE : wings hold flat with slow rise-fall
            // CRON-83: Reset bodyChest.xRot — closes the stale-state bug.
            this.bodyChest.xRot = 0.0F;
            float glide = (float) Math.sin(ageInTicks * 0.15F) * 0.15F;
            this.leftWing.zRot = -0.1F + glide;
            this.rightWing.zRot = 0.1F - glide;
            this.leftShoulder.zRot = 0.0F;
            this.rightShoulder.zRot = 0.0F;
            this.leftForearm.zRot = 0.0F;
            this.rightForearm.zRot = 0.0F;
            // legs hang loose on glide
            this.leftLeg.xRot = 0.0F;
            this.rightLeg.xRot = 0.0F;
        } else {
            // FLAP : full oscillation, elbow flexes on the downstroke
            this.leftWing.zRot = flap;
            this.rightWing.zRot = -flap;
            // CRON-COMPLETIONIST-59: Spine flex on flap cycle
            this.bodyChest.xRot = (float) Math.sin(ageInTicks * 0.6F) * 0.08F * limbSwingAmount;
            float downstroke = (float) Math.max(0.0F, Math.sin(ageInTicks * 0.6F));
            float elbow = downstroke * 0.3F * limbSwingAmount;
            this.leftShoulder.zRot = -elbow;
            this.rightShoulder.zRot = elbow;
            this.leftForearm.zRot = -elbow * 0.5F;
            this.rightForearm.zRot = elbow * 0.5F;
            // CRON-COMPLETIONIST-65: Body pitch on flap downstroke.
            // Real birds pitch body UP on the power stroke (downstroke) and DIP on recovery.
            // sin > 0 = downstroke = pitch up. sin < 0 = recovery = pitch down.
            this.root.xRot = -(float) Math.sin(ageInTicks * 0.6F) * 0.12F * limbSwingAmount;
            // legs tuck up when flapping hard
            this.leftLeg.xRot = -0.4F * limbSwingAmount;
            this.rightLeg.xRot = -0.4F * limbSwingAmount;
        }
        } // end wing/flight guard (!resting && !swimming)

        // ── CRON-COMPLETIONIST-21: banking — SKIPS during death to fix corpse sway bug ──
        if (entity.deathTime <= 0) {
            this.root.zRot = (float) Math.sin(ageInTicks * 0.1F) * 0.15F;
        }

        // ── tail fan sway ────────────────────────────────────────────────
        this.tail.yRot = (float) Math.sin(ageInTicks * 0.3F) * 0.2F;

        // ── attack : hawk talon-strikes (wings sweep, legs extend) ─────
        float atk = entity.attackAnim;
        if (atk > 0.0F) {
            float strike = (float) Math.sin(atk * Math.PI);
            // wings sweep down and forward (diving strike)
            this.leftWing.zRot = -0.1F - strike * 0.8F;
            this.rightWing.zRot = 0.1F + strike * 0.8F;
            // elbows tuck (wings fold partially)
            this.leftShoulder.zRot = -strike * 0.4F;
            this.rightShoulder.zRot = strike * 0.4F;
            this.leftForearm.zRot = -strike * 0.2F;
            this.rightForearm.zRot = strike * 0.2F;
            // body pitches down (stoop)
            this.root.xRot = strike * 0.5F;
            // head tilts down, beak forward
            this.head.xRot = strike * 0.4F;
            // legs extend for talon grab
            this.leftLeg.xRot = strike * 1.2F;
            this.rightLeg.xRot = strike * 1.2F;
        }

        // ── death : wings fold tight, body drops ─────────────────────
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 8.0F, 1.0F); // 0→1 over 0.4s (visible before fade)
            float collapse = t * t;
            // CRON-83: Reset bodyChest.xRot — closes the stale-state bug where
            // the flap-cycle heave would persist into the death animation.
            this.bodyChest.xRot = 0.0F;
            // body pitches forward and rolls
            this.root.xRot = collapse * 0.5F;
            this.root.zRot = collapse * -0.4F;
            // head droops
            this.head.xRot = collapse * 0.7F;
            this.head.zRot = collapse * 0.3F;
            // wings fold flat against body
            this.leftWing.zRot = 0.0F - collapse * 0.1F;
            this.rightWing.zRot = 0.0F + collapse * 0.1F;
            this.leftWing.xRot = collapse * 0.8F;   // wings fold down
            this.rightWing.xRot = -collapse * 0.8F;
            // tail drops
            this.tail.xRot = collapse * 0.5F;
            // legs go limp
            this.leftLeg.xRot = collapse * 0.3F;
            this.rightLeg.xRot = collapse * 0.3F;
        }
    }
}
