package dev.ergenverse.client.screen;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.screen.TalismanDeskMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * TalismanDeskScreen — renders the talisman desk UI.
 * Layout: [Paper] [Ink] [Intent] → [Output]
 * Uses the alchemy furnace GUI texture as base (similar dark stone style).
 */
public class TalismanDeskScreen extends AbstractContainerScreen<TalismanDeskMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Ergenverse.MOD_ID, "textures/gui/talisman_desk.png");

    public TalismanDeskScreen(TalismanDeskMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        // Progress arrow
        int progress = this.menu.getProgressScaled();
        if (progress > 0) {
            graphics.blit(TEXTURE, this.leftPos + 99, this.topPos + 34, 176, 0, progress, 17);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}
