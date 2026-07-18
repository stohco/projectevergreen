package dev.ergenverse.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Forge keybinding registration for the Divine Sense Atlas (M-key).
 *
 * <p>Per PROJECT_MASTER.md §5.5, the atlas opens with M. The actual
 * screen-open logic lives in {@link AtlasClientEvents} (FORGE-bus
 * subscriber listening on ClientTickEvent.Post). This class is solely
 * responsible for registering the {@link KeyMapping} on the MOD bus.
 *
 * <p>Key: {@code GLFW_KEY_M}. Category: "Ergenverse" (matches the
 * existing {@code ERKeybinds} category).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AtlasKeybind {

    private AtlasKeybind() {}

    /** Translation key for the M-key binding. */
    public static final String TRANSLATION_KEY = "key.ergenverse.divine_sense_atlas";

    /** Category translation key — matches ERKeybinds. */
    public static final String CATEGORY_KEY = "key.categories.ergenverse";

    /** The M-key KeyMapping. Consumed in {@link AtlasClientEvents} on each client tick. */
    public static final KeyMapping ATLAS_KEY = new KeyMapping(
            TRANSLATION_KEY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            CATEGORY_KEY
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ATLAS_KEY);
        Ergenverse.LOGGER.info("[Ergenverse] Divine Sense Atlas keybind registered (M).");
    }
}
