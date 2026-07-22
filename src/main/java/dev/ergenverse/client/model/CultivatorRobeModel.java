package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/cultivator/default.png  SIZE: 64x64
/*
 * CultivatorRobeModel — humanoid in flowing robes with meditation + cast poses.
 *
 * Extends HumanoidModel<EntityCultivator> so the standard head/body/arm/leg
 * structure and walk animations come from vanilla. This file adds:
 *   - robe_skirt : wide box below the torso that sways when walking
 *   - hair_bun   : small box on top of the head (cultivator topknot)
 *   - sleeve_R/L : inflated arm boxes (wide flowing sleeves) as arm children
 *
 * ANATOMY (added on top of HumanoidModel):
 *   - robe_skirt : 9 x 8 x 6 wide box at y=12 (covers upper legs), sways
 *   - hair_bun   : 4 x 2 x 4 box on top of head (y=-10..-8)
 *   - sleeve_R/L : arm boxes inflated by 0.5 (1 wider all around), child of
 *                  each arm so they inherit arm rotation
 *
 * ANIMATION:
 *   - Walk       : super.setupAnim handles arm-swing-opposite-legs. Robe skirt
 *                  adds robeSkirt.xRot = sin(swing*0.6662)*0.1*swingAmt sway.
 *   - Idle       : subtle breathing body.y = sin(age*0.1)*0.3; robe gentle
 *                  sway robeSkirt.xRot += sin(age*0.07)*0.03; hair bun still.
 *   - Meditate   : setMeditating(true) — arms raised forward+in (hands
 *                  together at chest, zhan zhuang / standing-stake pose),
 *                  head bowed, body slight forward lean, robe still.
 *   - Cast       : setCasting(true) — right arm raised straight up (channeling
 *                  pose), left arm relaxed at side.
 *   - Head turn  : super handles netHeadYaw / headPitch, unless meditating
 *                  (head bowed).
 *
 * USAGE (renderer must call):
 *   CultivatorRobeModel model = ...;
 *   model.setMeditating(entity.isMeditating());  // wire to a synced flag
 *   model.setCasting(entity.isCasting());        // wire to a synced flag
 *
 * HARSH SELF-CRITIQUE:
 *   - Robe is a single rigid box that rotates on xRot — real cloth drapes,
 *     folds, and reacts to wind + leg movement. Mine is a hinged board.
 *     A real flowing robe needs a multi-bone skirt chain or cloth simulation.
 *   - Sleeves are inflated arm boxes — they don't drape or swing independently
 *     of the arm. Real wide sleeves (水袖) have a fabric trail that lags the
 *     arm motion. Mine is rigid.
 *   - Hair bun is a plain cube — real cultivator topknots are wrapped,
 *     sometimes with a hairpin or jade crown. No detail.
 *   - Meditation pose is "standing stake" (zhan zhuang) because the entity
 *     has no sit mechanic. A proper seated meditation (盘腿坐) needs the
 *     entity to actually sit on the ground (entity pose / bounding box change),
 *     not just fold legs in mid-air.
 *   - meditating / casting flags are NOT synced — the renderer must set them
 *     from a synced DataAccessor on EntityCultivator, which does not yet
 *     exist. Until wired, meditation/cast poses never fire.
 *   - No qi aura / spiritual energy visualization on the model itself — that
 *     is handled (or not) by the renderer overlay.
 *   - No facial features — face is a blank head box relying on texture.
 *   - The vanilla HumanoidModel arms are 4 wide; inflated sleeves at 5 wide
 *     clip into the robe skirt when arms are lowered. Needs collision-aware
 *     posing or a custom render pass.
 *   - Texture UVs for robe/bun/sleeves are placed in regions that overlap
 *     vanilla player-skin layout; the cultivator texture MUST be regenerated
 *     to paint robe/bun/sleeves in the new UV regions.
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

    private final ModelPart robeSkirt;
    private final ModelPart hairBun;

    public CultivatorRobeModel(ModelPart root) {
        super(root);
        this.robeSkirt = root.getChild("robe_skirt");
        // CRON-COMPLETIONIST-21: hair bun is now child of head, not root
        this.hairBun = root.getChild("head").getChild("hair_bun");
    }

    public static LayerDefinition createBodyLayer() {
        // Start from the vanilla humanoid mesh — keeps the player-skin UV
        // layout for head/body/arms/legs/hat so existing cultivator textures
        // still partially apply.
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        // ── robe skirt : wide box below the torso ───────────────────────
        root.addOrReplaceChild("robe_skirt",
                CubeListBuilder.create().texOffs(16, 32)
                        .addBox(-4.5F, 0.0F, -3.0F, 9.0F, 8.0F, 6.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        // ── CRON-COMPLETIONIST-21: hair bun — NOW child of head, not root ──
        // Previously hair_bun was a root child, meaning it stayed fixed in space
        // when the cultivator looked up/down. Now it follows head rotation.
        root.getChild("head").addOrReplaceChild("hair_bun",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));

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

    @Override
    public void setupAnim(EntityCultivator entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Let vanilla handle walk cycle, head turn, arm swing, crouching, etc.
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // ── idle breathing ──────────────────────────────────────────────
        this.body.y = (float) Math.sin(ageInTicks * 0.1F) * 0.3F;

        // ── robe skirt sway : follows the walk + idle drift ─────────────
        float walkSway = (float) Math.sin(limbSwing * 0.6662F) * 0.10F * limbSwingAmount;
        float idleSway = (float) Math.sin(ageInTicks * 0.07F) * 0.03F;
        this.robeSkirt.xRot = walkSway + idleSway;
        this.robeSkirt.yRot = (float) Math.sin(ageInTicks * 0.05F) * 0.02F;

        // ── hair bun : barely-perceptible bob with breathing ────────────
        this.hairBun.yRot = (float) Math.sin(ageInTicks * 0.15F) * 0.05F;

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
            // robe settles — no walk sway while meditating
            this.robeSkirt.xRot = idleSway * 0.5F;
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
    }
}
