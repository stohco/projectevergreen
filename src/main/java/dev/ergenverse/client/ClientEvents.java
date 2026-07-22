package dev.ergenverse.client;

import dev.ergenverse.client.render.EntityCultivatorRenderer;
import dev.ergenverse.client.render.MosquitoSwarmRenderer;
import dev.ergenverse.client.render.SpiritBeastRenderers;
import dev.ergenverse.client.screen.AlchemyFurnaceScreen;
import dev.ergenverse.client.screen.TalismanDeskScreen;
import dev.ergenverse.client.screen.FormationPlatformScreen;
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
 */
@Mod.EventBusSubscriber(modid = "ergenverse", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EREntityTypes.MOSQUITO_SWARM.get(), MosquitoSwarmRenderer::new);
        // Register the polymorphic cultivator renderer. v1 uses the vanilla
        // humanoid model; v2 will switch textures based on character_id.
        event.registerEntityRenderer(EREntityTypes.CULTIVATOR.get(), EntityCultivatorRenderer::new);

        // ── Spirit Beast renderers — each beast type uses its own renderer
        //    with the correct anatomical model (prior bug: all used WolfModel).
        //    Article I: a Spirit Rabbit IS a rabbit; it uses RabbitModel.
        event.registerEntityRenderer(EREntityTypes.SPIRIT_RABBIT.get(),    SpiritBeastRenderers.RabbitRenderer::new);
        event.registerEntityRenderer(EREntityTypes.SPIRIT_WOLF.get(),      SpiritBeastRenderers.WolfRenderer::new);
        event.registerEntityRenderer(EREntityTypes.SPIRIT_DEER.get(),      SpiritBeastRenderers.DeerRenderer::new);
        event.registerEntityRenderer(EREntityTypes.SPIRIT_HAWK.get(),      SpiritBeastRenderers.HawkRenderer::new);
        event.registerEntityRenderer(EREntityTypes.FIRE_BEAST.get(),       SpiritBeastRenderers.FireBeastRenderer::new);
        event.registerEntityRenderer(EREntityTypes.STONE_BACK_BOAR.get(),  SpiritBeastRenderers.BoarRenderer::new);
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