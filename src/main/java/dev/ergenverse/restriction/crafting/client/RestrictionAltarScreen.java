package dev.ergenverse.restriction.crafting.client;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.restriction.crafting.RestrictionAltarMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;


public class RestrictionAltarScreen extends AbstractContainerScreen<RestrictionAltarMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Ergenverse.MOD_ID, "textures/gui/restriction_altar.png");

    public RestrictionAltarScreen(RestrictionAltarMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        // TODO: render GUI texture when available
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
    }
}
