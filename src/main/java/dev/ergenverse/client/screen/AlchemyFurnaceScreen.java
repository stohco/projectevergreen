package dev.ergenverse.client.screen;

import dev.ergenverse.block.entity.AlchemyFurnaceBlockEntity;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.screen.AlchemyFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * AlchemyFurnaceScreen — the client-side renderer for the Alchemy Furnace GUI.
 *
 * <p>Renders:
 * <ul>
 *   <li>The dark-stone GUI background (176×166 region of the texture)</li>
 *   <li>The flame icon (14×14, drawn between the fuel slot and the
 *       ingredient grid, with its visible height proportional to remaining
 *       fuel)</li>
 *   <li>The progress arrow (24×17, drawn between the ingredient grid and the
 *       output slot, with its visible width proportional to craft progress)</li>
 *   <li>The title text "Alchemy Furnace" and the player-inventory label</li>
 * </ul>
 *
 * <p>The flame and arrow live in an off-screen region of the same texture
 * (at x=176, y=0 for the flame and x=176, y=14 for the arrow). They are
 * blitted from there onto the visible GUI.
 *
 * <p>Registered on the client via
 * {@code MenuScreens.register(ErgenverseMenus.ALCHEMY_FURNACE.get(),
 * AlchemyFurnaceScreen::new)} in {@code ClientEvents} (on the
 * {@code FMLClientSetupEvent}).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public class AlchemyFurnaceScreen extends AbstractContainerScreen<AlchemyFurnaceMenu> {

    /** The GUI texture. 200×200 region with 176×166 main + flame + arrow. */
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Ergenverse.MOD_ID, "textures/gui/alchemy_furnace.png");

    // ── Off-screen sprite coordinates (inside the texture) ──────────────
    /** Flame sprite is 14×14 at (176, 0) of the texture. */
    private static final int FLAME_U = 176;
    private static final int FLAME_V = 0;
    private static final int FLAME_W = 14;
    private static final int FLAME_H = 14;

    /** Arrow sprite is 24×17 at (176, 14) of the texture. */
    private static final int ARROW_U = 176;
    private static final int ARROW_V = 14;
    private static final int ARROW_W = 24;
    private static final int ARROW_H = 17;

    // ── On-screen sprite positions (relative to the GUI top-left) ───────
    /** Flame is drawn just right of the fuel slot, vertically centered. */
    private static final int FLAME_X = 27;
    private static final int FLAME_Y = 37;

    /** Arrow is drawn between the ingredient grid and the output slot,
     *  vertically centered on the bottom ingredient row. */
    private static final int ARROW_X = 98;
    private static final int ARROW_Y = 35;

    public AlchemyFurnaceScreen(AlchemyFurnaceMenu menu, Inventory playerInv,
                                Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94; // standard vanilla offset
    }

    @Override
    protected void init() {
        super.init();
        // Move the title to the standard "upper-left" position.
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    // ── Background render ───────────────────────────────────────────────
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick,
                            int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight);

        // Flame: visible height proportional to fuelRemaining/fuelMax.
        int fuelHeight = this.menu.getScaledFuelRemaining(FLAME_H);
        if (fuelHeight > 0) {
            // Draw the bottom fuelHeight pixels of the flame sprite.
            int vOffset = FLAME_H - fuelHeight;
            graphics.blit(TEXTURE,
                    this.leftPos + FLAME_X,
                    this.topPos + FLAME_Y + vOffset,
                    FLAME_U, FLAME_V + vOffset,
                    FLAME_W, fuelHeight);
        }

        // Arrow: visible width proportional to craftProgress/CRAFT_TIME.
        int arrowWidth = this.menu.getScaledCraftProgress(ARROW_W);
        if (arrowWidth > 0) {
            graphics.blit(TEXTURE,
                    this.leftPos + ARROW_X,
                    this.topPos + ARROW_Y,
                    ARROW_U, ARROW_V,
                    arrowWidth, ARROW_H);
        }
    }

    // ── Foreground (labels) render ──────────────────────────────────────
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Title (drawn at titleLabelX/Y)
        graphics.drawString(this.font, this.title, this.titleLabelX,
                this.titleLabelY, 0x404040, false);
        // Player inventory label
        graphics.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    // ── Full render (background + slots + tooltips) ─────────────────────
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
