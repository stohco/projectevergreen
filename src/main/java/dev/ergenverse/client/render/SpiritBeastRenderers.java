package dev.ergenverse.client.render;

import dev.ergenverse.client.model.SpiritBeastModelLayers;
import dev.ergenverse.client.model.SpiritRabbitModel;
import dev.ergenverse.client.model.SpiritWolfModel;
import dev.ergenverse.client.model.SpiritDeerModel;
import dev.ergenverse.client.model.SpiritHawkModel;
import dev.ergenverse.client.model.SpiritFireBeastModel;
import dev.ergenverse.client.model.StoneBackBoarModel;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Per-beast-type spirit beast renderers.
 *
 * <p>Constitution Article I: "If Minecraft conflicts with canon: Minecraft changes. Never canon."
 * Each beast type now uses its OWN custom model — not a recolored vanilla shape.
 * The vanilla-model approach (RabbitModel, WolfModel, CowModel, ParrotModel, PigModel)
 * was deleted because it was exactly the laziness the user demanded we eliminate.
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
 *
 * <p>Each model has its own {@code createBodyLayer()} registered in
 * {@link SpiritBeastModelLayers} and baked at model-init time via
 * {@code EntityRenderersEvent.RegisterLayerDefinitions}.
 *
 * <p>Scale: render scale matches bounding box in {@link dev.ergenverse.entity.EREntityTypes}.
 * Spirit beasts are visually larger than their mortal counterparts to convey
 * their spiritual nature — a Spirit Wolf is notably bigger than a mortal wolf.
 */
public final class SpiritBeastRenderers {

    private SpiritBeastRenderers() {}

    // ─── Spirit Rabbit ──────────────────────────────────────────────

    public static class RabbitRenderer extends MobRenderer<SpiritBeastEntity, SpiritRabbitModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_rabbit.png");

        public RabbitRenderer(EntityRendererProvider.Context context) {
            super(context, new SpiritRabbitModel(context.bakeLayer(SpiritBeastModelLayers.SPIRIT_RABBIT)), 0.4F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ─── Spirit Wolf ────────────────────────────────────────────────

    public static class WolfRenderer extends MobRenderer<SpiritBeastEntity, SpiritWolfModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_wolf.png");

        public WolfRenderer(EntityRendererProvider.Context context) {
            super(context, new SpiritWolfModel(context.bakeLayer(SpiritBeastModelLayers.SPIRIT_WOLF)), 0.6F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ─── Spirit Deer ────────────────────────────────────────────────

    public static class DeerRenderer extends MobRenderer<SpiritBeastEntity, SpiritDeerModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_deer.png");

        public DeerRenderer(EntityRendererProvider.Context context) {
            super(context, new SpiritDeerModel(context.bakeLayer(SpiritBeastModelLayers.SPIRIT_DEER)), 0.6F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ─── Spirit Hawk ────────────────────────────────────────────────

    public static class HawkRenderer extends MobRenderer<SpiritBeastEntity, SpiritHawkModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_hawk.png");

        public HawkRenderer(EntityRendererProvider.Context context) {
            super(context, new SpiritHawkModel(context.bakeLayer(SpiritBeastModelLayers.SPIRIT_HAWK)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ─── Fire Beast ─────────────────────────────────────────────────

    public static class FireBeastRenderer extends MobRenderer<SpiritBeastEntity, SpiritFireBeastModel> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/fire_beast.png");

        public FireBeastRenderer(EntityRendererProvider.Context context) {
            super(context, new SpiritFireBeastModel(context.bakeLayer(SpiritBeastModelLayers.FIRE_BEAST)), 0.7F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ─── Stone Back Boar ────────────────────────────────────────────

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
