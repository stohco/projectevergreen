package dev.ergenverse.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ergenverse.network.BreakthroughRequestC2SPacket;
import dev.ergenverse.network.DivineSensePulseC2SPacket;
import dev.ergenverse.network.ERNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Keybinds for the cultivation system.
 *
 * <p>Piece 2 support: registers the breakthrough keybind and
 * dispatches the C2S packet on press.
 *
 * <p>Keybinds:
 * <ul>
 *   <li>B — Breakthrough attempt</li>
 *   <li>V — Divine Sense pulse (snapshot perception scan)</li>
 * </ul>
 *
 * <p>CRON-CLIENT-FIX: This class was previously on Bus.MOD only, which crashed the
 * client at main-menu launch with "IllegalArgumentException: Method onKeyInput
 * has @SubscribeEvent but takes an argument that is not a subtype of IModBusEvent".
 * Root cause: {@link InputEvent.Key} is a FORGE bus event, NOT a MOD bus event.
 * {@link RegisterKeyMappingsEvent} IS a MOD bus event. A single class cannot
 * serve both buses via one @Mod.EventBusSubscriber annotation. Fix: split into
 * two nested classes, each on the correct bus.
 */
public final class ERKeybinds {

    private ERKeybinds() {}

    /** Breakthrough attempt key — default B. */
    public static final KeyMapping BREAKTHROUGH_KEY = new KeyMapping(
            "key.ergenverse.breakthrough",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.ergenverse"
    );

    /** Divine Sense pulse key — default V.
     *
     * <p>Press to send a single divine sense snapshot pulse to the server.
     * The server scans nearby entities and concealed objects within
     * the player's divine sense radius, runs the PerceptionEngine,
     * and sends back the results. Results display in the
     * {@link DivineSenseHudOverlay} for 5 seconds.
     *
     * <p>Hold (future): continuous mode — re-pulses every second,
     * draining soul strain. Soul fracture risk if overused.
     */
    public static final KeyMapping DIVINE_SENSE_KEY = new KeyMapping(
            "key.ergenverse.divine_sense",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.ergenverse"
    );

    /**
     * MOD-bus subscriber for keybind registration.
     *
     * <p>{@link RegisterKeyMappingsEvent} is an {@code IModBusEvent} and must
     * be on {@link Mod.EventBusSubscriber.Bus#MOD}.
     */
    @Mod.EventBusSubscriber(modid = "ergenverse", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBusEvents {
        private ModBusEvents() {}

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(BREAKTHROUGH_KEY);
            event.register(DIVINE_SENSE_KEY);
        }
    }

    /**
     * FORGE-bus subscriber for key input handling.
     *
     * <p>{@link InputEvent.Key} is a runtime event on the FORGE event bus,
     * NOT an {@code IModBusEvent}. Registering it on Bus.MOD crashes the
     * client at launch with IllegalArgumentException.
     */
    @Mod.EventBusSubscriber(modid = "ergenverse", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeBusEvents {
        private ForgeBusEvents() {}

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (BREAKTHROUGH_KEY.consumeClick()) {
                ERNetwork.getChannel().sendToServer(new BreakthroughRequestC2SPacket());
            }
            if (DIVINE_SENSE_KEY.consumeClick()) {
                ERNetwork.getChannel().sendToServer(new DivineSensePulseC2SPacket());
            }
        }
    }
}