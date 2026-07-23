package dev.ergenverse.client;

import dev.ergenverse.client.model.SpiritBeastModelLayers;
import dev.ergenverse.client.render.EntityCultivatorRenderer;
import dev.ergenverse.client.render.MosquitoSwarmRenderer;
import dev.ergenverse.client.render.SpiritBeastRenderers;
import dev.ergenverse.client.screen.AlchemyFurnaceScreen;
import dev.ergenverse.entity.EREntityTypes;
import dev.ergenverse.screen.ErgenverseMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * ClientEvents — client-only event subscribers for the Ergenverse mod.
 *
 * <p>Registered on the {@link Mod.EventBusSubscriber.Bus#MOD} bus so that
 * renderer and screen registration fires during the mod-loading phase
 * (before the game enters the main loop).
 *
 * <p>v3 change: Added {@link #registerLayerDefinitions} to bake all custom
 * beast model LayerDefinitions. Without this, the custom models
 * (SpiritWolfModel, SpiritHawkModel, etc.) were dead code — they existed
 * on disk but were never registered in the model layer system, never baked,
 * and never used by any renderer. The renderers previously used VANILLA models
 * (RabbitModel, WolfModel, CowModel, ParrotModel, PigModel) which violated
 * the user's demand for custom anatomy, not recolored vanilla shapes.
 */
@Mod.EventBusSubscriber(modid = "ergenverse", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    /**
     * Register all custom model LayerDefinitions so they are baked at model-init.
     *
     * <p>Without this event handler, calling {@code context.bakeLayer(SOME_LOCATION)}
     * in a renderer would crash with "Model layer not found". Each custom model's
     * {@code createBodyLayer()} is registered here as a Supplier.
     */
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SpiritBeastModelLayers.SPIRIT_RABBIT,   SpiritBeastModelLayers.getSupplier(SpiritBeastModelLayers.SPIRIT_RABBIT));
        event.registerLayerDefinition(SpiritBeastModelLayers.SPIRIT_WOLF,     SpiritBeastModelLayers.getSupplier(SpiritBeastModelLayers.SPIRIT_WOLF));
        event.registerLayerDefinition(SpiritBeastModelLayers.SPIRIT_DEER,     SpiritBeastModelLayers.getSupplier(SpiritBeastModelLayers.SPIRIT_DEER));
        event.registerLayerDefinition(SpiritBeastModelLayers.SPIRIT_HAWK,     SpiritBeastModelLayers.getSupplier(SpiritBeastModelLayers.SPIRIT_HAWK));
        event.registerLayerDefinition(SpiritBeastModelLayers.FIRE_BEAST,      SpiritBeastModelLayers.getSupplier(SpiritBeastModelLayers.FIRE_BEAST));
        // Eye-only layer for fire beast emissive rendering (CRON-COMPLETIONIST-4).
        // The main model renders at ambient light; this eye-only model renders
        // at FULLBRIGHT so only the eyes glow, not the skull/jaw/horns.
        event.registerLayerDefinition(SpiritBeastModelLayers.FIRE_BEAST_EYES, SpiritBeastModelLayers.getSupplier(SpiritBeastModelLayers.FIRE_BEAST_EYES));
        event.registerLayerDefinition(SpiritBeastModelLayers.STONE_BACK_BOAR, SpiritBeastModelLayers.getSupplier(SpiritBeastModelLayers.STONE_BACK_BOAR));
        event.registerLayerDefinition(SpiritBeastModelLayers.CULTIVATOR_ROBE, SpiritBeastModelLayers.getSupplier(SpiritBeastModelLayers.CULTIVATOR_ROBE));
        event.registerLayerDefinition(SpiritBeastModelLayers.SPIRIT_CRANE, SpiritBeastModelLayers.getSupplier(SpiritBeastModelLayers.SPIRIT_CRANE));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EREntityTypes.MOSQUITO_SWARM.get(), MosquitoSwarmRenderer::new);
        // Cultivator renderer uses CultivatorRobeModel (custom) not vanilla HumanoidModel.
        event.registerEntityRenderer(EREntityTypes.CULTIVATOR.get(), EntityCultivatorRenderer::new);

        // ── Spirit Beast renderers — each uses its OWN custom model.
        //    NO vanilla models. A Spirit Wolf uses SpiritWolfModel, not WolfModel.
        //    Constitution Article I: "If Minecraft conflicts with canon: Minecraft changes."
        event.registerEntityRenderer(EREntityTypes.SPIRIT_RABBIT.get(),    SpiritBeastRenderers.RabbitRenderer::new);
        event.registerEntityRenderer(EREntityTypes.SPIRIT_WOLF.get(),      SpiritBeastRenderers.WolfRenderer::new);
        event.registerEntityRenderer(EREntityTypes.SPIRIT_DEER.get(),      SpiritBeastRenderers.DeerRenderer::new);
        event.registerEntityRenderer(EREntityTypes.SPIRIT_HAWK.get(),      SpiritBeastRenderers.HawkRenderer::new);
        event.registerEntityRenderer(EREntityTypes.FIRE_BEAST.get(),       SpiritBeastRenderers.FireBeastRenderer::new);
        event.registerEntityRenderer(EREntityTypes.STONE_BACK_BOAR.get(),  SpiritBeastRenderers.BoarRenderer::new);
        // CRON-COMPLETIONIST-24: Spirit crane renderer (7th beast type)
        event.registerEntityRenderer(EREntityTypes.SPIRIT_CRANE.get(),      SpiritBeastRenderers.CraneRenderer::new);

        // Flying sword projectile (launched by FlyingSwordItem). Invisible except
        // for its particle trail — the renderer is a no-op shell.
        event.registerEntityRenderer(dev.ergenverse.entity.projectile.ModProjectiles.FLYING_SWORD.get(),
                dev.ergenverse.client.render.FlyingSwordProjectileRenderer::new);
    }

    /**
     * Register the Alchemy Furnace screen against its menu type.
     *
     * <p>{@link MenuScreens#register} is NOT thread-safe — it must be
     * enqueued via {@link FMLClientSetupEvent#enqueueWork} so it runs on
     * the main thread after the mod-loading phase.
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ErgenverseMenus.ALCHEMY_FURNACE.get(),
                    AlchemyFurnaceScreen::new);
        });
    }
}