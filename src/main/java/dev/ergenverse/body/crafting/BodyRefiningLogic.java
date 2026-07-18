package dev.ergenverse.body.crafting;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Static recipe engine for Body Refining (炼体) — the Refining Pool.
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md §3.10:
 * <ul>
 *   <li>5 recipes for different tempering mediums.</li>
 *   <li>Each converts 1 medium into permanent bloodRefinement gain.</li>
 *   <li>HP drain per session scales with recipe.</li>
 *   <li>Realm-gated via minRealmOrdinal.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class BodyRefiningLogic {
    public static class BodyRecipe {
        public final String recipeId;
        public final Item medium;
        public final double bloodRefinementGain; // 0-1 added to bloodRefinement
        public final float hpDrainFraction; // fraction of max HP drained per session
        public final int minRealmOrdinal; // minimum realm ordinal
        public final String displayName;
        public BodyRecipe(String recipeId, Item medium, double bloodRefinementGain,
                          float hpDrainFraction, int minRealmOrdinal, String displayName) {
            this.recipeId = recipeId;
            this.medium = medium;
            this.bloodRefinementGain = bloodRefinementGain;
            this.hpDrainFraction = hpDrainFraction;
            this.minRealmOrdinal = minRealmOrdinal;
            this.displayName = displayName;
        }
    }

    private static final java.util.Map<String, BodyRecipe> RECIPES = new java.util.LinkedHashMap<>();

    private static Item getItem(String name) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("ergenverse", name));
        return item != null ? item : Items.AIR;
    }

    public static void bootstrap() {
        addRecipe(new BodyRecipe("spirit_iron_filings", getItem("spirit_iron_ingot"), 0.005, 0.10f, 2, "Spirit Iron Filings"));
        addRecipe(new BodyRecipe("beast_blood", getItem("spirit_blood"), 0.008, 0.15f, 2, "Beast Blood"));
        addRecipe(new BodyRecipe("ancient_god_bone", getItem("ancient_soul_fragment"), 0.015, 0.20f, 4, "Ancient God Bone Powder"));
        addRecipe(new BodyRecipe("tribulation_essence", getItem("thunder_essence"), 0.025, 0.30f, 5, "Tribulation Lightning Essence"));
        addRecipe(new BodyRecipe("soul_essence_temper", getItem("soul_essence"), 0.010, 0.15f, 3, "Soul Essence Medium"));
        Ergenverse.LOGGER.info("[Ergenverse] BodyRefiningLogic: {} recipes bootstrapped.", RECIPES.size());
    }

    private static void addRecipe(BodyRecipe r) { RECIPES.put(r.recipeId, r); }

    public static BodyRecipe findRecipe(Item medium) {
        if (medium == Items.AIR) return null;
        for (BodyRecipe r : RECIPES.values()) {
            if (medium == r.medium) return r;
        }
        return null;
    }

    public static java.util.Collection<BodyRecipe> allRecipes() { return RECIPES.values(); }
}