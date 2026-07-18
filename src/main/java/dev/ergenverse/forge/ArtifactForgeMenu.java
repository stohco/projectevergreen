package dev.ergenverse.forge;

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
 * Server-side menu for the Artifact Forge (炼器炉).
 *
 * <p>Layout (5 forge slots + player inv):
 * <ul>
 *   <li>Slot 0: Blank item input</li>
 *   <li>Slot 1: Material input</li>
 *   <li>Slot 2: Catalyst input</li>
 *   <li>Slot 3: Fuel (spirit stones)</li>
 *   <li>Slot 4: Output (forged artifact)</li>
 * </ul>
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md §3.2 §13: duration 5-8s per craft.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class ArtifactForgeMenu extends AbstractContainerMenu {

    private static final DeferredRegister<MenuType<?>> MENU_REGISTER =
            DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, Ergenverse.MOD_ID);

    public static final RegistryObject<MenuType<ArtifactForgeMenu>> TYPE =
            MENU_REGISTER.register("artifact_forge", () -> IForgeMenuType.create(
                    (containerId, playerInv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
                        if (be instanceof ArtifactForgeBlockEntity forge) {
                            return new ArtifactForgeMenu(containerId, playerInv, forge);
                        }
                        return null;
                    }));

    public static void register(IEventBus modEventBus) {
        MENU_REGISTER.register(modEventBus);
        Ergenverse.LOGGER.info("[Ergenverse] Artifact Forge menu type registered.");
    }

    public static final int FORGE_SLOT_COUNT = 5;
    public static final int SLOT_BLANK = 0;
    public static final int SLOT_MATERIAL = 1;
    public static final int SLOT_CATALYST = 2;
    public static final int SLOT_FUEL = 3;
    public static final int SLOT_OUTPUT = 4;
    private static final int PLAYER_INV_START = FORGE_SLOT_COUNT;
    private static final int PLAYER_HOTBAR_START = PLAYER_INV_START + 27;
    private static final int TOTAL_SLOTS = PLAYER_HOTBAR_START + 9;

    private final ArtifactForgeBlockEntity forge;
    private final ContainerLevelAccess access;

    public ArtifactForgeMenu(int containerId, Inventory playerInv, ArtifactForgeBlockEntity forge) {
        super(TYPE.get(), containerId);
        this.forge = forge;
        this.access = ContainerLevelAccess.create(forge.getLevel(), forge.getBlockPos());

        ItemStackHandler inv = forge.getInventory();
        this.addSlot(new SlotItemHandler(inv, SLOT_BLANK, 44, 36));
        this.addSlot(new SlotItemHandler(inv, SLOT_MATERIAL, 66, 36));
        this.addSlot(new SlotItemHandler(inv, SLOT_CATALYST, 88, 36));
        this.addSlot(new SlotItemHandler(inv, SLOT_FUEL, 110, 36));
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

    public ArtifactForgeBlockEntity getForge() { return forge; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack original = slot.getItem();
            itemStack = original.copy();
            if (index < FORGE_SLOT_COUNT) {
                if (!this.moveItemStackTo(original, PLAYER_INV_START, TOTAL_SLOTS, true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(original, SLOT_BLANK, SLOT_OUTPUT, false)) return ItemStack.EMPTY;
            }
            if (original.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((lvl, pos) ->
                lvl.getBlockState(pos).getBlock() instanceof dev.ergenverse.forge.ArtifactForgeBlock
                        && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0,
                true);
    }

    public boolean tryStartCraft(String recipeId, String mode) {
        if (forge.isCrafting()) return false;
        // Pull duration from the recipe (default 5s = 100 ticks).
        int duration = 100;
        dev.ergenverse.forge.ArtifactForgeCraftingLogic.ForgeRecipe recipe =
                dev.ergenverse.forge.ArtifactForgeCraftingLogic.getRecipe(recipeId);
        if (recipe != null) {
            duration = recipe.durationTicks();
            // Use the recipe's own mode if the caller didn't specify.
            if (mode == null || mode.isEmpty()) mode = recipe.mode();
        }
        forge.startCraft(recipeId, mode, duration);
        return true;
    }
}