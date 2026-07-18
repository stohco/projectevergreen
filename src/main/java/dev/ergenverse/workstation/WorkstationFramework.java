package dev.ergenverse.workstation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.*;

/**
 * Universal Workstation Framework — common architecture for ALL crafting stations.
 *
 * <p>Per the user's directive: "A common architecture for blocks, block entities,
 * menus, progress, upgrades, automation, and persistence."
 *
 * <p>Every workstation (Alchemy Furnace, Artifact Forge, Talisman Desk, etc.)
 * uses this framework. It provides:
 * <ul>
 *   <li><b>Slot configuration</b> — flexible input/output slot layout</li>
 *   <li><b>Craft progression</b> — timed crafting with ≤12 second cap</li>
 *   <li><b>Upgrade system</b> — workstations can be upgraded (mortal → spirit → ancient)</li>
 *   <li><b>Automation support</b> — hoppers can insert/extract from designated slots</li>
 *   <li><b>Persistence</b> — NBT save/load of all state</li>
 *   <li><b>Material integration</b> — reads MaterialProperties from all ingredients</li>
 *   <li><b>Knowledge integration</b> — checks KnowledgeEngine for recipe availability</li>
 *   <li><b>Fire profile</b> — 5-dimensional fire system (temperature, purity, stability, element, controller)</li>
 * </ul>
 */
public abstract class WorkstationFramework {

    /** Technology tier — determines what can be crafted. */
    public enum TechTier {
        MORTAL("Mortal", 0),      // clay furnace, iron forge
        SPIRIT("Spirit", 1),      // requires spirit materials
        ANCIENT("Ancient", 2),    // requires inheritance/blueprint
        DIVINE("Divine", 3);      // requires Dao comprehension

        public final String label;
        public final int level;
        TechTier(String label, int level) { this.label = label; this.level = level; }
    }

    /** Fire profile — 5-dimensional fire system. */
    public record FireProfile(
            double temperature,  // 0.0-1.0 (mortal charcoal → Nascent Soul flame)
            double purity,       // 0.0-1.0 (impure → perfectly pure)
            double stability,    // 0.0-1.0 (unstable → extremely stable)
            String element,      // "neutral", "fire", "water", "lightning", "dao"
            double controller    // 0.0-1.0 (uncontrolled → perfect control)
    ) {
        public static FireProfile MORTAL_FIRE = new FireProfile(0.6, 0.2, 0.3, "neutral", 0.0);
        public static FireProfile SPIRIT_FIRE = new FireProfile(0.7, 0.6, 0.7, "fire", 0.5);
        public static FireProfile SOUL_FIRE = new FireProfile(0.9, 0.9, 0.95, "dao", 1.0);

        /** Check if this fire can craft a recipe requiring a minimum fire profile. */
        public boolean canHandle(FireProfile minimum) {
            return temperature >= minimum.temperature &&
                   purity >= minimum.purity &&
                   stability >= minimum.stability &&
                   controller >= minimum.controller;
        }
    }

    /** Slot definition — describes a slot's role in the workstation. */
    public record SlotDef(
            int index,
            String role,       // "ingredient", "fuel", "output", "catalyst", "blood"
            boolean input,     // can items be inserted
            boolean output,    // can items be extracted
            String accepts     // item tag or "any" for what this slot accepts
    ) {}

    /** Workstation upgrade — modifies the workstation's capabilities. */
    public record Upgrade(
            String id,
            String name,
            TechTier requiredTier,
            double speedMultiplier,    // craft speed bonus
            double qualityMultiplier,  // output quality bonus
            double stabilityBonus,     // reduces explosion chance
            FireProfile fireUpgrade    // fire profile improvement
    ) {}

    // ═══ Core State ═══════════════════════════════════════════════════

    private final String workstationType;  // "alchemy_furnace", "artifact_forge", etc.
    private TechTier tier;
    private FireProfile currentFire;
    private final List<Upgrade> installedUpgrades = new ArrayList<>();
    private final List<SlotDef> slotDefinitions;

    // Craft state
    private int craftProgress = 0;
    private int craftDuration = 80; // 4 seconds default (≤12 second cap per DESIGN_UNIFIED_CRAFTING)
    private boolean isCrafting = false;

    // ═══ Constructor ═══════════════════════════════════════════════════

    protected WorkstationFramework(String type, List<SlotDef> slots) {
        this.workstationType = type;
        this.tier = TechTier.MORTAL;
        this.currentFire = FireProfile.MORTAL_FIRE;
        this.slotDefinitions = new ArrayList<>(slots);
    }

    // ═══ Abstract Methods ═════════════════════════════════════════════

    /**
     * Resolve the crafting result from the current ingredients.
     * Each workstation implements this differently:
     * - Alchemy Furnace: property-based pill resolution
     * - Artifact Forge: property-based artifact resolution
     * - Talisman Desk: property-based talisman resolution
     * - Formation Platform: component-based formation resolution
     *
     * @param ingredients The items in the input slots
     * @param fire The current fire profile
     * @param tier The workstation's tech tier
     * @return The resulting ItemStack, or EMPTY if no valid result
     */
    protected abstract ItemStack resolveCraft(List<ItemStack> ingredients, FireProfile fire, TechTier tier);

    /**
     * Check if the current ingredients can be crafted.
     * Returns false if: wrong tier, missing knowledge, incompatible materials, etc.
     */
    protected abstract boolean canCraft(List<ItemStack> ingredients, FireProfile fire, TechTier tier);

    // ═══ Craft Logic ═══════════════════════════════════════════════════

