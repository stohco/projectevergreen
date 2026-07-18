package dev.ergenverse.beast.crafting;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.network.ERNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for the Beast Pact Altar (御兽台) — workstation for taming
 * beasts via beast_core + pact_medium → tamed beast spawn egg.
 *
 * <p>3-slot inventory via Forge's {@link ItemStackHandler}:
 * <ul>
 *   <li>Slot 0: Beast Core (兽核) — consumed on attempt</li>
 *   <li>Slot 1: Pact Medium (灵血 etc.) — consumed on success, or on failure</li>
 *   <li>Slot 2: Output (tamed beast spawn egg)</li>
 * </ul>
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md §3.12:
 * <ul>
 *   <li>5-second taming session (100 ticks).</li>
 *   <li>Success roll: baseChance + (playerRealm.ordinal() - requiredRealm.ordinal()) * 0.1, capped at 0.95.</li>
 *   <li>On failure: pact medium consumed, beast core preserved.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class BeastPactAltarBlockEntity extends BlockEntity implements MenuProvider, Container {

    public static final int CONTAINER_SIZE = 3;
    public static final int SLOT_BEAST_CORE = 0;
    public static final int SLOT_PACT_MEDIUM = 1;
    public static final int SLOT_OUTPUT = 2;

    /** 5 seconds per taming session per §13 rebalance. */
    private static final int SESSION_TICKS = 100;

    private final ItemStackHandler inventory = new ItemStackHandler(CONTAINER_SIZE) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    private int sessionTimer;
    private boolean taming;
    @Nullable private ServerPlayer tamingPlayer;
    @Nullable private BeastTamingLogic.BeastRecipe activeRecipe;

    public BeastPactAltarBlockEntity(BlockPos pos, BlockState state) {
        super(dev.ergenverse.block.ErgenverseBlocks.BEAST_PACT_ALTAR_ENTITY.get(), pos, state);
    }

    // ─── NBT ──────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("SessionTimer", sessionTimer);
        tag.putBoolean("Taming", taming);
        if (activeRecipe != null) {
            tag.putString("ActiveRecipe", activeRecipe.recipeId);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) inventory.deserializeNBT(tag.getCompound("Inventory"));
        sessionTimer = tag.getInt("SessionTimer");
        taming = tag.getBoolean("Taming");
        String recipeId = tag.getString("ActiveRecipe");
        activeRecipe = BeastTamingLogic.RECIPES.get(recipeId);
    }

    // ─── Taming Session ────────────────────────────────────────────

    /**
     * Start a taming session. Validates inputs, realm gate, and recipe match.
     */
    public boolean startSession(ServerPlayer player) {
        if (taming) return false;

        ItemStack coreStack = inventory.getStackInSlot(SLOT_BEAST_CORE);
        ItemStack mediumStack = inventory.getStackInSlot(SLOT_PACT_MEDIUM);

        if (coreStack.isEmpty()) {
            player.sendSystemMessage(Component.literal("\u00A7cPlace a Beast Core on the altar."));
            return false;
        }
        if (mediumStack.isEmpty()) {
            player.sendSystemMessage(Component.literal("\u00A7cPlace a Pact Medium in the second slot."));
            return false;
        }
        if (!inventory.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            player.sendSystemMessage(Component.literal("\u00A7cRemove the output item first."));
            return false;
        }

        // Get player realm
        CultivationState state = CultivationCapability.get(player).orElse(null);
        if (state == null) return false;
        RealmId playerRealm = state.getCurrentRealm();

        // Find matching recipe
        BeastTamingLogic.BeastRecipe recipe = BeastTamingLogic.tryStartTame(
                coreStack.getItem(), mediumStack.getItem(), playerRealm);

        if (recipe == null) {
            player.sendSystemMessage(Component.literal("\u00A7cNo matching taming recipe for these inputs."));
            return false;
        }

        // Check realm gate
        if (!playerRealm.isAtLeast(recipe.realmRequired)) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cTaming this beast requires realm: " + recipe.realmRequired.name));
            return false;
        }

        this.activeRecipe = recipe;
        this.sessionTimer = 0;
        this.taming = true;
        this.tamingPlayer = player;
        setChanged();
        return true;
    }

    public void tickSession(Level level) {
        if (!taming) return;
        sessionTimer++;

        if (sessionTimer >= SESSION_TICKS) {
            completeSession(level);
        }
        setChanged();
        syncProgress();
    }

    private void completeSession(Level level) {
        ServerPlayer player = tamingPlayer;
        BeastTamingLogic.BeastRecipe recipe = activeRecipe;
        taming = false;
        tamingPlayer = null;
        activeRecipe = null;
        sessionTimer = 0;

        if (player == null || level == null || recipe == null) return;

        // Get player realm for success roll
        CultivationState state = CultivationCapability.get(player).orElse(null);
        RealmId playerRealm = state != null ? state.getCurrentRealm() : RealmId.MORTAL;

        // Success roll: baseChance + (playerRealm.ordinal() - requiredRealm.ordinal()) * 0.1, capped at 0.95
        double chance = recipe.successChanceBase
                + (playerRealm.ordinal() - recipe.realmRequired.ordinal()) * 0.1;
        chance = Math.min(chance, 0.95);
        chance = Math.max(chance, 0.01); // minimum 1% chance

        // Use level random for the roll
        boolean success = level.getRandom().nextFloat() < chance;

        if (success) {
            // Consume beast core and pact medium
            inventory.getStackInSlot(SLOT_BEAST_CORE).shrink(1);
            if (inventory.getStackInSlot(SLOT_BEAST_CORE).isEmpty()) {
                inventory.setStackInSlot(SLOT_BEAST_CORE, ItemStack.EMPTY);
            }
            inventory.getStackInSlot(SLOT_PACT_MEDIUM).shrink(1);
            if (inventory.getStackInSlot(SLOT_PACT_MEDIUM).isEmpty()) {
                inventory.setStackInSlot(SLOT_PACT_MEDIUM, ItemStack.EMPTY);
            }

            // Create output with NBT
            if (recipe.outputEgg != Items.AIR) {
                ItemStack output = new ItemStack(recipe.outputEgg, 1);
                CompoundTag tag = output.getOrCreateTag();
                tag.putString("tamed_beast", recipe.recipeId);
                output.setTag(tag);
                inventory.setStackInSlot(SLOT_OUTPUT, output);
            }

            int pct = (int) (chance * 100);
            player.sendSystemMessage(Component.literal(
                    "\u00A7a\u00A7lTaming successful! " + recipe.recipeId.replace('_', ' ')
                    + " (Tier " + recipe.beastTier + ") has been bound. (" + pct + "% chance)"));
        } else {
            // Failure: consume pact medium only
            inventory.getStackInSlot(SLOT_PACT_MEDIUM).shrink(1);
            if (inventory.getStackInSlot(SLOT_PACT_MEDIUM).isEmpty()) {
                inventory.setStackInSlot(SLOT_PACT_MEDIUM, ItemStack.EMPTY);
            }

            int pct = (int) (chance * 100);
            player.sendSystemMessage(Component.literal(
                    "\u00A7cTaming failed. The beast resisted the pact. (" + pct + "% chance) "
                    + "Pact medium consumed. Beast core preserved."));
        }

        setChanged();
        syncProgress();
    }

    private void syncProgress() {
        if (level == null || level.isClientSide()) return;
        float progress = taming
                ? Math.min(1.0f, (float) sessionTimer / (float) SESSION_TICKS)
                : 0.0f;
        String recipeName = activeRecipe != null ? activeRecipe.recipeId : "";
        // TODO: sync disabled
    }

    private java.util.List<ServerPlayer> getViewerPlayers() {
        java.util.List<ServerPlayer> viewers = new java.util.ArrayList<>();
        if (level == null || level.isClientSide()) return viewers;
        for (Player p : level.players()) {
            if (p instanceof ServerPlayer sp
                    && sp.containerMenu instanceof BeastPactAltarMenu menu
                    && menu.getAltar() == this) {
                viewers.add(sp);
            }
        }
        return viewers;
    }

    // ─── Accessors ────────────────────────────────────────────────────

    public float getSessionProgress() {
        return taming ? Math.min(1.0f, (float) sessionTimer / (float) SESSION_TICKS) : 0.0f;
    }
    public boolean isTaming() { return taming; }
    public ItemStackHandler getInventory() { return inventory; }
    @Nullable public BeastTamingLogic.BeastRecipe getActiveRecipe() { return activeRecipe; }

    /** Calculate the effective success chance for the current recipe and given player realm. */
    public double getSuccessChance(RealmId playerRealm) {
        if (activeRecipe == null) return 0.0;
        double chance = activeRecipe.successChanceBase
                + (playerRealm.ordinal() - activeRecipe.realmRequired.ordinal()) * 0.1;
        return Math.min(Math.max(chance, 0.01), 0.95);
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    // ── Container contract ───────────────────────────────────────────

    @Override public int getContainerSize() { return CONTAINER_SIZE; }
    @Override public boolean isEmpty() { for (int i = 0; i < CONTAINER_SIZE; i++) if (!inventory.getStackInSlot(i).isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int slot) { return inventory.getStackInSlot(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack s = inventory.getStackInSlot(slot); if (s.isEmpty()) return ItemStack.EMPTY; ItemStack e = s.split(amount); inventory.setStackInSlot(slot, s); setChanged(); return e; }
    @Override public ItemStack removeItemNoUpdate(int slot) { ItemStack s = inventory.getStackInSlot(slot); inventory.setStackInSlot(slot, ItemStack.EMPTY); return s; }
    @Override public void setItem(int slot, ItemStack stack) { inventory.setStackInSlot(slot, stack); setChanged(); }
    @Override public void clearContent() { for (int i = 0; i < CONTAINER_SIZE; i++) inventory.setStackInSlot(i, ItemStack.EMPTY); }
    @Override public boolean stillValid(Player player) { if (level == null) return false; if (level.getBlockEntity(worldPosition) != this) return false; return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0; }

    // ── MenuProvider contract ──────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ergenverse.beast_pact_altar");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new BeastPactAltarMenu(containerId, playerInv, this);
    }
}