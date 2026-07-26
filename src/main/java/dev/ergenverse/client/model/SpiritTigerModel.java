package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/beast/spirit_tiger.png  SIZE: 64x64
// v2 — CRON-COMPLETIONIST-83: added body CubeDeformation
/*
 * SpiritTigerModel — barrel-chested apex land predator.
 *
 * Canon (Renegade Immortal): spirit tigers are ferocious beasts found in the
 * mountains and forests near Wang Family Village and beyond. They are
 * solitary ambush predators with immense physical power. In cultivation
 * terms, they are low-to-mid tier beasts.
 *
 * REAL TIGER (Panthera tigris) anatomy:
 *   - Body: LARGE, barrel-chested (deep belly, wide ribcage). Stockier than wolf.
 *     The chest is the deepest part — front-heavy.
 *   - Head: broad, rounded skull with wide cheekbones. Short rostrum (snout)
 *     compared to wolves. Small rounded ears (not pointed like wolf).
 *   - Neck: VERY short and thick — almost invisible, unlike wolf's longer neck.
 *   - Legs: thick and muscular. Front paws are notably larger than rear.
 *     Paws are broad with visible toe separation.
 *   - Tail: VERY long (almost body-length), thick at the base, tapered.
 *     Used for balance during turns. Carried low when walking, up when running.
 *   - Stripes: orange-to-white gradient with black stripes — texture-driven.
 *   - Size: ~1.0m at shoulder, ~2.5m body length.
 *
 * KEY DIFFERENCES FROM WOLF MODEL:
 *   - Body is wider and deeper (barrel-chested, not lean)
 *   - Head is broader, rounder, with wider cheeks (wolf is lean-snouted)
 *   - Neck is nearly nonexistent (wolf has a visible neck connector)
 *   - Ears are small and rounded (wolf ears are tall and pointed)
 *   - Tail is longer (4 segments vs wolf's 3), carried differently
 *   - Front paws are larger than rear (wolf has equal-sized legs)
 *   - Stance is lower (big cats stalk with belly close to ground)
 *   - No visible fangs at rest (retractable claws, unlike wolf's exposed fangs)
 *
 * IMPROVEMENTS OVER WOLF MODEL (CRON-75 self-critique driven):
 *   - Tapered tail: segments decrease in width (1.0 → 0.8 → 0.6 → 0.4 → 0.2)
 *     instead of wolf's uniform 1.0 width throughout
 *   - Rounded ears: use CubeDeformation for softer ear shape instead of wolf's
 *     sharp box prisms
 *   - Wide cheekbones: explicit cheek box on each side of the skull
 *   - Larger front paws: front thigh/shin wider than rear
 *   - 4-segment tail (vs wolf's 3) for smoother taper and longer hang
 *
 * ANIMATION:
 *   - Walk: slow, deliberate stalk — body LOW, belly close to ground.
 *     Shoulder roll visible (body_chest y-rotates with gait).
 *   - Run: stretched gallop with longer tail extension, body pitches forward.
 *   - Idle: low crouch with breathing, tail tip twitches occasionally.
 *   - POSE_STALKING (unique to tiger): body drops extra low, head forward,
 *     legs wide for slow approach, tail hangs motionless.
 *   - POSE_CHARGING: body explodes forward, huge lunge, jaw opens wide.
 *   - Attack: powerful head snap with jaw opening.
 *   - Death: side collapse (big cats often fall sideways) with final tail flick.
 *
 * HARSH SELF-CRITIQUE:
 *   - Ears are still box-based (CubeDeformation makes them softer but they're
 *     still fundamentally cuboid, not the semi-circular pinna of real tigers).
 *   - No whiskers (vibrissae) — real tigers have long white whiskers on their
 *     muzzle that are critical for hunting. The MC box API limits this to thin
 *     stick-boxes which look wrong at this scale.
 *   - Stripes are texture-only — no geometric differentiation between striped
 *     and unstriped body parts. Real tiger stripes follow anatomical lines.
 *   - Paws don't have visible toes — real tiger paw prints show 5 toes with
 *     claw marks. Our paws are solid rectangular boxes.
 *   - The shoulder roll (body_chest.yRot during walk) is subtle and may not be
 *     visible at normal game distance. Real tigers have a pronounced sway.
 *   - No belly sag animation — real big cats have a visible belly that swings
 *     during locomotion. Our model uses a flat box for the body underside.
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

public class SpiritTigerModel extends HierarchicalModel<SpiritBeastEntity> {

    private final ModelPart root;
    private final ModelPart bodyChest;
    private final ModelPart bodyHip;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart earLeft;
    private final ModelPart earRight;
    private final ModelPart cheekLeft;
    private final ModelPart cheekRight;
    private final ModelPart tailBase;
    private final ModelPart tailMid1;
    private final ModelPart tailMid2;
    private final ModelPart tailTip;
    private final ModelPart frontLeftThigh;
    private final ModelPart frontLeftShin;
    private final ModelPart frontRightThigh;
    private final ModelPart frontRightShin;
    private final ModelPart backLeftThigh;
    private final ModelPart backLeftShin;
    private final ModelPart backRightThigh;
    private final ModelPart backRightShin;
    private final ModelPart eyeLeft;
    private final ModelPart eyeRight;

    public SpiritTigerModel(ModelPart root) {
        this.root = root;
        // CRON-COMPLETIONIST-85: Reparented neck/tail/thighs from root to body_chest/body_hip.
        // Neck attaches to chest (anatomy: neck rises from shoulders/thorax).
        // Tail attaches to hip (anatomy: tail extends from pelvis).
        // Front thighs attach to chest (anatomy: front legs/shoulders).
        // Back thighs attach to hip (anatomy: hind legs/pelvis).
        // This makes spine-flex animation (bodyChest.xRot/bodyHip.xRot) propagate
        // to the neck, tail, and legs — previously they were rigid because they
        // were parented to root, which has no spine-flex animation.
        this.bodyChest = root.getChild("body_chest");
        this.bodyHip = root.getChild("body_hip");
        this.neck = this.bodyChest.getChild("neck");
        // CRON-COMPLETIONIST-78: Fix head/neck hierarchy — head must be child of neck
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.earLeft = this.head.getChild("ear_left");
        this.earRight = this.head.getChild("ear_right");
        this.cheekLeft = this.head.getChild("cheek_left");
        this.cheekRight = this.head.getChild("cheek_right");
        this.tailBase = this.bodyHip.getChild("tail_base");
        this.tailMid1 = this.tailBase.getChild("mid1");
        this.tailMid2 = this.tailMid1.getChild("mid2");
        this.tailTip = this.tailMid2.getChild("tip");
        this.frontLeftThigh = this.bodyChest.getChild("front_left_thigh");
        this.frontLeftShin = this.frontLeftThigh.getChild("shin");
        this.frontRightThigh = this.bodyChest.getChild("front_right_thigh");
        this.frontRightShin = this.frontRightThigh.getChild("shin");
        this.backLeftThigh = this.bodyHip.getChild("back_left_thigh");
        this.backLeftShin = this.backLeftThigh.getChild("shin");
        this.backRightThigh = this.bodyHip.getChild("back_right_thigh");
        this.backRightShin = this.backRightThigh.getChild("shin");
        this.eyeLeft = this.head.getChild("eye_left");
        this.eyeRight = this.head.getChild("eye_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── Tapered tail: segments decrease in width for smooth taper ──
        CubeDeformation softDeform = new CubeDeformation(0.3F);

        // ── body_chest : DEEP barrel chest, wider than wolf ──────────
        // Tiger chest is the widest point of the body. 5x7x6 — deep belly.
        // CRON-COMPLETIONIST-86: Capture bodyChest/bodyHip as local PartDefinition variables
        // so createBodyLayer() (static) can use them for child reparenting (CRON-85 fixup).
        PartDefinition bodyChest = root.addOrReplaceChild("body_chest",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.5F, -3.5F, -5.5F, 5.0F, 7.0F, 5.5F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 5.5F, -3.0F));

        // ── body_hip : slightly narrower rear, overlaps chest by 1px ──────
        PartDefinition bodyHip = root.addOrReplaceChild("body_hip",
                CubeListBuilder.create().texOffs(0, 14)
                        .addBox(-2.0F, -3.0F, -1.0F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 5.0F, 2.5F));

        // ── neck : very short, thick — barely visible (big cat anatomy) ────
        // CRON-COMPLETIONIST-78: Capture neck PartDefinition for head parenting
        // CRON-COMPLETIONIST-85: Reparented from root to bodyChest.
        // Old root-space offset was (0, 3.5, -6.0). body_chest is at (0, 5.5, -3.0).
        // New offset = (0-0, 3.5-5.5, -6.0-(-3.0)) = (0, -2.0, -3.0). Rotation unchanged.
        PartDefinition neckDef = bodyChest.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(20, 0)
                        .addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 2.5F, softDeform),
                PartPose.offsetAndRotation(0.0F, -2.0F, -3.0F, -0.3F, 0.0F, 0.0F));

        // ── head : broad rounded skull + wide cheeks + rounded ears ──────
        PartDefinition head = neckDef.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 22)
                        .addBox(-2.0F, -1.8F, -1.8F, 4.0F, 3.6F, 3.6F, softDeform), // broad skull
                // CRON-COMPLETIONIST-78: offset relative to neck pivot (0,3.5,-6.0):
                // root-space (0,-0.5,-3.5) → relative (0,-4.0,2.5)
                PartPose.offset(0.0F, -4.0F, 2.5F));

        // snout : shorter and wider than wolf's
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-1.2F, 0.0F, -3.0F, 2.4F, 1.5F, 2.4F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 0.8F, -0.8F));

        // nose : broad flat pad
        head.addOrReplaceChild("nose_pad",
                CubeListBuilder.create().texOffs(8, 40)
                        .addBox(-0.8F, 0.0F, 0.0F, 1.6F, 0.6F, 0.6F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 1.1F, -2.4F));

        // cheeks : wide cheekbones — the defining tiger skull feature
        head.addOrReplaceChild("cheek_left",
                CubeListBuilder.create().texOffs(16, 0)
                        .addBox(-0.6F, -1.0F, -0.8F, 1.2F, 2.0F, 1.6F, softDeform),
                PartPose.offsetAndRotation(-2.5F, -0.5F, 0.5F, 0.0F, 0.0F, -0.2F));
        head.addOrReplaceChild("cheek_right",
                CubeListBuilder.create().texOffs(22, 0)
                        .addBox(-0.6F, -1.0F, -0.8F, 1.2F, 2.0F, 1.6F, softDeform),
                PartPose.offsetAndRotation(2.5F, -0.5F, 0.5F, 0.0F, 0.0F, 0.2F));

        // ears : small, rounded (CubeDeformation for softness), NOT pointed
        head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(16, 8)
                        .addBox(-0.8F, -1.0F, -0.6F, 1.6F, 1.2F, 1.2F, new CubeDeformation(0.6F)),
                PartPose.offsetAndRotation(-1.5F, -2.0F, 0.0F, 0.0F, 0.0F, -0.15F));
        head.addOrReplaceChild("ear_right",
                CubeListBuilder.create().texOffs(20, 8)
                        .addBox(-0.8F, -1.0F, -0.6F, 1.6F, 1.2F, 1.2F, new CubeDeformation(0.6F)),
                PartPose.offsetAndRotation(1.5F, -2.0F, 0.0F, 0.0F, 0.0F, 0.15F));

        // eyes : separate cubes for targeted emissive glow
        head.addOrReplaceChild("eye_left",
                CubeListBuilder.create().texOffs(44, 0)
                        .addBox(-0.6F, -0.6F, -1.81F, 1.2F, 1.2F, 0.6F),
                PartPose.offset(-0.8F, 0.2F, 0.0F));
        head.addOrReplaceChild("eye_right",
                CubeListBuilder.create().texOffs(44, 4)
                        .addBox(-0.6F, -0.6F, -1.81F, 1.2F, 1.2F, 0.6F),
                PartPose.offset(0.8F, 0.2F, 0.0F));

        // ── tail : 4-segment TAPERED chain (improvement over wolf's 3 uniform) ─
        // Widths: 1.0 → 0.8 → 0.6 → 0.4 → 0.2 (visible taper)
        // Tiger tail is very long and muscular, used for balance during turns.
        // CRON-COMPLETIONIST-85: Reparented from root to bodyHip.
        // Old root-space offset was (0, 3.5, 5.5). body_hip is at (0, 5.0, 2.5).
        // New offset = (0-0, 3.5-5.0, 5.5-2.5) = (0, -1.5, 3.0). Rotation unchanged.
        PartDefinition tailBase = bodyHip.addOrReplaceChild("tail_base",
                CubeListBuilder.create().texOffs(28, 14)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F, softDeform),
                PartPose.offsetAndRotation(0.0F, -1.5F, 3.0F, 0.25F, 0.0F, 0.0F));
        PartDefinition tailMid1 = tailBase.addOrReplaceChild("mid1",
                CubeListBuilder.create().texOffs(28, 20)
                        .addBox(-0.4F, -0.4F, 0.0F, 0.8F, 0.8F, 3.0F, softDeform),
                PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.25F, 0.0F, 0.0F));
        PartDefinition tailMid2 = tailMid1.addOrReplaceChild("mid2",
                CubeListBuilder.create().texOffs(28, 26)
                        .addBox(-0.3F, -0.3F, 0.0F, 0.6F, 0.6F, 3.0F, softDeform),
                PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.2F, 0.0F, 0.0F));
        tailMid2.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(32, 8)
                        .addBox(-0.1F, -0.1F, 0.0F, 0.2F, 0.2F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.15F, 0.0F, 0.0F));

        // ── front legs : LARGER than rear (big cat anatomy) ─────────────
        // Front thigh wider (2.5 wide) vs rear (2.0 wide)
        // CRON-COMPLETIONIST-85: Reparented from root to bodyChest.
        // Old root-space offset was (-2.5, 8.5, -5.0). body_chest at (0, 5.5, -3.0).
        // New offset = (-2.5, 3.0, -2.0).
        bodyChest.addOrReplaceChild("front_left_thigh",
                CubeListBuilder.create().texOffs(10, 42)
                        .addBox(-1.25F, 0.0F, -1.0F, 2.5F, 3.5F, 2.0F),
                PartPose.offset(-2.5F, 3.0F, -2.0F));
        bodyChest.getChild("front_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(10, 52)
                        .addBox(-1.25F, 0.0F, -1.0F, 2.5F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 3.5F, 0.0F));

        bodyChest.addOrReplaceChild("front_right_thigh",
                CubeListBuilder.create().texOffs(20, 42)
                        .addBox(-1.25F, 0.0F, -1.0F, 2.5F, 3.5F, 2.0F),
                PartPose.offset(2.5F, 3.0F, -2.0F));
        bodyChest.getChild("front_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(20, 52)
                        .addBox(-1.25F, 0.0F, -1.0F, 2.5F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 3.5F, 0.0F));

        // ── rear legs : narrower, powerful haunches ────────────────────
        // CRON-COMPLETIONIST-85: Reparented from root to bodyHip.
        // Old root-space offset was (-2.0, 8.5, 4.5). body_hip at (0, 5.0, 2.5).
        // New offset = (-2.0, 3.5, 2.0).
        bodyHip.addOrReplaceChild("back_left_thigh",
                CubeListBuilder.create().texOffs(0, 42)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.5F, 2.0F),
                PartPose.offset(-2.0F, 3.5F, 2.0F));
        bodyHip.getChild("back_left_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(0, 52)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 3.5F, 0.0F));

        // FIX (RE-APPLY-PHASE1): back_right_thigh/shin UVs were duplicates of
        // front_left (both at x=10) — moved to x=30 (free column, tiger tex is 64 wide).
        bodyHip.addOrReplaceChild("back_right_thigh",
                CubeListBuilder.create().texOffs(30, 42)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.5F, 2.0F),
                PartPose.offset(2.0F, 3.5F, 2.0F));
        bodyHip.getChild("back_right_thigh").addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(30, 52)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 3.5F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Expose eye cubes for targeted emissive rendering. */
    public ModelPart getEyeLeft() { return this.eyeLeft; }
    public ModelPart getEyeRight() { return this.eyeRight; }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(SpiritBeastEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        boolean resting = entity.getSpiritPose() == SpiritBeastEntity.POSE_RESTING;
        boolean swimming = entity.getSpiritPose() == SpiritBeastEntity.POSE_SWIMMING;
        boolean sprinting = entity.getSpiritPose() == SpiritBeastEntity.POSE_SPRINTING;
        boolean alert = entity.getSpiritPose() == SpiritBeastEntity.POSE_ALERT;

        // ── head turn (clamped) ──────────────────────────────────────────
        float yaw = netHeadYaw * 0.017453292F;
        float pitch = headPitch * 0.017453292F;
        this.head.yRot = Math.max(-1.0F, Math.min(1.0F, yaw));
        this.head.xRot = Math.max(-0.7F, Math.min(0.7F, pitch));

        if (alert) {
            // ── POSE_ALERT : STALKING CROUCH — tiger drops belly to ground ──
            // CRON-COMPLETIONIST-80: Fixed hip-higher-than-shoulder silhouette.
            // Canon: tigers crouch extremely low when detecting prey, with hindquarters
            // slightly raised above forequarters — the classic "cat ready to pounce"
            // silhouette. The forequarters (chest) drop to ground level while the
            // haunches (hips) remain elevated, coiling the hind legs like springs.
            float breath = (float) Math.sin(ageInTicks * 0.06F) * 0.03F; // near-static
            this.root.y = -3.0F + breath; // belly ON the ground (baseline)
            this.root.xRot = 0.15F; // body pitched forward, weight on front
            // HIP-HIGHER-THAN-SHOULDER: chest drops, hips stay elevated
            this.bodyChest.y = -0.8F; // forequarters lower (belly to ground)
            this.bodyHip.y = -0.2F; // hindquarters 0.6 higher (coiled spring)
            // Front legs splayed wide, bent at shin — wide stalking base
            this.frontLeftThigh.xRot  = 0.3F;
            this.frontRightThigh.xRot = 0.3F;
            this.frontLeftShin.xRot   = 0.6F;
            this.frontRightShin.xRot  = 0.6F;
            // Back legs bent under body — coiled spring for explosive launch
            // Back thighs angle MORE than front because hips are elevated
            this.backLeftThigh.xRot   = 0.7F;
            this.backRightThigh.xRot  = 0.7F;
            this.backLeftShin.xRot    = -0.8F;
            this.backRightShin.xRot   = -0.8F;
            // Head low and forward — targeting prey through grass
            this.head.xRot = 0.5F;
            this.neck.xRot = 0.4F;
            this.neck.yRot = 0.0F;
            // Ears pinned FLAT — tiger ears flatten completely when stalking
            this.earLeft.zRot  = -0.8F;
            this.earRight.zRot = 0.8F;
            this.earLeft.xRot  = -0.3F;
            this.earRight.xRot = -0.3F;
            // Jaw slightly open — tasting the air for scent
            this.jaw.xRot = 0.15F;
            // Tail PERFECTLY STILL — frozen to avoid detection
            this.tailBase.xRot = 0.3F;
            this.tailBase.yRot = 0.0F;
            this.tailMid1.yRot = 0.0F;
            this.tailMid2.yRot = 0.0F;
            this.tailTip.yRot = 0.0F;
            // Body flat — no shoulder roll, no spine flex
            this.bodyChest.xRot = 0.0F;
            this.bodyChest.yRot = 0.0F;
            this.bodyHip.xRot = 0.0F;
            this.bodyHip.yRot = 0.0F;
        } else if (resting) {
            // ── POSE_RESTING : big cat curls up, hind legs under body ──
            float breath = (float) Math.sin(ageInTicks * 0.08F) * 0.1F;
            float earTwitch = (ageInTicks % 80 < 5) ? (float) Math.sin(ageInTicks * 2.0F) * 0.08F : 0.0F;
            // Body lowers with breathing
            this.root.y = -2.0F + breath;
            this.root.xRot = 0.05F;
            // Front legs extend forward (big cats rest with front paws out)
            this.frontLeftThigh.xRot  = -0.6F;
            this.frontRightThigh.xRot = -0.6F;
            this.frontLeftShin.xRot   = 0.8F;
            this.frontRightShin.xRot  = 0.8F;
            // Back legs tuck under body (big cats fold hind legs under)
            this.backLeftThigh.xRot   = 0.8F;
            this.backRightThigh.xRot  = 0.8F;
            this.backLeftShin.xRot    = -1.0F;
            this.backRightShin.xRot   = -1.0F;
            // Head rests on paws with breathing
            this.head.xRot = 0.4F + breath * 0.3F;
            this.neck.xRot = 0.5F + breath * 0.2F;
            // Tail wraps loosely with slow sway
            float tailSway = (float) Math.sin(ageInTicks * 0.1F) * 0.06F;
            this.tailBase.xRot = 0.6F;
            this.tailMid1.xRot = 0.4F;
            this.tailMid2.xRot = 0.2F;
            this.tailTip.xRot = -0.3F;
            this.tailBase.yRot = 0.8F + tailSway;
            // Jaw relaxed, ears flat
            this.jaw.xRot = 0.0F;
            this.earLeft.zRot  = -0.1F + earTwitch;
            this.earRight.zRot = 0.1F - earTwitch;
            this.bodyChest.xRot = 0.0F;
            this.bodyHip.xRot = 0.0F;
        } else if (swimming) {
            // ── POSE_SWIMMING : tiger paddles through water ─────────────
            float paddle = ageInTicks * 1.0F;
            float bob = (float) Math.sin(paddle * 0.5F) * 0.12F;
            this.root.xRot = -0.25F;
            this.root.y = -1.5F + bob;
            this.head.xRot = -0.4F;
            this.neck.xRot = 0.1F;
            // Legs paddle
            this.frontLeftThigh.xRot  = (float) Math.cos(paddle) * 0.7F;
            this.frontRightThigh.xRot = (float) Math.cos(paddle + Math.PI) * 0.7F;
            this.backLeftThigh.xRot   = (float) Math.cos(paddle + Math.PI) * 0.5F;
            this.backRightThigh.xRot  = (float) Math.cos(paddle) * 0.5F;
            this.frontLeftShin.xRot   = -0.2F + Math.abs((float) Math.cos(paddle)) * 0.3F;
            this.frontRightShin.xRot  = -0.2F + Math.abs((float) Math.cos(paddle + Math.PI)) * 0.3F;
            this.backLeftShin.xRot    = -0.2F + Math.abs((float) Math.cos(paddle + Math.PI)) * 0.2F;
            this.backRightShin.xRot   = -0.2F + Math.abs((float) Math.cos(paddle)) * 0.2F;
            // Tail streams behind
            this.tailBase.xRot = 0.5F;
            this.tailBase.yRot = (float) Math.sin(ageInTicks * 0.12F) * 0.08F;
            this.tailMid1.yRot = 0.0F;
            this.tailMid2.yRot = 0.0F;
            this.tailTip.yRot = 0.0F;
            this.jaw.xRot = 0.0F;
            this.earLeft.zRot  = -0.4F;
            this.earRight.zRot = 0.4F;
            this.bodyChest.xRot = 0.0F;
            this.bodyHip.xRot = 0.0F;
        } else if (sprinting) {
            // ── SPRINT : explosive gallop, body stretches forward ──────────
            float sp = limbSwing * 2.0F * 0.6662F;
            float sprintAmp = 1.5F * limbSwingAmount;
            this.root.xRot = -0.2F;
            this.root.y = (float) Math.sin(ageInTicks * 0.15F) * 0.06F;
            // Extended stride
            this.frontLeftThigh.xRot  = (float) Math.cos(sp) * sprintAmp;
            this.frontRightThigh.xRot = (float) Math.cos(sp + Math.PI) * sprintAmp;
            this.backLeftThigh.xRot   = (float) Math.cos(sp + Math.PI) * sprintAmp;
            this.backRightThigh.xRot  = (float) Math.cos(sp) * sprintAmp;
            this.frontLeftShin.xRot  = -0.4F + Math.max(0.0F, (float) Math.cos(sp)) * 0.9F * limbSwingAmount;
            this.frontRightShin.xRot = -0.4F + Math.max(0.0F, (float) Math.cos(sp + Math.PI)) * 0.9F * limbSwingAmount;
            this.backLeftShin.xRot   = -0.4F + Math.max(0.0F, (float) Math.cos(sp + Math.PI)) * 0.9F * limbSwingAmount;
            this.backRightShin.xRot  = -0.4F + Math.max(0.0F, (float) Math.cos(sp)) * 0.9F * limbSwingAmount;
            // Spine flex amplified
            float sprintFlex = (float) Math.sin(sp + Math.PI * 0.5F) * 0.15F * limbSwingAmount;
            this.bodyChest.xRot = sprintFlex;
            this.bodyHip.xRot = -sprintFlex * 0.7F;
            // Head forward, ears pinned, jaw open
            this.head.xRot = -0.25F;
            this.neck.xRot = -0.15F;
            this.earLeft.zRot  = -0.5F;
            this.earRight.zRot = 0.5F;
            this.jaw.xRot = 0.35F;
            // Tail streams behind (extended, minimal sway during sprint)
            this.tailBase.xRot = 0.15F;
            this.tailBase.yRot = 0.0F;
            this.tailMid1.yRot = 0.0F;
            this.tailMid2.yRot = 0.0F;
            this.tailTip.yRot = 0.0F;
        } else {
            // ── WALK : slow, deliberate STALK — body LOW, belly near ground ──
            // Tiger walk is distinct from wolf trot: slower, lower, more fluid.
            // The body stays closer to the ground (big cat stalking).
            boolean running = limbSwingAmount > 0.5F;
            float amp = (running ? 1.0F : 0.6F) * limbSwingAmount;
            float phase = limbSwing * 0.6662F;

            // Root is lower than wolf — big cats walk with belly close to ground
            this.root.y = (float) Math.sin(ageInTicks * 0.08F) * 0.06F - 0.5F;
            this.root.xRot = -0.05F; // slight forward pitch for stalking

            // Diagonal gait (same as wolf but slower amplitude)
            this.frontLeftThigh.xRot  = (float) Math.cos(phase) * amp;
            this.frontRightThigh.xRot = (float) Math.cos(phase + Math.PI) * amp;
            this.backLeftThigh.xRot   = (float) Math.cos(phase + Math.PI) * amp;
            this.backRightThigh.xRot  = (float) Math.cos(phase) * amp;

            // Shin counter-flex
            this.frontLeftShin.xRot  = -0.3F + Math.max(0.0F, (float) Math.cos(phase)) * 0.5F * limbSwingAmount;
            this.frontRightShin.xRot = -0.3F + Math.max(0.0F, (float) Math.cos(phase + Math.PI)) * 0.5F * limbSwingAmount;
            this.backLeftShin.xRot   = -0.3F + Math.max(0.0F, (float) Math.cos(phase + Math.PI)) * 0.5F * limbSwingAmount;
            this.backRightShin.xRot  = -0.3F + Math.max(0.0F, (float) Math.cos(phase)) * 0.5F * limbSwingAmount;

            // Shoulder roll — tiger's barrel chest visibly rolls with stride
            float shoulderRoll = (float) Math.sin(phase + Math.PI * 0.5F) * 0.08F * limbSwingAmount;
            this.bodyChest.yRot = shoulderRoll;
            this.bodyHip.yRot = -shoulderRoll * 0.4F;

            // Spine flex
            float spineFlex = (float) Math.sin(phase + Math.PI * 0.5F) * 0.06F * limbSwingAmount;
            this.bodyChest.xRot = spineFlex;
            this.bodyHip.xRot = -spineFlex * 0.5F;

            // Neck barely moves during walk (very short neck)
            this.neck.xRot = -0.3F + (float) Math.sin(phase) * 0.03F * limbSwingAmount;
            this.neck.yRot = (float) Math.sin(phase * 0.5F) * 0.02F * limbSwingAmount;

            // Tail sway — slower, more deliberate than wolf. Long tail hangs low.
            this.tailBase.yRot = (float) Math.sin(ageInTicks * 0.15F) * 0.25F;
            this.tailMid1.yRot = (float) Math.sin(ageInTicks * 0.15F + 0.3F) * 0.2F;
            this.tailMid2.yRot = (float) Math.sin(ageInTicks * 0.15F + 0.6F) * 0.15F;
            // Tail tip twitch — occasional quick flick
            float tipFlick = (ageInTicks % 100 < 8) ? (float) Math.sin(ageInTicks * 3.0F) * 0.3F : 0.0F;
            this.tailTip.yRot = (float) Math.sin(ageInTicks * 0.15F + 0.9F) * 0.1F + tipFlick;
            this.tailBase.xRot = 0.2F; // tail hangs low during walk

            // ── attack lunge ────────────────────────────────────────────────
            float atk = entity.attackAnim;
            boolean lunging = false;
            if (atk > 0.0F) {
                lunging = true;
                float lunge = (float) Math.sin(atk * Math.PI);
                // Tiger lunge is POWERFUL — whole body surges forward
                this.root.xRot = -lunge * 0.8F;
                this.head.xRot = -lunge * 1.0F; // override
                this.jaw.xRot = lunge * 0.6F;  // override
                this.earLeft.zRot  = -0.7F;
                this.earRight.zRot = 0.7F;
                this.tailBase.xRot = 1.0F;
                this.frontLeftThigh.xRot  += lunge * 0.5F;
                this.frontRightThigh.xRot += lunge * 0.5F;
                this.backLeftThigh.xRot   -= lunge * 0.4F;
                this.backRightThigh.xRot  -= lunge * 0.4F;
            }

            // ── combat stance ──────────────────────────────────────────────
            if (!lunging) {
                boolean combat = entity.getTarget() != null
                        || entity.getSpiritPose() == SpiritBeastEntity.POSE_CHARGING;
                if (combat) {
                    this.head.xRot += 0.3F;
                    this.jaw.xRot = 0.5F;
                    this.earLeft.zRot  = -0.6F;
                    this.earRight.zRot = 0.6F;
                    this.tailBase.xRot = 0.8F;
                } else {
                    this.jaw.xRot = 0.0F;
                    this.earLeft.zRot  = -0.15F;
                    this.earRight.zRot = 0.15F;
                    this.tailBase.xRot = 0.2F;
                }
            }
        }

        // ── Death collapse : big cats fall sideways with final tail flick ──
        if (entity.deathTime > 0) {
            float t = Math.min(entity.deathTime / 10.0F, 1.0F);
            float collapse = t * t;
            // CRON-COMPLETIONIST-85: Reset spine rotations to prevent stale-state leak.
            // Without this, bodyChest.xRot/bodyHip.xRot from the previous pose (walk/sprint
            // spine flex) would persist and propagate to the reparented neck/tail/thighs,
            // causing the death collapse to start from a flexed position instead of neutral.
            this.bodyChest.xRot = 0.0F;
            this.bodyChest.yRot = 0.0F;
            this.bodyHip.xRot = 0.0F;
            this.bodyHip.yRot = 0.0F;
            // Sideways collapse (unlike wolf's forward collapse)
            this.root.xRot = collapse * -0.3F;
            this.root.zRot = collapse * 0.8F; // rolls onto side
            this.head.xRot = collapse * 0.6F;
            this.head.zRot = collapse * 0.4F;
            this.frontLeftThigh.zRot  = -collapse * 0.6F;
            this.frontRightThigh.zRot =  collapse * 0.6F;
            this.backLeftThigh.zRot   = -collapse * 0.5F;
            this.backRightThigh.zRot  = collapse * 0.5F;
            // Tail flicks upward at the moment of death (characteristic big cat death)
            float tailFlick = Math.max(0.0F, 1.0F - t * 2.0F) * 0.5F;
            this.tailBase.xRot = 0.2F + collapse * 0.8F + tailFlick * 0.5F;
            this.tailMid1.xRot = collapse * 0.3F;
            this.tailMid2.xRot = collapse * 0.2F;
            this.tailTip.xRot = tailFlick * 0.8F;
            this.jaw.xRot = collapse * 0.5F;
        }
    }
}
