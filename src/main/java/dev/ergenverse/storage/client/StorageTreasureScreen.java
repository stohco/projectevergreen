package dev.ergenverse.storage.client;

import dev.ergenverse.storage.StorageTreasureMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client-side screen for storage treasures (\u50a8\u7269\u6cd5\u5b9d).
 *
 * <p>Dynamically sized based on the number of storage slots.
 * Uses the vanilla chest/shulker box texture pattern.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class StorageTreasureScreen extends net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<StorageTreasureMenu> {

    private static final ResourceLocation CONTAINER_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    private final int storageRows;

    public StorageTreasureScreen(StorageTreasureMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.storageRows = (menu.getStorageSlotCount() + 8) / 9;
        // Dynamically size: header(17) + storage rows + gap(10) + player inv(3 rows) + hotbar(1 row)
        this.imageWidth = 176;
        this.imageHeight = 114 + storageRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // Render container background (generic_54 texture supports variable height)
        // The vanilla generic_54 texture is 176×184 with 6 rows; we need more rows
        // Use the inventory texture as fallback
        gfx.blit(CONTAINER_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // If we have more rows than the default 6, draw additional rows
        if (storageRows > 6) {
            // Draw extra rows by tiling the middle section of the texture
            for (int extra = 6; extra < storageRows; extra++) {
                int rowY = y + 17 + extra * 18;
                // Reuse row texture from the inventory area
                gfx.blit(CONTAINER_TEXTURE, x, rowY, 0, 17, this.imageWidth, 18);
            }
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        gfx.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}