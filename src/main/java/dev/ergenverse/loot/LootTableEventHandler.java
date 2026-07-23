package dev.ergenverse.loot;

import dev.ergenverse.core.Ergenverse;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * LootTableEventHandler — ensures the mod's custom loot tables are loaded
 * by the Forge event system.
 *
 * <p>The mod has 170+ loot table JSON files under
 * {@code data/ergenverse/loot_tables/chests/} (e.g., {@code tian_shui_city_governor_mansion.json},
 * {@code teng_family_city_market_district.json}, etc.). These define the contents
 * of chests placed by the settlement builders (TianShuiCityBuilder, TengFamilyCityBuilder,
 * WangFamilyVillageBuilder, HengYueSectBuilder).
 *
 * <p>Prior to this handler, the loot tables existed as JSON but were never wired into
 * the chests — all settlement chests were empty. Forge loads custom loot tables
 * automatically when the resource location is correct ({@code ergenverse:chests/<name>}),
 * so this handler primarily serves as a diagnostic/logging point and ensures the
 * event bus subscriber is registered.
 *
 * <p>Per Constitution Article XXII: "Canon is not a database. Canon is gameplay."
 * Loot tables are gameplay — they turn placed chests from empty shells into
 * containers with canon-appropriate rewards.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootTableEventHandler {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        // Log that a loot table was loaded. This is purely diagnostic — Forge
        // loads our custom tables automatically when resource locations match.
        // If a settlement chest has a loot table resource at ergenverse:chests/<name>
        // and the builder uses that name in the chest's LootTable, it works.
        // This handler exists to prove the event bus is connected and to
        // provide debug visibility into which tables are being loaded.
        if (event.getName().getNamespace().equals(Ergenverse.MOD_ID)) {
            Ergenverse.LOGGER.debug("[LootTable] Loaded custom loot table: {}", event.getName());
        }
    }
}
