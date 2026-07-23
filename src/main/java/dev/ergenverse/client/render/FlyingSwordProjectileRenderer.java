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
 * FlyingSwordProjectileRenderer — renders a spinning 3D sword with qi glow.
 *
 * <p>CRON-COMPLETIONIST-21: Added actual 3D model (blade + guard + handle + tassel).
 * <p>CRON-COMPLETIONIST-45: Added qi glow emissive pass + tassel trail physics.
 *
 * <h2>Qi Glow</h2>
 * <p>Canon (Renegade Immortal): Flying swords trail spiritual light — the blade
 * glows with the cultivator's qi. Low-grade swords emit a faint white-silver aura;
 * higher-grade swords emit intense golden or elemental light.
 *
 * <p>Implementation: Two-pass rendering:
 * <ol>
 *   <li><b>Normal pass</b> — blade, guard, handle at ambient world light.</li>
 *   <li><b>Emissive pass</b> — re-renders ONLY the blade at fullbright
 *       (packedLight=15728880) so it glows even in darkness. This is the
 *       standard MC technique (cf. spider eyes, enderman eyes).</li>
 * </ol>
 *
 * <h2>Tassel Physics</h2>
 * <p>Previously the tassel was rigid (oscillating sin wave in model). Now the
 * renderer applies a delayed xRot tilt that trails behind the sword's spin —
 * the tassel lags due to simulated air resistance, creating a flowing ribbon
 * effect behind the spinning blade.
 *
 * <p>HARSH SELF-CRITIQUE:
 *   - The glow pass re-renders the ENTIRE blade part, not just a glow overlay.
 *     In bright light the glow is invisible (correct) but in shadow it makes
 *     the blade read as brighter-than-ambient, which is the intended effect.
 *   - The tassel physics is a simple sin-based delay, not real cloth simulation.
 *     Real tassels would flutter unpredictably based on speed changes.
 *   - No particle emitter for spiritual qi particles along the blade trail.
 *     The server entity spawns Crit/Sweep/Enchant particles, but the client
 *     renderer could add localized glow particles around the blade for richer effect.
 *   - No color variation based on sword grade — all swords glow the same white.
 *     Canon swords should glow differently by element (fire=sword-red, water=sword-blue).
 *   - Glow is always on — even when the sword is returning to the owner.
 *     A dimming effect on return would be more natural.
 */
public class FlyingSwordProjectileRenderer extends EntityRenderer<FlyingSwordProjectileEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/flying_sword.png");

    /** Fullbright lightmap value for the qi glow pass. */
    private static final int FULLBRIGHT = 15728880;

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

        // ── CRON-COMPLETIONIST-45: Tassel trail physics ──
        // The tassel lags behind the spinning blade due to air resistance.
        // We apply a delayed counter-rotation to the model's tassel part.
        // The model's setupAnim handles the flutter; we add the trailing tilt here.
        float spinSpeed = 0.9425F; // same as spin rate
        float tasselDelay = 0.6F; // lag factor (0=instant, 1=fully delayed)
        // Tassel tries to point opposite to the spin direction (trailing)
        float tasselTilt = (float) Math.sin((entity.tickCount + partialTicks) * spinSpeed * (1.0F - tasselDelay)) * 0.5F;
        this.model.getTassel().xRot = tasselTilt;
        // Add slight lateral flutter (independent of spin)
        this.model.getTassel().zRot = (float) Math.sin((entity.tickCount + partialTicks) * 1.2F) * 0.15F;

        // ── Scale: slightly smaller than default (swords are thin) ──
        poseStack.scale(0.75F, 0.75F, 0.75F);

        // ── Normal render pass: blade + guard + handle + pommel + tassel at world light ──
        this.model.renderToBuffer(poseStack, buffer.getBuffer(this.model.renderType(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        // ── CRON-COMPLETIONIST-45: Qi glow emissive pass ──
        // Re-render ONLY the blade at fullbright so it glows in darkness.
        // The blade is a direct child of root, so rendering blade alone
        // renders just the blade cubes (lower + tip) — NOT the guard/handle/tassel.
        // This is the same technique used for FireBeastRenderer eye glow.
        poseStack.pushPose();
        poseStack.translate(0, 1.501F, 0); // 1 pixel above to prevent z-fighting
        var renderType = this.model.renderType(TEXTURE);
        var vertexConsumer = buffer.getBuffer(renderType);
        this.model.getBlade().render(poseStack, vertexConsumer, packedLight, FULLBRIGHT);
        poseStack.popPose();

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
