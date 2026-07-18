package dev.ergenverse.flora;

import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * RealmHarvestBehavior — cultivation as a lens applied to harvesting.
 *
 * <p>A mortal uproots the plant (1 item, plant destroyed, 30% quality retained —
 * clumsy harvesting damages the medicinal essence).
 *
 * <p>A Qi Condensation cultivator plucks carefully (1-2 items, plant regresses
 * to YOUNG, 60% quality).
 *
 * <p>A Foundation cultivator extracts the roots properly (2-3 items, plant
 * regresses to SPROUT, 85% quality). A Core Formation cultivator does the
 * same, slightly better (3-4 items, 90% quality).
 *
 * <p>A Nascent Soul+ cultivator uses divine sense to separate the medicinal
 * essence without harming the plant (3-5 items at full quality, plant stays
 * at MATURE).
 *
 * <p>Mutations double the yield and may add a special mutated-variant item.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public final class RealmHarvestBehavior {

    private RealmHarvestBehavior() {}

    // ── Result container ─────────────────────────────────────────────

    /**
     * The outcome of a harvest attempt. Yields are spawned at the block
     * position by the caller; this class is a pure data holder.
     */
    public static final class HarvestResult {
        public final List<ItemStack> yields;
        public final boolean plantDestroyed;
        public final float qualityRetained;
        public final String messageKey;

        public HarvestResult(List<ItemStack> yields, boolean plantDestroyed,
                             float qualityRetained, String messageKey) {
            this.yields = yields;
            this.plantDestroyed = plantDestroyed;
            this.qualityRetained = qualityRetained;
            this.messageKey = messageKey;
        }

        public static HarvestResult empty(String messageKey) {
            return new HarvestResult(new ArrayList<>(), false, 0f, messageKey);
        }
    }

    // ── Main entry point ─────────────────────────────────────────────

    /**
     * Attempt a harvest. The caller (SmallHerbBlock break handler) is
     * responsible for spawning yields at the block position and applying
     * the stage regression / destruction to the BlockEntity.
     *
     * @param player   the harvesting player (must be server-side)
     * @param species  the species of the plant
     * @param stage    the current biological stage
     * @param props    the hidden properties (used for mutation + quality)
     * @param level    the server level
     * @param pos      the block position
     */
    public static HarvestResult harvest(ServerPlayer player, FloraSpecies species,
                                        BiologicalStage stage, FloraHiddenProperties props,
                                        ServerLevel level, BlockPos pos) {
        if (species == null) {
            return HarvestResult.empty("msg.ergenverse.flora.unknown_species");
        }

        // Stage gate: cannot harvest if the stage doesn't permit it OR
        // the plant hasn't reached the species' minimum stage.
        if (!stage.canHarvest || !species.isHarvestableAt(stage)) {
            return HarvestResult.empty("msg.ergenverse.flora.too_young");
        }

        // Resolve the player's cultivation realm.
        RealmId realm = resolveRealm(player);

        // Compute base yield count + quality + outcome behavior.
        int baseCount;
        float qualityRetained;
        boolean plantDestroyed;
        BiologicalStage regressTo;
        String messageKey;

        switch (realm) {
            case MORTAL:
                baseCount = 1;
                qualityRetained = 0.30f;
                plantDestroyed = true;
                regressTo = BiologicalStage.DEAD;
                messageKey = "msg.ergenverse.flora.mortal_harvest";
                break;
            case QI_CONDENSATION:
                baseCount = 1 + rollBonus(level.getRandom(), 2); // 1-2
                qualityRetained = 0.60f;
                plantDestroyed = false;
                regressTo = BiologicalStage.YOUNG;
                messageKey = "msg.ergenverse.flora.qi_harvest";
                break;
            case FOUNDATION:
                baseCount = 2 + rollBonus(level.getRandom(), 2); // 2-3
                qualityRetained = 0.85f;
                plantDestroyed = false;
                regressTo = BiologicalStage.SPROUT;
                messageKey = "msg.ergenverse.flora.foundation_harvest";
                break;
            case CORE_FORMATION:
                baseCount = 3 + rollBonus(level.getRandom(), 2); // 3-4
                qualityRetained = 0.90f;
                plantDestroyed = false;
                regressTo = BiologicalStage.SPROUT;
                messageKey = "msg.ergenverse.flora.core_harvest";
                break;
            default:
                // NASCENT_SOUL and above: divine sense harvest
                baseCount = 3 + rollBonus(level.getRandom(), 3); // 3-5
                qualityRetained = 1.00f;
                plantDestroyed = false;
                regressTo = BiologicalStage.MATURE; // unharmed
                messageKey = "msg.ergenverse.flora.divine_sense_harvest";
                break;
        }

        // Stage yield multiplier (FLOWERING > MATURE > YOUNG > DORMANT).
        float stageMult = species.getYieldMultiplier(stage);
        int yieldCount = Math.max(1, Math.round(baseCount * stageMult));

        // Mutation bonus: doubled yields + special mutated-variant drop chance.
        boolean mutated = props != null && props.getMutation() != null;
        if (mutated) {
            yieldCount *= 2;
        }

        // Build the yield list.
        List<ItemStack> yields = new ArrayList<>();
        Item harvestItem = resolveItem(species.harvestItemId);
        if (harvestItem != null) {
            ItemStack stack = new ItemStack(harvestItem, yieldCount);
            // Tag the stack with quality + mutation metadata for downstream
            // alchemy / pill-crafting systems to read.
            var tag = stack.getOrCreateTag();
            tag.putFloat("quality", qualityRetained * (props != null ? props.qualityScore() : 0.5f));
            tag.putFloat("qualityRetained", qualityRetained);
            if (species.defaultElement != null) tag.putString("element", species.defaultElement);
            if (mutated && props != null) {
                tag.putString("mutation", props.getMutation());
                tag.putBoolean("mutated", true);
            }
            yields.add(stack);
        }

        // Fruiting stage: also drop fruit if the species has a fruit item.
        if (stage == BiologicalStage.FRUITING && species.fruitItemId != null) {
            Item fruitItem = resolveItem(species.fruitItemId);
            if (fruitItem != null) {
                int fruitCount = 1 + rollBonus(level.getRandom(), 2);
                if (mutated) fruitCount *= 2;
                yields.add(new ItemStack(fruitItem, fruitCount));
            }
        }

        // Mutated special drop: a separate "mutated variant" item, if it exists.
        if (mutated && species.harvestItemId != null) {
            String mutatedId = species.harvestItemId + "_mutated";
            Item mutatedItem = resolveItem(mutatedId);
            if (mutatedItem != null) {
                yields.add(new ItemStack(mutatedItem, 1));
            }
        }

        // Stash the regressTo stage on the result via a side-channel — we
        // return it through a thread-local since HarvestResult is the public
        // contract and we don't want to expose regression to mortal callers.
        // Actually, simpler: we just expose it as a public field on a subclass.
        // But to keep the API minimal, we'll re-derive the regression in the
        // caller via getRegressionStage(realm).
        return new HarvestResult(yields, plantDestroyed, qualityRetained, messageKey);
    }

    /**
     * Determine the stage a plant regresses to after harvest, given the
     * harvester's realm. MORTAL → DEAD (destroyed). QI_CONDENSATION → YOUNG.
     * FOUNDATION/CORE → SPROUT. NASCENT_SOUL+ → MATURE (unharmed).
     */
    public static BiologicalStage getRegressionStage(RealmId realm) {
        switch (realm) {
            case MORTAL: return BiologicalStage.DEAD;
            case QI_CONDENSATION: return BiologicalStage.YOUNG;
            case FOUNDATION:
            case CORE_FORMATION: return BiologicalStage.SPROUT;
            default: return BiologicalStage.MATURE;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────

    /** Resolve the player's realm from their cultivation capability. */
    private static RealmId resolveRealm(ServerPlayer player) {
        CultivationState state = player.getCapability(CultivationCapability.CULTIVATION_STATE)
                .orElse(null);
        if (state == null) return RealmId.MORTAL;
        try {
            return state.getCurrentRealm();
        } catch (Throwable t) {
            return RealmId.MORTAL;
        }
    }

    /** Resolve an item by string ID. Accepts "modid:path" or bare "path" (assumed ergenverse). */
    private static Item resolveItem(String id) {
        if (id == null || id.isEmpty()) return null;
        ResourceLocation rl;
        if (id.contains(":")) {
            rl = new ResourceLocation(id);
        } else {
            rl = new ResourceLocation(Ergenverse.MOD_ID, id);
        }
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        // ForgeRegistries.ITEMS.getValue returns Items.AIR for unknown — treat AIR as null.
        if (item == net.minecraft.world.item.Items.AIR) return null;
        return item;
    }

    /** Roll a bonus count in [0, max). */
    private static int rollBonus(RandomSource rng, int max) {
        if (max <= 0) return 0;
        return rng.nextInt(max);
    }
}
