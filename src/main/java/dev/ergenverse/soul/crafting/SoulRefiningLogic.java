package dev.ergenverse.soul.crafting;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Static recipe engine for Soul Refining (炼魂) — the demonic art of
 * converting raw soul fragments into refined soul energy stored in soul banners.
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md §3.9:
 * <ul>
 *   <li>6 recipes: different raw soul types yield different soul counts.</li>
 *   <li>Each refine adds karma (demonic art).</li>
 *   <li>Soul banners have tier-based capacity limits.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class SoulRefiningLogic {

    public static class SoulRecipe {
        public final String recipeId;
        public final Item rawSoul;
        public final int soulsAdded;
        public final double karmaCost;

        public SoulRecipe(String recipeId, Item rawSoul, int soulsAdded, double karmaCost) {
            this.recipeId = recipeId;
            this.rawSoul = rawSoul;
            this.soulsAdded = soulsAdded;
            this.karmaCost = karmaCost;
        }
    }

    private static final java.util.Map<String, SoulRecipe> RECIPES = new java.util.LinkedHashMap<>();

    private static Item getItem(String name) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("ergenverse", name));
        return item != null ? item : Items.AIR;
    }

    public static void bootstrap() {
        addRecipe(new SoulRecipe("cultivator_soul", getItem("cultivator_soul_fragment"), 100, 0.01));
        addRecipe(new SoulRecipe("beast_soul", getItem("beast_soul_fragment"), 50, 0.005));
        addRecipe(new SoulRecipe("wandering_soul", getItem("wandering_soul_fragment"), 30, 0.003));
        addRecipe(new SoulRecipe("demonic_soul", getItem("demonic_soul_fragment"), 200, 0.02));
        addRecipe(new SoulRecipe("ancient_soul", getItem("ancient_soul_fragment"), 500, 0.05));
        addRecipe(new SoulRecipe("soul_essence_refine", getItem("soul_essence"), 1000, 0.08));
        Ergenverse.LOGGER.info("[Ergenverse] SoulRefiningLogic: {} recipes bootstrapped.", RECIPES.size());
    }

    private static void addRecipe(SoulRecipe r) { RECIPES.put(r.recipeId, r); }

    public static SoulRecipe findRecipe(Item rawSoul) {
        if (rawSoul == Items.AIR) return null;
        for (SoulRecipe r : RECIPES.values()) {
            if (rawSoul == r.rawSoul) return r;
        }
        return null;
    }

    public static java.util.Collection<SoulRecipe> allRecipes() { return RECIPES.values(); }
}