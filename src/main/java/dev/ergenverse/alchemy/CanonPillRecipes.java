package dev.ergenverse.alchemy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.*;

/**
 * CanonPillRecipes — the 14 known pill patterns from Renegade Immortal.
 *
 * <p>Constitution Article I: These are canon. They are not "game mechanics."
 * They are the objective alchemical knowledge that exists in the Er Gen universe.
 *
 * <p>Constitution Article VII: "The player does not unlock recipes.
 * The player acquires knowledge." These recipes exist objectively.
 * Whether the player KNOWS them is determined by the KnowledgeEngine.
 *
 * <p>Each recipe specifies exact ingredients (canon-accurate) and produces
 * a deterministic result. Quality varies based on fire, skill, materials.
 *
 * <p>Source: worklog line 433 — "14 known PillPatterns (Qi-Recovery Pill,
 * Purification Pill, Heart-Calming Incense, Meridian-Clearing Powder,
 * Soul-Mending Elixir, Karma-Cleansing Pill, Body-Tempering Pill,
 * Foundation Pill, Nine-Turn Core Pill, Heaven-Defying Pill,
 * Dao Comprehension Pill, Lightning Essence Pill, Fury Pill,
 * Blood-Refinement Pill)."
 */
public final class CanonPillRecipes {

    private CanonPillRecipes() {}

    public record PillRecipe(
            String id,
            String name,
            String nameCn,
            List<String> ingredientIds,   // MaterialRegistry item IDs
            String resultItemId,          // output item
            int minRealm,                  // minimum cultivation realm to brew
            String canonSource             // where this recipe appears in canon
    ) {}

    private static final Map<String, PillRecipe> RECIPES = new LinkedHashMap<>();

