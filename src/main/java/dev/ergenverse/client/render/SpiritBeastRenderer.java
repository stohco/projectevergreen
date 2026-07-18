package dev.ergenverse.client.render;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.SpiritBeastEntity;
import dev.ergenverse.entity.SpiritBeastEntity.BeastType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * SpiritBeastRenderer — renders spirit beasts using PROPER vanilla models.
 *
 * <p>Constitution Article I: "If Minecraft conflicts with canon: Minecraft changes. Never canon."
 * A Spirit Wolf IS a wolf. It uses WolfModel. Not PigModel.
 * A Spirit Rabbit IS a rabbit. It uses RabbitModel.
 */
public class SpiritBeastRenderer extends MobRenderer<SpiritBeastEntity, EntityModel<SpiritBeastEntity>> {

    private static final ResourceLocation WOLF_TEX = 
            new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_wolf.png");
    private static final ResourceLocation RABBIT_TEX = 
            new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_rabbit.png");
    private static final ResourceLocation DEER_TEX = 
            new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_deer.png");
    private static final ResourceLocation FIRE_BEAST_TEX = 
            new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/fire_beast.png");
    private static final ResourceLocation BOAR_TEX = 
            new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/stone_back_boar.png");
    private static final ResourceLocation HAWK_TEX = 
            new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/beast/spirit_hawk.png");

    @SuppressWarnings("unchecked")
    public SpiritBeastRenderer(EntityRendererProvider.Context context) {
        super(context, (EntityModel<SpiritBeastEntity>)(Object) new WolfModel<>(context.bakeLayer(ModelLayers.WOLF)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(SpiritBeastEntity entity) {
        BeastType type = entity.getBeastType();
        if (type == BeastType.RABBIT) return RABBIT_TEX;
        if (type == BeastType.WOLF) return WOLF_TEX;
        if (type == BeastType.DEER) return DEER_TEX;
        if (type == BeastType.HAWK) return HAWK_TEX;
        if (type == BeastType.FIRE_BEAST) return FIRE_BEAST_TEX;
        if (type == BeastType.STONE_BACK_BOAR) return BOAR_TEX;
        return WOLF_TEX; // fallback
    }
}
