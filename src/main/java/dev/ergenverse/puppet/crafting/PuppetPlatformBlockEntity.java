package dev.ergenverse.puppet.crafting;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for the Puppet Platform (傀儡台) — puppet refining workstation.
 *
 * <p>3-slot inventory via Forge's {@link ItemStackHandler}:
 * <ul>
 *   <li>Slot 0: Soul Fragment (the target soul to be refined into a puppet)</li>
 *   <li>Slot 1: Frame Material (copper, iron, thunder essence, etc.)</li>
 *   <li>Slot 2: Fuel (spirit stone)</li>
 * </ul>
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md §3.6:
 * <ul>
 *   <li>10-second refining session (200 ticks) — longer than other crafts because dark art.</li>
 *   <li>On completion: consumes soul + frame + fuel, outputs a puppet_flag with tier NBT.</li>
 *   <li>Karma cost applied to player — this is a demonic art.</li>
 *   <li>Realm-gated per recipe tier.</li>
 * </ul>
 *
 * <p>Canon: Wang Lin's Immortal Guards — "Not mere puppets, but transformed cultivators,
 * born from a ruthless and nearly-lost art." The Emperor Furnace "captures and refines all things."
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class PuppetPlatformBlockEntity extends BlockEntity implements MenuProvider, Container {

    public static final int CONTAINER_SIZE = 3;
    public static final int SLOT_SOUL = 0;
    public static final int SLOT_FRAME = 1;
    public static final int SLOT_FUEL = 2;

    /** 10 seconds per refining session — dark art takes longer. */
    private static final int SESSION_TICKS = 200;

    private final ItemStackHandler inventory = new ItemStackHandler(CONTAINER_SIZE) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    private int sessionTimer;
    private boolean refining;
    @Nullable private ServerPlayer refiningPlayer;
    @Nullable private PuppetRefiningLogic.PuppetRecipe activeRecipe;

    public PuppetPlatformBlockEntity(BlockPos pos, BlockState state) {
        super(ErgenverseBlocks.PUPPET_PLATFORM_ENTITY.get(), pos, state);
    }

    // ─── NBT ──────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("SessionTimer", sessionTimer);
        tag.putBoolean("Refining", refining);
        if (activeRecipe != null) {
            tag.putString("ActiveRecipe", activeRecipe.recipeId);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) inventory.deserializeNBT(tag.getCompound("Inventory"));
        sessionTimer = tag.getInt("SessionTimer");
        refining = tag.getBoolean("Refining");
        String recipeId = tag.getString("ActiveRecipe");
        for (PuppetRefiningLogic.PuppetRecipe r : PuppetRefiningLogic.allRecipes()) {
            if (r.recipeId.equals(recipeId)) {
                activeRecipe = r;
                break;
            }
        }
    }

    // ─── Refining Session ────────────────────────────────────────────

    /**
     * Start a puppet refining session. Validates inputs, realm gate, and karma.
     */
    public boolean startSession(ServerPlayer player) {
        if (refining) return false;

        ItemStack soulStack = inventory.getStackInSlot(SLOT_SOUL);
        ItemStack frameStack = inventory.getStackInSlot(SLOT_FRAME);
        ItemStack fuelStack = inventory.getStackInSlot(SLOT_FUEL);

        if (soulStack.isEmpty()) {
            player.sendSystemMessage(Component.literal("\u00A7cPlace a soul fragment in the first slot."));
            return false;
        }
        if (frameStack.isEmpty()) {
            player.sendSystemMessage(Component.literal("\u00A7cPlace a frame material in the second slot."));
            return false;
        }
        if (fuelStack.isEmpty()) {
            player.sendSystemMessage(Component.literal("\u00A7cPlace a spirit stone as fuel in the third slot."));
            return false;
        }

        // Find matching recipe
        PuppetRefiningLogic.PuppetRecipe recipe = PuppetRefiningLogic.findRecipe(
                soulStack.getItem(), frameStack.getItem());
        if (recipe == null) {
            player.sendSystemMessage(Component.literal("\u00A7cNo puppet recipe matches these inputs."));
            return false;
        }

        // Get player realm
        CultivationState state = CultivationCapability.get(player).orElse(null);
        if (state == null) return false;

        int playerOrdinal = state.getCurrentRealm().ordinal();
        if (playerOrdinal < recipe.minRealmOrdinal) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cPuppet refining of this tier requires a higher cultivation realm."));
            return false;
        }

        // Karma check — dark art, but we don't block on karma.
        // Instead, warn the player of the karmic cost.
        if (state.getKarma() >= 0.90) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7c\u00A7lYour karma is already critically dark. Refining further will have severe consequences."));
            // Don't block — Wang Lin did it anyway. Just warn.
        }

        this.activeRecipe = recipe;
        this.sessionTimer = 0;
        this.refining = true;
        this.refiningPlayer = player;
        setChanged();

        player.sendSystemMessage(Component.literal(
                "\u00A78\u00A7lDark art invoked... The soul twists and screams."));
        return true;
    }

    public void tickSession(Level level) {
        if (!refining) return;
        sessionTimer++;

        if (sessionTimer >= SESSION_TICKS) {
            completeSession(level);
        }
        setChanged();
        syncProgress();
    }

    private void completeSession(Level level) {
        ServerPlayer player = refiningPlayer;
        PuppetRefiningLogic.PuppetRecipe recipe = activeRecipe;
        refining = false;
        refiningPlayer = null;
        activeRecipe = null;
        sessionTimer = 0;

        if (player == null || level == null || recipe == null) return;

        CultivationState state = CultivationCapability.get(player).orElse(null);

        // Consume inputs
        inventory.getStackInSlot(SLOT_SOUL).shrink(1);
        if (inventory.getStackInSlot(SLOT_SOUL).isEmpty()) {
            inventory.setStackInSlot(SLOT_SOUL, ItemStack.EMPTY);
        }
        inventory.getStackInSlot(SLOT_FRAME).shrink(1);
        if (inventory.getStackInSlot(SLOT_FRAME).isEmpty()) {
            inventory.setStackInSlot(SLOT_FRAME, ItemStack.EMPTY);
        }
        inventory.getStackInSlot(SLOT_FUEL).shrink(1);
        if (inventory.getStackInSlot(SLOT_FUEL).isEmpty()) {
            inventory.setStackInSlot(SLOT_FUEL, ItemStack.EMPTY);
        }

        // Create output: puppet_flag with tier NBT
        ItemStack output = new ItemStack(recipe.outputItem);
        CompoundTag tag = output.getOrCreateTag();
        tag.putString("PuppetTier", recipe.puppetTier);
        tag.putInt("PuppetPower", recipe.puppetPower);
        tag.putString("PuppetName", recipe.displayName);

        // Try to insert into the world (drop if no room in player inv)
        if (!player.getInventory().add(output)) {
            net.minecraft.world.Containers.dropItemStack(level,
                    worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), output);
        }

        // Apply karma cost
        if (state != null) {
            state.addKarma(recipe.karmaCost);
            player.sendSystemMessage(Component.literal(
                    "\u00A78\u00A7lPuppet refined: " + recipe.displayName
                    + " (Power: " + recipe.puppetPower + ")"
                    + "\u00A7c Karma darkened by " + (int)(recipe.karmaCost * 100) + "%."));
        }

        setChanged();
        syncProgress();
    }

    private void syncProgress() {
        if (level == null || level.isClientSide()) return;
        float progress = refining
                ? Math.min(1.0f, (float) sessionTimer / (float) SESSION_TICKS)
                : 0.0f;
        String recipeName = activeRecipe != null ? activeRecipe.displayName : "";
        // TODO: sync disabled
    }

    private java.util.List<ServerPlayer> getViewerPlayers() {
        java.util.List<ServerPlayer> viewers = new java.util.ArrayList<>();
        if (level == null || level.isClientSide()) return viewers;
        for (Player p : level.players()) {
            if (p instanceof ServerPlayer sp
                    && sp.containerMenu instanceof dev.ergenverse.puppet.crafting.PuppetPlatformMenu menu
                    && true) {
                viewers.add(sp);
            }
        }
        return viewers;
    }

    // ─── Accessors ────────────────────────────────────────────────────

    public float getSessionProgress() {
        return refining ? Math.min(1.0f, (float) sessionTimer / (float) SESSION_TICKS) : 0.0f;
    }
    public boolean isRefining() { return refining; }
    public ItemStackHandler getInventory() { return inventory; }
    @Nullable public PuppetRefiningLogic.PuppetRecipe getActiveRecipe() { return activeRecipe; }

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
        return Component.translatable("container.ergenverse.puppet_platform");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new dev.ergenverse.puppet.crafting.PuppetPlatformMenu(containerId, playerInv, this);
    }

    public ContainerData getData() { return new net.minecraft.world.inventory.SimpleContainerData(1); }
}