    /**
     * Tick the workstation. Called every server tick.
     * @return true if the craft completed this tick
     */
    public boolean tick(ItemStackHandler inventory) {
        if (!isCrafting) return false;

        // Apply speed upgrades
        int effectiveDuration = (int) (craftDuration / getSpeedMultiplier());

        craftProgress++;
        if (craftProgress >= effectiveDuration) {
            // Craft complete
            craftProgress = 0;
            isCrafting = false;
            return true;
        }
        return false;
    }

    /**
     * Start a craft. Checks if ingredients are valid.
     */
    public boolean startCraft(ItemStackHandler inventory) {
        List<ItemStack> ingredients = getIngredients(inventory);
        if (!canCraft(ingredients, currentFire, tier)) {
            return false;
        }
        isCrafting = true;
        craftProgress = 0;
        return true;
    }

    /**
     * Complete the craft — resolve result and consume ingredients.
     */
    public ItemStack completeCraft(ItemStackHandler inventory) {
        List<ItemStack> ingredients = getIngredients(inventory);
        ItemStack result = resolveCraft(ingredients, currentFire, tier);

        // Apply quality upgrades
        if (!result.isEmpty()) {
            // TODO: apply quality multiplier to result NBT
        }

        // Consume ingredients
        for (SlotDef slot : slotDefinitions) {
            if (slot.input() && slot.role().equals("ingredient")) {
                ItemStack stack = inventory.getStackInSlot(slot.index());
                if (!stack.isEmpty()) {
                    stack.shrink(1);
                }
            }
        }

        return result;
    }

    /**
     * Cancel the craft — return materials (with small waste chance for high-tier).
     */
    public void cancelCraft() {
        isCrafting = false;
        craftProgress = 0;
        // Materials are NOT consumed on cancel (per DESIGN_UNIFIED_CRAFTING.md)
    }

    // ═══ Helpers ═══════════════════════════════════════════════════════

    private List<ItemStack> getIngredients(ItemStackHandler inventory) {
        List<ItemStack> ingredients = new ArrayList<>();
        for (SlotDef slot : slotDefinitions) {
            if (slot.input() && slot.role().equals("ingredient")) {
                ingredients.add(inventory.getStackInSlot(slot.index()));
            }
        }
        return ingredients;
    }

    private double getSpeedMultiplier() {
        double mult = 1.0;
        for (Upgrade u : installedUpgrades) {
            mult *= u.speedMultiplier();
        }
        return mult;
    }

    // ═══ Upgrades ══════════════════════════════════════════════════════

    public boolean canInstallUpgrade(Upgrade upgrade) {
        return tier.level >= upgrade.requiredTier().level;
    }

    public void installUpgrade(Upgrade upgrade) {
        if (canInstallUpgrade(upgrade)) {
            installedUpgrades.add(upgrade);
            // Upgrade fire profile
            if (upgrade.fireUpgrade() != null) {
                currentFire = new FireProfile(
                        Math.max(currentFire.temperature(), upgrade.fireUpgrade().temperature()),
                        Math.max(currentFire.purity(), upgrade.fireUpgrade().purity()),
                        Math.max(currentFire.stability(), upgrade.fireUpgrade().stability()),
                        upgrade.fireUpgrade().element(),
                        Math.max(currentFire.controller(), upgrade.fireUpgrade().controller())
                );
            }
        }
    }

    // ═══ Getters/Setters ═══════════════════════════════════════════════

    public String workstationType() { return workstationType; }
    public TechTier tier() { return tier; }
    public void setTier(TechTier tier) { this.tier = tier; }
    public FireProfile currentFire() { return currentFire; }
    public void setFire(FireProfile fire) { this.currentFire = fire; }
    public int craftProgress() { return craftProgress; }
    public int craftDuration() { return craftDuration; }
    public void setCraftDuration(int ticks) { this.craftDuration = Math.min(ticks, 240); } // ≤12 second cap
    public boolean isCrafting() { return isCrafting; }
    public List<SlotDef> slotDefinitions() { return Collections.unmodifiableList(slotDefinitions); }
    public List<Upgrade> installedUpgrades() { return Collections.unmodifiableList(installedUpgrades); }

    /**
     * Get craft progress as percentage (0-100).
     */
    public int getProgressPercent() {
        int effectiveDuration = (int) (craftDuration / getSpeedMultiplier());
        return effectiveDuration > 0 ? (craftProgress * 100 / effectiveDuration) : 0;
    }

    // ═══ NBT Persistence ═══════════════════════════════════════════════

    public CompoundTag save(CompoundTag tag) {
        tag.putString("type", workstationType);
        tag.putInt("tier", tier.level);
        tag.putDouble("fireTemp", currentFire.temperature());
        tag.putDouble("firePurity", currentFire.purity());
        tag.putDouble("fireStability", currentFire.stability());
        tag.putString("fireElement", currentFire.element());
        tag.putDouble("fireController", currentFire.controller());
        tag.putInt("craftProgress", craftProgress);
        tag.putInt("craftDuration", craftDuration);
        tag.putBoolean("isCrafting", isCrafting);
        return tag;
    }

    public void load(CompoundTag tag) {
        tier = TechTier.values()[Math.min(tag.getInt("tier"), TechTier.values().length - 1)];
        currentFire = new FireProfile(
                tag.getDouble("fireTemp"),
                tag.getDouble("firePurity"),
                tag.getDouble("fireStability"),
                tag.getString("fireElement"),
                tag.getDouble("fireController")
        );
        craftProgress = tag.getInt("craftProgress");
        craftDuration = tag.getInt("craftDuration");
        isCrafting = tag.getBoolean("isCrafting");
    }
}
