package dev.ergenverse.loot;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
import java.util.HashSet;

@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SettlementLootInjector {
    private static final Set<ResourceLocation> INJECTED_TABLES = new HashSet<>();

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();
        if (name.getNamespace().equals(Ergenverse.MOD_ID)
                && name.getPath().startsWith("chests/")) {
            if (INJECTED_TABLES.add(name)) {
                Ergenverse.LOGGER.debug("[SettlementLoot] Injected settlement loot table: {}", name);
            }
        }
    }
}
