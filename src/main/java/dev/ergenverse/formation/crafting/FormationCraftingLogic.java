package dev.ergenverse.formation.crafting;
import net.minecraft.world.item.ItemStack;
import java.util.*;
public class FormationCraftingLogic {
    public record FormationRecipe(String id, String name, List<String> inputs, ItemStack output, int durationTicks) {
        public static FormationRecipe empty() { return new FormationRecipe("empty", "Empty", List.of(), ItemStack.EMPTY, 120); }
    }
    private static final Map<String, FormationRecipe> RECIPES = new HashMap<>();
    public static FormationRecipe getRecipe(String id) { return RECIPES.getOrDefault(id, FormationRecipe.empty()); }
    public static Collection<FormationRecipe> getAllRecipes() { return RECIPES.values(); }
    public static void registerRecipe(FormationRecipe r) { RECIPES.put(r.id(), r); }
}
