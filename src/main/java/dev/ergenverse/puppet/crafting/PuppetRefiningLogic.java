package dev.ergenverse.puppet.crafting;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Static recipe engine for Puppet Refining (炼傀) — the Puppet Platform.
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md §3.6:
 * <ul>
 *   <li>4 recipes for different puppet tiers: Copper, Silver, Yin-Yang, Ancient Slave.</li>
 *   <li>Each consumes 1 soul fragment + 1 puppet frame material + 1 fuel.</li>
 *   <li>Higher tiers require rarer materials and higher realm.</li>
 *   <li>Karma cost scales with tier — this is a dark art.</li>
 *   <li>Outputs a puppet_flag item with NBT storing puppet tier and power.</li>
 * </ul>
 *
 * <p>Canon: Wang Lin's Immortal Guards — Du Jian (Copper), Silver Thunder Daoist,
 * Silver Ta Shan, Silver Poison Female Corpse, Yi Si Puppet, Ling Dong (Ancient Slave).
 * "Not mere puppets, but transformed cultivators, born from a ruthless and nearly-lost art."
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class PuppetRefiningLogic {
    public static class PuppetRecipe {
        public final String recipeId;
        public final Item soulInput;       // soul fragment type
        public final Item frameMaterial;   // copper_ingot / iron_ingot / etc.
        public final Item outputItem;      // puppet_flag (same item, different NBT tier)
        public final String puppetTier;    // copper / silver / yin_yang / ancient_slave
        public final int puppetPower;      // combat power value stored in NBT
        public final double karmaCost;     // karma added to player (dark art!)
        public final int minRealmOrdinal;  // minimum realm ordinal to refine this tier
        public final String displayName;
        public PuppetRecipe(String recipeId, Item soulInput, Item frameMaterial, Item outputItem,
                           String puppetTier, int puppetPower, double karmaCost,
                           int minRealmOrdinal, String displayName) {
            this.recipeId = recipeId;
            this.soulInput = soulInput;
            this.frameMaterial = frameMaterial;
            this.outputItem = outputItem;
            this.puppetTier = puppetTier;
            this.puppetPower = puppetPower;
            this.karmaCost = karmaCost;
            this.minRealmOrdinal = minRealmOrdinal;
            this.displayName = displayName;
        }
    }

    private static final java.util.Map<String, PuppetRecipe> RECIPES = new java.util.LinkedHashMap<>();

    private static Item getItem(String name) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("ergenverse", name));
        return item != null ? item : Items.AIR;
    }

    private static Item getVanilla(String name) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", name));
        return item != null ? item : Items.AIR;
    }

    public static void bootstrap() {
        Item puppetFlag = getItem("puppet_flag");
        Item soulEssence = getItem("soul_essence");

        // Tier 1: Copper Celestial Guard (铜仙卫)
        // Canon: Du Jian — Wang Lin's first puppet, a copper-rank celestial guard
        addRecipe(new PuppetRecipe("copper_guard",
                getItem("cultivator_soul_fragment"), // cultivator soul
                getVanilla("copper_ingot"),
                puppetFlag, "copper", 50, 0.02, 3,
                "Copper Celestial Guard"));

        // Tier 2: Silver Thunder Daoist (银雷道士)
        // Canon: Silver-rank puppet with thunder abilities
        addRecipe(new PuppetRecipe("silver_thunder",
                getItem("beast_soul_fragment"), // beast soul for power
                getVanilla("iron_ingot"),
                puppetFlag, "silver", 150, 0.05, 4,
                "Silver Thunder Daoist"));

        // Tier 3: Yin-Yang Puppet (阴阳傀)
        // Canon: Higher tier requiring rare materials
        addRecipe(new PuppetRecipe("yin_yang_puppet",
                getItem("ancient_soul_fragment"), // ancient soul
                getItem("thunder_essence"),       // rare material
                puppetFlag, "yin_yang", 400, 0.10, 6,
                "Yin-Yang Puppet"));

        // Tier 4: Ancient Slave (古奴)
        // Canon: The pinnacle puppet — Ling Dong refined by the Emperor Furnace
        addRecipe(new PuppetRecipe("ancient_slave",
                soulEssence,                      // soul essence — rarest soul material
                getItem("ancient_soul_fragment"), // ancient god bone as frame
                puppetFlag, "ancient_slave", 1000, 0.20, 8,
                "Ancient Slave"));

        Ergenverse.LOGGER.info("[Ergenverse] PuppetRefiningLogic: {} recipes bootstrapped.", RECIPES.size());
    }

    private static void addRecipe(PuppetRecipe r) { RECIPES.put(r.recipeId, r); }

    /**
     * Find a matching recipe given the soul item and frame material.
     */
    public static PuppetRecipe findRecipe(Item soulInput, Item frameMaterial) {
        if (soulInput == Items.AIR || frameMaterial == Items.AIR) return null;
        for (PuppetRecipe r : RECIPES.values()) {
            if (soulInput == r.soulInput && frameMaterial == r.frameMaterial) return r;
        }
        return null;
    }

    public static java.util.Collection<PuppetRecipe> allRecipes() { return RECIPES.values(); }
}