    static {
        // 1. Qi-Recovery Pill (回气丹) — most basic pill, restores Qi
        register("qi_recovery_pill", "Qi-Recovery Pill", "回气丹",
                List.of("ergenverse:qi_gathering_grass", "ergenverse:spirit_stone_fragment"),
                "ergenverse:qi_gathering_pill", 0,
                "RI: basic pill known throughout cultivation world");

        // 2. Purification Pill (净化丹) — purifies Qi, removes impurities
        register("purification_pill", "Purification Pill", "净化丹",
                List.of("ergenverse:snow_heart_herb", "ergenverse:spirit_stone_fragment"),
                "ergenverse:purification_pill", 0,
                "RI: used to purify spiritual energy");

        // 3. Heart-Calming Incense (定心香) — calms the mind, prevents Qi deviation
        register("heart_calming_incense", "Heart-Calming Incense", "定心香",
                List.of("ergenverse:heart_calm_lily", "ergenverse:spirit_herb_seed"),
                "ergenverse:qi_gathering_pill", 0, // same item, different use
                "RI: used during meditation to prevent deviation");

        // 4. Meridian-Clearing Powder (通脉散) — clears blocked meridians
        register("meridian_clearing_powder", "Meridian-Clearing Powder", "通脉散",
                List.of("ergenverse:foundation_root_vine", "ergenverse:spirit_stone_fragment"),
                "ergenverse:qi_gathering_pill", 0,
                "RI: used to prepare for Foundation Establishment");

        // 5. Soul-Mending Elixir (补魂丹) — repairs soul damage
        register("soul_mending_elixir", "Soul-Mending Elixir", "补魂丹",
                List.of("ergenverse:soul_nourishing_lotus", "ergenverse:beast_core"),
                "ergenverse:soul_mending_pill", 1,
                "RI: Wang Lin uses these after soul-damaging battles");

        // 6. Karma-Cleansing Pill (洗业丹) — reduces karmic debt
        register("karma_cleansing_pill", "Karma-Cleansing Pill", "洗业丹",
                List.of("ergenverse:reincarnation_lily", "ergenverse:spirit_stone"),
                "ergenverse:purification_pill", 2,
                "RI: used by cultivators preparing for tribulation");

        // 7. Body-Tempering Pill (锻体丹) — tempers the physical body
        register("body_tempering_pill", "Body-Tempering Pill", "锻体丹",
                List.of("ergenverse:foundation_root_vine", "ergenverse:beast_bone", "ergenverse:spirit_stone_fragment"),
                "ergenverse:qi_gathering_pill", 0,
                "RI: used by body cultivators");

        // 8. Foundation Pill (筑基丹) — THE breakthrough pill for Foundation Establishment
        register("foundation_pill", "Foundation Pill", "筑基丹",
                List.of("ergenverse:foundation_root_vine", "ergenverse:spirit_stone", "ergenverse:beast_core"),
                "ergenverse:foundation_pill", 0,
                "RI: Wang Lin needs this for Foundation Establishment. Most sought-after pill in Zhao Country.");

        // 9. Nine-Turn Core Pill (九转金丹) — Core Formation breakthrough pill
        register("nine_turn_core_pill", "Nine-Turn Core Pill", "九转金丹",
                List.of("ergenverse:vermilion_blood_ginseng", "ergenverse:spirit_stone", "ergenverse:beast_core"),
                "ergenverse:foundation_pill", 2,
                "RI: extremely rare, needed for Core Formation");

        // 10. Heaven-Defying Pill (逆天丹) — legendary pill, defies heaven
        register("heaven_defying_pill", "Heaven-Defying Pill", "逆天丹",
                List.of("ergenverse:five_color_ginseng", "ergenverse:spirit_stone", "ergenverse:beast_core"),
                "ergenverse:soul_mending_pill", 3,
                "RI: legendary pill that can defy heavenly destiny");

        // 11. Dao Comprehension Pill (悟道丹) — aids Dao comprehension
        register("dao_comprehension_pill", "Dao Comprehension Pill", "悟道丹",
                List.of("ergenverse:dao_trace_vine", "ergenverse:spirit_stone"),
                "ergenverse:purification_pill", 2,
                "RI: used by cultivators seeking Dao insights");

        // 12. Lightning Essence Pill (雷精丹) — absorbs lightning energy
        register("lightning_essence_pill", "Lightning Essence Pill", "雷精丹",
                List.of("ergenverse:thunder_root_grass", "ergenverse:spirit_stone"),
                "ergenverse:qi_gathering_pill", 1,
                "RI: used to prepare for lightning tribulation");

        // 13. Fury Pill (暴怒丹) — temporarily boosts combat power
        register("fury_pill", "Fury Pill", "暴怒丹",
                List.of("ergenverse:blood_forgetting_grass", "ergenverse:beast_core"),
                "ergenverse:qi_gathering_pill", 0,
                "RI: desperate combat pill, has side effects");

        // 14. Blood-Refinement Pill (炼血丹) — used in blood refinement cultivation
        register("blood_refinement_pill", "Blood-Refinement Pill", "炼血丹",
                List.of("ergenverse:vermilion_blood_ginseng", "ergenverse:beast_core"),
                "ergenverse:qi_gathering_pill", 1,
                "RI: used by blood-refinement cultivators");
    }

    private static void register(String id, String name, String nameCn,
                                  List<String> ingredients, String result,
                                  int minRealm, String source) {
        RECIPES.put(id, new PillRecipe(id, name, nameCn, ingredients, result, minRealm, source));
    }

    /**
     * Try to match the given ingredients to a known canon recipe.
     * Returns the matching recipe, or null if no match.
     *
     * <p>Matching is by ingredient ID set — the ingredients must match exactly
     * (canon recipes are deterministic per Constitution Article I).
     */
    public static PillRecipe matchRecipe(List<String> ingredientIds) {
        Set<String> inputSet = new HashSet<>(ingredientIds);
        for (PillRecipe recipe : RECIPES.values()) {
            Set<String> recipeSet = new HashSet<>(recipe.ingredientIds());
            if (inputSet.equals(recipeSet)) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Get all known recipes.
     */
    public static Collection<PillRecipe> getAll() {
        return Collections.unmodifiableCollection(RECIPES.values());
    }

    /**
     * Get a specific recipe by ID.
     */
    public static PillRecipe get(String id) {
        return RECIPES.get(id);
    }

    /**
     * Get recipes that can be brewed at a given realm level.
     */
    public static List<PillRecipe> getAvailableAtRealm(int realmLevel) {
        return RECIPES.values().stream()
                .filter(r -> r.minRealm() <= realmLevel)
                .toList();
    }
}
