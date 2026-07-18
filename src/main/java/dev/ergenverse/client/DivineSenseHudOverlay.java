package dev.ergenverse.client;

import dev.ergenverse.network.ClientCultivationCache;
import dev.ergenverse.network.DivineSenseResultS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

import java.util.List;

/**
 * Divine Sense HUD overlay — renders the results of a divine sense pulse.
 *
 * <p>When the player presses V (divine sense keybind), the server computes
 * a pulse and sends back a {@link DivineSenseResultS2CPacket}. This overlay
 * renders the results for 5 seconds (100 ticks), then fades out.
 *
 * <p>Displays:
 * <ul>
 *   <li>Pulse radius and soul power</li>
 *   <li>Perceived entities (what the observer understands about each NPC)</li>
 *   <li>NPC reactions (intercepted, suppressed, unmasked, etc.)</li>
 *   <li>Concealed objects discovered (spirit veins, hidden caves)</li>
 *   <li>Soul fracture warning</li>
 * </ul>
 *
 * <p>Renders below the experience bar, aligned to the right side of the screen.
 * Uses the PerceptionResult color coding from the PerceptionEngine.
 */
public final class DivineSenseHudOverlay {

    private DivineSenseHudOverlay() {}

    /**
     * Render the divine sense results. Called from CultivationClientEvents
     * via RenderGuiOverlayEvent.Post on the EXPERIENCE_BAR overlay.
     */
    public static void render(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.EXPERIENCE_BAR.type()) return;

        DivineSenseResultS2CPacket result = ClientCultivationCache.getDivineSenseResult();
        if (result == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        GuiGraphics gui = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int fontHeight = mc.font.lineHeight;

        // Calculate alpha based on remaining display ticks (fade out in last 20 ticks)
        // We don't have direct access to the tick counter here, so we render at full alpha.
        int alpha = 0xFF;

        // ── Header: "Divine Sense Pulse" ──
        int y = 50;
        int rightX = width - 10;
        int maxTextWidth = 280;

        // Background panel
        int panelHeight = 20; // header
        panelHeight += result.perceivedEntities.size() * (fontHeight + 2) * 2; // entities (name + desc)
        panelHeight += result.npcReactions.size() * (fontHeight + 2); // NPC reactions
        panelHeight += result.perceivedObjects.size() * (fontHeight + 2); // concealed objects
        if (result.soulFractureInflicted) panelHeight += fontHeight + 4;
        panelHeight += 10; // padding
        panelHeight = Math.min(panelHeight, 200); // cap height

        int panelX = rightX - maxTextWidth - 10;
        gui.fill(panelX, y - 4, rightX, y + panelHeight, 0x60000000);

        // Header
        String header = "\u00A7b\u00A7lDivine Sense Pulse\u00A7r";
        String stats = String.format("\u00A77Radius: %d blocks | S_sense: %d",
                result.radius, result.sSense);
        gui.drawString(mc.font, header, panelX + 4, y, 0x55FFFF);
        y += fontHeight + 1;
        gui.drawString(mc.font, stats, panelX + 4, y, 0xAAAAAA);
        y += fontHeight + 4;

        // ── Perceived Entities ──
        if (!result.perceivedEntities.isEmpty()) {
            gui.drawString(mc.font, "\u00A7e\u00A7lPerceived Entities:\u00A7r",
                    panelX + 4, y, 0xFFFF55);
            y += fontHeight + 2;

            for (var entity : result.perceivedEntities) {
                if (y > 50 + panelHeight - 10) break; // don't overflow panel

                // Name with color based on observer tier
                String nameColor = entity.concealed ? "\u00A78" : "\u00A7f";
                gui.drawString(mc.font, nameColor + entity.perceivedName,
                        panelX + 8, y, 0xFFFFFF);
                y += fontHeight + 1;

                // Description (truncated if too long)
                String desc = entity.perceivedDescription;
                if (desc.length() > 50) desc = desc.substring(0, 47) + "...";
                gui.drawString(mc.font, "\u00A77" + desc, panelX + 12, y, 0xAAAAAA);
                y += fontHeight + 2;
            }
        }

        // ── NPC Reactions ──
        if (!result.npcReactions.isEmpty()) {
            if (y < 50 + panelHeight - 10) {
                gui.drawString(mc.font, "\u00A7c\u00A7lReactions:\u00A7r",
                        panelX + 4, y, 0xFF5555);
                y += fontHeight + 2;
            }

            for (var reaction : result.npcReactions) {
                if (y > 50 + panelHeight - 10) break;

                String outcomeColor = switch (reaction.outcome) {
                    case "intercepted" -> "\u00A7c";
                    case "suppressed" -> "\u00A7a";
                    case "unmasked" -> "\u00A7e";
                    case "ally_alerted" -> "\u00A7b";
                    default -> "\u00A77";
                };

                String reactionStr = outcomeColor + "[" + reaction.outcome + "] "
                        + "\u00A7f" + reaction.npcName;
                gui.drawString(mc.font, reactionStr, panelX + 8, y, 0xFFFFFF);
                y += fontHeight + 1;

                // Detail (truncated)
                String detailStr = reaction.detail;
                if (detailStr.length() > 55) {
                    detailStr = detailStr.substring(0, 52) + "...";
                }
                gui.drawString(mc.font, "\u00A78" + detailStr,
                        panelX + 12, y, 0x888888);
                y += fontHeight + 2;
            }
        }

        // ── Concealed Objects ──
        if (!result.perceivedObjects.isEmpty()) {
            if (y < 50 + panelHeight - 10) {
                gui.drawString(mc.font, "\u00A7d\u00A7lDiscovered:\u00A7r",
                        panelX + 4, y, 0xAA55FF);
                y += fontHeight + 2;
            }

            for (var obj : result.perceivedObjects) {
                if (y > 50 + panelHeight - 10) break;

                gui.drawString(mc.font, "\u00A7d" + obj.name + " [" + obj.kind + "]",
                        panelX + 8, y, 0xAA55FF);
                y += fontHeight + 1;

                String rewardStr = obj.reward;
                if (rewardStr.length() > 50) {
                    rewardStr = rewardStr.substring(0, 47) + "...";
                }
                gui.drawString(mc.font, "\u00A77" + rewardStr,
                        panelX + 12, y, 0xAAAAAA);
                y += fontHeight + 2;
            }
        }

        // ── Soul Fracture Warning ──
        if (result.soulFractureInflicted && y < 50 + panelHeight - 10) {
            gui.drawString(mc.font, "\u00A7c\u00A7l\u26A0 SOUL FRACTURE! \u00A7r\u00A77Your divine sense has been shattered.",
                    panelX + 4, y, 0xFF5555);
        }
    }
}