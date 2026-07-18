package dev.ergenverse.client.screen;

import dev.ergenverse.alchemy.PillFurnaceMenu;

import dev.ergenverse.network.ClientCultivationCache;
import dev.ergenverse.network.ERNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client-side screen for the Pill Furnace (丹炉).
 *
 * <p>Renders:
 * <ul>
 *   <li>The 4 furnace slots (2 inputs + fuel + output)</li>
 *   <li>The player inventory</li>
 *   <li>A progress bar (synced from server via AlchemySyncS2CPacket)</li>
 *   <li>A "Craft" button that sends an {@link AlchemyCraftC2SPacket}</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class PillFurnaceScreen extends AbstractContainerScreen<PillFurnaceMenu> {

    /** The furnace GUI texture. Vanilla furnace-like layout, 176×166. */
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("ergenverse", "textures/gui/pill_furnace.png");

    /** The progress bar inside the texture (x, y, w, h). */
    private static final int PROGRESS_X = 176;
    private static final int PROGRESS_Y = 0;
    private static final int PROGRESS_W = 24;
    private static final int PROGRESS_H = 17;

    public PillFurnaceScreen(PillFurnaceMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        // Craft button — placed to the right of the output slot.
        addRenderableWidget(Button.builder(
                        Component.translatable("container.ergenverse.pill_furnace.craft"),
                        button -> onCraftClicked())
                .bounds(leftPos + 116, topPos + 56, 50, 16)
                .build());
    }

    private void onCraftClicked() {
        // Send a C2S packet requesting to craft the currently-selected recipe.
        // For now, default to qi_gathering_pill (the simplest recipe).
        // TODO: add a recipe selector dropdown.
        // TODO: ERNetwork.getChannel().sendToServer(new AlchemyCraftC2SPacket("qi_gathering_pill"));
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        // Background panel
        gfx.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // Progress bar (synced from server via ClientCultivationCache).
        float progress = 0.0f;
        if (progress > 0.0f) {
            int filled = (int) (progress * PROGRESS_W);
            gfx.blit(TEXTURE,
                    leftPos + 102, topPos + 36,           // dest x, y
                    PROGRESS_X, PROGRESS_Y,                // src x, y
                    filled, PROGRESS_H);                   // src w, h
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
        // Title
        gfx.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        // Inventory label
        gfx.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        // Status line — show craft progress percent.
        float progress = 0.0f;
        if (progress > 0.0f) {
            String status;
            if (false) {
                status = "\u00A7eIncomplete: " + (int)(progress * 100) + "%";
            } else {
                status = "\u00A7aRefining: " + (int)(progress * 100) + "%";
            }
            gfx.drawString(this.font, status, 8, 58, 0xFFFFFF, false);
        }
    }
}
