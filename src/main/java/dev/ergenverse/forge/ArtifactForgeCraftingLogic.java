package dev.ergenverse.forge;

import net.minecraft.world.item.ItemStack;
import java.util.*;

/**
 * ArtifactForgeCraftingLogic — property-based artifact resolution.
 * Minimal version to get build green. Full implementation will use MaterialProperties.
 */
public class ArtifactForgeCraftingLogic {

    // Forge modes (per DESIGN_DECISIONS_FINAL.md)
    public static final String MODE_BLOOD_REFINE = "blood_refine";
    public static final String MODE_SOUL_SEAL = "soul_seal";
    public static final String MODE_DAO_IMPRINT = "dao_imprint";
    public static final String MODE_IMMORTAL_GUARD = "immortal_guard";
    public static final String MODE_POISON = "poison";
    public static final String MODE_REPAIR = "repair";

    public record ForgeRecipe(
            String id,
            String name,
            List<String> inputs,
            ItemStack output,
            int durationTicks,
            String mode
    ) {
        public static ForgeRecipe empty() {
            return new ForgeRecipe("empty", "Empty", List.of(), ItemStack.EMPTY, 100, MODE_BLOOD_REFINE);
        }
    }

    private static final Map<String, ForgeRecipe> RECIPES = new HashMap<>();

    public static ForgeRecipe getRecipe(String recipeId) {
        return RECIPES.getOrDefault(recipeId, ForgeRecipe.empty());
    }

    public static Collection<ForgeRecipe> getAllRecipes() {
        return RECIPES.values();
    }

    public static void registerRecipe(ForgeRecipe recipe) {
        RECIPES.put(recipe.id(), recipe);
    }

    public static String modeDisplayName(String mode) {
        return switch (mode) {
            case MODE_BLOOD_REFINE -> "Blood Refine";
            case MODE_SOUL_SEAL -> "Soul Seal";
            case MODE_DAO_IMPRINT -> "Dao Imprint";
            case MODE_IMMORTAL_GUARD -> "Immortal Guard";
            case MODE_POISON -> "Poison";
            case MODE_REPAIR -> "Repair";
            default -> "Unknown";
        };
    }

    public static List<String> getAllModes() {
        return List.of(MODE_BLOOD_REFINE, MODE_SOUL_SEAL, MODE_DAO_IMPRINT, MODE_IMMORTAL_GUARD, MODE_POISON, MODE_REPAIR);
    }
}
