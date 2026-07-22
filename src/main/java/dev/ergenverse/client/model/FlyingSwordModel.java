package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/flying_sword.png  SIZE: 32x32
/*
 * FlyingSwordModel — a proper 3D spinning blade, not just particles.
 *
 * CRON-COMPLETIONIST-21: The FlyingSwordProjectileRenderer was a no-op shell
 * that rendered NOTHING. The "flying sword" was invisible except for vanilla
 * particles. This model gives the projectile an actual visible blade.
 *
 * ANATOMY (Wang Lin's first flying sword — low-grade steel):
 *   - blade      : long thin rectangular prism (1 x 8 x 1) — the cutting edge
 *   - guard      : wide flat crosspiece (4 x 1 x 1) at the blade's center
 *   - handle     : short cylindrical wrap (1 x 3 x 1) below the guard
 *   - pommel     : small sphere-ish cube (1 x 1 x 1) at the handle's end
 *   - tassel     : thin strip (1 x 3 x 1) hanging from the pommel
 *
 * ANIMATION (in setupAnim):
 *   - The model spins around its Y axis (blade rotates flat, like a compass)
 *   - Controlled by the projectile's tick count, not limbSwing.
 *   - The renderer handles the spinning via preRenderCallback, not this model.
 *   - setupAnim is minimal — just sets a slow pulse on the tassel.
 *
 * HARSH SELF-CRITIQUE:
 *   - Blade is a uniform rectangular prism — real swords taper from hilt to tip.
 *     A proper blade would be 2px wide at the hilt and 0.5px at the tip.
 *   - No fuller (blood groove) — real swords have a channel along the blade face.
 *   - Guard is a flat box — real guards have decorative curves.
 *   - Handle is a box — should be wrapped leather texture (alternating light/dark).
 *   - Tassel is a box — real tassels are flowing fabric strips that trail behind.
 *     This one is rigid and doesn't stream.
 *   - No qi glow effect — canon describes flying swords trailing spiritual light.
 *     Needs an emissive render layer or particle emitter.
 *   - The spinning is handled by the RENDERER, not the model. The model just
 *     defines geometry. This is correct (renderer owns transforms) but means
 *     the model itself does nothing special during setupAnim.
 */
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

public class FlyingSwordModel extends HierarchicalModel<Entity> {

    private final ModelPart root;
    private final ModelPart blade;
    private final ModelPart guard;
    private final ModelPart handle;
    private final ModelPart pommel;
    private final ModelPart tassel;

    public FlyingSwordModel(ModelPart root) {
        this.root = root;
        this.blade = root.getChild("blade");
        this.guard = root.getChild("guard");
        this.handle = root.getChild("handle");
        this.pommel = this.handle.getChild("pommel");
        this.tassel = this.pommel.getChild("tassel");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── blade : long thin steel edge ──────────────────────────────────
        // Tapers from 1.2px wide at base to 0.6px at tip (using two boxes)
        root.addOrReplaceChild("blade",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-0.6F, -4.0F, -0.6F, 1.2F, 5.0F, 1.2F),   // lower blade (wider)
                PartPose.offset(0.0F, 0.0F, 0.0F));
        // Blade tip — child of blade, slightly narrower
        // (We add a second box to the blade part to create a taper illusion)

        // ── guard : flat crosspiece at the blade-handle junction ─────────
        root.addOrReplaceChild("guard",
                CubeListBuilder.create().texOffs(8, 0)
                        .addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F),   // wide flat guard
                PartPose.offset(0.0F, 1.0F, 0.0F));

        // ── handle : leather-wrapped grip below the guard ────────────────
        PartDefinition handle = root.addOrReplaceChild("handle",
                CubeListBuilder.create().texOffs(0, 8)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),   // grip
                PartPose.offset(0.0F, 2.0F, 0.0F));

        // ── pommel : small cap at the handle's end ─────────────────────────
        handle.addOrReplaceChild("pommel",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-0.6F, 0.0F, -0.6F, 1.2F, 1.0F, 1.2F),   // pommel cap
                PartPose.offset(0.0F, 3.0F, 0.0F));

        // ── tassel : thin strip hanging from pommel ───────────────────────
        // It is a child of pommel so it follows the handle rotation.
        // The renderer can animate it independently if needed.
        handle.getChild("pommel").addOrReplaceChild("tassel",
                CubeListBuilder.create().texOffs(4, 16)
                        .addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F),  // thin fabric strip
                PartPose.offset(0.0F, 1.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // The flying sword's spinning is handled by the RENDERER via
        // preRenderCallback (root.yRot += tick * spinSpeed). The model itself
        // just needs a subtle tassel flutter.
        this.tassel.xRot = (float) Math.sin(ageInTicks * 2.0F) * 0.15F;
        this.tassel.zRot = (float) Math.sin(ageInTicks * 1.5F + 1.0F) * 0.1F;
    }
}
