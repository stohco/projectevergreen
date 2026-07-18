package dev.ergenverse.talisman.crafting;

import net.minecraft.world.item.ItemStack;
import java.util.*;

public class TalismanCraftingLogic {
    public static final String MODE_TALISMAN = "talisman";
    public static final String MODE_JADE_SLIP = "jade_slip";
    public static final String CAT_ATTACK = "attack";
    public static final String CAT_DEFENSE = "defense";
    public static final String CAT_UTILITY = "utility";
    public static final String CAT_HEALING = "healing";
    public static final String CAT_TRANSPORT = "transport";
    public static final String CAT_CONCEALMENT = "concealment";
    public static final String CAT_TECHNIQUE_SLIP = "technique_slip";
    public static final String CAT_MAP_SLIP = "map_slip";
    public static final String CAT_MESSAGE_SLIP = "message_slip";
    public static final String CAT_INHERITANCE_SLIP = "inheritance_slip";
    public static final String CAT_SEAL = "seal";
    public static final String CAT_SUMMON = "summon";
    public record TalismanRecipe(String id, String name, List<String> inputs, ItemStack output, int durationTicks, float successRate, String mode, String category) {
        public static TalismanRecipe empty() { return new TalismanRecipe("empty", "Empty", List.of(), ItemStack.EMPTY, 40, 1.0f, MODE_TALISMAN, CAT_UTILITY); }
    }
    private static final Map<String, TalismanRecipe> RECIPES = new HashMap<>();
    public static TalismanRecipe getRecipe(String id) { return RECIPES.getOrDefault(id, TalismanRecipe.empty()); }
    public static Collection<TalismanRecipe> getAllRecipes() { return RECIPES.values(); }
    public static void registerRecipe(TalismanRecipe r) { RECIPES.put(r.id(), r); }
}
