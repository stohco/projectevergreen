package dev.ergenverse.client.model;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * SpiritBeastModelLayers — registers all custom beast model LayerDefinitions.
 *
 * <p>Prior to this class, SpiritWolfModel / SpiritHawkModel / SpiritDeerModel /
 * SpiritRabbitModel / SpiritFireBeastModel / StoneBackBoarModel all had
 * {@code createBodyLayer()} methods but were DEAD CODE — never registered in
 * the model layer system, never baked, never referenced by any renderer.
 *
 * <p>Each model layer is a {@link ModelLayerLocation} keyed by
 * {@code "ergenverse:<beast_type>"} with layer name {@code "main"}.
 * The renderer uses the same location to bake the model at init time.
 *
 * <p>Registered via {@code EntityRenderersEvent.RegisterLayerDefinitions} in
 * {@link dev.ergenverse.client.ClientEvents}.
 */
public final class SpiritBeastModelLayers {

    private SpiritBeastModelLayers() {}

    // ── Model layer locations ────────────────────────────────────────────
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
    public static final ModelLayerLocation FIRE_BEAST_EYES =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "fire_beast"), "eyes");
    public static final ModelLayerLocation STONE_BACK_BOAR =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "stone_back_boar"), "main");
    public static final ModelLayerLocation CULTIVATOR_ROBE =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "cultivator"), "main");
    // CRON-COMPLETIONIST-21: Flying sword model layer
    public static final ModelLayerLocation FLYING_SWORD =
            new ModelLayerLocation(new ResourceLocation(Ergenverse.MOD_ID, "flying_sword"), "main");

    // ── Supplier mapping for RegisterLayerDefinitions ────────────────────
    // RegisterLayerDefinitions expects a Map<ModelLayerLocation, Supplier<LayerDefinition>>.
    // We expose a static method to populate that map.

    /**
     * Returns a supplier that creates the LayerDefinition for the given location.
     * Returns null if the location is not one of ours.
     */
    public static Supplier<LayerDefinition> getSupplier(ModelLayerLocation location) {
        if (SPIRIT_RABBIT.equals(location))       return SpiritRabbitModel::createBodyLayer;
        if (SPIRIT_WOLF.equals(location))         return SpiritWolfModel::createBodyLayer;
        if (SPIRIT_DEER.equals(location))         return SpiritDeerModel::createBodyLayer;
        if (SPIRIT_HAWK.equals(location))         return SpiritHawkModel::createBodyLayer;
        if (FIRE_BEAST.equals(location))          return SpiritFireBeastModel::createBodyLayer;
        if (FIRE_BEAST_EYES.equals(location))   return SpiritFireBeastModel::createBodyLayer;
        if (STONE_BACK_BOAR.equals(location))     return StoneBackBoarModel::createBodyLayer;
        if (CULTIVATOR_ROBE.equals(location))     return CultivatorRobeModel::createBodyLayer;
        if (FLYING_SWORD.equals(location))           return FlyingSwordModel::createBodyLayer;
        return null;
    }
}