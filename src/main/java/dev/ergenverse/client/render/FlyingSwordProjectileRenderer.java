package dev.ergenverse.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ergenverse.client.model.FlyingSwordModel;
import dev.ergenverse.client.model.SpiritBeastModelLayers;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.FlyingSwordProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * FlyingSwordProjectileRenderer — CRON-COMPLETIONIST-21: NOW renders an actual 3D sword.
 *
 * <p>Previously this was a no-op shell — the flying sword was invisible except for particles.
 * Now it uses FlyingSwordModel to render a blade + guard + handle + tassel that spins
 * around its Y axis (flat spin like a compass needle) as it flies.
 *
 * <p>The spinning is achieved via preRenderCallback equivalent — we apply a yRot
 * based on the entity's tick count in the render method before calling the model.
 */
public class FlyingSwordProjectileRenderer extends EntityRenderer<FlyingSwordProjectileEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/flying_sword.png");

    private final FlyingSwordModel model;

    public FlyingSwordProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FlyingSwordModel(context.bakeLayer(SpiritBeastModelLayers.FLYING_SWORD));
    }

    @Override
    public ResourceLocation getTextureLocation(FlyingSwordProjectileEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(FlyingSwordProjectileEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // ── Spinning: rotate the sword around its Y axis based on tick count ──
        // The sword spins at ~3 revolutions per second (20 ticks/second * 3 * 2PI)
        float spinAngle = (entity.tickCount + partialTicks) * 0.9425F; // ~3 rev/sec
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(spinAngle));

        // ── Scale: slightly smaller than default (swords are thin) ──
        poseStack.scale(0.75F, 0.75F, 0.75F);

        // ── Render the model ──
        this.model.renderToBuffer(poseStack, buffer.getBuffer(this.model.renderType(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
