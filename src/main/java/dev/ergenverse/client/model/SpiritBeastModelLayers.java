package dev.ergenverse.client.model;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * SpiritBeastModelLayers - registers all custom beast model LayerDefinitions.
 * CRON-COMPLETIONIST-53: resolved git merge conflict (HEAD vs 069074e).
 */
public final class SpiritBeastModelLayers {

    private SpiritBeastModelLayers() {}

    public static final ModelLayerLocation SPIRIT_RABBIT =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "spirit_rabbit"), "main");
    public static final ModelLayerLocation SPIRIT_WOLF =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "spirit_wolf"), "main");
    public static final ModelLayerLocation SPIRIT_DEER =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "spirit_deer"), "main");
    public static final ModelLayerLocation SPIRIT_HAWK =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "spirit_hawk"), "main");
    public static final ModelLayerLocation FIRE_BEAST =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "fire_beast"), "main");
    public static final ModelLayerLocation STONE_BACK_BOAR =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "stone_back_boar"), "main");
    public static final ModelLayerLocation CULTIVATOR_ROBE =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "cultivator"), "main");
    public static final ModelLayerLocation SPIRIT_CRANE =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "spirit_crane"), "main");
    public static final ModelLayerLocation FLYING_SWORD =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "flying_sword"), "main");
    public static final ModelLayerLocation SPIRIT_BAT =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "spirit_bat"), "main");
    public static final ModelLayerLocation QILIN =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "qilin"), "main");
    public static final ModelLayerLocation SEA_SERPENT =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "sea_serpent"), "main");
    public static final ModelLayerLocation SOUL_FISH =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "soul_fish"), "main");

    public static Supplier<LayerDefinition> getSupplier(ModelLayerLocation location) {
        if (SPIRIT_RABBIT.equals(location))       return SpiritRabbitModel::createBodyLayer;
        if (SPIRIT_WOLF.equals(location))         return SpiritWolfModel::createBodyLayer;
        if (SPIRIT_DEER.equals(location))         return SpiritDeerModel::createBodyLayer;
        if (SPIRIT_HAWK.equals(location))         return SpiritHawkModel::createBodyLayer;
        if (FIRE_BEAST.equals(location))          return SpiritFireBeastModel::createBodyLayer;
        if (STONE_BACK_BOAR.equals(location))     return StoneBackBoarModel::createBodyLayer;
        if (CULTIVATOR_ROBE.equals(location))     return CultivatorRobeModel::createBodyLayer;
        if (SPIRIT_CRANE.equals(location))       return SpiritCraneModel::createBodyLayer;
        if (FLYING_SWORD.equals(location))        return FlyingSwordModel::createBodyLayer;
        if (SPIRIT_BAT.equals(location))           return SpiritBatModel::createBodyLayer;
        if (QILIN.equals(location))               return QilinModel::createBodyLayer;
        if (SEA_SERPENT.equals(location))          return SeaSerpentModel::createBodyLayer;
        if (SOUL_FISH.equals(location))            return SoulFishModel::createBodyLayer;
        return null;
    }
}
