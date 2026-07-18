package dev.ergenverse.body.crafting;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.MeditationHandler;
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
 * Block entity for the Refining Pool (炼体池) — body tempering workstation.
 *
 * <p>2-slot inventory via Forge's {@link ItemStackHandler}:
 * <ul>
 *   <li>Slot 0: Tempering Medium (spirit iron, beast blood, etc.) — consumed on success</li>
 *   <li>Slot 1: Fuel (spirit stone) — consumed each session</li>
 * </ul>
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md §3.10:
 * <ul>
 *   <li>8-second tempering session (160 ticks).</li>
 *   <li>On completion: consumes 1 medium, applies bloodRefinement gain, drains HP.</li>
 *   <li>HP drain can kill — low-realm cultivators beware.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class RefiningPoolBlockEntity extends BlockEntity implements MenuProvider, Container {

    public static final int CONTAINER_SIZE = 2;
    public static final int SLOT_MEDIUM = 0;
    public static final int SLOT_FUEL = 1;

    /** 8 seconds per tempering session per §13 rebalance. */
    private static final int SESSION_TICKS = 160;

    private final ItemStackHandler inventory = new ItemStackHandler(CONTAINER_SIZE) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    private int sessionTimer;
    private boolean tempering;
    @Nullable private ServerPlayer temperingPlayer;
    @Nullable private BodyRefiningLogic.BodyRecipe activeRecipe;

    public RefiningPoolBlockEntity(BlockPos pos, BlockState state) {
        super(dev.ergenverse.block.ErgenverseBlocks.REFINING_POOL_ENTITY.get(), pos, state);
    }

    // ─── NBT ──────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("SessionTimer", sessionTimer);
        tag.putBoolean("Tempering", tempering);
        if (activeRecipe != null) {
            tag.putString("ActiveRecipe", activeRecipe.recipeId);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) inventory.deserializeNBT(tag.getCompound("Inventory"));
        sessionTimer = tag.getInt("SessionTimer");
        tempering = tag.getBoolean("Tempering");
        String recipeId = tag.getString("ActiveRecipe");
        // Re-find recipe by ID — won't match if bootstrap hasn't run, but that's fine
        for (BodyRefiningLogic.BodyRecipe r : BodyRefiningLogic.allRecipes()) {
            if (r.recipeId.equals(recipeId)) {
                activeRecipe = r;
                break;
            }
        }
    }

    // ─── Tempering Session ────────────────────────────────────────────

    /**
     * Start a tempering session. Validates inputs, realm gate, HP, and fuel.
     */
    public boolean startSession(ServerPlayer player) {
        if (tempering) return false;

        ItemStack mediumStack = inventory.getStackInSlot(SLOT_MEDIUM);
        ItemStack fuelStack = inventory.getStackInSlot(SLOT_FUEL);

        if (mediumStack.isEmpty()) {
            player.sendSystemMessage(Component.literal("\u00A7cPlace a tempering medium in the first slot."));
            return false;
        }
        if (fuelStack.isEmpty()) {
            player.sendSystemMessage(Component.literal("\u00A7cPlace a spirit stone as fuel in the second slot."));
            return false;
        }

        // Find matching recipe
        BodyRefiningLogic.BodyRecipe recipe = BodyRefiningLogic.findRecipe(mediumStack.getItem());
        if (recipe == null) {
            player.sendSystemMessage(Component.literal("\u00A7cNo refining recipe for this item."));
            return false;
        }

        // Get player realm
        CultivationState state = CultivationCapability.get(player).orElse(null);
        if (state == null) return false;

        int playerOrdinal = state.getCurrentRealm().ordinal();
        if (playerOrdinal < recipe.minRealmOrdinal) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cBody refining requires at least realm ordinal " + recipe.minRealmOrdinal
                    + " (Foundation Establishment or higher)."));
            return false;
        }

        // HP check: must be at least 50% max HP
        if (player.getHealth() < player.getMaxHealth() * 0.5) {
            player.sendSystemMessage(Component.literal("\u00A7cToo injured to enter the pool. Heal to at least 50% HP."));
            return false;
        }

        this.activeRecipe = recipe;
        this.sessionTimer = 0;
        this.tempering = true;
        this.temperingPlayer = player;
        setChanged();
        return true;
    }

    public void tickSession(Level level) {
        if (!tempering) return;
        sessionTimer++;

        if (sessionTimer >= SESSION_TICKS) {
            completeSession(level);
        }
        setChanged();
        syncProgress();
    }

    private void completeSession(Level level) {
        ServerPlayer player = temperingPlayer;
        BodyRefiningLogic.BodyRecipe recipe = activeRecipe;
        tempering = false;
        temperingPlayer = null;
        activeRecipe = null;
        sessionTimer = 0;

        if (player == null || level == null || recipe == null) return;

        // Get player cultivation state
        CultivationState state = CultivationCapability.get(player).orElse(null);

        // Consume 1 medium
        inventory.getStackInSlot(SLOT_MEDIUM).shrink(1);
        if (inventory.getStackInSlot(SLOT_MEDIUM).isEmpty()) {
            inventory.setStackInSlot(SLOT_MEDIUM, ItemStack.EMPTY);
        }

        // Consume 1 fuel
        inventory.getStackInSlot(SLOT_FUEL).shrink(1);
        if (inventory.getStackInSlot(SLOT_FUEL).isEmpty()) {
            inventory.setStackInSlot(SLOT_FUEL, ItemStack.EMPTY);
        }

        // Apply blood refinement gain
        if (state != null) {
            state.setBloodRefinement(state.getBloodRefinement() + recipe.bloodRefinementGain);
            double newBR = state.getBloodRefinement();
            int pct = (int) (newBR * 100);
            player.sendSystemMessage(Component.literal(
                    "\u00A7a\u00A7lBody tempered! Blood refinement: " + pct + "%"
                    + " (+" + (int)(recipe.bloodRefinementGain * 100) + "%)"));

            // Sync cultivation state to client
            MeditationHandler.syncToClient(player);
        }

        // Apply HP drain — can kill
        float damage = player.getMaxHealth() * recipe.hpDrainFraction;
        player.hurt(level.damageSources().magic(), damage);

        // If player died from HP drain, that's canon
        if (!player.isAlive()) {
            player.sendSystemMessage(Component.literal(
                    "\u00A74Your body could not withstand the tempering..."));
        }

        setChanged();
        syncProgress();
    }

    private void syncProgress() {
        if (level == null || level.isClientSide()) return;
        float progress = tempering
                ? Math.min(1.0f, (float) sessionTimer / (float) SESSION_TICKS)
                : 0.0f;
        String recipeName = activeRecipe != null ? activeRecipe.displayName : "";
        double bloodRefinement = 0.0;
        if (temperingPlayer != null) {
            CultivationState state = CultivationCapability.get(temperingPlayer).orElse(null);
            if (state != null) bloodRefinement = state.getBloodRefinement();
        }
        // TODO: sync disabled
    }

    private java.util.List<ServerPlayer> getViewerPlayers() {
        java.util.List<ServerPlayer> viewers = new java.util.ArrayList<>();
        if (level == null || level.isClientSide()) return viewers;
        for (Player p : level.players()) {
            if (p instanceof ServerPlayer sp
                    && sp.containerMenu instanceof RefiningPoolMenu menu
                    && menu.getPool() == this) {
                viewers.add(sp);
            }
        }
        return viewers;
    }

    // ─── Accessors ────────────────────────────────────────────────────

    public float getSessionProgress() {
        return tempering ? Math.min(1.0f, (float) sessionTimer / (float) SESSION_TICKS) : 0.0f;
    }
    public boolean isTempering() { return tempering; }
    public ItemStackHandler getInventory() { return inventory; }
    @Nullable public BodyRefiningLogic.BodyRecipe getActiveRecipe() { return activeRecipe; }

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
        return Component.translatable("container.ergenverse.refining_pool");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new RefiningPoolMenu(containerId, playerInv, this);
    }
}