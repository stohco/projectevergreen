package dev.ergenverse.perception.atlas;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Forge capability provider for {@link DivineSenseAtlas}.
 *
 * <p>Attaches a per-player atlas to every {@link Player} entity. The atlas
 * persists across save/load via NBT serialization. The pattern mirrors
 * {@link dev.ergenverse.cultivation.CultivationCapability} — same
 * CapabilityManager + CapabilityToken + ICapabilitySerializable +
 * AttachCapabilitiesEvent wiring.
 *
 * <h2>Registration</h2>
 * <ul>
 *   <li>{@link #ATLAS} — the {@link Capability} handle, fetched via
 *       {@link CapabilityManager#get(CapabilityToken)} (Forge 47+ pattern).</li>
 *   <li>{@link RegisterCapabilitiesEvent} — registered as a MOD-bus
 *       listener via {@link AtlasCapability#register(RegisterCapabilitiesEvent)}.
 *       Wired from {@link dev.ergenverse.core.Ergenverse}'s constructor
 *       by the {@link AtlasRegistration} subscriber below.</li>
 *   <li>{@link AttachCapabilitiesEvent.Entity} — auto-registered on the
 *       FORGE bus via {@link AtlasCapability.AttachHandler}.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class AtlasCapability {

    private AtlasCapability() {}

    /** Capability key, used to identify this capability in NBT and provider lookups. */
    public static final ResourceLocation CAP_KEY =
            new ResourceLocation(Ergenverse.MOD_ID, "divine_sense_atlas");

    /** The Capability instance. Uses the modern CapabilityToken pattern. */
    public static final Capability<DivineSenseAtlas> ATLAS =
            CapabilityManager.get(new CapabilityToken<>() {});

    /**
     * Register the capability type. Called from {@link RegisterCapabilitiesEvent}.
     *
     * <p>Auto-registered via the inner {@link AtlasRegistration} class
     * (MOD-bus subscriber) so we don't have to edit {@link dev.ergenverse.core.Ergenverse}.
     */
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(DivineSenseAtlas.class);
        Ergenverse.LOGGER.info("[Ergenverse] Divine Sense Atlas capability registered.");
    }

    /**
     * Get the atlas from a player. Returns a LazyOptional — always check
     * {@code isPresent()} before resolving. If the capability is missing
     * (e.g. during a partial player spawn), the atlas UI gracefully
     * falls back to an empty state (see {@code DivineSenseAtlasScreen}).
     */
    public static LazyOptional<DivineSenseAtlas> get(Player player) {
        if (player == null) return LazyOptional.empty();
        return player.getCapability(ATLAS);
    }

    /**
     * Get the atlas from a server player, or null if missing.
     *
     * <p>Convenience wrapper for server-side code that needs a non-empty
     * atlas to mutate (e.g. {@link AtlasObservationEvents}). Returns null
     * instead of throwing — callers should handle the null case gracefully
     * (typically by logging a warning and skipping the observation).
     */
    @Nullable
    public static DivineSenseAtlas getOrNull(ServerPlayer player) {
        if (player == null) return null;
        return player.getCapability(ATLAS).orElse(null);
    }

    // ─── Provider ───────────────────────────────────────────────────

    /**
     * The capability provider that attaches a {@link DivineSenseAtlas}
     * to each player entity. Implements {@link ICapabilitySerializable}
     * so Forge auto-calls serialize/deserialize during save/load.
     */
    public static class Provider implements ICapabilitySerializable<CompoundTag> {

        private final DivineSenseAtlas atlas = new DivineSenseAtlas();
        private final LazyOptional<DivineSenseAtlas> lazy = LazyOptional.of(() -> atlas);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == ATLAS) {
                return lazy.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return atlas.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            atlas.deserializeNBT(tag);
        }
    }

    // ─── Event handlers (auto-registered via @Mod.EventBusSubscriber) ──

    /**
     * MOD-bus subscriber that registers the capability type and wires
     * up {@link #register(RegisterCapabilitiesEvent)} without editing
     * {@link dev.ergenverse.core.Ergenverse}.
     */
    @Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class AtlasRegistration {
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            AtlasCapability.register(event);
        }
    }

    /**
     * FORGE-bus subscriber that attaches the atlas capability to player
     * entities. Auto-registered; no manual {@code MinecraftForge.EVENT_BUS.register} call needed.
     */
    @Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class AttachHandler {
        @SubscribeEvent
        public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                if (event.getObject().getCapability(ATLAS).isPresent()) return; // don't double-attach
                event.addCapability(CAP_KEY, new Provider());
            }
        }
    }
}
