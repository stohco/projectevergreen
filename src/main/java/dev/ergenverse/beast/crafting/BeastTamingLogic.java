package dev.ergenverse.beast.crafting;

import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Beast Taming logic engine — recipe database and matching.
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md §3.12: Beast Pact Altar consumes a
 * beast_core + pact_medium to attempt taming, with success probability
 * scaling on player realm vs. required realm and beast tier.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class BeastTamingLogic {

    private BeastTamingLogic() {}

    /** A single beast-taming recipe. */
    public static class BeastRecipe {
        public final String recipeId;
        public final Item beastCore;
        public final Item pactMedium;
        public final Item outputEgg;
        public final int beastTier;       // 1-5
        public final RealmId realmRequired;
        public final double successChanceBase; // 0.0-1.0

        public BeastRecipe(String recipeId, Item beastCore, Item pactMedium,
                           Item outputEgg, int beastTier, RealmId realmRequired,
                           double successChanceBase) {
            this.recipeId = recipeId;
            this.beastCore = beastCore;
            this.pactMedium = pactMedium;
            this.outputEgg = outputEgg;
            this.beastTier = beastTier;
            this.realmRequired = realmRequired;
            this.successChanceBase = successChanceBase;
        }
    }

    /** All 8 taming recipes. Keyed by recipeId. */
    public static final Map<String, BeastRecipe> RECIPES = new LinkedHashMap<>();

    /** Safe item lookup — returns Items.AIR if not registered (recipe simply won't match). */
    private static Item getItem(String name) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("ergenverse", name));
        return item; // may return Items.AIR if not registered
    }

    /** Populate the recipe map. Called during mod bootstrap. */
    public static void bootstrap() {
        RECIPES.clear();

        // 1. Silver Horned Thunder Beast — tier 3, Core Formation, 60% base
        RECIPES.put("silver_horned_thunder_beast", new BeastRecipe(
                "silver_horned_thunder_beast",
                getItem("beast_core"),
                getItem("spirit_blood"),
                getItem("silver_horned_thunder_egg"),
                3, RealmId.CORE_FORMATION, 0.6));

        // 2. Restriction Beast — tier 4, Soul Formation, 40% base
        RECIPES.put("restriction_beast", new BeastRecipe(
                "restriction_beast",
                getItem("beast_core"),
                getItem("soul_essence"),
                getItem("restriction_beast_egg"),
                4, RealmId.SOUL_FORMATION, 0.4));

        // 3. Restriction Origin Beast — tier 5, Ascendant, 20% base
        RECIPES.put("restriction_origin_beast", new BeastRecipe(
                "restriction_origin_beast",
                getItem("beast_core"),
                getItem("soul_essence"),
                getItem("restriction_origin_egg"),
                5, RealmId.ASCENDANT, 0.2));

        // 4. Fire Phoenix Juvenile — tier 3, Core Formation, 50% base
        RECIPES.put("fire_phoenix_juvenile", new BeastRecipe(
                "fire_phoenix_juvenile",
                getItem("beast_core"),
                getItem("fire_essence"),
                getItem("fire_phoenix_egg"),
                3, RealmId.CORE_FORMATION, 0.5));

        // 5. Thunder Bird — tier 2, Foundation, 70% base
        RECIPES.put("thunder_bird", new BeastRecipe(
                "thunder_bird",
                getItem("beast_core"),
                getItem("spirit_blood"),
                getItem("thunder_bird_egg"),
                2, RealmId.FOUNDATION, 0.7));

        // 6. Vein Guardian — tier 4, Soul Formation, 35% base
        RECIPES.put("vein_guardian", new BeastRecipe(
                "vein_guardian",
                getItem("beast_core"),
                getItem("spirit_gathering_core"),
                getItem("vein_guardian_egg"),
                4, RealmId.SOUL_FORMATION, 0.35));

        // 7. Karma Demon — tier 4, Soul Formation, 30% base
        RECIPES.put("karma_demon", new BeastRecipe(
                "karma_demon",
                getItem("beast_core"),
                getItem("restriction_essence"),
                getItem("karma_demon_egg"),
                4, RealmId.SOUL_FORMATION, 0.3));

        // 8. Reincarnation Spirit — tier 5, Ascendant, 15% base
        RECIPES.put("reincarnation_spirit", new BeastRecipe(
                "reincarnation_spirit",
                getItem("beast_core"),
                getItem("inheritance_jade_slip"),
                getItem("reincarnation_spirit_egg"),
                5, RealmId.ASCENDANT, 0.15));

        Ergenverse.LOGGER.info("[Ergenverse] BeastTamingLogic: {} recipes loaded.", RECIPES.size());
    }

    /**
     * Try to find a matching recipe for the given inputs and player realm.
     *
     * @param beastCoreItem  Item in slot 0 (beast_core)
     * @param pactMediumItem Item in slot 1 (pact medium)
     * @param playerRealm    The player's current realm
     * @return matching BeastRecipe, or null if no match
     */
    @SuppressWarnings("deprecation")
    public static BeastRecipe tryStartTame(Item beastCoreItem, Item pactMediumItem, RealmId playerRealm) {
        if (beastCoreItem == Items.AIR || pactMediumItem == Items.AIR) return null;

        for (BeastRecipe recipe : RECIPES.values()) {
            // beastCore must match, pactMedium must match
            if (recipe.beastCore == Items.AIR) continue;
            if (recipe.outputEgg == Items.AIR) continue;
            if (recipe.beastCore != beastCoreItem) continue;
            if (recipe.pactMedium != pactMediumItem) continue;
            return recipe;
        }
        return null;
    }
}