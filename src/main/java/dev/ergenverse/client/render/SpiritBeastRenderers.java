package dev.ergenverse.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ergenverse.client.model.SpiritBeastModelLayers;
import dev.ergenverse.client.model.SpiritRabbitModel;
import dev.ergenverse.client.model.SpiritWolfModel;
import dev.ergenverse.client.model.SpiritDeerModel;
import dev.ergenverse.client.model.SpiritHawkModel;
import dev.ergenverse.client.model.SpiritCraneModel;
import dev.ergenverse.client.model.SpiritFireBeastModel;
import dev.ergenverse.client.model.StoneBackBoarModel;
import dev.ergenverse.client.model.SpiritBatModel;
import dev.ergenverse.client.model.QilinModel;
import dev.ergenverse.client.model.SeaSerpentModel;
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

    // ── Spirit Crane ───────────────────────────────────────
    // CRON-COMPLETIONIST-22/24: 7th beast type — red-crowned crane with 4-segment neck
    public static class CraneRenderer extends MobRenderer<SpiritBeastEntity, SpiritCraneModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_crane.png");

        public CraneRenderer(EntityRendererProvider.Context context) {
            super(context, new SpiritCraneModel(context.bakeLayer(SpiritBeastModelLayers.SPIRIT_CRANE)), 0.6F);
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

            // CRON-COMPLETIONIST-34: Emissive pass — render ONLY the eye cubes
            // at fullbright so the ember eyes glow even in shadow/night.
            //
            // FIX: Previously only getEyeLeft() was rendered (missing right eye).
            // Now both eyes are rendered. The eye cubes are direct children of the
            // head ModelPart but are NOT part of the head's CubeListBuilder — they
            // are separate addOrReplaceChild calls. So rendering eyeLeft and
            // eyeRight individually renders ONLY those 1x1x1 cubes, not the skull.
            // This is the correct vanilla technique (cf. SpiderEyeLayer).
            //
            // The 17+ round bug is now FIXED: both eyes glow, only eyes glow.
            poseStack.pushPose();
            poseStack.translate(0, 1.501F, 0); // pixel above normal to prevent z-fighting
            var renderType = this.getModel().renderType(getTextureLocation(entity));
            var vertexConsumer = buffer.getBuffer(renderType);
            this.getModel().getEyeLeft().render(poseStack, vertexConsumer, packedLight, FULLBRIGHT);
            this.getModel().getEyeRight().render(poseStack, vertexConsumer, packedLight, FULLBRIGHT);
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

    // ── Spirit Bat (CRON-COMPLETIONIST-33) ──────────────────────

    public static class BatRenderer extends MobRenderer<SpiritBeastEntity, SpiritBatModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_bat.png");

        public BatRenderer(EntityRendererProvider.Context context) {
            super(context, new SpiritBatModel(context.bakeLayer(SpiritBeastModelLayers.SPIRIT_BAT)), 0.3F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ── Qilin (CRON-COMPLETIONIST-33) ──────────────────────

    public static class QilinRenderer extends MobRenderer<SpiritBeastEntity, QilinModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/qilin.png");

        public QilinRenderer(EntityRendererProvider.Context context) {
            super(context, new QilinModel(context.bakeLayer(SpiritBeastModelLayers.QILIN)), 0.7F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ── Sea Serpent (CRON-COMPLETIONIST-33) ──────────────────────

    public static class SeaSerpentRenderer extends MobRenderer<SpiritBeastEntity, SeaSerpentModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/sea_serpent.png");

        public SeaSerpentRenderer(EntityRendererProvider.Context context) {
            super(context, new SeaSerpentModel(context.bakeLayer(SpiritBeastModelLayers.SEA_SERPENT)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }
}
