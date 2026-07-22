package dev.ergenverse.client.render;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Per-beast-type spirit beast renderers.
 *
 * <p>Constitution Article I: "If Minecraft conflicts with canon: Minecraft changes. Never canon."
 * A Spirit Wolf IS a wolf. A Spirit Rabbit IS a rabbit. Each beast type uses
 * the most anatomically appropriate vanilla model available, with a custom
 * spirit-beast texture layered on top.
 *
 * <h2>Model mapping</h2>
 * <ul>
 *   <li><b>Spirit Rabbit</b> → RabbitModel — correct anatomy.</li>
 *   <li><b>Spirit Wolf</b> → WolfModel — correct anatomy.</li>
 *   <li><b>Spirit Deer</b> → CowModel — quadruped, closest vanilla body shape to a deer.</li>
 *   <li><b>Spirit Hawk</b> → ParrotModel — avian, wings, perched stance.</li>
 *   <li><b>Fire Beast</b> → WolfModel — predator quadruped.</li>
 *   <li><b>Stone Back Boar</b> → PigModel — stocky quadruped body.</li>
 * </ul>
 *
 * <p>Prior bug: all beast types used WolfModel in the constructor (the old
 * single SpiritBeastRenderer). Now each entity type gets its own renderer
 * with the correct model. Also fixes: FIRE_BEAST and STONE_BACK_BOAR had
 * NO renderer registered at all (would have been invisible).
 *
 * <p>Scale: spirit beasts render at vanilla model scale (1.0x). A Spirit Wolf
 * is the same physical size as a mortal wolf — it differs in texture
 * (spiritual markings) and cultivation stats, not body size. This matches
 * canon: a Spirit Wolf looks like a wolf until it moves or attacks.
 */
public final class SpiritBeastRenderers {

    private SpiritBeastRenderers() {}

    // ─── Spirit Rabbit ──────────────────────────────────────────────

    public static class RabbitRenderer extends MobRenderer<SpiritBeastEntity, EntityModel<SpiritBeastEntity>> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_rabbit.png");

        @SuppressWarnings("unchecked")
        public RabbitRenderer(EntityRendererProvider.Context context) {
            super(context, (EntityModel<SpiritBeastEntity>) (Object)
                    new net.minecraft.client.model.RabbitModel(context.bakeLayer(ModelLayers.RABBIT)), 0.3F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ─── Spirit Wolf ────────────────────────────────────────────────

    public static class WolfRenderer extends MobRenderer<SpiritBeastEntity, EntityModel<SpiritBeastEntity>> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_wolf.png");

        @SuppressWarnings("unchecked")
        public WolfRenderer(EntityRendererProvider.Context context) {
            super(context, (EntityModel<SpiritBeastEntity>) (Object)
                    new net.minecraft.client.model.WolfModel(context.bakeLayer(ModelLayers.WOLF)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ─── Spirit Deer (uses CowModel — closest quadruped body) ───────

    public static class DeerRenderer extends MobRenderer<SpiritBeastEntity, EntityModel<SpiritBeastEntity>> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_deer.png");

        @SuppressWarnings("unchecked")
        public DeerRenderer(EntityRendererProvider.Context context) {
            super(context, (EntityModel<SpiritBeastEntity>) (Object)
                    new net.minecraft.client.model.CowModel(context.bakeLayer(ModelLayers.COW)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ─── Spirit Hawk (uses ParrotModel — avian) ─────────────────────

    public static class HawkRenderer extends MobRenderer<SpiritBeastEntity, EntityModel<SpiritBeastEntity>> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_hawk.png");

        @SuppressWarnings("unchecked")
        public HawkRenderer(EntityRendererProvider.Context context) {
            super(context, (EntityModel<SpiritBeastEntity>) (Object)
                    new net.minecraft.client.model.ParrotModel(context.bakeLayer(ModelLayers.PARROT)), 0.3F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ─── Fire Beast (uses WolfModel — predator quadruped) ───────────

    public static class FireBeastRenderer extends MobRenderer<SpiritBeastEntity, EntityModel<SpiritBeastEntity>> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/fire_beast.png");

        @SuppressWarnings("unchecked")
        public FireBeastRenderer(EntityRendererProvider.Context context) {
            super(context, (EntityModel<SpiritBeastEntity>) (Object)
                    new net.minecraft.client.model.WolfModel(context.bakeLayer(ModelLayers.WOLF)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }

    // ─── Stone Back Boar (uses PigModel — stocky quadruped) ─────────

    public static class BoarRenderer extends MobRenderer<SpiritBeastEntity, EntityModel<SpiritBeastEntity>> {
        private static final ResourceLocation TEX =
                new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/stone_back_boar.png");

        @SuppressWarnings("unchecked")
        public BoarRenderer(EntityRendererProvider.Context context) {
            super(context, (EntityModel<SpiritBeastEntity>) (Object)
                    new net.minecraft.client.model.PigModel(context.bakeLayer(ModelLayers.PIG)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(SpiritBeastEntity entity) { return TEX; }
    }
}
