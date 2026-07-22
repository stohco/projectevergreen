package dev.ergenverse.client.render;

import dev.ergenverse.entity.FlyingSwordProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * FlyingSwordProjectileRenderer — minimal renderer for the flying sword projectile.
 *
 * <p>The projectile's visual is entirely its particle trail (Crit + Sweep + Enchant
 * particles spawned in tick()). This renderer is a no-op shell — it exists only
 * because Minecraft requires a registered renderer for every entity type or the
 * client crashes on spawn. getTextureLocation returns a placeholder; render()
 * does nothing (the default EntityRenderer.render draws nothing).
 *
 * <p>Self-critique: A real flying sword should render as a 3D blade spinning
 * around its long axis, with a metal blade + guard + handle. This is invisible
 * except for particles. The cron job's completionist mandate should replace this
 * with a proper spinning-sword model renderer in a future pass.
 */
public class FlyingSwordProjectileRenderer extends EntityRenderer<FlyingSwordProjectileEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("ergenverse", "textures/entity/flying_sword.png");

    public FlyingSwordProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(FlyingSwordProjectileEntity entity) {
        return TEXTURE;
    }
}
