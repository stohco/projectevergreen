package dev.ergenverse.soul.crafting;

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
 * Block entity for the Soul Refining Cauldron (炼魂釜) — demonic art
 * workstation for refining raw soul fragments into refined soul energy
 * stored in soul banners.
 *
 * <p>3-slot inventory via Forge's {@link ItemStackHandler}:
 * <ul>
 *   <li>Slot 0: Raw Soul Fragment — consumed on refine</li>
 *   <li>Slot 1: Soul Banner (vessel) — updated in place with soul_count NBT</li>
 *   <li>Slot 2: Output (unused — banner updates in place)</li>
 * </ul>
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md §3.9:
 * <ul>
 *   <li>6-second refining session (120 ticks).</li>
 *   <li>Requires at least Core Formation realm.</li>
 *   <li>Karma check: refuses if player karma >= 0.90 (heart demon risk).</li>
 *   <li>Each refine adds karma (demonic art).</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class SoulRefiningCauldronBlockEntity extends BlockEntity implements MenuProvider, Container {

    public static final int CONTAINER_SIZE = 3;
    public static final int SLOT_RAW_SOUL = 0;
    public static final int SLOT_BANNER = 1;
    public static final int SLOT_OUTPUT = 2;

    /** 6 seconds per refining session per §13 rebalance. */
    private static final int SESSION_TICKS = 120;

    private final ItemStackHandler inventory = new ItemStackHandler(CONTAINER_SIZE) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    private int sessionTimer;
    private boolean refining;
    @Nullable private ServerPlayer refiningPlayer;
    @Nullable private SoulRefiningLogic.SoulRecipe activeRecipe;

    public SoulRefiningCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(dev.ergenverse.block.ErgenverseBlocks.SOUL_REFINING_CAULDRON_ENTITY.get(), pos, state);
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
        activeRecipe = SoulRefiningLogic.findRecipe(
                net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                        new net.minecraft.resources.ResourceLocation("ergenverse", recipeId)));
        // If the recipe item doesn't match, set null — recipe lookup is by item, not ID string
        // Re-derive by scanning all recipes
        for (SoulRefiningLogic.SoulRecipe r : SoulRefiningLogic.allRecipes()) {
            if (r.recipeId.equals(recipeId)) {
                activeRecipe = r;
                break;
            }
        }
    }

    // ─── Refining Session ────────────────────────────────────────────

    /**
     * Start a soul refining session. Validates inputs, realm gate, and karma.
     */
    public boolean startSession(ServerPlayer player) {
        if (refining) return false;

        ItemStack rawSoulStack = inventory.getStackInSlot(SLOT_RAW_SOUL);
        ItemStack bannerStack = inventory.getStackInSlot(SLOT_BANNER);

        if (rawSoulStack.isEmpty()) {
            player.sendSystemMessage(Component.literal("\u00A7cPlace a raw soul fragment in the first slot."));
            return false;
        }
        if (bannerStack.isEmpty()) {
            player.sendSystemMessage(Component.literal("\u00A7cPlace a soul banner in the second slot."));
            return false;
        }

        // Find matching recipe
        SoulRefiningLogic.SoulRecipe recipe = SoulRefiningLogic.findRecipe(rawSoulStack.getItem());

        if (recipe == null) {
            player.sendSystemMessage(Component.literal("\u00A7cNo refining recipe for this soul fragment."));
            return false;
        }

        // Check realm: requires at least Core Formation (soul refining is advanced)
        CultivationState state = CultivationCapability.get(player).orElse(null);
        if (state == null) return false;
        RealmId playerRealm = state.getCurrentRealm();

        if (!playerRealm.isAtLeast(RealmId.CORE_FORMATION)) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cSoul refining requires at least Core Formation realm."));
            return false;
        }

        // Check karma: refuse if karma >= 0.90 (heart demon risk)
        if (state.getKarma() >= 0.90) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cKarma too high \u2014 heart demon risk! Purify your karma first."));
            return false;
        }

        this.activeRecipe = recipe;
        this.sessionTimer = 0;
        this.refining = true;
        this.refiningPlayer = player;
        setChanged();
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
        SoulRefiningLogic.SoulRecipe recipe = activeRecipe;
        refining = false;
        refiningPlayer = null;
        activeRecipe = null;
        sessionTimer = 0;

        if (player == null || level == null || recipe == null) return;

        // Consume raw soul fragment
        inventory.getStackInSlot(SLOT_RAW_SOUL).shrink(1);
        if (inventory.getStackInSlot(SLOT_RAW_SOUL).isEmpty()) {
            inventory.setStackInSlot(SLOT_RAW_SOUL, ItemStack.EMPTY);
        }

        // Add souls to banner NBT
        ItemStack banner = inventory.getStackInSlot(SLOT_BANNER);
        int currentSouls = banner.getOrCreateTag().getInt("soul_count");
        int newSouls = currentSouls + recipe.soulsAdded;
        banner.getOrCreateTag().putInt("soul_count", newSouls);

        // Add karma to player (demonic art)
        CultivationState state = CultivationCapability.get(player).orElse(null);
        if (state != null) {
            state.addKarma(recipe.karmaCost);
        }

        player.sendSystemMessage(Component.literal(
                "\u00A75\u00A7lSoul refined! +" + recipe.soulsAdded
                + " souls (total: " + newSouls + "). Karma +" + recipe.karmaCost));

        // Check tier thresholds
        if (newSouls >= 10_000_000_000L && currentSouls < 10_000_000_000L) {
            player.sendSystemMessage(Component.literal(
                    "\u00A76\u00A7lThe soul banner resonates with ten billion souls! Tier ascended!"));
        } else if (newSouls >= 999 && currentSouls < 999) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7d\u00A7lThe soul banner has reached its base capacity (999)."));
        }

        setChanged();
        syncProgress();
    }

    private void syncProgress() {
        if (level == null || level.isClientSide()) return;
        float progress = refining
                ? Math.min(1.0f, (float) sessionTimer / (float) SESSION_TICKS)
                : 0.0f;
        String recipeName = activeRecipe != null ? activeRecipe.recipeId : "";
        int bannerSoulCount = 0;
        ItemStack banner = inventory.getStackInSlot(SLOT_BANNER);
        if (!banner.isEmpty() && banner.hasTag()) {
            bannerSoulCount = banner.getTag().getInt("soul_count");
        }
        // TODO: sync disabled
    }

    private java.util.List<ServerPlayer> getViewerPlayers() {
        java.util.List<ServerPlayer> viewers = new java.util.ArrayList<>();
        if (level == null || level.isClientSide()) return viewers;
        for (Player p : level.players()) {
            if (p instanceof ServerPlayer sp
                    && sp.containerMenu instanceof dev.ergenverse.soul.crafting.SoulRefiningCauldronMenu menu
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
    @Nullable public SoulRefiningLogic.SoulRecipe getActiveRecipe() { return activeRecipe; }

    public int getBannerSoulCount() {
        ItemStack banner = inventory.getStackInSlot(SLOT_BANNER);
        if (!banner.isEmpty() && banner.hasTag()) {
            return banner.getTag().getInt("soul_count");
        }
        return 0;
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
        return Component.translatable("container.ergenverse.soul_refining_cauldron");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new dev.ergenverse.soul.crafting.SoulRefiningCauldronMenu(containerId, playerInv, this);
    }

    public ContainerData getData() { return new net.minecraft.world.inventory.SimpleContainerData(1); }
}
