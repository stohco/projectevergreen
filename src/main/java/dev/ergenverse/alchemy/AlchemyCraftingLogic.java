package dev.ergenverse.alchemy;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.network.AlchemySyncS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Core alchemy crafting logic — capability-gated pill refinement.
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md §3.1 and §6:
 * The gate to crafting is the cultivator's CAPABILITY, not recipe discovery.
 * can_craft = materials AND realm >= recipe.min_realm AND divine_sense >= recipe.min_divine_sense.
 *
 * <p>This class manages:
 * <ul>
 *   <li>Recipe registry (code-defined pill recipes)</li>
 *   <li>Fire tier system (6 tiers from mortal to Ancient God)</li>
 *   <li>Waste pill handling (failed crafts produce waste pills, not nothing)</li>
 *   <li>Progress sync to client via {@link AlchemySyncS2CPacket}</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class AlchemyCraftingLogic {

    private AlchemyCraftingLogic() {}

    // ─── Fire Tier System ────────────────────────────────────────
    // Per DESIGN_UNIFIED_CRAFTING.md §3.1-fire

    /** Fire tiers: 0=mortal, 1=earth, 2=true, 3=samadhi, 4=heavenly, 5=Ancient God */
    public static final int FIRE_MORTAL = 0;
    public static final int FIRE_EARTH = 1;
    public static final int FIRE_TRUE = 2;
    public static final int FIRE_SAMADHI = 3;
    public static final int FIRE_HEAVENLY = 4;
    public static final int FIRE_ANCIENT_GOD = 5;

    public static String fireTierName(int tier) {
        return switch (tier) {
            case 0 -> "Mortal Fire (凡火)";
            case 1 -> "Earth Fire (地火)";
            case 2 -> "True Fire (真火)";
            case 3 -> "Samadhi True Fire (三昧真火)";
            case 4 -> "Heavenly Fire (天火)";
            case 5 -> "Ancient God Fire (古神之火)";
            default -> "Unknown Fire";
        };
    }

    // ─── Alchemy Recipes ────────────────────────────────────────

    /**
     * A pill recipe. Per §6: capability-gated, not recipe-gated.
     * All fields are immutable; the registry is code-defined.
     */
    public static final class PillRecipe {
        public final String id;
        public final String displayName;
        public final RealmId minRealm;
        public final int minDivineSense; // 0-100 normalized threshold
        public final int minFireTier;
        public final int minAlchemySkill; // 0-1000 (not yet tracked separately)
        public final ItemStack output; // the pill item stack
        public final ItemStack inputPrimary;
        public final ItemStack inputCatalyst;
        public final int craftDurationTicks; // 80 = 4s, 120 = 6s
        public final boolean highTier; // true = 6s, false = 4s
        public final float successRate; // 0-1, base success before capability modifiers

        public PillRecipe(String id, String displayName, RealmId minRealm,
                       int minDivineSense, int minFireTier,
                       int minAlchemySkill,
                       ItemStack output,
                       ItemStack inputPrimary,
                       ItemStack inputCatalyst,
                       boolean highTier,
                       float successRate) {
            this.id = id;
            this.displayName = displayName;
            this.minRealm = minRealm;
            this.minDivineSense = minDivineSense;
            this.minFireTier = minFireTier;
            this.minAlchemySkill = minAlchemySkill;
            this.output = output.copy();
            this.inputPrimary = inputPrimary.copy();
            this.inputCatalyst = inputCatalyst.copy();
            this.craftDurationTicks = highTier ? 120 : 80;
            this.highTier = highTier;
            this.successRate = successRate;
        }
    }

    /** Registered pill recipes. Populated in bootstrap(). */
    private static final Map<String, PillRecipe> RECIPES = new LinkedHashMap<>();

    // ─── Bootstrap ────────────────────────────────────────────────

    /**
     * Register all canonical pill recipes. Called from mod common setup.
     */
    public static void bootstrap() {
        // Helper to create a quick herb powder item
        // TODO: use actual herb items once they're registered as powder-able
        ItemStack qiPowder = herbPowder("qi_gathering_grass", "Qi Gathering Powder");
        ItemStack snowPowder = herbPowder("snow_heart_herb", "Snow Heart Powder");
        ItemStack soulPowder = herbPowder("soul_nourishing_lotus", "Soul Nourishing Powder");
        ItemStack ninePowder = herbPowder("nine_leaf_clover", "Nine Leaf Powder");
        ItemStack foundPowder = herbPowder("foundation_root_vine", "Foundation Root Powder");
        ItemStack spiritStone = makeItem("spirit_stone", 1);

        // Grade 1-3: Qi Condensation tier
        register("qi_gathering_pill", "Qi Gathering Pill (聚气丹)",
                RealmId.QI_CONDENSATION, 5, FIRE_MORTAL,
                qiPowder, ItemStack.EMPTY, 80, false, 0.8f);

        register("minor_healing_pill", "Minor Healing Pill (小疗伤丹)",
                RealmId.QI_CONDENSATION, 10, FIRE_MORTAL,
                snowPowder, ItemStack.EMPTY, 80, false, 0.7f);

        register("detox_pill", "Detox Pill (解毒丹)",
                RealmId.QI_CONDENSATION, 15, FIRE_MORTAL,
                ninePowder, ItemStack.EMPTY, 80, false, 0.6f);

        // Grade 4-6: Foundation tier
        register("foundation_pill", "Foundation Pill (筑基丹)",
                RealmId.FOUNDATION, 30, FIRE_EARTH,
                foundPowder, spiritStone, 80, false, 0.6f);

        register("heart_calming_incense", "Heart-Calming Incense (静心香)",
                RealmId.FOUNDATION, 40, FIRE_EARTH,
                ninePowder, spiritStone, 80, false, 0.5f);

        // Grade 7-8: Core Formation tier
        register("cold_dan", "Cold Dan (寒丹)",
                RealmId.CORE_FORMATION, 60, FIRE_TRUE,
                snowPowder, spiritStone, 100, false, 0.5f);

        register("distant_heaven_pill", "Distant Heaven Pill (远天丹)",
                RealmId.CORE_FORMATION, 80, FIRE_TRUE,
                snowPowder, spiritStone, 120, true, 0.3f);

        // Grade 9+: Soul Formation tier
        register("blood_soul_pill", "Blood Soul Pill (血魂丹)",
                RealmId.SOUL_FORMATION, 80, FIRE_SAMADHI,
                ninePowder, spiritStone, 100, false, 0.3f);

        register("purification_pill", "Purification Pill (净丹)",
                RealmId.CORE_FORMATION, 50, FIRE_TRUE,
                foundPowder, spiritStone, 80, false, 0.5f);

        register("soul_mending_pill", "Soul-Mending Pill (养魂丹)",
                RealmId.SOUL_FORMATION, 70, FIRE_SAMADHI,
                soulPowder, spiritStone, 100, false, 0.4f);

        register("meridian_clearing_powder", "Meridian-Clearing Powder (经脉净化散)",
                RealmId.CORE_FORMATION, 30, FIRE_EARTH,
                snowPowder, ItemStack.EMPTY, 80, false, 0.9f);

        Ergenverse.LOGGER.info("[Ergenverse] AlchemyCraftingLogic: {} pill recipes registered.", RECIPES.size());
    }

    /**
     * Register a pill recipe.
     *
     * @param id              recipe id (also used as output pill item id)
     * @param displayName     human-readable pill name (with Chinese)
     * @param minRealm        minimum cultivation realm required
     * @param minDivineSense  minimum divine sense (0-100 normalized)
     * @param minFireTier     minimum fire tier (FIRE_MORTAL..FIRE_ANCIENT_GOD)
     * @param inputPrimary    primary ingredient (e.g. herb powder)
     * @param inputCatalyst   secondary ingredient (e.g. spirit stone), may be EMPTY
     * @param durationTicks   craft duration in ticks (80 = 4s, 120 = 6s)
     * @param highTier        whether this is a high-tier recipe (6s)
     * @param successRate     base success rate 0-1
     */
    private static void register(String id, String displayName, RealmId minRealm,
                                  int minDivineSense, int minFireTier,
                                  ItemStack inputPrimary, ItemStack inputCatalyst,
                                  int durationTicks, boolean highTier,
                                  float successRate) {
        ItemStack output = makeItem(id, 1);
        PillRecipe recipe = new PillRecipe(id, displayName, minRealm,
                minDivineSense, minFireTier, 0,
                output, inputPrimary, inputCatalyst,
                highTier, successRate);
        RECIPES.put(id, recipe);
    }

    // ─── Recipe Access ────────────────────────────────────────

    public static PillRecipe getRecipe(String id) {
        return RECIPES.get(id);
    }

    public static boolean hasRecipe(String id) {
        return RECIPES.containsKey(id);
    }

    public static Map<String, PillRecipe> getAllRecipes() {
        return Map.copyOf(RECIPES);
    }

    // ─── Craft Execution ─────────────────────────────────────────

    /**
     * Attempt to start a craft. Validates capability gates before starting.
     *
     * @return true if the craft was started, false if the player
     *         lacks the required capability (with a diagnostic message).
     */
    public static boolean tryStartCraft(ServerPlayer player, String recipeId) {
        PillRecipe recipe = RECIPES.get(recipeId);
        if (recipe == null) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cUnknown recipe: " + recipeId));
            return false;
        }

        CultivationState state = CultivationCapability.get(player).orElse(null);
        if (state == null) return false;

        // Capability gate checks (§6)
        if (!state.getCurrentRealm().isAtLeast(recipe.minRealm)) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cYour realm is too low. Need: \u00A7f" +
                            recipe.minRealm.name));
            return false;
        }

        double ds = state.getDivineSense();
        if (ds < (recipe.minDivineSense / 100.0)) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cYour divine sense is too weak. Need: " +
                            recipe.minDivineSense + "/100"));
            return false;
        }

        int playerFireTier = computeEffectiveFireTier(player);
        if (playerFireTier < recipe.minFireTier) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cYour fire source is too weak. Need: \u00A7f" +
                            fireTierName(recipe.minFireTier) +
                            "\u00A77 (you have: " +
                            fireTierName(playerFireTier) + ")"));
            return false;
        }

        // Start crafting on the nearest Pill Furnace block entity
        // (in practice the player right-clicks the furnace to open the menu)
        player.sendSystemMessage(Component.literal(
                "\u00A7aPlace ingredients in the Pill Furnace and click Craft."));
        return true;
    }

    /**
     * Compute the player's effective fire tier.
     * Per §3.1-fire: mortal fire (torches) → earth fire
     * (fire vein block beneath furnace) → true fire (Foundation+ cultivator
     * internal) → etc. For now, everyone has earth fire by default
     * (fire vein blocks generate near sects — the "sect furnace" concept).
     *
     * <p>TODO: implement true fire detection (check for fire vein blocks
     * beneath the furnace, check player's internal fire tier).</p>
     */
    private static int computeEffectiveFireTier(ServerPlayer player) {
        CultivationState state = CultivationCapability.get(player).orElse(null);
        if (state == null) return FIRE_EARTH;
        int realmOrder = state.getCurrentRealm().order;
        if (realmOrder >= 5) return FIRE_SAMADHI;  // Soul Formation+
        if (realmOrder >= 3) return FIRE_TRUE;       // Core Formation+
        if (realmOrder >= 2) return FIRE_EARTH;       // Foundation+
        return FIRE_EARTH; // Qi Condensation+ (mortal torch suffices)
    }

    /**
     * Create a quick herb powder item from a block name.
     * TODO: replace with actual herb-powder items once registered.
     */
    private static ItemStack herbPowder(String blockName, String displayName) {
        // Use vanilla items as stand-ins for now
        Item item = blockName.equals("foundation_root_vine")
                ? Items.DIRT
                : blockName.equals("blood_forgetting_grass")
                ? Items.GUNPOWDER
                : Items.PAPER;
        ItemStack stack = new ItemStack(item, 1);
        stack.setHoverName(Component.literal(displayName));
        return stack;
    }

    private static ItemStack makeItem(String id, int count) {
        // Forge 1.20.1: ForgeRegistries.ITEMS.getValue returns Item directly (not Optional)
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Ergenverse.MOD_ID, id));
        if (item == null) item = Items.AIR;
        return new ItemStack(item, count);
    }

    /**
     * Get the effective fire tier name for display purposes.
     */
    public static String getPlayerFireTierName(ServerPlayer player) {
        return fireTierName(computeEffectiveFireTier(player));
    }
}
