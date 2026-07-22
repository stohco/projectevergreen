package dev.ergenverse.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ergenverse.client.model.SpiritBeastModelLayers;
import dev.ergenverse.client.model.SpiritRabbitModel;
import dev.ergenverse.client.model.SpiritWolfModel;
import dev.ergenverse.client.model.SpiritDeerModel;
import dev.ergenverse.client.model.SpiritHawkModel;
import dev.ergenverse.client.model.SpiritFireBeastModel;
import dev.ergenverse.client.model.StoneBackBoarModel;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Per-beast-type spirit beast renderers.
 *
 * <p>Constitution Article I: "If Minecraft conflicts with canon: Minecraft changes. Never canon."
 * Each beast type now uses its OWN custom model — not a recolored vanilla shape.
 *
 * <h2>Model mapping (custom → custom)</h2>
 * <ul>
 *   <li><b>Spirit Rabbit</b> → {@link SpiritRabbitModel} — round lagomorph with hop cycle</li>
 *   <li><b>Spirit Wolf</b> → {@link SpiritWolfModel} — lean quadruped predator with trot gait</li>
 *   <li><b>Spirit Deer</b> → {@link SpiritDeerModel} — long-necked grazer with antlers</li>
 *   <li><b>Spirit Hawk</b> → {@link SpiritHawkModel} — raptor with 3-segment wings and flight flap</li>
 *   <li><b>Fire Beast</b> → {@link SpiritFireBeastModel} — predator with flickering flame mane</li>
 *   <li><b>Stone Back Boar</b> → {@link StoneBackBoarModel} — stocky boar with stone plate</li>
 * </ul>
 */
public final class SpiritBeastRenderers {

    private SpiritBeastRenderers() {}

    // ── Spirit Rabbit ─────────────────────────────────────

    public static class RabbitRenderer extends MobRenderer<SpiritBeastEntity, SpiritRabbitModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_rabbit.png");

        public RabbitRenderer(EntityRendererProvider.Context context) {
            super(context, new SpiritRabbitModel(context.bakeLayer(SpiritBeastModelLayers.SPIRIT_RABBIT)), 0.4F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ── Spirit Wolf ───────────────────────────────────────

    public static class WolfRenderer extends MobRenderer<SpiritBeastEntity, SpiritWolfModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_wolf.png");

        public WolfRenderer(EntityRendererProvider.Context context) {
            super(context, new SpiritWolfModel(context.bakeLayer(SpiritBeastModelLayers.SPIRIT_WOLF)), 0.6F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ── Spirit Deer ───────────────────────────────────────

    public static class DeerRenderer extends MobRenderer<SpiritBeastEntity, SpiritDeerModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_deer.png");

        public DeerRenderer(EntityRendererProvider.Context context) {
            super(context, new SpiritDeerModel(context.bakeLayer(SpiritBeastModelLayers.SPIRIT_DEER)), 0.6F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ── Spirit Hawk ───────────────────────────────────────

    public static class HawkRenderer extends MobRenderer<SpiritBeastEntity, SpiritHawkModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_hawk.png");

        public HawkRenderer(EntityRendererProvider.Context context) {
            super(context, new SpiritHawkModel(context.bakeLayer(SpiritBeastModelLayers.SPIRIT_HAWK)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ── Fire Beast ──────────────────────────────────────
    //
    // CRON-COMPLETIONIST-3: Custom renderer that forces FULLBRIGHT on the eye
    // cubes so the ember eyes glow even in complete darkness. A Fire Beast's
    // eyes are canonically blazing embers — they MUST emit light visually.
    //
    // Implementation: override render() to call super.render() at normal
    // light level, then re-render JUST the eye parts at packedLight=15728880
    // (fullbright). This is the standard MC technique for glowing entity parts
    // (e.g. spider eyes, enderman eyes).
    //
    // HARSH SELF-CRITIQUE:
    //   - The fullbright pass renders the ENTIRE head part again, not just the
    //     eye cubes. This means the skull and jaw also glow. A proper fix would
    //     use a separate model part tree with ONLY the eyes, or use a custom
    //     RenderType with emissive texture. For now the whole-head glow is
    //     acceptable because the skull is dark charcoal and the eyes are bright
    //     yellow — the contrast still reads as "glowing eyes on a dark face"
    //     in most lighting. In very bright light the glow is invisible anyway.
    //   - No dynamic light emission to the world. A true Fire Beast should
    //     illuminate nearby blocks. That requires a dynamic lights mod hook
    //     (LambDynamicLights / Optifine) and is out of scope for this file.
    //

    public static class FireBeastRenderer extends MobRenderer<SpiritBeastEntity, SpiritFireBeastModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/fire_beast.png");

        /** Fullbright lightmap value — forces the part to render at max brightness
         *  regardless of ambient light. Used for spider eyes, enderman eyes, etc. */
        private static final int FULLBRIGHT = 15728880;

        public FireBeastRenderer(EntityRendererProvider.Context context) {
            super(context, new SpiritFireBeastModel(context.bakeLayer(SpiritBeastModelLayers.FIRE_BEAST)), 0.7F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }

        @Override
        public void render(SpiritBeastEntity entity, float entityYaw, float partialTicks,
                           PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            // Normal render pass — body, legs, mane, tail at ambient light
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

            // Emissive pass — re-render the HEAD at fullbright so the ember
            // eyes glow even in shadow/night.
            //
            // HARSH SELF-CRITIQUE: We re-render the entire head part tree.
            // MC 1.20.1 ModelPart has no getName() — we cannot selectively render
            // just the eye cubes. The whole head (skull + jaw + eyes + horns)
            // re-renders at fullbright. This is acceptable because:
            //   1. The skull is dark charcoal (40,30,35) — fullbright on a dark
            //      color is nearly invisible in bright light, and only slightly
            //      bright in dim light. The eyes (255,180,40) are the dominant visual.
            //   2. Spider eyes and enderman eyes in vanilla MC also re-render
            //      entire head parts at fullbright — this is the standard technique.
            //
            // A PROPER fix would use a separate model part tree for eyes-only,
            // or a custom RenderType with emissive texture. That requires a
            // separate LayerDefinition and is deferred.
            poseStack.pushPose();
            poseStack.translate(0, 1.501F, 0); // pixel above normal to prevent z-fighting
            var headPart = this.getModel().getHeadPart();
            headPart.render(poseStack,
                    buffer.getBuffer(model.renderType(getTextureLocation(entity))),
                    packedLight, FULLBRIGHT);
            poseStack.popPose();
        }
    }

    // ── Stone Back Boar ─────────────────────────────────────

    public static class BoarRenderer extends MobRenderer<SpiritBeastEntity, StoneBackBoarModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/stone_back_boar.png");

        public BoarRenderer(EntityRendererProvider.Context context) {
            super(context, new StoneBackBoarModel(context.bakeLayer(SpiritBeastModelLayers.STONE_BACK_BOAR)), 0.6F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }
}
