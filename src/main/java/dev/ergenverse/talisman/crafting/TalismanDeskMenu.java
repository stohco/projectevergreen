package dev.ergenverse.talisman.crafting;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

/**
 * Server-side menu for the Talisman Desk (\u7b26\u53f0).
 *
 * <p>Layout (5 desk slots + player inv):
 * <ul>
 *   <li>Slot 0: Primary input (blank paper / blank jade slip)</li>
 *   <li>Slot 1: Secondary input (catalyst)</li>
 *   <li>Slot 2: Spirit ink</li>
 *   <li>Slot 3: Extra material</li>
 *   <li>Slot 4: Output (talisman / jade slip)</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class TalismanDeskMenu extends AbstractContainerMenu {

    private static final DeferredRegister<MenuType<?>> MENU_REGISTER =
            DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, Ergenverse.MOD_ID);

    public static final RegistryObject<MenuType<TalismanDeskMenu>> TYPE =
            MENU_REGISTER.register("talisman_desk", () -> IForgeMenuType.create(
                    (containerId, playerInv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
                        if (be instanceof TalismanDeskBlockEntity desk) {
                            return new TalismanDeskMenu(containerId, playerInv, desk);
                        }
                        return null;
                    }));

    public static void register(IEventBus modEventBus) {
        MENU_REGISTER.register(modEventBus);
        Ergenverse.LOGGER.info("[Ergenverse] Talisman Desk menu type registered.");
    }

    public static final int DESK_SLOT_COUNT = 5;
    public static final int SLOT_PRIMARY = 0;
    public static final int SLOT_SECONDARY = 1;
    public static final int SLOT_INK = 2;
    public static final int SLOT_EXTRA = 3;
    public static final int SLOT_OUTPUT = 4;
    private static final int PLAYER_INV_START = DESK_SLOT_COUNT;
    private static final int PLAYER_HOTBAR_START = PLAYER_INV_START + 27;
    private static final int TOTAL_SLOTS = PLAYER_HOTBAR_START + 9;

    private final TalismanDeskBlockEntity desk;
    private final ContainerLevelAccess access;

    public TalismanDeskMenu(int containerId, Inventory playerInv, TalismanDeskBlockEntity desk) {
        super(TYPE.get(), containerId);
        this.desk = desk;
        this.access = ContainerLevelAccess.create(desk.getLevel(), desk.getBlockPos());

        ItemStackHandler inv = desk.getInventory();
        this.addSlot(new SlotItemHandler(inv, SLOT_PRIMARY, 44, 36));
        this.addSlot(new SlotItemHandler(inv, SLOT_SECONDARY, 66, 36));
        this.addSlot(new SlotItemHandler(inv, SLOT_INK, 88, 36));
        this.addSlot(new SlotItemHandler(inv, SLOT_EXTRA, 110, 36));
        this.addSlot(new SlotItemHandler(inv, SLOT_OUTPUT, 156, 36) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public TalismanDeskBlockEntity getDesk() { return desk; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack original = slot.getItem();
            itemStack = original.copy();
            if (index < DESK_SLOT_COUNT) {
                if (!this.moveItemStackTo(original, PLAYER_INV_START, TOTAL_SLOTS, true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(original, SLOT_PRIMARY, SLOT_OUTPUT, false)) return ItemStack.EMPTY;
            }
            if (original.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((lvl, pos) ->
                lvl.getBlockState(pos).getBlock() instanceof TalismanDeskBlock
                        && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0,
                true);
    }

    public boolean tryStartCraft(String recipeId, String category) {
        if (desk.isCrafting()) return false;
        int duration = 40; // 2s default
        String mode = TalismanCraftingLogic.MODE_TALISMAN;
        TalismanCraftingLogic.TalismanRecipe recipe =
                TalismanCraftingLogic.getRecipe(recipeId);
        if (recipe != null) {
            duration = recipe.durationTicks();
            mode = recipe.mode();
            if (category == null || category.isEmpty()) category = recipe.category();
        }
        desk.startCraft(recipeId, category, mode, duration);
        return true;
    }
}