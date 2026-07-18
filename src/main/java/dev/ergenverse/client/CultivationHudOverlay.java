package dev.ergenverse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.network.ClientCultivationCache;
import dev.ergenverse.network.CultivationSyncS2CPacket;
import dev.ergenverse.perception.PerceptionTier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

/**
 * Cultivation HUD overlay — renders Qi bar, realm name, breakthrough
 * progress, meditation indicator, and tribulation bolt counter.
 *
 * <p>Reads exclusively from {@link ClientCultivationCache} (never
 * from the server capability). Only renders when cache data is
 * available (i.e., after the first S2C sync on login).
 *
 * <p>Piece 6 of the Cultivation Vertical Slice.
 */
public final class CultivationHudOverlay {

    private CultivationHudOverlay() {}

    /** Render the cultivation HUD. Called from RenderGuiOverlayEvent.Post. */
    public static void render(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.EXPERIENCE_BAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!ClientCultivationCache.isAvailable()) return;

        CultivationSyncS2CPacket data = ClientCultivationCache.get();
        GuiGraphics gui = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // ── Top-left: Realm name + perception tier ────────────────
        String realmStr = "\u00A7a\u00A7l" + data.realmName + "\u00A7r";
        PerceptionTier tier = PerceptionTier.fromRealm(RealmId.byOrder(data.realmOrder));
        String tierStr = "\u00A77[" + tier.label + "]";
        gui.drawString(mc.font, realmStr, 4, 4, 0xFFFFFF);
        gui.drawString(mc.font, tierStr, 4, 14, 0xAAAAAA);

        // ── Qi bar (below realm name) ──────────────────────────────
        int barX = 4;
        int barY = 28;
        int barWidth = 120;
        int barHeight = 8;

        // Background
        gui.fill(barX, barY, barX + barWidth, barY + barHeight, 0x40000000);

        // Qi fill (green)
        int qiWidth = (int) (data.qi * barWidth);
        int qiColor = data.qi >= 0.80 ? 0xFF55FF55 : 0xFF55AA55; // bright green when sufficient
        if (qiWidth > 0) {
            gui.fill(barX, barY, barX + qiWidth, barY + barHeight, qiColor);
        }

        // Qi label
        String qiLabel = String.format("Qi: %.0f%%", data.qi * 100);
        gui.drawString(mc.font, qiLabel, barX + barWidth + 4, barY - 1, 0xAAAAAA);

        // ── Breakthrough progress bar (below Qi bar) ─────────────
        if (data.breakthroughProgress > 0) {
            int bpY = barY + barHeight + 4;
            int bpHeight = 4;
            gui.fill(barX, bpY, barX + barWidth, bpY + bpHeight, 0x40000000);
            int bpWidth = (int) (data.breakthroughProgress * barWidth);
            if (bpWidth > 0) {
                int bpColor = data.breakthroughProgress >= 1.0 ? 0xFFFFFF55 : 0xFFAAAAFF;
                gui.fill(barX, bpY, barX + bpWidth, bpY + bpHeight, bpColor);
            }
            String bpLabel = String.format("Breakthrough: %.1f%%", data.breakthroughProgress * 100);
            gui.drawString(mc.font, bpLabel, barX + barWidth + 4, bpY - 2, 0xAAAAAA);
        }

        // ── Karma indicator (below breakthrough) ──────────────────
        if (data.karma > 0.01) {
            int karmaY = barY + barHeight + (data.breakthroughProgress > 0 ? 12 : 4);
            int karmaColor = data.karma >= 0.5 ? 0xFFFF5555 : 0xFFAA5555;
            String karmaStr = String.format("\u00A7cKarma: %.0f%%", data.karma * 100);
            gui.drawString(mc.font, karmaStr, 4, karmaY, karmaColor);
        }

        // ── Meditation indicator (center-top) ─────────────────────
        if (ClientCultivationCache.isMeditatingVisual()) {
            String medStr = "\u00A7a\u00A7l\u25C9 Meditating \u00A7r\u00A77Qi flows through your meridians...";
            int medWidth = mc.font.width(medStr);
            gui.drawString(mc.font, medStr, (width - medWidth) / 2, 4, 0xFFFFFF);
        }

        // ── Tribulation overlay ───────────────────────────────────
        if (data.tribulationPending) {
            String tribStr = "\u00A7c\u00A7l\u26A1 TRIBULATION \u00A7r\u00A77The heavens strike!";
            int tribWidth = mc.font.width(tribStr);
            gui.drawString(mc.font, tribStr, (width - tribWidth) / 2, height / 2 - 20, 0xFFFFFF);

            // Flash effect
            int flashTimer = ClientCultivationCache.getTribulationFlashTimer();
            if (flashTimer > 0) {
                int alpha = (int) (255 * (flashTimer / 10.0));
                gui.fill(0, 0, width, height, (alpha << 24) | 0xFFFFFF);
                ClientCultivationCache.tickClientVisuals();
            }

            // Bolt counter
            int boltIdx = ClientCultivationCache.getLastBoltIndex();
            int boltTotal = ClientCultivationCache.getLastTotalBolts();
            if (boltTotal > 0) {
                String boltStr = String.format("Bolt %d/%d", boltIdx, boltTotal);
                int boltWidth = mc.font.width(boltStr);
                gui.drawString(mc.font, "\u00A7e" + boltStr, (width - boltWidth) / 2, height / 2, 0xFFFFFF);
            }
        }

        // ── Life force (bottom-left, near health bar) ────────────
        if (data.lifeForce < 99999) {
            String lifeStr;
            if (data.lifeForce >= 365) {
                lifeStr = String.format("\u00A7e\u00A7l\u2618 %d years\u00A7r", (long) data.lifeForce);
            } else {
                lifeStr = String.format("\u00A7e\u00A7l\u2618 %.0f days\u00A7r", data.lifeForce);
            }
            gui.drawString(mc.font, lifeStr, 4, height - 30, 0xAAAAAA);
        }
    }
}