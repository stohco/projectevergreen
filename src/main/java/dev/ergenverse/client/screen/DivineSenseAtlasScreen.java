package dev.ergenverse.client.screen;

import dev.ergenverse.client.AtlasClientEvents;
import dev.ergenverse.network.ERNetwork;
import dev.ergenverse.perception.atlas.AtlasEntry;
import dev.ergenverse.perception.atlas.AtlasNetworkPackets;
import dev.ergenverse.simulation.opportunity.PlayerObserverRealm;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * DivineSenseAtlasScreen — the M-key 4-layer perception map UI.
 *
 * <p>Per PROJECT_MASTER.md §5.5, this is a <b>perception record</b>, not a
 * satellite view. It renders ONLY entries the player has genuinely acquired
 * (synced via {@link AtlasNetworkPackets.AtlasSyncS2CPacket} from the
 * server's {@link dev.ergenverse.perception.atlas.DivineSenseAtlas}).
 *
 * <h2>Layout</h2>
 * <ul>
 *   <li>Centered 256×256 grid representing a 16-chunk (256-block) area
 *       centered on the player. Each grid cell = 1 block.</li>
 *   <li>4 layer toggle buttons in the top-right corner. Only enabled if
 *       the player's tier permits that layer (disabled + grayed-out
 *       otherwise). Clicking a layer sets the active filter.</li>
 *   <li>"All layers" radio button below the toggles (shows everything
 *       the player can perceive across all unlocked layers).</li>
 *   <li>Player's current position marked with a bright ▲ icon at the
 *       center of the grid.</li>
 *   <li>Hover tooltip on each entry shows: label, category, layer, note,
 *       discovered-via, world-tick observed.</li>
 *   <li>Title: "Divine Sense Atlas — Layer: &lt;Physical/Qi/Restriction/Law&gt;"</li>
 * </ul>
 *
 * <h2>Graceful degradation</h2>
 * <p>If no sync has been received yet (player just logged in, or capability
 * missing on the server), the screen renders an empty grid with a hint
 * message. It NEVER crashes — see {@link AtlasClientEvents#receiveSync}
 * for the sync-side defensive guards.</p>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class DivineSenseAtlasScreen extends Screen {

    /** Grid size: 256×256 pixels = 16×16 chunks (1 block per pixel). */
    private static final int GRID_SIZE = 256;
    /** Each grid cell is 1 pixel (so 256 cells per axis). */
    private static final int CELL_PX = 1;

    /** Active layer filter: 0 = all, 1-4 = specific layer. */
    private int activeLayerFilter = 0;

    /** Cached snapshot of the synced atlas state (from AtlasClientEvents). */
    private List<AtlasEntry> cachedEntries = new ArrayList<>();
    private PlayerObserverRealm cachedTier = PlayerObserverRealm.MORTAL;

    public DivineSenseAtlasScreen() {
        super(Component.translatable("ergenverse.atlas.title"));
    }

    @Override
    protected void init() {
        // Pull latest cached state at open-time so the screen reflects
        // the most recent sync packet.
        cachedEntries = AtlasClientEvents.getClientEntries();
        cachedTier = AtlasClientEvents.getClientTier();

        // ── 4 layer toggle buttons (top-right corner) ──
        int btnX = this.width - 110;
        int btnY = 30;
        addLayerButton(btnX, btnY,       "All Layers", 0);
        addLayerButton(btnX, btnY + 22,  "1: Physical", 1);
        addLayerButton(btnX, btnY + 44,  "2: Qi", 2);
        addLayerButton(btnX, btnY + 66,  "3: Restriction", 3);
        addLayerButton(btnX, btnY + 88,  "4: Law", 4);

        // ── Refresh button ──
        addRenderableWidget(Button.builder(
                Component.literal("Refresh"),
                button -> ERNetwork.getChannel().sendToServer(
                        new AtlasNetworkPackets.AtlasRequestC2SPacket()))
                .bounds(btnX, btnY + 120, 100, 18)
                .build());

        // ── Close button ──
        addRenderableWidget(Button.builder(
                Component.literal("Close"),
                button -> this.onClose())
                .bounds(btnX, this.height - 28, 100, 18)
                .build());
    }

    private void addLayerButton(int x, int y, String label, int layer) {
        boolean unlocked = isLayerUnlocked(layer);
        boolean active = (activeLayerFilter == layer);
        Button btn = Button.builder(
                Component.literal((active ? "▶ " : "  ") + label),
                b -> {
                    if (unlocked) {
                        activeLayerFilter = layer;
                        this.clearWidgets();
                        this.init();
                    }
                })
                .bounds(x, y, 100, 18)
                .build();
        btn.active = unlocked;
        addRenderableWidget(btn);
    }

    /** Per AtlasEntry.getLayerForRealm: which layers can this player perceive? */
    private boolean isLayerUnlocked(int layer) {
        if (layer == 0) return true; // "All Layers" is always available
        if (cachedTier == null) return false;
        switch (layer) {
            case 1: return cachedTier.isAtLeast(PlayerObserverRealm.MORTAL);
            case 2: return cachedTier.isAtLeast(PlayerObserverRealm.FOUNDATION);
            case 3: return cachedTier.isAtLeast(PlayerObserverRealm.NASCENT_SOUL);
            case 4: return cachedTier.isAtLeast(PlayerObserverRealm.ASCENDANT_PLUS);
            default: return false;
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        // Re-pull cached state on every frame (cheap) so live syncs show up.
        cachedEntries = AtlasClientEvents.getClientEntries();
        cachedTier = AtlasClientEvents.getClientTier();

        this.renderBackground(gfx);

        // Grid origin: centered horizontally, slightly below title.
        int gridX = (this.width - GRID_SIZE) / 2;
        int gridY = 40;

        // Background of the grid (dark slate)
        gfx.fill(gridX - 2, gridY - 2, gridX + GRID_SIZE + 2, gridY + GRID_SIZE + 2, 0xFF202028);
        gfx.fill(gridX, gridY, gridX + GRID_SIZE, gridY + GRID_SIZE, 0xFF101018);

        // Faint grid lines every 16 blocks (chunk boundaries)
        for (int i = 0; i <= 16; i++) {
            int lineX = gridX + i * 16;
            int lineY = gridY + i * 16;
            gfx.fill(lineX, gridY, lineX + 1, gridY + GRID_SIZE, 0xFF303040);
            gfx.fill(gridX, lineY, gridX + GRID_SIZE, lineY + 1, 0xFF303040);
        }

        // Player position (center of grid)
        LocalPlayer player = Minecraft.getInstance().player;
        int playerGridX = gridX + GRID_SIZE / 2;
        int playerGridY = gridY + GRID_SIZE / 2;
        if (player != null) {
            gfx.fill(playerGridX - 3, playerGridY - 3, playerGridX + 4, playerGridY + 4, 0xFFFFFFFF);
            gfx.fill(playerGridX - 1, playerGridY - 5, playerGridX + 2, playerGridY - 3, 0xFFFFFFFF);
        }

        // Render each visible entry as a colored dot
        BlockPos playerPos = (player != null) ? player.blockPosition() : BlockPos.ZERO;
        AtlasEntry hovered = null;
        for (AtlasEntry e : cachedEntries) {
            // Apply active layer filter
            if (activeLayerFilter != 0 && e.layer != activeLayerFilter) continue;
            // Apply tier filter (defensive — server should already pre-filter)
            if (!e.getLayerForRealm(cachedTier)) continue;

            int dx = e.pos.getX() - playerPos.getX();
            int dz = e.pos.getZ() - playerPos.getZ();
            // Skip entries outside the 256-block view
            if (Math.abs(dx) > 128 || Math.abs(dz) > 128) continue;

            int dotX = playerGridX + dx - 2;
            int dotY = playerGridY + dz - 2;
            int color = colorForLayer(e.layer);
            // 4×4 dot
            gfx.fill(dotX, dotY, dotX + 4, dotY + 4, color);
            // Outline for visibility
            gfx.fill(dotX, dotY, dotX + 4, dotY + 1, 0xFF000000);
            gfx.fill(dotX, dotY + 3, dotX + 4, dotY + 4, 0xFF000000);
            gfx.fill(dotX, dotY, dotX + 1, dotY + 4, 0xFF000000);
            gfx.fill(dotX + 3, dotY, dotX + 4, dotY + 4, 0xFF000000);

            // Hover detection (5×5 hitbox)
            if (mouseX >= dotX - 1 && mouseX < dotX + 5
                    && mouseY >= dotY - 1 && mouseY < dotY + 5) {
                hovered = e;
            }
        }

        // Title
        String layerName = activeLayerFilter == 0
                ? "All (" + cachedTier.label + ")"
                : AtlasEntry.layerName(activeLayerFilter);
        Component title = Component.literal("Divine Sense Atlas — Layer: ")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                .append(Component.literal(layerName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        gfx.drawString(this.font, title, gridX, 20, 0xFFFFFFFF, false);

        // Tier status
        Component tierLine = Component.literal("Perception Tier: " + cachedTier.label
                + "  |  Entries: " + cachedEntries.size())
                .withStyle(ChatFormatting.GRAY);
        gfx.drawString(this.font, tierLine, gridX, 30, 0xFFA0A0A0, false);

        // Hint when empty
        if (cachedEntries.isEmpty()) {
            Component hint = Component.literal(
                    "No observations yet. Explore the world — the atlas records what you perceive.")
                    .withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY);
            gfx.drawCenteredString(this.font, hint, this.width / 2, gridY + GRID_SIZE / 2, 0xFFFFFFFF);
        }

        // Hover tooltip
        if (hovered != null) {
            renderEntryTooltip(gfx, mouseX, mouseY, hovered);
        }

        // Legend (bottom-left)
        renderLegend(gfx, gridX, gridY + GRID_SIZE + 8);

        // Buttons (rendered last so they overlay)
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    private void renderEntryTooltip(GuiGraphics gfx, int mouseX, int mouseY, AtlasEntry e) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(e.label)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        lines.add(Component.literal("Category: " + e.category)
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Layer: " + AtlasEntry.layerName(e.layer))
                .withStyle(layerChatFormat(e.layer)));
        if (!e.observationNote.isEmpty()) {
            lines.add(Component.literal("Note: " + e.observationNote)
                    .withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC));
        }
        lines.add(Component.literal("Discovered via: " + e.discoveredVia
                + "  @ tick " + e.worldTickObserved)
                .withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.literal("Position: " + e.pos.getX() + ", " + e.pos.getY() + ", " + e.pos.getZ())
                .withStyle(ChatFormatting.DARK_GRAY));
        // Minecraft 1.20.1 Forge adds GuiGraphics#renderComponentTooltip(Font, List<Component>, int, int)
        gfx.renderComponentTooltip(this.font, lines, mouseX, mouseY);
    }

    private void renderLegend(GuiGraphics gfx, int x, int y) {
        gfx.drawString(this.font, "Legend:", x, y, 0xFFA0A0A0, false);
        int offset = 12;
        for (int layer = 1; layer <= 4; layer++) {
            boolean unlocked = isLayerUnlocked(layer);
            int color = unlocked ? colorForLayer(layer) : 0xFF404040;
            gfx.fill(x, y + offset, x + 8, y + offset + 8, color);
            gfx.fill(x, y + offset, x + 8, y + offset + 1, 0xFF000000);
            gfx.fill(x, y + offset + 7, x + 8, y + offset + 8, 0xFF000000);
            gfx.fill(x, y + offset, x + 1, y + offset + 8, 0xFF000000);
            gfx.fill(x + 7, y + offset, x + 8, y + offset + 8, 0xFF000000);
            String label = AtlasEntry.layerName(layer) + (unlocked ? "" : " (locked)");
            gfx.drawString(this.font, label, x + 12, y + offset + 1,
                    unlocked ? 0xFFFFFFFF : 0xFF606060, false);
            offset += 12;
        }
    }

    private static int colorForLayer(int layer) {
        switch (layer) {
            case 1: return 0xFF60C060; // Physical: green
            case 2: return 0xFF60A0FF; // Qi: blue
            case 3: return 0xFFFFA040; // Restriction: orange
            case 4: return 0xFFFF60FF; // Law: magenta
            default: return 0xFFC0C0C0;
        }
    }

    private static ChatFormatting layerChatFormat(int layer) {
        switch (layer) {
            case 1: return ChatFormatting.GREEN;
            case 2: return ChatFormatting.BLUE;
            case 3: return ChatFormatting.GOLD;
            case 4: return ChatFormatting.LIGHT_PURPLE;
            default: return ChatFormatting.GRAY;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
