package dev.ergenverse.storage;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Base class for storage treasures (\u50a8\u7269\u6cd5\u5b9d) — items that hold
 * extra inventory slots in their NBT.
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md \u00a73.7:
 * <ul>
 *   <li>Storage Pouch: 9-27 slots (starter)</li>
 *   <li>Space Ring: 27-54 slots (higher tier)</li>
 *   <li>Right-click opens an expanded inventory UI</li>
 *   <li>Inventory is NBT-stored on the item — pick it up, everything comes with you</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class StorageTreasureItem extends Item {

    /** How many extra slots this storage treasure provides. */
    private final int slotCount;

    /** Display name component key. */
    private final String nameKey;

    public StorageTreasureItem(int slotCount, String nameKey, Properties properties) {
        super(properties.stacksTo(1));
        this.slotCount = slotCount;
        this.nameKey = nameKey;
    }

    public int getSlotCount() { return slotCount; }

    // ── NBT Inventory helpers ──────────────────────────────────────

    private static final String TAG_ITEMS = "Items";
    private static final String TAG_SLOT = "Slot";

    /** Read the stored inventory from item NBT into a SimpleContainer. */
    public static SimpleContainer getInventory(ItemStack stack, int expectedSlots) {
        SimpleContainer container = new SimpleContainer(expectedSlots);
        if (!stack.hasTag()) return container;
        CompoundTag tag = stack.getTag();
        if (!tag.contains(TAG_ITEMS)) return container;
        ListTag list = tag.getList(TAG_ITEMS, 10); // 10 = COMPOUND
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getInt(TAG_SLOT);
            if (slot >= 0 && slot < expectedSlots) {
                container.setItem(slot, ItemStack.of(itemTag));
            }
        }
        return container;
    }

    /** Write a SimpleContainer's contents back to the item's NBT. */
    public static void saveInventory(ItemStack stack, SimpleContainer container) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = new ListTag();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack item = container.getItem(i);
            if (!item.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt(TAG_SLOT, i);
                item.save(itemTag);
                list.add(itemTag);
            }
        }
        tag.put(TAG_ITEMS, list);
        stack.setTag(tag);
    }

    // ── Right-click to open ─────────────────────────────────────────

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            SimpleContainer inv = getInventory(stack, slotCount);
            MenuProvider provider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return stack.getHoverName();
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player p) {
                    return new StorageTreasureMenu(containerId, playerInv, inv, slotCount, stack);
                }
            };
            net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer, provider, buf -> {
                buf.writeVarInt(slotCount);
                buf.writeVarInt(hand == InteractionHand.MAIN_HAND ? 0 : 1);
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Sparkle effect for storage treasures
        return true;
    }
}